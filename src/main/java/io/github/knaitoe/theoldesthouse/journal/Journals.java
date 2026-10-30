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
        /** The House day the other hand last wrote here (CoAuthor). */
        int inked;
        final Map<Integer,JournalPage> pages=new HashMap<>();
        /** Which object each rubbing was taken from, and the page it went on. */
        final Map<String,Integer> rubbed=new HashMap<>();
        /** Pages torn out (TornPages), by the stub they came from; the serial says which torn copy is the page now. */
        final Map<Integer,TornPages.Torn> torn=new HashMap<>();
        int tornSerial;
        // Not saved: the screen's allowance.
        int tokens=TOKENS; long refilled;
    }
    private final Map<UUID,Book> books=new HashMap<>();
    /** The order torn pages were left out in the world, across every reader (TornPages). */
    long tornOrder;

    public static Journals get(MinecraftServer server){return server.overworld().getDataStorage().computeIfAbsent(FACTORY,DATA_NAME);}
    Book book(UUID id){return books.computeIfAbsent(id,k->new Book());}
    Map<UUID,Book> books(){return books;}

    public int issue(UUID id){var b=books.get(id);return b==null?0:b.issue;}
    public int wear(UUID id){var b=books.get(id);return b==null?0:b.wear;}
    public boolean lost(UUID id){var b=books.get(id);return b!=null&&b.lost;}
    public JournalPage page(UUID id,int index){var b=books.get(id);return b==null?JournalPage.BLANK:b.pages.getOrDefault(index,JournalPage.BLANK);}
    /** Whether this page has been torn out, leaving its stub. */
    public boolean torn(UUID id,int index){var b=books.get(id);return b!=null&&b.torn.containsKey(index);}
    /** A page set as the House keeps it, header and all: the tests' own pages, and later the House's own writing. */
    public void put(UUID id,int index,JournalPage page){if(index<1||index>PAGES)return;book(id).pages.put(index,page);setDirty();}
    /** The House day the other hand last wrote in this reader's journal, and setting it back (the tests' way to a new day). */
    public int inked(UUID id){var b=books.get(id);return b==null?0:b.inked;}
    public void inked(UUID id,int day){book(id).inked=day;setDirty();}

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

    public static void open(ServerPlayer p,ItemStack s){open(p,s,0);}
    /** Opens the reader's own journal at the spread holding the given page. */
    static void open(ServerPlayer p,ItemStack s,int page){
        var data=get(p.server);
        switch(data.check(p,s)){
            case OPEN->{var contents=data.contents(p.getUUID());if(page>0)contents.putInt("Spread",page/2);io.github.knaitoe.theoldesthouse.network.HousePackets.send(p,new JournalOpenPayload(contents));p.serverLevel().playSound(null,p.blockPosition(),SoundEvents.BOOK_PAGE_TURN,SoundSource.PLAYERS,.6F,1);}
            case NOT_YOURS->p.displayClientMessage(Component.literal("It isn't yours. A name is written inside the cover: "+nameOf(s)+"."),true);
            case OLD_ISSUE->p.displayClientMessage(Component.literal("The pages are blank. This isn't your journal any more."),true);
            case NOT_A_JOURNAL->{}
        }
    }
    /** Everything the reader's screen draws: the name in the cover, the wear and every written page. */
    public CompoundTag contents(UUID id){
        var b=book(id);var t=new CompoundTag();t.putString("Name",b.name);t.putInt("Wear",b.wear);
        var pages=new CompoundTag();for(var e:b.pages.entrySet())if(!e.getValue().empty()||!e.getValue().begun().isEmpty())pages.put(Integer.toString(e.getKey()),e.getValue().save());
        t.put("Pages",pages);t.putIntArray("Torn",b.torn.keySet().stream().mapToInt(Integer::intValue).sorted().toArray());return t;
    }

    public enum Edit { ACCEPTED, NOT_CARRIED, NO_SUCH_PAGE, TOO_MUCH, TOO_FAST, TORN }
    /**
     * A page the reader wrote, from their screen. It is kept only if they carry their own journal, it fits what the screen
     * allows and their allowance holds; the words pass the server's text filter before they are kept. Only the writing is
     * the reader's: lines set on the page in shapes are the House's alone.
     */
    public static Edit edit(ServerPlayer p,int index,CompoundTag sent){
        var data=get(p.server);
        if(!data.carries(p))return Edit.NOT_CARRIED;
        if(index<1||index>PAGES)return Edit.NO_SUCH_PAGE;
        if(data.torn(p.getUUID(),index))return Edit.TORN;
        var page=JournalPage.fromReader(sent);if(page==null)return Edit.TOO_MUCH;
        var b=data.book(p.getUUID());long now=p.server.getTickCount();
        long refill=(now-b.refilled)/REFILL_TICKS;
        if(refill>0){b.tokens=(int)Math.min(TOKENS,b.tokens+refill);b.refilled+=refill*REFILL_TICKS;}
        if(b.tokens>=TOKENS)b.refilled=now;
        if(b.tokens<=0)return Edit.TOO_FAST;
        b.tokens--;
        p.getTextFilter().processMessageBundle(List.of(page.text())).thenAcceptAsync(filtered->data.keep(p,index,filtered),p.server);
        return Edit.ACCEPTED;
    }
    private void keep(ServerPlayer p,int index,List<FilteredText> filtered){
        if(filtered.size()!=1)return;
        var b=book(p.getUUID());if(b.torn.containsKey(index))return;var before=b.pages.getOrDefault(index,JournalPage.BLANK);
        // The reader writes; any shaped lines already on the page were set there by the House and stay as they are.
        var kept=before.withWriting(filtered.getFirst().filteredOrEmpty(),before.placed());
        // A page remembers where it was begun: the first time anything is written on it.
        if(before.begun().isEmpty()&&before.day()==0&&!kept.empty())kept=kept.begunAt(where(p),day(p),HouseDimensions.isHouseDimension(p.level().dimension()));
        if(kept.empty()&&before.begun().isEmpty())b.pages.remove(index);else b.pages.put(index,kept);
        setDirty();
    }
    public enum Rub { RUBBED, TURNED, NOT_RUBBABLE, NOT_YOURS, FULL }
    /** A page has room for a rubbing below its writing while it has none yet, nothing of the other hand's, and no more than this. */
    static final int ROOM_CHARS=180, ROOM_LINES=8;
    public static final float RUBBING_X=.08F, RUBBING_Y=.55F;
    /**
     * A pencil rubbing of an authored object, taken with the reader's own journal: charcoal and one line about it, laid below
     * the writing on the last written page if it has room, or the next one that does. Once per object for each reader; a
     * second try only turns to that page. It is no reward, story step or advancement and changes nothing in the room.
     */
    public static Rub rub(ServerPlayer p,ItemStack journal,net.minecraft.world.level.block.state.BlockState state){
        var kind=Rubbings.of(state);if(kind==null)return Rub.NOT_RUBBABLE;
        var data=get(p.server);
        if(data.check(p,journal)!=Opening.OPEN){open(p,journal);return Rub.NOT_YOURS;}
        var b=data.book(p.getUUID());var done=b.rubbed.get(kind.id());
        if(done!=null){open(p,journal,done);return Rub.TURNED;}
        int index=room(b);
        if(index<0){p.displayClientMessage(Component.literal("There is no room left in the journal for it."),true);return Rub.FULL;}
        var before=b.pages.getOrDefault(index,JournalPage.BLANK);
        var kept=before.withRubbing(new JournalPage.Rubbing(kind.id(),RUBBING_X,RUBBING_Y,tilt(kind.id())));
        if(before.begun().isEmpty()&&before.day()==0)kept=kept.begunAt(where(p),day(p),HouseDimensions.isHouseDimension(p.level().dimension()));
        b.pages.put(index,kept);b.rubbed.put(kind.id(),index);data.setDirty();
        p.serverLevel().playSound(null,p.blockPosition(),SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT,SoundSource.PLAYERS,.5F,.7F);
        open(p,journal,index);return Rub.RUBBED;
    }
    /** The paper is never laid quite square. */
    static float tilt(String id){return (Math.floorMod(id.hashCode(),9)-4)*1.2F;}
    /** The last written page if a rubbing fits below its writing, otherwise the next page that has room. */
    static int room(Book b){
        int last=1;for(var e:b.pages.entrySet())if(!e.getValue().empty())last=Math.max(last,e.getKey());
        for(int i=last;i<=PAGES;i++){if(b.torn.containsKey(i))continue;var page=b.pages.getOrDefault(i,JournalPage.BLANK);
            if(page.rubbings().isEmpty()&&page.notes().isEmpty()&&page.text().length()<=ROOM_CHARS&&page.text().chars().filter(c->c=='\n').count()<=ROOM_LINES)return i;}
        return -1;
    }
    public int rubbedPage(UUID id,String kind){var b=books.get(id);var page=b==null?null:b.rubbed.get(kind);return page==null?-1:page;}
    /**
     * A hand holding a journal on a rubbable object takes the rubbing, after the room has answered the click as it always does.
     * 0.4.199 (owner: "let players interact with objects with charcoal to make the charcoal sketches in their books"): a hand holding
     * charcoal does the same into the reader's own journal, wherever it is on them.
     */
    @SubscribeEvent(priority=EventPriority.LOWEST,receiveCanceled=true) public static void rubbing(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock e){
        if(e.getEntity().isSpectator())return;
        var held=e.getItemStack();var journal=owner(held)!=null?held:held.is(net.minecraft.world.item.Items.CHARCOAL)?carried(e.getEntity()):ItemStack.EMPTY;
        if(journal.isEmpty())return;
        var state=e.getLevel().getBlockState(e.getPos());if(Rubbings.of(state)==null)return;
        if(e.getEntity() instanceof ServerPlayer p)rub(p,journal,state);
        e.setCanceled(true);e.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
    }
    /** The reader's own journal, anywhere on them (on the server, the current one before an old copy), or nothing. */
    public static ItemStack carried(net.minecraft.world.entity.player.Player p){
        var inventory=p.getInventory();ItemStack any=ItemStack.EMPTY;
        for(int i=0;i<inventory.getContainerSize();i++){var s=inventory.getItem(i);if(!p.getUUID().equals(owner(s)))continue;
            if(!(p instanceof ServerPlayer sp)||get(sp.server).check(sp,s)==Opening.OPEN)return s;if(any.isEmpty())any=s;}
        return any;
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
        if(e.getExtraLife()<=0&&e.getEntity().level() instanceof net.minecraft.server.level.ServerLevel level&&TornPages.lostInWorld(level,e.getEntity().getItem()))return;
        if(e.getExtraLife()>0||!(e.getEntity().level() instanceof net.minecraft.server.level.ServerLevel level)||owner(e.getEntity().getItem())==null)return;
        // Never the Mother's: it goes back to its owner, not onto her shelf.
        get(level.getServer()).markLost(e.getEntity().getItem());e.getEntity().setItem(ItemStack.EMPTY);
    }
    @SubscribeEvent public static void destroyed(EntityLeaveLevelEvent e){
        if(e.getLevel().isClientSide()||!(e.getEntity() instanceof ItemEntity item))return;
        var reason=item.getRemovalReason();
        boolean gone=reason==Entity.RemovalReason.KILLED||reason==Entity.RemovalReason.DISCARDED&&item.getY()<e.getLevel().getMinBuildHeight()-32;
        if(gone&&e.getLevel() instanceof net.minecraft.server.level.ServerLevel level&&TornPages.lostInWorld(level,item.getItem()))return;
        if(owner(item.getItem())==null)return;
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
            var t=all.getCompound(key);var b=new Book();b.issue=t.getInt("Issue");b.wear=t.getInt("Wear");b.lost=t.getBoolean("Lost");b.name=t.getString("Name");b.inked=t.getInt("Inked");
            var pages=t.getCompound("Pages");for(String k:pages.getAllKeys()){try{int i=Integer.parseInt(k);if(i>=1&&i<=PAGES)b.pages.put(i,JournalPage.load(pages.getCompound(k)));}catch(NumberFormatException ignored){}}
            var rubbed=t.getCompound("Rubbed");for(String k:rubbed.getAllKeys())if(Rubbings.byId(k)!=null)b.rubbed.put(k,rubbed.getInt(k));
            b.tornSerial=t.getInt("TornSerial");var torn=t.getCompound("Torn");
            for(String k:torn.getAllKeys()){try{int i=Integer.parseInt(k);if(i>=1&&i<=PAGES)b.torn.put(i,TornPages.Torn.load(torn.getCompound(k)));}catch(NumberFormatException ignored){}}
            data.books.put(id,b);
        }
        data.tornOrder=tag.getLong("TornOrder");
        return data;
    }
    @Override public CompoundTag save(CompoundTag tag,HolderLookup.Provider registries){
        var all=new CompoundTag();
        for(var e:books.entrySet()){var b=e.getValue();var t=new CompoundTag();t.putInt("Issue",b.issue);t.putInt("Wear",b.wear);if(b.lost)t.putBoolean("Lost",true);t.putString("Name",b.name);if(b.inked>0)t.putInt("Inked",b.inked);
            var pages=new CompoundTag();for(var p:b.pages.entrySet())pages.put(Integer.toString(p.getKey()),p.getValue().save());t.put("Pages",pages);
            if(!b.rubbed.isEmpty()){var rubbed=new CompoundTag();b.rubbed.forEach(rubbed::putInt);t.put("Rubbed",rubbed);}
            if(b.tornSerial>0)t.putInt("TornSerial",b.tornSerial);
            if(!b.torn.isEmpty()){var torn=new CompoundTag();b.torn.forEach((i,page)->torn.put(Integer.toString(i),page.save()));t.put("Torn",torn);}
            all.put(e.getKey().toString(),t);}
        tag.put("Books",all);if(tornOrder>0)tag.putLong("TornOrder",tornOrder);return tag;
    }
}
