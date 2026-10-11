package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.*;

/** The fan's four-legged work ladder; visible rails and treads are its actual collision. */
public final class FanLadderBlock extends Block {
    public static final MapCodec<FanLadderBlock> CODEC=simpleCodec(FanLadderBlock::new);
    public static final IntegerProperty PART=IntegerProperty.create("part",0,29);
    private static final VoxelShape[] SHAPES=new VoxelShape[30];
    static {for(int part=0;part<30;part++){var shape=Shapes.empty();for(var b:FanLadderGeometry.BOXES[part])shape=Shapes.or(shape,Block.box(b[0],b[1],b[2],b[3],b[4],b[5]));SHAPES[part]=shape.optimize();}}
    public FanLadderBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(PART,4));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(PART);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return SHAPES[s.getValue(PART)];}
    @Override public boolean isLadder(BlockState s,LevelReader l,BlockPos p,LivingEntity entity){return true;}
    public static BlockState part(int n){return LiteraryRegistry.FAN_LADDER.get().defaultBlockState().setValue(PART,n);}
}
