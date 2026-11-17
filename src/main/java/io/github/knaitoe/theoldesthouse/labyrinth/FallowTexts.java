package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.672, Fallow, part 1 (docs/FALLOW_0_4_672.md): what the station's, the yard's, the road's and the field's things say to a hand, the
 * five people's own lines, and Harlan's note by the register, the first paper. Every line is new in this release and is flagged to the owner
 * in docs/FALLOW_0_4_672.md.
 */
public final class FallowTexts {
    private FallowTexts(){}
    public static final String HINT="There is a note by the register.";
    /** The five, in the order of Fallow's phases; the stranger has no name. */
    public static final String[] NAMES={"Harlan","Faye","Opal","Boyd","Otis"};

    public static final String TRAILER_DOOR="The trailer's door. It is locked.",
        CELLAR_OPENED="The cellar doors swing back on their hinges. Steps go down into the dark.",
        CELLAR_SHUT="The cellar doors come down and lie shut.";

    /** What a thing says to a hand, by its kind and stage; null for the note, which the House's own reading gives. */
    public static @Nullable String thing(FallowBlock.Kind kind,int stage){
        return switch(kind){
            case NOTE->null;
            case COUNTER->switch(stage){
                case 1->"The register. One of its keys sticks halfway down. The drawer is shut.";
                case 2->"The end of the counter, hinged so it lifts. It stands up, open, for whoever works behind it.";
                case 3->"A rack of candy bars and gum at the front of the counter, where a hand waiting to pay will find it.";
                default->"The counter. Its top is worn through to the board where people set things down to pay.";
            };
            case STOOL->"Harlan's stool behind the counter. The seat is split and mended with tape.";
            case BACKSHELF->switch(stage){
                case 1->"Cartons of cigarettes behind a little rail, cans of motor oil, fan belts in their paper sleeves.";
                case 2->"A radio on the shelf, playing low. A ball game, a long way off, and the crowd coming and going under it.";
                default->"A cabinet under the back shelves. Its doors are shut.";
            };
            case TV->switch(stage){
                case FallowBlock.CARD->"The television shows a card with nothing on it, and a tone.";
                case FallowBlock.DARK->"The television is off.";
                default->"A television on a bracket in the corner, the sound turned down. A weather map, and a man pointing at it.";
            };
            case COOLER->stage==3?"The lit panel along the tops of the cooler's doors."
                :stage>=1?"The cooler's lit sign: ICE COLD."
                :"A cooler with glass doors. Milk, bottles of pop, sandwiches in wax paper. The glass is beaded on the inside.";
            case GONDOLA->switch(stage){
                case 1->"Boxes of crackers, cereal, matches. Three loaves of bread.";
                case 2->"Quarts of oil, wiper blades, a funnel, spark plugs on a card.";
                case 3->"The top of the shelving. Somebody has left a pair of work gloves on it.";
                default->"Canned goods: beans, peaches, soup, potted meat.";
            };
            case MAPS->stage==1?"The rack's header: ROAD MAPS."
                :"A rack of road maps, one for each state round about. The nearest one has been unfolded and put back wrong.";
            case COFFEE->"A coffee machine on a stand, the pot half full. Paper cups, sugar, and a can with a slot cut in its lid.";
            case WINDOW->stage==1?"The top of the front window. A fly walks on the inside of the glass."
                :"The store's front window. Through it, the pumps, the road, and the wheat beyond the road, to the edge of sight.";
            case TUBE->"A strip light. It buzzes, very faintly.";
            case SIGNBAND->"The station's name, painted along the edge of the roof in letters a foot high.";
            case PUMP->switch(stage){
                case 1->"The pump's dials, gallons and price, on little drums. They read nothing.";
                case 2->"The diesel pump, set apart for the trucks. Its bell rings as the nozzle comes off its hook.";
                case 3->"The diesel pump's dials. They still show what the last truck took.";
                default->"A gas pump. Its bell rings as the nozzle comes off its hook.";
            };
            case ISLAND->stage==1?"The rounded end of the island, scraped where tires have run against it."
                :"The island's curb, painted where the cars pull in close.";
            case CANOPY->switch(stage){
                case 1->"A light let into the canopy, lit, though the sun is out.";
                case 2,3->"The canopy's edge, with the station's name along it.";
                case 4->"A column of the canopy. Pencil marks run up one side, a line and a date beside each.";
                case 5->"The column's foot, set in concrete.";
                default->"The canopy over the pumps. Swallows have built under it.";
            };
            case SIGN->switch(stage){
                case 1->"The price board: REGULAR, ETHYL, DIESEL, and the prices in slotted numbers.";
                case 2->"HARLAN'S, on the board at the top of the pole, and under it, GROCERIES.";
                default->"The pole of the station's sign.";
            };
            case POLE->switch(stage){
                case 1->"The top of a telephone pole: a crossarm, glass insulators, the wires.";
                case 2,3,4->"The wires, slack between the poles, running along the road out of sight both ways.";
                default->"A telephone pole, its wood split with weather.";
            };
            case WHEAT->switch(stage){
                case FallowBlock.BOWED->"Wheat with its heads bowed over, heavy.";
                case FallowBlock.STUBBLE->"Stubble, where the wheat has been cut.";
                case FallowBlock.ASH->"Stubble, burnt. The ash comes off on the hand.";
                default->"Wheat, ripe, to the waist. It runs to the edge of sight every way but the road.";
            };
            case PAD->switch(stage){
                case 1->"A fence post, set in concrete. The wire on it is new.";
                case 2->"The gate in the fence. It is chained and padlocked.";
                case 3->"A light pole inside the fence.";
                case 4->"A floodlight, lit, in daylight.";
                case 5->"A seamed edge of concrete, level with the gravel.";
                case 6->"A wide slab of concrete, flat in the gravel.";
                case 7->"A low steel box with a hatch in it, bolted shut.";
                default->"Chain-link, with three strands of wire on arms along the top. A sign on it: PROPERTY OF THE U.S. GOVERNMENT. NO TRESPASSING.";
            };
            case LIFT->switch(stage){
                case 1->"The lift's bar, under the car.";
                case 2->"The workbench. Tools lie on a rag in the order they were used.";
                case 3->"A pegboard over the bench, each tool's outline drawn on it. Two outlines are empty.";
                case 4->"A stack of used tires.";
                case 5->"An oil drum with its lid cut away, half full of rags.";
                case 6->"The top of a post of the lift.";
                default->"A post of the car lift.";
            };
            case TRAILER->switch(stage){
                case 1->"The trailer's hitch, propped on a block. It has not been hitched to anything in a long time.";
                case 2->"A propane tank, chained to the trailer's side.";
                case 3->"Cinder blocks, stacked for a step.";
                case 4->"A clothesline post.";
                case 5->"The clothesline. Two work shirts and a dish towel, pinned, stirring.";
                case 6->"A burn barrel, scorched through in places. Ash in the bottom.";
                case 7->"The yard's wire fence, sagging between its posts.";
                default->"A window of the trailer. A curtain is drawn across it.";
            };
            case CELLAR,CELLAR_OPEN->null;
            case SHELF->stage==1?"A shelf of tins, a box of candles, matches in a jar, a first-aid kit."
                :"A shelf of jars: peaches, beans, tomatoes, each dated on the lid.";
            case COT->"A cot, a blanket folded at its foot.";
            case CROCK->switch(stage){
                case 1->"A lantern on its hook, lit.";
                case 2->"A lantern on its hook, unlit.";
                default->"A crock of water, a dipper hung on its side.";
            };
            case FARM->switch(stage){
                case 1->"A post of the farmhouse porch.";
                case 2->"The porch rail. A coffee can of sand stands on it, full of cigarette ends.";
                case 3->"A mailbox on a post at the end of the drive. Its flag is down. The name on it has been painted over.";
                default->"A window of the farmhouse across the road. There is nobody at it.";
            };
            case RIG->switch(stage){
                case 0,1->"The rig's hood. The engine under it ticks as it cools.";
                case 4,5->"The sleeper behind the cab. A curtain is drawn across its window.";
                case 6->"The roof of the rig's cab.";
                default->"The rig's cab. A thermos on the dash, and a clipboard of papers.";
            };
            case BOX->switch(stage){
                case 0->"The trailer's wheels. Dust is caked on the hubs.";
                case 1->"The trailer's landing legs, cranked up.";
                case 2->"The trailer's back doors, sealed with a wire tag.";
                default->"The side of the trailer, dusty enough to write in. Nobody has.";
            };
            case BEAN->switch(stage){
                case 1->"The bean has put out two more leaves since it was set down.";
                case 2->"The bean has grown over the rim of its cup and leans out after the light.";
                case 3->"The bean has grown long and thin in the dark, its leaves open wide, a tendril feeling for something to hold.";
                default->"A paper cup of dirt with a bean come up in it, two leaves open. OPAL is written on the cup in pencil, the letters going downhill.";
            };
        };
    }

    // ------------------------------------------------------------------------------------------------ 0.4.673, the afternoon
    /** The register rung up for someone waiting; the drawer opened on nobody. */
    public static final String RING="The keys go down, the bell rings and the drawer jumps out. The money goes in.",
        RING_NOTHING="The drawer jumps out on its spring. Nobody is waiting to pay.",
        COINS_LEFT="Coins clink on the counter by the register.";
    /** Harlan's chore for a reader, in the four ways the House's read lets him address them: minding the till; keeping Opal company. */
    public static final String[] TILL={"Do me a favor and mind the till while I'm under that car. Ring up whatever they bring you.",
        "Do me a favor, sir, and mind the till while I'm under that car. Ring up whatever they bring you.",
        "Do me a favor, ma'am, and mind the till while I'm under that car. Ring up whatever they bring you.",
        "Do me a favor, friend, and mind the till while I'm under that car. Ring up whatever they bring you."};
    public static final String[] COMPANY={"Keep the little one company while her mama sees about the car? She's been on that step since noon.",
        "Keep the little one company, son, while her mama sees about the car? She's been on that step since noon.",
        "Keep the little one company, honey, while her mama sees about the car? She's been on that step since noon.",
        "Keep the little one company, kid, while her mama sees about the car? She's been on that step since noon."};
    /** Harlan under the car, to a reader with a chore: the first time, then the second each time after. */
    public static String working(boolean till,int heard){
        if(till)return heard==0?"Drawer sticks. Give it the heel of your hand.":"Gas is on the honor. Just ring up what they take from the store.";
        return heard==0?"She's no trouble. Talks about that bean like it was a dog.":"Her mama's had a long day. You're doing her a kindness.";
    }
    /** Boyd and Harlan at the car, in turn, overheard. */
    public static final String[][] AT_THE_CAR={{"Boyd","You said an hour an hour ago."},{"Harlan","Fuel pump's off a different year. I'm making it fit."},
        {"Boyd","Make it fit faster."}};
    /** Opal asking for her bean down out of the car, and Harlan handing it to her. */
    public static final String ASK_BEAN="Can I have my bean? It needs the sun.",HAND_BEAN="Here. Hold it level, now.";
    /** Opal by her bean, to whoever keeps her company, in order. */
    public static final String[] BEAN_TALK={"It's a bean. It came up in a cup at school, and I got to bring it.",
        "You can touch the leaf. Gentle.",
        "Boyd says it won't live in a cup. It's living.",
        "When we get to my aunt's I'm going to put it in the ground by her porch."};
    /** At the counter: Faye, Otis and the stranger, each asking, thanking whoever rang them up, or leaving the money. */
    public static final String[][] CUSTOMER={{"Just the milk.","Thank you.","I'll leave it by the register."},
        {"The coffee, and twenty on the diesel.","Obliged.","Money's on the counter, Harlan."},
        {"Just the pop.","Keep the change.","That ought to cover it."}};
    /** The stranger: to Opal, to himself when she does not answer, to a reader, and after. */
    public static final String STRANGER_GREETS="Hot one.",ASKS_NAME="That's a fine bean. What's your name?",NAMES_KEEP="That's all right. Names keep.",
        ASKS_TIME="Got the time?",TIME_ANSWER="Almost five.",SAY_NOTHING="Say nothing.",ANSWERED="Later than I thought.",UNANSWERED="Doesn't matter.";
    /** The stranger has no name; the conversation panel calls him this. */
    public static final String STRANGER_LABEL="The man";

    /** The five's lines: the first time, then the second each time after. */
    public static String line(int who,int heard){
        return switch(who){
            case Fallow.KEEPER->heard==0?"Pumps are on the honor when I'm under a car. Pay at the register when I come out."
                :"Been a dry summer. Wheat came in early, though.";
            case Fallow.WOMAN->heard==0?"It started making a noise past the last town and quit right in front of this place. He says it's the fuel pump."
                :"We were supposed to be at my sister's by supper.";
            case Fallow.GIRL->heard==0?"I'm growing a bean. It's in a cup. It's in the car, because Mama says it'll spill."
                :"You can see a long way here. There's nothing in the way.";
            case Fallow.MAN->heard==0?"He said an hour. It's been an hour."
                :"Don't touch the car.";
            default->heard==0?"Hauling seed corn north. I stop here every time I come through. Best coffee on this road, which isn't saying much."
                :"Those fenced squares out in the wheat, with the lights? Government. Don't go poking round them.";
        };
    }

    // ------------------------------------------------------------------------------------------------ 0.4.674, the four minutes
    /** The ball game stops for the tone; under it, after a while, a voice reading. */
    public static final String TONE="The ball game stops. A long tone comes out of the radio, two notes at once, and does not stop.",
        TONE_VOICE="Under the tone a man's voice reads, slow: go below ground now. Stay away from windows. Do not use the telephone.",
        RADIO_TONE="The radio on the shelf. The tone comes out of it, steady, and the voice under it reading the same three things over.",
        LIDS="Out in the wheat, both ways, something heavy grinds and keeps grinding.",
        LIDS_INSIDE="The floor hums. The jars on the back shelf touch each other and ring.",
        LAUNCH="A roar comes over the wheat. Out of the haze something climbs on a column of fire, slowly, then faster, then up and gone.",
        LAUNCH_TWO="Another, from the other side of the road.",
        SIREN="A long way off to the east a siren starts up, and another past it, fainter.";
    /** Boyd, laughing at the tone; Harlan, not; Otis and Opal when the ground starts to hum. */
    public static final String BOYD_TEST="It's a test. They do that.",HARLAN_NOT_TEST="That's not the test. The test tells you it's a test.",
        OTIS_PADS="Twenty years I've driven past those pads. Never once saw a soul out at them.",OPAL_HUM="Mama, the ground's buzzing.";
    /** Harlan, when they go up. */
    public static final String GOING_UP="That's ours going up. Theirs will be coming for the holes.",TO_THE_CELLAR="Everybody in the cellar. Now.";
    /** Harlan at the cellar doors, holding them, calling, in turn; to a reader who speaks to him there; to a reader who takes the doors. */
    public static final String[] CALLING={"Down the steps! Mind your heads at the bottom!","Faye! Leave it!","Boyd! There's room!","Come on. Come on."};
    public static final String HARLAN_HOLDING="Get down there. I've got the doors.",
        TAKE_DOORS="You hold them, then. Don't let them shut till everybody's down.",TAKEN_DOORS="You take the cellar doors from Harlan. He goes down the steps.",
        THROWN_BACK="Harlan throws the doors back open.";
    /** Harlan at the last, with the doors: shutting them over the people below and staying out; or pulling them down from below. */
    public static final String SHUT_OVER="Keep them shut. Don't open them for anything.",SHUT_OUT="The cellar doors come down and lie shut.",
        SHUT_BELOW="Harlan reaches up and pulls the doors down over the steps, and the dark comes in.",
        SHUT_BY_OTIS="Otis reaches up and pulls the doors down over the steps, and the dark comes in.",
        SHUT_AFTER="The doors come down again behind you.";
    /** Faye at the car, which is up on the lift; to a reader who takes her by the arm; asking after Opal; finding her below. */
    public static final String FAYE_LIFT="How do you get it down? How does it come down?",FAYE_STAYS="Everything we have is in that car.",
        FAYE_TAKEN="You take Faye by the arm.",FAYE_WHERE="Where's Opal? Where is she?",FAYE_FOUND="There you are. There you are.";
    /** Boyd at the rig's door, who will not go down. */
    public static final String[] BOYD={"Otis! Where's your keys? It's not even locked!","I'm not getting in a hole in the ground.",
        "This thing'll do sixty with the box on. We can be fifty miles off.","Go on, then. Go on down."};
    public static final String BOYD_FAYE="Faye! Faye, come on!";
    /** Otis going for Opal, lifting her, setting her down below. */
    public static final String OTIS_COMING="Come here, sweetheart. Bring your bean.",OPAL_CUP="I've got it. I've got it.",
        OTIS_HOLD="Hold on to me.",OTIS_DOWN="There. You sit right there with your bean.",OTIS_GO="Go on down. I've got her.";
    /** In the cellar while they wait, in order, a line every so often: who says it, and what. */
    public static final String[][] WAITING={{"Otis","How long, do you figure?"},{"Harlan","Not long."},{"Opal","It needs the sun. My bean."},
        {"Otis","It'll keep. Beans are tough."},{"Opal","Is Boyd coming?"},{"Otis","He's coming."},{"Harlan","Keep back from the steps."},
        {"Opal","Can I count?"},{"Otis","Go on and count."}};
    /** Opal counting, at the last. */
    public static final String[] COUNTING={"One.","Two.","Three.","Four.","Five.","Six.","Seven."};
    /** In the cellar at the light; the dust after. */
    public static final String CRACKS="Light comes in round the doors, at every crack at once.",DUST="Dust comes down out of the roof and does not stop.";

    private static List<Component> pages(String... texts){var out=new ArrayList<Component>();for(String text:texts)for(String p:LookoutTexts.pages(text,150))out.add(HouseWriting.page(HouseWriting.WritingStyle.PLAIN,p));return out;}
    /** The first paper: Harlan's note, propped against the register. */
    public static ItemStack source(){
        return HouseWriting.book("The note by the register","Harlan",pages(
            "Under the car or out back. Ring the bell on the pump and I'll come.",
            "Pay for gas here when I come in. Coffee is ten cents, help yourself. Milk is in the cooler.",
            "The cellar out back is for weather. If the weather turns, go down and pull the doors to. Don't wait on me.",
            "Phone is for emergencies. Road maps are free if you buy gas."));
    }
    /** The account. Nothing asks for it until a later part; it says only what anybody could see of the place. */
    public static ItemStack account(String name,CompoundTag own){
        return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.FALLOW),name,pages(
            "There was a gas station on a two-lane road, a store and a garage under one roof, in wheat that ran to the edge of sight. A car was up on the lift. "
                +"Across the road stood a farmhouse, and out in the wheat were two fenced squares with lights in them."));
    }
}
