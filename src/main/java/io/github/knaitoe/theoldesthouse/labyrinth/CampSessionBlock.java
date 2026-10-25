package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Camp Blood's last session (0.4.121): the camp radio that reads the PA, the horn on its pole, a dropped flashlight, the
 * mess hall's tables and benches, the director's clipboard, the schedule on its stand, a counselor's journal on a crate and
 * a plank with initials cut into it. tools/draw_camp_session.py draws the models, authored facing north.
 */
public final class CampSessionBlock extends Block {
    public static final MapCodec<CampSessionBlock> CODEC=simpleCodec(CampSessionBlock::new);
    public enum Kind implements StringRepresentable {
        RADIO,PA_HORN,FLASHLIGHT,MESS_TABLE,BENCH,CLIPBOARD,SCHEDULE_STAND,JOURNAL,INITIALS;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
    private static final Map<Kind,VoxelShape[]> SHAPES=new EnumMap<>(Kind.class);
    public CampSessionBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(KIND,Kind.MESS_TABLE).setValue(FACING,Direction.NORTH));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND,FACING);}
    public static BlockState of(Kind kind,Direction facing){return LiteraryRegistry.CAMP_SESSION.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing);}
    public static boolean is(BlockState s,Kind kind){return s.is(LiteraryRegistry.CAMP_SESSION.get())&&s.getValue(KIND)==kind;}

    private static VoxelShape base(Kind k){return switch(k){
        case RADIO->Shapes.or(Block.box(1,0,1,15,8,15),Block.box(3,8,5,13,15.5,12));
        case PA_HORN->Shapes.or(Block.box(7,0,7,9,11,9),Block.box(4,7.5,1,12,15.5,11));
        case FLASHLIGHT->Block.box(3,0,6.5,13.5,3,10);
        case MESS_TABLE->Block.box(0,0,.5,16,15,15.5);
        case BENCH->Block.box(0,0,4,16,9,12);
        case CLIPBOARD->Block.box(4,0,3,12,.5,13);
        case SCHEDULE_STAND->Block.box(0,0,6,16,16,10);
        case JOURNAL->Block.box(2,0,2,14,11.5,14);
        case INITIALS->Block.box(0,0,15.5,16,16,16);
    };}
    private static VoxelShape rotated(VoxelShape shape,Direction facing){
        if(facing==Direction.NORTH)return shape;var out=new VoxelShape[]{Shapes.empty()};int turns=facing.get2DDataValue()==0?2:facing.get2DDataValue()==1?3:facing.get2DDataValue()==3?1:0;
        shape.forAllBoxes((x0,y0,z0,x1,y1,z1)->{double a0=x0,b0=z0,a1=x1,b1=z1;for(int i=0;i<turns;i++){double na0=1-b1,nb0=a0,na1=1-b0,nb1=a1;a0=na0;b0=nb0;a1=na1;b1=nb1;}
            out[0]=Shapes.or(out[0],Shapes.box(a0,y0,b0,a1,y1,b1));});
        return out[0];
    }
    private static VoxelShape shape(BlockState s){
        var all=SHAPES.computeIfAbsent(s.getValue(KIND),x->new VoxelShape[4]);int i=s.getValue(FACING).get2DDataValue();
        if(all[i]==null)all[i]=rotated(base(s.getValue(KIND)),s.getValue(FACING));
        return all[i];
    }
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return shape(s);}
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        return switch(s.getValue(KIND)){case FLASHLIGHT,CLIPBOARD,INITIALS->Shapes.empty();default->shape(s);};
    }
    @Override protected boolean propagatesSkylightDown(BlockState s,BlockGetter l,BlockPos p){return true;}
}
