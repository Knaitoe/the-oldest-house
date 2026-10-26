package io.github.knaitoe.theoldesthouse.labyrinth;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
/** The original private visitor approaches during the light and becomes still when it drops out. */
public final class FoyerPresence {
    private static final Vec3[] PATH={new Vec3(.5,0,-18.5),new Vec3(-.6,0,-16.5),new Vec3(.2,0,-14.9)};
    private FoyerPresence(){}
    public static void tick(ServerPlayer p,BlockPos b,CompoundTag own,boolean dark){
        var a=UncannyStories.actor(p,own,"CastVisitor");if(a==null)return;a.appearance(LiteraryActor.UNCANNY_VISITOR,dark?Math.max(1,Math.min(3,own.getInt("FoyerStill131"))):0);
        if(dark){a.setDeltaMovement(Vec3.ZERO);return;}
        int leg=Math.min(2,own.getInt("FoyerStill131"));
        SceneFootpaths.step(a,PATH[leg].add(b.getX(),b.getY(),b.getZ()),.18);SceneFootpaths.face(a,leg==2?new Vec3(a.getX(),a.getY(),b.getZ()):p.position(),5);
        var d=p.position().subtract(a.position());float wanted=(float)Math.toDegrees(Math.atan2(-d.x,d.z));float offset=Mth.clamp(Mth.wrapDegrees(wanted-a.yBodyRot),-40,40);a.setYHeadRot(Mth.approachDegrees(a.getYHeadRot(),a.yBodyRot+offset,7));
        var pose=new CompoundTag();pose.putDouble("X",a.getX()-b.getX());pose.putDouble("Y",a.getY()-b.getY());pose.putDouble("Z",a.getZ()-b.getZ());pose.putFloat("Yaw",a.getYRot());own.put("FoyerPose131",pose);
    }
}
