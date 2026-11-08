package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/**
 * 0.4.376, the texture pass's third release (docs/TEXTURE_PASS_STAIRCASE_0_4_376.md): the great staircase's own well and rails.
 * The well's stone in three bands that darken with depth, each with a fluted pier for the pilasters, and a wrought-iron balustrade
 * for the landings. Drawn by tools/draw_stair_well_art.py. The stone keeps the collision and sound of the deepslate tiles and
 * chiselled deepslate it replaces; the balustrade is iron bars underneath (it connects, collides and breaks off at a gap as they do)
 * with its own posts, rails and balusters.
 */
public final class StairWell {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(TheOldestHouse.MOD_ID);
    private static BlockBehaviour.Properties fixed(Block like){return BlockBehaviour.Properties.ofFullCopy(like).strength(-1,3600000).noLootTable();}
    public static final List<String> BANDS=List.of("upper","middle","lower");
    private static final Map<String,DeferredBlock<Block>> STONE=new LinkedHashMap<>(),PIER=new LinkedHashMap<>();
    static{
        for(String band:BANDS){
            STONE.put(band,BLOCKS.registerSimpleBlock("stair_well_"+band,fixed(Blocks.DEEPSLATE_TILES)));
            PIER.put(band,BLOCKS.registerSimpleBlock("stair_well_pier_"+band,fixed(Blocks.CHISELED_DEEPSLATE)));
        }
    }
    /** Iron bars underneath, with their own model. */
    public static final class Balustrade extends IronBarsBlock{
        public static final MapCodec<Balustrade> CODEC=simpleCodec(Balustrade::new);
        public Balustrade(BlockBehaviour.Properties p){super(p);}
        @Override public MapCodec<? extends IronBarsBlock> codec(){return CODEC;}
    }
    public static final DeferredBlock<Balustrade> BALUSTRADE=BLOCKS.registerBlock("stair_well_balustrade",Balustrade::new,fixed(Blocks.IRON_BARS));

    /** The three bands of the descent: the first third down from the top, the second, and everything below that. */
    public static String band(int y){
        int fall=FinaleArchitecture.TOP-y,third=(FinaleArchitecture.TOP-FinaleArchitecture.ARENA)/3;
        return fall<third?"upper":fall<2*third?"middle":"lower";
    }
    public static BlockState stone(int y){return STONE.get(band(y)).get().defaultBlockState();}
    public static BlockState pier(int y){return PIER.get(band(y)).get().defaultBlockState();}
    /** The well's own stone or piers, in any band. */
    public static boolean isWall(BlockState s){for(var b:STONE.values())if(s.is(b.get()))return true;for(var b:PIER.values())if(s.is(b.get()))return true;return false;}
    /** A rail: the balustrade, or plain iron bars where something else laid them. */
    public static boolean isRail(BlockState s){return s.is(BALUSTRADE.get())||s.is(Blocks.IRON_BARS);}
    private StairWell(){}
    public static void register(IEventBus bus){BLOCKS.register(bus);}
}
