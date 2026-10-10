package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.*;

/** Ward furniture and finishes, with native collision matching every visible cube. */
public final class InstituteFixtureBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<InstituteFixtureBlock> CODEC=simpleCodec(InstituteFixtureBlock::new);
    public enum Kind implements StringRepresentable {
        PAINT,PEEL,DAMP,IVORY,DADO,FLOOR_IVORY,FLOOR_GRAY,CEILING,
        CHAIR,STOOL,BEDSIDE,DESK,TABLE,COUNTER,BENCH,SINK,LIGHT,BED_HEAD,BED_FOOT,SHELF;
        public String getSerializedName(){return name().toLowerCase(Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    private static final Map<Kind,Map<Direction,VoxelShape>> SHAPES=new EnumMap<>(Kind.class);
    static {
        for(var k:Kind.values()) {
            var facings=new EnumMap<Direction,VoxelShape>(Direction.class);
            for(var d:Direction.Plane.HORIZONTAL) {
                var shape=Shapes.empty();int turns=switch(d){case EAST->1;case SOUTH->2;case WEST->3;default->0;};
                for(var box:boxes(k)) {
                    double x0=box[0],z0=box[2],x1=box[3],z1=box[5];
                    for(int t=0;t<turns;t++){double nx0=16-z1,nx1=16-z0;z0=x0;z1=x1;x0=nx0;x1=nx1;}
                    shape=Shapes.or(shape,Block.box(x0,box[1],z0,x1,box[4],z1));
                }
                facings.put(d,shape.optimize());
            }
            SHAPES.put(k,facings);
        }
    }
    /** Dimensions also consumed by the native mesh proof. North-facing chairs look north. */
    public static double[][] boxes(Kind k){return switch(k){
        case CHAIR -> new double[][]{{2,7,2,14,9,14},{2,0,2,4,7,4},{12,0,2,14,7,4},{2,0,12,4,16,14},{12,0,12,14,16,14},{3,10,12,13,16,14}};
        case STOOL -> new double[][]{{2,7,2,14,9,14},{3,0,3,5,7,5},{11,0,3,13,7,5},{3,0,11,5,7,13},{11,0,11,13,7,13}};
        case BEDSIDE -> new double[][]{{1,3,1,15,15,15},{2,0,2,4,3,4},{12,0,2,14,3,4},{2,0,12,4,3,14},{12,0,12,14,3,14}};
        case DESK -> new double[][]{{0,14,0,16,16,16},{1,0,2,6,14,14},{12,0,2,14,14,4},{12,0,12,14,14,14},{6,11,12,14,14,14}};
        case TABLE -> new double[][]{{0,14,0,16,16,16},{2,0,2,4,14,4},{12,0,2,14,14,4},{2,0,12,4,14,14},{12,0,12,14,14,14}};
        case COUNTER -> new double[][]{{0,0,0,16,14,16},{0,14,0,16,16,16}};
        case BENCH -> new double[][]{{0,7,2,16,9,14},{1,0,3,3,7,5},{13,0,3,15,7,5},{1,0,11,3,7,13},{13,0,11,15,7,13},{0,10,13,16,16,15}};
        case SINK -> new double[][]{{2,0,4,14,10,14},{1,10,2,15,13,15},{2,13,3,4,15,14},{12,13,3,14,15,14},{4,13,3,12,15,5},{4,13,12,12,15,14},{7,15,11,9,16,14}};
        case LIGHT -> new double[][]{{1,14,2,15,16,14},{2,13,3,14,14,13}};
        case BED_HEAD -> new double[][]{{1,3,0,15,5,16},{1,5,0,15,9,16},{2,9,0,14,11,5},{1,0,1,3,15,3},{13,0,1,15,15,3},{3,12,1,13,14,3}};
        case BED_FOOT -> new double[][]{{1,3,0,15,5,16},{1,5,0,15,9,16},{1,0,13,3,12,15},{13,0,13,15,12,15},{3,10,13,13,12,15}};
        default -> new double[][]{{0,0,0,16,16,16}};
    };}
    public InstituteFixtureBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.PAINT));}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return SHAPES.get(s.getValue(KIND)).get(s.getValue(FACING));}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
    public static BlockState of(Kind k,Direction d){return NovelRegistry.WARD_FIXTURE.get().defaultBlockState().setValue(KIND,k).setValue(FACING,d);}
    public static BlockState of(Kind k){return of(k,Direction.NORTH);}
}
