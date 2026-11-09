package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.413 (docs/POLYBIUS_HOME_CABINET_0_4_413.md): the service key, taped under the clip of the men's clipboard and given with the
 * account to a reader who walked away from the black cabinet. Like every keepsake it is marked for that reader
 * ({@link LiteraryVignettes#OWNER}) and works only for them.
 *
 * <p>At a crafting table, with planks, a glass pane and redstone, it makes an arcade cabinet of the reader's own
 * ({@link HomeCabinetBlock}). The key is not used up: it comes back to the grid as it went in, mark and all. A grid with someone
 * else's key in it makes nothing (CraftingKeyMixin).
 */
public final class ServiceKeyItem extends Item {
    public ServiceKeyItem(Properties p){super(p);}
    @Override public boolean hasCraftingRemainingItem(ItemStack stack){return true;}
    @Override public ItemStack getCraftingRemainingItem(ItemStack stack){return stack.copyWithCount(1);}
    /** Whose key it is, or null for one nobody was given. */
    public static @Nullable UUID owner(ItemStack stack){
        var data=stack.get(DataComponents.CUSTOM_DATA);if(data==null)return null;var t=data.copyTag();
        return t.hasUUID(LiteraryVignettes.OWNER)&&LabyrinthPlace.POLYBIUS.id().equals(t.getString(LiteraryVignettes.PLACE))?t.getUUID(LiteraryVignettes.OWNER):null;
    }
    /** False if a crafting grid holds a service key that is not this player's own. */
    public static boolean allows(net.minecraft.world.inventory.CraftingContainer grid,Player p){
        for(int i=0;i<grid.getContainerSize();i++){var s=grid.getItem(i);if(s.is(PolybiusRegistry.SERVICE_KEY.get())&&!mine(s,p))return false;}
        return true;
    }
    /** True if this is the player's own key. */
    public static boolean mine(ItemStack stack,Player p){return stack.is(PolybiusRegistry.SERVICE_KEY.get())&&p.getUUID().equals(owner(stack));}
}
