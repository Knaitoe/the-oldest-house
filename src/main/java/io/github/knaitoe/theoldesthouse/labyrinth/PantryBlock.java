package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

/**
 * 0.4.363, the Cold Pantry (ColdPantry, docs/COLD_PANTRY_0_4_363.md): the pantry's fittings, drawn by tools/draw_pantry_fittings.py
 * facing toward the room and turned by {@link #FACING}.
 * <ul>
 * <li>The ice-box and the zinc tub, each holding none to three blocks of ice (stage).</li>
 * <li>A ventilation shutter, shut (0) or open (1).</li>
 * <li>A pipe under the ceiling, bare (0) to thick with frost (3); the thermometer at four heights.</li>
 * <li>The cold store's door, lower and upper, shut (0) or open (1).</li>
 * <li>The cake under its bell: perfect (0), slumped (1), sunk and furred (2).</li>
 * </ul>
 */
public final class PantryBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<PantryBlock> CODEC=simpleCodec(PantryBlock::new);
    public enum Kind implements StringRepresentable {
        ICEBOX,TUB,SHUTTER,PIPE,THERMO,STORE_DOOR,STORE_DOOR_TOP,CAKE;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,3);
    public PantryBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.PIPE).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing,int stage){return PuzzleRegistry.PANTRY.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,Math.max(0,Math.min(3,stage)));}
    public static boolean is(BlockState s,Kind kind){return s.is(PuzzleRegistry.PANTRY.get())&&s.getValue(KIND)==kind;}
    public static int stage(BlockState s){return s.is(PuzzleRegistry.PANTRY.get())?s.getValue(STAGE):-1;}
    /** An open shutter lets daylight in. */
    public static int light(BlockState s){return s.getValue(KIND)==Kind.SHUTTER&&s.getValue(STAGE)==1?13:0;}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    static VoxelShape box(Direction f,double x0,double y0,double z0,double x1,double y1,double z1){return WorkPrintBlock.box(f,x0,y0,z0,x1,y1,z1);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(s.getValue(KIND)){
            case ICEBOX->Block.box(1,0,1,15,13,15);
            case TUB->Block.box(2,0,2,14,7,14);
            case SHUTTER->box(f,0,0,14,16,16,16);
            case PIPE->box(f,0,11,6,16,15,10);
            case THERMO->box(f,5,1,15,11,15,16);
            case STORE_DOOR,STORE_DOOR_TOP->s.getValue(STAGE)==0?box(f,0,0,13,16,16,16):box(f,0,0,0,3,16,16);
            case CAKE->Block.box(1,0,1,15,15,15);
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){
            case ICEBOX,TUB,STORE_DOOR,STORE_DOOR_TOP,CAKE->getShape(s,l,p,c);
            default->Shapes.empty();
        };
    }
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return Shapes.empty();}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level level,BlockPos pos,Player player,BlockHitResult hit){
        if(level.isClientSide||!(player instanceof ServerPlayer p))return InteractionResult.SUCCESS;
        return ColdPantry.use(p,pos,s)?InteractionResult.CONSUME:InteractionResult.PASS;
    }
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
