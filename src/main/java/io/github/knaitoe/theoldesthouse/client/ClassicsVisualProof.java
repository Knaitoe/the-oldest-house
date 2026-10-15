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
/** Actual registered cast layer, native multi-cube items and all four wallpaper meshes. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class ClassicsVisualProof extends Screen {
    private static final String[] POSES={"seated","flight","standing"},VIEWS={"front","side","back","dim"};
    private int frames,page;
    public ClassicsVisualProof(){super(Component.literal("The sitting and the pattern"));}
    @Override public void renderBackground(GuiGraphics graphics,int x,int y,float delta){}
    @Override public void render(GuiGraphics g,int x,int y,float delta){
        var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xFF27231E);g.drawString(font,"Native séance / "+POSES[page/4]+" / "+VIEWS[page%4]+" / veil, skirts, cuffs and lap poses",12,9,0xFFE8DDC5,false);g.flush();
        for(int i=0;i<4;i++){
            var model=new SeanceRenderers.FamilyModel(mc.getEntityModels().bakeLayer(SeanceRenderers.LAYER));model.young=false;model.pose(i,page/4==0,page/4==1,frames);
            if(page/4==1){model.leftLeg.xRot=(float)Math.cos(frames*.66F)*.5F;model.rightLeg.xRot=-model.leftLeg.xRot;if(i==3){HumanoidMotion.gait(model,frames*.4F,.6F,true);HumanoidMotion.connect(model);}}
            var pose=g.pose();pose.pushPose();float size=65*(i==3?.72F:1);pose.translate(70+i*(width-45)/4,45+65*.52F+1.5F*(65-size),180);pose.scale(size,size,size);pose.mulPose(Axis.YP.rotationDegrees(new float[]{180,90,0,145}[page%4]));
            var buffers=mc.renderBuffers().bufferSource();model.renderToBuffer(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(SeanceRenderers.skin(i))),page%4==3?LightTexture.pack(7,0):LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xFFFFFFFF);buffers.endBatch();pose.popPose();
        }
        for(int i=0;i<4;i++){
            var state=ClassicsRegistry.WALLPAPER.get().defaultBlockState().setValue(WallpaperPanelBlock.FIGURE,(i&1)!=0).setValue(WallpaperPanelBlock.PEELED,(i&2)!=0);
            if(mc.getBlockRenderer().getBlockModel(state)==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing native wallpaper model "+state);
            var pose=g.pose();pose.pushPose();pose.translate(30+i*(width-50)/4,height-88,180);pose.scale(42,-42,42);pose.mulPose(Axis.YP.rotationDegrees(-15));mc.getBlockRenderer().renderSingleBlock(state,pose,mc.renderBuffers().bufferSource(),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);mc.renderBuffers().bufferSource().endBatch();pose.popPose();
        }
        int x0=width/2-45;for(var item:new net.minecraft.world.item.Item[]{ClassicsRegistry.PLANCHETTE.get(),ClassicsRegistry.FOLIO.get()}){
            var stack=new ItemStack(item);var baked=mc.getItemRenderer().getModel(stack,null,null,0);if(baked==mc.getModelManager().getMissingModel()||!baked.isGui3d())throw new IllegalStateException("Keepsake is not a native three-dimensional mesh: "+item);
            var pose=g.pose();pose.pushPose();pose.translate(x0,height-36,0);pose.scale(2,2,2);g.renderItem(stack,0,0);pose.popPose();x0+=60;
        }
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post event)throws Exception{
        var mc=Minecraft.getInstance();if((!Boolean.getBoolean("the_oldest_house.fontSmoke")&&!VignetteReviewSmoke.active())||!(mc.screen instanceof ClassicsVisualProof proof)||++proof.frames<24)return;
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve("native-classics-"+POSES[proof.page/4]+"-"+VIEWS[proof.page%4]+".png"));if(proof.page==0)image.writeToFile(dir.resolve("native-classics.png"));}
        if(++proof.page<12){proof.frames=0;return;}
        Files.writeString(dir.resolve("classics-passed.txt"),"Twelve saved native views: all four unchanged cast skins in seated, flight and standing poses, front/side/back/dim, original adult proportions and daughter scale; all four animated/torn wallpaper meshes and both three-dimensional keepsakes. Staged mesh/UV checks, not ordinary complete playthroughs.\n");TheOldestHouse.LOGGER.info("CLASSICS CHECK PASSED: twelve registered native cast, wallpaper and item views");mc.setScreen(VignetteReviewSmoke.active()?new LiteraryPhaseVisualProof():new HotelVisualProof());
    }
}
