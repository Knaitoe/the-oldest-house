package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.605, The Ones Already Inside, its fifth slice (docs/THE_ONES_ALREADY_INSIDE_PLAN.md, N3: "Daniel through the wall, the bolt,
 * the hatch, the restraints").
 *
 * <ul>
 * <li><b>Where a reader can speak to him.</b> In his room; at the food hatch in his room's west wall, from the passage, when it is
 * open; or at his door from the back passage, close to it and crouched, whether or not the bolt is shot. A hand on him from anywhere
 * else (through a shut door) does nothing.</li>
 * <li><b>The hatch.</b> Shut, a hand opens it. Open, a hand from the passage speaks to him through it; a crouched hand shuts it.</li>
 * <li><b>The door.</b> A hand draws the bolt and opens it, as before; crouched and close, a hand speaks through it instead.</li>
 * <li><b>When Dan's voice carries</b> (Dan speaking aloud in the hall, the back passage or the parlor), Daniel strains in the frame,
 * drives his heel back against it, and shouts the script's line. Not again for a minute.</li>
 * <li><b>The restraints.</b> A hand on the straps is told they are locked. They open with the key, which is slice 8's.</li>
 * </ul>
 */
public final class LodgingDaniel {
    private LodgingDaniel(){}
    /** How close to his door, or to the open hatch, a reader stands to speak through it. */
    public static final double THROUGH=2.6;
    /** Ticks he strains and hammers for; ticks before he will again. */
    public static final int HAMMER=60,AGAIN=1200;
    /** How far the shout and the blows are heard. */
    public static final double HEARD=18;
    public static final String HEARD_HAMMER="HeardHammer0605",THROUGH_HATCH="ThroughHatch0605",THROUGH_DOOR="ThroughDoor0605",FELT_STRAPS="FeltStraps0605";

    static final class State {long hammerUntil=-1,quietUntil=-1;int blows;}
    private static final Map<BlockPos,State> STATES=new HashMap<>();
    static State state(BlockPos b){return STATES.computeIfAbsent(b.immutable(),k->new State());}
    public static void clearAll(){STATES.clear();}
    public static void reset(BlockPos b){STATES.remove(b);}

    /** Where the reader is, in the house's own coordinates. */
    static Vec3 rel(ServerPlayer p,BlockPos b){return p.position().subtract(b.getX(),b.getY(),b.getZ());}
    /** The passage side of the food hatch: west of the wall it is in. */
    static boolean atHatch(ServerLevel l,BlockPos b,Vec3 r){
        var h=LodgingSite.FOOD_HATCH;
        return r.x<h.getX()&&r.distanceTo(new Vec3(h.getX()+.5,h.getY(),h.getZ()+.5))<=THROUGH&&LodgingHouse.hatchOpen(l,b,h);
    }
    /** The back passage side of his door, close to it. */
    static boolean atDoor(Vec3 r){
        var d=LodgingSite.DANIEL_DOOR;
        return LodgingSite.inBackPassage(r)&&r.distanceTo(new Vec3(d.getX()+.5,d.getY()+.5,d.getZ()+.5))<=THROUGH;
    }
    /** Whether this reader can speak to him from where they are. */
    public static boolean reach(ServerPlayer p,BlockPos b){
        var r=rel(p,b);var l=p.serverLevel();
        if(LodgingSite.inDaniels(r))return true;
        if(atHatch(l,b,r))return true;
        return atDoor(r)&&p.isShiftKeyDown();
    }

    /** A hand on the food hatch. True if it was answered here. */
    public static boolean hatch(ServerPlayer p,BlockPos b,BlockPos rel){
        if(!rel.equals(LodgingSite.FOOD_HATCH))return false;var l=p.serverLevel();var r=rel(p,b);
        if(!LodgingHouse.hatchOpen(l,b,rel)){LodgingHouse.hatch(l,b.offset(rel),true);if(r.x<rel.getX())LodgingHouse.cue(p,LodgingTexts.HATCH_LOOK);return true;}
        if(p.isShiftKeyDown()||r.x>=rel.getX()){LodgingHouse.hatch(l,b.offset(rel),false);return true;}
        var own=LodgingHouse.own(p);if(!own.getBoolean(THROUGH_HATCH)){own.putBoolean(THROUGH_HATCH,true);LodgingHouse.save(p,own);}
        LodgingPeople.speakTo(p,LodgingPeople.DANIEL);return true;
    }
    /** A crouched hand on his door from the back passage. True if it was answered here. */
    public static boolean door(ServerPlayer p,BlockPos b){
        if(!p.isShiftKeyDown()||!atDoor(rel(p,b))||LodgingSite.open(p.serverLevel(),b,LodgingSite.DANIEL_DOOR))return false;
        var own=LodgingHouse.own(p);
        if(!own.getBoolean(THROUGH_DOOR)){own.putBoolean(THROUGH_DOOR,true);LodgingHouse.save(p,own);LodgingHouse.cue(p,LodgingTexts.THROUGH_DOOR);}
        LodgingPeople.speakTo(p,LodgingPeople.DANIEL);return true;
    }
    /** A hand on the straps of his frame. */
    public static void straps(ServerPlayer p){
        var own=LodgingHouse.own(p);if(!own.getBoolean(FELT_STRAPS)){own.putBoolean(FELT_STRAPS,true);LodgingHouse.save(p,own);}
        LodgingHouse.cue(p,LodgingTexts.STRAPS);
    }

    /** Dan has said something aloud where he stands. If it carries to Daniel's room, Daniel answers it. */
    public static void danSpoke(ServerLevel l,BlockPos b,LiteraryActor dan){
        var r=dan.position().subtract(b.getX(),b.getY(),b.getZ());
        if(!LodgingSite.inHall(r)&&!LodgingSite.inBackPassage(r)&&!LodgingSite.inParlor(r))return;
        var s=state(b);long now=l.getGameTime();if(now<s.quietUntil)return;
        var daniel=LodgingPeople.find(l,b,LodgingPeople.DANIEL);if(daniel==null)return;
        s.hammerUntil=now+HAMMER;s.quietUntil=now+AGAIN;s.blows=0;
        daniel.gesture(LodgingPeople.STRAIN);
        daniel.say(LodgingPeople.name(LodgingPeople.DANIEL,new net.minecraft.nbt.CompoundTag()),LodgingTexts.DANIEL_SHOUTS);daniel.speaks(LodgingTexts.DANIEL_SHOUTS);
        for(var p:LodgingHouse.readers(l,b)){
            if(p.distanceToSqr(daniel)>HEARD*HEARD)continue;
            var own=LodgingHouse.own(p);if(!own.getBoolean(HEARD_HAMMER)){own.putBoolean(HEARD_HAMMER,true);LodgingHouse.save(p,own);}
        }
    }
    /** Every tick of the house: the blows while he strains, then back to hanging in the frame. */
    public static void tick(ServerLevel l,BlockPos b){
        var s=STATES.get(b);if(s==null||s.hammerUntil<0)return;long now=l.getGameTime();
        var daniel=LodgingPeople.find(l,b,LodgingPeople.DANIEL);if(daniel==null)return;
        if(now>=s.hammerUntil){s.hammerUntil=-1;if(daniel.gesture()==LodgingPeople.STRAIN)daniel.gesture(LodgingPeople.HELD);return;}
        long into=HAMMER-(s.hammerUntil-now);
        // Four blows of the heel against the frame's back, the last two closer together.
        if(into==6||into==18||into==30||into==38){
            s.blows++;var at=b.offset(LodgingSite.DANIEL_FRAME);
            l.playSound(null,at,SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR,SoundSource.NEUTRAL,.45F,.62F+.04F*s.blows);
        }
    }
    /** Whether he is straining now (for a case). */
    public static boolean straining(BlockPos b){var s=STATES.get(b);return s!=null&&s.hammerUntil>=0;}
}
