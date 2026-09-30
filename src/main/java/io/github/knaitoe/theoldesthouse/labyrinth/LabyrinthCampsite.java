package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Someone else made it this far.
 *
 * The barrel is a finite shared cache. Resting beside the fire is a one-time
 * recovery per player, persisted with LabyrinthData so leaving and returning
 * cannot turn the abandoned camp into an infinite healing station.
 */
public final class LabyrinthCampsite {
    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    private static final BlockState WALL = Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState();
    private static final BlockState FLOOR = Blocks.SMOOTH_STONE.defaultBlockState();
    private static final BlockState CEILING = Blocks.STONE.defaultBlockState();
    private static final String STATE = "explorer_camp";
    private static final Map<UUID, Integer> REST_TICKS = new HashMap<>();

    public static final BlockPos FIRE = new BlockPos(0, 0, -8);
    public static final BlockPos CACHE = new BlockPos(4, 0, -9);

    private LabyrinthCampsite() {
    }

    public static void build(MinecraftServer server, ServerLevel level, BlockPos base) {
        LabyrinthBuilder.room(level, base, -5, 5, 4, -14, -1, WALL, FLOOR, CEILING);

        // Worn patch where people actually stayed.
        for (int x = -3; x <= 3; x++) {
            for (int z = -11; z <= -5; z++) {
                level.setBlock(base.offset(x, -1, z),
                        (Math.floorMod(x * 17 + z * 31, 5) == 0
                                ? Blocks.COARSE_DIRT
                                : Blocks.PACKED_MUD).defaultBlockState(), FLAGS);
            }
        }

        level.setBlock(base.offset(FIRE),
                Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true), FLAGS);
        // Bedrolls, not beds: nobody has successfully made this home.
        for (int x = -3; x <= -1; x++) {
            level.setBlock(base.offset(x, 0, -10), Blocks.BROWN_CARPET.defaultBlockState(), FLAGS);
        }
        for (int x = 1; x <= 3; x++) {
            level.setBlock(base.offset(x, 0, -6), Blocks.GRAY_CARPET.defaultBlockState(), FLAGS);
        }
        level.setBlock(base.offset(-4, 0, -7), Blocks.CRAFTING_TABLE.defaultBlockState(), FLAGS);
        level.setBlock(base.offset(4, 0, -7), Blocks.CAULDRON.defaultBlockState(), FLAGS);
        level.setBlock(base.offset(CACHE), Blocks.BARREL.defaultBlockState(), FLAGS);

        if (level.getBlockEntity(base.offset(CACHE)) instanceof Container cache) {
            // Shared and finite. Rebuilding a Labyrinth version can recreate
            // the physical camp, but ordinary revisits never refill it.
            boolean empty = true;
            for (int i = 0; i < cache.getContainerSize(); i++) {
                empty &= cache.getItem(i).isEmpty();
            }
            if (empty) {
                cache.setItem(0, new ItemStack(Items.BREAD, 6));
                cache.setItem(1, new ItemStack(Items.BAKED_POTATO, 5));
                cache.setItem(2, new ItemStack(Items.COOKED_BEEF, 3));
                cache.setItem(3, new ItemStack(Items.APPLE, 4));
                cache.setItem(4, new ItemStack(Items.COOKED_COD, 3));
                cache.setItem(5, new ItemStack(Items.TORCH, 8));
                cache.setItem(6, new ItemStack(LabyrinthRegistry.CHALK.get()));
                cache.setItem(7, new ItemStack(LabyrinthRegistry.TRAIL_SPOOL.get()));
                cache.setItem(8, new ItemStack(Items.BONE, 4));
                cache.setItem(9, new ItemStack(Items.COD, 3));
            }
        }

        LabyrinthBuilder.hangLantern(level, base.offset(-4, 4, -3), true);
        LabyrinthBuilder.entrance(level, base, WALL, FLOOR, CEILING);
        LabyrinthBuilder.doors(level, base, LabyrinthPlace.EXPLORER_CAMP);
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % 20 != 0) {
            return;
        }
        ServerLevel level = server.getLevel(HouseDimensions.INTERIOR);
        BlockPos origin = HouseSavedData.get(server).houseOrigin();
        if (level == null || origin == null) {
            REST_TICKS.clear();
            return;
        }
        BlockPos base = LabyrinthPlaces.base(origin, LabyrinthPlace.EXPLORER_CAMP);
        if (base == null) {
            return;
        }

        LabyrinthData data = LabyrinthData.get(server);
        CompoundTag state = data.state(STATE);
        boolean changed = false;

        for (ServerPlayer player : level.players()) {
            UUID id = player.getUUID();
            if (LabyrinthPlaces.placeAt(origin, player.blockPosition()) != LabyrinthPlace.EXPLORER_CAMP
                    || player.distanceToSqr(base.offset(FIRE).getCenter()) > 20.25D) {
                REST_TICKS.remove(id);
                continue;
            }

            String key = "rested_" + id;
            if (state.getBoolean(key)) {
                REST_TICKS.remove(id);
                continue;
            }

            int seconds = REST_TICKS.merge(id, 1, Integer::sum);
            if (seconds >= 5) {
                state.putBoolean(key, true);
                changed = true;
                REST_TICKS.remove(id);
                player.removeEffect(MobEffects.DARKNESS);
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0, true, false, false));
                player.displayClientMessage(
                        Component.literal("For a little while, this feels like a place somebody meant to survive.")
                                .withStyle(ChatFormatting.DARK_GRAY),
                        true
                );
            }
        }

        if (changed) {
            data.setState(STATE, state);
        }
    }

    public static void clearAll() {
        REST_TICKS.clear();
    }
}
