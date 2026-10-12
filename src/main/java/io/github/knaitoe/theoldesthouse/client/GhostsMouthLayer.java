package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/** Native head-attached sauce, blood and saliva meshes, with stretching strands and falling drops. */
public final class GhostsMouthLayer extends RenderLayer<LiteraryActor,GhostsCastModel> {
    private static final ResourceLocation SURFACE=ResourceLocation.withDefaultNamespace("textures/block/white_concrete.png");
    private final ModelPart root;
    public GhostsMouthLayer(GhostsCastModel.Renderer parent){super(parent);root=mesh();}
    private static ModelPart mesh(){
        var m=new MeshDefinition();var r=m.getRoot();
        r.addOrReplaceChild("smear",CubeListBuilder.create().texOffs(0,0)
            .addBox(-1.65F,-1.8F,-4.045F,1.3F,.65F,.08F)
            .addBox(.45F,-1.65F,-4.05F,1.05F,.55F,.08F)
            .addBox(-.9F,-.8F,-4.04F,1.1F,.55F,.08F)
            .addBox(1.5F,-1.3F,-4.02F,.35F,1.1F,.09F),PartPose.ZERO);
        r.addOrReplaceChild("mouth",CubeListBuilder.create().texOffs(0,0).addBox(-1.5F,-1.7F,-4.07F,3,.45F,.1F),PartPose.ZERO);
        r.addOrReplaceChild("strand",CubeListBuilder.create().texOffs(0,0).addBox(-.07F,0,-.06F,.14F,3,.12F),PartPose.ZERO);
        r.addOrReplaceChild("drop",CubeListBuilder.create().texOffs(0,0).addBox(-.17F,0,-.15F,.34F,.48F,.3F),PartPose.ZERO);
        return LayerDefinition.create(m,16,16).bakeRoot();
    }
    public static boolean active(int role,int phase){return (role==LiteraryActor.GHOST_MARJORIE&&(phase==GhostsSet.EATING||phase==GhostsSet.VOMIT))||((role==LiteraryActor.GHOST_MARJORIE||role==LiteraryActor.GHOST_JOHN||role==LiteraryActor.GHOST_SARAH)&&(phase==GhostsSet.CHOKING||phase==GhostsSet.DEAD));}
    @Override public void render(PoseStack p,MultiBufferSource out,int light,LiteraryActor a,float walk,float amount,float partial,float age,float yaw,float pitch){render(getParentModel(),root,a.role(),a.phase(),age,p,out,light);}
    /** The proof uses the same geometry and animation as the actual entity render layer. */
    public static void preview(GhostsCastModel model,int role,int phase,float age,PoseStack p,MultiBufferSource out,int light){render(model,mesh(),role,phase,age,p,out,light);}
    private static void render(GhostsCastModel model,ModelPart root,int role,int phase,float age,PoseStack p,MultiBufferSource out,int light){
        if(!active(role,phase))return;boolean dying=phase==GhostsSet.CHOKING||phase==GhostsSet.DEAD;
        var buffer=out.getBuffer(RenderType.entityTranslucent(SURFACE));p.pushPose();model.head.translateAndRotate(p);
        if(role==LiteraryActor.GHOST_MARJORIE)root.getChild("smear").render(p,buffer,light,OverlayTexture.NO_OVERLAY,0xf0963c22);
        if(dying){root.getChild("mouth").render(p,buffer,light,OverlayTexture.NO_OVERLAY,0xff3c1114);root.getChild("smear").render(p,buffer,light,OverlayTexture.NO_OVERLAY,0xd0701f24);}
        p.translate(0,-1.35/16,-4.13/16);
        // Keep gravity vertical even when the collapsed head hangs forward.
        p.mulPose(Axis.XP.rotation(-model.head.xRot));p.mulPose(Axis.YP.rotation(-model.head.yRot));p.mulPose(Axis.ZP.rotation(-model.head.zRot));
        for(int i=0;i<(dying?3:2);i++){
            float drip=(age*.035F+i*.37F)%1;int colour=dying&&i!=1?0xd89a2930:0xb4e0d8c4;
            p.pushPose();p.translate((i-1)*.05,0,i*.007);var strand=root.getChild("strand");strand.yScale=.3F+drip*.95F;strand.render(p,buffer,light,OverlayTexture.NO_OVERLAY,colour);strand.yScale=1;
            p.translate(Math.sin(age*.035F+i)*.007,drip*drip*(dying?.56:.28),0);root.getChild("drop").render(p,buffer,light,OverlayTexture.NO_OVERLAY,colour);p.popPose();
        }
        p.popPose();
    }
}
