package io.github.knaitoe.theoldesthouse.house;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Marker block entity for hotel plaques. The actual number is intentionally
 * viewer-local, because two players can see different numbers on the same
 * physical plaque.
 */
public final class HotelRoomPlaqueBlockEntity extends BlockEntity {
    public HotelRoomPlaqueBlockEntity(BlockPos pos, BlockState state) {
        super(HouseBlockEntities.HOTEL_ROOM_PLAQUE.get(), pos, state);
    }
}
