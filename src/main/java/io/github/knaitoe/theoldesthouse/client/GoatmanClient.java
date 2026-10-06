package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import io.github.knaitoe.theoldesthouse.network.GoatmanScenePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class GoatmanClient {
    private static int phase,lease,demand,remaining;
    private GoatmanClient(){}
    public static void accept(GoatmanScenePayload p){phase=p.phase();lease=60;demand=p.demand();remaining=p.remaining();}
    /** The vigil sets its own night and dawn fog colour, which the interior's black must not cover. */
    public static boolean authorsFog(){return active();}
    private static boolean active(){var mc=Minecraft.getInstance();return lease>0&&phase>0&&mc.player!=null&&mc.player.isAlive()&&mc.level!=null&&mc.level.dimension().equals(HouseDimensions.INTERIOR);}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){if(lease>0)lease--;if(remaining>0)remaining--;if(!active()){phase=0;demand=0;}}
    @SubscribeEvent public static void fog(ViewportEvent.ComputeFogColor e){
        if(!active())return;boolean night=phase==GoatmanVignette.VIGIL;
        e.setRed(night?.025F:phase==GoatmanVignette.DAWN?.28F:.18F);e.setGreen(night?.035F:.16F);e.setBlue(night?.055F:.19F);
    }
    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Registration {
        @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(GoatmanRegistry.CHILD.get(),GoatmanChildRenderer::new);}
        @SubscribeEvent public static void overlays(RegisterGuiLayersEvent e){
            e.registerAboveAll(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"trailer_door"),(g,delta)->{
                if(!active()||remaining<=0||demand<1||demand>GoatmanVignette.DEMANDS.length)return;
                var font=Minecraft.getInstance().font;int y=g.guiHeight()-68;
                g.drawCenteredString(font,"At the trailer door",g.guiWidth()/2,y,0xFFB1AAA1);
                g.drawCenteredString(font,"\u201c"+GoatmanVignette.DEMANDS[demand-1]+"\u201d",g.guiWidth()/2,y+13,0xFFECE5DC);
            });
        }
    }
}
