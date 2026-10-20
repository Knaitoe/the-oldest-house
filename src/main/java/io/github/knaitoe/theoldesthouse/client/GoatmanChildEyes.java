package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.knaitoe.theoldesthouse.labyrinth.GoatmanChild;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;

/** The existing amber eye texel, fitted to the two eye bands of the child face. No skin repaint. */
public final class GoatmanChildEyes {
    // Front-left glowing texel of the shipped 64-pixel goatman_eyes atlas.
    private static final float U=8.5F/64,V=10.5F/64;
    private GoatmanChildEyes(){}
    public static boolean visible(GoatmanChild child){return child.stalkingAppearance()&&!child.monsterAppearance()&&!child.girl();}
    public static void render(GoatmanChildModel model,PoseStack poses,MultiBufferSource buffers){
        poses.pushPose();model.head.translateAndRotate(poses);
        var out=buffers.getBuffer(RenderType.eyes(GoatmanFigureRenderer.EYES));
        // The painted 16-pixel face occupies the native eight-pixel head.
        // Eye bands are x=3..5 / 10..12, y=7..8 on that face.
        eye(poses.last(),out,-2.5F,-1F);eye(poses.last(),out,1F,2.5F);
        poses.popPose();
    }
    private static void eye(PoseStack.Pose pose,VertexConsumer out,float left,float right){
        vertex(pose,out,left,-3.5F);vertex(pose,out,right,-3.5F);
        vertex(pose,out,right,-4.5F);vertex(pose,out,left,-4.5F);
    }
    private static void vertex(PoseStack.Pose pose,VertexConsumer out,float x,float y){
        out.addVertex(pose,x/16,y/16,-4.01F/16).setColor(0xffffffff).setUv(U,V)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose,0,0,-1);
    }
}
