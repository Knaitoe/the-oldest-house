package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.*;

/** Actual fitted collision, riding, block packets, saved originals and independent owners. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_backseat") @PrefixGameTestTemplate(false)
public final class BackseatCarTests {
    private static LiteraryTests.Fixture active;
    private record CarFixture(LiteraryTests.Fixture f,BlockPos at,ServerPlayer p,ServerPlayer peer){
        BackseatCarBlock.Car car(){return (BackseatCarBlock.Car)f.in.getBlockEntity(at);}
        void place(GameTestHelper h){
            for(int x=-5;x<=2;x++)for(int z=-4;z<=4;z++)f.in.setBlock(at.offset(x,-1,z),Blocks.STONE.defaultBlockState(),18);
            h.assertTrue(BackseatCarBlock.place(f.in,at,p.getUUID()),"the fresh car is fitted to actual loaded cells: "+java.util.stream.IntStream.range(0,BackseatCarCells.COUNT).mapToObj(i->at.offset(BackseatCarCells.offset(i))).filter(pos->!f.in.hasChunkAt(pos)||!f.in.getBlockState(pos).isAir()).limit(4).map(pos->pos+":"+f.in.hasChunkAt(pos)+":"+f.in.getBlockState(pos)).toList()+" root="+f.in.getBlockEntity(at));
            for(var who:new ServerPlayer[]{p,peer}){who.teleportTo(f.in,at.getX()-2.3,at.getY(),at.getZ()+1,180,0);who.setNoGravity(true);who.setDeltaMovement(Vec3.ZERO);who.connection.resetPosition();}
        }
        void click(GameTestHelper h,Vec3 point){
            var hit=new BlockHitResult(point,Direction.WEST,BlockPos.containing(point),false);
            var e=new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,hit.getBlockPos(),hit);NeoForge.EVENT_BUS.post(e);
            h.assertTrue(e.isCanceled()&&e.getCancellationResult().consumesAction(),"the actual native car block interaction is handled: phase="+car().phase+" rider="+p.getVehicle()+" cell="+f.in.getBlockState(hit.getBlockPos())+" at="+point+" result="+e.getCancellationResult());
        }
        Vec3 seatPoint(){return at.getCenter().add(-.625,.5,-.2);}
        Vec3 doorPoint(){return at.getCenter().add(-1.1875,.7,0);}
    }
    private static CarFixture fixture(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var at=f.origin.offset(0,40,-80);
        f.chunks.hold(f.in,new AABB(at.getX()-5,at.getY()-2,at.getZ()-5,at.getX()+4,at.getY()+5,at.getZ()+5));
        var p=NativeTestPlayers.survival(h,"car_driver");var peer=NativeTestPlayers.survival(h,"car_peer");f.players.add(p);f.players.add(peer);p.setNoGravity(true);peer.setNoGravity(true);
        return new CarFixture(f,at,p,peer);
    }
    private static void close(){if(active==null)return;var f=active;var at=f.origin.offset(0,40,-80);for(var p:f.players)if(p.getVehicle() instanceof BackseatSeat s&&s.car()!=null)s.car().phase=BackseatCarBlock.Car.PARKED;for(var e:f.in.getEntitiesOfClass(BackseatSeat.class,new AABB(f.origin.getX()-8,f.origin.getY()+35,f.origin.getZ()-88,f.origin.getX()+8,f.origin.getY()+50,f.origin.getZ()-70)))e.discard();for(int x=-5;x<=2;x++)for(int z=-4;z<=4;z++)for(int y=-1;y<=2;y++)f.in.setBlock(at.offset(x,y,z),Blocks.AIR.defaultBlockState(),18);f.close();active=null;}
    @AfterBatch(batch="backseat_controls") public static void controlsDone(ServerLevel ignored){close();}
    @GameTest(template="empty",batch="backseat_controls",timeoutTicks=300)
    public static void actualSeatDoorLookAndReloadKeepOriginalDriverAndBelongings(GameTestHelper h){
        var f=fixture(h);final ItemStack original=new ItemStack(Items.CLOCK);CustomData.update(DataComponents.CUSTOM_DATA,original,t->t.putString("Original","unchanged driver clock"));
        final java.util.UUID[] seatId={null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.f.chunks.ready(),"the car's native entity sections are ready"))
        .thenExecute(()->{
            f.place(h);f.p.getInventory().setItem(4,original);f.p.setYRot(180);f.p.setXRot(0);
            h.assertTrue(!f.car().board(f.peer),"a peer cannot take another reader's seat");
            f.peer.teleportTo(f.f.in,f.at.getX()-4,f.at.getY(),f.at.getZ()+3,0,0);
            f.click(h,f.seatPoint());h.assertTrue(f.p.getVehicle() instanceof BackseatSeat&&f.car().phase==BackseatCarBlock.Car.SEATED,"the original driver rides the physical seat before commitment");
            seatId[0]=f.p.getVehicle().getUUID();
            h.assertTrue(f.p.position().distanceToSqr(f.at.getCenter().add(BackseatCarMesh.DRIVER).add(0,-.5,0))<.0001,"the native riding position puts the hips on the cushion");
            f.p.stopRiding();h.assertTrue(!f.p.isPassenger(),"the reader can stand back up while the door is open");
            f.p.moveTo(f.at.getX()-2.3,f.at.getY(),f.at.getZ()+1);f.click(h,f.seatPoint());
            h.assertTrue(f.p.getVehicle().getUUID().equals(seatId[0]),"boarding again reuses the original saved anchor");
            f.click(h,f.doorPoint());h.assertTrue(f.car().phase==BackseatCarBlock.Car.CLOSING,"a physical second click closes the door");
        })
        .thenWaitUntil(()->h.assertTrue(f.car().phase==BackseatCarBlock.Car.LOCKED,"the actual leaf has finished closing"))
        .thenIdle(2) // The native connection flushes this tick's sound batch after the block-entity tick.
        .thenExecute(()->{
            for(int i=0;i<BackseatCarCells.COUNT;i++)h.assertTrue(!f.f.in.getBlockState(f.at.offset(BackseatCarCells.offset(i))).getValue(BackseatCarBlock.OPEN),"all actual cell collision belongs to the same closed leaf");
            var packets=NativeTestPlayers.packets(f.p);
            h.assertTrue(packets.stream().anyMatch(packet->packet instanceof net.minecraft.network.protocol.game.ClientboundStopSoundPacket stop&&stop.getName()!=null&&stop.getName().equals(BackseatRegistry.RADIO.getId())),"the actual door-close packet cuts the driver's radio: "+packets.stream().map(packet->packet.getClass().getSimpleName()+(packet instanceof net.minecraft.network.protocol.game.ClientboundStopSoundPacket stop?":"+stop.getName():"")).toList());
            h.assertTrue(packets.stream().anyMatch(packet->packet instanceof net.minecraft.network.protocol.game.ClientboundSoundPacket sound&&sound.getSound().value()==BackseatRegistry.LATCH.get()),"the fitted door has its own original latch cue");
            h.assertTrue(packets.stream().anyMatch(packet->packet instanceof net.minecraft.network.protocol.game.ClientboundStopSoundPacket stop&&stop.getSource()==net.minecraft.sounds.SoundSource.WEATHER),"locking cuts the driver's actual already-playing rain audio");
            h.assertTrue(NativeTestPlayers.sounds(f.peer).stream().noneMatch(sound->sound.getSound().value().getLocation().getPath().startsWith("backseat.")),"the peer hears none of the driver's private car cues");
            f.p.stopRiding();h.assertTrue(f.p.isPassenger(),"the native dismount event keeps this committed driver in the car");
            var saved=f.car().saveWithoutMetadata(f.f.in.registryAccess());f.car().loadWithComponents(saved,f.f.in.registryAccess());
            h.assertTrue(f.car().seatId.equals(seatId[0])&&f.car().owner.equals(f.p.getUUID())&&f.car().phase==BackseatCarBlock.Car.LOCKED,"native BE reload retains the exact original and locked state");
        }).thenIdle(80)
        .thenExecute(()->{
            h.assertTrue(f.car().phase==BackseatCarBlock.Car.LOCKED,"there is no countdown reveal while the driver keeps looking forward");
            f.p.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,f.at.getCenter().add(BackseatCarMesh.MIRROR).add(0,-.5,0));
        }).thenWaitUntil(()->h.assertTrue(f.car().phase==BackseatCarBlock.Car.REVEAL,"a real camera turn toward the mirror triggers immediately"))
        .thenExecute(()->{
            h.assertTrue(ItemStack.matches(original,f.p.getInventory().getItem(4))&&f.p.getInventory().getItem(4)==original,"seat, locks and reload preserve the actual carried stack and components");
            h.assertTrue(!f.peer.isPassenger()&&f.peer.getHealth()==f.peer.getMaxHealth(),"the peer inherits no seat, lock or consequence");
            h.assertTrue(f.p.getHealth()==f.p.getMaxHealth(),"the seated eye stays clear of the roof and takes no suffocation damage");
        }).thenSucceed();
    }
    @AfterBatch(batch="backseat_collision") public static void collisionDone(ServerLevel ignored){close();}
    @GameTest(template="empty",batch="backseat_collision",timeoutTicks=260)
    public static void doorCollisionIsHollowAndNeverClosesThroughAnotherBodyOrRestocksAnEdit(GameTestHelper h){
        var f=fixture(h);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.f.chunks.ready(),"the native car cells are ready"))
        .thenExecute(()->{
            f.place(h);
            var eye=new Vec3(f.at.getX()-.125,f.at.getY()+1.87,f.at.getZ()+.31);
            h.assertTrue(f.f.in.noCollision(null,new AABB(eye.x-.05,eye.y-.05,eye.z-.05,eye.x+.05,eye.y+.05,eye.z+.05)),"the actual driver's eye has a hollow cabin");
            var hood=new AABB(f.at.getX(),f.at.getY()+.8,f.at.getZ()-2.1,f.at.getX()+.1,f.at.getY()+1,f.at.getZ()-2);
            h.assertTrue(!f.f.in.noCollision(null,hood),"the rendered hood is actual collision, not a walk-through image");
            f.peer.moveTo(f.at.getX()-.72,f.at.getY()+.65,f.at.getZ()+.5);
            f.click(h,f.seatPoint());f.click(h,f.doorPoint());
        }).thenIdle(12).thenExecute(()->{
            h.assertTrue(f.car().phase==BackseatCarBlock.Car.SEATED&&f.f.in.getBlockState(f.at).getValue(BackseatCarBlock.OPEN),"another living body in the leaf keeps the car open and uncommitted");
            f.peer.moveTo(f.at.getX()-4,f.at.getY(),f.at.getZ()+3);f.click(h,f.doorPoint());
        }).thenWaitUntil(()->h.assertTrue(f.car().phase==BackseatCarBlock.Car.LOCKED,"the leaf closes when the actual body is clear"))
        .thenExecute(()->{
            var edited=f.at.offset(BackseatCarCells.offset(0));f.f.in.setBlock(edited,Blocks.GOLD_BLOCK.defaultBlockState(),18);
            h.assertTrue(BackseatCarBlock.place(f.f.in,f.at,f.p.getUUID())&&f.f.in.getBlockState(edited).is(Blocks.GOLD_BLOCK),"repeated preparation preserves the original and never restocks a later edit");
        }).thenSucceed();
    }
    @AfterBatch(batch="backseat_crash") public static void crashDone(ServerLevel ignored){
        if(active!=null){var at=active.origin.offset(0,40,-80);for(int x=-1;x<=1;x++)for(int y=0;y<=2;y++)active.in.setBlock(at.offset(x,y,-24),Blocks.AIR.defaultBlockState(),18);active.in.setBlock(at.offset(9,-1,0),Blocks.AIR.defaultBlockState(),18);}close();
    }
    @GameTest(template="empty",batch="backseat_crash",timeoutTicks=300)
    public static void actualLookDrivesOriginalSeatIntoNativeTreeAndReturnsOnlyItsReader(GameTestHelper h){
        var f=fixture(h);f.f.chunks.hold(f.f.in,new AABB(f.at.getX()-5,f.at.getY()-2,f.at.getZ()-32,f.at.getX()+12,f.at.getY()+5,f.at.getZ()+5));
        var original=new ItemStack(Items.COMPASS);CustomData.update(DataComponents.CUSTOM_DATA,original,t->t.putString("Original","before the crash"));
        var home=new LabyrinthData.Waypoint(f.f.in.dimension(),Vec3.atBottomCenterOf(f.at.offset(9,0,0)),90,false);
        var peerReturn=new LabyrinthData.Waypoint(f.f.in.dimension(),Vec3.atBottomCenterOf(f.at.offset(10,0,0)),0,false);
        final java.util.UUID[] originalSeat={null};final double[] moved={0};final long[] impactAt={0};
        final var heard=new java.util.HashSet<net.minecraft.sounds.SoundEvent>();
        final var volumes=new java.util.HashMap<net.minecraft.sounds.SoundEvent,Float>();
        h.onEachTick(()->NativeTestPlayers.sounds(f.p).forEach(sound->{heard.add(sound.getSound().value());volumes.merge(sound.getSound().value(),sound.getVolume(),Math::max);}));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.f.chunks.ready(),"the native track, obstacle and House return are loaded"))
        .thenExecute(()->{
            f.place(h);f.peer.moveTo(f.at.getX()-4,f.at.getY(),f.at.getZ()+3);f.p.getInventory().setItem(4,original);
            h.assertTrue(!f.car().arm(f.peer),"a peer cannot arm the original car");
            h.assertTrue(!f.car().arm(f.p),"the crash cannot consume a reader without an actual saved return");
            var data=LabyrinthData.get(f.p.server);data.pushReturn(f.p.getUUID(),home);data.pushReturn(f.peer.getUUID(),peerReturn);
            f.f.in.setBlock(f.at.offset(9,-1,0),Blocks.STONE.defaultBlockState(),18);
            for(int x=-1;x<=1;x++)for(int y=0;y<=2;y++)f.f.in.setBlock(f.at.offset(x,y,-24),Blocks.SPRUCE_LOG.defaultBlockState(),18);
            h.assertTrue(f.car().arm(f.p),"the original driver has a real House landing");f.click(h,f.seatPoint());originalSeat[0]=f.car().seatId;f.click(h,f.doorPoint());
        }).thenWaitUntil(()->h.assertTrue(f.car().phase==BackseatCarBlock.Car.LOCKED,"the actual driver has closed the car"))
        .thenIdle(20).thenExecute(()->{
            h.assertTrue(f.car().travel==0,"a forward-looking reader is never driven by a hidden countdown");
            f.p.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,f.at.getCenter().add(BackseatCarMesh.MIRROR).add(0,-.5,0));
        }).thenWaitUntil(()->h.assertTrue(f.car().travel>=BackseatCarBlock.Car.SPEED*2,"the physical seat and passenger actually move down the road"))
        .thenExecute(()->{
            moved[0]=f.car().travel;var seat=f.car().seat();h.assertTrue(seat!=null&&seat.getUUID().equals(originalSeat[0]),"the moving car retains its original seat");
            h.assertTrue(f.car().clock>20&&BackseatCarBlock.Car.speed(f.car().clock)<BackseatCarBlock.Car.SPEED,"the original car builds physical speed over several seconds instead of jumping to eighty mph");
            h.assertTrue(Math.abs(seat.getZ()-(f.at.getZ()+.5-f.car().travel))<.0001&&Math.abs(f.p.getZ()-(seat.getZ()+BackseatCarMesh.DRIVER.z))<.0001,"native passenger position follows the eighty-mph anchor");
            var saved=f.car().saveWithoutMetadata(f.f.in.registryAccess());int clock=f.car().clock;f.car().loadWithComponents(saved,f.f.in.registryAccess());h.assertTrue(f.car().sequenceEnabled&&f.car().travel==moved[0]&&f.car().clock==clock&&f.car().seatId.equals(originalSeat[0]),"native reload retains the original, travelled distance and acceleration progress");
            f.p.stopRiding();h.assertTrue(f.p.isPassenger(),"the moving car remains committed");
        }).thenWaitUntil(()->h.assertTrue(f.car().phase==BackseatCarBlock.Car.IMPACT,"the actual spruce collision causes the impact"))
        .thenExecute(()->{
            h.assertTrue(f.car().travel>moved[0]&&f.car().travel<22,"the car hits the physical tree before its driver passes it");
            h.assertTrue(LabyrinthData.get(f.p.server).peekReturn(f.p.getUUID()).equals(home),"the return is unspent during impact");
            impactAt[0]=f.f.in.getGameTime();
        }).thenIdle(20).thenExecute(()->{
            h.assertTrue(f.car().phase==BackseatCarBlock.Car.IMPACT&&f.p.getVehicle()==f.car().seat(),"the impact holds the actual cabin long enough for the crash to sound before dimension return");
            h.assertTrue(volumes.getOrDefault(BackseatRegistry.IMPACT.get(),0F)>=2,"the native crash packet requests the heavy impact level");
        }).thenWaitUntil(()->h.assertTrue(f.car().phase==BackseatCarBlock.Car.DONE,"only the confirmed native House arrival concludes this car sequence"))
        .thenIdle(2).thenExecute(()->{
            h.assertTrue(f.f.in.getGameTime()-impactAt[0]>=BackseatCarBlock.Car.IMPACT_HOLD_TICKS,"the collision's original audible hold completes before its return is consumed");
            h.assertTrue(f.p.position().distanceToSqr(home.pos())<.01&&!f.p.isPassenger(),"the driver actually lands at their original House return");
            h.assertTrue(LabyrinthData.get(f.p.server).peekReturn(f.p.getUUID())==null&&LabyrinthData.get(f.p.server).peekReturn(f.peer.getUUID()).equals(peerReturn),"only the successful driver's return is consumed");
            h.assertTrue(f.p.getInventory().getItem(4)==original&&f.p.getHealth()==f.p.getMaxHealth(),"the real carried stack, components and health survive the cut");
            for(var cue:new net.minecraft.sounds.SoundEvent[]{BackseatRegistry.RISE.get(),BackseatRegistry.GLASS.get(),BackseatRegistry.IMPACT.get()})h.assertTrue(heard.contains(cue),"the native sequence emits its own private "+cue.getLocation());
            h.assertTrue(NativeTestPlayers.sounds(f.peer).stream().noneMatch(packet->packet.getSound().value().getLocation().getPath().startsWith("backseat."))&&!f.peer.isPassenger(),"the independent reader receives no private scare or movement");
        }).thenSucceed();
    }
    @AfterBatch(batch="backseat_notebook") public static void notebookDone(ServerLevel ignored){close();}
    @GameTest(template="empty",batch="backseat_notebook",timeoutTicks=260)
    public static void passengerNotebookIsOptionalAndTransfersItsExactFiniteOriginalOnce(GameTestHelper h){
        var f=fixture(h);final ItemStack[] original={null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.f.chunks.ready(),"the native passenger cushion is loaded"))
        .thenExecute(()->{
            f.place(h);f.peer.moveTo(f.at.getX()-4,f.at.getY(),f.at.getZ()+3);
            f.car().notebook=BackseatNotebook.original(f.p.getUUID());f.car().notebookCreated=true;original[0]=f.car().notebook;f.car().sync();
            f.click(h,f.seatPoint());f.click(h,f.at.getCenter().add(.55,.53,0));
            h.assertTrue(f.p.containerMenu instanceof BackseatNotebook.Pages,"the physical passenger-side book opens its native private pages");
            var menu=f.p.containerMenu;h.assertTrue(!menu.clickMenuButton(f.peer,3),"a peer cannot take the driver's original");
            menu.clicked(0,0,net.minecraft.world.inventory.ClickType.PICKUP,f.p);h.assertTrue(f.p.containerMenu.getCarried().isEmpty(),"the displayed reading copy cannot be pulled into the cursor");
            h.assertTrue(menu.clickMenuButton(f.p,3),"taking transfers the actual original");
            h.assertTrue(f.car().notebook.isEmpty()&&java.util.stream.IntStream.range(0,f.p.getInventory().getContainerSize()).anyMatch(i->f.p.getInventory().getItem(i)==original[0]),"the original stack moves from the car into the actual inventory");
            h.assertTrue(!menu.clickMenuButton(f.p,3),"a repeated take cannot copy the book");
            var saved=f.car().saveWithoutMetadata(f.f.in.registryAccess());f.car().loadWithComponents(saved,f.f.in.registryAccess());
            h.assertTrue(f.car().notebookCreated&&f.car().notebook.isEmpty(),"native reload remembers that this original has left");
            h.assertTrue(BackseatCarBlock.place(f.f.in,f.at,f.p.getUUID())&&f.car().notebook.isEmpty(),"repeated preparation never restocks the removed notebook");
            f.click(h,f.doorPoint());
        }).thenWaitUntil(()->h.assertTrue(f.car().phase==BackseatCarBlock.Car.LOCKED,"taking or skipping pages introduces no reading gate before the real lock"))
        .thenSucceed();
    }
    private static void roadClick(GameTestHelper h,ServerPlayer p,Vec3 point){
        var hit=new BlockHitResult(point,Direction.WEST,BlockPos.containing(point),false);var event=new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,hit.getBlockPos(),hit);NeoForge.EVENT_BUS.post(event);h.assertTrue(event.isCanceled()&&event.getCancellationResult().consumesAction(),"the actual road car handles its native block input");
    }
    private static void roadExport(ServerLevel level,BlockPos base){try{
        var file=new com.google.gson.JsonObject();var palette=new com.google.gson.JsonArray();var blocks=new com.google.gson.JsonArray();var lookup=new java.util.LinkedHashMap<net.minecraft.world.level.block.state.BlockState,Integer>();
        for(var at:BlockPos.betweenClosed(base.offset(-14,-1,-58),base.offset(14,17,4))){
            var state=level.getBlockState(at);if(state.isAir()||state.is(BackseatRegistry.CAR.get()))continue;
            Integer id=lookup.get(state);if(id==null){id=lookup.size();lookup.put(state,id);palette.add(net.minecraft.world.level.block.state.BlockState.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE,state).getOrThrow());}
            var row=new com.google.gson.JsonArray();row.add(at.getX()-base.getX());row.add(at.getY()-base.getY());row.add(at.getZ()-base.getZ());row.add(id);blocks.add(row);
        }
        file.add("palette",palette);file.add("blocks",blocks);var folder=java.nio.file.Path.of(System.getProperty("the_oldest_house.backseatRoadExportDir","../build/backseat-road"));java.nio.file.Files.createDirectories(folder);java.nio.file.Files.writeString(folder.resolve("road.json"),new com.google.gson.Gson().toJson(file));
    }catch(Exception e){throw new IllegalStateException("Cannot export the actual private Backseat road",e);}}
    @AfterBatch(batch="backseat_road") public static void roadDone(ServerLevel ignored){BackseatSites.clear();close();}
    @GameTest(template="empty",batch="backseat_road",timeoutTicks=3000)
    public static void freshPrivateRoadsKeepOriginalCarsBooksEditsAndReturnOnlyTheActualDriver(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;BackseatSites.clear();var p=NativeTestPlayers.survival(h,"road_driver");var peer=NativeTestPlayers.survival(h,"road_peer");f.players.add(p);f.players.add(peer);
        var from=f.origin.offset(0,0,-6);f.chunks.hold(f.in,new AABB(from).inflate(5));
        final BackseatCarBlock.Car[] original={null};final ItemStack[] notebook={null};final Vec3[] peerAt={null};final BlockPos[] base={null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the actual House source door is loaded"))
        .thenExecute(()->{
            for(var at:BlockPos.betweenClosed(from.offset(-3,-1,-2),from.offset(3,-1,5)))f.in.setBlock(at,Blocks.STONE.defaultBlockState(),18);
            NovelRooms.door(f.in,from,Direction.SOUTH,Blocks.DARK_OAK_DOOR,false);f.data().putDoor(new LabyrinthData.Door("native/backseat/source",f.in.dimension(),from,Direction.SOUTH,LabyrinthData.DEALT,false));f.data().deal(p.getUUID(),f.data().door("native/backseat/source"),LabyrinthPlace.BACKSEAT.id(),false);h.assertTrue(LabyrinthDealer.vignettesAvailable(f.data(),p.getUUID()).contains(LabyrinthPlace.BACKSEAT),"public personal dealer includes Backseat");
            p.teleportTo(f.in,from.getX()+.1,from.getY(),from.getZ()+3,180,0);peer.teleportTo(f.in,from.getX()+.9,from.getY(),from.getZ()+3,180,0);p.setNoGravity(false);peer.setNoGravity(false);p.connection.resetPosition();peer.connection.resetPosition();
            h.assertTrue(BackseatSites.prepareEntry(p)==null&&BackseatSites.prepareEntry(peer)==null,"both actual readers queue independent first construction");
            h.assertTrue(!BackseatSites.base(p.server,p.getUUID()).equals(BackseatSites.base(p.server,peer.getUUID())),"the readers receive distinct physical roads");
        }).thenWaitUntil(()->{
            boolean ready=BackseatSites.prepareEntry(p)!=null;if(!ready)java.util.concurrent.locks.LockSupport.parkNanos(2_000_000L);h.assertTrue(ready,"bounded native construction completes the first road");
        }).thenExecute(()->{
            base[0]=BackseatSites.base(p.server,p.getUUID());original[0]=BackseatSites.car(p);notebook[0]=original[0].notebook;peerAt[0]=peer.position();
            h.assertTrue(original[0].road&&!notebook[0].isEmpty()&&BackseatSites.state(p.server,p.getUUID()).getBoolean("Built"),"one actual car and original notebook exist on the complete road");
            roadExport(f.out,base[0]);
            roadClick(h,p,from.getCenter());h.assertTrue(LabyrinthDoors.isBusy(p),"the actual House source block interaction starts native private entry");
        }).thenIdle(1).thenExecute(()->{
            // Embedded connections have no automatic real-client player tick; use the same
            // production pending-move tick as the existing native seam fixtures.
            io.github.knaitoe.theoldesthouse.house.HouseTransitionEvents.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p));
        }).thenWaitUntil(()->h.assertTrue(BackseatSites.inside(p)&&!LabyrinthDoors.isBusy(p),"the original player actually arrives on their own road"))
        .thenExecute(()->{
            var entry=BackseatSites.entry(p);p.moveTo(entry.lower.getX()+.5,entry.lower.getY(),entry.lower.getZ()-1.5);LabyrinthDoors.tickPlayer(p,f.origin);
            p.gameMode.useItemOn(p,f.out,ItemStack.EMPTY,InteractionHand.MAIN_HAND,new BlockHitResult(entry.lower.getCenter(),Direction.NORTH,entry.lower,false));h.assertTrue(f.out.getBlockState(entry.lower).getValue(net.minecraft.world.level.block.DoorBlock.OPEN),"the actual return door opens before commitment");p.move(net.minecraft.world.entity.MoverType.SELF,new Vec3(0,0,3));LabyrinthDoors.tickPlayer(p,f.origin);
            io.github.knaitoe.theoldesthouse.house.HouseTransitionEvents.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p));
        }).thenWaitUntil(()->h.assertTrue(p.serverLevel()==f.in&&!LabyrinthDoors.isBusy(p),"physical road retreat returns to the actual House source"))
        .thenExecute(()->{
            h.assertTrue(!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.BACKSEAT)&&BackseatSites.canDeal(f.data(),p.getUUID())&&BackseatSites.car(p)==original[0]&&original[0].notebook==notebook[0],"retreat preserves the same originals and grants no crash credit");p.moveTo(from.getX()+.5,from.getY(),from.getZ()+2);p.connection.resetPosition();roadClick(h,p,from.getCenter());io.github.knaitoe.theoldesthouse.house.HouseTransitionEvents.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p));
        }).thenWaitUntil(()->h.assertTrue(BackseatSites.inside(p)&&!LabyrinthDoors.isBusy(p),"the same dealt source returns to the original private road"))
        .thenWaitUntil(()->{
            boolean ready=BackseatSites.prepareEntry(peer)!=null;if(!ready)java.util.concurrent.locks.LockSupport.parkNanos(2_000_000L);h.assertTrue(ready,"the second reader's independent road is ready");
        }).thenExecute(()->{
            h.assertTrue(BackseatSites.car(peer)!=original[0]&&BackseatSites.car(peer).owner.equals(peer.getUUID()),"the peer has a different original car");
            var edit=base[0].offset(6,0,-10);f.out.setBlock(edit,Blocks.GOLD_BLOCK.defaultBlockState(),18);f.reload();
            h.assertTrue(BackseatSites.prepareEntry(p)!=null&&BackseatSites.car(p)==original[0]&&original[0].notebook==notebook[0]&&f.out.getBlockState(edit).is(Blocks.GOLD_BLOCK),"native saved-site reload preserves the original car, finite paper and player edit");
            for(int z=-3;z>=-14;z--)h.assertTrue(f.out.noCollision(p,new AABB(base[0].getX()-2.6,base[0].getY()+.01,base[0].getZ()+z-.3,base[0].getX()-2,base[0].getY()+1.8,base[0].getZ()+z+.3)),"the actual supported west approach stays clear of the car and trees");
            var at=original[0].getBlockPos();p.moveTo(at.getX()-2.3,at.getY(),at.getZ()+1);p.connection.resetPosition();
            roadClick(h,p,at.getCenter().add(-.625,.5,-.2));f.extras.add(original[0].seat());roadClick(h,p,at.getCenter().add(-1.1875,.7,0));
        }).thenWaitUntil(()->h.assertTrue(original[0].phase==BackseatCarBlock.Car.LOCKED,"the original car locks without any notebook-reading requirement"))
        .thenExecute(()->p.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,original[0].getBlockPos().getCenter().add(BackseatCarMesh.MIRROR).add(0,-.5,0)))
        .thenWaitUntil(()->h.assertTrue(original[0].phase==BackseatCarBlock.Car.DONE,"the actual native trees cause impact and the confirmed return"))
        .thenExecute(()->{
            h.assertTrue(p.serverLevel()==f.in&&p.position().distanceToSqr(Vec3.atBottomCenterOf(from.south()))<.01&&f.data().returnDepth(p.getUUID())==0,"only a completed native landing consumes the original House source return");
            h.assertTrue(BackseatSites.state(p.server,p.getUUID()).getBoolean("Resolved")&&!BackseatSites.state(peer.server,peer.getUUID()).getBoolean("Resolved"),"the crash concludes only its actual reader's road");h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.BACKSEAT)&&!WitnessAccount.has(f.data(),peer.getUUID(),WitnessAccount.Story.BACKSEAT)&&!LabyrinthDealer.vignettesAvailable(f.data(),p.getUUID()).contains(LabyrinthPlace.BACKSEAT)&&LabyrinthDealer.vignettesAvailable(f.data(),peer.getUUID()).contains(LabyrinthPlace.BACKSEAT),"only the actual survivor resolves this source while the peer keeps their own public doorway");
            h.assertTrue(peer.position().distanceToSqr(peerAt[0])<.05&&f.data().returnDepth(peer.getUUID())==0&&original[0].notebook==notebook[0],"the peer, their return stack and the unread original paper remain untouched");
            h.assertTrue(BackseatSites.prepareEntry(p)==null,"a concluded finite road is never rebuilt for the same reader");
        }).thenSucceed();
    }
}
