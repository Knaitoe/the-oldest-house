package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 0.4.518, the Pell house (docs/PELL_HOUSE_PROPOSAL.md): the street's and the house's own materials and fittings. Drawn by
 * tools/draw_pell_house.py, which checks that its list of materials and its kinds of fitting are these.
 */
public final class PellRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    private static final java.util.Map<String,DeferredBlock<Block>> MATERIALS=new java.util.LinkedHashMap<>();
    /**
     * The brick veneer, its lowest course (weep holes, salt bloom) and the newer brick of a window bricked up; the roof's tiles and the
     * eaves' board; June's lawn and the street's; concrete, the path's slab with a hand pressed into it and the drive's stain, the road,
     * the curb; the patio's pavers; the pool's coping, tiles and waterline band; deck boards; bark mulch; the shed's iron; plaster;
     * the ceiling, and Nessa's with its stars; the hall's carpet and the path worn in it; lino, worn and burnt; the garage's slab,
     * plain and stained.
     */
    public static final java.util.List<String> MATERIAL_NAMES=java.util.List.of("brick","brick_low","brick_new","roof","fascia","lawn","lawn_dry","concrete","concrete_print",
        "concrete_oil","asphalt","curb","paver","coping","pool_tile","pool_band","deck","mulch","iron","plaster","ceiling","ceiling_stars","carpet","carpet_worn","lino",
        "lino_worn","lino_burn","slab","slab_oil");
    private static Block like(String name){return switch(name){case "lawn","lawn_dry","mulch"->Blocks.GRASS_BLOCK;case "deck","fascia"->Blocks.OAK_PLANKS;case "carpet","carpet_worn"->Blocks.WHITE_WOOL;case "iron"->Blocks.IRON_BLOCK;default->Blocks.STONE;};}
    static{for(String name:MATERIAL_NAMES)MATERIALS.put(name,BLOCKS.registerSimpleBlock("pell_"+name,fixed(like(name))));}
    public static Block material(String name){var block=MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<PellBlock>> FITTING_TYPE=CODECS.register("pell_fitting",()->PellBlock.CODEC);
    public static final DeferredBlock<PellBlock> FITTING=BLOCKS.registerBlock("pell_fitting",PellBlock::new,fixed(Blocks.OAK_PLANKS).noOcclusion());
    /** 0.4.522: what is in the rooms (PellThing), drawn by tools/draw_pell_things.py. The bare bulb and the television give light. */
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<PellThing>> THING_TYPE=CODECS.register("pell_thing",()->PellThing.CODEC);
    public static final DeferredBlock<PellThing> THING=BLOCKS.registerBlock("pell_thing",PellThing::new,fixed(Blocks.OAK_PLANKS).noOcclusion().lightLevel(PellThing::light));
    private PellRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);}
}
