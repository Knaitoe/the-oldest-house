package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 0.4.518, the Pell house (docs/PELL_HOUSE_PROPOSAL.md): the street's and the house's own materials and fittings. Drawn by
 * tools/draw_pell_house.py, which checks that its list of materials and its kinds of fitting are these.
 */
public final class PellRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    private static final java.util.Map<String,DeferredBlock<Block>> MATERIALS=new java.util.LinkedHashMap<>();
    /**
     * The brick veneer, its lowest course (weep holes, salt bloom) and the newer brick of a window bricked up; the roof's tiles and the
     * eaves' board; June's lawn and the street's; concrete, the path's slab with a hand pressed into it and the drive's stain, the road,
     * the curb; the patio's pavers; the pool's coping, tiles and waterline band (0.4.533: with one tile missing and one that does not
     * match, and the deep end's drain, with the goggles under it); deck boards; bark mulch; the shed's iron; plaster;
     * the ceiling, and Nessa's with its stars; the hall's carpet and the path worn in it; lino, worn and burnt; the garage's slab,
     * plain and stained.
     */
    public static final java.util.List<String> MATERIAL_NAMES=java.util.List.of("brick","brick_low","brick_new","roof","fascia","lawn","lawn_dry","concrete","concrete_print",
        "concrete_oil","asphalt","curb","paver","coping","pool_tile","pool_band","pool_band_missing","pool_band_odd","pool_drain","deck","mulch","iron","plaster","ceiling","ceiling_stars","carpet","carpet_worn","lino",
        "lino_worn","lino_burn","slab","slab_oil");
    private static Block like(String name){return switch(name){case "lawn","lawn_dry","mulch"->Blocks.GRASS_BLOCK;case "deck","fascia"->Blocks.OAK_PLANKS;case "carpet","carpet_worn"->Blocks.WHITE_WOOL;case "iron"->Blocks.IRON_BLOCK;default->Blocks.STONE;};}
    static{for(String name:MATERIAL_NAMES)MATERIALS.put(name,BLOCKS.registerSimpleBlock("pell_"+name,fixed(like(name))));}
    public static Block material(String name){var block=MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<PellBlock>> FITTING_TYPE=CODECS.register("pell_fitting",()->PellBlock.CODEC);
    public static final DeferredBlock<PellBlock> FITTING=BLOCKS.registerBlock("pell_fitting",PellBlock::new,fixed(Blocks.OAK_PLANKS).noOcclusion());
    /** 0.4.522: what is in the rooms (PellThing), drawn by tools/draw_pell_things.py. The bare bulb and the television give light. */
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<PellThing>> THING_TYPE=CODECS.register("pell_thing",()->PellThing.CODEC);
    public static final DeferredBlock<PellThing> THING=BLOCKS.registerBlock("pell_thing",PellThing::new,fixed(Blocks.OAK_PLANKS).noOcclusion().lightLevel(PellThing::light));
    /** 0.4.523: the line round the lot (PellLine), painted into the ground it lies on; drawn by tools/draw_pell_line.py. */
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<PellLine>> LINE_TYPE=CODECS.register("pell_line",()->PellLine.CODEC);
    public static final DeferredBlock<PellLine> LINE=BLOCKS.registerBlock("pell_line",PellLine::new,fixed(Blocks.STONE));
    /** 0.4.524: Tilly's cane and Gail's folder, in their hands (PellPeople); drawn by tools/draw_pell_people.py. Nobody is given one. */
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    public static final net.neoforged.neoforge.registries.DeferredItem<net.minecraft.world.item.Item> CANE=ITEMS.register("pell_cane",()->new net.minecraft.world.item.Item(new net.minecraft.world.item.Item.Properties().stacksTo(1)));
    public static final net.neoforged.neoforge.registries.DeferredItem<net.minecraft.world.item.Item> FOLDER=ITEMS.register("pell_folder",()->new net.minecraft.world.item.Item(new net.minecraft.world.item.Item.Properties().stacksTo(1)));
    /** 0.4.526: June's dinner things (PellVisit): her plates as they lie on the table (laid, served, scraped) and the saucepan she serves
     *  from. Drawn by tools/draw_pell_people.py; nobody is given one. */
    public static final net.neoforged.neoforge.registries.DeferredItem<net.minecraft.world.item.Item> PLATE=ITEMS.register("pell_plate",()->new net.minecraft.world.item.Item(new net.minecraft.world.item.Item.Properties().stacksTo(1)));
    public static final net.neoforged.neoforge.registries.DeferredItem<net.minecraft.world.item.Item> DINNER=ITEMS.register("pell_dinner",()->new net.minecraft.world.item.Item(new net.minecraft.world.item.Item.Properties().stacksTo(1)));
    public static final net.neoforged.neoforge.registries.DeferredItem<net.minecraft.world.item.Item> SCRAPED=ITEMS.register("pell_plate_scraped",()->new net.minecraft.world.item.Item(new net.minecraft.world.item.Item.Properties().stacksTo(1)));
    public static final net.neoforged.neoforge.registries.DeferredItem<net.minecraft.world.item.Item> SAUCEPAN=ITEMS.register("pell_saucepan",()->new net.minecraft.world.item.Item(new net.minecraft.world.item.Item.Properties().stacksTo(1)));
    /** 0.4.531: breakfast as it lies on the table (PellMorning), toast and a fried egg. Nobody is given one. */
    public static final net.neoforged.neoforge.registries.DeferredItem<net.minecraft.world.item.Item> BREAKFAST=ITEMS.register("pell_breakfast",()->new net.minecraft.world.item.Item(new net.minecraft.world.item.Item.Properties().stacksTo(1)));
    /** 0.4.527: June's hairbrush, in her hand while she brushes Tilly's hair at bedtime (PellVisit). Nobody is given one. */
    public static final net.neoforged.neoforge.registries.DeferredItem<net.minecraft.world.item.Item> HAIRBRUSH=ITEMS.register("pell_hairbrush",()->new net.minecraft.world.item.Item(new net.minecraft.world.item.Item.Properties().stacksTo(1)));
    /** 0.4.530: what June carries out at night (PellNight): her lantern, the tin of paint and the brush. Nobody is given one. */
    public static final net.neoforged.neoforge.registries.DeferredItem<net.minecraft.world.item.Item> LANTERN=ITEMS.register("pell_lantern",()->new net.minecraft.world.item.Item(new net.minecraft.world.item.Item.Properties().stacksTo(1)));
    public static final net.neoforged.neoforge.registries.DeferredItem<net.minecraft.world.item.Item> PAINT_TIN=ITEMS.register("pell_tin",()->new net.minecraft.world.item.Item(new net.minecraft.world.item.Item.Properties().stacksTo(1)));
    public static final net.neoforged.neoforge.registries.DeferredItem<net.minecraft.world.item.Item> PAINTBRUSH=ITEMS.register("pell_paintbrush",()->new net.minecraft.world.item.Item(new net.minecraft.world.item.Item.Properties().stacksTo(1)));
    /** 0.4.532: the kitchen telephone's receiver, in June's hand while she is on it to Gail (PellCall). Nobody is given one. */
    public static final net.neoforged.neoforge.registries.DeferredItem<net.minecraft.world.item.Item> RECEIVER=ITEMS.register("pell_receiver",()->new net.minecraft.world.item.Item(new net.minecraft.world.item.Item.Properties().stacksTo(1)));
    /** 0.4.533: the pool's lane rope across the water a week in (PellWeek), a block's length of it to each item display; and the length
     *  of it where a float is missing. Nobody is given one. */
    public static final net.neoforged.neoforge.registries.DeferredItem<net.minecraft.world.item.Item> LANE=ITEMS.register("pell_lane",()->new net.minecraft.world.item.Item(new net.minecraft.world.item.Item.Properties().stacksTo(1)));
    public static final net.neoforged.neoforge.registries.DeferredItem<net.minecraft.world.item.Item> LANE_GAP=ITEMS.register("pell_lane_gap",()->new net.minecraft.world.item.Item(new net.minecraft.world.item.Item.Properties().stacksTo(1)));
    /** 0.4.534: the kitchen knife in Callum's hand at the sink in the night (PellKnife). Nobody is given one. */
    public static final net.neoforged.neoforge.registries.DeferredItem<net.minecraft.world.item.Item> KNIFE=ITEMS.register("pell_knife",()->new net.minecraft.world.item.Item(new net.minecraft.world.item.Item.Properties().stacksTo(1)));
    /** 0.4.538: the bucket from the laundry, water in it and the scrubbing brush across it (PellScrub, PellScrubItem). */
    public static final net.neoforged.neoforge.registries.DeferredItem<net.minecraft.world.item.Item> SCRUB=ITEMS.register("pell_scrub",()->new PellScrubItem(new net.minecraft.world.item.Item.Properties().stacksTo(1)));
    /** 0.4.540: June's lanyard, from her robe's pocket on the last night: three keys, one of them the freezer's (PellFreezer). */
    public static final net.neoforged.neoforge.registries.DeferredItem<net.minecraft.world.item.Item> LANYARD=ITEMS.register("pell_lanyard",()->new net.minecraft.world.item.Item(new net.minecraft.world.item.Item.Properties().stacksTo(1)));
    /** 0.4.541: Tilly's spare cane tip, the keepsake of the morning she came out (LiteraryVignettes.finishReward, Keepsakes.caneTip). */
    public static final net.neoforged.neoforge.registries.DeferredItem<net.minecraft.world.item.Item> CANE_TIP=ITEMS.register("pell_cane_tip",()->new net.minecraft.world.item.Item(new net.minecraft.world.item.Item.Properties().stacksTo(1)));
    /** 0.4.525: the house's sounds, made by tools/generate_pell_audio.py: Gail's car leaving (PellVisit); and from 0.4.527 the home
     *  tape on the television, the hairbrush at bedtime, and a door's lock turning when June locks up; from 0.4.530 the brush on the
     *  line at night and the tin's lid (PellNight). */
    private static final DeferredRegister<net.minecraft.sounds.SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    private static DeferredHolder<net.minecraft.sounds.SoundEvent,net.minecraft.sounds.SoundEvent> sound(String id){return SOUNDS.register(id,()->net.minecraft.sounds.SoundEvent.createVariableRangeEvent(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,id)));}
    public static final DeferredHolder<net.minecraft.sounds.SoundEvent,net.minecraft.sounds.SoundEvent> CAR_AWAY=sound("pell.car_away"),TAPE=sound("pell.tape"),
        BRUSH=sound("pell.brush"),LOCK=sound("pell.lock"),PAINT=sound("pell.paint"),TIN=sound("pell.tin"),
        // 0.4.535: a door slammed in the laundry, and June's cry (docs/PELL_HOUSE_HAND_0_4_535.md).
        HAND=sound("pell.hand"),
        // 0.4.536: the shed's deck taking a tape, and the woman's voice on the tapes (docs/PELL_HOUSE_TAPES_0_4_536.md).
        TAPE_PLAY=sound("pell.tape_play"),TAPE_VOICE=sound("pell.tape_voice"),
        // 0.4.538: the scrubbing brush on the line (docs/PELL_HOUSE_LAST_NIGHT_0_4_538.md).
        SCRUBBING=sound("pell.scrub"),
        // 0.4.541: Gail's car up the street at first light, pulling in at the curb (docs/PELL_HOUSE_DAWN_0_4_541.md).
        CAR_COME=sound("pell.car_come"),
        // 0.4.543: a cane tapping, behind a door that will not open (docs/PELL_HOUSE_FAILURE_0_4_543.md).
        CANE_TAP=sound("pell.cane");
    private PellRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);ITEMS.register(bus);SOUNDS.register(bus);}
}
