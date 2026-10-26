package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.BackseatSeat;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Only the actual driver uses the first-person cabin view; restore their prior camera afterward. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class BackseatClient {
    private static CameraType previous;
    private BackseatClient(){}
    private static void restore(){if(previous!=null){Minecraft.getInstance().options.setCameraType(previous);previous=null;}}
    @SubscribeEvent public static void tick(ClientTickEvent.Post ignored){
        var mc=Minecraft.getInstance();
        if(mc.player==null||!mc.player.isAlive()||!(mc.player.getVehicle() instanceof BackseatSeat seat)||seat.owner().filter(mc.player.getUUID()::equals).isEmpty()){restore();return;}
        if(previous==null)previous=mc.options.getCameraType();mc.options.setCameraType(CameraType.FIRST_PERSON);
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut ignored){restore();}
}
