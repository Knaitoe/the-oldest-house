package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.*;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** The same saved actors in direct sight, the owner's live CCTV, and the blood-fuelled mirror. */
public final class UncannyActorRenderer extends MobRenderer<LiteraryActor,UncannyActorRenderer.Model> {
    public static final ModelLayerLocation WIDE=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("the_oldest_house","uncanny_human"),"main");
    public static final ModelLayerLocation SLIM=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("the_oldest_house","uncanny_human_slim"),"main");
    public static final ModelLayerLocation BENT=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("the_oldest_house","uncanny_bent_body"),"main");
    public static LayerDefinition layer(boolean slim){
        return layer(slim,false);
    }
    public static LayerDefinition bentLayer(){return layer(false,true);}
    private static LayerDefinition layer(boolean slim,boolean bent){
        var mesh=PlayerModel.createMesh(CubeDeformation.NONE,slim);
        mesh.getRoot().addOrReplaceChild("bent_neck",CubeListBuilder.create().texOffs(0,32).addBox(-1,-2,-1,2,4,2),PartPose.offsetAndRotation(1.1F,8.3F,-2.2F,0,0,-.75F));
        if(bent)for(boolean left:new boolean[]{false,true}){var leg=mesh.getRoot().addOrReplaceChild(left?"left_leg":"right_leg",SegmentedSkin.limb(left?16:0,left?48:16,0,-2,0,-2,4,6,4,left,false),PartPose.offset(left?1.9F:-1.9F,12,0));leg.addOrReplaceChild("bent_knee",SegmentedSkin.limb(left?16:0,left?48:16,6,-2,0,-2,4,6,4,left,false),PartPose.offset(0,6,0));}
        return LayerDefinition.create(mesh,64,64);
    }
    private static final java.util.WeakHashMap<LiteraryActor,Integer> proofRenders=new java.util.WeakHashMap<>();
    public static boolean proofDrawn(LiteraryActor a){return a!=null&&a.tickCount-proofRenders.getOrDefault(a,-1000)<=3;}
    private final Model wide,slim,bent;
    public UncannyActorRenderer(EntityRendererProvider.Context c){
        super(c,new Model(c.bakeLayer(WIDE),false),.25F);wide=model;slim=new Model(c.bakeLayer(SLIM),true);bent=new Model(c.bakeLayer(BENT),false);
        addLayer(new net.minecraft.client.renderer.entity.layers.ItemInHandLayer<>(this,c.getItemInHandRenderer()));
    }
    public static boolean visible(LiteraryActor a){
        var mc=Minecraft.getInstance();if(a.owner().isPresent()&&(mc.player==null||!a.owner().get().equals(mc.player.getUUID())))return false;
        if(a.role()==LiteraryActor.UNCANNY_WAITRESS||a.role()==LiteraryActor.UNCANNY_PEELER)return true;
        if(a.role()==LiteraryActor.UNCANNY_GIANT&&a.phase()==0)return false;
        if(UncannyClient.mirror()||UncannyClient.outline(a))return true;if(a.phase()==0)return false;
        if(a.role()!=LiteraryActor.UNCANNY_PROPPED)return true;
        var camera=UncannyCameras.recording()?UncannyCameras.camera():mc.gameRenderer.getMainCamera();var delta=a.getEyePosition().subtract(camera.getPosition());var look=camera.getLookVector();
        double denom=Math.hypot(delta.x,delta.z)*Math.hypot(look.x(),look.z());if(denom<.00001)return false;
        double angle=Math.acos(Mth.clamp((delta.x*look.x()+delta.z*look.z())/denom,-1,1));double half=Math.atan(Math.tan(Math.toRadians(UncannyClient.fov()*.5))*mc.getWindow().getWidth()/mc.getWindow().getHeight());
        return angle>=half*.52&&angle<=half*1.04;
    }
    @Override public void render(LiteraryActor a,float yaw,float partial,PoseStack poses,MultiBufferSource buffers,int light){
        if(!visible(a)||UncannyCameras.maskDoll()&&a.getId()==UncannyClient.doll())return;
        var mc=Minecraft.getInstance();boolean feed=a.role()==LiteraryActor.UNCANNY_MARIONETTE&&UncannyCameras.recording()&&!UncannyClient.mirror();
        model=feed&&mc.player!=null&&mc.player.getSkin().model()==net.minecraft.client.resources.PlayerSkin.Model.SLIM?slim:a.role()==LiteraryActor.UNCANNY_GIANT?bent:wide;
        model.feed=feed;
        if(a.role()==LiteraryActor.UNCANNY_FALSE_WAITRESS&&a.phase()<2){
            if(a.phase()==0&&!UncannyClient.mirror()&&!UncannyClient.outline(a))return;
            poses.pushPose();setupRotations(a,poses,a.tickCount+partial,yaw,partial,a.getScale());poses.scale(-1,-1,1);poses.translate(0,-1.501,0);
            model.pose(a.role(),a.phase(),false,a.tickCount+partial,0,0,0,0);
            RecentActorMotion.blend(a,a.tickCount+partial,model);
            model.renderToBuffer(poses,buffers.getBuffer(RenderType.entityTranslucent(getTextureLocation(a))),light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,UncannyClient.mirror()?0xb0ffffff:0x50ffffff);poses.popPose();return;
        }
        super.render(a,yaw,partial,poses,buffers,light);
        if(io.github.knaitoe.theoldesthouse.gametest.UncannySocketProof.enabled()&&!UncannyCameras.recording())proofRenders.put(a,a.tickCount);
        if(feed)UncannyCameras.dollDrawn(a);
    }
    @Override public ResourceLocation getTextureLocation(LiteraryActor a){
        var p=Minecraft.getInstance().player;return model.feed&&p!=null?p.getSkin().texture():UncannyTextures.skin(a.role(),a.phase());
    }
    @Override protected RenderType getRenderType(LiteraryActor a,boolean body,boolean translucent,boolean glowing){
        if(a.role()==LiteraryActor.UNCANNY_FALSE_WAITRESS&&a.phase()<2)return RenderType.entityTranslucent(getTextureLocation(a));
        return super.getRenderType(a,body,translucent,glowing);
    }
    @Override protected boolean shouldShowName(LiteraryActor a){return false;}
    @Override public boolean shouldRender(LiteraryActor a,net.minecraft.client.renderer.culling.Frustum f,double x,double y,double z){if(!visible(a))return false;return a.role()==LiteraryActor.UNCANNY_GIANT?a.shouldRender(x,y,z)&&f.isVisible(a.getBoundingBox().inflate(2,1,3)):super.shouldRender(a,f,x,y,z);}
    public static final class Model extends PlayerModel<LiteraryActor> {
        boolean feed;public final ModelPart neck;
        public Model(ModelPart part,boolean slim){super(part,slim);neck=part.hasChild("bent_neck")?part.getChild("bent_neck"):null;}
        @Override protected Iterable<ModelPart> bodyParts(){return neck==null?super.bodyParts():com.google.common.collect.Iterables.concat(super.bodyParts(),java.util.List.of(neck));}
        @Override public void setupAnim(LiteraryActor a,float walk,float amount,float age,float yaw,float pitch){boolean great=a.role()==LiteraryActor.UNCANNY_GIANT&&a.gesture()==1;if(a.role()==LiteraryActor.UNCANNY_GIANT){var motion=HumanoidMotion.sample(a,age-a.tickCount);walk=motion.phase()/(great?6F:2.5F);amount=motion.amount();}pose(a.role(),a.phase(),feed,age,walk,amount,yaw,pitch,great);if(!feed&&a.role()!=LiteraryActor.UNCANNY_WAITRESS)RecentActorMotion.blend(a,age,this);}
        public void pose(int role,int phase,boolean feed,float age,float walk,float amount,float yaw,float pitch){
            pose(role,phase,feed,age,walk,amount,yaw,pitch,false);
        }
        public void pose(int role,int phase,boolean feed,float age,float walk,float amount,float yaw,float pitch,boolean great){
            for(var p:new ModelPart[]{head,hat,body,leftArm,rightArm,leftLeg,rightLeg}){p.getAllParts().forEach(ModelPart::resetPose);p.xScale=p.yScale=p.zScale=1;}
            if(neck!=null)neck.visible=false;
            young=false;attackTime=0;crouching=false;riding=false;
            head.yRot=yaw*Mth.DEG_TO_RAD;head.xRot=pitch*Mth.DEG_TO_RAD;
            float step=Mth.cos(walk*.6662F)*Math.min(.65F,amount*1.4F);leftLeg.xRot=-step;rightLeg.xRot=step;leftArm.xRot=step*.5F;rightArm.xRot=-step*.5F;
            if(role==LiteraryActor.UNCANNY_PROPPED){body.zRot=.08F;head.zRot=-.16F;leftArm.zRot=-.12F;rightArm.zRot=.12F;leftLeg.xRot=rightLeg.xRot=0;}
            if(role==LiteraryActor.UNCANNY_WAITRESS){head.xRot+=.025F;body.xRot=0;leftArm.xRot=step*.7F;rightArm.xRot=-step*.7F;}
            if(role==LiteraryActor.UNCANNY_FALSE_WAITRESS){leftArm.xRot=rightArm.xRot=phase>=3?-1.1F:-.15F;head.zRot=.025F*Mth.sin(age*.04F);}
            if(role==LiteraryActor.UNCANNY_GIANT){
                // Upright ribcage and dragging legs; the break is in her neck.
                // The folded head stays within the actual 2.7-block tunnel body.
                float gait=phase==2?Mth.sin(walk)*Math.min(1,amount*2):0,bob=Mth.abs(gait)*(great?.2F:.12F);
                body.y=7+bob;body.yScale=.6F;body.xRot=.12F;body.zRot=gait*.04F;
                body.xScale=.42F;body.zScale=.6F;head.xScale=.58F;head.yScale=.9F;head.zScale=.62F;
                leftLeg.y=rightLeg.y=14;leftLeg.yScale=rightLeg.yScale=5F/6;
                head.x=2.7F+gait*.1F;head.y=10.5F+bob;head.z=-2.4F;head.zRot=1.55F+gait*.025F;head.xRot=.12F;
                leftArm.y=rightArm.y=8+bob;leftArm.xRot=-gait*.22F;rightArm.xRot=-.35F+gait*.1F;
                leftArm.x=2.1F;rightArm.x=-2.1F;
                leftArm.xScale=rightArm.xScale=.45F;leftArm.zScale=rightArm.zScale=.55F;leftLeg.xScale=rightLeg.xScale=.5F;leftLeg.zScale=rightLeg.zScale=.6F;
                leftArm.zRot=-.025F;rightArm.zRot=.025F;
                leftLeg.xRot=gait*(great?.68F:.4F);rightLeg.xRot=-gait*(great?.48F:.24F)-.06F;
                if(leftLeg.hasChild("bent_knee")){leftLeg.getChild("bent_knee").xRot=.08F+Math.max(0,-gait)*.35F;rightLeg.getChild("bent_knee").xRot=.22F+Math.max(0,gait)*.45F;}
                if(neck!=null){neck.visible=true;neck.y=8.3F+bob;neck.zRot=-.75F+gait*.025F;}
            }
            if(role==LiteraryActor.UNCANNY_MARIONETTE&&!feed){
                float twitch=Mth.sin(age*.7F)*.045F;leftArm.zRot=-(float)Math.PI/2;rightArm.zRot=(float)Math.PI/2;
                head.zRot=twitch;head.xRot=.1F+twitch;body.y=-.45F;leftLeg.xRot=-.08F;rightLeg.xRot=.08F;
            }
            if(role==LiteraryActor.UNCANNY_MARIONETTE&&feed){head.xRot=0;head.yRot=0;head.zRot=0;body.yRot=.04F*Mth.sin(age*.07F);}
            if(role==LiteraryActor.UNCANNY_PEELER){float breath=Mth.sin(age*(phase>=1?.17F:.09F));body.xRot=.27F+breath*(phase>=1?.045F:.015F);head.xRot=.24F+breath*.025F;if(phase<3){rightArm.xRot=phase>=1?-1.4F+Mth.sin(age*.21F)*.12F:-.8F-Mth.abs(Mth.sin(age*.27F));leftArm.xRot=-.65F;leftArm.zRot=phase>=1?Mth.sin(age*.71F)*.025F:0;}}
            if(role==LiteraryActor.UNCANNY_VISITOR){leftLeg.xRot=phase>0?0:-step*.35F;rightLeg.xRot=phase>0?0:step*.35F;leftArm.xRot=phase>0?0:step*.08F;rightArm.xRot=phase>=3?-1.4F:phase>0?0:-step*.06F;rightArm.zRot=phase>=3?.08F:0;rightArm.z=phase>=3?1.6F:0;head.xRot=phase>=2?.12F:-.08F;body.xRot=phase>0?0:.015F*Mth.sin(age*.04F);}
            hat.copyFrom(head);jacket.copyFrom(body);leftSleeve.copyFrom(leftArm);rightSleeve.copyFrom(rightArm);leftPants.copyFrom(leftLeg);rightPants.copyFrom(rightLeg);
            hat.visible=feed||role==LiteraryActor.UNCANNY_WAITRESS||role==LiteraryActor.UNCANNY_FALSE_WAITRESS;
            jacket.visible=leftSleeve.visible=rightSleeve.visible=leftPants.visible=rightPants.visible=feed;
        }
    }
}
