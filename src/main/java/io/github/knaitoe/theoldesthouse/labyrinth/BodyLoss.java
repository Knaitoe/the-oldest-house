package io.github.knaitoe.theoldesthouse.labyrinth;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.network.BodyLossPayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingSwapItemsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * What the visitors at the cabin were given: whole hearts of a reader's life and the arm of the off hand. The loss is
 * the reader's own, saved with the world, and permanent: it survives death, every door and every login. Nothing in
 * the House gives it back.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class BodyLoss {
    public static final String STATE="cabin_body_0451";
    public static final ResourceLocation HEARTS=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"cabin_hearts");
    public static final int MAX_HEARTS=2;
    /** Rare: a phantom line at most once in this many ticks of the reader's own play. */
    public static final int PHANTOM_GAP=20*60*30;
    // Each side keeps its own view: the server's from the saved record, a client's from the payload.
    private static final Map<UUID,HumanoidArm> SERVER=new ConcurrentHashMap<>(),CLIENT=new ConcurrentHashMap<>();
    private BodyLoss(){}

    public static CompoundTag record(MinecraftServer s,UUID id){return LabyrinthData.get(s).stateEntry(STATE,id.toString());}
    private static void save(MinecraftServer s,UUID id,CompoundTag t){LabyrinthData.get(s).setStateEntry(STATE,id.toString(),t);}
    public static int hearts(MinecraftServer s,UUID id){return Math.min(MAX_HEARTS,record(s,id).getInt("Hearts"));}
    public static @Nullable HumanoidArm armTaken(MinecraftServer s,UUID id){var side=record(s,id).getString("Arm");return side.equals("LEFT")?HumanoidArm.LEFT:side.equals("RIGHT")?HumanoidArm.RIGHT:null;}
    /** The arm this player no longer has, as this side of the connection knows it. */
    public static @Nullable HumanoidArm missing(Player p){return (p.level().isClientSide()?CLIENT:SERVER).get(p.getUUID());}
    public static boolean oneArmed(Player p){return missing(p)!=null;}
    public static void clientSet(UUID id,int arm){if(arm==1)CLIENT.put(id,HumanoidArm.LEFT);else if(arm==2)CLIENT.put(id,HumanoidArm.RIGHT);else CLIENT.remove(id);}
    public static void clientClear(){CLIENT.clear();}

    /** One heart of a reader's life, permanently. */
    public static void takeHeart(ServerPlayer p){
        var t=record(p.server,p.getUUID());t.putInt("Hearts",Math.min(MAX_HEARTS,t.getInt("Hearts")+1));t.putLong("HeartTaken"+t.getInt("Hearts"),p.server.overworld().getGameTime());
        save(p.server,p.getUUID(),t);apply(p);
    }
    /**
     * The arm of the off hand, permanently. The off hand is emptied into the pack (or at the reader's feet), never
     * destroyed. The arm is hidden from every viewer only when {@link #reveal} sends it, so a scene can choose its moment.
     */
    public static boolean takeArm(ServerPlayer p){
        var t=record(p.server,p.getUUID());if(t.contains("Arm"))return false;
        var side=p.getMainArm().getOpposite();t.putString("Arm",side.name());t.putLong("ArmTaken",p.server.overworld().getGameTime());
        save(p.server,p.getUUID(),t);SERVER.put(p.getUUID(),side);seal(p,false);return true;
    }
    public static void reveal(ServerPlayer p){var side=SERVER.get(p.getUUID());HousePackets.sendToAll(p.server,new BodyLossPayload(p.getUUID(),code(side)));}
    private static int code(@Nullable HumanoidArm side){return side==HumanoidArm.LEFT?1:side==HumanoidArm.RIGHT?2:0;}

    /** Idempotent: the heart modifier matches the saved record and health never exceeds what is left. */
    public static void apply(ServerPlayer p){
        var health=p.getAttribute(Attributes.MAX_HEALTH);if(health==null)return;
        int hearts=hearts(p.server,p.getUUID());double want=-2D*hearts;var current=health.getModifier(HEARTS);
        if(hearts==0){if(current!=null)health.removeModifier(HEARTS);}
        else if(current==null||current.amount()!=want){health.removeModifier(HEARTS);health.addPermanentModifier(new AttributeModifier(HEARTS,want,AttributeModifier.Operation.ADD_VALUE));}
        if(p.getHealth()>p.getMaxHealth())p.setHealth(p.getMaxHealth());
    }
    /** Nothing stays in a hand that is not there; the item goes back into the pack, or to the reader's feet. */
    private static void seal(ServerPlayer p,boolean tell){
        var held=p.getOffhandItem();if(held.isEmpty())return;var moved=held.copy();p.setItemSlot(EquipmentSlot.OFFHAND,ItemStack.EMPTY);
        if(!p.getInventory().add(moved)&&!moved.isEmpty())p.drop(moved,false);p.inventoryMenu.broadcastChanges();
        if(tell)p.displayClientMessage(Component.literal("You have no hand to hold that with. It is back in your pack."),true);
    }

    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;var side=armTaken(p.server,p.getUUID());if(side!=null)SERVER.put(p.getUUID(),side);else SERVER.remove(p.getUUID());apply(p);
        for(var other:p.server.getPlayerList().getPlayers()){var theirs=SERVER.get(other.getUUID());if(theirs!=null&&other!=p)HousePackets.send(p,new BodyLossPayload(other.getUUID(),code(theirs)));}
        if(side!=null)reveal(p);
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){SERVER.remove(e.getEntity().getUUID());}
    @SubscribeEvent public static void clone(PlayerEvent.Clone e){if(e.getEntity() instanceof ServerPlayer p)apply(p);}
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e){if(e.getEntity() instanceof ServerPlayer p){apply(p);if(SERVER.containsKey(p.getUUID()))reveal(p);}}
    @SubscribeEvent public static void travelled(PlayerEvent.PlayerChangedDimensionEvent e){if(e.getEntity() instanceof ServerPlayer p)apply(p);}
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){SERVER.clear();}
    @SubscribeEvent public static void swap(LivingSwapItemsEvent.Hands e){
        if(e.getEntity() instanceof ServerPlayer p&&oneArmed(p)){e.setCanceled(true);p.displayClientMessage(Component.literal("There is no other hand to pass it to."),true);}
    }
    @SubscribeEvent public static void tick(PlayerTickEvent.Post e){
        if(!(e.getEntity() instanceof ServerPlayer p)||p.isSpectator())return;
        if(oneArmed(p))seal(p,true);
        if(p.tickCount%20==0&&(oneArmed(p)||hearts(p.server,p.getUUID())>0))phantom(p);
    }

    /** Occasional, rare and drawn from what the reader is actually doing; never more often than {@link #PHANTOM_GAP}. */
    private static void phantom(ServerPlayer p){
        int played=p.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME));var t=record(p.server,p.getUUID());
        if(!t.contains("PhantomPlayed")){t.putInt("PhantomPlayed",played);save(p.server,p.getUUID(),t);return;}
        if(played-t.getInt("PhantomPlayed")<PHANTOM_GAP||p.getRandom().nextInt(900)!=0)return;
        var line=phantomLine(p,missing(p),p.getRandom().nextInt(4));if(line==null)return;
        t.putInt("PhantomPlayed",played);t.putInt("PhantomLines",t.getInt("PhantomLines")+1);save(p.server,p.getUUID(),t);
        p.sendSystemMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC));
    }
    public static @Nullable String phantomLine(ServerPlayer p,@Nullable HumanoidArm arm,int pick){
        var level=p.serverLevel();
        if(arm!=null){
            String side=arm==HumanoidArm.LEFT?"left":"right";
            if(p.isUnderWater())return "The water closes around a hand you left at the cabin.";
            if(level.isRainingAt(p.blockPosition().above()))return "Rain on your "+side+" knuckles. You look down. There is nothing there for it to land on.";
            if(p.onClimbable())return "You reach for the next rung with the "+side+" hand. It doesn't come.";
            if(p.getMainHandItem().getItem() instanceof ShieldItem)return "Your shield arm aches. You hold the shield in the only hand you have.";
            if(p.getHealth()<=6)return "Your missing hand closes into a fist.";
            if(HouseDimensions.isHouseDimension(level.dimension()))return "Somewhere in the House, a shelf is holding your "+side+" hand.";
            if(level.dimension().equals(Level.OVERWORLD)&&!level.isDay())return "The stump itches where Sabrina tied the cord.";
            return switch(pick){case 0->"Your "+side+" fingers curl. You can feel each one.";case 1->"You go to scratch your "+side+" wrist.";case 2->"For a moment you are sure you are holding something in your "+side+" hand.";default->"The arm aches in the weather. It is not there to ache.";};
        }
        if(p.getHealth()<=6)return "Your heart stumbles and catches. It has done that since the cabin.";
        return pick%2==0?"You count your heartbeat. It comes up short.":"Your heart skips. It doesn't hurt, exactly.";
    }
}
