package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.DoorBlock;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.*;

/** Native reproductions of the supplied empty-plate and standing-family failures. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_wake") @PrefixGameTestTemplate(false)
public final class ImmediateFindingsTests {
    private static GoatmanTests.Fixture supper;
    private static ClassicsTests.Fixture family;
    private static LiteraryTests.Fixture root;
    @AfterBatch(batch="review491_supper") public static void supperDone(ServerLevel l){if(supper!=null){supper.close();supper=null;}}
    @AfterBatch(batch="review491_family") public static void familyDone(ServerLevel l){if(family!=null){family.close();family=null;}}

    @GameTest(template="empty",batch="review491_supper",timeoutTicks=600)
    public static void aStalePlateBitCannotRejectAnActualFrankOrReplaceItsComponents(GameTestHelper h){
        supper=new GoatmanTests.Fixture(h,37000);var f=supper;var p=f.nativePlayer("review491_cook");f.chunks.hold(f.l,IndianLakeRooms.bounds(f.b,LabyrinthPlace.GOATMAN));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"actual food sections are loaded")).thenExecute(()->{
            h.assertTrue(GoatmanVignette.enter(p),"the cook joins a fresh evening");var run=f.run();run.putInt("Phase",GoatmanVignette.GATHERING);run.putInt("Clock",850);run.putInt("Plated0464",1);run.putInt("EmptyPlates0465",0);run.putInt("CookingChatTicks0490",219);f.cohort(run,List.of(p),0);f.run(run);
            int supports=0;for(int x=-1;x<=1;x++)for(int z=-69;z<=-61;z++){var at=f.b.offset(x,1,z);var state=f.l.getBlockState(at);h.assertTrue(state.is(GoatmanRegistry.TABLE.get()),"every tile is the connected table");if(state.getCollisionShape(f.l,at).toAabbs().stream().anyMatch(box->box.minY<.8))supports++;}h.assertTrue(supports==6,"the entire long table has six supports, not four per tile");
            var food=new ItemStack(GoatmanRegistry.BRAT.get());food.set(DataComponents.CUSTOM_NAME,Component.literal("My actual cooked frank"));CustomData.update(DataComponents.CUSTOM_DATA,food,t->{t.putUUID(GoatmanVignette.ROUND,run.getUUID("Id"));t.putUUID("ReviewOriginal",UUID.randomUUID());});var exact=food.copyWithCount(1);p.setItemInHand(InteractionHand.MAIN_HAND,food);
            f.click(p,new BlockPos(-1,1,-61));var original=GoatmanWoods.serving(f.l,f.b,0);h.assertTrue(original!=null&&food.isEmpty()&&ItemStack.matches(GoatmanWoods.food(f.l,original),exact),"the physically empty plate accepts the last held original despite the stale occupied bit");var id=original.getUUID();
            f.click(p,new BlockPos(-1,1,-61));h.assertTrue(p.getMainHandItem().isEmpty()&&GoatmanWoods.serving(f.l,f.b,0).getUUID().equals(id),"the native empty-hand fallback cannot pick the last serving straight back up");
        }).thenIdle(3).thenExecute(()->{
            var original=GoatmanWoods.serving(f.l,f.b,0);var exact=GoatmanWoods.food(f.l,original).copy();f.click(p,new BlockPos(-1,1,-61));
            h.assertTrue(original.isRemoved()&&p.getInventory().items.stream().anyMatch(item->ItemStack.matches(item,exact)),"a later intentional pickup returns the exact native food components");
            h.assertTrue(f.run().getInt("CookingChatLine0490")>=1,"post-story cooking chatter advances at the shorter 220-tick interval");
        }).thenSucceed();
    }

    @GameTest(template="empty",batch="review491_family",timeoutTicks=650)
    public static void threeDistinctNativeTouchesSendTheSameFamilyThroughTheDoor(GameTestHelper h){
        family=new ClassicsTests.Fixture(h,48800,LabyrinthPlace.SEANCE);var f=family;var p=f.player("review491_ghost");var ids=new HashSet<UUID>();
        h.startSequence().thenWaitUntil(()->h.assertTrue(ClassicsVignettes.cast(f.l).size()==4,"the original native family is loaded")).thenExecute(()->{
            var cast=ClassicsVignettes.cast(f.l);cast.forEach(a->ids.add(a.getUUID()));
            for(var a:cast.subList(0,3)){f.at(p,a.getX()-f.b.getX(),0,a.getZ()-f.b.getZ()+1.2);NeoForge.EVENT_BUS.post(new PlayerInteractEvent.EntityInteract(p,InteractionHand.MAIN_HAND,a));}
            h.assertTrue(f.data().state(ClassicsVignettes.STATE).getBoolean("SittingEnded"),"three distinct touched people end the sitting in fear");f.at(p,.5,0,-11.5);
        }).thenWaitUntil(()->h.assertTrue(f.data().state(ClassicsVignettes.STATE).getBoolean("Fled0477"),"the family physically clears its chairs and doorway: "+ClassicsVignettes.cast(f.l).stream().map(a->a.role()+":"+a.position().subtract(f.b.getX(),f.b.getY(),f.b.getZ())).toList())).thenExecute(()->{
            var cast=ClassicsVignettes.cast(f.l);h.assertTrue(cast.stream().map(Entity::getUUID).collect(java.util.stream.Collectors.toSet()).equals(ids),"flight retains every original actor UUID");
            for(var a:cast)h.assertTrue(a.getX()<f.b.getX()&&a.getZ()<f.b.getZ()-23&&!a.seated(),"the original body actually reached the back room");
            h.assertTrue(!f.l.getBlockState(f.b.offset(-5,0,-22)).getValue(DoorBlock.OPEN),"the native door closes after the last person clears it");
        }).thenSucceed();
    }

}
