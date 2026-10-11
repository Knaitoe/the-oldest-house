package io.github.knaitoe.theoldesthouse.client;
import io.github.knaitoe.theoldesthouse.labyrinth.YachtGlazingBlock;

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
    private static int previous,ticks,clicked=-100,ack=-1;private static String shot;private static boolean stopping,portholePoseSeen;
    private static BlockPos proofCamp;
    private static boolean confusionCaptured,terrorCaptured;
    private static final java.util.Set<String> heardSides=new java.util.HashSet<>();
    private static final java.util.List<String> heardKnocks=new java.util.ArrayList<>();
    @SubscribeEvent public static void sounds(net.neoforged.neoforge.client.event.sound.PlaySoundEvent e){
        if(!LiveExpeditionProof.enabled()||proofCamp==null||previous<35||previous>36||e.getSound()==null)return;
        var sound=e.getSound();var id=sound.getLocation();if(!id.getNamespace().equals(TheOldestHouse.MOD_ID)||!(id.getPath().equals("literary.cabin_knock")||id.getPath().equals("goatman.claw")||id.getPath().equals("goatman.hammer")))return;
        // This event precedes sound resolution; only the instance's ID and world coordinates are ready.
        double x=sound.getX()-proofCamp.getX(),z=sound.getZ()-proofCamp.getZ();if(x< -3)heardSides.add("west");if(x>3)heardSides.add("east");if(z< -74)heardSides.add("rear");heardKnocks.add(id+" at "+x+","+z);
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post e) {
        if(!LiveExpeditionProof.enabled())return;var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.gameMode==null)return;
        var component=mc.player.getInventory().getItem(8).get(DataComponents.CUSTOM_DATA);if(component==null)return;
        var data=component.copyTag();int step=data.getInt("Step");if(step<1)return;String role=System.getProperty("the_oldest_house.liveProofRole","");
        if(previous!=step){previous=step;ticks=0;clicked=-100;ack=-1;portholePoseSeen=false;mc.options.keyUp.setDown(false);mc.options.keyShift.setDown(false);mc.options.keyAttack.setDown(false);mc.options.keyUse.setDown(false);}ticks++;
        BlockPos target=BlockPos.of(data.getLong("Target"));var block=mc.level.getBlockState(target);
        if(step==1&&ticks>30&&mc.level.players().size()>=2)ack(mc,1);
        if(step==2||step==31) {
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
            if(ack!=10&&ticks>95&&mc.level.getBlockState(target.offset(-2,0,-5)).is(io.github.knaitoe.theoldesthouse.labyrinth.StaircaseLeakRegistry.PROP.get())
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
            if(step==15&&ticks==40){
                var witch=io.github.knaitoe.theoldesthouse.labyrinth.DrownedTownRegistry.LAKE_WITCH.get().create(mc.level);
                if(witch==null)throw new IllegalStateException("Native Stacy model fixture missing");
                var model=new LakeWitchModel(mc.getEntityModels().bakeLayer(LakeWitchModel.LAYER));
                for(float yaw:new float[]{-120,-35,35,120}){
                    model.setupAnim(witch,1,.7F,12,yaw,80);
                    float expected=net.minecraft.util.Mth.clamp(yaw,-70,70)*(float)Math.PI/180;
                    if(Math.abs(model.head.yRot-expected)>.001||Math.abs(model.hat.yRot-expected)>.001||Math.abs(model.leftKnee.xRot)<.1F)
                        throw new IllegalStateException("Native setupAnim lost bounded tracking, hat alignment or folded joints");
                    model.attackTime=.25F;model.setupAnim(witch,1,.7F,12,yaw,-80);
                    if(Math.abs(model.head.yRot-expected)>.001||model.rightArm.yRot>-.4F)throw new IllegalStateException("Look tracking erases the claw attack");
                    model.attackTime=0;
                }
                witch.memoryPhase(0);model.setupAnim(witch,0,0,0,35,0);
                if(model.jaw.xRot!=0||model.leftKnee.xRot!=0)throw new IllegalStateException("Hunt joints leak into standing memory");
                shot=role+"-stacy-door";ack(mc,15);
            }}
        if(step==16){mc.options.keyShift.setDown(true);if(mc.player.position().distanceToSqr(net.minecraft.world.phys.Vec3.atBottomCenterOf(target))>.16)walk(mc,target);
            else{mc.options.keyUp.setDown(false);if(ticks>25&&ack!=16){shot=role+"-leaf-cover";ack(mc,16);}}}
        if(step==17){mc.options.keyUp.setDown(false);mc.options.keyShift.setDown(true);double dx=target.getX()+.5-mc.player.getX(),dz=target.getZ()+.5-mc.player.getZ();mc.player.setYRot((float)Math.toDegrees(Math.atan2(-dx,dz)));mc.player.setXRot(0);}
        if(step==18){mc.options.keyUp.setDown(false);mc.options.keyShift.setDown(true);if(ticks==30){shot=role+"-slasher-leaves";ack(mc,18);}}
        if(step==19&&ticks>30&&ack!=19){
            mc.options.keyUp.setDown(false);var actors=mc.level.getEntitiesOfClass(io.github.knaitoe.theoldesthouse.labyrinth.LiteraryActor.class,mc.player.getBoundingBox().inflate(32),a->a.role()==io.github.knaitoe.theoldesthouse.labyrinth.LiteraryActor.KILLER&&a.owner().isPresent());
            if(actors.size()==2){var frustum=new net.minecraft.client.renderer.culling.Frustum(new org.joml.Matrix4f(),new org.joml.Matrix4f().ortho(-64,64,-64,64,-64,64));var camera=mc.gameRenderer.getMainCamera().getPosition();frustum.prepare(camera.x,camera.y,camera.z);
                int visible=0;for(var a:actors){boolean mine=a.owner().orElseThrow().equals(mc.player.getUUID());boolean rendered=mc.getEntityRenderDispatcher().getRenderer(a).shouldRender(a,frustum,camera.x,camera.y,camera.z);if(rendered!=mine)throw new IllegalStateException("LIVE EXPEDITION native private actor renderer leaked or hid the wrong socket's killer");if(rendered)visible++;}
                if(visible!=1)throw new IllegalStateException("LIVE EXPEDITION each socket must render exactly its own elk killer");double dx=target.getX()+.5-mc.player.getX(),dz=target.getZ()+.5-mc.player.getZ();mc.player.setYRot((float)Math.toDegrees(Math.atan2(-dx,dz)));mc.player.setXRot(0);shot=role+"-private-elk";ack(mc,19);
            }
        }
        if(step==22){mc.options.keyUp.setDown(false);mc.options.keyShift.setDown(true);if(ticks>30&&mc.player.isCrouching()&&ack!=22)ack(mc,22);}
        if(step==20){mc.options.keyUp.setDown(false);double dx=target.getX()+.5-mc.player.getX(),dz=target.getZ()+.5-mc.player.getZ();mc.player.setYRot((float)Math.toDegrees(Math.atan2(-dx,dz)));mc.player.setXRot(0);}
        if(step==21&&ticks==30){mc.options.keyUp.setDown(false);shot=role+"-private-leaves";ack(mc,21);}
        if(step==23){
            mc.options.keyShift.setDown(true);boolean left=role.equals("A");
            if(!data.getBoolean("MineTurn")){mc.options.keyUp.setDown(false);mc.options.keyAttack.setDown(false);return;}
            if(!YachtGlazingBlock.opening(block)){
                mc.options.keyUp.setDown(false);var to=target.getCenter().subtract(mc.player.getEyePosition());
                mc.player.setYRot((float)Math.toDegrees(Math.atan2(-to.x,to.z)));mc.player.setXRot((float)-Math.toDegrees(Math.atan2(to.y,Math.hypot(to.x,to.z))));
                // Holding the actual attack input lets Minecraft retain its native mining progress.
                // Direct post-tick mining calls are canceled by the next tick's released input.
                if(!mc.isWindowActive())org.lwjgl.glfw.GLFW.glfwFocusWindow(mc.getWindow().getWindow());
                if(!mc.mouseHandler.isMouseGrabbed()){
                    mc.mouseHandler.grabMouse();
                    // Grabbing the mouse suppresses attacks until Minecraft processes a
                    // released attack tick. Do that before holding the native mining key.
                    mc.options.keyAttack.setDown(false);return;
                }
                mc.options.keyAttack.setDown(mc.hitResult instanceof BlockHitResult hit&&hit.getBlockPos().equals(target));
            }else{
                mc.options.keyAttack.setDown(false);
                if(mc.player.getForcedPose()==net.minecraft.world.entity.Pose.SWIMMING)portholePoseSeen=true;
                if(!portholePoseSeen)return;
                if(left?mc.player.getX()<target.getX()-.8:mc.player.getX()>target.getX()+1.3){
                    mc.options.keyUp.setDown(false);if(ack!=23){shot=role+"-porthole";ack(mc,23);}
                }else walk(mc,left?target.west(3):target.east(3));
            }
        }
        if(step==23&&ticks%100==0)TheOldestHouse.LOGGER.info("LIVE PORTHOLE socket={} position={} target={} block={} pose={} forced={} origin={} crawlSeen={} mouseGrabbed={} focused={} attackHeld={} hit={} screen={}",role,mc.player.position(),target,block,mc.player.getPose(),mc.player.getForcedPose(),HouseSightlineState.origin(),portholePoseSeen,mc.mouseHandler.isMouseGrabbed(),mc.isWindowActive(),mc.options.keyAttack.isDown(),mc.hitResult instanceof BlockHitResult hit?hit.getBlockPos():mc.hitResult,mc.screen);
        if(step==24&&ticks>10)ack(mc,24);
        if(step==25&&ticks>15){
            var to=target.getCenter().subtract(mc.player.getEyePosition());mc.player.setYRot((float)Math.toDegrees(Math.atan2(-to.x,to.z)));mc.player.setXRot((float)-Math.toDegrees(Math.atan2(to.y,Math.hypot(to.x,to.z))));
            mc.player.getInventory().selected=0;mc.options.keyUse.setDown(true);if(!mc.player.isUsingItem())mc.gameMode.useItem(mc.player,InteractionHand.MAIN_HAND);
            for(int i=0;i<mc.player.getInventory().getContainerSize();i++){var photo=mc.player.getInventory().getItem(i);if(photo.is(net.minecraft.world.item.Items.FILLED_MAP)&&photo.has(DataComponents.MAP_ID)){
                var custom=photo.get(DataComponents.CUSTOM_DATA);if(custom!=null&&custom.copyTag().hasUUID(io.github.knaitoe.theoldesthouse.labyrinth.NovelVignettes.PHOTO_OWNER)&&custom.copyTag().getUUID(io.github.knaitoe.theoldesthouse.labyrinth.NovelVignettes.PHOTO_OWNER).equals(mc.player.getUUID())){if(ack!=25){shot=role+"-camera-view";ack(mc,25);}break;}}}
        }
        if(step==26&&ticks>5){mc.options.keyUse.setDown(false);for(int i=0;i<9;i++){var photo=mc.player.getInventory().getItem(i);if(photo.is(net.minecraft.world.item.Items.FILLED_MAP)&&photo.has(DataComponents.MAP_ID)){mc.player.getInventory().selected=i;if(ticks>30&&mc.level.getMapData(photo.get(DataComponents.MAP_ID))!=null&&ack!=26){shot=role+"-developed-frame";ack(mc,26);}break;}}}
        if(step>=27&&step<=29){mc.options.keyUse.setDown(false);mc.player.getInventory().selected=7;
            var to=target.getCenter().add(0,-.45,0).subtract(mc.player.getEyePosition());mc.player.setYRot((float)Math.toDegrees(Math.atan2(-to.x,to.z)));mc.player.setXRot((float)-Math.toDegrees(Math.atan2(to.y,Math.hypot(to.x,to.z))));
            if(ticks>20&&EndingBookClient.hasView(target)){
                if(step==27&&!EndingBookClient.visible(target)&&ack!=27){shot=role+"-ending-hidden";ack(mc,27);}
                if(step==28&&EndingBookClient.visible(target)==role.equals("A")&&ack!=28){shot=role+"-ending-personal";ack(mc,28);}
                if(step==29){
                    if(role.equals("A")&&EndingBookClient.visible(target)){
                        if(mc.screen instanceof LecternScreen){mc.gameMode.handleInventoryButtonClick(mc.player.containerMenu.containerId,3);mc.player.closeContainer();}else click(mc,target,ticks);
                    }else if(!EndingBookClient.visible(target)&&ack!=29){shot=role+"-ending-collected";ack(mc,29);}
                }
            }
        }
        if(step==32){
            mc.options.keyUp.setDown(false);mc.options.keyShift.setDown(false);
            var bounds=io.github.knaitoe.theoldesthouse.labyrinth.IndianLakeRooms.bounds(target,io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlace.GOATMAN);
            var children=mc.level.getEntitiesOfClass(io.github.knaitoe.theoldesthouse.labyrinth.GoatmanChild.class,bounds);
            if(ticks>40&&mc.screen==null&&ack!=32&&children.stream().filter(c->!c.girl()).count()==8&&children.stream().filter(c->c.viewer().filter(mc.player.getUUID()::equals).isPresent()).count()==1){
                var camera=mc.gameRenderer.getMainCamera().getPosition();var frustum=new net.minecraft.client.renderer.culling.Frustum(new org.joml.Matrix4f(),new org.joml.Matrix4f().ortho(-128,128,-128,128,-128,128));frustum.prepare(camera.x,camera.y,camera.z);
                for(var c:children){boolean mine=!c.girl()||c.viewer().filter(mc.player.getUUID()::equals).isPresent();var renderer=mc.getEntityRenderDispatcher().getRenderer(c);
                    // At the entrance cousins can be beyond native model draw distance;
                    // the nearby private path girls must obey ownership here.
                    if(c.girl()&&renderer.shouldRender(c,frustum,camera.x,camera.y,camera.z)!=mine)throw new IllegalStateException("LIVE EXPEDITION a native path girl was hidden or leaked to its peer");
                    if(mine&&mc.getResourceManager().getResource(renderer.getTextureLocation(c)).isEmpty())throw new IllegalStateException("LIVE EXPEDITION the actual trailer child renderer selected a missing skin");
                }
                var girl=children.stream().filter(c->c.viewer().filter(mc.player.getUUID()::equals).isPresent()).findFirst().orElseThrow();var at=girl.getEyePosition().subtract(mc.player.getEyePosition());mc.player.setYRot((float)Math.toDegrees(Math.atan2(-at.x,at.z)));mc.player.setXRot((float)-Math.toDegrees(Math.atan2(at.y,Math.hypot(at.x,at.z))));
                shot=role+"-trailer-arrival";ack(mc,32);
            }
        }
        if(step==33){
            var at=mc.player.position().subtract(target.getX(),target.getY(),target.getZ());var progress=io.github.knaitoe.theoldesthouse.labyrinth.GoatmanWoods.project(at).progress();
            var next=io.github.knaitoe.theoldesthouse.labyrinth.GoatmanWoods.path(progress+1).add(target.getX(),target.getY(),target.getZ());
            var toward=next.subtract(mc.player.position());mc.player.setYRot((float)Math.toDegrees(Math.atan2(-toward.x,toward.z)));mc.player.setXRot(0);mc.options.keyShift.setDown(false);mc.options.keyUp.setDown(true);
        }
        if(step==34){
            mc.options.keyUp.setDown(false);
            var children=mc.level.getEntitiesOfClass(io.github.knaitoe.theoldesthouse.labyrinth.GoatmanChild.class,io.github.knaitoe.theoldesthouse.labyrinth.IndianLakeRooms.bounds(target,io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlace.GOATMAN));
            if(ticks>40&&ack!=34&&children.stream().filter(c->!c.girl()).count()==8){
                var camera=mc.gameRenderer.getMainCamera().getPosition();var frustum=new net.minecraft.client.renderer.culling.Frustum(new org.joml.Matrix4f(),new org.joml.Matrix4f().ortho(-128,128,-128,128,-128,128));frustum.prepare(camera.x,camera.y,camera.z);
                for(var c:children)if(!c.girl()&&!mc.getEntityRenderDispatcher().getRenderer(c).shouldRender(c,frustum,camera.x,camera.y,camera.z))throw new IllegalStateException("LIVE EXPEDITION a shared cousin is invisible at the actual campsite");
                var at=net.minecraft.world.phys.Vec3.atCenterOf(target.offset(-5,1,-48)).subtract(mc.player.getEyePosition());mc.player.setYRot((float)Math.toDegrees(Math.atan2(-at.x,at.z)));mc.player.setXRot((float)-Math.toDegrees(Math.atan2(at.y,Math.hypot(at.x,at.z))));shot=role+"-trailer-camp";ack(mc,34);
            }
        }
        if(step==35||step==36){
            proofCamp=target;mc.options.keyUp.setDown(false);mc.options.keyShift.setDown(false);mc.options.hideGui=false;
            // The outside vigil requires the actual front door to be closed before the quiet.
            // Let one connected client use the normal interaction, rather than admit the impostor.
            if(step==35&&role.equals("A")&&ticks>8&&open(mc.level.getBlockState(target.offset(io.github.knaitoe.theoldesthouse.labyrinth.GoatmanWoods.DOOR))))click(mc,target.offset(io.github.knaitoe.theoldesthouse.labyrinth.GoatmanWoods.DOOR),ticks);
            var children=mc.level.getEntitiesOfClass(io.github.knaitoe.theoldesthouse.labyrinth.GoatmanChild.class,io.github.knaitoe.theoldesthouse.labyrinth.IndianLakeRooms.bounds(target,io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlace.GOATMAN));
            if(ticks%100==0)try{
                Files.createDirectories(LiveExpeditionProof.folder());Files.writeString(LiveExpeditionProof.folder().resolve(role+"-trailer-night-status.txt"),"phase="+step+" priority="+GoatmanClient.priorityDemand()+" cousin="+GoatmanClient.cousinSubtitle()+" sides="+heardSides+"\n"+String.join("\n",heardKnocks)+"\n"+children.stream().map(c->c.getUUID()+" role="+c.fear()+" stage="+c.fearStage()+" cower="+c.cowering()+" at="+c.position().subtract(target.getX(),target.getY(),target.getZ())).collect(java.util.stream.Collectors.joining("\n"))+"\n");
            }catch(Exception ex){throw new IllegalStateException(ex);}
            if(step==35){
                mc.player.setYRot(180);mc.player.setXRot(0);var roles=children.stream().map(io.github.knaitoe.theoldesthouse.labyrinth.GoatmanChild::fear).filter(n->n>0).collect(java.util.stream.Collectors.toSet());
                if(!confusionCaptured&&shot==null&&mc.screen==null&&roles.size()==7&&GoatmanClient.cousinSubtitle()&&children.stream().filter(c->c.fearStage()==1).count()==7&&children.stream().noneMatch(io.github.knaitoe.theoldesthouse.labyrinth.GoatmanChild::cowering)){shot=role+"-trailer-confusion";confusionCaptured=true;}
                if(confusionCaptured&&shot==null&&GoatmanClient.night()&&mc.screen==null&&roles.size()==7&&children.stream().filter(c->c.fearStage()==2).count()==7&&children.stream().noneMatch(io.github.knaitoe.theoldesthouse.labyrinth.GoatmanChild::cowering)&&ack!=35){shot=role+"-trailer-fear";ack(mc,35);}
            }else{
                mc.player.setYRot(0);mc.player.setXRot(0);
                boolean terror=children.stream().filter(c->c.fearStage()==3).count()==7&&children.stream().filter(io.github.knaitoe.theoldesthouse.labyrinth.GoatmanChild::cowering).count()==1;
                if(terror&&!terrorCaptured&&shot==null&&mc.screen==null){mc.player.setYRot(180);shot=role+"-trailer-terror";terrorCaptured=true;}
                if(terror&&terrorCaptured&&shot==null&&mc.screen==null&&GoatmanClient.priorityDemand()>=104&&!GoatmanClient.cousinSubtitle()&&heardSides.size()==3&&ack!=36){
                    shot=role+"-trailer-voice";try{Files.createDirectories(LiveExpeditionProof.folder());Files.writeString(LiveExpeditionProof.folder().resolve(role+"-trailer-sounds.txt"),String.join("\n",heardKnocks)+"\n");}catch(Exception ex){throw new IllegalStateException(ex);}ack(mc,36);
                }
            }
        }
        if(step==30&&ticks==30){mc.options.keyUp.setDown(false);mc.options.keyShift.setDown(false);mc.options.keyAttack.setDown(false);mc.options.keyUse.setDown(false);mc.stop();}
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
