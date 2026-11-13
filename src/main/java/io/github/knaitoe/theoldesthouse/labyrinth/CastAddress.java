package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.560: once a tick, after every scene has turned its people, whoever is talking to a reader is turned to them
 * ({@link LiteraryActor#faceAddressees}). Last, so that a scene which sets a head every tick does not undo it while a line is up.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class CastAddress {
    private CastAddress(){}
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void afterScenes(ServerTickEvent.Post e){LiteraryActor.faceAddressees();}
}
