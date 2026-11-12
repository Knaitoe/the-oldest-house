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
    private static final Vec3[] TILLY_IN={v(-9.5,-64.6),v(-9.5,-62.8),v(-10.5,-61.5)};
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
            if(june==null||tilly==null||gail==null||PellPeople.find(l,b,PellPeople.CALLUM)==null)return;
            restart(l,b,h);
        }
        if(june==null)return;
        long now=l.getGameTime();
        clock(h,here,now);
        if(!h.gailGone)car(l,b,h);
        if(gail!=null&&h.step>=FIRST_RULE)leave(l,b,h,gail);
        if(tilly!=null&&h.step<CALLED)tilly(b,h,june,tilly);
        walkers(l,b,h);
        switch(h.step){
            case GREET->{if(gail!=null&&!gail.saying()&&arrived(b,june,gail,here)){say(h,gail,PellPeople.GAIL,PellTexts.GAIL_GREETS,now);h.step=JUNE_HELLO;}}
            case JUNE_HELLO->{if(ready(h,now,june)){say(h,june,PellPeople.JUNE,PellTexts.JUNE_GREETS,now);h.step=GAIL_HANDOVER;}}
            case GAIL_HANDOVER->{if(gail==null)h.step=FIRST_RULE;else if(ready(h,now,gail)){say(h,gail,PellPeople.GAIL,PellTexts.GAIL_READY,now);h.step=GAIL_GOODBYE;}}
            case GAIL_GOODBYE->{if(gail==null)h.step=FIRST_RULE;else if(ready(h,now,gail)){say(h,gail,PellPeople.GAIL,PellTexts.GAIL_GOES,now);h.step=FIRST_RULE;}}
            case FIRST_RULE->{if(ready(h,now,june)){say(h,june,PellPeople.JUNE,PellTexts.JUNE_LINE,now);heard(june,PellTexts.RULE_LINE,here);h.step=TOUR;h.waypoint=0;}}
            default->{if(h.step>=TOUR&&h.step<SETTLED)stop(l,b,h,june,STOPS[h.step-TOUR],here,now);else if(h.step>=SETTLED)evening(l,b,h,june,here,now);}
        }
    }

    /** Everyone back where the first visit puts them, June's doors shut, and the day from its start. */
    static void restart(ServerLevel l,BlockPos b,PellPeople.Household h){
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
    }

    /** Somebody has come: up to Gail on the path, or past her to June at the step, or into the house. */
    private static boolean arrived(BlockPos b,LiteraryActor june,LiteraryActor gail,List<ServerPlayer> here){
        for(var p:here){
            if(!p.isAlive()||p.isSpectator())continue;
            if(p.distanceToSqr(gail)<GREET_NEAR*GREET_NEAR||p.distanceToSqr(june)<JUNE_NEAR*JUNE_NEAR||PellSite.inHouse(p.position().subtract(b.getX(),b.getY(),b.getZ())))return true;
        }
        return false;
    }
    private static void say(PellPeople.Household h,LiteraryActor a,int who,String line,long now){a.say(PellPeople.name(who),line);a.speaks(line);h.next=now+Dialogue.ticks(line)+GAP;}
    /** What was last said has had its time, and whoever speaks next is not in the middle of something. */
    private static boolean ready(PellPeople.Household h,long now,LiteraryActor next){return now>=h.next&&!next.saying();}
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
    /** A plate: not there, laid, served, scraped. Who a place is for besides the house's people: a reader, or nobody. */
    public static final int NONE=0,LAID_PLATE=1,SERVED=2,SCRAPED=3,GUEST=10,NOBODY=-1;
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
    /** Tilly's way from Nessa's room to the kitchen door, and Callum's from the end of the hall; and back. */
    private static final Vec3[] TILLY_TO_KITCHEN={v(-9.5,-62.8),v(-9.5,-64.6),v(-9.5,-65.6),v(-9.5,-66.6)},
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
    private static void stand(ServerLevel l,BlockPos b,PellPeople.Household h,int who,PellPeople.Spot s){
        var a=PellPeople.find(l,b,who);if(a==null)return;
        a.moveTo(b.getX()+s.x(),b.getY(),b.getZ()+s.z(),s.look(),0);a.setYHeadRot(s.look());a.setYBodyRot(s.look());a.setDeltaMovement(Vec3.ZERO);
        h.rest[who]=s.look();h.seated[who]=false;h.place[who]=-1;h.way[who].clear();h.sitAt[who]=-1;
    }

    /** The house's hour: held through the tour and through dinner, going on to six once the day is settled and on into the evening
     *  after; sent to every reader at the house when it starts or stops going on, when they first come, and once a second. */
    static void clock(PellPeople.Household h,List<ServerPlayer> here,long now){
        long to=h.step==SETTLED?SIX:h.step>=CLEARED?EVENING:h.time;
        if(h.time<to)h.time++;
        int rate=h.time<to?1:0;boolean changed=rate!=h.rate;h.rate=rate;
        var payload=new io.github.knaitoe.theoldesthouse.network.PellPayload(h.time,h.rate);
        for(var p:here)if(changed||now%20==0||!h.told.contains(p.getUUID())){h.told.add(p.getUUID());io.github.knaitoe.theoldesthouse.network.HousePackets.send(p,payload);}
    }

    /** Sends one of them along a way, to sit at a place at the end of it, or (with none) to stand there facing this way. */
    static void go(PellPeople.Household h,int who,int place,float look,Vec3... way){h.way[who].clear();h.way[who].addAll(Arrays.asList(way));h.sitAt[who]=place;h.endLook[who]=look;h.stuckFor[who]=0;}
    static double pace(int who){return switch(who){case PellPeople.JUNE->JUNE_PACE;case PellPeople.TILLY->TILLY_PACE;case PellPeople.GAIL->GAIL_PACE;default->CALLUM_PACE;};}
    /** Each of them walking somewhere goes a step along their way; at its end they sit, or stand as they were sent to. Somebody who
     *  stands unable to get nearer for a while, unseen, is put at the next point. */
    static void walkers(ServerLevel l,BlockPos b,PellPeople.Household h){
        for(int who=0;who<PellPeople.COUNT;who++){
            if(h.way[who].isEmpty())continue;var a=PellPeople.find(l,b,who);if(a==null)continue;
            var to=world(b,h.way[who].peekFirst());var before=a.position();
            boolean there=SceneFootpaths.walk(a,to,pace(who));h.moving[who]=true;
            if(there){h.way[who].pollFirst();h.stuckFor[who]=0;}
            else if(a.position().distanceToSqr(before)>1e-4)h.stuckFor[who]=0;
            else if(++h.stuckFor[who]>STUCK&&!LiteraryVignettes.watched(l,a.getEyePosition())){a.moveTo(to.x,to.y,to.z,a.getYRot(),0);h.way[who].pollFirst();h.stuckFor[who]=0;}
            if(!h.way[who].isEmpty())continue;
            if(h.sitAt[who]>=0)sit(l,b,h,who,a,h.sitAt[who]);else h.rest[who]=h.endLook[who];
            h.sitAt[who]=-1;
        }
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
        int J=PellPeople.JUNE,T=PellPeople.TILLY,C=PellPeople.CALLUM;
        boolean juneStill=h.way[J].isEmpty();
        // By how far past the settled day the visit is (the steps follow the tour, whose length is the stops'): settled, called,
        // laid, serving, eating, clearing, cleared.
        switch(h.step-SETTLED){
            case 0->{if(h.time>=SIX&&ready(h,now,june))call(l,b,h,june,here,now);}
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
                    say(h,june,J,tillys?PellTexts.JUNE_SERVES_TILLY:PellTexts.JUNE_SERVES,now);
                    if(tillys)present(june,"SawTillyServed",here);
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
                if(t>=0&&h.plate[t]==SERVED&&now-h.servedAt>=TILLY_EATS)plate(l,b,h,t,SCRAPED);
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
                // At the sink a while, then back to her stove; the evening goes on to half past eight (clock).
                if(juneStill&&!june.getMainHandItem().isEmpty()&&there(june,b,SINK)&&now-h.clearedAt>=WASHING){
                    june.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);go(h,J,-1,KITCHEN.look(),v(KITCHEN.x(),KITCHEN.z()));
                }
            }
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
        var item=new ItemStack(switch(state){case SERVED->PellRegistry.DINNER.get();case SCRAPED->PellRegistry.SCRAPED.get();default->PellRegistry.PLATE.get();});
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
        if(diner==PellPeople.TILLY){PellHouse.cue(p,state==SCRAPED?PellTexts.TILLYS_EATEN:state==SERVED?PellTexts.TILLYS_PLATE:PellTexts.PLATE_EMPTY);return true;}
        if(diner==PellPeople.CALLUM){PellHouse.cue(p,PellTexts.CALLUMS_PLATE);return true;}
        if(diner!=GUEST){PellHouse.cue(p,h.step>=SERVING?PellTexts.NOBODY_PLACE:PellTexts.PLATE_EMPTY);return true;}
        if(state==LAID_PLATE){PellHouse.cue(p,PellTexts.PLATE_EMPTY);return true;}
        if(state==SCRAPED){PellHouse.cue(p,PellTexts.SCRAPED_PLATE);return true;}
        p.getFoodData().eat(6,.6F);
        p.serverLevel().playSound(null,p.getX(),p.getY(),p.getZ(),SoundEvents.GENERIC_EAT,SoundSource.PLAYERS,.8F,1F);
        plate(p.serverLevel(),b,h,best,SCRAPED);own.putBoolean("Ate",true);PellHouse.cue(p,PellTexts.ATE);
        return true;
    }
}
