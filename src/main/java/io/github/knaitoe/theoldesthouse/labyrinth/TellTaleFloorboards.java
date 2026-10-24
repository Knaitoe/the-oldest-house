package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseBlocks;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.VanillaGameEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * The Tell-Tale Heart: the floorboards. One-shot; verb: sneaking.
 *
 * An old man's bedroom, one candle, a bed nobody is in. Sculk lies under the
 * boards, and every vibration the player makes (the same ones a sculk sensor
 * hears, so crouching makes none, as players know from Ancient Cities) speeds
 * a heartbeat that comes from under one particular board. If it races to the
 * top, the lights go out and the player is back where they came from. Pry
 * that board up with an axe and the caregiver's note is underneath; after
 * that the heartbeat stops, and the room is never dealt again.
 *
 * The room lies north of its entry door (see {@link LabyrinthPlace}).
 * Cheating: the room cannot be dug out of, built in, or its floor broken;
 * trying is loud.
 */
public final class TellTaleFloorboards {
    public static final String ID = "floorboards";

    /** The loose board, relative to the room's base (the floor is at y -1). */
    public static final BlockPos LOOSE_BOARD = new BlockPos(2, -1, -6);
    /** The chopping block in the corner by the wardrobe; the hatchet lies on top of it. */
    public static final BlockPos AXE_BLOCK = new BlockPos(5, 0, -1);
    private static final String AXE_TAG = TheOldestHouse.MOD_ID + "_floorboard_axe";
    private static final String AXE_KEY = "FloorboardAxe0505";

    public static final double MAX_HEAT = 100.0D;
    private static final double DECAY_PER_TICK = 0.08D;
    private static final double BREAK_HEAT = 35.0D;

    private static double heat;
    private static long nextBeat;

    private TellTaleFloorboards() {
    }

    // ------------------------------------------------------------------
    // The room

    public static void build(ServerLevel level, BlockPos base, boolean withBoard) {
        int flags = LabyrinthBuilder.flags();
        // Walls of old brown plaster, a dark ceiling, a floor of spruce boards.
        BlockState wall = Blocks.BROWN_TERRACOTTA.defaultBlockState();
        BlockState floor = Blocks.SPRUCE_PLANKS.defaultBlockState();
        BlockState ceiling = Blocks.DARK_OAK_PLANKS.defaultBlockState();
        LabyrinthBuilder.room(level, base, -5, 5, 3, -9, -1, wall, floor, ceiling);

        // Under the boards: sculk. The room listens for itself; nothing under the floor makes a sound but the heart.
        for (int x = -5; x <= 5; x++) {
            for (int z = -9; z <= -1; z++) {
                level.setBlock(base.offset(x, -2, z), Blocks.SCULK.defaultBlockState(), flags);
                level.setBlock(base.offset(x, -3, z), wall, flags);
            }
        }

        level.setBlock(base.offset(LOOSE_BOARD), withBoard
                ? HouseBlocks.LOOSE_FLOORBOARD.get().defaultBlockState()
                : Blocks.AIR.defaultBlockState(), flags);

        // His bed, made; one candle on a barrel; a chair turned towards the bed.
        LabyrinthBuilder.bed(level, base.offset(-4, 0, -7), Direction.NORTH, Blocks.WHITE_BED);
        level.setBlock(base.offset(-3, 0, -9), LabyrinthBuilder.barrel(Direction.UP), flags);
        level.setBlock(base.offset(-3, 1, -9), LabyrinthBuilder.candle(3, true), flags);
        level.setBlock(base.offset(-2, 0, -6), LabyrinthBuilder.stairs(Blocks.DARK_OAK_STAIRS, Direction.EAST), flags);
        // A wardrobe by the door, and a shuttered lantern by the bed.
        level.setBlock(base.offset(4, 0, -1), LabyrinthBuilder.barrel(Direction.WEST), flags);
        level.setBlock(base.offset(4, 1, -1), LabyrinthBuilder.barrel(Direction.WEST), flags);
        // 0.4.105: a chopping block in the corner beside it, with the hatchet he split kindling with left lying on top.
        level.setBlock(base.offset(AXE_BLOCK), Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), flags);
        LabyrinthBuilder.hangLantern(level, base.offset(-4, 3, -5), true);

        LabyrinthBuilder.entrance(level, base, wall, floor, ceiling);
        LabyrinthBuilder.doors(level, base, LabyrinthPlace.FLOORBOARDS);
        heat = 0.0D;
    }

    /** Where the room stands for this world's manor, or null without one. */
    @Nullable
    private static BlockPos base(MinecraftServer server) {
        BlockPos origin = HouseSavedData.get(server).houseOrigin();
        return origin == null ? null : LabyrinthPlaces.base(origin, LabyrinthPlace.FLOORBOARDS);
    }

    /** The room's inside, where vibrations count and players are "in" it. */
    private static AABB interior(BlockPos base) {
        return new AABB(base.getX() - 5, base.getY() - 2, base.getZ() - 9, base.getX() + 6, base.getY() + 4, base.getZ());
    }

    /** The room with its walls, floor and ceiling. */
    public static boolean isInShell(BlockPos base, BlockPos pos) {
        BlockPos rel = pos.subtract(base);
        return rel.getX() >= -6 && rel.getX() <= 6
                && rel.getY() >= -3 && rel.getY() <= 4
                && rel.getZ() >= -10 && rel.getZ() <= 0;
    }

    /** Whether {@code pos} is the loose board (in the House dimension). */
    public static boolean isLooseBoard(ServerLevel level, BlockPos pos) {
        if (!level.dimension().equals(HouseDimensions.INTERIOR)) {
            return false;
        }
        BlockPos base = base(level.getServer());
        return base != null && pos.equals(base.offset(LOOSE_BOARD));
    }

    public static double heat() {
        return heat;
    }

    public static long heartbeatInterval(double agitation) {
        double t=Math.max(0,Math.min(1,agitation/MAX_HEAT));
        return Math.max(7,Math.round(18-11*t));
    }

    // ------------------------------------------------------------------
    // Vibrations

    /** How loud a vibration is to whatever is under the floor. */
    public static double weight(Holder<GameEvent> event) {
        if (event.equals(GameEvent.STEP)) {
            return 4.0D;
        }
        if (event.equals(GameEvent.HIT_GROUND) || event.equals(GameEvent.PROJECTILE_LAND)) {
            return 12.0D;
        }
        if (event.equals(GameEvent.BLOCK_OPEN) || event.equals(GameEvent.BLOCK_CLOSE)
                || event.equals(GameEvent.BLOCK_PLACE) || event.equals(GameEvent.BLOCK_DESTROY)
                || event.equals(GameEvent.BLOCK_CHANGE) || event.equals(GameEvent.BLOCK_ACTIVATE)) {
            return 20.0D;
        }
        return 6.0D;
    }

    /** Whether a sculk sensor would ignore this from its source (sneaking, wool, and so on). */
    public static boolean isSilent(Entity source, Holder<GameEvent> event) {
        return source.dampensVibrations()
                || (source.isSteppingCarefully() && event.is(GameEventTags.IGNORE_VIBRATIONS_SNEAKING));
    }

    public static void onGameEvent(VanillaGameEvent event) {
        Level level = event.getLevel();
        if (level.isClientSide() || !level.dimension().equals(HouseDimensions.INTERIOR) || level.getServer() == null) {
            return;
        }
        BlockPos base = base(level.getServer());
        if (base == null
                || !interior(base).contains(event.getEventPosition())
                || LabyrinthData.get(level.getServer()).isCompleted(ID)) {
            return;
        }
        Entity source = event.getContext().sourceEntity();
        if (source instanceof Projectile projectile && projectile.getOwner() != null) {
            source = projectile.getOwner();
        }
        if (!(source instanceof Player player) || !player.isAlive() || player.isSpectator() || isSilent(source, event.getVanillaEvent())) {
            return;
        }
        heat = Math.min(MAX_HEAT, heat + weight(event.getVanillaEvent()));
    }

    // ------------------------------------------------------------------
    // The heartbeat

    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        ServerLevel level = server.getLevel(HouseDimensions.INTERIOR);
        BlockPos base = base(server);
        if (level == null || base == null || level.players().isEmpty()) {
            heat = 0.0D;
            return;
        }
        if (server.getTickCount() % 20 == 0) keepHatchet(server, level, base);
        if (LabyrinthData.get(server).isCompleted(ID)) {
            return;
        }
        List<ServerPlayer> inside = new ArrayList<>();
        AABB room = interior(base);
        for (ServerPlayer player : level.players()) {
            if (player.isAlive()&&!player.isSpectator()&&room.contains(player.position())) {
                inside.add(player);
            }
        }
        if (inside.isEmpty()) {
            heat = Math.max(0.0D, heat - 0.5D);
            return;
        }

        Vec3 heart = Vec3.atCenterOf(base.offset(LOOSE_BOARD));
        if (heat >= MAX_HEAT) {
            // The lights go out.
            heat = 0.0D;
            for (ServerPlayer player : inside) {
                play(player, LabyrinthRegistry.FLOORBOARD_HEARTBEAT.get(), heart, 1.7F, 1.18F);
                LabyrinthDoors.sendBack(player, 2, 24, 30);
            }
            TheOldestHouse.LOGGER.info("The heartbeat under the floorboards reached its peak.");
            return;
        }
        heat = Math.max(0.0D, heat - DECAY_PER_TICK);

        long now = level.getGameTime();
        if (now >= nextBeat) {
            float t = (float) (heat / MAX_HEAT);
            for (ServerPlayer player : inside) {
                play(player, LabyrinthRegistry.FLOORBOARD_HEARTBEAT.get(), heart, 0.65F + 0.8F * t, 0.95F + 0.23F * t);
            }
            nextBeat = now + heartbeatInterval(heat);
        }
    }

    private static void play(ServerPlayer player, SoundEvent sound, Vec3 at, float volume, float pitch) {
        player.connection.send(new ClientboundSoundPacket(
                BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound), SoundSource.AMBIENT,
                at.x, at.y, at.z, volume, pitch, player.getRandom().nextLong()));
    }

    // ------------------------------------------------------------------
    // The loose board, and not cheating

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || player.isSpectator() || !isLooseBoard(player.serverLevel(), event.getPos())) {
            return;
        }
        ItemStack stack = event.getItemStack();
        if (!stack.is(ItemTags.AXES)) {
            // The board shifts a little under a hand, and the beat skips.
            player.serverLevel().playSound(null, event.getPos(), SoundEvents.WOOD_HIT, SoundSource.BLOCKS, 0.4F, 0.7F);
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        pry(player, stack, event.getHand());
    }

    /** Attack is an authored pry action, so a generic mining veto cannot swallow the puzzle. */
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.isSpectator()
                || event.getAction()!=PlayerInteractEvent.LeftClickBlock.Action.START
                || !isLooseBoard(player.serverLevel(), event.getPos())
                || !player.getMainHandItem().is(ItemTags.AXES)) return;
        event.setCanceled(true);
        pry(player, player.getMainHandItem(), InteractionHand.MAIN_HAND);
    }

    public static boolean pry(ServerPlayer player, ItemStack axe, InteractionHand hand) {
        ServerLevel level = player.serverLevel();
        LabyrinthData data = LabyrinthData.get(level.getServer());
        BlockPos base = base(level.getServer());
        if (base == null || !level.dimension().equals(HouseDimensions.INTERIOR) || player.isSpectator() || !axe.is(ItemTags.AXES)) {
            return false;
        }
        BlockPos board = base.offset(LOOSE_BOARD);
        if (data.isCompleted(ID) || !level.getBlockState(board).is(HouseBlocks.LOOSE_FLOORBOARD.get())
                || board.distToCenterSqr(player.position())>36) {
            return false;
        }
        level.setBlock(board, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        level.playSound(null, board, SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.BLOCKS, 0.7F, 1.4F);
        axe.hurtAndBreak(1, player, hand==InteractionHand.MAIN_HAND?EquipmentSlot.MAINHAND:EquipmentSlot.OFFHAND);
        heat = 0; nextBeat = 0;

        ItemEntity note = new ItemEntity(level, board.getX() + 0.5D, board.getY() + 0.1D, board.getZ() + 0.5D, caregiversNote());
        note.setDeltaMovement(Vec3.ZERO);
        note.setNoPickUpDelay();
        level.addFreshEntity(note);
        data.setCompleted(ID, true);
        player.connection.send(new ClientboundStopSoundPacket(LabyrinthRegistry.HEARTBEAT_ID, SoundSource.AMBIENT));
        for(ServerPlayer listener:level.players())if(!listener.getUUID().equals(player.getUUID())&&interior(base).contains(listener.position()))
            listener.connection.send(new ClientboundStopSoundPacket(LabyrinthRegistry.HEARTBEAT_ID, SoundSource.AMBIENT));
        WitnessAccount.resolve(player, WitnessAccount.Story.FLOORBOARDS, "pried");
        TheOldestHouse.LOGGER.info("{} pried up the loose floorboard.", player.getGameProfile().getName());
        return true;
    }

    /** The loose board comes up under an axe and stays put, quietly, under anything else; the rest of the room is loud. */
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !level.dimension().equals(HouseDimensions.INTERIOR)) {
            return;
        }
        BlockPos base = base(level.getServer());
        if (base == null || !isInShell(base, event.getPos())) {
            return;
        }
        event.setCanceled(true);
        if (event.getPos().equals(base.offset(LOOSE_BOARD)) && event.getPlayer() instanceof ServerPlayer player) {
            // Working at the board where the heartbeat is was never the noise; only an axe lifts it.
            if (player.getMainHandItem().is(ItemTags.AXES)) pry(player, player.getMainHandItem(), InteractionHand.MAIN_HAND);
            return;
        }
        // Trying to dig or break your way out is loud.
        heat = Math.min(MAX_HEAT, heat + BREAK_HEAT);
    }

    // ------------------------------------------------------------------
    // The hatchet on the chopping block

    /** The room's own hatchet: an ordinary axe the House knows as its own. */
    public static ItemStack hatchet() {
        ItemStack stack = new ItemStack(Items.IRON_AXE);
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putBoolean(AXE_KEY, true));
        return stack;
    }

    public static boolean isHatchet(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.copyTag().getBoolean(AXE_KEY);
    }

    /**
     * The room is shared, so its one hatchet never leaves it: in someone's hands inside the room, or lying on the floor
     * there, it stays where it is; anywhere else, the House takes it back and lays it on the block again.
     */
    public static void keepHatchet(MinecraftServer server, ServerLevel level, BlockPos base) {
        BlockPos block = base.offset(AXE_BLOCK);
        if (!level.hasChunkAt(block) || !level.areEntitiesLoaded(ChunkPos.asLong(block.getX() >> 4, block.getZ() >> 4))
                || !level.getBlockState(block).is(Blocks.STRIPPED_SPRUCE_LOG)) return;
        AABB room = interior(base);
        if (!level.getEntitiesOfClass(ItemFrame.class, room.inflate(1), frame -> frame.getTags().contains(AXE_TAG) && isHatchet(frame.getItem())).isEmpty()) return;
        if (!level.getEntitiesOfClass(ItemEntity.class, room, item -> isHatchet(item.getItem())).isEmpty()) return;
        for (ServerPlayer player : level.players()) {
            if (player.isAlive() && !player.isSpectator() && room.contains(player.position()) && carriesHatchet(player)) return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) takeHatchet(player);
        for (ItemFrame stale : level.getEntitiesOfClass(ItemFrame.class, room.inflate(1), frame -> frame.getTags().contains(AXE_TAG))) stale.discard();
        ItemFrame frame = new ItemFrame(level, block.above(), Direction.UP);
        frame.addTag(AXE_TAG);
        frame.setInvisible(true);
        frame.setItem(hatchet(), false);
        frame.setRotation(3);
        level.addFreshEntity(frame);
    }

    private static boolean carriesHatchet(ServerPlayer player) {
        if (isHatchet(player.containerMenu.getCarried())) return true;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            if (isHatchet(player.getInventory().getItem(slot))) return true;
        }
        return false;
    }

    private static void takeHatchet(ServerPlayer player) {
        if (isHatchet(player.containerMenu.getCarried())) player.containerMenu.setCarried(ItemStack.EMPTY);
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            if (isHatchet(player.getInventory().getItem(slot))) player.getInventory().setItem(slot, ItemStack.EMPTY);
        }
    }

    /** Picking the hatchet up off the block, by either hand: it comes away whole, and the frame holding it goes. */
    public static boolean pickUpHatchet(Player entity, Entity target) {
        if (!(entity instanceof ServerPlayer player) || !(target instanceof ItemFrame frame) || !frame.getTags().contains(AXE_TAG)) return false;
        if (!player.isAlive() || player.isSpectator() || frame.isRemoved()) return true;
        ItemStack axe = frame.getItem().copy();
        frame.setItem(ItemStack.EMPTY, false);
        frame.discard();
        if (!axe.isEmpty() && !player.getInventory().add(axe)) player.drop(axe, false);
        player.serverLevel().playSound(null, frame.blockPosition(), SoundEvents.WOOD_HIT, SoundSource.BLOCKS, 0.3F, 1.3F);
        return true;
    }

    public static void onAttackEntity(AttackEntityEvent event) {
        if (pickUpHatchet(event.getEntity(), event.getTarget())) event.setCanceled(true);
    }

    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        if (pickUpHatchet(event.getEntity(), event.getTarget())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    // ------------------------------------------------------------------
    // Testing

    public static void clearAll() {
        heat = 0.0D;
        nextBeat = 0L;
    }

    /** Puts the room back as it was, board and all, and lets it be dealt again. */
    public static boolean reset(MinecraftServer server) {
        ServerLevel level = server.getLevel(HouseDimensions.INTERIOR);
        BlockPos base = base(server);
        if (level == null || base == null) {
            return false;
        }
        LabyrinthData data = LabyrinthData.get(server);
        data.setCompleted(ID, false);
        build(level, base, true);
        return true;
    }

    public static ItemStack caregiversNote() {
        ItemStack book = HouseWriting.book(
                "Kept by his bed",
                "his caregiver",
                HouseWriting.WritingStyle.PLAIN,
                List.of(
                        "He says the eye is fine. He says it the way you say a door is locked.\n\n"
                                + "I sit with him until he sleeps. I only look to see that he is breathing.",
                        "Tonight I heard it through the floor, under his bed, under my feet. Slow, like his.\n\n"
                                + "He was already asleep.\n\nI am taking the board up to show him there is nothing there."
                )
        );
        return VignetteYields.mark(book, ID);
    }
}
