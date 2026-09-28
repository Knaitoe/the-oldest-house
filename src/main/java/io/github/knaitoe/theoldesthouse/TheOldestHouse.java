package io.github.knaitoe.theoldesthouse;

import com.mojang.logging.LogUtils;
import io.github.knaitoe.theoldesthouse.command.HouseCommands;
import io.github.knaitoe.theoldesthouse.house.HouseLabyrinth;
import io.github.knaitoe.theoldesthouse.house.HouseLifecycleEvents;
import io.github.knaitoe.theoldesthouse.house.HouseExteriorEntityMirror;
import io.github.knaitoe.theoldesthouse.house.HouseMirrorSyncEvents;
import io.github.knaitoe.theoldesthouse.house.HouseProxyEntityEvacuation;
import io.github.knaitoe.theoldesthouse.house.HouseTransitionEvents;
import io.github.knaitoe.theoldesthouse.network.HouseNetwork;
import io.github.knaitoe.theoldesthouse.opening.OpeningConfig;
import io.github.knaitoe.theoldesthouse.opening.OpeningRegistry;
import io.github.knaitoe.theoldesthouse.opening.OpeningSequence;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(TheOldestHouse.MOD_ID)
public final class TheOldestHouse {
    public static final String MOD_ID = "the_oldest_house";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TheOldestHouse(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(HouseNetwork::registerPayloads);
        OpeningRegistry.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.SERVER, OpeningConfig.SPEC);

        NeoForge.EVENT_BUS.addListener(HouseCommands::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(HouseLifecycleEvents::onPlayerWakeUp);
        NeoForge.EVENT_BUS.addListener(HouseLifecycleEvents::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(HouseLifecycleEvents::onServerStarted);
        NeoForge.EVENT_BUS.addListener(HouseLifecycleEvents::onServerStopped);
        NeoForge.EVENT_BUS.addListener(HouseTransitionEvents::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(HouseTransitionEvents::onPlayerLoggedOut);
        NeoForge.EVENT_BUS.addListener(HouseProxyEntityEvacuation::onServerTick);
        NeoForge.EVENT_BUS.addListener(HouseExteriorEntityMirror::onServerTick);
        NeoForge.EVENT_BUS.addListener(HouseExteriorEntityMirror::onEntityInteract);
        NeoForge.EVENT_BUS.addListener(HouseExteriorEntityMirror::onEntityInteractSpecific);
        NeoForge.EVENT_BUS.addListener(HouseExteriorEntityMirror::onAttack);
        NeoForge.EVENT_BUS.addListener(HouseLabyrinth::onCanPlayerSleep);
        NeoForge.EVENT_BUS.addListener(HouseLabyrinth::onSetSpawn);

        NeoForge.EVENT_BUS.addListener(HouseMirrorSyncEvents::onBreak);
        NeoForge.EVENT_BUS.addListener(HouseMirrorSyncEvents::onExplosion);
        NeoForge.EVENT_BUS.addListener(HouseMirrorSyncEvents::onPiston);
        NeoForge.EVENT_BUS.addListener(HouseMirrorSyncEvents::onPlace);
        NeoForge.EVENT_BUS.addListener(HouseMirrorSyncEvents::onRightClick);
        NeoForge.EVENT_BUS.addListener(HouseMirrorSyncEvents::onServerTick);

        OpeningSequence.register(NeoForge.EVENT_BUS);

        LOGGER.info("The Oldest House prototype initialized.");
    }
}
