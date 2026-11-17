package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;

/**
 * 0.4.672, Fallow (docs/FALLOW_0_4_672.md): the station's, the yard's, the road's and the wheat's things, drawn facing north by
 * tools/draw_fallow_art.py.
 * <ul>
 * <li>The store. COUNTER: a length of the counter, plain (0), with the register (1), its end, the flap lifted and standing up against the counter so the staff can pass (2), with the candy rack
 * (3), and (0.4.684) the till end after the light, the top scorched and the keeper's ledger singed on the shelf beneath (4). NOTE: the keeper's note propped against the register (0). STOOL: the keeper's stool. BACKSHELF: the back wall's cabinet (0), its
 * shelf of cartons and cans (1), the shelf with the radio on it (2). TV: the television on its bracket, its picture (0), a card with
 * nothing on it (1), dark (2). COOLER: a glass-doored cooler's lower half (0) and its lit top, one panel along the run of doors: ICE (1), COLD (2) and plain (3). GONDOLA: shelving down the middle,
 * double-sided, of cans (0), boxes (1) and oil and parts (2), and its low top shelf (3). MAPS: the rack of road maps (0) and its header
 * (1). COFFEE: the coffee machine on its stand. WINDOW: a plate window's lower (0) and upper (1) half, and the same blown in by the
 * light, the frame left and the glass gone but for its edges (2, 3). TUBE: a strip light.
 * SIGNBAND: the four boards of the station's name along the roof (0-3).</li>
 * <li>The forecourt. PUMP: a pump on its island (0) and its head with the dials (1); the diesel pump (2) and its head (3). ISLAND: the
 * island's curb (0) and its rounded end (1). CANOPY: the deck (0), the deck with a light let into it (1), its edge with the fascia (2),
 * a corner (3), a column (4), a column's foot (5), a column's top, reaching up into the deck (6). SIGN: the pole (0), the price board (1), the board with the name (2).</li>
 * <li>The road and the field. POLE: a telephone pole (0), its top with the crossarm (1), and its two wires between poles, high (2),
 * lower (3) and lowest (4); 0.4.682, after the light, a pole down along the way it faces (5) and its top, the crossarm stood on end out
 * of the ground (6). WHEAT: standing ripe (0), a bowed head (1), stubble (2), ash on stubble (3). PAD: a pad's chain-link
 * fence (0), a fence post (1), its gate (2), the light's pole (3) and its lamp (4), the lid's seamed edge (5) and its middle (6), the
 * hatch box (7).</li>
 * <li>The garage. LIFT: a post of the lift (0), a crossbar (1), the bench (2), its pegboard (3), a stack of tyres (4), an oil drum (5),
 * a post's top (6).</li>
 * <li>The yard and the cellar. TRAILER: a curtained window of the trailer (0), its hitch (1), the gas tank (2), cinder-block steps (3),
 * a clothesline post (4) and its lines (5), a barrel (6), the yard's wire fence (7). CELLAR: the cellar doors shut, two flat leaves on a low
 * concrete curb, the left leaf's three lengths from the south end (0-2) and the right's (3-5); CELLAR_OPEN the same, thrown back over
 * the curb onto the yard. SHELF: a cellar shelf of jars (0)
 * or tins (1). COT: its head (0) and foot (1). CROCK: the water crock (0), the lantern lit (1) and dark (2); 0.4.681, turned low (3).</li>
 * <li>Across the road. FARM: a farmhouse window (0), a porch post (1), the porch rail (2), the mailbox on its post (3); 0.4.682, after
 * the light, a beam of the farmhouse burnt through (4), a heap of its boards in their ash (5), a charred jamb of the front door's frame
 * on the door's side of its block (6) and the lintel over the door (7).</li>
 * <li>The rig. RIG: a slice of the cab, each drawn whole across both its cells: the hood (0, 1), the cab (2, 3), the sleeper (4, 5),
 * the roof over the cab and sleeper (6), and the other half of a slice, which only stands in the way (7). BOX: the trailer's two axles
 * (0), its landing legs (1), its back doors (2), a half that only stands in the way (3).</li>
 * <li>0.4.673, BEAN: Opal's bean in its paper cup, set down on the forecourt: two leaves (0), four (1), a tangle as high again as the
 * cup (2), grown long and thin in the dark (3). Nothing stands in its way.</li>
 * <li>0.4.680, RUBBLE: what the light leaves of the station. A low spread of the store (0), a heap of it (1), the shut cellar doors with
 * the store come down on them (2), a joist fallen across a spread (3), a sheet of the canopy's deck down on the forecourt, buckled (4),
 * another leaning on what caught it (5), a column of the canopy lying down (6), a fallen run of the store's wall (7). 0.4.681: the fall
 * over the doors (2) lets no light through.</li>
 * </ul>
 */
public final class FallowBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<FallowBlock> CODEC=simpleCodec(FallowBlock::new);
    public enum Kind implements StringRepresentable {
        COUNTER,NOTE,STOOL,BACKSHELF,TV,COOLER,GONDOLA,MAPS,COFFEE,WINDOW,TUBE,SIGNBAND,
        PUMP,ISLAND,CANOPY,SIGN,POLE,WHEAT,PAD,
        LIFT,TRAILER,CELLAR,CELLAR_OPEN,SHELF,COT,CROCK,FARM,RIG,BOX,BEAN,RUBBLE;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,7);
    /** The television's stages. */
    public static final int PICTURE=0,CARD=1,DARK=2;
    /** 0.4.684: the counter's till end after the light (FallowRuin.ledger). */
    public static final int LEDGER=4;
    /** Wheat's stages; 0.4.683: and coming up again round Opal's bean, shoots, then young wheat (FallowRoad.green). */
    public static final int RIPE=0,BOWED=1,STUBBLE=2,ASH=3,SHOOT=4,GREEN=5;
    /** A window's halves, whole and blown in. */
    public static final int PANE_LOW=0,PANE_HIGH=1,BLOWN_LOW=2,BLOWN_HIGH=3;
    /** 0.4.681: the lantern's stages (CROCK): lit, dark, and turned low (FallowBuried). */
    public static final int LANTERN_LIT=1,LANTERN_DARK=2,LANTERN_LOW=3;
    /** 0.4.682: a pole down (POLE) and its top; what the farmhouse burnt down to (FARM). */
    public static final int POLE_DOWN=5,POLE_DOWN_TOP=6,BEAM=4,BURNT=5,JAMB=6,LINTEL=7;
    /** Rubble's stages. */
    public static final int SPREAD=0,HEAP=1,ON_DOORS=2,JOIST=3,SHEET=4,LEANING=5,COLUMN=6,WALL=7;
    public FallowBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.COUNTER).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return FallowRegistry.FITTING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,Math.max(0,Math.min(7,stage)));}
    public static boolean is(BlockState s){return s.is(FallowRegistry.FITTING.get());}
    public static boolean is(BlockState s,Kind kind){return is(s)&&s.getValue(KIND)==kind;}
    /** The light a fitting gives: the strip lights, the canopy's lamps, the coolers' tops, the pumps' dials, the television's picture,
     * a pad's lamp and the cellar's lantern. */
    static int light(BlockState s){
        int stage=s.getValue(STAGE);
        return switch(s.getValue(KIND)){
            case TUBE->14;case CANOPY->stage==1?14:0;case COOLER->stage>=1?9:0;case PUMP->stage==1||stage==3?6:0;case TV->stage==PICTURE||stage==CARD?5:0;
            case PAD->stage==4?12:0;case CROCK->stage==LANTERN_LIT?12:stage==LANTERN_LOW?6:0;
            default->0;
        };
    }
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    private static VoxelShape t(Direction f,double x0,double y0,double z0,double x1,double y1,double z1){return MarshBlock.turned(f,x0,y0,z0,x1,y1,z1);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);int stage=s.getValue(STAGE);
        return switch(s.getValue(KIND)){
            case COUNTER->stage==2?t(f,0,0,1,3,16,15):Shapes.block();
            case NOTE->t(f,3,0,9,11,10,13);
            case STOOL->Block.box(4,0,4,12,11,12);
            case BACKSHELF->stage==0?Shapes.block():t(f,0,0,6,16,16,16);
            case TV->t(f,2,2,4,14,13,16);
            case COOLER->t(f,0,0,1,16,16,16);
            case GONDOLA->stage==3?t(f,1,0,3,15,8,13):t(f,1,0,3,15,16,13);
            case MAPS->t(f,3,0,9,13,16,16);
            case COFFEE->t(f,1,0,2,15,16,16);
            case WINDOW->t(f,0,0,7,16,16,9);
            case TUBE->Block.box(2,12,6,14,16,10);
            case SIGNBAND->t(f,0,0,6,16,16,10);
            case PUMP->stage==1||stage==3?t(f,3,0,4,13,12,12):t(f,0,0,2,16,16,14);
            case ISLAND->stage==1?t(f,2,0,1,14,6,16):t(f,0,0,2,16,6,14);
            case CANOPY->stage==4||stage==6?Block.box(5,0,5,11,16,11):stage==5?Block.box(4,0,4,12,16,12):Block.box(0,8,0,16,16,16);
            case SIGN->stage==0?Block.box(6,0,6,10,16,10):t(f,0,0,6,16,16,10);
            case POLE->stage==0?Block.box(5.5,0,5.5,10.5,16,10.5):stage==1?t(f,0,6,6,16,16,10):stage>=POLE_DOWN?down(f,stage):t(f,0,10,6,16,16,10);
            case WHEAT->Block.box(1,0,1,15,stage==GREEN?10:stage>=STUBBLE?4:13,15);
            case PAD->switch(stage){case 3->Block.box(6.5,0,6.5,9.5,16,9.5);case 4->Block.box(4,0,4,12,10,12);case 5,6->Shapes.block();case 7->t(f,3,0,3,13,9,13);default->t(f,0,0,7,16,16,9);};
            case LIFT->switch(stage){case 0,6->Block.box(4,0,4,12,16,12);case 1->t(f,0,12,6,16,16,10);case 2->t(f,0,0,4,16,15,16);case 3->t(f,0,0,14,16,16,16);case 4->Block.box(1,0,1,15,13,15);default->Block.box(3,0,3,13,15,13);};
            case TRAILER->switch(stage){case 0->Shapes.block();case 1->t(f,6,0,0,10,5,16);case 2->t(f,4,0,4,12,15,12);case 3->Shapes.or(t(f,0,0,0,16,4,16),t(f,0,4,8,16,8,16));case 4->Block.box(6.5,0,6.5,9.5,16,9.5);case 5->t(f,0,12,6,16,15,10);case 6->Block.box(3,0,3,13,14,13);default->t(f,0,0,7,16,14,9);};
            case CELLAR->t(f,0,0,0,16,9,16);
            case CELLAR_OPEN->{var curb=stage<3?t(f,0,0,0,2,7,16):t(f,14,0,0,16,7,16);yield stage%3==2?Shapes.or(curb,t(f,0,0,0,16,7,2)):stage%3==0?Shapes.or(curb,t(f,0,0,14,16,7,16)):curb;}
            case SHELF->t(f,0,0,8,16,16,16);
            case COT->Block.box(1,0,1,15,9,15);
            case CROCK->stage==0?Block.box(4,0,4,12,10,12):Block.box(5,3,5,11,16,11);
            case FARM->switch(stage){case 0->Shapes.block();case 1->Block.box(6,0,6,10,16,10);case 2->t(f,0,0,7,16,13,9);case BEAM->Block.box(0,0,3,16,4,13);
                case BURNT->Block.box(0,0,0,16,6,16);case JAMB->f==Direction.WEST?Block.box(0,0,0,4,16,4):Block.box(12,0,0,16,16,4);case LINTEL->Block.box(0,0,0,16,4,4);default->Block.box(5,0,5,11,16,11);};
            case RIG,BOX->Shapes.block();
            case BEAN->Block.box(4,0,4,12,stage==0?11:16,12);
            case RUBBLE->switch(stage){case SPREAD->Block.box(0,0,0,16,4,16);case HEAP->Block.box(0,0,0,16,9,16);case ON_DOORS->Block.box(0,0,0,16,13,16);
                case JOIST->Block.box(0,0,0,16,5,16);case SHEET->Block.box(0,0,0,16,3,16);case LEANING->Block.box(0,0,0,16,12,16);case COLUMN->t(f,0,0,4,16,7,12);
                default->Block.box(0,0,0,16,5,16);};
        };
    }
    /** 0.4.682: a pole lying along the way it faces, low enough to step over; its top with the crossarm stood up out of the ground. */
    private static VoxelShape down(Direction f,int stage){
        var shaft=f.getAxis()==Direction.Axis.Z?Block.box(5.5,0,0,10.5,5,16):Block.box(0,0,5.5,16,5,10.5);
        if(stage!=POLE_DOWN_TOP)return shaft;
        // The crossarm stands at the end the block faces toward.
        int dx=f.getStepX(),dz=f.getStepZ();
        double x0=dx==0?6:dx>0?11.5:2,x1=dx==0?10:dx>0?14:4.5,z0=dz==0?6:dz>0?11.5:2,z1=dz==0?10:dz>0?14:4.5;
        return Shapes.or(shaft,Block.box(x0,0,z0,x1,16,z1));
    }
    /** What stands in the way: the fittings with bodies, the shut cellar doors, the fences and the rig; not wheat, lights, signs above
     * head height, wires, the note, the lid's middle (which is walked over) or the lines on the clothesline. */
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        int stage=s.getValue(STAGE);
        return switch(s.getValue(KIND)){
            case NOTE,TUBE,WHEAT,SIGNBAND,BEAN->Shapes.empty();
            case POLE->stage==0||stage>=POLE_DOWN?getShape(s,l,p,c):Shapes.empty();
            case CANOPY->stage>=4?getShape(s,l,p,c):Shapes.empty();
            case SIGN->stage==0?getShape(s,l,p,c):Shapes.empty();
            case PAD->stage==4?Shapes.empty():getShape(s,l,p,c);
            case TRAILER->stage==5?Shapes.empty():getShape(s,l,p,c);
            case CROCK->stage==0?getShape(s,l,p,c):Shapes.empty();
            case LIFT->stage==1||stage==3?Shapes.empty():getShape(s,l,p,c);
            // A blown window is only its frame, and a body may climb in over its sill.
            case WINDOW->stage>=BLOWN_LOW?Shapes.empty():getShape(s,l,p,c);
            default->getShape(s,l,p,c);
        };
    }
    @Override protected boolean propagatesSkylightDown(BlockState s,BlockGetter l,BlockPos p){
        var k=s.getValue(KIND);int stage=s.getValue(STAGE);
        return !(k==Kind.COUNTER&&stage!=2||k==Kind.RIG||k==Kind.BOX||k==Kind.TRAILER&&stage==0||k==Kind.FARM&&stage==0||k==Kind.PAD&&(stage==5||stage==6)||k==Kind.CANOPY&&stage<4||k==Kind.CELLAR||buries(s));
    }
    /** 0.4.681: the store come down across the cellar doors lets no light through: below, in the buried days, there is only the lantern
     * until a length of the doors is dug clear. */
    @Override protected int getLightBlock(BlockState s,BlockGetter l,BlockPos p){return buries(s)?15:super.getLightBlock(s,l,p);}
    static boolean buries(BlockState s){return s.getValue(KIND)==Kind.RUBBLE&&s.getValue(STAGE)==ON_DOORS;}
}
