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
 * 0.4.228, Vacant Possession (docs/VACANT_POSSESSION_0_4_228.md): the flat's wallpaper, boards (and the boards the rug kept new),
 * plaster, the stair's carpet and the landing's lino, the building's brick, the street's pavement, road and kerb, the van's panels,
 * wheels and floor; the one fitting block; and the piece in the reader's arms while it is carried. Drawn by tools/draw_vacant_art.py.
 */
public final class VacantRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    private static final Map<String,DeferredBlock<Block>> MATERIALS=new LinkedHashMap<>();
    public static final List<String> MATERIAL_NAMES=List.of("wallpaper","boards","boards_new","plaster","stair_carpet","lino","brick","pavement","road","kerb","van_panel","van_wheel","van_floor");
    static{for(String name:MATERIAL_NAMES)MATERIALS.put(name,BLOCKS.registerSimpleBlock("vacant_"+name,fixed(name.startsWith("boards")||name.equals("stair_carpet")?Blocks.OAK_PLANKS:name.startsWith("van")?Blocks.IRON_BLOCK:Blocks.STONE)));}
    public static Block material(String name){var block=MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<VacantBlock>> FITTING_TYPE=CODECS.register("vacant_fitting",()->VacantBlock.CODEC);
    public static final DeferredBlock<VacantBlock> FITTING=BLOCKS.registerBlock("vacant_fitting",VacantBlock::new,fixed(Blocks.OAK_PLANKS).noOcclusion().lightLevel(VacantBlock::light));
    /** A piece of the tenant's furniture in the reader's arms: drawn as the piece itself (custom model data, one to nine). */
    public static final DeferredItem<Item> PIECE=ITEMS.registerSimpleItem("vacant_piece",new Item.Properties().stacksTo(1));
    private VacantRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);ITEMS.register(bus);}
}
