package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

/**
 * 0.4.199: the working parts of the lifts and landings, one block for each, drawn facing north and turned by {@link #FACING}.
 * The kitchen weights (STAGE: the bars stamped on them, 1 to 3) and the counterweight pan (STAGE: weights in it), the
 * dumbwaiter cage (STAGE 0 its lower half, 1 its upper), the pulley wheel, the heavy landing handle (STAGE: how far round
 * its grip has gone), the lift's pale up cord, dark down cord and brass-handled call cord, and the boards: a lift platform,
 * boards scraped in an arc where a landing swings, boards worn pale where it meets, and the riveted counterweight.
 * Drawn by tools/draw_puzzle_fittings.py.
 */
public final class PuzzleFittingBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<PuzzleFittingBlock> CODEC=simpleCodec(PuzzleFittingBlock::new);
    public enum Kind implements StringRepresentable {
        WEIGHT,PAN,CAGE,PULLEY,HANDLE,CORD_UP,CORD_DOWN,CALL,PLATFORM,SCRAPED,WORN,COUNTERWEIGHT;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
        public boolean full(){return this==PLATFORM||this==SCRAPED||this==WORN||this==COUNTERWEIGHT||this==CAGE;}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,3);
    public PuzzleFittingBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.PLATFORM).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return PuzzleRegistry.FITTING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing);}
    public static BlockState of(Kind kind,Direction facing,int stage){return of(kind,facing).setValue(STAGE,Math.max(0,Math.min(3,stage)));}
    public static boolean is(BlockState s,Kind kind){return s.is(PuzzleRegistry.FITTING.get())&&s.getValue(KIND)==kind;}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    static VoxelShape box(Direction f,double x0,double y0,double z0,double x1,double y1,double z1){return DareFittingBlock.box(f,x0,y0,z0,x1,y1,z1);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(s.getValue(KIND)){
            case WEIGHT->Block.box(5,0,5,11,2+2*Math.max(1,s.getValue(STAGE)),11);
            case PAN->Block.box(2,4,2,14,9,14);
            case PULLEY->Block.box(2,2,6,14,14,10);
            case HANDLE->Shapes.or(Block.box(6,0,6,10,16,10),box(f,3,9,8,13,13,16));
            case CORD_UP,CORD_DOWN,CALL->Block.box(6,0,6,10,16,10);
            default->Shapes.block();
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){
            // The landing handle stands on its post at railing height, so it is a rail as well as a handle.
            case HANDLE->Block.box(6,0,6,10,24,10);
            case WEIGHT->getShape(s,l,p,c);
            case PAN,PULLEY,CORD_UP,CORD_DOWN,CALL->Shapes.empty();
            default->Shapes.block();
        };
    }
    @Override protected InteractionResult useWithoutItem(BlockState s,Level level,BlockPos pos,Player player,BlockHitResult hit){
        if(!(player instanceof ServerPlayer p))return InteractionResult.SUCCESS;
        return PuzzleRooms.use(p,pos,s,hit.getLocation().y-pos.getY())?InteractionResult.CONSUME:InteractionResult.PASS;
    }
}
