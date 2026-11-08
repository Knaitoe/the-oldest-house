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
    public static final List<String> MATERIAL_NAMES=List.of("still_wall","still_ceiling","still_path","still_under",
        /* 0.4.386, the soft floor: wallpaper gone soft with damp, a ceiling stained brown, the firm landings and the packed earth below. */
        "soft_wall","soft_ceiling","soft_landing","soft_under",
        /* 0.4.387, the rising stair: wet glazed tile, a vaulted ceiling, silted flags at the foot, dry flags at the head, and windows lit from behind. */
        "rising_wall","rising_ceiling","rising_floor","rising_landing","rising_window",
        /* 0.4.388, the hall of many lengths: distempered plaster, a measuring rail at waist height, boards measured off in brass, a coffered ceiling. */
        "lengths_wall","lengths_ceiling","lengths_floor","lengths_rule");
    private static Block like(String name){return switch(name){case "still_under","rising_wall","rising_ceiling","rising_floor","rising_landing","rising_window"->Blocks.STONE_BRICKS;case "soft_under"->Blocks.PACKED_MUD;default->Blocks.DARK_OAK_PLANKS;};}
    static{for(String name:MATERIAL_NAMES)MATERIALS.put(name,BLOCKS.registerSimpleBlock("trial_"+name,name.equals("rising_window")?fixed(like(name)).lightLevel(s->12):fixed(like(name))));}
    public static Block material(String name){var block=MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    public static boolean isMaterial(net.minecraft.world.level.block.state.BlockState state){for(var block:MATERIALS.values())if(state.is(block.get()))return true;return false;}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<TrialBlock>> FITTING_TYPE=CODECS.register("trial_fitting",()->TrialBlock.CODEC);
    /** The fittings a trial is played on: the still light's boards that give and its lamps; the soft floor's squares and beams; the rising stair's treads. */
    public static final DeferredBlock<TrialBlock> FITTING=BLOCKS.registerBlock("trial_fitting",TrialBlock::new,fixed(Blocks.DARK_OAK_PLANKS).lightLevel(TrialBlock::light));
    private TrialRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);}
}
