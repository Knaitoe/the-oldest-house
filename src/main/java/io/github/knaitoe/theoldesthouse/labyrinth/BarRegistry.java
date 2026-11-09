package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BedItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/**
 * 0.4.414 (docs/BAR_GAMES_PROPOSAL.md): the bar's games. The pool table and its sounds (0.4.414), the dartboard, darts and theirs
 * (0.4.415), the bar's whistle (0.4.416), and the two keepsakes that make a table and a board at home (0.4.417). Drawn by
 * tools/draw_bar_art.py, the sounds made by tools/generate_bar_audio.py.
 */
public final class BarRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<EntityType<?>> ENTITIES=DeferredRegister.create(Registries.ENTITY_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<PoolTableBlock>> POOL_TABLE_TYPE=CODECS.register("pool_table",()->PoolTableBlock.CODEC);
    public static final DeferredBlock<PoolTableBlock> POOL_TABLE=BLOCKS.registerBlock("pool_table",PoolTableBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(2F,3F).noOcclusion());
    public static final DeferredItem<BedItem> POOL_TABLE_ITEM=ITEMS.register("pool_table",()->new BedItem(POOL_TABLE.get(),new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<PoolTableBlockEntity>> POOL_TABLE_ENTITY=BLOCK_ENTITIES.register("pool_table",
        ()->BlockEntityType.Builder.of(PoolTableBlockEntity::new,POOL_TABLE.get()).build(null));

    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<DartboardBlock>> DARTBOARD_TYPE=CODECS.register("dartboard",()->DartboardBlock.CODEC);
    public static final DeferredBlock<DartboardBlock> DARTBOARD=BLOCKS.registerBlock("dartboard",DartboardBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(1F,2F).noOcclusion());
    public static final DeferredItem<BlockItem> DARTBOARD_ITEM=ITEMS.register("dartboard",()->new BlockItem(DARTBOARD.get(),new Item.Properties()));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<DartboardBlockEntity>> DARTBOARD_ENTITY=BLOCK_ENTITIES.register("dartboard",
        ()->BlockEntityType.Builder.of(DartboardBlockEntity::new,DARTBOARD.get()).build(null));
    public static final DeferredItem<DartItem> DART=ITEMS.register("dart",()->new DartItem(new Item.Properties().stacksTo(16)));
    public static final DeferredHolder<EntityType<?>,EntityType<DartEntity>> DART_ENTITY=ENTITIES.register("dart",
        ()->EntityType.Builder.<DartEntity>of(DartEntity::new,MobCategory.MISC).sized(.15F,.15F).clientTrackingRange(6).updateInterval(1).build("dart"));

    /** 0.4.417: the bar's keepsakes, given with the morning shift's note to a reader who left the roll and walked out. Each makes its
     * object at a crafting table for its own reader and comes back ({@link KeepsakeKeyItem}). */
    public static final DeferredItem<KeepsakeKeyItem> HOUSE_CHALK=ITEMS.register("house_chalk",()->new KeepsakeKeyItem(new Item.Properties().stacksTo(1),"last_orders"));
    public static final DeferredItem<KeepsakeKeyItem> DART_TIN=ITEMS.register("dart_tin",()->new KeepsakeKeyItem(new Item.Properties().stacksTo(1),"last_orders"));
    public static final ResourceLocation POOL_TABLE_RECIPE=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"pool_table");
    public static final ResourceLocation DARTBOARD_RECIPE=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"dartboard");

    private static DeferredHolder<SoundEvent,SoundEvent> sound(String id){return SOUNDS.register(id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,id)));}
    /** The cue's tip on the ball, ball on ball, a ball off the cushion, and one dropping into a pocket. */
    /** A dart leaving the hand, sinking into the board, knocking into the wall, and the turn's darts pulled out. */
    public static final DeferredHolder<SoundEvent,SoundEvent> DARTS_THROW=sound("darts.throw"),DARTS_BOARD=sound("darts.board"),DARTS_WALL=sound("darts.wall"),DARTS_PULL=sound("darts.pull");
    /** 0.4.416: the man in booth four whistles three times when the last frame is his. */
    public static final DeferredHolder<SoundEvent,SoundEvent> ORDERS_WHISTLE=sound("orders.whistle");
    public static final DeferredHolder<SoundEvent,SoundEvent> POOL_STRIKE=sound("pool.strike"),POOL_CLACK=sound("pool.clack"),POOL_CUSHION=sound("pool.cushion"),POOL_POCKET=sound("pool.pocket");

    private BarRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);ITEMS.register(bus);BLOCK_ENTITIES.register(bus);ENTITIES.register(bus);SOUNDS.register(bus);}
}
