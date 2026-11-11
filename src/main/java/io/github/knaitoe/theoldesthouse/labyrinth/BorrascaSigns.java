package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;

/**
 * 0.4.449: Drisking's painted signs. The owner, of the shops: "Give the different stores some variance/characterization in their
 * signage"; of the historical society's: "Give it an actual sign"; of the trailhead arch and the rooms under the depot: "custom
 * signs". Every sign that was one of the game's own wooden ones is a picture now (tools/draw_borrasca_signs.py), sixteen pixels to
 * the block and lettered in the picture, so each shop has its own board, its own colours and its own shape.
 *
 * <p>A large sign is a grid of {@link BorrascaSignBlock}s, each showing its own square of the picture ({@link #hang}). A small one
 * is a single block whose board may reach past it. What a sign says in full, the lines too small to letter, is read by a hand on
 * it ({@link Sign#look}).
 */
public final class BorrascaSigns {
    private BorrascaSigns(){}

    /** How a sign is fixed: on the wall behind it; lettered straight onto what is behind it; inside a window, against the glass; or hanging free, read from both sides. */
    public enum Mount { WALL, DECAL, WINDOW, FREE }

    /** The signs, in the order the art tool draws them. A sign's blocks are numbered from {@link #first}, left to right and top row first. */
    public enum Sign {
        DINER(6,2,Mount.WALL,0,"HILLTOP CAFE, in red on the false front, a cup and a slice of pie painted under it. Pie, coffee, plate lunch."),
        ANTIQUES(6,1,Mount.WALL,0,"ROWE'S ANTIQUES, in gilt on green. Bought and sold. The Historical Society is through the back."),
        STATION(6,1,Mount.WALL,0,"RIDGE SERVICE, blue on white enamel. Gas, bait, ice, candy."),
        DOCTOR(6,1,Mount.WALL,0,"R. HALE, M.D. Family medicine. Walk-ins welcome."),
        DRUG(6,2,Mount.WALL,0,"MAIN STREET DRUG. Soda fountain. Prescriptions."),
        TIPPLE(6,1,Mount.WALL,9,"THE TIPPLE, in pink tube. Cold beer, pool, darts. Twenty-one and over."),
        STORE(9,2,Mount.WALL,0,"DRISKING GENERAL STORE. Feed and hardware. Est. 1961. The white is coming off the red in flakes."),
        COLD(2,1,Mount.WALL,0,"COLD DRINKS, on blue tin."),
        SHERIFF(5,1,Mount.WALL,0,"SHERIFF, in gold between two stars. County Sheriff, Drisking office."),
        SOCIETY(5,2,Mount.WALL,0,"DRISKING HISTORICAL SOCIETY, between two pairs of crossed picks. Open Saturdays."),
        HARDWARE(4,1,Mount.WALL,0,"HARDWARE. Ask at the counter."),
        LOST(2,1,Mount.WALL,0,"LOST & FOUND, in marker on a piece of box. Ask at the Sheriff's office."),
        TRAIL(4,2,Mount.WALL,0,"OZARK PINE TRAIL. Triple Tree, 1 mile. Marker Two, 2. The public walk ends at Marker Two. Stay on the marked trail. Return before dark. County Trails."),
        ARCH(7,1,Mount.WALL,0,"OZARK PINE TRAIL, burned into a plank."),
        FORT(3,2,Mount.WALL,0,"TRIPLE TREE FORT. NO GROWN-UPS. Under that, smaller, in pencil: knock first."),
        DEPOT(3,1,Mount.WALL,0,"RIDGE DEPOT. Office. Ring for service."),
        NO_ENTRY(2,2,Mount.WALL,0,"NO ENTRY."),
        CHURCH(4,2,Mount.WALL,0,"FIRST BAPTIST CHURCH. Sunday 10 am. All welcome."),
        SERVICE(3,1,Mount.WALL,0,"SERVICE EXIT. Mechanical release. Keep low. Return to trail."),
        COUNTY(3,1,Mount.DECAL,0,"COUNTY 17, stencilled on the tailgate. The Sheriff's office truck."),
        PRESCOTT(3,1,Mount.DECAL,0,"PRESCOTT ORE SERVICES, on the tailgate. D-417. 555-0190."),
        ARCH_MILE(1,1,Mount.FREE,0,"Triple Tree, 1 mile."),
        ROUNDEL(1,1,Mount.WALL,0,"No entry."),
        REFINING(1,1,Mount.WALL,0,"PRESCOTT REFINING. Service road. NO ENTRY."),
        NAME_PRUITT(1,1,Mount.WALL,0,"PRUITT, black on white enamel."),
        NAME_HOLLIS(1,1,Mount.WALL,0,"HOLLIS, burned into a piece of plank."),
        NAME_WEBB(1,1,Mount.WALL,0,"14. WEBB."),
        NAME_CORLEY(1,1,Mount.WALL,0,"CORLEY, stencilled on tin."),
        ROOM_1(1,1,Mount.WALL,0,"Room 1. The card slot is empty."),
        ROOM_2(1,1,Mount.WALL,0,"Room 2. The card slot is empty."),
        ROOM_3(1,1,Mount.WALL,0,"Room 3. The card slot is empty."),
        ROOM_4(1,1,Mount.WALL,0,"Room 4. The card slot is empty."),
        ROOM_5(1,1,Mount.WALL,0,"Room 5. The card slot is empty."),
        ROOM_6(1,1,Mount.WALL,0,"Room 6. A card in the slot: 263-A."),
        ROOM_7(1,1,Mount.WALL,0,"Room 7. A card in the slot: 264-A."),
        ROOM_8(1,1,Mount.WALL,0,"Room 8. The card slot is empty."),
        ROOM_9(1,1,Mount.WALL,0,"Room 9. The card slot is empty."),
        ROOM_10(1,1,Mount.WALL,0,"Room 10. The card slot is empty."),
        ROOM_11(1,1,Mount.WALL,0,"Room 11. The card slot is empty."),
        ROOM_12(1,1,Mount.WALL,0,"Room 12. The card slot is empty."),
        OPEN_DINER(1,1,Mount.WINDOW,7,"OPEN, in red tube, in the window."),
        OPEN_DRUG(1,1,Mount.WINDOW,5,"OPEN, blue on a lit white box."),
        OPEN_ANTIQUES(1,1,Mount.WINDOW,0,"OPEN, inked by hand on a card that hangs from a string."),
        IN_DOCTOR(2,1,Mount.WINDOW,0,"DOCTOR IS IN, on a card in the window.");

        public final int cols,rows,light;public final Mount mount;private final String look;private int first;
        Sign(int cols,int rows,Mount mount,int light,String look){this.cols=cols;this.rows=rows;this.mount=mount;this.light=light;this.look=look;}
        public int pieces(){return cols*rows;}
        /** The number of this sign's first block; its others follow, left to right, top row first. */
        public int first(){return first;}
        /** What a hand on it reads. */
        public String look(){return look;}
        /** The plate over a room under the depot. */
        public static Sign room(int number){return values()[ROOM_1.ordinal()+number-1];}
        static final int TOTAL;private static final Sign[] BY_PIECE;
        static{int n=0;for(var s:values()){s.first=n;n+=s.pieces();}TOTAL=n;BY_PIECE=new Sign[n];for(var s:values())for(int i=0;i<s.pieces();i++)BY_PIECE[s.first+i]=s;}
        public static Sign of(int piece){return BY_PIECE[piece];}
    }

    /** The way a sign's columns run, for someone facing it: to their right. */
    public static Direction right(Direction facing){return facing.getCounterClockWise();}
    /** Where one block of a sign goes: {@code at} is the left end of its bottom row as someone facing it sees it. */
    public static BlockPos cell(Sign s,BlockPos at,Direction facing,int col,int row){return at.relative(right(facing),col).above(s.rows-1-row);}
    /**
     * Hangs a whole sign, looking {@code facing}; {@code at} is the left end of its bottom row as someone facing it sees it. Every
     * block of it goes where there is nothing: a sign that would take the place of something else is a mistake in the builder, and says so.
     */
    /** Hangs a whole sign by the other end: {@code low} is the end of its bottom row with the smaller x or z, whichever way it looks. */
    public static void span(ServerLevel l,BlockPos low,Sign s,Direction facing){
        var r=right(facing);hang(l,r.getAxisDirection()==Direction.AxisDirection.POSITIVE?low:low.relative(r,-(s.cols-1)),s,facing);
    }
    public static void hang(ServerLevel l,BlockPos at,Sign s,Direction facing){
        for(int row=0;row<s.rows;row++)for(int col=0;col<s.cols;col++){
            var cell=cell(s,at,facing,col,row);
            if(!BuildBlocks.state(l,cell).isAir())throw new IllegalStateException("the "+s+" sign would hang where "+BuildBlocks.state(l,cell)+" is, at "+cell);
            BuildBlocks.set(l,cell,BorrascaSignBlock.of(s,row*s.cols+col,facing),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);
        }
    }
}
