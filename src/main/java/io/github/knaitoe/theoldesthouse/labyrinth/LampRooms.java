package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

/**
 * 0.4.199, Borrowed Light's deep room: the lamp rooms. The owner: "The light passes through several adjoining rooms. An
 * interior window shows where it needs to go, but reaching the next mirror requires taking a different route. The challenge
 * is understanding how the rooms connect." And: "A deeper version could later make the player recognize the arrangement
 * through a window long before they understand how to reach it."
 *
 * Four dim rooms round a cross of walls, off a narrow hall from the door. In the first, a stage lamp behind its shutter and
 * a dressing mirror on a pivot; in its north wall a leaded window, and through it, in the room beyond, a second mirror. That
 * room has no door from the first: it is reached the long way, through the east room, into the far room and back west
 * through its door. In the wall between those two another window shows the second mirror from the third, and the brass sun
 * on the far room's north wall beside a door with no handle. Turn each mirror's glass (an eighth at a time; these mirrors
 * are bolted down) until the beam goes lamp, window, mirror, window, mirror, sun. The same rules as Borrowed Light.
 */
public final class LampRooms {
    public static final LabyrinthPlace PLACE=LabyrinthPlace.LAMP_ROOMS;
    public static final BlockPos LAMP=new BlockPos(-11,1,-6),SHUTTER=new BlockPos(-10,1,-6),SUN=new BlockPos(6,1,-28);
    /** The three mirrors, and the eighth of a turn each must stand at (facing clockwise from north, then turned). */
    public static final BlockPos[] MIRRORS={new BlockPos(-4,0,-6),new BlockPos(-4,0,-20),new BlockPos(6,0,-20)};
    public static final int[] AIM={7,3,7};
    /** The two interior windows, each two panes tall. */
    public static final BlockPos NORTH_WINDOW=new BlockPos(-4,1,-13),EAST_WINDOW=new BlockPos(0,1,-20);
    private static final Direction[] CW={Direction.NORTH,Direction.EAST,Direction.SOUTH,Direction.WEST};
    private static final int FLAGS=PuzzleRooms.FLAGS;
    private LampRooms(){}

    /** A mirror's eighth of a turn as a block: its facing and whether its glass is turned the further eighth. */
    public static net.minecraft.world.level.block.state.BlockState mirror(int eighth){
        int e=Math.floorMod(eighth,8);return BorrowedLightBlock.of(BorrowedLightBlock.Kind.MIRROR,CW[e/2]).setValue(BorrowedLightBlock.TURNED,e%2==1);
    }
    public static int eighth(net.minecraft.world.level.block.state.BlockState s){int f=0;for(int i=0;i<4;i++)if(CW[i]==s.getValue(BorrowedLightBlock.FACING))f=i;return 2*f+(s.getValue(BorrowedLightBlock.TURNED)?1:0);}
    /** How the world first sets each mirror: two to six eighths short of its aim. */
    public static int start(long seed,int i){return Math.floorMod(AIM[i]-2-(int)Math.floorMod(seed>>>(8*i+3),5L),8);}

    public static void build(ServerLevel l,BlockPos b){
        var box=PLACE.room();var wall=PuzzleRegistry.material("wall").defaultBlockState();var floor=PuzzleRegistry.material("floor").defaultBlockState();var ceiling=PuzzleRegistry.material("ceiling").defaultBlockState();
        PuzzleRooms.shell(l,b,box,wall,floor,ceiling);
        // The cross of walls: four rooms off the hall by the door.
        PuzzleRooms.fill(l,b,0,0,-2,0,5,-28,wall);PuzzleRooms.fill(l,b,-11,0,-13,11,5,-13,wall);
        // The way round: the east room into the far room, and the far room's door back west.
        PuzzleRooms.fill(l,b,6,0,-13,6,1,-13,Blocks.AIR.defaultBlockState());PuzzleRooms.fill(l,b,0,0,-26,0,1,-26,Blocks.AIR.defaultBlockState());
        for(int y=1;y<=2;y++){l.setBlock(b.offset(NORTH_WINDOW.atY(y)),BorrowedLightBlock.of(BorrowedLightBlock.Kind.WINDOW,Direction.NORTH),FLAGS);l.setBlock(b.offset(EAST_WINDOW.atY(y)),BorrowedLightBlock.of(BorrowedLightBlock.Kind.WINDOW,Direction.EAST),FLAGS);}
        boolean solved=PuzzleRooms.solved(l.getServer(),PLACE);
        l.setBlock(b.offset(LAMP),BorrowedLightBlock.of(BorrowedLightBlock.Kind.LAMP,Direction.EAST),FLAGS);
        l.setBlock(b.offset(SHUTTER),BorrowedLightBlock.of(BorrowedLightBlock.Kind.SHUTTER,Direction.EAST).setValue(BorrowedLightBlock.OPEN,solved),FLAGS);
        for(int i=0;i<MIRRORS.length;i++)l.setBlock(b.offset(MIRRORS[i]),mirror(solved?AIM[i]:start(l.getSeed(),i)),FLAGS);
        l.setBlock(b.offset(SUN),BorrowedLightBlock.of(BorrowedLightBlock.Kind.PLATE,Direction.SOUTH),FLAGS);
        for(int[] at:new int[][]{{-11,2,-10},{11,2,-6},{-11,2,-24},{11,2,-24},{-11,2,-1},{11,2,-1}}){var facing=at[0]<0?Direction.EAST:Direction.WEST;
            l.setBlock(b.offset(at[0],at[1],at[2]),BorrowedLightBlock.of(BorrowedLightBlock.Kind.SCONCE,facing),FLAGS);}
        LabyrinthBuilder.entrance(l,b,wall,floor,ceiling);
        LabyrinthBuilder.doors(l,b,PLACE);
        PuzzleRooms.ironDoor(l,b,PLACE,solved);
        trace(l,b);
    }
    public static BorrowedLight.Trace beam(ServerLevel l,BlockPos b){return BorrowedLight.beam(l,b.offset(LAMP),b.offset(SHUTTER));}
    public static BorrowedLight.Trace trace(ServerLevel l,BlockPos b){return BorrowedLight.trace(l,b,PLACE,b.offset(LAMP),b.offset(SHUTTER),b.offset(SUN));}
}
