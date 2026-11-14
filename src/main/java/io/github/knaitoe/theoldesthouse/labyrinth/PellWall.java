package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * 0.4.573, the Pell house's walls, room by room (docs/PELL_HOUSE_NOTES_0_4_573.md; the owner: "the whole pell house is too uniform
 * in its walls and carpets"). One cell of a wall, as thick as the cell. Every wall in the house had the same plaster on every face;
 * a wall now has the finish of the room each of its faces looks into.
 * <ul>
 * <li>FRONT is the finish on the face the cell turns to FACING; BACK the finish on the face behind it. A wall between two rooms has
 * one room's on each. An outside wall has brick to the weather (FRONT, FACING outward) and the room's finish inside (BACK).</li>
 * <li>LEVEL is how high the cell stands in its wall, and every finish is drawn floor to ceiling in three cells: 0 with the
 * skirting at its foot, 1 the middle, 2 with the cornice at its head. Brick's lowest course is its level 0.</li>
 * <li>The two faces at the cell's ends are a door's jamb or a window's reveal: painted timber (the same on the faces above and
 * below, a door's head, a window's sill). WRAP turns them to the FRONT's finish instead: the end of a wall that a room goes
 * round, as the hall goes round into the entry and through its two arches.</li>
 * <li>Daylight. The outside walls were cells of the house's fitting, which let the day through them a little dimmer, and every
 * room was lit by its walls as much as by its windows. A cell with brick on its front still does (getLightBlock), so no room
 * is darker for the change; a wall between two rooms stops light, as the plaster did.</li>
 * </ul>
 * Drawn by tools/draw_pell_house.py, which checks that its finishes are these.
 */
public final class PellWall extends HorizontalDirectionalBlock {
    public static final MapCodec<PellWall> CODEC=simpleCodec(PellWall::new);
    public enum Finish implements StringRepresentable {
        /** The hall and the entry: cream paint gone a little yellow, scuffed low down. */
        HALL,
        /** Nessa's room: a child's paper in stripes, under the border of small animals. */
        NESSA,
        /** Callum's room: flat white rolled over everything, the skirting and the cornice with it. */
        CALLUM,
        /** The bathroom: tiles to head height under a capping tile, paint above. */
        BATH,
        /** June's room: a rose paper with a trellis in it. */
        JUNE,
        /** The kitchen: a pale yellow gloss. SPLASH: the tiles over its benches. */
        KITCHEN,SPLASH,
        /** The living room: an oatmeal paper with a gold diamond in it, a stained skirting. */
        LIVING,
        /** The laundry: painted sheet with cover strips. */
        LAUNDRY,
        /** The brick veneer, its lowest course at level 0; and the newer brick of the window bricked up. */
        BRICK,BRICK_NEW;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Finish> FRONT=EnumProperty.create("front",Finish.class),BACK=EnumProperty.create("back",Finish.class);
    public static final IntegerProperty LEVEL=IntegerProperty.create("level",0,2);
    public static final BooleanProperty WRAP=BooleanProperty.create("wrap");
    public PellWall(Properties p){
        super(p);
        registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(FRONT,Finish.HALL).setValue(BACK,Finish.HALL).setValue(LEVEL,1).setValue(WRAP,false));
    }
    public static BlockState of(Direction facing,Finish front,Finish back,int level,boolean wrap){
        return PellRegistry.WALL.get().defaultBlockState().setValue(FACING,facing).setValue(FRONT,front).setValue(BACK,back).setValue(LEVEL,Math.max(0,Math.min(2,level))).setValue(WRAP,wrap);
    }
    public static boolean is(BlockState s){return s.is(PellRegistry.WALL.get());}
    /** What a cell of wall shows on one of its four upright faces: a finish, or null where the face is painted timber (a jamb). */
    public static @Nullable Finish finish(BlockState s,Direction face){
        if(!is(s)||face.getAxis().isVertical())return null;
        var f=s.getValue(FACING);
        if(face==f)return s.getValue(FRONT);
        if(face==f.getOpposite())return s.getValue(BACK);
        return s.getValue(WRAP)?s.getValue(FRONT):null;
    }
    private static boolean brick(BlockState s){var f=s.getValue(FRONT);return f==Finish.BRICK||f==Finish.BRICK_NEW;}
    @Override protected int getLightBlock(BlockState s,BlockGetter l,BlockPos p){return brick(s)?1:super.getLightBlock(s,l,p);}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,FRONT,BACK,LEVEL,WRAP);}
}
