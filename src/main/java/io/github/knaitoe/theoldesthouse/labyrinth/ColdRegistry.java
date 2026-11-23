package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/**
 * 0.4.754, the cold room (docs/COLD_ROOM_0_4_754.md): the rented house's materials, its fittings, its two flights of stairs and its
 * sounds. Drawn by tools/draw_cold_room_art.py; heard from tools/generate_cold_room_audio.py.
 */
public final class ColdRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    private static final java.util.Map<String,DeferredBlock<Block>> MATERIALS=new java.util.LinkedHashMap<>();
    /**
     * Inside: the hall's boards, the stair's and landing's runner, the faded paper on every wall, the ceilings' plaster, the kitchen's
     * linoleum. Outside: the house's brick, the rendered wall that holds the steps, the steps' worn flags.
     */
    public static final java.util.List<String> MATERIAL_NAMES=java.util.List.of("boards","runner","paper","plaster","lino","brick","render","flags");
    static{for(String name:MATERIAL_NAMES)MATERIALS.put(name,BLOCKS.registerSimpleBlock("cold_room_"+name,fixed(switch(name){case "boards"->Blocks.SPRUCE_PLANKS;case "runner"->Blocks.RED_WOOL;case "brick"->Blocks.BRICKS;case "flags","render"->Blocks.STONE;default->Blocks.WHITE_CONCRETE;})));}
    public static Block material(String name){var block=MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    /** The stair inside, treads of the hall's boards with the runner up the middle; the steps outside, of the flags. */
    public static final DeferredBlock<StairBlock> TREADS=BLOCKS.registerBlock("cold_room_treads",p->new StairBlock(Blocks.SPRUCE_PLANKS.defaultBlockState(),p),fixed(Blocks.SPRUCE_STAIRS));
    public static final DeferredBlock<StairBlock> STEPS=BLOCKS.registerBlock("cold_room_steps",p->new StairBlock(Blocks.STONE.defaultBlockState(),p),fixed(Blocks.STONE_BRICK_STAIRS));
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<ColdBlock>> FITTING_TYPE=CODECS.register("cold_room_fitting",()->ColdBlock.CODEC);
    public static final DeferredBlock<ColdBlock> FITTING=BLOCKS.registerBlock("cold_room_fitting",ColdBlock::new,fixed(Blocks.SPRUCE_PLANKS).noOcclusion().lightLevel(ColdBlock::light));
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String id){return SOUNDS.register(id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,id)));}
    /** The house at night (wind at the glass, the radiator's tick); the stairs' boards under a foot. */
    public static final DeferredHolder<SoundEvent,SoundEvent> ROOM=sound("cold_room.room"),BOARDS=sound("cold_room.boards");
    private ColdRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);SOUNDS.register(bus);}
}
