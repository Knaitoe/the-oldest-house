package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.StaircaseLean;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * 0.4.594: a reader's view as they lean over a rail of the great staircase's well ({@link StaircaseLean}).
 *
 * <p>Standing still at a rail and looking down, the view eases out over the rail and a little lower; looking up, or stepping
 * away, it eases back. Only the view moves ({@code mixin.StairLeanCameraMixin}): the reader stands where they stood.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class StaircaseLeanView {
    /** How much of the lean is gained and lost in a tick, and the fastest a reader may be moving (blocks a tick, squared) and still lean out. */
    static final float OUT=.12F,BACK=.2F;
    static final double STILL=.0016;
    private static float lean,before;
    private static Vec3 over=Vec3.ZERO;
    private StaircaseLeanView(){}

    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        var mc=Minecraft.getInstance();if(mc.isPaused())return;
        before=lean;var p=mc.player;Vec3 now=null;
        if(p!=null&&mc.level!=null&&mc.level.dimension().equals(HouseDimensions.INTERIOR)&&mc.options.getCameraType().isFirstPerson()&&p.isAlive()
                &&!p.isSpectator()&&!p.isPassenger()&&p.onGround()&&p.getDeltaMovement().horizontalDistanceSqr()<STILL)
            now=StaircaseLean.over(mc.level,HouseSightlineState.origin(),p.position(),p.getEyeHeight(),p.getYRot(),p.getXRot());
        if(now!=null){over=now;lean=Math.min(1,lean+OUT);}else lean=Math.max(0,lean-BACK);
    }
    /** How far the view is out over the rail this frame, from the reader's own eye; null when they stand as they are. */
    public static Vec3 offset(float partial){
        float t=Mth.lerp(partial,before,lean);if(t<=0)return null;
        return over.scale(t*t*(3-2*t));
    }
    /** 0 standing, 1 leaning right out. */
    public static float leaning(){return lean;}
}
