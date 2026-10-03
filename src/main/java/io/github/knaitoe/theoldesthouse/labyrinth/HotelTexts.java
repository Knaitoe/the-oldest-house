package io.github.knaitoe.theoldesthouse.labyrinth;
import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.world.item.ItemStack;
/** Original hotel papers. Dates belong to the fiction; personal entries describe actual actions. */
public final class HotelTexts {
    private HotelTexts(){}
    private static ItemStack book(String title,String author,String... pages){return HouseWriting.book(title,author,HouseWriting.WritingStyle.PLAIN,List.of(pages));}
    public static ItemStack rules(){return book("Closing night","The desk",
        "Dinner is served at the remaining table. The orchestra has been asked to stay.\n\nThere is no charge tonight. Your account follows you out.",
        "The next chest you open will settle one place on the tab. Missing property is held at this desk, with its labels intact.\n\nAsk for it. We do not give another guest your things.",
        "Rooms increase toward the mountain. When the numbers begin again, walk against them.\n\n217 is made up. The grounds key waits in the hedge maze.");}
    public static ItemStack housekeeping(){return book("Housekeeping, room 217","Night staff",
        "1911. The lamp failed before the guest arrived. The floor was patched from below.\n\nPlease do not stand on the pale boards.",
        "1936. One bed made. No luggage entered on the sheet. In the morning the guest's pockets had been put in order.",
        "1978. Candles out. Torches returned in one stack. I found the same name in the drawer twice, in different hands.",
        "The first rest sorts your pockets. A later rest leaves them in your private drawers. Their contents are recorded before the pockets are emptied.\n\nSleeping here does not make this your home.");}
    public static ItemStack log(){return book("One sober day","The caretaker",
        "October 3. I promised a whole day. No glass before supper. We carried the children's trunks up together. Snow at the sill.",
        "October 9. The road is closed. We ate early. I tested the valve twice. Snow halfway up the glass.",
        "October 14. They asked me to come upstairs. I had only one gauge left to check. Snow above the latch.",
        "October 19. Heat, pressure, vent.\n\nOctober 20. Heat, pressure.\n\nOctober 21. Heat.");}
    public static ItemStack manuscript(){return book("A day's work","The caretaker",
        "I will go upstairs when the gauge is steady.\n\nI will go upstairs when the gauge is steady.",
        "I will go upstairs when the gauge is steady.\n\nI will go upstairs when the gauge is steady.");}
    public static ItemStack boiler(){return book("Heating plant","Maintenance",
        "The plant begins its shift when someone finds it. Pull the vent before three days of pressure have accumulated. The gauge has four faces.",
        "If the plant blows, the dining fire dies and 217 will not keep a sleeper. The restart switch stands beside the valve.\n\nThis circuit heats the hotel alone.");}
    public static ItemStack account(String name,int debt,boolean settled){return book("The closing account","The desk",
        "Guest: "+name+".\n\nUncollected places: "+debt+".\n\nProperty remains available at the desk and in the room drawers.",
        settled?"The key, the photograph, the bed and the log have been entered. The gauge was attended. You read the account through.\n\nNo one has signed the second place setting.":"A blank line remains. Visit the bed, the bar, the dance floor, the maze, the caretaker's log and the heating plant. Return here when the tab is settled.");}
    public static ItemStack blind(){return book("Before the unlit stretch","An electrician",
        "The lamps were removed. A bell remains at each turn. Follow its sound until it stops.\n\nThe printed arrow worked for the first two turns.",
        "Then it pointed through a wall. The bell had not moved.\n\nKeep a hand on the wall if you must return. There is no prize for believing a sign.");}
    public static List<ItemStack> specimens(){return List.of(rules(),housekeeping(),log(),manuscript(),boiler(),account("Reader_1234567890",12,true),account("Reader_1234567890",12,false),blind());}
}
