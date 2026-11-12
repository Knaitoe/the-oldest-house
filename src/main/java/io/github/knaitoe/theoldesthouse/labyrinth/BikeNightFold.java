package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.BikeNightMistPayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/**
 * 0.4.521, Bike night: the cemetery's fold. The owner, playing it on 12 November: "there should also be a transition in the cemetery
 * to a new zone, so this isn't visible - the cemetery should seem to suddenly expand, the player shouldn't see it looming in the
 * horizon or be able to revisit it come sunset".
 *
 * <p>The Hollow used to begin past the cemetery's last row, and its floating ground stood over the hill for anybody in the town to
 * see. It lies {@link #ENTRY} blocks further north now, further than a client draws anything, past a cemetery of its own
 * (CalderGraves.expanse) that is the town's cemetery as it was, going on where the town's stops. The town's cemetery has its back railings
 * again, and behind them the lane, its wall and a wood.
 *
 * <p>While the chase is on (the cemetery's gate has opened for it), somebody who goes north past {@link #SEAM}, a few rows short of
 * the back railings, is carried {@link #ENTRY} blocks north, with their bike, whoever is on its pegs and its way, under a surge of
 * the mist; they come out of it in the expanse, and as it thins there are rows on every side where the railings were. Back south past
 * the fold's other side ({@link #BACK_SEAM}), anybody is carried back. Through the back gate on the Hollow's far side, they are
 * carried to the same gate in the town cemetery's back railings ({@link #EXIT}), onto the lane above the town, with Calder Falls
 * down the hill behind them. Gus goes the same ways after his friend. Somebody the Hollow has had, or who is through, and anybody
 * before the chase is on or after the night is over, comes to the back railings and nothing else: there is no seeing the Hollow, and
 * no going back to it, from the town.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class BikeNightFold {
    private BikeNightFold(){}
    /** How far north the fold carries somebody: the expanse is the town's cemetery this far north, wider and longer. */
    public static final int ENTRY=-1024;
    /** How many more rows the expanse has north of where the town's back railings are: the Hollow begins this much further on (its ground is where it was before 0.4.521, ENTRY-EXTRA blocks north). */
    public static final int EXTRA=32;
    /** The fold's two sides: north of SEAM in the town somebody in the chase is carried; south of BACK_SEAM in the expanse, anybody is carried back. */
    public static final double SEAM=-257.5,BACK_SEAM=SEAM+ENTRY+1;
    /** Through the back gate on the Hollow's far side: how far somebody is carried, to the town cemetery's own back gate. */
    public static final int EXIT=CalderGraves.BACK-CalderHollow.BACK;
    /** The expanse's south end, and how far either way it reaches (as the town's cemetery, between its side railings). */
    public static final int SOUTH=-216+ENTRY,WEST=CalderGraves.W,EAST=CalderGraves.E;
    /** How long the mist is over somebody being carried, in ticks; and the expanse's own mist, in a hundred of the cemetery's thickest. */
    public static final int SURGE=30,MIST=45;

    // ------------------------------------------------------------------------------------------------ where things are
    /** Whether a place (its z, from the base) is on the fold's far side: the expanse and the Hollow. Halfway between is nowhere anybody stands. */
    public static boolean far(double z){return z<(CalderGraves.BACK+SOUTH)/2.0;}
    /**
     * The expanse's mist at a row (from the base), in a hundred of the cemetery's thickest: thin where the fold lets out, so that
     * there are rows on every side, and thickening to the cemetery's thickest by the hill's back, as the town's last rows did.
     */
    public static float mist(double z){
        double t=Math.max(0,Math.min(1,(BACK_SEAM-z)/(BACK_SEAM-(CalderHollow.EDGE+6))));
        return (float)(MIST+(BikeNightGraveyard.MIST_BACK-MIST)*t);
    }
    /** Whether a place (from the base) is in the expanse: the fold's far side short of the Hollow's edge. */
    public static boolean expanse(double x,double z){return far(z)&&z>CalderHollow.EDGE+.5&&z<SOUTH+1&&x>WEST&&x<EAST+1;}
    /** Whether a place (its z, from the base) is the lane behind the town cemetery's back gate, or anything north of that in the town. */
    public static boolean pastBack(double z){return !far(z)&&z<CalderGraves.BACK+.5;}
    /**
     * Where {@code to} is for somebody at {@code from} (both from the base): {@code to} itself if they are on the same side of the
     * fold, or else the place on {@code from}'s side that leads to it. From the town, anywhere on the far side is through the fold,
     * at the fold; and the lane behind the Hollow's far side is the town's own lane. From the far side, the town's lane is past the
     * Hollow's back gate; the rest of the town is back through the fold.
     */
    public static Vec3 near(Vec3 from,Vec3 to){
        boolean a=far(from.z),c=far(to.z);
        // The lane above the town is got to the way its riders came: through the fold and out of the Hollow's back gate.
        if(!a&&!c&&pastBack(to.z)&&!pastBack(from.z))return new Vec3(LINE,to.y,SEAM-1.5);
        if(a==c)return to;
        if(!a){
            if(to.z<CalderHollow.BACK+.5)return to.add(0,0,EXIT);
            return new Vec3(LINE,to.y,Math.max(to.z-ENTRY,SEAM-1.5));
        }
        if(pastBack(to.z))return to.add(0,0,-EXIT);
        return new Vec3(LINE,to.y,Math.min(to.z+ENTRY,BACK_SEAM+1.5));
    }
    /** The middle of the gate's line, which every row keeps clear on both sides of the fold: the way to the fold and from it. */
    static final double LINE=(CalderGraves.PATH_W+CalderGraves.PATH_E+1)/2.0;
    /** As {@link #near(Vec3,Vec3)}, in the world. */
    public static Vec3 near(BlockPos b,Vec3 from,Vec3 to){
        var o=Vec3.atLowerCornerOf(b);return near(from.subtract(o),to.subtract(o)).add(o);
    }
    /**
     * The rows of Bike night that are built on, from the base, as {min z, max z} pairs: the fold's far side (the expanse and the
     * Hollow, to the lane past it) and the town to the wood behind its cemetery. The thousand rows between are never built, and
     * nothing that loads or looks over the scene's ground (its chunks while it is built, a copy's, the architecture views) goes there.
     */
    public static List<int[]> built(){
        var r=LabyrinthPlace.BIKE_NIGHT.room();
        return List.of(new int[]{r.minZ(),SOUTH+16},new int[]{CalderGraves.WOOD_N-16,r.maxZ()});
    }

    // ------------------------------------------------------------------------------------------------ crossing it
    static void tick(ServerLevel l,BikeNight.Night n,List<ServerPlayer> here){
        hold(l,n,here);
        gates(l,n);
        for(var p:here){
            if(!p.isAlive()||p.isSpectator())continue;
            Entity body=p.getVehicle() instanceof NightBike bike?bike:p.getVehicle()==null?p:null;
            // Somebody on the pegs goes where the one pedalling goes; somebody on anything else (a swing, a seat) is not carried.
            if(body==null||body!=p&&((NightBike)body).getFirstPassenger()!=p)continue;
            double by=crossing(n,p,BikeNightHollow.from(n,body.position()));
            if(by!=0)carry(l,n,body,by);
        }
        gus(l,n);
    }
    /** How far somebody at a place (from the base) is to be carried now: 0 if not. */
    static double crossing(BikeNight.Night n,@Nullable ServerPlayer p,Vec3 at){
        if(!far(at.z)){
            if(at.z<SEAM&&at.z>CalderGraves.BACK+.5&&at.x>WEST&&at.x<EAST+1&&open(n,p))return ENTRY;
            return 0;
        }
        if(at.z>BACK_SEAM)return -ENTRY;
        if(at.z<CalderHollow.BACK-.5&&Math.abs(at.x)<=CalderHollow.SIDE+1)return EXIT;
        return 0;
    }
    /** Whether the fold takes somebody through it: the chase is on, and the Hollow has neither had them nor let them out. Gus (null) goes after whoever it took. */
    static boolean open(BikeNight.Night n,@Nullable ServerPlayer p){
        if(!n.yard.gateOpen)return false;
        if(p==null)return true;
        var id=p.getUUID();
        if(n.hollow.taken.contains(id)||n.hollow.through.contains(id))return false;
        var own=BikeNight.own(p);
        return !own.getBoolean("Taken")&&!own.getBoolean("Through");
    }
    /** Somebody carried through the fold: under the mist, their bike with them and whoever is on it, its way and its lean kept. */
    static void carry(ServerLevel l,BikeNight.Night n,Entity body,double by){
        var riders=body instanceof NightBike bike?List.copyOf(bike.getPassengers()):List.<Entity>of(body);
        if(body instanceof RiddenBike bike)bike.fold(by);
        // On foot: where they are, by the fold's width (the connection takes the place itself; the flags only keep the client's way on).
        else if(body instanceof ServerPlayer p)p.connection.teleport(p.getX(),p.getY(),p.getZ()+by,p.getYRot(),p.getXRot(),EnumSet.allOf(RelativeMovement.class));
        else body.teleportTo(body.getX(),body.getY(),body.getZ()+by);
        for(var e:riders)if(e instanceof ServerPlayer r){
            HousePackets.send(r,new BikeNightMistPayload(BikeNightGraveyard.mist(l,n.base,r),SURGE));
            r.playNotifySound(BikeNightRegistry.FOLD.get(),SoundSource.AMBIENT,.8F,.9F+l.random.nextFloat()*.15F);
            n.folds.merge(r.getUUID(),1,Integer::sum);
        }
    }
    /**
     * Gus: through the fold after his friend, the same ways, on his bike. He keeps his distance from where he is going
     * (BikeNightGus.KEEP), so the fold takes him that much sooner than it takes a reader, either way.
     */
    static void gus(ServerLevel l,BikeNight.Night n){
        var g=n.gus;if(g.bike==null||!(l.getEntity(g.bike) instanceof PackBike bike)||!bike.isAlive())return;
        var at=BikeNightHollow.from(n,bike.position());
        var friend=g.friend==null?null:l.getServer().getPlayerList().getPlayer(g.friend);
        if(friend==null||BikeNight.night(friend)!=n)return;
        double fz=BikeNightHollow.from(n,friend.position()).z,reach=BikeNightGus.KEEP+1;
        // Where his friend is only to be got to through the fold from the town: on its far side, or out of the back gate in the lane.
        boolean beyond=far(fz)||pastBack(fz);
        if(!far(at.z)){
            if(beyond&&open(n,null)&&at.z<SEAM+reach&&at.z>CalderGraves.BACK+.5&&at.x>WEST&&at.x<EAST+1)carry(l,n,bike,ENTRY);
        }else if(at.z>BACK_SEAM-reach){
            if(!beyond)carry(l,n,bike,-ENTRY);
        }else if(!far(fz)&&(crossing(n,null,at)==EXIT||pastBack(fz)&&n.hollow.gate&&(!n.yard.capHung||g.capBack)&&at.z<CalderHollow.BACK-.5+reach&&at.x>CalderGraves.PATH_W&&at.x<CalderGraves.PATH_E+1)){
            // Out of the back gate after them, from his distance short of it, once his cap is down off its post (the post is inside
            // that distance): into the town's lane where a reader comes out, past the town's back line, so the fold has nothing to
            // take him back in for.
            carry(l,n,bike,Math.min(EXIT,CalderGraves.BACK-.6-at.z));
        }
    }

    /**
     * The two back gates are one gate: open once the Hollow's has been opened tonight, shut until then. Each is set to that as its
     * ground is in, so a gate left open when its ground went out (a night over with nobody near it) is shut again.
     */
    static void gates(ServerLevel l,BikeNight.Night n){
        for(int dz:new int[]{0,EXIT})if(CalderHollow.backGateLoaded(l,n.base,dz)&&CalderHollow.backGateOpen(l,n.base,dz)!=n.hollow.gate)CalderHollow.backGate(l,n.base,n.hollow.gate,dz);
    }

    // ------------------------------------------------------------------------------------------------ the ground either side, held
    /** The night's held chunks: the fold's two sides and the two back gates, across the cemetery's width, while somebody is near any of them in the chase. */
    private static final TicketType<ChunkPos> TICKET=TicketType.create("bike_night_fold",Comparator.comparingLong(ChunkPos::toLong));
    private static final Map<Long,List<ChunkPos>> HELD=new HashMap<>();
    /** Rows north of the town's last bank, and rows of the far side, in which somebody is near enough to the fold for its ground to be held. */
    static final int NEAR_BANK=CalderGraves.BANKS[CalderGraves.BANKS.length-1];
    static void hold(ServerLevel l,BikeNight.Night n,List<ServerPlayer> here){
        boolean want=false;
        if(n.yard.gateOpen)for(var p:here){var z=p.getZ()-n.base.getZ();if(z<NEAR_BANK||far(z)){want=true;break;}}
        var key=n.base.asLong();var held=HELD.get(key);
        if(want==(held!=null))return;
        if(!want){release(l,key);return;}
        var chunks=new ArrayList<ChunkPos>();
        for(double z:new double[]{SEAM,BACK_SEAM-1,CalderHollow.BACK,CalderGraves.BACK})
            for(int x=WEST;x<=EAST+16;x+=16){var c=new ChunkPos(n.base.offset(Math.min(x,EAST),0,(int)Math.floor(z)));if(!chunks.contains(c))chunks.add(c);}
        for(var c:chunks)l.getChunkSource().addRegionTicket(TICKET,c,2,c);
        HELD.put(key,chunks);
    }
    static void release(ServerLevel l,long key){
        var held=HELD.remove(key);if(held!=null)for(var c:held)l.getChunkSource().removeRegionTicket(TICKET,c,2,c);
    }
    /** The night over: whatever it held, let go. */
    static void reset(ServerLevel l,BikeNight.Night n){release(l,n.base.asLong());}
    /** For tests: whether a night's fold is holding its ground. */
    public static boolean holding(BlockPos b){return HELD.containsKey(b.asLong());}
    public static void clear(){HELD.clear();}
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){clear();}
    /** How many times a reader has been carried through the fold tonight. For tests. */
    public static int folds(ServerPlayer p){var n=BikeNight.night(p);return n==null?0:n.folds.getOrDefault(p.getUUID(),0);}
}
