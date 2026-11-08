package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryActor;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;

/**
 * 0.4.274, Second Person: the resident, drawn in the viewing reader's own House body with their coat, glasses and hair (only the reader
 * whose flat it is ever sees them). Their gesture is their pose: 0 standing or walking, 1 sitting in a chair, 3 the phone at their
 * ear, 4 a camera held up to the eye, 5 sitting on something higher (a box, the bed's edge), 6 carrying something in both arms, 7 arms
 * folded against the cold. Asleep they lie in the bed (the native sleeping pose).
 * <p>
 * 0.4.370 (the animation review): 8 looking down at the phone in their hand; 9 walking with a bag in one hand; 10 knitting in a chair;
 * 11 walking asleep, slow, arms a little forward and head down; 12 a hand out to the latch. Carrying (6) now has the moving box in their
 * arms, drawn by {@link Held}; what they hold in a hand (the landline's receiver, their phone, the bag, Iris's sleeve) is their real
 * main-hand item, set by the server.
 */
public final class SecondPersonRenderers {
    private SecondPersonRenderers(){}
    public static final int STAND=0,SIT=1,PHONE=3,CAMERA=4,PERCH=5,CARRY=6,COLD=7,LOOK=8,BAG=9,KNIT=10,DRIFT=11,REACH=12;
    static boolean seated(int g){return g==SIT||g==PERCH||g==KNIT;}
    static boolean walking(int g){return g==STAND||g==CARRY||g==BAG||g==DRIFT||g==REACH;}

    public static final class Model extends PlayerModel<LiteraryActor> {
        public Model(net.minecraft.client.model.geom.ModelPart part,boolean slim){super(part,slim);}
        @Override public void setupAnim(LiteraryActor a,float walk,float amount,float age,float yaw,float pitch){
            int g=a.gesture();riding=seated(g);crouching=false;
            // Asleep on their feet the stride is short.
            super.setupAnim(a,walking(g)?walk:0,walking(g)?(g==DRIFT?amount*.45F:amount):0,age,yaw,pitch);
            gesture(this,g,age);
        }
        /** The pose itself, after the native walking or riding pose: shared with SecondPersonPoseProof, which draws every gesture. */
        static void gesture(Model m,int g,float age){
            var rightArm=m.rightArm;var leftArm=m.leftArm;var head=m.head;
            float breath=Mth.sin(age*.05F)*.03F;
            switch(g){
                case PHONE->{rightArm.xRot=-2.85F;rightArm.yRot=-.1F;rightArm.zRot=.1F;head.zRot=-.12F;}
                case CAMERA->{rightArm.xRot=-1.62F;leftArm.xRot=-1.62F;rightArm.yRot=-.38F;leftArm.yRot=.38F;rightArm.zRot=0;leftArm.zRot=0;head.xRot=0;}
                case CARRY->{rightArm.xRot=-1.05F+breath;leftArm.xRot=-1.05F+breath;rightArm.yRot=-.22F;leftArm.yRot=.22F;rightArm.zRot=0;leftArm.zRot=0;}
                case COLD->{rightArm.xRot=-.9F;leftArm.xRot=-.9F;rightArm.yRot=.62F;leftArm.yRot=-.62F;rightArm.zRot=-.2F;leftArm.zRot=.2F;head.xRot=.25F;}
                case SIT,PERCH->{rightArm.xRot=-.42F+breath;leftArm.xRot=-.38F+breath;rightArm.zRot=.05F;leftArm.zRot=-.05F;}
                case LOOK->{rightArm.xRot=-1.05F;rightArm.yRot=-.32F;rightArm.zRot=0;leftArm.xRot=-.72F;leftArm.yRot=.42F;leftArm.zRot=0;head.xRot=.62F;head.yRot=0;}
                case BAG->{rightArm.xRot=rightArm.xRot*.35F-.08F;rightArm.zRot=.06F;}
                case KNIT->{float k=Mth.sin(age*.55F)*.07F;rightArm.xRot=-.98F+k;leftArm.xRot=-.98F-k;rightArm.yRot=-.42F;leftArm.yRot=.42F;rightArm.zRot=0;leftArm.zRot=0;head.xRot=.5F;head.yRot=0;}
                case DRIFT->{rightArm.xRot=-.42F+breath;leftArm.xRot=-.38F+breath;rightArm.zRot=.04F;leftArm.zRot=-.04F;head.xRot=.32F;}
                case REACH->{rightArm.xRot=-1.3F+breath;rightArm.yRot=-.12F;rightArm.zRot=0;head.xRot=.18F;}
                default->{}
            }
            m.rightSleeve.copyFrom(rightArm);m.leftSleeve.copyFrom(leftArm);m.rightPants.copyFrom(m.rightLeg);m.leftPants.copyFrom(m.leftLeg);m.jacket.copyFrom(m.body);m.hat.copyFrom(head);
        }
        /** A gesture with no entity behind it (the pose proof): the native riding legs or a walking stride, then the gesture. */
        public void preview(int g,float walk,float age){
            for(var part:new net.minecraft.client.model.geom.ModelPart[]{head,hat,body,rightArm,leftArm,rightLeg,leftLeg})part.resetPose();
            // A bare model starts young (the renderer clears it every frame); the proof has no renderer.
            young=false;riding=seated(g);crouching=false;
            if(riding){rightArm.xRot=leftArm.xRot=-.62831855F;rightLeg.xRot=leftLeg.xRot=-1.4137167F;rightLeg.yRot=.31415927F;leftLeg.yRot=-.31415927F;rightLeg.zRot=.07853982F;leftLeg.zRot=-.07853982F;}
            else if(walking(g)){float amount=g==DRIFT?.3F*.45F:.48F;rightLeg.xRot=Mth.cos(walk*.6662F)*1.4F*amount;leftLeg.xRot=Mth.cos(walk*.6662F+Mth.PI)*1.4F*amount;rightArm.xRot=Mth.cos(walk*.6662F+Mth.PI)*amount;leftArm.xRot=Mth.cos(walk*.6662F)*amount;}
            gesture(this,g,age);
        }
    }

    public static final class Resident extends MobRenderer<LiteraryActor,Model> {
        private final Model wide,slim;
        public Resident(EntityRendererProvider.Context c){
            super(c,new Model(c.bakeLayer(ModelLayers.PLAYER),false),.3F);wide=model;slim=new Model(c.bakeLayer(ModelLayers.PLAYER_SLIM),true);
            addLayer(new net.minecraft.client.renderer.entity.layers.ItemInHandLayer<>(this,c.getItemInHandRenderer()));
            addLayer(new HouseReadLookClient.Layer<>(this,c.getModelSet(),LiteraryActor::owner));
            addLayer(new Held(this));
        }
        @Override public void render(LiteraryActor a,float yaw,float partial,PoseStack p,MultiBufferSource out,int light){
            var skin=UncannyActorRenderer.houseSkin();model=skin!=null&&skin.model()==net.minecraft.client.resources.PlayerSkin.Model.SLIM?slim:wide;
            // A chair's seat is lower than the hip of a body standing on the floor; a box or the bed's edge is about level with it.
            p.pushPose();if((a.gesture()==SIT||a.gesture()==KNIT)&&a.getPose()!=Pose.SLEEPING)p.translate(0,-.3,0);super.render(a,yaw,partial,p,out,light);p.popPose();
        }
        @Override public ResourceLocation getTextureLocation(LiteraryActor a){var skin=UncannyActorRenderer.houseSkin();return skin!=null?skin.texture():net.minecraft.client.resources.DefaultPlayerSkin.getDefaultTexture();}
    }

    /** The last moving box, carried up the stairs between both hands (gesture 6): the flat's own taped box, drawn at the hands. */
    static final class Held extends net.minecraft.client.renderer.entity.layers.RenderLayer<LiteraryActor,Model> {
        Held(Resident parent){super(parent);}
        @Override public void render(PoseStack p,MultiBufferSource out,int light,LiteraryActor a,float walk,float amount,float partial,float age,float yaw,float pitch){
            if(a.gesture()==CARRY)box(p,out,light,walk,amount);
        }
        /** From the model's own space (hanging from the neck, y down, the body facing -z): the box between the hands, turned upright. */
        static void box(PoseStack p,MultiBufferSource out,int light,float walk,float amount){
            var box=io.github.knaitoe.theoldesthouse.labyrinth.SecondPersonBlock.of(io.github.knaitoe.theoldesthouse.labyrinth.SecondPersonBlock.Kind.BOX,net.minecraft.core.Direction.NORTH,0);
            p.pushPose();
            p.translate(0,.42+Mth.sin(walk*.6662F)*amount*.02F,-.44);p.scale(-1,-1,1);p.scale(.52F,.52F,.52F);p.translate(-.5,-.42,-.5);
            net.minecraft.client.Minecraft.getInstance().getBlockRenderer().renderSingleBlock(box,p,out,light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
            p.popPose();
        }
    }
}
