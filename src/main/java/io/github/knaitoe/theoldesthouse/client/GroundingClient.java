package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.journal.Grounding;
import io.github.knaitoe.theoldesthouse.journal.JournalRegistry;
import io.github.knaitoe.theoldesthouse.network.GroundingPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * The static the worst rooms leave on a reader (journal/Grounding): a faint ringing, the view not quite steady, the edges of sight
 * a little dark. All of it follows the screen-effects setting, down to nothing at all. It eases toward what the server says is
 * left, and when writing settles it, it goes in three seconds.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class GroundingClient {
    private static final ResourceLocation VIGNETTE=ResourceLocation.withDefaultNamespace("textures/misc/vignette.png");
    private static float level,target;
    private static int settling;
    private static Ring ring;
    private GroundingClient(){}
    public static void accept(GroundingPayload p){
        if(p.settled()){settling=Grounding.SETTLE;target=0;return;}
        if(settling>0&&p.level()<=0)return;
        target=Mth.clamp(p.level(),0F,1F);if(target>level+.25F)level=target;
    }
    /** How strongly it shows now: what is left of it, times the screen-effects setting. */
    static float strength(){var mc=Minecraft.getInstance();return mc.options==null?0:Grounding.strength(level,mc.options.screenEffectScale().get().floatValue());}
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){level=target=0;settling=0;}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        var mc=Minecraft.getInstance();
        if(settling>0){level=Math.max(0F,level-level/settling);settling--;if(settling==0)level=0;}
        else level+=(target-level)*.05F;
        if(level<.002F&&target<=0)level=0;
        if(ring!=null&&(level<=0||mc.level!=ring.world)){mc.getSoundManager().stop(ring);ring=null;}
        if(ring==null&&strength()>0&&mc.level!=null&&mc.player!=null){ring=new Ring(mc.level);mc.getSoundManager().play(ring);}
    }
    /** The view is not quite steady: a slow, small drift, never a shake. */
    @SubscribeEvent public static void sway(ViewportEvent.ComputeCameraAngles e){
        float s=strength();if(s<=0)return;var mc=Minecraft.getInstance();if(mc.player==null)return;
        float t=mc.player.tickCount+(float)e.getPartialTick(),a=.45F*s;
        e.setYaw(e.getYaw()+(Mth.sin(t*.13F)+.4F*Mth.sin(t*.37F+1.1F))*a);
        e.setPitch(e.getPitch()+(Mth.sin(t*.17F+.6F)+.4F*Mth.sin(t*.29F))*a*.7F);
        e.setRoll(e.getRoll()+Mth.sin(t*.11F+2F)*a*.6F);
    }
    private static final class Ring extends AbstractTickableSoundInstance {
        final ClientLevel world;
        Ring(ClientLevel w){super(JournalRegistry.STATIC.get(),SoundSource.MASTER,RandomSource.create());world=w;looping=true;relative=true;attenuation=SoundInstance.Attenuation.NONE;volume=.01F;x=y=z=0;}
        @Override public void tick(){if(level<=0||Minecraft.getInstance().level!=world){stop();return;}volume=Math.max(.001F,.16F*strength());}
    }
    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Overlay {
        /** The edges of sight a little darker, the way vanilla darkens them at night, only more. */
        @SubscribeEvent public static void register(RegisterGuiLayersEvent event){
            event.registerBelowAll(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"grounding_static"),(g,delta)->{
                float s=strength();if(s<=0)return;int w=g.guiWidth(),h=g.guiHeight();float dark=.55F*s;
                RenderSystem.disableDepthTest();RenderSystem.depthMask(false);RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.ZERO,GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR);
                g.setColor(dark,dark,dark,1F);g.blit(VIGNETTE,0,0,-90,0F,0F,w,h,w,h);g.setColor(1F,1F,1F,1F);
                RenderSystem.defaultBlendFunc();RenderSystem.disableBlend();RenderSystem.depthMask(true);RenderSystem.enableDepthTest();
            });
        }
    }
}
