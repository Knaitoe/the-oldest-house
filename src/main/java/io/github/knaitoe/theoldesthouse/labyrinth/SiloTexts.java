package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** The owner's three physical passages, preserved verbatim. Nothing instructs the reader how to survive. */
public final class SiloTexts {
    public static final String[] PASSAGES={
        "An unrecovered body is a wonderful thing for a town. So long as they don't drag the river or empty the bin, a girl is just a blank sheet of paper, and everybody in the county gets to write their favorite ending on her. She’s in California. She’s waiting tables in Omaha. She’s anywhere but twelve feet down in the dark with her mouth full of yellow dust.",
        "He came out to the farm twice a week with his cruiser lights off, just sitting in the gravel with the window rolled down, looking up at the tin roof. I think he knew the bridge gave out under my sneakers the night I climbed up here. You can't look at a broken board for three winters and not know how weight works. He just preferred the version where I grew wings at the last second.",
        "When you wish for a life where nobody ever leaves, you don't realize how heavy eternity gets when it's trapped in a round room. Go ahead and open the hatch. The sky out there is just painted tin, and the corn is still waiting at the bottom for both of us to get tired of climbing."
    };
    public static final String[] RADIO={"She took a bus to Tucson.","She met a boy with a flatbed.","She's just waiting out the winter."};
    private static final String[] TITLES={"Yellow dust","Three winters","The roof"};
    private SiloTexts(){}
    public static ItemStack original(UUID owner,int piece){
        var pages=new ArrayList<String>();String remaining=PASSAGES[piece];
        while(remaining.length()>180){int end=remaining.lastIndexOf(' ',180);pages.add(remaining.substring(0,end));remaining=remaining.substring(end+1);}pages.add(remaining);
        var book=HouseWriting.book(TITLES[piece],"",HouseWriting.WritingStyle.WILL,pages);
        CustomData.update(DataComponents.CUSTOM_DATA,book,t->{t.putUUID("SiloOwner",owner);t.putUUID("SiloOriginal",UUID.randomUUID());t.putInt("SiloPiece",piece);});return book;
    }
}
