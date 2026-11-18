package io.github.knaitoe.theoldesthouse.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.SleepStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** 0.4.703: a level's own count of sleepers, which the House's shared count clears when the Overworld wakes everyone (HouseSleep). */
@Mixin(ServerLevel.class)
public interface HouseSleepAccess {
    @Accessor("sleepStatus") SleepStatus house$sleepStatus();
}
