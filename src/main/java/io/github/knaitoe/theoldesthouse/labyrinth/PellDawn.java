package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.network.HouseFadePayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.541, the Pell house's dawn and its endings (docs/PELL_HOUSE_DAWN_0_4_541.md), from the proposal's "3. The last night" ("Dawn:
 * Gail's car at the curb, either way") and "Endings": "Success: both out, and the name kept ... Gail's car is at the curb. Callum is
 * sitting on the front step with his shoes on" and "Failure: Tilly answers ... the older sibling is in Gail's car at dawn, and Tilly
 * waves from Nessa's window and doesn't come."
 *
 * <ul>
 * <li><b>The rest of the night.</b> Once the rite is over, either way (Tilly out through the line, or Tilly answered and back in Nessa's
 * bed), the hour goes on to first light, faster than the night went (PellVisit.clock). The readers at the house when it ended are the
 * ones the morning is for.</li>
 * <li><b>First light.</b> Anybody asleep wakes. Gail's car comes up the street and pulls in at the curb; Gail gets out and comes to the
 * foot of the path.</li>
 * <li><b>Out.</b> Callum is on the front step with his shoes on, and Tilly is with Gail at the curb, each the first moment nobody sees
 * them. Gail says good morning, and Tilly answers to her own name. The account is ready for each reader the morning is for, under the
 * mat; its reading resolves the story, once, and leaves Tilly's spare cane tip with them.</li>
 * <li><b>Answered.</b> Tilly is at Nessa's window, waving, with June behind her, the first moment nobody sees them. Gail waits at the
 * foot of the path. Each reader the morning is for has had their ending, with nothing resolved; going to Gail (or a minute and a half
 * waiting) puts them in the car, a beat to look back at the window, and the car takes them down the street and sets them down at the
 * House's door. When they are all gone, Gail and the car go, the first moment nobody sees them.</li>
 * </ul>
 */
public final class PellDawn {
    private PellDawn(){}
    static final int J=PellPeople.JUNE,T=PellPeople.TILLY,C=PellPeople.CALLUM,G=PellPeople.GAIL;
    /** Where the morning has got to: not yet light; the car coming up the street; Gail at the curb; and, after Tilly answered, the car
     *  gone down the street with the readers in it. */
    public static final int NOT_YET=0,COMING=1,COME=2,GONE=3;
    /** How fast the hour goes from the rite's end to first light (the night itself goes at PellNight.NIGHT_PACE). */
    public static final int AFTER_PACE=8;
    private static Vec3 v(double x,double z){return new Vec3(x,0,z);}
    /** The car's way up the street and round the end of the circle to the curb, the way it faces on each leg, and its pace. */
    static final Vec3[] CAR_IN={v(2.5,-6),v(2.5,-30),v(.5,-40.5),PellVisit.CAR};
    static final float[] CAR_IN_YAW={180,180,135,PellVisit.CAR_YAW};
    static final double CAR_PACE=.3;
    /** Gail out of the car at its door on the road side, round its front and up onto the footpath at the foot of the path. */
    static final Vec3[] GAIL_UP={v(-2.5,-43.3),v(.6,-43.3),v(.6,-46.4)};
    static final float GAIL_LOOK=180;
    /** Tilly at Gail's side at the curb; Callum on the front step beside the mat, looking down the path. */
    public static final Vec3 TILLY_BY_GAIL=v(-.4,-46.6),CALLUM_STEP=v(1.5,-56.6);
    static final float STEP_LOOK=0,TILLY_LOOK=180;
    /** Nessa's window, from inside her room: Tilly at it, June behind her, both looking out. */
    public static final Vec3 TILLY_WINDOW=v(-10.5,-58.75),JUNE_WINDOW=v(-10.4,-59.75);
    static final float WINDOW_LOOK=0;
    /** After Tilly answered: how long Gail waits before the readers are put in the car anyway, and how near her counts as gone to her. */
    public static final int WAIT=1800;
    public static final double TO_GAIL=3;
    /** Where the car sets the readers down: before the House's door, facing it. */
    public static final Vec3 SET_DOWN=v(.5,-1.5);
    static final float SET_LOOK=0;
    /** In the car: the beat before the black (to look back at the window), and how long into the black the reader is moved. */
    public static final int LOOK_BACK=50;
    static final int FADE_IN=10,FADE_HOLD=40,FADE_OUT=20,MOVE_AT=15;

    /** Whether the rite is over on the last night, either way, and the night goes on to first light: Tilly out through the line, or Tilly
     *  answered and back in Nessa's bed (not while June is still walking her home, or the morning could come before they are in). */
    public static boolean after(PellPeople.Household h){return h.visit>=PellWeek.LAST&&(out(h)||answered(h));}
    static boolean out(PellPeople.Household h){return h.rite==PellRite.OUT;}
    static boolean answered(PellPeople.Household h){return h.rite==PellRite.KEPT;}
    /** Whether Gail is at the house again this morning (so her goodbye of the first visit is not played: PellVisit). */
    public static boolean morning(PellPeople.Household h){return h.dawn>=COME;}

    /** The morning undone, for the visit beginning again. */
    static void restart(PellPeople.Household h){
        h.dawn=NOT_YET;h.endTaken=false;h.atEnd.clear();h.inCar.clear();h.carLeg=0;h.comeAt=0;
        h.callumStepped=false;h.tillyWithGail=false;h.atWindow=false;h.gailSaid=0;h.leaveAt.clear();h.faded.clear();
    }

    /** Every tick on the last night, after the rite's own (PellNight). */
    static void tick(ServerLevel l,BlockPos b,PellPeople.Household h,List<ServerPlayer> here,long now){
        if(!after(h))return;
        // The readers at the house when the night's end came are the ones the morning is for.
        if(!h.endTaken){h.endTaken=true;for(var p:here)if(p.isAlive()&&!p.isSpectator())h.atEnd.add(p.getUUID());}
        switch(h.dawn){
            case NOT_YET->{if(h.time>=PellMorning.DAWN)firstLight(l,b,h,here);}
            case COMING->drive(l,b,h,now);
            case COME->{if(out(h))morningOut(l,b,h,here,now);else morningAnswered(l,b,h,here,now);}
            default->away(l,b,h);
        }
    }

    // ------------------------------------------------------------------------------------------------ first light and the car
    private static void firstLight(ServerLevel l,BlockPos b,PellPeople.Household h,List<ServerPlayer> here){
        var start=PellVisit.world(b,CAR_IN[0]);var cell=BlockPos.containing(start);
        if(!l.isLoaded(cell)||!l.areEntitiesLoaded(ChunkPos.asLong(cell)))return;
        PellMorning.wakeAll(l,b,h,here,PellTexts.WOKEN_AT_DAWN,"WokenAtDawn");
        var c=PellVisit.car(l,b);
        if(c==null){
            c=BikeNightRegistry.TOWN_CAR.get().create(l);if(c==null)return;
            c.place(start,CAR_IN_YAW[0]);c.kind(TownCar.BLUE);
            if(!l.addFreshEntity(c))return;
        }
        c.place(start,CAR_IN_YAW[0]);h.car=c.getUUID();h.carLeg=1;
        var curb=PellVisit.world(b,PellVisit.CAR);
        l.playSound(null,curb.x,curb.y+.8,curb.z,PellRegistry.CAR_COME.get(),SoundSource.NEUTRAL,1F,1F);
        h.dawn=COMING;
    }
    /** The car a step along its way up the street; at the curb, Gail gets out. */
    private static void drive(ServerLevel l,BlockPos b,PellPeople.Household h,long now){
        var c=PellVisit.car(l,b);
        if(c==null){h.dawn=NOT_YET;h.carLeg=0;return;}
        if(h.carLeg>=CAR_IN.length){arrived(l,b,h,c,now);return;}
        var to=PellVisit.world(b,CAR_IN[h.carLeg]);var at=c.position();
        double dx=to.x-at.x,dz=to.z-at.z,d=Math.sqrt(dx*dx+dz*dz);
        float yaw=Mth.approachDegrees(c.getYRot(),CAR_IN_YAW[h.carLeg],6);
        if(d<=CAR_PACE){c.place(to,yaw);h.carLeg++;}
        else c.place(at.add(dx/d*CAR_PACE,0,dz/d*CAR_PACE),yaw);
    }
    private static void arrived(ServerLevel l,BlockPos b,PellPeople.Household h,TownCar c,long now){
        c.place(PellVisit.world(b,PellVisit.CAR),PellVisit.CAR_YAW);
        var gail=PellPeople.find(l,b,G);
        if(gail==null)gail=PellPeople.make(l,b,G,GAIL_UP[0],PellVisit.CAR_YAW);
        if(gail==null)return;
        var door=PellVisit.world(b,GAIL_UP[0]);gail.moveTo(door.x,door.y,door.z,0,0);
        h.gailGone=false;h.rest[G]=GAIL_LOOK;
        PellVisit.go(h,G,-1,GAIL_LOOK,GAIL_UP[1],GAIL_UP[2]);
        h.dawn=COME;h.comeAt=now;
    }
    /** Whether Gail is at the foot of the path, done walking. */
    private static boolean gailUp(PellPeople.Household h){return h.way[G].isEmpty();}

    // ------------------------------------------------------------------------------------------------ out
    private static void morningOut(ServerLevel l,BlockPos b,PellPeople.Household h,List<ServerPlayer> here,long now){
        // Callum on the front step with his shoes on, the first moment nobody sees him or it.
        if(!h.callumStepped){
            var c=PellPeople.find(l,b,C);
            if(c!=null&&unseen(l,b,c,CALLUM_STEP)){
                h.callumStrip=false;h.way[C].clear();
                place(c,b,CALLUM_STEP,STEP_LOOK);c.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);c.setXRot(0);
                c.gesture(PellPeople.STEP);h.rest[C]=STEP_LOOK;h.callumStepped=true;
            }
        }
        // Tilly with Gail at the curb, the first moment nobody sees her or the place; if somebody has brought her there, she is there.
        var tilly=PellPeople.find(l,b,T);
        if(!h.tillyWithGail&&tilly!=null){
            var by=PellVisit.world(b,TILLY_BY_GAIL);boolean near=tilly.position().distanceToSqr(by)<=2.5*2.5;
            if(near||unseen(l,b,tilly,TILLY_BY_GAIL)){
                if(!near)place(tilly,b,TILLY_BY_GAIL,TILLY_LOOK);
                tilly.setNoGravity(false);tilly.noPhysics=false;tilly.gesture(PellPeople.LED);h.leader=null;h.rest[T]=TILLY_LOOK;h.tillyWithGail=true;
            }
        }
        var gail=PellPeople.find(l,b,G);
        if(gail!=null&&gailUp(h)&&tilly!=null){
            if(h.gailSaid==0&&PellVisit.ready(h,now,gail)){PellVisit.say(h,gail,G,PellTexts.GAIL_DAWN,now);h.gailSaid=1;}
            else if(h.gailSaid==1&&h.tillyWithGail&&PellVisit.ready(h,now,gail)){PellVisit.say(h,gail,G,PellTexts.GAIL_TO_TILLY,now);h.gailSaid=2;}
            else if(h.gailSaid==2&&PellVisit.ready(h,now,tilly)){PellVisit.say(h,tilly,T,PellTexts.TILLY_TO_GAIL,now);h.gailSaid=3;}
        }
        // The account, for each reader the morning is for.
        for(var p:here){
            if(!p.isAlive()||p.isSpectator()||!h.atEnd.contains(p.getUUID()))continue;
            var own=PellHouse.own(p);if(own.getBoolean("Dawned"))continue;
            own.putBoolean("Dawned",true);own.putBoolean("SawCarCome",true);
            // 0.4.542: their ending has landed, read or not: the house is never dealt to them again as it was, and the door they came
            // in by is only a passage (LabyrinthDealer.finishedFor). The account waits under the mat, here and in the house's
            // aftermath (PellAftermath); a new visit counts from here.
            own.putBoolean("Ended",true);own.putInt("Beat",Math.max(own.getInt("Beat"),own.getInt("Visit")));
            String outcome=own.getBoolean("GotTillyOut")?"brought_tilly_out_with_her_name":own.getBoolean("SawTillyOut")?"saw_tilly_come_out_with_her_name":"there_when_tilly_came_out";
            LiteraryVignettes.ready(p,PellHouse.PLACE,own,outcome);PellHouse.save(p,own);
            PellHouse.cue(p,PellTexts.DAWN_OUT);
        }
    }

    // ------------------------------------------------------------------------------------------------ answered
    private static void morningAnswered(ServerLevel l,BlockPos b,PellPeople.Household h,List<ServerPlayer> here,long now){
        // Tilly at Nessa's window and June behind her, once June has her home, the first moment nobody sees them or it.
        var tilly=PellPeople.find(l,b,T);var june=PellPeople.find(l,b,J);
        if(!h.atWindow&&h.rite==PellRite.KEPT&&tilly!=null&&june!=null&&unseen(l,b,tilly,TILLY_WINDOW)&&unseen(l,b,june,JUNE_WINDOW)){
            h.perch[T]=null;h.perch[J]=null;h.asleep[T]=false;h.way[T].clear();h.way[J].clear();
            place(tilly,b,TILLY_WINDOW,WINDOW_LOOK);tilly.gesture(PellPeople.WAVE);h.rest[T]=WINDOW_LOOK;
            place(june,b,JUNE_WINDOW,WINDOW_LOOK);june.gesture(PellPeople.ROBE);h.rest[J]=WINDOW_LOOK;
            h.atWindow=true;
        }
        var gail=PellPeople.find(l,b,G);
        if(gail!=null&&gailUp(h)&&h.gailSaid==0&&PellVisit.ready(h,now,gail)){PellVisit.say(h,gail,G,PellTexts.GAIL_COME_ON,now);h.gailSaid=1;}
        boolean waited=now-h.comeAt>=WAIT;
        for(var p:here){
            if(!p.isAlive()||p.isSpectator()||!h.atEnd.contains(p.getUUID()))continue;
            var own=PellHouse.own(p);
            if(!own.getBoolean("Ended")){
                // Their ending: permanent, nothing resolved (PellHouse.dealable).
                own.putBoolean("Ended",true);own.putBoolean("TillyStayed",true);own.putBoolean("SawCarCome",true);own.putString("Outcome","tilly_answered");
                own.putInt("Beat",Math.max(own.getInt("Beat"),own.getInt("Visit")));
                // 0.4.543: a night slept in the garage, the last one too, has its entry (PellEntries); after a success, nothing new comes.
                if((own.getInt("GarageNights")&1<<h.visit)!=0)PellEntries.due(own,PellEntries.night(h.visit));
                PellHouse.save(p,own);
                PellHouse.cue(p,PellTexts.DAWN_ANSWERED);
            }
            if(h.inCar.contains(p.getUUID()))continue;
            boolean toGail=gail!=null&&gailUp(h)&&horizontal(p.position(),gail.position())<=TO_GAIL;
            if(!toGail&&!waited)continue;
            // Into the car: a beat to look back, then the black, and the street.
            h.inCar.add(p.getUUID());h.leaveAt.put(p.getUUID(),now+LOOK_BACK);
            PellHouse.cue(p,!toGail?PellTexts.CAR_HORN:h.atWindow?PellTexts.TILLY_WAVES:PellTexts.IN_CAR);
            if(h.atWindow&&!own.getBoolean("SawTillyWave")){own.putBoolean("SawTillyWave",true);PellHouse.save(p,own);}
        }
        leaving(l,b,h,now);
        // Everybody it was for gone down the street (or gone from the house): Gail and the car go.
        if(!h.inCar.isEmpty()&&h.leaveAt.isEmpty()&&nobodyDue(h,here))h.dawn=GONE;
    }
    /** Whether every reader the morning is for who is still here has gone to the car. */
    private static boolean nobodyDue(PellPeople.Household h,List<ServerPlayer> here){
        for(var p:here)if(p.isAlive()&&!p.isSpectator()&&h.atEnd.contains(p.getUUID())&&!h.inCar.contains(p.getUUID()))return false;
        return true;
    }
    /** Each reader in the car: the black after their beat, and in it the car down the street and the reader out at the House's door. */
    private static void leaving(ServerLevel l,BlockPos b,PellPeople.Household h,long now){
        for(var it=h.leaveAt.entrySet().iterator();it.hasNext();){
            var e=it.next();var id=e.getKey();var p=l.getServer().getPlayerList().getPlayer(id);
            if(p==null||p.level()!=l||!p.isAlive()){it.remove();h.faded.remove(id);continue;}
            long at=e.getValue();
            if(now>=at&&h.faded.add(id))HousePackets.send(p,new HouseFadePayload(FADE_IN,FADE_HOLD,FADE_OUT));
            if(now<at+MOVE_AT)continue;
            var to=PellVisit.world(b,SET_DOWN);
            p.teleportTo(l,to.x,to.y,to.z,SET_LOOK,0);p.setDeltaMovement(Vec3.ZERO);
            PellHouse.cue(p,PellTexts.SET_DOWN);
            var own=PellHouse.own(p);own.putBoolean("SetDown",true);PellHouse.save(p,own);
            it.remove();h.faded.remove(id);
        }
    }
    /** After Tilly answered and everybody it was for is gone: Gail gets in, and the car goes, each the first moment nobody sees it. */
    private static void away(ServerLevel l,BlockPos b,PellPeople.Household h){
        var gail=PellPeople.find(l,b,G);
        if(gail!=null&&!LiteraryVignettes.watched(l,gail.getEyePosition())){gail.discard();gail=null;}
        if(gail==null)h.gailGone=true;
        var c=PellVisit.car(l,b);if(c==null)return;
        var curb=PellVisit.world(b,PellVisit.CAR);
        if(gail!=null||LiteraryVignettes.watched(l,curb.add(0,.8,0))){c.place(curb,PellVisit.CAR_YAW);return;}
        l.playSound(null,curb.x,curb.y+.8,curb.z,PellRegistry.CAR_AWAY.get(),SoundSource.NEUTRAL,1F,1F);
        c.discard();h.car=null;
    }

    // ------------------------------------------------------------------------------------------------ a hand on them in the morning
    /** A hand on one of the people once Gail is back: true if the morning answers it (PellPeople.talk). */
    static boolean talk(ServerPlayer p,BlockPos b,PellPeople.Household h,LiteraryActor a,int who){
        if(!morning(h))return false;
        if(who==C&&h.callumStepped){PellHouse.cue(p,PellTexts.CALLUM_STEP_HAND[h.said[C]++%PellTexts.CALLUM_STEP_HAND.length]);return true;}
        String[] lines=null;
        if(who==T&&h.tillyWithGail)lines=PellTexts.TILLY_DAWN_TALK;
        else if(who==T&&h.atWindow)lines=PellTexts.TILLY_KEPT_TALK;
        else if(who==J&&h.atWindow)lines=PellTexts.JUNE_KEPT_TALK;
        else if(who==G)lines=out(h)?PellTexts.GAIL_DAWN_TALK:PellTexts.GAIL_WAIT_TALK;
        if(lines==null)return false;
        if(a.saying())return true;
        String line=lines[h.said[who]++%lines.length];
        a.say(PellPeople.name(who),line);a.speaks(line);
        return true;
    }

    // ------------------------------------------------------------------------------------------------ helpers
    /** Whether nobody sees an actor where it is, nor the place it is about to be (at standing eye height). */
    private static boolean unseen(ServerLevel l,BlockPos b,LiteraryActor a,Vec3 to){
        var there=PellVisit.world(b,to).add(0,a.getEyeHeight(),0);
        return !LiteraryVignettes.watched(l,a.getEyePosition())&&!LiteraryVignettes.watched(l,there);
    }
    private static void place(LiteraryActor a,BlockPos b,Vec3 at,float look){
        var w=PellVisit.world(b,at);a.moveTo(w.x,w.y,w.z,look,0);a.setYHeadRot(look);a.setYBodyRot(look);a.setDeltaMovement(Vec3.ZERO);
    }
    private static double horizontal(Vec3 a,Vec3 c){double dx=a.x-c.x,dz=a.z-c.z;return Math.sqrt(dx*dx+dz*dz);}

    // ------------------------------------------------------------------------------------------------ for tests
    /** For tests: where the morning has got to. */
    public static int dawn(BlockPos b){return PellPeople.household(b).dawn;}
    /** For tests: the night brought on to just short of first light. */
    public static void nearlyLight(BlockPos b){var h=PellPeople.household(b);h.time=Math.max(h.time,PellMorning.DAWN-8);}
    /** For tests: how far through her words at the curb Gail is (out: three, with Tilly's answer; answered: one). */
    public static int gailSaid(BlockPos b){return PellPeople.household(b).gailSaid;}
    /** For tests: Gail has waited as long as she waits. */
    public static void waitedOut(BlockPos b){var h=PellPeople.household(b);h.comeAt-=WAIT;}
    /** For tests: whether a reader is in Gail's car (or has been set down from it). */
    public static boolean inCar(BlockPos b,java.util.UUID id){return PellPeople.household(b).inCar.contains(id);}
    /** For tests: whether Tilly is at Nessa's window, and Callum on the step. */
    public static boolean atWindow(BlockPos b){return PellPeople.household(b).atWindow;}
    public static boolean callumStepped(BlockPos b){return PellPeople.household(b).callumStepped;}
    public static boolean tillyWithGail(BlockPos b){return PellPeople.household(b).tillyWithGail;}
    public static @Nullable TownCar car(ServerLevel l,BlockPos b){return PellVisit.car(l,b);}
}
