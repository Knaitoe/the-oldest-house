package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 0.4.533, a week in (docs/PELL_HOUSE_WEEK_0_4_533.md), from the proposal's "2. A week in": "The pool: the cover is off. June swims
 * lengths at dawn. Tilly: June starts calling her Nessa 'by mistake'." The knife, June's hand, the tapes, the freezer and the journal come
 * after; this is the visit itself, the house as June has made it by then, and the first of the slips.
 *
 * <ul>
 * <li><b>Which visit.</b> Each reader's own record keeps which of the house's visits they have had all of ("FirstVisit", "SecondVisit"):
 * a visit is had once Gail has rung and June has hung up with the reader at the house. When the house begins again it plays the earliest
 * visit that a reader at it has not had, so nobody misses the first; a reader who joins a visit already begun has the house as it is.
 * 0.4.538: a reader who has had both has the third, the last night (PellLastNight); its end comes later, so it is played again.</li>
 * <li><b>A week in.</b> No Gail and no car. June is at her stove, Tilly is in Nessa's room with the door open, and Callum at the end of
 * the hall. The front door and the sliding door are shut, and so is the garage's, with a towel pushed under it from the yard. The late
 * afternoon holds until a reader first comes in; June calls hello from the kitchen ("That you, love? Dinner's at six."), and the hour
 * goes on.</li>
 * <li><b>The house a week on.</b> The pool's cover is off and the water is to the brim; one tile of the waterline band is missing and
 * another does not match; a child's goggles lie under the deep end's drain; a lane rope crosses it at the step between the ends, one of
 * its floats gone. The line round the lot is fresher. One slot of the knife block is empty, and there is a knife in Callum's drawer of
 * cutlery; a second knife's outline on the garage's pegboard is empty. The shed's calendar has more nights crossed off, and the fridge
 * has two new photographs, of the doorstep the day they came.</li>
 * </ul>
 *
 * <p>The slips (PellVisit, PellMorning): at dinner June serves Tilly as Nessa and corrects herself; at bedtime she says goodnight to Nessa
 * and Tilly says who she is; at breakfast June asks Nessa where she put her goggles. At first light June swims (PellSwim). Gail's call is
 * the week's (PellCall). Each reader near enough keeps what they heard. Nothing marks anybody yet.
 */
public final class PellWeek {
    private PellWeek(){}
    /** The latest of the house's visits there is yet: 0.4.538, the third, the last night (PellLastNight). */
    public static final int LAST=3;
    /** The lane rope: the tag on its lengths' bodies, the row it crosses at (the step between the shallow end and the deep), the height of
     *  its floats' middles (the waterline), and the length of it where a float is missing. */
    public static final String LANE_TAG="pell_lane";
    public static final double ROPE_Z=PellSite.DEEP_S+1,WATERLINE=-1+8/9.0;
    public static final int GAP_X=-6;
    /** Where the things that change in a week are. */
    public static final BlockPos KNIFE=new BlockPos(-11,0,-71),CUTLERY=new BlockPos(-6,0,-61),SHED_CALENDAR=new BlockPos(5,1,-92),
        FRIDGE_TOP=new BlockPos(-13,1,-71),TOWEL=new BlockPos(13,0,-87);
    /** The pegboard's two cells, and the pool's details: the band's missing tile, its odd one, and the deep end's drain. */
    public static final List<BlockPos> PEGBOARD=List.of(new BlockPos(14,1,-95),new BlockPos(13,1,-95),new BlockPos(15,1,-95));
    /** 0.4.538: the kitchen's upper cupboards; the knife block is put away in the first on the last night. */
    public static final List<BlockPos> CUPBOARD=List.of(new BlockPos(-11,2,-71),new BlockPos(-13,2,-71),new BlockPos(-12,2,-71),new BlockPos(-8,2,-71),new BlockPos(-7,2,-71));

    /** The visit a reader is on: the first of the house's visits they have not had all of (the last there is, once they have had them all). */
    public static int next(CompoundTag own){return !own.getBoolean("FirstVisit")?1:!own.getBoolean("SecondVisit")?2:LAST;}
    /** For tests: the visit a reader's record has them on. */
    public static int nextFor(CompoundTag own){return next(own);}
    /** The visit the house plays for the readers at it: the earliest any of them is on. */
    public static int visitFor(List<ServerPlayer> here){
        int v=Integer.MAX_VALUE;
        for(var p:here){if(!p.isAlive()||p.isSpectator())continue;v=Math.min(v,next(PellHouse.own(p)));}
        return v==Integer.MAX_VALUE?1:Math.max(1,Math.min(LAST,v));
    }
    /** For tests: the visit the house is playing. */
    public static int visit(BlockPos b){return PellPeople.household(b).visit;}
    /** For tests: whether June has called hello, a week in. */
    public static boolean welcomed(BlockPos b){return PellPeople.household(b).welcomed;}
    /** Where a reader's record keeps what they told Gail, and that they spoke to her, on this visit's call: the first visit's under
     *  "ToldGail" and "SpokeToGail" (0.4.532), a week in under keys of the week's own, so the first's are kept. */
    public static String told(int visit){return visit>=2?"ToldGailWeek":"ToldGail";}
    public static String spoke(int visit){return visit>=2?"SpokeToGailWeek":"SpokeToGail";}
    /** A visit had, in a reader's record: it is theirs, and the House's own count of their visits moves on. */
    static void done(CompoundTag own,int visit){
        own.putBoolean(visit>=2?"SecondVisit":"FirstVisit",true);
        own.putInt("Beat",Math.max(own.getInt("Beat"),own.getInt("Visit")));
    }

    /** A week in, as the visit begins: no Gail and no car, everybody where they are by the afternoon, and the doors as a week leaves them. */
    static void weekIn(ServerLevel l,BlockPos b,PellPeople.Household h){
        var gail=PellPeople.find(l,b,PellPeople.GAIL);if(gail!=null)gail.discard();
        var car=PellVisit.car(l,b);if(car!=null)car.discard();
        h.gailGone=true;h.car=null;
        PellVisit.stand(l,b,h,PellPeople.JUNE,PellVisit.KITCHEN);PellVisit.stand(l,b,h,PellPeople.TILLY,PellVisit.TILLY_ROOM);
        PellVisit.stand(l,b,h,PellPeople.CALLUM,PellPeople.FIRST_VISIT[PellPeople.CALLUM]);
        if(!PellSite.open(l,b,PellSite.NESSA_DOOR))PellHouse.toggle(l,b,PellSite.NESSA_DOOR);
        h.tillyHome=true;h.tillyStep=PellVisit.TILLY_IN.length;h.trail.clear();h.step=PellVisit.SETTLED;h.waypoint=0;h.next=0;h.stuck=0;
    }
    /** The house as the visit has it: the pool, the line and the things a week changes, as they are on the first visit or a week on. */
    static void dress(ServerLevel l,BlockPos b,int visit){
        boolean week=visit>=2,last=visit>=LAST;
        PellSite.pool(l,b,!week);
        // 0.4.538: the last night, the lane rope is out of the water and the pool is lit from under it; the line fresh and wet in
        // places; the knife block gone from the bench and put away in the cupboard, every slot empty; the knife gone from Callum's
        // drawer; the calendar's circle tonight.
        rope(l,b,week&&!last);
        PellLastNight.poolLight(l,b,last);
        PellSite.repaint(l,b,last?PellLine.LAST:week?PellLine.SECOND:PellLine.FIRST);
        stage(l,b,KNIFE,PellThing.Kind.KNIFE_BLOCK,last?2:week?1:0);
        for(var at:CUPBOARD)stage(l,b,at,PellThing.Kind.CUPBOARD_UPPER,last&&at.equals(CUPBOARD.get(0))?1:0);
        stage(l,b,CUTLERY,PellThing.Kind.CUTLERY,last?2:week?1:0);
        stage(l,b,SHED_CALENDAR,PellThing.Kind.SHED_CALENDAR,last?2:week?1:0);
        stage(l,b,FRIDGE_TOP,PellThing.Kind.FRIDGE_TOP,week?1:0);
        // 0.4.571: the chore chart with June's strip on it, or (the last night) with it taken down (PellJobs).
        PellJobs.dress(l,b,visit);
        for(var at:PEGBOARD){var s=l.getBlockState(b.offset(at));if(PellThing.is(s,PellThing.Kind.PEGBOARD))set(l,b.offset(at),s.setValue(PellThing.STAGE,(s.getValue(PellThing.STAGE)&1)|(week?2:0)));}
        var towel=l.getBlockState(b.offset(TOWEL));
        if(week&&towel.isAir())set(l,b.offset(TOWEL),PellThing.of(PellThing.Kind.TOWEL,Direction.NORTH,0));
        else if(!week&&PellThing.is(towel,PellThing.Kind.TOWEL))set(l,b.offset(TOWEL),Blocks.AIR.defaultBlockState());
    }
    private static void stage(ServerLevel l,BlockPos b,BlockPos at,PellThing.Kind kind,int stage){
        var s=l.getBlockState(b.offset(at));if(PellThing.is(s,kind)&&s.getValue(PellThing.STAGE)!=stage)set(l,b.offset(at),s.setValue(PellThing.STAGE,stage));
    }
    private static void set(ServerLevel l,BlockPos at,BlockState s){if(!l.getBlockState(at).equals(s))l.setBlock(at,s,3);}

    /**
     * The lane rope across the pool at the step between its ends: a block's length of it to each body, its floats at the waterline, and at
     * GAP_X the length where a float is missing. Taken away first, so there is only ever one; with {@code on} false, only taken away.
     */
    public static void rope(ServerLevel l,BlockPos b,boolean on){
        for(var d:l.getEntitiesOfClass(Display.ItemDisplay.class,PellPeople.area(b),e->e.getTags().contains(LANE_TAG)))d.discard();
        if(!on)return;
        for(int x=PellSite.WATER_W;x<=PellSite.WATER_E;x++)
            lay(l,b.getX()+x+.5,b.getY()+WATERLINE,b.getZ()+ROPE_Z,new ItemStack(x==GAP_X?PellRegistry.LANE_GAP.get():PellRegistry.LANE.get()));
    }
    /** For tests: the lane rope's lengths at this house. */
    public static List<Display.ItemDisplay> rope(ServerLevel l,BlockPos b){return l.getEntitiesOfClass(Display.ItemDisplay.class,PellPeople.area(b),e->e.getTags().contains(LANE_TAG));}
    /** One length of the rope, as it lies: unturned, at its full size. */
    private static void lay(ServerLevel l,double x,double y,double z,ItemStack item){
        var d=EntityType.ITEM_DISPLAY.create(l);if(d==null)return;
        var t=d.saveWithoutId(new CompoundTag());t.put("item",item.save(l.registryAccess()));t.putString("item_display","fixed");
        var tr=new CompoundTag();tr.put("scale",floats(1,1,1));tr.put("translation",floats(0,0,0));tr.put("left_rotation",floats(0,0,0,1));tr.put("right_rotation",floats(0,0,0,1));
        t.put("transformation",tr);d.load(t);
        d.moveTo(x,y,z,0,0);d.addTag(LANE_TAG);l.addFreshEntity(d);
    }
    private static ListTag floats(float... v){var list=new ListTag();for(float f:v)list.add(FloatTag.valueOf(f));return list;}

    /** A week in, the first reader in: June calls hello from the kitchen, and those too far off to hear her are told. Then the hour goes on. */
    static void welcome(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,List<ServerPlayer> here,long now){
        if(!PellVisit.anyoneInside(b,here)||!PellVisit.ready(h,now,june))return;
        PellVisit.say(h,june,PellPeople.JUNE,PellTexts.JUNE_WELCOME,now);
        PellVisit.far(june,PellTexts.WELCOME_FAR,b,here);
        h.welcomed=true;
    }
    /** What a hand finds on one of the pool's details from the water, or nothing if it is none of them. */
    public static @Nullable String detail(BlockState s){
        if(s.is(PellRegistry.material("pool_drain")))return PellTexts.DRAIN;
        if(s.is(PellRegistry.material("pool_band_missing")))return PellTexts.BAND_MISSING;
        if(s.is(PellRegistry.material("pool_band_odd")))return PellTexts.BAND_ODD;
        return null;
    }
}
