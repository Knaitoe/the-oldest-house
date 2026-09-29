package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.HouseTransitionContextState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Bookkeeping for a dimension switch on the client. There is no motion any
 * more: the switch is made where the view is already still, and the frame
 * is simply held (see HouseTransitionClient). The context the server sends
 * is forgotten once the held frame lifts, when the server cancels the
 * switch, or after a timeout if neither happens.
 */
@EventBusSubscriber(modid = TheOldestHouse.MOD_ID, value = Dist.CLIENT)
public final class HouseTransitionMotion {
    private static final long HOLD_TIMEOUT_NANOS = 10_000_000_000L;

    private static int token = -1;
    private static long armedAt;

    private HouseTransitionMotion() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        int armedToken = HouseTransitionContextState.peekToken();
        if (HouseTransitionContextState.isArmed() && armedToken >= 0 && armedToken != token) {
            token = armedToken;
            armedAt = System.nanoTime();
            return;
        }
        if (token >= 0 && System.nanoTime() - armedAt > HOLD_TIMEOUT_NANOS) {
            HouseTransitionContextState.clear(token);
            token = -1;
        }
    }

    /**
     * Nothing about one server's House may survive into the next connection:
     * a stale reveal flag would draw the hallway view over an unrelated world.
     */
    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        HouseSightlineState.clear();
        HouseTransitionContextState.reset();
        token = -1;
    }

    /** The held frame has lifted: this switch is over. */
    public static void beginPost(int transitionToken) {
        HouseTransitionContextState.clear(transitionToken);
        if (transitionToken == token) {
            token = -1;
        }
    }
}
