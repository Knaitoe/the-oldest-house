package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.*;

/** Native load-bearing failure, finite originals, actual private entry, persisted damage and confirmed personal return. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_silo") @PrefixGameTestTemplate(false)
public final class SiloTests {
    private static LiteraryTests.Fixture active;
    @AfterBatch(batch="silo_weight") public static void weightDone(ServerLevel ignored){close();}
    @AfterBatch(batch="silo_papers") public static void papersDone(ServerLevel ignored){close();}
    @AfterBatch(batch="silo_source") public static void sourceDone(ServerLevel ignored){close();}
    private static void close(){SiloSites.clear();if(active!=null){active.close();active=null;}}
    private static LiteraryTests.Fixture fixture(GameTestHelper h){active=new LiteraryTests.Fixture(h);return active;}
    private static void click(GameTestHelper h,ServerPlayer p,BlockPos at){var e=new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,at,new BlockHitResult(at.getCenter(),Direction.SOUTH,at,false));NeoForge.EVENT_BUS.post(e);h.assertTrue(e.isCanceled()&&e.getCancellationResult().consumesAction(),"the actual native silo source/prop packet is handled");}
    private static void transition(ServerPlayer p){HouseTransitionEvents.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p));}
    private static void export(ServerLevel l,BlockPos b){export(l,b,"silo");}
    private static void export(ServerLevel l,BlockPos b,String name){try{
        var json=new com.google.gson.JsonObject();var palette=new com.google.gson.JsonArray();var blocks=new com.google.gson.JsonArray();var lookup=new LinkedHashMap<net.minecraft.world.level.block.state.BlockState,Integer>();
        for(var at:BlockPos.betweenClosed(b.offset(-11,-1,-11),b.offset(11,67,14))){var s=l.getBlockState(at);if(s.isAir())continue;Integer id=lookup.get(s);if(id==null){id=lookup.size();lookup.put(s,id);palette.add(net.minecraft.world.level.block.state.BlockState.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE,s).getOrThrow());}var row=new com.google.gson.JsonArray();row.add(at.getX()-b.getX());row.add(at.getY()-b.getY());row.add(at.getZ()-b.getZ());row.add(id);blocks.add(row);}
        json.add("palette",palette);json.add("blocks",blocks);var dir=java.nio.file.Path.of("../build/silo-art");java.nio.file.Files.createDirectories(dir);java.nio.file.Files.writeString(dir.resolve(name+".json"),new com.google.gson.Gson().toJson(json));
    }catch(Exception e){throw new IllegalStateException("Cannot export the actual native silo",e);}}

    @GameTest(template="empty",batch="silo_weight",timeoutTicks=280)
    public static void actualWeightOpensTheRealFloorAndReloadNeverHealsItsFatigue(GameTestHelper h){
        var f=fixture(h);var at=f.origin.offset(0,35,-80);f.chunks.hold(f.in,new AABB(at).inflate(8));var body=new net.minecraft.world.entity.npc.Villager(EntityType.VILLAGER,f.in);body.getBrain().removeAllBehaviors();f.extras.add(body);var observer=NativeTestPlayers.survival(h,"silo_spectator");f.players.add(observer);observer.setGameMode(GameType.SPECTATOR);double[] fatigue={0};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native timber and falling-body sections are resident")).thenExecute(()->{
            for(var p:BlockPos.betweenClosed(at.offset(-3,-9,-3),at.offset(3,4,3)))f.in.setBlock(p,p.getY()==at.getY()-9?SiloRegistry.CORN.get().defaultBlockState():Blocks.AIR.defaultBlockState(),18);
            f.in.setBlock(at,SiloTimberBlock.of(SiloTimberBlock.Kind.DOOR_LOW,Direction.NORTH),18);var wood=(SiloTimberBlock.Timber)f.in.getBlockEntity(at);wood.fatigue=23.8;body.moveTo(at.getX()+.5,at.getY()+.75,at.getZ()+.5);body.setOnGround(true);f.in.addFreshEntity(body);
        }).thenWaitUntil(()->h.assertTrue(f.in.getBlockState(at).getValue(SiloTimberBlock.STRAIN)>=1,"the real standing body deforms its physical floor")).thenExecute(()->{
            var wood=(SiloTimberBlock.Timber)f.in.getBlockEntity(at);fatigue[0]=wood.fatigue;var saved=wood.saveWithFullMetadata(f.in.registryAccess());wood.loadWithComponents(saved,f.in.registryAccess());h.assertTrue(wood.fatigue==fatigue[0],"native BE serialization preserves exact accumulated load");body.setNoGravity(true);body.moveTo(at.getX()+4,at.getY()+2,at.getZ());observer.teleportTo(f.in,at.getX()+.5,at.getY()+.75,at.getZ()+.5,0,0);observer.setOnGround(true);
        }).thenIdle(15).thenExecute(()->{
            var wood=(SiloTimberBlock.Timber)f.in.getBlockEntity(at);h.assertTrue(wood.fatigue==fatigue[0],"unoccupied wood does not heal, and a spectator does not load it");wood.fatigue=99.8;body.setNoGravity(false);body.moveTo(at.getX()+.5,at.getY()+.7421875,at.getZ()+.5);body.setDeltaMovement(Vec3.ZERO);body.setOnGround(true);
        }).thenWaitUntil(()->h.assertTrue(f.in.getBlockState(at).getValue(SiloTimberBlock.STRAIN)==4,"the timber breaks under continued actual weight"))
          .thenWaitUntil(()->h.assertTrue(body.getY()<at.getY()-3,"the real body falls through the changed native collision"))
          .thenWaitUntil(()->h.assertTrue(body.onGround(),"the actual grain receives the fall"))
          .thenExecute(()->h.assertTrue(body.getHealth()==body.getMaxHealth(),"grain catches the body without charging health")).thenSucceed();
    }

    @GameTest(template="empty",batch="silo_papers",timeoutTicks=300)
    public static void theThreeExactOriginalsTransferOnceAndAnObserverCannotTakeTheirSnapshots(GameTestHelper h){
        var f=fixture(h);var b=f.origin.offset(16,8,-80);f.chunks.hold(f.in,new AABB(b).inflate(8));var p=NativeTestPlayers.survival(h,"silo_paper_owner");var peer=NativeTestPlayers.survival(h,"silo_paper_peer");f.players.add(p);f.players.add(peer);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the physical paper supports are loaded")).thenExecute(()->{
            for(int i=0;i<3;i++){
                var at=b.offset(i*2,0,0);f.in.setBlock(at.below(),SiloRegistry.STEEL.get().defaultBlockState(),18);f.in.setBlock(at,SiloRegistry.PAPER.get().defaultBlockState(),18);var paper=(SiloPaperBlock.Paper)f.in.getBlockEntity(at);paper.seed(p.getUUID(),i);var original=paper.original();
                String text=String.join(" ",original.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(page->page.raw().getString()).toList());h.assertTrue(text.equals(SiloTexts.PASSAGES[i]),"each supplied passage survives native book pagination verbatim");
                peer.teleportTo(f.in,at.getX()+.5,at.getY(),at.getZ()+2,0,0);paper.open(peer);h.assertTrue(peer.containerMenu==peer.inventoryMenu,"another reader cannot open the owned original");
                p.teleportTo(f.in,at.getX()+.5,at.getY(),at.getZ()+2,0,0);paper.open(p);h.assertTrue(p.containerMenu instanceof LecternMenu,"the actual paper opens native readable pages");var menu=p.containerMenu;menu.clicked(0,0,ClickType.QUICK_MOVE,p);h.assertTrue(ItemStack.matches(original,paper.original()),"inventory packets cannot extract a display snapshot");
                h.assertTrue(menu.clickMenuButton(p,3)&&!menu.clickMenuButton(p,3),"Take transfers the original only once");var saved=paper.saveWithFullMetadata(f.in.registryAccess());paper.loadWithComponents(saved,f.in.registryAccess());paper.seed(p.getUUID(),i);
                h.assertTrue(paper.original().isEmpty()&&!f.in.getBlockState(at).getValue(SiloPaperBlock.PRESENT),"native reload and attempted seeding leave the taken mount empty");
            }
            h.assertTrue(p.getInventory().countItem(Items.WRITTEN_BOOK)==3&&peer.getInventory().countItem(Items.WRITTEN_BOOK)==0,"exactly the three original books exist in the owner's inventory");
        }).thenSucceed();
    }

    @GameTest(template="empty",batch="silo_source",timeoutTicks=5000)
    public static void actualPrivateEntryRetreatRadioRoofAndGrainConcludeOnlyTheConfirmedReadersReturn(GameTestHelper h){
        var f=fixture(h);var p=NativeTestPlayers.survival(h,"silo_reader");var peer=NativeTestPlayers.survival(h,"silo_peer");f.players.add(p);f.players.add(peer);var from=f.origin.offset(0,0,-6);f.chunks.hold(f.in,new AABB(from).inflate(5));BlockPos[] b={null};ItemStack[] original={null};int[] paused={0};Vec3[] peerAt={null};boolean[] ticking={false};
        h.onEachTick(()->{if(ticking[0]&&LabyrinthDoors.isBusy(p))transition(p);});
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the real House source is loaded")).thenExecute(()->{
            for(var at:BlockPos.betweenClosed(from.offset(-3,-1,-2),from.offset(3,-1,5)))f.in.setBlock(at,Blocks.STONE.defaultBlockState(),18);
            NovelRooms.door(f.in,from,Direction.SOUTH,Blocks.DARK_OAK_DOOR,false);f.data().putDoor(new LabyrinthData.Door("native/silo/source",f.in.dimension(),from,Direction.SOUTH,SiloSites.SOURCE,false));
            p.teleportTo(f.in,from.getX()+.1,from.getY(),from.getZ()+3,180,0);peer.teleportTo(f.in,from.getX()+.9,from.getY(),from.getZ()+3,180,0);p.connection.resetPosition();peer.connection.resetPosition();peerAt[0]=peer.position();
            SiloSites.prepareEntry(p);SiloSites.prepareEntry(peer);h.assertTrue(!SiloSites.base(p.server,p.getUUID()).equals(SiloSites.base(p.server,peer.getUUID())),"readers receive distinct physical silos");ticking[0]=true;
        }).thenWaitUntil(()->{boolean ready=SiloSites.prepareEntry(p)!=null;if(!ready)java.util.concurrent.locks.LockSupport.parkNanos(2_000_000L);h.assertTrue(ready,"the first native silo is completely built");})
          .thenExecute(()->{b[0]=SiloSites.base(p.server,p.getUUID());original[0]=((SiloPaperBlock.Paper)f.out.getBlockEntity(b[0].offset(SiloRooms.PAPERS.get(0)))).original();export(f.out,b[0]);click(h,p,from);})
          .thenWaitUntil(()->h.assertTrue(SiloSites.inside(p)&&!LabyrinthDoors.isBusy(p),"the original source produces a confirmed private native arrival"))
          .thenWaitUntil(()->h.assertTrue(SiloSites.state(p.server,p.getUUID()).getInt("SpeechRemaining")>20,"the actual first radio sentence has started"))
          .thenIdle(2)
          .thenExecute(()->{
              h.assertTrue(NativeTestPlayers.packets(p).stream().anyMatch(packet->packet instanceof net.minecraft.network.protocol.game.ClientboundSoundPacket sound&&sound.getSound().value()==SiloRegistry.RADIO_TUCSON.get()),"the father's original radio is sent to its actual reader through a native sound packet");
              h.assertTrue(NativeTestPlayers.packets(peer).stream().noneMatch(packet->packet instanceof net.minecraft.network.protocol.game.ClientboundSoundPacket sound&&sound.getSound().value()==SiloRegistry.RADIO_TUCSON.get()),"another reader outside the bin receives no private radio");
              h.assertTrue(!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.SILO),"arrival, radio and original papers alone confer no ending");var door=SiloSites.entry(p);p.moveTo(door.lower.getX()+.5,door.lower.getY(),door.lower.getZ()-1.5);LabyrinthDoors.tickPlayer(p,f.origin);p.gameMode.useItemOn(p,f.out,ItemStack.EMPTY,InteractionHand.MAIN_HAND,new BlockHitResult(door.lower.getCenter(),Direction.NORTH,door.lower,false));p.move(MoverType.SELF,new Vec3(0,0,3));LabyrinthDoors.tickPlayer(p,f.origin);
          }).thenWaitUntil(()->h.assertTrue(p.serverLevel()==f.in&&!LabyrinthDoors.isBusy(p),"ordinary physical retreat returns without completing the vignette"))
          .thenExecute(()->paused[0]=SiloSites.state(p.server,p.getUUID()).getInt("SpeechRemaining"))
          .thenIdle(12).thenExecute(()->{
              h.assertTrue(SiloSites.state(p.server,p.getUUID()).getInt("SpeechRemaining")==paused[0],"an unfinished radio sentence pauses outside the actual silo");p.moveTo(from.getX()+.5,from.getY(),from.getZ()+2);p.connection.resetPosition();click(h,p,from);
          }).thenWaitUntil(()->h.assertTrue(SiloSites.inside(p)&&!LabyrinthDoors.isBusy(p),"re-entry uses the same preserved silo"))
          .thenWaitUntil(()->h.assertTrue(SiloSites.state(p.server,p.getUUID()).getInt("Sentences")>0,"the audible native sentence finishes before its decay consequence"))
          .thenExecute(()->{
              var first=SiloRooms.BOARDS.stream().filter(x->f.out.getBlockEntity(b[0].offset(x.at())) instanceof SiloTimberBlock.Timber).findFirst().orElseThrow();var timber=(SiloTimberBlock.Timber)f.out.getBlockEntity(b[0].offset(first.at()));h.assertTrue(timber.rot>0,"completing the actual radio cue increases the original timber's rot");
              var edit=b[0].offset(5,3,6);f.out.setBlock(edit,Blocks.GOLD_BLOCK.defaultBlockState(),18);f.reload();h.assertTrue(f.out.getBlockState(edit).is(Blocks.GOLD_BLOCK)&&ItemStack.matches(original[0],((SiloPaperBlock.Paper)f.out.getBlockEntity(b[0].offset(SiloRooms.PAPERS.get(0)))).original()),"native saved-data reload preserves edits and the same finite paper");
              for(int y=0;y<=61;y++)h.assertTrue(f.out.getBlockState(b[0].offset(0,y,-9)).is(SiloRegistry.LADDER.get()),"the real maintenance ladder stays continuous through every level, including the roof opening");
              p.teleportTo(f.out,b[0].getX()+.5,b[0].getY()+62,b[0].getZ()-7.5,180,-90);p.setDeltaMovement(Vec3.ZERO);p.connection.resetPosition();click(h,p,b[0].offset(SiloRooms.HATCH));
          }).thenWaitUntil(()->h.assertTrue(SiloSites.state(p.server,p.getUUID()).getBoolean("PaintedSeen"),"an actual native sight ray through the opened hatch hits painted metal"))
          .thenExecute(()->{
              export(f.out,b[0],"roof-open");
              h.assertTrue(!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.SILO)&&!WitnessAccount.has(f.data(),peer.getUUID(),WitnessAccount.Story.SILO),"the roof and another reader's presence do not substitute for returning to the grain");
              p.moveTo(b[0].getX()+2.5,b[0].getY()+58,b[0].getZ()-7.5);p.setDeltaMovement(Vec3.ZERO);p.move(MoverType.SELF,new Vec3(0,-60,0));p.connection.resetPosition();
          }).thenWaitUntil(()->h.assertTrue(SiloSites.state(p.server,p.getUUID()).getBoolean("Resolved"),"the actual grain contact and confirmed native House return conclude this reader"))
          .thenExecute(()->{
              h.assertTrue(p.serverLevel()==f.in&&f.data().returnDepth(p.getUUID())==0&&WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.SILO),"only the completed actual landing consumes the saved return and grants the memory");h.assertTrue(!WitnessAccount.has(f.data(),peer.getUUID(),WitnessAccount.Story.SILO)&&peer.position().distanceToSqr(peerAt[0])<.05,"the other reader and their own ending remain independent");
              h.assertTrue(p.getHealth()==20&&SiloSites.canDeal(f.data(),peer.getUUID())&&!SiloSites.canDeal(f.data(),p.getUUID()),"the ending charges no health and only retires the actual reader's source");
          }).thenSucceed();
    }
}
