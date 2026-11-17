package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.labyrinth.BorrascaSigns.Mount;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;

/**
 * 0.4.675, The Last Crossing: the flood station's painted signs, made as Drisking's and Calder Falls' are ({@link BorrascaSigns},
 * {@link CalderSigns}): each a picture sixteen pixels to the block and lettered in the picture (tools/draw_crossing_signs.py), a large
 * one a grid of {@link CrossingSignBlock}s each showing its own square. The owner asked for the name board over the porch to have its
 * own picture, and that the station's other signs should have theirs: the keeper's plate by the office door, the barricade where the
 * road goes under the water, and the warning painted on the pump room's wall over the ramp. What a sign says, and what is on it besides,
 * is read by a hand on it ({@link Sign#look}).
 */
public final class CrossingSigns {
    private CrossingSigns(){}

    /** The signs, in the order the art tool draws them. A sign's blocks are numbered from {@link #first}, left to right and top row first. */
    public enum Sign {
        STATION(5,1,Mount.WALL,0,"ROOK FERRY FLOOD STATION, on boards over the porch. A line of dried silt runs across the lower letters."),
        KEEPER(1,1,Mount.WALL,0,"KEEPER, on an enamel plate by the door. A corner of it is chipped."),
        ROAD(3,1,Mount.FREE,0,"ROAD FLOODED, on the board across the sawhorse. Mud is caked up its legs."),
        RAMP(3,1,Mount.DECAL,0,"RAMP. KEEP CLEAR. Painted on the pump room's wall, coming away from the stone at the edges.");
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
    /** Hangs a whole sign centred on {@code middle}, the middle block of its bottom row (of an even number, the one left of the middle). */
    public static void centred(ServerLevel l,BlockPos middle,Sign s,Direction facing){hang(l,middle.relative(BorrascaSigns.right(facing),-(s.cols-1)/2),s,facing);}
    /**
     * Hangs a whole sign, looking {@code facing}; {@code at} is the left end of its bottom row as someone facing it sees it. Every
     * block of it goes where there is nothing: a sign that would take the place of something else is a mistake in the builder, and says so.
     */
    public static void hang(ServerLevel l,BlockPos at,Sign s,Direction facing){
        for(int row=0;row<s.rows;row++)for(int col=0;col<s.cols;col++){
            var cell=cell(s,at,facing,col,row);
            if(!BuildBlocks.state(l,cell).isAir())throw new IllegalStateException("the "+s+" sign would hang where "+BuildBlocks.state(l,cell)+" is, at "+cell);
            BuildBlocks.set(l,cell,CrossingSignBlock.of(s,row*s.cols+col,facing),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);
        }
    }
}
