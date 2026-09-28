package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import net.minecraft.core.BlockPos;
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

    private HouseRoomDoorClient() {
    }

    public static void set(BlockPos lowerDoor, boolean present) {
        door = present ? lowerDoor.immutable() : null;
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
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
