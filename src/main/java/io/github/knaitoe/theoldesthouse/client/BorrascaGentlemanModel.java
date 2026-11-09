package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Matrix3f;

/**
 * 0.4.434, the owner: "Ensure the shiny gentleman still appears as an actual monster out by the far marker too". He has a body, built from
 * boxes in native pixels (16 to a block) and painted by tools/draw_shiny_gentleman.py.
 * <p>
 * 0.4.439, the owner: "Well the orrery needs to be rebuilt too. Maybe redesign them both". In the story he is the machine at the plant, the
 * silver cone whose whirling lit up the valley (client.BorrascaOrrery), and now he is made of its parts. He keeps the tall hat bent over at
 * the crown and grown into his head, and the faceless polished oval with one bright band across it, on a neck like a stalk. Below that: a
 * bar of shoulders too wide with a ball at each end; a spindle for a body, three collars on it and two tilted hoops turning round it, a bead
 * riding each; from the waist a stepped cone like the machine's, banded, to a toothed hem; legs a pixel wide with a bead at each knee; arms
 * of rods with a ball at the elbow and the wrist, hanging apart from him and ending past his knees in four long hooked fingers. He stands,
 * sways a little, lays his head over to one side, his fingers curl and open and his hoops turn. He is still the reader's alone, drawn only
 * on their screen where the server says he stands.
 */
public final class BorrascaGentlemanModel {
    private BorrascaGentlemanModel(){}
    public static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/borrasca_gentleman.png");
    /** The atlas, in pixels (tools/draw_shiny_gentleman.py's W and H). */
    static final int ATLAS_W=128,ATLAS_H=64;
    /** From the bent tip of his hat to the soles of his feet, in pixels, at a scale of one. */
    public static final float HEIGHT=73;
    /** How far below the root's origin his feet are, in pixels. */
    private static final float FEET=72;
    private static final int[] FINGERS={5,8,7,4};
    /** A hoop's eight rods, four pixels each, stand this far from its middle. */
    private static final float HOOP=2/(float)Math.tan(Math.PI/8);
    private static ModelPart root;

    static ModelPart root(){if(root==null)root=build();return root;}

    /** The boxes, in model space (pixels, y down, his front to -z); their places on the atlas match tools/draw_shiny_gentleman.py's PARTS. */
    static ModelPart build(){
        var mesh=new MeshDefinition();var r=mesh.getRoot();
        var body=r.addOrReplaceChild("body",CubeListBuilder.create()
                .texOffs(52,26).addBox(-6.5F,22,-.5F,13,1,1)    // shoulders too wide: one bar
                .texOffs(116,12).addBox(-7.5F,21,-1.5F,3,3,3)   // a ball at each end
                .texOffs(116,12).addBox(4.5F,21,-1.5F,3,3,3)
                .texOffs(108,12).addBox(-1,24,-1,2,15,2)        // a spindle for a body
                .texOffs(88,22).addBox(-2,26,-2,4,1,4)          // its collars
                .texOffs(88,22).addBox(-2,31,-2,4,1,4)
                .texOffs(88,22).addBox(-2,36,-2,4,1,4)
                .texOffs(88,12).addBox(-2.5F,39,-2.5F,5,5,5)    // from the waist a stepped cone, like the machine's
                .texOffs(52,12).addBox(-4.5F,44,-4.5F,9,5,9)
                .texOffs(0,12).addBox(-6.5F,49,-6.5F,13,5,13)   // to a toothed hem
                .texOffs(0,30).addBox(-.5F,18,-.5F,1,4,1)       // neck, a stalk
                .texOffs(4,30).addBox(-2.5F,54,-.5F,1,17,1)     // legs, a pixel wide
                .texOffs(4,30).addBox(1.5F,54,-.5F,1,17,1)
                .texOffs(116,18).addBox(-3,61,-1,2,2,2)         // a bead at each knee
                .texOffs(116,18).addBox(1,61,-1,2,2,2)
                .texOffs(8,30).addBox(-3.5F,71,-1.5F,2,1,2)     // narrow feet
                .texOffs(8,30).addBox(1.5F,71,-1.5F,2,1,2),PartPose.ZERO);
        hoop(body,"hoop0",28.5F,.35F);hoop(body,"hoop1",33.5F,-.3F);
        var head=body.addOrReplaceChild("head",CubeListBuilder.create()
                .texOffs(18,0).addBox(-2.5F,-9,-2,5,8,4)        // the long smooth oval
                .texOffs(48,6).addBox(-1.5F,-1,-1.5F,3,1,3)     // its chin
                .texOffs(36,0).addBox(-4.5F,-10,-2.5F,9,1,5)    // a narrow brim
                .texOffs(0,0).addBox(-2.5F,-16,-2,5,6,4),       // the crown, too tall
                PartPose.offset(0,18,0));
        head.addOrReplaceChild("tip",CubeListBuilder.create().texOffs(36,6).addBox(-1.5F,-3,-1.5F,3,3,3),PartPose.offsetAndRotation(.5F,-16,0,0,0,-.7F));
        for(int side:new int[]{-1,1}){
            String s=side<0?"left":"right";
            var upper=body.addOrReplaceChild(s+"_arm",CubeListBuilder.create().texOffs(16,30).addBox(-.5F,0,-.5F,1,11,1),PartPose.offset(side*6F,23,0));
            var fore=upper.addOrReplaceChild(s+"_forearm",CubeListBuilder.create().texOffs(20,30).addBox(-.5F,0,-.5F,1,14,1)
                    .texOffs(116,18).addBox(-1,-1,-1,2,2,2).texOffs(116,18).addBox(-1,13,-1,2,2,2),PartPose.offset(0,11,0));
            for(int i=0;i<FINGERS.length;i++)fore.addOrReplaceChild(s+"_finger"+i,CubeListBuilder.create().texOffs(24,30).addBox(-.5F,0,-.5F,1,FINGERS[i],1),PartPose.offsetAndRotation(side*(i-1.5F)*1.25F,14,0,0,0,side*(i-1.5F)*.12F));
        }
        return LayerDefinition.create(mesh,ATLAS_W,ATLAS_H).bakeRoot();
    }
    /** A hoop round his spindle at {@code y}, tilted by {@code pitch}: eight rods in a ring, and a bead riding it. */
    private static void hoop(PartDefinition parent,String name,float y,float pitch){
        var h=parent.addOrReplaceChild(name,CubeListBuilder.create(),PartPose.offsetAndRotation(0,y,0,pitch,0,0));
        for(int k=0;k<8;k++){float t=k*Mth.PI/4;h.addOrReplaceChild("rod"+k,CubeListBuilder.create().texOffs(116,22).addBox(-2,-.5F,-.5F,4,1,1),PartPose.offsetAndRotation(HOOP*Mth.sin(t),0,HOOP*Mth.cos(t),0,t,0));}
        float t=Mth.PI/8,corner=HOOP/Mth.cos(t);
        h.addOrReplaceChild("bead",CubeListBuilder.create().texOffs(116,18).addBox(-1,-1,-1,2,2,2),PartPose.offset(corner*Mth.sin(t),0,corner*Mth.cos(t)));
    }

    /** How he holds himself at a moment: a slow sway, his head laid over, the arms hanging out from him, the fingers working, the hoops turning. */
    static void pose(ModelPart r,float time){
        var body=r.getChild("body");var head=body.getChild("head");
        body.zRot=.04F*Mth.sin(time*.031F);
        head.zRot=.16F+.05F*Mth.sin(time*.023F);head.xRot=-.08F;
        body.getChild("hoop0").yRot=time*.07F;body.getChild("hoop1").yRot=-time*.1F;
        for(int side:new int[]{-1,1}){
            String s=side<0?"left":"right";
            var upper=body.getChild(s+"_arm");var fore=upper.getChild(s+"_forearm");
            // Hanging away from him, and bent further out at the elbow.
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

    // ------------------------------------------------------------------ 0.4.439: at the machine
    /*
     * The owner: "Can you overlay the machinery with the shiny gentleman 'eating' her? ... Just a flash for about two seconds". For two
     * seconds the reader's machine is not there (client.BorrascaOrrery.devour): he is, four and a half times his height, standing in its
     * housing, the cone below his waist where its cone stood, folded down at the waist over its wall, his hoops whirling round him, his neck drawn out long down to the slot, his
     * faceless oval split across and open around her, and his arms drawn out too, the hooked fingers on her shoulders. The same boxes and
     * atlas as his standing body, cut apart at the joints this needs.
     */
    private static ModelPart feeding;
    static ModelPart feeding(){if(feeding==null)feeding=buildFeeding();return feeding;}
    static ModelPart buildFeeding(){
        var mesh=new MeshDefinition();var r=mesh.getRoot();
        // Cut at the waist so that he can fold over: the cone below it and the legs, and the body above.
        r.addOrReplaceChild("legs",CubeListBuilder.create().texOffs(88,12).addBox(-2.5F,0,-2.5F,5,5,5).texOffs(52,12).addBox(-4.5F,5,-4.5F,9,5,9).texOffs(0,12).addBox(-6.5F,10,-6.5F,13,5,13)
                .texOffs(4,30).addBox(-2.5F,15,-.5F,1,17,1).texOffs(4,30).addBox(1.5F,15,-.5F,1,17,1).texOffs(116,18).addBox(-3,22,-1,2,2,2).texOffs(116,18).addBox(1,22,-1,2,2,2)
                .texOffs(8,30).addBox(-3.5F,32,-1.5F,2,1,2).texOffs(8,30).addBox(1.5F,32,-1.5F,2,1,2),PartPose.offset(0,WAIST,0));
        var torso=r.addOrReplaceChild("torso",CubeListBuilder.create().texOffs(52,26).addBox(-6.5F,-17,-.5F,13,1,1).texOffs(116,12).addBox(-7.5F,-18,-1.5F,3,3,3).texOffs(116,12).addBox(4.5F,-18,-1.5F,3,3,3)
                .texOffs(108,12).addBox(-1,-15,-1,2,15,2).texOffs(88,22).addBox(-2,-13,-2,4,1,4).texOffs(88,22).addBox(-2,-8,-2,4,1,4).texOffs(88,22).addBox(-2,-3,-2,4,1,4),PartPose.offset(0,WAIST,0));
        hoop(torso,"hoop0",-10.5F,.35F);hoop(torso,"hoop1",-5.5F,-.3F);
        // The oval cut across its middle, hinged at the back: the crown half (with the brim, the hat and its bent tip) and the jaw half.
        var crown=r.addOrReplaceChild("crown",CubeListBuilder.create().texOffs(18,0).addBox(-2.5F,-4,-4,5,4,4).texOffs(36,0).addBox(-4.5F,-5,-4.5F,9,1,5)
                .texOffs(0,0).addBox(-2.5F,-11,-4,5,6,4),PartPose.offset(0,-5,2));
        crown.addOrReplaceChild("tip",CubeListBuilder.create().texOffs(36,6).addBox(-1.5F,-3,-1.5F,3,3,3),PartPose.offsetAndRotation(.5F,-11,-2,0,0,-.7F));
        r.addOrReplaceChild("jaw",CubeListBuilder.create().texOffs(18,4).addBox(-2.5F,0,-4,5,4,4).texOffs(48,6).addBox(-1.5F,4,-3.5F,3,1,3),PartPose.offset(0,-5,2));
        // Straight pieces to be drawn out between two points: the neck, the upper arm, the forearm, the fingers; and a ball for each joint.
        r.addOrReplaceChild("neck",CubeListBuilder.create().texOffs(0,30).addBox(-.5F,0,-.5F,1,4,1),PartPose.ZERO);
        r.addOrReplaceChild("upper",CubeListBuilder.create().texOffs(16,30).addBox(-.5F,0,-.5F,1,11,1),PartPose.ZERO);
        r.addOrReplaceChild("fore",CubeListBuilder.create().texOffs(20,30).addBox(-.5F,0,-.5F,1,14,1),PartPose.ZERO);
        r.addOrReplaceChild("finger",CubeListBuilder.create().texOffs(24,30).addBox(-.5F,0,-.5F,1,8,1),PartPose.ZERO);
        r.addOrReplaceChild("joint",CubeListBuilder.create().texOffs(116,18).addBox(-1,-1,-1,2,2,2),PartPose.ZERO);
        return LayerDefinition.create(mesh,ATLAS_W,ATLAS_H).bakeRoot();
    }
    /**
     * His size at the machine: four and a half times his own, so that the stepped cone below his waist stands on the housing's walls where
     * the machine's cone stood, from the top of the walls to nine blocks up.
     */
    static final float FEED_SCALE=4.5F;
    /** His waist, in his model's pixels; and how far he is folded over at it (from upright, radians): down over the housing's wall toward the slot. */
    static final float WAIST=39,FEED_BEND=2.3F;
    /** Where the split in his face is: just over her, at the slot. */
    static final Vec3 FEED_MOUTH=new Vec3(0,.3,.15);
    /**
     * Him at the slot. The pose's origin is the slot's mouth just above the belt, +z the way her head lies; {@code feet} is where he stands
     * in that frame (the middle of the machine's housing, on its floor); {@code open} is how far his face is split (radians each way);
     * {@code flesh} is her skin, whose torn-end and bone tiles line the inside and make the teeth.
     */
    public static void renderFeeding(PoseStack pose,MultiBufferSource buffers,float alpha,float time,float open,ResourceLocation flesh,Vec3 feet){
        var r=feeding();float s=FEED_SCALE;int c=(int)(255*(.84F+.1F*Mth.sin(time*.27F)));int a=(int)(255*Mth.clamp(alpha,0,1));int colour=FastColor.ARGB32.color(a,c,c,c);
        // His boxes first, then (in a batch of its own: two consumers cannot be built at once) her flesh inside his face.
        var out=buffers.getBuffer(type());int light=LightTexture.FULL_BRIGHT,ov=OverlayTexture.NO_OVERLAY;int wet=FastColor.ARGB32.color(a,255,255,255);
        // Standing in the machine and folded over its wall at the waist, his shoulders over the belt, his hoops whirling.
        var torso=r.getChild("torso");torso.xRot=FEED_BEND+.04F*Mth.sin(time*.3F);torso.getChild("hoop0").yRot=time*.55F;torso.getChild("hoop1").yRot=-time*.8F;
        pose.pushPose();pose.translate(feet.x,feet.y,feet.z);pose.mulPose(Axis.YP.rotation(Mth.PI));pose.translate(0,FEET/16F*s,0);pose.scale(-s,-s,s);
        r.getChild("legs").render(pose,out,light,ov,colour);torso.render(pose,out,light,ov,colour);pose.popPose();
        Vec3 neckBase=onTorso(feet,0,-18,torso.xRot,s);
        // The head hangs at the end of the neck, its crown toward her head and its face turned down onto her.
        Vec3 crownWay=new Vec3(0,-.25,1).normalize(),faceWay=new Vec3(0,-1,-.25).normalize();
        Vec3 chin=FEED_MOUTH.subtract(crownWay.scale(5/16D*s));
        segment(pose,r.getChild("neck"),neckBase,chin,4,s*1.8F,out,colour);
        var crown=r.getChild("crown");var jaw=r.getChild("jaw");crown.xRot=-open;jaw.xRot=open*.85F;
        pose.pushPose();head(pose,chin,crownWay,faceWay,s);crown.render(pose,out,light,ov,colour);jaw.render(pose,out,light,ov,colour);pose.popPose();
        // Arms drawn out long from the balls at his shoulders, bent out at the elbow; the hands come down on her shoulders and the fingers hook over.
        for(int sideSign:new int[]{-1,1}){
            Vec3 shoulder=onTorso(feet,sideSign*6,-16.5F,torso.xRot,s);
            Vec3 hand=new Vec3(sideSign*.42,.28,.75);
            Vec3 elbow=shoulder.add(hand).scale(.5).add(sideSign*s*.4,s*.22,0);
            segment(pose,r.getChild("upper"),shoulder,elbow,11,s*1.3F,out,colour);segment(pose,r.getChild("fore"),elbow,hand,14,s*1.3F,out,colour);
            ball(pose,r.getChild("joint"),elbow,s*1.4F,out,colour);ball(pose,r.getChild("joint"),hand,s,out,colour);
            for(int i=0;i<FINGERS.length;i++){Vec3 way=new Vec3(sideSign*(i-1.5)*.22-sideSign*.15,-.75,.35+.12*Math.sin(time*.2+i)).normalize();
                segment(pose,r.getChild("finger"),hand,hand.add(way.scale(FINGERS[i]/16D*3.5)),8,3,out,colour);}
        }
        if(buffers instanceof MultiBufferSource.BufferSource batch)batch.endBatch(type());
        // Inside the split: her own torn flesh, and a row of her bone for teeth along each lip.
        var inside=buffers.getBuffer(RenderType.entityTranslucentEmissive(flesh));
        pose.pushPose();head(pose,chin,crownWay,faceWay,s);
        for(var half:new ModelPart[]{crown,jaw}){boolean top=half==crown;pose.pushPose();half.translateAndRotate(pose);float y=top?.03F:-.03F;
            quad(pose,inside,new float[][]{{-2.5F,y,-4},{2.5F,y,-4},{2.5F,y,0},{-2.5F,y,0}},56,16,64,24,light,ov,wet);
            for(int t=0;t<5;t++){float x=-2F+t,h=top?1.3F+.3F*(t%2):-1.1F-.3F*((t+1)%2);float y0=Math.min(0,h),y1=Math.max(0,h);box(pose,inside,x-.25F,y0,-3.9F,x+.25F,y1,-3.4F,59,19,61,21,light,ov,wet);}
            pose.popPose();}
        pose.popPose();
        if(buffers instanceof MultiBufferSource.BufferSource batch)batch.endBatch(RenderType.entityTranslucentEmissive(flesh));
    }
    /** His head's frame: at its chin, the crown turned {@code crownWay} and the face {@code faceWay}, at his size, as his model is drawn. */
    private static void head(PoseStack pose,Vec3 chin,Vec3 crownWay,Vec3 faceWay,float s){
        pose.translate(chin.x,chin.y,chin.z);Vec3 side=crownWay.cross(faceWay);
        pose.mulPose(new Quaternionf().setFromNormalized(new Matrix3f((float)side.x,(float)side.y,(float)side.z,(float)crownWay.x,(float)crownWay.y,(float)crownWay.z,(float)faceWay.x,(float)faceWay.y,(float)faceWay.z)));
        pose.mulPose(Axis.YP.rotation(Mth.PI));pose.scale(-s,-s,s);
    }
    /** A point of his folded body (pixels from the waist, his model's axes), in the slot's frame, his feet at {@code feet}. */
    private static Vec3 onTorso(Vec3 feet,float x,float y,float bend,float s){
        float my=WAIST+y*Mth.cos(bend),mz=y*Mth.sin(bend);
        return feet.add(s*x/16D,s*(FEET-my)/16D,-s*mz/16D);
    }
    /** A straight piece {@code px} pixels long, drawn out from {@code from} to {@code to} in the pose's frame, {@code width} times as thick. */
    private static void segment(PoseStack pose,ModelPart piece,Vec3 from,Vec3 to,int px,float width,VertexConsumer out,int colour){
        Vec3 d=to.subtract(from);double length=d.length();if(length<1e-4)return;d=d.scale(1/length);
        pose.pushPose();pose.translate(from.x,from.y,from.z);pose.mulPose(new Quaternionf().rotationTo(0,1,0,(float)d.x,(float)d.y,(float)d.z));
        pose.scale(width,(float)(length/(px/16D)),width);piece.render(pose,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,colour);pose.popPose();
    }
    /** A joint's ball at {@code at}, {@code scale} times its own size. */
    private static void ball(PoseStack pose,ModelPart piece,Vec3 at,float scale,VertexConsumer out,int colour){
        pose.pushPose();pose.translate(at.x,at.y,at.z);pose.scale(scale,scale,scale);piece.render(pose,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,colour);pose.popPose();
    }
    private static void quad(PoseStack p,VertexConsumer out,float[][] q,float u0,float v0,float u1,float v1,int light,int overlay,int colour){
        float[][] uv={{u0,v0},{u1,v0},{u1,v1},{u0,v1}};
        for(int i=0;i<4;i++)out.addVertex(p.last(),q[i][0]/16,q[i][1]/16,q[i][2]/16).setColor(colour).setUv(uv[i][0]/64,uv[i][1]/64).setOverlay(overlay).setLight(light).setNormal(p.last(),0,1,0);
    }
    private static void box(PoseStack p,VertexConsumer out,float x0,float y0,float z0,float x1,float y1,float z1,float u0,float v0,float u1,float v1,int light,int overlay,int colour){
        quad(p,out,new float[][]{{x0,y0,z0},{x1,y0,z0},{x1,y1,z0},{x0,y1,z0}},u0,v0,u1,v1,light,overlay,colour);
        quad(p,out,new float[][]{{x1,y0,z1},{x0,y0,z1},{x0,y1,z1},{x1,y1,z1}},u0,v0,u1,v1,light,overlay,colour);
        quad(p,out,new float[][]{{x0,y0,z1},{x0,y0,z0},{x0,y1,z0},{x0,y1,z1}},u0,v0,u1,v1,light,overlay,colour);
        quad(p,out,new float[][]{{x1,y0,z0},{x1,y0,z1},{x1,y1,z1},{x1,y1,z0}},u0,v0,u1,v1,light,overlay,colour);
        quad(p,out,new float[][]{{x0,y0,z1},{x1,y0,z1},{x1,y0,z0},{x0,y0,z0}},u0,v0,u1,v1,light,overlay,colour);
        quad(p,out,new float[][]{{x0,y1,z0},{x1,y1,z0},{x1,y1,z1},{x0,y1,z1}},u0,v0,u1,v1,light,overlay,colour);
    }
}
