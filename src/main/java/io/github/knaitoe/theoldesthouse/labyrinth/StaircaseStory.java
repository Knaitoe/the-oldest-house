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

/**
 * One explorer's House of Leaves. The shelf gives its binding and title leaf; the five story
 * leaves lie loose on the five flights, one to each, and are bound in as they are found. Only
 * the next bound leaf of the explorer's own living original feeds the next hearth. The saved
 * record, not any copy of the book, is what decides which leaf is next.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class StaircaseStory {
    public static final String STATE="staircase_story_0444";
    /** Bindings from this edition on gather their leaves on the flights. */
    public static final int EDITION=449;
    private record Work(ServerPlayer player, BlockPos at, Block block, boolean built, int before) {}
    private static final Deque<Work> WORK=new ArrayDeque<>();
    private StaircaseStory() {}

    static boolean participant(ServerPlayer p) {
        return p.isAlive() && p.gameMode.getGameModeForPlayer()!=net.minecraft.world.level.GameType.SPECTATOR;
    }
    public static CompoundTag record(ServerPlayer p) {
        return LabyrinthData.get(p.server).stateEntry(STATE,p.getUUID().toString());
    }
    private static void save(ServerPlayer p,CompoundTag own) {
        LabyrinthData.get(p.server).setStateEntry(STATE,p.getUUID().toString(),own);
    }
    public static boolean exists(CompoundTag own) { return own.hasUUID("Original"); }
    public static int burned(CompoundTag own) { return Math.max(0,Math.min(StaircaseFire.REQUIRED,own.getInt("Burned"))); }
    /** Leaves bound so far. An account written before leaves were scattered already holds all five. */
    public static int found(CompoundTag own) {
        int found=own.contains("Found")?own.getInt("Found"):exists(own)?StaircaseFire.REQUIRED:0;
        return Math.max(burned(own),Math.min(StaircaseFire.REQUIRED,found));
    }

    // ------------------------------------------------------------------ Overworld work

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
            var own=record(p);own.putString(w.built()?"Built":"Broke",StaircaseAccount.blockName(w.block()));save(p,own);
        }
        for(var p:e.getServer().getPlayerList().getPlayers())if(p.tickCount%20==0&&participant(p))sync(p);
    }

    // ------------------------------------------------------------------ the account

    private static int flames(ServerPlayer p) { return StaircaseFire.flames(FinaleProgress.player(p.server,p.getUUID())); }
    /** A hearth lit before this edition (by paper, or an old visit) has used its flight's leaf. */
    private static CompoundTag reconcile(ServerPlayer p,CompoundTag own) {
        if(!exists(own))return own;
        int fires=flames(p),burned=Math.max(burned(own),fires),found=Math.max(found(own),burned);
        if(burned!=own.getInt("Burned")||found!=own.getInt("Found")||!own.contains("Found")){own.putInt("Burned",burned);own.putInt("Found",found);save(p,own);}
        return own;
    }
    /** Writes the account once, the first time this explorer takes their House of Leaves. */
    private static CompoundTag snapshot(ServerPlayer p,int burned,int found) {
        var own=record(p);
        if(exists(own))return reconcile(p,own);
        var original=UUID.randomUUID();long seed=p.getUUID().getMostSignificantBits()^original.getLeastSignificantBits();
        var story=StaircaseAccount.write(p,own,seed);
        own.putUUID("Original",original);own.putLong("Seed",seed);own.putString("Voice",story.voice().name());own.putString("Hand",story.voice().hand);
        own.putString("Front",story.front());
        var pages=new ListTag();for(String page:story.leaves())pages.add(StringTag.valueOf(page));own.put("Pages",pages);
        own.putInt("Burned",burned);own.putInt("Found",Math.max(burned,found));own.putInt("Edition",EDITION);
        save(p,own);return reconcile(p,own);
    }
    private static HouseWriting.WritingStyle hand(CompoundTag own) { return StaircaseAccount.hand(own.contains("Hand")?own.getString("Hand"):"WILL"); }
    private static String front(ServerPlayer p,CompoundTag own) { return own.contains("Front")?own.getString("Front"):StaircaseProse.legacyFront(p.getGameProfile().getName()); }
    /** The text of one story leaf, as its finder will read it. */
    public static String leaf(CompoundTag own,int index) { var all=own.getList("Pages",Tag.TAG_STRING);return index<all.size()?all.getString(index):""; }
    public static Component leafPage(CompoundTag own,int index) { return HouseWriting.page(hand(own),leaf(own,index)); }
    public static Component leafName(int count) {
        return HouseText.color(Component.literal(count<=0?"House of Leaves":"House of Leaves ("+count+(count==1?" leaf)":" leaves)")));
    }
    private static boolean autoName(Component name) {
        if(name==null)return true;
        for(int i=0;i<=StaircaseFire.REQUIRED;i++)if(leafName(i).equals(name)||StaircaseFire.legacyName(i).equals(name))return true;
        return false;
    }
    /** The written content of this explorer's binding: its title leaf, then every bound leaf not yet burned. */
    private static WrittenBookContent content(ServerPlayer p,CompoundTag own) {
        var pages=new ArrayList<String>();pages.add(front(p,own));
        for(int i=burned(own);i<found(own);i++)pages.add(leaf(own,i));
        return HouseWriting.book("House of Leaves",p.getGameProfile().getName(),hand(own),pages).get(DataComponents.WRITTEN_BOOK_CONTENT);
    }
    /** Bring a held binding up to date with the record, keeping every unrelated component. */
    private static void bind(ServerPlayer p,CompoundTag own,ItemStack book) {
        var data=book.get(DataComponents.CUSTOM_DATA);var tag=data==null?new CompoundTag():data.copyTag();
        int ready=found(own)-burned(own);
        tag.putUUID("StairReader",p.getUUID());tag.putUUID("StairStory",own.getUUID("Original"));tag.putInt("StairPage",burned(own));
        tag.putInt(StaircaseFire.LEAVES,ready);tag.putInt("StairBinding",EDITION);
        var content=content(p,own);
        if(!content.equals(book.get(DataComponents.WRITTEN_BOOK_CONTENT)))book.set(DataComponents.WRITTEN_BOOK_CONTENT,content);
        if(data==null||!data.copyTag().equals(tag))book.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
        if(autoName(book.get(DataComponents.CUSTOM_NAME)))book.set(DataComponents.CUSTOM_NAME,leafName(ready));
    }
    private static ItemStack binding(ServerPlayer p,CompoundTag own) {
        if(burned(own)>=StaircaseFire.REQUIRED)return ItemStack.EMPTY;
        var book=new ItemStack(Items.WRITTEN_BOOK);bind(p,own,book);return book;
    }
    /** Explicit operator request: retain the previous words and all finite progress, update the carried original once. */
    public static boolean rewrite(ServerPlayer p){
        if(!participant(p))return false;var own=reconcile(p,record(p));
        if(!exists(own)||burned(own)>=StaircaseFire.REQUIRED)return false;
        ItemStack held=null;for(int i=0;i<p.getInventory().getContainerSize();i++){var stack=p.getInventory().getItem(i);if(isCurrent(p,stack,own)){held=stack;break;}}
        if(held==null)return false;
        var previous=own.copy();previous.remove("PreviousWords");
        var story=StaircaseAccount.write(p,own,own.getLong("Seed"));
        own.put("PreviousWords",previous);own.putUUID("Original",UUID.randomUUID());
        own.putString("Voice",story.voice().name());own.putString("Hand",story.voice().hand);own.putString("Front",story.front());
        var pages=new ListTag();for(var page:story.leaves())pages.add(StringTag.valueOf(page));own.put("Pages",pages);own.putInt("Edition",EDITION);
        save(p,own);bind(p,own,held);p.getInventory().setChanged();return true;
    }
    /** This explorer's living original: their own, uncopied, the binding the record names. */
    public static boolean isCurrent(ServerPlayer p,ItemStack book) { return isCurrent(p,book,record(p)); }
    private static boolean isCurrent(ServerPlayer p,ItemStack book,CompoundTag own) {
        if(!exists(own)||!book.is(Items.WRITTEN_BOOK)||book.getCount()!=1)return false;
        var content=book.get(DataComponents.WRITTEN_BOOK_CONTENT);var data=book.get(DataComponents.CUSTOM_DATA);
        if(content==null||content.generation()!=0||data==null)return false;
        var tag=data.copyTag();
        return tag.hasUUID("StairReader")&&tag.getUUID("StairReader").equals(p.getUUID())&&tag.hasUUID("StairStory")&&tag.getUUID("StairStory").equals(own.getUUID("Original"));
    }
    /** Another reader's House of Leaves, recognised only so the fire can refuse it plainly. */
    public static boolean foreign(ServerPlayer p,ItemStack book) {
        var data=book.get(DataComponents.CUSTOM_DATA);if(data==null)return false;var tag=data.copyTag();
        return tag.hasUUID("StairReader")&&!tag.getUUID("StairReader").equals(p.getUUID());
    }
    public static boolean carries(ServerPlayer p) {
        var own=record(p);var inv=p.getInventory();
        for(int i=0;i<inv.getContainerSize();i++)if(isCurrent(p,inv.getItem(i),own))return true;
        return false;
    }
    private static boolean legacy(ServerPlayer p,ItemStack book) {
        if(!book.is(Items.WRITTEN_BOOK)||book.getCount()!=1)return false;
        int leaves=StaircaseFire.leaves(book,p.getUUID());if(leaves<1||leaves>StaircaseFire.REQUIRED)return false;
        var tag=book.get(DataComponents.CUSTOM_DATA).copyTag();if(tag.hasUUID("StairStory"))return false;
        var content=book.get(DataComponents.WRITTEN_BOOK_CONTENT);
        return content!=null&&content.generation()==0&&content.pages().equals(StaircaseFire.book(p.getUUID(),leaves).get(DataComponents.WRITTEN_BOOK_CONTENT).pages());
    }
    /**
     * Keep every House of Leaves this explorer carries truthful: the living original shows its
     * title leaf and its bound, unburned leaves. An old tutorial book (0.4.33-0.4.43) becomes the
     * explorer's account still holding the leaves it had; nothing is refilled.
     */
    public static void sync(ServerPlayer p) {
        if(!participant(p))return;
        var inv=p.getInventory();var own=record(p);
        for(int i=0;i<inv.getContainerSize();i++){
            var book=inv.getItem(i);if(book.isEmpty()||!book.is(Items.WRITTEN_BOOK))continue;
            if(!exists(own)&&legacy(p,book)){
                int leaves=StaircaseFire.leaves(book,p.getUUID());
                own=snapshot(p,Math.max(StaircaseFire.REQUIRED-leaves,flames(p)),StaircaseFire.REQUIRED);
                bind(p,own,book);continue;
            }
            if(exists(own)&&isCurrent(p,book,own)){
                own=reconcile(p,own);
                if(burned(own)>=StaircaseFire.REQUIRED){inv.setItem(i,ItemStack.EMPTY);continue;}
                bind(p,own,book);
            }
        }
    }

    // ------------------------------------------------------------------ shelf, leaves, fire

    public enum Shelf { ISSUED, REBOUND, CARRIED, FINISHED, REFUSED }
    /** The camp shelf: the first binding, or the same story bound again if the original is lost. */
    public static Shelf shelf(ServerPlayer p) {
        if(!participant(p))return Shelf.REFUSED;
        sync(p);
        var record=FinaleProgress.player(p.server,p.getUUID());boolean first=!record.getBoolean("StairBookTaken");
        if(first){record.putBoolean("StairBookTaken",true);FinaleProgress.save(p.server,p.getUUID(),record);}
        var own=record(p);boolean existed=exists(own);
        own=existed?reconcile(p,own):snapshot(p,flames(p),flames(p));
        if(burned(own)>=StaircaseFire.REQUIRED)return Shelf.FINISHED;
        if(carries(p))return Shelf.CARRIED;
        // A lost original is bound again from the record; the copy left behind goes cold.
        if(!first&&existed){own.putUUID("Original",UUID.randomUUID());save(p,own);}
        give(p,binding(p,own));
        return first?Shelf.ISSUED:Shelf.REBOUND;
    }
    private static void give(ServerPlayer p,ItemStack stack){if(!stack.isEmpty()&&!p.getInventory().add(stack)){var drop=p.drop(stack,false);if(drop!=null)drop.setTarget(p.getUUID());}}

    public enum Take { BOUND, NO_BINDING, NOT_CARRIED, TAKEN, EARLIER, FINISHED, REFUSED }
    /** Where this explorer stands with the leaf on one flight. */
    public static Take leafState(ServerPlayer p,int index) {
        if(!participant(p))return Take.REFUSED;
        var own=record(p);if(!exists(own))return Take.NO_BINDING;own=reconcile(p,own);
        if(burned(own)>=StaircaseFire.REQUIRED)return Take.FINISHED;
        if(index<found(own))return Take.TAKEN;
        if(index>found(own))return Take.EARLIER;
        return carries(p)?Take.BOUND:Take.NOT_CARRIED;
    }
    /** Bind the next loose leaf into the carried original. Only the leaf for the explorer's next flight comes loose. */
    public static Take takeLeaf(ServerPlayer p,int index) {
        var state=leafState(p,index);if(state!=Take.BOUND)return state;
        var own=record(p);own.putInt("Found",index+1);save(p,own);sync(p);p.inventoryMenu.broadcastChanges();
        return Take.BOUND;
    }
    /** The next bound leaf of the explorer's own living original, for the hearth that matches it. */
    public static boolean ready(ServerPlayer p,int hearth) {
        var own=reconcile(p,record(p));return exists(own)&&hearth==burned(own)&&found(own)>burned(own);
    }
    public static boolean burn(ServerPlayer p,ItemStack book,int hearth) {
        if(!participant(p))return false;
        var own=reconcile(p,record(p));
        if(!isCurrent(p,book,own)||hearth!=burned(own)||found(own)<=burned(own))return false;
        own.putInt("Burned",burned(own)+1);save(p,own);
        if(burned(own)>=StaircaseFire.REQUIRED)book.shrink(1);else bind(p,own,book);
        return true;
    }
}
