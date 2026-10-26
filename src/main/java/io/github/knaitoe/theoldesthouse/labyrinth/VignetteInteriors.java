package io.github.knaitoe.theoldesthouse.labyrinth;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import static io.github.knaitoe.theoldesthouse.labyrinth.VignetteInteriorBlock.Kind.*;

/** Explicit authored refits of older rooms, including bounded, resident-only saved-world migration. */
public final class VignetteInteriors {
 public static final String STATE="vignette_interiors_0125b";
 public static final List<LabyrinthPlace> PLACES=List.of(LabyrinthPlace.FLOORBOARDS,LabyrinthPlace.HIDE_AND_CLAP,LabyrinthPlace.SEANCE,LabyrinthPlace.WALLPAPER_NURSERY,
  LabyrinthPlace.HILL_NURSERY,LabyrinthPlace.MINIATURES,LabyrinthPlace.USHER,LabyrinthPlace.CRIMSON_HALL,LabyrinthPlace.MAPPING_INTERIOR,LabyrinthPlace.CONFESSION,
  LabyrinthPlace.DEVILS_ROCK,LabyrinthPlace.END_WORLD_CABIN,LabyrinthPlace.KAREN_ROOM,LabyrinthPlace.HOLLOWAY_CAMP);
 private static final int FLAGS=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
 public record Edit(BlockPos relative,BlockState expected,BlockState next){}
 private static Work work;
 private static final class Work {
  final ServerLevel level;final BlockPos origin,base;final LabyrinthPlace place;final List<Edit> edits;int cursor;
  Work(ServerLevel l,BlockPos o,LabyrinthPlace p){level=l;origin=o.immutable();place=p;base=LabyrinthPlaces.base(o,p);edits=plan(p);}
 }
 private VignetteInteriors(){}
 private static String key(BlockPos o,LabyrinthPlace p){return o.asLong()+":"+p.id();}
 public static boolean applies(LabyrinthPlace p){return PLACES.contains(p);}
 private static void fill(List<Edit> e,int x0,int y0,int z0,int x1,int y1,int z1,Block old,VignetteInteriorBlock.Kind next){
  for(int y=y0;y<=y1;y++)for(int z=z0;z<=z1;z++)for(int x=x0;x<=x1;x++)e.add(new Edit(new BlockPos(x,y,z),old.defaultBlockState(),VignetteInteriorBlock.of(next,Direction.NORTH)));
 }
 private static void shell(List<Edit> e,int x0,int x1,int z0,int z1,int y0,int y1,Block old,VignetteInteriorBlock.Kind next){
  fill(e,x0,y0,z0,x1,y1,z0,old,next);fill(e,x0,y0,z1,x1,y1,z1,old,next);fill(e,x0,y0,z0+1,x0,y1,z1-1,old,next);fill(e,x1,y0,z0+1,x1,y1,z1-1,old,next);
 }
 private static void fit(List<Edit> e,int x,int y,int z,VignetteInteriorBlock.Kind k,Direction f){e.add(new Edit(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState(),VignetteInteriorBlock.of(k,f)));}
 private static void fit(List<Edit> e,int x,int y,int z,VignetteInteriorBlock.Kind k){fit(e,x,y,z,k,Direction.SOUTH);}
 /** Human-scaled partitions, with deliberate full-height openings; never sealed fake doors. */
 private static void partition(List<Edit> e,int x,int z,VignetteInteriorBlock.Kind dado,VignetteInteriorBlock.Kind paper){
  fill(e,x,0,z,x,0,z,Blocks.AIR,dado);fill(e,x,1,z,x,3,z,Blocks.AIR,paper);
 }
 /** No note spawns or invented lore. Story actors, finite originals and routes retain their existing anchors. */
 public static List<Edit> plan(LabyrinthPlace p){var e=new ArrayList<Edit>();switch(p){
  case FLOORBOARDS->{
   shell(e,-6,6,-10,0,1,3,Blocks.BROWN_TERRACOTTA,SICKROOM_PLASTER);fill(e,-5,-1,-9,5,-1,-1,Blocks.SPRUCE_PLANKS,SICKROOM_BOARDS);
   fit(e,-5,0,-2,MEDICINE_PRESS,Direction.EAST);fit(e,-5,0,-3,SICKROOM_WASHSTAND,Direction.EAST);
  }
  case HIDE_AND_CLAP->{
   shell(e,-6,6,-13,0,1,3,Blocks.LIGHT_BLUE_TERRACOTTA,CLAP_STARS);fill(e,-5,-1,-12,5,-1,-1,Blocks.BIRCH_PLANKS,CLAP_BOARDS);
   fit(e,5,0,-12,TOY_CUPBOARD,Direction.WEST);fit(e,5,1,-12,TOY_PIANO,Direction.WEST);
  }
  case SEANCE->{fit(e,8,0,-12,SEANCE_SIDEBOARD);fit(e,8,1,-12,TEA_SERVICE);fit(e,7,0,-12,SEANCE_SIDEBOARD);fit(e,7,1,-12,PARLOUR_MUSIC_BOX);fit(e,8,0,-29,LINEN_PRESS);fit(e,8,1,-29,SEWING_BASKET);}
  case WALLPAPER_NURSERY->{
   // The original yellow paper, peel seams and all their textures are the story.
   fit(e,6,0,-25,LINEN_PRESS,Direction.WEST);fit(e,6,1,-25,SEWING_BASKET,Direction.WEST);fit(e,-7,0,-16,ROCKING_HORSE,Direction.EAST);
  }
  case HILL_NURSERY->{
   shell(e,-3,3,-43,0,1,4,Blocks.CALCITE,HILL_ROSES);
   for(int side:new int[]{-1,1}){int x0=side<0?-14:4,x1=side<0?-4:14;
    shell(e,x0,x1,-24,-10,1,5,Blocks.CALCITE,HILL_ROSES);shell(e,x0,x1,-24,-10,0,0,Blocks.DARK_OAK_PLANKS,HILL_DADO);
    fill(e,x0+1,-1,-23,x1-1,-1,-11,Blocks.DARK_OAK_PLANKS,HILL_FLOOR);
    fit(e,side*12,0,-18,LINEN_PRESS,side<0?Direction.EAST:Direction.WEST);fit(e,side*12,1,-18,HILL_LAMP);
    fit(e,side*13,1,-15,HILL_HEIGHT_MARKS,side<0?Direction.EAST:Direction.WEST);
   }fit(e,11,0,-23,ROCKING_HORSE,Direction.WEST);fit(e,-12,0,-23,SEWING_BASKET,Direction.EAST);
  }
  case MINIATURES->{
   shell(e,-14,14,-30,0,1,6,Blocks.CALCITE,WORKSHOP_PLASTER);
   for(int z:new int[]{-9,-10,-11})fit(e,-10,0,z,CRAFT_BENCH);
   fit(e,-10,1,-9,DRAFTING_BOARD);fit(e,-10,1,-10,MODEL_TOOLS);fit(e,-10,1,-11,TASK_LAMP);fit(e,-11,0,-11,MODEL_DRAWERS,Direction.EAST);
   fit(e,10,0,-23,CRAFT_BENCH);fit(e,10,1,-23,MODEL_TOOLS);fit(e,11,0,-23,MODEL_DRAWERS,Direction.WEST);
   for(int x:new int[]{-8,-7,-6})for(int z:new int[]{-7,-8})fit(e,x,0,z,CRAFT_BENCH);
   fit(e,-8,1,-7,DRAFTING_BOARD);fit(e,-7,1,-7,MODEL_TOOLS);fit(e,-6,1,-8,MODEL_DRAWERS);fit(e,-8,1,-8,TASK_LAMP);
   for(int x:new int[]{5,6})fit(e,x,0,-8,CRAFT_BENCH);fit(e,5,1,-8,DRAFTING_BOARD);fit(e,6,1,-8,MODEL_TOOLS);
   fit(e,-13,4,-11,MODEL_PLAN,Direction.EAST);fit(e,-13,4,-10,MODEL_PLAN,Direction.EAST);fit(e,13,3,-23,MODEL_PLAN,Direction.WEST);
  }
  case USHER->{
   shell(e,-17,17,-41,0,1,7,Blocks.DEEPSLATE_BRICKS,USHER_LINEN);fill(e,-16,-1,-40,16,-1,-1,Blocks.POLISHED_DEEPSLATE,USHER_BOARDS);
   shell(e,-8,8,-35,-20,-3,-1,Blocks.DEEPSLATE_BRICKS,VAULT_STONE);
   fit(e,11,0,-14,USHER_SPINET);fit(e,14,0,-14,USHER_PRESS,Direction.WEST);fit(e,-12,0,-13,DICTATION_DESK);fit(e,-12,1,-13,INK_SET);
   fit(e,16,2,-14,USHER_PORTRAIT,Direction.WEST);
   // Enclosed study bays flank the actual central vault stair. Both side passages remain open.
   for(int x:new int[]{-6,6})for(int z=-17;z<=-8;z++)if(z!=-13&&z!=-12)partition(e,x,z,USHER_BOARDS,USHER_LINEN);
   for(int side:new int[]{-1,1})for(int x=6;x<=16;x++)if(x!=10&&x!=11&&x!=14)partition(e,side*x,-8,USHER_BOARDS,USHER_LINEN);

  }
  case CRIMSON_HALL->{
   shell(e,-18,18,-43,0,1,5,Blocks.POLISHED_BLACKSTONE_BRICKS,CRIMSON_SILK);
   for(int x:new int[]{-11,11}){fit(e,x,0,-32,CYLINDER_CABINET);fit(e,x,1,-32,CYLINDER_HORN);fit(e,x,0,-29,FOLDING_MUSIC_STAND);fit(e,x<0?-17:17,2,-8,CRIMSON_COAT_HOOK,x<0?Direction.EAST:Direction.WEST);}
   fill(e,-17,-1,-42,-15,-1,-1,LiteraryRegistry.RED_FLOOR.get(),CRIMSON_WALNUT);fill(e,15,-1,-42,17,-1,-1,LiteraryRegistry.RED_FLOOR.get(),CRIMSON_WALNUT);
  }
  case MAPPING_INTERIOR->{
   shell(e,-13,13,-36,-8,1,4,LiteraryRegistry.SIDING.get(),TRAILER_PAPER);fill(e,-12,0,-24,12,4,-24,Blocks.CALCITE,TRAILER_PAPER);
   fill(e,-12,-1,-23,-6,-1,-9,Blocks.BIRCH_PLANKS,TRAILER_VINYL);
   fit(e,-10,0,-13,TRAILER_SINK,Direction.EAST);fit(e,-11,0,-13,TRAILER_CUPBOARD,Direction.EAST);fit(e,-11,0,-14,TRAILER_CUPBOARD,Direction.EAST);fit(e,-11,1,-14,HOLIDAY_CROCKERY,Direction.EAST);
  }
  case CONFESSION->{
   shell(e,-12,12,-33,0,1,7,LiteraryRegistry.DARK_PANEL.get(),CONFESSION_PAPER);shell(e,-12,12,-33,0,0,0,Blocks.DARK_OAK_PLANKS,CONFESSION_OAK);
   // An interview room inside the archive: central entrance and a two-block side opening to the blank-book shelves.
   for(int x:new int[]{-3,5})for(int z=-27;z<=-12;z++)if(!(x==-3&&(z==-24||z==-23)))partition(e,x,z,CONFESSION_OAK,CONFESSION_PAPER);
   for(int x=-3;x<=5;x++)for(int z:new int[]{-27,-12})if(!(z==-12&&x>=0&&x<=2))partition(e,x,z,CONFESSION_OAK,CONFESSION_PAPER);
   for(int z:new int[]{-12,-13})fit(e,-8,0,z,DICTATION_DESK);fit(e,-8,1,-12,INK_SET);fit(e,-8,1,-13,BEDSIDE_LAMP);fit(e,-9,0,-12,DICTATION_FILES,Direction.EAST);fit(e,8,0,-12,DICTATION_FILES,Direction.WEST);fit(e,8,1,-12,DESK_BELL);
  }
  case DEVILS_ROCK->{
   shell(e,-16,16,-42,0,1,6,Blocks.CALCITE,WATCHROOM_FLOWERS);shell(e,-14,-3,-38,-20,1,3,Blocks.CALCITE,WATCHROOM_FLOWERS);fill(e,-15,-1,-41,15,-1,-1,Blocks.OAK_PLANKS,WATCHROOM_BOARDS);
   // A real domestic kitchen beside the arrival, away from the camera, string and plate line at -18.
   for(int z=-13;z<=-3;z++)if(z!=-9&&z!=-8)partition(e,-4,z,WATCHROOM_BOARDS,WATCHROOM_FLOWERS);
   for(int x=-14;x<=-4;x++)partition(e,x,-13,WATCHROOM_BOARDS,WATCHROOM_FLOWERS);
   for(int z:new int[]{-6,-7,-8})fit(e,-14,0,z,WATCHROOM_CUPBOARD,Direction.EAST);
   fit(e,-14,0,-9,TRAILER_SINK,Direction.EAST);fit(e,-14,1,-6,HOLIDAY_CROCKERY);
   for(int x:new int[]{-10,-9})fit(e,x,0,-8,KITCHEN_WORKTABLE);fit(e,-10,1,-8,HOLIDAY_CROCKERY);
   fit(e,11,0,-27,CRAFT_BENCH);fit(e,11,1,-27,WATCHROOM_TRIPOD_CASE);fit(e,12,0,-27,WATCHROOM_DRAWER,Direction.WEST);fit(e,12,1,-27,WATCHROOM_TAPE);
   fit(e,-12,0,-35,WATCHROOM_DRAWER,Direction.EAST);fit(e,-12,1,-35,BEDSIDE_LAMP);
  }
  case END_WORLD_CABIN->{
   shell(e,-12,12,-35,-13,1,5,Blocks.SPRUCE_PLANKS,HOLIDAY_PINE);fill(e,-4,0,-34,-4,5,-25,Blocks.SPRUCE_PLANKS,HOLIDAY_PINE);fill(e,-11,0,-25,-4,5,-25,Blocks.SPRUCE_PLANKS,HOLIDAY_PINE);
   fill(e,-11,-1,-24,-5,-1,-14,Blocks.SPRUCE_PLANKS,HOLIDAY_TILE);fit(e,-9,0,-22,HOLIDAY_LARDER,Direction.EAST);fit(e,-9,1,-22,HOLIDAY_CROCKERY);fit(e,-9,0,-21,HOLIDAY_SINK,Direction.EAST);
  }
  case KAREN_ROOM->{
   shell(e,-9,9,-18,0,1,4,Blocks.BIRCH_PLANKS,KAREN_PAPER);fill(e,-8,-1,-17,8,-1,-1,Blocks.OAK_PLANKS,KAREN_CORK);
   fit(e,7,0,-10,SLIDE_CABINET,Direction.WEST);fit(e,7,1,-10,KAREN_SLIDE_SORTER,Direction.WEST);fit(e,7,0,-11,SLIDE_CABINET,Direction.WEST);fit(e,7,1,-11,KAREN_LAMP,Direction.WEST);fit(e,8,3,-5,KAREN_NOTICEBOARD,Direction.WEST);
  }
  case HOLLOWAY_CAMP->{fit(e,-5,0,-6,CAMP_KIT,Direction.EAST);fit(e,4,0,-10,CAMP_WORKTABLE);fit(e,4,1,-10,DRAFTING_BOARD);fit(e,5,0,-10,CAMP_WORKTABLE);fit(e,5,1,-10,TASK_LAMP);}
  default->{}
 }
 var unique=new LinkedHashMap<BlockPos,Edit>();for(var edit:e)unique.put(edit.relative(),edit);return List.copyOf(unique.values());
 }
 private static boolean safe(ServerLevel l,BlockPos at,Edit e,boolean recording){
  var actual=recording?BuildBlocks.state(l,at):l.getBlockState(at);if(!actual.equals(e.expected())||l.getBlockEntity(at)!=null)return false;
  if(e.expected().isAir()){
   var support=e.next().getValue(VignetteInteriorBlock.KIND).wall()?at.relative(e.next().getValue(VignetteInteriorBlock.FACING).getOpposite()):at.below();
   var below=recording?BuildBlocks.state(l,support):l.getBlockState(support);
   if(below.getCollisionShape(l,support).isEmpty()||below.getCollisionShape(l,support).max(Direction.Axis.Y)<.99)return false;
  }
  return l.getEntities((Entity)null,new AABB(at).inflate(.01),Entity::isAlive).isEmpty();
 }
 public static void fresh(ServerLevel l,BlockPos o,LabyrinthPlace p){
  if(!applies(p))return;var b=LabyrinthPlaces.base(o,p);
  for(var e:plan(p)){var at=b.offset(e.relative());if(safe(l,at,e,true))BuildBlocks.guardedSet(l,at,e.next(),FLAGS,()->safe(l,at,e,false)||l.getBlockState(at).equals(e.next()));}
  BuildBlocks.after(l,()->mark(l,o,p));
 }
 private static void mark(ServerLevel l,BlockPos o,LabyrinthPlace p){var d=LabyrinthData.get(l.getServer());var t=d.state(STATE);t.putBoolean(key(o,p),true);d.setState(STATE,t);}
 private static boolean loaded(ServerLevel l,AABB a){
  for(int x=(int)Math.floor(a.minX)>>4;x<=((int)Math.ceil(a.maxX)-1)>>4;x++)for(int z=(int)Math.floor(a.minZ)>>4;z<=((int)Math.ceil(a.maxZ)-1)>>4;z++)if(!l.hasChunk(x,z)||!l.areEntitiesLoaded(ChunkPos.asLong(x,z)))return false;return true;
 }
 private static boolean vacant(ServerLevel l,AABB a){return l.players().stream().noneMatch(p->a.inflate(24).intersects(p.getCamera().getBoundingBox()));}
 /** At most 256 visits or 3ms per tick. Modified cells and all BEs are skipped, never rebuilt or restocked. */
 public static boolean step(ServerLevel l,BlockPos o,LabyrinthPlace p){
  if(!applies(p))return true;var d=LabyrinthData.get(l.getServer());if(d.state(STATE).getBoolean(key(o,p)))return true;
  var a=SceneReview.area(LabyrinthPlaces.base(o,p),p);if(!loaded(l,a)||!vacant(l,a))return false;
  if(work==null)work=new Work(l,o,p);if(work.level!=l||work.place!=p||!work.origin.equals(o))return false;
  int visits=0;long deadline=System.nanoTime()+3_000_000L;
  while(work.cursor<work.edits.size()&&visits++<256&&System.nanoTime()<deadline){var e=work.edits.get(work.cursor++);var at=work.base.offset(e.relative());if(safe(l,at,e,false))l.setBlock(at,e.next(),FLAGS);}
  if(work.cursor<work.edits.size())return false;mark(l,o,p);work=null;return true;
 }
 public static void tick(MinecraftServer s){
  if(s instanceof net.minecraft.gametest.framework.GameTestServer||LabyrinthBuilder.isCarving())return;
  var o=HouseSavedData.get(s).houseOrigin();if(o==null){work=null;return;}
  if(work!=null){if(work.level.getServer()!=s||!work.origin.equals(o)){work=null;return;}step(work.level,o,work.place);return;}
  if(s.getTickCount()%40!=0||!o.equals(LabyrinthData.get(s).builtOrigin()))return;
  for(var p:PLACES){if(LabyrinthData.get(s).door(p.entryDoorId())==null||LabyrinthData.get(s).state(STATE).getBoolean(key(o,p)))continue;var l=s.getLevel(NovelRooms.dimension(p));
   if(l!=null&&loaded(l,SceneReview.area(LabyrinthPlaces.base(o,p),p))&&vacant(l,SceneReview.area(LabyrinthPlaces.base(o,p),p))){step(l,o,p);break;}}
 }
}
