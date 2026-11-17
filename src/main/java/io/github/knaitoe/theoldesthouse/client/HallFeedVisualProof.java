package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.HallFeed;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import org.joml.Matrix4f;
import org.joml.Vector4f;

/**
 * 0.4.662, the feed: the monitor through its production model and the feed through its production compositor, laid over the model's
 * screen as the game lays it. A hall drawn for the purpose from a camera high above its door stands in for the picture a reader's game
 * takes. Four moments (the empty hall, the first footprints, all of them, breaking up) on the monitor from the front, from one side,
 * from below where a reader stands, and the feed flat beside them.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class HallFeedVisualProof extends Screen {
    private static final Vec3 FROM=new Vec3(0,2.6,0);
    private int frames;
    private static boolean launched;
    private final Matrix4f camera=camera();
    private final int[] picture=hall(camera);
    private final List<HallFeed.Print> walk=walk();
    public HallFeedVisualProof(){super(Component.literal("The feed"));}

    private static Matrix4f camera(){
        var projection=new Matrix4f().setPerspective((float)Math.toRadians(70),(float)HallFeedClient.W/HallFeedClient.H,.05F,96);
        return projection.mul(new Matrix4f().lookAt(0,0,0,0,-2.4F,7,0,1,0));
    }
    /** A hall three wide and three high, boards underfoot, a door at its far end, as a night camera would see it from over the near door. */
    private static int[] hall(Matrix4f camera){
        var inverse=new Matrix4f(camera).invert();var out=new int[HallFeedClient.W*HallFeedClient.H];
        for(int y=0;y<HallFeedClient.H;y++)for(int x=0;x<HallFeedClient.W;x++){
            var near=new Vector4f(x*2F/HallFeedClient.W-1,1-y*2F/HallFeedClient.H,-1,1);inverse.transform(near);near.div(near.w);
            var far=new Vector4f(x*2F/HallFeedClient.W-1,1-y*2F/HallFeedClient.H,1,1);inverse.transform(far);far.div(far.w);
            var d=new Vec3(far.x-near.x,far.y-near.y,far.z-near.z).normalize();var o=FROM;
            double best=1e9;int v=10;
            if(d.y<0){double t=-o.y/d.y;var p=o.add(d.scale(t));if(Math.abs(p.x)<=1.5&&p.z<=14&&t<best){best=t;v=(int)(150-p.z*5)+((int)Math.floor(p.x*2.6+7)%2==0?-10:6);}}
            if(d.y>0){double t=(3-o.y)/d.y;var p=o.add(d.scale(t));if(Math.abs(p.x)<=1.5&&p.z<=14&&t<best){best=t;v=(int)(60-p.z*2);}}
            for(int s=-1;s<=1;s+=2)if(d.x*s>0){double t=(1.5*s-o.x)/d.x;var p=o.add(d.scale(t));if(p.y>=0&&p.y<=3&&p.z<=14&&t<best){best=t;v=(int)(115-p.z*4)-(p.y<.3?25:0);}}
            if(d.z>0){double t=(14-o.z)/d.z;var p=o.add(d.scale(t));if(Math.abs(p.x)<=1.5&&p.y>=0&&p.y<=3&&t<best){best=t;v=Math.abs(p.x)<.55&&p.y<2.1?18:70;}}
            out[y*HallFeedClient.W+x]=Math.max(0,Math.min(230,v));
        }
        return out;
    }
    /** The way walked: down the middle of the hall to its far door. */
    private static List<HallFeed.Print> walk(){
        var marks=new ArrayList<HallFeed.Mark>();for(int i=0;i<=30;i++)marks.add(new HallFeed.Mark(new Vec3(i*.04-.5,0,1.5+i*.4),SoundEvents.WOOD_STEP,1,1));
        return HallFeed.walked(marks,HallFeed.STRIDE,HallFeed.LENGTH);
    }

    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xFF1E1D1B);
        g.drawString(font,"The feed / the monitor's production model, the feed's production compositor",12,10,0xFFE8DFCF,false);g.flush();
        int[] moments={0,3,walk.size(),-1};String[] names={"empty","first steps","all steps","breaking"};float[][] turns={{0,8},{35,8},{-35,8},{0,-28}};
        var buffers=mc.renderBuffers().bufferSource();
        for(int i=0;i<4;i++){
            var feed=HallFeedClient.proofFrame(picture,camera,FROM,walk,moments[i]<0?walk.size():moments[i],moments[i]<0);
            float xx=width*(i+.5F)/4,yy=height*.36F;var pose=g.pose();
            pose.pushPose();pose.translate(xx,yy,150);pose.scale(110,-110,110);pose.mulPose(Axis.XP.rotationDegrees(turns[i][1]));pose.mulPose(Axis.YP.rotationDegrees(180+turns[i][0]));pose.translate(-.5,-.5,-.5);
            HallFeedClient.draw(pose,buffers,LightTexture.pack(9,0),Direction.NORTH,feed);buffers.endBatch();pose.popPose();
            g.drawString(font,names[i],(int)xx-20,(int)(height*.62F),0xFFE8DFCF,false);
            if(i==2)g.blit(feed,(int)(width*.5F)-HallFeedClient.W,(int)(height*.66F),0,0,HallFeedClient.W*2,HallFeedClient.H*2/3,HallFeedClient.W*2,HallFeedClient.H*2/3);
        }
    }

    /** Opened alone, without the rest of the client proof, with -PhallFeedSmoke. */
    @SubscribeEvent public static void launch(net.neoforged.neoforge.client.event.ClientTickEvent.Post ignored){
        var mc=Minecraft.getInstance();
        if(Boolean.getBoolean("the_oldest_house.hallFeedSmoke")&&!launched&&mc.screen!=null&&mc.getOverlay()==null){launched=true;mc.options.guiScale().set(2);mc.resizeDisplay();mc.setScreen(new HallFeedVisualProof());}
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();boolean direct=Boolean.getBoolean("the_oldest_house.hallFeedSmoke");
        if(!(direct||Boolean.getBoolean("the_oldest_house.fontSmoke"))||!(mc.screen instanceof HallFeedVisualProof p)||++p.frames<12)return;
        if(mc.getModelManager().getModel(HallFeedClient.MODEL)==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing the feed's monitor model");
        if(p.walk.size()<5)throw new IllegalStateException("The walk down the hall gave "+p.walk.size()+" footprints");
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);
        try(NativeImage im=Screenshot.takeScreenshot(mc.getMainRenderTarget())){im.writeToFile(dir.resolve("native-hall-feed.png"));}
        // Each footprint in the picture darkens it where it falls; none is there before its time.
        HallFeedClient.proofFrame(p.picture,p.camera,FROM,p.walk,0,false);var empty=copy(HallFeedClient.screenPixels());
        HallFeedClient.proofFrame(p.picture,p.camera,FROM,p.walk,p.walk.size(),false);var walked=copy(HallFeedClient.screenPixels());
        walked.writeToFile(dir.resolve("native-hall-feed-screen.png"));
        int darker=0,inView=0;var report=new StringBuilder();
        for(var print:p.walk){
            var side=new Vec3(-print.heading().z,0,print.heading().x).scale(print.left()?.12:-.12);
            var at=HallFeedClient.proofProject(p.camera,FROM,print.at().add(side).add(print.heading().scale(.01)).add(0,.02,0));
            if(at==null||at[0]<2||at[1]<2||at[0]>=HallFeedClient.W-2||at[1]>=HallFeedClient.H-2){report.append(" out:").append(print.at());continue;}
            // Pixels about it a print has darkened by more than a quarter (beyond the noise between frames), where there was light to darken.
            inView++;int dark=0,lit=0;
            for(int dy=-2;dy<=2;dy++)for(int dx=-2;dx<=2;dx++){int was=lum(empty.getPixelRGBA(at[0]+dx,at[1]+dy)),now=lum(walked.getPixelRGBA(at[0]+dx,at[1]+dy));if(was>=30){lit++;if(now<was*3/4-6)dark++;}}
            if(dark>=2||lit<4)darker++;report.append(" (").append(at[0]).append(',').append(at[1]).append(' ').append(dark).append('/').append(lit).append(')');
        }
        empty.close();
        if(inView<p.walk.size()-2||darker<inView)throw new IllegalStateException(darker+" of "+inView+" footprints in view show on the feed ("+p.walk.size()+" in all):"+report);
        // The clock is drawn: its figures stand out in the bottom left.
        int bright=0;for(int y=HallFeedClient.H-8;y<HallFeedClient.H-3;y++)for(int x=4;x<36;x++)if(lum(walked.getPixelRGBA(x,y))>180)bright++;
        walked.close();
        if(bright<20)throw new IllegalStateException("The feed's clock is missing ("+bright+" lit pixels)");
        Files.writeString(dir.resolve("hall-feed-passed.txt"),"The feed's monitor through its production model and the feed through its production compositor: "+darker+" of the "+inView+" footprints in view darken the picture where they fall; the clock is lit.\n");
        TheOldestHouse.LOGGER.info("HALL FEED CHECK PASSED: monitor model, "+darker+" footprints, clock");if(direct)mc.stop();else mc.setScreen(new SceneVisualProof());
    }
    private static NativeImage copy(NativeImage from){var out=new NativeImage(from.getWidth(),from.getHeight(),false);out.copyFrom(from);return out;}
    private static int lum(int c){return ((c&255)*30+(c>>8&255)*59+(c>>16&255)*11)/100;}
}
