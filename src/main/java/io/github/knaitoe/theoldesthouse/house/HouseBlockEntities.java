package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class HouseBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, TheOldestHouse.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HotelRoomPlaqueBlockEntity>> HOTEL_ROOM_PLAQUE =
            BLOCK_ENTITIES.register("hotel_room_plaque", () ->
                    BlockEntityType.Builder.of(
                            HotelRoomPlaqueBlockEntity::new,
                            HouseBlocks.HOTEL_ROOM_PLAQUE.get()
                    ).build(null));

    private HouseBlockEntities() {}

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
    }
}
