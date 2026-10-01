package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import io.github.knaitoe.theoldesthouse.opening.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;

@GameTestHolder(TheOldestHouse.MOD_ID)
@net.neoforged.neoforge.gametest.PrefixGameTestTemplate(false)
public final class PetsAndClapTests {
    @GameTest(template="empty") public static void livingCustodyHasRealPricePersistentDeadlineAndRepeatableRecovery(GameTestHelper h) {
        var registries=h.getLevel().registryAccess();var data=new MotherCollection();UUID owner=UUID.randomUUID(),original=UUID.randomUUID();
        CompoundTag saved=new CompoundTag();saved.putUUID("UUID",original);saved.putFloat("Health",7);
        var entry=data.keepLivingPet(original,saved,owner,"Button");
        h.assertTrue(entry!=null&&data.keepLivingPet(original,saved,owner,"Button")==null,"one living animal cannot be archived twice");
        var dirt=new ItemStack(Items.DIRT);dirt.set(DataComponents.CUSTOM_NAME,Component.literal("Very precious dirt"));
        h.assertTrue(!data.canRecoverPet(owner,entry.id,dirt,0)&&!data.canRecoverPet(UUID.randomUUID(),entry.id,new ItemStack(Items.DIAMOND),0),"naming rubble and another owner's diamonds cannot steal a pet");
        h.assertTrue(data.advanceRansom(owner,5000,true).isEmpty()&&entry.ransomRemaining==MotherCollection.RANSOM_TICKS,"travel time does not consume the rescue window");
        data.beginRansom(owner);data.advanceRansom(owner,200,true);data.advanceRansom(owner,10000,false);
        var loaded=MotherCollection.load(data.save(new CompoundTag(),registries),registries);var held=loaded.entry(entry.id);
        h.assertTrue(held.livingClaim&&held.ransomStarted&&held.ransomRemaining==1000&&held.contents.getFloat("Health")==7,"live state and the paused deadline survive restart");
        var emeralds=new ItemStack(Items.EMERALD,7);h.assertTrue(loaded.recoverPet(owner,entry.id,emeralds,registries,0)&&emeralds.getCount()==3,"the exact four-emerald price is consumed once");
        h.assertTrue(!loaded.holdsLivingPet(owner)&&loaded.visible().stream().anyMatch(e->e.item(registries).is(Items.EMERALD)&&e.item(registries).getCount()==4),"the Mother keeps the real payment and gives up custody");
        h.assertTrue(loaded.keepLivingPet(original,saved,owner,"Button")!=null,"the recovered original may be claimed on a later loss");
        var second=loaded.all().stream().filter(e->e.livingClaim).findFirst().orElseThrow();loaded.beginRansom(owner);
        h.assertTrue(loaded.advanceRansom(owner,MotherCollection.RANSOM_TICKS,true).contains(second)&&loaded.disposeLivingPet(second.id),"an unpaid live ransom reaches a real permanent disposal");
        h.assertTrue(!loaded.canRecoverPet(owner,second.id,new ItemStack(Items.DIAMOND),0)&&loaded.keepPet(original,saved,owner,"Button")==null,"disposal cannot turn into the old resurrection exchange");h.succeed();
    }
    @GameTest(template="empty") public static void lostPetsBiasOnlyTheirOwnersDenAndSurviveReload(GameTestHelper h) {
        var data=new LabyrinthData();UUID owner=UUID.randomUUID(),other=UUID.randomUUID();CompoundTag state=data.state(MotherOfStrays.ID),owners=new CompoundTag();owners.putBoolean(owner.toString(),true);state.put("LivingPetOwners",owners);data.setState(MotherOfStrays.ID,state);
        data.visit(owner,LabyrinthPlace.MOTHER_DEN);
        var loaded=LabyrinthData.FACTORY.deserializer().apply(data.save(new CompoundTag(),h.getLevel().registryAccess()),h.getLevel().registryAccess());
        h.assertTrue(LabyrinthDealer.vignetteChance(loaded,owner)==65&&LabyrinthDealer.vignetteChance(loaded,other)==30,"only the bereaved player's vignette opportunity increases");
        h.assertTrue(LabyrinthDealer.rememberedWeight(loaded,owner,LabyrinthPlace.MOTHER_DEN,1)==96&&LabyrinthDealer.rememberedWeight(loaded,other,LabyrinthPlace.MOTHER_DEN,1)==12,"a held live pet overrides only its owner's recent-den suppression");h.succeed();
    }
    @GameTest(template="empty",batch="pet_actions") public static void wheelPetsCatsDogsPekingeseAndParrotsWithoutReplacingStay(GameTestHelper h) {
        var level=h.getLevel();var owner=h.makeMockServerPlayerInLevel();BlockPos at=h.absolutePos(new BlockPos(2,2,2));owner.moveTo(at.getCenter());
        for(var type:List.of(EntityType.CAT,EntityType.WOLF,MotherRegistry.PEKINGESE.get(),EntityType.PARROT)) {
            var entity=type.create(level);h.assertTrue(entity instanceof TamableAnimal,"every supported animal is a native tameable");var pet=(TamableAnimal)entity;
            pet.moveTo(owner.position().add(1,0,0));pet.tame(owner);pet.addTag(MotherOfStrays.RELEASED);level.addFreshEntity(pet);
            h.assertTrue(CompanionOrders.issue(pet,owner,CompanionOrders.Order.STAY)&&CompanionOrders.command(owner,pet.getId(),CompanionOrders.PET_ACTION),"the real server packet route accepts the Pet action");
            h.assertTrue(CompanionOrders.order(pet)==CompanionOrders.Order.STAY&&pet.isOrderedToSit()&&owner.getUUID().equals(pet.getOwnerUUID()),"a pat preserves the chosen order and native owner");
            pet.addTag(MotherOfStrays.PET);h.assertTrue(!CompanionOrders.canCommand(owner,pet),"a kept display animal cannot be commanded out of custody");pet.discard();
        }
        owner.server.getPlayerList().remove(owner);h.succeed();
    }
    @GameTest(template="empty",batch="pet_actions") public static void hillaryRequestsANewPatAndResumesSavedStayAtTheManor(GameTestHelper h) {
        var level=h.getLevel();var owner=h.makeMockServerPlayerInLevel();var server=level.getServer();var oldHouse=HouseSavedData.get(server);
        var house=new HouseSavedData();server.overworld().getDataStorage().set("the_oldest_house",house);
        Wolf wolf=null;
        try {
            BlockPos home=h.absolutePos(new BlockPos(2,2,2));owner.moveTo(home.getCenter());wolf=Hillary.spawn(level,home,owner.getUUID());wolf.tame(owner);
            var state=OpeningSequence.state(owner);state.markHillaryArrived(1);state.setHillary(wolf.getUUID());Hillary.acknowledge(wolf);
            h.assertTrue(CompanionOrders.issue(wolf,owner,CompanionOrders.Order.STAY)&&wolf.isOrderedToSit(),"day-two Stay works before there is a house");
            BlockPos origin=home.offset(35,0,35);house.markSpawned(origin);
            h.assertTrue(Hillary.introductionActive(wolf)&&!Hillary.introductionConfirmed(wolf),"a greeting at home is not a premature confirmation of the absent house");
            Hillary.tickGuide(level,owner,wolf.getUUID(),true,origin);
            h.assertTrue(!wolf.isOrderedToSit()&&CompanionOrders.order(wolf)==CompanionOrders.Order.STAY,"the introduction temporarily owns movement without erasing Stay");
            h.assertTrue(CompanionOrders.pet(wolf,owner)&&Hillary.introductionConfirmed(wolf),"the wheel's Pet action confirms the invitation");
            var porch=HouseProxyEntityEvacuation.frontDoorExit(level,origin);
            if(porch==null)porch=Vec3.atBottomCenterOf(origin.offset(HouseLayout.AXIS_X,1,HouseLayout.FRONT_DOOR_Z-2));
            owner.moveTo(porch);wolf.moveTo(porch.add(1,0,0));Hillary.tickGuide(level,owner,wolf.getUUID(),true,origin);
            h.assertTrue(!Hillary.introductionActive(wolf)&&wolf.getPersistentData().getBoolean(Hillary.INTRO_DONE)&&wolf.isOrderedToSit()
                    &&CompanionOrders.order(wolf)==CompanionOrders.Order.STAY&&owner.getUUID().equals(wolf.getOwnerUUID()),"arrival restores the wheel command and native owner");h.succeed();
        } finally {if(wolf!=null)wolf.discard();server.getPlayerList().remove(owner);server.overworld().getDataStorage().set("the_oldest_house",oldHouse);}
    }
    private static final class Fixture implements AutoCloseable {
        final net.minecraft.server.MinecraftServer server;final ServerLevel level;final HouseSavedData oldHouse;final LabyrinthData oldData;final MotherCollection oldMother;
        final GameTestHelper helper;final BlockPos origin,base;final List<ServerPlayer> players=new ArrayList<>();final boolean oldKeep;
        Fixture(GameTestHelper h,BlockPos origin){helper=h;server=h.getLevel().getServer();level=HouseTestLevel.get(server);this.origin=origin;
            oldHouse=HouseSavedData.get(server);oldData=LabyrinthData.get(server);oldMother=MotherCollection.get(server);oldKeep=level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);
            var house=new HouseSavedData();house.markSpawned(origin);server.overworld().getDataStorage().set("the_oldest_house",house);
            var data=new LabyrinthData();data.setBuilt(LabyrinthBuilder.VERSION,origin);server.overworld().getDataStorage().set("the_oldest_house_labyrinth",data);server.overworld().getDataStorage().set("the_oldest_house_mother",new MotherCollection());
            base=LabyrinthPlaces.base(origin,LabyrinthPlace.HIDE_AND_CLAP);HideAndClap.clearAll();HideAndClap.build(server,level,base);LabyrinthBuilder.registerDoors(data,LabyrinthPlace.HIDE_AND_CLAP,base);
            level.getChunkSource().addRegionTicket(TicketType.PORTAL,new ChunkPos(base),3,base);level.getGameRules().getRule(GameRules.RULE_KEEPINVENTORY).set(true,server);
        }
        ServerPlayer player(BlockPos at){var p=helper.makeMockServerPlayerInLevel();p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);p.teleportTo(level,at.getX()+.5,at.getY(),at.getZ()+.5,180,0);players.add(p);return p;}
        Wolf dog(ServerPlayer owner,boolean stay){var pet=EntityType.WOLF.create(level);pet.moveTo(owner.position().add(stay?-1:1,0,0));pet.tame(owner);pet.setHealth(7);pet.setNoAi(true);level.addFreshEntity(pet);CompanionOrders.issue(pet,owner,stay?CompanionOrders.Order.STAY:CompanionOrders.Order.FOLLOW);return pet;}
        public void close(){HideAndClap.clearAll();MotherOfStrays.clearAll();for(var p:players){if(server.getPlayerList().getPlayers().contains(p))server.getPlayerList().remove(p);else p.discard();}
            for(var place:List.of(LabyrinthPlace.HIDE_AND_CLAP,LabyrinthPlace.MOTHER_DEN)) {
                BlockPos b=LabyrinthPlaces.base(origin,place);for(var e:level.getEntitiesOfClass(Entity.class,new AABB(b).inflate(40),e->!(e instanceof ServerPlayer)))e.discard();
                level.getChunkSource().removeRegionTicket(TicketType.PORTAL,new ChunkPos(b),3,b);
            }
            level.getGameRules().getRule(GameRules.RULE_KEEPINVENTORY).set(oldKeep,server);
            server.overworld().getDataStorage().set("the_oldest_house",oldHouse);server.overworld().getDataStorage().set("the_oldest_house_labyrinth",oldData);server.overworld().getDataStorage().set("the_oldest_house_mother",oldMother);LabyrinthBuilder.clearAll();LabyrinthDoors.clearAll();
        }
    }
    private static Fixture death,recovery;
    @AfterBatch(batch="clap_reveal") public static void cleanDeath(ServerLevel level){if(death!=null){death.close();death=null;}}
    @AfterBatch(batch="pet_ransom") public static void cleanRecovery(ServerLevel level){if(recovery!=null){recovery.close();recovery=null;}}
    @GameTest(template="empty",batch="clap_reveal",timeoutTicks=100)
    public static void nativeFailureRemovesClothBeforeKillingAndClaimsOnlyFollowingPet(GameTestHelper h){
        death=new Fixture(h,new BlockPos(9200,80,9200));var f=death;var owner=f.player(f.base.offset(0,0,-4));var follower=f.dog(owner,false);var staying=f.dog(owner,true);UUID id=follower.getUUID();
        owner.getInventory().setItem(0,new ItemStack(Items.DIAMOND,3));owner.setItemSlot(EquipmentSlot.HEAD,new ItemStack(LabyrinthRegistry.BLINDFOLD.get()));HideAndClap.enter(owner);
        h.runAfterDelay(5,()->{
            h.assertTrue(HideAndClap.isBound(owner.getItemBySlot(EquipmentSlot.HEAD),owner.getUUID()),"server ticks bind the real worn cloth");
            var data=LabyrinthData.get(f.server);CompoundTag state=data.state(HideAndClap.ID),session=state.getCompound("Session");session.putLong("EquippedAt",f.level.getGameTime()-ClapGameClock.LIMIT_TICKS);state.put("Session",session);data.setState(HideAndClap.ID,state);HideAndClap.clearAll();
        });
        h.runAfterDelay(23,()->h.assertTrue(owner.isAlive()&&owner.getItemBySlot(EquipmentSlot.HEAD).isEmpty()&&HideAndClap.isLocked(owner),"the cloth physically comes off while the living player remains locked for the reveal"));
        h.runAfterDelay(58,()->{
            h.assertTrue(owner.isDeadOrDying()&&owner.getInventory().isEmpty(),"the native kill and keepInventory loss execute after the visible interval");
            var collection=MotherCollection.get(f.server);var entry=collection.all().stream().filter(e->e.livingClaim).findFirst().orElseThrow();
            h.assertTrue(follower.isRemoved()&&entry.contents.hasUUID("UUID")&&entry.contents.getUUID("UUID").equals(id)&&entry.contents.getFloat("Health")==7,"a real vignette death captures the exact surviving follower");
            h.assertTrue(staying.isAlive()&&!staying.isRemoved()&&collection.all().stream().filter(e->e.livingClaim).count()==1,"the pet ordered to stay is left alone");
            h.assertTrue(LabyrinthDealer.rescueNeeded(LabyrinthData.get(f.server),owner.getUUID()),"the owner receives the urgent den dealing");h.succeed();
        });
    }
    @GameTest(template="empty",batch="pet_ransom",timeoutTicks=100)
    public static void nativeLivingRecoveryRetainsIdentityHealthAndOrderAndChargesOnce(GameTestHelper h){
        recovery=new Fixture(h,new BlockPos(9600,80,9600));var f=recovery;var owner=f.player(f.base.offset(0,0,-4));var pet=f.dog(owner,false);UUID id=pet.getUUID();
        var den=LabyrinthPlaces.base(f.origin,LabyrinthPlace.MOTHER_DEN);MotherOfStrays.build(f.server,f.level,den);f.level.getChunkSource().addRegionTicket(TicketType.PORTAL,new ChunkPos(den),3,den);
        h.runAfterDelay(3,()->{
            h.assertTrue(MotherOfStrays.claimFollowingPets(owner)==1&&pet.isRemoved(),"standing follower enters living custody");
            owner.teleportTo(f.level,den.getX()+.5,den.getY(),den.getZ()-5.5,0,0);MotherOfStrays.onArrive(owner,LabyrinthPlace.MOTHER_DEN);
        });
        h.runAfterDelay(8,()->{
            var captive=f.level.getEntitiesOfClass(Mob.class,new AABB(den).inflate(30),e->e.getTags().contains(MotherOfStrays.PET)&&e.getPersistentData().hasUUID("MotherEntry")).getFirst();
            h.assertTrue(!captive.getUUID().equals(id),"the den display has its own UUID and cannot duplicate the original");
            owner.moveTo(captive.position().add(0,0,1));owner.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND,2));
            MotherOfStrays.onEntityInteract(new PlayerInteractEvent.EntityInteract(owner,InteractionHand.MAIN_HAND,captive));
        });
        h.runAfterDelay(12,()->{
            var restored=f.level.getEntity(id);h.assertTrue(restored instanceof Wolf&&((Wolf)restored).getHealth()==7&&owner.getUUID().equals(((Wolf)restored).getOwnerUUID()),"the original identity and health return to the native world");
            h.assertTrue(CompanionOrders.order((Wolf)restored)==CompanionOrders.Order.FOLLOW&&CompanionOrders.canCommand(owner,(Wolf)restored)&&owner.getMainHandItem().getCount()==1,"one actual diamond pays once and the recovered pet's wheel works");
            h.assertTrue(!MotherCollection.get(f.server).holdsLivingPet(owner.getUUID())&&!LabyrinthDealer.rescueNeeded(LabyrinthData.get(f.server),owner.getUUID()),"successful recovery immediately restores ordinary dealing");h.succeed();
        });
    }
}
