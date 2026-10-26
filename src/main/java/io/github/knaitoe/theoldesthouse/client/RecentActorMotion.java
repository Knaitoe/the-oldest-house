package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** Frame-time pose continuity per actual body/mesh, shared by direct and repeated camera renders. */
public final class RecentActorMotion {
    private static final WeakHashMap<LiteraryActor,WeakHashMap<Object,Sample>> SAMPLES=new WeakHashMap<>();
    private static final class Sample {float time;int role;float[] values;}
    private RecentActorMotion(){}
    private static boolean recent(LiteraryActor a){return a.role()>=LiteraryActor.BORRASCA_KYLE&&a.role()<=LiteraryActor.UNCANNY_VISITOR||a.role()>=LiteraryActor.FAMILY_FATHER&&a.role()<=LiteraryActor.FAMILY_CHILD||a.role()==LiteraryActor.SILHOUETTE&&a.scene().equals(LabyrinthPlace.CHILD_ROOM.id());}
    public static void blend(LiteraryActor a,float time,HumanoidModel<?> model,ModelPart... extra){
        if(!recent(a))return;var roots=new ArrayList<ModelPart>(List.of(model.head,model.hat,model.body,model.leftArm,model.rightArm,model.leftLeg,model.rightLeg));Collections.addAll(roots,extra);
        var parts=roots.stream().filter(Objects::nonNull).flatMap(ModelPart::getAllParts).distinct().toList();var sample=SAMPLES.computeIfAbsent(a,k->new WeakHashMap<>()).computeIfAbsent(model,k->new Sample());
        var target=new float[parts.size()*9];int i=0;for(var p:parts){target[i++]=p.x;target[i++]=p.y;target[i++]=p.z;target[i++]=p.xRot;target[i++]=p.yRot;target[i++]=p.zRot;target[i++]=p.xScale;target[i++]=p.yScale;target[i++]=p.zScale;}
        float dt=time-sample.time;
        if(sample.values==null||sample.values.length!=target.length||sample.role!=a.role()||dt<0||dt>20){sample.values=target;sample.time=time;sample.role=a.role();return;}
        // A second render in the same frame reuses the same result rather than advancing again.
        float alpha=dt<=0?0:1-(float)Math.exp(-dt/2.5F);i=0;
        for(var p:parts){for(int n=0;n<9;n++,i++){
            float old=sample.values[i],next=target[i];target[i]=n>=3&&n<=5?old+Mth.wrapDegrees((next-old)*Mth.RAD_TO_DEG)*Mth.DEG_TO_RAD*alpha:Mth.lerp(alpha,old,next);
        }int j=i-9;p.setPos(target[j],target[j+1],target[j+2]);p.setRotation(target[j+3],target[j+4],target[j+5]);p.xScale=target[j+6];p.yScale=target[j+7];p.zScale=target[j+8];}
        sample.values=target;sample.time=time;
    }
}
