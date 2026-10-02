package io.github.knaitoe.theoldesthouse.client;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
/** Native baked child, animated tree joints, furniture and styled theoretical writing. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class HomeDesignProof extends Screen {
    private int frames;
    public HomeDesignProof(){super(Component.literal("Home and threshold"));}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float delta){}
    @Override public void render(GuiGraphics g,int x,int y,float delta){
        var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xFF25221E);g.drawString(font,"Native home / prisoner / tree / writing",12,10,0xFFE8DFC9,false);
        String[] names={"Prisoner","Tendril, first pose","Tendril, later pose","Remembered chair"};
        for(int i=0;i<4;i++){
            int left=8+i*width/4;g.fill(left,30,left+width/4-12,height-115,0xFF3B3530);g.drawString(font,names[i],left+5,36,0xFFE8DFC9,false);g.flush();
            var pose=g.pose();var buffers=mc.renderBuffers().bufferSource();pose.pushPose();pose.translate(left+width/8-5,i==0?61:132,150);pose.scale(68,68,68);pose.mulPose(Axis.YP.rotationDegrees(i==0?25:35));
            if(i==0){pose.scale(.7F,.7F,.7F);new PlayerModel<>(mc.getEntityModels().bakeLayer(ModelLayers.PLAYER),false).renderToBuffer(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/lake_boy.png"))),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xFFFFFFFF);}
            else if(i<3){pose.translate(-.5,-.5,-.5);var part=mc.getEntityModels().bakeLayer(TreeTendrilRenderer.LAYER);var tree=new TreeTendrilRenderer(part);tree.renderAt(i==1?0:60,7,Direction.SOUTH,pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);if(Math.abs(part.getChild("tip").xRot)<.001)throw new IllegalStateException("Tree joint did not animate");}
            else {pose.scale(1,-1,1);pose.translate(-.5,0,-.5);mc.getBlockRenderer().renderSingleBlock(HouseBlocks.HOUSEHOLD_FURNITURE.get().defaultBlockState(),pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);}
            pose.popPose();buffers.endBatch();
        }
        int y0=height-97;for(var line:font.split(ThresholdWriting.pages(0).getFirst(),width-28)){g.drawString(font,line,14,y0,0xFFE8DFC9,false);y0+=10;}
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!Boolean.getBoolean("the_oldest_house.fontSmoke")||!(mc.screen instanceof HomeDesignProof s)||++s.frames<4)return;
        var branch=new TreeTendrilBlockEntity(BlockPos.ZERO,HouseBlocks.TREE_TENDRIL.get().defaultBlockState());if(mc.getBlockEntityRenderDispatcher().getRenderer(branch)==null)throw new IllegalStateException("The native tendril renderer is unregistered");
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve("native-home-design.png"));}
        Files.writeString(dir.resolve("home-design-passed.txt"),"Native child mesh, tree renderer registration, two joint poses, household model and blue HOME typography rendered.\n");mc.setScreen(new SceneVisualProof());
    }
}
