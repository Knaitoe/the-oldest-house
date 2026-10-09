package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWatchers;
import java.util.List;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;

/** A saved, occupied-time ambush at an ordinary door. One actor, one clock. */
public final class KillerDoors {
    private static final String STATE="KillerDoor0461";
    public static final int WAIT_TICKS=200, CRACK_TICKS=60;
    private KillerDoors() {}
    private static CompoundTag state(LiteraryActor a){return a.getPersistentData().getCompound(STATE);}
    public static int waitTicks(LiteraryActor a){return state(a).getInt("Ticks");}
    public static boolean active(LiteraryActor a){return state(a).contains("Door");}
    public static BlockPos door(LiteraryActor a){var s=state(a);return s.contains("Door")?BlockPos.of(s.getLong("Door")):null;}
    public static boolean breakable(LiteraryActor a,BlockPos at){
        if(!(a.level() instanceof ServerLevel l)||!l.hasChunkAt(at)||!l.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING))return false;
        var block=l.getBlockState(at);
        if(!(block.getBlock() instanceof DoorBlock)||!block.is(BlockTags.WOODEN_DOORS)||block.getValue(DoorBlock.OPEN))return false;
        var lower=block.getValue(DoorBlock.HALF)==DoubleBlockHalf.UPPER?at.below():at;
        var upper=l.getBlockState(lower.above());
        return upper.is(block.getBlock())&&upper.getValue(DoorBlock.HALF)==DoubleBlockHalf.UPPER
                &&l.getBlockEntity(lower)==null&&LabyrinthData.get(l.getServer()).doorAt(l.dimension(),lower)==null;
    }
    public static void clear(LiteraryActor a){
        var at=door(a);if(at!=null&&a.level() instanceof ServerLevel l)l.destroyBlockProgress(a.getId(),at,-1);
        a.getPersistentData().remove(STATE);
    }
    /** Find a real door across the next approach, never an unrelated nearby door. */
    static BlockPos approach(LiteraryActor a,Vec3 goal){
        if(active(a))return door(a);
        BlockPos best=null;double nearest=9;
        for(var at:BlockPos.betweenClosed(a.blockPosition().offset(-2,-1,-2),a.blockPosition().offset(2,1,2))){
            if(!breakable(a,at))continue;var block=a.level().getBlockState(at);if(block.getValue(DoorBlock.HALF)!=DoubleBlockHalf.LOWER)continue;
            var center=Vec3.atBottomCenterOf(at);boolean x=block.getValue(DoorBlock.FACING).getAxis()==Direction.Axis.X;
            double from=x?a.getX()-center.x:a.getZ()-center.z,to=x?goal.x-center.x:goal.z-center.z;
            if(from*to>=0||Math.abs(from)<.15||Math.abs((x?a.getZ()-center.z:a.getX()-center.x))>1.8)continue;
            double distance=a.position().distanceToSqr(center);if(distance<nearest){nearest=distance;best=at.immutable();}
        }return best;
    }
    private static Vec3 side(LiteraryActor a,BlockPos at){
        var block=a.level().getBlockState(at);boolean x=block.getValue(DoorBlock.FACING).getAxis()==Direction.Axis.X;
        var center=Vec3.atBottomCenterOf(at);double sign=Math.signum(x?a.getX()-center.x:a.getZ()-center.z);
        for(int hand:new int[]{1,-1}){
            var point=center.add(x?sign*1.05:hand*1.0,0,x?hand*1.0:sign*1.05);
            var body=a.getBoundingBox().move(point.subtract(a.position()));
            if(CarcassHunt.fits(a,body))return point;
        }return null;
    }
    /** Advance once per occupied actor tick. Opening or changing the original door cancels demolition. */
    static boolean work(LiteraryActor a,BlockPos at,List<ServerPlayer> readers){
        if(!breakable(a,at)||a.position().distanceToSqr(Vec3.atBottomCenterOf(at))>9){clear(a);return false;}
        var l=(ServerLevel)a.level();var original=NbtUtils.writeBlockState(l.getBlockState(at));var s=state(a);
        if(!s.contains("Door")||s.getLong("Door")!=at.asLong()){
            clear(a);s=new CompoundTag();s.putLong("Door",at.asLong());s.put("Original",original);
        }else if(!s.getCompound("Original").equals(original)){clear(a);return false;}
        if(s.contains("GameTick")&&s.getLong("GameTick")==l.getGameTime())return true;
        s.putLong("GameTick",l.getGameTime());s.putInt("Ticks",s.getInt("Ticks")+1);
        a.getNavigation().stop();a.setDeltaMovement(a.getDeltaMovement().multiply(0,1,0));a.appearance(LiteraryActor.KILLER,ElkHunt.WATCH);
        if(!s.contains("SideX")&&readers.stream().anyMatch(p->p.isAlive()&&!p.isSpectator()&&p.distanceToSqr(at.getCenter())<24*24&&HouseWatchers.sees(p,at.above().getCenter()))){
            var point=side(a,at);if(point!=null){s.putDouble("SideX",point.x);s.putDouble("SideY",point.y);s.putDouble("SideZ",point.z);}
        }
        if(s.contains("SideX")){
            var point=new Vec3(s.getDouble("SideX"),s.getDouble("SideY"),s.getDouble("SideZ"));var delta=point.subtract(a.position()).multiply(1,0,1);
            if(delta.lengthSqr()>.01){var move=delta.normalize().scale(Math.min(.14,delta.length()));var sweep=a.getBoundingBox().minmax(a.getBoundingBox().move(move));
                if(CarcassHunt.fits(a,sweep)){CarcassHunt.clearLeaves(a,move);var before=a.position();a.move(MoverType.SELF,move);a.walkAnimation.update((float)a.position().distanceTo(before)*4,.4F);}}
        }
        var face=at.getCenter().subtract(a.position());a.setYRot((float)Math.toDegrees(Math.atan2(-face.x,face.z)));a.yBodyRot=a.getYRot();
        int crack=s.getInt("Ticks")-(WAIT_TICKS-CRACK_TICKS);
        if(crack>0){if(crack%20==1){a.swing(InteractionHand.MAIN_HAND);l.levelEvent(1019,at,0);}l.destroyBlockProgress(a.getId(),at,Math.min(9,crack*10/CRACK_TICKS));}
        a.getPersistentData().put(STATE,s);
        if(s.getInt("Ticks")>=WAIT_TICKS){
            if(breakable(a,at)&&s.getCompound("Original").equals(NbtUtils.writeBlockState(l.getBlockState(at)))
                    &&EventHooks.onEntityDestroyBlock(a,at,l.getBlockState(at))&&EventHooks.onEntityDestroyBlock(a,at.above(),l.getBlockState(at.above()))
                    &&l.destroyBlock(at,true,a))l.levelEvent(1021,at,0);
            clear(a);
        }return true;
    }
}
