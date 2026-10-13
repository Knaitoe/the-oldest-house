package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.HolyRabbit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

/** Personal daylight and wind-driven snow; never changes the server's weather or time. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class RabbitSceneClient {
    private static ClientLevel world;private static int lease,dayTicks;private static net.minecraft.core.BlockPos base;
    private RabbitSceneClient(){}
    public static void accept(CompoundTag tag){var mc=Minecraft.getInstance();if(mc.player==null||!tag.getBoolean("RabbitScene0491")||!tag.hasUUID("Reader")||!tag.getUUID("Reader").equals(mc.player.getUUID()))return;var at=tag.getIntArray("RabbitBase");if(at.length!=3)return;base=new net.minecraft.core.BlockPos(at[0],at[1],at[2]);world=mc.level;lease=25;dayTicks=Math.max(0,Math.min(HolyRabbit.DAY_TICKS,tag.getInt("RabbitDayTicks")));}
    public static boolean active(Level level){var mc=Minecraft.getInstance();return level!=null&&lease>0&&level==world&&level==mc.level&&mc.player!=null&&mc.player.isAlive()&&!mc.player.isSpectator()&&level.dimension().equals(HouseDimensions.OUTSIDE)&&base!=null&&io.github.knaitoe.theoldesthouse.labyrinth.IndianLakeRooms.bounds(base,io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlace.HOLY_RABBIT).contains(mc.player.position());}
    public static long time(Level level,long nativeTime){return active(level)?HolyRabbit.skyTime(dayTicks):nativeTime;}
    public static int darkness(Level level,int nativeDarkness){if(!active(level))return nativeDarkness;long time=HolyRabbit.skyTime(dayTicks);return time<12000?0:time<14000?(int)((time-12000)*10/2000):10;}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){var mc=Minecraft.getInstance();if(lease>0)lease--;if(!active(mc.level)){if(lease==0||mc.level!=world){world=null;lease=0;}return;}mc.level.updateSkyBrightness();var p=mc.player;
        int count=mc.options.particles().get()==net.minecraft.client.ParticleStatus.MINIMAL?5:14;
        for(int i=0;i<count;i++){double x=p.getX()+mc.level.random.nextDouble()*28-14,y=p.getY()+4+mc.level.random.nextDouble()*10,z=p.getZ()+mc.level.random.nextDouble()*28-14;mc.level.addParticle(ParticleTypes.SNOWFLAKE,x,y,z,.16,-.12,.035);}
    }
    @SubscribeEvent public static void fog(ViewportEvent.ComputeFogColor e){var mc=Minecraft.getInstance();if(!active(mc.level)||mc.player.isInWater())return;boolean night=HolyRabbit.skyTime(dayTicks)>=13000;e.setRed(night?.20F:.73F);e.setGreen(night?.24F:.78F);e.setBlue(night?.30F:.83F);}
    @SubscribeEvent public static void distance(ViewportEvent.RenderFog e){var mc=Minecraft.getInstance();if(active(mc.level)&&!mc.player.isInWater()&&e.getMode()==net.minecraft.client.renderer.FogRenderer.FogMode.FOG_TERRAIN){e.setNearPlaneDistance(3);e.setFarPlaneDistance(22);e.setCanceled(true);}}
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){world=null;lease=0;dayTicks=0;}
}
