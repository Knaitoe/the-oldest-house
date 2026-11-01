package io.github.knaitoe.theoldesthouse.client;

import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.LateBodiesPayload;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

/**
 * 0.4.216, your own light's cost on the client (docs/OWN_LIGHT_0_4_213.md): a reader whose double went home before them is drawn,
 * now and then, a beat behind their own body, the way the double moved in the hall. Only other readers see it: the reader's own
 * client draws them as they are. A spell lasts about four seconds, comes round every twenty-five or so at a moment of its own for
 * each reader, and eases in and out; inside it the body is where the reader was eight ticks before, turned as they were turned.
 * Nothing about the reader moves on the server.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class LateBodyClient {
    private LateBodyClient(){}
    /** How far behind: a beat. */
    public static final int BEAT=8;
    static final int PERIOD=500,SPELL=90,EASE=15,KEEP=BEAT+4;
    /** Further than this between then and now is a teleport or a door, not a step: drawn as they are. */
    static final double STEP=4;
    private record Was(double x,double y,double z,float body){}
    private static final Set<UUID> LATE=new HashSet<>();
    private static final Map<UUID,ArrayList<Was>> TRAILS=new HashMap<>();
    private static final Set<Integer> PUSHED=new HashSet<>();

    public static void accept(LateBodiesPayload payload){LATE.clear();LATE.addAll(payload.readers());TRAILS.keySet().retainAll(LATE);}
    public static boolean late(UUID reader){return LATE.contains(reader);}
    /** How far into a spell a reader is at a time: 0 outside one, rising to 1 and falling back. */
    public static float spell(UUID reader,long time){
        long t=Math.floorMod(time+(reader.hashCode()&0xffff),PERIOD);if(t>=SPELL)return 0;
        return Math.min(1F,Math.min(t,SPELL-t)/(float)EASE);
    }

    /** Where each late reader has been, tick by tick, kept for a beat and a little more. */
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        var mc=Minecraft.getInstance();var level=mc.level;if(level==null){TRAILS.clear();return;}
        for(var id:LATE){var player=level.getPlayerByUUID(id);
            if(player==null||player==mc.player){TRAILS.remove(id);continue;}
            var trail=TRAILS.computeIfAbsent(id,k->new ArrayList<>());trail.add(new Was(player.getX(),player.getY(),player.getZ(),player.yBodyRot));
            while(trail.size()>KEEP)trail.remove(0);}
    }

    @SubscribeEvent(priority=EventPriority.LOWEST) public static void before(RenderPlayerEvent.Pre e){
        var mc=Minecraft.getInstance();var player=e.getEntity();if(mc.level==null||player==mc.player||!LATE.contains(player.getUUID()))return;
        var trail=TRAILS.get(player.getUUID());if(trail==null||trail.size()<BEAT+2)return;
        float s=spell(player.getUUID(),mc.level.getGameTime());if(s<=0)return;
        float partial=e.getPartialTick();int i=trail.size()-1-BEAT;var a=trail.get(i-1);var b=trail.get(i);
        double dx=Mth.lerp(partial,a.x(),b.x())-Mth.lerp(partial,player.xo,player.getX());
        double dy=Mth.lerp(partial,a.y(),b.y())-Mth.lerp(partial,player.yo,player.getY());
        double dz=Mth.lerp(partial,a.z(),b.z())-Mth.lerp(partial,player.zo,player.getZ());
        if(dx*dx+dy*dy+dz*dz>STEP*STEP)return;
        float turn=Mth.wrapDegrees(Mth.rotLerp(partial,a.body(),b.body())-Mth.rotLerp(partial,player.yBodyRotO,player.yBodyRot));
        var pose=e.getPoseStack();pose.pushPose();pose.translate(dx*s,dy*s,dz*s);pose.mulPose(Axis.YP.rotationDegrees(-turn*s));PUSHED.add(player.getId());
    }
    @SubscribeEvent public static void after(RenderPlayerEvent.Post e){if(PUSHED.remove(e.getEntity().getId()))e.getPoseStack().popPose();}
    @SubscribeEvent public static void left(ClientPlayerNetworkEvent.LoggingOut e){LATE.clear();TRAILS.clear();PUSHED.clear();}
}
