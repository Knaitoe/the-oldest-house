package io.github.knaitoe.theoldesthouse.client;

import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.NightBike;
import io.github.knaitoe.theoldesthouse.labyrinth.RiddenBike;
import io.github.knaitoe.theoldesthouse.network.BikeKeysPayload;
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
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Riding a bike, as the rider sees it (0.4.435 for Borrasca's bikes, 0.4.463 for every bike). Anyone on a bike leans with it, as others
 * see them, and the one pedalling goes down over the bars while the jump key is held. In first person the view tips with the lean and
 * dips with the crouch, both scaled by the reader's distortion setting (Comfort). Under the crosshair a needle shows the lean: upright
 * in the middle, and over at either end. Under it a line fills as the hop is wound up, and on the Rattler a second shows what is left
 * of the card's burst. The jump and sprint keys are sent to the server as they change (BikeKeysPayload).
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class BikeClient {
    private BikeClient(){}
    /** How far the view tips at a lean of one and dips at a full crouch, in degrees, before the comfort setting. */
    static final float ROLL=14,DIP=5;
    /** How far forward a rider wound up for a hop leans, in degrees, and how far down they come, in blocks. */
    static final float CROUCH_TILT=24,CROUCH_DROP=.14F;
    private static final Set<Player> LEANED=Collections.newSetFromMap(new IdentityHashMap<>());
    private static boolean sentJump,sentTurbo,wasPedalling;

    static @Nullable RiddenBike riding(){var mc=Minecraft.getInstance();return mc.player!=null&&mc.player.getVehicle() instanceof RiddenBike b?b:null;}

    // The riders lean with the bike, about the line its wheels touch the road (their feet are on the pedals, a little above it).
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void lean(RenderPlayerEvent.Pre e){
        if(!(e.getEntity().getVehicle() instanceof RiddenBike b))return;float partial=e.getPartialTick();float yaw=Mth.rotLerp(partial,b.yRotO,b.getYRot());
        var pose=e.getPoseStack();pose.pushPose();
        // 0.4.447: about the road under the bike, however far above it this rider sits.
        float up=(float)(Mth.lerp(partial,e.getEntity().yo,e.getEntity().getY())-Mth.lerp(partial,b.yo,b.getY()));
        pose.translate(0,-up,0);pose.mulPose(Axis.YP.rotationDegrees(-(yaw+90)));pose.mulPose(Axis.XP.rotationDegrees(b.shownLean(partial)*RiddenBike.LEAN_DEGREES));
        // 0.4.463: the one pedalling, wound up for a hop, comes down and forward over the bars, about their hips.
        float crouch=b.getFirstPassenger()==e.getEntity()?b.shownCrouch(partial):0;
        if(crouch>.001F){float hips=up+.675F*e.getEntity().getScale();pose.translate(0,hips-CROUCH_DROP*crouch,0);pose.mulPose(Axis.ZP.rotationDegrees(-CROUCH_TILT*crouch));pose.translate(0,-hips,0);}
        pose.mulPose(Axis.YP.rotationDegrees(yaw+90));pose.translate(0,up,0);
        LEANED.add(e.getEntity());
    }
    @SubscribeEvent public static void unlean(RenderPlayerEvent.Post e){if(LEANED.remove(e.getEntity()))e.getPoseStack().popPose();}

    @SubscribeEvent public static void roll(ViewportEvent.ComputeCameraAngles e){
        var b=riding();if(b==null||!Minecraft.getInstance().options.getCameraType().isFirstPerson())return;
        float partial=(float)e.getPartialTick();
        e.setRoll(e.getRoll()+b.shownLean(partial)*ROLL*Comfort.effects());
        if(b.getFirstPassenger()==Minecraft.getInstance().player)e.setPitch(e.getPitch()+b.shownCrouch(partial)*DIP*Comfort.effects());
    }

    /** The keys the game does not send for a rider: jump and sprint, to the server whenever either changes while this reader pedals. */
    @SubscribeEvent public static void keys(ClientTickEvent.Post e){
        var mc=Minecraft.getInstance();var p=mc.player;var b=riding();
        boolean pedalling=b!=null&&b.getFirstPassenger()==p;
        if(!pedalling){wasPedalling=false;return;}
        boolean open=mc.screen==null;
        boolean jump=open&&p.input!=null&&p.input.jumping,turbo=open&&mc.options.keySprint.isDown();
        if(!wasPedalling||jump!=sentJump||turbo!=sentTurbo){PacketDistributor.sendToServer(new BikeKeysPayload(jump,turbo));sentJump=jump;sentTurbo=turbo;}
        wasPedalling=true;
    }

    /** The needle under the crosshair, the hop winding up under it, and the Rattler's card. */
    static void balance(GuiGraphics g,float partial){
        var b=riding();var mc=Minecraft.getInstance();if(b==null||mc.options.hideGui)return;
        boolean pedalling=b.getFirstPassenger()==mc.player;
        draw(g,g.guiWidth()/2,g.guiHeight()/2+22,b.shownLean(partial),pedalling?b.shownCrouch(partial):0,pedalling&&b instanceof NightBike night&&night.make()==NightBike.Make.RATTLER?night.turbo():-1);
    }
    /**
     * The same, at a place on the screen: the needle at {@code lean} (-1 to 1); under it the hop wound up by {@code crouch} (nothing
     * drawn at none); beside it the card, {@code card} of it left, drawn as a card with its pip when it is whole (no card below nothing).
     */
    static void draw(GuiGraphics g,int cx,int y,float lean,float crouch,float card){
        int half=40;
        g.fill(cx-half-1,y-1,cx+half+2,y+4,0x90000000);
        g.fill(cx-half,y,cx-half+8,y+3,0x90A8392C);g.fill(cx+half-7,y,cx+half+1,y+3,0x90A8392C);
        g.fill(cx,y-2,cx+1,y+5,0xB0FFFFFF);
        int x=cx+Math.round(Mth.clamp(lean,-1,1)*half);
        g.fill(x-1,y-3,x+2,y+6,0xFFF2E8D0);
        if(crouch>.01F){int w=Math.round(half*crouch);g.fill(cx-half-1,y+7,cx+half+2,y+10,0x90000000);g.fill(cx-w,y+8,cx+w+1,y+9,crouch>.98F?0xFFF2E8D0:0xC0C9BFA8);}
        if(card>=0){
            int left=cx+half+8,top=y-4,tall=10,wide=7;
            g.fill(left-1,top-1,left+wide+1,top+tall+1,0x90000000);
            int fill=Math.round(tall*Mth.clamp(card,0,1));
            g.fill(left,top+tall-fill,left+wide,top+tall,card>=.999F?0xFFF2ECDD:0xC0D8D2C2);
            if(card>=.999F){g.fill(left+1,top+1,left+2,top+2,0xFFB3261F);g.fill(left+wide-2,top+tall-2,left+wide-1,top+tall-1,0xFFB3261F);}
        }
    }

    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Layers {
        @SubscribeEvent public static void register(RegisterGuiLayersEvent e){e.registerAboveAll(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"borrasca_bike"),(g,delta)->balance(g,delta.getGameTimeDeltaPartialTick(false)));}
    }
}
