package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 0.4.463 (docs/BIKE_NIGHT_PROPOSAL.md): Bike night's own things. This slice: the six bikes (a block while nobody rides one, an entity
 * while somebody does), the rack they stand in, the plank ramps, the light the Lamp throws ahead of itself, and the bikes' sounds.
 * Drawn by tools/draw_bike_night_art.py, the sounds made by tools/generate_bike_night_audio.py. None of them is an item.
 */
public final class BikeNightRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<EntityType<?>> ENTITIES=DeferredRegister.create(Registries.ENTITY_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<NightBikeBlock>> BIKE_TYPE=CODECS.register("night_bike",()->NightBikeBlock.CODEC);
    public static final DeferredBlock<NightBikeBlock> BIKE_BLOCK=BLOCKS.registerBlock("night_bike",NightBikeBlock::new,
        BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1F,3F).sound(SoundType.METAL).noOcclusion().noLootTable().pushReaction(PushReaction.BLOCK));
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<BikeRackBlock>> RACK_TYPE=CODECS.register("bike_rack",()->BikeRackBlock.CODEC);
    public static final DeferredBlock<BikeRackBlock> RACK=BLOCKS.registerBlock("bike_rack",BikeRackBlock::new,
        BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3F,6F).sound(SoundType.METAL).noOcclusion().noLootTable().pushReaction(PushReaction.BLOCK));
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<BikeRampBlock>> RAMP_TYPE=CODECS.register("bike_ramp",()->BikeRampBlock.CODEC);
    public static final DeferredBlock<BikeRampBlock> RAMP=BLOCKS.registerBlock("bike_ramp",BikeRampBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion().noLootTable().pushReaction(PushReaction.BLOCK));
    /** The light a moving Lamp throws ahead of itself: nothing to see or touch, and gone a moment after the bike is (BikeBeamBlock). */
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<BikeBeamBlock>> BEAM_TYPE=CODECS.register("bike_beam",()->BikeBeamBlock.CODEC);
    public static final DeferredBlock<BikeBeamBlock> BEAM=BLOCKS.registerBlock("bike_beam",BikeBeamBlock::new,
        BlockBehaviour.Properties.of().mapColor(MapColor.NONE).replaceable().noCollission().noOcclusion().noLootTable().instabreak().lightLevel(s->s.getValue(BikeBeamBlock.LEVEL)).pushReaction(PushReaction.DESTROY));

    /** A night bike while somebody rides it (NightBike). Never saved: a bike nobody is on is a block. */
    public static final DeferredHolder<EntityType<?>,EntityType<NightBike>> BIKE=ENTITIES.register("night_bike",
        ()->EntityType.Builder.<NightBike>of(NightBike::new,MobCategory.MISC).sized(.6F,.9F).clientTrackingRange(8).updateInterval(1).noSave().noSummon().build("night_bike"));

    private static DeferredHolder<SoundEvent,SoundEvent> sound(String id){return SOUNDS.register(id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,id)));}
    /** Any bike: the tyres letting go of the road, and both of them coming back down. */
    public static final DeferredHolder<SoundEvent,SoundEvent> HOP=sound("bike.hop"),LAND=sound("bike.land");
    /** The Rattler's card in its spokes and the burst it gives; the Lamp's dynamo against its tyre. */
    public static final DeferredHolder<SoundEvent,SoundEvent> CARD=sound("bike.card"),TURBO=sound("bike.turbo"),DYNAMO=sound("bike.dynamo");

    private BikeNightRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);ENTITIES.register(bus);SOUNDS.register(bus);}
}
