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
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID+"_caver")
@PrefixGameTestTemplate(false)
public final class CaverTests {
    static final class Fixture implements AutoCloseable {
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
        CompoundTag own(ServerPlayer p){return CaverVignette.personal(data(),p.getUUID());}
        void edit(ServerPlayer p,java.util.function.Consumer<CompoundTag> change){var all=data().state(CaverVignette.ID);var people=all.getCompound("Players");var o=people.getCompound(p.getUUID().toString());change.accept(o);people.put(p.getUUID().toString(),o);all.put("Players",people);data().setState(CaverVignette.ID,all);}
        /** Native chunk and entity sections for the deep cave, as the saved-cave reshaping requires. */
        boolean deepLoaded(){for(int x=(b.getX()-10)>>4;x<=(b.getX()+11)>>4;x++)for(int z=(b.getZ()-61)>>4;z<=(b.getZ()-15)>>4;z++)if(!l.hasChunk(x,z)||!l.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(x,z)))return false;return true;}
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
    private static Fixture work,journals,escape,cleanup,upgrade,route,breath,line,repair;
    @AfterBatch(batch="caver_route") public static void cleanRoute(ServerLevel l){if(route!=null){route.close();route=null;}}
    @AfterBatch(batch="caver_breath") public static void cleanBreath(ServerLevel l){if(breath!=null){breath.close();breath=null;}}
    @AfterBatch(batch="caver_line") public static void cleanLine(ServerLevel l){if(line!=null){line.close();line=null;}}
    @AfterBatch(batch="caver_repair") public static void cleanRepair(ServerLevel l){if(repair!=null){repair.close();repair=null;}}
    @AfterBatch(batch="caver_work") public static void cleanWork(ServerLevel l){if(work!=null){work.close();work=null;}}
    @AfterBatch(batch="caver_journals") public static void cleanJournals(ServerLevel l){if(journals!=null){journals.close();journals=null;}}
    @AfterBatch(batch="caver_escape") public static void cleanEscape(ServerLevel l){if(escape!=null){escape.close();escape=null;}}
    @AfterBatch(batch="caver_cleanup") public static void cleanCleanup(ServerLevel l){if(cleanup!=null){cleanup.close();cleanup=null;}}
    @AfterBatch(batch="caver_upgrade") public static void cleanUpgrade(ServerLevel l){if(upgrade!=null){upgrade.close();upgrade=null;}}
    @GameTest(template="empty") public static void caveAppendsAndFitsWithoutReinterpretingSavedSlots(GameTestHelper h){
        for(int y:new int[]{65,80,150,250}){var origin=new BlockPos(100,y,100);var b=LabyrinthPlaces.base(origin,LabyrinthPlace.TED_CAVER);var slot=LabyrinthPlaces.slotBounds(origin,LabyrinthPlace.TED_CAVER);
            h.assertTrue(slot.isInside(b.offset(-9,-4,-60))&&slot.isInside(b.offset(9,8,0)),"the low cave stays inside its native slot at manor height "+y);}
        h.assertTrue(LabyrinthPlace.TED_CAVER.slot()==32&&LabyrinthPlace.GOATMAN.slot()==31&&DoorLeakKind.STONE.ordinal()==12&&DoorLeakKind.WOODS.ordinal()==11,"new room and dry stone hint append after all old indices");
        var buffer=io.netty.buffer.Unpooled.buffer();try{var payload=new io.github.knaitoe.theoldesthouse.network.CaverCrawlPayload(true,-400,150,-200,CaverVignette.IN_PURSUED);var codec=io.github.knaitoe.theoldesthouse.network.CaverCrawlPayload.STREAM_CODEC;
            codec.encode(buffer,payload);h.assertTrue(codec.decode(buffer).equals(payload),"native client crawl synchronization preserves its bounded signed origin and the draught on the crawling body");}finally{buffer.release();}h.succeed();
    }
    @GameTest(template="empty",batch="caver_work",timeoutTicks=760)
    public static void actualPickaxeWorkIsTimedSharedAndSavedWithoutReplenishingTools(GameTestHelper h){
        work=new Fixture(h,30800);var f=work;var p=f.player();f.at(p,.5,-3,-21.5);var at=f.b.offset(CaverCave.APERTURE);
        h.runAfterDelay(8,()->{f.click(p,CaverCave.APERTURE);h.assertTrue(f.data().state(CaverVignette.ID).getInt("Work")==0&&CaverCave.isRubble(f.l.getBlockState(at)),"bare hands cannot remove the packed rubble");p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.IRON_PICKAXE));});
        for(int i=0;i<CaverVignette.STROKES;i++){int stroke=i;h.runAfterDelay(10+i*CaverVignette.STROKE_INTERVAL,()->{
            int front=CaverCave.front(f.l,f.b);h.assertTrue(front==stroke/CaverVignette.PER_BLOCK,"five strokes take out each block in turn, from the mouth inward: front "+front+" at stroke "+stroke);
            f.at(p,.5,-3,-21.5-front);f.click(p,CaverCave.rubbleCell(front));int count=f.data().state(CaverVignette.ID).getInt("Work");
            int next=CaverCave.front(f.l,f.b);if(next>=0)f.click(p,CaverCave.rubbleCell(next));
            h.assertTrue(f.data().state(CaverVignette.ID).getInt("Work")==count,"same-tick right/left spam cannot accelerate work");
            if(stroke==CaverVignette.PER_BLOCK-1)h.assertTrue(f.l.getBlockState(at).isAir()&&CaverCave.isRubble(f.l.getBlockState(f.b.offset(CaverCave.rubbleCell(1)))),"the first block is out and the rest is still packed behind it");
        });}
        h.runAfterDelay(20+CaverVignette.STROKES*CaverVignette.STROKE_INTERVAL,()->{
            h.assertTrue(f.data().state(CaverVignette.ID).getInt("Work")==CaverVignette.STROKES&&CaverCave.front(f.l,f.b)<0,"timed native work opens the whole one-block passage");
            for(int i=0;i<CaverCave.RUBBLE;i++)h.assertTrue(f.l.getBlockState(f.b.offset(CaverCave.rubbleCell(i))).isAir(),"rubble block "+i+" has really gone");
            h.assertTrue(WitnessAccount.count(f.data(),p.getUUID())==0,"opening the passage is not an ending");
            var barrel=(BarrelBlockEntity)f.l.getBlockEntity(f.b.offset(CaverCave.CACHE));barrel.clearContent();f.reload();CaverCave.build(f.l.getServer(),f.l,f.b);
            h.assertTrue(f.l.getBlockState(at).isAir()&&CaverCave.front(f.l,f.b)<0&&((BarrelBlockEntity)f.l.getBlockEntity(f.b.offset(CaverCave.CACHE))).isEmpty(),"saved work survives and rebuilding does not issue a second tool cache");h.succeed();});
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
    @GameTest(template="empty",batch="caver_escape",timeoutTicks=1500)
    public static void nativeCrawlChamberAndPhysicalReturnCreditOnlyTheExplorer(GameTestHelper h){
        escape=new Fixture(h,31400);var f=escape;f.open();var p=f.player();var peer=f.player();f.at(peer,4.5,-3,-53.5);
        int[] phase={0},clock={0};
        h.onEachTick(()->{
            clock[0]++;if(clock[0]<10)return;
            if(phase[0]==0){
                // The reader ties their own line off at the ladder before going down.
                f.at(p,.5,0,-8.5);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.STRING,2));f.click(p,CaverCave.CHAIN);
                h.assertTrue(f.own(p).getBoolean("LineTied")&&p.getMainHandItem().getCount()==1,"tying the line spends one real string");
                f.click(p,CaverCave.CHAIN);h.assertTrue(p.getMainHandItem().getCount()==1,"a tied line is never tied twice");p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
                f.at(p,.5,-3,-21.5);p.setShiftKeyDown(true);CaverVignette.playerTick(p);phase[0]=1;return;}
            if(phase[0]==1){CaverVignette.playerTick(p);
                if(!CaverVignette.crawling(p)&&p.getZ()-f.b.getZ()< -34.1){p.setShiftKeyDown(false);phase[0]=2;clock[0]=0;return;}
                h.assertTrue(p.getBbHeight()<.7,"native crawling uses the actual short collision box at "+p.position()+"; pose "+p.getPose()+"; forced "+p.getForcedPose());
                p.move(MoverType.SELF,new Vec3(0,0,-.14));return;}
            if(phase[0]==2){if(clock[0]<3)return;CaverVignette.playerTick(p);h.assertTrue(f.own(p).getBoolean("Squeezed")&&!CaverVignette.crawling(p),"the full physical squeeze reaches a standing chamber");
                h.assertTrue(f.own(p).getLongArray("LinePath").length>=6,"the line paid out behind the reader through the squeeze: "+f.own(p).getLongArray("LinePath").length);
                f.at(p,-2.5,-3,-39.5);f.click(p,CaverCave.MARK);f.at(p,4.5,-3,-41.5);
                CaverVignette.setBreath(f.l.getServer(),10);f.click(p,CaverCave.STONE);
                h.assertTrue(!f.l.getBlockState(f.b.offset(CaverCave.STONE)).isAir()&&!f.own(p).getBoolean("StoneSeen"),"the stone stays in its seat while the cave breathes out");
                CaverVignette.setBreath(f.l.getServer(),CaverVignette.INHALE_START+5);f.click(p,CaverCave.STONE);
                h.assertTrue(f.l.getBlockState(f.b.offset(CaverCave.STONE)).isAir()&&f.own(p).getBoolean("StoneSeen"),"on the in-breath the actual stone rolls aside");
                f.at(p,4.5,-3,-53.5);phase[0]=3;clock[0]=0;return;}
            if(phase[0]==3){CaverVignette.playerTick(p);h.assertTrue(WitnessAccount.count(f.data(),p.getUUID())==0&&WitnessAccount.count(f.data(),peer.getUUID())==0,"the chamber and an observing peer have no premature credit");
                if(!f.own(p).getBoolean("Pursuit")){h.assertTrue(clock[0]<CaverVignette.DEEP_TICKS+5,"the wait in the low chamber ends: "+f.own(p).getInt("DeepTicks"));return;}
                h.assertTrue(clock[0]>=CaverVignette.DEEP_TICKS-5&&f.own(peer).getInt("DeepTicks")==0,"the long wait is the explorer's own, counted only after their stone: "+clock[0]);
                var untied=f.own(p);untied.putBoolean("LineTied",false);
                h.assertTrue(f.own(p).getInt("LineFlash")>0&&CaverVignette.push(f.own(p),-1)==CaverVignette.IN_HELD&&CaverVignette.push(untied,-1)==CaverVignette.IN_PURSUED,"the line draws tight and holds against the in-breath that drags an untied reader");
                f.reload();f.at(p,.5,-3,-35.5);p.setShiftKeyDown(true);phase[0]=4;return;}
            if(phase[0]==4){CaverVignette.playerTick(p);p.move(MoverType.SELF,new Vec3(0,0,.14));if(p.getZ()-f.b.getZ()> -21){p.setShiftKeyDown(false);f.at(p,-.5,-3,-9.5);phase[0]=5;}return;}
            if(phase[0]==5){h.assertTrue(p.onClimbable(),"the return uses the real native ladder beside the line");p.move(MoverType.SELF,new Vec3(0,.14,0));
                if(p.getY()-f.b.getY()>=0){f.at(p,.5,0,-7.5);phase[0]=6;}return;}
            CaverVignette.playerTick(p);
            h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.TED_CAVER)&&!WitnessAccount.has(f.data(),peer.getUUID(),WitnessAccount.Story.TED_CAVER),"retracing the squeeze and climbing above the rope credits only that explorer");
            h.assertTrue(!CaverVignette.crawling(p)&&p.getForcedPose()==null&&!CaverVignette.canDeal(f.data(),p.getUUID())&&CaverVignette.canDeal(f.data(),peer.getUUID()),"native pose restores and the cave remains available to the peer");
            int count=WitnessAccount.count(f.data(),p.getUUID());CaverVignette.playerTick(p);h.assertTrue(WitnessAccount.count(f.data(),p.getUUID())==count,"repeat escape cannot duplicate evidence");
            // Later trips through the House carry two echoes and the last undated page; the cave is not reopened.
            int pages=CaverVignette.journal(f.data(),p.getUUID()).get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size();
            for(int i=0;i<6;i++)CaverVignette.onArrive(p,LabyrinthPlace.HARRIGAN);
            h.assertTrue(pages==8&&CaverVignette.journal(f.data(),p.getUUID()).get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size()==8,"the echoes take later trips, not one");
            CaverVignette.onArrive(p,LabyrinthPlace.HARRIGAN);CaverVignette.onArrive(p,LabyrinthPlace.HARRIGAN);
            h.assertTrue(f.own(p).getInt("EchoArrivals")==7&&CaverVignette.journal(f.data(),p.getUUID()).get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size()==9&&WitnessAccount.count(f.data(),p.getUUID())==count,"the seventh later arrival adds the last page and nothing else");h.succeed();
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
        h.assertTrue(!LabyrinthBuilder.ensureBuilt(f.l.getServer()),"the existing version-19 world begins an append upgrade");LabyrinthBuilder.finishGameTest(f.l.getServer());
        h.assertTrue(d.builtVersion()==LabyrinthBuilder.VERSION&&d.door(LabyrinthPlace.TED_CAVER.entryDoorId())!=null&&f.l.getBlockState(f.b.offset(CaverCave.APERTURE)).is(LabyrinthRegistry.CAVE_RUBBLE.get()),"the new physical cave and its return door are appended");
        h.assertTrue(f.l.getBlockEntity(old)==cache&&cache.getItem(0).isEmpty()&&cache.getItem(4).getCount()==3&&read.equals(d.state(HarriganVignette.ID))&&random.equals(d.state(GoatmanVignette.ID))&&paper.equals(d.state(HouseMarginalia.ID))&&WitnessAccount.has(d,p.getUUID(),WitnessAccount.Story.HARRIGAN),"old storage identity, finite cache, reading, run and personal evidence remain");h.succeed();
    }

    /** Every cell a crouching reader's body fits on real support, reached on foot from {@code start}: steps up one, drops up to three. */
    private static Set<BlockPos> reachable(Fixture f,BlockPos start){
        var seen=new HashSet<BlockPos>();var open=new ArrayDeque<BlockPos>();seen.add(start.immutable());open.add(start.immutable());var room=LabyrinthPlace.TED_CAVER.room();
        while(!open.isEmpty()){var at=open.poll();
            for(var d:Direction.Plane.HORIZONTAL)for(int dy:new int[]{0,1,-1,-2,-3}){var next=at.relative(d).above(dy);var r=next.subtract(f.b);
                if(r.getX()<room.minX()||r.getX()>room.maxX()||r.getZ()<room.minZ()||r.getZ()>room.maxZ()||r.getY()<room.minY())continue;
                if(dy>0&&!clear(f.l,at,1.01,2.49))continue;
                if(dy<0&&!clear(f.l,at.relative(d),.01,1.49))continue;
                if(fits(f.l,next)){if(seen.add(next.immutable()))open.add(next.immutable());break;}}}
        return seen;
    }
    private static boolean clear(ServerLevel l,BlockPos at,double from,double to){double x=at.getX()+.5,y=at.getY(),z=at.getZ()+.5;return l.noCollision(new AABB(x-.3,y+from,z-.3,x+.3,y+to,z+.3));}
    private static boolean fits(ServerLevel l,BlockPos at){double x=at.getX()+.5,y=at.getY(),z=at.getZ()+.5;
        return l.noCollision(new AABB(x-.3,y+.01,z-.3,x+.3,y+1.49,z+.3))&&!l.noCollision(new AABB(x-.3,y-.05,z-.3,x+.3,y-.01,z+.3));}
    private static boolean behindStone(Fixture f,BlockPos at){var r=at.subtract(f.b);return r.getZ()<=-44&&r.getX()>=3;}

    @GameTest(template="empty",batch="caver_route",timeoutTicks=100)
    public static void onlyTheRolledStoneOpensAWalkingRouteToTheCrouchHighChamber(GameTestHelper h){
        route=new Fixture(h,33400);var f=route;
        h.runAfterDelay(4,()->{
            var start=f.b.offset(0,-3,-36);
            var before=reachable(f,start);
            h.assertTrue(before.size()>40&&before.stream().noneMatch(at->behindStone(f,at)),"with the stone in its seat, no walking or crouching route from the bowl reaches the passage or chamber behind it");
            CaverCave.stone(f.l,f.b,true);var after=reachable(f,start);
            h.assertTrue(after.contains(f.b.offset(4,-3,-47))&&after.contains(f.b.offset(5,-3,-54))&&after.contains(f.b.offset(2,-3,-57)),"once the stone has rolled aside the passage and the whole low chamber are reachable on foot");
            var cell=f.b.offset(5,-3,-54);double x=cell.getX()+.5,y=cell.getY(),z=cell.getZ()+.5;
            h.assertTrue(!f.l.noCollision(new AABB(x-.3,y+.01,z-.3,x+.3,y+1.8,z+.3))&&f.l.noCollision(new AABB(x-.3,y+.01,z-.3,x+.3,y+1.49,z+.3)),"the low chamber has room to crouch and none to stand");
            var passage=f.b.offset(4,-3,-47);x=passage.getX()+.5;y=passage.getY();z=passage.getZ()+.5;
            h.assertTrue(f.l.noCollision(new AABB(x-.3,y+.01,z-.3,x+.3,y+1.8,z+.3))&&!f.l.noCollision(new AABB(x-.3,y+2.01,z-.3,x+.3,y+2.5,z+.3)),"the passage behind the stone is two blocks high and no more");
            h.succeed();});
    }
    @GameTest(template="empty",batch="caver_breath",timeoutTicks=200)
    public static void oneSharedBreathPausesWhenEmptyAndTheStoneGivesOnlyOnTheInhale(GameTestHelper h){
        breath=new Fixture(h,33700);var f=breath;var s=f.l.getServer();ServerPlayer[] who=new ServerPlayer[2];
        h.runAfterDelay(4,()->CaverVignette.setBreath(s,50));
        h.runAfterDelay(24,()->{h.assertTrue(CaverVignette.breathTick(s)==50&&CaverVignette.breathing(s)==1,"the cave does not breathe while nobody is in it");
            who[0]=f.player();who[1]=f.player();f.at(who[0],4.5,-3,-41.5);f.at(who[1],4.5,-3,-40.5);
            f.edit(who[0],o->o.putBoolean("MarkRead",true));f.edit(who[1],o->o.putBoolean("MarkRead",true));});
        h.runAfterDelay(44,()->{var p=who[0];var peer=who[1];int t=CaverVignette.breathTick(s);
            h.assertTrue(t>50&&t<=72,"one clock advances for everyone inside: "+t);
            CaverVignette.setBreath(s,30);f.click(p,CaverCave.STONE);
            h.assertTrue(!f.own(p).getBoolean("StoneSeen")&&!f.l.getBlockState(f.b.offset(CaverCave.STONE)).isAir(),"while the cave breathes out the stone is pressed into its seat");
            CaverVignette.setBreath(s,CaverVignette.EXHALE_END+5);h.assertTrue(CaverVignette.breathing(s)==0,"a still moment follows the out-breath");f.click(p,CaverCave.STONE);
            h.assertTrue(!f.l.getBlockState(f.b.offset(CaverCave.STONE)).isAir(),"in the still moment it does not move either");
            CaverVignette.setBreath(s,CaverVignette.INHALE_START+10);f.click(p,CaverCave.STONE);
            h.assertTrue(f.l.getBlockState(f.b.offset(CaverCave.STONE)).isAir()&&f.l.getBlockState(f.b.offset(6,-3,-43)).is(Blocks.SMOOTH_BASALT)&&f.own(p).getBoolean("StoneSeen")&&!f.own(peer).getBoolean("StoneSeen"),"on the in-breath it rolls aside, and only the hand that moved it has seen it");
            CaverVignette.setBreath(s,20);f.click(peer,new BlockPos(6,-3,-43));
            h.assertTrue(f.own(peer).getBoolean("StoneSeen"),"a later reader examines the rolled stone themselves, at any breath");
            f.reload();h.assertTrue(f.data().state(CaverVignette.ID).getBoolean("StoneMoved")&&f.data().state(CaverVignette.ID).getInt("Breath")==20&&WitnessAccount.count(f.data(),p.getUUID())==0,"the moved stone and the shared clock survive a native reload and confer no ending");
            h.succeed();});
    }
    @GameTest(template="empty",batch="caver_line",timeoutTicks=200)
    public static void eachReaderTiesTheirOwnLineAndItHoldsAgainstTheInhale(GameTestHelper h){
        line=new Fixture(h,34000);var f=line;ServerPlayer[] who=new ServerPlayer[2];
        h.runAfterDelay(6,()->{var p=f.player();var peer=f.player();who[0]=p;who[1]=peer;f.at(p,.5,0,-8.5);f.at(peer,-.5,0,-8.5);
            p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.STRING,2));f.click(p,CaverCave.CHAIN);
            h.assertTrue(f.own(p).getBoolean("LineTied")&&p.getMainHandItem().getCount()==1&&!f.own(peer).getBoolean("LineTied"),"one reader's own string ties only their line");
            peer.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.STRING));f.click(peer,CaverCave.LADDER);
            h.assertTrue(f.own(peer).getBoolean("LineTied")&&peer.getMainHandItem().isEmpty(),"the ladder takes a peer's own line too");});
        for(int i=0;i<8;i++){double z=-10.5-i*1.5;h.runAfterDelay(8+i,()->f.at(who[0],.5,-3,z));}
        h.runAfterDelay(20,()->{var p=who[0];var peer=who[1];
            h.assertTrue(f.own(p).getLongArray("LinePath").length>=4&&f.own(peer).getLongArray("LinePath").length==1,"the line pays out behind the reader who walks, and only theirs: "+f.own(p).getLongArray("LinePath").length);
            var o=new CompoundTag();
            h.assertTrue(CaverVignette.push(o,1)==CaverVignette.OUT&&CaverVignette.push(o,-1)==CaverVignette.IN&&CaverVignette.push(o,0)==0,"before the long wait the draught is gentle both ways");
            o.putBoolean("Pursuit",true);h.assertTrue(CaverVignette.push(o,-1)==CaverVignette.IN_PURSUED&&CaverVignette.push(o,1)==CaverVignette.OUT_PURSUED,"on the way out the in-breath drags hard and the out-breath helps");
            o.putBoolean("LineTied",true);h.assertTrue(CaverVignette.push(o,-1)==CaverVignette.IN_HELD&&Math.abs(CaverVignette.IN_HELD)<Math.abs(CaverVignette.IN_PURSUED)/3,"a tied line holds against the drag");
            o.putBoolean("Escaped",true);h.assertTrue(CaverVignette.push(o,-1)==CaverVignette.IN,"once out, nothing pursues");
            f.reload();h.assertTrue(f.own(p).getLongArray("LinePath").length>=4&&f.own(p).getBoolean("LineTied"),"the line survives a native reload");h.succeed();});
    }
    /** The cave as layouts 20 to 40 carved it: the open back of the bowl, the taller passage and chamber, one cracked block. */
    private static void legacy(Fixture f){
        var air=Blocks.AIR.defaultBlockState();
        for(int z=-43;z>=-47;z--)for(int x=3;x<=6;x++)for(int y=-3;y<=5;y++)if(x*x+(z+41)*(z+41)+((y+3)*(y+3))/2<52)f.l.setBlock(f.b.offset(x,y,z),air,2);
        for(int x=4;x<=5;x++)for(int z=-50;z<=-43;z++)f.l.setBlock(f.b.offset(x,-1,z),air,2);
        for(int x=2;x<=7;x++)for(int z=-57;z<=-50;z++)for(int y=-3;y<=1;y++)f.l.setBlock(f.b.offset(x,y,z),air,2);
        f.l.setBlock(f.b.offset(5,1,-52),Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,2),2);
        CaverCave.stone(f.l,f.b,false);
        f.l.setBlock(f.b.offset(CaverCave.APERTURE),Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState(),2);
        for(int i=1;i<CaverCave.RUBBLE;i++)f.l.setBlock(f.b.offset(CaverCave.rubbleCell(i)),air,2);
    }
    @GameTest(template="empty",batch="caver_repair",timeoutTicks=300)
    public static void anOlderCaveIsReshapedOnceKeepingWorkAndTorchesAndWaitingForCamerasAndBodies(GameTestHelper h){
        repair=new Fixture(h,34300);var f=repair;
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.deepLoaded(),"wait for native chunks and entity sections in the deep cave")).thenExecute(()->{
            legacy(f);var d=f.data();var state=d.state(CaverVignette.ID);state.putInt("Work",12);d.setState(CaverVignette.ID,state);
            var done=d.state(CaverRedesign.STATE);done.remove(Long.toString(f.b.asLong()));d.setState(CaverRedesign.STATE,done);
            h.assertTrue(reachable(f,f.b.offset(0,-3,-36)).stream().anyMatch(at->behindStone(f,at)),"the older cave really could be walked round its stone");
            var p=f.player();var torch=f.b.offset(3,-3,-44);f.l.setBlock(torch,Blocks.TORCH.defaultBlockState(),3);
            var placed=d.state(CaverVignette.ID);var torches=placed.getCompound("Torches");torches.putUUID(Long.toString(torch.asLong()),p.getUUID());placed.put("Torches",torches);d.setState(CaverVignette.ID,placed);
            f.at(p,.5,-3,-40.5);
            h.assertTrue(!CaverRedesign.repair(f.l,f.b)&&f.l.getBlockState(torch).is(Blocks.TORCH)&&f.l.getBlockState(f.b.offset(4,-2,-54)).isAir(),"a reader in the deep cave sees nothing change");
            f.at(p,.5,0,-3.5);var stand=new ArmorStand(f.l,f.b.getX()+4.5,f.b.getY()-3,f.b.getZ()-54.5);f.l.addFreshEntity(stand);
            h.assertTrue(!CaverRedesign.repair(f.l,f.b)&&f.l.getBlockState(torch).is(Blocks.TORCH)&&f.l.getBlockState(f.b.offset(4,-2,-54)).isAir()&&f.l.getBlockState(f.b.offset(3,-3,-45)).isAir(),"nothing changes, anywhere, while new rock would close through a body in the chamber");
            stand.discard();
            h.assertTrue(CaverRedesign.repair(f.l,f.b),"the reshaping finishes once the chamber is clear and unwatched");
            var barrel=(BarrelBlockEntity)f.l.getBlockEntity(f.b.offset(CaverCave.CACHE));
            h.assertTrue(!f.l.getBlockState(torch).isAir()&&!f.l.getBlockState(torch).is(Blocks.TORCH)&&barrel.getItem(2).is(Items.TORCH)&&barrel.getItem(2).getCount()==7&&CaverVignette.torchOwner(f.data(),torch)==null,"a torch standing where rock returns goes back to the camp barrel");
            h.assertTrue(f.l.getBlockState(f.b.offset(4,-2,-54)).is(Blocks.TUFF_SLAB)&&!f.l.getBlockState(f.b.offset(4,-1,-54)).isAir()&&f.l.getBlockState(f.b.offset(5,-3,-54)).is(Blocks.LIGHT)&&!f.l.getBlockState(f.b.offset(5,1,-52)).is(Blocks.LIGHT),"the chamber gets its low ceiling and keeps a faint light");
            h.assertTrue(!f.l.getBlockState(f.b.offset(4,-1,-46)).isAir()&&!f.l.getBlockState(f.b.offset(3,-3,-45)).isAir()&&!f.l.getBlockState(f.b.offset(4,-1,-43)).isAir(),"the passage and the bowl's back close round the stone");
            h.assertTrue(f.l.getBlockState(f.b.offset(CaverCave.rubbleCell(0))).isAir()&&f.l.getBlockState(f.b.offset(CaverCave.rubbleCell(1))).isAir()&&CaverCave.isRubble(f.l.getBlockState(f.b.offset(CaverCave.rubbleCell(2))))&&CaverCave.front(f.l,f.b)==2&&f.data().state(CaverVignette.ID).getInt("Work")==12,"saved strokes keep the two blocks they had earned; the rest of the crack is packed");
            h.assertTrue(reachable(f,f.b.offset(0,-3,-36)).stream().noneMatch(at->behindStone(f,at)),"after the reshaping the stone is the only way on");
            f.reload();h.assertTrue(CaverRedesign.done(f.data(),f.b)&&CaverRedesign.repair(f.l,f.b),"the saved checkpoint survives reload and never repeats");
            // An older cave that had already been opened stays open, with its work counted as complete.
            var open=f.data().state(CaverVignette.ID);open.putInt("Work",24);f.data().setState(CaverVignette.ID,open);var again=f.data().state(CaverRedesign.STATE);again.remove(Long.toString(f.b.asLong()));f.data().setState(CaverRedesign.STATE,again);
            for(int i=0;i<CaverCave.RUBBLE;i++)f.l.setBlock(f.b.offset(CaverCave.rubbleCell(i)),Blocks.AIR.defaultBlockState(),2);
            h.assertTrue(CaverRedesign.repair(f.l,f.b)&&CaverCave.front(f.l,f.b)<0&&f.data().state(CaverVignette.ID).getInt("Work")==CaverVignette.STROKES,"an already opened crack is never packed again");
        }).thenSucceed();
    }
}
