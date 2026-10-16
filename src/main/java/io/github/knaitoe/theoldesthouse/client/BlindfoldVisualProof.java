package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthRegistry;
import io.github.knaitoe.theoldesthouse.network.ClapGamePayload;
import java.nio.file.*;
import java.util.UUID;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/** Native standard/slim player heads and the same fitted layer used by actual PlayerRenderers. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class BlindfoldVisualProof extends Screen {
    private int page,frames;
    public BlindfoldVisualProof(){super(Component.literal("Blindfold fit and release"));}
    @Override protected void init(){
        if(BlindfoldWornLayer.registeredSkinCount()<2)throw new IllegalStateException("Blindfold layer missing from a native player skin renderer");
        var band=Minecraft.getInstance().getEntityModels().bakeLayer(BlindfoldWornLayer.LAYER).getChild("band");
        boolean[] fitted={false};
        band.visit(new com.mojang.blaze3d.vertex.PoseStack(),(pose,path,index,cube)->{if(index==0)fitted[0]=cube.minZ< -4.5F&&cube.minX<= -4.5F&&cube.maxX>=4.5F&&cube.minY<= -4&&cube.maxY>= -3;});
        if(!fitted[0])throw new IllegalStateException("Blindfold misses the native player's eye row or clips through the hat");
        if(!BlindfoldWornLayer.visible(new ItemStack(LabyrinthRegistry.BLINDFOLD.get()))||BlindfoldWornLayer.visible(ItemStack.EMPTY))throw new IllegalStateException("Blindfold worn-layer guard survives empty equipment");
        ClapGameClientState.accept(new ClapGamePayload(true,0,0));
        if(!ClapGameClientState.bound())throw new IllegalStateException("Native cloth lock was not applied");
        ClapGameClientState.accept(new ClapGamePayload(false,0,0));
        if(ClapGameClientState.bound()||ClapGameClientState.ending())throw new IllegalStateException("Completed native cloth state did not clear");
    }
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        var mc=Minecraft.getInstance();boolean worn=page==0;g.fill(0,0,width,height,0xff35302a);
        g.drawString(font,TheOldestHouse.displayName()+" / blindfold "+(worn?"eye fit and head tracking":"removed after completion"),10,9,0xffe9dfcd,false);g.flush();
        var out=mc.renderBuffers().bufferSource();float cw=width/4F,ch=(height-32)/2F,scale=Math.min(cw*1.35F,ch*1.25F);
        for(int i=0;i<8;i++){
            boolean slim=i%2==1;int view=i/2;var model=new PlayerModel<AbstractClientPlayer>(mc.getEntityModels().bakeLayer(slim?ModelLayers.PLAYER_SLIM:ModelLayers.PLAYER),slim);
            if(view==3){model.head.y=4.2F;model.head.yRot=.7F;model.head.xRot=.45F;}model.hat.copyFrom(model.head);
            var pose=g.pose();pose.pushPose();pose.translate((i%4+.5F)*cw,30+(i/4)*ch+ch*.72F,180);pose.scale(scale,scale,scale);pose.mulPose(Axis.YP.rotationDegrees(new float[]{180,90,0,180}[view]));
            var texture=DefaultPlayerSkin.get(new UUID(499,slim?1:0)).texture();var skin=out.getBuffer(RenderType.entityCutoutNoCull(texture));
            model.head.render(pose,skin,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);model.hat.render(pose,skin,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);
            if(worn)BlindfoldWornLayer.draw(model.head,mc.getEntityModels().bakeLayer(BlindfoldWornLayer.LAYER),pose,out,LightTexture.FULL_BRIGHT);
            out.endBatch();pose.popPose();g.drawCenteredString(font,(slim?"slim":"standard")+" / "+new String[]{"front","side","back","crouched head turn"}[view],(int)((i%4+.5F)*cw),(int)(30+(i/4+1)*ch-12),0xffe9dfcd);
        }
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post event)throws Exception{
        var mc=Minecraft.getInstance();if((!Boolean.getBoolean("the_oldest_house.fontSmoke")&&!VignetteReviewSmoke.active())||!(mc.screen instanceof BlindfoldVisualProof proof)||++proof.frames<24)return;
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);
        try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve("native-blindfold-"+(proof.page==0?"worn":"removed")+".png"));}
        if(++proof.page<2){proof.frames=0;return;}
        Files.writeString(dir.resolve("blindfold-fit-passed.txt"),"Both actual native player skin renderers have the fitted cloth layer. Standard/slim player heads show front/side/back and crouched head turn with the production head-attached band, side wraps and knot, then empty equipment. Actual eye-row/hat clearance and completed client lock removal checked. Staged native geometry, not an uninterrupted ordinary playthrough.\n");
        TheOldestHouse.LOGGER.info("BLINDFOLD FIT CHECK PASSED: both native player skins, head-following cloth and removed equipment");
        mc.setScreen(VignetteReviewSmoke.active()?new ClassicsVisualProof():new StaceyScrabbleVisualProof());
    }
}
