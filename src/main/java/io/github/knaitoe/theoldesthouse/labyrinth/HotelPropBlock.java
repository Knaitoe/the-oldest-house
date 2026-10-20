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
public final class HotelPropBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<HotelPropBlock> CODEC=simpleCodec(HotelPropBlock::new);
    public enum Kind implements StringRepresentable {PIANO,TYPEWRITER,BOILER,GAUGE,MEAL,RECEPTION,HOSE_RACK,PHOTO,HEADSTONE,PATCH;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}}
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty PRESSURE=IntegerProperty.create("pressure",0,3);
    public HotelPropBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.PIANO).setValue(PRESSURE,0));}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override public net.minecraft.world.level.block.entity.BlockEntity newBlockEntity(BlockPos pos,BlockState state){return state.getValue(KIND)==Kind.PHOTO?new HotelPhotoBlockEntity(pos,state):null;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,PRESSURE);}
    private static VoxelShape north(Kind kind){return switch(kind){
        case PIANO->Shapes.or(Block.box(0,12,0,30,16,16),Block.box(0,16,9,30,29,17),Block.box(0,14,-3,30,17,5),Block.box(1,0,1,3,12,3),Block.box(13,0,13,15,12,15));
        case TYPEWRITER->Shapes.or(Block.box(2,0,2,14,7,14),Block.box(4,5,11,12,16,11.4));case MEAL->Block.box(1,0,1,15,6,15);
        case BOILER->Shapes.or(Block.box(1,0,1,15,29,15),Block.box(5,29,5,11,32,11));
        case GAUGE->Block.box(2,2,13,14,14,16);case PHOTO->Block.box(0,0,12.8,32,32,16);
        case HOSE_RACK->Block.box(0,0,11,16,16,16);case PATCH->Shapes.block();case HEADSTONE->Block.box(3,0,5,13,20,11);
        case RECEPTION->Shapes.or(Shapes.block(),Block.box(10,16,4,14,20,8));
        };}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){var shape=north(s.getValue(KIND));int turns=(s.getValue(FACING).get2DDataValue()+2)%4;var out=new VoxelShape[]{Shapes.empty()};shape.forAllBoxes((x0,y0,z0,x1,y1,z1)->{for(int i=0;i<turns;i++){double nx0=1-z1,nx1=1-z0;z0=x0;z1=x1;x0=nx0;x1=nx1;}out[0]=Shapes.or(out[0],Shapes.box(x0,y0,z0,x1,y1,z1));});return out[0];}
    /** Rotate a real native hit back to the mesh's authored north orientation. */
    public static net.minecraft.world.phys.Vec3 localHit(BlockState s,net.minecraft.world.phys.Vec3 hit){int turns=(4-(s.getValue(FACING).get2DDataValue()+2)%4)%4;for(int i=0;i<turns;i++)hit=new net.minecraft.world.phys.Vec3(1-hit.z,hit.y,hit.x);return hit;}
}
