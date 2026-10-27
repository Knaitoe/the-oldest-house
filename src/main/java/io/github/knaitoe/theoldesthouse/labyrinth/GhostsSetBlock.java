package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
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

/** Domestic furniture and production equipment, with physical space beneath the dinner table. */
public final class GhostsSetBlock extends Block {
    public static final MapCodec<GhostsSetBlock> CODEC = simpleCodec(GhostsSetBlock::new);
    public enum Kind implements StringRepresentable {
        WALLPAPER, FLOOR, CEILING, TILE, PANEL, TABLE, CHAIR, COUNTER, SINK, COOKER,
        PLATE, HAT, BOOKS, JUG, BOLTS, CAMERA, CASE, MONITOR, DRAWING, LAMP, BED_HEAD, BED_FOOT,
        PLAIN_PLATE, PLAYHOUSE, TORN_CARD, DRAWER, DRAWER_RIG, PLASTER,
        MEAL_STEW, MEAL_SANDWICH, MEAL_SOUP, MEAL_ROAST, MEAL_POTATOES, LAMP_OFF, RUG, STAIR, RAIL, TABLE_LEG, STAIR_RAIL, STAIR_RAIL_TOP;
        @Override public String getSerializedName() { return name().toLowerCase(java.util.Locale.ROOT); }
        public boolean cube() { return ordinal() <= PANEL.ordinal(); }
    }
    public static final EnumProperty<Kind> KIND = EnumProperty.create("kind", Kind.class);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public GhostsSetBlock(Properties p) {
        super(p);
        registerDefaultState(stateDefinition.any().setValue(KIND, Kind.WALLPAPER).setValue(FACING, Direction.NORTH));
    }
    @Override protected MapCodec<? extends Block> codec() { return CODEC; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(KIND, FACING); }
    public static BlockState of(Kind k, Direction f) { return LiteraryRegistry.GHOST_SET.get().defaultBlockState().setValue(KIND,k).setValue(FACING,f); }
    private static VoxelShape north(Kind k) {
        return switch(k) {
            case STAIR -> Shapes.or(Block.box(0,0,0,16,8,16),Block.box(0,8,0,16,16,8));
            case RAIL -> Shapes.or(Block.box(6,0,1,10,16,3),Block.box(6,0,13,10,16,15),Block.box(5,14,0,11,16,16));
            case STAIR_RAIL,STAIR_RAIL_TOP -> stairRail(k==Kind.STAIR_RAIL_TOP);
            case TABLE -> Block.box(0,15,0,16,16,16);
            case TABLE_LEG -> Shapes.or(Block.box(0,15,0,16,16,16),Block.box(1,0,1,3,15,3));
            case CHAIR -> Shapes.or(Block.box(2,8,2,14,10,14), Block.box(2,0,2,4,8,4), Block.box(12,0,2,14,8,4), Block.box(2,0,12,4,8,14), Block.box(12,0,12,14,8,14), Block.box(2,4,2,14,6,4), Block.box(2,10,12,14,22,14));
            case BOLTS -> Shapes.or(Block.box(0,15,0,16,16,16),Block.box(4,9,6,12,13,9),Block.box(1,0,1,3,15,3));
            case PLATE, PLAIN_PLATE, MEAL_STEW, MEAL_SANDWICH, MEAL_SOUP, MEAL_ROAST, MEAL_POTATOES, HAT, BOOKS -> Block.box(3,0,3,13,3,13);
            case JUG -> Block.box(5,0,5,11,9,11);
            case DRAWING -> Block.box(2,2,15,14,14,16);
            case LAMP,LAMP_OFF -> Shapes.or(Block.box(3,6,3,13,12,13),Block.box(7,12,7,9,16,9));
            case RUG -> Block.box(0,0,0,16,1,16);
            case BED_HEAD, BED_FOOT -> Shapes.or(Block.box(0,6,0,16,12,16), Block.box(1,0,1,3,6,3), Block.box(13,0,13,15,6,15));
            default -> Shapes.block();
        };
    }
    private static VoxelShape stairRail(boolean top){
        var shape=Shapes.or(Block.box(6,-8,13,10,11,15),Block.box(6,-8,12,18,-6,16));
        for(int z=top?9:0;z<16;z++)shape=Shapes.or(shape,Block.box(5,22.1-z,z,11,24.9-z,z+1));
        return top?Shapes.or(shape,Block.box(5,14,-16,11,16,9)):shape;
    }
    private static VoxelShape shape(BlockState s) {
        var original=north(s.getValue(KIND)); int turns=(s.getValue(FACING).get2DDataValue()+2)%4;
        var out=new VoxelShape[]{Shapes.empty()};
        original.forAllBoxes((x0,y0,z0,x1,y1,z1)->{
            for(int i=0;i<turns;i++){double nx0=1-z1,nz0=x0,nx1=1-z0,nz1=x1; x0=nx0;z0=nz0;x1=nx1;z1=nz1;}
            out[0]=Shapes.or(out[0],Shapes.box(x0,y0,z0,x1,y1,z1));
        }); return out[0];
    }
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c) { return shape(s); }
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c) {
        return switch(s.getValue(KIND)){case DRAWING,PLATE,PLAIN_PLATE,MEAL_STEW,MEAL_SANDWICH,MEAL_SOUP,MEAL_ROAST,MEAL_POTATOES,HAT,BOOKS,RUG->Shapes.empty();default->shape(s);};
    }
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p) { return s.getValue(KIND).cube()?Shapes.block():Shapes.empty(); }
    public static int light(BlockState s) { return s.getValue(KIND)==Kind.LAMP?12:0; }
    public static Kind meal(int day){return switch(day){case 1->Kind.MEAL_STEW;case 3->Kind.MEAL_SANDWICH;case 4->Kind.MEAL_SOUP;case 5->Kind.MEAL_ROAST;case 6->Kind.MEAL_POTATOES;default->Kind.PLATE;};}
    @Override protected BlockState rotate(BlockState s,net.minecraft.world.level.block.Rotation r) { return s.setValue(FACING,r.rotate(s.getValue(FACING))); }
    @Override protected BlockState mirror(BlockState s,net.minecraft.world.level.block.Mirror m) { return s.rotate(m.getRotation(s.getValue(FACING))); }
}
