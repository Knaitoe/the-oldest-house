package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.172 (owner: "The rope swing should be interactive. Players should be able to swing."): the porch's swing seats a reader
 * and swings them. The seat hangs from the crossbar on its two ropes; while someone sits, the still seat and ropes are hidden and
 * the seat entity carries the reader along the arc, building up to a gentle swing, and the renderer draws the seat and ropes
 * moving with them. Getting off puts the still swing back. Every position comes from the time sat down, so client and server
 * agree without sending it.
 */
public final class HouseReadSwing {
    private HouseReadSwing(){}
    /** Ropes three blocks long above the seat to the crossbar's underside, the seat's top half a block up. */
    public static final double PIVOT=4.0,SEAT=.5,LENGTH=PIVOT-SEAT;
    /** Radians a tick of the swing's sway, the widest arc, and how quickly a sitter builds up to it. */
    public static final double RATE=.12,REACH=.5,BUILD=.004;
    public static boolean is(BlockState s){return s.is(HouseReadRegistry.FITTING.get())&&s.getValue(HouseReadPropBlock.PART)==HouseReadPropBlock.Part.SWING;}
    /** The seat's two halves: this one and its neighbour along the crossbar, west first. */
    public static List<BlockPos> pair(Level l,BlockPos at){
        var out=new ArrayList<BlockPos>();
        if(is(l.getBlockState(at.west()))){out.add(at.west());out.add(at);}else{out.add(at);if(is(l.getBlockState(at.east())))out.add(at.east());}
        return out;
    }
    /** Where the swing hangs from, at the middle of the seat under the crossbar. */
    public static Vec3 pivot(List<BlockPos> pair){var first=pair.getFirst();return new Vec3(first.getX()+pair.size()*.5,first.getY()+PIVOT,first.getZ()+.5);}
    /** The angle from the rest position after {@code ticks} sat: the sway grows to its reach and stays there. */
    public static double angle(double ticks){return Math.min(REACH,Math.max(0,ticks)*BUILD)*Math.sin(Math.max(0,ticks)*RATE);}
    /** The seat's place on the arc for an angle: forward (toward the house, north) is positive. */
    public static Vec3 seat(Vec3 pivot,double angle){return pivot.add(0,-LENGTH*Math.cos(angle),-LENGTH*Math.sin(angle));}
    /** A reader sits: the still swing is hidden so the moving one can be drawn in its place. */
    public static void hide(ServerPlayer p,BlockPos at){
        var l=p.serverLevel();
        for(var pos:pair(l,at)){var s=l.getBlockState(pos);l.setBlock(pos,s.setValue(HouseReadPropBlock.TURNED,true),Block.UPDATE_CLIENTS);for(int y=1;y<=3;y++)if(l.getBlockState(pos.above(y)).is(HouseReadRegistry.ROPE.get()))l.setBlock(pos.above(y),Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);}
        if(HouseReadSites.inside(p)==HouseReadSites.Room.PORCH)HouseReadSites.act(p,HouseReadSites.Room.PORCH,"sat on the swing");
    }
    /** Off the swing: the still seat and its ropes come back, up to the crossbar. */
    public static void restore(Level l,BlockPos at){
        for(var pos:pair(l,at)){var s=l.getBlockState(pos);if(is(s))l.setBlock(pos,s.setValue(HouseReadPropBlock.TURNED,false),Block.UPDATE_CLIENTS);
            for(int y=1;y<=3;y++)if(l.getBlockState(pos.above(y)).isAir())l.setBlock(pos.above(y),HouseReadRegistry.ROPE.get().defaultBlockState().setValue(ChainBlock.AXIS,Direction.Axis.Y),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);}
    }
}
