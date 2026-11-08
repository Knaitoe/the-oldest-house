package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.labyrinth.CrossingBlock.Kind;
import io.github.knaitoe.theoldesthouse.labyrinth.LastCrossing.Run;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * 0.4.352, The Last Crossing, Night One (docs/LAST_CROSSING_0_4_352.md), from the owner's design: "The first surge comes through the
 * intake when the crew puts the pump under load ... A thing comes through the broken corner of the grate and gets one claw in the intake
 * worker before they can pull back. That contact is the authored first infection. A quick rescue still matters."
 *
 * <p>The night starts at dusk and runs four minutes on the station's clock. Once the pump is under load (primed before dusk, or primed
 * after Vera calls the valve down), the surge comes through the intake and a hand comes up through the grate's broken corner and takes
 * hold of the reader working the inlet: the authored first infection, the run's first slot. If nobody is below, it waits under the grate
 * for the first of the crew to come near. The grip holds them where they stand and hurts them a little at a time (never below three
 * hearts); it lets go when it is struck four times from the intake, by them or anyone else, or when they take the hand someone reaches
 * down through the inspection landing's rail. Meanwhile the intake fills; the brake held or latched above keeps the pump fighting the water
 * and slows it. Left too long, the lower floor floods: the grip lets go as the water comes over them and whoever is below comes up the
 * stair streaming, and the crew has lost the lower floor for the run. Then the isolation valve by the landing keeps the upper floor dry;
 * left open, the things from the water come up through the landing too. Once clear, the carrier has a private beat: one breath, and
 * another under it, and the certainty of changing at the next dusk; afterwards, now and then, that second breath again, heard by nobody
 * else. They can say so to Vera aloud, lay a palm on the plate, or keep it.
 *
 * <p>From the spillway, the things that come up out of the water (Undertow) climb over its banks a few at a time, as many as the crew,
 * and go for whoever is nearest. The cable house's door can be barred; left open and unwatched, one in there for a few seconds knocks the
 * seated roller off again. At the first rise the ferry strains at its line: made fast at the head of the ramp it holds, and someone can
 * still throw a turn of the loose line round the cleat in time; otherwise it swings off its sill. A clean reader can be cut by them, and
 * can die. At dawn the things go back into the water and the station holds in the morning light: Day Two is the next slice.
 */
public final class LastCrossingNight {
    private LastCrossingNight(){}
    /** Ticks: the surge after the pump takes its load; the surge regardless if the intake never primes; the intake's flood under a grip,
     * counted double while nobody holds the brake; how often the grip hurts; blows that break it; how long a hand reached down is held out;
     * the brake latch's hold; the first of the things from the water and how often after; how long before dawn they stop coming; how long
     * a lost, unisolated lower floor waits before they come up through the landing; the first rise and how long the ferry strains; how long
     * one in the cable house takes to foul the roller; the carrier's second breath; how long a rescued carrier runs with water. The surge,
     * the things from the water, the lost floor's grace and the rise run on the night's own clock; the grip, the hand, the latch and the
     * carrier's beat on the station's ticks. */
    public static final int SURGE_AFTER=200,SURGE_LATE=1200,GRIP_FLOOD=400,GRIP_HURT=40,BLOWS=4,HAND_OFFER=100,LATCH=300,FIRST_SPAWN=400,SPAWN_EVERY=300,
        SPAWN_STOP=600,ISOLATE_GRACE=600,RISE_AT=1600,RISE_WINDOW=400,FOUL_AFTER=60,SYMPTOM_EVERY=900,SOAKED=600;
    /** The grip never takes a reader below three hearts. */
    static final float GRIP_FLOOR=6F;

    static final class Night {
        int tick;int primedAt=-1;boolean surged,waiting;
        @Nullable UUID gripped;long gripAt=-1;Vec3 gripPos;int flood,blows;final LinkedHashSet<UUID> struck=new LinkedHashSet<>();
        @Nullable UUID offeredBy;long offerAt=-1;
        @Nullable UUID carrier;boolean freed;String freedHow="";long beatAt=-1;int beat;long gaugeUntil=-1;
        @Nullable UUID brakeBy;long latchUntil=-1;
        boolean lowerFlooded,isolated;int floodedAt=-1;
        boolean barred,rose,held,loose,fouled,dawn;int riseUntil=-1;int fouling;
        int spawned,landingSpawns;int lastSpawn=-1;final Set<UUID> mobs=new HashSet<>();
        final Map<UUID,Long> soakedUntil=new HashMap<>();
    }

    // ------------------------------------------------------------------------------------------------ the night's clock
    /** Dusk: Vera goes in to the gauge room and keeps the night there. */
    static void begin(ServerLevel l,Run r){
        LastCrossing.route(r,new Vec3(13.5,0,-35.5),new Vec3(26.5,0,-35.5),new Vec3(26.5,0,-50.5),new Vec3(18.5,0,-50.5),new Vec3(18.5,0,-53.5),new Vec3(16.5,0,-59.5),LastCrossingSite.VERA_NIGHT);
    }
    static void tick(ServerLevel l,Run r,List<ServerPlayer> here){
        var n=r.night;n.tick++;
        if(r.primed&&n.primedAt<0)n.primedAt=n.tick;
        surge(l,r,n);
        grip(l,r,n,here);
        beat(l,r,n,here);
        mobs(l,r,n,here);
        rise(l,r,n,here);
        if(n.latchUntil>=0&&r.tick>=n.latchUntil){n.latchUntil=-1;if(n.brakeBy==null)brake(l,r,false);}
        if(n.tick>=LastCrossing.NIGHT_TICKS)dawn(l,r,n,here);
    }
    /** The needle when the night moves it: down into the branches as the worker is pulled out. */
    static int gauge(Run r){return r.night.gaugeUntil>r.tick?0:-1;}

    // ------------------------------------------------------------------------------------------------ the surge and the grip
    static void surge(ServerLevel l,Run r,Night n){
        if(n.surged||n.dawn)return;
        boolean due=n.primedAt>=0?n.tick-n.primedAt>=SURGE_AFTER:n.tick>=SURGE_LATE;
        if(!due)return;
        n.surged=true;var b=r.base;var grate=b.offset(LastCrossingSite.GRATE);
        LastCrossing.sound(l,grate,CrossingRegistry.SURGE.get(),1.4F,1F);
        l.sendParticles(ParticleTypes.SPLASH,grate.getX()+.5,grate.getY()+1.1,grate.getZ()+.5,30,1.5,.2,1.5,.2);
        for(var p:LastCrossing.readers(l,b)){var q=LastCrossing.rel(p.position(),b);
            if(LastCrossingSite.inIntake(q)||LastCrossingSite.inGaugeRoom(q)){LastCrossing.mark(p,"HeardSurge");LastCrossing.cue(p,"The surge hits the intake: the pipes slam and water roars off into them.");}}
        var worker=worker(l,r,6);
        if(worker!=null){seize(l,r,n,worker);LastCrossing.speak(l,r,LastCrossingTexts.GRIPPED_CALL,40);}
        else{n.waiting=true;LastCrossing.speak(l,r,LastCrossingTexts.SURGE_CALL,40);}
    }
    /** The reader working the inlet: the intake worker if they are below near the grate, else whoever of the crew is nearest it. */
    @Nullable static ServerPlayer worker(ServerLevel l,Run r,double reach){
        var grate=Vec3.atCenterOf(r.base.offset(LastCrossingSite.GRATE).above());
        var job=LastCrossing.player(l,r.intakeBy);
        if(job!=null&&eligible(l,r,job)&&job.position().distanceToSqr(grate)<reach*reach)return job;
        ServerPlayer best=null;double d=reach*reach;
        for(var p:LastCrossing.readers(l,r.base)){if(!eligible(l,r,p))continue;double q=p.position().distanceToSqr(grate);if(q<d){d=q;best=p;}}
        return best;
    }
    static boolean eligible(ServerLevel l,Run r,ServerPlayer p){return !p.isSpectator()&&p.isAlive()&&r.crew.containsKey(p.getUUID())&&LastCrossing.readers(l,r.base).contains(p)&&LastCrossingSite.inIntake(LastCrossing.rel(p.position(),r.base));}
    /** A claw in them: the run's first slot, held where they stand. */
    static void seize(ServerLevel l,Run r,Night n,ServerPlayer p){
        var b=r.base;n.waiting=false;n.gripped=p.getUUID();n.gripAt=r.tick;n.gripPos=p.position();n.flood=0;n.blows=0;n.struck.clear();
        if(n.carrier==null)n.carrier=p.getUUID();
        l.setBlock(b.offset(LastCrossingSite.GRATE),LastCrossingSite.fit(Kind.GRATE,Direction.NORTH,2),Block.UPDATE_CLIENTS);
        float dmg=Math.min(2F,Math.max(0F,p.getHealth()-GRIP_FLOOR));if(dmg>0)p.hurt(p.damageSources().generic(),dmg);
        var own=LastCrossing.own(p);own.putBoolean("Gripped",true);
        if(p.getUUID().equals(n.carrier)){own.putBoolean("Infected",true);own.putInt("Slot",1);}
        LastCrossing.save(p,own);LastCrossing.job(p,"intake");
        LastCrossing.cue(p,LastCrossingTexts.CLAWED);
        sawClaw(l,r);
    }
    /** Whoever sees the hand in the grate: from the intake, or looking down through the landing from the gauge room. */
    static void sawClaw(ServerLevel l,Run r){
        var at=Vec3.atCenterOf(r.base.offset(LastCrossingSite.GRATE).above());
        for(var q:LastCrossing.readers(l,r.base)){if(q.getUUID().equals(r.night.gripped)||LastCrossing.own(q).getBoolean("SawClaw"))continue;var rel=LastCrossing.rel(q.position(),r.base);
            boolean down=LastCrossingSite.inGaugeRoom(rel)&&q.getXRot()>40&&rel.x>LastCrossingSite.LANDING_W-3&&rel.x<LastCrossingSite.LANDING_E+4&&rel.z<LastCrossingSite.LANDING_S+4&&rel.z>LastCrossingSite.LANDING_N-3;
            if(down||LastCrossing.sees(q,at,14,.3)){LastCrossing.mark(q,"SawClaw");LastCrossing.cue(q,"Below, a hand has come up through the corner of the grate, and it has hold of someone.");}}
    }
    static void grip(ServerLevel l,Run r,Night n,List<ServerPlayer> here){
        // Nobody was below at the surge: it waits under the grate for the first of the crew to come near.
        if(n.waiting&&n.gripped==null&&!n.dawn){var p=worker(l,r,2.5);if(p!=null){seize(l,r,n,p);LastCrossing.speak(l,r,LastCrossingTexts.GRIPPED_CALL,40);}}
        if(n.gripped==null)return;
        var p=LastCrossing.player(l,n.gripped);
        if(p==null||!here.contains(p)||!p.isAlive()){free(l,r,n,"gone",null);return;}
        // Held where they stand.
        if(p.position().distanceToSqr(n.gripPos)>.36){p.teleportTo(l,n.gripPos.x,n.gripPos.y,n.gripPos.z,p.getYRot(),p.getXRot());}
        p.setDeltaMovement(Vec3.ZERO);p.hurtMarked=true;
        if((r.tick-n.gripAt)%GRIP_HURT==0&&r.tick>n.gripAt&&p.getHealth()>GRIP_FLOOR+.5F)p.hurt(p.damageSources().generic(),Math.min(1F,p.getHealth()-GRIP_FLOOR));
        if(r.tick%10==0){l.sendParticles(ParticleTypes.BUBBLE_POP,p.getX(),p.getY()+.2,p.getZ(),4,.4,.1,.4,.02);sawClaw(l,r);}
        // The hand reached down is only held out so long.
        if(n.offeredBy!=null&&r.tick-n.offerAt>HAND_OFFER){n.offeredBy=null;if(Talk.replies(p).contains("Take the hand."))Talk.close(p);}
        // The intake filling: slowed while someone holds or latches the brake above.
        var holder=LastCrossing.player(l,n.brakeBy);
        if(holder!=null&&(!here.contains(holder)||holder.position().distanceToSqr(Vec3.atCenterOf(r.base.offset(LastCrossingSite.BRAKE)))>2.5*2.5)){n.brakeBy=null;if(n.latchUntil<r.tick)brake(l,r,false);holder=null;}
        boolean held=holder!=null||n.latchUntil>r.tick;
        n.flood+=held?1:2;
        if(n.flood>=GRIP_FLOOD)floodLower(l,r,n);
    }
    /** The grip broken: by blows, by a hand from above, by the water, or because the one it held is gone. */
    static void free(ServerLevel l,Run r,Night n,String how,@Nullable ServerPlayer by){
        var b=r.base;var was=n.gripped;var p=LastCrossing.player(l,was);n.gripped=null;n.offeredBy=null;n.freed=true;n.freedHow=how;
        l.setBlock(b.offset(LastCrossingSite.GRATE),LastCrossingSite.fit(Kind.GRATE,Direction.NORTH,1),Block.UPDATE_CLIENTS);
        n.brakeBy=null;n.latchUntil=-1;brake(l,r,false);
        // Gone from the station while it held them: the carrier's beat waits for them to come back.
        if(how.equals("gone")){if(was!=null&&was.equals(n.carrier)&&n.beatAt<0)n.beatAt=r.tick;return;}
        LastCrossing.sound(l,b.offset(LastCrossingSite.GRATE),SoundEvents.DROWNED_HURT_WATER,1F,.6F);
        // The needle drops into the branches as the worker comes free; whoever is at the gauge sees it.
        n.gaugeUntil=r.tick+40;
        for(var q:LastCrossing.readers(l,b))if(q.position().distanceToSqr(Vec3.atCenterOf(b.offset(LastCrossingSite.GAUGE)))<4*4){LastCrossing.mark(q,"SawPressure");LastCrossing.cue(q,"On the gauge the needle drops: pressure going out into the service branches.");}
        if(p==null)return;
        var own=LastCrossing.own(p);own.putString("Freed",how);if(how.equals("fought"))own.putBoolean("FoughtFree",true);
        if(by!=null&&by!=p&&how.equals("hand"))own.putString("TookHand",LastCrossing.name(by));
        LastCrossing.save(p,own);
        if(p.getUUID().equals(n.carrier)){n.beatAt=r.tick+40;n.soakedUntil.put(p.getUUID(),r.tick+SOAKED);}
        LastCrossing.speak(l,r,LastCrossingTexts.UP_HERE,40);
    }
    /** Hitting the hand in the grate: four blows from the intake and it lets go. */
    static boolean strike(ServerPlayer p){
        var r=LastCrossing.runOf(p);if(r==null||r.night.gripped==null)return false;var n=r.night;var l=p.serverLevel();
        var grate=Vec3.atCenterOf(r.base.offset(LastCrossingSite.GRATE).above());
        if(p.position().distanceToSqr(grate)>4*4)return false;
        n.blows++;n.struck.add(p.getUUID());LastCrossing.sound(l,r.base.offset(LastCrossingSite.GRATE),SoundEvents.ZOMBIE_ATTACK_IRON_DOOR,.6F,1.4F);
        l.sendParticles(ParticleTypes.SPLASH,grate.x-.3,grate.y,grate.z-.3,6,.2,.1,.2,.1);
        if(!p.getUUID().equals(n.gripped))LastCrossing.mark(p,"StruckHand");
        if(n.blows<BLOWS){LastCrossing.cue(p,n.blows==1?"You hit it. It holds.":"You hit it again. The fingers loosen.");return true;}
        boolean alone=n.struck.size()==1&&n.struck.contains(n.gripped);
        for(var id:n.struck){var q=LastCrossing.player(l,id);if(q!=null)LastCrossing.cue(q,"The hand lets go and slides back down through the corner into the black water.");}
        free(l,r,n,alone?"fought":"struck",p);return true;
    }
    /** A hand reached down through the landing's rail to the one held below. */
    static boolean reachDown(ServerPlayer p,BlockPos rel){
        var r=LastCrossing.runOf(p);if(r==null||r.night.gripped==null||p.getUUID().equals(r.night.gripped))return false;var n=r.night;var l=p.serverLevel();
        if(rel.getY()!=0||rel.getX()<LastCrossingSite.LANDING_W-1||rel.getX()>LastCrossingSite.LANDING_E+1||rel.getZ()>LastCrossingSite.LANDING_S+1||rel.getZ()<LastCrossingSite.LANDING_N-1)return false;
        if(!LastCrossingSite.inGaugeRoom(LastCrossing.rel(p.position(),r.base)))return false;
        var held=LastCrossing.player(l,n.gripped);if(held==null)return false;
        n.offeredBy=p.getUUID();n.offerAt=r.tick;
        LastCrossing.cue(p,"You lean over the rail and reach down through the landing.");
        LastCrossing.begin(held,"A hand from above");
        Talk.line(held,"",LastCrossing.name(p)+" is leaning over the landing's rail, reaching down to you.");
        Talk.ask(held,Talk.act("Take the hand.",LastCrossingNight::takeHand),Talk.act("Not yet.",Talk::close));
        return true;
    }
    static void takeHand(ServerPlayer q){
        Talk.close(q);var r=LastCrossing.runOf(q);if(r==null)return;var n=r.night;var l=q.serverLevel();
        if(!q.getUUID().equals(n.gripped))return;
        var by=LastCrossing.player(l,n.offeredBy);
        if(by==null||r.tick-n.offerAt>HAND_OFFER||!LastCrossingSite.inGaugeRoom(LastCrossing.rel(by.position(),r.base))){LastCrossing.cue(q,"The hand's gone back over the rail.");return;}
        free(l,r,n,"hand",by);
        var to=LastCrossingSite.PULLED_UP.get(0);double best=Double.MAX_VALUE;
        for(var spot:LastCrossingSite.PULLED_UP){double d=LastCrossing.world(r.base,spot).distanceToSqr(by.position());if(d<best){best=d;to=spot;}}
        var at=LastCrossing.world(r.base,to);q.teleportTo(l,at.x,at.y,at.z,q.getYRot(),q.getXRot());
        var own=LastCrossing.own(by);own.putString("PulledUp",LastCrossing.name(q));LastCrossing.save(by,own);
        LastCrossing.cue(by,"You get a grip on "+LastCrossing.name(q)+" and haul. The thing below lets go, and you drag them up over the rail.");
        LastCrossing.cue(q,"You take "+LastCrossing.name(by)+"'s hand. Something below pulls once more, then lets go, and you come up over the rail.");
    }
    /** The brake above held, or latched, while the intake is filling. */
    static boolean brakeUse(ServerPlayer p){
        var r=LastCrossing.runOf(p);if(r==null||r.night.gripped==null)return false;var n=r.night;var l=p.serverLevel();
        if(p.isShiftKeyDown()){n.latchUntil=r.tick+LATCH;brake(l,r,true);LastCrossing.mark(p,"Latched");LastCrossing.job(p,"gauge");
            LastCrossing.cue(p,"A latch catches the brake and holds it open. It won't hold long.");return true;}
        n.brakeBy=p.getUUID();brake(l,r,true);LastCrossing.mark(p,"HeldBrake");LastCrossing.job(p,"gauge");
        LastCrossing.cue(p,"You lean on the brake and hold it. Below, the pump fights the water.");return true;
    }
    static void brake(ServerLevel l,Run r,boolean open){
        var at=r.base.offset(LastCrossingSite.BRAKE);var want=LastCrossingSite.fit(Kind.BRAKE,Direction.WEST,open?1:0);
        if(!l.getBlockState(at).equals(want))l.setBlock(at,want,Block.UPDATE_CLIENTS);
    }

    // ------------------------------------------------------------------------------------------------ the lower floor lost
    static void floodLower(ServerLevel l,Run r,Night n){
        var b=r.base;n.lowerFlooded=true;n.floodedAt=n.tick;
        for(int x=LastCrossingSite.PUMP_W+1;x<LastCrossingSite.PUMP_E;x++)for(int y=LastCrossingSite.LOWER;y<=-3;y++)for(int z=LastCrossingSite.PUMP_S-1;z>LastCrossingSite.PUMP_N;z--){
            var at=b.offset(x,y,z);if(l.getBlockState(at).isAir())l.setBlock(at,Blocks.WATER.defaultBlockState(),Block.UPDATE_CLIENTS);}
        LastCrossing.sound(l,b.offset(LastCrossingSite.GRATE),CrossingRegistry.SURGE.get(),1.2F,.8F);
        var held=LastCrossing.player(l,n.gripped);
        free(l,r,n,"flood",null);
        // Everyone below comes up the stair, streaming.
        int i=0;
        for(var p:LastCrossing.readers(l,b)){var q=LastCrossing.rel(p.position(),b);
            if(LastCrossingSite.inIntake(q)){var to=LastCrossing.world(b,LastCrossingSite.FLOOD_OUT.add(i++%3,0,0));p.teleportTo(l,to.x,to.y,to.z,p.getYRot(),p.getXRot());
                n.soakedUntil.put(p.getUUID(),r.tick+SOAKED);
                LastCrossing.cue(p,p==held?"The water comes up over you all at once and the grip lets go. You come up the stair streaming.":"The water comes up over the lower floor all at once. You come up the stair streaming.");}
            if(LastCrossingSite.inIntake(q)||LastCrossingSite.inGaugeRoom(q))LastCrossing.mark(p,"LowerFlooded");}
        LastCrossing.speak(l,r,LastCrossingTexts.FLOODED_CALL,40);
    }
    static boolean isolate(ServerPlayer p,BlockPos pos,int stage){
        var r=LastCrossing.runOf(p);if(r==null)return false;var n=r.night;var l=p.serverLevel();
        if(stage==1){LastCrossing.cue(p,"It's shut. The upper floor's on its own pump now.");return true;}
        if(!n.lowerFlooded){LastCrossing.cue(p,"The isolation valve for the lower floor. You'd only shut it if the lower floor went.");return true;}
        l.setBlock(pos,LastCrossingSite.fit(Kind.ISOLATOR,Direction.SOUTH,1),Block.UPDATE_CLIENTS);LastCrossing.sound(l,pos,SoundEvents.IRON_TRAPDOOR_CLOSE,1F,.6F);
        n.isolated=true;LastCrossing.mark(p,"Isolated");LastCrossing.job(p,"gauge");
        LastCrossing.cue(p,"You heave the isolation valve shut. Below, the water stops climbing; up here the pump draws the floor dry.");
        LastCrossing.speak(l,r,LastCrossingTexts.ISOLATED,24);
        return true;
    }

    // ------------------------------------------------------------------------------------------------ after the grip: the carrier's beat
    static void beat(ServerLevel l,Run r,Night n,List<ServerPlayer> here){
        var b=r.base;
        // Whoever came up out of the intake runs with water, and the carrier bleeds; those who see them come up remember.
        for(var e:List.copyOf(n.soakedUntil.entrySet())){var p=LastCrossing.player(l,e.getKey());
            if(p==null||r.tick>e.getValue()||!here.contains(p)){n.soakedUntil.remove(e.getKey());continue;}
            if(r.tick%10==0){l.sendParticles(ParticleTypes.DRIPPING_WATER,p.getX(),p.getY()+1.2,p.getZ(),3,.25,.4,.25,0);
                if(p.getUUID().equals(n.carrier))l.sendParticles(new DustParticleOptions(new Vector3f(.45F,.05F,.05F),1F),p.getX(),p.getY()+.9,p.getZ(),2,.2,.3,.2,0);}
            var q=LastCrossing.rel(p.position(),b);
            if(r.tick%20==0&&LastCrossingSite.inGaugeRoom(q))for(var w:here){if(w==p||!LastCrossingSite.inGaugeRoom(LastCrossing.rel(w.position(),b))||!LastCrossing.sees(w,p.position().add(0,1,0),12,.3))continue;
                LastCrossing.addName(w,"SawSoaked",LastCrossing.name(p));}
        }
        carrierBeat(l,r,n,here);
    }
    /** The carrier's private beat once clear, and the second breath now and then afterwards; through the day after too (0.4.353). */
    static void carrierBeat(ServerLevel l,Run r,Night n,List<ServerPlayer> here){
        // A carrier who died here has no beat and no second breath: they watch from the far bank.
        var c=LastCrossing.player(l,n.carrier);if(c==null||!here.contains(c)||r.dead.contains(c.getUUID()))return;
        if(n.beatAt>=0&&r.tick>=n.beatAt&&n.beat==0){n.beat=1;c.playNotifySound(CrossingRegistry.BREATH.get(),SoundSource.PLAYERS,1F,1F);LastCrossing.cue(c,LastCrossingTexts.INFECTED_ONE);}
        else if(n.beat==1&&r.tick>=n.beatAt+70){n.beat=2;LastCrossing.cue(c,LastCrossingTexts.INFECTED_TWO);LastCrossing.mark(c,"Beat");}
        // Afterwards, now and then, the second breath under theirs.
        else if(n.beat==2&&(r.tick-n.beatAt)%SYMPTOM_EVERY==0){var own=LastCrossing.own(c);int k=own.getInt("Symptoms");own.putInt("Symptoms",k+1);LastCrossing.save(c,own);
            c.playNotifySound(CrossingRegistry.BREATH.get(),SoundSource.PLAYERS,.7F,.95F);if(k%2==0)LastCrossing.cue(c,LastCrossingTexts.SYMPTOMS.get((k/2)%LastCrossingTexts.SYMPTOMS.size()));}
    }

    // ------------------------------------------------------------------------------------------------ the things from the water
    static void mobs(ServerLevel l,Run r,Night n,List<ServerPlayer> here){
        var b=r.base;
        n.mobs.removeIf(id->!(l.getEntity(id) instanceof Undertow u&&u.isAlive()));
        int cap=Math.max(3,Math.min(LastCrossing.MAX_CREW,r.crew.size()));
        if(!n.dawn&&n.surged&&n.tick>=FIRST_SPAWN&&n.tick<LastCrossing.NIGHT_TICKS-SPAWN_STOP&&n.mobs.size()<cap&&(n.lastSpawn<0||n.tick-n.lastSpawn>=SPAWN_EVERY)){
            // With the lower floor lost and left open, every other one comes up through the landing.
            boolean landing=n.lowerFlooded&&!n.isolated&&n.tick-n.floodedAt>=ISOLATE_GRACE;
            var from=landing&&n.landingSpawns++%2==0?LastCrossingSite.LANDING_UP:n.spawned%2==0?LastCrossingSite.WEST_BANK:LastCrossingSite.EAST_BANK;
            spawn(l,r,n,from);
        }
        if(r.tick%20!=0)return;
        var door=LastCrossing.world(b,new Vec3(18.5,0,-50.5));
        for(var id:n.mobs){if(!(l.getEntity(id) instanceof Undertow u))continue;var q=LastCrossing.rel(u.position(),b);
            // Nothing of theirs leaves the station: off it, they go back into the water.
            if(!LastCrossingSite.atStation(q)&&!LastCrossingSite.onBridge(q)){sink(l,u);continue;}
            if(u.getTarget()==null&&u.getNavigation().isDone())u.getNavigation().moveTo(door.x,door.y,door.z,1.0);
            if(LastCrossingSite.inCableHouse(q))n.fouling+=20;
        }
        // One in the cable house a few seconds knocks the seated roller off again.
        if(n.fouling>=FOUL_AFTER&&!n.fouled){n.fouled=true;
            if(r.rollerSeated){r.rollerSeated=false;var at=b.offset(LastCrossingSite.LOOSE_ROLLER);l.setBlock(at,LastCrossingSite.fit(Kind.ROLLER,Direction.NORTH,1),Block.UPDATE_CLIENTS);
                LastCrossing.sound(l,at,SoundEvents.ANVIL_LAND,.5F,.8F);
                for(var p:here)if(LastCrossingSite.inCableHouse(LastCrossing.rel(p.position(),b)))LastCrossing.mark(p,"SawFouled");}}
    }
    static void spawn(ServerLevel l,Run r,Night n,Vec3 rel){
        var u=CrossingRegistry.UNDERTOW_TYPE.get().create(l);if(u==null)return;var at=LastCrossing.world(r.base,rel);
        u.addTag(LabyrinthSpawnRules.ALLOWED_TAG);u.addTag("crossing_undertow");
        u.moveTo(at.x,at.y,at.z,180,0);
        if(!l.addFreshEntity(u))return;
        n.mobs.add(u.getUUID());n.spawned++;n.lastSpawn=n.tick;
        l.sendParticles(ParticleTypes.SPLASH,at.x,at.y+.3,at.z,24,.5,.2,.5,.2);
        LastCrossing.sound(l,BlockPos.containing(at),CrossingRegistry.UNDERTOW.get(),1F,1F);
        for(var p:LastCrossing.readers(l,r.base)){if(!LastCrossing.sees(p,at.add(0,1,0),20,.2))continue;var own=LastCrossing.own(p);if(own.getBoolean("SawUndertow"))continue;own.putBoolean("SawUndertow",true);LastCrossing.save(p,own);
            LastCrossing.cue(p,rel==LastCrossingSite.LANDING_UP?"Something comes up through the landing out of the flooded floor below, streaming.":"Something comes up out of the spillway and over its edge, streaming.");}
    }
    static void sink(ServerLevel l,Undertow u){
        l.sendParticles(ParticleTypes.SPLASH,u.getX(),u.getY()+.3,u.getZ(),16,.4,.2,.4,.2);u.discard();
    }
    /** A clean reader cut by one of them. */
    static void wounded(ServerPlayer p){
        var r=LastCrossing.runOf(p);if(r==null)return;var own=LastCrossing.own(p);boolean first=!own.getBoolean("Wounded");own.putBoolean("Wounded",true);own.putInt("Wounds",own.getInt("Wounds")+1);LastCrossing.save(p,own);
        if(first&&!own.getBoolean("Infected"))LastCrossing.cue(p,"It opens a cut across your arm. Only a cut.");
    }

    // ------------------------------------------------------------------------------------------------ the cable house's door
    static boolean bar(ServerPlayer p,BlockPos rel,int stage){
        var r=LastCrossing.runOf(p);if(r==null)return false;var n=r.night;var l=p.serverLevel();var b=r.base;
        if(stage==0&&rel.equals(LastCrossingSite.BAR_REST)){
            var cell=b.offset(LastCrossingSite.BAR_ACROSS);
            if(!l.getEntitiesOfClass(LivingEntity.class,new AABB(cell),LivingEntity::isAlive).isEmpty()){LastCrossing.cue(p,"Someone's in the doorway.");return true;}
            var door=b.offset(LastCrossingSite.CABLE_DOOR);
            for(int y=0;y<=1;y++){var at=door.above(y);var s=l.getBlockState(at);if(s.getBlock() instanceof DoorBlock&&s.getValue(DoorBlock.OPEN))l.setBlock(at,s.setValue(DoorBlock.OPEN,false),Block.UPDATE_CLIENTS);}
            l.setBlock(b.offset(LastCrossingSite.BAR_REST),Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);
            l.setBlock(cell,LastCrossingSite.fit(Kind.BAR,Direction.NORTH,1),Block.UPDATE_CLIENTS);
            LastCrossing.sound(l,cell,SoundEvents.WOODEN_DOOR_CLOSE,1F,.6F);n.barred=true;LastCrossing.mark(p,"BarredDoor");LastCrossing.job(p,"lookout");
            LastCrossing.cue(p,"You shut the door and drop the bar across it into its brackets.");return true;}
        if(stage==1&&rel.equals(LastCrossingSite.BAR_ACROSS)){
            l.setBlock(b.offset(LastCrossingSite.BAR_ACROSS),Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);
            l.setBlock(b.offset(LastCrossingSite.BAR_REST),LastCrossingSite.fit(Kind.BAR,Direction.NORTH),Block.UPDATE_CLIENTS);
            n.barred=false;LastCrossing.mark(p,"UnbarredDoor");LastCrossing.cue(p,"You lift the bar off the brackets and stand it by the door.");return true;}
        return false;
    }

    // ------------------------------------------------------------------------------------------------ the first rise
    static void rise(ServerLevel l,Run r,Night n,List<ServerPlayer> here){
        var b=r.base;
        if(!n.rose&&n.tick>=RISE_AT){n.rose=true;
            LastCrossing.sound(l,b.offset(33,0,-68),CrossingRegistry.RISE.get(),1.2F,1F);
            for(var p:here)if(atRamp(p,b))LastCrossing.cue(p,"The water lifts. The ferry rides up on its sill and strains at its line.");
            if(r.lineStaged)held(l,r,n,null);else n.riseUntil=n.tick+RISE_WINDOW;}
        if(n.riseUntil>=0&&!n.held&&!n.loose&&n.tick>=n.riseUntil){n.loose=true;n.riseUntil=-1;
            var coil=b.offset(LastCrossingSite.RAMP_COIL);
            if(CrossingBlock.is(l.getBlockState(coil),Kind.COIL)){l.setBlock(coil,LastCrossingSite.fit(Kind.COIL,Direction.NORTH,3),Block.UPDATE_CLIENTS);LastCrossing.sound(l,coil,CrossingRegistry.SNAP.get(),1F,1F);}
            LastCrossing.sound(l,b.offset(33,0,-68),SoundEvents.WOOD_BREAK,1.2F,.6F);
            for(var p:here)if(atRamp(p,b)){LastCrossing.mark(p,"SawFerryLoose");LastCrossing.cue(p,"The ferry swings off its sill on the rise and fetches up against the cut, grinding.");}}
    }
    static boolean atRamp(ServerPlayer p,BlockPos b){var q=LastCrossing.rel(p.position(),b);return LastCrossingSite.atRamp(q)||LastCrossingSite.onFerry(q)||LastCrossingSite.inPit(q);}
    static void held(ServerLevel l,Run r,Night n,@Nullable ServerPlayer by){
        n.held=true;n.riseUntil=-1;
        if(by!=null){LastCrossing.mark(by,"HeldFerry");LastCrossing.job(by,"landing");}
        for(var p:LastCrossing.readers(l,r.base))if(atRamp(p,r.base)){LastCrossing.mark(p,"SawSill");LastCrossing.cue(p,"The line holds. The ferry settles back against its sill.");}
    }
    /** During the rise: the loose line on the ramp thrown round the cleat, or the line in hand made fast. */
    static boolean holdLine(ServerPlayer p,BlockPos rel,int stage){
        var r=LastCrossing.runOf(p);if(r==null)return false;var n=r.night;var l=p.serverLevel();
        if(n.riseUntil<0||n.held||n.loose||!rel.equals(LastCrossingSite.RAMP_COIL)||stage!=0)return false;
        l.setBlock(r.base.offset(LastCrossingSite.RAMP_COIL),Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);
        l.setBlock(r.base.offset(LastCrossingSite.STAGED_COIL),LastCrossingSite.fit(Kind.COIL,Direction.NORTH,2),Block.UPDATE_CLIENTS);
        r.lineIn=true;r.lineStaged=true;LastCrossing.cue(p,"You snatch up the loose line, throw a turn round the cleat and haul it tight.");
        held(l,r,n,p);return true;
    }
    /** The line in hand made fast at the head of the ramp during the rise. */
    static void madeFast(ServerPlayer p,Run r){if(r.night.riseUntil>=0&&!r.night.held&&!r.night.loose)held(p.serverLevel(),r,r.night,p);}

    // ------------------------------------------------------------------------------------------------ dawn
    static void dawn(ServerLevel l,Run r,Night n,List<ServerPlayer> here){
        n.dawn=true;n.gripped=null;n.waiting=false;r.phase=LastCrossing.DAWN;
        for(var id:n.mobs)if(l.getEntity(id) instanceof Undertow u)sink(l,u);
        n.mobs.clear();brake(l,r,false);n.latchUntil=-1;n.brakeBy=null;
        l.setBlock(r.base.offset(LastCrossingSite.GRATE),LastCrossingSite.fit(Kind.GRATE,Direction.NORTH,1),Block.UPDATE_CLIENTS);
        LastCrossing.speak(l,r,LastCrossingTexts.DAWN,64);
        for(var p:here){if(r.crew.containsKey(p.getUUID()))LastCrossing.mark(p,"NightOne");if(p.getUUID().equals(n.carrier))LastCrossing.cue(p,LastCrossingTexts.DAWN_CARRIER);}
        LastCrossing.route(r,new Vec3(16.5,0,-59.5),new Vec3(18.5,0,-53.5),new Vec3(18.5,0,-50.5),new Vec3(26.5,0,-50.5),new Vec3(26.5,0,-35.5),new Vec3(13.5,0,-35.5),LastCrossingSite.VERA_PORCH);
    }

    // ------------------------------------------------------------------------------------------------ the station set back
    static void reset(ServerLevel l,Run r){
        var b=r.base;
        var area=new AABB(b.offset(LastCrossingSite.WEST,LastCrossingSite.LOW,LastCrossingSite.NORTH)).minmax(new AABB(b.offset(LastCrossingSite.EAST,LastCrossingSite.HIGH,1)));
        for(var u:l.getEntitiesOfClass(Undertow.class,area))u.discard();
        r.night=new Night();
    }
    /** For the record: who said so aloud, and who heard them. */
    static void told(ServerPlayer p){
        var r=LastCrossing.runOf(p);if(r==null)return;var l=p.serverLevel();LastCrossing.mark(p,"Told");
        for(var q:LastCrossing.readers(l,r.base))if(q!=p&&q.distanceToSqr(p)<12*12)LastCrossing.addName(q,"ToldBy",LastCrossing.name(p));
    }
}
