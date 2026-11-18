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
 * 0.4.699, The list (docs/THE_LIST_0_4_699.md): Lamesa's night on its reader's screen. Outside, a dark haze with a little of the street
 * lights' warmth in it, near enough that the cotton runs on into it; it draws in closer as a reader nears the town's edge, so that no edge
 * is ever seen. Under a roof (the house, a store, the gin, the seed house) the ordinary air.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class TheListClient {
    private TheListClient(){}
    /** Where the haze is whole at its farthest, and the least it ever closes to, in blocks. */
    public static final float HAZE_FAR=52F,HAZE_CLOSEST=12F;
    /** Where this reader is, relative to the town's base, if they are in it (a spectator watching counts too). */
    @Nullable static Vec3 at(){
        var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||!HouseSightlineState.hasOrigin())return null;
        var origin=HouseSightlineState.origin();var p=LabyrinthPlaces.placeAt(origin,mc.player.blockPosition());
        if(p!=LabyrinthPlace.THE_LIST||!mc.level.dimension().equals(NovelRooms.dimension(p)))return null;
        var b=LabyrinthPlaces.base(origin,p);if(b==null)return null;
        var r=mc.player.position().subtract(b.getX(),b.getY(),b.getZ());return TheListSite.inScene(r)?r:null;
    }
    /** How far the haze lets a reader see from here: its farthest, or nearer as the town's edge comes within reach of it. */
    public static float far(Vec3 r){
        double edge=Math.min(Math.min(r.x-TheListSite.WEST,TheListSite.EAST+1-r.x),Math.min(r.z-TheListSite.NORTH,TheListSite.SOUTH+1-r.z));
        return (float)Math.max(HAZE_CLOSEST,Math.min(HAZE_FAR,edge+4));
    }
    @SubscribeEvent public static void colour(ViewportEvent.ComputeFogColor e){
        var r=at();if(r==null||TheListSite.indoors(r))return;
        e.setRed(.06F);e.setGreen(.06F);e.setBlue(.09F);
    }
    @SubscribeEvent(priority=EventPriority.LOW) public static void haze(ViewportEvent.RenderFog e){
        if(e.getMode()!=FogRenderer.FogMode.FOG_TERRAIN)return;
        var r=at();if(r==null||TheListSite.indoors(r))return;
        float far=Math.min(e.getFarPlaneDistance(),far(r)),near=far*.3F;
        e.setNearPlaneDistance(near);e.setFarPlaneDistance(far);e.setCanceled(true);
    }
}
