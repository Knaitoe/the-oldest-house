package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.CompanionNearbyPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * 0.4.269, the companion key (the owner: "Currently, opening the wheel requires an empty hand or the compass shortcut. A rebindable
 * nearby-companion key would remove that inventory shuffle."). G by default, under Controls; pressed in the world, whatever is in the
 * hands, it asks the server to open the wheel for the reader's own cat or dog close by. The server picks the animal and checks it.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class CompanionKeyClient {
    private CompanionKeyClient(){}
    public static final String CATEGORY="key.categories."+TheOldestHouse.MOD_ID;
    public static final KeyMapping COMPANION=new KeyMapping("key."+TheOldestHouse.MOD_ID+".companion",KeyConflictContext.IN_GAME,InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_G,CATEGORY);

    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        var mc=Minecraft.getInstance();
        while(COMPANION.consumeClick())if(mc.screen==null&&mc.player!=null&&mc.getConnection()!=null&&!mc.player.isSpectator())PacketDistributor.sendToServer(new CompanionNearbyPayload());
    }

    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Keys {
        private Keys(){}
        @SubscribeEvent public static void register(RegisterKeyMappingsEvent e){e.register(COMPANION);}
    }
}
