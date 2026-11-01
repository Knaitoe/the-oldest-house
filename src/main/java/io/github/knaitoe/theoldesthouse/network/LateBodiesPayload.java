package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
/**
 * 0.4.216, your own light's cost (docs/OWN_LIGHT_0_4_213.md): every reader whose double went home before them, sent to every
 * client. Until they next sleep, other readers' clients now and then draw them a beat behind their own body.
 */
public record LateBodiesPayload(List<UUID> readers) implements CustomPacketPayload {
    public static final Type<LateBodiesPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"late_bodies"));
    public static final StreamCodec<ByteBuf,LateBodiesPayload> STREAM_CODEC=StreamCodec.composite(UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs.list(64)),LateBodiesPayload::readers,LateBodiesPayload::new);
    @Override public Type<LateBodiesPayload> type(){return TYPE;}
}
