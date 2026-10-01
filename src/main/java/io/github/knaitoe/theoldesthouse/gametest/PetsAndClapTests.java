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
    @GameTest(template="empty") public static void vignetteArtifactsPayOnceAndBorrowedArtifactsAreRefused(GameTestHelper h) {
        var data=new MotherCollection();var registries=h.getLevel().registryAccess();UUID owner=UUID.randomUUID();
        for(var reward:List.of(VignetteYields.mark(new ItemStack(Items.PAPER,2),HideAndClap.ID),
                new ItemStack(LabyrinthRegistry.PHONE.get()),new ItemStack(DrownedTownRegistry.LAKE_PHONE.get()),
                new ItemStack(LabyrinthRegistry.SCRATCH_TICKET.get()),new ItemStack(DrownedTownRegistry.CHURCH_KEY.get()))) {
            CompoundTag pet=new CompoundTag();UUID original=UUID.randomUUID();pet.putUUID("UUID",original);
            var entry=data.keepLivingPet(original,pet,owner,"Waiting dog");int before=reward.getCount();
            h.assertTrue(data.recoverPet(owner,entry.id,reward,registries,0)&&reward.getCount()==before-1,"one genuine vignette reward pays for one living pet");
        }
        var borrowed=VignetteYields.mark(new ItemStack(Items.PAPER),HideAndClap.ID);
        var kept=data.keepItem(borrowed,registries,owner,0);data.presence(owner,false);data.presence(owner,true);
        var loan=data.claimItem(owner,kept.id,registries);
        h.assertTrue(!loan.isEmpty()&&MotherCollection.ransomPrice(loan)==0,"the Mother's own borrowed artifact cannot buy a pet");
        var gift=VignetteYields.mark(new ItemStack(Items.PAPER),TellTaleFloorboards.ID);
        h.assertTrue(data.trade(owner,gift,registries,0)&&data.debt(owner)==null&&gift.isEmpty(),"a vignette keepsake also settles a shelf bargain");h.succeed();
    }
    @GameTest(template="empty") public static void livingPaymentsAreSealedAndPersistWithoutDuplicatingOrSalving(GameTestHelper h) {
        var data=new MotherCollection();var registries=h.getLevel().registryAccess();UUID owner=UUID.randomUUID(),original=UUID.randomUUID(),given=UUID.randomUUID();
        CompoundTag pet=new CompoundTag();pet.putUUID("UUID",original);var entry=data.keepLivingPet(original,pet,owner,"Button");
        CompoundTag stray=new CompoundTag();stray.putUUID("UUID",given);stray.putFloat("Health",5);
        h.assertTrue(!data.tradeLife(UUID.randomUUID(),entry.id,given,stray,"Stray",registries),"another player cannot trade away the owner's captive");
        h.assertTrue(data.tradeLife(owner,entry.id,given,stray,"Stray",registries)&&!data.holdsLivingPet(owner),"one life replaces one captive");
        var loaded=MotherCollection.load(data.save(new CompoundTag(),registries),registries);
        var offering=loaded.all().stream().filter(e->e.offeredLife).findFirst().orElseThrow();
        h.assertTrue(offering.sealed&&!offering.livingClaim&&offering.owner==null&&offering.contents.getUUID("UUID").equals(given)
                &&offering.contents.getFloat("Health")==5&&!loaded.canKeepOfferedLife(given),"the actual animal and its identity remain in saved sealed custody");
        h.assertTrue(!loaded.canRecoverPet(owner,offering.id,new ItemStack(Items.DIAMOND),0)&&!loaded.salved(),"the offered animal cannot be bought straight back and a life bargain does not resolve Mother peacefully");
        UUID person=UUID.randomUUID(),other=UUID.randomUUID();CompoundTag next=new CompoundTag();next.putUUID("UUID",other);
        var second=loaded.keepLivingPet(other,next,owner,"Next dog");
        h.assertTrue(loaded.tradeLife(owner,second.id,person,null,"Explorer",registries),"the collection records a completed native player payment");
        var again=MotherCollection.load(loaded.save(new CompoundTag(),registries),registries);
        h.assertTrue(again.wasPlayerOffered(person)&&again.all().stream().filter(e->e.pet).count()==1,"a player payment persists without spawning an archived player");h.succeed();
    }
    @GameTest(template="empty") public static void playerOfferRequiresTheSameClosedRoomIncludingACeiling(GameTestHelper h) {
        BlockPos base=h.absolutePos(new BlockPos(4,2,4));closedRoom(h.getLevel(),base);
        BlockPos beside=base.east();
        h.assertTrue(MotherOfferings.enclosedTogether(h.getLevel(),base,beside),"a small closed room really encloses both positions");
        h.assertTrue(!MotherOfferings.enclosedTogether(h.getLevel(),base,base.offset(4,0,0)),"someone outside the room is not a payment");
        h.getLevel().setBlock(base.offset(2,0,0),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);
        h.getLevel().setBlock(base.offset(2,1,0),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);
        h.assertTrue(!MotherOfferings.enclosedTogether(h.getLevel(),base,beside),"an open exit invalidates the trap immediately");
        closedRoom(h.getLevel(),base);h.getLevel().setBlock(base.above(3),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);
        h.assertTrue(!MotherOfferings.enclosedTogether(h.getLevel(),base,beside),"missing roofing is an escape, not an enclosed room");h.succeed();
    }
    private static void closedRoom(ServerLevel level,BlockPos base) {
        for(int x=-2;x<=2;x++)for(int y=-1;y<=3;y++)for(int z=-2;z<=2;z++)
            level.setBlock(base.offset(x,y,z),(x==-2||x==2||z==-2||z==2||y==-1||y==3
                    ?net.minecraft.world.level.block.Blocks.STONE:net.minecraft.world.level.block.Blocks.AIR).defaultBlockState(),3);
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
    private static HouseSavedData introHouse;
    private static ServerPlayer introOwner;
    private static Wolf introWolf;
    private static BlockPos introPorch;
    @AfterBatch(batch="hillary_introduction") public static void cleanIntroduction(ServerLevel level) {
        if(introWolf!=null){introWolf.discard();introWolf=null;}
        if(introOwner!=null){level.getServer().getPlayerList().remove(introOwner);introOwner=null;}
        if(introPorch!=null){level.getChunkSource().removeRegionTicket(TicketType.PORTAL,new ChunkPos(introPorch),3,introPorch);introPorch=null;}
        if(introHouse!=null){level.getServer().overworld().getDataStorage().set("the_oldest_house",introHouse);introHouse=null;}
    }
    @GameTest(template="empty",batch="hillary_introduction",timeoutTicks=80) public static void hillaryRequestsANewPatAndResumesSavedStayAtTheManor(GameTestHelper h) {
        var level=h.getLevel();var owner=h.makeMockServerPlayerInLevel();var server=level.getServer();var oldHouse=HouseSavedData.get(server);
        introHouse=oldHouse;introOwner=owner;
        var house=new HouseSavedData();server.overworld().getDataStorage().set("the_oldest_house",house);
            BlockPos home=h.absolutePos(new BlockPos(2,2,2));owner.moveTo(home.getCenter());var wolf=Hillary.spawn(level,home,owner.getUUID());introWolf=wolf;wolf.tame(owner);
            var state=OpeningSequence.state(owner);state.markHillaryArrived(1);state.setHillary(wolf.getUUID());Hillary.acknowledge(wolf);
            h.assertTrue(CompanionOrders.issue(wolf,owner,CompanionOrders.Order.STAY)&&wolf.isOrderedToSit(),"day-two Stay works before there is a house");
            BlockPos origin=home.offset(35,0,35);house.markSpawned(origin);
            h.assertTrue(Hillary.introductionActive(wolf)&&!Hillary.introductionConfirmed(wolf),"a greeting at home is not a premature confirmation of the absent house");
            Hillary.tickGuide(level,owner,wolf.getUUID(),true,origin);
            h.assertTrue(!wolf.isOrderedToSit()&&CompanionOrders.order(wolf)==CompanionOrders.Order.STAY,"the introduction temporarily owns movement without erasing Stay");
            h.assertTrue(CompanionOrders.pet(wolf,owner)&&Hillary.introductionConfirmed(wolf),"the wheel's Pet action confirms the invitation");
            var porch=HouseProxyEntityEvacuation.frontDoorExit(level,origin);
            if(porch==null)porch=Vec3.atBottomCenterOf(origin.offset(HouseLayout.AXIS_X,1,HouseLayout.FRONT_DOOR_Z-2));
            BlockPos platform=BlockPos.containing(porch).below();
            for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++)level.setBlock(platform.offset(x,0,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3);
            var grounded=HouseProxyEntityEvacuation.frontDoorExit(level,origin);if(grounded!=null)porch=grounded;
            wolf.setNoAi(true);
            introPorch=BlockPos.containing(porch);level.getChunkSource().addRegionTicket(TicketType.PORTAL,new ChunkPos(introPorch),3,introPorch);level.getChunkAt(introPorch);
            owner.moveTo(porch);wolf.moveTo(porch.add(1,0,0));
            h.onEachTick(()->{
            if(level.getEntity(wolf.getUUID())!=wolf)return;
            Hillary.tickGuide(level,owner,wolf.getUUID(),true,origin);
            h.assertTrue(!Hillary.introductionActive(wolf)&&wolf.getPersistentData().getBoolean(Hillary.INTRO_DONE)&&wolf.isOrderedToSit()
                    &&CompanionOrders.order(wolf)==CompanionOrders.Order.STAY&&owner.getUUID().equals(wolf.getOwnerUUID()),"arrival restores the wheel command and native owner; done="+wolf.getPersistentData().getBoolean(Hillary.INTRO_DONE)+"; sit="+wolf.isOrderedToSit()+"; owner="+wolf.getOwnerUUID()+"; wolf="+wolf.position()+"; porch="+HouseProxyEntityEvacuation.frontDoorExit(level,origin)+"; confirmed="+Hillary.introductionConfirmed(wolf));h.succeed();
            });
    }
    private static final class Fixture implements AutoCloseable {
        final net.minecraft.server.MinecraftServer server;final ServerLevel level;final HouseSavedData oldHouse;final LabyrinthData oldData;final MotherCollection oldMother;
        final GameTestHelper helper;final BlockPos origin,base;final List<ServerPlayer> players=new ArrayList<>();final boolean oldKeep;
        Fixture(GameTestHelper h,BlockPos origin){this(h,origin,LabyrinthPlace.HIDE_AND_CLAP);}
        Fixture(GameTestHelper h,BlockPos origin,LabyrinthPlace place){helper=h;server=h.getLevel().getServer();level=HouseTestLevel.get(server);this.origin=origin;
            oldHouse=HouseSavedData.get(server);oldData=LabyrinthData.get(server);oldMother=MotherCollection.get(server);oldKeep=level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);
            var house=new HouseSavedData();house.markSpawned(origin);server.overworld().getDataStorage().set("the_oldest_house",house);
            var data=new LabyrinthData();data.setBuilt(LabyrinthBuilder.VERSION,origin);server.overworld().getDataStorage().set("the_oldest_house_labyrinth",data);server.overworld().getDataStorage().set("the_oldest_house_mother",new MotherCollection());
            base=LabyrinthPlaces.base(origin,place);HideAndClap.clearAll();MotherOfStrays.clearAll();
            if(place==LabyrinthPlace.HIDE_AND_CLAP)HideAndClap.build(server,level,base);else MotherOfStrays.build(server,level,base);
            LabyrinthBuilder.registerDoors(data,place,base);
            level.getChunkSource().addRegionTicket(TicketType.PORTAL,new ChunkPos(base),3,base);level.getGameRules().getRule(GameRules.RULE_KEEPINVENTORY).set(true,server);
        }
        ServerPlayer player(BlockPos at){var p=helper.makeMockServerPlayerInLevel();p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);p.teleportTo(level,at.getX()+.5,at.getY(),at.getZ()+.5,180,0);p.hasChangedDimension();players.add(p);return p;}
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
    private static Fixture death,recovery,unpaid,lifeTrade,playerTrade;
    @AfterBatch(batch="clap_reveal") public static void cleanDeath(ServerLevel level){if(death!=null){death.close();death=null;}}
    @AfterBatch(batch="pet_ransom") public static void cleanRecovery(ServerLevel level){if(recovery!=null){recovery.close();recovery=null;}}
    @GameTest(template="empty",batch="clap_reveal",timeoutTicks=1400)
    public static void nativeFailureRemovesClothBeforeKillingAndClaimsOnlyFollowingPet(GameTestHelper h){
        death=new Fixture(h,new BlockPos(9200,80,9200));var f=death;var owner=f.player(f.base.offset(0,0,-4));var follower=f.dog(owner,false);var staying=f.dog(owner,true);UUID id=follower.getUUID();
        owner.getInventory().setItem(0,new ItemStack(Items.DIAMOND,3));owner.setItemSlot(EquipmentSlot.HEAD,new ItemStack(LabyrinthRegistry.BLINDFOLD.get()));HideAndClap.enter(owner);
        h.runAfterDelay(5,()->{
            h.assertTrue(HideAndClap.isBound(owner.getItemBySlot(EquipmentSlot.HEAD),owner.getUUID()),"server ticks bind the real worn cloth");
        });
        h.runAfterDelay(ClapGameClock.LIMIT_TICKS+23,()->{
            HideAndClap.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Pre(owner));
            h.assertTrue(owner.isAlive()&&owner.getItemBySlot(EquipmentSlot.HEAD).isEmpty()&&HideAndClap.isLocked(owner),"the cloth physically comes off and player ticks cannot rebind it during the reveal");
        });
        h.runAfterDelay(ClapGameClock.LIMIT_TICKS+58,()->{
            h.assertTrue(owner.isDeadOrDying()&&owner.getInventory().isEmpty(),"the native kill and keepInventory loss execute after the visible interval; health="+owner.getHealth()+"; removed="+owner.isRemoved()+"; inventory="+owner.getInventory().isEmpty()+"; changing="+owner.isChangingDimension()+"; invulnerable="+owner.isInvulnerableTo(owner.damageSources().genericKill()));
            var collection=MotherCollection.get(f.server);var entry=collection.all().stream().filter(e->e.livingClaim).findFirst().orElseThrow();
            h.assertTrue(follower.isRemoved()&&entry.contents.hasUUID("UUID")&&entry.contents.getUUID("UUID").equals(id)&&entry.contents.getFloat("Health")==7,"a real vignette death captures the exact surviving follower");
            h.assertTrue(staying.isAlive()&&!staying.isRemoved()&&collection.all().stream().filter(e->e.livingClaim).count()==1,"the pet ordered to stay is left alone");
            h.assertTrue(LabyrinthDealer.rescueNeeded(LabyrinthData.get(f.server),owner.getUUID()),"the owner receives the urgent den dealing");h.succeed();
        });
    }
    @AfterBatch(batch="pet_unpaid") public static void cleanUnpaid(ServerLevel level){if(unpaid!=null){unpaid.close();unpaid=null;}}
    @GameTest(template="empty",batch="pet_unpaid",timeoutTicks=90)
    public static void nativeRefusedRansomEndsInPetDeathRatherThanAnotherArchive(GameTestHelper h){
        unpaid=new Fixture(h,new BlockPos(10000,80,10000),LabyrinthPlace.MOTHER_DEN);var f=unpaid;var owner=f.player(f.base.offset(0,0,-4));var original=f.dog(owner,false);
        var den=LabyrinthPlaces.base(f.origin,LabyrinthPlace.MOTHER_DEN);MotherOfStrays.build(f.server,f.level,den);f.level.getChunkSource().addRegionTicket(TicketType.PORTAL,new ChunkPos(den),3,den);
        final Mob[] display={null};final UUID[] entryId={null};
        h.runAfterDelay(3,()->{
            h.assertTrue(MotherOfStrays.claimFollowingPets(owner)==1,"the living original is actually claimed");
            owner.teleportTo(f.level,den.getX()+.5,den.getY(),den.getZ()-5.5,0,0);MotherOfStrays.onArrive(owner,LabyrinthPlace.MOTHER_DEN);
            var collection=MotherCollection.get(f.server);var entry=collection.all().stream().filter(e->e.livingClaim).findFirst().orElseThrow();entryId[0]=entry.id;
            display[0]=f.level.getEntitiesOfClass(Mob.class,new AABB(den).inflate(30),e->e.getPersistentData().hasUUID("MotherEntry")&&entry.id.equals(e.getPersistentData().getUUID("MotherEntry"))).getFirst();
            owner.moveTo(display[0].position().add(0,0,1));var dirt=new ItemStack(Items.DIRT);dirt.set(DataComponents.CUSTOM_NAME,Component.literal("Precious"));owner.setItemInHand(InteractionHand.MAIN_HAND,dirt);
            MotherOfStrays.onEntityInteract(new PlayerInteractEvent.EntityInteract(owner,InteractionHand.MAIN_HAND,display[0]));
            h.assertTrue(collection.holdsLivingPet(owner.getUUID())&&owner.getMainHandItem().getCount()==1,"a refused trade consumes nothing and leaves custody intact");
            entry.ransomRemaining=20;collection.setDirty();
        });
        h.runAfterDelay(47,()->{
            var collection=MotherCollection.get(f.server);
            var remaining=collection.entry(entryId[0]);
            h.assertTrue(display[0].isDeadOrDying()||display[0].isRemoved(),"the displayed animal physically dies at the native deadline; owner="+owner.position()+"; alive="+owner.isAlive()+"; inDen="+MotherOfStrays.inDen(owner)+"; levelPlayers="+f.level.players().size()+"; remaining="+(remaining==null?-1:remaining.ransomRemaining)+"; started="+(remaining!=null&&remaining.ransomStarted)+"; health="+display[0].getHealth()+"; built="+LabyrinthBuilder.isBuilt(f.server));
            h.assertTrue(collection.entry(entryId[0])==null&&!collection.holdsLivingPet(owner.getUUID())&&!collection.canRecoverPet(owner.getUUID(),entryId[0],new ItemStack(Items.DIAMOND),0),"disposal removes the living entry instead of archiving a resurrectable duplicate");
            h.assertTrue(!LabyrinthDealer.rescueNeeded(LabyrinthData.get(f.server),owner.getUUID())&&collection.corruption()==0,"the urgent route ends and Mother's appearance resets");h.succeed();
        });
    }
    @GameTest(template="empty",batch="pet_ransom",timeoutTicks=100)
    public static void nativeLivingRecoveryRetainsIdentityHealthAndOrderAndChargesOnce(GameTestHelper h){
        recovery=new Fixture(h,new BlockPos(9600,80,9600),LabyrinthPlace.MOTHER_DEN);var f=recovery;var owner=f.player(f.base.offset(0,0,-4));var pet=f.dog(owner,false);UUID id=pet.getUUID();
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
    @AfterBatch(batch="living_offerings") public static void cleanLifeTrades(ServerLevel level){if(lifeTrade!=null){lifeTrade.close();lifeTrade=null;}}
    @GameTest(template="empty",batch="living_offerings",timeoutTicks=100)
    public static void nativeMotherClickAcceptsArtifactThenAnActualStrayLife(GameTestHelper h) {
        lifeTrade=new Fixture(h,new BlockPos(10400,80,10400),LabyrinthPlace.MOTHER_DEN);var f=lifeTrade;var owner=f.player(f.base.offset(0,0,-4));var first=f.dog(owner,false);UUID firstId=first.getUUID();
        var den=LabyrinthPlaces.base(f.origin,LabyrinthPlace.MOTHER_DEN);MotherOfStrays.build(f.server,f.level,den);f.level.getChunkSource().addRegionTicket(TicketType.PORTAL,new ChunkPos(den),3,den);
        final MotherEntity[] mother={null};final Wolf[] given={null};final UUID[] nextId={null};
        h.runAfterDelay(3,()->{
            h.assertTrue(MotherOfStrays.claimFollowingPets(owner)==1,"the original enters custody");
            owner.teleportTo(f.level,den.getX()+.5,den.getY(),den.getZ()-9.5,0,0);MotherOfStrays.onArrive(owner,LabyrinthPlace.MOTHER_DEN);
            mother[0]=f.level.getEntitiesOfClass(MotherEntity.class,new AABB(den).inflate(30)).getFirst();
            owner.setItemInHand(InteractionHand.MAIN_HAND,VignetteYields.mark(new ItemStack(Items.PAPER,2),HideAndClap.ID));
            MotherOfStrays.onEntityInteract(new PlayerInteractEvent.EntityInteract(owner,InteractionHand.MAIN_HAND,mother[0]));
            h.assertTrue(f.level.getEntity(firstId) instanceof Wolf&&owner.getMainHandItem().getCount()==1,"clicking Mother herself delivers the pet and consumes exactly one artifact");
            f.level.getEntity(firstId).discard();var second=f.dog(owner,false);nextId[0]=second.getUUID();
            h.assertTrue(MotherOfStrays.claimFollowingPets(owner)==1,"another real loss creates a new bargain");
            given[0]=EntityType.WOLF.create(f.level);given[0].moveTo(mother[0].position().add(1,0,0));given[0].setNoAi(true);given[0].setHealth(5);given[0].getPersistentData().putBoolean("HouseStray",true);f.level.addFreshEntity(given[0]);
        });
        h.runAfterDelay(9,()->{
            owner.moveTo(mother[0].position().add(0,0,1));owner.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);owner.setShiftKeyDown(true);
            MotherOfStrays.onEntityInteract(new PlayerInteractEvent.EntityInteract(owner,InteractionHand.MAIN_HAND,mother[0]));
            var collection=MotherCollection.get(f.server);var stored=collection.all().stream().filter(e->e.offeredLife).findFirst().orElseThrow();
            h.assertTrue(given[0].isRemoved()&&stored.contents.getUUID("UUID").equals(given[0].getUUID())&&stored.contents.getFloat("Health")==5,"the actual stray is kept, including its identity and health");
            h.assertTrue(f.level.getEntity(nextId[0]) instanceof Wolf&&!collection.holdsLivingPet(owner.getUUID())&&!collection.salved(),"the second original returns without granting a peaceful resolution");h.succeed();
        });
    }
    @AfterBatch(batch="trapped_player_offering") public static void cleanPlayerTrade(ServerLevel level){if(playerTrade!=null){playerTrade.close();playerTrade=null;}}
    @GameTest(template="empty",batch="trapped_player_offering",timeoutTicks=100)
    public static void nativePlayerTradeRejectsAnOpenRoomAndKillsOnlyTheTrappedExplorer(GameTestHelper h) {
        playerTrade=new Fixture(h,new BlockPos(10800,80,10800),LabyrinthPlace.MOTHER_DEN);var f=playerTrade;var owner=f.player(f.base.offset(0,0,-4));var pet=f.dog(owner,false);UUID original=pet.getUUID();
        var den=LabyrinthPlaces.base(f.origin,LabyrinthPlace.MOTHER_DEN);MotherOfStrays.build(f.server,f.level,den);f.level.getChunkSource().addRegionTicket(TicketType.PORTAL,new ChunkPos(den),3,den);
        final MotherEntity[] mother={null};final ServerPlayer[] victim={null};final Mob[] captive={null};final BlockPos[] room={null};
        h.runAfterDelay(3,()->{
            h.assertTrue(MotherOfStrays.claimFollowingPets(owner)==1,"the original pet is waiting for a real trade");
            owner.teleportTo(f.level,den.getX()+.5,den.getY(),den.getZ()-5.5,0,0);MotherOfStrays.onArrive(owner,LabyrinthPlace.MOTHER_DEN);
            mother[0]=f.level.getEntitiesOfClass(MotherEntity.class,new AABB(den).inflate(30)).getFirst();mother[0].setNoAi(true);room[0]=mother[0].blockPosition();
            closedRoom(f.level,room[0]);victim[0]=f.player(room[0].east());
            captive[0]=f.level.getEntitiesOfClass(Mob.class,new AABB(den).inflate(30),e->e.getPersistentData().hasUUID("MotherEntry")).getFirst();
            owner.moveTo(captive[0].position().add(0,0,1));owner.setShiftKeyDown(true);owner.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
            victim[0].gameMode.changeGameModeForPlayer(GameType.CREATIVE);
            h.assertTrue(!MotherOfferings.playerEligible(owner,mother[0],victim[0]),"creative players cannot be offered");victim[0].gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
            f.level.setBlock(room[0].offset(2,0,0),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);
            f.level.setBlock(room[0].offset(2,1,0),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);
            MotherOfStrays.onEntityInteract(new PlayerInteractEvent.EntityInteract(owner,InteractionHand.MAIN_HAND,captive[0]));
            h.assertTrue(victim[0].isAlive()&&MotherCollection.get(f.server).holdsLivingPet(owner.getUUID()),"an open exit refuses the player offering without taking the captive");
        });
        h.runAfterDelay(9,()->{
            closedRoom(f.level,room[0]);
            h.assertTrue(MotherOfferings.playerEligible(owner,mother[0],victim[0]),"the survival player is physically enclosed with Mother and within her reach; mother="+mother[0].position()+"; victim="+victim[0].position()+"; alive="+victim[0].isAlive()+"; removed="+victim[0].isRemoved()+"; mode="+victim[0].gameMode.getGameModeForPlayer()+"; sight="+mother[0].hasLineOfSight(victim[0])+"; enclosed="+MotherOfferings.enclosedTogether(f.level,mother[0].blockPosition(),victim[0].blockPosition()));
            MotherOfStrays.onEntityInteract(new PlayerInteractEvent.EntityInteract(owner,InteractionHand.MAIN_HAND,captive[0]));
            var collection=MotherCollection.get(f.server);
            h.assertTrue(victim[0].isDeadOrDying()&&owner.isAlive()&&collection.wasPlayerOffered(victim[0].getUUID()),"the actual trapped player dies as the payment while the trader survives; health="+victim[0].getHealth()+"; removed="+victim[0].isRemoved()+"; paid="+collection.wasPlayerOffered(victim[0].getUUID())+"; changing="+victim[0].isChangingDimension()+"; invulnerable="+victim[0].isInvulnerableTo(victim[0].damageSources().genericKill())+"; traderAlive="+owner.isAlive());
            h.assertTrue(f.level.getEntity(original) instanceof Wolf&&!collection.holdsLivingPet(owner.getUUID()),"the original pet returns only after the native player payment succeeds");h.succeed();
        });
    }
}
