package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** A real, gravity-driven hop over one yacht rail, with dry supported takeoff and landing. */
final class YachtRailVault {
    private static final String KEY="YachtVault0464",MISSED="YachtVaultMissed0464";
    /** A hop that fell short is not tried again over the same rail for this long, so the blocked-route recovery plans around it. */
    private static final int MISS_MEMORY=200;
    private static AABB body(Vec3 foot){return CarcassHunt.box(foot,1.3);}
    private static boolean supported(LiteraryActor actor,BlockPos foot){
        if(!actor.level().hasChunkAt(foot)||!actor.level().hasChunkAt(foot.below()))return false;
        var floor=actor.level().getBlockState(foot.below());
        return floor.getFluidState().isEmpty()&&floor.isFaceSturdy(actor.level(),foot.below(),Direction.UP)
                &&actor.level().getBlockState(foot).getFluidState().isEmpty();
    }
    static boolean canCross(LiteraryActor actor,BlockPos from,BlockPos to){
        if(actor.owner().isEmpty()||!actor.scene().equals(LabyrinthPlace.ELK_CARCASSES.id())||from.getY()!=to.getY())return false;
        int dx=to.getX()-from.getX(),dz=to.getZ()-from.getZ();
        if(Math.abs(dx)+Math.abs(dz)!=2||dx!=0&&dz!=0)return false;
        // The pathfinder asks this of every landing: the rail itself is the cheap test, so it comes first.
        var rail=from.offset(dx/2,0,dz/2);
        if(!actor.level().hasChunkAt(rail)||!actor.level().getBlockState(rail).is(LiteraryRegistry.YACHT_RAIL.get())
                ||actor.level().getBlockState(rail.above()).is(LiteraryRegistry.YACHT_RAIL.get()))return false;
        if(missed(actor,from,to)||!supported(actor,from)||!supported(actor,to))return false;
        Vec3 a=Vec3.atBottomCenterOf(from),b=Vec3.atBottomCenterOf(to);
        if(!CarcassHunt.fits(actor,body(a))||!CarcassHunt.fits(actor,body(b)))return false;
        // The crouched body and its entire jump arc must fit beneath the real roof.
        var sweep=body(a.add(0,1.05,0)).minmax(body(b.add(0,1.6,0)));
        return CarcassHunt.fits(actor,sweep)&&CarcassHunt.fits(actor,body(a).minmax(body(a.add(0,1.6,0))))
                &&CarcassHunt.fits(actor,body(b).minmax(body(b.add(0,1.6,0))));
    }
    private static boolean missed(LiteraryActor actor,BlockPos from,BlockPos to){
        if(!actor.getPersistentData().contains(MISSED))return false;var m=actor.getPersistentData().getCompound(MISSED);int age=actor.tickCount-m.getInt("At");
        return m.getLong("From")==from.asLong()&&m.getLong("Landing")==to.asLong()&&age>=0&&age<MISS_MEMORY;
    }
    static boolean active(LiteraryActor actor){return actor.getPersistentData().contains(KEY);}
    static void cancel(LiteraryActor actor){if(active(actor))finish(actor);}
    /** Out of the hop: no carried horizontal push, and standing again (the landing was checked for the full arc's headroom). */
    private static void finish(LiteraryActor actor){
        actor.getPersistentData().remove(KEY);actor.setDeltaMovement(actor.getDeltaMovement().multiply(0,1,0));actor.setPose(Pose.STANDING);actor.refreshDimensions();
    }
    static void begin(LiteraryActor actor,BlockPos landing){
        var state=new CompoundTag();state.putLong("From",actor.blockPosition().asLong());state.putLong("Landing",landing.asLong());state.putInt("Ticks",0);
        actor.getPersistentData().put(KEY,state);actor.getNavigation().stop();actor.setPose(Pose.CROUCHING);actor.refreshDimensions();
        actor.setDeltaMovement(new Vec3(0,.48,0));
    }
    /** Called only while the actor's original reader is present and participating. */
    static void prepare(LiteraryActor actor){
        var state=actor.getPersistentData().getCompound(KEY);int ticks=state.getInt("Ticks");
        if(ticks>4&&actor.onGround()||ticks>=32){
            if(actor.position().distanceToSqr(Vec3.atBottomCenterOf(BlockPos.of(state.getLong("Landing"))))>.36){
                var m=new CompoundTag();m.putLong("From",state.getLong("From"));m.putLong("Landing",state.getLong("Landing"));m.putInt("At",actor.tickCount);actor.getPersistentData().put(MISSED,m);}
            finish(actor);return;}
        actor.getNavigation().stop();actor.setPose(Pose.CROUCHING);actor.refreshDimensions();
        var delta=Vec3.atBottomCenterOf(BlockPos.of(state.getLong("Landing"))).subtract(actor.position()).multiply(1,0,1);
        var horizontal=delta.lengthSqr()<.001?Vec3.ZERO:delta.normalize().scale(Math.min(.20,delta.length()));
        actor.setDeltaMovement(new Vec3(horizontal.x,actor.getDeltaMovement().y,horizontal.z));
        // The arc was judged with foliage passable (as the walk is), so the hop tears through what it touches, as a step does.
        CarcassHunt.clearLeaves(actor,actor.getDeltaMovement());
        actor.setYRot((float)Math.toDegrees(Math.atan2(-delta.x,delta.z)));actor.yBodyRot=actor.getYRot();
        state.putInt("Ticks",ticks+1);actor.getPersistentData().put(KEY,state);
    }
}
