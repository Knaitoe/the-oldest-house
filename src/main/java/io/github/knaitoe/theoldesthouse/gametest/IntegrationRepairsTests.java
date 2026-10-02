package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import io.github.knaitoe.theoldesthouse.opening.CompanionOrders;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class IntegrationRepairsTests {
    private static final class Fixture implements AutoCloseable {
        final net.minecraft.server.MinecraftServer server;final HouseSavedData oldHouse;final LabyrinthData oldData;
        final BlockPos origin;final LabyrinthData data=new LabyrinthData();final List<Entity> entities=new ArrayList<>();final List<ServerPlayer> players=new ArrayList<>();
        Fixture(GameTestHelper h,int at){server=h.getLevel().getServer();origin=new BlockPos(at,80,at);oldHouse=HouseSavedData.get(server);oldData=LabyrinthData.get(server);
            var house=new HouseSavedData();house.markSpawned(origin);server.overworld().getDataStorage().set("the_oldest_house",house);data.setBuilt(LabyrinthBuilder.VERSION,origin);server.overworld().getDataStorage().set("the_oldest_house_labyrinth",data);}
        ServerLevel level(LabyrinthPlace p){return HouseTestLevel.get(server,NovelRooms.dimension(p));}
        ServerPlayer player(GameTestHelper h,ServerLevel l,Vec3 at){var p=NativeTestPlayers.survival(h,"integration");p.teleportTo(l,at.x,at.y,at.z,0,0);players.add(p);return p;}
        <E extends Entity>E keep(E e){entities.add(e);return e;}
        public void close(){for(var p:players){p.stopRiding();p.getInventory().clearContent();NativeTestPlayers.remove(p);}entities.forEach(Entity::discard);server.overworld().getDataStorage().set("the_oldest_house",oldHouse);server.overworld().getDataStorage().set("the_oldest_house_labyrinth",oldData);LabyrinthBuilder.clearAll();FinaleArchitecture.clearAll();}
    }
    @GameTest(template="empty",batch="integration_companions")
    public static void nativeWheelAndServerCommandsAcceptOnlyOwnedCatsAndDogs(GameTestHelper h){
        try(var f=new Fixture(h,96500)){
            var l=f.level(LabyrinthPlace.FLOORBOARDS);var p=f.player(h,l,new Vec3(96500,100,96500));
            var cat=f.keep(EntityType.CAT.create(l));var wolf=f.keep(EntityType.WOLF.create(l));var parrot=f.keep(EntityType.PARROT.create(l));
            for(TamableAnimal pet:List.of(cat,wolf,parrot)){pet.setTame(true,true);pet.setOwnerUUID(p.getUUID());pet.moveTo(p.position());l.addFreshEntity(pet);}
            h.assertTrue(CompanionOrders.command(p,cat.getId(),CompanionOrders.Order.STAY.ordinal())&&CompanionOrders.command(p,wolf.getId(),CompanionOrders.Order.STAY.ordinal()),"both native cats and dogs retain their owned wheel commands");
            h.assertTrue(!CompanionOrders.supported(parrot)&&!CompanionOrders.canCommand(p,parrot)&&!CompanionOrders.command(p,parrot.getId(),0)&&!CompanionOrders.issue(parrot,p,CompanionOrders.Order.STAY)&&!CompanionOrders.managed(parrot),"an owned parrot cannot open or forge a companion command or acquire saved wheel orders");
            cat.setOwnerUUID(UUID.randomUUID());h.assertTrue(!CompanionOrders.command(p,cat.getId(),0),"species eligibility does not bypass ownership");h.succeed();
        }
    }
    @GameTest(template="empty",batch="integration_toss")
    public static void canceledCanoeTossKeepsResidualOriginalThroughNativeSaveAndRelease(GameTestHelper h){
        try(var f=new Fixture(h,97000)){
            var l=f.level(LabyrinthPlace.PHONE_CANOE);var b=LabyrinthPlaces.base(f.origin,LabyrinthPlace.PHONE_CANOE);var p=f.player(h,l,Vec3.atBottomCenterOf(b.offset(PhoneCanoe.DOCK)));
            for(int i=0;i<36;i++)p.getInventory().setItem(i,new ItemStack(Items.STONE,64));
            var original=new ItemStack(Items.DIAMOND,7);original.set(DataComponents.CUSTOM_NAME,Component.literal("The seven originals"));CustomData.update(DataComponents.CUSTOM_DATA,original,t->t.putUUID("Provenance",UUID.randomUUID()));
            p.getInventory().setItem(0,original.copyWithCount(60));var personal=new CompoundTag();personal.putInt("Phase",3);var all=f.data.state(PhoneCanoe.ID);all.put(p.getUUID().toString(),personal);f.data.setState(PhoneCanoe.ID,all);
            var dropped=new ItemEntity(l,p.getX(),p.getY(),p.getZ(),original.copy());var toss=new ItemTossEvent(dropped,p);PhoneCanoe.onToss(toss);
            var queue=PhoneCanoe.personal(f.data,p.getUUID()).getList("ReturnedTosses",Tag.TAG_COMPOUND);var residual=ItemStack.parseOptional(p.registryAccess(),queue.getCompound(0));
            h.assertTrue(toss.isCanceled()&&p.getInventory().getItem(0).getCount()==64&&residual.getCount()==3&&ItemStack.isSameItemSameComponents(original,residual)&&dropped.getItem().isEmpty(),"partial native insertion keeps exactly the three residual originals with every component");
            var loaded=LabyrinthData.FACTORY.deserializer().apply(f.data.save(new CompoundTag(),p.registryAccess()),p.registryAccess());f.server.overworld().getDataStorage().set("the_oldest_house_labyrinth",loaded);
            var saved=PhoneCanoe.personal(loaded,p.getUUID());saved.putInt("Phase",0);all=loaded.state(PhoneCanoe.ID);all.put(p.getUUID().toString(),saved);loaded.setState(PhoneCanoe.ID,all);
            PhoneCanoe.tick(p);
            var returned=l.getEntitiesOfClass(ItemEntity.class,p.getBoundingBox().inflate(2),e->ItemStack.isSameItemSameComponents(e.getItem(),original));returned.forEach(f::keep);
            h.assertTrue(returned.size()==1&&returned.getFirst().getItem().getCount()==3&&p.getUUID().equals(returned.getFirst().getTarget())&&PhoneCanoe.personal(loaded,p.getUUID()).getList("ReturnedTosses",Tag.TAG_COMPOUND).isEmpty(),"a full inventory after the saved binding ends returns one owner-targeted residual stack");
            PhoneCanoe.tick(p);h.assertTrue(l.getEntitiesOfClass(ItemEntity.class,p.getBoundingBox().inflate(2),e->ItemStack.isSameItemSameComponents(e.getItem(),original)).size()==1,"subsequent ticks cannot duplicate the saved toss refund");h.succeed();
        }
    }
    @GameTest(template="empty",batch="integration_finale")
    public static void nativeFinaleClaimReleasesOnlyItsOwnerAndRetainsOfflineCommittedAttempts(GameTestHelper h){
        try(var f=new Fixture(h,97500)){
            var l=f.level(LabyrinthPlace.FLOORBOARDS);UUID owner=UUID.randomUUID(),peer=UUID.randomUUID();var world=f.data.state(FinaleProgress.STATE);world.putUUID("Owner",owner);f.data.setState(FinaleProgress.STATE,world);FinaleProgress.phase(f.server,owner,FinaleProgress.Phase.FIGHT);FinaleArchitecture.seal(l,f.origin,true);
            var seal=FinaleArchitecture.base(f.origin).offset(0,FinaleArchitecture.ARENA,29);
            h.assertTrue(!FinaleController.releaseClaim(f.server,peer)&&!l.getBlockState(seal).isAir(),"a peer cannot release another explorer's physical fight seal");
            FinaleController.reconcileClaim(f.server);h.assertTrue(FinaleProgress.world(f.server).hasUUID("Owner")&&!l.getBlockState(seal).isAir(),"offline native combat remains owned and paused");
            FinaleProgress.phase(f.server,owner,FinaleProgress.Phase.HOMEWARD);FinaleController.reconcileClaim(f.server);
            h.assertTrue(!FinaleProgress.world(f.server).hasUUID("Owner")&&l.getBlockState(seal).isAir()&&!WitnessAccount.has(f.data,owner,WitnessAccount.Story.ZAMPANO),"the released prisoner's homeward route opens the shared seal without synthesizing personal evidence");
            world=FinaleProgress.world(f.server);world.putUUID("Owner",owner);f.data.setState(FinaleProgress.STATE,world);FinaleProgress.phase(f.server,owner,FinaleProgress.Phase.LOCKED_OUT);FinaleArchitecture.seal(l,f.origin,true);FinaleController.reconcileClaim(f.server);
            h.assertTrue(!FinaleProgress.world(f.server).hasUUID("Owner")&&l.getBlockState(seal).isAir(),"a stale terminal saved claim is repaired physically");h.succeed();
        }
    }
    private static Fixture relocation;
    @AfterBatch(batch="integration_relocation")public static void cleanupRelocation(ServerLevel l){if(relocation!=null){relocation.close();relocation=null;}}
    @GameTest(template="empty",batch="integration_relocation",timeoutTicks=160)
    public static void nativeIslandRelocationPreservesInventoryFramesRidersAndReturnCoordinates(GameTestHelper h){
        relocation=new Fixture(h,98000);var f=relocation;var scene=LabyrinthPlace.PHONE_CANOE;var l=f.level(scene);var old=LabyrinthPlaces.legacyBase(f.origin,scene);var dest=LabyrinthPlaces.base(f.origin,scene);var delta=dest.subtract(old);f.data.setBuilt(26,f.origin);
        for(int x=-2;x<=2;x++)for(int z=-14;z<=0;z++)l.setBlock(old.offset(x,-1,z),Blocks.SPRUCE_PLANKS.defaultBlockState(),2);
        var cache=old.offset(2,0,-8);l.setBlock(cache,Blocks.BARREL.defaultBlockState(),2);var barrel=(BarrelBlockEntity)l.getBlockEntity(cache);barrel.setItem(5,new ItemStack(Items.EMERALD,9));
        var backing=old.offset(-1,1,-7);l.setBlock(backing,Blocks.STONE_BRICKS.defaultBlockState(),2);var frame=f.keep(new ItemFrame(l,backing.south(),Direction.SOUTH));frame.setItem(new ItemStack(Items.MAP));l.addFreshEntity(frame);
        var boat=f.keep(DrownedTownRegistry.CAVE_CANOE.get().create(l));boat.addTag(PhoneCanoe.CANOE);boat.mobile(true);boat.moveTo(Vec3.atBottomCenterOf(old.offset(PhoneCanoe.BOAT)));l.addFreshEntity(boat);
        var rider=f.player(h,l,boat.position());rider.startRiding(boat,true);var beforeBoat=boat.position();var beforeRider=rider.position();UUID boatId=boat.getUUID(),frameId=frame.getUUID();var frameAnchor=frame.getPos();
        f.data.pushReturn(rider.getUUID(),new LabyrinthData.Waypoint(HouseDimensions.OUTSIDE,Vec3.atBottomCenterOf(old.offset(0,0,1)),180,true));
        var own=new CompoundTag();own.putDouble("DropX",old.getX()+1.2);own.putDouble("DropY",old.getY()-.1);own.putDouble("DropZ",old.getZ()-32.5);var phones=f.data.state(PhoneCanoe.ID);phones.put(rider.getUUID().toString(),own);f.data.setState(PhoneCanoe.ID,phones);
        h.succeedWhen(()->{
            h.assertTrue(OutdoorRelocation.upgrade(f.server,f.origin),"the original scene's native entity chunks finish loading before migration");
            var moved=(BarrelBlockEntity)l.getBlockEntity(cache.offset(delta));h.assertTrue(moved!=null&&moved.getItem(0).isEmpty()&&moved.getItem(5).getCount()==9&&l.getBlockState(cache).isAir(),"the original finite cache moves without restocking or leaving a second item source");
            h.assertTrue(boat.getUUID().equals(boatId)&&frame.getUUID().equals(frameId)&&frame.getPos().equals(frameAnchor.offset(delta))&&ItemStack.isSameItemSameComponents(frame.getItem(),new ItemStack(Items.MAP)),"native canoe and hanging original retain identities, attachments and contents");
            h.assertTrue(rider.getVehicle()==boat&&boat.position().distanceToSqr(beforeBoat.add(delta.getX(),delta.getY(),delta.getZ()))<.1&&rider.position().distanceToSqr(beforeRider.add(delta.getX(),delta.getY(),delta.getZ()))<2,"an occupied canoe and its native rider move together without ejecting or double-translating");
            var returned=f.data.popReturn(rider.getUUID());h.assertTrue(returned!=null&&returned.pos().distanceToSqr(Vec3.atBottomCenterOf(dest.offset(0,0,1)))<.01&&f.data.door(scene.entryDoorId()).lower.equals(dest.offset(0,0,1)),"saved return coordinates and the registered physical doorway translate to the isolated island");
            h.assertTrue(Math.abs(PhoneCanoe.personal(f.data,rider.getUUID()).getDouble("DropZ")-(dest.getZ()-32.5))<.01,"the dropped-camera location follows its actual lake");
            h.assertTrue(OutdoorRelocation.upgrade(f.server,f.origin)&&moved.getItem(5).getCount()==9,"the saved relocation checkpoint cannot copy originals twice");
            var outside=Arrays.stream(LabyrinthPlace.values()).filter(NovelRooms::outside).toList();for(int a=0;a<outside.size();a++)for(int c=a+1;c<outside.size();c++)h.assertTrue(Math.abs(LabyrinthPlaces.base(f.origin,outside.get(a)).getZ()-LabyrinthPlaces.base(f.origin,outside.get(c)).getZ())>=4096,"outdoor islands are physically separate beyond native maximum render distance");
        });
    }
}
