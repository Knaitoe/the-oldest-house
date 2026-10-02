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
        "My dear child,\n\nThere are three attics. They insist there have always been three.\n\nI remember a room without a roof. I remember you asking if the rain could come in.",
        "I will write when they bring the post. Leave a little room in a chest for me.\n\nThe last loose sheets have beginnings I could not say aloud. Count them. Leave a pause between each number at the middle attic."));}
    public static ItemStack letter(int n,String name){return HouseWriting.book("Letter, "+new String[]{"7 April","8 April","14 April","undated","16 April","9 April?","Tuesday","after your letter"}[Math.min(7,n)],"Pelafina",HouseWriting.WritingStyle.PELAFINA,switch(n){
        case 0->List.of("My dear "+name+",\n\nThey brought an orange with breakfast. I saved the peel because it made the drawer smell like somewhere with a kitchen.\n\nI hope you are eating properly.","I folded this three times before I remembered the envelope.\n\nDo you still open letters with your thumb? I used to worry you would cut yourself.\n\nThere is a loose sheet behind this one.","The bell rings.\n\nHands fold.\n\nRooms settle.\n\nEvery door waits.\n\nExcept mine.");
        case 1->List.of("I have not had an answer.\n\nPerhaps you wrote and it was put in the wrong drawer. I prefer that explanation today.\n\nI moved the orange peel. It had begun to dry.","They say the windows open.\n\nToday I counted the coats of paint over their catches.\n\nThe afternoon was warm. I thought of your coat hanging over a chair.","Only the post knows where you are.\n\nNobody will give me the address.\n\nEven so, I write.");
        case 2->List.of("The dates are no longer consecutive.\n\nI asked the nurse what happened to the missing morning. She brought tea.\n\nI thanked her. I had been rude about the dates.","I am irritated that I have to keep writing into silence.\n\nThen I imagine you carrying these pages around, and I am ashamed of the irritation.\n\nNeither feeling stays long enough to be useful.","The third stair creaks.\n\nWait before the next knock.\n\nOnly the middle door listens.");
        case 3->List.of("I have left a page upstairs.\n\nI wanted to write something ordinary on it. A shopping list. A reminder to mend a cuff.\n\nI could not remember what you needed.","The beginnings of the loose sheets make three numbers. The middle attic listens to the pauses.\n\nThere is a gap where the next date should be.\n\nDo not fill it in for me.");
        case 4->List.of("I read the first letter again.\n\nThe drawer I described is here. The kitchen I imagined is not.\n\nI was trying to bring the smell of one room into another.","Someone has underlined the word SAVE.\n\nI meant the peel.\n\nI think I meant the peel.\n\nPlease keep the earlier page.");
        case 5->List.of("The date on this one goes backward.\n\nI have left it. You will know I was here on the morning I wrote it, even if you cannot place that morning.","I thought I heard you in the hall.\n\nI did not ask you to come in. I listened until the footsteps reached a door I could not see.\n\nIt was kinder than being certain.");
        case 6->List.of("I am writing because the afternoon is quiet.\n\nNothing happened that needs an account.\n\nI wish we had more letters like that.","There is a clean cup beside mine.\n\nI put it there myself.\n\nThat is not evidence. It is an invitation.");
        default->List.of("I do not know whether the answer in the drawer arrived before this letter.\n\nI read what was there.\n\nFor a while I could imagine someone reading back.","You do not have to solve every sentence to remain with me.\n\nLeave the blank date blank. Keep the ordinary details.\n\nThey were the things I was trying to send.");});}
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
