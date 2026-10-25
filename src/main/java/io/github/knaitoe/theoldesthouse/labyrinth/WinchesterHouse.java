package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseBlocks;
import io.github.knaitoe.theoldesthouse.house.SceneDetailBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.WinchesterFittingBlock.Kind;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The Winchester house (0.4.121): always unfinished. LiteraryRooms lays the shell, the sixteen easy-riser switchbacks and
 * the upper ledger; this adds what makes it the house that was never done. A stair of thirteen steps climbs into the
 * ceiling. Doors open onto walls, and one high in the east wall onto nothing; the door off the upper ledger still opens on
 * the drop into the hay. Leaded windows look onto more wallpaper, and one is set in the ledger's floor, over the carpenters'
 * sawhorse below. Under the ledger is the blue séance room: a table, one chair, a hand bell and thirteen robes. The hall
 * clock's dial has thirteen hours and it strikes thirteen.
 *
 * <p>The carpenters are never seen. Each reader hears them, privately, behind whichever wall is nearest: a hammer, a saw.
 * Between visits they rebuild: when a reader comes in to an empty house whose side passages nobody can see, the doors in
 * the side passages swap which of them open onto a wall, and the sawhorse and lumber have moved. The upper ledger, reached
 * by the switchbacks, stays the resolution.
 */
public final class WinchesterHouse {
    private static final int F = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    static final String STATE = "winchester_0121";
    /** The hall clock, two blocks tall, by the east wall in the entrance bay. */
    public static final BlockPos CLOCK = new BlockPos(29, 0, -6);
    /** The séance room under the ledger: its door in the last partition, its table and its one chair. */
    public static final BlockPos SEANCE_DOOR = new BlockPos(25, 0, -64), TABLE = new BlockPos(27, 0, -70), CHAIR = new BlockPos(27, 0, -72);
    /** The thirteen-step stair into the ceiling, up the west wall from the front. */
    public static final int STAIR_STEPS = 13, STAIR_Z = -2;
    /** The window in the ledger's floor. */
    public static final BlockPos FLOOR_WINDOW = new BlockPos(0, 7, -72);
    /** Side-passage doors the carpenters rebuild: their own doors stay, the wall behind one or the other goes up. */
    public static final List<BlockPos> CHANGING_DOORS = List.of(new BlockPos(-25, 0, -32), new BlockPos(25, 0, -32), new BlockPos(-25, 0, -48), new BlockPos(25, 0, -48));
    /** Where the carpenters' work stands for each layout. */
    private static final BlockPos[][] WORK = {{new BlockPos(27, 0, -40), new BlockPos(28, 0, -44)}, {new BlockPos(-27, 0, -40), new BlockPos(-28, 0, -44)}};
    public static final int STRIKE_AT = 600, STRIKE_EVERY = 30;

    private WinchesterHouse() {}

    private static void set(ServerLevel l, BlockPos b, int x, int y, int z, BlockState s) { BuildBlocks.set(l, b.offset(x, y, z), s, F); }
    private static void fill(ServerLevel l, BlockPos b, int x0, int y0, int z0, int x1, int y1, int z1, BlockState s) {
        for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++) for (int y = Math.min(y0, y1); y <= Math.max(y0, y1); y++)
            for (int z = Math.min(z0, z1); z <= Math.max(z0, z1); z++) set(l, b, x, y, z, s);
    }
    private static void fit(ServerLevel l, BlockPos b, BlockPos at, Kind k, Direction f) { BuildBlocks.set(l, b.offset(at), WinchesterFittingBlock.of(k, f), F); }
    private static void coat(ServerLevel l, BlockPos b, int x, int z, Direction f) { set(l, b, x, 2, z, SceneDetailBlock.state(SceneDetailBlock.Kind.COAT, f)); }

    public static void build(ServerLevel l, BlockPos b) {
        BlockState paper = LiteraryRegistry.WINCHESTER_WALLPAPER.get().defaultBlockState(), blue = LiteraryRegistry.WINCHESTER_SEANCE_PAPER.get().defaultBlockState();
        BlockState glass = LiteraryRegistry.WINCHESTER_LEADED_GLASS.get().defaultBlockState(), planks = Blocks.DARK_OAK_PLANKS.defaultBlockState();
        // The rails rest on a ledge along each flight instead of hanging in the air.
        BlockState ledge = Blocks.DARK_OAK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP);
        for (int flight = 0; flight < 16; flight++) for (int x = -21; x <= 21; x++) set(l, b, x, flight / 2, -9 - flight * 4, ledge);

        // Thirteen steps up the west wall into the ceiling: a closed string on the open side, balusters on it.
        for (int k = 0; k < STAIR_STEPS; k++) {
            int z = STAIR_Z - k;
            for (int x = -29; x <= -28; x++) {
                if (k > 0) fill(l, b, x, 0, z, x, k - 1, z, planks);
                set(l, b, x, k, z, Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
            }
            fill(l, b, -27, 0, z, -27, k, z, Blocks.STRIPPED_DARK_OAK_WOOD.defaultBlockState());
            set(l, b, -27, k + 1, z, Blocks.DARK_OAK_FENCE.defaultBlockState());
        }
        // The last two steps go on into a bulkhead under the ceiling.
        fill(l, b, -29, 12, -13, -27, 14, -13, paper);
        fill(l, b, -29, 13, -14, -27, 14, -14, paper);
        NovelRooms.sign(l, b.offset(-28, 12, -12), Direction.SOUTH, new String[]{"ATTIC", "Continue upward", "", ""});

        // Leaded windows that look onto more wallpaper.
        for (int side : new int[]{-1, 1}) for (int z : new int[]{-22, -37, -52}) {
            fill(l, b, side * 30, 2, z, side * 30, 4, z - 1, glass);
            fill(l, b, side * 31, 2, z, side * 31, 4, z - 1, paper);
        }
        // A window in the floor of the upper ledger, over the carpenters' sawhorse.
        fill(l, b, FLOOR_WINDOW.getX() - 1, 7, FLOOR_WINDOW.getZ() - 1, FLOOR_WINDOW.getX() + 1, 7, FLOOR_WINDOW.getZ() + 1, glass);
        fit(l, b, new BlockPos(0, 0, -72), Kind.SAWHORSE, Direction.EAST);
        fit(l, b, new BlockPos(2, 0, -74), Kind.LUMBER, Direction.NORTH);
        fit(l, b, new BlockPos(-2, 0, -71), Kind.NAIL_KEG, Direction.SOUTH);
        set(l, b, 0, 5, -72, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 9));

        // A door onto the west wall, numbered; a door high in the east wall onto nothing, on a sill.
        NovelRooms.door(l, b.offset(-29, 0, -40), Direction.EAST, Blocks.DARK_OAK_DOOR, false);
        fit(l, b, new BlockPos(-29, 1, -41), Kind.PLAQUE, Direction.EAST);
        set(l, b, 29, 8, -40, ledge);
        NovelRooms.door(l, b.offset(29, 9, -40), Direction.WEST, Blocks.DARK_OAK_DOOR, false);

        // The hall clock by the east wall of the entrance bay.
        fit(l, b, CLOCK, Kind.CLOCK, Direction.WEST);
        fit(l, b, CLOCK.above(), Kind.CLOCK_TOP, Direction.WEST);

        // The blue séance room under the ledger, entered by the last partition's east door.
        fill(l, b, 24, 0, -76, 24, 6, -65, blue);
        fill(l, b, 30, 0, -76, 30, 6, -65, blue);
        fill(l, b, 25, 0, -77, 29, 6, -77, blue);
        fill(l, b, 27, 0, -64, 29, 6, -64, blue);
        fill(l, b, 25, 3, -64, 26, 6, -64, blue);
        for (int x = 24; x <= 29; x++) for (int z = -76; z <= -65; z++) if (BuildBlocks.state(l, b.offset(x, 7, z)).isAir()) set(l, b, x, 7, z, planks);
        fit(l, b, TABLE, Kind.SEANCE_TABLE, Direction.SOUTH);
        fit(l, b, CHAIR, Kind.SEANCE_CHAIR, Direction.SOUTH);
        // Thirteen robes on their hooks: five on each long wall, three at the end.
        for (int z : new int[]{-75, -73, -71, -69, -67}) { coat(l, b, 25, z, Direction.EAST); coat(l, b, 29, z, Direction.WEST); }
        for (int x : new int[]{26, 27, 28}) coat(l, b, x, -76, Direction.SOUTH);
        set(l, b, 27, 5, -70, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 6));
        fit(l, b, new BlockPos(27, 1, -63), Kind.PLAQUE, Direction.SOUTH);

        layout(l, b, 0);
    }

    /** Hang the side-passage walls and set out the carpenters' work for one layout (0 or 1). */
    static void layout(ServerLevel l, BlockPos b, int layout) {
        BlockState paper = LiteraryRegistry.WINCHESTER_WALLPAPER.get().defaultBlockState(), air = Blocks.AIR.defaultBlockState();
        for (int i = 0; i < CHANGING_DOORS.size(); i++) {
            var door = CHANGING_DOORS.get(i);
            // Layout 0 walls up the east door at -32 and the west at -48; layout 1 the other two.
            boolean walled = (i == 1 || i == 2) == (layout == 0);
            for (int dy = 0; dy < 2; dy++) BuildBlocks.set(l, b.offset(door.getX(), dy, door.getZ() - 1), walled ? paper : air, F);
        }
        for (var at : WORK[1 - layout]) if (BuildBlocks.state(l, b.offset(at)).is(LiteraryRegistry.WINCHESTER_FITTING.get())) BuildBlocks.set(l, b.offset(at), air, F);
        fit(l, b, WORK[layout][0], Kind.SAWHORSE, Direction.NORTH);
        fit(l, b, WORK[layout][1], Kind.LUMBER, Direction.NORTH);
    }

    /** Whether a side passage door opens onto wall now. */
    public static boolean walled(ServerLevel l, BlockPos b, BlockPos door) { return !l.getBlockState(b.offset(door).north()).isAir(); }
    public static int layout(LabyrinthData d, BlockPos b) { return d.stateEntry(STATE, Long.toString(b.asLong())).getInt("Layout"); }

    private static AABB area(BlockPos b) { var r = LabyrinthPlace.WINCHESTER.room(); return new AABB(Vec3.atLowerCornerOf(b.offset(r.minX(), r.minY(), r.minZ())), Vec3.atLowerCornerOf(b.offset(r.maxX() + 1, r.maxY() + 1, r.maxZ() + 1))); }

    /**
     * A reader comes in. Their clock and carpenters start over, and if they have the house to themselves and nobody can see
     * the side passages, the carpenters have been at work since the last visit.
     */
    public static void arrive(ServerPlayer p, BlockPos b, CompoundTag own) {
        own.putInt("Strikes", 0);
        own.putInt("StrikeAt", STRIKE_AT);
        own.putInt("Carpenter", 60 + p.getRandom().nextInt(80));
        var l = p.serverLevel();var d = LabyrinthData.get(p.server);String key = Long.toString(b.asLong());
        var s = d.stateEntry(STATE, key);
        int visits = s.getInt("Visits") + 1;s.putInt("Visits", visits);
        if (visits > 1 && rebuildable(l, b, p)) {
            int next = 1 - s.getInt("Layout");
            layout(l, b, next);
            s.putInt("Layout", next);
            s.putInt("Rebuilds", s.getInt("Rebuilds") + 1);
        }
        d.setStateEntry(STATE, key, s);
    }

    /** Nobody else in the house, no camera on a changing door or the work, and nothing living where a wall would go up. */
    static boolean rebuildable(ServerLevel l, BlockPos b, ServerPlayer arriving) {
        var area = area(b);
        for (var o : l.players()) if (o != arriving && area.inflate(8).intersects(o.getCamera().getBoundingBox())) return false;
        for (var door : CHANGING_DOORS) {
            var behind = b.offset(door).north();
            if (!l.isLoaded(behind) || LiteraryVignettes.watched(l, behind.getCenter()) || LiteraryVignettes.watched(l, b.offset(door).getCenter())) return false;
            if (!l.getEntitiesOfClass(LivingEntity.class, new AABB(behind).inflate(1.5)).isEmpty()) return false;
        }
        for (var work : WORK) for (var at : work) {
            if (LiteraryVignettes.watched(l, b.offset(at).getCenter())) return false;
            if (!l.getEntitiesOfClass(LivingEntity.class, new AABB(b.offset(at)).inflate(1.5)).isEmpty()) return false;
        }
        return true;
    }

    /** Every five ticks for a reader inside: the carpenters behind the nearest wall, and the clock at its hour. */
    public static void reader(ServerPlayer p, BlockPos b, CompoundTag own) {
        int t = own.getInt("ThisVisitTicks");
        if (t >= own.getInt("Carpenter")) {
            var rng = p.getRandom();
            double rx = p.getX() - b.getX(), ry = p.getY() - b.getY(), rz = p.getZ() - b.getZ();
            // Inside the nearer side wall, a few blocks along from them, about head height.
            Vec3 at = new Vec3(rx < 0 ? -30.5 : 31.5, Math.max(1, Math.min(13, ry + 1 + rng.nextInt(3) - 1)), Math.max(-76, Math.min(-2, rz + rng.nextInt(13) - 6)));
            boolean saw = rng.nextInt(3) == 0;
            sound(p, Vec3.atLowerCornerOf(b).add(at), saw ? LiteraryRegistry.WINCHESTER_SAW.get() : LiteraryRegistry.WINCHESTER_HAMMER.get(), .9F, .9F + rng.nextFloat() * .2F);
            own.putInt("Carpenter", t + 100 + rng.nextInt(120));
        }
        if (t >= STRIKE_AT && own.getInt("Strikes") < 13 && t >= own.getInt("StrikeAt")) {
            sound(p, Vec3.atCenterOf(b.offset(CLOCK.above())), LiteraryRegistry.WINCHESTER_CHIME.get(), 2.5F, 1F);
            own.putInt("Strikes", own.getInt("Strikes") + 1);
            own.putInt("StrikeAt", t + STRIKE_EVERY);
        }
    }

    private static void sound(ServerPlayer p, Vec3 at, SoundEvent sound, float volume, float pitch) {
        p.connection.send(new ClientboundSoundPacket(Holder.direct(sound), SoundSource.AMBIENT, at.x, at.y, at.z, volume, pitch, p.getRandom().nextLong()));
    }

    /** The cells a generic test may treat as the house's own fittings. */
    public static boolean fitting(BlockState s) { return s.is(LiteraryRegistry.WINCHESTER_FITTING.get()) || s.is(HouseBlocks.SCENE_DETAIL.get()); }
}
