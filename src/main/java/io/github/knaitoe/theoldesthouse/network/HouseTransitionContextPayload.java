package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseTransitionKind;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record HouseTransitionContextPayload(int kindId) implements CustomPacketPayload {
    public static final Type<HouseTransitionContextPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(
                    TheOldestHouse.MOD_ID,
                    "transition_context"
            )
    );

    public static final StreamCodec<FriendlyByteBuf, HouseTransitionContextPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    HouseTransitionContextPayload::kindId,
                    HouseTransitionContextPayload::new
            );

    public HouseTransitionContextPayload(HouseTransitionKind kind) {
        this(kind.ordinal());
    }

    public HouseTransitionKind kind() {
        return HouseTransitionKind.fromId(kindId);
    }

    @Override
    public Type<HouseTransitionContextPayload> type() {
        return TYPE;
    }
}
