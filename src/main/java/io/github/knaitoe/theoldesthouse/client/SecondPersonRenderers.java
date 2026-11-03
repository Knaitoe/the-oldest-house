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
 */
public final class SecondPersonRenderers {
    private SecondPersonRenderers(){}
    public static final int STAND=0,SIT=1,PHONE=3,CAMERA=4,PERCH=5,CARRY=6,COLD=7;

    public static final class Model extends PlayerModel<LiteraryActor> {
        public Model(net.minecraft.client.model.geom.ModelPart part,boolean slim){super(part,slim);}
        @Override public void setupAnim(LiteraryActor a,float walk,float amount,float age,float yaw,float pitch){
            int g=a.gesture();riding=g==SIT||g==PERCH;crouching=false;
            super.setupAnim(a,g==STAND||g==CARRY?walk:0,g==STAND||g==CARRY?amount:0,age,yaw,pitch);
            float breath=Mth.sin(age*.05F)*.03F;
            switch(g){
                case PHONE->{rightArm.xRot=-2.35F;rightArm.yRot=-.55F;rightArm.zRot=.12F;head.zRot=-.12F;}
                case CAMERA->{rightArm.xRot=-1.62F;leftArm.xRot=-1.62F;rightArm.yRot=-.38F;leftArm.yRot=.38F;rightArm.zRot=0;leftArm.zRot=0;head.xRot=0;}
                case CARRY->{rightArm.xRot=-1.05F+breath;leftArm.xRot=-1.05F+breath;rightArm.yRot=-.22F;leftArm.yRot=.22F;rightArm.zRot=0;leftArm.zRot=0;}
                case COLD->{rightArm.xRot=-.9F;leftArm.xRot=-.9F;rightArm.yRot=.62F;leftArm.yRot=-.62F;rightArm.zRot=-.2F;leftArm.zRot=.2F;head.xRot=.25F;}
                case SIT,PERCH->{rightArm.xRot=-.42F+breath;leftArm.xRot=-.38F+breath;rightArm.zRot=.05F;leftArm.zRot=-.05F;}
                default->{}
            }
            rightSleeve.copyFrom(rightArm);leftSleeve.copyFrom(leftArm);rightPants.copyFrom(rightLeg);leftPants.copyFrom(leftLeg);jacket.copyFrom(body);hat.copyFrom(head);
        }
    }

    public static final class Resident extends MobRenderer<LiteraryActor,Model> {
        private final Model wide,slim;
        public Resident(EntityRendererProvider.Context c){
            super(c,new Model(c.bakeLayer(ModelLayers.PLAYER),false),.3F);wide=model;slim=new Model(c.bakeLayer(ModelLayers.PLAYER_SLIM),true);
            addLayer(new net.minecraft.client.renderer.entity.layers.ItemInHandLayer<>(this,c.getItemInHandRenderer()));
            addLayer(new HouseReadLookClient.Layer<>(this,c.getModelSet(),LiteraryActor::owner));
        }
        @Override public void render(LiteraryActor a,float yaw,float partial,PoseStack p,MultiBufferSource out,int light){
            var skin=UncannyActorRenderer.houseSkin();model=skin!=null&&skin.model()==net.minecraft.client.resources.PlayerSkin.Model.SLIM?slim:wide;
            // A chair's seat is lower than the hip of a body standing on the floor; a box or the bed's edge is about level with it.
            p.pushPose();if(a.gesture()==SIT&&a.getPose()!=Pose.SLEEPING)p.translate(0,-.3,0);super.render(a,yaw,partial,p,out,light);p.popPose();
        }
        @Override public ResourceLocation getTextureLocation(LiteraryActor a){var skin=UncannyActorRenderer.houseSkin();return skin!=null?skin.texture():net.minecraft.client.resources.DefaultPlayerSkin.getDefaultTexture();}
    }
}
