package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/**
 * 0.4.641, A sort of thing (docs/SLEEPING_KING_PLAN.md): the country's materials, each twice (filled, and ruled: its own outline on
 * paper, derived by tools/draw_king_art.py from the filled picture), the drawing-room's own materials, and the one fitting block.
 */
public final class KingRegistry {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Block>> CODECS=DeferredRegister.create(Registries.BLOCK_TYPE,TheOldestHouse.MOD_ID);
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(TheOldestHouse.MOD_ID);
    /** 0.4.644: the bottle and the cake from the glass table on the hill. */
    public static final net.neoforged.neoforge.registries.DeferredItem<KingDraughtItem> BOTTLE=ITEMS.register("king_bottle",()->new KingDraughtItem(false)),CAKE=ITEMS.register("king_cake",()->new KingDraughtItem(true));
    private static final DeferredRegister<net.minecraft.sounds.SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheOldestHouse.MOD_ID);
    private static DeferredHolder<net.minecraft.sounds.SoundEvent,net.minecraft.sounds.SoundEvent> sound(String id){return SOUNDS.register(id,()->net.minecraft.sounds.SoundEvent.createVariableRangeEvent(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,id)));}
    /** His breath, in and out (tools/generate_king_audio.py), and a sort of thing's soft feet. */
    public static final DeferredHolder<net.minecraft.sounds.SoundEvent,net.minecraft.sounds.SoundEvent> BREATH_IN=sound("king.breath_in"),BREATH_OUT=sound("king.breath_out"),SOFT_STEP=sound("king.soft_step"),
        /** 0.4.643: he stirs in his sleep; the crow over the wood. */
        STIR=sound("king.stir"),CROW=sound("king.crow"),
        // 0.4.644: the train starting and its whistle before the jump.
        TRAIN=sound("king.train"),WHISTLE=sound("king.whistle");
    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    /** The country's materials; every one has its ruled twin, named with "_ruled". */
    public static final List<String> COUNTRY=List.of("turf_light","turf_dark","soil","hedge","brook","bridge","path","bark","bough");
    public static final List<String> ROOM=List.of("wallpaper","panel","floorboards","ceiling","chimney","hearth");
    public static final List<String> MATERIAL_NAMES=List.of("turf_light","turf_dark","soil","hedge","brook","bridge","path","bark","bough",
        "turf_light_ruled","turf_dark_ruled","soil_ruled","hedge_ruled","brook_ruled","bridge_ruled","path_ruled","bark_ruled","bough_ruled",
        "wallpaper","panel","floorboards","ceiling","chimney","hearth");
    private static final Map<String,DeferredBlock<Block>> MATERIALS=new LinkedHashMap<>();
    private static Block like(String name){
        String base=name.replace("_ruled","");
        return switch(base){case "turf_light","turf_dark"->Blocks.MOSS_BLOCK;case "soil"->Blocks.DIRT;case "hedge","bough"->Blocks.OAK_PLANKS;
            case "bridge","bark","panel","floorboards"->Blocks.OAK_PLANKS;case "path"->Blocks.GRAVEL;default->Blocks.STONE;};
    }
    static{for(String name:MATERIAL_NAMES){var props=fixed(like(name));if(name.startsWith("brook"))props=props.noOcclusion();
        // 0.4.642: a hedge holds a thing and gives to anything less (KingHedgeBlock); a body inside one is not choked or blinded.
        if(name.startsWith("hedge")){var hedge=props.isSuffocating((s,l,p)->false).isViewBlocking((s,l,p)->false);MATERIALS.put(name,BLOCKS.registerBlock("king_"+name,KingHedgeBlock::new,hedge));}
        else MATERIALS.put(name,BLOCKS.registerSimpleBlock("king_"+name,props));}}
    public static Block material(String name){var block=MATERIALS.get(name);if(block==null)throw new IllegalArgumentException(name);return block.get();}
    /** A country material, filled or ruled. */
    public static BlockState country(String name,boolean ruled){return material(ruled?name+"_ruled":name).defaultBlockState();}
    /** 0.4.642: whether a block is one of the country's materials, filled or ruled. */
    public static boolean isCountry(BlockState s){for(var name:COUNTRY){if(s.is(MATERIALS.get(name).get())||s.is(MATERIALS.get(name+"_ruled").get()))return true;}return false;}
    /** 0.4.642: a country material as its filled or ruled twin; null for anything else. */
    public static BlockState twin(BlockState s,boolean ruled){
        for(var name:COUNTRY){if(s.is(MATERIALS.get(name).get())||s.is(MATERIALS.get(name+"_ruled").get()))return country(name,ruled);}
        return null;
    }
    public static boolean material(BlockState s){for(var b:MATERIALS.values())if(s.is(b.get()))return true;return false;}
    /** Whether a block is a country material drawn ruled. */
    public static boolean ruled(BlockState s){for(var e:MATERIALS.entrySet())if(e.getKey().endsWith("_ruled")&&s.is(e.getValue().get()))return true;return false;}
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<KingBlock>> FITTING_TYPE=CODECS.register("king_fitting",()->KingBlock.CODEC);
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<KingHedgeBlock>> HEDGE_TYPE=CODECS.register("king_hedge",()->KingHedgeBlock.CODEC);
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<KingVergeBlock>> VERGE_TYPE=CODECS.register("king_verge",()->KingVergeBlock.CODEC);
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<KingDrawnBridgeBlock>> DRAWN_TYPE=CODECS.register("king_drawn_bridge",()->KingDrawnBridgeBlock.CODEC);
    /** 0.4.642: the brook's unseen verge (KingVergeBlock). */
    public static final DeferredBlock<KingVergeBlock> VERGE=BLOCKS.registerBlock("king_verge",KingVergeBlock::new,BlockBehaviour.Properties.of().noOcclusion().strength(-1,3600000).noLootTable().isSuffocating((s,l,p)->false).isViewBlocking((s,l,p)->false));
    /** 0.4.645: the drawn bridge beyond the fifth square (KingDrawnBridgeBlock). */
    public static final DeferredBlock<KingDrawnBridgeBlock> DRAWN_BRIDGE=BLOCKS.registerBlock("king_drawn_bridge",KingDrawnBridgeBlock::new,fixed(Blocks.OAK_PLANKS).noOcclusion().isSuffocating((s,l,p)->false).isViewBlocking((s,l,p)->false));
    public static final DeferredBlock<KingBlock> FITTING=BLOCKS.registerBlock("king_fitting",KingBlock::new,fixed(Blocks.OAK_PLANKS).noOcclusion().lightLevel(KingBlock::light));
    private KingRegistry(){}
    public static void register(IEventBus bus){CODECS.register(bus);BLOCKS.register(bus);SOUNDS.register(bus);ITEMS.register(bus);}
}
