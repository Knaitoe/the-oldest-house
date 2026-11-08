package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.CrossingBlock.Kind;
import io.github.knaitoe.theoldesthouse.labyrinth.LastCrossing.Run;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.joml.Vector3f;

/**
 * 0.4.354, The Last Crossing, Night Two (docs/LAST_CROSSING_0_4_354.md), from the owner's design: "At sunset, the infected reader turns where
 * they are standing. Anyone with a view can witness it. Someone who went below or shut a door first may be absent from the first roll call
 * and back in human shape at dawn. The crew must hold the winch through a full pull while mobs come up the service channels."
 *
 * <p>At the second dusk the Night One carrier turns where they stand, and whoever has them in view sees whose face it is. Vera goes over to
 * the cable house and calls the roll from its doorway; anyone below, or shut behind a door, doesn't hear it and is noted absent. Then the
 * pull: someone takes the brake off and holds it while the drum hauls; it binds at the jammed fairlead halfway (cleared from above with the
 * marlinspike, or in one shove from the flooded passage under it, where only a turned reader can breathe) and on a loose roller; let go,
 * the drum runs back until someone catches the brake, and if nobody does it throws the rollers and the pull has failed. The things from the
 * water come up over the spillway's banks, and through any branch left open: the floor grate behind the brake gives them a way in below
 * the barred door, unless someone stands on it or pins it shut.
 *
 * <p>The turned reader breathes water and can go where water goes: down through the cable house's hatch into the passage, through a wet
 * grate into its branch, up through the floor grate; wherever they come up onto a dry floor they leave wet prints. Their bare hands are
 * claws (the slasher's splatter, a tear left in the cloth that the dawn doesn't mend). Crouched a few seconds anywhere, they call: up to
 * two of the things turn toward them, or, called from the water, go down into the branch and come up the floor grate after it unless it's
 * shut. Holding a live grate shut chokes them; a friend pinning it takes the load off. Within the plate's reach their gills clamp shut and
 * the call catches. Their speech comes out broken, in chat and aloud (LastCrossingSpeech). The things grapple a clean reader caught on
 * their own; a friend's blow breaks it, or a hand pulls them up onto the dry ledge. In a crew of six with all six still standing, a grapple
 * that runs its course fills the second slot: that reader changes at the last dusk. At dawn the things go back into the water, the turned
 * come back to themselves where they are, and the station holds in the morning light: Day Three is the last slice.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class LastCrossingNightTwo {
    private LastCrossingNightTwo(){}
    /** Ticks, on the night's own clock unless noted: the turn after dusk and how long it takes; the latest the roll waits for Vera at the
     * doorway; how long she waits for an answer, and after a name said again; the latest the pull is called; how long it takes lined up on
     * the marks and off them; how long the drum runs back before it throws the rollers (station ticks); the things from the water, the first
     * and how often after, and when they stop coming; how long a turned reader crouches to call and how long between calls; how long the
     * things called keep coming; how long the things called into the branch wait below; how long a grapple holds and the blows that break
     * it; how long a throat holds before it closes, and how often it hurts after; how many levers of the marlinspike free the jam. */
    public static final int TURN_AT=40,TURNING=60,ROLL_BY=500,ANSWER=100,AGAIN=60,PULL_BY=1400,PULL_LINED=1200,PULL_UNLINED=1800,RUN_BACK=80,FIRST_SPAWN=500,
        SPAWN_EVERY=300,SPAWN_STOP=600,CALL_CROUCH=60,CALL_COOLDOWN=900,CALL_DRAW=240,BELOW_WAIT=100,GRAPPLE=100,GRAPPLE_BLOWS=3,CHOKE_WARN=30,CHOKE_EVERY=20,
        JAM_LEVERS=8;
    /** The roll's earshot and the call's reach, in blocks; the plate's reach over a turned reader, and where its pressure first shows. */
    static final double EARSHOT=48,CALL_REACH=32,WARD=4,WARD_EDGE=5.5;
    /** A throat closed on a gate never takes a reader below one heart; the plate never below two. */
    static final float GATE_FLOOR=2F,PLATE_FLOOR=4F;
    /** Pull states: not called; called, waiting for hands on the brake; hauling; binding; running back; taken up; failed. */
    public static final int NOT_CALLED=0,CALLED=1,HAULING=2,BINDING=3,RUNNING_BACK=4,PULLED=5,FAILED=6;

    record Drawn(Vec3 to,long until){}
    static final class Night {
        int tick;boolean begun,dawn;
        /** The turn: who is turning and since when, whether anyone saw it, and who has turned. */
        @Nullable UUID turning;long turnAt=-1;boolean turnSeen;final Set<UUID> turned=new LinkedHashSet<>();
        /** The roll from the cable house's doorway: which name, how long, called twice, who answered, and done. */
        boolean rollStarted,rollDone;int calling=-1,callTicks;boolean calledTwice;long answerAt=-1;final Set<UUID> answered=new HashSet<>();final List<String> absent=new ArrayList<>();
        /** The pull: its state, how far it has hauled of how far it must, who is on the brake, when it was let go, the jam and its levers. */
        int pull,progress,need;@Nullable UUID brakeBy;long dropAt=-1;long pullAt=-1;boolean jamCleared,threw;int levers;long leverAt=-1;String binding="";
        final LinkedHashSet<UUID> pullHands=new LinkedHashSet<>();
        /** The floor grate over the winch spur held shut and by whom, pinned; and each turned reader's closing throat. */
        @Nullable UUID gateBy;boolean pinned;final Map<UUID,Integer> choking=new HashMap<>();final Map<UUID,Integer> edge=new HashMap<>();
        /** The night call: each turned reader's crouch and last call; the things drawn and where to; those called down into the branch. */
        final Map<UUID,Integer> crouch=new HashMap<>();final Map<UUID,Long> calledAt=new HashMap<>();final Map<UUID,Drawn> drawn=new HashMap<>();
        int below;long belowAt=-1;@Nullable UUID belowBy;
        /** The things from the water up tonight; how many have come, and when the last; the cable house fouled. */
        final Set<UUID> mobs=new HashSet<>();int spawned,lastSpawn=-1,fouling;boolean fouled;
        /** The grapple: who, by what, since when, where they're held, the blows on it; the second slot once filled. */
        @Nullable UUID grappled,grappler;long grappleAt=-1;Vec3 grapplePos=Vec3.ZERO;int blows;@Nullable UUID secondSlot;
        /** Tears in the cloth (kept through dawn), and wet prints left on dry floors. */
        final Set<UUID> torn=new LinkedHashSet<>();final List<BlockPos> prints=new ArrayList<>();
    }

    // ------------------------------------------------------------------------------------------------ for the station and its tests
    static boolean active(Run r){return r.phase==LastCrossing.DUSK&&r.dayNo>=2&&!r.night2.dawn;}
    /** A reader turned tonight, now. */
    static boolean turned(Run r,ServerPlayer p){return active(r)&&r.night2.turned.contains(p.getUUID());}
    public static boolean turnedNow(ServerPlayer p){var r=LastCrossing.runOf(p);return r!=null&&turned(r,p);}
    /** One of the things from the water drawn by a call: it goes where it was called and takes no one meanwhile. */
    public static boolean drawn(Undertow u){
        for(var r:LastCrossing.runs())if(r.night2.drawn.containsKey(u.getUUID()))return true;
        return false;
    }
    public static int pull(BlockPos b){var r=LastCrossing.run(b);return r==null?-1:r.night2.pull;}
    public static int progress(BlockPos b){var r=LastCrossing.run(b);return r==null?0:r.night2.progress;}
    public static int night(BlockPos b){var r=LastCrossing.run(b);return r==null?-1:r.night2.tick;}
    public static List<UUID> turned(BlockPos b){var r=LastCrossing.run(b);return r==null?List.of():List.copyOf(r.night2.turned);}
    public static List<UUID> torn(BlockPos b){var r=LastCrossing.run(b);return r==null?List.of():List.copyOf(r.night2.torn);}
    @Nullable public static UUID secondSlot(BlockPos b){var r=LastCrossing.run(b);return r==null?null:r.night2.secondSlot;}
    @Nullable public static UUID grappled(BlockPos b){var r=LastCrossing.run(b);return r==null?null:r.night2.grappled;}
    public static boolean pinned(BlockPos b){var r=LastCrossing.run(b);return r!=null&&r.night2.pinned;}
    public static boolean jamCleared(BlockPos b){var r=LastCrossing.run(b);return r!=null&&r.night2.jamCleared;}
    public static int below(BlockPos b){var r=LastCrossing.run(b);return r==null?0:r.night2.below;}
    public static List<UUID> mobs(BlockPos b){var r=LastCrossing.run(b);return r==null?List.of():List.copyOf(r.night2.mobs);}
    /** For tests: the drum hauled on by this many ticks while it is hauling (it still binds where it binds). */
    public static void haul(BlockPos b,int ticks){var r=LastCrossing.run(b);if(r!=null&&r.night2.pull==HAULING)r.night2.progress=Math.min(r.night2.need-1,r.night2.progress+ticks);}

    // ------------------------------------------------------------------------------------------------ the night's clock
    /** The second dusk: Vera goes over the spillway to the cable house, to call the roll from its doorway. */
    static void begin(ServerLevel l,Run r){
        var n=r.night2;n.begun=true;
        LastCrossing.route(r,new Vec3(13.5,0,-35.5),new Vec3(26.5,0,-35.5),new Vec3(26.5,0,-50.5),new Vec3(39.5,0,-50.5),new Vec3(45.5,0,-50.5),LastCrossingSite.VERA_DOOR);
    }
    static void tick(ServerLevel l,Run r,List<ServerPlayer> here){
        var n=r.night2;if(!n.begun)begin(l,r);n.tick++;
        turn(l,r,n,here);
        roll(l,r,n,here);
        pull(l,r,n,here);
        gate(l,r,n,here);
        calls(l,r,n,here);
        below(l,r,n,here);
        mobs(l,r,n,here);
        grapple(l,r,n,here);
        bodies(l,r,n,here);
        secondBeat(l,r,n,here);
        if(r.tick%10==0)for(var p:here)prints(l,r,n,p);
        if(n.tick>=LastCrossing.NIGHT_TICKS)dawn(l,r,n,here);
    }

    // ------------------------------------------------------------------------------------------------ the turn
    /** At sunset the Night One carrier turns where they stand. One who isn't at the station turns when they come back to it. */
    static void turn(ServerLevel l,Run r,Night n,List<ServerPlayer> here){
        if(n.tick<TURN_AT)return;
        var c=LastCrossing.player(l,r.night.carrier);
        if(c==null||r.dead.contains(c.getUUID())||n.turned.contains(c.getUUID())||!here.contains(c)||c.isSpectator())return;
        if(n.turning==null){n.turning=c.getUUID();n.turnAt=r.tick;n.turnSeen=false;
            LastCrossing.sound(l,c.blockPosition(),CrossingRegistry.TURN.get(),1F,1F);
            c.playNotifySound(CrossingRegistry.BREATH.get(),SoundSource.PLAYERS,1F,.8F);LastCrossing.cue(c,LastCrossingTexts.TURN_START);}
        if(!c.getUUID().equals(n.turning))return;
        if(r.tick%4==0){l.sendParticles(ParticleTypes.DRIPPING_WATER,c.getX(),c.getY()+1.2,c.getZ(),6,.3,.5,.3,0);
            l.sendParticles(new DustParticleOptions(new Vector3f(.2F,.3F,.28F),1.2F),c.getX(),c.getY()+1,c.getZ(),4,.3,.5,.3,0);}
        // Anyone with a view of them while they turn knows whose face it was.
        if(r.tick%5==0)for(var w:here){if(w==c||w.isSpectator()||!LastCrossing.sees(w,c.getEyePosition(),20,.3))continue;n.turnSeen=true;
            var own=LastCrossing.own(w);if(Arrays.asList(own.getString("SawTurn").split(",")).contains(LastCrossing.name(c)))continue;
            LastCrossing.addName(w,"SawTurn",LastCrossing.name(c));
            LastCrossing.cue(w,LastCrossing.name(c)+" doubles over where they stand. When the head comes up the skin is slick and running wet, the jaw sits wrong, and the face is still "+LastCrossing.name(c)+"'s.");}
        if(r.tick-n.turnAt<TURNING)return;
        n.turning=null;n.turned.add(c.getUUID());
        var own=LastCrossing.own(c);own.putBoolean("Turned",true);own.putString("TurnedWhere",LastCrossing.placeName(LastCrossing.rel(c.position(),r.base)));
        if(!n.turnSeen)own.putBoolean("TurnedUnseen",true);LastCrossing.save(c,own);
        LastCrossing.cue(c,LastCrossingTexts.TURNED);
        LastCrossing.sync(l,r);
    }

    // ------------------------------------------------------------------------------------------------ the roll from the doorway
    static void roll(ServerLevel l,Run r,Night n,List<ServerPlayer> here){
        if(n.rollDone)return;
        if(!n.rollStarted){
            // Vera reaches the cable house's door, opens it unless it's barred, and calls from the doorway.
            if(LastCrossing.walking(r)&&n.tick<ROLL_BY)return;
            n.rollStarted=true;openCableDoor(l,r);n.calling=-1;n.callTicks=0;
            LastCrossing.speak(l,r,LastCrossingTexts.NIGHT_ROLL,EARSHOT);return;}
        n.callTicks++;var names=rollNames(r);
        if(n.calling<0){if(n.callTicks>=30)next(l,r,n);return;}
        if(n.calling<names.size()){
            var e=names.get(n.calling);var id=e.getKey();
            if(n.answered.contains(id)){if(r.tick-n.answerAt>=20)next(l,r,n);}
            else if(!n.calledTwice&&n.callTicks>=ANSWER){n.calledTwice=true;n.callTicks=0;LastCrossing.speak(l,r,e.getValue()+"?",EARSHOT);offer(l,r,id,e.getValue()+"?");}
            else if(n.calledTwice&&n.callTicks>=AGAIN){
                var p=LastCrossing.player(l,id);if(p!=null){LastCrossing.mark(p,"AbsentNightRoll");if(LastCrossing.TALK.equals(Talk.key(p)))Talk.close(p);}
                n.absent.add(e.getValue());next(l,r,n);}
            return;}
        // Done: the names that went unanswered, noted by everyone who heard them go unanswered.
        n.rollDone=true;
        if(n.absent.isEmpty())LastCrossing.speak(l,r,LastCrossingTexts.ROLL_ALL,EARSHOT);
        else{LastCrossing.speak(l,r,LastCrossingTexts.ROLL_ABSENT,EARSHOT);
            for(var p:here){if(!hears(l,r,p))continue;for(var who:n.absent)if(!who.equals(LastCrossing.name(p)))LastCrossing.addName(p,"NotedAbsent",who);}}
        LastCrossing.route(r,new Vec3(45.5,0,-55.5),LastCrossingSite.VERA_PULL);
    }
    /** The names on the roll, less those who have died here: she knows. */
    static List<Map.Entry<UUID,String>> rollNames(Run r){var out=new ArrayList<Map.Entry<UUID,String>>();for(var e:r.crew.entrySet())if(!r.dead.contains(e.getKey()))out.add(e);return out;}
    static void next(ServerLevel l,Run r,Night n){
        n.calling++;n.callTicks=0;n.calledTwice=false;var names=rollNames(r);
        if(n.calling<names.size()){var e=names.get(n.calling);LastCrossing.speak(l,r,e.getValue()+".",EARSHOT);offer(l,r,e.getKey(),e.getValue()+".");}
    }
    /** A name called: its reader can answer only if they hear it. */
    static void offer(ServerLevel l,Run r,UUID id,String called){
        var p=LastCrossing.player(l,id);if(p==null||!LastCrossing.readers(l,r.base).contains(p)||!hears(l,r,p))return;
        Talk.begin(p,LastCrossing.TALK,"Vera");Talk.line(p,"Vera",called);Talk.ask(p,Talk.reply("Here.",LastCrossingNightTwo::answer));
    }
    static void answer(ServerPlayer p){
        Talk.close(p);var r=LastCrossing.runOf(p);if(r==null||!r.crew.containsKey(p.getUUID()))return;
        r.night2.answered.add(p.getUUID());r.night2.answerAt=r.tick;LastCrossing.mark(p,"AnsweredNightRoll");
    }
    /** Whether a reader hears Vera: near enough, not below ground, and not shut in behind a door of a building she isn't in. */
    static boolean hears(ServerLevel l,Run r,ServerPlayer p){
        var a=LastCrossing.actor(l,r.vera);var from=a==null?LastCrossing.world(r.base,LastCrossingSite.VERA_DOOR):a.position();
        if(p.position().distanceToSqr(from)>EARSHOT*EARSHOT)return false;
        var q=LastCrossing.rel(p.position(),r.base);var v=LastCrossing.rel(from,r.base);
        if(q.y<-1.5&&v.y>=-1.5)return false;
        var mine=building(q);var hers=building(v);
        if(!mine.equals(hers)&&(shut(l,r,mine)||shut(l,r,hers)))return false;
        return true;
    }
    static String building(Vec3 q){return LastCrossingSite.inOffice(q)?"office":LastCrossingSite.inGaugeRoom(q)?"pump":LastCrossingSite.inCableHouse(q)?"cable":"";}
    static boolean shut(ServerLevel l,Run r,String building){
        var door=switch(building){case "office"->LastCrossingSite.OFFICE_DOOR;case "pump"->LastCrossingSite.PUMP_DOOR;case "cable"->LastCrossingSite.CABLE_DOOR;default->null;};
        if(door==null)return false;var s=l.getBlockState(r.base.offset(door));
        return s.getBlock() instanceof DoorBlock&&!s.getValue(DoorBlock.OPEN);
    }
    static void openCableDoor(ServerLevel l,Run r){
        if(r.night.barred)return;var door=r.base.offset(LastCrossingSite.CABLE_DOOR);
        for(int y=0;y<=1;y++){var at=door.above(y);var s=l.getBlockState(at);if(s.getBlock() instanceof DoorBlock&&!s.getValue(DoorBlock.OPEN))l.setBlock(at,s.setValue(DoorBlock.OPEN,true),Block.UPDATE_CLIENTS);}
    }

    // ------------------------------------------------------------------------------------------------ the pull
    static void pull(ServerLevel l,Run r,Night n,List<ServerPlayer> here){
        var b=r.base;
        if(n.pull==NOT_CALLED){
            if(!(n.rollDone&&!LastCrossing.walking(r))&&n.tick<PULL_BY)return;
            if(n.pullAt<0){n.pullAt=r.tick;return;}
            if(r.tick-n.pullAt<60&&n.tick<PULL_BY)return;
            n.pull=CALLED;n.need=r.dayTwo.lined?PULL_LINED:PULL_UNLINED;LastCrossing.speak(l,r,r.dayTwo.lined?LastCrossingTexts.PULL_LINED:LastCrossingTexts.PULL_OFF,24);
            // The catch comes out of the drum's teeth for the pull.
            if(r.dayTwo.catchSet){r.dayTwo.catchSet=false;l.setBlock(b.offset(LastCrossingSite.CATCH),LastCrossingSite.fit(Kind.CATCH,Direction.WEST),Block.UPDATE_CLIENTS);}
            return;}
        if(n.pull>=PULLED)return;
        var brake=Vec3.atCenterOf(b.offset(LastCrossingSite.CABLE_BRAKE));
        var holder=LastCrossing.player(l,n.brakeBy);
        boolean held=holder!=null&&here.contains(holder)&&holder.isAlive()&&!holder.isSpectator()&&holder.position().distanceToSqr(brake)<2.5*2.5&&!holder.getUUID().equals(n.grappled);
        if(!held&&n.brakeBy!=null){if(holder!=null&&(n.pull==HAULING||n.pull==BINDING))LastCrossing.mark(holder,"LetGoBrake");n.brakeBy=null;
            if(n.pull==HAULING||n.pull==BINDING){n.pull=RUNNING_BACK;n.dropAt=r.tick;LastCrossing.speak(l,r,LastCrossingTexts.RUNNING_BACK,24);
                LastCrossing.sound(l,b.offset(LastCrossingSite.CABLE_BRAKE),CrossingRegistry.RATCHET.get(),1.2F,.6F);
                for(var p:here)if(LastCrossingSite.inCableHouse(LastCrossing.rel(p.position(),b))){LastCrossing.mark(p,"SawRunBack");LastCrossing.cue(p,"The brake's let go: the drum runs back, the cable paying out over the rollers.");}}}
        var want=LastCrossingSite.fit(Kind.BRAKE,Direction.WEST,held?1:0);var at=b.offset(LastCrossingSite.CABLE_BRAKE);
        if(!l.getBlockState(at).equals(want))l.setBlock(at,want,Block.UPDATE_CLIENTS);
        switch(n.pull){
            case HAULING,BINDING->{
                String bind=!r.rollerSeated?"roller":!n.jamCleared&&n.progress>=n.need/2?"jam":"";
                if(!bind.isEmpty()){if(n.pull!=BINDING||!bind.equals(n.binding)){n.pull=BINDING;n.binding=bind;LastCrossing.speak(l,r,bind.equals("jam")?LastCrossingTexts.BINDING_JAM:LastCrossingTexts.BINDING_ROLLER,24);}
                    if(r.tick%30==0)LastCrossing.sound(l,b.offset(bind.equals("jam")?LastCrossingSite.FAIRLEAD:LastCrossingSite.LOOSE_ROLLER),SoundEvents.CHAIN_STEP,1F,.5F);return;}
                n.pull=HAULING;n.binding="";n.progress++;
                if(r.tick%20==0)LastCrossing.sound(l,at,CrossingRegistry.RATCHET.get(),1F,1F);
                if(n.progress>=n.need)taken(l,r,n,here);}
            case RUNNING_BACK->{n.progress=Math.max(0,n.progress-2);if(r.tick%8==0)LastCrossing.sound(l,at,CrossingRegistry.RATCHET.get(),.9F,1.4F);
                if(r.tick-n.dropAt>=RUN_BACK)failed(l,r,n,here,true);}
            default->{}
        }
    }
    /** The cable house's brake at night: taken off and held for the pull, or caught as the drum runs back. */
    static boolean brake(ServerPlayer p){
        var r=LastCrossing.runOf(p);if(r==null||!active(r))return false;var n=r.night2;var l=p.serverLevel();
        if(n.pull==NOT_CALLED){LastCrossing.cue(p,"The brake's set. Wait for Vera to call the pull.");return true;}
        if(n.pull==PULLED){LastCrossing.cue(p,"The brake's set, the catch in. She's taken up.");return true;}
        if(n.pull==FAILED){LastCrossing.cue(p,"The cable's off the rollers. Nothing to pull on till they're reset.");return true;}
        if(p.getUUID().equals(n.brakeBy)){LastCrossing.cue(p,"You're holding it.");return true;}
        if(n.brakeBy!=null){LastCrossing.cue(p,"Someone's on the brake.");return true;}
        n.brakeBy=p.getUUID();n.pullHands.add(p.getUUID());LastCrossing.mark(p,"HeldPull");LastCrossing.job(p,"brake");
        if(n.pull==RUNNING_BACK){n.pull=HAULING;LastCrossing.mark(p,"CaughtBrake");LastCrossing.speak(l,r,LastCrossingTexts.CAUGHT,24);LastCrossing.cue(p,"You throw your weight on the brake and the drum stops running back.");}
        else{if(n.pull==CALLED){n.pull=HAULING;LastCrossing.speak(l,r,LastCrossingTexts.PULL_HOLD,24);}LastCrossing.cue(p,"You take the brake off and hold it. The drum takes up the cable, a notch at a time.");}
        return true;
    }
    static void taken(ServerLevel l,Run r,Night n,List<ServerPlayer> here){
        var b=r.base;n.pull=PULLED;n.brakeBy=null;
        l.setBlock(b.offset(LastCrossingSite.CABLE_BRAKE),LastCrossingSite.fit(Kind.BRAKE,Direction.WEST),Block.UPDATE_CLIENTS);
        r.dayTwo.catchSet=true;r.dayTwo.lined=true;l.setBlock(b.offset(LastCrossingSite.CATCH),LastCrossingSite.fit(Kind.CATCH,Direction.WEST,1),Block.UPDATE_CLIENTS);
        LastCrossing.sound(l,b.offset(LastCrossingSite.CATCH),SoundEvents.IRON_TRAPDOOR_CLOSE,1F,.7F);
        LastCrossing.speak(l,r,LastCrossingTexts.PULLED,24);
        for(var id:n.pullHands){var p=LastCrossing.player(l,id);if(p!=null)LastCrossing.mark(p,"Pulled");}
        for(var p:here)if(LastCrossingSite.inCableHouse(LastCrossing.rel(p.position(),b)))LastCrossing.mark(p,"SawPull");
    }
    /** The pull failed: run back too far, it throws the rollers; or the night ran out before it was done. */
    static void failed(ServerLevel l,Run r,Night n,List<ServerPlayer> here,boolean thrown){
        var b=r.base;n.pull=FAILED;n.brakeBy=null;
        if(thrown){r.rollerSeated=false;n.threw=true;
            for(var at:List.of(LastCrossingSite.LOOSE_ROLLER,LastCrossingSite.ROLLERS.get(2)))l.setBlock(b.offset(at),LastCrossingSite.fit(Kind.ROLLER,Direction.NORTH,1),Block.UPDATE_CLIENTS);
            LastCrossing.sound(l,b.offset(LastCrossingSite.LOOSE_ROLLER),SoundEvents.ANVIL_LAND,.8F,.6F);LastCrossing.speak(l,r,LastCrossingTexts.PULL_FAILED,24);}
        for(var p:here)if(LastCrossingSite.inCableHouse(LastCrossing.rel(p.position(),b))||n.pullHands.contains(p.getUUID()))LastCrossing.mark(p,"PullFailed");
    }
    /** The jam at the fairlead worked from above with the marlinspike, a lever at a time. */
    static boolean fairlead(ServerPlayer p,int stage){
        var r=LastCrossing.runOf(p);if(r==null||!active(r)||stage!=0)return false;var n=r.night2;var l=p.serverLevel();
        if(!LastCrossing.holds(p,"tools")){LastCrossing.cue(p,"The cable's kinked hard into the housing. You'd want the marlinspike to it, or a hand from below.");return true;}
        if(r.tick-n.leverAt<10)return true;n.leverAt=r.tick;n.levers++;LastCrossing.mark(p,"WorkedJam");
        LastCrossing.sound(l,r.base.offset(LastCrossingSite.FAIRLEAD),SoundEvents.CHAIN_HIT,1F,.6F+n.levers*.05F);
        if(n.levers<JAM_LEVERS){LastCrossing.cue(p,LastCrossingTexts.JAM_WORK);return true;}
        clearJam(l,r,n,p,false);LastCrossing.cue(p,LastCrossingTexts.JAM_WORKED);return true;
    }
    /** The fairlead's throat from the passage under it: one shove from below frees it. */
    static boolean underside(ServerPlayer p,int stage){
        var r=LastCrossing.runOf(p);if(r==null)return false;var l=p.serverLevel();
        if(stage!=0||!active(r)){LastCrossing.cue(p,LastCrossingTexts.look(Kind.UNDERSIDE,stage));return true;}
        clearJam(l,r,r.night2,p,true);LastCrossing.cue(p,LastCrossingTexts.JAM_CLEARED_BELOW);return true;
    }
    static void clearJam(ServerLevel l,Run r,Night n,ServerPlayer by,boolean below){
        var b=r.base;n.jamCleared=true;
        l.setBlock(b.offset(LastCrossingSite.FAIRLEAD),LastCrossingSite.fit(Kind.FAIRLEAD,Direction.NORTH,1),Block.UPDATE_CLIENTS);
        l.setBlock(b.offset(LastCrossingSite.UNDERSIDE),LastCrossingSite.fit(Kind.UNDERSIDE,Direction.NORTH,1),Block.UPDATE_CLIENTS);
        LastCrossing.sound(l,b.offset(LastCrossingSite.FAIRLEAD),SoundEvents.CHAIN_BREAK,1.2F,.7F);
        LastCrossing.mark(by,below?"ClearedJamBelow":"ClearedJam");
        if(!below)return;
        // From above, nobody was at it: those in the cable house see the cable jump free on its own.
        for(var q:LastCrossing.readers(l,b)){if(q==by||!LastCrossingSite.inCableHouse(LastCrossing.rel(q.position(),b)))continue;LastCrossing.mark(q,"SawJamCleared");LastCrossing.cue(q,LastCrossingTexts.JAM_SEEN);}
    }

    // ------------------------------------------------------------------------------------------------ the floor grate behind the brake
    /** Hands on the parking catch while the drum is hauling: not now. */
    static boolean catchBlocked(ServerPlayer p){
        var r=LastCrossing.runOf(p);if(r==null||!active(r))return false;int s=r.night2.pull;
        if(s<CALLED||s>RUNNING_BACK)return false;LastCrossing.cue(p,"Not while she's hauling.");return true;
    }
    /** True while the winch's branch runs: then something pushes up under its grate. */
    static boolean live(Run r){return r.dayTwo.winchOpen;}
    static boolean shutGate(Run r){var n=r.night2;return n.pinned||n.gateBy!=null;}
    /** A click on the floor grate at night: from above, held down, pinned by a second pair of hands or unpinned; a turned reader crouching
     * on it goes down through it; from below, a turned reader comes up through it. */
    static boolean floorGrate(ServerPlayer p,int stage){
        var r=LastCrossing.runOf(p);if(r==null||!active(r))return false;var n=r.night2;var l=p.serverLevel();var b=r.base;
        var q=LastCrossing.rel(p.position(),b);
        if(q.y<-1.2){if(!turned(r,p)||n.pinned||!live(r)){LastCrossing.cue(p,n.pinned?"It's pinned from above. It won't lift.":"It won't lift.");return true;}
            if(n.gateBy!=null){LastCrossing.cue(p,"Something's standing on it.");return true;}
            up(l,r,n,p,LastCrossingSite.ABOVE_FLOOR,LastCrossingSite.FLOOR_PRINTS);LastCrossing.mark(p,"CameUpGrate");return true;}
        if(turned(r,p)&&p.isShiftKeyDown()&&live(r)&&!n.pinned&&n.gateBy==null){
            LastCrossing.cue(p,LastCrossingTexts.GRATE_THROUGH);down(l,r,p,LastCrossingSite.BELOW_FLOOR);LastCrossing.mark(p,"WentBelowGrate");return true;}
        if(n.pinned){setGrate(l,r,live(r)?1:0);n.pinned=false;LastCrossing.mark(p,"PulledPin");LastCrossing.cue(p,LastCrossingTexts.GATE_UNPINNED);
            LastCrossing.sound(l,b.offset(LastCrossingSite.FLOOR_GRATES.get(0)),SoundEvents.IRON_TRAPDOOR_OPEN,.8F,1.2F);return true;}
        if(!live(r)){LastCrossing.cue(p,LastCrossingTexts.GATE_DRY);return true;}
        if(n.gateBy!=null&&!n.gateBy.equals(p.getUUID())){
            // A second pair of hands drives the pin home and takes the load off whoever was holding it.
            var holder=LastCrossing.player(l,n.gateBy);n.gateBy=null;n.pinned=true;setGrate(l,r,3);
            LastCrossing.sound(l,b.offset(LastCrossingSite.FLOOR_GRATES.get(0)),SoundEvents.ANVIL_PLACE,.5F,1.3F);
            LastCrossing.mark(p,"PinnedGate");LastCrossing.cue(p,LastCrossingTexts.GATE_PINNED);
            if(holder!=null){LastCrossing.addName(p,"PinnedFor",LastCrossing.name(holder));LastCrossing.addName(holder,"PinnedBy",LastCrossing.name(p));
                if(n.choking.getOrDefault(holder.getUUID(),0)>0)LastCrossing.cue(holder,LastCrossingTexts.GATE_EASED);}
            shutIn(l,r,n,p);return true;}
        if(p.getUUID().equals(n.gateBy)){n.gateBy=null;LastCrossing.cue(p,"You step off the grate.");return true;}
        n.gateBy=p.getUUID();LastCrossing.mark(p,"HeldGate");LastCrossing.cue(p,LastCrossingTexts.GATE_HOLD);shutIn(l,r,n,p);return true;
    }
    static void setGrate(ServerLevel l,Run r,int stage){for(var g:LastCrossingSite.FLOOR_GRATES)l.setBlock(r.base.offset(g),LastCrossingSite.fit(Kind.FLOOR_GRATE,Direction.NORTH,stage),Block.UPDATE_CLIENTS);}
    /** The grate shut over things called down into the branch: whoever shut it shut them in. */
    static void shutIn(ServerLevel l,Run r,Night n,ServerPlayer p){if(n.below>0)LastCrossing.mark(p,"ShutThemIn");}
    static void gate(ServerLevel l,Run r,Night n,List<ServerPlayer> here){
        if(n.gateBy==null)return;var p=LastCrossing.player(l,n.gateBy);
        var grate=Vec3.atCenterOf(r.base.offset(LastCrossingSite.FLOOR_GRATES.get(0)).above());
        if(p==null||!here.contains(p)||!p.isAlive()||p.position().distanceToSqr(grate)>2.2*2.2||!live(r)){n.gateBy=null;if(p!=null&&n.choking.getOrDefault(p.getUUID(),0)>0)LastCrossing.cue(p,LastCrossingTexts.BREATH_BACK);}
    }

    // ------------------------------------------------------------------------------------------------ water
    /** Down into the water: through the hatch, or a grate. */
    static void down(ServerLevel l,Run r,ServerPlayer p,Vec3 to){
        var at=LastCrossing.world(r.base,to);
        for(var w:LastCrossing.readers(l,r.base))if(w!=p&&!w.isSpectator()&&LastCrossing.sees(w,p.getEyePosition(),16,.3))LastCrossing.addName(w,"SawGoBelow",LastCrossing.name(p));
        l.sendParticles(ParticleTypes.SPLASH,p.getX(),p.getY()+.2,p.getZ(),20,.4,.1,.4,.1);LastCrossing.sound(l,p.blockPosition(),SoundEvents.PLAYER_SPLASH,.8F,.8F);
        p.teleportTo(l,at.x,at.y,at.z,p.getYRot(),p.getXRot());LastCrossing.mark(p,"WentBelow");
    }
    /** Up out of the water onto a dry floor, leaving wet prints there. */
    static void up(ServerLevel l,Run r,Night n,ServerPlayer p,Vec3 to,BlockPos printsAt){
        var at=LastCrossing.world(r.base,to);p.teleportTo(l,at.x,at.y,at.z,p.getYRot(),p.getXRot());
        l.sendParticles(ParticleTypes.SPLASH,at.x,at.y+.2,at.z,20,.4,.1,.4,.1);LastCrossing.sound(l,BlockPos.containing(at),SoundEvents.PLAYER_SPLASH_HIGH_SPEED,.6F,.9F);
        var cell=r.base.offset(printsAt);
        if(l.getBlockState(cell).isAir()||CrossingBlock.is(l.getBlockState(cell),Kind.PRINTS)){l.setBlock(cell,LastCrossingSite.fit(Kind.PRINTS,p.getDirection(),0),Block.UPDATE_CLIENTS);if(!n.prints.contains(printsAt))n.prints.add(printsAt);}
        LastCrossing.mark(p,"CameUp");
        for(var w:LastCrossing.readers(l,r.base))if(w!=p&&!w.isSpectator()&&LastCrossing.sees(w,p.getEyePosition(),16,.3))LastCrossing.addName(w,"SawComeUp",LastCrossing.name(p));
    }
    /** The hatch at night: a turned reader goes down through it and comes back up it; to anyone else it lifts an inch on black water. */
    static boolean hatch(ServerPlayer p){
        var r=LastCrossing.runOf(p);if(r==null||!active(r)||!turned(r,p))return false;var n=r.night2;var l=p.serverLevel();
        var q=LastCrossing.rel(p.position(),r.base);
        if(q.y<-1.2){up(l,r,n,p,LastCrossingSite.ABOVE_HATCH,LastCrossingSite.HATCH_PRINTS);LastCrossing.cue(p,LastCrossingTexts.HATCH_UP);return true;}
        LastCrossing.cue(p,LastCrossingTexts.HATCH_DOWN);down(l,r,p,LastCrossingSite.BELOW_HATCH);LastCrossing.mark(p,"WentBelowHatch");return true;
    }
    /** A wet grate into the ramp's branch, either end: a turned reader folds through it to the other side. */
    static boolean branchGrate(ServerPlayer p,BlockPos rel){
        var r=LastCrossing.runOf(p);if(r==null||!active(r)||!turned(r,p)||!r.dayTwo.rampOpen)return false;var n=r.night2;var l=p.serverLevel();
        boolean pit=rel.getX()==LastCrossingSite.PIT_GRATES.get(0).getX();double x=p.getX()-r.base.getX();
        LastCrossing.cue(p,LastCrossingTexts.GRATE_THROUGH);
        if(pit){if(x<rel.getX()+.5)down(l,r,p,LastCrossingSite.RAMP_BRANCH_WEST);else{up(l,r,n,p,LastCrossingSite.PIT_SIDE,LastCrossingSite.PIT_PRINTS);}}
        else{var to=x<rel.getX()+.5?LastCrossingSite.PASSAGE_SIDE:LastCrossingSite.RAMP_BRANCH_EAST;var at=LastCrossing.world(r.base,to);p.teleportTo(l,at.x,at.y,at.z,p.getYRot(),p.getXRot());}
        LastCrossing.mark(p,"ThroughBranch");return true;
    }
    /** What the turned do each tick: breathe water (and see in it), and choke on a live grate held shut or near the plate. */
    static void bodies(ServerLevel l,Run r,Night n,List<ServerPlayer> here){
        for(var p:here){
            if(!turned(r,p)||!p.isAlive()){n.choking.remove(p.getUUID());n.edge.remove(p.getUUID());continue;}
            if(r.tick%20==0){p.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING,220,0,true,false));
                boolean under=underwater(p);
                if(under)p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION,220,0,true,false));else p.removeEffect(MobEffects.NIGHT_VISION);
                if(!under)l.sendParticles(ParticleTypes.DRIPPING_WATER,p.getX(),p.getY()+1.3,p.getZ(),2,.25,.4,.25,0);}
            // The stronger of the two: a live grate held shut, or the plate.
            boolean gate=p.getUUID().equals(n.gateBy)&&live(r);double plate=plateDistance(l,r,p);
            boolean ward=plate<WARD;
            if(!gate&&!ward){
                int was=n.choking.getOrDefault(p.getUUID(),0);if(was>CHOKE_WARN)LastCrossing.cue(p,LastCrossingTexts.BREATH_BACK);n.choking.remove(p.getUUID());
                if(plate<WARD_EDGE){if(!n.edge.containsKey(p.getUUID())){n.edge.put(p.getUUID(),1);LastCrossing.cue(p,LastCrossingTexts.PLATE_EDGE);}}else n.edge.remove(p.getUUID());
                continue;}
            int t=n.choking.merge(p.getUUID(),1,Integer::sum);
            if(t==CHOKE_WARN){LastCrossing.cue(p,gate?LastCrossingTexts.GATE_WARN:LastCrossingTexts.PLATE_CHOKE);p.playNotifySound(CrossingRegistry.CHOKE.get(),SoundSource.PLAYERS,1F,1F);
                LastCrossing.mark(p,gate?"Choked":"Gilled");}
            int every=gate?CHOKE_EVERY:CHOKE_EVERY*2;float floor=gate?GATE_FLOOR:PLATE_FLOOR;
            if(t>CHOKE_WARN&&(t-CHOKE_WARN)%every==0&&p.getHealth()>floor+.5F){p.hurt(p.damageSources().drown(),Math.min(1F,p.getHealth()-floor));
                if(gate&&t==CHOKE_WARN+every)LastCrossing.cue(p,LastCrossingTexts.GATE_CHOKE);}
        }
    }
    /** Whether the water is over a reader's eyes, read from the block there. */
    static boolean underwater(ServerPlayer p){return p.level().getFluidState(BlockPos.containing(p.getEyePosition())).is(net.minecraft.tags.FluidTags.WATER);}
    /** How far the plate is from a reader: carried by someone, or standing or hung where it was set. */
    static double plateDistance(ServerLevel l,Run r,ServerPlayer p){
        Vec3 at=null;
        if(r.plateBy!=null){var c=LastCrossing.player(l,r.plateBy);if(c!=null)at=c.position();}
        else if(r.plateAt!=null)at=Vec3.atCenterOf(r.base.offset(r.plateAt));
        return at==null?Double.MAX_VALUE:Math.sqrt(at.distanceToSqr(p.position()));
    }
    /** Wet prints seen: by the hatch, by the floor grate, in the pit; they dry to a tidemark at dawn. */
    static void prints(ServerLevel l,Run r,Night n,ServerPlayer p){
        if(n.prints.isEmpty()||LastCrossing.own(p).getBoolean("SawPrints"))return;
        for(var at:n.prints){var c=Vec3.atBottomCenterOf(r.base.offset(at));if(!LastCrossing.sees(p,c,5,.3)&&p.position().distanceToSqr(c)>1.6*1.6)continue;
            LastCrossing.mark(p,"SawPrints");LastCrossing.cue(p,at.equals(LastCrossingSite.HATCH_PRINTS)?LastCrossingTexts.PRINTS_SEEN:"Wet prints on the floor. Somebody came up out of the water here.");return;}
    }

    // ------------------------------------------------------------------------------------------------ the call after dark
    /** A turned reader crouched a few seconds anywhere: up to two of the things turn toward them; from the water, they come down after it. */
    static void calls(ServerLevel l,Run r,Night n,List<ServerPlayer> here){
        for(var p:here){
            if(!turned(r,p)||!p.isShiftKeyDown()||!p.isAlive()){n.crouch.remove(p.getUUID());continue;}
            int t=n.crouch.merge(p.getUUID(),1,Integer::sum);if(t<CALL_CROUCH)continue;n.crouch.remove(p.getUUID());
            if(r.tick-n.calledAt.getOrDefault(p.getUUID(),-1_000_000L)<CALL_COOLDOWN){LastCrossing.cue(p,"You breathe out, slow. Nothing answers yet.");continue;}
            if(plateDistance(l,r,p)<WARD){LastCrossing.mark(p,"CallWarded");LastCrossing.cue(p,"By the plate, the breath catches in your throat. Nothing answers.");continue;}
            call(l,r,n,p);
        }
    }
    static void call(ServerLevel l,Run r,Night n,ServerPlayer p){
        var b=r.base;n.calledAt.put(p.getUUID(),r.tick);
        var own=LastCrossing.own(p);own.putInt("NightCalls",own.getInt("NightCalls")+1);LastCrossing.save(p,own);
        var near=new ArrayList<Undertow>();
        for(var id:n.mobs)if(l.getEntity(id) instanceof Undertow u&&u.isAlive()&&u.distanceToSqr(p)<CALL_REACH*CALL_REACH&&u.getUUID()!=n.grappler)near.add(u);
        near.sort(Comparator.comparingDouble(u->u.distanceToSqr(p)));
        boolean water=underwater(p);
        LastCrossing.cue(p,water?LastCrossingTexts.NIGHT_CALL_BELOW:LastCrossingTexts.NIGHT_CALL);
        for(var m:List.of(LastCrossingSite.PIPE_A_DOWN,LastCrossingSite.PIPE_B_DOWN,LastCrossingSite.FLOOR_GRATES.get(0)))LastCrossing.sound(l,b.offset(m),CrossingRegistry.SCRAPE.get(),.9F,.9F);
        for(var q:LastCrossing.readers(l,b))if(q!=p&&q.position().distanceToSqr(p.position())<24*24){LastCrossing.mark(q,"HeardScrape");LastCrossing.cue(q,"Something scrapes, deep in the pipes, and stops.");}
        int k=0;
        for(var u:near){if(k++>=2)break;
            if(water){
                // Called from the water, they go down into it, into the branch, and come up the grate behind the brake after it.
                LastCrossingNight.sink(l,u);n.mobs.remove(u.getUUID());n.below++;n.belowAt=r.tick;n.belowBy=p.getUUID();LastCrossing.mark(p,"DrewIntoBranch");}
            else{n.drawn.put(u.getUUID(),new Drawn(p.position(),r.tick+CALL_DRAW));u.setTarget(null);u.getNavigation().moveTo(p.getX(),p.getY(),p.getZ(),1.1);LastCrossing.mark(p,"DrewThem");}
        }
    }
    /** The things called down into the branch: up the floor grate after a while, unless it's shut or the branch has been drained. */
    static void below(ServerLevel l,Run r,Night n,List<ServerPlayer> here){
        if(n.below<=0||r.tick-n.belowAt<BELOW_WAIT)return;
        if(!live(r)){n.below=0;return;}
        if(shutGate(r)){if(r.tick%40==0){LastCrossing.sound(l,r.base.offset(LastCrossingSite.FLOOR_GRATES.get(0)),SoundEvents.ZOMBIE_ATTACK_IRON_DOOR,.6F,.7F);
            for(var p:here)if(p.position().distanceToSqr(Vec3.atCenterOf(r.base.offset(LastCrossingSite.FLOOR_GRATES.get(0))))<4*4)LastCrossing.mark(p,"FeltGrateKnock");}return;}
        n.below--;n.belowAt=r.tick-BELOW_WAIT+40;spawn(l,r,n,LastCrossingSite.FLOOR_RISE);
    }

    // ------------------------------------------------------------------------------------------------ the things from the water
    static void mobs(ServerLevel l,Run r,Night n,List<ServerPlayer> here){
        var b=r.base;
        n.mobs.removeIf(id->!(l.getEntity(id) instanceof Undertow u&&u.isAlive()));
        n.drawn.keySet().removeIf(id->!n.mobs.contains(id)||r.tick>n.drawn.get(id).until());
        int cap=Math.max(3,Math.min(LastCrossing.MAX_CREW,r.crew.size()));
        if(n.tick>=FIRST_SPAWN&&n.tick<LastCrossing.NIGHT_TICKS-SPAWN_STOP&&n.mobs.size()<cap&&(n.lastSpawn<0||n.tick-n.lastSpawn>=SPAWN_EVERY)){
            // Over the spillway's banks; and every other one through a branch left open, the grate behind the brake if nobody's shut it.
            Vec3 from=n.spawned%2==0?LastCrossingSite.WEST_BANK:LastCrossingSite.EAST_BANK;
            if(n.spawned%2==1&&live(r)&&!shutGate(r))from=LastCrossingSite.FLOOR_RISE;
            else if(n.spawned%3==2&&r.dayTwo.rampOpen)from=LastCrossingSite.PIT_RISE;
            spawn(l,r,n,from);
        }
        if(r.tick%10!=0)return;
        var door=LastCrossing.world(b,new Vec3(45.5,0,-52.5));
        for(var id:List.copyOf(n.mobs)){if(!(l.getEntity(id) instanceof Undertow u))continue;var q=LastCrossing.rel(u.position(),b);
            if(!LastCrossingSite.atStation(q)&&!LastCrossingSite.onBridge(q)){LastCrossingNight.sink(l,u);n.mobs.remove(id);continue;}
            var d=n.drawn.get(id);
            if(d!=null){if(u.position().distanceToSqr(d.to())<2*2)n.drawn.remove(id);else{u.setTarget(null);u.getNavigation().moveTo(d.to().x,d.to().y,d.to().z,1.1);}}
            else if(u.getTarget()==null&&u.getNavigation().isDone())u.getNavigation().moveTo(door.x,door.y,door.z,1.0);
            if(LastCrossingSite.inCableHouse(q))n.fouling+=10;
            if(r.tick%20==0)LastCrossingDay.passedBy(l,r,u,here);
        }
        // One in the cable house a few seconds knocks a seated roller off again.
        if(n.fouling>=LastCrossingNight.FOUL_AFTER&&!n.fouled&&r.rollerSeated){n.fouled=true;r.rollerSeated=false;
            var at=b.offset(LastCrossingSite.LOOSE_ROLLER);l.setBlock(at,LastCrossingSite.fit(Kind.ROLLER,Direction.NORTH,1),Block.UPDATE_CLIENTS);LastCrossing.sound(l,at,SoundEvents.ANVIL_LAND,.5F,.8F);
            for(var p:here)if(LastCrossingSite.inCableHouse(LastCrossing.rel(p.position(),b)))LastCrossing.mark(p,"SawFouled");}
    }
    static void spawn(ServerLevel l,Run r,Night n,Vec3 rel){
        var u=CrossingRegistry.UNDERTOW_TYPE.get().create(l);if(u==null)return;var at=LastCrossing.world(r.base,rel);
        u.addTag(LabyrinthSpawnRules.ALLOWED_TAG);u.addTag("crossing_undertow");u.moveTo(at.x,at.y,at.z,180,0);
        if(!l.addFreshEntity(u))return;
        n.mobs.add(u.getUUID());n.spawned++;n.lastSpawn=n.tick;
        l.sendParticles(ParticleTypes.SPLASH,at.x,at.y+.3,at.z,24,.5,.2,.5,.2);LastCrossing.sound(l,BlockPos.containing(at),CrossingRegistry.UNDERTOW.get(),1F,1F);
        boolean grate=rel==LastCrossingSite.FLOOR_RISE||rel==LastCrossingSite.PIT_RISE;
        for(var p:LastCrossing.readers(l,r.base)){if(!LastCrossing.sees(p,at.add(0,1,0),20,.2))continue;
            if(grate){LastCrossing.mark(p,"SawGrateRise");LastCrossing.cue(p,rel==LastCrossingSite.FLOOR_RISE?"Something comes up through the grate behind the brake, streaming.":"Something comes up through the grate into the pit, streaming.");}
            else if(!LastCrossing.own(p).getBoolean("SawUndertow")){LastCrossing.mark(p,"SawUndertow");LastCrossing.cue(p,"Something comes up out of the spillway and over its edge, streaming.");}}
    }

    // ------------------------------------------------------------------------------------------------ the grapple and the second slot
    static void grapple(ServerLevel l,Run r,Night n,List<ServerPlayer> here){
        if(n.grappled!=null){
            var v=LastCrossing.player(l,n.grappled);var u=l.getEntity(n.grappler) instanceof Undertow g&&g.isAlive()?g:null;
            if(v==null||!here.contains(v)||!v.isAlive()||u==null){release(l,r,n,null,false);return;}
            if(v.position().distanceToSqr(n.grapplePos)>.36)v.teleportTo(l,n.grapplePos.x,n.grapplePos.y,n.grapplePos.z,v.getYRot(),v.getXRot());
            v.setDeltaMovement(Vec3.ZERO);v.hurtMarked=true;u.setDeltaMovement(Vec3.ZERO);u.getNavigation().stop();
            if(r.tick%10==0)l.sendParticles(ParticleTypes.DRIPPING_WATER,v.getX(),v.getY()+1,v.getZ(),4,.3,.4,.3,0);
            if(r.tick-n.grappleAt>=GRAPPLE)ranCourse(l,r,n,v,u,here);
            return;}
        if(r.tick%10!=0)return;
        for(var id:n.mobs){if(!(l.getEntity(id) instanceof Undertow u)||!(u.getTarget() instanceof ServerPlayer t))continue;
            if(LastCrossing.spared(t)||!here.contains(t)||t.isSpectator()||u.distanceToSqr(t)>1.6*1.6||!alone(l,r,t,here))continue;
            n.grappled=t.getUUID();n.grappler=u.getUUID();n.grappleAt=r.tick;n.grapplePos=t.position();n.blows=0;
            LastCrossing.mark(t,"Grappled");LastCrossing.cue(t,LastCrossingTexts.GRAPPLED);LastCrossing.sound(l,t.blockPosition(),SoundEvents.DROWNED_AMBIENT_WATER,1F,.6F);
            for(var w:here)if(w!=t&&!w.isSpectator()&&LastCrossing.sees(w,t.getEyePosition(),20,.3))LastCrossing.addName(w,"SawGrapple",LastCrossing.name(t));
            return;}
    }
    /** On their own: none of the rest of the crew alive and standing within six blocks. */
    static boolean alone(ServerLevel l,Run r,ServerPlayer t,List<ServerPlayer> here){
        for(var q:here)if(q!=t&&!q.isSpectator()&&q.isAlive()&&r.crew.containsKey(q.getUUID())&&!r.dead.contains(q.getUUID())&&q.distanceToSqr(t)<6*6)return false;
        return true;
    }
    /** The grapple broken: by a blow on what holds them (theirs or a friend's), a hand pulling them up onto the dry ledge, or because one of
     * them is gone. */
    static void release(ServerLevel l,Run r,Night n,@Nullable ServerPlayer by,boolean ledge){
        var v=LastCrossing.player(l,n.grappled);var u=l.getEntity(n.grappler) instanceof Undertow g?g:null;
        n.grappled=null;n.grappler=null;n.grappleAt=-1;n.blows=0;
        if(v==null)return;
        LastCrossing.cue(v,LastCrossingTexts.GRAPPLE_BROKEN);
        if(by!=null&&by!=v){LastCrossing.mark(by,"BrokeGrapple");LastCrossing.addName(v,"BrokenFreeBy",LastCrossing.name(by));}
        else if(by==v)LastCrossing.mark(v,"FoughtGrapple");
        if(ledge&&by!=null){var at=LastCrossing.world(r.base,LastCrossingSite.LEDGE_SPOT);v.teleportTo(l,at.x,at.y,at.z,v.getYRot(),v.getXRot());
            LastCrossing.mark(by,"PulledToLedge");LastCrossing.addName(v,"PulledUpBy",LastCrossing.name(by));
            LastCrossing.cue(v,LastCrossing.name(by)+" hauls you up out of its arms onto the dry ledge.");}
        if(u!=null){var away=u.position().subtract(v.position()).multiply(1,0,1);if(away.lengthSqr()>1e-4)u.push(away.normalize().x*.6,.2,away.normalize().z*.6);}
    }
    /** A grapple that runs its course: the second slot, if a full crew of six all still stand and it is free; a deep cut otherwise. */
    static void ranCourse(ServerLevel l,Run r,Night n,ServerPlayer v,Undertow u,List<ServerPlayer> here){
        n.grappled=null;n.grappler=null;n.grappleAt=-1;
        LastCrossingNight.sink(l,u);n.mobs.remove(u.getUUID());
        var own=LastCrossing.own(v);
        if(secondSlotOpen(l,r,here)&&!own.getBoolean("Infected")){
            n.secondSlot=v.getUUID();own.putBoolean("Infected",true);own.putInt("Slot",2);own.putBoolean("SecondSlot",true);own.putLong("SecondAt",r.tick);LastCrossing.save(v,own);
            v.playNotifySound(CrossingRegistry.BREATH.get(),SoundSource.PLAYERS,1F,.9F);LastCrossing.cue(v,LastCrossingTexts.SECOND_SLOT);
            float dmg=Math.min(2F,Math.max(0F,v.getHealth()-6F));if(dmg>0)v.hurt(v.damageSources().generic(),dmg);}
        else{own.putBoolean("Wounded",true);own.putInt("Wounds",own.getInt("Wounds")+1);own.putBoolean("DeepCut",true);LastCrossing.save(v,own);LastCrossing.cue(v,LastCrossingTexts.GRAPPLE_WOUND);
            float dmg=Math.min(4F,Math.max(0F,v.getHealth()-4F));if(dmg>0)v.hurt(v.damageSources().generic(),dmg);}
    }
    /** "The second slot is available only with all six readers still active at the point of infection." */
    static boolean secondSlotOpen(ServerLevel l,Run r,List<ServerPlayer> here){
        if(r.night2.secondSlot!=null||r.crew.size()<LastCrossing.MAX_CREW||!r.dead.isEmpty())return false;
        for(var id:r.crew.keySet()){var p=LastCrossing.player(l,id);if(p==null||!here.contains(p)||!p.isAlive()||p.isSpectator())return false;}
        return true;
    }
    /** The second carrier's breath, now and then, heard by nobody else; they stay themselves through tonight and Day Three. */
    static void secondBeat(ServerLevel l,Run r,Night n,List<ServerPlayer> here){
        var c=LastCrossing.player(l,n.secondSlot);if(c==null||!here.contains(c))return;
        long since=r.tick-LastCrossing.own(c).getLong("SecondAt");
        if(since>0&&since%LastCrossingNight.SYMPTOM_EVERY==0){c.playNotifySound(CrossingRegistry.BREATH.get(),SoundSource.PLAYERS,.7F,.95F);LastCrossing.cue(c,LastCrossingTexts.SYMPTOMS.get((int)(since/LastCrossingNight.SYMPTOM_EVERY)%LastCrossingTexts.SYMPTOMS.size()));}
    }

    // ------------------------------------------------------------------------------------------------ dawn
    static void dawn(ServerLevel l,Run r,Night n,List<ServerPlayer> here){
        n.dawn=true;r.phase=LastCrossing.DAWN;r.dayNo=3;
        for(var id:n.mobs)if(l.getEntity(id) instanceof Undertow u)LastCrossingNight.sink(l,u);
        n.mobs.clear();n.drawn.clear();n.below=0;n.gateBy=null;n.grappled=null;n.grappler=null;n.turning=null;
        if(n.pull<PULLED)failed(l,r,n,here,false);
        l.setBlock(r.base.offset(LastCrossingSite.CABLE_BRAKE),LastCrossingSite.fit(Kind.BRAKE,Direction.WEST),Block.UPDATE_CLIENTS);
        // The wet prints dry to a tidemark; they're still there to be argued over.
        for(var at:n.prints){var cell=r.base.offset(at);var s=l.getBlockState(cell);if(CrossingBlock.is(s,Kind.PRINTS))l.setBlock(cell,s.setValue(CrossingBlock.STAGE,1),Block.UPDATE_CLIENTS);}
        LastCrossing.speak(l,r,LastCrossingTexts.DAWN_TWO,64);
        for(var p:here){if(r.crew.containsKey(p.getUUID()))LastCrossing.mark(p,"NightTwo");
            if(n.turned.contains(p.getUUID())){p.removeEffect(MobEffects.WATER_BREATHING);p.removeEffect(MobEffects.NIGHT_VISION);LastCrossing.mark(p,"BackAtDawn");
                var own=LastCrossing.own(p);own.putString("DawnWhere",LastCrossing.placeName(LastCrossing.rel(p.position(),r.base)));LastCrossing.save(p,own);
                LastCrossing.cue(p,LastCrossingTexts.TURNED_DAWN);}}
        n.turned.clear();n.choking.clear();
        LastCrossing.route(r,new Vec3(45.5,0,-55.5),new Vec3(45.5,0,-50.5),new Vec3(39.5,0,-50.5),new Vec3(26.5,0,-50.5),new Vec3(26.5,0,-35.5),new Vec3(13.5,0,-35.5),LastCrossingSite.VERA_PORCH);
        LastCrossing.sync(l,r);
    }

    // ------------------------------------------------------------------------------------------------ the station set back
    static void reset(ServerLevel l,Run r){
        CLAWED.clear();
        for(var id:r.night2.turned){var p=LastCrossing.player(l,id);if(p!=null){p.removeEffect(MobEffects.WATER_BREATHING);p.removeEffect(MobEffects.NIGHT_VISION);}}
        r.night2=new Night();
    }

    // ------------------------------------------------------------------------------------------------ speech after dark
    /** What a reader's words come out as: broken if they're turned tonight, worse near the plate or holding a live grate shut. The broken
     * line is made once and shown to everyone who hears it, the speaker too. */
    public static String voice(ServerPlayer p,String text){
        var r=LastCrossing.runOf(p);if(r==null||!turned(r,p)||text==null||text.isBlank())return text;var n=r.night2;var l=p.serverLevel();
        int level=plateDistance(l,r,p)<WARD||p.getUUID().equals(n.gateBy)&&live(r)?LastCrossingSpeech.CHOKED:LastCrossingSpeech.NIGHT_TWO;
        var names=new ArrayList<>(r.crew.values());names.addAll(List.of("Vera","Ellis","Cordelia"));
        var said=LastCrossingSpeech.garble(text,level,names,r.id+r.tick);
        LastCrossing.mark(p,"SpokeTurned");
        for(var q:LastCrossing.readers(l,r.base))if(q!=p&&q.distanceToSqr(p)<Spoken.EARSHOT*Spoken.EARSHOT)LastCrossing.addName(q,"HeardRasp",LastCrossing.name(p));
        return said;
    }
    /** Chat after dark from a turned reader at the station comes out broken too, the name still on it. */
    @SubscribeEvent public static void chat(ServerChatEvent e){
        var p=e.getPlayer();if(!turnedNow(p))return;
        var said=voice(p,e.getRawText());if(!said.equals(e.getRawText()))e.setMessage(Component.literal(said));
    }

    // ------------------------------------------------------------------------------------------------ claws, and blows on a grapple
    /** Ticks between one turned reader's claws. */
    static final int CLAW_EVERY=10;
    private static final Map<UUID,Long> CLAWED=new HashMap<>();
    /** A blow on whatever has hold of someone in a grapple counts toward breaking it; a turned reader's bare hand is a claw. The claw is
     * dealt here, not by the ordinary blow, so it lands on another reader whether or not the server lets players hurt each other. */
    @SubscribeEvent public static void attack(AttackEntityEvent e){
        if(!(e.getEntity() instanceof ServerPlayer a)||!(e.getTarget() instanceof net.minecraft.world.entity.LivingEntity v))return;
        var r=LastCrossing.runOf(a);if(r==null||!active(r))return;
        blow(a,v);
        if(turned(r,a)&&a.getMainHandItem().isEmpty()){e.setCanceled(true);claw(a,v);}
    }
    static void blow(ServerPlayer a,net.minecraft.world.entity.LivingEntity v){
        var r=LastCrossing.runOf(a);if(r==null||!active(r))return;var n=r.night2;
        if(n.grappled==null||!v.getUUID().equals(n.grappler))return;
        if(!a.getUUID().equals(n.grappled))release(a.serverLevel(),r,n,a,false);
        else if(++n.blows>=GRAPPLE_BLOWS)release(a.serverLevel(),r,n,a,false);
        else LastCrossing.cue(a,"You hit it. It holds on.");
    }
    /** A turned reader's claw: two hearts on a reader, three on one of the things, the slasher's splatter, and a reader's clothes torn. */
    public static boolean claw(ServerPlayer a,net.minecraft.world.entity.LivingEntity v){
        var r=LastCrossing.runOf(a);if(r==null||!turned(r,a)||v==a||!v.isAlive())return false;var n=r.night2;var l=a.serverLevel();
        long now=l.getGameTime();if(now-CLAWED.getOrDefault(a.getUUID(),-1000L)<CLAW_EVERY)return false;CLAWED.put(a.getUUID(),now);
        a.swing(net.minecraft.world.InteractionHand.MAIN_HAND,true);
        if(v instanceof Player){v.hurt(l.damageSources().generic(),4F);v.knockback(.4,a.getX()-v.getX(),a.getZ()-v.getZ());}
        else v.hurt(l.damageSources().playerAttack(a),6F);
        ElkHunt.blood(l,v,a);LastCrossing.sound(l,v.blockPosition(),CrossingRegistry.CLAW.get(),1F,1F);
        var own=LastCrossing.own(a);boolean first=own.getInt("Claws")==0;own.putInt("Claws",own.getInt("Claws")+1);LastCrossing.save(a,own);
        if(first)LastCrossing.cue(a,LastCrossingTexts.CLAWED_SOMEONE);
        if(v instanceof ServerPlayer q&&LastCrossing.runOf(q)==r){
            boolean fresh=n.torn.add(q.getUUID());LastCrossing.mark(q,"Clawed");LastCrossing.addName(q,"ClawedBy",LastCrossing.name(a));LastCrossing.addName(a,"ClawedReader",LastCrossing.name(q));
            LastCrossing.cue(q,LastCrossingTexts.RAKED);if(fresh)LastCrossing.sync(l,r);}
        for(var w:LastCrossing.readers(l,r.base))if(w!=a&&w!=v&&!w.isSpectator()&&LastCrossing.sees(w,v.position().add(0,1,0),16,.3))LastCrossing.addName(w,"SawClaws",LastCrossing.name(a));
        return true;
    }
    /** A hand to someone held in a grapple: they're hauled out of its arms (onto the dry ledge, in the cable house). */
    @SubscribeEvent public static void reach(PlayerInteractEvent.EntityInteract e){
        if(!(e.getEntity() instanceof ServerPlayer p)||!(e.getTarget() instanceof ServerPlayer q)||e.getHand()!=net.minecraft.world.InteractionHand.MAIN_HAND)return;
        if(pullFree(p,q))e.setCanceled(true);
    }
    public static boolean pullFree(ServerPlayer p,ServerPlayer q){
        var r=LastCrossing.runOf(p);if(r==null||!active(r)||!q.getUUID().equals(r.night2.grappled)||p==q)return false;
        boolean ledge=LastCrossingSite.inCableHouse(LastCrossing.rel(q.position(),r.base));
        release(p.serverLevel(),r,r.night2,p,ledge);return true;
    }
    /** What has hold of someone holds them; it doesn't beat them while it does. */
    @SubscribeEvent public static void held(LivingIncomingDamageEvent e){
        if(!(e.getEntity() instanceof ServerPlayer v)||!(e.getSource().getEntity() instanceof Undertow u))return;
        var r=LastCrossing.runOf(v);if(r==null||!active(r))return;
        if(v.getUUID().equals(r.night2.grappled)&&u.getUUID().equals(r.night2.grappler))e.setCanceled(true);
    }
}
