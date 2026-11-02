package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * 0.4.255, the hall drawers (docs/HALL_DRAWERS_0_4_255.md), the owner's rule of 2 November: the House takes from a reader everything
 * they carry in that it did not provide, that is not eaten or drunk, and that is not a light it can work with (a torch, a lantern, a
 * candle), so nothing brought from outside can be built with or used to get round its rooms. It keeps them for the reader in a chest
 * of drawers in the Navidsons' front hall, just inside the front door.
 * <p>
 * The House never marks or changes an item. It keeps three lists for each reader:
 * <ul>
 * <li><b>The drawer.</b> What it took, exactly as it was.</li>
 * <li><b>Loose.</b> The reader's own things taken back out of the drawer and still inside the manor, in hand or put down. Carried past
 * the manor into the rest of the House, they go back to the drawer.</li>
 * <li><b>Carried out.</b> Things the House gave the reader that they carried out into the world, so they come back in with them, however
 * long they were away (the last 256 stacks).</li>
 * </ul>
 * Where a reader is: outside (not in any of the House's dimensions), in the manor (the Navidsons' rooms) or beyond it (the hallway, the
 * rooms and everything after). Crossing from outside to inside, the House takes what it did not provide; crossing from the manor beyond,
 * it takes back the reader's own things taken out of the drawer; going out, it notes what of its own the reader carries. A reader it has
 * not seen before is taken to be where they stand. Creative and spectating players are left alone.
 * <p>
 * Allowed in: the mod's own items; any item carrying custom data (only the House makes those in ordinary play); food and drink (not
 * chorus fruit, which moves the eater); torches, soul torches, lanterns, soul lanterns and candles.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class HouseKeeping extends SavedData {
    public static final String DATA_NAME="the_oldest_house_keeping";
    public static final Factory<HouseKeeping> FACTORY=new Factory<>(HouseKeeping::new,HouseKeeping::load);
    /** The chest of drawers in the front hall, against its west wall two blocks in from the front door, relative to the manor's origin. */
    public static final BlockPos DRAWERS=new BlockPos(HouseLayout.HALL_MIN_X,1,HouseLayout.FRONT_DOOR_Z+2);
    /** Stacks to a page of the drawer: a chest's six rows. */
    public static final int SHOWN=54;
    /** How many of a reader's taken stacks the House remembers the slots of (0.4.267); the oldest are forgotten first. */
    static final int HOMES=256;
    /** The drawer's buttons (0.4.267), sent as vanilla menu-button clicks. */
    public static final int RETRIEVE=0,PREVIOUS=1,NEXT=2;
    /** How many stacks of its own things the House remembers a reader having carried out; the oldest are forgotten first. */
    static final int CARRIED_LIMIT=256;
    public enum Where{OUTSIDE,MANOR,BEYOND}

    /** One reader's lists, and where the House last saw them. */
    public static final class Keep {
        public final List<ItemStack> drawer=new ArrayList<>(),loose=new ArrayList<>(),carried=new ArrayList<>();
        /** 0.4.267: where in the reader's inventory each thing was when the House took it, newest first, so it can go back there. */
        public final List<Home> homes=new ArrayList<>();
        @Nullable public Where where;
    }
    /** A slot something was taken from (0.4.267): which thing, which inventory slot, and how many of it are still to go back there. */
    public static final class Home {
        public final ItemStack like;public final int slot;public int count;
        Home(ItemStack like,int slot,int count){this.like=like.copyWithCount(1);this.slot=slot;this.count=count;}
    }
    private final Map<UUID,Keep> keeps=new HashMap<>();
    public static HouseKeeping get(MinecraftServer server){return server.overworld().getDataStorage().computeIfAbsent(FACTORY,DATA_NAME);}
    public Keep keep(UUID id){return keeps.computeIfAbsent(id,k->new Keep());}
    public static Keep of(ServerPlayer p){return get(p.server).keep(p.getUUID());}
    /** Forgets a reader entirely (tests). */
    public void forget(UUID id){keeps.remove(id);setDirty();}

    // ------------------------------------------------------------------ what the House allows
    /** Whether a reader may carry this past the House's doors: its own, House-made, eaten or drunk, or a light. */
    public static boolean allowed(ItemStack s){
        if(s.isEmpty())return true;
        if(BuiltInRegistries.ITEM.getKey(s.getItem()).getNamespace().equals(TheOldestHouse.MOD_ID))return true;
        var data=s.get(DataComponents.CUSTOM_DATA);if(data!=null&&!data.isEmpty())return true;
        if(s.is(Items.CHORUS_FRUIT))return false;
        if(s.has(DataComponents.FOOD))return true;
        if(s.is(Items.POTION)||s.is(Items.MILK_BUCKET)||s.is(Items.HONEY_BOTTLE)||s.is(Items.OMINOUS_BOTTLE))return true;
        return s.is(Items.TORCH)||s.is(Items.SOUL_TORCH)||s.is(Items.LANTERN)||s.is(Items.SOUL_LANTERN)||s.is(ItemTags.CANDLES);
    }
    /** The same thing, wear aside: a sword a little more worn is still the reader's sword. */
    static boolean same(ItemStack a,ItemStack b){
        if(a.getItem()!=b.getItem())return false;
        var x=a.copyWithCount(1);var y=b.copyWithCount(1);x.remove(DataComponents.DAMAGE);y.remove(DataComponents.DAMAGE);
        return ItemStack.isSameItemSameComponents(x,y);
    }
    /** Takes up to {@code count} of {@code s} off a list, and says how many it found. */
    static int take(List<ItemStack> list,ItemStack s,int count){
        int found=0;
        for(var it=list.iterator();it.hasNext()&&found<count;){var e=it.next();if(!same(e,s))continue;int n=Math.min(e.getCount(),count-found);e.shrink(n);found+=n;if(e.isEmpty())it.remove();}
        return found;
    }
    /** Adds to a list, topping up stacks of the same thing first. */
    static void add(List<ItemStack> list,ItemStack s){
        var left=s.copy();
        for(var e:list){if(left.isEmpty())return;if(ItemStack.isSameItemSameComponents(e,left)&&e.getCount()<e.getMaxStackSize()){int n=Math.min(left.getCount(),e.getMaxStackSize()-e.getCount());e.grow(n);left.shrink(n);}}
        while(!left.isEmpty()){list.add(left.split(Math.min(left.getCount(),left.getMaxStackSize())));}
    }
    /** Every slot a reader carries things in: the inventory, armour and off hand, the two-by-two crafting grid and the cursor. */
    static List<Container> pockets(ServerPlayer p){
        var cursor=new SimpleContainer(1){
            @Override public ItemStack getItem(int i){return p.containerMenu.getCarried();}
            @Override public void setItem(int i,ItemStack s){p.containerMenu.setCarried(s);}
        };
        return List.of(p.getInventory(),p.inventoryMenu.getCraftSlots(),cursor);
    }

    // ------------------------------------------------------------------ crossing the House's doors
    public static Where where(ServerPlayer p){
        if(!HouseDimensions.isHouseDimension(p.level().dimension()))return Where.OUTSIDE;
        return HouseDays.isInManor(p)?Where.MANOR:Where.BEYOND;
    }
    @SubscribeEvent public static void tick(PlayerTickEvent.Post e){
        if(!(e.getEntity() instanceof ServerPlayer p)||p.level().isClientSide())return;
        var mode=p.gameMode.getGameModeForPlayer();if(mode==GameType.CREATIVE||mode==GameType.SPECTATOR)return;
        update(p,where(p));
    }
    /** The reader is now {@code now}: whatever crossing that is, the House does its part. */
    public static void update(ServerPlayer p,Where now){
        var data=get(p.server);var keep=data.keep(p.getUUID());var was=keep.where;if(was==now)return;
        keep.where=now;data.setDirty();
        if(was==null)return;
        if(was==Where.OUTSIDE)enter(p,data,keep);
        else if(now==Where.OUTSIDE)leave(p,data,keep);
        else if(was==Where.MANOR&&now==Where.BEYOND)cross(p,data,keep);
    }
    /** In from outside: everything the House did not provide goes to the drawer. */
    static void enter(ServerPlayer p,HouseKeeping data,Keep keep){
        int taken=0;
        for(var c:pockets(p))for(int i=0;i<c.getContainerSize();i++){
            var s=c.getItem(i);if(allowed(s))continue;
            int ours=take(keep.carried,s,s.getCount());int theirs=s.getCount()-ours;if(theirs<=0)continue;
            add(keep.drawer,s.copyWithCount(theirs));
            if(c==p.getInventory())keep.homes.add(0,new Home(s,i,theirs));
            var left=s.copyWithCount(ours);c.setItem(i,left.isEmpty()?ItemStack.EMPTY:left);taken+=theirs;
        }
        while(keep.homes.size()>HOMES)keep.homes.remove(keep.homes.size()-1);
        if(taken>0){data.setDirty();p.inventoryMenu.broadcastChanges();
            say(p,"What you carried in from outside, the House has put away in the chest of drawers in the front hall.");
            p.playNotifySound(SoundEvents.CHISELED_BOOKSHELF_INSERT,SoundSource.PLAYERS,.8F,.8F);}
    }
    /** From the manor into the rest of the House: the reader's own things taken out of the drawer go back to it. */
    static void cross(ServerPlayer p,HouseKeeping data,Keep keep){
        if(keep.loose.isEmpty())return;
        int taken=0;
        for(var c:pockets(p))for(int i=0;i<c.getContainerSize();i++){
            var s=c.getItem(i);if(allowed(s))continue;
            int own=take(keep.loose,s,s.getCount());if(own<=0)continue;
            add(keep.drawer,s.copyWithCount(own));var left=s.copyWithCount(s.getCount()-own);c.setItem(i,left.isEmpty()?ItemStack.EMPTY:left);taken+=own;
        }
        if(taken>0){data.setDirty();p.inventoryMenu.broadcastChanges();
            say(p,"Not past the hall: the House has put your own things back in the chest of drawers.");
            p.playNotifySound(SoundEvents.CHISELED_BOOKSHELF_INSERT,SoundSource.PLAYERS,.8F,.8F);}
    }
    /** Out into the world: the reader's own things go with them; what the House gave them is noted, so it comes back in with them. */
    static void leave(ServerPlayer p,HouseKeeping data,Keep keep){
        for(var c:pockets(p))for(int i=0;i<c.getContainerSize();i++){
            var s=c.getItem(i);if(allowed(s))continue;
            int own=take(keep.loose,s,s.getCount());if(s.getCount()>own)add(keep.carried,s.copyWithCount(s.getCount()-own));
        }
        while(keep.carried.size()>CARRIED_LIMIT)keep.carried.remove(0);
        data.setDirty();
        if(!keep.drawer.isEmpty())say(p,"Your own things are still in the chest of drawers in the Navidsons' front hall.");
    }

    // ------------------------------------------------------------------ the drawers
    @SubscribeEvent public static void use(PlayerInteractEvent.RightClickBlock e){
        if(e.getLevel().isClientSide()||!(e.getEntity() instanceof ServerPlayer p)||p.isSpectator())return;
        if(!p.level().dimension().equals(HouseDimensions.INTERIOR))return;
        var origin=HouseSavedData.get(p.server).houseOrigin();if(origin==null||!e.getPos().equals(origin.offset(DRAWERS)))return;
        var s=p.level().getBlockState(e.getPos());
        if(!s.is(HouseBlocks.HOUSEHOLD_FURNITURE.get())||s.getValue(HouseholdFurnitureBlock.KIND)!=HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS)return;
        e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);
        if(e.getHand()==InteractionHand.MAIN_HAND)open(p,e.getPos());
    }
    /** Opens the reader's own drawer: only their things, and only theirs to take. */
    public static void open(ServerPlayer p,BlockPos at){
        var view=new View(p,at);
        p.openMenu(new SimpleMenuProvider((id,inv,pl)->new Drawer(id,inv,view),Component.literal("Kept for you")));
        view.tell();
        p.playNotifySound(SoundEvents.CHISELED_BOOKSHELF_PICKUP,SoundSource.BLOCKS,.7F,.9F);
    }
    /**
     * The drawer's six rows (0.4.267), with three buttons beside them on the reader's screen: take everything back, and turn the
     * drawer's pages. The buttons arrive as vanilla menu-button clicks, so nothing but the drawer's own reader can press them.
     */
    public static final class Drawer extends ChestMenu {
        private final View view;
        Drawer(int id,Inventory inv,View view){super(MenuType.GENERIC_9x6,id,inv,view,6);this.view=view;}
        public View view(){return view;}
        @Override public boolean clickMenuButton(Player p,int button){
            // The button packet is handled only while the menu is still valid for its player (vanilla checks it first).
            if(!(p instanceof ServerPlayer reader)||reader!=view.reader)return false;
            switch(button){
                case RETRIEVE->retrieve(reader,view);
                case PREVIOUS->view.turn(-1);
                case NEXT->view.turn(1);
                default->{return false;}
            }
            broadcastChanges();return true;
        }
    }
    /**
     * Everything in the drawer back into the reader's pockets (0.4.267, the owner: "Add 'retrieve belongings', restore equipment to its
     * previous slots where possible"). Each thing goes back to the slot it was taken from if that slot is empty, or holds the same thing
     * with room (armour only to its own place); then to the inventory's free room; what still will not fit stays in the drawer. What
     * comes out is loose, as if taken by hand. True if the drawer is empty after.
     */
    public static boolean retrieve(ServerPlayer p,@Nullable View view){
        var data=get(p.server);var keep=data.keep(p.getUUID());var inv=p.getInventory();int moved=0;
        var left=new ArrayList<ItemStack>();
        for(var stack:keep.drawer){
            var s=stack.copy();
            for(var home:keep.homes){
                if(s.isEmpty())break;if(home.count<=0||!same(home.like,s)||!fits(p,home.slot,s))continue;
                var there=inv.getItem(home.slot);int room;
                if(there.isEmpty())room=s.getMaxStackSize();
                else if(ItemStack.isSameItemSameComponents(there,s))room=there.getMaxStackSize()-there.getCount();
                else continue;
                int n=Math.min(Math.min(room,home.count),s.getCount());if(n<=0)continue;
                if(there.isEmpty())inv.setItem(home.slot,s.copyWithCount(n));else there.grow(n);
                s.shrink(n);home.count-=n;moved+=n;if(!allowed(stack))add(keep.loose,stack.copyWithCount(n));
            }
            if(!s.isEmpty()){int before=s.getCount();inv.add(s);int n=before-s.getCount();if(n>0){moved+=n;if(!allowed(stack))add(keep.loose,stack.copyWithCount(n));}}
            if(!s.isEmpty())left.add(s);
        }
        keep.drawer.clear();keep.drawer.addAll(left);keep.homes.removeIf(h->h.count<=0);data.setDirty();
        p.inventoryMenu.broadcastChanges();if(view!=null)view.refresh();
        if(moved>0){
            say(p,left.isEmpty()?"Your things are back where you carried them.":"What would not fit is still in the drawer.");
            p.playNotifySound(SoundEvents.CHISELED_BOOKSHELF_PICKUP_ENCHANTED,SoundSource.BLOCKS,.7F,.9F);
        }else if(!left.isEmpty())say(p,"There is no room on you for any of it.");
        return left.isEmpty();
    }
    /** Whether a thing may go back into an inventory slot: anything to the pockets and the off hand, armour only to its own place. */
    static boolean fits(ServerPlayer p,int slot,ItemStack s){
        if(slot<0||slot>=p.getInventory().getContainerSize())return false;
        if(slot>=Inventory.INVENTORY_SIZE&&slot<Inventory.INVENTORY_SIZE+4){var to=p.getEquipmentSlotForItem(s);return to.getType()==EquipmentSlot.Type.HUMANOID_ARMOR&&to.getIndex()==slot-Inventory.INVENTORY_SIZE;}
        return true;
    }
    /**
     * A reader's drawer as a chest's six rows. Every change is written straight back: what leaves the drawer is noted as loose (if the
     * House would not have let it in), and what is put back is struck off the loose list. A drawer of more than six rows turns in pages
     * of six (0.4.267); the pages before and after the open one are kept as they are.
     */
    public static final class View extends SimpleContainer {
        private final ServerPlayer reader;private final BlockPos at;private final HouseKeeping data;private final Keep keep;
        private final List<ItemStack> before=new ArrayList<>(),after=new ArrayList<>();private List<ItemStack> last;private int page;
        View(ServerPlayer reader,BlockPos at){
            super(SHOWN);this.reader=reader;this.at=at;data=get(reader.server);keep=data.keep(reader.getUUID());
            refresh();
        }
        /** The open page (from 0) and how many there are. */
        public int page(){return page;}
        public int pages(){return Math.max(1,(keep.drawer.size()+SHOWN-1)/SHOWN);}
        /** Lays the open page out again from the drawer as it stands, without counting that as the reader's doing. */
        void refresh(){
            last=null;page=Math.max(0,Math.min(page,pages()-1));before.clear();after.clear();
            for(int i=0;i<SHOWN;i++)super.setItem(i,ItemStack.EMPTY);
            for(int i=0;i<keep.drawer.size();i++){var s=keep.drawer.get(i).copy();int k=i-page*SHOWN;if(k<0)before.add(s);else if(k<SHOWN)super.setItem(k,s);else after.add(s);}
            last=snapshot();tell();
        }
        void turn(int by){int next=Math.max(0,Math.min(page+by,pages()-1));if(next==page)return;page=next;refresh();reader.playNotifySound(SoundEvents.CHISELED_BOOKSHELF_PICKUP,SoundSource.BLOCKS,.4F,1.2F);}
        /** Tells the reader's screen which page is open, how many there are and how many stacks the drawer holds. */
        void tell(){if(reader.containerMenu instanceof Drawer d&&d.view==this)io.github.knaitoe.theoldesthouse.network.HousePackets.send(reader,new io.github.knaitoe.theoldesthouse.network.KeptStatePayload(d.containerId,page,pages(),keep.drawer.size()));}
        private List<ItemStack> snapshot(){var out=new ArrayList<ItemStack>();for(int i=0;i<getContainerSize();i++)if(!getItem(i).isEmpty())out.add(getItem(i).copy());return out;}
        @Override public void setChanged(){
            super.setChanged();if(last==null)return;var now=snapshot();
            for(var s:merge(last)){int after=count(now,s);if(after<s.getCount()&&!allowed(s))add(keep.loose,s.copyWithCount(s.getCount()-after));}
            for(var s:merge(now)){int before=count(last,s);if(s.getCount()>before)take(keep.loose,s,s.getCount()-before);}
            keep.drawer.clear();for(var s:before)keep.drawer.add(s.copy());for(var s:now)keep.drawer.add(s.copy());for(var s:after)keep.drawer.add(s.copy());
            last=now;data.setDirty();tell();
        }
        private static List<ItemStack> merge(List<ItemStack> list){var out=new ArrayList<ItemStack>();for(var s:list){boolean found=false;for(var o:out)if(same(o,s)){o.grow(s.getCount());found=true;break;}if(!found)out.add(s.copyWithCount(s.getCount()));}return out;}
        private static int count(List<ItemStack> list,ItemStack s){int n=0;for(var o:list)if(same(o,s))n+=o.getCount();return n;}
        @Override public boolean stillValid(Player p){return p==reader&&p.level().dimension().equals(HouseDimensions.INTERIOR)&&p.distanceToSqr(at.getCenter())<=64;}
        @Override public void stopOpen(Player p){p.level().playSound(null,at,SoundEvents.CHISELED_BOOKSHELF_INSERT,SoundSource.BLOCKS,.6F,.8F);}
    }

    // ------------------------------------------------------------------ things set down in the manor
    /** Own things dropped in the manor, by item entity, with whose they are: picked up past the hall, they go back to that drawer. */
    private static final Map<UUID,UUID> SET_DOWN=new HashMap<>();
    public static void clearAll(){SET_DOWN.clear();}
    @SubscribeEvent public static void tossed(ItemTossEvent e){
        if(!(e.getPlayer() instanceof ServerPlayer p)||p.level().isClientSide())return;
        var s=e.getEntity().getItem();if(allowed(s)||where(p)!=Where.MANOR)return;
        var keep=of(p);int own=take(keep.loose,s,s.getCount());if(own<=0)return;
        get(p.server).setDirty();SET_DOWN.put(e.getEntity().getUUID(),p.getUUID());
    }
    @SubscribeEvent public static void pickup(ItemEntityPickupEvent.Pre e){
        if(!(e.getPlayer() instanceof ServerPlayer p)||p.level().isClientSide())return;
        var item=e.getItemEntity();var owner=SET_DOWN.get(item.getUUID());if(owner==null)return;
        var data=get(p.server);
        if(where(p)==Where.BEYOND){
            e.setCanPickup(TriState.FALSE);add(data.keep(owner).drawer,item.getItem());item.discard();SET_DOWN.remove(item.getUUID());data.setDirty();
            say(p,"The House takes it back to the chest of drawers in the front hall.");return;
        }
        SET_DOWN.remove(item.getUUID());add(data.keep(p.getUUID()).loose,item.getItem());data.setDirty();
    }
    /** On the action bar, as the House's other lines are. */
    static void say(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}

    // ------------------------------------------------------------------ saving
    private static ListTag write(List<ItemStack> list,HolderLookup.Provider r){var out=new ListTag();for(var s:list)if(!s.isEmpty())out.add(s.save(r));return out;}
    private static void read(ListTag tag,List<ItemStack> into,HolderLookup.Provider r){for(int i=0;i<tag.size();i++){var s=ItemStack.parseOptional(r,tag.getCompound(i));if(!s.isEmpty())into.add(s);}}
    @Override public CompoundTag save(CompoundTag tag,HolderLookup.Provider r){
        var all=new CompoundTag();
        for(var en:keeps.entrySet()){var k=en.getValue();var t=new CompoundTag();
            t.put("Drawer",write(k.drawer,r));t.put("Loose",write(k.loose,r));t.put("Carried",write(k.carried,r));if(k.where!=null)t.putString("Where",k.where.name());
            var homes=new ListTag();for(var h:k.homes){if(h.count<=0)continue;var e=new CompoundTag();e.put("Item",h.like.save(r));e.putInt("Slot",h.slot);e.putInt("Count",h.count);homes.add(e);}t.put("Homes",homes);
            all.put(en.getKey().toString(),t);}
        tag.put("Readers",all);return tag;
    }
    public static HouseKeeping load(CompoundTag tag,HolderLookup.Provider r){
        var data=new HouseKeeping();var all=tag.getCompound("Readers");
        for(var key:all.getAllKeys()){UUID id;try{id=UUID.fromString(key);}catch(IllegalArgumentException ex){continue;}
            var t=all.getCompound(key);var k=data.keep(id);
            read(t.getList("Drawer",Tag.TAG_COMPOUND),k.drawer,r);read(t.getList("Loose",Tag.TAG_COMPOUND),k.loose,r);read(t.getList("Carried",Tag.TAG_COMPOUND),k.carried,r);
            if(t.contains("Where"))try{k.where=Where.valueOf(t.getString("Where"));}catch(IllegalArgumentException ex){k.where=null;}
            var homes=t.getList("Homes",Tag.TAG_COMPOUND);for(int i=0;i<homes.size();i++){var e=homes.getCompound(i);var like=ItemStack.parseOptional(r,e.getCompound("Item"));if(!like.isEmpty()&&e.getInt("Count")>0)k.homes.add(new Home(like,e.getInt("Slot"),e.getInt("Count")));}}
        return data;
    }
}
