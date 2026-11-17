package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.HallFeed;
import io.github.knaitoe.theoldesthouse.network.HallFeedPayload;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;

/**
 * 0.4.662, the feed (docs/HALL_FEED_0_4_662.md), as one reader's game draws it. While a hall is watched, a picture of it is taken once a
 * second from the camera the server chose, aimed where the reader is but without them in it, and the reader's way across it is kept.
 * When the server shows the feed, a monitor hangs on the wall of the next hall: the last picture, washed grey like a night camera's,
 * running a clock; after a few seconds footprints come into it one at a time along the way the reader walked, out to the door they left
 * by, and the monitor gives out a footstep with each. Then the picture breaks up and the screen goes dark.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class HallFeedClient {
    public static final int W=160,H=120;
    /** Ticks between footprints (a walking pace), before the first, after the last, and of the picture breaking up. */
    public static final int PACE=8,DELAY=70,AFTER=40,BREAK=24;
    /** Long ago, for times not yet come (well clear of overflowing when subtracted from). */
    private static final long NEVER=-1_000_000L;
    public static final ModelResourceLocation MODEL=ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"block/hall_feed/monitor"));

    private static Vec3 camera;private static boolean watching,frozen;
    private static long lastShot=NEVER,lastMark=NEVER;
    private static int[] still;private static Matrix4f seen;private static Vec3 seenFrom;
    private static final ArrayDeque<HallFeed.Mark> trail=new ArrayDeque<>();
    private static BlockPos monitor;private static Direction facing=Direction.NORTH;private static long left,start;
    private static List<HallFeed.Print> prints=List.of();private static int heard=-1,clock;
    private static TextureTarget target;private static Lens lens;private static boolean recording;
    private static DynamicTexture screen;private static ResourceLocation screenId;private static long frame=Long.MIN_VALUE;private static boolean screenOn;
    private static int shots;

    private HallFeedClient(){}

    public static boolean recording(){return recording;}
    public static com.mojang.blaze3d.pipeline.RenderTarget target(){return target;}
    /** For checks: whether a hall is watched, how many pictures were taken, the footprints to come, and whether the screen is lit. */
    public static boolean watching(){return watching;}
    public static int shots(){return shots;}
    public static List<HallFeed.Print> prints(){return prints;}
    public static boolean screenOn(){return screenOn;}
    public static BlockPos monitor(){return monitor;}
    /** The last picture taken of the watched hall, grey levels row by row, or null. For checks. */
    public static int[] picture(){return still;}

    public static void accept(HallFeedPayload p){
        switch(p.mode()){
            case HallFeedPayload.ARM->{camera=new Vec3(p.x(),p.y(),p.z());watching=true;frozen=false;trail.clear();still=null;seen=null;lastShot=NEVER;lastMark=NEVER;}
            case HallFeedPayload.SHOW->{
                watching=false;monitor=BlockPos.containing(p.x(),p.y(),p.z());facing=Direction.from2DDataValue(p.facing());left=p.time();start=left+DELAY;
                prints=HallFeed.walked(new ArrayList<>(trail),HallFeed.STRIDE,HallFeed.LENGTH);heard=-1;frame=Long.MIN_VALUE;screenOn=false;
                var mc=Minecraft.getInstance();long day=mc.level==null?18000:mc.level.getDayTime();clock=(int)(((day/1000+6)%24)*3600+(day%1000)*3600/1000);
            }
            case HallFeedPayload.HIDE->{monitor=null;prints=List.of();screenOn=false;}
            default->clear();
        }
    }
    public static void clear(){camera=null;watching=false;frozen=false;trail.clear();still=null;seen=null;monitor=null;prints=List.of();screenOn=false;recording=false;}
    @SubscribeEvent public static void out(ClientPlayerNetworkEvent.LoggingOut e){clear();}

    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null)return;long now=mc.level.getGameTime();
        if(watching&&!frozen&&now-lastMark>=4){
            lastMark=now;var at=mc.player.position();
            if(!trail.isEmpty()&&trail.peekLast().at().distanceTo(at)>4)frozen=true;
            else if(mc.player.onGround()){
                var below=BlockPos.containing(at.x,at.y-.2,at.z);var type=mc.level.getBlockState(below).getSoundType(mc.level,below,mc.player);
                trail.add(new HallFeed.Mark(at,type.getStepSound(),type.getVolume(),type.getPitch()));while(trail.size()>120)trail.removeFirst();
            }
        }
        if(monitor==null)return;
        long end=start+(long)PACE*prints.size()+AFTER;
        while(heard+1<prints.size()&&now>=start+(long)PACE*(heard+1)&&now<end){
            heard++;var p=prints.get(heard);float fade=1-.45F*heard/Math.max(1,prints.size()-1);
            mc.getSoundManager().play(new SimpleSoundInstance(p.step(),SoundSource.BLOCKS,.22F*fade*p.volume(),p.pitch()*.9F,RandomSource.create(now*31+heard),monitor.getX()+.5,monitor.getY()+.2,monitor.getZ()+.5));
        }
        if(now<frame+4)return;frame=now;
        if(now>=end+BREAK){screenOn=false;return;}
        compose(now,now>=end);
    }

    /**
     * For the client proof: the feed as the monitor would show it, from a picture and a camera given, with the first {@code shown}
     * footprints come (the last of them new), or breaking up. Returns the screen's texture; the feed's own state is put back after.
     */
    public static ResourceLocation proofFrame(int[] picture,Matrix4f camera,Vec3 from,List<HallFeed.Print> walk,int shown,boolean breaking){
        var keep=new Object[]{still,seen,seenFrom,prints};long keepLeft=left,keepStart=start;int keepClock=clock;
        still=picture;seen=camera;seenFrom=from;prints=walk;left=0;start=0;clock=3*3600+17*60;
        try{compose(shown<=0?-1:(long)PACE*(shown-1)+3,breaking);}
        finally{still=(int[])keep[0];seen=(Matrix4f)keep[1];seenFrom=(Vec3)keep[2];@SuppressWarnings("unchecked") var p=(List<HallFeed.Print>)keep[3];prints=p;left=keepLeft;start=keepStart;clock=keepClock;}
        return screenId;
    }
    /** The screen's pixels as last composed, for the client proof. */
    public static NativeImage screenPixels(){return screen==null?null:screen.getPixels();}
    /** Where a point falls in the picture taken with {@code camera} from {@code from}, for the client proof. */
    public static int[] proofProject(Matrix4f camera,Vec3 from,Vec3 world){var keepSeen=seen;var keepFrom=seenFrom;seen=camera;seenFrom=from;try{return project(world);}finally{seen=keepSeen;seenFrom=keepFrom;}}

    private static void compose(long now,boolean breaking){
        var mc=Minecraft.getInstance();
        if(screen==null){screen=new DynamicTexture(W,H,true);screen.setFilter(false,false);screenId=mc.getTextureManager().register("hall_feed_screen",screen);}
        var img=screen.getPixels();var rnd=new Random(now*7919L+13);int band=(int)(now*3%(H+40))-20;
        for(int y=0;y<H;y++)for(int x=0;x<W;x++){
            int v;
            if(breaking)v=rnd.nextInt(200)+(still==null?0:still[y*W+x]/5);
            else{
                v=still==null?14:still[y*W+x];v+=rnd.nextInt(19)-9;if((y&1)==1)v=v*7/8;
                int db=Math.abs(y-band);if(db<6)v+=(6-db)*3;
                double dx=(x-W/2.0)/(W/2.0),dy=(y-H/2.0)/(H/2.0);v=(int)(v*(1-.32*(dx*dx+dy*dy)));
            }
            img.setPixelRGBA(x,y,grey(v));
        }
        if(!breaking&&still!=null&&seen!=null)for(int i=0;i<prints.size();i++){long due=start+(long)PACE*i;if(now<due)break;print(img,prints.get(i),now-due<8);}
        if(!breaking){int t=clock+(int)Math.max(0,(now-left)/20);time(img,4,H-8,t);}
        screen.upload();screenOn=true;
    }
    private static int grey(int v){v=Math.max(0,Math.min(255,v));int r=v*84/100,g=v*96/100,b=v*80/100;return 0xff000000|b<<16|g<<8|r;}
    private static int scale(int c,double k){int r=c&255,g=c>>8&255,b=c>>16&255;return 0xff000000|(int)Math.min(255,b*k)<<16|(int)Math.min(255,g*k)<<8|(int)Math.min(255,r*k);}

    /** One footprint, heel to toe, darker than the floor; a new one with a little dust about it. */
    private static void print(NativeImage img,HallFeed.Print p,boolean fresh){
        var side=new Vec3(-p.heading().z,0,p.heading().x).scale(p.left()?.12:-.12);
        int[] heel=project(p.at().add(side).add(p.heading().scale(-.11)).add(0,.02,0)),toe=project(p.at().add(side).add(p.heading().scale(.13)).add(0,.02,0));
        if(heel==null||toe==null)return;
        int n=Math.max(1,Math.max(Math.abs(toe[0]-heel[0]),Math.abs(toe[1]-heel[1])));boolean wide=n>=3;
        for(int k=0;k<=n;k++){
            int x=heel[0]+(toe[0]-heel[0])*k/n,y=heel[1]+(toe[1]-heel[1])*k/n;
            mark(img,x,y,.4);if(wide)mark(img,x+1,y,.5);
        }
        if(fresh){int cx=(heel[0]+toe[0])/2,cy=(heel[1]+toe[1])/2;for(int a=0;a<12;a++){double t=a*Math.PI/6;mark(img,cx+(int)Math.round(Math.cos(t)*3),cy+(int)Math.round(Math.sin(t)*2),1.25);}}
    }
    private static void mark(NativeImage img,int x,int y,double k){if(x>=0&&y>=0&&x<W&&y<H)img.setPixelRGBA(x,y,scale(img.getPixelRGBA(x,y),k));}
    private static int[] project(Vec3 world){
        var d=world.subtract(seenFrom);var pt=new Vector4f((float)d.x,(float)d.y,(float)d.z,1);seen.transform(pt);
        if(pt.w<=.05F)return null;int x=(int)((pt.x/pt.w+1)*W/2),y=(int)((1-pt.y/pt.w)*H/2);
        return x<-2||y<-2||x>W+2||y>H+2?null:new int[]{x,y};
    }
    private static final String[] DIGITS={"111101101101111","010110010010111","111001111100111","111001111001111","101101111001001","111100111001111","111100111101111","111001001001001","111101111101111","111101111001111"};
    /** A night camera's clock, hours, minutes and seconds, in a three-by-five figure, with a shadow. */
    private static void time(NativeImage img,int x,int y,int seconds){
        int s=Math.floorMod(seconds,86400);String text=String.format("%02d:%02d:%02d",s/3600,s/60%60,s%60);
        for(int pass=0;pass<2;pass++){
            int cx=x;
            for(char c:text.toCharArray()){
                if(c==':'){for(int dy:new int[]{1,3})dot(img,cx+pass,y+dy+pass,pass);cx+=2;continue;}
                var g=DIGITS[c-'0'];for(int i=0;i<15;i++)if(g.charAt(i)=='1')dot(img,cx+i%3+pass,y+i/3+pass,pass);cx+=4;
            }
        }
    }
    private static void dot(NativeImage img,int x,int y,int pass){if(x>=0&&y>=0&&x<W&&y<H)img.setPixelRGBA(x,y,pass==0?scale(img.getPixelRGBA(x,y),.25):grey(226));}

    private static final class Lens extends Camera {
        void aim(Vec3 at,Vec3 towards){
            var mc=Minecraft.getInstance();setup(mc.level,mc.player,false,false,mc.getTimer().getGameTimeDeltaPartialTick(false));var d=towards.subtract(at);setPosition(at);
            setRotation((float)Math.toDegrees(Math.atan2(d.z,d.x))-90,(float)-Math.toDegrees(Math.atan2(d.y,Math.hypot(d.x,d.z))));
        }
    }
    /** Once a second while a hall is watched and the reader is in it: a picture of it from the camera, aimed at them, without them. */
    @SubscribeEvent public static void capture(RenderLevelStageEvent event){
        if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_LEVEL||recording||UncannyCameras.recording()||!watching||frozen||camera==null)return;
        var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null||mc.player.isSpectator()||!mc.player.isAlive())return;
        long now=mc.level.getGameTime();if(now-lastShot<20||mc.player.position().distanceTo(camera)>32)return;
        lastShot=now;
        if(target==null){target=new TextureTarget(W,H,true,Minecraft.ON_OSX);target.setClearColor(.02F,.02F,.02F,1);lens=new Lens();}
        lens.aim(camera,mc.player.position().add(0,.5,0));
        var oldProjection=new Matrix4f(RenderSystem.getProjectionMatrix());var sorting=RenderSystem.getVertexSorting();var main=mc.gameRenderer.getMainCamera();
        var oldView=new Matrix4f().rotation(main.rotation().conjugate(new Quaternionf()));var view=new Matrix4f().rotation(lens.rotation().conjugate(new Quaternionf()));
        var projection=new Matrix4f().setPerspective((float)Math.toRadians(70),(float)W/H,.05F,96);
        var modelView=RenderSystem.getModelViewStack();modelView.pushMatrix();modelView.identity();RenderSystem.applyModelViewMatrix();
        recording=true;
        try{
            target.clear(Minecraft.ON_OSX);target.bindWrite(true);RenderSystem.setProjectionMatrix(projection,VertexSorting.DISTANCE_TO_ORIGIN);
            mc.levelRenderer.prepareCullFrustum(lens.getPosition(),view,projection);
            mc.levelRenderer.renderLevel(mc.getTimer(),false,lens,mc.gameRenderer,mc.gameRenderer.lightTexture(),view,projection);
            try(var shot=Screenshot.takeScreenshot(target)){
                var grey=new int[W*H];
                for(int y=0;y<H;y++)for(int x=0;x<W;x++){
                    int c=shot.getPixelRGBA(x,y);double l=(.3*(c&255)+.59*(c>>8&255)+.11*(c>>16&255))/255;
                    double v=Math.pow(l,.55);v=Math.max(0,Math.min(1,(v-.03)*1.12));grey[y*W+x]=(int)(v*230);
                }
                still=grey;seen=new Matrix4f(projection).mul(view);seenFrom=lens.getPosition();shots++;
            }
        }finally{
            recording=false;modelView.popMatrix();RenderSystem.applyModelViewMatrix();mc.getMainRenderTarget().bindWrite(true);RenderSystem.setProjectionMatrix(oldProjection,sorting);
            mc.levelRenderer.prepareCullFrustum(main.getPosition(),oldView,oldProjection);
            mc.getEntityRenderDispatcher().prepare(mc.level,main,mc.crosshairPickEntity);
            RenderSystem.viewport(0,0,mc.getWindow().getWidth(),mc.getWindow().getHeight());
        }
    }

    /** The monitor on its bracket, and, while it is lit, the feed on its screen. */
    @SubscribeEvent public static void draw(RenderLevelStageEvent event){
        if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_ENTITIES||monitor==null||recording||UncannyCameras.recording())return;
        var mc=Minecraft.getInstance();if(mc.level==null)return;
        var eye=event.getCamera().getPosition();var buffers=mc.renderBuffers().bufferSource();
        var pose=event.getPoseStack();pose.pushPose();pose.translate(monitor.getX()-eye.x,monitor.getY()-.3-eye.y,monitor.getZ()-eye.z);
        draw(pose,buffers,LevelRenderer.getLightColor(mc.level,monitor),facing,screenOn?screenId:null);
        pose.popPose();buffers.endBatch();
    }
    /** The monitor in a block's space with its back to the wall behind {@code facing}; the screen too when {@code feed} is given. */
    public static void draw(PoseStack pose,net.minecraft.client.renderer.MultiBufferSource buffers,int light,Direction facing,ResourceLocation feed){
        var mc=Minecraft.getInstance();
        pose.pushPose();pose.translate(.5,0,.5);pose.mulPose(Axis.YP.rotationDegrees(180-facing.toYRot()));pose.translate(-.5,0,-.5);
        mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(),buffers.getBuffer(Sheets.cutoutBlockSheet()),null,mc.getModelManager().getModel(MODEL),1F,1F,1F,light,OverlayTexture.NO_OVERLAY,ModelData.EMPTY,null);
        if(feed!=null){
            VertexConsumer out=buffers.getBuffer(RenderType.text(feed));float z=1/16F-.002F,x0=4/16F,x1=12/16F,y0=7/16F,y1=12/16F;int tint=0xffdfe8dc;
            // Wound anticlockwise as seen from in front of the screen (from -z): the text type culls the other side.
            out.addVertex(pose.last(),x1,y1,z).setColor(tint).setUv(0,0).setLight(LightTexture.FULL_BRIGHT);
            out.addVertex(pose.last(),x1,y0,z).setColor(tint).setUv(0,1).setLight(LightTexture.FULL_BRIGHT);
            out.addVertex(pose.last(),x0,y0,z).setColor(tint).setUv(1,1).setLight(LightTexture.FULL_BRIGHT);
            out.addVertex(pose.last(),x0,y1,z).setColor(tint).setUv(1,0).setLight(LightTexture.FULL_BRIGHT);
        }
        pose.popPose();
    }
}
