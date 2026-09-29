package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseCalendar;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseLayout;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.opening.Doorsteps;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.PistonEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Hill House's Red Room. Recurring temptation; verb: recognizing your room.
 *
 * A copy of the room the player spends the most time in at home, rebuilt
 * deep inside. They walk through a door in the gray and it is their room,
 * entered through its own doorway, the way they always come in. Every
 * container is empty, the redstone answers nothing, and anything bigger than
 * a modest room is cut off at the walls of the slot. Its windows look onto
 * white.
 *
 * Its bed works, one of the exceptions past the labyrinth threshold, though
 * only at night and without setting a spawn point. Sleep in it and time is
 * lost (a day or two, if the player is alone in the world; hunger either way)
 * and they wake somewhere deeper, with the way back leading first to the
 * bed.
 *
 * How a room is chosen: see {@link HomeRooms}. It is copied while the player
 * is standing in it, so nothing ever has to load.
 */
public final class RedRoom {
    public static final String ID = "red_room";

    private static final int SAMPLE_TICKS = 200;
    private static final int SAMPLE_SECONDS = SAMPLE_TICKS / 20;
    /** A spot counts as home within this far of the bed, horizontally and vertically. */
    private static final int HOME_RADIUS = 40;
    private static final int HOME_HEIGHT = 16;
    /** Time in a spot before its room is worth copying. */
    private static final int MIN_SECONDS = 120;

    /** How far from where the player stands a room may reach: 13 across inside, 6 high. */
    private static final int REACH = 6;
    private static final int LOW = -1;
    private static final int HIGH = 5;
    private static final int MAX_FILL = 1400;

    private RedRoom() {
    }

    // ------------------------------------------------------------------
    // Copying a room

    /** Where a player could stand or walk: nothing to bump into above ankle height, and not a gate or a door. */
    public static boolean isOpen(BlockGetter level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof DoorBlock || block instanceof FenceGateBlock) {
            return false;
        }
        VoxelShape shape = state.getCollisionShape(level, pos);
        return shape.isEmpty() || shape.max(Direction.Axis.Y) <= 0.5D;
    }

    private record Doorway(BlockPos pos, Direction out, boolean door) {
    }

    /**
     * Copies the room around {@code feet}: the open space reachable from
     * there (capped), with its walls, floor and ceiling, turned so its door
     * (or, with none, a gap cut in the wall nearest the middle of a side)
     * lies at local (0, 0, 0) facing south. Returns null where there is no
     * room to speak of.
     */
    @Nullable
    public static RoomSnapshot capture(Level level, BlockPos feet, long stamp, long day, long cell) {
        BlockPos anchor = isOpen(level, feet) ? feet.immutable()
                : isOpen(level, feet.above()) ? feet.above().immutable() : null;
        if (anchor == null) {
            return null;
        }
        Set<BlockPos> fill = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        fill.add(anchor);
        queue.add(anchor);
        while (!queue.isEmpty() && fill.size() < MAX_FILL) {
            BlockPos c = queue.poll();
            for (Direction d : Direction.values()) {
                BlockPos n = c.relative(d);
                if (!fill.contains(n) && withinReach(anchor, n) && isOpen(level, n)) {
                    fill.add(n);
                    queue.add(n);
                }
            }
        }

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (BlockPos p : fill) {
            minX = Math.min(minX, p.getX());
            minY = Math.min(minY, p.getY());
            minZ = Math.min(minZ, p.getZ());
            maxX = Math.max(maxX, p.getX());
            maxY = Math.max(maxY, p.getY());
            maxZ = Math.max(maxZ, p.getZ());
        }
        BoundingBox box = new BoundingBox(minX - 1, minY - 1, minZ - 1, maxX + 1, maxY + 1, maxZ + 1);

        Doorway doorway = findDoor(level, fill, box, anchor);
        if (doorway == null) {
            doorway = findOpening(level, fill, box, anchor);
        }
        if (doorway == null) {
            return null;
        }
        Rotation turn = LabyrinthDoors.rotationFrom(doorway.out(), Direction.SOUTH);

        // The copy's extent, turned, and cut to what the Red Room can hold.
        BoundingBox limit = LabyrinthPlace.RED_ROOM.room();
        int lx0 = Integer.MAX_VALUE, ly0 = Integer.MAX_VALUE, lz0 = Integer.MAX_VALUE;
        int lx1 = Integer.MIN_VALUE, ly1 = Integer.MIN_VALUE, lz1 = Integer.MIN_VALUE;
        for (int cx : new int[]{box.minX(), box.maxX()}) {
            for (int cy : new int[]{box.minY(), box.maxY()}) {
                for (int cz : new int[]{box.minZ(), box.maxZ()}) {
                    BlockPos l = new BlockPos(cx, cy, cz).subtract(doorway.pos()).rotate(turn);
                    lx0 = Math.min(lx0, l.getX());
                    ly0 = Math.min(ly0, l.getY());
                    lz0 = Math.min(lz0, l.getZ());
                    lx1 = Math.max(lx1, l.getX());
                    ly1 = Math.max(ly1, l.getY());
                    lz1 = Math.max(lz1, l.getZ());
                }
            }
        }
        BlockPos localMin = new BlockPos(Math.max(lx0, limit.minX()), Math.max(ly0, limit.minY()), Math.max(lz0, limit.minZ()));
        BlockPos localMax = new BlockPos(Math.min(lx1, limit.maxX()), Math.min(ly1, limit.maxY()), Math.min(lz1, limit.maxZ()));
        if (localMin.getX() > localMax.getX() || localMin.getY() > localMax.getY() || localMin.getZ() > localMax.getZ()
                || localMin.getY() > -1 || localMax.getY() < 1 || localMax.getZ() != 0) {
            return null;
        }

        RoomSnapshot.Builder copy = new RoomSnapshot.Builder(localMin, localMax, LabyrinthBuilder.SOLID);
        BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
        for (int x = box.minX(); x <= box.maxX(); x++) {
            for (int y = box.minY(); y <= box.maxY(); y++) {
                for (int z = box.minZ(); z <= box.maxZ(); z++) {
                    at.set(x, y, z);
                    BlockPos local = at.subtract(doorway.pos()).rotate(turn);
                    if (!copy.contains(local)) {
                        continue;
                    }
                    BlockState state = level.getBlockState(at);
                    CompoundTag data = null;
                    BlockEntity blockEntity = level.getBlockEntity(at);
                    if (blockEntity != null && keepsData(blockEntity)) {
                        data = blockEntity.saveWithoutMetadata(level.registryAccess());
                    }
                    copy.set(local, state.rotate(turn), data);
                }
            }
        }

        // Their own door stands open in its doorway; with none, the gap is cut.
        if (doorway.door()) {
            for (int y = 0; y <= 1; y++) {
                BlockPos local = new BlockPos(0, y, 0);
                BlockState state = copy.get(local);
                if (state != null && state.getBlock() instanceof DoorBlock) {
                    copy.set(local, state.setValue(DoorBlock.OPEN, true), null);
                }
            }
        } else {
            copy.set(BlockPos.ZERO, Blocks.AIR.defaultBlockState(), null);
            copy.set(new BlockPos(0, 1, 0), Blocks.AIR.defaultBlockState(), null);
        }
        return copy.build(stamp, day, cell);
    }

    private static boolean withinReach(BlockPos anchor, BlockPos pos) {
        int dy = pos.getY() - anchor.getY();
        return Math.abs(pos.getX() - anchor.getX()) <= REACH && Math.abs(pos.getZ() - anchor.getZ()) <= REACH
                && dy >= LOW && dy <= HIGH;
    }

    /** Only data that cannot hold items or run anything: writing, patterns, heads. */
    private static boolean keepsData(BlockEntity blockEntity) {
        return blockEntity instanceof SignBlockEntity
                || blockEntity instanceof BannerBlockEntity
                || blockEntity instanceof SkullBlockEntity;
    }

    /** Whether {@code pos} lies in the wall on the {@code out} side of the box. */
    private static boolean onFace(BoundingBox box, BlockPos pos, Direction out) {
        return switch (out) {
            case EAST -> pos.getX() == box.maxX();
            case WEST -> pos.getX() == box.minX();
            case SOUTH -> pos.getZ() == box.maxZ();
            case NORTH -> pos.getZ() == box.minZ();
            default -> false;
        };
    }

    /** The door in the room's walls nearest where the player stood. */
    @Nullable
    private static Doorway findDoor(Level level, Set<BlockPos> fill, BoundingBox box, BlockPos anchor) {
        Doorway best = null;
        long bestDistance = Long.MAX_VALUE;
        for (BlockPos c : fill) {
            for (Direction d : Direction.Plane.HORIZONTAL) {
                BlockPos n = c.relative(d);
                if (!onFace(box, n, d)) {
                    continue;
                }
                BlockState state = level.getBlockState(n);
                if (!(state.getBlock() instanceof DoorBlock)) {
                    continue;
                }
                BlockPos lower = state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER ? n : n.below();
                long dx = lower.getX() - anchor.getX();
                long dz = lower.getZ() - anchor.getZ();
                long distance = dx * dx + dz * dz;
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = new Doorway(lower.immutable(), d, true);
                }
            }
        }
        return best;
    }

    /** With no door: a gap two high in a wall at floor level, as near the middle of a side as it can be. */
    @Nullable
    private static Doorway findOpening(Level level, Set<BlockPos> fill, BoundingBox box, BlockPos anchor) {
        Doorway best = null;
        double bestScore = Double.MAX_VALUE;
        for (BlockPos c : fill) {
            if (c.getY() != anchor.getY() || !fill.contains(c.above())) {
                continue;
            }
            for (Direction d : Direction.Plane.HORIZONTAL) {
                BlockPos n = c.relative(d);
                if (!onFace(box, n, d)) {
                    continue;
                }
                double score = d.getAxis() == Direction.Axis.X
                        ? Math.abs(n.getZ() - (box.minZ() + box.maxZ()) / 2.0D)
                        : Math.abs(n.getX() - (box.minX() + box.maxX()) / 2.0D);
                if (score < bestScore) {
                    bestScore = score;
                    best = new Doorway(n.immutable(), d, false);
                }
            }
        }
        return best;
    }

    // ------------------------------------------------------------------
    // Standing it in its slot

    @Nullable
    static BlockPos base(MinecraftServer server) {
        BlockPos origin = HouseSavedData.get(server).houseOrigin();
        return origin == null ? null : LabyrinthPlaces.base(origin, LabyrinthPlace.RED_ROOM);
    }

    /** Whether {@code pos} lies in the Red Room itself (not its vestibule). */
    public static boolean isInRoom(BlockPos base, BlockPos pos) {
        BoundingBox room = LabyrinthPlace.RED_ROOM.room();
        return room != null && room.isInside(pos.subtract(base));
    }

    public static boolean isInRoom(Level level, BlockPos pos) {
        if (!level.dimension().equals(HouseDimensions.INTERIOR) || level.getServer() == null) {
            return false;
        }
        BlockPos base = base(level.getServer());
        return base != null && isInRoom(base, pos);
    }

    /**
     * Stands a copy in the Red Room's slot (or, with none, a bare little room
     * so its door has somewhere to open), with its entry door behind the
     * copy's own doorway. Whatever was there before goes back to solid.
     */
    public static void place(ServerLevel level, BlockPos base, @Nullable RoomSnapshot snapshot) {
        BoundingBox box = LabyrinthPlace.RED_ROOM.room();
        int flags = LabyrinthBuilder.flags();
        List<BlockPos> withData = new ArrayList<>();
        BlockPos.MutableBlockPos local = new BlockPos.MutableBlockPos();
        for (int x = box.minX(); x <= box.maxX(); x++) {
            for (int y = box.minY(); y <= box.maxY(); y++) {
                for (int z = box.minZ(); z <= box.maxZ(); z++) {
                    local.set(x, y, z);
                    BlockState state = snapshot == null ? null : snapshot.stateAt(local);
                    if (state == null) {
                        state = LabyrinthBuilder.SOLID;
                    }
                    BlockPos to = base.offset(x, y, z);
                    if (level.getBlockState(to) != state) {
                        level.setBlock(to, state, flags);
                    }
                    if (snapshot != null && snapshot.blockEntityAt(local) != null) {
                        withData.add(local.immutable());
                    }
                }
            }
        }
        for (BlockPos l : withData) {
            BlockPos to = base.offset(l);
            BlockEntity blockEntity = level.getBlockEntity(to);
            CompoundTag data = snapshot.blockEntityAt(l);
            if (blockEntity != null && data != null) {
                blockEntity.loadWithComponents(data, level.registryAccess());
                blockEntity.setChanged();
                BlockState state = level.getBlockState(to);
                level.sendBlockUpdated(to, state, state, Block.UPDATE_CLIENTS);
            }
        }

        BlockState wall = Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState();
        BlockState floor = Blocks.SMOOTH_STONE.defaultBlockState();
        BlockState ceiling = Blocks.STONE.defaultBlockState();
        if (snapshot == null) {
            LabyrinthBuilder.room(level, base, -2, 2, 2, -4, -1, wall, floor, ceiling);
        }
        LabyrinthBuilder.entrance(level, base, wall, floor, ceiling, snapshot == null);
        LabyrinthBuilder.doors(level, base, LabyrinthPlace.RED_ROOM);
    }

    /** Called by the builder after the slot is filled solid: stands whatever copy stood there, or the latest. */
    static void build(MinecraftServer server, ServerLevel level, BlockPos base) {
        HomeRooms rooms = HomeRooms.get(server);
        HomeRooms.Choice choice = rooms.placed();
        if (choice == null) {
            choice = rooms.latest();
        }
        place(level, base, choice == null ? null : choice.snapshot());
        rooms.setPlaced(choice);
    }

    /**
     * Makes the Red Room this player's before they go in: their own room if
     * it has been copied, else the latest anyone's. Left as it is if someone
     * is inside, or something has been left in one of its containers (the
     * house keeps the room you left something in). False when there is no
     * room to show at all.
     */
    public static boolean prepare(ServerPlayer player) {
        MinecraftServer server = player.server;
        HomeRooms rooms = HomeRooms.get(server);
        HomeRooms.Choice choice = rooms.choiceFor(player.getUUID());
        if (choice == null) {
            return false;
        }
        if (rooms.isPlaced(choice)) {
            return true;
        }
        ServerLevel level = server.getLevel(HouseDimensions.INTERIOR);
        BlockPos base = base(server);
        if (level == null || base == null) {
            return false;
        }
        boolean standing = rooms.placed() != null;
        if (standing && (anyoneInside(level, base) || holdsAnything(level, base))) {
            return true;
        }
        place(level, base, choice.snapshot());
        rooms.setPlaced(choice);
        TheOldestHouse.LOGGER.info("Stood a copy of {}'s room in the Red Room.", choice.owner());
        return true;
    }

    /** After a place is dealt: if one of its doors now leads to the Red Room, make it this player's. */
    static void prepareIfDealt(ServerPlayer player, LabyrinthPlace place) {
        LabyrinthData data = LabyrinthData.get(player.server);
        for (LabyrinthPlace.DoorSpec spec : place.doors()) {
            LabyrinthData.Door door = data.door(place.doorId(spec));
            LabyrinthData.Deal dealt = door == null ? null : data.deal(player.getUUID(), door);
            if (door != null && LabyrinthData.DEALT.equals(door.destination)
                    && dealt != null && ID.equals(dealt.place())) {
                prepare(player);
                return;
            }
        }
    }

    private static boolean anyoneInside(ServerLevel level, BlockPos base) {
        for (ServerPlayer player : level.players()) {
            if (isInRoom(base, player.blockPosition())) {
                return true;
            }
        }
        return false;
    }

    /** Whether any container in the Red Room has something in it. */
    static boolean holdsAnything(ServerLevel level, BlockPos base) {
        BoundingBox box = LabyrinthPlace.RED_ROOM.room();
        for (int x = box.minX(); x <= box.maxX(); x++) {
            for (int y = box.minY(); y <= box.maxY(); y++) {
                for (int z = box.minZ(); z <= box.maxZ(); z++) {
                    if (level.getBlockEntity(base.offset(x, y, z)) instanceof Container container && !container.isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // ------------------------------------------------------------------
    // Watching where players live, and the bed

    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % SAMPLE_TICKS == 0) {
            sample(server);
        }
        wakeSleepers(server);
    }

    private static void sample(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        if (overworld.players().isEmpty()) {
            return;
        }
        HomeRooms rooms = HomeRooms.get(server);
        BlockPos manor = HouseSavedData.get(server).houseOrigin();
        long today = HouseCalendar.today(server);
        for (ServerPlayer player : overworld.players()) {
            if (player.isSpectator()) {
                continue;
            }
            BlockPos bed = Doorsteps.bedPosition(player).orElse(null);
            BlockPos feet = player.blockPosition();
            if (bed == null || !isAtHome(overworld, feet, bed, manor)) {
                continue;
            }
            rooms.record(player.getUUID(), feet, SAMPLE_SECONDS);
            Map.Entry<Long, HomeRooms.Cell> top = rooms.topCell(player.getUUID());
            long here = HomeRooms.cellOf(feet);
            if (top != null && top.getKey() == here && top.getValue().seconds() >= MIN_SECONDS
                    && rooms.needsCapture(player.getUUID(), here, today)) {
                captureHere(player);
            }
        }
    }

    /** Indoors (no sky overhead), near the bed, and not in the Navidsons' house. */
    static boolean isAtHome(Level level, BlockPos pos, BlockPos bed, @Nullable BlockPos manor) {
        int dx = pos.getX() - bed.getX();
        int dz = pos.getZ() - bed.getZ();
        if (dx * dx + dz * dz > HOME_RADIUS * HOME_RADIUS || Math.abs(pos.getY() - bed.getY()) > HOME_HEIGHT) {
            return false;
        }
        if (manor != null) {
            int rx = pos.getX() - manor.getX();
            int rz = pos.getZ() - manor.getZ();
            if (rx >= HouseLayout.MIN_X - 2 && rx <= HouseLayout.MAX_X + 2 && rz >= HouseLayout.MIN_Z - 2 && rz <= HouseLayout.MAX_Z + 2) {
                return false;
            }
        }
        return !level.canSeeSky(pos);
    }

    /** Copies the room the player stands in as theirs. */
    public static boolean captureHere(ServerPlayer player) {
        MinecraftServer server = player.server;
        BlockPos feet = player.blockPosition();
        RoomSnapshot snapshot = capture(player.level(), feet, server.overworld().getGameTime(),
                HouseCalendar.today(server), HomeRooms.cellOf(feet));
        if (snapshot == null) {
            return false;
        }
        HomeRooms.get(server).setSnapshot(player.getUUID(), snapshot);
        LabyrinthData.get(server).setReady(ID, true);
        TheOldestHouse.LOGGER.info("Copied {}'s room ({} blocks) for the Red Room.",
                player.getGameProfile().getName(), snapshot.blockCount());
        return true;
    }

    private static void wakeSleepers(MinecraftServer server) {
        ServerLevel level = server.getLevel(HouseDimensions.INTERIOR);
        if (level == null || level.players().isEmpty()) {
            return;
        }
        BlockPos base = base(server);
        if (base == null) {
            return;
        }
        for (ServerPlayer player : List.copyOf(level.players())) {
            if (player.isSleepingLongEnough() && player.getSleepingPos().map(pos -> isInRoom(base, pos)).orElse(false)) {
                int lost = loseTime(player);
                LabyrinthDoors.wakeDeeper(player);
                TheOldestHouse.LOGGER.info("{} slept in the Red Room and lost {} day(s).", player.getGameProfile().getName(), lost);
            }
        }
    }

    /**
     * A day or two gone: the world's clock jumps (only for a player alone in
     * the world, since everyone shares it) and they wake hungry.
     */
    private static int loseTime(ServerPlayer player) {
        int lost = 1 + player.getRandom().nextInt(2);
        MinecraftServer server = player.server;
        if (server.getPlayerCount() <= 1) {
            ServerLevel overworld = server.overworld();
            long next = (overworld.getDayTime() / HouseCalendar.TICKS_PER_DAY + 1 + lost) * HouseCalendar.TICKS_PER_DAY;
            overworld.setDayTime(next);
        }
        FoodData food = player.getFoodData();
        food.setFoodLevel(Math.max(4, food.getFoodLevel() - 4 * lost));
        food.setSaturation(0.0F);
        return lost;
    }

    /** Beds in the Red Room are the exception to the labyrinth's rule. */
    public static boolean isRedRoomBed(Level level, BlockPos pos) {
        return isInRoom(level, pos);
    }

    // ------------------------------------------------------------------
    // Nothing in it runs

    /** Doors, beds, containers and crafting tables work; nothing else answers a hand. */
    static boolean isUsable(Level level, BlockPos pos, BlockState state) {
        Block block = state.getBlock();
        if (block instanceof DoorBlock || block instanceof TrapDoorBlock || block instanceof FenceGateBlock
                || block instanceof BedBlock || block instanceof CraftingTableBlock) {
            return true;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof BaseContainerBlockEntity || blockEntity instanceof EnderChestBlockEntity;
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide() || !isInRoom(level, event.getPos())) {
            return;
        }
        if (!isUsable(level, event.getPos(), level.getBlockState(event.getPos()))) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    /** Redstone is disabled: nothing in the room tells its neighbours it changed. */
    public static void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
        if (event.getLevel() instanceof Level level && !level.isClientSide() && isInRoom(level, event.getPos())) {
            event.setCanceled(true);
        }
    }

    public static void onPiston(PistonEvent.Pre event) {
        if (event.getLevel() instanceof Level level && !level.isClientSide() && isInRoom(level, event.getPos())) {
            event.setCanceled(true);
        }
    }

    // ------------------------------------------------------------------
    // Status

    public static List<String> describe(MinecraftServer server, @Nullable ServerPlayer viewer) {
        List<String> lines = new ArrayList<>();
        HomeRooms rooms = HomeRooms.get(server);
        HomeRooms.Choice placed = rooms.placed();
        String standing;
        if (placed == null) {
            standing = "a bare room (no one's room has been copied yet)";
        } else {
            UUID owner = placed.owner();
            ServerPlayer ownerPlayer = server.getPlayerList().getPlayer(owner);
            String who = viewer != null && owner.equals(viewer.getUUID()) ? "your"
                    : (ownerPlayer != null ? ownerPlayer.getGameProfile().getName() : owner.toString()) + "'s";
            standing = "a copy of " + who + " room, taken on day " + placed.snapshot().day();
        }
        lines.add("Red Room: " + standing + "; "
                + (LabyrinthData.get(server).isReady(ID) ? "the dealer can deal it." : "not dealt until a room is copied."));
        if (viewer != null) {
            lines.addAll(rooms.describe(viewer.getUUID()));
            RoomSnapshot own = rooms.snapshot(viewer.getUUID());
            lines.add(own == null
                    ? "Your room has not been copied (it is, after two minutes in the spot you spend the most time in)."
                    : "Your room was last copied on day " + own.day() + ".");
        }
        return lines;
    }
}
