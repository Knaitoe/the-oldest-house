package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** How much of the static is left on the reader, 0 to 1, and whether writing has just settled it (journal/Grounding). */
public record GroundingPayload(float level,boolean settled) implements CustomPacketPayload {
    public static final Type<GroundingPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"grounding"));
    public static final StreamCodec<ByteBuf,GroundingPayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.FLOAT,GroundingPayload::level,ByteBufCodecs.BOOL,GroundingPayload::settled,GroundingPayload::new);
    @Override public Type<GroundingPayload> type(){return TYPE;}
}
