package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.List;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

/** Real hands on the loft's wheels, cleat and platform, including another reader and an animal in the machinery. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration") @PrefixGameTestTemplate(false)
public final class TensionSafetyTests {
    private static LiteraryTests.Fixture active;
    @AfterBatch(batch="tension_safety") public static void done(ServerLevel ignored){if(active!=null){active.close();active=null;}TensionRooms.clearAll();}
    private static void press(ServerLevel l,ServerPlayer p,BlockPos at){
        hAssert(l.getBlockState(at).useWithoutItem(l,p,new BlockHitResult(at.getCenter(),Direction.EAST,at,false)).consumesAction());
    }
    private static void hAssert(boolean consumed){if(!consumed)throw new IllegalStateException("The native fitting did not consume the hand interaction");}
    private static void stand(ServerPlayer p,ServerLevel l,BlockPos b,double x,double y,double z){p.teleportTo(l,b.getX()+x,b.getY()+y,b.getZ()+z,0,0);p.setDeltaMovement(Vec3.ZERO);p.setNoGravity(true);}

    @GameTest(template="empty",batch="tension_safety",timeoutTicks=900)
    public static void theCableIsReversibleAndItsPlatformCarriesLivingRidersWithoutCrushingThem(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=TensionRooms.EARLY;var b=f.base(place);var l=f.in;
        var bounds=LabyrinthPlaces.slotBounds(f.origin,place);
        f.chunks.hold(l,new AABB(bounds.minX(),bounds.minY(),bounds.minZ(),bounds.maxX()+1,bounds.maxY()+1,bounds.maxZ()+1));
        var p=NativeTestPlayers.survival(h,"tension_turner");var peer=NativeTestPlayers.survival(h,"tension_rider");f.players.addAll(List.of(p,peer));
        var wheel=b.offset(TensionRooms.ENTRY_WHEEL);var cleat=b.offset(TensionRooms.CLEAT_AT);var far=b.offset(TensionRooms.FAR_WHEEL);
        var bird=EntityType.CHICKEN.create(l);bird.setPersistenceRequired();bird.setNoAi(true);bird.setNoGravity(true);f.extras.add(bird);
        var pig=EntityType.PIG.create(l);pig.setPersistenceRequired();pig.setNoAi(true);pig.setNoGravity(true);f.extras.add(pig);
        var seq=h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the loft and its native entity sections are resident"))
            .thenExecute(()->{
                TensionRooms.build(l,b);LabyrinthBuilder.registerDoors(f.data(),place,b);
                stand(p,l,b,-3.5,0,-2.5);stand(peer,l,b,-4.5,1,-6.5);
                h.assertTrue(!PuzzleRooms.solved(f.data(),place)&&TensionRooms.weight(l,b)==0&&!TensionRooms.platformUp(l,b),"the weight is down and the platform locked");
                press(l,p,wheel);
            }).thenIdle(5).thenExecute(()->{
                press(l,p,wheel);
                h.assertTrue(TensionRooms.weight(l,b)==0&&l.getBlockState(wheel).getValue(TensionBlock.STAGE)==0,"the occupied shaft prevents the turn without partially winding the wheel");
                stand(peer,l,b,2.5,0,-4.5);
            }).thenIdle(5).thenExecute(()->{press(l,p,wheel);h.assertTrue(TensionRooms.weight(l,b)==1,"a clear shaft lets the weight rise");p.setShiftKeyDown(true);})
            .thenIdle(5).thenExecute(()->{press(l,p,wheel);h.assertTrue(TensionRooms.weight(l,b)==0,"crouching pays out again");p.setShiftKeyDown(false);});
        for(int height=1;height<=TensionRooms.MARK;height++){
            final int expected=height;
            seq.thenIdle(5).thenExecute(()->{press(l,p,wheel);h.assertTrue(TensionRooms.weight(l,b)==expected,"each turn raises the weight one marked step");});
        }
        seq.thenIdle(5).thenExecute(()->{
                stand(p,l,b,-3.5,0,-8.5);press(l,p,cleat);press(l,p,cleat);
                h.assertTrue(TensionRooms.fast(l,b),"the cleat holds the raised weight");
                stand(p,l,b,-3.5,0,-11.5);press(l,p,far);
            }).thenIdle(5).thenExecute(()->{
                press(l,p,far);h.assertTrue(TensionRooms.pin(l,b)==1,"the first turn draws the pin halfway");
                bird.moveTo(b.getX()-4.5,b.getY()+3,b.getZ()-6.5);h.assertTrue(l.addFreshEntity(bird),"the persistent obstruction animal is admitted to the House");
            }).thenWaitUntil(()->h.assertTrue(l.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,new AABB(b.offset(TensionRooms.SHAFT.above(3)))).contains(bird),"the animal occupies the shaft's native entity lookup"))
            .thenExecute(()->{
                press(l,p,cleat);
                h.assertTrue(TensionRooms.fast(l,b)&&TensionRooms.weight(l,b)==4&&TensionRooms.pin(l,b)==1,"an animal below the weight prevents casting off without losing any cable state: weight="+TensionRooms.weight(l,b)+", pin="+TensionRooms.pin(l,b)+", cleat="+TensionRooms.fast(l,b));
                bird.moveTo(b.getX()+3.5,b.getY(),b.getZ()-4.5);
                stand(peer,l,b,.5,TensionRooms.DOWN_Y+1,-12.5);
                pig.moveTo(b.getX()-.5,b.getY()+TensionRooms.DOWN_Y+1,b.getZ()-13.5);h.assertTrue(l.addFreshEntity(pig),"the persistent rider is admitted to the House");
                l.setBlock(b.offset(1,-1,-14),Blocks.STONE.defaultBlockState(),2);
            }).thenWaitUntil(()->h.assertTrue(l.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,new AABB(b.offset(-1,-3,-14),b.offset(2,1,-12))).contains(pig),"the platform's animal occupies its native entity lookup"))
            .thenIdle(5).thenExecute(()->{
                press(l,p,far);
                h.assertTrue(!TensionRooms.platformUp(l,b)&&TensionRooms.drum(l,b)==1&&l.getBlockState(b.offset(1,-1,-14)).is(Blocks.STONE),"a solid obstruction in the lift's path is neither overwritten nor passed through");
                l.setBlock(b.offset(1,-1,-14),Blocks.AIR.defaultBlockState(),2);
                l.setBlock(b.offset(0,1,-13),Blocks.STONE.defaultBlockState(),2);
            }).thenIdle(5).thenExecute(()->{
                press(l,p,far);
                h.assertTrue(!TensionRooms.platformUp(l,b)&&TensionRooms.drum(l,b)==1&&!PuzzleRooms.solved(f.data(),place),"blocked rider headroom holds the platform, pin and far drum unchanged");
                h.assertTrue(Math.abs(peer.getY()-(b.getY()+TensionRooms.DOWN_Y+1))<.01,"the rider was not moved into the obstruction");
                l.setBlock(b.offset(0,1,-13),Blocks.AIR.defaultBlockState(),2);
            }).thenIdle(5).thenExecute(()->{
                var id=pig.getUUID();press(l,p,far);
                h.assertTrue(TensionRooms.platformUp(l,b)&&PuzzleRooms.solved(f.data(),place),"with headroom the pin draws free and the platform rises");
                h.assertTrue(Math.abs(peer.getY()-b.getY())<.01&&Math.abs(pig.getY()-b.getY())<.01&&pig.getUUID().equals(id),"the same reader and living animal ride to floor level");
                h.assertTrue(peer.getHealth()==peer.getMaxHealth()&&pig.getHealth()==pig.getMaxHealth(),"the lift wounds neither rider");
                var loaded=LabyrinthData.FACTORY.deserializer().apply(f.data().save(new CompoundTag(),l.registryAccess()),l.registryAccess());
                f.in.getServer().overworld().getDataStorage().set("the_oldest_house_labyrinth",loaded);
                h.assertTrue(PuzzleRooms.solved(loaded,place)&&WitnessAccount.count(loaded,p.getUUID())==0,"the shared settled room persists through saved-data reload and grants no Witness");
                p.setShiftKeyDown(true);
            }).thenIdle(5).thenExecute(()->{
                press(l,p,far);h.assertTrue(TensionRooms.platformUp(l,b),"the caught platform stays up when cable is paid out after solving");
            }).thenSucceed();
    }
}
