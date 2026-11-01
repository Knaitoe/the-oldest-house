package io.github.knaitoe.theoldesthouse.client;

import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.PittGame;
import io.github.knaitoe.theoldesthouse.network.PittPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

/**
 * 0.4.207, the God of the Pitt on the reader's own client: Brad's brass barometer in the corner of sight while they carry it,
 * its needle in the green in the draft, swinging and shaking as it dies, hard over in the red in the still air; the dark closing
 * in while the collapse follows them; a red press at the edges while they are pinned.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class PittClient {
    private PittClient(){}
    private static int phase=-1,left,flags;private static float needle=-50,shown=-50;
    private static final RandomSource SHAKE=RandomSource.create();
    private static final ResourceLocation DIAL=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/gui/pitt_barometer.png");
    public static void set(PittPayload p){phase=p.phase();left=p.left();flags=p.flags();}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        if(phase<0)return;if(left>0)left--;
        needle=switch(phase){case PittGame.P_SAFE,PittGame.P_STILL->-50;case PittGame.P_WARN->-50+100*(1-left/(float)PittGame.WARN);default->55;};
        shown+=(needle-shown)*.25F;
    }
    @SubscribeEvent public static void out(ClientPlayerNetworkEvent.LoggingOut e){phase=-1;flags=0;}
    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Layers {
        @SubscribeEvent public static void register(RegisterGuiLayersEvent e){e.registerBelowAll(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"pitt"),(g,d)->{
            if(phase<0||(flags&8)==0)return;int w=g.guiWidth(),h=g.guiHeight();
            if((flags&4)!=0){for(int i=0;i<6;i++){int m=i*Math.min(w,h)/14;g.fill(0,0,w,m,0x28000000);g.fill(0,h-m,w,h,0x28000000);g.fill(0,0,m,h,0x28000000);g.fill(w-m,0,w,h,0x28000000);}}
            if((flags&2)!=0){int m=Math.min(w,h)/10;g.fill(0,0,w,m,0x30500000);g.fill(0,h-m,w,h,0x30500000);g.fill(0,0,m,h,0x30500000);g.fill(w-m,0,w,h,0x30500000);}
            if((flags&1)==0)return;
            int size=48,x=w-size-10,y=10;
            g.blit(DIAL,x,y,0,0,size,size,size,size);
            float jitter=phase==PittGame.P_WARN||phase==PittGame.P_CRUSH||phase==PittGame.P_COLLAPSE?(SHAKE.nextFloat()-.5F)*(phase==PittGame.P_WARN?10:16):0;
            var pose=g.pose();pose.pushPose();pose.translate(x+size/2F,y+size/2F+4,0);pose.mulPose(Axis.ZP.rotationDegrees(shown+jitter));
            g.fill(-1,-19,1,2,0xFF1A1410);g.fill(-1,-19,1,-15,shown>20?0xFFC03020:0xFF3A7A30);pose.popPose();
            g.fill(x+size/2-2,y+size/2+2,x+size/2+2,y+size/2+6,0xFF6A5030);
        });}
    }
}
