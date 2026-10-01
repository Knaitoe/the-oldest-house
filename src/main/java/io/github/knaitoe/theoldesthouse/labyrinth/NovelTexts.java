package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Original adaptations, not transcriptions of the novel. Short pages fit native books. */
public final class NovelTexts {
    private NovelTexts(){}
    public static ItemStack archive(){return HouseWriting.book("The unfinished survey","Zampano",HouseWriting.WritingStyle.ZAMPANO,List.of(
        "The windows were sealed before the measurements began.\n\nA reader described the sky to me. I asked her to start again.",
        "Seven appointments. Seven voices.\n\nThe cats knew which chair was empty before I did.",
        "The gouges are beside the trunk. The manuscript calls them damage to the floor.\n\nNo one has measured their spacing.\n\n[Someone has counted them.]"));}
    public static ItemStack whaleOpening(){return HouseWriting.book("The address on the envelope","Pelafina",HouseWriting.WritingStyle.PELAFINA,List.of(
        "There are three attics. They insist there have always been three.\n\nI remember a room without a roof.",
        "I will write when they bring the post.\n\nOpen an ordinary chest later. Leave a little room.\n\nCount the beginnings of my paragraphs. Pause between the numbers."));}
    public static ItemStack letter(int n,String name){return HouseWriting.book("Letter, "+new String[]{"7 April","8 April","14 April","undated"}[Math.min(3,n)],"Pelafina",HouseWriting.WritingStyle.PELAFINA,switch(n){
        case 0->List.of("The bell rings.\n\nHands fold.\n\nRooms settle.\n\nEvery door waits.\n\nExcept mine.","Five beginnings.\n\nA number.\n\nKeep the pauses between the numbers when you knock at the middle attic.");
        case 1->List.of("Only the post knows where you are.\n\nNobody will give me the address.\n\nEven so, I write.","They say the windows open.\n\nToday I counted the coats of paint over their catches.");
        case 2->List.of("The third stair creaks.\n\nWait before the next knock.\n\nOnly the middle door listens.","The dates are no longer consecutive.\n\nI have asked the nurse what happened to the missing morning.");
        default->List.of("I have left a page upstairs.\n\nIf the first letters made sense, you can reach it.\n\nI cannot promise I will be there.","There is a gap where the next date should be.\n\nDo not fill it in for me.");});}
    public static ItemStack whaleLast(){return HouseWriting.book("The undated letter","Pelafina",HouseWriting.WritingStyle.PELAFINA,List.of(
        "You heard the pauses.\n\nI tried to make a door out of them.\n\nThey told me this attic was empty.",
        "There is no next date.\n\nPlease leave that space.\n\nI have spent so long being told what belongs there."));}
    public static ItemStack well(){return HouseWriting.book("The cover","Karen Green",HouseWriting.WritingStyle.KAREN,List.of(
        "There was a barn. There was a well.\n\nThere were two of us.\n\nThat is the part I can put in order.",
        "At the bottom, the initials are low enough for a child to reach.\n\nThe cover opens from above.\n\nI remember waiting more clearly than I remember the hands."));}
    public static ItemStack apology(){return HouseWriting.book("What the frame kept","Will Navidson",HouseWriting.WritingStyle.WILL,List.of(
        "I found the angle before I found the words.\n\nThat sounds like an excuse.\n\nIt is one.",
        "The bird circles something beyond the dunes.\n\nUse the spyglass. Hold the shape in its centre.\n\nA picture is easier to carry than the thing it leaves out."));}
    public static ItemStack hospitalOpening(){return HouseWriting.book("Night duty","?",HouseWriting.WritingStyle.PLAIN,List.of(
        "The call button is beside the incubator.\n\nPress it when the monitor calls.\n\nSomeone should come.",
        "The night is shorter here. Stay until morning.\n\nIf you leave, the ward will begin again."));}
    public static ItemStack hospitalLast(){return HouseWriting.book("At dawn","?",List.of(
        Component.literal("I pressed it when the sound began.\n\nI pressed it sooner the next time.\n\nThere is no entry in the chart for either of those things.").withStyle(s->s.withColor(0xAA82CC)),
        Component.literal("The alarms stopped at dawn.\n\nThe chair was still in the same place.\n\nI had mistaken an answer for someone coming.")));}
    public static ItemStack karen(){return HouseWriting.book("A room to come back to","Karen Green",HouseWriting.WritingStyle.KAREN,List.of(
        "This bed remembers you.\n\nSleep here to set a place inside the House. It is the only bed beyond the hall that does.",
        "The projector keeps pictures of the rooms you reached. Click it to change the frame.\n\nWhen you wake, count what is on the table.\n\nI do."));}
    public static ItemStack reply(String keyword){return HouseWriting.book("A reply without a date","Pelafina",HouseWriting.WritingStyle.PELAFINA,List.of(
        "You wrote about "+keyword+".\n\nI read that part twice.\n\nThe first time I imagined a window. The second time I imagined the window opening.",
        "Your book is safe in the drawer.\n\nYou may write again.\n\nI cannot make the dates behave."));}
}
