package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.pipeline.*;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/** Four actual native room views. One original doll changes its presentation only during the feed pass. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class UncannyCameras {
    private static final int W=256,H=192;
    private static final DynamicTexture[] images=new DynamicTexture[4];private static final ResourceLocation[] ids=new ResourceLocation[4];
    private static final int[] frames=new int[4];private static final boolean[] dollFrames=new boolean[4];
    private static boolean diagnosticMask;private static int headPixels;private static final int[] headRect=new int[4];
    private static TextureTarget target;private static Lens lens;private static boolean recording,dollDrawn;private static int cycle,channel;
    private UncannyCameras(){}
    public static boolean recording(){return recording;}
    public static boolean maskDoll(){return diagnosticMask;}
    public static int verifiedHeadPixels(){return headPixels;}
    public static NativeImage copyFrame(int channel){if(images[channel]==null)return null;var copy=new NativeImage(W,H,false);copy.copyFrom(images[channel].getPixels());return copy;}
    public static RenderTarget target(){return target;}
    public static Camera camera(){return lens==null?Minecraft.getInstance().gameRenderer.getMainCamera():lens;}
    public static int frames(int camera){return frames[camera];}
    public static boolean ownFace(int camera){return dollFrames[camera];}
    public static void dollDrawn(LiteraryActor a){
        if(!recording||diagnosticMask||a.getId()!=UncannyClient.doll())return;dollDrawn=true;
        var d=a.getEyePosition().subtract(lens.getPosition());var view=new Matrix4f().rotation(lens.rotation().conjugate(new Quaternionf()));
        var projection=new Matrix4f().setPerspective((float)Math.toRadians(62),(float)W/H,.05F,128);
        int x0=W,y0=H,x1=0,y1=0;
        for(int x:new int[]{-1,1})for(int y:new int[]{-1,1})for(int z:new int[]{-1,1}){
            var point=new org.joml.Vector4f((float)d.x+x*.34F,(float)d.y+y*.34F,(float)d.z+z*.34F,1);
            new Matrix4f(projection).mul(view).transform(point);if(point.w<=0)continue;
            int px=(int)((point.x/point.w+1)*W/2),py=(int)((1-point.y/point.w)*H/2);
            x0=Math.min(x0,px);x1=Math.max(x1,px);y0=Math.min(y0,py);y1=Math.max(y1,py);
        }
        headRect[0]=Math.max(0,x0);headRect[1]=Math.max(0,y0);headRect[2]=Math.min(W,x1+1);headRect[3]=Math.min(H,y1+1);
    }
    private static final class Lens extends Camera {
        void aim(Vec3 at,Vec3 towards){var mc=Minecraft.getInstance();setup(mc.level,mc.player,true,false,mc.getTimer().getGameTimeDeltaPartialTick(false));var d=towards.subtract(at);setPosition(at);setRotation((float)Math.toDegrees(Math.atan2(d.z,d.x))-90,(float)-Math.toDegrees(Math.atan2(d.y,Math.hypot(d.x,d.z))));}
    }
    private static void initialize(){
        if(target!=null)return;target=new TextureTarget(W,H,true,Minecraft.ON_OSX);target.setClearColor(.025F,.04F,.04F,1);lens=new Lens();
        for(int i=0;i<4;i++){images[i]=new DynamicTexture(W,H,true);images[i].setFilter(false,false);ids[i]=Minecraft.getInstance().getTextureManager().register("uncanny_feed_"+i,images[i]);}
    }
    @SubscribeEvent public static void capture(RenderLevelStageEvent event){
        if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_LEVEL||recording||!UncannyClient.room().equals("missing_sleep"))return;
        var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null||mc.player.isSpectator()||++cycle%3!=0)return;
        var base=UncannyClient.base();if(mc.player.distanceToSqr(base.offset(UncannyRooms.MONITOR_AT).getCenter())>100)return;
        initialize();channel=(channel+1)%4;
        Vec3[] from={new Vec3(-8,8,-24),new Vec3(1.5,8,-28),new Vec3(8,8,-12),new Vec3(0,3,-4)};
        Vec3[] to={new Vec3(-6,5.6,-26),new Vec3(1.5,6,-14),new Vec3(5.5,6,-23),new Vec3(-7,1.5,-8)};
        var offset=Vec3.atLowerCornerOf(base);lens.aim(from[channel].add(offset),to[channel].add(offset));
        var oldProjection=new Matrix4f(RenderSystem.getProjectionMatrix());var sorting=RenderSystem.getVertexSorting();var main=mc.gameRenderer.getMainCamera();
        var oldView=new Matrix4f().rotation(main.rotation().conjugate(new Quaternionf()));var view=new Matrix4f().rotation(lens.rotation().conjugate(new Quaternionf()));
        var projection=new Matrix4f().setPerspective((float)Math.toRadians(62),(float)W/H,.05F,128);
        // renderLevel receives the camera view itself; the global stack must not apply it a second time.
        var modelView=RenderSystem.getModelViewStack();modelView.pushMatrix();modelView.identity();RenderSystem.applyModelViewMatrix();
        recording=true;dollDrawn=false;
        try{
            // RenderTarget.clear unbinds its framebuffer. Bind after clearing so the native passes draw into the feed.
            target.clear(Minecraft.ON_OSX);target.bindWrite(true);RenderSystem.setProjectionMatrix(projection,VertexSorting.DISTANCE_TO_ORIGIN);
            mc.levelRenderer.prepareCullFrustum(lens.getPosition(),view,projection);
            mc.levelRenderer.renderLevel(mc.getTimer(),false,lens,mc.gameRenderer,mc.gameRenderer.lightTexture(),view,projection);
            try(var shot=Screenshot.takeScreenshot(target)){images[channel].getPixels().copyFrom(shot);images[channel].upload();}
            frames[channel]++;dollFrames[channel]|=dollDrawn;
            if(Boolean.getBoolean("the_oldest_house.uncannySocketSmoke")&&channel==1&&dollDrawn&&headPixels<12){
                diagnosticMask=true;
                try{
                    target.clear(Minecraft.ON_OSX);target.bindWrite(true);
                    mc.levelRenderer.prepareCullFrustum(lens.getPosition(),view,projection);
                    mc.levelRenderer.renderLevel(mc.getTimer(),false,lens,mc.gameRenderer,mc.gameRenderer.lightTexture(),view,projection);
                    try(var masked=Screenshot.takeScreenshot(target)){
                        var normal=images[channel].getPixels();int changed=0;
                        for(int y=headRect[1];y<headRect[3];y++)for(int x=headRect[0];x<headRect[2];x++){
                            int a=normal.getPixelRGBA(x,y),b=masked.getPixelRGBA(x,y);
                            int difference=Math.abs((a&255)-(b&255))+Math.abs((a>>8&255)-(b>>8&255))+Math.abs((a>>16&255)-(b>>16&255));
                            if(difference>24)changed++;
                        }headPixels=Math.max(headPixels,changed);
                        if(frames[1]==2){
                            try{
                                var dir=java.nio.file.Path.of(System.getProperty("the_oldest_house.liveProofDir","build/uncanny-live-proof"));java.nio.file.Files.createDirectories(dir);var role=System.getProperty("the_oldest_house.liveProofRole","");
                                normal.writeToFile(dir.resolve(role+"-cctv-head-normal.png"));masked.writeToFile(dir.resolve(role+"-cctv-head-masked.png"));
                                java.nio.file.Files.writeString(dir.resolve(role+"-cctv-head-bounds.txt"),"Native head rectangle="+Arrays.toString(headRect)+" changed="+changed+" lens="+lens.getPosition()+"\n");
                            }catch(java.io.IOException ex){throw new IllegalStateException("Cannot retain native CCTV head readbacks",ex);}
                        }
                    }
                }finally{diagnosticMask=false;}
            }
        }finally{
            recording=false;modelView.popMatrix();RenderSystem.applyModelViewMatrix();mc.getMainRenderTarget().bindWrite(true);RenderSystem.setProjectionMatrix(oldProjection,sorting);
            mc.levelRenderer.prepareCullFrustum(main.getPosition(),oldView,oldProjection);
            mc.getEntityRenderDispatcher().prepare(mc.level,main,mc.crosshairPickEntity);
            RenderSystem.viewport(0,0,mc.getWindow().getWidth(),mc.getWindow().getHeight());
        }
    }
    public static void monitor(PoseStack p,MultiBufferSource buffers,int light){
        if(ids[0]==null||recording)return;
        for(int i=0;i<4;i++){
            float x=(2+i%2*6)/16F,y=(3+i/2*4)/16F,X=x+6/16F,Y=y+4/16F;
            var out=buffers.getBuffer(RenderType.text(ids[i]));quad(p,out,x,y,X,Y,14.15F/16);
        }
    }
    private static void quad(PoseStack p,VertexConsumer out,float x,float y,float X,float Y,float z){
        out.addVertex(p.last(),x,y,z).setColor(0xffb1c6b9).setUv(0,1).setLight(LightTexture.FULL_BRIGHT);
        out.addVertex(p.last(),X,y,z).setColor(0xffb1c6b9).setUv(1,1).setLight(LightTexture.FULL_BRIGHT);
        out.addVertex(p.last(),X,Y,z).setColor(0xffb1c6b9).setUv(1,0).setLight(LightTexture.FULL_BRIGHT);
        out.addVertex(p.last(),x,Y,z).setColor(0xffb1c6b9).setUv(0,0).setLight(LightTexture.FULL_BRIGHT);
    }
    public static final class MonitorScreen extends Screen {
        private int selected;
        public MonitorScreen(){super(Component.literal("Four-channel monitor"));}
        public int selected(){return selected;}
        private int panelWidth(){return Math.max(32,Math.min(256,Math.min((width-24)/2,(height-48)*2/3)));}
        @Override public boolean keyPressed(int key,int scan,int mods){if(key>=49&&key<=52){selected=key-49;return true;}return super.keyPressed(key,scan,mods);}
        @Override public boolean isPauseScreen(){return false;}
        @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
        @Override public boolean mouseClicked(double x,double y,int button){
            int w=panelWidth(),h=w*3/4,left=(width-2*w)/2,top=24;if(x<left||x>=left+w*2||y<top||y>=top+h*2)return false;selected=(y-top<h?0:1)*2+(x-left<w?0:1);return true;
        }
        @Override public void render(GuiGraphics g,int x,int y,float partial){
            g.fill(0,0,width,height,0xf0111816);int panelW=panelWidth(),panelH=panelW*3/4,left=(width-panelW*2)/2,top=24;g.drawCenteredString(font,"1–4: inspect feed   /   Esc: stand up",width/2,9,0xffc9d5ca);
            for(int i=0;i<4;i++){
                int px=left+i%2*panelW,py=top+i/2*panelH;
                if(ids[i]!=null)g.blit(ids[i],px,py,0,0,panelW,panelH,panelW,panelH);
                for(int line=0;line<panelH;line+=3)g.fill(px,py+line,px+panelW,py+line+1,0x3a07110b);
                g.drawString(font,"CAM 0"+(i+1)+" / "+new String[]{"BED","HALLWAY","PARENTS","STAIR"}[i],px+5,py+5,i==selected?0xffece9b8:0xffc9d5ca,false);
            }
            g.drawCenteredString(font,UncannyClient.sign(),width/2,height-17,0xffc9d5ca);super.render(g,x,y,partial);
        }
    }
    public static void clear(){
        var mc=Minecraft.getInstance();for(int i=0;i<4;i++){if(ids[i]!=null)mc.getTextureManager().release(ids[i]);ids[i]=null;images[i]=null;frames[i]=0;dollFrames[i]=false;}
        if(target!=null)target.destroyBuffers();target=null;lens=null;recording=false;diagnosticMask=false;headPixels=0;cycle=0;channel=0;
    }
}
