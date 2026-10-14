package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.gametest.LiveExpeditionProof;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/** Native boat input, movement, breath HUD and painted actor frames from connected clients. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class CanoeSocketClient {
    private static int step,age,verified;private static float health;private static UUID canoe;private static String role,pending;
    private static BlockPos base;private static final Set<String> SHOTS=new HashSet<>();
    private CanoeSocketClient(){}
    public static boolean ready(Minecraft mc,int next,int ticks,String viewer,BlockPos at){
        if(step!=next){step=next;verified=0;pending=null;mc.options.setCameraType(CameraType.FIRST_PERSON);}age=ticks;role=viewer;base=at;
        mc.options.keyUp.setDown(false);mc.options.keyShift.setDown(false);mc.options.keySprint.setDown(false);
        if(verified==next)return true;
        if(next==47){if(ticks>40&&mc.player.getVehicle() instanceof LakeHouseCanoe c){canoe=c.getUUID();health=mc.player.getHealth();return true;}return false;}
        if(next==48||next==49){
            if(!(mc.player.getVehicle() instanceof LakeHouseCanoe c)||!c.getUUID().equals(canoe))return false;
            if(ticks>40&&(mc.player.getHealth()!=health||mc.player.isInWall()||mc.player.isEyeInFluid(FluidTags.WATER)||mc.player.getAirSupply()!=mc.player.getMaxAirSupply()))throw new IllegalStateException("LIVE CANOE CLIENT: the real rider suffocates or loses breath; boat="+c.position()+" body="+mc.player.position()+" pose="+mc.player.getPose()+" air="+mc.player.getAirSupply());
            if(next==48){mc.player.setYRot(180);mc.player.setXRot(8);if(ticks>=340&&c.underLowRoof()&&mc.player.getForcedPose()==Pose.CROUCHING)pending="canoe-under-roof";}
            else{
                mc.player.setYRot(180);mc.player.setXRot(8);mc.options.keyUp.setDown(true);
                float reach=(float)Math.sin(Math.min(1,c.pushAge(0)/LakeHouseCanoe.STROKE_TICKS)*Math.PI);
                if(c.underLowRoof()&&c.pushSide()!=0&&reach>.4){var label=c.pushSide()<0?"canoe-left-hand":"canoe-right-hand";if(!SHOTS.contains(label))pending=label;}
                if(c.getZ()<at.getZ()+LakeHouse.CHANNEL_NORTH-8&&!c.underLowRoof()&&mc.player.getForcedPose()==null){
                    mc.options.keyUp.setDown(false);mc.player.setYRot(225);mc.player.setXRot(18);mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
                    if(SHOTS.contains("canoe-left-hand")&&SHOTS.contains("canoe-right-hand"))pending="canoe-seated-open-water";
                }
            }
        }
        if(next==50){mc.options.setCameraType(CameraType.FIRST_PERSON);mc.player.setYRot(180);mc.player.setXRot(80);mc.options.keyShift.setDown(true);mc.options.keyUp.setDown(!mc.player.isPassenger());
            if(ticks>45&&!mc.player.isPassenger()&&mc.player.getForcedPose()==null&&mc.player.isEyeInFluid(FluidTags.WATER)&&mc.player.getAirSupply()<mc.player.getMaxAirSupply()-20&&mc.player.getAirSupply()>0&&mc.player.getHealth()==health){mc.options.keyUp.setDown(false);mc.options.keyShift.setDown(false);pending="canoe-dismounted-dive";}
        }
        if(next==51){
            if(viewer.equals("B")){if(ticks>80)pending="canoe-peer-waiting";}
            else{
                var actors=mc.level.getEntitiesOfClass(LiteraryActor.class,new AABB(mc.player.blockPosition()).inflate(48),a->a.role()==LiteraryActor.LAKE_TESS&&a.owner().filter(mc.player.getUUID()::equals).isPresent());
                if(actors.size()==1){var actor=actors.getFirst();var to=actor.getEyePosition().subtract(mc.player.getEyePosition());mc.player.setYRot((float)Math.toDegrees(Math.atan2(-to.x,to.z)));mc.player.setXRot((float)-Math.toDegrees(Math.atan2(to.y,Math.hypot(to.x,to.z))));
                    if(ticks>100&&actor.phase()==2&&actor.getY()>at.getY()-20&&actor.distanceToSqr(mc.player)<64&&mc.player.hasLineOfSight(actor))pending="tess-swimming-stair";
                }
            }
        }
        return verified==next;
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post event)throws Exception{
        if(!LiveExpeditionProof.enabled()||pending==null)return;var label=pending;var mc=Minecraft.getInstance();Files.createDirectories(LiveExpeditionProof.folder());
        try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(LiveExpeditionProof.folder().resolve(role+"-"+label+".png"));}
        Files.writeString(LiveExpeditionProof.folder().resolve(role+"-"+label+".txt"),"Actual connected survival client. phase="+step+" age="+age+" health="+mc.player.getHealth()+" air="+mc.player.getAirSupply()+" body="+mc.player.position()+" eyes="+mc.player.getEyeY()+" pose="+mc.player.getPose()+" forced="+mc.player.getForcedPose()+"\n");SHOTS.add(label);pending=null;
        if(step==48||step==50||step==51||step==49&&label.equals("canoe-seated-open-water"))verified=step;
    }
}
