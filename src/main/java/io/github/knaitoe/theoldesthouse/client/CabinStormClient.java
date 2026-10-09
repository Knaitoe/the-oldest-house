package io.github.knaitoe.theoldesthouse.client;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryRegistry;
import io.github.knaitoe.theoldesthouse.network.CabinStormPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

/**
 * One reader's storm at the cabin and the two scenes the visitors take something in. Only this client sees it: the
 * rain, thunder, darkened sky and flashes are the reader's own weather, driven by the server's account of the bargain.
 * Flashes honour the accessibility option that hides lightning; shaking scales with the screen-effect setting.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class CabinStormClient {
    public static final int HEART=1,ARM=2,BREAKING=3;
    /** The arm: the cord, the blow, black until waking, then the floor (the server's timeline). */
    static final int BLOW=io.github.knaitoe.theoldesthouse.labyrinth.CabinBargain.BLOW,WAKE=io.github.knaitoe.theoldesthouse.labyrinth.CabinBargain.WAKE,DONE=io.github.knaitoe.theoldesthouse.labyrinth.CabinBargain.DONE;
    private static int lease,scene,sceneTicks,flash,thunderIn,nextBolt=200;private static float target,storm,oStorm;private static ClientLevel world;
    private CabinStormClient(){}
    public static void accept(CabinStormPayload p){var mc=Minecraft.getInstance();if(mc.level==null)return;world=mc.level;lease=30;target=Mth.clamp(p.storm()/100F,0,1);
        if(p.scene()!=scene||Math.abs(p.sceneTicks()-sceneTicks)>6)sceneTicks=p.sceneTicks();scene=p.scene();if(p.flash()>0)bolt(mc,true);}
    private static boolean active(){var mc=Minecraft.getInstance();return lease>0&&mc.level!=null&&mc.level==world&&mc.player!=null&&!mc.player.isSpectator();}
    /** Rain the reader alone hears and sees, at the strength of their own bargain. */
    public static float rain(Level level,float nativeRain){return level==world&&active()?Math.max(nativeRain,Mth.clamp(storm*1.6F,0,1)):nativeRain;}
    public static float thunder(Level level,float nativeThunder){return level==world&&active()?Math.max(nativeThunder,Mth.clamp((storm-.45F)*1.8F,0,1)):nativeThunder;}
    /** The void biome the outside pockets use never rains; during this reader's storm, on the render thread only, it does. */
    public static boolean precipitating(){return RenderSystem.isOnRenderThread()&&active()&&storm>.02F;}
    public static float level(float partial){return Mth.lerp(partial,oStorm,storm);}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){var mc=Minecraft.getInstance();
        if(lease>0)lease--;if(!active()){if(lease<=0){scene=0;target=0;}oStorm=storm;storm=Math.max(0,storm-.02F);flash=Math.max(0,flash-1);return;}
        oStorm=storm;storm+=Mth.clamp(target-storm,-.012F,.012F);if(scene!=0)sceneTicks++;if(flash>0)flash--;
        if(thunderIn>0&&--thunderIn==0)mc.level.playLocalSound(mc.player.getX(),mc.player.getY()+12,mc.player.getZ(),SoundEvents.LIGHTNING_BOLT_THUNDER,SoundSource.WEATHER,1.4F+storm,.75F+mc.level.random.nextFloat()*.2F,false);
        // Lightning comes more often as the storm grows; it never strikes anything.
        if(storm>.5F&&--nextBolt<=0){bolt(mc,false);nextBolt=(int)(110+mc.level.random.nextInt(240)*(1.6F-storm));}
        if(storm>.15F&&mc.player.tickCount%100==0)mc.level.playLocalSound(mc.player.getX(),mc.player.getY()+2,mc.player.getZ(),LiteraryRegistry.CABIN_WIND.get(),SoundSource.WEATHER,.25F+storm*.6F,.9F+storm*.15F,false);
    }
    private static void bolt(Minecraft mc,boolean near){if(!mc.options.hideLightningFlash().get()){mc.level.setSkyFlashTime(2);flash=near?7:4;}thunderIn=near?3:12+mc.level.random.nextInt(30);}
    @SubscribeEvent public static void fogColor(ViewportEvent.ComputeFogColor e){if(!active()||storm<=0)return;float s=level((float)e.getPartialTick())*.7F;
        e.setRed(Mth.lerp(s,e.getRed(),.09F));e.setGreen(Mth.lerp(s,e.getGreen(),.1F));e.setBlue(Mth.lerp(s,e.getBlue(),.12F));}
    @SubscribeEvent public static void fog(ViewportEvent.RenderFog e){if(!active()||storm<=.05F||e.getMode()!=net.minecraft.client.renderer.FogRenderer.FogMode.FOG_TERRAIN)return;float s=level((float)e.getPartialTick());
        e.setFarPlaneDistance(Math.min(e.getFarPlaneDistance(),Mth.lerp(s,e.getFarPlaneDistance(),26)));e.setNearPlaneDistance(Math.min(e.getNearPlaneDistance(),Mth.lerp(s,e.getNearPlaneDistance(),2)));e.setCanceled(true);}
    @SubscribeEvent public static void camera(ViewportEvent.ComputeCameraAngles e){if(!active())return;var mc=Minecraft.getInstance();float effect=(float)(double)mc.options.screenEffectScale().get();double t=sceneTicks+e.getPartialTick();
        float roll=0,pitch=0;
        if(scene==HEART&&t<14){float k=(float)(1-t/14);roll+=(float)Math.sin(t*3.1)*2.2F*k;pitch+=(float)Math.sin(t*2.3)*1.6F*k;}
        if(scene==ARM){
            if(t<BLOW-4)roll+=(float)Math.sin(t*.09)*.9F;
            else if(t<BLOW+6){float k=(float)(1-Math.abs(t-BLOW)/8);roll+=(float)Math.sin(t*4.7)*9*k;pitch+=(float)Math.sin(t*3.3)*5*k+4*k;}
            else if(t>=WAKE&&t<DONE){float k=(float)((DONE-t)/(DONE-WAKE));roll+=24*k*k;}
        }
        roll+=(float)Math.sin((mc.player.tickCount+e.getPartialTick())*.07)*.35F*storm;
        if(roll!=0||pitch!=0){e.setRoll(e.getRoll()+roll*effect);e.setPitch(e.getPitch()+pitch*effect);}
    }
    private static final String[][] DARK={{"95","Sabrina: Breathe. In through your nose."},{"118","Sabrina: Stay with me. Look at me. Good."},{"138","Sabrina: Press here. Harder."},{"156","Leonard: It's done. It's done now."}};
    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Layers {
        @SubscribeEvent public static void register(RegisterGuiLayersEvent e){e.registerAboveAll(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"cabin_storm"),(g,delta)->{
            if(!active())return;var mc=Minecraft.getInstance();int w=g.guiWidth(),h=g.guiHeight();float t=sceneTicks+delta.getGameTimeDeltaPartialTick(false);
            if(flash>0)g.fill(0,0,w,h,((int)(flash/7F*110)<<24)|0xE8EEFF);
            if(scene==HEART&&t<60){int a=(int)(Math.max(0,1-t/60)*120);edges(g,w,h,a,0x8A0000);if(t<8)g.fill(0,0,w,h,((int)((1-t/8)*90)<<24)|0x6A0000);}
            if(scene==ARM){
                if(t<BLOW){int a=(int)(Mth.clamp(t/BLOW,0,1)*170);edges(g,w,h,a,0x050000);}
                else if(t<BLOW+3)g.fill(0,0,w,h,0xFF9A0000);
                else if(t<WAKE){g.fill(0,0,w,h,0xFF000000);for(var line:DARK){int at=Integer.parseInt(line[0]);if(t>=at&&t<at+22){var text=Component.literal(line[1]);g.drawCenteredString(mc.font,text,w/2,h/2-4,0xFFD8CFC2);}}}
                else if(t<DONE){float k=(float)((DONE-t)/(DONE-WAKE));g.fill(0,0,w,h,((int)(k*235)<<24));edges(g,w,h,(int)(80+k*120),0x3A0000);}
            }
        });}
        private static void edges(net.minecraft.client.gui.GuiGraphics g,int w,int h,int alpha,int rgb){alpha=Mth.clamp(alpha,0,255);int band=Math.max(8,Math.min(w,h)/5);
            for(int i=0;i<band;i+=2){int a=(int)(alpha*(1-i/(float)band));int c=(a<<24)|rgb;g.fill(0,i,w,i+2,c);g.fill(0,h-i-2,w,h-i,c);g.fill(i,0,i+2,h,c);g.fill(w-i-2,0,w-i,h,c);}}
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){lease=0;scene=0;storm=0;oStorm=0;target=0;flash=0;world=null;}
}
