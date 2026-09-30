package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Keeps the cloth closed even during an inventory prediction; ending is sent only to its player. */
public record ClapGamePayload(boolean bound, int endingTick, float yaw) implements CustomPacketPayload {
    public static final Type<ClapGamePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, "clap_game"));
    public static final StreamCodec<ByteBuf, ClapGamePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ClapGamePayload::bound,
            ByteBufCodecs.VAR_INT, ClapGamePayload::endingTick,
            ByteBufCodecs.FLOAT, ClapGamePayload::yaw, ClapGamePayload::new);
    @Override public Type<ClapGamePayload> type() { return TYPE; }
}
