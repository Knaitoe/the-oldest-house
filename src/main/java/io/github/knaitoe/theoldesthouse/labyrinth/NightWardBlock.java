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
        WALL,DADO,FLOOR,CEILING,CURTAIN,CALL_PANEL,WINDOW,BED_HEAD,BED_FOOT,CHAIR,BEDSIDE,TABLE,STOOL,LIGHT;
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
        case BEDSIDE->InstituteFixtureBlock.boxes(InstituteFixtureBlock.Kind.BEDSIDE);
        case TABLE->InstituteFixtureBlock.boxes(InstituteFixtureBlock.Kind.TABLE);
        case STOOL->InstituteFixtureBlock.boxes(InstituteFixtureBlock.Kind.STOOL);
        case LIGHT->new double[][]{{0,0,0,16,16,16}}; // keeps the authored sea lantern's support and light volume
        default->new double[][]{{0,0,0,16,16,16}};
    };}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var result=Shapes.empty();int turns=switch(s.getValue(FACING)){case EAST->1;case SOUTH->2;case WEST->3;default->0;};
        for(var b:boxes(s.getValue(KIND))){double x0=b[0],z0=b[2],x1=b[3],z1=b[5];for(int i=0;i<turns;i++){double a=16-z1,e=16-z0;z0=x0;z1=x1;x0=a;x1=e;}result=Shapes.or(result,Block.box(x0,b[1],z0,x1,b[4],z1));}
        return result;
    }
    private static boolean solid(Kind k){return k.ordinal()<=Kind.CALL_PANEL.ordinal();}
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return solid(s.getValue(KIND))?Shapes.block():Shapes.empty();}
    @Override protected int getLightBlock(BlockState s,BlockGetter l,BlockPos p){return solid(s.getValue(KIND))?15:0;}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
}
