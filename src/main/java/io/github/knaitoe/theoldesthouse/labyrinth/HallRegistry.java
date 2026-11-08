package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/**
 * 0.4.219, the texture pass's first release (docs/TEXTURE_PASS_HALLS_0_4_219.md): the ordinary halls' own surfaces. Plaster in three
 * variants, a picture rail, a skirting board, floorboards and worn floorboards (each with a twin turned a quarter for halls that run
 * east to west), a plaster ceiling, and a sash window in two halves. Drawn by tools/draw_hall_art.py. Each keeps the collision and
 * sound of the vanilla block it replaces.
 */
public final class HallRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    public static final List<String> NAMES=List.of("plaster","plaster_cracked","plaster_stained","rail","skirting","floor","floor_x","floor_worn","floor_worn_x","ceiling","window_lower","window_upper");
    private static final Map<String,DeferredBlock<Block>> SURFACES=new LinkedHashMap<>();
    static{
        for(String name:NAMES){
            var like=name.startsWith("floor")||name.equals("skirting")?Blocks.SPRUCE_PLANKS:name.equals("ceiling")?Blocks.WHITE_CONCRETE:Blocks.WHITE_TERRACOTTA;
            var properties=fixed(like);if(name.startsWith("window"))properties=properties.sound(SoundType.GLASS);
            SURFACES.put(name,BLOCKS.registerSimpleBlock("hall_"+name,properties));
        }
    }
    /** 0.4.374, the texture pass's second release (docs/TEXTURE_PASS_STONE_0_4_374.md): the stone halls' own surfaces (tools/draw_stone_hall_art.py). */
    public static final List<String> STONE_NAMES=List.of("ashlar","ashlar_weathered","ashlar_cracked","ashlar_damp","damp_course","flags","flags_worn","carved");
    private static final Map<String,DeferredBlock<Block>> STONE=new LinkedHashMap<>();
    static{
        for(String name:STONE_NAMES)STONE.put(name,BLOCKS.registerSimpleBlock("hall_stone_"+name,fixed(name.startsWith("flags")?Blocks.POLISHED_ANDESITE:Blocks.STONE_BRICKS)));
    }
    public static Block stone(String name){var block=STONE.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static boolean isStone(net.minecraft.world.level.block.state.BlockState state){for(var block:STONE.values())if(state.is(block.get()))return true;return false;}
    public static Block surface(String name){var block=SURFACES.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static boolean is(net.minecraft.world.level.block.state.BlockState state){for(var block:SURFACES.values())if(state.is(block.get()))return true;return false;}
    private HallRegistry(){}
    public static void register(IEventBus bus){BLOCKS.register(bus);}
}
