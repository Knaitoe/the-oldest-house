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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID) @PrefixGameTestTemplate(false)
public final class HouseNpcTests {
    private static final class Fixture implements AutoCloseable {
        final GameTestHelper h;final ServerLevel l;final BlockPos origin,b;final HouseSavedData oldHouse;final LabyrinthData oldData;
        final List<ServerPlayer> players=new ArrayList<>();
        Fixture(GameTestHelper h,int coordinate){this.h=h;var s=h.getLevel().getServer();l=HouseTestLevel.get(s);origin=new BlockPos(coordinate,80,coordinate);
            oldHouse=HouseSavedData.get(s);oldData=LabyrinthData.get(s);var house=new HouseSavedData();house.markSpawned(origin);
            s.overworld().getDataStorage().set("the_oldest_house",house);s.overworld().getDataStorage().set("the_oldest_house_labyrinth",new LabyrinthData());
            b=LabyrinthPlaces.base(origin,LabyrinthPlace.HOLLOWAY_CAMP);IndianLakeRooms.keepLoaded(l,b,LabyrinthPlace.HOLLOWAY_CAMP);
            var bounds=IndianLakeRooms.bounds(b,LabyrinthPlace.HOLLOWAY_CAMP);for(int x=((int)bounds.minX)>>4;x<=((int)bounds.maxX)>>4;x++)for(int z=((int)bounds.minZ)>>4;z<=((int)bounds.maxZ)>>4;z++)l.getChunk(x,z);
            HollowayCamp.build(l,b);data().setBuilt(LabyrinthBuilder.VERSION,origin);LabyrinthBuilder.registerDoors(data(),LabyrinthPlace.HOLLOWAY_CAMP,b);
        }
        LabyrinthData data(){return LabyrinthData.get(l.getServer());}CompoundTag own(ServerPlayer p){return HollowayVignette.personal(data(),p.getUUID());}
        ServerPlayer player(){return player(null);}
        ServerPlayer player(UUID identity){var p=h.makeMockServerPlayerInLevel();if(identity!=null)p.setUUID(identity);p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
            p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-3.5,180,0);p.hasChangedDimension();players.add(p);HollowayVignette.enter(p);return p;}
        void at(ServerPlayer p,double x,double z){p.moveTo(b.getX()+x,b.getY(),b.getZ()+z);p.setDeltaMovement(Vec3.ZERO);p.resetFallDistance();}
        void click(ServerPlayer p,BlockPos relative){var at=b.offset(relative);at(p,relative.getX()+.5,relative.getZ()+.5);
            var event=new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,at,new BlockHitResult(at.getCenter(),Direction.UP,at,false));NeoForge.EVENT_BUS.post(event);h.assertTrue(event.isCanceled(),"the registered native scene interaction handles the block");}
        HollowayVignette.CacheMenu cache(ServerPlayer p){click(p,HollowayCamp.CACHE);h.assertTrue(p.containerMenu instanceof HollowayVignette.CacheMenu,"the real cache menu intercepts actual takes");return (HollowayVignette.CacheMenu)p.containerMenu;}
        void loot(ServerPlayer p,int slot){var menu=cache(p);h.assertTrue(!menu.quickMoveStack(p,slot).isEmpty(),"native shift-click really removes a supply");p.closeContainer();}
        void returnForHunt(ServerPlayer p){at(p,0,19);HollowayVignette.depart(p);at(p,0,-3.5);HollowayVignette.enter(p);h.assertTrue(own(p).getBoolean("Run"),"the next physical visit starts this looter's run");}
        HouseHuman actor(){return HollowayVignette.ensureActor(l,b);}
        void reload(){var loaded=LabyrinthData.FACTORY.deserializer().apply(data().save(new CompoundTag(),l.registryAccess()),l.registryAccess());l.getServer().overworld().getDataStorage().set("the_oldest_house_labyrinth",loaded);}
        @Override public void close(){HollowayVignette.clearAll();LabyrinthBuilder.clearAll();for(var p:players){p.closeContainer();if(l.getServer().getPlayerList().getPlayers().contains(p))l.getServer().getPlayerList().remove(p);else p.discard();}
            for(var entity:List.copyOf(l.getEntitiesOfClass(Entity.class,IndianLakeRooms.bounds(b,LabyrinthPlace.HOLLOWAY_CAMP))))entity.discard();
            var bounds=IndianLakeRooms.bounds(b,LabyrinthPlace.HOLLOWAY_CAMP);for(int x=((int)bounds.minX)>>4;x<=((int)bounds.maxX)>>4;x++)for(int z=((int)bounds.minZ)>>4;z<=((int)bounds.maxZ)>>4;z++)l.getChunkSource().removeRegionTicket(TicketType.PORTAL,new net.minecraft.world.level.ChunkPos(x,z),3,b);
            var s=l.getServer();s.overworld().getDataStorage().set("the_oldest_house",oldHouse);s.overworld().getDataStorage().set("the_oldest_house_labyrinth",oldData);}
    }
    private static Fixture cache,hunt,resume,torches,guards,upgrade;
    @AfterBatch(batch="npc_cache") public static void c1(ServerLevel l){if(cache!=null){cache.close();cache=null;}}
    @AfterBatch(batch="npc_hunt") public static void c2(ServerLevel l){if(hunt!=null){hunt.close();hunt=null;}}
    @AfterBatch(batch="npc_resume") public static void c3(ServerLevel l){if(resume!=null){resume.close();resume=null;}}
    @AfterBatch(batch="npc_torches") public static void c4(ServerLevel l){if(torches!=null){torches.close();torches=null;}}
    @AfterBatch(batch="npc_guards") public static void c5(ServerLevel l){if(guards!=null){guards.close();guards=null;}}
    @AfterBatch(batch="npc_upgrade") public static void c6(ServerLevel l){if(upgrade!=null){upgrade.close();upgrade=null;}}

    @GameTest(template="empty",batch="npc_cache") public static void simultaneousNativeCacheMenusArmOnlyThePlayerWhoTakesSupplies(GameTestHelper h){
        cache=new Fixture(h,41000);var f=cache;var a=f.player();var b=f.player();var id=f.actor().getUUID();
        var ma=f.cache(a);var mb=f.cache(b);h.assertTrue(!f.own(a).getBoolean("Looted")&&!f.own(b).getBoolean("Looted"),"opening either live menu is not theft");
        ma.clicked(0,0,ClickType.PICKUP,a);
        h.assertTrue(f.own(a).getBoolean("Looted")&&!f.own(a).getBoolean("Run")&&!f.own(b).getBoolean("Looted"),"an actual normal click arms only the taker, on a future visit");
        h.assertTrue(mb.getSlot(0).getItem().isEmpty()&&a.containerMenu.getCarried().getCount()==16,"both native menus share the finite barrel while the taker has the original cursor stack");
        a.closeContainer();b.closeContainer();f.returnForHunt(a);
        h.assertTrue(!HollowayVignette.pursued(b)&&f.actor().getUUID().equals(id)&&f.l.getEntitiesOfClass(HouseHuman.class,IndianLakeRooms.bounds(f.b,LabyrinthPlace.HOLLOWAY_CAMP)).size()==1,"a peer staying in the camp neither advances nor duplicates the shared character");
        var barrel=(BarrelBlockEntity)f.l.getBlockEntity(f.b.offset(HollowayCamp.CACHE));h.assertTrue(barrel.getItem(0).isEmpty(),"a next visit never replenishes looted supplies");
        barrel.clearContent();var late=f.player();f.click(late,HollowayCamp.JOURNAL);var survey=(HollowayVignette.SurveyMenu)late.containerMenu;
        h.assertTrue(!f.own(late).getBoolean("Looted")&&survey.clickMenuButton(late,3)&&!survey.clickMenuButton(late,3)&&f.own(late).getBoolean("Looted"),"a late explorer takes one personal survey through the native book menu, even after every shared supply is gone");
        h.assertTrue(barrel.isEmpty(),"the late-player path never refills the shared barrel");h.succeed();
    }
    @GameTest(template="empty",batch="npc_hunt",timeoutTicks=350) public static void twoPlayersEscapeTheSharedHunterButMustEachPullTheLatch(GameTestHelper h){
        hunt=new Fixture(h,41400);var f=hunt;var a=f.player();var b=f.player();var observer=f.player();f.loot(a,0);f.loot(b,1);f.returnForHunt(a);f.returnForHunt(b);
        f.at(a,0,-18);f.at(b,1,-18);int[] clock={0};
        h.onEachTick(()->{
            clock[0]++;HollowayVignette.playerTick(a);HollowayVignette.playerTick(b);
            if(clock[0]==90){h.assertTrue(f.own(a).getInt("ArenaTicks")==80&&f.own(b).getInt("ArenaTicks")==80,"both people remain in the actual first arena");a.setHealth(20);b.setHealth(20);f.at(a,0,-32);f.at(b,1,-32);}
            if(clock[0]==180){h.assertTrue(f.own(a).getInt("Arena")==2&&f.own(b).getInt("Arena")==2,"both cross into and endure the actual second arena");a.setHealth(20);b.setHealth(20);f.at(a,-7,-58);f.at(b,-6,-58);}
            if(clock[0]==275){
                h.assertTrue(f.own(a).getInt("Arena")==3&&f.own(a).getInt("ArenaTicks")==80&&f.own(a).getBoolean("Seen")&&f.own(b).getBoolean("Seen"),"each has personally traversed all three arenas and seen the shared native hunter");
                f.at(a,-7,-66);a.setShiftKeyDown(true);f.click(a,HollowayCamp.LATCH);
                h.assertTrue(WitnessAccount.has(f.data(),a.getUUID(),WitnessAccount.Story.HOLLOWAY)&&!WitnessAccount.has(f.data(),b.getUUID(),WitnessAccount.Story.HOLLOWAY),"one latch pull credits only its own explorer");
                var shield=a.getInventory().items.stream().filter(s->s.is(Items.SHIELD)).findFirst().orElseThrow();b.getInventory().add(shield.copy());
                h.assertTrue(!WitnessAccount.has(f.data(),b.getUUID(),WitnessAccount.Story.HOLLOWAY),"borrowing the shield cannot borrow the resolution");
                int items=a.getInventory().items.stream().mapToInt(ItemStack::getCount).sum();f.click(a,HollowayCamp.LATCH);
                h.assertTrue(items==a.getInventory().items.stream().mapToInt(ItemStack::getCount).sum(),"repeat latch pulls do not refill personal rewards");
                b.setShiftKeyDown(true);f.click(b,HollowayCamp.LATCH);h.assertTrue(WitnessAccount.has(f.data(),b.getUUID(),WitnessAccount.Story.HOLLOWAY),"the second explorer can finish the same shared scene independently");
                observer.setShiftKeyDown(true);f.click(observer,HollowayCamp.LATCH);
                h.assertTrue(!WitnessAccount.has(f.data(),observer.getUUID(),WitnessAccount.Story.HOLLOWAY)&&observer.getHealth()==20,"a present non-looter is neither a pursuit target nor a credited escape");
                h.assertTrue(!HollowayVignette.canDeal(f.data(),a.getUUID())&&HollowayVignette.canDeal(f.data(),UUID.randomUUID())&&f.actor().isAlive(),"personal completion exhausts only its own door and leaves the core character alive");h.succeed();
            }
        });
    }
    @GameTest(template="empty",batch="npc_resume",timeoutTicks=170) public static void offlineTimeAndSavedReloadPreserveTheRunAndActorIdentity(GameTestHelper h){
        resume=new Fixture(h,41800);var f=resume;var a=f.player();f.loot(a,0);f.returnForHunt(a);f.at(a,0,-18);var id=f.actor().getUUID();
        h.runAfterDelay(35,()->{
            int before=f.own(a).getInt("ArenaTicks");h.assertTrue(before>0&&before<80,"a real present run has begun");var uuid=a.getUUID();
            NeoForge.EVENT_BUS.post(new PlayerEvent.PlayerLoggedOutEvent(a));a.remove(Entity.RemovalReason.UNLOADED_WITH_PLAYER);f.reload();
            h.runAfterDelay(50,()->{h.assertTrue(HollowayVignette.personal(f.data(),uuid).getInt("ArenaTicks")==before,"offline native ticks confer no progress");
                var joined=f.player(uuid);f.at(joined,0,-18);h.assertTrue(f.own(joined).getInt("Visits")==2&&f.own(joined).getInt("ArenaTicks")==before&&f.own(joined).getBoolean("Run"),"joining in the room resumes the same visit, without resetting a saved pursuit");
                h.assertTrue(f.actor().getUUID().equals(id),"a save/reload and rejoin preserve the one native shared actor");
                h.runAfterDelay(10,()->{h.assertTrue(f.own(joined).getInt("ArenaTicks")>before&&!WitnessAccount.has(f.data(),joined.getUUID(),WitnessAccount.Story.HOLLOWAY),"only resumed play advances it, without awarding an unfinished escape");h.succeed();});
            });
        });
    }
    @GameTest(template="empty",batch="npc_torches") public static void searchMarkersBelongToTheirNativeTorchPlacer(GameTestHelper h){
        torches=new Fixture(h,42200);var f=torches;var a=f.player();var b=f.player();var pa=f.b.offset(-2,0,-18);var pb=f.b.offset(2,0,-20);
        for(var pair:List.of(Map.entry(a,pa),Map.entry(b,pb))){f.at(pair.getKey(),pair.getValue().getX()-f.b.getX()+.5,pair.getValue().getZ()-f.b.getZ()+.5);f.l.setBlock(pair.getValue(),Blocks.TORCH.defaultBlockState(),3);
            var event=new BlockEvent.EntityPlaceEvent(BlockSnapshot.create(f.l.dimension(),f.l,pair.getValue()),Blocks.STONE.defaultBlockState(),pair.getKey());NeoForge.EVENT_BUS.post(event);h.assertTrue(!event.isCanceled(),"the registered labyrinth protection permits actual torches in the hunt rooms");}
        h.assertTrue(pa.equals(HollowayVignette.torchTarget(f.l,a))&&pb.equals(HollowayVignette.torchTarget(f.l,b)),"a peer's placed light cannot overwrite another explorer's search marker");
        f.l.setBlock(pa,Blocks.AIR.defaultBlockState(),3);h.assertTrue(HollowayVignette.torchTarget(f.l,a)==null&&pb.equals(HollowayVignette.torchTarget(f.l,b)),"removed torches no longer guide the hunter");
        var forbidden=new BlockEvent.EntityPlaceEvent(BlockSnapshot.create(f.l.dimension(),f.l,pb),Blocks.STONE.defaultBlockState(),a);forbidden.setCanceled(true);HollowayVignette.placed(forbidden);
        h.assertTrue(HollowayVignette.torchTarget(f.l,a)==null,"canceled native placements never leave a phantom search marker");h.succeed();
    }
    @GameTest(template="empty",batch="npc_guards") public static void observersEarlyLatchPullsAndDeathsCannotResolveTheHunt(GameTestHelper h){
        guards=new Fixture(h,42600);var f=guards;var a=f.player();f.loot(a,0);f.returnForHunt(a);a.setShiftKeyDown(true);f.click(a,HollowayCamp.LATCH);
        h.assertTrue(!WitnessAccount.has(f.data(),a.getUUID(),WitnessAccount.Story.HOLLOWAY)&&a.getInventory().items.stream().noneMatch(s->s.is(Items.SHIELD)),"skipping the native arenas cannot take a reward");
        a.gameMode.changeGameModeForPlayer(GameType.SPECTATOR);HollowayVignette.playerTick(a);h.assertTrue(!HollowayVignette.pursued(a),"an observer is excluded from pursuit and progression");
        a.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);f.at(a,0,-3);HollowayVignette.enter(a);HollowayVignette.death(new net.neoforged.neoforge.event.entity.living.LivingDeathEvent(a,f.l.damageSources().generic()));
        h.assertTrue(!f.own(a).getBoolean("Run")&&f.own(a).getBoolean("Looted")&&!WitnessAccount.has(f.data(),a.getUUID(),WitnessAccount.Story.HOLLOWAY),"death ends the attempt while preserving the theft that arms another visit");h.succeed();
    }
    @GameTest(template="empty",batch="npc_upgrade",timeoutTicks=150) public static void layoutTwentyOneAppendsTheCampWithoutRefillingOrReplacingOldScenes(GameTestHelper h){
        upgrade=new Fixture(h,43000);var f=upgrade;var old=f.b.offset(30,0,0);f.l.setBlock(old,Blocks.BARREL.defaultBlockState(),3);var container=f.l.getBlockEntity(old);((BarrelBlockEntity)container).setItem(0,new ItemStack(Items.DIAMOND,3));
        var harrigan=LabyrinthPlaces.base(f.origin,LabyrinthPlace.HARRIGAN);HarriganVignette.build(f.l.getServer(),f.l,harrigan);
        var bodies=f.l.getEntitiesOfClass(ArmorStand.class,IndianLakeRooms.bounds(harrigan,LabyrinthPlace.HARRIGAN));var body=bodies.getFirst();var id=body.getUUID();
        var head=body.getItemBySlot(EquipmentSlot.HEAD).copy();head.remove(DataComponents.CUSTOM_DATA);body.setItemSlot(EquipmentSlot.HEAD,head);
        var prior=new CompoundTag();prior.putBoolean("TicketVisible",true);prior.putInt("Visit",1);f.data().setState(HarriganVignette.ID,prior);f.data().setBuilt(21,f.origin);
        LabyrinthBuilder.ensureBuilt(f.l.getServer());
        h.runAfterDelay(100,()->{
            h.assertTrue(f.data().builtVersion()==22&&f.l.getBlockEntity(old)==container&&((BarrelBlockEntity)container).getItem(0).getCount()==3,"an append preserves existing container identity and contents");
            h.assertTrue(body.isAlive()&&body.getUUID().equals(id)&&HarriganAppearance.variant(body)==1&&f.data().state(HarriganVignette.ID).equals(prior),"legacy Harrigan receives a native tracked human skin in place, without changing actor identity or dialogue");
            var saved=body.saveWithoutId(new CompoundTag());var restored=new ArmorStand(f.l,body.getX(),body.getY(),body.getZ());restored.load(saved);
            h.assertTrue(HarriganAppearance.variant(restored)==1&&restored.getUUID().equals(id),"the client-visible appearance component persists with the native actor");
            body.discard();for(var e:bodies)if(e!=body)e.discard();h.succeed();
        });
    }
    @GameTest(template="empty") public static void newCampFitsWithoutMovingAnyOlderSlots(GameTestHelper h){
        for(int y:new int[]{65,80,150,250}){var origin=new BlockPos(100,y,100);var b=LabyrinthPlaces.base(origin,LabyrinthPlace.HOLLOWAY_CAMP);var bounds=LabyrinthPlaces.slotBounds(origin,LabyrinthPlace.HOLLOWAY_CAMP);var room=LabyrinthPlace.HOLLOWAY_CAMP.room();
            h.assertTrue(b!=null&&bounds.isInside(b.offset(room.minX(),room.minY(),room.minZ()))&&bounds.isInside(b.offset(room.maxX(),room.maxY(),room.maxZ())),"the new native scene fits at a supported manor height "+y);}
        h.assertTrue(LabyrinthPlace.HOLLOWAY_CAMP.slot()==39&&LabyrinthPlace.KAREN_ROOM.slot()==38&&LabyrinthPlace.HARRIGAN.slot()==9&&WitnessAccount.REQUIRED==13,"old stable scene slots remain and the seventeenth resolved source requires thirteen");h.succeed();
    }
}
