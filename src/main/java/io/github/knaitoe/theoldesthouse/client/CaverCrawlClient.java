package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthRegistry;
import io.github.knaitoe.theoldesthouse.labyrinth.NovelRegistry;
import io.github.knaitoe.theoldesthouse.network.CaverCrawlPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Pose;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

/**
 * The crawling body in Ted the Caver's crawls: native pose agreement with the server, the cave's draught on the body, and
 * (0.4.73) what bad air feels like. Everything here is the local reader's own; nothing is broadcast.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class CaverCrawlClient {
    private static LocalPlayer player;
    private static Pose previousForced,previousDisplayed;
    private static int lease,x,y,z,beat;
    /** The cave's draught along the crawl, applied each tick to the crawling body's own motion. */
    private static float pushX,pushZ;
    private static Breath breath;
    private static final ResourceLocation VIGNETTE=ResourceLocation.withDefaultNamespace("textures/misc/vignette.png");
    private CaverCrawlClient(){}
    public static void accept(CaverCrawlPayload p){
        if(!p.active()){clear();return;}var current=Minecraft.getInstance().player;
        if(current==null)return;if(player!=current){clear();player=current;previousForced=player.getForcedPose();previousDisplayed=player.getPose();player.setForcedPose(Pose.SWIMMING);player.setPose(Pose.SWIMMING);player.refreshDimensions();}
        lease=80;x=p.x();y=p.y();z=p.z();pushX=p.pushX();pushZ=p.pushZ();
    }
    /** True while the local reader is crawling in the cave. */
    public static boolean active(){return player!=null&&player==Minecraft.getInstance().player;}
    /** 0 with full air, 1 with none; only while crawling. */
    public static float strain(){if(!active())return 0;int max=player.getMaxAirSupply();return max<=0?0:Math.max(0,Math.min(1,1-player.getAirSupply()/(float)max));}
    private static void clear(){
        if(player!=null&&player.getForcedPose()==Pose.SWIMMING){player.setForcedPose(previousForced);player.setPose(previousForced==null?previousDisplayed:previousForced);player.refreshDimensions();}
        player=null;previousForced=null;previousDisplayed=null;lease=0;pushX=pushZ=0;beat=0;
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){clear();}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        var mc=Minecraft.getInstance();
        if(breath!=null&&(!active()||mc.level!=breath.world)){mc.getSoundManager().stop(breath);breath=null;}
        if(player==null)return;lease--;
        double rx=player.getX()-x,ry=player.getY()-y,rz=player.getZ()-z;
        if(lease<=0||mc.player!=player||!player.isAlive()||player.isSpectator()||mc.level==null||!mc.level.dimension().equals(HouseDimensions.INTERIOR)
                ||rx< -7.5||rx>8.5||ry< -3.5||ry> -1.8||rz> -20.5||rz< -58){clear();return;}
        // Air moving through the crawl: it eases one way and drags the other, and the line holds against it.
        if(pushX!=0||pushZ!=0)player.setDeltaMovement(player.getDeltaMovement().add(pushX,0,pushZ));
        // Thin air: a strained breath under everything, and the heart once it is nearly gone.
        float strain=strain();
        if(breath==null&&strain>.15F){breath=new Breath(mc.level,player);mc.getSoundManager().play(breath);}
        if(strain>.6F&&++beat>=Math.round(22-strain*10)){beat=0;mc.getSoundManager().play(SimpleSoundInstance.forUI(LabyrinthRegistry.FLOORBOARD_HEARTBEAT.get(),.9F+strain*.2F,.35F+strain*.35F));}
    }
    /** The walls come in as the air goes. */
    @SubscribeEvent public static void fov(ViewportEvent.ComputeFov e){if(active())e.setFOV(e.getFOV()*(.88-.16*strain()));}
    private static final class Breath extends AbstractTickableSoundInstance {
        final ClientLevel world;final LocalPlayer reader;
        Breath(ClientLevel w,LocalPlayer p){super(NovelRegistry.WELL_BREATH.get(),SoundSource.PLAYERS,RandomSource.create());world=w;reader=p;looping=true;relative=true;attenuation=Attenuation.NONE;volume=.05F;x=y=z=0;}
        @Override public boolean canPlaySound(){return reader.isAlive();}
        @Override public void tick(){if(!active()||Minecraft.getInstance().player!=reader||Minecraft.getInstance().level!=world){stop();return;}
            float strain=strain();volume=strain<.1F?0:.06F+strain*.34F;pitch=1+strain*.25F;}
    }
    /** The edges of sight darken with the air, the way vanilla darkens them, only more. */
    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Overlay {
        @SubscribeEvent public static void register(RegisterGuiLayersEvent event){
            event.registerBelowAll(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"caver_air"),(g,delta)->{
                float s=active()?.25F+strain()*.75F:0;if(s<=0)return;int w=g.guiWidth(),h=g.guiHeight();
                RenderSystem.disableDepthTest();RenderSystem.depthMask(false);RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.ZERO,GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR);
                g.setColor(s,s,s,1F);g.blit(VIGNETTE,0,0,-90,0F,0F,w,h,w,h);g.setColor(1F,1F,1F,1F);
                RenderSystem.defaultBlendFunc();RenderSystem.disableBlend();RenderSystem.depthMask(true);RenderSystem.enableDepthTest();
            });
        }
    }
}
