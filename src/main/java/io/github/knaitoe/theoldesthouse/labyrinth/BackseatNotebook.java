package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** The one notebook on the passenger cushion. Reading is optional; taking transfers the original. */
public final class BackseatNotebook {
    private BackseatNotebook(){}
    public static ItemStack original(java.util.UUID owner){
        var book=HouseWriting.book("Twenty seconds","Anonymous",HouseWriting.WritingStyle.WILL,List.of(
            "Cars have a lie built right into the upholstery. You hunker down behind two tons of Detroit steel and safety glass, frost on the AC and four chords on the radio, and you think you’re in your own private vacuum. You think the world outside the windshield is just a movie you’re driving through.",
            "I only left the door open for twenty seconds while I went back to drop the house keys in the mailbox. Twenty seconds under the streetlamp with the dome light shining on the vinyl.",
            "The steering column locked up a quarter-mile past the county line, right as the pines closed in over the median. I could see the oncoming headlights swerving into my lane, and I was already apologizing to my mother in my head for the dent in the fender, right up until I looked in the rearview mirror and realized the backseat wasn't empty anymore—and whatever climbed in while I was at the mailbox was the one holding the wheel steady."));
        CustomData.update(DataComponents.CUSTOM_DATA,book,t->{t.putUUID("BackseatOwner",owner);t.putUUID("BackseatOriginal",java.util.UUID.randomUUID());});return book;
    }
    public static boolean readable(BackseatCarBlock.Car car,Player p){return car.mine(p)&&car.phase<BackseatCarBlock.Car.CLOSING&&!car.notebook.isEmpty()&&p.distanceToSqr(car.getBlockPos().getCenter())<=36;}
    public static void open(BackseatCarBlock.Car car,ServerPlayer p){if(readable(car,p))p.openMenu(new SimpleMenuProvider((id,inv,who)->new Pages(id,car,p),Component.literal("Twenty seconds")));}
    private static boolean handOver(ServerPlayer p,ItemStack original){
        int slot=p.getInventory().getFreeSlot();
        if(slot>=0){p.getInventory().setItem(slot,original);p.inventoryMenu.broadcastChanges();return true;}
        var item=new net.minecraft.world.entity.item.ItemEntity(p.serverLevel(),p.getX(),p.getY()+.2,p.getZ(),original);item.setTarget(p.getUUID());return p.serverLevel().addFreshEntity(item);
    }
    public static final class Pages extends LecternMenu {
        private final BackseatCarBlock.Car car;private final ServerPlayer reader;
        private Pages(int id,BackseatCarBlock.Car car,ServerPlayer reader){super(id,container(car.notebook),new SimpleContainerData(1));this.car=car;this.reader=reader;}
        private static Container container(ItemStack book){var slots=new SimpleContainer(1);slots.setItem(0,book.copy());return slots;}
        @Override public boolean stillValid(Player p){return p==reader&&readable(car,p);}
        @Override public void clicked(int slot,int button,ClickType kind,Player p){}
        @Override public ItemStack quickMoveStack(Player p,int slot){return ItemStack.EMPTY;}
        @Override public boolean clickMenuButton(Player p,int button){
            if(!stillValid(p))return false;
            if(button==3){var original=car.notebook;car.notebook=ItemStack.EMPTY;if(!handOver(reader,original)){car.notebook=original;car.sync();return false;}car.sync();reader.closeContainer();return true;}
            return super.clickMenuButton(p,button);
        }
    }
}
