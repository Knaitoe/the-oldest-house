package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.MinotaurEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Original articulated geometry: a bull's head carried low against a man's chest, a hump and mane, hands, and a bull's hind
 * legs on cloven hooves.
 * 0.4.199 (owner: "Can we also improve the Minotaur's texture?"): every box has its own region of its own skin.
 * 0.4.584 (owner: "use multiple blocks for its body to give it more granular detail. its size can increase."): about a
 * hundred and eighty boxes in forty-seven parts, a fifth taller. tools/draw_minotaur.py is the one place they are written
 * down: it draws the skin (textures/entity/minotaur.png, one texel to a model pixel) and writes the parts between the two
 * marked lines below. What is written by hand here is which parts move, and how.
 */
public final class MinotaurModel extends HierarchicalModel<MinotaurEntity> {
    public static final ModelLayerLocation LAYER=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"minotaur"),"main");
    private static final int LEFT=0,RIGHT=1;
    /** How far the hips come down when it kneels, so that its knees and hooves rest on the floor. */
    private static final float KNEEL=15.5F;
    private final ModelPart root,body,neck,head,jaw,tail,tail2,tail3,ragFront,ragBack;
    private final ModelPart[] arm=new ModelPart[2],forearm=new ModelPart[2],fingers=new ModelPart[2],fingertips=new ModelPart[2],
            leg=new ModelPart[2],shin=new ModelPart[2],cannon=new ModelPart[2],ear=new ModelPart[2];
    private final java.util.List<ModelPart> parts;
    public MinotaurModel(ModelPart baked){
        root=baked.getChild("root");body=root.getChild("body");neck=body.getChild("neck");head=neck.getChild("head");jaw=head.getChild("jaw");
        tail=body.getChild("tail");tail2=tail.getChild("tail2");tail3=tail2.getChild("tail3");ragFront=body.getChild("rag_front");ragBack=body.getChild("rag_back");
        for(int side=0;side<2;side++){String s=side==LEFT?"left_":"right_";
            arm[side]=body.getChild(s+"arm");forearm[side]=arm[side].getChild(s+"forearm");
            fingers[side]=forearm[side].getChild(s+"hand").getChild(s+"fingers");fingertips[side]=fingers[side].getChild(s+"fingertips");
            leg[side]=root.getChild(s+"leg");shin[side]=leg[side].getChild(s+"thigh").getChild(s+"shin");cannon[side]=shin[side].getChild(s+"cannon");
            ear[side]=head.getChild(s+"ear");}
        parts=root.getAllParts().toList();}
    @Override public ModelPart root(){return root;}
    public static LayerDefinition createBodyLayer(){
        MeshDefinition mesh=new MeshDefinition();
        // <parts: written by tools/draw_minotaur.py; change them there>
        PartDefinition root=mesh.getRoot().addOrReplaceChild("root",CubeListBuilder.create(),PartPose.offset(0,24,0));
        PartDefinition body=root.addOrReplaceChild("body",CubeListBuilder.create().texOffs(96,26).addBox(-8,-4,-4,16,7,9).texOffs(198,96).addBox(-9,-4,-5,18,2,1).texOffs(160,96).addBox(-9,-4,5,18,2,1)
                .texOffs(230,57).addBox(-9,-4,-4,1,2,9).mirror().texOffs(230,57).addBox(8,-4,-4,1,2,9).mirror(false).texOffs(126,96).addBox(3,-4,-6,3,3,1)
                .texOffs(96,96).addBox(4,-1,-5.8F,1,4,1).texOffs(190,26).addBox(-7,-10,-4,14,6,9).texOffs(104,80).addBox(-6,-11,5,12,7,1)
                .texOffs(236,96).addBox(-5,-6.25F,-5,4,2,1).mirror().texOffs(236,96).addBox(1,-6.25F,-5,4,2,1).mirror(false).texOffs(236,96).addBox(-5,-8.5F,-5,4,2,1).mirror()
                .texOffs(236,96).addBox(1,-8.5F,-5,4,2,1).mirror(false).texOffs(236,96).addBox(-5,-10.75F,-5,4,2,1).mirror().texOffs(236,96).addBox(1,-10.75F,-5,4,2,1).mirror(false)
                .texOffs(38,69).addBox(-8,-9,-3,1,5,6).mirror().texOffs(38,69).addBox(7,-9,-3,1,5,6).mirror(false).texOffs(234,80).addBox(-1,-10,-4.6F,2,7,1)
                .texOffs(0,0).addBox(-11,-22,-7,22,12,14).texOffs(0,80).addBox(-10,-21,-9,9,7,2).texOffs(214,69).addBox(1,-21,-9,9,7,2)
                .texOffs(210,89).addBox(-12,-12.5F,-6,1,2,4).mirror().texOffs(210,89).addBox(11,-12.5F,-6,1,2,4).mirror(false).texOffs(210,89).addBox(-12,-14.75F,-6,1,2,4).mirror()
                .texOffs(210,89).addBox(11,-14.75F,-6,1,2,4).mirror(false).texOffs(236,26).addBox(-12,-20,-1,3,8,7).mirror().texOffs(236,26).addBox(9,-20,-1,3,8,7).mirror(false)
                .texOffs(0,57).addBox(-10,-21,7,20,11,1).texOffs(160,0).addBox(-12,-24,-6,24,4,13).texOffs(16,101).addBox(-10,-23,-7,8,1,1).mirror()
                .texOffs(16,101).addBox(2,-23,-7,8,1,1).mirror(false).texOffs(108,69).addBox(-9,-26,-3,5,3,7).mirror().texOffs(108,69).addBox(4,-26,-3,5,3,7).mirror(false)
                .texOffs(0,26).addBox(-9,-28,2,18,9,8).texOffs(0,43).addBox(-7,-31,0,14,6,8).texOffs(162,69).addBox(-4,-33,1,8,3,6)
                .texOffs(218,80).addBox(-8,-26,10,3,7,1).texOffs(80,80).addBox(-5,-25,10,3,8,1).texOffs(88,80).addBox(-1,-26,10,3,8,1)
                .texOffs(154,69).addBox(2,-25,10,3,9,1).texOffs(96,80).addBox(6,-27,10,3,8,1).texOffs(202,43).addBox(-10,-27,3,1,7,6).mirror()
                .texOffs(202,43).addBox(9,-27,3,1,7,6).mirror(false).texOffs(134,96).addBox(-2,-35,2,2,2,2).texOffs(246,96).addBox(0,-34,3,2,1,2)
                .texOffs(84,69).addBox(-12,-26,-2,4,2,8).mirror().texOffs(84,69).addBox(8,-26,-2,4,2,8),PartPose.offset(0,-25.902F,1));
        body.addOrReplaceChild("rag_front",CubeListBuilder.create().texOffs(88,57).addBox(-4,0,-0.5F,8,11,1),PartPose.offset(0,-2,-5.1F));
        body.addOrReplaceChild("rag_back",CubeListBuilder.create().texOffs(20,69).addBox(-4,0,-0.5F,8,10,1),PartPose.offset(0,-2,6.1F));
        PartDefinition neck=body.addOrReplaceChild("neck",CubeListBuilder.create().texOffs(146,26).addBox(-6,-5,-6,12,6,10).texOffs(56,80).addBox(-2,0,-9,4,7,2).texOffs(190,69).addBox(-5,-6,4,10,7,2)
                .texOffs(132,69).addBox(-7,-4,-2,1,5,5).mirror().texOffs(132,69).addBox(6,-4,-2,1,5,5),PartPose.offsetAndRotation(0,-19,-2,0.25F,0,0));
        PartDefinition head=neck.addOrReplaceChild("head",CubeListBuilder.create().texOffs(72,0).addBox(-7,-10,-9,14,13,11).texOffs(120,57).addBox(-5,-12,-8,10,3,8).texOffs(150,96).addBox(-3,-9,-10,2,3,1)
                .texOffs(64,96).addBox(-1,-9,-10.4F,3,4,1).texOffs(0,101).addBox(2,-9,-10,2,2,1).texOffs(100,96).addBox(-8,-8,-10,6,2,2).mirror()
                .texOffs(100,96).addBox(2,-8,-10,6,2,2).mirror(false).texOffs(116,96).addBox(-7,-6,-9.4F,4,3,1).mirror().texOffs(116,96).addBox(3,-6,-9.4F,4,3,1).mirror(false)
                .texOffs(216,43).addBox(-8,-4,-8,1,7,6).mirror().texOffs(216,43).addBox(7,-4,-8,1,7,6).mirror(false).texOffs(130,80).addBox(-3,-6,-14,6,3,5)
                .texOffs(168,43).addBox(-5,-3,-16,10,6,7).texOffs(238,89).addBox(-4,-3,-17,8,4,1).texOffs(88,89).addBox(-4,3,-15.5F,8,1,5),PartPose.offset(0,-4,-1));
        head.addOrReplaceChild("right_ear",CubeListBuilder.create().texOffs(0,96).addBox(-6,-2,-1,6,3,2).texOffs(142,96).addBox(-8,-2,-1,2,2,2),PartPose.offsetAndRotation(-7,-5,0,0,0.25F,-0.3F));
        PartDefinition rightHorn=head.addOrReplaceChild("right_horn",CubeListBuilder.create().texOffs(136,89).addBox(-2.5F,-0.5F,-2.5F,5,1,5).texOffs(174,80).addBox(-2,-4,-2,4,4,4),PartPose.offsetAndRotation(-6,-9,-2,0.5F,0,-1.2F));
        PartDefinition rightHorn1=rightHorn.addOrReplaceChild("right_horn1",CubeListBuilder.create().texOffs(190,80).addBox(-2,-4,-2,4,4,4),PartPose.offsetAndRotation(0,-4,0,0.12F,0,0.24F));
        PartDefinition rightHorn2=rightHorn1.addOrReplaceChild("right_horn2",CubeListBuilder.create().texOffs(56,89).addBox(-1.5F,-4,-1.5F,3,4,3),PartPose.offsetAndRotation(0,-4,0,0.12F,0,0.24F));
        PartDefinition rightHorn3=rightHorn2.addOrReplaceChild("right_horn3",CubeListBuilder.create().texOffs(68,89).addBox(-1.5F,-4,-1.5F,3,4,3),PartPose.offsetAndRotation(0,-4,0,0.12F,0,0.24F));
        PartDefinition rightHorn4=rightHorn3.addOrReplaceChild("right_horn4",CubeListBuilder.create().texOffs(80,96).addBox(-1,-3,-1,2,3,2),PartPose.offsetAndRotation(0,-4,0,0.12F,0,0.24F));
        PartDefinition rightHorn5=rightHorn4.addOrReplaceChild("right_horn5",CubeListBuilder.create().texOffs(88,96).addBox(-1,-3,-1,2,3,2),PartPose.offsetAndRotation(0,-3,0,0.12F,0,0.24F));
        rightHorn5.addOrReplaceChild("right_horn6",CubeListBuilder.create().texOffs(156,96).addBox(-0.5F,-3,-0.5F,1,3,1),PartPose.offsetAndRotation(0,-3,0,0.12F,0,0.24F));
        head.addOrReplaceChild("jaw",CubeListBuilder.create().texOffs(52,26).addBox(-4,0,-11,8,3,14).texOffs(114,89).addBox(-3,3,-10,6,1,5).texOffs(34,101).addBox(-3,-1,-10.5F,6,1,1)
                .texOffs(42,89).addBox(-4,-1,-9,1,1,6).mirror().texOffs(42,89).addBox(3,-1,-9,1,1,6).mirror(false).texOffs(152,80).addBox(-2,-1,-9,4,1,7)
                .texOffs(172,89).addBox(-2,4,-9,4,3,3).texOffs(6,101).addBox(-1,7,-8,2,2,1),PartPose.offsetAndRotation(0,3,-5,0.05F,0,0));
        head.addOrReplaceChild("left_ear",CubeListBuilder.create().mirror().texOffs(0,96).addBox(0,-2,-1,6,3,2).texOffs(142,96).addBox(6,-2,-1,2,2,2),PartPose.offsetAndRotation(7,-5,0,0,-0.25F,0.3F));
        PartDefinition leftHorn=head.addOrReplaceChild("left_horn",CubeListBuilder.create().mirror().texOffs(136,89).addBox(-2.5F,-0.5F,-2.5F,5,1,5).texOffs(174,80).addBox(-2,-4,-2,4,4,4),PartPose.offsetAndRotation(6,-9,-2,0.5F,0,1.2F));
        PartDefinition leftHorn1=leftHorn.addOrReplaceChild("left_horn1",CubeListBuilder.create().mirror().texOffs(190,80).addBox(-2,-4,-2,4,4,4),PartPose.offsetAndRotation(0,-4,0,0.12F,0,-0.24F));
        PartDefinition leftHorn2=leftHorn1.addOrReplaceChild("left_horn2",CubeListBuilder.create().mirror().texOffs(56,89).addBox(-1.5F,-4,-1.5F,3,4,3),PartPose.offsetAndRotation(0,-4,0,0.12F,0,-0.24F));
        PartDefinition leftHorn3=leftHorn2.addOrReplaceChild("left_horn3",CubeListBuilder.create().mirror().texOffs(68,89).addBox(-1.5F,-4,-1.5F,3,4,3),PartPose.offsetAndRotation(0,-4,0,0.12F,0,-0.24F));
        PartDefinition leftHorn4=leftHorn3.addOrReplaceChild("left_horn4",CubeListBuilder.create().mirror().texOffs(80,96).addBox(-1,-3,-1,2,3,2),PartPose.offsetAndRotation(0,-4,0,0.12F,0,-0.24F));
        PartDefinition leftHorn5=leftHorn4.addOrReplaceChild("left_horn5",CubeListBuilder.create().mirror().texOffs(88,96).addBox(-1,-3,-1,2,3,2),PartPose.offsetAndRotation(0,-3,0,0.12F,0,-0.24F));
        leftHorn5.addOrReplaceChild("left_horn6",CubeListBuilder.create().mirror().texOffs(156,96).addBox(-0.5F,-3,-0.5F,1,3,1),PartPose.offsetAndRotation(0,-3,0,0.12F,0,-0.24F));
        PartDefinition rightArm=body.addOrReplaceChild("right_arm",CubeListBuilder.create().texOffs(122,0).addBox(-5,-4,-5,9,9,10).texOffs(52,69).addBox(-5,-6,-3,8,2,8).texOffs(106,57).addBox(-6,-4,-2,1,6,6)
                .texOffs(42,57).addBox(-4,5,-5,7,7,5).texOffs(208,57).addBox(-4,5,0,7,7,4).texOffs(156,89).addBox(-3,11,2,5,3,3),PartPose.offsetAndRotation(-13,-20,0,0,0,0.12F));
        PartDefinition rightForearm=rightArm.addOrReplaceChild("right_forearm",CubeListBuilder.create().texOffs(44,43).addBox(-4,0,-4,8,6,8).texOffs(184,57).addBox(-3,6,-3,6,5,6).texOffs(144,69).addBox(-5,1,-2,1,6,4)
                .texOffs(46,96).addBox(-2,1,4,4,4,1),PartPose.offsetAndRotation(-0.5F,12,0,-0.3F,0,0));
        PartDefinition rightHand=rightForearm.addOrReplaceChild("right_hand",CubeListBuilder.create().texOffs(156,57).addBox(-4,0,-3,8,5,6).texOffs(220,89).addBox(4,1,-3,2,3,3).texOffs(72,96).addBox(4.5F,4,-3,2,3,2)
                .texOffs(12,101).addBox(5,7,-2.5F,1,2,1),PartPose.offsetAndRotation(0,11,0,0.1F,0,0));
        PartDefinition rightFingers=rightHand.addOrReplaceChild("right_fingers",CubeListBuilder.create().texOffs(200,89).addBox(-4,0,-2,2,3,3).texOffs(200,89).addBox(-2,0,-2,2,3,3).texOffs(200,89).addBox(0,0,-2,2,3,3)
                .texOffs(200,89).addBox(2,0,-2,2,3,3),PartPose.offsetAndRotation(0,5,0,0.25F,0,0));
        rightFingers.addOrReplaceChild("right_fingertips",CubeListBuilder.create().texOffs(56,96).addBox(-4,0,-1,2,3,2).texOffs(12,101).addBox(-3.5F,3,-0.5F,1,2,1).texOffs(56,96).addBox(-2,0,-1,2,3,2)
                .texOffs(12,101).addBox(-1.5F,3,-0.5F,1,2,1).texOffs(56,96).addBox(0,0,-1,2,3,2).texOffs(12,101).addBox(0.5F,3,-0.5F,1,2,1)
                .texOffs(56,96).addBox(2,0,-1,2,3,2).texOffs(12,101).addBox(2.5F,3,-0.5F,1,2,1),PartPose.offsetAndRotation(0,3,-0.5F,0.35F,0,0));
        PartDefinition tail=body.addOrReplaceChild("tail",CubeListBuilder.create().texOffs(68,80).addBox(-1.5F,0,-1.5F,3,6,3),PartPose.offsetAndRotation(0,-1,5,0.6F,0,0));
        PartDefinition tail2=tail.addOrReplaceChild("tail2",CubeListBuilder.create().texOffs(226,80).addBox(-1,0,-1,2,6,2),PartPose.offsetAndRotation(0,6,0,-0.25F,0,0));
        PartDefinition tail3=tail2.addOrReplaceChild("tail3",CubeListBuilder.create().texOffs(80,89).addBox(-1,0,-1,2,5,2),PartPose.offsetAndRotation(0,6,0,-0.2F,0,0));
        tail3.addOrReplaceChild("tail_tuft",CubeListBuilder.create().texOffs(40,80).addBox(-2,-1,-2,4,5,4).texOffs(230,89).addBox(-1,4,-1,2,4,2),PartPose.offsetAndRotation(0,5,0,-0.1F,0,0));
        PartDefinition leftArm=body.addOrReplaceChild("left_arm",CubeListBuilder.create().mirror().texOffs(122,0).addBox(-4,-4,-5,9,9,10).texOffs(52,69).addBox(-3,-6,-3,8,2,8).texOffs(106,57).addBox(5,-4,-2,1,6,6)
                .texOffs(42,57).addBox(-3,5,-5,7,7,5).texOffs(208,57).addBox(-3,5,0,7,7,4).texOffs(156,89).addBox(-2,11,2,5,3,3),PartPose.offsetAndRotation(13,-20,0,0,0,-0.12F));
        PartDefinition leftForearm=leftArm.addOrReplaceChild("left_forearm",CubeListBuilder.create().mirror().texOffs(44,43).addBox(-4,0,-4,8,6,8).texOffs(184,57).addBox(-3,6,-3,6,5,6).texOffs(144,69).addBox(4,1,-2,1,6,4)
                .texOffs(46,96).addBox(-2,1,4,4,4,1),PartPose.offsetAndRotation(0.5F,12,0,-0.3F,0,0));
        PartDefinition leftHand=leftForearm.addOrReplaceChild("left_hand",CubeListBuilder.create().mirror().texOffs(156,57).addBox(-4,0,-3,8,5,6).texOffs(220,89).addBox(-6,1,-3,2,3,3).texOffs(72,96).addBox(-6.5F,4,-3,2,3,2)
                .texOffs(12,101).addBox(-6,7,-2.5F,1,2,1),PartPose.offsetAndRotation(0,11,0,0.1F,0,0));
        PartDefinition leftFingers=leftHand.addOrReplaceChild("left_fingers",CubeListBuilder.create().mirror().texOffs(200,89).addBox(2,0,-2,2,3,3).texOffs(200,89).addBox(0,0,-2,2,3,3).texOffs(200,89).addBox(-2,0,-2,2,3,3)
                .texOffs(200,89).addBox(-4,0,-2,2,3,3),PartPose.offsetAndRotation(0,5,0,0.25F,0,0));
        leftFingers.addOrReplaceChild("left_fingertips",CubeListBuilder.create().mirror().texOffs(56,96).addBox(2,0,-1,2,3,2).texOffs(12,101).addBox(2.5F,3,-0.5F,1,2,1).texOffs(56,96).addBox(0,0,-1,2,3,2)
                .texOffs(12,101).addBox(0.5F,3,-0.5F,1,2,1).texOffs(56,96).addBox(-2,0,-1,2,3,2).texOffs(12,101).addBox(-1.5F,3,-0.5F,1,2,1)
                .texOffs(56,96).addBox(-4,0,-1,2,3,2).texOffs(12,101).addBox(-3.5F,3,-0.5F,1,2,1),PartPose.offsetAndRotation(0,3,-0.5F,0.35F,0,0));
        PartDefinition rightLeg=root.addOrReplaceChild("right_leg",CubeListBuilder.create().texOffs(132,43).addBox(-5,-2,-4,9,4,9),PartPose.offset(-6,-25.902F,1));
        PartDefinition rightThigh=rightLeg.addOrReplaceChild("right_thigh",CubeListBuilder.create().texOffs(76,43).addBox(-5,1,-5,9,8,6).texOffs(66,57).addBox(-4,1,1,7,8,4).texOffs(0,89).addBox(-4,8,-6,7,3,4),PartPose.offsetAndRotation(0,0,0,-0.35F,0,0));
        PartDefinition rightShin=rightThigh.addOrReplaceChild("right_shin",CubeListBuilder.create().texOffs(106,43).addBox(-4,0,-3,7,8,6).texOffs(186,89).addBox(-3,1,3,5,4,2).texOffs(16,96).addBox(-3,7,1,5,2,3),PartPose.offsetAndRotation(0,9,-1,0.9F,0,0));
        PartDefinition rightCannon=rightShin.addOrReplaceChild("right_cannon",CubeListBuilder.create().texOffs(0,69).addBox(-3,0,-2.5F,5,6,5).texOffs(32,96).addBox(-3,4,2.5F,5,3,2).texOffs(206,80).addBox(-4,4,-2.5F,1,3,5)
                .texOffs(206,80).addBox(2,4,-2.5F,1,3,5),PartPose.offsetAndRotation(0,8,0,-0.65F,0,0));
        rightCannon.addOrReplaceChild("right_hoof",CubeListBuilder.create().texOffs(22,89).addBox(-2.5F,0,-3,5,2,5).texOffs(22,80).addBox(-3.25F,2,-5,3,3,6).texOffs(22,80).addBox(0.25F,2,-5,3,3,6)
                .texOffs(48,101).addBox(-2.5F,2,2,1,1,1).texOffs(48,101).addBox(1.5F,2,2,1,1,1),PartPose.offsetAndRotation(-0.5F,6,0,0.1F,0,0));
        PartDefinition leftLeg=root.addOrReplaceChild("left_leg",CubeListBuilder.create().mirror().texOffs(132,43).addBox(-4,-2,-4,9,4,9),PartPose.offset(6,-25.902F,1));
        PartDefinition leftThigh=leftLeg.addOrReplaceChild("left_thigh",CubeListBuilder.create().mirror().texOffs(76,43).addBox(-4,1,-5,9,8,6).texOffs(66,57).addBox(-3,1,1,7,8,4).texOffs(0,89).addBox(-3,8,-6,7,3,4),PartPose.offsetAndRotation(0,0,0,-0.35F,0,0));
        PartDefinition leftShin=leftThigh.addOrReplaceChild("left_shin",CubeListBuilder.create().mirror().texOffs(106,43).addBox(-3,0,-3,7,8,6).texOffs(186,89).addBox(-2,1,3,5,4,2).texOffs(16,96).addBox(-2,7,1,5,2,3),PartPose.offsetAndRotation(0,9,-1,0.9F,0,0));
        PartDefinition leftCannon=leftShin.addOrReplaceChild("left_cannon",CubeListBuilder.create().mirror().texOffs(0,69).addBox(-2,0,-2.5F,5,6,5).texOffs(32,96).addBox(-2,4,2.5F,5,3,2).texOffs(206,80).addBox(3,4,-2.5F,1,3,5)
                .texOffs(206,80).addBox(-3,4,-2.5F,1,3,5),PartPose.offsetAndRotation(0,8,0,-0.65F,0,0));
        leftCannon.addOrReplaceChild("left_hoof",CubeListBuilder.create().mirror().texOffs(22,89).addBox(-2.5F,0,-3,5,2,5).texOffs(22,80).addBox(0.25F,2,-5,3,3,6).texOffs(22,80).addBox(-3.25F,2,-5,3,3,6)
                .texOffs(48,101).addBox(1.5F,2,2,1,1,1).texOffs(48,101).addBox(-2.5F,2,2,1,1,1),PartPose.offsetAndRotation(0.5F,6,0,0.1F,0,0));
        // </parts>
        return LayerDefinition.create(mesh,256,128);
    }
    @Override public void setupAnim(MinotaurEntity entity,float walk,float amount,float age,float yaw,float pitch){
        pose(entity.motion(),walk,amount,age,yaw,pitch);
        // A blow lands: the head snaps back and the arms come up.
        if(entity.hurtTime>0&&entity.motion()!=MinotaurEntity.WOUNDED){float recoil=entity.hurtTime/10F;head.xRot-=recoil*.45F;jaw.xRot+=recoil*.25F;arm[LEFT].xRot-=recoil*.5F;arm[RIGHT].xRot-=recoil*.5F;}
    }
    /** A leg swung from the hip: coming forward the hock folds and the hoof lifts, trailing it straightens. */
    private void stride(int side,float swing){
        float fold=Math.max(0,-swing),trail=Math.max(0,swing);leg[side].xRot=swing;
        shin[side].xRot=shin[side].getInitialPose().xRot+fold*.7F-trail*.2F;cannon[side].xRot=cannon[side].getInitialPose().xRot-fold*.45F+trail*.25F;
    }
    /** Both hands closed. */
    private void fists(){
        for(int side=0;side<2;side++){fingers[side].xRot=fingers[side].getInitialPose().xRot+1F;fingertips[side].xRot=fingertips[side].getInitialPose().xRot+1.1F;}
    }
    public void pose(int state,float walk,float amount,float age,float yaw,float pitch){
        parts.forEach(ModelPart::resetPose);float breath=Mth.sin(age*.08F)*.025F;
        body.xRot=.12F+breath;head.yRot=yaw*Mth.DEG_TO_RAD*.45F;head.xRot=pitch*Mth.DEG_TO_RAD*.4F;jaw.xRot=.05F+breath;
        stride(LEFT,Mth.cos(walk*.65F)*amount*.6F);stride(RIGHT,Mth.cos(walk*.65F+Mth.PI)*amount*.6F);
        // The tail hangs and swings, and an ear flicks now and then.
        tail.zRot=Mth.sin(age*.07F)*.16F;tail2.zRot=Mth.sin(age*.07F-.9F)*.2F;tail3.zRot=Mth.sin(age*.07F-1.8F)*.24F;
        float flick=Mth.sin(age*.11F)-.94F,other=Mth.sin(age*.11F+2.3F)-.94F;
        if(flick>0)ear[LEFT].yRot-=flick*9F;if(other>0)ear[RIGHT].yRot+=other*9F;
        // It lowers its horns and paws the floor with one hoof before it comes.
        if(state==MinotaurEntity.WINDUP){body.xRot=.36F;neck.xRot+=.2F;head.xRot=-.3F;arm[LEFT].xRot=arm[RIGHT].xRot=-.25F;fists();jaw.xRot=.3F;
            stride(RIGHT,-.3F+Mth.sin(age*.9F)*.45F);stride(LEFT,.08F);tail.zRot=Mth.sin(age*.9F)*.5F;}
        if(state==MinotaurEntity.CHARGING){body.xRot=.55F;neck.xRot+=.3F;head.xRot=-.45F;arm[LEFT].xRot=arm[RIGHT].xRot=-.7F;forearm[LEFT].xRot-=.5F;forearm[RIGHT].xRot-=.5F;fists();
            float run=Mth.sin(age*1.3F)*.8F;stride(LEFT,run);stride(RIGHT,-run);tail.xRot+=.9F;}
        if(state==MinotaurEntity.STUNNED){head.xRot=.55F;head.zRot=Mth.sin(age*.7F)*.09F;body.xRot=.15F;jaw.xRot=.35F;arm[LEFT].xRot=arm[RIGHT].xRot=.1F;}
        // Wounded, it is down on its knees and its knuckles, its head still up.
        if(state==MinotaurEntity.WOUNDED){root.y+=KNEEL;body.xRot=.6F+breath*2;neck.xRot-=.2F;head.xRot=-.35F;jaw.xRot=.18F;tail.xRot+=.2F;
            for(int side=0;side<2;side++){
                arm[side].xRot=-.6F+Mth.sin(age*.12F)*(side==LEFT?.04F:-.04F);forearm[side].xRot-=.79F;
                leg[side].xRot=-.7F;shin[side].xRot=shin[side].getInitialPose().xRot+1.55F;cannon[side].xRot=cannon[side].getInitialPose().xRot+.75F;}}
        // Out of its cell after a kill (0.4.84): heavy, slow, shoulders rolling, head low.
        if(state==MinotaurEntity.LOOSE){body.xRot=.22F+breath;body.zRot=Mth.sin(walk*.33F)*amount*.08F;head.xRot=.3F;jaw.xRot=.1F;arm[LEFT].xRot=Mth.cos(walk*.65F+Mth.PI)*amount*.35F;arm[RIGHT].xRot=Mth.cos(walk*.65F)*amount*.35F;}
        if(state==MinotaurEntity.RELEASED){body.xRot=.07F+breath;head.xRot=.18F;arm[LEFT].xRot=Mth.cos(walk*.65F+Mth.PI)*amount*.15F;arm[RIGHT].xRot=Mth.cos(walk*.65F)*amount*.15F;jaw.xRot=.02F;}
        // The rags hang plumb however far the body bends; a thigh coming forward pushes the front one out, one trailing the back one.
        float forward=Math.max(0,-Math.min(leg[LEFT].xRot,leg[RIGHT].xRot)),back=Math.max(0,Math.max(leg[LEFT].xRot,leg[RIGHT].xRot));
        ragFront.xRot=-body.xRot*.85F-forward*.45F;ragBack.xRot=-body.xRot*.85F+back*.3F;
    }
}
