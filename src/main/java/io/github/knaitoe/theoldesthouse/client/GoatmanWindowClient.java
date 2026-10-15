package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.gametest.LiveExpeditionProof;
import io.github.knaitoe.theoldesthouse.gametest.GoatmanWindowViews;
import io.github.knaitoe.theoldesthouse.labyrinth.GoatmanChild;
import io.github.knaitoe.theoldesthouse.labyrinth.GoatmanStalking;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

/** Opt-in connected window/audio proof; ordinary clients never call it. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class GoatmanWindowClient {
    private static final Set<Boolean> forms=new HashSet<>();
    private static final Set<Integer> childFaces=new HashSet<>();
    private static final Set<Boolean> panes=new HashSet<>();
    private static UUID original;
    private static SimpleSoundInstance vanilla;
    private static long probeAt;
    private static int aiming;
    private static boolean audioPassed,departed,middleView;
    private static GoatmanChild pendingBody;private static Vec3 pendingBase;private static String pendingName;private static boolean pendingForm,drawn,maskBody;private static int pendingSkin,pixelStage,visiblePixels;private static double pendingZ;private static int[] facePixels;
    private GoatmanWindowClient(){}
    static boolean ready(){return forms.size()==2&&childFaces.size()>=2&&panes.size()==2&&audioPassed;}
    static String tick(Minecraft mc,List<GoatmanChild> children,BlockPos base,String role,boolean capture){
        long now=mc.level.getGameTime();
        if(vanilla==null&&GoatmanScore.playing()){
            vanilla=SimpleSoundInstance.forMusic(SoundEvents.MUSIC_GAME.value());probeAt=now;
            mc.getSoundManager().play(vanilla);
        }
        if(!audioPassed&&vanilla!=null&&now-probeAt>=10){
            if(mc.getSoundManager().isActive(vanilla)||!GoatmanScore.playing())throw new IllegalStateException("LIVE EXPEDITION vanilla music overlaps the trailer score or the original score is missing");
            audioPassed=true;write(role+"-trailer-score.txt","Original Goatman score active in the actual sound engine. A real vanilla MUSIC_GAME play request was suppressed; it remained inactive while the scene score played.\n");
        }
        // Keep the requested native view until the rendered frame is captured, even when a later client tick precedes it.
        if(pendingName!=null){aim(mc,pendingBody);return pendingName;}
        if(!capture)return null;
        // Walk to the clear side of the actual bunks; the centre aisle's watching cousin and peer can cover this window.
        boolean middle=middleView;double window=GoatmanWindowViews.window(middle);var goal=GoatmanWindowViews.feet(base,role,middle);var walk=goal.subtract(mc.player.position());
        if(walk.horizontalDistanceSqr()>.04){mc.player.setYRot((float)Math.toDegrees(Math.atan2(-walk.x,walk.z)));mc.player.setXRot(0);mc.options.keyUp.setDown(true);return null;}
        mc.options.keyUp.setDown(false);
        for(var c:children){
            var rel=c.position().subtract(base.getX(),base.getY(),base.getZ());
            if(!c.stalkingAppearance()||rel.y<.85||Math.abs(rel.z-window)>.2||rel.x> -8)continue;
            aim(mc,c);
            if(!GoatmanStalking.visible(mc.level,mc.player,mc.player.getEyePosition(),face(c)))continue;
            // Earlier evening sightings consume the same finite deck. Follow the original body's next pane
            // instead of waiting through all later laps at a window showing a face already captured here.
            if(c.monsterAppearance()?forms.contains(true)&&panes.contains(middle):childFaces.contains(c.visibleSkin())){middleView=!middleView;aiming=0;return null;}
            if(++aiming<4)return null;
            if(original!=null&&!original.equals(c.getUUID()))throw new IllegalStateException("LIVE EXPEDITION a window flash replaced the original Goatman body");
            pendingBody=c;pendingBase=Vec3.atLowerCornerOf(base);pendingForm=c.monsterAppearance();pendingSkin=c.visibleSkin();pendingZ=window;pendingName=role+"-trailer-window-"+(pendingForm?"goatman"+(forms.contains(true)?middle?"-middle":"-front":""):childFaces.isEmpty()?"child":"child-"+pendingSkin);pixelStage=visiblePixels=0;maskBody=false;facePixels=null;aiming=0;return pendingName;
        }
        aiming=0;return null;
    }
    private static Vec3 face(GoatmanChild c){return c.position().add(0,c.monsterAppearance()?2.2:1.1,0);}
    private static void aim(Minecraft mc,GoatmanChild c){var toward=face(c).subtract(mc.player.getEyePosition());mc.player.setYRot((float)Math.toDegrees(Math.atan2(-toward.x,toward.z)));mc.player.setXRot((float)-Math.toDegrees(Math.atan2(toward.y,Math.hypot(toward.x,toward.z))));mc.options.keyUp.setDown(false);}
    static boolean windowShot(String name){return name.contains("-trailer-window-");}
    static boolean waiting(){return pendingName!=null;}
    static boolean frameReady(Minecraft mc,String name){
        if(!name.equals(pendingName)||pendingBody==null)return false;var rel=pendingBody.position().subtract(pendingBase);
        if(!pendingBody.isAlive()||!pendingBody.stalkingAppearance()||pendingBody.monsterAppearance()!=pendingForm||!pendingForm&&pendingBody.visibleSkin()!=pendingSkin||rel.y<.85||Math.abs(rel.z-pendingZ)>.2){pendingName=null;pendingBody=null;maskBody=false;return false;}
        var camera=mc.gameRenderer.getMainCamera();var point=face(pendingBody);var ray=point.subtract(camera.getPosition()).normalize();var look=camera.getLookVector();
        if(!(drawn||maskBody)||ray.dot(new Vec3(look.x(),look.y(),look.z()))<=.999||!GoatmanStalking.visible(mc.level,mc.player,camera.getPosition(),point))return false;
        if(pixelStage==0){facePixels=facePixels(mc);maskBody=true;pixelStage=1;return false;}
        if(pixelStage==1){var hidden=facePixels(mc);for(int i=0;i<hidden.length;i++){int a=facePixels[i],b=hidden[i];if(Math.abs((a&255)-(b&255))+Math.abs((a>>8&255)-(b>>8&255))+Math.abs((a>>16&255)-(b>>16&255))>45)visiblePixels++;}maskBody=false;pixelStage=2;if(visiblePixels<20)throw new IllegalStateException("LIVE EXPEDITION the window face was tracked but not visibly drawn: changed head pixels="+visiblePixels);return false;}
        return drawn;
    }
    static void captured(Minecraft mc,String name){
        if(!windowShot(name))return;if(!frameReady(mc,name))throw new IllegalStateException("LIVE EXPEDITION the window image does not show the original native face through clear glazing");
        original=pendingBody.getUUID();forms.add(pendingForm);panes.add(pendingZ==GoatmanWindowViews.window(true));if(!pendingForm&&!childFaces.add(pendingSkin))throw new IllegalStateException("LIVE EXPEDITION a later window sighting recycled a child face");write(name+".txt","Original native body="+original+" form="+(pendingForm?"goatman":"child "+pendingSkin)+" relative="+pendingBody.position().subtract(pendingBase)+" actual eye="+mc.player.getEyePosition()+" camera="+mc.gameRenderer.getMainCamera().getPosition()+" native look="+mc.gameRenderer.getMainCamera().getLookVector()+" visible head pixels="+visiblePixels+"; actual head-region render was compared with one diagnostic frame suppressing only this original body. Clear glazing ray and settled native aim checked.\n");if(childFaces.size()>=2)write(name.substring(0,1)+"-trailer-window-variety.txt","Original body="+original+"; distinct observed child faces="+childFaces+"; Goatman form="+forms.contains(true)+". Native front and middle window screenshots each require visible head pixels, not just entity tracking.\n");middleView=pendingZ==GoatmanWindowViews.window(false);pendingName=null;pendingBody=null;maskBody=false;
    }
    private static int[] facePixels(Minecraft mc){try(var image=net.minecraft.client.Screenshot.takeScreenshot(mc.getMainRenderTarget())){int[] pixels=new int[64*64];int x=image.getWidth()/2-32,y=image.getHeight()/2-32;for(int i=0;i<pixels.length;i++)pixels[i]=image.getPixelRGBA(x+i%64,y+i/64);return pixels;}}
    @SubscribeEvent public static void beginFrame(RenderFrameEvent.Pre ignored){drawn=false;}
    @SubscribeEvent public static void mask(RenderLivingEvent.Pre<?,?> e){if(maskBody&&pendingBody!=null&&e.getEntity()==pendingBody)e.setCanceled(true);}
    @SubscribeEvent public static void bodyDrawn(RenderLivingEvent.Post<?,?> e){if(pendingBody!=null&&e.getEntity()==pendingBody)drawn=true;}
    static void leave(Minecraft mc,int step,int ticks,String role){
        if(departed||step!=37||ticks<15)return;
        if(GoatmanScore.active()||GoatmanScore.playing())throw new IllegalStateException("LIVE EXPEDITION the trailer score or vanilla suppression leaked into the next vignette");
        departed=true;write(role+"-trailer-score-left.txt","The original score stopped and its scene music suppression ended after the actual native departure.\n");
    }
    private static void write(String name,String value){try{Files.createDirectories(LiveExpeditionProof.folder());Files.writeString(LiveExpeditionProof.folder().resolve(name),value);}catch(Exception e){throw new IllegalStateException(e);}}
}
