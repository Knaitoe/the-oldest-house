package io.github.knaitoe.theoldesthouse.client;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.FinaleArchitecture;
import io.github.knaitoe.theoldesthouse.network.StaircaseLightPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
/**
 * The dark on the great staircase, as far as the reader's own book lights it.
 *
 * <p>0.4.595 (docs/STAIR_NIGHTS_0_4_595.md): it lifted once five fires burned, and the well then showed forty-six blocks. It now
 * holds at whatever the server sends (twelve blocks on the stair, less at a camp whose fire is cold) for the whole descent, and
 * it eases from one distance to the next, so a lit fire pushes the dark back and the foot of the stair opens as it is walked into.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class StaircaseDarkness {
    /** The well's own fog (VignetteViewRules): at this sight the stair's dark is not drawn. Its near plane there. */
    static final float OPEN=46,OPEN_NEAR=10;
    /** How fast the dark closes in and draws back, in blocks a tick. */
    static final float CLOSE=.6F,DRAW_BACK=.35F;
    private static int lease;private static float target=OPEN,sight=OPEN;
    private StaircaseDarkness(){}
    public static void accept(StaircaseLightPayload p){
        lease=p.active()?30:0;
        target=!p.active()||p.sight()>=io.github.knaitoe.theoldesthouse.labyrinth.StaircaseFire.CLEAR?OPEN:Math.max(.8F,p.sight());
    }
    /** How far the reader sees this tick. */
    public static float sight(){return sight;}
    private static boolean active(){var mc=Minecraft.getInstance();return lease>0&&mc.level!=null&&mc.player!=null&&mc.player.isAlive()&&!mc.player.isSpectator()&&!mc.player.isCreative()
            &&mc.level.dimension().equals(HouseDimensions.INTERIOR)&&HouseSightlineState.origin()!=null&&FinaleArchitecture.contains(HouseSightlineState.origin(),mc.player.blockPosition());}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        if(lease>0)lease--;
        if(!active()){lease=0;target=OPEN;}
        if(Minecraft.getInstance().isPaused())return;
        sight=step(sight,target);
    }
    /** One tick of the dark moving toward where it should stand. */
    static float step(float sight,float target){return target<sight?Math.max(target,sight-CLOSE):Math.min(target,sight+DRAW_BACK);}
    private static boolean drawn(){return sight<OPEN-.01F&&inWell();}
    /** In the well and not looking at it as a spectator or in creative; the dark goes on easing back after the server stops sending it. */
    private static boolean inWell(){var mc=Minecraft.getInstance();return mc.level!=null&&mc.player!=null&&mc.player.isAlive()&&!mc.player.isSpectator()&&!mc.player.isCreative()
            &&mc.level.dimension().equals(HouseDimensions.INTERIOR)&&HouseSightlineState.origin()!=null&&FinaleArchitecture.contains(HouseSightlineState.origin(),mc.player.blockPosition());}
    @SubscribeEvent public static void color(ViewportEvent.ComputeFogColor e){if(drawn()&&!StaircaseLeakClient.active()&&StaircaseLeakClient.returnLight()==0){e.setRed(0);e.setGreen(0);e.setBlue(0);}}
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void fog(ViewportEvent.RenderFog e){
        if(!drawn()||e.getMode()!=net.minecraft.client.renderer.FogRenderer.FogMode.FOG_TERRAIN)return;
        // At the book's twelve blocks the dark begins at the reader's hand; as it draws back to the well's own fog, so does its near edge.
        float open=Math.max(0,(sight-io.github.knaitoe.theoldesthouse.labyrinth.StaircaseFire.SIGHT)/(OPEN-io.github.knaitoe.theoldesthouse.labyrinth.StaircaseFire.SIGHT));
        e.setNearPlaneDistance(.15F+(OPEN_NEAR-.15F)*Math.min(1,open));e.setFarPlaneDistance(sight);e.setCanceled(true);
    }
}
