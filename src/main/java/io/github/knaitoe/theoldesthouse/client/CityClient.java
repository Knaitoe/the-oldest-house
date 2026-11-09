package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import io.github.knaitoe.theoldesthouse.network.CityPayload;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

/**
 * 0.4.409, the city on the ice: the hour (a low sun, or dawn for a reader who outran what fills the tunnel), the snow blowing across the
 * camp and up the pass, the clear cold air from the crest on over the city, and the dark under it.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class CityClient {
    private CityClient(){}
    private static long time=-1;
    public static void accept(CityPayload payload){time=payload.time();}
    /** In the city's scene clock (mode 20), the hour the server has sent this reader. */
    public static long time(Level level,long ordinary){return time>=0&&NovelSceneClient.clockMode(level)==20?time:ordinary;}
    /** Where this reader is, relative to the city's base, if they are in it. */
    @Nullable static Vec3 here(){
        var mc=Minecraft.getInstance();var origin=HouseSightlineState.origin();if(mc.player==null||mc.level==null||mc.player.isSpectator()||origin==null)return null;
        var p=LabyrinthPlaces.placeAt(origin,mc.player.blockPosition());if(p!=LabyrinthPlace.BLACK_CITY||!mc.level.dimension().equals(NovelRooms.dimension(p)))return null;
        var b=LabyrinthPlaces.base(origin,p);return b==null?null:mc.player.position().subtract(b.getX(),b.getY(),b.getZ());
    }
    /** The camp and the climb, where the snow blows; the crest and the city, where the air is clear; and the dark under the city. */
    static boolean blowing(Vec3 r){return r.z>CitySite.CREST_S+4&&r.y>-6;}
    static boolean under(Vec3 r){return r.y<-12;}
    @SubscribeEvent public static void fogColour(ViewportEvent.ComputeFogColor e){
        var r=here();if(r==null)return;
        if(under(r)){e.setRed(.02F);e.setGreen(.02F);e.setBlue(.03F);}
        else if(blowing(r)){e.setRed(.82F);e.setGreen(.86F);e.setBlue(.9F);}
    }
    @SubscribeEvent public static void fog(ViewportEvent.RenderFog e){
        var r=here();if(r==null||e.getMode()!=FogRenderer.FogMode.FOG_TERRAIN)return;
        if(under(r)){e.setNearPlaneDistance(2);e.setFarPlaneDistance(30);e.setCanceled(true);}
        else if(blowing(r)){e.setNearPlaneDistance(4);e.setFarPlaneDistance(56);e.setCanceled(true);}
    }
    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Layers {
        /** Snow streaking flat across the view in the camp and on the climb, thicker in the gusts' half of the wind. */
        @SubscribeEvent public static void register(RegisterGuiLayersEvent e){e.registerAboveAll(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"city_snow"),(g,delta)->{
            var r=here();if(r==null||!blowing(r)||under(r))return;var mc=Minecraft.getInstance();int time=mc.player.tickCount;
            int count=CitySite.inHut(r)?0:(int)(70*Comfort.effects())+20;
            for(int i=0;i<count;i++){int x=Math.floorMod(i*83-time*(5+i%4),g.guiWidth()),y=Math.floorMod(i*47+time*(1+i%2),g.guiHeight());g.fill(x,y,x+2+i%3,y+1,0x99E6ECF2);}
        });}
    }
}
