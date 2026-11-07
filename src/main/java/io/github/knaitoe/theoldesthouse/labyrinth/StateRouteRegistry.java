package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/**
 * 0.4.333, State Route 9 (docs/STATE_ROUTE_0_4_333.md): the highway's anchor (which keeps the road's one shared state and draws the
 * traffic), the roadside fittings, the padlocked chain-link gate, the brass key, the county-line postcard and the amber reflector, and
 * the road's sounds. Drawn by tools/draw_state_route_art.py; heard from tools/generate_state_route_audio.py.
 */
public final class StateRouteRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    private static final java.util.Map<String,DeferredBlock<Block>> MATERIALS=new java.util.LinkedHashMap<>();
    /** Wet asphalt, and the rows of it that carry the lines: a white edge on the near or far side, the dashed lane line, the yellow edges by the median. */
    public static final java.util.List<String> MATERIAL_NAMES=java.util.List.of("asphalt","edge_white_s","edge_white_n","dash_n","edge_yellow_n","edge_yellow_s");
    static{for(String name:MATERIAL_NAMES)MATERIALS.put(name,BLOCKS.registerSimpleBlock("state_route_"+name,fixed(Blocks.BLACKSTONE)));}
    public static Block material(String name){var block=MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<StateRouteAnchorBlock>> ANCHOR_TYPE=CODECS.register("state_route_anchor",()->StateRouteAnchorBlock.CODEC);
    public static final DeferredBlock<StateRouteAnchorBlock> ANCHOR=BLOCKS.registerBlock("state_route_anchor",StateRouteAnchorBlock::new,fixed(Blocks.STONE));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<StateRouteScene>> SCENE=BES.register("state_route_scene",()->BlockEntityType.Builder.of(StateRouteScene::new,ANCHOR.get()).build(null));
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<StateRouteBlock>> FITTING_TYPE=CODECS.register("state_route_fitting",()->StateRouteBlock.CODEC);
    public static final DeferredBlock<StateRouteBlock> FITTING=BLOCKS.registerBlock("state_route_fitting",StateRouteBlock::new,fixed(Blocks.IRON_BLOCK).noOcclusion().lightLevel(StateRouteBlock::light));
    /** The pedestrian gate in the chain-link fence on the starting shoulder: padlocked until the brass key turns in it. The scene's way on. */
    public static final DeferredBlock<DoorBlock> GATE=BLOCKS.register("state_route_gate",()->new DoorBlock(BlockSetType.IRON,BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_DOOR).strength(-1,3600000).noOcclusion().noLootTable()));
    /** The brass key from the mailbox, its note wrapped round it. Only for the gate; it never leaves the road. */
    public static final DeferredItem<Item> KEY=ITEMS.registerSimpleItem("state_route_key",new Item.Properties().stacksTo(1));
    /** The unmailed postcard of the county line, in the mailbox for a reader the House read as one who takes the road. */
    public static final DeferredItem<Item> POSTCARD=ITEMS.registerSimpleItem("state_route_postcard",new Item.Properties().stacksTo(1));
    /** A cracked amber reflector button off a guardrail post: the road's keepsake. Carried, it leans the dealing toward roads and open air. */
    public static final DeferredItem<Item> REFLECTOR=ITEMS.registerSimpleItem("state_route_reflector",new Item.Properties().stacksTo(1));
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String id){return SOUNDS.register(id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,id)));}
    /** A slow pickup's diesel; high-speed tires on wet tar; the semi's horn going by; a car passing; the wind off a trailer. */
    public static final DeferredHolder<SoundEvent,SoundEvent> DIESEL=sound("route.diesel"),HISS=sound("route.hiss"),HORN=sound("route.horn"),PASS=sound("route.pass"),WAKE=sound("route.wake");
    /** Brakes and the blackout; engines ticking in the rain at a crawl; every horn on the highway at once; the padlock. */
    public static final DeferredHolder<SoundEvent,SoundEvent> SCREECH=sound("route.screech"),TICK=sound("route.tick"),CHORD=sound("route.chord"),PADLOCK=sound("route.padlock");
    private StateRouteRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);ITEMS.register(bus);BES.register(bus);SOUNDS.register(bus);}
}
