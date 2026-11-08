package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.labyrinth.CrossingBlock.Kind;
import io.github.knaitoe.theoldesthouse.labyrinth.LastCrossing.Run;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.355, The Last Crossing, Day Three (docs/LAST_CROSSING_0_4_355.md), from the owner's design: "Vera checks the depth against a mark on the
 * ferry sill. On the next rise the boat can clear it. She takes roll again and asks who is working the launch, who is looking for the
 * source, and who is still below. ... The crew can prepare either plan or divide its time between them."
 *
 * <p>The third morning: the night's rise jumped the cable off the last roller, and a pull that failed threw two more, each of which takes
 * three heaves to reset; Vera had the launch pin in overnight to dress it and has left it on the porch rack. She goes down to the ramp and
 * reads the depth against the sill, takes the roll there in the order she took it on the first day (the dead are called too, and nobody
 * answers for them; a reader below ground or shut in doesn't hear their name), calls Ellis, and a breath later something answers up the
 * speaking pipes. Then she asks who's on the launch, who's after the source and who's still below, and each reader who heard her can say.
 *
 * <p>The launch: every roller seated, the pin carried down from the porch and set in its sleeve, the sluice wheel tried through its travel,
 * one reader taking the launch rope at its bollard and one the pin. The source: the live feed traced from its tag by the gauge down past
 * the intake, where it goes into the wall beside a rusted iron door that gives once the feed is traced to it; the stair behind the door
 * down to the old lower gallery, where the feed runs on along the roof into the feed gate, with the flooded chamber and the source behind
 * thick glass; the locking bar carried down from its rack in the intake and staged at the gate; the two heaps of fallen debris in the
 * passage south cleared to the upstream cutoff's alcove. The master bypass by the sluice wheel is rusted fast by day; a reader the plate
 * has exposed knows it will turn at high water, and once exposed, the source offers them the Other Breath under their own. Vera says where
 * things stand before dark, and asks whoever has the plate to decide where it stands tonight. At the end of the day the third dusk falls:
 * Night Three is LastCrossingNightThree (0.4.356).
 */
public final class LastCrossingDayThree {
    private LastCrossingDayThree(){}
    /** Ticks: the morning before Day Three starts; how long Vera reads the depth before the roll; how long she waits for an answer, after a
     * name said again, and after Ellis's; how long the answer comes up the pipe after his name; how long the question stays open; how long
     * after Day Three starts an exposed carrier hears the offer; the least time between shoves, heaves and clearing strokes. How many shoves
     * open the rusted door, heaves reset a thrown roller, and strokes clear a heap of debris; how far the pipes' answer carries. */
    public static final int MORNING=300,DEPTH_READ=60,ANSWER=100,AGAIN=60,ELLIS_WAIT=50,PIPE_AFTER=40,QUESTION=300,OFFER_AFTER=400,STROKE=10,
        DOOR_SHOVES=3,ROLLER_HEAVES=3,DEBRIS_STROKES=4;
    static final double EARSHOT=48,PIPE_EAR=6;
    /** Which of the feed's tags have been traced: the gauge room's, the intake's, the gallery's. */
    public static final int FEED_GAUGE=1,FEED_INTAKE=2,FEED_GALLERY=4,FEED_ALL=7;

    static final class Day {
        int morning;boolean started;int vera;long veraAt=-1;
        /** The roll at the ramp: which name, how long, called twice, who answered, who didn't. */
        int calling=-1,callTicks;boolean calledTwice;long answerAt=-1;final Set<UUID> answered=new HashSet<>();final List<String> absent=new ArrayList<>();
        /** What each reader said they'd work: launch, source or either. */
        final Map<UUID,String> plans=new LinkedHashMap<>();
        /** The launch: rollers thrown by a failed pull and the heaves on each; the pin taken, staged, and whose; the wheel tried; the rope's. */
        final Set<BlockPos> thrown=new HashSet<>();final Map<BlockPos,Integer> heaves=new HashMap<>();
        boolean pinTaken,pinStaged,wheelTested,launchReady;@Nullable UUID pinBy,ropeBy;
        /** The source: the feed's tags traced, the door's shoves and open, the bar taken and staged, the debris strokes, the path, ready. */
        int feed,shoves;boolean doorOpen,barTaken,barStaged,pathCleared,galleryReady;final Map<BlockPos,Integer> strokes=new HashMap<>();
        final Map<BlockPos,Long> lastStroke=new HashMap<>();final LinkedHashSet<UUID> feedHands=new LinkedHashSet<>(),pathHands=new LinkedHashSet<>();
        boolean briefed;final Set<UUID> offered=new HashSet<>();
    }
    static boolean active(Run r){return r.phase==LastCrossing.DAY&&r.dayNo>=3;}
    public static int feed(BlockPos b){var r=LastCrossing.run(b);return r==null?0:r.dayThree.feed;}
    public static int vera(BlockPos b){var r=LastCrossing.run(b);return r==null?-1:r.dayThree.vera;}
    @Nullable public static String plan(BlockPos b,UUID id){var r=LastCrossing.run(b);return r==null?null:r.dayThree.plans.get(id);}

    // ------------------------------------------------------------------------------------------------ the morning
    /** The third morning, after Night Two's dawn: Vera back on her porch, then the depth. */
    static void morning(ServerLevel l,Run r,List<ServerPlayer> here){
        var d=r.dayThree;d.morning++;
        if(d.morning>=MORNING&&!LastCrossing.walking(r))start(l,r,here);
    }
    static void start(ServerLevel l,Run r,List<ServerPlayer> here){
        var d=r.dayThree;var b=r.base;r.phase=LastCrossing.DAY;r.dayNo=3;r.day=0;d.started=true;
        // The rise jumped the cable off the last roller; a pull that failed threw two more, which take more than a shove to reset.
        if(r.night2.threw)for(var at:List.of(LastCrossingSite.LOOSE_ROLLER,LastCrossingSite.ROLLERS.get(2))){var s=l.getBlockState(b.offset(at));if(CrossingBlock.is(s,Kind.ROLLER)&&s.getValue(CrossingBlock.STAGE)==1)d.thrown.add(at);}
        l.setBlock(b.offset(LastCrossingSite.ROLLERS.get(3)),LastCrossingSite.fit(Kind.ROLLER,Direction.NORTH,1),Block.UPDATE_CLIENTS);
        boolean seated=true;for(var at:LastCrossingSite.ROLLERS)if(CrossingBlock.is(l.getBlockState(b.offset(at)),Kind.ROLLER)&&l.getBlockState(b.offset(at)).getValue(CrossingBlock.STAGE)==1)seated=false;
        r.rollerSeated=seated;
        // Vera had the pin in overnight to dress it: the sleeve is empty and the pin hangs on the porch rack.
        r.pinSeated=false;r.wheelBy=null;
        l.setBlock(b.offset(LastCrossingSite.PIN),LastCrossingSite.fit(Kind.PIN,Direction.SOUTH,2),Block.UPDATE_CLIENTS);
        l.setBlock(b.offset(LastCrossingSite.WHEEL),LastCrossingSite.fit(Kind.WHEEL,Direction.SOUTH),Block.UPDATE_CLIENTS);
        l.setBlock(b.offset(LastCrossingSite.PIN_RACK),LastCrossingSite.fit(Kind.RACK,Direction.SOUTH,1),Block.UPDATE_CLIENTS);
        LastCrossing.speak(l,r,LastCrossingTexts.DEPTH_FIRST,64);
        LastCrossing.route(r,new Vec3(13.5,0,-35.5),new Vec3(26.5,0,-35.5),new Vec3(26.5,0,-50.5),LastCrossingSite.VERA_RAMP);d.vera=1;
    }

    // ------------------------------------------------------------------------------------------------ the day
    static void tick(ServerLevel l,Run r,List<ServerPlayer> here){
        var d=r.dayThree;
        r.day++;
        vera(l,r,here);
        LastCrossingDay.calls(l,r,here);
        LastCrossingNight.carrierBeat(l,r,r.night,here);
        LastCrossingNightTwo.secondBeat(l,r,r.night2,here);
        if(r.tick%10==0)for(var p:here)seen(l,r,p);
        offers(l,r,here);
        launch(l,r,here);
        gallery(l,r,here);
        if(!d.briefed&&r.day>=LastCrossing.DAY_TICKS*5/6){d.briefed=true;LastCrossing.speak(l,r,LastCrossingTexts.DAY3_BRIEF,64);
            for(var p:here)if(r.crew.containsKey(p.getUUID()))LastCrossing.mark(p,"HeardBrief");}
        // The third dusk: Night Three (LastCrossingNightThree, 0.4.356).
        if(r.day>=LastCrossing.DAY_TICKS)LastCrossingNightThree.begin(l,r,here);
    }
    /** Vera at the ramp: the depth, the roll in the first day's order, Ellis, and the question; then back to her porch. */
    static void vera(ServerLevel l,Run r,List<ServerPlayer> here){
        var d=r.dayThree;
        switch(d.vera){
            case 1->{if(LastCrossing.walking(r))return;d.vera=2;d.veraAt=r.tick;LastCrossing.speak(l,r,LastCrossingTexts.DEPTH_READ,24);
                var a=LastCrossing.actor(l,r.vera);var from=a==null?LastCrossing.world(r.base,LastCrossingSite.VERA_RAMP):a.position();
                for(var p:here)if(p.position().distanceToSqr(from)<16*16&&!p.isSpectator())LastCrossing.mark(p,"HeardDepth");}
            case 2->{if(r.tick-d.veraAt<DEPTH_READ)return;d.vera=3;d.calling=-1;d.callTicks=0;LastCrossing.speak(l,r,LastCrossingTexts.DAY3_ROLL,EARSHOT);}
            case 3->roll(l,r,here);
            case 4->{if(r.tick-d.veraAt<PIPE_AFTER)return;d.vera=5;d.veraAt=r.tick;ellisAnswers(l,r,here);}
            case 5->{if(r.tick-d.veraAt<ELLIS_WAIT)return;d.vera=6;d.veraAt=r.tick;question(l,r,here);}
            case 6->{if(r.tick-d.veraAt<QUESTION)return;d.vera=7;
                for(var p:here)if(LastCrossing.TALK.equals(Talk.key(p))&&!d.plans.containsKey(p.getUUID()))Talk.close(p);
                LastCrossing.route(r,new Vec3(26.5,0,-50.5),new Vec3(26.5,0,-35.5),new Vec3(13.5,0,-35.5),LastCrossingSite.VERA_PORCH);}
            default->{}
        }
    }
    /** The names on the roll in the order they signed, the dead among them: she calls them all. */
    static void roll(ServerLevel l,Run r,List<ServerPlayer> here){
        var d=r.dayThree;d.callTicks++;var names=new ArrayList<>(r.crew.entrySet());
        if(d.calling<0){if(d.callTicks>=30)next(l,r);return;}
        if(d.calling<names.size()){
            var e=names.get(d.calling);var id=e.getKey();
            if(d.answered.contains(id)){if(r.tick-d.answerAt>=20)next(l,r);}
            else if(r.dead.contains(id)){if(d.callTicks>=ANSWER){d.absent.add(e.getValue());next(l,r);}}
            else if(!d.calledTwice&&d.callTicks>=ANSWER){d.calledTwice=true;d.callTicks=0;LastCrossing.speak(l,r,e.getValue()+"?",EARSHOT);offer(l,r,id,e.getValue()+"?");}
            else if(d.calledTwice&&d.callTicks>=AGAIN){
                var p=LastCrossing.player(l,id);if(p!=null){LastCrossing.mark(p,"AbsentDayRoll");if(LastCrossing.TALK.equals(Talk.key(p)))Talk.close(p);}
                d.absent.add(e.getValue());next(l,r);}
            return;}
        d.vera=4;d.veraAt=r.tick;LastCrossing.speak(l,r,LastCrossingTexts.ELLIS,EARSHOT);
    }
    static void next(ServerLevel l,Run r){
        var d=r.dayThree;d.calling++;d.callTicks=0;d.calledTwice=false;var names=new ArrayList<>(r.crew.entrySet());
        if(d.calling<names.size()){var e=names.get(d.calling);LastCrossing.speak(l,r,e.getValue()+".",EARSHOT);if(!r.dead.contains(e.getKey()))offer(l,r,e.getKey(),e.getValue()+".");}
    }
    static void offer(ServerLevel l,Run r,UUID id,String called){
        var p=LastCrossing.player(l,id);if(p==null||!LastCrossing.readers(l,r.base).contains(p)||!LastCrossingNightTwo.hears(l,r,p))return;
        Talk.begin(p,LastCrossing.TALK,"Vera");Talk.line(p,"Vera",called);Talk.ask(p,Talk.reply("Here.",LastCrossingDayThree::answer));
    }
    static void answer(ServerPlayer p){
        Talk.close(p);var r=LastCrossing.runOf(p);if(r==null||!r.crew.containsKey(p.getUUID()))return;
        r.dayThree.answered.add(p.getUUID());r.dayThree.answerAt=r.tick;LastCrossing.mark(p,"AnsweredDayRoll");
    }
    /** After Ellis's name, a breath late, something answers up the speaking pipes: heard by whoever is near a mouth. */
    static void ellisAnswers(ServerLevel l,Run r,List<ServerPlayer> here){
        var mouths=List.of(LastCrossingSite.PIPE_A_UP,LastCrossingSite.PIPE_A_DOWN,LastCrossingSite.PIPE_B_UP,LastCrossingSite.PIPE_B_DOWN);
        for(var m:mouths)LastCrossing.sound(l,r.base.offset(m),CrossingRegistry.SCRAPE.get(),.5F,1.3F);
        for(var p:here){boolean near=false;for(var m:mouths)near|=p.position().distanceToSqr(Vec3.atCenterOf(r.base.offset(m)))<PIPE_EAR*PIPE_EAR;
            if(!near)continue;LastCrossing.mark(p,"HeardEllisAnswer");
            p.displayClientMessage(Component.literal(LastCrossingTexts.PIPE_ANSWER).withStyle(net.minecraft.ChatFormatting.GRAY),true);}
    }
    /** "Who's working the launch? Who's looking for the source? And who's still below?" Each reader who hears it can say. */
    static void question(ServerLevel l,Run r,List<ServerPlayer> here){
        var d=r.dayThree;
        LastCrossing.speak(l,r,LastCrossingTexts.DAY3_QUESTION,EARSHOT);
        if(!d.absent.isEmpty())LastCrossing.speak(l,r,String.format(LastCrossingTexts.STILL_BELOW,String.join(", ",d.absent)),EARSHOT);
        for(var p:here){if(p.isSpectator()||!r.crew.containsKey(p.getUUID())||r.dead.contains(p.getUUID())||!LastCrossingNightTwo.hears(l,r,p))continue;
            for(var who:d.absent)if(!who.equals(LastCrossing.name(p)))LastCrossing.addName(p,"NotedAbsentDay",who);
            LastCrossing.begin(p,"Vera");Talk.line(p,"Vera",LastCrossingTexts.DAY3_QUESTION);
            Talk.ask(p,Talk.reply("The launch.",q->plan(q,"launch")),Talk.reply("The source.",q->plan(q,"source")),Talk.reply("Wherever I'm needed.",q->plan(q,"either")));}
    }
    static void plan(ServerPlayer p,String plan){
        Talk.close(p);var r=LastCrossing.runOf(p);if(r==null)return;r.dayThree.plans.put(p.getUUID(),plan);
        var own=LastCrossing.own(p);own.putString("Plan",plan);LastCrossing.save(p,own);
    }

    /** One stroke on a fitting (a heave, a shove, an armful of debris): no more than one every STROKE ticks on the same fitting. */
    static boolean stroke(Run r,BlockPos rel){var d=r.dayThree;long last=d.lastStroke.getOrDefault(rel,-1_000L);if(r.tick-last<STROKE)return false;d.lastStroke.put(rel,r.tick);return true;}

    // ------------------------------------------------------------------------------------------------ the launch
    /** A roller on Day Three: one the rise jumped seats with a heave; one a failed pull threw takes three. */
    static boolean roller(ServerPlayer p,BlockPos rel,int stage){
        var r=LastCrossing.runOf(p);if(r==null||!active(r))return false;var d=r.dayThree;var l=p.serverLevel();
        if(stage==0){LastCrossing.cue(p,"Seated, the cable over it.");return true;}
        var at=r.base.offset(rel);
        if(d.thrown.contains(rel)){
            if(!stroke(r,rel))return true;
            int n=d.heaves.merge(rel,1,Integer::sum);LastCrossing.mark(p,"HeavedRoller");LastCrossing.sound(l,at,SoundEvents.ANVIL_PLACE,.3F,.9F+n*.1F);
            if(n<ROLLER_HEAVES){LastCrossing.cue(p,LastCrossingTexts.HEAVE);return true;}
            d.thrown.remove(rel);LastCrossing.mark(p,"ResetRoller");}
        l.setBlock(at,LastCrossingSite.fit(Kind.ROLLER,Direction.NORTH),Block.UPDATE_CLIENTS);LastCrossing.sound(l,at,SoundEvents.ANVIL_PLACE,.35F,1.4F);
        LastCrossing.mark(p,"SeatedRoller");LastCrossing.job(p,"launch");
        boolean all=true;for(var q:LastCrossingSite.ROLLERS){var s=l.getBlockState(r.base.offset(q));if(CrossingBlock.is(s,Kind.ROLLER)&&s.getValue(CrossingBlock.STAGE)==1)all=false;}
        if(rel.equals(LastCrossingSite.ROLLERS.get(3)))LastCrossing.mark(p,"SeatedLastRoller");
        if(all){r.rollerSeated=true;LastCrossing.cue(p,LastCrossingTexts.ROLLERS_SEATED);}
        else LastCrossing.cue(p,"You heave the roller back into its cradle. The cable settles over it.");
        return true;
    }
    /** The launch pin's sleeve: empty until the pin is brought down from the porch; staged, someone says they'll seat it tonight. */
    static boolean pin(ServerPlayer p,int stage){
        var r=LastCrossing.runOf(p);if(r==null||r.dayNo<3)return false;var d=r.dayThree;var l=p.serverLevel();var at=r.base.offset(LastCrossingSite.PIN);
        if(stage==2){
            if(!LastCrossing.holds(p,"pin")){LastCrossing.cue(p,LastCrossingTexts.SLEEVE_EMPTY);return true;}
            LastCrossing.drop(p,"pin");d.pinStaged=true;d.pinTaken=false;if(d.pinBy==null)d.pinBy=p.getUUID();
            l.setBlock(at,LastCrossingSite.fit(Kind.PIN,Direction.SOUTH,0),Block.UPDATE_CLIENTS);LastCrossing.sound(l,at,SoundEvents.IRON_TRAPDOOR_OPEN,.7F,1.1F);
            LastCrossing.mark(p,"StagedPin");LastCrossing.job(p,"launch");if(p.getUUID().equals(d.pinBy))LastCrossing.mark(p,"PinHand");
            LastCrossing.cue(p,LastCrossingTexts.PIN_STAGED);return true;}
        // With someone on the wheel below, the old practice: let the rehearsal seat it.
        if(!active(r)||r.wheelBy!=null||stage!=0)return false;
        LastCrossing.begin(p,"The launch pin");
        Talk.line(p,"","The launch pin, up in its sleeve over the sluice. Somebody has to drop it in tonight, when the wheel's held shut below."+(d.pinBy==null?"":" "+(p.getUUID().equals(d.pinBy)?"You said you would.":"It's "+who(l,d.pinBy)+"'s.")));
        if(!p.getUUID().equals(d.pinBy))Talk.ask(p,Talk.reply("I'll seat it tonight.",q->claimPin(q)),Talk.act("Leave it.",Talk::close));
        else Talk.ask(p,Talk.act("Leave it.",Talk::close));
        return true;
    }
    static void claimPin(ServerPlayer p){Talk.close(p);var r=LastCrossing.runOf(p);if(r==null)return;r.dayThree.pinBy=p.getUUID();LastCrossing.mark(p,"PinHand");LastCrossing.job(p,"launch");}
    static String who(ServerLevel l,UUID id){var q=LastCrossing.player(l,id);if(q!=null)return LastCrossing.name(q);return "somebody";}
    /** The sluice wheel tried through its whole travel on Day Three. */
    static void wheel(ServerPlayer p){
        var r=LastCrossing.runOf(p);if(r==null||!active(r)||r.pinSeated)return;var d=r.dayThree;
        boolean first=!d.wheelTested;d.wheelTested=true;LastCrossing.mark(p,"TestedWheel");LastCrossing.job(p,"launch");
        if(first)LastCrossing.cue(p,LastCrossingTexts.WHEEL_TESTED);
    }
    /** The launch rope at its bollard: by day, someone says they'll take it tonight. */
    static boolean release(ServerPlayer p,int stage){
        var r=LastCrossing.runOf(p);if(r==null)return false;var d=r.dayThree;var l=p.serverLevel();
        if(r.dayNo<3){LastCrossing.cue(p,LastCrossingTexts.ROPE_EARLY);return true;}
        if(!active(r))return false;
        LastCrossing.begin(p,"The launch rope");
        Talk.line(p,"",LastCrossingTexts.ROPE+(d.ropeBy==null?"":" "+(p.getUUID().equals(d.ropeBy)?"You said you'd take it.":"It's "+who(l,d.ropeBy)+"'s.")));
        if(!p.getUUID().equals(d.ropeBy))Talk.ask(p,Talk.reply("I'll take the rope tonight.",LastCrossingDayThree::claimRope),Talk.act("Leave it.",Talk::close));
        else Talk.ask(p,Talk.act("Leave it.",Talk::close));
        return true;
    }
    static void claimRope(ServerPlayer p){Talk.close(p);var r=LastCrossing.runOf(p);if(r==null)return;r.dayThree.ropeBy=p.getUUID();LastCrossing.mark(p,"LaunchHand");LastCrossing.job(p,"launch");}
    /** Ready to launch: every roller seated, the pin in its sleeve, the wheel tried, a hand for the rope and one for the pin. */
    static void launch(ServerLevel l,Run r,List<ServerPlayer> here){
        var d=r.dayThree;if(d.launchReady)return;
        if(!r.rollerSeated||!d.pinStaged||!d.wheelTested||d.ropeBy==null||d.pinBy==null)return;
        d.launchReady=true;
        for(var p:here){var q=LastCrossing.rel(p.position(),r.base);if(LastCrossingSite.atRamp(q)||LastCrossingSite.inPit(q)||LastCrossingSite.inCableHouse(q)){LastCrossing.mark(p,"LaunchReady");LastCrossing.cue(p,LastCrossingTexts.LAUNCH_READY);}}
        for(var id:new UUID[]{d.ropeBy,d.pinBy}){var p=LastCrossing.player(l,id);if(p!=null)LastCrossing.mark(p,"LaunchReady");}
    }

    // ------------------------------------------------------------------------------------------------ the source
    /** One of the live feed's tags, traced: chalked, and remembered by everyone who had a hand in it. */
    static boolean feed(ServerPlayer p,BlockPos rel,int stage){
        var r=LastCrossing.runOf(p);if(r==null)return false;var d=r.dayThree;var l=p.serverLevel();
        int bit=rel.equals(LastCrossingSite.FEED_GAUGE)?FEED_GAUGE:rel.equals(LastCrossingSite.FEED_INTAKE)?FEED_INTAKE:FEED_GALLERY;
        if(!active(r)){LastCrossing.cue(p,LastCrossingTexts.feedLook(bit));return true;}
        d.feedHands.add(p.getUUID());
        if((d.feed&bit)!=0){LastCrossing.cue(p,LastCrossingTexts.feedLook(bit));return true;}
        d.feed|=bit;l.setBlock(r.base.offset(rel),l.getBlockState(r.base.offset(rel)).setValue(CrossingBlock.STAGE,1),Block.UPDATE_CLIENTS);
        LastCrossing.mark(p,"TracedFeedPart");LastCrossing.job(p,"source");
        LastCrossing.cue(p,LastCrossingTexts.feedTrace(bit));
        if(d.feed==FEED_ALL)for(var id:d.feedHands){var q=LastCrossing.player(l,id);if(q==null)continue;LastCrossing.mark(q,"TracedFeed");LastCrossing.cue(q,LastCrossingTexts.FEED_TRACED);}
        return true;
    }
    /** The rusted door in the intake's east wall: shut fast until the feed is traced to it; then a few shoves grind it open. */
    static boolean door(ServerPlayer p,int stage){
        var r=LastCrossing.runOf(p);if(r==null)return false;var d=r.dayThree;var l=p.serverLevel();var at=r.base.offset(LastCrossingSite.GALLERY_DOOR);
        if(stage==1){LastCrossing.cue(p,LastCrossingTexts.DOOR_OPEN_LOOK);return true;}
        if(!active(r)||(d.feed&FEED_INTAKE)==0){LastCrossing.cue(p,LastCrossingTexts.DOOR_RUSTED);return true;}
        if(!stroke(r,LastCrossingSite.GALLERY_DOOR))return true;
        d.shoves++;LastCrossing.sound(l,at,SoundEvents.IRON_DOOR_CLOSE,.8F,.5F+d.shoves*.1F);LastCrossing.mark(p,"ShovedDoor");
        if(d.shoves<DOOR_SHOVES){LastCrossing.cue(p,LastCrossingTexts.DOOR_SHOVE);return true;}
        d.doorOpen=true;l.setBlock(at,LastCrossingSite.fit(Kind.GALLERY_DOOR,Direction.EAST,1),Block.UPDATE_CLIENTS);
        LastCrossing.sound(l,at,SoundEvents.IRON_DOOR_OPEN,1F,.5F);LastCrossing.mark(p,"OpenedGallery");LastCrossing.job(p,"source");
        LastCrossing.cue(p,LastCrossingTexts.DOOR_OPENED);
        for(var q:LastCrossing.readers(l,r.base))if(q!=p&&LastCrossingSite.inIntake(LastCrossing.rel(q.position(),r.base)))LastCrossing.cue(q,LastCrossingTexts.DOOR_HEARD);
        return true;
    }
    /** The racks: the launch pin on the porch from the third morning, and the locking bar in the intake, taken down on Day Three. */
    static boolean rack(ServerPlayer p,BlockPos rel,int stage){
        var r=LastCrossing.runOf(p);if(r==null)return false;var d=r.dayThree;var l=p.serverLevel();var at=r.base.offset(rel);
        if(stage==0){
            if(rel.equals(LastCrossingSite.PIN_RACK)&&LastCrossing.holds(p,"pin")){LastCrossing.drop(p,"pin");d.pinTaken=false;l.setBlock(at,LastCrossingSite.fit(Kind.RACK,Direction.SOUTH,1),Block.UPDATE_CLIENTS);LastCrossing.cue(p,"You hang the pin back on the rack.");return true;}
            if(rel.equals(LastCrossingSite.BAR_RACK)&&LastCrossing.holds(p,"lockbar")){LastCrossing.drop(p,"lockbar");d.barTaken=false;l.setBlock(at,LastCrossingSite.fit(Kind.RACK,Direction.WEST,2),Block.UPDATE_CLIENTS);LastCrossing.cue(p,"You lay the bar back on its hooks.");return true;}
            LastCrossing.cue(p,"An empty rack on the wall.");return true;}
        if(r.dayNo<3||r.phase==LastCrossing.DAWN||r.phase==LastCrossing.END){LastCrossing.cue(p,stage==2?LastCrossingTexts.BAR_EARLY:"The launch pin, on its rack.");return true;}
        boolean pin=stage==1;
        if(!LastCrossing.give(p,LastCrossing.item(pin?CrossingRegistry.PIN.get():CrossingRegistry.LOCKBAR.get(),pin?"pin":"lockbar",null))){LastCrossing.cue(p,"Your hands are full.");return true;}
        l.setBlock(at,LastCrossingSite.fit(Kind.RACK,l.getBlockState(at).getValue(CrossingBlock.FACING),0),Block.UPDATE_CLIENTS);
        if(pin){d.pinTaken=true;LastCrossing.mark(p,"TookPin");LastCrossing.job(p,"launch");LastCrossing.cue(p,LastCrossingTexts.PIN_TAKEN);}
        else{d.barTaken=true;LastCrossing.mark(p,"TookBar");LastCrossing.job(p,"source");LastCrossing.cue(p,LastCrossingTexts.BAR_TAKEN);}
        return true;
    }
    /** The feed gate: the locking bar staged in its bracket by day; Night Three bars and draws it. */
    static boolean gate(ServerPlayer p,int stage){
        var r=LastCrossing.runOf(p);if(r==null)return false;var d=r.dayThree;var l=p.serverLevel();var at=r.base.offset(LastCrossingSite.FEED_GATE);
        if(stage==0&&LastCrossing.holds(p,"lockbar")){
            LastCrossing.drop(p,"lockbar");d.barStaged=true;d.barTaken=false;l.setBlock(at,LastCrossingSite.fit(Kind.FEED_GATE,Direction.WEST,1),Block.UPDATE_CLIENTS);
            LastCrossing.sound(l,at,SoundEvents.CHAIN_PLACE,1F,.7F);LastCrossing.mark(p,"StagedBar");LastCrossing.job(p,"source");
            LastCrossing.cue(p,LastCrossingTexts.BAR_STAGED);return true;}
        LastCrossing.mark(p,"SawGate");LastCrossing.cue(p,stage==1?LastCrossingTexts.GATE_STAGED_LOOK:LastCrossingTexts.GATE_LOOK);
        return true;
    }
    /** A heap of fallen debris in the passage to the cutoff: four strokes to clear it, the first two bringing it down to half. */
    static boolean debris(ServerPlayer p,BlockPos rel,int stage){
        var r=LastCrossing.runOf(p);if(r==null)return false;var d=r.dayThree;var l=p.serverLevel();var at=r.base.offset(rel);
        if(!active(r)){LastCrossing.cue(p,LastCrossingTexts.DEBRIS_LOOK);return true;}
        if(!stroke(r,rel))return true;
        int n=d.strokes.merge(rel,1,Integer::sum);d.pathHands.add(p.getUUID());LastCrossing.mark(p,"WorkedDebris");LastCrossing.job(p,"source");
        LastCrossing.sound(l,at,SoundEvents.GRAVEL_BREAK,1F,.8F);l.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,at.getX()+.5,at.getY()+1,at.getZ()+.5,3,.3,.3,.3,.002);
        if(n<DEBRIS_STROKES){if(n==DEBRIS_STROKES/2)l.setBlock(at,LastCrossingSite.fit(Kind.DEBRIS,Direction.NORTH,1),Block.UPDATE_CLIENTS);LastCrossing.cue(p,n==1?LastCrossingTexts.DEBRIS_FIRST:LastCrossingTexts.DEBRIS_MORE);return true;}
        l.setBlock(at,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);LastCrossing.cue(p,LastCrossingTexts.DEBRIS_CLEARED);
        boolean clear=true;for(var q:LastCrossingSite.DEBRIS)if(CrossingBlock.is(l.getBlockState(r.base.offset(q)),Kind.DEBRIS))clear=false;
        if(clear&&!d.pathCleared){d.pathCleared=true;for(var id:d.pathHands){var q=LastCrossing.player(l,id);if(q==null)continue;LastCrossing.mark(q,"ClearedPath");LastCrossing.cue(q,LastCrossingTexts.PATH_CLEAR);}}
        return true;
    }
    /** The upstream cutoff on its alcove wall: it won't turn against the feed by day. */
    static boolean cutoff(ServerPlayer p,int stage){
        var r=LastCrossing.runOf(p);if(r==null)return false;LastCrossing.mark(p,"ReachedCutoff");if(active(r))LastCrossing.job(p,"source");
        LastCrossing.cue(p,stage==1?"The upstream cutoff, wound shut.":LastCrossingTexts.CUTOFF_LOOK);return true;
    }
    /** Ready in the gallery: the feed traced, the bar staged at the gate, the way to the cutoff clear. */
    static void gallery(ServerLevel l,Run r,List<ServerPlayer> here){
        var d=r.dayThree;if(d.galleryReady||d.feed!=FEED_ALL||!d.barStaged||!d.pathCleared)return;
        d.galleryReady=true;
        for(var p:here){if(!LastCrossingSite.inGallery(LastCrossing.rel(p.position(),r.base)))continue;LastCrossing.mark(p,"GalleryReady");LastCrossing.cue(p,LastCrossingTexts.GALLERY_READY);}
        for(var set:List.of(d.feedHands,d.pathHands))for(var id:set){var p=LastCrossing.player(l,id);if(p!=null)LastCrossing.mark(p,"GalleryReady");}
    }

    // ------------------------------------------------------------------------------------------------ the master bypass and the bargain
    /** The master bypass by the sluice wheel: rusted fast by day. A reader the plate has shown carrying knows it will turn at high water. */
    static boolean master(ServerPlayer p,int stage){
        var r=LastCrossing.runOf(p);if(r==null)return false;var own=LastCrossing.own(p);
        if(stage==1){LastCrossing.cue(p,"The master bypass, open. Water's running behind it into the ramp channel.");return true;}
        if(own.getBoolean("Infected")&&own.getBoolean("Exposed")&&r.dayNo>=3){LastCrossing.mark(p,"KnowsMaster");LastCrossing.cue(p,LastCrossingTexts.MASTER_KNOWN);return true;}
        LastCrossing.mark(p,"SawMaster");LastCrossing.cue(p,LastCrossingTexts.MASTER_LOOK);return true;
    }
    /** The downstream sluice, where the branches empty into the river. */
    static boolean downstream(ServerPlayer p,int stage){
        var r=LastCrossing.runOf(p);if(r==null)return false;LastCrossing.mark(p,"SawDownstream");
        LastCrossing.cue(p,stage==1?"The downstream sluice, wound shut. Whatever runs in the branches backs up behind it.":LastCrossingTexts.DOWNSTREAM_LOOK);return true;
    }
    /** "The source offers the Other Breath privately after an exposed reading": a carrier who laid a palm on the plate and showed the second
     * print, on Day Three or before it, hears the offer under their own breath. */
    static void offers(ServerLevel l,Run r,List<ServerPlayer> here){
        var d=r.dayThree;if(r.day<OFFER_AFTER||r.tick%20!=0)return;
        for(var p:here){if(d.offered.contains(p.getUUID())||r.dead.contains(p.getUUID())||p.isSpectator())continue;var own=LastCrossing.own(p);
            if(!own.getBoolean("Infected")||!own.getBoolean("Exposed"))continue;offer(p,d);}
    }
    /** An exposed reading on Day Three: the offer follows it at once. */
    static void exposed(ServerPlayer p){var r=LastCrossing.runOf(p);if(r==null||!active(r)||r.day<1)return;if(!r.dayThree.offered.contains(p.getUUID()))offer(p,r.dayThree);}
    static void offer(ServerPlayer p,Day d){
        d.offered.add(p.getUUID());LastCrossing.mark(p,"OfferedBreath");
        p.playNotifySound(CrossingRegistry.BREATH.get(),SoundSource.PLAYERS,1F,.8F);LastCrossing.cue(p,LastCrossingTexts.OTHER_BREATH);
    }
    /** What a reader sees of the gallery: the source through the glass. */
    static void seen(ServerLevel l,Run r,ServerPlayer p){
        var own=LastCrossing.own(p);
        if(!own.getBoolean("SawSource")&&LastCrossingSite.inGallery(LastCrossing.rel(p.position(),r.base))&&LastCrossing.sees(p,LastCrossing.world(r.base,LastCrossingSite.SOURCE_SEEN),12,.5)){
            LastCrossing.mark(p,"SawSource");LastCrossing.cue(p,LastCrossingTexts.SOURCE_SEEN);}
    }
    /** The cable house's brake and catch on Day Three: the drum is where the night left it. */
    static boolean brake(ServerPlayer p){
        var r=LastCrossing.runOf(p);if(r==null||!active(r))return false;
        LastCrossing.cue(p,r.night2.pull==LastCrossingNightTwo.PULLED?LastCrossingTexts.BRAKE_TAKEN_UP:LastCrossingTexts.BRAKE_THROWN);return true;
    }
    static boolean catchHeld(ServerPlayer p){var r=LastCrossing.runOf(p);if(r==null||!active(r))return false;LastCrossing.cue(p,"Leave the catch where it is. Nobody's pulling today.");return true;}

    // ------------------------------------------------------------------------------------------------ the station's things carried about
    /** The pin or the bar carried off the station, or left where nobody holds it: back to its rack. */
    static void restore(ServerLevel l,Run r,String w){
        var d=r.dayThree;var b=r.base;
        if(w.equals("pin")&&!d.pinStaged){d.pinTaken=false;l.setBlock(b.offset(LastCrossingSite.PIN_RACK),LastCrossingSite.fit(Kind.RACK,Direction.SOUTH,1),Block.UPDATE_CLIENTS);}
        if(w.equals("lockbar")&&!d.barStaged){d.barTaken=false;l.setBlock(b.offset(LastCrossingSite.BAR_RACK),LastCrossingSite.fit(Kind.RACK,Direction.WEST,2),Block.UPDATE_CLIENTS);}
    }
    static boolean out(Run r,String w){return w.equals("pin")?r.dayThree.pinTaken:w.equals("lockbar")&&r.dayThree.barTaken;}

    // ------------------------------------------------------------------------------------------------ the station set back
    static void reset(ServerLevel l,Run r){r.dayThree=new Day();}
}
