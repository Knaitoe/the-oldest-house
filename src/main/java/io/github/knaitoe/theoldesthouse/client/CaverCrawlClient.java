package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.network.CaverCrawlPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Pose;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class CaverCrawlClient {
    private static LocalPlayer player;
    private static Pose previousForced,previousDisplayed;
    private static int lease,x,y,z;
    private CaverCrawlClient(){}
    public static void accept(CaverCrawlPayload p){
        if(!p.active()){clear();return;}var current=Minecraft.getInstance().player;
        if(current==null)return;if(player!=current){clear();player=current;previousForced=player.getForcedPose();previousDisplayed=player.getPose();player.setForcedPose(Pose.SWIMMING);player.setPose(Pose.SWIMMING);player.refreshDimensions();}
        lease=80;x=p.x();y=p.y();z=p.z();
    }
    private static void clear(){
        if(player!=null&&player.getForcedPose()==Pose.SWIMMING){player.setForcedPose(previousForced);player.setPose(previousForced==null?previousDisplayed:previousForced);player.refreshDimensions();}
        player=null;previousForced=null;previousDisplayed=null;lease=0;
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){clear();}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        if(player==null)return;var mc=Minecraft.getInstance();lease--;
        double rx=player.getX()-x,ry=player.getY()-y,rz=player.getZ()-z;
        if(lease<=0||mc.player!=player||!player.isAlive()||mc.level==null||!mc.level.dimension().equals(HouseDimensions.INTERIOR)
                ||rx<-.5||rx>1.5||ry< -3.5||ry> -1.8||rz> -20.5||rz< -36.4)clear();
    }
}
