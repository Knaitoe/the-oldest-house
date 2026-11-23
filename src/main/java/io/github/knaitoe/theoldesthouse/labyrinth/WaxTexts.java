package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.749, the waxworks, part 1 (docs/WAXWORKS_0_4_749.md): what the museum's things say to a hand, its plaques, and the notice by the
 * time clock, typed by the day man for whoever has the night. Nobody speaks; the figures are wax. Every line is new in this release and is
 * flagged to the owner in docs/WAXWORKS_0_4_749.md.
 */
public final class WaxTexts {
    private WaxTexts(){}
    public static final String HINT="There is a typed notice pinned up by the time clock.";
    public static final String PLINTH="A plinth, painted to look like stone. The paint is worn through at the front, where people lean.",
        PLAQUE="A brass plate on a short post. Somebody polishes it.",
        ROPE="A brass post with a rope of velvet run through its ring. The rope sags exactly as much as the one beside it.",
        CAMERA="A camera on a bracket up in the corner. A small lamp on it is lit, and the lens is turned on the room.",
        MONITOR="A screen in a steel case. It shows a gallery from a corner, grainy and flat. Nothing in it moves.",
        DESK="The guard's desk, scarred along the edge where a long line of elbows has rested.",
        DESK_LOG="The shift log lies open on the desk. The last page is ruled for the hours from midnight to six. Nothing is written on it yet.",
        DESK_PHONE="A desk telephone, a list of numbers taped beside it: the police, the day man, the alarm company. The receiver is warm.",
        CHAIR="A swivel chair with a split in the seat, mended with tape.",
        CLOCK="The office clock says eight minutes to midnight. Its second hand moves in small jerks.",
        TIMECLOCK="The time clock takes a card and stamps it. The card waiting in the slot has tonight's date typed on it, and a blank for the name.",
        LOCKER="A locker with a paper label on the door: NIGHTS. It is shut. Something inside it shifts on a hook when the door is touched.",
        LOCKER_OPEN="The NIGHTS locker opens on a coat nobody has worn in years and a flashlight on a hook. The flashlight works.",
        LOCKER_EMPTY="The hook in the NIGHTS locker is empty. The coat is still there.",
        LIGHT_KEPT="The flashlight belongs to the museum. It goes back on its hook at the end of the night.",
        FRONT="The front doors, glass in bronze. Locked from the outside with a chain. The street beyond the glass is dark.",
        SIGN="MERRIMAN'S, in gilt letters on a painted board. Under it, smaller: FIGURES FROM LIFE.",
        BOOTH="The ticket booth. A roll of tickets on its spindle, a tin for change, and a bell for service that nobody rings.",
        TABLE="Supper on the table: wax potatoes, a wax roast, a jug of wax milk with a skin on it. Every fork is laid square.",
        DRESSER="A kitchen dresser with its plates set out on edge. One plate is real; it is the only one with dust on it.",
        STOVE="An iron stove, cold. A kettle on it, and painted steam coming out of the spout.",
        COFFIN="A coffin with its lid propped against the wall. The lining is satin, and it holds the shape of somebody who is sitting up.",
        GRAVESTONE="A stone with a name worn off it. A little moss, made of felt.",
        VAT="A vat for melting wax, cold now. A skin has set across the top with a thumbprint in it.",
        BENCH="The modeller's bench: knives, spatulas, a dish of glass eyes sorted by size.",
        BENCH_HEAD="A head on a stand at the bench, bald, its eyes not set. The sockets have been smoothed and are waiting.",
        BENCH_HANDS="Hands laid out in a row on a cloth, each one a little different, as if they were taken from different people.",
        HEADS="A shelf of finished heads, each on its own stand, each turned the same way. They have eyes.";
    /** What each figure's plaque says, in the order of WaxSite.FIGURES. The twenty-fourth has none. */
    private static final String[] PLAQUES={
        "EDWIN MERRIMAN. Founder of this museum. Modelled from his own death mask, as he asked.",
        "THE DOORMAN. Stood at these doors for thirty-one years. He never missed a night.",
        "THE TICKET SELLER. She gave every visitor exact change and the same smile.",
        "A STATESMAN. He spoke for nine hours without notes.",
        "A STATESMAN. The scroll in his hand is the treaty he would not sign.",
        "A STATESMAN. Modelled from life. He sat for the modeller forty times.",
        "A GENERAL. He is pointing at the hill where he lost his army.",
        "A JUDGE. He sentenced eleven people to hang, and wept at every one.",
        "A FIRST LADY. Her gown is her own, given to the museum by her family.",
        "AN ORATOR. When he spoke, it is said, the crowd forgot to breathe.",
        "SUPPER: THE FATHER. A family at their table, as families were.",
        "SUPPER: THE MOTHER. She has served everyone but herself.",
        "SUPPER: THE DAUGHTER. She is waiting to be told she may begin.",
        "SUPPER: THE SON. His hands are under the table. Nobody has asked him why.",
        "THE CRYPT: THE RISEN. Visitors are asked not to step over the rope.",
        "THE CRYPT: THE GRAVEDIGGER. He was paid to dig, and did not ask what for.",
        "THE CRYPT: THE SLEEPWALKER. She walks here every night, and remembers nothing in the morning.",
        "THE CRYPT: THE WATCHER. Nobody has seen the face under the hood. Not even the modeller.",
        "THE CRYPT: THE COUNT. He is pointing at whoever stands where you are standing.",
        "THE WORKSHOP: THE APPRENTICE. He is skimming the vat, as he was taught.",
        "THE WORKSHOP: THE MODELLER. The museum's own. Every figure here passed through his hands.",
        "THE WORKSHOP: A FIGURE IN PROGRESS. Not yet finished.",
        "THE WORKSHOP: UNDER WRAPS. The museum's next figure. Not to be uncovered before it is finished."};
    public static String plaque(int i){return i<PLAQUES.length?PLAQUES[i]:PLAQUE;}
    /** What a hand finds on each figure, in the same order; the last is the one with no plaque. */
    private static final String[] TOUCH={
        "Cold, and a little tacky. The face is heavier than a face should be.",
        "His glove is real leather, gone hard. Under it the hand is wax.",
        "Her fingers rest on the roll of tickets. One of them has left a small dent in it.",
        "The lapel is real cloth. The hand on it is wax, with a fine down of real hair on the back.",
        "The scroll is paper. The hand that holds it is wax, cold through.",
        "Cold, smooth, a little greasy, the way a candle is. Its eyes are glass, and very clear.",
        "The pointing arm is held up on a rod you can feel through the sleeve.",
        "His robe is real and smells of a cupboard. The face under the wig is wax.",
        "The gown is silk, gone thin. Her skin is wax, finer than the rest.",
        "Cold, and the jaw is open a finger's width, as if he were about to go on.",
        "His fork is real. His hand on it is wax, and very slightly warm from the room.",
        "The dish in her hands is real china. She is wax to the elbows.",
        "She is smaller than she looks. Wax, cold, her hair real and dry.",
        "His hands, under the table, are folded. Wax.",
        "Wax, sitting up out of the satin. The hands on the coffin's edges grip it.",
        "The spade is real. The hands on it are wax, with real dirt pressed into the knuckles.",
        "The candle in her hand is wax too. So is the flame.",
        "The robe is coarse and real. Under the hood, where the face would be, the hand finds wax.",
        "Wax, and his finger points past you no matter where you stand.",
        "His sleeves are rolled. His forearms are wax, with a burn painted on one.",
        "The modeller's apron is real and stiff with old wax. So is he.",
        "Half a figure: the left side finished, a face and a hand; the right side bare armature and the start of a cheek.",
        "A sheet over a standing shape. The shape is cold through the sheet.",
        "Cold and smooth, like the others. There is no plaque, and nothing on the floor says where it is meant to stand."};
    public static String touch(int i){return i<TOUCH.length?TOUCH[i]:TOUCH[TOUCH.length-1];}
    private static List<Component> pages(String... texts){var out=new ArrayList<Component>();for(String text:texts)for(String p:LookoutTexts.pages(text,150))out.add(HouseWriting.page(HouseWriting.WritingStyle.PLAIN,p));return out;}
    /** The first paper: the notice by the time clock, typed by the day man. The figures are counted from the place as it is built: twenty-three on the plaques. */
    public static ItemStack source(){
        return HouseWriting.book("Notice to nights","R. Toller, days",pages(
            "NIGHTS.\n\nPunch in at the clock. Flashlight is in the locker. The lights in the galleries stay off. That is policy and it saves the museum money.",
            "Rounds on the hour. Walk all five rooms. Count the figures in each one and write the count in the log. There are twenty-three. If you count more or less, count again.",
            "The cameras are for the insurance. Watch them anyway. If anything has been moved, write down which and where. Do not move it back. That is my job.",
            "Phone is on the desk. You will not need it.\n\nThey are wax. I have done nights for nine years. They are wax.\n\nR.T."));
    }
    /** The account. Nothing asks for it until part 3; it says only what anybody could see of the place. */
    public static ItemStack account(String name,CompoundTag own){
        return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.WAXWORKS),name,pages(
            "A wax museum after closing, with no lights on in its galleries. Statesmen, a family at supper, a crypt, a workshop. "
                +"A desk under six screens. The notice by the clock said there were twenty-three."));
    }
}
