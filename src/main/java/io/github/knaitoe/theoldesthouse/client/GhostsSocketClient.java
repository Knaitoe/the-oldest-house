package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.gametest.LiveExpeditionProof;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.Files;
import java.util.UUID;
import net.minecraft.client.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/** Opt-in native movement, held lens and original-body capture; inert in ordinary sessions. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class GhostsSocketClient {
    private static int step,heldTicks;private static boolean shot,done;private static UUID original;private static Vec3 heldBody,heldEye;private static float head;private static String role,pending;private static BlockPos site;
    private GhostsSocketClient(){}
    private static void require(boolean okay,String words){if(!okay)throw new IllegalStateException("LIVE GHOSTS CLIENT: "+words);}
    public static boolean ready(Minecraft mc,int next,int age,String reader,BlockPos at){
        if(step!=next){step=next;done=false;pending=null;mc.options.keyUp.setDown(false);if(next==52){mc.player.getInventory().selected=0;original=null;heldTicks=0;heldBody=heldEye=null;shot=false;}}
        role=reader;site=at;mc.options.keySprint.setDown(false);mc.options.keyShift.setDown(false);mc.options.keyUse.setDown(false);if(done)return true;
        if(next==53){mc.options.keyUp.setDown(false);if(age>20&&!GhostsTakeClient.frozen()&&GhostsTakeClient.day()==0&&mc.getCameraEntity()==mc.player&&mc.player.distanceToSqr(at.getCenter())>32*32)pending="ghosts-restored-camera";return false;}
        var cast=mc.level.getEntitiesOfClass(LiteraryActor.class,new AABB(at.offset(-21,-5,-47),at.offset(22,13,1)),a->a.role()==LiteraryActor.GHOST_MARJORIE);
        if(cast.isEmpty())return false;require(cast.size()==1&&cast.getFirst().owner().filter(mc.player.getUUID()::equals).isPresent(),"the client receives only its own native Marjorie");var m=cast.getFirst();if(original==null)original=m.getUUID();require(original.equals(m.getUUID()),"the approach and held jump keep the original client UUID");
        if(!mc.player.isUsingItem()&&age>10)mc.gameMode.useItem(mc.player,InteractionHand.MAIN_HAND);
        if(!GhostsTakeClient.frozen()){
            Vec3 goal=mc.player.getZ()<at.getZ()-24?new Vec3(at.getX()-9.5,at.getY()+5,at.getZ()-22.5):new Vec3(at.getX()+(reader.equals("A")?-7.5:-11.5),at.getY()+5,at.getZ()-18.5);
            Vec3 delta=goal.subtract(mc.player.position());mc.player.setYRot((float)Math.toDegrees(Math.atan2(-delta.x,delta.z)));mc.player.setXRot(0);mc.options.keyUp.setDown(delta.horizontalDistanceSqr()>.09);
            if(m.phase()==GhostsSet.STAND&&!shot&&m.getZ()>at.getZ()-27){pending="ghosts-native-approach";shot=true;}
            return false;
        }
        mc.options.keyUp.setDown(false);require(m.phase()==GhostsSet.LEAP&&m.getY()>at.getY()+6&&m.getZ()>at.getZ()-15,"the camera handoff waits for the real original beyond the native banister");
        if(heldBody==null){heldBody=m.position();heldEye=mc.getCameraEntity().position();head=m.getYHeadRot();}
        require(m.position().distanceToSqr(heldBody)<.002&&mc.getCameraEntity()!=mc.player&&mc.getCameraEntity().position().distanceToSqr(heldEye)<.000001,"the original body and reader's native held lens remain fixed");
        if(++heldTicks>=43){require(Math.abs(net.minecraft.util.Mth.wrapDegrees(m.getYHeadRot()-head))>10,"only the original head turns toward this reader's lens during the hold");pending="ghosts-held-banister-jump";}
        return false;
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post ignored)throws Exception{
        if(!LiveExpeditionProof.enabled()||pending==null)return;var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.getOverlay()!=null)return;String label=pending;
        Files.createDirectories(LiveExpeditionProof.folder());try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(LiveExpeditionProof.folder().resolve(role+"-"+label+".png"));}
        Files.writeString(LiveExpeditionProof.folder().resolve(role+"-"+label+".txt"),"Actual connected client; explicit day-six fixture. original="+original+" heldBody="+heldBody+" heldEye="+heldEye+" nativeCamera="+mc.gameRenderer.getMainCamera().getPosition()+" heldTicks="+heldTicks+" frozen="+GhostsTakeClient.frozen()+" reader="+mc.player.position()+"\n");pending=null;if(!label.equals("ghosts-native-approach"))done=true;
    }
}
