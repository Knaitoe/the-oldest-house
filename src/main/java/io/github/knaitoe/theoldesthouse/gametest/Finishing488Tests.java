package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID+"_playtest488") @PrefixGameTestTemplate(false)
public final class Finishing488Tests {
    private static Fixture active;
    private static final class Fixture implements AutoCloseable {
        final GameTestHelper h;final ServerLevel l;final LabyrinthPlace place;final BlockPos origin,b;
        final NativeTestChunks chunks=new NativeTestChunks();final List<ServerPlayer> players=new ArrayList<>();final List<Entity> extras=new ArrayList<>();
        final HouseSavedData oldHouse;final LabyrinthData oldData;
        Fixture(GameTestHelper h,LabyrinthPlace p,int coord){this.h=h;place=p;l=HouseTestLevel.get(h.getLevel().getServer(),NovelRooms.dimension(p));origin=new BlockPos(coord,80,coord);b=LabyrinthPlaces.base(origin,p);
            oldHouse=HouseSavedData.get(l.getServer());oldData=LabyrinthData.get(l.getServer());var house=new HouseSavedData();house.markSpawned(origin);var store=l.getServer().overworld().getDataStorage();store.set("the_oldest_house",house);store.set("the_oldest_house_labyrinth",new LabyrinthData());
            d().setBuilt(LabyrinthBuilder.VERSION,origin);chunks.hold(l,SceneReview.area(b,p));active=this;}
        LabyrinthData d(){return LabyrinthData.get(l.getServer());}
        void put(int x,int y,int z,Block block){l.setBlock(b.offset(x,y,z),block.defaultBlockState(),3);}
        ServerPlayer player(String name){var p=NativeTestPlayers.survival(h,name);p.setNoGravity(true);players.add(p);return p;}
        void at(ServerPlayer p,int x,int z){p.teleportTo(l,b.getX()+x+.5,b.getY(),b.getZ()+z+.5,180,0);p.connection.resetPosition();p.hasChangedDimension();}
        void away(ServerPlayer p){p.teleportTo(h.getLevel(),.5,100,.5,0,0);p.connection.resetPosition();}
        void reload(){l.getServer().overworld().getDataStorage().set("the_oldest_house_labyrinth",LabyrinthData.FACTORY.deserializer().apply(d().save(new CompoundTag(),l.registryAccess()),l.registryAccess()));}
        void reset(){d().setState(PlaytestScenes0488.STATE,new CompoundTag());}
        @Override public void close(){if(active!=this)return;NovelVignettes.clearAll();LiteraryVignettes.clearAll();players.forEach(NativeTestPlayers::remove);extras.forEach(Entity::discard);chunks.close();var store=l.getServer().overworld().getDataStorage();store.set("the_oldest_house",oldHouse);store.set("the_oldest_house_labyrinth",oldData);active=null;}
    }
    @AfterBatch(batch="finish488_plain") public static void plainDone(ServerLevel ignored){if(active!=null)active.close();}
    @AfterBatch(batch="finish488_archive") public static void archiveDone(ServerLevel ignored){if(active!=null)active.close();}
    @AfterBatch(batch="finish488_lighting") public static void lightsDone(ServerLevel ignored){if(active!=null)active.close();}
    @AfterBatch(batch="finish488_camp") public static void campDone(ServerLevel ignored){if(active!=null)active.close();}

    @GameTest(template="empty",batch="finish488_plain",timeoutTicks=400)
    public static void fieldCasePreservesExactSourceAndFiniteNativeReadingThroughReload(GameTestHelper h){
        var f=new Fixture(h,LabyrinthPlace.PLAIN,642000);h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native plain sections are ready")).thenExecute(()->{
            var at=f.b.offset(NovelRooms.APOLOGY);f.l.setBlock(at.below(),Blocks.SANDSTONE.defaultBlockState(),3);f.l.setBlock(at,Blocks.LECTERN.defaultBlockState(),3);
            var original=NovelTexts.apology();original.set(DataComponents.CUSTOM_NAME,Component.literal("Existing apology"));((LecternBlockEntity)f.l.getBlockEntity(at)).setBook(original);
            var p=f.player("field_case_reader");f.at(p,-4,-5);h.assertTrue(!PlaytestScenes0488.repair(f.l,f.b,f.place,true),"the actual reader postpones replacement");f.away(p);
            h.assertTrue(PlaytestScenes0488.repair(f.l,f.b,f.place,true),"the vacant case migration completes");
            h.assertTrue(f.l.getBlockState(at).is(HouseBlocks.VIGNETTE_DETAIL.get())&&f.l.getBlockState(at).getValue(VignetteDetailBlock.KIND)==VignetteDetailBlock.Kind.PLAIN_NOTES&&ItemStack.matches(original,PlaytestScenes0488.plainOriginal(f.l,f.b)),"the custom case retains every original book component");
            f.reload();f.at(p,-4,-5);NovelVignettes.onArrive(p,f.place);
            NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,at,new BlockHitResult(at.getCenter(),Direction.SOUTH,at,false)));
            h.assertTrue(p.containerMenu instanceof NovelVignettes.NovelBookMenu,"the actual field case opens the existing native reading menu");
            var own=NovelVignettes.personal(f.d(),p.getUUID());h.assertTrue(ItemStack.matches(original,ItemStack.parseOptional(f.l.registryAccess(),own.getCompound("Original_Apology"))),"the reader's original is exact after native interaction and reload");
            h.assertTrue(!WitnessAccount.has(f.d(),p.getUUID(),WitnessAccount.Story.PLAIN),"reading the note cannot replace the actual camera resolution");p.closeContainer();f.away(p);
            f.l.setBlock(at,Blocks.AIR.defaultBlockState(),3);h.assertTrue(PlaytestScenes0488.repair(f.l,f.b,f.place,true)&&f.l.getBlockState(at).isAir(),"the saved checkpoint never refills a subsequently removed case");
            for(int z=-60;z<=17;z++)h.assertTrue(SceneClock.time(SceneClock.at(f.origin,f.b.offset(0,0,z),HouseDimensions.OUTSIDE),0,18000)==6000,"the whole actual approach and plain use daylight while native time is night");
        }).thenSucceed();
    }

    @GameTest(template="empty",batch="finish488_archive",timeoutTicks=400)
    public static void denseWritingAndStoneBowlKeepOriginalsEditsAndLivingResidents(GameTestHelper h){
        var f=new Fixture(h,LabyrinthPlace.ZAMPANO_COURTYARD,643000);h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native archive sections are ready")).thenExecute(()->{
            for(int x=-11;x<=11;x++)for(int y=0;y<=6;y++){f.put(x,y,-39,NovelRegistry.PAPER.get());f.put(x,y,-38,Blocks.AIR);}
            f.put(0,1,-7,Blocks.WATER_CAULDRON);f.put(-5,0,-30,Blocks.LECTERN);var lectern=(LecternBlockEntity)f.l.getBlockEntity(f.b.offset(NovelRooms.ARCHIVE_DESK));lectern.setBook(NovelTexts.archive());var book=lectern.getBook().copy();
            f.put(0,2,-38,Blocks.DIAMOND_BLOCK);var cat=EntityType.CAT.create(f.l);cat.setNoAi(true);cat.setNoGravity(true);cat.moveTo(Vec3.atBottomCenterOf(f.b.offset(CourtyardFountain.BOWL)));f.l.addFreshEntity(cat);f.extras.add(cat);var id=cat.getUUID();
            h.assertTrue(!PlaytestScenes0488.repair(f.l,f.b,f.place,true),"a real body postpones bowl collision changes");cat.moveTo(Vec3.atBottomCenterOf(f.b.offset(8,0,-12)));
            h.assertTrue(PlaytestScenes0488.repair(f.l,f.b,f.place,true),"the safe native handwriting and bowl pass completes");
            int written=0;for(int x=-10;x<=10;x++)for(int y=1;y<=5;y++){var s=f.l.getBlockState(f.b.offset(x,y,-38));if(s.is(HouseBlocks.VIGNETTE_DETAIL.get())&&s.getValue(VignetteDetailBlock.KIND).name().startsWith("SCRIBBLE_"))written++;}
            h.assertTrue(written==104&&f.l.getBlockState(f.b.offset(0,2,-38)).is(Blocks.DIAMOND_BLOCK),"the wall uses dense original handwriting and preserves the edited cell");
            h.assertTrue(CourtyardFountain.bowl(f.l.getBlockState(f.b.offset(CourtyardFountain.BOWL)))&&f.l.getBlockState(f.b.offset(CourtyardFountain.BOWL)).is(HouseBlocks.VIGNETTE_DETAIL.get()),"the actual fountain has its custom stone bowl");
            h.assertTrue(lectern==f.l.getBlockEntity(f.b.offset(NovelRooms.ARCHIVE_DESK))&&ItemStack.matches(book,lectern.getBook())&&cat.getUUID().equals(id)&&cat.isAlive(),"source container, book and resident identities remain exact");
            f.reload();f.l.setBlock(f.b.offset(-10,1,-38),Blocks.AIR.defaultBlockState(),3);PlaytestScenes0488.repair(f.l,f.b,f.place,true);h.assertTrue(f.l.getBlockState(f.b.offset(-10,1,-38)).isAir(),"reload never replaces a later removed writing");
        }).thenSucceed();
    }

    @GameTest(template="empty",batch="finish488_lighting",timeoutTicks=400)
    public static void instituteKeepsOnlyCeilingIlluminationAndExactCabinetContents(GameTestHelper h){
        var f=new Fixture(h,LabyrinthPlace.WHALE,644000);h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native institute sections are ready")).thenExecute(()->{
            f.l.setBlock(f.b.offset(7,4,-11),InstituteFixtureBlock.of(InstituteFixtureBlock.Kind.LIGHT),3);
            f.l.setBlock(f.b.offset(6,1,-11),SceneDetailBlock.state(SceneDetailBlock.Kind.TABLE_LAMP,Direction.NORTH),3);f.put(0,2,-14,Blocks.LIGHT);f.put(-7,0,-15,Blocks.LANTERN);
            f.put(9,0,-20,Blocks.BARREL);var barrel=(BarrelBlockEntity)f.l.getBlockEntity(f.b.offset(9,0,-20));var exact=new ItemStack(Items.PAPER,2);exact.set(DataComponents.CUSTOM_NAME,Component.literal("Kept paper"));barrel.setItem(8,exact);
            h.assertTrue(PlaytestScenes0488.repair(f.l,f.b,f.place,true),"vacant loaded lighting pass completes");
            for(int x=-14;x<=14;x++)for(int z=-33;z<=0;z++)for(int y=0;y<=10;y++){var at=f.b.offset(x,y,z);var s=f.l.getBlockState(at);if(s.getLightEmission(f.l,at)>0)h.assertTrue(y>=4&&s.is(NovelRegistry.WARD_FIXTURE.get())&&s.getValue(InstituteFixtureBlock.KIND)==InstituteFixtureBlock.Kind.LIGHT,"only actual yellow ceiling fixtures emit authored light");}
            h.assertTrue(barrel==f.l.getBlockEntity(f.b.offset(9,0,-20))&&ItemStack.matches(exact,barrel.getItem(8)),"the lighting pass keeps the native cabinet and finite contents");
        }).thenSucceed();
    }

    @GameTest(template="empty",batch="finish488_camp",timeoutTicks=400)
    public static void treeCampWaitsForSpectatorsAndBodiesAndPreservesFatherAndCarving(GameTestHelper h){
        var f=new Fixture(h,LabyrinthPlace.HOLY_RABBIT,645000);h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native tree-camp sections are ready")).thenExecute(()->{
            for(int x=-3;x<=3;x++)for(int z=-28;z<=-21;z++){f.put(x,-1,z,Blocks.PODZOL);for(int y=0;y<=2;y++)f.put(x,y,z,Blocks.SPRUCE_LEAVES);}
            f.put(0,2,-25,LiteraryRegistry.CARVED_TRUNK.get());var spectator=f.player("camp_watching");spectator.setGameMode(GameType.SPECTATOR);f.at(spectator,0,-19);
            h.assertTrue(!PlaytestScenes0488.repair(f.l,f.b,f.place,true),"an actual spectator camera prevents the new camp dressing");f.away(spectator);
            HolyRabbit.shelter(f.l,f.b);
            var father=LiteraryRegistry.ACTOR.get().create(f.l);father.appearance(LiteraryActor.FATHER,2);father.setNoAi(true);father.setNoGravity(true);father.moveTo(Vec3.atBottomCenterOf(f.b.offset(3,0,-22)));father.setHealth(7);f.l.addFreshEntity(father);f.extras.add(father);var id=father.getUUID();
            h.assertTrue(PlaytestScenes0488.repair(f.l,f.b,f.place,true),"the loaded sheltered camp opens without replacing the original father");
            h.assertTrue(f.l.getBlockState(f.b.offset(-2,0,-27)).is(HouseBlocks.SCENE_DETAIL.get())&&f.l.getBlockState(f.b.offset(1,0,-27)).is(HouseBlocks.SCENE_DETAIL.get())&&f.l.getBlockState(f.b.offset(-1,2,-22)).isAir(),"two supported bedrolls share standing headroom under the existing tree");
            h.assertTrue(f.l.getBlockState(f.b.offset(0,2,-25)).is(LiteraryRegistry.CARVED_TRUNK.get())&&father.isAlive()&&father.getUUID().equals(id)&&father.getHealth()==7,"the carving and original father's identity, injury phase and health remain");
            f.reload();f.l.setBlock(f.b.offset(-2,0,-27),Blocks.AIR.defaultBlockState(),3);PlaytestScenes0488.repair(f.l,f.b,f.place,true);h.assertTrue(f.l.getBlockState(f.b.offset(-2,0,-27)).isAir(),"the saved camp does not refill removed belongings");
        }).thenSucceed();
    }
}
