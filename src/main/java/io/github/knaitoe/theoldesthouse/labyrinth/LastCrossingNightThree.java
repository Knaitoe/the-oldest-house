package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.CrossingBlock.Kind;
import io.github.knaitoe.theoldesthouse.labyrinth.LastCrossing.Run;
import io.github.knaitoe.theoldesthouse.network.HouseFadePayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * 0.4.356, The Last Crossing, Night Three (docs/LAST_CROSSING_0_4_356.md), from the owner's design: "The final rise reaches the ramp. Anyone
 * carrying the infection transforms again or, for the second carrier, for the first time. Mobs come through whichever branches are open.
 * Vera calls the roll in the same order she did when the crew arrived."
 *
 * <p>The last night runs on Night Two's workings (the turned reader's water, claws, call and broken speech, the floor grate, the things
 * from the water, the grapple), with no pull and no second slot, and both carriers turning one after the other. Vera keeps it at the ramp
 * and calls the roll in the first day's order. A little in, the final rise fills the sluice pit to below its deck, and the master bypass
 * at the pit's foot comes within reach.
 *
 * <p>The launch: someone holds the sluice wheel shut at the bottom of the flooded pit while another drops the pin from the deck above; a
 * turned reader holding the wheel feels their throat close as the gate cuts the source's channel off, until the pin is in. A while after,
 * the ferry floats off her sill, and whoever pulls the launch rope lets her go with everyone aboard; she crosses, and if one of them is
 * clean the crossing is the crew's. Vera calls the roll again on the far bank. The gallery: one holds the feed gate while another drops the
 * locking bar across it, and with the gate barred the upstream cutoff winds shut; the chamber drains, the gate draws up, and the things
 * holding the source have to be fought off it before it can be struck dead. Its death releases the turned at once, and the station bell
 * calls the muster. Siding with the source: a turned reader can open the master bypass at high water and hold it; the pipes hammer, the
 * water comes up over the ramp's approach and the things come up onto the landing, and the source's choke lets go. A carrier the plate
 * exposed, held there long enough, takes the Other Breath: three more seconds under water, theirs for good. Winding the downstream sluice
 * shut before then denies it; after, it only drains the landing. The source takes the crossing when no clean reader is left standing at the
 * station, or the night ends with neither the ferry across nor the source dead: the ferry comes in at the far landing anyway, and every
 * name on the roll gets an answer. Then morning on the far bank. The accounts, the nameplate and the Witness source are slice 4c.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class LastCrossingNightThree {
    private LastCrossingNightThree(){}
    /** Ticks: the last night; when the final rise reaches the ramp; how long after the pin seats the ferry floats off her sill; her
     * crossing; the chamber's drain; how long a turned reader holds the master open for the bargain; how long the muster waits for the bell;
     * how long each ending's last beat runs; how long Vera waits for an answer on the far bank; the least time between blows on the source. */
    public static final int NIGHT3_TICKS=6000,RISE_AT=300,SILL_CLEAR=200,TRANSIT=200,DRAIN=100,BARGAIN_HOLD=400,MUSTER_WAIT=400,BEAT=200,FAR_ANSWER=80,BLOW=10;
    /** Blows that kill the source once nothing is covering it. */
    public static final int SOURCE_HITS=6;
    /** Ticks a clean reader who drops out still counts as standing, for the source's check (the design's short grace window). */
    public static final int GRACE=600;
    /** On the crew's crossing, when the delayed answer for Ellis comes from the far end of the boat, and how long Vera waits on that name. */
    static final int LATE_ANSWER=70,ELLIS_WAIT=FAR_ANSWER+30;
    /** How the crossing ends: not yet; the ferry crossed; the station cleared; the source took it. */
    public static final int NONE=0,FERRY=1,GALLERY=2,SOURCE=3;
    /** The crew's crossing's beat after the far roll, while the plate is broken and the spare named. */
    static final int PIECES=3;
    /** The Other Breath: three more seconds of air. Air drains with a chance of 1/(1+bonus) a tick, so a bonus of 0.2 stretches fifteen
     * seconds to eighteen. */
    static final ResourceLocation OTHER_BREATH=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"other_breath");
    static final double OTHER_BREATH_BONUS=.2;

    static final class Night {
        boolean begun,risen;
        /** The launch: the pin seated and when, the ferry off her sill, released and when and by whom, who went aboard, arrived. */
        boolean pinned,floating,released,arrived;long pinnedAt=-1,releasedAt=-1;@Nullable UUID releasedBy,pinnedBy;final LinkedHashSet<UUID> aboard=new LinkedHashSet<>();
        /** The gallery: who holds the gate, barred, the cutoff shut, the drain and done, the defenders, blows on the source, dead and when,
         * who was down there for the fight, mustered. */
        @Nullable UUID gateBy;boolean barred,cutoff,drained,destroyed,mustered;int drain,hits;long hitAt=-1,destroyedAt=-1;
        final Set<UUID> defenders=new HashSet<>(),inGallery=new LinkedHashSet<>();
        /** The master bypass: who holds it open, how long, open, the bargain taken or denied, by whom; the landing flooded. */
        @Nullable UUID masterBy,bargainBy;int masterHeld;boolean masterOpen,bargained,denied,flooded;
        /** The ending: which, when, its beat, the far roll's place in the names, and done. */
        int ending;long endingAt=-1;int beat,farCalling=-1,farTicks;boolean ended;final Set<UUID> farAnswered=new HashSet<>();
        /** When each clean reader of the crew was last seen standing at the station. */
        final Map<UUID,Long> lastClean=new HashMap<>();
        /** 0.4.357: the nameplate broken, the spare's closing roll open and settled, when it opened, and who named whom. */
        boolean broken,spareOpen,spareSettled;long spareAt=-1,settledAt=-1;final Map<UUID,UUID> nominations=new LinkedHashMap<>();
    }
    static Night n(Run r){return r.night3;}
    static boolean active(Run r){return r.phase==LastCrossing.DUSK&&r.dayNo>=3;}
    /** For the station: a turned reader holding the wheel before the pin seats, or the feed gate before it is barred, has their throat closed. */
    static boolean holdingLive(Run r,ServerPlayer p){
        if(!active(r))return false;var n=n(r);
        return p.getUUID().equals(r.wheelBy)&&!n.pinned||p.getUUID().equals(n.gateBy)&&!n.barred;
    }
    static boolean masterOpen(Run r){return active(r)&&n(r).masterOpen&&!n(r).denied;}
    /** On the last night the pin stays where it is put. */
    static boolean keepsPin(Run r){return r.dayNo>=3&&(r.phase==LastCrossing.DUSK||r.phase==LastCrossing.END);}

    // ------------------------------------------------------------------------------------------------ for tests
    public static int ending(BlockPos b){var r=LastCrossing.run(b);return r==null?-1:r.night3.ending;}
    public static List<UUID> aboard(BlockPos b){var r=LastCrossing.run(b);return r==null?List.of():List.copyOf(r.night3.aboard);}
    public static int defenders(BlockPos b){var r=LastCrossing.run(b);return r==null?0:r.night3.defenders.size();}
    /** For tests: the night's own clocks run on by this many ticks (the sill, the crossing, the drain, the master's hold, the muster). */
    public static void hurry(BlockPos b,int ticks){var r=LastCrossing.run(b);if(r==null)return;var n=r.night3;
        if(n.pinnedAt>=0)n.pinnedAt-=ticks;if(n.spareAt>=0)n.spareAt-=ticks;if(n.settledAt>=0)n.settledAt-=ticks;if(n.releasedAt>=0)n.releasedAt-=ticks;if(n.destroyedAt>=0)n.destroyedAt-=ticks;if(n.endingAt>=0)n.endingAt-=ticks;
        if(n.cutoff&&!n.drained)n.drain=Math.min(DRAIN-1,n.drain+ticks);if(n.masterBy!=null)n.masterHeld=Math.min(BARGAIN_HOLD-1,n.masterHeld+ticks);}
    /** The Other Breath's bonus on a reader, for tests. */
    public static double breath(ServerPlayer p){var a=p.getAttribute(Attributes.OXYGEN_BONUS);var m=a==null?null:a.getModifier(OTHER_BREATH);return m==null?0:m.amount();}

    // ------------------------------------------------------------------------------------------------ the third dusk
    /** The last dusk: Night Two's workings set up again for tonight (what the second night left kept: tears, prints, the pinned grate,
     * the second slot, the pull's outcome), and Vera goes down to keep it at the ramp. */
    static void begin(ServerLevel l,Run r,List<ServerPlayer> here){
        var old=r.night2;var n2=new LastCrossingNightTwo.Night();
        n2.third=true;n2.torn.addAll(old.torn);n2.prints.addAll(old.prints);n2.pinned=old.pinned;n2.secondSlot=old.secondSlot;n2.jamCleared=old.jamCleared;n2.pull=old.pull;n2.threw=old.threw;
        r.night2=n2;r.night3=new Night();r.night3.begun=true;r.phase=LastCrossing.DUSK;
        LastCrossing.speak(l,r,LastCrossingTexts.DUSK3,64);
        for(var p:here)if(r.crew.containsKey(p.getUUID()))LastCrossing.mark(p,"DayThree");
        LastCrossing.route(r,new Vec3(13.5,0,-35.5),new Vec3(26.5,0,-35.5),new Vec3(26.5,0,-50.5),LastCrossingSite.VERA_RAMP);
    }
    static void tick(ServerLevel l,Run r,List<ServerPlayer> here){
        var n=n(r);var n2=r.night2;if(!n.begun)begin(l,r,here);
        if(n.ending!=NONE){ending(l,r,n,here);return;}
        n2.tick++;
        for(var p:here)if(r.crew.containsKey(p.getUUID())&&p.isAlive()&&!p.isSpectator()&&!r.dead.contains(p.getUUID())&&!LastCrossing.own(p).getBoolean("Infected"))n.lastClean.put(p.getUUID(),r.tick);
        if(!n.risen&&n2.tick>=RISE_AT)rise(l,r,n,here);
        LastCrossingNightTwo.turn(l,r,n2,here);
        LastCrossingNightTwo.roll(l,r,n2,here);
        LastCrossingNightTwo.gate(l,r,n2,here);
        LastCrossingNightTwo.calls(l,r,n2,here);
        LastCrossingNightTwo.below(l,r,n2,here);
        LastCrossingNightTwo.mobs(l,r,n2,here);
        LastCrossingNightTwo.grapple(l,r,n2,here);
        LastCrossingNightTwo.bodies(l,r,n2,here);
        if(r.tick%10==0)for(var p:here)LastCrossingNightTwo.prints(l,r,n2,p);
        launch(l,r,n,here);
        gallery(l,r,n,here);
        master(l,r,n,here);
        if(n.ending==NONE)source(l,r,n,here);
    }
    /** The final rise: the pit fills to below its deck, and the master bypass at its foot comes within reach of anyone who breathes water. */
    static void rise(ServerLevel l,Run r,Night n,List<ServerPlayer> here){
        n.risen=true;LastCrossingSite.floodPit(l,r.base);
        LastCrossing.sound(l,r.base.offset(LastCrossingSite.WHEEL),CrossingRegistry.RISE.get(),1.2F,.8F);
        LastCrossing.speak(l,r,LastCrossingTexts.RISE3,64);
        for(var p:here){LastCrossing.mark(p,"SawRise");if(LastCrossingNightTwo.turned(r,p)&&LastCrossing.own(p).getBoolean("KnowsMaster"))LastCrossing.cue(p,LastCrossingTexts.MASTER_NOW);}
    }

    // ------------------------------------------------------------------------------------------------ the launch
    static void launch(ServerLevel l,Run r,Night n,List<ServerPlayer> here){
        // The wheel at night is held as long as its holder stays on it.
        if(r.wheelBy!=null&&!r.pinSeated){var p=LastCrossing.player(l,r.wheelBy);
            if(p!=null&&here.contains(p)&&p.isAlive()&&p.position().distanceToSqr(Vec3.atCenterOf(r.base.offset(LastCrossingSite.WHEEL)))<2.5*2.5){r.wheelUntil=r.tick+5;if(r.tick%20==0)LastCrossing.mark(p,"HeldWheel");}}
        if(!n.pinned&&r.pinSeated){n.pinned=true;n.pinnedAt=r.tick;LastCrossing.speak(l,r,LastCrossingTexts.PINNED3,48);
            var below=LastCrossing.player(l,r.wheelBy);if(below!=null){LastCrossing.mark(below,"HeldWheelToPin");if(LastCrossingNightTwo.turned(r,below))LastCrossing.cue(below,LastCrossingTexts.WHEEL_EASED);}}
        if(n.pinned&&!n.floating&&r.tick-n.pinnedAt>=SILL_CLEAR){n.floating=true;LastCrossing.speak(l,r,LastCrossingTexts.OFF_SILL,64);
            LastCrossing.sound(l,r.base.offset(33,0,-66),CrossingRegistry.RISE.get(),1F,1.2F);}
        if(n.released&&!n.arrived){
            for(var it=n.aboard.iterator();it.hasNext();){var p=LastCrossing.player(l,it.next());if(p==null||!here.contains(p)||!LastCrossingSite.onFerry(LastCrossing.rel(p.position(),r.base))){if(p!=null)LastCrossing.mark(p,"WentOverSide");it.remove();}}
            if(r.tick%10==0){var at=LastCrossing.world(r.base,new Vec3(33.5,-.8,-76.5));l.sendParticles(ParticleTypes.SPLASH,at.x,at.y,at.z,10,2.5,.1,.5,.1);}
            if(r.tick-n.releasedAt>=TRANSIT)arrive(l,r,n,here);}
    }
    /** The launch rope pulled: let go, once she's off the sill, with whoever is aboard. */
    static boolean release(ServerPlayer p){
        var r=LastCrossing.runOf(p);if(r==null||!active(r))return false;var n=n(r);
        if(n.released){LastCrossing.cue(p,"The rope's slipped. She's away.");return true;}
        if(!n.floating){LastCrossing.cue(p,n.pinned?LastCrossingTexts.STILL_ON_SILL:LastCrossingTexts.NOT_PINNED);return true;}
        depart(p.serverLevel(),r,n,p);return true;
    }
    static void depart(ServerLevel l,Run r,Night n,ServerPlayer by){
        var b=r.base;n.released=true;n.releasedAt=r.tick;n.releasedBy=by.getUUID();
        l.setBlock(b.offset(LastCrossingSite.RELEASE),LastCrossingSite.fit(Kind.RELEASE,Direction.NORTH,1),Block.UPDATE_CLIENTS);
        LastCrossing.sound(l,b.offset(LastCrossingSite.RELEASE),SoundEvents.LEASH_KNOT_BREAK,1F,.7F);
        LastCrossing.mark(by,"PulledRelease");
        // Vera goes aboard with them.
        var a=LastCrossing.actor(l,r.vera);if(a!=null){LastCrossing.place(a,b,LastCrossingSite.VERA_FERRY,180);r.route.clear();}
        LastCrossing.speak(l,r,LastCrossingTexts.LET_GO,64);
        for(var p:LastCrossing.readers(l,b)){var q=LastCrossing.rel(p.position(),b);
            if(LastCrossingSite.onFerry(q)&&p.isAlive()&&!p.isSpectator()&&!r.dead.contains(p.getUUID())){n.aboard.add(p.getUUID());LastCrossing.mark(p,"Aboard");LastCrossing.cue(p,LastCrossingTexts.ABOARD);
                // The station slides away into the dark while she crosses.
                HousePackets.send(p,new HouseFadePayload(60,TRANSIT-60,30));}
            else if(r.crew.containsKey(p.getUUID())&&!r.dead.contains(p.getUUID())){LastCrossing.mark(p,"LeftOnStation");LastCrossing.cue(p,LastCrossingTexts.LEFT_ON_STATION);}}
        for(var id:n.aboard){var p=LastCrossing.player(l,id);if(p==null)continue;for(var other:n.aboard)if(!other.equals(id)){var q=LastCrossing.player(l,other);if(q!=null)LastCrossing.addName(p,"AboardWith",LastCrossing.name(q));}}
        for(var p:LastCrossing.readers(l,b)){if(n.aboard.contains(p.getUUID()))continue;for(var id:n.aboard){var q=LastCrossing.player(l,id);if(q!=null&&LastCrossingNightTwo.turned(r,q))LastCrossing.addName(p,"SawTurnedAboard",LastCrossing.name(q));}}
    }
    /** She comes in at the far landing: whoever is still aboard steps off; the crossing is the crew's if one of them is clean. */
    static void arrive(ServerLevel l,Run r,Night n,List<ServerPlayer> here){
        n.arrived=true;var b=r.base;int i=0;boolean clean=false;
        for(var id:n.aboard){var p=LastCrossing.player(l,id);if(p==null)continue;
            var to=LastCrossing.world(b,LastCrossingSite.FAR_LANDING.get(i++%LastCrossingSite.FAR_LANDING.size()));p.teleportTo(l,to.x,to.y,to.z,180,0);
            LastCrossing.mark(p,"CrossedOver");if(LastCrossingNightTwo.turned(r,p))LastCrossing.mark(p,"CrossedTurned");
            if(!LastCrossing.own(p).getBoolean("Infected"))clean=true;}
        var a=LastCrossing.actor(l,r.vera);if(a!=null)LastCrossing.place(a,b,LastCrossingSite.FAR_VERA,0);
        LastCrossing.sound(l,b.offset(LastCrossingSite.FAR_LAMP),CrossingRegistry.RISE.get(),1F,1.4F);
        if(clean)end(l,r,n,FERRY);
        else for(var p:here)LastCrossing.cue(p,LastCrossingTexts.FERRY_NO_CLEAN);
    }

    // ------------------------------------------------------------------------------------------------ the gallery
    static void gallery(ServerLevel l,Run r,Night n,List<ServerPlayer> here){
        var b=r.base;var gate=Vec3.atCenterOf(b.offset(LastCrossingSite.FEED_GATE));
        if(n.gateBy!=null&&!n.barred){var p=LastCrossing.player(l,n.gateBy);
            if(p==null||!here.contains(p)||!p.isAlive()||p.position().distanceToSqr(gate)>2.5*2.5){if(p!=null){LastCrossing.mark(p,"LetGoGate");LastCrossing.cue(p,LastCrossingTexts.GATE_LET_GO);}n.gateBy=null;}}
        if(n.cutoff&&!n.drained){n.drain++;
            if(n.drain%(DRAIN/4)==0)LastCrossingSite.drainLayer(l,b,n.drain/(DRAIN/4)-1);
            if(n.drain>=DRAIN)drained(l,r,n,here);}
        if(n.drained&&!n.destroyed&&r.tick%10==0){
            n.defenders.removeIf(id->!(l.getEntity(id) instanceof Undertow u&&u.isAlive()));
            var src=Vec3.atBottomCenterOf(b.offset(LastCrossingSite.SOURCE));
            for(var id:n.defenders)if(l.getEntity(id) instanceof Undertow u&&u.position().distanceToSqr(src)>6*6){u.setTarget(null);u.getNavigation().moveTo(src.x,src.y,src.z,1.2);}
            for(var p:here)if(LastCrossingSite.inGallery(LastCrossing.rel(p.position(),b))||inChamber(LastCrossing.rel(p.position(),b)))n.inGallery.add(p.getUUID());}
        if(n.destroyed&&!n.mustered&&r.tick-n.destroyedAt>=MUSTER_WAIT)muster(l,r,n,here,null);
    }
    static boolean inChamber(Vec3 q){return q.x>=LastCrossingSite.CHAMBER_W&&q.x<LastCrossingSite.CHAMBER_E+1&&q.z>=LastCrossingSite.CHAMBER_N&&q.z<LastCrossingSite.CHAMBER_S+1&&q.y<-6.5&&q.y>-12;}
    /** The feed gate at night: held by one while another drops the bar across it. */
    static boolean gate(ServerPlayer p,int stage){
        var r=LastCrossing.runOf(p);if(r==null||!active(r))return false;var n=n(r);var l=p.serverLevel();var at=r.base.offset(LastCrossingSite.FEED_GATE);
        if(n.barred){LastCrossing.cue(p,n.cutoff?"Barred, and the cutoff shut.":LastCrossingTexts.BARRED_CUTOFF);return true;}
        boolean bar=LastCrossing.holds(p,"lockbar");
        if(n.gateBy==null){
            if(stage==0&&bar)return false;
            n.gateBy=p.getUUID();LastCrossing.mark(p,"HeldFeedGate");LastCrossing.job(p,"source");LastCrossing.sound(l,at,SoundEvents.IRON_DOOR_CLOSE,1F,.5F);
            LastCrossing.cue(p,LastCrossingNightTwo.turned(r,p)?LastCrossingTexts.GATE_HOLD_TURNED:LastCrossingTexts.GATE_HOLD3);return true;}
        if(p.getUUID().equals(n.gateBy)){LastCrossing.cue(p,"You're holding it. Somebody has to drop the bar.");return true;}
        if(stage!=1&&!bar){LastCrossing.cue(p,LastCrossingTexts.NO_BAR);return true;}
        if(bar)LastCrossing.drop(p,"lockbar");
        n.barred=true;var holder=LastCrossing.player(l,n.gateBy);n.gateBy=null;
        l.setBlock(at,LastCrossingSite.fit(Kind.FEED_GATE,Direction.WEST,2),Block.UPDATE_CLIENTS);LastCrossing.sound(l,at,SoundEvents.ANVIL_PLACE,.8F,.6F);
        LastCrossing.mark(p,"BarredGate");LastCrossing.job(p,"source");LastCrossing.cue(p,LastCrossingTexts.GATE_BARRED);
        if(holder!=null){LastCrossing.mark(holder,"HeldGateToBar");LastCrossing.addName(holder,"BarredWith",LastCrossing.name(p));LastCrossing.addName(p,"BarredWith",LastCrossing.name(holder));LastCrossing.cue(holder,LastCrossingTexts.FEED_GATE_EASED);}
        return true;
    }
    /** The upstream cutoff at night: it winds shut once the gate is barred, and the chamber starts to drain. */
    static boolean cutoff(ServerPlayer p,int stage){
        var r=LastCrossing.runOf(p);if(r==null||!active(r))return false;var n=n(r);var l=p.serverLevel();
        if(stage==1||n.cutoff){LastCrossing.cue(p,"The upstream cutoff, wound shut.");return true;}
        if(!n.barred){LastCrossing.cue(p,LastCrossingTexts.CUTOFF_UNBARRED);return true;}
        n.cutoff=true;l.setBlock(r.base.offset(LastCrossingSite.CUTOFF),LastCrossingSite.fit(Kind.CUTOFF,Direction.NORTH,1),Block.UPDATE_CLIENTS);
        LastCrossing.sound(l,r.base.offset(LastCrossingSite.CUTOFF),SoundEvents.IRON_TRAPDOOR_CLOSE,1F,.5F);
        LastCrossing.mark(p,"ClosedCutoff");LastCrossing.job(p,"source");LastCrossing.cue(p,LastCrossingTexts.CUTOFF_SHUT);
        return true;
    }
    /** The chamber down: the source on its floor, the gate drawn up, and the things holding it come off the walls. */
    static void drained(ServerLevel l,Run r,Night n,List<ServerPlayer> here){
        var b=r.base;n.drained=true;
        l.setBlock(b.offset(LastCrossingSite.SOURCE),LastCrossingSite.fit(Kind.SOURCE,Direction.WEST,1),Block.UPDATE_CLIENTS);
        LastCrossingSite.gateDrawn(l,b);LastCrossing.sound(l,b.offset(LastCrossingSite.FEED_GATE),SoundEvents.IRON_DOOR_OPEN,1F,.4F);
        int count=2+Math.max(1,r.crew.size())/2;
        for(int i=0;i<count;i++){var u=CrossingRegistry.UNDERTOW_TYPE.get().create(l);if(u==null)continue;
            var at=LastCrossing.world(b,new Vec3(43.5+(i%3),-11,-51.5-(i/3)*2.5));u.addTag(LabyrinthSpawnRules.ALLOWED_TAG);u.addTag("crossing_undertow");u.addTag("crossing_defender");
            u.moveTo(at.x,at.y,at.z,90,0);if(l.addFreshEntity(u))n.defenders.add(u.getUUID());}
        for(var p:here){var q=LastCrossing.rel(p.position(),b);if(LastCrossingSite.inGallery(q)||inChamber(q)){LastCrossing.mark(p,"SawSourceBare");LastCrossing.cue(p,LastCrossingTexts.DRAINED);}}
    }
    /** A blow on the source: nothing lands while anything still covers it. */
    public static boolean strike(ServerPlayer p){
        var r=LastCrossing.runOf(p);if(r==null||!active(r))return false;var n=n(r);var l=p.serverLevel();
        if(!n.drained||n.destroyed)return false;
        n.defenders.removeIf(id->!(l.getEntity(id) instanceof Undertow u&&u.isAlive()));
        if(!n.defenders.isEmpty()){LastCrossing.cue(p,LastCrossingTexts.COVERED);return true;}
        if(r.tick-n.hitAt<BLOW)return true;n.hitAt=r.tick;n.hits++;LastCrossing.mark(p,"StruckSource");
        var at=r.base.offset(LastCrossingSite.SOURCE);LastCrossing.sound(l,at,SoundEvents.SLIME_SQUISH,1.2F,.5F);
        l.sendParticles(ParticleTypes.SQUID_INK,at.getX()+.5,at.getY()+.6,at.getZ()+.5,8,.3,.3,.3,.02);
        if(n.hits<SOURCE_HITS){LastCrossing.cue(p,n.hits==1?LastCrossingTexts.SOURCE_STRUCK:LastCrossingTexts.SOURCE_AGAIN);return true;}
        destroy(l,r,n,p);return true;
    }
    static void destroy(ServerLevel l,Run r,Night n,ServerPlayer by){
        var b=r.base;n.destroyed=true;n.destroyedAt=r.tick;
        l.setBlock(b.offset(LastCrossingSite.SOURCE),LastCrossingSite.fit(Kind.SOURCE,Direction.WEST,2),Block.UPDATE_CLIENTS);
        LastCrossing.sound(l,b.offset(LastCrossingSite.SOURCE),CrossingRegistry.SURGE.get(),1F,.6F);
        LastCrossing.mark(by,"DestroyedSource");
        var n2=r.night2;
        for(var p:LastCrossing.readers(l,b)){var q=LastCrossing.rel(p.position(),b);if(LastCrossingSite.inGallery(q)||inChamber(q)){LastCrossing.mark(p,"SawSourceDie");n.inGallery.add(p.getUUID());}
            // Its death lets go of the turned, wherever they are.
            if(n2.turned.contains(p.getUUID())){p.removeEffect(MobEffects.WATER_BREATHING);p.removeEffect(MobEffects.NIGHT_VISION);LastCrossing.mark(p,"ReleasedByFight");LastCrossing.cue(p,LastCrossingTexts.RELEASED);}}
        n2.turned.clear();n2.choking.clear();LastCrossing.sync(l,r);
        for(var id:n2.mobs)if(l.getEntity(id) instanceof Undertow u)LastCrossingNight.sink(l,u);n2.mobs.clear();
        if(cleanAlive(l,r))end(l,r,n,GALLERY);
    }
    /** The station bell rung once the source is dead: the muster. */
    static void bell(ServerPlayer p){var r=LastCrossing.runOf(p);if(r==null||!active(r))return;var n=n(r);if(n.destroyed&&!n.mustered)muster(p.serverLevel(),r,n,LastCrossing.readers(p.serverLevel(),r.base),p);}
    static void muster(ServerLevel l,Run r,Night n,List<ServerPlayer> here,@Nullable ServerPlayer by){
        n.mustered=true;if(by!=null)LastCrossing.mark(by,"RangMuster");
        LastCrossing.speak(l,r,LastCrossingTexts.MUSTER,64);
        for(var id:n.inGallery){var p=LastCrossing.player(l,id);if(p==null||!p.isAlive()||r.dead.contains(id))continue;LastCrossing.mark(p,"OutOfGallery");}
        for(var p:here)LastCrossing.mark(p,"HeardMuster");
        if(n.ending==GALLERY){n.beat=1;n.endingAt=r.tick;LastCrossingNameplate.breakPlate(l,r,n);}
    }

    // ------------------------------------------------------------------------------------------------ the master bypass and the bargain
    /** The master bypass at night: past the rise, a reader who breathes water can turn it, and hold it open. */
    static boolean masterUse(ServerPlayer p,int stage){
        var r=LastCrossing.runOf(p);if(r==null||!active(r)||!n(r).risen)return false;var n=n(r);var l=p.serverLevel();var at=r.base.offset(LastCrossingSite.MASTER);
        if(!LastCrossingNightTwo.turned(r,p)){LastCrossing.mark(p,"SawMaster");LastCrossing.cue(p,LastCrossingTexts.MASTER_WONT);return true;}
        if(n.denied){LastCrossing.cue(p,LastCrossingTexts.MASTER_DENIED_LOOK);return true;}
        if(n.masterBy!=null&&!p.getUUID().equals(n.masterBy)){LastCrossing.cue(p,"Someone's already got it open.");return true;}
        if(p.getUUID().equals(n.masterBy)){LastCrossing.cue(p,LastCrossingTexts.MASTER_HOLDING);return true;}
        n.masterBy=p.getUUID();n.masterHeld=0;n.masterOpen=true;r.night2.landing=true;
        l.setBlock(at,LastCrossingSite.fit(Kind.MASTER,Direction.EAST,1),Block.UPDATE_CLIENTS);LastCrossing.sound(l,at,SoundEvents.IRON_TRAPDOOR_OPEN,1F,.4F);
        LastCrossing.mark(p,"OpenedMaster");LastCrossing.cue(p,LastCrossingTexts.MASTER_OPENED);
        for(var q:LastCrossing.readers(l,r.base))if(q!=p&&LastCrossing.sees(q,Vec3.atCenterOf(at),12,.3))LastCrossing.addName(q,"SawMasterOpened",LastCrossing.name(p));
        return true;
    }
    static void master(ServerLevel l,Run r,Night n,List<ServerPlayer> here){
        if(n.masterBy==null)return;var p=LastCrossing.player(l,n.masterBy);
        if(p==null||!here.contains(p)||!p.isAlive()||p.position().distanceToSqr(Vec3.atCenterOf(r.base.offset(LastCrossingSite.MASTER)))>2.5*2.5){
            if(n.bargained){n.masterBy=null;return;}
            if(p!=null){LastCrossing.mark(p,"LetGoMaster");LastCrossing.cue(p,LastCrossingTexts.MASTER_SWINGS_SHUT);}
            shutMaster(l,r,n);return;}
        n.masterHeld++;
        // The pipes hammer with water going the wrong way.
        if(r.tick%20==0){var mouths=List.of(LastCrossingSite.PIPE_A_UP,LastCrossingSite.PIPE_A_DOWN,LastCrossingSite.PIPE_B_UP,LastCrossingSite.PIPE_B_DOWN);
            for(var m:mouths)LastCrossing.sound(l,r.base.offset(m),CrossingRegistry.KNOCK.get(),1F,.6F);
            for(var q:here){if(q==p||LastCrossing.own(q).getBoolean("HeardHammer"))continue;boolean near=false;for(var m:mouths)near|=q.position().distanceToSqr(Vec3.atCenterOf(r.base.offset(m)))<8*8;
                if(near||LastCrossingSite.atRamp(LastCrossing.rel(q.position(),r.base))){LastCrossing.mark(q,"HeardHammer");LastCrossing.cue(q,LastCrossingTexts.HAMMER);}}}
        if(!n.flooded&&n.masterHeld>=BARGAIN_HOLD/2){n.flooded=true;LastCrossingSite.floodApproach(l,r.base);for(var q:here)if(LastCrossingSite.atRamp(LastCrossing.rel(q.position(),r.base))){LastCrossing.mark(q,"SawApproachFlood");LastCrossing.cue(q,LastCrossingTexts.APPROACH_FLOOD);}}
        if(!n.bargained&&n.masterHeld>=BARGAIN_HOLD)bargain(l,r,n,p);
    }
    static void shutMaster(ServerLevel l,Run r,Night n){
        n.masterBy=null;n.masterHeld=0;n.masterOpen=false;r.night2.landing=false;
        l.setBlock(r.base.offset(LastCrossingSite.MASTER),LastCrossingSite.fit(Kind.MASTER,Direction.EAST),Block.UPDATE_CLIENTS);
        if(n.flooded){n.flooded=false;LastCrossingSite.drainApproach(l,r.base);}
    }
    /** Held open till the water reached the ramp's approach: the bargain, for a carrier the plate exposed. */
    static void bargain(ServerLevel l,Run r,Night n,ServerPlayer p){
        n.bargained=true;n.bargainBy=p.getUUID();LastCrossing.mark(p,"SidedWithSource");
        var own=LastCrossing.own(p);
        if(own.getBoolean("Infected")&&own.getBoolean("Exposed")){giveBreath(p);LastCrossing.mark(p,"OtherBreath");
            var kept=LastCrossing.own(p);kept.putBoolean("OtherBreathKept",true);LastCrossing.save(p,kept);
            p.playNotifySound(CrossingRegistry.BREATH.get(),SoundSource.PLAYERS,1F,.7F);LastCrossing.cue(p,LastCrossingTexts.BREATH_TAKEN);}
        else LastCrossing.cue(p,LastCrossingTexts.SIDED);
    }
    /** The Other Breath, for good: a permanent bonus to the reader's air. */
    static void giveBreath(ServerPlayer p){
        var a=p.getAttribute(Attributes.OXYGEN_BONUS);if(a==null||a.getModifier(OTHER_BREATH)!=null)return;
        a.addPermanentModifier(new AttributeModifier(OTHER_BREATH,OTHER_BREATH_BONUS,AttributeModifier.Operation.ADD_VALUE));
    }
    /** Earned once, kept: put back on a reader who comes back from death or from the End. */
    @SubscribeEvent public static void clone(PlayerEvent.Clone e){
        if(e.getEntity() instanceof ServerPlayer p&&LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),LastCrossing.PLACE).getBoolean("OtherBreathKept"))giveBreath(p);
    }
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){
        if(e.getEntity() instanceof ServerPlayer p&&LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),LastCrossing.PLACE).getBoolean("OtherBreathKept"))giveBreath(p);
    }
    /** The downstream sluice at night: wound shut, it turns the master's water. Before the bargain is done it denies it. */
    static boolean downstream(ServerPlayer p,int stage){
        var r=LastCrossing.runOf(p);if(r==null||!active(r))return false;var n=n(r);var l=p.serverLevel();var at=r.base.offset(LastCrossingSite.DOWNSTREAM);
        if(stage==1){LastCrossing.cue(p,"The downstream sluice, wound shut.");return true;}
        l.setBlock(at,LastCrossingSite.fit(Kind.DOWNSTREAM,Direction.WEST,1),Block.UPDATE_CLIENTS);LastCrossing.sound(l,at,SoundEvents.IRON_TRAPDOOR_CLOSE,1F,.5F);
        LastCrossing.mark(p,"ShutDownstream");
        if(n.masterOpen&&!n.bargained){var holder=LastCrossing.player(l,n.masterBy);n.denied=true;
            if(holder!=null){LastCrossing.mark(holder,"BargainDenied");LastCrossing.addName(holder,"DeniedBy",LastCrossing.name(p));LastCrossing.cue(holder,LastCrossingTexts.DENIED);}
            LastCrossing.mark(p,"DeniedBargain");shutMaster(l,r,n);n.denied=true;LastCrossing.cue(p,LastCrossingTexts.DOWNSTREAM_IN_TIME);}
        else if(n.bargained){r.night2.landing=false;if(n.flooded){n.flooded=false;LastCrossingSite.drainApproach(l,r.base);}LastCrossing.mark(p,"DrainedLanding");LastCrossing.cue(p,LastCrossingTexts.DOWNSTREAM_LATE);}
        else LastCrossing.cue(p,LastCrossingTexts.DOWNSTREAM_NOTHING);
        return true;
    }

    // ------------------------------------------------------------------------------------------------ the source takes it
    /** Whether anyone clean of the crew is still standing at the station (or already across). A clean reader who has just dropped out
     * still counts for a little while. */
    static boolean cleanAlive(ServerLevel l,Run r){
        var readers=LastCrossing.readers(l,r.base);
        for(var id:r.crew.keySet()){if(r.dead.contains(id))continue;var p=LastCrossing.player(l,id);
            if(p==null){var seen=r.night3.lastClean.get(id);if(seen!=null&&r.tick-seen<GRACE)return true;continue;}
            if(!p.isAlive()||p.isSpectator()||!readers.contains(p))continue;if(!LastCrossing.own(p).getBoolean("Infected"))return true;}
        return false;
    }
    static void source(ServerLevel l,Run r,Night n,List<ServerPlayer> here){
        boolean transit=n.released&&!n.arrived;if(transit)return;
        if(r.tick%20==0&&!cleanAlive(l,r)){end(l,r,n,SOURCE);return;}
        if(r.night2.tick>=NIGHT3_TICKS)end(l,r,n,SOURCE);
    }

    // ------------------------------------------------------------------------------------------------ the endings
    static void end(ServerLevel l,Run r,Night n,int kind){
        if(n.ending!=NONE)return;n.ending=kind;n.endingAt=r.tick;n.beat=0;
        String key=kind==FERRY?"ferry":kind==GALLERY?"gallery":"source";
        for(var p:LastCrossing.readers(l,r.base))if(r.crew.containsKey(p.getUUID())){var own=LastCrossing.own(p);own.putString("Ending",key);LastCrossing.save(p,own);}
        switch(kind){
            case FERRY->{n.beat=1;n.farCalling=-1;n.farTicks=0;LastCrossing.speak(l,r,LastCrossingTexts.FAR_ROLL,64);}
            case GALLERY->LastCrossing.speak(l,r,LastCrossingTexts.STILL_WATER,64);
            default->{n.beat=1;n.farCalling=-1;n.farTicks=0;
                // Everyone is brought to the far landing, where the ferry comes in anyway.
                gather(l,r,n);LastCrossing.speak(l,r,LastCrossingTexts.SOURCE_DOCKS,64);}
        }
    }
    /** Everyone still at the station brought over to the far landing, and Vera with them. */
    static void gather(ServerLevel l,Run r,Night n){
        var b=r.base;int i=0;
        for(var p:LastCrossing.readers(l,b)){var q=LastCrossing.rel(p.position(),b);if(LastCrossingSite.farBank(q))continue;
            var to=LastCrossing.world(b,LastCrossingSite.FAR_LANDING.get(i++%LastCrossingSite.FAR_LANDING.size()));HousePackets.send(p,new HouseFadePayload(4,20,24));p.teleportTo(l,to.x,to.y,to.z,180,0);LastCrossing.mark(p,"BroughtAcross");}
        var a=LastCrossing.actor(l,r.vera);if(a!=null){LastCrossing.place(a,b,LastCrossingSite.FAR_VERA,0);r.route.clear();}
    }
    static void ending(ServerLevel l,Run r,Night n,List<ServerPlayer> here){
        if(n.ended)return;
        switch(n.ending){
            case FERRY->{
                // The roll on the far bank, then the plate broken and the spare named, then morning.
                if(n.beat==1)farRoll(l,r,n,here,false);
                else if(n.beat==PIECES){LastCrossingNameplate.tick(l,r,n);if(n.spareSettled&&r.tick-n.settledAt>=BEAT)morning(l,r,n,here);}}
            case SOURCE->farRoll(l,r,n,here,true);
            case GALLERY->{
                // The muster: the bell rung by one of the crew, or by Vera after a while; the plate broken there and the spare named; then
                // morning.
                if(!n.mustered&&r.tick-n.destroyedAt>=MUSTER_WAIT)muster(l,r,n,here,null);
                if(n.mustered&&n.beat==1){LastCrossingNameplate.tick(l,r,n);if(n.spareSettled&&r.tick-Math.max(n.endingAt,n.settledAt)>=BEAT)morning(l,r,n,here);}}
            default->{}
        }
    }
    /** The roll on the far bank. On the crew's crossing, whoever is there answers, with room left for a name that goes unanswered; when the
     * source has taken it, every name gets an answer, Ellis's too. */
    static void farRoll(ServerLevel l,Run r,Night n,List<ServerPlayer> here,boolean everyone){
        if(n.beat!=1)return;n.farTicks++;var names=new ArrayList<>(r.crew.entrySet());
        if(n.farCalling<0){if(n.farTicks>=40)farNext(l,r,n,names,everyone);return;}
        if(n.farCalling<names.size()){var e=names.get(n.farCalling);
            if(n.farAnswered.contains(e.getKey())||n.farTicks>=FAR_ANSWER){
                if(!n.farAnswered.contains(e.getKey())){var p=LastCrossing.player(l,e.getKey());if(p!=null)LastCrossing.mark(p,"UnansweredFar");
                    for(var q:here)if(!q.getUUID().equals(e.getKey()))LastCrossing.addName(q,"HeardUnanswered",e.getValue());}
                farNext(l,r,n,names,everyone);}
            else if(everyone&&n.farTicks==30){voice(l,r,"Here.");n.farAnswered.add(e.getKey());
                for(var q:here)LastCrossing.addName(q,"HeardAnsweredFor",e.getValue());}
            return;}
        if(n.farCalling==names.size()){
            // Ellis, last. On the source's crossing the boat answers for Ellis as for everyone; on the crew's, Vera waits on the name, and
            // a long breath later something answers from the far end of the boat.
            if(everyone&&n.farTicks==30){voice(l,r,"Here.");for(var q:here)LastCrossing.mark(q,"HeardEllisFar");}
            if(!everyone&&n.farTicks==LATE_ANSWER){voice(l,r,"Here.");for(var q:here)if(LastCrossingSite.farBank(LastCrossing.rel(q.position(),r.base)))LastCrossing.mark(q,"HeardEllisLate");}
            if(n.farTicks>=(everyone?FAR_ANSWER:ELLIS_WAIT)){n.farCalling++;n.farTicks=0;
                if(everyone)morning(l,r,n,here);else{n.beat=PIECES;LastCrossingNameplate.breakPlate(l,r,n);}}
            return;}
    }
    static void farNext(ServerLevel l,Run r,Night n,List<Map.Entry<UUID,String>> names,boolean everyone){
        n.farCalling++;n.farTicks=0;
        if(n.farCalling<names.size()){var e=names.get(n.farCalling);
            // On the crew's crossing Vera calls it; when the source has it, someone aboard does, in her order.
            if(everyone)call(l,r,e.getValue()+".");else LastCrossing.speak(l,r,e.getValue()+".",64);
            var p=LastCrossing.player(l,e.getKey());
            if(!everyone&&p!=null&&!r.dead.contains(e.getKey())&&LastCrossing.readers(l,r.base).contains(p)&&LastCrossingSite.farBank(LastCrossing.rel(p.position(),r.base))){
                Talk.begin(p,LastCrossing.TALK,"Vera");Talk.line(p,"Vera",e.getValue()+".");Talk.ask(p,Talk.reply("Here.",LastCrossingNightThree::farAnswer));}}
        else if(n.farCalling==names.size()){if(everyone)call(l,r,LastCrossingTexts.ELLIS);else LastCrossing.speak(l,r,LastCrossingTexts.ELLIS,64);}
    }
    static void farAnswer(ServerPlayer p){Talk.close(p);var r=LastCrossing.runOf(p);if(r==null)return;r.night3.farAnswered.add(p.getUUID());LastCrossing.mark(p,"AnsweredFar");}
    /** A voice from the far end of the boat, nobody's: on the action bar of everyone at the far landing. */
    static void voice(ServerLevel l,Run r,String line){aboard(l,r,LastCrossingTexts.FROM_THE_BOAT,line);}
    /** Someone aboard the ferry that came in by itself, calling the roll. */
    static void call(ServerLevel l,Run r,String line){aboard(l,r,LastCrossingTexts.BOAT_CALLS,line);}
    static void aboard(ServerLevel l,Run r,String who,String line){
        for(var p:LastCrossing.readers(l,r.base))if(LastCrossingSite.farBank(LastCrossing.rel(p.position(),r.base)))p.displayClientMessage(Component.literal(who+" “"+line+"”").withStyle(net.minecraft.ChatFormatting.GRAY),true);
    }
    /** Morning on the far bank: the turned come back to themselves, everyone is brought across, and the crossing is over. */
    static void morning(ServerLevel l,Run r,Night n,List<ServerPlayer> here){
        n.ended=true;n.beat=2;gather(l,r,n);
        var n2=r.night2;
        for(var id:n2.mobs)if(l.getEntity(id) instanceof Undertow u)LastCrossingNight.sink(l,u);n2.mobs.clear();
        for(var id:n.defenders)if(l.getEntity(id) instanceof Undertow u)LastCrossingNight.sink(l,u);n.defenders.clear();
        for(var p:LastCrossing.readers(l,r.base)){
            if(n2.turned.contains(p.getUUID())){p.removeEffect(MobEffects.WATER_BREATHING);p.removeEffect(MobEffects.NIGHT_VISION);LastCrossing.mark(p,"BackAtMorning");}
            if(r.crew.containsKey(p.getUUID()))LastCrossing.mark(p,"NightThree");}
        n2.turned.clear();n2.choking.clear();r.phase=LastCrossing.END;
        LastCrossing.speak(l,r,n.ending==FERRY?LastCrossingTexts.MORNING_FERRY:n.ending==GALLERY?LastCrossingTexts.MORNING_GALLERY:LastCrossingTexts.MORNING_SOURCE,64);
        LastCrossing.sync(l,r);
        resolveAll(l,r,n);
    }

    // ------------------------------------------------------------------------------------------------ 0.4.357: each reader's account
    static String outcome(Night n){return n.ending==FERRY?"ferry":n.ending==GALLERY?"gallery":"source";}
    /** Morning: everyone on the roll resolved, the dead and the absent included; a reader who isn't online gets theirs when they are back. */
    static void resolveAll(ServerLevel l,Run r,Night n){
        String outcome=outcome(n);var d=LabyrinthData.get(l.getServer());
        for(var id:r.crew.keySet()){var p=LastCrossing.player(l,id);
            if(p!=null){resolve(p,outcome);continue;}
            var own=LiteraryVignettes.personal(d,id,LastCrossing.PLACE);if(own.getBoolean("Completed"))continue;
            own.putString("OwedEnding",outcome);if(own.getString("Ending").isEmpty())own.putString("Ending",outcome);LiteraryVignettes.save(d,id,LastCrossing.PLACE,own);}
    }
    /** One reader's crossing, finished: the Witness source, and their own account from their own record. */
    static void resolve(ServerPlayer p,String outcome){
        var own=LastCrossing.own(p);if(own.getBoolean("Completed"))return;
        if(own.getString("Ending").isEmpty())own.putString("Ending",outcome);
        own.putString("Outcome",own.getString("Ending"));own.putBoolean("Ready",true);own.putBoolean("Completed",true);own.putBoolean("Taken_Ending",true);own.putInt("Beat",own.getInt("Visit"));own.remove("OwedEnding");
        LastCrossing.save(p,own);
        WitnessAccount.resolve(p,WitnessAccount.Story.LAST_CROSSING,own.getString("Outcome"));
        LabyrinthData.get(p.server).setCompleted(LastCrossing.PLACE.id(),true);
        LiteraryVignettes.reward(p,LastCrossing.PLACE,own,"Ending",LastCrossingTexts.account(p.getGameProfile().getName(),own));
    }
    /** Back after the crossing ended without them: their result. */
    @SubscribeEvent public static void owed(PlayerEvent.PlayerLoggedInEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;var own=LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),LastCrossing.PLACE);
        if(own.contains("OwedEnding"))resolve(p,own.getString("OwedEnding"));
    }

    // ------------------------------------------------------------------------------------------------ events
    /** A blow on the source, once the chamber is down. */
    @SubscribeEvent public static void hit(PlayerInteractEvent.LeftClickBlock e){
        if(!(e.getEntity() instanceof ServerPlayer p)||e.getAction()!=PlayerInteractEvent.LeftClickBlock.Action.START)return;var r=LastCrossing.runOf(p);if(r==null)return;
        if(!CrossingBlock.is(p.serverLevel().getBlockState(e.getPos()),Kind.SOURCE))return;
        if(strike(p))e.setCanceled(true);
    }

    // ------------------------------------------------------------------------------------------------ the station set back
    static void reset(ServerLevel l,Run r){
        var area=new AABB(r.base.offset(LastCrossingSite.WEST,LastCrossingSite.LOW,LastCrossingSite.NORTH)).minmax(new AABB(r.base.offset(LastCrossingSite.EAST,LastCrossingSite.HIGH,1)));
        for(var u:l.getEntitiesOfClass(Undertow.class,area,u->u.getTags().contains("crossing_defender")))u.discard();
        r.night3=new Night();
    }
}
