package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.ElkDrag;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * What the reader sees of their own drag (0.4.120): their sight going in pulses, then coming back in snatches while they
 * are pulled along on their back, the ground jolting under them on every heave, then black; and on waking, the dark
 * lifting slowly inside the pile. The server carries the camera; this is only the reader's eyes.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class ElkDragClient {
    private static int t,phase=-1,lease,waking;
    private ElkDragClient(){}

    public static void accept(CompoundTag models){
        if(!models.contains("ElkDrag"))return;var drag=models.getCompound("ElkDrag");
        if(drag.getInt("Phase")==3){phase=-1;lease=0;waking=60;return;}
        t=drag.getInt("T");phase=drag.getInt("Phase");lease=12;waking=0;
    }
    public static boolean active(){return lease>0&&phase>=0;}

    /** How dark the screen is, 0 to 1: blackout pulses, the drag's lapses on each heave and between, then black. */
    static float darkness(int tick,int stage,float partial){
        float time=tick+partial;
        if(stage==0){float pulse=Mth.abs(Mth.sin(time*.13F));return Mth.clamp(.72F+.28F*pulse+time/ElkDrag.BLACKOUT*.1F,0,1);}
        if(stage==2)return 1;
        float k=time-ElkDrag.BLACKOUT;
        // Coming round at the start, fading at the end; between, a lapse every few heaves and a dimming on each one.
        float rise=Mth.clamp(1-k/40F,0,1),fall=Mth.clamp((k-(ElkDrag.DRAGGING-30))/30F,0,1);
        float lapse=Mth.clamp(Mth.sin((k-55)/75F*Mth.PI*2)*1.6F-.9F,0,1);
        float heave=.18F*Mth.abs(Mth.sin(k/24F*Mth.PI));
        return Mth.clamp(.08F+heave+lapse*.8F+rise*.9F+fall,0,1);
    }

    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        if(lease>0){lease--;if(phase>=0)t++;}
        if(waking>0)waking--;
        var mc=Minecraft.getInstance();if(mc.player==null){lease=0;waking=0;}
    }
    @SubscribeEvent public static void hideHud(RenderGuiLayerEvent.Pre e){if(active())e.setCanceled(true);}
    @SubscribeEvent public static void overlay(RenderGuiEvent.Post e){
        var mc=Minecraft.getInstance();if(mc.player==null)return;float alpha;
        if(active())alpha=darkness(t,phase,e.getPartialTick().getGameTimeDeltaPartialTick(false));
        else if(waking>0)alpha=Mth.clamp(waking/60F,0,1);
        else return;
        var g=e.getGuiGraphics();int w=mc.getWindow().getGuiScaledWidth(),h=mc.getWindow().getGuiScaledHeight();
        g.fill(0,0,w,h,((int)(alpha*255)<<24)|0x050101);
        // A dark red-black edge closing in, thickest while dragged.
        if(active()&&phase==1){int edge=Math.max(8,h/5);for(int i=0;i<edge;i+=2){int a=(int)(150*(1-i/(float)edge));int c=(a<<24)|0x1a0303;g.fill(0,i,w,i+2,c);g.fill(0,h-i-2,w,h-i,c);g.fill(i,0,i+2,h,c);g.fill(w-i-2,0,w-i,h,c);}}
    }
    /** The body bumps over the ground on every heave and rolls a little; the reader cannot steer. */
    @SubscribeEvent public static void camera(ViewportEvent.ComputeCameraAngles e){
        if(!active()||phase!=1)return;float k=(float)(t-ElkDrag.BLACKOUT+e.getPartialTick());
        float heave=Mth.sin(k/24F*Mth.PI);
        e.setPitch(e.getPitch()+heave*heave*3.5F+Mth.sin(k*.9F)*.6F);e.setRoll(Mth.sin(k*.11F)*4F+Mth.sin(k*.37F)*1.2F);e.setYaw(e.getYaw()+Mth.sin(k*.07F)*5F);
    }
    @SubscribeEvent public static void still(MovementInputUpdateEvent e){
        if(!active())return;var in=e.getInput();in.forwardImpulse=0;in.leftImpulse=0;in.up=in.down=in.left=in.right=false;in.jumping=false;in.shiftKeyDown=false;
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){phase=-1;lease=0;waking=0;}
}
