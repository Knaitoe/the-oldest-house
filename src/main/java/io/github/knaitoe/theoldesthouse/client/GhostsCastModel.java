package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
    private final ModelPart rightElbow,leftElbow,rightKnee,leftKnee,jaw,lids;
    private int currentRole,currentPhase;
    private float blink;
    private final java.util.List<ModelPart> joints;
    private final java.util.WeakHashMap<LiteraryActor,Tween> transitions=new java.util.WeakHashMap<>();
    private static final class Tween {int phase;float started;float[] from,last;Tween(int phase){this.phase=phase;}}
    public GhostsCastModel(ModelPart root){super(root);rightElbow=rightArm.getChild("elbow");leftElbow=leftArm.getChild("elbow");rightKnee=rightLeg.getChild("knee");leftKnee=leftLeg.getChild("knee");jaw=head.getChild("jaw");lids=head.getChild("lids");joints=root.getAllParts().toList();}
    public static LayerDefinition layer(){
        var mesh=HumanoidModel.createMesh(CubeDeformation.NONE,0);var r=mesh.getRoot();
        for(boolean left:new boolean[]{false,true}){
            var arm=r.addOrReplaceChild(left?"left_arm":"right_arm",CubeListBuilder.create().texOffs(40,16).mirror(left).addBox(left?-1:-3,-2,-2,4,6,4),PartPose.offset(left?5:-5,2,0));
            arm.addOrReplaceChild("elbow",CubeListBuilder.create().texOffs(40,22).mirror(left).addBox(left?-1:-3,0,-2,4,6,4),PartPose.offset(0,4,0));
            var leg=r.addOrReplaceChild(left?"left_leg":"right_leg",CubeListBuilder.create().texOffs(0,16).mirror(left).addBox(-2,0,-2,4,6,4),PartPose.offset(left?1.9F:-1.9F,12,0));
            leg.addOrReplaceChild("knee",CubeListBuilder.create().texOffs(0,22).mirror(left).addBox(-2,0,-2,4,6,4),PartPose.offset(0,6,0));
        }
        // Explicit face UVs keep the opened jaw's inner surface out of the eye/forehead island.
        var head=r.addOrReplaceChild("head",CubeListBuilder.create(),PartPose.ZERO);
        head.addOrReplaceChild("jaw",CubeListBuilder.create(),PartPose.offset(0,-2,0));
        head.addOrReplaceChild("lids",CubeListBuilder.create(),PartPose.ZERO);
        return LayerDefinition.create(mesh,128,64);
    }
    @Override public void setupAnim(LiteraryActor a,float walk,float amount,float age,float yaw,float pitch){
        float partial=age-a.tickCount;
        age=GhostsTakeClient.poseAge(a,age);
        for(var part:new ModelPart[]{head,hat,body,rightArm,leftArm,rightLeg,leftLeg})part.getAllParts().forEach(ModelPart::resetPose);
        currentRole=a.role();crouching=false;riding=false;super.setupAnim(a,walk,amount,age,yaw,pitch);hat.visible=false;jaw.visible=true;
        var motion=HumanoidMotion.sample(a,partial);float step=(float)Math.sin(motion.phase());
        if(a.phase()==GhostsSet.STAND)HumanoidMotion.gait(this,motion.phase(),motion.amount(),motion.running());
        pose(a.phase(),a.getScale(),step,age);
        blend(a,age);
    }
    private float[] snapshot(){var values=new float[joints.size()*6];int i=0;for(var p:joints){values[i++]=p.x;values[i++]=p.y;values[i++]=p.z;values[i++]=p.xRot;values[i++]=p.yRot;values[i++]=p.zRot;}return values;}
    private void blend(LiteraryActor actor,float age){
        var state=transitions.computeIfAbsent(actor,a->new Tween(a.phase()));
        // A newly visible apex must freeze in its complete leap, even if its earlier phase was last rendered off-camera.
        if(GhostsTakeClient.frozen()&&actor.phase()==GhostsSet.LEAP){state.phase=actor.phase();state.from=null;state.last=snapshot();return;}
        if(state.phase!=actor.phase()){state.from=state.last;state.started=age;state.phase=actor.phase();}
        if(state.from!=null){float t=net.minecraft.util.Mth.clamp((age-state.started)/(actor.phase()==GhostsSet.LEAP?5F:8F),0,1);t=t*t*(3-2*t);int i=0;
            for(var p:joints){p.x=net.minecraft.util.Mth.lerp(t,state.from[i++],p.x);p.y=net.minecraft.util.Mth.lerp(t,state.from[i++],p.y);p.z=net.minecraft.util.Mth.lerp(t,state.from[i++],p.z);p.xRot=angle(state.from[i++],p.xRot,t);p.yRot=angle(state.from[i++],p.yRot,t);p.zRot=angle(state.from[i++],p.zRot,t);}if(t==1)state.from=null;
        }state.last=snapshot();
    }
    private static float angle(float from,float to,float t){return from+net.minecraft.util.Mth.wrapDegrees((to-from)*net.minecraft.util.Mth.RAD_TO_DEG)*net.minecraft.util.Mth.DEG_TO_RAD*t;}
    public void preview(int role,int phase,float age){for(var part:new ModelPart[]{head,hat,body,rightArm,leftArm,rightLeg,leftLeg})part.getAllParts().forEach(ModelPart::resetPose);currentRole=role;young=false;crouching=false;riding=false;hat.visible=false;jaw.visible=true;pose(phase,role==LiteraryActor.GHOST_MERRY?.58F:role==LiteraryActor.GHOST_MARJORIE?.86F:1,(float)Math.sin(age*.08F),age);}
    @Override public void renderToBuffer(PoseStack p,VertexConsumer out,int light,int overlay,int colour){
        var fitted=GhostsSkinMesh.fit(out,currentRole,currentPhase);
        super.renderToBuffer(p,fitted,light,overlay,colour);
        GhostsSkinMesh.head(head,jaw,blink,p,fitted,light,overlay,colour);
    }
    private void seatLegs(){
        for(var part:new ModelPart[]{head,hat,body,leftArm,rightArm,leftLeg,rightLeg})part.y+=12;
        leftLeg.xRot=rightLeg.xRot=-(float)Math.PI/2;leftKnee.xRot=rightKnee.xRot=(float)Math.PI/2;
    }
    private void pose(int phase,float scale,float step,float age){
        currentPhase=phase;float cycle=(age+currentRole*13)%93;
        blink=phase==GhostsSet.DEAD||phase==GhostsSet.PRAY?1:phase!=GhostsSet.LEAP&&cycle<6?(float)Math.sin(Math.PI*cycle/6):0;lids.visible=blink>0;
        switch(phase){
            case GhostsSet.SIT,GhostsSet.SLUMP,GhostsSet.VOMIT,GhostsSet.HAND,GhostsSet.EATING,GhostsSet.CHOKING,GhostsSet.DEAD->{
                seatLegs();leftArm.xRot=-.7F;rightArm.xRot=-.8F;leftElbow.xRot=-.65F;rightElbow.xRot=-.6F;
                if(phase==GhostsSet.HAND){rightArm.xRot=-1.25F;rightElbow.xRot=-1.4F;head.xRot=.35F;head.yRot=-.3F;}
                if(phase==GhostsSet.SLUMP){body.xRot=.52F;head.xRot=.85F;head.zRot=.17F;leftArm.xRot=rightArm.xRot=-.1F;leftElbow.xRot=rightElbow.xRot=.2F;}
                if(phase==GhostsSet.EATING){float chew=(float)Math.sin(age*.23F);head.xRot=.12F+chew*.025F;head.yRot=.2F;rightArm.xRot=-1.04F;rightElbow.xRot=-1.65F+chew*.08F;jaw.xRot=.04F+Math.max(0,chew)*.16F;}
                if(phase==GhostsSet.CHOKING){float rate=currentRole==LiteraryActor.GHOST_JOHN?.33F:currentRole==LiteraryActor.GHOST_SARAH?.75F:.48F;float catchBreath=(float)Math.sin(age*rate+currentRole);body.xRot=.3F+catchBreath*.065F;body.zRot=(float)Math.sin(age*rate*.69F)*.045F;head.xRot=.27F-catchBreath*.08F;head.zRot=(float)Math.sin(age*rate*.78F)*.06F;rightArm.xRot=-1.18F;rightElbow.xRot=-1.95F;leftArm.xRot=-.5F;leftElbow.xRot=-.95F;jaw.xRot=.28F+Math.max(0,catchBreath)*.07F;if(currentRole==LiteraryActor.GHOST_JOHN){body.xRot=-.12F+catchBreath*.12F;head.xRot=-.45F;leftArm.xRot=-1.1F;leftElbow.xRot=-1.8F;}if(currentRole==LiteraryActor.GHOST_SARAH){body.zRot=.23F+catchBreath*.09F;leftArm.xRot=-1.2F;rightArm.xRot=-.4F;}}
                if(phase==GhostsSet.DEAD){body.xRot=.66F;body.zRot=.12F;head.xRot=.93F;head.zRot=.24F;leftArm.xRot=rightArm.xRot=-.12F;leftArm.zRot=rightArm.zRot=0;leftElbow.xRot=rightElbow.xRot=.13F;leftKnee.xRot=1.36F;rightKnee.xRot=1.48F;jaw.xRot=.32F;}
                if(phase==GhostsSet.DEAD&&currentRole==LiteraryActor.GHOST_JOHN){body.xRot=-.3F;body.zRot=-.12F;head.xRot=-.55F;leftLeg.yRot=.18F;rightArm.zRot=.32F;}
                if(phase==GhostsSet.DEAD&&currentRole==LiteraryActor.GHOST_SARAH){body.xRot=.24F;body.zRot=.38F;head.xRot=.65F;head.zRot=.45F;leftArm.zRot=-.2F;rightArm.xRot=-.65F;}
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
            case GhostsSet.LEAP->{body.xRot=-.12F;leftArm.xRot=-1.05F;rightArm.xRot=-.85F;leftArm.zRot=-.05F;rightArm.zRot=.05F;leftElbow.xRot=-.45F;rightElbow.xRot=-.65F;leftLeg.xRot=-.55F;rightLeg.xRot=.2F;leftKnee.xRot=.95F;rightKnee.xRot=.75F;}
            case GhostsSet.PRAY->{if(currentRole==LiteraryActor.GHOST_MERRY)seatLegs();head.xRot=.35F;leftArm.xRot=rightArm.xRot=-.65F;leftArm.yRot=.67F;rightArm.yRot=-.67F;leftArm.zRot=rightArm.zRot=0;leftElbow.xRot=rightElbow.xRot=-.8F;}
            case GhostsSet.HELP->{head.xRot=-.35F;body.xRot=-.14F;leftArm.xRot=-2.25F;rightArm.xRot=-2.4F;leftElbow.xRot=-.4F;rightElbow.xRot=-.45F;}
            case GhostsSet.FALL->{body.xRot=.16F;head.xRot=-.2F;leftArm.zRot=-.8F;rightArm.zRot=.8F;leftLeg.xRot=-.18F;rightLeg.xRot=.15F;leftKnee.xRot=.6F;rightKnee.xRot=.5F;}
            case GhostsSet.IMPACT->{body.xRot=.45F;head.xRot=.2F;leftLeg.xRot=rightLeg.xRot=-.7F;leftKnee.xRot=rightKnee.xRot=1.4F;leftArm.xRot=-.4F;rightArm.xRot=-.5F;}
            default->{leftElbow.xRot=-.12F;rightElbow.xRot=-.12F;}
        }
        if(phase!=GhostsSet.CRAWL)HumanoidMotion.connect(this);hat.copyFrom(head);
    }
    public static ResourceLocation skin(LiteraryActor a){return skin(a.role(),a.phase());}
    public static ResourceLocation skin(int role,int phase){String name=GhostsSet.PEOPLE[Math.max(0,Math.min(5,role-LiteraryActor.GHOST_MARJORIE))].toLowerCase(java.util.Locale.ROOT).replace(' ','_');if(role==LiteraryActor.GHOST_MARJORIE&&(phase==GhostsSet.VOMIT||phase==GhostsSet.EATING||phase==GhostsSet.CHOKING||phase==GhostsSet.DEAD))name+="_dinner";if(role==LiteraryActor.GHOST_JOHN&&(phase==GhostsSet.FLINCH||phase==GhostsSet.HAND))name+="_hand";return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/ghosts_"+name+".png");}
    public static final class Renderer extends MobRenderer<LiteraryActor,GhostsCastModel>{
        public Renderer(EntityRendererProvider.Context c){super(c,new GhostsCastModel(c.bakeLayer(LAYER)),.2F);addLayer(new GhostsMouthLayer(this));}
        @Override public ResourceLocation getTextureLocation(LiteraryActor a){return skin(a);}
        @Override protected void setupRotations(LiteraryActor a,PoseStack p,float age,float yaw,float partial,float scale){super.setupRotations(a,p,age,yaw,partial,scale);if(a.phase()==GhostsSet.BED)LiteraryRenderers.bedPose(p,1);}
        @Override public void render(LiteraryActor a,float yaw,float partial,PoseStack p,MultiBufferSource out,int light){super.render(a,yaw,partial,p,out,light);}
    }
}
