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
import net.minecraft.world.level.block.RotatedPillarBlock;
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
 *
 * <p>0.4.464: Calder Park. Its materials (the back wall's brick, the street's asphalt and centre line, the path's concrete flags, the
 * kerb, the trees' October leaves), its fittings ({@link CalderBlock}) and its painted signs ({@link CalderSigns}). Drawn by
 * tools/draw_calder_art.py and tools/draw_calder_signs.py.
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

    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    private static final java.util.Map<String,DeferredBlock<Block>> MATERIALS=new java.util.LinkedHashMap<>();
    /** Calder Falls' plain materials: brick, asphalt, concrete flags, kerbstone. */
    public static final java.util.List<String> MATERIAL_NAMES=java.util.List.of("brick","asphalt","paving","kerb");
    static{for(String name:MATERIAL_NAMES)MATERIALS.put(name,BLOCKS.registerSimpleBlock("calder_"+name,fixed(name.equals("brick")?Blocks.BRICKS:Blocks.STONE)));}
    public static Block material(String name){var block=MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    /** Asphalt with the street's centre line on it, along its axis. */
    public static final DeferredBlock<RotatedPillarBlock> ROAD_LINE=BLOCKS.registerBlock("calder_road_line",RotatedPillarBlock::new,fixed(Blocks.STONE));
    /** The trees' leaves in October. They do not fall, burn or grow back. */
    public static final DeferredBlock<Block> LEAVES=BLOCKS.registerSimpleBlock("calder_leaves",fixed(Blocks.OAK_LEAVES));
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<CalderBlock>> FITTING_TYPE=CODECS.register("calder_fitting",()->CalderBlock.CODEC);
    public static final DeferredBlock<CalderBlock> FITTING=BLOCKS.registerBlock("calder_fitting",CalderBlock::new,fixed(Blocks.IRON_BLOCK).noOcclusion().lightLevel(CalderBlock::light));
    /** Fallen leaves: nothing stops on them, and whatever is set down where they lie takes their place. */
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<CalderLitterBlock>> LITTER_TYPE=CODECS.register("calder_litter",()->CalderLitterBlock.CODEC);
    public static final DeferredBlock<CalderLitterBlock> LITTER=BLOCKS.registerBlock("calder_litter",CalderLitterBlock::new,
        BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).replaceable().noCollission().noOcclusion().noLootTable().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY));
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<CalderSignBlock>> SIGN_TYPE=CODECS.register("calder_sign",()->CalderSignBlock.CODEC);
    public static final DeferredBlock<CalderSignBlock> SIGN=BLOCKS.registerBlock("calder_sign",CalderSignBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(-1,3600000).noOcclusion().noCollission().noLootTable().lightLevel(CalderSignBlock::light));

    /** A night bike while somebody rides it (NightBike). Never saved: a bike nobody is on is a block. */
    public static final DeferredHolder<EntityType<?>,EntityType<NightBike>> BIKE=ENTITIES.register("night_bike",
        ()->EntityType.Builder.<NightBike>of(NightBike::new,MobCategory.MISC).sized(.6F,.9F).clientTrackingRange(8).updateInterval(1).noSave().noSummon().build("night_bike"));

    private static DeferredHolder<SoundEvent,SoundEvent> sound(String id){return SOUNDS.register(id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,id)));}
    /** Any bike: the tyres letting go of the road, and both of them coming back down. */
    public static final DeferredHolder<SoundEvent,SoundEvent> HOP=sound("bike.hop"),LAND=sound("bike.land");
    /** The Rattler's card in its spokes and the burst it gives; the Lamp's dynamo against its tyre. */
    public static final DeferredHolder<SoundEvent,SoundEvent> CARD=sound("bike.card"),TURBO=sound("bike.turbo"),DYNAMO=sound("bike.dynamo");
    /** The park: the failing lamp striking and buzzing as it comes back on; the chain on the gate when a hand tries it. */
    public static final DeferredHolder<SoundEvent,SoundEvent> LAMP_BUZZ=sound("park.lamp_buzz"),CHAIN=sound("park.chain");

    private BikeNightRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);ENTITIES.register(bus);SOUNDS.register(bus);}
}
