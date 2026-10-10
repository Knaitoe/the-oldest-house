package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/** The actual baked assets and fear pose, in the same native client used for writing proof. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class PlaytestVisualProof extends Screen {
    private int frames;
    public PlaytestVisualProof(){super(Component.literal("October 9 native assets"));}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xffb5b8ab);g.drawString(font,"New cousins: standing / cowering. Distant boy. Native pixel items and furniture.",10,8,0xff1b2527,false);g.flush();
        var out=mc.renderBuffers().bufferSource();
        for(int i=0;i<6;i++){
            var model=new GoatmanChildModel(mc.getEntityModels().bakeLayer(ModelLayers.PLAYER));model.young=false;if(i>=3)model.cowerPose();
            var p=g.pose();p.pushPose();p.translate(28+i*(width-100)/7F,height*.55F,120);p.scale(38,38,38);p.mulPose(Axis.YP.rotationDegrees(165));p.translate(0,-1.5,0);
            model.renderToBuffer(p,out.getBuffer(RenderType.entityCutoutNoCull(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/trailer_child_"+(6+i%3)+".png"))),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xffffffff);p.popPose();
        }
        var boy=new LiteraryRenderers.CastModel(mc.getEntityModels().bakeLayer(LiteraryRenderers.CAST));boy.young=false;boy.pose(LiteraryActor.SILHOUETTE,4,0);
        var p=g.pose();p.pushPose();p.translate(width-50,height*.55F,120);p.scale(30,30,30);p.mulPose(Axis.YP.rotationDegrees(180));p.translate(0,-1.5,0);
        boy.renderToBuffer(p,out.getBuffer(RenderType.entityCutoutNoCull(LiteraryRenderers.skin(LiteraryActor.SILHOUETTE,4))),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xffffffff);p.popPose();out.endBatch();
        int n=0;for(var stack:new ItemStack[]{new ItemStack(NovelRegistry.CAMERA.get()),new ItemStack(NovelRegistry.ARCHIVE_KEY.get()),new ItemStack(DrownedTownRegistry.CHURCH_KEY.get()),new ItemStack(NovelRegistry.RIBBON.get()),new ItemStack(GoatmanRegistry.FRANKS.get())}){
            p.pushPose();p.translate(16+n++*52,height-58,0);p.scale(2,2,1);g.renderItem(stack,0,0);p.popPose();}
        var states=new net.minecraft.world.level.block.state.BlockState[]{HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.CHESS_TABLE,Direction.NORTH),NovelRegistry.SEALED_WINDOW.get().defaultBlockState(),NovelRegistry.PAPER.get().defaultBlockState()};
        for(int i=0;i<states.length;i++){if(mc.getBlockRenderer().getBlockModel(states[i])==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing 0.4.64 native model "+states[i]);
            p.pushPose();p.translate(width-146+i*47,height-22,150);p.scale(26,-26,26);p.mulPose(Axis.XP.rotationDegrees(20));p.mulPose(Axis.YP.rotationDegrees(25));mc.getBlockRenderer().renderSingleBlock(states[i],p,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);p.popPose();}out.endBatch();
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!Boolean.getBoolean("the_oldest_house.fontSmoke")||!(mc.screen instanceof PlaytestVisualProof proof)||++proof.frames<16)return;
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve("native-playtest-0464.png"));}
        Files.writeString(dir.resolve("playtest-assets-passed.txt"),"Additional cousin skins, shared cowering pose, distant boy, camera, keys, ribbon, packets, chess table, varied paper and boarded window rendered by the native client.\n");mc.setScreen(new TrailerVisualProof());
    }
}
