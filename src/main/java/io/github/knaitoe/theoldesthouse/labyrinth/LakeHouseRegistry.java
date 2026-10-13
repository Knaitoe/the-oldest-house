package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Display;
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

    public static final DeferredHolder<EntityType<?>,EntityType<LakeHouseDress>> DRESS=TYPES.register("lake_house_dress",()->EntityType.Builder.<LakeHouseDress>of(LakeHouseDress::new,MobCategory.MISC).sized(.1F,.1F).clientTrackingRange(8).updateInterval(2).build("lake_house_dress"));
    public static final DeferredHolder<EntityType<?>,EntityType<LakeHouseSeat>> SEAT=TYPES.register("lake_house_seat",()->EntityType.Builder.<LakeHouseSeat>of(LakeHouseSeat::new,MobCategory.MISC).sized(.1F,.1F).clientTrackingRange(8).build("lake_house_seat"));
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<LakeHouseFittingBlock>> FITTING_TYPE=CODECS.register("lake_house_fitting",()->LakeHouseFittingBlock.CODEC);
    public static final DeferredBlock<LakeHouseFittingBlock> FITTING=BLOCKS.registerBlock("lake_house_fitting",LakeHouseFittingBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(-1,3600000).noOcclusion().noLootTable().lightLevel(s->{var k=s.getValue(LakeHouseFittingBlock.KIND);return k==LakeHouseFittingBlock.Kind.CANDLE||k==LakeHouseFittingBlock.Kind.HEARTH?7:s.getValue(LakeHouseFittingBlock.LIT)&&(k==LakeHouseFittingBlock.Kind.LAMP||k==LakeHouseFittingBlock.Kind.BULB)?14:0;}));
    public static final DeferredBlock<Block> FLOOR=material("lake_house_floor",Blocks.OAK_PLANKS),WALL=material("lake_house_wall",Blocks.BRICKS),POOL_STONE=material("lake_house_pool_stone",Blocks.STONE_BRICKS),CEDAR=material("lake_house_cedar",Blocks.SPRUCE_PLANKS);
    public static final DeferredBlock<net.minecraft.world.level.block.StairBlock> RED_STAIR=BLOCKS.registerBlock("lake_house_red_stair",p->new net.minecraft.world.level.block.StairBlock(Blocks.DARK_OAK_PLANKS.defaultBlockState(),p),BlockBehaviour.Properties.ofFullCopy(Blocks.DARK_OAK_STAIRS).noLootTable());
    public static final DeferredItem<LakeHouseArtifactItem> FOUND_FLASHLIGHT=ITEMS.register("lake_found_flashlight",()->new LakeHouseArtifactItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE),LakeHouseArtifactItem.FLASHLIGHT));
    public static final DeferredItem<LakeHouseArtifactItem> HOUSE_LIGHT=ITEMS.register("lake_house_light",()->new LakeHouseArtifactItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE),LakeHouseArtifactItem.LIGHT));
    public static final DeferredItem<LakeHouseArtifactItem> GOLD_HELMET=ITEMS.register("lake_gold_helmet",()->new LakeHouseArtifactItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE),LakeHouseArtifactItem.HELMET));

    /** The lent gear (see {@link LakeHouseGear}). The tanks' durability is their air: a full fill lasts a minute under water. */
    public static final DeferredItem<LakeHouseGear.Lent> FLASHLIGHT=ITEMS.register("lake_house_flashlight",()->new LakeHouseGear.Lent(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<LakeHouseGear.Worn> HELMET=ITEMS.register("lake_house_helmet",()->new LakeHouseGear.Worn(EquipmentSlot.HEAD,new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<LakeHouseGear.Worn> TANKS=ITEMS.register("lake_house_tanks",()->new LakeHouseGear.Worn(EquipmentSlot.CHEST,new Item.Properties().durability(LakeHouseDive.TANK_AIR)));

    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<LakeHouseCompressorBlock>> COMPRESSOR_TYPE=CODECS.register("lake_house_compressor",()->LakeHouseCompressorBlock.CODEC);
    /** The compressor on the raft. Scenery: no item, no drops, not to be broken. */
    public static final DeferredBlock<LakeHouseCompressorBlock> COMPRESSOR=BLOCKS.registerBlock("lake_house_compressor",LakeHouseCompressorBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).strength(-1,3600000).noOcclusion().noLootTable());

    private static final DeferredRegister<net.minecraft.world.level.block.entity.BlockEntityType<?>> BES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<net.minecraft.core.particles.ParticleType<?>> PARTICLES=DeferredRegister.create(Registries.PARTICLE_TYPE,TheOldestHouse.MOD_ID);
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<LakeHouseLightBlock>> HOME_LIGHT_TYPE=CODECS.register("lake_house_home_light",()->LakeHouseLightBlock.CODEC);
    public static final DeferredBlock<LakeHouseLightBlock> HOME_LIGHT=BLOCKS.registerBlock("lake_house_home_light",LakeHouseLightBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.LANTERN).noLootTable().lightLevel(s->s.getValue(LakeHouseLightBlock.LIT)?14:0));
    public static final DeferredHolder<net.minecraft.world.level.block.entity.BlockEntityType<?>,net.minecraft.world.level.block.entity.BlockEntityType<LakeHouseLightBlock.Lamp>> HOME_LIGHT_ENTITY=BES.register("lake_house_home_light",()->net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(LakeHouseLightBlock.Lamp::new,HOME_LIGHT.get()).build(null));
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>,net.minecraft.core.particles.SimpleParticleType> STILL_BUBBLE=PARTICLES.register("lake_house_bubble",()->new net.minecraft.core.particles.SimpleParticleType(false));
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>,net.minecraft.core.particles.SimpleParticleType> WET_FOOTPRINT=PARTICLES.register("lake_wet_footprint",()->new net.minecraft.core.particles.SimpleParticleType(false));
    /** Each subtitled: the compressor's engine, the canoe against the culvert, air into the tanks. */
    public static final DeferredHolder<SoundEvent,SoundEvent> COMPRESSOR_SOUND=sound("lake_house.compressor"),SCRAPE=sound("lake_house.scrape"),HISS=sound("lake_house.hiss");

    private static DeferredBlock<Block> material(String id,Block base){return BLOCKS.registerBlock(id,Block::new,BlockBehaviour.Properties.ofFullCopy(base).noLootTable());}
    private LakeHouseRegistry(){}
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String id){return SOUNDS.register(id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,id)));}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);ITEMS.register(bus);TYPES.register(bus);SOUNDS.register(bus);BES.register(bus);PARTICLES.register(bus);}
}
