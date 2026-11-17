package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** 0.4.669, Last Seen: the park's hour for this reader, morning through to dusk on the second day (docs/LAST_SEEN_0_4_669.md). */
public record LastSeenTimePayload(long time) implements CustomPacketPayload {
    public static final Type<LastSeenTimePayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"last_seen_time"));
    public static final StreamCodec<FriendlyByteBuf,LastSeenTimePayload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_LONG,LastSeenTimePayload::time,LastSeenTimePayload::new);
    @Override public Type<LastSeenTimePayload> type(){return TYPE;}
}
