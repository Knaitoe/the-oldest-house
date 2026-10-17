package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LakeSharkEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * The film's shark (0.4.102), a little over five blocks long, nose to the north in model space. Its box layout matches
 * the skin tools/draw_movie_night_art.py paints (SHARK_BOXES). The tall dorsal fin is three stacked boxes so it tapers.
 */
public final class LakeSharkModel extends EntityModel<LakeSharkEntity> {
    public static final ModelLayerLocation LAYER=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"lake_shark"),"main");
    private final ModelPart body,jaw,tail,fluke,leftFin,rightFin;
    public LakeSharkModel(ModelPart root){
        body=root.getChild("body");jaw=body.getChild("head").getChild("jaw");tail=body.getChild("tail");fluke=tail.getChild("fluke");
        leftFin=body.getChild("left_fin");rightFin=body.getChild("right_fin");
    }
    public static LayerDefinition createBodyLayer(){
        var mesh=new MeshDefinition();var root=mesh.getRoot();
        var body=root.addOrReplaceChild("body",CubeListBuilder.create().texOffs(0,0).addBox(-8,-8,-16,16,16,32),PartPose.ZERO);
        var head=body.addOrReplaceChild("head",CubeListBuilder.create().texOffs(96,0).addBox(-7,-6,-16,14,12,16).texOffs(156,0).addBox(-5,-4,-24,10,6,8),PartPose.offset(0,-1,-16));
        head.addOrReplaceChild("jaw",CubeListBuilder.create().texOffs(192,0).addBox(-6,0,-14,12,4,14),PartPose.offset(0,5,-2));
        body.addOrReplaceChild("dorsal",CubeListBuilder.create().texOffs(100,48).addBox(-1,-8,-7,2,8,14).texOffs(132,48).addBox(-1,-14,-4,2,6,10).texOffs(156,48).addBox(-1,-19,-1,2,5,6),PartPose.offset(0,-8,-2));
        body.addOrReplaceChild("left_fin",CubeListBuilder.create().texOffs(172,48).addBox(0,0,-5,14,2,10),PartPose.offsetAndRotation(7,5,-8,0,0,.35F));
        body.addOrReplaceChild("right_fin",CubeListBuilder.create().texOffs(172,48).mirror().addBox(-14,0,-5,14,2,10),PartPose.offsetAndRotation(-7,5,-8,0,0,-.35F));
        var tail=body.addOrReplaceChild("tail",CubeListBuilder.create().texOffs(0,48).addBox(-5,-5,0,10,10,18),PartPose.offset(0,-1,16));
        tail.addOrReplaceChild("fluke",CubeListBuilder.create().texOffs(56,48).addBox(-1,-20,0,2,20,10).texOffs(80,48).addBox(-1,0,0,2,12,8),PartPose.offsetAndRotation(0,0,16,-.35F,0,0));
        return LayerDefinition.create(mesh,256,128);
    }
    @Override public void setupAnim(LakeSharkEntity e,float limbSwing,float limbAmount,float age,float headYaw,float headPitch){
        // The tail drives it; the flukes follow a beat behind. The jaw drops as it goes under.
        float beat=age*.28F;
        tail.yRot=Mth.sin(beat)*.28F;fluke.yRot=Mth.sin(beat-.7F)*.38F;body.yRot=-Mth.sin(beat)*.05F;
        jaw.xRot=(e==null?0:e.jaw())*.62F;
        leftFin.zRot=.35F+Mth.sin(beat*.5F)*.08F;rightFin.zRot=-leftFin.zRot;
    }
    @Override public void renderToBuffer(PoseStack pose,VertexConsumer out,int light,int overlay,int color){body.render(pose,out,light,overlay,color);}
}
