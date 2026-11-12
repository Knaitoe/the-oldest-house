package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.network.BikeNightMistPayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.495, Bike night (docs/BIKE_NIGHT_PROPOSAL.md, "Act three"): what happens to somebody in the Hollow who misses. The proposal:
 * "Missing a jump drops them onto the last island they stood on (a checkpoint), with a scare."
 *
 * <p>An island is a run of the course long enough to get going on again (CalderHollow.ISLANDS): the hill, each grave, the fences, the
 * vine, the stairs with the top, the two halves of the last stretch, the far side. The lids are a bridge, not islands. Each reader's
 * last island is their own. Somebody who is under all the ground near them has missed. They are set down on their island at its
 * place, a run short of its far end, pointing the way on: on their bike if they were riding it, stopped and upright, with whoever
 * was on its pegs; unhurt. A bike whose rider let go of it over nothing, or which lies where its rider cannot walk back to it, is
 * laid by them.
 *
 * <p>The scare is theirs alone: the mist over everything for a moment (as much of it as their comfort setting lets through), what is
 * in it heard, and a line. Anybody else sees mist where they went down and where they are.
 */
public final class BikeNightHollow {
    private BikeNightHollow(){}
    /** What the Hollow keeps for a night: each reader's last island, and how many times each has gone down. */
    static final class Course {
        final Map<UUID,Integer> stood=new HashMap<>(),fell=new HashMap<>();
    }
    /** How far under the lowest ground near them somebody is before they have missed, in blocks; and how long the mist is over them, in ticks. */
    public static final double UNDER=2;
    public static final int OVER=24;
    /** The way on: north. */
    public static final float ON=180;

    private static Vec3 from(BikeNight.Night n,Vec3 at){return at.subtract(n.base.getX(),n.base.getY(),n.base.getZ());}
    /** In the Hollow: past the hill's edge (and on what is left of its last rows), and short of the far side's ground but for its first rows. */
    public static boolean in(Vec3 at){
        return at.z<CalderHollow.EDGE+1&&at.z>CalderHollow.FAR_SIDE-2&&at.x>CalderGraves.W-1&&at.x<CalderGraves.E+2;
    }
    /** Where somebody is set down on an island, in the world. */
    static Vec3 place(BikeNight.Night n,int island){
        return CalderHollow.ISLANDS.get(Mth.clamp(island,0,CalderHollow.ISLANDS.size()-1)).place().add(n.base.getX(),n.base.getY(),n.base.getZ());
    }
    /** The island a reader last stood on tonight (the hill, until they have stood on another), and how many times they have gone down. For tests. */
    public static int island(ServerPlayer p){var n=BikeNight.night(p);return n==null?-1:n.hollow.stood.getOrDefault(p.getUUID(),CalderHollow.HILL);}
    public static int fell(ServerPlayer p){var n=BikeNight.night(p);return n==null?0:n.hollow.fell.getOrDefault(p.getUUID(),0);}

    static void tick(ServerLevel l,BikeNight.Night n,List<ServerPlayer> here){
        for(var p:here){
            if(!p.isAlive()||p.isSpectator())continue;
            NightBike bike=p.getVehicle() instanceof NightBike b?b:null;
            // Somebody on the pegs goes where the one pedalling goes.
            if(bike!=null&&bike.getFirstPassenger()!=p)continue;
            Entity body=bike!=null?bike:p;var at=from(n,body.position());
            if(!in(at))continue;
            // A bike is on the ground when it says so. A body on foot is where its reader's own game says, so it is asked of the ground.
            int island=bike!=null&&!bike.onGround()?-1:CalderHollow.stood(at.x,at.y,at.z);
            if(island>=0){
                if(bike!=null)for(var e:bike.getPassengers())n.hollow.stood.put(e.getUUID(),island);
                else n.hollow.stood.put(p.getUUID(),island);
            }else if(at.y<CalderHollow.floor(at.z)-UNDER)missed(l,n,p);
        }
    }

    /** Somebody has missed: back onto their island, with a scare. */
    static void missed(ServerLevel l,BikeNight.Night n,ServerPlayer p){
        int island=n.hollow.stood.getOrDefault(p.getUUID(),CalderHollow.HILL);var to=place(n,island);var from=p.position();
        if(p.getVehicle() instanceof NightBike bike){
            var riders=List.copyOf(bike.getPassengers());
            bike.putBack(to.x,to.y,to.z,ON);
            for(var e:riders)if(e instanceof ServerPlayer r){
                r.resetFallDistance();r.lookAt(EntityAnchorArgument.Anchor.EYES,to.add(0,1,-200));
                scare(l,n,r,BikeNightTexts.UNDER_RIDING);
            }
        }else{
            if(p.isPassenger())p.stopRiding();
            p.connection.teleport(to.x,to.y,to.z,ON,0);p.setDeltaMovement(Vec3.ZERO);p.hurtMarked=true;p.resetFallDistance();
            bring(l,n,p,island,to);
            scare(l,n,p,BikeNightTexts.UNDER_WALKING);
        }
        // For anybody watching: mist where they went down, and where they are now.
        l.sendParticles(ParticleTypes.WHITE_SMOKE,from.x,from.y+.5,from.z,24,.5,.6,.5,.02);
        l.sendParticles(ParticleTypes.WHITE_SMOKE,to.x,to.y+.6,to.z,16,.4,.5,.4,.01);
    }
    private static void scare(ServerLevel l,BikeNight.Night n,ServerPlayer p,String line){
        n.hollow.fell.merge(p.getUUID(),1,Integer::sum);
        HousePackets.send(p,new BikeNightMistPayload(BikeNightGraveyard.mist(l,n.base,p),OVER));
        p.playNotifySound(BikeNightRegistry.UNDER.get(),SoundSource.AMBIENT,.9F,.95F+l.random.nextFloat()*.1F);
        BikeNight.cue(p,line);
    }

    /** Somebody on foot has been set down: their bike, if it lies in the Hollow anywhere but on their island, is laid by them. */
    private static void bring(ServerLevel l,BikeNight.Night n,ServerPlayer p,int island,Vec3 to){
        for(var make:NightBike.Make.values()){
            int k=make.ordinal();var at=n.at[k];
            if(at==null||!p.getUUID().equals(n.owner[k]))continue;
            var lies=from(n,Vec3.atBottomCenterOf(at));
            if(!in(lies)||CalderHollow.stood(lies.x,lies.y,lies.z)==island)continue;
            var laid=beside(l,to);if(laid==null)continue;
            var s=l.getBlockState(at);
            if(s.is(BikeNightRegistry.BIKE_BLOCK.get())&&s.getValue(NightBikeBlock.MAKE)==make)l.setBlock(at,Blocks.AIR.defaultBlockState(),3);
            l.setBlock(laid,NightBikeBlock.of(make,Direction.NORTH,true),3);n.at[k]=laid;BikeNight.remember(l,n);
        }
    }
    /**
     * A bike has been let go of with nothing under it (NightBikes.Night.parked). If its rider is in the Hollow, it is laid by their
     * island's place, where they are about to be. Null if they are not, when it goes back to the rack as it always has.
     */
    static @Nullable BlockPos lost(ServerLevel l,BikeNight.Night n,@Nullable UUID reader){
        var p=reader==null?null:l.getServer().getPlayerList().getPlayer(reader);
        if(p==null||BikeNight.night(p)!=n||!in(from(n,p.position())))return null;
        return beside(l,place(n,n.hollow.stood.getOrDefault(reader,CalderHollow.HILL)));
    }
    /**
     * A cell by an island's place to lay a bike in: beside where a body stands there, or a little behind, on whole ground. Not the
     * cell they stand in.
     */
    public static @Nullable BlockPos beside(ServerLevel l,Vec3 to){
        int bx=Mth.floor(to.x),by=Mth.floor(to.y+.5),bz=Mth.floor(to.z);
        for(int back=0;back<=4;back++)for(int dx:new int[]{1,-1,2,-2,0})for(int dy:new int[]{0,1,-1}){
            var at=new BlockPos(bx+dx,by+dy,bz+back);
            if(back==0&&at.getX()+1>to.x-.35&&at.getX()<to.x+.35)continue;
            var here=l.getBlockState(at);
            if((here.isAir()||here.canBeReplaced())&&l.getBlockState(at.below()).isFaceSturdy(l,at.below(),Direction.UP))return at;
        }
        return null;
    }
    static void reset(ServerLevel l,BikeNight.Night n){n.hollow=new Course();}
}
