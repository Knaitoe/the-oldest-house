package io.github.knaitoe.theoldesthouse.labyrinth;
import java.util.*;
import io.github.knaitoe.theoldesthouse.house.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
/** An ordinary contemporary restroom and usable appliances; recognized saved cells change once. */
@net.neoforged.fml.common.EventBusSubscriber(modid=io.github.knaitoe.theoldesthouse.TheOldestHouse.MOD_ID)
public final class DinerDetails {
 public static final String STATE="diner_details133";
 public static final BlockPos BATH_DOOR=new BlockPos(10,0,-23),ROACH=new BlockPos(5,0,-28);
 private record Work(ServerLevel level,BlockPos base,BuildBlocks.Plan before,BuildBlocks.Plan after){}private static Work work;
 private DinerDetails(){}
 private static void put(ServerLevel l,BlockPos b,int x,int y,int z,ServiceFittingBlock.Kind k,Direction d){BuildBlocks.set(l,b.offset(x,y,z),ServiceFittingBlock.of(k,d),UncannyRooms.FLAGS);}
 public static void build(ServerLevel l,BlockPos b){
  NovelRooms.room(l,b,10,15,-28,-21,0,4,UncannyRegistry.material("bathroom_wall"),UncannyRegistry.material("bathroom_floor"));
  BuildBlocks.set(l,b.offset(BATH_DOOR),Blocks.AIR.defaultBlockState(),UncannyRooms.FLAGS);BuildBlocks.set(l,b.offset(BATH_DOOR).above(),Blocks.AIR.defaultBlockState(),UncannyRooms.FLAGS);NovelRooms.door(l,b.offset(BATH_DOOR),Direction.WEST,UncannyRegistry.DINER_DOOR.get(),false);
  put(l,b,13,0,-27,ServiceFittingBlock.Kind.TOILET,Direction.SOUTH);put(l,b,14,0,-24,ServiceFittingBlock.Kind.BASIN,Direction.WEST);put(l,b,14,2,-24,ServiceFittingBlock.Kind.MIRROR,Direction.WEST);put(l,b,14,1,-25,ServiceFittingBlock.Kind.SOAP,Direction.WEST);put(l,b,11,1,-25,ServiceFittingBlock.Kind.DRYER,Direction.EAST);
  BuildBlocks.set(l,b.offset(11,0,-27),UncannyFurnitureBlock.of(UncannyFurnitureBlock.Kind.BIN,Direction.EAST),UncannyRooms.FLAGS);BuildBlocks.set(l,b.offset(13,3,-24),UncannyFurnitureBlock.of(UncannyFurnitureBlock.Kind.FLUORESCENT,Direction.NORTH),UncannyRooms.FLAGS);
  put(l,b,4,1,-31,ServiceFittingBlock.Kind.MICROWAVE,Direction.SOUTH);put(l,b,6,1,-31,ServiceFittingBlock.Kind.TOASTER,Direction.SOUTH);put(l,b,8,1,-31,ServiceFittingBlock.Kind.KETTLE,Direction.SOUTH);put(l,b,5,0,-28,ServiceFittingBlock.Kind.ROACH,Direction.NORTH);
 }
 /** Never run construction callbacks, rebuild used containers, or overwrite player edits. */
 public static boolean repair(ServerLevel l,BlockPos b){
  var data=LabyrinthData.get(l.getServer());var saved=data.state(STATE);String key=Long.toString(b.asLong());if(saved.getBoolean(key))return true;
  var area=new net.minecraft.world.phys.AABB(net.minecraft.world.phys.Vec3.atLowerCornerOf(b.offset(-11,-1,-32)),net.minecraft.world.phys.Vec3.atLowerCornerOf(b.offset(16,5,-4)));
  for(int x=(int)Math.floor(area.minX)>>4;x<=((int)Math.ceil(area.maxX)-1)>>4;x++)for(int z=(int)Math.floor(area.minZ)>>4;z<=((int)Math.ceil(area.maxZ)-1)>>4;z++)if(!l.hasChunk(x,z)||!l.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(x,z)))return false;
  if(l.players().stream().anyMatch(p->area.inflate(24).intersects(p.getCamera().getBoundingBox())))return false;
  if(work==null||work.level()!=l||!work.base().equals(b))work=new Work(l,b.immutable(),BuildBlocks.record(l,()->UncannyRooms0132.build(l,b,LabyrinthPlace.SECOND_WAITRESS)),BuildBlocks.record(l,()->UncannyRooms.build(l,b,LabyrinthPlace.SECOND_WAITRESS)));
  var cells=new ArrayList<BlockPos>();for(int y=-1;y<=4;y++)for(int z=-28;z<=-21;z++)for(int x=10;x<=15;x++)cells.add(b.offset(x,y,z));
  for(var rel:List.of(new BlockPos(4,1,-31),new BlockPos(6,1,-31),new BlockPos(8,1,-31),ROACH,UncannyRooms.account(LabyrinthPlace.SECOND_WAITRESS)))cells.add(b.offset(rel));
  int cursor=saved.getInt(key+":cursor"),visited=0;long until=System.nanoTime()+3_000_000;
  while(cursor<cells.size()&&visited++<256&&(visited==1||System.nanoTime()<until)){
   var at=cells.get(cursor);var before=work.before().plannedState(at);var next=work.after().plannedState(at);var actual=l.getBlockState(at);
   if(next!=null&&!actual.equals(next)&&before!=null&&actual.equals(before)){
    var be=l.getBlockEntity(at);boolean redundantPaper=at.equals(b.offset(UncannyRooms.account(LabyrinthPlace.SECOND_WAITRESS)))&&be instanceof UncannyPropBlock.Fitting f&&f.meal.isEmpty();
    if(be==null||redundantPaper){if(!l.getEntities((net.minecraft.world.entity.Entity)null,new net.minecraft.world.phys.AABB(at).inflate(.05),net.minecraft.world.entity.Entity::isAlive).isEmpty())break;l.setBlock(at,next,UncannyRooms.FLAGS);}
   }cursor++;
  }
  saved.putInt(key+":cursor",cursor);if(cursor>=cells.size()){saved.putBoolean(key,true);LabyrinthBuilder.registerDoors(data,LabyrinthPlace.SECOND_WAITRESS,b);work=null;}data.setState(STATE,saved);return cursor>=cells.size();
 }
 @net.neoforged.bus.api.SubscribeEvent public static void tick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post e){var s=e.getServer();if(s instanceof net.minecraft.gametest.framework.GameTestServer||LabyrinthBuilder.isCarving()||s.getTickCount()%5!=2)return;var origin=HouseSavedData.get(s).houseOrigin();var l=s.getLevel(HouseDimensions.OUTSIDE);if(origin!=null&&l!=null&&LabyrinthBuilder.isPlaceReady(s,LabyrinthPlace.SECOND_WAITRESS))for(var b:SceneInstances.bases(s,LabyrinthPlace.SECOND_WAITRESS))repair(l,b);}
}
