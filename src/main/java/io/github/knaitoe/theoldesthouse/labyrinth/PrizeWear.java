package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.network.PrizeWearPayload;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/**
 * 0.4.411: the prizes a reader wears. A slap bracelet on either wrist, a mood ring on either hand, each on the arm of the hand that
 * held it. They are saved with the world and stay on through death, doors and logins, and every viewer draws them. Crouching and using
 * an empty hand takes one off into that hand: the ring first, then a bracelet, the main arm's before the other's.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class PrizeWear {
    public static final String STATE="prize_wear_0411";
    /** One bit for each place: a bracelet on the right or left wrist, a ring on the right or left hand. */
    public static final int RIGHT_WRIST=1,LEFT_WRIST=2,RIGHT_RING=4,LEFT_RING=8;
    private static final Map<UUID,Integer> SERVER=new ConcurrentHashMap<>(),CLIENT=new ConcurrentHashMap<>();
    private PrizeWear(){}

    public static int bit(HumanoidArm arm,PolybiusPrize.Kind kind){
        boolean right=arm==HumanoidArm.RIGHT;
        return kind==PolybiusPrize.Kind.MOOD_RING?(right?RIGHT_RING:LEFT_RING):(right?RIGHT_WRIST:LEFT_WRIST);
    }
    /** What this player wears, as this side of the connection knows it. */
    public static int worn(Player p){
        if(p.level().isClientSide())return CLIENT.getOrDefault(p.getUUID(),0);
        return p instanceof ServerPlayer sp?saved(sp.server,sp.getUUID()):0;
    }
    public static boolean free(Player p,HumanoidArm arm,PolybiusPrize.Kind kind){return (worn(p)&bit(arm,kind))==0;}
    public static void clientSet(UUID id,int worn){if(worn==0)CLIENT.remove(id);else CLIENT.put(id,worn);}
    public static void clientClear(){CLIENT.clear();}

    static int saved(MinecraftServer s,UUID id){return SERVER.computeIfAbsent(id,k->LabyrinthData.get(s).stateEntry(STATE,k.toString()).getInt("Worn"));}
    private static void save(ServerPlayer p,int worn){
        var t=new CompoundTag();t.putInt("Worn",worn);LabyrinthData.get(p.server).setStateEntry(STATE,p.getUUID().toString(),t);
        SERVER.put(p.getUUID(),worn);reveal(p);
    }
    public static void reveal(ServerPlayer p){HousePackets.sendToAll(p.server,new PrizeWearPayload(p.getUUID(),saved(p.server,p.getUUID())));}

    /** The prize goes on the arm; false if that place is already taken. */
    public static boolean putOn(ServerPlayer p,HumanoidArm arm,PolybiusPrize.Kind kind){
        int worn=saved(p.server,p.getUUID()),bit=bit(arm,kind);
        if((worn&bit)!=0)return false;
        save(p,worn|bit);return true;
    }

    /** A crouching reader with an empty main hand takes one prize off into it. Returns whether anything came off. */
    public static boolean takeOff(ServerPlayer p){
        if(!p.isCrouching()||!p.getMainHandItem().isEmpty()||p.isSpectator())return false;
        int worn=saved(p.server,p.getUUID());if(worn==0)return false;
        boolean right=p.getMainArm()==HumanoidArm.RIGHT;
        int[] order=right?new int[]{RIGHT_RING,LEFT_RING,RIGHT_WRIST,LEFT_WRIST}:new int[]{LEFT_RING,RIGHT_RING,LEFT_WRIST,RIGHT_WRIST};
        for(int bit:order){
            if((worn&bit)==0)continue;
            boolean ring=bit==RIGHT_RING||bit==LEFT_RING;
            var kind=ring?PolybiusPrize.Kind.MOOD_RING:PolybiusPrize.Kind.SLAP_BRACELET;
            p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(PolybiusRegistry.PRIZES.get(kind.ordinal()).get()));
            save(p,worn&~bit);
            p.level().playSound(null,p.getX(),p.getY()+1,p.getZ(),ring?SoundEvents.ARMOR_EQUIP_GOLD.value():PolybiusRegistry.SLAP.get(),SoundSource.PLAYERS,.5F,ring?1.3F:.7F);
            return true;
        }
        return false;
    }

    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;
        SERVER.remove(p.getUUID());
        for(var other:p.server.getPlayerList().getPlayers())if(other!=p){int w=saved(p.server,other.getUUID());if(w!=0)HousePackets.send(p,new PrizeWearPayload(other.getUUID(),w));}
        if(saved(p.server,p.getUUID())!=0)reveal(p);
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){SERVER.remove(e.getEntity().getUUID());}
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e){if(e.getEntity() instanceof ServerPlayer p&&saved(p.server,p.getUUID())!=0)reveal(p);}
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){SERVER.clear();}
}
