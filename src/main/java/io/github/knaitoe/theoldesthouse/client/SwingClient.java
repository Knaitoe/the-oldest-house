package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.SeatEntity;
import io.github.knaitoe.theoldesthouse.labyrinth.HouseReadSwing;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

/**
 * 0.4.465, Bike night: a reader on one of the park's swings, as the others see them. A park swing goes far higher than the porch's,
 * and at the top of a high arc its chains lie well over; so the reader goes over with it, about their hips, exactly as the seat and
 * its chains are turned (SeatClientEvents), and their hands are up on the chains. The porch's sitter, on a wide plank and a gentle
 * arc, is left as they were. A reader does not see themself, and their own view is not turned.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class SwingClient {
    private SwingClient(){}
    private static final Set<Player> TILTED=Collections.newSetFromMap(new IdentityHashMap<>());
    /** A reader's hips above their feet at full size, and how their arms are raised to the chains. */
    private static final float HIPS=.6F,ARMS_UP=-2.7F,ARMS_OUT=.35F;
    /** The seat somebody is swinging on, if it is one of the park's. */
    static @Nullable SeatEntity swing(Entity rider){return rider.getVehicle() instanceof SeatEntity seat&&seat.swinging()&&seat.pumped()?seat:null;}
    /** How far over the swing is now: the angle the moving seat is drawn at. */
    static float angle(SeatEntity seat,float partial){
        var level=seat.level();var pair=HouseReadSwing.pair(level,seat.support());var state=level.getBlockState(pair.getFirst());if(!HouseReadSwing.is(state))return 0;
        var forward=HouseReadSwing.forward(state);var pivot=HouseReadSwing.pivot(pair);var at=seat.getPosition(partial);
        return (float)Math.atan2((at.x-pivot.x)*forward.getStepX()+(at.z-pivot.z)*forward.getStepZ(),pivot.y-at.y);
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void tilt(RenderPlayerEvent.Pre e){
        var seat=swing(e.getEntity());if(seat==null)return;
        var state=seat.level().getBlockState(seat.support());if(!HouseReadSwing.is(state))return;var forward=HouseReadSwing.forward(state);
        float hips=HIPS*e.getEntity().getScale();var pose=e.getPoseStack();pose.pushPose();
        pose.translate(0,hips,0);pose.mulPose(new org.joml.Quaternionf().rotateAxis(angle(seat,e.getPartialTick()),-forward.getStepZ(),0,forward.getStepX()));pose.translate(0,-hips,0);
        TILTED.add(e.getEntity());
    }
    @SubscribeEvent public static void untilt(RenderPlayerEvent.Post e){if(TILTED.remove(e.getEntity()))e.getPoseStack().popPose();}
    /** Both hands up on the chains, a little out from the shoulders (PlayerPatMixin). */
    public static void pose(PlayerModel<?> model,LivingEntity entity,float age){
        if(swing(entity)==null)return;
        model.rightArm.xRot=model.leftArm.xRot=ARMS_UP;model.rightArm.yRot=model.leftArm.yRot=0;model.rightArm.zRot=ARMS_OUT;model.leftArm.zRot=-ARMS_OUT;
        model.leftSleeve.copyFrom(model.leftArm);model.rightSleeve.copyFrom(model.rightArm);
    }
}
