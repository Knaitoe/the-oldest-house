package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LakePhoneCamera;
import io.github.knaitoe.theoldesthouse.labyrinth.PhoneCanoe;
import io.github.knaitoe.theoldesthouse.network.LakePhonePayload;
import net.minecraft.client.*;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

/**
 * Client controls always expire; a disconnect, dimension switch or missing camera restores normal play.
 *
 * <p>0.4.121 films it. While recording, the screen is the phone's viewfinder: a blinking REC, a running timecode, the
 * frame corners and a little night grain. When the phone goes over the side the picture tumbles and splashes, then lies
 * looking up through the water at a moon that will not hold still, with the lake's own muffled sound, for the seven
 * seconds the server holds it. The fade to black beats like a heart, and the reader comes to soaked, dripping.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class LakePhoneClient {
    /** The tumble after the phone leaves the hand, and how long the reader takes to come to on the jetty. */
    static final int TUMBLE=24,WAKE=90;
    private static int phase,cameraId=-1,elapsed,lease,waking,dripping;
    private static net.minecraft.client.multiplayer.ClientLevel sceneLevel;
    private static CameraType priorView;
    private static boolean splashed;
    private LakePhoneClient(){}
    public static void accept(LakePhonePayload packet){
        if(packet.phase()==0){boolean finished=packet.elapsed()>=PhoneCanoe.END&&phase>=3;clear();if(finished)wake();return;}
        phase=packet.phase();cameraId=packet.cameraId();elapsed=packet.elapsed();lease=80;
    }
    public static boolean bound(){return phase>=2&&lease>0;}
    /** Ticks since the phone went over the side, or a negative number while it is still filming. */
    static float sinceDrop(float partial){return elapsed+partial-PhoneCanoe.FILM_TICKS;}
    @SubscribeEvent public static void tick(ClientTickEvent.Post event){
        if(lease>0){lease--;elapsed++;}enforce();
        var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.isPaused())return;
        if(bound()&&phase>=3){
            int t=elapsed-PhoneCanoe.FILM_TICKS;
            if(!splashed&&mc.getCameraEntity() instanceof LakePhoneCamera camera){splashed=true;
                for(int i=0;i<24;i++)mc.level.addParticle(ParticleTypes.SPLASH,camera.getX()+(mc.level.random.nextDouble()-.5)*.8,camera.getY()+.2,camera.getZ()+(mc.level.random.nextDouble()-.5)*.8,0,.2,0);
                ui(SoundEvents.AMBIENT_UNDERWATER_ENTER,1,.7F);}
            // Under the water, the world goes thick: a low drone and the odd bubble rising past the lens.
            if(t>TUMBLE&&t%60==0)ui(SoundEvents.AMBIENT_UNDERWATER_LOOP_ADDITIONS,.8F,.45F);
            if(mc.getCameraEntity() instanceof LakePhoneCamera camera&&t%7==0)mc.level.addParticle(ParticleTypes.BUBBLE,camera.getX()+(mc.level.random.nextDouble()-.5)*.6,camera.getY()-.3,camera.getZ()+(mc.level.random.nextDouble()-.5)*.6,0,.06,0);
            // The heart, slowing, under the fade to black.
            int beat=elapsed-(PhoneCanoe.FILM_TICKS+PhoneCanoe.SKY_TICKS);
            if(beat==-16||beat==0||beat==14||beat==28)ui(SoundEvents.WARDEN_HEARTBEAT,beat<0?1.05F:.85F,1F);
        }
        if(waking>0){waking--;if(waking==WAKE-4){ui(SoundEvents.AMBIENT_UNDERWATER_EXIT,1,.8F);ui(SoundEvents.PLAYER_SPLASH,.7F,.25F);}}
        if(dripping>0){dripping--;if(dripping%3==0){var p=mc.player;mc.level.addParticle(ParticleTypes.DRIPPING_WATER,p.getX()+(p.getRandom().nextDouble()-.5)*.6,p.getY()+.6+p.getRandom().nextDouble()*1.2,p.getZ()+(p.getRandom().nextDouble()-.5)*.6,0,0,0);}}
    }
    private static void ui(net.minecraft.sounds.SoundEvent sound,float pitch,float volume){Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound,pitch,volume));}
    private static void wake(){waking=WAKE;dripping=240;}
    @SubscribeEvent public static void frame(RenderFrameEvent.Pre event){enforce();}
    private static void enforce(){
        Minecraft mc=Minecraft.getInstance();
        if(!bound()||mc.player==null||mc.level==null||mc.player.isDeadOrDying()||mc.player.isSpectator()||(sceneLevel!=null&&sceneLevel!=mc.level)){if(phase!=0||priorView!=null||sceneLevel!=null)clear();return;}
        if(sceneLevel==null)sceneLevel=mc.level;
        if(priorView==null)priorView=mc.options.getCameraType();mc.options.setCameraType(CameraType.FIRST_PERSON);
        if(mc.screen instanceof AbstractContainerScreen<?>)mc.setScreen(null);
        if(phase>=3&&mc.level.getEntity(cameraId) instanceof LakePhoneCamera camera)mc.setCameraEntity(camera);
    }
    public static void clear(){
        Minecraft mc=Minecraft.getInstance();
        if(mc.getCameraEntity() instanceof LakePhoneCamera)mc.setCameraEntity(mc.player);
        if(priorView!=null)mc.options.setCameraType(priorView);priorView=null;
        sceneLevel=null;phase=0;cameraId=-1;lease=0;splashed=false;
    }
    @SubscribeEvent public static void input(MovementInputUpdateEvent event){
        if(!(bound()&&phase>=3)&&waking<=0)return;var input=event.getInput();input.forwardImpulse=0;input.leftImpulse=0;
        input.up=false;input.down=false;input.left=false;input.right=false;input.jumping=false;input.shiftKeyDown=false;
    }
    @SubscribeEvent public static void interact(InputEvent.InteractionKeyMappingTriggered event){if(bound()&&phase>=3){event.setCanceled(true);event.setSwingHand(false);}}
    @SubscribeEvent public static void angles(ViewportEvent.ComputeCameraAngles event){
        if(!bound()||phase<3)return;float t=sinceDrop((float)event.getPartialTick());
        if(t<TUMBLE){
            // Over and over as it falls: the sky, the canoe's side, the water, and the sky again.
            float k=Math.max(0,t)/TUMBLE;event.setYaw(Mth.lerp(k,0,-310)+Mth.sin(t*.9F)*20);event.setPitch(Mth.lerp(k*k,20,-88)+Mth.sin(t*.7F)*55*(1-k));event.setRoll(Mth.lerp(k,0,520)%360);
            return;
        }
        // Lying on its back in the water, rocked by what is left of the wake.
        float s=t-TUMBLE;event.setYaw(Mth.sin(s*.031F)*4);event.setPitch(-88+Mth.sin(s*.047F)*1.5F);event.setRoll(3+Mth.sin(s*.053F)*2.5F);
    }

    /** The phone's viewfinder, shared by the overlay and the native proof: frame corners, REC, timecode, battery, grain. */
    static void viewfinder(GuiGraphics g,Font font,int w,int h,int ticks,boolean wet){
        int bar=Math.max(10,h/16);g.fill(0,0,w,bar,0x8C050709);g.fill(0,h-bar,w,h,0x8C050709);
        int inset=Math.max(14,w/28),arm=Math.max(10,w/40),c=0xD8F2F0EA;
        for(int sx:new int[]{-1,1})for(int sy:new int[]{-1,1}){int x=sx<0?inset:w-inset,y=sy<0?bar+inset/2:h-bar-inset/2;
            g.fill(Math.min(x,x-sx*arm),y,Math.max(x,x-sx*arm)+1,y+1,c);g.fill(x,Math.min(y,y-sy*arm),x+1,Math.max(y,y-sy*arm)+1,c);}
        int cx=w/2,cy=h/2;for(int sx:new int[]{-1,1})for(int sy:new int[]{-1,1}){g.fill(cx+sx*12,cy+sy*9,cx+sx*7,cy+sy*9+(sy>0?1:-1),0x90F2F0EA);g.fill(cx+sx*12,cy+sy*9,cx+sx*12+(sx>0?1:-1),cy+sy*4,0x90F2F0EA);}
        boolean dot=(ticks/10)%2==0;int top=(bar-8)/2;
        if(dot)g.fill(12,top+1,18,top+7,0xFFE0453A);
        g.drawString(font,"REC",22,top,wet?0xFFACB1AB:0xFFF0EDE6,false);
        String time=String.format(java.util.Locale.ROOT,"00:00:%02d:%02d",ticks/20,(ticks%20)*3/2);g.drawString(font,time,cx-font.width(time)/2,top,0xFFF0EDE6,false);
        int bx=w-34;g.fill(bx,top+1,bx+18,top+8,0xFFF0EDE6);g.fill(bx+1,top+2,bx+17,top+7,0xFF101214);g.fill(bx+2,top+3,bx+11,top+6,0xFFF0EDE6);g.fill(bx+18,top+3,bx+20,top+6,0xFFF0EDE6);
        g.drawString(font,"NIGHT  ISO 6400",12,h-bar+(bar-8)/2,0xB8F0EDE6,false);String q=wet?"LENS WET":"1080p 30";g.drawString(font,q,w-12-font.width(q),h-bar+(bar-8)/2,wet?0xFFE0B04A:0xB8F0EDE6,false);
        // Night grain: a scatter of faint specks, new every frame.
        var random=java.util.concurrent.ThreadLocalRandom.current();for(int i=0;i<60;i++){int x=random.nextInt(w),y=random.nextInt(h);g.fill(x,y,x+1,y+1,(random.nextBoolean()?0x22FFFFFF:0x22000000));}
    }
    /** The view from under the water: a teal dark, and the moon above broken into drifting bands. */
    static void waterline(GuiGraphics g,int w,int h,float t){
        g.fillGradient(0,0,w,h,0x30163A44,0x7A031015);
        int cx=w/2+(int)(Mth.sin(t*.031F)*6),cy=(int)(h*.36F),r=Math.max(10,h/12);
        for(int y=-r*2;y<=r*2;y++){float d=Math.abs(y)/(r*2F);int half=(int)(Math.sqrt(Math.max(0,1-d*d))*r*2);int shift=(int)(Mth.sin(t*.22F+y*.31F)*3);g.fill(cx-half+shift,cy+y,cx+half+shift,cy+y+1,0x1CC8E0E0);}
        for(int y=-r;y<=r;y++){float d=Math.abs(y)/(float)r;int half=(int)(Math.sqrt(Math.max(0,1-d*d))*r);float wave=Mth.sin(t*.25F+y*.42F);int shift=(int)(wave*3.5F);
            int alpha=(int)(150+60*Mth.sin(t*.13F+y*.2F));if((y+((int)t/3))%5==0)continue;g.fill(cx-half+shift,cy+y,cx+half+shift,cy+y+1,(alpha<<24)|0xE9EED8);}
        for(int i=0;i<5;i++){int y=(int)(h*.15F+i*h*.12F+Mth.sin(t*.05F+i)*4);int x0=(int)(w*.1F+Mth.sin(t*.04F+i*2)*20);g.fill(x0,y,x0+w/3,y+1,0x10E0F0F0);}
    }
    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Overlay {
        @SubscribeEvent public static void register(RegisterGuiLayersEvent event){
            event.registerAboveAll(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"lake_phone"),(g,delta)->{
                int w=g.guiWidth(),h=g.guiHeight();var mc=Minecraft.getInstance();
                if(waking>0){float k=waking/(float)WAKE;g.fill(0,0,w,h,((int)(Mth.clamp(k*1.3F,0,1)*255)<<24)|0x020405);
                    int edge=(int)(h*.35F*k);for(int i=0;i<edge;i+=2){int a=(int)(140*k*(1-i/(float)Math.max(1,edge)));g.fill(0,i,w,i+2,(a<<24)|0x02080A);g.fill(0,h-i-2,w,h-i,(a<<24)|0x02080A);}return;}
                if(!bound())return;float t=sinceDrop(delta.getGameTimeDeltaPartialTick(false));
                if(phase>=3&&t>TUMBLE*.5F)waterline(g,w,h,t);
                if(phase>=3&&t<TUMBLE)g.fill(0,0,w,h,((int)(90*Mth.abs(Mth.sin(t*.8F)))<<24)|0x0A1C24);
                viewfinder(g,mc.font,w,h,elapsed,phase>=3);
            });
        }
    }
}
