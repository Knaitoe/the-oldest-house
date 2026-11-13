package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.PellCutPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

/**
 * 0.4.534, the Pell house's knife (labyrinth/PellKnife; PellCutPayload): a sharp cut to black over everything, held; then static over
 * the reader's own view, thick at first and clearing, while the black lifts from under it. The black is the story's: it always shows.
 * The static follows the screen-effects setting (Comfort), down to none. The reader's breathing is sent by the server as sounds.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class PellCutClient {
    private PellCutClient(){}
    private static int black,clear,clearFor;
    public static void accept(PellCutPayload p){black=Math.max(0,p.black());clear=clearFor=Math.max(0,p.clear());}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){if(black>0)black--;else if(clear>0)clear--;}
    @SubscribeEvent public static void out(ClientPlayerNetworkEvent.LoggingOut e){black=clear=0;}
    /** Whether the cut is on this screen now. */
    public static boolean active(){return black>0||clear>0;}

    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Overlay {
        @SubscribeEvent public static void register(RegisterGuiLayersEvent event){
            event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"pell_cut"),(g,delta)->{
                int w=g.guiWidth(),h=g.guiHeight();
                if(black>0){g.fill(0,0,w,h,0xFF000000);return;}
                if(clear<=0||clearFor<=0)return;
                // How much is left to clear, 1 to 0; the black lifts over the first quarter of it.
                float left=clear/(float)clearFor;
                int under=(int)(255*Mth.clamp((left-.75F)/.25F,0F,1F));
                if(under>0)g.fill(0,0,w,h,under<<24);
                float strength=left*Comfort.effects();if(strength<=0)return;
                RenderSystem.enableBlend();
                var r=RandomSource.create(net.minecraft.Util.getMillis()/40);
                int cell=3;
                for(int y=0;y<h;y+=cell){
                    boolean band=r.nextInt(16)==0;int shift=band?r.nextInt(9)-4:0;
                    for(int x=0;x<w;x+=cell){
                        if(r.nextFloat()>strength)continue;
                        int v=50+r.nextInt(206);int a=(int)(Mth.clamp(strength*(band?1.1F:.9F),0F,1F)*255);
                        g.fill(x+shift,y,x+shift+cell,y+cell,(a<<24)|(v<<16)|(v<<8)|v);
                    }
                }
                RenderSystem.disableBlend();
            });
        }
    }
}
