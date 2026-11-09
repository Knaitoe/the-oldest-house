package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 0.4.414 (docs/BAR_GAMES_PROPOSAL.md): a pool table, two blocks long, laid like a bed: the foot where it was put down and the head
 * one block on, the way the placer faced ({@link #FACING} points from foot to head). The foot keeps the table's block entity, and the
 * frame on it is the foot's ({@link PoolTables}). Using either end joins the frame, or watches it.
 *
 * <p>An ordinary block: it breaks by hand and drops itself from the foot. Inside the House, the House's own rule keeps it where it
 * stands.
 */
public final class PoolTableBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<PoolTableBlock> CODEC=simpleCodec(PoolTableBlock::new);
    public static final EnumProperty<BedPart> PART=BlockStateProperties.BED_PART;
    public PoolTableBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(PART,BedPart.FOOT));}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,PART);}
    public static boolean is(BlockState s){return s.is(BarRegistry.POOL_TABLE.get());}
    /** The table's foot, where its frame is kept, from either end. */
    public static BlockPos foot(BlockState s,BlockPos pos){return s.getValue(PART)==BedPart.HEAD?pos.relative(s.getValue(FACING).getOpposite()):pos;}
    /**
     * Places a whole table, foot here and head the way it faces; for the builders. 0.4.441 (owner: "it's not physically in last_orders"):
     * through BuildBlocks, so that a scene built from a recorded plan lays the table in its turn. Written straight into the level, it went
     * in while the plan was still being recorded and the plan's own clearing of the room took it out again.
     */
    public static void place(Level level,BlockPos foot,Direction facing){
        var s=BarRegistry.POOL_TABLE.get().defaultBlockState().setValue(FACING,facing);
        if(level instanceof net.minecraft.server.level.ServerLevel sl){BuildBlocks.set(sl,foot,s.setValue(PART,BedPart.FOOT),18);BuildBlocks.set(sl,foot.relative(facing),s.setValue(PART,BedPart.HEAD),18);return;}
        level.setBlock(foot,s.setValue(PART,BedPart.FOOT),18);level.setBlock(foot.relative(facing),s.setValue(PART,BedPart.HEAD),18);
    }

    static final VoxelShape SHAPE=Block.box(0,0,0,16,13,16);
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return SHAPE;}

    @Override public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx){
        var dir=ctx.getHorizontalDirection();var head=ctx.getClickedPos().relative(dir);
        return ctx.getLevel().getBlockState(head).canBeReplaced(ctx)&&ctx.getLevel().getWorldBorder().isWithinBounds(head)?defaultBlockState().setValue(FACING,dir):null;
    }
    @Override public void setPlacedBy(Level level,BlockPos pos,BlockState state,@Nullable LivingEntity placer,ItemStack stack){
        if(!level.isClientSide)level.setBlock(pos.relative(state.getValue(FACING)),state.setValue(PART,BedPart.HEAD),3);
    }
    @Override protected BlockState updateShape(BlockState state,Direction dir,BlockState other,LevelAccessor level,BlockPos pos,BlockPos otherPos){
        var toOther=state.getValue(PART)==BedPart.FOOT?state.getValue(FACING):state.getValue(FACING).getOpposite();
        if(dir!=toOther)return super.updateShape(state,dir,other,level,pos,otherPos);
        return other.is(this)&&other.getValue(PART)!=state.getValue(PART)?state:Blocks.AIR.defaultBlockState();
    }
    @Override public BlockState playerWillDestroy(Level level,BlockPos pos,BlockState state,Player player){
        // Broken from its head by a player in creative, the foot goes without dropping anything, as a bed's does.
        if(!level.isClientSide&&player.isCreative()&&state.getValue(PART)==BedPart.HEAD){
            var foot=foot(state,pos);var f=level.getBlockState(foot);
            if(f.is(this)&&f.getValue(PART)==BedPart.FOOT){level.setBlock(foot,Blocks.AIR.defaultBlockState(),35);level.levelEvent(player,2001,foot,Block.getId(f));}
        }
        return super.playerWillDestroy(level,pos,state,player);
    }
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){
        if(level.isClientSide)return InteractionResult.SUCCESS;
        if(player instanceof ServerPlayer p)PoolTables.use(p,foot(state,pos));
        return InteractionResult.CONSUME;
    }
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos,BlockState state){return state.getValue(PART)==BedPart.FOOT?new PoolTableBlockEntity(pos,state):null;}
}
