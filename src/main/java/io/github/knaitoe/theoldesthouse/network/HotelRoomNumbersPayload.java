package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** The hotel base and this viewer's current apparent lap count. */
public record HotelRoomNumbersPayload(BlockPos base, int laps) implements CustomPacketPayload {
    public static final Type<HotelRoomNumbersPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, "hotel_room_numbers")
    );

    public static final StreamCodec<FriendlyByteBuf, HotelRoomNumbersPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    HotelRoomNumbersPayload::base,
                    ByteBufCodecs.VAR_INT,
                    HotelRoomNumbersPayload::laps,
                    HotelRoomNumbersPayload::new
            );

    @Override
    public Type<HotelRoomNumbersPayload> type() {
        return TYPE;
    }
}
