package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record HouseSightlineStatePayload(
        BlockPos origin,
        boolean revealed
) implements CustomPacketPayload {
    public static final Type<HouseSightlineStatePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(
                    TheOldestHouse.MOD_ID,
                    "sightline_state"
            )
    );

    public static final StreamCodec<FriendlyByteBuf, HouseSightlineStatePayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    HouseSightlineStatePayload::origin,
                    ByteBufCodecs.BOOL,
                    HouseSightlineStatePayload::revealed,
                    HouseSightlineStatePayload::new
            );

    @Override
    public Type<HouseSightlineStatePayload> type() {
        return TYPE;
    }
}
