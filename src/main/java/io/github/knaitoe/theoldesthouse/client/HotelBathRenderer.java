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
        int[] flesh={0xb5a18d,0xc4b09b,0xa49083,0xcdbba4,0x968583};
        for(int y=0;y<64;y++)for(int x=0;x<64;x++){int n=Math.floorMod(x/3*37+y/3*71,19);put(image,x,y,flesh[n<8?0:n<13?1:n<16?2:n<18?3:4]);}
        for(int[] patch:new int[][]{{14,13,2},{22,23,2},{26,29,2},{17,21,1},{46,23,2},{46,32,1},{5,22,1},{5,30,2},{27,37,1}})
            for(int dy=-patch[2];dy<=patch[2];dy++)for(int dx=-patch[2];dx<=patch[2];dx++){
                int q=dx*dx+dy*dy;if(q>patch[2]*patch[2])continue;
                put(image,patch[0]+dx,patch[1]+dy,q<2?0x674644:q<4?0x8d6057:0xa88870);
            }
        // Eight-pixel human face: hooded eyes, bridge, intact cheek and a small slack mouth.
        for(int x=8;x<16;x++)put(image,x,8,0xb6a08a);
        for(int x:new int[]{9,10,13,14})put(image,x,9,0x85736a);
        for(int x:new int[]{9,13}){put(image,x,10,0xd6cdbc);put(image,x+1,10,0xb9b4a5);put(image,x,11,0x9a8280);}
        put(image,10,10,0x787b76);put(image,13,10,0x83847a);
        put(image,11,11,0xc6b09a);put(image,12,11,0x998274);put(image,11,12,0x9c8075);put(image,12,12,0xaa8d7c);
        put(image,10,13,0xb89988);put(image,11,13,0x735754);put(image,12,13,0x674b49);put(image,11,14,0x997568);put(image,12,14,0xa98b77);
        // One cheek has slipped down, exposing tissue rather than a skull cavity.
        put(image,14,11,0x897174);put(image,14,12,0x9b6b60);put(image,15,12,0x704945);put(image,14,13,0x86554d);put(image,15,13,0x563e3c);put(image,14,14,0xb99b7c);put(image,15,14,0x9f7c68);
        for(int y=32;y<44;y++)for(int x=0;x<24;x++)put(image,x,y,(x+y)%5==0?0xa78f7b:0xbba28b);
        put(image,8,39,0x795b54);put(image,9,39,0x886257);
        for(int y=40;y<56;y++)for(int x=48;x<64;x++)put(image,x,y,(x+y)%5==0?0x996d5d:(x/2+y/3)%3==0?0xc5aa88:0xb79a7d);
        for(int y=48;y<64;y++)for(int x=0;x<16;x++)put(image,x,y,(x+y/4)%4==0?0x696056:0x484440);
        for(int y=0;y<16;y++)for(int x=32;x<38;x++)put(image,x,y,(x+y/4)%4==0?0x696056:0x484440);
        var dynamic=new DynamicTexture(image);dynamic.setFilter(false,false);texture=Minecraft.getInstance().getTextureManager().register("hotel_bath_decayed_human",dynamic);return texture;
    }
    private static void put(NativeImage image,int x,int y,int rgb){if(x>=0&&x<64&&y>=0&&y<64)image.setPixelRGBA(x,y,0xff000000|(rgb&255)<<16|(rgb&0xff00)|(rgb>>16&255));}
    public static final class Model extends EntityModel<HotelActor>{
        private final ModelPart root,head,jaw,body,leftArm,rightArm,leftElbow,rightElbow,leftLeg,rightLeg,leftKnee,rightKnee;private int phase,age;
        public Model(ModelPart r){root=r;body=r.getChild("body");head=body.getChild("head");jaw=head.getChild("jaw");leftArm=body.getChild("left_arm");rightArm=body.getChild("right_arm");leftElbow=leftArm.getChild("elbow");rightElbow=rightArm.getChild("elbow");leftLeg=r.getChild("left_leg");rightLeg=r.getChild("right_leg");leftKnee=leftLeg.getChild("knee");rightKnee=rightLeg.getChild("knee");}
        public static LayerDefinition layer(){
            var mesh=new MeshDefinition();var r=mesh.getRoot();
            var torso=r.addOrReplaceChild("body",CubeListBuilder.create().texOffs(16,16).addBox(-4,0,-2,8,12,4),PartPose.ZERO);
            var head=torso.addOrReplaceChild("head",CubeListBuilder.create().texOffs(0,0).addBox(-4,-8,-4,8,8,8).texOffs(24,32).addBox(-1,-4,-4.6F,2,2,1),PartPose.ZERO);
            head.addOrReplaceChild("jaw",CubeListBuilder.create().texOffs(0,32).addBox(-3,0,-3.9F,6,2,6),PartPose.offset(0,-1.4F,0));
            head.addOrReplaceChild("cheek",CubeListBuilder.create().texOffs(48,40).addBox(0,0,-.3F,2,3.5F,.6F),PartPose.offsetAndRotation(1.8F,-2.8F,-4.05F,.12F,0,-.13F));
            torso.addOrReplaceChild("chest_skin",CubeListBuilder.create().texOffs(48,40).addBox(-1.4F,0,-.3F,2.8F,4.5F,.6F),PartPose.offsetAndRotation(-1.6F,2.5F,-2.2F,-.11F,0,.08F));
            torso.addOrReplaceChild("belly_skin",CubeListBuilder.create().texOffs(48,46).addBox(-1.8F,0,-.3F,3.6F,3,.6F),PartPose.offsetAndRotation(1.5F,8,-2.15F,-.16F,0,-.12F));
            head.addOrReplaceChild("hair_back",CubeListBuilder.create().texOffs(0,48).addBox(-3.5F,0,-.4F,7,9,.8F),PartPose.offset(0,-7,3.7F));
            for(int i=0;i<3;i++)head.addOrReplaceChild("hair"+i,CubeListBuilder.create().texOffs(32,0).addBox(-.4F,0,-.35F,.8F,9+i,.7F),PartPose.offset(i==0?-3.8F:i==1?3.8F:2.8F,-7,i==2?-3.8F:.7F));
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
            body.xRot=.10F;head.xRot=.08F;head.zRot=.045F;jaw.xRot=.035F+.012F*Mth.sin(time*.1F);
            leftArm.xRot=-1.03F;rightArm.xRot=-1.23F;leftArm.yRot=.12F;rightArm.yRot=-.13F;leftElbow.xRot=-.3F;rightElbow.xRot=-.12F;
            float drag=Mth.cos(walk*.6662F)*Math.min(.24F,amount);leftLeg.xRot=.04F+drag;rightLeg.xRot=.06F-drag;leftKnee.xRot=.08F+Math.max(0,-drag);rightKnee.xRot=.1F+Math.max(0,drag);
            for(int i=0;i<3;i++){leftElbow.getChild("finger"+i).xRot=-.32F-.14F*Mth.sin(time*.09F+i);rightElbow.getChild("finger"+i).xRot=-.4F-.18F*Mth.sin(time*.08F+i);}
            if(phase==HotelBathHaunt.RISING){body.xRot=.23F;head.xRot=.12F;leftLeg.xRot=rightLeg.xRot=-.45F;leftKnee.xRot=rightKnee.xRot=.65F;leftArm.xRot=-.55F;rightArm.xRot=-.85F;}
            if(phase==HotelBathHaunt.FADING){body.xRot+=.15F;head.xRot+=.18F;}
        }
        @Override public void setupAnim(HotelActor a,float walk,float amount,float time,float yaw,float pitch){pose(a.bathPhase(),a.bathAge(),time,walk,amount);}
        @Override public void renderToBuffer(PoseStack p,VertexConsumer out,int light,int overlay,int color){int alpha=phase==HotelBathHaunt.FADING?Math.max(0,(int)(255*(1-age/(float)HotelBathHaunt.FADE_TICKS))):255;root.render(p,out,light,overlay,(color&0xffffff)|alpha<<24);}
    }
}
