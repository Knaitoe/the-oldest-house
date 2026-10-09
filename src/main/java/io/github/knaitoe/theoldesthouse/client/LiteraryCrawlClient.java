package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Pose;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

/** A private server lease keeps the real local collision body equal to the server's crawl. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class LiteraryCrawlClient {
    private static LocalPlayer player;private static Pose previousForced,previousDisplayed;private static int lease;
    private LiteraryCrawlClient(){}
    public static void accept(boolean active){
        if(!active){clear();return;}var current=Minecraft.getInstance().player;var origin=HouseSightlineState.origin();if(current==null||origin==null)return;
        if(player!=current){clear();player=current;previousForced=player.getForcedPose();previousDisplayed=player.getPose();}
        player.setForcedPose(Pose.SWIMMING);player.setPose(Pose.SWIMMING);player.refreshDimensions();
        PortholeCrawl.local(player,LabyrinthPlaces.base(origin,LabyrinthPlace.ELK_CARCASSES));lease=30;
    }
    private static void clear(){
        if(player!=null){PortholeCrawl.local(player,null);if(player.getForcedPose()==Pose.SWIMMING){player.setForcedPose(previousForced);player.setPose(previousForced==null?previousDisplayed:previousForced);player.refreshDimensions();}}
        player=null;previousForced=null;previousDisplayed=null;lease=0;
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){clear();}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        if(player==null)return;var mc=Minecraft.getInstance();if(--lease<=0||mc.player!=player||!player.isAlive()||player.isSpectator()||mc.level==null||!mc.level.dimension().equals(HouseDimensions.OUTSIDE))clear();
    }
}
