package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/** 0.4.199: the puzzle rooms' working parts: Borrowed Light's (tools/draw_borrowed_light.py), and the lifts' and landings' (tools/draw_puzzle_fittings.py). */
public final class PuzzleRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,TheOldestHouse.MOD_ID);
    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    private static final java.util.Map<String,DeferredBlock<Block>> MATERIALS=new java.util.LinkedHashMap<>();
    /** Borrowed Light's dressing room: its papered wall, dark boards and plaster ceiling. */
    public static final java.util.List<String> MATERIAL_NAMES=java.util.List.of("wall","floor","ceiling");
    static{for(String name:MATERIAL_NAMES)MATERIALS.put(name,BLOCKS.registerSimpleBlock("borrowed_light_"+name,fixed(Blocks.SPRUCE_PLANKS)));}
    public static Block material(String name){var block=MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<BorrowedLightBlock>> BORROWED_LIGHT_TYPE=CODECS.register("borrowed_light",()->BorrowedLightBlock.CODEC);
    public static final DeferredBlock<BorrowedLightBlock> BORROWED_LIGHT=BLOCKS.registerBlock("borrowed_light",BorrowedLightBlock::new,fixed(Blocks.SPRUCE_PLANKS).noOcclusion().lightLevel(BorrowedLightBlock::light));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<BorrowedLightBlock.Beam>> BEAM=BES.register("borrowed_light_beam",()->BlockEntityType.Builder.of(BorrowedLightBlock.Beam::new,BORROWED_LIGHT.get()).build(null));
    /** 0.4.199: the lifts' and landings' working parts (PuzzleFittingBlock), drawn by tools/draw_puzzle_fittings.py. */
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<PuzzleFittingBlock>> FITTING_TYPE=CODECS.register("puzzle_fitting",()->PuzzleFittingBlock.CODEC);
    public static final DeferredBlock<PuzzleFittingBlock> FITTING=BLOCKS.registerBlock("puzzle_fitting",PuzzleFittingBlock::new,fixed(Blocks.DARK_OAK_PLANKS).noOcclusion());
    /** 0.4.230: the draft rooms' distempered plaster, scrubbed boards and scullery quarry tiles, and their working parts (DraftBlock), drawn by tools/draw_draft_fittings.py. */
    private static final java.util.Map<String,DeferredBlock<Block>> DRAFT_MATERIALS=new java.util.LinkedHashMap<>();
    public static final java.util.List<String> DRAFT_MATERIAL_NAMES=java.util.List.of("plaster","boards","tile");
    static{for(String name:DRAFT_MATERIAL_NAMES)DRAFT_MATERIALS.put(name,BLOCKS.registerSimpleBlock("draft_"+name,fixed(name.equals("tile")?Blocks.STONE:Blocks.SPRUCE_PLANKS)));}
    public static Block draftMaterial(String name){var block=DRAFT_MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<DraftBlock>> DRAFT_TYPE=CODECS.register("draft_fitting",()->DraftBlock.CODEC);
    public static final DeferredBlock<DraftBlock> DRAFT=BLOCKS.registerBlock("draft_fitting",DraftBlock::new,fixed(Blocks.SPRUCE_PLANKS).noOcclusion());
    /** 0.4.250: the Folding Rooms' herringbone parquet, the floor and ceiling tracks (plain and worn), the worn marks of furniture, panelling, paper and plaster, and their working parts (FoldBlock), drawn by tools/draw_fold_fittings.py. */
    private static final java.util.Map<String,DeferredBlock<Block>> FOLD_MATERIALS=new java.util.LinkedHashMap<>();
    public static final java.util.List<String> FOLD_MATERIAL_NAMES=java.util.List.of("parquet","track","track_worn","marks","panelling","paper","ceiling","ceiling_track");
    static{for(String name:FOLD_MATERIAL_NAMES)FOLD_MATERIALS.put(name,BLOCKS.registerSimpleBlock("fold_"+name,fixed(Blocks.OAK_PLANKS)));}
    public static Block foldMaterial(String name){var block=FOLD_MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<FoldBlock>> FOLD_TYPE=CODECS.register("fold_fitting",()->FoldBlock.CODEC);
    public static final DeferredBlock<FoldBlock> FOLD=BLOCKS.registerBlock("fold_fitting",FoldBlock::new,fixed(Blocks.OAK_PLANKS).noOcclusion());
    /** 0.4.251: the Window Cleaner's winch, brake, cradle and casement (WinchBlock), drawn by tools/draw_winch_fittings.py. */
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<WinchBlock>> WINCH_TYPE=CODECS.register("winch_fitting",()->WinchBlock.CODEC);
    public static final DeferredBlock<WinchBlock> WINCH=BLOCKS.registerBlock("winch_fitting",WinchBlock::new,fixed(Blocks.IRON_BLOCK).noOcclusion());
    private PuzzleRegistry(){}
    /** 0.4.252: the Night Operator's cord switchboard (ExchangeBlock), drawn by tools/draw_exchange_fittings.py, and its sounds: the rooms its jacks reach, heard down the line, and its buzzer (tools/generate_operator_audio.py). */
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<ExchangeBlock>> EXCHANGE_TYPE=CODECS.register("exchange_fitting",()->ExchangeBlock.CODEC);
    public static final DeferredBlock<ExchangeBlock> EXCHANGE=BLOCKS.registerBlock("exchange_fitting",ExchangeBlock::new,fixed(Blocks.DARK_OAK_PLANKS).noOcclusion().lightLevel(ExchangeBlock::light));
    private static final DeferredRegister<net.minecraft.sounds.SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    private static DeferredHolder<net.minecraft.sounds.SoundEvent,net.minecraft.sounds.SoundEvent> sound(String id){return SOUNDS.register(id,()->net.minecraft.sounds.SoundEvent.createVariableRangeEvent(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,id)));}
    /** The lines, in NightOperator.Line's order. */
    public static final java.util.List<DeferredHolder<net.minecraft.sounds.SoundEvent,net.minecraft.sounds.SoundEvent>> LINES=java.util.Arrays.stream(new String[]{"water","fridge","boards","clock","rain","radio","kettle","quiet","exchange"}).map(n->sound("operator.line_"+n)).toList();
    public static final DeferredHolder<net.minecraft.sounds.SoundEvent,net.minecraft.sounds.SoundEvent> BUZZ=sound("operator.buzz");
    /** 0.4.254: the Fuse Board's basement (whitewashed brick, painted concrete, a concrete soffit, steel plate) and its electrics (PowerBlock), drawn by tools/draw_power_fittings.py; the appliances' hum, the motors running, and the shutter's motor straining against its catch (tools/generate_power_audio.py). */
    private static final java.util.Map<String,DeferredBlock<Block>> POWER_MATERIALS=new java.util.LinkedHashMap<>();
    public static final java.util.List<String> POWER_MATERIAL_NAMES=java.util.List.of("floor","brick","ceiling","plate");
    static{for(String name:POWER_MATERIAL_NAMES)POWER_MATERIALS.put(name,BLOCKS.registerSimpleBlock("power_"+name,fixed(name.equals("plate")?Blocks.IRON_BLOCK:Blocks.STONE)));}
    public static Block powerMaterial(String name){var block=POWER_MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<PowerBlock>> POWER_TYPE=CODECS.register("power_fitting",()->PowerBlock.CODEC);
    public static final DeferredBlock<PowerBlock> POWER=BLOCKS.registerBlock("power_fitting",PowerBlock::new,fixed(Blocks.IRON_BLOCK).noOcclusion().lightLevel(PowerBlock::light));
    public static final DeferredHolder<net.minecraft.sounds.SoundEvent,net.minecraft.sounds.SoundEvent> HUM=sound("power.hum"),MOTOR=sound("power.motor"),STRAIN=sound("power.strain");
    /** 0.4.256, the Header Tank (HeaderTanks): the utility room's materials, its waterworks, and their sounds. */
    private static final java.util.Map<String,DeferredBlock<Block>> TANK_MATERIALS=new java.util.LinkedHashMap<>();
    public static final java.util.List<String> TANK_MATERIAL_NAMES=java.util.List.of("tiles","brick","mark","faint","plate","boards");
    static{for(String name:TANK_MATERIAL_NAMES)TANK_MATERIALS.put(name,BLOCKS.registerSimpleBlock("tank_"+name,fixed(name.equals("plate")?Blocks.IRON_BLOCK:name.equals("boards")?Blocks.OAK_PLANKS:Blocks.STONE)));}
    public static Block tankMaterial(String name){var block=TANK_MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<TankBlock>> TANK_TYPE=CODECS.register("tank_fitting",()->TankBlock.CODEC);
    public static final DeferredBlock<TankBlock> TANK=BLOCKS.registerBlock("tank_fitting",TankBlock::new,fixed(Blocks.IRON_BLOCK).noOcclusion());
    public static final DeferredHolder<net.minecraft.sounds.SoundEvent,net.minecraft.sounds.SoundEvent> VALVE=sound("tank.valve"),RUN=sound("tank.run"),PUMP=sound("tank.pump"),KNOCK=sound("tank.knock");
    /** 0.4.306, the Tension Room (TensionRooms): the loft's limewashed plaster, painted boards and the mark on the shaft, its cable gear (TensionBlock), drawn by tools/draw_tension_fittings.py, and its sounds (tools/generate_tension_audio.py). */
    private static final java.util.Map<String,DeferredBlock<Block>> TENSION_MATERIALS=new java.util.LinkedHashMap<>();
    public static final java.util.List<String> TENSION_MATERIAL_NAMES=java.util.List.of("plaster","boards","mark");
    static{for(String name:TENSION_MATERIAL_NAMES)TENSION_MATERIALS.put(name,BLOCKS.registerSimpleBlock("tension_"+name,fixed(name.equals("boards")?Blocks.SPRUCE_PLANKS:Blocks.STONE)));}
    public static Block tensionMaterial(String name){var block=TENSION_MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<TensionBlock>> TENSION_TYPE=CODECS.register("tension_fitting",()->TensionBlock.CODEC);
    public static final DeferredBlock<TensionBlock> TENSION=BLOCKS.registerBlock("tension_fitting",TensionBlock::new,fixed(Blocks.IRON_BLOCK).noOcclusion());
    public static final DeferredHolder<net.minecraft.sounds.SoundEvent,net.minecraft.sounds.SoundEvent> TENSION_WHEEL=sound("tension.wheel"),TENSION_TAKE=sound("tension.take"),TENSION_PIN=sound("tension.pin");
    /** 0.4.306, the Sphinx (Sphinx): the vestibule's tiles and plaster, the hall's ceiling, the cracked seam of a sealed door, and her crossing's fittings (SphinxBlock), drawn by tools/draw_sphinx_fittings.py; her voice, breath, grit and the latches (tools/generate_sphinx_audio.py). */
    private static final java.util.Map<String,DeferredBlock<Block>> SPHINX_MATERIALS=new java.util.LinkedHashMap<>();
    public static final java.util.List<String> SPHINX_MATERIAL_NAMES=java.util.List.of("tile","marked","plaster","ceiling","seam","dark","ashlar","flags","ashlar_seam","rock","rock_seam");
    static{for(String name:SPHINX_MATERIAL_NAMES)SPHINX_MATERIALS.put(name,BLOCKS.registerSimpleBlock("sphinx_"+name,fixed(name.equals("ceiling")?Blocks.SPRUCE_PLANKS:Blocks.STONE)));}
    public static Block sphinxMaterial(String name){var block=SPHINX_MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<SphinxBlock>> SPHINX_TYPE=CODECS.register("sphinx_fitting",()->SphinxBlock.CODEC);
    public static final DeferredBlock<SphinxBlock> SPHINX=BLOCKS.registerBlock("sphinx_fitting",SphinxBlock::new,fixed(Blocks.SPRUCE_PLANKS).noOcclusion());
    /** 0.4.343, loose fill held aside by a piece of the Dream Stela (StelaPassage). */
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<StelaFillBlock>> STELA_FILL_TYPE=CODECS.register("stela_fill",()->StelaFillBlock.CODEC);
    public static final DeferredBlock<StelaFillBlock> STELA_FILL=BLOCKS.registerBlock("stela_fill",StelaFillBlock::new,fixed(Blocks.SAND).noOcclusion().pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK).isSuffocating((st,l,pos)->false).isViewBlocking((st,l,pos)->false).isRedstoneConductor((st,l,pos)->false));
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<SphinxEyeBlock>> SPHINX_EYE_TYPE=CODECS.register("sphinx_eye",()->SphinxEyeBlock.CODEC);
    public static final DeferredBlock<SphinxEyeBlock> SPHINX_EYE=BLOCKS.registerBlock("sphinx_eye",SphinxEyeBlock::new,fixed(Blocks.SPRUCE_PLANKS).noOcclusion().noCollission().lightLevel(s->8));
    /** 0.4.337: her whole face over the first crossing's hall, drawn by its renderer. */
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<SphinxFaceBlock>> SPHINX_FACE_TYPE=CODECS.register("sphinx_face",()->SphinxFaceBlock.CODEC);
    public static final DeferredBlock<SphinxFaceBlock> SPHINX_FACE=BLOCKS.registerBlock("sphinx_face",SphinxFaceBlock::new,fixed(Blocks.SPRUCE_PLANKS).noOcclusion().noCollission());
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<SphinxFaceBlock.Face>> SPHINX_FACE_BE=BES.register("sphinx_face",()->BlockEntityType.Builder.of(SphinxFaceBlock.Face::new,SPHINX_FACE.get()).build(null));
    public static final DeferredHolder<net.minecraft.sounds.SoundEvent,net.minecraft.sounds.SoundEvent> SPHINX_VOICE=sound("sphinx.voice"),SPHINX_BREATH=sound("sphinx.breath"),SPHINX_LATCH=sound("sphinx.latch"),
        SPHINX_CATCH=sound("sphinx.catch"),SPHINX_PIN=sound("sphinx.pin"),SPHINX_STRIKE=sound("sphinx.strike"),SPHINX_SCRAPE=sound("sphinx.scrape"),
        // 0.4.335: the later crossings, the dream and the stone.
        SPHINX_RATTLE=sound("sphinx.rattle"),SPHINX_GRIND=sound("sphinx.grind"),SPHINX_RUSTLE=sound("sphinx.rustle"),SPHINX_WING=sound("sphinx.wing"),
        SPHINX_SAND=sound("sphinx.sand"),SPHINX_CRACK=sound("sphinx.crack"),SPHINX_SETTLE=sound("sphinx.settle");
    /** 0.4.307, Johnny Truant's service halls (TruantService): the chalk arrow, steam main, lift brakes and car (TruantFittingBlock), drawn by tools/draw_truant_service_fittings.py; their sounds (tools/generate_truant_service_audio.py). */
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<TruantFittingBlock>> TRUANT_FITTING_TYPE=CODECS.register("truant_fitting",()->TruantFittingBlock.CODEC);
    public static final DeferredBlock<TruantFittingBlock> TRUANT_FITTING=BLOCKS.registerBlock("truant_fitting",TruantFittingBlock::new,fixed(Blocks.IRON_BLOCK).noOcclusion());
    public static final DeferredHolder<net.minecraft.sounds.SoundEvent,net.minecraft.sounds.SoundEvent> TRUANT_CHALK=sound("truant_service.chalk"),TRUANT_KNOCK=sound("truant_service.knock"),TRUANT_RATTLE=sound("truant_service.rattle"),
        TRUANT_HISS=sound("truant_service.hiss"),TRUANT_BRAKE=sound("truant_service.brake"),TRUANT_LURCH=sound("truant_service.lurch"),TRUANT_PAGE=sound("truant_service.page");
    /** 0.4.360, the Resonant Pipes (ResonantPipes): the singing pipework (ResonanceBlock), drawn by tools/draw_resonance_fittings.py, and its notes (tools/generate_resonance_audio.py). */
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<ResonanceBlock>> RESONANCE_TYPE=CODECS.register("resonance_fitting",()->ResonanceBlock.CODEC);
    public static final DeferredBlock<ResonanceBlock> RESONANCE=BLOCKS.registerBlock("resonance_fitting",ResonanceBlock::new,fixed(Blocks.IRON_BLOCK).noOcclusion());
    public static final DeferredHolder<net.minecraft.sounds.SoundEvent,net.minecraft.sounds.SoundEvent> RESONANCE_LOW=sound("resonance.low"),RESONANCE_MIDDLE=sound("resonance.middle"),RESONANCE_HIGH=sound("resonance.high"),
        RESONANCE_BUZZ=sound("resonance.buzz"),RESONANCE_LATCH=sound("resonance.latch");
    /** 0.4.309, the House's yard (YardRooms): clapboard, wet gravel and the garage door; the porch bulb and the station wagon (YardBlock), drawn by tools/draw_yard_fittings.py; the engine, a passing car, a crow and the creek (tools/generate_yard_audio.py). */
    private static final java.util.Map<String,DeferredBlock<Block>> YARD_MATERIALS=new java.util.LinkedHashMap<>();
    public static final java.util.List<String> YARD_MATERIAL_NAMES=java.util.List.of("clapboard","wet_gravel","garage_door");
    static{for(String name:YARD_MATERIAL_NAMES)YARD_MATERIALS.put(name,BLOCKS.registerSimpleBlock("yard_"+name,fixed(name.equals("wet_gravel")?Blocks.GRAVEL:name.equals("garage_door")?Blocks.DARK_OAK_PLANKS:Blocks.BIRCH_PLANKS)));}
    public static Block yardMaterial(String name){var block=YARD_MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<YardBlock>> YARD_TYPE=CODECS.register("yard_fitting",()->YardBlock.CODEC);
    public static final DeferredBlock<YardBlock> YARD=BLOCKS.registerBlock("yard_fitting",YardBlock::new,fixed(Blocks.IRON_BLOCK).noOcclusion().lightLevel(YardBlock::light));
    public static final DeferredHolder<net.minecraft.sounds.SoundEvent,net.minecraft.sounds.SoundEvent> YARD_IDLE=sound("yard.idle"),YARD_TRAFFIC=sound("yard.traffic"),YARD_CROW=sound("yard.crow"),YARD_CREEK=sound("yard.creek");
    public static final DeferredHolder<net.minecraft.sounds.SoundEvent,net.minecraft.sounds.SoundEvent> YARD_FOLLOW=sound("yard.follow"),YARD_WIND=sound("yard.wind");
    /** 0.4.358, the clothesline's quiet return: a wet sheet's weight, and the line pulled taut when it falls back. */
    public static final DeferredHolder<net.minecraft.sounds.SoundEvent,net.minecraft.sounds.SoundEvent> YARD_WET_SHEET=sound("yard.wet_sheet"),YARD_LINE_SNAP=sound("yard.line_snap");
    // 0.4.342, the Sphinx's dream: her whole body, and the piece of the stela she leaves at the door.
    private static final DeferredRegister<net.minecraft.world.entity.EntityType<?>> TYPES=DeferredRegister.create(Registries.ENTITY_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    public static final DeferredHolder<net.minecraft.world.entity.EntityType<?>,net.minecraft.world.entity.EntityType<SphinxDreamEntity>> SPHINX_DREAM=TYPES.register("sphinx_dream",
        ()->net.minecraft.world.entity.EntityType.Builder.<SphinxDreamEntity>of(SphinxDreamEntity::new,net.minecraft.world.entity.MobCategory.MISC).sized(10F,6F).noSave().clientTrackingRange(20).updateInterval(1).build("sphinx_dream"));
    public static final DeferredItem<DreamStelaItem> DREAM_STELA=ITEMS.registerItem("dream_stela",DreamStelaItem::new,new net.minecraft.world.item.Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.UNCOMMON).fireResistant());
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);BES.register(bus);SOUNDS.register(bus);TYPES.register(bus);ITEMS.register(bus);}
}
