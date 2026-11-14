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
 * 0.4.409, the city on the ice (docs/THE_BLACK_CITY_0_4_409.md): the camp's, the pass's and the city's materials, the camp's fittings, the
 * carved panels, what fills the tunnel, and their sounds. Drawn by tools/draw_city_art.py; heard from tools/generate_city_audio.py.
 */
public final class CityRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    private static final java.util.Map<String,DeferredBlock<Block>> MATERIALS=new java.util.LinkedHashMap<>();
    /**
     * The city's dark stone, the same stone carved in fives, packed snow, blue ice, the tents' canvas, and the huts' weathered boards.
     */
    public static final java.util.List<String> MATERIAL_NAMES=java.util.List.of("basalt","fives","snow","ice","canvas","boards");
    static{for(String name:MATERIAL_NAMES)MATERIALS.put(name,BLOCKS.registerSimpleBlock("city_"+name,fixed(switch(name){case "snow"->Blocks.SNOW_BLOCK;case "ice"->Blocks.PACKED_ICE;case "canvas"->Blocks.WHITE_WOOL;case "boards"->Blocks.SPRUCE_PLANKS;default->Blocks.DEEPSLATE;})));}
    public static Block material(String name){var block=MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<CityBlock>> FITTING_TYPE=CODECS.register("city_fitting",()->CityBlock.CODEC);
    public static final DeferredBlock<CityBlock> FITTING=BLOCKS.registerBlock("city_fitting",CityBlock::new,fixed(Blocks.SPRUCE_PLANKS).noOcclusion().lightLevel(CityBlock::light));
    /** 0.4.579: the aeroplane, a part to each cell it stands in: doped fabric over a frame, which stops a body where there is any of it. */
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<CityPlaneBlock>> PLANE_TYPE=CODECS.register("city_plane",()->CityPlaneBlock.CODEC);
    public static final DeferredBlock<CityPlaneBlock> PLANE=BLOCKS.registerBlock("city_plane",CityPlaneBlock::new,fixed(Blocks.WHITE_WOOL).noOcclusion());
    /** 0.4.579: the fixed rope up the pass and its stakes; it stops nobody. */
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<CityRopeBlock>> ROPE_TYPE=CODECS.register("city_rope",()->CityRopeBlock.CODEC);
    public static final DeferredBlock<CityRopeBlock> ROPE=BLOCKS.registerBlock("city_rope",CityRopeBlock::new,fixed(Blocks.SPRUCE_PLANKS).noOcclusion().noCollission());
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<CityMural>> MURAL_TYPE=CODECS.register("city_mural",()->CityMural.CODEC);
    public static final DeferredBlock<CityMural> MURAL=BLOCKS.registerBlock("city_mural",CityMural::new,fixed(Blocks.DEEPSLATE));
    /** What fills the tunnel: drawn, never touched; it stops nobody, and a body inside it is taken into the dark. */
    public static final DeferredBlock<Block> MASS=BLOCKS.registerSimpleBlock("city_mass",BlockBehaviour.Properties.of().noCollission().noLootTable().strength(-1,3600000)
        .sound(SoundType.SLIME_BLOCK).isSuffocating((s,l,p)->false).isViewBlocking((s,l,p)->false).isRedstoneConductor((s,l,p)->false));
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String id){return SOUNDS.register(id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,id)));}
    /** Wind across the ice; the aeroplane's torn fabric; something piping in the dark; wings in the dark; the wet push in the tunnel. */
    public static final DeferredHolder<SoundEvent,SoundEvent> WIND=sound("city.wind"),FLAP=sound("city.flap"),PIPING=sound("city.piping"),BIRDS=sound("city.birds"),MASS_PUSH=sound("city.mass");
    private CityRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);SOUNDS.register(bus);}
}
