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
        PHOTO=new BlockPos(27,2,-19),KEY=new BlockPos(0,0,-39),BOILER=new BlockPos(0,-4,-60),FIRE=new BlockPos(9,0,-23);
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
        box(l,b,15,0,-33,25,0,-30,Blocks.DARK_OAK_PLANKS);prop(l,b,new BlockPos(20,1,-32),HotelPropBlock.Kind.PIANO,Direction.SOUTH);
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
        architecturalFinish(l,b);
        LabyrinthBuilder.entrance(l,b,HouseBlocks.HOTEL_WALLPAPER.get().defaultBlockState(),Blocks.DARK_OAK_PLANKS.defaultBlockState(),HouseBlocks.HOTEL_CEILING.get().defaultBlockState());LabyrinthBuilder.doors(l,b,LabyrinthPlace.HOTEL);
    }
    private static boolean wallMaterial(net.minecraft.world.level.block.state.BlockState s){return s.is(HouseBlocks.HOTEL_WALLPAPER.get())||s.is(HouseBlocks.HOTEL_WAINSCOT.get())||s.is(HouseBlocks.HOTEL_CEILING.get());}
    /** Authored construction for each actual room, rather than a veneer over the large outside shell. */
    private static void finishRoom(ServerLevel l,BlockPos b,int x0,int x1,int z0,int z1,int y,int h){
        for(int x=x0-1;x<=x1+1;x++)for(int z:new int[]{z0-1,z1+1})for(int dy=0;dy<=h;dy++){
            var at=b.offset(x,y+dy,z);var s=l.getBlockState(at);if(!wallMaterial(s))continue;
            if(x==x0-1||x==x1+1||(x-x0)%4==0)l.setBlock(at,Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(),F);
            else if(dy==h||dy==2)l.setBlock(at,Blocks.DARK_OAK_PLANKS.defaultBlockState(),F);
        }
        for(int z=z0;z<=z1;z++)for(int x:new int[]{x0-1,x1+1})for(int dy=0;dy<=h;dy++){
            var at=b.offset(x,y+dy,z);var s=l.getBlockState(at);if(!wallMaterial(s))continue;
            if((z-z0)%5==0)l.setBlock(at,Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(),F);
            else if(dy==h||dy==2)l.setBlock(at,Blocks.DARK_OAK_PLANKS.defaultBlockState(),F);
        }
        for(int x=x0;x<=x1;x++)for(int z=z0;z<=z1;z++){
            var floor=b.offset(x,y-1,z);if((x==x0||x==x1||z==z0||z==z1)&&l.getBlockState(floor).is(Blocks.DARK_OAK_PLANKS))l.setBlock(floor,Blocks.STRIPPED_DARK_OAK_WOOD.defaultBlockState(),F);
            if((x-x0)%4!=0&&(z-z0)%5!=0)continue;var ceiling=b.offset(x,y+h+1,z);if(l.getBlockState(ceiling).is(HouseBlocks.HOTEL_CEILING.get()))l.setBlock(ceiling,Blocks.DARK_OAK_PLANKS.defaultBlockState(),F);
            var drop=b.offset(x,y+h,z);if(l.getBlockState(drop).isAir())l.setBlock(drop,Blocks.DARK_OAK_SLAB.defaultBlockState(),F);
        }
    }
    private static void caseDoor(ServerLevel l,BlockPos b,int x,int y,int z,Direction facing){var across=facing.getAxis()==Direction.Axis.X?Direction.NORTH:Direction.EAST;for(int side:new int[]{-1,1})for(int dy=0;dy<3;dy++){var at=b.offset(x,y+dy,z).relative(across,side);if(wallMaterial(l.getBlockState(at)))l.setBlock(at,Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(),F);}var top=b.offset(x,y+2,z);if(l.getBlockState(top).isAir()||wallMaterial(l.getBlockState(top)))l.setBlock(top,Blocks.DARK_OAK_PLANKS.defaultBlockState(),F);}
    private static void architecturalFinish(ServerLevel l,BlockPos b){
        // The rear wing is a real office with linked linen and repair rooms, not unused floor space.
        room(l,b,13,20,-65,-50,0,3);room(l,b,22,27,-65,-50,0,3);room(l,b,13,27,-47,-41,0,3);
        portal(l,b,18,0,-40,Direction.SOUTH);box(l,b,18,0,-49,18,2,-48,Blocks.AIR);NovelRooms.door(l,b.offset(18,0,-49),Direction.SOUTH,Blocks.DARK_OAK_DOOR,true);
        box(l,b,24,0,-49,24,2,-48,Blocks.AIR);NovelRooms.door(l,b.offset(24,0,-49),Direction.SOUTH,Blocks.DARK_OAK_DOOR,true);
        furniture(l,b,25,0,-43,HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.WEST);detail(l,b,25,1,-43,SceneDetailBlock.Kind.FILE_TRAY);
        furniture(l,b,23,0,-43,HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.EAST);
        for(int x:new int[]{14,16})for(int z:new int[]{-64,-58}){furniture(l,b,x,0,z,HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.SOUTH);detail(l,b,x,1,z,SceneDetailBlock.Kind.TOWELS);}
        for(int x:new int[]{15,19})l.setBlock(b.offset(x,0,-61),Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING,Direction.SOUTH),F);
        furniture(l,b,25,0,-62,HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.SOUTH);detail(l,b,25,1,-62,SceneDetailBlock.Kind.TOOLS);
        furniture(l,b,25,0,-56,HouseholdFurnitureBlock.Kind.WASHING_MACHINE,Direction.WEST);at(l,b,23,0,-62,Blocks.CRAFTING_TABLE);
        // Painted mountains sit behind actual office glass, with a timber sill and deep reveal.
        box(l,b,14,1,-48,16,2,-48,Blocks.GLASS);for(int x=14;x<=16;x++){at(l,b,x,1,-49,x==15?Blocks.WHITE_TERRACOTTA:Blocks.GRAY_TERRACOTTA);at(l,b,x,2,-49,Blocks.LIGHT_BLUE_CONCRETE);at(l,b,x,0,-48,Blocks.DARK_OAK_PLANKS);}
        for(var a:new int[][]{{-11,11,-27,-5,0,3},{-27,-17,-27,-6,0,4},{13,27,-34,-6,0,7},{-27,-17,-64,-40,0,4},{-9,9,-25,-7,5,3},{13,20,-65,-50,0,3},{22,27,-65,-50,0,3},{13,27,-47,-41,0,3}})finishRoom(l,b,a[0],a[1],a[2],a[3],a[4],a[5]);
        for(var a:new int[][]{{0,0,-4,0},{-12,0,-17,1},{-16,0,-17,1},{12,0,-17,1},{-16,0,-44,1},{0,0,-35,0},{-10,5,-13,1},{0,5,-6,0},{18,0,-40,0},{18,0,-49,0},{24,0,-49,0}})caseDoor(l,b,a[0],a[1],a[2],a[3]==1?Direction.EAST:Direction.SOUTH);
        // The bar's fitted back has a dark mirror and supported bottle shelves.
        box(l,b,-29,1,-24,-29,3,-9,Blocks.BLACK_CONCRETE);box(l,b,-28,1,-24,-28,3,-9,Blocks.BLACK_STAINED_GLASS);
        box(l,b,-27,0,-24,-27,1,-9,Blocks.DARK_OAK_PLANKS);for(int z:new int[]{-10,-15,-22})detail(l,b,-27,2,z,SceneDetailBlock.Kind.BOTTLES);
        box(l,b,8,2,-24,10,2,-24,Blocks.DARK_OAK_SLAB);at(l,b,9,3,-24,Blocks.STONE_BRICKS);
        furniture(l,b,-20,0,-52,HouseholdFurnitureBlock.Kind.FLORAL_ARMCHAIR,Direction.EAST);furniture(l,b,-19,0,-52,HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.WEST);detail(l,b,-19,1,-52,SceneDetailBlock.Kind.TEA_SET);
        furniture(l,b,8,5,-18,HouseholdFurnitureBlock.Kind.RADIATOR,Direction.WEST);detail(l,b,-6,6,-21,SceneDetailBlock.Kind.TOWELS);
        for(int i=0;i<=4;i++)at(l,b,-16,i,-7-i,Blocks.DARK_OAK_FENCE);for(int x=-15;x<=-11;x++)at(l,b,x,5,-14,Blocks.DARK_OAK_FENCE);
        // Foyer ribs and the corridor's structural uprights frame the long sightline.
        for(int z=-8;z>=-62;z-=9)for(int x:new int[]{-16,-12})for(int y=1;y<=3;y++){var at=b.offset(x,y,z);if(wallMaterial(l.getBlockState(at)))l.setBlock(at,Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(),F);}
        for(int x:new int[]{-5,5})for(int y=0;y<=3;y++)at(l,b,x,y,-3,Blocks.STRIPPED_DARK_OAK_LOG);
        // The heating plant belongs to masonry and copper, not the guest-room wallpaper.
        for(int x=-10;x<=10;x++)for(int z:new int[]{-65,-48})for(int y=-4;y<=-2;y++){var at=b.offset(x,y,z);if(wallMaterial(l.getBlockState(at)))l.setBlock(at,Blocks.STONE_BRICKS.defaultBlockState(),F);}
        for(int z=-64;z<=-49;z++)for(int x:new int[]{-10,10})for(int y=-4;y<=-2;y++){var at=b.offset(x,y,z);if(wallMaterial(l.getBlockState(at)))l.setBlock(at,Blocks.STONE_BRICKS.defaultBlockState(),F);}
        at(l,b,-3,-4,-61,Blocks.IRON_BLOCK);detail(l,b,-3,-3,-61,SceneDetailBlock.Kind.TOOLS);at(l,b,-4,-4,-61,Blocks.CRAFTING_TABLE);
        for(int z:new int[]{-52,-57,-62})at(l,b,-6,-3,z,Blocks.STONE_BRICK_WALL);
        lamp(l,b,18,2,-44);lamp(l,b,17,2,-57);lamp(l,b,25,2,-59);
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
        for(int x:new int[]{-24,-18})for(int z:new int[]{-25,-18})box(l,b,x,0,z,x,3,z,Blocks.SPRUCE_LOG);
        box(l,b,-25,4,-26,-17,4,-17,Blocks.SPRUCE_PLANKS);box(l,b,-25,5,-26,-17,5,-17,Blocks.SNOW);
        furniture(l,b,-23,0,-23,HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.EAST);detail(l,b,-23,1,-23,SceneDetailBlock.Kind.TOOLS);
        NovelRooms.safeApproach(l,b);LabyrinthBuilder.doors(l,b,LabyrinthPlace.HOTEL_GROUNDS);
    }
}
