package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.*;

/**
 * 0.4.701, The list (docs/THE_LIST_0_4_701.md): the pasture's two machines. The block draws what stands still (the windmill's head on
 * its mast, the pumpjack's saddle on its Samson post); its block entity keeps nothing, and its renderer draws what moves (the wheel, the
 * walking beam, the cranks, the pitman arms and the rod) from the level's time (TheListMachines).
 */
public final class TheListMachineBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<TheListMachineBlock> CODEC=simpleCodec(TheListMachineBlock::new);
    public enum Machine implements StringRepresentable {
        /** CABLE is never placed: it is the pumpjack's bridle, which the renderer stretches from the carrier bar to the horsehead. */
        WINDMILL,PUMPJACK,CABLE;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Machine> MACHINE=EnumProperty.create("machine",Machine.class);
    public TheListMachineBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(MACHINE,Machine.WINDMILL));}
    public static BlockState of(Machine m,Direction facing){return TheListRegistry.MACHINE.get().defaultBlockState().setValue(MACHINE,m).setValue(FACING,facing);}
    public static boolean is(BlockState s,Machine m){return s.is(TheListRegistry.MACHINE.get())&&s.getValue(MACHINE)==m;}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,MACHINE);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return s.getValue(MACHINE)==Machine.WINDMILL?MarshBlock.turned(f,4,2,2,12,13,14):MarshBlock.turned(f,0,0,5,16,19,11);
    }
    @Override public BlockEntity newBlockEntity(BlockPos at,BlockState s){return new Turning(at,s);}
    /** Nothing kept: it is there to be drawn. */
    public static final class Turning extends BlockEntity {
        public Turning(BlockPos at,BlockState s){super(TheListRegistry.MACHINE_ENTITY.get(),at,s);}
    }
}
