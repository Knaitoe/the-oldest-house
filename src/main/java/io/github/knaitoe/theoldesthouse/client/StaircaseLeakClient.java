package io.github.knaitoe.theoldesthouse.client;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.network.StaircaseLeakPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class StaircaseLeakClient {
    private static int lease,glow,glowMax;private StaircaseLeakClient(){}
    public static void accept(StaircaseLeakPayload p){lease=p.active()?100:0;glow=glowMax=Math.max(0,Math.min(200,p.returnGlow()));}
    public static boolean active(){return lease>0;}
    public static float returnLight(){return glowMax==0?0:(float)glow/glowMax;}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null||!mc.level.dimension().equals(HouseDimensions.INTERIOR)){lease=glow=glowMax=0;return;}if(lease>0)lease--;if(glow>0)glow--;}
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void color(ViewportEvent.ComputeFogColor e){if(active()){e.setRed(.12F);e.setGreen(.095F);e.setBlue(.065F);}else if(glow>0){float f=returnLight()*.055F;e.setRed(f);e.setGreen(f*.8F);e.setBlue(f*.6F);}}
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void fog(ViewportEvent.RenderFog e){if(active()){e.setNearPlaneDistance(8);e.setFarPlaneDistance(36);e.setCanceled(true);}}
}
