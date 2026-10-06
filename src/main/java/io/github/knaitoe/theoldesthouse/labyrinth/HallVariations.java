package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

/** Small changes of width, alignment and ceiling rhythm; authored exits stay at fixed coordinates. */
public final class HallVariations {
    private HallVariations() {}
    public static boolean domestic(LabyrinthPlace p) {
        return p==LabyrinthPlace.ALCOVE_HALL||p==LabyrinthPlace.OFFSET_HALL||p==LabyrinthPlace.SERVICE_LANDING;
    }
    public static boolean added(LabyrinthPlace p) { return domestic(p)||p==LabyrinthPlace.STONE_ARCADE||p==LabyrinthPlace.STONE_BEND||p==LabyrinthPlace.STONE_LANDING; }
    private static void rectangle(Set<BlockPos> out,int x0,int x1,int z0,int z1) {
        for(int x=x0;x<=x1;x++)for(int z=z0;z<=z1;z++)out.add(new BlockPos(x,0,z));
    }
    public static Set<BlockPos> floor(LabyrinthPlace p) {
        Set<BlockPos> out=new HashSet<>();
        switch(p) {
            case ALCOVE_HALL -> {
                rectangle(out,-1,1,-41,0); rectangle(out,-4,4,-21,-13);
                rectangle(out,-7,-3,-18,-16); rectangle(out,-3,3,-33,-29);
            }
            case OFFSET_HALL -> {
                rectangle(out,-1,1,-17,0); rectangle(out,-1,5,-18,-14);
                rectangle(out,3,5,-39,-16); rectangle(out,3,8,-28,-23);
            }
            case SERVICE_LANDING -> {
                rectangle(out,-1,1,-30,0); rectangle(out,-8,8,-22,-10);
                rectangle(out,-10,-7,-17,-15);
            }
            case STONE_ARCADE -> {
                rectangle(out,-2,2,-39,0);
                for(int z=-10;z>=-34;z-=8)rectangle(out,-4,4,z-2,z+2);
            }
            case STONE_BEND -> {
                rectangle(out,-2,2,-20,0); rectangle(out,-11,2,-21,-17);
                rectangle(out,-11,-7,-35,-18);
            }
            case STONE_LANDING -> {
                rectangle(out,-2,2,-27,0); rectangle(out,-10,10,-23,-8);
                rectangle(out,9,11,-18,-14);
            }
            default -> throw new IllegalArgumentException(p.id());
        }
        return Set.copyOf(out);
    }
    private static void set(ServerLevel l,BlockPos at,BlockState state) { BuildBlocks.set(l,at,state,LabyrinthBuilder.flags()); }
    private static BlockState slab(boolean stone) {
        return (stone?Blocks.STONE_BRICK_SLAB:Blocks.SPRUCE_SLAB).defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP);
    }
    public static void buildDomestic(ServerLevel l,BlockPos b,LabyrinthPlace p) {
        var open=floor(p);var box=p.room();int roof=4;
        for(int x=box.minX();x<=box.maxX();x++)for(int z=box.minZ();z<=box.maxZ();z++)for(int y=-1;y<=roof+1;y++) {
            boolean inside=open.contains(new BlockPos(x,0,z));
            BlockState s=!inside?(y==0?Blocks.SPRUCE_PLANKS.defaultBlockState():LabyrinthDomestic.WALL)
                    :y==-1?Blocks.SPRUCE_PLANKS.defaultBlockState():y==roof+1?Blocks.WHITE_CONCRETE.defaultBlockState():Blocks.AIR.defaultBlockState();
            set(l,b.offset(x,y,z),s);
        }
        finish(l,b,p);
        LabyrinthBuilder.entrance(l,b,LabyrinthDomestic.WALL,LabyrinthDomestic.FLOOR,LabyrinthDomestic.CEILING);
        LabyrinthBuilder.doors(l,b,p);
    }
    public static void buildStone(ServerLevel l,BlockPos b,LabyrinthPlace p) {
        var open=floor(p);var box=p.room();int roof=p==LabyrinthPlace.STONE_BEND?5:6;
        for(int x=box.minX();x<=box.maxX();x++)for(int z=box.minZ();z<=box.maxZ();z++)for(int y=-1;y<=roof+1;y++) {
            boolean inside=open.contains(new BlockPos(x,0,z));
            BlockState s=!inside||y==roof+1?Blocks.STONE_BRICKS.defaultBlockState()
                    :y==-1?(Math.floorMod(x+z,7)==0?Blocks.POLISHED_DIORITE:Blocks.POLISHED_ANDESITE).defaultBlockState():Blocks.AIR.defaultBlockState();
            set(l,b.offset(x,y,z),s);
        }
        finish(l,b,p);
    }
    public static void finish(ServerLevel l,BlockPos b,LabyrinthPlace p) {
        boolean stone=!domestic(p);int roof=stone?(p==LabyrinthPlace.STONE_BEND?5:6):4;
        var open=floor(p);
        // Ceiling ribs and a few recessed bays vary the silhouette without reducing head clearance.
        for(BlockPos cell:open) {
            if(nearDoor(p,cell))continue;
            if(Math.floorMod(cell.getZ(),8)==3) {
                var at=b.offset(cell).above(roof);
                if(BuildBlocks.state(l,at).isAir())set(l,at,slab(stone));
            }
            for(Direction side:Direction.Plane.HORIZONTAL) {
                BlockPos wall=cell.relative(side);
                if(open.contains(wall)||nearDoor(p,wall))continue;
                var at=b.offset(wall);
                if(l.getBlockEntity(at)!=null)continue;
                set(l,at,(stone?Blocks.CHISELED_STONE_BRICKS:Blocks.STRIPPED_SPRUCE_WOOD).defaultBlockState());
                if(!stone&&Math.floorMod(wall.getZ(),11)==0) {
                    // Glass has a real, dark back, rather than opening onto uncarved space.
                    set(l,at.above(),Blocks.GRAY_STAINED_GLASS.defaultBlockState());
                    set(l,at.above(2),Blocks.GRAY_STAINED_GLASS.defaultBlockState());
                    set(l,at.relative(side).above(),Blocks.DEEPSLATE.defaultBlockState());
                    set(l,at.relative(side).above(2),Blocks.DEEPSLATE.defaultBlockState());
                }
            }
        }
        for(int z=-6;z>p.room().minZ()+4;z-=10) {
            int x=p==LabyrinthPlace.OFFSET_HALL&&z<-18?4:p==LabyrinthPlace.STONE_BEND&&z<-21?-9:0;
            if(open.contains(new BlockPos(x,0,z)))LabyrinthBuilder.hangLantern(l,b.offset(x,roof,z),false);
        }
        if(p==LabyrinthPlace.SERVICE_LANDING||p==LabyrinthPlace.STONE_LANDING) {
            // Seating is supported at the side of a broad landing, with no new supplies.
            int x=p==LabyrinthPlace.SERVICE_LANDING?7:-9;
            for(int z=-12;z>=-15;z--)set(l,b.offset(x,0,z),LabyrinthBuilder.stairs(stone?Blocks.STONE_BRICK_STAIRS:Blocks.SPRUCE_STAIRS,Direction.WEST));
        }
    }
    private static boolean nearDoor(LabyrinthPlace p,BlockPos cell) {
        for(var door:p.doors())if(Math.abs(cell.getX()-door.rel().getX())+Math.abs(cell.getZ()-door.rel().getZ())<4)return true;
        return false;
    }
}
