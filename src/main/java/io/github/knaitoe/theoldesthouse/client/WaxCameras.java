package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.WaxSite;
import io.github.knaitoe.theoldesthouse.network.WaxFeedPayload;
import io.github.knaitoe.theoldesthouse.network.WaxMonitorPayload;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/**
 * 0.4.752, the waxworks' six monitors (docs/WAXWORKS_0_4_752.md). While this reader sits at the bank, one camera a frame draws the
 * museum itself from its corner (the native level renderer from that lens, the same way the bedroom's monitor is drawn), turned to the
 * grey, grainy, flat picture a night camera gives: the galleries are dark, so the picture is brightened, and what moves in it is what
 * is there. Other readers are in it. The screens go to snow when the night fails.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class WaxCameras {
    private WaxCameras(){}
    private static final int W=192,H=144,FEEDS=6;
    private static final String[] NAMES={"FOYER","STATESMEN","KITCHEN","CHAMBER","WORKSHOP","CORRIDOR"};
    private static final DynamicTexture[] images=new DynamicTexture[FEEDS];private static final ResourceLocation[] ids=new ResourceLocation[FEEDS];
    private static final int[] frames=new int[FEEDS];
    private static TextureTarget target;private static Lens lens;private static boolean recording;private static int channel,cycle;
    private static BlockPos base;private static boolean dark;private static String time="11:52";
    public static boolean recording(){return recording;}
    public static TextureTarget target(){return target;}
    public static int frames(int feed){return frames[feed];}
    public static Camera camera(){return lens==null?Minecraft.getInstance().gameRenderer.getMainCamera():lens;}

    private static final class Lens extends Camera {
        void aim(Vec3 at,float yaw,float pitch){var mc=Minecraft.getInstance();setup(mc.level,mc.player,true,false,mc.getTimer().getGameTimeDeltaPartialTick(false));setPosition(at);setRotation(yaw,pitch);}
    }
    /** Sat down at the bank (from the server), or the screens gone dark while sitting there. */
    public static void open(WaxMonitorPayload p){
        base=p.base();dark=p.dark();time=p.time();
        var mc=Minecraft.getInstance();
        if(!(mc.screen instanceof MonitorScreen))mc.setScreen(new MonitorScreen());
    }
    private static void initialize(){
        if(target!=null)return;target=new TextureTarget(W,H,true,Minecraft.ON_OSX);target.setClearColor(0,0,0,1);lens=new Lens();
        for(int i=0;i<FEEDS;i++){images[i]=new DynamicTexture(W,H,true);images[i].setFilter(false,false);ids[i]=Minecraft.getInstance().getTextureManager().register("wax_feed_"+i,images[i]);}
    }
    @SubscribeEvent public static void capture(RenderLevelStageEvent event){
        if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_LEVEL||recording||base==null||dark)return;
        var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null||!(mc.screen instanceof MonitorScreen)||++cycle%2!=0)return;
        initialize();channel=(channel+1)%FEEDS;
        var cam=WaxSite.CAMERAS.get(channel);var at=Vec3.atCenterOf(base.offset(cam.at()));
        double yaw=Math.toRadians(cam.yaw());at=at.add(-Math.sin(yaw)*.45,-.1,Math.cos(yaw)*.45);
        lens.aim(at,cam.yaw(),channel==5?12F:26F);
        var oldProjection=new Matrix4f(RenderSystem.getProjectionMatrix());var sorting=RenderSystem.getVertexSorting();var main=mc.gameRenderer.getMainCamera();
        var oldView=new Matrix4f().rotation(main.rotation().conjugate(new Quaternionf()));var view=new Matrix4f().rotation(lens.rotation().conjugate(new Quaternionf()));
        var projection=new Matrix4f().setPerspective((float)Math.toRadians(78),(float)W/H,.05F,96);
        var modelView=RenderSystem.getModelViewStack();modelView.pushMatrix();modelView.identity();RenderSystem.applyModelViewMatrix();
        recording=true;
        try{
            target.clear(Minecraft.ON_OSX);target.bindWrite(true);RenderSystem.setProjectionMatrix(projection,VertexSorting.DISTANCE_TO_ORIGIN);
            mc.levelRenderer.prepareCullFrustum(lens.getPosition(),view,projection);
            mc.levelRenderer.renderLevel(mc.getTimer(),false,lens,mc.gameRenderer,mc.gameRenderer.lightTexture(),view,projection);
            try(var shot=Screenshot.takeScreenshot(target)){night(shot,channel,mc.level.getGameTime());images[channel].getPixels().copyFrom(shot);images[channel].upload();}
            frames[channel]++;
        }catch(RuntimeException ex){TheOldestHouse.LOGGER.warn("Waxworks feed {} could not be drawn: {}",channel,ex.toString());}
        finally{
            recording=false;modelView.popMatrix();RenderSystem.applyModelViewMatrix();mc.getMainRenderTarget().bindWrite(true);RenderSystem.setProjectionMatrix(oldProjection,sorting);
            mc.levelRenderer.prepareCullFrustum(main.getPosition(),oldView,oldProjection);
            mc.getEntityRenderDispatcher().prepare(mc.level,main,mc.crosshairPickEntity);
            RenderSystem.viewport(0,0,mc.getWindow().getWidth(),mc.getWindow().getHeight());
        }
    }
    /** A night camera's picture: grey, brightened out of the dark, with grain that moves. */
    static void night(NativeImage img,int feed,long time){
        long seed=time*31+feed*977;
        for(int y=0;y<img.getHeight();y++)for(int x=0;x<img.getWidth();x++){
            int c=img.getPixelRGBA(x,y);int r=c&255,g=c>>8&255,b=c>>16&255;
            double lum=.3*r+.59*g+.11*b;
            seed=seed*6364136223846793005L+1442695040888963407L;double grain=((seed>>>40)&255)/255.0-.5;
            int v=(int)Math.max(0,Math.min(255,14+Math.sqrt(lum/255.0)*215+grain*26));
            img.setPixelRGBA(x,y,0xff000000|v<<16|v<<8|v);
        }
    }
    static void stood(){PacketDistributor.sendToServer(new WaxFeedPayload(-1,false));}

    public static final class MonitorScreen extends Screen {
        private int selected;
        MonitorScreen(){super(Component.literal("Security monitors"));}
        @Override protected void init(){PacketDistributor.sendToServer(new WaxFeedPayload(selected,false));}
        @Override public boolean isPauseScreen(){return false;}
        @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
        private int panelW(){return Math.max(48,Math.min(220,Math.min((width-24)/3,(height-64)*4/6)));}
        private void select(int i){if(i==selected)return;selected=i;PacketDistributor.sendToServer(new WaxFeedPayload(selected,false));}
        private void note(){PacketDistributor.sendToServer(new WaxFeedPayload(selected,true));}
        @Override public boolean keyPressed(int key,int scan,int mods){
            if(key>=49&&key<=54){select(key-49);return true;}
            if(key==78){note();return true;}
            return super.keyPressed(key,scan,mods);
        }
        @Override public boolean mouseClicked(double x,double y,int button){
            int w=panelW(),h=w*3/4,left=(width-3*w)/2,top=26;
            if(x>=left&&x<left+3*w&&y>=top&&y<top+2*h){int i=(int)((y-top)/h)*3+(int)((x-left)/w);if(i==selected&&button==1)note();else select(i);return true;}
            int by=top+2*h+8;if(y>=by&&y<by+14&&x>=width/2-70&&x<width/2+70){note();return true;}
            return super.mouseClicked(x,y,button);
        }
        @Override public void removed(){stood();super.removed();}
        @Override public void render(GuiGraphics g,int mx,int my,float partial){
            g.fill(0,0,width,height,0xf00b0f0d);
            int w=panelW(),h=w*3/4,left=(width-3*w)/2,top=26;var mc=Minecraft.getInstance();long now=mc.level==null?0:mc.level.getGameTime();
            g.drawCenteredString(font,"1-6: a feed   N or right-click: something's moved   Esc: stand up",width/2,9,0xffc9d1c9);
            for(int i=0;i<FEEDS;i++){
                int px=left+i%3*w,py=top+i/3*h;
                if(dark)snow(g,px,py,w,h,now+i*17);
                else if(ids[i]!=null&&frames[i]>0)g.blit(ids[i],px+1,py+1,0,0,w-2,h-2,w-2,h-2);
                else g.fill(px+1,py+1,px+w-1,py+h-1,0xff101410);
                for(int line=py;line<py+h;line+=3)g.fill(px,line,px+w,line+1,0x2a000000);
                int edge=i==selected?0xffe6e2b8:0xff3a423c;
                g.fill(px,py,px+w,py+1,edge);g.fill(px,py+h-1,px+w,py+h,edge);g.fill(px,py,px+1,py+h,edge);g.fill(px+w-1,py,px+w,py+h,edge);
                g.drawString(font,"CAM "+(i+1)+"  "+NAMES[i],px+4,py+4,i==selected?0xffe6e2b8:0xffb9c2b9,false);
                g.drawString(font,time,px+w-4-font.width(time),py+h-12,0xffb9c2b9,false);
            }
            int by=top+2*h+8;boolean over=mx>=width/2-70&&mx<width/2+70&&my>=by&&my<by+14;
            g.fill(width/2-70,by,width/2+70,by+14,over?0xff3a423c:0xff222824);
            g.drawCenteredString(font,"Something's moved",width/2,by+3,0xffe6e2b8);
            super.render(g,mx,my,partial);
        }
        private void snow(GuiGraphics g,int x,int y,int w,int h,long t){
            long seed=t*7919;
            for(int yy=y+1;yy<y+h-1;yy+=3)for(int xx=x+1;xx<x+w-1;xx+=3){seed=seed*6364136223846793005L+1442695040888963407L;int v=(int)((seed>>>40)&255);g.fill(xx,yy,Math.min(xx+3,x+w-1),Math.min(yy+3,y+h-1),0xff000000|v<<16|v<<8|v);}
        }
    }
    public static void clear(){
        var mc=Minecraft.getInstance();for(int i=0;i<FEEDS;i++){if(ids[i]!=null)mc.getTextureManager().release(ids[i]);ids[i]=null;images[i]=null;frames[i]=0;}
        if(target!=null)target.destroyBuffers();target=null;lens=null;recording=false;base=null;dark=false;
    }
}
