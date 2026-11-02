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
 * 0.4.257, A Place for You (docs/A_PLACE_FOR_YOU_0_4_257.md): Mara's house. The sage clapboard and the shingles, the hall's and the
 * sitting room's papers, the floorboards, the kitchen's linoleum, the porch boards, the bathroom's tiles and the cream trim; the one
 * fitting block; and the things a reader may carry for the household (Kit's bag, the serving spoon, a plate, the cups). Drawn by
 * tools/draw_place_for_you_art.py.
 */
public final class PlaceForYouRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    private static final Map<String,DeferredBlock<Block>> MATERIALS=new LinkedHashMap<>();
    public static final List<String> MATERIAL_NAMES=List.of("siding","shingle","paper","stripe","boards","lino","porch","tile","trim");
    static{for(String name:MATERIAL_NAMES)MATERIALS.put(name,BLOCKS.registerSimpleBlock("pfy_"+name,fixed(name.equals("boards")||name.equals("siding")||name.equals("porch")||name.equals("trim")?Blocks.OAK_PLANKS:Blocks.STONE)));}
    public static Block material(String name){var block=MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<PlaceForYouBlock>> FITTING_TYPE=CODECS.register("pfy_fitting",()->PlaceForYouBlock.CODEC);
    public static final DeferredBlock<PlaceForYouBlock> FITTING=BLOCKS.registerBlock("pfy_fitting",PlaceForYouBlock::new,fixed(Blocks.OAK_PLANKS).noOcclusion());
    /** What a reader carries for the household: Kit's bag (custom model data 1), the serving spoon (2), a plate (3), the cups (4). */
    public static final DeferredItem<Item> CARRIED=ITEMS.registerSimpleItem("pfy_carried",new Item.Properties().stacksTo(1));
    public static final int BAG=1,SPOON=2,PLATE=3,CUPS=4;
    private PlaceForYouRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);ITEMS.register(bus);}
}
