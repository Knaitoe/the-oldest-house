package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 0.4.390, the new ordinary halls' fittings (OrdinaryHalls), drawn by tools/draw_ordinary_hall_art.py. {@link #FACING} is the way
 * the fitting's front faces.
 *
 * <p>The laundry corridor's wicker baskets of folded washing, and the stool kept to reach the lines. 0.4.391, the atrium's half
 * steps and its balustrade (a straight length, {@link #FACING} the side of the block it stands on, and a corner, on that side and
 * the next clockwise), a rail too high to climb over. 0.4.391, the library stacks' reading table.
 */
public final class OrdinaryHallBlock extends Block {
    public static final MapCodec<OrdinaryHallBlock> CODEC=simpleCodec(OrdinaryHallBlock::new);
    public enum Kind implements StringRepresentable {
        BASKET,STOOL,STEP,BALUSTRADE,BALUSTRADE_CORNER,TABLE;
        @Override public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
    public OrdinaryHallBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(KIND,Kind.BASKET).setValue(FACING,Direction.SOUTH));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND,FACING);}
    public static BlockState of(Kind kind,Direction facing){return OrdinaryHallRegistry.FITTING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing);}
    public static boolean is(BlockState s,Kind kind){return s.is(OrdinaryHallRegistry.FITTING.get())&&s.getValue(KIND)==kind;}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return shape(s,16);}
    /** The balustrade stops a body to a block and a half, as a fence does, so it cannot be climbed over. */
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return shape(s,24);}
    private static VoxelShape shape(BlockState s,int railTop){var f=s.getValue(FACING);return switch(s.getValue(KIND)){
        case BASKET->box(2,0,2,14,9,14);
        case STOOL->box(3,0,3,13,11,13);
        case STEP->box(0,0,0,16,8,16);
        case BALUSTRADE->side(f,railTop);
        case BALUSTRADE_CORNER->net.minecraft.world.phys.shapes.Shapes.or(side(f,railTop),side(f.getClockWise(),railTop));
        case TABLE->box(1,0,1,15,13,15);
    };}
    /** A rail two pixels thick along one side of the block. */
    public static VoxelShape side(Direction f,int top){return switch(f){
        case NORTH->box(0,0,0,16,top,2);case SOUTH->box(0,0,14,16,top,16);case WEST->box(0,0,0,2,top,16);default->box(14,0,0,16,top,16);};}
    /** 0.4.397: a basket of folded washing answers a hand (OrdinaryHalls.folded); nothing else here does. */
    @Override protected net.minecraft.world.InteractionResult useWithoutItem(BlockState s,net.minecraft.world.level.Level l,BlockPos at,net.minecraft.world.entity.player.Player player,net.minecraft.world.phys.BlockHitResult hit){
        var kind=s.getValue(KIND);
        if(kind!=Kind.BASKET&&kind!=Kind.TABLE)return net.minecraft.world.InteractionResult.PASS;
        if(l.isClientSide)return net.minecraft.world.InteractionResult.SUCCESS;
        if(player instanceof net.minecraft.server.level.ServerPlayer p){if(kind==Kind.BASKET)OrdinaryHalls.folded(p,at);else OrdinaryHalls.readTable(p,at);}
        return net.minecraft.world.InteractionResult.CONSUME;
    }
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
