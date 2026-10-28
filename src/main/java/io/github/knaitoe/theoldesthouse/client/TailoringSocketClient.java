package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.gametest.TailoringSocketProof;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.inventory.LecternScreen;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.LecternMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

/** Real native entity visibility, same-profile reconnect, menu input and production framebuffer capture. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class TailoringSocketClient {
    private static int phase,age,clicked=-100,ack=-1;private static String shot;private static boolean stopping,sawFirst;
    private TailoringSocketClient(){}
    private static String role(){return System.getProperty("the_oldest_house.liveProofRole","");}
    private static void require(boolean okay,String text){if(!okay)throw new IllegalStateException("TAILORING CLIENT: "+text);}
    private static void look(Minecraft mc,Vec3 at){var d=at.subtract(mc.player.getEyePosition());mc.player.setYRot((float)(Math.atan2(d.z,d.x)*180/Math.PI)-90);mc.player.setXRot((float)(-Math.atan2(d.y,Math.sqrt(d.x*d.x+d.z*d.z))*180/Math.PI));}
    private static RoutineEchoDisplay own(Minecraft mc,BlockPos at,CompoundTag tag){
        var found=new ArrayList<RoutineEchoDisplay>();for(var e:mc.level.entitiesForRendering())if(e instanceof RoutineEchoDisplay r&&r.distanceToSqr(at.getCenter())<9)found.add(r);
        if(found.isEmpty())return null;require(found.size()==1,"the actual peer's private display must not arrive or render at the same counter");var item=found.getFirst();require(tag.hasUUID("Own")&&item.getUUID().equals(tag.getUUID("Own")),"the sole native display is this reader's saved original");
        var actual=ItemStack.parseOptional(mc.level.registryAccess(),item.saveWithoutId(new CompoundTag()).getCompound("item"));require(actual.is(HouseReadRegistry.ROUTINES.get(role().equals("A")?0:6).get()),"the actual native item metadata carries this reader's distinct hair echo");return item;
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){if(!TailoringSocketProof.enabled())return;var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.gameMode==null)return;
        var data=mc.player.getInventory().getItem(8).get(DataComponents.CUSTOM_DATA);if(data==null)return;var tag=data.copyTag();int next=tag.getInt("TailoringStep");if(next==0)return;
        if(next!=phase){phase=next;age=0;clicked=-100;ack=-1;shot=null;stopping=false;}age++;mc.options.keyUp.setDown(false);mc.options.keyShift.setDown(false);mc.options.keyJump.setDown(false);mc.options.setCameraType(CameraType.FIRST_PERSON);
        if(phase==5){mc.stop();return;}if(ack==phase)return;if(mc.screen instanceof net.minecraft.client.gui.screens.ReceivingLevelScreen)return;var at=BlockPos.of(tag.getLong("Target"));
        if(phase<=3){
            var original=own(mc,at,tag);if(original==null||!mc.levelRenderer.isSectionCompiled(at)||age<50)return;look(mc,original.position().add(0,.025,0));
            if(phase==1){sawFirst=true;shot=role()+"-sink";}
            else if(phase==2){
                if(role().equals("B")&&!tag.getBoolean("Reconnected")){if(sawFirst){stopping=true;shot="B-before-reconnect";}return;}
                if(!tag.getBoolean("Reconnected"))return;shot=role()+"-sink-reconnected";
            }else shot=role()+"-vanity";
        }else if(phase==4){
            if(mc.screen instanceof LecternScreen){
                var book=mc.player.containerMenu.getSlot(0).getItem();var content=book.get(DataComponents.WRITTEN_BOOK_CONTENT);require(content!=null&&content.author().equals("Dad")&&content.pages().size()==3,"the actual native menu has the complete original and one added care memory");
                String text=content.pages().get(1).raw().getString().toLowerCase(Locale.ROOT);require(text.contains(role().equals("A")?"wrap":"brush"),"the card's real native component contains this reader's settled memory");
                if(((LecternMenu)mc.player.containerMenu).getPage()!=1){if(age-clicked>=10){clicked=age;mc.gameMode.handleInventoryButtonClick(mc.player.containerMenu.containerId,101);}return;}
                if(age>=40)shot=role()+"-card";
            }else if(mc.screen==null&&age-clicked>=15&&mc.player.distanceToSqr(at.getCenter())<25){clicked=age;look(mc,at.getCenter());mc.gameMode.useItemOn(mc.player,InteractionHand.MAIN_HAND,new BlockHitResult(at.getCenter(),Direction.UP,at,false));}
        }
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{if(!TailoringSocketProof.enabled()||shot==null)return;var mc=Minecraft.getInstance();if(mc.player==null||ack==phase)return;var name=shot;shot=null;Files.createDirectories(TailoringSocketProof.folder());
        try(var im=Screenshot.takeScreenshot(mc.getMainRenderTarget())){im.writeToFile(TailoringSocketProof.folder().resolve(name+".png"));}
        if(phase<=3){var data=mc.player.getInventory().getItem(8).get(DataComponents.CUSTOM_DATA).copyTag();String id=data.getUUID("Own").toString();var file=TailoringSocketProof.folder().resolve(role()+"-sink-original.txt");if(phase==1)Files.writeString(file,id);if(phase==2&&!stopping)require(Files.readString(file).equals(id),"the rendered original UUID is unchanged across the real reconnect");Files.writeString(TailoringSocketProof.folder().resolve(name+".txt"),"Native entity "+id+"; sole owner-only display at "+BlockPos.of(data.getLong("Target"))+"; rendered frame "+name+".\n");}
        if(stopping){Files.writeString(TailoringSocketProof.folder().resolve("restart-b.txt"),"B captured its actual original and disconnected.\n");mc.stop();return;}
        ack=phase;mc.player.connection.sendCommand("tailoringproof "+phase);TheOldestHouse.LOGGER.info("TAILORING CLIENT {} captured phase {}",role(),phase);
    }
}
