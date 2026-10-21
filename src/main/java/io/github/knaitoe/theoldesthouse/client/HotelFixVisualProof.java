package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

/** Close native views of the exact print, hose, bath and mounted plaque production paths. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class HotelFixVisualProof extends Screen {
    private int page,frames;private ResourceLocation photo;private static boolean launched;
    public HotelFixVisualProof(){super(Component.literal("Hotel fitting checks"));}
    @SubscribeEvent public static void launch(ClientTickEvent.Post ignored){var mc=Minecraft.getInstance();if(Boolean.getBoolean("the_oldest_house.hotelFixSmoke")&&!launched&&mc.screen!=null&&mc.getOverlay()==null){launched=true;mc.options.guiScale().set(2);mc.resizeDisplay();mc.setScreen(new HotelFixVisualProof());}}
    private static void block(BlockState state,PoseStack pose,MultiBufferSource.BufferSource buffers){Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state,pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float delta){}
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xff302c25);g.drawString(font,new String[]{"Closing night / the guest belongs to the photograph","Fire hose / joined ribbon in all three phases","Room 217 / the woman in the bath","Brass numbers / all four wall orientations","Wall mounts / front and side","The decaying woman / rise, grasp, disappear","The woman / native face and jaw review"}[page],10,10,0xffe9dfcd,false);g.flush();var p=g.pose();var buffers=mc.renderBuffers().bufferSource();
        if(page==0){
            if(photo==null)try(var stream=mc.getResourceManager().open(net.minecraft.client.resources.DefaultPlayerSkin.get(new java.util.UUID(0,0)).texture());var skin=NativeImage.read(stream);var source=mc.getResourceManager().open(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/block/stanley/party.png"));var original=NativeImage.read(source)){
                var painted=HotelPhotoPainter.paint(skin,original);int changed=0;for(int py=0;py<64;py++)for(int px=0;px<64;px++){int c=painted.getPixelRGBA(px,py);if(c>>>24!=255)throw new IllegalStateException("The party print has a transparent hole");if(c!=original.getPixelRGBA(px,py)){changed++;if(px<28||px>36||py<38||py>55)throw new IllegalStateException("The sitter repaints the other guests");}}
                if(changed<30||changed>150)throw new IllegalStateException("The integrated sitter is not the crowd's pixel size: "+changed);photo=mc.getTextureManager().register("hotel_fitting_photo",new DynamicTexture(painted));
            }catch(java.io.IOException e){throw new IllegalStateException(e);}
            p.pushPose();p.translate(width*.36,height*.82,180);p.scale(100,-100,100);p.mulPose(Axis.YP.rotationDegrees(65));block(HotelRegistry.PROP.get().defaultBlockState().setValue(HotelPropBlock.KIND,HotelPropBlock.Kind.PHOTO).setValue(HotelPropBlock.FACING,Direction.WEST),p,buffers);HotelPhotoRenderer.front(p,buffers,Direction.WEST,photo,LightTexture.FULL_BRIGHT);buffers.endBatch();p.popPose();
        }else if(page==1){
            for(int phase=0;phase<3;phase++)for(int angle=0;angle<2;angle++){
                p.pushPose();p.translate((phase+.5)*width/3,80+angle*(height-100)/2.,180);p.scale(75,-75,75);p.mulPose(Axis.XP.rotationDegrees(50));p.mulPose(Axis.YP.rotationDegrees(angle*65));p.translate(0,-1.5,0);var hose=new HotelRenderers.HoseModel(mc.getEntityModels().bakeLayer(HotelRenderers.HOSE));hose.pose(frames,phase);hose.renderToBuffer(p,buffers.getBuffer(RenderType.entityCutoutNoCull(HotelRenderers.rubber())),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,-1);buffers.endBatch();p.popPose();
                for(int i=0;i<HotelRenderers.HoseModel.SEGMENTS;i++){var a=HotelRenderers.HoseModel.point(frames,phase,i);var b=HotelRenderers.HoseModel.point(frames,phase,i+1);if(Math.hypot(a[0]-b[0],a[1]-b[1])>3)throw new IllegalStateException("Disconnected hose span");}
            }
        }else if(page==2){
            p.pushPose();p.translate(width*.44,height*.82,180);p.scale(90,-90,90);p.mulPose(Axis.XP.rotationDegrees(16));p.mulPose(Axis.YP.rotationDegrees(35));
            for(int z=0;z<2;z++){p.pushPose();p.translate(0,0,z);block(StanleyBlock.of(StanleyBlock.Kind.TUB,Direction.NORTH,z+2),p,buffers);p.popPose();}
            p.pushPose();p.translate(.5,.45,.9);p.mulPose(Axis.YP.rotationDegrees(180));p.scale(-1,-1,1);p.translate(0,-1.501,0);var model=new HotelBathRenderer.Model(mc.getEntityModels().bakeLayer(HotelBathRenderer.LAYER));model.pose(HotelBathHaunt.RISING,45,frames,0,0);model.renderToBuffer(p,buffers.getBuffer(RenderType.entityCutoutNoCull(HotelRenderers.skin(6))),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,-1);p.popPose();buffers.endBatch();p.popPose();
        }else if(page==3){
            int i=0;for(var facing:Direction.Plane.HORIZONTAL){for(int ink=0;ink<2;ink++){p.pushPose();p.translate(25+(i+.5)*(width-50)/4.,height*.32+ink*80,180);p.scale(70,-70,70);p.mulPose(Axis.YP.rotationDegrees(facing.toYRot()));p.translate(-.5,-.5,-.5);block(HouseBlocks.HOTEL_ROOM_PLAQUE.get().defaultBlockState().setValue(HotelRoomPlaqueBlock.FACING,facing),p,buffers);if(ink==1)HotelRoomPlaqueRenderer.engrave(font,i%2==0?217:237,facing,p,buffers,LightTexture.FULL_BRIGHT);buffers.endBatch();p.popPose();}g.drawCenteredString(font,facing.getName(),(int)(25+(i+.5)*(width-50)/4.),(int)(height*.32+108),0xffe9dfcd);i++;}
            p.pushPose();p.translate(width*.5,height*.88,180);p.scale(30,-30,30);p.mulPose(Axis.XP.rotationDegrees(12));p.mulPose(Axis.YP.rotationDegrees(220));for(int yy=0;yy<3;yy++){p.pushPose();p.translate(0,yy,1);block(Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(),p,buffers);p.popPose();}p.pushPose();p.translate(0,2,0);block(Blocks.DARK_OAK_PLANKS.defaultBlockState(),p,buffers);p.popPose();p.translate(0,1,0);block(StanleyBlock.of(StanleyBlock.Kind.SCONCE,Direction.NORTH),p,buffers);buffers.endBatch();p.popPose();
        }else if(page==4){
            int i=0;for(var facing:Direction.Plane.HORIZONTAL){
                for(int side=0;side<2;side++){
                    p.pushPose();p.translate((i+.5)*width/4,90+side*150,180);p.scale(65,-65,65);p.mulPose(Axis.XP.rotationDegrees(12));p.mulPose(Axis.YP.rotationDegrees(facing.toYRot()+side*65));p.translate(-.5,-.5,-.5);
                    var behind=BlockPos.ZERO.relative(facing.getOpposite());p.pushPose();p.translate(behind.getX(),0,behind.getZ());block(Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(),p,buffers);p.popPose();
                    block(StanleyBlock.of(StanleyBlock.Kind.SCONCE,facing),p,buffers);buffers.endBatch();p.popPose();
                }g.drawCenteredString(font,facing.getName()+" / wall + arm + shade",(int)((i+.5)*width/4),height-20,0xffe9dfcd);i++;
            }
        }else if(page==5){
            int[] phases={HotelBathHaunt.RISING,HotelBathHaunt.PURSUIT,HotelBathHaunt.FADING},ages={35,120,8};
            for(int i=0;i<3;i++)for(int side=0;side<2;side++){
                p.pushPose();p.translate((i+.5)*width/3,170+side*160,180);p.scale(70,70,-70);p.mulPose(Axis.YP.rotationDegrees(side*65));p.translate(0,-1.5,0);
                var model=new HotelBathRenderer.Model(mc.getEntityModels().bakeLayer(HotelBathRenderer.LAYER));model.pose(phases[i],ages[i],frames,1.5F,.3F);
                model.renderToBuffer(p,buffers.getBuffer(RenderType.entityTranslucent(HotelBathRenderer.skin())),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,-1);buffers.endBatch();p.popPose();
                g.drawCenteredString(font,new String[]{"Under the water","Reaching in the hall","Disappearing"}[i],(int)((i+.5)*width/3),190+side*160,0xffe9dfcd);
            }
            var image=((DynamicTexture)mc.getTextureManager().getTexture(HotelBathRenderer.skin())).getPixels();
            if(image==null||image.getWidth()!=64||image.getHeight()!=64||image.getPixelRGBA(8,9)==image.getPixelRGBA(11,10))throw new IllegalStateException("The decayed woman lost her hooded eight-pixel eyes");
        }else{
            for(int i=0;i<3;i++){
                p.pushPose();p.translate((i+.5)*width/3,height*.55,180);p.scale(210,210,-210);p.mulPose(Axis.YP.rotationDegrees(i*35));
                var model=new HotelBathRenderer.Model(mc.getEntityModels().bakeLayer(HotelBathRenderer.LAYER));model.pose(HotelBathHaunt.PURSUIT,80,frames,0,0);
                model.renderHead(p,buffers.getBuffer(RenderType.entityCutoutNoCull(HotelBathRenderer.skin())),i==2?LightTexture.pack(7,7):LightTexture.FULL_BRIGHT);buffers.endBatch();p.popPose();
                g.drawCenteredString(font,new String[]{"Front","Three-quarter","Profile / dim"}[i],(int)((i+.5)*width/3),(int)(height*.68),0xffe9dfcd);
            }
        }
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post ignored)throws Exception{var mc=Minecraft.getInstance();if(!(mc.screen instanceof HotelFixVisualProof proof)||++proof.frames<12)return;var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);try(var shot=Screenshot.takeScreenshot(mc.getMainRenderTarget())){if(proof.page==3){int factor=shot.getWidth()/proof.width;for(int i=0;i<4;i++){int cx=(int)(25+(i+.5)*(proof.width-50)/4.)*factor,cy=(int)(proof.height*.32)*factor,changed=0;for(int yy=-14*factor;yy<=14*factor;yy++)for(int xx=-27*factor;xx<=27*factor;xx++)if(shot.getPixelRGBA(cx+xx,cy+yy)!=shot.getPixelRGBA(cx+xx,cy+yy+80*factor))changed++;if(changed<20)throw new IllegalStateException("Plaque ink is invisible on native facing "+i+": "+changed);}}shot.writeToFile(dir.resolve("native-hotel-fitting-"+proof.page+".png"));if((proof.page==2||proof.page>=4)&&Boolean.getBoolean("the_oldest_house.hotelFixSmoke")){System.out.println("HOTEL_MOUNT_VIEW_BEGIN "+proof.page);String encoded=java.util.Base64.getEncoder().encodeToString(Files.readAllBytes(dir.resolve("native-hotel-fitting-"+proof.page+".png")));for(int i=0;i<encoded.length();i+=900)System.out.println("HOTEL_MOUNT_VIEW_DATA "+encoded.substring(i,Math.min(i+900,encoded.length())));System.out.println("HOTEL_MOUNT_VIEW_END");}}proof.frames=0;if(++proof.page<7)return;if(proof.photo!=null)mc.getTextureManager().release(proof.photo);Files.writeString(dir.resolve("hotel-fitting-passed.txt"),"Native flat sitter, connected three-phase hose, decaying human and filled bath, flush four-facing brass and four-facing wall-supported lamp with continuous visible arm.\n");if(Boolean.getBoolean("the_oldest_house.hotelFixSmoke"))mc.stop();else mc.setScreen(new StanleyVisualProof());}
}
