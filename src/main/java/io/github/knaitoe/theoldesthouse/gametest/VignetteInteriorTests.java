package io.github.knaitoe.theoldesthouse.gametest;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

/** Production construction and ordinary use; original custody and real player approaches. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_interior_review") @PrefixGameTestTemplate(false)
public final class VignetteInteriorTests {
 private static LiteraryTests.Fixture fixture;
 private static BlockEntity originalChest;private static net.minecraft.world.entity.Entity originalActor;
 private static final List<BlockEntity> originals=new ArrayList<>();
 private static void build(LiteraryTests.Fixture f,LabyrinthPlace p){
  f.built.add(p);var l=f.level(p);var b=f.base(p);var s=l.getServer();
  switch(p){
   case FLOORBOARDS->TellTaleFloorboards.build(l,b,true);case HIDE_AND_CLAP->HideAndClap.build(s,l,b);
   case SEANCE,WALLPAPER_NURSERY->ClassicsRooms.build(l,b,p);case KAREN_ROOM->NovelRooms.build(s,l,b,p);
   case HOLLOWAY_CAMP->HollowayCamp.build(l,b);default->LiteraryRooms.build(l,b,p);
  }
  LabyrinthBuilder.registerDoors(f.data(),p,b);
  if(!LiteraryRooms.isLiterary(p)){HouseFurnishings.decorate(l,b,p);VignetteArchitecture.decorateOnce(l,f.origin,p);}
  ScenePolish.polishOnce(l,f.origin,p);
 }
 @GameTest(template="empty",batch="interior_rooms",timeoutTicks=900)
 public static void individualFittingsWorkAndLeaveTheOriginalStoryRoutesClear(GameTestHelper h){
  var f=new LiteraryTests.Fixture(h);fixture=f;HouseSavedData.get(h.getLevel().getServer()).markInteriorInitialized();
  for(var p:VignetteInteriors.PLACES)f.chunks.hold(f.level(p),SceneReview.area(f.base(p),p));
  f.chunks.hold(f.in,SceneReview.area(f.base(LabyrinthPlace.QUIET_ROOM),LabyrinthPlace.QUIET_ROOM));
  h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"all authored room sections are resident")).thenExecute(()->{
   var reader=NativeTestPlayers.survival(h,"interior_reader");f.players.add(reader);var missing=new ArrayList<String>();
   for(var p:VignetteInteriors.PLACES){
    build(f,p);var l=f.level(p);var b=f.base(p);int fittings=0;
    for(var edit:VignetteInteriors.plan(p)){
     if(edit.next().is(LiteraryRegistry.HANDLING.get())){
      var at=b.offset(edit.relative());h.assertTrue(l.getBlockState(at).is(LiteraryRegistry.HANDLING.get())&&l.getBlockEntity(at) instanceof VignetteHandlingBlockEntity,"the original fitting address receives its native handled object: "+p+" "+edit.relative());fittings++;continue;
     }
     var kind=edit.next().getValue(VignetteInteriorBlock.KIND);
     if(kind.surface()){
      if(edit.expected().isAir()&&!l.getBlockState(b.offset(edit.relative())).equals(edit.next()))missing.add(p.id()+" incomplete authored partition at "+edit.relative()+" found "+l.getBlockState(b.offset(edit.relative())));
      continue;
     }
     var at=b.offset(edit.relative());var state=l.getBlockState(at);
     if(!VignetteInteriorBlock.is(state,kind)){missing.add(p.id()+" "+kind+" at "+edit.relative()+" found "+state+" over "+l.getBlockState(at.below())+" back "+l.getBlockState(at.relative(edit.next().getValue(VignetteInteriorBlock.FACING).getOpposite()))+" occupants "+l.getEntities((net.minecraft.world.entity.Entity)null,new AABB(at).inflate(.01),net.minecraft.world.entity.Entity::isAlive));continue;}
     fittings++;var shape=state.getCollisionShape(l,at);h.assertTrue(!shape.isEmpty()&&shape.min(Direction.Axis.Y)>=0&&shape.max(Direction.Axis.Y)<=1,"the fitting uses contained native collision: "+kind);
     if(!kind.usable())continue;
     reader.teleportTo(l,at.getX()+2.5,at.getY(),at.getZ()+2.5,0,0);reader.setNoGravity(true);
     h.assertTrue(VignetteInteriorBlock.interact(l,at,reader)==InteractionResult.CONSUME,"ordinary native use reaches "+kind);
     h.assertTrue(l.getBlockState(at).getValue(VignetteInteriorBlock.ACTIVE),"use changes the actual saved fitting state: "+kind);
     if(kind.lamp())h.assertTrue(l.getBlockState(at).getLightEmission(l,at)==11,"the lamp changes real emitted light");
     VignetteInteriorBlock.interact(l,at,reader);h.assertTrue(!l.getBlockState(at).getValue(VignetteInteriorBlock.ACTIVE),"the fitting can also be closed or switched off: "+kind);
    }
    h.assertTrue(fittings>0,p.id()+" has its authored physical details");
    if(LiteraryRooms.isLiterary(p)){
     h.assertTrue(!SceneHuntReview.sourceBook(l,b,p).isEmpty(),p.id()+" keeps its finite original source");
     for(int z=-3;z>=-6;z--)body(h,l,b,0,0,z,p.id()+" arrival");
    }
    if(p==LabyrinthPlace.CONFESSION){body(h,l,b,1,0,-12,"interview entrance");body(h,l,b,-3,0,-24,"archive-to-interview side opening");}
    if(p==LabyrinthPlace.USHER)for(int side:new int[]{-1,1}){body(h,l,b,side*6,0,-13,"study-to-vault opening");body(h,l,b,side*10,0,-8,"study's front arch");}
    if(p==LabyrinthPlace.DEVILS_ROCK){body(h,l,b,-4,0,-8,"kitchen entrance");h.assertTrue(l.getBlockState(b.offset(-9,0,-12)).isAir(),"the first loose page retains its original landing inside the kitchen");}
    if(p==LabyrinthPlace.MINIATURES)for(int x:new int[]{-8,0,8})body(h,l,b,x,0,-16,"model examination front");
    if(p==LabyrinthPlace.MAPPING_INTERIOR){body(h,l,b,0,0,-16,"original hatch approach");var c=b.offset(0,-5,-40);h.assertTrue(l.noCollision(null,new AABB(c.getX()+.2,c.getY()+.01,c.getZ()+.2,c.getX()+.8,c.getY()+.6,c.getZ()+.8)),"the low crawl stays traversable while crawling");}
    if(p==LabyrinthPlace.CONFESSION){body(h,l,b,1,0,-19,"the two original interview chairs");h.assertTrue(l.getBlockState(b.offset(11,0,-20)).is(Blocks.BOOKSHELF),"the original shelf still receives the blank-book story interaction");}
    if(p==LabyrinthPlace.CRIMSON_HALL)h.assertTrue(l.getBlockEntity(b.offset(0,0,-30)) instanceof net.minecraft.world.level.block.entity.JukeboxBlockEntity,"the three originals still play in a real native jukebox");
    try{ArchitectureTests.export(l,b,p);}catch(Exception ex){throw new RuntimeException(ex);}
   }
   h.assertTrue(missing.isEmpty(),"Every authored fitting has an unobstructed position: "+missing);
   try{var folder=java.nio.file.Path.of("../build/interior-environment-proof");java.nio.file.Files.createDirectories(folder);
    for(var p:VignetteInteriors.PLACES)java.nio.file.Files.copy(java.nio.file.Path.of("../build/architecture-proof",p.id()+".json"),folder.resolve(p.id()+".json"),java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    slice(folder,"mapping_interior","mapping_living_rooms",-13,13,-36,-8,-1,3);
    slice(folder,"end_world_cabin","holiday_living_rooms",-12,12,-35,-13,-1,3);
    slice(folder,"usher","usher_vault",-8,8,-35,-18,-4,-2);
    slice(folder,"hill_nursery","hill_cellar",-13,13,-42,-30,-5,-2);
    var quiet=LabyrinthPlace.QUIET_ROOM;f.built.add(quiet);var qb=f.base(quiet);LabyrinthHalls.build(f.in,qb,quiet);ScenePolish.polishOnce(f.in,f.origin,quiet);ArchitectureTests.export(f.in,qb,quiet);
    java.nio.file.Files.move(java.nio.file.Path.of("../build/architecture-proof",quiet.id()+".json"),folder.resolve(quiet.id()+".json"),java.nio.file.StandardCopyOption.REPLACE_EXISTING);
   }catch(Exception ex){throw new RuntimeException(ex);}h.succeed();
  });
 }
 @GameTest(template="empty",batch="interior_refit",timeoutTicks=500)
 public static void boundedRefitKeepsOriginalCustodyPlayerEditsAndUsedSwitchesAcrossReload(GameTestHelper h){
  var f=new LiteraryTests.Fixture(h,144);fixture=f;HouseSavedData.get(h.getLevel().getServer()).markInteriorInitialized();
  var p=LabyrinthPlace.MINIATURES;var l=f.level(p);var b=f.base(p);f.chunks.hold(l,SceneReview.area(b,p));
  h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"legacy room is resident")).thenExecute(()->{
   f.built.add(p);LiteraryRooms.build(l,b,p);LabyrinthBuilder.registerDoors(f.data(),p,b);
   originals.clear();for(var rel:List.of(LiteraryRooms.source(p),new BlockPos(-8,1,-18),new BlockPos(0,1,-18),new BlockPos(8,1,-18)))originals.add(l.getBlockEntity(b.offset(rel)));
   l.setBlock(b.offset(-14,2,-13),Blocks.GOLD_BLOCK.defaultBlockState(),3);l.setBlock(b.offset(-10,0,-9),Blocks.CHEST.defaultBlockState(),3);
   originalChest=l.getBlockEntity(b.offset(-10,0,-9));((net.minecraft.world.Container)originalChest).setItem(0,new ItemStack(Items.DIAMOND));
   var actor=net.minecraft.world.entity.EntityType.ARMOR_STAND.create(l);actor.setNoGravity(true);actor.moveTo(b.getX()-9.5,b.getY(),b.getZ()-10.5);l.addFreshEntity(actor);f.extras.add(actor);originalActor=actor;
  }).thenWaitUntil(()->h.assertTrue(VignetteInteriors.step(l,f.origin,p),"the sliced refit finishes its saved checkpoint")).thenExecute(()->{
   h.assertTrue(l.getBlockState(b.offset(-14,2,-13)).is(Blocks.GOLD_BLOCK),"the edited wall is not repainted");
   h.assertTrue(l.getBlockEntity(b.offset(-10,0,-9))==originalChest&&((net.minecraft.world.Container)originalChest).getItem(0).is(Items.DIAMOND),"the same container and finite contents survive");
   var rels=List.of(LiteraryRooms.source(p),new BlockPos(-8,1,-18),new BlockPos(0,1,-18),new BlockPos(8,1,-18));
   for(int i=0;i<rels.size();i++)h.assertTrue(originals.get(i)!=null&&l.getBlockEntity(b.offset(rels.get(i)))==originals.get(i),"the finite source and original miniature block entities retain their identities");
   h.assertTrue(l.getEntity(originalActor.getUUID())==originalActor&&l.getBlockState(b.offset(-10,0,-11)).isAir(),"occupied cell is preserved without moving or recreating its body");
   var at=b.offset(11,0,-23);h.assertTrue(VignetteInteriorBlock.is(l.getBlockState(at),VignetteInteriorBlock.Kind.MODEL_DRAWERS),"the unoccupied original space receives its cabinet");
   l.setBlock(at,l.getBlockState(at).setValue(VignetteInteriorBlock.ACTIVE,true),3);f.reload();VignetteInteriors.step(l,f.origin,p);
   h.assertTrue(l.getBlockState(at).getValue(VignetteInteriorBlock.ACTIVE),"the reloaded checkpoint never restocks or closes an already used fitting");h.succeed();
  });
 }
 @GameTest(template="empty",batch="interior_geometry",timeoutTicks=50)
 public static void everyRenderedStateHasMatchingBoundedNativeCollision(GameTestHelper h){
  concurrentNativeShapeReads();
  var l=h.getLevel();var at=h.absolutePos(new BlockPos(0,1,0));
  for(var kind:VignetteInteriorBlock.Kind.values())for(var f:Direction.Plane.HORIZONTAL)for(boolean active:new boolean[]{false,true}){
   var state=VignetteInteriorBlock.of(kind,f).setValue(VignetteInteriorBlock.ACTIVE,active);var shape=state.getCollisionShape(l,at);
   h.assertTrue(!shape.isEmpty()&&shape.min(Direction.Axis.X)>=0&&shape.max(Direction.Axis.X)<=1&&shape.min(Direction.Axis.Y)>=0&&shape.max(Direction.Axis.Y)<=1&&shape.min(Direction.Axis.Z)>=0&&shape.max(Direction.Axis.Z)<=1,"every model state is bounded and solid: "+kind+" "+f+" "+active);
  }h.succeed();
 }
 /** Cold native states reproduce first-load meshing without touching a world off-thread. */
 private static void concurrentNativeShapeReads(){
  var states=new ArrayList<net.minecraft.world.level.block.state.BlockState>();
  // Use the real registered blocks: constructing another Block after bootstrap attempts
  // to add an intrusive holder to Minecraft's frozen registry. Only cool the pure caches;
  // no registry, block-state definition or world is mutated by the workers.
  for(var block:List.of(LiteraryRegistry.DETAIL.get(),LiteraryRegistry.INTERIOR.get())){
   try{var field=block.getClass().getDeclaredField("SHAPES");field.setAccessible(true);((Map<?,?>)field.get(null)).clear();}
   catch(ReflectiveOperationException ex){throw new IllegalStateException("Cannot prepare cold native shape cache",ex);}
   states.addAll(block.getStateDefinition().getPossibleStates());
  }
  var workers=java.util.concurrent.Executors.newFixedThreadPool(4);var start=new java.util.concurrent.CyclicBarrier(4);var jobs=new ArrayList<java.util.concurrent.CompletableFuture<Void>>();
  try{
   for(int worker=0;worker<4;worker++){int offset=worker*17;jobs.add(java.util.concurrent.CompletableFuture.runAsync(()->{
    try{start.await();}catch(InterruptedException ex){Thread.currentThread().interrupt();throw new IllegalStateException(ex);}catch(java.util.concurrent.BrokenBarrierException ex){throw new IllegalStateException(ex);}
    for(int pass=0;pass<2;pass++)for(int i=0;i<states.size();i++){var state=states.get((i+offset)%states.size());var empty=net.minecraft.world.level.EmptyBlockGetter.INSTANCE;var outline=state.getShape(empty,BlockPos.ZERO);var collision=state.getCollisionShape(empty,BlockPos.ZERO);boolean roach=state.getBlock() instanceof ServiceFittingBlock&&state.getValue(ServiceFittingBlock.KIND)==ServiceFittingBlock.Kind.ROACH;
     if(outline.isEmpty()||collision.isEmpty()!=roach)throw new IllegalStateException("Concurrent native shape changed: "+state);
    }
   },workers));}
   java.util.concurrent.CompletableFuture.allOf(jobs.toArray(java.util.concurrent.CompletableFuture[]::new)).get(15,java.util.concurrent.TimeUnit.SECONDS);
  }catch(Exception ex){throw new IllegalStateException("Cold native collision/outline lookups failed on meshing workers",ex);}finally{workers.shutdownNow();}
 }
 /** A documented crop of the same actual server export; never invented geometry or a lighting simulation. */
 static void slice(java.nio.file.Path folder,String source,String name,int x0,int x1,int z0,int z1,int y0,int y1)throws Exception{
  var root=com.google.gson.JsonParser.parseString(java.nio.file.Files.readString(folder.resolve(source+".json"))).getAsJsonObject();var blocks=new com.google.gson.JsonArray();
  for(var element:root.getAsJsonArray("blocks")){var row=element.getAsJsonArray();int x=row.get(0).getAsInt(),y=row.get(1).getAsInt(),z=row.get(2).getAsInt();
   if(x<x0||x>x1||z<z0||z>z1||y<y0||y>y1||y>y0&&(x==x1||z==z1))continue;blocks.add(row);
  }root.addProperty("name",name);root.add("blocks",blocks);java.nio.file.Files.writeString(folder.resolve(name+".json"),root.toString());
 }
 private static void body(GameTestHelper h,ServerLevel l,BlockPos b,int x,int y,int z,String what){var at=b.offset(x,y,z);h.assertTrue(l.noCollision(null,new AABB(at.getX()+.2,at.getY()+.01,at.getZ()+.2,at.getX()+.8,at.getY()+1.8,at.getZ()+.8)),what+" remains physically clear");}
 @AfterBatch(batch="interior_rooms") public static void closeRooms(ServerLevel l){close();}
 @AfterBatch(batch="interior_refit") public static void closeRefit(ServerLevel l){close();}
 private static void close(){if(fixture!=null){fixture.close();fixture=null;}originals.clear();originalChest=null;originalActor=null;}
}
