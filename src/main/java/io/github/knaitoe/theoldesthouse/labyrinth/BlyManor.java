package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.ManorBlock.Kind;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

/**
 * Bly, rebuilt (0.4.105): the ground floor of a manor at night. The front door gives on to a flagged hall with a wet mat, the
 * hall on to a panelled corridor whose boards are wet down one narrow line, and the corridor on to the glazed conservatory
 * round the pond: the gardener's "door, corridor, room with the water". Off the corridor are the drawing room (the one fire),
 * the library, the schoolroom with the girl's dolls' house, and the children's room. Three linen cupboards stand in each
 * corridor wall, their catches on the inside.
 * <p>The story's own places are kept: her lane along x 0 from the hall to the pond, the cupboard doors at x ±4 and z -9/-20/-31,
 * the pond's water at x -3..3, z -50..-40, and the gardener's note and the account on his bench at x 8. Polish, composition and
 * review passes do not touch it.
 */
public final class BlyManor {
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE|Block.UPDATE_SUPPRESS_DROPS;
    /** The cupboards' door rows; a pair of cupboards, one each side, stands at every one. */
    public static final int[] CUPBOARDS={-9,-20,-31};
    public static final BlockPos NOTE=new BlockPos(8,1,-36),ACCOUNT=new BlockPos(8,1,-38);
    private BlyManor(){}

    /** The door of the cupboard a reader stands in, or null when they are not inside one. */
    public static @Nullable BlockPos cupboardDoor(BlockPos rel){
        int ax=Math.abs(rel.getX());if(ax<5||ax>7||rel.getY()<0||rel.getY()>2)return null;
        for(int zz:CUPBOARDS)if(Math.abs(rel.getZ()-zz)<=1)return new BlockPos(rel.getX()<0?-4:4,0,zz);
        return null;
    }

    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void fill(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){for(int x=x0;x<=x1;x++)for(int y=y0;y<=y1;y++)for(int z=z0;z<=z1;z++)set(l,b,x,y,z,s);}
    private static BlockState of(Kind k){return ManorBlock.of(k,Direction.NORTH);}
    private static BlockState of(Kind k,Direction f){return ManorBlock.of(k,f);}
    private static BlockState of(Kind k,Direction f,int stage){return ManorBlock.of(k,f,stage);}
    private static void furniture(ServerLevel l,BlockPos b,int x,int y,int z,HouseholdFurnitureBlock.Kind k,Direction f){NovelRooms.furniture(l,b.offset(x,y,z),k,f);}
    /** Facing away from the wall it hangs on, for whatever is mounted at {@code x} beside a wall at {@code x±1}. */
    private static Direction off(int wallSide){return wallSide>0?Direction.WEST:Direction.EAST;}
    private static BlockState log(){return Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState();}

    /** A panelled room: wainscot to the rail, damask above, a plaster cornice and ceiling. Walls on the perimeter. */
    private static void room(ServerLevel l,BlockPos b,int x0,int x1,int z0,int z1,BlockState floor){
        for(int x=x0;x<=x1;x++)for(int z=z0;z<=z1;z++){boolean edge=x==x0||x==x1||z==z0||z==z1;
            set(l,b,x,-1,z,floor);set(l,b,x,5,z,of(Kind.PLASTER));
            for(int y=0;y<=4;y++)set(l,b,x,y,z,!edge?Blocks.AIR.defaultBlockState():y<=1?of(Kind.WAINSCOT):y<=3?of(Kind.DAMASK):of(Kind.CORNICE));}
    }
    private static void opening(ServerLevel l,BlockPos b,int x,int z0,int z1,boolean alongZ){
        // A doorway in a wall plane: two jambs and a lintel in stripped oak, open three high.
        if(alongZ){for(int z=z0;z<=z1;z++)fill(l,b,x,0,z,x,2,z,Blocks.AIR.defaultBlockState());fill(l,b,x,0,z0-1,x,3,z0-1,log());fill(l,b,x,0,z1+1,x,3,z1+1,log());fill(l,b,x,3,z0,x,3,z1,log());}
        else{for(int xx=z0;xx<=z1;xx++)fill(l,b,xx,0,x,xx,3,x,Blocks.AIR.defaultBlockState());fill(l,b,z0-1,0,x,z0-1,4,x,log());fill(l,b,z1+1,0,x,z1+1,4,x,log());fill(l,b,z0,4,x,z1,4,x,log());}
    }
    private static void bed(ServerLevel l,BlockPos b,int x,int z,Direction toHead,Block bed){
        var foot=bed.defaultBlockState().setValue(BedBlock.FACING,toHead).setValue(BedBlock.PART,BedPart.FOOT);
        set(l,b,x,0,z,foot);set(l,b,x+toHead.getStepX(),0,z+toHead.getStepZ(),foot.setValue(BedBlock.PART,BedPart.HEAD));
    }
    private static void lantern(ServerLevel l,BlockPos b,int x,int y,int z){set(l,b,x,y+1,z,Blocks.CHAIN.defaultBlockState().setValue(ChainBlock.AXIS,Direction.Axis.Y));set(l,b,x,y,z,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));}

    public static void build(ServerLevel l,BlockPos b){
        var air=Blocks.AIR.defaultBlockState();
        // The house's brick first; every room is cut out of it.
        fill(l,b,-13,-5,-54,13,10,0,Blocks.BRICKS.defaultBlockState());
        // Rooms: the hall, the corridor, the two wings each side of it.
        room(l,b,-6,6,-6,0,of(Kind.FLAGS));
        room(l,b,-13,-4,-35,-6,of(Kind.BOARDS));room(l,b,4,13,-35,-6,of(Kind.BOARDS));
        room(l,b,-4,4,-35,-6,of(Kind.BOARDS));
        // The wings are each two rooms; the partition runs from the outer wall to the middle cupboard.
        for(int x:new int[]{-12,-11,-10,-9,9,10,11,12})for(int y=0;y<=4;y++)set(l,b,x,y,-20,y<=1?of(Kind.WAINSCOT):y<=3?of(Kind.DAMASK):of(Kind.CORNICE));
        // Her line: wet from the front door to the pond.
        for(int z=-1;z>=-5;z--)set(l,b,0,-1,z,of(Kind.WET_FLAGS));
        for(int z=-6;z>=-34;z--)set(l,b,0,-1,z,of(Kind.WET_BOARDS));
        opening(l,b,-6,-2,2,false);
        for(int s:new int[]{-1,1}){
            // Doorways from the corridor into the front and back rooms of each wing.
            opening(l,b,4*s,-15,-14,true);opening(l,b,4*s,-26,-25,true);
            for(int zz:CUPBOARDS)cupboard(l,b,s,zz);
        }
        hall(l,b);corridor(l,b);drawingRoom(l,b);library(l,b);schoolroom(l,b);nursery(l,b);conservatory(l,b);
        // Into the house from the House: the front door's opening through the hall's wall.
        set(l,b,0,0,0,air);set(l,b,0,1,0,air);
    }

    private static void cupboard(ServerLevel l,BlockPos b,int s,int zz){
        var air=Blocks.AIR.defaultBlockState();int door=4*s,back=8*s;
        // A panelled press standing into the wing; inside, three cells deep, three wide and three high, in the dark.
        for(int x=Math.min(door,back);x<=Math.max(door,back);x++)for(int z=zz-2;z<=zz+2;z++){
            set(l,b,x,-1,z,of(Kind.BOARDS));for(int y=0;y<=3;y++)set(l,b,x,y,z,of(Kind.WAINSCOT));}
        fill(l,b,Math.min(5*s,7*s),0,zz-1,Math.max(5*s,7*s),2,zz+1,air);
        fill(l,b,Math.min(5*s,7*s),3,zz-1,Math.max(5*s,7*s),3,zz+1,of(Kind.PLASTER));
        // The corridor's side of the wall goes back to damask above the press.
        for(int z=zz-2;z<=zz+2;z++){set(l,b,door,2,z,of(Kind.DAMASK));set(l,b,door,3,z,of(Kind.DAMASK));set(l,b,door,4,z,of(Kind.CORNICE));}
        NovelRooms.door(l,b.offset(door,0,zz),Direction.EAST,Blocks.DARK_OAK_DOOR,false);
        fill(l,b,door,0,zz-1,door,2,zz-1,log());fill(l,b,door,0,zz+1,door,2,zz+1,log());set(l,b,door,2,zz,log());
        // Shelves of linen in the back corners, and the catch on the inside, beside the door.
        for(int y=0;y<=1;y++)for(int dz:new int[]{-1,1})set(l,b,7*s,y,zz+dz,of(Kind.LINEN,off(s)));
        set(l,b,5*s,1,zz+1,of(Kind.CATCH,off(-s)));
    }

    private static void hall(ServerLevel l,BlockPos b){
        set(l,b,0,0,-1,of(Kind.DOORMAT));
        set(l,b,-5,0,-3,of(Kind.CLOCK_LOW,Direction.EAST));set(l,b,-5,1,-3,of(Kind.CLOCK_HIGH,Direction.EAST));
        set(l,b,5,0,-2,of(Kind.UMBRELLA));
        furniture(l,b,5,0,-4,HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.WEST);
        set(l,b,5,2,-4,of(Kind.PORTRAIT,Direction.WEST,3));
        set(l,b,-5,2,-1,of(Kind.SCONCE,Direction.EAST));set(l,b,5,2,-1,of(Kind.SCONCE,Direction.WEST));
    }

    private static void corridor(ServerLevel l,BlockPos b){
        for(int side:new int[]{-3,3}){var face=off(side>0?1:-1);
            for(int z:new int[]{-12,-17,-23,-28})set(l,b,side,2,z,of(Kind.SCONCE,face));
            set(l,b,side,2,-33,of(Kind.PORTRAIT,face,side<0?0:3));}
        set(l,b,3,0,-22,Blocks.POTTED_FERN.defaultBlockState());
        furniture(l,b,-3,0,-18,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.EAST);
    }

    private static void drawingRoom(ServerLevel l,BlockPos b){
        // The house's one fire, in a brick chimney breast on the outer wall, with a marble shelf and her portrait over it.
        var brick=Blocks.BRICKS.defaultBlockState();
        fill(l,b,-12,0,-12,-12,1,-12,brick);fill(l,b,-12,0,-14,-12,1,-14,brick);set(l,b,-12,1,-13,brick);fill(l,b,-12,2,-14,-12,4,-12,brick);
        set(l,b,-12,0,-13,Blocks.CAMPFIRE.defaultBlockState().setValue(net.minecraft.world.level.block.CampfireBlock.FACING,Direction.EAST));
        set(l,b,-11,1,-13,of(Kind.MANTEL,Direction.EAST));set(l,b,-11,3,-13,of(Kind.PORTRAIT,Direction.EAST,3));
        furniture(l,b,-10,0,-12,HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.WEST);furniture(l,b,-10,0,-14,HouseholdFurnitureBlock.Kind.FLORAL_ARMCHAIR,Direction.WEST);
        fill(l,b,-9,0,-15,-7,0,-12,of(Kind.RUG));
        furniture(l,b,-11,0,-18,HouseholdFurnitureBlock.Kind.BLUE_SOFA,Direction.NORTH);furniture(l,b,-10,0,-18,HouseholdFurnitureBlock.Kind.BLUE_SOFA,Direction.NORTH);
        furniture(l,b,-9,0,-18,HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.NORTH);
        for(int z:new int[]{-9,-17})fill(l,b,-13,1,z,-13,2,z,Blocks.BLACK_STAINED_GLASS.defaultBlockState());
        for(int z:new int[]{-10,-16})set(l,b,-12,2,z,of(Kind.SCONCE,Direction.EAST));
    }

    private static void library(ServerLevel l,BlockPos b){
        fill(l,b,-12,0,-32,-12,3,-23,Blocks.BOOKSHELF.defaultBlockState());
        furniture(l,b,-9,0,-27,HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.EAST);furniture(l,b,-10,0,-27,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.EAST);
        furniture(l,b,-10,0,-32,HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.EAST);
        fill(l,b,-11,0,-26,-9,0,-24,of(Kind.RUG));
        set(l,b,-5,2,-27,of(Kind.PORTRAIT,Direction.WEST,0));
        set(l,b,-5,2,-23,of(Kind.SCONCE,Direction.WEST));set(l,b,-11,2,-34,of(Kind.SCONCE,Direction.SOUTH));
    }

    private static void schoolroom(ServerLevel l,BlockPos b){
        for(int z=-14;z<=-12;z++)set(l,b,12,1,z,of(Kind.BLACKBOARD,Direction.WEST));
        for(int z:new int[]{-12,-14}){set(l,b,9,0,z,of(Kind.SCHOOL_DESK,Direction.EAST));furniture(l,b,8,0,z,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.EAST);}
        set(l,b,12,0,-17,of(Kind.DOLLHOUSE,Direction.WEST));set(l,b,11,0,-8,of(Kind.GLOBE));
        fill(l,b,8,0,-17,10,0,-15,of(Kind.RUG));
        set(l,b,5,2,-12,of(Kind.PORTRAIT,Direction.EAST,1));set(l,b,5,2,-17,of(Kind.PORTRAIT,Direction.EAST,2));
        set(l,b,12,2,-9,of(Kind.SCONCE,Direction.WEST));
        fill(l,b,13,1,-10,13,2,-10,Blocks.BLACK_STAINED_GLASS.defaultBlockState());
    }

    private static void nursery(ServerLevel l,BlockPos b){
        bed(l,b,11,-24,Direction.EAST,Blocks.LIGHT_BLUE_BED);bed(l,b,11,-28,Direction.EAST,Blocks.PINK_BED);
        furniture(l,b,12,0,-26,HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.WEST);
        furniture(l,b,9,0,-33,HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.NORTH);
        fill(l,b,9,0,-27,11,0,-25,of(Kind.RUG));
        set(l,b,5,2,-27,of(Kind.PORTRAIT,Direction.EAST,0));set(l,b,12,2,-26,of(Kind.SCONCE,Direction.WEST));
        fill(l,b,13,1,-31,13,2,-31,Blocks.BLACK_STAINED_GLASS.defaultBlockState());
    }

    private static void conservatory(ServerLevel l,BlockPos b){
        var air=Blocks.AIR.defaultBlockState();var night=Blocks.BLACK_CONCRETE.defaultBlockState();
        var post=Blocks.QUARTZ_PILLAR.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.Y);
        // The back of the house is brick; the conservatory is glass on painted iron, with the night behind it.
        fill(l,b,-13,0,-54,13,8,-36,night);
        fill(l,b,-12,-1,-53,12,-1,-36,of(Kind.FLAGS));
        fill(l,b,-11,0,-52,11,6,-36,air);
        for(int z=-53;z<=-36;z++)for(int x:new int[]{-12,12}){set(l,b,x,0,z,Blocks.BRICKS.defaultBlockState());for(int y=1;y<=6;y++)set(l,b,x,y,z,of(Kind.GLAZING,x<0?Direction.EAST:Direction.WEST));}
        for(int x=-11;x<=11;x++){set(l,b,x,0,-53,Blocks.BRICKS.defaultBlockState());for(int y=1;y<=6;y++)set(l,b,x,y,-53,of(Kind.GLAZING,Direction.NORTH));}
        for(int z=-52;z<=-36;z+=4)for(int x:new int[]{-12,12})fill(l,b,x,0,z,x,6,z,post);
        for(int x=-12;x<=12;x+=4)fill(l,b,x,0,-53,x,6,-53,post);
        fill(l,b,-12,7,-53,12,7,-35,Blocks.QUARTZ_BLOCK.defaultBlockState());
        fill(l,b,-11,7,-52,11,7,-36,of(Kind.GLAZING,Direction.NORTH,1));
        for(int x=-11;x<=11;x+=4)fill(l,b,x,7,-52,x,7,-36,Blocks.QUARTZ_BLOCK.defaultBlockState());
        // The house wall it leans on, with a wide arch from the corridor.
        for(int x=-12;x<=12;x++)for(int y=0;y<=6;y++)set(l,b,x,y,-35,Blocks.BRICKS.defaultBlockState());
        fill(l,b,-2,0,-35,2,3,-35,air);set(l,b,0,-1,-35,of(Kind.WET_FLAGS));for(int x:new int[]{-2,-1,1,2})set(l,b,x,-1,-35,of(Kind.FLAGS));
        fill(l,b,-3,0,-35,-3,4,-35,post);fill(l,b,3,0,-35,3,4,-35,post);fill(l,b,-2,4,-35,2,4,-35,Blocks.QUARTZ_BLOCK.defaultBlockState());
        // The pond: a mossy kerb flush with the flags, dark water four deep over mud, weed, and pads off her line.
        var kerb=Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
        fill(l,b,-4,-5,-51,4,-1,-39,kerb);fill(l,b,-3,-4,-50,3,-1,-40,Blocks.WATER.defaultBlockState());fill(l,b,-3,-5,-50,3,-5,-40,Blocks.MUD.defaultBlockState());
        for(int z=-36;z>=-39;z--)set(l,b,0,-1,z,of(Kind.WET_FLAGS));
        for(var at:new int[][]{{-2,-42},{2,-43},{-3,-46},{3,-49},{-2,-50}})set(l,b,at[0],-4,at[1],Blocks.SEAGRASS.defaultBlockState());
        for(var at:new int[][]{{2,-44},{-2,-45},{-3,-48},{3,-41}})set(l,b,at[0],0,at[1],Blocks.LILY_PAD.defaultBlockState());
        // The gardener's bench, his note and, once a reader's wait is done, their account.
        for(int z=-36;z>=-38;z--)set(l,b,8,0,z,of(Kind.BENCH,Direction.WEST));
        set(l,b,NOTE.getX(),NOTE.getY(),NOTE.getZ(),of(Kind.NOTE,Direction.WEST));set(l,b,8,1,-37,Blocks.POTTED_DEAD_BUSH.defaultBlockState());
        // Wicker chairs looking at the water; pots along the glass; two lanterns.
        furniture(l,b,6,0,-44,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.WEST);furniture(l,b,6,0,-46,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.WEST);
        for(var at:new int[][]{{-10,-38},{10,-41},{-10,-50},{10,-51},{-7,-52},{7,-52},{-10,-44}})
            set(l,b,at[0],0,at[1],(at[0]+at[1])%3==0?Blocks.POTTED_FERN.defaultBlockState():(at[0]+at[1])%3==1?Blocks.POTTED_AZALEA.defaultBlockState():Blocks.POTTED_FLOWERING_AZALEA.defaultBlockState());
        for(int x:new int[]{-6,6})for(int z:new int[]{-42,-49})lantern(l,b,x,5,z);
    }
}
