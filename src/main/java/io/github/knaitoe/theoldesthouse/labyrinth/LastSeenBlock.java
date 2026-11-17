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
 * 0.4.667, Last Seen (docs/LAST_SEEN_0_4_667.md): the office's and the park's things, drawn facing north by tools/draw_last_seen_art.py.
 * <ul>
 * <li>The office. COUNTER: a length of the front desk, plain (0) or its end with the lifting flap (1). CARD: the typed card lying on the
 * counter (the first paper). CHAIR: an office chair. SHELF: a bay of steel shelving, floor to ceiling, of folders standing spine out: three
 * fills (0-2), one with a folder out (3), the top bay with archive boxes (4). POSTER and POSTER_B: the poster wall, one poster to a cell,
 * each a face and two lines of type; sixteen in all. TABLE: a length of the archive's long table, bare (0), with the day's folders in a
 * pile (1), with its lamp (2). DESK: Nora's desk with the typewriter on it, its one drawer locked (0), unlocked (1) or open (2). LOUVRE:
 * one of the office's louvred windows. TUBE: a strip light on the ceiling.</li>
 * <li>The park. RAILING: the iron railing, a run of bars (0), a post with its finial (1). GATE: a leaf of a park gate, shut (0), shut and
 * chained (1), open (2). LAMP_POST: a park lamp, its foot (0), its shaft (1), its globe lit (2) or dark (3). BENCH: a park bench's left end
 * (0), middle (1) or right end (2). FOUNTAIN: the dry fountain's rim (0), its rim's corner (1), its plinth (2) and its bowl (3). KIOSK: the
 * kiosk's shuttered front (0), its side with old bills pasted on it (1). TRUNK: a silk-floss tree's trunk, its foot (0), its swollen belly
 * (1), its neck (2), its fork (3). LEAVES: a plane's canopy (0), a silk-floss's (1), a plane's thinner edge (2). PLAY: the playground's
 * swing frame's post (0), a swing on its chains (1), the slide's ladder (2) and chute (3), the see-saw (4), its low fence (5), the frame's
 * bar (6), and the chains under it (7). BANDSTAND: a post
 * (0), the balustrade (1), the eave with its iron valance on its outer face (2), a step (3), the eave at a corner, its valance on both outer
 * faces (4).</li>
 * <li>The avenue. SHELTER: the bus shelter's glass side (0), its roof (1), the stop's sign (2), its bench (3). SHUTTER: a house's window
 * set into its wall with a sill and a head, one block high with the shutter down (0) or a light behind it (1); or a tall window's low half
 * (2) and top half (3) with the blind down, or with the blind half up on a lit room (4, 5).</li>
 * </ul>
 */
public final class LastSeenBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<LastSeenBlock> CODEC=simpleCodec(LastSeenBlock::new);
    public enum Kind implements StringRepresentable {
        COUNTER,CARD,CHAIR,SHELF,POSTER,POSTER_B,TABLE,DESK,LOUVRE,TUBE,
        RAILING,GATE,LAMP_POST,BENCH,FOUNTAIN,KIOSK,TRUNK,LEAVES,PLAY,BANDSTAND,
        SHELTER,SHUTTER;
        public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Kind> KIND=EnumProperty.create("kind",Kind.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,7);
    /** Stages of a shelf bay. */
    public static final int FULL=0,ONE_OUT=3,BOXES=4;
    /** Stages of a gate leaf, and of Nora's drawer. */
    public static final int SHUT=0,CHAINED=1,OPEN=2,LOCKED=0,UNLOCKED=1,DRAWN=2;
    /** Stages of a lamp post. */
    public static final int FOOT=0,SHAFT=1,LIT=2,DARK=3;
    public LastSeenBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(KIND,Kind.COUNTER).setValue(STAGE,0));}
    public static BlockState of(Kind kind,Direction facing){return of(kind,facing,0);}
    public static BlockState of(Kind kind,Direction facing,int stage){return LastSeenRegistry.FITTING.get().defaultBlockState().setValue(KIND,kind).setValue(FACING,facing).setValue(STAGE,Math.max(0,Math.min(7,stage)));}
    public static boolean is(BlockState s){return s.is(LastSeenRegistry.FITTING.get());}
    public static boolean is(BlockState s,Kind kind){return is(s)&&s.getValue(KIND)==kind;}
    /** The light a fitting gives: a lit lamp globe, the archive's table lamp, a window with a light behind it. */
    static int light(BlockState s){
        return switch(s.getValue(KIND)){
            case LAMP_POST->s.getValue(STAGE)==LIT?15:0;case TUBE->13;case TABLE->s.getValue(STAGE)==2?11:0;case SHUTTER->s.getValue(STAGE)==1||s.getValue(STAGE)==4?7:0;
            default->0;
        };
    }
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,KIND,STAGE);}
    private static VoxelShape t(Direction f,double x0,double y0,double z0,double x1,double y1,double z1){return MarshBlock.turned(f,x0,y0,z0,x1,y1,z1);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        var f=s.getValue(FACING);int stage=s.getValue(STAGE);
        return switch(s.getValue(KIND)){
            case COUNTER->Shapes.block();
            case CARD->t(f,4,0,5,12,1,11);
            case CHAIR->Shapes.or(t(f,3,0,3,13,9,13),t(f,3,9,11,13,19,13));
            case SHELF->Shapes.block();
            case POSTER,POSTER_B->t(f,1,1,15,15,15,16);
            case TABLE->t(f,0,0,0,16,13,16);
            case DESK->t(f,0,0,1,16,13,16);
            case LOUVRE->t(f,0,0,6,16,16,10);
            case TUBE->Block.box(2,13,6,14,16,10);
            case RAILING->stage==1?Block.box(6,0,6,10,16,10):t(f,0,0,7,16,16,9);
            case GATE->t(f,0,0,7,16,16,9);
            case LAMP_POST->stage==FOOT?Block.box(5,0,5,11,16,11):stage==SHAFT?Block.box(6.5,0,6.5,9.5,16,9.5):Block.box(3,0,3,13,13,13);
            case BENCH->t(f,0,0,2,16,15,14);
            case FOUNTAIN->stage==2?Block.box(3,0,3,13,16,13):stage==3?Block.box(1,0,1,15,6,15):t(f,0,0,0,16,8,16);
            case KIOSK->Shapes.block();
            case TRUNK->stage==2?Block.box(4,0,4,12,16,12):stage==3?Block.box(3,0,3,13,16,13):Shapes.block();
            case LEAVES->Shapes.block();
            case PLAY->switch(stage){case 1->t(f,4,4,6,12,16,10);case 3->t(f,2,0,0,14,10,16);case 4->t(f,0,0,6,16,8,10);case 5->t(f,0,0,7,16,10,9);case 6->t(f,0,12,6.5,16,15,9.5);case 7->t(f,4,0,7,12,16,9);default->t(f,6,0,6,10,16,10);};
            case BANDSTAND->switch(stage){case 0->Block.box(6,0,6,10,16,10);case 1->t(f,0,0,7,16,12,9);case 2,4->Block.box(0,15,0,16,16,16);default->t(f,0,0,8,16,8,16);};
            case SHELTER->switch(stage){case 1->Block.box(0,13,0,16,16,16);case 2->Block.box(7,0,7,9,16,9);case 3->t(f,0,0,8,16,8,14);default->t(f,0,0,7,16,16,9);};
            case SHUTTER->Shapes.block();
        };
    }
    /** What stands in the way: the furniture, the railings and gates (a shut gate), the fountain, the kiosk, the trunks, the bandstand's posts and
     * rail, and the houses' windows, which are part of their walls. */
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        int stage=s.getValue(STAGE);
        return switch(s.getValue(KIND)){
            case CARD,POSTER,POSTER_B,LEAVES,TUBE->Shapes.empty();
            case GATE->stage==OPEN?Shapes.empty():getShape(s,l,p,c);
            case PLAY->stage==1||stage>=6?Shapes.empty():getShape(s,l,p,c);
            case BANDSTAND->stage==2||stage==4?Shapes.empty():getShape(s,l,p,c);
            case SHELTER->stage==1?Shapes.empty():getShape(s,l,p,c);
            default->getShape(s,l,p,c);
        };
    }
    @Override protected boolean propagatesSkylightDown(BlockState s,BlockGetter l,BlockPos p){var k=s.getValue(KIND);return k!=Kind.COUNTER&&k!=Kind.SHELF&&k!=Kind.KIOSK&&k!=Kind.TRUNK;}
}
