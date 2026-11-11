package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * 0.4.473, the owner, of how long dialogue stays up: "make it configurable via config or settings". The mod's settings have
 * only been files until now. This gives them the game's own settings screen (Mods, this mod, Config): every setting in the
 * world's two files and this computer's one, each with the note written beside it in its file. The world's settings can be
 * changed there from inside a world by whoever may change them.
 */
@Mod(value=TheOldestHouse.MOD_ID,dist=Dist.CLIENT)
public final class HouseSettings {
    public HouseSettings(ModContainer container){container.registerExtensionPoint(IConfigScreenFactory.class,ConfigurationScreen::new);}
}
