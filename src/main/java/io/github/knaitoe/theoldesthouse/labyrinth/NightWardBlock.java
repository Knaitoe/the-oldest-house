package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import java.util.Locale;
import net.minecraft.core.*;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.*;

/** The night-duty ward's own finishes and enamel fittings. No story state lives in these blocks. */
public final class NightWardBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<NightWardBlock> CODEC=simpleCodec(NightWardBlock::new);
    public enum Kind implements StringRepresentable {
        WALL,DADO,FLOOR,CEILING,CURTAIN,CALL_PANEL,WINDOW,BED_HEAD,BED_FOOT,CHAIR,BEDSIDE,TABLE,STOOL,LIGHT,
        // 0.4.199, the clinical ward (NightWard): the station counter, a basin, the call column, unlit ceiling panels, bed-head
        // service panels, a monitor on a wheeled stand, skirting night lights, a trolley and a drip stand.
        COUNTER,SINK,COLUMN,LIGHT_OFF,BED_UNIT,MONITOR_STAND,MONITOR,NIGHT_LIGHT,TROLLEY,DRIP;
        public String getSerializedName(){return name().toLowerCase(Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public NightWardBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(KIND,Kind.WALL).setValue(FACING,Direction.NORTH));}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND,FACING);}
    public static BlockState of(Kind k){return of(k,Direction.NORTH);}
    public static BlockState of(Kind k,Direction d){return NovelRegistry.NIGHT_WARD.get().defaultBlockState().setValue(KIND,k).setValue(FACING,d);}
    public static double[][] boxes(Kind k){return switch(k){
        case BED_HEAD->InstituteFixtureBlock.boxes(InstituteFixtureBlock.Kind.BED_HEAD);
        case BED_FOOT->InstituteFixtureBlock.boxes(InstituteFixtureBlock.Kind.BED_FOOT);
        case CHAIR->InstituteFixtureBlock.boxes(InstituteFixtureBlock.Kind.CHAIR);
        case BEDSIDE->new double[][]{{1,3,1,15,16,15},{2,0,2,4,3,4},{12,0,2,14,3,4},{2,0,12,4,3,14},{12,0,12,14,3,14}};
        case TABLE->InstituteFixtureBlock.boxes(InstituteFixtureBlock.Kind.TABLE);
        case STOOL->InstituteFixtureBlock.boxes(InstituteFixtureBlock.Kind.STOOL);
        case LIGHT->new double[][]{{0,0,0,16,16,16}}; // keeps the authored sea lantern's support and light volume
        case COUNTER->InstituteFixtureBlock.boxes(InstituteFixtureBlock.Kind.COUNTER);
        case SINK->InstituteFixtureBlock.boxes(InstituteFixtureBlock.Kind.SINK);
        case BED_UNIT->new double[][]{{0,3,13,16,13,16},{5,9,9,11,12,13}};
        case MONITOR_STAND->new double[][]{{2,0,2,14,2,14},{7,2,7,9,16,9}};
        case MONITOR->new double[][]{{3,6,6,13,14,10},{7,0,7,9,7,9}};
        case NIGHT_LIGHT->new double[][]{{5,2,14,11,5,16}};
        case TROLLEY->new double[][]{{1,0,2,15,14,14}};
        case DRIP->new double[][]{{3,0,3,13,1,13},{7,1,7,9,16,9}};
        default->new double[][]{{0,0,0,16,16,16}};
    };}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var result=Shapes.empty();int turns=switch(s.getValue(FACING)){case EAST->1;case SOUTH->2;case WEST->3;default->0;};
        for(var b:boxes(s.getValue(KIND))){double x0=b[0],z0=b[2],x1=b[3],z1=b[5];for(int i=0;i<turns;i++){double a=16-z1,e=16-z0;z0=x0;z1=x1;x0=a;x1=e;}result=Shapes.or(result,Block.box(x0,b[1],z0,x1,b[4],z1));}
        return result;
    }
    private static boolean solid(Kind k){return k.ordinal()<=Kind.CALL_PANEL.ordinal()||k==Kind.COLUMN||k==Kind.LIGHT_OFF;}
    /** The lit panels, the skirting night lights and a monitor's screen give light; nothing else does. */
    public static int light(BlockState s){return switch(s.getValue(KIND)){case LIGHT->15;case NIGHT_LIGHT->6;case MONITOR->3;default->0;};}
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return solid(s.getValue(KIND))?Shapes.block():Shapes.empty();}
    @Override protected int getLightBlock(BlockState s,BlockGetter l,BlockPos p){return solid(s.getValue(KIND))?15:0;}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
