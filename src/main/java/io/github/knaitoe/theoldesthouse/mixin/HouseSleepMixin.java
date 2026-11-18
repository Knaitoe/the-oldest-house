package io.github.knaitoe.theoldesthouse.mixin;

import io.github.knaitoe.theoldesthouse.house.HouseSleep;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.SleepStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 0.4.703 (owner: "One player sleeping in the house triggers day phase. That player should just be synced in with the rest of the player
 * sleeping count, if possible."): the Overworld and the House pass the night on one count of sleepers (HouseSleep).
 */
@Mixin(ServerLevel.class)
public abstract class HouseSleepMixin {
    @Redirect(method="tick",at=@At(value="INVOKE",target="Lnet/minecraft/server/players/SleepStatus;areEnoughSleeping(I)Z"))
    private boolean house$enoughSleeping(SleepStatus own,int percent){
        return HouseSleep.enoughSleeping((ServerLevel)(Object)this,own,percent);
    }
    @Redirect(method="tick",at=@At(value="INVOKE",target="Lnet/minecraft/server/players/SleepStatus;areEnoughDeepSleeping(ILjava/util/List;)Z"))
    private boolean house$enoughDeepSleeping(SleepStatus own,int percent,List<ServerPlayer> players){
        return HouseSleep.enoughDeepSleeping((ServerLevel)(Object)this,own,percent,players);
    }
    @Inject(method="wakeUpAllPlayers",at=@At("TAIL"))
    private void house$wakeTheRest(CallbackInfo ci){
        HouseSleep.wakeTheRest((ServerLevel)(Object)this);
    }
    @Inject(method="announceSleepStatus",at=@At("HEAD"),cancellable=true)
    private void house$sharedTally(CallbackInfo ci){
        if(HouseSleep.announce((ServerLevel)(Object)this))ci.cancel();
    }
}
