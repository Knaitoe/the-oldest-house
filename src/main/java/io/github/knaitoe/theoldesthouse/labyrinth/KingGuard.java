package io.github.knaitoe.theoldesthouse.labyrinth;

import javax.annotation.Nullable;
import io.github.knaitoe.theoldesthouse.house.HouseSitting;
import io.github.knaitoe.theoldesthouse.house.SeatEntity;
import io.github.knaitoe.theoldesthouse.network.HouseFadePayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.644, A sort of thing, part 4 (docs/SLEEPING_KING_0_4_644.md): the railway on the third square. A reader who sits down in the
 * carriage is looked at by the Guard through one glass after another, three in all; while he looks they are a thing, and a thing with
 * no ticket keeps its seat. When he lowers a glass they may get up, and the ride is off. If they sit through all three, the train
 * whistles and takes the brook at a jump, and puts them down on the road at the near edge of the fourth square.
 */
public final class KingGuard {
    private KingGuard(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.SLEEPING_KING;
    public static final int PHASE=221;
    /** Standing by the door (0), his glass up and looking (1), the glass lowered (2). */
    public static final int STANDING=0,LOOKING=1,LOWERED=2;
    /** The ride: the ticket line, then three looks with a pause between, then the whistle and the jump. */
    public static final int TICKET=20,LOOK=60,PAUSE=20,WHISTLE=20,RIDE=TICKET+3*LOOK+2*PAUSE+WHISTLE;
    static final String RODE="KingRode";

    public static boolean is(LiteraryActor a){return a.role()==LiteraryActor.STRANGER&&a.phase()==PHASE;}
    public static @Nullable LiteraryActor find(ServerLevel l,BlockPos b){
        for(var a:l.getEntitiesOfClass(LiteraryActor.class,new AABB(b.offset(KingSite.GUARD)).inflate(12,6,12),KingGuard::is))if(a.isAlive())return a;
        return null;
    }
    static void ensure(ServerLevel l,BlockPos b){
        var at=b.offset(KingSite.GUARD);
        if(!l.isLoaded(at)||!l.areEntitiesLoaded(ChunkPos.asLong(at))||find(l,b)!=null)return;
        var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)return;
        a.bind(PLACE,null);a.appearance(LiteraryActor.STRANGER,PHASE);
        a.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5,90,0);a.setYBodyRot(90);a.setYHeadRot(90);
        l.addFreshEntity(a);
    }
    static void reset(KingSleep.Sleep s){s.rider=null;s.rideAt=0;}

    /** Where in the ride the rider is (ticks since they sat down), or -1. */
    static int into(ServerLevel l,KingSleep.Sleep s){return s.rider==null?-1:(int)(l.getGameTime()-s.rideAt);}
    /** Whether a look is up at this point in the ride. */
    static boolean lookUp(int t){if(t<TICKET)return false;int k=t-TICKET;for(int i=0;i<3;i++){int at=i*(LOOK+PAUSE);if(k>=at&&k<at+LOOK)return true;}return false;}
    /** Whether he has this reader under his glass now. */
    public static boolean holding(ServerPlayer p,BlockPos b){var s=KingSleep.get(b);if(!p.getUUID().equals(s.rider))return false;return lookUp(into(p.serverLevel(),s));}
    public static @Nullable java.util.UUID rider(BlockPos b){return KingSleep.get(b).rider;}

    // ------------------------------------------------------------------------------------------------ while anybody is there
    static void tick(ServerLevel l,BlockPos b,KingSleep.Sleep s){
        ensure(l,b);var a=find(l,b);if(a==null)return;
        if(s.rider==null){
            a.gesture(STANDING);
            // Somebody has sat down who has not ridden yet: the ride begins.
            for(var p:KingSleep.readers(l,b)){
                if(!(p.getVehicle() instanceof SeatEntity seat)||!KingBlock.seat(l.getBlockState(seat.support()))||rode(p,s))continue;
                s.rider=p.getUUID();s.rideAt=l.getGameTime();s.riderSeat=seat.support();
                say(a,KingTexts.GUARD_TICKET);
                var at=Vec3.atCenterOf(b.offset(KingSite.GUARD));l.playSound(null,at.x,at.y,at.z,KingRegistry.TRAIN.get(),SoundSource.AMBIENT,1F,1F);
                break;
            }
            var near=KingSleep.nearest(l,b,a.position());if(near!=null&&near.distanceTo(a)<10)KingSleep.face(a,near.getEyePosition());
            return;
        }
        var p=l.getServer().getPlayerList().getPlayer(s.rider);int t=into(l,s);
        if(p==null||!KingSleep.readers(l,b).contains(p)){reset(s);return;}
        boolean up=lookUp(t);
        a.gesture(up?LOOKING:LOWERED);KingSleep.face(a,p.getEyePosition());
        if(t==TICKET+LOOK+PAUSE)say(a,KingTexts.GUARD_SECOND);
        if(t==TICKET+2*(LOOK+PAUSE))say(a,KingTexts.GUARD_THIRD);
        boolean seated=p.getVehicle() instanceof SeatEntity;
        // Under his glass a thing with no ticket keeps its seat; between looks, getting up ends the ride.
        if(!seated){
            if(up&&s.riderSeat!=null)HouseSitting.sit(p,s.riderSeat);
            else{reset(s);King.cue(p,KingTexts.GOT_UP);return;}
        }
        if(t==RIDE-WHISTLE){var at=Vec3.atCenterOf(b.offset(KingSite.GUARD));l.playSound(null,at.x,at.y,at.z,KingRegistry.WHISTLE.get(),SoundSource.AMBIENT,1.3F,1F);}
        if(t>=RIDE){
            // The train takes the brook at a jump, and they are on the road on the far side of it.
            s.ridden.add(p.getUUID());reset(s);
            p.stopRiding();HousePackets.send(p,new HouseFadePayload(4,20,16));
            var out=Vec3.atLowerCornerOf(b).add(KingSite.RIDE_OUT);p.fallDistance=0;p.setDeltaMovement(Vec3.ZERO);
            p.teleportTo(l,out.x,out.y,out.z,180,0);King.cue(p,KingTexts.JUMPED);
        }
    }
    static boolean rode(ServerPlayer p,KingSleep.Sleep s){
        if(s.ridden.contains(p.getUUID()))return true;
        var d=LabyrinthData.get(p.server);return LiteraryVignettes.personal(d,p.getUUID(),PLACE).getBoolean(RODE);
    }
    static void say(LiteraryActor a,String line){a.say(KingTexts.GUARD_NAME,line);a.speaks(line);}
}
