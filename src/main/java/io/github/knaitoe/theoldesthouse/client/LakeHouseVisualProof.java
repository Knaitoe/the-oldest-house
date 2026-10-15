package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/** Actual native baked fixtures, Tess's painted atlas and long hair, swimming/rowing poses and original artifacts. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class LakeHouseVisualProof extends Screen {
    private int page,frames;private LiteraryRenderers.CastModel cast;
    public LakeHouseVisualProof(){super(Component.literal("The lake house — native assets"));}
    @Override protected void init(){LakeHairDynamics.verify();cast=new LiteraryRenderers.CastModel(Minecraft.getInstance().getEntityModels().bakeLayer(LiteraryRenderers.CAST));}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xff253035);g.drawString(font,page==0?"Tess / front, back, rowing and swimming — pale skin, navy two-piece":page==1?"Underwater rooms / fitted interactive native furniture":"Lent diving gear / earned artifacts / empty-lake aftermath",10,10,0xffe6ded0,false);g.flush();var buffers=mc.renderBuffers().bufferSource();
        if(page==0){
            for(int i=0;i<8;i++){
                int role=i<6?LiteraryActor.LAKE_TESS:LiteraryActor.LAKE_WAX;int phase=i==2?1:i>=3&&i<6?2:0;
                for(var part:new net.minecraft.client.model.geom.ModelPart[]{cast.head,cast.body,cast.leftArm,cast.rightArm,cast.leftLeg,cast.rightLeg})part.getAllParts().forEach(net.minecraft.client.model.geom.ModelPart::resetPose);
                cast.young=false;cast.pose(role,phase,frames);if(role==LiteraryActor.LAKE_TESS)cast.previewLakeHair(i==3||i==4,frames+i*21);var pose=g.pose();pose.pushPose();pose.translate(30+(i+.5F)*(width-60)/8,height*.67F,220);pose.scale(43,43,43);pose.mulPose(Axis.YP.rotationDegrees(i==1||i==7?180:20));if(phase==2){pose.mulPose(Axis.XP.rotationDegrees(-90));pose.translate(0,-1,.3);}pose.translate(0,-1.501,0);
                cast.renderToBuffer(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(LiteraryRenderers.skin(role,phase))),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xffffffff);buffers.endBatch();pose.popPose();
            }
        }else if(page==1){
            int i=0;for(var kind:LakeHouseFittingBlock.Kind.values())for(boolean lit:new boolean[]{false,true}){
                var state=LakeHouseFittingBlock.of(kind,Direction.NORTH).setValue(LakeHouseFittingBlock.LIT,lit);if(mc.getBlockRenderer().getBlockModel(state)==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing lake fitting "+kind+" "+lit);
                var pose=g.pose();pose.pushPose();pose.translate(20+(i%9+.5F)*(width-40)/9,40+(i/9+.6F)*(height-60)/5,210);pose.scale(27,-27,27);pose.mulPose(Axis.XP.rotationDegrees(20));pose.mulPose(Axis.YP.rotationDegrees(30));mc.getBlockRenderer().renderSingleBlock(state,pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);buffers.endBatch();pose.popPose();i++;
            }
        }else{
            var items=new net.minecraft.world.item.Item[]{LakeHouseRegistry.FLASHLIGHT.get(),LakeHouseRegistry.HELMET.get(),LakeHouseRegistry.TANKS.get(),LakeHouseRegistry.FOUND_FLASHLIGHT.get(),LakeHouseRegistry.HOUSE_LIGHT.get(),LakeHouseRegistry.GOLD_HELMET.get()};
            for(int i=0;i<items.length;i++){var pose=g.pose();pose.pushPose();pose.translate(25+(i+.3F)*(width-50)/6,height*.45F,220);pose.scale(3,3,3);g.renderItem(new ItemStack(items[i]),0,0);pose.popPose();}
            var pose=g.pose();pose.pushPose();pose.translate(width*.4F,height*.75F,210);pose.scale(40,-40,40);pose.mulPose(Axis.XP.rotationDegrees(25));pose.mulPose(Axis.YP.rotationDegrees(30));mc.getBlockRenderer().renderSingleBlock(LakeHouseRegistry.COMPRESSOR.get().defaultBlockState(),pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);buffers.endBatch();pose.popPose();
        }
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();boolean direct=Boolean.getBoolean("the_oldest_house.lakeHairSmoke");if(direct&&mc.screen instanceof net.minecraft.client.gui.screens.TitleScreen){mc.setScreen(new LakeHouseVisualProof());return;}if(!(direct||Boolean.getBoolean("the_oldest_house.fontSmoke"))||!(mc.screen instanceof LakeHouseVisualProof s)||++s.frames<20)return;
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve("native-lake-house-"+(s.page==0?"cast":s.page==1?"fixtures":"gear")+".png"));}
        if(++s.page<3){s.frames=0;return;}Files.writeString(dir.resolve("lake-house-passed.txt"),"Native painted Tess front/back/rowing/swimming, lake hair drift versus rigid house hair, nine-joint motion bounds and immediate indoor stop, featureless dressed wax figure, 44 fixture models, lent gear and three bound artifact models rendered.\n");if(direct)mc.stop();else mc.setScreen(new BorrascaVisualProof());
    }
}
