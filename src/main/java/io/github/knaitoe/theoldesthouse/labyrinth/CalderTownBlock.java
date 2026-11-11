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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 0.4.481, Bike night: the town's own fittings, each a kind of this one block (tools/draw_calder_town_art.py), as the park's are
 * kinds of {@link CalderBlock}. What a fitting faces is the way it looks; a thing fixed to a wall has the wall behind it.
 * <ul>
 * <li>CHAINLINK: one block of chain-link fence on the block's middle line. Stage 0 the lower block, 1 the upper; 2 and 3 the same
 * two with the wire cut and pulled back, which is a hole a child on a bike goes through.</li>
 * <li>BLEACHER: a bleacher's plank on its frame. The plank is all that is stood on; under a raised one there is room to ride.</li>
 * <li>PUMPKIN: a carved pumpkin, lit from inside. Its stage is its face.</li>
 * <li>AWNING: an awning over a shop's front. Its stage is its stripe.</li>
 * <li>HYDRANT, MAILBOX, CART: a fire hydrant, a post box, a grocery cart.</li>
 * <li>POLE: a barber's pole, which turns.</li>
 * <li>TP: toilet paper hung in a tree.</li>
 * </ul>
 * And what is behind the shops (the owner: "clutter and decor in alleys would benefit it too. space between buildings, dumpsters,
 * trash"):
 * <ul>
 * <li>DUMPSTER: half of a steel bin two blocks long, its back to a wall. Stage 0 is the half at the left hand of whoever stands with
 * their back to that wall, 1 the half at their right; 2 and 3 the same two with the lid thrown back.</li>
 * <li>BAGS: trash bags, one, two or three, or one torn open. BOXES: cardboard, one, a heap, flattened, or one with its flaps up.
 * TRASH: what lies on the ground (newspaper, cans, a burger's box and a cup, a puddle). A bike goes through all of these.</li>
 * <li>CRATE: a packing crate, a milk crate, a pallet flat, a pallet stood against the wall. DRUM: oil drums. TYRES. These are in
 * the way.</li>
 * <li>PIPE: a drainpipe on the wall behind it (a length, its foot, its head). UNIT: what is fixed to a back wall: a meter, an air
 * conditioner, a caged bulb (lit), a vent.</li>
 * <li>BOARDS: a board fence on the block's middle line, as chain-link is: stage 0 the lower block, 1 the upper; 2 and 3 the same
 * two with boards gone, which is a way through.</li>
 * </ul>
 */
public final class CalderTownBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<CalderTownBlock> CODEC=simpleCodec(CalderTownBlock::new);
    public enum Kind implements StringRepresentable {
        CHAINLINK,BLEACHER,PUMPKIN,AWNING,HYDRANT,MAILBOX,POLE,TP,CART,DUMPSTER,BAGS,CRATE,BOXES,DRUM,TYRES,TRASH,PIPE,UNIT,BOARDS;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,3);
    /** Chain-link's blocks, and a board fence's. */
    public static final int LOWER=0,UPPER=1,TORN=2,TORN_UPPER=3;
    /** A dumpster's lid thrown back (added to its half); what a crate, a heap of boxes or a wall's unit can be. */
    public static final int OPEN=2,MILK_CRATE=1,PALLET=2,PALLET_STOOD=3,FLAT=2,METER=0,COOLER=1,BULB=2,VENT=3,PUDDLE=3;
    public CalderTownBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.CHAINLINK).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing,int stage){return BikeNightRegistry.TOWN.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,Math.max(0,Math.min(3,stage)));}
    public static boolean is(BlockState s,Kind kind){return s.is(BikeNightRegistry.TOWN.get())&&s.getValue(KIND)==kind;}
    /** The light a fitting gives: a pumpkin's candle, a little from the pole. */
    static int light(BlockState s){return switch(s.getValue(KIND)){case PUMPKIN->10;case POLE->3;case UNIT->s.getValue(STAGE)==BULB?9:0;default->0;};}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);
        return switch(s.getValue(KIND)){
            case CHAINLINK->MarshBlock.turned(f,0,0,7,16,16,9);
            case BLEACHER->MarshBlock.turned(f,0,13,4,16,15,13);
            case PUMPKIN->Block.box(3,0,3,13,9,13);
            case AWNING->MarshBlock.turned(f,0,4,0,16,12,16);
            case HYDRANT->Block.box(5,0,5,11,12,11);
            case MAILBOX->MarshBlock.turned(f,3,0,4,13,14.6,12);
            case POLE->MarshBlock.turned(f,5.5,2,10,10.5,15,16);
            case TP->Block.box(2,0,2,14,16,14);
            case CART->MarshBlock.turned(f,3.5,0,2,12.5,13.4,15);
            case DUMPSTER->MarshBlock.turned(f,0,0,2.5,16,13,14.5);
            case BAGS->Block.box(1,0,2,15,8,14);
            case CRATE->switch(s.getValue(STAGE)){case MILK_CRATE->Block.box(3,0,3,13,8,13);case PALLET->Block.box(0,0,1,16,3,15);case PALLET_STOOD->MarshBlock.turned(f,0,0,9,16,16,16);default->Block.box(1,0,1,15,14,15);};
            case BOXES->s.getValue(STAGE)==FLAT?Block.box(0,0,0,16,1,16):Block.box(1,0,3,15,10,14);
            case DRUM->switch(s.getValue(STAGE)){case 2->Block.box(0,0,3,16,14,13);case 3->MarshBlock.turned(f,1,0,6,15,7,13);default->Block.box(4,0,4,12,14,12);};
            case TYRES->switch(s.getValue(STAGE)){case 1->Block.box(2.5,0,2.5,13.5,9,13.5);case 2->MarshBlock.turned(f,3,0,12.5,13,10,15.5);case 3->Block.box(0,0,4,16,3,13);default->Block.box(3,0,3,13,3,13);};
            case TRASH->Block.box(0,0,0,16,1,16);
            case PIPE->MarshBlock.turned(f,5.5,0,9,10.5,16,16);
            case UNIT->MarshBlock.turned(f,2,3,9,14,12,16);
            case BOARDS->MarshBlock.turned(f,0,0,7,16,16,9);
        };
    }
    /**
     * A fence stands half a block higher than it looks, whole; torn, or with its boards gone, it is not there. Awnings, the pole and
     * paper give way; so do bags, boxes and what lies on the ground; and what is on a wall is not in anybody's way.
     */
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        int stage=s.getValue(STAGE);
        return switch(s.getValue(KIND)){
            case CHAINLINK,BOARDS->stage>=TORN?Shapes.empty():MarshBlock.turned(s.getValue(FACING),0,0,7,16,stage==UPPER?24:16,9);
            case AWNING,POLE,TP,BAGS,BOXES,TRASH,PIPE,UNIT->Shapes.empty();
            default->getShape(s,l,p,c);
        };
    }
    @Override protected boolean isPathfindable(BlockState s,PathComputationType t){return false;}
}
