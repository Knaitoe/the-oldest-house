package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * 0.4.678: one lettered plate ({@link LetterPlates}). Its facing is the way the words look; its words are in its
 * {@link LetterPlateEntity}. Nothing walks into it, and a hand on it reads it.
 */
public final class LetterPlateBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<LetterPlateBlock> CODEC=simpleCodec(LetterPlateBlock::new);
    public static final EnumProperty<LetterPlates.Kind> KIND=EnumProperty.create("kind",LetterPlates.Kind.class);
    public LetterPlateBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,LetterPlates.Kind.NOTE));}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND);}
    public static boolean is(BlockState s){return s.is(BoardRegistry.PLATE.get());}
    public static boolean is(BlockState s,LetterPlates.Kind kind){return is(s)&&s.getValue(KIND)==kind;}
    public static BlockState of(LetterPlates.Kind kind,Direction facing){return BoardRegistry.PLATE.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing);}
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new LetterPlateEntity(pos,state);}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level level,BlockPos pos,Player player,BlockHitResult hit){
        if(player instanceof ServerPlayer p&&level.getBlockEntity(pos) instanceof LetterPlateEntity plate)
            if(!plate.look().isEmpty())p.displayClientMessage(Component.literal(plate.look()).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){var kept=SHAPES_0767.get(s);if(kept==null){kept=shapeBuilt0767(s);SHAPES_0767.put(s,kept);}return kept;}
    // 0.4.767: the shape of each state, built once and kept; it was built again on every call (docs/REVIEW_REPAIRS_0_4_767.md).
    private static final java.util.Map<BlockState,VoxelShape> SHAPES_0767=new java.util.concurrent.ConcurrentHashMap<>();
    private VoxelShape shapeBuilt0767(BlockState s){
        var f=s.getValue(FACING);
        return switch(s.getValue(KIND)){
            case NOTE->Shapes.or(MarshBlock.turned(f,1,7,7,15,16,9),MarshBlock.turned(f,7,0,7,9,7,9));
            case LABEL->MarshBlock.turned(f,2,5,15,14,11,16);
            case NOTICE->MarshBlock.turned(f,2,0,15,14,16,16);
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Shapes.empty();}
}
