package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.math.Transformation;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseBlocks;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.house.HouseWatchers;
import io.github.knaitoe.theoldesthouse.house.TractHouseBlock;
import io.github.knaitoe.theoldesthouse.house.TractHouseBlock.Kind;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.sounds.SoundEvent;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
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
 * A Cuesta Verde show home, as built in 1982 (0.4.121): wood-paneled living room on shag carpet with a plaid sofa, two
 * recliners and a console television; a linoleum kitchen in harvest gold with a honey-oak table and six chairs; a child's
 * room in pink whose window looks onto a small yard at night. The salesman's binder lies on the kitchen counter (the
 * headstones were moved, the bodies weren't); it is this vignette's object, and can be taken on any visit. Everything is
 * the tract_house block and its materials, drawn by tools/draw_tract_house.py; no pass dresses it.
 *
 * One beat per visit ({@link LabyrinthPlace.Kind#MULTI_VISIT}):
 * <ol>
 *   <li>The child's door is locked. Once a player has seen the table set, turning away from it for a second stacks every
 *   kitchen chair on it, nearly to the ceiling (block displays, so the stack can sway). Seeing the stack is the beat; the
 *   next time nobody is looking, the chairs are back where they were.</li>
 *   <li>Nothing stacks. The television is on, all static; watching it for a few seconds is the beat, and behind the
 *   watcher the child's door comes open, unlocked from now on.</li>
 *   <li>Nothing stacks. The tree in the yard has come to the window; whoever goes into the child's room sets it off: the
 *   glass breaks in and a limb reaches through toward them. Seeing it finishes the model home, and each later reader who
 *   goes in sees it reach for them too.</li>
 * </ol>
 * The tree in the yard stands a stage closer on every visit.
 *
 * The house lies north of its entry door (see {@link LabyrinthPlace}): inside, x -8..8, z -18..-1, five high, the living
 * room south of a double wall at z -8..-9, the kitchen east and the child's room west of a double wall at x -1..0; the
 * yard is west of the child's room, x -22..-10, z -20..-6, walled and roofed in black.
 */
public final class ModelHome {
    public static final String ID = "model_home";
    /** The visit on which the tree comes in and the model home is finished. */
    public static final int FINAL_VISIT = 3;

    /** A chair where it belongs, its back to {@code back}. */
    public record Chair(BlockPos pos, Direction back, Kind kind) {
        public Chair(BlockPos pos, Direction back) {
            this(pos, back, Kind.DINING_CHAIR);
        }
        BlockState state() {
            return TractHouseBlock.of(kind, back.getOpposite());
        }
    }

    /** The table: two halves side by side, their tops the table top. */
    public static final BlockPos[] TABLE = {new BlockPos(4, 0, -13), new BlockPos(5, 0, -13)};
    public static final Chair[] KITCHEN_CHAIRS = {
            new Chair(new BlockPos(3, 0, -13), Direction.WEST),
            new Chair(new BlockPos(6, 0, -13), Direction.EAST),
            new Chair(new BlockPos(4, 0, -14), Direction.NORTH),
            new Chair(new BlockPos(5, 0, -14), Direction.NORTH),
            new Chair(new BlockPos(4, 0, -12), Direction.SOUTH),
            new Chair(new BlockPos(5, 0, -12), Direction.SOUTH)
    };
    public static final Chair KIDS_CHAIR = new Chair(new BlockPos(-3, 0, -11), Direction.EAST, Kind.KIDS_CHAIR);
    public static final BlockPos BINDER = new BlockPos(8, 1, -12);
    /** The kid's window, lower pane; the upper is above it, the second pair north of these. */
    public static final BlockPos WINDOW = new BlockPos(-9, 1, -13);
    public static final BlockPos KIDS_DOOR = new BlockPos(-4, 0, -8);
    public static final BlockPos BED_FOOT = new BlockPos(-8, 0, -12);
    public static final BlockPos TV = new BlockPos(8, 0, -4);
    /** Where the tree's trunk stands on each visit: off across the yard, nearer, then at the window. */
    private static final int[] TRUNK_X = {-20, -20, -16, -12};
    private static final int TRUNK_Z = -13;

    static final int HEIGHT = 4;
    private static final String TAG = TheOldestHouse.MOD_ID + "_model_home_chair";
    private static final String INDEX_TAG = TAG + "_";
    private static final String LIMB_TAG = TheOldestHouse.MOD_ID + "_model_home_limb";
    private static final BlockState PANELING = HouseBlocks.TRACT_PANELING.get().defaultBlockState();
    private static final BlockState SHAG = HouseBlocks.TRACT_SHAG.get().defaultBlockState();
    private static final BlockState LINOLEUM = HouseBlocks.TRACT_LINOLEUM.get().defaultBlockState();
    private static final BlockState POPCORN = HouseBlocks.TRACT_POPCORN.get().defaultBlockState();
    private static final BlockState KITCHEN_PAPER = HouseBlocks.TRACT_KITCHEN_PAPER.get().defaultBlockState();
    private static final BlockState KIDS_PAPER = HouseBlocks.MODEL_HOME_KIDS_WALLPAPER.get().defaultBlockState();
    private static final BlockState NIGHT = Blocks.BLACK_CONCRETE.defaultBlockState();
    private static final BlockState GRASS = Blocks.GRASS_BLOCK.defaultBlockState();

    /** Ticks (at one check every other tick) the table must go unwatched before the chairs move. */
    private static final int STACK_AFTER = 10;
    private static final int UNSTACK_AFTER = 20;
    private static final double NOTICE = 10.0D;
    private static final double TURNED_AWAY_WITHIN = 9.0D;
    private static final int TEETER_INTERVAL = 4;
    /** Checks (every other tick) of watching the static before the child's door comes open: three seconds. */
    public static final int STATIC_WATCH = 30;
    /** The limb: its segments, how far it can reach from outside the window, and the ticks it takes to reach. */
    public static final int LIMB_SEGMENTS = 7;
    public static final double LIMB_REACH = 5.2D;
    static final int LIMB_GROW = 40;
    private static final Holder<SoundEvent> STATIC = Holder.direct(SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, "leak.television")));

    /** What is going on in the house right now; not saved (a restart puts the chairs back and the limb out). */
    private static final class Scene {
        boolean stacked;
        boolean sawNeat;
        boolean seenStacked;
        boolean doneForVisit;
        int unwatched;
        long nextTap;
        int watchedStatic;
        long nextHiss;
        /** Ticks since the limb came in, or -1 while it is outside; it grows, holds, and goes back when the room empties. */
        int limbAge = -1;
        int limbEmpty;
        Vec3 limbTarget;
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

    /** The chairs the stack is made of on a visit: the kitchen's six, on the first visit only. */
    public static List<Chair> stackedChairs(int visit) {
        return visit == 1 ? List.of(KITCHEN_CHAIRS) : List.of();
    }

    private static List<Chair> allChairs() {
        List<Chair> chairs = new ArrayList<>(List.of(KITCHEN_CHAIRS));
        chairs.add(KIDS_CHAIR);
        return chairs;
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
        if (level == null || base == null) return;
        if (visitors(level, base).stream().anyMatch(other -> !other.getUUID().equals(player.getUUID()))) {
            return;
        }
        LabyrinthData data = LabyrinthData.get(server);
        CompoundTag state = data.state(ID);
        if (!data.isCompleted(ID)) {
            int visit = nextVisit(state.getInt("Visit"), state.getBoolean("BeatDone"));
            if (visit != state.getInt("Visit")) {
                state.putInt("Visit", visit);
                state.putBoolean("BeatDone", false);
                state.remove("Unlocked");
                data.setState(ID, state);
                TheOldestHouse.LOGGER.info("{} is in the model home: visit {} of {}.", player.getGameProfile().getName(), visit, FINAL_VISIT);
            }
        }
        int visit = visitFor(data);
        stage(level, base, visit);
        String line = switch (visit) {
            case 1 -> "Six chairs around the kitchen table. The child's door is locked.";
            case 2 -> "The television is on. Nobody is watching it.";
            default -> data.isCompleted(ID) ? "The child's window is broken. The tree is right outside it." : "The child's door stands open. Something taps at the glass.";
        };
        player.displayClientMessage(Component.literal(line), false);
    }

    /** The visit the house is set for: the saved one, at least the first; the last once finished. */
    static int visitFor(LabyrinthData data) {
        return data.isCompleted(ID) ? FINAL_VISIT : Math.max(1, Math.min(FINAL_VISIT, data.state(ID).getInt("Visit")));
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

    private static void put(ServerLevel level, BlockPos base, int x, int y, int z, BlockState state) {
        level.setBlock(base.offset(x, y, z), state, LabyrinthBuilder.flags());
    }

    private static void furnish(ServerLevel level, BlockPos base, int x, int y, int z, Kind kind, Direction facing) {
        put(level, base, x, y, z, TractHouseBlock.of(kind, facing));
    }

    /** The house, its furniture and the yard's walls, as they are on every visit. */
    public static void buildHome(ServerLevel level, BlockPos base) {
        // The yard first: the house's west wall is laid over the yard's east one.
        LabyrinthBuilder.room(level, base, -22, -10, 9, -20, -6, NIGHT, GRASS, NIGHT);
        LabyrinthBuilder.room(level, base, -8, 8, HEIGHT, -18, -1, PANELING, SHAG, POPCORN);

        // The kitchen's and the child's own walls and floors, and the double walls between the rooms.
        for (int y = -1; y <= HEIGHT + 1; y++) {
            for (int z = -19; z <= -9; z++) {
                put(level, base, 9, y, z, KITCHEN_PAPER);
                put(level, base, -9, y, z, KIDS_PAPER);
            }
            for (int x = -9; x <= 9; x++) put(level, base, x, y, -19, x <= -1 ? KIDS_PAPER : KITCHEN_PAPER);
        }
        for (int y = 0; y <= HEIGHT; y++) {
            for (int x = -8; x <= 8; x++) {
                put(level, base, x, y, -8, PANELING);
                put(level, base, x, y, -9, x <= -1 ? KIDS_PAPER : KITCHEN_PAPER);
            }
            for (int z = -18; z <= -10; z++) {
                put(level, base, -1, y, z, KIDS_PAPER);
                put(level, base, 0, y, z, KITCHEN_PAPER);
            }
        }
        for (int x = -8; x <= 8; x++) {
            for (int z = -18; z <= -10; z++) {
                if (x >= 1) put(level, base, x, -1, z, LINOLEUM);
                else if (x <= -2) put(level, base, x, -1, z, Blocks.PINK_WOOL.defaultBlockState());
            }
        }
        for (int x = -1; x <= 1; x++) for (int z = -2; z <= -1; z++) put(level, base, x, -1, z, LINOLEUM);
        // An archway from the living room into the kitchen; the child's door is in the double wall's south face.
        for (int x = 1; x <= 3; x++) for (int y = 0; y <= 2; y++) for (int z = -9; z <= -8; z++) put(level, base, x, y, z, Blocks.AIR.defaultBlockState());
        for (int y = 0; y <= 1; y++) put(level, base, KIDS_DOOR.getX(), y, -9, Blocks.AIR.defaultBlockState());
        setKidsDoor(level, base, false);

        // Windows: the living room's onto the same dark yard as the child's, so the tree is seen coming from both; a small
        // one in the kitchen's back wall onto the night, backed in black inside the slot.
        BlockState along = Blocks.GLASS_PANE.defaultBlockState().setValue(CrossCollisionBlock.NORTH, true).setValue(CrossCollisionBlock.SOUTH, true);
        BlockState across = Blocks.GLASS_PANE.defaultBlockState().setValue(CrossCollisionBlock.EAST, true).setValue(CrossCollisionBlock.WEST, true);
        for (int z = -7; z <= -6; z++) for (int y = 1; y <= 2; y++) put(level, base, -9, y, z, along);
        for (int x = 3; x <= 6; x++) for (int y = 0; y <= 3; y++) put(level, base, x, y, -20, NIGHT);
        for (int x = 4; x <= 5; x++) put(level, base, x, 2, -19, across);

        // Light: a show home is lit in every room.
        for (BlockPos light : new BlockPos[]{new BlockPos(-3, HEIGHT, -4), new BlockPos(4, HEIGHT, -4), new BlockPos(6, HEIGHT, -16), new BlockPos(-5, HEIGHT, -14)}) {
            put(level, base, light.getX(), light.getY(), light.getZ(), TractHouseBlock.of(Kind.CEILING_LIGHT, Direction.NORTH));
        }
        furnish(level, base, 3, HEIGHT, -11, Kind.PENDANT_LIGHT, Direction.NORTH);

        // The living room: the sofa against the west wall facing the console set, a recliner either side of the coffee
        // table, lamps at the sofa's ends, family photographs, a curtain at the window onto the yard.
        furnish(level, base, -8, 0, -5, Kind.SOFA_LEFT, Direction.EAST);
        furnish(level, base, -8, 0, -4, Kind.SOFA_MIDDLE, Direction.EAST);
        furnish(level, base, -8, 0, -3, Kind.SOFA_RIGHT, Direction.EAST);
        furnish(level, base, -8, 0, -6, Kind.END_TABLE_LAMP, Direction.EAST);
        furnish(level, base, -8, 0, -2, Kind.END_TABLE_LAMP, Direction.EAST);
        furnish(level, base, -6, 0, -4, Kind.COFFEE_TABLE, Direction.EAST);
        furnish(level, base, -6, 0, -6, Kind.RECLINER, Direction.SOUTH);
        furnish(level, base, -6, 0, -2, Kind.RECLINER, Direction.NORTH);
        furnish(level, base, -8, 2, -4, Kind.FAMILY_PHOTO, Direction.EAST);
        furnish(level, base, -3, 2, -1, Kind.FAMILY_PHOTO, Direction.NORTH);
        furnish(level, base, 2, 2, -1, Kind.FAMILY_PHOTO, Direction.NORTH);
        furnish(level, base, 5, 2, -1, Kind.FAMILY_PHOTO, Direction.NORTH);
        furnish(level, base, -8, 2, -5, Kind.CURTAIN_LEFT, Direction.EAST);
        put(level, base, 8, 0, -7, Blocks.POTTED_FERN.defaultBlockState());
        put(level, base, 8, 0, -1, Blocks.POTTED_FERN.defaultBlockState());

        // The kitchen: the table, counters down the east wall with the sink, the range and the fridge, cabinets above, the
        // phone on the north wall beside the window.
        furnish(level, base, TABLE[0].getX(), 0, TABLE[0].getZ(), Kind.TABLE_WEST, Direction.NORTH);
        furnish(level, base, TABLE[1].getX(), 0, TABLE[1].getZ(), Kind.TABLE_EAST, Direction.NORTH);
        for (int z = -16; z <= -10; z++) furnish(level, base, 8, 0, z, z == -15 ? Kind.COUNTER_SINK : Kind.COUNTER, Direction.WEST);
        furnish(level, base, 8, 0, -17, Kind.STOVE, Direction.WEST);
        furnish(level, base, 8, 0, -18, Kind.FRIDGE_LOWER, Direction.WEST);
        furnish(level, base, 8, 1, -18, Kind.FRIDGE_UPPER, Direction.WEST);
        for (int z = -18; z <= -10; z++) if (z != -17) furnish(level, base, 8, z == -18 ? 3 : 2, z, Kind.WALL_CABINET, Direction.WEST);
        furnish(level, base, 2, 1, -18, Kind.WALL_PHONE, Direction.SOUTH);

        // The child's room: the bed under the window, curtains either side of it, the toy chest, the clown on the floor.
        LabyrinthBuilder.bed(level, base.offset(BED_FOOT), Direction.NORTH, Blocks.PINK_BED);
        furnish(level, base, -8, 2, -15, Kind.CURTAIN_RIGHT, Direction.EAST);
        furnish(level, base, -8, 2, -12, Kind.CURTAIN_LEFT, Direction.EAST);
        furnish(level, base, -5, 0, -18, Kind.TOY_CHEST, Direction.SOUTH);
        furnish(level, base, -7, 0, -16, Kind.CLOWN_DOLL, Direction.EAST);

        LabyrinthBuilder.entrance(level, base, PANELING, SHAG, POPCORN);
        LabyrinthBuilder.doors(level, base, LabyrinthPlace.MODEL_HOME);
    }

    /**
     * Sets the house for a visit: every chair in its place, the television on or off, the tree where it stands this visit,
     * the window whole or broken, the kid's door shut or standing open.
     */
    public static void stage(ServerLevel level, BlockPos base, int visit) {
        LabyrinthData data = LabyrinthData.get(level.getServer());
        stage(level, base, visit, data.isCompleted(ID), data.state(ID).getBoolean("Unlocked"));
    }

    /** As {@link #stage(ServerLevel, BlockPos, int)}, with the window already broken in or the static's door unlocked. */
    public static void stage(ServerLevel level, BlockPos base, int visit, boolean broken, boolean unlocked) {
        clearStack(level, base);
        clearLimb(level, base);
        for (Chair chair : allChairs()) {
            level.setBlock(base.offset(chair.pos()), chair.state(), LabyrinthBuilder.flags());
        }
        int v = Math.min(FINAL_VISIT, Math.max(1, visit));
        level.setBlock(base.offset(TV), TractHouseBlock.of(v == 2 ? Kind.TV_STATIC : Kind.TV_OFF, Direction.WEST), LabyrinthBuilder.flags());
        yard(level, base, v, v >= FINAL_VISIT && broken);
        setKidsDoor(level, base, v >= FINAL_VISIT || v == 2 && unlocked);
        scene = new Scene();
    }

    private static void yard(ServerLevel level, BlockPos base, int visit, boolean broken) {
        int flags = LabyrinthBuilder.flags();
        for (int x = -22; x <= -10; x++) {
            for (int z = -20; z <= -6; z++) {
                level.setBlock(base.offset(x, -1, z), GRASS, flags);
                for (int y = 0; y <= 9; y++) {
                    level.setBlock(base.offset(x, y, z), Blocks.AIR.defaultBlockState(), flags);
                }
            }
        }
        if (visit >= FINAL_VISIT) {
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
        // Unequal limbs and layered crowns make the same approaching tree recognisable.
        for(int[] limb:new int[][]{{-1,3,-1},{-2,4,-1},{-2,5,-2},{1,4,1},{1,5,2},{0,6,-1}}){
            int x=trunk+limb[0];if(x>=-22&&x<=-10)level.setBlock(base.offset(x,limb[1],TRUNK_Z+limb[2]),log,flags);
        }
        for(int[] crown:new int[][]{{-2,6,-2},{1,7,2},{0,8,-1}})for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++){
            BlockPos at=base.offset(trunk+crown[0]+dx,crown[1],TRUNK_Z+crown[2]+dz);if(at.getX()>=base.getX()-22&&at.getX()<=base.getX()-10&&level.getBlockState(at).isAir())level.setBlock(at,leaves,flags);
        }
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -1; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    int x = trunk + dx;
                    if (dx * dx + dy * dy + dz * dz > 6 || x < -22 || x > -10 || (dx == 0 && dz == 0 && dy <= 0)) {
                        continue;
                    }
                    level.setBlock(base.offset(x, 6 + dy, TRUNK_Z + dz), leaves, flags);
                }
            }
        }
        // A dim light by the tree, as from a streetlamp nobody can see.
        level.setBlock(base.offset(Math.min(-10, trunk + 2), 4, TRUNK_Z + 2),
                Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 7), flags);

        // On the last visit a low branch has come to the glass; the rest of it comes in when someone goes into the room.
        if (visit >= FINAL_VISIT) {
            for (int x = trunk + 1; x <= -10; x++) {
                level.setBlock(base.offset(x, 2, TRUNK_Z), across, flags);
            }
            for (int x = -12; x <= -10; x++) {
                for (int y = 1; y <= 3; y++) {
                    for (int z = TRUNK_Z - 1; z <= TRUNK_Z + 1; z++) {
                        BlockPos at = base.offset(x, y, z);
                        if (level.getBlockState(at).isAir()) {
                            level.setBlock(at, leaves, flags);
                        }
                    }
                }
            }
        }

        // The window: whole, or already broken in by the limb on an earlier reader's visit.
        BlockState pane = Blocks.GLASS_PANE.defaultBlockState()
                .setValue(CrossCollisionBlock.NORTH, true).setValue(CrossCollisionBlock.SOUTH, true);
        for (BlockPos glass : windowPanes()) {
            level.setBlock(base.offset(glass), broken ? Blocks.AIR.defaultBlockState() : pane, flags);
        }
    }

    public static BlockPos[] windowPanes() {
        return new BlockPos[]{WINDOW, WINDOW.above(), WINDOW.north(), WINDOW.north().above()};
    }

    private static void setKidsDoor(ServerLevel level, BlockPos base, boolean open) {
        BlockState door = Blocks.BIRCH_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, Direction.SOUTH)
                .setValue(DoorBlock.HINGE, DoorHingeSide.LEFT)
                .setValue(DoorBlock.OPEN, open);
        level.setBlock(base.offset(KIDS_DOOR), door.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER), LabyrinthBuilder.flags());
        level.setBlock(base.offset(KIDS_DOOR).above(), door.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), LabyrinthBuilder.flags());
    }

    /** The child's room is open once the static has unlocked it on the second visit, and from then on. */
    public static boolean kidsRoomRelevant(LabyrinthData data){int v=visitFor(data);return v>=FINAL_VISIT||v==2&&data.state(ID).getBoolean("Unlocked");}
    public static void onRightClickBlock(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock event){
        if(!(event.getEntity() instanceof ServerPlayer p)||!p.serverLevel().dimension().equals(HouseDimensions.INTERIOR))return;
        BlockPos b=base(p.server);if(b==null)return;BlockPos door=b.offset(KIDS_DOOR);
        if(!event.getPos().equals(door)&&!event.getPos().equals(door.above()))return;
        if(kidsRoomRelevant(LabyrinthData.get(p.server)))return;
        // An old save may put an explorer inside. Never lock their only way out.
        if(p.getX()<b.getX()-1&&p.getZ()<b.getZ()-8)return;
        event.setCanceled(true);event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
        setKidsDoor(p.serverLevel(),b,false);
        p.serverLevel().playSound(null,door,SoundEvents.WOODEN_TRAPDOOR_CLOSE,SoundSource.BLOCKS,.35F,.8F);
        p.displayClientMessage(Component.literal("The handle turns a little way and stops. Locked from inside."),true);
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
        return new AABB(base.getX() - 8, base.getY(), base.getZ() - 18, base.getX() - 1, base.getY() + HEIGHT + 1, base.getZ() - 9);
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

    static Vec3 screen(BlockPos base) {
        return Vec3.atCenterOf(base.offset(TV)).add(-0.35D, 0.1D, 0.0D);
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
            spawnChair(level, pivot, chairs.get(i).state(), i, chairs.size());
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

    /** How far apart the chairs sit in the stack, so that it reaches almost to the ceiling; a chair stands 1.2 high. */
    private static float step(int count) {
        float room = HEIGHT + 1 - 0.1F - 1.07F - 1.2F;
        return count <= 1 ? 0.0F : Math.min(0.7F, room / (count - 1));
    }

    /**
     * Chair {@code i} of the stack, leaning with the whole stack by {@code lean}
     * about the table top: set on the ones below it, turned a quarter more
     * each time, a little off true, and every other one upside down.
     */
    static Transformation chairTransform(int i, int count, Quaternionf lean) {
        float yaw = (float) Math.toRadians(i * 90.0D + (i % 2 == 0 ? 8.0D : -11.0D));
        Vector3f offset = new Vector3f((i % 3 - 1) * 0.07F, i * step(count), ((i + 1) % 3 - 1) * 0.07F).rotate(lean);
        Matrix4f matrix = new Matrix4f()
                .translate(offset)
                .rotate(lean)
                .rotateY(yaw);
        if (i % 2 == 1) {
            matrix.translate(0.0F, 1.2F, 0.0F).rotateZ((float) Math.PI);
        }
        matrix.translate(-0.5F, 0.0F, -0.5F);
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
    private static Display.BlockDisplay spawnDisplay(ServerLevel level, Vec3 at, BlockState state, Transformation transformation, int interpolation,
                                                     float width, float height) {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", "minecraft:block_display");
        tag.put("block_state", NbtUtils.writeBlockState(state));
        tag.put("transformation", encode(transformation));
        tag.putInt("interpolation_duration", interpolation);
        // The box the client culls it by, from its feet up: the whole stack, or the limb's whole sweep.
        tag.putFloat("width", width);
        tag.putFloat("height", height);
        Entity entity = EntityType.loadEntityRecursive(tag, level, created -> {
            created.moveTo(at.x, at.y, at.z, 0.0F, 0.0F);
            return created;
        });
        return entity instanceof Display.BlockDisplay display ? display : null;
    }

    private static void retransform(Display.BlockDisplay display, Transformation transformation, int interpolation) {
        CompoundTag tag = display.saveWithoutId(new CompoundTag());
        tag.put("transformation", encode(transformation));
        tag.putInt("interpolation_duration", interpolation);
        tag.putInt("start_interpolation", 0);
        display.load(tag);
    }

    @Nullable
    private static Display.BlockDisplay spawnChair(ServerLevel level, Vec3 pivot, BlockState state, int index, int count) {
        Display.BlockDisplay display = spawnDisplay(level, pivot, state, chairTransform(index, count, new Quaternionf()), TEETER_INTERVAL, 3.0F, HEIGHT + 1.0F);
        if (display == null) {
            return null;
        }
        display.addTag(TAG);
        display.addTag(INDEX_TAG + index);
        display.addTag(INDEX_TAG + "of_" + count);
        return level.addFreshEntity(display) ? display : null;
    }

    private static int tagNumber(Display display, String prefix, int fallback) {
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
            retransform(display, chairTransform(index, count, lean), TEETER_INTERVAL);
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
    // The limb

    /** Where the limb comes from: the low branch outside the child's window. */
    public static Vec3 limbRoot(BlockPos base) {
        return new Vec3(base.getX() - 10.6D, base.getY() + 2.45D, base.getZ() + TRUNK_Z + 0.0D);
    }

    /** Where the limb's displays stand: under the root, on the floor, so their culling box holds the whole reach. */
    private static Vec3 limbAnchor(BlockPos base) {
        return new Vec3(limbRoot(base).x, base.getY(), limbRoot(base).z);
    }

    public static List<Display.BlockDisplay> limbDisplays(ServerLevel level, BlockPos base) {
        return level.getEntitiesOfClass(Display.BlockDisplay.class, house(base).inflate(16.0D), display -> display.getTags().contains(LIMB_TAG));
    }

    private static void clearLimb(ServerLevel level, BlockPos base) {
        limbDisplays(level, base).forEach(Entity::discard);
    }

    /**
     * The limb's spine at {@code reach} (0 outside, 1 at full stretch) toward {@code target}: a curve from the branch out
     * of the window, bowing up a little, reaching no further than {@link #LIMB_REACH}, with a slow sideways sway.
     */
    public static Vec3[] limbSpine(Vec3 root, Vec3 target, double reach, double time) {
        Vec3 to = target.subtract(root);
        double length = Math.min(LIMB_REACH, to.length()) * Math.max(0.05D, reach);
        Vec3 tip = root.add(to.normalize().scale(length));
        Vec3 side = new Vec3(-to.z, 0, to.x).normalize();
        Vec3 control = root.add(tip).scale(0.5D).add(0, 0.35D * reach, 0).add(side.scale(Math.sin(time * 0.09D) * 0.25D * reach));
        Vec3[] points = new Vec3[LIMB_SEGMENTS + 1];
        for (int i = 0; i <= LIMB_SEGMENTS; i++) {
            double s = i / (double) LIMB_SEGMENTS, u = 1 - s;
            points[i] = root.scale(u * u).add(control.scale(2 * u * s)).add(tip.scale(s * s));
        }
        return points;
    }

    /** One segment of bark from {@code from} to {@code to}, {@code width} thick, relative to the display's own position {@code root}. */
    static Transformation segment(Vec3 root, Vec3 from, Vec3 to, float width) {
        Vector3f dir = new Vector3f((float) (to.x - from.x), (float) (to.y - from.y), (float) (to.z - from.z));
        float length = Math.max(0.05F, dir.length());
        Quaternionf turn = new Quaternionf().rotationTo(new Vector3f(0, 0, 1), dir.normalize());
        Vector3f centre = new Vector3f(-width / 2, -width / 2, 0).rotate(turn);
        Vector3f translation = new Vector3f((float) (from.x - root.x), (float) (from.y - root.y), (float) (from.z - root.z)).add(centre);
        return new Transformation(translation, turn, new Vector3f(width, width, length * 1.08F), null);
    }

    private static void spawnLimb(ServerLevel level, BlockPos base) {
        clearLimb(level, base);
        Vec3 anchor = limbAnchor(base);
        BlockState bark = Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z);
        for (int i = 0; i <= LIMB_SEGMENTS + 1; i++) {
            BlockState state = i >= LIMB_SEGMENTS ? Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true) : bark;
            Display.BlockDisplay display = spawnDisplay(level, anchor, state, new Transformation(null, null, new Vector3f(0.01F), null), 2,
                    (float) LIMB_REACH * 2.0F + 2.0F, HEIGHT + 1.0F);
            if (display == null) continue;
            display.addTag(LIMB_TAG);
            display.addTag(LIMB_TAG + "_" + i);
            level.addFreshEntity(display);
        }
    }

    private static void poseLimb(ServerLevel level, BlockPos base, double reach, double time) {
        Vec3 root = limbRoot(base), anchor = limbAnchor(base);
        Vec3[] spine = limbSpine(root, scene.limbTarget, reach, time);
        for (Display.BlockDisplay display : limbDisplays(level, base)) {
            int i = tagNumber(display, LIMB_TAG + "_", -1);
            if (i < 0) continue;
            Transformation t;
            if (i < LIMB_SEGMENTS) {
                float width = (float) (0.36D - 0.26D * i / LIMB_SEGMENTS);
                t = segment(anchor, spine[i], spine[i + 1], width);
            } else {
                // Two clumps of leaves at the end, as on a branch torn in through glass.
                Vec3 tip = spine[LIMB_SEGMENTS];
                float size = (float) (0.55D * Math.max(0.2D, reach));
                Vec3 at = tip.subtract(anchor).add(i == LIMB_SEGMENTS ? -size / 2 : -size / 3, -size / 2 + (i == LIMB_SEGMENTS ? 0 : 0.2D), -size / 2);
                t = new Transformation(new Vector3f((float) at.x, (float) at.y, (float) at.z), null, new Vector3f(size), null);
            }
            retransform(display, t, 2);
        }
    }

    /** The glass comes in and the limb starts through, toward whoever stepped into the room. */
    public static void breakIn(ServerLevel level, BlockPos base, ServerPlayer toward) {
        boolean glassBroke = false;
        for (BlockPos glass : windowPanes()) {
            BlockPos at = base.offset(glass);
            BlockState state = level.getBlockState(at);
            if (!state.isAir()) {
                level.levelEvent(2001, at, Block.getId(state));
                level.setBlock(at, Blocks.AIR.defaultBlockState(), LabyrinthBuilder.flags());
                glassBroke = true;
            }
        }
        if (glassBroke) {
            level.playSound(null, BlockPos.containing(window(base)), SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1.0F, 0.7F);
        }
        level.playSound(null, BlockPos.containing(window(base)), SoundEvents.AZALEA_LEAVES_BREAK, SoundSource.BLOCKS, 1.0F, 0.6F);
        scene.limbAge = 0;
        scene.limbEmpty = 0;
        scene.limbTarget = toward.getEyePosition().subtract(0, 0.5D, 0);
        spawnLimb(level, base);
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
        if(!kidsRoomRelevant(data)&&server.getTickCount()%20==0&&visitors.stream().noneMatch(p->p.getX()-base.getX()<-1&&p.getZ()-base.getZ()<-8)){
            for(var at:java.util.List.of(base.offset(KIDS_DOOR),base.offset(KIDS_DOOR).above())){var door=level.getBlockState(at);if(door.getBlock() instanceof DoorBlock&&door.getValue(DoorBlock.OPEN))level.setBlock(at,door.setValue(DoorBlock.OPEN,false),LabyrinthBuilder.flags());}
        }
        if (visitors.isEmpty()) {
            if (scene.stacked) {
                unstack(level, base, visit);
            }
            if (scene.limbAge >= 0) {
                clearLimb(level, base);
                scene.limbAge = -1;
            }
            return;
        }
        if (server.getTickCount() % 20 == 0) {
            checkBinder(level, base, data);
        }
        switch (visit) {
            case 1 -> tickChairs(level, base, data, visit, visitors);
            case 2 -> tickTelevision(level, base, data, visitors);
            default -> tickLastVisit(level, base, data, visitors);
        }
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
                beat(data, visitors);
                TheOldestHouse.LOGGER.info("The chairs in the model home were stacked on visit {}.", visit);
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

    private static void beat(LabyrinthData data, List<ServerPlayer> visitors) {
        CompoundTag state = data.state(ID);
        if (state.getBoolean("BeatDone")) {
            return;
        }
        state.putBoolean("BeatDone", true);
        data.setState(ID, state);
        for(ServerPlayer visitor:visitors)visitor.displayClientMessage(Component.literal("You saw it. ").append(Component.literal("The doorway will remember this when you leave and return.").withStyle(net.minecraft.ChatFormatting.GRAY,net.minecraft.ChatFormatting.ITALIC)),false);
    }

    /** The second visit: the set hisses; three seconds of watching it, and the child's door comes open behind the watcher. */
    private static void tickTelevision(ServerLevel level, BlockPos base, LabyrinthData data, List<ServerPlayer> visitors) {
        long now = level.getGameTime();
        if (now >= scene.nextHiss) {
            level.playSound(null, base.offset(TV), STATIC.value(), SoundSource.BLOCKS, 0.8F, 0.85F + level.getRandom().nextFloat() * 0.1F);
            scene.nextHiss = now + 60;
        }
        CompoundTag state = data.state(ID);
        if (state.getBoolean("Unlocked")) {
            return;
        }
        Vec3 screen = screen(base);
        if (visitors.stream().anyMatch(p -> p.getEyePosition().distanceToSqr(screen) < 100.0D && HouseWatchers.sees(p, screen))) {
            scene.watchedStatic++;
        }
        if (scene.watchedStatic < STATIC_WATCH) {
            return;
        }
        state.putBoolean("Unlocked", true);
        data.setState(ID, state);
        setKidsDoor(level, base, true);
        level.playSound(null, base.offset(KIDS_DOOR), SoundEvents.WOODEN_DOOR_OPEN, SoundSource.BLOCKS, 0.9F, 0.55F);
        for (ServerPlayer visitor : visitors) {
            visitor.displayClientMessage(Component.literal("Behind you, down the hall, a door has come open."), false);
        }
        beat(data, visitors);
        TheOldestHouse.LOGGER.info("The static in the model home opened the child's door.");
    }

    /** The third visit: taps at the glass; then, for whoever goes into the child's room, the limb. */
    private static void tickLastVisit(ServerLevel level, BlockPos base, LabyrinthData data, List<ServerPlayer> visitors) {
        AABB kids = kidsRoom(base);
        ServerPlayer inside = null;
        for (ServerPlayer player : visitors) {
            if (kids.contains(player.position()) && (inside == null || player.position().distanceToSqr(window(base)) < inside.position().distanceToSqr(window(base)))) {
                inside = player;
            }
        }
        long now = level.getGameTime();
        if (scene.limbAge < 0) {
            if (inside == null) {
                if (now >= scene.nextTap && visitors.stream().anyMatch(p -> p.position().distanceToSqr(window(base)) < 144.0D)) {
                    level.playSound(null, BlockPos.containing(window(base)), SoundEvents.GLASS_HIT, SoundSource.BLOCKS, 0.7F, 0.6F + level.getRandom().nextFloat() * 0.2F);
                    scene.nextTap = now + 40 + level.getRandom().nextInt(80);
                }
                return;
            }
            breakIn(level, base, inside);
        }
        scene.limbAge += 2;
        if (inside != null) {
            scene.limbEmpty = 0;
            // It follows whoever it is reaching for, slowly.
            Vec3 want = inside.getEyePosition().subtract(0, 0.5D, 0);
            scene.limbTarget = scene.limbTarget.add(want.subtract(scene.limbTarget).scale(0.15D));
        } else {
            scene.limbEmpty += 2;
        }
        double reach = Math.min(1.0D, scene.limbAge / (double) LIMB_GROW);
        reach = 1 - (1 - reach) * (1 - reach);
        if (scene.limbEmpty > 0) {
            reach *= Math.max(0.0D, 1.0D - scene.limbEmpty / 60.0D);
        }
        if (scene.limbEmpty >= 60) {
            // Nobody in the room for three seconds: it has gone back out; the next one in sets it off again.
            clearLimb(level, base);
            scene.limbAge = -1;
            return;
        }
        poseLimb(level, base, reach, now);
        if (scene.limbAge < 20) {
            return;
        }
        Vec3 tip = limbSpine(limbRoot(base), scene.limbTarget, reach, now)[LIMB_SEGMENTS];
        for (ServerPlayer player : visitors) {
            if (!WitnessAccount.has(data,player.getUUID(),WitnessAccount.Story.MODEL_HOME)&&kids.contains(player.position()) && (HouseWatchers.sees(player, tip) || HouseWatchers.sees(player, window(base)))) {
                CompoundTag state = data.state(ID);
                state.putBoolean("BeatDone", true);
                data.setState(ID, state);
                data.setCompleted(ID, true);
                WitnessAccount.resolve(player,WitnessAccount.Story.MODEL_HOME,"saw_tree");
                level.playSound(null, BlockPos.containing(window(base)), SoundEvents.AZALEA_LEAVES_STEP, SoundSource.BLOCKS, 0.8F, 0.7F);
                TheOldestHouse.LOGGER.info("{} saw the tree reach into the kid's room; the model home is finished.", player.getGameProfile().getName());
                player.displayClientMessage(Component.literal("The tree has come in through the window, reaching for you. This visit is finished; the doorway is behind you."),false);
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
        state.remove("Unlocked");
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
