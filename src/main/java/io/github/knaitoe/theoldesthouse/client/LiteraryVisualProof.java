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
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
/** Registered native meshes and every prop variant are rendered before architecture QA. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class LiteraryVisualProof extends Screen {
    private int frames;
    public LiteraryVisualProof(){super(Component.literal("The literary wings — native meshes"));}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float delta){}
    @Override public void render(GuiGraphics g,int x,int y,float delta){var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xff262320);g.drawString(font,"Literary wings / tailored cast, antlerless elk, native fixtures and keepsakes",10,8,0xffeee0c4,false);g.flush();
        for(int i=0;i<16;i++){var model=new LiteraryRenderers.CastModel(mc.getEntityModels().bakeLayer(LiteraryRenderers.CAST));int phase=i==1?1:i==7?2:i>=10&&i<=12?1:0;model.pose(i,phase,frames);var skin=LiteraryRenderers.skin(i,phase);if(mc.getResourceManager().getResource(skin).isEmpty())throw new IllegalStateException("Missing literary cast atlas "+skin);var p=g.pose();p.pushPose();p.translate(15+(i%8)*(width-25)/8,26+(i/8)*71,190);p.scale(29,29,29);p.mulPose(Axis.YP.rotationDegrees(155));var buffers=mc.renderBuffers().bufferSource();model.renderToBuffer(p,buffers.getBuffer(RenderType.entityCutoutNoCull(skin)),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xffffffff);buffers.endBatch();p.popPose();}
        int n=0;for(var kind:LiteraryPropBlock.Kind.values()){for(int stage=0;stage<4;stage++)for(var face:net.minecraft.core.Direction.Plane.HORIZONTAL){var s=LiteraryRegistry.PROP.get().defaultBlockState().setValue(LiteraryPropBlock.KIND,kind).setValue(LiteraryPropBlock.STAGE,stage).setValue(LiteraryPropBlock.FACING,face);if(mc.getBlockRenderer().getBlockModel(s)==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing literary fixture "+s);}var s=LiteraryRegistry.PROP.get().defaultBlockState().setValue(LiteraryPropBlock.KIND,kind).setValue(LiteraryPropBlock.STAGE,frames/6%4);var p=g.pose();p.pushPose();p.translate(20+(n%9)*(width-40)/9,198+(n/9)*62,185);p.scale(20,-20,20);p.mulPose(Axis.YP.rotationDegrees(-25));mc.getBlockRenderer().renderSingleBlock(s,p,mc.renderBuffers().bufferSource(),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);mc.renderBuffers().bufferSource().endBatch();p.popPose();n++;}
        var elk=new LiteraryRenderers.ElkModel(mc.getEntityModels().bakeLayer(LiteraryRenderers.ELK));elk.lying();var p=g.pose();p.pushPose();p.translate(85,height-40,185);p.scale(26,-26,26);p.mulPose(Axis.YP.rotationDegrees(65));var buffers=mc.renderBuffers().bufferSource();elk.renderToBuffer(p,buffers.getBuffer(RenderType.entityCutoutNoCull(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/literary_elk_wounded.png"))),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xffffffff);buffers.endBatch();p.popPose();
        int item=0;for(var value:new Item[]{LiteraryRegistry.TOOTH.get(),LiteraryRegistry.KEY.get(),LiteraryRegistry.KNIFE.get(),LiteraryRegistry.JAR.get(),LiteraryRegistry.PHOTOGRAPH.get(),LiteraryRegistry.FILM.get(),LiteraryRegistry.MEAL.get(),LiteraryRegistry.STEW.get(),LiteraryRegistry.CYLINDER_ONE.get(),LiteraryRegistry.CYLINDER_TWO.get(),LiteraryRegistry.CYLINDER_THREE.get()}){var stack=new ItemStack(value);var mesh=mc.getItemRenderer().getModel(stack,null,null,0);if(mesh==mc.getModelManager().getMissingModel()||!mesh.isGui3d())throw new IllegalStateException("Literary keepsake lacks a native mesh: "+value);g.renderItem(stack,170+(item++)*30,height-26);}
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{var mc=Minecraft.getInstance();if(!Boolean.getBoolean("the_oldest_house.fontSmoke")||!(mc.screen instanceof LiteraryVisualProof proof)||++proof.frames<24)return;var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve("native-literary.png"));}Files.writeString(dir.resolve("literary-passed.txt"),"24 native frames: sixteen cast roles and altered appearances, the jointed antlerless elk, 288 native prop variants, and eleven three-dimensional keepsakes.\n");TheOldestHouse.LOGGER.info("LITERARY MESH CHECK PASSED: native models and authored atlases");mc.setScreen(new LiteraryEffectsVisualProof());}
}
