package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.WritableBookContent;

/** Optional prose starters; the native editable book and its writer remain authoritative. */
public final class WhaleWriting {
    private WhaleWriting(){}
    public static void open(ServerPlayer p,BlockPos desk){
        var choices=new LinkedHashMap<Integer,ItemStack>();
        choices.put(0,LiteraryChoiceMenu.icon(Items.WRITABLE_BOOK,"Write in your own hand"));
        choices.put(2,LiteraryChoiceMenu.icon(Items.PAPER,"Begin with a room you remember"));
        choices.put(4,LiteraryChoiceMenu.icon(Items.FEATHER,"Begin with something left unsaid"));
        choices.put(6,LiteraryChoiceMenu.icon(Items.CLOCK,"Begin with a small ordinary thing"));
        choices.put(8,LiteraryChoiceMenu.icon(Items.INK_SAC,"Let another hand continue"));
        LiteraryChoiceMenu.open(p,"Paper and a waiting pen",choices,()->valid(p,desk),slot->choose(p,desk,slot));
    }
    private static boolean valid(ServerPlayer p,BlockPos desk){return NovelVignettes.inside(p,LabyrinthPlace.WHALE)
        &&p.position().distanceToSqr(desk.getCenter())<=36
        &&p.serverLevel().getBlockState(desk).is(NovelRegistry.WARD_FIXTURE.get())
        &&p.serverLevel().getBlockState(desk).getValue(InstituteFixtureBlock.KIND)==InstituteFixtureBlock.Kind.DESK;}
    public static boolean choose(ServerPlayer p,BlockPos desk,int slot){
        if(!LiteraryVignettes.participant(p)||!valid(p,desk)||!(slot==0||slot==2||slot==4||slot==6||slot==8))return false;
        var book=p.getMainHandItem();if(!book.is(Items.WRITABLE_BOOK)){NovelVignettes.cue(p,NovelVignettes.personal(LabyrinthData.get(p.server),p.getUUID()),"Hold the paper you want to write on.");return true;}
        var before=book.getOrDefault(DataComponents.WRITABLE_BOOK_CONTENT,WritableBookContent.EMPTY);
        if(slot==0){NovelVignettes.cue(p,NovelVignettes.personal(LabyrinthData.get(p.server),p.getUUID()),"The page waits. Open the book to write.");p.playNotifySound(net.minecraft.sounds.SoundEvents.BOOK_PAGE_TURN,net.minecraft.sounds.SoundSource.PLAYERS,.5F,1);return true;}
        var pages=new ArrayList<>(before.pages());boolean written=pages.stream().anyMatch(page->!page.raw().isBlank());
        var data=LabyrinthData.get(p.server);var own=NovelVignettes.personal(data,p.getUUID());var w=own.getCompound(WhaleInstitute.KEY);
        if(slot!=8&&written){NovelVignettes.cue(p,own,"The words already on the page are yours. The pen leaves them alone.");return true;}
        int hands=w.getInt("OtherHand0470");
        if(slot==8&&hands>=3){NovelVignettes.cue(p,own,"The pen lies still.");return true;}
        String prose=switch(slot){
            case 2->"Dear ...\n\nThere is a room I keep returning to in memory. Before I describe what happened there, I want to tell you how the light fell on ...";
            case 4->"Dear ...\n\nI have written the ordinary news first. The thing I meant to say keeps moving to the next paragraph. It begins with ...";
            case 6->"Dear ...\n\nToday I noticed a small thing: ...\n\nIt should not have made me think of you, but it did. I think it was because ...";
            default->otherHand(hands,w.getInt("Sent"));
        };
        // A continuation is a new page. Existing words, filtering and every other component stay exact.
        if(!written)pages.clear();if(pages.size()>=100){NovelVignettes.cue(p,own,"There is no unwritten page left.");return true;}
        pages.add(Filterable.passThrough(prose));book.set(DataComponents.WRITABLE_BOOK_CONTENT,new WritableBookContent(pages));p.getInventory().setChanged();
        if(slot==8){w.putInt("OtherHand0470",hands+1);own.put(WhaleInstitute.KEY,w);NovelVignettes.save(data,p.getUUID(),own);NovelVignettes.cue(p,own,"You stop moving the pen. It finishes the line.");}
        else NovelVignettes.cue(p,own,"A beginning, if you want it. The rest of the page waits for you.");
        p.playNotifySound(net.minecraft.sounds.SoundEvents.BOOK_PAGE_TURN,net.minecraft.sounds.SoundSource.PLAYERS,.6F,.8F);return true;
    }
    private static String otherHand(int n,int sent){return switch(n){
        case 0->"I meant to describe the room. Instead I have written down the sound of a cup being set on a table. It is a very small sound. I cannot tell you why I would give so much to hear it again.";
        case 1->sent>0?"I have sent a letter. The place where it rested on the desk is lighter than the surface around it. I keep putting my hand there, as though warmth were something the post could leave behind.":"There is room on this page for what I have not said. I leave it open beside me. By morning the silence has acquired the shape of a person, and I am ashamed of how carefully I make space for it.";
        default->"I was going to end with love. My hand began the word before I had decided who it belonged to. There is a long pause after the first letter. I am still in that pause. Whoever finishes this, please leave me a little of the white.";
    };}
}
