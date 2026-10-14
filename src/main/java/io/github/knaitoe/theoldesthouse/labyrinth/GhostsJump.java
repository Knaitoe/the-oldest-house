package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** An original body leaves the bed, walks to the front balcony and clears the native guard before its frozen apex. */
public final class GhostsJump {
    private static final double[][] APPROACH={{-9.5,5.75,-32.5},{-8.4,5.75,-32.5},{-8.4,5,-30.5},{-9.5,5,-24.5},{-9.5,5,-21.5},{-9.5,5,-16.25}};
    private static final String ARC="GhostJumpArc0495",APEX="GhostJumpApex0495";
    private GhostsJump(){}
    static boolean room(ServerPlayer player,BlockPos base,CompoundTag own){
        if(own.getInt("FilmDay")!=6||own.getInt("ShotClock")<600)return false;
        var at=player.position().subtract(base.getX(),base.getY(),base.getZ());
        return at.y>=4.5&&at.y<10&&at.x>-19&&at.x<0&&at.z>-44&&at.z<-14;
    }
    static boolean prepare(ServerPlayer player,BlockPos base,LiteraryActor actor,CompoundTag own){
        int step=own.getInt("JumpApproach0495");
        if(step>=APPROACH.length){
            Vec3 apex=new Vec3(base.getX()-9.5,base.getY()+7,base.getZ()-14.45);
            var hit=player.serverLevel().clip(new ClipContext(player.getEyePosition(),apex,ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,player));
            return hit.getType()==HitResult.Type.MISS;
        }
        actor.appearance(actor.role(),GhostsSet.STAND);
        // Open only the original room door; edited construction is never replaced.
        if(step>=3){var door=base.offset(-9,5,-23);var state=player.serverLevel().getBlockState(door);if(state.getBlock() instanceof DoorBlock nativeDoor&&!state.getValue(DoorBlock.OPEN))nativeDoor.setOpen(actor,player.serverLevel(),state,door,true);}
        var target=Vec3.atLowerCornerOf(base).add(APPROACH[step][0],APPROACH[step][1],APPROACH[step][2]);
        GhostsMotion.toward(actor,target,.5);
        if(actor.position().distanceToSqr(target)<.012)own.putInt("JumpApproach0495",step+1);
        if(!own.getBoolean("JumpLandingLine0495")){player.displayClientMessage(net.minecraft.network.chat.Component.literal("Barry: Follow her to the landing. Keep the railing in frame."),false);own.putBoolean("JumpLandingLine0495",true);}
        return false;
    }
    static boolean begin(LiteraryActor actor){
        var data=actor.getPersistentData();if(data.getBoolean(APEX))return true;
        if(!data.contains(ARC)){GhostsMotion.stop(actor);data.putInt(ARC,0);actor.appearance(actor.role(),GhostsSet.LEAP);actor.setYRot(0);actor.setYBodyRot(0);actor.setYHeadRot(0);}
        return false;
    }
    static void freezeHead(LiteraryActor actor,float target){var data=actor.getPersistentData();if(!data.contains(ARC)){data.putInt(ARC,6);data.putBoolean(APEX,true);}data.putFloat("GhostJumpHead0495",target);data.putLong("GhostJumpHeadAt0495",actor.level().getGameTime());}
    static boolean tick(LiteraryActor actor){
        var data=actor.getPersistentData();if(!data.contains(ARC))return false;
        if(data.getBoolean(APEX)){actor.setDeltaMovement(Vec3.ZERO);actor.setYRot(0);actor.setYBodyRot(0);
            if(data.contains("GhostJumpHead0495")){float turn=net.minecraft.util.Mth.wrapDegrees(data.getFloat("GhostJumpHead0495"));float progress=net.minecraft.util.Mth.clamp((actor.level().getGameTime()-data.getLong("GhostJumpHeadAt0495"))/45F,0,1);actor.setYHeadRot(turn*progress);}return true;}
        int tick=data.getInt(ARC);double up=.56-.08*tick;
        var before=actor.position();actor.move(MoverType.SELF,new Vec3(0,up,.3));actor.setDeltaMovement(Vec3.ZERO);
        // Never count a collision-blocked jump as a completed leap.
        if(actor.position().distanceToSqr(before)<.000001)return true;
        data.putInt(ARC,tick+1);if(tick>=5)data.putBoolean(APEX,true);
        return true;
    }
    static void clear(LiteraryActor actor){actor.getPersistentData().remove(ARC);actor.getPersistentData().remove(APEX);actor.getPersistentData().remove("GhostJumpHead0495");actor.getPersistentData().remove("GhostJumpHeadAt0495");}
}
