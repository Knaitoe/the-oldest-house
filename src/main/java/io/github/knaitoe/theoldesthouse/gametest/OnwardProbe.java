package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.*;

/** LOCAL ONLY, never committed: builds each vignette and lists wall cells a forward door could use. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_onward_probe") @PrefixGameTestTemplate(false)
public final class OnwardProbe {
    private static final Set<LabyrinthPlace> SKIP=EnumSet.of(LabyrinthPlace.BACKSEAT,LabyrinthPlace.SILO,LabyrinthPlace.BORRASCA,LabyrinthPlace.FAMILY_COPY,LabyrinthPlace.OLD_CABIN,LabyrinthPlace.RED_ROOM);
    private static boolean wall(ServerLevel l,BlockPos p){var s=l.getBlockState(p);return !s.isAir()&&!s.hasBlockEntity()&&s.getFluidState().isEmpty()&&s.isCollisionShapeFullBlock(l,p)&&!(s.getBlock() instanceof LeavesBlock)&&!(s.getBlock() instanceof TransparentBlock)&&!(s.getBlock() instanceof StainedGlassBlock);}
    private static boolean open(ServerLevel l,BlockPos p){var s=l.getBlockState(p);return s.getCollisionShape(l,p).isEmpty()&&s.getFluidState().isEmpty();}
    private static String id(BlockState s){return BuiltInRegistries.BLOCK.getKey(s.getBlock()).getPath();}
    static List<String> dump(ServerLevel l,BlockPos b,LabyrinthPlace place){
        var room=place.room();var lines=new ArrayList<String>();
                      var cands=new ArrayList<long[]>();
                      for(int x=room.minX();x<=room.maxX();x++)for(int z=room.minZ();z<=room.maxZ();z++)for(int y=room.minY();y<=room.maxY()-1;y++){
                          var at=b.offset(x,y,z);if(!wall(l,at)||!wall(l,at.above()))continue;
                          for(var d:Direction.Plane.HORIZONTAL){var front=at.relative(d);if(!open(l,front)||!open(l,front.above())||!wall(l,front.below()))continue;
                              var front2=front.relative(d);if(!open(l,front2)||!open(l,front2.above()))continue;
                              int behind=wall(l,at.relative(d.getOpposite()))?1:0;
                              String mat=id(l.getBlockState(at));boolean natural=mat.endsWith("_log")||mat.endsWith("_wood")||Set.of("dirt","coarse_dirt","podzol","grass_block","stone","andesite","diorite","granite","deepslate","tuff","sand","gravel","clay","mud","snow_block","ice","packed_ice","rooted_dirt","moss_block","black_concrete").contains(mat);
                              if(!natural)behind+=5;
                              double dist=Math.sqrt(x*x+(z-1)*(z-1)+y*y);
                              cands.add(new long[]{x,y,z,d.get2DDataValue(),(long)(dist*10),behind});}
                      }
                      cands.sort((a1,a2)->Long.compare(a2[4]+a2[5]*60,a1[4]+a1[5]*60));
                      var chosen=new ArrayList<long[]>();for(var c:cands){boolean near=false;for(var o:chosen)if(Math.abs(o[0]-c[0])+Math.abs(o[1]-c[1])+Math.abs(o[2]-c[2])<6)near=true;if(!near)chosen.add(c);if(chosen.size()>=14)break;}
                      lines.add(place.name()+" dim="+l.dimension().location().getPath()+" room="+room+" candidates="+cands.size());
                      for(var c:chosen){var at=b.offset((int)c[0],(int)c[1],(int)c[2]);var d=Direction.from2DDataValue((int)c[3]);
                          var around=new TreeMap<String,Integer>();for(var q:BlockPos.betweenClosed(at.offset(-3,-1,-3),at.offset(3,3,3))){var s=l.getBlockState(q);if(!s.isAir())around.merge(id(s),1,Integer::sum);}
                          var top=around.entrySet().stream().sorted((e1,e2)->e2.getValue()-e1.getValue()).limit(7).map(e->e.getKey()+"x"+e.getValue()).toList();
                          lines.add(String.format("  rel=(%d,%d,%d) face=%s dist=%.1f exterior=%d wall=%s/%s floor=%s near=%s",c[0],c[1],c[2],d.getSerializedName(),c[4]/10.0,c[5],id(l.getBlockState(at)),id(l.getBlockState(at.above())),id(l.getBlockState(at.relative(d).below())),top));}
                      // Top-down maps of every storey that has floor: row = z (north at top), column = x.
                      for(int y=room.minY();y<=room.maxY()-1;y++){var rows=new ArrayList<String>();int floor=0;
                          for(int z=room.minZ();z<=room.maxZ()+1;z++){var row=new StringBuilder(String.format("%4d ",z));
                              for(int x=room.minX();x<=room.maxX();x++){var at=b.offset(x,y,z);var s=l.getBlockState(at);char ch;
                                  if(s.getBlock() instanceof DoorBlock)ch='D';else if(!s.getFluidState().isEmpty())ch='~';
                                  else if(open(l,at)&&open(l,at.above())&&wall(l,at.below())){ch='.';floor++;}
                                  else if(s.getBlock() instanceof LeavesBlock)ch='t';else if(wall(l,at))ch='#';else if(s.isAir())ch=' ';else ch='+';
                                  for(int i=0;i<chosen.size();i++){var c=chosen.get(i);if(c[0]==x&&c[1]==y&&c[2]==z)ch=(char)(i<10?'0'+i:'a'+i-10);}
                                  if(x==0&&z==1&&y==0)ch='E';row.append(ch);}
                              rows.add(row.toString());}
                          if(floor>12){lines.add("");lines.add("y="+y+"   x from "+room.minX()+" to "+room.maxX());lines.addAll(rows);}}
        return lines;
    }
    @GameTest(template="empty",batch="onward_final",timeoutTicks=200000)
    public static void finalBuild(GameTestHelper h)throws Exception{
        var server=h.getLevel().getServer();var oldHouse=io.github.knaitoe.theoldesthouse.house.HouseSavedData.get(server);var oldData=LabyrinthData.get(server);
        BlockPos origin=new BlockPos(84000,80,84000);var house=new io.github.knaitoe.theoldesthouse.house.HouseSavedData();house.markSpawned(origin);
        server.overworld().getDataStorage().set("the_oldest_house",house);var data=new LabyrinthData();server.overworld().getDataStorage().set("the_oldest_house_labyrinth",data);
        server.overworld().getDataStorage().set("the_oldest_house_mother",new MotherCollection());
        LabyrinthBuilder.clearAll();LabyrinthBuilder.rebuild(server);LabyrinthBuilder.finishGameTest(server);
        var out=Paths.get(System.getProperty("user.dir"),"..","build","onward-final");Files.createDirectories(out);
        for(String id:System.getProperty("onward.places","whale").split(",")){var place=LabyrinthPlace.byId(id);
            var level=HouseTestLevel.get(server,NovelRooms.dimension(place));Files.write(out.resolve(id+".txt"),dump(level,LabyrinthPlaces.base(origin,place),place));}
        h.succeed();
    }
    @GameTest(template="empty",batch="onward_probe",timeoutTicks=200000)
    public static void probe(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);var places=new ArrayList<LabyrinthPlace>();
        for(var p:LabyrinthPlace.values())if(p.isVignette()&&p.slot()>=0&&!SKIP.contains(p)&&HouseReadSites.kind(p)==null)places.add(p);
        var out=Paths.get(System.getProperty("user.dir"),"..","build","onward-probe");
        var seq=h.startSequence();
        for(var place:places){
            seq=seq.thenExecute(()->{var l=f.level(place);var slot=LabyrinthPlaces.slotBounds(f.origin,place);f.chunks.hold(l,new AABB(slot.minX(),slot.minY(),slot.minZ(),slot.maxX()+1,slot.maxY()+1,slot.maxZ()+1));})
              .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"resident "+place))
              .thenExecute(()->{
                  try{
                      var m=LabyrinthBuilder.class.getDeclaredMethod("build",net.minecraft.server.MinecraftServer.class,ServerLevel.class,BlockPos.class,LabyrinthPlace.class);m.setAccessible(true);m.invoke(null,f.in.getServer(),f.in,f.origin,place);
                      var l=f.level(place);var b=f.base(place);var lines=dump(l,b,place);
                      Files.createDirectories(out);Files.write(out.resolve(place.id()+".txt"),lines);
                  }catch(Exception ex){try{Files.createDirectories(out);Files.writeString(out.resolve(place.id()+".error.txt"),ex.toString()+"\n"+(ex.getCause()==null?"":ex.getCause().toString()));}catch(Exception ignored){}}
                  f.chunks.close();
              });
        }
        seq.thenSucceed();
    }
}
