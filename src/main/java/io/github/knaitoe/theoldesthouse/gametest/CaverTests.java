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
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CaverTests {
    private static final class Fixture implements AutoCloseable {
        final GameTestHelper h;final ServerLevel l;final BlockPos origin,b;
        final HouseSavedData oldHouse;final LabyrinthData oldData;final MotherCollection oldMother;
        final List<ServerPlayer> players=new ArrayList<>();
        Fixture(GameTestHelper h,int coordinate){this.h=h;l=HouseTestLevel.get(h.getLevel().getServer());origin=new BlockPos(coordinate,80,coordinate);
            var s=l.getServer();oldHouse=HouseSavedData.get(s);oldData=LabyrinthData.get(s);oldMother=MotherCollection.get(s);
            var house=new HouseSavedData();house.markSpawned(origin);s.overworld().getDataStorage().set("the_oldest_house",house);
            s.overworld().getDataStorage().set("the_oldest_house_labyrinth",new LabyrinthData());s.overworld().getDataStorage().set("the_oldest_house_mother",new MotherCollection());
            b=LabyrinthPlaces.base(origin,LabyrinthPlace.TED_CAVER);CaverCave.build(s,l,b);data().setBuilt(LabyrinthBuilder.VERSION,origin);IndianLakeRooms.keepLoaded(l,b,LabyrinthPlace.TED_CAVER);
        }
        LabyrinthData data(){return LabyrinthData.get(l.getServer());}
        ServerPlayer player(){var p=h.makeMockServerPlayerInLevel();p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-3.5,0,0);p.hasChangedDimension();players.add(p);return p;}
        void at(ServerPlayer p,double x,double y,double z){p.moveTo(new Vec3(b.getX()+x,b.getY()+y,b.getZ()+z));p.setDeltaMovement(Vec3.ZERO);}
        void click(ServerPlayer p,BlockPos local){var pos=b.offset(local);var e=new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,pos,new BlockHitResult(pos.getCenter(),Direction.SOUTH,pos,false));NeoForge.EVENT_BUS.post(e);h.assertTrue(e.isCanceled(),"the real registered interaction handles the authored prop");}
        void open(){var all=data().state(CaverVignette.ID);all.putInt("Work",CaverVignette.STROKES);data().setState(CaverVignette.ID,all);CaverCave.aperture(l,b,true);}
        void reload(){var loaded=LabyrinthData.FACTORY.deserializer().apply(data().save(new CompoundTag(),l.registryAccess()),l.registryAccess());l.getServer().overworld().getDataStorage().set("the_oldest_house_labyrinth",loaded);}
        @Override public void close(){CaverVignette.clearAll();for(var p:players)l.getServer().getPlayerList().remove(p);
            for(var e:l.getEntitiesOfClass(Entity.class,IndianLakeRooms.bounds(b,LabyrinthPlace.TED_CAVER),e->e instanceof Wolf||e instanceof net.minecraft.world.entity.item.ItemEntity))e.discard();
            var bounds=IndianLakeRooms.bounds(b,LabyrinthPlace.TED_CAVER);
            for(int x=((int)bounds.minX-1)>>4;x<=((int)bounds.maxX+1)>>4;x++)for(int z=((int)bounds.minZ-1)>>4;z<=((int)bounds.maxZ+1)>>4;z++)l.getChunkSource().removeRegionTicket(TicketType.PORTAL,new net.minecraft.world.level.ChunkPos(x,z),3,b);
            var s=l.getServer();s.overworld().getDataStorage().set("the_oldest_house",oldHouse);s.overworld().getDataStorage().set("the_oldest_house_labyrinth",oldData);s.overworld().getDataStorage().set("the_oldest_house_mother",oldMother);LabyrinthBuilder.clearAll();}
    }
    private static Fixture work,journals,escape,cleanup,upgrade;
    @AfterBatch(batch="caver_work") public static void cleanWork(ServerLevel l){if(work!=null){work.close();work=null;}}
    @AfterBatch(batch="caver_journals") public static void cleanJournals(ServerLevel l){if(journals!=null){journals.close();journals=null;}}
    @AfterBatch(batch="caver_escape") public static void cleanEscape(ServerLevel l){if(escape!=null){escape.close();escape=null;}}
    @AfterBatch(batch="caver_cleanup") public static void cleanCleanup(ServerLevel l){if(cleanup!=null){cleanup.close();cleanup=null;}}
    @AfterBatch(batch="caver_upgrade") public static void cleanUpgrade(ServerLevel l){if(upgrade!=null){upgrade.close();upgrade=null;}}
    @GameTest(template="empty") public static void caveAppendsAndFitsWithoutReinterpretingSavedSlots(GameTestHelper h){
        for(int y:new int[]{65,80,150,250}){var origin=new BlockPos(100,y,100);var b=LabyrinthPlaces.base(origin,LabyrinthPlace.TED_CAVER);var slot=LabyrinthPlaces.slotBounds(origin,LabyrinthPlace.TED_CAVER);
            h.assertTrue(slot.isInside(b.offset(-9,-4,-60))&&slot.isInside(b.offset(9,8,0)),"the low cave stays inside its native slot at manor height "+y);}
        h.assertTrue(LabyrinthPlace.TED_CAVER.slot()==32&&LabyrinthPlace.GOATMAN.slot()==31&&DoorLeakKind.STONE.ordinal()==12&&DoorLeakKind.WOODS.ordinal()==11,"new room and dry stone hint append after all old indices");
        var buffer=io.netty.buffer.Unpooled.buffer();try{var payload=new io.github.knaitoe.theoldesthouse.network.CaverCrawlPayload(true,-400,150,-200);var codec=io.github.knaitoe.theoldesthouse.network.CaverCrawlPayload.STREAM_CODEC;
            codec.encode(buffer,payload);h.assertTrue(codec.decode(buffer).equals(payload),"native client crawl synchronization preserves its bounded signed origin");}finally{buffer.release();}h.succeed();
    }
    @GameTest(template="empty",batch="caver_work",timeoutTicks=700)
    public static void actualPickaxeWorkIsTimedSharedAndSavedWithoutReplenishingTools(GameTestHelper h){
        work=new Fixture(h,30800);var f=work;var p=f.player();f.at(p,.5,-3,-21.5);var at=f.b.offset(CaverCave.APERTURE);
        h.runAfterDelay(8,()->{f.click(p,CaverCave.APERTURE);h.assertTrue(f.data().state(CaverVignette.ID).getInt("Work")==0&&!f.l.getBlockState(at).isAir(),"bare hands cannot remove the aperture");p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.IRON_PICKAXE));});
        for(int i=0;i<CaverVignette.STROKES;i++)h.runAfterDelay(10+i*CaverVignette.STROKE_INTERVAL,()->{
            var event=new PlayerInteractEvent.LeftClickBlock(p,at,Direction.SOUTH,PlayerInteractEvent.LeftClickBlock.Action.START);NeoForge.EVENT_BUS.post(event);
            h.assertTrue(event.isCanceled(),"native mining is replaced with an authored stroke");int count=f.data().state(CaverVignette.ID).getInt("Work");
            f.click(p,CaverCave.APERTURE);h.assertTrue(f.data().state(CaverVignette.ID).getInt("Work")==count,"same-tick right/left spam cannot accelerate work");
        });
        h.runAfterDelay(620,()->{h.assertTrue(f.data().state(CaverVignette.ID).getInt("Work")==24&&f.l.getBlockState(at).isAir(),"timed native work opens the actual one-block passage");
            h.assertTrue(WitnessAccount.count(f.data(),p.getUUID())==0,"opening the passage is not an ending");
            var barrel=(BarrelBlockEntity)f.l.getBlockEntity(f.b.offset(CaverCave.CACHE));barrel.clearContent();f.reload();CaverCave.build(f.l.getServer(),f.l,f.b);
            h.assertTrue(f.l.getBlockState(at).isAir()&&((BarrelBlockEntity)f.l.getBlockEntity(f.b.offset(CaverCave.CACHE))).isEmpty(),"saved work survives and rebuilding does not issue a second tool cache");h.succeed();});
    }
    @GameTest(template="empty",batch="caver_journals",timeoutTicks=100)
    public static void nativeJournalMenusArePersonalFiniteAndDoNotBorrowWitnessCredit(GameTestHelper h){
        journals=new Fixture(h,31100);var f=journals;var p=f.player();var peer=f.player();
        h.runAfterDelay(8,()->{f.at(p,-2.5,0,-3.5);f.click(p,CaverCave.JOURNAL);var menu=(CaverVignette.JournalMenu)p.containerMenu;
            h.assertTrue(menu.book().get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size()==2&&!menu.clickMenuButton(p,999),"the native opening notebook has practical context and bounds forged pages");
            h.assertTrue(menu.clickMenuButton(p,3)&&!menu.clickMenuButton(p,3),"the actual Take Book button grants one original");p.closeContainer();
            var original=p.getInventory().items.stream().filter(s->CaverVignette.ownedJournal(s,p.getUUID())).findFirst().orElseThrow().copy();
            f.at(p,.5,-3,-21.5);p.getInventory().selected=1;p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.IRON_PICKAXE));f.click(p,CaverCave.APERTURE);
            var grown=p.getInventory().items.stream().filter(s->CaverVignette.ownedJournal(s,p.getUUID())).findFirst().orElseThrow();
            h.assertTrue(original.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size()==2&&grown.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size()==3,"only the holder's original grows after their own work");
            peer.getInventory().add(grown.copy());f.at(peer,-2.5,0,-3.5);f.click(peer,CaverCave.JOURNAL);
            h.assertTrue(((CaverVignette.JournalMenu)peer.containerMenu).book().get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size()==2,"a second native reader sees their own opening instead of the other explorer's progress");
            h.assertTrue(MotherCollection.vignetteOffering(grown)&&WitnessAccount.count(f.data(),peer.getUUID())==0,"a legitimate tradeable field notebook transfers no personal credit");
            f.reload();f.at(p,-2.5,0,-3.5);f.click(p,CaverCave.JOURNAL);h.assertTrue(!((CaverVignette.JournalMenu)p.containerMenu).clickMenuButton(p,3),"finite collection survives native saved-data reload");h.succeed();});
    }
    @GameTest(template="empty",batch="caver_escape",timeoutTicks=600)
    public static void nativeCrawlChamberAndPhysicalReturnCreditOnlyTheExplorer(GameTestHelper h){
        escape=new Fixture(h,31400);var f=escape;f.open();var p=f.player();var peer=f.player();f.at(peer,4.5,-3,-53.5);
        int[] phase={0},clock={0};
        h.onEachTick(()->{
            clock[0]++;if(clock[0]<10)return;
            if(phase[0]==0){f.at(p,.5,-3,-21.5);p.setShiftKeyDown(true);CaverVignette.playerTick(p);phase[0]=1;return;}
            if(phase[0]==1){CaverVignette.playerTick(p);
                if(!CaverVignette.crawling(p)&&p.getZ()-f.b.getZ()< -34.1){p.setShiftKeyDown(false);phase[0]=2;clock[0]=0;return;}
                h.assertTrue(p.getBbHeight()<.7,"native crawling uses the actual short collision box at "+p.position()+"; pose "+p.getPose()+"; forced "+p.getForcedPose());
                p.move(MoverType.SELF,new Vec3(0,0,-.14));return;}
            if(phase[0]==2){if(clock[0]<3)return;CaverVignette.playerTick(p);h.assertTrue(CaverVignette.personal(f.data(),p.getUUID()).getBoolean("Squeezed")&&!CaverVignette.crawling(p),"the full physical squeeze reaches a standing chamber");
                f.at(p,-2.5,-3,-39.5);f.click(p,CaverCave.MARK);f.at(p,2.5,-3,-42.5);f.click(p,CaverCave.STONE);
                h.assertTrue(f.l.getBlockState(f.b.offset(CaverCave.STONE)).isAir(),"examining the actual stone opens its hidden route");f.at(p,4.5,-3,-53.5);phase[0]=3;clock[0]=0;return;}
            if(phase[0]==3){CaverVignette.playerTick(p);h.assertTrue(WitnessAccount.count(f.data(),p.getUUID())==0&&WitnessAccount.count(f.data(),peer.getUUID())==0,"the chamber and an observing peer have no premature credit");
                if(!CaverVignette.personal(f.data(),p.getUUID()).getBoolean("Pursuit"))return;
                f.reload();f.at(p,.5,-3,-35.5);p.setShiftKeyDown(true);phase[0]=4;return;}
            if(phase[0]==4){CaverVignette.playerTick(p);p.move(MoverType.SELF,new Vec3(0,0,.14));if(p.getZ()-f.b.getZ()> -21){p.setShiftKeyDown(false);f.at(p,-.5,-3,-10.5);phase[0]=5;}return;}
            if(phase[0]==5){h.assertTrue(p.onClimbable(),"the return uses the real native ladder beside the line");p.move(MoverType.SELF,new Vec3(0,.14,0));
                if(p.getY()-f.b.getY()>=0){f.at(p,.5,0,-7.5);phase[0]=6;}return;}
            CaverVignette.playerTick(p);
            h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.TED_CAVER)&&!WitnessAccount.has(f.data(),peer.getUUID(),WitnessAccount.Story.TED_CAVER),"retracing the squeeze and climbing above the rope credits only that explorer");
            h.assertTrue(!CaverVignette.crawling(p)&&p.getForcedPose()==null&&!CaverVignette.canDeal(f.data(),p.getUUID())&&CaverVignette.canDeal(f.data(),peer.getUUID()),"native pose restores and the cave remains available to the peer");
            int count=WitnessAccount.count(f.data(),p.getUUID());CaverVignette.playerTick(p);h.assertTrue(WitnessAccount.count(f.data(),p.getUUID())==count,"repeat escape cannot duplicate evidence");h.succeed();
        });
    }
    @GameTest(template="empty",batch="caver_cleanup",timeoutTicks=100)
    public static void transientCrawlRestoresOnLogoutSpectatorAndNativeDeathWhilePetIdentityStays(GameTestHelper h){
        cleanup=new Fixture(h,31700);var f=cleanup;f.open();var p=f.player();
        h.runAfterDelay(8,()->{p.setForcedPose(Pose.CROUCHING);p.setPose(Pose.CROUCHING);f.at(p,.5,-3,-25.5);CaverVignette.playerTick(p);
            h.assertTrue(CaverVignette.crawling(p)&&p.getForcedPose()==Pose.SWIMMING,"the authored tight passage temporarily replaces the prior forced pose");
            NeoForge.EVENT_BUS.post(new PlayerEvent.PlayerLoggedOutEvent(p));h.assertTrue(!CaverVignette.crawling(p)&&p.getForcedPose()==Pose.CROUCHING,"logout restores the prior native forced pose");
            f.at(p,.5,0,-7.5);p.setForcedPose(null);p.setPose(Pose.STANDING);
            var pet=EntityType.WOLF.create(f.l);pet.tame(p);pet.setPersistenceRequired();pet.moveTo(p.position());f.l.addFreshEntity(pet);CompanionOrders.issue(pet,p,CompanionOrders.Order.DEEPER);
            UUID id=pet.getUUID();float health=pet.getHealth();f.at(p,.5,-3,-25.5);
            h.assertTrue(CaverVignette.companion(pet,p)&&pet.getZ()>f.b.getZ()-8.2&&pet.getUUID().equals(id)&&pet.getHealth()==health&&pet.getOwnerUUID().equals(p.getUUID())&&CompanionOrders.order(pet)==CompanionOrders.Order.DEEPER,"refusal leaves the real pet at the landing without rewriting identity or orders");
        });
        h.runAfterDelay(12,()->{CaverVignette.playerTick(p);p.gameMode.changeGameModeForPlayer(GameType.SPECTATOR);CaverVignette.playerTick(p);
            h.assertTrue(!CaverVignette.crawling(p)&&p.getForcedPose()==null&&WitnessAccount.count(f.data(),p.getUUID())==0,"native spectator mode ends crawling without credit");p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);});
        h.runAfterDelay(16,()->{CaverVignette.playerTick(p);h.assertTrue(CaverVignette.crawling(p),"a resumed alive player uses the tight passage again");
            var all=f.data().state(CaverVignette.ID);var people=all.getCompound("Players");var own=people.getCompound(p.getUUID().toString());own.putBoolean("Pursuit",true);own.putInt("ReturnCrawl",40);own.putInt("JournalStage",6);people.put(p.getUUID().toString(),own);all.put("Players",people);f.data().setState(CaverVignette.ID,all);
            p.hurt(p.damageSources().genericKill(),Float.MAX_VALUE);h.assertTrue(!p.isAlive()&&!CaverVignette.crawling(p)&&p.getForcedPose()==null,"an actual native death clears the transient pose");
            var after=CaverVignette.personal(f.data(),p.getUUID());h.assertTrue(!after.getBoolean("Pursuit")&&after.getInt("ReturnCrawl")==0&&!after.getBoolean("Escaped")&&CaverVignette.journal(f.data(),p.getUUID()).get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size()==7,"dying cannot turn a later arrival into an escape or erase the written observations");h.succeed();});
    }
    @GameTest(template="empty",batch="caver_upgrade",timeoutTicks=100)
    public static void oldLayoutAppendsCaveWithoutErasingFurnitureActorsCachesOrEvidence(GameTestHelper h){
        upgrade=new Fixture(h,32000);var f=upgrade;var d=f.data();var p=f.player();
        BlockPos old=LabyrinthPlaces.base(f.origin,LabyrinthPlace.HARRIGAN).offset(3,0,-4);f.l.setBlock(old,Blocks.BARREL.defaultBlockState(),3);var cache=(BarrelBlockEntity)f.l.getBlockEntity(old);cache.setItem(4,new ItemStack(Items.DIAMOND,3));
        var read=new CompoundTag();read.putBoolean("ReadingFinished",true);d.setState(HarriganVignette.ID,read);
        var random=new CompoundTag();random.putUUID("SavedRound",UUID.randomUUID());d.setState(GoatmanVignette.ID,random);
        WitnessAccount.resolve(d,p.getUUID(),WitnessAccount.Story.HARRIGAN,"kept_phone");var paper=new CompoundTag();paper.putString("Original","kept");d.setState(HouseMarginalia.ID,paper);
        d.setState(CaverVignette.ID,new CompoundTag());d.setBuilt(19,f.origin);
        h.assertTrue(!LabyrinthBuilder.ensureBuilt(f.l.getServer()),"the existing version-19 world begins an append upgrade");while(LabyrinthBuilder.isCarving())LabyrinthBuilder.tick(f.l.getServer());
        h.assertTrue(d.builtVersion()==20&&d.door(LabyrinthPlace.TED_CAVER.entryDoorId())!=null&&f.l.getBlockState(f.b.offset(CaverCave.APERTURE)).is(Blocks.CRACKED_DEEPSLATE_BRICKS),"the new physical cave and its return door are appended");
        h.assertTrue(f.l.getBlockEntity(old)==cache&&cache.getItem(0).isEmpty()&&cache.getItem(4).getCount()==3&&read.equals(d.state(HarriganVignette.ID))&&random.equals(d.state(GoatmanVignette.ID))&&paper.equals(d.state(HouseMarginalia.ID))&&WitnessAccount.has(d,p.getUUID(),WitnessAccount.Story.HARRIGAN),"old storage identity, finite cache, reading, run and personal evidence remain");h.succeed();
    }
}
