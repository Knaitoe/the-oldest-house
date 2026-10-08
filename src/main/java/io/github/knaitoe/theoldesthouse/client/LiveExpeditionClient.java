package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.gametest.LiveExpeditionProof;
import io.github.knaitoe.theoldesthouse.labyrinth.StaircaseFire;
import java.nio.file.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.inventory.LecternScreen;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

/** Opt-in actual native input/menu/payload proof; never runs in user sessions. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class LiveExpeditionClient {
    private static int previous,ticks,clicked=-100,ack=-1;private static String shot;private static boolean stopping;
    @SubscribeEvent public static void tick(ClientTickEvent.Post e) {
        if(!LiveExpeditionProof.enabled())return;var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.gameMode==null)return;
        var component=mc.player.getInventory().getItem(8).get(DataComponents.CUSTOM_DATA);if(component==null)return;
        var data=component.copyTag();int step=data.getInt("Step");if(step<1)return;String role=System.getProperty("the_oldest_house.liveProofRole","");
        if(previous!=step){previous=step;ticks=0;clicked=-100;ack=-1;mc.options.keyUp.setDown(false);mc.options.keyShift.setDown(false);}ticks++;
        BlockPos target=BlockPos.of(data.getLong("Target"));var block=mc.level.getBlockState(target);
        if(step==1&&ticks>30&&mc.level.players().size()>=2)ack(mc,1);
        if(step==2) {
            if(mc.player.distanceToSqr(target.getCenter())<25)click(mc,target,ticks);
            else if(ticks>30) {
                BlockPos entry=null;
                for(var p:BlockPos.betweenClosed(mc.player.blockPosition().offset(-3,-1,-5),mc.player.blockPosition().offset(3,1,5)))
                    if(mc.level.getBlockState(p).getBlock() instanceof DoorBlock&&mc.level.getBlockState(p).getValue(DoorBlock.HALF)==net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER){entry=p.immutable();break;}
                if(entry!=null)walk(mc,entry.north(4));else {mc.player.setYRot(180);mc.options.keyUp.setDown(true);}
            }
        }
        if(step==3) {
            mc.options.keyUp.setDown(false);if(role.equals("A")&&!open(block))click(mc,target,ticks);
            if(ticks>35&&open(block))ack(mc,3);
        }
        if(step==4) {
            // Stop real input once the native return packet puts us on the source floor.
            // The other reader may still be crossing; their progress cannot restart our walk.
            if(Math.abs(mc.player.getY()-target.getY())>2)mc.options.keyUp.setDown(false);
            else walk(mc,target.south(7));
        }
        if(step==5) {
            mc.options.keyUp.setDown(false);
            if(mc.screen instanceof LecternScreen)mc.gameMode.handleInventoryButtonClick(mc.player.containerMenu.containerId,3);
            else if(mc.screen==null)click(mc,target,ticks);
        }
        if(step==6||step==8) {
            mc.options.keyUp.setDown(false);boolean burner=step==8||role.equals("A");
            if(burner&&ticks>15&&!BurnEmbersClient.showing())click(mc,target,ticks);
            if(burner&&BurnEmbersClient.showing()) {
                if(!BurnEmbersClient.current().text().equals(data.getString("Expected")))throw new IllegalStateException("LIVE EXPEDITION private excerpt differs from own saved words");
                if(ticks>25){shot=role+"-embers"+(step==8?"-reconnected":"");ack(mc,step);}
            }else if(!burner&&ticks>50&&block.is(Blocks.CAMPFIRE)&&block.getValue(CampfireBlock.LIT)) {
                if(BurnEmbersClient.showing())throw new IllegalStateException("LIVE EXPEDITION another reader's private smoke reached peer");ack(mc,step);
            }
        }
        if(step==7){mc.options.keyUp.setDown(false);if(ticks==21){shot=role+"-first";stopping=role.equals("B");}}
        if(step==9){
            mc.options.keyUp.setDown(false);
            if(mc.screen instanceof LecternScreen){
                var book=mc.player.containerMenu.getSlot(0).getItem().get(DataComponents.WRITTEN_BOOK_CONTENT);
                if(book!=null&&ticks==45&&book.pages().size()>1)mc.gameMode.handleInventoryButtonClick(mc.player.containerMenu.containerId,100+book.pages().size()-1);
                if(ticks==80)mc.player.closeContainer();
            }else if(ticks<70&&mc.screen==null)click(mc,target,ticks);
        }
        if(step==10){
            mc.options.keyUp.setDown(false);
            if(ticks==30)click(mc,target.offset(-2,1,-5),ticks);
            if(ticks==65)click(mc,target.offset(-2,0,-5),ticks);
            if(ticks>95&&mc.level.getBlockState(target.offset(-2,0,-5)).is(io.github.knaitoe.theoldesthouse.labyrinth.StaircaseLeakRegistry.PROP.get())
                    &&mc.level.getBlockState(target.offset(-2,0,-5)).getValue(io.github.knaitoe.theoldesthouse.labyrinth.StaircaseLeakProps.KIND)==io.github.knaitoe.theoldesthouse.labyrinth.StaircaseLeakProps.Kind.DRAWER_OPEN){
                if(!StaircaseLeakClient.active())throw new IllegalStateException("LIVE EXPEDITION private scene lease missing");
                if(ack!=10){shot=role+"-leak";ack(mc,10);}
            }
        }
        if(step==11){
            if(role.equals("A")){if(mc.player.distanceToSqr(target.getCenter())<100){if(!open(block))click(mc,target,ticks);walk(mc,target.south(3));}else mc.options.keyUp.setDown(false);}
            else{mc.options.keyUp.setDown(false);if(ticks==25){shot="B-leak-first";stopping=true;}}
        }
        if(step==12)mc.options.keyUp.setDown(false);
        if(step==13){mc.options.keyUp.setDown(false);if(ticks==30){if(StaircaseLeakClient.active())throw new IllegalStateException("LIVE EXPEDITION scene lease survived return");shot=role+"-reconnected";ack(mc,13);}}
        if(step==14||step==15){mc.options.keyUp.setDown(false);if(ticks>15){double dx=target.getX()+.5-mc.player.getX(),dz=target.getZ()+.5-mc.player.getZ();mc.player.setYRot((float)Math.toDegrees(Math.atan2(-dx,dz)));mc.player.setXRot(0);}
            if(step==15&&ticks==40){shot=role+"-stacy-door";ack(mc,15);}}
        if(step==16){mc.options.keyShift.setDown(true);if(mc.player.position().distanceToSqr(net.minecraft.world.phys.Vec3.atBottomCenterOf(target))>.16)walk(mc,target);
            else{mc.options.keyUp.setDown(false);if(ticks>25&&ack!=16){shot=role+"-leaf-cover";ack(mc,16);}}}
        if(step==17){mc.options.keyUp.setDown(false);mc.options.keyShift.setDown(true);double dx=target.getX()+.5-mc.player.getX(),dz=target.getZ()+.5-mc.player.getZ();mc.player.setYRot((float)Math.toDegrees(Math.atan2(-dx,dz)));mc.player.setXRot(0);}
        if(step==18){mc.options.keyUp.setDown(false);mc.options.keyShift.setDown(true);if(ticks==30){shot=role+"-slasher-leaves";stopping=true;}}
    }
    private static boolean open(net.minecraft.world.level.block.state.BlockState s){return s.getBlock() instanceof DoorBlock&&s.getValue(DoorBlock.OPEN);}
    private static void walk(Minecraft mc,BlockPos target) {
        double dx=target.getX()+.5-mc.player.getX();double dz=Math.abs(dx)>.12?0:target.getZ()+.5-mc.player.getZ();
        mc.player.setYRot((float)Math.toDegrees(Math.atan2(-dx,dz)));mc.player.setXRot(0);mc.options.keyUp.setDown(true);
    }
    private static void click(Minecraft mc,BlockPos pos,int age) {
        if(age-clicked<30)return;clicked=age;
        mc.gameMode.useItemOn(mc.player,InteractionHand.MAIN_HAND,new BlockHitResult(pos.getCenter(),Direction.SOUTH,pos,false));
    }
    private static void ack(Minecraft mc,int step){if(ack!=step){ack=step;mc.player.connection.sendCommand("othproof "+step);}}
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception {
        if(!LiveExpeditionProof.enabled()||shot==null)return;var mc=Minecraft.getInstance();Files.createDirectories(LiveExpeditionProof.folder());
        try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(LiveExpeditionProof.folder().resolve(shot+".png"));}
        Files.writeString(LiveExpeditionProof.folder().resolve(shot+".txt"),"Actual connected native client rendered and acknowledged this phase.\n");shot=null;if(stopping)mc.stop();
    }
}
