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

/** 0.4.199: the puzzle rooms' working parts. Borrowed Light first; drawn by tools/draw_borrowed_light.py. */
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
    private PuzzleRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);BES.register(bus);}
}
