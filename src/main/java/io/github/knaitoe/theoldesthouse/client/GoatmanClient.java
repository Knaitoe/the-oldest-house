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
    private static int phase,lease,demand,remaining,flags,cousinLine,cousinRemaining;
    private GoatmanClient(){}
    public static void accept(GoatmanScenePayload p){phase=p.phase();lease=60;demand=p.demand();remaining=p.remaining();flags=p.flags();cousinLine=p.cousinLine();cousinRemaining=p.cousinRemaining();}
    /** The vigil sets its own night and dawn fog colour, which the interior's black must not cover. */
    public static boolean authorsFog(){return active();}
    private static boolean active(){var mc=Minecraft.getInstance();return lease>0&&phase>0&&mc.player!=null&&mc.player.isAlive()&&mc.level!=null&&mc.level.dimension().equals(HouseDimensions.INTERIOR);}
    public static int priorityDemand(){return active()&&remaining>0?demand:0;}
    public static boolean cousinSubtitle(){return active()&&demand==0&&cousinRemaining>0&&cousinLine>0;}
    private static boolean quiet(){return (flags&GoatmanScenePayload.QUIET)!=0;}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        if(lease>0)lease--;if(remaining>0)remaining--;if(cousinRemaining>0)cousinRemaining--;if(!active()){phase=0;demand=0;flags=0;cousinLine=0;cousinRemaining=0;return;}
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
        if(demand>=101)return GoatmanVignette.MIMIC_LINES[Math.min(GoatmanVignette.MIMIC_LINES.length-1,demand-101)];
        return GoatmanVignette.LEGACY_MIMIC_LINES[Math.min(GoatmanVignette.LEGACY_MIMIC_LINES.length,demand)-1];
    }
    /** Shared by the actual GUI layer and native GPU proof. The outside voice owns the center. */
    public static void renderDialogue(net.minecraft.client.gui.GuiGraphics g,net.minecraft.client.gui.Font font,int phase,int demand,int remaining,int child,int childRemaining,int tick){
        boolean voice=remaining>0&&demand!=0&&(demand>0?phase==GoatmanVignette.VIGIL:phase==GoatmanVignette.GATHERING);
        if(voice){
            var rows=font.split(net.minecraft.network.chat.Component.literal("“"+line(demand)+"”"),Math.max(80,Math.min(420,g.guiWidth()-48)));
            int center=g.guiWidth()/2,y=g.guiHeight()/2-25,span=rows.size()*13+25;
            g.fill(16,y-7,g.guiWidth()-16,y+span,0x66000000);
            g.drawCenteredString(font,"At the door",center,y,0xFFB1AAA1);
            int shake=demand>=104&&tick%7==0?(tick/7%2==0?1:-1):0;
            for(int i=0;i<rows.size();i++){
                if(demand>=104)g.drawCenteredString(font,rows.get(i),center-shake+1,y+15+i*13,0x66B99880);
                g.drawCenteredString(font,rows.get(i),center+shake,y+14+i*13,0xFFF2EADF);
            }
            return;
        }
        if(phase!=GoatmanVignette.VIGIL&&phase!=GoatmanVignette.GATHERING||childRemaining<=0||child<=0)return;
        var rows=font.split(net.minecraft.network.chat.Component.literal(GoatmanFear.line(child)),Math.max(80,Math.min(440,g.guiWidth()-40)));
        // Keep the reaction below the central voice and clear of lingering camp chat.
        int y=Math.min(g.guiHeight()/2+40,g.guiHeight()-40-rows.size()*11);g.fill(16,y-4,g.guiWidth()-16,y+rows.size()*11+3,0xEE000000);
        for(int i=0;i<rows.size();i++)g.drawCenteredString(font,rows.get(i),g.guiWidth()/2,y+i*11,0xFFC6BFB4);
    }
    public static boolean night(){return phase==GoatmanVignette.VIGIL;}
    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Registration {
        @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){
            e.registerEntityRenderer(GoatmanRegistry.CHILD.get(),GoatmanChildRenderer::new);e.registerEntityRenderer(GoatmanRegistry.FIGURE.get(),GoatmanFigureRenderer::new);
            e.registerBlockEntityRenderer(GoatmanRegistry.DOOR_ENTITY.get(),TrailerDoorRenderer::new);
        }
        @SubscribeEvent public static void setup(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent e){e.enqueueWork(()->{
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(GoatmanRegistry.WINDOW.get(),net.minecraft.client.renderer.RenderType.cutout());
        });}
        @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e){e.registerLayerDefinition(GoatmanFigureModel.LAYER,GoatmanFigureModel::createBodyLayer);}
        @SubscribeEvent public static void overlays(RegisterGuiLayersEvent e){
            e.registerAboveAll(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"trailer_door"),(g,delta)->{
                if(!active())return;
                var mc=Minecraft.getInstance();renderDialogue(g,mc.font,phase,demand,remaining,cousinLine,cousinRemaining,mc.player.tickCount);

            });
        }
    }
}
