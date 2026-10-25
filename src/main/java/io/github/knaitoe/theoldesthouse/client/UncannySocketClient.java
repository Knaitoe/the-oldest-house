package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.gametest.UncannySocketProof;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.inventory.LecternScreen;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

/** Native socket-client source/menu/bed/feed/door/health actions and genuine GPU readback. Opt-in only. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class UncannySocketClient {
    private static int phase,age,clicked=-100,selected,feedMask,paperSent=-1,bedroomLeg;private static String shot;private static boolean stopAfter,closed,paid,paperOpened;private static final Set<String> captures=new HashSet<>();
    private UncannySocketClient(){}
    private static String role(){return System.getProperty("the_oldest_house.liveProofRole","");}
    private static void require(boolean okay,String message){if(!okay)throw new IllegalStateException("UNCANNY CLIENT: "+message);}
    private static void stop(Minecraft mc){mc.options.keyUp.setDown(false);mc.options.keyShift.setDown(false);mc.options.keyUse.setDown(false);mc.options.keyAttack.setDown(false);mc.options.keyJump.setDown(false);mc.options.keySprint.setDown(false);}
    private static void look(Minecraft mc,Vec3 at){var delta=at.subtract(mc.player.getEyePosition());mc.player.setYRot((float)Math.toDegrees(Math.atan2(-delta.x,delta.z)));mc.player.setXRot((float)-Math.toDegrees(Math.atan2(delta.y,Math.hypot(delta.x,delta.z))));}
    private static void click(Minecraft mc,BlockPos at){if(age-clicked<25||mc.screen!=null)return;clicked=age;look(mc,at.getCenter());mc.gameMode.useItemOn(mc.player,InteractionHand.MAIN_HAND,new BlockHitResult(at.getCenter(),Direction.SOUTH,at,false));}
    private static boolean lastPage(Minecraft mc){var pages=mc.player.containerMenu.getSlot(0).getItem().get(DataComponents.WRITTEN_BOOK_CONTENT);if(pages==null)return false;mc.gameMode.handleInventoryButtonClick(mc.player.containerMenu.containerId,100+pages.pages().size()-1);return true;}
    private static void ack(Minecraft mc,String view){if(shot!=null||captures.contains(phase+":"+view))return;shot=role()+"-"+view;}
    private static LiteraryActor actor(Minecraft mc,int wanted){for(var entity:mc.level.entitiesForRendering())if(entity instanceof LiteraryActor a&&a.role()==wanted&&a.owner().filter(id->id.equals(mc.player.getUUID())).isPresent())return a;return null;}
    private static void paper(Minecraft mc,BlockPos at,String view,boolean verified){
        if(mc.screen instanceof LecternScreen){
            paperOpened=true;if(paperSent<0&&lastPage(mc))paperSent=age;
            if(paperSent>=0&&age-paperSent>=20)mc.player.closeContainer();
        }else if(!paperOpened||paperSent<0)click(mc,at);
        // The native menu and final-page round trip must happen; elapsed phase time alone proves neither.
        if(paperOpened&&paperSent>=0&&verified&&mc.screen==null)ack(mc,view);
    }
    /** Walk the actual doorway, stair flight and landing with ordinary native movement packets. */
    private static boolean bedroomWalk(Minecraft mc,BlockPos b){
        boolean peer=role().equals("B");
        Vec3[] route={new Vec3(peer?-.5:.5,0,-2.5),new Vec3(peer?-7.3:-6.5,0,-2.5),new Vec3(peer?-7.3:-6.5,5,-10.5),new Vec3(peer?-6.3:-5.5,5,-20.5)};
        if(bedroomLeg>=route.length)return true;
        var target=route[bedroomLeg].add(Vec3.atLowerCornerOf(b));var delta=target.subtract(mc.player.position());
        if(delta.horizontalDistanceSqr()<.09&&Math.abs(delta.y)<.3&&mc.player.onGround()){
            TheOldestHouse.LOGGER.info("UNCANNY CLIENT {} native bedroom waypoint {} at {}",role(),bedroomLeg,mc.player.position());
            if(++bedroomLeg==route.length){
                require(!mc.player.isNoGravity(),"the doorway and stairs use real gravity");
                try{Files.createDirectories(UncannySocketProof.folder());Files.writeString(UncannySocketProof.folder().resolve(role()+"-bedroom-native-route.txt"),"Four native walking waypoints completed from the return doorway, up the five real stair treads and onto the notebook landing; no teleport, flight or no-gravity movement. Final body="+mc.player.position()+" grounded="+mc.player.onGround()+"\n");}catch(Exception ex){throw new IllegalStateException(ex);}
                return true;
            }
            return false;
        }
        look(mc,new Vec3(target.x,mc.player.getEyeY(),target.z));mc.options.keyUp.setDown(true);return false;
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        if(!UncannySocketProof.enabled())return;var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.gameMode==null)return;
        var custom=mc.player.getInventory().getItem(8).get(DataComponents.CUSTOM_DATA);if(custom==null)return;var data=custom.copyTag();int next=data.getInt("UncannyStep");if(next==0)return;
        if(next!=phase){phase=next;age=0;clicked=-100;feedMask=0;selected=0;bedroomLeg=0;closed=false;paid=false;paperOpened=false;paperSent=-1;stop(mc);if(mc.screen!=null)mc.setScreen(null);if(phase==9||phase==11)UncannyCameras.clear();}age++;
        stop(mc);var b=UncannyClient.base();
        if(age%100==0)TheOldestHouse.LOGGER.info("UNCANNY CLIENT {} phase={} room={} eye={} feeds={} headPixels={} health={} ring={} mirror={} screen={}",role(),phase,UncannyClient.room(),mc.player.getEyePosition(),feedMask,UncannyCameras.verifiedHeadPixels(),mc.player.getHealth(),UncannyClient.ring(),UncannyClient.mirror(),mc.screen==null?"none":mc.screen.getClass().getSimpleName());
        for(var entity:mc.level.entitiesForRendering())if(entity instanceof LiteraryActor a&&UncannyStories.role(a.role())&&a.owner().isPresent())require(a.owner().get().equals(mc.player.getUUID()),"another reader's private original was tracked");
        if(phase==1&&age>30&&UncannyClient.room().equals("painted_interior")){
            mc.player.setYRot(180);mc.player.setXRot(0);var propped=actor(mc,LiteraryActor.UNCANNY_PROPPED);if(propped!=null&&propped.phase()==1)ack(mc,"boarded-periphery");
        }
        if(phase==2&&!captures.contains("2:diner-room")){
            look(mc,b.offset(1,1,-5).getCenter());if(age>25&&UncannyClient.room().equals("second_waitress"))ack(mc,"diner-room");return;
        }
        if((phase==2||phase==3)&&age>30){var fake=actor(mc,LiteraryActor.UNCANNY_FALSE_WAITRESS);if(fake!=null){look(mc,fake.getEyePosition());if(phase==2&&fake.phase()==1||phase==3&&fake.phase()>=2)ack(mc,phase==2?"diner-translucent":"diner-solid");}}
        if(phase==4&&age>25){var giant=actor(mc,LiteraryActor.UNCANNY_GIANT);if(giant!=null){look(mc,giant.position().add(0,1.8,-2));require(giant.getScale()==2.5F&&giant.getBbHeight()<=2.8,"the five-block actor has its folded collision body");ack(mc,"giant-folded");}}
        if(phase==5&&UncannyClient.room().equals("missing_sleep")&&bedroomWalk(mc,b))paper(mc,b.offset(UncannyRooms.source(LabyrinthPlace.MISSING_SLEEP)),"bedroom-source",data.getBoolean("PaperRead"));
        if(phase==6||phase==7){if(age>15&&clicked<0)click(mc,b.offset(UncannyRooms.BED_AT));if(age>35&&UncannyClient.sleeps()==(phase==6?1:2))ack(mc,phase==6?"bedroom-first-cut":"bedroom-second-cut");}
        if(phase==8&&age>30){var doll=actor(mc,LiteraryActor.UNCANNY_MARIONETTE);if(doll!=null&&doll.phase()==1){look(mc,doll.getEyePosition());require(UncannyActorRenderer.visible(doll),"the direct form is visible to its owner");ack(mc,"bedroom-direct");}}
        if((phase==9||phase==11)&&UncannyClient.room().equals("missing_sleep")){
            if(!(mc.screen instanceof UncannyCameras.MonitorScreen)){click(mc,b.offset(UncannyRooms.MONITOR_AT));}
            else if(age>30){
                var monitor=(UncannyCameras.MonitorScreen)mc.screen;
                // Hold a feed until its actual native framebuffer has been captured.
                // Fixed 35-tick rotations used to skip the one viable frame on slow GPU runners.
                selected=0;
                while(selected<3&&(feedMask&(1<<selected))!=0)selected++;
                monitor.keyPressed(49+selected,0,0);
                if((feedMask&(1<<selected))==0&&UncannyCameras.frames(selected)>1&&age%10==0){
                    if(selected!=1||UncannyCameras.ownFace(1)&&UncannyCameras.verifiedHeadPixels()>=12){
                        try(var image=UncannyCameras.copyFrame(selected)){require(image!=null,"actual native channel exists");Files.createDirectories(UncannySocketProof.folder());image.writeToFile(UncannySocketProof.folder().resolve(role()+"-cctv-"+(phase==11?"reconnected-":"")+selected+".png"));}catch(Exception ex){throw new IllegalStateException(ex);}feedMask|=1<<selected;
                    }
                }
                if(feedMask==15&&age>160){
                    var renderer=mc.getEntityRenderDispatcher().getRenderer(actor(mc,LiteraryActor.UNCANNY_MARIONETTE));require(renderer.getClass().getEnclosingClass()==LiteraryRenderers.class&&renderer.getClass().getSimpleName().equals("CastRenderer"),"the production saved actor renderer is used");
                    try{Files.writeString(UncannySocketProof.folder().resolve(role()+"-cctv-"+(phase==11?"reconnected-":"")+"pixels.txt"),"Native same-frame masked-body head pixels="+UncannyCameras.verifiedHeadPixels()+"; actual skin="+mc.player.getSkin().texture()+"; model="+mc.player.getSkin().model()+"; four native captures="+feedMask+"\n");}catch(Exception ex){throw new IllegalStateException(ex);}
                    ack(mc,phase==11?"bedroom-cctv-reconnected":"bedroom-cctv");
                }
            }
        }
        if(phase==10&&age>20&&!captures.contains("10:bedroom-reconnect")){stopAfter=role().equals("B");ack(mc,"bedroom-reconnect");}
        if(phase==12){if(mc.screen!=null)mc.setScreen(null);if(age>15&&!data.getBoolean("DoorBlocked"))click(mc,b.offset(UncannyRooms.PARENTS_DOOR));if(age>75&&data.getBoolean("DoorBlocked"))ack(mc,"bedroom-door-closed");}
        if(phase==13)paper(mc,b.offset(UncannyRooms.account(LabyrinthPlace.MISSING_SLEEP)),"bedroom-account",data.getBoolean("PaperRead"));
        if(phase==14&&age>120){look(mc,b.offset(UncannyRooms.MASS).getCenter());ack(mc,"cellar-growth");}
        if(phase==15){
            look(mc,b.offset(0,1,-17).getCenter());var state=mc.level.getBlockState(b.offset(UncannyRooms.FLOOD));
            if(state.is(UncannyRegistry.PROP.get())){
                boolean dark=state.getValue(UncannyPropBlock.STAGE)>0;String view=dark?"foyer-dark":"foyer-lit";
                if(!captures.contains("15:"+view)&&age>20){shot=role()+"-"+view;}
                if(age>410&&captures.contains("15:foyer-dark")&&captures.contains("15:foyer-lit"))ack(mc,"foyer-cycles");
            }
        }
        if(phase==16){
            if(age>15&&!paid&&mc.player.getMainHandItem().is(UncannyRegistry.SMOKY_MIRROR.get())&&mc.player.getOffhandItem().is(UncannyRegistry.BONE_RING.get())){mc.gameMode.useItem(mc.player,InteractionHand.MAIN_HAND);paid=true;}
            var doll=actor(mc,LiteraryActor.UNCANNY_MARIONETTE);if(doll!=null)look(mc,doll.getEyePosition());
            if(age>35&&UncannyClient.ring()&&UncannyClient.mirror()&&mc.player.getHealth()<=18){require(doll!=null&&UncannyClient.outline(doll),"the ring outlines the original only on its own client");ack(mc,"ring-and-mirror");}
        }
        if(phase==17&&age>30)mc.stop();
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        if(!UncannySocketProof.enabled()||shot==null)return;var mc=Minecraft.getInstance();Files.createDirectories(UncannySocketProof.folder());
        try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(UncannySocketProof.folder().resolve(shot+".png"));}
        String view=shot.substring(role().length()+1);captures.add(phase+":"+view);
        if(!view.equals("foyer-lit")&&!view.equals("foyer-dark")&&!view.equals("diner-room"))mc.player.connection.sendCommand("uncannyproof "+phase);
        shot=null;if(stopAfter){stopAfter=false;mc.stop();}
    }
}
