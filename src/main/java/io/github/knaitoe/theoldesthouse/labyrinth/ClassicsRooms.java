package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;

/** Furnished, framed wings with real supports, clear approaches and physical reading surfaces. */
public final class ClassicsRooms {
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    public static final BlockPos CUPBOARD=new BlockPos(-8,0,-13),GRACE=new BlockPos(-5,0,-26),ALBUM=new BlockPos(6,0,-27),SITTING=new BlockPos(-4,0,-10),GATE=new BlockPos(5,0,-22);
    public static final List<BlockPos> CANDLES=List.of(new BlockPos(-1,1,-15),new BlockPos(0,1,-16),new BlockPos(-7,1,-19));
    public static final List<BlockPos> SHUTTERS=List.of(new BlockPos(10,2,-24),new BlockPos(10,2,-25),new BlockPos(10,2,-26));
    public static final List<BlockPos> PANELS=List.of(new BlockPos(-4,1,-28),new BlockPos(-1,1,-28),new BlockPos(2,1,-28),new BlockPos(5,1,-28));
    public static final BlockPos NURSERY_DESK=new BlockPos(-5,0,-23);
    private ClassicsRooms(){}
    public static void build(ServerLevel level,BlockPos base,LabyrinthPlace place){if(place==LabyrinthPlace.SEANCE)seance(level,base);else nursery(level,base);}
    private static void at(ServerLevel l,BlockPos b,int x,int y,int z,Block block){l.setBlock(b.offset(x,y,z),block.defaultBlockState(),F);}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,Block block){NovelRooms.box(l,b,x0,y0,z0,x1,y1,z1,block.defaultBlockState());}
    private static void lectern(ServerLevel l,BlockPos at,ItemStack book){l.setBlock(at,Blocks.LECTERN.defaultBlockState(),F);if(l.getBlockEntity(at) instanceof LecternBlockEntity d){d.setBook(book);d.setChanged();}}
    private static void frame(ServerLevel l,BlockPos b,int x,int z,Direction direction){
        if(direction.getAxis()==Direction.Axis.Z){for(int side:new int[]{-1,1})box(l,b,x+side,0,z,x+side,3,z,Blocks.STRIPPED_DARK_OAK_LOG);box(l,b,x-1,3,z,x+1,3,z,Blocks.DARK_OAK_PLANKS);}
        else {for(int side:new int[]{-1,1})box(l,b,x,0,z+side,x,3,z+side,Blocks.STRIPPED_DARK_OAK_LOG);box(l,b,x,3,z-1,x,3,z+1,Blocks.DARK_OAK_PLANKS);}
        box(l,b,x,0,z,x,2,z,Blocks.AIR);
    }
    private static void furniture(ServerLevel l,BlockPos b,int x,int y,int z,HouseholdFurnitureBlock.Kind kind,Direction facing){NovelRooms.furniture(l,b.offset(x,y,z),kind,facing);}
    private static void seance(ServerLevel l,BlockPos b){
        LabyrinthBuilder.room(l,b,-10,10,6,-31,-8,ClassicsRegistry.SEANCE_WALL.get().defaultBlockState(),Blocks.DARK_OAK_PLANKS.defaultBlockState(),Blocks.STRIPPED_BIRCH_WOOD.defaultBlockState());
        LabyrinthBuilder.room(l,b,-3,3,4,-8,-1,Blocks.SMOOTH_SANDSTONE.defaultBlockState(),Blocks.DARK_OAK_PLANKS.defaultBlockState(),Blocks.BIRCH_PLANKS.defaultBlockState());
        for(int x=-10;x<=10;x++){at(l,b,x,0,-32,ClassicsRegistry.WAINSCOT.get());if(Math.abs(x)>3)at(l,b,x,0,-7,ClassicsRegistry.WAINSCOT.get());}
        for(int z=-31;z<=-8;z++)for(int x:new int[]{-11,11}){at(l,b,x,0,z,ClassicsRegistry.WAINSCOT.get());at(l,b,x,6,z,Blocks.DARK_OAK_PLANKS);}
        for(int z:new int[]{-10,-16,-22,-28})box(l,b,-10,6,z,10,6,z,Blocks.DARK_OAK_LOG);
        box(l,b,-3,0,-8,3,4,-8,ClassicsRegistry.SEANCE_WALL.get());frame(l,b,0,-8,Direction.SOUTH);
        for(int x=-10;x<=10;x++)for(int y=0;y<=6;y++)at(l,b,x,y,-22,y==0?ClassicsRegistry.WAINSCOT.get():Blocks.SMOOTH_SANDSTONE);
        box(l,b,0,0,-31,0,6,-23,Blocks.DARK_OAK_PLANKS);
        frame(l,b,-5,-22,Direction.SOUTH);NovelRooms.door(l,b.offset(-5,0,-22),Direction.SOUTH,Blocks.DARK_OAK_DOOR,false);
        frame(l,b,5,-22,Direction.SOUTH);NovelRooms.door(l,b.offset(GATE),Direction.SOUTH,Blocks.IRON_DOOR,false);
        // A marquetry table sits on its own modeled legs. Candle blocks have actual support.
        for(int x=-1;x<=0;x++)for(int z=-16;z<=-15;z++)at(l,b,x,0,z,ClassicsRegistry.TABLE.get());
        furniture(l,b,-3,0,-15,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.EAST);
        furniture(l,b,2,0,-15,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.WEST);
        furniture(l,b,0,0,-13,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.NORTH);
        furniture(l,b,0,0,-18,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.SOUTH);
        for(int z=-19;z<=-17;z++)box(l,b,7,0,z,7,2,z,Blocks.BOOKSHELF);
        furniture(l,b,-7,0,-19,HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.EAST);
        for(BlockPos candle:CANDLES)l.setBlock(b.offset(candle),Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.LIT,true).setValue(CandleBlock.CANDLES,2),F);
        at(l,b,CUPBOARD.getX(),0,CUPBOARD.getZ(),Blocks.CHEST);
        lectern(l,b.offset(SITTING),ClassicsTexts.medium());lectern(l,b.offset(GRACE),ClassicsTexts.grace());lectern(l,b.offset(ALBUM),ClassicsTexts.album());
        furniture(l,b,-7,0,-27,HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.EAST);
        box(l,b,7,0,-29,7,2,-29,Blocks.BOOKSHELF);
        for(int z=-26;z<=-24;z++){at(l,b,11,2,z,Blocks.GLASS);at(l,b,12,2,z,Blocks.BLACK_CONCRETE);}
        for(BlockPos shutter:SHUTTERS)l.setBlock(b.offset(shutter),Blocks.DARK_OAK_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.FACING,Direction.EAST).setValue(TrapDoorBlock.HALF,Half.TOP).setValue(TrapDoorBlock.OPEN,false),F);
        // The landing and both rear rooms have lamps chained to their real ceilings.
        box(l,b,0,3,-6,0,4,-6,Blocks.CHAIN);l.setBlock(b.offset(0,2,-6),Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true),F);
        for(int x:new int[]{-5,5}){box(l,b,x,5,-25,x,6,-25,Blocks.CHAIN);l.setBlock(b.offset(x,4,-25),Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true),F);}
        // A fireplace with a closed iron hearth, mantel, stone jambs and soot above it.
        box(l,b,-9,0,-12,-7,0,-10,Blocks.POLISHED_ANDESITE);
        box(l,b,-9,1,-12,-9,3,-12,Blocks.STONE_BRICKS);box(l,b,-7,1,-12,-7,3,-12,Blocks.STONE_BRICKS);
        box(l,b,-9,3,-12,-7,3,-12,Blocks.DARK_OAK_SLAB);at(l,b,-8,1,-12,Blocks.IRON_BARS);at(l,b,-8,2,-12,Blocks.COAL_BLOCK);
        LabyrinthBuilder.entrance(l,b,ClassicsRegistry.SEANCE_WALL.get().defaultBlockState(),Blocks.DARK_OAK_PLANKS.defaultBlockState(),Blocks.BIRCH_PLANKS.defaultBlockState());LabyrinthBuilder.doors(l,b,LabyrinthPlace.SEANCE);
    }
    private static void nursery(ServerLevel l,BlockPos b){
        LabyrinthBuilder.room(l,b,-8,8,6,-27,-7,ClassicsRegistry.WALLPAPER.get().defaultBlockState(),Blocks.BIRCH_PLANKS.defaultBlockState(),Blocks.SMOOTH_SANDSTONE.defaultBlockState());
        LabyrinthBuilder.room(l,b,-3,3,4,-7,-1,Blocks.WHITE_TERRACOTTA.defaultBlockState(),Blocks.BIRCH_PLANKS.defaultBlockState(),Blocks.BIRCH_PLANKS.defaultBlockState());
        box(l,b,-3,0,-7,3,4,-7,Blocks.WHITE_TERRACOTTA);frame(l,b,0,-7,Direction.SOUTH);
        for(int z=-27;z<=-7;z++)for(int x:new int[]{-9,9}){at(l,b,x,0,z,Blocks.STRIPPED_BIRCH_WOOD);at(l,b,x,6,z,Blocks.BIRCH_PLANKS);}
        for(int x=-8;x<=8;x++){at(l,b,x,0,-28,Blocks.STRIPPED_BIRCH_WOOD);at(l,b,x,6,-28,Blocks.BIRCH_PLANKS);if(Math.abs(x)>3)at(l,b,x,0,-6,Blocks.STRIPPED_BIRCH_WOOD);}
        for(int z:new int[]{-10,-18,-25})box(l,b,-8,6,z,8,6,z,Blocks.STRIPPED_OAK_LOG);
        // The blocked windows have glass, deep reveals, iron bars and a real bright backing.
        for(int z:new int[]{-12,-20})for(int y=1;y<=3;y++){
            at(l,b,-9,y,z,Blocks.GLASS);at(l,b,-10,y,z,Blocks.WHITE_CONCRETE);at(l,b,-8,y,z,Blocks.IRON_BARS);
            at(l,b,9,y,z,Blocks.GLASS);at(l,b,10,y,z,Blocks.WHITE_CONCRETE);at(l,b,8,y,z,Blocks.IRON_BARS);
        }
        NovelRooms.bed(l,b.offset(4,0,-24),Blocks.WHITE_BED,Direction.NORTH);
        for(int z=-25;z<=-24;z++)for(int x:new int[]{3,5})at(l,b,x,0,z,Blocks.IRON_BARS);
        furniture(l,b,-5,0,-23,HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.SOUTH);
        furniture(l,b,-5,0,-21,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.NORTH);
        furniture(l,b,5,0,-10,HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.WEST);
        box(l,b,-5,0,-10,-5,2,-10,Blocks.BOOKSHELF);
        furniture(l,b,4,0,-19,HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.WEST);
        at(l,b,4,1,-19,Blocks.FLOWER_POT);
        for(int z=-16;z<=-13;z++)for(int x=-3;x<=3;x++)at(l,b,x,0,z,(x==-3||x==3||z==-16||z==-13)?Blocks.BROWN_CARPET:Blocks.YELLOW_CARPET);
        // Four low seams can be watched from the floor. Ordinary wallpaper remains around them.
        for(BlockPos panel:PANELS){l.setBlock(b.offset(panel),ClassicsRegistry.WALLPAPER.get().defaultBlockState(),F);at(l,b,panel.getX(),2,panel.getZ(),ClassicsRegistry.WALLPAPER.get());}
        for(int z:new int[]{-5,-18}){box(l,b,0,z==-5?3:5,z,0,z==-5?4:6,z,Blocks.CHAIN);l.setBlock(b.offset(0,z==-5?2:4,z),Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true),F);}
        LabyrinthBuilder.entrance(l,b,Blocks.WHITE_TERRACOTTA.defaultBlockState(),Blocks.BIRCH_PLANKS.defaultBlockState(),Blocks.BIRCH_PLANKS.defaultBlockState());LabyrinthBuilder.doors(l,b,LabyrinthPlace.WALLPAPER_NURSERY);
    }
}
