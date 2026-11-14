package io.github.knaitoe.theoldesthouse.client;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.HazardRegistry;
import io.github.knaitoe.theoldesthouse.network.HazardViewPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
/**
 * The appended hazards' private views: the listing hall's tilt and push, and how far behind its reader the mirror hall's glass
 * shows them. 0.4.592: the glass and the reader in it are {@link MirrorHallGlass}, a true mirror on the reader's own client; the
 * figure this class used to draw behind panes of glass is gone. Nothing reflected is spawned or tracked by another client.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class HazardClient {
    private static HazardViewPayload state;private static ClientLevel level;private static LocalPlayer reader;private static int lease;
    private HazardClient(){}
    public static void accept(HazardViewPayload payload){if(payload.mode()==0){clear();return;}var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null)return;state=payload;level=mc.level;reader=mc.player;lease=16;}
    private static boolean active(){var mc=Minecraft.getInstance();return lease>0&&state!=null&&mc.level==level&&mc.player==reader&&reader!=null&&reader.isAlive()&&!reader.isSpectator()&&level.dimension().equals(HouseDimensions.INTERIOR);}
    private static void clear(){state=null;reader=null;level=null;lease=0;}
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){clear();}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){if(lease>0)lease--;boolean on=active();if(!on)clear();MirrorHallGlass.tick(on&&state.mode()==1?state:null);if(on&&state.mode()==2)reader.setDeltaMovement(reader.getDeltaMovement().add(state.view().getFloat("Push"),0,0));}
    @SubscribeEvent public static void camera(ViewportEvent.ComputeCameraAngles e){if(active()&&state.mode()==2)e.setRoll(e.getRoll()+state.view().getFloat("Tilt")*7*Comfort.effects());}
    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Layers {
        @SubscribeEvent public static void setup(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent e){e.enqueueWork(()->ItemBlockRenderTypes.setRenderLayer(HazardRegistry.PIECE.get(),RenderType.translucent()));}
    }
}
