package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.Arrays;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.530, the Pell house's first night (docs/PELL_HOUSE_NIGHT_0_4_530.md), from the proposal's "1. The placement": "At night: the
 * older sibling sees June out with the tin; Tilly hears singing through the wall; Callum stands on the line in the dark, facing in."
 *
 * <ul>
 * <li><b>June's rounds.</b> Once the house is locked up and June is in her room, the hour goes on, three times as fast, to eleven.
 * Then she comes out with a battery lantern, unlocks the back door, goes out and locks it behind her. She unlocks the shed, takes the
 * tin marked LINE and its brush from the bench, and padlocks the shed again. Then she goes up the passage between the shed and the
 * garage to the strip behind the shed, where the line is thinnest. There she sets the lantern down, with the tin beside her, and
 * kneels. She paints the line again cell by cell, the tin moving along with her, and leaves it wet. Wet paint smears under a foot,
 * and she paints over that too while she is out. Then she takes the tin back to the shed, comes in by the back door, locks it and
 * goes to her room. The hour goes on to midnight, where the night waits.</li>
 * <li><b>Seen from the garage.</b> The garage's window is the only window that looks along that stretch. A reader who sees June out,
 * or painting, keeps it in their own record, and whether they saw it from the garage.</li>
 * <li><b>Callum on the line.</b> Once June is out, at the first moment nobody can see his bed or the place, Callum is standing on the
 * line by the drive, facing in. He stays there.
 *   <ul>
 *   <li>He copies the nearest reader a beat late: he crouches when they crouch.</li>
 *   <li>While they look at him he looks where they look, as a reflection would. When they look away, he looks at them.</li>
 *   </ul></li>
 * </ul>
 *
 * <p>Wherever June has to shut a door behind her, she waits until nobody is on the far side of it or in its way, and asks anyone who
 * is to come out. Nobody is hurt and nothing is forced.
 *
 * <p>Tilly's singing through the wall reaches only a reader playing Tilly, and no reader plays her yet; it waits for that.
 *
 * <p>0.4.531 (PellMorning): a reader asleep on the camp bed is woken at eleven by the back door's lock as June goes out. A reader
 * asleep sees nothing and Callum does not copy them; while every reader at the house is asleep, June's rounds are done at once. The
 * small hours go on to the morning.
 */
public final class PellNight {
    private PellNight(){}
    /** Eleven, when June goes out; midnight, where the night waits. While she is in her room the hour goes on NIGHT_PACE times as fast. */
    public static final long ELEVEN=17000,TWELVE=18000;
    public static final int NIGHT_PACE=3;
    /**
     * After the house is locked up (PellVisit.NIGHT, June in her room): June on her way out; to the shed for the tin; to the line behind
     * it; painting; packing up and taking the tin back; home and in; the small hours, where the night waits for the morning.
     */
    public static final int ROUNDS=PellVisit.NIGHT+1,TO_SHED=ROUNDS+1,TO_LINE=ROUNDS+2,PAINTING=ROUNDS+3,PACKING=ROUNDS+4,HOMEWARD=ROUNDS+5,SMALL_HOURS=ROUNDS+6;
    /** How long June paints a cell of the line, and how often her brush is heard; how many ticks behind a reader Callum is. */
    public static final int STROKES=60,STROKE=15,BEAT=12;
    /** How near a reader comes before June says something to them; how far a reader sees June or Callum from; how near Callum a reader
     *  is for him to copy them. */
    public static final double NOTICE=5,SEE=40,COPY=12;

    static Vec3 v(double x,double z){return new Vec3(x,0,z);}
    /** June's way from her room to inside the back door; the back step outside it; outside the shed's door, and in front of the bench. */
    static final Vec3[] ROOM_TO_BACK={v(8.5,-63.4),v(8.5,-65.6),v(7.5,-65.6),v(7.5,-66.6),v(7.5,-68.4),v(7.5,-69.5),v(8.5,-70.5)},
        BACK_TO_ROOM={v(7.5,-69.5),v(7.5,-68.4),v(7.5,-66.6),v(7.5,-65.6),v(8.5,-65.6),v(8.5,-63.4),v(PellVisit.HER_ROOM.x(),PellVisit.HER_ROOM.z())};
    static final Vec3 BACK_IN=v(8.5,-70.5),BACK_STEP=v(8.5,-73.6),SHED_STEP=v(6.5,-88.6),BENCH=v(7.5,-91.6),STRIP_END=v(10.5,-95.4);
    static final Vec3[] STEP_TO_SHED={v(8.5,-76.5),v(6.5,-86.5),SHED_STEP},SHED_TO_STEP={v(6.5,-86.5),v(8.5,-76.5),BACK_STEP},
        INTO_SHED={v(6.5,-90.4),BENCH},OUT_OF_SHED={v(6.5,-90.4),SHED_STEP},
        TO_STRIP={v(10.5,-88.6),v(10.5,-94.6),STRIP_END},FROM_STRIP={v(10.5,-94.6),v(10.5,-88.6),SHED_STEP};
    /** Where she sets the lantern down, at the strip's end by the passage; where the tin stands on the bench. */
    public static final BlockPos LANTERN_AT=new BlockPos(11,0,-96),BENCH_TIN=new BlockPos(8,1,-93);
    /** The thin cells she paints, from the passage end west; where she kneels for each (inside the line, facing it); where the tin
     *  stands while she paints it (beside her, toward the passage). */
    public static final int[] CELLS={9,8,7,6,5,4};
    public static Vec3 kneel(int x){return v(x+.5,-95.72);}
    public static BlockPos tinBeside(int x){return new BlockPos(x+1,0,-96);}
    /** Callum's place on the line: the east run by the drive, short of the garage, facing in. */
    public static final PellPeople.Spot CALLUM_LINE=new PellPeople.Spot(19.5,-84.5,90);
    private static final int NONE=Integer.MIN_VALUE;

    /** Whether June is up and about tonight, from leaving her room until she is back in it. */
    public static boolean up(BlockPos b){var h=PellPeople.household(b);return h.step>=ROUNDS&&h.step<SMALL_HOURS;}
    /** Whether Callum is out on the line. */
    public static boolean callumOut(BlockPos b){return PellPeople.household(b).callumOut;}
    /** The stage of the thin line at one of its cells (x along the strip). */
    public static int stage(ServerLevel l,BlockPos b,int x){var s=l.getBlockState(b.offset(x,-1,PellSite.LINE_N));return PellLine.is(s)?s.getValue(PellLine.STAGE):-1;}

    /**
     * For tests: the night as locking up leaves it, without the evening. The day settled and dinner done, June in her room with its door
     * shut, Tilly asleep along Nessa's bed, Callum asleep in his circle, the three outside doors shut and locked, and half past eight.
     */
    public static void lockedUp(ServerLevel l,BlockPos b){
        PellVisit.dined(l,b);var h=PellPeople.household(b);
        PellVisit.stand(l,b,h,PellPeople.JUNE,PellVisit.HER_ROOM);
        var june=PellPeople.find(l,b,PellPeople.JUNE);if(june!=null)june.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        var tilly=PellPeople.find(l,b,PellPeople.TILLY);if(tilly!=null)PellVisit.lie(l,b,h,PellPeople.TILLY,tilly,PellVisit.TILLY_SLEEPS);
        var callum=PellPeople.find(l,b,PellPeople.CALLUM);if(callum!=null)PellVisit.lie(l,b,h,PellPeople.CALLUM,callum,PellVisit.CALLUM_SLEEPS);
        for(var door:PellVisit.LOCKS){if(PellSite.open(l,b,door))PellHouse.toggle(l,b,door);h.locked.add(door);}
        if(PellSite.open(l,b,PellSite.JUNE_DOOR))PellHouse.toggle(l,b,PellSite.JUNE_DOOR);
        h.step=PellVisit.NIGHT;h.waypoint=0;h.time=PellVisit.EVENING;h.next=0;
    }

    /** The night undone, for the visit beginning again: the thin stretch as the first visit has it, the tin back on the bench, nothing
     *  left on the strip, June's other hand empty, Callum standing as he does by day. */
    static void restart(ServerLevel l,BlockPos b,PellPeople.Household h){
        for(int x:CELLS)paint(l,b,x,PellLine.FIRST);
        clearStrip(l,b);
        var bench=b.offset(BENCH_TIN);if(!PellThing.is(l.getBlockState(bench),PellThing.Kind.TIN))l.setBlock(bench,PellThing.of(PellThing.Kind.TIN,Direction.SOUTH,0),3);
        var june=PellPeople.find(l,b,PellPeople.JUNE);if(june!=null)june.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
        var callum=PellPeople.find(l,b,PellPeople.CALLUM);if(callum!=null)callum.setXRot(0);
        h.cell=0;h.paintedAt=0;h.nightSaid=0;h.feetSaid=-1;h.noticed=false;h.smearSaid=false;h.smearDue=false;h.callumOut=false;h.copied=0;h.copyOf=null;
    }

    /** Every tick for the house from the moment it is locked up (PellVisit.evening hands it over). */
    static void tick(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,List<ServerPlayer> here,long now){
        int J=PellPeople.JUNE;boolean still=h.way[J].isEmpty();
        switch(h.step-PellVisit.NIGHT){
            case 0->{
                // In her room: the door shut behind her. At eleven she gets up, the lantern in her other hand, and goes to the back door.
                if(still&&PellVisit.near(june,b,v(PellVisit.HER_ROOM.x(),PellVisit.HER_ROOM.z()),.6)&&PellSite.open(l,b,PellSite.JUNE_DOOR))PellHouse.toggle(l,b,PellSite.JUNE_DOOR);
                if(h.time<ELEVEN||!still)break;
                june.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(PellRegistry.LANTERN.get()));
                next(h,ROUNDS);PellVisit.go(h,J,-1,180,ROOM_TO_BACK);
            }
            case 1->{
                if(!still)break;
                if(h.waypoint==0){
                    // Inside the back door: the lock turned back and the door opened, and out onto the step. 0.4.531: it wakes anyone
                    // asleep in the garage.
                    PellVisit.unlock(b,PellSite.BACK_DOOR);lock(l,b,PellSite.BACK_DOOR);
                    PellMorning.wakeAll(l,b,h,here,PellTexts.WOKEN_BY_LOCK,"WokenByTheLock");
                    if(!PellSite.open(l,b,PellSite.BACK_DOOR))PellHouse.toggle(l,b,PellSite.BACK_DOOR);
                    PellVisit.go(h,J,-1,0,v(8.5,-72.4),BACK_STEP);h.waypoint=1;break;
                }
                // On the step, turned back to the door: shut and locked behind her once nobody is in the house or the doorway.
                h.rest[J]=0;
                if(!shut(l,b,h,june,PellSite.BACK_DOOR,here,now,PellTexts.JUNE_OUT_YOU_COME))break;
                next(h,TO_SHED);PellVisit.go(h,J,-1,180,STEP_TO_SHED);
            }
            case 2->{
                if(!still)break;
                switch(h.waypoint){
                    case 0->{
                        // At the shed: the padlock off, the door open, in to the bench.
                        lock(l,b,PellSite.SHED_DOOR);if(!PellSite.open(l,b,PellSite.SHED_DOOR))PellHouse.toggle(l,b,PellSite.SHED_DOOR);
                        PellVisit.go(h,J,-1,180,INTO_SHED);h.waypoint=1;
                    }
                    case 1->{
                        // The tin and its brush off the bench, and out again; 0.4.536, the deck turned off if a tape is playing.
                        PellTapes.juneStops(l,b,h,here);
                        var at=b.offset(BENCH_TIN);if(PellThing.is(l.getBlockState(at),PellThing.Kind.TIN))l.setBlock(at,Blocks.AIR.defaultBlockState(),3);
                        june.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(PellRegistry.PAINT_TIN.get()));
                        l.playSound(null,at,SoundEvents.CHAIN_HIT,SoundSource.BLOCKS,.35F,1.4F);
                        PellVisit.go(h,J,-1,0,OUT_OF_SHED);h.waypoint=2;
                    }
                    default->{
                        // Out, turned back to it: the shed shut and padlocked once nobody is in it; up the passage to the strip.
                        h.rest[J]=0;
                        if(!shut(l,b,h,june,PellSite.SHED_DOOR,here,now,PellTexts.JUNE_OUT_OF_SHED))break;
                        next(h,TO_LINE);PellVisit.go(h,J,-1,180,TO_STRIP);
                    }
                }
            }
            case 3->{
                if(!still)break;
                if(h.waypoint==0){
                    // At the strip's end: the lantern set down by the passage.
                    if(!put(l,b,LANTERN_AT,PellThing.of(PellThing.Kind.LANTERN,Direction.SOUTH,0),here))break;
                    june.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
                    PellVisit.go(h,J,-1,180,kneel(CELLS[0]));h.waypoint=1;break;
                }
                // At the first cell: the tin set down beside her and opened, and the brush in her hand.
                tin(l,b,CELLS[0],here);sound(l,b,tinBeside(CELLS[0]),PellRegistry.TIN.get(),1F);
                june.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(PellRegistry.PAINTBRUSH.get()));
                next(h,PAINTING);h.cell=0;h.paintedAt=0;
            }
            case 4->painting(l,b,h,june,here,now);
            case 5->{
                if(!still)break;
                switch(h.waypoint){
                    case 0->{
                        // Up off her knees, the tin's lid tapped back on and the tin taken up; to the lantern.
                        june.gesture(0);clearTins(l,b);sound(l,b,BlockPos.containing(june.position().subtract(b.getX(),b.getY(),b.getZ())),PellRegistry.TIN.get(),.8F);
                        june.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(PellRegistry.PAINT_TIN.get()));june.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
                        PellVisit.go(h,J,-1,90,STRIP_END);h.waypoint=1;
                    }
                    case 1->{
                        // The lantern taken up, and back down the passage to the shed.
                        var at=b.offset(LANTERN_AT);if(PellThing.is(l.getBlockState(at),PellThing.Kind.LANTERN))l.setBlock(at,Blocks.AIR.defaultBlockState(),3);
                        june.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(PellRegistry.LANTERN.get()));
                        PellVisit.go(h,J,-1,180,FROM_STRIP);h.waypoint=2;
                    }
                    case 2->{
                        lock(l,b,PellSite.SHED_DOOR);if(!PellSite.open(l,b,PellSite.SHED_DOOR))PellHouse.toggle(l,b,PellSite.SHED_DOOR);
                        PellVisit.go(h,J,-1,180,INTO_SHED);h.waypoint=3;
                    }
                    case 3->{
                        // The tin back on the bench, where it always stands.
                        l.setBlock(b.offset(BENCH_TIN),PellThing.of(PellThing.Kind.TIN,Direction.SOUTH,0),3);
                        l.playSound(null,b.offset(BENCH_TIN),SoundEvents.CHAIN_PLACE,SoundSource.BLOCKS,.35F,1.4F);
                        june.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
                        PellVisit.go(h,J,-1,0,OUT_OF_SHED);h.waypoint=4;
                    }
                    default->{
                        h.rest[J]=0;
                        if(!shut(l,b,h,june,PellSite.SHED_DOOR,here,now,PellTexts.JUNE_OUT_OF_SHED))break;
                        next(h,HOMEWARD);PellVisit.go(h,J,-1,180,SHED_TO_STEP);
                    }
                }
            }
            case 6->{
                if(!still)break;
                switch(h.waypoint){
                    case 0->{
                        // At the back step: unlocked, opened, and in.
                        PellVisit.unlock(b,PellSite.BACK_DOOR);lock(l,b,PellSite.BACK_DOOR);
                        if(!PellSite.open(l,b,PellSite.BACK_DOOR))PellHouse.toggle(l,b,PellSite.BACK_DOOR);
                        PellVisit.go(h,J,-1,180,v(8.5,-72.4),BACK_IN);h.waypoint=1;
                    }
                    case 1->{
                        // Inside, turned to it: shut and locked once nobody else has come in after her; then to her room.
                        h.rest[J]=180;
                        if(!shut(l,b,h,june,PellSite.BACK_DOOR,here,now,PellTexts.JUNE_BACK_OUT))break;
                        june.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
                        PellVisit.go(h,J,-1,PellVisit.HER_ROOM.look(),BACK_TO_ROOM);h.waypoint=2;
                    }
                    default->{
                        if(PellSite.open(l,b,PellSite.JUNE_DOOR))PellHouse.toggle(l,b,PellSite.JUNE_DOOR);
                        next(h,SMALL_HOURS);
                    }
                }
            }
            // 0.4.531: the small hours, first light, and the morning.
            default->PellMorning.tick(l,b,h,june,here,now);
        }
        callum(l,b,h,here);
        smears(l,b,h,june,here,now);
        if(h.step>=ROUNDS&&h.step<SMALL_HOURS)speak(l,b,h,june,here,now);
        seen(l,b,h,june,here,now);
    }
    private static void next(PellPeople.Household h,int step){h.step=step;h.waypoint=0;h.nudgedAt=0;}

    /**
     * At the line: each cell in turn from the passage end, then any she has painted that a foot has smeared since. She walks to where
     * she kneels for it, with the tin moved along beside her; waits, and says so once, while a reader stands on it; kneels and paints,
     * the brush heard as she goes; and leaves it wet. When there is nothing left to paint, she packs up.
     */
    private static void painting(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,List<ServerPlayer> here,long now){
        int J=PellPeople.JUNE;
        if(!h.way[J].isEmpty())return;
        int x=h.cell<CELLS.length?CELLS[h.cell]:smeared(l,b,june);
        if(x==NONE){next(h,PACKING);return;}
        if(!PellVisit.near(june,b,kneel(x),.35)){
            // Up, the tin taken up in her other hand, and along to the next.
            june.gesture(0);h.paintedAt=0;clearTins(l,b);june.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(PellRegistry.PAINT_TIN.get()));
            PellVisit.go(h,J,-1,180,kneel(x));return;
        }
        // There: the tin set down beside her.
        if(june.getOffhandItem().is(PellRegistry.PAINT_TIN.get())){june.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);tin(l,b,x,here);}
        h.rest[J]=180;h.moving[J]=true;
        if(standingOn(b,x,here)){
            june.gesture(0);h.paintedAt=0;
            if(h.feetSaid!=x&&PellVisit.ready(h,now,june)){PellVisit.say(h,june,J,PellTexts.JUNE_FEET,now);h.feetSaid=x;}
            return;
        }
        if(h.paintedAt==0){h.paintedAt=now;june.gesture(PellPeople.PAINT);}
        long t=now-h.paintedAt;
        if(t%STROKE==0){var at=b.offset(x,0,PellSite.LINE_N);l.playSound(null,at.getX()+.5,at.getY()+.1,at.getZ()+.5,PellRegistry.PAINT.get(),SoundSource.NEUTRAL,.8F,.9F+(float)((now/STROKE)%3)*.08F);}
        // 0.4.531: with every reader asleep, done at once.
        if(t<STROKES&&!h.hurried)return;
        paint(l,b,x,PellLine.LAST);
        h.paintedAt=0;h.feetSaid=-1;
        if(h.cell<CELLS.length)h.cell++;
    }
    /** The smeared cell of the thin stretch nearest June, or none. */
    private static int smeared(ServerLevel l,BlockPos b,LiteraryActor june){
        int best=NONE;double bestD=Double.MAX_VALUE;
        for(int x:CELLS){if(stage(l,b,x)!=PellLine.FOOTPRINT)continue;double d=Math.abs(june.getX()-(b.getX()+x+.5));if(d<bestD){bestD=d;best=x;}}
        return best;
    }
    /** A reader is standing on this cell of the thin stretch, or where June kneels for it. */
    private static boolean standingOn(BlockPos b,int x,List<ServerPlayer> here){
        var cell=new AABB(b.getX()+x,b.getY(),b.getZ()+PellSite.LINE_N,b.getX()+x+1,b.getY()+2,b.getZ()+PellSite.LINE_N+2);
        for(var p:here)if(p.isAlive()&&!p.isSpectator()&&p.getBoundingBox().intersects(cell))return true;
        return false;
    }
    /** A cell of the thin stretch, painted to this stage (its shape, surface and facing kept). */
    static void paint(ServerLevel l,BlockPos b,int x,int stage){
        var at=b.offset(x,-1,PellSite.LINE_N);var s=l.getBlockState(at);
        if(PellLine.is(s)&&s.getValue(PellLine.STAGE)!=stage)l.setBlock(at,s.setValue(PellLine.STAGE,stage),3);
    }
    /** The tin, open, beside where she kneels for this cell; from wherever it stood before. It is not put where a reader stands. */
    private static void tin(ServerLevel l,BlockPos b,int x,List<ServerPlayer> here){
        var to=tinBeside(x);
        if(PellThing.is(l.getBlockState(b.offset(to)),PellThing.Kind.TIN))return;
        clearTins(l,b);put(l,b,to,PellThing.of(PellThing.Kind.TIN,Direction.SOUTH,1),here);
    }
    /** The open tin wherever it stands on the strip, taken up. */
    private static void clearTins(ServerLevel l,BlockPos b){
        for(int x=PellSite.SHED_W-1;x<PellSite.GARAGE_W;x++)for(int z=PellSite.LOT_N+1;z<PellSite.SHED_N;z++){
            var at=b.offset(x,0,z);if(PellThing.is(l.getBlockState(at),PellThing.Kind.TIN))l.setBlock(at,Blocks.AIR.defaultBlockState(),3);
        }
    }
    /** Anything of the night's left on the strip, taken up. */
    private static void clearStrip(ServerLevel l,BlockPos b){
        clearTins(l,b);
        var at=b.offset(LANTERN_AT);if(PellThing.is(l.getBlockState(at),PellThing.Kind.LANTERN))l.setBlock(at,Blocks.AIR.defaultBlockState(),3);
    }
    /** One of her things set down in an empty cell nobody stands in. True if it is there. */
    private static boolean put(ServerLevel l,BlockPos b,BlockPos rel,net.minecraft.world.level.block.state.BlockState state,List<ServerPlayer> here){
        var at=b.offset(rel);var now=l.getBlockState(at);
        if(now.equals(state))return true;
        if(!now.isAir())return false;
        var cell=new AABB(at);for(var p:here)if(p.isAlive()&&!p.isSpectator()&&p.getBoundingBox().intersects(cell))return false;
        l.setBlock(at,state,3);return true;
    }
    private static void sound(ServerLevel l,BlockPos b,BlockPos rel,net.minecraft.sounds.SoundEvent sound,float pitch){var at=b.offset(rel);l.playSound(null,at.getX()+.5,at.getY()+.3,at.getZ()+.5,sound,SoundSource.NEUTRAL,.7F,pitch);}
    /** A key in the back door's lock, or the shed's padlock. */
    private static void lock(ServerLevel l,BlockPos b,BlockPos door){var at=Vec3.atBottomCenterOf(b.offset(door)).add(0,1,0);l.playSound(null,at.x,at.y,at.z,PellRegistry.LOCK.get(),SoundSource.BLOCKS,.8F,door.equals(PellSite.SHED_DOOR)?.8F:1F);}
    /**
     * A door shut behind June: once nobody is on its far side (in the house, for the back door; in the shed, for the shed's) or in its
     * way, asking anyone who is to come out; then shut and locked. True once it is.
     */
    static boolean shut(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,BlockPos door,List<ServerPlayer> here,long now,String ask){
        boolean shed=door.equals(PellSite.SHED_DOOR),beyond=false;
        for(var p:here){
            if(!p.isAlive()||p.isSpectator())continue;var r=p.position().subtract(b.getX(),b.getY(),b.getZ());
            if(shed?PellSite.inShed(r):PellSite.inHouse(r))beyond=true;
        }
        if(beyond){
            if(now-h.nudgedAt>=PellVisit.NUDGE&&PellVisit.ready(h,now,june)){PellVisit.say(h,june,PellPeople.JUNE,ask,now);h.nudgedAt=now;}
            return false;
        }
        if(!PellVisit.clearOf(b,door,here))return false;
        if(PellSite.open(l,b,door))PellHouse.toggle(l,b,door);
        if(!shed)h.locked.add(door);
        lock(l,b,door);
        return true;
    }

    /** Whether June is out of the house tonight, with the tin. */
    public static boolean juneOut(ServerLevel l,BlockPos b){
        if(!up(b))return false;var june=PellPeople.find(l,b,PellPeople.JUNE);
        return june!=null&&!PellSite.inHouse(june.position().subtract(b.getX(),b.getY(),b.getZ()));
    }
    /** What June says out at the line when there is a moment for it: about the paint, once a foot has been in it; or, once a night, to
     *  the first reader who comes near her. */
    private static void speak(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,List<ServerPlayer> here,long now){
        if(!PellVisit.ready(h,now,june))return;
        if(h.smearDue){h.smearDue=false;if(h.step<=PACKING){PellVisit.say(h,june,PellPeople.JUNE,PellTexts.JUNE_MIND_PAINT,now);return;}}
        if(h.noticed||PellSite.inHouse(june.position().subtract(b.getX(),b.getY(),b.getZ())))return;
        for(var p:here){
            if(!p.isAlive()||p.isSpectator()||p.distanceToSqr(june)>NOTICE*NOTICE)continue;
            PellVisit.say(h,june,PellPeople.JUNE,PellTexts.JUNE_NOTICES,now);h.noticed=true;return;
        }
    }

    /**
     * Callum. Once June is out, the first moment nobody can see where he is or the place on the line, he is standing there, facing in.
     * Then each tick he is the nearest reader's copy, a beat behind: crouched if they were, his head turned as a reflection's would be
     * while they were looking at him, and turned to them when they were not.
     */
    private static void callum(ServerLevel l,BlockPos b,PellPeople.Household h,List<ServerPlayer> here){
        int C=PellPeople.CALLUM;var c=PellPeople.find(l,b,C);if(c==null)return;
        float body=CALLUM_LINE.look();
        // 0.4.534: a week in, from one o'clock while he has the knife, and once it is over, the knife has him (PellKnife).
        if(!h.callumOut&&h.visit>=2&&h.knife>=PellKnife.AT_SINK&&h.knife<=PellKnife.OVER)return;
        if(!h.callumOut){
            // 0.4.531: once it is light he does not go out again.
            if(h.step<TO_SHED||h.dawned)return;
            var spot=PellPeople.world(b,CALLUM_LINE);
            if(seen(l,c.getEyePosition())||seen(l,spot.add(0,c.getEyeHeight(),0)))return;
            for(var p:here)if(p.position().distanceToSqr(spot)<1.5*1.5)return;
            c.moveTo(spot.x,spot.y,spot.z,body,0);c.setYHeadRot(body);c.setYBodyRot(body);c.setDeltaMovement(Vec3.ZERO);
            c.gesture(0);h.asleep[C]=false;h.rest[C]=body;h.callumOut=true;h.copied=0;h.copyOf=null;
            return;
        }
        h.moving[C]=true;c.setYRot(body);c.setYBodyRot(body);
        ServerPlayer near=null;double best=COPY*COPY;
        for(var p:here){if(!p.isAlive()||p.isSpectator()||p.isSleeping())continue;double d=p.distanceToSqr(c);if(d<best){best=d;near=p;}}
        if(near==null){
            h.copied=0;h.copyOf=null;c.gesture(0);c.setXRot(0);c.setYHeadRot(Mth.approachDegrees(c.getYHeadRot(),body,6));return;
        }
        if(!near.getUUID().equals(h.copyOf)){h.copyOf=near.getUUID();h.copied=0;}
        // What the reader is doing now: how far their look is turned off him, how far up or down, whether they crouch.
        int n=BEAT+1,i=h.copied%n;
        h.copyYaw[i]=Mth.wrapDegrees(near.getYRot()-PellPeople.yawTo(near,c.getEyePosition()));h.copyPitch[i]=near.getXRot();h.copyCrouch[i]=near.isCrouching();
        h.copied++;
        if(h.copied<=BEAT)return;
        // What they were doing a beat ago.
        int j=h.copied%n;float off=h.copyYaw[j];
        float toReader=PellPeople.yawTo(c,near.getEyePosition());
        float want=Math.abs(off)<=90?toReader-off:toReader;
        c.setYHeadRot(body+Mth.clamp(Mth.wrapDegrees(want-body),-PellPeople.HEAD_TURN,PellPeople.HEAD_TURN));
        c.setXRot(Mth.clamp(h.copyPitch[j],-60,60));
        c.gesture(h.copyCrouch[j]?PellPeople.CROUCH:0);
    }

    /** Wet paint on the thin stretch smears under a reader's foot. They keep that they did; June, still out, says so once. */
    private static void smears(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,List<ServerPlayer> here,long now){
        if(h.step<PAINTING)return;
        for(var p:here){
            if(!p.isAlive()||p.isSpectator()||Math.abs(p.getY()-b.getY())>.5)continue;
            var r=BlockPos.containing(p.getX()-b.getX(),0,p.getZ()-b.getZ());
            if(!PellSite.thin(r))continue;
            var at=b.offset(r.getX(),-1,r.getZ());var s=l.getBlockState(at);
            if(!PellLine.is(s)||s.getValue(PellLine.STAGE)!=PellLine.LAST)continue;
            l.setBlock(at,s.setValue(PellLine.STAGE,PellLine.FOOTPRINT),3);
            l.playSound(null,at.getX()+.5,at.getY()+1,at.getZ()+.5,SoundEvents.HONEY_BLOCK_STEP,SoundSource.PLAYERS,.4F,1.3F);
            var own=PellHouse.own(p);if(!own.getBoolean("Smeared")){own.putBoolean("Smeared",true);PellHouse.save(p,own);}
            // June, still out and near enough to hear it, says so when she next can (speak); once a night.
            if(h.step<=PACKING&&!h.smearSaid&&p.distanceToSqr(june)<PellVisit.HEAR*PellVisit.HEAR){h.smearDue=true;h.smearSaid=true;}
        }
    }

    /**
     * What each reader sees of the night, kept in their own record: June out of the house ("SawJuneOut"), June painting the line
     * ("SawPainting"), either of those from inside the garage ("SawFromGarage"), and Callum on the line ("SawCallumOnLine").
     */
    private static void seen(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,List<ServerPlayer> here,long now){
        if(now%5!=0)return;
        boolean out=h.step>=ROUNDS&&h.step<SMALL_HOURS&&!PellSite.inHouse(june.position().subtract(b.getX(),b.getY(),b.getZ()));
        var callum=h.callumOut?PellPeople.find(l,b,PellPeople.CALLUM):null;
        if(!out&&callum==null)return;
        for(var p:here){
            // 0.4.531: asleep, a reader sees nothing.
            if(!p.isAlive()||p.isSpectator()||p.isSleeping())continue;
            var own=PellHouse.own(p);boolean changed=false;
            if(out&&sees(p,june.getEyePosition(),SEE)){
                changed|=mark(own,"SawJuneOut");
                if(june.gesture()==PellPeople.PAINT)changed|=mark(own,"SawPainting");
                if(PellSite.inGarage(p.position().subtract(b.getX(),b.getY(),b.getZ())))changed|=mark(own,"SawFromGarage");
            }
            if(callum!=null&&sees(p,callum.getEyePosition(),SEE))changed|=mark(own,"SawCallumOnLine");
            if(changed)PellHouse.save(p,own);
        }
    }
    private static boolean mark(net.minecraft.nbt.CompoundTag own,String key){if(own.getBoolean(key))return false;own.putBoolean(key,true);return true;}
    /**
     * Whether a reader sees a point: near enough, within sixty degrees of where they look, and nothing in the way but clear glass (and,
     * 0.4.533, the pool fence's bars). The garage's window is how the strip is seen from inside it.
     */
    public static boolean sees(ServerPlayer p,Vec3 at,double reach){return sees(p,at,reach,.5);}
    /** As sees(), within this cone (the dot of the look and the way to the point). */
    static boolean sees(ServerPlayer p,Vec3 at,double reach,double cone){
        var eye=p.getEyePosition();var d=at.subtract(eye);double dist=d.length();
        if(dist>reach)return false;if(dist<1.5)return true;
        var dir=d.scale(1/dist);if(p.getLookAngle().dot(dir)<cone)return false;
        var target=BlockPos.containing(at);var from=eye;
        for(int i=0;i<4;i++){
            var hit=p.level().clip(new ClipContext(from,at,ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,p));
            if(hit.getType()==HitResult.Type.MISS||hit.getBlockPos().distManhattan(target)<=1)return true;
            var s=p.level().getBlockState(hit.getBlockPos());
            // Clear glass is seen through; 0.4.533, and so are the pool fence's bars and its gate's.
            boolean clear=PellBlock.is(s,PellBlock.Kind.WINDOW)&&s.getValue(PellBlock.STAGE)==0||PellBlock.is(s,PellBlock.Kind.POOL_FENCE)||PellBlock.is(s,PellBlock.Kind.POOL_GATE);
            if(!clear)return false;
            from=hit.getLocation().add(dir.scale(.6));
        }
        return false;
    }
    /**
     * 0.4.534: whether anybody awake (a spectator too) could see this point, as the House's watching has it (within 48 blocks and
     * the wide cone) and through the house's clear glass and the pool fence as well. What the house does "the first moment nobody
     * sees" waits for this: a reader at the kitchen's window sees the sink.
     */
    public static boolean seen(ServerLevel l,Vec3 at){
        for(var p:l.players())if(p.isAlive()&&!p.isSleeping()&&sees(p,at,48,.26))return true;
        return false;
    }
    /** The thin cells, for tests. */
    public static List<Integer> cells(){return Arrays.stream(CELLS).boxed().toList();}
}
