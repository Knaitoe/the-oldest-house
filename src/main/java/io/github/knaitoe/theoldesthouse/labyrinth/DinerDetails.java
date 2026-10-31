package io.github.knaitoe.theoldesthouse.labyrinth;
import java.util.*;
import io.github.knaitoe.theoldesthouse.house.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
/** An ordinary contemporary restroom and usable appliances. */
public final class DinerDetails {
 public static final BlockPos BATH_DOOR=new BlockPos(10,0,-23),ROACH=new BlockPos(5,0,-28);
 private DinerDetails(){}
 private static void put(ServerLevel l,BlockPos b,int x,int y,int z,ServiceFittingBlock.Kind k,Direction d){BuildBlocks.set(l,b.offset(x,y,z),ServiceFittingBlock.of(k,d),UncannyRooms.FLAGS);}
 public static void build(ServerLevel l,BlockPos b){
  NovelRooms.room(l,b,10,15,-28,-21,0,4,UncannyRegistry.material("bathroom_wall"),UncannyRegistry.material("bathroom_floor"));
  BuildBlocks.set(l,b.offset(BATH_DOOR),Blocks.AIR.defaultBlockState(),UncannyRooms.FLAGS);BuildBlocks.set(l,b.offset(BATH_DOOR).above(),Blocks.AIR.defaultBlockState(),UncannyRooms.FLAGS);NovelRooms.door(l,b.offset(BATH_DOOR),Direction.WEST,UncannyRegistry.DINER_DOOR.get(),false);
  put(l,b,13,0,-27,ServiceFittingBlock.Kind.TOILET,Direction.SOUTH);put(l,b,14,0,-24,ServiceFittingBlock.Kind.BASIN,Direction.WEST);put(l,b,14,2,-24,ServiceFittingBlock.Kind.MIRROR,Direction.WEST);put(l,b,14,1,-25,ServiceFittingBlock.Kind.SOAP,Direction.WEST);put(l,b,11,1,-25,ServiceFittingBlock.Kind.DRYER,Direction.EAST);
  BuildBlocks.set(l,b.offset(11,0,-27),UncannyFurnitureBlock.of(UncannyFurnitureBlock.Kind.BIN,Direction.EAST),UncannyRooms.FLAGS);BuildBlocks.set(l,b.offset(13,3,-24),UncannyFurnitureBlock.of(UncannyFurnitureBlock.Kind.FLUORESCENT,Direction.NORTH),UncannyRooms.FLAGS);
  put(l,b,4,1,-31,ServiceFittingBlock.Kind.MICROWAVE,Direction.SOUTH);put(l,b,6,1,-31,ServiceFittingBlock.Kind.TOASTER,Direction.SOUTH);put(l,b,8,1,-31,ServiceFittingBlock.Kind.KETTLE,Direction.SOUTH);put(l,b,5,0,-28,ServiceFittingBlock.Kind.ROACH,Direction.NORTH);
 }
}
