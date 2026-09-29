package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * A transition the server announced will not happen after all: the client
 * forgets its presentation context at once rather than holding it until
 * it times out.
 */
public record HouseTransitionCancelPayload(int token) implements CustomPacketPayload {
    public static final Type<HouseTransitionCancelPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(
                    TheOldestHouse.MOD_ID,
                    "transition_cancel"
            )
    );

    public static final StreamCodec<FriendlyByteBuf, HouseTransitionCancelPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    HouseTransitionCancelPayload::token,
                    HouseTransitionCancelPayload::new
            );

    @Override
    public Type<HouseTransitionCancelPayload> type() {
        return TYPE;
    }
}
