package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseBlocks;
import io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock;
import io.github.knaitoe.theoldesthouse.house.SceneDetailBlock;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Furnishes rooms that were left bare: a rug on the open floor away from the
 * walls, furniture with small objects on it set against straight runs of wall,
 * pictures and clocks between, cobwebs in high corners of the older places, and
 * weathering in the labyrinth's stone. Everything stands on a real surface or
 * hangs on a real wall, nothing narrows a passage below two blocks, and story
 * positions and doorways are left exactly as they are.
 */
final class SceneDressing {
    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    private static final HouseholdFurnitureBlock.Kind DRAWERS = HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,
            BEDSIDE = HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE, DESK = HouseholdFurnitureBlock.Kind.WALNUT_DESK,
            FORMICA = HouseholdFurnitureBlock.Kind.FORMICA_TABLE, CANE = HouseholdFurnitureBlock.Kind.CANE_CHAIR,
            GREEN = HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR, FLORAL = HouseholdFurnitureBlock.Kind.FLORAL_ARMCHAIR,
            STOOL = HouseholdFurnitureBlock.Kind.FOOTSTOOL, KITCHEN = HouseholdFurnitureBlock.Kind.KITCHEN_STOOL,
            RADIATOR = HouseholdFurnitureBlock.Kind.RADIATOR;
    private static final SceneDetailBlock.Kind BOOKS = SceneDetailBlock.Kind.BOOKS, VASE = SceneDetailBlock.Kind.VASE,
            LAMP = SceneDetailBlock.Kind.TABLE_LAMP, TEA = SceneDetailBlock.Kind.TEA_SET, TOYS = SceneDetailBlock.Kind.TOYS,
            INK = SceneDetailBlock.Kind.INK_PAPERS, BOTTLES = SceneDetailBlock.Kind.BOTTLES, TOOLS = SceneDetailBlock.Kind.TOOLS,
            FILES = SceneDetailBlock.Kind.FILE_TRAY, MEDICAL = SceneDetailBlock.Kind.MEDICAL_TRAY, TOWELS = SceneDetailBlock.Kind.TOWELS,
            CRATE = SceneDetailBlock.Kind.CRATE, FRAME = SceneDetailBlock.Kind.FRAME, CLOCK = SceneDetailBlock.Kind.CLOCK,
            COAT = SceneDetailBlock.Kind.COAT;

    record Theme(@Nullable Block rug, List<HouseholdFurnitureBlock.Kind> furniture, List<SceneDetailBlock.Kind> tops,
                 List<SceneDetailBlock.Kind> walls, boolean shelves, boolean cobwebs, boolean weathering) {
        boolean furnishes() {
            return !furniture.isEmpty();
        }
    }

    private SceneDressing() {}

    @Nullable
    static Theme theme(LabyrinthPlace place) {
        return switch (place) {
            case HILL_NURSERY -> new Theme(Blocks.LIGHT_BLUE_CARPET, List.of(DRAWERS, BEDSIDE, STOOL), List.of(TOYS, VASE, BOOKS, LAMP), List.of(FRAME, CLOCK), false, false, false);
            case MINIATURES -> new Theme(Blocks.BROWN_CARPET, List.of(DRAWERS, DESK), List.of(TOOLS, INK, BOTTLES), List.of(FRAME), true, false, false);
            case MASQUE -> new Theme(null, List.of(FLORAL, BEDSIDE), List.of(VASE, BOTTLES, TEA), List.of(), false, false, false);
            case USHER -> new Theme(Blocks.GRAY_CARPET, List.of(DRAWERS, BEDSIDE), List.of(BOOKS, BOTTLES, INK), List.of(FRAME, CLOCK), true, true, false);
            case WINCHESTER -> new Theme(Blocks.RED_CARPET, List.of(DRAWERS, CANE), List.of(VASE, BOOKS), List.of(FRAME), false, true, false);
            case CHILD_ROOM -> new Theme(Blocks.PINK_CARPET, List.of(DRAWERS, STOOL), List.of(TOYS, BOOKS), List.of(FRAME), false, false, false);
            case CRIMSON_HALL -> new Theme(null, List.of(FLORAL, BEDSIDE), List.of(VASE, TEA, BOTTLES), List.of(FRAME, CLOCK), true, true, false);
            case BLY_ROUTE -> new Theme(Blocks.GREEN_CARPET, List.of(DRAWERS, BEDSIDE, CANE), List.of(BOOKS, VASE, LAMP), List.of(FRAME, COAT), false, false, false);
            case ELK_FAN -> new Theme(Blocks.GRAY_CARPET, List.of(DRAWERS, GREEN), List.of(BOTTLES, CRATE), List.of(COAT), false, false, false);
            case CONFESSION -> new Theme(Blocks.RED_CARPET, List.of(DESK, GREEN), List.of(BOOKS, INK, LAMP), List.of(FRAME, CLOCK), true, true, false);
            case DEVILS_ROCK -> new Theme(Blocks.BLUE_CARPET, List.of(DRAWERS, FORMICA, CANE), List.of(FILES, BOOKS, LAMP), List.of(FRAME, CLOCK), false, false, false);
            case WHEEL -> new Theme(null, List.of(BEDSIDE, CANE, RADIATOR), List.of(MEDICAL, VASE, BOOKS), List.of(CLOCK), false, false, false);
            case GHOSTS_SET -> new Theme(null, List.of(FORMICA, KITCHEN), List.of(FILES, CRATE, INK), List.of(), false, false, false);
            case HOSPITAL -> new Theme(null, List.of(BEDSIDE, RADIATOR, CANE), List.of(MEDICAL, TOWELS, VASE), List.of(CLOCK), false, false, false);
            case KAREN_ROOM -> new Theme(Blocks.LIGHT_GRAY_CARPET, List.of(DRAWERS, BEDSIDE), List.of(VASE, BOOKS, LAMP), List.of(FRAME), false, false, false);
            case WHALE -> new Theme(Blocks.BROWN_CARPET, List.of(DRAWERS), List.of(BOOKS, INK, BOTTLES), List.of(FRAME, CLOCK), true, false, false);
            case FOLDED_MAZE, DEEP_MAZE, ABYSS_MAZE, GRAY_CORRIDOR -> new Theme(null, List.of(), List.of(), List.of(), false, true, true);
            case STONE_GALLERY, STONE_CROSSING, STONE_DESCENT -> new Theme(null, List.of(), List.of(), List.of(), false, true, false);
            default -> null;
        };
    }

    private static boolean wall(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isCollisionShapeFullBlock(level, pos) && state.isFaceSturdy(level, pos, Direction.UP) && !state.hasBlockEntity();
    }

    private static boolean nearDoor(ServerLevel level, BlockPos pos) {
        // Doorways and their approaches stay entirely clear, rugs included.
        for (BlockPos at : BlockPos.betweenClosed(pos.offset(-4, -1, -4), pos.offset(4, 2, 4)))
            if (level.getBlockState(at).getBlock() instanceof DoorBlock) return true;
        return false;
    }

    /** Story objects, containers, readable things and anything a mechanism listens to or is worked by. */
    private static boolean interactive(BlockState state) {
        Block block = state.getBlock();
        String namespace = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).getNamespace();
        return state.hasBlockEntity() || block instanceof DoorBlock || block instanceof net.minecraft.world.level.block.BedBlock
                || block instanceof net.minecraft.world.level.block.LadderBlock || block instanceof net.minecraft.world.level.block.TrapDoorBlock
                || block instanceof net.minecraft.world.level.block.BasePressurePlateBlock || block instanceof net.minecraft.world.level.block.TripWireBlock
                || block instanceof net.minecraft.world.level.block.TripWireHookBlock || block instanceof net.minecraft.world.level.block.ButtonBlock
                || block instanceof net.minecraft.world.level.block.LeverBlock || block instanceof net.minecraft.world.level.block.SculkSensorBlock
                || block instanceof net.minecraft.world.level.block.CampfireBlock || block instanceof net.minecraft.world.level.block.CauldronBlock
                || block instanceof net.minecraft.world.level.block.PowderSnowBlock
                // The House's own props (not its wall, floor and panel materials, which are whole blocks).
                || ("the_oldest_house".equals(namespace) && !state.canOcclude()
                        && block != HouseBlocks.SCENE_DETAIL.get() && block != HouseBlocks.HOUSEHOLD_FURNITURE.get());
    }

    /** Whether anything a story or mechanism needs stands within two blocks. */
    private static boolean nearInteractive(ServerLevel level, BlockPos pos) {
        for (BlockPos at : BlockPos.betweenClosed(pos.offset(-2, -2, -2), pos.offset(2, 2, 2)))
            if (interactive(level.getBlockState(at))) return true;
        return false;
    }

    /** Carpet muffles footsteps: rooms that listen for them get no rugs. */
    private static boolean listens(ServerLevel level, BlockPos base, BoundingBox r) {
        for (BlockPos at : BlockPos.betweenClosed(base.offset(r.minX(), r.minY(), r.minZ()), base.offset(r.maxX(), r.maxY(), r.maxZ()))) {
            Block block = level.getBlockState(at).getBlock();
            if (block instanceof net.minecraft.world.level.block.SculkSensorBlock || block instanceof net.minecraft.world.level.block.TripWireBlock
                    || block instanceof net.minecraft.world.level.block.BasePressurePlateBlock) return true;
        }
        return false;
    }

    private static boolean storyKept(LabyrinthPlace place, BlockPos base, BlockPos pos) {
        int x = pos.getX() - base.getX(), y = pos.getY() - base.getY(), z = pos.getZ() - base.getZ();
        if (VignetteArchitecture.storyReserved(place, x, y, z)) return true;
        if (LiteraryRooms.isLiterary(place)) {
            for (BlockPos kept : List.of(LiteraryRooms.source(place), LiteraryRooms.ending(place)))
                if (Math.abs(kept.getX() - x) <= 2 && Math.abs(kept.getZ() - z) <= 2 && Math.abs(kept.getY() - y) <= 2) return true;
        }
        return false;
    }

    /** Detail objects per walkable floor cell, the audit's measure of a bare room. */
    static double density(ServerLevel level, BlockPos base, BoundingBox r, List<BlockPos> floor) {
        if (floor.isEmpty()) return 1;
        int detail = 0;
        for (BlockPos at : BlockPos.betweenClosed(base.offset(r.minX(), r.minY(), r.minZ()), base.offset(r.maxX(), r.maxY(), r.maxZ()))) {
            BlockState state = level.getBlockState(at);
            if (!state.isAir() && level.getFluidState(at).isEmpty() && (!state.isCollisionShapeFullBlock(level, at) || state.hasBlockEntity())) detail++;
        }
        return detail / (double) floor.size();
    }

    static int apply(ServerLevel level, BlockPos base, LabyrinthPlace place, List<BlockPos> floor) {
        Theme theme = theme(place);
        if (theme == null || floor.isEmpty()) return 0;
        BoundingBox r = place.room();
        if (theme.furnishes() && density(level, base, r, floor) >= 0.10D) return 0;
        Set<BlockPos> open = new HashSet<>(floor);
        int changed = 0;
        if (theme.weathering()) changed += weather(level, base, place, floor);
        if (theme.cobwebs()) changed += cobwebs(level, base, place, floor);
        if (!theme.furnishes()) return changed;
        changed += furnish(level, base, place, theme, floor, open);
        if (theme.rug() != null && !listens(level, base, r)) changed += rugs(level, base, place, theme.rug(), floor, open);
        return changed;
    }

    private static int hash(BlockPos pos, int salt) {
        return Math.floorMod(pos.getX() * 73428767 ^ pos.getZ() * 912931 ^ pos.getY() * 19349663 ^ salt * 83492791, 1000);
    }

    // ------------------------------------------------------------------

    private static int furnish(ServerLevel level, BlockPos base, LabyrinthPlace place, Theme theme, List<BlockPos> floor, Set<BlockPos> open) {
        List<BlockPos> placed = new ArrayList<>();
        int changed = 0, index = 0;
        for (BlockPos cell : floor) {
            Direction toWall = null;
            for (Direction side : Direction.Plane.HORIZONTAL) {
                if (wall(level, cell.relative(side)) && wall(level, cell.relative(side).above())) {
                    if (toWall != null) {
                        toWall = null;
                        break;
                    }
                    toWall = side;
                }
            }
            if (toWall == null) continue;
            Direction out = toWall.getOpposite(), along = toWall.getClockWise();
            // A straight run of wall, open floor in front for at least two cells, and headroom.
            if (!open.contains(cell.relative(out)) || !open.contains(cell.relative(out, 2))) continue;
            if (!open.contains(cell.relative(along)) || !open.contains(cell.relative(along.getOpposite()))) continue;
            if (!wall(level, cell.relative(along).relative(toWall)) || !wall(level, cell.relative(along.getOpposite()).relative(toWall))) continue;
            if (!level.getBlockState(cell).isAir() || !level.getBlockState(cell.above()).isAir() || !level.getBlockState(cell.above(2)).isAir()) continue;
            if (nearDoor(level, cell) || storyKept(place, base, cell) || nearInteractive(level, cell)) continue;
            boolean crowded = false;
            for (BlockPos other : placed) if (other.distManhattan(cell) < 4) crowded = true;
            if (crowded) continue;
            int h = hash(cell, place.ordinal());
            if (h >= 450) continue; // leave much of the wall plain
            placed.add(cell);
            index++;
            if (!theme.walls().isEmpty() && index % 3 == 0) {
                SceneDetailBlock.Kind kind = theme.walls().get(h % theme.walls().size());
                level.setBlock(cell.above(), detail(kind, out), FLAGS);
                changed++;
                continue;
            }
            if (theme.shelves() && index % 4 == 1) {
                level.setBlock(cell, Blocks.BOOKSHELF.defaultBlockState(), FLAGS);
                level.setBlock(cell.above(), Blocks.BOOKSHELF.defaultBlockState(), FLAGS);
                changed += 2;
                continue;
            }
            HouseholdFurnitureBlock.Kind kind = theme.furniture().get(h % theme.furniture().size());
            level.setBlock(cell, HouseBlocks.HOUSEHOLD_FURNITURE.get().defaultBlockState()
                    .setValue(HouseholdFurnitureBlock.KIND, kind).setValue(HouseholdFurnitureBlock.FACING, out), FLAGS);
            changed++;
            if (!kind.seat && kind != RADIATOR && !theme.tops().isEmpty()) {
                BlockState top = detail(theme.tops().get((h / 7) % theme.tops().size()), out);
                if (SceneDetailBlock.supported(level, cell.above(), top)) {
                    level.setBlock(cell.above(), top, FLAGS);
                    changed++;
                }
            }
        }
        return changed;
    }

    private static BlockState detail(SceneDetailBlock.Kind kind, Direction facing) {
        return HouseBlocks.SCENE_DETAIL.get().defaultBlockState().setValue(SceneDetailBlock.KIND, kind).setValue(SceneDetailBlock.FACING, facing);
    }

    /** Rugs cover open floor at least two cells in from any wall or furniture. */
    private static int rugs(ServerLevel level, BlockPos base, LabyrinthPlace place, Block rug, List<BlockPos> floor, Set<BlockPos> open) {
        int changed = 0;
        for (BlockPos cell : floor) {
            boolean inner = true;
            for (int dx = -2; dx <= 2 && inner; dx++) for (int dz = -2; dz <= 2 && inner; dz++) {
                BlockPos near = cell.offset(dx, 0, dz);
                inner = open.contains(near) && level.getBlockState(near).isAir();
            }
            if (!inner || storyKept(place, base, cell) || nearDoor(level, cell) || nearInteractive(level, cell)) continue;
            if (!level.getBlockState(cell.below()).isFaceSturdy(level, cell.below(), Direction.UP)) continue;
            level.setBlock(cell, rug.defaultBlockState(), FLAGS);
            changed++;
        }
        return changed;
    }

    /** Cobwebs gather in the top corners, out of the way of anyone walking. */
    private static int cobwebs(ServerLevel level, BlockPos base, LabyrinthPlace place, List<BlockPos> floor) {
        int changed = 0;
        for (BlockPos cell : floor) {
            BlockPos top = cell;
            while (top.getY() - cell.getY() < 8 && level.getBlockState(top.above()).isAir()) top = top.above();
            if (top.getY() - cell.getY() < 2 || !wall(level, top.above())) continue;
            int walls = 0;
            Direction first = null;
            for (Direction side : Direction.Plane.HORIZONTAL)
                if (wall(level, top.relative(side))) {
                    if (first != null && side.getAxis() != first.getAxis()) walls = 2;
                    if (first == null) first = side;
                }
            if (walls < 2 || hash(top, 17 + place.ordinal()) >= 400 || storyKept(place, base, top) || nearDoor(level, top) || nearInteractive(level, top)) continue;
            level.setBlock(top, Blocks.COBWEB.defaultBlockState(), FLAGS);
            changed++;
        }
        return changed;
    }

    /** Old stone walls are not one flat colour: a few weathered blocks in the runs that face the paths. */
    private static int weather(ServerLevel level, BlockPos base, LabyrinthPlace place, List<BlockPos> floor) {
        int changed = 0;
        Set<BlockPos> seen = new HashSet<>();
        for (BlockPos cell : floor)
            for (Direction side : Direction.Plane.HORIZONTAL)
                for (int y = 0; y <= 2; y++) {
                    BlockPos at = cell.relative(side).above(y);
                    if (!seen.add(at) || !level.getBlockState(at).is(Blocks.STONE) || storyKept(place, base, at)) continue;
                    int h = hash(at, 41);
                    BlockState worn = h < 50 ? Blocks.COBBLESTONE.defaultBlockState() : h < 80 ? Blocks.ANDESITE.defaultBlockState()
                            : h < 95 ? Blocks.MOSSY_COBBLESTONE.defaultBlockState() : null;
                    if (worn == null) continue;
                    level.setBlock(at, worn, FLAGS);
                    changed++;
                }
        return changed;
    }

    // ------------------------------------------------------------------
    // Outdoor ground that is only a flat colour gets what grows on it.

    static int groundCover(ServerLevel level, BlockPos base, LabyrinthPlace place) {
        BoundingBox r = place.room();
        List<BlockPos> tops = new ArrayList<>();
        int covered = 0;
        for (int x = r.minX(); x <= r.maxX(); x++)
            for (int z = r.minZ(); z <= r.maxZ(); z++)
                for (int y = Math.min(r.maxY(), 12); y >= Math.max(r.minY(), -12); y--) {
                    BlockPos at = base.offset(x, y, z);
                    BlockState state = level.getBlockState(at);
                    if (state.isAir()) continue;
                    if (!state.getFluidState().isEmpty()) break;
                    if (state.canBeReplaced() || !state.isCollisionShapeFullBlock(level, at)) {
                        covered++;
                        break;
                    }
                    if (level.getBlockState(at.above()).isAir()) tops.add(at.immutable());
                    break;
                }
        if (tops.isEmpty() || covered / (double) tops.size() >= 0.06D) return 0;
        int changed = 0;
        for (BlockPos ground : tops) {
            BlockPos above = ground.above();
            if (storyKept(place, base, above) || nearDoor(level, above) || nearInteractive(level, above)) continue;
            BlockState top = level.getBlockState(ground);
            int h = hash(ground, 101 + place.ordinal());
            BlockState plant = null;
            if (top.is(Blocks.GRASS_BLOCK)) plant = h < 160 ? Blocks.SHORT_GRASS.defaultBlockState() : h < 200 ? Blocks.FERN.defaultBlockState()
                    : h < 206 ? Blocks.DANDELION.defaultBlockState() : h < 210 ? Blocks.AZURE_BLUET.defaultBlockState() : null;
            else if (top.is(Blocks.PODZOL) || top.is(Blocks.COARSE_DIRT)) plant = h < 90 ? Blocks.FERN.defaultBlockState()
                    : h < 105 ? Blocks.BROWN_MUSHROOM.defaultBlockState() : h < 112 ? Blocks.DEAD_BUSH.defaultBlockState() : null;
            else if (top.is(Blocks.SNOW_BLOCK)) plant = h < 140 ? Blocks.SNOW.defaultBlockState() : null;
            else if (top.is(Blocks.SAND)) plant = h < 15 ? Blocks.DEAD_BUSH.defaultBlockState() : null;
            if (h >= 995 && natural(top) && clearAround(level, ground)) {
                changed += feature(level, ground, h);
                continue;
            }
            if (plant != null && plant.canSurvive(level, above)) {
                level.setBlock(above, plant, FLAGS);
                changed++;
            }
        }
        return changed;
    }

    private static boolean natural(BlockState state) {
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.PODZOL) || state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.COARSE_DIRT);
    }

    /** A boulder or a fallen trunk needs open, natural ground around it, away from any path. */
    private static boolean clearAround(ServerLevel level, BlockPos ground) {
        for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) {
            BlockPos at = ground.offset(dx, 0, dz);
            if (!natural(level.getBlockState(at)) || !level.getBlockState(at.above()).isAir() || !level.getBlockState(at.above(2)).isAir()) return false;
        }
        return true;
    }

    private static int feature(ServerLevel level, BlockPos ground, int h) {
        int changed = 0;
        if (h % 2 == 0) {
            BlockState[] stone = {Blocks.MOSSY_COBBLESTONE.defaultBlockState(), Blocks.ANDESITE.defaultBlockState(), Blocks.STONE.defaultBlockState()};
            for (BlockPos at : List.of(ground.above(), ground.above().east(), ground.above().south(), ground.above(2))) {
                level.setBlock(at, stone[Math.floorMod(at.hashCode(), stone.length)], FLAGS);
                changed++;
            }
        } else {
            Direction along = h % 4 == 1 ? Direction.EAST : Direction.SOUTH;
            BlockState log = Blocks.SPRUCE_LOG.defaultBlockState().setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS, along.getAxis());
            for (int i = -1; i <= 1; i++) {
                level.setBlock(ground.above().relative(along, i), log, FLAGS);
                changed++;
            }
            BlockState moss = Blocks.MOSS_CARPET.defaultBlockState();
            if (level.getBlockState(ground.above(2)).isAir()) level.setBlock(ground.above(2), moss, FLAGS);
        }
        return changed;
    }
}
