package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.538, the last night begins (docs/PELL_HOUSE_LAST_NIGHT_0_4_538.md), from the proposal's "3. The last night": "the calendar's
 * circle is today; the pool is full and lit; the line is fresh and wet in places; every knife slot is empty; Callum is on the line at
 * its thinnest point, behind the shed." And from "The last night, in play": "Callum on the line. He stands where it is thinnest and
 * does not move for pushing. What moves him: food. A plate from the fridge, anything, offered to him. He takes it, sits down on the
 * grass inside the line and eats, and the line behind him is free to scrub. When the line breaks: he looks up and says the only
 * thing he says in the whole vignette: 'My name's Callum.'"
 *
 * <ul>
 * <li><b>The third visit</b> is the last night, for a reader who has had the first two. It begins in the evening: June beside her
 * chair with the television about to go on, Tilly in Nessa's room, Callum out on the line. The evening holds until a reader first
 * comes in, and June says it will be an early night. Then the evening goes as it does (the television, Tilly's hair brushed, everyone
 * sent out and the house locked up), except that Callum stays where he is.</li>
 * <li><b>The night</b> goes on, locked up, to midnight, and waits there. June does not go out on her rounds; the line stays as the
 * readers leave it. What happens at midnight comes next.</li>
 * <li><b>Callum on the line</b> stands in the middle of the thin stretch behind the shed, facing the house, his head turned to the
 * nearest reader. He does not move for pushing. While a reader scrubs a cell of the thin stretch he steps onto it and stands there,
 * and nothing comes off.</li>
 * <li><b>Food moves him.</b> A plate from the fridge (one to each reader, on the last night), or anything to eat, held out to him: he
 * takes it, sits down on the grass a step inside the line and eats, and the stretch is free. On the visits before the last he looks
 * at it and does not take it.</li>
 * <li><b>When the line breaks</b> (two cells side by side scrubbed out, anywhere), he looks up and says: "My name's Callum." Once.</li>
 * </ul>
 */
public final class PellLastNight {
    private PellLastNight(){}
    static final int J=PellPeople.JUNE,C=PellPeople.CALLUM;
    /** The hour the last night begins: the evening, a while before bedtime. */
    public static final long DUSK=PellVisit.EVENING-600;
    /** Where Callum stands on the line: the middle of the thin stretch behind the shed. */
    public static final int STRIP_X=6;
    public static final Vec3 ON_LINE=new Vec3(STRIP_X+.5,0,PellSite.LINE_N+.5),SITS=new Vec3(STRIP_X+.5,0,PellSite.LINE_N+1.6);
    /** How long he takes to eat. */
    public static final int EATS=240;
    /** How near a reader comes for Callum to look at them. */
    static final double LOOK=12;
    /** The pool's light: under the water, at the band, along each long side. */
    static final BlockPos[] POOL_LIGHTS={new BlockPos(PellSite.WATER_W,-2,-84),new BlockPos(PellSite.WATER_E,-2,-84),new BlockPos(PellSite.WATER_W,-2,-90),new BlockPos(PellSite.WATER_E,-2,-90)};

    /** Whether this is the last night, at this house now. */
    public static boolean tonight(BlockPos b){return PellPeople.household(b).visit>=PellWeek.LAST;}
    /** For tests: whether Callum has eaten, and whether he has said his name. */
    public static boolean fed(BlockPos b){return PellPeople.household(b).callumFed;}
    public static boolean said(BlockPos b){return PellPeople.household(b).callumSaid;}

    /** The last night as the visit begins: the evening, everybody where it has them, and Callum out on the line. */
    static void begin(ServerLevel l,BlockPos b,PellPeople.Household h){
        h.time=DUSK;h.step=PellVisit.TELLY;h.welcomed=false;
        put(l,b,h,J,PellVisit.RECLINER.beside(),PellVisit.RECLINER.look());
        put(l,b,h,C,ON_LINE,0);
        var c=PellPeople.find(l,b,C);if(c!=null){c.gesture(0);c.setXRot(0);c.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);}
        h.asleep[C]=false;h.callumStrip=true;h.callumFed=false;h.callumSaid=false;h.fedAt=0;h.guardX=STRIP_X;
        // A hand on him gets his lines on the line from the first.
        h.said[C]=0;
    }
    /**
     * For tests: the last night as locking up leaves it, without the evening, a little before midnight. The house locked, June in her
     * room with the door shut, Tilly asleep along Nessa's bed, and Callum out on the line behind the shed.
     */
    public static void lockedUp(ServerLevel l,BlockPos b){
        PellNight.lockedUp(l,b);var h=PellPeople.household(b);
        put(l,b,h,C,ON_LINE,0);h.asleep[C]=false;h.callumStrip=true;h.welcomed=true;
        var c=PellPeople.find(l,b,C);if(c!=null)c.gesture(0);
        h.time=PellNight.TWELVE-90;
    }
    /** The last night undone, for the visit beginning again. */
    static void restart(ServerLevel l,BlockPos b,PellPeople.Household h){
        h.callumStrip=false;h.callumFed=false;h.callumSaid=false;h.fedAt=0;h.guardX=STRIP_X;h.plated.clear();
        var c=PellPeople.find(l,b,C);if(c!=null){c.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);if(c.gesture()==PellPeople.EATING||c.gesture()==PellPeople.SITTING)c.gesture(0);}
    }
    private static void put(ServerLevel l,BlockPos b,PellPeople.Household h,int who,Vec3 r,float look){
        var a=PellPeople.find(l,b,who);if(a==null)return;var at=PellVisit.world(b,r);
        a.moveTo(at.x,at.y,at.z,look,0);a.setYHeadRot(look);a.setYBodyRot(look);a.setDeltaMovement(Vec3.ZERO);
        h.way[who].clear();h.rest[who]=look;h.perch[who]=null;h.perchTo[who]=null;h.sleepAt[who]=null;
    }
    /** The pool lit from under the water, on the last night; dark on the others. Each light is a cell of the water, lit. */
    static void poolLight(ServerLevel l,BlockPos b,boolean on){
        for(var r:POOL_LIGHTS){
            var at=b.offset(r);var s=l.getBlockState(at);
            if(on&&s.is(Blocks.WATER))l.setBlock(at,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,12).setValue(BlockStateProperties.WATERLOGGED,true),3);
            else if(!on&&s.is(Blocks.LIGHT))l.setBlock(at,Blocks.WATER.defaultBlockState(),3);
        }
    }
    /** For tests: whether the pool is lit. */
    public static boolean lit(ServerLevel l,BlockPos b){return l.getBlockState(b.offset(POOL_LIGHTS[0])).is(Blocks.LIGHT);}

    /** The first reader in on the last night: June, from her chair, says it will be an early night, and the evening goes on. */
    static void welcome(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,List<ServerPlayer> here,long now){
        if(!PellVisit.anyoneInside(b,here)||!PellVisit.ready(h,now,june))return;
        PellVisit.say(h,june,J,PellTexts.JUNE_LAST_WELCOME,now);
        PellVisit.far(june,PellTexts.LAST_WELCOME_FAR,b,here);
        h.welcomed=true;
    }

    // ------------------------------------------------------------------------------------------------ Callum on the line
    /** Whether Callum is keeping the thin stretch: the last night, out on the line and not yet fed. */
    static boolean guarding(ServerLevel l,BlockPos b,PellPeople.Household h){return h.visit>=PellWeek.LAST&&h.callumStrip&&!h.callumFed&&PellPeople.find(l,b,C)!=null;}
    /** A reader scrubbing the thin stretch while he keeps it: he steps onto the cell and stands there. Told once. */
    static void stands(ServerLevel l,BlockPos b,PellPeople.Household h,ServerPlayer p,int x){
        if(h.guardX!=x&&h.way[C].isEmpty()){h.guardX=x;PellVisit.go(h,C,-1,0,new Vec3(x+.5,0,PellSite.LINE_N+.5));}
        var own=PellHouse.own(p);if(own.getBoolean("CallumStood"))return;
        own.putBoolean("CallumStood",true);PellHouse.save(p,own);PellHouse.cue(p,PellTexts.CALLUM_STANDS);
    }
    /** Whether a stack is something to eat: anything with food in it, or one of the house's plates of food. */
    static boolean food(ItemStack s){return !s.isEmpty()&&(s.has(DataComponents.FOOD)||s.is(PellRegistry.DINNER.get())||s.is(PellRegistry.BREAKFAST.get()));}
    /**
     * A reader's hand on Callum with something to eat in it. On the last night, out on the line: he takes it, goes a step inside the line,
     * sits down on the grass and eats. Before then he looks at it and does not take it. True if the hand held food.
     */
    static boolean feed(ServerPlayer p,BlockPos b,PellPeople.Household h,LiteraryActor c,CompoundTag own){
        var s=p.getMainHandItem();if(!food(s))return false;
        if(h.visit<PellWeek.LAST||!h.callumStrip){PellHouse.cue(p,PellTexts.CALLUM_WONT_TAKE);return true;}
        if(h.callumFed){PellHouse.cue(p,PellTexts.CALLUM_HAS_EATEN);return true;}
        var bite=s.copyWithCount(1);if(!p.getAbilities().instabuild)s.shrink(1);
        c.setItemInHand(InteractionHand.OFF_HAND,bite);
        // Fed, a hand on him gets his lines while he eats from the first.
        h.callumFed=true;h.fedAt=0;h.said[C]=0;
        PellVisit.go(h,C,-1,0,SITS);
        PellHouse.cue(p,PellTexts.CALLUM_TAKES);own.putBoolean("FedCallum",true);
        return true;
    }
    /** Every tick on the last night: Callum on the line, or sitting inside it eating, or sat there after. */
    static void callum(ServerLevel l,BlockPos b,PellPeople.Household h,List<ServerPlayer> here,long now){
        var c=PellPeople.find(l,b,C);if(c==null||!h.callumStrip||!h.way[C].isEmpty())return;
        h.moving[C]=true;c.setYRot(0);c.setYBodyRot(0);
        if(h.callumFed){
            if(h.fedAt==0){h.fedAt=now;c.gesture(PellPeople.EATING);}
            long ate=now-h.fedAt;
            if(ate<EATS){if(ate%7==0)l.playSound(null,c.getX(),c.getY()+.5,c.getZ(),SoundEvents.GENERIC_EAT,SoundSource.NEUTRAL,.35F,1.3F);}
            else if(c.gesture()==PellPeople.EATING){c.gesture(PellPeople.SITTING);c.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);}
            if(!h.callumSaid)c.setXRot(20);
            c.setYHeadRot(Mth.approachDegrees(c.getYHeadRot(),0,6));
            return;
        }
        var near=nearest(c,here);
        float want=near==null?0:Mth.clamp(Mth.wrapDegrees(PellPeople.yawTo(c,near.getEyePosition())),-PellPeople.HEAD_TURN,PellPeople.HEAD_TURN);
        c.setYHeadRot(Mth.approachDegrees(c.getYHeadRot(),want,6));
    }
    private static @Nullable ServerPlayer nearest(LiteraryActor c,List<ServerPlayer> here){
        ServerPlayer near=null;double best=LOOK*LOOK;
        for(var p:here){if(!p.isAlive()||p.isSpectator()||p.isSleeping())continue;double d=p.distanceToSqr(c);if(d<best){best=d;near=p;}}
        return near;
    }
    /** The line broken, on the last night: Callum looks up and says his name, the only time he speaks. Everybody near hears it. */
    static void broken(ServerLevel l,BlockPos b,PellPeople.Household h){
        if(h.visit<PellWeek.LAST||h.callumSaid)return;
        var c=PellPeople.find(l,b,C);if(c==null)return;
        h.callumSaid=true;c.setXRot(-25);
        PellVisit.say(h,c,C,PellTexts.CALLUM_NAME,l.getGameTime());
        PellVisit.present(c,"HeardCallum",PellHouse.readers(l,b));
    }
}
