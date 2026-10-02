package io.github.knaitoe.theoldesthouse.house;
import java.util.List;
import net.minecraft.network.chat.Component;
/** Original fictional marginalia; theoretical references belong in the design document. */
public final class ThresholdWriting {
    private ThresholdWriting(){}
    public static List<Component> pages(int chapter){return switch(Math.floorMod(chapter,4)){
        case 0->List.of(Component.literal("UN").append(Component.literal("HOME").withStyle(s->s.withColor(0x3366DD))).append("LY\n\nNot merely a strange room. A familiar room returning with what had been concealed inside it.\n\n[The chair was always here. That is what frightens me.]"));
        case 1->List.of(Component.literal("LIMEN\n\nA threshold has an extent. You may be inside it while you are still deciding whether to enter.\n\nI heard the room change before I touched its door."),Component.literal("The hall removes our company, our landmarks and our useful measures.\n\nWhat remains is brought by the person walking it.\n\nA route that saved one explorer may be useless to the next."));
        case 2->List.of(Component.literal("Field account, restored:\n\n").append(Component.literal("A beast waits below.").withStyle(s->s.withColor(0xA52A2A).withStrikethrough(true))).append("\n\nA child asked who had locked the door. I did not answer.\n\n[The first sentence was removed before this copy.]"),Component.literal("Second account:\n\nI found no child. I heard someone breathing behind the wall.\n\nThe earlier writer says I approached the wrong cell.\n\nThere is only one cell."));
        default->List.of(Component.literal("A reader's objection:\n\nIf every horror is ours, why do we agree on the bars?\n\nIf the prisoner belongs to the hall, why does it know what I brought?\n\n[Leave both questions in the text.]"));
    };}
}
