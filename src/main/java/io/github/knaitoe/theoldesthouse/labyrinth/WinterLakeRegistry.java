package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Everything Winter Lake (0.4.104) registers: the lake's ice and what stands on it, her ribbon, and the night's sounds. */
public final class WinterLakeRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(BuiltInRegistries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<WinterPropBlock>> PROP_TYPE=CODECS.register("winter_prop",()->WinterPropBlock.CODEC);
    /** Black ice, snowed-over ice, its two stages of cracking, the frozen cattails and the boys' snowballs. No item, no drops, not to be broken. */
    public static final DeferredBlock<WinterPropBlock> PROP=BLOCKS.registerBlock("winter_prop",WinterPropBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.PACKED_ICE).strength(-1,3600000).noOcclusion().noLootTable());
    /** Her hair ribbon, which Hank has carried since the shallows. */
    public static final DeferredItem<WinterRibbonItem> RIBBON=ITEMS.register("winter_ribbon",()->new WinterRibbonItem(new Item.Properties().stacksTo(1)));

    /** The ice, her, and the boys, each subtitled. */
    public static final DeferredHolder<SoundEvent,SoundEvent> KNOCK=sound("winter.knock"),CRACK=sound("winter.crack"),GROAN=sound("winter.groan"),
            BREAK=sound("winter.break"),HAND=sound("winter.hand"),RUN=sound("winter.run"),WIND=sound("winter.wind");

    private WinterLakeRegistry(){}
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String id){return SOUNDS.register(id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,id)));}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);ITEMS.register(bus);SOUNDS.register(bus);}
}
