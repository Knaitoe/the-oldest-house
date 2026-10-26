package io.github.knaitoe.theoldesthouse.gametest;
import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

/** Real native ray picks and block use, persisted objects, sound packets and the original story model identities. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_handling") @PrefixGameTestTemplate(false)
public final class VignetteHandlingTests {
 private static final List<ServerPlayer> PLAYERS=new ArrayList<>();private static NativeTestChunks chunks;private static LiteraryTests.Fixture workshop;
 private static VignetteHandlingBlockEntity object(ServerLevel l,BlockPos at,VignetteHandlingBlock.Kind k){l.setBlock(at.below(),Blocks.STONE.defaultBlockState(),3);l.setBlock(at,VignetteHandlingBlock.of(k,Direction.NORTH),3);return (VignetteHandlingBlockEntity)l.getBlockEntity(at);}
 private static ServerPlayer reader(GameTestHelper h,String n){var p=NativeTestPlayers.survival(h,n);p.setNoGravity(true);PLAYERS.add(p);return p;}
 private static void use(GameTestHelper h,ServerPlayer p,VignetteHandlingBlockEntity b,double x,double z,boolean crouch){
  var l=(ServerLevel)b.getLevel();var at=b.getBlockPos();double xx=x/16,zz=z/16;for(int i=0;i<VignetteHandlingBlock.turns(b.getBlockState().getValue(VignetteHandlingBlock.FACING));i++){double t=xx;xx=1-zz;zz=t;}var target=new Vec3(at.getX()+xx,at.getY()-.1,at.getZ()+zz);
  p.teleportTo(l,target.x,at.getY()+3-p.getEyeHeight(),target.z,0,90);p.setNoGravity(true);p.setShiftKeyDown(crouch);p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
  var hit=l.clip(new ClipContext(p.getEyePosition(),target,ClipContext.Block.OUTLINE,ClipContext.Fluid.NONE,p));
  h.assertTrue(hit.getType()==HitResult.Type.BLOCK&&hit.getBlockPos().equals(at),"an actual native ray picks the handled object");
  p.gameMode.useItemOn(p,l,ItemStack.EMPTY,InteractionHand.MAIN_HAND,hit);p.setShiftKeyDown(false);
 }
 private static void snapshot(String name,VignetteHandlingBlockEntity b){try{
  var j=new JsonObject();j.addProperty("name",name);j.add("state",net.minecraft.world.level.block.state.BlockState.CODEC.encodeStart(JsonOps.INSTANCE,b.getBlockState()).getOrThrow());j.addProperty("data",b.getUpdateTag(b.getLevel().registryAccess()).toString());
  var folder=Path.of("../build/handling-proof");Files.createDirectories(folder);Files.writeString(folder.resolve(name+".json"),j.toString());
 }catch(Exception ex){throw new RuntimeException(ex);}}
 private static VignetteHandlingBlockEntity reload(VignetteHandlingBlockEntity b){var l=b.getLevel();var tag=b.saveWithFullMetadata(l.registryAccess());var at=b.getBlockPos();var state=b.getBlockState();l.removeBlockEntity(at);var fresh=BlockEntity.loadStatic(at,state,tag,l.registryAccess());l.setBlockEntity(fresh);return (VignetteHandlingBlockEntity)fresh;}
 @GameTest(template="empty",batch="handling_keys",timeoutTicks=250)
 public static void nativeKeysHaveTwelvePitchesAndSlidesReallyAdvanceAndEject(GameTestHelper h){
  var l=h.getLevel();var at=h.absolutePos(new BlockPos(2,2,2));chunks=new NativeTestChunks();chunks.hold(l,new AABB(at).inflate(6));
  h.startSequence().thenWaitUntil(()->h.assertTrue(chunks.ready(),"object sections resident")).thenExecute(()->{
   var p=reader(h,"keys_and_slides");var toy=object(l,at,VignetteHandlingBlock.Kind.TOY_PIANO);var spinet=object(l,at.east(3),VignetteHandlingBlock.Kind.SPINET);var slides=object(l,at.east(6),VignetteHandlingBlock.Kind.SLIDES);
   int[] notes={0,2,4,5,7,9,11};var pitches=new HashSet<Float>();
   for(var piano:List.of(toy,spinet)){
    for(int i=0;i<7;i++){int note=notes[i];NativeTestPlayers.sounds(p);use(h,p,piano,14-i*2,3,false);h.assertTrue(piano.view().key()==notes[i],"the picked white key depresses its own geometry");var sounds=NativeTestPlayers.sounds(p);var expected=(piano==toy?LiteraryRegistry.HANDLING_TOY:LiteraryRegistry.HANDLING_SPINET).get();h.assertTrue(sounds.stream().anyMatch(s->s.getSound().value()==expected&&Math.abs(s.getPitch()-Math.pow(2,note/12.0))<.001),"the real native sound packet uses the right instrument and pitch");if(piano==toy)pitches.add((float)Math.pow(2,notes[i]/12.0));}
    int[] gaps={13,11,7,5,3},black={1,3,6,8,10};for(int i=0;i<5;i++){use(h,p,piano,gaps[i],7,false);h.assertTrue(piano.view().key()==black[i],"the raised black key is independently picked");if(piano==toy)pitches.add((float)Math.pow(2,black[i]/12.0));}
   }
   for(var facing:Direction.Plane.HORIZONTAL){l.setBlock(toy.getBlockPos(),toy.getBlockState().setValue(VignetteHandlingBlock.FACING,facing),3);use(h,p,toy,13,7,false);h.assertTrue(toy.view().key()==1,"the same raised black key is correctly picked in every native orientation");}
   l.setBlock(toy.getBlockPos(),toy.getBlockState().setValue(VignetteHandlingBlock.FACING,Direction.NORTH),3);
   h.assertTrue(pitches.size()==12,"an octave contains twelve distinct pitches, not one generic note");
   use(h,p,toy,14,3,false);snapshot("01_toy_c",toy);use(h,p,toy,13,7,false);snapshot("02_toy_cs",toy);
   use(h,p,spinet,14,3,false);snapshot("03_spinet_c",spinet);use(h,p,spinet,6,3,false);snapshot("04_spinet_g",spinet);
   snapshot("05_slide_window",slides);for(int i=1;i<4;i++){use(h,p,slides,3,0.5,false);h.assertTrue(slides.view().slide()==i,"advancing selects a visibly different native slide");snapshot("0"+(5+i)+"_slide_"+i,slides);}
   use(h,p,slides,8,3,false);h.assertTrue(slides.view().tray()&&slides.view().slide()==3,"the tray ejects without replacing the selected slide");snapshot("09_slide_ejected",slides);
   use(h,p,slides,8,0.5,false);h.assertTrue(!slides.view().tray(),"the same physical handle returns the tray");snapshot("10_slide_returned",slides);
   use(h,p,slides,14,14,false);h.assertTrue(slides.view().lamp()&&slides.getBlockState().getLightEmission(l,slides.getBlockPos())==7,"the real lightbox emits light");snapshot("11_slide_lit",slides);
   var saved=reload(slides);h.assertTrue(saved.view().slide()==3&&saved.view().lamp()&&!saved.view().tray(),"tray, selected slide and lamp survive native serialization");use(h,p,saved,14,14,false);snapshot("12_slide_unlit",saved);
  }).thenIdle(8).thenExecute(()->{
   for(var off:new int[]{0,3})h.assertTrue(((VignetteHandlingBlockEntity)l.getBlockEntity(at.east(off))).view().key()==-1,"depressed keys release on native server time");h.succeed();
  });
 }
 @GameTest(template="empty",batch="handling_models",timeoutTicks=500)
 public static void practiceFurnitureMovesAndRotatesWithoutChangingOriginalRecordedRooms(GameTestHelper h){
  var f=new LiteraryTests.Fixture(h,144);workshop=f;HouseSavedData.get(h.getLevel().getServer()).markInteriorInitialized();var room=LabyrinthPlace.MINIATURES;var l=f.level(room);var b=f.base(room);f.chunks.hold(l,SceneReview.area(b,room));
  h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"actual workshop sections resident")).thenExecute(()->{
   f.build(room);ScenePolish.polishOnce(l,f.origin,room);
   var originals=List.of(l.getBlockEntity(b.offset(-8,1,-18)),l.getBlockEntity(b.offset(0,1,-18)),l.getBlockEntity(b.offset(8,1,-18)));
   var originalData=originals.stream().map(o->o.getUpdateTag(l.registryAccess()).copy()).toList();var source=l.getBlockEntity(b.offset(LiteraryRooms.source(room)));
   var kit=(VignetteHandlingBlockEntity)l.getBlockEntity(b.offset(-10,1,-10));h.assertTrue(kit!=null,"the spare kit occupies the workbench, not a story-model stand");var p=reader(h,"model_maker");
   snapshot("13_model_initial",kit);use(h,p,kit,4,4,false);h.assertTrue(kit.view().held()==0,"the actual chair can be lifted");snapshot("14_model_lifted",kit);var peer=reader(h,"second_model_maker");use(h,peer,kit,8,8,false);h.assertTrue(kit.view().held()==0&&kit.view().cells()[0]==0,"another reader cannot move a piece while its first reader holds it");
   var received=new VignetteHandlingBlockEntity(kit.getBlockPos(),kit.getBlockState());received.onDataPacket(null,net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(kit),l.registryAccess());h.assertTrue(received.view().held()==0,"the actual native BE packet carries the lifted-piece view");
   use(h,p,kit,8,4,false);h.assertTrue(kit.view().cells()[0]==1&&kit.view().held()==-1,"the chair is placed in the chosen empty cell");snapshot("15_model_moved",kit);
   use(h,p,kit,8,4,true);h.assertTrue(kit.view().turns()[0]==1,"crouch-use rotates the chosen piece, not the whole object");snapshot("16_model_turned",kit);
   var fresh=reload(kit);h.assertTrue(fresh.view().cells()[0]==1&&fresh.view().turns()[0]==1&&fresh.view().held()==-1,"native reload keeps the arrangement and releases a temporary pick");snapshot("17_model_reloaded",fresh);
   for(int i=0;i<3;i++)h.assertTrue(l.getBlockEntity(b.offset(new int[]{-8,0,8}[i],1,-18))==originals.get(i)&&originals.get(i).getUpdateTag(l.registryAccess()).equals(originalData.get(i)),"each original recorded-room BE remains identical and unchanged");
   h.assertTrue(l.getBlockEntity(b.offset(LiteraryRooms.source(room)))==source,"the finite original account is untouched");
   h.succeed();
  });
 }
 @GameTest(template="empty",batch="handling_tape",timeoutTicks=650)
 public static void recorderActuallyPlaysPausesRewindsAndFinishesWithoutAffectingItsNeighbour(GameTestHelper h){
  var l=h.getLevel();var at=h.absolutePos(new BlockPos(2,2,2));chunks=new NativeTestChunks();chunks.hold(l,new AABB(at).inflate(6));
  final VignetteHandlingBlockEntity[] first={null},other={null};final int[] paused={0};final ServerPlayer[] p={null};
  h.startSequence().thenWaitUntil(()->h.assertTrue(chunks.ready(),"recorder sections resident")).thenExecute(()->{
   p[0]=reader(h,"tape_listener");first[0]=object(l,at,VignetteHandlingBlock.Kind.TAPE);other[0]=object(l,at.east(3),VignetteHandlingBlock.Kind.TAPE);
   use(h,p[0],first[0],7.5,3,false);use(h,p[0],other[0],7.5,3,false);h.assertTrue(first[0].view().mode()==1&&other[0].view().mode()==1,"the two actual play buttons engage separate transports");snapshot("18_tape_started",first[0]);
  }).thenIdle(7).thenExecute(()->{h.assertTrue(first[0].view().cursor()>0,"the native ticker advances the tape");snapshot("19_tape_turning_a",first[0]);}).thenIdle(6).thenExecute(()->{
   snapshot("20_tape_turning_b",first[0]);use(h,p[0],first[0],4,3,false);paused[0]=first[0].view().cursor();snapshot("21_tape_paused",first[0]);
  }).thenIdle(16).thenExecute(()->{
   h.assertTrue(first[0].view().cursor()==paused[0]&&first[0].view().mode()==0,"pause holds the actual tape position");h.assertTrue(other[0].view().cursor()>paused[0],"pausing one recorder leaves its neighbour running");
   first[0]=reload(first[0]);h.assertTrue(first[0].view().cursor()==paused[0]&&first[0].view().mode()==0,"a paused native recording resumes from its saved position");
   use(h,p[0],first[0],12,3,false);h.assertTrue(first[0].view().mode()==2,"rewind is a separate running transport");snapshot("22_tape_rewinding",first[0]);
  }).thenIdle(8).thenExecute(()->{
   h.assertTrue(first[0].view().cursor()==0&&first[0].view().mode()==0,"rewind returns the tape to its beginning and stops");snapshot("23_tape_rewound",first[0]);use(h,p[0],first[0],7.5,3,false);
  }).thenIdle(325).thenExecute(()->{
   h.assertTrue(first[0].view().cursor()==VignetteHandlingBlockEntity.LENGTH&&first[0].view().mode()==0,"the finite recording reaches its real end and stops");snapshot("24_tape_ended",first[0]);h.succeed();
  });
 }
 @GameTest(template="empty",batch="handling_upgrade",timeoutTicks=450)
 public static void earlierIncidentalFittingsUpgradeWithoutResettingOriginalsUsedControlsOrPlayerContainers(GameTestHelper h){
  var f=new LiteraryTests.Fixture(h,144);workshop=f;HouseSavedData.get(h.getLevel().getServer()).markInteriorInitialized();var room=LabyrinthPlace.KAREN_ROOM;var l=f.level(room);var b=f.base(room);f.chunks.hold(l,SceneReview.area(b,room));
  h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"legacy bedroom sections resident")).thenExecute(()->{
   NovelRooms.build(l.getServer(),l,b,room);
   for(var stale:l.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,SceneReview.area(b,room)))stale.discard(); // Only debris from this fixture's previous native world.
   var original=l.getBlockEntity(b.offset(6,1,-11));var at=b.offset(7,1,-10);
   l.setBlock(at.below(),VignetteInteriorBlock.of(VignetteInteriorBlock.Kind.SLIDE_CABINET,Direction.WEST),3);
   l.setBlock(at,VignetteInteriorBlock.of(VignetteInteriorBlock.Kind.KAREN_SLIDE_SORTER,Direction.WEST).setValue(VignetteInteriorBlock.ACTIVE,true),3);
   var body=net.minecraft.world.entity.EntityType.ARMOR_STAND.create(l);body.setNoGravity(true);body.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5);l.addFreshEntity(body);f.extras.add(body);
   h.assertTrue(!VignetteHandlingUpgrade.step(l,f.origin,room)&&l.getBlockState(at).is(LiteraryRegistry.INTERIOR.get()),"an occupied legacy fitting is not changed");body.discard();
   h.assertTrue(VignetteHandlingUpgrade.step(l,f.origin,room),"the same fitting upgrades once its own resident cell is vacant: "+l.getEntities((net.minecraft.world.entity.Entity)null,new AABB(at).inflate(.02),net.minecraft.world.entity.Entity::isAlive));var handled=(VignetteHandlingBlockEntity)l.getBlockEntity(at);
   h.assertTrue(handled!=null&&handled.view().lamp()&&l.getBlockState(at).getValue(VignetteHandlingBlock.FACING)==Direction.WEST,"the old switched-on light and physical orientation survive");
   h.assertTrue(l.getBlockEntity(b.offset(6,1,-11))==original,"the original source is never recreated or restocked");
   f.reload();h.assertTrue(VignetteHandlingUpgrade.step(l,f.origin,room)&&l.getBlockEntity(at)==handled,"the loaded checkpoint retains the same handled block entity");
   l.setBlock(at,Blocks.CHEST.defaultBlockState(),3);var chest=l.getBlockEntity(at);((net.minecraft.world.Container)chest).setItem(0,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND));
   var data=f.data();var tag=data.state(VignetteHandlingUpgrade.STATE);tag.remove(f.origin.asLong()+":"+room.id());data.setState(VignetteHandlingUpgrade.STATE,tag);
   h.assertTrue(VignetteHandlingUpgrade.step(l,f.origin,room)&&l.getBlockEntity(at)==chest&&((net.minecraft.world.Container)chest).getItem(0).is(net.minecraft.world.item.Items.DIAMOND),"even a repeated migration cannot replace a player's container or contents");h.succeed();
  });
 }
 @AfterBatch(batch="handling_upgrade") public static void closeUpgrade(ServerLevel l){close();}
 @AfterBatch(batch="handling_keys") public static void closeKeys(ServerLevel l){close();}
 @AfterBatch(batch="handling_models") public static void closeModels(ServerLevel l){close();}
 @AfterBatch(batch="handling_tape") public static void closeTape(ServerLevel l){close();}
 private static void close(){for(var p:PLAYERS)NativeTestPlayers.remove(p);PLAYERS.clear();if(chunks!=null){chunks.close();chunks=null;}if(workshop!=null){workshop.close();workshop=null;}}
}
