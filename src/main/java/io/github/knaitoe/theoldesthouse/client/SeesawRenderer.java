package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.knaitoe.theoldesthouse.labyrinth.SeesawBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import org.joml.Quaternionf;

/**
 * 0.4.468, Bike night: the seesaw's board, drawn turned about its stand's axle as far over as the board is. A block is drawn
 * where its block is, and a board that tips cannot be; so the stand is a block, and the board (its middle, and an end each way,
 * with their seats, handles and bumpers) is drawn here from the same models, turned.
 */
public final class SeesawRenderer implements BlockEntityRenderer<SeesawBlock.Board> {
    public SeesawRenderer(BlockEntityRendererProvider.Context context){}
    @Override public void render(SeesawBlock.Board board,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        if(!SeesawBlock.is(board.getBlockState(),SeesawBlock.Part.STAND))return;
        draw(pose,buffers,board.facing(),(float)board.tilt(partial),light,overlay);
    }
    /** The board over a stand at the pose's origin, `tilt` over: more than nothing, the end the stand faces is down. */
    public static void draw(PoseStack pose,MultiBufferSource buffers,Direction facing,float tilt,int light,int overlay){
        var blocks=Minecraft.getInstance().getBlockRenderer();
        pose.pushPose();
        pose.translate(.5,SeesawBlock.AXLE,.5);pose.mulPose(new Quaternionf().rotateAxis(tilt,facing.getStepZ(),0,-facing.getStepX()));pose.translate(-.5,-SeesawBlock.AXLE,-.5);
        blocks.renderSingleBlock(SeesawBlock.of(SeesawBlock.Part.BOARD,facing),pose,buffers,light,overlay);
        for(int end=0;end<2;end++){
            var out=end==0?facing:facing.getOpposite();
            pose.pushPose();pose.translate(out.getStepX()*SeesawBlock.OUT,0,out.getStepZ()*SeesawBlock.OUT);
            blocks.renderSingleBlock(SeesawBlock.of(SeesawBlock.Part.SEAT,out),pose,buffers,light,overlay);
            pose.popPose();
        }
        pose.popPose();
    }
    /** The board reaches well past its stand's block. */
    @Override public AABB getRenderBoundingBox(SeesawBlock.Board board){return new AABB(board.getBlockPos()).inflate(SeesawBlock.OUT+1,1,SeesawBlock.OUT+1);}
}
