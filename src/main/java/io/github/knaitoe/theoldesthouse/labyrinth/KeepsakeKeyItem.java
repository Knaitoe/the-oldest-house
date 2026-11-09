package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.417 (docs/LAST_ORDERS_KEEPSAKES_0_4_417.md): one rule for every keepsake that unlocks a recipe, first written for the service
 * key (0.4.413). The keepsake is marked for the reader it was given to ({@link LiteraryVignettes#OWNER}) and for the room it came
 * from. At a crafting table it makes its object and comes back to the grid as it went in, mark and all. A grid holding someone
 * else's (or one nobody was given) shows no result, so nothing can be made with it (CraftingKeyMixin).
 */
public class KeepsakeKeyItem extends Item {
    private final String place;
    /** {@code place} is the id of the room the keepsake comes from ({@link LabyrinthPlace#id()}). */
    public KeepsakeKeyItem(Properties p,String place){super(p);this.place=place;}
    @Override public boolean hasCraftingRemainingItem(ItemStack stack){return true;}
    @Override public ItemStack getCraftingRemainingItem(ItemStack stack){return stack.copyWithCount(1);}
    /** Whose keepsake it is, or null for one nobody was given (or not a keepsake key at all). */
    public static @Nullable UUID owner(ItemStack stack){
        if(!(stack.getItem() instanceof KeepsakeKeyItem key))return null;
        var data=stack.get(DataComponents.CUSTOM_DATA);if(data==null)return null;var t=data.copyTag();
        return t.hasUUID(LiteraryVignettes.OWNER)&&key.place.equals(t.getString(LiteraryVignettes.PLACE))?t.getUUID(LiteraryVignettes.OWNER):null;
    }
    /** True if this is one of the player's own keepsake keys. */
    public static boolean mine(ItemStack stack,Player p){return p.getUUID().equals(owner(stack));}
    /** False if a crafting grid holds a keepsake key that is not this player's own. */
    public static boolean allows(CraftingContainer grid,Player p){
        for(int i=0;i<grid.getContainerSize();i++){var s=grid.getItem(i);if(s.getItem() instanceof KeepsakeKeyItem&&!mine(s,p))return false;}
        return true;
    }
}
