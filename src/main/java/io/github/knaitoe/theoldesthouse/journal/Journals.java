package io.github.knaitoe.theoldesthouse.journal;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlace;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlaces;
import io.github.knaitoe.theoldesthouse.labyrinth.StaircaseProse;
import io.github.knaitoe.theoldesthouse.network.JournalOpenPayload;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.item.ItemExpireEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Every reader's journal: the words live here, in the world's saved data, and the item in their hands only points to them.
 * A copied stack opens the same pages, a lost one loses nothing, and nothing is ever signed. Docs: JOURNAL_DESIGN.md.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class Journals extends SavedData {
    public static final String DATA_NAME=TheOldestHouse.MOD_ID+"_journals";
    public static final Factory<Journals> FACTORY=new Factory<>(Journals::new,Journals::load);
    public static final int PAGES=96, WORN=4;
    public static final String OWNER="JournalOwner", NAME="JournalName", ISSUE="JournalIssue", WEAR="JournalWear";
    /** One edit costs a token; a reader's screen sends one or two on each turn of the page. */
    static final int TOKENS=8, REFILL_TICKS=5;

    public enum Opening { OPEN, NOT_YOURS, OLD_ISSUE, NOT_A_JOURNAL }

    /** A reader's journal: which copy is theirs now, how worn it is, whether it is out in the world lost, and its pages. */
    static final class Book {
        int issue, wear; boolean lost; String name="";
        final Map<Integer,JournalPage> pages=new HashMap<>();
        // Not saved: the screen's allowance.
        int tokens=TOKENS; long refilled;
    }
    private final Map<UUID,Book> books=new HashMap<>();

    public static Journals get(MinecraftServer server){return server.overworld().getDataStorage().computeIfAbsent(FACTORY,DATA_NAME);}
    private Book book(UUID id){return books.computeIfAbsent(id,k->new Book());}

    public int issue(UUID id){var b=books.get(id);return b==null?0:b.issue;}
    public int wear(UUID id){var b=books.get(id);return b==null?0:b.wear;}
    public boolean lost(UUID id){var b=books.get(id);return b!=null&&b.lost;}
    public JournalPage page(UUID id,int index){var b=books.get(id);return b==null?JournalPage.BLANK:b.pages.getOrDefault(index,JournalPage.BLANK);}

    /** A new copy for its owner, one wear worse when it replaces a lost one; any earlier copy goes blank. */
    public ItemStack issue(ServerPlayer p){
        var b=book(p.getUUID());
        if(b.issue>0&&b.lost)b.wear=Math.min(WORN,b.wear+1);
        b.issue++;b.lost=false;b.name=p.getGameProfile().getName();setDirty();
        return stack(p.getUUID(),b);
    }
    static ItemStack stack(UUID owner,Book b){
        var stack=new ItemStack(JournalRegistry.JOURNAL.get());
        CustomData.update(DataComponents.CUSTOM_DATA,stack,t->{t.putUUID(OWNER,owner);t.putString(NAME,b.name);t.putInt(ISSUE,b.issue);t.putInt(WEAR,b.wear);});
        return stack;
    }
    public static UUID owner(ItemStack s){var c=s.get(DataComponents.CUSTOM_DATA);if(!(s.getItem() instanceof JournalItem)||c==null)return null;var t=c.copyTag();return t.hasUUID(OWNER)?t.getUUID(OWNER):null;}
    public static int issueOf(ItemStack s){var c=s.get(DataComponents.CUSTOM_DATA);return c==null?0:c.copyTag().getInt(ISSUE);}
    public static int wearOf(ItemStack s){var c=s.get(DataComponents.CUSTOM_DATA);return c==null?0:c.copyTag().getInt(WEAR);}
    public static String nameOf(ItemStack s){var c=s.get(DataComponents.CUSTOM_DATA);return c==null?"":c.copyTag().getString(NAME);}

    /** Whether this reader may open this copy: only their own, and only the copy that is theirs now. */
    public Opening check(ServerPlayer p,ItemStack s){
        var owner=owner(s);if(owner==null)return Opening.NOT_A_JOURNAL;
        if(!owner.equals(p.getUUID()))return Opening.NOT_YOURS;
        return issueOf(s)==issue(owner)&&issue(owner)>0?Opening.OPEN:Opening.OLD_ISSUE;
    }
    /** True when the reader carries the copy that is theirs now. */
    public boolean carries(ServerPlayer p){
        for(var s:p.getInventory().items)if(check(p,s)==Opening.OPEN)return true;
        for(var s:p.getInventory().offhand)if(check(p,s)==Opening.OPEN)return true;
        return check(p,p.containerMenu.getCarried())==Opening.OPEN;
    }

    public static void open(ServerPlayer p,ItemStack s){
        var data=get(p.server);
        switch(data.check(p,s)){
            case OPEN->{PacketDistributor.sendToPlayer(p,new JournalOpenPayload(data.contents(p.getUUID())));p.serverLevel().playSound(null,p.blockPosition(),SoundEvents.BOOK_PAGE_TURN,SoundSource.PLAYERS,.6F,1);}
            case NOT_YOURS->p.displayClientMessage(Component.literal("It isn't yours. A name is written inside the cover: "+nameOf(s)+"."),true);
            case OLD_ISSUE->p.displayClientMessage(Component.literal("The pages are blank. This isn't your journal any more."),true);
            case NOT_A_JOURNAL->{}
        }
    }
    /** Everything the reader's screen draws: the name in the cover, the wear and every written page. */
    public CompoundTag contents(UUID id){
        var b=book(id);var t=new CompoundTag();t.putString("Name",b.name);t.putInt("Wear",b.wear);
        var pages=new CompoundTag();for(var e:b.pages.entrySet())if(!e.getValue().empty()||!e.getValue().begun().isEmpty())pages.put(Integer.toString(e.getKey()),e.getValue().save());
        t.put("Pages",pages);return t;
    }

    public enum Edit { ACCEPTED, NOT_CARRIED, NO_SUCH_PAGE, TOO_MUCH, TOO_FAST }
    /**
     * A page the reader wrote, from their screen. It is kept only if they carry their own journal, it fits what the screen
     * allows and their allowance holds; the words pass the server's text filter before they are kept.
     */
    public static Edit edit(ServerPlayer p,int index,CompoundTag sent){
        var data=get(p.server);
        if(!data.carries(p))return Edit.NOT_CARRIED;
        if(index<1||index>PAGES)return Edit.NO_SUCH_PAGE;
        var page=JournalPage.fromReader(sent);if(page==null)return Edit.TOO_MUCH;
        var b=data.book(p.getUUID());long now=p.server.getTickCount();
        long refill=(now-b.refilled)/REFILL_TICKS;
        if(refill>0){b.tokens=(int)Math.min(TOKENS,b.tokens+refill);b.refilled+=refill*REFILL_TICKS;}
        if(b.tokens>=TOKENS)b.refilled=now;
        if(b.tokens<=0)return Edit.TOO_FAST;
        b.tokens--;
        var words=new ArrayList<String>();words.add(page.text());for(var run:page.placed())words.add(run.text());
        p.getTextFilter().processMessageBundle(words).thenAcceptAsync(filtered->data.keep(p,index,page,filtered),p.server);
        return Edit.ACCEPTED;
    }
    private void keep(ServerPlayer p,int index,JournalPage page,List<FilteredText> filtered){
        if(filtered.size()!=1+page.placed().size())return;
        var runs=new ArrayList<JournalPage.Placed>();
        for(int i=0;i<page.placed().size();i++){var run=page.placed().get(i);String text=filtered.get(i+1).filteredOrEmpty();if(!text.isBlank())runs.add(new JournalPage.Placed(text,run.shape(),run.x(),run.y(),run.angle(),run.size(),run.mirrored()));}
        var b=book(p.getUUID());var before=b.pages.getOrDefault(index,JournalPage.BLANK);
        var kept=before.withWriting(filtered.getFirst().filteredOrEmpty(),runs);
        // A page remembers where it was begun: the first time anything is written on it.
        if(before.begun().isEmpty()&&before.day()==0&&!kept.empty())kept=kept.begunAt(where(p),day(p),HouseDimensions.isHouseDimension(p.level().dimension()));
        if(kept.empty()&&before.begun().isEmpty())b.pages.remove(index);else b.pages.put(index,kept);
        setDirty();
    }
    static String where(ServerPlayer p){
        if(!HouseDimensions.isHouseDimension(p.level().dimension()))return "";
        var origin=HouseSavedData.get(p.server).houseOrigin();
        LabyrinthPlace place=origin==null||!p.level().dimension().equals(HouseDimensions.INTERIOR)?null:LabyrinthPlaces.placeAt(origin,p.blockPosition());
        return place==null?"somewhere in the House":StaircaseProse.place(place.id());
    }
    static int day(ServerPlayer p){return (int)(p.server.overworld().getDayTime()/24000L)+1;}

    /** The first journal: left on the doorstep with Will's letter, addressed in the reader's own hand. Empty once one is issued. */
    public ItemStack parcel(ServerPlayer p){return issue(p.getUUID())>0?ItemStack.EMPTY:issue(p);}
    /** A reader who came after the letter finds theirs the first time they are in the House. */
    public boolean grantIfNone(ServerPlayer p){
        if(issue(p.getUUID())>0)return false;
        give(p,issue(p));
        p.displayClientMessage(Component.literal("A parcel addressed in your own hand was waiting for you: a blank journal. You don't remember sending it."),false);
        return true;
    }
    /** A journal destroyed or lost out in the world waits for its owner on their next arrival in the House, one wear worse. */
    public boolean returnLost(ServerPlayer p){
        if(!lost(p.getUUID())||carries(p))return false;
        give(p,issue(p));
        p.displayClientMessage(Component.literal(wear(p.getUUID())>=WORN?"Your journal is back in your hands, swollen and torn. Every word is still there.":"Your journal is back in your hands, water-stained. Every word is still there."),false);
        return true;
    }
    /** Into the pack rather than the hotbar, so it never lands in a hand that was kept empty for something else. */
    static void give(ServerPlayer p,ItemStack stack){
        var items=p.getInventory().items;
        for(int i=9;i<items.size();i++)if(items.get(i).isEmpty()){p.getInventory().setItem(i,stack);p.inventoryMenu.broadcastChanges();return;}
        if(!p.getInventory().add(stack))p.drop(stack,false);
    }
    public void markLost(ItemStack s){var owner=owner(s);if(owner==null)return;var b=books.get(owner);if(b==null||issueOf(s)!=b.issue)return;b.lost=true;setDirty();}

    @SubscribeEvent(priority=EventPriority.HIGH) public static void expired(ItemExpireEvent e){
        if(e.getExtraLife()>0||!(e.getEntity().level() instanceof net.minecraft.server.level.ServerLevel level)||owner(e.getEntity().getItem())==null)return;
        // Never the Mother's: it goes back to its owner, not onto her shelf.
        get(level.getServer()).markLost(e.getEntity().getItem());e.getEntity().setItem(ItemStack.EMPTY);
    }
    @SubscribeEvent public static void destroyed(EntityLeaveLevelEvent e){
        if(e.getLevel().isClientSide()||!(e.getEntity() instanceof ItemEntity item)||owner(item.getItem())==null)return;
        var reason=item.getRemovalReason();
        boolean gone=reason==Entity.RemovalReason.KILLED||reason==Entity.RemovalReason.DISCARDED&&item.getY()<e.getLevel().getMinBuildHeight()-32;
        if(gone&&e.getLevel() instanceof net.minecraft.server.level.ServerLevel level)get(level.getServer()).markLost(item.getItem());
    }
    /** Each time a reader comes in through the House's front door: their first journal if they have none, or a lost one back. */
    public static void arrived(ServerPlayer p){if(p.isSpectator())return;var data=get(p.server);if(!data.grantIfNone(p))data.returnLost(p);}
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        if(e.getServer().getTickCount()%100!=0)return;
        for(var p:e.getServer().getPlayerList().getPlayers())if(p.isAlive()&&!p.isSpectator()&&HouseDimensions.isHouseDimension(p.level().dimension()))get(e.getServer()).returnLost(p);
    }

    public static Journals load(CompoundTag tag,HolderLookup.Provider registries){
        var data=new Journals();var all=tag.getCompound("Books");
        for(String key:all.getAllKeys()){
            UUID id;try{id=UUID.fromString(key);}catch(IllegalArgumentException ignored){continue;}
            var t=all.getCompound(key);var b=new Book();b.issue=t.getInt("Issue");b.wear=t.getInt("Wear");b.lost=t.getBoolean("Lost");b.name=t.getString("Name");
            var pages=t.getCompound("Pages");for(String k:pages.getAllKeys()){try{int i=Integer.parseInt(k);if(i>=1&&i<=PAGES)b.pages.put(i,JournalPage.load(pages.getCompound(k)));}catch(NumberFormatException ignored){}}
            data.books.put(id,b);
        }
        return data;
    }
    @Override public CompoundTag save(CompoundTag tag,HolderLookup.Provider registries){
        var all=new CompoundTag();
        for(var e:books.entrySet()){var b=e.getValue();var t=new CompoundTag();t.putInt("Issue",b.issue);t.putInt("Wear",b.wear);if(b.lost)t.putBoolean("Lost",true);t.putString("Name",b.name);
            var pages=new CompoundTag();for(var p:b.pages.entrySet())pages.put(Integer.toString(p.getKey()),p.getValue().save());t.put("Pages",pages);all.put(e.getKey().toString(),t);}
        tag.put("Books",all);return tag;
    }
}
