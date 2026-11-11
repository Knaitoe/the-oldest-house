package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.knaitoe.theoldesthouse.labyrinth.SpringBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import org.joml.Quaternionf;

/**
 * 0.4.471, Bike night: the spring rider's horse, drawn rocked about its foot as far over as it is. The foot is a block; the horse
 * on its coil is drawn here from a model that is never a block in the world.
 */
public final class SpringRenderer implements BlockEntityRenderer<SpringBlock.Rocker> {
    public SpringRenderer(BlockEntityRendererProvider.Context context){}
    @Override public void render(SpringBlock.Rocker rocker,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        if(!SpringBlock.is(rocker.getBlockState(),SpringBlock.Part.FOOT))return;
        draw(pose,buffers,rocker.facing(),(float)rocker.angle(partial),light,overlay);
    }
    /** The horse over a foot at the pose's origin, `angle` over: more than nothing, the way it looks. */
    public static void draw(PoseStack pose,MultiBufferSource buffers,Direction facing,float angle,int light,int overlay){
        pose.pushPose();
        pose.translate(.5,SpringBlock.PIVOT,.5);pose.mulPose(new Quaternionf().rotateAxis(angle,facing.getStepZ(),0,-facing.getStepX()));pose.translate(-.5,-SpringBlock.PIVOT,-.5);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(SpringBlock.of(SpringBlock.Part.HORSE,facing),pose,buffers,light,overlay);
        pose.popPose();
    }
    /** The horse leans out of its foot's block. */
    @Override public AABB getRenderBoundingBox(SpringBlock.Rocker rocker){return new AABB(rocker.getBlockPos()).inflate(1,1,1);}
}
