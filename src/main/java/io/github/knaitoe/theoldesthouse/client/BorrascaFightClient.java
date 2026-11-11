package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.BorrascaSheriffFight;
import io.github.knaitoe.theoldesthouse.labyrinth.BorrascaStory;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryActor;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

/**
 * 0.4.457 (owner: "Punches should be sluggish with real weight"): what a reader's own blow feels like when it lands on the
 * Sheriff (labyrinth.BorrascaSheriffFight). The arm is slow because the server holds it slow. This is the other half: when a
 * blow brought all the way back goes in and he is seen to take it, the view is jolted for a moment, down and to one side, the
 * other side the next time. It scales with Distortion Effects, to nothing at none.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class BorrascaFightClient {
    private BorrascaFightClient(){}
    /** How long the jolt lasts, and how far the view goes, in degrees. */
    static final float JOLT=8,PITCH=2.4F,ROLL=1.6F;
    private static int thrownAt=-100,target=-1;private static long landedAt=-100;private static float side=1;

    /** A blow thrown at him with the arm all the way back: it has not landed until he is seen to take it. */
    @SubscribeEvent public static void thrown(AttackEntityEvent e){
        var mc=Minecraft.getInstance();
        if(mc.player==null||e.getEntity()!=mc.player||!(e.getTarget() instanceof LiteraryActor a)||a.role()!=LiteraryActor.BORRASCA_SHERIFF)return;
        if(a.gesture()==BorrascaStory.KNELT||a.gesture()==BorrascaStory.FALLEN||mc.player.getAttackStrengthScale(.5F)<BorrascaSheriffFight.FULL_BLOW)return;
        thrownAt=mc.player.tickCount;target=a.getId();
    }
    @SubscribeEvent public static void landed(ClientTickEvent.Post e){
        var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||target<0)return;
        if(mc.player.tickCount-thrownAt>6){target=-1;return;}
        if(mc.level.getEntity(target) instanceof LiteraryActor a&&a.hurtTime>0){landedAt=mc.level.getGameTime();side=-side;target=-1;}
    }
    /** How far the view is taken, of 1, at a time after a blow landed. */
    static float jolt(float ticks){if(ticks<0||ticks>=JOLT)return 0;float k=1-ticks/JOLT;return k*k;}
    @SubscribeEvent public static void view(ViewportEvent.ComputeCameraAngles e){
        var mc=Minecraft.getInstance();if(mc.level==null)return;
        float k=jolt(mc.level.getGameTime()-landedAt+(float)e.getPartialTick())*Comfort.effects();if(k<=0)return;
        e.setPitch(e.getPitch()+PITCH*k);e.setRoll(e.getRoll()+ROLL*side*k);
    }
}
