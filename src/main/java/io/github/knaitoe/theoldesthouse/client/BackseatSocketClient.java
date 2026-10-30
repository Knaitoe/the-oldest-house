package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.gametest.BackseatSocketProof;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.inventory.LecternScreen;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.event.sound.PlaySoundSourceEvent;

/** Real client movement, native use/menu packets, decoded audio and production framebuffer/mirror readback. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class BackseatSocketClient {
    private static int phase,age,clicked=-100,leg,seatStage;
    private static UUID originalSeat;
    private static String shot;private static boolean shotAck,stopAfter,cameraSet,looked;
    private static NativeImage before;
    private static NativeImage windowBefore;
    private static Vec3 windowEye;
    private static double windowTravel;
    private static float revealYaw,revealPitch;
    private static float firstWind;private static double firstPull;
    private static final Set<String> CAPTURES=new HashSet<>();
    private static final Set<String> HEARD=java.util.concurrent.ConcurrentHashMap.newKeySet();
    private static final List<net.minecraft.client.resources.sounds.SoundInstance> RADIOS=new java.util.concurrent.CopyOnWriteArrayList<>();
    private static final List<net.minecraft.client.resources.sounds.SoundInstance> WINDS=new java.util.concurrent.CopyOnWriteArrayList<>();
    private BackseatSocketClient(){}
    private static String role(){return System.getProperty("the_oldest_house.liveProofRole","");}
    private static void require(boolean okay,String message){if(!okay)throw new IllegalStateException("BACKSEAT CLIENT: "+message);}
    @SubscribeEvent public static void sound(PlaySoundSourceEvent e){if(!BackseatSocketProof.enabled()||e.getSound()==null)return;var sound=e.getSound();var id=sound.getLocation();if(!id.getNamespace().equals(TheOldestHouse.MOD_ID)||!id.getPath().startsWith("backseat."))return;HEARD.add(id.getPath());if(id.equals(BackseatRegistry.RADIO.getId()))RADIOS.add(sound);if(id.equals(BackseatRegistry.WIND.getId()))WINDS.add(sound);}
    private static void stop(Minecraft mc){mc.options.keyUp.setDown(false);mc.options.keyShift.setDown(false);mc.options.keyUse.setDown(false);mc.options.keyJump.setDown(false);mc.options.keySprint.setDown(false);}
    private static void look(Minecraft mc,Vec3 at){var d=at.subtract(mc.player.getEyePosition());mc.player.setYRot((float)Math.toDegrees(Math.atan2(-d.x,d.z)));mc.player.setXRot((float)-Math.toDegrees(Math.atan2(d.y,d.horizontalDistance())));}
    private static void forward(Minecraft mc){mc.player.setYRot(180);mc.player.setXRot(0);}
    private static void click(Minecraft mc,Vec3 hit,boolean turn){if(age-clicked<30||mc.screen!=null)return;clicked=age;if(turn)look(mc,hit);mc.gameMode.useItemOn(mc.player,InteractionHand.MAIN_HAND,new BlockHitResult(hit,Direction.WEST,BlockPos.containing(hit),false));}
    private static void capture(String view,boolean ack){String name=role()+"-"+view;if(shot!=null||CAPTURES.contains(name))return;shot=name;shotAck=ack;}
    private static BackseatCarBlock.Car car(Minecraft mc,BlockPos at){return mc.level.getBlockEntity(at) instanceof BackseatCarBlock.Car car?car:null;}
    private static void locked(Minecraft mc,BackseatCarBlock.Car car){
        require(mc.player.getVehicle() instanceof BackseatSeat seat&&seat.owner().filter(mc.player.getUUID()::equals).isPresent(),"only the actual driver is on this seat");
        require(mc.options.getCameraType()==CameraType.FIRST_PERSON,"the real cabin view is first person");
        require(mc.level.getRainLevel(1)==0,"the actual door lock cuts this driver's rain immediately");
        require(RADIOS.stream().noneMatch(mc.getSoundManager()::isActive),"the actual radio sources have stopped at the lock");
        require(car.phase==BackseatCarBlock.Car.LOCKED,"a forward-looking driver remains locked without a countdown");
    }
    private static boolean returned(Minecraft mc){return mc.level.dimension().equals(HouseDimensions.INTERIOR)&&!mc.player.isPassenger()&&mc.player.onGround()&&mc.screen==null&&mc.levelRenderer.isSectionCompiled(mc.player.blockPosition().below());}
    private static void endView(Minecraft mc,String name){
        require(HEARD.containsAll(Set.of("backseat.rise","backseat.glass","backseat.impact","backseat.wind")),"this driver actually decoded their original reveal, continuous wind and collision audio: "+HEARD);
        require(WINDS.stream().noneMatch(mc.getSoundManager()::isActive),"the acceleration wind stops at the actual crash/House return");
        require(CAPTURES.contains(role()+"-acceleration"),"the later camera peel and heavier wind were verified before impact");
        require(CAPTURES.contains(role()+"-reveal"),"the moving original figure and native mirror were captured before the impact");capture(name,true);
        require(CAPTURES.contains(role()+"-side-window-early")&&CAPTURES.contains(role()+"-side-window-late"),"the actual driver saw native roadside detail passing through the side glass during the same acceleration");
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post ignored){
        if(!BackseatSocketProof.enabled())return;var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.gameMode==null)return;
        var custom=mc.player.getInventory().getItem(8).get(DataComponents.CUSTOM_DATA);if(custom==null)return;var tag=custom.copyTag();int next=tag.getInt("BackseatStep");if(next==0)return;
        stop(mc);if(mc.screen instanceof net.minecraft.client.gui.screens.ReceivingLevelScreen)return;
        if(next!=phase){phase=next;age=0;clicked=-100;leg=0;looked=false;}age++;
        if(age%100==0)TheOldestHouse.LOGGER.info("BACKSEAT CLIENT {} phase={} feet={} vehicle={} screen={} sounds={}",role(),phase,mc.player.position(),mc.player.getVehicle(),mc.screen==null?"none":mc.screen.getClass().getSimpleName(),HEARD);
        var road=tag.contains("Road")?BlockPos.of(tag.getLong("Road")):null;var at=road==null?null:road.offset(BackseatRooms.CAR);var car=at==null?null:car(mc,at);
        if(phase==1){
            if(mc.level.dimension().equals(HouseDimensions.INTERIOR)){click(mc,BlockPos.of(tag.getLong("Source")).getCenter(),true);return;}
            if(car!=null&&car.road&&mc.player.onGround()&&mc.levelRenderer.isSectionCompiled(mc.player.blockPosition().below())&&age>30){
                require(mc.level.getDayTime()==18000&&mc.level.getRainLevel(1)==1,"the actual private road is midnight and raining");
                require(mc.level.getBiome(mc.player.blockPosition()).value().hasPrecipitation(),"the actual native biome renderer receives precipitation, beyond the rain-level getter");
                if(HEARD.containsAll(Set.of("backseat.radio","backseat.wiper","backseat.idle"))){look(mc,at.getCenter().add(0,1,0));capture("rainy-approach",true);}
            }
        }else if(phase==2&&car!=null){
            if(!cameraSet){mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);cameraSet=true;}
            if(mc.player.getVehicle() instanceof BackseatSeat seat&&car.phase==BackseatCarBlock.Car.SEATED){
                if(seatStage==0){originalSeat=seat.getUUID();seatStage=1;}
                if(seatStage==1){mc.options.keyShift.setDown(true);return;}
                require(seat.getUUID().equals(originalSeat),"native stand-up and reboarding reuse the same original seat");capture("seated",true);return;
            }
            if(seatStage==1){capture("standing-before-lock",false);return;}
            if(seatStage==2){click(mc,at.getCenter().add(-.625,.5,-.2),true);return;}
            Vec3[] route={new Vec3(road.getX()-2,road.getY(),road.getZ()-5),new Vec3(at.getX()-2,at.getY(),at.getZ()+.3)};
            if(leg<route.length){var target=route[leg];var d=target.subtract(mc.player.position());if(d.horizontalDistanceSqr()<.08&&Math.abs(d.y)<.3&&mc.player.onGround())leg++;else{look(mc,new Vec3(target.x,mc.player.getEyeY(),target.z));mc.options.keyUp.setDown(true);}return;}
            click(mc,at.getCenter().add(-.625,.5,-.2),true);
        }else if(phase==3&&car!=null){
            if(role().equals("B")){look(mc,at.getCenter().add(.55,.53,0));if(age>25)capture("notebook-unread",true);}
            else if(mc.screen instanceof LecternScreen){
                // The screen opens on its own packet; the menu's contents follow, so a fast client waits a tick for the book.
                var book=mc.player.containerMenu.getSlot(0).getItem();if(book.isEmpty())return;var content=book.get(DataComponents.WRITTEN_BOOK_CONTENT);var data=book.get(DataComponents.CUSTOM_DATA);
                require(content!=null&&data!=null&&data.copyTag().getUUID("BackseatOwner").equals(mc.player.getUUID()),"the native notebook menu contains this driver's actual original");
                if(!CAPTURES.contains("A-notebook")){capture("notebook",false);return;}
                mc.gameMode.handleInventoryButtonClick(mc.player.containerMenu.containerId,3);
            }else if(car.notebook.isEmpty())capture("notebook-kept",true);
            else click(mc,at.getCenter().add(.55,.53,0),true);
        }else if(phase==4&&car!=null){
            forward(mc);if(car.phase==BackseatCarBlock.Car.SEATED)click(mc,at.getCenter().add(-1.1875,.7,0),false);
            if(car.phase==BackseatCarBlock.Car.LOCKED&&age>30){locked(mc,car);if(before==null)before=BackseatMirror.copy();if(before!=null)capture("locked",true);}
        }else if(phase==5&&car!=null){
            forward(mc);if(age<30||!(mc.player.getVehicle() instanceof BackseatSeat))return;locked(mc,car);
            if(role().equals("B")&&!tag.getBoolean("Reconnected")){stopAfter=true;capture("locked-before-reconnect",false);}
            else if(tag.getBoolean("Reconnected")){if(before==null)before=BackseatMirror.copy();if(before!=null)capture("locked-reconnected",true);}
        }else if(phase==6||phase==7){
            boolean driver=phase==6?role().equals("A"):role().equals("B");
            if(driver){
                if(returned(mc)&&age>30)endView(mc,"house-return");
                else if(car!=null&&mc.player.getVehicle() instanceof BackseatSeat){
                    if(!looked){look(mc,at.getCenter().add(BackseatCarMesh.MIRROR).add(0,-.5,0));revealYaw=mc.player.getYRot();revealPitch=mc.player.getXRot();looked=true;}
                    if(car.phase==BackseatCarBlock.Car.REVEAL){
                        if(!CAPTURES.contains(role()+"-reveal")&&car.travel>3)capture("reveal",false);
                        else if(CAPTURES.contains(role()+"-reveal")&&!CAPTURES.contains(role()+"-side-window-late")&&(car.clock<60||CAPTURES.contains(role()+"-side-window-early"))){
                            // One real glance out each driver's own side glass. Hold the same
                            // heading for both frames so visible movement comes from native travel.
                            // The first frame comes early in the acceleration; a slow client may take the
                            // second later in the same reveal (run 1346), and the acceleration frame waits for it.
                            mc.player.setYRot(role().equals("A")?90:-90);mc.player.setXRot(0);
                            if(!CAPTURES.contains(role()+"-side-window-early"))capture("side-window-early",false);
                            else if(car.travel-windowTravel>3)capture("side-window-late",false);
                        }else{mc.player.setYRot(revealYaw);mc.player.setXRot(revealPitch);}
                        if(car.clock>=60&&CAPTURES.contains(role()+"-side-window-late"))capture("acceleration",false);
                    }
                }
            }else if(car!=null&&phase==6){forward(mc);if(age>60){locked(mc,car);require(!HEARD.contains("backseat.rise")&&!HEARD.contains("backseat.glass")&&!HEARD.contains("backseat.impact")&&!HEARD.contains("backseat.wind"),"the other driver's crash and wind audio remain private");capture("peer-still-locked",true);}}
            else if(returned(mc)&&phase==7&&age>30)capture("peer-still-home",true);
        }else if(phase==8&&age>20){stop(mc);if(before!=null){before.close();before=null;}if(windowBefore!=null){windowBefore.close();windowBefore=null;}mc.stop();}
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        if(!BackseatSocketProof.enabled()||shot==null)return;var mc=Minecraft.getInstance();Files.createDirectories(BackseatSocketProof.folder());
        if(shot.contains("-side-window-")){
            if(Math.abs(net.minecraft.util.Mth.wrapDegrees(mc.gameRenderer.getMainCamera().getYRot()-(role().equals("A")?90:-90)))>1)return;
            require(mc.player.getVehicle() instanceof BackseatSeat,"the window frames use the original moving driver's seat");
            var car=((BackseatSeat)mc.player.getVehicle()).car();require(car!=null&&car.phase==BackseatCarBlock.Car.REVEAL,"the actual window view is recorded before native impact");
            if(shot.endsWith("-early")){
                windowBefore=Screenshot.takeScreenshot(mc.getMainRenderTarget());windowEye=mc.gameRenderer.getMainCamera().getPosition();windowTravel=car.travel;
            }else{
                require(windowBefore!=null&&windowEye.distanceToSqr(mc.gameRenderer.getMainCamera().getPosition())>6.25&&car.travel-windowTravel>2.5,"the two fixed-heading frames span real movement through the world");
                try(var after=Screenshot.takeScreenshot(mc.getMainRenderTarget())){
                    int changed=0;for(int y=after.getHeight()/4;y<after.getHeight()*2/3;y++)for(int x=after.getWidth()/4;x<after.getWidth()*3/4;x++)if(after.getPixelRGBA(x,y)!=windowBefore.getPixelRGBA(x,y))changed++;
                    require(changed>256,"native scenery visibly passes across the side-window view: "+changed);
                    Files.writeString(BackseatSocketProof.folder().resolve(role()+"-window-motion.txt"),"Same native side-window heading; actual travel "+windowTravel+" -> "+car.travel+" metres, native eye "+windowEye+" -> "+mc.gameRenderer.getMainCamera().getPosition()+"; "+changed+" changed window pixels. Near reflectors and the farther tree line are actual stationary world blocks.\n");
                }
                windowBefore.close();windowBefore=null;
            }
        }
        if(shot.endsWith("-standing-before-lock")){require(!mc.player.isPassenger()&&mc.options.getCameraType()==CameraType.THIRD_PERSON_BACK,"standing on the actual road before commitment restores the prior third-person camera");seatStage=2;}
        if(shot.endsWith("-house-return"))require(mc.options.getCameraType()==CameraType.FIRST_PERSON,"the confirmed House return respects its existing first-person view rule");
        // Run 1351: a slow reconnected client may take its late side-window frame after the car is already at speed, leaving no
        // frame before impact for a separate acceleration view. That late frame then carries the same speed checks.
        boolean speed=shot.endsWith("-acceleration")||shot.endsWith("-side-window-late")&&!CAPTURES.contains(role()+"-acceleration")&&mc.player.getVehicle() instanceof BackseatSeat moving&&moving.car()!=null&&moving.car().clock>=60;
        if(shot.endsWith("-reveal")||speed){
            var seat=(BackseatSeat)mc.player.getVehicle();var car=seat.car();require(car!=null,"the real moving seat retains its original car");
            var expected=Vec3.atBottomCenterOf(seat.root()).add(BackseatCarMesh.DRIVER).add(0,mc.player.getEyeHeight(),-car.visualTravel(mc.getTimer().getGameTimeDeltaPartialTick(false)));
            double pull=BackseatClient.cameraPullback(mc.getTimer().getGameTimeDeltaPartialTick(false));
            require(mc.gameRenderer.getMainCamera().getPosition().distanceToSqr(expected.add(0,0,pull))<.002,"the actual rendered eye shares the native car motion plus only its bounded rearward peel: camera="+mc.gameRenderer.getMainCamera().getPosition()+" expected="+expected+" pull="+pull);
            require(pull>.03&&pull<=.181,"the speed camera remains inside the real cabin");
            float wind=WINDS.stream().filter(mc.getSoundManager()::isActive).map(net.minecraft.client.resources.sounds.SoundInstance::getVolume).max(Float::compare).orElse(0F);
            if(shot.endsWith("-reveal")){firstPull=pull;firstWind=wind;}
            else{require(pull>firstPull+.025&&wind>firstWind+.1,"the later actual frame pulls farther back and has heavier continuous wind");Files.writeString(BackseatSocketProof.folder().resolve(role()+"-speed-camera.txt"),"Native acceleration: "+car.clock+" ticks; camera peel "+firstPull+" -> "+pull+" metres; continuous wind "+firstWind+" -> "+wind+(shot.endsWith("-acceleration")?"":" (in the late side-window frame)")+".\n");
                if(!shot.endsWith("-acceleration"))CAPTURES.add(role()+"-acceleration");}
            if(shot.endsWith("-reveal"))try(var after=BackseatMirror.copy()){
                require(before!=null&&after!=null,"both live production mirror images exist");int changed=0;
                for(int y=0;y<after.getHeight();y++)for(int x=0;x<after.getWidth();x++)if(after.getPixelRGBA(x,y)!=before.getPixelRGBA(x,y))changed++;
                require(changed>12,"the actual rearview changes when the same body rises: "+changed);before.writeToFile(BackseatSocketProof.folder().resolve(role()+"-mirror-before.png"));after.writeToFile(BackseatSocketProof.folder().resolve(role()+"-mirror-raised.png"));
                Files.writeString(BackseatSocketProof.folder().resolve(role()+"-mirror-pixels.txt"),"Production live rearview changed "+changed+" native pixels during the actual moving-car reveal.\n");
            }
        }
        try(var pixels=Screenshot.takeScreenshot(mc.getMainRenderTarget())){pixels.writeToFile(BackseatSocketProof.folder().resolve(shot+".png"));}
        CAPTURES.add(shot);shot=null;if(shotAck)mc.player.connection.sendCommand("backseatproof "+phase);if(stopAfter){stopAfter=false;mc.stop();}
    }
}
