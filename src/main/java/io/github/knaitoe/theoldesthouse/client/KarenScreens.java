package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.opening.KarenClientConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/**
 * What Karen's projector shows (0.4.121): the reader's own F2 screenshots, read from this computer's screenshots folder,
 * and, among them, small pictures of the reader's screen that this client took now and then without saying so, kept in
 * the game folder under the_oldest_house/karen. Everything here is read, scaled and drawn on this computer only: no image,
 * file name or count is ever sent to the server, which only says where the wall is. Both halves can be turned off in the
 * client settings.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class KarenScreens {
    /** Projected slides, and the secret snapshots as they are saved. */
    static final int WIDTH=256,HEIGHT=144,SNAP_WIDTH=160,SNAP_HEIGHT=90;
    /** At most this many of each kind on the wall; at most this many snapshots kept on disk. */
    static final int SHOW=10,KEEP=24;
    /** Six seconds to a slide; a snapshot every five to ten minutes of play. */
    static final int SLIDE=120,SNAP_EVERY=6000;
    private static final RandomSource RANDOM=RandomSource.create();
    private static final List<ResourceLocation> slides=new ArrayList<>();
    private static boolean loading,snapPending;
    private static int slide,slideTicks,lastMap=Integer.MIN_VALUE,untilSnap=-1,generation;
    private KarenScreens(){}

    static Path snapshots(){return Minecraft.getInstance().gameDirectory.toPath().resolve("the_oldest_house").resolve("karen");}
    static Path screenshots(){return Minecraft.getInstance().gameDirectory.toPath().resolve("screenshots");}

    /** Whether the wall is showing one of the reader's own pictures instead of a photograph from the House. */
    public static boolean showing(){return PersonalProjectionClient.wall()!=null&&current()!=null;}
    private static ResourceLocation current(){
        if(slides.isEmpty())return null;
        // A House photograph, when there is one, takes its turn after every third picture.
        boolean photo=PersonalProjectionClient.map()>=0;int cycle=photo?slides.size()+(slides.size()+2)/3:slides.size();int at=Math.floorMod(slide,cycle);
        if(!photo)return slides.get(at);
        int group=at/4,inGroup=at%4;if(inGroup==3)return null;int index=group*3+inGroup;return index<slides.size()?slides.get(index):null;
    }

    /** The newest PNG files in a folder, newest first; none when the folder is missing or unreadable. */
    static List<Path> newest(Path folder,int count){
        if(!Files.isDirectory(folder))return List.of();
        try(Stream<Path> files=Files.list(folder)){
            return files.filter(f->f.getFileName().toString().toLowerCase(java.util.Locale.ROOT).endsWith(".png")&&Files.isRegularFile(f))
                .sorted(Comparator.comparingLong(KarenScreens::modified).reversed()).limit(count).toList();
        }catch(IOException|RuntimeException e){return List.of();}
    }
    private static long modified(Path f){try{return Files.getLastModifiedTime(f).toMillis();}catch(IOException e){return 0;}}

    /** Newest screenshot, newest snapshot, the next of each, and so on. */
    static List<Path> order(List<Path> shots,List<Path> snaps){
        var out=new ArrayList<Path>();for(int i=0;i<Math.max(shots.size(),snaps.size());i++){if(i<shots.size())out.add(shots.get(i));if(i<snaps.size())out.add(snaps.get(i));}return out;
    }

    /** A picture cut to the wall's shape from its middle and scaled down; null when it cannot be read. */
    static NativeImage fit(Path file){
        try(var in=Files.newInputStream(file);var image=NativeImage.read(NativeImage.Format.RGBA,in)){
            int w=image.getWidth(),h=image.getHeight();if(w<16||h<9)return null;
            int cw=Math.min(w,h*16/9),ch=Math.min(h,w*9/16);
            var small=new NativeImage(NativeImage.Format.RGBA,WIDTH,HEIGHT,false);
            image.resizeSubRectTo((w-cw)/2,(h-ch)/2,cw,ch,small);return small;
        }catch(IOException|RuntimeException e){return null;}
    }

    private static void load(){
        var mc=Minecraft.getInstance();loading=true;int ticket=++generation;
        boolean shots=KarenClientConfig.SCREENSHOTS.get(),snaps=KarenClientConfig.SNAPSHOTS.get();
        var shotFolder=screenshots();var snapFolder=snapshots();
        Util.backgroundExecutor().execute(()->{
            var images=new ArrayList<NativeImage>();
            for(var file:order(shots?newest(shotFolder,SHOW):List.of(),snaps?newest(snapFolder,SHOW):List.of())){var image=fit(file);if(image!=null)images.add(image);}
            mc.execute(()->{
                if(ticket!=generation||PersonalProjectionClient.wall()==null){images.forEach(NativeImage::close);loading=false;return;}
                for(var image:images)slides.add(mc.getTextureManager().register("oth_karen_slide",new DynamicTexture(image)));
                loading=false;slide=0;slideTicks=0;
            });
        });
    }
    private static void release(){
        var mc=Minecraft.getInstance();for(var id:slides)mc.getTextureManager().release(id);slides.clear();generation++;loading=false;slide=0;slideTicks=0;lastMap=Integer.MIN_VALUE;
    }

    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        var mc=Minecraft.getInstance();
        var wall=PersonalProjectionClient.wall();
        if(wall==null){if(!slides.isEmpty()||loading)release();lastMap=Integer.MIN_VALUE;}
        else{
            if(slides.isEmpty()&&!loading&&lastMap==Integer.MIN_VALUE&&(KarenClientConfig.SCREENSHOTS.get()||KarenClientConfig.SNAPSHOTS.get()))load();
            // The projector's own button (a new House photograph) moves the slides on as well.
            int map=PersonalProjectionClient.map();if(lastMap!=Integer.MIN_VALUE&&map!=lastMap){slide++;slideTicks=0;}lastMap=map;
            if(!slides.isEmpty()&&++slideTicks>=SLIDE){slideTicks=0;slide++;if(mc.level!=null&&mc.player!=null)mc.level.playLocalSound(wall.getX()+.5,wall.getY(),wall.getZ()+1.5,SoundEvents.LEVER_CLICK,SoundSource.BLOCKS,.12F,1.6F,false);}
        }
        // The secret snapshots: now and then, while playing with no screen open.
        if(!KarenClientConfig.SNAPSHOTS.get()||mc.player==null||mc.level==null||mc.screen!=null||mc.isPaused()){return;}
        if(untilSnap<0)untilSnap=SNAP_EVERY+RANDOM.nextInt(SNAP_EVERY);
        if(--untilSnap<=0){untilSnap=-1;snapPending=true;}
    }

    @SubscribeEvent public static void frame(RenderFrameEvent.Post e){
        if(!snapPending)return;snapPending=false;var mc=Minecraft.getInstance();if(mc.player==null||mc.screen!=null)return;
        NativeImage small;
        try(var full=Screenshot.takeScreenshot(mc.getMainRenderTarget())){
            int w=full.getWidth(),h=full.getHeight();if(w<16||h<9)return;int cw=Math.min(w,h*16/9),ch=Math.min(h,w*9/16);
            small=new NativeImage(NativeImage.Format.RGBA,SNAP_WIDTH,SNAP_HEIGHT,false);full.resizeSubRectTo((w-cw)/2,(h-ch)/2,cw,ch,small);
        }
        var folder=snapshots();var name="snap-"+LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))+".png";
        Util.ioPool().execute(()->{
            try{Files.createDirectories(folder);small.writeToFile(folder.resolve(name));
                var all=newest(folder,Integer.MAX_VALUE);for(int i=KEEP;i<all.size();i++)Files.deleteIfExists(all.get(i));
            }catch(IOException|RuntimeException ignored){}finally{small.close();}
        });
    }

    @SubscribeEvent public static void render(RenderLevelStageEvent e){
        if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS)return;var wall=PersonalProjectionClient.wall();var id=wall==null?null:current();if(id==null)return;
        var mc=Minecraft.getInstance();var camera=e.getCamera().getPosition();if(camera.distanceToSqr(wall.getCenter())>400)return;
        var pose=e.getPoseStack();pose.pushPose();
        // The white wall's face: seven blocks wide and three high, centred on its middle block; the picture keeps 16:9.
        pose.translate(wall.getX()+.5-camera.x,wall.getY()+.5-camera.y,wall.getZ()+1.004-camera.z);
        float hw=2.58F,hh=1.45F;var last=pose.last();var buffer=mc.renderBuffers().bufferSource().getBuffer(RenderType.entityCutoutNoCull(id));
        int light=LightTexture.FULL_BRIGHT,overlay=OverlayTexture.NO_OVERLAY,c=232;
        buffer.addVertex(last,-hw,-hh,0).setColor(c,c,c-6,255).setUv(0,1).setOverlay(overlay).setLight(light).setNormal(last,0,0,1);
        buffer.addVertex(last,hw,-hh,0).setColor(c,c,c-6,255).setUv(1,1).setOverlay(overlay).setLight(light).setNormal(last,0,0,1);
        buffer.addVertex(last,hw,hh,0).setColor(c,c,c-6,255).setUv(1,0).setOverlay(overlay).setLight(light).setNormal(last,0,0,1);
        buffer.addVertex(last,-hw,hh,0).setColor(c,c,c-6,255).setUv(0,0).setOverlay(overlay).setLight(light).setNormal(last,0,0,1);
        pose.popPose();mc.renderBuffers().bufferSource().endBatch();
    }
    /**
     * For the native client proof: a full-size screenshot, an older one and a small snapshot in a scratch folder come back
     * newest screenshot first, then the snapshot, then the older screenshot, each cut and scaled to the wall's 256 by 144.
     */
    static void check(Path dir)throws IOException{
        var shots=dir.resolve("screenshots");var snaps=dir.resolve("karen");Files.createDirectories(shots);Files.createDirectories(snaps);
        try(var big=new NativeImage(1920,1080,false)){big.fillRect(0,0,1920,1080,0xff6a4a2a);big.writeToFile(shots.resolve("2026-10-24_20.00.00.png"));}
        try(var old=new NativeImage(1280,1024,false)){old.fillRect(0,0,1280,1024,0xff304050);old.fillRect(0,0,1280,100,0xff101010);old.fillRect(0,924,1280,100,0xff101010);old.writeToFile(shots.resolve("2026-10-01_09.00.00.png"));}
        try(var snap=new NativeImage(SNAP_WIDTH,SNAP_HEIGHT,false)){snap.fillRect(0,0,SNAP_WIDTH,SNAP_HEIGHT,0xff203020);snap.writeToFile(snaps.resolve("snap-20261024-120000.png"));}
        var now=System.currentTimeMillis();
        Files.setLastModifiedTime(shots.resolve("2026-10-24_20.00.00.png"),java.nio.file.attribute.FileTime.fromMillis(now));
        Files.setLastModifiedTime(shots.resolve("2026-10-01_09.00.00.png"),java.nio.file.attribute.FileTime.fromMillis(now-86_400_000L));
        Files.writeString(shots.resolve("notes.txt"),"not a picture");
        var order=order(newest(shots,SHOW),newest(snaps,SHOW));
        if(order.size()!=3||!order.get(0).getFileName().toString().startsWith("2026-10-24")||!order.get(1).getFileName().toString().startsWith("snap-")||!order.get(2).getFileName().toString().startsWith("2026-10-01"))throw new IllegalStateException("Karen's slides are out of order: "+order);
        for(var file:order)try(var fitted=fit(file)){if(fitted==null||fitted.getWidth()!=WIDTH||fitted.getHeight()!=HEIGHT)throw new IllegalStateException("Karen's slide was not cut to the wall: "+file);
            // The squarer old screenshot is cut from its middle, so the bands at its top and bottom are left out, not squeezed in.
            if(file==order.get(2)&&((fitted.getPixelRGBA(128,0)&0xffffff)==0x101010||(fitted.getPixelRGBA(128,HEIGHT-1)&0xffffff)==0x101010))throw new IllegalStateException("Karen's slide kept the screenshot's cut-away edge");}
        if(fit(shots.resolve("notes.txt"))!=null||!newest(dir.resolve("missing"),SHOW).isEmpty())throw new IllegalStateException("Karen's slides read something that is not a picture");
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){release();snapPending=false;untilSnap=-1;}
}
