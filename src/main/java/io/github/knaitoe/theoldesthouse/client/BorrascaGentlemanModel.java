package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

/**
 * 0.4.434, the owner: "Ensure the shiny gentleman still appears as an actual monster out by the far marker too". Until now he was one flat
 * picture turned to the reader; now he has a body, built from boxes in native pixels (16 to a block) and painted by
 * tools/draw_shiny_gentleman.py: a tall hat bent over at the crown and grown into his head, a faceless polished oval with one bright band
 * across it, a neck like a stalk, shoulders too wide over a narrow coat that falls to a ragged hem, legs a pixel wide, and arms that hang
 * apart from the coat, bend out at the elbow and end past his knees in four long hooked fingers. He stands, sways a little, lays his head
 * over to one side, and his fingers curl and open. He is still the reader's alone, drawn only on their screen where the server says he
 * stands.
 */
public final class BorrascaGentlemanModel {
    private BorrascaGentlemanModel(){}
    public static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/borrasca_gentleman.png");
    /** From the bent tip of his hat to the soles of his feet, in pixels, at a scale of one. */
    public static final float HEIGHT=73;
    /** How far below the root's origin his feet are, in pixels. */
    private static final float FEET=72;
    private static final int[] FINGERS={5,8,7,4};
    private static ModelPart root;

    static ModelPart root(){if(root==null)root=build();return root;}

    /** The boxes, in model space (pixels, y down, his front to -z); their places on the atlas match tools/draw_shiny_gentleman.py's PARTS. */
    static ModelPart build(){
        var mesh=new MeshDefinition();var r=mesh.getRoot();
        var body=r.addOrReplaceChild("body",CubeListBuilder.create()
                .texOffs(32,6).addBox(-5.5F,22,-1.5F,11,2,3)    // shoulders, too wide
                .texOffs(0,20).addBox(-4.5F,24,-1.5F,9,7,3)     // chest
                .texOffs(24,21).addBox(-3.5F,31,-1.5F,7,8,3)    // waist, narrow
                .texOffs(0,0).addBox(-5.5F,39,-2.5F,11,15,5)    // the coat's skirt, to a ragged hem
                .texOffs(12,30).addBox(-.5F,18,-.5F,1,4,1)      // neck, a stalk
                .texOffs(16,30).addBox(-2.5F,54,-.5F,1,17,1)    // legs, a pixel wide
                .texOffs(16,30).addBox(1.5F,54,-.5F,1,17,1)
                .texOffs(0,34).addBox(-3.5F,71,-1.5F,2,1,2)     // narrow feet
                .texOffs(0,34).addBox(1.5F,71,-1.5F,2,1,2),PartPose.ZERO);
        var head=body.addOrReplaceChild("head",CubeListBuilder.create()
                .texOffs(44,21).addBox(-2.5F,-9,-2,5,8,4)       // the long smooth oval
                .texOffs(0,30).addBox(-1.5F,-1,-1.5F,3,1,3)     // its chin
                .texOffs(32,0).addBox(-4.5F,-10,-2.5F,9,1,5)    // a narrow brim
                .texOffs(32,11).addBox(-2.5F,-16,-2,5,6,4),     // the crown, too tall
                PartPose.offset(0,18,0));
        head.addOrReplaceChild("tip",CubeListBuilder.create().texOffs(50,11).addBox(-1.5F,-3,-1.5F,3,3,3),PartPose.offsetAndRotation(.5F,-16,0,0,0,-.7F));
        for(int side:new int[]{-1,1}){
            String s=side<0?"left":"right";
            var upper=body.addOrReplaceChild(s+"_arm",CubeListBuilder.create().texOffs(20,33).addBox(-.5F,0,-.5F,1,11,1),PartPose.offset(side*5F,23,0));
            var fore=upper.addOrReplaceChild(s+"_forearm",CubeListBuilder.create().texOffs(24,33).addBox(-.5F,0,-.5F,1,14,1),PartPose.offset(0,11,0));
            for(int i=0;i<FINGERS.length;i++)fore.addOrReplaceChild(s+"_finger"+i,CubeListBuilder.create().texOffs(28,33).addBox(-.5F,0,-.5F,1,FINGERS[i],1),PartPose.offsetAndRotation(side*(i-1.5F)*1.25F,14,0,0,0,side*(i-1.5F)*.12F));
        }
        return LayerDefinition.create(mesh,64,64).bakeRoot();
    }

    /** How he holds himself at a moment: a slow sway, his head laid over, the arms hanging out from the coat, the fingers working. */
    static void pose(ModelPart r,float time){
        var body=r.getChild("body");var head=body.getChild("head");
        body.zRot=.04F*Mth.sin(time*.031F);
        head.zRot=.16F+.05F*Mth.sin(time*.023F);head.xRot=-.08F;
        for(int side:new int[]{-1,1}){
            String s=side<0?"left":"right";
            var upper=body.getChild(s+"_arm");var fore=upper.getChild(s+"_forearm");
            // Hanging away from the coat, and bent further out at the elbow.
            upper.zRot=-side*(.1F+.03F*Mth.sin(time*.05F+side));
            fore.zRot=-side*.22F;
            for(int i=0;i<FINGERS.length;i++){var finger=fore.getChild(s+"_finger"+i);finger.xRot=-.3F-.45F*Math.max(0,Mth.sin(time*.11F+i*.9F+side));finger.zRot=side*(i-1.5F)*.12F;}
        }
    }

    /**
     * Him, standing at the pose's origin and facing +z, scale times his own size: faintly lit, so he shows at dusk, and shining a little as
     * he stands. The caller ends the batch.
     */
    public static void render(PoseStack pose,MultiBufferSource buffers,float alpha,float time,float scale){
        var r=root();pose(r,time);
        pose.pushPose();
        pose.mulPose(Axis.YP.rotation(Mth.PI));
        pose.translate(0,FEET/16F*scale,0);pose.scale(-scale,-scale,scale);
        int c=(int)(255*(.84F+.1F*Mth.sin(time*.27F)));int a=(int)(255*Mth.clamp(alpha,0,1));
        r.render(pose,buffers.getBuffer(type()),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,FastColor.ARGB32.color(a,c,c,c));
        pose.popPose();
    }
    public static RenderType type(){return RenderType.entityTranslucentEmissive(TEXTURE);}
}
