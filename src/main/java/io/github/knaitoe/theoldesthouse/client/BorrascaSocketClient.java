package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.gametest.LiveExpeditionProof;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.inventory.LecternScreen;
import net.minecraft.client.resources.sounds.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/** Native key/use/menu input. Staged endpoints and actual traversals are documented separately. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class BorrascaSocketClient {
    private static int last,route,clicked=-100;private static String shot;private static SimpleSoundInstance vanilla;
    private BorrascaSocketClient(){}
    private static void stop(Minecraft mc){mc.options.keyUp.setDown(false);mc.options.keyShift.setDown(false);mc.options.keyUse.setDown(false);mc.options.keyAttack.setDown(false);mc.options.keyJump.setDown(false);mc.options.keySprint.setDown(false);}
    private static boolean walk(Minecraft mc,BlockPos p,double tolerance){double dx=p.getX()+.5-mc.player.getX(),dz=p.getZ()+.5-mc.player.getZ();if(Math.hypot(dx,dz)<tolerance){mc.options.keyUp.setDown(false);return true;}if(Math.abs(dx)>.15)dz=0;mc.player.setYRot((float)Math.toDegrees(Math.atan2(-dx,dz)));mc.player.setXRot(0);mc.options.keyUp.setDown(true);return false;}
    private static boolean walk(Minecraft mc,Vec3 p,double tolerance){double dx=p.x-mc.player.getX(),dz=p.z-mc.player.getZ();if(Math.hypot(dx,dz)<tolerance){mc.options.keyUp.setDown(false);return true;}if(Math.abs(dx)>.15)dz=0;mc.player.setYRot((float)Math.toDegrees(Math.atan2(-dx,dz)));mc.player.setXRot(0);mc.options.keyUp.setDown(true);return false;}
    private static void click(Minecraft mc,BlockPos at,int age){if(age-clicked<20)return;clicked=age;mc.player.setXRot((float)-Math.toDegrees(Math.atan2(at.getY()+.5-mc.player.getEyeY(),Math.sqrt(mc.player.position().distanceToSqr(at.getCenter())))));mc.gameMode.useItemOn(mc.player,InteractionHand.MAIN_HAND,new BlockHitResult(at.getCenter(),Direction.SOUTH,at,false));}
    public static boolean ready(Minecraft mc,int step,int age,String role,BlockPos target){if(last!=step){last=step;route=0;clicked=-100;vanilla=null;stop(mc);}
        if(age%40==0)TheOldestHouse.LOGGER.info("LIVE BORRASCA CLIENT {} phase={} route={} body={} ground={} gravity={} pose={} velocity={} below={} screen={}",role,step,route,mc.player.position(),mc.player.onGround(),!mc.player.isNoGravity(),mc.player.getPose(),mc.player.getDeltaMovement(),mc.level.getBlockState(mc.player.blockPosition().below()),mc.screen==null?"none":mc.screen.getClass().getSimpleName());
        if(step==54){if(BorrascaAtmosphere.active()){var b=BorrascaAtmosphere.source().subtract(BorrascaRooms.MACHINE);if(walk(mc,b.offset(0,0,-3),.4))return true;}else{click(mc,target,age);walk(mc,target.north(3),.4);}return false;}
        if(step==55){
            if(!BorrascaAtmosphere.active())return false;if(age>=20&&vanilla==null){vanilla=SimpleSoundInstance.forMusic(SoundEvents.MUSIC_GAME.value());mc.getSoundManager().play(vanilla);}if(age>45&&vanilla!=null&&mc.getSoundManager().isActive(vanilla))throw new IllegalStateException("LIVE BORRASCA vanilla music overlapped the industrial scene");
            for(var e:mc.level.entitiesForRendering())if(e instanceof LiteraryActor a&&a.scene().equals(LabyrinthPlace.BORRASCA.id())&&a.owner().isPresent()&&!a.owner().get().equals(mc.player.getUUID()))throw new IllegalStateException("LIVE BORRASCA peer's private cast was tracked");
            if(mc.screen instanceof LecternScreen){stop(mc);if(age>25)lastPage(mc);if(age>60)mc.player.closeContainer();}else if(age<60){if(mc.player.distanceToSqr(target.getCenter())<20){stop(mc);click(mc,target,age);}else walk(mc,target.south(2),.4);}
            if(age>75&&BorrascaAtmosphere.playing()){if(!BorrascaAtmosphere.source().equals(target.subtract(BorrascaRooms.SOURCE).offset(BorrascaRooms.MACHINE)))throw new IllegalStateException("LIVE BORRASCA industrial source moved between reader and scene");shot=role+"-borrasca-source-and-sound";return true;}return false;
        }
        if(step==56){var b=target;var points=new BlockPos[]{b.offset(0,0,-70),b.offset(3,0,-86),b.offset(3,0,-88)};if(route<points.length){if(walk(mc,points[route],.35))route++;return false;}if(route==3){mc.player.setYRot(180);mc.player.setXRot(0);mc.options.keyUp.setDown(true);if(mc.player.getY()>=b.getY()+6.05)route++;return false;}if(route==4){if(walk(mc,b.offset(1,6,-88),.35))route++;return false;}if(walk(mc,b.offset(1,6,-92),.4)){stop(mc);shot=role+"-borrasca-native-ladder";return true;}return false;}
        if(step==57){stop(mc);if(mc.screen instanceof LecternScreen){if(age>20)lastPage(mc);if(age>50)mc.gameMode.handleInventoryButtonClick(mc.player.containerMenu.containerId,3);if(age>70)mc.player.closeContainer();}else if(age<70)click(mc,target,age);else if(age>90)click(mc,target.subtract(BorrascaRooms.EVIDENCE).offset(BorrascaRooms.PACK),age);if(age>120){shot=role+"-borrasca-original-register";return true;}return false;}
        if(step==58){var b=target.subtract(BorrascaRooms.RELEASE);mc.options.keyShift.setDown(true);if(age<25){click(mc,target,age);mc.options.keyUp.setDown(false);return false;}// The open service door folds against the west side of its cell; aim for the free part of the doorway.
            var points=new Vec3[]{Vec3.atLowerCornerOf(b).add(-16.4,12,-204.5),Vec3.atLowerCornerOf(b).add(-16.4,12,-207.5),Vec3.atLowerCornerOf(b).add(-24.5,11,-208.5)};if(route<points.length&&walk(mc,points[route],route==0?.08:.25))route++;if(route>=points.length){stop(mc);shot=role+"-borrasca-native-pipe";return true;}return false;}
        if(step==59){mc.options.keyShift.setDown(false);if(mc.level.dimension()==HouseDimensions.OUTSIDE){if(mc.player.distanceToSqr(target.getCenter())<20)click(mc,target,age);walk(mc,target.south(4),.4);return false;}stop(mc);if(BorrascaAtmosphere.active()||BorrascaAtmosphere.playing())throw new IllegalStateException("LIVE BORRASCA industrial sound or suppression leaked through the actual return");if(age>20){shot=role+"-borrasca-native-return";return true;}}
        return false;
    }
    private static void lastPage(Minecraft mc){var content=mc.player.containerMenu.getSlot(0).getItem().get(DataComponents.WRITTEN_BOOK_CONTENT);if(content!=null)mc.gameMode.handleInventoryButtonClick(mc.player.containerMenu.containerId,100+content.pages().size()-1);}
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{if(!LiveExpeditionProof.enabled()||shot==null)return;var mc=Minecraft.getInstance();Files.createDirectories(LiveExpeditionProof.folder());try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(LiveExpeditionProof.folder().resolve(shot+".png"));}Files.writeString(LiveExpeditionProof.folder().resolve(shot+".txt"),"Actual connected client: native source/use/menu/movement and production Borrasca rendering. Fixture scope is described in borrasca-socket-passed.txt.\n");shot=null;}
}
