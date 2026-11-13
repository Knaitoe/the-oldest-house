package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.525, the Pell house (docs/PELL_HOUSE_VISIT_0_4_525.md): the first visit's day, from the proposal's "1. The placement":
 * "Gail drops them off. June shows them round, and Callum watches from the end of the hall", with June's rules "kindly given".
 *
 * <ul>
 * <li><b>The handover.</b> Gail's car is parked at the turning circle's curb. When a reader comes up to Gail (or past her, to June
 * or into the house), Gail turns to them, June says hello, and Gail hands the siblings over and says goodbye. She walks back to her
 * car and waits at its door until nobody can see her or it; then she is gone, and the car with her, and what is heard is a car door
 * and a car pulling away.</li>
 * <li><b>June's tour.</b> At the step June gives the first of her rules, the line. Then she opens the front door and shows them
 * round: Callum from the hall; Nessa's room, which is Tilly's now; the pool, through the glass; and across the yard, the garage, the
 * reader's. At each stop she waits until somebody is with her, and for what was last said to have been read, then says her piece
 * and opens what she has come to open. Then she tells them when dinner is, and goes back to her kitchen.</li>
 * <li><b>Tilly</b> follows June, a step behind along the way June went, until she is shown her room; then she goes in and stays by
 * the bed.</li>
 * <li><b>Callum</b> stays where he is, at the end of the hall, and watches.</li>
 * </ul>
 *
 * <p><b>The rules are each reader's own.</b> A reader hears a rule if they are within earshot when June gives it, on the tour or
 * from a hand on her later (PellTexts.rule). The record is the reader's own ("Rules").
 *
 * <p><b>0.4.526, the first evening</b> (docs/PELL_HOUSE_DINNER_0_4_526.md). The house keeps its own hours: the late afternoon holds
 * through the tour, then goes on to six; at six June calls them to dinner, lays four places, and serves Tilly first, then a place for
 * each reader; Callum's plate she passes over, and a place laid at a chair nobody takes stays empty. When the plates are done, or dinner
 * has gone on long enough, June clears; Tilly goes back to Nessa's room and Callum to the end of the hall, and the evening goes on to
 * half past eight, where it waits for what comes next.
 *
 * <p><b>0.4.527, bedtime</b> (docs/PELL_HOUSE_BEDTIME_0_4_527.md). After the washing up June sits in her recliner with a birthday tape
 * on the television. At half past eight she turns it off and calls Tilly to bed; on the edge of Nessa's bed she brushes Tilly's hair, and
 * Tilly lies down. Callum goes to his room and lies down in his circle. June sends whoever is still in the house out to the garage,
 * waits until nobody is, and locks the sliding door, the back door and the front door from inside; then she goes to her room and shuts
 * the door, and the hour goes on to ten, where the night waits for what comes next.
 *
 * <p><b>0.4.530, the first night</b> (PellNight, docs/PELL_HOUSE_NIGHT_0_4_530.md). From the moment the house is locked up, the night is
 * PellNight's: the hour goes on to eleven, June goes out with the tin and paints the line behind the shed, and Callum stands on it.
 *
 * <p><b>0.4.531, the first morning</b> (PellMorning, docs/PELL_HOUSE_MORNING_0_4_531.md). A reader can lie down on the camp bed; while
 * every reader at the house is asleep the night hurries on. At first light the line is dry and Callum is in; June gets up, unlocks,
 * lays breakfast, and calls them to it at seven.
 *
 * <p><b>It is the house's, for everyone at it.</b> One visit for every reader at the house; each step waits for whoever is nearest.
 * When the house has been empty for a while it begins again from the start: everyone back where the first visit puts them, Gail and
 * her car back at the curb, June's doors shut.
 */
public final class PellVisit {
    private PellVisit(){}
    /** How near a reader comes to Gail before she turns to them; how near June must be for coming past Gail to count; how near a stop June waits for somebody; how far her words carry. */
    public static final double GREET_NEAR=6,JUNE_NEAR=3,AT_STOP=5,HEAR=12;
    /** Ticks between one line ending and the next beginning; ticks June stands unable to get nearer before she is put where she was going, if nobody sees. */
    public static final int GAP=10,STUCK=80;
    /** How long the house is empty before its visit begins again. */
    public static final int EMPTY=100;
    static final double JUNE_PACE=.1,GAIL_PACE=.11,TILLY_PACE=.12,CALLUM_PACE=.1,BEHIND=1.6;

    /** Gail's car, parked at the turning circle's curb in front of June's, its nose west; and Gail's way to its door. */
    public static final Vec3 CAR=new Vec3(-2.5,0,-45);
    public static final float CAR_YAW=90;
    private static final Vec3[] GAIL_TO_CAR={v(.5,-50.5),v(.6,-46.5),v(.6,-43.3),v(-2.5,-43.3)};
    /** Where Tilly ends up, by the foot of Nessa's bed, and her way in from the hall. */
    public static final PellPeople.Spot TILLY_ROOM=new PellPeople.Spot(-10.5,-61.5,90);
    static final Vec3[] TILLY_IN={v(-9.5,-64.6),v(-9.5,-62.8),v(-10.5,-61.5)};
    /** Where June ends up: her kitchen, at the stove. */
    public static final PellPeople.Spot KITCHEN=new PellPeople.Spot(-8.5,-69.5,180);

    /**
     * One of June's stops: the way there (each point after the last), which way she faces when there, what she opens there, what she
     * says, the rule it gives, and whether she waits for somebody to be with her first.
     */
    public record Stop(Vec3[] route,float look,@Nullable BlockPos opens,@Nullable String line,int rule,boolean waits){}
    private static Vec3 v(double x,double z){return new Vec3(x,0,z);}
    public static final Stop[] STOPS={
        // The hall, from the step: Callum at its far end.
        new Stop(new Vec3[]{v(.5,-56.6),v(.5,-61),v(.5,-65.5)},90,null,PellTexts.JUNE_CALLUM,0,true),
        // Nessa's door, along the hall, opened.
        new Stop(new Vec3[]{v(-8.5,-65.5)},27,PellSite.NESSA_DOOR,PellTexts.JUNE_NESSA,PellTexts.RULE_NESSA,true),
        // The living room, at the sliding door, looking out at the pool.
        new Stop(new Vec3[]{v(0,-65.5),v(0,-68.5),v(.5,-70.5)},180,null,PellTexts.JUNE_POOL,PellTexts.RULE_POOL,true),
        // Out through the sliding door and across the yard to the garage, opened.
        new Stop(new Vec3[]{v(.5,-73.5),v(6.5,-79.5),v(13.5,-84.5)},180,PellSite.GARAGE_DOOR,PellTexts.JUNE_GARAGE,PellTexts.RULE_GARAGE,true),
        // Where she leaves them.
        new Stop(new Vec3[0],180,null,PellTexts.JUNE_DINNER,0,false),
        // Back to her kitchen.
        new Stop(new Vec3[]{v(6.5,-79.5),v(.5,-73.5),v(.5,-70.5),v(0,-68.5),v(0,-65.5),v(-9,-65.5),v(-9,-68.5),v(KITCHEN.x(),KITCHEN.z())},KITCHEN.look(),null,null,0,false)};
    /** The doors June opens when she comes to them on her way: the front door and the sliding door. */
    private static final BlockPos[] ON_THE_WAY={PellSite.FRONT_DOOR,PellSite.SLIDER};
    /** The doors she has opened by the end of the day, all shut again when the visit begins again. */
    public static final List<BlockPos> HER_DOORS=List.of(PellSite.FRONT_DOOR,PellSite.NESSA_DOOR,PellSite.SLIDER,PellSite.GARAGE_DOOR);

    /**
     * Where the visit is: Gail's greeting, June's hello, Gail's handover, Gail's goodbye, June's first rule at the step; then each of
     * June's stops in turn from TOUR; then the day is settled.
     */
    public static final int GREET=0,JUNE_HELLO=1,GAIL_HANDOVER=2,GAIL_GOODBYE=3,FIRST_RULE=4,TOUR=5,SETTLED=TOUR+STOPS.length;

    /** Where the visit is at this house, for tests and for what comes after it. */
    public static int step(BlockPos b){return PellPeople.household(b).step;}
    /** Whether Gail has gone. */
    public static boolean gailGone(BlockPos b){return PellPeople.household(b).gailGone;}
    /** Gail's car at this house, while it is there. */
    public static @Nullable TownCar car(ServerLevel l,BlockPos b){var id=PellPeople.household(b).car;return id!=null&&l.getEntity(id) instanceof TownCar c&&c.isAlive()?c:null;}
    static Vec3 world(BlockPos b,Vec3 r){return new Vec3(b.getX()+r.x,b.getY()+r.y,b.getZ()+r.z);}

    /** Once a game tick for the house, with readers at it. Says who walked this tick (Household.moving), so their heads are left alone. */
    static void tick(ServerLevel l,BlockPos b,PellPeople.Household h,List<ServerPlayer> here){
        Arrays.fill(h.moving,false);
        var june=PellPeople.find(l,b,PellPeople.JUNE);var tilly=PellPeople.find(l,b,PellPeople.TILLY);
        var gail=h.gailGone&&!h.fresh?null:PellPeople.find(l,b,PellPeople.GAIL);
        if(h.fresh){
            // 0.4.533: which visit, from the readers here (PellWeek); a week in, Gail does not come.
            int visit=PellWeek.visitFor(here);
            // 0.4.544: and not until the whole lot is loaded, so nobody is moved onto ground that is not there yet (PellPeople.hold).
            if(june==null||tilly==null||visit<2&&gail==null||PellPeople.find(l,b,PellPeople.CALLUM)==null||!PellPeople.ground(l,b))return;
            restart(l,b,h,visit);
            if(h.gailGone)gail=null;
        }
        if(june==null)return;
        long now=l.getGameTime();
        // 0.4.531: who is asleep, and whether everybody is (the night hurries on).
        PellMorning.sleepers(l,b,h,here,now);
        clock(h,here,now);
        if(!h.gailGone)car(l,b,h);
        // 0.4.541: back at first light on the last night, Gail stays (PellDawn).
        if(gail!=null&&h.step>=FIRST_RULE&&!PellDawn.morning(h))leave(l,b,h,gail);
        // 0.4.545: a reader as Tilly walks themselves.
        if(tilly!=null&&h.step<CALLED&&h.tillyReader==null)tilly(b,h,june,tilly);
        // 0.4.545: the older sibling's part of the house is for the readers who are not Tilly (PellTilly.siblings).
        var sib=PellTilly.siblings(h,here);
        walkers(l,b,h);
        // 0.4.536: the shed's deck, whatever the hour.
        PellTapes.tick(l,b,h,here,now);
        // 0.4.538: a bucket nobody here has goes back to the laundry; on the last night, Callum out on the line.
        if(now%PellScrub.KEEP==0)PellScrub.keep(l,b,here);
        // 0.4.540: the lanyard looked for and Nessa where she lies; while the freezer is open, who sees into it.
        if(now%PellFreezer.KEEP==0)PellFreezer.keep(l,b,here);
        if(now%10==0)PellFreezer.seen(l,b,here);
        if(h.visit>=PellWeek.LAST)PellLastNight.callum(l,b,h,sib,now);
        switch(h.step){
            case GREET->{if(gail!=null&&!gail.saying()&&arrived(b,june,gail,here)){say(h,gail,PellPeople.GAIL,PellTexts.GAIL_GREETS,now);h.step=JUNE_HELLO;}}
            case JUNE_HELLO->{if(ready(h,now,june)){say(h,june,PellPeople.JUNE,PellTexts.JUNE_GREETS,now);h.step=GAIL_HANDOVER;}}
            case GAIL_HANDOVER->{if(gail==null)h.step=FIRST_RULE;else if(ready(h,now,gail)){say(h,gail,PellPeople.GAIL,PellTexts.GAIL_READY,now);h.step=GAIL_GOODBYE;}}
            case GAIL_GOODBYE->{if(gail==null)h.step=FIRST_RULE;else if(ready(h,now,gail)){say(h,gail,PellPeople.GAIL,PellTexts.GAIL_GOES,now);h.step=FIRST_RULE;}}
            case FIRST_RULE->{if(ready(h,now,june)){say(h,june,PellPeople.JUNE,PellTexts.JUNE_LINE,now);heard(june,PellTexts.RULE_LINE,here);h.step=TOUR;h.waypoint=0;}}
            default->{if(h.step>=TOUR&&h.step<SETTLED)stop(l,b,h,june,STOPS[h.step-TOUR],here,now);else if(h.step>=SETTLED)evening(l,b,h,june,sib,now);}
        }
    }

    /** Everyone back where the first visit puts them, June's doors shut, and the day from its start; 0.4.533, as the visit has it: a
     *  week in, without Gail and the house as June has made it by then (PellWeek). */
    static void restart(ServerLevel l,BlockPos b,PellPeople.Household h,int visit){
        for(int who=0;who<PellPeople.COUNT;who++){
            var a=PellPeople.find(l,b,who);if(a==null)continue;var s=PellPeople.FIRST_VISIT[who];
            a.moveTo(b.getX()+s.x(),b.getY(),b.getZ()+s.z(),s.look(),0);a.setYHeadRot(s.look());a.setYBodyRot(s.look());a.setDeltaMovement(Vec3.ZERO);
            h.rest[who]=s.look();
        }
        for(var door:HER_DOORS)if(PellSite.open(l,b,door))PellHouse.toggle(l,b,door);
        h.step=GREET;h.waypoint=0;h.gailStep=0;h.tillyStep=0;h.tillyHome=false;h.gailGone=false;h.next=0;h.stuck=0;h.trail.clear();h.fresh=false;
        // 0.4.526: the afternoon again, nobody sitting or walking, the table bare and June's hands empty.
        h.time=AFTERNOON;h.rate=0;h.told.clear();
        Arrays.fill(h.seated,false);Arrays.fill(h.place,-1);Arrays.fill(h.sitAt,-1);for(var w:h.way)w.clear();
        clearPlates(l,b);Arrays.fill(h.plate,NONE);Arrays.fill(h.plates,null);Arrays.fill(h.diner,NOBODY);h.order.clear();h.serving=0;
        var june=PellPeople.find(l,b,PellPeople.JUNE);if(june!=null)june.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        // 0.4.527: nobody asleep or perched, every door shut and none locked, the television off; Tilly has her cane again.
        Arrays.fill(h.perch,null);Arrays.fill(h.perchTo,null);Arrays.fill(h.sleepAt,null);Arrays.fill(h.asleep,false);h.locked.clear();
        h.chairSaid=false;h.brushSaid=0;h.lockIndex=0;h.watchSaid=0;h.tapeAt=0;h.brushedAt=0;h.nudgedAt=0;
        for(var door:ALL_DOORS)if(PellSite.open(l,b,door))PellHouse.toggle(l,b,door);
        tv(l,b,false);
        // 0.4.530: the night undone (PellNight.restart); 0.4.531, the morning too (PellMorning.restart); 0.4.532, and Gail's call.
        PellNight.restart(l,b,h);PellMorning.restart(l,b,h);PellCall.restart(l,b,h);PellSwim.restart(l,b,h);PellKnife.restart(l,b,h);PellHand.restart(l,b,h);PellTapes.restart(l,b,h);
        // 0.4.538: the bucket back in the laundry, and the last night undone.
        PellScrub.restart(l,b,h);PellLastNight.restart(l,b,h);
        // 0.4.539: and midnight in the pool undone.
        PellRite.restart(l,b,h);
        // 0.4.540: the freezer shut and padlocked again, and the lanyard back.
        PellFreezer.restart(l,b);
        // 0.4.541: and the morning.
        PellDawn.restart(h);
        // 0.4.545: the cane back on its hook unless a reader is Tilly here.
        PellTilly.restart(l,b,h);
        for(int who=0;who<PellPeople.COUNT;who++){var a=PellPeople.find(l,b,who);if(a!=null)dress(a,who);}
        h.visit=visit;h.welcomed=false;
        if(visit>=2)PellWeek.weekIn(l,b,h);
        PellWeek.dress(l,b,visit);
        // 0.4.538: the third visit is the last night, from the evening.
        if(visit>=PellWeek.LAST)PellLastNight.begin(l,b,h);
    }
    /** Each of them as they are by day: Tilly with her cane held out, Gail with her folder, June and Callum with nothing in hand. */
    static void dress(LiteraryActor a,int who){
        a.gesture(who==PellPeople.TILLY?PellPeople.CANE:who==PellPeople.GAIL?PellPeople.FOLDER:0);
        a.setItemInHand(InteractionHand.MAIN_HAND,who==PellPeople.TILLY?new ItemStack(PellRegistry.CANE.get()):who==PellPeople.GAIL?new ItemStack(PellRegistry.FOLDER.get()):ItemStack.EMPTY);
    }

    /** Somebody has come: up to Gail on the path, or past her to June at the step, or into the house. */
    private static boolean arrived(BlockPos b,LiteraryActor june,LiteraryActor gail,List<ServerPlayer> here){
        for(var p:here){
            if(!p.isAlive()||p.isSpectator())continue;
            if(p.distanceToSqr(gail)<GREET_NEAR*GREET_NEAR||p.distanceToSqr(june)<JUNE_NEAR*JUNE_NEAR||PellSite.inHouse(p.position().subtract(b.getX(),b.getY(),b.getZ())))return true;
        }
        return false;
    }
    static void say(PellPeople.Household h,LiteraryActor a,int who,String line,long now){
        // 0.4.545: Tilly's line, with a reader as Tilly, is theirs to say in their own words or not (PellTilly.prompt), in the same time.
        if(who!=PellPeople.TILLY||!PellTilly.prompt(h,a,line)){a.say(PellPeople.name(who),line);a.speaks(line);}
        h.next=now+Dialogue.ticks(line)+GAP;
    }
    /** What was last said has had its time, and whoever speaks next is not in the middle of something. */
    static boolean ready(PellPeople.Household h,long now,LiteraryActor next){return now>=h.next&&!next.saying();}
    /** Every reader near enough to hear it has heard this rule, as their own. */
    static void heard(LiteraryActor speaker,int rule,List<ServerPlayer> here){heard(speaker,rule,here,null,null);}
    /** As above, with the reader whose hand is on the speaker: their rule goes into {@code talkers}, the record the talk is holding
     *  and saves afterwards, since a save of their record here would be written over by it. */
    static void heard(LiteraryActor speaker,int rule,List<ServerPlayer> here,@Nullable ServerPlayer talker,@Nullable net.minecraft.nbt.CompoundTag talkers){
        if(rule==0)return;
        if(talker!=null&&talkers!=null)talkers.putInt("Rules",talkers.getInt("Rules")|rule);
        for(var p:here){
            if(p==talker||!p.isAlive()||p.isSpectator()||p.distanceToSqr(speaker)>HEAR*HEAR)continue;
            var own=PellHouse.own(p);int had=own.getInt("Rules");if((had&rule)==rule)continue;
            own.putInt("Rules",had|rule);PellHouse.save(p,own);
        }
    }
    private static @Nullable ServerPlayer near(Vec3 at,List<ServerPlayer> here,double reach){
        for(var p:here)if(p.isAlive()&&!p.isSpectator()&&p.position().subtract(at).horizontalDistanceSqr()<=reach*reach&&Math.abs(p.getY()-at.y)<3)return p;
        return null;
    }

    /** June, at one of her stops: on her way there, or there and waiting, or saying her piece. */
    private static void stop(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,Stop s,List<ServerPlayer> here,long now){
        if(h.waypoint<s.route().length){
            var to=world(b,s.route()[h.waypoint]);
            for(var door:ON_THE_WAY)if(june.position().distanceToSqr(Vec3.atBottomCenterOf(b.offset(door)))<1.8*1.8&&!PellSite.open(l,b,door))PellHouse.toggle(l,b,door);
            var before=june.position();
            boolean there=SceneFootpaths.walk(june,to,JUNE_PACE);
            h.moving[PellPeople.JUNE]=true;trail(h,june);
            if(there){h.waypoint++;h.stuck=0;}
            else if(june.position().distanceToSqr(before)>1e-4)h.stuck=0;
            else if(++h.stuck>STUCK&&!LiteraryVignettes.watched(l,june.getEyePosition())){june.moveTo(to.x,to.y,to.z,june.getYRot(),0);h.waypoint++;h.stuck=0;}
            if(h.waypoint<s.route().length)return;
            h.rest[PellPeople.JUNE]=s.look();
        }
        if(s.line()==null){h.step++;h.waypoint=0;return;}
        if(!ready(h,now,june))return;
        if(s.waits()&&near(june.position(),here,AT_STOP)==null)return;
        if(s.opens()!=null&&!PellSite.open(l,b,s.opens()))PellHouse.toggle(l,b,s.opens());
        say(h,june,PellPeople.JUNE,s.line(),now);
        heard(june,s.rule(),here);
        // Shown her room, Tilly goes in.
        if(PellSite.NESSA_DOOR.equals(s.opens()))h.tillyHome=true;
        h.step++;h.waypoint=0;
    }
    /** Where June has been, for Tilly to follow. */
    private static void trail(PellPeople.Household h,LiteraryActor june){
        if(h.trail.isEmpty()||h.trail.peekLast().distanceToSqr(june.position())>.04)h.trail.addLast(june.position());
        while(h.trail.size()>160)h.trail.pollFirst();
    }

    /** Tilly: a step behind June along the way June went, from when June goes in until she is shown her room; then into it. */
    private static void tilly(BlockPos b,PellPeople.Household h,LiteraryActor june,LiteraryActor tilly){
        if(h.tillyHome){
            if(h.tillyStep>=TILLY_IN.length)return;
            if(SceneFootpaths.walk(tilly,world(b,TILLY_IN[h.tillyStep]),TILLY_PACE))h.tillyStep++;
            h.moving[PellPeople.TILLY]=true;
            if(h.tillyStep>=TILLY_IN.length)h.rest[PellPeople.TILLY]=TILLY_ROOM.look();
            return;
        }
        if(h.step<TOUR)return;
        Vec3 to=null;
        for(var it=h.trail.descendingIterator();it.hasNext();){var at=it.next();if(at.distanceToSqr(june.position())>=BEHIND*BEHIND){to=at;break;}}
        if(to==null||tilly.position().distanceToSqr(to)<.09)return;
        SceneFootpaths.walk(tilly,to,TILLY_PACE);h.moving[PellPeople.TILLY]=true;
        while(!h.trail.isEmpty()&&h.trail.peekFirst()!=to)h.trail.pollFirst();
    }

    /** Gail's car at the curb, while she is here. It is a body moved every tick (TownCar), made again where it is when it is not there. */
    private static void car(ServerLevel l,BlockPos b,PellPeople.Household h){
        var at=world(b,CAR);var c=car(l,b);
        if(c==null){
            var cell=BlockPos.containing(at);if(!l.isLoaded(cell)||!l.areEntitiesLoaded(ChunkPos.asLong(cell)))return;
            c=BikeNightRegistry.TOWN_CAR.get().create(l);if(c==null)return;
            c.place(at,CAR_YAW);c.kind(TownCar.BLUE);
            if(!l.addFreshEntity(c))return;
            h.car=c.getUUID();
        }
        c.place(at,CAR_YAW);
    }
    /** Gail, her goodbye said: to her car's door, and gone with it the first moment nobody sees either. */
    private static void leave(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor gail){
        if(h.gailStep<GAIL_TO_CAR.length){
            if(SceneFootpaths.walk(gail,world(b,GAIL_TO_CAR[h.gailStep]),GAIL_PACE))h.gailStep++;
            h.moving[PellPeople.GAIL]=true;
            if(h.gailStep<GAIL_TO_CAR.length)return;
            h.rest[PellPeople.GAIL]=180;
        }
        var car=world(b,CAR).add(0,.8,0);
        if(LiteraryVignettes.watched(l,gail.getEyePosition())||LiteraryVignettes.watched(l,car))return;
        l.playSound(null,car.x,car.y,car.z,PellRegistry.CAR_AWAY.get(),SoundSource.NEUTRAL,1F,1F);
        gail.discard();
        var c=car(l,b);if(c!=null)c.discard();
        h.gailGone=true;h.car=null;
    }

    // ------------------------------------------------------------------------------------------------ 0.4.526: the first evening
    /** The house's hours: the visit's late afternoon, which holds through the tour; six, when dinner is called, which holds through
     *  dinner; half past eight, where the evening waits. Between them the hour goes on a tick of the day to a game tick. */
    public static final long AFTERNOON=10800,SIX=12000,EVENING=14500;
    /** After the day: dinner called, the table laid, being served, being eaten, being cleared, and the evening. */
    public static final int CALLED=SETTLED+1,LAID=SETTLED+2,SERVING=SETTLED+3,EATING=SETTLED+4,CLEARING=SETTLED+5,CLEARED=SETTLED+6;
    /** How long June waits after calling before she serves whoever has come; how long Tilly takes over hers; how long dinner lasts
     *  at most; how long June waits for the children to leave the kitchen before she clears; how long she is at the sink. */
    public static final int SERVE_BY=600,TILLY_EATS=300,DINNER_LONGEST=1200,LEAVE_TABLE=100,WASHING=200;
    /** A plate: not there, laid, served, scraped; 0.4.531, breakfast on it. Who a place is for besides the house's people: a reader,
     *  or nobody. */
    public static final int NONE=0,LAID_PLATE=1,SERVED=2,SCRAPED=3,BREAKFAST=4,GUEST=10,NOBODY=-1;
    /** A plate with a meal on it, dinner or breakfast. */
    static boolean full(int state){return state==SERVED||state==BREAKFAST;}
    /** One of the table's four places: its chair, where its plate lies on the table, which way a body sitting there faces, and where
     *  one stands to get into it. Tilly's is by the hall door and Callum's by the stove; the two by the fridge are on the far side,
     *  got into from the near end. */
    public record Place(BlockPos chair,Vec3 plate,float look,Vec3 beside){}
    public static final Place[] TABLE={
        new Place(new BlockPos(-11,0,-68),new Vec3(-10.5,.95,-68.27),180,v(-9.5,-67.5)),
        new Place(new BlockPos(-11,0,-70),new Vec3(-10.5,.95,-68.73),0,v(-9.5,-69.5)),
        new Place(new BlockPos(-12,0,-70),new Vec3(-11.5,.95,-68.73),0,v(-9.5,-69.5)),
        new Place(new BlockPos(-12,0,-68),new Vec3(-11.5,.95,-68.27),180,v(-9.5,-67.5))};
    /** Where June stands to lay the table, serve and clear: its near end. Where she stands to wash up: at the sink. */
    public static final PellPeople.Spot TABLE_END=new PellPeople.Spot(-9.5,-68.6,90),SINK=new PellPeople.Spot(-9.5,-69.6,180);
    /** Tilly's way from Nessa's room to the kitchen door, and Callum's from the end of the hall; and back (0.4.531: after breakfast too). */
    static final Vec3[] TILLY_TO_KITCHEN={v(-9.5,-62.8),v(-9.5,-64.6),v(-9.5,-65.6),v(-9.5,-66.6)},
        TILLY_BACK={v(-9.5,-66.6),v(-9.5,-65.6),v(-9.5,-64.6),v(-9.5,-62.8),v(TILLY_ROOM.x(),TILLY_ROOM.z())},
        CALLUM_TO_KITCHEN={v(-9.5,-65.6),v(-9.5,-66.6)},
        CALLUM_BACK={v(-9.5,-66.6),v(-9.5,-65.6),v(PellPeople.FIRST_VISIT[PellPeople.CALLUM].x(),PellPeople.FIRST_VISIT[PellPeople.CALLUM].z())};
    /** The tag on the plates' bodies, so that any left from before (saved with the world) are taken away when the visit begins again. */
    public static final String PLATE_TAG="pell_plate";

    /** The house's hour now (for tests and for what comes after). */
    public static long hour(BlockPos b){return PellPeople.household(b).time;}
    /** For tests: the hour put on to this, if it is earlier. */
    public static void hurry(BlockPos b,long to){var h=PellPeople.household(b);if(h.time<to)h.time=to;}
    /** Whether one of the people is sitting at the table, and at which place (-1 if not). */
    public static int placeOf(BlockPos b,int who){var h=PellPeople.household(b);return h.seated[who]?h.place[who]:-1;}
    /** A place's plate (NONE to SCRAPED), and who it is for (a person, GUEST, or NOBODY). */
    public static int plate(BlockPos b,int place){return PellPeople.household(b).plate[place];}
    public static int diner(BlockPos b,int place){return PellPeople.household(b).diner[place];}
    /** The places June has served, in the order she served them. */
    public static List<Integer> served(BlockPos b){var h=PellPeople.household(b);return List.copyOf(h.order.subList(0,Math.min(h.serving,h.order.size())));}
    /**
     * For tests: the day as June's tour leaves it, without walking it. Gail and her car gone, June at her stove, Tilly by Nessa's bed,
     * Callum at the end of the hall, the doors June opened open, and the afternoon on its way to six.
     */
    public static void settle(ServerLevel l,BlockPos b){
        var h=PellPeople.household(b);
        var gail=PellPeople.find(l,b,PellPeople.GAIL);if(gail!=null)gail.discard();var c=car(l,b);if(c!=null)c.discard();
        h.gailGone=true;h.car=null;h.fresh=false;h.empty=0;
        stand(l,b,h,PellPeople.JUNE,KITCHEN);stand(l,b,h,PellPeople.TILLY,TILLY_ROOM);stand(l,b,h,PellPeople.CALLUM,PellPeople.FIRST_VISIT[PellPeople.CALLUM]);
        for(var door:HER_DOORS)if(!PellSite.open(l,b,door))PellHouse.toggle(l,b,door);
        h.tillyHome=true;h.tillyStep=TILLY_IN.length;h.trail.clear();h.step=SETTLED;h.waypoint=0;h.next=0;h.stuck=0;
    }
    static void stand(ServerLevel l,BlockPos b,PellPeople.Household h,int who,PellPeople.Spot s){
        var a=PellPeople.find(l,b,who);if(a==null)return;
        a.moveTo(b.getX()+s.x(),b.getY(),b.getZ()+s.z(),s.look(),0);a.setYHeadRot(s.look());a.setYBodyRot(s.look());a.setDeltaMovement(Vec3.ZERO);
        h.rest[who]=s.look();h.seated[who]=false;h.place[who]=-1;h.way[who].clear();h.sitAt[who]=-1;
    }

    /** The house's hour: held through the tour and through dinner, going on to six once the day is settled and on into the evening
     *  after; sent to every reader at the house when it starts or stops going on, when they first come, and once a second. 0.4.530:
     *  locked up, it goes on to eleven, faster while June is in her room; from her rounds to midnight. 0.4.531: from June's return to
     *  her room, on through the night to six, as fast again; while she goes round the doors to the kitchen, to half past; once breakfast
     *  is laid, to seven; from breakfast's call to eight, as by day; and while every reader at the house is asleep, much faster. 0.4.535:
     *  a week in, the afternoon also holds while June is away at the laundry (PellHand). */
    static void clock(PellPeople.Household h,List<ServerPlayer> here,long now){
        long to=h.step==SETTLED?(h.visit>=2&&(!h.welcomed||PellHand.holds(h))?h.time:SIX):h.step==PellNight.SMALL_HOURS&&PellKnife.holds(h)?h.time:h.step==PellNight.SMALL_HOURS&&PellSwim.out(h)?PellSwim.HALF_FIVE:h.step>=PellMorning.CALLED?PellMorning.EIGHT:h.step>=PellMorning.LAID?PellMorning.SEVEN
            :h.step>=PellMorning.RISING?PellMorning.HALF_SIX:h.step>=PellNight.SMALL_HOURS?PellMorning.UP
            :h.step>=PellNight.ROUNDS?PellNight.TWELVE:h.step>=NIGHT?(h.visit>=PellWeek.LAST?(PellDawn.after(h)||PellTilly.holdingOut(h)?PellMorning.DAWN:PellNight.TWELVE):PellNight.ELEVEN)
            // 0.4.538: on the last night the evening holds until a reader first comes in.
            :h.step>=CLEARED?(h.visit>=PellWeek.LAST&&!h.welcomed?h.time:EVENING):h.time;
        // 0.4.541: on the last night, once the rite is over either way, the rest of the night goes faster, to first light (PellDawn).
        int pace=h.hurried?PellMorning.HURRY:h.step==NIGHT&&PellDawn.after(h)?PellDawn.AFTER_PACE:h.step==NIGHT||h.step>=PellNight.SMALL_HOURS&&h.step<PellMorning.CALLED?PellNight.NIGHT_PACE:1;
        if(h.time<to)h.time=Math.min(to,h.time+pace);
        int rate=h.time<to?pace:0;boolean changed=rate!=h.rate;h.rate=rate;
        var payload=new io.github.knaitoe.theoldesthouse.network.PellPayload(h.time,h.rate);
        for(var p:here)if(changed||now%20==0||!h.told.contains(p.getUUID())){h.told.add(p.getUUID());io.github.knaitoe.theoldesthouse.network.HousePackets.send(p,payload);}
    }

    /** Sends one of them along a way, to sit at a place at the end of it, or (with none) to stand there facing this way. */
    static void go(PellPeople.Household h,int who,int place,float look,Vec3... way){h.way[who].clear();h.way[who].addAll(Arrays.asList(way));h.sitAt[who]=place;h.endLook[who]=look;h.stuckFor[who]=0;h.perchTo[who]=null;h.sleepAt[who]=null;}
    /** 0.4.527: along a way, to sit in a seat away from the table at the end of it (perch), or to lie down at a place (lie). */
    static void goPerch(PellPeople.Household h,int who,Perch perch,Vec3... way){go(h,who,-1,perch.look(),way);h.perchTo[who]=perch;}
    static void goSleep(PellPeople.Household h,int who,Vec3 at,float look,Vec3... way){go(h,who,-1,look,way);h.sleepAt[who]=at;}
    static double pace(int who){return switch(who){case PellPeople.JUNE->JUNE_PACE;case PellPeople.TILLY->TILLY_PACE;case PellPeople.GAIL->GAIL_PACE;default->CALLUM_PACE;};}
    /** Each of them walking somewhere goes a step along their way; at its end they sit, or stand as they were sent to. Somebody who
     *  stands unable to get nearer for a while, unseen, is put at the next point. */
    static void walkers(ServerLevel l,BlockPos b,PellPeople.Household h){
        for(int who=0;who<PellPeople.COUNT;who++){
            if(h.way[who].isEmpty())continue;var a=PellPeople.find(l,b,who);if(a==null)continue;
            // 0.4.531: with every reader asleep, straight to the end of the way.
            if(h.hurried){var end=world(b,h.way[who].peekLast());a.moveTo(end.x,end.y,end.z,h.endLook[who],0);a.setYHeadRot(h.endLook[who]);a.setYBodyRot(h.endLook[who]);h.way[who].clear();h.stuckFor[who]=0;}
            if(h.way[who].isEmpty()){arrive(l,b,h,who,a);continue;}
            var to=world(b,h.way[who].peekFirst());var before=a.position();
            through(l,b,who,a);
            boolean there=SceneFootpaths.walk(a,to,pace(who));h.moving[who]=true;
            if(there){h.way[who].pollFirst();h.stuckFor[who]=0;}
            else if(a.position().distanceToSqr(before)>1e-4)h.stuckFor[who]=0;
            else if(++h.stuckFor[who]>STUCK&&!LiteraryVignettes.watched(l,a.getEyePosition())){a.moveTo(to.x,to.y,to.z,a.getYRot(),0);h.way[who].pollFirst();h.stuckFor[who]=0;}
            if(!h.way[who].isEmpty())continue;
            arrive(l,b,h,who,a);
        }
    }
    /** At the end of their way: they sit, perch, lie down, or stand as they were sent to. */
    private static void arrive(ServerLevel l,BlockPos b,PellPeople.Household h,int who,LiteraryActor a){
        if(h.sitAt[who]>=0)sit(l,b,h,who,a,h.sitAt[who]);
        else if(h.perchTo[who]!=null)perch(l,b,h,who,a,h.perchTo[who]);
        else if(h.sleepAt[who]!=null)lie(l,b,h,who,a,h.sleepAt[who]);
        else h.rest[who]=h.endLook[who];
        h.sitAt[who]=-1;h.perchTo[who]=null;h.sleepAt[who]=null;
    }
    /** Into a chair at the table, turned to it; or, if a reader has sat there since, the next one free; or standing, if none is. */
    static void sit(ServerLevel l,BlockPos b,PellPeople.Household h,int who,LiteraryActor a,int place){
        if(readerAt(l,b,place)){
            int other=-1;for(int i=0;i<TABLE.length;i++)if(i!=place&&h.diner[i]==NOBODY&&!readerAt(l,b,i)){other=i;break;}
            if(other<0){h.rest[who]=TABLE[place].look();return;}
            h.diner[place]=NOBODY;h.diner[other]=who;place=other;
        }
        var c=TABLE[place];var at=Vec3.atBottomCenterOf(b.offset(c.chair()));
        a.moveTo(at.x,at.y,at.z,c.look(),0);a.setYHeadRot(c.look());a.setYBodyRot(c.look());a.setDeltaMovement(Vec3.ZERO);
        h.seated[who]=true;h.place[who]=place;h.rest[who]=c.look();
    }
    /** Up out of their chair, to stand beside it, and away along a way. */
    static void up(ServerLevel l,BlockPos b,PellPeople.Household h,int who,float look,Vec3... way){
        var a=PellPeople.find(l,b,who);if(a==null)return;
        if(h.seated[who]){var at=world(b,TABLE[h.place[who]].beside());a.moveTo(at.x,at.y,at.z,a.getYRot(),0);}
        unperch(b,h,who,a);
        h.seated[who]=false;h.place[who]=-1;go(h,who,-1,look,way);
    }
    /** A reader is sitting at this place. */
    static boolean readerAt(ServerLevel l,BlockPos b,int place){return !l.getEntitiesOfClass(io.github.knaitoe.theoldesthouse.house.SeatEntity.class,new AABB(b.offset(TABLE[place].chair()))).isEmpty();}
    /** The first place from this one round the table that nobody has and no reader is sitting at. */
    static int free(ServerLevel l,BlockPos b,PellPeople.Household h,int from){for(int k=0;k<TABLE.length;k++){int i=(from+k)%TABLE.length;if(h.diner[i]==NOBODY&&!readerAt(l,b,i))return i;}return -1;}
    /** A reader has come to dinner: into the kitchen, or sat at the table. */
    static boolean come(ServerLevel l,BlockPos b,List<ServerPlayer> here){
        for(var p:here){
            if(!p.isAlive()||p.isSpectator())continue;var r=p.position().subtract(b.getX(),b.getY(),b.getZ());
            if(r.x>=-13&&r.x<-6&&r.z>=-71&&r.z<-67&&r.y>-1&&r.y<3)return true;
        }
        return false;
    }
    static boolean there(LiteraryActor a,BlockPos b,PellPeople.Spot s){return a.position().subtract(b.getX()+s.x(),a.getY(),b.getZ()+s.z()).horizontalDistanceSqr()<.09;}

    /** After the day: dinner at six, and the evening. */
    static void evening(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,List<ServerPlayer> here,long now){
        // 0.4.530: locked up, the night is PellNight's.
        if(h.step>=NIGHT){PellNight.tick(l,b,h,june,here,now);return;}
        int J=PellPeople.JUNE,T=PellPeople.TILLY,C=PellPeople.CALLUM;
        boolean juneStill=h.way[J].isEmpty();
        // By how far past the settled day the visit is (the steps follow the tour, whose length is the stops'): settled, called,
        // laid, serving, eating, clearing, cleared.
        switch(h.step-SETTLED){
            case 0->{
                // 0.4.533: a week in, June calls hello from the kitchen to the first reader in; the afternoon goes on from then.
                if(h.visit>=2&&!h.welcomed){PellWeek.welcome(l,b,h,june,here,now);return;}
                // 0.4.535: a while after her hello, the laundry (PellHand); while she is away, the hour and dinner wait.
                if(PellHand.tick(l,b,h,june,here,now))return;
                if(h.time>=SIX&&ready(h,now,june))call(l,b,h,june,here,now);
            }
            case 1->{
                // At the table's end: four places laid. Then to the stove for the saucepan.
                if(!juneStill)return;
                for(int i=0;i<TABLE.length;i++)plate(l,b,h,i,LAID_PLATE);
                var at=world(b,v(TABLE_END.x(),TABLE_END.z()));l.playSound(null,at.x,at.y+1,at.z,SoundEvents.DECORATED_POT_PLACE,SoundSource.BLOCKS,.5F,1.5F);
                go(h,J,-1,KITCHEN.look(),v(KITCHEN.x(),KITCHEN.z()));h.step=LAID;
            }
            case 2->{
                if(!juneStill)return;
                if(june.getMainHandItem().isEmpty())june.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(PellRegistry.SAUCEPAN.get()));
                if(!come(l,b,here)&&now-h.calledAt<SERVE_BY)return;
                // Tilly first; then a place for each reader at the house, where they sit if they are sitting; Callum's is passed over.
                h.order.clear();h.serving=0;
                for(int i=0;i<TABLE.length;i++)if(h.diner[i]==T)h.order.add(i);
                int readers=0;for(var p:here)if(p.isAlive()&&!p.isSpectator())readers++;
                for(int i=0;i<TABLE.length&&readers>0;i++)if(h.diner[i]==NOBODY&&readerAt(l,b,i)){h.diner[i]=GUEST;h.order.add(i);readers--;}
                for(int i=0;i<TABLE.length&&readers>0;i++)if(h.diner[i]==NOBODY){h.diner[i]=GUEST;h.order.add(i);readers--;}
                go(h,J,-1,TABLE_END.look(),v(TABLE_END.x(),TABLE_END.z()));h.step=SERVING;
            }
            case 3->{
                if(!juneStill||!ready(h,now,june))return;
                if(h.serving<h.order.size()){
                    int i=h.order.get(h.serving++);var at=world(b,TABLE[i].plate());
                    h.rest[J]=PellPeople.yawTo(june,at);
                    plate(l,b,h,i,SERVED);l.playSound(null,at.x,at.y,at.z,SoundEvents.HONEY_BLOCK_PLACE,SoundSource.NEUTRAL,.5F,1.2F);
                    boolean tillys=h.diner[i]==T;
                    // 0.4.533: a week in, the wrong name first.
                    boolean slip=tillys&&h.visit>=2;
                    say(h,june,J,slip?PellTexts.JUNE_SERVES_NESSA:tillys?PellTexts.JUNE_SERVES_TILLY:PellTexts.JUNE_SERVES,now);
                    if(tillys)present(june,"SawTillyServed",here);
                    if(slip)present(june,"HeardNessaAtDinner",here);
                    return;
                }
                // Everybody served but Callum. She asks after Tilly's, and takes the saucepan back to the stove.
                h.rest[J]=PellPeople.yawTo(june,world(b,TABLE[0].plate()));
                say(h,june,J,PellTexts.JUNE_ASKS,now);
                go(h,J,-1,90,v(KITCHEN.x(),KITCHEN.z()));h.servedAt=now;h.step=EATING;
            }
            case 4->{
                if(juneStill&&!june.getMainHandItem().isEmpty())june.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
                int t=-1;for(int i=0;i<TABLE.length;i++)if(h.diner[i]==T)t=i;
                // 0.4.545: a reader as Tilly eats theirs, or doesn't (table).
                if(t>=0&&h.plate[t]==SERVED&&now-h.servedAt>=TILLY_EATS&&h.tillyReader==null)plate(l,b,h,t,SCRAPED);
                boolean done=t<0||h.plate[t]!=SERVED;
                for(int i=0;i<TABLE.length;i++)if(h.diner[i]==GUEST&&h.plate[i]==SERVED)done=false;
                if(!(done||now-h.servedAt>=DINNER_LONGEST)||!juneStill||!ready(h,now,june))return;
                // "Leave those." The children get down and go; she clears once they have.
                say(h,june,J,PellTexts.JUNE_CLEARS,now);
                up(l,b,h,T,TILLY_ROOM.look(),TILLY_BACK);up(l,b,h,C,PellPeople.FIRST_VISIT[C].look(),CALLUM_BACK);
                h.clearedAt=now;h.step=CLEARING;
            }
            case 5->{
                if(juneStill&&!there(june,b,TABLE_END)){
                    if(now-h.clearedAt<LEAVE_TABLE&&(inKitchen(l,b,T)||inKitchen(l,b,C)))return;
                    go(h,J,-1,TABLE_END.look(),v(TABLE_END.x(),TABLE_END.z()));return;
                }
                if(!juneStill)return;
                for(int i=0;i<TABLE.length;i++)plate(l,b,h,i,NONE);
                var at=world(b,v(TABLE_END.x(),TABLE_END.z()));l.playSound(null,at.x,at.y+1,at.z,SoundEvents.DECORATED_POT_PLACE,SoundSource.BLOCKS,.5F,1.3F);
                june.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(PellRegistry.SCRAPED.get()));
                go(h,J,-1,SINK.look(),v(SINK.x(),SINK.z()));h.clearedAt=now;h.step=CLEARED;
            }
            case 6->{
                // At the sink a while; then (0.4.527) through to the living room, to her chair and the television. The evening goes on
                // to half past eight (clock).
                if(juneStill&&!june.getMainHandItem().isEmpty()&&there(june,b,SINK)&&now-h.clearedAt>=WASHING){
                    june.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);goPerch(h,J,RECLINER,TO_RECLINER);h.step=TELLY;
                }
            }
            case 7->telly(l,b,h,june,here,now);
            case 8->{
                // Bedtime called: when June and Tilly are both on the edge of Nessa's bed, she starts to brush.
                var tilly=PellPeople.find(l,b,T);if(tilly==null)return;
                if(h.perch[J]!=JUNE_BED||h.perch[T]!=TILLY_BED){
                    // A walker who came to their side of the bed and found it taken waits there and tries again.
                    if(juneStill&&h.perch[J]==null&&near(june,b,JUNE_BED.beside(),.5))perch(l,b,h,J,june,JUNE_BED);
                    if(h.tillyReader==null&&h.way[T].isEmpty()&&h.perch[T]==null&&near(tilly,b,TILLY_BED.beside(),.5))perch(l,b,h,T,tilly,TILLY_BED);
                    // 0.4.545: a reader as Tilly sits on the edge of the bed themselves (PellTilly.bed). Not there a while after June is,
                    // she says goodnight to the bed and goes to send the others out.
                    if(h.tillyReader!=null&&h.perch[J]==JUNE_BED&&now-h.bedAt>=PellTilly.BED_WAIT&&ready(h,now,june)){say(h,june,J,PellTexts.JUNE_NIGHT_NIGHT,now);goodnight(l,b,h);}
                    return;
                }
                june.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(PellRegistry.HAIRBRUSH.get()));june.gesture(PellPeople.BRUSH);tilly.gesture(PellPeople.BRUSHED);
                h.brushedAt=now;h.brushSaid=0;h.step=BRUSHING;
            }
            case 9->brushing(l,b,h,june,here,now);
            case 10->{
                // On her way to the sliding door; there, turned to the room, she sends them out, or says goodnight to an empty house.
                if(!juneStill||!near(june,b,v(AT_LOCK[0].x(),AT_LOCK[0].z()),.6))return;
                h.rest[J]=0;
                if(!ready(h,now,june))return;
                if(anyoneInside(b,here)){say(h,june,J,PellTexts.JUNE_SENDS_OUT,now);far(june,PellTexts.SENT_FAR,b,here);present(june,"SentToGarage",here,HEAR,b);}
                else say(h,june,J,PellTexts.JUNE_LOCKING,now);
                h.nudgedAt=now;h.lockIndex=0;h.step=SENDING;
            }
            case 11,12->locking(l,b,h,june,here,now);
            default->{}
        }
    }
    /** Six o'clock. June calls them, from the kitchen; whoever is too far off hears it from where they are. Tilly and Callum come to
     *  their places, and June goes to the table to lay it. */
    static void call(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,List<ServerPlayer> here,long now){
        say(h,june,PellPeople.JUNE,PellTexts.JUNE_CALLS,now);
        for(var p:here)if(p.isAlive()&&!p.isSpectator()&&p.distanceToSqr(june)>HEAR*HEAR)PellHouse.cue(p,PellTexts.CALLED_FAR);
        Arrays.fill(h.diner,NOBODY);
        int t=free(l,b,h,0);if(t>=0)h.diner[t]=PellPeople.TILLY;
        int c=free(l,b,h,1);if(c>=0)h.diner[c]=PellPeople.CALLUM;
        if(t>=0)go(h,PellPeople.TILLY,t,TABLE[t].look(),with(TILLY_TO_KITCHEN,TABLE[t].beside()));
        // 0.4.545: a reader as Tilly comes to it themselves.
        if(t>=0)PellTilly.cue(l,h,PellTexts.TILLY_R_PLACE);
        if(c>=0)go(h,PellPeople.CALLUM,c,TABLE[c].look(),with(CALLUM_TO_KITCHEN,TABLE[c].beside()));
        go(h,PellPeople.JUNE,-1,TABLE_END.look(),v(TABLE_END.x(),TABLE_END.z()));
        h.calledAt=now;h.step=CALLED;
    }
    private static Vec3[] with(Vec3[] way,Vec3 last){var out=Arrays.copyOf(way,way.length+1);out[way.length]=last;return out;}
    /** One of the people somewhere in the kitchen. */
    static boolean inKitchen(ServerLevel l,BlockPos b,int who){
        var a=PellPeople.find(l,b,who);if(a==null)return false;var r=a.position().subtract(b.getX(),b.getY(),b.getZ());
        return r.x>=-13&&r.x<-6&&r.z>=-71&&r.z<-67;
    }
    /** Every reader near enough to see it was there (their own record). */
    static void present(LiteraryActor at,String key,List<ServerPlayer> here){
        for(var p:here){
            if(!p.isAlive()||p.isSpectator()||p.distanceToSqr(at)>HEAR*HEAR)continue;
            var own=PellHouse.own(p);if(own.getBoolean(key))continue;own.putBoolean(key,true);PellHouse.save(p,own);
        }
    }
    /** A place's plate, as it now is (laid, served, scraped), or taken away. Each is a body lying on the table, saved with the world. */
    static void plate(ServerLevel l,BlockPos b,PellPeople.Household h,int i,int state){
        var old=h.plates[i]==null?null:l.getEntity(h.plates[i]);if(old!=null)old.discard();
        h.plates[i]=null;h.plate[i]=state;
        if(state==NONE)return;
        var item=new ItemStack(switch(state){case SERVED->PellRegistry.DINNER.get();case BREAKFAST->PellRegistry.BREAKFAST.get();case SCRAPED->PellRegistry.SCRAPED.get();default->PellRegistry.PLATE.get();});
        var d=KitchenFittings.lay(l,world(b,TABLE[i].plate()),item,TABLE[i].look(),PLATE_TAG,.45F);
        if(d!=null)h.plates[i]=d.getUUID();
    }
    /** Any plates lying at this house, from a visit before. */
    public static void clearPlates(ServerLevel l,BlockPos b){for(var d:l.getEntitiesOfClass(net.minecraft.world.entity.Display.ItemDisplay.class,PellPeople.area(b),e->e.getTags().contains(PLATE_TAG)))d.discard();}

    /**
     * A hand on the table. At the place nearest the reader, it finds what is there: Tilly's dinner, Callum's empty plate, a place laid
     * for nobody; or, at a place served for a reader, their dinner, which they eat. True if the table answered.
     */
    public static boolean table(ServerPlayer p,BlockPos b,CompoundTag own){
        var h=PellPeople.household(b);int best=-1;double bestD=2.4*2.4;
        for(int i=0;i<TABLE.length;i++){if(h.plate[i]==NONE)continue;double d=p.position().subtract(world(b,TABLE[i].plate())).horizontalDistanceSqr();if(d<bestD){bestD=d;best=i;}}
        if(best<0)return false;
        int state=h.plate[best],diner=h.diner[best];
        // 0.4.545: Tilly's place is a reader as Tilly's own.
        boolean mine=diner==GUEST||diner==PellPeople.TILLY&&PellTilly.isTilly(h,p);
        if(diner==PellPeople.TILLY&&!mine){PellHouse.cue(p,state==SCRAPED?PellTexts.TILLYS_EATEN:state==SERVED?PellTexts.TILLYS_PLATE:state==BREAKFAST?PellTexts.TILLYS_BREAKFAST:PellTexts.PLATE_EMPTY);return true;}
        if(diner==PellPeople.CALLUM){PellHouse.cue(p,PellTexts.CALLUMS_PLATE);return true;}
        if(!mine){PellHouse.cue(p,h.step>=SERVING&&h.step<PellMorning.RISING||h.step>=PellMorning.LAID?PellTexts.NOBODY_PLACE:PellTexts.PLATE_EMPTY);return true;}
        if(state==LAID_PLATE){PellHouse.cue(p,PellTexts.PLATE_EMPTY);return true;}
        if(state==SCRAPED){PellHouse.cue(p,PellTexts.SCRAPED_PLATE);return true;}
        // 0.4.531: breakfast as dinner was.
        boolean morning=state==BREAKFAST;
        p.getFoodData().eat(morning?5:6,.6F);
        p.serverLevel().playSound(null,p.getX(),p.getY(),p.getZ(),SoundEvents.GENERIC_EAT,SoundSource.PLAYERS,.8F,1F);
        plate(p.serverLevel(),b,h,best,SCRAPED);own.putBoolean(morning?"AteBreakfast":"Ate",true);PellHouse.cue(p,morning?PellTexts.ATE_BREAKFAST:PellTexts.ATE);
        return true;
    }

    // ------------------------------------------------------------------------------------------------ 0.4.527: bedtime
    /** Ten o'clock. 0.4.530: once the house is locked up the hour goes on past it to eleven, when June goes out (PellNight). */
    public static final long LATE=16000;
    /**
     * After the washing up: June in her chair with the tape on; bedtime called; Tilly's hair being brushed; Tilly lying down and June on
     * her way to the sliding door; June sending them out and waiting for the house to empty; locking up; the night.
     */
    public static final int TELLY=SETTLED+7,BEDTIME=SETTLED+8,BRUSHING=SETTLED+9,TUCKED=SETTLED+10,SENDING=SETTLED+11,LOCKING=SETTLED+12,NIGHT=SETTLED+13;
    /** How often the tape's sound begins again while it plays; how long June brushes, at the least; how often she asks again while
     *  somebody stays in the house after she has sent them out. */
    public static final int TAPE_EVERY=170,BRUSH_TIME=240,NUDGE=400;
    /** How near June a reader stands to have seen her brush Tilly's hair. */
    public static final double SEE_NEAR=5;
    /** A seat away from the table: its cell, where a body sits in it and which way they face, and where one stands to get in or out. */
    public record Perch(BlockPos cell,Vec3 at,float look,Vec3 beside){}
    /** June's recliner, turned to the television; the edge of Nessa's bed, Tilly by its head and June beside her, turned to her. */
    public static final Perch RECLINER=new Perch(new BlockPos(-2,0,-70),new Vec3(-1.5,0,-69.5),90,v(-2.6,-69.5)),
        TILLY_BED=new Perch(new BlockPos(-13,0,-61),new Vec3(-12.5,0,-60.3),0,v(-12.5,-59.6)),
        JUNE_BED=new Perch(new BlockPos(-12,0,-61),new Vec3(-11.5,0,-60.3),35,v(-11.5,-59.6));
    /** Where Tilly lies, along Nessa's bed, and Callum, on his mattress in the circle. */
    public static final Vec3 TILLY_SLEEPS=new Vec3(-12.5,0,-60.5),CALLUM_SLEEPS=new Vec3(-3.5,0,-60.6);
    /** The television. */
    public static final BlockPos TV=new BlockPos(-5,0,-70);
    /** The outside doors June locks, in the order she locks them, where she stands inside each to do it, and her way from each to the
     *  next; then her room. */
    public static final List<BlockPos> LOCKS=List.of(PellSite.SLIDER,PellSite.BACK_DOOR,PellSite.FRONT_DOOR);
    public static final PellPeople.Spot[] AT_LOCK={new PellPeople.Spot(.5,-70.6,180),new PellPeople.Spot(8.5,-70.5,180),new PellPeople.Spot(.5,-59.6,0)};
    public static final PellPeople.Spot HER_ROOM=new PellPeople.Spot(8.5,-61.5,0);
    /** Every door of the house's own, shut when the visit begins again (0.4.530: the shed's too, which June opens at night). */
    static final List<BlockPos> ALL_DOORS=List.of(PellSite.FRONT_DOOR,PellSite.NESSA_DOOR,PellSite.CALLUM_DOOR,PellSite.BATH_DOOR,PellSite.JUNE_DOOR,PellSite.LAUNDRY_DOOR,
        PellSite.BACK_DOOR,PellSite.SLIDER,PellSite.GARAGE_DOOR,PellSite.SHED_DOOR);
    /** The inside doors the people open as they come to them, and June's own, which only she opens. */
    private static final List<BlockPos> INSIDE_DOORS=List.of(PellSite.NESSA_DOOR,PellSite.CALLUM_DOOR,PellSite.LAUNDRY_DOOR,PellSite.BATH_DOOR);
    private static final Vec3[] TO_RECLINER={v(-9.5,-68.5),v(-9.5,-65.6),v(0,-65.6),v(0,-68.5),v(-2.6,-68.6),v(-2.6,-69.5)},
        RECLINER_TO_NESSA={v(-2.6,-68.6),v(0,-68.5),v(0,-65.6),v(-9.5,-65.6),v(-9.5,-64.6),v(-9.5,-62.8),v(-10.6,-59.7),v(-11.5,-59.6)},
        TILLY_TO_BED={v(-10.6,-59.7),v(-12.5,-59.6)},
        CALLUM_TO_BED={v(-4.5,-65.6),v(-3.5,-64.6),v(-3.5,-63.2),v(-3.5,-61.9)},
        NESSA_TO_SLIDER={v(-10.6,-59.7),v(-9.5,-62.8),v(-9.5,-64.6),v(-9.5,-65.6),v(0,-65.6),v(0,-68.5),v(.5,-70.6)};
    private static final Vec3[][] TO_LOCK={{},
        {v(0,-68.5),v(0,-65.6),v(7.5,-65.6),v(7.5,-66.6),v(7.5,-68.4),v(7.5,-69.5),v(8.5,-70.5)},
        {v(7.5,-69.5),v(7.5,-68.4),v(7.5,-66.6),v(7.5,-65.6),v(.5,-65.6),v(.5,-59.6)},
        {v(.5,-62),v(.5,-65.6),v(8.5,-65.6),v(8.5,-63.4),v(8.5,-61.5)}};

    /** Whether June is in her chair with the tape on. */
    public static boolean watching(BlockPos b){var h=PellPeople.household(b);return h.step==TELLY&&h.perch[PellPeople.JUNE]==RECLINER;}
    /** Where one of them sits away from the table, if anywhere. */
    public static @Nullable Perch perched(BlockPos b,int who){return PellPeople.household(b).perch[who];}
    /** Whether one of them is asleep. */
    public static boolean asleep(BlockPos b,int who){return PellPeople.household(b).asleep[who];}
    /** Whether June has locked this outside door. */
    public static boolean locked(BlockPos b,BlockPos door){return PellPeople.household(b).locked.contains(door);}
    /** The lock turned back from inside. */
    public static void unlock(BlockPos b,BlockPos door){PellPeople.household(b).locked.remove(door);}
    /** Whether the television is on. */
    public static boolean tvOn(ServerLevel l,BlockPos b){var s=l.getBlockState(b.offset(TV));return PellThing.is(s,PellThing.Kind.TV_CABINET)&&s.getValue(PellThing.STAGE)==1;}
    /**
     * For tests: the evening as dinner leaves it, without eating it. The day settled, the table bare, June at the sink with the plates
     * and done washing up, Tilly in Nessa's room and Callum at the end of the hall, and half past six.
     */
    public static void dined(ServerLevel l,BlockPos b){
        settle(l,b);var h=PellPeople.household(b);
        stand(l,b,h,PellPeople.JUNE,SINK);var june=PellPeople.find(l,b,PellPeople.JUNE);
        if(june!=null)june.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(PellRegistry.SCRAPED.get()));
        h.step=CLEARED;h.clearedAt=l.getGameTime()-WASHING;h.time=SIX+500;
    }

    /** June, after the washing up: to her chair, the television on, the tape's sound now and again; at half past eight, bedtime. */
    static void telly(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,List<ServerPlayer> here,long now){
        int J=PellPeople.JUNE;boolean juneStill=h.way[J].isEmpty();
        // At her chair (in it, or beside it): the television on with the remote from the side table, the tape playing.
        if(h.perch[J]==RECLINER||juneStill&&near(june,b,RECLINER.beside(),.5)){
            if(!tvOn(l,b)){tv(l,b,true);var at=Vec3.atCenterOf(b.offset(TV));l.playSound(null,at.x,at.y,at.z,SoundEvents.STONE_BUTTON_CLICK_ON,SoundSource.BLOCKS,.4F,1.4F);h.tapeAt=now-TAPE_EVERY;}
            // A reader in her chair: she says so, once, and stands beside it until they get up.
            if(h.perch[J]==null){
                if(readerIn(l,b,RECLINER.cell())){if(!h.chairSaid&&ready(h,now,june)){say(h,june,J,PellTexts.JUNE_MY_CHAIR,now);h.chairSaid=true;}h.rest[J]=RECLINER.look();}
                else perch(l,b,h,J,june,RECLINER);
            }
        }
        if(tvOn(l,b)&&now-h.tapeAt>=TAPE_EVERY){h.tapeAt=now;var at=Vec3.atCenterOf(b.offset(TV)).add(0,.5,0);l.playSound(null,at.x,at.y,at.z,PellRegistry.TAPE.get(),SoundSource.NEUTRAL,.9F,1F);}
        // 0.4.538: the last night, she says it will be an early night to the first reader in (PellLastNight).
        if(h.visit>=PellWeek.LAST&&!h.welcomed){PellLastNight.welcome(l,b,h,june,here,now);return;}
        if(h.time<EVENING||!juneStill||!ready(h,now,june))return;
        // Half past eight. The television off, and bedtime: Tilly to the edge of Nessa's bed, June after her, Callum to his circle.
        tv(l,b,false);
        say(h,june,J,PellTexts.JUNE_BEDTIME,now);
        up(l,b,h,J,JUNE_BED.look(),RECLINER_TO_NESSA);h.perchTo[J]=JUNE_BED;
        var tilly=PellPeople.find(l,b,PellPeople.TILLY);
        if(tilly!=null){unperch(b,h,PellPeople.TILLY,tilly);goPerch(h,PellPeople.TILLY,TILLY_BED,TILLY_TO_BED);}
        // 0.4.545: a reader as Tilly is told where June is going.
        PellTilly.cue(l,h,PellTexts.TILLY_R_BED);h.bedAt=now;
        // 0.4.538: on the last night Callum stays out on the line.
        var callum=PellPeople.find(l,b,PellPeople.CALLUM);
        if(callum!=null&&!h.callumStrip)goSleep(h,PellPeople.CALLUM,CALLUM_SLEEPS,0,CALLUM_TO_BED);
        h.step=BEDTIME;
    }
    /** On the edge of the bed: June brushes, they say what they say with time between, and when she has brushed long enough Tilly
     *  lies down, June says goodnight and goes to send the others out. */
    static void brushing(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,List<ServerPlayer> here,long now){
        int J=PellPeople.JUNE,T=PellPeople.TILLY;var tilly=PellPeople.find(l,b,T);if(tilly==null)return;
        // June looks at what she is doing; Tilly keeps still.
        h.moving[J]=true;h.moving[T]=true;june.setYHeadRot(PellPeople.yawTo(june,tilly.getEyePosition()));tilly.setYHeadRot(tilly.getYRot());
        boolean brushing=h.brushSaid<4,week=h.visit>=2;
        if(brushing&&(now-h.brushedAt)%30==0)l.playSound(null,june.getX(),june.getY()+1,june.getZ(),PellRegistry.BRUSH.get(),SoundSource.NEUTRAL,.6F,1F);
        if(brushing&&(now-h.brushedAt)%10==0)present(june,"SawHairBrushed",here,SEE_NEAR,b);
        if(!ready(h,now,june)||!ready(h,now,tilly))return;
        switch(h.brushSaid){
            case 0->say(h,june,J,PellTexts.JUNE_HOLD_STILL,now);
            // 0.4.533: a week in, the brushing is how it has been every night, and how somebody liked it.
            case 1->say(h,tilly,T,week?PellTexts.TILLY_SAME_WAY:PellTexts.TILLY_DAD,now);
            case 2->say(h,june,J,week?PellTexts.JUNE_HOW_SHE_LIKED:PellTexts.JUNE_EVERY_NIGHT,now);
            case 3->{if(now-h.brushedAt<BRUSH_TIME)return;say(h,june,J,week?PellTexts.JUNE_COMING_ON:PellTexts.JUNE_GROW_LONG,now);}
            case 4->{
                // June puts the brush away and says goodnight, and Tilly lies down. 0.4.533: a week in, goodnight to Nessa; Tilly says
                // who she is, still sitting up, and June says goodnight to her by her name before Tilly lies down.
                june.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);june.gesture(0);
                if(!week){tuck(l,b,h,tilly);say(h,june,J,PellTexts.JUNE_NIGHT_NIGHT,now);goodnight(l,b,h);return;}
                say(h,june,J,PellTexts.JUNE_NIGHT_NESSA,now);present(june,"HeardNessaAtBedtime",here);
            }
            case 5->{
                // 0.4.545: a reader as Tilly answers the name on the panel, or says nothing; June waits a while for it.
                if(h.tillyReader==null)say(h,tilly,T,PellTexts.TILLY_ITS_TILLY,now);
                else if(!PellTilly.asked(l,b,h,now,PellTexts.JUNE_NIGHT_NESSA,PellTexts.NIGHT_OWN,PellTexts.NIGHT_YES))return;
            }
            default->{tuck(l,b,h,tilly);say(h,june,J,h.tillyReader!=null&&h.tillySaidYes?PellTexts.JUNE_NIGHT_ANSWERED:PellTexts.JUNE_NIGHT_TILLY,now);goodnight(l,b,h);return;}
        }
        h.brushSaid++;
    }
    /** Tilly off the edge of the bed and lying down in it. */
    private static void tuck(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor tilly){
        // 0.4.545: a reader as Tilly is tucked in if they are by the bed (PellTilly.tuck).
        if(h.tillyReader!=null){PellTilly.tuck(l,b,h);return;}
        unperch(b,h,PellPeople.TILLY,tilly);lie(l,b,h,PellPeople.TILLY,tilly,TILLY_SLEEPS);
    }
    /** Goodnight said: June goes to send them out. */
    private static void goodnight(ServerLevel l,BlockPos b,PellPeople.Household h){up(l,b,h,PellPeople.JUNE,AT_LOCK[0].look(),NESSA_TO_SLIDER);h.step=TUCKED;}
    /**
     * Locking up. At each outside door in turn June waits until nobody is in the house (asking again now and then, from wherever she is),
     * and until nobody stands in the doorway; then she shuts it if it is open and locks it. After the last, her room.
     */
    static void locking(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,List<ServerPlayer> here,long now){
        int J=PellPeople.JUNE;boolean juneStill=h.way[J].isEmpty();
        if(h.lockIndex>=LOCKS.size()){if(juneStill)h.step=NIGHT;return;}
        var spot=AT_LOCK[h.lockIndex];
        if(!juneStill)return;
        if(!near(june,b,v(spot.x(),spot.z()),.6)){go(h,J,-1,spot.look(),TO_LOCK[h.lockIndex]);return;}
        if(anyoneInside(b,here)){
            h.rest[J]=0;
            if(now-h.nudgedAt>=NUDGE&&ready(h,now,june)){
                String line=PellTexts.JUNE_NUDGE[(int)((now/NUDGE)%PellTexts.JUNE_NUDGE.length)];
                say(h,june,J,line,now);far(june,"June, calling through the house: \u201c"+line+"\u201d",b,here);h.nudgedAt=now;
            }
            return;
        }
        var door=LOCKS.get(h.lockIndex);
        h.rest[J]=spot.look();
        if(!clearOf(b,door,here))return;
        if(PellSite.open(l,b,door))PellHouse.toggle(l,b,door);
        h.locked.add(door);
        var at=Vec3.atBottomCenterOf(b.offset(door)).add(0,1,0);l.playSound(null,at.x,at.y,at.z,PellRegistry.LOCK.get(),SoundSource.BLOCKS,.8F,1F);
        h.lockIndex++;h.step=LOCKING;
        if(h.lockIndex<LOCKS.size())go(h,J,-1,AT_LOCK[h.lockIndex].look(),TO_LOCK[h.lockIndex]);
        else{
            // Locked up, with every reader at the house outside it: their own record keeps that.
            for(var p:here){if(!p.isAlive()||p.isSpectator())continue;var own=PellHouse.own(p);if(!own.getBoolean("LockedOut")){own.putBoolean("LockedOut",true);PellHouse.save(p,own);}}
            go(h,J,-1,HER_ROOM.look(),TO_LOCK[LOCKS.size()]);
        }
    }

    /** The television on (its tape playing) or off (and its sound stopped for anyone near). */
    static void tv(ServerLevel l,BlockPos b,boolean on){
        var at=b.offset(TV);var s=l.getBlockState(at);
        if(PellThing.is(s,PellThing.Kind.TV_CABINET)&&s.getValue(PellThing.STAGE)!=(on?1:0))l.setBlock(at,s.setValue(PellThing.STAGE,on?1:0),3);
        if(on)return;
        var packet=new net.minecraft.network.protocol.game.ClientboundStopSoundPacket(PellRegistry.TAPE.getId(),SoundSource.NEUTRAL);
        for(var p:l.players())if(p.distanceToSqr(Vec3.atCenterOf(at))<48*48)p.connection.send(packet);
    }
    /** Into a seat away from the table, turned the way it faces; or, if a reader is in it, standing beside it facing the same way. */
    static void perch(ServerLevel l,BlockPos b,PellPeople.Household h,int who,LiteraryActor a,Perch p){
        if(readerIn(l,b,p.cell())){h.rest[who]=p.look();return;}
        var at=world(b,p.at());a.moveTo(at.x,at.y,at.z,p.look(),0);a.setYHeadRot(p.look());a.setYBodyRot(p.look());a.setDeltaMovement(Vec3.ZERO);
        h.perch[who]=p;h.rest[who]=p.look();
    }
    /** Up out of a seat away from the table, to stand beside it. */
    static void unperch(BlockPos b,PellPeople.Household h,int who,LiteraryActor a){
        var p=h.perch[who];if(p==null)return;
        var at=world(b,p.beside());a.moveTo(at.x,at.y,at.z,a.getYRot(),0);h.perch[who]=null;
    }
    /** Lying down to sleep along the bed at this place: Tilly's cane out of her hand. */
    static void lie(ServerLevel l,BlockPos b,PellPeople.Household h,int who,LiteraryActor a,Vec3 at){
        var w=world(b,at);a.moveTo(w.x,w.y,w.z,a.getYRot(),0);a.setDeltaMovement(Vec3.ZERO);
        a.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);a.gesture(PellPeople.ASLEEP);h.asleep[who]=true;
    }
    /** A walker opens the inside door they come to; June her own as well. */
    private static void through(ServerLevel l,BlockPos b,int who,LiteraryActor a){
        for(var door:INSIDE_DOORS)if(a.position().subtract(Vec3.atBottomCenterOf(b.offset(door))).horizontalDistanceSqr()<1.6*1.6&&!PellSite.open(l,b,door))PellHouse.toggle(l,b,door);
        if(who==PellPeople.JUNE&&a.position().subtract(Vec3.atBottomCenterOf(b.offset(PellSite.JUNE_DOOR))).horizontalDistanceSqr()<1.6*1.6&&!PellSite.open(l,b,PellSite.JUNE_DOOR))PellHouse.toggle(l,b,PellSite.JUNE_DOOR);
    }
    /** A reader is sitting in this cell. */
    static boolean readerIn(ServerLevel l,BlockPos b,BlockPos cell){return !l.getEntitiesOfClass(io.github.knaitoe.theoldesthouse.house.SeatEntity.class,new AABB(b.offset(cell))).isEmpty();}
    /** A reader is inside the house's walls. */
    static boolean anyoneInside(BlockPos b,List<ServerPlayer> here){
        for(var p:here)if(p.isAlive()&&!p.isSpectator()&&PellSite.inHouse(p.position().subtract(b.getX(),b.getY(),b.getZ())))return true;
        return false;
    }
    /** Nobody stands in this doorway. */
    static boolean clearOf(BlockPos b,BlockPos door,List<ServerPlayer> here){
        var at=Vec3.atBottomCenterOf(b.offset(door));
        for(var p:here)if(p.isAlive()&&!p.isSpectator()&&p.position().subtract(at).horizontalDistanceSqr()<1.1*1.1&&Math.abs(p.getY()-at.y)<2)return false;
        return true;
    }
    /** One of them within this distance of a place. */
    static boolean near(LiteraryActor a,BlockPos b,Vec3 r,double d){return a.position().subtract(b.getX()+r.x,a.getY(),b.getZ()+r.z).horizontalDistanceSqr()<d*d;}
    /** What June says, told in grey to readers in the house too far off to hear her. */
    static void far(LiteraryActor june,String line,BlockPos b,List<ServerPlayer> here){
        for(var p:here)if(p.isAlive()&&!p.isSpectator()&&p.distanceToSqr(june)>HEAR*HEAR&&PellSite.inHouse(p.position().subtract(b.getX(),b.getY(),b.getZ())))PellHouse.cue(p,line);
    }
    /** As present, within this distance and inside the house. */
    static void present(LiteraryActor at,String key,List<ServerPlayer> here,double reach,BlockPos b){
        for(var p:here){
            if(!p.isAlive()||p.isSpectator()||p.distanceToSqr(at)>reach*reach||!PellSite.inHouse(p.position().subtract(b.getX(),b.getY(),b.getZ())))continue;
            var own=PellHouse.own(p);if(own.getBoolean(key))continue;own.putBoolean(key,true);PellHouse.save(p,own);
        }
    }
    /** A hand on the television while the tape plays: what is on it, in order, for that reader. True if it answered. */
    public static boolean tvHand(ServerPlayer p,BlockPos b,CompoundTag own){
        if(!tvOn(p.serverLevel(),b))return false;
        int seen=own.getInt("Tape");PellHouse.cue(p,PellTexts.TV_TAPE[seen%PellTexts.TV_TAPE.length]);
        own.putInt("Tape",seen+1);own.putBoolean("SawTape",true);
        return true;
    }
}
