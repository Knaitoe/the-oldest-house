package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.ElkLot;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlace;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlaces;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryRegistry;
import io.github.knaitoe.theoldesthouse.labyrinth.RoadhouseBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * The Last Stop (0.4.78) on the client: a night haze over the lot and road that thickens past the field fence until a
 * reader in the stalks sees a dozen blocks and no further, so the field's repeat is never in view; and the pickups' paint.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class ElkLotClient {
    private ElkLotClient(){}
    /** How far into the field the camera is, from 0 at the fence's lot side to 1 in the stalks; -1 outside the scene. */
    static float depth(){
        var mc=Minecraft.getInstance();var origin=HouseSightlineState.origin();if(mc.player==null||mc.level==null||origin==null||!mc.level.dimension().equals(HouseDimensions.OUTSIDE))return -1;
        var at=mc.gameRenderer.getMainCamera().getBlockPosition();if(LabyrinthPlaces.placeAt(origin,at)!=LabyrinthPlace.ELK_LOT)return -1;
        var b=LabyrinthPlaces.base(origin,LabyrinthPlace.ELK_LOT);if(b==null)return -1;
        double z=mc.gameRenderer.getMainCamera().getPosition().z-b.getZ();
        return (float)Math.max(0,Math.min(1,(-38-z)/(-38-(ElkLot.FIELD_START-3))));
    }
    @SubscribeEvent(priority=EventPriority.LOW) public static void fog(ViewportEvent.RenderFog e){
        float depth=depth();if(depth<0||e.getMode()!=FogRenderer.FogMode.FOG_TERRAIN)return;
        var mc=Minecraft.getInstance();if(mc.player.hasEffect(net.minecraft.world.effect.MobEffects.DARKNESS)||mc.player.hasEffect(net.minecraft.world.effect.MobEffects.BLINDNESS))return;
        float far=Math.min(e.getFarPlaneDistance(),40+(12-40)*depth),near=Math.min(far*.6F,8+(1-8)*depth);
        e.setNearPlaneDistance(near);e.setFarPlaneDistance(far);e.setCanceled(true);
    }
    @SubscribeEvent public static void colour(ViewportEvent.ComputeFogColor e){
        float depth=depth();if(depth<0)return;
        // A blue-black night, a little lighter where the lot's lamp and the bar's windows wash the air.
        e.setRed(.035F+.02F*(1-depth));e.setGreen(.04F+.02F*(1-depth));e.setBlue(.065F+.02F*(1-depth));
    }
    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
    public static final class Colours {
        /** The pickups' bodies take their paint from their stage; glass, chrome, tyres and lights are drawn untinted. */
        @SubscribeEvent public static void blocks(RegisterColorHandlersEvent.Block e){
            e.register((state,level,pos,index)->index==0&&state.getValue(RoadhouseBlock.KIND).pickup()?RoadhouseBlock.PAINT[state.getValue(RoadhouseBlock.STAGE)]:-1,LiteraryRegistry.ROADHOUSE.get());
        }
    }
}
