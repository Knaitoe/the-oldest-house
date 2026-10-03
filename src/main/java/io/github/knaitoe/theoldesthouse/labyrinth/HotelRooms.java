package io.github.knaitoe.theoldesthouse.labyrinth;
import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.properties.*;
/** A physical hotel: 217 over the last table, two wings, service stairs and a separate snowbound garden. */
public final class HotelRooms {
    public static final BlockPos DESK=new BlockPos(3,0,-2),TABLE=new BlockPos(0,0,-16),MEAL=new BlockPos(0,1,-16),
        BED=new BlockPos(5,5,-21),DRAWER=new BlockPos(-6,5,-21),PATCH=new BlockPos(0,4,-16),ROOM_DOOR=new BlockPos(0,5,-6),
        LOG=new BlockPos(-22,0,-57),TYPEWRITER=new BlockPos(-24,1,-48),GAUGE=new BlockPos(0,-3,-59),VALVE=new BlockPos(4,-3,-58),RESTART=new BlockPos(6,-3,-58),
        PHOTO=new BlockPos(27,2,-20),KEY=new BlockPos(0,0,-39),BOILER=new BlockPos(0,-4,-60),FIRE=new BlockPos(9,0,-23);
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private HotelRooms(){}
    public static void build(ServerLevel l,BlockPos b,LabyrinthPlace p){if(p==LabyrinthPlace.HOTEL)hotel(l,b);else grounds(l,b);}
    private static void at(ServerLevel l,BlockPos b,int x,int y,int z,Block block){l.setBlock(b.offset(x,y,z),block.defaultBlockState(),F);}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,Block block){NovelRooms.box(l,b,x0,y0,z0,x1,y1,z1,block instanceof LeavesBlock?block.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true):block.defaultBlockState());}
    private static void prop(ServerLevel l,BlockPos b,BlockPos p,HotelPropBlock.Kind kind,Direction facing){l.setBlock(b.offset(p),HotelRegistry.PROP.get().defaultBlockState().setValue(HotelPropBlock.KIND,kind).setValue(HotelPropBlock.FACING,facing),F);}
    private static void furniture(ServerLevel l,BlockPos b,int x,int y,int z,HouseholdFurnitureBlock.Kind kind,Direction facing){NovelRooms.furniture(l,b.offset(x,y,z),kind,facing);}
    private static void paper(ServerLevel l,BlockPos b,BlockPos p,ItemStack book){l.setBlock(b.offset(p),Blocks.LECTERN.defaultBlockState(),F);if(l.getBlockEntity(b.offset(p)) instanceof LecternBlockEntity e){e.setBook(book);e.setChanged();}}
    private static void portal(ServerLevel l,BlockPos b,int x,int y,int z,Direction facing){box(l,b,x,y,z,x,y+2,z,Blocks.AIR);NovelRooms.door(l,b.offset(x,y,z),facing,Blocks.DARK_OAK_DOOR,true);}
    private static void room(ServerLevel l,BlockPos b,int x0,int x1,int z0,int z1,int y,int h){LabyrinthBuilder.room(l,b.above(y),x0,x1,h,z0,z1,HouseBlocks.HOTEL_WALLPAPER.get().defaultBlockState(),Blocks.DARK_OAK_PLANKS.defaultBlockState(),HouseBlocks.HOTEL_CEILING.get().defaultBlockState());
        for(int x=x0;x<=x1;x++)for(int z:new int[]{z0-1,z1+1})at(l,b,x,y,z,HouseBlocks.HOTEL_WAINSCOT.get());for(int z=z0;z<=z1;z++)for(int x:new int[]{x0-1,x1+1})at(l,b,x,y,z,HouseBlocks.HOTEL_WAINSCOT.get());}
    private static void lamp(ServerLevel l,BlockPos b,int x,int y,int z){at(l,b,x,y+1,z,Blocks.CHAIN);l.setBlock(b.offset(x,y,z),Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true),F);}
    private static void hotel(ServerLevel l,BlockPos b){
        room(l,b,-28,28,-66,-2,0,12);room(l,b,-5,5,-3,-1,0,3);
        room(l,b,-11,11,-27,-5,0,3);room(l,b,-27,-17,-27,-6,0,4);room(l,b,13,27,-34,-6,0,7);
        room(l,b,-27,-17,-64,-40,0,4);room(l,b,-9,9,-25,-7,5,3);
        // Corridors are carved after partitions, with continuous supported timber floors.
        box(l,b,-15,0,-65,-13,3,-5,Blocks.AIR);box(l,b,-28,0,-38,28,3,-36,Blocks.AIR);
        box(l,b,-15,-1,-65,-13,-1,-5,Blocks.DARK_OAK_PLANKS);box(l,b,-28,-1,-38,28,-1,-36,Blocks.DARK_OAK_PLANKS);
        for(int z=-64;z<=-5;z++)at(l,b,-14,0,z,HouseBlocks.HOTEL_CARPET.get());
        for(int x=-27;x<=27;x++)at(l,b,x,0,-37,HouseBlocks.HOTEL_CARPET.get());
        portal(l,b,0,0,-4,Direction.SOUTH);portal(l,b,-12,0,-17,Direction.EAST);portal(l,b,-16,0,-17,Direction.EAST);portal(l,b,12,0,-17,Direction.WEST);
        portal(l,b,-16,0,-44,Direction.EAST);portal(l,b,0,0,-35,Direction.SOUTH);
        // A real broad stair joins the dining landing to the upstairs made bed.
        for(int i=0;i<=5;i++){int z=-7-i;box(l,b,-15,-1,z,-13,i-1,z,Blocks.DARK_OAK_PLANKS);box(l,b,-15,i,z,-13,i+2,z,Blocks.AIR);for(int x=-15;x<=-13;x++)l.setBlock(b.offset(x,i-1,z),Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH),F);}
        box(l,b,-15,4,-14,0,4,-13,Blocks.DARK_OAK_PLANKS);box(l,b,-15,5,-14,0,7,-13,Blocks.AIR);
        portal(l,b,-10,5,-13,Direction.WEST);portal(l,b,0,5,-6,Direction.SOUTH);
        box(l,b,-9,4,-6,9,4,-5,Blocks.DARK_OAK_PLANKS);box(l,b,-9,5,-5,9,7,-5,Blocks.AIR);
        prop(l,b,DESK,HotelPropBlock.Kind.RECEPTION,Direction.SOUTH);paper(l,b,new BlockPos(-3,0,-2),HotelTexts.rules());
        furniture(l,b,0,0,-16,HouseholdFurnitureBlock.Kind.FORMICA_TABLE,Direction.NORTH);
        furniture(l,b,0,0,-14,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.NORTH);furniture(l,b,0,0,-18,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.SOUTH);
        for(int x:new int[]{-7,7})for(int z:new int[]{-10,-19}){furniture(l,b,x,0,z,HouseholdFurnitureBlock.Kind.FORMICA_TABLE,Direction.NORTH);furniture(l,b,x,1,z,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.SOUTH);}
        prop(l,b,new BlockPos(-7,0,-25),HotelPropBlock.Kind.PIANO,Direction.SOUTH);
        for(int x:new int[]{-4,0,4})furniture(l,b,x,0,-26,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.NORTH);
        box(l,b,8,-1,-24,10,1,-24,Blocks.STONE_BRICKS);at(l,b,9,0,-23,Blocks.CAMPFIRE);
        for(int z=-24;z<=-11;z++)box(l,b,-25,0,z,-25,0,z,Blocks.DARK_OAK_PLANKS);
        prop(l,b,new BlockPos(-25,1,-18),HotelPropBlock.Kind.MEAL,Direction.EAST);
        for(int z:new int[]{-10,-15,-22})furniture(l,b,-21,0,z,HouseholdFurnitureBlock.Kind.KITCHEN_STOOL,Direction.WEST);
        furniture(l,b,-19,0,-25,HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.EAST);
        // Ballroom parquet, raised orchestra and a framed party photograph.
        for(int x=16;x<=24;x++)for(int z=-29;z<=-12;z++)at(l,b,x,-1,z,(x+z)%2==0?Blocks.BIRCH_PLANKS:Blocks.DARK_OAK_PLANKS);
        box(l,b,15,0,-33,25,0,-31,Blocks.DARK_OAK_PLANKS);prop(l,b,new BlockPos(20,1,-32),HotelPropBlock.Kind.PIANO,Direction.SOUTH);
        prop(l,b,PHOTO,HotelPropBlock.Kind.PHOTO,Direction.WEST);
        // Actual glass reveals contain a painted mountain diorama, never an exterior view.
        for(int x=-8;x<=-3;x++){at(l,b,x,2,-28,Blocks.GLASS);at(l,b,x,2,-29,x<-6?Blocks.WHITE_TERRACOTTA:Blocks.GRAY_TERRACOTTA);at(l,b,x,3,-29,Blocks.LIGHT_BLUE_CONCRETE);}
        NovelRooms.bed(l,b.offset(BED),Blocks.WHITE_BED,Direction.NORTH);furniture(l,b,-6,5,-21,HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.EAST);
        furniture(l,b,7,5,-23,HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.WEST);at(l,b,7,6,-23,Blocks.CANDLE);
        paper(l,b,new BlockPos(-6,5,-11),HotelTexts.housekeeping());prop(l,b,PATCH,HotelPropBlock.Kind.PATCH,Direction.NORTH);
        NovelRooms.sign(l,b.offset(2,6,-5),Direction.SOUTH,new String[]{"217","","",""});
        prop(l,b,new BlockPos(2,5,-5),HotelPropBlock.Kind.HOSE_RACK,Direction.WEST);
        paper(l,b,LOG,HotelTexts.log());furniture(l,b,-24,0,-48,HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.SOUTH);prop(l,b,TYPEWRITER,HotelPropBlock.Kind.TYPEWRITER,Direction.SOUTH);
        NovelRooms.bed(l,b.offset(-23,0,-62),Blocks.BROWN_BED,Direction.NORTH);box(l,b,-25,2,-65,-19,3,-65,Blocks.GLASS);box(l,b,-25,0,-66,-19,3,-66,Blocks.SNOW_BLOCK);
        furniture(l,b,-18,0,-60,HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.WEST);
        box(l,b,-11,0,-39,11,3,-39,HouseBlocks.HOTEL_WAINSCOT.get());box(l,b,11,0,-66,11,3,-39,HouseBlocks.HOTEL_WAINSCOT.get());
        box(l,b,0,0,-39,0,2,-39,Blocks.AIR);NovelRooms.door(l,b.offset(0,0,-39),Direction.SOUTH,Blocks.IRON_DOOR,false);
        // A stair descends from the service hall into a real basement, rather than teleporting.
        room(l,b,-9,9,-64,-49,-4,2);
        for(int i=0;i<=4;i++){int z=-42-i;box(l,b,7,-5,z,9,-1-i,z,Blocks.STONE_BRICKS);box(l,b,7,-i,z,9,2,z,Blocks.AIR);for(int x=7;x<=9;x++)l.setBlock(b.offset(x,-1-i,z),Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.SOUTH),F);}
        box(l,b,7,-4,-50,9,-2,-47,Blocks.AIR);box(l,b,7,-5,-50,9,-5,-46,Blocks.STONE_BRICKS);
        prop(l,b,BOILER,HotelPropBlock.Kind.BOILER,Direction.SOUTH);prop(l,b,GAUGE,HotelPropBlock.Kind.GAUGE,Direction.SOUTH);
        at(l,b,4,-3,-58,Blocks.LEVER);at(l,b,6,-3,-58,Blocks.LEVER);paper(l,b,new BlockPos(-5,-4,-56),HotelTexts.boiler());
        box(l,b,-7,-4,-63,-7,-2,-50,Blocks.COPPER_BLOCK);box(l,b,-9,-5,-64,9,-5,-49,Blocks.STONE_BRICKS);
        for(int z:new int[]{-8,-20})lamp(l,b,0,2,z);lamp(l,b,-22,3,-18);lamp(l,b,20,6,-20);lamp(l,b,-21,3,-53);lamp(l,b,0,7,-12);
        for(int z=-8;z>=-62;z-=9)lamp(l,b,-14,2,z);
        detail(l,b,-19,1,-25,SceneDetailBlock.Kind.BOOKS); // Supported table added below, not an armchair.
        furniture(l,b,-19,0,-25,HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.EAST);
        furniture(l,b,-18,0,-48,HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.WEST);detail(l,b,-18,1,-48,SceneDetailBlock.Kind.BOTTLES);
        LabyrinthBuilder.entrance(l,b,HouseBlocks.HOTEL_WALLPAPER.get().defaultBlockState(),Blocks.DARK_OAK_PLANKS.defaultBlockState(),HouseBlocks.HOTEL_CEILING.get().defaultBlockState());LabyrinthBuilder.doors(l,b,LabyrinthPlace.HOTEL);
    }
    private static void detail(ServerLevel l,BlockPos b,int x,int y,int z,SceneDetailBlock.Kind kind){l.setBlock(b.offset(x,y,z),HouseBlocks.SCENE_DETAIL.get().defaultBlockState().setValue(SceneDetailBlock.KIND,kind).setValue(SceneDetailBlock.FACING,Direction.NORTH),F);}
    private static void grounds(ServerLevel l,BlockPos b){
        box(l,b,-27,-2,-67,27,12,0,Blocks.AIR);box(l,b,-27,-3,-67,27,-1,0,Blocks.DIRT);box(l,b,-27,0,-67,27,0,0,Blocks.SNOW);
        // Snowbanks rise at the perimeter; the sky is physically open.
        for(int x=-27;x<=27;x++)for(int z=-67;z<=0;z++)if(Math.abs(x)>24||z<-64){int h=Math.min(5,Math.max(Math.abs(x)-24,-64-z));box(l,b,x,0,z,x,h,z,Blocks.SNOW_BLOCK);}
        // A deterministic perfect maze; two-cell corridors prevent snow from trapping a visitor.
        java.util.Random random=new java.util.Random(217237);boolean[][] seen=new boolean[7][7];var stack=new ArrayDeque<int[]>();seen[3][6]=true;stack.push(new int[]{3,6});
        box(l,b,-14,0,-55,14,3,-27,Blocks.SPRUCE_LEAVES);
        for(int x=0;x<7;x++)for(int z=0;z<7;z++)box(l,b,-13+x*4,0,-54+z*4,-11+x*4,4,-52+z*4,Blocks.AIR);
        while(!stack.isEmpty()){var cell=stack.peek();var choices=new ArrayList<int[]>();for(var d:new int[][]{{1,0},{-1,0},{0,1},{0,-1}}){int nx=cell[0]+d[0],nz=cell[1]+d[1];if(nx>=0&&nx<7&&nz>=0&&nz<7&&!seen[nx][nz])choices.add(new int[]{nx,nz});}if(choices.isEmpty()){stack.pop();continue;}var next=choices.get(random.nextInt(choices.size()));seen[next[0]][next[1]]=true;
            int x0=-12+cell[0]*4,z0=-53+cell[1]*4,x1=-12+next[0]*4,z1=-53+next[1]*4;box(l,b,Math.min(x0,x1)-1,0,Math.min(z0,z1)-1,Math.max(x0,x1)+1,4,Math.max(z0,z1)+1,Blocks.AIR);stack.push(next);}
        box(l,b,-1,0,-28,1,3,-25,Blocks.AIR);box(l,b,-2,0,-41,2,4,-37,Blocks.AIR);
        at(l,b,0,-1,-39,Blocks.CHISELED_STONE_BRICKS);
        for(int x:new int[]{-19,19})for(int z:new int[]{-8,-15,-22}){furniture(l,b,x,0,z,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.NORTH);}
        for(int x:new int[]{-21,-18}){prop(l,b,new BlockPos(x,0,-60),HotelPropBlock.Kind.HEADSTONE,Direction.SOUTH);at(l,b,x,-1,-60,Blocks.GRAVEL);}
        furniture(l,b,7,0,-8,HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.WEST);detail(l,b,7,1,-8,SceneDetailBlock.Kind.ROPE_COIL);
        furniture(l,b,-7,0,-8,HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.EAST);detail(l,b,-7,1,-8,SceneDetailBlock.Kind.TOOLS);
        NovelRooms.safeApproach(l,b);LabyrinthBuilder.doors(l,b,LabyrinthPlace.HOTEL_GROUNDS);
    }
}
