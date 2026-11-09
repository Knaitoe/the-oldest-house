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
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Matrix3f;

/**
 * 0.4.434, the owner: "Ensure the shiny gentleman still appears as an actual monster out by the far marker too". Until now he was one flat
 * picture turned to the reader; now he has a body, built from boxes in native pixels (16 to a block) and painted by
 * tools/draw_shiny_gentleman.py: a tall hat bent over at the crown and grown into his head, a faceless polished oval with one bright band
 * across it, a neck like a stalk, shoulders too wide over a narrow coat that falls to a ragged hem, legs a pixel wide, and arms that hang
 * apart from the coat, bend out at the elbow and end past his knees in four long hooked fingers. He stands, sways a little, lays his head
 * over to one side, and his fingers curl and open. He is still the reader's alone, drawn only on their screen where the server says he
 * stands.
 * <p>
 * 0.4.439, the owner: "the monster is the child's imagination seeing him as an adult is that trauma made real". He is what the child makes
 * of the machine on the hill, and he keeps that shape: on the last night the grown reader sees the same figure at the machine, for two
 * seconds, real ({@link #renderFeeding}).
 */
public final class BorrascaGentlemanModel {
    private BorrascaGentlemanModel(){}
    public static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/borrasca_gentleman.png");
    /** The atlas, in pixels (tools/draw_shiny_gentleman.py's W and H): his boxes on the left half, the machine's materials on the right. */
    static final int ATLAS_W=128,ATLAS_H=64;
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
        return LayerDefinition.create(mesh,ATLAS_W,ATLAS_H).bakeRoot();
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

    // ------------------------------------------------------------------ 0.4.439: at the machine
    /*
     * The owner: "Can you overlay the machinery with the shiny gentleman 'eating' her? ... Just a flash for about two seconds". For two
     * seconds the reader's machine is not there (client.BorrascaOrrery.devour) and he is: the same figure the child saw in the pines, real,
     * four and a half times his height, standing in its housing with his coat on its walls where the cone stood, folded down at the waist
     * over its south wall, his neck drawn out long down to the slot, his faceless oval split across and open around her, and his arms
     * drawn out too, the hooked fingers on her shoulders. The same boxes and atlas as his standing body, cut apart at the joints this needs.
     */
    private static ModelPart feeding;
    static ModelPart feeding(){if(feeding==null)feeding=buildFeeding();return feeding;}
    static ModelPart buildFeeding(){
        var mesh=new MeshDefinition();var r=mesh.getRoot();
        // Cut at the waist so that he can fold over: the coat's skirt and the legs below it, and the body above.
        r.addOrReplaceChild("legs",CubeListBuilder.create().texOffs(0,0).addBox(-5.5F,0,-2.5F,11,15,5).texOffs(16,30).addBox(-2.5F,15,-.5F,1,17,1).texOffs(16,30).addBox(1.5F,15,-.5F,1,17,1)
                .texOffs(0,34).addBox(-3.5F,32,-1.5F,2,1,2).texOffs(0,34).addBox(1.5F,32,-1.5F,2,1,2),PartPose.offset(0,WAIST,0));
        r.addOrReplaceChild("torso",CubeListBuilder.create().texOffs(32,6).addBox(-5.5F,-17,-1.5F,11,2,3).texOffs(0,20).addBox(-4.5F,-15,-1.5F,9,7,3).texOffs(24,21).addBox(-3.5F,-8,-1.5F,7,8,3),PartPose.offset(0,WAIST,0));
        // The oval cut across its middle, hinged at the back: the crown half (with the brim, the hat and its bent tip) and the jaw half.
        var crown=r.addOrReplaceChild("crown",CubeListBuilder.create().texOffs(44,21).addBox(-2.5F,-4,-4,5,4,4).texOffs(32,0).addBox(-4.5F,-5,-4.5F,9,1,5)
                .texOffs(32,11).addBox(-2.5F,-11,-4,5,6,4),PartPose.offset(0,-5,2));
        crown.addOrReplaceChild("tip",CubeListBuilder.create().texOffs(50,11).addBox(-1.5F,-3,-1.5F,3,3,3),PartPose.offsetAndRotation(.5F,-11,-2,0,0,-.7F));
        r.addOrReplaceChild("jaw",CubeListBuilder.create().texOffs(44,25).addBox(-2.5F,0,-4,5,4,4).texOffs(0,30).addBox(-1.5F,4,-3.5F,3,1,3),PartPose.offset(0,-5,2));
        // Straight pieces to be drawn out between two points: the neck, the upper arm, the forearm, the fingers.
        r.addOrReplaceChild("neck",CubeListBuilder.create().texOffs(12,30).addBox(-.5F,0,-.5F,1,4,1),PartPose.ZERO);
        r.addOrReplaceChild("upper",CubeListBuilder.create().texOffs(20,33).addBox(-.5F,0,-.5F,1,11,1),PartPose.ZERO);
        r.addOrReplaceChild("fore",CubeListBuilder.create().texOffs(24,33).addBox(-.5F,0,-.5F,1,14,1),PartPose.ZERO);
        r.addOrReplaceChild("finger",CubeListBuilder.create().texOffs(28,33).addBox(-.5F,0,-.5F,1,8,1),PartPose.ZERO);
        return LayerDefinition.create(mesh,ATLAS_W,ATLAS_H).bakeRoot();
    }
    /**
     * His size at the machine: four and a half times his own, so that his coat stands on the housing's walls where the machine's cone stood,
     * from the top of the walls to nine blocks up.
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
        // Standing in the machine and folded down over its wall at the waist, his shoulders over the belt.
        var torso=r.getChild("torso");torso.xRot=FEED_BEND+.04F*Mth.sin(time*.3F);
        pose.pushPose();pose.translate(feet.x,feet.y,feet.z);pose.mulPose(Axis.YP.rotation(Mth.PI));pose.translate(0,FEET/16F*s,0);pose.scale(-s,-s,s);
        r.getChild("legs").render(pose,out,light,ov,colour);torso.render(pose,out,light,ov,colour);pose.popPose();
        Vec3 neckBase=onTorso(feet,0,-17,torso.xRot,s);
        // The head hangs at the end of the neck, its crown toward her head and its face turned down onto her.
        Vec3 crownWay=new Vec3(0,-.25,1).normalize(),faceWay=new Vec3(0,-1,-.25).normalize();
        Vec3 chin=FEED_MOUTH.subtract(crownWay.scale(5/16D*s));
        segment(pose,r.getChild("neck"),neckBase,chin,4,s*1.8F,out,colour);
        var crown=r.getChild("crown");var jaw=r.getChild("jaw");crown.xRot=-open;jaw.xRot=open*.85F;
        pose.pushPose();head(pose,chin,crownWay,faceWay,s);crown.render(pose,out,light,ov,colour);jaw.render(pose,out,light,ov,colour);pose.popPose();
        // Arms drawn out long from the shoulders, bent out at the elbow; the hands come down on her shoulders and the fingers hook over.
        for(int sideSign:new int[]{-1,1}){
            Vec3 shoulder=onTorso(feet,sideSign*5,-16,torso.xRot,s);
            Vec3 hand=new Vec3(sideSign*.42,.28,.75);
            Vec3 elbow=shoulder.add(hand).scale(.5).add(sideSign*s*.4,s*.22,0);
            segment(pose,r.getChild("upper"),shoulder,elbow,11,s*1.3F,out,colour);segment(pose,r.getChild("fore"),elbow,hand,14,s*1.3F,out,colour);
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
