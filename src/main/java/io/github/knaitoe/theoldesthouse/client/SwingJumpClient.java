package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.SeatEntity;
import io.github.knaitoe.theoldesthouse.network.SwingJumpPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 0.4.423: pressing jump while sitting on a swing lets go of it; the server decides the launch from the seat's own speed.
 * 0.4.468: pressing jump on an end of the park's seesaw pushes off the sand, by the same message; the server decides whether that
 * end is down.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class SwingJumpClient {
    private SwingJumpClient(){}
    private static boolean held;
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        var mc=Minecraft.getInstance();var p=mc.player;
        if(p==null||mc.screen!=null){held=false;return;}
        boolean jumping=p.input!=null&&p.input.jumping;
        if(jumping&&!held&&(p.getVehicle() instanceof SeatEntity seat&&seat.swinging()||p.getVehicle() instanceof io.github.knaitoe.theoldesthouse.labyrinth.SeesawSeat))PacketDistributor.sendToServer(SwingJumpPayload.INSTANCE);
        held=jumping;
    }
}
