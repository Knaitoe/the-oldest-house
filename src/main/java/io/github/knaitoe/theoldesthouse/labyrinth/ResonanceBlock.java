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
 * 0.4.360, the Resonant Pipes (ResonantPipes, docs/RESONANT_PIPES_0_4_360.md): the fittings of the singing pipework, drawn by
 * tools/draw_resonance_fittings.py in vanilla's manner. A run of pipe lies along its {@link #FACING} (north is along z).
 * <ul>
 * <li>The drum a tone comes from: low (0), middle (1), high (2).</li>
 * <li>A pipe and a riser: silent (0) or carrying a tone (1), the dust on it trembling.</li>
 * <li>A valve: shut (0) or open (1).</li>
 * <li>A short section of pipe: out on its bracket (0) or fitted (1).</li>
 * <li>A reed box: quiet (0), singing its own note (1), buzzing against a wrong one (2).</li>
 * <li>The latch on the far door: down (0) or lifted (1).</li>
 * </ul>
 */
public final class ResonanceBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<ResonanceBlock> CODEC=simpleCodec(ResonanceBlock::new);
    public enum Kind implements StringRepresentable {
        DRUM,PIPE,RISER,VALVE,SECTION,REED,LATCH;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,2);
    public ResonanceBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.PIPE).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing,int stage){return PuzzleRegistry.RESONANCE.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,Math.max(0,Math.min(2,stage)));}
    public static boolean is(BlockState s,Kind kind){return s.is(PuzzleRegistry.RESONANCE.get())&&s.getValue(KIND)==kind;}
    public static int stage(BlockState s){return s.is(PuzzleRegistry.RESONANCE.get())?s.getValue(STAGE):-1;}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    static boolean alongX(BlockState s){return s.getValue(FACING).getAxis()==Direction.Axis.X;}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        boolean x=alongX(s);
        return switch(s.getValue(KIND)){
            case DRUM->Block.box(2,0,2,14,16,14);
            case PIPE->x?Block.box(0,5,5,16,11,11):Block.box(5,5,0,11,11,16);
            case RISER->Block.box(5,0,5,11,16,11);
            case VALVE->x?Block.box(0,3,3,16,13,13):Block.box(3,3,0,13,13,16);
            case SECTION->s.getValue(STAGE)==1?(x?Block.box(0,4,4,16,12,12):Block.box(4,4,0,12,12,16)):(x?Shapes.or(Block.box(0,4,4,3,12,12),Block.box(13,4,4,16,12,12)):Shapes.or(Block.box(4,4,0,12,12,3),Block.box(4,4,13,12,12,16)));
            case REED,LATCH->WorkPrintBlock.box(s.getValue(FACING),2,1,11,14,15,16);
        };
    }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return s.getValue(KIND)==Kind.DRUM?getShape(s,l,p,c):Shapes.empty();}
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return Shapes.empty();}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level level,BlockPos pos,Player player,BlockHitResult hit){
        if(level.isClientSide||!(player instanceof ServerPlayer p))return InteractionResult.SUCCESS;
        return ResonantPipes.use(p,pos,s)?InteractionResult.CONSUME:InteractionResult.PASS;
    }
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
