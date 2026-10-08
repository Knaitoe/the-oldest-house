package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class NovelCorrespondenceTests {
    private static MarginaliaTests.Fixture originals;
    @AfterBatch(batch="novel_source_originals") public static void clean(ServerLevel l){if(originals!=null){originals.close();originals=null;}}
    private static String text(ItemStack book){return book.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(p->p.raw().getString()).reduce("",(a,b)->a+"\n"+b).replaceAll("\\s+"," ").strip();}
    private static String id(ItemStack book){return book.getOrDefault(DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.EMPTY).copyTag().getString("CorrespondenceId");}
    @GameTest(template="empty")
    public static void sourceWordsCipherAndAuthorsSurviveNativePagination(GameTestHelper h){
        for(var note:NovelCorrespondence.all()){
            var book=CorrespondenceTexts.book(note,Map.of());var content=book.get(DataComponents.WRITTEN_BOOK_CONTENT);
            h.assertTrue(text(book).equals(note.text().replaceAll("\\s+"," ").strip()),"native wrapping must retain every source word, case and punctuation: "+note.id());
            h.assertTrue(content.author().equals(note.author())&&content.title().raw().equals(note.title())&&content.pages().size()<=100,"source authors and native titles remain exact: "+note.id());
            for(var page:content.pages())page.raw().visit((style,words)->{if(!words.isEmpty())h.assertTrue(note.style().font().equals(style.getFont()),"one paper keeps its actual writer's hand: "+note.id());return Optional.empty();},net.minecraft.network.chat.Style.EMPTY);
        }
        String cipher=text(CorrespondenceTexts.book(CorrespondenceTexts.find("HOL_P05"),Map.of()));
        h.assertTrue(cipher.contains("elevAted")&&cipher.contains("nulliFied")&&cipher.contains("embArked")&&cipher.contains("deCeased")&&cipher.contains("froWned")&&cipher.contains("insiDe"),"both the word-initial cipher and inner capitals remain readable");
        h.assertTrue(CorrespondenceTexts.find("HOL_P04").text().contains("use the first letter of each word")&&CorrespondenceTexts.find("HOL_P07").text().contains("That won’t do."),"the cipher instructions and later account of missing comfort remain part of the ordered selection");
        h.assertTrue(NovelCorrespondence.all().stream().filter(n->n.style()==HouseWriting.WritingStyle.JOHNNY).count()==34&&NovelCorrespondence.halls().size()==45,"the complete travel collection and 45 hall papers are present");
        h.succeed();
    }
    @GameTest(template="empty",batch="novel_source_originals",timeoutTicks=140)
    public static void sourceFindInterleavesEarlyAndCollectedReadingKeepsOldCursorPeersAndOriginals(GameTestHelper h){
        originals=new MarginaliaTests.Fixture(h,98300,false);var f=originals;var a=f.player();var b=f.player();f.depth(a,16);f.depth(b,16);
        h.runAfterDelay(8,()->{
            ItemStack first=null;
            for(var thread:List.of(HouseMarginalia.Thread.ROOM,HouseMarginalia.Thread.HOUSEKEEPING,HouseMarginalia.Thread.CALLS)){
                var menu=f.open(a,thread);if(first==null)first=menu.book().copy();f.end(a,menu);a.closeContainer();HouseCorrespondence.crossed(a);HouseCorrespondence.crossed(a);
            }
            var before=HouseCorrespondence.record(f.data(),a.getUUID());int cursor=before.getInt("Cursor");
            var source=f.open(a,HouseMarginalia.Thread.CALLS);var book=source.book().copy();
            h.assertTrue(id(book).equals("HOL_P01")&&text(book).contains("Ponce de León")&&!text(book).contains(a.getGameProfile().getName()),"a warm historical letter can appear at the fourth reading without addressing the player");
            h.assertTrue(source.clickMenuButton(a,3)&&!HouseCorrespondence.record(f.data(),a.getUUID()).getCompound("Read").getBoolean("HOL_P01"),"taking the multi-page source alone does not finish it");a.closeContainer();
            for(int slot=0;slot<a.getInventory().getContainerSize();slot++)if(id(a.getInventory().getItem(slot)).equals("HOL_P01")){a.getInventory().removeItemNoUpdate(slot);break;}
            b.setItemInHand(InteractionHand.MAIN_HAND,book);var borrowed=new PlayerInteractEvent.RightClickItem(b,InteractionHand.MAIN_HAND);NeoForge.EVENT_BUS.post(borrowed);
            h.assertTrue(!borrowed.isCanceled()&&HouseCorrespondence.record(f.data(),b.getUUID()).getCompound("Next").getInt(NovelCorrespondence.PELAFINA_CHAIN)==0,"a peer can read the historical words without inheriting this chain");
            b.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);a.setItemInHand(InteractionHand.MAIN_HAND,book);NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickItem(a,InteractionHand.MAIN_HAND));
            h.assertTrue(a.containerMenu instanceof HouseCorrespondence.OriginalMenu,"the actual collected source opens its owner's native page menu");
            var menu=(HouseCorrespondence.OriginalMenu)a.containerMenu;h.assertTrue(menu.clickMenuButton(a,100+book.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size()-1),"the source completes only on its last native page");a.closeContainer();
            var own=HouseCorrespondence.record(f.data(),a.getUUID());h.assertTrue(own.getInt("Cursor")==cursor&&own.getCompound("Next").getInt(NovelCorrespondence.PELAFINA_CHAIN)==1,"new source progress keeps the old 125-position cursor and advances only the dated chain");
            for(String key:before.getCompound("Books").getAllKeys())h.assertTrue(before.getCompound("Books").get(key).equals(own.getCompound("Books").get(key)),"every older original remains byte-exact: "+key);
            f.reload();var same=f.open(a,HouseMarginalia.Thread.CALLS);h.assertTrue(ItemStack.isSameItemSameComponents(book,same.book())&&!same.clickMenuButton(a,3),"the saved source remains exact and cannot refill after reload");
            h.assertTrue(!HouseCorrespondence.available(a,"HOL_P02"),"the next dated letter waits for actual travel");HouseCorrespondence.crossed(a);h.assertTrue(!HouseCorrespondence.available(a,"HOL_P02"),"one crossing is insufficient");HouseCorrespondence.crossed(a);h.assertTrue(HouseCorrespondence.available(a,"HOL_P02"),"two actual crossings unlock the next dated source");
            h.assertTrue(WitnessAccount.count(f.data(),a.getUUID())==0&&WitnessAccount.REQUIRED==33&&WitnessAccount.Story.values().length==43,"poems, correspondence and cipher reading never add Witness sources");h.succeed();
        });
    }
}
