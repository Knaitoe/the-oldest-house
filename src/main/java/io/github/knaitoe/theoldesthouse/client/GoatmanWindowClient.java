package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.gametest.LiveExpeditionProof;
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
    private static UUID original;
    private static SimpleSoundInstance vanilla;
    private static long probeAt;
    private static int aiming;
    private static boolean audioPassed,departed;
    private static GoatmanChild pendingBody;private static Vec3 pendingBase;private static String pendingName;private static boolean pendingForm,drawn;
    private GoatmanWindowClient(){}
    static boolean ready(){return forms.size()==2&&audioPassed;}
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
        var goal=new Vec3(base.getX()-3.2,base.getY()+1,base.getZ()+(role.equals("A")?-57.2:-60.4));var walk=goal.subtract(mc.player.position());
        if(walk.horizontalDistanceSqr()>.04){mc.player.setYRot((float)Math.toDegrees(Math.atan2(-walk.x,walk.z)));mc.player.setXRot(0);mc.options.keyUp.setDown(true);return null;}
        mc.options.keyUp.setDown(false);
        for(var c:children){
            var rel=c.position().subtract(base.getX(),base.getY(),base.getZ());
            if(!c.stalkingAppearance()||forms.contains(c.monsterAppearance())||rel.y<.85||Math.abs(rel.z+58.5)>.2||Math.abs(rel.x)<8)continue;
            aim(mc,c);
            if(!GoatmanStalking.visible(mc.level,mc.player,mc.player.getEyePosition(),face(c)))continue;
            if(++aiming<4)return null;
            if(original!=null&&!original.equals(c.getUUID()))throw new IllegalStateException("LIVE EXPEDITION a window flash replaced the original Goatman body");
            pendingBody=c;pendingBase=Vec3.atLowerCornerOf(base);pendingForm=c.monsterAppearance();pendingName=role+"-trailer-window-"+(pendingForm?"goatman":"child");aiming=0;return pendingName;
        }
        aiming=0;return null;
    }
    private static Vec3 face(GoatmanChild c){return c.position().add(0,c.monsterAppearance()?2.2:1.1,0);}
    private static void aim(Minecraft mc,GoatmanChild c){var toward=face(c).subtract(mc.player.getEyePosition());mc.player.setYRot((float)Math.toDegrees(Math.atan2(-toward.x,toward.z)));mc.player.setXRot((float)-Math.toDegrees(Math.atan2(toward.y,Math.hypot(toward.x,toward.z))));mc.options.keyUp.setDown(false);}
    static boolean windowShot(String name){return name.contains("-trailer-window-");}
    static boolean waiting(){return pendingName!=null;}
    static boolean frameReady(Minecraft mc,String name){
        if(!name.equals(pendingName)||pendingBody==null)return false;var rel=pendingBody.position().subtract(pendingBase);
        if(!pendingBody.isAlive()||!pendingBody.stalkingAppearance()||pendingBody.monsterAppearance()!=pendingForm||rel.y<.85||Math.abs(rel.z+58.5)>.2){pendingName=null;pendingBody=null;return false;}
        var camera=mc.gameRenderer.getMainCamera();var point=face(pendingBody);var ray=point.subtract(camera.getPosition()).normalize();var look=camera.getLookVector();
        return drawn&&ray.dot(new Vec3(look.x(),look.y(),look.z()))>.999&&GoatmanStalking.visible(mc.level,mc.player,camera.getPosition(),point);
    }
    static void captured(Minecraft mc,String name){
        if(!windowShot(name))return;if(!frameReady(mc,name))throw new IllegalStateException("LIVE EXPEDITION the window image does not show the original native face through clear glazing");
        original=pendingBody.getUUID();forms.add(pendingForm);write(name+".txt","Original native body="+original+" form="+(pendingForm?"goatman":"child")+" relative="+pendingBody.position().subtract(pendingBase)+" actual eye="+mc.player.getEyePosition()+" camera="+mc.gameRenderer.getMainCamera().getPosition()+" native look="+mc.gameRenderer.getMainCamera().getLookVector()+" native body rendered in this frame; clear glazing ray and settled native aim checked.\n");pendingName=null;pendingBody=null;
    }
    @SubscribeEvent public static void beginFrame(RenderFrameEvent.Pre ignored){drawn=false;}
    @SubscribeEvent public static void bodyDrawn(RenderLivingEvent.Post<?,?> e){if(pendingBody!=null&&e.getEntity()==pendingBody)drawn=true;}
    static void leave(Minecraft mc,int step,int ticks,String role){
        if(departed||step!=37||ticks<15)return;
        if(GoatmanScore.active()||GoatmanScore.playing())throw new IllegalStateException("LIVE EXPEDITION the trailer score or vanilla suppression leaked into the next vignette");
        departed=true;write(role+"-trailer-score-left.txt","The original score stopped and its scene music suppression ended after the actual native departure.\n");
    }
    private static void write(String name,String value){try{Files.createDirectories(LiveExpeditionProof.folder());Files.writeString(LiveExpeditionProof.folder().resolve(name),value);}catch(Exception e){throw new IllegalStateException(e);}}
}
