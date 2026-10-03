package io.github.knaitoe.theoldesthouse.labyrinth;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;
/** Native furniture, with collision kept below its visible walking silhouette. */
public final class HotelPropBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<HotelPropBlock> CODEC=simpleCodec(HotelPropBlock::new);
    public enum Kind implements StringRepresentable {PIANO,TYPEWRITER,BOILER,GAUGE,MEAL,RECEPTION,HOSE_RACK,PHOTO,HEADSTONE,PATCH;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}}
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty PRESSURE=IntegerProperty.create("pressure",0,3);
    public HotelPropBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.PIANO).setValue(PRESSURE,0));}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,PRESSURE);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return switch(s.getValue(KIND)){
        case TYPEWRITER,MEAL->Block.box(2,0,2,14,7,14);case GAUGE,PHOTO->Block.box(0,0,13,16,16,16);
        case HOSE_RACK->Block.box(0,0,11,16,16,16);case PATCH->Shapes.block();case HEADSTONE->Block.box(3,0,5,13,20,11);
        default->Block.box(0,0,0,16,16,16);};}
}
