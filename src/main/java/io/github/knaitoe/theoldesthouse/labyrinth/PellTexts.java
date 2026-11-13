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
            // 0.4.542, the house's aftermath (PellAftermath).
            case MARKS_AFTER->stage==0?MARKS_EMPTY:MARKS_KEPT;
            case FOR_SALE->SIGN;
            // 0.4.548: the last school photograph, after June's night with it (PellMark).
            case PHOTO_BROKEN->"The last school photograph hangs crooked. The glass is starred across her face.";
            // 0 the border; 1 where it was steamed off and put back.
            case BORDER->stage==0?"A wallpaper border of small animals along the top of the wall.":"Behind the bed the border has been steamed off and put back, not quite straight.";
            case MATTRESS->"A mattress on the floor. No bed.";
            case CIRCLE->"A circle painted on the floor round the mattress. The toys stand on its line, facing out.";
            case SHOES->"Shoes lined up at the door, laces tucked in.";
            // 0 no knives; 1 a knife among them; 2 (0.4.534, after the knife night) the space where it lay.
            case CUTLERY->stage==0?"A drawer of the house's missing cutlery: spoons and forks in rows. No knives."
                :stage==1?"A drawer of the house's missing cutlery: spoons and forks in rows, and a knife."
                :"A drawer of the house's missing cutlery: spoons and forks in rows, and a space between them where something longer lay.";
            case VANITY->"Two toothbrushes in the cup. One is dry and old, and kept.";
            // 0 clear; 1 steamed.
            case MIRROR->stage==0?"The mirror over the basin.":"The steam on the mirror shows a word written with a finger: NESSA.";
            case BATH->"A non-slip mat in the bath. On the rack, a bath toy, faded on one side.";
            case TOILET->"The toilet. Its lid is down.";
            // Bit 1: the padlock open.
            case FREEZER->(stage&2)==0?"A chest freezer, humming, padlocked. On its lid a folded towel and a hairbrush with long hair in it."
                :"The freezer's padlock hangs open.";
            // 0.4.540: its lid up (PellFreezer).
            case FREEZER_OPEN->"The freezer stands open, its lid up against the wall. Cold comes up out of it.";
            case STOOL->"A step stool by the freezer, worn where someone sits.";
            // 0.4.548: 1, the note taken off it (PellMark).
            case POWER->stage==0?"A note taped over the power point's switch: DO NOT SWITCH OFF.":"The power point's switch is taped down. Where the note was, the tape has left its corners.";
            case CORD->"The freezer's cord, taped to the floor every hand's width.";
            // 0.4.538: the brush is across the bucket, as the art has it; the bucket is taken to scrub the line (PellScrub).
            case TROUGH->"The laundry trough. Its tap drips.";
            case BUCKET->"A bucket of water, a scrubbing brush across it, its bristles worn to one side.";
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
            // 0 under the garage's door; 1 (0.4.534) on the kitchen floor, after the knife.
            case TOWEL->stage==0?"A towel, pushed under the door from the other side.":"A tea towel on the kitchen floor, folded over on itself.";
            case TAPES->"Tapes in plain cases, dates on the spines, in order. Two dates are written over. One case is empty.";
            // 1: the empty tins under it.
            case WORKBENCH->stage==0?"The workbench.":"Three empty paint tins under the bench.";
            case SHED_TV->"A portable television on a video deck. The deck's tracking dial is worn smooth.";
            // 0 on the bench, shut, the brush beside it; 1 (0.4.530) open on the grass beside June while she paints.
            case TIN->stage==0?"A tin of paint and a brush. On the lid, in marker: LINE.":"The tin of paint, open. The paint on its rim is wet.";
            // 2: the last page.
            case TRANSCRIPT->stage<2?"Pages taped to the wall: the tapes written out by hand, corrections down the margins."
                :stage==2?"The last page. Down its side, one word over and over: Nessa."
                // 0.4.548: 3, the last page taken (PellMark).
                :"Where the last page was, a strip of tape and a torn edge of paper under it.";
            // 0 the first visit; 1 the second; 2 the last.
            case SHED_CALENDAR->stage==0?"A chemist's calendar. Nights crossed off in pencil, toward a night circled twice."
                :stage==1?"A chemist's calendar. The crossed-off nights are nearer the circle.":"A chemist's calendar. The circled night is tonight.";
            // 0 the old stakes; 1 the newest.
            case STAKE->stage==0?"A stake with a dead seedling. The tag says NESSA, in a child's printing.":"The newest stake, a fresh seedling. The tag says TILLY, in careful capitals.";
            case KENNEL->"A kennel. BISCUIT over the door, and a heart in crayon. A chain clipped to the wall, and to nothing.";
            case BOWL->"A steel bowl full of leaves.";
            // 0.4.533: stage 1, June's robe dropped over it while she swims.
            case POOL_CHAIR->stage==1?"A towelling robe has been dropped over the pool chair. It is still warm."
                :"A pool chair turned to face the water. The towel on its back is faded on one side only.";
            case PUMP->"The pool pump. It runs all the time.";
            // 0.4.530: June's, set down on the grass behind the shed while she paints.
            case LANTERN->"A battery lantern, set down on the grass, turned low.";
            // 0.4.532: the kitchen's telephone. Stage 1: its receiver off the hook.
            case PHONE->stage==1?"The receiver is off the hook.":"A telephone on the wall, its cord stretched out of its curl. On a strip of tape beside it, in pencil: GAIL, and a number.";
        };
    }
    /**
     * 0.4.523: what a hand on the line finds, by what it is painted on and how it is now. Each says what is there; none says what the
     * line is for, and none names a colour.
     */
    public static String line(PellLine.Surface surface,boolean thin,int stage){return line(surface,thin,stage,false);}
    /** 0.4.531: as above, after first light, when the paint put down in the night has dried. */
    public static String line(PellLine.Surface surface,boolean thin,int stage,boolean dry){
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
            case PellLine.FOOTPRINT->where+(dry?" A shoe went through it while it was wet and dragged it. It has dried like that.":" It is wet, and a shoe has gone through it and dragged it.");
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
    /**
     * 0.4.531, the first morning (PellMorning, docs/PELL_HOUSE_MORNING_0_4_531.md): what wakes a reader asleep in the garage at eleven;
     * June's call to breakfast and what is heard of it from further off; her calling Callum in; what Tilly says she heard in the night
     * and what June says to it; what a hand on the table finds at breakfast; and what a hand on June gets in the morning, in turn. None of
     * it says what was singing.
     */
    public static final String WOKEN_BY_LOCK="Across the yard, a key turns in a lock.",
        JUNE_BREAKFAST="Breakfast!",
        BREAKFAST_FAR="June, calling from the kitchen: “Breakfast!”",
        JUNE_CALLUM_IN="Callum. Breakfast.",
        TILLY_SINGING="Somebody was singing last night.",
        JUNE_WAS_THERE="Was there, love?",
        TILLY_IN_THE_WALL="In the wall by my bed. The same bit, over and over.",
        JUNE_PIPES="That'll be the pipes, love. Eat your egg.";
    public static final String TILLYS_BREAKFAST="Tilly's breakfast. Her toast is cut into fingers, the crusts off.",
        ATE_BREAKFAST="Toast and a fried egg, the yolk still soft. You eat it all.";
    public static final String[] JUNE_MORNING={
        "Morning, love. Sleep all right?",
        "There's more toast if you want it.",
        "Callum's had his."};
    /**
     * 0.4.532, Gail's call (PellCall, docs/PELL_HOUSE_CALL_0_4_532.md). June answers it and has her say first; then each reader at the
     * house may take the receiver and answer Gail on the conversation panel; then June has it back, and her goodbye answers what was
     * said in front of her. Gail has always just spoken to June. None of it names the reader as anything.
     */
    public static final String JUNE_THATLL_BE_GAIL="That'll be Gail.",
        GAIL_FOR_JUNE="Hello? June? Oh, hello, love. It's Gail. Is June there? Put her on for me first, would you?",
        JUNE_TAKES="I'll take that, love.",
        JUNE_ON_PHONE="June has the receiver to her ear. She lifts a finger: a minute.",
        PHONE_BUSY="Somebody else has the receiver.",
        HOLDING="You have the receiver. Gail is waiting for June.",
        HAD_A_TURN="Gail has had her word with you.",
        JUNE_HOLDS_OUT="It's Gail, love. She wants a word.",
        JUNE_NEXT="Your turn, love. Gail wants a word.",
        PHONE_FAR="June, calling from the kitchen: “Phone, love! It's Gail.”",
        // June's side, to Gail.
        JUNE_HELLO="Hello? Oh, Gail. Morning, love.",
        JUNE_SORRY="Gail? Sorry, love. Morning.",
        JUNE_NO_TROUBLE="No, no. No trouble at all.",
        JUNE_SLEPT="Slept like logs, the pair of them.",
        JUNE_UP="Slept like logs. Well, one of them was up and about in the night. I saw them back to bed.",
        JUNE_TILLY_ATE="Tilly ate every bite. She's a love.",
        JUNE_CALLUM_GOOD="And Callum's been good as gold.",
        JUNE_PUT_ON="Hang on, I'll put them on.",
        // Gail, to a reader on the line, and what they can say to her.
        GAIL_HELLO="Hello, love. It's Gail.",
        GAIL_ASKS="I've just been talking to June. She says you've settled in. How was the first night?",
        TELL_FINE="It was fine.",TELL_NO_SLEEP="I didn't sleep much.",TELL_JUNE_OUT="June was out in the garden in the night.",
        TELL_CALLUM="Callum was standing outside in the dark.",TELL_LEAVE="I don't want to stay here.",TELL_NOTHING="Say nothing.",
        GAIL_BYE="I'll ring again tomorrow. Put June back on for me, would you?",
        HAND_BACK="Give the receiver back to June.",
        // June, back on.
        JUNE_BACK="Gail? It's me.",
        JUNE_NOBODY="Gail? They're a bit shy yet. I'll tell them you rang.",
        JUNE_ABOUT_LINE="Oh, that. I don't sleep, you know me. I was only seeing to the garden.",
        JUNE_ABOUT_CALLUM="He does wander. I keep an eye on him.",
        JUNE_EARLY_DAYS="It's early days, Gail. Early days.",
        JUNE_SHY="Shy. It'll come.",
        JUNE_BYE="Same time tomorrow. Bye now, love.",
        // To the room, once she has hung up.
        JUNE_INSIDE_LINE="Inside the line after dark, love. I did say.",
        JUNE_GIVE_IT_TIME="Give it time, love. That's all.";
    /**
     * 0.4.533, a week in (PellWeek, PellSwim, docs/PELL_HOUSE_WEEK_0_4_533.md): June's hello from the kitchen; the first slips, at dinner,
     * at bedtime and at breakfast, each time the name of the girl who had the room first and then Tilly's; the week's call; what a hand
     * on June and on Tilly gets a week in; June at the pool at first light; and what a hand finds in the pool. None of it says why.
     */
    public static final String JUNE_WELCOME="That you, love? Dinner's at six.",
        WELCOME_FAR="June, calling from the kitchen: “That you, love? Dinner's at six.”",
        JUNE_SERVES_NESSA="There you go, Nessa. Oh. Tilly. Sorry, love.",
        TILLY_SAME_WAY="You do it the same way every night.",
        JUNE_HOW_SHE_LIKED="That's how she liked it.",
        JUNE_COMING_ON="There. It's coming on. Nearly to your shoulders.",
        JUNE_NIGHT_NESSA="Night night, Nessa.",
        TILLY_ITS_TILLY="It's Tilly.",
        JUNE_NIGHT_TILLY="Tilly. I know, love. Night night.",
        JUNE_GOGGLES="Nessa, love, where did you put your goggles?",
        TILLY_NO_GOGGLES="I haven't got any goggles.",
        JUNE_ALL_OVER="Tilly. Sorry, love. I'm all over the place this morning.",
        TILLY_ALL_RIGHT="That's all right.",
        // The week's call: June's side, then Gail to a reader on the line, then Gail's goodbye.
        JUNE_WEEK_SETTLED="Settling in lovely, the pair of them.",
        JUNE_WEEK_UP="Settling in. One of them's still up and about at night, but I see them back to bed.",
        JUNE_WEEK_TILLY="Tilly's eating well. She's a different girl.",
        GAIL_WEEK_ASKS="I've just been talking to June. She says it's all going well. A week in, now. How are you finding it?",
        GAIL_WEEK_BYE="That's the first week done. I'll ring again next week. Put June back on for me, would you?",
        TELL_WEEK_FINE="It's fine.",TELL_WEEK_NO_SLEEP="I'm not sleeping much.",
        JUNE_WEEK_BYE="Next week, then. Bye now, love.",
        // June at the pool, and in the water.
        JUNE_OUT_OF_POOL="Out you come, love. I'm locking it.",
        JUNE_SWIMMING="June doesn't stop. She turns at the wall and pushes off again.",
        POOL_GATE_OPEN="The padlock hangs open on the hasp. The gate swings in.",
        // In the pool, a week in.
        DRAIN="Under the grate of the drain at the deep end, a child's swimming goggles, the strap knotted where it broke.",
        BAND_MISSING="One tile of the band along the waterline is missing. The grout round the gap is new.",
        BAND_ODD="One tile of the band does not match the rest. It is newer, and not quite the same.";
    /**
     * 0.4.546, Tilly's senses (PellSenses.hush): what June says low into the telephone, between her other lines to Gail, where she thinks
     * nobody can hear it. Only a reader as Tilly hears it, anywhere in the house; the older sibling never hears it said. Four to a call:
     * the first morning's, and a week in (the first of those knows whether Tilly has answered to the name).
     */
    public static final String[] JUNE_QUIETLY_FIRST={"Tilly's in Nessa's bed. It fits, Gail. It fits.","The older one watches me. All the time.",
        "No. Nobody goes near the pool. I've seen to that.","A week. Give me a week and you'll see."};
    public static final String JUNE_QUIETLY_ANSWERS="Tilly answers to it now. Doesn't even notice.",JUNE_QUIETLY_NOT_YET="Tilly still won't answer to it. That'll come.";
    public static final String[] JUNE_QUIETLY_WEEK={"The older one's been telling stories. You'll hear them. Don't listen.",
        "It's all ready for next week. Nothing's changed.","You'll see, after. You won't know the difference."};
    /** June's quiet lines for this visit's call, in order (PellSenses.hush), for a reader as Tilly with this record. */
    static List<String> juneQuietly(int visit,CompoundTag own){
        if(visit<2)return List.of(JUNE_QUIETLY_FIRST);
        var out=new ArrayList<String>();out.add(own.getInt("AnsweredNessa")>0?JUNE_QUIETLY_ANSWERS:JUNE_QUIETLY_NOT_YET);out.addAll(List.of(JUNE_QUIETLY_WEEK));
        return out;
    }
    /** 0.4.534, the knife: the kitchen in the night, a week in; June after it; her version on the week's call; Gail to a reader she
     *  found in the kitchen and to one she did not; and at breakfast. */
    public static final String
        KNIFE_WOKEN="Something wakes you. Over at the house, a light is on in the kitchen.",
        KNIFE_SEEN="Callum is at the sink with his back to the room. There is a knife in his hand.",
        KNIFE_HOLD="You take hold of his wrist.",
        KNIFE_WRENCH="He pulls against you. He is stronger than he looks.",
        JUNE_KNIFE_BED="Go on back to bed, love.",
        JUNE_KNIFE_HAND="Bed, love. We'll talk in the morning.",
        JUNE_KNIFE_OUT="Out you go, love. I'm locking up.",
        JUNE_KNIFE_TROUBLE="Well. There was, Gail. I won't lie to you.",
        JUNE_KNIFE_WHAT="Callum was up in the night. One of them went for him. In the kitchen, with a knife.",
        JUNE_KNIFE_HURT="No. Nobody's hurt. I got there.",
        JUNE_KNIFE_BLOCK="They'd had it out of the block.",
        TELL_KNIFE_TOOK="He had the knife. I was getting it off him.",
        TELL_KNIFE_DONT_KNOW="I don't know what happened.",
        TELL_KNIFE_LYING="She's lying.",
        TELL_KNIFE_NOT_ME="It wasn't me.",
        TELL_KNIFE_SAW="I saw him at the sink with it.",
        GAIL_KNIFE_BYE="I'll ring next week. Sooner, if I have to. Put June back on for me, would you?",
        JUNE_KNIFE_UPSET="Don't mind that, Gail. They're upset, that's all.",
        JUNE_KNIFE_EYE="I'll keep an eye. I always do.",
        JUNE_KNIVES="Knives stay in the block in this house, love.";
    /** What Gail asks a reader June found in the kitchen, and one she did not. */
    public static final List<String> GAIL_KNIFE_ASKS=List.of("June's told me about last night, love. She says you went for Callum. With a knife.","I have to hear it from you. What happened?"),
        GAIL_KNIFE_OTHER=List.of("June's told me about last night. Callum, and a knife.","She says it was one of you. Was it you, love?");
    /** What a reader told Gail about the knife: kept with the week's answers ("ToldGailWeek"). */
    public static final String KNIFE_TOOK="knife_took",KNIFE_DONT_KNOW="knife_dont_know",KNIFE_LYING="knife_lying",KNIFE_NOT_ME="knife_not_me",KNIFE_SAW="knife_saw";
    /** What Gail says to an answer about the knife, before her goodbye. A reader June found in the kitchen goes on Gail's file, whatever
     *  they say: none of them can say what happened in the black. */
    public static List<String> gailKnife(String told,boolean found){
        return switch(told){
            case KNIFE_TOOK->List.of("June says it was the other way about, love. She says she came in and you had hold of it.","I'm putting it on your file. I'm sorry.");
            case KNIFE_DONT_KNOW->List.of("That's what frightens me, love.","I'm putting it on your file. I'm sorry.");
            case KNIFE_LYING->List.of("I won't have that, love. Not about June.","She's been doing this a long time. It's going on your file.");
            case KNIFE_NOT_ME->List.of("All right, love. She says it was one of you.","You keep well out of it.");
            case KNIFE_SAW->List.of("On his own? With a knife?","June says one of you had it out of the block. I'll put down what you saw, love.");
            case LEAVE->List.of("Oh, love. I know.","Not after last night. Not yet. Give it a bit longer. That's all I'm asking.");
            default->found?List.of("Nothing to say? All right.","It's going on your file, love."):List.of("Hello? Are you there, love?","That's all right. You keep out of it.");
        };
    }
    /** 0.4.535, June's hand: on her way to the laundry and in it; the slam, heard; her coming out and a hand on her after; her version on
     *  the week's call; and Gail to the reader June says did it and to the rest. */
    public static final String
        JUNE_TO_LAUNDRY="I'll just put a wash on, love. Won't be a minute.",
        JUNE_LAUNDRY_OUT="Out you come a minute, love. Let me get the wash on.",
        JUNE_LAUNDRY_KEEPS="It'll keep.",
        SLAM_HEARD="A door slams at the back of the house, hard. June cries out.",
        SLAM_FAR="Over at the house a door slams, hard, and somebody cries out.",
        JUNE_HAND_OUT="It's nothing. I caught it in the door.",
        HAND_WRAPPED="June's right hand is wrapped in a tea towel. She holds it against her.",
        JUNE_HAND_TROUBLE="Well. Not trouble, Gail. Not really.",
        JUNE_HAND_WHAT="One of them slammed the laundry door on my hand yesterday.",
        JUNE_HAND_AND="And yesterday one of them slammed the laundry door on my hand.",
        JUNE_HAND_BRUISED="No, it's only bruised. I'll live.",
        TELL_HAND_NOT_ME="I never touched the door.",
        TELL_HAND_AWAY="I wasn't even in the house.",
        TELL_HAND_HERSELF="She did it herself.",
        TELL_HAND_HEARD="I heard it. I didn't see it.",
        TELL_HAND_WASNT_ME="It wasn't me.",
        JUNE_HAND_UPSET="They'll say anything when they're like this.";
    /** What a hand on June gets about her hand, once each, the rest of the visit. */
    public static final String[] JUNE_HAND_TALK={
        "Don't fuss, love. It's only a knock.",
        "I've had worse.",
        "Mind yourself round that laundry door, love. It swings shut."};
    /** What Gail asks about June's hand: the reader June says did it, or the rest; after asking about the knife, or first. */
    public static List<String> gailHandAsks(boolean named,boolean after){
        if(named)return List.of(after?"And June's hand, love. She says you slammed the laundry door on it.":"June's told me about her hand, love. She says you slammed the laundry door on it.","Did you?");
        return List.of(after?"And June's hand. She says one of you slammed the laundry door on it.":"June's told me about her hand. She says one of you slammed the laundry door on it.","Did you see what happened, love?");
    }
    /** What a reader told Gail about June's hand: kept apart from the week's other answer ("ToldGailHand"). */
    public static final String HAND_NOT_ME="hand_not_me",HAND_AWAY="hand_away",HAND_HERSELF="hand_herself",HAND_HEARD="hand_heard",HAND_WASNT_ME="hand_wasnt_me";
    /** What Gail says to an answer about June's hand. The reader June names is on Gail's file for it whatever they say: nobody saw it. */
    public static List<String> gailHand(String told,boolean named){
        return switch(told){
            case HAND_NOT_ME->List.of("June says you were the nearest, love. Right by the laundry.","It's going on your file.");
            case HAND_AWAY->List.of("June says you were right there at the door.","I've only got her word and yours, love. It's going on your file.");
            case HAND_HERSELF->List.of("Slammed a door on her own hand? Love.","I won't hear that. It's going on your file.");
            case HAND_HEARD->List.of("Heard what, love? A door?","All right. I'll put down that you heard it.");
            case HAND_WASNT_ME->List.of("No, love. She doesn't say it was you.","You keep out of it.");
            case LEAVE->List.of("Oh, love. I know.","Give it a bit longer. That's all I'm asking.");
            default->named?List.of("Nothing to say? All right.","It's going on your file, love."):List.of("That's all right, love.","You keep out of it.");
        };
    }
    // ------------------------------------------------------------------------------------------------ 0.4.536, the tapes (PellTapes)
    /** The dates on the spines, in the order she made them; each picture has the same date stamped in its corner (tools/draw_pell_tapes.py). */
    public static final String[] TAPE_DATES={"2 March","9 March","16 March","23 March","30 March","6 April","13 April","20 April","27 April"};
    /** The two spines whose dates are written over, as the shelf's own line has it: the sixth and the eighth. */
    public static boolean writtenOver(int tape){return tape==5||tape==7;}
    /**
     * What the woman on each tape says, in the order she made them, to somebody off camera who has lost a child. In fragments: the
     * ring, the one kept inside it, the other child, the water. Nothing says what the boy is, or whether any of it works.
     */
    public static final String[][] TAPE_LINES={
        {"Is it on? The little light's on. Good.",
         "You asked me how. I'll show you once, and you watch it as often as you need to.",
         "Don't ring me about it after. And don't tell a soul, not even family."},
        {"First, the ring. Paint is best. Salt blows away, and chalk washes off.",
         "One ring, all the way round. No gap in it, not a door's width, not a finger's.",
         "What you call back can't go where the ring isn't. That's all a ring is."},
        {"Round the house, not round a room. You'll want to keep it small. Don't.",
         "It goes looking for the thin place, so go round every night with the brush.",
         "If it's scuffed in the morning, it was tested. Paint it again."},
        {"Now. It won't stay in an empty ring. It needs somebody to stay in.",
         "A child holds it best. One nobody will come asking after.",
         "Keep him inside the ring. He mustn't ever cross it, or it goes out with him."},
        {"He doesn't eat. I know how that sounds. Water, yes. Never food.",
         "Food brings him back into himself, and what he's holding goes.",
         "Lay him a place at every meal all the same. It has to think it's at a table."},
        {"Then you want the other one. A girl, as near to yours as you can find.",
         "Near in the way that matters. You'll know it when you see her sit down.",
         "Not one with people who'd come looking. One that's been passed about."},
        {"She has to answer to the name. Say it every day. Say it as if you forgot.",
         "Give her the room. The clothes, the brush. Let her sleep where yours slept.",
         "The day she turns her head to it without thinking, she's ready."},
        {"It's done in water. Where you lost yours, if you can bear it.",
         "Warm it. Leave the light on under it. She shouldn't be frightened.",
         "Say the name the whole time. Keep her under till she answers to it.",
         "Then it's poured into her, and you'll have yours back."},
        {"The boy you can let go of, once it's poured. He won't remember the ring.",
         "That's all of it. I'm tired now.",
         "Turn it off. Then wind it back, and watch it again from the start."}};
    /** Over her lines, as a caption from the television; and the heading her lines are kept under in the reader's journal. */
    public static final String TAPE_VOICE="On the tape";
    public static String tapeHeading(int tape){return "A tape, "+TAPE_DATES[tape];}
    /** The shelf, on the conversation panel: the spines, in order, the deck's tape's case empty. */
    public static final String TAPES_WHO="The tapes",TAPES_LOOK="Tapes in plain cases, a date on each spine in the same hand.",TAPES_LEAVE="Leave them";
    public static String spine(int tape,boolean inDeck){return TAPE_DATES[tape]+(writtenOver(tape)?", written over":"")+(inDeck?". The case is empty":"");}
    /** A hand on the television: the deck plays the tape that is in it, or the picture goes. */
    public static final String TAPE_PLAY="The deck clunks and the screen comes up.",TAPE_STOP="The picture goes.",
        /** From the shelf: the tape in the deck back in its case first. */
        TAPE_SWAP="The tape in the deck goes back in its case. The deck takes the other.",
        /** On her rounds, come in for the tin while a tape plays. */
        TAPE_JUNE_OFF="June turns the deck off.";

    // ------------------------------------------------------------------------------------------------ 0.4.538, the last night (PellLastNight, PellScrub)
    /** June from her chair to the first reader in, on the last night; heard further off. */
    public static final String JUNE_LAST_WELCOME="That you, love? Early night tonight, all of us.",
        LAST_WELCOME_FAR="June, calling from the front room: “That you, love? Early night tonight, all of us.”";
    /** The bucket and the fridge. */
    public static final String BUCKET_TAKEN="You take the bucket and its brush.",
        HANDS_FULL="There is no room to carry it.",
        FRIDGE_PLATE="In the fridge, a plate of dinner under cling film. You take it.",
        FRIDGE_EMPTY="The fridge hums. There is nothing else in it to take.";
    /** Callum on the line on the last night: stepping onto the cell being scrubbed; held out food on the visits before; held out food
     *  when he has already eaten; taking it; and his name, the only thing he says. */
    public static final String CALLUM_STANDS="Callum steps onto the line where the brush is, and stands there.",
        CALLUM_WONT_TAKE="Callum looks at it. He does not take it.",
        CALLUM_HAS_EATEN="He already has what he was given.",
        CALLUM_TAKES="Callum takes it. He sits down on the grass inside the line, and eats.",
        CALLUM_NAME="My name's Callum.";
    /** A hand on Callum on the last night: on the line, and eating. */
    public static final String[] CALLUM_ON_LINE={
        "Callum does not move. His bare feet are on the paint.",
        "He is looking at the house. He does not look at the hand on his arm.",
        "He stays where he is, facing the house.",
    },CALLUM_EATING={
        "Callum eats, his head down over it.",
        "He does not look up from it.",
        "He eats all of it, slowly.",
    };
    // ------------------------------------------------------------------------------------------------ 0.4.539, midnight in the pool (PellRite)
    /** June at Nessa's bed at midnight, Tilly woken, and June taking her hand. */
    public static final String JUNE_RITE_WAKE="Nessa. Up you get, love. Come with Mum.",
        TILLY_RITE_NAME="I'm Tilly.",
        JUNE_RITE_HAND="Course you are. Give me your hand.",
        // Held out long enough, Tilly answers to it; and June.
        TILLY_ANSWERS="Yes, Mum.",
        JUNE_ANSWERED="There's my girl. There she is.",
        // June wading to her where she was let go in the water, and taking her back.
        JUNE_TAKES_BACK="There you are. Come here.",
        // A hand on Tilly: June has her; out of reach in the water; taking her hand, and taking it again; holding it already; somebody
        // else's; and what she says when a hand first finds hers.
        TILLY_WITH_JUNE="June has her by the hand, and does not let go.",
        TILLY_OUT_OF_REACH="She is out in the water, out of reach.",
        TILLY_TAKES="You take Tilly's hand under the water. She holds on.",
        TILLY_TAKES_LAND="You take Tilly's hand. She holds on.",
        TILLY_TAKES_AGAIN="You take her hand again. She holds on.",
        TILLY_HAS_YOUR_HAND="She has hold of your hand.",
        TILLY_HAS_A_HAND="She is holding somebody else's hand.",
        TILLY_WHO="Is that you?",
        // Led: the paint under her foot; the hand gone too far off; out through the gap.
        TILLY_AT_LINE="Tilly stops short. Her bare foot has found the paint, and she will not step on it.",
        TILLY_LET_GO="Her hand slips out of yours. She stands where she is.",
        TILLY_OUT="Tilly steps through the gap with you. She is outside the line.",
        // Told across the yard once she is out.
        JUNE_STOPPED="In the pool, June has stopped calling.",
        // A hand on June: in the water holding her; calling after her; stopped; by the bed after.
        JUNE_RITE_HOLDING="June does not look round. Her hands are on Tilly's shoulders.",
        JUNE_RITE_CALLING="June does not look round. She goes on calling the name.",
        JUNE_RITE_STOPPED="June stands in the water with her arms down. She does not look at you.",
        JUNE_KEPT="Shh. She's sleeping.",
        // Woken on the camp bed.
        WOKEN_BY_POOL="Out the back, water splashes, and June is saying a name over and over.";
    /** To a hand on June on her way down with Tilly, or back. */
    public static final String[] JUNE_RITE_BUSY={"Not now, love.","Back to bed with you.","This isn't for you, love."},
        // In the water calling her, again and again; letting her up into the air; calling after her once a hand has taken hers.
        RITE_CALLS={"Nessa.","Nessa, love. It's Mum.","Say yes for Mum, Nessa.","You're all right, Nessa. Say it.","Nessa. Answer me, love.","That's my Nessa. Say yes."},
        JUNE_AFTER_UNDER={"Nessa?","There. Nessa?","Nessa? Say it, love."},
        JUNE_CALLS_AFTER={"Nessa.","Nessa, come back to Mum.","Nessa?","Where are you going, Nessa?"};

    // ------------------------------------------------------------------------------------------------ 0.4.540, the freezer
    public static final String
        LANYARD_FOUND="In the robe's pocket, a lanyard with three keys on it. You take it.",
        LANYARD_GONE="The robe's pocket is empty.",
        FREEZER_UNLOCKED="You try the keys on the lanyard. The third one turns, and the padlock comes open.",
        FREEZER_LID="You lift the lid. Cold comes up out of it.",
        FREEZER_OPENED="Under frosted plastic, a girl lies on her back in a party dress, a paper crown flat beside her head. The girl from every photograph in the house. She is still.",
        FREEZER_AGAIN="She is there under the plastic, as still as before.",
        NESSA_HAND="The plastic is cold. Under it, she is colder.",
        JUNES_HAND="You take June's hand. She lets you.",
        JUNE_HAS_YOUR_HAND="She has hold of your hand.",
        JUNE_HAS_A_HAND="She is holding somebody else's hand.",
        JUNE_OUT_OF_REACH="She is out in the water, out of reach.",
        JUNE_LET_GO="Her hand slips out of yours. She stands where she is.",
        JUNE_AT_FREEZER="Nessa?",
        TELL_NESSA="Nessa.",
        TELL_ITS_NESSA="June. It's Nessa.",
        TELL_JUNE_NOTHING="Say nothing.",
        JUNE_SITS="June looks down into the freezer for a long time. Then she sits on the stool beside it, her hand on the plastic, and does not call again.",
        JUNE_BY_HER="June does not look up.",
        JUNE_STOPPED_INSIDE="In the house, June has stopped calling.";
    /** June, led away from the water by a reader's hand, still saying the name now and then. */
    public static final String[] JUNE_LED_CALLS={"Nessa?","Nessa, love?","Where's my Nessa?"};

    // ------------------------------------------------------------------------------------------------ 0.4.541, first light (PellDawn)
    /** Woken on the camp bed at first light, once the night's end has come either way. */
    public static final String WOKEN_AT_DAWN="It is getting light. Out in the street, a car is coming.",
        // Gail at the foot of the path; to Tilly beside her; and Tilly's answer.
        GAIL_DAWN="Morning, love. I came as soon as it was light.",
        GAIL_TO_TILLY="Tilly? Is that you, sweetheart?",
        TILLY_TO_GAIL="It's me. I'm Tilly.",
        // To each reader the morning is for, after Tilly came out.
        DAWN_OUT="Gail's car is at the curb.",
        // After Tilly answered: Gail to the readers; what each is told; going to her, with Tilly at the window or not; the horn, for
        // whoever has not come to her; and the end of the street.
        GAIL_COME_ON="June rang in the night. She thinks it's best you come with me, love. Tilly's settled.",
        DAWN_ANSWERED="Gail's car is at the curb. Gail is waiting at the foot of the path.",
        IN_CAR="Gail opens the car door for you.",
        TILLY_WAVES="Gail opens the car door. Up at Nessa's window, Tilly is waving. June stands behind her.",
        CAR_HORN="Out in the street, Gail sounds the horn.",
        SET_DOWN="The car stops at the end of the street, and Gail lets you out.";
    /** A hand on them at first light: Callum on the step (he does not speak); Tilly with Gail; Tilly and June at Nessa's window; Gail,
     *  after Tilly came out and after she answered. */
    public static final String[] CALLUM_STEP_HAND={
        "Callum is sitting on the step with his shoes on.",
        "He moves along the step to make room.",
        "He is looking down the path at the car.",
    },TILLY_DAWN_TALK={
        "It's me. I'm Tilly.",
        "Is that Gail's car? It sounds like hers.",
        "I can't hear June any more.",
    },TILLY_KEPT_TALK={
        "I'm staying here with Mum.",
        "Mum says I can wave.",
        "Bye.",
    },JUNE_KEPT_TALK={
        "Go on, love. Gail's waiting.",
        "She's home now.",
        "Wave bye-bye, Nessa.",
    },GAIL_DAWN_TALK={
        "Morning, love.",
        "I've got her. Go on.",
        "Callum's got his shoes on. Look at that.",
    },GAIL_WAIT_TALK={
        "In you get, love.",
        "June knows best. She's been doing this a long time.",
        "Tilly's settled. Come on.",
    };

    // ------------------------------------------------------------------------------------------------ 0.4.544, the torn page (PellPages)
    /** At first light on the last visit, Gail holding a reader's torn false page out to them from her folder: what they see, what she
     *  says, and what she says putting it back. */
    public static final String PAGE_HELD_OUT="Gail takes a page out of her folder and holds it out to you. It is the page you tore out of your journal.",
        GAIL_PAGE="June gave me this, love. It's your writing, isn't it?",
        GAIL_PAGE_FILE="It'll have to go in your file.";

    // ------------------------------------------------------------------------------------------------ 0.4.542, after (PellAftermath)
    /** Coming back to the house in a later hall: after Tilly came out, and after she answered. */
    public static final String AFTER_EMPTY="The house is empty, and the pool has been drained. On the front lawn, a sign: FOR SALE.",
        AFTER_KEPT="The line has been painted again, all the way round, and it is still wet. Nobody is home.",
        // A hand on the sign; on the ground where the line ran, grass or not; on the height marks, after either.
        SIGN="FOR SALE, and a telephone number to ring.",
        LINE_GONE="The paint has been scrubbed off. Where it lay on the grass, the grass is dead.",
        LINE_GONE_HARD="The paint has been scrubbed off. You can still see where it went.",
        MARKS_EMPTY="Pencil marks up the door frame, a date by each. Beside the last one a letter has been rubbed out.",
        MARKS_KEPT="Pencil marks up the door frame, a date by each. Above the rest, a new one, dated later, with an N beside it.",
        // In the empty house: Callum's circle, painted into the floor; anything else left behind, built in or fixed to the walls.
        CIRCLE_EMPTY="A circle is painted on the floor. Somebody has tried to scrub it off.",
        LEFT="Left with the house, and wiped clean.",
        // The pool gate, its padlock gone.
        GATE_OPEN="The padlock is gone. The gate swings.";

    /** Tilly's own name back to the calls, fainter the longer she holds out; near the end, nothing. */
    public static String[] tillyHolds(long held,int out){
        if(held<out/3)return new String[]{"I'm Tilly.","My name's Tilly."};
        if(held<out*2/3)return new String[]{"Tilly.","I'm Tilly."};
        if(held<out*9L/10)return new String[]{"Tilly...","...Tilly."};
        return new String[0];
    }
    /** What June calls in the water, told to a reader on the lot too far off to hear it. */
    public static String fromPool(String line){return "June, from the pool: \u201C"+line+"\u201D";}
    // ------------------------------------------------------------------------------------------------ 0.4.537, the false entries (PellEntries)
    /** In the older sibling's journal, in their own hand, as if they wrote it: after the knife, after June's hand, and after a night in
     *  the garage a week in (and on the last visit, when it has a morning). */
    public static String falseEntry(String id){return switch(id){
        case "knife"->"I took the knife out of the block.";
        case "hand"->"I slammed the door on her hand. Nobody saw.";
        case "night2"->"I told Tilly nobody wants her here.";
        case "night3"->"I scuffed the line by the drive on purpose, to see what she'd do.";
        // 0.4.548: after the last photograph (PellMark).
        case "photo"->"I took the last photograph off the wall and broke it.";
        default->"";
    };}
    // ------------------------------------------------------------------------------------------------ 0.4.543, after a failure (PellEntries, PellAfterlife)
    /**
     * After Tilly answered, the entries go on: a twisted line about a real thing the reader did in a later story, in their own hand, at
     * most one a House day (docs/PELL_HOUSE_FAILURE_0_4_543.md). One line for each Witness story but the Pell house's own, keyed by the
     * story's id: the thing the reader did there (its Witness line), told as if they had done it worse. Never the reader's feelings,
     * colour, age or sex; nothing about the house itself.
     */
    public static String forged(String story){return switch(story){
        case "floorboards"->"I put the board back so I could hear it again.";
        case "hide_and_clap"->"I could see through the blindfold the whole game.";
        case "harrigan"->"His telephone rang after he was gone. I let it ring.";
        case "model_home"->"I broke the window myself. I said it was the tree.";
        case "drowned_town"->"I kept the key and left the essays in the water.";
        case "preserved_cave"->"I took something out of one of their pockets on the way past.";
        case "shallows"->"I let go of her wrists on two. Nobody saw.";
        case "phone_canoe"->"I dropped the telephone over the side on purpose.";
        case "goatman"->"I knew whose voice it was at the door. I kept it shut anyway.";
        case "ted_caver"->"I cut the line on the way back so nothing could follow it.";
        case "zampano_courtyard"->"I pushed the cat off the mat with my foot.";
        case "whale"->"I wrote the answer myself and posted it to me.";
        case "barn_well"->"I pulled the cover shut from underneath.";
        case "plain"->"Something was out past the dunes. I took its picture and walked away.";
        case "hospital"->"I turned the alarms off myself.";
        case "holloway_camp"->"I left my light on so it would follow the next one instead.";
        case "mother_of_strays"->"I gave it to her so I wouldn't have to carry it.";
        case "seance"->"I made the noises in the cupboard so they'd think she was angry.";
        case "wallpaper_nursery"->"I tore her words off the wall and kept them.";
        case "hotel"->"I signed for both places at the table.";
        case "hill_nursery"->"I kept knocking after it stopped knocking back.";
        case "miniatures"->"I put the little figure behind mine myself.";
        case "masque"->"I stopped the clock so nobody else could leave.";
        case "usher"->"I nailed the lid down myself.";
        case "winchester"->"I counted what I'd killed in the ledger, and added more.";
        case "child_room"->"I shut the last door myself and hid under the bed.";
        case "crimson_hall"->"I stopped the third voice before it finished.";
        case "bly_route"->"There was room for two in the cupboard. I held the door shut.";
        case "elk_lot"->"I wrote the ending in the clipping myself.";
        case "elk_fan"->"The tooth in my hand was one I pulled.";
        case "mapping_interior"->"At 2:49 I stood in the doorway so he couldn't get past.";
        case "holy_rabbit"->"I knew where the meat came from. I asked for more.";
        case "confession"->"I sealed the journal in the wall myself.";
        case "elk_carcasses"->"I lay under the hides and let them find the other one.";
        case "costume_night"->"I told her which of them to go for.";
        case "movie_night"->"I rowed past the ones in the water.";
        case "winter_lake"->"I dared her out onto the ice.";
        case "camp_blood"->"I ran past the last cabin and didn't knock.";
        case "devils_rock"->"It wasn't chasing me. I was chasing it.";
        case "wheel"->"Every room measured me against her. I cheated.";
        case "ghosts_set"->"Marjorie asked for a blanket. I didn't bring it.";
        case "end_world_cabin"->"I answered the visitors for everyone in the cabin.";
        case "family_copy"->"I told them I had lived there. I hadn't.";
        case "three_fingers"->"I made every wish knowing how it would go.";
        case "lake_house"->"I broke the one rule while the others were under.";
        case "borrasca"->"I signed for her things too.";
        case "painted_interior"->"I painted over the people in the corners.";
        case "second_waitress"->"I ate everything she brought me.";
        case "tunnel_periphery"->"I slowed down so she would catch whoever was behind me.";
        case "missing_sleep"->"The one in the hallway on the monitor was me.";
        case "layered_man"->"I broke the knife on purpose and left him down there.";
        case "motion_light"->"I pressed the buzzer.";
        case "backseat"->"I opened the back door for it.";
        case "silo"->"I made up the places she could have gone.";
        case "dare_in_the_dark"->"I was the one holding the handle in the hall.";
        case "feet_up"->"I pulled the ladder up with the others still on the floor.";
        case "whistlers"->"I was the one in the north tower, watching.";
        case "work_print"->"I made a copy of the tape before I stopped it.";
        case "polaroid_trail"->"I took every one of those photographs myself.";
        case "left_right"->"I got into the back of the wagon.";
        case "dead_air"->"I put the highchair in front of the screens.";
        case "closing_hall"->"I sent them through first to see if it was safe.";
        case "storeroom"->"I turned round in the storeroom. I saw it.";
        case "stillness"->"I moved, once, while it was breathing on me.";
        case "line"->"Someone was on the line behind me. I didn't wait.";
        case "own_light"->"I followed it. It was me who came back.";
        case "polybius"->"When it asked for me by name, I answered.";
        case "replacement"->"I brought the wrong one home. I knew.";
        case "people_lights"->"I told her who was upstairs. I was lying.";
        case "second_person"->"I hid their things so they'd think it was them.";
        case "place_for_you"->"I locked the doors so the one packing couldn't leave.";
        case "vacant_possession"->"I carried the last chair out with them still in it.";
        case "last_swimmer"->"I was the one who said to pull the cover over.";
        case "deer_road"->"I saw the doe. I didn't slow down.";
        case "state_route"->"One car had somebody in it. I walked past.";
        case "household"->"I wrote my own name at the end of their list.";
        case "growing_things"->"I told their father to go out to the back step.";
        case "tall_grass"->"It was me who called the boy into the grass.";
        case "marsh_supper"->"I told them her ride had come.";
        case "last_crossing"->"The one name more was a name I gave him.";
        case "pinehook"->"The one more scare was mine.";
        case "the_drawing"->"My slip wasn't blank. I swapped it.";
        case "the_house_on_the_corner"->"I let her go into the last room by herself.";
        case "the_black_city"->"I left the others at the bottom of the tunnel.";
        case "the_kept"->"I opened their doors so it would leave mine alone.";
        case "last_orders"->"I took everything off his table.";
        case "bike_night"->"I led them up into the graveyard on purpose.";
        default->"";
    };}
    /** The cane behind a door that will not open: when it starts; and, at the door, June calling and the answer she gets. */
    public static final String CANE_BEGINS="Behind one of the doors, a cane is tapping.",
        CANE_CALL="“Nessa?”",CANE_ANSWER="“"+TILLY_ANSWERS+"”";
    public static final String[] JUNE_WEEK_TALK={
        "There you are, love. Dinner's at six.",
        "I go in the pool first thing, every morning. Clears the head.",
        "The pool gate stays locked, mind. Same as ever.",
        "Inside the line after dark, love. You know that by now.",
        "Tilly's settling in lovely. She's sleeping right through."};
    public static final String[] TILLY_WEEK_TALK={
        "June brushes my hair every night now.",
        "Is the pool open? I can hear the water.",
        "She calls me love. Mostly.",
        "Callum's hair drips. I can hear it.",
        "I'm keeping my hat on."};
    /** What a hand on June gets at first light, on her way down to the pool or back from it in her robe. */
    public static final String[] JUNE_SWIM_TALK={
        "Morning, love. You're up early.",
        "I go in first thing. Every morning. Clears the head.",
        "Back to bed, love. It's not six yet."};
    /** What each reader can tell Gail, kept in their record ("ToldGail"); and walking off with the receiver without a word. */
    public static final String FINE="fine",NO_SLEEP="no_sleep",JUNE_OUT="june_out",CALLUM="callum",LEAVE="leave",NOTHING="nothing",WALKED_OFF="walked_off";
    /** What Gail says to an answer, before her goodbye: she has June's version already. */
    public static List<String> gail(String told,boolean juneToldUp){return gail(told,juneToldUp,1);}
    /** 0.4.533: as above, on the visit it is: a week in, she says so. */
    public static List<String> gail(String told,boolean juneToldUp,int visit){
        if(visit>=2)switch(told){
            case NO_SLEEP->{return juneToldUp?List.of("June said somebody's still up at night.","It can take a while. It does get easier, love.")
                :List.of("Still? That's hard, love.","It can take a while. It does get easier.");}
            case LEAVE->{return List.of("Oh, love. I know.","It's only been a week. Give it a bit longer. Give June a bit longer. That's all I'm asking.");}
            case FINE->{return List.of("Good. That's good, love.","June says you're a help to her. You're in good hands.");}
            default->{}
        }
        return switch(told){
            case FINE->List.of("Good. That's good, love.","June's been doing this a long time. You're in good hands.");
            case NO_SLEEP->juneToldUp?List.of("June said somebody was up and about.","First nights are hard. It gets easier, I promise it does.")
                :List.of("No, I don't suppose you did. First nights are hard.","It gets easier. I promise it does.");
            case JUNE_OUT->List.of("In the garden? In the dark?","She'll have been seeing to something, love. She's always up at all hours, June.");
            case CALLUM->List.of("Callum does that. June did say.","He won't hurt anybody. Let him be, love.");
            case LEAVE->List.of("Oh, love. I know.","Nowhere's going to be home for a while. Give it a week. Give June a week. That's all I'm asking.");
            default->List.of("Hello? Are you there, love?","That's all right. You don't have to say anything.");
        };
    }
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
        // 0.4.533: a week in, June says two of them again.
        if(line.equals(JUNE_LINE)||line.equals(JUNE_TALK[1])||line.equals(JUNE_WEEK_TALK[3]))return RULE_LINE;
        if(line.equals(JUNE_POOL)||line.equals(JUNE_TALK[2])||line.equals(JUNE_WEEK_TALK[2]))return RULE_POOL;
        if(line.equals(JUNE_NESSA)||line.equals(JUNE_TALK[4]))return RULE_NESSA;
        if(line.equals(JUNE_GARAGE)||line.equals(JUNE_TALK[3]))return RULE_GARAGE;
        return 0;
    }

    // ------------------------------------------------------------------------------------------------ 0.4.545, a reader as Tilly (PellTilly)
    /**
     * Tilly's own lines, to the reader playing her: theirs to say in their own words, or not (the proposal's "Co-op lines", as Tess's
     * are at the lake house). Nobody else is shown them.
     */
    public static String mightSay(String line){return "Tilly might say: “"+line+"”";}
    /** The cane by the front door, while it can still be taken: to every reader on the first visit, once June has opened the door. */
    public static final String CANE_HINT="Inside the front door a long cane hangs on a hook beside a small coat. Whoever takes the cane is Tilly.";
    /** The cane taken: to whoever took it, with who they are Tilly to; and to the others there. */
    public static String caneTaken(String others){return "You take the cane from the hook. To "+others+", you are Tilly now. Tilly's lines come to you in grey: say them in your own words, or don't.";}
    public static String caneTakenBy(String name){return name+" takes the cane from the hook. To you, they are Tilly now.";}
    /** To the reader playing Tilly, as the visit sends Tilly somewhere: her place at the table, the edge of Nessa's bed. */
    public static final String TILLY_R_PLACE="June has laid a place for you at the table.",
        TILLY_R_BED="June is going to Nessa's room, to brush your hair on the edge of the bed.",
        TILLY_R_SIT="You sit on the edge of Nessa's bed.",
        TILLY_R_TUCKED="June tucks you in.",
        TILLY_R_LIE="You lie down in Nessa's bed.",
        // Midnight, the last night: June at Nessa's bed with the reader somewhere else; and then by the bed.
        TILLY_R_FETCH="Somewhere in the house, June is saying a name over and over: Nessa. She is at Nessa's bed.",
        TILLY_R_FETCHED="You are standing by Nessa's bed. June is there, saying a name.",
        TILLY_R_LED="June has your hand.",
        // In the water: a sibling's hand; let go of; taken back; the paint under a bare foot; out through the gap; holding out alone
        // until it is light; and answered, back in Nessa's bed.
        TILLY_R_HAND="A hand finds yours under the water. June's hands come away from your shoulders.",
        TILLY_R_LET_GO="The hand lets go of yours.",
        TILLY_R_TAKEN_BACK="June's hands are on your shoulders again.",
        TILLY_R_PAINT="Your bare foot finds wet paint. You step back off it.",
        TILLY_R_OUT="You step through the gap. You are outside the line.",
        TILLY_R_HELD_OUT="It is getting light. June's hands come away from your shoulders, and she does not say the name again.",
        TILLY_R_KEPT="June puts you to bed in Nessa's bed, and sits on its edge beside you.",
        // At first light after answering: at Nessa's window; the car going; and the end of the street.
        TILLY_R_WINDOW="June stands you at Nessa's window. Gail's car is at the curb.",
        TILLY_R_CAR_GONE="Gail's car goes away down the street.",
        TILLY_R_SET_DOWN="Some time later you are at the end of the street, on your own.";
    /** The panel's answers to the name: the one June wants, Tilly's own, and nothing. */
    public static final String ANSWER_YES=TILLY_ANSWERS,ANSWER_OWN=TILLY_RITE_NAME,ANSWER_NOTHING="Say nothing.",
        // A week in, the name at bedtime and at breakfast: Tilly's own answer, and Nessa's.
        NIGHT_OWN=TILLY_ITS_TILLY,NIGHT_YES="Night night, Mum.",GOGGLES_OWN=TILLY_NO_GOGGLES,GOGGLES_YES="By the pool, Mum.",
        // June after an answer to the name: at bedtime, at breakfast, and the first call in the water after an answer before.
        JUNE_NIGHT_ANSWERED="Night night, love. Sleep tight.",JUNE_GOGGLES_ANSWERED="Course they are. We'll get them after.",
        JUNE_YOU_SAID="You've said it to me before, love. Say it again.",
        // Answered in the water, to a reader: June's words never say what the reader is.
        JUNE_ANSWERED_R="There's my Nessa. There you are.";
    /** What the readers with a reader as Tilly are told of her, never saying "she": as TILLY_TAKES and the rest. */
    public static final String TILLY_TAKES_R="You take Tilly's hand under the water. Tilly holds on.",
        TILLY_TAKES_LAND_R="You take Tilly's hand.",
        TILLY_TAKES_AGAIN_R="You take Tilly's hand again.",
        TILLY_HAS_YOUR_HAND_R="Tilly has hold of your hand.",
        TILLY_HAS_A_HAND_R="Tilly is holding somebody else's hand.",
        TILLY_OUT_OF_REACH_R="Tilly is out in the water, out of reach.",
        TILLY_WITH_JUNE_R="June has Tilly by the hand, and does not let go.",
        TILLY_LET_GO_R="Tilly's hand slips out of yours.",
        TILLY_OUT_R="Tilly steps through the gap with you. You are both outside the line.",
        // At first light, to the others, after a reader as Tilly held out alone in the water until it was light.
        DAWN_HELD_OUT="It is light. June has let go of Tilly in the pool, and stopped calling. Gail's car is at the curb.";
    /**
     * 0.4.545, the account of a reader who played Tilly: the house from Tilly's side, in the reader's words, from their own record. It
     * says nothing of what they look like, and nothing of how they felt. It ends in the street, as the older sibling's does.
     */
    public static ItemStack tillyAccount(String name,CompoundTag own){
        var p=new ArrayList<String>();
        p.add("There was a house at the end of a street with a pool behind it. I could smell the pool from the path.");
        p.add("I took my cane off the hook inside the front door. June gave me her daughter's room. Her daughter was called Nessa.");
        // 0.4.547: who else was at this house: the older one, in a house of their own pair's (SceneInstances).
        var others=others(own);
        if(!others.isEmpty())p.add("June gave "+CoopAccounts.names(others)+" the garage across the yard.");
        int r=own.getInt("Rules");
        if((r&RULE_LINE)!=0)p.add("June said I had to stay inside the line after dark. I couldn't see any line.");
        if(own.getInt("TillyBrushed")>0)p.add("At bedtime June brushed my hair on the edge of Nessa's bed, the same way every time, all the way down.");
        int yes=own.getInt("AnsweredNessa"),no=own.getInt("SaidOwnNameBefore");
        if(yes>0)p.add(yes==1?"Once, when June called me Nessa, I answered to it.":Stillness.words(yes)+" times, when June called me Nessa, I answered to it.");
        if(no>0)p.add("When she called me Nessa, I told her my name.");
        // 0.4.548: an older one Gail came for in the night (PellMark).
        var gone=taken(own);
        if(!gone.isEmpty())p.add("In the night a car came to the curb, and a door shut out in the street. "+CoopAccounts.names(gone)+(gone.size()==1?" was":" were")+" not there at midnight. Gail had come in the night and taken "+(gone.size()==1?"them":"them all")+" away.");
        p.add("At midnight on the last night June got me up out of Nessa's bed and took me down to the pool by the hand. The water was warm. She stood behind me with her hands on my shoulders and said Nessa, over and over. She put me under, and let me up, and said it again.");
        int own_=own.getInt("SaidOwnName");
        p.add(own_>0?"I said my own name back to her.":"I didn't say anything back.");
        String hand=own.getString("TillyHand"),with=own.getString("OutWith");
        if(!hand.isEmpty())p.add(hand+" took my hand under the water, and June let go of me.");
        if(own.getBoolean("CameOut"))p.add(with.isEmpty()?"I went across the grass to a gap in the paint, and through it.":"We went across the grass to a gap in the paint, and through it.");
        else if(own.getBoolean("HeldOut"))p.add("I kept my own name until it got light. Then June let go.");
        p.add("When it got light, Gail's car came to the curb. Gail asked if it was me, and I said it was.");
        if(own.getBoolean("SawCallumStep"))p.add("Callum was sitting on the front step with his shoes on.");
        p.add("I had the spare tip for my cane in my pocket. I kept it.");
        p.add(own.getBoolean("CameOut")&&!with.isEmpty()?"We went down the path and over the line into the street. Behind us nobody was calling any name at all."
            :"I went down the path and over the line into the street. Behind me nobody was calling any name at all.");
        return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.PELL_HOUSE),name,pages(String.join("\n\n",p)));
    }
    /**
     * 0.4.547: the readers who were older siblings with this reader at their own house (a house to each pair, SceneInstances), by name, in
     * the order they met (PellHouse.WITH). A reader who took the cane is Tilly, and is not one of them.
     */
    public static List<String> others(CompoundTag own){
        var out=new ArrayList<String>();var with=own.getList(PellHouse.WITH,net.minecraft.nbt.Tag.TAG_STRING);String tilly=own.getString(PellTilly.TILLY_NAME);
        for(int i=0;i<with.size();i++){var n=with.getString(i);if(!n.isEmpty()&&!n.equals(tilly)&&!out.contains(n))out.add(n);}
        return out;
    }
    private static List<String> with(List<String> names,String last){var out=new ArrayList<>(names);out.add(last);return out;}
    /** 0.4.548: whoever Gail took away in the night from this reader's house, by name (PellMark, "GailTook"). */
    public static List<String> taken(CompoundTag own){
        var out=new ArrayList<String>();var list=own.getList(PellMark.TOOK,net.minecraft.nbt.Tag.TAG_STRING);
        for(int i=0;i<list.size();i++){var n=list.getString(i);if(!n.isEmpty()&&!out.contains(n))out.add(n);}
        return out;
    }
    /**
     * 0.4.547, the close of the house's line in a reader's Witness book (CoopAccounts.PELL): whoever came through it with them, all of
     * their own house, by their part in it. The reader as Tilly: who was the older one, and had the garage. An older sibling: who took
     * the cane and was Tilly, and who had the garage with them. Empty if there is nobody to name.
     */
    public static String company(CompoundTag own,List<String> names){
        if(names.isEmpty())return "";
        // 0.4.548: whoever Gail came for in the night (PellMark).
        var gone=taken(own);String until=" until Gail came for them in the night";
        if(own.getBoolean(PellTilly.PLAYS)){
            var stayed=new ArrayList<>(names);stayed.removeAll(gone);var away=new ArrayList<>(names);away.retainAll(gone);
            var out=new ArrayList<String>();
            if(!stayed.isEmpty())out.add(CoopAccounts.names(stayed)+(stayed.size()==1?" was the older one, and had the garage across the yard.":" were the older ones, and had the garage across the yard."));
            if(!away.isEmpty())out.add(CoopAccounts.names(away)+(away.size()==1?" was the older one,":" were the older ones,")+until+".");
            return String.join(" ",out);
        }
        String tilly=own.getString(PellTilly.TILLY_NAME);var rest=new ArrayList<>(names);boolean was=!tilly.isEmpty()&&rest.remove(tilly);
        var out=new ArrayList<String>();
        if(was)out.add(tilly+" took the cane off the hook, and was Tilly.");
        var stayed=new ArrayList<>(rest);stayed.removeAll(gone);var away=new ArrayList<>(rest);away.retainAll(gone);
        if(!stayed.isEmpty())out.add(CoopAccounts.names(stayed)+" had the garage with me.");
        if(!away.isEmpty())out.add(CoopAccounts.names(away)+" had the garage with me,"+until+".");
        return String.join(" ",out);
    }
    /** 0.4.545: the house's Witness line for a reader: Tilly's own, for a reader who played her; without "she" for a reader whose Tilly
     *  was another reader; the older sibling's otherwise. */
    public static String witness(CompoundTag own,String text){
        if(own.getBoolean(PellTilly.PLAYS))return own.getBoolean("HeldOut")
            ?"I stayed three times in a house where a woman called me by her daughter's name. On the last night she held me in the pool and said it over and over. I kept my own until it was light."
            :"I stayed three times in a house where a woman called me by her daughter's name. On the last night she held me in the pool and said it over and over, and I kept my own, and came out through a gap in the line.";
        if(own.contains(PellTilly.TILLY_ID))return "I stayed three times in a house where a woman had ringed the lot with paint. On the last night, in the water, Tilly would not answer to any name but Tilly, and came out through a gap in the line, and at first light a car came to the curb.";
        return text;
    }

    // ------------------------------------------------------------------------------------------------ 0.4.548, Gail's third mark (PellMark)
    /** Glass breaking in the hall, heard inside the house and from outside it; June to the photograph, for whoever is near. */
    public static final String GLASS_HEARD="In the hall, glass breaks.",GLASS_FAR="Over at the house, glass breaks.",
        JUNE_PHOTO="Oh, look at you. Look what they've done to you.";
    /** June on the kitchen telephone to Gail, in the night, a pause after each line while Gail talks. */
    public static final String[] JUNE_RINGS_GAIL={"Gail? It's June. I'm sorry, love, I know what time it is.","It's one of them again. The same one.",
        "Nessa's photograph. Off the wall, and the glass smashed. Her last one.","No. I've tried, Gail. I can't have it. Not with Tilly in the house.",
        "Would you? Thank you, love. I'll wait up."};
    /** A car in the night: what wakes a reader asleep, and what Gail says, coming up the path, to whoever is near. */
    public static final String WOKEN_BY_CAR="Lights cross the wall. Out in the street, a car is pulling in.",
        GAIL_NIGHT_HELLO="Hello, love. June rang me.";
    /** A hand on Gail in the night: to the reader June says did it, and to anybody else (with that reader's name). */
    public static final String[] GAIL_NIGHT_MARKED={"June's told me about the photograph, love.","That's three things now. The knife, her hand, and this.",
        "I can't leave you here tonight. Unless there's something you want to show me."};
    public static String[] gailNightOther(String name){return new String[]{"It's late, love. I've come for "+name+".","Unless there's something you want to show me."};}
    /** What a reader can say to her: only what they have about them, or have seen. */
    public static final String TELL_PAGE="Read this. It's June's writing.",TELL_NOTE="This was taped over the freezer's switch.",
        TELL_TAPES="Come and see what's in the shed.",TELL_THINGS="I'll get my things.",TELL_SAY_NOTHING="Say nothing.";
    /** The page and the note in her hands; the tape. */
    public static final String GAIL_READS_PAGE="Gail reads the page, then turns it over and reads it again.",
        GAIL_HOLDS_NOTE="Gail holds the note up to the light from the house. DO NOT SWITCH OFF, in June's capitals.";
    public static final String[] GAIL_ON_PAGE={"This is June's writing.","Where did you find this, love?"},
        GAIL_ON_NOTE={"What's she got in there that can't go off?"};
    public static final String GAIL_SHOW_ME="Show me, then.",GAIL_TURN_IT_OFF="Turn it off. Turn it off, love.",
        GAIL_NOTHING_HERE="There's nothing here, love. Come on.",GAIL_LET_GO="Gail stops. She will not go any further without you.";
    /** Once a mark has come off, under three: she goes without anybody. */
    public static final String[] GAIL_STAYS={"All right.","I'm not taking anybody anywhere tonight.","I'll be back first thing, and I'll talk to June then."};
    /** Into the car: what she says, and what the reader sees. */
    public static final String GAIL_IN_YOU_GET="Come on, love. In you get.",
        NIGHT_IN_CAR="Gail opens the car door for you. Up at the house, a light is on in June's room.",
        GAIL_GONE="Out in the street, Gail's car pulls away.";
    /** The page off the shed's wall and the note off the freezer's switch, taken; and put back. */
    public static final String PAGE_TAKEN="You peel the last page off the wall. Down its side, one word over and over: Nessa.",
        NOTE_TAKEN="You peel the note off the switch. DO NOT SWITCH OFF.";
    /** Later hands on Gail in the night. */
    public static final String[] GAIL_NIGHT_TALK={"Go on, love. Go and get your things.","June knows best. She's been doing this a long time.","It's late. Come on."};

    /** The first paper: the placement notice, as it was put through the letterbox. */
    public static ItemStack source(){
        return HouseWriting.book("Notice of placement","G. Mercer",pages(
            "NOTICE OF PLACEMENT\n\nTwo children, the elder and a younger half-sister, to the care of Ms June Pell, 9 Wren Close, from today.\n\nMs Pell is an approved carer of long standing. She has one child in placement already.",
            "The younger child is partly sighted and uses a cane. Ms Pell has experience of this. Her own daughter, who died, had the same condition, and the house is still set up for it.\n\nThe elder child will have the converted garage, for privacy.",
            "I will telephone every morning for the first week.\n\nPlease be patient with one another. It has been a hard month for everyone.\n\nGail Mercer\nCaseworker"));
    }
    /**
     * 0.4.541, the account (docs/PELL_HOUSE_DAWN_0_4_541.md), ready only once Tilly has come out with her own name: the house as this
     * reader had it, from their own record, in their words. It ends on the street, beyond the line. Nothing in it says what the reader is
     * like or how they felt; what June and Gail said is theirs.
     */
    public static ItemStack account(String name,CompoundTag own){
        // 0.4.545: a reader who played Tilly has the house from Tilly's side.
        if(own.getBoolean(PellTilly.PLAYS))return tillyAccount(name,own);
        // 0.4.545: a reader whose Tilly was another reader is never told "she" of them.
        boolean coop=own.contains(PellTilly.TILLY_ID);
        var p=new ArrayList<String>();
        p.add("There was a brick house at the head of a street where every lawn but one was dead. Hers was green.");
        if(own.getBoolean("WentIn"))p.add("I went in by a screen door that had been torn low down and taped.");
        // 0.4.547: another older sibling at this reader's own house is named with them (a reader who took the cane is Tilly).
        var others=others(own);
        String took=others.isEmpty()?"June Pell took us in, Tilly and me.":"June Pell took us in: Tilly, "+CoopAccounts.names(with(others,"me"))+".";
        p.add(took+(coop?" Tilly went by the cane and by sound. June gave Tilly Nessa's room."
            :" Tilly went by her cane and by what she could hear. June gave her Nessa's room."));
        int r=own.getInt("Rules");var rules=new ArrayList<String>();
        if((r&RULE_LINE)!=0)rules.add("Inside the line after dark.");
        if((r&RULE_POOL)!=0)rules.add("The pool gate stays locked.");
        if((r&RULE_NESSA)!=0)rules.add("Nessa's room is Tilly's now.");
        if((r&RULE_GARAGE)!=0)rules.add(others.isEmpty()?"I was in the garage, for my privacy.":CoopAccounts.names(with(others,"I"))+" were in the garage, for our privacy.");
        if(!rules.isEmpty())p.add("June had rules. "+String.join(" ",rules));
        boolean dinner=own.getBoolean("HeardNessaAtDinner"),bed=own.getBoolean("HeardNessaAtBedtime");
        if(dinner&&bed)p.add("At the table, and again at bedtime, June called Tilly Nessa.");
        else if(dinner)p.add("At the table June called Tilly Nessa, and went on serving.");
        else if(bed)p.add("At bedtime June called Tilly Nessa.");
        if(own.getBoolean("SawHairBrushed"))p.add("I watched June brush out Tilly's hair at bedtime, slowly, all the way down.");
        int nights=own.getInt("GarageNights");
        if(nights>1)p.add("I slept in the garage, "+Stillness.words(nights)+" nights in all.");
        else if(own.getBoolean("SleptInGarage"))p.add("I slept in the garage.");
        if(own.getBoolean("SawPainting"))p.add("At night I saw June out on the grass with a tin and a brush, painting the line where it had worn thin.");
        else if(own.getBoolean("SawJuneOut"))p.add("At night I saw June out in the garden in the dark.");
        if(own.getBoolean("SawCallumOnLine"))p.add("Callum stood out on the line in the dark, facing the house, and did not move.");
        String first=switch(own.getString("ToldGail")){
            case FINE->"I told her it was fine.";
            case NO_SLEEP->"I told her I had not slept much.";
            case JUNE_OUT->"I told her June had been out in the garden in the night. She said June would have been seeing to something.";
            case CALLUM->"I told her Callum had been standing outside in the dark. She said he did that.";
            case LEAVE->"I told her I did not want to stay. She asked me to give it a week.";
            case NOTHING->"I said nothing.";
            case WALKED_OFF->"I walked off with the receiver and said nothing.";
            default->"";
        };
        if(!first.isEmpty())p.add("In the morning Gail rang, and June gave me the phone. "+first);
        if(own.getBoolean("InTheBlack"))p.add("A week on, Callum was at the kitchen sink in the night with a knife in his hand."+(own.getBoolean("FoughtCallum")?" I took hold of his wrist.":"")
            +" After that there is a gap. When it closed, the knife was gone, there was a towel on the floor, and June was in the doorway.");
        else if(own.getBoolean("SawKnifeAtSink"))p.add("A week on, I saw Callum at the kitchen sink in the night with a knife in his hand.");
        String week=own.getString("ToldGailWeek");
        String w=switch(week){
            case KNIFE_TOOK->"I told her Callum had the knife and I was getting it off him.";
            case KNIFE_DONT_KNOW->"I told her I did not know what had happened.";
            case KNIFE_LYING->"I told her June was lying.";
            case KNIFE_NOT_ME->"I told her it was not me.";
            case KNIFE_SAW->"I told her I had seen him at the sink with it.";
            case FINE->"I told her it was fine.";
            case NO_SLEEP->"I told her I still was not sleeping.";
            case LEAVE->"I told her I did not want to stay.";
            case NOTHING->"I said nothing.";
            case WALKED_OFF->"I walked off with the receiver and said nothing.";
            default->"";
        };
        if(!w.isEmpty())p.add("When Gail rang again"+(week.startsWith("knife_")?", she had June's version of the night already. ":", ")+w);
        if(own.getBoolean("HeardJuneHand"))p.add("In the laundry a door slammed, hard, and June cried out. Nobody saw it.");
        if(own.getBoolean("SawJuneHand"))p.add("She came out with her hand wrapped in a tea towel, and said she had caught it in the door.");
        if(own.getBoolean("BlamedForHand"))p.add("By the time Gail rang, it was me who had shut the door on it.");
        String hand=switch(own.getString("ToldGailHand")){
            case HAND_NOT_ME->"I told Gail I never touched the door.";
            case HAND_AWAY->"I told Gail I had not even been in the house.";
            case HAND_HERSELF->"I told Gail June had done it herself.";
            case HAND_HEARD->"I told Gail I had heard it and not seen it.";
            case HAND_WASNT_ME->"I told Gail it was not me.";
            case NOTHING,WALKED_OFF->"About the hand, I said nothing.";
            default->"";
        };
        if(!hand.isEmpty())p.add(hand);
        // 0.4.548: the last photograph, and Gail in the night (PellMark).
        if(own.getBoolean("HeardGlass"))p.add(own.getBoolean("BlamedForPhoto")
            ?"On the last night, after the house was locked, glass broke in the hall. It was the last of Nessa's school photographs. June rang Gail, and by then it was me who had broken it."
            :"On the last night, after the house was locked, glass broke in the hall.");
        if(own.getBoolean("GailCame")){
            var gone=taken(own);
            p.add(own.getBoolean("BlamedForPhoto")?"Gail's car came up the street in the night. June had rung her about me.":"Gail's car came up the street in the night. June had rung her.");
            String showed=switch(own.getString("ShowedGail")){
                case PellMark.PAGE->"I gave her the last page from the shed wall, in June's writing. She read it twice.";
                case PellMark.NOTE->"I gave her the note from the freezer's switch. She held it a long time.";
                case PellMark.TAPE->"I took her out to the shed and played her one of the tapes. She asked me to turn it off.";
                default->"";
            };
            if(!showed.isEmpty())p.add(showed);
            if(own.getBoolean("GailLeft"))p.add(own.getBoolean("BlamedForPhoto")?"She went away again without me.":"She went away again on her own.");
            if(!gone.isEmpty())p.add("She took "+CoopAccounts.names(gone)+" away in her car.");
        }
        int marks=own.getInt("GailMarks"),off=own.getInt("MarksCleared"),put=marks+off;
        if(put>0)p.add((put==1?"Gail put one thing on my file":"Gail put "+Stillness.words(put)+" things on my file")+(off==0?".":off==1?", and took one off again.":", and took "+Stillness.words(off)+" off again."));
        if(!PellEntries.written(own).isEmpty())p.add("In my journal, in my own hand, there were lines I never wrote.");
        int tapes=Integer.bitCount(own.getInt("TapesWatched"));
        if(own.getBoolean("ReadTheTapes"))p.add("In the shed were tapes of a woman talking to somebody who had lost a child. I watched all of them, in order, and by the end I knew what the water was for.");
        else if(tapes>0)p.add("In the shed were tapes of a woman talking to somebody who had lost a child. I watched "+(tapes==1?"one":Stillness.words(tapes))
            +(own.getBoolean("WatchedOutOfOrder")?", not in order.":"."));
        if(own.getBoolean("TookBucket"))p.add("On the last night I took the bucket from the laundry and scrubbed at the line"+(own.getBoolean("ScrubbedOut")?" until there was a gap in it.":"."));
        if(own.getBoolean("FedCallum"))p.add("There was a plate of dinner in the fridge under cling film. I took it out to Callum on the line. He sat down on the grass and ate it, and said, \u201CMy name's Callum.\u201D");
        else if(own.getBoolean("HeardCallum"))p.add("Out on the line, Callum said, \u201CMy name's Callum.\u201D It was the only thing I heard him say.");
        if(own.getBoolean("WokenByThePool"))p.add("I woke to water splashing out the back, and June saying a name over and over.");
        if(own.getBoolean("SawRite"))p.add(coop?"At midnight June walked Tilly down into the pool and said the name Nessa, again and again, and waited for an answer to it. Every time, the answer was Tilly."
            :"At midnight June walked Tilly down into the pool and called her Nessa, again and again, and waited for her to answer to it. Every time, Tilly said her own name back.");
        if(own.getBoolean("TookTillysHand"))p.add("I took Tilly's hand under the water.");
        if(own.getBoolean("GotTillyOut"))p.add(coop?"We went across the grass to the gap in the line, Tilly and I, and through it together.":"I led her across the grass to the gap in the line, and she stepped through it with me.");
        else if(own.getBoolean("SawTillyOut"))p.add("I saw Tilly come out through the gap in the line.");
        if(own.getBoolean("TookLanyard"))p.add("June's robe was over the chair by the pool, with a lanyard in its pocket. I took it.");
        if(own.getBoolean("UnlockedFreezer"))p.add("The third key on it opened the padlock on the freezer in the laundry.");
        if(own.getBoolean("SawNessa"))p.add((own.getBoolean("OpenedFreezer")?"I lifted the lid. ":"The freezer stood open. ")+"Under frosted plastic was Nessa, in a party dress and a paper crown, still.");
        if(own.getBoolean("BroughtJune"))p.add("I brought June through the house to the freezer by the hand, and told her it was Nessa. She sat down beside it with her hand on the plastic, and stopped calling.");
        else if(own.getBoolean("SawJuneSit"))p.add("June sat down beside the freezer with her hand on the plastic, and stopped calling.");
        p.add("When it got light, Gail's car came up the street and pulled in at the curb. Callum was sitting on the front step with his shoes on.");
        p.add(coop?"Tilly stood by Gail at the curb. Gail asked if it was Tilly, and it was.":"Tilly stood by Gail at the curb. Gail asked if it was Tilly, and she said it was.");
        if(own.getBoolean("BroughtJune"))p.add("June was still sitting by the freezer when the car came.");
        p.add(coop?"As I went past, Tilly put something in my hand: the spare rubber tip from the cane.":"As I went past, Tilly put something in my hand: the spare rubber tip from her cane.");
        p.add("I went down the path, over the line, and out into the street. Behind me nobody was calling any name at all.");
        return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.PELL_HOUSE),name,pages(String.join("\n\n",p)));
    }
}
