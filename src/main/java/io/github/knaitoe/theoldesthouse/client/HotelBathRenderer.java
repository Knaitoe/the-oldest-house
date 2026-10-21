package io.github.knaitoe.theoldesthouse.client;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
/** A waterlogged human body: intact proportions beneath loosened skin, never a skeletal ghoul. */
public final class HotelBathRenderer extends MobRenderer<HotelActor,HotelBathRenderer.Model> {
    public static final ModelLayerLocation LAYER=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("the_oldest_house","bath_creature"),"main");
    private static ResourceLocation texture;
    public HotelBathRenderer(EntityRendererProvider.Context c){super(c,new Model(c.bakeLayer(LAYER)),0);}
    public static boolean visible(HotelActor a){return a.bathPhase()!=0;}
    @Override public void render(HotelActor a,float yaw,float partial,PoseStack p,MultiBufferSource buffers,int light){if(visible(a))super.render(a,yaw,partial,p,buffers,light);}
    @Override protected RenderType getRenderType(HotelActor a,boolean body,boolean translucent,boolean glowing){return RenderType.entityTranslucent(skin());}
    @Override protected boolean shouldShowName(HotelActor a){return false;}
    @Override public ResourceLocation getTextureLocation(HotelActor a){return skin();}
    public static ResourceLocation skin(){
        if(texture!=null)return texture;
        var image=new NativeImage(64,64,false);
        // Low-contrast islands of swollen, bruised skin, rather than green skeletal striping.
        int[] flesh={0xb3a29a,0xc5b5a7,0x9f8a88,0xcabcb0,0x8b777c};
        for(int y=0;y<64;y++)for(int x=0;x<64;x++){int n=Math.floorMod(x/3*37+y/3*71,19);put(image,x,y,flesh[n<8?0:n<13?1:n<16?2:n<18?3:4]);}
        for(int[] patch:new int[][]{{14,13,2},{22,23,2},{26,29,2},{17,21,1},{46,23,2},{46,32,1},{5,22,1},{5,30,2},{27,37,1}})
            for(int dy=-patch[2];dy<=patch[2];dy++)for(int dx=-patch[2];dx<=patch[2];dx++){
                int q=dx*dx+dy*dy;if(q>patch[2]*patch[2])continue;
                put(image,patch[0]+dx,patch[1]+dy,q<2?0x674644:q<4?0x8d6057:0xa88870);
            }
        // One native eight-pixel face. Both nose and mouth belong to this plane, without a projecting lower-face block.
        for(int y=8;y<=15;y++)for(int x=8;x<=15;x++)put(image,x,y,x==8||x==15?0x9f8984:y<10?0xb8a49a:0xb09a90);
        put(image,9,9,0x756065);put(image,10,9,0x81696b);put(image,13,9,0x725d63);put(image,14,9,0x6b575d);
        put(image,9,10,0xcac8ba);put(image,10,10,0x9b9d92);put(image,13,10,0xb7b9b0);put(image,14,10,0x959d96);
        put(image,9,11,0x887274);put(image,10,11,0x9b7f7d);put(image,13,11,0x765961);put(image,14,11,0x805d62);
        put(image,11,11,0xbea99d);put(image,12,11,0xa18a82);put(image,11,12,0xaa8e83);put(image,12,12,0x957970);
        put(image,8,12,0x8d7370);put(image,9,12,0xa68a80);put(image,9,13,0x9b7d73);
        // Raw cheek and a torn lip remain bounded by recognizable human features.
        put(image,14,12,0x815257);put(image,15,11,0x78565d);put(image,15,12,0x6a454b);put(image,14,13,0x9b665e);put(image,15,13,0x70504d);
        put(image,10,14,0x8a6463);put(image,11,14,0x614246);put(image,12,14,0x533b40);put(image,13,14,0x795051);put(image,13,15,0x986962);
        put(image,11,15,0xaf8f81);put(image,12,15,0xa38075);put(image,14,15,0x885b56);
        for(int y=40;y<56;y++)for(int x=48;x<64;x++)put(image,x,y,(x+y)%5==0?0x8c5f5b:(x/2+y/3)%3==0?0xbba298:0xa58177);
        for(int y=48;y<64;y++)for(int x=0;x<16;x++)put(image,x,y,(x+y/4)%4==0?0x696056:0x484440);
        for(int y=0;y<16;y++)for(int x=32;x<62;x++)put(image,x,y,(x+y/4)%4==0?0x696056:0x484440);
        var dynamic=new DynamicTexture(image);dynamic.setFilter(false,false);texture=Minecraft.getInstance().getTextureManager().register("hotel_bath_decayed_human",dynamic);return texture;
    }
    private static void put(NativeImage image,int x,int y,int rgb){if(x>=0&&x<64&&y>=0&&y<64)image.setPixelRGBA(x,y,0xff000000|(rgb&255)<<16|(rgb&0xff00)|(rgb>>16&255));}
    public static final class Model extends EntityModel<HotelActor>{
        private final ModelPart root,head,body,leftArm,rightArm,leftElbow,rightElbow,leftLeg,rightLeg,leftKnee,rightKnee;private int phase,age;
        public Model(ModelPart r){root=r;body=r.getChild("body");head=body.getChild("head");leftArm=body.getChild("left_arm");rightArm=body.getChild("right_arm");leftElbow=leftArm.getChild("elbow");rightElbow=rightArm.getChild("elbow");leftLeg=r.getChild("left_leg");rightLeg=r.getChild("right_leg");leftKnee=leftLeg.getChild("knee");rightKnee=rightLeg.getChild("knee");}
        public static LayerDefinition layer(){
            var mesh=new MeshDefinition();var r=mesh.getRoot();
            var torso=r.addOrReplaceChild("body",CubeListBuilder.create().texOffs(16,16).addBox(-4,0,-2,8,12,4),PartPose.ZERO);
            var head=torso.addOrReplaceChild("head",CubeListBuilder.create().texOffs(0,0).addBox(-4,-8,-4,8,8,8),PartPose.ZERO);
            head.addOrReplaceChild("cheek",CubeListBuilder.create().texOffs(48,40).addBox(0,0,-.15F,1.4F,2.8F,.3F).texOffs(48,44).addBox(.6F,2.4F,-.1F,.8F,1.4F,.2F),PartPose.offsetAndRotation(2.5F,-2.8F,-4.05F,.10F,0,-.16F));
            torso.addOrReplaceChild("chest_skin",CubeListBuilder.create().texOffs(48,40).addBox(-1.4F,0,-.3F,2.8F,4.5F,.6F),PartPose.offsetAndRotation(-1.6F,2.5F,-2.2F,-.11F,0,.08F));
            torso.addOrReplaceChild("belly_skin",CubeListBuilder.create().texOffs(48,46).addBox(-1.8F,0,-.3F,3.6F,3,.6F),PartPose.offsetAndRotation(1.5F,8,-2.15F,-.16F,0,-.12F));
            head.addOrReplaceChild("hair_back",CubeListBuilder.create().texOffs(0,48).addBox(-3.5F,0,-.4F,7,10,.8F),PartPose.offset(0,-7.5F,3.8F));
            head.addOrReplaceChild("hair_crown",CubeListBuilder.create().texOffs(38,0).addBox(-3.5F,-.2F,-1,7,.4F,4),PartPose.offset(0,-8,0));
            for(int i=0;i<4;i++)head.addOrReplaceChild("hair"+i,CubeListBuilder.create().texOffs(32,0).addBox(-.4F,0,-.35F,.8F,9+i%2,.7F),PartPose.offset(i<2?-3.9F:3.9F,-7,i%2==0?-2.8F:1.2F));
            for(boolean left:new boolean[]{true,false}){
                var arm=torso.addOrReplaceChild(left?"left_arm":"right_arm",CubeListBuilder.create().texOffs(40,16).addBox(-1.5F,-1.5F,-1.5F,3,6.5F,3),PartPose.offset(left?5:-5,2,0));
                var elbow=arm.addOrReplaceChild("elbow",CubeListBuilder.create().texOffs(40,26).addBox(-1.35F,0,-1.35F,2.7F,6,2.7F).texOffs(40,36).addBox(-1.4F,5.4F,-1.5F,2.8F,2,2),PartPose.offset(0,4.5F,0));
                elbow.addOrReplaceChild("loose_skin",CubeListBuilder.create().texOffs(48,40).addBox(-1,0,-.25F,2,3.4F,.5F),PartPose.offsetAndRotation(.1F,1.4F,-1.45F,-.15F,0,.18F));
                for(int i=0;i<3;i++)elbow.addOrReplaceChild("finger"+i,CubeListBuilder.create().texOffs(40,42).addBox(-.35F,0,-.35F,.7F,2.3F,.7F),PartPose.offset(-.9F+i*.9F,7,-1.1F));
                elbow.addOrReplaceChild("thumb",CubeListBuilder.create().texOffs(40,42).addBox(-.35F,0,-.35F,.7F,1.8F,.7F),PartPose.offsetAndRotation(left?-1.5F:1.5F,5.5F,-1.1F,-.35F,0,left?.45F:-.45F));
                var leg=r.addOrReplaceChild(left?"left_leg":"right_leg",CubeListBuilder.create().texOffs(0,16).addBox(-1.75F,0,-1.75F,3.5F,6,3.5F),PartPose.offset(left?2:-2,12,0));
                var knee=leg.addOrReplaceChild("knee",CubeListBuilder.create().texOffs(0,24).addBox(-1.5F,0,-1.5F,3,6,3).texOffs(16,48).addBox(-1.5F,5,-2.5F,3,1,4),PartPose.offset(0,6,0));
                knee.addOrReplaceChild("loose_skin",CubeListBuilder.create().texOffs(48,46).addBox(-1,0,-.25F,2,3,.5F),PartPose.offsetAndRotation(.3F,1,-1.6F,-.13F,0,-.08F));
            }return LayerDefinition.create(mesh,64,64);
        }
        public void pose(int phase,int age,float time,float walk,float amount){
            root.getAllParts().forEach(ModelPart::resetPose);this.phase=phase;this.age=age;
            body.xRot=.20F;head.xRot=-.18F;head.zRot=-.035F;head.xScale=.94F;head.zScale=.90F;
            leftArm.xRot=-1.43F;rightArm.xRot=-1.58F;leftArm.yRot=.10F;rightArm.yRot=-.16F;leftElbow.xRot=-.18F;rightElbow.xRot=-.32F;
            float drag=Mth.cos(walk*.6662F)*Math.min(.24F,amount);leftLeg.xRot=.04F+drag;rightLeg.xRot=.06F-drag;leftKnee.xRot=.08F+Math.max(0,-drag);rightKnee.xRot=.1F+Math.max(0,drag);
            for(int i=0;i<3;i++){leftElbow.getChild("finger"+i).xRot=-.62F-.14F*Mth.sin(time*.09F+i);rightElbow.getChild("finger"+i).xRot=-.72F-.18F*Mth.sin(time*.08F+i);}
            if(phase==HotelBathHaunt.RISING){body.xRot=.28F;head.xRot=-.22F;leftLeg.xRot=rightLeg.xRot=-.45F;leftKnee.xRot=rightKnee.xRot=.65F;leftArm.xRot=-1.2F;rightArm.xRot=-1.5F;}
            if(phase==HotelBathHaunt.FADING){body.xRot+=.15F;head.xRot+=.18F;}
        }
        @Override public void setupAnim(HotelActor a,float walk,float amount,float time,float yaw,float pitch){pose(a.bathPhase(),a.bathAge(),time,walk,amount);}
        /** The native face review keeps the same parent rotation, face, hair and hanging cheek as the scene. */
        public void renderHead(PoseStack p,VertexConsumer out,int light){p.pushPose();body.translateAndRotate(p);head.render(p,out,light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,-1);p.popPose();}
        @Override public void renderToBuffer(PoseStack p,VertexConsumer out,int light,int overlay,int color){int alpha=phase==HotelBathHaunt.FADING?Math.max(0,(int)(255*(1-age/(float)HotelBathHaunt.FADE_TICKS))):255;root.render(p,out,light,overlay,(color&0xffffff)|alpha<<24);}
    }
}
