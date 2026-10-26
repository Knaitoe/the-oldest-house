package io.github.knaitoe.theoldesthouse.labyrinth;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
/** The original private visitor approaches during the light and becomes still when it drops out. */
public final class FoyerPresence {
    private static final Vec3[] PATH={new Vec3(1.5,0,-18.5),new Vec3(-1.5,0,-17.5),new Vec3(.5,0,-16.5)};
    private FoyerPresence(){}
    public static void tick(ServerPlayer p,BlockPos b,CompoundTag own,boolean dark){
        var a=UncannyStories.actor(p,own,"CastVisitor");if(a==null)return;a.appearance(LiteraryActor.UNCANNY_VISITOR,dark?1:0);
        if(dark){a.setDeltaMovement(Vec3.ZERO);return;}
        int leg=own.getInt("FoyerApproach130");
        if(leg<PATH.length){var target=PATH[leg].add(b.getX(),b.getY(),b.getZ());if(SceneFootpaths.step(a,target,.18))own.putInt("FoyerApproach130",leg+1);}
        else{int side=own.getInt("UncannyAge")/180%2;SceneFootpaths.step(a,new Vec3(b.getX()+.25+side*.5,b.getY(),b.getZ()-16.5),.035);SceneFootpaths.face(a,p.position(),5);}
        var d=p.position().subtract(a.position());float wanted=(float)Math.toDegrees(Math.atan2(-d.x,d.z));float offset=Mth.clamp(Mth.wrapDegrees(wanted-a.yBodyRot),-40,40);a.setYHeadRot(Mth.approachDegrees(a.getYHeadRot(),a.yBodyRot+offset,7));
    }
}
