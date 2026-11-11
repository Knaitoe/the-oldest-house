package io.github.knaitoe.theoldesthouse.client;

import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.BorrascaBike;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * 0.4.435: riding a bike, as the rider sees it. A rider on a bike leans with it, as others see them. In first person the view tips the
 * same way, scaled by the reader's distortion setting (Comfort). Under the crosshair a needle shows the lean: upright in the middle, and
 * over at either end.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class BorrascaBikeClient {
    private BorrascaBikeClient(){}
    /** How far the view tips at a lean of one, in degrees, before the comfort setting. */
    static final float ROLL=14;
    private static final Set<Player> LEANED=Collections.newSetFromMap(new IdentityHashMap<>());

    static @Nullable BorrascaBike riding(){var mc=Minecraft.getInstance();return mc.player!=null&&mc.player.getVehicle() instanceof BorrascaBike b?b:null;}

    // The rider leans with the bike, about the line its wheels touch the road (their feet are on the pedals, a little above it).
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void lean(RenderPlayerEvent.Pre e){
        if(!(e.getEntity().getVehicle() instanceof BorrascaBike b))return;float partial=e.getPartialTick();float yaw=Mth.rotLerp(partial,b.yRotO,b.getYRot());
        var pose=e.getPoseStack();pose.pushPose();
        // 0.4.447: about the road under the bike, however far above it this rider sits.
        float up=(float)(Mth.lerp(partial,e.getEntity().yo,e.getEntity().getY())-Mth.lerp(partial,b.yo,b.getY()));
        pose.translate(0,-up,0);pose.mulPose(Axis.YP.rotationDegrees(-(yaw+90)));pose.mulPose(Axis.XP.rotationDegrees(b.shownLean(partial)*BorrascaBike.LEAN_DEGREES));pose.mulPose(Axis.YP.rotationDegrees(yaw+90));pose.translate(0,up,0);
        LEANED.add(e.getEntity());
    }
    @SubscribeEvent public static void unlean(RenderPlayerEvent.Post e){if(LEANED.remove(e.getEntity()))e.getPoseStack().popPose();}

    @SubscribeEvent public static void roll(ViewportEvent.ComputeCameraAngles e){
        var b=riding();if(b==null||!Minecraft.getInstance().options.getCameraType().isFirstPerson())return;
        e.setRoll(e.getRoll()+b.shownLean((float)e.getPartialTick())*ROLL*Comfort.effects());
    }

    /** The needle under the crosshair. */
    static void balance(GuiGraphics g,float partial){
        var b=riding();if(b==null||Minecraft.getInstance().options.hideGui)return;
        int cx=g.guiWidth()/2,y=g.guiHeight()/2+22,half=40;
        g.fill(cx-half-1,y-1,cx+half+2,y+4,0x90000000);
        g.fill(cx-half,y,cx-half+8,y+3,0x90A8392C);g.fill(cx+half-7,y,cx+half+1,y+3,0x90A8392C);
        g.fill(cx,y-2,cx+1,y+5,0xB0FFFFFF);
        int x=cx+Math.round(Mth.clamp(b.shownLean(partial),-1,1)*half);
        g.fill(x-1,y-3,x+2,y+6,0xFFF2E8D0);
    }

    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Layers {
        @SubscribeEvent public static void register(RegisterGuiLayersEvent e){e.registerAboveAll(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"borrasca_bike"),(g,delta)->balance(g,delta.getGameTimeDeltaPartialTick(false)));}
    }
}
