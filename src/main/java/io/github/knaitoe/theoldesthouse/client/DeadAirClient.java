package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.DeadAirGame;
import io.github.knaitoe.theoldesthouse.network.DeadAirPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

/**
 * 0.4.206, the Dead Air Broadcast on the reader's own client: a faint wash of the channel's colour over sight (magenta on the
 * stage, green on the marsh), and in dead air the blue of an empty screen, with snow creeping in over its last two seconds.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class DeadAirClient {
    private DeadAirClient(){}
    private static int channel=-1,dead;
    private static final RandomSource SNOW=RandomSource.create();
    public static void set(DeadAirPayload p){channel=p.channel();dead=p.dead();}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){if(dead>0)dead--;}
    @SubscribeEvent public static void out(ClientPlayerNetworkEvent.LoggingOut e){channel=-1;dead=0;}
    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Layers {
        @SubscribeEvent public static void register(RegisterGuiLayersEvent e){e.registerBelowAll(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"dead_air"),(g,d)->{
            if(channel<0)return;int w=g.guiWidth(),h=g.guiHeight();
            if(dead>0){g.fill(0,0,w,h,0x3000E5FF);
                if(dead<=DeadAirGame.CREEP){float k=(1-dead/(float)DeadAirGame.CREEP)*Comfort.effects();int n=(int)(k*k*900);
                    for(int i=0;i<n;i++){int x=SNOW.nextInt(w),y=SNOW.nextInt(h),v=SNOW.nextInt(256);g.fill(x,y,x+2,y+2,(int)(k*180)<<24|v<<16|v<<8|v);}}
                return;}
            if(channel==DeadAirGame.STAGE)g.fill(0,0,w,h,0x14C8328C);else if(channel==DeadAirGame.MARSH)g.fill(0,0,w,h,0x1032CD32);
        });}
    }
}
