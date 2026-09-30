package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseBlocks;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.WardrobeBlock;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.house.HouseLayout;
import io.github.knaitoe.theoldesthouse.network.ClapGamePayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.portal.DimensionTransition;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerRespawnPositionEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * The Conjuring: hide-and-clap. One-shot; verb: following claps blindfolded.
 *
 * A child's bedroom with open floor. On the wall by the bed, over its post,
 * hang a blindfold and a note: put it on, count to ten, follow the claps,
 * open the wardrobe while still blindfolded. Put the blindfold on and a
 * child counts to ten. Then three or four claps come from open spots in the
 * room; get within a block or two of one and the next sounds somewhere
 * else, wander off and the current one comes again, louder. At the third,
 * small bare feet step away through the strip under the blindfold. After
 * the last, the house reads which way the player faces and puts a wardrobe
 * two blocks directly behind them (or the nearest open spot within 45
 * degrees), and two claps come from inside it. Inside: a crayon drawing of
 * someone in a blindfold, drawn from exactly where the wardrobe stands.
 *
 * Entering locks the room. The cloth binds until its owner opens the wardrobe.
 * A minute after equipping it, the girl finishes an unfinished game with a
 * brief approach and a fatal camera twist. Items drop normally; the player
 * respawns in the manor outside the impossible hallway. The round resets.
 *
 * The room lies north of its entry door (see {@link LabyrinthPlace}):
 * inside, x -5..5, z -12..-1, three high.
 */
public final class HideAndClap {
    public static final String ID = "hide_and_clap";

    public static final BlockPos BED_FOOT = new BlockPos(-4, 0, -11);
    public static final BlockPos BED_POST = new BlockPos(-3, 0, -12);
    public static final BlockPos BLINDFOLD_FRAME = new BlockPos(-3, 1, -12);
    public static final BlockPos NOTE_FRAME = new BlockPos(-4, 1, -12);

    private static final ResourceLocation CLAP = sound("vignette.clap");
    private static final ResourceLocation CLAP_MUFFLED = sound("vignette.clap_muffled");
    private static final ResourceLocation SMALL_STEPS = sound("vignette.small_steps");
    private static final String FEET_TAG = TheOldestHouse.MOD_ID + "_feet";

    private static final int COUNT_INTERVAL = 20;
    private static final int CLAP_INTERVAL = 50;
    private static final double FOUND_DISTANCE = 1.6D;
    private static final int FEET_TICKS = 30;
    private static final String[] COUNT = {"one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten"};

    private static final String BOUND_TO = "ClapBoundTo";
    private enum Stage { WAITING, COUNTING, CLAPPING, WARDROBE, ENDING }

    private static final class Game {
        final UUID player;
        final ClapGameClock clock;
        Stage stage = Stage.WAITING;
        int count;
        long next;
        final List<BlockPos> spots = new ArrayList<>();
        int index;
        int louder;
        double lastDistance = Double.MAX_VALUE;
        int insideClaps;
        long wardrobeRetryAfter;
        @Nullable
        BlockPos wardrobe;
        ItemStack originalCloth = ItemStack.EMPTY;
        long endingAt = -1;
        Vec3 anchor = Vec3.ZERO;
        float endingYaw;
        boolean needsSync = true;

        Game(UUID player, long now) {
            this(new ClapGameClock(player), now);
        }
        Game(ClapGameClock clock, long now) {
            this.clock = clock;
            this.player = clock.owner();
            this.next = now + 10;
        }
        CompoundTag save(ServerLevel level) {
            CompoundTag tag = clock.save();
            tag.putString("Stage", stage.name());
            tag.putInt("Count", count); tag.putLong("Next", next);
            tag.putInt("Index", index); tag.putInt("Louder", louder);
            tag.putInt("InsideClaps", insideClaps);
            tag.putLong("WardrobeRetryAfter", wardrobeRetryAfter);
            tag.putLongArray("Spots", spots.stream().mapToLong(BlockPos::asLong).toArray());
            if (wardrobe != null) tag.putLong("Wardrobe", wardrobe.asLong());
            if (!originalCloth.isEmpty()) tag.put("Cloth", originalCloth.save(level.registryAccess()));
            tag.putLong("EndingAt", endingAt);
            tag.putDouble("X", anchor.x); tag.putDouble("Y", anchor.y); tag.putDouble("Z", anchor.z);
            tag.putFloat("Yaw", endingYaw);
            return tag;
        }
        static Game load(CompoundTag tag, ServerLevel level) {
            Game restored = new Game(ClapGameClock.load(tag), level.getGameTime());
            try { restored.stage = Stage.valueOf(tag.getString("Stage")); }
            catch (IllegalArgumentException ignored) { restored.stage = restored.clock.started() ? Stage.COUNTING : Stage.WAITING; }
            restored.count = Math.max(0, Math.min(COUNT.length - 1, tag.getInt("Count")));
            restored.next = tag.getLong("Next"); restored.index = tag.getInt("Index");
            restored.louder = tag.getInt("Louder"); restored.insideClaps = tag.getInt("InsideClaps");
            restored.wardrobeRetryAfter = tag.getLong("WardrobeRetryAfter");
            for (long spot : tag.getLongArray("Spots")) restored.spots.add(BlockPos.of(spot));
            if (tag.contains("Wardrobe")) restored.wardrobe = BlockPos.of(tag.getLong("Wardrobe"));
            restored.originalCloth = ItemStack.parseOptional(level.registryAccess(), tag.getCompound("Cloth"));
            restored.endingAt = tag.contains("EndingAt") ? tag.getLong("EndingAt") : -1;
            restored.anchor = new Vec3(tag.getDouble("X"), tag.getDouble("Y"), tag.getDouble("Z"));
            restored.endingYaw = tag.getFloat("Yaw");
            return restored;
        }
    }

    private static final class Feet {
        final ClapGhostEntity stand;
        final UUID viewer;
        final Vec3 from;
        final Vec3 to;
        int tick;
        final int duration;

        Feet(ClapGhostEntity stand, UUID viewer, Vec3 from, Vec3 to, int duration) {
            this.stand = stand;
            this.viewer = viewer;
            this.from = from;
            this.to = to;
            this.duration = duration;
        }
    }

    @Nullable
    private static Game game;
    @Nullable
    private static Feet feet;
    private static long nextUnderBed;
    /** Set when the room is rebuilt: its frames are hung once its entities are loaded. */
    private static boolean entitiesPending;

    private HideAndClap() {
    }

    private static ResourceLocation sound(String path) {
        return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, path);
    }

    // ------------------------------------------------------------------
    // The room

    /** Called by the builder after the slot is filled solid. */
    public static void build(MinecraftServer server, ServerLevel level, BlockPos base) {
        LabyrinthData data = LabyrinthData.get(server);
        int flags = LabyrinthBuilder.flags();
        BlockState wall = Blocks.LIGHT_BLUE_TERRACOTTA.defaultBlockState();
        BlockState floor = Blocks.BIRCH_PLANKS.defaultBlockState();
        BlockState ceiling = Blocks.BIRCH_PLANKS.defaultBlockState();
        LabyrinthBuilder.room(level, base, -5, 5, 3, -12, -1, wall, floor, ceiling);

        // A small bed against the north wall, its post beside the pillow.
        LabyrinthBuilder.bed(level, base.offset(BED_FOOT), Direction.NORTH, Blocks.LIGHT_BLUE_BED);
        level.setBlock(base.offset(BED_POST), Blocks.SPRUCE_FENCE.defaultBlockState(), flags);
        // A rug in the corner by the door, a toy piano, a little table and chair, one lamp.
        for (int x = 2; x <= 4; x++) {
            for (int z = -3; z <= -2; z++) {
                level.setBlock(base.offset(x, 0, z), Blocks.YELLOW_CARPET.defaultBlockState(), flags);
            }
        }
        level.setBlock(base.offset(4, 0, -12), Blocks.NOTE_BLOCK.defaultBlockState(), flags);
        level.setBlock(base.offset(4, 0, -8), Blocks.SPRUCE_FENCE.defaultBlockState(), flags);
        level.setBlock(base.offset(4, 1, -8), Blocks.SPRUCE_PRESSURE_PLATE.defaultBlockState(), flags);
        level.setBlock(base.offset(3, 0, -8), LabyrinthBuilder.stairs(Blocks.SPRUCE_STAIRS, Direction.WEST), flags);
        LabyrinthBuilder.hangLantern(level, base.offset(0, 3, -6), false);

        CompoundTag state = data.state(ID);
        if (state.contains("Wardrobe")) {
            setWardrobe(level, base.offset(BlockPos.of(state.getLong("Wardrobe"))),
                    Direction.from3DDataValue(state.getInt("Facing")), state.getBoolean("Opened"));
        }
        LabyrinthBuilder.entrance(level, base, wall, floor, ceiling);
        LabyrinthBuilder.doors(level, base, LabyrinthPlace.HIDE_AND_CLAP);
        game = null;
        entitiesPending = true;
        placeEntitiesIfLoaded(level, base, data);
    }

    /** The frames on the wall: the note always, the blindfold until the game has been won. */
    private static void placeEntitiesIfLoaded(ServerLevel level, BlockPos base, LabyrinthData data) {
        if (!entitiesPending || !level.areEntitiesLoaded(ChunkPos.asLong(base))) {
            return;
        }
        entitiesPending = false;
        AABB room = interior(base).inflate(1.0D);
        level.getEntitiesOfClass(ItemFrame.class, room).forEach(frame -> frame.discard());
        level.getEntitiesOfClass(ArmorStand.class, room, stand -> stand.getTags().contains(FEET_TAG)).forEach(stand -> stand.discard());
        level.getEntitiesOfClass(ClapGhostEntity.class, room).forEach(stand -> stand.discard());

        ItemFrame noteFrame = new ItemFrame(level, base.offset(NOTE_FRAME), Direction.SOUTH);
        noteFrame.setItem(note(), false);
        level.addFreshEntity(noteFrame);
        if (!data.isCompleted(ID)) {
            ItemFrame blindfoldFrame = new ItemFrame(level, base.offset(BLINDFOLD_FRAME), Direction.SOUTH);
            blindfoldFrame.setItem(new ItemStack(LabyrinthRegistry.BLINDFOLD.get()), false);
            level.addFreshEntity(blindfoldFrame);
        }
    }

    @Nullable
    static BlockPos base(MinecraftServer server) {
        BlockPos origin = HouseSavedData.get(server).houseOrigin();
        return origin == null ? null : LabyrinthPlaces.base(origin, LabyrinthPlace.HIDE_AND_CLAP);
    }

    private static AABB interior(BlockPos base) {
        return new AABB(base.getX() - 5, base.getY() - 1, base.getZ() - 12, base.getX() + 6, base.getY() + 4, base.getZ());
    }

    public static boolean isInRoom(BlockPos base, Vec3 pos) {
        return interior(base).contains(pos);
    }

    /** Spots on the floor a child could clap from: open, with floor under them, off the walls. */
    public static List<BlockPos> openSpots(Level level, BlockPos base) {
        List<BlockPos> spots = new ArrayList<>();
        for (int x = -4; x <= 4; x++) {
            for (int z = -11; z <= -2; z++) {
                BlockPos pos = base.offset(x, 0, z);
                if (RedRoom.isOpen(level, pos) && RedRoom.isOpen(level, pos.above()) && !RedRoom.isOpen(level, pos.below())) {
                    spots.add(pos);
                }
            }
        }
        return spots;
    }

    /**
     * Two blocks directly behind someone at {@code at} facing {@code yaw};
     * if that is blocked, the nearest open spot within 45 degrees of it.
     * Never in the doorway, never where they stand. Null if nowhere fits.
     */
    @Nullable
    public static BlockPos wardrobeSpot(Level level, BlockPos base, Vec3 at, float yaw) {
        double[] distances = {2.0D, 2.5D, 1.6D};
        double[] angles = {0.0D, 15.0D, -15.0D, 30.0D, -30.0D, 45.0D, -45.0D};
        for (double distance : distances) {
            for (double angle : angles) {
                double r = Math.toRadians(yaw + 180.0D + angle);
                BlockPos pos = BlockPos.containing(at.x - Math.sin(r) * distance, at.y + 0.1D, at.z + Math.cos(r) * distance);
                if (canStandWardrobe(level, base, pos, at)) {
                    return pos;
                }
            }
        }
        return null;
    }

    private static boolean canStandWardrobe(Level level, BlockPos base, BlockPos pos, Vec3 player) {
        BlockPos rel = pos.subtract(base);
        if (rel.getX() < -5 || rel.getX() > 5 || rel.getZ() < -12 || rel.getZ() > -2 || rel.getY() != 0) {
            return false;
        }
        double dx = pos.getX() + 0.5D - player.x;
        double dz = pos.getZ() + 0.5D - player.z;
        if (dx * dx + dz * dz < 1.3D * 1.3D) {
            return false;
        }
        return isClear(level.getBlockState(pos)) && isClear(level.getBlockState(pos.above()))
                && !RedRoom.isOpen(level, pos.below());
    }

    private static boolean isClear(BlockState state) {
        return state.canBeReplaced() || state.is(BlockTags.WOOL_CARPETS);
    }

    private static void setWardrobe(ServerLevel level, BlockPos lower, Direction facing, boolean open) {
        BlockState wardrobe = HouseBlocks.HIDE_AND_CLAP_WARDROBE.get().defaultBlockState()
                .setValue(WardrobeBlock.FACING, facing)
                .setValue(WardrobeBlock.OPEN, open);
        level.setBlock(lower, wardrobe.setValue(WardrobeBlock.HALF, DoubleBlockHalf.LOWER), LabyrinthBuilder.flags());
        level.setBlock(lower.above(), wardrobe.setValue(WardrobeBlock.HALF, DoubleBlockHalf.UPPER), LabyrinthBuilder.flags());
    }

    // ------------------------------------------------------------------
    // The game

    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        ServerLevel level = server.getLevel(HouseDimensions.INTERIOR);
        BlockPos base = base(server);
        if (level == null || base == null) {
            return;
        }
        tickFeet(level);
        LabyrinthData data = LabyrinthData.get(server);
        placeEntitiesIfLoaded(level, base, data);
        restore(data, level);
        if (data.isCompleted(ID)) {
            if (game != null && server.getPlayerList().getPlayer(game.player) != null) {
                release(game, server);
                game = null;
                persist(data, level);
            }
            removeFeet();
            return;
        }
        long now = level.getGameTime();
        if (game == null) {
            for (ServerPlayer player : level.players()) {
                if (!isInRoom(base, player.position()) || player.isSpectator() || !player.isAlive() || failed(data, player.getUUID())) {
                    continue;
                }
                enter(player);
                break;
            }
            if (game == null) return;
        }

        Game current = game;
        ServerPlayer player = server.getPlayerList().getPlayer(current.player);
        if (player == null || !player.isAlive()) return;
        if (current.needsSync) {
            current.needsSync = false;
            if (current.stage == Stage.ENDING) beginEnding(player, current, level, base, data, now);
            else sync(player, current, 0);
        }
        confine(player, current, level, base);
        startIfWorn(player, current, level, data, now);
        if (current.clock.bound()) enforceBlindfold(player, current);
        if (current.clock.expired(now) && current.stage != Stage.ENDING) beginEnding(player, current, level, base, data, now);
        switch (current.stage) {
            case WAITING -> {
                if (now >= nextUnderBed && player.getRandom().nextInt(700) == 0) {
                    play(player, CLAP_MUFFLED, Vec3.atBottomCenterOf(base.offset(BED_FOOT)).add(0, .15, 0), .8F, 1);
                    nextUnderBed = now + 600;
                }
            }
            case COUNTING -> {
                if (now >= current.next) {
                    count(player, current, level, base, now);
                    persist(data, level);
                }
            }
            case CLAPPING -> {
                clap(player, current, level, base, data, now);
                if (now % 20 == 0) persist(data, level);
            }
            case WARDROBE -> {
                if (current.wardrobe != null && current.insideClaps < 2 && now >= current.next) {
                    play(player, CLAP_MUFFLED, Vec3.atCenterOf(current.wardrobe).add(0.0D, 0.5D, 0.0D), 1.0F, 1.0F);
                    current.insideClaps++;
                    current.next = now + 30;
                }
            }
            case ENDING -> {
                long tick = now - current.endingAt;
                if (tick == ClapGameClock.TWIST_TICK) {
                    sync(player, current, (int) tick + 1);
                    player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.BONE_BLOCK_BREAK,
                            SoundSource.PLAYERS, .8F, .6F);
                }
                if (tick >= ClapGameClock.ENDING_TICKS) {
                    player.kill();
                }
            }
        }
    }

    private static void restore(LabyrinthData data, ServerLevel level) {
        if (game != null) return;
        CompoundTag session = data.state(ID).getCompound("Session");
        if (game == null && session.hasUUID("Player")) game = Game.load(session, level);
    }

    private static void persist(LabyrinthData data, ServerLevel level) {
        CompoundTag state = data.state(ID);
        if (game == null) state.remove("Session");
        else state.put("Session", game.save(level));
        data.setState(ID, state);
    }

    /** The first entrant reserves the game; visitors cannot take that player's turn. */
    public static boolean canEnter(ServerPlayer player) {
        LabyrinthData data = LabyrinthData.get(player.server);
        ServerLevel level = player.server.getLevel(HouseDimensions.INTERIOR);
        if (level != null) restore(data, level);
        return data.isCompleted(ID) || game == null || game.player.equals(player.getUUID());
    }

    public static boolean enter(ServerPlayer player) {
        ServerLevel level = player.server.getLevel(HouseDimensions.INTERIOR);
        BlockPos base = base(player.server);
        if (level == null || base == null || player.isSpectator() || !player.isAlive()) return false;
        LabyrinthData data = LabyrinthData.get(player.server);
        if (data.isCompleted(ID)) return true;
        restore(data, level);
        if (!canEnter(player)) return false;
        if (game == null) {
            game = new Game(player.getUUID(), level.getGameTime());
            game.anchor = player.position();
            CompoundTag state = data.state(ID);
            if (state.contains("Wardrobe")) game.wardrobe = base.offset(BlockPos.of(state.getLong("Wardrobe")));
            for (ItemFrame frame : level.getEntitiesOfClass(ItemFrame.class, interior(base).inflate(1))) {
                if (frame.getPos().equals(base.offset(NOTE_FRAME)) && !frame.getItem().isEmpty()) frame.setItem(note(), false);
            }
            persist(data, level);
            player.displayClientMessage(Component.literal("The door shuts. Someone is waiting for you to put the blindfold on.")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
        }
        closeEntry(level, data);
        return true;
    }

    public static boolean isLocked(ServerPlayer player) {
        LabyrinthData data = LabyrinthData.get(player.server);
        ServerLevel level = player.server.getLevel(HouseDimensions.INTERIOR);
        if (level != null) restore(data, level);
        return !data.isCompleted(ID) && game != null && game.player.equals(player.getUUID()) && player.isAlive();
    }

    public static void confineLockedPlayer(ServerPlayer player) {
        ServerLevel level = player.server.getLevel(HouseDimensions.INTERIOR);
        BlockPos base = base(player.server);
        if (level != null && base != null && isLocked(player)) confine(player, game, level, base);
    }

    private static void closeEntry(ServerLevel level, LabyrinthData data) {
        LabyrinthData.Door entry = data.door(LabyrinthPlace.HIDE_AND_CLAP.entryDoorId());
        if (entry != null) {
            BlockState state = level.getBlockState(entry.lower);
            if (state.getBlock() instanceof DoorBlock door && state.getValue(DoorBlock.OPEN)) {
                door.setOpen(null, level, state, entry.lower, false);
            }
        }
    }

    private static void confine(ServerPlayer player, Game current, ServerLevel level, BlockPos base) {
        closeEntry(level, LabyrinthData.get(player.server));
        boolean inside = player.serverLevel() == level && isInRoom(base, player.position());
        if (inside && current.stage != Stage.ENDING) {
            current.anchor = player.position();
            return;
        }
        Vec3 to = isInRoom(base, current.anchor) ? current.anchor : Vec3.atBottomCenterOf(base.offset(0, 0, -3));
        if (!inside || player.position().distanceToSqr(to) > .0025D) {
            player.stopRiding();
            player.teleportTo(level, to.x, to.y, to.z,
                    current.stage == Stage.ENDING ? current.endingYaw : player.getYRot(),
                    current.stage == Stage.ENDING ? 32 : player.getXRot());
        }
        player.setDeltaMovement(Vec3.ZERO);
        player.resetFallDistance();
    }

    private static void startIfWorn(ServerPlayer player, Game current, ServerLevel level, LabyrinthData data, long now) {
        if (!current.clock.started() && BlindfoldItem.isWorn(player) && current.clock.equip(player.getUUID(), now)) {
            current.originalCloth = player.getItemBySlot(EquipmentSlot.HEAD).copy();
            player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            current.stage = current.wardrobe == null ? Stage.COUNTING : Stage.WARDROBE;
            current.next = now + 10;
            enforceBlindfold(player, current);
            sync(player, current, 0);
            persist(data, level);
            player.displayClientMessage(Component.literal("The cloth tightens. Find my wardrobe before a minute passes.")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
        }
    }

    public static boolean isBound(ItemStack stack, UUID player) {
        if (!stack.is(LabyrinthRegistry.BLINDFOLD.get())) return false;
        CustomData custom = stack.get(DataComponents.CUSTOM_DATA);
        if (custom == null) return false;
        CompoundTag tag = custom.copyTag();
        return tag.hasUUID(BOUND_TO) && tag.getUUID(BOUND_TO).equals(player);
    }

    private static void removeBoundCopies(ServerPlayer player, UUID owner) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (isBound(player.getInventory().getItem(i), owner)) player.getInventory().setItem(i, ItemStack.EMPTY);
        }
        for (Slot slot : player.containerMenu.slots) if (isBound(slot.getItem(), owner)) slot.set(ItemStack.EMPTY);
        if (isBound(player.containerMenu.getCarried(), owner)) player.containerMenu.setCarried(ItemStack.EMPTY);
    }

    private static void enforceBlindfold(ServerPlayer player, Game current) {
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        if (isBound(head, current.player)) return;
        ItemStack replacement = head.copy();
        removeBoundCopies(player, current.player);
        ItemStack cloth = current.originalCloth.isEmpty() ? new ItemStack(LabyrinthRegistry.BLINDFOLD.get()) : current.originalCloth.copy();
        cloth.setCount(1);
        CustomData old = cloth.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = old == null ? new CompoundTag() : old.copyTag();
        tag.putUUID(BOUND_TO, current.player);
        cloth.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        cloth.enchant(player.registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(Enchantments.BINDING_CURSE), 1);
        player.setItemSlot(EquipmentSlot.HEAD, cloth);
        if (!replacement.isEmpty() && !isBound(replacement, current.player)) {
            // The first bind takes this same cloth from the head, not another copy.
            if (!player.getInventory().add(replacement)) player.drop(replacement, false);
        }
        player.inventoryMenu.broadcastChanges();
        player.containerMenu.broadcastChanges();
    }

    private static void release(Game current, MinecraftServer server) {
        ServerPlayer player = server.getPlayerList().getPlayer(current.player);
        if (player != null) {
            removeBoundCopies(player, current.player);
            if (!current.originalCloth.isEmpty()) {
                ItemStack cloth = current.originalCloth.copy();
                if (!player.getInventory().add(cloth)) player.drop(cloth, false);
            }
            HousePackets.send(player, new ClapGamePayload(false, 0, 0));
            player.inventoryMenu.broadcastChanges();
        }
    }

    private static void sync(ServerPlayer player, Game current, int endingTick) {
        HousePackets.send(player, new ClapGamePayload(current.clock.bound(), endingTick, current.endingYaw));
    }

    private static void beginEnding(ServerPlayer player, Game current, ServerLevel level, BlockPos base, LabyrinthData data, long now) {
        removeFeet();
        current.stage = Stage.ENDING;
        current.endingAt = now;
        current.anchor = player.position();
        current.endingYaw = player.getYRot();
        double r = Math.toRadians(current.endingYaw);
        Vec3 forward = new Vec3(-Math.sin(r), 0, Math.cos(r));
        if (!isInRoom(base, current.anchor.add(forward.scale(1.5)))) {
            Vec3 center = Vec3.atBottomCenterOf(base.offset(0, 0, -6)).subtract(current.anchor);
            forward = new Vec3(center.x, 0, center.z).normalize();
            current.endingYaw = (float) Math.toDegrees(Math.atan2(-forward.x, forward.z));
        }
        Vec3 from = current.anchor.add(forward.scale(1.5));
        Vec3 to = current.anchor.add(forward.scale(.85));
        spawnFeet(level, player, from, to, current.endingYaw + 180, ClapGameClock.ENDING_TICKS);
        enforceBlindfold(player, current);
        player.closeContainer();
        player.stopRiding();
        player.teleportTo(level, current.anchor.x, current.anchor.y, current.anchor.z, current.endingYaw, 32);
        sync(player, current, 1);
        persist(data, level);
    }

    /** Clear carried items, armor, offhand and the menu cursor, including keepInventory worlds. */
    public static void emptyInventory(ServerPlayer player) {
        player.containerMenu.setCarried(ItemStack.EMPTY);
        player.inventoryMenu.setCarried(ItemStack.EMPTY);
        player.getInventory().clearContent();
        player.inventoryMenu.broadcastChanges();
    }

    private static boolean failed(LabyrinthData data, UUID player) {
        return data.state(ID).getCompound("FailedPlayers").getBoolean(player.toString());
    }

    private static void resetRound(MinecraftServer server) {
        ServerLevel level = server.getLevel(HouseDimensions.INTERIOR);
        BlockPos base = base(server);
        if (level == null || base == null) return;
        LabyrinthData data = LabyrinthData.get(server);
        CompoundTag state = data.state(ID);
        if (state.contains("Wardrobe")) {
            BlockPos wardrobe = base.offset(BlockPos.of(state.getLong("Wardrobe")));
            level.setBlock(wardrobe, Blocks.AIR.defaultBlockState(), LabyrinthBuilder.flags());
            level.setBlock(wardrobe.above(), Blocks.AIR.defaultBlockState(), LabyrinthBuilder.flags());
        }
        state.remove("Session"); state.remove("Wardrobe"); state.remove("Facing");
        state.remove("Drawing"); state.remove("Opened");
        data.setState(ID, state);
        game = null;
        removeFeet();
        entitiesPending = true;
        placeEntitiesIfLoaded(level, base, data);
    }

    private static void count(ServerPlayer player, Game current, ServerLevel level, BlockPos base, long now) {
        String word = COUNT[current.count];
        current.count++;
        boolean last = current.count >= COUNT.length;
        player.displayClientMessage(Component.literal(last ? word + ". Ready or not." : word + "...")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
        current.next = now + COUNT_INTERVAL;
        if (!last) {
            return;
        }
        List<BlockPos> open = openSpots(level, base);
        int claps = 3 + player.getRandom().nextInt(2);
        BlockPos from = player.blockPosition();
        for (int i = 0; i < claps && !open.isEmpty(); i++) {
            BlockPos pick = pickSpot(open, from, current.spots, player);
            current.spots.add(pick);
            from = pick;
        }
        current.stage = Stage.CLAPPING;
        current.index = 0;
        current.next = now + 25;
    }

    private static BlockPos pickSpot(List<BlockPos> open, BlockPos from, List<BlockPos> taken, ServerPlayer player) {
        for (double least : new double[]{4.0D, 2.5D, 0.0D}) {
            List<BlockPos> candidates = new ArrayList<>();
            for (BlockPos spot : open) {
                if (!taken.contains(spot) && Math.sqrt(spot.distSqr(from)) >= least) {
                    candidates.add(spot);
                }
            }
            if (!candidates.isEmpty()) {
                return candidates.get(player.getRandom().nextInt(candidates.size()));
            }
        }
        return open.get(0);
    }

    private static void clap(ServerPlayer player, Game current, ServerLevel level, BlockPos base, LabyrinthData data, long now) {
        if (current.index >= current.spots.size()) {
            return;
        }
        BlockPos spot = current.spots.get(current.index);
        double dx = spot.getX() + 0.5D - player.getX();
        double dz = spot.getZ() + 0.5D - player.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        if (distance <= FOUND_DISTANCE) {
            if (now < current.wardrobeRetryAfter) return;
            current.index++;
            current.louder = 0;
            current.lastDistance = Double.MAX_VALUE;
            current.next = now + 20;
            if (current.index >= current.spots.size()) {
                hideInWardrobe(player, current, level, base, data, now);
            } else if (current.index == 2) {
                startFeet(level, base, player, current.spots.get(2));
            }
            return;
        }
        if (now < current.next) {
            return;
        }
        if (distance > current.lastDistance + 0.75D) {
            current.louder = Math.min(4, current.louder + 1);
        }
        current.lastDistance = distance;
        play(player, CLAP, Vec3.atBottomCenterOf(spot).add(0.0D, 1.1D, 0.0D), 0.9F + 0.45F * current.louder,
                0.95F + player.getRandom().nextFloat() * 0.1F);
        current.next = now + CLAP_INTERVAL;
    }

    /** The last clap: a wardrobe that wasn't there, behind them, and two claps from inside it. */
    private static void hideInWardrobe(ServerPlayer player, Game current, ServerLevel level, BlockPos base, LabyrinthData data, long now) {
        BlockPos spot = wardrobeSpot(level, base, player.position(), player.getYRot());
        if (spot == null) {
            // A cramped spot must not cancel the timer or free the blindfold.
            play(player, CLAP_MUFFLED, Vec3.atBottomCenterOf(base.offset(BED_FOOT)).add(0.0D, 0.15D, 0.0D), 1.2F, 1.0F);
            current.index = Math.max(0, current.spots.size() - 1);
            current.next = now + CLAP_INTERVAL;
            current.wardrobeRetryAfter = current.next;
            return;
        }
        Vec3 toPlayer = player.position().subtract(Vec3.atBottomCenterOf(spot));
        Direction facing = Direction.getNearest(toPlayer.x, 0.0D, toPlayer.z);
        setWardrobe(level, spot, facing, false);

        Vec3 eye = Vec3.atBottomCenterOf(spot).add(0.0D, 1.0D, 0.0D);
        float yaw = (float) Math.toDegrees(Math.atan2(-toPlayer.x, toPlayer.z));
        byte[] page = CrayonDrawing.draw(eye, yaw, player.position(), Vec3.atBottomCenterOf(base.offset(BED_FOOT)), player.getRandom().nextLong());
        MapId drawing = CrayonDrawing.store(level, page);

        CompoundTag state = data.state(ID);
        state.putLong("Wardrobe", spot.subtract(base).asLong());
        state.putInt("Facing", facing.get3DDataValue());
        state.putInt("Drawing", drawing.id());
        data.setState(ID, state);

        current.wardrobe = spot;
        current.stage = Stage.WARDROBE;
        current.insideClaps = 0;
        current.next = now + 20;
        persist(data, level);
        TheOldestHouse.LOGGER.info("The hide-and-clap wardrobe stands behind {}.", player.getGameProfile().getName());
    }

    // ------------------------------------------------------------------
    // The feet

    private static void startFeet(ServerLevel level, BlockPos base, ServerPlayer player, BlockPos towards) {
        removeFeet();
        double r = Math.toRadians(player.getYRot());
        Vec3 forward = new Vec3(-Math.sin(r), 0.0D, Math.cos(r));
        Vec3 from = player.position().add(forward.scale(1.8D));
        if (!isInRoom(base, from)) {
            from = player.position().add(forward.scale(0.9D));
        }
        Vec3 away = Vec3.atBottomCenterOf(towards).subtract(from);
        away = new Vec3(away.x, 0.0D, away.z);
        if (away.lengthSqr() < 1.0E-4D) {
            away = forward;
        }
        Vec3 to = from.add(away.normalize().scale(2.6D));
        float yaw = (float) Math.toDegrees(Math.atan2(-away.x, away.z));
        spawnFeet(level, player, new Vec3(from.x, base.getY(), from.z),
                new Vec3(to.x, base.getY(), to.z), yaw, FEET_TICKS);
    }

    private static void spawnFeet(ServerLevel level, ServerPlayer player, Vec3 from, Vec3 to, float yaw, int duration) {
        ClapGhostEntity stand = ClapGhostRegistry.GIRL.get().create(level);
        if (stand == null) {
            return;
        }
        stand.setViewer(player.getUUID());
        stand.addTag(FEET_TAG);
        stand.moveTo(from.x, from.y, from.z, yaw, 0.0F);
        level.addFreshEntity(stand);
        feet = new Feet(stand, player.getUUID(), from, to, duration);
    }

    private static void tickFeet(ServerLevel level) {
        Feet current = feet;
        if (current == null) {
            return;
        }
        current.tick++;
        int travel = current.duration == ClapGameClock.ENDING_TICKS ? ClapGameClock.TWIST_TICK : current.duration;
        double t = Math.min(1.0D, current.tick / (double) travel);
        Vec3 at = current.from.lerp(current.to, t);
        current.stand.moveTo(at.x, current.stand.getY(), at.z, current.stand.getYRot(), 0.0F);
        current.stand.hasImpulse = true;
        if (current.tick % 6 == 1 && level.getServer().getPlayerList().getPlayer(current.viewer) instanceof ServerPlayer viewer) {
            play(viewer, SMALL_STEPS, at, 0.45F, 1.0F);
        }
        if (current.tick >= current.duration) {
            removeFeet();
        }
    }

    private static void removeFeet() {
        if (feet != null) {
            feet.stand.discard();
            feet = null;
        }
    }

    // ------------------------------------------------------------------
    // Opening the wardrobe, and not cheating

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide() || !level.dimension().equals(HouseDimensions.INTERIOR) || level.getServer() == null) {
            return;
        }
        BlockPos base = base(level.getServer());
        LabyrinthData data = LabyrinthData.get(level.getServer());
        CompoundTag state = data.state(ID);
        if (base == null || !state.contains("Wardrobe")) {
            return;
        }
        BlockPos wardrobe = base.offset(BlockPos.of(state.getLong("Wardrobe")));
        if (!event.getPos().equals(wardrobe) && !event.getPos().equals(wardrobe.above())) {
            return;
        }
        // It is never a container: it opens once, onto the drawing, and stays open.
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getHand() != InteractionHand.MAIN_HAND
                || state.getBoolean("Opened") || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        restore(data, serverLevel);
        if (game == null || game.stage != Stage.WARDROBE || !game.player.equals(player.getUUID())
                || !game.clock.complete(player.getUUID(), serverLevel.getGameTime())) return;
        Game finished = game;
        Direction facing = Direction.from3DDataValue(state.getInt("Facing"));
        setWardrobe(serverLevel, wardrobe, facing, true);
        serverLevel.playSound(null, wardrobe, SoundEvents.BARREL_OPEN, SoundSource.BLOCKS, 0.8F, 0.8F);
        Vec3 front = Vec3.atBottomCenterOf(wardrobe.relative(facing)).add(0.0D, 0.4D, 0.0D);
        ItemEntity drawing = new ItemEntity(serverLevel, front.x, front.y, front.z, CrayonDrawing.item(new MapId(state.getInt("Drawing"))));
        drawing.setDeltaMovement(Vec3.ZERO);
        drawing.setNoPickUpDelay();
        serverLevel.addFreshEntity(drawing);
        state.putBoolean("Opened", true);
        data.setState(ID, state);
        data.setCompleted(ID, true);
        release(finished, level.getServer());
        game = null;
        persist(data, serverLevel);
        removeFeet();
        TheOldestHouse.LOGGER.info("{} opened the wardrobe.", player.getGameProfile().getName());
    }

    /** Empty frames in the room stay on the wall. */
    public static void onAttackEntity(AttackEntityEvent event) {
        if (event.getTarget() instanceof ItemFrame frame && frame.getItem().isEmpty()
                && frame.level() instanceof ServerLevel level && level.dimension().equals(HouseDimensions.INTERIOR)) {
            BlockPos base = base(level.getServer());
            if (base != null && isInRoom(base, frame.position())) {
                event.setCanceled(true);
            }
        }
        if (event.getTarget() instanceof ItemFrame frame && game != null && !game.player.equals(event.getEntity().getUUID())
                && frame.level() instanceof ServerLevel level) {
            BlockPos base = base(level.getServer());
            if (base != null && isInRoom(base, frame.position())) event.setCanceled(true);
        }
    }

    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getSlot() == EquipmentSlot.HEAD && event.getEntity() instanceof ServerPlayer player && isLocked(player)) {
            ServerLevel level = player.server.getLevel(HouseDimensions.INTERIOR);
            startIfWorn(player, game, level, LabyrinthData.get(player.server), level.getGameTime());
            if (game.clock.bound()) enforceBlindfold(player, game);
        }
    }

    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        if (event.getEntity() instanceof ServerPlayer player && isLocked(player)) {
            confineLockedPlayer(player);
            ServerLevel level = player.server.getLevel(HouseDimensions.INTERIOR);
            startIfWorn(player, game, level, LabyrinthData.get(player.server), level.getGameTime());
            if (game.clock.bound()) enforceBlindfold(player, game);
        }
    }

    public static void onItemToss(ItemTossEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player && isLocked(player)
                && isBound(event.getEntity().getItem(), player.getUUID())) {
            event.setCanceled(true);
            event.getEntity().discard();
        }
    }

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (isLocked(player)) {
                game.needsSync = true;
                confineLockedPlayer(player);
                if (game.clock.bound()) enforceBlindfold(player, game);
                sync(player, game, 0);
            } else HousePackets.send(player, new ClapGamePayload(false, 0, 0));
        }
    }

    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && game != null && game.player.equals(player.getUUID())) {
            persist(LabyrinthData.get(player.server), player.server.getLevel(HouseDimensions.INTERIOR));
            game.needsSync = true;
            removeFeet();
        }
    }

    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && game != null && game.player.equals(player.getUUID())) {
            LabyrinthData data = LabyrinthData.get(player.server);
            CompoundTag state = data.state(ID);
            CompoundTag failures = state.getCompound("FailedPlayers");
            failures.putBoolean(player.getUUID().toString(), true);
            state.put("FailedPlayers", failures);
            data.setState(ID, state);
            // Restore the actual cloth before vanilla creates death drops. The
            // temporary binding marker and curse must not strand it afterward.
            Game dying = game;
            removeBoundCopies(player, dying.player);
            if (!dying.originalCloth.isEmpty()) player.setItemSlot(EquipmentSlot.HEAD, dying.originalCloth.copy());
            ItemStack cursor = player.containerMenu.getCarried();
            player.containerMenu.setCarried(ItemStack.EMPTY);
            if (!cursor.isEmpty()) {
                ItemEntity drop = player.drop(cursor, true, false);
                if (drop != null) MotherOfStrays.recordDropOwner(drop, player.getUUID());
            }
            data.clearReturns(player.getUUID());
            resetRound(player.server);
            // In an ordinary world vanilla drops these once. In a keepInventory
            // world the game's loss still creates ordinary recoverable items.
            if (player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) dropInventoryForFailure(player);
        }
    }

    /** Native, recoverable drops with their original components and the Mother's owner record. */
    public static void dropInventoryForFailure(ServerPlayer player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().removeItemNoUpdate(slot);
            if (!stack.isEmpty()) {
                ItemEntity item = player.drop(stack, true, false);
                if (item != null) MotherOfStrays.recordDropOwner(item, player.getUUID());
            }
        }
    }

    /** Outside the impossible hallway's near entrance, in the manor's domestic hall. */
    public static Vec3 manorRespawn(BlockPos origin) {
        return Vec3.atBottomCenterOf(origin.offset(HouseLayout.AXIS_X, 1, HouseLayout.THRESHOLD_Z - 2)).add(0, .1, 0);
    }

    public static void onPlayerRespawnPosition(PlayerRespawnPositionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !event.isFromEndFight()
                && failed(LabyrinthData.get(player.server), player.getUUID())) {
            BlockPos origin = HouseSavedData.get(player.server).houseOrigin();
            ServerLevel manor = player.server.getLevel(HouseDimensions.INTERIOR);
            if (origin != null && manor != null) {
                Vec3 at = manorRespawn(origin);
                manor.getChunkAt(BlockPos.containing(at));
                event.setDimensionTransition(new DimensionTransition(manor, at, Vec3.ZERO, 0, 0, DimensionTransition.DO_NOTHING));
            }
        }
    }

    public static void onPlayerRespawned(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            LabyrinthData data = LabyrinthData.get(player.server);
            if (failed(data, player.getUUID())) {
                emptyInventory(player);
                CompoundTag state = data.state(ID);
                CompoundTag failures = state.getCompound("FailedPlayers");
                failures.remove(player.getUUID().toString());
                state.put("FailedPlayers", failures);
                data.setState(ID, state);
                HousePackets.send(player, new ClapGamePayload(false, 0, 0));
            }
        }
    }

    public static void onServerStopping(ServerStoppingEvent event) {
        ServerLevel level = event.getServer().getLevel(HouseDimensions.INTERIOR);
        if (level != null && game != null) persist(LabyrinthData.get(event.getServer()), level);
        removeFeet();
        clearAll();
    }

    public static void clearAll() {
        game = null;
        feet = null;
        nextUnderBed = 0L;
        entitiesPending = false;
    }

    // ------------------------------------------------------------------
    // Sounds and notes

    private static void play(ServerPlayer player, ResourceLocation sound, Vec3 at, float volume, float pitch) {
        Holder<SoundEvent> holder = Holder.direct(SoundEvent.createVariableRangeEvent(sound));
        player.connection.send(new ClientboundSoundPacket(holder, SoundSource.AMBIENT, at.x, at.y, at.z, volume, pitch,
                player.getRandom().nextLong()));
    }

    private static ItemStack book(String title, String... pages) {
        List<Filterable<Component>> content = new ArrayList<>();
        for (String page : pages) {
            content.add(Filterable.passThrough(Component.literal(page)));
        }
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        book.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(Filterable.passThrough(title), "?", 0, content, true));
        return book;
    }

    public static ItemStack note() {
        return book("hide and clap",
                "lets play hide and clap!!\n\nput the blindfold on and count to ten.\n\nthen follow my claps.",
                "you have one minute after putting it on.\n\nopen my wardrobe to finish. then you can take it off.\n\nif you cant find me, ill finish for you.");
    }

    public static ItemStack peekedNote() {
        return book("you peeked", "you peeked.\n\ni saw you.\n\nput it back on and count again.");
    }

    // ------------------------------------------------------------------
    // Testing

    /** Puts the room back as new: no wardrobe, the blindfold on the wall, dealable again. */
    public static boolean reset(MinecraftServer server) {
        ServerLevel level = server.getLevel(HouseDimensions.INTERIOR);
        BlockPos base = base(server);
        if (level == null || base == null) {
            return false;
        }
        LabyrinthData data = LabyrinthData.get(server);
        if (game != null) release(game, server);
        data.setCompleted(ID, false);
        data.setState(ID, new CompoundTag());
        removeFeet();
        build(server, level, base);
        return true;
    }
}
