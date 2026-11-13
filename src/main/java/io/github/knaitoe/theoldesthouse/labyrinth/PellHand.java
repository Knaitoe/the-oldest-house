package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.535, June's hand (docs/PELL_HOUSE_HAND_0_4_535.md), from the proposal's "2. A week in": "June's hand. Off screen there is a door,
 * a sound and a cry. June comes out with her hand wrapped. By the time Gail calls, it was the older sibling who slammed the door on it."
 *
 * <ul>
 * <li><b>The laundry.</b> A week in, in the late afternoon a while after her hello, June leaves her stove for the laundry ("I'll just put
 * a wash on, love."). Once nobody is in the laundry or in either of its doorways she shuts both its doors; anyone in there she asks out.
 * The hour holds while she is gone.</li>
 * <li><b>Off screen.</b> A moment after she is shut in, with nobody inside and nobody able to see her: a door slams, hard, and she
 * cries out. Whoever is near enough hears it. Nobody sees it.</li>
 * <li><b>Her hand.</b> She comes out with her right hand wrapped in a tea towel ("It's nothing. I caught it in the door."), goes back to
 * her stove, and keeps it wrapped for the rest of the visit. The first hands on her get a word about it.</li>
 * <li><b>June's version.</b> Whoever was nearest the laundry when she cried out is the one she will say did it. They go on Gail's file
 * for it, once ("MarkHand", "GailMarks"), whatever they tell her. At the call (PellCall) Gail hears it from June first.</li>
 * <li><b>Kept.</b> If she cannot have the laundry to herself within a minute, the wash keeps, and nothing happens this visit.</li>
 * </ul>
 */
public final class PellHand {
    private PellHand(){}
    /** When she goes: a while after her hello and before six. */
    public static final long AT=11400;
    /** Where it has got to (Household.hand): not yet; on her way; at the laundry, waiting to have it to herself; shut in; after the
     *  cry; on her way back; done; and kept, with nothing this visit. */
    public static final int NOT_YET=0,TO_LAUNDRY=1,SHUTTING=2,INSIDE=3,CRIED=4,BACK=5,DONE=6,KEPT=7;
    /** How long she is shut in before it happens; how long after the cry she comes out; how often she asks a reader out; how long she
     *  waits to have the laundry to herself before the wash keeps. */
    public static final int BEAT=60,AFTER=50,ASK=100,KEEPS=1200;
    /** How far the slam is heard. */
    public static final double HEARD=24;
    /** Where she stands inside, a step through the door, facing the washing machine. */
    public static final PellPeople.Spot INSIDE_SPOT=new PellPeople.Spot(7.5,-68.6,180);
    /** Her way from her stove through the arch and along the hall to the laundry, and back. */
    static final Vec3[] TO_LAUNDRY_WAY={PellMorning.v(-9.5,-68.4),PellMorning.v(-9.5,-66.6),PellMorning.v(-9.5,-65.6),PellMorning.v(7.5,-65.6),PellMorning.v(7.5,-66.6),PellMorning.v(INSIDE_SPOT.x(),INSIDE_SPOT.z())},
        BACK_WAY={PellMorning.v(7.5,-66.6),PellMorning.v(7.5,-65.6),PellMorning.v(-9.5,-65.6),PellMorning.v(-9.5,-66.6),PellMorning.v(-9.5,-68.4),PellMorning.v(PellVisit.KITCHEN.x(),PellVisit.KITCHEN.z())};
    /** The laundry's two doors: from the hall, and out to the yard. */
    static final List<BlockPos> DOORS=List.of(PellSite.LAUNDRY_DOOR,PellSite.BACK_DOOR);
    private static final int J=PellPeople.JUNE;

    /** Whether a place (from the house's base) is in the laundry. */
    public static boolean inLaundry(Vec3 r){return r.x>=6&&r.x<10&&r.z>=-71&&r.z<-67&&r.y>-.5&&r.y<3;}
    /** For tests: where it has got to. */
    public static int stage(BlockPos b){return PellPeople.household(b).hand;}
    /** For tests: whom June will say did it. */
    public static @Nullable UUID blamed(BlockPos b){return PellPeople.household(b).blamed;}
    /** Whether her hand is hurt, this visit. */
    public static boolean hurt(BlockPos b){var h=PellPeople.household(b);return h.visit>=2&&h.blamed!=null;}
    /** Whether June is away at the laundry, and the hour holds. */
    static boolean holds(PellPeople.Household h){return h.visit>=2&&h.hand>=TO_LAUNDRY&&h.hand<DONE;}

    /** For tests: an afternoon a week in on which she never goes to the laundry. */
    public static void skip(BlockPos b){PellPeople.household(b).hand=KEPT;}
    /** For tests: the afternoon put on to just before she goes. */
    public static void beforeHand(BlockPos b){var h=PellPeople.household(b);if(h.time<AT-5)h.time=AT-5;}
    /**
     * For tests: the visit as the laundry leaves it, this reader the one June says did it (away from the house when it happened, or
     * not): on Gail's file for it and keeping that they heard it; June's hand wrapped.
     */
    public static void blamed(ServerLevel l,BlockPos b,ServerPlayer p,boolean away){
        var h=PellPeople.household(b);h.hand=DONE;h.blamed=p.getUUID();
        var own=PellHouse.own(p);own.putBoolean("BlamedForHand",true);own.putBoolean("HeardJuneHand",true);
        if(away)own.putBoolean("AwayForHand",true);
        mark(own);PellHouse.save(p,own);
        var june=PellPeople.find(l,b,J);if(june!=null)june.marks(june.marks()|PellPeople.WRAPPED);
    }
    /** The laundry undone, for the visit beginning again: her hand as it was, and nobody to blame. */
    static void restart(ServerLevel l,BlockPos b,PellPeople.Household h){
        h.hand=NOT_YET;h.handAt=0;h.shutAt=0;h.blamed=null;h.handSaid=0;h.toldHand.clear();
        var june=PellPeople.find(l,b,J);if(june!=null&&(june.marks()&PellPeople.WRAPPED)!=0)june.marks(june.marks()&~PellPeople.WRAPPED);
    }

    /**
     * Every tick of the settled afternoon a week in, once June has called hello (PellVisit asks first). True while she is away at the
     * laundry: dinner waits for her.
     */
    static boolean tick(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,List<ServerPlayer> here,long now){
        if(h.visit<2||h.hand>=DONE)return false;
        boolean still=h.way[J].isEmpty();
        switch(h.hand){
            case NOT_YET->{
                if(h.time<AT||!still||!PellVisit.ready(h,now,june))return false;
                PellVisit.say(h,june,J,PellTexts.JUNE_TO_LAUNDRY,now);
                PellVisit.go(h,J,-1,INSIDE_SPOT.look(),TO_LAUNDRY_WAY);
                h.hand=TO_LAUNDRY;h.nudgedAt=0;
            }
            case TO_LAUNDRY->{if(still){h.hand=SHUTTING;h.handAt=now;}}
            case SHUTTING->shutting(l,b,h,june,here,now);
            case INSIDE->inside(l,b,h,june,here,now);
            case CRIED->{if(now-h.shutAt>=AFTER)out(l,b,h,june,here,now);}
            case BACK->{if(still){h.hand=DONE;h.rest[J]=PellVisit.KITCHEN.look();}}
            default->{}
        }
        return h.hand>=TO_LAUNDRY&&h.hand<DONE;
    }
    /** At the laundry: anyone in it asked out, and both its doors shut once it is hers. If it never is, the wash keeps. */
    private static void shutting(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,List<ServerPlayer> here,long now){
        h.rest[J]=INSIDE_SPOT.look();
        boolean someone=someoneIn(b,here),doorways=true;
        for(var door:DOORS)if(!PellVisit.clearOf(b,door,here))doorways=false;
        if(someone||!doorways){
            if(now-h.handAt>=KEEPS){keep(h,june,now);return;}
            if(someone&&now-h.nudgedAt>=ASK&&PellVisit.ready(h,now,june)){PellVisit.say(h,june,J,PellTexts.JUNE_LAUNDRY_OUT,now);h.nudgedAt=now;}
            return;
        }
        for(var door:DOORS)if(PellSite.open(l,b,door))PellHouse.toggle(l,b,door);
        h.hand=INSIDE;h.shutAt=now;
    }
    /**
     * Shut in. Should a reader open a door or come in, she sees to it again. While anybody could see her, it waits (and keeps if that goes
     * on); otherwise, a moment later, it happens.
     */
    private static void inside(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,List<ServerPlayer> here,long now){
        boolean open=false;for(var door:DOORS)if(PellSite.open(l,b,door))open=true;
        if(open||someoneIn(b,here)){h.hand=SHUTTING;return;}
        if(PellNight.seen(l,june.getEyePosition())){
            if(now-h.handAt>=KEEPS){keep(h,june,now);return;}
            h.shutAt=now;return;
        }
        if(now-h.shutAt>=BEAT)cry(l,b,h,june,here,now);
    }
    /**
     * Off screen: the door and the cry, heard by whoever is near enough. Her hand is wrapped from now on. Whoever was nearest the laundry's
     * hall door is the one she will say did it, and goes on Gail's file for it.
     */
    private static void cry(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,List<ServerPlayer> here,long now){
        var at=PellPeople.world(b,INSIDE_SPOT);var door=Vec3.atBottomCenterOf(b.offset(PellSite.LAUNDRY_DOOR));
        l.playSound(null,at.x,at.y+1,at.z,PellRegistry.HAND.get(),SoundSource.NEUTRAL,1F,1F);
        ServerPlayer nearest=null;double best=Double.MAX_VALUE;
        for(var p:here){
            if(!p.isAlive()||p.isSpectator())continue;
            double d=p.position().distanceToSqr(door);if(d<best){best=d;nearest=p;}
            if(p.position().distanceToSqr(at)>HEARD*HEARD)continue;
            PellHouse.cue(p,PellSite.inHouse(rel(b,p.position()))?PellTexts.SLAM_HEARD:PellTexts.SLAM_FAR);
            var own=PellHouse.own(p);if(!own.getBoolean("HeardJuneHand")){own.putBoolean("HeardJuneHand",true);PellHouse.save(p,own);}
        }
        if(nearest!=null){
            h.blamed=nearest.getUUID();
            var own=PellHouse.own(nearest);own.putBoolean("BlamedForHand",true);
            if(!PellSite.inHouse(rel(b,nearest.position())))own.putBoolean("AwayForHand",true);
            mark(own);PellHouse.save(nearest,own);
        }
        june.marks(june.marks()|PellPeople.WRAPPED);
        h.hand=CRIED;h.shutAt=now;
    }
    /** Out, her hand wrapped: a word to whoever is by, and back to her stove. */
    private static void out(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,List<ServerPlayer> here,long now){
        if(!PellSite.open(l,b,PellSite.LAUNDRY_DOOR))PellHouse.toggle(l,b,PellSite.LAUNDRY_DOOR);
        ServerPlayer near=null;double best=PellVisit.HEAR*PellVisit.HEAR;
        for(var p:here){if(!p.isAlive()||p.isSpectator())continue;double d=p.distanceToSqr(june);if(d<best){best=d;near=p;}}
        if(near!=null)PellVisit.say(h,june,J,PellTexts.JUNE_HAND_OUT,now);
        for(var p:here){
            if(!p.isAlive()||p.isSpectator()||!PellNight.sees(p,june.getEyePosition(),PellVisit.HEAR))continue;
            PellHouse.cue(p,PellTexts.HAND_WRAPPED);
            var own=PellHouse.own(p);if(!own.getBoolean("SawJuneHand")){own.putBoolean("SawJuneHand",true);PellHouse.save(p,own);}
        }
        PellVisit.go(h,J,-1,PellVisit.KITCHEN.look(),BACK_WAY);
        h.hand=BACK;
    }
    /** The wash keeps: she goes back to her stove, and nothing happens this visit. */
    private static void keep(PellPeople.Household h,LiteraryActor june,long now){
        if(PellVisit.ready(h,now,june))PellVisit.say(h,june,J,PellTexts.JUNE_LAUNDRY_KEEPS,now);
        PellVisit.go(h,J,-1,PellVisit.KITCHEN.look(),BACK_WAY);h.hand=KEPT;
    }
    /** On Gail's file for the hand, once. */
    private static void mark(CompoundTag own){
        if(own.getBoolean("MarkHand"))return;
        own.putBoolean("MarkHand",true);own.putInt("GailMarks",own.getInt("GailMarks")+1);
    }

    // ------------------------------------------------------------------------------------------------ helpers
    private static boolean someoneIn(BlockPos b,List<ServerPlayer> here){
        for(var p:here)if(p.isAlive()&&!p.isSpectator()&&inLaundry(rel(b,p.position())))return true;
        return false;
    }
    private static Vec3 rel(BlockPos b,Vec3 at){return at.subtract(b.getX(),b.getY(),b.getZ());}
}
