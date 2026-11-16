package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import io.github.knaitoe.theoldesthouse.house.HouseWriting;

/**
 * 0.4.621, A Little Longer (docs/A_LITTLE_LONGER_PLAN.md): the work order (the first paper), and what each of the shop's and the
 * flat's things says to a hand. From 0.4.622, what Rowan says before their conversation begins. Every line is flagged to the
 * owner in docs/A_LITTLE_LONGER_0_4_621.md or docs/A_LITTLE_LONGER_0_4_622.md.
 */
public final class LongerTexts {
    private LongerTexts(){}
    public static final String HINT="There is a work order on the spike over the bench.";
    public static final String LAMP_ON="The lamp comes on. Its shade has been mended with tape, carefully.",
        LAMP_OFF="The lamp goes off. The bulb ticks as it cools.",
        BENCH_RADIO="A table set with its back off. Somebody has been in it with a screwdriver tonight; the screws are in a saucer, all but one.",
        FLAT_RADIO_ON="The kitchen radio clicks and warms up, and a tune comes through, clear as anything.",
        FLAT_RADIO_OFF="The tune stops in the middle of itself.",
        TRAYS="Parts trays, every compartment labelled in the same pencil: valves, knobs, grub screws, \"odd\".",
        CHAIR="The chair's front leg has been mended twice: once with a splint, once with a better splint.",
        SHELF="Sets waiting their turn, each with a tag tied to the dial. Most of the tags give a day of the week and nothing else.",
        COUNTER="The counter has been wiped so often the varnish has gone in a pale oval where elbows go.",
        FRONT_DOOR="The shop door will not open. Through its glass the street goes on in the rain: a lamp, a curb, the far pavement, nobody.",
        WINDOW="Rain on the glass. Beyond it the street is exactly where it was.",
        FLAT_WINDOW="Rain runs down the window. The same lamp, the same curb. Nothing goes by.",
        KETTLE="A heavy old kettle, the kind somebody brings with them from one job to the next.",
        CUPS="Two cups that were never a pair. One has a crack down from the rim that has been glued.",
        TOASTER_DOWN="The toaster's lever goes down and stays down. Nothing is in it yet.",
        TOASTER_UP="The toaster gives the lever back with a clack.",
        TABLE="A small table under the window, wiped down, with room for two and no more.",
        ARMCHAIR="The armchair by the table, the good chair, sat into the shape of one person.",
        KITCHEN_CHAIR="A kitchen chair, hard, set where the draught from the window comes in.",
        HOOK="A coat on the hook at the top of the stairs, its pockets weighed down with small things.",
        WORKTOP="The worktop is clear except for crumbs nobody has got round to.",
        SINK="One plate in the rack, one fork, one knife.",
        BASIN="A toothbrush, a tin of tooth powder, a razor on the shelf. A towel folded over the edge to dry.",
        LAVATORY="An ordinary bathroom, cleaner than the rest of the flat.",
        BENCH="A bench under the awning, dry. From here a scrap of sky shows between the tops of the buildings.",
        TANK="A water tank on its stand, rain drumming on its lid.",
        AWNING="Corrugated sheet on a frame, rain running off its front edge in a line.",
        ORDER="A work order on the spike.";
    /**
     * 0.4.622 (docs/A_LITTLE_LONGER_0_4_622.md): Rowan's name over what they say, and what they say to a hand on them before the
     * evening's conversation begins (the next slice): at the bench, the first time and after; at the shelf; at the counter; walking.
     */
    public static final String ROWAN="Rowan",
        ROWAN_BENCH="Nearly. Really, this time.",
        ROWAN_SHELF="I had a valve this size. I know I had one.",
        ROWAN_COUNTER="Nobody has come in all night. I still look.",
        ROWAN_WALKING="Hold on. I'm only going to look at something.";
    /**
     * 0.4.623, chapter 1, the workbench (LongerBench). The proposal's own lines are kept word for word: "Could you hold that side a
     * second?", "There. Thank you.", "That sounded expensive.", "I said I knew where the stairs were.", "You drew them in three places.",
     * "And one of those was right." The rest are new and flagged for the owner in docs/A_LITTLE_LONGER_0_4_623.md.
     */
    public static final String ASK_HOLD="Could you hold that side a second?",
        HOLD_IT="Hold the side of the radio",WATCH="Watch",NOT_NOW="Not right now.",
        ONE_HANDED="Fine. I've done it one-handed before.",
        HOLD_WHERE="The left side. Flat, like that.",HOLD_REMIND="Just the left side. Flat.",
        HOLD_CUE="You hold the back panel flat against the case.",
        THANKS="There. Thank you.",THERE="There.",
        SCREW="Small screw",SCREW_GONE="No. No, no.",
        SCREW_BACK="That's the one. I'd have been on my knees for an hour.",
        SCREW_SPARE="Never mind. There's another in the tray.",
        LAMP_BETTER="Better. Leave it there.",LAMP_TURNED="You turn the lamp onto the radio.",
        EXPENSIVE="That sounded expensive.",STANDING="Sorry. You've been standing there.",
        ROUTE_TRIED="I've tried every door on that side. Here.",
        ON_PAPER="The paper",ROUTE="In pencil: back door. Corridor. Left at the second door, crossed out. Stairs? Stairs? Stairs.",
        DOUBT="Are you sure about the stairs?",STAIRS="I said I knew where the stairs were.",
        THREE="You drew them in three places.",RIGHT="And one of those was right.",
        WHICH="Which turn didn't work?",WHICH_ANSWER="The one with the line through it. It went back to here.",
        LEAVE="Leave it there",
        ROWAN_AFTER="I'm leaving it alone tonight. It can think about what it did.";
    /**
     * 0.4.624, chapter 2, the kettle (LongerKettle). The proposal's own lines word for word: "I'm making tea.", "Is that part of the
     * repair?" (the reader's), "No.", "You could stay for it.", "You don't have to be polite.", "I'm being brave." (the reader's),
     * "That's worse." The rest are new and flagged for the owner in docs/A_LITTLE_LONGER_0_4_624.md.
     */
    public static final String MAKING_TEA="I'm making tea.",PART="Is that part of the repair?",NO="No.",SAY_NOTHING="Say nothing",
        STAY_FOR="You could stay for it.",YES="I'd like that.",ONE_CUP="One cup.",KEEP_GOING="I should keep going.",
        GO_ON="All right. It's the same door you came in by.",STILL_THERE="Kettle's still there.",
        POURED="The one without the crack's yours.",TOOK_CUP="You take the cup without the crack.",
        NOTICED_LINE="This is the good chair. I know. I'm not getting up.",
        GOOD_TAKEN="You found the good one. That's fine. That's completely fine.",
        OPPOSITE="Is it cold by the window? Pull the chair in. It's warmer this side.",
        STAND_SIT="You can sit. That chair doesn't bite. This one does, a bit.",
        HELPING="Two slices. The left side burns, so watch it.",
        TOAST_ASK="Push the toaster down, would you? It's the only thing in this flat I can cook.",
        HEAT="It's fine. The heat's perfectly manageable.",RESCUED_LINE="Show-off.",
        TEASE="Perfectly manageable.",TEASED_LINE="It was until it wasn't.",
        OFFER="Give them the good slice",OFFERED_LINE="Then you've got the burnt one. I won't stop you.",
        BRAVE_ASK="I'll have the burnt one.",POLITE="You don't have to be polite.",BRAVE_LINE="I'm being brave.",WORSE="That's worse.",
        TOASTER_DOWN_NOW="The lever goes down on two slices. The elements tick.",
        ONE_BURNT="The toaster throws up two slices. The left one is black down one side.",
        KETTLE_ON="Kettle's on. Sit down somewhere.",
        ROWAN_KETTLE_AFTER="Sit as long as you like. I'm not going anywhere. I've checked.",
        Q_MUSIC="Music, or quiet? I never know which people want.",A_MUSIC="Music. Quiet in here and I start listening for things.",
        Q_PLAN="Do you like having a plan?",A_PLAN="I have a list. I don't do what's on it.",
        Q_FIRST="First thing you'd do, out there?",A_FIRST="Find out what day it is.",
        ROWAN_TEA="Drink it while it's hot. It won't be, for long.";
    /**
     * 0.4.625, chapter 3, the radio (LongerRadio). The proposal's own lines word for word: "We should be listening for the doors." and
     * "They keep changing." and "What did?" and "Why?" (the reader's), "We have been listening for the doors.", "I know.", "That came out
     * badly.", "I wanted a minute where it was just a broken radio.", "When you finish that, come outside with me.", "Because I want to
     * show you something.", "And because you keep finding reasons to stand up." The rest are new and flagged in
     * docs/A_LITTLE_LONGER_0_4_625.md.
     */
    public static final String PUT_IT_ON="Put the radio on, would you? That one works. I fixed it in March.",
        LEAVE_IT_ON="Leave it on?",LEAVE_IT="Leave it on.",SUITS="It suits you, having it on.",TURN_IT_DOWN="Could we turn it down?",
        HEARD="Something could hear that.",GOOD="Good.",IT_DOES="It does, doesn't it.",
        LISTENING="We should be listening for the doors.",HAVE_BEEN="We have been listening for the doors.",CHANGING="They keep changing.",
        I_KNOW="I know.",SHARP="Then you listen. I'll do the cups.",
        NOT_MUSIC="It's not about the music.",WHY_MATTER="Why does it matter so much?",LET_PASS="Let it pass",SORRY="Sorry. I didn't mean to spoil it.",
        BADLY="That came out badly.",WHAT_DID="What did?",BROKEN_RADIO="I wanted a minute where it was just a broken radio.",
        WANTED_TOO="I wanted that too.",STILL_REAL="The doors are still real.",JUST_QUIETER="Keep it on. Just quieter.",
        BOTH_DID="Then we both did.",QUIETER_THEN="I know they are. Quieter, then. Not off.",LIVE_WITH="I can live with quieter.",
        COME_OUTSIDE="When you finish that, come outside with me.",WHY="Why?",SHOW_YOU="Because I want to show you something.",
        STAND_UP="And because you keep finding reasons to stand up.",BRING_CUP="Bring your cup. Come and sit outside with me a while.",
        GO_WITH="Go with them",IN_A_MINUTE="In a minute.",UP_THERE="I'll be up there.",ROOF_WAIT="It's up here. Come and look.";
    /**
     * 0.4.626, chapter 4, the roof (LongerRoof). The proposal's own lines word for word: "I thought I'd be braver if something like this
     * happened.", "You got the lights back on." (the reader's), "That was wiring. I'm good at wiring.", "Was there really something you
     * wanted to show me?" and "The sky?" (the reader's), "Yes.", "At first." The rest are new and flagged in docs/A_LITTLE_LONGER_0_4_626.md.
     */
    public static final String THERE_SKY="There. Between those two. That's the sky.",NOT_SURE_SKY="I'm not sure it's a real one. I like looking at it anyway.",
        IS_IT_REAL="Is it real?",REAL_RAIN="Real enough to get rain out of.",SIT_BESIDE="Sit down beside them",MAKE_ROOM="Rowan moves their cup to make room.",
        NEARER_BENCH="You would have to be nearer the bench.",SAY_NOTHING_WHILE="Say nothing for a while",NICE_QUIET="This is nice. Saying nothing.",
        BRAVER="I thought I'd be braver if something like this happened.",LIGHTS_ON="You got the lights back on.",WIRING="That was wiring. I'm good at wiring.",
        IT_MATTERS="It matters, though.",FRIGHTENED_TOO="I'm frightened too.",GOOD_AT_WIRING="You're good at a lot of wiring.",SIT_WITH_IT="Sit with it",
        MAYBE="Maybe.",NOT_JUST_ME="Good. I mean, not good. Good that it's not just me.",TWO_THINGS="Two things. I'm good at two things.",
        BREATHES_OUT="Rowan breathes out, and leaves it there.",
        REMEMBER_HELD="You held the panel. Most people would have watched.",REMEMBER_SCREW="You picked that screw up off the floor. I'd have been on my knees all night.",
        REMEMBER_LAMP="You moved the lamp before I asked. I noticed that.",REMEMBER_DOOR="You waited at the door before you came in. People don't, usually.",
        GLAD_STOPPED="I'm glad I stopped.",GLAD_STAYED="I'm glad you stayed.",
        REALLY_SHOW="Was there really something you wanted to show me?",YES_SHOW="Yes.",THE_SKY_Q="The sky?",AT_FIRST="At first.",
        SIT_CLOSER="Can I sit closer?",YOU_CAN="You can.",HERE_HAND="Here.",TAKE_IT="Take their hand",NOT_YET="Not yet.",ALL_RIGHT="All right.",
        HAND_TAKEN="Their hand is warm from the cup.",
        CAN_I_START="Can I...",PIGEON="There's a pigeon. Sorry. There isn't a pigeon.",CAN_I_KISS="Can I kiss you?",KISS_YES="Yes.",NOT_TONIGHT_R="Not tonight.",
        STILL_HERE="That's all right. I'll still be here.",CAN_I_KISS_YOU="Can I kiss you?",KISS_YES_R="Yes.",COME_HERE="You'll have to come over here for that.",
        LEANS_IN="Rowan leans in. The rain keeps on.",
        FRIENDS="I'd like us to be friends.",FRIEND_ON_ROOF="Then I've got a friend on a roof. That's more than I had this morning.",
        GO_BACK_IN="Go back in",COLLAR="Come on. It's getting into my collar.",INDOORS_AGAIN="Warmer in here. Just about.";
    /**
     * 0.4.627, the return indoors (LongerReturn). The proposal's own lines word for word: "I was going to say there's something wrong
     * with the radio again.", "Is there?" (the reader's), "No. I just don't want you to go yet.", "You could have just said that." (the
     * reader's), "I'm trying." The rest are new and flagged in docs/A_LITTLE_LONGER_0_4_627.md.
     */
    public static final String RADIO_AGAIN="I was going to say there's something wrong with the radio again.",IS_THERE="Is there?",
        DONT_GO_YET="No. I just don't want you to go yet.",MAKE_SOMETHING_UP="I was going to make something up about the radio. I don't need to, do I.",
        NO_R="No.",GOOD_DONT_GO="Good. I don't want you to go yet.",DONT_WANT_EITHER="I don't want to go either.",JUST_SAID="You could have just said that.",
        HOPING_TOO="I was hoping for another excuse too.",LET_IT_STAND="Let it stand",DOESNT_LOOK_AWAY="Rowan looks at you, and doesn't look away.",
        TRYING="I'm trying.",BOTH_HOPING="Then we were both hoping.",NEITHER_FILLS="Neither of you fills the quiet.",
        WASH_CUPS="Wash the cups",WASH_DRY="Rowan washes. You dry. The radio plays.",ABOUT_TOAST="The toast wasn't that bad.",
        CHARCOAL="It was charcoal. We both know it was charcoal.",SIT_DOWN="Sit down",SITS_TOO="Rowan sits. Nobody says the next thing for a while.",
        STAY_LONG="Stay as long as you like.";
    /**
     * 0.4.628, chapter 5, tomorrow (LongerTomorrow). The proposal's own lines word for word: "What do you want for breakfast?",
     * "Something you haven't set on fire." (the reader's), "All right. You're making it.", "What do we need?", "You already gave me the
     * good cup." (the reader's), "You can have it tomorrow too." The rest are new and flagged in docs/A_LITTLE_LONGER_0_4_628.md.
     */
    public static final String HEAR_THAT="Did you hear that?",FOOT_LEFT="That was a foot to the left this morning.",FLOOR_NEW="And that floor wasn't there.",
        DONE_BEFORE="Has it done this before?",NOT_WATCHING="Not since I've been watching it.",LOOK_FLOOR="Look at the floor",
        PALE_TILES="Pale tiles, clean, laid by nobody. They stop at the door.",GO_SEE="We should go and see.",WE_SHOULD="We should.",
        NOT_ON_MY_OWN="I'm not staying here on my own with it doing that. Give me a minute.",COAT_ON="Rowan hangs up the apron and puts their coat on.",
        WHAT_BREAKFAST="What do you want for breakfast?",NOT_ON_FIRE="Something you haven't set on fire.",YOURE_MAKING_IT="All right. You're making it.",
        GOOD_CUP="You already gave me the good cup.",CUP_TOMORROW="You can have it tomorrow too.",PICK_NEXT_TIME="You can pick what's on next time. Within reason.",
        WANT_BREAKFAST="We'll want breakfast. Wherever we are.",FINDS_SCRAP="Rowan finds a scrap of paper and a pencil stub.",WHAT_NEED="What do we need?",
        ADD_TEA="And tea. Proper tea.",ADD_TEA_FOR_ME="And tea, for me.",SCRAP_GIVEN="Rowan writes it under yours and hands you the scrap.",SCRAP_NAME="A scrap of paper",
        READY_WHEN="Ready when you are.",GO_TOGETHER="Let's go together.",TOMORROW_MATTERS="I'd like that. Tomorrow, I mean.",CARRY_BREAD="You're carrying the bread.",
        FRIENDS_ROAD="Friends on the road, then?",GOOD_GO="Good.",DAY_AFTER="So would I. And the day after.",SO_WOULD_I="So would I.",
        CARRY_EVERYTHING="I'm carrying everything. You've got the list.",FRIENDS_ROAD_R="Friends on the road.";
    /** 0.4.629, chapter 6, the cut (LongerCut). One word from Rowan, the proposal's "Wait."; the rest are cues, flagged in docs/A_LITTLE_LONGER_0_4_629.md. */
    public static final String UNPLUG="Rowan goes to pull the toaster's plug out before you go.",RADIO_STOPS="The radio stops in the middle of the phrase.",
        EDGE="By the toaster the wall has an edge it did not have, and it goes back further than any wall is thick.",WAIT_R="Wait.",
        WALL_AGAIN="Where the wall was there is a shallow space with bare brick at the back of it.";
    /** The reader's answers to Rowan's three questions, in order of the questions. */
    public static final java.util.List<java.util.List<String>> ANSWERS_NOW=java.util.List.of(
        java.util.List.of("Music.","Quiet.","Whatever you'd have on."),
        java.util.List.of("Yes.","Not really.","Depends whose."),
        java.util.List.of("Eat something that isn't toast.","Sleep.","I don't know yet."));
    /** The reader's four first questions, and Rowan's answers, in order. */
    public static final java.util.List<String> QUESTIONS=java.util.List.of("Where is this?","What's wrong with it?","It's going well, then.","I'm only passing through."),
        ANSWERS=java.util.List.of("My shop. It was on Carver Street until about a week ago. Now it's on whatever this is.",
            "It picks something up late at night and loses it again. I'd like to know what.",
            "It's going exactly as well as it sounded.",
            "Everybody is. I've tried most of the doors.");
    private static List<Component> pages(String... texts){var out=new ArrayList<Component>();for(String text:texts)for(String p:LookoutTexts.pages(text,150))out.add(HouseWriting.page(HouseWriting.WritingStyle.PLAIN,p));return out;}
    /** The first paper: the unfinished work order on the spike over the bench, in Rowan's pencil. */
    public static ItemStack source(){
        return HouseWriting.book("Work order","R.",pages(
            "WORK ORDER\n\nSet: table radio, walnut case.\nFault: hums. Will not hold a station.\nPromised: Friday.",
            "Valves tested, all good. Back panel off. One screw short; it went somewhere.\n\nIt finds something late at night and then loses it again. Not interference. Not the aerial.",
            "Shop closed until further notice.\n\nR."));
    }
    /** The account. Nothing asks for it until the evening can end; it says only what anybody could see of the place. */
    public static ItemStack account(String name,CompoundTag own){
        return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.LITTLE_LONGER),name,pages(
            "There was a radio repair shop with a flat over it, and rain on every window, and a street outside that nobody walked along."));
    }
}
