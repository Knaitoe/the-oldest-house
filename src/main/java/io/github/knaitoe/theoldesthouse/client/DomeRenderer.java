package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.knaitoe.theoldesthouse.labyrinth.DomeBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * 0.4.471, Bike night: the climbing dome's bars and joints. A block is drawn square to the world; a dome's bars lie at every
 * angle, so its ground is blocks that draw nothing and what is seen of it is drawn here, each bar laid from one joint to the
 * next, from models that are never blocks in the world.
 */
public final class DomeRenderer implements BlockEntityRenderer<DomeBlock.Bars> {
    private static final DomeBlock.Part[] PAINTS={DomeBlock.Part.RED,DomeBlock.Part.YELLOW,DomeBlock.Part.BLUE};
    public DomeRenderer(BlockEntityRendererProvider.Context context){}
    @Override public void render(DomeBlock.Bars bars,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){draw(pose,buffers,light,overlay);}
    /** The dome over a middle cell at the pose's origin. */
    public static void draw(PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        var blocks=Minecraft.getInstance().getBlockRenderer();
        pose.pushPose();
        pose.translate(.5,0,.5);
        for(var bar:DomeBlock.BARS){
            var from=DomeBlock.JOINTS.get(bar[0]);var to=DomeBlock.JOINTS.get(bar[1]);
            var along=new Vector3f((float)(to.x-from.x),(float)(to.y-from.y),(float)(to.z-from.z));float length=along.length();
            pose.pushPose();
            pose.translate(from.x,from.y,from.z);
            // The bar's model lies along east, a block long: turned to lie along the bar, and made as long as the bar is.
            pose.mulPose(new Quaternionf().rotationTo(new Vector3f(1,0,0),along.div(length)));
            pose.scale(length,1,1);
            blocks.renderSingleBlock(DomeBlock.of(PAINTS[bar[2]]),pose,buffers,light,overlay);
            pose.popPose();
        }
        for(var joint:DomeBlock.JOINTS){
            pose.pushPose();
            pose.translate(joint.x,joint.y,joint.z);
            pose.mulPose(new Quaternionf().rotateY((float)-Math.atan2(joint.z,joint.x)));
            blocks.renderSingleBlock(DomeBlock.of(DomeBlock.Part.JOINT),pose,buffers,light,overlay);
            pose.popPose();
        }
        pose.popPose();
    }
    /** The dome reaches a block and a half from its middle, and is over a block and a half high. */
    @Override public AABB getRenderBoundingBox(DomeBlock.Bars bars){return new AABB(bars.getBlockPos()).inflate(1.5,1,1.5);}
    @Override public int getViewDistance(){return 96;}
}
