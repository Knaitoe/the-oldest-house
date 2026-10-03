package io.github.knaitoe.theoldesthouse;

import com.mojang.logging.LogUtils;
import io.github.knaitoe.theoldesthouse.command.HouseCommands;
import io.github.knaitoe.theoldesthouse.gametest.ShutdownWatch;
import io.github.knaitoe.theoldesthouse.house.HouseBetweenRoom;
import io.github.knaitoe.theoldesthouse.house.HouseBlockEntities;
import io.github.knaitoe.theoldesthouse.house.HouseBlocks;
import io.github.knaitoe.theoldesthouse.house.HouseChunkKeeper;
import io.github.knaitoe.theoldesthouse.house.HouseConfig;
import io.github.knaitoe.theoldesthouse.house.HouseDays;
import io.github.knaitoe.theoldesthouse.house.HouseSitting;
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
import io.github.knaitoe.theoldesthouse.labyrinth.ClapGhostRegistry;
import io.github.knaitoe.theoldesthouse.labyrinth.HarriganVignette;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthDoors;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthHazards;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthLighting;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthDoorLeaks;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthCampsite;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthRegistry;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthSpawnRules;
import io.github.knaitoe.theoldesthouse.labyrinth.ModelHome;
import io.github.knaitoe.theoldesthouse.labyrinth.MotherOfStrays;
import io.github.knaitoe.theoldesthouse.labyrinth.MotherRegistry;
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
        HouseBlockEntities.register(modEventBus);
        LabyrinthRegistry.register(modEventBus);
        io.github.knaitoe.theoldesthouse.labyrinth.DrownedTownRegistry.register(modEventBus);
        MotherRegistry.register(modEventBus);
        io.github.knaitoe.theoldesthouse.labyrinth.FinaleRegistry.register(modEventBus);
        io.github.knaitoe.theoldesthouse.labyrinth.NovelRegistry.register(modEventBus);
        io.github.knaitoe.theoldesthouse.labyrinth.ClassicsRegistry.register(modEventBus);
        io.github.knaitoe.theoldesthouse.labyrinth.FinaleLoot.register(modEventBus);
        ClapGhostRegistry.register(modEventBus);
        io.github.knaitoe.theoldesthouse.labyrinth.GoatmanRegistry.register(modEventBus);
        HouseSitting.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(HouseSitting::onRightClick);
        NeoForge.EVENT_BUS.addListener(io.github.knaitoe.theoldesthouse.house.HouseMarginalia::onRightClick);
        modContainer.registerConfig(ModConfig.Type.SERVER, OpeningConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.SERVER, HouseConfig.SPEC, MOD_ID + "-house-server.toml");

        NeoForge.EVENT_BUS.addListener(HouseCommands::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(HouseLifecycleEvents::onPlayerWakeUp);
        NeoForge.EVENT_BUS.addListener(HouseDays::onSleepFinished);
        NeoForge.EVENT_BUS.addListener(HouseLifecycleEvents::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(HouseLifecycleEvents::onServerStarted);
        NeoForge.EVENT_BUS.addListener(HouseLifecycleEvents::onServerStopped);
        // Only on the GameTest server: explains a shutdown that stalls.
        NeoForge.EVENT_BUS.addListener(ShutdownWatch::onServerStopping);
        NeoForge.EVENT_BUS.addListener(ShutdownWatch::onServerStopped);
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
        NeoForge.EVENT_BUS.addListener(LabyrinthSpawnRules::onEntityJoinLevel);
        NeoForge.EVENT_BUS.addListener(LabyrinthHazards::onServerTick);
        NeoForge.EVENT_BUS.addListener(LabyrinthLighting::onServerTick);
        NeoForge.EVENT_BUS.addListener(LabyrinthDoorLeaks::onServerTick);
        NeoForge.EVENT_BUS.addListener(LabyrinthCampsite::onServerTick);
        NeoForge.EVENT_BUS.addListener(io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthEncounters::onServerTick);
        NeoForge.EVENT_BUS.addListener(io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthEncounters::onInteract);
        NeoForge.EVENT_BUS.addListener(MotherOfStrays::onServerTick);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, MotherOfStrays::onItemExpire);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, MotherOfStrays::onItemToss);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, MotherOfStrays::onLivingDrops);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, MotherOfStrays::onPetDeath);
        NeoForge.EVENT_BUS.addListener(MotherOfStrays::onEntityInteract);
        NeoForge.EVENT_BUS.addListener(MotherOfStrays::onEntityInteractSpecific);
        NeoForge.EVENT_BUS.addListener(MotherOfStrays::onAttack);
        NeoForge.EVENT_BUS.addListener(MotherOfStrays::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(MotherOfStrays::onDimensionChanged);

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
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, true, TellTaleFloorboards::onLeftClickBlock);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, true, TellTaleFloorboards::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, true, TellTaleFloorboards::onBreak);
        NeoForge.EVENT_BUS.addListener(RedRoom::onServerTick);
        NeoForge.EVENT_BUS.addListener(RedRoom::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(RedRoom::onNeighborNotify);
        NeoForge.EVENT_BUS.addListener(RedRoom::onPiston);
        NeoForge.EVENT_BUS.addListener(Growl::onServerTick);
        NeoForge.EVENT_BUS.addListener(Growl::onPlayerLoggedOut);
        NeoForge.EVENT_BUS.addListener(HideAndClap::onServerTick);
        NeoForge.EVENT_BUS.addListener(HideAndClap::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(HideAndClap::onAttackEntity);
        NeoForge.EVENT_BUS.addListener(HideAndClap::onEquipmentChange);
        NeoForge.EVENT_BUS.addListener(HideAndClap::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(HideAndClap::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(HideAndClap::onPlayerLoggedOut);
        NeoForge.EVENT_BUS.addListener(HideAndClap::onPlayerRespawnPosition);
        NeoForge.EVENT_BUS.addListener(HideAndClap::onPlayerRespawned);
        NeoForge.EVENT_BUS.addListener(HideAndClap::onDeath);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, HideAndClap::onItemToss);
        NeoForge.EVENT_BUS.addListener(HideAndClap::onServerStopping);
        NeoForge.EVENT_BUS.addListener(ModelHome::onServerTick);
        NeoForge.EVENT_BUS.addListener(ModelHome::onAttackEntity);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, ModelHome::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(HarriganVignette::onServerTick);
        NeoForge.EVENT_BUS.addListener(HarriganVignette::onEntityTick);
        NeoForge.EVENT_BUS.addListener(HarriganVignette::onContainerClose);
        NeoForge.EVENT_BUS.addListener(HarriganVignette::onEntityInteract);
        NeoForge.EVENT_BUS.addListener(HarriganVignette::onEntityInteractSpecific);
        NeoForge.EVENT_BUS.addListener(HarriganVignette::onAttackEntity);
        NeoForge.EVENT_BUS.addListener(HarriganVignette::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(HarriganVignette::onServerChat);
        NeoForge.EVENT_BUS.addListener(HarriganVignette::onPlayerLoggedOut);
        NeoForge.EVENT_BUS.addListener(io.github.knaitoe.theoldesthouse.labyrinth.DrownedTown::onServerTick);
        NeoForge.EVENT_BUS.addListener(io.github.knaitoe.theoldesthouse.labyrinth.PreservedCave::onServerTick);
        NeoForge.EVENT_BUS.addListener(io.github.knaitoe.theoldesthouse.labyrinth.PreservedCave::onGameEvent);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, io.github.knaitoe.theoldesthouse.labyrinth.PreservedCave::onAttack);
        NeoForge.EVENT_BUS.addListener(io.github.knaitoe.theoldesthouse.labyrinth.Shallows::onServerTick);
        NeoForge.EVENT_BUS.addListener(io.github.knaitoe.theoldesthouse.labyrinth.Shallows::onToss);
        NeoForge.EVENT_BUS.addListener(io.github.knaitoe.theoldesthouse.labyrinth.CaverVignette::onServerTick);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, io.github.knaitoe.theoldesthouse.labyrinth.CaverVignette::onRightClick);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, io.github.knaitoe.theoldesthouse.labyrinth.CaverVignette::onLeftClick);
        NeoForge.EVENT_BUS.addListener(io.github.knaitoe.theoldesthouse.labyrinth.CaverVignette::onLogout);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, io.github.knaitoe.theoldesthouse.labyrinth.CaverVignette::onDeath);
        NeoForge.EVENT_BUS.addListener(io.github.knaitoe.theoldesthouse.labyrinth.GoatmanVignette::onServerTick);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, io.github.knaitoe.theoldesthouse.labyrinth.GoatmanVignette::onBlock);
        NeoForge.EVENT_BUS.addListener(io.github.knaitoe.theoldesthouse.labyrinth.GoatmanVignette::onAttack);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, io.github.knaitoe.theoldesthouse.labyrinth.GoatmanVignette::onDeath);
        NeoForge.EVENT_BUS.addListener(io.github.knaitoe.theoldesthouse.labyrinth.GoatmanVignette::onRespawnPosition);
        NeoForge.EVENT_BUS.addListener(io.github.knaitoe.theoldesthouse.labyrinth.GoatmanVignette::onRespawn);
        NeoForge.EVENT_BUS.addListener(io.github.knaitoe.theoldesthouse.labyrinth.GoatmanVignette::onLogin);
        NeoForge.EVENT_BUS.addListener(io.github.knaitoe.theoldesthouse.labyrinth.GoatmanVignette::onLogout);
        NeoForge.EVENT_BUS.addListener(io.github.knaitoe.theoldesthouse.labyrinth.PhoneCanoe::onServerTick);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, io.github.knaitoe.theoldesthouse.labyrinth.PhoneCanoe::onDamage);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, io.github.knaitoe.theoldesthouse.labyrinth.PhoneCanoe::onToss);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, io.github.knaitoe.theoldesthouse.labyrinth.PhoneCanoe::onAttack);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, io.github.knaitoe.theoldesthouse.labyrinth.PhoneCanoe::onBlock);
        NeoForge.EVENT_BUS.addListener(io.github.knaitoe.theoldesthouse.labyrinth.PhoneCanoe::onLogout);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, io.github.knaitoe.theoldesthouse.labyrinth.Shallows::onAttack);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, io.github.knaitoe.theoldesthouse.labyrinth.DrownedTown::onRightClick);
        NeoForge.EVENT_BUS.addListener(io.github.knaitoe.theoldesthouse.labyrinth.DrownedTown::onSmelted);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, io.github.knaitoe.theoldesthouse.labyrinth.DrownedTown::onPlaced);
        NeoForge.EVENT_BUS.addListener(io.github.knaitoe.theoldesthouse.labyrinth.ChurchKeyItem::onChangeTarget);
        NeoForge.EVENT_BUS.addListener(io.github.knaitoe.theoldesthouse.labyrinth.ChurchKeyItem::onEntityTick);

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

        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, io.github.knaitoe.theoldesthouse.opening.CompanionOrders::onInteract);
        NeoForge.EVENT_BUS.addListener(io.github.knaitoe.theoldesthouse.opening.CompanionOrders::onEntityTick);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, io.github.knaitoe.theoldesthouse.labyrinth.NavigationAids::onRightClick);
        NeoForge.EVENT_BUS.addListener(io.github.knaitoe.theoldesthouse.labyrinth.NavigationAids::onPlayerTick);
        OpeningSequence.register(NeoForge.EVENT_BUS);

        LOGGER.info("The Oldest House prototype initialized.");
    }
}
