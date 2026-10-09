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
    private static int phase,lease,demand,remaining,flags;
    private GoatmanClient(){}
    public static void accept(GoatmanScenePayload p){phase=p.phase();lease=60;demand=p.demand();remaining=p.remaining();flags=p.flags();}
    /** The vigil sets its own night and dawn fog colour, which the interior's black must not cover. */
    public static boolean authorsFog(){return active();}
    private static boolean active(){var mc=Minecraft.getInstance();return lease>0&&phase>0&&mc.player!=null&&mc.player.isAlive()&&mc.level!=null&&mc.level.dimension().equals(HouseDimensions.INTERIOR);}
    private static boolean quiet(){return (flags&GoatmanScenePayload.QUIET)!=0;}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        if(lease>0)lease--;if(remaining>0)remaining--;if(!active()){phase=0;demand=0;flags=0;return;}
        // Once the woods go quiet the air fills with copper motes, thickest at dusk.
        var mc=Minecraft.getInstance();if(mc.isPaused()||!quiet()&&phase!=GoatmanVignette.VIGIL)return;
        var p=mc.player;int count=phase==GoatmanVignette.VIGIL?1:3;
        for(int i=0;i<count;i++){double x=p.getX()+(p.getRandom().nextDouble()-.5)*16,y=p.getY()+p.getRandom().nextDouble()*3,z=p.getZ()+(p.getRandom().nextDouble()-.5)*16;
            mc.level.addParticle(GoatmanRegistry.COPPER.get(),x,y,z,(p.getRandom().nextDouble()-.5)*.01,.003,(p.getRandom().nextDouble()-.5)*.01);}
    }
    @SubscribeEvent public static void fog(ViewportEvent.ComputeFogColor e){
        if(!active())return;boolean night=phase==GoatmanVignette.VIGIL;
        if(night){e.setRed(.03F);e.setGreen(.03F);e.setBlue(.05F);return;}
        if(phase==GoatmanVignette.DAWN){e.setRed(.28F);e.setGreen(.16F);e.setBlue(.19F);return;}
        // The quiet turns the dusk a little rusty.
        e.setRed(quiet()?.22F:.18F);e.setGreen(quiet()?.13F:.16F);e.setBlue(quiet()?.11F:.19F);
    }
    static String line(int demand){
        if(demand==-99)return "Let me—";
        if(demand<0)return GoatmanVignette.RUNNER_LINES[Math.min(GoatmanVignette.RUNNER_LINES.length,-demand)-1];
        return GoatmanVignette.MIMIC_LINES[Math.min(GoatmanVignette.MIMIC_LINES.length,demand)-1];
    }
    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Registration {
        @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){
            e.registerEntityRenderer(GoatmanRegistry.CHILD.get(),GoatmanChildRenderer::new);e.registerEntityRenderer(GoatmanRegistry.FIGURE.get(),GoatmanFigureRenderer::new);
        }
        @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e){e.registerLayerDefinition(GoatmanFigureModel.LAYER,GoatmanFigureModel::createBodyLayer);}
        @SubscribeEvent public static void overlays(RegisterGuiLayersEvent e){
            e.registerAboveAll(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"trailer_door"),(g,delta)->{
                // Only words actually said at the door: the cousin outside at dusk, or what knocks at night. Never before.
                if(!active()||remaining<=0||demand==0)return;
                if(demand>0&&phase!=GoatmanVignette.VIGIL||demand<0&&phase!=GoatmanVignette.GATHERING)return;
                var font=Minecraft.getInstance().font;int y=g.guiHeight()-68;
                g.drawCenteredString(font,"At the door",g.guiWidth()/2,y,0xFFB1AAA1);
                g.drawCenteredString(font,"“"+line(demand)+"”",g.guiWidth()/2,y+13,0xFFECE5DC);
            });
        }
    }
}
