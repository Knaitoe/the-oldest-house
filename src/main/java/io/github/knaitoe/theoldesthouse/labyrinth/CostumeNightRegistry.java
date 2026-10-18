package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Everything Costume Night (0.4.103) registers: the party's props and its sounds. */
public final class CostumeNightRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(BuiltInRegistries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<CostumePropBlock>> PROP_TYPE=CODECS.register("costume_prop",()->CostumePropBlock.CODEC);
    /** The bulbs, radio, apple tub, candy bowl and contest board. Scenery: no item, no drops, not to be broken. */
    public static final DeferredBlock<CostumePropBlock> PROP=BLOCKS.registerBlock("costume_prop",CostumePropBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_PLANKS).strength(-1,3600000).noOcclusion().noLootTable().lightLevel(CostumePropBlock::light));

    /** The party and its end, each subtitled. */
    public static final DeferredHolder<SoundEvent,SoundEvent> RADIO=sound("costume.radio"),CHEER=sound("costume.cheer"),POP=sound("costume.pop"),
            MEGAPHONE=sound("costume.megaphone"),DRIP=sound("costume.drip"),GIGGLE=sound("costume.giggle");

    private CostumeNightRegistry(){}
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String id){return SOUNDS.register(id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,id)));}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);SOUNDS.register(bus);}
}
