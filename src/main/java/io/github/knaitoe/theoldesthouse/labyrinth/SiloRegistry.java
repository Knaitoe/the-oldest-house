package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

public final class SiloRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    public static final DeferredHolder<SoundEvent,SoundEvent> GROAN=sound("groan"),SPLINTER=sound("splinter"),GRAIN=sound("grain"),RADIO_TUCSON=sound("radio_tucson"),RADIO_FLATBED=sound("radio_flatbed"),RADIO_WINTER=sound("radio_winter");
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String name){return SOUNDS.register("silo."+name,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"silo."+name)));}
    private static BlockBehaviour.Properties metal(){return BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).strength(-1,3600000).noLootTable();}
    public static final DeferredBlock<Block> TIN=BLOCKS.registerBlock("silo_tin",Block::new,metal());
    public static final DeferredBlock<Block> STEEL=BLOCKS.registerBlock("silo_steel",Block::new,metal());
    public static final DeferredBlock<Block> SKY=BLOCKS.registerBlock("silo_sky",Block::new,metal().lightLevel(s->5));
    public static final DeferredBlock<SiloGrainBlock> CORN=BLOCKS.registerBlock("silo_corn",SiloGrainBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.PACKED_MUD).strength(-1,3600000).noLootTable().sound(SoundType.SAND));
    public static final DeferredBlock<SiloTimberBlock> TIMBER=BLOCKS.registerBlock("silo_timber",SiloTimberBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_PLANKS).strength(-1,3600000).noOcclusion().dynamicShape().noLootTable());
    public static final DeferredBlock<LadderBlock> LADDER=BLOCKS.register("silo_ladder",()->new LadderBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.LADDER).strength(-1,3600000).noLootTable()){});
    public static final DeferredBlock<SiloHatchBlock> HATCH=BLOCKS.registerBlock("silo_hatch",SiloHatchBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_TRAPDOOR).strength(-1,3600000).noLootTable());
    public static final DeferredBlock<SiloPaperBlock> PAPER=BLOCKS.registerBlock("silo_paper",SiloPaperBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PRESSURE_PLATE).strength(-1,3600000).noOcclusion().noLootTable());
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<SiloTimberBlock>> TIMBER_TYPE=CODECS.register("silo_timber",()->SiloTimberBlock.CODEC);
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<SiloPaperBlock>> PAPER_TYPE=CODECS.register("silo_paper",()->SiloPaperBlock.CODEC);
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<SiloTimberBlock.Timber>> TIMBER_ENTITY=BES.register("silo_timber",()->BlockEntityType.Builder.of(SiloTimberBlock.Timber::new,TIMBER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<SiloPaperBlock.Paper>> PAPER_ENTITY=BES.register("silo_paper",()->BlockEntityType.Builder.of(SiloPaperBlock.Paper::new,PAPER.get()).build(null));
    private SiloRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);BES.register(bus);SOUNDS.register(bus);}
}
