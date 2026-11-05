package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/**
 * 0.4.310, A Deer in the Road (docs/DEER_ROAD_0_4_310.md): the scene's anchor (which keeps the road's one shared state and draws the
 * car, the doe and the next vehicle), the stopped car's cells, the roadside fittings, the drainage gate, the warning triangle and the
 * seats. Drawn by tools/draw_deer_road_art.py.
 */
public final class DeerRoadRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<EntityType<?>> ENTITIES=DeferredRegister.create(Registries.ENTITY_TYPE,TheOldestHouse.MOD_ID);
    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    private static final Map<String,DeferredBlock<Block>> MATERIALS=new LinkedHashMap<>();
    /** The hatched shoulder behind the wreck, and the mud in the ditch's bed. */
    public static final List<String> MATERIAL_NAMES=List.of("hatch","mud");
    static{for(String name:MATERIAL_NAMES)MATERIALS.put(name,BLOCKS.registerSimpleBlock("deer_road_"+name,fixed(name.equals("mud")?Blocks.MUD:Blocks.STONE)));}
    public static Block material(String name){var block=MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<DeerRoadAnchorBlock>> ANCHOR_TYPE=CODECS.register("deer_road_anchor",()->DeerRoadAnchorBlock.CODEC);
    public static final DeferredBlock<DeerRoadAnchorBlock> ANCHOR=BLOCKS.registerBlock("deer_road_anchor",DeerRoadAnchorBlock::new,fixed(Blocks.STONE));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<DeerRoadScene>> SCENE=BES.register("deer_road_scene",()->BlockEntityType.Builder.of(DeerRoadScene::new,ANCHOR.get()).build(null));
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<DeerCarBlock>> CAR_TYPE=CODECS.register("deer_road_car",()->DeerCarBlock.CODEC);
    /** The stopped car's cells; the cabin's middle cell is the dome light while a door stands open. */
    public static final DeferredBlock<DeerCarBlock> CAR=BLOCKS.registerBlock("deer_road_car",DeerCarBlock::new,fixed(Blocks.IRON_BLOCK).noOcclusion().dynamicShape()
            .lightLevel(s->s.getValue(DeerCarBlock.CELL)==DeerCarCells.cell(0,1,0)&&s.getValue(DeerCarBlock.MASK)!=0?8:0));
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<DeerRoadBlock>> FITTING_TYPE=CODECS.register("deer_road_fitting",()->DeerRoadBlock.CODEC);
    public static final DeferredBlock<DeerRoadBlock> FITTING=BLOCKS.registerBlock("deer_road_fitting",DeerRoadBlock::new,fixed(Blocks.IRON_BLOCK).noOcclusion().lightLevel(DeerRoadBlock::light));
    /** The hinged drainage gate at the head of the ditch: hand-opened, though it sounds like iron. The scene's way on. */
    public static final DeferredBlock<DoorBlock> GATE=BLOCKS.register("deer_road_gate",()->new DoorBlock(BlockSetType.COPPER,BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_DOOR).strength(-1,3600000).noOcclusion().noLootTable()));
    /** The warning triangle from the boot; set on the hatched shoulder it slows the next vehicle. */
    public static final DeferredItem<Item> TRIANGLE=ITEMS.register("deer_road_triangle",()->new DeerRoadTriangleItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<EntityType<?>,EntityType<DeerRoadSeat>> SEAT=ENTITIES.register("deer_road_seat",()->EntityType.Builder.<DeerRoadSeat>of(DeerRoadSeat::new,MobCategory.MISC).sized(.1F,.1F).clientTrackingRange(10).updateInterval(1).build("deer_road_seat"));
    private DeerRoadRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);ITEMS.register(bus);BES.register(bus);ENTITIES.register(bus);}
}
