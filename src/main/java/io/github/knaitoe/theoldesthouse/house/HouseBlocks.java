package io.github.knaitoe.theoldesthouse.house;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Authored blocks used where a vanilla stand-in would be conspicuous. */
public final class HouseBlocks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> BLOCK_TYPES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_TYPE, TheOldestHouse.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends Block>, MapCodec<WardrobeBlock>> WARDROBE_TYPE =
            BLOCK_TYPES.register("wardrobe", () -> WardrobeBlock.CODEC);

    public static final DeferredBlock<CarpetBlock> GREAT_ROOM_RUG_AUTHORED = carpet("great_room_rug_authored", Blocks.BROWN_CARPET);
    public static final DeferredBlock<CarpetBlock> GREAT_ROOM_RUG_SHIFTED = carpet("great_room_rug_shifted", Blocks.RED_CARPET);
    public static final DeferredBlock<CarpetBlock> STUDY_RUG_AUTHORED = carpet("study_rug_authored", Blocks.RED_CARPET);
    public static final DeferredBlock<CarpetBlock> STUDY_RUG_SHIFTED = carpet("study_rug_shifted", Blocks.LIGHT_BLUE_CARPET);
    public static final DeferredBlock<CarpetBlock> PRINCIPAL_RUG_AUTHORED = carpet("principal_rug_authored", Blocks.RED_CARPET);
    public static final DeferredBlock<CarpetBlock> PRINCIPAL_RUG_SHIFTED = carpet("principal_rug_shifted", Blocks.BROWN_CARPET);
    public static final DeferredBlock<CarpetBlock> LITERARY_RUG_AUTHORED = carpet("literary_rug_authored", Blocks.LIGHT_BLUE_CARPET);
    public static final DeferredBlock<CarpetBlock> LITERARY_RUG_SHIFTED = carpet("literary_rug_shifted", Blocks.RED_CARPET);

    public static final DeferredBlock<Block> LOOSE_FLOORBOARD = BLOCKS.registerBlock(
            "loose_floorboard", Block::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_PLANKS).noLootTable());

    public static final DeferredBlock<WardrobeBlock> HIDE_AND_CLAP_WARDROBE = BLOCKS.registerBlock(
            "hide_and_clap_wardrobe", WardrobeBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.DARK_OAK_PLANKS)
                    .noOcclusion()
                    .strength(2.0F, 3.0F)
                    .noLootTable());

    private HouseBlocks() {}

    private static DeferredBlock<CarpetBlock> carpet(String id, Block vanilla) {
        return BLOCKS.registerBlock(id, CarpetBlock::new,
                () -> BlockBehaviour.Properties.ofFullCopy(vanilla));
    }

    public static void register(IEventBus modEventBus) {
        BLOCK_TYPES.register(modEventBus);
        BLOCKS.register(modEventBus);
    }
}
