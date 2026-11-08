package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/**
 * 0.4.390, the new ordinary halls of docs/HALLS_TRIALS_AND_TIME_DESIGN.md (OrdinaryHalls): their materials, the washing on the
 * laundry corridor's lines, and their fittings. Drawn by tools/draw_ordinary_hall_art.py.
 */
public final class OrdinaryHallRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    private static final Map<String,DeferredBlock<Block>> MATERIALS=new LinkedHashMap<>();
    /** The laundry corridor: limewashed brick, its foot tarred against the wet, quarry tiles and a boarded ceiling. */
    public static final List<String> MATERIAL_NAMES=List.of("laundry_wall","laundry_wall_low","laundry_floor","laundry_ceiling",
        /* 0.4.391, the long bedroom hall: sprigged wallpaper over a panelled dado, boards with a runner down them, a plaster ceiling, and painted door frames. */
        "bedroom_wall","bedroom_dado","bedroom_floor","bedroom_runner","bedroom_ceiling","bedroom_trim",
        /* 0.4.391, the atrium: panelled plaster, a chequered floor, the balcony's boards, a coffered ceiling, stone steps and a skylight lit from above. */
        "atrium_wall","atrium_floor","atrium_deck","atrium_ceiling","atrium_step","atrium_skylight",
        /* 0.4.391, the cistern walk: wet dressed stone, the walk's flags, silted stone under the water, the columns and the vault. */
        "cistern_wall","cistern_floor","cistern_under","cistern_pillar","cistern_vault",
        /* 0.4.391, the library stacks: dark panelling, worn boards and a beamed ceiling. */
        "stacks_wall","stacks_floor","stacks_ceiling");
    private static Block like(String name){
        if(name.startsWith("cistern")||name.equals("atrium_step"))return Blocks.STONE_BRICKS;
        if(name.endsWith("floor")||name.endsWith("deck")||name.endsWith("ceiling")||name.endsWith("trim")||name.equals("bedroom_dado")||name.equals("stacks_wall"))return name.equals("laundry_floor")||name.equals("atrium_floor")?Blocks.BRICKS:Blocks.SPRUCE_PLANKS;
        if(name.equals("bedroom_runner"))return Blocks.WHITE_WOOL;
        if(name.equals("atrium_skylight"))return Blocks.GLASS;
        return Blocks.BRICKS;
    }
    static{for(String name:MATERIAL_NAMES)MATERIALS.put(name,BLOCKS.registerSimpleBlock("hall_"+name,name.equals("atrium_skylight")?fixed(like(name)).lightLevel(s->14):fixed(like(name))));}
    public static Block material(String name){var block=MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static boolean isMaterial(net.minecraft.world.level.block.state.BlockState state){for(var block:MATERIALS.values())if(state.is(block.get()))return true;return false;}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<HallSheetBlock>> SHEET_TYPE=CODECS.register("hall_sheet",()->HallSheetBlock.CODEC);
    /** Washing on a line (HallSheetBlock): no collision, light passes, it swings when walked through. */
    public static final DeferredBlock<HallSheetBlock> SHEET=BLOCKS.registerBlock("hall_sheet",HallSheetBlock::new,
        fixed(Blocks.WHITE_WOOL).noCollission().noOcclusion().sound(SoundType.WOOL).pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK));
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<OrdinaryHallBlock>> FITTING_TYPE=CODECS.register("hall_fitting",()->OrdinaryHallBlock.CODEC);
    public static final DeferredBlock<OrdinaryHallBlock> FITTING=BLOCKS.registerBlock("hall_fitting",OrdinaryHallBlock::new,fixed(Blocks.SPRUCE_PLANKS).noOcclusion());
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<HallFaceBlock>> FACE_TYPE=CODECS.register("hall_face",()->HallFaceBlock.CODEC);
    /** 0.4.391: painted doors, and 0.4.391 shelves of books, that say what they are when tried (HallFaceBlock). */
    public static final DeferredBlock<HallFaceBlock> FACE=BLOCKS.registerBlock("hall_face",HallFaceBlock::new,fixed(Blocks.SPRUCE_PLANKS));
    /** 0.4.391: the long bedroom hall's two doors that open (HallDoorBlock). */
    public static final DeferredBlock<HallDoorBlock> DOOR=BLOCKS.registerBlock("hall_door",HallDoorBlock::new,fixed(Blocks.SPRUCE_DOOR).noOcclusion());
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<HallDripBlock>> DRIP_TYPE=CODECS.register("hall_cistern_drip",()->HallDripBlock.CODEC);
    /** 0.4.391: the cistern's vault where it drips (HallDripBlock). */
    public static final DeferredBlock<HallDripBlock> DRIP=BLOCKS.registerBlock("hall_cistern_drip",HallDripBlock::new,fixed(Blocks.STONE_BRICKS));
    private OrdinaryHallRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);}
}
