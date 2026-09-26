package io.github.knaitoe.theoldesthouse;

import com.mojang.logging.LogUtils;
import io.github.knaitoe.theoldesthouse.command.HouseCommands;
import io.github.knaitoe.theoldesthouse.house.HouseLifecycleEvents;
import io.github.knaitoe.theoldesthouse.house.HouseTransitionEvents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(TheOldestHouse.MOD_ID)
public final class TheOldestHouse {
    public static final String MOD_ID = "the_oldest_house";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TheOldestHouse(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.addListener(HouseCommands::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(HouseLifecycleEvents::onPlayerWakeUp);
        NeoForge.EVENT_BUS.addListener(HouseTransitionEvents::onPlayerTick);
        LOGGER.info("The Oldest House prototype initialized.");
    }
}
