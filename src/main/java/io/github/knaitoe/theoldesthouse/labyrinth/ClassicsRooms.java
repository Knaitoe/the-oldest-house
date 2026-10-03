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
    public static final List<BlockPos> SHUTTERS=List.of(new BlockPos(9,1,-24),new BlockPos(9,1,-25),new BlockPos(9,1,-26));
    public static final List<BlockPos> PANELS=List.of(new BlockPos(-4,1,-27),new BlockPos(-1,1,-27),new BlockPos(2,1,-27),new BlockPos(5,1,-27));
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
        for(int x=-9;x<=9;x++){at(l,b,x,0,-31,ClassicsRegistry.WAINSCOT.get());at(l,b,x,0,-8,ClassicsRegistry.WAINSCOT.get());}
        for(int z=-30;z<=-9;z++)for(int x:new int[]{-10,10}){at(l,b,x,0,z,ClassicsRegistry.WAINSCOT.get());at(l,b,x,5,z,Blocks.DARK_OAK_PLANKS);}
        for(int z:new int[]{-10,-16,-22,-28})box(l,b,-9,5,z,9,5,z,Blocks.DARK_OAK_LOG);
        frame(l,b,0,-8,Direction.SOUTH);
        for(int x=-9;x<=9;x++)for(int y=0;y<5;y++)at(l,b,x,y,-22,y==0?ClassicsRegistry.WAINSCOT.get():Blocks.SMOOTH_SANDSTONE);
        box(l,b,0,0,-30,0,5,-23,Blocks.DARK_OAK_PLANKS);
        frame(l,b,-5,-22,Direction.SOUTH);NovelRooms.door(l,b.offset(-5,0,-22),Direction.SOUTH,Blocks.DARK_OAK_DOOR,false);
        frame(l,b,5,-22,Direction.SOUTH);NovelRooms.door(l,b.offset(GATE),Direction.SOUTH,Blocks.IRON_DOOR,false);
        // A marquetry table sits on its own modeled legs. Candle blocks have actual support.
        for(int x=-1;x<=0;x++)for(int z=-16;z<=-15;z++)at(l,b,x,0,z,ClassicsRegistry.TABLE.get());
        furniture(l,b,-3,0,-15,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.EAST);
        furniture(l,b,2,0,-15,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.WEST);
        furniture(l,b,0,0,-13,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.NORTH);
        furniture(l,b,0,0,-18,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.SOUTH);
        for(int z=-19;z<=-17;z++)furniture(l,b,7,0,z,HouseholdFurnitureBlock.Kind.BOOKCASE,Direction.WEST);
        furniture(l,b,-7,0,-19,HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.EAST);
        for(BlockPos candle:CANDLES)l.setBlock(b.offset(candle),Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.LIT,true).setValue(CandleBlock.CANDLES,2),F);
        at(l,b,CUPBOARD.getX(),0,CUPBOARD.getZ(),Blocks.CHEST);
        lectern(l,b.offset(SITTING),ClassicsTexts.medium());lectern(l,b.offset(GRACE),ClassicsTexts.grace());lectern(l,b.offset(ALBUM),ClassicsTexts.album());
        furniture(l,b,-7,0,-27,HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.EAST);
        box(l,b,7,0,-29,7,2,-29,Blocks.BOOKSHELF);
        for(int z=-27;z<=-24;z++){at(l,b,10,2,z,Blocks.GLASS);at(l,b,11,2,z,Blocks.BLACK_CONCRETE);}
        for(BlockPos shutter:SHUTTERS)l.setBlock(b.offset(shutter),Blocks.DARK_OAK_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.FACING,Direction.WEST).setValue(TrapDoorBlock.HALF,Half.TOP).setValue(TrapDoorBlock.OPEN,true),F);
        // Both lamps are suspended from the authored ceiling, never from empty air.
        for(int z:new int[]{-6,-25}){at(l,b,0,z==-6?3:4,z,Blocks.CHAIN);l.setBlock(b.offset(0,z==-6?2:3,z),Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true),F);}
        // A fireplace with a closed iron hearth, mantel, stone jambs and soot above it.
        box(l,b,-9,0,-12,-7,0,-10,Blocks.POLISHED_ANDESITE);
        box(l,b,-9,1,-12,-9,3,-12,Blocks.STONE_BRICKS);box(l,b,-7,1,-12,-7,3,-12,Blocks.STONE_BRICKS);
        box(l,b,-9,3,-12,-7,3,-12,Blocks.DARK_OAK_SLAB);at(l,b,-8,1,-12,Blocks.IRON_BARS);at(l,b,-8,2,-12,Blocks.COAL_BLOCK);
        LabyrinthBuilder.entrance(l,b,ClassicsRegistry.SEANCE_WALL.get().defaultBlockState(),Blocks.DARK_OAK_PLANKS.defaultBlockState(),Blocks.BIRCH_PLANKS.defaultBlockState());LabyrinthBuilder.doors(l,b,LabyrinthPlace.SEANCE);
    }
    private static void nursery(ServerLevel l,BlockPos b){
        LabyrinthBuilder.room(l,b,-8,8,6,-27,-7,ClassicsRegistry.WALLPAPER.get().defaultBlockState(),Blocks.BIRCH_PLANKS.defaultBlockState(),Blocks.SMOOTH_SANDSTONE.defaultBlockState());
        LabyrinthBuilder.room(l,b,-3,3,4,-7,-1,Blocks.WHITE_TERRACOTTA.defaultBlockState(),Blocks.BIRCH_PLANKS.defaultBlockState(),Blocks.BIRCH_PLANKS.defaultBlockState());
        frame(l,b,0,-7,Direction.SOUTH);
        for(int z=-26;z<=-8;z++)for(int x:new int[]{-8,8}){at(l,b,x,0,z,Blocks.STRIPPED_BIRCH_WOOD);at(l,b,x,5,z,Blocks.BIRCH_PLANKS);}
        for(int x=-7;x<=7;x++){at(l,b,x,0,-27,Blocks.STRIPPED_BIRCH_WOOD);at(l,b,x,5,-27,Blocks.BIRCH_PLANKS);}
        for(int z:new int[]{-10,-18,-25})box(l,b,-7,5,z,7,5,z,Blocks.STRIPPED_OAK_LOG);
        // The blocked windows have glass, deep reveals, iron bars and a real bright backing.
        for(int z:new int[]{-12,-20})for(int y=1;y<=3;y++){
            at(l,b,-8,y,z,Blocks.GLASS);at(l,b,-9,y,z,Blocks.WHITE_CONCRETE);at(l,b,-7,y,z,Blocks.IRON_BARS);
            at(l,b,8,y,z,Blocks.GLASS);at(l,b,9,y,z,Blocks.WHITE_CONCRETE);at(l,b,7,y,z,Blocks.IRON_BARS);
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
        for(int z:new int[]{-5,-18}){at(l,b,0,z==-5?3:4,z,Blocks.CHAIN);l.setBlock(b.offset(0,z==-5?2:3,z),Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true),F);}
        LabyrinthBuilder.entrance(l,b,Blocks.WHITE_TERRACOTTA.defaultBlockState(),Blocks.BIRCH_PLANKS.defaultBlockState(),Blocks.BIRCH_PLANKS.defaultBlockState());LabyrinthBuilder.doors(l,b,LabyrinthPlace.WALLPAPER_NURSERY);
    }
}
