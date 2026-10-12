package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Everything the house at the bottom of the lake registers. Hooked from {@link TheOldestHouse} with one line. */
public final class LakeHouseRegistry {
    private static final DeferredRegister<EntityType<?>> TYPES=DeferredRegister.create(Registries.ENTITY_TYPE,TheOldestHouse.MOD_ID);
    /** The lent green canoe: a vanilla boat's size and handling. */
    public static final DeferredHolder<EntityType<?>,EntityType<LakeHouseCanoe>> CANOE=TYPES.register("lake_house_canoe",
            ()->EntityType.Builder.<LakeHouseCanoe>of(LakeHouseCanoe::new,MobCategory.MISC).sized(1.375F,.5625F).clientTrackingRange(10).build("lake_house_canoe"));
    private LakeHouseRegistry(){}
    public static void register(IEventBus bus){TYPES.register(bus);}
}
