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
    private static int lease,scene,sceneTicks,flash,nextBolt=200,nextGust=120,boltId=-20_000_000;private static float target,storm,oStorm;private static ClientLevel world;private static Gale gale;
    /** The wind comes off the lake, from the north and a little west. */
    private static final double WIND_X=.34,WIND_Z=.94;
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
        weather(mc);
        // Real bolts, more often and nearer as the storm grows: the crack, then the thunder. They strike nothing.
        if(storm>.3F&&--nextBolt<=0){bolt(mc,false);nextBolt=(int)(50+mc.level.random.nextInt(220)*(1.25F-storm));}
        // Under everything, the gale; over it, gusts that swell and pass.
        if(gale==null&&storm>.06F&&mc.options.getSoundSourceVolume(SoundSource.WEATHER)>0){gale=new Gale(mc.level);mc.getSoundManager().play(gale);}
        if(storm>.35F&&--nextGust<=0){nextGust=60+mc.level.random.nextInt(120);mc.level.playLocalSound(mc.player.getX()-WIND_X*6,mc.player.getY()+2,mc.player.getZ()-WIND_Z*6,LiteraryRegistry.CABIN_WIND.get(),SoundSource.WEATHER,.4F+storm*.7F,.85F+mc.level.random.nextFloat()*.3F,false);}
    }
    /** Rain where the sky is open, heavier with the storm; leaves torn off and driven before the wind. */
    private static void weather(Minecraft mc){
        if(storm<.05F)return;var l=mc.level;var cam=mc.gameRenderer.getMainCamera().getPosition();var r=l.random;
        var setting=mc.options.particles().get();float share=setting==net.minecraft.client.ParticleStatus.MINIMAL?.25F:setting==net.minecraft.client.ParticleStatus.DECREASED?.55F:1;
        double gust=.65+.35*Math.sin(mc.player.tickCount*.05);double wind=(.08+.32*storm)*gust;
        int drops=(int)(storm*storm*80*share);
        for(int i=0;i<drops;i++){
            double x=cam.x+(r.nextDouble()-.5)*40,z=cam.z+(r.nextDouble()-.5)*40,y=cam.y+4+r.nextDouble()*12;
            if(l.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,(int)Math.floor(x),(int)Math.floor(z))>y)continue;
            l.addParticle(LiteraryRegistry.CABIN_RAIN.get(),x-WIND_X*wind*4,y,z-WIND_Z*wind*4,WIND_X*wind,-1.1-storm*.7,WIND_Z*wind);
        }
        int leaves=storm<.3F?0:(int)Math.ceil((storm-.3F)*5*share);
        for(int i=0;i<leaves;i++){
            double x=cam.x-WIND_X*14+(r.nextDouble()-.5)*24,z=cam.z-WIND_Z*14+(r.nextDouble()-.5)*24;int top=l.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,(int)Math.floor(x),(int)Math.floor(z));
            if(top>cam.y+6)continue;double speed=wind*(1.4+r.nextDouble());
            l.addParticle(LiteraryRegistry.CABIN_LEAF.get(),x,top+.3+r.nextDouble()*3,z,WIND_X*speed,.02+r.nextDouble()*.05,WIND_Z*speed);
        }
    }
    /** A client-only bolt (no fire, no damage, nobody else sees it): vanilla draws it, flashes the sky and plays its crack and thunder. */
    private static void bolt(Minecraft mc,boolean near){
        var l=mc.level;var r=l.random;var me=mc.player.position();
        double angle=near?Math.atan2(-WIND_Z,-WIND_X)+(r.nextDouble()-.5)*1.2:r.nextDouble()*Math.PI*2,distance=near?14+r.nextDouble()*12:24+r.nextDouble()*(70-storm*40);
        double x=me.x+Math.cos(angle)*distance,z=me.z+Math.sin(angle)*distance;int y=l.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,(int)Math.floor(x),(int)Math.floor(z));
        var strike=net.minecraft.world.entity.EntityType.LIGHTNING_BOLT.create(l);if(strike==null)return;
        strike.moveTo(x,y,z);strike.setVisualOnly(true);strike.setId(boltId--);l.addEntity(strike);
        if(near&&!mc.options.hideLightningFlash().get())flash=7;
    }
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
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){lease=0;scene=0;storm=0;oStorm=0;target=0;flash=0;world=null;if(gale!=null){Minecraft.getInstance().getSoundManager().stop(gale);gale=null;}}
    /** The storm's wind, looped under everything at the storm's own strength; it dies away when the storm does. */
    private static final class Gale extends net.minecraft.client.resources.sounds.AbstractTickableSoundInstance {
        private final ClientLevel level;
        Gale(ClientLevel level){super(LiteraryRegistry.CABIN_GALE.get(),SoundSource.WEATHER,net.minecraft.util.RandomSource.create());this.level=level;looping=true;delay=0;relative=true;attenuation=Attenuation.NONE;volume=.01F;x=y=z=0;}
        @Override public boolean canStartSilent(){return true;}
        @Override public void tick(){
            var mc=Minecraft.getInstance();if(mc.level!=level){stop();gale=null;return;}
            float want=active()?Mth.clamp(storm*1.15F,0,1):0;volume+=Mth.clamp(want-volume,-.02F,.02F);pitch=.82F+storm*.25F;
            if(!active()&&volume<=.01F){stop();gale=null;}
        }
    }
}
