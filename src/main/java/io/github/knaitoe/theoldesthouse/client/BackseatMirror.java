package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

/** Live native rear-cabin reflection, sharing the actual body's pose rather than a face decal. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class BackseatMirror {
    private static final int W=112,H=32;
    private static TextureTarget target;
    private static DynamicTexture image;
    private static ResourceLocation texture;
    private static BackseatStowawayModel body;
    private BackseatMirror(){}
    static int cabinLight(int light){return LightTexture.pack(Math.max(6,LightTexture.block(light)),LightTexture.sky(light));}
    static BackseatStowawayModel body(){if(body==null)body=new BackseatStowawayModel(Minecraft.getInstance().getEntityModels().bakeLayer(BackseatStowawayModel.LAYER));return body;}
    public static float rise(BackseatCarBlock.Car car,float partial){
        if(car.phase<BackseatCarBlock.Car.REVEAL)return 0;if(car.phase>BackseatCarBlock.Car.REVEAL)return 1;
        float age=car.getLevel()==null?0:car.getLevel().getGameTime()+partial;
        return Math.min(1,(car.clock+Math.max(0,age-car.syncedAt))/4F);
    }
    @SubscribeEvent public static void frame(RenderLevelStageEvent event){
        if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_LEVEL)return;
        var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null||!(mc.player.getVehicle() instanceof BackseatSeat seat)||seat.owner().filter(mc.player.getUUID()::equals).isEmpty())return;
        var car=seat.car();if(car==null)return;capture(rise(car,mc.getTimer().getGameTimeDeltaPartialTick(false)),cabinLight(LevelRenderer.getLightColor(mc.level,car.getBlockPos())));
    }
    static void capture(float rise,int light){
        var mc=Minecraft.getInstance();var buffers=mc.renderBuffers().bufferSource();buffers.endBatch();
        if(target==null){target=new TextureTarget(W,H,true,Minecraft.ON_OSX);target.setClearColor(.055F,.064F,.057F,1);image=new DynamicTexture(W,H,true);image.setFilter(false,false);texture=mc.getTextureManager().register("backseat_rearview",image);}
        var projection=new Matrix4f(RenderSystem.getProjectionMatrix());var sort=RenderSystem.getVertexSorting();var stack=RenderSystem.getModelViewStack();var color=RenderSystem.getShaderColor().clone();int[] viewport=new int[4];GL11.glGetIntegerv(GL11.GL_VIEWPORT,viewport);int framebuffer=GL11.glGetInteger(org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_BINDING);boolean depth=GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        stack.pushMatrix();stack.identity();RenderSystem.applyModelViewMatrix();
        try{
            target.clear(Minecraft.ON_OSX);target.bindWrite(true);RenderSystem.setProjectionMatrix(new Matrix4f().setPerspective((float)Math.toRadians(27),(float)W/H,.025F,12),VertexSorting.DISTANCE_TO_ORIGIN);RenderSystem.enableDepthTest();RenderSystem.depthMask(true);RenderSystem.setShaderColor(1,1,1,1);
            stack.set(new Matrix4f().lookAt(new Vector3f(0,2,-1.13F),new Vector3f(-.625F,1.87F,.3F),new Vector3f(0,1,0)));RenderSystem.applyModelViewMatrix();var p=new PoseStack();
            BackseatArt.draw(p,buffers,light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,0,12,false);
            body().draw(p,buffers,light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,rise);buffers.endBatch();
            try(var shot=Screenshot.takeScreenshot(target)){image.getPixels().copyFrom(shot);image.upload();}
        }finally{
            stack.popMatrix();RenderSystem.applyModelViewMatrix();RenderSystem.setProjectionMatrix(projection,sort);org.lwjgl.opengl.GL30.glBindFramebuffer(org.lwjgl.opengl.GL30.GL_FRAMEBUFFER,framebuffer);RenderSystem.viewport(viewport[0],viewport[1],viewport[2],viewport[3]);RenderSystem.setShaderColor(color[0],color[1],color[2],color[3]);if(!depth)RenderSystem.disableDepthTest();
        }
    }
    static NativeImage copy(){if(image==null)return null;var result=new NativeImage(W,H,false);result.copyFrom(image.getPixels());return result;}
    static void draw(PoseStack p,MultiBufferSource buffers,int light,int overlay){
        if(texture==null)return;var out=buffers.getBuffer(RenderType.entityCutoutNoCull(texture));
        float x=3.5F/16,y=31/16F,Y=33/16F,z=-18.38F/16;
        // A rearview reverses the lens horizontally. Its flat pixel surface remains native sized.
        for(int i=0;i<4;i++)out.addVertex(p.last(),i==0||i==3?-x:x,i<2?y:Y,z).setColor(-1).setUv(i==0||i==3?1:0,i<2?1:0).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(p.last(),0,0,1);
    }
    static void clear(){if(texture!=null)Minecraft.getInstance().getTextureManager().release(texture);if(target!=null)target.destroyBuffers();target=null;image=null;texture=null;body=null;}
}
