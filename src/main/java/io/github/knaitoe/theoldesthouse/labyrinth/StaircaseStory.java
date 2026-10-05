package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** A finite, owner-bound original. Burning changes the book, never the player's statistics. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class StaircaseStory {
    public static final String STATE="staircase_story_0444";
    private record Work(ServerPlayer player, BlockPos at, Block block, boolean built, int before) {}
    private static final Deque<Work> WORK=new ArrayDeque<>();
    private StaircaseStory() {}

    private static boolean participant(ServerPlayer p) {
        return p.isAlive() && p.gameMode.getGameModeForPlayer()!=net.minecraft.world.level.GameType.SPECTATOR;
    }
    private static CompoundTag record(ServerPlayer p) {
        return LabyrinthData.get(p.server).stateEntry(STATE,p.getUUID().toString());
    }
    private static void save(ServerPlayer p,CompoundTag own) {
        LabyrinthData.get(p.server).setStateEntry(STATE,p.getUUID().toString(),own);
    }
    private static int stat(ServerPlayer p,net.minecraft.resources.ResourceLocation id) {
        return p.getStats().getValue(Stats.CUSTOM.get(id));
    }

    // Events precede the native stat award. Verify that award after the tick so a
    // protected/canceled block, or a peer's action, never becomes this player's memory.
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void broke(BlockEvent.BreakEvent e) {
        if(e.getPlayer() instanceof ServerPlayer p && participant(p) && p.level().dimension().equals(Level.OVERWORLD))
            enqueue(new Work(p,e.getPos().immutable(),e.getState().getBlock(),false,p.getStats().getValue(Stats.BLOCK_MINED.get(e.getState().getBlock()))));
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void built(BlockEvent.EntityPlaceEvent e) {
        if(e.getEntity() instanceof ServerPlayer p && participant(p) && p.level().dimension().equals(Level.OVERWORLD)) {
            var block=e.getPlacedBlock().getBlock();
            if(block.asItem()!=Items.AIR)enqueue(new Work(p,e.getPos().immutable(),block,true,p.getStats().getValue(Stats.ITEM_USED.get(block.asItem()))));
        }
    }
    private static void enqueue(Work work) { if(WORK.size()<4096)WORK.addLast(work); }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e) {
        // One server only drains its own work; detached test/reloaded servers cannot leak facts.
        int remaining=WORK.size();
        while(remaining-->0) {
            var w=WORK.removeFirst();var p=w.player();
            if(p.server!=e.getServer())continue;
            if(p.server.getPlayerList().getPlayer(p.getUUID())!=p || !participant(p))continue;
            int now=w.built()?p.getStats().getValue(Stats.ITEM_USED.get(w.block().asItem())):p.getStats().getValue(Stats.BLOCK_MINED.get(w.block()));
            if(now<=w.before())continue;
            var own=record(p);own.putString(w.built()?"Built":"Broke",w.block().getName().getString());save(p,own);
        }
        for(var p:e.getServer().getPlayerList().getPlayers())if(p.tickCount%20==0&&participant(p))
            for(int slot=0;slot<p.getInventory().getContainerSize();slot++)prepare(p,p.getInventory().getItem(slot));
    }

    /** Five short native pages, built from recorded facts rather than an invented biography. */
    public static List<String> account(ServerPlayer p) {
        var d=LabyrinthData.get(p.server);var work=record(p);
        var home=HouseExperience.record(d,p.getUUID());var letters=HouseCorrespondence.record(d,p.getUUID());
        long steps=stat(p,Stats.WALK_ONE_CM)/100L;
        int deaths=stat(p,Stats.DEATHS),nights=stat(p,Stats.SLEEP_IN_BED),bred=stat(p,Stats.ANIMALS_BRED);
        int bread=p.getStats().getValue(Stats.ITEM_CRAFTED.get(Items.BREAD));
        int trades=stat(p,Stats.TRADED_WITH_VILLAGER),kills=stat(p,Stats.MOB_KILLS);
        String road=steps>0?"Your record keeps "+quantity(steps,"metre","metres")+" walked. The road was a habit before the staircase asked why.":"The record has no full metre to offer. This page will not invent a road for you.";
        String made=!work.getString("Built").isEmpty()?" In the Overworld you placed "+shortName(work.getString("Built"))+". Here, even what you put down follows.":bread>0?" You made "+quantity(bread,"loaf","loaves")+" of bread. Your hands knew work that could keep you alive.":" The page leaves room for work it did not witness.";
        String care=home.getInt("Care")>0&&!home.getString("CaredName").isEmpty()?"You put your hand on "+shortName(home.getString("CaredName"))+". The ink keeps that touch; it cannot say where the animal is now.":bred>0?"You bred animals "+quantity(bred,"time","times")+". There were lives in your record that began with care.":trades>0?"You traded with villagers "+quantity(trades,"time","times")+". For a while, what changed hands had an agreed value.":"There is no recorded touch here to borrow. The page will not give somebody else's companion your name.";
        String sleep=nights>0?" You slept in a bed "+quantity(nights,"time","times")+". Not every darkness needed a fire.":" No bed-sleep is recorded. This is an absence in the record, not a claim that you never rested.";
        String loss=deaths>0?"You died "+quantity(deaths,"time","times")+"; the record continued. It keeps a count where you might remember the cost of returning.":"No death is recorded. The blank is only a blank; the House cannot make it a promise.";
        String damage=!work.getString("Broke").isEmpty()?" You broke "+shortName(work.getString("Broke"))+" in the Overworld. Your work left a space where something stood.":kills>0?" Your record also counts "+quantity(kills,"creature","creatures")+" killed. It does not write a reason beside them.":" The ink refuses to turn silence into a confession.";
        String house=!letters.getString("SafeRetreat").isEmpty()?"You returned from "+placeName(letters.getString("SafeRetreat"))+" with its account unfinished. The doorway let you leave a sentence open.":home.getInt("Sleeps")>0?"You slept inside the manor. This house has held you still as well as moved you.":home.getInt("Deepest")>0?"You reached "+quantity(home.getInt("Deepest"),"door","doors")+" deep. Your own record keeps the depth; another explorer's footsteps do not add to it.":"The House has not yet recorded a deeper journey for you. The page will not mistake a stranger's arrival for yours.";
        int read=(int)letters.getCompound("Read").getAllKeys().stream().filter(k->letters.getCompound("Read").getBoolean(k)).count();
        String reading=read>0?" You read "+read+" of its letters. For a moment, the House had to address a reader.":" There are sentences here you have not yet read.";
        String last="These leaves remember your life as their ink found it. Your life keeps moving.\n\nFire takes one page at a time. It cannot take the facts from you.\n\nYou may still go back.";
        return List.of("I. The road\n\n"+road+made,"II. What you kept\n\n"+care+sleep,
                "III. What was lost\n\n"+loss+damage,"IV. The house\n\n"+house+reading,"V. The unwritten\n\n"+last);
    }
    private static String quantity(long count,String singular,String plural){return count+" "+(count==1?singular:plural);}
    private static String shortName(String text) {
        String clean=text.replaceAll("[\\p{Cntrl}]","").trim();
        int end=clean.offsetByCodePoints(0,Math.min(24,clean.codePointCount(0,clean.length())));
        return clean.substring(0,end);
    }
    private static String placeName(String id) {
        return id.replace('_',' ').replace(':',' ').substring(0,Math.min(32,id.length()));
    }
    private static CompoundTag snapshot(ServerPlayer p,int alreadyBurned) {
        var own=record(p);
        if(!own.hasUUID("Original")) {
            own.putUUID("Original",UUID.randomUUID());own.putInt("Burned",alreadyBurned);
            var pages=new ListTag();for(String page:account(p))pages.add(StringTag.valueOf(page));own.put("Pages",pages);
            save(p,own);
        }
        return own;
    }
    private static List<String> pages(CompoundTag own,int from) {
        var all=own.getList("Pages",Tag.TAG_STRING);var text=new ArrayList<String>();
        for(int i=from;i<all.size();i++)text.add(all.getString(i));return text;
    }
    private static ItemStack original(ServerPlayer p,CompoundTag own) {
        int burned=Math.max(0,Math.min(StaircaseFire.REQUIRED,own.getInt("Burned")));
        if(burned==StaircaseFire.REQUIRED)return ItemStack.EMPTY;
        ItemStack book=HouseWriting.book("House of Leaves",p.getGameProfile().getName(),HouseWriting.WritingStyle.WILL,pages(own,burned));
        var tag=new CompoundTag();tag.putUUID("StairReader",p.getUUID());tag.putUUID("StairStory",own.getUUID("Original"));
        tag.putInt("StairPage",burned);tag.putInt("StaircaseLeaves",StaircaseFire.REQUIRED-burned);
        book.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));book.set(DataComponents.CUSTOM_NAME,leafName(StaircaseFire.REQUIRED-burned));return book;
    }
    public static ItemStack issue(ServerPlayer p) { return original(p,snapshot(p,0)); }
    public static Component leafName(int count) { return HouseText.color(Component.literal("House of Leaves ("+count+" leaves)")); }

    /** Upgrade the actual held legacy original before its reader opens or burns it. */
    public static boolean prepare(ServerPlayer p,ItemStack book){
        if(!participant(p)||!book.is(Items.WRITTEN_BOOK)||book.getCount()!=1)return false;
        int leaves=StaircaseFire.leaves(book,p.getUUID());
        if(leaves<1||leaves>StaircaseFire.REQUIRED)return false;
        var tag=book.get(DataComponents.CUSTOM_DATA).copyTag();
        if(tag.hasUUID("StairStory")||tag.contains("StairPage"))return false;
        var content=book.get(DataComponents.WRITTEN_BOOK_CONTENT);
        if(content==null||content.generation()!=0||content.pages().size()!=2)return false;
        var legacy=StaircaseFire.book(p.getUUID(),leaves).get(DataComponents.WRITTEN_BOOK_CONTENT);
        if(!content.pages().equals(legacy.pages()))return false;
        var own=snapshot(p,StaircaseFire.REQUIRED-leaves);
        if(leaves!=StaircaseFire.REQUIRED-own.getInt("Burned"))return false;
        book.set(DataComponents.WRITTEN_BOOK_CONTENT,original(p,own).get(DataComponents.WRITTEN_BOOK_CONTENT));
        tag.putUUID("StairStory",own.getUUID("Original"));tag.putInt("StairPage",own.getInt("Burned"));
        book.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));return true;
    }

    /** Reject copies/replayed leaves. Upgrade an existing finite tutorial original without refilling it. */
    public static boolean burn(ServerPlayer p,ItemStack book) {
        if(!participant(p)||!book.is(Items.WRITTEN_BOOK)||book.getCount()!=1)return false;
        var content=book.get(DataComponents.WRITTEN_BOOK_CONTENT);
        int leaves=StaircaseFire.leaves(book,p.getUUID());
        if(content==null||content.generation()!=0||leaves<1||leaves>StaircaseFire.REQUIRED)return false;
        var tag=book.get(DataComponents.CUSTOM_DATA).copyTag();
        if(!tag.hasUUID("StairStory")) {
            if(!prepare(p,book))return false;
            tag=book.get(DataComponents.CUSTOM_DATA).copyTag();content=book.get(DataComponents.WRITTEN_BOOK_CONTENT);
        }
        var own=record(p);if(!own.hasUUID("Original"))return false;int cursor=own.getInt("Burned");
        if(leaves!=StaircaseFire.REQUIRED-cursor)return false;
        if(!own.getUUID("Original").equals(tag.getUUID("StairStory"))||tag.getInt("StairPage")!=cursor)return false;
        var expected=original(p,own).get(DataComponents.WRITTEN_BOOK_CONTENT);
        if(!content.pages().equals(expected.pages()))return false;
        own.putInt("Burned",cursor+1);save(p,own);
        if(leaves==1)book.shrink(1);
        else {
            book.set(DataComponents.WRITTEN_BOOK_CONTENT,new WrittenBookContent(content.title(),content.author(),content.generation(),List.copyOf(content.pages().subList(1,content.pages().size())),content.resolved()));
            tag.putInt("StairPage",cursor+1);tag.putInt("StaircaseLeaves",leaves-1);
            book.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
            if(leafName(leaves).equals(book.get(DataComponents.CUSTOM_NAME)))book.set(DataComponents.CUSTOM_NAME,leafName(leaves-1));
        }
        return true;
    }
}
