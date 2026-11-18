package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.714, The list, part 4c-1 (docs/THE_LIST_0_4_714.md): the fryer in the Caprock drive-in's stand, against its front wall under the
 * order windows, two vats of oil with a basket hung over each, its knob and pilot on the front. Off at night, the oil cold. A hand on it
 * lights it for anyone who is not a spectator, with or without a hunt; it takes eight seconds to come up to heat (the block's own
 * scheduled tick), and hot it bubbles and smokes. Another hand puts it out. The fryer's own part in the hunt, one of the five forced face
 * down into the hot oil, is TheListHunt's (State.FRYER).
 */
public final class TheListFryer {
    private TheListFryer(){}
    /** Where it stands, in the town's coordinates, and which way its knob faces: into the stand. */
    public static final BlockPos AT=new BlockPos(TheListSite.AVE_E+12,0,-134);
    public static final Direction FACING=Direction.NORTH;
    /** The fitting's FRYER stages: off, heating and hot, with the oil clean; DARK added, the same with the oil gone dark. */
    public static final int OFF=0,WARM=1,HOT=2,DARK=3;
    /** How long it takes the oil to come up to heat, in ticks. */
    public static final int HEAT=160;
    /** How near the fryer one held must be for the slasher to force them into it. */
    public static final double REACH=3;
    /** How long the hot oil takes them, held under, before they go still. */
    public static final int UNDER=60;
    /** How near a reader is told the oil has come up to heat. */
    public static final double TOLD=8;
    /** Where one forced into it stands, bent over it with the face in the west vat, in the town's coordinates (y from the floor). */
    public static final Vec3 STAND=new Vec3(AT.getX()+.28,0,AT.getZ()-.3);
    /** Over each vat, a little above the oil, in the fitting's own frame (x across the front, z back from it). */
    static final double[][] VATS={{.28,.5},{.72,.5}};

    public static int heat(BlockState st){return st.getValue(TheListBlock.STAGE)%DARK;}
    public static boolean dark(BlockState st){return st.getValue(TheListBlock.STAGE)>=DARK;}
    static boolean is(BlockState st){return TheListBlock.is(st,TheListBlock.Kind.FRYER);}
    /** The fitting there now, if its ground is loaded and it is the fryer. */
    public static @org.jetbrains.annotations.Nullable BlockState state(ServerLevel l,BlockPos b){
        var at=b.offset(AT);if(!l.isLoaded(at))return null;var st=l.getBlockState(at);return is(st)?st:null;
    }
    /** Whether the oil is hot now. */
    public static boolean hot(ServerLevel l,BlockPos b){var st=state(l,b);return st!=null&&heat(st)==HOT;}
    /** Whether the fryer is lit, heating or hot. */
    public static boolean lit(ServerLevel l,BlockPos b){var st=state(l,b);return st!=null&&heat(st)!=OFF;}

    // ------------------------------------------------------------------------------------------------ a hand on it
    /**
     * A hand or a blow on the fryer (TheList.click, TheListBody.punched). The slasher with hold of one of the five near it is the hunt's
     * (TheListHunt.fryer): into the oil when it is hot, told it is not hot yet while it heats. Otherwise it is the knob, for anyone who
     * is not a spectator: lit when it is off, put out when it is lit. True when it was either.
     */
    public static boolean hand(ServerPlayer p,BlockPos b,BlockPos rel){
        if(p.isSpectator()||!rel.equals(AT))return false;
        var l=p.serverLevel();var st=l.getBlockState(b.offset(rel));
        if(!is(st))return false;
        if(TheListHunt.fryer(p,b))return true;
        turn(l,b,p,heat(st)==OFF);
        return true;
    }
    /** The knob turned: the burners catch with a soft thump and the oil starts to heat, or they go out. */
    static void turn(ServerLevel l,BlockPos b,ServerPlayer p,boolean on){
        var at=b.offset(AT);var st=l.getBlockState(at);int dark=dark(st)?DARK:0;
        l.setBlock(at,st.setValue(TheListBlock.STAGE,dark+(on?WARM:OFF)),3);
        if(on&&!l.getBlockTicks().hasScheduledTick(at,st.getBlock()))l.scheduleTick(at,st.getBlock(),HEAT);
        l.playSound(null,at.getX()+.5,at.getY()+.3,at.getZ()+.5,TheListRegistry.BURNER.get(),SoundSource.BLOCKS,1F,on?1F:.85F);
        TheList.cue(p,on?TheListTexts.FRYER_ON:TheListTexts.FRYER_OFF);
    }
    /**
     * The block's own scheduled tick (TheListBlock.tick), HEAT ticks after it was lit: the oil is hot, if it is still lit. Anyone near who
     * is not a spectator is told, and the oil is heard.
     */
    public static void cameUp(ServerLevel l,BlockPos at,BlockState st){
        if(!is(st)||heat(st)!=WARM)return;
        l.setBlock(at,st.setValue(TheListBlock.STAGE,st.getValue(TheListBlock.STAGE)+1),3);
        l.playSound(null,at.getX()+.5,at.getY()+1,at.getZ()+.5,TheListRegistry.FRYER.get(),SoundSource.BLOCKS,.35F,1.3F);
        var mid=net.minecraft.world.phys.Vec3.atCenterOf(at);
        for(var p:l.players())if(!p.isSpectator()&&p.position().distanceTo(mid)<=TOLD)TheList.cue(p,TheListTexts.FRYER_HOT);
    }

    // ------------------------------------------------------------------------------------------------ the oil
    /** The town's tick: hot oil bubbles and smokes, and is heard close by now and then. */
    static void tick(ServerLevel l,BlockPos b,List<ServerPlayer> here){
        var st=state(l,b);if(st==null||heat(st)!=HOT)return;
        int tick=(int)l.getGameTime();
        var at=b.offset(AT);
        if(tick%3==0)for(var v:VATS){
            var o=over(at,v);
            l.sendParticles(ParticleTypes.BUBBLE_POP,o.x,o.y,o.z,2,.09,0,.18,0);
            if(tick%9==0)l.sendParticles(ParticleTypes.SMOKE,o.x,o.y+.1,o.z,1,.08,.02,.15,.005);
        }
        if(tick%60==0&&TheListLine.near(here,at,10))l.playSound(null,at.getX()+.5,at.getY()+1,at.getZ()+.5,TheListRegistry.FRYER.get(),SoundSource.BLOCKS,.2F,1.4F);
    }
    /** A point over a vat, just above the oil, in the level. */
    static Vec3 over(BlockPos at,double[] v){return new Vec3(at.getX()+v[0],at.getY()+.95,at.getZ()+v[1]);}
    /** Over the vat one of the five has been forced into. */
    public static Vec3 vat(BlockPos b){return over(b.offset(AT),VATS[0]);}
    /** The oil gone dark: what was in it, kept with the heat it has. */
    static void darken(ServerLevel l,BlockPos b){
        var at=b.offset(AT);var st=l.getBlockState(at);if(!is(st)||dark(st))return;
        l.setBlock(at,st.setValue(TheListBlock.STAGE,st.getValue(TheListBlock.STAGE)+DARK),3);
    }
}
