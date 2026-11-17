package io.github.knaitoe.theoldesthouse.mixin;

import io.github.knaitoe.theoldesthouse.labyrinth.NotYetTrial;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 0.4.665, Not Yet: a reader lying down for the machine's trial gets up when they ask to (the bed screen's Leave Bed) and only then. The
 * game's own wake-up is held off for the trial (GoatmanWakeMixin); this lets the reader's own request through.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class NotYetGetUpMixin {
    @Shadow public ServerPlayer player;
    @Inject(method="handlePlayerCommand",at=@At("HEAD"))
    private void house$askedToGetUp(ServerboundPlayerCommandPacket packet,CallbackInfo ci){
        PacketUtils.ensureRunningOnSameThread(packet,(ServerGamePacketListenerImpl)(Object)this,player.serverLevel());
        if(packet.getAction()==ServerboundPlayerCommandPacket.Action.STOP_SLEEPING)NotYetTrial.asked(player);
    }
}
