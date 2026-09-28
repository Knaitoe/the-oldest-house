package io.github.knaitoe.theoldesthouse.opening;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** Registry entries for the opening sequence. */
public final class OpeningRegistry {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, TheOldestHouse.MOD_ID);
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, TheOldestHouse.MOD_ID);

    public static final DeferredBlock<EntranceDoorBlock> ENTRANCE_DOOR =
            BLOCKS.register("entrance_door", () -> new EntranceDoorBlock(EntranceDoorBlock.properties()));

    public static final DeferredItem<DoubleHighBlockItem> ENTRANCE_DOOR_ITEM =
            ITEMS.register("entrance_door", () -> new DoubleHighBlockItem(ENTRANCE_DOOR.get(), new Item.Properties()));

    public static final DeferredHolder<EntityType<?>, EntityType<DeliveredItemEntity>> DELIVERED_ITEM =
            ENTITY_TYPES.register("delivered_item", () -> EntityType.Builder
                    .<DeliveredItemEntity>of(DeliveredItemEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .eyeHeight(0.2125F)
                    .clientTrackingRange(6)
                    .updateInterval(20)
                    .build("delivered_item"));

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<OpeningPlayerState>> PLAYER_STATE =
            ATTACHMENT_TYPES.register("opening", () -> AttachmentType
                    .builder(() -> new OpeningPlayerState())
                    .serialize(OpeningPlayerState.CODEC)
                    .copyOnDeath()
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<HillaryTag>> HILLARY =
            ATTACHMENT_TYPES.register("hillary", () -> AttachmentType
                    .builder(() -> HillaryTag.NONE)
                    .serialize(HillaryTag.CODEC)
                    .build());

    private OpeningRegistry() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
        ATTACHMENT_TYPES.register(modEventBus);
    }
}
