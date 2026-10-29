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
    public static final String WAITING="A waiting room is the only place left where time still moves at the speed of wallpaper.\n\nFolks who remember when the afternoon used to be long just find a chair with good springs, fold the paper back to the local page, and let the clock do its own work. Folks who came up with a wire in their pocket don't know what to do with their knees when a door is shut—they’re up and down the rug before the minute hand even clears the twelve, looking for a piece of glass that’ll glow back at them.";
    public static final String BATHROOM="You never really sleep right in a strange bed until you’ve stood over the sink for two minutes with the hot water running.\n\nEven when you’re dead on your feet, your hands still know the order of things before the light goes out. You reach for the silk so the cotton pillowcase doesn't take what’s yours, or you work the oil into the part, or you run the bristles through until your scalp stops humming from the drive. The house doesn't mind waiting in the hall while you get yourself settled.";
    public static final String SHELTER="A summer storm is a great excuse to stand under a roof with somebody you haven't worked up the nerve to talk to yet.\n\nYou put a nickel in the slot just so the quiet doesn't get too heavy under the rafters, and then your feet tell on you anyway. Some people drift over to the rail where the boy is watching the water; some go sit by the girl with the cold bottle; some stand right in the middle of the floorboards and hope neither one of them goes home before the rain stops.";
    public static final String LOCAL_PAGE="THE COUNTY REGISTER\nLocal page\n\nThe library will keep its reading room open late on Thursday. Returns may be left in the box beside the side entrance.\n\nRain postponed the ball game. The groundskeeper says the field should be ready by Saturday.\n\nFOUND: One small brass key near the bus shelter. Ask at the post office.";
    public static final String MAGAZINE="COUNTY AND GARDEN\n\nLeave the porch pots out in a light rain. Turn them afterward; the plants beside the wall always drink less.\n\nA loose screen can usually be made quiet with a fresh length of spline. The old frame is often sound.\n\nFor next month: pears, late beans, and putting the summer chairs away.";
    /**
     * 0.4.168: inside the crayon tin's lid. The design's placeholder draft, for the owner to replace or rewrite
     * (docs/HOUSE_READ_DESIGN.md, the self-portrait). It names no colour and no kind of person.
     */
    public static final String KITCHEN="Every tin of crayons used to come with one stick called flesh, as if there were only the one kind.\n\nKids figure out which stick is theirs before anybody tells them. Some go straight for it and press so hard the paper shines; some try three on the back of the sheet first, holding each one up against the back of a hand like matching paint at the hardware store. Nobody has to say a word. The hands already know what color they've always been.";
    private HouseReadTexts(){}
    public static String text(HouseReadSites.Room room){return switch(room){case COATROOM->COAT;case PORCH->PORCH;case WAITING->WAITING;case BATHROOM->BATHROOM;case SHELTER->SHELTER;case KITCHEN->KITCHEN;};}
    public static String title(HouseReadSites.Room room){return switch(room){case COATROOM->"The umbrella stand";case PORCH->"The back steps";case WAITING->"The wall calendar";case BATHROOM->"The postcard";case SHELTER->"The song strips";case KITCHEN->"Inside the lid";};}
    public static ItemStack book(boolean porch){return book(porch?HouseReadSites.Room.PORCH:HouseReadSites.Room.COATROOM);}
    public static ItemStack book(HouseReadSites.Room room){return pages(title(room),text(room));}
    private static ItemStack pages(String title,String text){var pages=new ArrayList<String>();for(String paragraph:text.split("\n\n")){while(paragraph.length()>180){int end=paragraph.lastIndexOf(' ',180);pages.add(paragraph.substring(0,end));paragraph=paragraph.substring(end+1);}pages.add(paragraph);}var book=HouseWriting.book(title,"",HouseWriting.WritingStyle.WILL,pages);net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA,book,t->t.putBoolean("HouseReadNote",true));return book;}
    public static void open(ServerPlayer p,boolean porch){open(p,porch?HouseReadSites.Room.PORCH:HouseReadSites.Room.COATROOM);}
    public static void open(ServerPlayer p,HouseReadSites.Room room){openLiteral(p,room,title(room),text(room));}
    public static void openLiteral(ServerPlayer p,HouseReadSites.Room room,String title,String text){p.openMenu(new SimpleMenuProvider((id,inv,reader)->new Note(id,p,room,pages(title,text)),Component.literal(title)));}
    private static final class Note extends LecternMenu {
        private final ServerPlayer owner;private final HouseReadSites.Room room;
        Note(int id,ServerPlayer p,HouseReadSites.Room room,ItemStack book){super(id,container(book),new SimpleContainerData(1));owner=p;this.room=room;}
        private static Container container(ItemStack book){var c=new SimpleContainer(1);c.setItem(0,book);return c;}
        @Override public boolean stillValid(Player p){return p==owner&&HouseReadSites.inside(owner)==room;}
        @Override public void clicked(int slot,int button,ClickType type,Player p){}
        @Override public ItemStack quickMoveStack(Player p,int slot){return ItemStack.EMPTY;}
        @Override public boolean clickMenuButton(Player p,int button){return button!=3&&stillValid(p)&&super.clickMenuButton(p,button);}
    }
}
