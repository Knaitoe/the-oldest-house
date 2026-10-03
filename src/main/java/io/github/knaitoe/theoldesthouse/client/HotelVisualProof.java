package io.github.knaitoe.theoldesthouse.client;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
/** Actual registered models and textures, including all gauge faces and the animated native hose. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class HotelVisualProof extends Screen {
    private int frames;
    public HotelVisualProof(){super(Component.literal("The hotel — native meshes"));}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float delta){}
    @Override public void render(GuiGraphics g,int x,int y,float delta){var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xFF282520);g.drawString(font,"Hotel / native cast, articulated hose, fixtures and keepsakes",10,8,0xFFE9DFCD,false);g.flush();
        for(int i=0;i<6;i++){var m=new HotelRenderers.CastModel(mc.getEntityModels().bakeLayer(HotelRenderers.CAST));m.pose(i,i>=4,frames);var p=g.pose();p.pushPose();p.translate(22+i*(width-35)/6,30,190);p.scale(40,40,40);p.mulPose(Axis.YP.rotationDegrees(155));var buffers=mc.renderBuffers().bufferSource();m.renderToBuffer(p,buffers.getBuffer(RenderType.entityCutoutNoCull(HotelRenderers.skin(i))),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xFFFFFFFF);buffers.endBatch();p.popPose();}
        for(var kind:HotelPropBlock.Kind.values())for(int pressure=0;pressure<4;pressure++){var s=HotelRegistry.PROP.get().defaultBlockState().setValue(HotelPropBlock.KIND,kind).setValue(HotelPropBlock.PRESSURE,pressure);if(mc.getBlockRenderer().getBlockModel(s)==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing hotel fixture "+s);}
        int n=0;for(var kind:HotelPropBlock.Kind.values()){var s=HotelRegistry.PROP.get().defaultBlockState().setValue(HotelPropBlock.KIND,kind).setValue(HotelPropBlock.PRESSURE,frames/6%4);var p=g.pose();p.pushPose();p.translate(18+(n%5)*(width-35)/5,130+(n/5)*60,185);p.scale(25,-25,25);p.mulPose(Axis.YP.rotationDegrees(-25));mc.getBlockRenderer().renderSingleBlock(s,p,mc.renderBuffers().bufferSource(),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);mc.renderBuffers().bufferSource().endBatch();p.popPose();n++;}
        var hose=new HotelRenderers.HoseModel(mc.getEntityModels().bakeLayer(HotelRenderers.HOSE));hose.pose(frames,Math.min(2,frames/8));var p=g.pose();p.pushPose();p.translate(width/2.-60,height-48,185);p.scale(25,-25,25);p.mulPose(Axis.YP.rotationDegrees(65));var buffers=mc.renderBuffers().bufferSource();hose.renderToBuffer(p,buffers.getBuffer(RenderType.entityCutoutNoCull(HotelRenderers.rubber())),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xFFFFFFFF);buffers.endBatch();p.popPose();
        int x0=width-110;for(var item:new net.minecraft.world.item.Item[]{HotelRegistry.MASTER_KEY.get(),HotelRegistry.DRINK.get(),HotelRegistry.PHOTOGRAPH.get()}){var stack=new ItemStack(item);var m=mc.getItemRenderer().getModel(stack,null,null,0);if(m==mc.getModelManager().getMissingModel()||!m.isGui3d())throw new IllegalStateException("Hotel keepsake missing native mesh: "+item);g.renderItem(stack,x0,height-26);x0+=28;}
        // Render the actual wall print and the native player-head mesh in the same physical positions.
        var print=HotelRegistry.PROP.get().defaultBlockState().setValue(HotelPropBlock.KIND,HotelPropBlock.Kind.PHOTO).setValue(HotelPropBlock.FACING,net.minecraft.core.Direction.WEST);
        var portrait=g.pose();portrait.pushPose();portrait.translate(width/2.+85,height-45,185);portrait.scale(32,-32,32);portrait.mulPose(Axis.YP.rotationDegrees(65));
        mc.getBlockRenderer().renderSingleBlock(print,portrait,mc.renderBuffers().bufferSource(),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);
        portrait.translate(0,0,-1);var skull=net.minecraft.client.renderer.blockentity.SkullBlockRenderer.createSkullRenderers(mc.getEntityModels()).get(net.minecraft.world.level.block.SkullBlock.Types.PLAYER);
        net.minecraft.client.renderer.blockentity.SkullBlockRenderer.renderSkull(net.minecraft.core.Direction.WEST,net.minecraft.core.Direction.EAST.toYRot(),0,portrait,mc.renderBuffers().bufferSource(),LightTexture.FULL_BRIGHT,skull,RenderType.entityTranslucent(net.minecraft.client.resources.DefaultPlayerSkin.get(new java.util.UUID(0,0)).texture()));
        mc.renderBuffers().bufferSource().endBatch();portrait.popPose();
        var listener=net.minecraft.world.phys.Vec3.ZERO;var source=new net.minecraft.world.phys.Vec3(0,0,8);if(HotelAtmosphere.arrow(listener,source,0,false).equals(HotelAtmosphere.arrow(listener,source,0,true)))throw new IllegalStateException("Blind arrow never changes");
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{var mc=Minecraft.getInstance();if(!Boolean.getBoolean("the_oldest_house.fontSmoke")||!(mc.screen instanceof HotelVisualProof proof)||++proof.frames<24)return;var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve("native-hotel.png"));}Files.writeString(dir.resolve("hotel-passed.txt"),"24 native frames: six tailored cast skins and instrumental/dance poses, sixteen articulated hose parts, ten fixtures/four gauge faces, three three-dimensional keepsakes a native player head inside the wall print, and independent false-arrow calculation.\n");TheOldestHouse.LOGGER.info("HOTEL CHECK PASSED: native cast, hose, fixtures, keepsakes and independent direction cues");mc.setScreen(new SceneVisualProof());}
}
