package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.labyrinth.CrossingBlock.Kind;
import io.github.knaitoe.theoldesthouse.labyrinth.LastCrossing.Run;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.353, The Last Crossing, Day Two (docs/LAST_CROSSING_0_4_353.md), from the owner's design: "The first infected reader looks and
 * sounds human. Vera checks the work slate, hands over the cable tools, and asks who's taking the far end. This is the first full day in
 * which someone can lie about what happened to them."
 *
 * <p>A morning after the night, then Vera goes in to the slate: Ellis is still on it, marked present. She lays the cable tools on the desk
 * and asks who's taking the far end. The winch is the day's work: the witness plate on the cable bed at the far end, greased over until
 * someone scrapes it with the tools, shows how far the paint on the cable sits from the mark cut in the bed, and whoever reads it can call
 * it aloud; the brake by the drum eases it out a notch or cranks it in. Left alone the drum creeps out under the river's pull, so one hand
 * stays on the brake while another reads the marks, or a reader working alone drops the parking catch: set square on the marks it holds
 * for the pull, set anywhere else it slips after a while. The work bell by the roller bed is there for signals.
 *
 * <p>Two bypass wheels flood two service branches: the ramp's, in the intake, the low tunnel from the sluice pit to the cable house's
 * passage; the winch's, in the cable house, a spur coming up under a floor grate behind the brake. Nothing says who turned one. What it
 * leaves: the wheel turned and its tag with it, the grates wet and later damp, water running behind them, a stain spreading under the
 * fairlead for the winch's, and the gauge dipping sooner than it did the day before. An infected reader crouching by a wet grate can call:
 * something scrapes an answer through the pipes, and a little later one of the things from the water comes up through that grate. They
 * pass a carrier by. Near the plate, the call catches in the throat. Before dusk Vera asks for the brake to be left set for the first pull,
 * and says what Ellis's answer sounded like in the pipe. At the second dusk the station holds until Night Two is built.
 */
public final class LastCrossingDay {
    private LastCrossingDay(){}
    /** Ticks: the morning before Day Two starts; how long a drum left alone holds before it creeps out a notch; how long a catch set off
     * the marks holds before it slips; how long a shut branch's grates stay damp and its stain takes to dry; how long an infected reader
     * crouches by a wet grate to call, and how long between calls; how soon the thing called comes up, and how long it stays by day. The
     * plate's reach over the call, in blocks; how many notches the drum can stand off the marks either way. */
    public static final int MORNING=300,CREEP=200,CATCH_SLIP=400,DRY=1200,CALL_CROUCH=60,CALL_COOLDOWN=900,RISE_AFTER=60,DAY_STAY=600,WARD=4,FAR=5;

    record Rise(long at,boolean floor){}
    static final class Day {
        int morning;boolean started;int vera;long veraAt=-1;boolean toolsOut,toolsTaken;@Nullable UUID farEnd;boolean briefed,voiced;long briefAt=-1;
        /** The winch: how many notches the cable's paint sits short of the bed's mark (past it if negative), whether anyone has touched it,
         * who is on the brake, the last creep, the catch, lined up, the plate scraped, and everyone who had a hand in it. */
        int offset=3;boolean touched;@Nullable UUID brakeBy;long lastCreep=-1;boolean catchSet,lined;long catchAt=-1;boolean scraped;
        final LinkedHashSet<UUID> hands=new LinkedHashSet<>();
        /** The bypasses, and when each was last shut (its grates damp until they dry). */
        boolean winchOpen,rampOpen;long winchShutAt=-1,rampShutAt=-1;
        /** The call: how long each carrier has crouched by a wet grate, when each last called, what is coming up, and what came up by day. */
        final Map<UUID,Integer> crouch=new HashMap<>();final Map<UUID,Long> calledAt=new HashMap<>();
        final List<Rise> rising=new ArrayList<>();final Map<UUID,Long> mobs=new HashMap<>();
        /** Night Two's clock (its slice). */
        int nightTick;
    }
    static boolean dayTwo(Run r){return r.phase==LastCrossing.DAY&&r.dayNo>=2;}
    static boolean open(Run r){return r.dayTwo.winchOpen||r.dayTwo.rampOpen;}
    /** Where the drum starts off the marks: short by three or four, or past by two or three. */
    static int initial(Run r){int[] opts={3,4,-2,-3};return opts[Math.floorMod((int)(r.id^r.base.asLong()),opts.length)];}

    // ------------------------------------------------------------------------------------------------ the morning and the day
    /** The morning after Night One: Vera back on her porch, then the slate. */
    static void morning(ServerLevel l,Run r,List<ServerPlayer> here){
        var d=r.dayTwo;d.morning++;
        if(d.morning>=MORNING&&!LastCrossing.walking(r))start(l,r,here);
    }
    static void start(ServerLevel l,Run r,List<ServerPlayer> here){
        var d=r.dayTwo;r.phase=LastCrossing.DAY;r.dayNo=2;r.day=0;d.started=true;d.offset=initial(r);
        // The surge filled the intake the hard way: whatever was done or not done to prime it, the pump has its load this morning.
        if(!r.primed){r.primed=true;r.primedAt=r.tick;l.setBlock(r.base.offset(LastCrossingSite.PUMP),LastCrossingSite.fit(Kind.PUMP,Direction.SOUTH,1),Block.UPDATE_CLIENTS);}
        LastCrossing.speak(l,r,LastCrossingTexts.SLATE_FIRST,64);
        LastCrossing.route(r,new Vec3(15.5,0,-36.5),new Vec3(15.5,0,-39.5),LastCrossingSite.VERA_SLATE);d.vera=1;
    }
    static void tick(ServerLevel l,Run r,List<ServerPlayer> here){
        var d=r.dayTwo;r.day++;
        vera(l,r,here);
        winch(l,r,here);
        calls(l,r,here);
        LastCrossingNight.carrierBeat(l,r,r.night,here);
        if(!d.briefed&&r.day>=LastCrossing.DAY_TICKS*5/6){d.briefed=true;d.briefAt=r.tick;LastCrossing.speak(l,r,LastCrossingTexts.BRAKE_SET,64);}
        if(d.briefed&&!d.voiced&&r.tick-d.briefAt>=120){d.voiced=true;LastCrossing.speak(l,r,LastCrossingTexts.ELLIS_VOICE,64);
            var a=LastCrossing.actor(l,r.vera);var from=a==null?LastCrossing.world(r.base,LastCrossingSite.VERA_PORCH):a.position();
            for(var p:here)if(p.position().distanceToSqr(from)<64*64)LastCrossing.mark(p,"HeardEllisVoice");}
        if(r.day>=LastCrossing.DAY_TICKS)dusk(l,r,here);
    }
    /** Vera at the slate, the tools laid on the desk, and the question; then back to her porch. */
    static void vera(ServerLevel l,Run r,List<ServerPlayer> here){
        var d=r.dayTwo;
        if(d.vera==1&&!LastCrossing.walking(r)){d.vera=2;d.veraAt=r.tick;LastCrossing.speak(l,r,LastCrossingTexts.SLATE_SEEN,24);
            for(var p:here)if(LastCrossingSite.inOffice(LastCrossing.rel(p.position(),r.base)))LastCrossing.mark(p,"HeardSlate");
            if(!d.toolsTaken)tools(l,r);}
        else if(d.vera==2&&r.tick-d.veraAt>=100){d.vera=3;LastCrossing.speak(l,r,LastCrossingTexts.FAR_END,24);
            LastCrossing.route(r,new Vec3(15.5,0,-39.5),new Vec3(15.5,0,-36.5),LastCrossingSite.VERA_PORCH);}
    }
    /** The cable tools on the desk. */
    static void tools(ServerLevel l,Run r){r.dayTwo.toolsOut=true;l.setBlock(r.base.offset(LastCrossingSite.TOOLS),LastCrossingSite.fit(Kind.TOOLS,Direction.NORTH),Block.UPDATE_CLIENTS);}
    /** The second dusk: everyone on the roll who is there has worked Day Two. The station holds here until Night Two is built. */
    static void dusk(ServerLevel l,Run r,List<ServerPlayer> here){
        r.phase=LastCrossing.DUSK;r.dayTwo.nightTick=0;LastCrossing.speak(l,r,LastCrossingTexts.DUSK,64);
        for(var p:here)if(r.crew.containsKey(p.getUUID()))LastCrossing.mark(p,"DayTwo");
    }

    // ------------------------------------------------------------------------------------------------ the winch
    static void winch(ServerLevel l,Run r,List<ServerPlayer> here){
        var d=r.dayTwo;var b=r.base;var brake=Vec3.atCenterOf(b.offset(LastCrossingSite.CABLE_BRAKE));
        var holder=LastCrossing.player(l,d.brakeBy);
        boolean held=holder!=null&&here.contains(holder)&&holder.position().distanceToSqr(brake)<2.5*2.5;
        if(!held&&d.brakeBy!=null){d.brakeBy=null;d.lastCreep=r.tick;}
        var want=LastCrossingSite.fit(Kind.BRAKE,Direction.WEST,held?1:0);var at=b.offset(LastCrossingSite.CABLE_BRAKE);
        if(!l.getBlockState(at).equals(want))l.setBlock(at,want,Block.UPDATE_CLIENTS);
        // Left alone, the drum creeps out under the river's pull.
        if(d.touched&&!held&&!d.catchSet&&d.offset>-FAR&&r.tick-d.lastCreep>=CREEP){d.offset--;d.lastCreep=r.tick;
            LastCrossing.sound(l,at,CrossingRegistry.RATCHET.get(),.8F,.85F);
            for(var p:here)if(LastCrossingSite.inCableHouse(LastCrossing.rel(p.position(),b)))LastCrossing.cue(p,"Nobody on the brake: the drum creeps out a notch.");}
        // A catch set off the marks slips after a while, and the drum runs out a notch.
        if(d.catchSet&&!d.lined&&r.tick-d.catchAt>=CATCH_SLIP){d.catchSet=false;d.offset=Math.max(-FAR,d.offset-1);d.lastCreep=r.tick;
            l.setBlock(b.offset(LastCrossingSite.CATCH),LastCrossingSite.fit(Kind.CATCH,Direction.WEST),Block.UPDATE_CLIENTS);
            LastCrossing.sound(l,b.offset(LastCrossingSite.CATCH),CrossingRegistry.RATCHET.get(),1F,.7F);
            for(var p:here)if(LastCrossingSite.inCableHouse(LastCrossing.rel(p.position(),b))){LastCrossing.mark(p,"CatchSlipped");LastCrossing.cue(p,"The catch slips out of the drum's teeth and the drum runs out a notch.");}}
    }
    /** The cable house's brake on Day Two: eased, the drum pays out a notch; leaned on crouching, it is cranked in a notch. */
    static boolean brake(ServerPlayer p){
        var r=LastCrossing.runOf(p);if(r==null||r.dayNo<2)return false;var d=r.dayTwo;var l=p.serverLevel();
        if(r.phase!=LastCrossing.DAY){LastCrossing.cue(p,d.lined?"The brake's set, the catch in the drum's teeth. That's what we pull on.":"The brake's set.");return true;}
        if(d.catchSet){LastCrossing.cue(p,"The catch is down in the drum's teeth. Knock it off first.");return true;}
        boolean in=p.isShiftKeyDown();
        d.offset=Mth.clamp(d.offset+(in?1:-1),-FAR,FAR);d.touched=true;d.brakeBy=p.getUUID();d.lastCreep=r.tick;d.hands.add(p.getUUID());
        LastCrossing.sound(l,r.base.offset(LastCrossingSite.CABLE_BRAKE),CrossingRegistry.RATCHET.get(),1F,1F);
        LastCrossing.mark(p,in?"CrankedDrum":"EasedDrum");LastCrossing.job(p,"brake");
        LastCrossing.cue(p,in?"You crank the drum in a notch.":"You ease the brake and the drum pays out a notch.");
        return true;
    }
    /** The parking catch: dropped into the drum's teeth, and knocked out again. */
    static boolean catchUse(ServerPlayer p,int stage){
        var r=LastCrossing.runOf(p);if(r==null)return false;var d=r.dayTwo;var l=p.serverLevel();var at=r.base.offset(LastCrossingSite.CATCH);
        if(r.dayNo<2){LastCrossing.cue(p,"The winch's parking catch. Nobody's pulling today.");return true;}
        if(stage==1){if(r.phase!=LastCrossing.DAY&&d.lined){LastCrossing.cue(p,"The catch holds the drum on the marks for the pull.");return true;}
            d.catchSet=false;d.lined=false;d.lastCreep=r.tick;l.setBlock(at,LastCrossingSite.fit(Kind.CATCH,Direction.WEST),Block.UPDATE_CLIENTS);
            LastCrossing.sound(l,at,SoundEvents.IRON_TRAPDOOR_OPEN,.8F,.8F);LastCrossing.cue(p,"You knock the catch out of the drum's teeth.");return true;}
        d.catchSet=true;d.catchAt=r.tick;d.touched=true;d.hands.add(p.getUUID());
        l.setBlock(at,LastCrossingSite.fit(Kind.CATCH,Direction.WEST,1),Block.UPDATE_CLIENTS);LastCrossing.sound(l,at,SoundEvents.IRON_TRAPDOOR_CLOSE,1F,.7F);
        LastCrossing.mark(p,"SetCatch");LastCrossing.job(p,"brake");
        if(d.offset!=0){LastCrossing.cue(p,"The catch drops into the drum's teeth.");return true;}
        d.lined=true;
        LastCrossing.cue(p,"The catch drops into the drum's teeth with the marks square. It'll hold there for the pull.");
        var names=new ArrayList<String>();for(var id:d.hands){var q=LastCrossing.player(l,id);if(q!=null)names.add(LastCrossing.name(q));}
        for(var id:d.hands){var q=LastCrossing.player(l,id);if(q==null)continue;LastCrossing.mark(q,"LinedUp");for(var other:names)if(!other.equals(LastCrossing.name(q)))LastCrossing.addName(q,"LinedWith",other);}
        var a=LastCrossing.actor(l,r.vera);if(a!=null&&a.position().distanceToSqr(Vec3.atCenterOf(at))<24*24)LastCrossing.speak(l,r,LastCrossingTexts.LINED,24);
        return true;
    }
    private static final String[] WORDS={"one","two","three","four","five"};
    static String word(int n){return WORDS[Mth.clamp(n,1,5)-1];}
    static String upper(String w){return Character.toUpperCase(w.charAt(0))+w.substring(1);}
    /** The witness plate: greased until it is scraped with the cable tools, then read, and the reading called aloud. */
    static boolean witness(ServerPlayer p,int stage){
        var r=LastCrossing.runOf(p);if(r==null)return false;var d=r.dayTwo;var l=p.serverLevel();var at=r.base.offset(LastCrossingSite.WITNESS);
        if(stage==0){
            if(!LastCrossing.holds(p,"tools")){LastCrossing.cue(p,"Grease and grit over the witness plate. You'd want a scraper to it.");return true;}
            l.setBlock(at,LastCrossingSite.fit(Kind.WITNESS,Direction.EAST,1),Block.UPDATE_CLIENTS);d.scraped=true;
            LastCrossing.sound(l,at,SoundEvents.BRUSH_GENERIC,1F,.8F);LastCrossing.mark(p,"ScrapedMarks");LastCrossing.job(p,"far end");
            LastCrossing.cue(p,"You scrape the grease and grit off the witness plate. A mark cut square across the bed, and on the cable over it, a stripe of paint.");return true;}
        if(r.dayNo<2){LastCrossing.cue(p,"The witness plate, scraped clean. The paint on the cable sits where it sits; nobody's pulling today.");return true;}
        d.hands.add(p.getUUID());LastCrossing.mark(p,"ReadMarks");LastCrossing.job(p,"far end");
        int o=d.offset,n=Math.abs(o);
        String where=o==0?"square on":word(n)+" hand-width"+(n==1?"":"s")+(o>0?" short of":" past");
        String call=o==0?"Marks meet! Set the catch!":upper(word(n))+(o>0?" short!":" past!");
        LastCrossing.begin(p,"The witness marks");
        Talk.line(p,"","The paint on the cable sits "+where+" the mark on the bed.");
        Talk.ask(p,Talk.reply(call,q->{LastCrossing.mark(q,"CalledMarks");Talk.close(q);}),Talk.act("Leave it.",Talk::close));
        return true;
    }
    /** The cable tools taken off the desk: the far end is theirs. */
    static boolean takeTools(ServerPlayer p,BlockPos pos){
        var r=LastCrossing.runOf(p);if(r==null)return false;var d=r.dayTwo;var l=p.serverLevel();
        if(!LastCrossing.give(p,LastCrossing.item(CrossingRegistry.TOOLS.get(),"tools",null))){LastCrossing.cue(p,"Your hands are full.");return true;}
        l.setBlock(pos,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);d.toolsTaken=true;d.toolsOut=false;if(d.farEnd==null)d.farEnd=p.getUUID();
        LastCrossing.mark(p,"TookTools");LastCrossing.mark(p,"FarEnd");LastCrossing.job(p,"far end");
        LastCrossing.cue(p,"You take the cable tools: a scraper, a marlinspike and rags, rolled in canvas. The far end's yours.");return true;
    }

    // ------------------------------------------------------------------------------------------------ the branches
    /** A bypass wheel turned open (flooding its branch) or wound shut. */
    static boolean bypass(ServerPlayer p,BlockPos rel,int stage){
        var r=LastCrossing.runOf(p);if(r==null)return false;var d=r.dayTwo;var l=p.serverLevel();var b=r.base;
        boolean winch=rel.equals(LastCrossingSite.WINCH_BYPASS),open=stage==0;
        l.setBlock(b.offset(rel),LastCrossingSite.fit(Kind.BYPASS,Direction.WEST,open?1:0),Block.UPDATE_CLIENTS);
        LastCrossing.sound(l,b.offset(rel),SoundEvents.IRON_TRAPDOOR_OPEN,.7F,.55F);
        if(winch){d.winchOpen=open;
            if(open){LastCrossingSite.floodSpur(l,b);d.winchShutAt=-1;}else{LastCrossingSite.drainSpur(l,b);d.winchShutAt=r.tick;}
            for(var g:LastCrossingSite.FLOOR_GRATES)l.setBlock(b.offset(g),LastCrossingSite.fit(Kind.FLOOR_GRATE,Direction.NORTH,open?1:2),Block.UPDATE_CLIENTS);
            l.setBlock(b.offset(LastCrossingSite.STAIN),LastCrossingSite.fit(Kind.STAIN,Direction.NORTH,open?0:1),Block.UPDATE_CLIENTS);}
        else{d.rampOpen=open;
            if(open){LastCrossingSite.floodRamp(l,b);d.rampShutAt=-1;}else{LastCrossingSite.drainRamp(l,b);d.rampShutAt=r.tick;}
            for(var g:branchGrates())l.setBlock(b.offset(g),LastCrossingSite.fit(Kind.BRANCH,Direction.EAST,open?1:2),Block.UPDATE_CLIENTS);}
        LastCrossing.mark(p,open?"OpenedBypass":"ShutBypass");LastCrossing.addName(p,"Bypasses",(winch?"winch":"ramp")+(open?"":" shut"));
        var at=Vec3.atCenterOf(b.offset(rel));
        for(var q:LastCrossing.readers(l,b)){if(q==p||!LastCrossing.sees(q,at,16,.3))continue;
            LastCrossing.addName(q,"SawBypassBy",LastCrossing.name(p));LastCrossing.cue(q,LastCrossing.name(p)+(open?" turns a bypass wheel open.":" winds a bypass wheel shut."));}
        LastCrossing.cue(p,open?"The wheel comes round. Somewhere below, water starts to run.":"You wind the wheel shut. Below, the running water slows and stops.");
        return true;
    }
    static List<BlockPos> branchGrates(){var all=new ArrayList<BlockPos>(LastCrossingSite.PIT_GRATES);all.addAll(LastCrossingSite.PASSAGE_GRATES);return all;}
    /** Every tick, from the moment Day Two starts: the branches drying, the water running, what a reader sees of them, and the things called up. */
    static void always(ServerLevel l,Run r,List<ServerPlayer> here){
        var d=r.dayTwo;var b=r.base;if(!d.started)return;
        if(!d.winchOpen&&d.winchShutAt>=0&&r.tick-d.winchShutAt>=DRY){d.winchShutAt=-1;
            for(var g:LastCrossingSite.FLOOR_GRATES)l.setBlock(b.offset(g),LastCrossingSite.fit(Kind.FLOOR_GRATE,Direction.NORTH),Block.UPDATE_CLIENTS);
            l.setBlock(b.offset(LastCrossingSite.STAIN),Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);}
        if(!d.rampOpen&&d.rampShutAt>=0&&r.tick-d.rampShutAt>=DRY){d.rampShutAt=-1;
            for(var g:branchGrates())l.setBlock(b.offset(g),LastCrossingSite.fit(Kind.BRANCH,Direction.EAST),Block.UPDATE_CLIENTS);}
        if(r.tick%40==0){if(d.winchOpen)LastCrossing.sound(l,b.offset(LastCrossingSite.FLOOR_GRATES.get(0)),CrossingRegistry.BRANCH.get(),.5F,1F);
            if(d.rampOpen)LastCrossing.sound(l,b.offset(LastCrossingSite.PIT_GRATES.get(0)),CrossingRegistry.BRANCH.get(),.6F,.95F);}
        if(r.tick%20==0){var wet=new ArrayList<BlockPos>();if(d.winchOpen)wet.addAll(LastCrossingSite.FLOOR_GRATES);if(d.rampOpen)wet.addAll(LastCrossingSite.PIT_GRATES);
            for(var g:wet){var at=b.offset(g);l.sendParticles(ParticleTypes.DRIPPING_WATER,at.getX()+.5,at.getY()+1.02,at.getZ()+.5,2,.3,0,.3,0);}}
        if(r.tick%10==0)for(var p:here)seen(l,r,p);
        rising(l,r,here);
        mobs(l,r,here);
    }
    /** A wet or damp grate, or the stain, seen close to. */
    static void seen(ServerLevel l,Run r,ServerPlayer p){
        var b=r.base;var own=LastCrossing.own(p);
        if(!own.getBoolean("SawWetGrate")){
            var all=new ArrayList<BlockPos>(LastCrossingSite.FLOOR_GRATES);all.addAll(LastCrossingSite.PIT_GRATES);
            for(var g:all){var s=l.getBlockState(b.offset(g));int st=s.getValue(CrossingBlock.STAGE);var c=Vec3.atCenterOf(b.offset(g));
                if(st==0||p.position().distanceToSqr(c)>1.6*1.6&&!LastCrossing.sees(p,c,5,.3))continue;
                LastCrossing.mark(p,"SawWetGrate");
                LastCrossing.cue(p,st==1?"The grate's wet. Water's running in the branch behind it.":"The grate's bars are damp, beaded with water. Something ran behind it not long ago.");break;}}
        if(!own.getBoolean("SawStain")){var at=b.offset(LastCrossingSite.STAIN);
            if(CrossingBlock.is(l.getBlockState(at),Kind.STAIN)&&LastCrossing.sees(p,Vec3.atBottomCenterOf(at),4.5,.3)){LastCrossing.mark(p,"SawStain");
                LastCrossing.cue(p,l.getBlockState(at).getValue(CrossingBlock.STAGE)==0?"A dark wet stain on the boards under the fairlead, spreading.":"A stain on the boards under the fairlead, drying at its edges.");}}
    }

    // ------------------------------------------------------------------------------------------------ the call by day
    /** An infected reader crouching by a wet grate, by daylight: after a few seconds, something answers. */
    static void calls(ServerLevel l,Run r,List<ServerPlayer> here){
        var d=r.dayTwo;
        for(var p:here){
            var grate=p.isShiftKeyDown()&&LastCrossing.spared(p)?wetGrate(l,r,p):null;
            if(grate==null){d.crouch.remove(p.getUUID());continue;}
            int t=d.crouch.merge(p.getUUID(),1,Integer::sum);if(t<CALL_CROUCH)continue;d.crouch.remove(p.getUUID());
            if(r.tick-d.calledAt.getOrDefault(p.getUUID(),-1_000_000L)<CALL_COOLDOWN){LastCrossing.cue(p,"You breathe out by the grate. Nothing answers yet.");continue;}
            if(warded(l,r,p)){LastCrossing.mark(p,"CallWarded");LastCrossing.cue(p,"By the plate, the breath catches in your throat. Nothing answers.");continue;}
            call(l,r,p,LastCrossingSite.FLOOR_GRATES.contains(grate));
        }
    }
    /** A wet grate the reader is crouched at: one over the winch branch, or the ramp branch's grate into the pit. */
    @Nullable static BlockPos wetGrate(ServerLevel l,Run r,ServerPlayer p){
        var d=r.dayTwo;var all=new ArrayList<BlockPos>();if(d.winchOpen)all.addAll(LastCrossingSite.FLOOR_GRATES);if(d.rampOpen)all.addAll(LastCrossingSite.PIT_GRATES);
        for(var g:all)if(p.position().distanceToSqr(Vec3.atCenterOf(r.base.offset(g)))<2.2*2.2)return g;
        return null;
    }
    /** Within the plate's reach: the plate standing or hung near, or carried by someone near. */
    static boolean warded(ServerLevel l,Run r,ServerPlayer p){
        Vec3 plate=null;
        if(r.plateBy!=null){var c=LastCrossing.player(l,r.plateBy);if(c!=null)plate=c.position();}
        else if(r.plateAt!=null)plate=Vec3.atCenterOf(r.base.offset(r.plateAt));
        return plate!=null&&plate.distanceToSqr(p.position())<WARD*WARD;
    }
    static void call(ServerLevel l,Run r,ServerPlayer p,boolean floor){
        var d=r.dayTwo;var b=r.base;d.calledAt.put(p.getUUID(),r.tick);
        var own=LastCrossing.own(p);own.putInt("DayCalls",own.getInt("DayCalls")+1);LastCrossing.save(p,own);
        var mouths=new ArrayList<BlockPos>(List.of(LastCrossingSite.PIPE_A_UP,LastCrossingSite.PIPE_A_DOWN,LastCrossingSite.PIPE_B_UP,LastCrossingSite.PIPE_B_DOWN));
        mouths.addAll(LastCrossingSite.FLOOR_GRATES);mouths.addAll(LastCrossingSite.PIT_GRATES);
        for(var m:List.of(LastCrossingSite.PIPE_A_DOWN,LastCrossingSite.PIPE_B_DOWN,floor?LastCrossingSite.FLOOR_GRATES.get(0):LastCrossingSite.PIT_GRATES.get(0)))
            LastCrossing.sound(l,b.offset(m),CrossingRegistry.SCRAPE.get(),.9F,1F);
        for(var q:LastCrossing.readers(l,b)){
            if(q==p){LastCrossing.mark(q,"HeardScrape");LastCrossing.cue(q,"You crouch by the wet grate and breathe out, slow. Down in the branch, something scrapes an answer, and comes.");continue;}
            var rel=LastCrossing.rel(q.position(),b);boolean near=LastCrossingSite.inIntake(rel)||LastCrossingSite.inCableHouse(rel)||LastCrossingSite.inPit(rel);
            for(var m:mouths)near|=q.position().distanceToSqr(Vec3.atCenterOf(b.offset(m)))<6*6;
            if(near){LastCrossing.mark(q,"HeardScrape");LastCrossing.cue(q,"Something scrapes, deep in the pipes, and stops.");}
        }
        d.rising.add(new Rise(r.tick+RISE_AFTER,floor));
    }
    /** The thing called comes up through its grate, if the branch is still wet; otherwise it knocks once under the iron and goes. */
    static void rising(ServerLevel l,Run r,List<ServerPlayer> here){
        var d=r.dayTwo;var b=r.base;
        for(var it=d.rising.iterator();it.hasNext();){var rise=it.next();if(r.tick<rise.at())continue;it.remove();
            boolean wet=rise.floor()?d.winchOpen:d.rampOpen;var grate=rise.floor()?LastCrossingSite.FLOOR_GRATES.get(0):LastCrossingSite.PIT_GRATES.get(0);
            if(!wet){LastCrossing.sound(l,b.offset(grate),CrossingRegistry.TAP.get(),.8F,.8F);continue;}
            var u=CrossingRegistry.UNDERTOW_TYPE.get().create(l);if(u==null)continue;var at=LastCrossing.world(b,rise.floor()?LastCrossingSite.FLOOR_RISE:LastCrossingSite.PIT_RISE);
            u.addTag(LabyrinthSpawnRules.ALLOWED_TAG);u.addTag("crossing_undertow");u.moveTo(at.x,at.y,at.z,180,0);
            if(!l.addFreshEntity(u))continue;
            d.mobs.put(u.getUUID(),r.tick+DAY_STAY);
            l.sendParticles(ParticleTypes.SPLASH,at.x,at.y+.3,at.z,24,.5,.2,.5,.2);LastCrossing.sound(l,BlockPos.containing(at),CrossingRegistry.UNDERTOW.get(),1F,1F);
            for(var p:here){if(!LastCrossing.sees(p,at.add(0,1,0),16,.2))continue;LastCrossing.mark(p,"SawUndertow");LastCrossing.cue(p,"Something comes up through the grate in broad daylight, streaming.");}
        }
    }
    /** What came up by day goes back into the water after a while, or off the station; while it is up, it passes a carrier by. */
    static void mobs(ServerLevel l,Run r,List<ServerPlayer> here){
        var d=r.dayTwo;var b=r.base;
        for(var it=d.mobs.entrySet().iterator();it.hasNext();){var e=it.next();
            if(!(l.getEntity(e.getKey()) instanceof Undertow u)||!u.isAlive()){it.remove();continue;}
            var q=LastCrossing.rel(u.position(),b);
            if(r.tick>=e.getValue()||!LastCrossingSite.atStation(q)&&!LastCrossingSite.onBridge(q)){LastCrossingNight.sink(l,u);it.remove();continue;}
            if(r.tick%20==0)passedBy(l,r,u,here);
        }
    }
    /** One of the things from the water close to a carrier, going for someone else or nobody: the carrier knows, and whoever sees it. */
    static void passedBy(ServerLevel l,Run r,Undertow u,List<ServerPlayer> here){
        for(var c:here){if(!LastCrossing.spared(c)||u.distanceToSqr(c)>3.5*3.5||u.getTarget()==c)continue;
            LastCrossing.mark(c,"PassedBy");
            for(var w:here)if(w!=c&&!LastCrossing.spared(w)&&LastCrossing.sees(w,u.position().add(0,1,0),14,.3))LastCrossing.addName(w,"SawPassedBy",LastCrossing.name(c));}
    }

    // ------------------------------------------------------------------------------------------------ the station set back
    static void reset(ServerLevel l,Run r){r.dayTwo=new Day();r.dayNo=1;}
}
