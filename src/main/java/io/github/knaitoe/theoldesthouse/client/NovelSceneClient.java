package io.github.knaitoe.theoldesthouse.client;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.network.NovelScenePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
/** Gentle dust shake scales to the user's screen effects setting; no camera lock. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class NovelSceneClient {
    private static int mode,lease,elapsed,captionTicks,clockTicks;private static float shake;private static String caption="";private static ClientLevel clockLevel;private static long previousTime;
    private NovelSceneClient(){}
    public static void accept(NovelScenePayload p){mode=p.mode();lease=60;elapsed=p.elapsed();caption=p.caption();captionTicks=p.captionTicks();shake=Math.max(0,Math.min(1,p.shake()));}
    private static boolean active(){var mc=Minecraft.getInstance();return lease>0&&mode>0&&mc.player!=null&&mc.player.isAlive()&&!mc.player.isSpectator()&&mc.level!=null&&HouseDimensions.isHouseDimension(mc.level.dimension());}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){if(lease>0)lease--;if(captionTicks>0)captionTicks--;elapsed++;var mc=Minecraft.getInstance();
        if(active()&&(mode==1||mode==3||mode==4||mode==5)){
            if(clockLevel!=mc.level){restoreClock();clockLevel=mc.level;previousTime=clockLevel.getDayTime();clockTicks=0;}clockTicks++;
            clockLevel.setDayTime(mode==1?21000:mode==3?18000:mode==4?6000:18000+Math.min(6000,elapsed*6000L/3600));
        }else restoreClock();if(!active()){mode=0;shake=0;caption="";}}
    private static void restoreClock(){if(clockLevel!=null){clockLevel.setDayTime(previousTime+clockTicks);clockLevel=null;}}
    @SubscribeEvent public static void fog(ViewportEvent.ComputeFogColor e){if(!active())return;if(mode==5){e.setRed(.16F);e.setGreen(.12F);e.setBlue(.21F);}else if(mode==1||mode==3){e.setRed(.035F);e.setGreen(.05F);e.setBlue(.073F);}else if(mode==4){e.setRed(.76F);e.setGreen(.67F);e.setBlue(.46F);}else if(mode>=10){e.setRed(.065F);e.setGreen(.059F);e.setBlue(.05F);}}
    @SubscribeEvent public static void mist(ViewportEvent.RenderFog e){
        if(!active()||mode!=3||e.getMode()!=net.minecraft.client.renderer.FogRenderer.FogMode.FOG_TERRAIN||Minecraft.getInstance().player.hasEffect(net.minecraft.world.effect.MobEffects.DARKNESS))return;
        e.setNearPlaneDistance(14);e.setFarPlaneDistance(48);e.setCanceled(true);
    }
    @SubscribeEvent public static void camera(ViewportEvent.ComputeCameraAngles e){if(!active()||shake<=0)return;var mc=Minecraft.getInstance();float intensity=shake*(float)(double)mc.options.screenEffectScale().get();double time=mc.player.tickCount+e.getPartialTick();e.setRoll(e.getRoll()+(float)Math.sin(time*1.9)*intensity*.8F);e.setPitch(e.getPitch()+(float)Math.sin(time*2.3)*intensity*.35F);}
    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Layers {
        @SubscribeEvent public static void overlay(RegisterGuiLayersEvent e){e.registerAboveAll(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"novel_caption"),(g,delta)->{
            if(!active()||captionTicks<=0||caption.isEmpty())return;var mc=Minecraft.getInstance();var lines=mc.font.split(net.minecraft.network.chat.Component.literal(caption),Math.min(380,g.guiWidth()-32));int y=g.guiHeight()-72-lines.size()*10;
            for(var line:lines){int width=mc.font.width(line);g.fill((g.guiWidth()-width)/2-5,y-2,(g.guiWidth()+width)/2+5,y+10,0x88000000);g.drawString(mc.font,line,(g.guiWidth()-width)/2,y,0xFFE8DED1);y+=11;}
        });}
    }
}
