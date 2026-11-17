package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.labyrinth.BorrascaSigns.Mount;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;

/**
 * 0.4.677: the short signs that were still the game's own wooden signs with words typed on them, painted as the flood station's are
 * ({@link CrossingSigns}). The owner, of the Last Crossing: "there are several signs in last crossing that could benefit from custom
 * textures. i was hoping you'd notice that on your own". These are the rest of them: Camp Blood's gate board, its cabin plates and the
 * board where its sand road ends; the elk lot's service gate and the name on the yacht; Pinehook's name at the apron and the
 * boathouse's PRIVATE plate; Movie Night's ticket hut; and the tags on the graves under Borrasca. Each is a picture sixteen pixels to
 * the block, lettered in the picture (tools/draw_board_signs.py), a large one a grid of {@link BoardSignBlock}s each showing its own
 * square. What a sign says, and what is on it besides, is read by a hand on it ({@link Sign#look}).
 */
public final class BoardSigns {
    private BoardSigns(){}

    /** The signs, in the order the art tool draws them. A sign's blocks are numbered from {@link #first}, left to right and top row first. */
    public enum Sign {
        CAMP(3,2,Mount.FREE,0,"CAMP, routed into two planks between the posts. Under it: CABINS 1-6. LIGHTS OUT AT TEN."),
        CABIN_1(1,1,Mount.WALL,0,"A 1 routed into a plank by the door, a nail at each end."),
        CABIN_2(1,1,Mount.WALL,0,"A 2 routed into a plank by the door, a nail at each end."),
        CABIN_3(1,1,Mount.WALL,0,"A 3 routed into a plank by the door, a nail at each end."),
        CABIN_4(1,1,Mount.WALL,0,"A 4 routed into a plank by the door, a nail at each end."),
        CABIN_5(1,1,Mount.WALL,0,"A 5 routed into a plank by the door, a nail at each end."),
        CABIN_6(1,1,Mount.WALL,0,"A 6 routed into a plank by the door, a nail at each end."),
        CAMP_ROAD(4,1,Mount.FREE,0,"SERVICE ROAD. NO CAMP TRAFFIC. Moss has got into the bottom of the letters."),
        CREW_GATE(4,2,Mount.FREE,0,"SERVICE ROAD. CREW ONLY. Under it, in marker: SIGN OUT HERE."),
        BOAT(4,1,Mount.DECAL,0,"SECOND SUMMER, painted on her side. INDIAN LAKE under it, smaller. The paint is flaking."),
        PINEHOOK(4,1,Mount.FREE,0,"PINEHOOK, burned into a split log between two posts."),
        PRIVATE(3,1,Mount.WALL,0,"PRIVATE. NO ENTRY. A tin plate screwed over the door, rust run down from the screws."),
        TICKETS(5,1,Mount.FREE,0,"TICKETS. MOVIE NIGHT. A row of bulbs round the board; most of them still work."),
        GRAVE_0(1,1,Mount.FREE,0,"211-A, written on a tin tag nailed to the stake."),
        GRAVE_1(1,1,Mount.FREE,0,"214-A, written on a tin tag nailed to the stake."),
        GRAVE_2(1,1,Mount.FREE,0,"219-A, written on a tin tag nailed to the stake."),
        GRAVE_3(1,1,Mount.FREE,0,"222-A, written on a tin tag nailed to the stake."),
        GRAVE_4(1,1,Mount.FREE,0,"226-A, written on a tin tag nailed to the stake."),
        GRAVE_5(1,1,Mount.FREE,0,"230-A, written on a tin tag nailed to the stake."),
        GRAVE_6(1,1,Mount.FREE,0,"234-A, written on a tin tag nailed to the stake."),
        GRAVE_7(1,1,Mount.FREE,0,"238-A, written on a tin tag nailed to the stake."),
        GRAVE_8(1,1,Mount.FREE,0,"241-A, written on a tin tag nailed to the stake."),
        GRAVE_9(1,1,Mount.FREE,0,"245-A, written on a tin tag nailed to the stake."),
        GRAVE_10(1,1,Mount.FREE,0,"247-A, written on a tin tag nailed to the stake."),
        GRAVE_11(1,1,Mount.FREE,0,"251-A, written on a tin tag nailed to the stake."),
        GRAVE_12(1,1,Mount.FREE,0,"255-A, written on a tin tag nailed to the stake."),
        GRAVE_13(1,1,Mount.FREE,0,"258-A, written on a tin tag nailed to the stake."),
        GRAVE_14(1,1,Mount.FREE,0,"260-A, written on a tin tag nailed to the stake.");
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
        /** The plate by cabin {@code n}'s door, 1 to 6. */
        public static Sign cabin(int n){return values()[CABIN_1.ordinal()+n-1];}
        /** The tag on grave {@code i}'s stake, as BorrascaRooms numbers them. */
        public static Sign grave(int i){return values()[GRAVE_0.ordinal()+i];}
    }

    /** Where one block of a sign goes: {@code at} is the left end of its bottom row as someone facing it sees it. */
    public static BlockPos cell(Sign s,BlockPos at,Direction facing,int col,int row){return at.relative(BorrascaSigns.right(facing),col).above(s.rows-1-row);}
    /** The left end of the bottom row of a sign centred on {@code middle}, the middle block of its bottom row (of an even number, the
     * one left of the middle). */
    public static BlockPos from(BlockPos middle,Sign s,Direction facing){return middle.relative(BorrascaSigns.right(facing),-(s.cols-1)/2);}
    /** Hangs a whole sign centred on {@code middle} (see {@link #from}). */
    public static void centred(ServerLevel l,BlockPos middle,Sign s,Direction facing){hang(l,from(middle,s,facing),s,facing);}
    /**
     * Hangs a whole sign, looking {@code facing}; {@code at} is the left end of its bottom row as someone facing it sees it. Every
     * block of it goes where there is nothing: a sign that would take the place of something else is a mistake in the builder, and says so.
     */
    public static void hang(ServerLevel l,BlockPos at,Sign s,Direction facing){
        for(int row=0;row<s.rows;row++)for(int col=0;col<s.cols;col++){
            var cell=cell(s,at,facing,col,row);
            if(!BuildBlocks.state(l,cell).isAir())throw new IllegalStateException("the "+s+" sign would hang where "+BuildBlocks.state(l,cell)+" is, at "+cell);
            BuildBlocks.set(l,cell,BoardSignBlock.of(s,row*s.cols+col,facing),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);
        }
    }
}
