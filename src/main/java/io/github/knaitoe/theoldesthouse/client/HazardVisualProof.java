package io.github.knaitoe.theoldesthouse.client;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import java.util.UUID;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/** Baked collision-matched environment meshes, both lamp states and the native standard/slim private mirror model. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class HazardVisualProof extends Screen {
    private static final String[] VIEWS={"materials","dark","mirror"};private int page,frames;
    public HazardVisualProof(){super(Component.literal("The gray hazards / native assets"));}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    @Override protected void init(){var mc=Minecraft.getInstance();for(var kind:HazardBlock.Kind.values())for(int stage=0;stage<2;stage++){
        var state=HazardBlock.of(kind).setValue(HazardBlock.STAGE,stage);if(mc.getBlockRenderer().getBlockModel(state)==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing hazard mesh "+state);
        var texture=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/block/hazard_"+kind.getSerializedName()+(stage==1&&(kind==HazardBlock.Kind.HEAT_LAMP||kind==HazardBlock.Kind.SCONCE)?"_off":"")+".png");
        try(var stream=mc.getResourceManager().open(texture);var image=NativeImage.read(stream)){if(image.getWidth()!=16||image.getHeight()!=16)throw new IllegalStateException("Wrong hazard texture size "+texture);}catch(java.io.IOException e){throw new IllegalStateException("Missing hazard pixels "+texture,e);}}
    }
    @Override public void render(GuiGraphics g,int x,int y,float partial){var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xff252a29);g.drawString(font,"Gray hazards / "+VIEWS[page]+" / native baked models",10,9,0xffe8dfc0,false);g.flush();var buffers=mc.renderBuffers().bufferSource();var pose=g.pose();
        if(page<2){int i=0;for(var kind:HazardBlock.Kind.values()){int n=i++;float cw=(width-20)/5F,ch=(height-38)/4F;var state=HazardBlock.of(kind).setValue(HazardBlock.STAGE,page);pose.pushPose();pose.translate(10+(n%5+.5F)*cw,28+(n/5+.67F)*ch,160);float scale=Math.min(cw,ch)*.55F;pose.scale(scale,-scale,scale);pose.mulPose(Axis.XP.rotationDegrees(25));pose.mulPose(Axis.YP.rotationDegrees(30));pose.translate(-.5,0,-.5);mc.getBlockRenderer().renderSingleBlock(state,pose,buffers,page==0?LightTexture.FULL_BRIGHT:LightTexture.pack(9,0),OverlayTexture.NO_OVERLAY);pose.popPose();buffers.endBatch();g.drawCenteredString(font,kind.getSerializedName().replace('_',' '),(int)(10+(n%5+.5F)*cw),(int)(28+(n/5+.9F)*ch),0xffe8dfc0);}}
        else{for(int n=0;n<4;n++){boolean slim=n%2==1,crouch=n>=2;var skin=DefaultPlayerSkin.get(new UUID(480,n));var model=HazardClient.model(mc,slim);HazardClient.pose(model,10,crouch,.45F,frames);pose.pushPose();pose.translate((n+.5F)*width/4F,height*.8F,180);float scale=Math.min(width/6F,height*.34F);pose.scale(scale,scale,scale);pose.mulPose(Axis.YP.rotationDegrees(165));pose.translate(0,-1.5,0);model.renderToBuffer(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(skin.texture())),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xffd2d9dc);pose.popPose();buffers.endBatch();g.drawCenteredString(font,(slim?"slim":"standard")+(crouch?" / crouch":" / delay"),(int)((n+.5F)*width/4),height-25,0xffe8dfc0);}}
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{var mc=Minecraft.getInstance();if(!Boolean.getBoolean("the_oldest_house.fontSmoke")||!(mc.screen instanceof HazardVisualProof p)||++p.frames<12)return;var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve("native-hazards-"+VIEWS[p.page]+".png"));}if(++p.page<VIEWS.length){p.frames=0;return;}Files.writeString(dir.resolve("hazards-passed.txt"),"Forty native hazard block states, 22 environment textures, both lamp states, collision-matched narrow doors and native standard/slim mirror poses rendered.\n");mc.setScreen(new ElkVisualProof());}
}
