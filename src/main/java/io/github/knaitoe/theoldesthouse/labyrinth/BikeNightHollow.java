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
 * <p>0.4.497: the long way's run gives way under whoever is on it (see {@link #run}).
 *
 * <p>The scare is theirs alone: the mist over everything for a moment (as much of it as their comfort setting lets through), what is
 * in it heard, and a line. Anybody else sees mist where they went down and where they are.
 */
public final class BikeNightHollow {
    private BikeNightHollow(){}
    /** What the Hollow keeps for a night: each reader's last island, and how many times each has gone down. */
    static final class Course {
        final Map<UUID,Integer> stood=new HashMap<>(),fell=new HashMap<>();
        /** The long way's run: when it began to give way (-1 while it is whole and nobody is on it), how many of its rows are gone, and when it is back (-1 unless it is all gone). */
        long runAt=-1,runBackAt=-1;int runGone;
        /** 0.4.504, the steep way's bank: when somebody was first on it (-1 while nobody has been), whether it is gone, and when it is back. */
        long bankAt=-1,bankBackAt=-1;boolean bankGone;
        /** 0.4.505, the tight way's tendril: as the long way's run. */
        long curlAt=-1,curlBackAt=-1;int curlGone;
        /** 0.4.498: the ways each reader has had under them other than the plain one; whether the back gate is open; who is out by it. */
        final Map<UUID,java.util.TreeSet<String>> ways=new HashMap<>();boolean gate;final java.util.Set<UUID> through=new java.util.HashSet<>();
        /** 0.4.499: who a specter has caught tonight. */
        final java.util.Set<UUID> taken=new java.util.HashSet<>();
    }
    /**
     * 0.4.499, caught. "Being caught by any of them is the Hollow's failure": "The screen goes to mist", and "they come round at
     * dawn outside the graveyard's front gate, alive, the bike gone." The mist is over them for TAKEN_OVER ticks (as much as their
     * comfort setting lets through); they are on the pavement outside the front gate, on foot, unhurt; their bike is gone from
     * under them or from where it lay, and is nobody's for the rest of the night; it goes in their record with what caught them;
     * and they take no other bike tonight. Whoever was on their pegs is left where they were, over whatever was under them.
     */
    public static final int TAKEN_OVER=80;
    static void taken(ServerLevel l,BikeNight.Night n,ServerPlayer p,String by){
        if(!n.hollow.taken.add(p.getUUID()))return;
        var from=p.position();
        if(p.getVehicle() instanceof NightBike bike){if(bike.getFirstPassenger()==p)bike.taken();else p.stopRiding();}
        for(var make:NightBike.Make.values()){
            int k=make.ordinal();if(!p.getUUID().equals(n.owner[k])||n.at[k]==null)continue;
            var s=l.getBlockState(n.at[k]);
            if(s.is(BikeNightRegistry.BIKE_BLOCK.get())&&s.getValue(NightBikeBlock.MAKE)==make)l.setBlock(n.at[k],Blocks.AIR.defaultBlockState(),3);
            n.at[k]=null;
        }
        BikeNight.remember(l,n);
        var to=outside(n);
        if(p.isPassenger())p.stopRiding();
        p.connection.teleport(to.x,to.y,to.z,ON,0);p.setDeltaMovement(Vec3.ZERO);p.hurtMarked=true;p.resetFallDistance();
        var own=BikeNight.own(p);own.putBoolean("Taken",true);own.putString("TakenBy",by);BikeNight.save(p,own);
        HousePackets.send(p,new BikeNightMistPayload(BikeNightGraveyard.mist(l,n.base,p),TAKEN_OVER));
        p.playNotifySound(BikeNightRegistry.UNDER.get(),SoundSource.AMBIENT,.9F,.8F);
        BikeNight.cue(p,BikeNightTexts.TAKEN);
        l.sendParticles(ParticleTypes.WHITE_SMOKE,from.x,from.y+.8,from.z,40,.6,.8,.6,.02);
    }
    /** Outside the cemetery's front gate, on the town's side, facing it. */
    static Vec3 outside(BikeNight.Night n){return new Vec3(n.base.getX()+BikeNightGraveyard.GATE_X,n.base.getY(),n.base.getZ()+CalderTown.RAILS+3.5);}
    /** Whether a specter has caught a reader tonight. For tests. */
    public static boolean taken(ServerPlayer p){var n=BikeNight.night(p);return n!=null&&n.hollow.taken.contains(p.getUUID());}
    /**
     * 0.4.498, the way out. "The Hollow folds back down into the graveyard's far side. The riders come out through the back gate
     * onto a quiet lane above the town, with the lights of Calder Falls on again below." The back gate is shut until somebody is on
     * the far side's ground within GATE_NEAR blocks of it; then it stands open for the rest of the night. Whoever goes through it
     * onto the lane is through: it goes in their record with what they were on, the ways they took and how often they went down,
     * they are told, and the town's lamps come back on.
     */
    public static final int GATE_NEAR=16;
    /**
     * 0.4.497, the long way's run (CalderHollow.LONG). "long: a run only the Comet's top speed carries": once somebody is on it, it
     * gives way from its near end, GRACE ticks later and then PACE rows a tick: faster than a bike is pedalled (RiddenBike.TOP),
     * slower than the Comet, or the Rattler while its card has it. All of it goes, whoever is still on it; BACK ticks after the
     * last row it is whole again.
     */
    public static final int GRACE=6,BACK=100;public static final float PACE=.4F;
    /** How far under the lowest ground near them somebody is before they have missed, in blocks; and how long the mist is over them, in ticks. */
    public static final double UNDER=2;
    public static final int OVER=24;
    /** The way on: north. */
    public static final float ON=180;

    static Vec3 from(BikeNight.Night n,Vec3 at){return at.subtract(n.base.getX(),n.base.getY(),n.base.getZ());}
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
        run(l,n,here);
        bank(l,n,here);
        curl(l,n,here);
        out(l,n,here);
        for(var p:here){
            if(!p.isAlive()||p.isSpectator()||n.hollow.taken.contains(p.getUUID()))continue;
            NightBike bike=p.getVehicle() instanceof NightBike b?b:null;
            // Somebody on the pegs goes where the one pedalling goes.
            if(bike!=null&&bike.getFirstPassenger()!=p)continue;
            Entity body=bike!=null?bike:p;var at=from(n,body.position());
            if(!in(at))continue;
            // A bike is on the ground when it says so. A body on foot is where its reader's own game says, so it is asked of the ground.
            int island=bike!=null&&!bike.onGround()?-1:CalderHollow.stood(at.x,at.y,at.z);
            if(island>=0){
                String way=CalderHollow.ISLANDS.get(island).way();
                for(var e:bike!=null?bike.getPassengers():List.<Entity>of(p)){
                    n.hollow.stood.put(e.getUUID(),island);
                    if(!way.isEmpty()&&!way.equals("plain"))n.hollow.ways.computeIfAbsent(e.getUUID(),k->new java.util.TreeSet<>()).add(way);
                }
            }else if(at.y<CalderHollow.floor(at.x,at.z)-UNDER)missed(l,n,p);
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
     * A cell by an island's place to lay a bike in: beside where a body stands there, or a little behind, on whole ground that
     * stays (not the long way's run). Not the cell they stand in, unless there is nowhere else.
     */
    public static @Nullable BlockPos beside(ServerLevel l,Vec3 to){
        int bx=Mth.floor(to.x),by=Mth.floor(to.y+.5),bz=Mth.floor(to.z);
        for(int back=0;back<=4;back++)for(int dx:new int[]{1,-1,2,-2,0})for(int dy:new int[]{0,1,-1}){
            var at=new BlockPos(bx+dx,by+dy,bz+back);
            if(back==0&&at.getX()+1>to.x-.35&&at.getX()<to.x+.35)continue;
            var here=l.getBlockState(at);
            if((here.isAir()||here.canBeReplaced())&&l.getBlockState(at.below()).isFaceSturdy(l,at.below(),Direction.UP)&&!onRun(l,at))return at;
        }
        var own=BlockPos.containing(to.x,to.y+.01,to.z);var there=l.getBlockState(own);
        return there.isAir()||there.canBeReplaced()?own:null;
    }
    /** The back gate opening for whoever is on the far side, and whoever goes through it. */
    static void out(ServerLevel l,BikeNight.Night n,List<ServerPlayer> here){
        var c=n.hollow;int level=CalderGraves.level(CalderHollow.EDGE);
        var gateAt=n.base.offset(0,level,CalderHollow.BACK);if(!l.isLoaded(gateAt))return;
        for(var p:here){
            if(!p.isAlive()||p.isSpectator())continue;
            var at=from(n,p.getVehicle() instanceof NightBike bike?bike.position():p.position());
            if(Math.abs(at.y-level)>1.5||Math.abs(at.x)>CalderHollow.SIDE+1)continue;
            if(!c.gate&&at.z<=CalderHollow.FAR_SIDE+1&&at.z>CalderHollow.BACK&&at.z-CalderHollow.BACK<GATE_NEAR&&Math.abs(at.x)<GATE_NEAR){
                c.gate=true;CalderHollow.backGate(l,n.base,true);
                l.playSound(null,gateAt,BikeNightRegistry.CHAIN.get(),SoundSource.BLOCKS,1F,.8F);
            }
            if(c.gate&&at.z<CalderHollow.BACK-.5&&at.z>CalderHollow.FAR&&c.through.add(p.getUUID()))through(l,n,p);
        }
    }
    private static void through(ServerLevel l,BikeNight.Night n,ServerPlayer p){
        var own=BikeNight.own(p);var ways=n.hollow.ways.get(p.getUUID());
        own.putBoolean("Through",true);
        own.putString("ThroughOn",p.getVehicle() instanceof NightBike bike?bike.make().getSerializedName():"foot");
        own.putBoolean("ThroughOnPegs",p.getVehicle() instanceof NightBike carried&&carried.getFirstPassenger()!=p);
        own.putString("HollowWays",ways==null||ways.isEmpty()?"plain":String.join(",",ways));
        own.putInt("HollowFell",n.hollow.fell.getOrDefault(p.getUUID(),0));
        BikeNight.save(p,own);
        BikeNightGraveyard.lightsOn(l,n);
        BikeNight.cue(p,BikeNightTexts.THROUGH);
    }
    /** Whether the back gate is open in this park, and whether a reader is through it. For tests. */
    public static boolean gateOpen(BlockPos b){var n=BikeNight.night(b);return n!=null&&n.hollow.gate;}
    public static boolean through(ServerPlayer p){var n=BikeNight.night(p);return n!=null&&n.hollow.through.contains(p.getUUID());}
    /** The long way's run, giving way and coming back. */
    static void run(ServerLevel l,BikeNight.Night n,List<ServerPlayer> here){
        var c=n.hollow;
        if(!l.isLoaded(n.base.offset(CalderHollow.RUN_X0,0,CalderHollow.RUN_SOUTH))||!l.isLoaded(n.base.offset(CalderHollow.RUN_X0,0,CalderHollow.RUN_SOUTH-CalderHollow.RUN_ROWS)))return;
        if(c.runAt<0){
            if(c.runBackAt>=0){
                if(n.tick<c.runBackAt)return;
                for(int i=0;i<CalderHollow.RUN_ROWS;i++)CalderHollow.runRow(l,n.base,i,true);
                var mid=n.base.offset(CalderHollow.RUN_X0+1,0,CalderHollow.RUN_SOUTH-CalderHollow.RUN_ROWS/2);
                l.sendParticles(ParticleTypes.WHITE_SMOKE,mid.getX(),mid.getY()+.1,mid.getZ()+.5,60,.6,.1,CalderHollow.RUN_ROWS/2.0,.01);
                c.runGone=0;c.runBackAt=-1;return;
            }
            // It begins to give way when somebody is on it: a bike that is on the ground there, or somebody on their feet.
            for(var p:here){
                if(!p.isAlive()||p.isSpectator())continue;
                Entity body=p.getVehicle() instanceof NightBike b?b:p;var at=from(n,body.position());
                if(Math.abs(at.y)<.1&&CalderHollow.runRow(at.x,at.z)>=0&&(body==p||body.onGround())){c.runAt=n.tick;break;}
            }
            return;
        }
        int gone=Mth.clamp((int)Math.floor((n.tick-c.runAt-GRACE)*PACE),0,CalderHollow.RUN_ROWS);
        while(c.runGone<gone){
            int row=c.runGone++;CalderHollow.runRow(l,n.base,row,false);
            var at=n.base.offset(CalderHollow.RUN_X0+1,0,CalderHollow.RUN_SOUTH-row);
            l.sendParticles(ParticleTypes.WHITE_SMOKE,at.getX(),at.getY()-.3,at.getZ()+.5,5,.6,.1,.3,.01);
            if(row%3==0)l.playSound(null,at.getX(),at.getY(),at.getZ()+.5,BikeNightRegistry.DROP.get(),SoundSource.BLOCKS,.8F,.9F+l.random.nextFloat()*.2F);
        }
        if(c.runGone>=CalderHollow.RUN_ROWS){c.runAt=-1;c.runBackAt=n.tick+BACK;}
    }
    /**
     * 0.4.504, the steep way's bank (CalderHollow.STEEP). "steep: a climb only the Hare gets going up in time": BANK_HOLDS ticks
     * after somebody is first on it (a bike on the ground there, or somebody on their feet), all of it slides away, whoever is
     * still on it; BANK_BACK ticks later it is there again, when nobody is where it was.
     */
    public static final int BANK_HOLDS=80,BANK_BACK=100;
    static void bank(ServerLevel l,BikeNight.Night n,List<ServerPlayer> here){
        var c=n.hollow;var foot=n.base.offset(CalderHollow.BANK_X0,CalderHollow.BANK_FOOT,CalderHollow.BANK_SOUTH);
        if(!l.isLoaded(foot)||!l.isLoaded(foot.offset(0,0,-CalderHollow.BANK_ROWS)))return;
        if(c.bankGone){
            if(n.tick<c.bankBackAt)return;
            // Not under anybody who is where it was.
            for(var p:here){var at=from(n,p.position());if(CalderHollow.bankRow(at.x,at.z)>=0&&at.y>CalderHollow.BANK_FOOT-3&&at.y<CalderHollow.BANK_FOOT+CalderHollow.BANK_ROWS+2)return;}
            CalderHollow.bank(l,n.base,true);c.bankGone=false;c.bankBackAt=-1;
            l.sendParticles(ParticleTypes.WHITE_SMOKE,foot.getX()+1,foot.getY()+1.5,foot.getZ()-1,40,.6,1,1,.01);return;
        }
        if(c.bankAt<0){
            for(var p:here){
                if(!p.isAlive()||p.isSpectator())continue;
                Entity body=p.getVehicle() instanceof NightBike b?b:p;var at=from(n,body.position());int row=CalderHollow.bankRow(at.x,at.z);
                if(row>=0&&Math.abs(at.y-CalderHollow.BANK_FOOT-1-row)<.1&&(body==p||body.onGround())){c.bankAt=n.tick;break;}
            }
            return;
        }
        if(n.tick-c.bankAt<BANK_HOLDS)return;
        CalderHollow.bank(l,n.base,false);c.bankGone=true;c.bankAt=-1;c.bankBackAt=n.tick+BANK_BACK;
        var earth=new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK,BikeNightRegistry.material("hollow_earth").defaultBlockState());
        l.sendParticles(earth,foot.getX()+1,foot.getY()+1,foot.getZ()-1,120,.7,1.2,1.2,.05);
        l.playSound(null,foot.getX()+1,foot.getY()+1,foot.getZ()-1,BikeNightRegistry.SLIDE_AWAY.get(),SoundSource.BLOCKS,1F,.95F+l.random.nextFloat()*.1F);
    }
    /**
     * 0.4.505, the tight way's tendril (CalderHollow.TIGHT). It curls up from its near end behind whoever is in it: CURL_GRACE
     * ticks after somebody is first on it, and then CURL_PACE rows a tick, its walls and gates with it. All of it goes, whoever
     * is still in it; CURL_BACK ticks after its last row it is whole again, when nobody is where it was.
     */
    public static final int CURL_GRACE=10,CURL_BACK=100;public static final float CURL_PACE=.21F;
    static void curl(ServerLevel l,BikeNight.Night n,List<ServerPlayer> here){
        var c=n.hollow;int rows=CalderHollow.TIGHT_ROWS;
        if(!l.isLoaded(n.base.offset(CalderHollow.TIGHT_X0,0,CalderHollow.TIGHT_SOUTH))||!l.isLoaded(n.base.offset(CalderHollow.TIGHT_X0,0,CalderHollow.TIGHT_SOUTH-rows)))return;
        if(c.curlAt<0){
            if(c.curlBackAt>=0){
                if(n.tick<c.curlBackAt)return;
                for(var p:here){var at=from(n,p.position());if(CalderHollow.tendrilRow(at.x,at.z)>=0&&at.y>-4&&at.y<CalderHollow.GATE_HIGH+2)return;}
                for(int i=0;i<rows;i++)CalderHollow.tendrilRow(l,n.base,i,true);
                var mid=n.base.offset((CalderHollow.TIGHT_X0+CalderHollow.TIGHT_X1+1)/2,0,CalderHollow.TIGHT_SOUTH-rows/2);
                l.sendParticles(ParticleTypes.WHITE_SMOKE,mid.getX(),mid.getY()+.5,mid.getZ()+.5,80,2.5,.6,rows/2.0,.01);
                c.curlGone=0;c.curlBackAt=-1;return;
            }
            for(var p:here){
                if(!p.isAlive()||p.isSpectator())continue;
                Entity body=p.getVehicle() instanceof NightBike b?b:p;var at=from(n,body.position());
                if(Math.abs(at.y)<.1&&CalderHollow.tendrilRow(at.x,at.z)>=0&&(body==p||body.onGround())){c.curlAt=n.tick;break;}
            }
            return;
        }
        int gone=Mth.clamp((int)Math.floor((n.tick-c.curlAt-CURL_GRACE)*CURL_PACE),0,rows);
        while(c.curlGone<gone){
            int row=c.curlGone++;CalderHollow.tendrilRow(l,n.base,row,false);
            var at=n.base.offset((CalderHollow.TIGHT_X0+CalderHollow.TIGHT_X1+1)/2,0,CalderHollow.TIGHT_SOUTH-row);
            l.sendParticles(new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK,BikeNightRegistry.material("vine").defaultBlockState()),at.getX(),at.getY()+.5,at.getZ()+.5,24,2.5,1,.3,.02);
            if(row%4==0)l.playSound(null,at.getX(),at.getY(),at.getZ()+.5,BikeNightRegistry.CURL.get(),SoundSource.BLOCKS,.9F,.9F+l.random.nextFloat()*.2F);
        }
        if(c.curlGone>=rows){c.curlAt=-1;c.curlBackAt=n.tick+CURL_BACK;}
    }
    /** How many of the tendril's rows are gone in this park, and whether it is curling up. For tests. */
    public static int curlGone(BlockPos b){var n=BikeNight.night(b);return n==null?0:n.hollow.curlGone;}
    public static boolean curlGoing(BlockPos b){var n=BikeNight.night(b);return n!=null&&n.hollow.curlAt>=0;}
    /**
     * 0.4.507, Gus. As answered: "Gus rides the plain way through the Hollow with no specter of his own", "so he is at the back
     * gate whenever the reader is." With a friend who is past the hill's edge, he comes through the stones to the gate's line at
     * the edge, and from there he is on the Hollow's line (CalderHollow.TRAIL): as far along it as his friend is north, less the
     * distance he keeps from them anywhere; at the pace he rides at anywhere; never back along it, and never stopping in the air.
     * He does not ride it as a reader does, and cannot miss. Nothing in the Hollow is his: the specters are each some reader's,
     * and what jumps out comes out for readers. Past its end he rides after his friend again as he does on any ground. True if
     * this has said where he rides this tick.
     */
    static boolean gus(ServerLevel l,BikeNight.Night n,BikeNightGus.Friend g,PackBike bike,ServerPlayer friend,double d){
        var there=from(n,friend.position());var trail=CalderHollow.TRAIL;int last=trail.size()-1;
        boolean out=there.z<CalderHollow.EDGE+.5&&there.x>CalderGraves.W-1&&there.x<CalderGraves.E+2;
        if(g.trail<0){
            if(!out)return false;
            // To where the line begins: by the gate and through the stones, as after a friend who stood at the hill's edge on the
            // gate's line. He stops as far short of them as he keeps from anybody, which is where it begins.
            var start=BikeNightGus.at(n,trail.get(0).at());var brink=start.add(0,0,-BikeNightGus.KEEP);
            if(BikeNightGus.flat(bike.position(),start)<2*2){g.trail=0;g.way.plan=null;}
            else return BikeNightGraveyard.friend(l,n,g.way,bike,brink,Math.sqrt(BikeNightGus.flat(bike.position(),brink)));
        }
        if(g.trail>=last){
            // Off its far end: after them on the far side's ground and through the gate; waiting, if they are back out on the course.
            if(there.z>CalderHollow.FAR_SIDE+1||d<BikeNightGus.KEEP)bike.toward(null,0);
            else bike.toward(friend.position(),(float)Math.min(BikeNightGus.RIDE_PACE,Math.max(.12,(d-BikeNightGus.KEEP+.6)*.16)));
            return true;
        }
        double to=there.z+BikeNightGus.KEEP,left=BikeNightGus.RIDE_PACE;boolean moved=false;
        while(left>0&&g.trail<last){
            int i=(int)Math.floor(g.trail);var a=trail.get(i);var c=trail.get(i+1);
            // On as far as his friend is north, and over a gap only if where he comes down is no further than that; from the air, on
            // to the ground.
            if(!a.air()){int land=i+1;while(land<last&&trail.get(land).air())land++;if(trail.get(land).at().z<to)break;}
            double seg=a.at().distanceTo(c.at()),rest=(1-(g.trail-i))*seg;
            if(rest<=left){g.trail=i+1;left-=rest;}else{g.trail+=left/Math.max(seg,1e-6);left=0;}
            moved=true;
        }
        int i=Math.min(last-1,(int)Math.floor(g.trail));var a=trail.get(i).at();var c=trail.get(i+1).at();var at=a.lerp(c,Mth.clamp(g.trail-i,0,1));
        float yaw=(float)Math.toDegrees(Math.atan2(-(c.x-a.x),c.z-a.z));
        bike.carry(BikeNightGus.at(n,at),yaw,moved?BikeNightGus.RIDE_PACE:0);
        return true;
    }
    /** How far along the Hollow's line Gus is in this park, or -1. For tests. */
    public static double gusTrail(BlockPos b){var n=BikeNight.night(b);return n==null?-1:n.gus.trail;}
    /** The ways a reader has had under them tonight other than the plain one. For tests. */
    public static java.util.Set<String> ways(ServerPlayer p){var n=BikeNight.night(p);var ways=n==null?null:n.hollow.ways.get(p.getUUID());return ways==null?java.util.Set.of():java.util.Set.copyOf(ways);}
    /** Whether the bank is gone in this park, and whether somebody has been on it and it has yet to go. For tests. */
    public static boolean bankGone(BlockPos b){var n=BikeNight.night(b);return n!=null&&n.hollow.bankGone;}
    public static boolean bankGoing(BlockPos b){var n=BikeNight.night(b);return n!=null&&n.hollow.bankAt>=0;}
    /** How many of the run's rows are gone in this park, and whether it is giving way. For tests. */
    public static int runGone(BlockPos b){var n=BikeNight.night(b);return n==null?0:n.hollow.runGone;}
    public static boolean runGoing(BlockPos b){var n=BikeNight.night(b);return n!=null&&n.hollow.runAt>=0;}
    /** Whether a cell's ground is one of the run's rows, in whichever park's Hollow the cell is. */
    private static boolean onRun(ServerLevel l,BlockPos at){
        var n=BikeNight.nightAt(l,at);if(n==null)return false;var r=at.subtract(n.base);
        return r.getY()==0&&CalderHollow.runRow(r.getX()+.5,r.getZ()+.5)>=0||CalderHollow.bankRow(r.getX()+.5,r.getZ()+.5)>=0||CalderHollow.tendrilRow(r.getX()+.5,r.getZ()+.5)>=0;
    }
    static void reset(ServerLevel l,BikeNight.Night n){
        if(n.hollow.runGone>0&&l.isLoaded(n.base.offset(CalderHollow.RUN_X0,0,CalderHollow.RUN_SOUTH)))for(int i=0;i<CalderHollow.RUN_ROWS;i++)CalderHollow.runRow(l,n.base,i,true);
        if(n.hollow.gate&&l.isLoaded(n.base.offset(0,0,CalderHollow.BACK)))CalderHollow.backGate(l,n.base,false);
        if(n.hollow.bankGone&&l.isLoaded(n.base.offset(CalderHollow.BANK_X0,0,CalderHollow.BANK_SOUTH)))CalderHollow.bank(l,n.base,true);
        if(n.hollow.curlGone>0&&l.isLoaded(n.base.offset(CalderHollow.TIGHT_X0,0,CalderHollow.TIGHT_SOUTH)))for(int i=0;i<CalderHollow.TIGHT_ROWS;i++)CalderHollow.tendrilRow(l,n.base,i,true);
        n.hollow=new Course();
    }
}
