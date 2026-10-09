package io.github.knaitoe.theoldesthouse.client;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.BlockPos;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Vector3f;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
/** Actual baked client models and shipped skin UVs, after the existing NPC/font checks. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class CastVisualProof extends Screen {
    private static int frames;
    public CastVisualProof(){super(Component.literal("The remaining cast"));}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float delta){}
    @Override public void render(GuiGraphics g,int x,int y,float delta){
        var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xFF211E1B);
        g.drawString(font,"Native cast meshes | human memory / transformed hunt",10,10,0xFFE6DFCF,false);
        String[] titles={"Lake witch","Her memory","Mother of Strays","Trailer cousin"};String[] textures={"lake_witch","lake_witch_memory","mother_of_strays","trailer_child_2"};
        for(int i=0;i<4;i++){
            int left=10+i*width/4;g.fill(left,32,left+width/4-18,height-24,0xFF39332D);g.drawString(font,titles[i],left+6,40,0xFFE6DFCF,false);
            g.flush();var pose=g.pose();pose.pushPose();pose.translate(left+width/8-8,i==2?96:75,100);pose.scale(78,78,78);pose.mulPose(Axis.YP.rotationDegrees(i==1?150:170));
            var buffers=mc.renderBuffers().bufferSource();var consumer=buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/"+textures[i]+".png")));
            if(i==2)new MotherModel(mc.getEntityModels().bakeLayer(MotherModel.LAYER)).renderToBuffer(pose,consumer,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xFFFFFFFF);
            else if(i==3)new GoatmanChildModel(mc.getEntityModels().bakeLayer(ModelLayers.PLAYER)).renderToBuffer(pose,consumer,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xFFFFFFFF);
            else {var model=new LakeWitchModel(mc.getEntityModels().bakeLayer(LakeWitchModel.LAYER));if(i==0){grounded(model);model.huntPose(0,.5F,0,LakeWitchEntity.STALK,false);if(model.body.xRot<1.5F||model.head.y<15)throw new IllegalStateException("Hunt model is not on all fours");}model.renderToBuffer(pose,consumer,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xFFFFFFFF);}
            buffers.endBatch();pose.popPose();
        }
        g.drawString(font,"Native UVs / original appearance IDs / ordinary clothing, changed by wear and the lake.",10,height-14,0xFFE6DFCF,false);
    }
    /** Measure the actual baked cubes, including elbow and knee children, in model space. */
    private static float lowest(ModelPart limb){
        float[] low={-Float.MAX_VALUE};
        limb.visit(new PoseStack(),(pose,path,index,cube)->{
            for(float x:new float[]{cube.minX,cube.maxX})for(float y:new float[]{cube.minY,cube.maxY})for(float z:new float[]{cube.minZ,cube.maxZ})
                low[0]=Math.max(low[0],pose.pose().transformPosition(new Vector3f(x/16F,y/16F,z/16F)).y);
        });
        return low[0];
    }
    /** Her hands and feet meet the ground: through a whole stride of each hunt phase nothing sinks more than two pixels, and at rest nothing hangs. */
    private static void grounded(LakeWitchModel model){
        float ground=1.5F;
        for(int phase:new int[]{LakeWitchEntity.STALK,LakeWitchEntity.WITHDRAW,LakeWitchEntity.LUNGE}){
            boolean lunge=phase==LakeWitchEntity.LUNGE;
            for(int k=0;k<=16;k++){
                model.huntPose(k/(16F*.23F),1,0,phase,lunge);
                for(var limb:new ModelPart[]{model.leftArm,model.rightArm,model.leftLeg,model.rightLeg}){
                    float low=lowest(limb);
                    if(low>ground+2/16F)throw new IllegalStateException("A hunting limb sinks through the ground: phase "+phase+" step "+k+" "+low);
                }
            }
            model.huntPose(0,0,0,phase,lunge);
            for(var limb:new ModelPart[]{model.leftArm,model.rightArm,model.leftLeg,model.rightLeg}){
                boolean legPart=limb==model.leftLeg||limb==model.rightLeg;if(lunge&&!legPart)continue; // the claws are raised
                float low=lowest(limb);
                if(Math.abs(low-ground)>1/16F)throw new IllegalStateException("A resting limb is off the ground: phase "+phase+" "+low);
            }
        }
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!Boolean.getBoolean("the_oldest_house.fontSmoke")||!(mc.screen instanceof CastVisualProof)||++frames<4)return;
        var effects=new HouseOutsideEffects();if(!Float.isNaN(effects.getCloudHeight())||effects.hasGround())throw new IllegalStateException("Outdoor pockets still have sea-level sky or clouds");
        var origin=new BlockPos(0,70,0);var lake=LabyrinthPlaces.base(origin,LabyrinthPlace.DROWNED_TOWN).north(8);
        for(long time:new long[]{0,6000,12000,23999,72000})if(SceneClock.time(SceneClock.at(origin,lake,HouseDimensions.OUTSIDE),0,time)!=18000)throw new IllegalStateException("A native time update can flash the lake sky");
        Path folder=Path.of("../build/font-smoke");Files.createDirectories(folder);
        try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(folder.resolve("native-cast.png"));}
        Files.writeString(folder.resolve("cast-passed.txt"),"Native remaining-cast models, memory/hunting skins and outdoor effects passed.\n");
        TheOldestHouse.LOGGER.info("HOUSE CAST CHECK PASSED: native screenshot saved");mc.setScreen(new StaceyScrabbleVisualProof());
    }
}
