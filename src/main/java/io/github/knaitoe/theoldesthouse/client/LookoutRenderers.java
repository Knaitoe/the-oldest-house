package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryActor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * 0.4.202, the Whistlers in the Pines: the thing in the woods, two and a half blocks of long limbs on a narrow body with a
 * small head and no eyes to speak of; and the figures in the towers round tower four, each in the reader's own House body,
 * crouching when the reader crouches, the north one with its hands up to its face.
 */
public final class LookoutRenderers {
    private LookoutRenderers(){}
    public static final ModelLayerLocation WHISTLER=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("the_oldest_house","lookout_whistler"),"main");
    static final ResourceLocation SKIN=ResourceLocation.fromNamespaceAndPath("the_oldest_house","textures/entity/lookout_whistler.png");
    public static boolean role(int role){return role==LiteraryActor.WHISTLER||role==LiteraryActor.LOOKOUT_DOUBLE;}

    /** Its own net on a 64 by 64 sheet (tools/draw_lookout_art.py): head, a narrow body, arms twenty long, legs eighteen. */
    public static LayerDefinition layer(){
        var mesh=new MeshDefinition();var root=mesh.getRoot();
        root.addOrReplaceChild("head",CubeListBuilder.create().texOffs(0,0).addBox(-4,-8,-4,8,8,8),PartPose.offset(0,-8,0));
        root.addOrReplaceChild("body",CubeListBuilder.create().texOffs(32,0).addBox(-3,0,-1.5F,6,14,3),PartPose.offset(0,-8,0));
        root.addOrReplaceChild("right_arm",CubeListBuilder.create().texOffs(0,16).addBox(-1.5F,-1,-1.5F,3,20,3),PartPose.offset(-4.5F,-6.5F,0));
        root.addOrReplaceChild("left_arm",CubeListBuilder.create().texOffs(12,16).addBox(-1.5F,-1,-1.5F,3,20,3),PartPose.offset(4.5F,-6.5F,0));
        root.addOrReplaceChild("right_leg",CubeListBuilder.create().texOffs(24,17).addBox(-1.5F,0,-1.5F,3,18,3),PartPose.offset(-1.6F,6,0));
        root.addOrReplaceChild("left_leg",CubeListBuilder.create().texOffs(36,17).addBox(-1.5F,0,-1.5F,3,18,3),PartPose.offset(1.6F,6,0));
        return LayerDefinition.create(mesh,64,64);
    }
    public static final class WhistlerModel extends EntityModel<LiteraryActor> {
        private final ModelPart root,head,body,rightArm,leftArm,rightLeg,leftLeg;
        public WhistlerModel(ModelPart root){this.root=root;head=root.getChild("head");body=root.getChild("body");rightArm=root.getChild("right_arm");leftArm=root.getChild("left_arm");rightLeg=root.getChild("right_leg");leftLeg=root.getChild("left_leg");}
        /** Still, but never quite: the head leans one way and the other, the long arms drift, the weight moves from foot to foot. At the glass (tier 3) both hands are flat on it. */
        @Override public void setupAnim(LiteraryActor a,float walk,float amount,float age,float yaw,float pitch){
            float shift=Mth.sin(age*.021F),slow=Mth.sin(age*.013F+1.3F);
            head.yRot=yaw*Mth.DEG_TO_RAD*.6F;head.xRot=pitch*Mth.DEG_TO_RAD*.5F+.12F;head.zRot=.32F*shift;
            body.zRot=-.05F*shift;body.xRot=.08F;
            rightArm.xRot=-.08F+.06F*slow;leftArm.xRot=-.04F-.06F*slow;rightArm.zRot=.08F+.03F*shift;leftArm.zRot=-.08F+.03F*shift;rightArm.yRot=0;leftArm.yRot=0;
            rightLeg.zRot=.03F*shift;leftLeg.zRot=.03F*shift;rightLeg.xRot=0;leftLeg.xRot=0;
            if(a.phase()>=3){rightArm.xRot=-2.05F;leftArm.xRot=-2.1F;rightArm.yRot=-.18F;leftArm.yRot=.18F;rightArm.zRot=0;leftArm.zRot=0;head.xRot=.05F;head.zRot=.45F*shift;}
        }
        @Override public void renderToBuffer(PoseStack p,VertexConsumer v,int light,int overlay,int colour){root.render(p,v,light,overlay,colour);}
    }
    public static final class Whistler extends MobRenderer<LiteraryActor,WhistlerModel> {
        public Whistler(EntityRendererProvider.Context c){super(c,new WhistlerModel(c.bakeLayer(WHISTLER)),0F);}
        @Override public ResourceLocation getTextureLocation(LiteraryActor a){return SKIN;}
        @Override protected boolean shouldShowName(LiteraryActor a){return false;}
    }

    /** A figure in the reader's own body: crouching (phase 1) when they crouch; binoculars up (gesture 1). */
    public static final class DoubleModel extends PlayerModel<LiteraryActor> {
        public DoubleModel(ModelPart part,boolean slim){super(part,slim);}
        @Override public void setupAnim(LiteraryActor a,float walk,float amount,float age,float yaw,float pitch){
            // 0.4.213: walking beside a reader (gesture 2), it walks as they do; in the tower it stands still.
            boolean walks=a.gesture()==2;crouching=a.phase()==1;super.setupAnim(a,walks?walk:0,walks?amount:0,age,yaw,pitch);
            if(a.gesture()==1){rightArm.xRot=-1.72F;leftArm.xRot=-1.72F;rightArm.yRot=-.42F;leftArm.yRot=.42F;rightArm.zRot=0;leftArm.zRot=0;head.xRot=0;
                rightSleeve.copyFrom(rightArm);leftSleeve.copyFrom(leftArm);hat.copyFrom(head);}
        }
    }
    public static final class Double extends MobRenderer<LiteraryActor,DoubleModel> {
        private final DoubleModel wide,slim;
        public Double(EntityRendererProvider.Context c){super(c,new DoubleModel(c.bakeLayer(ModelLayers.PLAYER),false),.3F);wide=model;slim=new DoubleModel(c.bakeLayer(ModelLayers.PLAYER_SLIM),true);addLayer(new net.minecraft.client.renderer.entity.layers.ItemInHandLayer<>(this,c.getItemInHandRenderer()));}
        @Override public void render(LiteraryActor a,float yaw,float partial,PoseStack p,MultiBufferSource out,int light){
            var skin=UncannyActorRenderer.houseSkin();model=skin!=null&&skin.model()==net.minecraft.client.resources.PlayerSkin.Model.SLIM?slim:wide;
            p.pushPose();if(a.phase()==1)p.translate(0,-.125,0);super.render(a,yaw,partial,p,out,light);p.popPose();
        }
        @Override public ResourceLocation getTextureLocation(LiteraryActor a){var skin=UncannyActorRenderer.houseSkin();return skin!=null?skin.texture():net.minecraft.client.resources.DefaultPlayerSkin.getDefaultTexture();}
        @Override protected boolean shouldShowName(LiteraryActor a){return false;}
    }
    /** Only the reader whose tower it is sees their own figures. */
    public static boolean owned(LiteraryActor a){var p=Minecraft.getInstance().player;return a.owner().isEmpty()||p!=null&&a.owner().get().equals(p.getUUID());}
}
