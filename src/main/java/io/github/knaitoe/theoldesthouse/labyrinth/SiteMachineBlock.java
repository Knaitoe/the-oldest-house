package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
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
 * The road crew's machines at the elk carcass site (0.4.120): the skid steer, bucket down, and the flatbed they hauled the
 * elk on. Each cell of a machine is one {@link Kind}, named machine_column_row_level, authored facing north with the
 * machine's front toward -z; its box is the box round that cell's pieces. tools/draw_site_machines.py draws the models
 * and confirms these boxes.
 */
public final class SiteMachineBlock extends Block {
    public static final MapCodec<SiteMachineBlock> CODEC=simpleCodec(SiteMachineBlock::new);
    public enum Machine {
        SKID(2,3,2),TRUCK(3,8,3);
        public final int columns,rows,levels;
        Machine(int columns,int rows,int levels){this.columns=columns;this.rows=rows;this.levels=levels;}
    }
    public enum Kind implements StringRepresentable {
        SKID_0_0_0(0,0,0,16,16,16),
        SKID_1_0_0(0,0,0,16,16,16),
        SKID_0_1_0(0,0,0,16,16,16),
        SKID_1_1_0(0,0,0,16,16,16),
        SKID_0_2_0(0,0,0,16,16,15),
        SKID_1_2_0(0,0,0,16,16,15),
        SKID_0_0_1(1,0,12,16,11,16),
        SKID_1_0_1(0,0,12,15,11,16),
        SKID_0_1_1(1,0,0,16,16,16),
        SKID_1_1_1(0,0,0,15,16,16),
        SKID_0_2_1(1,0,0,16,16,15),
        SKID_1_2_1(0,0,0,15,16,15),
        TRUCK_0_0_0(1,0,1,16,16,16),
        TRUCK_1_0_0(0,6,1,16,16,16),
        TRUCK_2_0_0(0,0,1,15,16,16),
        TRUCK_0_1_0(0,0,0,16,16,16),
        TRUCK_1_1_0(0,8,0,16,16,16),
        TRUCK_2_1_0(0,0,0,16,16,16),
        TRUCK_0_2_0(0,8,0,16,16,16),
        TRUCK_1_2_0(0,8,0,16,16,16),
        TRUCK_2_2_0(0,8,0,16,16,16),
        TRUCK_0_3_0(0,7,0,16,16,16),
        TRUCK_1_3_0(0,8,0,16,16,16),
        TRUCK_2_3_0(0,6,0,16,16,16),
        TRUCK_0_4_0(0,8,0,16,16,16),
        TRUCK_1_4_0(0,8,0,16,16,16),
        TRUCK_2_4_0(0,8,0,16,16,16),
        TRUCK_0_5_0(0,8,0,16,16,16),
        TRUCK_1_5_0(0,8,0,16,16,16),
        TRUCK_2_5_0(0,8,0,16,16,16),
        TRUCK_0_6_0(0,0,0,16,16,16),
        TRUCK_1_6_0(0,8,0,16,16,16),
        TRUCK_2_6_0(0,0,0,16,16,16),
        TRUCK_0_7_0(0,2,0,16,16,16),
        TRUCK_1_7_0(0,7,0,16,16,16),
        TRUCK_2_7_0(0,2,0,16,16,16),
        TRUCK_0_0_1(2,0,3,16,14,16),
        TRUCK_1_0_1(0,0,3,16,14,16),
        TRUCK_2_0_1(0,0,3,14,14,16),
        TRUCK_0_1_1(0,0,0,16,16,16),
        TRUCK_1_1_1(0,0,0,16,16,16),
        TRUCK_2_1_1(0,0,0,16,16,16),
        TRUCK_0_2_1(2,0,0,16,16,16),
        TRUCK_1_2_1(0,0,0,16,16,16),
        TRUCK_2_2_1(0,0,0,14,16,16),
        TRUCK_0_3_1(0,0,1,2,11,16),
        TRUCK_2_3_1(14,0,1,16,11,16),
        TRUCK_0_4_1(0,0,0,2,11,16),
        TRUCK_2_4_1(14,0,0,16,11,16),
        TRUCK_0_5_1(0,0,0,2,11,16),
        TRUCK_2_5_1(14,0,0,16,11,16),
        TRUCK_0_6_1(0,0,0,2,11,16),
        TRUCK_2_6_1(14,0,0,16,11,16),
        TRUCK_0_7_1(0,0,0,2,11,15),
        TRUCK_2_7_1(14,0,0,16,11,15),
        TRUCK_0_1_2(0,0,10,16,12,16),
        TRUCK_1_1_2(0,0,10,16,12,16),
        TRUCK_2_1_2(0,0,10,16,12,16),
        TRUCK_0_2_2(2,0,0,16,12,16),
        TRUCK_1_2_2(0,0,0,16,15,16),
        TRUCK_2_2_2(0,0,0,14,12,16);
        public final Machine machine;public final int column,row,level;private final VoxelShape box;
        Kind(int x0,int y0,int z0,int x1,int y1,int z1){
            var part=name().split("_");machine=Machine.valueOf(part[0]);column=Integer.parseInt(part[1]);row=Integer.parseInt(part[2]);level=Integer.parseInt(part[3]);
            box=Block.box(x0,y0,z0,x1,y1,z1);
        }
        public String getSerializedName(){return name().toLowerCase(Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
    private static final Map<Kind,VoxelShape[]> SHAPES=new EnumMap<>(Kind.class);
    public SiteMachineBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(KIND,Kind.SKID_0_0_0).setValue(FACING,Direction.NORTH));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND,FACING);}
    public static BlockState of(Kind kind,Direction facing){return LiteraryRegistry.SITE_MACHINE.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing);}

    /**
     * Each cell of a machine with its place: corner is the machine's front-left corner (the corner of its first cell
     * facing north), and the machine is turned about it to face {@code facing}.
     */
    public static void place(Machine machine,BlockPos corner,Direction facing,BiConsumer<BlockPos,BlockState> set){
        for(var k:Kind.values()){
            if(k.machine!=machine)continue;
            int c=k.column,r=k.row,x,z;
            switch(facing){
                case EAST->{x=-r-1;z=c;}
                case SOUTH->{x=-c-1;z=-r-1;}
                case WEST->{x=r;z=-c-1;}
                default->{x=c;z=r;}
            }
            set.accept(corner.offset(x,k.level,z),of(k,facing));
        }
    }

    private static VoxelShape rotated(VoxelShape shape,Direction facing){
        if(facing==Direction.NORTH)return shape;var out=new VoxelShape[]{Shapes.empty()};int turns=switch(facing){case EAST->1;case SOUTH->2;default->3;};
        shape.forAllBoxes((x0,y0,z0,x1,y1,z1)->{double a0=x0,b0=z0,a1=x1,b1=z1;for(int i=0;i<turns;i++){double na0=1-b1,nb0=a0,na1=1-b0,nb1=a1;a0=na0;b0=nb0;a1=na1;b1=nb1;}
            out[0]=Shapes.or(out[0],Shapes.box(a0,y0,b0,a1,y1,b1));});
        return out[0];
    }
    private static VoxelShape shape(BlockState s){
        var k=s.getValue(KIND);var all=SHAPES.computeIfAbsent(k,x->new VoxelShape[4]);int i=s.getValue(FACING).get2DDataValue();
        if(all[i]==null)all[i]=rotated(k.box,s.getValue(FACING));
        return all[i];
    }
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return shape(s);}
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return Shapes.empty();}
    @Override protected boolean propagatesSkylightDown(BlockState s,BlockGetter l,BlockPos p){return true;}
    @Override protected BlockState rotate(BlockState s,net.minecraft.world.level.block.Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,net.minecraft.world.level.block.Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
