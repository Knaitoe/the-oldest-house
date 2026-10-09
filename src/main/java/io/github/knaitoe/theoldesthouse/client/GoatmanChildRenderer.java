package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Pose;

public final class GoatmanChildRenderer extends MobRenderer<GoatmanChild,GoatmanChildModel> {
    public GoatmanChildRenderer(EntityRendererProvider.Context c){super(c,new GoatmanChildModel(c.bakeLayer(ModelLayers.PLAYER)),.25F);}
    @Override public boolean shouldRender(GoatmanChild e,Frustum f,double x,double y,double z){
        var player=Minecraft.getInstance().player;
        if(e.girl()&&(player==null||!e.viewer().get().equals(player.getUUID())))return false;
        return super.shouldRender(e,f,x,y,z);
    }
    @Override public void render(GoatmanChild e,float yaw,float partial,com.mojang.blaze3d.vertex.PoseStack poses,net.minecraft.client.renderer.MultiBufferSource buffers,int light){
        float old=e.yBodyRot,previous=e.yBodyRotO;
        if(e.tell(GoatmanVignette.FACE)&&Minecraft.getInstance().player!=null){
            var at=Minecraft.getInstance().player.position().subtract(e.position());e.yBodyRot=e.yBodyRotO=(float)(Math.atan2(at.z,at.x)*180/Math.PI)-90;
        }
        // A seated child is set on the seat's surface; the bent pose puts its hips there, not its feet.
        poses.pushPose();if(e.seated()&&!e.isSleeping()&&!e.hasPose(Pose.SLEEPING))poses.translate(0,-GoatmanChild.HIPS,0);
        super.render(e,yaw,partial,poses,buffers,light);poses.popPose();e.yBodyRot=old;e.yBodyRotO=previous;
    }
    @Override public ResourceLocation getTextureLocation(GoatmanChild e){return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/trailer_child_"+e.skin()+".png");}
    @Override protected boolean shouldShowName(GoatmanChild e){return false;}
}
