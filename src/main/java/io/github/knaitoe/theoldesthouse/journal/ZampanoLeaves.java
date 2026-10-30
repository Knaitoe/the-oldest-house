package io.github.knaitoe.theoldesthouse.journal;

import io.github.knaitoe.theoldesthouse.journal.JournalPage.Placed;
import io.github.knaitoe.theoldesthouse.journal.JournalPage.Shape;
import java.util.List;

/**
 * Zampanò's pages in the courtyard's filing cabinets (JOURNAL_DESIGN.md, step 2): nine folders of two leaves, written for
 * the courtyard in his manner and drawn by the journal's page renderer in his hand. Each leaf turns, spirals, cascades,
 * boxes or mirrors its words, so the reader turns the page, lifts it to the light or reads it from the back. None of it is
 * quoted from the book. Shared by the server, which knows which leaves a reader has pulled, and the screen.
 */
public final class ZampanoLeaves {
    public static final int CABINETS=3, DRAWERS=3, FOLDERS=CABINETS*DRAWERS, LEAVES=FOLDERS*2;
    /** A run as he left it; struck runs are crossed through, as his Minotaur passages are. */
    public record Run(Placed placed, boolean struck) {}
    public record Leaf(String title, String text, int wear, List<Run> runs) {}
    private ZampanoLeaves(){}

    static Run line(String t,float x,float y,float angle,float size){return new Run(new Placed(t,Shape.LINE,x,y,angle,size,false),false);}
    static Run struck(String t,float x,float y,float angle,float size){return new Run(new Placed(t,Shape.LINE,x,y,angle,size,false),true);}
    static Run box(String t,float x,float y,float angle,float size,boolean mirrored){return new Run(new Placed(t,Shape.BOX,x,y,angle,size,mirrored),false);}
    static Run column(String t,float x,float y,float angle,float size){return new Run(new Placed(t,Shape.COLUMN,x,y,angle,size,false),false);}
    static Run cascade(String t,float x,float y,float angle,float size){return new Run(new Placed(t,Shape.CASCADE,x,y,angle,size,false),false);}
    static Run spiral(String t,float x,float y,float angle,float size){return new Run(new Placed(t,Shape.SPIRAL,x,y,angle,size,false),false);}
    static Leaf leaf(String title,String text,int wear,Run... runs){return new Leaf(title,text,wear,List.of(runs));}

    private static final String[] NAMES={"Beatrice","Leonie","Pauline","Ruth","Anne","Esther","Helen"};
    private static Run[] names(){
        var runs=new Run[NAMES.length+1];
        for(int i=0;i<NAMES.length;i++){double a=Math.toRadians(-90+i*360.0/NAMES.length);runs[i]=line(NAMES[i],(float)(.5+Math.cos(a)*.17),(float)(.5+Math.sin(a)*.13),(float)Math.toDegrees(a),.9F);}
        runs[NAMES.length]=line("one chair empty",.37F,.5F,0,.6F);return runs;
    }
    private static Run[] compass(){
        var runs=new Run[8];for(int i=0;i<8;i++)runs[i]=line("     which way",.5F,.48F,i*45-90,.9F);return runs;
    }

    private static final List<Leaf> ALL=List.of(
        leaf("I. Measurements",
            "The hall was measured three times: once with a cloth tape, once with a surveyor's chain, and once by counting paces in the dark. Only the third was never contradicted, because nobody could check it.",1,
            line("inside, longer. outside, unchanged.",.07F,.93F,-90,.7F),
            box("¹ Ruth read the figures back in a different order each time. I have kept all three orders.",.2F,.7F,-3,.8F,false)),
        leaf("",
            "Each night the figure grew.",0,
            cascade("by a thumb, then a hand, then nobody would hold the tape",.06F,.2F,0,.78F),
            line("measure it again",.86F,.92F,180,.9F)),
        leaf("II. Echoes",
            "A word spoken in the hall comes back late, or not at all. Hartwell blames the timber. I have never known timber to keep anything so long.²",2,
            line("hello",.18F,.44F,0,1.3F),line("hello",.42F,.49F,4,.9F),line("hello",.62F,.53F,9,.6F),
            box("² Hartwell, The Acoustics of Vacant Rooms (1958), 112-14. His figures were taken in a church.",.13F,.61F,0,.72F,false),
            line("The hall is not a church,",.1F,.87F,-2,.7F),line("though it has been asked to be one.",.1F,.92F,-2,.7F)),
        leaf("",
            "",1,
            spiral("the voice goes out and turns and turns and comes back smaller and turns and does not come back",.5F,.45F,0,1),
            line("³ It did not come back.",.22F,.9F,0,.75F)),
        leaf("III. The plan",
            "Ruth asked me to describe the plan. I gave her the rooms in the order they were entered:",0,
            box("hall, landing, stair, hall, door, hall, door, the same hall, a hall not on the plan, stair, hall",.2F,.38F,0,.85F,false),
            line("She asked where it ends.",.12F,.8F,-2,.75F),line("It does not end. It was put down.",.12F,.85F,-2,.75F)),
        leaf("",
            "",2,
            box("hall, landing, stair, hall, door, hall, door, the same hall, a hall not on the plan, stair, hall",.8F,.38F,0,.85F,true),
            struck("The Minotaur was fed at the centre",.1F,.77F,-2,.75F),
            struck("because the centre was the one room",.1F,.82F,-2,.75F),
            struck("nobody had to walk past.",.1F,.87F,-2,.75F)),
        leaf("IV. The stair",
            "Esther counted the stair aloud while I wrote. She stopped at a number I will not put down, and began again from one.",1,
            cascade("one two three four five six seven eight nine",.12F,.36F,0,.9F),
            line("one",.12F,.92F,0,.9F)),
        leaf("",
            "",0,
            line("the bottom is not where it was",.88F,.1F,180,.9F),
            line("⁴ Esther's count, as I have it:",.1F,.85F,0,.6F),line("one to nine, one to nine, one to seven.",.1F,.89F,0,.6F)),
        leaf("V. The readers",
            "Two columns. I have not decided which of them is the survey.",1,
            column("What Leonie read: the door at the end of the hall was shut.",.1F,.28F,0,1.1F),
            column("What I wrote down: the door at the end of the hall was not there.",.55F,.28F,0,1.1F)),
        leaf("",
            "",2,names()),
        leaf("VI. Notes on the notes",
            "The survey has more notes than survey.⁵ I am told this is a fault.⁶",0,
            box("⁵ See note 6.",.16F,.34F,-4,.85F,false),
            box("⁶ See note 5.",.56F,.52F,5,.85F,false),
            line("⁷ There is no note 7.",.07F,.62F,-90,.6F),line("The number was on the page when Anne turned to it.",.08F,.92F,0,.55F)),
        leaf("",
            "",1,
            line("⁸ continued from a page I can no longer find,",.9F,.06F,90,.7F),
            line("which Pauline says was never written,",.9F,.92F,180,.7F),
            line("and is therefore the truest page in the survey.",.1F,.94F,-90,.7F),
            spiral("see note 8 see note 8",.5F,.5F,0,.8F)),
        leaf("VII. The growl",
            "Below the camp there is a sound like breath let out through stone. Helen called it a growl. I asked her for a smaller word. She did not have one.",2,
            line("growl",.14F,.58F,0,.7F),line("growl",.3F,.65F,0,1.1F),line("growl",.44F,.74F,0,1.6F),line("GROWL",.18F,.88F,0,2.3F)),
        leaf("",
            "",0,
            line("and then",.12F,.16F,0,1.4F),line("it is",.3F,.28F,0,1.1F),line("quiet,",.44F,.38F,0,.9F),line("which is",.56F,.46F,0,.7F),line("worse",.66F,.52F,0,.5F),
            struck("It is not the Minotaur.",.2F,.84F,-3,.85F)),
        leaf("VIII. Doors",
            "Every door in the house opens, at least once, onto the hall it was opened from.",1,
            box("a door which is also a wall",.3F,.42F,0,1.1F,false),
            line("knock",.14F,.32F,-20,.8F),line("knock",.7F,.36F,15,.8F),line("knock",.42F,.72F,-5,.8F)),
        leaf("",
            "",2,compass()),
        leaf("IX. Last leaves",
            "I have written the plan of the hall on top of the plan of the hall. The second is more correct and harder to read.",2,
            line("the hall",.16F,.14F,-6,1.2F),line("the hall the hall",.24F,.22F,3,1),line("longer",.5F,.3F,-2,1.4F),line("it is longer than it was",.1F,.38F,7,.9F)),
        leaf("",
            "",1,
            line("I have stopped measuring.",.22F,.42F,0,.9F),
            line("It is the house that has been measuring.",.12F,.58F,-2,.7F),
            struck("the Minotaur is",.3F,.85F,0,.85F)));

    public static Leaf leaf(int index){return ALL.get(index);}
    public static int count(){return ALL.size();}
    /** The two leaves in folder f: drawer d of cabinet c is folder c*DRAWERS+d, and holds leaves 2f and 2f+1. */
    public static int first(int folder){return folder*2;}
    public static int folderOf(int leaf){return leaf/2;}
    public static boolean valid(int leaf){return leaf>=0&&leaf<ALL.size();}
    public static String titleOf(int leaf){var own=ALL.get(leaf).title();return own.isEmpty()?ALL.get(leaf&~1).title():own;}
}
