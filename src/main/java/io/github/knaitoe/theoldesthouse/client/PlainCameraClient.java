package io.github.knaitoe.theoldesthouse.client;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.PlainCameraItem;
import io.github.knaitoe.theoldesthouse.network.*;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
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
        // Past this client's view distance the body never arrives; the long lens records the same crouched mesh at its feet.
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
    /** Project the production mesh so the long lens cannot turn an untracked crouched child into a standing adult. */
    private static void silhouette(NativeImage image,RenderLevelStageEvent e,Vec3 feet){
        var view=new Matrix4f().rotation(e.getCamera().rotation().conjugate(new Quaternionf()));
        silhouette(image,new Matrix4f(RenderSystem.getProjectionMatrix()).mul(view),feet.subtract(e.getCamera().getPosition()));
    }
    static void silhouette(NativeImage image,Matrix4f projection,Vec3 feet){
        var model=new PlainFigureModel(Minecraft.getInstance().getEntityModels().bakeLayer(PlainFigureModel.LAYER));
        model.renderToBuffer(PlainFigureModel.atFeet(feet),new Silhouette(image,projection),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,-1);
    }
    private static final class Silhouette implements VertexConsumer {
        private final NativeImage image;private final Matrix4f projection;private final float[][] quad=new float[4][];private int next;
        Silhouette(NativeImage image,Matrix4f projection){this.image=image;this.projection=projection;}
        @Override public VertexConsumer addVertex(float x,float y,float z){
            var clip=projection.transform(new Vector4f(x,y,z,1));
            quad[next++]=clip.w<=.01F?null:new float[]{(clip.x/clip.w+1)*image.getWidth()/2,(1-clip.y/clip.w)*image.getHeight()/2};
            if(next==4){next=0;if(quad[0]!=null&&quad[1]!=null&&quad[2]!=null&&quad[3]!=null){triangle(quad[0],quad[1],quad[2]);triangle(quad[0],quad[2],quad[3]);}}
            return this;
        }
        private static float edge(float[] a,float[] b,float x,float y){return (x-a[0])*(b[1]-a[1])-(y-a[1])*(b[0]-a[0]);}
        private void triangle(float[] a,float[] b,float[] c){
            float area=edge(a,b,c[0],c[1]);if(Math.abs(area)<.00001F)return;float sign=area<0?-1:1;
            int x0=Math.max(0,(int)Math.floor(Math.min(a[0],Math.min(b[0],c[0])))),x1=Math.min(image.getWidth()-1,(int)Math.ceil(Math.max(a[0],Math.max(b[0],c[0]))));
            int y0=Math.max(0,(int)Math.floor(Math.min(a[1],Math.min(b[1],c[1])))),y1=Math.min(image.getHeight()-1,(int)Math.ceil(Math.max(a[1],Math.max(b[1],c[1]))));
            for(int y=y0;y<=y1;y++)for(int x=x0;x<=x1;x++)if(edge(a,b,x+.5F,y+.5F)*sign>=0&&edge(b,c,x+.5F,y+.5F)*sign>=0&&edge(c,a,x+.5F,y+.5F)*sign>=0)image.setPixelRGBA(x,y,0xFF1A1616);
        }
        @Override public VertexConsumer setColor(int r,int g,int b,int a){return this;}
        @Override public VertexConsumer setUv(float u,float v){return this;}
        @Override public VertexConsumer setUv1(int u,int v){return this;}
        @Override public VertexConsumer setUv2(int u,int v){return this;}
        @Override public VertexConsumer setNormal(float x,float y,float z){return this;}
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
