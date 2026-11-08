package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/** 0.4.385, the trial rooms (TrialRooms): their materials and fittings. Drawn by tools/draw_trial_art.py. */
public final class TrialRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    private static final Map<String,DeferredBlock<Block>> MATERIALS=new LinkedHashMap<>();
    /** The still light: its blackened wainscot, its smoke-dark ceiling, the boards worn pale where the way runs, and the wet brick under the floor. */
    public static final List<String> MATERIAL_NAMES=List.of("still_wall","still_ceiling","still_path","still_under");
    static{for(String name:MATERIAL_NAMES)MATERIALS.put(name,BLOCKS.registerSimpleBlock("trial_"+name,fixed(name.endsWith("under")?Blocks.STONE_BRICKS:Blocks.DARK_OAK_PLANKS)));}
    public static Block material(String name){var block=MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static boolean isMaterial(net.minecraft.world.level.block.state.BlockState state){for(var block:MATERIALS.values())if(state.is(block.get()))return true;return false;}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<TrialBlock>> FITTING_TYPE=CODECS.register("trial_fitting",()->TrialBlock.CODEC);
    /** The fittings a trial is played on: the still light's boards that give and its lamps. */
    public static final DeferredBlock<TrialBlock> FITTING=BLOCKS.registerBlock("trial_fitting",TrialBlock::new,fixed(Blocks.DARK_OAK_PLANKS).lightLevel(TrialBlock::light));
    private TrialRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);}
}
