package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

/** Ordinary household pieces, cut short by the plaster around them. No story or reward flags. */
public final class LabyrinthDomestic {
    public static final BlockState WALL = Blocks.WHITE_TERRACOTTA.defaultBlockState();
    public static final BlockState FLOOR = Blocks.SPRUCE_PLANKS.defaultBlockState();
    public static final BlockState CEILING = Blocks.BIRCH_PLANKS.defaultBlockState();
    public enum Room { KITCHEN, LAUNDRY, BEDROOM, DINING }
    public record Fragment(Room room, int x0, int x1, int z0, int z1, Direction entrance) {}
    private LabyrinthDomestic() {}

    public static List<Fragment> fragments(LabyrinthPlace place) {
        return switch (place) {
            case STRAIGHT_HALL -> List.of(
                    new Fragment(Room.KITCHEN, -6, -3, -20, -17, Direction.EAST),
                    new Fragment(Room.LAUNDRY, 3, 6, -30, -27, Direction.WEST));
            case BENT_HALL -> List.of(
                    new Fragment(Room.DINING, 3, 6, -14, -11, Direction.WEST),
                    new Fragment(Room.BEDROOM, -12, -9, -29, -25, Direction.WEST));
            case CROSS_HALL -> List.of(
                    new Fragment(Room.KITCHEN, -10, -7, -12, -9, Direction.NORTH),
                    new Fragment(Room.DINING, 6, 9, -21, -18, Direction.SOUTH));
            default -> List.of();
        };
    }

    /** The route is kept at its old coordinates, including every registered door and cache. */
    public static void decorateHall(ServerLevel level, BlockPos base, LabyrinthPlace place) {
        var floor = LabyrinthHalls.floor(place);
        int height = place == LabyrinthPlace.QUIET_ROOM ? 4 : 3;
        for (BlockPos relative : floor) {
            replace(level, base.offset(relative).below(), Blocks.SMOOTH_STONE, FLOOR);
            replace(level, base.offset(relative).above(height + 1), Blocks.STONE, CEILING);
            for (Direction side : Direction.Plane.HORIZONTAL) {
                BlockPos wall = relative.relative(side);
                if (floor.contains(wall)) continue;
                for (int y = 0; y <= height; y++) replace(level, base.offset(wall).above(y),
                        Blocks.LIGHT_GRAY_TERRACOTTA, y == 0 ? Blocks.OAK_PLANKS.defaultBlockState() : WALL);
            }
        }
        warmVestibule(level, base);
        for (Fragment fragment : fragments(place)) buildFragment(level, base, fragment);
        if (place == LabyrinthPlace.QUIET_ROOM) {
            // The window belonged to a sitting room; there is only plaster behind it now.
            window(level, base.offset(-6, 1, -10), Direction.EAST, 3);
            for (int z = -8; z >= -11; z--)
                replace(level, base.offset(-4, 0, z), Blocks.DARK_OAK_STAIRS,
                        LabyrinthBuilder.stairs(Blocks.SPRUCE_STAIRS, Direction.WEST));
        }
    }

    public static void decorateJunction(ServerLevel level, BlockPos base) {
        for (int x = -5; x <= 5; x++) for (int z = -13; z <= 0; z++) {
            for (int y = -1; y <= 5; y++) {
                BlockPos pos = base.offset(x, y, z);
                replace(level, pos, Blocks.LIGHT_GRAY_TERRACOTTA,
                        y == 0 ? Blocks.OAK_PLANKS.defaultBlockState() : WALL);
                if (y == -1) replace(level, pos, Blocks.SMOOTH_STONE, FLOOR);
                if (y == 5) replace(level, pos, Blocks.STONE, CEILING);
            }
        }
        for (int z : new int[]{-3, -9})
            replace(level, base.offset(0, 4, z), Blocks.SOUL_LANTERN,
                    Blocks.LANTERN.defaultBlockState().setValue(net.minecraft.world.level.block.LanternBlock.HANGING, true));
        for (int z : new int[]{-9, -10}) {
            BlockPos seat = base.offset(-4, 0, z);
            if (level.getBlockState(seat).isAir() || level.getBlockState(seat).is(Blocks.STONE_STAIRS))
                set(level, seat, LabyrinthBuilder.stairs(Blocks.SPRUCE_STAIRS, Direction.WEST));
        }
        // A shoe rack, cupboard fronts and a sealed sitting-room window.
        set(level, base.offset(4, 0, -3), LabyrinthBuilder.stairs(Blocks.OAK_STAIRS, Direction.EAST));
        for (int x = -3; x <= -1; x++) set(level, base.offset(x, 0, -13), LabyrinthBuilder.barrel(Direction.SOUTH));
        window(level, base.offset(5, 1, -10), Direction.WEST, 3);
        warmVestibule(level, base);
    }

    /** In-place upgrade: ordinary caches, drops, animals, marks and placed lights are not rebuilt. */
    public static void upgrade(ServerLevel level, BlockPos origin, LabyrinthPlace place) {
        BlockPos base = LabyrinthPlaces.base(origin, place);
        if (base == null) return;
        var data = LabyrinthData.get(level.getServer());
        var state = data.state("domestic_0417");
        if (!state.contains("Origin") || state.getLong("Origin") != origin.asLong()) state = new net.minecraft.nbt.CompoundTag();
        if (state.getBoolean(place.id())) return;
        if (place == LabyrinthPlace.JUNCTION) decorateJunction(level, base);
        else if (LabyrinthHalls.isHall(place)) decorateHall(level, base, place);
        else if (LabyrinthMaze.isMaze(place)) {
            var layout = LabyrinthMaze.layout(level.getServer(), place);
            decorateMaze(level, base, place, layout);
            if (place == LabyrinthPlace.GRAY_CORRIDOR) {
                for (int variant = 0; variant < 3; variant++)
                    for (BlockPos lamp : LabyrinthMaze.lightPositions(place, 0, variant))
                        replace(level, base.offset(lamp), Blocks.SOUL_LANTERN,
                                Blocks.LANTERN.defaultBlockState().setValue(net.minecraft.world.level.block.LanternBlock.HANGING, true));
                warmVestibule(level, base);
            }
        }
        state.putLong("Origin", origin.asLong());
        state.putBoolean(place.id(), true);
        data.setState("domestic_0417", state);
    }

    private static void warmVestibule(ServerLevel level, BlockPos base) {
        var box = LabyrinthPlaces.localVestibule();
        for (int x = -2; x <= 2; x++) for (int z = 1; z <= box.maxZ(); z++) for (int y = -1; y <= 4; y++) {
            BlockPos pos = base.offset(x, y, z);
            replace(level, pos, Blocks.LIGHT_GRAY_TERRACOTTA, WALL);
            if (y == -1) replace(level, pos, Blocks.SMOOTH_STONE, FLOOR);
            if (y == 4) replace(level, pos, Blocks.STONE, CEILING);
            if (y == 3) replace(level, pos, Blocks.SOUL_LANTERN,
                    Blocks.LANTERN.defaultBlockState().setValue(net.minecraft.world.level.block.LanternBlock.HANGING, true));
        }
    }

    private static void buildFragment(ServerLevel level, BlockPos base, Fragment f) {
        BlockState wall = switch (f.room) {
            case KITCHEN -> Blocks.WHITE_TERRACOTTA.defaultBlockState();
            case LAUNDRY -> Blocks.LIGHT_BLUE_TERRACOTTA.defaultBlockState();
            case BEDROOM -> Blocks.PINK_TERRACOTTA.defaultBlockState();
            case DINING -> Blocks.YELLOW_TERRACOTTA.defaultBlockState();
        };
        for (int x = f.x0 - 1; x <= f.x1 + 1; x++) for (int z = f.z0 - 1; z <= f.z1 + 1; z++)
            for (int y = -1; y <= 4; y++) {
                boolean inside = x >= f.x0 && x <= f.x1 && z >= f.z0 && z <= f.z1;
                BlockState state = !inside ? wall : y == -1 ? fragmentFloor(f.room, x, z)
                        : y == 4 ? CEILING : Blocks.AIR.defaultBlockState();
                set(level, base.offset(x, y, z), state);
            }
        // Two-block opening; the household finish ends abruptly at the ordinary corridor.
        int cx = (f.x0 + f.x1) / 2, cz = (f.z0 + f.z1) / 2;
        for (int n = 0; n < 2; n++) {
            BlockPos throat = switch (f.entrance) {
                case EAST -> new BlockPos(f.x1 + 1, 0, cz + n);
                case WEST -> new BlockPos(f.x0 - 1, 0, cz + n);
                case NORTH -> new BlockPos(cx + n, 0, f.z0 - 1);
                default -> new BlockPos(cx + n, 0, f.z1 + 1);
            };
            for (int y = 0; y < 3; y++) set(level, base.offset(throat).above(y), Blocks.AIR.defaultBlockState());
            set(level, base.offset(throat).below(), FLOOR);
        }
        Direction into = f.entrance.getOpposite();
        BlockPos back = switch (f.entrance) {
            case EAST -> new BlockPos(f.x0, 0, cz);
            case WEST -> new BlockPos(f.x1, 0, cz);
            case NORTH -> new BlockPos(cx, 0, f.z1);
            default -> new BlockPos(cx, 0, f.z0);
        };
        furniture(level, base.offset(back), into, f.room);
        LabyrinthBuilder.hangLantern(level, base.offset(cx, 3, cz), false);
        // A glazing fragment has a solid backing, never an exterior view or an escape hole.
        window(level, base.offset(back).above().relative(into), into.getOpposite(), 2);
    }

    private static BlockState fragmentFloor(Room room, int x, int z) {
        if (room == Room.KITCHEN || room == Room.LAUNDRY)
            return ((x + z) & 1) == 0 ? Blocks.QUARTZ_BLOCK.defaultBlockState() : Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState();
        return room == Room.BEDROOM ? Blocks.BIRCH_PLANKS.defaultBlockState() : Blocks.DARK_OAK_PLANKS.defaultBlockState();
    }

    private static void furniture(ServerLevel level, BlockPos back, Direction into, Room room) {
        Direction along = into.getClockWise();
        switch (room) {
            case KITCHEN -> {
                set(level, back, Blocks.CAULDRON.defaultBlockState());
                set(level, back.relative(along), Blocks.FURNACE.defaultBlockState().setValue(FurnaceBlock.FACING, into.getOpposite()));
                set(level, back.relative(along.getOpposite()), LabyrinthBuilder.barrel(into.getOpposite()));
                set(level, back.above(2), Blocks.OAK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP));
            }
            case LAUNDRY -> {
                set(level, back, Blocks.WATER_CAULDRON.defaultBlockState());
                set(level, back.relative(along), LabyrinthBuilder.barrel(into.getOpposite()));
                set(level, back.relative(along).above(), Blocks.WHITE_WOOL.defaultBlockState());
                set(level, back.relative(along.getOpposite()), Blocks.OAK_SLAB.defaultBlockState());
            }
            case BEDROOM -> {
                LabyrinthBuilder.bed(level, back.relative(along.getOpposite()), along, Blocks.WHITE_BED);
                set(level, back.relative(along), LabyrinthBuilder.barrel(into.getOpposite()));
                set(level, back.relative(along).above(), Blocks.FLOWER_POT.defaultBlockState());
            }
            case DINING -> {
                set(level, back, Blocks.OAK_FENCE.defaultBlockState());
                set(level, back.above(), Blocks.OAK_PRESSURE_PLATE.defaultBlockState());
                set(level, back.relative(along), LabyrinthBuilder.stairs(Blocks.OAK_STAIRS, along));
                set(level, back.relative(along.getOpposite()), LabyrinthBuilder.stairs(Blocks.OAK_STAIRS, along.getOpposite()));
            }
        }
    }

    public record MazeFragment(Room room, BlockPos back, Direction into) {}

    public static List<MazeFragment> mazeFragments(LabyrinthPlace place, MazeLayout layout) {
        var ends = layout.graph().entrySet().stream().filter(e -> e.getValue().size() == 1
                        && !e.getKey().equals(layout.start()) && !e.getKey().equals(layout.goal()))
                .sorted(Comparator.comparingInt((java.util.Map.Entry<MazeLayout.Cell, java.util.Set<MazeLayout.Cell>> e) -> e.getKey().row())
                        .thenComparingInt(e -> e.getKey().x())).toList();
        var fragments = new java.util.ArrayList<MazeFragment>();
        for (var end : ends) {
            BlockPos center = layout.node(end.getKey()), neighbor = layout.node(end.getValue().iterator().next());
            Direction into = Direction.fromDelta(Integer.signum(center.getX() - neighbor.getX()), 0,
                    Integer.signum(center.getZ() - neighbor.getZ()));
            if (into == null || LabyrinthMaze.nearFold(layout, center)) continue;
            Direction along = into.getClockWise();
            BlockPos back = center.relative(into, 2);
            boolean safe = true;
            for (int side = -2; side <= 2; side++) {
                BlockPos rel = back.relative(along, side);
                if (!place.room().isInside(rel) || layout.floor().contains(rel)
                        || layout.floor().contains(rel.relative(into)) || LabyrinthMaze.nearFold(layout, rel)) safe = false;
            }
            if (!safe) continue;
            fragments.add(new MazeFragment(Room.values()[(place.slot() + fragments.size()) % Room.values().length], back, into));
            if (fragments.size() == 2) break;
        }
        return List.copyOf(fragments);
    }

    /** Furnish recesses in dead-end walls, leaving every old floor tile and fold sleeve walkable. */
    public static void decorateMaze(ServerLevel level, BlockPos base, LabyrinthPlace place, MazeLayout layout) {
        for (MazeFragment fragment : mazeFragments(place, layout)) {
            BlockPos back = fragment.back;
            Direction into = fragment.into, along = into.getClockWise();
            Room room = fragment.room;
            // If already furnished, or changed by an explorer, leave these exact recesses alone.
            boolean untouched = true;
            for (int side = -1; side <= 1; side++) {
                BlockState state = level.getBlockState(base.offset(back.relative(along, side)));
                if (!state.is(Blocks.LIGHT_GRAY_TERRACOTTA) && !state.is(Blocks.WHITE_TERRACOTTA)) untouched = false;
            }
            if (!untouched) continue;
            for (int side = -1; side <= 1; side++) {
                BlockPos pos = base.offset(back.relative(along, side));
                for (int y = 0; y < 3; y++) set(level, pos.above(y), Blocks.AIR.defaultBlockState());
                set(level, pos.below(), fragmentFloor(room, side, 0));
                set(level, pos.above(3), Blocks.OAK_PLANKS.defaultBlockState());
                for (int y = 0; y < 3; y++) set(level, pos.relative(into).above(y), WALL);
            }
            furniture(level, base.offset(back), into, room);
            BlockPos center = back.relative(into.getOpposite(), 2);
            for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++)
                replace(level, base.offset(center).offset(x, -1, z), Blocks.SMOOTH_STONE, fragmentFloor(room, x, z));
        }
    }

    private static void window(ServerLevel level, BlockPos start, Direction facing, int width) {
        Direction along = facing.getClockWise();
        for (int i = 0; i < width; i++) for (int y = 0; y < 2; y++) {
            BlockPos pos = start.relative(along, i).above(y);
            set(level, pos, Blocks.GLASS.defaultBlockState());
            set(level, pos.relative(facing.getOpposite()), Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState());
        }
    }

    private static void replace(ServerLevel level, BlockPos pos, Block from, BlockState to) {
        if (level.getBlockState(pos).is(from)) set(level, pos, to);
    }
    private static void set(ServerLevel level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state, LabyrinthBuilder.flags());
    }
}
