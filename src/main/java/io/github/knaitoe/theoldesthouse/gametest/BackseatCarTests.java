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
}
