package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/**
 * 0.4.274, Second Person (docs/SECOND_PERSON_0_4_274.md): the street and the flat above the launderette. Twelve materials, the one
 * fitting block, and the thing a reader carries for the resident (the routine item, the coat, the drawing, the photograph). Drawn by
 * tools/draw_second_person_art.py.
 */
public final class SecondPersonRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    private static final Map<String,DeferredBlock<Block>> MATERIALS=new LinkedHashMap<>();
    public static final List<String> MATERIAL_NAMES=List.of("brick","paving","road","kerb","shopfront","paper","kitchen","carpet","lino","tile","ceiling","launderette");
    static{for(String name:MATERIAL_NAMES)MATERIALS.put(name,BLOCKS.registerSimpleBlock("sp_"+name,fixed(name.equals("shopfront")||name.equals("carpet")?Blocks.OAK_PLANKS:Blocks.STONE)));}
    public static Block material(String name){var block=MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<SecondPersonBlock>> FITTING_TYPE=CODECS.register("sp_fitting",()->SecondPersonBlock.CODEC);
    public static final DeferredBlock<SecondPersonBlock> FITTING=BLOCKS.registerBlock("sp_fitting",SecondPersonBlock::new,fixed(Blocks.OAK_PLANKS).noOcclusion());
    /** What a reader carries for the resident: the routine item (custom model data 1-16, by its number), the coat (17), the drawing (18), the photograph (19). */
    public static final DeferredItem<Item> CARRIED=ITEMS.registerSimpleItem("sp_carried",new Item.Properties().stacksTo(1));
    public static final int COAT_MODEL=17,SHEET_MODEL=18,PHOTO_MODEL=19,TICKET_MODEL=20,SLEEVE_MODEL=21,RECEIVER_MODEL=22;
    private SecondPersonRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);ITEMS.register(bus);}
}
