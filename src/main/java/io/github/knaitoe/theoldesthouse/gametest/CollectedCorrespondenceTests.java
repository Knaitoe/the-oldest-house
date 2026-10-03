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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CollectedCorrespondenceTests {
    private static MarginaliaTests.Fixture originals,facts;
    @AfterBatch(batch="correspondence_collected_originals") public static void cleanOriginals(ServerLevel level){if(originals!=null){originals.close();originals=null;}}
    @AfterBatch(batch="correspondence_confirmed_facts") public static void cleanFacts(ServerLevel level){if(facts!=null){facts.close();facts=null;}}
    private static String text(ItemStack book){return book.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(p->p.raw().getString()).reduce("",(a,b)->a+" "+b).replace('\n',' ');}
    private static ItemStack heldBook(ServerPlayer player){return player.getInventory().items.stream().filter(i->i.has(DataComponents.WRITTEN_BOOK_CONTENT)).findFirst().orElseThrow();}
    private static void cursor(MarginaliaTests.Fixture f,ServerPlayer p,String id){
        int index=0;while(!CorrespondenceTexts.all().get(index).id().equals(id))index++;
        var own=HouseCorrespondence.record(f.data(),p.getUUID());own.putInt("Cursor",index);
        var state=f.data().state(HouseCorrespondence.ID);state.put(p.getUUID().toString(),own);f.data().setState(HouseCorrespondence.ID,state);
    }
    @GameTest(template="empty",batch="correspondence_collected_originals",timeoutTicks=120)
    public static void collectedOriginalsAdvanceOnTheirReadersLastPageAndRemainFiniteAfterReload(GameTestHelper h){
        originals=new MarginaliaTests.Fixture(h,30800,false);var f=originals;var a=f.player();var b=f.player();f.depth(a,20);f.depth(b,20);
        h.runAfterDelay(8,()->{
            cursor(f,a,"J01");var menu=f.open(a,HouseMarginalia.Thread.POEMS);var book=menu.book();var id=book.get(DataComponents.CUSTOM_DATA).copyTag().getString("CorrespondenceId");
            h.assertTrue(id.equals("J01")&&book.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size()>1,"the actual paper opens a multi-page first correspondence as readable native pages");
            h.assertTrue(menu.clickMenuButton(a,3)&&!HouseCorrespondence.record(f.data(),a.getUUID()).getCompound("Read").getBoolean(id),"taking a multi-page original alone does not claim a completed reading");a.closeContainer();
            var original=heldBook(a);for(int slot=0;slot<a.getInventory().getContainerSize();slot++)if(a.getInventory().getItem(slot)==original){a.getInventory().removeItemNoUpdate(slot);break;}b.setItemInHand(InteractionHand.MAIN_HAND,original);
            var borrowed=new PlayerInteractEvent.RightClickItem(b,InteractionHand.MAIN_HAND);NeoForge.EVENT_BUS.post(borrowed);
            h.assertTrue(!borrowed.isCanceled()&&HouseCorrespondence.record(f.data(),b.getUUID()).isEmpty(),"a borrowed original cannot transfer the author's personal sequence to another reader");
            b.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);a.setItemInHand(InteractionHand.MAIN_HAND,original);
            NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickItem(a,InteractionHand.MAIN_HAND));
            h.assertTrue(a.containerMenu instanceof HouseCorrespondence.OriginalMenu,"native item use opens the collected original's private page menu");
            var reader=(HouseCorrespondence.OriginalMenu)a.containerMenu;int last=original.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size()-1;
            h.assertTrue(!reader.clickMenuButton(a,3)&&!reader.clickMenuButton(a,999)&&reader.clickMenuButton(a,100+last),"the original cannot be duplicated or completed by an invalid page request");
            h.assertTrue(HouseCorrespondence.record(f.data(),a.getUUID()).getCompound("Next").getInt("J")==1&&!HouseCorrespondence.available(a,"J02"),"reading records the next installment but does not immediately offer it");
            HouseCorrespondence.crossed(a);h.assertTrue(!HouseCorrespondence.available(a,"J02"),"one crossing is insufficient separation");HouseCorrespondence.crossed(a);h.assertTrue(HouseCorrespondence.available(a,"J02"),"two actual route callbacks separate installments");
            a.closeContainer();f.reload();h.assertTrue(ItemStack.isSameItemSameComponents(original,heldBook(a))&&HouseCorrespondence.record(f.data(),a.getUUID()).getCompound("Taken").getBoolean(id),"reload retains the exact owned original and its finite custody");
            h.assertTrue(!f.open(a,HouseMarginalia.Thread.POEMS).book().isEmpty()&&WitnessAccount.count(f.data(),a.getUUID())==0,"expanded correspondence remains scenery without false Witness credit");h.succeed();
        });
    }
    @GameTest(template="empty",batch="correspondence_confirmed_facts",timeoutTicks=140)
    public static void contextualLettersUseNativeCustodyCareMarkersAndConfirmedReturns(GameTestHelper h){
        facts=new MarginaliaTests.Fixture(h,31100,false);var f=facts;var a=f.player();var b=f.player();f.depth(a,20);f.depth(b,20);
        var oldCollection=MotherCollection.get(a.server);a.server.overworld().getDataStorage().set("the_oldest_house_mother",new MotherCollection());
        h.runAfterDelay(8,()->{
            try{
                h.assertTrue(!HouseCorrespondence.available(a,"L01")&&!HouseCorrespondence.available(a,"P01")&&!HouseCorrespondence.available(a,"P04")&&!HouseCorrespondence.available(a,"P05"),"intrusions wait for actual native personal facts");
                var item=new ItemStack(Items.COMPASS);item.set(DataComponents.CUSTOM_NAME,Component.literal("A winter compass"));var collection=MotherCollection.get(a.server);var lost=collection.keepItem(item,a.registryAccess(),a.getUUID(),0);
                h.assertTrue(lost!=null&&HouseCorrespondence.available(a,"L01")&&!HouseCorrespondence.available(b,"L01"),"native custody unlocks only the original owner's keeper sequence");
                cursor(f,a,"L01");var ledger=f.open(a,HouseMarginalia.Thread.ROOM);h.assertTrue(text(ledger.book()).contains("A winter compass"),"the writer names the real original belonging");f.end(a,ledger);var snapshot=ledger.book();a.closeContainer();
                collection.claimItem(a.getUUID(),lost.id,a.registryAccess());HouseCorrespondence.crossed(a);HouseCorrespondence.crossed(a);cursor(f,a,"L02");var later=f.open(a,HouseMarginalia.Thread.ROOM);
                h.assertTrue(!text(later.book()).contains("is still")&&HouseCorrespondence.available(a,"L02"),"the keeper's historical sequence survives recovery without falsely claiming current custody");
                var wolf=EntityType.WOLF.create(f.level);wolf.tame(a);wolf.setCustomName(Component.literal("Button"));HouseExperience.cared(a,wolf);h.assertTrue(HouseCorrespondence.available(a,"P01")&&!HouseCorrespondence.available(b,"P01"),"real owned companion care remains personal");
                var one=f.base.offset(7,0,-8);var two=f.base.offset(7,0,-9);for(var at:List.of(one,two)){f.level.setBlock(at.below(),Blocks.STONE.defaultBlockState(),3);NavigationAids.placeLine(f.level,at);NavigationAids.remember(f.level,at,a.getUUID(),false);}
                h.assertTrue(HouseCorrespondence.available(a,"P04")&&!HouseCorrespondence.available(b,"P04"),"two supported native markers use their actual placer");f.level.setBlock(two,Blocks.AIR.defaultBlockState(),3);h.assertTrue(!HouseCorrespondence.available(a,"P04"),"removed markers cannot fabricate a trail");
                HouseExperience.arrived(a,LabyrinthPlace.DROWNED_TOWN);HouseExperience.returned(a);h.assertTrue(!HouseCorrespondence.available(a,"P05"),"starting a doorway return does not yet assert safety");HouseCorrespondence.returnedSafely(a,LabyrinthPlace.DROWNED_TOWN.id());h.assertTrue(HouseCorrespondence.available(a,"P05")&&!HouseCorrespondence.available(b,"P05"),"only the physically confirmed unfinished return unlocks the retreat note");
                f.reload();h.assertTrue(ItemStack.isSameItemSameComponents(snapshot,ItemStack.parseOptional(a.registryAccess(),HouseCorrespondence.record(f.data(),a.getUUID()).getCompound("Books").getCompound("L01"))),"a discovered loss letter remains immutable after recovery and native reload");
                h.assertTrue(WitnessAccount.count(f.data(),a.getUUID())==0,"personal facts and correspondence confer no story completion");h.succeed();
            }finally{a.server.overworld().getDataStorage().set("the_oldest_house_mother",oldCollection);}
        });
    }
}
