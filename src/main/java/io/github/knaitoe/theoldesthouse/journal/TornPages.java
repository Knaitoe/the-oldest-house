package io.github.knaitoe.theoldesthouse.journal;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseLayout;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.network.JournalOpenPayload;
import io.github.knaitoe.theoldesthouse.network.TornPagePayload;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

/**
 * 0.4.199, journal step 5 (JOURNAL_DESIGN.md, "Co-op, and the reader's own wall"): a reader tears a written page out of their
 * journal and it becomes a page anyone can hold and read, "Torn from <name>'s journal". The journal keeps the stub. A torn page
 * pins to a wall or lies on a table, showing a few tiny lines, and opens in full to a right-click; dropped in the House it stays
 * where it fell. In the manor a reader may pin as many as they like, a wall of their own; out in the rest of the House six of a
 * reader's pages may be left at a time, and a seventh sends the oldest back into its stub. The writer can take back their own page
 * at any time, wherever it is. The words pass the server's text filter as they are torn, and an operator can send every page of a
 * reader home with one command. Like the journal, the torn page is only a handle: its words stay in the journal's saved data, so
 * a copied stack is a blank one, and a page lost in the world goes back into its stub rather than being lost.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class TornPages {
    public static final int LIMIT=6, GLANCE_LINES=4, GLANCE_WIDTH=24;
    public static final String OWNER="TornOwner", PAGE="TornPage", SERIAL="TornSerial", NAME="TornName";
    private TornPages(){}

    public enum Where { CARRIED, PINNED, DROPPED }
    /** A page out of its journal: which copy it is, its words, and where it was last left. */
    static final class Torn {
        int serial; JournalPage page=JournalPage.BLANK; Where where=Where.CARRIED;
        String dim=""; long pos; @Nullable UUID entity; long order; boolean counted;
        CompoundTag save(){var t=new CompoundTag();t.putInt("Serial",serial);t.put("Page",page.save());t.putString("Where",where.name());
            if(!dim.isEmpty()){t.putString("Dim",dim);t.putLong("Pos",pos);}if(entity!=null)t.putUUID("Entity",entity);if(order>0)t.putLong("Order",order);if(counted)t.putBoolean("Counted",true);return t;}
        static Torn load(CompoundTag t){var torn=new Torn();torn.serial=t.getInt("Serial");torn.page=JournalPage.load(t.getCompound("Page"));
            try{torn.where=Where.valueOf(t.getString("Where"));}catch(IllegalArgumentException ignored){}
            torn.dim=t.getString("Dim");torn.pos=t.getLong("Pos");torn.entity=t.hasUUID("Entity")?t.getUUID("Entity"):null;torn.order=t.getLong("Order");torn.counted=t.getBoolean("Counted");return torn;}
    }
    /** Which torn page a stack or a pinned page is: its writer, the stub it came from and which copy. */
    public record Ref(UUID owner,int page,int serial){}

    // ------------------------------------------------------------------ the handle
    public static ItemStack stack(UUID owner,String name,int page,int serial){
        var s=new ItemStack(JournalRegistry.TORN_PAGE.get());
        CustomData.update(DataComponents.CUSTOM_DATA,s,t->{t.putUUID(OWNER,owner);t.putInt(PAGE,page);t.putInt(SERIAL,serial);t.putString(NAME,name);});
        return s;
    }
    public static @Nullable Ref ref(ItemStack s){
        if(!s.is(JournalRegistry.TORN_PAGE.get()))return null;var d=s.get(DataComponents.CUSTOM_DATA);if(d==null)return null;var t=d.copyTag();
        return t.hasUUID(OWNER)?new Ref(t.getUUID(OWNER),t.getInt(PAGE),t.getInt(SERIAL)):null;
    }
    public static String nameOf(ItemStack s){var d=s.get(DataComponents.CUSTOM_DATA);return d==null?"":d.copyTag().getString(NAME);}
    /** The page a handle points to, while it is still that page: null for a copy, or for a page taken back. */
    static @Nullable Torn torn(Journals data,@Nullable Ref ref){
        if(ref==null)return null;var b=data.books().get(ref.owner());if(b==null)return null;var t=b.torn.get(ref.page());
        return t!=null&&t.serial==ref.serial()?t:null;
    }
    public static boolean valid(MinecraftServer server,@Nullable Ref ref){return torn(Journals.get(server),ref)!=null;}
    public static @Nullable JournalPage page(MinecraftServer server,@Nullable Ref ref){var t=torn(Journals.get(server),ref);return t==null?null:t.page.unquoted();}

    // ------------------------------------------------------------------ tearing
    public enum Tear { TEARING, NOT_CARRIED, NO_SUCH_PAGE, BLANK, ALREADY }
    /**
     * Tears a page out of the reader's own journal, from their screen. Only a page with writing on it; the words pass the text
     * filter first, and then the page is theirs to hold and the stub stays in the book.
     */
    public static Tear tear(ServerPlayer p,int index){
        var data=Journals.get(p.server);
        if(!data.carries(p))return Tear.NOT_CARRIED;
        if(index<1||index>Journals.PAGES)return Tear.NO_SUCH_PAGE;
        var b=data.book(p.getUUID());if(b.torn.containsKey(index))return Tear.ALREADY;
        var page=b.pages.get(index);if(page==null||page.text().isBlank())return Tear.BLANK;
        p.getTextFilter().processMessageBundle(List.of(page.text())).thenAcceptAsync(filtered->tornOut(p,index,filtered),p.server);
        return Tear.TEARING;
    }
    private static void tornOut(ServerPlayer p,int index,List<FilteredText> filtered){
        var data=Journals.get(p.server);var b=data.book(p.getUUID());var page=b.pages.get(index);
        if(filtered.size()!=1||page==null||b.torn.containsKey(index)||!data.carries(p))return;
        String text=filtered.getFirst().filteredOrEmpty();
        if(text.isBlank()){p.displayClientMessage(Component.literal("Nothing on the page would come away."),true);return;}
        var torn=new Torn();torn.serial=++b.tornSerial;torn.page=page.withWriting(text,page.placed());
        b.pages.remove(index);b.torn.put(index,torn);Quotes.torn(b,index);data.setDirty();
        Journals.give(p,stack(p.getUUID(),b.name.isEmpty()?p.getGameProfile().getName():b.name,index,torn.serial));
        p.serverLevel().playSound(null,p.blockPosition(),SoundEvents.BOOK_PAGE_TURN,SoundSource.PLAYERS,1F,.55F);
        reopen(p,index);
    }
    /** The reader's screen, if it is open, shows the book as it is now. */
    static void reopen(ServerPlayer p,int index){
        if(CoAuthor.open(p.getUUID()).isEmpty())return;var contents=Journals.get(p.server).contents(p.getUUID());contents.putInt("Spread",index/2);
        HousePackets.send(p,new JournalOpenPayload(contents));
    }

    // ------------------------------------------------------------------ reading
    /** Opens a torn page in full for whoever holds or looks at it. A copy, or a page taken back, is blank. */
    public static boolean read(ServerPlayer reader,@Nullable Ref ref,String name){
        var page=page(reader.server,ref);
        if(page==null){reader.displayClientMessage(Component.literal("The page is blank."),true);return false;}
        HousePackets.send(reader,new TornPagePayload(name,page.save()));
        reader.serverLevel().playSound(null,reader.blockPosition(),SoundEvents.BOOK_PAGE_TURN,SoundSource.PLAYERS,.5F,1.1F);
        return true;
    }
    /** The few tiny lines a pinned page shows at a glance: its first words, wrapped short. */
    public static String glance(JournalPage page){
        var lines=new ArrayList<String>();
        for(String para:page.text().split("\n")){
            var line=new StringBuilder();
            for(String word:para.trim().split("\\s+")){
                if(word.isEmpty())continue;
                if(line.length()>0&&line.length()+1+word.length()>GLANCE_WIDTH){lines.add(line.toString());line.setLength(0);if(lines.size()>=GLANCE_LINES)break;}
                if(line.length()>0)line.append(' ');line.append(word.length()>GLANCE_WIDTH?word.substring(0,GLANCE_WIDTH):word);
            }
            if(lines.size()>=GLANCE_LINES)break;if(line.length()>0)lines.add(line.toString());if(lines.size()>=GLANCE_LINES)break;
        }
        return String.join("\n",lines.subList(0,Math.min(lines.size(),GLANCE_LINES)));
    }

    // ------------------------------------------------------------------ where it is left
    /** In the manor a reader may leave as many pages as they like; anywhere else in the House their pages count toward the six. */
    public static boolean counts(ServerLevel l,BlockPos pos){
        if(!HouseDimensions.isHouseDimension(l.dimension()))return false;
        var origin=HouseSavedData.get(l.getServer()).houseOrigin();
        return !(origin!=null&&l.dimension().equals(HouseDimensions.INTERIOR)&&HouseLayout.isInsideDomesticVolume(pos.getX()+.5-origin.getX(),pos.getY()-origin.getY(),pos.getZ()+.5-origin.getZ()));
    }
    /** A page has been pinned or dropped: where it lies, and, if it was left somewhere new, its turn in the order. Past six, the oldest goes home. */
    static void left(ServerLevel l,Ref ref,Where where,Entity e){
        var data=Journals.get(l.getServer());var t=torn(data,ref);if(t==null)return;
        boolean moved=t.where!=where||!e.getUUID().equals(t.entity);
        t.where=where;t.dim=l.dimension().location().toString();t.pos=e.blockPosition().asLong();t.entity=e.getUUID();t.counted=counts(l,e.blockPosition());
        if(moved)t.order=++data.tornOrder;
        data.setDirty();
        if(t.counted)evict(l.getServer(),ref.owner());
    }
    static void carried(MinecraftServer server,Ref ref){var data=Journals.get(server);var t=torn(data,ref);if(t==null)return;t.where=Where.CARRIED;t.entity=null;t.dim="";t.counted=false;data.setDirty();}
    /** How many of a reader's pages are left out in the House, beyond the manor. */
    public static int out(MinecraftServer server,UUID owner){var b=Journals.get(server).books().get(owner);return b==null?0:(int)b.torn.values().stream().filter(t->t.counted).count();}
    static void evict(MinecraftServer server,UUID owner){
        var b=Journals.get(server).books().get(owner);if(b==null)return;
        while(true){
            int oldest=-1;long at=Long.MAX_VALUE;int count=0;
            for(var e:b.torn.entrySet())if(e.getValue().counted){count++;if(e.getValue().order<at){at=e.getValue().order;oldest=e.getKey();}}
            if(count<=LIMIT||oldest<0)return;
            takeBack(server,owner,oldest,"One of the pages you left in the House has found its way back into your journal.");
        }
    }

    // ------------------------------------------------------------------ taking back
    /**
     * A torn page back into its stub, wherever it is: a pinned or dropped page in the world goes, and any held copy goes blank.
     * Returns whether there was a page to take back. A line goes to the writer if they are about and a reason is given.
     */
    public static boolean takeBack(MinecraftServer server,UUID owner,int index,@Nullable String why){
        var data=Journals.get(server);var b=data.books().get(owner);if(b==null)return false;var t=b.torn.remove(index);if(t==null)return false;
        b.pages.put(index,t.page);data.setDirty();
        if(t.entity!=null&&!t.dim.isEmpty()){
            var level=server.getLevel(ResourceKey.create(Registries.DIMENSION,ResourceLocation.parse(t.dim)));
            var e=level==null?null:level.getEntity(t.entity);if(e!=null)e.discard();
        }
        var writer=server.getPlayerList().getPlayer(owner);
        if(writer!=null){
            if(why!=null)writer.displayClientMessage(Component.literal(why).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),false);
            writer.serverLevel().playSound(null,writer.blockPosition(),SoundEvents.BOOK_PUT,SoundSource.PLAYERS,.6F,1F);
            reopen(writer,index);
        }
        return true;
    }
    /** The writer takes their own page back: from the stub in their journal, or from the page itself. */
    public static boolean reclaim(ServerPlayer p,int index){
        var data=Journals.get(p.server);if(!data.carries(p))return false;
        return takeBack(p.server,p.getUUID(),index,null);
    }
    /** Moderation: every torn page of a reader goes back into its stub, wherever it was. Returns how many. */
    public static int clear(MinecraftServer server,UUID owner){
        var b=Journals.get(server).books().get(owner);if(b==null)return 0;int n=0;
        for(int index:new ArrayList<>(b.torn.keySet()))if(takeBack(server,owner,index,null))n++;
        return n;
    }
    /** A torn page burnt, crushed or expired in the world is not lost: it goes back into its stub. True if the stack was one. */
    static boolean lostInWorld(ServerLevel level,ItemStack s){
        var ref=ref(s);if(ref==null)return false;
        if(torn(Journals.get(level.getServer()),ref)!=null)takeBack(level.getServer(),ref.owner(),ref.page(),"A page you tore out has found its way back into your journal.");
        return true;
    }

    // ------------------------------------------------------------------ the world
    /** A dropped page in the House stays where it fell; a copy, or a page taken back, never comes into the world at all. */
    @SubscribeEvent public static void joined(EntityJoinLevelEvent e){
        if(!(e.getLevel() instanceof ServerLevel l))return;
        if(e.getEntity() instanceof ItemEntity item){
            var ref=ref(item.getItem());if(ref==null)return;var t=torn(Journals.get(l.getServer()),ref);
            if(t==null||t.where==Where.PINNED){e.setCanceled(true);return;}
            if(HouseDimensions.isHouseDimension(l.dimension()))item.setUnlimitedLifetime();
            left(l,ref,Where.DROPPED,item);
        }else if(e.getEntity() instanceof PinnedPage pinned){
            var t=torn(Journals.get(l.getServer()),pinned.ref());
            if(t==null||t.where!=Where.PINNED||!pinned.getUUID().equals(t.entity))e.setCanceled(true);
        }
    }
    @SubscribeEvent public static void pickedUp(ItemEntityPickupEvent.Post e){
        if(e.getPlayer().level().isClientSide())return;var ref=ref(e.getOriginalStack());if(ref!=null)carried(e.getPlayer().getServer(),ref);
    }
    public static ResourceKey<Level> dimension(String id){return ResourceKey.create(Registries.DIMENSION,ResourceLocation.parse(id));}
}
