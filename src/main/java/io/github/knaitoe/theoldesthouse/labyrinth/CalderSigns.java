package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.labyrinth.BorrascaSigns.Mount;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;

/**
 * 0.4.464, Bike night: Calder Falls' painted signs, made as Drisking's are ({@link BorrascaSigns}): each a picture sixteen pixels to
 * the block and lettered in the picture (tools/draw_calder_signs.py), a large one a grid of {@link CalderSignBlock}s each showing its
 * own square. What a sign says in full, the lines too small to letter, is read by a hand on it ({@link Sign#look}). The park's
 * two, and (0.4.481) the town's: its shops' names and fronts, its street names, and what is hung for Halloween.
 */
public final class CalderSigns {
    private CalderSigns(){}

    /** The signs, in the order the art tool draws them. A sign's blocks are numbered from {@link #first}, left to right and top row first. */
    public enum Sign {
        ARCH(6,1,Mount.FREE,0,"CALDER PARK, in cream letters on an iron board over the gate."),
        RULES(4,2,Mount.WALL,0,"PARK RULES. Shut at dusk. No cycling. Under that, in marker, in another hand: NO FUN."),
        // 0.4.481, the town. A shop's front is a picture too: its window band, hung on the wall like a sign.
        WASH_NAME(8,1,Mount.WALL,6,"WASH-O-MAT, in lit letters over the door."),
        WASH_FRONT(7,2,Mount.WALL,10,"Six dryers in a row behind the glass, four of them turning. A woman folding a sheet at the counter, alone. A television on a bracket, with a late film on. COIN-OP. WASH .50. OPEN, 24H."),
        BIGTOP_NAME(7,2,Mount.WALL,8,"BIG TOP BURGERS, in tubes under a tent's stripes. One letter of BURGERS keeps going out."),
        BIGTOP_NAME_DIM(7,2,Mount.WALL,8,"BIG TOP BURGERS, in tubes under a tent's stripes. One letter of BURGERS is out."),
        BIGTOP_FRONT(7,2,Mount.WALL,12,"The counter and its hatch, ORDER HERE, the menu over it, and an arcade cabinet in the corner with its screen going."),
        FOODFAIR_NAME(7,2,Mount.WALL,6,"CALDER FOOD FAIR."),
        FOOD_A(3,2,Mount.WALL,10,"Lettered by hand on butcher paper and taped inside the glass: CHUCK ROAST .89 LB."),
        FOOD_B(3,2,Mount.WALL,10,"Lettered by hand on butcher paper and taped inside the glass: CANDY CORN 2 FOR 1."),
        FOOD_C(3,2,Mount.WALL,10,"Lettered by hand on butcher paper and taped inside the glass: PUMPKINS .05 LB."),
        FOOD_A_DARK(3,2,Mount.WALL,0,"CHUCK ROAST .89 LB, on butcher paper. The lights behind it are off."),
        FOOD_B_DARK(3,2,Mount.WALL,0,"CANDY CORN 2 FOR 1, on butcher paper. The lights behind it are off."),
        FOOD_C_DARK(3,2,Mount.WALL,0,"PUMPKINS .05 LB, on butcher paper. The lights behind it are off."),
        MARQUEE_A(7,2,Mount.WALL,14,"Tonight, in changeable letters: CELLAR DOOR 2, and THE MOON MEN."),
        MARQUEE_B(7,2,Mount.WALL,14,"Tonight, in changeable letters: BITING HOUR, and SWAMP PROM."),
        MARQUEE_C(7,2,Mount.WALL,14,"Tonight, in changeable letters: IT CAME BACK, and DR. GRAVELY."),
        THEATER_FRONT(7,2,Mount.WALL,8,"TICKETS at the window, the doors, and a poster in a case each side: a house on a hill under the moon, and a hand coming up out of a well."),
        JRHIGH_NAME(8,1,Mount.WALL,0,"CALDER JR. HIGH, in cast letters."),
        SCHOOL_FRONT(6,2,Mount.WALL,0,"The school's doors, shut, and the blinds down. A paper banner: GO OTTERS."),
        TROPHY(2,2,Mount.WALL,8,"The trophy case, lit: cups on three shelves, and a team's photograph nobody has dusted."),
        BANK_NAME(7,1,Mount.WALL,0,"CALDER SAVINGS, cut into the stone."),
        BANK_FRONT(5,2,Mount.WALL,2,"Shut. The blinds are down. NIGHT deposit. EST. 1921."),
        VIDEO_NAME(7,1,Mount.WALL,8,"VIDEO HUT, in tubes."),
        VIDEO_FRONT(5,2,Mount.WALL,10,"Three shelves of tapes, face out. RENT, on a card. OPEN. DROP, over the slot in the door, for the ones coming back."),
        BARBER_NAME(5,1,Mount.WALL,0,"BARBER."),
        BARBER_FRONT(4,2,Mount.WALL,5,"Two chairs and the long mirror, by the one light left on. SHUT."),
        HARDWARE_NAME(7,1,Mount.WALL,0,"HALE HARDWARE."),
        HARDWARE_FRONT(6,2,Mount.WALL,4,"Rakes fanned out in the window, two shovels, a wheelbarrow. Pumpkins by the door, .05 each."),
        HOUSE_A(2,2,Mount.WALL,8,"A front window with the lamp on, and a paper skeleton taped inside the glass."),
        HOUSE_B(2,2,Mount.WALL,8,"A front window with the lamp on. A carved pumpkin on the sill, and the cat beside it, looking out."),
        HOUSE_C(2,2,Mount.WALL,3,"A dark window with one candle in it."),
        PARK_ST(3,1,Mount.FREE,0,"PARK ST."),
        MAIN_ST(3,1,Mount.FREE,0,"MAIN ST."),
        ELM_ST(3,1,Mount.FREE,0,"ELM ST."),
        CHURCH_LN(3,1,Mount.FREE,0,"CHURCH LN."),
        MILL_ST(3,1,Mount.FREE,0,"MILL ST."),
        WEST_AVE(3,1,Mount.FREE,0,"WEST AVE."),
        EAST_AVE(3,1,Mount.FREE,0,"EAST AVE."),
        CEMETERY(6,1,Mount.FREE,0,"CALDER HILL CEMETERY, on the iron over the gate."),
        STOP(1,1,Mount.FREE,0,"STOP."),
        HOUSE_DOOR(1,2,Mount.WALL,0,"A front door. Somebody has taped a paper bat to it."),
        FOOD_DOOR(2,2,Mount.WALL,9,"The grocery's doors: IN on one, OUT on the other."),
        DRIVE_THRU(3,1,Mount.FREE,8,"DRIVE THRU."),
        DELIVERIES(3,1,Mount.WALL,0,"DELIVERIES."),
        // 0.4.481, behind the shops and out among the houses.
        BACK_DOOR(1,2,Mount.WALL,0,"A back door, steel. It is locked."),
        SHUTTER(2,2,Mount.WALL,0,"A roll-down shutter, padlocked at the foot."),
        NO_PARKING(2,1,Mount.WALL,0,"NO PARKING."),
        TAG_RULES(3,2,Mount.DECAL,0,"TULLY RULES, in red spray paint, and a line under it."),
        TAG_HERE(3,1,Mount.DECAL,0,"RAY WUZ HERE."),
        TAG_SKULL(1,1,Mount.DECAL,0,"A skull, sprayed through a stencil."),
        FOR_SALE(2,1,Mount.FREE,0,"FOR SALE. The number under it has weathered off."),
        BEWARE(2,1,Mount.WALL,0,"BEWARE OF DOG. Nothing barks."),
        ELM_CT(3,1,Mount.FREE,0,"ELM CT."),
        DEAD_END(2,1,Mount.FREE,0,"DEAD END."),
        LOST_CAT(1,1,Mount.WALL,0,"LOST CAT. Grey, answers to Biscuit. The telephone number has been torn off."),
        // The way to the cemetery, named at the mouth of the cut; three one-sheets down the theater's side; the field's scoreboard.
        TO_CEMETERY(3,1,Mount.FREE,0,"CEMETERY. The blade lies along the path between the fences."),
        POSTER_A(2,2,Mount.WALL,6,"A one-sheet in a lit case. CELLAR DOOR 2. Two doors in the grass, one of them lifted, and a light under it."),
        POSTER_B(2,2,Mount.WALL,6,"A one-sheet in a lit case. SWAMP PROM. A hand up out of the water, a corsage on its wrist."),
        POSTER_C(2,2,Mount.WALL,6,"A one-sheet in a lit case. DR. GRAVELY. A headstone with a doctor's lamp strapped round it."),
        SCOREBOARD(5,2,Mount.WALL,0,"The scoreboard, switched off. OTTERS 06, GUESTS 34, in the fourth quarter. Nobody has cleared it.");
        public final int cols,rows,light;public final Mount mount;private final String look;private int first;
        Sign(int cols,int rows,Mount mount,int light,String look){this.cols=cols;this.rows=rows;this.mount=mount;this.light=light;this.look=look;}
        public int pieces(){return cols*rows;}
        /** The number of this sign's first block; its others follow, left to right, top row first. */
        public int first(){return first;}
        /** What a hand on it reads. */
        public String look(){return look;}
        static final int TOTAL;private static final Sign[] BY_PIECE;
        static{int n=0;for(var s:values()){s.first=n;n+=s.pieces();}TOTAL=n;BY_PIECE=new Sign[n];for(var s:values())for(int i=0;i<s.pieces();i++)BY_PIECE[s.first+i]=s;}
        public static Sign of(int piece){return BY_PIECE[piece];}
    }

    /** Where one block of a sign goes: {@code at} is the left end of its bottom row as someone facing it sees it. */
    public static BlockPos cell(Sign s,BlockPos at,Direction facing,int col,int row){return at.relative(BorrascaSigns.right(facing),col).above(s.rows-1-row);}
    /**
     * Hangs a whole sign, looking {@code facing}; {@code at} is the left end of its bottom row as someone facing it sees it. Every
     * block of it goes where there is nothing: a sign that would take the place of something else is a mistake in the builder, and says so.
     */
    public static void hang(ServerLevel l,BlockPos at,Sign s,Direction facing){
        for(int row=0;row<s.rows;row++)for(int col=0;col<s.cols;col++){
            var cell=cell(s,at,facing,col,row);
            if(!BuildBlocks.state(l,cell).isAir())throw new IllegalStateException("the "+s+" sign would hang where "+BuildBlocks.state(l,cell)+" is, at "+cell);
            BuildBlocks.set(l,cell,CalderSignBlock.of(s,row*s.cols+col,facing),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);
        }
    }
}
