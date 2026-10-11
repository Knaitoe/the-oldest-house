package io.github.knaitoe.theoldesthouse.client;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.network.CampBedPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Pose;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class CampBedClient {
    private static LocalPlayer player;private static Pose forced,pose;private static int lease;
    private CampBedClient(){}
    public static void accept(CampBedPayload p){if(!p.active()){clear();return;}var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||!mc.level.dimension().equals(HouseDimensions.OUTSIDE))return;
        if(player!=mc.player){clear();player=mc.player;forced=player.getForcedPose();pose=player.getPose();}
        player.setForcedPose(Pose.SWIMMING);player.setPose(Pose.SWIMMING);player.refreshDimensions();lease=30;
    }
    private static void clear(){if(player!=null&&player.getForcedPose()==Pose.SWIMMING){player.setForcedPose(forced);player.setPose(forced==null?pose:forced);player.refreshDimensions();}player=null;lease=0;}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){var mc=Minecraft.getInstance();if(player!=null&&(--lease<=0||mc.player!=player||!player.isAlive()||player.isSpectator()||mc.level==null||!mc.level.dimension().equals(HouseDimensions.OUTSIDE)))clear();}
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){clear();}
}
