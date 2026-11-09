package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.413 (docs/POLYBIUS_HOME_CABINET_0_4_413.md): the service key, taped under the clip of the men's clipboard and given with the
 * account to a reader who walked away from the black cabinet. Like every keepsake it is marked for that reader
 * ({@link LiteraryVignettes#OWNER}) and works only for them.
 *
 * <p>At a crafting table, with planks, a glass pane and redstone, it makes an arcade cabinet of the reader's own
 * ({@link HomeCabinetBlock}). The key is not used up: it comes back to the grid as it went in, mark and all. A grid with someone
 * else's key in it makes nothing (CraftingKeyMixin). Since 0.4.417 the rule is {@link KeepsakeKeyItem}'s, shared with the bar's
 * keepsakes.
 */
public final class ServiceKeyItem extends KeepsakeKeyItem {
    public ServiceKeyItem(Properties p){super(p,"polybius");}
    /** Whose key it is, or null for one nobody was given. */
    public static @Nullable UUID owner(ItemStack stack){return stack.is(PolybiusRegistry.SERVICE_KEY.get())?KeepsakeKeyItem.owner(stack):null;}
    /** True if this is the player's own key. */
    public static boolean mine(ItemStack stack,Player p){return stack.is(PolybiusRegistry.SERVICE_KEY.get())&&KeepsakeKeyItem.mine(stack,p);}
}
