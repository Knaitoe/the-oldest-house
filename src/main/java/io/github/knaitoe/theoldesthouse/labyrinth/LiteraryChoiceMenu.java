package io.github.knaitoe.theoldesthouse.labyrinth;
import java.util.*;
import java.util.function.IntPredicate;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.core.component.DataComponents;
/** Native nine-slot dialogue; buttons cannot be moved, shift-clicked, thrown or duplicated. */
public final class LiteraryChoiceMenu extends ChestMenu {
    private final ServerPlayer reader;private final java.util.function.BooleanSupplier valid;private final IntPredicate choose;
    private LiteraryChoiceMenu(int id,ServerPlayer p,Container icons,java.util.function.BooleanSupplier valid,IntPredicate choose){super(MenuType.GENERIC_9x1,id,p.getInventory(),icons,1);this.reader=p;this.valid=valid;this.choose=choose;}
    public static ItemStack icon(Item item,String label){var s=new ItemStack(item);s.set(DataComponents.CUSTOM_NAME,Component.literal(label));return s;}
    public static void open(ServerPlayer p,String title,Map<Integer,ItemStack> icons,java.util.function.BooleanSupplier valid,IntPredicate choose){var c=new SimpleContainer(9);icons.forEach((slot,item)->c.setItem(slot,item.copy()));p.openMenu(new SimpleMenuProvider((id,inv,who)->new LiteraryChoiceMenu(id,p,c,valid,choose),Component.literal(title)));}
    @Override public void clicked(int slot,int button,ClickType type,Player who){if(who!=reader||!LiteraryVignettes.participant(reader)||!valid.getAsBoolean())return;if(type==ClickType.PICKUP&&button==0&&slot>=0&&slot<9&&choose.test(slot))reader.closeContainer();}
    @Override public ItemStack quickMoveStack(Player p,int slot){return ItemStack.EMPTY;}
    @Override public boolean stillValid(Player p){return p==reader&&LiteraryVignettes.participant(reader)&&valid.getAsBoolean();}
}
