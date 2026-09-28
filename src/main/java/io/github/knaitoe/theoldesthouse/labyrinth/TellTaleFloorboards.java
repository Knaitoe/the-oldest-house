package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.VanillaGameEvent;
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
 * Cheating: the room cannot be dug out of, built in, or its floor broken;
 * trying is loud.
 */
public final class TellTaleFloorboards {
    public static final String ID = "floorboards";

    /** The loose board, relative to the room's base (the floor is at y -1). */
    public static final BlockPos LOOSE_BOARD = new BlockPos(2, -1, 5);

    public static final double MAX_HEAT = 100.0D;
    private static final double DECAY_PER_TICK = 0.08D;
    private static final double PRY_HEAT = 30.0D;
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
        LabyrinthBuilder.room(level, base, -5, 5, 3, 0, 8,
                Blocks.BROWN_TERRACOTTA.defaultBlockState(),
                Blocks.SPRUCE_PLANKS.defaultBlockState(),
                Blocks.DARK_OAK_PLANKS.defaultBlockState());

        // Under the boards: sculk, and a few sensors that click when he stirs.
        for (int x = -5; x <= 5; x++) {
            for (int z = 0; z <= 8; z++) {
                level.setBlock(base.offset(x, -2, z), Blocks.SCULK.defaultBlockState(), flags);
                level.setBlock(base.offset(x, -3, z), Blocks.BROWN_TERRACOTTA.defaultBlockState(), flags);
            }
        }
        for (BlockPos sensor : List.of(new BlockPos(-3, -2, 2), new BlockPos(3, -2, 6), new BlockPos(0, -2, 4))) {
            level.setBlock(base.offset(sensor), Blocks.SCULK_SENSOR.defaultBlockState(), flags);
        }

        level.setBlock(base.offset(LOOSE_BOARD), withBoard
                ? Blocks.DARK_OAK_PLANKS.defaultBlockState()
                : Blocks.AIR.defaultBlockState(), flags);

        // His bed, made; one candle on a barrel; a chair turned towards the bed.
        LabyrinthBuilder.bed(level, base.offset(-4, 0, 6), Direction.SOUTH, Blocks.WHITE_BED);
        level.setBlock(base.offset(-3, 0, 8), LabyrinthBuilder.barrel(Direction.UP), flags);
        level.setBlock(base.offset(-3, 1, 8), LabyrinthBuilder.candle(3, true), flags);
        level.setBlock(base.offset(-2, 0, 5), LabyrinthBuilder.stairs(Blocks.DARK_OAK_STAIRS, Direction.EAST), flags);
        // A wardrobe by the door, and a shuttered lantern by the bed.
        level.setBlock(base.offset(4, 0, 0), LabyrinthBuilder.barrel(Direction.WEST), flags);
        level.setBlock(base.offset(4, 1, 0), LabyrinthBuilder.barrel(Direction.WEST), flags);
        LabyrinthBuilder.hangLantern(level, base.offset(-4, 3, 4), true);

        LabyrinthBuilder.doors(level, base, LabyrinthPlace.FLOORBOARDS);
        heat = 0.0D;
    }

    private static BlockPos base() {
        return LabyrinthPlace.FLOORBOARDS.base();
    }

    /** The room's inside, where vibrations count and players are "in" it. */
    private static AABB interior() {
        BlockPos b = base();
        return new AABB(b.getX() - 5, b.getY() - 2, b.getZ(), b.getX() + 6, b.getY() + 4, b.getZ() + 9);
    }

    /** The room with its walls, floor and ceiling: nothing here may be broken or built. */
    public static boolean isInShell(BlockPos pos) {
        BlockPos rel = pos.subtract(base());
        return rel.getX() >= -6 && rel.getX() <= 6
                && rel.getY() >= -3 && rel.getY() <= 4
                && rel.getZ() >= -1 && rel.getZ() <= 9;
    }

    private static Vec3 heart() {
        return Vec3.atCenterOf(base().offset(LOOSE_BOARD));
    }

    public static double heat() {
        return heat;
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
        if (level.isClientSide() || !level.dimension().equals(HouseDimensions.LABYRINTH) || level.getServer() == null) {
            return;
        }
        if (!interior().contains(event.getEventPosition())
                || LabyrinthData.get(level.getServer()).isCompleted(ID)) {
            return;
        }
        Entity source = event.getContext().sourceEntity();
        if (source instanceof Projectile projectile && projectile.getOwner() != null) {
            source = projectile.getOwner();
        }
        if (!(source instanceof Player) || isSilent(source, event.getVanillaEvent())) {
            return;
        }
        heat = Math.min(MAX_HEAT, heat + weight(event.getVanillaEvent()));
    }

    // ------------------------------------------------------------------
    // The heartbeat

    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        ServerLevel level = server.getLevel(HouseDimensions.LABYRINTH);
        if (level == null || level.players().isEmpty()) {
            heat = 0.0D;
            return;
        }
        if (LabyrinthData.get(server).isCompleted(ID)) {
            return;
        }
        List<ServerPlayer> inside = new ArrayList<>();
        AABB room = interior();
        for (ServerPlayer player : level.players()) {
            if (room.contains(player.position())) {
                inside.add(player);
            }
        }
        if (inside.isEmpty()) {
            heat = Math.max(0.0D, heat - 0.5D);
            return;
        }

        if (heat >= MAX_HEAT) {
            // The lights go out.
            heat = 0.0D;
            for (ServerPlayer player : inside) {
                play(player, SoundEvents.WARDEN_HEARTBEAT, heart(), 2.0F, 1.3F);
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
                play(player, SoundEvents.WARDEN_HEARTBEAT, heart(), 0.35F + 1.1F * t, 0.9F + 0.35F * t);
            }
            nextBeat = now + Math.max(7L, Math.round(40.0D - 32.0D * t));
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
                || !event.getLevel().dimension().equals(HouseDimensions.LABYRINTH)
                || !event.getPos().equals(base().offset(LOOSE_BOARD))) {
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
        pry(player, stack);
    }

    private static void pry(ServerPlayer player, ItemStack axe) {
        ServerLevel level = player.serverLevel();
        LabyrinthData data = LabyrinthData.get(level.getServer());
        BlockPos board = base().offset(LOOSE_BOARD);
        if (data.isCompleted(ID) || !level.getBlockState(board).is(Blocks.DARK_OAK_PLANKS)) {
            return;
        }
        level.setBlock(board, Blocks.AIR.defaultBlockState(), net.minecraft.world.level.block.Block.UPDATE_ALL);
        level.playSound(null, board, SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.BLOCKS, 0.7F, 1.4F);
        axe.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        heat = Math.min(MAX_HEAT, heat + PRY_HEAT);

        ItemEntity note = new ItemEntity(level, board.getX() + 0.5D, board.getY() + 0.1D, board.getZ() + 0.5D, caregiversNote());
        note.setDeltaMovement(Vec3.ZERO);
        note.setNoPickUpDelay();
        level.addFreshEntity(note);
        data.setCompleted(ID, true);
        TheOldestHouse.LOGGER.info("{} pried up the loose floorboard.", player.getGameProfile().getName());
    }

    public static void onBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || !level.dimension().equals(HouseDimensions.LABYRINTH)
                || !isInShell(event.getPos())) {
            return;
        }
        event.setCanceled(true);
        if (event.getPos().equals(base().offset(LOOSE_BOARD))
                && event.getPlayer() instanceof ServerPlayer player
                && player.getMainHandItem().is(ItemTags.AXES)) {
            pry(player, player.getMainHandItem());
            return;
        }
        // Trying to dig or break your way out is loud.
        heat = Math.min(MAX_HEAT, heat + BREAK_HEAT);
    }

    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof ServerLevel level
                && level.dimension().equals(HouseDimensions.LABYRINTH)
                && isInShell(event.getPos())) {
            event.setCanceled(true);
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
        ServerLevel level = server.getLevel(HouseDimensions.LABYRINTH);
        if (level == null) {
            return false;
        }
        LabyrinthData data = LabyrinthData.get(server);
        data.setCompleted(ID, false);
        build(level, base(), true);
        return true;
    }

    public static ItemStack caregiversNote() {
        List<Filterable<Component>> pages = List.of(
                Filterable.passThrough(Component.literal(
                        "He says the eye is fine. He says it the way you say a door is locked.\n\n"
                                + "I sit with him until he sleeps. I only look to see that he is breathing.")),
                Filterable.passThrough(Component.literal(
                        "Tonight I heard it through the floor, under his bed, under my feet. Slow, like his.\n\n"
                                + "He was already asleep.\n\nI am taking the board up to show him there is nothing there."))
        );
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        book.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(
                Filterable.passThrough("Kept by his bed"), "his caregiver", 0, pages, true));
        return book;
    }
}
