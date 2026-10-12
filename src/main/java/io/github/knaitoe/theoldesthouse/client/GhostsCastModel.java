package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;

/** Connected elbows, knees and jaw on the six domestic characters; each pose remains a possible human movement. */
public final class GhostsCastModel extends HumanoidModel<LiteraryActor> {
    public static final ModelLayerLocation LAYER=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"ghosts_cast"),"main");
    private final ModelPart rightElbow,leftElbow,rightKnee,leftKnee,jaw;
    public GhostsCastModel(ModelPart root){super(root);rightElbow=rightArm.getChild("elbow");leftElbow=leftArm.getChild("elbow");rightKnee=rightLeg.getChild("knee");leftKnee=leftLeg.getChild("knee");jaw=head.getChild("jaw");}
    public static LayerDefinition layer(){
        var mesh=HumanoidModel.createMesh(CubeDeformation.NONE,0);var r=mesh.getRoot();
        for(boolean left:new boolean[]{false,true}){
            var arm=r.addOrReplaceChild(left?"left_arm":"right_arm",CubeListBuilder.create().texOffs(40,16).mirror(left).addBox(left?-1:-3,-2,-2,4,6,4),PartPose.offset(left?5:-5,2,0));
            arm.addOrReplaceChild("elbow",CubeListBuilder.create().texOffs(40,22).mirror(left).addBox(left?-1:-3,0,-2,4,6,4),PartPose.offset(0,4,0));
            var leg=r.addOrReplaceChild(left?"left_leg":"right_leg",CubeListBuilder.create().texOffs(0,16).mirror(left).addBox(-2,0,-2,4,6,4),PartPose.offset(left?1.9F:-1.9F,12,0));
            leg.addOrReplaceChild("knee",CubeListBuilder.create().texOffs(0,22).mirror(left).addBox(-2,0,-2,4,6,4),PartPose.offset(0,6,0));
        }
        r.getChild("head").addOrReplaceChild("jaw",CubeListBuilder.create().texOffs(64,0).addBox(-2,0,-1,4,1,1),PartPose.offset(0,-2,-3.2F));
        return LayerDefinition.create(mesh,128,64);
    }
    @Override public void setupAnim(LiteraryActor a,float walk,float amount,float age,float yaw,float pitch){
        for(var part:new ModelPart[]{head,hat,body,rightArm,leftArm,rightLeg,leftLeg})part.getAllParts().forEach(ModelPart::resetPose);
        crouching=false;riding=false;super.setupAnim(a,walk,amount,age,yaw,pitch);hat.copyFrom(head);jaw.visible=a.role()==LiteraryActor.GHOST_MARJORIE;
        var motion=HumanoidMotion.sample(a,age-a.tickCount);float step=(float)Math.sin(motion.phase());
        if(a.phase()==GhostsSet.STAND)HumanoidMotion.gait(this,motion.phase(),motion.amount(),motion.running());
        pose(a.phase(),a.getScale(),step,age);
    }
    public void preview(int role,int phase,float age){for(var part:new ModelPart[]{head,hat,body,rightArm,leftArm,rightLeg,leftLeg})part.getAllParts().forEach(ModelPart::resetPose);young=false;crouching=false;riding=false;jaw.visible=role==LiteraryActor.GHOST_MARJORIE;pose(phase,role==LiteraryActor.GHOST_MERRY?.58F:1,(float)Math.sin(age*.08F),age);}
    private void pose(int phase,float scale,float step,float age){
        switch(phase){
            case GhostsSet.SIT,GhostsSet.SLUMP,GhostsSet.VOMIT,GhostsSet.HAND->{
                float lower=12;for(var part:new ModelPart[]{head,hat,body,leftArm,rightArm,leftLeg,rightLeg})part.y+=lower;
                leftLeg.xRot=rightLeg.xRot=-1F;leftKnee.xRot=rightKnee.xRot=1F;leftArm.xRot=-.7F;rightArm.xRot=-.8F;leftElbow.xRot=-.65F;rightElbow.xRot=-.6F;
                if(phase==GhostsSet.HAND){rightArm.xRot=-1.25F;rightElbow.xRot=-1.4F;head.xRot=.35F;head.yRot=-.3F;}
                if(phase==GhostsSet.SLUMP){body.xRot=.52F;head.xRot=.85F;head.zRot=.17F;leftArm.xRot=rightArm.xRot=-.1F;leftElbow.xRot=rightElbow.xRot=.2F;}
                // The dinner is an unnaturally still stare and a slowly opening mouth, without a retching body loop.
                if(phase==GhostsSet.VOMIT){head.yRot=.55F;head.xRot=.03F;jaw.xRot=.25F+(float)Math.sin(age*.035F)*.025F;}
            }
            case GhostsSet.CROUCH->{body.xRot=.3F;head.xRot=.2F;leftLeg.xRot=rightLeg.xRot=-.5F;leftKnee.xRot=rightKnee.xRot=.8F;leftElbow.xRot=rightElbow.xRot=-.9F;}
            case GhostsSet.CRAWL->{
                body.y=17.8F;body.z=1;body.xRot=1.43F;head.y=21;head.z=-2.8F;head.xRot=-.1F;
                leftArm.y=rightArm.y=18.1F;leftArm.z=rightArm.z=3;leftArm.xRot=-.35F+step*.2F;rightArm.xRot=-.35F-step*.2F;
                leftElbow.xRot=-1.15F-step*.15F;rightElbow.xRot=-1.15F+step*.15F;
                leftLeg.y=rightLeg.y=19.5F;leftLeg.z=rightLeg.z=12.88F;leftLeg.xRot=-1.2F-step*.22F;rightLeg.xRot=-1.2F+step*.22F;leftKnee.xRot=2.77F+step*.22F;rightKnee.xRot=2.77F-step*.22F;
                head.yRot+=(float)Math.sin(age*.035F)*.07F;
            }
            case GhostsSet.WALL->{leftArm.xRot=-2.9F;rightArm.xRot=-2.65F;leftArm.zRot=-.3F;rightArm.zRot=.25F;leftElbow.xRot=-.55F;rightElbow.xRot=-.8F;leftLeg.xRot=-.8F;rightLeg.xRot=-.55F;leftKnee.xRot=1.2F;rightKnee.xRot=.95F;head.xRot=.3F;}
            case GhostsSet.BED->{leftElbow.xRot=rightElbow.xRot=-.3F;leftArm.zRot=-.3F;rightArm.zRot=.3F;leftKnee.xRot=.2F;rightKnee.xRot=.08F;head.yRot=.12F;}
            case GhostsSet.FLINCH->{body.zRot=.15F;head.xRot=-.2F;rightArm.xRot=-1.25F;rightElbow.xRot=-1.4F;leftArm.xRot=-.7F;leftElbow.xRot=-1.1F;rightKnee.xRot=.35F;}
            case GhostsSet.LEAP->{body.xRot=-.12F;leftArm.xRot=-1.05F;rightArm.xRot=-.85F;leftElbow.xRot=-.45F;rightElbow.xRot=-.65F;leftLeg.xRot=-.55F;rightLeg.xRot=.2F;leftKnee.xRot=.95F;rightKnee.xRot=.75F;}
            default->{leftElbow.xRot=-.12F;rightElbow.xRot=-.12F;}
        }
        if(phase!=GhostsSet.CRAWL)HumanoidMotion.connect(this);hat.copyFrom(head);
    }
    public static ResourceLocation skin(LiteraryActor a){return skin(a.role(),a.phase());}
    public static ResourceLocation skin(int role,int phase){String name=GhostsSet.PEOPLE[Math.max(0,Math.min(5,role-LiteraryActor.GHOST_MARJORIE))].toLowerCase(java.util.Locale.ROOT).replace(' ','_');if(role==LiteraryActor.GHOST_MARJORIE&&phase==GhostsSet.VOMIT)name+="_dinner";if(role==LiteraryActor.GHOST_JOHN&&(phase==GhostsSet.FLINCH||phase==GhostsSet.HAND))name+="_hand";return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/ghosts_"+name+".png");}
    public static final class Renderer extends MobRenderer<LiteraryActor,GhostsCastModel>{
        public Renderer(EntityRendererProvider.Context c){super(c,new GhostsCastModel(c.bakeLayer(LAYER)),.2F);}
        @Override public ResourceLocation getTextureLocation(LiteraryActor a){return skin(a);}
        @Override protected void setupRotations(LiteraryActor a,PoseStack p,float age,float yaw,float partial,float scale){super.setupRotations(a,p,age,yaw,partial,scale);if(a.phase()==GhostsSet.BED)LiteraryRenderers.bedPose(p,1);}
        @Override public void render(LiteraryActor a,float yaw,float partial,PoseStack p,MultiBufferSource out,int light){super.render(a,yaw,partial,p,out,light);}
    }
}
