package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.HouseFadePayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;

/**
 * 0.4.643, A sort of thing, the third part (docs/SLEEPING_KING_0_4_643.md): how deep he sleeps, what stirs him, and both endings in
 * small.
 *
 * <p><b>How deep he sleeps</b> is one number for the country, shown only by how far the fill reaches (KingSite): deep, lightly, barely,
 * awake. Two stirs take him from each to the next, so six wake him. He does not settle by himself.
 *
 * <p><b>What stirs him</b>: a reader beside him who is a thing or more and runs, lands a jump, drops something, or is caught mid-step
 * by the in-breath; and the brothers' battle (KingBrothers), which is two. Less than a thing cannot stir him.
 *
 * <p><b>The endings in small.</b> A reader who has heard the brothers can say his whole name beside him: the breathing stops, the
 * country goes over to lines, and everybody still in it comes to in the drawing-room with the account on the armchair. Or a reader
 * who has been to him walks off the board until there is nothing of them left on it, and comes to there alone, leaving him his sleep.
 * If he wakes before either, from stirring, the country goes out with the readers in it: the story's one failure (VignetteGate).
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class KingSleep {
    private KingSleep(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.SLEEPING_KING;
    public static final int DEEP=0,LIGHTLY=1,BARELY=2,AWAKE=3;
    /** Six stirs wake him from a deep sleep. */
    public static final int WAKE_AT=6;
    /** A reader's own noises stir him at most once in this many ticks. */
    static final int STIR_GAP=40;
    /** After the breathing stops: the country goes to lines, then everybody comes to. */
    static final int LINES_AT=30,ENDS_AT=90;
    /** A country nobody has been in for this long is a fresh dream when somebody comes back. */
    static final int FORGOTTEN=200;
    public static final String MET="KingMet",HEARD="KingHeard",ENDING="KingEnding",STIRRED="KingStirred",COUNTED="KingCounted";

    static final class Sleep{
        int stirs;long seen=-1;long wakingAt=-1;boolean awake;boolean tooSoon;@Nullable UUID namer;int turn;
        // The brothers (KingBrothers).
        int battle;long battleAt;boolean rattleTaken;@Nullable UUID rattleBy;final java.util.Set<UUID> sawBattle=new java.util.HashSet<>();
        // 0.4.644: the Queen (KingQueen) and the Guard's ride (KingGuard); who has run with her and who has ridden, in this dream.
        int queen;@Nullable UUID queenWith;long queenAt;final java.util.Set<UUID> ran=new java.util.HashSet<>();
        @Nullable UUID rider;long rideAt;@Nullable BlockPos riderSeat;final java.util.Set<UUID> ridden=new java.util.HashSet<>();
    }
    /**
     * How the waking ended for each reader who was in the country, kept until that reader's own tick settles it. The waking ends inside
     * one reader's tick, which saves that reader's record whole afterward, so nothing is written to a record from here.
     */
    private static final Map<UUID,String> COMING_TO=new HashMap<>();
    private static final Map<Long,Sleep> SLEEPS=new HashMap<>();
    static Sleep get(BlockPos b){return SLEEPS.computeIfAbsent(b.asLong(),k->new Sleep());}

    /** How deep he sleeps now: two stirs to each depth, and awake once the breathing has stopped and the lines have come. */
    public static int level(BlockPos b){var s=get(b);return s.awake?AWAKE:Math.min(BARELY,s.stirs/2);}
    public static int stirs(BlockPos b){return get(b).stirs;}
    /** Whether his breathing has stopped: he is waking. */
    public static boolean waking(BlockPos b){return get(b).wakingAt>=0;}

    // ------------------------------------------------------------------------------------------------ once a tick, while anybody is there
    static void tick(ServerLevel l,BlockPos b){
        var s=get(b);long now=l.getGameTime();
        if(s.seen>=0&&now-s.seen>FORGOTTEN)reset(l,b);
        s=get(b);s.seen=now;
        // He is made once his tree's ground is loaded, which may be after the reader arrived in the drawing-room.
        var king=King.find(l,b);if(king==null){King.ensure(l,b);king=King.find(l,b);}
        if(king!=null){
            if(s.wakingAt<0)King.lie(king,b.offset(KingSite.KING),180+s.turn);
            else{king.setPose(Pose.STANDING);king.clearSleepingPos();var to=nearest(l,b,king.position());if(to!=null)face(king,to.position());}
        }
        KingBrothers.tick(l,b,s);
        KingQueen.tick(l,b,s);
        KingGuard.tick(l,b,s);
        if(s.wakingAt<0)return;
        long t=now-s.wakingAt;
        if(t>=LINES_AT&&!s.awake){s.awake=true;KingBreath.redraw(l,b);for(var p:readers(l,b))King.cue(p,s.tooSoon?KingTexts.LINES_TOO_SOON:KingTexts.LINES);}
        if(t>=ENDS_AT)finish(l,b,s);
    }

    // ------------------------------------------------------------------------------------------------ each reader, every quarter second
    static void reader(ServerPlayer p,BlockPos b,CompoundTag own,int state){
        if(settle(p,b,own))return;
        var s=get(b);
        if(s.sawBattle.contains(p.getUUID()))own.putBoolean("KingSawBattle",true);
        // 0.4.644: what this dream remembers of them goes into their own record from their own tick: running with the Queen, the ride,
        // and every square whose peg has counted them (stood on while somebody, of the dream or another reader, was looking at them).
        if(s.ran.contains(p.getUUID()))own.putBoolean(KingQueen.RAN,true);
        if(s.ridden.contains(p.getUUID()))own.putBoolean(KingGuard.RODE,true);
        for(int r=2;r<=KingSite.RANKS;r++)if(KingQueen.onPeg(p,b,r)&&(own.getInt(COUNTED)&1<<r)==0&&(KingBreath.lookedAtByTheDream(p,b)||KingBreath.noticed(p,b))){
            own.putInt(COUNTED,own.getInt(COUNTED)|1<<r);King.cue(p,KingTexts.COUNTED);}
        var r=p.position().subtract(b.getX(),b.getY(),b.getZ());boolean beside=KingBreath.beside(r);
        if(beside&&state>=KingBreath.THING&&!own.getBoolean(MET))own.putBoolean(MET,true);
        double moved=own.contains("KingLastX")?Math.hypot(r.x-own.getDouble("KingLastX"),r.z-own.getDouble("KingLastZ")):0;
        own.putDouble("KingLastX",r.x);own.putDouble("KingLastZ",r.z);
        boolean air=!p.onGround()&&!p.isInWater(),wasAir=own.getBoolean("KingAir");own.putBoolean("KingAir",air);
        if(s.wakingAt>=0||!beside||state<KingBreath.THING)return;
        var l=p.serverLevel();long now=l.getGameTime();
        if(now-own.getLong("KingStirAt")<STIR_GAP)return;
        String why=null;
        if(p.isSprinting()&&moved>.4)why="ran";
        else if(wasAir&&!air)why="landed";
        else if(KingBreath.in(b)&&KingBreath.phase(b)<6&&moved>.3&&own.getInt("KingCaught")!=KingBreath.cycle(b)+1){why="caught";own.putInt("KingCaught",KingBreath.cycle(b)+1);}
        if(why!=null){own.putLong("KingStirAt",now);stir(l,b,p,own,1);}
    }

    /** A thing beside him lets go of something: it lands, and he hears it. */
    @SubscribeEvent public static void dropped(ItemTossEvent e){
        if(!(e.getPlayer() instanceof ServerPlayer p)||LiteraryVignettes.current(p)!=PLACE)return;
        var b=LiteraryVignettes.base(p,PLACE);if(b==null||KingBreath.of(p)<KingBreath.THING)return;
        if(!KingBreath.beside(p.position().subtract(b.getX(),b.getY(),b.getZ())))return;
        var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),PLACE);
        stir(p.serverLevel(),b,p,own,1);LiteraryVignettes.save(d,p.getUUID(),PLACE,own);
    }

    /** He stirs: a grumble and a turn in his sleep; every second stir the country draws in to the next depth; the sixth wakes him. */
    public static void stir(ServerLevel l,BlockPos b,@Nullable ServerPlayer by,@Nullable CompoundTag own,int times){
        var s=get(b);if(s.wakingAt>=0||times<=0)return;
        int before=level(b);s.stirs+=times;s.turn=s.turn>0?-14:14;
        if(own!=null)own.putInt(STIRRED,own.getInt(STIRRED)+times);
        var at=Vec3.atCenterOf(b.offset(KingSite.KING));l.playSound(null,at.x,at.y,at.z,KingRegistry.STIR.get(),SoundSource.AMBIENT,1.4F,.9F+l.random.nextFloat()*.15F);
        if(s.stirs>=WAKE_AT){wake(l,b,null,true);return;}
        int after=level(b);
        if(after!=before)KingBreath.redraw(l,b);
        String line=after!=before?KingTexts.drewIn(after):KingTexts.STIRRED;
        for(var p:readers(l,b))King.cue(p,line);
    }

    /** The breathing stops in the middle of a breath: the first silence since the glass. */
    public static void wake(ServerLevel l,BlockPos b,@Nullable ServerPlayer namer,boolean tooSoon){
        var s=get(b);if(s.wakingAt>=0)return;
        s.wakingAt=l.getGameTime();s.tooSoon=tooSoon;s.namer=namer==null?null:namer.getUUID();
        for(var p:readers(l,b)){
            p.connection.send(new ClientboundStopSoundPacket(KingRegistry.BREATH_IN.getId(),SoundSource.AMBIENT));
            p.connection.send(new ClientboundStopSoundPacket(KingRegistry.BREATH_OUT.getId(),SoundSource.AMBIENT));
            King.cue(p,tooSoon?KingTexts.WOKE_TOO_SOON:KingTexts.BREATHING_STOPPED);
        }
    }

    /** The waking is over: everybody still in the country comes to, or, if he woke too soon, goes out with it. */
    static void finish(ServerLevel l,BlockPos b,Sleep s){
        for(var p:readers(l,b))COMING_TO.put(p.getUUID(),s.tooSoon?"too_soon":p.getUUID().equals(s.namer)?"said_his_whole_name":"there_when_he_woke");
        reset(l,b);
    }
    /** A reader's own part of the waking's end, from their own tick. True if it moved them. */
    static boolean settle(ServerPlayer p,BlockPos b,CompoundTag own){
        var how=COMING_TO.remove(p.getUUID());if(how==null)return false;
        if(own.getBoolean("Ready")){
            // Somebody who left him his sleep and is still in the drawing-room: he woke after they had gone.
            if("left_him_his_sleep".equals(own.getString(ENDING))&&!how.equals("too_soon"))own.putBoolean("KingWokeAfter",true);
            return false;
        }
        if(how.equals("too_soon")){
            own.putBoolean("KingWokeTooSoon",true);KingBreath.leave(p);
            VignetteGate.failed(p);King.cue(p,KingTexts.WENT_OUT);LabyrinthDoors.sendBack(p,20,40,30);
        }else{
            own.putString(ENDING,how);LiteraryVignettes.ready(p,PLACE,own,how);
            comeTo(p,b);King.cue(p,KingTexts.CAME_TO);
        }
        return true;
    }

    /**
     * Off the board, a line is nowhere. A reader who has been beside him and heard the brothers has left him his sleep: they come to
     * in the drawing-room, and he goes on breathing behind the glass. True if that was so; anybody else is put back (KingBreath).
     */
    static boolean walkedOff(ServerPlayer p,BlockPos b,CompoundTag own){
        if(!own.getBoolean(MET)||!own.getBoolean(HEARD)||own.getBoolean("Ready")||waking(b))return false;
        var d=LabyrinthData.get(p.server);own.putString(ENDING,"left_him_his_sleep");
        LiteraryVignettes.ready(p,PLACE,own,"left_him_his_sleep");LiteraryVignettes.save(d,p.getUUID(),PLACE,own);
        HousePackets.send(p,new HouseFadePayload(4,30,20));comeTo(p,b);King.cue(p,KingTexts.LEFT_HIM);
        return true;
    }

    /** The drawing-room, between the armchair and the fire, facing the glass. */
    static void comeTo(ServerPlayer p,BlockPos b){
        var at=Vec3.atLowerCornerOf(b).add(KingSite.COME_TO);
        p.fallDistance=0;p.setDeltaMovement(Vec3.ZERO);KingBreath.leave(p);
        p.teleportTo(p.serverLevel(),at.x,at.y,at.z,180,0);
    }

    /** A fresh dream: deep asleep, the rattle under his tree, the brothers in the wood. */
    static void reset(ServerLevel l,BlockPos b){
        SLEEPS.remove(b.asLong());get(b).seen=l.getGameTime();
        KingBreath.redraw(l,b);
        if(l.isLoaded(b.offset(KingSite.RATTLE))&&!KingBlock.is(l.getBlockState(b.offset(KingSite.RATTLE)),KingBlock.Kind.RATTLE))KingSite.rattle(l,b);
        var king=King.find(l,b);if(king!=null)King.lie(king,b.offset(KingSite.KING),180);
        KingBrothers.reset(l,b);
        var fresh=get(b);KingQueen.reset(l,b,fresh);KingGuard.reset(fresh);
    }
    /** For tests: forget the dream at this country. */
    public static void forget(BlockPos b){SLEEPS.remove(b.asLong());}

    /** Every reader standing in this country now. */
    static List<ServerPlayer> readers(ServerLevel l,BlockPos b){
        var out=new ArrayList<ServerPlayer>();
        for(var p:l.players())if(!p.isSpectator()&&p.isAlive()&&LiteraryVignettes.current(p)==PLACE&&b.equals(LiteraryVignettes.base(p,PLACE))&&KingSite.inside(p.position().subtract(b.getX(),b.getY(),b.getZ())))out.add(p);
        return out;
    }
    static @Nullable ServerPlayer nearest(ServerLevel l,BlockPos b,Vec3 from){
        ServerPlayer best=null;double d=Double.MAX_VALUE;
        for(var p:readers(l,b)){double e=p.distanceToSqr(from);if(e<d){d=e;best=p;}}
        return best;
    }
    static void face(LiteraryActor a,Vec3 to){
        float yaw=(float)(Math.toDegrees(Math.atan2(to.z-a.getZ(),to.x-a.getX()))-90);
        a.setYRot(yaw);a.setYBodyRot(yaw);a.setYHeadRot(yaw);
    }
}
