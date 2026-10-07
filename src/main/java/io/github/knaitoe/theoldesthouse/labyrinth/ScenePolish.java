package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.DirtPathBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;

/**
 * One finishing pass per scene (0.4.42), after its construction and dressing:
 * lanterns hang from something, wall fittings face a wall, nothing that would
 * pop off on the next block update is left standing, trees are rooted, the
 * authored foliage does not decay, creatures are not buried, outdoor scenes
 * end in woods rather than at the void, and interiors that are not dark by
 * design are lit. It only changes blocks that are defective or empty space,
 * never story volumes, doors' approaches or block entities.
 */
public final class ScenePolish {
    public static final String STATE = "scene_polish_0442";
    public static final String REPAIR_STATE = "scene_polish_repairs_0443";
    private static final Set<LabyrinthPlace> REPAIR_PLACES = EnumSet.of(LabyrinthPlace.PHONE_CANOE, LabyrinthPlace.HOTEL,
            LabyrinthPlace.MASQUE, LabyrinthPlace.END_WORLD_CABIN, LabyrinthPlace.ELK_FAN, LabyrinthPlace.HILL_NURSERY,
            LabyrinthPlace.WHALE, LabyrinthPlace.ZAMPANO_COURTYARD, LabyrinthPlace.WINCHESTER, LabyrinthPlace.BLY_ROUTE,
            LabyrinthPlace.MOTHER_DEN);
    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    /** Darkness is the point of these. */
    private static final Set<LabyrinthPlace> DARK_BY_DESIGN = EnumSet.of(LabyrinthPlace.LIGHT_SINK, LabyrinthPlace.BLIND_STRETCH,
            LabyrinthPlace.PRESERVED_CAVE,
            LabyrinthPlace.TED_CAVER, LabyrinthPlace.FLOODED_PASSAGE, LabyrinthPlace.HIDE_AND_CLAP,
            // The night vigil and the torch-marked hunt are played in the dark.
            LabyrinthPlace.GOATMAN, LabyrinthPlace.HOLLOWAY_CAMP);
    /** Floating fragments are the anomaly here. */
    private static final Set<LabyrinthPlace> FLOATING_BY_DESIGN = EnumSet.of(LabyrinthPlace.GRAVITY_DRIFT, LabyrinthPlace.FRACTURED_WALKWAY);
    /** Uneasy places stay dim: about half their floor may stay in shadow. */
    private static final Set<LabyrinthPlace> DIM = EnumSet.of(LabyrinthPlace.COMPRESSION_PASSAGE, LabyrinthPlace.FALSE_DISTANCE,
            LabyrinthPlace.MOVING_THRESHOLD, LabyrinthPlace.DUPLICATE_PASSAGE, LabyrinthPlace.GRAVITY_DRIFT, LabyrinthPlace.FRACTURED_WALKWAY,
            LabyrinthPlace.LONG_HALLWAY, LabyrinthPlace.HOTEL_HALLWAY, LabyrinthPlace.SPIRAL_STAIR, LabyrinthPlace.MOTHER_DEN,
            LabyrinthPlace.GOATMAN, LabyrinthPlace.SEANCE, LabyrinthPlace.CRIMSON_HALL, LabyrinthPlace.USHER, LabyrinthPlace.CHILD_ROOM,
            LabyrinthPlace.GHOSTS_SET, LabyrinthPlace.CONFESSION, LabyrinthPlace.GRAY_CORRIDOR, LabyrinthPlace.WHALE,
            LabyrinthPlace.STONE_GALLERY, LabyrinthPlace.STONE_CROSSING, LabyrinthPlace.STONE_DESCENT);
    /** The mazes keep most of their dark, with a lamp often enough to read the walls by. */
    private static final Set<LabyrinthPlace> SPARSE = EnumSet.of(LabyrinthPlace.FOLDED_MAZE, LabyrinthPlace.DEEP_MAZE, LabyrinthPlace.ABYSS_MAZE);
    private static final int BORDER = 12;

    private ScenePolish() {}

    public static boolean applies(LabyrinthPlace place) {
        return place.room() != null && place.slot() >= 0 && place != LabyrinthPlace.RED_ROOM
                && place != LabyrinthPlace.FAMILY_COPY && place != LabyrinthPlace.OLD_CABIN;
    }

    private static String key(BlockPos origin, LabyrinthPlace place) {
        return origin.asLong() + ":" + place.id();
    }

    /**
     * Polishes a freshly built scene and records it, so the in-place pass leaves it alone,
     * then hands a polished scene on to its composition (0.4.46).
     */
    public static void polishOnce(ServerLevel level, BlockPos origin, LabyrinthPlace place) {
        polish(level, origin, place);
        if (applies(place) && LabyrinthData.get(level.getServer()).state(STATE).getBoolean(key(origin, place)))
            SceneCraft.craftOnce(level, origin, place);
        SceneSupportRepairs.repairOnce(level,origin,place);
        SceneReview.fresh(level,origin,place);
    }

    private static void polish(ServerLevel level, BlockPos origin, LabyrinthPlace place) {
        if (!applies(place)) return;
        LabyrinthData data = LabyrinthData.get(level.getServer());
        CompoundTag done = data.state(STATE);
        CompoundTag repaired = data.state(REPAIR_STATE);
        boolean polished = done.getBoolean(key(origin, place));
        if (polished && (repaired.getBoolean(key(origin, place)) || !REPAIR_PLACES.contains(place))) return;
        BlockPos base = LabyrinthPlaces.base(origin, place);
        if (base == null) return;
        if (polished) {
            AABB area = repairArea(base, place);
            if (level.players().stream().anyMatch(p -> area.intersects(p.getBoundingBox())) || !loaded(level, area, base)) return;
            repairs(level, base, place);
        } else apply(level, base, place);
        done.putBoolean(key(origin, place), true);
        data.setState(STATE, done);
        repaired.putBoolean(key(origin, place), true);
        data.setState(REPAIR_STATE, repaired);
    }

    /** An explicit rebuild authors the room again, so it is polished (and composed) again. */
    public static void forget(MinecraftServer server, BlockPos origin, LabyrinthPlace place) {
        SceneCraft.forget(server, origin, place);
        ScenePlaytestRepairs.forget(server, origin, place);
        SceneSupportRepairs.forget(server,origin,place);
        SceneReview.forget(server,origin,place);
        LabyrinthData data = LabyrinthData.get(server);
        CompoundTag done = data.state(STATE);
        if (done.contains(key(origin, place))) {
            done.remove(key(origin, place));
            data.setState(STATE, done);
        }
        CompoundTag repaired = data.state(REPAIR_STATE);
        if (repaired.contains(key(origin, place))) {
            repaired.remove(key(origin, place));
            data.setState(REPAIR_STATE, repaired);
        }
    }

    // ------------------------------------------------------------------
    // Existing worlds: one standing scene at a time, once nobody is in it.

    public static void tick(MinecraftServer server) {
        if (server.getTickCount() % 40 != 7 || server instanceof net.minecraft.gametest.framework.GameTestServer
                || LabyrinthBuilder.isCarving()) return;
        BlockPos origin = HouseSavedData.get(server).houseOrigin();
        if (origin == null) return;
        LabyrinthData data = LabyrinthData.get(server);
        if (!origin.equals(data.builtOrigin())) return;
        CompoundTag done = data.state(STATE);
        CompoundTag repaired = data.state(REPAIR_STATE);
        for (LabyrinthPlace place : LabyrinthPlace.values()) {
            boolean polished = done.getBoolean(key(origin, place));
            if (!applies(place) || polished && (repaired.getBoolean(key(origin, place)) || !REPAIR_PLACES.contains(place)) || data.door(place.entryDoorId()) == null) continue;
            if (!LabyrinthBuilder.isPlaceReady(data, place)) continue;
            ServerLevel level = server.getLevel(NovelRooms.dimension(place));
            BlockPos base = LabyrinthPlaces.base(origin, place);
            if (level == null || base == null) continue;
            AABB area = repairArea(base, place);
            if (level.players().stream().anyMatch(p -> area.intersects(p.getBoundingBox()))) continue;
            if (!loaded(level, area, base)) return; // one scene at a time: wait for this one's chunks
            long started = System.nanoTime();
            if (polished) repairs(level, base, place); else apply(level, base, place);
            done.putBoolean(key(origin, place), true);
            data.setState(STATE, done);
            repaired.putBoolean(key(origin, place), true);
            data.setState(REPAIR_STATE, repaired);
            TheOldestHouse.LOGGER.info("Polished {} in place ({} ms).", place.id(), (System.nanoTime() - started) / 1_000_000L);
            return;
        }
    }

    private static AABB repairArea(BlockPos base, LabyrinthPlace place) {
        BoundingBox r = place.room();
        int margin = NovelRooms.outside(place) ? BORDER + 1 : 1;
        return new AABB(base.getX() + r.minX() - margin, base.getY() + r.minY() - 1, base.getZ() + r.minZ() - margin,
                base.getX() + r.maxX() + margin + 1, base.getY() + r.maxY() + 2, base.getZ() + r.maxZ() + margin + 1);
    }

    /** Already polished worlds receive targeted corrections without repeating scenery or supplies. */
    private static void repairs(ServerLevel level, BlockPos base, LabyrinthPlace place) {
        specifics(level, base, place);
        if (place == LabyrinthPlace.MOTHER_DEN) unbury(level, base, place);
    }

    /** Tickets let chunks load between ticks; a scene is never pulled in synchronously. */
    private static boolean loaded(ServerLevel level, AABB area, BlockPos ticket) {
        boolean ready = true;
        for (int x = ((int) Math.floor(area.minX)) >> 4; x <= ((int) Math.ceil(area.maxX) - 1) >> 4; x++)
            for (int z = ((int) Math.floor(area.minZ)) >> 4; z <= ((int) Math.ceil(area.maxZ) - 1) >> 4; z++) {
                ChunkPos chunk = new ChunkPos(x, z);
                level.getChunkSource().addRegionTicket(TicketType.PORTAL, chunk, 3, ticket);
                ready &= level.isLoaded(new BlockPos(x << 4, ticket.getY(), z << 4)) && level.areEntitiesLoaded(chunk.toLong());
            }
        return ready;
    }

    // ------------------------------------------------------------------

    /** Applies every rule to one scene. Returns how many blocks it changed. */
    public static int apply(ServerLevel level, BlockPos base, LabyrinthPlace place) {
        int changed = 0;
        changed += specifics(level, base, place);
        changed += foliageAndFittings(level, base, place);
        changed += lanterns(level, base, place);
        if (!FLOATING_BY_DESIGN.contains(place)) changed += floatingTrees(level, base, place);
        unbury(level, base, place);
        if (NovelRooms.outside(place)) {
            changed += SceneDressing.groundCover(level, base, place);
            changed += border(level, base, place);
        }
        else if (!DARK_BY_DESIGN.contains(place)) {
            changed += SceneDressing.apply(level, base, place, new Lighting(level, base, place).floor);
            changed += light(level, base, place, darkAllowed(place));
        }
        return changed;
    }

    // ------------------------------------------------------------------
    // Supports a few authored props were built without.

    private static boolean emptyOrWater(ServerLevel level, BlockPos at) {
        BlockState state = level.getBlockState(at);
        return state.isAir() || state.is(Blocks.WATER);
    }

    private static int support(ServerLevel level, BlockPos at, BlockState state) {
        if (!emptyOrWater(level, at)) return 0;
        level.setBlock(at, state, FLAGS);
        return 1;
    }

    private static int specifics(ServerLevel level, BlockPos base, LabyrinthPlace place) {
        int changed = 0;
        switch (place) {
            // The drowned bell tower gets the beam its bell hangs from.
            case PHONE_CANOE -> changed += support(level, base.offset(0, -1, -37), Blocks.DEEPSLATE_BRICKS.defaultBlockState());
            // The boiler room's two levers stand on a control plinth.
            case HOTEL -> {
                for (int x : new int[]{4, 6}) changed += support(level, base.offset(x, -4, -58), Blocks.POLISHED_ANDESITE.defaultBlockState());
            }
            // The last chamber's red light stands on a post.
            case MASQUE -> changed += support(level, base.offset(0, 0, -77), Blocks.POLISHED_BLACKSTONE_BRICK_WALL.defaultBlockState());
            // The cabin television stands on a cabinet.
            case END_WORLD_CABIN -> {
                BlockPos under = base.offset(7, 0, -24);
                if (level.getBlockState(under).isAir()) {
                    level.setBlock(under, io.github.knaitoe.theoldesthouse.house.HouseBlocks.HOUSEHOLD_FURNITURE.get().defaultBlockState()
                            .setValue(io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock.KIND,
                                    io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS)
                            .setValue(io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock.FACING, Direction.SOUTH), FLAGS);
                    changed++;
                }
            }
            // The ceiling fan hangs on a rod.
            case ELK_FAN -> {
                for (int y = 8; y < 12 && level.getBlockState(base.offset(0, y, -17)).isAir(); y++) {
                    level.setBlock(base.offset(0, y, -17), Blocks.CHAIN.defaultBlockState(), FLAGS);
                    changed++;
                }
            }
            // A nursery door cannot stand on powder snow: the cold spot moves just inside. And the
            // corridor's wall behind each nursery door is opened, or the nurseries are sealed.
            case HILL_NURSERY -> {
                for (int x : new int[]{-3, 3})
                    for (int y = 0; y <= 1; y++) {
                        BlockPos gap = base.offset(x, y, -16);
                        if (!level.getBlockState(gap).isAir() && !level.getBlockState(gap).hasBlockEntity()) changed += set(level, gap, Blocks.AIR.defaultBlockState());
                    }
                BlockPos sill = base.offset(-4, -1, -16), inside = base.offset(-5, -1, -16);
                if (level.getBlockState(sill).is(Blocks.POWDER_SNOW)) {
                    level.setBlock(sill, Blocks.DARK_OAK_PLANKS.defaultBlockState(), FLAGS);
                    if (level.getBlockState(inside).is(Blocks.DARK_OAK_PLANKS)) level.setBlock(inside, Blocks.POWDER_SNOW.defaultBlockState(), FLAGS);
                    changed += 2;
                }
            }
            // The attic ladder runs unbroken past each landing.
            case WHALE -> {
                for (int y : new int[]{4, 7, 10}) {
                    BlockPos rung = base.offset(0, y, -18);
                    if (level.getBlockState(rung).is(Blocks.SMOOTH_STONE))
                        changed += set(level, rung, Blocks.LADDER.defaultBlockState().setValue(net.minecraft.world.level.block.LadderBlock.FACING, Direction.SOUTH));
                }
            }
            // The cell wall's gouged block reaches the floor.
            case ZAMPANO_COURTYARD -> changed += support(level, base.offset(5, 0, -33), Blocks.DEEPSLATE_TILES.defaultBlockState());
            // The stair railings rest on a ledge along each flight instead of hanging in the air.
            case WINCHESTER -> {
                BoundingBox r = place.room();
                for (BlockPos at : BlockPos.betweenClosed(base.offset(r.minX(), r.minY(), r.minZ()), base.offset(r.maxX(), r.maxY(), r.maxZ()))) {
                    if (!level.getBlockState(at).is(Blocks.DARK_OAK_FENCE) || !level.getBlockState(at.below()).isAir()) continue;
                    BlockPos ledge = at.below();
                    boolean beside = false;
                    for (Direction side : Direction.Plane.HORIZONTAL) beside |= !level.getBlockState(ledge.relative(side)).isAir();
                    if (!beside) continue;
                    level.setBlock(ledge, Blocks.DARK_OAK_SLAB.defaultBlockState()
                            .setValue(net.minecraft.world.level.block.SlabBlock.TYPE, net.minecraft.world.level.block.state.properties.SlabType.TOP), FLAGS);
                    changed++;
                }
            }
            // The garden pool is a basin with walls, not water hanging over the space under the floor.
            case BLY_ROUTE -> {
                for (int y = -4; y <= -2; y++)
                    for (int x = -2; x <= 2; x++)
                        for (int z = -51; z <= -40; z++)
                            if (Math.abs(x) == 2 || z == -51 || z == -40) {
                                BlockPos at = base.offset(x, y, z);
                                if (level.getBlockState(at).isAir() || level.getBlockState(at).is(Blocks.WATER) && (Math.abs(x) == 2 || z == -51 || z == -40))
                                    changed += set(level, at, Blocks.STONE_BRICKS.defaultBlockState());
                            }
            }
            default -> { }
        }
        return changed;
    }

    private static int set(ServerLevel level, BlockPos at, BlockState state) {
        level.setBlock(at, state, FLAGS);
        return 1;
    }

    private static boolean reserved(LabyrinthPlace place, int x, int y, int z) {
        for (var door : place.doors())
            if (Math.abs(x - door.rel().getX()) <= 1 && Math.abs(z - door.rel().getZ()) <= 2
                    && y >= door.rel().getY() - 1 && y <= door.rel().getY() + 2) return true;
        return VignetteArchitecture.storyReserved(place, x, y, z);
    }

    private static boolean vanilla(BlockState state) {
        return "minecraft".equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace());
    }

    // ------------------------------------------------------------------
    // Fittings that cannot stay where they are, and foliage that would decay.

    private static int foliageAndFittings(ServerLevel level, BlockPos base, LabyrinthPlace place) {
        BoundingBox r = place.room();
        int changed = 0;
        for (BlockPos at : BlockPos.betweenClosed(base.offset(r.minX(), r.minY(), r.minZ()), base.offset(r.maxX(), r.maxY(), r.maxZ()))) {
            BlockState state = level.getBlockState(at);
            if (state.isAir()) continue;
            if (state.getBlock() instanceof LeavesBlock && !state.getValue(LeavesBlock.PERSISTENT)) {
                level.setBlock(at, state.setValue(LeavesBlock.PERSISTENT, true), FLAGS);
                changed++;
                continue;
            }
            if (!vanilla(state) || state.canSurvive(level, at)) continue;
            BlockPos pos = at.immutable();
            BlockState turned = reorient(level, pos, state);
            if (turned != null) {
                level.setBlock(pos, turned, FLAGS);
                changed++;
                continue;
            }
            Block block = state.getBlock();
            if (block instanceof WallSignBlock) {
                changed += sign(level, pos, state);
                continue;
            }
            if (state.hasBlockEntity()) continue;
            if (block instanceof DoorBlock) {
                if (state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER && level.getBlockState(pos.below()).canBeReplaced()) {
                    level.setBlock(pos.below(), floorLike(level, pos.below()), FLAGS);
                    changed++;
                }
            } else if (block instanceof DirtPathBlock) {
                level.setBlock(pos, Blocks.DIRT.defaultBlockState(), FLAGS);
                changed++;
            } else if (block instanceof LanternBlock) {
                // Handled with the chains below.
            } else if (block instanceof LeverBlock || block instanceof ButtonBlock) {
                // Working controls are kept where the story put them.
                TheOldestHouse.LOGGER.debug("Left an unsupported control in {} at {}", place.id(), pos);
            } else if (block instanceof SugarCaneBlock || block instanceof SnowLayerBlock || block instanceof DoublePlantBlock
                    || !state.isCollisionShapeFullBlock(level, pos)) {
                // What vanilla would drop on the next neighbour update: take it now, with no item.
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), FLAGS | Block.UPDATE_SUPPRESS_DROPS);
                changed++;
            }
        }
        return changed;
    }

    /**
     * A wall sign set into the wall line itself steps out onto the wall's face,
     * which is restored behind it; a free-standing one gets a post. Its words
     * are kept either way.
     */
    private static int sign(ServerLevel level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(WallSignBlock.FACING);
        BlockState left = level.getBlockState(pos.relative(facing.getClockWise())), right = level.getBlockState(pos.relative(facing.getCounterClockWise()));
        BlockPos front = pos.relative(facing);
        CompoundTag words = level.getBlockEntity(pos) == null ? null : level.getBlockEntity(pos).saveWithoutMetadata(level.registryAccess());
        if (left.is(right.getBlock()) && left.isCollisionShapeFullBlock(level, pos) && !left.hasBlockEntity() && level.getBlockState(front).isAir()) {
            level.setBlock(pos, left, FLAGS);
            level.setBlock(front, state, FLAGS);
            restore(level, front, words);
            return 2;
        }
        var key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        Block standing = BuiltInRegistries.BLOCK.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(key.getNamespace(),
                key.getPath().replace("_wall_sign", "_sign")));
        BlockPos ground = pos.below();
        while (ground.getY() > pos.getY() - 4 && level.getBlockState(ground).isAir()) ground = ground.below();
        if (!(standing instanceof StandingSignBlock) || !level.getBlockState(ground).isFaceSturdy(level, ground, Direction.UP)) return 0;
        for (BlockPos post = ground.above(); post.getY() < pos.getY(); post = post.above())
            level.setBlock(post, Blocks.SPRUCE_FENCE.defaultBlockState(), FLAGS);
        level.setBlock(pos, standing.defaultBlockState().setValue(StandingSignBlock.ROTATION, RotationSegment.convertToSegment(facing)), FLAGS);
        restore(level, pos, words);
        return 1 + pos.getY() - ground.getY();
    }

    private static void restore(ServerLevel level, BlockPos pos, CompoundTag words) {
        var entity = level.getBlockEntity(pos);
        if (entity == null || words == null) return;
        entity.loadWithComponents(words, level.registryAccess());
        entity.setChanged();
        level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), Block.UPDATE_CLIENTS);
    }

    /** The same fitting turned to the first way it can hang or stand, keeping everything else. */
    private static BlockState reorient(ServerLevel level, BlockPos pos, BlockState state) {
        List<Property<?>> turning = new ArrayList<>();
        for (Property<?> property : state.getProperties())
            if (property == BlockStateProperties.HORIZONTAL_FACING || property == BlockStateProperties.FACING
                    || property == BlockStateProperties.ATTACH_FACE || property == BlockStateProperties.BELL_ATTACHMENT
                    || property == BlockStateProperties.HANGING) turning.add(property);
        if (turning.isEmpty() || state.getBlock() instanceof DoorBlock) return null;
        for (BlockState candidate : state.getBlock().getStateDefinition().getPossibleStates()) {
            boolean same = true;
            for (Property<?> property : state.getProperties())
                if (!turning.contains(property) && !candidate.getValue(property).equals(state.getValue(property))) {
                    same = false;
                    break;
                }
            if (same && candidate != state && candidate.canSurvive(level, pos)) return candidate;
        }
        return null;
    }

    private static BlockState floorLike(ServerLevel level, BlockPos pos) {
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockState next = level.getBlockState(pos.relative(side));
            if (next.isCollisionShapeFullBlock(level, pos.relative(side)) && !next.hasBlockEntity()) return next;
        }
        return Blocks.SPRUCE_PLANKS.defaultBlockState();
    }

    // ------------------------------------------------------------------
    // Hanging lanterns hang from something.

    private static int lanterns(ServerLevel level, BlockPos base, LabyrinthPlace place) {
        BoundingBox r = place.room();
        int top = base.getY() + r.maxY() + 3, changed = 0;
        List<BlockPos> lamps = new ArrayList<>();
        for (BlockPos at : BlockPos.betweenClosed(base.offset(r.minX(), r.minY(), r.minZ()), base.offset(r.maxX(), r.maxY(), r.maxZ()))) {
            BlockState state = level.getBlockState(at);
            if (state.getBlock() instanceof LanternBlock) lamps.add(at.immutable());
        }
        for (BlockPos lamp : lamps) {
            BlockState state = level.getBlockState(lamp);
            if (FLOATING_BY_DESIGN.contains(place) && state.getValue(LanternBlock.HANGING)) continue;
            if (!state.getValue(LanternBlock.HANGING)) {
                if (state.canSurvive(level, lamp)) continue;
                level.setBlock(lamp, state.setValue(LanternBlock.HANGING, true), FLAGS);
                state = level.getBlockState(lamp);
            }
            BlockPos up = lamp.above();
            while (level.getBlockState(up).getBlock() instanceof ChainBlock && up.getY() < top) up = up.above();
            if (Block.canSupportCenter(level, up, Direction.DOWN)) continue;
            // Run the chain on up to the ceiling, if there is one close above.
            BlockPos ceiling = up;
            while (ceiling.getY() < top && level.getBlockState(ceiling).isAir()) ceiling = ceiling.above();
            if (ceiling.getY() < top && Block.canSupportCenter(level, ceiling, Direction.DOWN) && ceiling.getY() - lamp.getY() <= 12) {
                for (BlockPos c = up; c.getY() < ceiling.getY(); c = c.above()) level.setBlock(c, Blocks.CHAIN.defaultBlockState(), FLAGS);
                changed += ceiling.getY() - up.getY();
                continue;
            }
            // Nothing overhead: a lamp post from the ground instead.
            for (BlockPos c = lamp.above(); c.getY() < up.getY(); c = c.above()) level.setBlock(c, Blocks.AIR.defaultBlockState(), FLAGS);
            BlockPos ground = lamp.below();
            while (ground.getY() > lamp.getY() - 8 && level.getBlockState(ground).getCollisionShape(level, ground).isEmpty()
                    && level.getFluidState(ground).isEmpty()) ground = ground.below();
            boolean sturdy = level.getBlockState(ground).isFaceSturdy(level, ground, Direction.UP);
            if (!sturdy) {
                level.setBlock(lamp, Blocks.AIR.defaultBlockState(), FLAGS);
                changed++;
                continue;
            }
            for (BlockPos c = ground.above(); c.getY() < lamp.getY(); c = c.above()) level.setBlock(c, Blocks.SPRUCE_FENCE.defaultBlockState(), FLAGS);
            level.setBlock(lamp, state.setValue(LanternBlock.HANGING, false), FLAGS);
            changed += lamp.getY() - ground.getY();
        }
        return changed;
    }

    // ------------------------------------------------------------------
    // Trees that stand on nothing (planted before a lake was dug under them).

    private static boolean foliage(BlockState state) {
        return state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES) || state.is(Blocks.VINE);
    }

    private static int floatingTrees(ServerLevel level, BlockPos base, LabyrinthPlace place) {
        BoundingBox r = place.room();
        int sx = r.maxX() - r.minX() + 1, sy = r.maxY() - r.minY() + 1, sz = r.maxZ() - r.minZ() + 1;
        BitSet solid = new BitSet(sx * sy * sz), tree = new BitSet(sx * sy * sz);
        BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
        for (int x = 0; x < sx; x++) for (int y = 0; y < sy; y++) for (int z = 0; z < sz; z++) {
            at.set(base.getX() + r.minX() + x, base.getY() + r.minY() + y, base.getZ() + r.minZ() + z);
            BlockState state = level.getBlockState(at);
            if (state.isAir() || state.is(Blocks.LIGHT) || state.is(Blocks.BARRIER)) continue;
            if (!level.getFluidState(at).isEmpty() && state.getCollisionShape(level, at).isEmpty()) continue;
            int i = (x * sy + y) * sz + z;
            solid.set(i);
            if (foliage(state)) tree.set(i);
        }
        BitSet seen = new BitSet(solid.size());
        int changed = 0;
        int[] queue = new int[Math.max(16, solid.cardinality())];
        for (int i = tree.nextSetBit(0); i >= 0; i = tree.nextSetBit(i + 1)) {
            if (seen.get(i)) continue;
            int head = 0, tail = 0;
            queue[tail++] = i;
            seen.set(i);
            boolean grounded = false;
            while (head < tail) {
                int c = queue[head++];
                int z = c % sz, y = (c / sz) % sy, x = c / (sz * sy);
                if (x == 0 || y == 0 || z == 0 || x == sx - 1 || z == sz - 1) grounded = true; // may continue outside the scene
                if (!tree.get(c)) {
                    grounded = true; // rests on something that is not a tree
                    continue;
                }
                int[][] n = {{x + 1, y, z}, {x - 1, y, z}, {x, y + 1, z}, {x, y - 1, z}, {x, y, z + 1}, {x, y, z - 1}};
                for (int[] p : n) {
                    if (p[0] < 0 || p[1] < 0 || p[2] < 0 || p[0] >= sx || p[1] >= sy || p[2] >= sz) continue;
                    int k = (p[0] * sy + p[1]) * sz + p[2];
                    if (solid.get(k) && !seen.get(k)) {
                        if (tree.get(k)) {
                            seen.set(k);
                            queue[tail++] = k;
                        } else grounded = true;
                    }
                }
            }
            if (grounded || tail > 800) continue;
            for (int q = 0; q < tail; q++) {
                int c = queue[q];
                int z = c % sz, y = (c / sz) % sy, x = c / (sz * sy);
                if (reserved(place, r.minX() + x, r.minY() + y, r.minZ() + z)) continue;
                level.setBlock(base.offset(r.minX() + x, r.minY() + y, r.minZ() + z), Blocks.AIR.defaultBlockState(), FLAGS);
                changed++;
            }
        }
        return changed;
    }

    // ------------------------------------------------------------------
    // Creatures placed inside blocks step out into the nearest open space.

    private static void unbury(ServerLevel level, BlockPos base, LabyrinthPlace place) {
        BoundingBox r = place.room();
        AABB box = new AABB(base.getX() + r.minX(), base.getY() + r.minY(), base.getZ() + r.minZ(),
                base.getX() + r.maxX() + 1, base.getY() + r.maxY() + 1, base.getZ() + r.maxZ() + 1);
        for (Mob mob : level.getEntitiesOfClass(Mob.class, box, Mob::isInWall)) {
            BlockPos from = mob.blockPosition();
            search:
            for (int d = 1; d <= 3; d++)
                for (BlockPos to : BlockPos.betweenClosed(from.offset(-d, -1, -d), from.offset(d, d, d))) {
                    AABB body = mob.getType().getDimensions().makeBoundingBox(to.getX() + 0.5D, to.getY(), to.getZ() + 0.5D);
                    if (level.noCollision(mob, body) && level.getBlockState(to.below()).isFaceSturdy(level, to.below(), Direction.UP)) {
                        mob.moveTo(to.getX() + 0.5D, to.getY(), to.getZ() + 0.5D, mob.getYRot(), mob.getXRot());
                        break search;
                    }
                }
        }
    }

    // ------------------------------------------------------------------
    // Outdoor scenes end in a wood, not at the edge of the world.

    private static boolean ground(BlockState state) {
        return state.is(BlockTags.DIRT) || state.is(Blocks.SAND) || state.is(Blocks.RED_SAND) || state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.GRAVEL) || state.is(Blocks.SANDSTONE) || state.is(Blocks.STONE) || state.is(Blocks.CLAY);
    }

    private static int border(ServerLevel level, BlockPos base, LabyrinthPlace place) {
        BoundingBox r = place.room();
        int changed = 0;
        for (int x = r.minX() - BORDER; x <= r.maxX() + BORDER; x++)
            for (int z = r.minZ() - BORDER; z <= r.maxZ() + BORDER; z++) {
                if (x >= r.minX() && x <= r.maxX() && z >= r.minZ() && z <= r.maxZ()) continue;
                // The arrival vestibule behind the entry door stays exactly as the door copies it.
                if (Math.abs(x) <= 10 && z >= 0 && z <= 20) continue;
                int cx = Math.max(r.minX(), Math.min(r.maxX(), x)), cz = Math.max(r.minZ(), Math.min(r.maxZ(), z));
                int surface = -1;
                BlockState top = Blocks.GRASS_BLOCK.defaultBlockState();
                boolean found = false;
                for (int y = Math.min(r.maxY(), 12); y >= Math.max(r.minY(), -12); y--) {
                    BlockState s = level.getBlockState(base.offset(cx, y, cz));
                    if (s.isAir() || !s.getFluidState().isEmpty() || s.canBeReplaced()) continue;
                    if (ground(s)) {
                        surface = y;
                        top = s.is(BlockTags.DIRT) && !s.is(Blocks.PODZOL) && !s.is(Blocks.COARSE_DIRT) && !s.is(Blocks.MYCELIUM)
                                ? Blocks.GRASS_BLOCK.defaultBlockState() : s;
                        found = true;
                        break;
                    }
                }
                if (!found) surface = -1;
                surface = Math.min(surface, 2);
                int distance = Math.max(Math.max(r.minX() - x, x - r.maxX()), Math.max(r.minZ() - z, z - r.maxZ()));
                // Already standing ground here (an earlier ring, or the scene's own skirt) is left alone.
                if (!level.getBlockState(base.offset(x, surface, z)).isAir()) continue;
                BlockState under = top.is(Blocks.SAND) ? Blocks.SANDSTONE.defaultBlockState()
                        : top.is(Blocks.SNOW_BLOCK) ? Blocks.SNOW_BLOCK.defaultBlockState() : Blocks.DIRT.defaultBlockState();
                for (int y = surface - 4; y < surface; y++) level.setBlock(base.offset(x, y, z), under, FLAGS);
                level.setBlock(base.offset(x, surface, z), top, FLAGS);
                changed += 5;
                boolean snowy = top.is(Blocks.SNOW_BLOCK);
                BlockState leaves = Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
                if (distance >= BORDER - 1) {
                    // The far edge is undergrowth too thick to see through.
                    for (int y = surface + 1; y <= surface + 9; y++) level.setBlock(base.offset(x, y, z), leaves, FLAGS);
                    changed += 9;
                    continue;
                }
                int hash = Math.floorMod(x * 734287 + z * 912931 + place.ordinal() * 31, 97);
                if (distance >= 2 && hash < 11 && !top.is(Blocks.SAND)) changed += spruce(level, base.offset(x, surface + 1, z), 6 + hash % 5, leaves);
                else if (distance >= 1 && hash > 80 && top.is(Blocks.GRASS_BLOCK))
                    level.setBlock(base.offset(x, surface + 1, z), (hash % 2 == 0 ? Blocks.FERN : Blocks.SHORT_GRASS).defaultBlockState(), FLAGS);
                else if (snowy && level.getBlockState(base.offset(x, surface + 1, z)).isAir() && hash % 3 == 0)
                    level.setBlock(base.offset(x, surface + 1, z), Blocks.SNOW.defaultBlockState(), FLAGS);
            }
        return changed;
    }

    /** A spruce of the ordinary shape: a straight trunk in narrowing rings of needles. */
    private static int spruce(ServerLevel level, BlockPos root, int height, BlockState leaves) {
        int changed = 0;
        for (int y = 0; y < height; y++) {
            BlockPos at = root.above(y);
            if (!level.getBlockState(at).canBeReplaced()) return changed;
        }
        for (int y = 0; y < height; y++) {
            level.setBlock(root.above(y), Blocks.SPRUCE_LOG.defaultBlockState(), FLAGS);
            changed++;
        }
        for (int y = 2; y <= height; y++) {
            int fromTop = height - y;
            int radius = y == height ? 0 : fromTop % 2 == 0 ? Math.min(3, 1 + fromTop / 3) : Math.max(1, fromTop / 3);
            for (int dx = -radius; dx <= radius; dx++) for (int dz = -radius; dz <= radius; dz++) {
                if (Math.abs(dx) + Math.abs(dz) > radius + (radius > 1 ? 1 : 0)) continue;
                BlockPos at = root.offset(dx, y, dz);
                if (level.getBlockState(at).isAir()) {
                    level.setBlock(at, leaves, FLAGS);
                    changed++;
                }
            }
        }
        level.setBlock(root.above(height + 1), leaves, FLAGS);
        return changed + 1;
    }

    // ------------------------------------------------------------------
    // Interiors get enough light to read the room, hung from the ceiling.

    /** Block light over a scene's walkable floor, propagated the way blocks pass it (sky ignored). */
    private static final class Lighting {
        final BoundingBox r;
        final int sx, sy, sz;
        final int[] light;
        final boolean[] opaque;
        final List<BlockPos> floor = new ArrayList<>();
        final ArrayDeque<int[]> queue = new ArrayDeque<>();
        final BlockPos base;

        Lighting(ServerLevel level, BlockPos base, LabyrinthPlace place) {
            this.base = base;
            r = place.room();
            sx = r.maxX() - r.minX() + 1;
            sy = r.maxY() - r.minY() + 1;
            sz = r.maxZ() - r.minZ() + 1;
            light = new int[sx * sy * sz];
            opaque = new boolean[sx * sy * sz];
            BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
            for (int x = 0; x < sx; x++) for (int y = 0; y < sy; y++) for (int z = 0; z < sz; z++) {
                at.set(base.getX() + r.minX() + x, base.getY() + r.minY() + y, base.getZ() + r.minZ() + z);
                BlockState state = level.getBlockState(at);
                int i = (x * sy + y) * sz + z;
                opaque[i] = state.getLightBlock(level, at) >= 15;
                int emitted = state.getLightEmission();
                if (emitted > 0) {
                    light[i] = emitted;
                    queue.add(new int[]{x, y, z});
                }
                if (y + 2 < sy && state.isFaceSturdy(level, at, Direction.UP)) {
                    BlockPos up = at.above();
                    if (level.getBlockState(up).getCollisionShape(level, up).isEmpty()
                            && level.getBlockState(up.above()).getCollisionShape(level, up.above()).isEmpty()
                            && level.getFluidState(up).isEmpty()) floor.add(up.immutable());
                }
            }
            spread(queue, light, opaque, sx, sy, sz);
            // Only floor a visitor can actually reach from a doorway counts: not the sealed
            // space above a roof, nor the inside of a wall.
            BitSet reachable = reachable(level, base, place, r, sx, sy, sz);
            floor.removeIf(cell -> {
                int x = cell.getX() - base.getX() - r.minX(), y = cell.getY() - base.getY() - r.minY(), z = cell.getZ() - base.getZ() - r.minZ();
                return !reachable.get((x * sy + y) * sz + z);
            });
        }

        int at(BlockPos cell) {
            int x = cell.getX() - base.getX() - r.minX(), y = cell.getY() - base.getY() - r.minY(), z = cell.getZ() - base.getZ() - r.minZ();
            return light[(x * sy + y) * sz + z];
        }

        void add(BlockPos lamp, int value) {
            int x = lamp.getX() - base.getX() - r.minX(), y = lamp.getY() - base.getY() - r.minY(), z = lamp.getZ() - base.getZ() - r.minZ();
            if (x < 0 || y < 0 || z < 0 || x >= sx || y >= sy || z >= sz) return;
            light[(x * sy + y) * sz + z] = value;
            queue.add(new int[]{x, y, z});
            spread(queue, light, opaque, sx, sy, sz);
        }

        double dark() {
            if (floor.isEmpty()) return 0;
            int dark = 0;
            for (BlockPos cell : floor) if (at(cell) <= 3) dark++;
            return dark / (double) floor.size();
        }
    }

    private static boolean passable(ServerLevel level, BlockPos pos, BlockState state) {
        Block block = state.getBlock();
        return state.getCollisionShape(level, pos).isEmpty() || block instanceof DoorBlock
                || block instanceof net.minecraft.world.level.block.TrapDoorBlock || block instanceof net.minecraft.world.level.block.FenceGateBlock;
    }

    /** Open space connected to the scene's doorways, through open doors and gates. */
    private static BitSet reachable(ServerLevel level, BlockPos base, LabyrinthPlace place, BoundingBox r, int sx, int sy, int sz) {
        BitSet seen = new BitSet(sx * sy * sz);
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        for (var door : place.doors())
            for (Direction side : new Direction[]{door.facing(), door.facing().getOpposite()})
                for (int up = 0; up <= 1; up++) {
                    BlockPos start = base.offset(door.rel()).relative(side).above(up);
                    int x = start.getX() - base.getX() - r.minX(), y = start.getY() - base.getY() - r.minY(), z = start.getZ() - base.getZ() - r.minZ();
                    if (x < 0 || y < 0 || z < 0 || x >= sx || y >= sy || z >= sz) continue;
                    int i = (x * sy + y) * sz + z;
                    if (!seen.get(i) && passable(level, start, level.getBlockState(start))) {
                        seen.set(i);
                        queue.add(new int[]{x, y, z});
                    }
                }
        BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
        while (!queue.isEmpty()) {
            int[] c = queue.poll();
            int[][] n = {{c[0] + 1, c[1], c[2]}, {c[0] - 1, c[1], c[2]}, {c[0], c[1] + 1, c[2]}, {c[0], c[1] - 1, c[2]}, {c[0], c[1], c[2] + 1}, {c[0], c[1], c[2] - 1}};
            for (int[] p : n) {
                if (p[0] < 0 || p[1] < 0 || p[2] < 0 || p[0] >= sx || p[1] >= sy || p[2] >= sz) continue;
                int k = (p[0] * sy + p[1]) * sz + p[2];
                if (seen.get(k)) continue;
                at.set(base.getX() + r.minX() + p[0], base.getY() + r.minY() + p[1], base.getZ() + r.minZ() + p[2]);
                if (!passable(level, at, level.getBlockState(at))) continue;
                seen.set(k);
                queue.add(p);
            }
        }
        return seen;
    }

    /** Walkable floor a visitor can reach from the scene's doorways. */
    public static List<BlockPos> reachableFloor(ServerLevel level, BlockPos base, LabyrinthPlace place) {
        return new Lighting(level, base, place).floor;
    }

    /** The share of a scene's walkable floor left in darkness (block light three or less). */
    public static double darkFraction(ServerLevel level, BlockPos base, LabyrinthPlace place) {
        return new Lighting(level, base, place).dark();
    }

    /** The share of a scene's floor that may stay dark. */
    static double darkAllowed(LabyrinthPlace place) {
        return SPARSE.contains(place) ? 0.6D : DIM.contains(place) ? 0.45D : 0.2D;
    }

    static int light(ServerLevel level, BlockPos base, LabyrinthPlace place, double darkAllowed) {
        Lighting map = new Lighting(level, base, place);
        List<BlockPos> floor = map.floor;
        int changed = 0, lamps = 0;
        double darkness = map.dark();
        for (BlockPos cell : floor) {
            if (darkness <= darkAllowed || lamps >= 60) break;
            if (map.at(cell) > 3) continue;
            int rx = cell.getX() - base.getX(), ry = cell.getY() - base.getY(), rz = cell.getZ() - base.getZ();
            if (reserved(place, rx, ry, rz)) continue;
            // The ceiling directly above, low enough to hang from.
            int ceiling = -1;
            for (int h = 2; h <= 12; h++) {
                BlockState s = level.getBlockState(cell.above(h));
                if (s.isAir()) continue;
                if (Block.canSupportCenter(level, cell.above(h), Direction.DOWN)) ceiling = h;
                break;
            }
            if (ceiling < 3) continue;
            int lampAt = Math.min(3, ceiling - 1);
            boolean clear = true;
            for (int h = 2; h < ceiling && clear; h++) clear = level.getBlockState(cell.above(h)).isAir() && !reserved(place, rx, ry + h, rz);
            if (!clear) continue;
            for (int h = lampAt + 1; h < ceiling; h++) level.setBlock(cell.above(h), Blocks.CHAIN.defaultBlockState(), FLAGS);
            level.setBlock(cell.above(lampAt), Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), FLAGS);
            changed += ceiling - lampAt;
            lamps++;
            map.add(cell.above(lampAt), 15);
            darkness = map.dark();
        }
        return changed;
    }

    private static void spread(ArrayDeque<int[]> queue, int[] light, boolean[] opaque, int sx, int sy, int sz) {
        while (!queue.isEmpty()) {
            int[] c = queue.poll();
            int value = light[(c[0] * sy + c[1]) * sz + c[2]] - 1;
            if (value <= 0) continue;
            int[][] n = {{c[0] + 1, c[1], c[2]}, {c[0] - 1, c[1], c[2]}, {c[0], c[1] + 1, c[2]}, {c[0], c[1] - 1, c[2]}, {c[0], c[1], c[2] + 1}, {c[0], c[1], c[2] - 1}};
            for (int[] p : n) {
                if (p[0] < 0 || p[1] < 0 || p[2] < 0 || p[0] >= sx || p[1] >= sy || p[2] >= sz) continue;
                int k = (p[0] * sy + p[1]) * sz + p[2];
                if (opaque[k] || light[k] >= value) continue;
                light[k] = value;
                queue.add(p);
            }
        }
    }
}
