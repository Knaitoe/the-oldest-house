package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.BorrascaFeeding;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryActor;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.jetbrains.annotations.Nullable;

/**
 * 0.4.439, the owner: "Can you overlay the machinery with the shiny gentleman 'eating' her? ... Just a flash for about two seconds, not the
 * whole feeding sequence overlayed. Ensure the players screen is pulsing/flashing a dull angry red as she's ate by the orrery".
 * <p>
 * Everything here is read from Dana's own synced state (LiteraryActor.torn and her gesture), on this client only; the server sends nothing
 * new. While the machine is still taking her the screen throbs a dull red, harder at each lurch, and lets go once the belt has not moved
 * her for five seconds (the breaker, or the end). When the machine reaches her chest ({@link BorrascaFeeding#GENTLEMAN_AT}) with the slot
 * in this reader's sight, for two seconds the machine is not there and the Shiny Gentleman is, eating her: the machine's renderer draws him
 * in its place ({@link #at}, BorrascaOrrery.devour), and then he is gone and it is turning again.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class BorrascaFeedingClient {
    private BorrascaFeedingClient(){}
    /** How long he is there, in ticks. */
    public static final int FLASH=40;
    /** After the belt has not taken any more of her for this long, the red lets go. */
    static final int STILL=100;
    private static final Map<Integer,long[]> seen=new HashMap<>();
    private static long flashAt=Long.MIN_VALUE,lastTaken=Long.MIN_VALUE;private static float spike;
    private static Vec3 flashSlot=Vec3.ZERO;

    /** Where the slot cuts her: from her feet, back along her length by as much as the machine has taken. */
    static Vec3 slot(LiteraryActor a){float yaw=a.getYRot()*Mth.DEG_TO_RAD;double d=a.torn()/1000D;return a.position().add(Mth.sin(yaw)*d,.25,-Mth.cos(yaw)*d);}
    static boolean inSight(Vec3 at){
        var mc=Minecraft.getInstance();var eye=mc.gameRenderer.getMainCamera().getPosition();if(eye.distanceToSqr(at)>48*48)return false;
        var ray=mc.level.clip(new ClipContext(eye,at,ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,mc.player));return ray.getType()==HitResult.Type.MISS||ray.getLocation().distanceToSqr(at)<2.25;
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null){seen.clear();flashAt=lastTaken=Long.MIN_VALUE;return;}
        long now=mc.level.getGameTime();spike=Math.max(0,spike-.1F);
        for(var a:mc.level.getEntitiesOfClass(LiteraryActor.class,mc.player.getBoundingBox().inflate(64),BorrascaNativeArt::torn)){
            if(a.isInvisible())continue;int taken=a.torn();var was=seen.get(a.getId());
            if(was==null){seen.put(a.getId(),new long[]{taken,now});lastTaken=Math.max(lastTaken,now);continue;}
            if(taken>was[0]){
                if(taken-was[0]>=50)spike=1; // a lurch: the belt jumps as the bone gives
                int at=(int)Math.round(BorrascaFeeding.GENTLEMAN_AT*1000);
                if(was[0]<at&&taken>=at&&inSight(slot(a))){flashAt=now;flashSlot=slot(a);spike=1;}
                was[0]=taken;was[1]=now;lastTaken=now;
            }
        }
        if(now%200==0)seen.keySet().removeIf(id->mc.level.getEntity(id)==null);
    }
    /** How hard the red is now, 0 to 1, before the reader's comfort setting. */
    static float red(float partial){
        var mc=Minecraft.getInstance();if(mc.level==null)return 0;float since=mc.level.getGameTime()+partial-lastTaken;if(since<0||since>STILL)return 0;
        float t=mc.level.getGameTime()+partial;
        // A slow heartbeat: two beats close together, then a pause.
        float beat=(float)Math.pow(Math.max(0,Mth.sin(t*.42F)),8)+.6F*(float)Math.pow(Math.max(0,Mth.sin(t*.42F-.75F)),8);
        float fade=since<STILL-20?1:(STILL-since)/20F;
        return Mth.clamp((.35F+.45F*beat+.6F*spike)*fade,0,1);
    }
    @SubscribeEvent public static void wash(RenderGuiEvent.Post e){
        var mc=Minecraft.getInstance();float r=red(e.getPartialTick().getGameTimeDeltaPartialTick(false))*Comfort.effects();if(r<=0)return;
        var g=e.getGuiGraphics();int w=g.guiWidth(),h=g.guiHeight();
        g.fill(0,0,w,h,(int)(r*95)<<24|0x6E120D);
        // Darker toward the edges, a band at a time.
        for(int k=0;k<6;k++){int in=k*Math.max(4,Math.min(w,h)/28),a=(int)(r*(26+6*(5-k)))<<24|0x4A0906;
            g.fill(0,in,w,in+Math.max(4,Math.min(w,h)/28),a);g.fill(0,h-in-Math.max(4,Math.min(w,h)/28),w,h-in,a);
            g.fill(in,0,in+Math.max(4,Math.min(w,h)/28),h,a);g.fill(w-in-Math.max(4,Math.min(w,h)/28),0,w-in,h,a);}
    }
    /** He is at a machine: for {@code age} ticks so far, her slot at {@code slot} from the machine block's corner. */
    public record Flash(float age,Vec3 slot){}
    /** If he is at this machine for this reader now, for how long and where her slot is; otherwise null. */
    public static @Nullable Flash at(BlockPos machine,float partial){
        var mc=Minecraft.getInstance();if(mc.level==null||flashAt==Long.MIN_VALUE)return null;
        float age=mc.level.getGameTime()+partial-flashAt;if(age<0||age>=FLASH)return null;
        if(flashSlot.distanceToSqr(Vec3.atCenterOf(machine))>8*8)return null;
        return new Flash(age,flashSlot.subtract(Vec3.atLowerCornerOf(machine)));
    }
}
