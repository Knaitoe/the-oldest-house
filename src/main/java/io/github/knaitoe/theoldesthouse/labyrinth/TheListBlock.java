package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;

/**
 * 0.4.699, The list (docs/THE_LIST_0_4_699.md): Lamesa's things, drawn facing north by tools/draw_the_list_art.py.
 * <ul>
 * <li>The ground. COTTON: a plant with its bolls open (0), a short one (1), one leaning over the row's end (2), bare stalks (3). MESQUITE:
 * a trunk (0), a fork where it splits (1), a canopy (2), a thin canopy at its edge (3), a low branch out to one side (4). WEED: a
 * tumbleweed (0), broomweed (1), a tuft of dry grass (2), a mat of goatheads (3), a prickly pear (4).</li>
 * <li>The streets. POLE: a utility pole (0), its top with the crossarm (1), its wires high (2), lower (3) and lowest (4), the street
 * light's arm with its lamp (5), a transformer can (6). FENCE: chain-link (0), a post (1), a gate (2), a gate swung open (3, 0.4.701). SIGNPOST: the corner's blades for
 * South Ninth and Avenue K (0), Main and Avenue K (1), South Seventh and Avenue K (2); a stop sign (3), the bus stop's sign (4), the
 * railroad's crossbuck (5). TRACK: rails on their ties. MAILBOX: a mailbox on its post.</li>
 * <li>The reader's house. WINDOW: curtained and dark (0), lit (1), the kitchen's with its curtain half across (2), a screened one, dark
 * (3), the picture window's left (4) and right (5) halves. KITCHEN: a base cabinet (0), the sink (1), the range (2), the refrigerator's
 * lower (3) and upper (4) half, a wall cabinet (5), the telephone on the wall (6), the counter with the canisters and the toaster (7).
 * TABLE: the dinette table (0), a chair (1). BED: its head (0) and foot (1). ROOM: a dresser (0), the desk with the tape deck (1), the
 * nightstand with the clock radio (2), the closet's folding doors (3), a low bookcase (4). POSTER: a band (0), a film (1), the feed
 * store's calendar (2), a pennant (3), a photograph in a frame (4), a picture of somewhere else (5). DEN: the couch's left (0) and
 * right (1) half, the recliner (2), the console television (3), an end table with its lamp (4), the coffee table (5). BATH: the tub's
 * head (0) and foot (1), the toilet (2), the vanity (3), the medicine cabinet's mirror (4). ROOFTOP: the swamp cooler (0), its duct
 * (1), the aerial (2), a vent stack (3). YARD: the porch light (0), a clothesline's post (1) and its lines (2), the burn barrel (3), the
 * faucet and hose (4), the stoop (5), a carport post (6), the carport's roof (7). SHOP: the roll-up door's lower (0) and upper (1) half,
 * a shop window (2), the light over the door (3). FIXTURE: the kitchen's ceiling light (0), the hall light (1), a store's strip light
 * (2), a bare bulb (3), a floodlight (4).</li>
 * <li>Main Street. FRONT: a display window's lower half, lit inside (0), its transom (1), an awning (2), its valance (3), a dark display
 * window (4), a brick pilaster (5). SIGN_DRUG and SIGN_FEED: the four boards of the drugstore's and the feed store's names; SIGN_VIDEO the
 * five of the video store's. SIGN_LAW: the substation's star and its word across four boards (0-3). SIGN_DRIVEIN: the drive-in's pole (0),
 * its board in six pieces, three across and two down (1-6), its arrow (7). DRUG: a double shelf of goods (0), its end (1), the counter (2) and its register (3),
 * the pharmacist's shelves (4), a fountain stool (5), the magazine rack (6), the card spinner (7). VIDEO: the horror wall (0), new
 * releases (1), a double shelf (2), the counter (3) and its register (4), the return slot (5), a cardboard standee (6), the television
 * on its bracket (7). LAW: a desk (0), the desk with the radio (1), a filing cabinet (2), a bench (3), the flagpole (4) and its flag
 * (5), the bulletin board (6), the water cooler (7). FEED: feed sacks stacked (0), a low pile of them (1), mineral blocks (2), the
 * counter (3), a seed bin (4), the bus bench's left (5) and right (6) half, the dock's bumper (7). DRIVEIN: the stalls' canopy (0),
 * the canopy with a light (1), a canopy post (2), a stall's menu and speaker (3), the order window (4), the fryer (5), the flat-top (6),
 * a picnic table (7).</li>
 * <li>The gin, the seed house and the tower. GIN: the module feeder's lower (0) and upper (1) half, a gin stand (2), a lint cleaner (3),
 * the press's lower (4) and upper (5) half, an auger in its trough (6), the suction pipe (7). BALE: a wrapped bale (0), a bale with its
 * tag (1), a module under its tarp (2), a module's end (3). SEED: a bin's wall (0), a pile of seed (1), the auger in its trench (2), a
 * ladder (3), the sliding door's lower (4) and upper (5) half, the lamp over the door (6), the chute (7). TOWER: a leg (0), a leg with
 * its braces (1), the ladder (2), the catwalk's grating (3), its rail (4), the beacon (5), the riser (6). TANK: the tank's wall (0), its
 * letters (1-3), its roof (4), its cap (5), its bowl (6).</li>
 * <li>0.4.700, the school's field (docs/THE_LIST_0_4_700.md). FIELD: a goalpost's padded foot (0), its pole (1), the gooseneck bending
 * over the end line (2), the crossbar's middle (3) and an end of it with its upright (4), an upright (5), a light tower's pole (6), its
 * bank of floodlights, dark (7). STANDS: a row's plank at seat height (0) or a step higher (1), the frame under the stands (2), the rail
 * along the top (3), a team bench (4), the press box's window (5), its table with the microphone (6), the horn on its roof (7). LOCKER:
 * a locker's lower (0) and upper (1) half, a bench (2), the coach's desk (3), the chalkboard (4), a shower head (5), the concession
 * stand's shutter (6) and its prices (7). SCORE: the scoreboard in six boards, three across and two down (0-5), its post (6), the ticket
 * booth's window (7).</li>
 * <li>Amber's house. AMBER: the television with the VCR under it (0), the shelf of rented tapes (1), tapes stacked on the carpet (2),
 * her desk at the window with the notebook and the lamp (3), the nightstand with the phone (4), a bookcase of paperbacks (5), her
 * window, lit (6), the coffee table with the remote and two tapes (7). LOT, the neighbors' yards: a sprinkler (0), a doghouse (1), a
 * trash can (2), a bike on its side (3), a lawn chair (4), a grill (5), a hose on its reel (6), a kiddie pool (7).</li>
 * <li>0.4.701, the pasture (docs/THE_LIST_0_4_701.md). PASTURE: a cedar post with the strands across it (0), barbed wire between posts
 * (1), the wire gap shut (2) and laid back open (3), a cattle guard set into the road (4), a corner post braced to the next (5), a
 * mineral block in its tub (6), a hay ring round the last of a bale (7). STOCK, the stock tank: its wall (0), a corner of it (1), the
 * seam where it turns (2), the wall with the windmill's pipe over it (3), the pipe along the ground (4). WINDMILL: the tower in four
 * sections from the ground up (0-3), the head (4, drawn by the machine block), the wheel (5, drawn turning by its renderer), a footing
 * (6), the tail (7). PUMPJACK: the wellhead (0), the Samson post (1), the walking beam's front with the horsehead (2) and its back with
 * the equalizer (3), the cranks and weights (4), the pitman arms (5), the rod and carrier (6), the gearbox and motor on the skid (7);
 * 2-6 are drawn moving by the machine's renderer and never stand as blocks. BATTERY: a tank's lower (0) and upper (1) half, the
 * separator (2), the tank's girth (3, drawn by the tank), the lease's sign (4), the wells' plates (5-7).</li>
 * </ul>
 */
public final class TheListBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<TheListBlock> CODEC=simpleCodec(TheListBlock::new);
    public enum Kind implements StringRepresentable {
        COTTON,MESQUITE,WEED,POLE,FENCE,SIGNPOST,TRACK,MAILBOX,
        WINDOW,KITCHEN,TABLE,BED,ROOM,POSTER,DEN,BATH,ROOFTOP,YARD,SHOP,FIXTURE,
        FRONT,SIGN_DRUG,SIGN_FEED,SIGN_VIDEO,SIGN_LAW,SIGN_DRIVEIN,DRUG,VIDEO,LAW,FEED,DRIVEIN,
        GIN,BALE,SEED,TOWER,TANK,
        FIELD,STANDS,LOCKER,SCORE,AMBER,LOT,
        PASTURE,STOCK,WINDMILL,PUMPJACK,BATTERY;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,7);
    public TheListBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.COTTON).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return TheListRegistry.FITTING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,Math.max(0,Math.min(7,stage)));}
    public static boolean is(BlockState s){return s.is(TheListRegistry.FITTING.get());}
    public static boolean is(BlockState s,Kind kind){return is(s)&&s.getValue(KIND)==kind;}
    /** The lights of a night in town: the street lights, lit windows and fixtures, the signs that are on, the drive-in's canopy and window,
     * the porch light, the lamp over the seed house's door, the tower's beacon, and the screens that are playing. */
    static int light(BlockState s){
        int stage=s.getValue(STAGE);
        return switch(s.getValue(KIND)){
            case POLE->stage==5?15:0;
            case WINDOW->stage==1||stage==2||stage==4||stage==5?6:0;
            case FIXTURE->stage==2||stage==4?15:13;
            case SIGN_VIDEO->10;case SIGN_DRIVEIN->stage>=1?12:0;
            case DRIVEIN->stage==1?14:stage==4?10:0;
            case YARD->stage==0?12:0;
            case SEED->stage==6?12:0;
            case TOWER->stage==5?9:0;
            case DEN->stage==3?4:stage==4?12:0;
            case ROOM->stage==2?2:0;
            case VIDEO->stage==7?5:0;
            case FRONT->stage==0?8:0;
            case AMBER->stage==0?6:stage==3?12:stage==6?8:0;
            case SHOP->stage==3?12:0;
            default->0;
        };
    }
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    private static VoxelShape t(Direction f,double x0,double y0,double z0,double x1,double y1,double z1){return MarshBlock.turned(f,x0,y0,z0,x1,y1,z1);}
    /** The two ladders, the seed house's and the tower's, are climbed. */
    @Override public boolean isLadder(BlockState s,LevelReader l,BlockPos p,LivingEntity e){var k=s.getValue(KIND);int stage=s.getValue(STAGE);return k==Kind.SEED&&stage==3||k==Kind.TOWER&&stage==2;}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);int stage=s.getValue(STAGE);
        return switch(s.getValue(KIND)){
            case COTTON->Block.box(2,0,2,14,stage==3?11:stage==1?10:14,14);
            case MESQUITE->switch(stage){case 0->Block.box(5,0,5,11,16,11);case 1->Block.box(3,0,3,13,16,13);case 4->t(f,0,4,5,11,12,11);default->Shapes.block();};
            case WEED->switch(stage){case 0->Block.box(2,0,2,14,11,14);case 3->Block.box(1,0,1,15,1,15);case 4->Block.box(3,0,3,13,13,13);default->Block.box(3,0,3,13,9,13);};
            case POLE->switch(stage){case 0->Block.box(5.5,0,5.5,10.5,16,10.5);case 1->t(f,0,6,6,16,16,10);case 5->t(f,4,4,0,12,12,16);case 6->Block.box(4,0,4,12,14,12);default->t(f,0,10,6,16,16,10);};
            case FENCE->stage==1?Block.box(6.5,0,6.5,9.5,16,9.5):stage==3?t(f,0,0,0,1.5,14,9):t(f,0,0,7,16,16,9);
            case SIGNPOST->stage==5?t(f,0,0,7,16,16,9):Block.box(7,0,7,9,16,9);
            case TRACK->Block.box(0,0,0,16,3,16);
            case MAILBOX->Block.box(5,0,4,11,16,12);
            case WINDOW->t(f,0,0,0,16,16,2);
            case KITCHEN->switch(stage){case 3,4->t(f,1,0,1,15,16,16);case 5->t(f,0,0,9,16,16,16);case 6->t(f,5,3,13,11,15,16);default->Shapes.block();};
            case TABLE->stage==1?Block.box(3,0,3,13,15,13):Block.box(0,0,0,16,13,16);
            case BED->Block.box(0,0,0,16,9,16);
            case ROOM->switch(stage){case 0->t(f,0,0,4,16,14,16);case 1->t(f,0,0,3,16,15,16);case 2->Block.box(2,0,2,14,12,14);case 3->t(f,0,0,13,16,16,16);default->t(f,0,0,7,16,11,16);};
            case POSTER->t(f,1,1,15,15,15,16);
            case DEN->switch(stage){case 0,1->t(f,0,0,2,16,13,15);case 2->Block.box(1,0,1,15,15,15);case 3->t(f,1,0,3,15,14,16);case 4->Block.box(2,0,2,14,15,14);default->t(f,1,0,3,15,7,13);};
            case BATH->switch(stage){case 2->t(f,3,0,2,13,15,16);case 3->t(f,0,0,4,16,15,16);case 4->t(f,3,2,14,13,14,16);default->Block.box(0,0,0,16,9,16);};
            case ROOFTOP->switch(stage){case 0->Shapes.block();case 1->Block.box(4,0,4,12,16,12);case 2->Block.box(7,0,7,9,16,9);default->Block.box(6,0,6,10,10,10);};
            case YARD->switch(stage){case 0->t(f,5,5,12,11,13,16);case 1->Block.box(6.5,0,6.5,9.5,16,9.5);case 2->t(f,0,11,7,16,16,9);case 3->Block.box(3,0,3,13,14,13);case 4->t(f,3,0,11,13,9,16);case 5->Block.box(0,0,0,16,4,16);case 6->Block.box(6,0,6,10,16,10);default->Block.box(0,13,0,16,16,16);};
            case SHOP->stage==3?t(f,4,6,10,12,14,16):Shapes.block();
            case FIXTURE->switch(stage){case 2->Block.box(1,13,5,15,16,11);case 4->t(f,3,4,8,13,12,16);default->Block.box(4,12,4,12,16,12);};
            case FRONT->switch(stage){case 2->t(f,0,8,0,16,16,16);case 3->t(f,0,9,0,16,16,2);case 5->Shapes.block();default->t(f,0,0,0,16,16,2);};
            case SIGN_DRUG,SIGN_FEED,SIGN_VIDEO,SIGN_LAW->t(f,0,0,13,16,16,16);
            case SIGN_DRIVEIN->stage==0?Block.box(5,0,5,11,16,11):stage==7?t(f,0,2,7,16,14,9):t(f,0,0,6,16,16,10);
            case DRUG->switch(stage){case 5->Block.box(4,0,4,12,12,12);case 6,7->Block.box(3,0,3,13,16,13);case 4->t(f,0,0,6,16,16,16);default->Shapes.block();};
            case VIDEO->switch(stage){case 0,1->t(f,0,0,6,16,16,16);case 5->t(f,3,2,12,13,12,16);case 6->t(f,2,0,7,14,16,9);case 7->t(f,2,4,4,14,14,16);default->Shapes.block();};
            case LAW->switch(stage){case 3->t(f,0,0,3,16,8,13);case 4->Block.box(7,0,7,9,16,9);case 5->t(f,8,4,7,16,14,9);case 6->t(f,1,2,14,15,14,16);case 7->Block.box(4,0,4,12,16,12);default->Shapes.block();};
            case FEED->switch(stage){case 1->Block.box(0,0,0,16,8,16);case 2->Block.box(1,0,1,15,10,15);case 5,6->t(f,0,0,4,16,14,14);case 7->t(f,0,0,13,16,10,16);default->Shapes.block();};
            case DRIVEIN->switch(stage){case 0,1->Block.box(0,12,0,16,16,16);case 2->Block.box(6,0,6,10,16,10);case 3->Block.box(5,0,5,11,16,11);case 4->t(f,0,0,8,16,16,16);case 7->Block.box(0,0,0,16,12,16);default->Shapes.block();};
            case GIN->stage==6?Block.box(0,0,2,16,9,14):stage==7?Block.box(3,0,3,13,16,13):Shapes.block();
            case BALE->Shapes.block();
            case SEED->switch(stage){case 1->Block.box(0,0,0,16,10,16);case 2->Block.box(0,0,2,16,8,14);case 3->t(f,1,0,13,15,16,16);case 6->t(f,4,5,10,12,14,16);case 7->t(f,4,0,0,12,16,16);default->stage==4||stage==5?t(f,0,0,13,16,16,16):Shapes.block();};
            case TOWER->switch(stage){case 0->Block.box(5,0,5,11,16,11);case 1->Block.box(0,0,0,16,16,16);case 2->t(f,2,0,13,14,16,16);case 3->Block.box(0,0,0,16,2,16);case 4->t(f,0,0,13,16,16,16);case 5->Block.box(5,0,5,11,8,11);default->Block.box(5,0,5,11,16,11);};
            case TANK->Shapes.block();
            case FIELD->switch(stage){case 0->Block.box(5,0,5,11,16,11);case 1->Block.box(6.5,0,6.5,9.5,16,9.5);case 2->t(f,6.5,0,0,9.5,9,9.5);
                case 3->t(f,0,6,6.5,16,9,16);case 4->t(f,6.5,6,6.5,16,16,9.5);case 5->Block.box(7,0,7,9,16,9);case 6->Block.box(5,0,5,11,16,11);default->t(f,0,4,0,16,16,8);};
            case STANDS->switch(stage){case 0->Block.box(0,6,0,16,8,16);case 1->Block.box(0,14,0,16,16,16);case 2->Block.box(7,0,7,9,16,9);
                case 3->t(f,0,0,14,16,14,16);case 4->t(f,0,0,3,16,14,13);case 5->t(f,0,0,0,16,16,2);case 6->t(f,0,0,0,16,12,9);default->t(f,4,0,0,12,12,10);};
            case LOCKER->switch(stage){case 0,1->t(f,0,0,4,16,16,16);case 2->t(f,0,0,4,16,8,12);case 3->t(f,0,0,2,16,13,14);case 4->t(f,0,2,15,16,14,16);
                case 5->t(f,6,8,11,10,11,16);case 6->t(f,0,0,13,16,16,16);default->t(f,1,2,15,15,14,16);};
            case SCORE->stage<=5?t(f,0,0,12,16,16,16):stage==6?Block.box(5,0,5,11,16,11):Shapes.block();
            case AMBER->switch(stage){case 0->t(f,1,0,3,15,16,15);case 1->t(f,0,0,6,16,16,16);case 2->Block.box(3,0,4,11,7,10);case 3->t(f,0,0,4,16,12,16);
                case 4->Block.box(2,0,2,14,14,14);case 5->t(f,0,0,8,16,16,16);case 6->t(f,0,0,0,16,16,2);default->t(f,1,0,3,15,8,13);};
            case LOT->switch(stage){case 0->Block.box(7,0,7,9,3,9);case 1->Block.box(1,0,1,15,15,15);case 2->Block.box(2.5,0,2.5,13.5,15,13.5);case 3->Block.box(0,0,0,16,2,16);
                case 4->t(f,1,0,2,15,14,11);case 5->Block.box(4,0,4,12,15,12);case 6->t(f,2,0,4,14,11,12);default->Block.box(0,0,0,16,3,16);};
            case PASTURE->switch(stage){case 0->Block.box(6,0,6,10,16,10);case 1,2->t(f,0,0,7,16,16,9);case 3->t(f,0,0,2,16,2,14);case 4->Shapes.block();
                case 5->Block.box(5,0,5,11,16,11);case 6->Block.box(3,0,3,13,8,13);default->Block.box(0,0,0,16,8,16);};
            case STOCK->switch(stage){case 0,3->t(f,0,0,13,16,16,16);case 1->Shapes.or(t(f,0,0,13,16,16,16),t(f,13,0,0,16,16,13));case 2->t(f,13,0,13,16,16,16);default->t(f,6,0,0,10,7,16);};
            case WINDMILL->switch(stage){case 0->Block.box(5,0,5,11,13,11);case 6->t(f,0,0,0,6,5,6);case 7->t(f,7,0,0,9,16,16);default->Block.box(7,0,7,9,16,9);};
            case PUMPJACK->switch(stage){case 0->Block.box(5,0,5,11,13,11);case 1->t(f,0,0,5,16,16,11);case 7->t(f,2,0,1,14,16,15);default->Block.box(7,0,7,9,16,9);};
            case BATTERY->switch(stage){case 0,1,3->Shapes.block();case 2->t(f,0,0,2,16,16,14);case 4->Block.box(0,0,6,16,16,10);default->t(f,6,0,8,10,16,12);};
        };
    }
    /** What stands in the way: buildings' fittings, fences and walls, trunks, posts and machines; not crops, weeds, canopies, wires,
     * lights, signs over a reader's head, posters, the clothesline or the awnings. */
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        int stage=s.getValue(STAGE);
        return switch(s.getValue(KIND)){
            case COTTON,WEED,POSTER,FIXTURE->Shapes.empty();
            case MESQUITE->stage<=1?getShape(s,l,p,c):Shapes.empty();
            case POLE->stage==0||stage==6?getShape(s,l,p,c):Shapes.empty();
            case SIGNPOST->stage==5?Block.box(7,0,7,9,16,9):getShape(s,l,p,c);
            case TRACK->Block.box(0,0,0,16,2,16);
            case YARD->stage==0||stage==2||stage==4?Shapes.empty():getShape(s,l,p,c);
            // 0.4.701: a gate swung open lets anyone through.
            case FENCE->stage==3?Shapes.empty():getShape(s,l,p,c);
            case SHOP->stage==3?Shapes.empty():getShape(s,l,p,c);
            case FRONT->stage==2||stage==3?Shapes.empty():getShape(s,l,p,c);
            case SIGN_DRIVEIN->stage==0?getShape(s,l,p,c):Shapes.empty();
            case DRIVEIN->stage==1||stage==0?Shapes.empty():getShape(s,l,p,c);
            case SEED->stage==6?Shapes.empty():getShape(s,l,p,c);
            case TOWER->stage==5?Shapes.empty():getShape(s,l,p,c);
            case ROOFTOP->stage==2?Shapes.empty():getShape(s,l,p,c);
            case LAW->stage==5?Shapes.empty():getShape(s,l,p,c);
            case BATH->stage==4?Shapes.empty():getShape(s,l,p,c);
            case KITCHEN->stage==6?Shapes.empty():getShape(s,l,p,c);
            case VIDEO->stage==5||stage==7?Shapes.empty():getShape(s,l,p,c);
            case FIELD->stage==7?Shapes.empty():getShape(s,l,p,c);
            case STANDS->stage==7?Shapes.empty():getShape(s,l,p,c);
            case LOCKER->stage==4||stage==5||stage==7?Shapes.empty():getShape(s,l,p,c);
            case LOT->stage==0||stage==3?Shapes.empty():getShape(s,l,p,c);
            // The pasture's fence stands a block and a half, so nobody steps or jumps it; a reader crouched low slips between the strands
            // of a span or a shut gap, as one does. The gap laid back, the guard and the posts are what they are.
            case PASTURE->switch(stage){
                case 1,2->c instanceof EntityCollisionContext e&&e.getEntity() instanceof net.minecraft.world.entity.player.Player pl&&pl.isCrouching()?Shapes.empty():t(s.getValue(FACING),0,0,7,16,24,9);
                case 0->Block.box(6,0,6,10,24,10);case 5->Block.box(5,0,5,11,24,11);case 3->Shapes.empty();default->getShape(s,l,p,c);};
            case WINDMILL->stage==0||stage==6?getShape(s,l,p,c):Shapes.empty();
            case PUMPJACK->switch(stage){case 0,7->getShape(s,l,p,c);case 1->t(s.getValue(FACING),0,0,5,16,32,11);default->Shapes.empty();};
            case BATTERY->switch(stage){case 0,1,3->Shapes.block();case 2->getShape(s,l,p,c);case 4->Shapes.or(t(s.getValue(FACING),-13,0,7,-11,16,9),t(s.getValue(FACING),27,0,7,29,16,9));default->t(s.getValue(FACING),7,0,9,9,13,11);};
            default->getShape(s,l,p,c);
        };
    }
    /** Light from the sky falls through everything but the solid furniture, the walls of fittings and the machines. */
    @Override protected boolean propagatesSkylightDown(BlockState s,BlockGetter l,BlockPos p){
        var k=s.getValue(KIND);int stage=s.getValue(STAGE);
        return switch(k){
            case BALE,TANK->false;
            case SCORE->stage!=7;
            case SHOP->stage==3;
            case GIN->stage==6||stage==7;
            case FRONT->stage!=5;
            default->true;
        };
    }
}
