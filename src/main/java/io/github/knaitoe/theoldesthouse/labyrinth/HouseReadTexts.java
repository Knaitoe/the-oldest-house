package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

/** Supplied prose stays in its original object. There is no take/print button. */
public final class HouseReadTexts {
    public static final String COAT="Nobody minds if you borrow a dry coat off the hall tree when the rain comes through the screen. Cloth is just cloth when your teeth are chattering.\n\nStill, it’s a funny thing watching someone dress themselves in an empty foyer. Even with nobody in the room to see them, a person’s hands will hunt through five hangers just to find the seam that sits right on their collarbones. And if the hall glass doesn't agree with what they see when they button it up, they’ll turn the mirror right around to face the wallpaper before they take another step into the house.";
    public static final String PORCH="A whistle carries three blocks when the humidity is down.\n\nSome kids hear that sound when the streetlights kick on and their feet are already moving toward the screen door before the echo dies, because they know the plates are warm and the lock is going to turn behind them. Other kids hear the exact same note and their stomachs drop right into their shoes, and they spend the whole walk up the driveway listening to how hard the heels are hitting the kitchen linoleum so they know which door to use.";
    public static final String SUPPER="Wash your hands before you sit down, alright? It’s getting cold.";
    private HouseReadTexts(){}
    public static ItemStack book(boolean porch){var pages=new ArrayList<String>();for(String paragraph:(porch?PORCH:COAT).split("\n\n")){while(paragraph.length()>180){int end=paragraph.lastIndexOf(' ',180);pages.add(paragraph.substring(0,end));paragraph=paragraph.substring(end+1);}pages.add(paragraph);}var book=HouseWriting.book(porch?"The back steps":"The umbrella stand","",HouseWriting.WritingStyle.WILL,pages);net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA,book,t->t.putBoolean("HouseReadNote",true));return book;}
    public static void open(ServerPlayer p,boolean porch){p.openMenu(new SimpleMenuProvider((id,inv,reader)->new Note(id,p,porch),Component.literal(porch?"The back steps":"The umbrella stand")));}
    private static final class Note extends LecternMenu {
        private final ServerPlayer owner;private final boolean porch;
        Note(int id,ServerPlayer p,boolean porch){super(id,container(porch),new SimpleContainerData(1));owner=p;this.porch=porch;}
        private static Container container(boolean porch){var c=new SimpleContainer(1);c.setItem(0,book(porch));return c;}
        @Override public boolean stillValid(Player p){return p==owner&&HouseReadSites.inside(owner)==(porch?HouseReadSites.Room.PORCH:HouseReadSites.Room.COATROOM);}
        @Override public void clicked(int slot,int button,ClickType type,Player p){}
        @Override public ItemStack quickMoveStack(Player p,int slot){return ItemStack.EMPTY;}
        @Override public boolean clickMenuButton(Player p,int button){return button!=3&&stillValid(p)&&super.clickMenuButton(p,button);}
    }
}
