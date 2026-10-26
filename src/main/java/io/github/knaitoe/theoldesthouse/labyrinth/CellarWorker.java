package io.github.knaitoe.theoldesthouse.labyrinth;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
/** The original worker reaches an exposed surface, strikes it and destroys a real tissue block. */
public final class CellarWorker {
    private CellarWorker(){}
    private static boolean visible(ServerLevel l,LiteraryActor a,BlockPos at){return l.clip(new ClipContext(a.getEyePosition(),at.getCenter(),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,a)).getBlockPos().equals(at);}
    public static void mine(ServerLevel l,BlockPos b,CompoundTag shared,LiteraryActor worker){
        long now=l.getGameTime();BlockPos target=shared.contains("WorkerTarget131")?BlockPos.of(shared.getLong("WorkerTarget131")):null;
        if(target!=null&&(!UncannyFleshBlock.is(l.getBlockState(target))||worker.distanceToSqr(target.getCenter())>12||!visible(l,worker,target))){l.destroyBlockProgress(worker.getId(),target,-1);target=null;shared.remove("WorkerTarget131");}
        if(target==null){
            double nearest=Double.MAX_VALUE;Vec3 approach=null;double approachDistance=Double.MAX_VALUE;
            for(int x=-6;x<=6;x++)for(int y=-5;y<=-2;y++)for(int z=-28;z<=-10;z++){
                var r=new BlockPos(x,y,z);var at=b.offset(r);var state=l.getBlockState(at);if(!UncannyFleshBlock.is(state)||state.is(UncannyRegistry.FLESH.get())&&state.getValue(UncannyFleshBlock.LAYER)>=4)continue;
                double d=worker.distanceToSqr(at.getCenter());if(d<=12&&d<nearest&&visible(l,worker,at)){nearest=d;target=at;}
                if(y!=-4&&y!=-5)continue;
                for(var side:Direction.Plane.HORIZONTAL){var point=new Vec3(at.getX()+.5+side.getStepX()*1.2,b.getY()-5,at.getZ()+.5+side.getStepZ()*1.2);var below=BlockPos.containing(point).below();
                    if(!l.getBlockState(below).isFaceSturdy(l,below,Direction.UP)||!l.noCollision(worker,worker.getBoundingBox().move(point.subtract(worker.position()))))continue;
                    double distance=worker.distanceToSqr(point);if(distance<approachDistance){approach=point;approachDistance=distance;}
                }
            }
            if(target==null){if(approach!=null)SceneFootpaths.step(worker,approach,.35);return;}
            shared.putLong("WorkerTarget131",target.asLong());shared.putLong("WorkerMiningAt131",now);
        }
        SceneFootpaths.face(worker,target.getCenter(),14);worker.setDeltaMovement(Vec3.ZERO);
        int progress=(int)((now-shared.getLong("WorkerMiningAt131"))/5);l.destroyBlockProgress(worker.getId(),target,Math.min(9,progress));
        if(progress%3==0)worker.swing(InteractionHand.MAIN_HAND,true);
        if(progress<10)return;
        l.destroyBlockProgress(worker.getId(),target,-1);
        if(l.destroyBlock(target,false,worker)){UncannyCellar.cut(l,b,target.subtract(b),shared);shared.putInt("WorkerCuts131",shared.getInt("WorkerCuts131")+1);shared.putLong("LastPeel",now);}
        shared.remove("WorkerTarget131");
    }
}
