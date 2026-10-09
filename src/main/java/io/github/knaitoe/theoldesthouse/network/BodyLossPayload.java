package io.github.knaitoe.theoldesthouse.network;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
/** Which arm a player gave at the cabin (0 none, 1 left, 2 right), so every viewer draws them as they are. */
public record BodyLossPayload(UUID player,int arm) implements CustomPacketPayload {
    public static final Type<BodyLossPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"body_loss"));
    public static final StreamCodec<ByteBuf,BodyLossPayload> STREAM_CODEC=StreamCodec.composite(UUIDUtil.STREAM_CODEC,BodyLossPayload::player,ByteBufCodecs.VAR_INT,BodyLossPayload::arm,BodyLossPayload::new);
    @Override public Type<BodyLossPayload> type(){return TYPE;}
}
