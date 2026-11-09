package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;

/**
 * 0.4.407, The Drawing: the village square's own things, drawn facing north by tools/draw_drawing_art.py.
 * <ul>
 * <li>BOX: the black box with its slot, on the stool. Stage 0 a split corner bound with string; 1 with wire; 2 with a bootlace; 3 the
 * paint mostly gone and the split wider, still with the bootlace.</li>
 * <li>STOOL: the three-legged stool the box stands on.</li>
 * <li>NOTICE: the typed notice pinned to the post office board. ACCOUNT: the other half of the board; stage 0 a bare drawing pin,
 * stage 1 a folded paper under it (sent to a reader whose account is ready).</li>
 * <li>STONES: the pile the children make against the wall, stage 0 a few to stage 3 a heap.</li>
 * <li>SHUTTER: a house window; stage 0 curtained, stage 1 shuttered.</li>
 * <li>AWNING: the grocery's striped awning, one block of it. SIGN: a painted sign over a door; stage 0 the post office, 1 the bank,
 * 2 the grocery, 3 the meeting hall. CRATE: a produce crate outside the grocery, stage 0 apples, stage 1 beans.</li>
 * </ul>
 */
public final class DrawingBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<DrawingBlock> CODEC=simpleCodec(DrawingBlock::new);
    public enum Kind implements StringRepresentable {
        BOX,STOOL,NOTICE,ACCOUNT,STONES,SHUTTER,AWNING,SIGN,CRATE;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,3);
    public DrawingBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.BOX).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return DrawingRegistry.FITTING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,Math.max(0,Math.min(3,stage)));}
    public static boolean is(BlockState s,Kind kind){return s.is(DrawingRegistry.FITTING.get())&&s.getValue(KIND)==kind;}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(s.getValue(KIND)){
            case BOX->Block.box(3,0,4,13,9,12);
            case STOOL->Block.box(2,0,2,14,16,14);
            case NOTICE,ACCOUNT->MarshBlock.turned(f,0,2,14,16,15,16);
            case STONES->Block.box(1,0,1,15,2+2*s.getValue(STAGE),15);
            case SHUTTER->Shapes.block();
            case AWNING->MarshBlock.turned(f,0,4,0,16,8,16);
            case SIGN->MarshBlock.turned(f,0,3,13,16,13,16);
            case CRATE->Block.box(1,0,1,15,10,15);
        };
    }
    /** The box, the board, the stones and the signs are reached past; the stool, the windows and the crates stand in the way. */
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){
            case STOOL->Block.box(2,0,2,14,16,14);
            case SHUTTER->Shapes.block();
            case CRATE->Block.box(1,0,1,15,10,15);
            case BOX->Block.box(3,0,4,13,9,12);
            default->Shapes.empty();
        };
    }
}
