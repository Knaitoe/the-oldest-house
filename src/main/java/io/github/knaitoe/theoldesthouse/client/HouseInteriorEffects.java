package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;
import org.joml.Matrix4f;

/** Enclosed rooms fade into black, including creative/spectator views and a fully lit descent. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class HouseInteriorEffects extends DimensionSpecialEffects {
    public HouseInteriorEffects(){super(Float.NaN,false,SkyType.NORMAL,false,false);}
    public static boolean blackAt(BlockPos origin,BlockPos camera){
        if(origin==null)return true;
        if(FinaleArchitecture.contains(origin,camera))return true;
        var place=LabyrinthPlaces.placeAt(origin,camera);
        if(place!=null&&NovelRooms.dimension(place).equals(HouseDimensions.INTERIOR))return true;
        // Only the copied domestic surroundings have outdoor sky here. Unknown
        // origin/late packets and the deep void must never expose an Overworld sky.
        return Math.abs((long)camera.getX()-origin.getX())>128||Math.abs((long)camera.getZ()-origin.getZ())>128
                ||camera.getY()<origin.getY()-128||camera.getY()>origin.getY()+128;
    }
    // black() is asked several times a frame; the place lookup only changes with the camera's block or the origin.
    private static BlockPos cachedCamera,cachedOrigin;private static boolean cachedBlack;
    private static boolean black(){var mc=Minecraft.getInstance();if(mc.level==null||!mc.level.dimension().equals(HouseDimensions.INTERIOR))return false;
        var camera=BlockPos.containing(mc.gameRenderer.getMainCamera().getPosition());var origin=HouseSightlineState.origin();
        if(!camera.equals(cachedCamera)||!java.util.Objects.equals(origin,cachedOrigin)){cachedCamera=camera;cachedOrigin=origin;cachedBlack=blackAt(origin,camera);}
        return cachedBlack;}
    @Override public Vec3 getBrightnessDependentFogColor(Vec3 color,float daylight){return black()?Vec3.ZERO:color.multiply(daylight*.94F+.06F,daylight*.94F+.06F,daylight*.91F+.09F);}
    @Override public boolean isFoggyAt(int x,int z){return false;}
    /** The same decision is used by the real dimension renderer and native pixel proof. */
    public static void drawSkyAt(BlockPos origin,BlockPos camera,Matrix4f view,Vec3 color,float time){
        if(!blackAt(origin,camera))HouseOutsideEffects.drawSky(view,color,time);
    }
    @Override public boolean renderSky(ClientLevel level,int ticks,float partial,Matrix4f view,Camera camera,Matrix4f projection,boolean foggy,Runnable setupFog){
        setupFog.run();if(foggy||camera.getFluidInCamera()!=FogType.NONE)return true;
        drawSkyAt(HouseSightlineState.origin(),camera.getBlockPosition(),view,level.getSkyColor(camera.getPosition(),partial),level.getTimeOfDay(partial));return true;
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void color(ViewportEvent.ComputeFogColor e){
        // A scene that authors its own fog colour (the Goatman vigil and dawn, the hospital, the side mazes) keeps it.
        if(black()&&e.getCamera().getFluidInCamera()==FogType.NONE&&!GoatmanClient.authorsFog()&&!NovelSceneClient.authorsFog()&&!StaircaseLeakClient.active()&&StaircaseLeakClient.returnLight()==0){e.setRed(0);e.setGreen(0);e.setBlue(0);}
    }
}
