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
        if(previous!=step){previous=step;ticks=0;clicked=-100;ack=-1;mc.options.keyUp.setDown(false);}ticks++;
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
        if(step==4) {walk(mc,target.south(7));}
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
        if(step==7||step==9){mc.options.keyUp.setDown(false);if(ticks>20){shot=role+(step==7?"-first":"-reconnected");stopping=true;}}
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
