package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.CaverVignette;
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
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Pose;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

/**
 * The crawling body in Ted the Caver's crawls: native pose agreement with the server, the cave's draught on the body, and
 * (0.4.73) what bad air feels like. 0.4.75: the reader's own breath gauge in place of Minecraft's bubbles, and when the air is
 * gone a few seconds of choking before the blackout: strangled, wheezing breaths, a pounding heart, the view shaking and the
 * dark closing in from the edges. Everything here is the local reader's own; nothing is broadcast.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class CaverCrawlClient {
    private static LocalPlayer player;
    private static Pose previousForced,previousDisplayed;
    private static int lease,x,y,z,beat,choke;
    /** The cave's draught along the crawl, applied each tick to the crawling body's own motion; and the reader's own air. */
    private static float pushX,pushZ,air=1;
    private static Breath breath;
    private static Wheeze wheeze;
    private static final ResourceLocation VIGNETTE=ResourceLocation.withDefaultNamespace("textures/misc/vignette.png");
    /** The breath gauge: lungs and a bar where the air bubbles would be (frames laid out by tools/generate_caver_assets.py). */
    private static final ResourceLocation GAUGE=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/gui/caver_breath.png");
    private CaverCrawlClient(){}
    public static void accept(CaverCrawlPayload p){
        if(!p.active()){clear();return;}var current=Minecraft.getInstance().player;
        if(current==null)return;if(player!=current){clear();player=current;previousForced=player.getForcedPose();previousDisplayed=player.getPose();player.setForcedPose(Pose.SWIMMING);player.setPose(Pose.SWIMMING);player.refreshDimensions();}
        lease=80;x=p.x();y=p.y();z=p.z();pushX=p.pushX();pushZ=p.pushZ();air=Mth.clamp(p.air(),0F,1F);choke=Math.max(0,p.choke());
    }
    /** True while the local reader is crawling in the cave. */
    public static boolean active(){return player!=null&&player==Minecraft.getInstance().player;}
    /** 0 with full air, 1 with none; only while crawling. */
    public static float strain(){return active()?1-air:0;}
    /** 0 while the reader can breathe, rising to 1 as the choking runs its course; 1 through the blackout. */
    public static float choking(){return active()&&choke>0?Math.min(1F,choke/(float)CaverVignette.CHOKE):0;}
    private static boolean blackout(){return active()&&choke>CaverVignette.CHOKE;}
    private static boolean wheezing(){return active()&&choke>0&&choke<=CaverVignette.CHOKE;}
    /** How dark the world has gone: closing in while choking, black through the blackout, lifting as the reader comes to. */
    static float darkness(){
        if(!active()||choke<=0)return 0;
        if(choke<=CaverVignette.CHOKE){float c=choke/(float)CaverVignette.CHOKE;return c*c;}
        int out=choke-CaverVignette.CHOKE,waking=CaverVignette.BLACKOUT-CaverVignette.WAKE;
        return out<=waking?1F:Math.max(0F,1-(out-waking)/(float)CaverVignette.WAKE);
    }
    private static void clear(){
        if(player!=null&&player.getForcedPose()==Pose.SWIMMING){player.setForcedPose(previousForced);player.setPose(previousForced==null?previousDisplayed:previousForced);player.refreshDimensions();}
        player=null;previousForced=null;previousDisplayed=null;lease=0;pushX=pushZ=0;beat=0;air=1;choke=0;
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){clear();}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        var mc=Minecraft.getInstance();
        if(breath!=null&&(!active()||mc.level!=breath.world)){mc.getSoundManager().stop(breath);breath=null;}
        if(wheeze!=null&&(!wheezing()||mc.level!=wheeze.world)){mc.getSoundManager().stop(wheeze);wheeze=null;}
        if(player==null)return;lease--;
        double rx=player.getX()-x,ry=player.getY()-y,rz=player.getZ()-z;
        if(lease<=0||mc.player!=player||!player.isAlive()||player.isSpectator()||mc.level==null||!mc.level.dimension().equals(HouseDimensions.INTERIOR)
                ||rx< -7.5||rx>8.5||ry< -3.5||ry> -1.8||rz> -20.5||rz< -58){clear();return;}
        // Air moving through the crawl: it eases one way and drags the other, and the line holds against it.
        if(pushX!=0||pushZ!=0)player.setDeltaMovement(player.getDeltaMovement().add(pushX,0,pushZ));
        // Thin air: a strained breath under everything, and the heart once it is nearly gone.
        float strain=strain();
        if(breath==null&&strain>.15F){breath=new Breath(mc.level,player);mc.getSoundManager().play(breath);}
        // No air: the breath is strangled into wheezing, and the heart hammers faster the longer it lasts.
        if(wheeze==null&&wheezing()){wheeze=new Wheeze(mc.level,player);mc.getSoundManager().play(wheeze);}
        if(wheezing()){float c=choking();if(++beat>=Math.round(11-6*c)){beat=0;mc.getSoundManager().play(SimpleSoundInstance.forUI(LabyrinthRegistry.FLOORBOARD_HEARTBEAT.get(),.95F+c*.25F,.7F+c*.3F));}}
        else if(!blackout()&&strain>.6F&&++beat>=Math.round(22-strain*10)){beat=0;mc.getSoundManager().play(SimpleSoundInstance.forUI(LabyrinthRegistry.FLOORBOARD_HEARTBEAT.get(),.9F+strain*.2F,.35F+strain*.35F));}
    }
    /** The walls come in as the air goes, and further while choking. */
    @SubscribeEvent public static void fov(ViewportEvent.ComputeFov e){if(active())e.setFOV(e.getFOV()*(.88-.16*strain()-.14*choking()));}
    /** Choking shakes the view, harder as it goes on; the accessibility setting for screen effects scales it, down to none. */
    @SubscribeEvent public static void shake(ViewportEvent.ComputeCameraAngles e){
        if(!wheezing())return;var mc=Minecraft.getInstance();float scale=mc.options.screenEffectScale().get().floatValue();if(scale<=0)return;
        float c=choking(),t=player.tickCount+(float)e.getPartialTick(),a=(.35F+2.4F*c*c)*scale;
        e.setYaw(e.getYaw()+(Mth.sin(t*1.9F)+.5F*Mth.sin(t*4.7F+1.3F))*a*.5F);
        e.setPitch(e.getPitch()+(Mth.sin(t*2.6F+.7F)+.5F*Mth.sin(t*5.9F))*a*.45F);
        e.setRoll(e.getRoll()+Mth.sin(t*3.3F+2.1F)*a*.8F);
    }
    private static final class Breath extends AbstractTickableSoundInstance {
        final ClientLevel world;final LocalPlayer reader;
        Breath(ClientLevel w,LocalPlayer p){super(NovelRegistry.WELL_BREATH.get(),SoundSource.PLAYERS,RandomSource.create());world=w;reader=p;looping=true;relative=true;attenuation=Attenuation.NONE;volume=.05F;x=y=z=0;}
        @Override public boolean canPlaySound(){return reader.isAlive();}
        @Override public void tick(){if(!active()||Minecraft.getInstance().player!=reader||Minecraft.getInstance().level!=world){stop();return;}
            float strain=strain();volume=strain<.1F||choke>0?0:.06F+strain*.34F;pitch=1+strain*.25F;}
    }
    /** Strangled, wheezing breaths that will not go in, louder and tighter as the choking goes on. */
    private static final class Wheeze extends AbstractTickableSoundInstance {
        final ClientLevel world;final LocalPlayer reader;
        Wheeze(ClientLevel w,LocalPlayer p){super(LabyrinthRegistry.CAVER_CHOKE.get(),SoundSource.PLAYERS,RandomSource.create());world=w;reader=p;looping=true;relative=true;attenuation=Attenuation.NONE;volume=.45F;x=y=z=0;}
        @Override public boolean canPlaySound(){return reader.isAlive();}
        @Override public void tick(){if(!wheezing()||Minecraft.getInstance().player!=reader||Minecraft.getInstance().level!=world){stop();return;}
            float c=choking();volume=.45F+c*.55F;pitch=1.05F-c*.12F;}
    }
    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Overlay {
        @SubscribeEvent public static void register(RegisterGuiLayersEvent event){
            // The edges of sight darken with the air, the way vanilla darkens them, only more. Choking closes them in: the dark
            // window narrows, black beyond it, and goes black at the end. The HUD stays readable above it.
            event.registerBelowAll(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"caver_air"),(g,delta)->{
                if(!active())return;float dark=darkness(),s=dark>0?1F:.25F+strain()*.75F;int w=g.guiWidth(),h=g.guiHeight();
                int ix=Math.round(w*.4F*dark),iy=Math.round(h*.4F*dark);
                if(ix>0||iy>0){g.fill(0,0,w,iy,0xFF000000);g.fill(0,h-iy,w,h,0xFF000000);g.fill(0,iy,ix,h-iy,0xFF000000);g.fill(w-ix,iy,w,h-iy,0xFF000000);}
                RenderSystem.disableDepthTest();RenderSystem.depthMask(false);RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.ZERO,GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR);
                g.setColor(s,s,s,1F);g.blit(VIGNETTE,ix,iy,-90,0F,0F,w-2*ix,h-2*iy,w-2*ix,h-2*iy);g.setColor(1F,1F,1F,1F);
                RenderSystem.defaultBlendFunc();RenderSystem.disableBlend();RenderSystem.depthMask(true);RenderSystem.enableDepthTest();
                if(dark>0){float pulse=.5F+.5F*Mth.sin((player.tickCount+delta.getGameTimeDeltaPartialTick(false))*.9F);
                    int alpha=Math.round(255*Math.min(1F,dark*dark*(.9F+.1F*pulse)));if(alpha>0)g.fill(0,0,w,h,alpha<<24);}
            });
            // The reader's own breath gauge, where Minecraft's air bubbles would be: a bar that drains and the lungs beside it,
            // shaking and flashing red while they choke. Hidden with the HUD, and for anyone the air cannot touch.
            event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"caver_breath"),(g,delta)->{
                var mc=Minecraft.getInstance();if(!active()||mc.options.hideGui||mc.gameMode==null||!mc.gameMode.canHurtPlayer())return;
                int w=g.guiWidth(),h=g.guiHeight(),right=w/2+91,top=h-49,t=player.tickCount,jx=0,jy=0;boolean choking=choke>0;
                if(wheezing()){var shake=RandomSource.create(t*31L);jx=shake.nextInt(3)-1;jy=shake.nextInt(3)-1;}
                RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();
                int barX=right-83+jx,barY=top+1+jy,fill=blackout()?0:Math.round(70*air);
                g.blit(GAUGE,barX,barY,0F,9F,72,7,128,32);
                if(fill>0)g.blit(GAUGE,barX+1,barY+1,0F,16F,fill,5,128,32);
                if(choking&&(t/4)%2==0)g.blit(GAUGE,barX+1,barY+1,0F,21F,70,5,128,32);
                g.blit(GAUGE,right-9+jx,top+jy,(choking?2:air<.4F?1:0)*9F,0F,9,9,128,32);
                RenderSystem.disableBlend();
            });
        }
    }
}
