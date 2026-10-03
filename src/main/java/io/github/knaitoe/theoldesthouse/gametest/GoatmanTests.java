package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import io.github.knaitoe.theoldesthouse.opening.CompanionOrders;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class GoatmanTests {
    @GameTest(template="empty") public static void woodsAppendWithoutMovingOldRoomsOrReinterpretingLeaks(GameTestHelper h){
        for(int y:new int[]{65,80,150,250}){
            var origin=new BlockPos(100,y,100);var b=LabyrinthPlaces.base(origin,LabyrinthPlace.GOATMAN);var slot=LabyrinthPlaces.slotBounds(origin,LabyrinthPlace.GOATMAN);
            h.assertTrue(slot.isInside(b.offset(-23,-1,-82))&&slot.isInside(b.offset(23,13,0)),"woods and trailer fit their native slot at manor height "+y);
        }
        h.assertTrue(LabyrinthPlace.PHONE_CANOE.slot()==30&&LabyrinthPlace.GOATMAN.slot()==31&&DoorLeakKind.CANOE.ordinal()==10&&DoorLeakKind.WOODS.ordinal()==11,"the new site and sensory hint append to the existing saved layout");
        h.assertTrue(WitnessAccount.REQUIRED==15&&WitnessAccount.Story.values().length==19,"the current nineteen playable sources require fifteen personal resolutions");h.succeed();
    }
    private static final class Fixture implements AutoCloseable {
        final GameTestHelper h;final net.minecraft.server.MinecraftServer server;final ServerLevel l;final BlockPos origin,b;
        final HouseSavedData oldHouse;final LabyrinthData oldData;final MotherCollection oldMother;final boolean keep;
        final List<ServerPlayer> players=new ArrayList<>();
        Fixture(GameTestHelper h,int coordinate){
            this.h=h;server=h.getLevel().getServer();l=HouseTestLevel.get(server);origin=new BlockPos(coordinate,80,coordinate);
            oldHouse=HouseSavedData.get(server);oldData=LabyrinthData.get(server);oldMother=MotherCollection.get(server);keep=l.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);
            var house=new HouseSavedData();house.markSpawned(origin);server.overworld().getDataStorage().set("the_oldest_house",house);
            var data=new LabyrinthData();data.setBuilt(LabyrinthBuilder.VERSION,origin);server.overworld().getDataStorage().set("the_oldest_house_labyrinth",data);
            server.overworld().getDataStorage().set("the_oldest_house_mother",new MotherCollection());
            b=LabyrinthPlaces.base(origin,LabyrinthPlace.GOATMAN);GoatmanVignette.build(server,l,b);LabyrinthBuilder.registerDoors(data,LabyrinthPlace.GOATMAN,b);IndianLakeRooms.keepLoaded(l,b,LabyrinthPlace.GOATMAN);
        }
        ServerPlayer player(){var p=h.makeMockServerPlayerInLevel();p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-.5,180,0);p.hasChangedDimension();players.add(p);return p;}
        void at(ServerPlayer p,double progress){p.moveTo(GoatmanWoods.path(progress).add(b.getX(),b.getY(),b.getZ()));}
        void indoors(ServerPlayer p){p.moveTo(b.getX()+.5,b.getY()+1,b.getZ()-58.5,180,0);}
        void run(CompoundTag r){LabyrinthData data=LabyrinthData.get(server);CompoundTag all=data.state(GoatmanVignette.ID);all.put("Run",r);data.setState(GoatmanVignette.ID,all);}
        List<GoatmanChild> children(){return l.getEntitiesOfClass(GoatmanChild.class,IndianLakeRooms.bounds(b,LabyrinthPlace.GOATMAN));}
        void waiting(List<ServerPlayer> participants,boolean extraInside,int elapsed){
            var r=GoatmanVignette.run(LabyrinthData.get(server));r.putInt("Phase",GoatmanVignette.VIGIL);r.putInt("Clock",elapsed);r.putBoolean("ExtraInside",extraInside);
            var cohort=r.getCompound("Cohort");for(var p:participants){var own=cohort.getCompound(p.getUUID().toString());own.putBoolean("Arrived",true);own.putBoolean("GirlGone",true);own.putInt("Vigil",elapsed);cohort.put(p.getUUID().toString(),own);indoors(p);}r.put("Cohort",cohort);run(r);
            GoatmanWoods.atmosphere(l,b,true);GoatmanWoods.door(l,b,false);GoatmanVignette.stage(l,b,r);
        }
        public void close(){
            for(var p:players){GoatmanVignette.depart(p);if(server.getPlayerList().getPlayers().contains(p))server.getPlayerList().remove(p);else p.discard();}
            AABB bounds=IndianLakeRooms.bounds(b,LabyrinthPlace.GOATMAN);for(var e:l.getEntitiesOfClass(Entity.class,bounds,e->e instanceof GoatmanChild||e instanceof Display||e instanceof Wolf||e instanceof net.minecraft.world.entity.item.ItemEntity))e.discard();
            for(int x=((int)bounds.minX-1)>>4;x<=((int)bounds.maxX+1)>>4;x++)for(int z=((int)bounds.minZ-1)>>4;z<=((int)bounds.maxZ+1)>>4;z++)l.getChunkSource().removeRegionTicket(TicketType.PORTAL,new ChunkPos(x,z),3,b);
            l.getGameRules().getRule(GameRules.RULE_KEEPINVENTORY).set(keep,server);
            server.overworld().getDataStorage().set("the_oldest_house",oldHouse);server.overworld().getDataStorage().set("the_oldest_house_labyrinth",oldData);server.overworld().getDataStorage().set("the_oldest_house_mother",oldMother);LabyrinthBuilder.clearAll();LabyrinthDoors.clearAll();
        }
    }
    private static Fixture path,vigil,resume,failure,upgrade;
    @AfterBatch(batch="goat_path") public static void cleanPath(ServerLevel l){if(path!=null){path.close();path=null;}}
    @AfterBatch(batch="goat_vigil") public static void cleanVigil(ServerLevel l){if(vigil!=null){vigil.close();vigil=null;}}
    @AfterBatch(batch="goat_resume") public static void cleanResume(ServerLevel l){if(resume!=null){resume.close();resume=null;}}
    @AfterBatch(batch="goat_failure") public static void cleanFailure(ServerLevel l){if(failure!=null){failure.close();failure=null;}}
    @AfterBatch(batch="goat_upgrade") public static void cleanUpgrade(ServerLevel l){if(upgrade!=null){upgrade.close();upgrade=null;}}

    @GameTest(template="empty",batch="goat_path",timeoutTicks=230)
    public static void nativeBackwardGirlMatchesWalkingSprintAndStopThenLeavesAtTheBend(GameTestHelper h){
        path=new Fixture(h,28000);var f=path;var p=f.player();p.getAttribute(Attributes.SCALE).setBaseValue(1.2);h.assertTrue(GoatmanVignette.enter(p),"the actual latecomer enrolls");
        var data=LabyrinthData.get(f.server);CompoundTag original=GoatmanVignette.run(data);UUID round=original.getUUID("Id");int wrong=original.getInt("Wrong"),tells=original.getInt("Tells");
        h.assertTrue(Integer.bitCount(tells)>=2&&Integer.bitCount(tells)<=3&&wrong>=0&&wrong<5,"one cousin receives two or three tells");
        h.assertTrue(Math.abs(p.getAttributeValue(Attributes.SCALE)-.84)<.001,"child view multiplies the existing scale instead of destroying it");
        var pet=EntityType.WOLF.create(f.l);pet.tame(p);pet.setPersistenceRequired();pet.moveTo(GoatmanWoods.path(4).add(f.b.getX(),f.b.getY(),f.b.getZ()));f.l.addFreshEntity(pet);CompanionOrders.issue(pet,p,CompanionOrders.Order.DEEPER);
        int[] tick={0};float[] stopped={0};
        h.onEachTick(()->{
            tick[0]++;double progress=tick[0]<=10?0:tick[0]<=30?(tick[0]-10)*.12:tick[0]<=50?2.4+(tick[0]-30)*.4:tick[0]<=70?10.4:Math.min(73,10.4+(tick[0]-70)*.6);f.at(p,progress);
        });
        h.runAfterDelay(8,()->h.assertTrue(f.children().stream().filter(c->!c.girl()).count()==5&&f.children().stream().filter(GoatmanChild::girl).count()==1,"five real shared cousins and the private path girl are staged"));
        h.runAfterDelay(45,()->{
            var girl=f.children().stream().filter(GoatmanChild::girl).findFirst().orElseThrow();var own=GoatmanVignette.run(LabyrinthData.get(f.server)).getCompound("Cohort").getCompound(p.getUUID().toString());
            double girlProgress=GoatmanWoods.project(girl.position().subtract(f.b.getX(),f.b.getY(),f.b.getZ())).progress();
            h.assertTrue(Math.abs(girlProgress-own.getDouble("Progress")-7)<.02&&girl.speed()<0,"sprinting retains a seven-block gap and a reversed walk phase");
            h.assertTrue(GoatmanWoods.project(pet.position().subtract(f.b.getX(),f.b.getY(),f.b.getZ())).progress()<own.getDouble("Progress"),"a DEEPER command cannot send the actual companion ahead");
        });
        h.runAfterDelay(58,()->stopped[0]=f.children().stream().filter(GoatmanChild::girl).findFirst().orElseThrow().walk(0));
        h.runAfterDelay(66,()->{var girl=f.children().stream().filter(GoatmanChild::girl).findFirst().orElseThrow();h.assertTrue(girl.speed()==0&&Math.abs(girl.walk(0)-stopped[0])<.001,"stopping also stops her reverse animation");
            var loaded=LabyrinthData.FACTORY.deserializer().apply(LabyrinthData.get(f.server).save(new CompoundTag(),f.l.registryAccess()),f.l.registryAccess());f.server.overworld().getDataStorage().set("the_oldest_house_labyrinth",loaded);
        });
        h.runAfterDelay(160,()->{
            var r=GoatmanVignette.run(LabyrinthData.get(f.server));h.assertTrue(r.getUUID("Id").equals(round)&&r.getInt("Wrong")==wrong&&r.getInt("Tells")==tells,"saved reload never rerolls the identity or tells");
            h.assertTrue(r.getCompound("Cohort").getCompound(p.getUUID().toString()).getBoolean("Arrived")&&f.children().stream().noneMatch(GoatmanChild::girl),"the trail reaches the clearing after the apparition disappears around its bend");
            h.assertTrue(GoatmanVignette.expectedCount(r)==5&&GoatmanVignette.presentCount(f.l,f.b)==6&&!WitnessAccount.has(LabyrinthData.get(f.server),p.getUUID(),WitnessAccount.Story.GOATMAN),"arrival presents the discrepancy without resolution credit");
            h.assertTrue(CompanionOrders.order(pet)==CompanionOrders.Order.DEEPER&&pet.getOwnerUUID().equals(p.getUUID()),"temporary refusal preserves the native pet and saved order");
            p.teleportTo(f.server.overworld(),100,80,100,0,0);p.hasChangedDimension();GoatmanVignette.tick(f.server);h.assertTrue(Math.abs(p.getAttributeValue(Attributes.SCALE)-1.2)<.001,"leaving restores the pre-existing scale");h.succeed();
        });
    }

    @GameTest(template="empty",batch="goat_vigil",timeoutTicks=2650)
    public static void nativeGatheringSupperAndFullDoorVigilCreditOnlyTheChildrenWhoStay(GameTestHelper h){
        vigil=new Fixture(h,28300);var f=vigil;var a=f.player();var b=f.player();h.assertTrue(GoatmanVignette.enter(a)&&GoatmanVignette.enter(b),"both native players join the same gathering");
        var r=GoatmanVignette.run(LabyrinthData.get(f.server));r.putBoolean("ExtraInside",false);f.run(r);
        var observer=f.player();observer.gameMode.changeGameModeForPlayer(GameType.SPECTATOR);f.indoors(observer);
        int[] tick={0};boolean[] counted={false},night={false};
        h.onEachTick(()->{
            tick[0]++;var current=GoatmanVignette.run(LabyrinthData.get(f.server));int phase=current.getInt("Phase");
            if(tick[0]<=94){f.at(a,Math.min(73,tick[0]*.8));f.at(b,Math.min(73,tick[0]*.8));}
            else {f.indoors(a);f.indoors(b);}
            if(phase==GoatmanVignette.GATHERING&&!counted[0]){
                counted[0]=true;h.assertTrue(GoatmanVignette.expectedCount(current)==6&&GoatmanVignette.presentCount(f.l,f.b)==7,"two real children add a plate and bunk while the group still has one extra");
                h.assertTrue(f.l.getEntitiesOfClass(Display.ItemDisplay.class,IndianLakeRooms.bounds(f.b,LabyrinthPlace.GOATMAN),e->e.getTags().contains(GoatmanWoods.PLATE)).size()==6,"the multiplayer supply count is physically staged");
            }
            if(phase==GoatmanVignette.VIGIL&&current.getInt("Clock")==100){
                night[0]=true;h.assertTrue(f.children().stream().filter(c->!c.girl()).count()==4,"the absent-extra variant really leaves four NPCs inside with two players");
                h.assertTrue(!f.l.getBlockState(f.b.offset(GoatmanWoods.DOOR)).getValue(DoorBlock.OPEN),"hammering never opens the door by itself");
                h.assertTrue(!WitnessAccount.has(LabyrinthData.get(f.server),a.getUUID(),WitnessAccount.Story.GOATMAN),"enduring the first knocks does not resolve the encounter early");
            }
            if(phase==GoatmanVignette.DAWN){
                var data=LabyrinthData.get(f.server);h.assertTrue(counted[0]&&night[0]&&WitnessAccount.has(data,a.getUUID(),WitnessAccount.Story.GOATMAN)&&WitnessAccount.has(data,b.getUUID(),WitnessAccount.Story.GOATMAN),"both present players endure the full native minute and receive their own resolution");
                h.assertTrue(!WitnessAccount.has(data,observer.getUUID(),WitnessAccount.Story.GOATMAN)&&!GoatmanVignette.childScale(observer),"a spectator receives neither credit nor forced child scale");
                h.assertTrue(!GoatmanVignette.canDeal(data,a.getUUID())&&GoatmanVignette.canDeal(data,UUID.randomUUID())&&!data.isCompleted(GoatmanVignette.ID),"a personal ending exhausts only its own explorer's encounter");
                h.assertTrue(f.children().stream().filter(c->!c.girl()).count()==4,"dawn does not reveal or resurrect an identified suspect");h.succeed();
            }
        });
    }

    @GameTest(template="empty",batch="goat_resume",timeoutTicks=100)
    public static void savedVigilPausesWithoutVisitorsAndDoesNotCreditSomeoneWhoLeft(GameTestHelper h){
        resume=new Fixture(h,28600);var f=resume;var a=f.player();var b=f.player();GoatmanVignette.enter(a);GoatmanVignette.enter(b);f.waiting(List.of(a,b),true,1170);
        var data=LabyrinthData.get(f.server);var saved=data.save(new CompoundTag(),f.l.registryAccess());var loaded=LabyrinthData.FACTORY.deserializer().apply(saved,f.l.registryAccess());f.server.overworld().getDataStorage().set("the_oldest_house_labyrinth",loaded);
        a.moveTo(100,80,100);b.moveTo(100,80,100);GoatmanVignette.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(a));
        GoatmanVignette.tick(f.server);h.assertTrue(GoatmanVignette.run(loaded).getInt("Clock")==1170&&!GoatmanVignette.childScale(a),"no visitors means the saved minute pauses, and logout removes the transient scale");
        f.indoors(a);GoatmanVignette.enter(a);GoatmanVignette.depart(b);
        h.runAfterDelay(10,()->h.assertTrue(f.children().stream().filter(c->!c.girl()).count()==5,"the present-extra variant keeps all five NPCs during hammering"));
        h.runAfterDelay(42,()->{
            var current=LabyrinthData.get(f.server);h.assertTrue(WitnessAccount.has(current,a.getUUID(),WitnessAccount.Story.GOATMAN)&&!WitnessAccount.has(current,b.getUUID(),WitnessAccount.Story.GOATMAN),"the saved vigil resumes but a departed peer gains no credit");
            int count=WitnessAccount.count(current,a.getUUID());GoatmanVignette.tick(f.server);h.assertTrue(WitnessAccount.count(current,a.getUUID())==count,"the ending cannot farm repeated resolutions");h.succeed();
        });
    }

    @GameTest(template="empty",batch="goat_failure",timeoutTicks=130)
    public static void nativeDoorOpeningTakesOriginalGearAndFollowingPetThenRespawnsInManor(GameTestHelper h){
        failure=new Fixture(h,28900);var f=failure;var a=f.player();var peer=f.player();GoatmanVignette.enter(a);GoatmanVignette.enter(peer);f.waiting(List.of(a,peer),true,200);
        f.l.getGameRules().getRule(GameRules.RULE_KEEPINVENTORY).set(true,f.server);
        var axe=new ItemStack(Items.IRON_AXE);axe.setDamageValue(37);axe.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Trail axe"));a.getInventory().setItem(4,axe.copy());
        a.getInventory().setItem(5,new ItemStack(Items.DIAMOND,3));a.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.TORCH,7));a.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.IRON_CHESTPLATE));a.containerMenu.setCarried(new ItemStack(Items.BREAD,2));
        Wolf pet=EntityType.WOLF.create(f.l);pet.tame(a);pet.setPersistenceRequired();pet.moveTo(f.b.getX()+1.5,f.b.getY()+1,f.b.getZ()-58.5);f.l.addFreshEntity(pet);CompanionOrders.issue(pet,a,CompanionOrders.Order.FOLLOW);UUID petId=pet.getUUID();
        h.runAfterDelay(6,()->{
            a.hasChangedDimension();BlockPos door=f.b.offset(GoatmanWoods.DOOR);
            // Native carried-time ownership stamps may arrive during these six real ticks.
            // Compare the complete originals at the moment of taking, including those components.
            List<ItemStack> expected=new ArrayList<>();
            for(int i=0;i<a.getInventory().getContainerSize();i++){var stack=a.getInventory().getItem(i);if(!stack.isEmpty())expected.add(stack.copy());}
            expected.add(a.containerMenu.getCarried().copy());h.assertTrue(expected.size()==5,"the actual opener still carries the five provisioned original stacks");
            var click=new PlayerInteractEvent.RightClickBlock(a,InteractionHand.OFF_HAND,door,new BlockHitResult(Vec3.atCenterOf(door),Direction.NORTH,door,false));NeoForge.EVENT_BUS.post(click);
            h.assertTrue(click.isCanceled()&&a.isDeadOrDying()&&a.getInventory().isEmpty()&&a.containerMenu.getCarried().isEmpty(),"a real door click causes native death and removes gear even with keepInventory");
            var collection=MotherCollection.get(f.server);var originals=collection.all().stream().filter(e->!e.pet&&a.getUUID().equals(e.owner)).map(e->ItemStack.parseOptional(f.l.registryAccess(),e.contents)).toList();
            h.assertTrue(originals.size()==expected.size()&&expected.stream().allMatch(wanted->originals.stream().anyMatch(s->s.getCount()==wanted.getCount()&&ItemStack.isSameItemSameComponents(s,wanted))),"all five original stacks reach actual shelves, with every live component, quantity and cursor content intact");
            h.assertTrue(collection.all().stream().anyMatch(e->e.livingClaim&&e.contents.hasUUID("UUID")&&e.contents.getUUID("UUID").equals(petId))&&pet.isRemoved(),"the existing native death hook keeps the exact following pet in living custody");
            h.assertTrue(peer.isAlive()&&!WitnessAccount.has(LabyrinthData.get(f.server),a.getUUID(),WitnessAccount.Story.GOATMAN)&&!WitnessAccount.has(LabyrinthData.get(f.server),peer.getUUID(),WitnessAccount.Story.GOATMAN),"opening takes only the opener and awards nobody an early resolution");
            var respawn=f.server.getPlayerList().respawn(a,false,Entity.RemovalReason.KILLED);f.players.add(respawn);respawn.hasChangedDimension();
            h.assertTrue(respawn.level()==f.l&&respawn.position().distanceToSqr(HideAndClap.manorRespawn(f.origin))<1&&respawn.getInventory().isEmpty()&&!GoatmanVignette.childScale(respawn),"native respawn wakes in the manor with normal scale and no duplicate inventory");
            h.assertTrue(!GoatmanVignette.personal(LabyrinthData.get(f.server),a.getUUID()).getBoolean("Respawn"),"the saved manor respawn marker clears only after the actual respawn");h.succeed();
        });
    }

    @GameTest(template="empty",batch="goat_upgrade",timeoutTicks=200)
    public static void nativeVersionSeventeenUpgradePreservesCachesActorsAndExistingEvidence(GameTestHelper h){
        upgrade=new Fixture(h,29200);var f=upgrade;var data=LabyrinthData.get(f.server);
        BlockPos junction=LabyrinthPlaces.base(f.origin,LabyrinthPlace.JUNCTION);LabyrinthBuilder.buildJunction(f.l,junction);LabyrinthLighting.buildEarlyAid(f.server,f.l,junction);
        var cache=(BarrelBlockEntity)f.l.getBlockEntity(junction.offset(LabyrinthLighting.TOM_CACHE));cache.setItem(0,ItemStack.EMPTY);cache.setItem(3,new ItemStack(Items.DIAMOND));
        BlockPos canoeBase=LabyrinthPlaces.base(f.origin,LabyrinthPlace.PHONE_CANOE);PhoneCanoe.build(f.server,f.l,canoeBase);var canoe=PhoneCanoe.stage(f.l,canoeBase);UUID canoeId=canoe.getUUID();
        CompoundTag reading=data.state(HarriganVignette.ID);reading.putBoolean("ReadingFinished",true);data.setState(HarriganVignette.ID,reading);
        UUID player=UUID.randomUUID();WitnessAccount.resolve(data,player,WitnessAccount.Story.HARRIGAN,"kept_phone");data.setBuilt(17,f.origin);
        h.assertTrue(!LabyrinthBuilder.ensureBuilt(f.server),"the old layout starts its incremental upgrade");while(LabyrinthBuilder.isCarving())LabyrinthBuilder.tick(f.server);
        h.assertTrue(data.builtVersion()==LabyrinthBuilder.VERSION&&data.door(LabyrinthPlace.GOATMAN.entryDoorId())!=null,"the trailer's physical slot and return door are appended");
        h.assertTrue(f.l.getBlockEntity(junction.offset(LabyrinthLighting.TOM_CACHE))==cache&&cache.getItem(0).isEmpty()&&cache.getItem(3).is(Items.DIAMOND),"existing finite supplies are neither rebuilt nor replenished");
        h.assertTrue(!canoe.isRemoved()&&canoe.getUUID().equals(canoeId)&&data.state(HarriganVignette.ID).getBoolean("ReadingFinished")&&WitnessAccount.has(data,player,WitnessAccount.Story.HARRIGAN),"the existing recording actor, finished dialogue and personal evidence survive unchanged");h.succeed();
    }
}
