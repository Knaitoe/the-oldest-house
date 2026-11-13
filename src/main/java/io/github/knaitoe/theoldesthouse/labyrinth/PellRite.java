package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.539, midnight in the pool (docs/PELL_HOUSE_POOL_0_4_539.md), from the proposal's "3. The last night": "At midnight June takes
 * Tilly into the water. June talks softly and calls her Nessa." And from "The last night, in play": "Tilly in the water. The calls: June
 * calls her Nessa, softly, again and again. A character Tilly holds out for a fixed time. No drowning: if breath runs out, June lets her
 * up into the air and asks again. Nobody drowns. The failure is the name, not the water. Getting her out. Getting to her: the older
 * sibling has to get into the pool and take Tilly's hand. The way out: guide her out of the water and across the yard to the gap.
 * A character Tilly follows the reader's hand. June stays in the water calling the name. She does not chase."
 *
 * <ul>
 * <li><b>Midnight.</b> On the last night, locked up, the hour comes to midnight. June comes out of her room in her robe, goes to Nessa's
 * room and wakes Tilly: "Nessa. Up you get, love. Come with Mum." Tilly: "I'm Tilly." June takes her hand and leads her out by the
 * sliding door, unlocked and left open, through the pool's gate, unlocked and left open, to the chair, where the robe comes off, and
 * along the shallow end's edge into the water. Tilly has no cane; she is led.</li>
 * <li><b>In the water</b> June stands behind her with her hands on her shoulders, both facing the deep end, and calls her Nessa, softly,
 * again and again. Tilly answers with her own name, fainter as it goes on. Now and then June presses her under for a few seconds and lets
 * her up into the air, and asks again. The splashing wakes anybody asleep on the camp bed.</li>
 * <li><b>Holding out.</b> Three minutes in June's hands, and Tilly answers: "Yes, Mum." June: "There's my girl. There she is." She
 * takes her out of the water and back to Nessa's bed, puts her robe on at the chair on the way, and sits on the edge of the bed beside her
 * for the rest of the night.</li>
 * <li><b>A hand.</b> A reader in reach of her in the water (in it, or right at its edge) takes her hand. She holds on, and comes up if
 * she was under. June's hands come away; she stays where she is and goes on calling the name. Tilly follows the hand: through the water
 * (she keeps to the shallow end), up out of it where the reader stands at its edge, and across the yard, at a careful walk. She never
 * steps into the pool on her own, nor onto the line where it is unbroken: her bare foot finds the paint and she stops, and goes along it
 * if the hand is further along. A hand gone more than a few steps off lets go of her, and she stands where she is until a hand takes
 * hers again; let go in the water, in a few seconds June wades to her and takes her back into her hands, where the holding out goes on.</li>
 * <li><b>Out.</b> On a cell of the line scrubbed out where it is broken, or anywhere outside the line, with the hand still hers, Tilly is
 * out. June stops calling.</li>
 * </ul>
 */
public final class PellRite {
    private PellRite(){}
    static final int J=PellPeople.JUNE,T=PellPeople.TILLY;
    /** Where the rite has got: not yet; June on her way to Nessa's room; waking Tilly; leading her down to the water; in the water; Tilly
     *  has answered; June taking her back to bed; Tilly in bed again, June beside her; Tilly out through the gap. */
    public static final int NOT_YET=0,FETCH=1,WAKE=2,LEAD=3,WATER=4,ANSWERED=5,HOME=6,KEPT=7,OUT=8;
    /** How long Tilly holds out in June's hands; how often June calls; how often June presses her under, and for how long; how long a
     *  girl let go in the water stands before June wades to her and takes her back. */
    public static final int HOLD_OUT=3600,CALL_EVERY=110,UNDER_EVERY=320,UNDER_FOR=50,TAKE_BACK=100;
    /** How near a reader comes to take her hand; how far off they go before it slips out of theirs; how near she comes up behind them;
     *  how far up out of the water a reader at its edge lifts her; how near June walks ahead of her, holding her hand; how far a reader is
     *  from her to hear her answer. */
    public static final double REACH=2.5,LET_GO=5,BESIDE=1.3,LIFT=2.2,HAND=1.1,HEARD=TalkHistory.HEARD;
    /** How fast she follows a hand on land and in the water, and how fast June wades. */
    static final double LED_PACE=.15,WATER_PACE=.1,WADE_PACE=.05;
    /** Heights in the water, the shallow end being deeper than either of them is tall: June's feet, Tilly's, and Tilly's under. */
    public static final double JUNE_Y=-1.2,TILLY_Y=-.95,UNDER_Y=-1.85;
    /** Where June first holds her: Tilly in the middle of the shallow end, June a step behind her (south), both facing the deep end. */
    public static final double HOLD_X=PellSwim.LANE_X,HOLD_Z=-81.25,BEHIND=.75;
    /** The shallow end, where Tilly can be in the water (a little in from its walls). */
    static final double IN_W=PellSite.WATER_W+.35,IN_E=PellSite.WATER_E+1-.35,IN_N=PellSite.DEEP_S+1+.35,IN_S=PellSite.WATER_S+1-.35;
    static Vec3 v(double x,double z){return new Vec3(x,0,z);}
    /** June's ways: her room to Nessa's bed; Nessa's bed to inside the sliding door; out to the gate; to the chair; to the shallow end's
     *  edge; and back, from the edge to the chair, from the chair to Nessa's bed. */
    static final Vec3[] TO_NESSA={v(8.5,-63.4),v(8.5,-65.6),v(0,-65.6),v(-9.5,-65.6),v(-9.5,-64.6),v(-9.5,-62.8),v(-10.6,-59.7),v(-11.5,-59.6)},
        TO_SLIDER={v(-10.6,-59.7),v(-9.5,-62.8),v(-9.5,-64.6),v(-9.5,-65.6),v(0,-65.6),v(0,-68.5),PellSwim.INSIDE},
        TO_GATE={v(.5,-72.4),PellSwim.STEP,PellSwim.GATE_OUT},
        TO_CHAIR={PellSwim.GATE_IN,PellSwim.CORNER,PellSwim.AT_CHAIR},
        TO_EDGE={PellSwim.EDGE_CORNER,PellSwim.EDGE},
        BACK_TO_CHAIR={PellSwim.EDGE_CORNER,PellSwim.AT_CHAIR},
        BACK_TO_BED={PellSwim.CORNER,PellSwim.GATE_IN,PellSwim.GATE_OUT,PellSwim.STEP,v(.5,-72.4),PellSwim.INSIDE,v(0,-68.5),v(0,-65.6),v(-9.5,-65.6),
            v(-9.5,-64.6),v(-9.5,-62.8),v(-10.6,-59.7),v(-11.5,-59.6)};

    // ------------------------------------------------------------------------------------------------ for tests
    public static int step(BlockPos b){return PellPeople.household(b).rite;}
    public static boolean held(BlockPos b){var h=PellPeople.household(b);return h.rite==WATER&&h.inHold;}
    public static boolean under(BlockPos b){return PellPeople.household(b).under;}
    public static @Nullable java.util.UUID leader(BlockPos b){return PellPeople.household(b).leader;}
    public static long heldFor(BlockPos b){return PellPeople.household(b).heldFor;}
    /** Every line said in the rite so far, as "Who: line". */
    public static List<String> lines(BlockPos b){return List.copyOf(PellPeople.household(b).riteLines);}
    /** For tests: June and Tilly already in the water, June holding her, as the walk down leaves them. */
    public static void inWater(ServerLevel l,BlockPos b){
        var h=PellPeople.household(b);var june=PellPeople.find(l,b,J);var tilly=PellPeople.find(l,b,T);if(june==null||tilly==null)return;
        PellVisit.unperch(b,h,J,june);h.asleep[T]=false;h.perch[T]=null;h.way[J].clear();h.way[T].clear();
        if(PellVisit.locked(b,PellSite.SLIDER))PellVisit.unlock(b,PellSite.SLIDER);
        if(!PellSite.open(l,b,PellSite.SLIDER))PellHouse.toggle(l,b,PellSite.SLIDER);
        PellSwim.gate(l,b,2);PellSwim.chair(l,b,1);
        h.time=PellNight.TWELVE;
        into(l,b,h,june,tilly,l.getGameTime());
    }
    /** For tests: Tilly a breath from answering, if nobody takes her hand. */
    public static void nearlyAnswers(BlockPos b){var h=PellPeople.household(b);h.heldFor=Math.max(h.heldFor,HOLD_OUT-40);}

    /** The rite undone, for the visit beginning again. */
    static void restart(ServerLevel l,BlockPos b,PellPeople.Household h){
        h.rite=NOT_YET;h.inHold=false;h.under=false;h.reply=false;h.afterUnder=false;h.heldFor=0;h.underAt=0;h.callAt=0;h.letGoAt=0;h.lineToldAt=0;
        h.leader=null;h.calls=0;h.replies=0;h.tookBack=false;h.riteLines.clear();h.hands.clear();
        h.juneLeader=null;h.juneSat=false;h.juneCalledAt=0;h.juneTrail.clear();
        var june=PellPeople.find(l,b,J);if(june!=null)june.setNoGravity(false);
        var tilly=PellPeople.find(l,b,T);if(tilly!=null)tilly.setNoGravity(false);
    }

    /** Every tick on the last night from locking up: midnight, when it comes, and what follows. */
    static void tick(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,List<ServerPlayer> here,long now){
        var tilly=PellPeople.find(l,b,T);if(tilly==null)return;
        boolean still=h.way[J].isEmpty();
        switch(h.rite){
            case NOT_YET->{
                // Midnight: out of her room in her robe, to Nessa's.
                if(h.time<PellNight.TWELVE||!still)return;
                june.gesture(PellPeople.ROBE);
                PellVisit.go(h,J,-1,0,TO_NESSA);step(h,FETCH);
            }
            case FETCH->{if(still){h.rest[J]=PellPeople.yawTo(june,tilly.position());step(h,WAKE);}}
            case WAKE->wake(l,b,h,june,tilly,now);
            case LEAD->{follow(l,b,h,june,tilly);lead(l,b,h,june,tilly,here,now,still);}
            case WATER->{water(l,b,h,june,tilly,here,now);byHer(l,b,h,june,here,now);}
            case ANSWERED->answered(l,b,h,june,tilly,here,now);
            case HOME->{follow(l,b,h,june,tilly);home(l,b,h,june,tilly,still);}
            case OUT->{if(h.leader!=null)led(l,b,h,june,tilly,here,now);byHer(l,b,h,june,here,now);}
            default->{}
        }
    }
    private static void step(PellPeople.Household h,int rite){h.rite=rite;h.waypoint=0;}
    private static void say(PellPeople.Household h,LiteraryActor a,int who,String line,long now){
        PellVisit.say(h,a,who,line,now);h.riteLines.add(PellPeople.name(who)+": "+line);
    }

    /** At Nessa's bed: Tilly woken by the name, her own given back, and her hand taken. */
    private static void wake(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,LiteraryActor tilly,long now){
        switch(h.waypoint){
            case 0->{if(PellVisit.ready(h,now,june)){say(h,june,J,PellTexts.JUNE_RITE_WAKE,now);h.waypoint=1;}}
            case 1->{
                if(!PellVisit.ready(h,now,june))return;
                // Up out of the bed, standing beside it, her hand out for June's. Her cane stays by the bed.
                h.asleep[T]=false;h.perch[T]=null;var at=PellVisit.world(b,PellVisit.TILLY_BED.beside());
                tilly.moveTo(at.x,at.y,at.z,PellPeople.yawTo(tilly,june.position()),0);tilly.setDeltaMovement(Vec3.ZERO);
                tilly.gesture(PellPeople.LED);h.rest[T]=tilly.getYRot();
                say(h,tilly,T,PellTexts.TILLY_RITE_NAME,now);h.waypoint=2;
            }
            default->{
                if(!PellVisit.ready(h,now,june))return;
                say(h,june,J,PellTexts.JUNE_RITE_HAND,now);
                june.gesture(PellPeople.LEADS);h.trail.clear();
                PellVisit.go(h,J,-1,180,TO_SLIDER);step(h,LEAD);
            }
        }
    }
    /** Down to the water: the sliding door unlocked and left open, the gate the same, the robe off at the chair, and in. */
    private static void lead(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,LiteraryActor tilly,List<ServerPlayer> here,long now,boolean still){
        if(!still)return;
        switch(h.waypoint){
            case 0->{
                if(PellVisit.locked(b,PellSite.SLIDER)){PellVisit.unlock(b,PellSite.SLIDER);sound(l,b,PellSite.SLIDER,PellRegistry.LOCK.get(),.7F,1F);}
                if(!PellSite.open(l,b,PellSite.SLIDER))PellHouse.toggle(l,b,PellSite.SLIDER);
                PellVisit.go(h,J,-1,180,TO_GATE);h.waypoint=1;
            }
            case 1->{
                if(PellSwim.gate(l,b)!=2){sound(l,b,PellSite.POOL_GATE,PellRegistry.LOCK.get(),.7F,.8F);PellSwim.gate(l,b,2);sound(l,b,PellSite.POOL_GATE,SoundEvents.FENCE_GATE_OPEN,.7F,1.1F);}
                PellVisit.go(h,J,-1,90,TO_CHAIR);h.waypoint=2;
            }
            case 2->{
                // At the chair: the robe off, dropped over it, and on in her swimming things.
                PellSwim.chair(l,b,1);var c=Vec3.atCenterOf(b.offset(PellSwim.CHAIR));l.playSound(null,c.x,c.y,c.z,SoundEvents.WOOL_PLACE,SoundSource.NEUTRAL,.6F,.9F);
                june.gesture(PellPeople.LEADS_SWIM);
                PellVisit.go(h,J,-1,180,TO_EDGE);h.waypoint=3;
            }
            default->into(l,b,h,june,tilly,now);
        }
    }
    /** Into the shallow end, June behind Tilly with her hands on her shoulders. The splashing wakes whoever is asleep. */
    private static void into(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,LiteraryActor tilly,long now){
        h.holdX=HOLD_X;h.holdZ=HOLD_Z;
        place(june,b,h.holdX,JUNE_Y,h.holdZ+BEHIND,180);june.setNoGravity(true);june.gesture(PellPeople.HOLD);
        place(tilly,b,h.holdX,TILLY_Y,h.holdZ,180);tilly.setNoGravity(true);tilly.gesture(0);
        PellSwim.splash(l,PellVisit.world(b,new Vec3(h.holdX,PellWeek.WATERLINE,h.holdZ)),1F);
        h.inHold=true;h.under=false;h.reply=false;h.afterUnder=false;h.heldFor=0;h.leader=null;h.callAt=now-CALL_EVERY+40;
        PellMorning.wakeAll(l,b,h,PellHouse.readers(l,b),PellTexts.WOKEN_BY_POOL,"WokenByThePool");
        step(h,WATER);
    }

    // ------------------------------------------------------------------------------------------------ in the water
    private static void water(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,LiteraryActor tilly,List<ServerPlayer> here,long now){
        h.moving[J]=true;h.moving[T]=true;
        if(h.leader!=null)led(l,b,h,june,tilly,here,now);
        if(h.rite!=WATER)return;
        boolean free=h.juneLeader==null&&!h.juneSat;
        if(h.inHold)hold(l,b,h,june,tilly,here,now);
        else if(h.leader==null&&free)standing(l,b,h,june,tilly,now);
        // Her hands empty, June turns where she stands to watch the girl go.
        if(!h.inHold&&h.leader!=null&&free)face(june,tilly.position());
        if(h.rite==WATER&&free)calls(l,b,h,june,tilly,here,now);
        seen(l,b,h,tilly,here,now);
    }
    /** In June's hands: held at the surface, now and then under, and in the end the answer. */
    private static void hold(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,LiteraryActor tilly,List<ServerPlayer> here,long now){
        h.heldFor++;
        if(!h.under&&h.heldFor%UNDER_EVERY==0&&h.heldFor<HOLD_OUT-UNDER_FOR){
            h.under=true;h.underAt=now;h.reply=false;june.gesture(PellPeople.PRESS);
            var at=PellVisit.world(b,new Vec3(h.holdX,PellWeek.WATERLINE,h.holdZ));
            l.playSound(null,at.x,at.y,at.z,SoundEvents.GENERIC_SPLASH,SoundSource.NEUTRAL,.7F,1.2F);
            PellMorning.wakeAll(l,b,h,here,PellTexts.WOKEN_BY_POOL,"WokenByThePool");
        }
        if(h.under){
            var at=PellVisit.world(b,new Vec3(h.holdX,PellWeek.WATERLINE,h.holdZ));
            if(now%4==0)l.sendParticles(ParticleTypes.BUBBLE_POP,at.x,at.y,at.z,3,.2,0,.2,0);
            if((now-h.underAt)%16==0)l.playSound(null,at.x,at.y,at.z,SoundEvents.BUBBLE_COLUMN_UPWARDS_INSIDE,SoundSource.NEUTRAL,.5F,1.3F);
            if(now-h.underAt>=UNDER_FOR)up(l,b,h,june,tilly);
        }
        // June behind her, her hands on Tilly's shoulders; Tilly at the surface, or going under and coming up.
        place(june,b,h.holdX,JUNE_Y,h.holdZ+BEHIND,180);june.setXRot(25);
        double want=h.under?UNDER_Y:TILLY_Y,y=tilly.getY()-b.getY();
        place(tilly,b,h.holdX,y+Mth.clamp(want-y,-.15,.15),h.holdZ,180);
        // Held out long enough, she answers to it.
        if(h.heldFor>=HOLD_OUT&&!h.under&&PellVisit.ready(h,now,june)&&!tilly.saying()){
            say(h,tilly,T,PellTexts.TILLY_ANSWERS,now);
            for(var p:here){
                if(!p.isAlive()||p.isSpectator()||p.isSleeping()||p.distanceTo(tilly)>HEARD)continue;
                var own=PellHouse.own(p);if(!own.getBoolean("HeardTillyAnswer")){own.putBoolean("HeardTillyAnswer",true);PellHouse.save(p,own);}
            }
            h.inHold=false;step(h,ANSWERED);
        }
    }
    /** Let up into the air: a splash and a cough, and June asks again. */
    private static void up(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,LiteraryActor tilly){
        // June asks again as soon as she has her up.
        h.under=false;h.afterUnder=true;h.callAt=Math.min(h.callAt,l.getGameTime()-CALL_EVERY+15);june.gesture(PellPeople.HOLD);
        var at=PellVisit.world(b,new Vec3(h.holdX,PellWeek.WATERLINE,h.holdZ));
        PellSwim.splash(l,at,.6F);
        l.playSound(null,at.x,at.y+.4,at.z,SoundEvents.PLAYER_HURT_DROWN,SoundSource.NEUTRAL,.6F,1.6F);
    }
    /** Let go in the water, nobody's hand in hers: she stands there; in a while June wades to her and takes her back. */
    private static void standing(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,LiteraryActor tilly,long now){
        var r=rel(b,tilly.position());
        // 0.4.540: let go of on dry land on her way to the freezer, she stands where she is.
        if(!PellSite.inWater(r)||!PellSite.inWater(rel(b,june.position())))return;
        place(tilly,b,r.x,TILLY_Y,r.z,tilly.getYRot());
        if(now-h.letGoAt<TAKE_BACK){june.setXRot(0);return;}
        // To a step behind her, facing the deep end as she held her before.
        var j=rel(b,june.position());double tx=r.x,tz=Math.min(r.z+BEHIND,IN_S),dx=tx-j.x,dz=tz-j.z,d=Math.sqrt(dx*dx+dz*dz);
        if(d>.12){double s=Math.min(WADE_PACE,d);place(june,b,j.x+dx/d*s,JUNE_Y,j.z+dz/d*s,PellPeople.yawTo(june,tilly.position()));june.gesture(PellPeople.WADE);return;}
        h.holdX=r.x;h.holdZ=tz-BEHIND;h.inHold=true;h.tookBack=true;june.gesture(PellPeople.HOLD);tilly.gesture(0);
        place(tilly,b,h.holdX,TILLY_Y,h.holdZ,180);
    }
    /** June calls her Nessa, softly, again and again; Tilly in her hands answers with her own name, fainter as it goes on. */
    private static void calls(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,LiteraryActor tilly,List<ServerPlayer> here,long now){
        if(h.reply&&h.inHold&&!h.under&&PellVisit.ready(h,now,june)&&!tilly.saying()){
            h.reply=false;var lines=PellTexts.tillyHolds(h.heldFor,HOLD_OUT);
            if(lines.length>0)say(h,tilly,T,lines[h.replies++%lines.length],now);
            return;
        }
        // Taken back into her hands: the first thing she says, as soon as she can.
        if(h.tookBack&&h.inHold&&!h.under&&PellVisit.ready(h,now,june)&&!tilly.saying()){
            h.tookBack=false;say(h,june,J,PellTexts.JUNE_TAKES_BACK,now);h.callAt=now;h.reply=false;return;
        }
        if(h.under||now-h.callAt<CALL_EVERY||!PellVisit.ready(h,now,june)||tilly.saying())return;
        String line;
        if(h.afterUnder&&h.inHold){line=PellTexts.JUNE_AFTER_UNDER[h.calls++%PellTexts.JUNE_AFTER_UNDER.length];h.afterUnder=false;}
        else if(h.inHold)line=PellTexts.RITE_CALLS[h.calls++%PellTexts.RITE_CALLS.length];
        else line=PellTexts.JUNE_CALLS_AFTER[h.calls++%PellTexts.JUNE_CALLS_AFTER.length];
        say(h,june,J,line,now);h.callAt=now;h.reply=h.inHold;
        carry(b,june,line,here);
    }
    /** What June calls in the water carries across the yard: told in grey to readers on the lot too far off to hear her. */
    private static void carry(BlockPos b,LiteraryActor june,String line,List<ServerPlayer> here){
        for(var p:here){
            if(!p.isAlive()||p.isSpectator()||p.isSleeping()||p.distanceToSqr(june)<=PellVisit.HEAR*PellVisit.HEAR)continue;
            if(PellSite.onLot(rel(b,p.position())))PellHouse.cue(p,PellTexts.fromPool(line));
        }
    }
    /** A reader awake who sees her in June's hands keeps it. */
    private static void seen(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor tilly,List<ServerPlayer> here,long now){
        if(!h.inHold||now%5!=0)return;
        for(var p:here){
            if(!p.isAlive()||p.isSpectator()||p.isSleeping()||!PellNight.sees(p,tilly.position().add(0,.4,0),PellNight.SEE))continue;
            var own=PellHouse.own(p);if(!own.getBoolean("SawRite")){own.putBoolean("SawRite",true);PellHouse.save(p,own);}
        }
    }

    // ------------------------------------------------------------------------------------------------ the answer, and back to bed
    private static void answered(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,LiteraryActor tilly,List<ServerPlayer> here,long now){
        h.moving[J]=true;h.moving[T]=true;
        place(june,b,h.holdX,JUNE_Y,h.holdZ+BEHIND,180);june.setXRot(10);
        if(!PellVisit.ready(h,now,june)||tilly.saying()||june.saying())return;
        if(h.waypoint==0){say(h,june,J,PellTexts.JUNE_ANSWERED,now);h.waypoint=1;return;}
        // Out onto the edge, Tilly a step behind her, and away to the chair.
        var edge=PellSwim.EDGE;
        place(june,b,edge.x,0,edge.z,90);june.setNoGravity(false);june.setXRot(0);june.gesture(PellPeople.LEADS_SWIM);
        place(tilly,b,edge.x+HAND,0,edge.z,90);tilly.setNoGravity(false);tilly.gesture(PellPeople.LED);
        PellSwim.splash(l,PellVisit.world(b,new Vec3(edge.x,PellWeek.WATERLINE,edge.z-1)),.8F);
        h.trail.clear();PellVisit.go(h,J,-1,90,BACK_TO_CHAIR);step(h,HOME);
    }
    private static void home(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,LiteraryActor tilly,boolean still){
        if(!still)return;
        switch(h.waypoint){
            case 0->{
                // The robe on again from the chair.
                PellSwim.chair(l,b,0);june.gesture(PellPeople.LEADS);
                var c=Vec3.atCenterOf(b.offset(PellSwim.CHAIR));l.playSound(null,c.x,c.y,c.z,SoundEvents.WOOL_PLACE,SoundSource.NEUTRAL,.6F,1.1F);
                PellVisit.go(h,J,-1,0,BACK_TO_BED);h.waypoint=1;
            }
            default->{
                // Tilly in Nessa's bed again, and June sat on its edge beside her.
                PellVisit.lie(l,b,h,T,tilly,PellVisit.TILLY_SLEEPS);
                june.gesture(PellPeople.ROBE);PellVisit.perch(l,b,h,J,june,PellVisit.JUNE_BED);
                step(h,KEPT);
            }
        }
    }

    // ------------------------------------------------------------------------------------------------ Tilly and June's hand, and a reader's
    /** Where June has been, for Tilly to follow. */
    private static void trail(PellPeople.Household h,LiteraryActor june){
        if(h.trail.isEmpty()||h.trail.peekLast().distanceToSqr(june.position())>.04)h.trail.addLast(june.position());
        while(h.trail.size()>160)h.trail.pollFirst();
    }
    /** Led by June: a step behind her along the way she went, her hand in June's. */
    private static void follow(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,LiteraryActor tilly){
        trail(h,june);h.moving[T]=true;
        Vec3 to=null;
        for(var it=h.trail.descendingIterator();it.hasNext();){var at=it.next();if(at.distanceToSqr(june.position())>=HAND*HAND){to=at;break;}}
        if(to==null||tilly.position().distanceToSqr(to)<.04){face(tilly,june.position());return;}
        // With every reader asleep (the night hurried on), or held back out of sight, straight there.
        if(h.hurried||tilly.distanceToSqr(june)>36&&!LiteraryVignettes.watched(l,tilly.getEyePosition()))tilly.moveTo(to.x,to.y,to.z,tilly.getYRot(),0);
        else SceneFootpaths.walk(tilly,to,PellVisit.TILLY_PACE+.02);
        while(!h.trail.isEmpty()&&h.trail.peekFirst()!=to)h.trail.pollFirst();
    }

    /** A hand on Tilly. In the water, a reader in reach takes hers; let go, a hand takes hers again. True if it answered. */
    static boolean hand(ServerPlayer p,BlockPos b,PellPeople.Household h,LiteraryActor tilly,CompoundTag own){
        if(h.visit<PellWeek.LAST||h.rite==NOT_YET||h.rite==FETCH||h.rite==KEPT)return false;
        if(h.rite==WAKE||h.rite==LEAD||h.rite==ANSWERED||h.rite==HOME){PellHouse.cue(p,PellTexts.TILLY_WITH_JUNE);return true;}
        if(p.getUUID().equals(h.leader)){PellHouse.cue(p,PellTexts.TILLY_HAS_YOUR_HAND);return true;}
        if(h.leader!=null){PellHouse.cue(p,PellTexts.TILLY_HAS_A_HAND);return true;}
        if(PellSite.inWater(rel(b,tilly.position()))&&p.distanceTo(tilly)>REACH){PellHouse.cue(p,PellTexts.TILLY_OUT_OF_REACH);return true;}
        // Again: this reader's hand has had hers before tonight. A hand new to her, she asks after.
        var l=p.serverLevel();boolean again=h.hands.contains(p.getUUID()),wet=PellSite.inWater(rel(b,tilly.position()));var june=PellPeople.find(l,b,J);
        h.hands.add(p.getUUID());
        h.leader=p.getUUID();h.inHold=false;h.tookBack=false;tilly.gesture(PellPeople.LED);
        // Under when the hand finds her, she comes up with it.
        if(h.under&&june!=null)up(l,b,h,june,tilly);
        h.under=false;h.reply=false;h.afterUnder=false;
        if(june!=null){june.gesture(PellPeople.WADE);june.setXRot(0);}
        PellHouse.cue(p,again?PellTexts.TILLY_TAKES_AGAIN:wet?PellTexts.TILLY_TAKES:PellTexts.TILLY_TAKES_LAND);
        if(!again&&!tilly.saying()){tilly.say(PellPeople.name(T),PellTexts.TILLY_WHO);tilly.speaks(PellTexts.TILLY_WHO);h.riteLines.add(PellPeople.name(T)+": "+PellTexts.TILLY_WHO);}
        own.putBoolean("TookTillysHand",true);
        return true;
    }
    /** A hand on June at midnight: she does not stop for it. True if it answered. */
    static boolean june(ServerPlayer p,BlockPos b,PellPeople.Household h,LiteraryActor june,CompoundTag own){
        if(h.visit<PellWeek.LAST||h.rite==NOT_YET)return false;
        // 0.4.540: with the freezer open, her hands free of Tilly, June lets herself be led to it; there, she is told the name.
        if((h.rite==WATER||h.rite==OUT)&&PellFreezer.open(p.serverLevel(),b)&&freezerHand(p,b,h,june,own))return true;
        switch(h.rite){
            case WATER,ANSWERED->PellHouse.cue(p,h.inHold?PellTexts.JUNE_RITE_HOLDING:PellTexts.JUNE_RITE_CALLING);
            case OUT->PellHouse.cue(p,PellTexts.JUNE_RITE_STOPPED);
            case KEPT->{if(!june.saying()){june.say(PellPeople.name(J),PellTexts.JUNE_KEPT);june.speaks(PellTexts.JUNE_KEPT);}}
            default->{if(!june.saying()){var line=PellTexts.JUNE_RITE_BUSY[h.said[J]++%PellTexts.JUNE_RITE_BUSY.length];june.say(PellPeople.name(J),line);june.speaks(line);}}
        }
        return true;
    }
    // ------------------------------------------------------------------------------------------------ 0.4.540, June to the freezer
    /** How far apart the points June follows along a reader's way are, and how many are kept. */
    static final double TRAIL=.7;static final int TRAIL_MOST=96;
    /** How often June says the name while she is led. */
    static final int LED_CALL_EVERY=2*CALL_EVERY;
    static final String FREEZER_TALK="pell_freezer";
    /** A hand on June with the freezer open. True if it answered. */
    private static boolean freezerHand(ServerPlayer p,BlockPos b,PellPeople.Household h,LiteraryActor june,CompoundTag own){
        var l=p.serverLevel();
        if(h.juneSat){PellHouse.cue(p,PellTexts.JUNE_BY_HER);return true;}
        if(atFreezer(b,june)&&!h.inHold){ask(p,b);return true;}
        if(p.getUUID().equals(h.juneLeader)){PellHouse.cue(p,PellTexts.JUNE_HAS_YOUR_HAND);return true;}
        if(h.juneLeader!=null){PellHouse.cue(p,PellTexts.JUNE_HAS_A_HAND);return true;}
        if(h.inHold)return false;
        if(PellSite.inWater(rel(b,june.position()))&&p.distanceTo(june)>REACH){PellHouse.cue(p,PellTexts.JUNE_OUT_OF_REACH);return true;}
        h.juneLeader=p.getUUID();h.juneTrail.clear();h.juneCalledAt=l.getGameTime();
        june.gesture(PellPeople.JUNE_LED);june.setXRot(0);
        PellHouse.cue(p,PellTexts.JUNES_HAND);own.putBoolean("TookJunesHand",true);
        return true;
    }
    /** Whether June stands at the open freezer, in the laundry, near enough to be told the name there. */
    static boolean atFreezer(BlockPos b,LiteraryActor june){
        var r=rel(b,june.position());double dx=r.x-PellFreezer.NESSA_AT.x,dz=r.z-PellFreezer.NESSA_AT.z;
        return PellHand.inLaundry(r)&&dx*dx+dz*dz<=PellFreezer.AT_FREEZER*PellFreezer.AT_FREEZER;
    }
    /** At the open freezer: June looks at it, and at the reader; the reader can say the name to her, or nothing. */
    private static void ask(ServerPlayer p,BlockPos b){
        Talk.begin(p,FREEZER_TALK,PellPeople.name(J));Talk.line(p,PellPeople.name(J),PellTexts.JUNE_AT_FREEZER);
        Talk.ask(p,Talk.reply(PellTexts.TELL_NESSA,q->named(q,b)),Talk.reply(PellTexts.TELL_ITS_NESSA,q->named(q,b)),Talk.act(PellTexts.TELL_JUNE_NOTHING,q->{}));
    }
    /** The name, said to June at the open freezer: she sits down beside it and stops. */
    private static void named(ServerPlayer p,BlockPos b){
        var l=p.serverLevel();var h=PellPeople.household(b);var june=PellPeople.find(l,b,J);
        if(june==null||h.juneSat||!PellFreezer.open(l,b)||!atFreezer(b,june)||(h.rite!=WATER&&h.rite!=OUT))return;
        h.juneSat=true;h.juneLeader=null;h.juneTrail.clear();h.inHold=false;h.under=false;h.reply=false;
        june.setNoGravity(false);june.noPhysics=false;
        PellVisit.perch(l,b,h,J,june,PellFreezer.BY_HER);
        june.gesture(PellPeople.BY_HER);june.setXRot(30);
        var own=PellHouse.own(p);own.putBoolean("BroughtJune",true);PellHouse.save(p,own);
        for(var q:PellHouse.readers(l,b)){
            if(!q.isAlive()||q.isSpectator()||q.isSleeping())continue;
            if(q==p||q.distanceTo(june)<=PellVisit.HEAR){
                PellHouse.cue(q,PellTexts.JUNE_SITS);
                if(q!=p){var o=PellHouse.own(q);if(!o.getBoolean("SawJuneSit")){o.putBoolean("SawJuneSit",true);PellHouse.save(q,o);}}
            }else if(PellSite.onLot(rel(b,q.position())))PellHouse.cue(q,PellTexts.JUNE_STOPPED_INSIDE);
        }
    }
    /** June led by a reader's hand, or sat by the freezer: each tick of the water and after. */
    private static void byHer(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,List<ServerPlayer> here,long now){
        if(h.juneSat){
            // On the stool, her head bowed to the freezer; she is not turned by anybody near.
            h.moving[J]=true;var at=PellVisit.world(b,PellFreezer.NESSA_AT);
            float look=PellPeople.yawTo(june,at);june.setYHeadRot(look);june.setXRot(30);
            if(june.gesture()!=PellPeople.BY_HER)june.gesture(PellPeople.BY_HER);
            return;
        }
        if(h.juneLeader!=null)juneFollows(l,b,h,june,now);
    }
    /** After the hand: along the way the reader walked, a step behind; through the water, up out of it at the edge, and on. */
    private static void juneFollows(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,long now){
        h.moving[J]=true;
        var p=l.getServer().getPlayerList().getPlayer(h.juneLeader);
        if(p==null||p.level()!=l||!p.isAlive()||p.isSpectator()||p.distanceTo(june)>LET_GO){
            if(p!=null)PellHouse.cue(p,PellTexts.JUNE_LET_GO);
            h.juneLeader=null;h.juneTrail.clear();june.gesture(PellPeople.WADE);return;
        }
        var to=p.position();var last=h.juneTrail.peekLast();
        if(last==null||last.distanceToSqr(to)>=TRAIL*TRAIL){h.juneTrail.addLast(to);while(h.juneTrail.size()>TRAIL_MOST)h.juneTrail.pollFirst();}
        // Now and then, still, the name.
        if(now-h.juneCalledAt>=LED_CALL_EVERY&&!june.saying()){var line=PellTexts.JUNE_LED_CALLS[h.calls++%PellTexts.JUNE_LED_CALLS.length];say(h,june,J,line,now);h.juneCalledAt=now;}
        var at=june.position();
        if(horizontal(at,to)<=BESIDE){face(june,to);return;}
        while(h.juneTrail.size()>1&&horizontal(h.juneTrail.peekFirst(),at)<.4)h.juneTrail.pollFirst();
        var next=h.juneTrail.isEmpty()?to:h.juneTrail.peekFirst();
        var r=rel(b,at);
        if(PellSite.inWater(r)){wade(l,b,june,r,rel(b,next));return;}
        if(june.isNoGravity()){june.setNoGravity(false);june.noPhysics=false;}
        if(june.gesture()!=PellPeople.JUNE_LED)june.gesture(PellPeople.JUNE_LED);
        SceneFootpaths.walk(june,next,LED_PACE);
    }
    private static double horizontal(Vec3 a,Vec3 c){double dx=a.x-c.x,dz=a.z-c.z;return Math.sqrt(dx*dx+dz*dz);}
    /** June in the water after the hand: wading along the shallow end, and up out of it where the way leaves the water. */
    private static void wade(ServerLevel l,BlockPos b,LiteraryActor june,Vec3 r,Vec3 to){
        double dx=to.x-r.x,dz=to.z-r.z,d=Math.sqrt(dx*dx+dz*dz);
        if(d<1e-3)return;
        float yaw=(float)(Mth.atan2(dz,dx)*Mth.RAD_TO_DEG)-90;
        if(!PellSite.inWater(new Vec3(to.x,-.5,to.z))&&d<=LIFT){
            var at=PellVisit.world(b,new Vec3(to.x,0,to.z));
            if(l.noCollision(june,june.getDimensions(june.getPose()).makeBoundingBox(at))){
                PellSwim.splash(l,PellVisit.world(b,new Vec3(r.x,PellWeek.WATERLINE,r.z)),.9F);
                june.setNoGravity(false);june.noPhysics=false;june.gesture(PellPeople.JUNE_LED);
                june.moveTo(at.x,at.y,at.z,yaw,0);june.setYHeadRot(yaw);june.setYBodyRot(yaw);june.setDeltaMovement(Vec3.ZERO);
                return;
            }
        }
        double s=Math.min(WATER_PACE,d);
        double nx=Mth.clamp(r.x+dx/d*s,IN_W,IN_E),nz=Mth.clamp(r.z+dz/d*s,IN_N,IN_S);
        place(june,b,nx,JUNE_Y,nz,yaw);june.setNoGravity(true);june.gesture(PellPeople.JUNE_LED);
    }
    /** For tests: who has June's hand, and whether she sits by the freezer. */
    public static @Nullable UUID juneLeader(BlockPos b){return PellPeople.household(b).juneLeader;}
    public static boolean juneSat(BlockPos b){return PellPeople.household(b).juneSat;}

    private static @Nullable ServerPlayer leaderOf(ServerLevel l,PellPeople.Household h){
        if(h.leader==null)return null;var p=l.getServer().getPlayerList().getPlayer(h.leader);
        return p!=null&&p.level()==l&&p.isAlive()&&!p.isSpectator()?p:null;
    }
    /**
     * Tilly with a reader's hand: after them through the water and out of it, and across the yard, never into the pool on her own nor
     * onto the line where it is unbroken. A hand gone too far off lets go. Out through the gap, the rite is over.
     */
    private static void led(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,LiteraryActor tilly,List<ServerPlayer> here,long now){
        h.moving[T]=true;
        var p=leaderOf(l,h);
        if(p==null||p.distanceTo(tilly)>LET_GO){
            if(p!=null)PellHouse.cue(p,PellTexts.TILLY_LET_GO);
            h.leader=null;h.letGoAt=now;tilly.gesture(0);return;
        }
        var r=rel(b,tilly.position());var to=rel(b,p.position());
        if(PellSite.inWater(r)){swim(l,b,h,tilly,p,r,to);return;}
        double dx=to.x-r.x,dz=to.z-r.z;
        if(dx*dx+dz*dz<=BESIDE*BESIDE){face(tilly,p.position());out(l,b,h,june,tilly,p,here);return;}
        var before=tilly.position();
        SceneFootpaths.walk(tilly,p.position(),LED_PACE);
        if(blocked(l,b,tilly.position())){
            // Her bare foot on the paint (or at the pool's edge): back, and along it toward the hand if she can.
            tilly.moveTo(before.x,before.y,before.z,tilly.getYRot(),0);
            boolean moved=false;
            for(var s:new Vec3[]{new Vec3(Math.signum(dx)*Math.min(LED_PACE,Math.abs(dx)),0,0),new Vec3(0,0,Math.signum(dz)*Math.min(LED_PACE,Math.abs(dz)))}){
                if(s.lengthSqr()<1e-6)continue;
                tilly.move(MoverType.SELF,s);
                if(!blocked(l,b,tilly.position())&&tilly.position().distanceToSqr(before)>1e-5){moved=true;break;}
                tilly.moveTo(before.x,before.y,before.z,tilly.getYRot(),0);
            }
            tilly.setDeltaMovement(Vec3.ZERO);
            if(onPaint(l,b,stepped(before,dx,dz))&&now-h.lineToldAt>=200){PellHouse.cue(p,PellTexts.TILLY_AT_LINE);h.lineToldAt=now;}
            if(moved)tilly.walkAnimation.update((float)(4*LED_PACE),.4F);
        }
        out(l,b,h,june,tilly,p,here);
    }
    /** Where a step from here toward the hand would put her foot. */
    private static Vec3 stepped(Vec3 at,double dx,double dz){double d=Math.sqrt(dx*dx+dz*dz);return d<1e-6?at:at.add(dx/d*(LED_PACE+.05),0,dz/d*(LED_PACE+.05));}
    /** In the water after the hand, keeping to the shallow end; lifted out where the reader stands at its edge. */
    private static void swim(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor tilly,ServerPlayer p,Vec3 r,Vec3 to){
        double dx=to.x-r.x,dz=to.z-r.z,d=Math.sqrt(dx*dx+dz*dz);
        float yaw=PellPeople.yawTo(tilly,p.position());
        if(!PellSite.inWater(to)&&d<=LIFT){
            // Up out of the water just in front of the hand, if there is room to stand there.
            var spot=new Vec3(to.x-dx/d*.8,0,to.z-dz/d*.8);
            var at=PellVisit.world(b,spot);
            if(!PellSite.inWater(new Vec3(spot.x,-.5,spot.z))&&l.noCollision(tilly,tilly.getDimensions(tilly.getPose()).makeBoundingBox(at))){
                PellSwim.splash(l,PellVisit.world(b,new Vec3(r.x,PellWeek.WATERLINE,r.z)),.7F);
                tilly.setNoGravity(false);tilly.moveTo(at.x,at.y,at.z,yaw,0);tilly.setYHeadRot(yaw);tilly.setYBodyRot(yaw);tilly.setDeltaMovement(Vec3.ZERO);
                return;
            }
        }
        double nx=r.x,nz=r.z;
        if(d>BESIDE){double s=Math.min(WATER_PACE,d-BESIDE);nx=Mth.clamp(r.x+dx/d*s,IN_W,IN_E);nz=Mth.clamp(r.z+dz/d*s,IN_N,IN_S);}
        place(tilly,b,nx,TILLY_Y,nz,yaw);tilly.setNoGravity(true);
        if(Math.abs(nx-r.x)+Math.abs(nz-r.z)>1e-3&&l.getGameTime()%12==0){var at=PellVisit.world(b,new Vec3(nx,PellWeek.WATERLINE,nz));l.playSound(null,at.x,at.y,at.z,SoundEvents.PLAYER_SWIM,SoundSource.NEUTRAL,.3F,1.4F);}
    }
    /** Out: on the line where it is broken, or outside it, her hand still in the reader's. June stops calling. */
    private static void out(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,LiteraryActor tilly,ServerPlayer p,List<ServerPlayer> here){
        if(h.rite!=WATER||!outside(l,b,tilly.position()))return;
        step(h,OUT);h.inHold=false;h.under=false;h.reply=false;
        if(h.juneLeader==null&&!h.juneSat){june.gesture(PellPeople.WADE);june.setXRot(0);}
        PellHouse.cue(p,PellTexts.TILLY_OUT);
        var own=PellHouse.own(p);own.putBoolean("GotTillyOut",true);PellHouse.save(p,own);
        for(var q:here){
            if(q==p||!q.isAlive()||q.isSpectator()||q.isSleeping())continue;
            if(q.distanceTo(tilly)<=PellVisit.HEAR){var o=PellHouse.own(q);if(!o.getBoolean("SawTillyOut")){o.putBoolean("SawTillyOut",true);PellHouse.save(q,o);}}
            if(!h.juneSat&&PellSite.onLot(rel(b,q.position())))PellHouse.cue(q,PellTexts.JUNE_STOPPED);
        }
    }

    // ------------------------------------------------------------------------------------------------ the line under her feet
    private static BlockPos cell(BlockPos b,Vec3 at){return BlockPos.containing(at.x,b.getY()-1,at.z);}
    /** Whether her foot here is on the line where it is unbroken (a cell not scrubbed out, or scrubbed out without its neighbour). */
    static boolean onPaint(ServerLevel l,BlockPos b,Vec3 at){
        var c=cell(b,at);var r=c.subtract(b);
        if(!PellSite.onLine(new BlockPos(r.getX(),0,r.getZ())))return false;
        var s=l.getBlockState(c);
        return !(PellLine.is(s)&&s.getValue(PellLine.STAGE)==PellLine.GAP&&PellScrub.gapAt(l,c));
    }
    /** Where she will not walk: onto the unbroken line, or into the pool. */
    private static boolean blocked(ServerLevel l,BlockPos b,Vec3 at){return onPaint(l,b,at)||PellSite.inWater(new Vec3(at.x-b.getX(),-.5,at.z-b.getZ()));}
    /** Whether a foot here is through the line: on a cell of it where it is broken, or outside it altogether. */
    static boolean outside(ServerLevel l,BlockPos b,Vec3 at){
        var r=cell(b,at).subtract(b);int x=r.getX(),z=r.getZ();
        if(x<PellSite.LINE_W||x>PellSite.LINE_E||z<PellSite.LINE_N||z>PellSite.LINE_S)return true;
        return PellSite.onLine(new BlockPos(x,0,z))&&!onPaint(l,b,at);
    }

    // ------------------------------------------------------------------------------------------------ helpers
    private static Vec3 rel(BlockPos b,Vec3 at){return at.subtract(b.getX(),b.getY(),b.getZ());}
    private static void place(LiteraryActor a,BlockPos b,double x,double y,double z,float yaw){
        a.moveTo(b.getX()+x,b.getY()+y,b.getZ()+z,yaw,a.getXRot());a.setYHeadRot(yaw);a.setYBodyRot(yaw);a.setDeltaMovement(Vec3.ZERO);
    }
    private static void sound(ServerLevel l,BlockPos b,BlockPos at,SoundEvent s,float volume,float pitch){var c=Vec3.atCenterOf(b.offset(at));l.playSound(null,c.x,c.y,c.z,s,SoundSource.BLOCKS,volume,pitch);}
    /** Turned toward something, a little at a time, head and body together. */
    private static void face(LiteraryActor a,Vec3 at){
        float y=Mth.approachDegrees(a.getYRot(),PellPeople.yawTo(a,at),10);a.setYRot(y);a.setYBodyRot(y);a.setYHeadRot(y);
    }
}
