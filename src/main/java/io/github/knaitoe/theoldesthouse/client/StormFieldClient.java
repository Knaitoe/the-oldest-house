package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * 0.4.347, the storm field: the sky over the corn follows the field's own storm, read from the windsock on the rise (slack, lifting, full),
 * so every reader in the field sees the same weather. Overcast with the view clear to the farmhouse; rain, closing the view to the middle
 * of the field; the storm, with the view taken down to a few rows. Under the shed's roof the rain falls outside.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class StormFieldClient {
    private StormFieldClient(){}
    static boolean inField(){
        var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||!mc.level.dimension().equals(HouseDimensions.OUTSIDE)||!HouseSightlineState.hasOrigin())return false;
        return LabyrinthPlaces.placeAt(HouseSightlineState.origin(),mc.player.blockPosition())==YardStorm.PLACE;
    }
    /** The storm's stage as the windsock shows it: 0 overcast, 1 rain, 2 the storm; -1 out of the field. */
    static int stage(){
        if(!inField())return -1;var mc=Minecraft.getInstance();
        var at=LabyrinthPlaces.base(HouseSightlineState.origin(),YardStorm.PLACE).offset(YardStorm.WINDSOCK);var st=mc.level.getBlockState(at);
        return YardBlock.is(st,YardBlock.Kind.WINDSOCK)?st.getValue(YardBlock.STAGE):0;
    }
    public static boolean precipitating(){return com.mojang.blaze3d.systems.RenderSystem.isOnRenderThread()&&stage()>0;}
    public static float rain(net.minecraft.world.level.Level level,float nativeRain){if(level!=Minecraft.getInstance().level)return nativeRain;int s=stage();return s<0?nativeRain:s==0?.15F:s==1?.6F:1F;}
    public static float thunder(net.minecraft.world.level.Level level,float nativeThunder){if(level!=Minecraft.getInstance().level)return nativeThunder;int s=stage();return s<0?nativeThunder:s==2?.9F:0;}
    @SubscribeEvent public static void fog(ViewportEvent.ComputeFogColor e){int s=stage();if(s>0){float k=s==1?.55F:.32F;e.setRed(e.getRed()*k+.06F);e.setGreen(e.getGreen()*k+.065F);e.setBlue(e.getBlue()*k+.07F);}}
    @SubscribeEvent public static void fog(ViewportEvent.RenderFog e){int s=stage();if(s>0&&e.getMode()==FogRenderer.FogMode.FOG_TERRAIN){e.setNearPlaneDistance(s==1?10:3);e.setFarPlaneDistance(s==1?44:16);e.setCanceled(true);}}
}
