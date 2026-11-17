package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 0.4.677: one block of one of the painted signs that replaced the game's own ({@link BoardSigns}), as the flood station's are
 * ({@link CrossingSignBlock}). {@link #PIECE} is which square of which sign's picture it shows; its facing is the way the lettering
 * looks. Nothing walks into it, and a hand on it reads it.
 */
public final class BoardSignBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<BoardSignBlock> CODEC=simpleCodec(BoardSignBlock::new);
    public static final IntegerProperty PIECE=IntegerProperty.create("piece",0,BoardSigns.Sign.TOTAL-1);
    public BoardSignBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(PIECE,0));}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,PIECE);}
    public static boolean is(BlockState s){return s.is(BoardRegistry.SIGN.get());}
    public static boolean is(BlockState s,BoardSigns.Sign sign){return is(s)&&sign(s)==sign;}
    public static BoardSigns.Sign sign(BlockState s){return BoardSigns.Sign.of(s.getValue(PIECE));}
    /** Block {@code piece} of a sign (0 is its top left), looking {@code facing}. */
    public static BlockState of(BoardSigns.Sign sign,int piece,Direction facing){return BoardRegistry.SIGN.get().defaultBlockState().setValue(PIECE,sign.first()+piece).setValue(FACING,facing);}
    static int light(BlockState s){return sign(s).light;}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level level,BlockPos pos,Player player,BlockHitResult hit){
        if(player instanceof ServerPlayer p)p.displayClientMessage(net.minecraft.network.chat.Component.literal(sign(s).look()).withStyle(net.minecraft.ChatFormatting.GRAY,net.minecraft.ChatFormatting.ITALIC),true);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(sign(s).mount){
            case WALL,DECAL->MarshBlock.turned(f,0,0,14,16,16,16);
            case WINDOW->MarshBlock.turned(f,0,1,0,16,15,2);
            case FREE->MarshBlock.turned(f,0,0,7,16,16,9);
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Shapes.empty();}
}
