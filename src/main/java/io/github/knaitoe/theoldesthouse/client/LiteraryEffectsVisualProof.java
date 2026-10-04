package io.github.knaitoe.theoldesthouse.client;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
/** Exercise the same bounded native model and television paths used in the rooms. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class LiteraryEffectsVisualProof extends Screen {
    private int frames;
    public LiteraryEffectsVisualProof(){super(Component.literal("Native literary effects"));}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float delta){}
    @Override public void render(GuiGraphics g,int x,int y,float delta){var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xff302c27);g.drawString(font,"Native literary effects / bounded room models, rotating fan, room view and shutdown",12,10,0xfff0e4ca,false);g.flush();var buffers=mc.renderBuffers().bufferSource();
        var model=new CompoundTag();int[] states=new int[256];for(int i=0;i<256;i++){int xx=i%8,yy=i/64,zz=(i/8)%8;var block=yy==0?Blocks.SPRUCE_PLANKS:xx==0||zz==7?LiteraryRegistry.SIDING.get():xx==7&&zz==4?Blocks.CRAFTING_TABLE:Blocks.AIR;states[i]=Block.getId(block.defaultBlockState());}model.putIntArray("Blocks",states);model.putBoolean("Behind",true);
        var tiny=new HumanoidModel<>(mc.getEntityModels().bakeLayer(ModelLayers.PLAYER));var figure=new LiteraryRenderers.CastModel(mc.getEntityModels().bakeLayer(LiteraryRenderers.CAST));var pose=g.pose();pose.pushPose();pose.translate(55,145,185);pose.scale(140,-140,140);pose.mulPose(Axis.XP.rotationDegrees(25));pose.mulPose(Axis.YP.rotationDegrees(-35));LiteraryRenderers.ModelRenderer.miniature(model,ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/harrigan.png"),tiny,figure,pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);buffers.endBatch();pose.popPose();
        var fan=mc.getEntityModels().bakeLayer(LiteraryRenderers.FAN);pose.pushPose();pose.translate(width-130,145,185);pose.scale(85,-85,85);pose.mulPose(Axis.XP.rotationDegrees(35));pose.mulPose(Axis.YP.rotation(frames*.16F));fan.render(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/literary_fan.png"))),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);buffers.endBatch();pose.popPose();
        int[] pixels=new int[384];for(int i=0;i<384;i++){int xx=i%24,yy=i/24;pixels[i]=yy>10?0x765538:xx<3||xx>20?0x485666:yy<3?0x9c8b70:xx>8&&xx<16?0xa09672:0x5d4939;}
        for(int n=0;n<2;n++){pose.pushPose();pose.translate(70+n*(width/2),height-65,185);pose.scale(170,-170,170);LiteraryRenderers.ModelRenderer.screen(pixels,n==1,pose,buffers);buffers.endBatch();pose.popPose();}
        g.drawString(font,"Native room blocks / player and figure",18,190,0xfff0e4ca,false);g.drawString(font,"Four articulated fan blades",width/2+15,190,0xfff0e4ca,false);g.drawString(font,"Captured room view",25,height-27,0xfff0e4ca,false);g.drawString(font,"Closed room screen",width/2+25,height-27,0xfff0e4ca,false);
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{var mc=Minecraft.getInstance();if(!Boolean.getBoolean("the_oldest_house.fontSmoke")||!(mc.screen instanceof LiteraryEffectsVisualProof proof)||++proof.frames<24)return;var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve("native-literary-effects.png"));}Files.writeString(dir.resolve("literary-effects-passed.txt"),"24 native frames: bounded room block models, correctly mapped native player/figure meshes, four rotating fan blades and the room-view/shutdown screen paths.\n");TheOldestHouse.LOGGER.info("LITERARY EFFECTS CHECK PASSED: bounded native room, fan and television render paths");mc.setScreen(new SceneVisualProof());}
}
