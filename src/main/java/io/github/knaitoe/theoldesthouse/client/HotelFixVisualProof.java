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
        var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xff302c25);g.drawString(font,new String[]{"Closing night / the guest belongs to the photograph","Fire hose / joined ribbon in all three phases","Room 217 / the woman in the bath","Brass numbers / all four wall orientations"}[page],10,10,0xffe9dfcd,false);g.flush();var p=g.pose();var buffers=mc.renderBuffers().bufferSource();
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
            p.pushPose();p.translate(width*.44,height*.82,180);p.scale(90,-90,90);p.mulPose(Axis.XP.rotationDegrees(16));p.mulPose(Axis.YP.rotationDegrees(145));
            for(int z=0;z<2;z++){p.pushPose();p.translate(0,0,z);block(StanleyBlock.of(StanleyBlock.Kind.TUB,Direction.NORTH,z),p,buffers);p.popPose();}
            p.pushPose();p.translate(.5,.45,.9);p.mulPose(Axis.YP.rotationDegrees(180));p.scale(-1,-1,1);p.translate(0,-1.501,0);var model=new HotelRenderers.CastModel(mc.getEntityModels().bakeLayer(HotelRenderers.CAST));model.preview(6,false,frames,HotelActor.IDLE,0);model.renderToBuffer(p,buffers.getBuffer(RenderType.entityCutoutNoCull(HotelRenderers.skin(6))),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,-1);p.popPose();buffers.endBatch();p.popPose();
        }else{
            int i=0;for(var facing:Direction.Plane.HORIZONTAL){p.pushPose();p.translate(25+(i+.5)*(width-50)/4.,height*.48,180);p.scale(70,-70,70);p.mulPose(Axis.YP.rotationDegrees(facing.toYRot()));p.translate(-.5,-.5,-.5);block(HouseBlocks.HOTEL_ROOM_PLAQUE.get().defaultBlockState().setValue(HotelRoomPlaqueBlock.FACING,facing),p,buffers);HotelRoomPlaqueRenderer.engrave(font,i%2==0?217:237,facing,p,buffers,LightTexture.FULL_BRIGHT);buffers.endBatch();p.popPose();g.drawCenteredString(font,facing.getName(),(int)(25+(i+.5)*(width-50)/4.),(int)(height*.48+28),0xffe9dfcd);i++;}
            p.pushPose();p.translate(width*.5,height*.88,180);p.scale(30,-30,30);p.mulPose(Axis.XP.rotationDegrees(12));p.mulPose(Axis.YP.rotationDegrees(40));for(int yy=0;yy<3;yy++){p.pushPose();p.translate(0,yy,1);block(Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(),p,buffers);p.popPose();}p.translate(0,1,0);block(StanleyBlock.of(StanleyBlock.Kind.SCONCE,Direction.NORTH),p,buffers);buffers.endBatch();p.popPose();
        }
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post ignored)throws Exception{var mc=Minecraft.getInstance();if(!(mc.screen instanceof HotelFixVisualProof proof)||++proof.frames<12)return;var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);try(var shot=Screenshot.takeScreenshot(mc.getMainRenderTarget())){shot.writeToFile(dir.resolve("native-hotel-fitting-"+proof.page+".png"));}proof.frames=0;if(++proof.page<4)return;if(proof.photo!=null)mc.getTextureManager().release(proof.photo);Files.writeString(dir.resolve("hotel-fitting-passed.txt"),"Native flat sitter, connected three-phase hose, fitted bath ghost, flush four-facing brass and wall-mounted light.\n");if(Boolean.getBoolean("the_oldest_house.hotelFixSmoke"))mc.stop();else mc.setScreen(new StanleyVisualProof());}
}
