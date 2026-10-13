package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

/**
 * The lake house's lent gear: a flashlight, the gold diving helmet on the compressor's hose, and air tanks. The vignette
 * lends each reader their own, marked with their name; it works for nobody else and goes back the moment its reader is
 * outside the scene. It cannot be thrown away or dropped on death, and it never leaves the lake house.
 * <p>The reader's own breathing aids are not gear: inside the scene nothing but this gear gives air under water
 * ({@link LakeHouseDive}).
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class LakeHouseGear {
    public static final String LENT="LakeHouseLent";
    private LakeHouseGear(){}

    /** Lent gear in general: it keeps only while its own reader is in the lake house. */
    public static class Lent extends Item {
        public Lent(Properties properties){super(properties);}
        @Override public void inventoryTick(ItemStack stack,Level level,Entity entity,int slot,boolean selected){
            if(!level.isClientSide&&entity instanceof ServerPlayer p&&!keeps(p,stack))stack.setCount(0);
        }
    }
    /** Lent gear that is worn: the helmet on the head, the tanks on the back. */
    public static final class Worn extends Lent implements Equipable {
        private final EquipmentSlot slot;
        public Worn(EquipmentSlot slot,Properties properties){super(properties);this.slot=slot;}
        @Override public EquipmentSlot getEquipmentSlot(){return slot;}
        @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){return swapWithEquipmentSlot(this,level,player,hand);}
    }

    public static ItemStack lend(ServerPlayer p,Item item){
        var s=new ItemStack(item);var t=new CompoundTag();t.putUUID(LENT,p.getUUID());s.set(DataComponents.CUSTOM_DATA,CustomData.of(t));return s;
    }
    public static @Nullable UUID lentTo(ItemStack s){
        var d=s.get(DataComponents.CUSTOM_DATA);if(d==null)return null;var t=d.copyTag();return t.hasUUID(LENT)?t.getUUID(LENT):null;
    }
    public static boolean lent(ItemStack s){return lentTo(s)!=null;}
    /** This reader's own lent piece of this kind. */
    public static boolean mine(ItemStack s,Player p,Item item){return s.is(item)&&p.getUUID().equals(lentTo(s));}
    public static boolean has(Player p,Item item){return p.getInventory().hasAnyMatching(s->mine(s,p,item));}
    /** Lends a piece unless the reader already has theirs. True when something was handed over. */
    public static boolean give(ServerPlayer p,Item item){if(has(p,item))return false;LiteraryVignettes.give(p,lend(p,item));return true;}
    static boolean keeps(ServerPlayer p,ItemStack s){return p.getUUID().equals(lentTo(s))&&p.isAlive()&&LakeHouse.inside(p)&&!LakeHouse.empty(p);}
    /** Takes back every lent piece the reader is carrying or wearing. */
    public static void reclaim(ServerPlayer p){
        var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(lent(inv.getItem(i)))inv.setItem(i,ItemStack.EMPTY);
        if(lent(p.containerMenu.getCarried()))p.containerMenu.setCarried(ItemStack.EMPTY);
    }

    /** Thrown away, it stays in the reader's hands: it is not theirs to leave here. */
    @SubscribeEvent public static void toss(ItemTossEvent e){
        var s=e.getEntity().getItem();if(!lent(s))return;
        e.setCanceled(true);
        if(e.getPlayer() instanceof ServerPlayer p&&keeps(p,s)){p.getInventory().add(s.copy());p.displayClientMessage(Component.literal("It's only lent. It goes back when you leave."),true);}
    }
    /** Nothing lent falls where a death happens. */
    @SubscribeEvent public static void drops(LivingDropsEvent e){e.getDrops().removeIf(i->lent(i.getItem()));}
    /** Loose lent pieces lying in the scene go back when nobody they belong to is there. */
    static void tidy(net.minecraft.server.level.ServerLevel l,net.minecraft.world.phys.AABB box){
        for(var item:l.getEntitiesOfClass(ItemEntity.class,box,i->lent(i.getItem()))){
            var id=lentTo(item.getItem());var reader=id==null?null:l.getServer().getPlayerList().getPlayer(id);
            if(reader==null||!LakeHouse.inside(reader))item.discard();
        }
    }
}
