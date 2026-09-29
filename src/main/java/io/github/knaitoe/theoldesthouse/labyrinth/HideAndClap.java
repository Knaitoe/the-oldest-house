package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * The Conjuring: hide-and-clap. One-shot; verb: following claps blindfolded.
 *
 * A child's bedroom with open floor. On the wall by the bed, over its post,
 * hang a blindfold and a note: put it on, count to ten, follow the claps,
 * take it off when you think you've found me. Put the blindfold on and a
 * child counts to ten. Then three or four claps come from open spots in the
 * room; get within a block or two of one and the next sounds somewhere
 * else, wander off and the current one comes again, louder. At the third,
 * small bare feet step away through the strip under the blindfold. After
 * the last, the house reads which way the player faces and puts a wardrobe
 * two blocks directly behind them (or the nearest open spot within 45
 * degrees), and two claps come from inside it. Inside: a crayon drawing of
 * someone in a blindfold, drawn from exactly where the wardrobe stands.
 *
 * Take the blindfold off before the wardrobe and the claps stop, the game
 * resets and a new note on the bed says you peeked. Never put it on and,
 * now and then, a single clap comes from under the bed. Once the wardrobe
 * has been opened it stays open and the game is not offered again.
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

    private enum Stage { COUNTING, CLAPPING, WARDROBE }

    private static final class Game {
        final UUID player;
        Stage stage = Stage.COUNTING;
        int count;
        long next;
        final List<BlockPos> spots = new ArrayList<>();
        int index;
        int louder;
        double lastDistance = Double.MAX_VALUE;
        int insideClaps;
        @Nullable
        BlockPos wardrobe;

        Game(UUID player, long now) {
            this.player = player;
            this.next = now + 10;
        }
    }

    private static final class Feet {
        final ArmorStand stand;
        final UUID viewer;
        final Vec3 from;
        final Vec3 to;
        int tick;

        Feet(ArmorStand stand, UUID viewer, Vec3 from, Vec3 to) {
            this.stand = stand;
            this.viewer = viewer;
            this.from = from;
            this.to = to;
        }
    }

    @Nullable
    private static Game game;
    @Nullable
    private static Feet feet;
    @Nullable
    private static UUID peekNote;
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
        BlockState barrel = Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, facing).setValue(BarrelBlock.OPEN, open);
        level.setBlock(lower, barrel, LabyrinthBuilder.flags());
        level.setBlock(lower.above(), barrel.setValue(BarrelBlock.OPEN, false), LabyrinthBuilder.flags());
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
        if (data.isCompleted(ID) || level.players().isEmpty()) {
            game = null;
            return;
        }
        long now = level.getGameTime();

        if (game == null) {
            for (ServerPlayer player : level.players()) {
                if (!isInRoom(base, player.position()) || player.isSpectator()) {
                    continue;
                }
                if (BlindfoldItem.isWorn(player) && !data.state(ID).contains("Wardrobe")) {
                    game = new Game(player.getUUID(), now);
                    TheOldestHouse.LOGGER.info("{} put the blindfold on.", player.getGameProfile().getName());
                    return;
                }
                if (!BlindfoldItem.isWorn(player) && now >= nextUnderBed && player.getRandom().nextInt(700) == 0) {
                    // Nobody is playing yet. Something under the bed is.
                    play(player, CLAP_MUFFLED, Vec3.atBottomCenterOf(base.offset(BED_FOOT)).add(0.0D, 0.15D, 0.0D), 0.8F, 1.0F);
                    nextUnderBed = now + 600;
                }
            }
            return;
        }

        Game current = game;
        ServerPlayer player = server.getPlayerList().getPlayer(current.player);
        if (player == null || player.serverLevel() != level || !isInRoom(base, player.position())) {
            game = null;
            removeFeet();
            return;
        }
        boolean worn = BlindfoldItem.isWorn(player);
        switch (current.stage) {
            case COUNTING -> {
                if (!worn) {
                    peek(level, base, player);
                } else if (now >= current.next) {
                    count(player, current, level, base, now);
                }
            }
            case CLAPPING -> {
                if (!worn) {
                    peek(level, base, player);
                } else {
                    clap(player, current, level, base, data, now);
                }
            }
            case WARDROBE -> {
                if (current.wardrobe != null && current.insideClaps < 2 && now >= current.next) {
                    play(player, CLAP_MUFFLED, Vec3.atCenterOf(current.wardrobe).add(0.0D, 0.5D, 0.0D), 1.0F, 1.0F);
                    current.insideClaps++;
                    current.next = now + 30;
                }
                if (!worn) {
                    // Taken off at the end: the game's own rule. The wardrobe waits to be opened.
                    game = null;
                }
            }
        }
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
            // Nowhere behind them to stand it: the child is back under the bed, and the game ends there.
            play(player, CLAP_MUFFLED, Vec3.atBottomCenterOf(base.offset(BED_FOOT)).add(0.0D, 0.15D, 0.0D), 1.2F, 1.0F);
            game = null;
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
        TheOldestHouse.LOGGER.info("The hide-and-clap wardrobe stands behind {}.", player.getGameProfile().getName());
    }

    /** Taken off too soon: the claps stop, the game resets, and there is a new note on the bed. */
    private static void peek(ServerLevel level, BlockPos base, ServerPlayer player) {
        game = null;
        removeFeet();
        if (peekNote != null && level.getEntity(peekNote) instanceof ItemEntity old) {
            old.discard();
        }
        Vec3 onBed = Vec3.atBottomCenterOf(base.offset(BED_FOOT)).add(0.0D, 0.6D, 0.0D);
        ItemEntity note = new ItemEntity(level, onBed.x, onBed.y, onBed.z, peekedNote());
        note.setDeltaMovement(Vec3.ZERO);
        note.setUnlimitedLifetime();
        level.addFreshEntity(note);
        peekNote = note.getUUID();
        TheOldestHouse.LOGGER.info("{} peeked.", player.getGameProfile().getName());
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
        ArmorStand stand = EntityType.ARMOR_STAND.create(level);
        if (stand == null) {
            return;
        }
        CompoundTag look = new CompoundTag();
        look.putBoolean("Small", true);
        look.putBoolean("Marker", true);
        look.putBoolean("Invisible", true);
        look.putBoolean("NoBasePlate", true);
        stand.readAdditionalSaveData(look);
        stand.setInvisible(true);
        stand.setNoGravity(true);
        stand.setInvulnerable(true);
        stand.setSilent(true);
        ItemStack bare = new ItemStack(Items.LEATHER_BOOTS);
        bare.set(DataComponents.DYED_COLOR, new DyedItemColor(0xE2B08C, false));
        stand.setItemSlot(EquipmentSlot.FEET, bare);
        stand.addTag(FEET_TAG);
        float yaw = (float) Math.toDegrees(Math.atan2(-away.x, away.z));
        stand.moveTo(from.x, base.getY(), from.z, yaw, 0.0F);
        level.addFreshEntity(stand);
        feet = new Feet(stand, player.getUUID(), from, to);
    }

    private static void tickFeet(ServerLevel level) {
        Feet current = feet;
        if (current == null) {
            return;
        }
        current.tick++;
        double t = Math.min(1.0D, current.tick / (double) FEET_TICKS);
        Vec3 at = current.from.lerp(current.to, t);
        current.stand.moveTo(at.x, current.stand.getY(), at.z, current.stand.getYRot(), 0.0F);
        current.stand.hasImpulse = true;
        if (current.tick % 6 == 1 && level.getServer().getPlayerList().getPlayer(current.viewer) instanceof ServerPlayer viewer) {
            play(viewer, SMALL_STEPS, at, 0.45F, 1.0F);
        }
        if (current.tick >= FEET_TICKS) {
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
        game = null;
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
    }

    public static void onServerStopping(ServerStoppingEvent event) {
        removeFeet();
        clearAll();
    }

    public static void clearAll() {
        game = null;
        feet = null;
        peekNote = null;
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
                "take it off when you think you found me.\n\nNO PEEKING");
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
        data.setCompleted(ID, false);
        data.setState(ID, new CompoundTag());
        removeFeet();
        build(server, level, base);
        return true;
    }
}
