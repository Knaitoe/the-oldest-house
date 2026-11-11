package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * 0.4.448 (owner: "The tree is old. So is the town and its secrets. There should be names all over this tree."): bark with names cut
 * in it, for the Triple Tree's burl and its trunks. A block of oak bark with a picture of two names laid over each of its four sides
 * (tools/draw_borrasca_names.py): pale tan where a cut has weathered, dark where the bark has closed over an old one, and here and
 * there a flat patch where a name was scraped out. No name is on the Tree twice.
 *
 * <p>{@link #FACE} 0 to 19 are the old names. 20 to 22 are the block beside the reader's own carving, which has Patrick's and
 * Kendra's on its south side: both there, his scraped out, both scraped out, and both patches gone grey years later
 * ({@link #friends}).
 */
public final class BorrascaNamesBlock extends Block {
    public static final int OLD=20,BOTH=20,HIS_GONE=21,BOTH_GONE=22,LONG_GONE=23;
    public static final IntegerProperty FACE=IntegerProperty.create("face",0,LONG_GONE);
    public BorrascaNamesBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACE,0));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACE);}
    public static boolean is(BlockState s){return s.is(BorrascaRegistry.NAMES.get());}
    /** Old names: which of the twenty faces a block at this place takes, so that neighbours differ. */
    public static BlockState old(int x,int y,int z){return BorrascaRegistry.NAMES.get().defaultBlockState().setValue(FACE,Math.floorMod(x*7+y*11+z*13,OLD));}
    /** The block beside the carving, as it stands on a visit: their two names, then his gone, then both. */
    public static BlockState friends(int visit){return BorrascaRegistry.NAMES.get().defaultBlockState().setValue(FACE,visit<=1?BOTH:visit==2?HIS_GONE:visit==3?BOTH_GONE:LONG_GONE);}
}
