package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.gametest.GoatmanBedSocketProof;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.client.Screenshot;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class GoatmanBedSocketClient {
    private static int ticks,frames,lastClick=-100;private static boolean done;
    private GoatmanBedSocketClient(){}
    private static String role(){return System.getProperty("the_oldest_house.liveProofRole","");}
    private static void require(boolean condition,String message){if(!condition)throw new IllegalStateException("BED CAMERA137: "+message);}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        if(!GoatmanBedSocketProof.enabled())return;var mc=Minecraft.getInstance();if(done){if(++ticks>60){mc.disconnect();mc.stop();}return;}
        if(mc.player==null||mc.level==null||mc.screen!=null&&!(mc.screen instanceof net.minecraft.client.gui.screens.InBedChatScreen))return;
        var data=mc.player.getInventory().getItem(8).get(DataComponents.CUSTOM_DATA);if(data==null||!data.copyTag().getBoolean("BedCamera137"))return;ticks++;
        if(mc.player.isSleeping())return;var head=BlockPos.of(data.copyTag().getLong("Head"));var state=mc.level.getBlockState(head);if(!(state.getBlock() instanceof BedBlock)||ticks-lastClick<25)return;
        if(mc.player.position().distanceToSqr(head.getCenter())>16||!mc.player.onGround())return;
        mc.options.setCameraType(CameraType.FIRST_PERSON);lastClick=ticks;var delta=head.getCenter().subtract(mc.player.getEyePosition());mc.player.setYRot((float)Math.toDegrees(Math.atan2(-delta.x,delta.z)));mc.player.setXRot((float)-Math.toDegrees(Math.atan2(delta.y,Math.hypot(delta.x,delta.z))));
        mc.gameMode.useItemOn(mc.player,InteractionHand.MAIN_HAND,new BlockHitResult(head.getCenter(),Direction.UP,head,false));
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        if(!GoatmanBedSocketProof.enabled()||done)return;var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||!GoatmanClient.watchingFromBed())return;
        var data=mc.player.getInventory().getItem(8).get(DataComponents.CUSTOM_DATA);if(data==null||!data.copyTag().getBoolean("BedCamera137"))return;
        if(++frames==12){
            require(mc.screen instanceof net.minecraft.client.gui.screens.InBedChatScreen,"native sleeping screen is open");
            var bed=(net.minecraft.client.gui.screens.InBedChatScreen)mc.screen;
            for(var child:bed.children())if(child instanceof net.minecraft.client.gui.components.Button button)button.onPress();
            bed.keyPressed(256,0,0);bed.onClose();
        }
        if(frames<48)return;require(mc.player.isSleeping()&&mc.screen instanceof net.minecraft.client.gui.screens.InBedChatScreen,"button, Escape and close attempts retain native sleep");
        var tag=data.copyTag();var head=mc.player.getSleepingPos().orElseThrow();var state=mc.level.getBlockState(head);require(state.getValue(BedBlock.PART)==BedPart.HEAD,"the actual player lies at a native pillow");var foot=head.relative(state.getValue(BedBlock.FACING).getOpposite());double center=tag.getDouble("CenterX");
        require(Math.abs(foot.getX()+.5-center)<Math.abs(head.getX()+.5-center),"the pillow is outward and the feet inward");
        var camera=mc.gameRenderer.getMainCamera();var look=camera.getLookVector();double expected=Math.signum(center-camera.getPosition().x);require(look.x()*expected>.98&&Math.abs(look.z())<.05,"the actual rendered native camera faces the center table, not the window");
        var folder=GoatmanBedSocketProof.folder();Files.createDirectories(folder);try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(folder.resolve(role()+"-bed-camera.png"));}
        Files.writeString(folder.resolve(role()+"-bed-camera.txt"),"Actual native head="+head+" foot="+foot+" camera="+camera.getPosition()+" look="+look+" centerX="+center+" sleeping="+mc.player.isSleeping()+" after native button, Escape and close attempts. No post-sleep player aim or camera override exists in this proof; the production Camera.setup TAIL supplies the view.\n");
        mc.getConnection().sendCommand("goatbedproof137");done=true;ticks=0;
    }
}
