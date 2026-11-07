package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.house.TractHouseBlock;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.239, the familiar kitchen in use (docs/KITCHEN_AND_DOORS_0_4_239.md). The owner: "Make the kitchen useful and consistently calm.
 * Chairs should support sitting as well as dragging; the sink and appliances should have ordinary uses. Leaving supplies and
 * arranging furniture gives players reasons to return. Its changing window already provides enough mystery."
 *
 * <p>Everything here is ordinary and shared, and kept: the tap runs and fills a bottle; the stove cooks what can be cooked on it, a
 * little at a time, and nothing on it burns; the fridge and the four wall cupboards hold what is left in them for whoever comes next;
 * and whatever is set down on the table stays on it, for anyone to see and take. Nothing in the kitchen frightens anyone.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class KitchenFittings {
    private KitchenFittings(){}
    static final LabyrinthPlace KITCHEN=LabyrinthPlace.FAMILIAR_KITCHEN;
    /** The stove, the four wall cupboards over the counters, and where things set on the table lie. */
    public static final BlockPos STOVE=new BlockPos(-5,0,-11);
    public static final List<BlockPos> CUPBOARDS=List.of(new BlockPos(-5,3,-3),new BlockPos(-5,3,-4),new BlockPos(-5,3,-10),new BlockPos(-5,3,-11));
    public static final double[][] TABLE_SPOTS={{-.22,-.22},{.22,-.22},{-.22,.22},{.22,.22}};
    /** How long the stove takes, and how much it takes at once. */
    public static final int COOK=200,HOB=4;
    public static final String TABLE_TAG="the_oldest_house_kitchen_table",HOB_TAG="the_oldest_house_kitchen_hob";
    static final String STORE="kitchen_0239";

    static void say(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(net.minecraft.ChatFormatting.GRAY,net.minecraft.ChatFormatting.ITALIC),true);}
    static void give(ServerPlayer p,ItemStack s){if(s.isEmpty())return;if(!p.getInventory().add(s))p.drop(s,false);p.inventoryMenu.broadcastChanges();}
    @Nullable static BlockPos base(net.minecraft.server.MinecraftServer server){var o=HouseSavedData.get(server).houseOrigin();return o==null?null:LabyrinthPlaces.base(o,KITCHEN);}

    /** Which of the kitchen's things a block is, kitchen-relative; nothing if it is none of them. */
    public enum Fitting{SINK,STOVE,FRIDGE,CUPBOARD,TABLE,BOOK}
    @Nullable static Fitting fitting(ServerLevel l,BlockPos b,BlockPos at){
        var rel=at.subtract(b);var s=l.getBlockState(at);
        if(rel.equals(FamiliarRoom.SINK)&&TractHouseBlock.is(s,TractHouseBlock.Kind.COUNTER_SINK))return Fitting.SINK;
        if(rel.equals(STOVE)&&TractHouseBlock.is(s,TractHouseBlock.Kind.STOVE))return Fitting.STOVE;
        if((rel.equals(FamiliarRoom.FRIDGE)&&TractHouseBlock.is(s,TractHouseBlock.Kind.FRIDGE_LOWER))||(rel.equals(FamiliarRoom.FRIDGE.above())&&TractHouseBlock.is(s,TractHouseBlock.Kind.FRIDGE_UPPER)))return Fitting.FRIDGE;
        if(CUPBOARDS.contains(rel)&&TractHouseBlock.is(s,TractHouseBlock.Kind.WALL_CABINET))return Fitting.CUPBOARD;
        if(rel.equals(FamiliarRoom.TABLE)&&TraceBlock.is(s,TraceBlock.Kind.TABLE))return Fitting.TABLE;
        // 0.4.331: the counter by the door, where the kitchen keeps its book of what was eaten here.
        if(rel.equals(KitchenVisitors.BOOK)&&TractHouseBlock.is(s,TractHouseBlock.Kind.COUNTER))return Fitting.BOOK;
        return null;
    }

    @SubscribeEvent(priority=EventPriority.HIGH)
    public static void use(PlayerInteractEvent.RightClickBlock e){
        if(e.getLevel().isClientSide()||!(e.getEntity() instanceof ServerPlayer p)||!(e.getLevel() instanceof ServerLevel l))return;
        if(!l.dimension().equals(HouseDimensions.INTERIOR))return;
        var b=base(p.server);if(b==null)return;var which=fitting(l,b,e.getPos());if(which==null)return;
        e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);
        if(e.getHand()!=InteractionHand.MAIN_HAND||p.isSpectator())return;
        use(p,l,b,which,e.getPos(),e.getHitVec().getLocation());
    }
    /** A hand on one of the kitchen's things. Returns whether anything came of it. */
    public static boolean use(ServerPlayer p,ServerLevel l,BlockPos b,Fitting which,BlockPos at,Vec3 hit){
        return switch(which){
            case SINK->sink(p,l,at);
            case STOVE->stove(p,l,b);
            case FRIDGE->open(p,l,"fridge",27,b.offset(FamiliarRoom.FRIDGE),"Fridge");
            case CUPBOARD->open(p,l,"cupboard"+CUPBOARDS.indexOf(at.subtract(b)),9,at,"Cupboard");
            case TABLE->table(p,l,b,hit);
            case BOOK->KitchenVisitors.read(p);
        };
    }

    // ------------------------------------------------------------------ the sink
    /** The tap: a bottle filled, or the water run over the reader's hands (which puts out anything burning on them). */
    static boolean sink(ServerPlayer p,ServerLevel l,BlockPos at){
        var hand=p.getMainHandItem();var tap=Vec3.atCenterOf(at).add(0,.55,0);
        l.playSound(null,at,SoundEvents.WATER_AMBIENT,SoundSource.BLOCKS,.35F,1.6F);
        l.sendParticles(ParticleTypes.SPLASH,tap.x,tap.y,tap.z,10,.12,.02,.12,0);
        if(hand.is(Items.GLASS_BOTTLE)){
            hand.shrink(1);give(p,PotionContents.createItemStack(Items.POTION,Potions.WATER));
            l.playSound(null,at,SoundEvents.BOTTLE_FILL,SoundSource.BLOCKS,1F,1F);return true;
        }
        if(p.isOnFire())p.clearFire();
        say(p,"You run the tap over your hands. The water comes cold, and clear.");
        return true;
    }

    // ------------------------------------------------------------------ the stove
    static CompoundTag hob(net.minecraft.server.MinecraftServer server){return LabyrinthData.get(server).stateEntry(STORE,"stove");}
    static void hob(net.minecraft.server.MinecraftServer server,CompoundTag t){LabyrinthData.get(server).setStateEntry(STORE,"stove",t);}
    /** What the stove makes of this, a hob's worth; nothing if it is not something a stove cooks. */
    public static ItemStack cooked(ServerLevel l,ItemStack raw){
        if(raw.isEmpty())return ItemStack.EMPTY;
        return l.getRecipeManager().getRecipeFor(RecipeType.SMOKING,new SingleRecipeInput(raw),l).map(h->h.value().getResultItem(l.registryAccess()).copy()).orElse(ItemStack.EMPTY);
    }
    /** Whether something is on the stove, and whether it is done. */
    public static boolean cooking(net.minecraft.server.MinecraftServer server){return hob(server).contains("Done");}
    public static boolean done(ServerLevel l){var t=hob(l.getServer());return t.contains("Done")&&l.getGameTime()>=t.getLong("Ready");}
    /**
     * On the stove: whatever cooks, up to four at a time; off it when it is done. Nothing left on it alone burns.
     * <p>0.4.331: put anything else in with it (another kind of food, a bottle of water, milk, a seasoning) and it is a pan with a dish
     * in it ({@link KitchenCooking}). A pan comes off whenever a hand takes it, done or not; left on, it goes from underdone to cooked
     * through to well done, and then burns, and the ring clicks off.
     */
    static boolean stove(ServerPlayer p,ServerLevel l,BlockPos b){
        var t=hob(p.server);var top=Vec3.atBottomCenterOf(b.offset(STOVE)).add(0,1.01,0);var hand=p.getMainHandItem();
        if(t.contains("Pan"))return pan(p,l,b,t,hand);
        if(t.contains("Done")){
            if(!hand.isEmpty()&&KitchenCooking.ingredient(hand)!=null&&!ItemStack.isSameItemSameComponents(hand,ItemStack.parseOptional(l.registryAccess(),t.getCompound("Raw")))){
                // Something else goes in with it: the ring's four become the start of a pan, timed from when they went on.
                var raw=ItemStack.parseOptional(l.registryAccess(),t.getCompound("Raw"));var pan=new ListTag();
                for(int i=0;i<raw.getCount();i++)pan.add(raw.copyWithCount(1).save(l.registryAccess()));
                var started=new CompoundTag();started.put("Pan",pan);started.putLong("Lit",t.getLong("Ready")-COOK);
                return pan(p,l,b,started,hand);
            }
            if(l.getGameTime()<t.getLong("Ready")){say(p,"It isn't done yet.");return true;}
            give(p,ItemStack.parseOptional(l.registryAccess(),t.getCompound("Done")));
            clearHob(l,b);hob(p.server,new CompoundTag());
            l.playSound(null,b.offset(STOVE),SoundEvents.ITEM_PICKUP,SoundSource.PLAYERS,.4F,1F);say(p,"You take it off the stove.");return true;
        }
        var out=cooked(l,hand);
        if(out.isEmpty()){
            if(KitchenCooking.ingredient(hand)!=null)return pan(p,l,b,new CompoundTag(),hand);
            say(p,hand.isEmpty()?"The stove is cold. It would cook something, if you had something to cook.":"That isn't something to cook.");return true;
        }
        int n=Math.min(HOB,hand.getCount());var raw=hand.copyWithCount(n);hand.shrink(n);
        t=new CompoundTag();t.put("Raw",raw.save(l.registryAccess()));t.put("Done",out.copyWithCount(Math.min(out.getMaxStackSize(),out.getCount()*n)).save(l.registryAccess()));t.putLong("Ready",l.getGameTime()+COOK);
        hob(p.server,t);clearHob(l,b);lay(l,top,raw,0,HOB_TAG,.45F);
        l.playSound(null,b.offset(STOVE),SoundEvents.FIRECHARGE_USE,SoundSource.BLOCKS,.25F,1.8F);
        say(p,"You light the ring and put it on.");return true;
    }
    /** How many things a pan takes. */
    public static final int PAN=6;
    public static List<ItemStack> pan(ServerLevel l){var out=new ArrayList<ItemStack>();for(var tag:hob(l.getServer()).getList("Pan",net.minecraft.nbt.Tag.TAG_COMPOUND))out.add(ItemStack.parseOptional(l.registryAccess(),(CompoundTag)tag));return out;}
    /** How long the pan has been on the ring since the last thing that needed cooking went in, stopping when it burnt. */
    public static int panTicks(ServerLevel l){var t=hob(l.getServer());if(!t.contains("Pan"))return 0;long end=t.getBoolean("Off")?t.getLong("OffAt"):l.getGameTime();return (int)Math.max(0,end-t.getLong("Lit"));}
    static boolean pan(ServerPlayer p,ServerLevel l,BlockPos b,CompoundTag t,ItemStack hand){
        var at=b.offset(STOVE);var list=t.getList("Pan",net.minecraft.nbt.Tag.TAG_COMPOUND);
        if(hand.isEmpty()){
            var pan=pan(l);if(!t.contains("Pan"))pan=new ArrayList<>();
            if(!KitchenCooking.hasMain(pan)){for(var s:pan)give(p,s);say(p,"There was nothing in the pan to eat. You take the things back out.");}
            else{var dish=KitchenCooking.dish(pan,panTicks(l),p.getGameProfile().getName());give(p,KitchenCooking.stack(dish));say(p,"You take it off the stove: "+dish.strokes()+".");}
            clearHob(l,b);hob(p.server,new CompoundTag());
            l.playSound(null,at,SoundEvents.ITEM_PICKUP,SoundSource.PLAYERS,.4F,1F);return true;
        }
        var ing=KitchenCooking.ingredient(hand);
        if(ing==null){say(p,"That doesn't go in the pan.");return true;}
        if(t.getBoolean("Off")){say(p,"It's burnt. Take it off first.");return true;}
        if(list.size()>=PAN){say(p,"The pan is full.");return true;}
        boolean fresh=list.isEmpty();
        list.add(hand.copyWithCount(1).save(l.registryAccess()));t.put("Pan",list);
        // Raw food restarts the clock; a seasoning or water does not.
        if(fresh||ing.main()&&!t.contains("Lit"))t.putLong("Lit",l.getGameTime());else if(ing.main())t.putLong("Lit",Math.max(t.getLong("Lit"),l.getGameTime()-KitchenCooking.ready(KitchenCooking.Texture.CRISP)/4));
        var left=KitchenCooking.leftover(hand);hand.shrink(1);give(p,left);
        t.remove("Raw");t.remove("Done");t.remove("Ready");t.remove("Smelled");
        hob(p.server,t);showPan(l,b);
        l.playSound(null,at,fresh?SoundEvents.FIRECHARGE_USE:SoundEvents.GENERIC_SPLASH,SoundSource.BLOCKS,.25F,fresh?1.8F:1.6F);
        say(p,fresh?"You light the ring and put it in the pan.":"You put it in with the rest.");return true;
    }
    /** The pan on the ring, drawn as the dish it is becoming. */
    static void showPan(ServerLevel l,BlockPos b){
        var pan=pan(l);if(pan.isEmpty())return;var texture=KitchenCooking.texture(pan);
        var look=new ItemStack(KitchenRegistry.DISH.get());look.set(net.minecraft.core.component.DataComponents.CUSTOM_MODEL_DATA,new net.minecraft.world.item.component.CustomModelData(KitchenCooking.model(texture,KitchenCooking.cook(texture,panTicks(l)))));
        clearHob(l,b);lay(l,Vec3.atBottomCenterOf(b.offset(STOVE)).add(0,1.01,0),look,0,HOB_TAG,.5F);
    }
    static void clearHob(ServerLevel l,BlockPos b){for(var d:l.getEntitiesOfClass(Display.ItemDisplay.class,new AABB(b.offset(STOVE)).inflate(.5,1,.5),d->d.getTags().contains(HOB_TAG)))d.discard();}

    // ------------------------------------------------------------------ the fridge and the cupboards
    /** What is kept in the fridge and each cupboard: one set for everyone, saved with the House. */
    static final class Store extends SimpleContainer {
        final String key;final LabyrinthData data;final ServerLevel level;final BlockPos at;
        Store(String key,int size,LabyrinthData data,ServerLevel level,BlockPos at){
            super(size);this.key=key;this.data=data;this.level=level;this.at=at.immutable();
            // Each thing where it was put: the slots are kept, not packed to the front.
            var t=data.stateEntry(STORE,key);if(t.contains("Items"))net.minecraft.world.ContainerHelper.loadAllItems(t,getItems(),level.registryAccess());
            addListener(c->save());
        }
        void save(){var t=new CompoundTag();net.minecraft.world.ContainerHelper.saveAllItems(t,getItems(),true,level.registryAccess());data.setStateEntry(STORE,key,t);}
        @Override public boolean stillValid(Player p){return p.isAlive()&&p.level()==level&&p.distanceToSqr(Vec3.atCenterOf(at))<64;}
        @Override public void startOpen(Player p){level.playSound(null,at,key.equals("fridge")?SoundEvents.IRON_DOOR_OPEN:SoundEvents.WOODEN_TRAPDOOR_OPEN,SoundSource.BLOCKS,.5F,1.4F);}
        @Override public void stopOpen(Player p){level.playSound(null,at,key.equals("fridge")?SoundEvents.IRON_DOOR_CLOSE:SoundEvents.WOODEN_TRAPDOOR_CLOSE,SoundSource.BLOCKS,.5F,1.4F);save();}
    }
    private static final Map<LabyrinthData,Map<String,Store>> STORES=new WeakHashMap<>();
    /** The fridge's or a cupboard's things: the same set for everyone who opens it while the House is up. */
    public static SimpleContainer store(ServerLevel l,String key,int size,BlockPos at){
        var data=LabyrinthData.get(l.getServer());return STORES.computeIfAbsent(data,d->new HashMap<>()).computeIfAbsent(key,k->new Store(k,size,data,l,at));
    }
    static boolean open(ServerPlayer p,ServerLevel l,String key,int size,BlockPos at,String title){
        var store=store(l,key,size,at);int rows=size/9;var type=rows==3?MenuType.GENERIC_9x3:MenuType.GENERIC_9x1;
        p.openMenu(new SimpleMenuProvider((id,inv,who)->new ChestMenu(type,id,inv,store,rows),Component.literal(title)));
        return true;
    }

    // ------------------------------------------------------------------ the table
    /** Something set down on the table stays there, where anyone can see it, until someone takes it. */
    static boolean table(ServerPlayer p,ServerLevel l,BlockPos b,Vec3 hit){
        var t=b.offset(FamiliarRoom.TABLE);var hand=p.getMainHandItem();var lying=lying(l,t);
        if(hand.isEmpty()){
            if(lying.isEmpty()){say(p,"There's nothing on the table.");return true;}
            Display.ItemDisplay nearest=null;double best=Double.MAX_VALUE;
            for(var d:lying){double dd=d.position().distanceToSqr(hit);if(dd<best){best=dd;nearest=d;}}
            give(p,item(l,nearest));nearest.discard();
            l.playSound(null,t,SoundEvents.ITEM_PICKUP,SoundSource.PLAYERS,.4F,1F);return true;
        }
        for(int i=0;i<TABLE_SPOTS.length;i++){
            var spot=Vec3.atBottomCenterOf(t).add(TABLE_SPOTS[i][0],15/16.+.01,TABLE_SPOTS[i][1]);boolean taken=false;
            for(var d:lying)if(d.position().distanceToSqr(spot)<.01)taken=true;
            if(taken)continue;
            var put=hand.copy();hand.setCount(0);lay(l,spot,put,p.getYRot(),TABLE_TAG,.5F);
            l.playSound(null,t,SoundEvents.WOOD_PLACE,SoundSource.BLOCKS,.4F,1.4F);return true;
        }
        say(p,"There's no room left on the table.");return true;
    }
    public static List<Display.ItemDisplay> lying(ServerLevel l,BlockPos table){return l.getEntitiesOfClass(Display.ItemDisplay.class,new AABB(table).inflate(.1,.6,.1),d->d.getTags().contains(TABLE_TAG));}
    /** The thing an item display shows, whole. */
    public static ItemStack item(ServerLevel l,Display.ItemDisplay d){var tag=d.saveWithoutId(new CompoundTag());return tag.contains("item")?ItemStack.parseOptional(l.registryAccess(),tag.getCompound("item")):ItemStack.EMPTY;}
    /** A thing lying flat on a surface, kept with the world. */
    static Display.ItemDisplay lay(ServerLevel l,Vec3 at,ItemStack item,float yaw,String tag,float scale){
        var d=EntityType.ITEM_DISPLAY.create(l);if(d==null)return null;
        var t=d.saveWithoutId(new CompoundTag());t.put("item",item.save(l.registryAccess()));t.putString("item_display","fixed");
        var tr=new CompoundTag();tr.put("scale",floats(scale,scale,scale));tr.put("translation",floats(0,0,0));
        tr.put("left_rotation",floats(-.70710677F,0,0,.70710677F));tr.put("right_rotation",floats(0,0,0,1));t.put("transformation",tr);d.load(t);
        d.moveTo(at.x,at.y,at.z,yaw,0);d.addTag(tag);l.addFreshEntity(d);return d;
    }
    private static ListTag floats(float... v){var list=new ListTag();for(float f:v)list.add(FloatTag.valueOf(f));return list;}

    // ------------------------------------------------------------------ the clock
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();if(server.getTickCount()%10!=0||!FamiliarRoom.occupied())return;
        var l=server.getLevel(HouseDimensions.INTERIOR);var b=base(server);if(l==null||b==null||!l.isLoaded(b.offset(STOVE)))return;
        var t=hob(server);var at=b.offset(STOVE);
        if(t.contains("Pan")){tickPan(server,l,b,t);return;}
        if(!t.contains("Done"))return;
        if(l.getGameTime()<t.getLong("Ready")){
            // It cooks: a little smoke and the sound of it.
            l.sendParticles(ParticleTypes.SMOKE,at.getX()+.5,at.getY()+1.1,at.getZ()+.5,1,.12,.02,.12,.005);
            if(server.getTickCount()%40==0)l.playSound(null,at,SoundEvents.CAMPFIRE_CRACKLE,SoundSource.BLOCKS,.35F,1.2F);
            return;
        }
        // Done: what lies on the ring is what it became, and the ring goes off by itself. Nothing on it burns.
        if(!t.getBoolean("Off")){
            clearHob(l,b);lay(l,Vec3.atBottomCenterOf(at).add(0,1.01,0),ItemStack.parseOptional(l.registryAccess(),t.getCompound("Done")),0,HOB_TAG,.45F);
            t.putBoolean("Off",true);hob(server,t);
            for(var p:l.players())if(FamiliarRoom.inKitchen(p,HouseSavedData.get(server).houseOrigin()))say(p,"Whatever is on the stove is done; the ring clicks off.");
        }
    }
    /** A pan on the ring: it cooks, it smells done, it starts to catch, and then it burns and the ring clicks off. */
    static void tickPan(net.minecraft.server.MinecraftServer server,ServerLevel l,BlockPos b,CompoundTag t){
        if(t.getBoolean("Off"))return;var at=b.offset(STOVE);var pan=pan(l);var texture=KitchenCooking.texture(pan);int ticks=panTicks(l);
        l.sendParticles(ticks>KitchenCooking.ready(texture)*1.5?ParticleTypes.LARGE_SMOKE:ParticleTypes.SMOKE,at.getX()+.5,at.getY()+1.1,at.getZ()+.5,1,.12,.02,.12,.005);
        if(server.getTickCount()%40==0)l.playSound(null,at,texture==KitchenCooking.Texture.CRISP?SoundEvents.CAMPFIRE_CRACKLE:SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT,SoundSource.BLOCKS,.35F,1.2F);
        int stage=ticks>=KitchenCooking.burnsAt(texture)?3:ticks>=KitchenCooking.ready(texture)*1.5?2:ticks>=KitchenCooking.ready(texture)?1:0;
        if(stage>t.getInt("Smelled")){
            t.putInt("Smelled",stage);
            String line=switch(stage){case 1->"Whatever is in the pan smells done.";case 2->"The pan is starting to catch.";default->"The pan has burnt; the ring clicks off.";};
            if(stage==3){t.putBoolean("Off",true);t.putLong("OffAt",l.getGameTime());}
            hob(server,t);showPan(l,b);
            for(var p:l.players())if(FamiliarRoom.inKitchen(p,HouseSavedData.get(server).houseOrigin()))say(p,line);
        }
    }
    public static void clearAll(){STORES.clear();}
}
