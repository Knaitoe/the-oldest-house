package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Native passengers do not auto-duck for a ceiling. Match the real body, eyes and seated reach. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class CanoeRiderPose {
    private static final Map<Player,Pose> PREVIOUS=new WeakHashMap<>();
    private CanoeRiderPose(){}
    public static synchronized void apply(Player player,boolean low){
        if(low&&player.isAlive()&&!player.isSpectator()){
            if(!PREVIOUS.containsKey(player))PREVIOUS.put(player,player.getForcedPose());
            player.setSwimming(false);player.setForcedPose(Pose.CROUCHING);player.setPose(Pose.CROUCHING);player.refreshDimensions();
        }else if(PREVIOUS.containsKey(player)){
            var old=PREVIOUS.remove(player);player.setForcedPose(old);player.setPose(old==null?Pose.STANDING:old);player.refreshDimensions();
        }
    }
    @SubscribeEvent public static void tick(PlayerTickEvent.Post event){
        var player=event.getEntity();apply(player,player.getVehicle() instanceof LakeHouseCanoe canoe&&canoe.underLowRoof());
    }
}
