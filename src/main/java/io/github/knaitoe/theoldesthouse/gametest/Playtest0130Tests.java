package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.function.BiConsumer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

/** Actual native orders, finite plate custody, unlimited seated pressure and visit-scoped courtesy. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_uncanny") @PrefixGameTestTemplate(false)
public final class Playtest0130Tests {
    private static LiteraryTests.Fixture fixture;
    private static void close(){if(fixture!=null){fixture.close();fixture=null;}}
    private static void run(GameTestHelper h,BiConsumer<LiteraryTests.Fixture,ServerPlayer> test){var f=new LiteraryTests.Fixture(h);fixture=f;HouseSavedData.get(h.getLevel().getServer()).markInteriorInitialized();var p=f.player("review130",LabyrinthPlace.SECOND_WAITRESS);f.chunks.hold(f.level(LabyrinthPlace.SECOND_WAITRESS),IndianLakeRooms.bounds(f.base(LabyrinthPlace.SECOND_WAITRESS),LabyrinthPlace.SECOND_WAITRESS));h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native diner, surfaces and entities are resident")).thenExecute(()->test.accept(f,p)).thenSucceed();}
    private static CompoundTag step(LiteraryTests.Fixture f,ServerPlayer p){var own=f.own(p,LabyrinthPlace.SECOND_WAITRESS);UncannyStories.reader(p,f.base(LabyrinthPlace.SECOND_WAITRESS),LabyrinthPlace.SECOND_WAITRESS,own);LiteraryVignettes.save(f.data(),p.getUUID(),LabyrinthPlace.SECOND_WAITRESS,own);return own;}
    @GameTest(template="empty",batch="review130_order",timeoutTicks=600)
    public static void everyBreakfastIsChosenThroughTheHandedMenuAndItsOneSpoiledPlateWaitsWhileSeated(GameTestHelper h){run(h,(f,first)->{
        var scene=LabyrinthPlace.SECOND_WAITRESS;var b=f.base(scene);var l=f.level(scene);UncannyStories.prepare(l,b,scene);var waitress=UncannyDiner.waitress(first,b);var waitressId=waitress.getUUID();
        for(var order:UncannyBreakfast.Order.values()){
            var p=order.ordinal()==0?first:f.player("order130_"+order.id,scene);var own=f.own(p,scene);UncannyStories.arrive(p,scene,own);var original=ItemStack.parseOptional(p.registryAccess(),own.getCompound("OriginalMeal"));var id=original.get(DataComponents.CUSTOM_DATA).copyTag().getUUID("UncannyMeal");
            own.putBoolean("DinerFlow130",true);own.putBoolean("DinerMenuGiven130",true);own.putBoolean("DinerSeated127",true);own.putBoolean("DinerSeating127",true);own.putInt("DinerHostLeg127",4);LiteraryVignettes.save(f.data(),p.getUUID(),scene,own);
            var shared=f.data().stateEntry(UncannyStories.WORLD,scene.id()+":"+b.asLong());shared.putUUID("Host127",p.getUUID());f.data().setStateEntry(UncannyStories.WORLD,scene.id()+":"+b.asLong(),shared);waitress.moveTo(b.getX()+3.5,b.getY(),b.getZ()-18.5);
            p.moveTo(Vec3.atBottomCenterOf(b.offset(UncannyDiner.SEAT)));HouseSitting.sit(p,b.offset(UncannyDiner.SEAT));h.assertTrue(UncannyDiner.seated(p,b),"the order comes from an actual occupied booth");
            var menu=UncannyBreakfast.menu(p);p.setItemInHand(InteractionHand.MAIN_HAND,menu);p.gameMode.useItem(p,l,menu,InteractionHand.MAIN_HAND);h.assertTrue(p.containerMenu instanceof LiteraryChoiceMenu,"the held menu opens the actual native choice container");
            var choice=p.containerMenu;h.assertTrue(choice.quickMoveStack(p,1).isEmpty(),"menu icons cannot be shift-clicked into inventory");choice.clicked(1,0,ClickType.THROW,p);h.assertTrue(!f.own(p,scene).contains("Breakfast130"),"throw packets neither steal an icon nor place an order");choice.clicked(1+order.ordinal()*2,0,ClickType.PICKUP,p);
            own=f.own(p,scene);h.assertTrue(own.getString("Breakfast130").equals(order.id)&&!UncannyBreakfast.choose(p,order),"one native choice records the selected breakfast exactly once");
            var selected=ItemStack.parseOptional(p.registryAccess(),own.getCompound("OriginalMeal"));h.assertTrue(selected.get(DataComponents.CUSTOM_DATA).copyTag().getUUID("UncannyMeal").equals(id)&&selected.get(DataComponents.CUSTOM_MODEL_DATA).value()==order.ordinal()+1,"selecting breakfast changes its appearance without replacing the original meal identity");
            var fake=UncannyStories.actor(p,own,"CastFake");fake.moveTo(b.getX()+3.5,b.getY(),b.getZ()-18.5);own.putBoolean("DinerHosted127",true);own.putInt("DinerHostedAt127",0);own.putInt("UncannyAge",450);LiteraryVignettes.save(f.data(),p.getUUID(),scene,own);own=step(f,p);
            h.assertTrue(own.getBoolean("MealServed")&&!own.contains("OriginalMeal"),"the actual second waitress serves the finite selected plate");var plate=(ItemEntity)l.getEntity(own.getUUID("PlateEntity"));h.assertTrue(plate!=null&&plate.getItem().get(DataComponents.CUSTOM_MODEL_DATA).value()==order.ordinal()+1&&plate.getItem().get(DataComponents.CUSTOM_DATA).copyTag().getUUID("UncannyMeal").equals(id),"the correct original plate is really on the table");
            var plateId=plate.getUUID();for(int i=0;i<260;i++)own=step(f,p);h.assertTrue(!UncannyStories.failed(own)&&own.getInt("BoothPressureLine130")>=6&&fake.phase()==2,"the staged pressure continues at the real booth without a timed death or premature changed face");
            int line=own.getInt("BoothPressureLine130");var seat=p.getVehicle();p.stopRiding();if(seat!=null)seat.discard();for(int i=0;i<8;i++)own=step(f,p);h.assertTrue(own.getInt("BoothPressureLine130")==line,"seated dialogue stops when the reader stands");f.reload();h.assertTrue(plateId.equals(f.own(p,scene).getUUID("PlateEntity"))&&l.getEntity(plateId)==plate&&waitress.getUUID().equals(waitressId),"reload retains both the original waitress and exact served plate");p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        }h.assertTrue(!UncannyBreakfast.canOrder(first),"a served reader cannot request another meal");
    });}
    @AfterBatch(batch="review130_order") public static void orderDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="review130_courtesy",timeoutTicks=600)
    public static void entryResetsTheActualFriendlyStrikeStageButOrdinaryTicksDoNotAndPoolsVary(GameTestHelper h){run(h,(f,p)->{
        var scene=LabyrinthPlace.SECOND_WAITRESS;var b=f.base(scene);UncannyStories.prepare(f.level(scene),b,scene);var a=UncannyDiner.waitress(p,b);p.moveTo(a.position().add(0,0,1));LiteraryVignettes.onArrive(p,scene);p.attack(a);h.assertTrue(f.own(p,scene).getInt("FriendlyStrikes127")==1,"a real friendly hit gives the first stage");
        var own=f.own(p,scene);own.putInt("FriendlyStrikes127",2);LiteraryVignettes.save(f.data(),p.getUUID(),scene,own);step(f,p);h.assertTrue(f.own(p,scene).getInt("FriendlyStrikes127")==2,"routine actor ticks never reset the visit's warnings");
        LiteraryVignettes.onArrive(p,scene);h.assertTrue(f.own(p,scene).getInt("FriendlyStrikes127")==0&&!f.own(p,scene).contains("FriendlyStrikeAt127"),"the room entry hook resets both strikes and the old hit debounce");p.attack(a);h.assertTrue(f.own(p,scene).getInt("FriendlyStrikes127")==1&&!f.own(p,scene).getBoolean("Interrupted"),"the new visit really starts at stage one");
        own=f.own(p,scene);for(int stage=1;stage<=3;stage++){String previous="";var seen=new java.util.HashSet<String>();for(int i=0;i<40;i++){String next=FriendlyNpcWarnings.pick(p,own,stage);h.assertTrue(!previous.equals(next),"a stage never repeats its previous warning");seen.add(next);previous=next;}h.assertTrue(seen.size()>=4,"each escalating stage has a genuine varied pool");}
    });}
    @AfterBatch(batch="review130_courtesy") public static void courtesyDone(ServerLevel l){close();}
}
