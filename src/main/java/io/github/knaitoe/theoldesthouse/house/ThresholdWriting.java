package io.github.knaitoe.theoldesthouse.house;
import java.util.List;
import net.minecraft.network.chat.Component;
/** Occasional original scraps; theoretical references remain in the design document. */
public final class ThresholdWriting {
    private ThresholdWriting(){}
    public static List<Component> pages(int chapter){return switch(Math.floorMod(chapter,4)){
        case 0->List.of(HouseWriting.page(HouseWriting.WritingStyle.KAREN,"I put your cup back in the cupboard. I couldn't leave it beside the sink another night.\n\nWhen you come ").copy().append(Component.literal("home").withStyle(s->s.withColor(0x3366DD))).append(HouseWriting.page(HouseWriting.WritingStyle.KAREN,", use the blue one.")));
        case 1->List.of(HouseWriting.page(HouseWriting.WritingStyle.WILL,"The strip of carpet under the door belongs to neither room. I stood on it while Karen went to get the tape.\n\nI heard her call from the kitchen. I couldn't decide which side to step onto."));
        case 2->List.of(HouseWriting.page(HouseWriting.WritingStyle.ZAMPANO,"The boy said the coat was his. The cuffs came past his hands.\n\nI asked who had brought him here. He began folding the sleeves back."));
        default->List.of(HouseWriting.page(HouseWriting.WritingStyle.KAREN,"I feel petulant. Are these even reaching you?\n\nThe envelope came back on Thursday. There was no mark on it. I checked the address twice."));
    };}
}
