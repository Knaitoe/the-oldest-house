package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.*;

/**
 * 0.4.579, the city on the ice: the fixed rope up the pass, a hand line along the foot of the rock at each side of the steps, and the
 * stakes it is made fast to. Drawn by tools/draw_city_art.py facing north with the rock to its west: FACING is the way along the rope
 * the block looks, the rock on its left.
 * <ul>
 * <li>SLOPE: level; or climbing a whole block across the cell, as the steps under it do, the way it faces (RISE) or the other way
 * (FALL).</li>
 * <li>STAKE: none; a stake the rope runs on past (BOTH); a stake the rope goes on from only ahead, or only behind, its end hanging from
 * the hitch.</li>
 * </ul>
 * It stops nobody. A hand finds a stake, and passes through the rope.
 */
public final class CityRopeBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<CityRopeBlock> CODEC=simpleCodec(CityRopeBlock::new);
    public enum Slope implements StringRepresentable {
        FLAT,RISE,FALL;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public enum Stake implements StringRepresentable {
        NONE,BOTH,AHEAD,BEHIND;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Slope> SLOPE=EnumProperty.create("slope",Slope.class);
    public static final EnumProperty<Stake> STAKE=EnumProperty.create("stake",Stake.class);
    /**
     * In sixteenths of a block: how far the rope stands out from the side of its cell the rock is on, and how high it is over the cell's
     * floor at the edge of a row. On steps it is that high at the upper edge and a whole block lower at the lower one. The tool draws
     * to the same two numbers and checks that it does.
     */
    public static final int OUT=3,HIGH=18;
    public CityRopeBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(SLOPE,Slope.FLAT).setValue(STAKE,Stake.NONE));}
    public static BlockState of(Direction facing,Slope slope,Stake stake){return CityRegistry.ROPE.get().defaultBlockState().setValue(FACING,facing).setValue(SLOPE,slope).setValue(STAKE,stake);}
    public static boolean is(BlockState s){return s.is(CityRegistry.ROPE.get());}
    /** The rope's height over its cell's floor at the edge on one side of it, in sixteenths; -1 where the rope does not run to that edge. */
    public static int high(BlockState s,Direction side){
        var f=s.getValue(FACING);boolean ahead=side==f;
        if(!ahead&&side!=f.getOpposite())return -1;
        if(s.getValue(STAKE)==(ahead?Stake.BEHIND:Stake.AHEAD))return -1;
        return switch(s.getValue(SLOPE)){case FLAT->HIGH;case RISE->ahead?HIGH:HIGH-16;case FALL->ahead?HIGH-16:HIGH;};
    }
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,SLOPE,STAKE);}
    /** A stake: in the middle of a level row, on the upper tread of a row of steps. The rope itself is nothing to a hand. */
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        if(s.getValue(STAKE)==Stake.NONE)return Shapes.empty();
        double z=switch(s.getValue(SLOPE)){case FLAT->8;case RISE->4;case FALL->12;};
        return MarshBlock.turned(s.getValue(FACING),OUT-1.5,0,z-1.5,OUT+1.5,16,z+1.5);
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Shapes.empty();}
}
