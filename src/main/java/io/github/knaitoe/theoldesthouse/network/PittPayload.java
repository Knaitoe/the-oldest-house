package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 0.4.207, the God of the Pitt: where the draft is (-1 out of the passages, 0 draft, 1 warning, 2 still air, 3 steady, 4 the
 * collapse), how many ticks of it are left, and flags (1 the reader carries the barometer, 2 they are pinned, 4 the lamps are
 * out, 8 they are in the passages).
 */
public record PittPayload(int phase,int left,int flags) implements CustomPacketPayload {
    public static final Type<PittPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"pitt"));
    public static final StreamCodec<ByteBuf,PittPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,PittPayload::phase,ByteBufCodecs.VAR_INT,PittPayload::left,ByteBufCodecs.VAR_INT,PittPayload::flags,PittPayload::new);
    @Override public Type<PittPayload> type(){return TYPE;}
}
