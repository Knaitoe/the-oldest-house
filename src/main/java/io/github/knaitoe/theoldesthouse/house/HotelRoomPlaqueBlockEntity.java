package io.github.knaitoe.theoldesthouse.house;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Hotel plaques. In the numbered corridor the number is viewer-local, because two players can see different numbers on the
 * same physical plaque, and the plaque carries none. The hotel's own guest corridor (0.4.105) has fixed numbers, carried here.
 */
public final class HotelRoomPlaqueBlockEntity extends BlockEntity {
    private int number;

    public HotelRoomPlaqueBlockEntity(BlockPos pos, BlockState state) {
        super(HouseBlockEntities.HOTEL_ROOM_PLAQUE.get(), pos, state);
    }

    /** The fixed number on this plaque, or 0 where the corridor numbers it for each viewer. */
    public int number() {
        return number;
    }

    /** Sets a hotel plaque's fixed number and shows it to everyone who has the plaque loaded. */
    public static void number(ServerLevel level, BlockPos pos, int number) {
        if (level.getBlockEntity(pos) instanceof HotelRoomPlaqueBlockEntity plaque && plaque.number != number) {
            plaque.number = number;
            plaque.setChanged();
            level.sendBlockUpdated(pos, plaque.getBlockState(), plaque.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        number = tag.getInt("Number");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (number > 0) tag.putInt("Number", number);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
