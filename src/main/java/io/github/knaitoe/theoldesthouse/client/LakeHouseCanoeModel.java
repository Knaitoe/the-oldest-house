package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LakeHouseCanoe;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * A canoe, not a boat: long and narrow, a low hull that tapers to both ends, upswept stems, two seats and a yoke, and
 * single-bladed paddles. It keeps the vanilla boat's frame of reference (long axis X, bow at +X, model Y down, the hull's
 * bottom 6 pixels below the origin) so the vanilla boat's floating, seats and paddle timing all line up.
 * <p>Texture 128 by 64 ({@code textures/entity/lake_house_canoe*.png}, drawn by tools/draw_lake_house_art.py). The sides'
 * outer faces carry the green paint: the minimum-Z face on the -Z side, the maximum-Z face on the +Z side.
 */
public final class LakeHouseCanoeModel extends HierarchicalModel<LakeHouseCanoe> {
    public static final ModelLayerLocation LAYER=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"lake_house_canoe"),"main");
    /** The angle of the tapering end panels: from the side wall at x ±14, z ±5.5 to the stem at x ±19.9. */
    private static final float TAPER=(float)Math.atan2(5.5,6);
    private final ModelPart canoe,leftPaddle,rightPaddle,waterPatch;

    public LakeHouseCanoeModel(ModelPart baked){
        canoe=baked.getChild("canoe");leftPaddle=canoe.getChild("left_paddle");rightPaddle=canoe.getChild("right_paddle");waterPatch=baked.getChild("water_patch");
    }
    @Override public ModelPart root(){return canoe;}
    /** The water mask: hides the lake's surface inside the hull. */
    public ModelPart waterPatch(){return waterPatch;}

    public static LayerDefinition createBodyLayer(){
        MeshDefinition mesh=new MeshDefinition();PartDefinition root=mesh.getRoot();
        PartDefinition c=root.addOrReplaceChild("canoe",CubeListBuilder.create(),PartPose.ZERO);
        // The hull: a flat bottom between two long sides, a brown gunwale along the top of each.
        c.addOrReplaceChild("bottom",CubeListBuilder.create().texOffs(0,0).addBox(-14,5,-5,28,1,10)
                .texOffs(78,0).addBox(14,5,-3.5F,2,1,7).texOffs(96,0).addBox(16,5,-1.5F,2,1,3)
                .texOffs(78,0).addBox(-16,5,-3.5F,2,1,7).texOffs(96,0).addBox(-18,5,-1.5F,2,1,3),PartPose.ZERO);
        c.addOrReplaceChild("side_neg",CubeListBuilder.create().texOffs(0,11).addBox(-14,-2,-6,28,8,1)
                .texOffs(0,29).addBox(-14,-3,-6.5F,28,1,2),PartPose.ZERO);
        c.addOrReplaceChild("side_pos",CubeListBuilder.create().texOffs(0,20).addBox(-14,-2,5,28,8,1)
                .texOffs(0,29).addBox(-14,-3,4.5F,28,1,2),PartPose.ZERO);
        // Four panels close the hull into a point at each end, each with its own strip of gunwale.
        end(c,"bow_neg",14,-5.5F,-TAPER,60,11);
        end(c,"bow_pos",14,5.5F,TAPER,60,20);
        end(c,"stern_neg",-19.9F,-.1F,TAPER,60,11);
        end(c,"stern_pos",-19.9F,.1F,-TAPER,60,20);
        // The stems sweep up above the gunwales at both ends.
        c.addOrReplaceChild("stems",CubeListBuilder.create().texOffs(80,11).addBox(19,-5,-.5F,2,11,1)
                .texOffs(80,11).addBox(-21,-5,-.5F,2,11,1),PartPose.ZERO);
        // Two seats low in the hull and a yoke across the middle.
        c.addOrReplaceChild("seats",CubeListBuilder.create().texOffs(88,11).addBox(8,1,-5,3,1,10)
                .texOffs(88,11).addBox(-11,1,-5,3,1,10).texOffs(88,22).addBox(-.5F,-2,-5,1,1,10),PartPose.ZERO);
        // Single-bladed paddles, pivoting just outside the gunwales where the vanilla boat's oars would.
        c.addOrReplaceChild("left_paddle",paddle(),PartPose.offsetAndRotation(2,-4,7,0,0,(float)(Math.PI/16)));
        c.addOrReplaceChild("right_paddle",paddle(),PartPose.offsetAndRotation(2,-4,-7,0,(float)Math.PI,(float)(Math.PI/16)));
        root.addOrReplaceChild("water_patch",CubeListBuilder.create().texOffs(0,0).addBox(-14,-2,-5,28,3,10)
                .addBox(14,-2,-3.5F,2,3,7).addBox(16,-2,-1.5F,2,3,3).addBox(-16,-2,-3.5F,2,3,7).addBox(-18,-2,-1.5F,2,3,3),PartPose.ZERO);
        return LayerDefinition.create(mesh,128,64);
    }
    private static void end(PartDefinition c,String name,float x,float z,float yaw,int u,int v){
        c.addOrReplaceChild(name,CubeListBuilder.create().texOffs(u,v).addBox(0,-2,-.5F,8,8,1).texOffs(62,29).addBox(0,-3,-1,8,1,2),
                PartPose.offsetAndRotation(x,0,z,0,yaw,0));
    }
    private static CubeListBuilder paddle(){
        return CubeListBuilder.create().texOffs(0,34).addBox(-.5F,0,-4,1,1,14).texOffs(32,34).addBox(-1.5F,0,-5,3,1,1)
                .texOffs(40,34).addBox(-.5F,-2,10,1,5,6);
    }

    @Override public void setupAnim(LakeHouseCanoe canoe,float partialTicks,float unused,float age,float yaw,float pitch){
        leftPaddle.visible=rightPaddle.visible=!canoe.underLowRoof();
        paddle(canoe,0,leftPaddle,partialTicks);paddle(canoe,1,rightPaddle,partialTicks);
    }
    /** The vanilla boat's stroke, so the paddles keep time with its paddling sounds and speed. */
    private static void paddle(LakeHouseCanoe canoe,int side,ModelPart paddle,float partialTicks){
        float t=canoe.getRowingTime(side,partialTicks);
        paddle.xRot=Mth.clampedLerp((float)(-Math.PI/3),(float)(-Math.PI/12),(Mth.sin(-t)+1)/2);
        paddle.yRot=Mth.clampedLerp((float)(-Math.PI/4),(float)(Math.PI/4),(Mth.sin(-t+1)+1)/2);
        if(side==1)paddle.yRot=(float)Math.PI-paddle.yRot;
    }
}
