package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

/** Familiar hallways come first. Corners and side doors make choices without folding space. */
public final class LabyrinthHalls {
    public static final BlockPos QUIET_CACHE = new BlockPos(4, 0, -11);
    private LabyrinthHalls() {}
    public static boolean isHall(LabyrinthPlace place) {
        return place == LabyrinthPlace.STRAIGHT_HALL || place == LabyrinthPlace.BENT_HALL
                || place == LabyrinthPlace.CROSS_HALL || place == LabyrinthPlace.QUIET_ROOM;
    }
    public static Set<BlockPos> floor(LabyrinthPlace place) {
        Set<BlockPos> floor = new HashSet<>();
        switch (place) {
            case STRAIGHT_HALL -> {
                rectangle(floor,-1,1,-35,0);
                rectangle(floor,-2,2,-5,-1);
                rectangle(floor,-2,2,-23,-20);
            }
            case BENT_HALL -> {
                rectangle(floor, -1, 1, -20, 0);
                rectangle(floor, -16, 1, -20, -18);
                rectangle(floor, -16, -14, -32, -18);
                rectangle(floor, -16, -12, -22, -18);
            }
            case CROSS_HALL -> {
                rectangle(floor, -1, 1, -31, 0);
                rectangle(floor, -14, 14, -16, -14);
                rectangle(floor, -3, 3, -18, -12);
            }
            case QUIET_ROOM -> {
                rectangle(floor, -1, 1, -5, 0);
                rectangle(floor, -5, 5, -14, -5);
            }
            default -> {}
        }
        for (var fragment : LabyrinthDomestic.fragments(place)) {
            rectangle(floor, fragment.x0(), fragment.x1(), fragment.z0(), fragment.z1());
            int cx = (fragment.x0() + fragment.x1()) / 2, cz = (fragment.z0() + fragment.z1()) / 2;
            for (int n = 0; n < 2; n++) floor.add(switch (fragment.entrance()) {
                case EAST -> new BlockPos(fragment.x1() + 1, 0, cz + n);
                case WEST -> new BlockPos(fragment.x0() - 1, 0, cz + n);
                case NORTH -> new BlockPos(cx + n, 0, fragment.z0() - 1);
                default -> new BlockPos(cx + n, 0, fragment.z1() + 1);
            });
        }
        return Set.copyOf(floor);
    }
    private static void rectangle(Set<BlockPos> floor, int x0, int x1, int z0, int z1) {
        for (int x=x0;x<=x1;x++) for(int z=z0;z<=z1;z++) floor.add(new BlockPos(x,0,z));
    }
    public static void build(ServerLevel level, BlockPos base, LabyrinthPlace place) {
        Set<BlockPos> floor = floor(place);
        var box=place.room();
        int flags=LabyrinthBuilder.flags(), height=height(place);
        for(int x=box.minX();x<=box.maxX();x++) for(int z=box.minZ();z<=box.maxZ();z++)
            for(int y=-1;y<=height+1;y++) {
                boolean open=floor.contains(new BlockPos(x,0,z));
                var state=!open ? (y==0 ? Blocks.OAK_PLANKS.defaultBlockState() : LabyrinthDomestic.WALL)
                        : y==-1 ? floorState(place)
                        : y==height+1 ? Blocks.WHITE_CONCRETE.defaultBlockState() : Blocks.AIR.defaultBlockState();
                level.setBlock(base.offset(x,y,z),state,flags);
            }
        for(int z=-5;z>box.minZ()+3;z-=10) if(floor.contains(new BlockPos(0,0,z)))
            LabyrinthBuilder.hangLantern(level,base.offset(0,height,z),false);
        // No masonry monuments announce the first ordinary corridor.
        if(place==LabyrinthPlace.BENT_HALL) LabyrinthBuilder.hangLantern(level,base.offset(-12,3,-19),false);
        if(place==LabyrinthPlace.CROSS_HALL) {
            LabyrinthBuilder.hangLantern(level,base.offset(-9,3,-15),false);
            LabyrinthBuilder.hangLantern(level,base.offset(9,3,-15),false);
        }
        if(place==LabyrinthPlace.BENT_HALL) NavigationAids.explorerMark(level,base.offset(-9,0,-19),Direction.EAST);
        if(place==LabyrinthPlace.CROSS_HALL) NavigationAids.explorerMark(level,base.offset(-10,0,-15),Direction.EAST);
        if(place==LabyrinthPlace.QUIET_ROOM) {
            for(int z=-8;z>=-11;z--) level.setBlock(base.offset(-4,0,z),
                    LabyrinthBuilder.stairs(Blocks.DARK_OAK_STAIRS,Direction.WEST),flags);
            level.setBlock(base.offset(-3,0,-12),Blocks.DARK_OAK_FENCE.defaultBlockState(),flags);
            level.setBlock(base.offset(-3,1,-12),Blocks.OAK_PRESSURE_PLATE.defaultBlockState(),flags);
            level.setBlock(base.offset(4,0,-7),Blocks.WATER_CAULDRON.defaultBlockState(),flags);
            level.setBlock(base.offset(QUIET_CACHE),Blocks.BARREL.defaultBlockState(),flags);
            if(level.getBlockEntity(base.offset(QUIET_CACHE)) instanceof Container cache) {
                cache.setItem(0,new ItemStack(Items.BREAD,2));
                cache.setItem(1,new ItemStack(Items.APPLE,2));
                cache.setItem(2,new ItemStack(Items.PAPER,3));
            }
            BlockPos note=base.offset(4,0,-13);
            level.setBlock(note,Blocks.LECTERN.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.LecternBlock.FACING,Direction.WEST)
                    .setValue(net.minecraft.world.level.block.LecternBlock.HAS_BOOK,true),flags);
            if(level.getBlockEntity(note) instanceof net.minecraft.world.level.block.entity.LecternBlockEntity lectern)
                lectern.setBook(io.github.knaitoe.theoldesthouse.house.HouseWriting.book("A place to stop","No name",
                        io.github.knaitoe.theoldesthouse.house.HouseWriting.WritingStyle.PLAIN,
                        java.util.List.of("Sit down. Eat something. The corridor will still be there when you stand.\n\n"+
                                "Tie the string at each door. Compare your marks with the chipped pillars, not with another mark.",
                                "Let the animal listen before you follow. If it waits, catch up.\n\n"+
                                "I found a cat here. It was more frightened than I was.")));
            LabyrinthBuilder.hangLantern(level,base.offset(-3,4,-7),false);
        }
        LabyrinthBuilder.entrance(level,base,LabyrinthDomestic.WALL,LabyrinthDomestic.FLOOR,LabyrinthDomestic.CEILING);
        LabyrinthDomestic.decorateHall(level,base,place);
        LabyrinthBuilder.doors(level,base,place);
        DomesticHallUpgrade.apply(level,base,place);
    }
    public static int height(LabyrinthPlace place){return place==LabyrinthPlace.CROSS_HALL||place==LabyrinthPlace.QUIET_ROOM?4:3;}
    public static net.minecraft.world.level.block.state.BlockState floorState(LabyrinthPlace place){
        return (place==LabyrinthPlace.STRAIGHT_HALL?Blocks.OAK_PLANKS:place==LabyrinthPlace.BENT_HALL?Blocks.SMOOTH_STONE:place==LabyrinthPlace.CROSS_HALL?Blocks.DARK_OAK_PLANKS:Blocks.SPRUCE_PLANKS).defaultBlockState();
    }
}
