package io.github.knaitoe.theoldesthouse.labyrinth;
import java.util.*;
import io.github.knaitoe.theoldesthouse.house.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
/** An ordinary contemporary restroom and usable appliances. */
public final class DinerDetails {
 /** 0.4.201 (owner: "no see-through door in a diner... no 'bathroom' texture that bleeds into the diner... appropriately
  *  grungy"). The restroom no longer borrows the diner's east wall: that wall keeps its own plaster, dado and windows, and a
  *  solid steel door in it opens into an annex whose outside matches the diner and whose inside is lined with grimy tile. */
 public static final BlockPos BATH_DOOR=new BlockPos(10,0,-24),ROACH=new BlockPos(5,0,-28);
 public static final BlockPos TOILET=new BlockPos(13,0,-28),BASIN=new BlockPos(14,0,-26),SOAP=new BlockPos(14,1,-27),DRYER=new BlockPos(12,1,-26);
 private DinerDetails(){}
 private static void put(ServerLevel l,BlockPos b,int x,int y,int z,ServiceFittingBlock.Kind k,Direction d){BuildBlocks.set(l,b.offset(x,y,z),ServiceFittingBlock.of(k,d),UncannyRooms.FLAGS);}
 private static void put(ServerLevel l,BlockPos b,BlockPos at,ServiceFittingBlock.Kind k,Direction d){put(l,b,at.getX(),at.getY(),at.getZ(),k,d);}
 private static void box(ServerLevel l,BlockPos b,int x,int y,int z,int X,int Y,int Z,net.minecraft.world.level.block.state.BlockState s){NovelRooms.box(l,b,x,y,z,X,Y,Z,s);}
 public static void build(ServerLevel l,BlockPos b){
  var tile=UncannyRegistry.material("bathroom_wall").defaultBlockState();var floor=UncannyRegistry.material("bathroom_floor").defaultBlockState();
  var plaster=UncannyRegistry.material("diner_wall").defaultBlockState();var dado=UncannyRegistry.material("diner_dado").defaultBlockState();
  // The outer shell, x 10 (the diner's own wall) to 16 and z -22 to -30, in the diner's exterior finish, under the diner's roof.
  box(l,b,11,-1,-29,15,-1,-23,floor);
  for(int y=0;y<=4;y++){var s=y<=1?dado:plaster;box(l,b,11,y,-22,16,y,-22,s);box(l,b,11,y,-30,16,y,-30,s);box(l,b,16,y,-29,16,y,-23,s);}
  box(l,b,10,5,-31,17,5,-21,UncannyRegistry.material("roof").defaultBlockState());
  // The tiled lining one block in, and a stained plaster ceiling over a room three by five.
  box(l,b,11,0,-29,15,3,-29,tile);box(l,b,11,0,-23,15,3,-23,tile);box(l,b,11,0,-28,11,3,-24,tile);box(l,b,15,0,-28,15,3,-24,tile);
  box(l,b,11,4,-29,15,4,-23,plaster);box(l,b,12,0,-28,14,3,-24,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
  // The door is in the diner's wall; the tile lining behind it is cut away for a short doorway.
  box(l,b,11,0,BATH_DOOR.getZ(),11,1,BATH_DOOR.getZ(),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
  BuildBlocks.set(l,b.offset(BATH_DOOR),Blocks.AIR.defaultBlockState(),UncannyRooms.FLAGS);BuildBlocks.set(l,b.offset(BATH_DOOR).above(),Blocks.AIR.defaultBlockState(),UncannyRooms.FLAGS);NovelRooms.door(l,b.offset(BATH_DOOR),Direction.WEST,UncannyRegistry.RESTROOM_DOOR.get(),false);
  put(l,b,TOILET,ServiceFittingBlock.Kind.TOILET,Direction.SOUTH);put(l,b,BASIN,ServiceFittingBlock.Kind.BASIN,Direction.WEST);put(l,b,BASIN.above(2),ServiceFittingBlock.Kind.MIRROR,Direction.WEST);put(l,b,SOAP,ServiceFittingBlock.Kind.SOAP,Direction.WEST);put(l,b,DRYER,ServiceFittingBlock.Kind.DRYER,Direction.EAST);
  BuildBlocks.set(l,b.offset(12,0,-28),UncannyFurnitureBlock.of(UncannyFurnitureBlock.Kind.BIN,Direction.EAST),UncannyRooms.FLAGS);BuildBlocks.set(l,b.offset(13,3,-26),UncannyFurnitureBlock.of(UncannyFurnitureBlock.Kind.FLUORESCENT,Direction.NORTH),UncannyRooms.FLAGS);
  put(l,b,4,1,-31,ServiceFittingBlock.Kind.MICROWAVE,Direction.SOUTH);put(l,b,6,1,-31,ServiceFittingBlock.Kind.TOASTER,Direction.SOUTH);put(l,b,8,1,-31,ServiceFittingBlock.Kind.KETTLE,Direction.SOUTH);put(l,b,5,0,-28,ServiceFittingBlock.Kind.ROACH,Direction.NORTH);
 }
}
