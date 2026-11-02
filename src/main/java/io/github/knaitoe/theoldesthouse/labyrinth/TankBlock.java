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
 * 0.4.256, the Header Tank (docs/HEADER_TANK_0_4_256.md): a utility room's waterworks, drawn facing north (front to the room, back to
 * its wall) and turned by {@link #FACING}; drawn by tools/draw_tank_fittings.py.
 * <ul>
 * <li>The platform: slatted boards on floats, riding the water; a floor across the whole pit.</li>
 * <li>The feed valve's wheel, shut (stage 0) or open (1).</li>
 * <li>The hand pump, its handle up (0) or down (1).</li>
 * <li>A pipe's mouth on the pit wall, dry (0) or running (1).</li>
 * <li>The gauge board for the tank overhead, its float on a rod, the needle at 0 (empty) to 4 (full).</li>
 * <li>A pipe on the wall: a horizontal run (0), a vertical run (1), a joint where it turns or passes through a wall (2).</li>
 * <li>The transfer valve between two pits, shut (0), running east to west (1) or west to east (2), its arrow on the dial.</li>
 * </ul>
 */
public final class TankBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<TankBlock> CODEC=simpleCodec(TankBlock::new);
    public enum Kind implements StringRepresentable {
        PLATFORM,VALVE,PUMP,OUTLET,GAUGE,PIPE,TRANSFER;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,4);
    /** A pipe's stages. */
    public static final int RUN=0,RISE=1,JOINT=2;
    public TankBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.PIPE).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return PuzzleRegistry.TANK.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,Math.max(0,Math.min(4,stage)));}
    public static boolean is(BlockState s,Kind kind){return s.is(PuzzleRegistry.TANK.get())&&s.getValue(KIND)==kind;}
    public static int stage(BlockState s){return s.is(PuzzleRegistry.TANK.get())?s.getValue(STAGE):-1;}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    static VoxelShape box(Direction f,double x0,double y0,double z0,double x1,double y1,double z1){return WorkPrintBlock.box(f,x0,y0,z0,x1,y1,z1);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(s.getValue(KIND)){
            case PLATFORM->Block.box(0,10,0,16,16,16);
            case VALVE,TRANSFER->box(f,3,3,10,13,13,16);
            case PUMP->box(f,4,0,6,12,16,14);
            case OUTLET->box(f,5,5,8,11,11,16);
            case GAUGE->box(f,3,2,14,13,14,16);
            case PIPE->s.getValue(STAGE)==JOINT?box(f,4,4,12,12,12,16):s.getValue(STAGE)==RISE?box(f,6,0,13,10,16,16):box(f,0,6,13,16,10,16);
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){
            case PLATFORM,PUMP->getShape(s,l,p,c);
            default->Shapes.empty();
        };
    }
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return Shapes.empty();}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level level,BlockPos pos,Player player,BlockHitResult hit){
        if(level.isClientSide||!(player instanceof ServerPlayer p))return InteractionResult.SUCCESS;
        return HeaderTanks.use(p,pos,s)?InteractionResult.CONSUME:InteractionResult.PASS;
    }
    /** The pump's handle springs back up after a stroke. */
    @Override protected void tick(BlockState s,net.minecraft.server.level.ServerLevel l,BlockPos p,net.minecraft.util.RandomSource r){HeaderTanks.handleUp(l,p,s);}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
