package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.533, June's lengths at first light, a week in (docs/PELL_HOUSE_WEEK_0_4_533.md), from the proposal's "2. A week in": "The pool:
 * the cover is off. June swims lengths at dawn."
 *
 * <p>At first light June comes out of her room in her swimming things and her towelling robe. She lets herself out by the sliding door
 * and locks it behind her, takes the padlock off the pool's gate, goes in and shuts the gate behind her. At the chair she takes the robe
 * off and leaves it there; she gets in at the shallow end and swims her lengths down the middle, under the lane rope and back, without
 * stopping. Then she gets out, puts the robe on, padlocks the gate behind her, comes in by the sliding door, locks it, and goes to her
 * room to dress. The hour holds at half past five while she is out, and she gets up for the day at six as she always does.
 *
 * <p>While she is in the water the padlock hangs open on the gate's hasp, and a reader can open the gate and go in. Wherever she has to
 * shut something behind her she waits until nobody is on its far side or in its way, and asks them out. A reader awake who sees her in
 * the water keeps it. While every reader at the house is asleep, she is back in her room at once.
 */
public final class PellSwim {
    private PellSwim(){}
    /** Half past five, where the hour holds while she is out; how long she takes to dress after; how many lengths; how long a length. */
    public static final long HALF_FIVE=23500;
    public static final int DRESS=100,LENGTHS=6,LENGTH=110;
    /** Where she is: not out yet; on her way down to the water; in it; back out and home. */
    static final int NOT_YET=0,DOWN=1,IN=2,HOME=3;
    /** The chair, where the robe is left; the lane she swims down, its two ends, and how high in the water she lies (and how far
     *  under the rope she goes). */
    public static final BlockPos CHAIR=new BlockPos(-14,0,-86);
    static final double LANE_X=-6.5,SHALLOW_Z=-80.3,DEEP_Z=-91.7,IN_WATER=-.62,UNDER=.38;
    static Vec3 v(double x,double z){return new Vec3(x,0,z);}
    /** Her ways: from her room to inside the sliding door; out onto the patio; from the step to the pool's gate; through it; from inside
     *  the gate to the chair; from the chair to the shallow end's middle; and back the same way, and in, and to her room. */
    static final Vec3[] ROOM_TO_SLIDER={v(8.5,-63.4),v(8.5,-65.6),v(0,-65.6),v(0,-68.5),v(.5,-70.6)},
        SLIDER_TO_ROOM={v(0,-68.5),v(0,-65.6),v(8.5,-65.6),v(8.5,-63.4),v(PellVisit.HER_ROOM.x(),PellVisit.HER_ROOM.z())};
    static final Vec3 STEP=v(.5,-73.6),INSIDE=v(.5,-70.6),GATE_OUT=v(-3.5,-74.6),GATE_IN=v(-3.5,-77.4),CORNER=v(-12.6,-77.6),
        AT_CHAIR=v(-12.6,-85.5),EDGE_CORNER=v(-12.6,-78.5),EDGE=v(LANE_X,-78.5);

    /** Whether June is out at the pool this morning: from leaving her room until she is back in it. */
    static boolean out(PellPeople.Household h){return h.swim!=NOT_YET&&!h.swum;}
    /** For tests and for a hand on her: whether she is in the water. */
    public static boolean inWater(BlockPos b){return PellPeople.household(b).swim==IN&&!PellPeople.household(b).swum;}
    /** For tests: whether she has had her swim this morning and is back in. */
    public static boolean swum(BlockPos b){return PellPeople.household(b).swum;}
    /** For tests: how many lengths she has swum. */
    public static int lengths(BlockPos b){return PellPeople.household(b).lengths;}

    /** The swim undone, for the visit beginning again: the robe off the chair, the gate padlocked. */
    static void restart(ServerLevel l,BlockPos b,PellPeople.Household h){
        var june=PellPeople.find(l,b,PellPeople.JUNE);if(june!=null)june.setNoGravity(false);
        h.swim=NOT_YET;h.lengths=0;h.swum=false;h.lengthAt=0;h.swumAt=0;
        chair(l,b,0);gate(l,b,1);
    }
    private static void step(PellPeople.Household h,int swim){h.swim=swim;h.waypoint=0;h.nudgedAt=0;}

    /** Every tick from first light until she is back in her room (PellMorning.tick hands it on, a week in). */
    static void tick(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,List<ServerPlayer> here,long now){
        int J=PellPeople.JUNE;boolean still=h.way[J].isEmpty();
        switch(h.swim){
            case NOT_YET->{
                // Out of her room in her swimming things and her robe.
                if(!still)return;
                if(!PellSite.open(l,b,PellSite.JUNE_DOOR))PellHouse.toggle(l,b,PellSite.JUNE_DOOR);
                june.gesture(PellPeople.ROBE);
                step(h,DOWN);PellVisit.go(h,J,-1,180,ROOM_TO_SLIDER);
            }
            case DOWN->down(l,b,h,june,here,now,still);
            case IN->swim(l,b,h,june,here,now);
            default->home(l,b,h,june,here,now,still);
        }
        seen(l,b,h,june,here,now);
    }
    /** Down to the water: out by the sliding door, locking it behind her; in at the gate, shutting it; the robe off at the chair; in. */
    private static void down(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,List<ServerPlayer> here,long now,boolean still){
        int J=PellPeople.JUNE;if(!still)return;
        switch(h.waypoint){
            case 0->{
                if(PellVisit.locked(b,PellSite.SLIDER)){PellVisit.unlock(b,PellSite.SLIDER);lock(l,b,PellSite.SLIDER);}
                if(!PellSite.open(l,b,PellSite.SLIDER))PellHouse.toggle(l,b,PellSite.SLIDER);
                PellVisit.go(h,J,-1,0,v(.5,-72.4),STEP);h.waypoint=1;
            }
            case 1->{
                // On the step, turned back to it: shut and locked once nobody is in the house or the doorway.
                h.rest[J]=0;
                if(!shut(l,b,h,june,PellSite.SLIDER,r->PellSite.inHouse(r),here,now,PellTexts.JUNE_BACK_OUT))return;
                h.locked.add(PellSite.SLIDER);lock(l,b,PellSite.SLIDER);
                PellVisit.go(h,J,-1,180,GATE_OUT);h.waypoint=2;
            }
            case 2->{
                // At the gate: the padlock off, the gate open, and in.
                lock(l,b,PellSite.POOL_GATE);gate(l,b,2);sound(l,b,PellSite.POOL_GATE,SoundEvents.FENCE_GATE_OPEN,.7F,1.1F);
                PellVisit.go(h,J,-1,0,GATE_IN);h.waypoint=3;
            }
            case 3->{
                // Inside, turned back to it: shut once nobody is in its way. The padlock stays open on the hasp while she swims.
                h.rest[J]=0;
                if(!PellVisit.clearOf(b,PellSite.POOL_GATE,here))return;
                if(gate(l,b)==2){gate(l,b,0);sound(l,b,PellSite.POOL_GATE,SoundEvents.FENCE_GATE_CLOSE,.7F,1.1F);}
                PellVisit.go(h,J,-1,90,CORNER,AT_CHAIR);h.waypoint=4;
            }
            case 4->{
                // At the chair: the robe off, dropped over it.
                chair(l,b,1);var at=Vec3.atCenterOf(b.offset(CHAIR));l.playSound(null,at.x,at.y,at.z,SoundEvents.WOOL_PLACE,SoundSource.NEUTRAL,.6F,.9F);
                june.gesture(PellPeople.SWIM);
                PellVisit.go(h,J,-1,180,EDGE_CORNER,EDGE);h.waypoint=5;
            }
            default->{
                // At the edge of the shallow end: in.
                var at=PellVisit.world(b,new Vec3(LANE_X,IN_WATER,SHALLOW_Z));
                june.setNoGravity(true);june.moveTo(at.x,at.y,at.z,180,0);june.setYHeadRot(180);june.setYBodyRot(180);june.setDeltaMovement(Vec3.ZERO);
                splash(l,at,1F);
                step(h,IN);h.lengths=0;h.lengthAt=now;
            }
        }
    }
    /** In the water: her lengths down the middle and back, under the rope at the step, a turn at each wall. */
    private static void swim(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,List<ServerPlayer> here,long now){
        int J=PellPeople.JUNE;h.moving[J]=true;
        if(h.hurried)h.lengths=LENGTHS;
        if(h.lengths>=LENGTHS){step(h,HOME);return;}
        double frac=Math.min(1,(now-h.lengthAt)/(double)LENGTH);boolean deep=h.lengths%2==0;
        double z=deep?SHALLOW_Z+(DEEP_Z-SHALLOW_Z)*frac:DEEP_Z+(SHALLOW_Z-DEEP_Z)*frac;
        double dip=Math.abs(z-PellWeek.ROPE_Z)<.8?UNDER:0;
        var at=PellVisit.world(b,new Vec3(LANE_X,IN_WATER-dip,z));float look=deep?180:0;
        june.moveTo(at.x,at.y,at.z,look,0);june.setYHeadRot(look);june.setYBodyRot(look);june.setDeltaMovement(Vec3.ZERO);
        if(now%3==0)l.sendParticles(ParticleTypes.SPLASH,at.x,b.getY()+PellWeek.WATERLINE,at.z+(deep?-.6:.6),3,.3,0,.3,0);
        if(now%16==0)l.playSound(null,at.x,at.y+.5,at.z,SoundEvents.PLAYER_SWIM,SoundSource.NEUTRAL,.5F,.85F+l.random.nextFloat()*.2F);
        if(frac<1)return;
        // The wall: a turn, and the next length.
        h.lengths++;h.lengthAt=now;splash(l,at,.6F);
    }
    /** Out of the water, the robe on, the gate padlocked behind her, in by the sliding door, locked, and to her room. */
    private static void home(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,List<ServerPlayer> here,long now,boolean still){
        int J=PellPeople.JUNE;
        switch(h.waypoint){
            case 0->{
                // Up out of the water onto the edge.
                var at=PellVisit.world(b,EDGE);splash(l,at,.8F);
                june.setNoGravity(false);june.moveTo(at.x,at.y,at.z,0,0);june.setYHeadRot(0);june.setYBodyRot(0);june.setDeltaMovement(Vec3.ZERO);
                PellVisit.go(h,J,-1,-90,EDGE_CORNER,AT_CHAIR);h.waypoint=1;
            }
            case 1->{
                if(!still)return;
                // The robe on again from the chair.
                chair(l,b,0);june.gesture(PellPeople.ROBE);
                var at=Vec3.atCenterOf(b.offset(CHAIR));l.playSound(null,at.x,at.y,at.z,SoundEvents.WOOL_PLACE,SoundSource.NEUTRAL,.6F,1.1F);
                PellVisit.go(h,J,-1,0,CORNER,GATE_IN);h.waypoint=2;
            }
            case 2->{
                if(!still)return;
                if(gate(l,b)!=2){gate(l,b,2);sound(l,b,PellSite.POOL_GATE,SoundEvents.FENCE_GATE_OPEN,.7F,1.1F);}
                PellVisit.go(h,J,-1,180,GATE_OUT);h.waypoint=3;
            }
            case 3->{
                if(!still)return;
                // Outside, turned back to it: shut and padlocked once nobody is inside the pool's fence or in its way.
                h.rest[J]=180;
                if(!shut(l,b,h,june,PellSite.POOL_GATE,PellSite::inPoolYard,here,now,PellTexts.JUNE_OUT_OF_POOL))return;
                gate(l,b,1);sound(l,b,PellSite.POOL_GATE,SoundEvents.FENCE_GATE_CLOSE,.7F,1.1F);lock(l,b,PellSite.POOL_GATE);
                PellVisit.go(h,J,-1,0,STEP);h.waypoint=4;
            }
            case 4->{
                if(!still)return;
                // At the sliding door: unlocked, opened, and in.
                if(PellVisit.locked(b,PellSite.SLIDER)){PellVisit.unlock(b,PellSite.SLIDER);lock(l,b,PellSite.SLIDER);}
                if(!PellSite.open(l,b,PellSite.SLIDER))PellHouse.toggle(l,b,PellSite.SLIDER);
                PellVisit.go(h,J,-1,180,v(.5,-71.4),INSIDE);h.waypoint=5;
            }
            case 5->{
                if(!still)return;
                // Inside, turned to it: shut and locked once nobody else has come in after her; then to her room.
                h.rest[J]=180;
                if(!shut(l,b,h,june,PellSite.SLIDER,r->PellSite.inHouse(r),here,now,PellTexts.JUNE_BACK_OUT))return;
                h.locked.add(PellSite.SLIDER);lock(l,b,PellSite.SLIDER);
                PellVisit.go(h,J,-1,PellVisit.HER_ROOM.look(),SLIDER_TO_ROOM);h.waypoint=6;
            }
            default->{
                if(!still)return;
                // In her room: the door shut behind her once nobody is in it; she dresses.
                if(PellSite.open(l,b,PellSite.JUNE_DOOR)){if(!PellVisit.clearOf(b,PellSite.JUNE_DOOR,here))return;PellHouse.toggle(l,b,PellSite.JUNE_DOOR);}
                h.swum=true;h.swumAt=now;
            }
        }
    }
    /**
     * A door or the gate shut behind June: once nobody is on its far side (a reader there is asked out, now and then) and nobody is in
     * its way. True once it may be.
     */
    private static boolean shut(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,BlockPos door,Predicate<Vec3> beyond,List<ServerPlayer> here,long now,String ask){
        boolean someone=false;
        for(var p:here){if(!p.isAlive()||p.isSpectator())continue;if(beyond.test(p.position().subtract(b.getX(),b.getY(),b.getZ())))someone=true;}
        if(someone){
            if(now-h.nudgedAt>=PellVisit.NUDGE&&PellVisit.ready(h,now,june)){PellVisit.say(h,june,PellPeople.JUNE,ask,now);h.nudgedAt=now;}
            return false;
        }
        if(!PellVisit.clearOf(b,door,here))return false;
        if(door.equals(PellSite.POOL_GATE))return true;
        if(PellSite.open(l,b,door))PellHouse.toggle(l,b,door);
        return true;
    }
    /** A reader awake who sees her in the water keeps it. */
    private static void seen(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,List<ServerPlayer> here,long now){
        if(h.swim!=IN||now%5!=0)return;
        for(var p:here){
            if(!p.isAlive()||p.isSpectator()||p.isSleeping()||!PellNight.sees(p,june.position().add(0,.4,0),PellNight.SEE))continue;
            var own=PellHouse.own(p);if(!own.getBoolean("SawJuneSwim")){own.putBoolean("SawJuneSwim",true);PellHouse.save(p,own);}
        }
    }

    // ------------------------------------------------------------------------------------------------ the gate and the chair
    /** The pool's gate: 0 shut, 1 shut and padlocked, 2 open. */
    static int gate(ServerLevel l,BlockPos b){var s=l.getBlockState(b.offset(PellSite.POOL_GATE));return PellBlock.is(s,PellBlock.Kind.POOL_GATE)?s.getValue(PellBlock.STAGE):1;}
    static void gate(ServerLevel l,BlockPos b,int stage){
        var at=b.offset(PellSite.POOL_GATE);var s=l.getBlockState(at);
        if(PellBlock.is(s,PellBlock.Kind.POOL_GATE)&&s.getValue(PellBlock.STAGE)!=stage)l.setBlock(at,s.setValue(PellBlock.STAGE,stage),3);
    }
    /** For tests: the gate's state. */
    public static int gateStage(ServerLevel l,BlockPos b){return gate(l,b);}
    /** A hand on the gate: while its padlock is off (June in the water) it opens and shuts. False while it is padlocked. */
    static boolean gateHand(ServerPlayer p,ServerLevel l,BlockPos b){
        int g=gate(l,b);if(g==1)return false;
        gate(l,b,g==2?0:2);sound(l,b,PellSite.POOL_GATE,g==2?SoundEvents.FENCE_GATE_CLOSE:SoundEvents.FENCE_GATE_OPEN,.7F,1F);
        if(g==0)PellHouse.cue(p,PellTexts.POOL_GATE_OPEN);
        return true;
    }
    /** The pool chair: the towel on its back (0), and the robe dropped over it too while she swims (1). */
    static void chair(ServerLevel l,BlockPos b,int stage){
        var at=b.offset(CHAIR);var s=l.getBlockState(at);
        if(PellThing.is(s,PellThing.Kind.POOL_CHAIR)&&s.getValue(PellThing.STAGE)!=stage)l.setBlock(at,s.setValue(PellThing.STAGE,stage),3);
    }
    private static void lock(ServerLevel l,BlockPos b,BlockPos door){var at=Vec3.atBottomCenterOf(b.offset(door)).add(0,1,0);l.playSound(null,at.x,at.y,at.z,PellRegistry.LOCK.get(),SoundSource.BLOCKS,.7F,door.equals(PellSite.POOL_GATE)?.8F:1F);}
    private static void sound(ServerLevel l,BlockPos b,BlockPos at,SoundEvent s,float volume,float pitch){var c=Vec3.atCenterOf(b.offset(at));l.playSound(null,c.x,c.y,c.z,s,SoundSource.BLOCKS,volume,pitch);}
    private static void splash(ServerLevel l,Vec3 at,float volume){
        l.playSound(null,at.x,at.y+.6,at.z,SoundEvents.GENERIC_SPLASH,SoundSource.NEUTRAL,volume,1F);
        l.sendParticles(ParticleTypes.SPLASH,at.x,Math.floor(at.y)+1,at.z,12,.4,.1,.4,0);
    }
}
