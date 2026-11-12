package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ChunkPos;
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
    static final double JUNE_PACE=.1,GAIL_PACE=.11,TILLY_PACE=.12,BEHIND=1.6;

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
        if(!h.gailGone)car(l,b,h);
        if(gail!=null&&h.step>=FIRST_RULE)leave(l,b,h,gail);
        if(tilly!=null)tilly(b,h,june,tilly);
        switch(h.step){
            case GREET->{if(gail!=null&&!gail.saying()&&arrived(b,june,gail,here)){say(h,gail,PellPeople.GAIL,PellTexts.GAIL_GREETS,now);h.step=JUNE_HELLO;}}
            case JUNE_HELLO->{if(ready(h,now,june)){say(h,june,PellPeople.JUNE,PellTexts.JUNE_GREETS,now);h.step=GAIL_HANDOVER;}}
            case GAIL_HANDOVER->{if(gail==null)h.step=FIRST_RULE;else if(ready(h,now,gail)){say(h,gail,PellPeople.GAIL,PellTexts.GAIL_READY,now);h.step=GAIL_GOODBYE;}}
            case GAIL_GOODBYE->{if(gail==null)h.step=FIRST_RULE;else if(ready(h,now,gail)){say(h,gail,PellPeople.GAIL,PellTexts.GAIL_GOES,now);h.step=FIRST_RULE;}}
            case FIRST_RULE->{if(ready(h,now,june)){say(h,june,PellPeople.JUNE,PellTexts.JUNE_LINE,now);heard(june,PellTexts.RULE_LINE,here);h.step=TOUR;h.waypoint=0;}}
            default->{if(h.step>=TOUR&&h.step<SETTLED)stop(l,b,h,june,STOPS[h.step-TOUR],here,now);}
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
}
