package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 0.4.451: the papers on the front of Drisking General. The owner's playtest notes of 10 November: "try to add the faces of the
 * actual children, especially if they're npcs from the vignette"; "Cluttered"; and "Have this whole front of house filled with lost
 * papers after vignette has ended – after the orrery has either been fed or stopped."
 *
 * <p>The front changes with the reader's own visits, in their own Drisking ({@link #stage}):
 * <ol>
 * <li>as built: the noticeboard, two blocks by two, one board under the town's own clutter;</li>
 * <li>Patrick's LOST sheet by the door, his own face on it;</li>
 * <li>Kendra's the other side of the door, and the board as it is years on;</li>
 * <li>the last night: both sheets gone yellow, and the last board;</li>
 * <li>after the machine has been fed or stopped: a sheet on every board of the front and on the glass of both windows, the board
 * covered, nothing hung across the doorway. The store's own tin sign and its bench are left as they are.</li>
 * </ol>
 * A stage is hung once, when the reader is in town and the front is loaded, and never taken down.
 *
 * <p>{@link Sheet} is the table tools/draw_borrasca_papers.py draws from; its --check compares the two.
 */
public final class BorrascaPapers {
    private BorrascaPapers(){}
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    /** A picture: how many blocks it covers, whether it is also taped to glass, and what a hand on it reads. */
    public enum Sheet {
        PATRICK(1,false,"LOST. Patrick Maddox. Last seen at Marker Two."),
        KENDRA(1,false,"LOST. Kendra Bell. Last seen on the ridge road."),
        PATRICK_YELLOWED(1,false,"LOST. Patrick Maddox. The paper has gone yellow and a corner is off it."),
        KENDRA_YELLOWED(1,false,"LOST. Kendra Bell. The paper has gone yellow."),
        WALL_PATRICK(1,true,"LOST. Patrick Maddox. Last seen at Marker Two."),
        WALL_KENDRA(1,true,"LOST. Kendra Bell. Last seen on the ridge road."),
        WALL_KELLY(1,true,"LOST. Kelly Garrity. CLOSED is written across the bottom in marker."),
        WALL_DANA(1,true,"LOST. Dana Pruitt. The sun has taken most of it."),
        WALL_KAYLA(1,true,"LOST. Kayla. The paper is new."),
        WALL_SCHOOL(1,true,"LOST. A school photograph. The name has worn away."),
        WALL_SQUINT(1,true,"LOST. A boy squinting at whoever held the camera. The corner with his name on it is gone."),
        WALL_PENCIL(1,true,"LOST. Someone went over the name in pencil when the ink faded. The pencil has faded too."),
        WALL_HOLES(1,true,"LOST. The corners are all staple holes. It has been taken down and put back up more than once."),
        WALL_BLEACHED(1,true,"LOST. The photograph has bleached into the paper. The telephone number under it has four figures."),
        WALL_CUT(1,true,"LOST, and a name. The photograph has been cut out with a blade."),
        WALL_TORN(1,true,"The top of a sheet: LOST, and two staples."),
        WALL_TWO(1,true,"Two sheets on the same staples. LOST, twice."),
        WALL_CORNERS(1,true,"Corners. There are more sheets under these than show."),
        BOARD(4,false,""),
        BOARD_THIRD(4,false,""),
        BOARD_LAST(4,false,""),
        BOARD_COVERED(4,false,"");
        public final int pieces;public final boolean glass;
        /** What a hand on it reads; empty for the noticeboard, which opens as pages. */
        public final String look;
        Sheet(int pieces,boolean glass,String look){this.pieces=pieces;this.glass=glass;this.look=look;}
        public static final int TOTAL=Arrays.stream(values()).mapToInt(s->s.pieces).sum();
        /** The number of this sheet's first block among all of them. */
        public int first(){int n=0;for(var s:values()){if(s==this)return n;n+=s.pieces;}throw new IllegalStateException();}
        public static Sheet of(int piece){for(var s:values()){if(piece<s.pieces)return s;piece-=s.pieces;}throw new IllegalArgumentException("no such block of paper: "+piece);}
    }
    /** The squares of the papered front, in the order they are dealt along it. */
    static final Sheet[] WALL=Arrays.stream(Sheet.values()).filter(s->s.glass).toArray(Sheet[]::new);

    /** The front's own plane, a block out from its boards on the porch side: from the deck to under the porch roof, end to end. */
    public static final int FRONT_X=-15,SOUTH=-17,NORTH=-29,FOOT=0,HEAD=3;
    /** The door stands in this column; nothing is hung across it. */
    public static final int DOOR_Z=-23;
    public static final int AS_BUILT=1,PATRICK=2,KENDRA=3,LAST=4,PAPERED=5;

    /** How far the front has got for a reader on this visit; {@code ended} once the machine has been fed or stopped. */
    public static int stage(int visit,boolean ended){return ended?PAPERED:Math.max(AS_BUILT,Math.min(LAST,visit));}
    public static Sheet board(int stage){return stage>=PAPERED?Sheet.BOARD_COVERED:stage==LAST?Sheet.BOARD_LAST:stage==KENDRA?Sheet.BOARD_THIRD:Sheet.BOARD;}
    /** The quarter of a board's picture that hangs in one of {@link BorrascaRooms#BOARD}'s cells: a reader on the porch faces west, so their left is south. */
    public static BlockState boardPiece(Sheet board,BlockPos cell){
        int south=Arrays.stream(BorrascaRooms.BOARD).mapToInt(BlockPos::getZ).max().orElseThrow(),top=Arrays.stream(BorrascaRooms.BOARD).mapToInt(BlockPos::getY).max().orElseThrow();
        return BorrascaPaperBlock.of(board,(top-cell.getY())*2+(south-cell.getZ()),Direction.EAST,false);
    }
    /** Which square of the papered front hangs at a height and a place along it: Patrick's and Kendra's where their sheets were, and no square beside itself. */
    public static Sheet wall(int y,int z){
        if(y==BorrascaRooms.ELI_POSTER.getY()&&z==BorrascaRooms.ELI_POSTER.getZ())return Sheet.WALL_PATRICK;
        if(y==BorrascaRooms.WREN_POSTER.getY()&&z==BorrascaRooms.WREN_POSTER.getZ())return Sheet.WALL_KENDRA;
        int k=Math.floorMod(z*5+y*3,WALL.length);
        for(int[] d:new int[][]{{0,1},{0,-1},{1,0},{-1,0}}){
            int ny=y+d[0],nz=z+d[1];
            boolean kept=ny==BorrascaRooms.ELI_POSTER.getY()&&(nz==BorrascaRooms.ELI_POSTER.getZ()||nz==BorrascaRooms.WREN_POSTER.getZ());
            if(kept&&WALL[k]==wall(ny,nz))k=(k+7)%WALL.length;
        }
        return WALL[k];
    }

    /** Whether the whole front is there to be hung on. */
    static boolean loaded(ServerLevel l,BlockPos b){
        for(int z:new int[]{SOUTH,NORTH})for(int x:new int[]{FRONT_X,FRONT_X-1})if(!l.hasChunkAt(BorrascaRooms.at(b,x,FOOT,z)))return false;
        return true;
    }
    /** Hang the front as it is at a stage. Each stage holds what the ones before it hung. */
    static void hang(ServerLevel l,BlockPos b,int stage){
        var board=board(stage);
        for(var cell:BorrascaRooms.BOARD)l.setBlock(BorrascaRooms.at(b,cell),boardPiece(board,cell),F);
        if(stage>=PAPERED){paper(l,b);return;}
        if(stage>=PATRICK)l.setBlock(BorrascaRooms.at(b,BorrascaRooms.ELI_POSTER),BorrascaPaperBlock.of(stage>=LAST?Sheet.PATRICK_YELLOWED:Sheet.PATRICK,0,Direction.EAST,false),F);
        if(stage>=KENDRA)l.setBlock(BorrascaRooms.at(b,BorrascaRooms.WREN_POSTER),BorrascaPaperBlock.of(stage>=LAST?Sheet.KENDRA_YELLOWED:Sheet.KENDRA,0,Direction.EAST,false),F);
    }
    /** Whether a sheet can be hung in a cell of the front: the cell is empty (or holds a sheet already) and boards or a pane stand behind it. */
    public static boolean holds(ServerLevel l,BlockPos b,int y,int z){
        if(z==DOOR_Z&&y<=FOOT+1||Arrays.asList(BorrascaRooms.BOARD).contains(new BlockPos(FRONT_X,y,z)))return false;
        var here=l.getBlockState(BorrascaRooms.at(b,FRONT_X,y,z));
        if(!here.isAir()&&!BorrascaPaperBlock.is(here))return false;
        var at=BorrascaRooms.at(b,FRONT_X-1,y,z);var behind=l.getBlockState(at);
        return behind.is(Blocks.GLASS_PANE)||behind.isFaceSturdy(l,at,Direction.EAST);
    }
    private static void paper(ServerLevel l,BlockPos b){
        for(int z=SOUTH;z>=NORTH;z--)for(int y=FOOT;y<=HEAD;y++){
            if(!holds(l,b,y,z))continue;
            boolean glass=l.getBlockState(BorrascaRooms.at(b,FRONT_X-1,y,z)).is(Blocks.GLASS_PANE);
            l.setBlock(BorrascaRooms.at(b,FRONT_X,y,z),BorrascaPaperBlock.of(wall(y,z),0,Direction.EAST,glass),F);
        }
    }
}
