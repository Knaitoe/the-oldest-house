package io.github.knaitoe.theoldesthouse;

import com.mojang.logging.LogUtils;
import io.github.knaitoe.theoldesthouse.command.HouseCommands;
import io.github.knaitoe.theoldesthouse.house.HouseBetweenRoom;
import io.github.knaitoe.theoldesthouse.house.HouseBlocks;
import io.github.knaitoe.theoldesthouse.house.HouseChunkKeeper;
import io.github.knaitoe.theoldesthouse.house.HouseConfig;
import io.github.knaitoe.theoldesthouse.house.HouseDays;
import io.github.knaitoe.theoldesthouse.house.HouseLabyrinth;
import io.github.knaitoe.theoldesthouse.house.HouseLifecycleEvents;
import io.github.knaitoe.theoldesthouse.house.HouseExteriorEntityMirror;
import io.github.knaitoe.theoldesthouse.house.HouseMemory;
import io.github.knaitoe.theoldesthouse.house.HouseMirrorSyncEvents;
import io.github.knaitoe.theoldesthouse.house.HouseProxyEntityEvacuation;
import io.github.knaitoe.theoldesthouse.house.HouseShifts;
import io.github.knaitoe.theoldesthouse.house.HouseSoundBridge;
import io.github.knaitoe.theoldesthouse.house.HouseTransitionEvents;
import io.github.knaitoe.theoldesthouse.labyrinth.Growl;
import io.github.knaitoe.theoldesthouse.labyrinth.HideAndClap;
import io.github.knaitoe.theoldesthouse.labyrinth.HarriganVignette;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthDoors;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthRegistry;
import io.github.knaitoe.theoldesthouse.labyrinth.ModelHome;
import io.github.knaitoe.theoldesthouse.labyrinth.RedRoom;
import io.github.knaitoe.theoldesthouse.labyrinth.TellTaleFloorboards;
import io.github.knaitoe.theoldesthouse.network.HouseNetwork;
import io.github.knaitoe.theoldesthouse.opening.OpeningConfig;
import io.github.knaitoe.theoldesthouse.opening.OpeningRegistry;
import io.github.knaitoe.theoldesthouse.opening.OpeningSequence;
import net.neoforged.bus.api.EventPriority;
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
        HouseBlocks.register(modEventBus);
        LabyrinthRegistry.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.SERVER, OpeningConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.SERVER, HouseConfig.SPEC, MOD_ID + "-house-server.toml");

        NeoForge.EVENT_BUS.addListener(HouseCommands::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(HouseLifecycleEvents::onPlayerWakeUp);
        NeoForge.EVENT_BUS.addListener(HouseDays::onSleepFinished);
        NeoForge.EVENT_BUS.addListener(HouseLifecycleEvents::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(HouseLifecycleEvents::onServerStarted);
        NeoForge.EVENT_BUS.addListener(HouseLifecycleEvents::onServerStopped);
        NeoForge.EVENT_BUS.addListener(HouseTransitionEvents::onPlayerTick);
        // The manor's doors are crossed by the handle, before anything else sees the click.
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, HouseTransitionEvents::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(HouseTransitionEvents::onServerTick);
        NeoForge.EVENT_BUS.addListener(HouseChunkKeeper::onServerTick);
        NeoForge.EVENT_BUS.addListener(HouseTransitionEvents::onPlayerLoggedOut);
        NeoForge.EVENT_BUS.addListener(HouseProxyEntityEvacuation::onServerTick);
        NeoForge.EVENT_BUS.addListener(HouseExteriorEntityMirror::onServerTick);
        NeoForge.EVENT_BUS.addListener(HouseExteriorEntityMirror::onEntityInteract);
        NeoForge.EVENT_BUS.addListener(HouseExteriorEntityMirror::onEntityInteractSpecific);
        NeoForge.EVENT_BUS.addListener(HouseExteriorEntityMirror::onAttack);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, HouseSoundBridge::onSoundAtPosition);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, HouseSoundBridge::onSoundAtEntity);
        NeoForge.EVENT_BUS.addListener(HouseLabyrinth::onCanPlayerSleep);
        NeoForge.EVENT_BUS.addListener(HouseLabyrinth::onSetSpawn);

        // Doors that lead elsewhere (the labyrinth's, the hallway's far door,
        // test doors) are handled before anything else sees the click.
        NeoForge.EVENT_BUS.addListener(LabyrinthDoors::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(LabyrinthDoors::onBreak);
        NeoForge.EVENT_BUS.addListener(LabyrinthDoors::onPlace);
        NeoForge.EVENT_BUS.addListener(LabyrinthDoors::onExplosion);
        NeoForge.EVENT_BUS.addListener(LabyrinthDoors::onServerTick);
        NeoForge.EVENT_BUS.addListener(LabyrinthDoors::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(LabyrinthDoors::onPlayerLoggedOut);
        // The first vignette: The Tell-Tale Heart's floorboards.
        NeoForge.EVENT_BUS.addListener(TellTaleFloorboards::onGameEvent);
        NeoForge.EVENT_BUS.addListener(TellTaleFloorboards::onServerTick);
        NeoForge.EVENT_BUS.addListener(TellTaleFloorboards::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(TellTaleFloorboards::onBreak);
        NeoForge.EVENT_BUS.addListener(RedRoom::onServerTick);
        NeoForge.EVENT_BUS.addListener(RedRoom::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(RedRoom::onNeighborNotify);
        NeoForge.EVENT_BUS.addListener(RedRoom::onPiston);
        NeoForge.EVENT_BUS.addListener(Growl::onServerTick);
        NeoForge.EVENT_BUS.addListener(Growl::onPlayerLoggedOut);
        NeoForge.EVENT_BUS.addListener(HideAndClap::onServerTick);
        NeoForge.EVENT_BUS.addListener(HideAndClap::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(HideAndClap::onAttackEntity);
        NeoForge.EVENT_BUS.addListener(HideAndClap::onServerStopping);
        NeoForge.EVENT_BUS.addListener(ModelHome::onServerTick);
        NeoForge.EVENT_BUS.addListener(ModelHome::onAttackEntity);
        NeoForge.EVENT_BUS.addListener(HarriganVignette::onServerTick);
        NeoForge.EVENT_BUS.addListener(HarriganVignette::onContainerClose);
        NeoForge.EVENT_BUS.addListener(HarriganVignette::onEntityInteract);
        NeoForge.EVENT_BUS.addListener(HarriganVignette::onEntityInteractSpecific);
        NeoForge.EVENT_BUS.addListener(HarriganVignette::onAttackEntity);
        NeoForge.EVENT_BUS.addListener(HarriganVignette::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(HarriganVignette::onServerChat);
        NeoForge.EVENT_BUS.addListener(HarriganVignette::onPlayerLoggedOut);

        // The room between rooms: its door is handled before the mirror sees the click.
        NeoForge.EVENT_BUS.addListener(HouseBetweenRoom::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(HouseBetweenRoom::onRightClickInPocket);
        NeoForge.EVENT_BUS.addListener(HouseBetweenRoom::onBreak);
        NeoForge.EVENT_BUS.addListener(HouseBetweenRoom::onPlace);
        NeoForge.EVENT_BUS.addListener(HouseBetweenRoom::onExplosion);
        NeoForge.EVENT_BUS.addListener(HouseBetweenRoom::onPiston);
        NeoForge.EVENT_BUS.addListener(HouseBetweenRoom::onServerTick);
        NeoForge.EVENT_BUS.addListener(HouseBetweenRoom::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(HouseBetweenRoom::onPlayerLoggedOut);

        // Subtle changes: the house remembering its chests, and its echoes.
        NeoForge.EVENT_BUS.addListener(HouseMemory::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(HouseShifts::onServerTick);

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
