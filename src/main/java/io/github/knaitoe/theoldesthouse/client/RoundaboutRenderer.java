package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.labyrinth.RoundaboutBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/**
 * 0.4.469, Bike night: the merry-go-round's deck and rails, drawn turned about its hub as far round as the wheel is. A block is
 * drawn where its block is and as it was made; a deck that turns cannot be, so its ground is blocks and what is seen of it is
 * drawn here.
 */
public final class RoundaboutRenderer implements BlockEntityRenderer<RoundaboutBlock.Wheel> {
    public RoundaboutRenderer(BlockEntityRendererProvider.Context context){}
    @Override public void render(RoundaboutBlock.Wheel wheel,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        draw(pose,buffers,(float)wheel.angle(partial),light,overlay);
    }
    /** The wheel over a hub at the pose's origin, `angle` round: a point of it that was east of the hub is that far round toward the south. */
    public static void draw(PoseStack pose,MultiBufferSource buffers,float angle,int light,int overlay){
        pose.pushPose();
        pose.translate(.5,0,.5);pose.mulPose(Axis.YP.rotation(-angle));pose.translate(-.5,0,-.5);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(RoundaboutBlock.of(RoundaboutBlock.Cell.WHEEL),pose,buffers,light,overlay);
        pose.popPose();
    }
    /** The deck reaches a block and a half from its hub. */
    @Override public AABB getRenderBoundingBox(RoundaboutBlock.Wheel wheel){return new AABB(wheel.getBlockPos()).inflate(1.5,1,1.5);}
}
