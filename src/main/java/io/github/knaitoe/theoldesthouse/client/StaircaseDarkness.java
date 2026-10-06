package io.github.knaitoe.theoldesthouse.client;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.FinaleArchitecture;
import io.github.knaitoe.theoldesthouse.network.StaircaseLightPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class StaircaseDarkness {
    private static int lease,fires;private static float sight;
    private StaircaseDarkness(){}
    public static void accept(StaircaseLightPayload p){lease=p.active()?30:0;fires=p.fires();sight=Math.max(.8F,p.sight());}
    private static boolean active(){var mc=Minecraft.getInstance();return lease>0&&mc.level!=null&&mc.player!=null&&mc.player.isAlive()&&!mc.player.isSpectator()&&!mc.player.isCreative()
            &&mc.level.dimension().equals(HouseDimensions.INTERIOR)&&HouseSightlineState.origin()!=null&&FinaleArchitecture.contains(HouseSightlineState.origin(),mc.player.blockPosition());}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){if(lease>0)lease--;if(!active())lease=0;}
    @SubscribeEvent public static void color(ViewportEvent.ComputeFogColor e){if(active()&&fires<5){e.setRed(0);e.setGreen(0);e.setBlue(0);}}
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void fog(ViewportEvent.RenderFog e){if(active()&&fires<5&&e.getMode()==net.minecraft.client.renderer.FogRenderer.FogMode.FOG_TERRAIN){e.setNearPlaneDistance(.15F);e.setFarPlaneDistance(sight);e.setCanceled(true);}}
}
