package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 0.4.549, The Ones Already Inside: a cell of wall between two of the lodging house's rooms, each face papered as its own room is.
 * PAIR names the front and the back: the front (and the two sides, which a corner shows) faces FACING and the back the other way,
 * so HALL_PARLOR facing east is the hall's paper to the east and the parlor's to the west. LATH is the passage side, bare laths.
 * PART: LOW is the lowest course, with the skirting board along its foot; MID any other; HATCH a hatch in the wall, shut, with its
 * slide on the front; HATCH_OPEN the same slid back, a hole through to the far side. Drawn by tools/draw_lodging_house.py.
 */
public final class LodgingWall extends HorizontalDirectionalBlock {
    public static final MapCodec<LodgingWall> CODEC=simpleCodec(LodgingWall::new);
    public enum Pair implements StringRepresentable {
        HALL_PARLOR,HALL_DINING,HALL_DANIEL,HALL_SCULLERY,HALL_DISTEMPER,HALL_LATH,PARLOR_LATH,DINING_LATH,DANIEL_LATH,DANIEL_DISTEMPER,DANIEL_SCULLERY,
        SCULLERY_DISTEMPER,SCULLERY_LATH,HALL_ROOM,ROOM_LATH,ROOM_WORK,ROOM_VERA,HALL_WORK,HALL_VERA,WORK_LATH,VERA_LATH;
        public String getSerializedName(){return name().toLowerCase(Locale.ROOT);}
        public String front(){return getSerializedName().split("_")[0];}
        public String back(){return getSerializedName().split("_")[1];}
    }
    public enum Part implements StringRepresentable {
        LOW,MID,HATCH,HATCH_OPEN;
        public String getSerializedName(){return name().toLowerCase(Locale.ROOT);}
    }
    public static final EnumProperty<Pair> PAIR=EnumProperty.create("pair",Pair.class);
    public static final EnumProperty<Part> PART=EnumProperty.create("part",Part.class);
    public LodgingWall(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(PAIR,Pair.HALL_LATH).setValue(PART,Part.MID));}
    public static BlockState of(Pair pair,Direction front,Part part){return LodgingRegistry.WALL.get().defaultBlockState().setValue(PAIR,pair).setValue(FACING,front).setValue(PART,part);}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,PAIR,PART);}
    /** An open hatch hides nothing behind it, and lets a little light through. */
    @Override protected VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return s.getValue(PART)==Part.HATCH_OPEN?Shapes.empty():Shapes.block();}
}
