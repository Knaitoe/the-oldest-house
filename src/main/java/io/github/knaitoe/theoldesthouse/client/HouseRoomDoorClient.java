package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * The manor's side of the door to the room between rooms never opens. Left
 * to itself the client would swing it open the moment it is clicked, before
 * the server's reply, and show the bookshelves behind it for a frame. The
 * click is still sent to the server, which moves the player through.
 */
@EventBusSubscriber(modid = TheOldestHouse.MOD_ID, value = Dist.CLIENT)
public final class HouseRoomDoorClient {
    private static volatile BlockPos door;
    private static volatile Set<GlobalPos> sealed = Set.of();

    private HouseRoomDoorClient() {
    }

    public static void set(BlockPos lowerDoor, boolean present) {
        door = present ? lowerDoor.immutable() : null;
    }

    /** Every labyrinth and test door: they lead elsewhere and never open where they stand. */
    public static void setSealed(List<GlobalPos> doors) {
        sealed = Set.copyOf(doors);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide() && !sealed.isEmpty()) {
            ResourceKey<Level> dimension = event.getLevel().dimension();
            BlockPos clicked = event.getPos();
            if (sealed.contains(GlobalPos.of(dimension, clicked)) || sealed.contains(GlobalPos.of(dimension, clicked.below()))) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
                return;
            }
        }
        BlockPos lower = door;
        if (lower == null
                || !event.getLevel().isClientSide()
                || !event.getLevel().dimension().equals(HouseDimensions.INTERIOR)) {
            return;
        }
        BlockPos pos = event.getPos();
        if (pos.equals(lower) || pos.equals(lower.above())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }
}
