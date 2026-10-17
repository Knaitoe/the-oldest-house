package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.labyrinth.LakeCongregantEntity;
import io.github.knaitoe.theoldesthouse.labyrinth.Shallows;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

public final class LakeCongregantModel extends HumanoidModel<LakeCongregantEntity> {
    public LakeCongregantModel(ModelPart root) { super(root); }
    @Override public void setupAnim(LakeCongregantEntity entity, float walk, float amount, float age, float yaw, float pitch) {
        HumanoidMotion.reset(this);
        super.setupAnim(entity, walk, 0, age, 0, 0);
        leftArm.xRot = rightArm.xRot = entity.preacher() ? -.25F : -.05F;
        float seat=entity.seating(age-entity.tickCount);
        // Lower the whole seated pose; unbending the knees alone leaves heads at standing height.
        head.y=hat.y=body.y=6*seat;
        leftArm.y=rightArm.y=2+6*seat;
        leftLeg.y=rightLeg.y=12+6*seat;
        leftLeg.xRot=rightLeg.xRot=-1.45F*seat;leftLeg.yRot=.15F*seat;rightLeg.yRot=-.15F*seat;
        body.xRot=.1F*seat;head.xRot=.12F*seat;
        if(entity.memoryBoy()){var motion=HumanoidMotion.sample(entity,age-entity.tickCount);if(seat<.1F)HumanoidMotion.gait(this,motion.phase(),motion.amount(),motion.running());body.yRot=.02F*(float)Math.sin(age*.035F+entity.getId());
            shallows(entity,age);HumanoidMotion.connect(this);}
    }
    /**
     * 0.4.101: the boy at her feet holds and swings her; every boy on the bank watches her and reacts, to this reader only.
     * Torso-local angles: the caller connects head and arms to the bent torso afterward.
     */
    private void shallows(LakeCongregantEntity boy,float age){
        float partial=age-boy.tickCount;
        carrierPose(boy.carry(),boy.swingAt(partial));
        var focus=ShallowsClient.focus(partial);
        if(focus!=null&&boy.carry()==0){
            var eye=boy.getPosition(partial).add(0,1.55,0);double dx=focus.x-eye.x,dz=focus.z-eye.z;
            float turn=Mth.wrapDegrees((float)(Mth.atan2(-dx,dz)*Mth.RAD_TO_DEG)-Mth.rotLerp(partial,boy.yBodyRotO,boy.yBodyRot));
            head.yRot=Mth.clamp(turn,-75,75)*Mth.DEG_TO_RAD;head.xRot=Mth.clamp((float)-Math.atan2(focus.y-eye.y,Math.sqrt(dx*dx+dz*dz)),-.6F,.9F);
        }
        reaction(ShallowsClient.mood(boy),ShallowsClient.moodAge(boy,partial));
    }
    /** The boy at her feet: bent over her ankles, swinging her, or following through after letting go. */
    public void carrierPose(int carry,float swing){
        switch(carry){
            case Shallows.HOLD->{body.xRot=.38F;head.xRot=.55F;leftArm.xRot=rightArm.xRot=-.62F;leftArm.zRot=.1F;rightArm.zRot=-.1F;leftLeg.xRot=-.18F;rightLeg.xRot=.12F;}
            // Facing the reader across her, the water is on his right.
            case Shallows.SWINGING->{ShallowsClient.carryArms(this,swing,1,0,true);rightLeg.xRot=.18F;leftLeg.xRot=-.22F;}
            case Shallows.RELEASED->{ShallowsClient.carryArms(this,swing*.4F,1,1,true);body.xRot=.18F;}
            default->{}
        }
    }
    /** What a boy does with a line or alone: jeers, cheers, freezes, backs away, whispers. */
    public void reaction(int mood,float t){
        float bob=Mth.sin(t*.5F);
        switch(mood){
            case Shallows.JEER->{rightArm.xRot=-1.55F+bob*.18F;rightArm.yRot=head.yRot*.6F;leftArm.xRot=-.2F;}
            case Shallows.CHEER->{leftArm.xRot=-2.75F+bob*.25F;rightArm.xRot=-2.75F-bob*.25F;leftArm.zRot=-.25F;rightArm.zRot=.25F;}
            case Shallows.FROZEN->{leftArm.xRot=rightArm.xRot=-.08F;leftArm.zRot=rightArm.zRot=0;body.xRot=-.04F;head.xRot+=.08F;}
            case Shallows.RECOIL->{float k=Math.min(1,t/6);body.xRot=-.28F*k;leftArm.xRot=rightArm.xRot=-1.25F*k;leftArm.zRot=-.45F*k;rightArm.zRot=.45F*k;head.xRot-=.2F*k;
                leftLeg.xRot=-.35F*k;rightLeg.xRot=.25F*k;}
            case Shallows.WHISPER->{body.zRot=.12F;head.zRot=.18F;leftArm.xRot=rightArm.xRot=-.1F;}
            default->{}
        }
    }
}
