package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

/**
 * 0.4.254, the Fuse Board (docs/FUSE_BOARD_0_4_254.md): a basement's electrics, drawn facing north (front to the room, back to its wall)
 * and turned by {@link #FACING}; drawn by tools/draw_power_fittings.py.
 * <ul>
 * <li>A fuse holder for each circuit, its label plate in an old hand: empty (stage 0) or with a fuse in it (1).</li>
 * <li>The spare clip, with the board's pilot lamp: empty (0) or holding a fuse (1).</li>
 * <li>A cable for each circuit, clipped to the wall: a horizontal run (0), a vertical run (1), a junction box where it turns or passes
 * through a wall (2).</li>
 * <li>The work light, off (0) or on (1).</li>
 * <li>The roller shutter's slats (0) and its bottom slat (1), with a line of light under it; its housing, the motor's lamp dark (0) or
 * lit (1); the guide rail; the manual catch on the rail, engaged (0) or drawn and pinned back (1).</li>
 * <li>An electric latch box beside a door, its lamp dark (0) or lit (1); the wedge, lying by the wall (0) or kicked under the open
 * door (1).</li>
 * <li>A wall hoist's motor, dark (0) or lit (1); its jib arm (0) and the arm's end with its pulley (1); the crate it lifts; the floor
 * hatch under it, shut (0) or open (1), climbable through when open.</li>
 * </ul>
 * The shutter, its housing and the guide rail stop light: the room behind the shutter is lit and the workshop is dark.
 */
public final class PowerBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<PowerBlock> CODEC=simpleCodec(PowerBlock::new);
    public enum Kind implements StringRepresentable {
        HOLDER_LIGHT,HOLDER_SHUTTER,HOLDER_LATCH,HOLDER_HOIST,CLIP,
        CABLE_LIGHT,CABLE_SHUTTER,CABLE_LATCH,CABLE_HOIST,
        WORK_LAMP,SHUTTER,SHUTTER_BOX,GUIDE,CATCH,LATCH,WEDGE,HOIST,JIB,CRATE,HATCH;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
        public boolean holder(){return this==HOLDER_LIGHT||this==HOLDER_SHUTTER||this==HOLDER_LATCH||this==HOLDER_HOIST;}
        public boolean cable(){return this==CABLE_LIGHT||this==CABLE_SHUTTER||this==CABLE_LATCH||this==CABLE_HOIST;}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,2);
    /** A cable's stages. */
    public static final int RUN=0,RISE=1,JUNCTION=2;
    public PowerBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.CLIP).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return PuzzleRegistry.POWER.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,Math.max(0,Math.min(2,stage)));}
    public static boolean is(BlockState s,Kind kind){return s.is(PuzzleRegistry.POWER.get())&&s.getValue(KIND)==kind;}
    public static Kind kind(BlockState s){return s.is(PuzzleRegistry.POWER.get())?s.getValue(KIND):null;}
    public static int stage(BlockState s){return s.is(PuzzleRegistry.POWER.get())?s.getValue(STAGE):-1;}
    public static int light(BlockState s){
        return switch(s.getValue(KIND)){
            case WORK_LAMP->s.getValue(STAGE)==1?15:0;
            case CLIP->4;
            case SHUTTER->s.getValue(STAGE)==1?3:0;
            case LATCH,HOIST,SHUTTER_BOX->s.getValue(STAGE)==1?4:0;
            default->0;
        };
    }
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    static VoxelShape box(Direction f,double x0,double y0,double z0,double x1,double y1,double z1){return WorkPrintBlock.box(f,x0,y0,z0,x1,y1,z1);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);var kind=s.getValue(KIND);
        if(kind.holder())return box(f,4,2,13,12,14,16);
        if(kind.cable())return s.getValue(STAGE)==JUNCTION?box(f,4,4,12,12,12,16):s.getValue(STAGE)==RISE?box(f,6,0,14,10,16,16):box(f,0,6,14,16,10,16);
        return switch(kind){
            case CLIP->box(f,4,3,13,12,13,16);
            case WORK_LAMP->box(f,4,4,8,12,12,16);
            case SHUTTER->box(f,0,0,6,16,16,10);
            case SHUTTER_BOX->box(f,0,0,2,16,16,14);
            case GUIDE,CATCH->Shapes.block();
            case LATCH->box(f,4,3,12,12,13,16);
            case WEDGE->s.getValue(STAGE)==0?box(f,3,0,9,13,3,15):box(f,10,0,6,16,3,16);
            case HOIST->box(f,2,2,6,14,14,16);
            case JIB->s.getValue(STAGE)==0?box(f,6,10,0,10,14,16):box(f,4,6,0,12,14,16);
            case CRATE->box(f,1,0,1,15,14,15);
            case HATCH->s.getValue(STAGE)==0?Shapes.block():Shapes.or(Block.box(0,0,0,16,16,2),Block.box(0,0,14,16,16,16),Block.box(0,0,0,2,16,16),Block.box(14,0,0,16,16,16));
            default->Shapes.block();
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var kind=s.getValue(KIND);
        if(kind.holder()||kind.cable())return Shapes.empty();
        return switch(kind){
            case CLIP,WORK_LAMP,LATCH,WEDGE,JIB->Shapes.empty();
            // The shutter stops a body across its whole height, the bottom slat's gap included.
            case SHUTTER->box(s.getValue(FACING),0,0,6,16,16,10);
            // An open hatch is a hole to climb through.
            case HATCH->s.getValue(STAGE)==0?Shapes.block():Shapes.empty();
            default->getShape(s,l,p,c);
        };
    }
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){
        var kind=s.getValue(KIND);return kind==Kind.GUIDE||kind==Kind.CATCH||kind==Kind.HATCH&&s.getValue(STAGE)==0?Shapes.block():Shapes.empty();
    }
    /** The shutter, its housing and its rails keep the lit room's light on its own side. */
    @Override protected int getLightBlock(BlockState s,BlockGetter l,BlockPos p){
        var kind=s.getValue(KIND);
        return kind==Kind.SHUTTER||kind==Kind.SHUTTER_BOX||kind==Kind.GUIDE||kind==Kind.CATCH?15:super.getLightBlock(s,l,p);
    }
    @Override public boolean isLadder(BlockState s,LevelReader l,BlockPos p,LivingEntity e){return s.getValue(KIND)==Kind.HATCH&&s.getValue(STAGE)==1;}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level level,BlockPos pos,Player player,BlockHitResult hit){
        if(level.isClientSide||!(player instanceof ServerPlayer p))return InteractionResult.SUCCESS;
        return FuseRooms.use(p,pos,s)?InteractionResult.CONSUME:InteractionResult.PASS;
    }
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
