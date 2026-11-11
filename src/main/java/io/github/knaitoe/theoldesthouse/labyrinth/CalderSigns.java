package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.labyrinth.BorrascaSigns.Mount;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;

/**
 * 0.4.464, Bike night: Calder Falls' painted signs, made as Drisking's are ({@link BorrascaSigns}): each a picture sixteen pixels to
 * the block and lettered in the picture (tools/draw_calder_signs.py), a large one a grid of {@link CalderSignBlock}s each showing its
 * own square. What a sign says in full, the lines too small to letter, is read by a hand on it ({@link Sign#look}). This slice, the
 * park's two; the town's come with the town.
 */
public final class CalderSigns {
    private CalderSigns(){}

    /** The signs, in the order the art tool draws them. A sign's blocks are numbered from {@link #first}, left to right and top row first. */
    public enum Sign {
        ARCH(6,1,Mount.FREE,0,"CALDER PARK, in cream letters on an iron board over the gate."),
        RULES(4,2,Mount.WALL,0,"PARK RULES. Shut at dusk. No cycling. Under that, in marker, in another hand: NO FUN.");
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
