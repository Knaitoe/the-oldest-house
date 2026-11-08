package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 0.4.391, the new ordinary halls' solid faces that look like more than they are (OrdinaryHalls), drawn by
 * tools/draw_ordinary_hall_art.py. Whole blocks, opaque; trying one (a hand on it) gets a line (OrdinaryHalls.tried).
 *
 * <ul>
 *   <li>The long bedroom hall's doors painted onto the wall: the lower half with its knob, and the upper. {@link #FACING} is the
 *   way the painting faces.</li>
 *   <li>0.4.391, the library stacks' shelves, packed with books that will not come out, in two fillings.</li>
 * </ul>
 */
public final class HallFaceBlock extends Block {
    public static final MapCodec<HallFaceBlock> CODEC=simpleCodec(HallFaceBlock::new);
    public enum Kind implements StringRepresentable {
        PAINTED_DOOR,PAINTED_DOOR_TOP,SHELF,SHELF_B;
        @Override public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
        public boolean door(){return this==PAINTED_DOOR||this==PAINTED_DOOR_TOP;}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
    public HallFaceBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(KIND,Kind.PAINTED_DOOR).setValue(FACING,Direction.SOUTH));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND,FACING);}
    public static BlockState of(Kind kind,Direction facing){return OrdinaryHallRegistry.FACE.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing);}
    public static boolean is(BlockState s,Kind kind){return s.is(OrdinaryHallRegistry.FACE.get())&&s.getValue(KIND)==kind;}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos at,Player player,BlockHitResult hit){
        if(l.isClientSide)return InteractionResult.SUCCESS;
        if(player instanceof ServerPlayer p)OrdinaryHalls.tried(p,at,s.getValue(KIND));
        return InteractionResult.CONSUME;
    }
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
