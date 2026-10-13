package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/** Head-attached painted strips: loose lengths move in the lake, then become unnaturally still indoors. */
public final class LakeHouseHair {
    private final ModelPart[] bones=new ModelPart[LakeHairDynamics.JOINTS];
    private static final class Motion {final LakeHairDynamics joints=new LakeHairDynamics();int tick=Integer.MIN_VALUE;Vec3 at;float yaw;}
    private final Map<LiteraryActor,Motion> motion=new WeakHashMap<>();
    public LakeHouseHair(ModelPart root){for(int c=0;c<3;c++){var parent=root;for(int i=0;i<3;i++){parent=parent.getChild("strand_"+c+"_"+i);bones[c*3+i]=parent;}}}
    public static void layer(PartDefinition head){
        var root=head.addOrReplaceChild("lake_hair",CubeListBuilder.create()
            .texOffs(64,0).addBox(-4,-8,3,8,8,1,new CubeDeformation(.06F))
            .texOffs(84,0).addBox(-4,-8,-2,1,8,6,new CubeDeformation(.06F))
            .texOffs(84,0).mirror().addBox(3,-8,-2,1,8,6,new CubeDeformation(.06F)),PartPose.ZERO);
        for(int c=0;c<3;c++){
            var parent=root;int row=8;float previous=0;
            for(int i=0;i<3;i++){
                int height=c==0?(i==0?4:3):(i==0?3:2);var cube=CubeListBuilder.create().texOffs(c==0?64:84,row).mirror(c==2);
                if(c==0)cube.addBox(-4,0,0,8,height,1,new CubeDeformation(.035F));else cube.addBox(0,0,0,1,height,6,new CubeDeformation(.035F));
                parent=parent.addOrReplaceChild("strand_"+c+"_"+i,cube,i==0?PartPose.offset(c==0?0:c==1?-4:3,0,c==0?3:-2):PartPose.offset(0,previous,0));previous=height;row+=height;
            }
        }
    }
    public static boolean inHouse(LiteraryActor a){
        var origin=HouseSightlineState.origin();if(origin==null)return false;var b=LabyrinthPlaces.base(origin,LabyrinthPlace.LAKE_HOUSE);var r=a.position().subtract(Vec3.atLowerCornerOf(b));
        return Math.abs(r.x-.5)<9&&r.z<-161&&r.z>-179&&r.y>-44&&r.y<-14;
    }
    private void apply(LakeHairDynamics d,float partial){for(int i=0;i<bones.length;i++){bones[i].xRot=d.pitch(i,partial);bones[i].zRot=d.roll(i,partial);}}
    public void pose(LiteraryActor a,float age){
        var m=motion.computeIfAbsent(a,key->new Motion());boolean drift=a.isInWater()&&!inHouse(a);int tick=a.tickCount;
        if(!drift){m.joints.still();m.tick=tick;m.at=a.position();m.yaw=a.getYRot();apply(m.joints,1);return;}
        if(m.tick!=tick){
            var delta=m.at==null?Vec3.ZERO:a.position().subtract(m.at);if(delta.lengthSqr()>16)m.joints.still();double yaw=Math.toRadians(a.getYRot());double side=delta.x*Math.cos(yaw)+delta.z*Math.sin(yaw),forward=-delta.x*Math.sin(yaw)+delta.z*Math.cos(yaw);
            int steps=m.tick==Integer.MIN_VALUE?1:Math.min(4,Math.max(1,tick-m.tick));float turn=Mth.wrapDegrees(a.getYRot()-m.yaw);
            for(int n=0;n<steps;n++)m.joints.step(true,side,forward,turn,tick-steps+n+1);m.tick=tick;m.at=a.position();m.yaw=a.getYRot();
        }apply(m.joints,Mth.clamp(age-tick,0,1));
    }
    public void preview(boolean drifting,float age){var d=new LakeHairDynamics();for(int t=0;t<80;t++)d.step(drifting,.06,.09,0,(int)age-80+t);apply(d,1);}
}
