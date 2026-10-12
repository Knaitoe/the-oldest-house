package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Everything the house at the bottom of the lake registers. Hooked from {@link TheOldestHouse} with one line. */
public final class LakeHouseRegistry {
    private static final DeferredRegister<EntityType<?>> TYPES=DeferredRegister.create(Registries.ENTITY_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(BuiltInRegistries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);

    /** The lent green canoe: a vanilla boat's size and handling. */
    public static final DeferredHolder<EntityType<?>,EntityType<LakeHouseCanoe>> CANOE=TYPES.register("lake_house_canoe",
            ()->EntityType.Builder.<LakeHouseCanoe>of(LakeHouseCanoe::new,MobCategory.MISC).sized(1.375F,.5625F).clientTrackingRange(10).build("lake_house_canoe"));

    /** The lent gear (see {@link LakeHouseGear}). The tanks' durability is their air: a full fill lasts a minute under water. */
    public static final DeferredItem<LakeHouseGear.Lent> FLASHLIGHT=ITEMS.register("lake_house_flashlight",()->new LakeHouseGear.Lent(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<LakeHouseGear.Worn> HELMET=ITEMS.register("lake_house_helmet",()->new LakeHouseGear.Worn(EquipmentSlot.HEAD,new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<LakeHouseGear.Worn> TANKS=ITEMS.register("lake_house_tanks",()->new LakeHouseGear.Worn(EquipmentSlot.CHEST,new Item.Properties().durability(LakeHouseDive.TANK_AIR)));

    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<LakeHouseCompressorBlock>> COMPRESSOR_TYPE=CODECS.register("lake_house_compressor",()->LakeHouseCompressorBlock.CODEC);
    /** The compressor on the raft. Scenery: no item, no drops, not to be broken. */
    public static final DeferredBlock<LakeHouseCompressorBlock> COMPRESSOR=BLOCKS.registerBlock("lake_house_compressor",LakeHouseCompressorBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).strength(-1,3600000).noOcclusion().noLootTable());

    /** Each subtitled: the compressor's engine, the canoe against the culvert, air into the tanks. */
    public static final DeferredHolder<SoundEvent,SoundEvent> COMPRESSOR_SOUND=sound("lake_house.compressor"),SCRAPE=sound("lake_house.scrape"),HISS=sound("lake_house.hiss");

    private LakeHouseRegistry(){}
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String id){return SOUNDS.register(id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,id)));}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);ITEMS.register(bus);TYPES.register(bus);SOUNDS.register(bus);}
}
