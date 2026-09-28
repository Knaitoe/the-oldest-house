package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Public entry points for generating The Oldest House.
 *
 * The architecture lives in {@link HouseShell}, {@link HouseRoofs} and
 * {@link HouseInteriors}; the geometry they share lives in {@link HouseLayout}.
 */
public final class HouseBuilder {
    /**
     * What each room keeps in its chests and barrels (barrels double as the
     * manor's cupboards and bedside drawers). Anything outside a named room,
     * such as the landings, is treated as parlour storage.
     */
    private static final Map<String, ResourceKey<LootTable>> ROOM_LOOT = Map.ofEntries(
            Map.entry("great_room", lootTable("chests/parlour")),
            Map.entry("great_bay", lootTable("chests/parlour")),
            Map.entry("hall", lootTable("chests/parlour")),
            Map.entry("upper_hall", lootTable("chests/parlour")),
            Map.entry("stair_tower", lootTable("chests/parlour")),
            Map.entry("long_gallery", lootTable("chests/parlour")),
            Map.entry("kitchen", lootTable("chests/kitchen")),
            Map.entry("scullery", lootTable("chests/scullery")),
            Map.entry("study", lootTable("chests/study")),
            Map.entry("cellar", lootTable("chests/basement")),
            Map.entry("principal_bedroom", lootTable("chests/bedroom")),
            Map.entry("literary_bedroom", lootTable("chests/literary")),
            Map.entry("maker_loft", lootTable("chests/workshop")),
            Map.entry("box_room", lootTable("chests/attic"))
    );
    private static final ResourceKey<LootTable> DEFAULT_LOOT = lootTable("chests/parlour");

    /** Relief the foundations are allowed to absorb across the footprint. */
    public static final int MAX_SITE_RELIEF = 5;
    private static final int MAX_FOOTPRINT_WATER = 10;

    private HouseBuilder() {
    }

    // ------------------------------------------------------------------
    // Site evaluation
    // ------------------------------------------------------------------

    /**
     * True when the claimed above-ground volume holds nothing but air,
     * replaceable vegetation, flowers or saplings.
     *
     * A MOTION_BLOCKING heightmap pass rejects trees, walls and anything
     * solid first. Whatever survives it cannot be motion-blocking, so it must
     * stand on the ground, and only the lowest few courses need a block scan.
     */
    public static boolean canBuildAt(ServerLevel level, BlockPos origin) {
        for (int x = HouseLayout.CLEAR_MIN_X; x <= HouseLayout.CLEAR_MAX_X; x++) {
            for (int z = HouseLayout.CLEAR_MIN_Z; z <= HouseLayout.CLEAR_MAX_Z; z++) {
                int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING, origin.getX() + x, origin.getZ() + z);
                if (top > origin.getY()) {
                    return false;
                }
            }
        }

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = HouseLayout.CLEAR_MIN_X; x <= HouseLayout.CLEAR_MAX_X; x++) {
            for (int z = HouseLayout.CLEAR_MIN_Z; z <= HouseLayout.CLEAR_MAX_Z; z++) {
                for (int y = 0; y <= 5; y++) {
                    BlockState state = level.getBlockState(pos.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z));
                    if (!isClearable(state)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static boolean isClearable(BlockState state) {
        return state.isAir()
                || state.canBeReplaced()
                || state.is(BlockTags.SMALL_FLOWERS)
                || state.is(BlockTags.TALL_FLOWERS)
                || state.is(BlockTags.SAPLINGS);
    }

    /**
     * Lower is better; {@link Integer#MAX_VALUE} rejects the site.
     *
     * @param relief height range already measured across the footprint
     */
    public static int siteScore(ServerLevel level, BlockPos origin, int relief) {
        int footprintWater = 0;
        for (int x = 0; x <= 27; x++) {
            for (int z = 0; z <= 26; z++) {
                int worldX = origin.getX() + x;
                int worldZ = origin.getZ() + z;
                if (surfaceIsWater(level, worldX, worldZ, surfaceHeight(level, worldX, worldZ))) {
                    footprintWater++;
                    if (footprintWater > MAX_FOOTPRINT_WATER) {
                        return Integer.MAX_VALUE;
                    }
                }
            }
        }

        SiteProfile site = SiteProfile.capture(level, origin);
        int porchDrop = Math.max(0, origin.getY() - site.frontGroundY());

        return relief * 30
                + footprintWater * 60
                + Math.min(8, porchDrop) * 5
                + (site.frontWater() ? 12 : 0);
    }

    // ------------------------------------------------------------------
    // Construction
    // ------------------------------------------------------------------

    /**
     * Builds the complete house structure and furnishings. Container contents
     * (loot, books) are not placed here: they belong to the authoritative House
     * dimension and are added by {@link #applyInteriorContents} once it exists.
     */
    public static void build(ServerLevel level, BlockPos origin) {
        SiteProfile site = SiteProfile.capture(level, origin);
        HouseCanvas canvas = new HouseCanvas(level, origin);

        HouseShell.clearSite(canvas);
        HouseShell.buildStructure(canvas);
        HouseShell.buildChimneys(canvas);
        HouseShell.buildFacade(canvas);
        HouseShell.glaze(canvas);
        HouseRoofs.build(canvas, HouseLayout.ROOFS);
        HouseShell.buildCeilings(canvas);
        HouseShell.roofAccents(canvas);
        HouseShell.trimWindows(canvas);
        HouseShell.buildPorchAndYard(canvas, site);
        HouseInteriors.furnish(canvas);
        HouseShell.buildFoundations(canvas);
        canvas.refreshShapes();

        spawnDomesticPaintings(level, origin);
    }

    /**
     * The first impossible stage: an ordinary-looking door appears, closed,
     * in the wall at the far end of the hall.
     */
    public static void revealImpossibleDoor(ServerLevel level, BlockPos origin) {
        HouseCanvas canvas = new HouseCanvas(level, origin);
        int z = HouseLayout.THRESHOLD_Z;

        canvas.fill(HouseLayout.HALL_MIN_X, 1, z, HouseLayout.HALL_MAX_X, 5, z, HouseShell.PLASTER);

        BlockState door = Blocks.SPRUCE_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, Direction.NORTH)
                .setValue(DoorBlock.HINGE, DoorHingeSide.LEFT)
                .setValue(DoorBlock.OPEN, false);
        canvas.set(HouseLayout.AXIS_X, 1, z, door.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
        canvas.set(HouseLayout.AXIS_X, 2, z, door.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));

        // Nothing may block the approach to the new door.
        for (int x = HouseLayout.HALL_MIN_X; x <= HouseLayout.HALL_MAX_X; x++) {
            for (int y = 1; y <= 3; y++) {
                BlockPos pos = canvas.pos(x, y, z - 1);
                BlockState state = level.getBlockState(pos);
                if (!state.isAir() && state.getCollisionShape(level, pos).max(Direction.Axis.Y) > 0.1D) {
                    canvas.clear(x, y, z - 1);
                }
            }
        }
    }

    /**
     * Adds container contents to the authoritative House dimension: authored
     * loot tables, books on lecterns and a scatter of books on the chiseled
     * shelves. The Overworld shell never holds contents, so nothing in it can
     * be looted twice.
     */
    public static void applyInteriorContents(ServerLevel level, BlockPos origin) {
        for (BlockEntity blockEntity : blockEntitiesInEnvelope(level, origin)) {
            BlockPos pos = blockEntity.getBlockPos();
            if (blockEntity instanceof RandomizableContainerBlockEntity container
                    && container.getLootTable() == null
                    && container.isEmpty()) {
                // Filled from its table when first opened, like any chest in the world.
                ResourceKey<LootTable> table = lootFor(pos.subtract(origin));
                container.setLootTable(table, level.getSeed() ^ pos.asLong() ^ table.location().hashCode());
                container.setChanged();
            } else if (blockEntity instanceof LecternBlockEntity lectern && !lectern.hasBook()) {
                LecternBlock.tryPlaceBook(null, level, pos, level.getBlockState(pos), new ItemStack(Items.WRITABLE_BOOK));
            } else if (blockEntity instanceof ChiseledBookShelfBlockEntity shelf && shelf.isEmpty()) {
                long hash = pos.asLong() * 0x9E3779B97F4A7C15L;
                for (int slot = 0; slot < 6; slot++) {
                    if (((hash >>> (slot * 5)) & 3L) != 0L) {
                        shelf.setItem(slot, new ItemStack(Items.BOOK));
                    }
                }
            }
        }
    }

    public static void spawnDomesticPaintings(ServerLevel level, BlockPos origin) {
        HouseInteriors.spawnPaintings(level, origin);
    }

    private static List<BlockEntity> blockEntitiesInEnvelope(ServerLevel level, BlockPos origin) {
        int minX = origin.getX() + HouseLayout.MIN_X;
        int maxX = origin.getX() + HouseLayout.MAX_X;
        int minZ = origin.getZ() + HouseLayout.CLEAR_MIN_Z;
        int maxZ = origin.getZ() + HouseLayout.MAX_Z;
        int minY = origin.getY() + HouseLayout.MIN_Y;
        int maxY = origin.getY() + HouseLayout.MAX_Y;

        List<BlockEntity> result = new ArrayList<>();
        for (int cx = minX >> 4; cx <= maxX >> 4; cx++) {
            for (int cz = minZ >> 4; cz <= maxZ >> 4; cz++) {
                LevelChunk chunk = level.getChunk(cx, cz);
                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    BlockPos pos = blockEntity.getBlockPos();
                    if (pos.getX() >= minX && pos.getX() <= maxX
                            && pos.getZ() >= minZ && pos.getZ() <= maxZ
                            && pos.getY() >= minY && pos.getY() <= maxY) {
                        result.add(blockEntity);
                    }
                }
            }
        }
        return result;
    }

    // ------------------------------------------------------------------
    // Terrain sampling
    // ------------------------------------------------------------------

    static int surfaceHeight(ServerLevel level, int x, int z) {
        return level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
    }

    static boolean surfaceIsWater(ServerLevel level, int x, int z, int surfaceHeight) {
        return level.getFluidState(new BlockPos(x, surfaceHeight - 1, z)).is(FluidTags.WATER);
    }

    private static ResourceKey<LootTable> lootTable(String path) {
        return ResourceKey.create(
                Registries.LOOT_TABLE,
                ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, path)
        );
    }

    private static ResourceKey<LootTable> lootFor(BlockPos rel) {
        for (HouseLayout.Room room : HouseLayout.ROOMS) {
            if (room.box().contains(rel.getX(), rel.getY(), rel.getZ())) {
                return ROOM_LOOT.getOrDefault(room.name(), DEFAULT_LOOT);
            }
        }
        return DEFAULT_LOOT;
    }

    /** Terrain in front of the entrance, sampled before anything is cleared. */
    record SiteProfile(int frontGroundY, boolean frontWater) {
        static SiteProfile capture(ServerLevel level, BlockPos origin) {
            int[] heights = new int[12];
            int index = 0;
            int waterSamples = 0;

            for (int z = -2; z >= -5; z--) {
                for (int x = HouseLayout.HALL_MIN_X; x <= HouseLayout.HALL_MAX_X; x++) {
                    int worldX = origin.getX() + x;
                    int worldZ = origin.getZ() + z;
                    int height = surfaceHeight(level, worldX, worldZ);

                    heights[index++] = height;
                    if (surfaceIsWater(level, worldX, worldZ, height)) {
                        waterSamples++;
                    }
                }
            }

            Arrays.sort(heights);
            return new SiteProfile(heights[heights.length / 2], waterSamples >= 4);
        }
    }
}
