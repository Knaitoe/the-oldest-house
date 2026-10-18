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
 * Costume Night's party (0.4.103): a string of bulbs on its wire, the radio, the tub for bobbing apples, the candy bowl
 * and the contest board in two halves. Each model faces north; FACING turns it. LIT is the party's power: the bulbs give
 * light and the radio's dial glows until the lights go out.
 */
public final class CostumePropBlock extends Block {
    public static final MapCodec<CostumePropBlock> CODEC=simpleCodec(CostumePropBlock::new);
    public enum Kind implements StringRepresentable {
        BULBS,RADIO,APPLE_TUB,CANDY_BOWL,BOARD,BOARD_TOP;
        @Override public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT=BlockStateProperties.LIT;
    public CostumePropBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(KIND,Kind.BULBS).setValue(FACING,Direction.NORTH).setValue(LIT,true));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND,FACING,LIT);}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
    public static BlockState of(Kind k,Direction d){return CostumeNightRegistry.PROP.get().defaultBlockState().setValue(KIND,k).setValue(FACING,d);}
    public static boolean is(BlockState s,Kind k){return s.is(CostumeNightRegistry.PROP.get())&&s.getValue(KIND)==k;}
    /** How much light a prop gives: the bulbs while the power is on, the radio's dial a little. */
    public static int light(BlockState s){return !s.getValue(LIT)?0:switch(s.getValue(KIND)){case BULBS->11;case RADIO->3;default->0;};}
    /** A box given as it sits facing north, turned to the block's facing. */
    private static VoxelShape turned(Direction d,double x0,double y0,double z0,double x1,double y1,double z1){
        return switch(d){case SOUTH->box(16-x1,y0,16-z1,16-x0,y1,16-z0);case EAST->box(16-z1,y0,x0,16-z0,y1,x1);case WEST->box(z0,y0,16-x1,z1,y1,16-x0);default->box(x0,y0,z0,x1,y1,z1);};
    }
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos at,CollisionContext c){
        var d=s.getValue(FACING);
        return switch(s.getValue(KIND)){case BULBS->turned(d,0,9,6,16,14,10);case RADIO->turned(d,3,0,5,13,8,11);case APPLE_TUB->box(1,0,1,15,9,15);
            case CANDY_BOWL->box(4,0,4,12,4,12);case BOARD,BOARD_TOP->turned(d,1,0,6,15,16,10);};
    }
    /** The bulbs hang overhead and the bowl sits low; nothing there to bump into. */
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos at,CollisionContext c){
        return switch(s.getValue(KIND)){case BULBS,CANDY_BOWL->Shapes.empty();default->getShape(s,l,at,c);};
    }
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos at,Player player,BlockHitResult hit){
        if(player instanceof ServerPlayer p)CostumeNight.use(p,s.getValue(KIND),at);
        return InteractionResult.sidedSuccess(l.isClientSide());
    }
}
