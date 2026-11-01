package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import static io.github.knaitoe.theoldesthouse.labyrinth.WorkPrintBlock.Kind.*;

/**
 * 0.4.203, Work print: a rental shop on a closed street, after hours, and the rooms behind it. The reader comes in by the
 * shop's front door (the entry, at the base) into the shop: tape shelves down both walls and two runs back to back, the
 * returns bin under its slot by the door, posters, the counter across the back with the rental card, the phone and the
 * ledger on it, and the film's monster on a mannequin at its end. Behind the counter a door leads to the back room (the
 * TV cart with its two decks and the CRT, two folding chairs, a box of blank tapes); off that, the edit bay to the west (the
 * flatbed, the splicer, the light table, the reels, the continuity photographs, the script and the call sheet) and the
 * sound booth to the east. Through the edit bay is the set: the film's hallway, built of flats, with its bulb, its chalk
 * mark and the House's camera on a tripod, still pointed down it. Every position is relative to the base.
 */
public final class WorkPrintShop {
    private WorkPrintShop(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.WORK_PRINT;
    public static boolean is(LabyrinthPlace p){return p==PLACE;}
    /** The rental card on the counter (the source) and the shop's ledger (the account). */
    public static final BlockPos CARD=new BlockPos(-4,1,-10),LEDGER=new BlockPos(1,1,-10);
    public static final BlockPos PHONE=new BlockPos(-1,1,-10),RETURNS=new BlockPos(-3,0,-1),MANNEQUIN=new BlockPos(5,0,-11);
    /** The TV cart with its two decks, and the CRT on it; the box of blanks. */
    public static final BlockPos CART=new BlockPos(0,0,-21),CRT=new BlockPos(0,1,-21),BLANKS=new BlockPos(-3,0,-22);
    public static final Direction SCREEN_FACING=Direction.SOUTH;
    /** The edit bay: the flatbed (its screen faces east), the splicer, the light table, and the papers on the walls. */
    public static final BlockPos FLATBED=new BlockPos(-11,0,-21),SPLICER=new BlockPos(-14,0,-15),LIGHT_TABLE=new BlockPos(-8,0,-25);
    public static final Direction FLATBED_FACING=Direction.EAST;
    public static final BlockPos SCRIPT=new BlockPos(-10,1,-15),CALL_SHEET=new BlockPos(-8,1,-15),RACK=new BlockPos(-16,1,-18),DESK=new BlockPos(10,0,-16);
    public static final List<BlockPos> PHOTOS=List.of(new BlockPos(-15,1,-26),new BlockPos(-14,1,-26),new BlockPos(-13,1,-26),new BlockPos(-15,2,-26),new BlockPos(-14,2,-26),new BlockPos(-13,2,-26));
    /** The set: the hallway between two runs of flats, its bulb and chalk mark, and the camera on its tripod. */
    public static final BlockPos MARK=new BlockPos(-10,0,-36),BULB=new BlockPos(-10,4,-36),TRIPOD=new BlockPos(-10,0,-30);
    /** Where a reader the film takes comes to: the far end of the hallway, facing back down it. */
    public static final BlockPos TAKEN=new BlockPos(-10,0,-38);
    /** The set's fire door in its far wall: the way on. */
    public static final int EXIT_X=-14,EXIT_Z=-41;
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    private static BlockState mat(String name){return WorkPrintRegistry.material(name).defaultBlockState();}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){NovelRooms.box(l,b,x0,y0,z0,x1,y1,z1,s);}
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){BuildBlocks.set(l,b.offset(at),s,F);}
    private static BlockState fit(WorkPrintBlock.Kind k,Direction f,int stage){return WorkPrintBlock.of(k,f,stage);}
    private static BlockState fit(WorkPrintBlock.Kind k,Direction f){return WorkPrintBlock.of(k,f,0);}

    /** A room: walls one block thick round x0..x1, z0..z1 (interior), floor at -1 and ceiling at h. */
    private static void room(ServerLevel l,BlockPos b,int x0,int x1,int z0,int z1,int h,BlockState wall,BlockState floor,BlockState ceiling){
        box(l,b,x0-1,-1,z0-1,x1+1,h,z1+1,wall);
        box(l,b,x0,-1,z0,x1,-1,z1,floor);
        box(l,b,x0,h,z0,x1,h,z1,ceiling);
        box(l,b,x0,0,z0,x1,h-1,z1,Blocks.AIR.defaultBlockState());
    }
    /** An ordinary door, two halves, in a wall cell, opening into the room it faces. */
    private static void door(ServerLevel l,BlockPos b,int x,int z,Direction facing,Block door){
        var lower=door.defaultBlockState().setValue(DoorBlock.FACING,facing).setValue(DoorBlock.HINGE,DoorHingeSide.LEFT).setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER);
        set(l,b,x,0,z,lower);set(l,b,x,1,z,lower.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
    }

    public static void build(ServerLevel l,BlockPos b){
        var air=Blocks.AIR.defaultBlockState();
        // The whole footprint cleared first, so nothing of the slot's fill is left between the rooms.
        box(l,b,-18,-2,-42,12,6,0,mat("concrete"));
        // The rooms, the far ones first so the nearer rooms' walls are the ones left standing where they meet: the set (a
        // studio a block taller, black above the flats), the edit bay, the sound booth, the back room, and the shop.
        room(l,b,-16,-4,-40,-28,5,mat("concrete"),mat("plywood"),Blocks.BLACK_CONCRETE.defaultBlockState());
        room(l,b,-16,-6,-26,-15,4,mat("pegboard"),mat("concrete"),mat("ceiling"));
        room(l,b,6,10,-19,-15,4,mat("foam"),mat("carpet"),mat("foam"));
        room(l,b,-4,4,-22,-15,4,mat("wall"),mat("concrete"),mat("ceiling"));
        room(l,b,-7,7,-13,-1,4,mat("wall"),mat("carpet"),mat("ceiling"));
        // Doorways between the rooms.
        door(l,b,-3,-14,Direction.NORTH,Blocks.OAK_DOOR);
        door(l,b,-5,-19,Direction.WEST,Blocks.OAK_DOOR);
        door(l,b,5,-17,Direction.EAST,Blocks.OAK_DOOR);
        door(l,b,-10,-27,Direction.NORTH,Blocks.DARK_OAK_DOOR);

        // The shop floor. Shelves of tapes down both walls, two runs back to back down the middle.
        for(int z=-2;z>=-8;z--){
            set(l,b,-7,0,z,fit(WorkPrintBlock.Kind.SHELF,Direction.EAST,Math.floorMod(z,3)));set(l,b,-7,1,z,fit(WorkPrintBlock.Kind.SHELF,Direction.EAST,Math.floorMod(z+1,3)));
            set(l,b,7,0,z,fit(WorkPrintBlock.Kind.SHELF,Direction.WEST,Math.floorMod(z+2,3)));set(l,b,7,1,z,fit(WorkPrintBlock.Kind.SHELF,Direction.WEST,Math.floorMod(z,3)));
        }
        for(int z=-4;z>=-7;z--){set(l,b,-1,0,z,fit(WorkPrintBlock.Kind.SHELF,Direction.WEST,Math.floorMod(z,3)));set(l,b,1,0,z,fit(WorkPrintBlock.Kind.SHELF,Direction.EAST,Math.floorMod(z+1,3)));
            set(l,b,0,0,z,mat("wall"));}
        set(l,b,-7,2,-5,fit(WorkPrintBlock.Kind.SECTION,Direction.EAST));set(l,b,7,2,-5,fit(WorkPrintBlock.Kind.SECTION,Direction.WEST));
        set(l,b,-5,1,-1,fit(WorkPrintBlock.Kind.POSTER,Direction.NORTH,0));set(l,b,5,1,-1,fit(WorkPrintBlock.Kind.POSTER,Direction.NORTH,1));
        set(l,b,-7,1,-10,fit(WorkPrintBlock.Kind.POSTER,Direction.EAST,2));set(l,b,7,1,-10,fit(WorkPrintBlock.Kind.POSTER,Direction.WEST,3));
        set(l,b,RETURNS,fit(WorkPrintBlock.Kind.RETURNS,Direction.NORTH,1));set(l,b,RETURNS.above(),fit(WorkPrintBlock.Kind.SLOT,Direction.NORTH));
        // The counter across the back, with the till, the card, the phone and the ledger on it; the mannequin at its end.
        for(int x=-5;x<=1;x++)set(l,b,x,0,-10,fit(WorkPrintBlock.Kind.COUNTER,Direction.SOUTH,x==-2?1:0));
        set(l,b,CARD,fit(WorkPrintBlock.Kind.CARD,Direction.SOUTH));set(l,b,PHONE,fit(WorkPrintBlock.Kind.PHONE,Direction.SOUTH));set(l,b,LEDGER,fit(WorkPrintBlock.Kind.LEDGER,Direction.SOUTH));
        set(l,b,MANNEQUIN,fit(WorkPrintBlock.Kind.MANNEQUIN,Direction.SOUTH,0));
        set(l,b,-3,3,-4,fit(WorkPrintBlock.Kind.TUBE,Direction.NORTH,1));set(l,b,3,3,-4,fit(WorkPrintBlock.Kind.TUBE,Direction.NORTH,1));set(l,b,-3,3,-8,fit(WorkPrintBlock.Kind.TUBE,Direction.NORTH,1));set(l,b,0,3,-12,fit(WorkPrintBlock.Kind.TUBE,Direction.NORTH,0));
        set(l,b,4,0,-13,fit(WorkPrintBlock.Kind.TAPES,Direction.NORTH));set(l,b,-6,0,-13,fit(WorkPrintBlock.Kind.TAPES,Direction.EAST));

        // The back room.
        set(l,b,CART,fit(WorkPrintBlock.Kind.CART,SCREEN_FACING,0));set(l,b,CRT,fit(WorkPrintBlock.Kind.CRT,SCREEN_FACING,1));
        set(l,b,-1,0,-17,fit(WorkPrintBlock.Kind.CHAIR,Direction.NORTH));set(l,b,1,0,-17,fit(WorkPrintBlock.Kind.CHAIR,Direction.NORTH));
        set(l,b,BLANKS,fit(WorkPrintBlock.Kind.BLANKS,Direction.SOUTH));set(l,b,3,0,-15,fit(WorkPrintBlock.Kind.TAPES,Direction.WEST));set(l,b,4,0,-22,fit(WorkPrintBlock.Kind.TAPES,Direction.SOUTH));
        set(l,b,0,3,-18,fit(WorkPrintBlock.Kind.TUBE,Direction.EAST,1));

        // The edit bay.
        set(l,b,FLATBED,fit(WorkPrintBlock.Kind.FLATBED,FLATBED_FACING,0));
        set(l,b,SPLICER,fit(WorkPrintBlock.Kind.SPLICER,Direction.NORTH,0));set(l,b,-15,0,-15,fit(WorkPrintBlock.Kind.DESK,Direction.NORTH));
        set(l,b,LIGHT_TABLE,fit(WorkPrintBlock.Kind.LIGHT_TABLE,Direction.NORTH,1));set(l,b,-7,0,-25,fit(WorkPrintBlock.Kind.LIGHT_TABLE,Direction.NORTH,1));
        for(int z=-17;z>=-20;z--){set(l,b,-16,1,z,fit(WorkPrintBlock.Kind.RACK,Direction.EAST));set(l,b,-16,2,z,fit(WorkPrintBlock.Kind.RACK,Direction.EAST));}
        for(int i=0;i<PHOTOS.size();i++)set(l,b,PHOTOS.get(i),fit(WorkPrintBlock.Kind.PHOTOS,Direction.SOUTH,i%3));
        set(l,b,SCRIPT,fit(WorkPrintBlock.Kind.SCRIPT,Direction.NORTH));set(l,b,CALL_SHEET,fit(WorkPrintBlock.Kind.CALL_SHEET,Direction.NORTH));
        set(l,b,-15,0,-23,fit(WorkPrintBlock.Kind.TRIM_BIN,Direction.EAST));set(l,b,-6,0,-16,fit(WorkPrintBlock.Kind.TAPES,Direction.WEST));
        set(l,b,-11,3,-20,fit(WorkPrintBlock.Kind.TUBE,Direction.EAST,1));

        // The sound booth.
        set(l,b,DESK,fit(WorkPrintBlock.Kind.DESK,Direction.WEST));set(l,b,7,0,-18,fit(WorkPrintBlock.Kind.MIC,Direction.EAST));set(l,b,8,3,-17,fit(WorkPrintBlock.Kind.TUBE,Direction.NORTH,1));

        // The set: the film's hallway between two runs of flats, closed at the far end, its bulb over the chalk mark.
        for(int z=-31;z>=-39;z--){set(l,b,-12,0,z,fit(WorkPrintBlock.Kind.FLAT,Direction.EAST,0));set(l,b,-12,1,z,fit(WorkPrintBlock.Kind.FLAT,Direction.EAST,0));set(l,b,-12,2,z,fit(WorkPrintBlock.Kind.FLAT,Direction.EAST,0));
            set(l,b,-8,0,z,fit(WorkPrintBlock.Kind.FLAT,Direction.WEST,0));set(l,b,-8,1,z,fit(WorkPrintBlock.Kind.FLAT,Direction.WEST,0));set(l,b,-8,2,z,fit(WorkPrintBlock.Kind.FLAT,Direction.WEST,0));}
        for(int x=-11;x<=-9;x++)for(int y=0;y<=2;y++)set(l,b,x,y,-40,fit(WorkPrintBlock.Kind.FLAT,Direction.SOUTH,0));
        box(l,b,-11,0,-39,-9,2,-31,air);
        set(l,b,MARK,fit(WorkPrintBlock.Kind.MARK,Direction.NORTH));set(l,b,BULB,fit(WorkPrintBlock.Kind.BULB,Direction.NORTH,0));
        set(l,b,TRIPOD,fit(WorkPrintBlock.Kind.TRIPOD,Direction.NORTH,0));set(l,b,-14,0,-30,fit(WorkPrintBlock.Kind.SLATE,Direction.EAST));
        set(l,b,-5,0,-29,fit(WorkPrintBlock.Kind.TAPES,Direction.WEST));
    }
}
