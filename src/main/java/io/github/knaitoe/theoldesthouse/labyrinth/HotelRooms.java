package io.github.knaitoe.theoldesthouse.labyrinth;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.StanleyBlock.Kind;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;

/**
 * The hotel as one building (0.4.105). Ground floor (walk 0..4): the lobby with the desk and the grand stair, the bar, the
 * office behind the desk, the dining room with the table for two, the double-height ballroom with the 1921 photograph, the
 * kitchen and stores, and across the back the service corridor to the caretaker's rooms, the staff entrance, the back porch,
 * the workshop and the stair down to the boiler. Upstairs (carpet at 6, walk to 8): the guest corridor, built exactly like
 * the numbered corridor it runs into, Room 217 over the table for two, the linen room and the caretaker's family's rooms.
 * The back door stands in the rear wall; the grounds see that same wall, siding and lit windows, from outside.
 * <p>Polish, composition and review passes skip it. Interior partitions between differently finished rooms are two thick, so
 * each room shows its own walls.
 */
public final class HotelRooms {
    // The lobby and the desk.
    public static final BlockPos DESK=new BlockPos(-7,0,-5),REGISTER=new BlockPos(-7,0,-4),RULES=new BlockPos(-3,0,-2),OFFICE_NOTES=new BlockPos(-20,1,-4);
    // The dining room: the table for two, the fire, and above the table the patched floor of 217.
    public static final BlockPos TABLE=new BlockPos(-21,0,-21),PATCH=new BlockPos(-21,5,-21),FIRE=new BlockPos(-20,0,-33),ORCHESTRA=new BlockPos(-15,1,-30);
    // The ballroom's photograph; a guest's head goes in the front row's empty place.
    public static final BlockPos PHOTO=new BlockPos(-10,2,-28),FRONT_ROW=new BlockPos(-10,2,-27),PIANO=new BlockPos(-3,1,-39);
    // Room 217, its door on the guest corridor, the plaque beside it, and the hose rack across from it.
    public static final BlockPos BED=new BlockPos(-23,6,-17),BED_HEAD=new BlockPos(-23,6,-16),DRAWER=new BlockPos(-18,6,-18),CANDLE=new BlockPos(-22,7,-16),
        HOUSEKEEPING=new BlockPos(-27,7,-17),ROOM_DOOR=new BlockPos(-16,6,-21),PLAQUE_217=new BlockPos(-15,7,-20),HOSE_RACK=new BlockPos(-13,6,-21),
        FAMILY_NOTE=new BlockPos(-22,7,-44),NUMBERS=new BlockPos(-14,6,-45);
    // The back of house.
    public static final BlockPos LOG=new BlockPos(-24,1,-59),TYPEWRITER=new BlockPos(-27,1,-63),SERVICE_DOOR=new BlockPos(-10,0,-56),
        STAFF_EXIT=new BlockPos(-10,0,-67),BACK_DOOR=new BlockPos(0,0,-67),TIME_CLOCK=new BlockPos(-8,1,-61);
    // The boiler room, down the service stair.
    public static final BlockPos BOILER=new BlockPos(0,-4,-65),GAUGE=new BlockPos(0,-3,-64),VALVE=new BlockPos(3,-3,-66),RESTART=new BlockPos(5,-3,-66),PLANT=new BlockPos(-4,-3,-66);
    // The grounds: the master key under the snow at the heart of the maze.
    public static final BlockPos KEY=new BlockPos(0,0,-39),STAFF_DOOR_OUTSIDE=new BlockPos(-10,0,1);
    /** 217's floor, ceiling to the dining room: what a guest's torch may light. */
    public static final int ROOM_X0=-27,ROOM_X1=-18,ROOM_Z0=-26,ROOM_Z1=-16;
    /** The bar, where a drink is poured onto the tab, and the ballroom's dance floor, where the photograph is taken. */
    public static final int BAR_X0=13,BAR_X1=27,BAR_Z0=-16,BAR_Z1=-1,FLOOR_X0=-9,FLOOR_X1=9,FLOOR_Z0=-35,FLOOR_Z1=-19;
    /** The boiler room, which the blast fills. */
    public static final int PLANT_X0=-10,PLANT_X1=11,PLANT_Z0=-66,PLANT_Z1=-60;

    /** A window: its lower pane's cell and the way its room side faces. The upper pane is the cell above. */
    public record Window(BlockPos low,Direction facing){}
    public static final List<Window> WINDOWS;
    /** The numbered doors that stay locked, each with its number. */
    public record Room(int number,BlockPos door,Direction facing,Direction start){}
    public static final List<Room> LOCKED;
    /** The rear wall's windows, x along z -67: the caretaker's, the porch's either side of the back door, the workshop's. */
    private static final int[] REAR_WINDOWS={-25,-24,-19,-18,-2,2,17,18,23,24};
    static{
        var w=new ArrayList<Window>();
        for(int x:new int[]{-8,-7,-4,-3,3,4,7,8,-24,-23,-17,-16,16,17,23,24})w.add(new Window(new BlockPos(x,1,0),Direction.NORTH));
        for(int z:new int[]{-4,-5,-13,-14,-17,-18,-25,-26,-29,-30,-44,-45,-61,-62})w.add(new Window(new BlockPos(-28,1,z),Direction.EAST));
        for(int z:new int[]{-14,-15,-45,-46,-61,-62})w.add(new Window(new BlockPos(28,1,z),Direction.WEST));
        for(int x:REAR_WINDOWS)w.add(new Window(new BlockPos(x,1,-67),Direction.SOUTH));
        for(int z:new int[]{-19,-20,-26,-44,-45})w.add(new Window(new BlockPos(-28,7,z),Direction.EAST));
        WINDOWS=List.copyOf(w);
        LOCKED=List.of(new Room(211,new BlockPos(5,6,-11),Direction.NORTH,Direction.EAST),new Room(212,new BlockPos(2,6,-15),Direction.SOUTH,Direction.EAST),
            new Room(213,new BlockPos(-1,6,-11),Direction.NORTH,Direction.EAST),new Room(214,new BlockPos(-4,6,-15),Direction.SOUTH,Direction.EAST),
            new Room(215,new BlockPos(-7,6,-11),Direction.NORTH,Direction.EAST),new Room(216,new BlockPos(-12,6,-18),Direction.WEST,Direction.SOUTH),
            new Room(218,new BlockPos(-12,6,-24),Direction.WEST,Direction.SOUTH),new Room(219,new BlockPos(-16,6,-30),Direction.EAST,Direction.SOUTH),
            new Room(220,new BlockPos(-12,6,-30),Direction.WEST,Direction.SOUTH),new Room(221,new BlockPos(-12,6,-37),Direction.WEST,Direction.SOUTH),
            new Room(222,new BlockPos(-12,6,-42),Direction.WEST,Direction.SOUTH));
    }
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE|Block.UPDATE_SUPPRESS_DROPS;
    private HotelRooms(){}
    public static void build(ServerLevel l,BlockPos b,LabyrinthPlace p){if(p==LabyrinthPlace.HOTEL)hotel(l,b);else grounds(l,b);}

    // ------------------------------------------------------------------------------------------------ helpers
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void fill(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){BuildBlocks.box(l,b.offset(Math.min(x0,x1),Math.min(y0,y1),Math.min(z0,z1)),b.offset(Math.max(x0,x1),Math.max(y0,y1),Math.max(z0,z1)),s,F);}
    private static void fill(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,Block block){fill(l,b,x0,y0,z0,x1,y1,z1,block.defaultBlockState());}
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,Block block){set(l,b,x,y,z,block.defaultBlockState());}
    private static BlockState st(Kind k){return StanleyBlock.of(k,Direction.NORTH);}
    private static BlockState st(Kind k,Direction f){return StanleyBlock.of(k,f);}
    private static void put(ServerLevel l,BlockPos b,BlockPos at,Kind k,Direction f){BuildBlocks.set(l,b.offset(at),st(k,f),F);}
    private static void put(ServerLevel l,BlockPos b,int x,int y,int z,Kind k,Direction f){set(l,b,x,y,z,st(k,f));}
    private static void prop(ServerLevel l,BlockPos b,BlockPos at,HotelPropBlock.Kind kind,Direction facing){BuildBlocks.set(l,b.offset(at),HotelRegistry.PROP.get().defaultBlockState().setValue(HotelPropBlock.KIND,kind).setValue(HotelPropBlock.FACING,facing),F);}
    private static void furniture(ServerLevel l,BlockPos b,int x,int y,int z,HouseholdFurnitureBlock.Kind kind,Direction facing){NovelRooms.furniture(l,b.offset(x,y,z),kind,facing);}
    private static void detail(ServerLevel l,BlockPos b,int x,int y,int z,SceneDetailBlock.Kind kind,Direction facing){BuildBlocks.set(l,b.offset(x,y,z),HouseBlocks.SCENE_DETAIL.get().defaultBlockState().setValue(SceneDetailBlock.KIND,kind).setValue(SceneDetailBlock.FACING,facing),F);}
    private static void lantern(ServerLevel l,BlockPos b,int x,int y,int z){set(l,b,x,y,z,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));}
    private static void stairs(ServerLevel l,BlockPos b,int x,int y,int z,Block block,Direction facing){set(l,b,x,y,z,block.defaultBlockState().setValue(StairBlock.FACING,facing));}
    private static void topSlab(ServerLevel l,BlockPos b,int x,int y,int z,Block block){set(l,b,x,y,z,block.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP));}
    private static void door(ServerLevel l,BlockPos b,BlockPos at,Direction facing,Block block){NovelRooms.door(l,b.offset(at),facing,block,false);}
    private static void door(ServerLevel l,BlockPos b,int x,int y,int z,Direction facing,Block block){door(l,b,new BlockPos(x,y,z),facing,block);}

    /** A finish for a room's walls: a lower band up to {@code band} rows above the floor, the rest above it and a cornice row. */
    private record Finish(BlockState floor,BlockState lower,int band,BlockState upper,BlockState cornice,BlockState ceiling){
        BlockState wall(int row,int top){return row<=band?lower:row==top&&cornice!=null?cornice:upper;}
    }
    /**
     * A room's air over walk levels {@code y0..y1} inside {@code x0..x1, z0..z1}, its floor below, its ceiling above and one
     * ring of walls round it from floor to ceiling in its own finish.
     */
    private static void room(ServerLevel l,BlockPos b,int x0,int x1,int z0,int z1,int y0,int y1,Finish f){
        int lx=Math.min(x0,x1),hx=Math.max(x0,x1),lz=Math.min(z0,z1),hz=Math.max(z0,z1);
        fill(l,b,lx,y0,lz,hx,y1,hz,Blocks.AIR);
        fill(l,b,lx,y0-1,lz,hx,y0-1,hz,f.floor());
        fill(l,b,lx-1,y1+1,lz-1,hx+1,y1+1,hz+1,f.ceiling());
        for(int y=y0-1;y<=y1;y++){var s=y<y0?f.floor():f.wall(y-y0,y1-y0);
            fill(l,b,lx-1,y,lz-1,hx+1,y,lz-1,s);fill(l,b,lx-1,y,hz+1,hx+1,y,hz+1,s);
            fill(l,b,lx-1,y,lz,lx-1,y,hz,s);fill(l,b,hx+1,y,lz,hx+1,y,hz,s);}
    }
    /** A doorway {@code w} wide and {@code h} high through a wall {@code depth} thick, cut along {@code into}. */
    private static void opening(ServerLevel l,BlockPos b,int x,int y,int z,Direction into,int depth,int w,int h){
        var across=into.getClockWise();
        for(int d=0;d<depth;d++)for(int i=-(w-1)/2;i<=w/2;i++)for(int dy=0;dy<h;dy++){var at=new BlockPos(x,y,z).relative(into,d).relative(across,i).above(dy);set(l,b,at.getX(),at.getY(),at.getZ(),Blocks.AIR);}
    }
    /** An archway frame of dark timber either side of and over an opening. */
    private static void arch(ServerLevel l,BlockPos b,int x,int y,int z,Direction into,int depth,int w,int h){
        var across=into.getClockWise();
        for(int d=0;d<depth;d++){var c=new BlockPos(x,y,z).relative(into,d);
            for(int dy=0;dy<=h;dy++)for(int side:new int[]{-(w-1)/2-1,w/2+1}){var at=c.relative(across,side).above(dy);set(l,b,at.getX(),at.getY(),at.getZ(),Blocks.STRIPPED_DARK_OAK_LOG);}
            for(int i=-(w-1)/2;i<=w/2;i++){var at=c.relative(across,i).above(h);set(l,b,at.getX(),at.getY(),at.getZ(),Blocks.DARK_OAK_PLANKS);}}
        opening(l,b,x,y,z,into,depth,w,h);
    }
    private static void window(ServerLevel l,BlockPos b,Window w){
        set(l,b,w.low().getX(),w.low().getY(),w.low().getZ(),StanleyBlock.of(Kind.WINDOW_LOW,w.facing()));
        set(l,b,w.low().getX(),w.low().getY()+1,w.low().getZ(),StanleyBlock.of(Kind.WINDOW_HIGH,w.facing()));
    }
    private static Finish lobby(){return new Finish(Blocks.DARK_OAK_PLANKS.defaultBlockState(),HouseBlocks.HOTEL_WAINSCOT.get().defaultBlockState(),1,HouseBlocks.HOTEL_WALLPAPER.get().defaultBlockState(),Blocks.DARK_OAK_PLANKS.defaultBlockState(),st(Kind.CEILING));}
    private static Finish service(int band){return new Finish(st(Kind.LINO),st(Kind.PAINT_LOW),band,st(Kind.PAINT),null,st(Kind.PAINT));}

    // ------------------------------------------------------------------------------------------------ the hotel
    private static void hotel(ServerLevel l,BlockPos b){
        groundFloor(l,b);
        backOfHouse(l,b);
        basement(l,b);
        upstairs(l,b);
        for(var w:WINDOWS)window(l,b,w);
        LabyrinthBuilder.entrance(l,b,HouseBlocks.HOTEL_WALLPAPER.get().defaultBlockState(),Blocks.DARK_OAK_PLANKS.defaultBlockState(),st(Kind.CEILING));
        LabyrinthBuilder.doors(l,b,LabyrinthPlace.HOTEL);
        // The front doors: a fanlight over the entry and lamps either side of it.
        set(l,b,0,2,0,StanleyBlock.of(Kind.WINDOW_HIGH,Direction.NORTH));
        for(int x:new int[]{-1,1})for(int y=0;y<=3;y++)set(l,b,x,y,0,Blocks.STRIPPED_DARK_OAK_LOG);
        for(int x:new int[]{-2,2})put(l,b,x,2,-1,Kind.SCONCE,Direction.NORTH);
    }

    private static void groundFloor(ServerLevel l,BlockPos b){
        var wainscot=HouseBlocks.HOTEL_WAINSCOT.get().defaultBlockState();var planks=Blocks.DARK_OAK_PLANKS.defaultBlockState();
        // The lobby: dark panelling, the papered walls above it, a coffered ceiling.
        room(l,b,-10,10,-16,-1,0,4,lobby());
        // The office behind the desk, panelled all the way up.
        room(l,b,-27,-13,-8,-1,0,4,new Finish(planks,wainscot,3,wainscot,planks,st(Kind.CEILING)));
        // The dining room, the bar in gold, and the ballroom two storeys high.
        room(l,b,-27,-13,-33,-11,0,4,new Finish(planks,wainscot,0,HouseBlocks.HOTEL_WALLPAPER.get().defaultBlockState(),planks,st(Kind.CEILING)));
        room(l,b,13,27,-16,-1,0,4,new Finish(planks,wainscot,0,st(Kind.GOLD),planks,st(Kind.CEILING)));
        room(l,b,-10,10,-40,-19,0,9,new Finish(st(Kind.PARQUET),wainscot,1,st(Kind.GOLD),planks,st(Kind.CEILING)));
        lobbyRoom(l,b,planks);
        office(l,b);
        dining(l,b);
        bar(l,b);
        ballroom(l,b);
    }

    private static void lobbyRoom(ServerLevel l,BlockPos b,BlockState planks){
        // The front desk down the west side, facing the room; the clerk's floor behind it; the office door behind that.
        for(int z=-3;z>=-7;z--)put(l,b,-7,0,z,Kind.COUNTER,Direction.EAST);
        put(l,b,REGISTER,Kind.REGISTER,Direction.EAST);
        prop(l,b,DESK,HotelPropBlock.Kind.RECEPTION,Direction.EAST);
        for(int z=-3;z>=-4;z--)put(l,b,-10,2,z,Kind.KEY_RACK,Direction.EAST);
        opening(l,b,-11,0,-6,Direction.WEST,2,1,2);door(l,b,-11,0,-6,Direction.EAST,Blocks.DARK_OAK_DOOR);
        put(l,b,RULES,Kind.EASEL,Direction.SOUTH);
        // A rug, a seat by the stair, a long-case clock under the landing, and two chandeliers.
        fill(l,b,-4,0,-14,4,0,-3,HouseBlocks.HOTEL_CARPET.get());
        furniture(l,b,-2,0,-9,HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.EAST);furniture(l,b,2,0,-9,HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.WEST);
        furniture(l,b,0,0,-9,HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.SOUTH);detail(l,b,0,1,-9,SceneDetailBlock.Kind.TABLE_LAMP,Direction.SOUTH);
        set(l,b,10,0,-15,ManorBlock.of(ManorBlock.Kind.CLOCK_LOW,Direction.WEST));set(l,b,10,1,-15,ManorBlock.of(ManorBlock.Kind.CLOCK_HIGH,Direction.WEST));
        set(l,b,-10,0,-16,Blocks.POTTED_FERN);set(l,b,10,0,-1,Blocks.POTTED_FERN);set(l,b,-10,0,-1,Blocks.POTTED_FERN);
        for(int z:new int[]{-5,-12})put(l,b,0,4,z,Kind.CHANDELIER,Direction.NORTH);
        for(int z:new int[]{-10,-14})put(l,b,-10,2,z,Kind.SCONCE,Direction.EAST);
        // The grand stair up the east side: six treads, a closed string and a rail, through the ceiling to the landing.
        for(int i=0;i<=5;i++){int z=-6-i;
            for(int x=8;x<=10;x++){if(i>0)fill(l,b,x,0,z,x,i-1,z,planks);stairs(l,b,x,i,z,Blocks.DARK_OAK_STAIRS,Direction.NORTH);}
            fill(l,b,7,0,z,7,i,z,planks);set(l,b,7,i+1,z,Blocks.DARK_OAK_FENCE);}
        fill(l,b,8,5,-10,10,5,-9,Blocks.AIR);
        set(l,b,7,0,-5,Blocks.STRIPPED_DARK_OAK_LOG);set(l,b,7,1,-5,Blocks.DARK_OAK_FENCE);
        // Through to the bar, the dining room and the ballroom.
        arch(l,b,11,0,-3,Direction.EAST,2,3,4);
        arch(l,b,-11,0,-13,Direction.WEST,2,3,4);
        arch(l,b,0,0,-17,Direction.NORTH,2,3,4);
    }

    private static void office(ServerLevel l,BlockPos b){
        furniture(l,b,-20,0,-4,HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.SOUTH);furniture(l,b,-20,0,-3,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.NORTH);
        put(l,b,OFFICE_NOTES,Kind.BOOK,Direction.SOUTH);
        for(int x:new int[]{-25,-24})put(l,b,x,0,-8,Kind.FILING,Direction.SOUTH);
        put(l,b,-14,0,-8,Kind.SWITCHBOARD,Direction.SOUTH);
        furniture(l,b,-26,0,-3,HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.EAST);
        detail(l,b,-13,2,-3,SceneDetailBlock.Kind.CLOCK,Direction.WEST);detail(l,b,-27,2,-7,SceneDetailBlock.Kind.FRAME,Direction.EAST);
        put(l,b,-20,4,-5,Kind.CHANDELIER,Direction.NORTH);
    }

    private static void dining(ServerLevel l,BlockPos b){
        // The table for two under 217's patched floor; every other table has been cleared.
        fill(l,b,-24,0,-24,-18,0,-18,Blocks.RED_CARPET);
        for(int[] t:new int[][]{{-25,-15},{-17,-16},{-25,-28},{-19,-26},{-25,-21}})put(l,b,t[0],0,t[1],Kind.TABLE,Direction.NORTH);
        furniture(l,b,TABLE.getX(),0,TABLE.getZ()+1,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.NORTH);
        furniture(l,b,TABLE.getX(),0,TABLE.getZ()-1,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.SOUTH);
        put(l,b,TABLE,Kind.TABLE_SET,Direction.NORTH);
        // The fire, set into the north wall under a stone breast and a mantel.
        for(int x:new int[]{-22,-18})fill(l,b,x,0,-33,x,4,-33,Blocks.STONE_BRICKS);
        for(int x:new int[]{-21,-19})fill(l,b,x,0,-33,x,1,-33,Blocks.STONE_BRICKS);
        set(l,b,FIRE.getX(),FIRE.getY(),FIRE.getZ(),Blocks.CAMPFIRE);set(l,b,-20,1,-33,Blocks.AIR);
        fill(l,b,-21,2,-33,-19,4,-33,Blocks.STONE_BRICKS);
        for(int x=-22;x<=-18;x++)topSlab(l,b,x,2,-32,Blocks.DARK_OAK_SLAB);
        detail(l,b,-20,3,-32,SceneDetailBlock.Kind.CLOCK,Direction.SOUTH);
        fill(l,b,-21,-1,-32,-19,-1,-32,Blocks.POLISHED_ANDESITE);
        // The orchestra's dais in the far corner: music on the stands, the chairs empty.
        fill(l,b,-16,0,-32,-13,0,-28,Blocks.DARK_OAK_PLANKS);
        for(int x=-16;x<=-13;x++)stairs(l,b,x,0,-27,Blocks.DARK_OAK_STAIRS,Direction.NORTH);
        prop(l,b,new BlockPos(-14,1,-31),HotelPropBlock.Kind.PIANO,Direction.SOUTH);
        for(int x:new int[]{-16,-13}){set(l,b,x,1,-29,MasqueBlock.of(MasqueBlock.Kind.MUSIC_STAND,Direction.SOUTH));furniture(l,b,x,1,-30,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.SOUTH);}
        for(int z:new int[]{-15,-27})put(l,b,-21,4,z,Kind.CHANDELIER,Direction.NORTH);
        for(int z:new int[]{-16,-22,-28})put(l,b,-13,2,z,Kind.SCONCE,Direction.WEST);
    }

    private static void bar(ServerLevel l,BlockPos b){
        for(int z=-4;z>=-12;z--){put(l,b,21,0,z,Kind.BAR_COUNTER,Direction.WEST);put(l,b,27,0,z,Kind.BACK_BAR,Direction.WEST);}
        for(int z:new int[]{-5,-7,-9,-11})furniture(l,b,20,0,z,HouseholdFurnitureBlock.Kind.KITCHEN_STOOL,Direction.EAST);
        for(int z:new int[]{-4,-9,-14}){furniture(l,b,15,0,z,HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.SOUTH);
            furniture(l,b,14,0,z,HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.EAST);furniture(l,b,16,0,z,HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.WEST);}
        detail(l,b,15,1,-9,SceneDetailBlock.Kind.BOTTLES,Direction.SOUTH);
        put(l,b,19,4,-8,Kind.CHANDELIER,Direction.NORTH);
        for(int z:new int[]{-3,-13})put(l,b,13,2,z,Kind.SCONCE,Direction.EAST);
        // A dark mirror above the bottles.
        for(int z=-4;z>=-12;z--)set(l,b,27,2,z,Blocks.BLACK_STAINED_GLASS);
        for(int z=-4;z>=-12;z--)set(l,b,28,2,z,Blocks.BLACK_CONCRETE);
    }

    private static void ballroom(ServerLevel l,BlockPos b){
        var log=Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState();
        for(int x:new int[]{-11,11})for(int z:new int[]{-22,-34})fill(l,b,x,0,z,x,8,z,log);
        // The bandstand across the far end, its steps in the middle.
        fill(l,b,-8,0,-40,8,0,-37,Blocks.DARK_OAK_PLANKS);
        for(int x=-2;x<=2;x++)stairs(l,b,x,0,-36,Blocks.DARK_OAK_STAIRS,Direction.NORTH);
        prop(l,b,PIANO,HotelPropBlock.Kind.PIANO,Direction.SOUTH);
        for(int x:new int[]{3,5})set(l,b,x,1,-39,MasqueBlock.of(MasqueBlock.Kind.MUSIC_STAND,Direction.SOUTH));
        // The 1921 photograph in its frame on the west wall.
        prop(l,b,PHOTO,HotelPropBlock.Kind.PHOTO,Direction.EAST);
        // Two tall mirrors across from it, and tables for the party along that side.
        for(int z:new int[]{-25,-31}){fill(l,b,11,1,z,11,6,z,Blocks.BLACK_STAINED_GLASS);fill(l,b,12,1,z,12,6,z,Blocks.BLACK_CONCRETE);}
        for(int z:new int[]{-23,-29}){put(l,b,9,0,z,Kind.TABLE,Direction.WEST);furniture(l,b,8,0,z,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.EAST);}
        for(int z:new int[]{-24,-31})put(l,b,0,9,z,Kind.CHANDELIER,Direction.NORTH);
        for(int z:new int[]{-20,-38})put(l,b,-10,3,z,Kind.SCONCE,Direction.EAST);
    }

    private static void backOfHouse(ServerLevel l,BlockPos b){
        var white=st(Kind.WHITE_TILE);var paint=st(Kind.PAINT);
        // Kitchen, stores and laundry, and the service corridor across the back.
        room(l,b,-27,-13,-50,-36,0,4,new Finish(st(Kind.TILE),white,2,paint,null,st(Kind.CEILING)));
        room(l,b,-10,10,-50,-43,0,4,service(1));
        room(l,b,13,27,-50,-43,0,4,service(1));
        room(l,b,-27,27,-55,-53,0,3,service(1));
        // Behind it: the caretaker's rooms, the staff entrance, the back porch and the workshop.
        room(l,b,-27,-15,-66,-58,0,4,service(1));
        room(l,b,-12,-8,-66,-58,0,4,new Finish(Blocks.SPRUCE_PLANKS.defaultBlockState(),st(Kind.PAINT_LOW),1,paint,null,paint));
        room(l,b,-3,3,-66,-58,0,3,new Finish(Blocks.SPRUCE_PLANKS.defaultBlockState(),st(Kind.SIDING),3,st(Kind.SIDING),null,Blocks.SPRUCE_PLANKS.defaultBlockState()));
        room(l,b,14,27,-66,-58,0,4,service(1));
        // The rear wall either side of the porch is siding, as it is from the grounds.
        for(int x=-7;x<=7;x++){fill(l,b,x,-1,-67,x,7,-67,st(Kind.SIDING));set(l,b,x,5,-67,st(Kind.EAVE));}
        // Doors and doorways: the kitchen's swing door from the dining room's far corner first.
        opening(l,b,-15,0,-34,Direction.NORTH,2,1,2);door(l,b,-15,0,-34,Direction.SOUTH,Blocks.DARK_OAK_DOOR);
        arch(l,b,-12,0,-46,Direction.EAST,2,1,3);
        for(int x:new int[]{-20,0,20})arch(l,b,x,0,-51,Direction.NORTH,2,1,3);
        opening(l,b,-21,0,-56,Direction.NORTH,2,1,2);door(l,b,-21,0,-56,Direction.SOUTH,Blocks.SPRUCE_DOOR);
        opening(l,b,SERVICE_DOOR.getX(),0,-56,Direction.NORTH,2,1,2);door(l,b,SERVICE_DOOR,Direction.SOUTH,Blocks.IRON_DOOR);
        arch(l,b,0,0,-56,Direction.NORTH,2,3,3);
        opening(l,b,20,0,-56,Direction.NORTH,2,1,2);door(l,b,20,0,-56,Direction.SOUTH,Blocks.SPRUCE_DOOR);
        kitchen(l,b);
        stores(l,b);
        corridor(l,b);
        caretaker(l,b);
        staff(l,b);
        porch(l,b);
        workshop(l,b);
    }

    private static void kitchen(ServerLevel l,BlockPos b){
        for(int z=-38;z>=-41;z--)put(l,b,-27,0,z,Kind.RANGE,Direction.EAST);
        for(int z=-38;z>=-41;z--)set(l,b,-27,3,z,Blocks.STONE_BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP));
        for(int x=-21;x<=-19;x++)for(int z=-42;z>=-44;z--)put(l,b,x,0,z,Kind.STEEL_TABLE,Direction.NORTH);
        detail(l,b,-20,1,-43,SceneDetailBlock.Kind.DISH_RACK,Direction.NORTH);detail(l,b,-21,1,-42,SceneDetailBlock.Kind.CROCK,Direction.NORTH);
        for(int z:new int[]{-47,-48})set(l,b,-27,0,z,Blocks.CAULDRON);
        set(l,b,-14,0,-49,Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING,Direction.WEST));
        put(l,b,-14,0,-37,Kind.STEEL_TABLE,Direction.WEST);detail(l,b,-14,1,-37,SceneDetailBlock.Kind.CROCK,Direction.WEST);
        for(int z:new int[]{-39,-46})lantern(l,b,-20,4,z);
    }

    private static void stores(ServerLevel l,BlockPos b){
        // Dry stores: shelves of barrels and sacks, and a chest.
        for(int x=-9;x<=9;x+=2){set(l,b,x,0,-50,Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING,Direction.UP));if(x%4==1)detail(l,b,x,1,-50,SceneDetailBlock.Kind.FEED_SACK,Direction.SOUTH);else detail(l,b,x,1,-50,SceneDetailBlock.Kind.CRATE,Direction.SOUTH);}
        set(l,b,9,0,-44,Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING,Direction.WEST));
        for(int x:new int[]{-6,6})lantern(l,b,x,4,-46);
        // The laundry: machines along the outer wall, baskets and the ironing table.
        for(int z:new int[]{-43,-48,-50})furniture(l,b,27,0,z,HouseholdFurnitureBlock.Kind.WASHING_MACHINE,Direction.WEST);
        put(l,b,20,0,-46,Kind.STEEL_TABLE,Direction.NORTH);detail(l,b,20,1,-46,SceneDetailBlock.Kind.TOWELS,Direction.NORTH);
        for(int x:new int[]{14,15})set(l,b,x,0,-50,Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING,Direction.UP));
        put(l,b,14,0,-43,Kind.LINEN_SHELF,Direction.NORTH);put(l,b,15,0,-43,Kind.LINEN_SHELF,Direction.NORTH);
        lantern(l,b,20,4,-46);
    }

    private static void corridor(ServerLevel l,BlockPos b){
        for(int x=-24;x<=24;x+=8)lantern(l,b,x,3,-54);
        put(l,b,-14,1,-53,Kind.NOTICE,Direction.NORTH);
    }

    private static void caretaker(ServerLevel l,BlockPos b){
        NovelRooms.bed(l,b.offset(-16,0,-64),Blocks.BROWN_BED,Direction.NORTH);
        furniture(l,b,-24,0,-59,HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.SOUTH);furniture(l,b,-24,0,-58,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.NORTH);
        put(l,b,LOG,Kind.BOOK,Direction.SOUTH);
        furniture(l,b,-27,0,-63,HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.EAST);prop(l,b,TYPEWRITER,HotelPropBlock.Kind.TYPEWRITER,Direction.EAST);
        furniture(l,b,-26,0,-63,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.WEST);
        furniture(l,b,-20,0,-62,HouseholdFurnitureBlock.Kind.FLORAL_ARMCHAIR,Direction.EAST);furniture(l,b,-19,0,-62,HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.WEST);
        detail(l,b,-19,1,-62,SceneDetailBlock.Kind.BOTTLES,Direction.WEST);
        furniture(l,b,-15,0,-60,HouseholdFurnitureBlock.Kind.RADIATOR,Direction.WEST);furniture(l,b,-18,0,-66,HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.SOUTH);
        fill(l,b,-24,0,-64,-20,0,-61,Blocks.BROWN_CARPET);
        detail(l,b,-15,2,-63,SceneDetailBlock.Kind.CLOCK,Direction.WEST);
        lantern(l,b,-21,4,-62);
    }

    private static void staff(ServerLevel l,BlockPos b){
        put(l,b,TIME_CLOCK,Kind.TIME_CLOCK,Direction.WEST);
        for(int z=-60;z>=-62;z--)stairs(l,b,-12,0,z,Blocks.SPRUCE_STAIRS,Direction.WEST);
        detail(l,b,-12,2,-64,SceneDetailBlock.Kind.COAT,Direction.EAST);detail(l,b,-9,0,-65,SceneDetailBlock.Kind.SHOES,Direction.SOUTH);
        put(l,b,-8,1,-64,Kind.NOTICE,Direction.WEST);
        lantern(l,b,-10,4,-62);
    }

    private static void porch(ServerLevel l,BlockPos b){
        set(l,b,0,0,-66,ManorBlock.of(ManorBlock.Kind.DOORMAT,Direction.SOUTH));
        for(int z=-63;z>=-65;z--){set(l,b,-3,0,z,Blocks.SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.Z));if(z<-63)set(l,b,-3,1,z,Blocks.SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.Z));}
        for(int z=-60;z>=-61;z--)stairs(l,b,3,0,z,Blocks.SPRUCE_STAIRS,Direction.EAST);
        detail(l,b,3,0,-63,SceneDetailBlock.Kind.TOOLS,Direction.WEST);detail(l,b,3,2,-64,SceneDetailBlock.Kind.COAT,Direction.WEST);
        set(l,b,-3,0,-59,ManorBlock.of(ManorBlock.Kind.UMBRELLA,Direction.EAST));
        lantern(l,b,0,3,-61);
    }

    private static void workshop(ServerLevel l,BlockPos b){
        furniture(l,b,24,0,-66,HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.SOUTH);detail(l,b,24,1,-66,SceneDetailBlock.Kind.TOOLS,Direction.SOUTH);
        set(l,b,26,0,-66,Blocks.CRAFTING_TABLE);set(l,b,21,0,-66,Blocks.ANVIL.defaultBlockState().setValue(AnvilBlock.FACING,Direction.EAST));
        set(l,b,15,0,-66,Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING,Direction.SOUTH));
        for(int z:new int[]{-59,-60})set(l,b,27,0,z,Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING,Direction.UP));
        detail(l,b,27,1,-59,SceneDetailBlock.Kind.ROPE_COIL,Direction.WEST);
        lantern(l,b,20,4,-62);
    }

    private static void basement(ServerLevel l,BlockPos b){
        // The boiler room under the back porch: brick walls, a concrete floor, the joists of the floor above.
        room(l,b,PLANT_X0,PLANT_X1,PLANT_Z0,PLANT_Z1,-4,-2,new Finish(Blocks.SMOOTH_STONE.defaultBlockState(),Blocks.BRICKS.defaultBlockState(),3,Blocks.BRICKS.defaultBlockState(),null,Blocks.SPRUCE_PLANKS.defaultBlockState()));
        // The service stair, down from the corridor through its own walled well.
        for(int z=-57;z>=-59;z--){fill(l,b,8,-5,z,8,4,z,st(Kind.PAINT));fill(l,b,12,-5,z,12,4,z,st(Kind.PAINT));}
        fill(l,b,9,0,-60,11,4,-60,st(Kind.PAINT));fill(l,b,9,4,-59,11,4,-57,st(Kind.PAINT));
        for(int i=0;i<3;i++){int z=-57-i,y=-2-i;fill(l,b,9,-5,z,11,y-1,z,Blocks.STONE_BRICKS);
            for(int x=9;x<=11;x++)stairs(l,b,x,y,z,Blocks.STONE_BRICK_STAIRS,Direction.SOUTH);fill(l,b,9,y+1,z,11,3,z,Blocks.AIR);}
        fill(l,b,9,0,-56,11,3,-56,Blocks.AIR);fill(l,b,9,-1,-56,11,-1,-56,st(Kind.LINO));
        lantern(l,b,10,3,-58);
        // The plant: the boiler against the north wall with its gauge, the vent valve and the restart switch beside it.
        prop(l,b,BOILER,HotelPropBlock.Kind.BOILER,Direction.SOUTH);prop(l,b,GAUGE,HotelPropBlock.Kind.GAUGE,Direction.SOUTH);
        put(l,b,VALVE,Kind.VALVE,Direction.SOUTH);put(l,b,RESTART,Kind.SWITCH,Direction.SOUTH);put(l,b,PLANT,Kind.NOTICE,Direction.SOUTH);
        for(int x=-9;x<=9;x++)if(Math.abs(x)>1)set(l,b,x,-2,-66,Blocks.CUT_COPPER);
        for(int z=-60;z>=-65;z--)set(l,b,1,-2,z,Blocks.CUT_COPPER);
        fill(l,b,-10,-4,-66,-8,-3,-64,Blocks.COAL_BLOCK);for(int x=-10;x<=-8;x++)set(l,b,x,-4,-63,Blocks.STONE_BRICK_WALL);
        furniture(l,b,8,-4,-66,HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.SOUTH);detail(l,b,8,-3,-66,SceneDetailBlock.Kind.TOOLS,Direction.SOUTH);
        for(int x:new int[]{-6,0,7})lantern(l,b,x,-2,-62);
    }

    private static void upstairs(ServerLevel l,BlockPos b){
        // The guest corridor: up the stair to the landing, west along the front, then north past 217 to the corridor's
        // end, carpet over boards, panelled to the knee, papered above, exactly as the numbered corridor beyond it is.
        var open=new HashSet<Long>();
        for(int x=8;x<=10;x++)for(int z=-14;z<=-9;z++)open.add(BlockPos.asLong(x,0,z));
        for(int x=-15;x<=7;x++)for(int z=-14;z<=-12;z++)open.add(BlockPos.asLong(x,0,z));
        for(int x=-15;x<=-13;x++)for(int z=-44;z<=-15;z++)open.add(BlockPos.asLong(x,0,z));
        for(long column:open){var c=BlockPos.of(column);
            for(var side:Direction.Plane.HORIZONTAL){var n=c.relative(side);if(open.contains(BlockPos.asLong(n.getX(),0,n.getZ())))continue;
                for(int y=5;y<=9;y++)set(l,b,n.getX(),y,n.getZ(),y==5?st(Kind.CEILING):hallWall(y));}
            set(l,b,c.getX(),5,c.getZ(),st(Kind.CEILING));fill(l,b,c.getX(),6,c.getZ(),c.getX(),8,c.getZ(),Blocks.AIR);
            set(l,b,c.getX(),9,c.getZ(),HouseBlocks.HOTEL_CEILING.get().defaultBlockState());
            if(c.getZ()<=-12)set(l,b,c.getX(),6,c.getZ(),HouseBlocks.HOTEL_CARPET.get());}
        // The stairwell under the landing's near end, and the top tread level with the landing.
        fill(l,b,8,5,-10,10,5,-9,Blocks.AIR);
        for(int x=8;x<=10;x++)stairs(l,b,x,5,-11,Blocks.DARK_OAK_STAIRS,Direction.NORTH);
        for(int x:new int[]{4,-2,-8})lantern(l,b,x,8,-13);
        for(int z=-18;z>=-42;z-=6)lantern(l,b,-14,8,z);
        // Room 217, the linen room and the family's rooms, with their own walls behind the corridor's.
        var boards=st(Kind.CEILING);var wainscot=HouseBlocks.HOTEL_WAINSCOT.get().defaultBlockState();var rose=st(Kind.ROSE);var ceiling=HouseBlocks.HOTEL_CEILING.get().defaultBlockState();
        room(l,b,ROOM_X0,ROOM_X1,ROOM_Z0,ROOM_Z1,6,8,new Finish(boards,wainscot,0,rose,null,ceiling));
        room(l,b,-22,-18,-38,-33,6,8,new Finish(boards,st(Kind.PAINT_LOW),0,st(Kind.PAINT),null,ceiling));
        room(l,b,-27,-18,-50,-41,6,8,new Finish(boards,wainscot,0,rose,null,ceiling));
        // Their doors: on the corridor's side of the wall, with the inner leaf of the wall cut back behind them.
        for(var at:List.of(ROOM_DOOR,new BlockPos(-16,6,-35),new BlockPos(-16,6,-43))){fill(l,b,-17,6,at.getZ(),-17,7,at.getZ(),Blocks.AIR);door(l,b,at,Direction.EAST,Blocks.SPRUCE_DOOR);}
        // The numbered doors that stay locked, each with its brass plate just before it.
        for(var room:LOCKED){door(l,b,room.door(),room.facing(),Blocks.SPRUCE_DOOR);plaque(l,b,room.door(),room.facing(),room.start(),room.number());}
        plaque(l,b,ROOM_DOOR,Direction.EAST,Direction.SOUTH,217);
        // Across from 217, the fire hose on its rack.
        prop(l,b,HOSE_RACK,HotelPropBlock.Kind.HOSE_RACK,Direction.WEST);
        room217(l,b);
        linen(l,b);
        family(l,b);
    }
    /** The corridor's wall at each height, as the numbered corridor has it. */
    private static BlockState hallWall(int y){
        return y<=6?HouseBlocks.HOTEL_WAINSCOT.get().defaultBlockState():y>=9?HouseBlocks.HOTEL_CEILING.get().defaultBlockState():HouseBlocks.HOTEL_WALLPAPER.get().defaultBlockState();
    }
    private static void plaque(ServerLevel l,BlockPos b,BlockPos door,Direction into,Direction start,int number){
        var at=b.offset(door.relative(into).relative(start).above());
        BuildBlocks.set(l,at,HouseBlocks.HOTEL_ROOM_PLAQUE.get().defaultBlockState().setValue(HotelRoomPlaqueBlock.FACING,into),F);
        BuildBlocks.after(l,()->HotelRoomPlaqueBlockEntity.number(l,at,number));
    }

    private static void room217(ServerLevel l,BlockPos b){
        NovelRooms.bed(l,b.offset(BED),Blocks.WHITE_BED,Direction.SOUTH);
        for(int x:new int[]{-24,-22})furniture(l,b,x,6,-16,HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.NORTH);
        set(l,b,CANDLE.getX(),CANDLE.getY(),CANDLE.getZ(),Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.LIT,true));
        detail(l,b,-24,7,-16,SceneDetailBlock.Kind.TABLE_LAMP,Direction.NORTH);
        furniture(l,b,-27,6,-17,HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.EAST);put(l,b,HOUSEKEEPING,Kind.CARD,Direction.EAST);
        furniture(l,b,DRAWER.getX(),DRAWER.getY(),DRAWER.getZ(),HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.WEST);detail(l,b,-18,7,-18,SceneDetailBlock.Kind.TOWELS,Direction.WEST);
        furniture(l,b,-27,6,-21,HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.EAST);furniture(l,b,-26,6,-21,HouseholdFurnitureBlock.Kind.FOOTSTOOL,Direction.EAST);
        furniture(l,b,-18,6,-22,HouseholdFurnitureBlock.Kind.RADIATOR,Direction.WEST);
        fill(l,b,-26,6,-20,-23,6,-18,Blocks.RED_CARPET);
        // The pale boards over the dinner table, patched from below.
        prop(l,b,PATCH,HotelPropBlock.Kind.PATCH,Direction.NORTH);
        // The bathroom behind a papered partition: the tub under the window, the basin, tiles.
        fill(l,b,-27,6,-23,-22,8,-23,st(Kind.ROSE));fill(l,b,-22,6,-26,-22,8,-24,st(Kind.ROSE));
        set(l,b,-25,6,-23,Blocks.AIR);set(l,b,-25,7,-23,Blocks.AIR);
        for(int y=6;y<=8;y++){fill(l,b,-28,y,-26,-28,y,-24,st(Kind.WHITE_TILE));fill(l,b,-27,y,-27,-23,y,-27,st(Kind.WHITE_TILE));}
        set(l,b,-27,6,-25,StanleyBlock.of(Kind.TUB,Direction.NORTH,0));set(l,b,-27,6,-24,StanleyBlock.of(Kind.TUB,Direction.NORTH,1));
        put(l,b,-23,6,-26,Kind.SINK,Direction.SOUTH);fill(l,b,-25,6,-26,-24,6,-25,Blocks.WHITE_CARPET);
        lantern(l,b,-22,8,-19);lantern(l,b,-25,8,-25);
    }

    private static void linen(ServerLevel l,BlockPos b){
        for(int z=-33;z>=-38;z--)put(l,b,-22,6,z,Kind.LINEN_SHELF,Direction.EAST);
        put(l,b,-19,6,-34,Kind.CART,Direction.SOUTH);
        set(l,b,-18,6,-38,Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING,Direction.WEST));
        lantern(l,b,-20,8,-36);
    }

    private static void family(ServerLevel l,BlockPos b){
        // The caretaker's family's rooms. Two small beds made, the children's trunks at their feet, a note on the table.
        NovelRooms.bed(l,b.offset(-26,6,-48),Blocks.RED_BED,Direction.NORTH);NovelRooms.bed(l,b.offset(-24,6,-48),Blocks.BLUE_BED,Direction.NORTH);
        for(int x:new int[]{-26,-24})put(l,b,x,6,-47,Kind.TRUNK,Direction.SOUTH);
        furniture(l,b,-22,6,-44,HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.EAST);put(l,b,FAMILY_NOTE,Kind.CARD,Direction.EAST);
        furniture(l,b,-21,6,-48,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.WEST);detail(l,b,-19,6,-50,SceneDetailBlock.Kind.TOYS,Direction.NORTH);
        furniture(l,b,-18,6,-46,HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.WEST);
        fill(l,b,-26,6,-45,-23,6,-42,Blocks.BLUE_CARPET);
        lantern(l,b,-22,8,-46);
    }

    // ------------------------------------------------------------------------------------------------ the grounds
    /**
     * The grounds behind the hotel: the rear wall the back door stands in, seen from outside, a terrace, the topiary walk, the
     * hedge maze with the master key at its heart, and the pet cemetery behind it, all under snow in a blizzard.
     */
    private static void grounds(ServerLevel l,BlockPos b){
        fill(l,b,-28,-2,-69,28,16,0,Blocks.AIR);
        fill(l,b,-28,-3,-69,28,-2,0,Blocks.DIRT);fill(l,b,-28,-1,-69,28,-1,0,Blocks.SNOW_BLOCK);
        for(int x=-28;x<=28;x++)for(int z=-68;z<=0;z++)if((x*31+z*17&7)<3)set(l,b,x,0,z,Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS,1+((x+z)&1)));
        // Snow banks rise at the sides, a hedge closes the far end.
        for(int x=-28;x<=28;x++)for(int z=-68;z<=0;z++)if(Math.abs(x)>25){int h=Math.min(5,Math.abs(x)-25);fill(l,b,x,0,z,x,h,z,Blocks.SNOW_BLOCK);}
        fill(l,b,-28,0,-69,28,4,-69,Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true));
        facade(l,b);
        // The terrace along the back of the hotel, its balustrade, benches and lamps.
        fill(l,b,-12,-1,-6,12,-1,0,Blocks.POLISHED_ANDESITE);fill(l,b,-12,0,-6,12,0,0,Blocks.AIR);
        for(int x=-12;x<=12;x++)if(Math.abs(x)>2)set(l,b,x,0,-7,Blocks.DIORITE_WALL);
        for(int z=-6;z<=-1;z++)for(int x:new int[]{-12,12})set(l,b,x,0,z,Blocks.DIORITE_WALL);
        for(int x:new int[]{-12,-3,3,12})set(l,b,x,1,-7,Blocks.POTTED_DEAD_BUSH);
        for(int x:new int[]{-8,8})for(int dx=-1;dx<=1;dx++)stairs(l,b,x+dx,0,-5,Blocks.SPRUCE_STAIRS,Direction.SOUTH);
        // The back door's canopy and lamps.
        for(int x=-2;x<=2;x++)for(int z=-1;z<=0;z++)topSlab(l,b,x,3,z,Blocks.DARK_OAK_SLAB);
        for(int x:new int[]{-2,2})fill(l,b,x,0,-1,x,2,-1,Blocks.DARK_OAK_FENCE);
        for(int x:new int[]{-1,1})put(l,b,x,2,0,Kind.SCONCE,Direction.NORTH);
        // The walk between the topiary, gravel under the snow, lamps on posts.
        fill(l,b,-1,-1,-26,1,-1,-8,Blocks.GRAVEL);fill(l,b,-1,0,-26,1,0,-8,Blocks.AIR);
        for(int z:new int[]{-12,-20})for(int x:new int[]{-3,3}){fill(l,b,x,0,z,x,1,z,Blocks.DARK_OAK_FENCE);set(l,b,x,2,z,Blocks.LANTERN);}
        for(int x:new int[]{-12,12}){fill(l,b,x,1,-7,x,1,-7,Blocks.DARK_OAK_FENCE);set(l,b,x,2,-7,Blocks.LANTERN);}
        // Hedge animals clipped in spruce, snow on their backs: lions either side of the maze, a rabbit and a dog in the drifts.
        topiary(l,b,-5,-24,LION);topiary(l,b,5,-24,LION);topiary(l,b,-14,-16,RABBIT);topiary(l,b,14,-16,DOG);
        maze(l,b);
        cemetery(l,b);
        // The groundskeeper's open shed by the west bank.
        for(int x:new int[]{-24,-18})for(int z:new int[]{-25,-18})fill(l,b,x,0,z,x,3,z,Blocks.SPRUCE_LOG);
        fill(l,b,-25,4,-26,-17,4,-17,Blocks.SPRUCE_PLANKS);fill(l,b,-25,5,-26,-17,5,-17,Blocks.SNOW);
        furniture(l,b,-23,0,-23,HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.EAST);detail(l,b,-23,1,-23,SceneDetailBlock.Kind.TOOLS,Direction.EAST);
        detail(l,b,-19,0,-19,SceneDetailBlock.Kind.ROPE_COIL,Direction.NORTH);
        LabyrinthBuilder.doors(l,b,LabyrinthPlace.HOTEL_GROUNDS);
    }

    /** The hotel's rear wall from outside: siding, the ground floor's lit windows, the upper floor shuttered, a cornice and the roof. */
    private static void facade(ServerLevel l,BlockPos b){
        var siding=st(Kind.SIDING);
        // The mass of the building behind the wall; the back door's copy of the porch replaces what is in front of its door.
        fill(l,b,-28,-1,2,28,10,18,siding);
        fill(l,b,-28,-1,1,28,9,1,siding);fill(l,b,-28,-1,1,28,-1,1,Blocks.STONE_BRICKS);
        fill(l,b,-28,5,1,28,5,1,st(Kind.EAVE));
        for(int x:REAR_WINDOWS){set(l,b,x,1,1,StanleyBlock.of(Kind.WINDOW_LOW,Direction.SOUTH));set(l,b,x,2,1,StanleyBlock.of(Kind.WINDOW_HIGH,Direction.SOUTH));
            if(Math.abs(x)>2)for(int y=7;y<=8;y++)set(l,b,x,y,1,StanleyBlock.of(Kind.SHUTTERS,Direction.NORTH));}
        // The cornice overhangs; the roof climbs away from the grounds.
        fill(l,b,-28,10,0,28,10,1,st(Kind.EAVE));
        for(int k=0;k<4;k++)fill(l,b,-28,11+k,1+k,28,11+k,18,st(Kind.ROOF));
        for(int x:new int[]{-20,20})fill(l,b,x,11,4,x+1,16,5,Blocks.BRICKS);
        // The staff door, shut from inside.
        door(l,b,STAFF_DOOR_OUTSIDE,Direction.SOUTH,Blocks.SPRUCE_DOOR);
    }

    private static final int[][] LION={{-1,0,-1},{0,0,-1},{1,0,-1},{-1,0,0},{0,0,0},{1,0,0},{-1,0,1},{1,0,1},
        {-1,1,-1},{0,1,-1},{1,1,-1},{-1,1,0},{0,1,0},{1,1,0},{-1,2,0},{0,2,0},{1,2,0},{0,2,1},{-1,3,0},{1,3,0},{0,1,-2}};
    private static final int[][] DOG={{-1,0,-1},{1,0,-1},{-1,0,1},{1,0,1},{-1,1,-1},{0,1,-1},{1,1,-1},{-1,1,0},{0,1,0},{1,1,0},
        {-1,1,1},{0,1,1},{1,1,1},{0,2,1},{0,2,2},{0,3,1},{0,2,-2}};
    private static final int[][] RABBIT={{0,0,0},{0,0,-1},{0,0,1},{-1,0,0},{1,0,0},{0,1,0},{0,1,1},{0,2,1},{-1,3,1},{1,3,1}};
    private static void topiary(ServerLevel l,BlockPos b,int x,int z,int[][] shape){
        var leaves=Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true);var cells=new HashSet<Long>();
        for(int[] o:shape){set(l,b,x+o[0],o[1],z+o[2],leaves);cells.add(BlockPos.asLong(x+o[0],o[1],z+o[2]));}
        for(int[] o:shape)if(!cells.contains(BlockPos.asLong(x+o[0],o[1]+1,z+o[2])))set(l,b,x+o[0],o[1]+1,z+o[2],Blocks.SNOW);
    }
    private static void maze(ServerLevel l,BlockPos b){
        // A deterministic perfect maze; two-cell corridors keep snow from trapping a visitor.
        java.util.Random random=new java.util.Random(217237);boolean[][] seen=new boolean[7][7];var stack=new ArrayDeque<int[]>();seen[3][6]=true;stack.push(new int[]{3,6});
        fill(l,b,-14,0,-55,14,3,-27,Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true));
        for(int x=0;x<7;x++)for(int z=0;z<7;z++)fill(l,b,-13+x*4,0,-54+z*4,-11+x*4,4,-52+z*4,Blocks.AIR);
        while(!stack.isEmpty()){var cell=stack.peek();var choices=new ArrayList<int[]>();for(var d:new int[][]{{1,0},{-1,0},{0,1},{0,-1}}){int nx=cell[0]+d[0],nz=cell[1]+d[1];if(nx>=0&&nx<7&&nz>=0&&nz<7&&!seen[nx][nz])choices.add(new int[]{nx,nz});}if(choices.isEmpty()){stack.pop();continue;}var next=choices.get(random.nextInt(choices.size()));seen[next[0]][next[1]]=true;
            int x0=-12+cell[0]*4,z0=-53+cell[1]*4,x1=-12+next[0]*4,z1=-53+next[1]*4;fill(l,b,Math.min(x0,x1)-1,0,Math.min(z0,z1)-1,Math.max(x0,x1)+1,4,Math.max(z0,z1)+1,Blocks.AIR);stack.push(next);}
        fill(l,b,-1,0,-28,1,3,-25,Blocks.AIR);fill(l,b,-2,0,-41,2,4,-37,Blocks.AIR);
        set(l,b,KEY.getX(),KEY.getY()-1,KEY.getZ(),Blocks.CHISELED_STONE_BRICKS);
    }

    private static void cemetery(ServerLevel l,BlockPos b){
        // A picket fence across behind the maze with a gate in the middle, and down both sides; the plots are dug as names come.
        for(int x=-25;x<=25;x++)if(x!=0)set(l,b,x,0,-57,Blocks.SPRUCE_FENCE);
        set(l,b,0,0,-57,Blocks.SPRUCE_FENCE_GATE.defaultBlockState().setValue(FenceGateBlock.FACING,Direction.NORTH));
        for(int z=-58;z>=-68;z--)for(int x:new int[]{-25,25})set(l,b,x,0,z,Blocks.SPRUCE_FENCE);
        fill(l,b,-24,0,-68,24,0,-58,Blocks.AIR);
        for(int x:new int[]{-24,24}){prop(l,b,new BlockPos(x,0,-58),HotelPropBlock.Kind.HEADSTONE,Direction.SOUTH);set(l,b,x,-1,-58,Blocks.GRAVEL);}
    }
}
