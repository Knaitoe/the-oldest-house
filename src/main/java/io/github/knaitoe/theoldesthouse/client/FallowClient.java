package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * 0.4.672, Fallow (docs/FALLOW_0_4_672.md): the late afternoon over the wheat on its reader's screen. Outside, a dry, warm haze the
 * colour of the dust off the road, near enough that the wheat runs on into it and no edge of the field is ever seen; under a roof
 * (the store, the garage, the cellar) the ordinary air.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class FallowClient {
    private FallowClient(){}
    /** Where the haze begins and where it is whole, in blocks: the field is 56 blocks to each side of the road, so it closes before them. */
    public static final float HAZE_NEAR=12F,HAZE_FAR=46F;
    /** Where this reader is, relative to the station's base, if they are at it. */
    @Nullable static Vec3 here(){var mc=Minecraft.getInstance();return mc.player==null||mc.player.isSpectator()?null:at();}
    /** 0.4.674: as here(), but a spectator watching counts too (FallowStrikeClient: they see the strike, and earn nothing). */
    @Nullable static Vec3 at(){
        var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||!HouseSightlineState.hasOrigin())return null;
        var origin=HouseSightlineState.origin();var p=LabyrinthPlaces.placeAt(origin,mc.player.blockPosition());
        if(p!=LabyrinthPlace.FALLOW||!mc.level.dimension().equals(NovelRooms.dimension(p)))return null;
        var b=LabyrinthPlaces.base(origin,p);if(b==null)return null;
        var r=mc.player.position().subtract(b.getX(),b.getY(),b.getZ());return FallowSite.inScene(r)?r:null;
    }
    /** Under the store's, the garage's or the cellar's roof. */
    static boolean indoors(Vec3 r){return FallowSite.inStore(r)||FallowSite.inGarage(r)||FallowSite.inCellar(r);}
    @SubscribeEvent public static void colour(ViewportEvent.ComputeFogColor e){
        var r=here();if(r==null||indoors(r))return;
        e.setRed(.80F);e.setGreen(.72F);e.setBlue(.54F);
    }
    @SubscribeEvent(priority=EventPriority.LOW) public static void haze(ViewportEvent.RenderFog e){
        var r=here();if(r==null||indoors(r)||e.getMode()!=FogRenderer.FogMode.FOG_TERRAIN)return;
        float far=Math.min(e.getFarPlaneDistance(),HAZE_FAR),near=Math.min(HAZE_NEAR,far*.5F);
        e.setNearPlaneDistance(near);e.setFarPlaneDistance(far);e.setCanceled(true);
    }
}
