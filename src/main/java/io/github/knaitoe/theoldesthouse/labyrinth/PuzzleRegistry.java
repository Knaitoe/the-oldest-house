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
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);BES.register(bus);}
}
