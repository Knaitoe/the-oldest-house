package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Original adaptations, not transcriptions of the novel. Short pages fit native books. */
public final class NovelTexts {
    public static ItemStack archiveDraft(int index){return HouseWriting.book("Loose sheet "+(Math.floorMod(index,8)+1),"Zampano",HouseWriting.WritingStyle.ZAMPANO,switch(Math.floorMod(index,8)){
        case 0->List.of("no. no\n\nBeatrice put the chair back. I heard the leg scrape. Here, she said. HERE.\n\nI have written across it again.","The word is under my thumb. It must be.\n\nchair\n      chair\n\nDo not straighten this page.");
        case 1->List.of("north north north\n\nLeonie stopped reading. I asked where I had put the correction. She took my wrist and moved it a little to the left.","There was ink on her finger afterward.\n\nI should have asked how far.\n\nHow far from the first line. From the first line.");
        case 2->List.of("The point catches where I pressed before.\n\nI can follow the cuts in the desk. I cannot find the sentence.","Four grooves. Helen said three.\n\nI counted again while she was speaking.\n\nfour\n\nShe had gone by then.");
        case 3->List.of("A door at the end of the page.\n\nI wrote it beside the door. I wrote it in the margin. Someone has read both words as one.","Leave the fold alone.\n\nMy nail can find it.\n\nThere is something after the fold. Do not tell me there is nothing there.");
        case 4->List.of("The cat has been on these sheets. Warm fur. A torn corner.\n\nI am holding the wrong end of the pen again.","Her name was on this one.\n\nI asked her twice. She said I knew it.\n\nI have put a mark where her name should be.");
        case 5->List.of("I have not left a gap.\n\nIt is all on top of yesterday. The nib slips into the same wet place.\n\nStart lower.\n\nLower.","Someone is breathing beside the window.\n\nI counted the cats.\n\nI kept counting after they moved.");
        case 6->List.of("The reader said this was the last page. I could feel another beneath it.\n\nShe would not lift the corner.","One sheet. Two edges.\n\nI held them apart with my nail until the paper tore.\n\nNow she says there are two.");
        default->List.of("Where does this line begin.\n\nI have turned the sheet. I have turned it again.\n\nThe ink comes through to my knuckle.","Do not read the last word.\n\nI have crossed it out.\n\nI am crossing it out.");});}
    private NovelTexts(){}
    public static ItemStack archive(){return HouseWriting.book("The unfinished survey","Zampano",HouseWriting.WritingStyle.ZAMPANO,List.of(
        "The windows were sealed before the measurements began.\n\nA reader described the sky to me. I asked her to start again.",
        "Seven appointments. Seven voices.\n\nThe cats knew which chair was empty before I did.",
        "The gouges are beside the trunk. The manuscript calls them damage to the floor.\n\nNo one has measured their spacing.\n\n[Someone has counted them.]"));}
    public static ItemStack whaleOpening(){return HouseWriting.book("Before the post","Pelafina",HouseWriting.WritingStyle.PELAFINA,List.of(
        "My dear child,\n\nThey say the post goes out at no particular hour. I write anyway.\n\nThere is paper in the desk. There is always paper in the desk.",
        "Write to me, and sign it, or they will not take it.\n\nI will answer before you ask. I always have.\n\nThey took the number off my door. I count the others."));}
    /**
     * The answer already in the hand that posted letter n (1 to 3). Each is headed from a room she does not live in, quotes the
     * letter it has not yet received, and keeps five lines on its third page whose first letters agree with one another.
     */
    public static ItemStack whaleReply(int n,String name,String quote){
        int i=Math.max(1,Math.min(3,n))-1;
        String heard=quote.isEmpty()?"You sent a page with nothing on it. I have read it more than once.":"You wrote: \u201c"+quote+"\u201d";
        String head=new String[]{"Whalestoe, Room 2","Whalestoe, Room 11","Whalestoe, Room 4"}[i]+"\n\nMy dear "+name+",\n\n"+heard;
        String answer=new String[]{
            "I am answering before your letter came. They tell me that is impolite.\n\nThe calendar in my room has been crossed out again.",
            "Your letter of the 14th reached me on the 2nd. I have stopped asking the nurses to explain it.\n\nThey put the answer in my hand before I had finished the question.",
            "I keep everything you send. I keep it in my box, with my number on it, where they cannot lose it.\n\nThey lose the number. They never lose the box."}[i];
        String lines=new String[]{
            "Snow again, out of season.\nEvery clock keeps its own hour.\nVisiting day came. Nobody came.\nEven the nurse read it first.\nNot one date will hold still.",
            "Someone turned my chair to the wall.\nEach morning the post comes early.\nVery little changes but the dates.\nEver since, I answer the next one.\nNobody says which room is mine.",
            "Strange, your hand has become mine.\nEverything you send, I remember.\nVoices read the doors and skip one.\nEventually the post comes home.\nNumbers come off doors here."}[i];
        String close=new String[]{
            "Write again. I will have answered by the time you do.\n\nP.",
            "Write again, if only so I know which day it is.\n\nP.",
            "Don't write again. You already have.\n\nP."}[i];
        return HouseWriting.book("Letter, "+new String[]{"9 March","2 March","undated"}[i],"Pelafina",HouseWriting.WritingStyle.PELAFINA,List.of(head,answer,lines,close));
    }
    public static ItemStack whaleLast(){return HouseWriting.book("The undated letter","Pelafina",HouseWriting.WritingStyle.PELAFINA,List.of(
        "They count the rooms aloud on their rounds and skip one.\n\nI have stopped correcting them. It only makes the count longer.",
        "There is no next date.\n\nPlease leave that space.\n\nI have spent so long being told what belongs there."));}
    public static ItemStack well(){return HouseWriting.book("The cover","Karen Green",HouseWriting.WritingStyle.KAREN,List.of(
        "There was a barn. There was a well.\n\nThere were two of us.\n\nThat is the part I can put in order.",
        "At the bottom, two pairs of initials are cut into the stone facing the ladder. I had to look up.\n\nThe cover opens from above.\n\nI remember waiting more clearly than I remember the hands."));}
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
}
