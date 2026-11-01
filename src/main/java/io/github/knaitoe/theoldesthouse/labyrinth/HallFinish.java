package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 0.4.219, the texture pass's first release (docs/TEXTURE_PASS_HALLS_0_4_219.md): the last thing done to an ordinary hall as it is
 * built. It changes only the hall's own surfaces, and only from the vanilla block each was laid in, one for one, so nothing's shape,
 * collision, door, block entity or story changes:
 * <ul>
 * <li>the white terracotta walls become plaster, a crack or a water stain here and there, with a picture rail round the hall;</li>
 * <li>the skirting at the foot of the walls becomes a skirting board;</li>
 * <li>the spruce boards become floorboards running the length of the hall, worn pale down the middle of each passage;</li>
 * <li>the white concrete ceiling becomes plaster;</li>
 * <li>the grey glass windows become two-pane sash windows with nothing but dark behind them.</li>
 * </ul>
 * Everything is read and written through {@link BuildBlocks}, and every choice comes from coordinates relative to the hall, so a
 * paced build is the same as a direct one, block for block.
 */
public final class HallFinish {
    private HallFinish(){}

    /** Any of the halls' own surfaces, or the terracotta they replaced: walls that other passes may still treat as plain fabric. */
    public static boolean fabric(BlockState s){return s.is(Blocks.WHITE_TERRACOTTA)||HallRegistry.is(s);}

    /** A domestic hall (the shapes {@link HallVariations} builds): the whole shell, up to its roof. */
    public static void domestic(ServerLevel l,BlockPos b,LabyrinthPlace p){
        dress(l,b,HallVariations.shell(p),HallVariations.roof(p),rel->false,true);
    }
    /** One of the first halls: its corridor's walls, skirting and ceiling; its floor keeps its own boards or stone. */
    public static void first(ServerLevel l,BlockPos b,LabyrinthPlace p,Predicate<BlockPos> fragment){
        dress(l,b,LabyrinthHalls.floor(p),LabyrinthHalls.height(p),fragment,false);
    }

    static void dress(ServerLevel l,BlockPos b,Set<BlockPos> open,int roof,Predicate<BlockPos> skip,boolean boards){
        int rail=roof>=4?3:roof-1;
        // Floors and ceilings first, then walls, so a block that is both (the vaulted hall's neck roofs, seen from the nave) is always
        // a ceiling, whatever order the cells come in.
        for(BlockPos cell:open){
            if(skip.test(cell))continue;
            if(boards){var at=b.offset(cell).below();if(BuildBlocks.state(l,at).is(Blocks.SPRUCE_PLANKS))set(l,at,floor(open,cell));}
            // The ceiling over the cell: the first solid block above it, past a hanging lamp and its chain (the vaulted hall's necks
            // are roofed low, in the wall's own terracotta).
            for(int y=1;y<=roof+1;y++){var at=b.offset(cell).above(y);var s=BuildBlocks.state(l,at);
                if(s.isAir()||s.is(Blocks.LANTERN)||s.is(Blocks.CHAIN))continue;
                if(s.is(Blocks.WHITE_CONCRETE)||s.is(Blocks.WHITE_TERRACOTTA))set(l,at,surface("ceiling"));
                break;}
        }
        for(BlockPos cell:open){
            if(skip.test(cell))continue;
            for(Direction side:Direction.Plane.HORIZONTAL){
                BlockPos wall=cell.relative(side);if(skip.test(wall))continue;
                if(open.contains(wall)){
                    // Over an open neighbour, only a face this cell looks at: the vaulted hall's end walls, standing over its low necks.
                    for(int y=1;y<=roof;y++){var here=BuildBlocks.state(l,b.offset(cell).above(y));
                        if(!(here.isAir()||here.is(Blocks.LANTERN)||here.is(Blocks.CHAIN)))continue;
                        var at=b.offset(wall).above(y);if(BuildBlocks.state(l,at).is(Blocks.WHITE_TERRACOTTA))set(l,at,plaster(wall.getX(),y,wall.getZ(),y==roof));}
                    continue;
                }
                for(int y=0;y<=roof;y++){
                    var at=b.offset(wall).above(y);var s=BuildBlocks.state(l,at);
                    if(y==0){if(s.is(Blocks.STRIPPED_SPRUCE_WOOD)||s.is(Blocks.STRIPPED_OAK_WOOD)||s.is(Blocks.SPRUCE_PLANKS))set(l,at,surface("skirting"));continue;}
                    if(y==1&&s.is(Blocks.GRAY_STAINED_GLASS)&&BuildBlocks.state(l,at.above()).is(Blocks.GRAY_STAINED_GLASS)){
                        set(l,at,surface("window_lower"));set(l,at.above(),surface("window_upper"));y++;continue;
                    }
                    if(s.is(Blocks.WHITE_TERRACOTTA))set(l,at,y==rail?surface("rail"):plaster(wall.getX(),y,wall.getZ(),y==roof));
                }
            }
        }
    }
    /** Mostly plain plaster; now and then a hairline crack, and under the ceiling now and then a water stain. */
    static BlockState plaster(int x,int y,int z,boolean underCeiling){
        int h=Math.floorMod(x*734287+y*912271+z*438289,97);
        if(underCeiling&&h%13==5)return surface("plaster_stained");
        if(h==3||h==51)return surface("plaster_cracked");
        return surface("plaster");
    }
    /** Boards run the length of the passage; down the middle of a passage three, five or seven wide, they are worn pale. */
    static BlockState floor(Set<BlockPos> open,BlockPos cell){
        int w=run(open,cell,Direction.WEST),e=run(open,cell,Direction.EAST),n=run(open,cell,Direction.NORTH),s=run(open,cell,Direction.SOUTH);
        int across=w+e+1,along=n+s+1;boolean eastWest=across>along;
        boolean worn=eastWest?(along==3||along==5||along==7)&&n==s:(across==3||across==5||across==7)&&w==e;
        return surface((worn?"floor_worn":"floor")+(eastWest?"_x":""));
    }
    private static int run(Set<BlockPos> open,BlockPos from,Direction way){int n=0;var at=from.relative(way);while(open.contains(at)&&n<64){n++;at=at.relative(way);}return n;}
    private static BlockState surface(String name){return HallRegistry.surface(name).defaultBlockState();}
    private static void set(ServerLevel l,BlockPos at,BlockState state){BuildBlocks.set(l,at,state,LabyrinthBuilder.flags());}
}
