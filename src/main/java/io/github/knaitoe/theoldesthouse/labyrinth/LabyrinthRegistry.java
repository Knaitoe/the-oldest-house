package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/** Items the labyrinth's vignettes hand out. */
public final class LabyrinthRegistry {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    /** The caver's packed crack (0.4.71): slow, honest pickaxe work, one block at a time, and nothing to carry away. */
    public static final DeferredBlock<Block> CAVE_RUBBLE=BLOCKS.registerSimpleBlock("cave_rubble",
            BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).strength(15F,6F).sound(SoundType.DEEPSLATE).requiresCorrectToolForDrops().noLootTable());
    /** The cuts under the mineral crust (0.4.72): cave rock, not a dressed block, so the mark reads as part of the wall. */
    public static final DeferredBlock<Block> CAVE_MARKS=BLOCKS.registerSimpleBlock("cave_marks",
            BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5F,6F).sound(SoundType.STONE).requiresCorrectToolForDrops().noLootTable());
    public static final ResourceLocation HEARTBEAT_ID=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"vignette.heartbeat");
    public static final DeferredHolder<SoundEvent,SoundEvent> FLOORBOARD_HEARTBEAT=SOUNDS.register("vignette.heartbeat",()->SoundEvent.createVariableRangeEvent(HEARTBEAT_ID));
    public static final DeferredHolder<SoundEvent,SoundEvent> HALL_PIPES=sound("hall.pipes"), HALL_STONE=sound("hall.stone"), HALL_SETTLE=sound("hall.settle");
    public static final DeferredHolder<SoundEvent,SoundEvent> STAIRCASE_SCORE=sound("staircase.descent");
    /** The cave's breath, its stone, the line and what is heard behind a shut door (0.4.71). */
    public static final DeferredHolder<SoundEvent,SoundEvent> CAVER_EXHALE=sound("caver.exhale"),CAVER_INHALE=sound("caver.inhale"),CAVER_SCRAPE=sound("caver.scrape"),
            CAVER_LINE=sound("caver.line_taut"),CAVER_STONE=sound("caver.stone_roll"),CAVER_CHISEL=sound("caver.chisel");
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String id) {
        return SOUNDS.register(id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,id)));
    }

    public static final DeferredItem<BlindfoldItem> BLINDFOLD =
            ITEMS.register("blindfold", () -> new BlindfoldItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<HarriganPhoneItem> PHONE =
            ITEMS.register("phone", () -> new HarriganPhoneItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> HARRIGANS_PHONE =
            ITEMS.register("harrigans_phone", () -> new HarriganPhoneItem.Keepsake(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> SCRATCH_TICKET =
            ITEMS.registerSimpleItem("scratch_ticket", new Item.Properties().stacksTo(1));

    public static final DeferredItem<NavigationItems.Chalk> CHALK =
            ITEMS.register("chalk", () -> new NavigationItems.Chalk(new Item.Properties().durability(64)));
    public static final DeferredItem<NavigationItems.Spool> TRAIL_SPOOL =
            ITEMS.register("trail_spool", () -> new NavigationItems.Spool(new Item.Properties().durability(192)));

    private LabyrinthRegistry() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        SOUNDS.register(modEventBus);
    }
}
