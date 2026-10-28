package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID+"_goatman_meal") @PrefixGameTestTemplate(false)
public final class GoatmanMealTests {
    private static GoatmanTests.Fixture active;
    @AfterBatch(batch="goat_meal_breath134") public static void mealDone(ServerLevel ignored){bunsDone(ignored);}
    private static void reloadData(GoatmanTests.Fixture f){var d=LabyrinthData.get(f.server);var restored=LabyrinthData.FACTORY.deserializer().apply(d.save(new CompoundTag(),f.l.registryAccess()),f.l.registryAccess());f.server.overworld().getDataStorage().set("the_oldest_house_labyrinth",restored);}

    @AfterBatch(batch="goat_buns137") public static void bunsDone(ServerLevel ignored){if(active!=null){active.close();active=null;}}
    @AfterBatch(batch="goat_pillows137") public static void bedsDone(ServerLevel ignored){bunsDone(ignored);}
    @GameTest(template="empty",batch="goat_buns137",timeoutTicks=450)
    public static void actualBunsTransferIntoTheOriginalFoodAndToAnOriginalCousinWithoutRefillingOrSpendingTwice(GameTestHelper h){
        active=new GoatmanTests.Fixture(h,58300);var f=active;f.finishedCamper();var p=f.nativePlayer("buns_reader");f.chunks.hold(f.l,IndianLakeRooms.bounds(f.b,LabyrinthPlace.GOATMAN));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the real supper table and cast sections are resident")).thenExecute(()->{
            GoatmanVignette.enter(p);var r=f.run();r.putInt("Phase",GoatmanVignette.GATHERING);r.putInt("Clock",200);r.putInt("Wrong",0);r.putInt("Runner",7);r.putBoolean("MealStarted137",true);r.putInt("BunsLeft137",8);f.cohort(r,List.of(p),0);f.run(r);GoatmanVignette.stage(f.l,f.b,r);
            var original=new ItemStack(GoatmanRegistry.BRAT.get());CustomData.update(DataComponents.CUSTOM_DATA,original,t->{t.putUUID(GoatmanVignette.ROUND,r.getUUID("Id"));t.putString("ExactOriginal137","kept");});var display=GoatmanWoods.served(f.l,f.b,0,original);var id=display.getUUID();GoatmanMeal.tick(f.l,f.b,r,List.of(p));f.run(r);
            p.moveTo(f.b.getX()+.5,f.b.getY()+1,f.b.getZ()-59);var basket=GoatmanMeal.spot(4).add(f.b.getX(),f.b.getY()+.15,f.b.getZ());p.lookAt(EntityAnchorArgument.Anchor.EYES,basket);
            var support=f.b.offset(0,1,-61);p.gameMode.useItemOn(p,f.l,p.getMainHandItem(),InteractionHand.MAIN_HAND,new BlockHitResult(basket,Direction.UP,support,false));
            h.assertTrue(p.getMainHandItem().is(GoatmanRegistry.BUN.get())&&p.getMainHandItem().getCount()==8&&f.run().getInt("BunsLeft137")==0,"the actual native use transfers the finite basket into the reader's hand");
            f.click(p,BlockPos.containing(GoatmanWoods.plate(0)).below());var food=GoatmanWoods.food(f.l,display);
            h.assertTrue(display.getUUID().equals(id)&&food.get(DataComponents.CUSTOM_DATA).copyTag().getBoolean("GoatmanBun137")&&food.get(DataComponents.CUSTOM_DATA).copyTag().getString("ExactOriginal137").equals("kept")&&p.getMainHandItem().getCount()==7,"one bun surrounds the exact original frank without replacing its physical display or custom components");
            f.click(p,BlockPos.containing(GoatmanWoods.plate(0)).below());h.assertTrue(p.getMainHandItem().getCount()==7,"a second native use cannot spend another bun on the same serving");
            var cousin=f.cousin(3).orElseThrow();p.moveTo(cousin.getX()+1.5,cousin.getY(),cousin.getZ());var give=new PlayerInteractEvent.EntityInteract(p,InteractionHand.MAIN_HAND,cousin);NeoForge.EVENT_BUS.post(give);
            h.assertTrue(give.isCanceled()&&p.getMainHandItem().getCount()==6&&(f.run().getInt("BunsCousins137")&(1<<3))!=0,"the reader can hand a bun to an actual original cousin");
            NeoForge.EVENT_BUS.post(new PlayerInteractEvent.EntityInteract(p,InteractionHand.MAIN_HAND,cousin));h.assertTrue(p.getMainHandItem().getCount()==6,"duplicate native entity interaction cannot spend twice");
            var restored=LabyrinthData.FACTORY.deserializer().apply(LabyrinthData.get(f.server).save(new CompoundTag(),f.l.registryAccess()),f.l.registryAccess());f.server.overworld().getDataStorage().set("the_oldest_house_labyrinth",restored);GoatmanMeal.tick(f.l,f.b,f.run(),List.of(p));
            h.assertTrue(f.run().getInt("BunsLeft137")==0&&f.l.getEntitiesOfClass(net.minecraft.world.entity.Display.ItemDisplay.class,IndianLakeRooms.bounds(f.b,LabyrinthPlace.GOATMAN),e->e.getTags().contains(GoatmanMeal.TAG)&&e.getPersistentData().getInt("Supply137")==4).isEmpty(),"reload never refills the used basket");
        }).thenSucceed();
    }
    @GameTest(template="empty",batch="goat_pillows137",timeoutTicks=350)
    public static void nativeBedPillowsAreAgainstBothOuterWallsAndTheFeetPointTowardTheActualTable(GameTestHelper h){
        active=new GoatmanTests.Fixture(h,58600);var f=active;f.finishedCamper();f.chunks.hold(f.l,IndianLakeRooms.bounds(f.b,LabyrinthPlace.GOATMAN));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the finished native bedroom is resident")).thenExecute(()->{
            GoatmanWoods.beds(f.l,f.b,10);int heads=0;
            for(var rel:BlockPos.betweenClosed(-7,1,-76,7,3,-55)){
                var at=f.b.offset(rel);var state=f.l.getBlockState(at);if(!(state.getBlock() instanceof BedBlock)||state.getValue(BedBlock.PART)!=BedPart.HEAD)continue;
                heads++;var foot=at.relative(state.getValue(BedBlock.FACING).getOpposite());var other=f.l.getBlockState(foot);
                h.assertTrue(other.getBlock() instanceof BedBlock&&other.getValue(BedBlock.PART)==BedPart.FOOT,"each actual native pillow has its complete mattress");
                h.assertTrue(Math.abs(foot.getX()+.5-f.b.getX()-.5)<Math.abs(at.getX()+.5-f.b.getX()-.5),"the foot of the bed, not its pillow, is closer to the center table on both sides");
            }GoatmanMealExport.save(f,"beds_west",-8,1,-74,-55);GoatmanMealExport.save(f,"beds_east",0,8,-74,-55);h.assertTrue(heads==10,"all ten native beds have outward pillows and inward feet");
        }).thenSucceed();
    }
    @GameTest(template="empty",batch="goat_meal_breath134",timeoutTicks=1800)
    public static void theFirstPlateSendsTheRunnerForGasWhileTheOriginalCousinsBringCondimentsAndEatThroughTheBreather(GameTestHelper h){
        active=new GoatmanTests.Fixture(h,57700);var f=active;f.finishedCamper();var p=f.nativePlayer("meal_137");f.chunks.hold(f.l,IndianLakeRooms.bounds(f.b,LabyrinthPlace.GOATMAN));
        int[] observed={0};boolean[] begun={false},loaded={false},carried={false};var originals=new HashSet<UUID>();
        h.onEachTick(()->{if(!begun[0])return;var r=f.run();
            if(observed[0]++%200==0)TheOldestHouse.LOGGER.info("MEAL137 clock={} stage={}/{} bottles={} seated={} cast={}",r.getInt("Clock"),r.getInt("ProvisionCarrier137_0"),r.getInt("ProvisionCarrier137_1"),r.getInt("CondimentsPlaced137"),r.contains("MealSeatedAt137"),f.cousins().stream().map(c->c.getPersistentData().getInt(GoatmanVignette.INDEX)+":"+f.rel(c)+":"+c.seated()).toList());
            carried[0]|=f.cousins().stream().anyMatch(c->c.getMainHandItem().is(GoatmanRegistry.KETCHUP.get())&&c.getOffhandItem().is(GoatmanRegistry.MUSTARD.get()));
            if(!r.contains("MealSeatedAt137")||r.getInt("Clock")-r.getInt("MealSeatedAt137")<GoatmanMeal.MEAL_TICKS)h.assertTrue(r.getInt("RunnerState")!=GoatmanVignette.R_RETURNING&&r.getInt("RunnerState")!=GoatmanVignette.R_KNOCKING,"the actual meal and carrying actions precede the returning cousin");
            if(r.contains("MealSeatedAt137")&&!loaded[0]){reloadData(f);loaded[0]=true;}
        });
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the finished camper and original native cast are resident")).thenExecute(()->{
            GoatmanVignette.enter(p);var r=f.run();r.putInt("Phase",GoatmanVignette.GATHERING);r.putInt("Clock",120);r.putInt("Wrong",0);r.putInt("Runner",7);f.cohort(r,List.of(p),0);f.run(r);GoatmanVignette.stage(f.l,f.b,r);
            for(var c:f.cousins())if(c.getPersistentData().getInt(GoatmanVignette.INDEX)!=7)originals.add(c.getUUID());
            p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,franks(r,8));
            f.click(p,BlockPos.containing(GoatmanWoods.plate(0)).below());
            h.assertTrue(f.run().getInt("RunnerState")==GoatmanVignette.R_LEAVING&&!f.run().contains("MealSeatedAt137")&&p.getMainHandItem().getCount()==7,"the first native serving immediately starts the gas trip before the remaining seven are plated");
            for(int i=1;i<8;i++)f.click(p,BlockPos.containing(GoatmanWoods.plate(i)).below());f.indoors(p);begun[0]=true;
        }).thenWaitUntil(()->h.assertTrue(f.run().contains("MealSeatedAt137"),"the two original cousins physically fetch four bottles, set them down and sit"))
        .thenExecute(()->{
            GoatmanMealExport.save(f,"meal_table",-3,4,-70,-58);
            var r=f.run();h.assertTrue(carried[0]&&r.getInt("CondimentsPlaced137")==15&&r.getInt("ProvisionCarrier137_0")==3&&r.getInt("ProvisionCarrier137_1")==3,"bottles arrive through the original carriers' hands");
            h.assertTrue(f.l.getEntitiesOfClass(net.minecraft.world.entity.Display.ItemDisplay.class,IndianLakeRooms.bounds(f.b,LabyrinthPlace.GOATMAN),e->e.getTags().contains(GoatmanMeal.TAG)).size()==5,"four actual bottles and the finite buns occupy the supper table");
            h.assertTrue(originals.equals(f.cousins().stream().filter(c->c.getPersistentData().getInt(GoatmanVignette.INDEX)!=7).map(Entity::getUUID).collect(java.util.stream.Collectors.toSet())),"the meal keeps every original diner");
        }).thenWaitUntil(()->h.assertTrue(f.run().getInt("RunnerState")==GoatmanVignette.R_RETURNING||f.run().getInt("RunnerState")==GoatmanVignette.R_KNOCKING,"the actual seated meal finishes before the cousin calls from the trail"))
        .thenExecute(()->h.assertTrue(loaded[0]&&f.run().getInt("MealTalk137")==4,"all four ordinary eating lines and the genuine meal survive reload")).thenSucceed();
    }
    private static net.minecraft.world.item.ItemStack franks(CompoundTag r,int n){var item=new net.minecraft.world.item.ItemStack(GoatmanRegistry.BRAT.get(),n);net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA,item,t->t.putUUID(GoatmanVignette.ROUND,r.getUUID("Id")));return item;}
}
