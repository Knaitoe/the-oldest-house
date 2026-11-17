package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** 0.4.677: the painted signs that replaced the game's own wooden ones (BoardSigns). Drawn by tools/draw_board_signs.py.
 * 0.4.678: the lettered plates for the longer notices (LetterPlates), drawn by tools/draw_letter_plates.py. */
public final class BoardRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,TheOldestHouse.MOD_ID);
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<BoardSignBlock>> SIGN_TYPE=CODECS.register("board_sign",()->BoardSignBlock.CODEC);
    /** One block of a painted sign. Nothing walks into it. */
    public static final DeferredBlock<BoardSignBlock> SIGN=BLOCKS.registerBlock("board_sign",BoardSignBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_PLANKS).strength(-1,3600000).noLootTable().noOcclusion().noCollission().lightLevel(BoardSignBlock::light));
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<LetterPlateBlock>> PLATE_TYPE=CODECS.register("letter_plate",()->LetterPlateBlock.CODEC);
    /** A plate with words lettered over it by the game. Nothing walks into it. */
    public static final DeferredBlock<LetterPlateBlock> PLATE=BLOCKS.registerBlock("letter_plate",LetterPlateBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_PLANKS).strength(-1,3600000).noLootTable().noOcclusion().noCollission());
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<LetterPlateEntity>> PLATE_ENTITY=
        BES.register("letter_plate",()->BlockEntityType.Builder.of(LetterPlateEntity::new,PLATE.get()).build(null));
    private BoardRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);BES.register(bus);}
}
