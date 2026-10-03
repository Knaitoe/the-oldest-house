package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;

/** Original letters: grief, enforced rest and a voice that learns to keep its own pages. */
public final class ClassicsTexts {
    private ClassicsTexts(){}
    public static ItemStack grace(){return HouseWriting.book("Letters not posted","G.",HouseWriting.WritingStyle.KAREN,List.of(
        "My dear,\n\nI laid your place again. The children did not laugh this time. I think they have begun to fear the sound of a third chair.",
        "The curtains stay shut. She says the light troubles them. Yet every morning I find them standing at the glass, asking what the garden smells like.",
        "There are three days I cannot account for. Your coat is dry. The children insist you came home during the rain. I wish I had been awake.",
        "If you come, knock softly. I have taught them to be brave about footsteps. I have not taught myself."));}
    public static ItemStack album(){return HouseWriting.book("Names beneath the photographs","A family album",HouseWriting.WritingStyle.PLAIN,List.of(
        "Ada.\n\nHer hand was cold, though the room was warm. They made her hold a book so her fingers would not show.",
        "Thomas.\n\nThe photographer asked us to stand very still. Nobody had to ask him.",
        "The last space has no photograph.\n\nA name has been pencilled beneath it. The pencil has worn through the paper."));}
    public static ItemStack medium(){return HouseWriting.book("A sitting","Mrs. Vale",HouseWriting.WritingStyle.ZAMPANO,List.of(
        "The little girl asked whether a house can forget who lives in it. Her mother corrected her. I wrote the question down.",
        "A cupboard opened with nobody near it. The mother said it was the draught. The windows were nailed shut.",
        "When I asked for a name, the girl gave me the name of a person I could not see. She was looking at the empty chair."));}
    public static ItemStack wallpaper(int page){String[][] pages={
        {"He calls this the best room.\n\nThere is air from both windows, if I am permitted to open them. The bed has been nailed down. He says it used to belong to children.","I have hidden this where the pattern doubles back. He reads everything left on the desk. He never looks closely at a wall."},
        {"He counted the pages tonight.\n\nThen he kissed my forehead and told me how much better I looked. I thanked him for finding the missing sheets.","There is a woman in the spaces between the flowers. When I ask her what she wants, she bends her head toward the seam."},
        {"The paper comes away in strips.\n\nUnder it the wall is perfectly ordinary. I had hoped for a door. I am ashamed of how long I stood looking at plaster.","She crosses the room when his footsteps stop. I cannot decide whether I am helping her out or teaching her where to hide."},
        {"He has left the key on the landing.\n\nI heard him explain to someone that I must not be excited. His voice sounded tired. It was the first kindness I could not forgive.","I am keeping these pages.\n\nIf he asks, tell him you found only paper. Tell him the room was empty. You need not tell him which of us left first."}
    };return HouseWriting.book("Behind the pattern "+(page+1),"An unnamed writer",HouseWriting.WritingStyle.PELAFINA,List.of(pages[Math.max(0,Math.min(3,page))]));}
    public static ItemStack folio(){var pages=new ArrayList<net.minecraft.network.chat.Component>();for(int i=0;i<4;i++)pages.addAll(wallpaper(i).get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(p->p.raw()).toList());
        var book=HouseWriting.book("The pages she kept","An unnamed writer",pages);var folio=new ItemStack(ClassicsRegistry.FOLIO.get());folio.set(DataComponents.WRITTEN_BOOK_CONTENT,book.get(DataComponents.WRITTEN_BOOK_CONTENT));folio.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("The pages she kept"));return folio;}
    public static List<ItemStack> specimens(){var all=new ArrayList<ItemStack>(List.of(grace(),album(),medium(),folio()));for(int i=0;i<4;i++)all.add(wallpaper(i));return all;}
}
