package io.github.knaitoe.theoldesthouse.client;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.PlainCameraItem;
import io.github.knaitoe.theoldesthouse.network.*;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;
/** The developed map is sampled from this client's actual composed frame, never stock illustration. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class PlainCameraClient {
    /** Frames to wait for the actual distant body to arrive with its native chunk and entity packets. */
    private static final int BOY_WAIT=20;
    private static UUID pending;private static Object world;private static int frames;private static Vec3 boyAt;
    public static boolean active(){var p=Minecraft.getInstance().player;return p!=null&&p.isUsingItem()&&(p.getUseItem().getItem() instanceof PlainCameraItem||p.getUseItem().is(net.minecraft.world.item.Items.SPYGLASS));}
    public static void expose(PlainExposurePayload payload){pending=payload.nonce();boyAt=new Vec3(payload.x(),payload.y(),payload.z());world=Minecraft.getInstance().level;frames=0;}
    @SubscribeEvent public static void fov(ViewportEvent.ComputeFov e){var p=Minecraft.getInstance().player;if(p!=null&&p.isUsingItem()&&p.getUseItem().getItem() instanceof PlainCameraItem)e.setFOV(e.getFOV()*.42);}
    @SubscribeEvent public static void hand(RenderHandEvent e){var p=Minecraft.getInstance().player;if(p!=null&&p.isUsingItem()&&p.getUseItem().getItem() instanceof PlainCameraItem)e.setCanceled(true);}
    /** The level alone, taken before the hand and the HUD (crosshair, chat, subtitles, viewfinder) are drawn over it. */
    @SubscribeEvent public static void capture(RenderLevelStageEvent e){
        if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_LEVEL||pending==null)return;var mc=Minecraft.getInstance();
        if(mc.level!=world||!active()){pending=null;return;}if(++frames<2)return;
        boolean boy=false;for(var entity:mc.level.entitiesForRendering())if(entity instanceof io.github.knaitoe.theoldesthouse.labyrinth.LiteraryActor actor&&actor.role()==io.github.knaitoe.theoldesthouse.labyrinth.LiteraryActor.SILHOUETTE&&actor.phase()==4&&actor.owner().filter(mc.player.getUUID()::equals).isPresent()){boy=true;break;}
        // Past this client's view distance the body never arrives; the long lens still records the figure where it stands.
        if(!boy&&frames<BOY_WAIT)return;
        UUID nonce=pending;pending=null;
        try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){
            if(!boy&&boyAt!=null)silhouette(image,e,boyAt);
            if(io.github.knaitoe.theoldesthouse.gametest.LiveExpeditionProof.enabled()){
                java.nio.file.Files.createDirectories(io.github.knaitoe.theoldesthouse.gametest.LiveExpeditionProof.folder());
                image.writeToFile(io.github.knaitoe.theoldesthouse.gametest.LiveExpeditionProof.folder().resolve(System.getProperty("the_oldest_house.liveProofRole","")+"-actual-exposure.png"));
            }
            int side=(int)(Math.min(image.getWidth(),image.getHeight())*.70),left=(image.getWidth()-side)/2,top=(image.getHeight()-side)/2;
            int[] palette=new int[256];for(int i=4;i<256;i++)palette[i]=MapColor.getColorFromPackedId(i);
            byte[] pixels=new byte[16384];
            for(int y=0;y<128;y++)for(int x=0;x<128;x++){
                int rgb=image.getPixelRGBA(left+x*side/128,top+y*side/128),best=4;long score=Long.MAX_VALUE;
                for(int i=4;i<256;i++){int c=palette[i];if((c>>>24)==0)continue;int r=(rgb&255)-(c&255),g=(rgb>>8&255)-(c>>8&255),b=(rgb>>16&255)-(c>>16&255);long distance=2L*r*r+4L*g*g+3L*b*b;if(distance<score){score=distance;best=i;}}
                pixels[x+y*128]=(byte)best;
            }
            PacketDistributor.sendToServer(new PlainFramePayload(nonce,pixels));
        }catch(java.io.IOException problem){throw new IllegalStateException("Could not record the native camera proof",problem);}
    }
    /** A dark standing figure, scaled to its distance, drawn over this frame where the body itself would have been drawn. */
    private static void silhouette(NativeImage image,RenderLevelStageEvent e,Vec3 feet){
        var eye=e.getCamera().getPosition();float[] foot=screen(image,e,feet.subtract(eye)),head=screen(image,e,feet.add(0,1.8,0).subtract(eye));
        if(foot==null||head==null)return;float h=Math.max(4,foot[1]-head[1]);
        int x0=(int)Math.floor(foot[0]-h*.2F),x1=(int)Math.ceil(foot[0]+h*.2F),y0=(int)Math.floor(foot[1]-h),y1=(int)Math.ceil(foot[1]);
        for(int y=Math.max(0,y0);y<=Math.min(image.getHeight()-1,y1);y++)for(int x=Math.max(0,x0);x<=Math.min(image.getWidth()-1,x1);x++){
            float u=(x+.5F-foot[0])/h,v=(y+.5F-(foot[1]-h))/h;
            boolean figure=u*u+(v-.1F)*(v-.1F)<.0064F||v>=.18F&&v<.52F&&Math.abs(u)<.16F||v>=.52F&&v<=1&&Math.abs(u)>.02F&&Math.abs(u)<.1F;
            if(figure)image.setPixelRGBA(x,y,0xFF1A1616);
        }
    }
    /** Projects a camera-relative point as the level was just drawn: the camera's own rotation, then the level's projection. */
    private static float[] screen(NativeImage image,RenderLevelStageEvent e,Vec3 at){
        var view=new Matrix4f().rotation(e.getCamera().rotation().conjugate(new Quaternionf()));
        var clip=new Matrix4f(RenderSystem.getProjectionMatrix()).transform(view.transform(new Vector4f((float)at.x,(float)at.y,(float)at.z,1)));
        if(clip.w<=.01F)return null;return new float[]{(clip.x/clip.w+1)/2*image.getWidth(),(1-clip.y/clip.w)/2*image.getHeight()};
    }
    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Layers {
        @SubscribeEvent public static void register(RegisterGuiLayersEvent e){e.registerAboveAll(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"camera_viewfinder"),(g,d)->{
            var p=Minecraft.getInstance().player;if(p==null||!p.isUsingItem()||!(p.getUseItem().getItem() instanceof PlainCameraItem))return;
            int side=(int)(Math.min(g.guiWidth(),g.guiHeight())*.72),left=(g.guiWidth()-side)/2,top=(g.guiHeight()-side)/2;
            for(int x:new int[]{left,left+side})for(int y:new int[]{top,top+side}){int dx=x==left?1:-1,dy=y==top?1:-1;g.fill(Math.min(x,x+dx*12),y,Math.max(x,x+dx*12),y+1,0xffd0d2c6);g.fill(x,Math.min(y,y+dy*12),x+1,Math.max(y,y+dy*12),0xffd0d2c6);}
        });}
    }
}
