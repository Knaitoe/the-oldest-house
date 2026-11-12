package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.518, the Pell house: what is written. All of it is original; only the shape of the story is taken from its source
 * (docs/PELL_HOUSE_PROPOSAL.md). Nothing here says what the reader is like, how they feel, or what they are called.
 */
public final class PellTexts {
    private PellTexts(){}
    public static final String HINT="The house at the head of the street is the one with the green lawn.";
    public static final String JUNES_DOOR="June's room. The door is shut.",PADLOCKED="Padlocked.",POOL_GATE="The gate is padlocked. The lock is new and the latch is old.",
        FIXED_GLASS="This half doesn't slide.",ROLLER="It hasn't been up in years. The runners are painted over.",NOBODY_HOME="Nobody comes. The blinds are down.";
    /**
     * 0.4.522: what a reader finds with a hand on each of the house's things, by its kind and stage. They say what is there; none
     * says what it means, and none names a colour or an age.
     */
    public static String thing(PellThing.Kind kind,int stage){
        return switch(kind){
            case COUNTER->"A laminate bench, wiped clean.";
            case SINK->"The sink is under the window onto the pool.";
            case STOVE->"The stove. Every knob is turned to off.";
            case FRIDGE->"The fridge hums.";
            // Stage 0: the girl's photographs. Stage 1 (from the second visit): and the two new ones of the doorstep.
            case FRIDGE_TOP->stage==0?"Photographs of one girl, a year to each. On an old one a magnet sits square over a face."
                :"Photographs of one girl, a year to each, and two new ones at the edge: the doorstep, the day of the placement.";
            // 0 a handle in every slot; 1 one slot empty; 2 the block gone.
            case KNIFE_BLOCK->stage==0?"A knife block. A handle in every slot.":stage==1?"A knife block. One slot is empty.":"A clean square on the bench where the knife block stood.";
            case LUNCHBOXES->"Two lunchboxes. One has a name label. On the other the label has been scratched off.";
            // 0 shut; 1 open, and the knife block put away in it.
            case CUPBOARD_UPPER->stage==0?"Plates, cups, a stack of bowls.":"The knife block is in the cupboard, behind the plates. Every slot is empty.";
            case TABLE->"The kitchen table. Four chairs.";
            case CHAIR->"A vinyl chair. One seam has split and been taped.";
            case CHORE_CHART->"A chore chart. CALLUM; then TILLY, on a strip of paper stuck over another name. Its corner has lifted.";
            case CALENDAR->"The calendar is still on the month she died. Tilly's appointments are written in its margins.";
            case RECLINER->"A recliner turned to the television, not the window. One arm is worn through where a hand rests.";
            // 0 off; 1 on.
            case TV_CABINET->stage==0?"Under the television, tapes in a row, each labelled with a birthday.":"The television is on. A girl is blowing out candles.";
            case MANTEL->"Cards along the mantel, every one turned face down.";
            case WINDOW_SEAT->"The window seat faces the pool.";
            case SIDE_TABLE->"By the chair, a side table: the remote, a mug gone cold under a skin, and a tape's case, open and empty. Its label is a birthday.";
            // 0 off; 1 on.
            case FLOOR_LAMP->stage==0?"A standard lamp, its shade tipped toward the chair.":"The lamp is on. Its light falls on the chair and nowhere else.";
            case PHOTOS->"School photographs, one for each year. The same girl, a little older in each.";
            case HOOKS->"Picture hooks, already hammered in at the same spacing. Nothing hangs on them.";
            // 0 the coat and the cane; 1 the cane gone; 2 both gone.
            case COAT_HOOK->stage==0?"A small coat on one peg. On the other, a cane.":stage==1?"A small coat on one peg. The other is bare.":"Two bare pegs.";
            case BED->"The bed is made. At its edge the quilt is pressed flat where someone sits.";
            case DRAWERS->"Raised dots on the drawers' labels.";
            case DESK->"A desk with a magnifier on a stand, angled over the blotter.";
            case BOOKSHELF->"Large-print books, the titles big enough to read across the room. One is out.";
            // Stage bit 1: open.
            case WARDROBE->(stage&2)==0?"The wardrobe.":"Clothes on hangers. Leaning in the corner, a cane like Tilly's, shorter.";
            case SWITCH->"Textured tape round the light switch, so it can be found by touch.";
            case MARKS->"Pencil heights up the door frame, dated, stopping. Under the last, a fresh one, the day's date, and a small N.";
            // 0 the border; 1 where it was steamed off and put back.
            case BORDER->stage==0?"A wallpaper border of small animals along the top of the wall.":"Behind the bed the border has been steamed off and put back, not quite straight.";
            case MATTRESS->"A mattress on the floor. No bed.";
            case CIRCLE->"A circle painted on the floor round the mattress. The toys stand on its line, facing out.";
            case SHOES->"Shoes lined up at the door, laces tucked in.";
            // 0 no knives; 1 a knife among them.
            case CUTLERY->stage==0?"A drawer of the house's missing cutlery: spoons and forks in rows. No knives.":"A drawer of the house's missing cutlery: spoons and forks in rows, and a knife.";
            case VANITY->"Two toothbrushes in the cup. One is dry and old, and kept.";
            // 0 clear; 1 steamed.
            case MIRROR->stage==0?"The mirror over the basin.":"The steam on the mirror shows a word written with a finger: NESSA.";
            case BATH->"A non-slip mat in the bath. On the rack, a bath toy, faded on one side.";
            case TOILET->"The toilet. Its lid is down.";
            // Bit 1: the padlock open.
            case FREEZER->(stage&2)==0?"A chest freezer, humming, padlocked. On its lid a folded towel and a hairbrush with long hair in it."
                :"The freezer's padlock hangs open.";
            case STOOL->"A step stool by the freezer, worn where someone sits.";
            case POWER->"A note taped over the power point's switch: DO NOT SWITCH OFF.";
            case CORD->"The freezer's cord, taped to the floor every hand's width.";
            case TROUGH->"The laundry trough. A scrubbing brush on its edge.";
            case BUCKET->"A bucket.";
            case CHLORINE->"A box of pool chlorine, far too big for one pool.";
            case WASHER->"The washing machine. Its lid is up, airing.";
            case CAMP_BED->"A camp bed, a sleeping bag on it. The concrete under it has an oil stain.";
            // 0 on; 1 off.
            case BULB->"A bare bulb on a pull cord.";
            case BOX_WINTER->"A box, taped shut: NESSA, WINTER.";
            case BOX_BOOKS->"A box, taped shut: NESSA, BOOKS.";
            case BOX_DONT->"A box, taped shut: NESSA, DON'T. The last word has been gone over twice.";
            // Bit 1 (from the second visit): a second knife's outline empty.
            case PEGBOARD->(stage&2)==0?"Every tool's outline painted on the pegboard. Some are empty. One of them is a knife's."
                :"Every tool's outline painted on the pegboard. Some are empty. Two of them are knives'.";
            case TOWEL->"A towel, pushed under the door from the other side.";
            case TAPES->"Tapes in plain cases, dates on the spines, in order. Two dates are written over. One case is empty.";
            // 1: the empty tins under it.
            case WORKBENCH->stage==0?"The workbench.":"Three empty paint tins under the bench.";
            case SHED_TV->"A portable television on a video deck. The deck's tracking dial is worn smooth.";
            // 0 on the bench, shut, the brush beside it; 1 (0.4.530) open on the grass beside June while she paints.
            case TIN->stage==0?"A tin of paint and a brush. On the lid, in marker: LINE.":"The tin of paint, open. The paint on its rim is wet.";
            // 2: the last page.
            case TRANSCRIPT->stage<2?"Pages taped to the wall: the tapes written out by hand, corrections down the margins."
                :"The last page. Down its side, one word over and over: Nessa.";
            // 0 the first visit; 1 the second; 2 the last.
            case SHED_CALENDAR->stage==0?"A chemist's calendar. Nights crossed off in pencil, toward a night circled twice."
                :stage==1?"A chemist's calendar. The crossed-off nights are nearer the circle.":"A chemist's calendar. The circled night is tonight.";
            // 0 the old stakes; 1 the newest.
            case STAKE->stage==0?"A stake with a dead seedling. The tag says NESSA, in a child's printing.":"The newest stake, a fresh seedling. The tag says TILLY, in careful capitals.";
            case KENNEL->"A kennel. BISCUIT over the door, and a heart in crayon. A chain clipped to the wall, and to nothing.";
            case BOWL->"A steel bowl full of leaves.";
            case POOL_CHAIR->"A pool chair turned to face the water. The towel on its back is faded on one side only.";
            case PUMP->"The pool pump. It runs all the time.";
            // 0.4.530: June's, set down on the grass behind the shed while she paints.
            case LANTERN->"A battery lantern, set down on the grass, turned low.";
        };
    }
    /**
     * 0.4.523: what a hand on the line finds, by what it is painted on and how it is now. Each says what is there; none says what the
     * line is for, and none names a colour.
     */
    public static String line(PellLine.Surface surface,boolean thin,int stage){
        String where=thin?"Behind the shed the line is at its thinnest, barely a finger wide. The grass shows through it."
            :switch(surface){
                case CONCRETE->"A line of paint across the concrete, a hand wide. It has soaked in.";
                case TYRE->"The line goes across the drive. Tyres have run over it, and their marks are painted over.";
                case PAVER->"Paint on the pavers, a hand wide. It sits up in beads where it would not take, and has run into the joins.";
                case LAWN->"A line of paint through the grass, a hand wide, sitting on the blades. Under it the grass is dead.";
                case MULCH->"Paint pooled in the mulch of the bed. It has been laid twice here; the first coat sank in.";
                case DECK->"Paint across the deck boards, a hand wide, lifting and flaking along the grain.";
            };
        return switch(stage){
            case PellLine.SECOND->where+" It has been gone over again, not long ago.";
            case PellLine.LAST->where+" It is wet in places. It comes away on a finger.";
            case PellLine.FOOTPRINT->where+" It is wet, and a shoe has gone through it and dragged it.";
            case PellLine.HALF_SCRUBBED->"The paint here has been scrubbed half away.";
            case PellLine.GAP->surface==PellLine.Surface.LAWN?"The line is scrubbed out here. The dead grass shows where it ran.":"The line is scrubbed out here. The ground shows where it ran.";
            default->where;
        };
    }
    /** 0.4.523: the mat at the step, lifted and put back. */
    public static final String MAT_LIFTED="Under the mat the line runs straight on, unbroken.",MAT_DOWN="The mat goes back over it.";
    /** What the talking clock on Nessa's drawers says when it is pressed. */
    public static String clock(long dayTime){
        int minutes=(int)(((dayTime%24000)+24000)%24000*60/1000),hour=(minutes/60+6)%24,minute=minutes%60/5*5;
        String[] words={"twelve","one","two","three","four","five","six","seven","eight","nine","ten","eleven"};
        String h=words[hour%12];
        String m=minute==0?"o'clock":minute==5?"oh five":switch(minute){case 10->"ten";case 15->"fifteen";case 20->"twenty";case 25->"twenty-five";case 30->"thirty";
            case 35->"thirty-five";case 40->"forty";case 45->"forty-five";case 50->"fifty";default->"fifty-five";};
        return "The clock on top, pressed: \"The time is "+h+" "+m+(hour<12?" a.m.":" p.m.")+"\"";
    }
    private static List<Component> pages(String... texts){var out=new ArrayList<Component>();for(String text:texts)for(String p:LookoutTexts.pages(text,150))out.add(HouseWriting.page(HouseWriting.WritingStyle.PLAIN,p));return out;}
    /**
     * 0.4.524, the people (PellPeople): what a hand on each of them gets, in turn, on the first visit. June is kind and specific, and
     * slightly wrong; Gail has always just spoken to June; Tilly asks what she cannot see. None of it names the reader as anything.
     */
    public static final String[] JUNE_TALK={
        "There you both are. Come in out of the heat.",
        "Inside the line after dark, love. It's for the dog.",
        "The pool gate stays locked. That's the one thing in this house I'll ask twice.",
        "You're in the garage. It's all done up. For your privacy.",
        "Tilly's in Nessa's room now. I've kept it nice.",
        "Callum doesn't say much. He'll come round to you."};
    public static final String[] GAIL_TALK={
        "I've just been talking to June. She's got everything ready for you.",
        "You're in good hands. June's been doing this a long time.",
        "I'll ring in the morning and see how the first night went.",
        "I'm sorry it's all so fast. It won't always be.",
        "I have to get going. I've another one to see before four."};
    public static final String[] TILLY_TALK={
        "Is it nice? Tell me what it looks like.",
        "It smells like a swimming pool. Is there a pool?",
        "Someone's in the hall. I can hear them breathing.",
        "I'm keeping my hat on.",
        "Don't go far, okay?"};
    /** Callum does not speak. A hand on him gets what he does instead. */
    public static final String[] CALLUM_HAND={
        "He doesn't answer. He looks at your hand, then at you.",
        "He steps back, as far as you stepped forward.",
        "His hair drips on the carpet. Nobody has been swimming.",
        "He holds out his hand the way you held out yours, a moment late."};
    /**
     * 0.4.525, the first visit's day (PellVisit, docs/PELL_HOUSE_VISIT_0_4_525.md): what Gail and June say at the step as the
     * siblings arrive, and what June says at each stop as she shows them round. Nothing here says what the reader is.
     */
    public static final String GAIL_GREETS="There you are. Come and meet June.",
        JUNE_GREETS="Hello, love. Gail's told me all about you.",
        GAIL_READY="She's got everything ready for you. I'll ring in the morning and see how the first night went.",
        GAIL_GOES="I have to get going. I've another one to see before four.",
        JUNE_LINE="Come in out of the heat. And inside the line after dark, love. It's for the dog.",
        JUNE_CALLUM="That's Callum. He doesn't say much. He'll come round to you.",
        JUNE_NESSA="This was Nessa's room. It's Tilly's now.",
        JUNE_POOL="The pool gate stays locked. That's the one thing in this house I'll ask twice.",
        JUNE_GARAGE="You're in the garage. It's all done up. For your privacy.",
        JUNE_DINNER="Dinner's at six. I'll give you a call.";
    /**
     * 0.4.526, the first evening (PellVisit, docs/PELL_HOUSE_DINNER_0_4_526.md): June's call, what she says as she serves and clears,
     * and what a hand on the table finds at each place. Callum, at the table, does not speak either.
     */
    public static final String JUNE_CALLS="Dinner!",
        CALLED_FAR="June, calling from the kitchen: \u201cDinner!\u201d",
        JUNE_SERVES_TILLY="There you go, love.",
        JUNE_SERVES="And one for you.",
        JUNE_ASKS="Is that all right, love? Tell me if it's too hot.",
        JUNE_CLEARS="Leave those. I'll do them.";
    public static final String TILLYS_PLATE="Tilly's dinner.",TILLYS_EATEN="Tilly's plate. She has eaten it all.",
        CALLUMS_PLATE="His plate. Nothing on it.",NOBODY_PLACE="A place laid. Nobody sits there.",PLATE_EMPTY="A plate. Nothing on it yet.",
        SCRAPED_PLATE="Scraped clean.",ATE="Sausages, mash and peas, the gravy thick on the mash. You eat it all.";
    public static final String[] CALLUM_TABLE={
        "His hands stay under the table.",
        "He watches Tilly's plate.",
        "He doesn't look at his own."};
    /**
     * 0.4.527, bedtime (PellVisit, docs/PELL_HOUSE_BEDTIME_0_4_527.md): June in her chair with a birthday tape on; what the tape shows a
     * reader who puts a hand on the television while it plays, in order; bedtime, the brushing, goodnight; and sending the readers out to
     * the garage so she can lock up. Asleep, Tilly and Callum are only looked at.
     */
    public static final String JUNE_MY_CHAIR="That's my chair, love.";
    public static final String[] JUNE_WATCHING={
        "That's my Nessa.",
        "Every birthday, a crown. She wouldn't have a cake without one."};
    public static final String[] TV_TAPE={
        "A birthday on the tape, in this kitchen. Somebody carries a cake in with its candles lit.",
        "The camera finds a girl in a paper crown. Whoever is holding it laughs and says her name: Nessa.",
        "She blows the candles out and looks straight into the lens.",
        "The tape jumps. The same kitchen, another year, the same crown."};
    public static final String JUNE_BEDTIME="Bedtime, Tilly.",
        JUNE_HOLD_STILL="Hold still for me, love.",
        TILLY_DAD="Dad did it in one go. It always came out crooked.",
        JUNE_EVERY_NIGHT="Well. I'll do it every night now.",
        JUNE_GROW_LONG="There. It'll grow long here.",
        JUNE_NIGHT_NIGHT="Night night, love.",
        JUNE_SENDS_OUT="Bed, love. Out to the garage, and I'll lock up behind you.",
        SENT_FAR="June, calling through the house: “Bed, love. Out to the garage.”",
        JUNE_LOCKING="Night, love. I'm locking up.";
    public static final String[] JUNE_NUDGE={
        "Go on, love. It's late.",
        "Garage, love. I want to lock up."};
    public static final String TILLY_ASLEEP="Tilly is asleep, her hair brushed out across the pillow.",
        CALLUM_ASLEEP="Callum is asleep in the middle of the circle, on his back, very still.";
    /**
     * 0.4.530, the first night (PellNight, docs/PELL_HOUSE_NIGHT_0_4_530.md): what June says out at the line, to a reader who comes near,
     * steps in the paint, stands where she is painting, or follows her in; what a hand on her has while she is out, in turn; what a hand
     * on Callum finds while he stands on the line. None of it says what the line is for beyond what June says, and none names the reader.
     */
    public static final String JUNE_NOTICES="Can't sleep, love? It's only me.",
        JUNE_MIND_PAINT="Oh, love. Mind the paint.",
        JUNE_FEET="Feet, love. I'm doing that bit.",
        JUNE_OUT_YOU_COME="Out you come, love. Not tonight.",
        JUNE_OUT_OF_SHED="Out of there, love. I'm locking up.",
        JUNE_BACK_OUT="Back out you go, love. Bed.",
        SHED_OPEN="The padlock hangs open on its hasp.",
        BACK_LOCKED_OUT="The back door is locked. June has the key with her.";
    public static final String[] JUNE_NIGHT={
        "You should be asleep, love. It's late.",
        "Just going over it. It wears, out here.",
        "Mind the paint. It's still wet.",
        "Biscuit needs it bright. He's used to it.",
        "Go on, back to bed. I won't be long."};
    public static final String[] CALLUM_NIGHT={
        "Callum is standing on the line, barefoot, facing the house. His hair is wet.",
        "He doesn't move for your hand.",
        "The grass round his feet is dry."};
    /** A hand on an outside door June has locked, from outside it. */
    public static String locked(BlockPos door){
        if(door.equals(PellSite.FRONT_DOOR))return "The screen door is locked. The lock is on the inside.";
        if(door.equals(PellSite.SLIDER))return "The sliding door is locked from inside.";
        return "The back door is locked from inside.";
    }
    /** June's four rules, as bits of a reader's record ("Rules"): the line, the pool gate, Nessa's room, the garage. */
    public static final int RULE_LINE=1,RULE_POOL=2,RULE_NESSA=4,RULE_GARAGE=8,RULES=15;
    /** The rule a line of June's gives, from the tour or from a hand on her, or none. */
    public static int rule(String line){
        if(line.equals(JUNE_LINE)||line.equals(JUNE_TALK[1]))return RULE_LINE;
        if(line.equals(JUNE_POOL)||line.equals(JUNE_TALK[2]))return RULE_POOL;
        if(line.equals(JUNE_NESSA)||line.equals(JUNE_TALK[4]))return RULE_NESSA;
        if(line.equals(JUNE_GARAGE)||line.equals(JUNE_TALK[3]))return RULE_GARAGE;
        return 0;
    }

    /** The first paper: the placement notice, as it was put through the letterbox. */
    public static ItemStack source(){
        return HouseWriting.book("Notice of placement","G. Mercer",pages(
            "NOTICE OF PLACEMENT\n\nTwo children, the elder and a younger half-sister, to the care of Ms June Pell, 9 Wren Close, from today.\n\nMs Pell is an approved carer of long standing. She has one child in placement already.",
            "The younger child is partly sighted and uses a cane. Ms Pell has experience of this. Her own daughter, who died, had the same condition, and the house is still set up for it.\n\nThe elder child will have the converted garage, for privacy.",
            "I will telephone every morning for the first week.\n\nPlease be patient with one another. It has been a hard month for everyone.\n\nGail Mercer\nCaseworker"));
    }
    /**
     * The account. This slice the house cannot be finished, so nothing asks for this; it says only what a reader could have seen
     * of the street, and later slices write the rest from the reader's own record.
     */
    public static ItemStack account(String name,CompoundTag own){
        var p=new ArrayList<String>();
        p.add("There was a brick house at the head of a street where every lawn but one was dead. Hers was green.");
        if(own.getBoolean("WentIn"))p.add("I went in by a screen door that had been torn low down and taped.");
        return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.PELL_HOUSE),name,pages(String.join("\n\n",p)));
    }
}
