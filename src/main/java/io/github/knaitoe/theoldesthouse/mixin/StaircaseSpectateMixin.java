package io.github.knaitoe.theoldesthouse.mixin;

import io.github.knaitoe.theoldesthouse.labyrinth.StaircaseLeaks;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keep a spectator camera on the stairs while another reader is in personal scenery. */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class StaircaseSpectateMixin {
    @Shadow public ServerPlayer player;
    @Inject(method="handleTeleportToEntityPacket",at=@At("HEAD"),cancellable=true)
    private void house$privateReader(ServerboundTeleportToEntityPacket packet,CallbackInfo ci){
        PacketUtils.ensureRunningOnSameThread(packet,(ServerGamePacketListenerImpl)(Object)this,player.serverLevel());
        if(!player.isSpectator())return;
        for(var level:player.server.getAllLevels())if(packet.getEntity(level) instanceof ServerPlayer reader&&StaircaseLeaks.active(reader)){ci.cancel();return;}
    }
}
