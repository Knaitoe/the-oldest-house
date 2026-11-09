package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 0.4.413 (docs/POLYBIUS_HOME_CABINET_0_4_413.md): an arcade cabinet of the reader's own, made with the service key. It stands two
 * blocks tall like the arcade's (the same models): the control panel below, the screen and marquee above. It plays one of the
 * arcade's three working games, never the black one: Mothlight, Undertow or Signal ({@link #GAME}). Using it plays; crouching
 * and using it changes the board to the next game. One reader plays it at a time. It keeps its own table of ten
 * ({@link HomeCabinetBlockEntity}) and pays no tickets: the tickets are the arcade's.
 *
 * <p>Unlike the House's fittings it is an ordinary block: it breaks by hand or axe and drops itself, from its lower half.
 */
public final class HomeCabinetBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<HomeCabinetBlock> CODEC=simpleCodec(HomeCabinetBlock::new);
    public static final EnumProperty<DoubleBlockHalf> HALF=BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final IntegerProperty GAME=IntegerProperty.create("game",0,PolybiusSite.BLACK-1);
    public HomeCabinetBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(HALF,DoubleBlockHalf.LOWER).setValue(GAME,0));}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,HALF,GAME);}
    public static int light(BlockState s){return s.getValue(HALF)==DoubleBlockHalf.UPPER?8:0;}
    /** The cabinet's lower half, where its table is kept, from either half. */
    public static BlockPos lower(BlockState s,BlockPos pos){return s.getValue(HALF)==DoubleBlockHalf.UPPER?pos.below():pos;}
    public static boolean is(BlockState s){return s.is(PolybiusRegistry.HOME_CABINET.get());}

    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return s.getValue(HALF)==DoubleBlockHalf.LOWER?PolybiusBlock.box(f,1,0,1,15,16,15):PolybiusBlock.box(f,1,0,2,15,16,15);
    }

    // ------------------------------------------------------------------------------------------------ two halves, as a door is
    @Override public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx){
        var pos=ctx.getClickedPos();var level=ctx.getLevel();
        if(pos.getY()>=level.getMaxBuildHeight()-1||!level.getBlockState(pos.above()).canBeReplaced(ctx))return null;
        return defaultBlockState().setValue(FACING,ctx.getHorizontalDirection().getOpposite()).setValue(HALF,DoubleBlockHalf.LOWER);
    }
    @Override public void setPlacedBy(Level level,BlockPos pos,BlockState state,@Nullable LivingEntity placer,ItemStack stack){
        level.setBlock(pos.above(),state.setValue(HALF,DoubleBlockHalf.UPPER),3);
    }
    @Override protected BlockState updateShape(BlockState state,Direction facing,BlockState other,LevelAccessor level,BlockPos pos,BlockPos otherPos){
        var half=state.getValue(HALF);
        if(facing.getAxis()!=Direction.Axis.Y||half==DoubleBlockHalf.LOWER!=(facing==Direction.UP))return super.updateShape(state,facing,other,level,pos,otherPos);
        // The halves keep each other: the screen follows the panel's board and facing, and neither stands without the other.
        return other.is(this)&&other.getValue(HALF)!=half?other.setValue(HALF,half):net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
    }
    @Override protected boolean canSurvive(BlockState state,LevelReader level,BlockPos pos){
        if(state.getValue(HALF)==DoubleBlockHalf.LOWER)return true;
        var below=level.getBlockState(pos.below());return below.is(this)&&below.getValue(HALF)==DoubleBlockHalf.LOWER;
    }
    @Override public BlockState playerWillDestroy(Level level,BlockPos pos,BlockState state,Player player){
        // Broken from its screen by a player in creative, the panel goes without dropping anything, as a door's does.
        if(!level.isClientSide&&player.isCreative()&&state.getValue(HALF)==DoubleBlockHalf.UPPER){
            var below=pos.below();var lower=level.getBlockState(below);
            if(lower.is(this)&&lower.getValue(HALF)==DoubleBlockHalf.LOWER){level.setBlock(below,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),35);level.levelEvent(player,2001,below,Block.getId(lower));}
        }
        return super.playerWillDestroy(level,pos,state,player);
    }

    // ------------------------------------------------------------------------------------------------ playing it
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){
        if(level.isClientSide)return InteractionResult.SUCCESS;
        if(!(player instanceof ServerPlayer p))return InteractionResult.PASS;
        var at=lower(state,pos);var panel=level.getBlockState(at);if(!is(panel))return InteractionResult.PASS;
        if(p.isSecondaryUseActive()){
            if(Polybius.playingHome(at,level)){p.displayClientMessage(Component.literal("Someone is playing it.").withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);return InteractionResult.CONSUME;}
            int game=(panel.getValue(GAME)+1)%(PolybiusSite.BLACK);
            level.setBlock(at,panel.setValue(GAME,game),3);
            level.playSound(null,at,SoundEvents.IRON_TRAPDOOR_CLOSE,SoundSource.BLOCKS,.5F,1.6F);
            p.displayClientMessage(Component.literal("You swap the board. The marquee says "+PolybiusScores.NAMES[game]+".").withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);
            return InteractionResult.CONSUME;
        }
        Polybius.playHome(p,at,panel.getValue(GAME));
        return InteractionResult.CONSUME;
    }

    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos,BlockState state){return state.getValue(HALF)==DoubleBlockHalf.LOWER?new HomeCabinetBlockEntity(pos,state):null;}
}
