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
 *
 * <p>0.4.465: the playground's equipment ({@link CalderPlayBlock}, drawn by tools/draw_calder_play_art.py), and the night's one item,
 * a ball of sand.
 */
public final class BikeNightRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<EntityType<?>> ENTITIES=DeferredRegister.create(Registries.ENTITY_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<net.minecraft.world.level.block.entity.BlockEntityType<?>> BLOCK_ENTITIES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,TheOldestHouse.MOD_ID);

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
    public static final java.util.List<String> MATERIAL_NAMES=java.util.List.of("brick","asphalt","paving","kerb",
        // 0.4.481, the town's: stucco, tan brick, clapboard, concrete, a tarred roof, shingles, a lot's asphalt and its bay line, porch
        // boards, an alley's gravel, the school field's turf, plain, with a tyre's track through its dew and with a yard line chalked
        // down it, and a hedge.
        "stucco","tanbrick","siding","concrete","roofing","shingle","lot","lot_line","plank","gravel","turf","turf_track","turf_line","hedge");
    private static Block like(String name){return switch(name){case "brick","tanbrick"->Blocks.BRICKS;case "siding","plank"->Blocks.OAK_PLANKS;case "gravel"->Blocks.GRAVEL;case "turf","turf_track","turf_line","hedge"->Blocks.MOSS_BLOCK;default->Blocks.STONE;};}
    static{for(String name:MATERIAL_NAMES)MATERIALS.put(name,BLOCKS.registerSimpleBlock("calder_"+name,fixed(like(name))));}
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
    /** 0.4.465: the playground's equipment (CalderPlayBlock). This slice, the swings: a steel frame, chains and belt seats. */
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<CalderPlayBlock>> PLAY_TYPE=CODECS.register("calder_play",()->CalderPlayBlock.CODEC);
    /** 0.4.481: the town's fittings (CalderTownBlock), and a place of the bank's clock. */
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<CalderTownBlock>> TOWN_TYPE=CODECS.register("calder_town",()->CalderTownBlock.CODEC);
    public static final DeferredBlock<CalderTownBlock> TOWN=BLOCKS.registerBlock("calder_town",CalderTownBlock::new,fixed(Blocks.IRON_BLOCK).noOcclusion().lightLevel(CalderTownBlock::light));
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<CalderDigitBlock>> DIGIT_TYPE=CODECS.register("calder_digit",()->CalderDigitBlock.CODEC);
    public static final DeferredBlock<CalderDigitBlock> DIGIT=BLOCKS.registerBlock("calder_digit",CalderDigitBlock::new,fixed(Blocks.IRON_BLOCK).noOcclusion().lightLevel(s->7));
    public static final DeferredBlock<CalderPlayBlock> PLAY=BLOCKS.registerBlock("calder_play",CalderPlayBlock::new,fixed(Blocks.IRON_BLOCK).noOcclusion());
    /** A heel mark dug into the sand where the furthest jump off the swings came down. It gives way as fallen leaves do, and is the same kind of block. */
    public static final DeferredBlock<CalderLitterBlock> MARK=BLOCKS.registerBlock("calder_mark",CalderLitterBlock::new,
        BlockBehaviour.Properties.of().mapColor(MapColor.SAND).replaceable().noCollission().noOcclusion().noLootTable().instabreak().sound(SoundType.SAND).pushReaction(PushReaction.DESTROY));
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<CalderSignBlock>> SIGN_TYPE=CODECS.register("calder_sign",()->CalderSignBlock.CODEC);
    public static final DeferredBlock<CalderSignBlock> SIGN=BLOCKS.registerBlock("calder_sign",CalderSignBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(-1,3600000).noOcclusion().noCollission().noLootTable().lightLevel(CalderSignBlock::light));

    /** A night bike while somebody rides it (NightBike). Never saved: a bike nobody is on is a block. */
    public static final DeferredHolder<EntityType<?>,EntityType<NightBike>> BIKE=ENTITIES.register("night_bike",
        ()->EntityType.Builder.<NightBike>of(NightBike::new,MobCategory.MISC).sized(.6F,.9F).clientTrackingRange(8).updateInterval(1).noSave().noSummon().build("night_bike"));

    /** 0.4.479: a bike ridden by somebody who is not a reader (PackBike). Never saved. */
    public static final DeferredHolder<EntityType<?>,EntityType<PackBike>> PACK_BIKE=ENTITIES.register("pack_bike",
        ()->EntityType.Builder.<PackBike>of(PackBike::new,MobCategory.MISC).sized(.6F,.9F).clientTrackingRange(8).updateInterval(1).noSave().noSummon().build("pack_bike"));

    /** 0.4.484: one of the town's cars (TownCar): a body the night's traffic moves. Never saved. */
    public static final DeferredHolder<EntityType<?>,EntityType<TownCar>> TOWN_CAR=ENTITIES.register("town_car",
        ()->EntityType.Builder.<TownCar>of(TownCar::new,MobCategory.MISC).sized(1.9F,1.45F).clientTrackingRange(10).updateInterval(1).noSave().noSummon().build("town_car"));

    /**
     * 0.4.465: a ball of the sandpit's sand, packed by hand and thrown (SandBallItem, SandBall). The night's one item: it runs out
     * through the fingers of anyone who carries it out of the park.
     */
    public static final net.neoforged.neoforge.registries.DeferredItem<SandBallItem> SAND_BALL=ITEMS.register("sand_ball",()->new SandBallItem(new net.minecraft.world.item.Item.Properties().stacksTo(SandBallItem.MOST)));
    public static final DeferredHolder<EntityType<?>,EntityType<SandBall>> SAND_BALL_THROWN=ENTITIES.register("sand_ball",
        ()->EntityType.Builder.<SandBall>of(SandBall::new,MobCategory.MISC).sized(.25F,.25F).clientTrackingRange(6).updateInterval(10).noSave().build("sand_ball"));

    /** 0.4.466: a go down the slide (SlideRide). Never saved; nothing is drawn of it. */
    public static final DeferredHolder<EntityType<?>,EntityType<SlideRide>> SLIDE_RIDE=ENTITIES.register("slide_ride",
        ()->EntityType.Builder.<SlideRide>of(SlideRide::new,MobCategory.MISC).sized(.05F,.05F).clientTrackingRange(8).updateInterval(20).noSave().noSummon().build("slide_ride"));

    /** 0.4.467: a reader's hold on the monkey bars (BarsHang). Never saved; nothing is drawn of it. It is sent every tick: it is moved by hand. */
    public static final DeferredHolder<EntityType<?>,EntityType<BarsHang>> BARS_HANG=ENTITIES.register("bars_hang",
        ()->EntityType.Builder.<BarsHang>of(BarsHang::new,MobCategory.MISC).sized(.05F,.05F).clientTrackingRange(8).updateInterval(1).noSave().noSummon().build("bars_hang"));

    /**
     * 0.4.468: the playground's seesaw ({@link SeesawBlock}): its stand, which keeps the board, and the places its ends are over; a
     * rider's seat on an end (SeesawSeat), never saved, of which nothing is drawn.
     */
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<SeesawBlock>> SEESAW_TYPE=CODECS.register("calder_seesaw",()->SeesawBlock.CODEC);
    public static final DeferredBlock<SeesawBlock> SEESAW=BLOCKS.registerBlock("calder_seesaw",SeesawBlock::new,fixed(Blocks.IRON_BLOCK).noOcclusion());
    public static final DeferredHolder<net.minecraft.world.level.block.entity.BlockEntityType<?>,net.minecraft.world.level.block.entity.BlockEntityType<SeesawBlock.Board>> SEESAW_BOARD=BLOCK_ENTITIES.register("calder_seesaw",
        ()->net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(SeesawBlock.Board::new,SEESAW.get()).build(null));
    public static final DeferredHolder<EntityType<?>,EntityType<SeesawSeat>> SEESAW_SEAT=ENTITIES.register("seesaw_seat",
        ()->EntityType.Builder.<SeesawSeat>of(SeesawSeat::new,MobCategory.MISC).sized(.05F,.05F).clientTrackingRange(8).updateInterval(20).noSave().noSummon().build("seesaw_seat"));

    /** 0.4.469: the playground's merry-go-round ({@link RoundaboutBlock}): the ground its deck is over, and the wheel its hub keeps. */
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<RoundaboutBlock>> ROUNDABOUT_TYPE=CODECS.register("calder_roundabout",()->RoundaboutBlock.CODEC);
    public static final DeferredBlock<RoundaboutBlock> ROUNDABOUT=BLOCKS.registerBlock("calder_roundabout",RoundaboutBlock::new,fixed(Blocks.IRON_BLOCK).noOcclusion());
    public static final DeferredHolder<net.minecraft.world.level.block.entity.BlockEntityType<?>,net.minecraft.world.level.block.entity.BlockEntityType<RoundaboutBlock.Wheel>> ROUNDABOUT_WHEEL=BLOCK_ENTITIES.register("calder_roundabout",
        ()->net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(RoundaboutBlock.Wheel::new,ROUNDABOUT.get()).build(null));

    /** 0.4.471: the playground's spring rider ({@link SpringBlock}): its foot, which keeps the rocking; and a rider's seat on it (SpringSeat). */
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<SpringBlock>> SPRING_TYPE=CODECS.register("calder_spring",()->SpringBlock.CODEC);
    public static final DeferredBlock<SpringBlock> SPRING_RIDER=BLOCKS.registerBlock("calder_spring",SpringBlock::new,fixed(Blocks.IRON_BLOCK).noOcclusion());
    public static final DeferredHolder<net.minecraft.world.level.block.entity.BlockEntityType<?>,net.minecraft.world.level.block.entity.BlockEntityType<SpringBlock.Rocker>> SPRING_ROCKER=BLOCK_ENTITIES.register("calder_spring",
        ()->net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(SpringBlock.Rocker::new,SPRING_RIDER.get()).build(null));
    public static final DeferredHolder<EntityType<?>,EntityType<SpringSeat>> SPRING_SEAT=ENTITIES.register("spring_seat",
        ()->EntityType.Builder.<SpringSeat>of(SpringSeat::new,MobCategory.MISC).sized(.05F,.05F).clientTrackingRange(8).updateInterval(20).noSave().noSummon().build("spring_seat"));

    /** 0.4.471: the playground's climbing dome ({@link DomeBlock}): its ground's cells, and the middle one's block entity, which is there to be drawn. */
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<DomeBlock>> DOME_TYPE=CODECS.register("calder_dome",()->DomeBlock.CODEC);
    public static final DeferredBlock<DomeBlock> DOME=BLOCKS.registerBlock("calder_dome",DomeBlock::new,fixed(Blocks.IRON_BLOCK).noOcclusion());
    public static final DeferredHolder<net.minecraft.world.level.block.entity.BlockEntityType<?>,net.minecraft.world.level.block.entity.BlockEntityType<DomeBlock.Bars>> DOME_BARS=BLOCK_ENTITIES.register("calder_dome",
        ()->net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(DomeBlock.Bars::new,DOME.get()).build(null));

    private static DeferredHolder<SoundEvent,SoundEvent> sound(String id){return SOUNDS.register(id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,id)));}
    /** Any bike: the tyres letting go of the road, and both of them coming back down. */
    public static final DeferredHolder<SoundEvent,SoundEvent> HOP=sound("bike.hop"),LAND=sound("bike.land");
    /** 0.4.479: a freewheel ticking under somebody coasting; a car going by on Park Street; the gate's padlock coming open; somebody coughing sand; sand kicked. */
    public static final DeferredHolder<SoundEvent,SoundEvent> FREEWHEEL=sound("bike.freewheel"),CAR_PASS=sound("park.car_pass"),PADLOCK=sound("park.padlock"),COUGH=sound("park.cough"),SAND_KICK=sound("park.sand_kick");
    /** The Rattler's card in its spokes and the burst it gives; the Lamp's dynamo against its tyre. */
    public static final DeferredHolder<SoundEvent,SoundEvent> CARD=sound("bike.card"),TURBO=sound("bike.turbo"),DYNAMO=sound("bike.dynamo");
    /** The park: the failing lamp striking and buzzing as it comes back on; the chain on the gate when a hand tries it. */
    public static final DeferredHolder<SoundEvent,SoundEvent> LAMP_BUZZ=sound("park.lamp_buzz"),CHAIN=sound("park.chain");
    /** The playground: a swing's chains at the bottom of its arc; sand packed in a hand, thrown, and bursting where it lands. */
    public static final DeferredHolder<SoundEvent,SoundEvent> SWING_CREAK=sound("park.swing_creak"),SAND_SCOOP=sound("park.sand_scoop"),SAND_THROW=sound("park.sand_throw"),SAND_BURST=sound("park.sand_burst");
    /** A body going down the slide's chute. */
    public static final DeferredHolder<SoundEvent,SoundEvent> SLIDE=sound("park.slide");
    /** A hand coming down on a rung of the monkey bars. */
    public static final DeferredHolder<SoundEvent,SoundEvent> BARS=sound("park.bars");
    /** The seesaw: its axle as a rider pushes off the sand, and a bumper coming down on it. */
    public static final DeferredHolder<SoundEvent,SoundEvent> SEESAW_CREAK=sound("park.seesaw_creak"),SEESAW_THUD=sound("park.seesaw_thud");
    /** The drinking fountain running; the payphone's dial tone, and its handset going back on the hook. */
    public static final DeferredHolder<SoundEvent,SoundEvent> FOUNTAIN=sound("park.fountain"),PHONE_TONE=sound("park.phone_tone"),PHONE_HOOK=sound("park.phone_hook");
    /** The spring rider's spring, as it comes through the middle. */
    public static final DeferredHolder<SoundEvent,SoundEvent> SPRING=sound("park.spring");
    /** The merry-go-round's bearing, once a quarter of a turn. */
    public static final DeferredHolder<SoundEvent,SoundEvent> ROUNDABOUT_SQUEAK=sound("park.roundabout_squeak");
    /** The hunt (0.4.482): somebody landing in dry leaves, and the three laughing as they go. */
    public static final DeferredHolder<SoundEvent,SoundEvent> LEAVES_FALL=sound("town.leaves"),LAUGH=sound("town.laugh");
    /** 0.4.484, the cars: an engine going by, and each car's own horn, in TownCar's order. */
    public static final DeferredHolder<SoundEvent,SoundEvent> ENGINE=sound("town.engine");
    public static final java.util.List<DeferredHolder<SoundEvent,SoundEvent>> HORNS=java.util.List.of(sound("town.horn_wagon"),sound("town.horn_pickup"),sound("town.horn_sedan"),sound("town.horn_muscle"));

    /** 0.4.486, the town's own: the laundromat's dryers, the neon's bad letter, the grocery's lights switched off, the theater's house laughing, a car radio. */
    public static final DeferredHolder<SoundEvent,SoundEvent> DRYERS=sound("town.dryers"),NEON=sound("town.neon"),BREAKER=sound("town.breaker"),LAUGHTER=sound("town.laughter"),RADIO=sound("town.radio");

    private BikeNightRegistry(){}
    /** 0.4.485: the pillowcase a trick-or-treater carries. Nobody is given one. */
    public static final net.neoforged.neoforge.registries.DeferredItem<net.minecraft.world.item.Item> TREAT_SACK=ITEMS.register("treat_sack",()->new net.minecraft.world.item.Item(new net.minecraft.world.item.Item.Properties().stacksTo(1)));
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);ITEMS.register(bus);BLOCK_ENTITIES.register(bus);ENTITIES.register(bus);SOUNDS.register(bus);}
}
