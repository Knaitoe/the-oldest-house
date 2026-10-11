package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Vanilla-scale pixels and collision for the appended gray hazards. */
public final class HazardBlock extends Block {
    public static final MapCodec<HazardBlock> CODEC=simpleCodec(HazardBlock::new);
    public enum Kind implements StringRepresentable {
        LOCKER_WALL,FROSTED_CRATE,COMPRESSOR,HEAT_LAMP,HOOK,SHIP_PLATE,RAIL,SCULK_BOARDS,MAZE_PANEL,MIRROR_GLASS,NET,PARLOUR_WALL,NARROW_ARCH,SKIRT_GAP,NUMBER_FOUR,SCONCE,CISTERN_TILE,PIT_STONE,PRESSURE_PANEL,DRIFT_TILE;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,1);
    public HazardBlock(Properties properties){super(properties);registerDefaultState(stateDefinition.any().setValue(KIND,Kind.LOCKER_WALL).setValue(STAGE,0));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND,STAGE);}
    public static BlockState of(Kind k){return HazardRegistry.PIECE.get().defaultBlockState().setValue(KIND,k);}
    public static boolean is(BlockState s,Kind k){return s.is(HazardRegistry.PIECE.get())&&s.getValue(KIND)==k;}
    public static int light(BlockState s){return s.getValue(STAGE)==0&&(s.getValue(KIND)==Kind.HEAT_LAMP||s.getValue(KIND)==Kind.SCONCE)?14:0;}
    @Override public VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return switch(s.getValue(KIND)){
        case RAIL->Shapes.or(box(6,0,6,10,16,10),box(0,11,6,16,14,10));
        case HOOK->Shapes.or(box(7,8,7,9,16,9),box(7,5,7,13,8,9));
        case NUMBER_FOUR->box(14,0,0,16,16,16);
        case NET,SCULK_BOARDS->box(0,14,0,16,16,16);
        case NARROW_ARCH->Shapes.or(box(0,0,0,3,16,16),box(13,0,0,16,16,16),box(3,15,0,13,16,16));
        case SKIRT_GAP->Shapes.or(box(0,0,0,5,16,16),box(11,0,0,16,16,16),box(5,10,0,11,16,16));
        default->Shapes.block();};}
    @Override public VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return getShape(s,l,p,c);}
}
