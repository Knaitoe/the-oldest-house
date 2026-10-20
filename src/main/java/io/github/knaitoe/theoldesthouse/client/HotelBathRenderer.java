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
/** Dedicated decayed creature, with a loose jaw, thin ribs, sparse wet hair and articulated grasping hands. */
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
        if(texture!=null)return texture;var image=new NativeImage(64,64,false);int[] flesh={0x8a8b72,0x787c62,0xa09d80,0x626a53,0xb3aa89};
        for(int y=0;y<64;y++)for(int x=0;x<64;x++){int h=Math.floorMod(x/2*73+y/2*151+x*y*7,31);put(image,x,y,flesh[h<8?0:h<18?1:h<24?2:h<29?3:4]);}
        for(int[] wound:new int[][]{{23,25,3},{28,21,2},{44,24,2},{6,24,2},{32,46,3},{19,39,2},{13,4,2}})
            for(int dy=-wound[2];dy<=wound[2];dy++)for(int dx=-wound[2];dx<=wound[2];dx++){int q=dx*dx+dy*dy;if(q<=wound[2]*wound[2])put(image,wound[0]+dx,wound[1]+dy,q<3?0x3e3630:q<6?0x635049:0x514f37);}
        for(int y=0;y<8;y++)for(int x=8;x<16;x++)if((x+y)%3!=0)put(image,x,y,0x45493b);
        for(int y=8;y<16;y++)for(int x=24;x<32;x++)if((x+y/3)%3==0)put(image,x,y,0x41473a);
        for(int x=8;x<16;x++)put(image,x,8,0x696b54);for(int x=9;x<15;x++)put(image,x,9,x%2==0?0x67634e:0x454b3d);
        for(int x:new int[]{9,13})for(int y=10;y<=11;y++){put(image,x,y,0x353b32);put(image,x+1,y,0x353b32);}
        put(image,9,10,0xd0d4bd);put(image,10,10,0xaeb6a2);put(image,13,10,0xc8cdbb);put(image,14,10,0xd6d6c3);
        put(image,11,11,0x595d47);put(image,12,12,0x514e3b);put(image,10,12,0x4d4638);put(image,14,12,0x6a5245);
        for(int x=9;x<=14;x++)for(int y=13;y<=15;y++)put(image,x,y,0x322d28);for(int x:new int[]{9,11,14})put(image,x,13,0xb5ab83);put(image,13,14,0x887853);
        for(int y=37;y<=39;y++)for(int x=5;x<=11;x++)put(image,x,y,0x302a27);for(int x:new int[]{5,8,10})put(image,x,37,0xc1b694);
        for(int y=0;y<12;y++)for(int x=48;x<56;x++)put(image,x,y,(x+y)%4==0?0x77785d:0x383f32);
        var dynamic=new DynamicTexture(image);dynamic.setFilter(false,false);texture=Minecraft.getInstance().getTextureManager().register("hotel_bath_creature",dynamic);return texture;
    }
    private static void put(NativeImage image,int x,int y,int rgb){if(x>=0&&x<64&&y>=0&&y<64)image.setPixelRGBA(x,y,0xff000000|(rgb&255)<<16|(rgb&0xff00)|(rgb>>16&255));}
    public static final class Model extends EntityModel<HotelActor>{
        private final ModelPart root,head,jaw,body,leftArm,rightArm,leftElbow,rightElbow,leftLeg,rightLeg,leftKnee,rightKnee;private int phase,age;
        public Model(ModelPart r){root=r;head=r.getChild("head");jaw=head.getChild("jaw");body=r.getChild("body");leftArm=r.getChild("left_arm");rightArm=r.getChild("right_arm");leftElbow=leftArm.getChild("elbow");rightElbow=rightArm.getChild("elbow");leftLeg=r.getChild("left_leg");rightLeg=r.getChild("right_leg");leftKnee=leftLeg.getChild("knee");rightKnee=rightLeg.getChild("knee");}
        public static LayerDefinition layer(){
            var mesh=new MeshDefinition();var r=mesh.getRoot();
            var head=r.addOrReplaceChild("head",CubeListBuilder.create().texOffs(0,0).addBox(-4,-8,-4,8,8,8),PartPose.offset(0,4,-2));
            head.addOrReplaceChild("jaw",CubeListBuilder.create().texOffs(0,32).addBox(-3.5F,0,-4,7,3,5),PartPose.offset(0,-1,0));
            for(int i=0;i<5;i++)head.addOrReplaceChild("hair"+i,CubeListBuilder.create().texOffs(48,0).addBox(-.25F,0,-.25F,.5F,7+(i%2)*2,.5F),PartPose.offset((i-2)*1.6F,-6,i%2==0?3.8F:-3.7F));
            var body=r.addOrReplaceChild("body",CubeListBuilder.create().texOffs(16,16).addBox(-3,0,-1.5F,6,10,3).texOffs(16,33).addBox(-3,10,-2,6,4,4),PartPose.offset(0,4,0));
            for(int i=0;i<3;i++)body.addOrReplaceChild("rib"+i,CubeListBuilder.create().texOffs(32,40).addBox(-2.8F,0,-.3F,5.6F,.6F,.6F),PartPose.offset(0,3+i*2,-1.65F));
            for(boolean left:new boolean[]{true,false}){
                var arm=r.addOrReplaceChild(left?"left_arm":"right_arm",CubeListBuilder.create().texOffs(40,16).addBox(-1,-1,-1,2,6,2),PartPose.offset(left?4:-4,5,0));
                var elbow=arm.addOrReplaceChild("elbow",CubeListBuilder.create().texOffs(40,26).addBox(-1,0,-1,2,7,2).texOffs(40,36).addBox(-1.4F,6,-1.4F,2.8F,2.5F,2),PartPose.offset(0,5,0));
                for(int i=0;i<3;i++)elbow.addOrReplaceChild("finger"+i,CubeListBuilder.create().texOffs(40,42).addBox(-.3F,0,-.3F,.6F,3.8F,.6F),PartPose.offset(-.95F+i*.95F,8,-1.1F));
                var leg=r.addOrReplaceChild(left?"left_leg":"right_leg",CubeListBuilder.create().texOffs(0,16).addBox(-1.25F,0,-1.25F,2.5F,5,2.5F),PartPose.offset(left?1.7F:-1.7F,14,0));
                leg.addOrReplaceChild("knee",CubeListBuilder.create().texOffs(0,24).addBox(-1,0,-1,2,5,2),PartPose.offset(0,5,0));
            }return LayerDefinition.create(mesh,64,64);
        }
        public void pose(int phase,int age,float time,float walk,float amount){
            root.getAllParts().forEach(ModelPart::resetPose);this.phase=phase;this.age=age;
            body.xRot=.25F;head.xRot=.14F;head.zRot=.1F;head.z=-2.5F;jaw.xRot=.2F+.055F*Mth.sin(time*.12F);
            leftArm.xRot=-1.18F;rightArm.xRot=-1.45F;leftArm.yRot=.17F;rightArm.yRot=-.18F;leftElbow.xRot=-.35F;rightElbow.xRot=.12F;
            float drag=Mth.cos(walk*.6662F)*Math.min(.35F,amount);leftLeg.xRot=.1F+drag;rightLeg.xRot=.12F-drag;leftKnee.xRot=.18F+Math.max(0,-drag);rightKnee.xRot=.14F+Math.max(0,drag);
            for(int i=0;i<3;i++){leftElbow.getChild("finger"+i).xRot=-.35F-.18F*Mth.sin(time*.09F+i);rightElbow.getChild("finger"+i).xRot=-.45F-.22F*Mth.sin(time*.08F+i);}
            if(phase==HotelBathHaunt.RISING){body.xRot=.4F;head.xRot=.3F;leftLeg.xRot=rightLeg.xRot=-.7F;leftKnee.xRot=rightKnee.xRot=1;leftArm.xRot=-.7F;rightArm.xRot=-1.1F;}
            if(phase==HotelBathHaunt.FADING){body.xRot+=.2F;head.xRot+=.25F;}
        }
        @Override public void setupAnim(HotelActor a,float walk,float amount,float time,float yaw,float pitch){pose(a.bathPhase(),a.bathAge(),time,walk,amount);}
        @Override public void renderToBuffer(PoseStack p,VertexConsumer out,int light,int overlay,int color){int alpha=phase==HotelBathHaunt.FADING?Math.max(0,(int)(255*(1-age/(float)HotelBathHaunt.FADE_TICKS))):255;root.render(p,out,light,overlay,(color&0xffffff)|alpha<<24);}
    }
}
