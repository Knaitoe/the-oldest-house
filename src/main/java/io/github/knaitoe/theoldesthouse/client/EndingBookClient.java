package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.EndingBookPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class EndingBookClient {
    private static Object world;private static BlockPos at;private static boolean visible;private static int lease;
    public static void accept(EndingBookPayload p) { world=Minecraft.getInstance().level;at=p.at();visible=p.visible();lease=30; }
    public static boolean visible(BlockPos pos) { return lease>0&&visible&&pos.equals(at)&&Minecraft.getInstance().level==world; }
    @SubscribeEvent public static void tick(ClientTickEvent.Post e) { if(lease>0)lease--;if(Minecraft.getInstance().level!=world)lease=0; }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e) { lease=0;at=null;world=null; }
}
