package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import static io.github.knaitoe.theoldesthouse.labyrinth.DareFittingBlock.Kind.*;

/**
 * 0.4.173, "Third Floor Girls'": the school's third-floor hall, lockers down one side, and the girls' bathroom off it behind
 * a heavy wooden door. In the bathroom a wide mirror hangs over four sinks with a worn mat before the middle two, and the
 * stalls stand behind the reader's shoulder, the first one shut. Every position is relative to the scene's base.
 */
public final class DareRooms {
    private DareRooms(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.DARE_IN_THE_DARK;
    public static boolean is(LabyrinthPlace p){return p==PLACE;}
    /** The hall runs north from the entry, x -2..2, to the stairwell door in its far wall. */
    public static final int HALL_X0=-2,HALL_X1=2,HALL_Z1=0,HALL_Z0=-26,EXIT_Z=-27;
    /** The bathroom door's lower half, in the hall's east wall, and the passage behind it through the bathroom's own wall. */
    public static final BlockPos DOOR=new BlockPos(3,0,-13),PASSAGE=new BlockPos(4,0,-13);
    /** The bathroom's inside: x 5..12, z -18..-9, three high. */
    public static final int BATH_X0=5,BATH_X1=12,BATH_Z0=-18,BATH_Z1=-9;
    /** The mirror's lower left block, as one faces it: four across and two high over the sinks, facing south. */
    public static final BlockPos MIRROR=new BlockPos(7,1,-18);
    public static final Direction MIRROR_FACING=Direction.SOUTH;
    /** The worn mat in front of the middle two sinks. */
    public static final BlockPos MAT=new BlockPos(8,0,-17);
    /** The stall that stays shut, over the reader's left shoulder in the glass. */
    public static final BlockPos SHUT_STALL=new BlockPos(6,0,-12);
    /** Where the two kids stand either side of the door, and where their sheets are left. */
    public static final BlockPos KID_A=new BlockPos(1,0,-11),KID_B=new BlockPos(1,0,-15);
    /** The folded notebook square on the floor by the door, and the account on the first sheet. */
    public static final BlockPos NOTE=new BlockPos(2,0,-12),ACCOUNT=KID_A;
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    private static Block mat(String name){return DareRegistry.material(name);}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,Block block){NovelRooms.box(l,b,x0,y0,z0,x1,y1,z1,block.defaultBlockState());}
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void fit(ServerLevel l,BlockPos b,int x,int y,int z,DareFittingBlock.Kind k,Direction f){set(l,b,x,y,z,DareFittingBlock.of(k,f));}
    private static void tall(ServerLevel l,BlockPos b,int x,int z,DareFittingBlock.Kind k,Direction f){set(l,b,x,0,z,DareFittingBlock.of(k,f,0));set(l,b,x,1,z,DareFittingBlock.of(k,f,1));}

    public static void build(ServerLevel l,BlockPos b){
        // The room's whole box is painted block wall, then the hall and the bathroom are carved out of it.
        box(l,b,-4,-2,-29,14,5,0,mat("wall"));
        // The hall: vinyl tile, acoustic ceiling, the walls' darker band to shoulder height.
        box(l,b,HALL_X0,-1,HALL_Z0,HALL_X1,-1,HALL_Z1,mat("hall_tile"));box(l,b,HALL_X0,4,HALL_Z0,HALL_X1,4,HALL_Z1,mat("ceiling"));
        box(l,b,HALL_X0,0,HALL_Z0,HALL_X1,3,HALL_Z1,Blocks.AIR);
        box(l,b,-3,0,HALL_Z0,-3,1,HALL_Z1,mat("wall_band"));box(l,b,3,0,HALL_Z0,3,1,HALL_Z1,mat("wall_band"));box(l,b,HALL_X0,0,EXIT_Z,HALL_X1,1,EXIT_Z,mat("wall_band"));
        box(l,b,-3,2,HALL_Z0,-3,2,HALL_Z1,mat("trim"));box(l,b,3,2,HALL_Z0,3,2,HALL_Z1,mat("trim"));box(l,b,HALL_X0,2,EXIT_Z,HALL_X1,2,EXIT_Z,mat("trim"));
        // Lockers down the west wall, broken by two classroom doors and the trophy case.
        for(int z=HALL_Z1-1;z>=HALL_Z0+1;z--){
            if(z==-6||z==-19)tall(l,b,-3,z,CLASSROOM_DOOR,Direction.EAST);
            else if(z==-12||z==-13)tall(l,b,-3,z,TROPHY,Direction.EAST);
            else tall(l,b,-3,z,LOCKER,Direction.EAST);
        }
        // The east wall: two more classrooms, the bulletin board, the clock, the drinking fountain, a poster and the GIRLS plate.
        tall(l,b,3,-3,CLASSROOM_DOOR,Direction.WEST);tall(l,b,3,-23,CLASSROOM_DOOR,Direction.WEST);
        set(l,b,2,1,-8,DareFittingBlock.of(BULLETIN,Direction.WEST,0));set(l,b,2,1,-7,DareFittingBlock.of(BULLETIN,Direction.WEST,1));
        fit(l,b,2,2,-10,CLOCK,Direction.WEST);fit(l,b,2,0,-17,FOUNTAIN,Direction.WEST);fit(l,b,2,1,-25,POSTER,Direction.WEST);
        fit(l,b,-2,2,-9,POSTER,Direction.EAST);fit(l,b,2,2,DOOR.getZ(),PLAQUE,Direction.WEST);
        for(int z:new int[]{-4,-13,-22})fit(l,b,0,3,z,HALL_LIGHT,Direction.NORTH);
        // The bathroom, behind its own wall: hex tile, glazed walls, a lower ceiling.
        box(l,b,BATH_X0-1,-1,BATH_Z0-1,BATH_X1+1,3,BATH_Z1+1,mat("bath_wall"));
        box(l,b,BATH_X0,-1,BATH_Z0,BATH_X1,-1,BATH_Z1,mat("bath_floor"));box(l,b,BATH_X0,3,BATH_Z0,BATH_X1,3,BATH_Z1,mat("ceiling"));
        box(l,b,BATH_X0,0,BATH_Z0,BATH_X1,2,BATH_Z1,Blocks.AIR);
        // The door in the hall wall and the passage through the bathroom's wall behind it.
        box(l,b,PASSAGE.getX(),0,PASSAGE.getZ(),PASSAGE.getX(),1,PASSAGE.getZ(),Blocks.AIR);set(l,b,PASSAGE.getX(),-1,PASSAGE.getZ(),mat("bath_floor").defaultBlockState());
        NovelRooms.door(l,b.offset(DOOR),Direction.EAST,DareRegistry.BATHROOM_DOOR.get(),true);
        // Four sinks on the north wall, the mirror over them, the mat before the middle two.
        for(int x=MIRROR.getX();x<MIRROR.getX()+DareMirrorBlock.WIDTH;x++){
            fit(l,b,x,0,BATH_Z0,SINK,Direction.SOUTH);
            for(int row=0;row<DareMirrorBlock.HEIGHT;row++)set(l,b,x,MIRROR.getY()+row,MIRROR.getZ(),DareMirrorBlock.of(MIRROR_FACING,x-MIRROR.getX(),row));
        }
        fit(l,b,MAT.getX(),0,MAT.getZ(),BATHMAT,Direction.NORTH);fit(l,b,MAT.getX()+1,0,MAT.getZ(),BATHMAT,Direction.SOUTH);
        // The stalls along the south wall: fronts at z -12, partitions between them, the first door shut and the others open.
        for(int x=BATH_X0;x<=BATH_X1;x++){
            if(x==SHUT_STALL.getX())tall(l,b,x,SHUT_STALL.getZ(),STALL_DOOR,Direction.NORTH);
            else if(x!=8&&x!=11)tall(l,b,x,-12,STALL_PANEL,Direction.NORTH);
        }
        for(int x:new int[]{7,10})for(int z=-11;z<=BATH_Z1;z++)tall(l,b,x,z,PARTITION,Direction.NORTH);
        for(int x:new int[]{6,8,11})fit(l,b,x,0,BATH_Z1,TOILET,Direction.NORTH);
        // Towels and a bin by the door, the dead ceiling light, and the one frosted window, black with night.
        fit(l,b,BATH_X0,1,-16,TOWELS,Direction.EAST);fit(l,b,BATH_X0,0,-15,BIN,Direction.EAST);
        fit(l,b,9,2,-14,BATH_LIGHT,Direction.NORTH);fit(l,b,BATH_X1,1,-15,WINDOW,Direction.WEST);
        // The folded square by the door.
        fit(l,b,NOTE.getX(),NOTE.getY(),NOTE.getZ(),DareFittingBlock.Kind.NOTE,Direction.NORTH);
    }

    // ---------------------------------------------------------------------------------------------- where the reader is
    public static boolean inBathroom(BlockPos b,Vec3 at){
        double x=at.x-b.getX(),z=at.z-b.getZ();return x>=BATH_X0-1&&x<=BATH_X1+1&&z>=BATH_Z0&&z<=BATH_Z1+1;
    }
    public static boolean inHall(BlockPos b,Vec3 at){double x=at.x-b.getX(),z=at.z-b.getZ();return x>=HALL_X0&&x<HALL_X1+1&&z>=HALL_Z0&&z<HALL_Z1+1;}
    /** On the mat: standing anywhere over it, up to the basins. */
    public static boolean onMat(BlockPos b,Vec3 feet){
        double x=feet.x-b.getX(),y=feet.y-b.getY(),z=feet.z-b.getZ();
        return x>=MAT.getX()-.3&&x<=MAT.getX()+2.3&&z>=MAT.getZ()-.65&&z<=MAT.getZ()+1.4&&y>-.2&&y<.6;
    }
    /** The plane of the glass, a hair in front of the wall plate, and the glass's extent on it. */
    public static double glassZ(BlockPos b){return b.getZ()+MIRROR.getZ()+.75/16;}
    public static AABB glass(BlockPos b){var lo=b.offset(MIRROR);return new AABB(lo.getX()+1/16.,lo.getY()+1/16.,glassZ(b),lo.getX()+DareMirrorBlock.WIDTH-1/16.,lo.getY()+DareMirrorBlock.HEIGHT-1/16.,glassZ(b));}
    public static BlockPos door(BlockPos b){return b.offset(DOOR);}
    public static boolean doorOpen(ServerLevel l,BlockPos b){var s=l.getBlockState(door(b));return !(s.getBlock() instanceof DoorBlock)||s.getValue(DoorBlock.OPEN);}
}
