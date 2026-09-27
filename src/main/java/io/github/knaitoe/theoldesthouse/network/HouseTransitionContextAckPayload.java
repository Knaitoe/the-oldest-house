package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record HouseTransitionContextAckPayload(int token) implements CustomPacketPayload {
    public static final Type<HouseTransitionContextAckPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(
                    TheOldestHouse.MOD_ID,
                    "transition_context_ack"
            )
    );

    public static final StreamCodec<FriendlyByteBuf, HouseTransitionContextAckPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    HouseTransitionContextAckPayload::token,
                    HouseTransitionContextAckPayload::new
            );

    @Override
    public Type<HouseTransitionContextAckPayload> type() {
        return TYPE;
    }
}
