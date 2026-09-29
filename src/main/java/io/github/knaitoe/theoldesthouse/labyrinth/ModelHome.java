package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.math.Transformation;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.house.HouseWatchers;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Poltergeist: the model home. Multi-visit; verb: turning around.
 *
 * A furnished show home, bright and empty: a living room, a kitchen with a
 * table and six chairs, a kid's room whose window looks onto a small yard at
 * night. The salesman's binder lies on the kitchen counter (the headstones
 * were moved, the bodies weren't); it is this vignette's object, and can be
 * taken on any visit.
 *
 * One beat per visit ({@link LabyrinthPlace.Kind#MULTI_VISIT}):
 * <ol>
 *   <li>Once a player has seen the table set, turning away from it for a
 *   second stacks every chair on it, nearly to the ceiling (block displays,
 *   so the stack can sway). It teeters more the closer they come. Seeing the
 *   stack is the visit's beat; the next time nobody is looking, the chairs
 *   are back where they were, and they stay there.</li>
 *   <li>Again, and the living room's armchairs are in the stack too.</li>
 *   <li>Again, with the kid's chair as well. A branch taps at the kid's
 *   window, and the grass in the yard has sunk in one long patch.</li>
 *   <li>The last visit: the chairs stay put. The kid's door stands open, the
 *   window is broken, and a branch has come in over the bed. Seeing it
 *   finishes the model home.</li>
 * </ol>
 * All the while the tree in the yard stands a stage closer on every visit.
 *
 * The house lies north of its entry door (see {@link LabyrinthPlace}):
 * inside, x -8..8, z -18..-1, six high; the yard is west of the kid's room,
 * x -22..-10, z -20..-6, walled and roofed in black.
 */
public final class ModelHome {
    public static final String ID = "model_home";
    /** The visit on which the tree is in and the model home is finished. */
    public static final int FINAL_VISIT = 4;

    /** A chair where it belongs: a stair block, its tall back to {@code back}. */
    public record Chair(BlockPos pos, Direction back) {
        BlockState state() {
            return LabyrinthBuilder.stairs(Blocks.SPRUCE_STAIRS, back);
        }
    }

    /** The table: two posts side by side, their tops the table top. */
    public static final BlockPos[] TABLE = {new BlockPos(4, 0, -13), new BlockPos(5, 0, -13)};
    public static final Chair[] KITCHEN_CHAIRS = {
            new Chair(new BlockPos(3, 0, -13), Direction.WEST),
            new Chair(new BlockPos(6, 0, -13), Direction.EAST),
            new Chair(new BlockPos(4, 0, -14), Direction.NORTH),
            new Chair(new BlockPos(5, 0, -14), Direction.NORTH),
            new Chair(new BlockPos(4, 0, -12), Direction.SOUTH),
            new Chair(new BlockPos(5, 0, -12), Direction.SOUTH)
    };
    public static final Chair[] ARMCHAIRS = {
            new Chair(new BlockPos(2, 0, -3), Direction.WEST),
            new Chair(new BlockPos(2, 0, -5), Direction.WEST)
    };
    public static final Chair KIDS_CHAIR = new Chair(new BlockPos(-3, 0, -11), Direction.EAST);
    public static final BlockPos BINDER = new BlockPos(8, 1, -12);
    /** The kid's window, lower pane; the upper is above it. */
    public static final BlockPos WINDOW = new BlockPos(-9, 1, -13);
    public static final BlockPos KIDS_DOOR = new BlockPos(-4, 0, -8);
    public static final BlockPos BED_FOOT = new BlockPos(-8, 0, -12);
    /** Where the tree's trunk stands on each visit: further off, then at the window, then in it. */
    private static final int[] TRUNK_X = {-20, -20, -16, -12, -11};
    private static final int TRUNK_Z = -13;

    private static final int HEIGHT = 5;
    private static final String TAG = TheOldestHouse.MOD_ID + "_model_home_chair";
    private static final String INDEX_TAG = TAG + "_";
    private static final BlockState WALL = Blocks.WHITE_CONCRETE.defaultBlockState();
    private static final BlockState FLOOR = Blocks.BIRCH_PLANKS.defaultBlockState();
    private static final BlockState CEILING = Blocks.SMOOTH_QUARTZ.defaultBlockState();
    private static final BlockState NIGHT = Blocks.BLACK_CONCRETE.defaultBlockState();
    private static final BlockState GRASS = Blocks.GRASS_BLOCK.defaultBlockState();

    /** Ticks (at one check every other tick) the table must go unwatched before the chairs move. */
    private static final int STACK_AFTER = 10;
    private static final int UNSTACK_AFTER = 20;
    private static final double NOTICE = 10.0D;
    private static final double TURNED_AWAY_WITHIN = 9.0D;
    private static final int TEETER_INTERVAL = 4;

    /** What is going on in the house right now; not saved (a restart puts the chairs back). */
    private static final class Scene {
        boolean stacked;
        boolean sawNeat;
        boolean seenStacked;
        boolean doneForVisit;
        int unwatched;
        long nextTap;
    }

    private static Scene scene = new Scene();
    private static boolean binderPending;

    private ModelHome() {
    }

    // ------------------------------------------------------------------
    // Visits

    /** The visit a new arrival plays: the first, the one left unfinished, or the next once its beat is done. */
    public static int nextVisit(int visit, boolean beatDone) {
        if (visit < 1) {
            return 1;
        }
        return beatDone && visit < FINAL_VISIT ? visit + 1 : visit;
    }

    /** The chairs the stack is made of on a visit. */
    public static List<Chair> stackedChairs(int visit) {
        List<Chair> chairs = new ArrayList<>(List.of(KITCHEN_CHAIRS));
        if (visit >= 2) {
            chairs.addAll(List.of(ARMCHAIRS));
        }
        if (visit >= 3) {
            chairs.add(KIDS_CHAIR);
        }
        return chairs;
    }

    private static List<Chair> allChairs() {
        return stackedChairs(FINAL_VISIT);
    }

    /**
     * Someone has come through the door. Unless somebody is already inside
     * (then the house stays as they have it), the visit moves on if the last
     * one's beat was done, and the house is set for it.
     */
    public static void onArrive(ServerPlayer player, LabyrinthPlace place) {
        if (place != LabyrinthPlace.MODEL_HOME) {
            return;
        }
        MinecraftServer server = player.server;
        ServerLevel level = server.getLevel(HouseDimensions.INTERIOR);
        BlockPos base = base(server);
        if (level == null || base == null || !visitors(level, base).isEmpty()) {
            return;
        }
        LabyrinthData data = LabyrinthData.get(server);
        CompoundTag state = data.state(ID);
        if (!data.isCompleted(ID)) {
            int visit = nextVisit(state.getInt("Visit"), state.getBoolean("BeatDone"));
            if (visit != state.getInt("Visit")) {
                state.putInt("Visit", visit);
                state.putBoolean("BeatDone", false);
                data.setState(ID, state);
                TheOldestHouse.LOGGER.info("{} is in the model home: visit {} of {}.", player.getGameProfile().getName(), visit, FINAL_VISIT);
            }
        }
        stage(level, base, visitFor(data));
    }

    /** The visit the house is set for: the saved one, at least the first; the last once finished. */
    static int visitFor(LabyrinthData data) {
        return data.isCompleted(ID) ? FINAL_VISIT : Math.max(1, data.state(ID).getInt("Visit"));
    }

    // ------------------------------------------------------------------
    // The house

    /** Called by the builder after the slot is filled solid. */
    public static void build(MinecraftServer server, ServerLevel level, BlockPos base) {
        buildHome(level, base);
        stage(level, base, visitFor(LabyrinthData.get(server)));
        binderPending = true;
        placeBinderIfLoaded(level, base, LabyrinthData.get(server));
    }

    /** The house, its furniture and the yard's walls, as they are on every visit. */
    public static void buildHome(ServerLevel level, BlockPos base) {
        int flags = LabyrinthBuilder.flags();
        // The yard first: the house's west wall is laid over the yard's east one.
        LabyrinthBuilder.room(level, base, -22, -10, 9, -20, -6, NIGHT, GRASS, NIGHT);
        LabyrinthBuilder.room(level, base, -8, 8, HEIGHT, -18, -1, WALL, FLOOR, CEILING);

        // Across the house at z -8, with an archway into the kitchen and the
        // kid's door; between the kid's room and the kitchen at x -1.
        for (int y = 0; y <= HEIGHT; y++) {
            for (int x = -8; x <= 8; x++) {
                level.setBlock(base.offset(x, y, -8), WALL, flags);
            }
            for (int z = -18; z <= -9; z++) {
                level.setBlock(base.offset(-1, y, z), WALL, flags);
            }
        }
        for (int x = 1; x <= 3; x++) {
            for (int y = 0; y <= 3; y++) {
                level.setBlock(base.offset(x, y, -8), Blocks.AIR.defaultBlockState(), flags);
            }
        }
        setKidsDoor(level, base, false);

        // Light: a show home is lit everywhere.
        for (BlockPos light : new BlockPos[]{new BlockPos(-4, HEIGHT + 1, -4), new BlockPos(4, HEIGHT + 1, -4),
                new BlockPos(4, HEIGHT + 1, -13), new BlockPos(4, HEIGHT + 1, -17)}) {
            level.setBlock(base.offset(light), Blocks.SEA_LANTERN.defaultBlockState(), flags);
        }
        level.setBlock(base.offset(-5, HEIGHT, -13), Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), flags);

        // The living room: a sofa and two armchairs facing a television, a rug, a fern.
        for (int z = -5; z <= -3; z++) {
            level.setBlock(base.offset(-7, 0, z), LabyrinthBuilder.stairs(Blocks.SPRUCE_STAIRS, Direction.WEST), flags);
        }
        for (int x = -3; x <= 0; x++) {
            for (int z = -5; z <= -3; z++) {
                level.setBlock(base.offset(x, 0, z), Blocks.LIGHT_GRAY_CARPET.defaultBlockState(), flags);
            }
        }
        level.setBlock(base.offset(7, 0, -4), LabyrinthBuilder.barrel(Direction.UP), flags);
        level.setBlock(base.offset(7, 1, -4), Blocks.BLACK_CONCRETE.defaultBlockState(), flags);
        level.setBlock(base.offset(-7, 0, -7), Blocks.POTTED_FERN.defaultBlockState(), flags);

        // The kitchen: the table, a counter along the east wall with a sink,
        // a stove and a refrigerator.
        level.setBlock(base.offset(TABLE[0]), Blocks.SPRUCE_FENCE.defaultBlockState().setValue(FenceBlock.EAST, true), flags);
        level.setBlock(base.offset(TABLE[1]), Blocks.SPRUCE_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true), flags);
        for (BlockPos leg : TABLE) {
            level.setBlock(base.offset(leg).above(), Blocks.SPRUCE_PRESSURE_PLATE.defaultBlockState(), flags);
        }
        for (int z = -17; z <= -10; z++) {
            level.setBlock(base.offset(8, 0, z), CEILING, flags);
        }
        level.setBlock(base.offset(8, 0, -15), Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3), flags);
        level.setBlock(base.offset(8, 0, -17), Blocks.SMOKER.defaultBlockState().setValue(AbstractFurnaceBlock.FACING, Direction.WEST), flags);
        level.setBlock(base.offset(8, 0, -18), Blocks.IRON_BLOCK.defaultBlockState(), flags);
        level.setBlock(base.offset(8, 1, -18), Blocks.IRON_BLOCK.defaultBlockState(), flags);

        // The kid's room: the bed under the window, a little table and chair.
        LabyrinthBuilder.bed(level, base.offset(BED_FOOT), Direction.NORTH, Blocks.RED_BED);
        level.setBlock(base.offset(-4, 0, -11), Blocks.SPRUCE_FENCE.defaultBlockState(), flags);
        level.setBlock(base.offset(-4, 1, -11), Blocks.SPRUCE_PRESSURE_PLATE.defaultBlockState(), flags);
        for (int x = -6; x <= -4; x++) {
            level.setBlock(base.offset(x, 0, -16), Blocks.YELLOW_CARPET.defaultBlockState(), flags);
        }

        LabyrinthBuilder.entrance(level, base, WALL, FLOOR, CEILING);
        LabyrinthBuilder.doors(level, base, LabyrinthPlace.MODEL_HOME);
    }

    /**
     * Sets the house for a visit: every chair in its place, the tree where it
     * stands this visit, the window whole or broken, the kid's door shut or
     * standing open.
     */
    public static void stage(ServerLevel level, BlockPos base, int visit) {
        clearStack(level, base);
        for (Chair chair : allChairs()) {
            level.setBlock(base.offset(chair.pos()), chair.state(), LabyrinthBuilder.flags());
        }
        yard(level, base, Math.min(FINAL_VISIT, Math.max(1, visit)));
        scene = new Scene();
    }

    private static void yard(ServerLevel level, BlockPos base, int visit) {
        int flags = LabyrinthBuilder.flags();
        for (int x = -22; x <= -10; x++) {
            for (int z = -20; z <= -6; z++) {
                level.setBlock(base.offset(x, -1, z), GRASS, flags);
                for (int y = 0; y <= 9; y++) {
                    level.setBlock(base.offset(x, y, z), Blocks.AIR.defaultBlockState(), flags);
                }
            }
        }
        // Whatever of the tree came into the kid's room last time goes back out.
        for (int x = -9; x <= -6; x++) {
            for (int y = 1; y <= 3; y++) {
                for (int z = TRUNK_Z - 1; z <= TRUNK_Z + 1; z++) {
                    BlockState here = level.getBlockState(base.offset(x, y, z));
                    if (here.is(Blocks.OAK_LOG) || here.is(Blocks.OAK_LEAVES)) {
                        level.setBlock(base.offset(x, y, z), Blocks.AIR.defaultBlockState(), flags);
                    }
                }
            }
        }
        if (visit >= 3) {
            // Where the grass has sunk, the length of someone.
            for (int x = -19; x <= -18; x++) {
                for (int z = -18; z <= -16; z++) {
                    level.setBlock(base.offset(x, -1, z), Blocks.COARSE_DIRT.defaultBlockState(), flags);
                }
            }
        }

        int trunk = TRUNK_X[visit];
        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        BlockState across = log.setValue(RotatedPillarBlock.AXIS, Direction.Axis.X);
        BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        for (int y = 0; y <= 5; y++) {
            level.setBlock(base.offset(trunk, y, TRUNK_Z), log, flags);
        }
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -1; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    int x = trunk + dx;
                    if (dx * dx + dy * dy + dz * dz > 6 || x > -10 || (dx == 0 && dz == 0 && dy <= 0)) {
                        continue;
                    }
                    level.setBlock(base.offset(x, 6 + dy, TRUNK_Z + dz), leaves, flags);
                }
            }
        }
        // A dim light by the tree, as from a streetlamp nobody can see.
        level.setBlock(base.offset(Math.min(-10, trunk + 2), 4, TRUNK_Z + 2),
                Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 7), flags);

        // A low branch towards the kid's window: at the glass, then through it.
        boolean inside = visit >= FINAL_VISIT;
        if (visit >= 3) {
            int reach = inside ? -6 : -10;
            for (int x = trunk + 1; x <= reach; x++) {
                if (x != -9 || inside) {
                    level.setBlock(base.offset(x, 2, TRUNK_Z), across, flags);
                }
            }
            for (int x = reach - 2; x <= reach; x++) {
                for (int y = 1; y <= 3; y++) {
                    for (int z = TRUNK_Z - 1; z <= TRUNK_Z + 1; z++) {
                        BlockPos at = base.offset(x, y, z);
                        if (x != -9 && level.getBlockState(at).isAir()) {
                            level.setBlock(at, leaves, flags);
                        }
                    }
                }
            }
        }

        // The window: whole, or broken in.
        BlockState pane = Blocks.GLASS_PANE.defaultBlockState()
                .setValue(CrossCollisionBlock.NORTH, true).setValue(CrossCollisionBlock.SOUTH, true);
        for (BlockPos glass : new BlockPos[]{WINDOW, WINDOW.above(), WINDOW.north(), WINDOW.north().above()}) {
            BlockPos at = base.offset(glass);
            if (!inside) {
                level.setBlock(at, pane, flags);
            } else if (!level.getBlockState(at).is(Blocks.OAK_LOG)) {
                level.setBlock(at, Blocks.AIR.defaultBlockState(), flags);
            }
        }
        setKidsDoor(level, base, inside);
    }

    private static void setKidsDoor(ServerLevel level, BlockPos base, boolean open) {
        BlockState door = Blocks.BIRCH_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, Direction.SOUTH)
                .setValue(DoorBlock.HINGE, DoorHingeSide.LEFT)
                .setValue(DoorBlock.OPEN, open);
        level.setBlock(base.offset(KIDS_DOOR), door.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER), LabyrinthBuilder.flags());
        level.setBlock(base.offset(KIDS_DOOR).above(), door.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), LabyrinthBuilder.flags());
    }

    @Nullable
    static BlockPos base(MinecraftServer server) {
        BlockPos origin = HouseSavedData.get(server).houseOrigin();
        return origin == null ? null : LabyrinthPlaces.base(origin, LabyrinthPlace.MODEL_HOME);
    }

    private static AABB house(BlockPos base) {
        return new AABB(base.getX() - 8, base.getY(), base.getZ() - 18, base.getX() + 9, base.getY() + HEIGHT + 1, base.getZ());
    }

    private static AABB kidsRoom(BlockPos base) {
        return new AABB(base.getX() - 8, base.getY(), base.getZ() - 18, base.getX() - 1, base.getY() + HEIGHT + 1, base.getZ() - 8);
    }

    public static boolean isInHouse(BlockPos base, Vec3 pos) {
        return house(base).contains(pos);
    }

    private static List<ServerPlayer> visitors(ServerLevel level, BlockPos base) {
        List<ServerPlayer> inside = new ArrayList<>();
        for (ServerPlayer player : level.players()) {
            if (!player.isSpectator() && isInHouse(base, player.position())) {
                inside.add(player);
            }
        }
        return inside;
    }

    /** The middle of the table top: where the stack stands and pivots. */
    public static Vec3 tableTop(BlockPos base) {
        return new Vec3(base.getX() + 5.0D, base.getY() + 1.07D, base.getZ() - 12.5D);
    }

    private static Vec3 window(BlockPos base) {
        return new Vec3(base.getX() + WINDOW.getX() + 0.5D, base.getY() + WINDOW.getY() + 1.0D, base.getZ() + WINDOW.getZ());
    }

    // ------------------------------------------------------------------
    // The stack

    /**
     * Every chair for the visit, off the floor and onto the table, in one
     * tick: the blocks go, and a stack of display copies stands on the table
     * top, alternately upright and upside down, nearly to the ceiling.
     */
    public static void stack(ServerLevel level, BlockPos base, int visit) {
        clearStack(level, base);
        List<Chair> chairs = stackedChairs(visit);
        for (Chair chair : chairs) {
            level.setBlock(base.offset(chair.pos()), Blocks.AIR.defaultBlockState(), LabyrinthBuilder.flags());
        }
        Vec3 pivot = tableTop(base);
        for (int i = 0; i < chairs.size(); i++) {
            BlockState state = chairs.get(i).state().setValue(StairBlock.HALF, i % 2 == 0 ? Half.BOTTOM : Half.TOP);
            spawnChair(level, pivot, state, i, chairs.size());
        }
        scene.stacked = true;
    }

    /** The chairs back in their places and the stack gone, as if it had never been. */
    public static void unstack(ServerLevel level, BlockPos base, int visit) {
        clearStack(level, base);
        for (Chair chair : stackedChairs(visit)) {
            level.setBlock(base.offset(chair.pos()), chair.state(), LabyrinthBuilder.flags());
        }
        scene.stacked = false;
    }

    public static List<Display.BlockDisplay> stackedDisplays(ServerLevel level, BlockPos base) {
        return level.getEntitiesOfClass(Display.BlockDisplay.class, house(base).inflate(2.0D), display -> display.getTags().contains(TAG));
    }

    private static void clearStack(ServerLevel level, BlockPos base) {
        stackedDisplays(level, base).forEach(Entity::discard);
    }

    /** How far apart the chairs sit in the stack, so that it reaches almost to the ceiling. */
    private static float step(int count) {
        float room = HEIGHT + 1 - 0.1F - 1.07F - 1.0F;
        return count <= 1 ? 0.0F : Math.min(0.7F, room / (count - 1));
    }

    /**
     * Chair {@code i} of the stack, leaning with the whole stack by {@code lean}
     * about the table top: set on the ones below it, turned a quarter more
     * each time, a little off true.
     */
    static Transformation chairTransform(int i, int count, Quaternionf lean) {
        float yaw = (float) Math.toRadians(i * 90.0D + (i % 2 == 0 ? 8.0D : -11.0D));
        Vector3f offset = new Vector3f((i % 3 - 1) * 0.07F, i * step(count), ((i + 1) % 3 - 1) * 0.07F).rotate(lean);
        Matrix4f matrix = new Matrix4f()
                .translate(offset)
                .rotate(lean)
                .rotateY(yaw)
                .translate(-0.5F, 0.0F, -0.5F);
        return new Transformation(matrix);
    }

    private static Tag encode(Transformation transformation) {
        // Built from a matrix, a Transformation splits itself into its parts
        // only when a getter asks, and the codec reads the parts directly.
        Transformation parts = new Transformation(transformation.getTranslation(), transformation.getLeftRotation(),
                transformation.getScale(), transformation.getRightRotation());
        return Transformation.EXTENDED_CODEC.encodeStart(NbtOps.INSTANCE, parts).getOrThrow();
    }

    @Nullable
    private static Display.BlockDisplay spawnChair(ServerLevel level, Vec3 pivot, BlockState state, int index, int count) {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", "minecraft:block_display");
        tag.put("block_state", NbtUtils.writeBlockState(state));
        tag.put("transformation", encode(chairTransform(index, count, new Quaternionf())));
        tag.putInt("interpolation_duration", TEETER_INTERVAL);
        Entity entity = EntityType.loadEntityRecursive(tag, level, created -> {
            created.moveTo(pivot.x, pivot.y, pivot.z, 0.0F, 0.0F);
            return created;
        });
        if (!(entity instanceof Display.BlockDisplay display)) {
            return null;
        }
        display.addTag(TAG);
        display.addTag(INDEX_TAG + index);
        display.addTag(INDEX_TAG + "of_" + count);
        return level.addFreshEntity(display) ? display : null;
    }

    private static int tagNumber(Display.BlockDisplay display, String prefix, int fallback) {
        for (String tag : display.getTags()) {
            if (tag.startsWith(prefix)) {
                try {
                    return Integer.parseInt(tag.substring(prefix.length()));
                } catch (NumberFormatException ignored) {
                    // Not this one.
                }
            }
        }
        return fallback;
    }

    /**
     * The stack sways about the table top, by {@code amplitude} radians at
     * most: barely at all from across the room, visibly at arm's length.
     */
    public static void teeter(ServerLevel level, BlockPos base, double amplitude, long time) {
        Quaternionf lean = new Quaternionf()
                .rotateZ((float) (amplitude * Math.sin(time * 0.21D)))
                .rotateX((float) (amplitude * 0.7D * Math.sin(time * 0.15D + 1.3D)));
        for (Display.BlockDisplay display : stackedDisplays(level, base)) {
            int count = tagNumber(display, INDEX_TAG + "of_", 1);
            int index = -1;
            for (String tag : display.getTags()) {
                if (tag.startsWith(INDEX_TAG) && !tag.startsWith(INDEX_TAG + "of_")) {
                    try {
                        index = Integer.parseInt(tag.substring(INDEX_TAG.length()));
                    } catch (NumberFormatException ignored) {
                        // Not the index.
                    }
                }
            }
            if (index < 0) {
                continue;
            }
            CompoundTag tag = display.saveWithoutId(new CompoundTag());
            tag.put("transformation", encode(chairTransform(index, count, lean)));
            tag.putInt("interpolation_duration", TEETER_INTERVAL);
            tag.putInt("start_interpolation", 0);
            display.load(tag);
        }
    }

    /** How much the stack sways with someone this far from it. */
    public static double amplitude(double distance) {
        if (distance >= 7.0D) {
            return 0.004D;
        }
        return 0.004D + Math.min(1.0D, (7.0D - distance) / 5.0D) * 0.09D;
    }

    // ------------------------------------------------------------------
    // Each tick

    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % 2 != 0) {
            return;
        }
        ServerLevel level = server.getLevel(HouseDimensions.INTERIOR);
        BlockPos base = base(server);
        if (level == null || base == null || !level.isLoaded(base)) {
            return;
        }
        LabyrinthData data = LabyrinthData.get(server);
        placeBinderIfLoaded(level, base, data);
        List<ServerPlayer> visitors = visitors(level, base);
        int visit = visitFor(data);
        if (visitors.isEmpty()) {
            if (scene.stacked) {
                unstack(level, base, visit);
            }
            return;
        }
        if (server.getTickCount() % 20 == 0) {
            checkBinder(level, base, data);
        }
        if (visit >= FINAL_VISIT) {
            tickLastVisit(level, base, data, visitors);
            return;
        }
        if (visit >= 3) {
            tapAtTheWindow(level, base, visitors);
        }
        tickChairs(level, base, data, visit, visitors);
    }

    private static void tickChairs(ServerLevel level, BlockPos base, LabyrinthData data, int visit, List<ServerPlayer> visitors) {
        Vec3 table = tableTop(base);
        double nearest = Double.MAX_VALUE;
        for (ServerPlayer player : visitors) {
            nearest = Math.min(nearest, player.position().distanceTo(table));
        }
        boolean watched = HouseWatchers.isWatched(level, table.add(0.0D, 0.2D, 0.0D));
        if (!scene.stacked) {
            if (scene.doneForVisit) {
                return;
            }
            if (watched) {
                scene.sawNeat |= nearest <= NOTICE;
                scene.unwatched = 0;
            } else if (scene.sawNeat && nearest <= TURNED_AWAY_WITHIN && ++scene.unwatched >= STACK_AFTER) {
                stack(level, base, visit);
                scene.unwatched = 0;
            }
            return;
        }

        if (watched && nearest <= NOTICE) {
            scene.unwatched = 0;
            if (!scene.seenStacked) {
                scene.seenStacked = true;
                CompoundTag state = data.state(ID);
                if (!state.getBoolean("BeatDone")) {
                    state.putBoolean("BeatDone", true);
                    data.setState(ID, state);
                    TheOldestHouse.LOGGER.info("The chairs in the model home were stacked on visit {}.", visit);
                }
            }
        } else if (scene.seenStacked && ++scene.unwatched >= UNSTACK_AFTER) {
            unstack(level, base, visit);
            scene.doneForVisit = true;
            return;
        }
        long time = level.getGameTime();
        if (time % TEETER_INTERVAL < 2) {
            double amplitude = amplitude(nearest);
            teeter(level, base, amplitude, time);
            if (amplitude > 0.05D && level.getRandom().nextInt(8) == 0) {
                level.playSound(null, BlockPos.containing(table.add(0.0D, 3.0D, 0.0D)), SoundEvents.WOOD_HIT, SoundSource.BLOCKS,
                        0.35F, 0.5F + level.getRandom().nextFloat() * 0.2F);
            }
        }
    }

    private static void tapAtTheWindow(ServerLevel level, BlockPos base, List<ServerPlayer> visitors) {
        long now = level.getGameTime();
        if (now < scene.nextTap) {
            return;
        }
        AABB kids = kidsRoom(base);
        for (ServerPlayer player : visitors) {
            if (kids.contains(player.position())) {
                level.playSound(null, BlockPos.containing(window(base)), SoundEvents.GLASS_HIT, SoundSource.BLOCKS, 0.7F,
                        0.6F + level.getRandom().nextFloat() * 0.2F);
                scene.nextTap = now + 40 + level.getRandom().nextInt(80);
                return;
            }
        }
    }

    private static void tickLastVisit(ServerLevel level, BlockPos base, LabyrinthData data, List<ServerPlayer> visitors) {
        if (data.isCompleted(ID)) {
            return;
        }
        AABB kids = kidsRoom(base);
        for (ServerPlayer player : visitors) {
            if (kids.contains(player.position()) && HouseWatchers.isWatched(level, window(base))) {
                CompoundTag state = data.state(ID);
                state.putBoolean("BeatDone", true);
                data.setState(ID, state);
                data.setCompleted(ID, true);
                level.playSound(null, BlockPos.containing(window(base)), SoundEvents.AZALEA_LEAVES_STEP, SoundSource.BLOCKS, 0.8F, 0.7F);
                TheOldestHouse.LOGGER.info("{} saw the tree in the kid's room; the model home is finished.", player.getGameProfile().getName());
                return;
            }
        }
    }

    // ------------------------------------------------------------------
    // The binder

    /** The salesman's binder on the counter, until someone takes it. */
    private static void placeBinderIfLoaded(ServerLevel level, BlockPos base, LabyrinthData data) {
        if (!binderPending || !level.areEntitiesLoaded(ChunkPos.asLong(base.offset(BINDER)))) {
            return;
        }
        binderPending = false;
        AABB counter = new AABB(base.offset(BINDER)).inflate(0.5D);
        level.getEntitiesOfClass(ItemFrame.class, counter).forEach(Entity::discard);
        ItemFrame frame = new ItemFrame(level, base.offset(BINDER), Direction.UP);
        if (!data.state(ID).getBoolean("BinderTaken")) {
            frame.setItem(binder(), false);
        }
        level.addFreshEntity(frame);
    }

    private static void checkBinder(ServerLevel level, BlockPos base, LabyrinthData data) {
        CompoundTag state = data.state(ID);
        if (state.getBoolean("BinderTaken") || binderPending) {
            return;
        }
        AABB counter = new AABB(base.offset(BINDER)).inflate(0.5D);
        List<ItemFrame> frames = level.getEntitiesOfClass(ItemFrame.class, counter);
        if (!frames.isEmpty() && frames.get(0).getItem().isEmpty()) {
            state.putBoolean("BinderTaken", true);
            data.setState(ID, state);
            TheOldestHouse.LOGGER.info("The salesman's binder was taken from the model home.");
        }
    }

    /** Empty frames in the house stay where they are. */
    public static void onAttackEntity(AttackEntityEvent event) {
        if (event.getTarget() instanceof ItemFrame frame && frame.getItem().isEmpty()
                && frame.level() instanceof ServerLevel level && level.dimension().equals(HouseDimensions.INTERIOR)) {
            BlockPos base = base(level.getServer());
            if (base != null && isInHouse(base, frame.position())) {
                event.setCanceled(true);
            }
        }
    }

    public static ItemStack binder() {
        List<Filterable<Component>> pages = new ArrayList<>();
        for (String page : new String[]{
                "CUESTA VERDE\nPhase Four\n\nWelcome home.\n\nThree bedrooms, two baths, and a kitchen your family will gather in.\n\nAsk us about easy financing.",
                "THE MODEL HOME\n\nEvery home in Phase Four is built to this plan.\n\nFurnishings are for display only.\n\nPlease do not sit on the chairs.",
                "Lot 1127 backs onto the greenbelt: mature trees, left standing wherever the grading allowed.\n\nImagine the children's swing.",
                "(clipped inside the back cover)\n\nRe: Phase Four site preparation.\n\nThe county permit covered relocating the headstones. It required nothing more, so nothing more was moved.\n\nDo not raise this with buyers.",
                "(in pencil, on the back of a price sheet)\n\nchairs again this morning.\n\nall of them, on the table.\n\ni put them back before the first appointment."
        }) {
            pages.add(Filterable.passThrough(Component.literal(page)));
        }
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        book.set(DataComponents.WRITTEN_BOOK_CONTENT,
                new WrittenBookContent(Filterable.passThrough("Cuesta Verde: The Model Home"), "Sales Office", 0, pages, true));
        return VignetteYields.mark(book, ID);
    }

    // ------------------------------------------------------------------
    // Status and testing

    public static List<String> describe(MinecraftServer server) {
        LabyrinthData data = LabyrinthData.get(server);
        CompoundTag state = data.state(ID);
        int visit = state.getInt("Visit");
        String where = data.isCompleted(ID) ? "finished"
                : visit < 1 ? "not visited yet"
                : "visit " + visit + " of " + FINAL_VISIT + (state.getBoolean("BeatDone") ? ", its beat done (the next arrival starts the next)" : ", its beat not yet seen");
        return List.of("Model home: " + where + "; the binder " + (state.getBoolean("BinderTaken") ? "has been taken" : "is on the counter")
                + (scene.stacked ? "; the chairs are stacked right now" : "") + ".");
    }

    /** Sets the saved visit (its beat not yet done) and the house for it, unfinished. */
    public static boolean setVisit(MinecraftServer server, int visit) {
        ServerLevel level = server.getLevel(HouseDimensions.INTERIOR);
        BlockPos base = base(server);
        if (level == null || base == null) {
            return false;
        }
        LabyrinthData data = LabyrinthData.get(server);
        CompoundTag state = data.state(ID);
        state.putInt("Visit", Math.max(0, Math.min(FINAL_VISIT, visit)));
        state.putBoolean("BeatDone", false);
        data.setState(ID, state);
        data.setCompleted(ID, false);
        stage(level, base, Math.max(1, visit));
        return true;
    }

    /** Puts the model home back as new: never visited, the binder on the counter, dealable again. */
    public static boolean reset(MinecraftServer server) {
        ServerLevel level = server.getLevel(HouseDimensions.INTERIOR);
        BlockPos base = base(server);
        if (level == null || base == null) {
            return false;
        }
        LabyrinthData data = LabyrinthData.get(server);
        data.setCompleted(ID, false);
        data.setState(ID, new CompoundTag());
        build(server, level, base);
        return true;
    }

    public static void clearAll() {
        scene = new Scene();
        binderPending = false;
    }
}
