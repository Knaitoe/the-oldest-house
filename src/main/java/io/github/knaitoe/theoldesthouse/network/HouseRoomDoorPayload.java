package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Where the manor's door to the room between rooms is (its lower half), so
 * the client never predicts it opening onto the shelves behind it.
 */
public record HouseRoomDoorPayload(BlockPos door, boolean present) implements CustomPacketPayload {
    public static final Type<HouseRoomDoorPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, "room_door")
    );

    public static final StreamCodec<FriendlyByteBuf, HouseRoomDoorPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    HouseRoomDoorPayload::door,
                    ByteBufCodecs.BOOL,
                    HouseRoomDoorPayload::present,
                    HouseRoomDoorPayload::new
            );

    @Override
    public Type<HouseRoomDoorPayload> type() {
        return TYPE;
    }
}
