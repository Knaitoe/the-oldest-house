package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 0.4.549, The Ones Already Inside (docs/THE_ONES_ALREADY_INSIDE_PLAN.md): the lodging house's own materials, walls, floors, stairs
 * and fittings. Drawn by tools/draw_lodging_house.py, which checks that its lists of materials, pairs, parts and kinds are these.
 */
public final class LodgingRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    private static final java.util.Map<String,DeferredBlock<Block>> MATERIALS=new java.util.LinkedHashMap<>();
    /**
     * The outside walls' and the cellar's brick, and its lowest course gone damp; the cellar's flags; the roof; the passages' laths;
     * the varnished panelling round the cupboard under the stair; the hall's paper, the parlor's, the guest rooms' and the back stair's
     * distemper, each with its skirting course, for walls with the same room on both sides.
     */
    public static final java.util.List<String> MATERIAL_NAMES=java.util.List.of("brick","brick_damp","flags","roof","lath","panel","paper_hall","paper_hall_low",
        "paper_parlor","paper_parlor_low","paper_room","paper_room_low","distemper","distemper_low");
    private static Block like(String name){return switch(name){case "lath","panel"->Blocks.SPRUCE_PLANKS;case "paper_hall","paper_hall_low","paper_parlor","paper_parlor_low","paper_room","paper_room_low","distemper","distemper_low"->Blocks.WHITE_WOOL;default->Blocks.BRICKS;};}
    static{for(String name:MATERIAL_NAMES)MATERIALS.put(name,BLOCKS.registerSimpleBlock("lodging_"+name,fixed(like(name))));}
    public static Block material(String name){var block=MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<LodgingBlock>> FITTING_TYPE=CODECS.register("lodging_fitting",()->LodgingBlock.CODEC);
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<LodgingWall>> WALL_TYPE=CODECS.register("lodging_wall",()->LodgingWall.CODEC);
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<LodgingFloor>> FLOOR_TYPE=CODECS.register("lodging_floor",()->LodgingFloor.CODEC);
    public static final DeferredBlock<LodgingBlock> FITTING=BLOCKS.registerBlock("lodging_fitting",LodgingBlock::new,fixed(Blocks.OAK_PLANKS).noOcclusion().lightLevel(LodgingBlock::light));
    public static final DeferredBlock<LodgingWall> WALL=BLOCKS.registerBlock("lodging_wall",LodgingWall::new,fixed(Blocks.WHITE_WOOL));
    public static final DeferredBlock<LodgingFloor> FLOOR=BLOCKS.registerBlock("lodging_floor",LodgingFloor::new,fixed(Blocks.OAK_PLANKS));
    /** The main stair, its treads under a runner, and the back stair down to the cellar, bare. */
    public static final DeferredBlock<StairBlock> STAIR=BLOCKS.registerBlock("lodging_stair",p->new StairBlock(Blocks.OAK_PLANKS.defaultBlockState(),p),fixed(Blocks.OAK_STAIRS));
    public static final DeferredBlock<StairBlock> CELLAR_STAIR=BLOCKS.registerBlock("lodging_cellar_stair",p->new StairBlock(Blocks.SPRUCE_PLANKS.defaultBlockState(),p),fixed(Blocks.SPRUCE_STAIRS));
    private LodgingRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);}
}
