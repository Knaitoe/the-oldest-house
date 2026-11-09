package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.BedItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/**
 * 0.4.414 (docs/BAR_GAMES_PROPOSAL.md): the bar's games. The pool table and its sounds; darts and the bar itself follow. Drawn by
 * tools/draw_bar_art.py, the sounds made by tools/generate_bar_audio.py.
 */
public final class BarRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<PoolTableBlock>> POOL_TABLE_TYPE=CODECS.register("pool_table",()->PoolTableBlock.CODEC);
    public static final DeferredBlock<PoolTableBlock> POOL_TABLE=BLOCKS.registerBlock("pool_table",PoolTableBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(2F,3F).noOcclusion());
    public static final DeferredItem<BedItem> POOL_TABLE_ITEM=ITEMS.register("pool_table",()->new BedItem(POOL_TABLE.get(),new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<PoolTableBlockEntity>> POOL_TABLE_ENTITY=BLOCK_ENTITIES.register("pool_table",
        ()->BlockEntityType.Builder.of(PoolTableBlockEntity::new,POOL_TABLE.get()).build(null));

    private static DeferredHolder<SoundEvent,SoundEvent> sound(String id){return SOUNDS.register(id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,id)));}
    /** The cue's tip on the ball, ball on ball, a ball off the cushion, and one dropping into a pocket. */
    public static final DeferredHolder<SoundEvent,SoundEvent> POOL_STRIKE=sound("pool.strike"),POOL_CLACK=sound("pool.clack"),POOL_CUSHION=sound("pool.cushion"),POOL_POCKET=sound("pool.pocket");

    private BarRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);ITEMS.register(bus);BLOCK_ENTITIES.register(bus);SOUNDS.register(bus);}
}
