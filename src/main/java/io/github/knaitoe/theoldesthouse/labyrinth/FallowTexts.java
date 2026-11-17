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
 * in docs/FALLOW_0_4_672.md; each later part's in its own doc (0.4.681's, the buried days, in docs/FALLOW_0_4_681.md).
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
            case WINDOW->stage>=FallowBlock.BLOWN_LOW?"The window's frame. The glass has gone out of it, all but a few teeth along the edges."
                :stage==1?"The top of the front window. A fly walks on the inside of the glass."
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
                // 0.4.682, after the light.
                case FallowBlock.POLE_DOWN->"A telephone pole, down across the road. The wires are gone off it.";
                case FallowBlock.POLE_DOWN_TOP->"The top of the pole, down on the road, its crossarm driven into the ground on end. One insulator is still on it.";
                default->"A telephone pole, its wood split with weather.";
            };
            case WHEAT->switch(stage){
                case FallowBlock.BOWED->"Wheat with its heads bowed over, heavy.";
                case FallowBlock.STUBBLE->"Stubble, where the wheat has been cut.";
                case FallowBlock.ASH->"Stubble, burnt. The ash comes off on the hand.";
                // 0.4.683, round the girl's bean.
                case FallowBlock.SHOOT->"Shoots, up through the ash.";
                case FallowBlock.GREEN->"Young wheat, its heads still soft. It came up after the light.";
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
            case RUBBLE->switch(stage){
                case FallowBlock.HEAP->"A heap of what the roof was: joists, tar paper, a strip light still in its housing.";
                case FallowBlock.ON_DOORS->"The store, down across the cellar doors. It does not shift.";
                case FallowBlock.JOIST->"A roof joist, fallen across the rest, its nails still in it.";
                case FallowBlock.SHEET->"A sheet of the canopy's deck, buckled flat on the concrete.";
                case FallowBlock.LEANING->"A sheet of the canopy, caught on the island and leaning there.";
                case FallowBlock.COLUMN->"A column of the canopy, broken off at its foot. The pencil marks run along it now, a line and a date beside each.";
                case FallowBlock.WALL->"A run of the store's wall, its boards still lapped, lying where it fell.";
                default->"Siding, ceiling tile, a shelf's bracket, and dust over all of it.";
            };
            case FARM->switch(stage){
                case 1->"A post of the farmhouse porch.";
                case 2->"The porch rail. A coffee can of sand stands on it, full of cigarette ends.";
                case 3->"A mailbox on a post at the end of the drive. Its flag is down. The name on it has been painted over.";
                // 0.4.682, after the light.
                case FallowBlock.BEAM->"A beam of the farmhouse, burnt through. Under the ash it is still warm.";
                case FallowBlock.BURNT->"What the farmhouse came down to: boards burnt to char, and the ash of the rest.";
                case FallowBlock.JAMB,FallowBlock.LINTEL->"The front door's frame, charred through. The door in it was not touched.";
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
    /** 0.4.680, from below as the station comes down (FallowRuin): the canopy out front, the store, the fall of it across the doors. */
    public static final String RUIN_CANOPY="Out toward the road, sheet metal comes down on concrete and goes on ringing.",
        RUIN_STORE="Nearer, a roof gives, and everything under it goes with it.",
        RUIN_DOORS="Something heavy lands across the doors overhead. The light at their cracks is gone.";

    // ------------------------------------------------------------------------------------------------ 0.4.681, buried
    /** The ground still, below; the roof to a hand before the light and on each day after; the doors, too hot to dig at, from below and
     * from the yard; the first stroke from each side; a length of the doors clear, half of them, and the doors giving; coming up. */
    public static final String STILL="The shaking stops. Overhead, something goes on burning.",
        CEILING_BEFORE="The cellar's roof: planks on timbers, with the yard's earth over them.";
    public static final String[] CEILING={"The timbers overhead are hot. Something up there is still burning.",
        "The timbers are warm, but a hand can stay on them now.",
        "The timbers are cool. Whatever was burning up there has burned itself out."};
    public static final String HOT_DOORS="Heat comes down through the boards of the doors. Whatever lies on them is still burning.",
        HOT_HEAP="Heat comes off the heap over the cellar doors. Under it, something is still burning.",
        DIG_FIRST="You set your shoulder to the doors and heave. Dirt sifts down, and something overhead shifts.",
        DIG_FIRST_ABOVE="You drag at the fall over the cellar doors. A piece of it comes away in your hands.",
        CRACK="Between two boards over the steps, a crack of daylight.",
        HALF="Half the doors are clear. Ash sifts down between the boards.",
        GIVE="The doors give and go back over on their hinges. Ash comes down the steps on the air.",
        GIVE_ABOVE="The cellar doors heave up out of the fall and go back over on their hinges.",
        CAME_UP="Up the steps and out. Ash is coming down out of a sky with no sun in it, and lying on everything.";
    /** A hand on the lantern: turned down, put out, lit again; dry; guttering; gone out. */
    public static final String LANTERN_LOW="You turn the wick down. The light draws in to the lantern's glass.",
        LANTERN_DOUSED="You turn the wick down until the flame goes out.",
        LANTERN_LIT="You strike a match from the jar on the shelf and light the wick.",
        LANTERN_DRY="The lantern is dry. There is no oil left in it.",
        LANTERN_GUTTERS="The lantern gutters. Its flame shrinks and jumps.",
        LANTERN_GONE="The lantern goes out. There is no more oil.";
    /** Each morning below, from the second. */
    public static final String[] MORNING={"A second morning, by Otis's watch.","A third morning, by Otis's watch.","A fourth morning, by Otis's watch.",
        "Another morning, by Otis's watch."};
    /** Her bean, grown in the dark, seen when the light comes back; what she says of it then, by how far it has grown. */
    public static final String BEAN_GREW="In the lantern's light, Opal's bean is taller than it was. It has grown in the dark.";
    public static final String[] BEAN_BELOW={"It needs the sun. There isn't any.","It grew. In the dark, it grew.","Look how tall. It doesn't even need the sun.",
        "It's leaning at the doors."};
    /** The crock and the shelves, by the day. */
    public static final String[] CROCK_DAYS={"The crock of water, near full. The dipper hangs back on its side.",
        "The crock, down a hand's width. Nobody takes more than the dipper holds.",
        "The crock, half gone. A line is scratched on its side at the level each morning."};
    public static final String[] SHELF_DAYS={"Jars of peaches, beans and tomatoes. A jar of peaches is open and going round.",
        "Two jars are gone from the shelf. The empties stand back in their places, lids on.",
        "The shelf is thinner. The dates on the lids go back eleven summers."};
    /** Otis: putting the lantern out for the night, lighting it in the morning (or not), to a reader who lights it at night; digging with
     * them, beginning alone; the doors giving; up, looking out at it. */
    public static final String OTIS_NIGHT="Let's put it out a while. Save the oil for when we need to see.",OTIS_MORNING="Morning. Near enough, by my watch.",
        OTIS_MORNING_DARK="Morning, by my watch. For what that's worth down here.",OTIS_LEAVE_IT="Let it be dark a while. We'll want that oil.",
        OTIS_DIG="It's cooled up there. Shoulders to the doors: push when I push.",OTIS_ALONE="I'll make a start. Spell me when you've a mind to.",
        OTIS_GIVE="There. Up we go, slow. Mind the edges.",OTIS_LOOKS="Well. There it is.";
    /** Up in the yard: Opal by the steps with her bean, Faye calling toward the rig, Harlan at what is left of his store. */
    public static final String OPAL_UP="It needs the sun. There isn't any.",FAYE_UP_LINE="Boyd? Boyd!",HARLAN_UP_LINE="That was a good store.";

    // 0.4.683, the road (FallowRoad).
    /** Back at the station, a reader the light found out in it. */
    public static final String ARRIVE_AFTER="The store has no roof now. Over it the sky is ash.";
    /** The shadows on the store's front: one's own, a hand on it again, another reader's, Boyd's. */
    public static final String FOUND_OWN="Your shadow is on the store's front, where you stood when the light came.",OWN_ON_HAND="It does not come off on your hand.",
        SHADOW_OF="%s's shadow is on the wall, where the light found them.",SHADOW_REACHING="%s's shadow is on the wall, an arm up toward the rig.";
    /** Opal and her bean: setting it down, sitting with it, the fourth ring; and what comes up round her, seen. */
    public static final String OPAL_PLANTS="It can go here.",OPAL_SITS_WITH_IT="If I sit with it, it'll grow.",OPAL_COMING_UP="Look. It's all coming up.",
        GREENING="Around where the girl sits, something is coming up through the ash.";
    /** The man on the road: first seen, his faces, spoken to. */
    public static final String STRANGER_SEEN="Somebody is walking down the road out of the ash, from where the pads were.",HIS_FACE="The man has %s's face.",
        YOUR_FACE="The man has your face.",LOOKS_PAST="He looks past you, at the cellar doors.";
    /** Opal taken down out of his sight: by Faye, or by Otis when Faye is not up. */
    public static final String FAYE_TAKES_HER="Opal. Down the steps. Now.",OTIS_TAKES_HER="Down you go, sweetheart. Quiet as you can.";
    /** Otis's question on the second day, the reader's two answers, and what he says to each. What he says to an answer follows the House's
     * read of the reader's home: somebody to get back to, somewhere not to go back to, nowhere at all, or nothing read. */
    public static final String HOME_ASK="Anybody waiting on you, back home?",TELL_HIM="Tell him.",HOME_QUIET="That's all right. You don't have to say.";
    public static String homeAnswer(String read){
        return switch(read){
            case "HAVEN"->"Then you've got somewhere to get back to. Hold on to that down here.";
            case "DANGER"->"Some places you don't hurry back to. Nobody here's going to make you.";
            case "RUNAWAY"->"Nowhere at all? Well. You're with us now, for what that's worth.";
            default->"Fair enough. You don't owe me the whole of it.";
        };
    }
    /** Below by day, a line every so often, by the day (the last again after): who, what, and when it is said (Faye below or not, Harlan
     * below, the bean grown). */
    public static final String[][][] BELOW_TALK={
        {{"Otis","That's the store burning up there. We sit tight till it cools.",""},{"Harlan","Peaches on the second shelf. Mind the jars, they're all there is.","harlan"},
            {"Faye","Boyd got into the rig. That rig's steel all round.","faye"},{"Opal","Is Mama still in the car?","nofaye"},{"Otis","We'll go and see, when we're out.","nofaye"},
            {"Opal","It's too dark for my bean.",""}},
        {{"Harlan","Second day. I keep wanting to go up and open the store.","harlan"},{"Faye","My sister will have called the highway patrol by now.","faye"},
            {"Opal","I gave it some of my water. Only a little.","grown"},{"Otis","I've a sister up in Duluth. She'll be watching the road.",""}},
        {{"Harlan","Built this cellar for twisters. Never thought it'd see anything else.","harlan"},{"Opal","When we get out, can I put it in the ground?",""},
            {"Otis","First thing, sweetheart.",""},{"Faye","Whatever's up there, we go up together.","faye"}}};
    /** A word with one of them below: Otis before the roof is cool and after; Opal by how far her bean has grown; Harlan; Faye. */
    public static final String[] OTIS_WAIT={"We dig when it cools. Not before.","Save your strength. You'll want it for the doors."};
    public static final String OTIS_PUSH="Push when I push.";
    public static final String[] OPAL_BELOW={"It's too dark down here for it.","It's growing anyway. Look.","It's taller than the cup now.",
        "It's leaning at the doors. It wants out too."};
    public static final String[] HARLAN_BELOW={"There's water in the crock. A dipper each, morning and night.","I should have checked the oil in that lantern. I always meant to."};
    public static final String[] FAYE_BELOW={"Did you see him get into the rig? Did you see?","He'll be in the rig. He'll be all right in the rig."};
    /** A word with one of them once the doors have given. */
    public static String upTalk(int who){
        return switch(who){case Fallow.DRIVER->"Easy on the steps.";case Fallow.GIRL->"It needs the sun.";case Fallow.WOMAN->"Boyd?";default->"Mind the glass.";};
    }

    private static List<Component> pages(String... texts){var out=new ArrayList<Component>();for(String text:texts)for(String p:LookoutTexts.pages(text,150))out.add(HouseWriting.page(HouseWriting.WritingStyle.PLAIN,p));return out;}
    /** The first paper: Harlan's note, propped against the register. */
    public static ItemStack source(){
        return HouseWriting.book("The note by the register","Harlan",pages(
            "Under the car or out back. Ring the bell on the pump and I'll come.",
            "Pay for gas here when I come in. Coffee is ten cents, help yourself. Milk is in the cooler.",
            "The cellar out back is for weather. If the weather turns, go down and pull the doors to. Don't wait on me.",
            "Phone is for emergencies. Road maps are free if you buy gas."));
    }
    // ------------------------------------------------------------------------------------------------ 0.4.684: what he asks
    /** At the cellar doors (FallowSteps): his question and the ride he offers, in the panel and over his head. */
    public static final String WHERE_IS_SHE="Folks ought to stick together. Where's the little one?",
        RIDE="That big fella's rig will run. I'll carry the lot of you to town, the little one too.";
    /** The answers a reader may give him, and what he says to them. */
    public static final String NOBODY_DOWN_THERE="There's nobody down there.",DOWN_THE_STEPS="She's down the steps.",
        IS_THAT_RIGHT="Is that right.",OBLIGED="Obliged.";
    /** The rig that should not run. */
    public static final String RIG_TURNS="Out on the forecourt, the rig's engine turns over and catches.";
    /** A reader on the steps: he smiles at them, and goes. */
    public static final String HE_SMILES="He looks at you on the steps, and smiles.",FIND_HER_ANYWAY="Have it your way. I'll find her anyway.",
        WALKS_BACK="He turns from the steps and walks back toward the road.";
    /** Nobody on the steps: down he goes, and she is not there. */
    public static final String GOES_DOWN="He goes down the cellar steps.",FAYE_GONE="She was right here. She was right here by me.",
        OTIS_GONE="She went right past me. I never saw her go.",
        AT_THE_FENCE="He stands at the yard's fence, looking out at the girl in the field.";
    /** Gone. */
    public static final String ROAD_EMPTY="The road is empty as far as the ash lets you see.",FENCE_EMPTY="Nobody is at the fence now.";
    /** The girl, out in the field. */
    public static final String CAME_UP_FOR_ME="It came up for me.";

    /** 0.4.684: the account, in the keeper's ledger under the counter, its edges singed. Written from what this reader did that afternoon
     * and after, and from nothing they did not. */
    public static ItemStack account(String name,CompoundTag own){
        var parts=new ArrayList<String>();
        // The afternoon.
        var a=new StringBuilder("There was a gas station on a two-lane road, a store and a garage under one roof, in wheat that ran to the edge of sight.");
        String chore=own.getString("Chore");
        if(chore.equals(Fallow.TILL))a.append(" The keeper had me mind his till while he was under a car.");
        else if(chore.equals(Fallow.GIRL_CHORE))a.append(" The keeper had me keep a girl company while her mother saw about their car. She had a bean in a paper cup.");
        if(own.getBoolean("Answered"))a.append(" A man bought a soda and asked me the time, and I told him. He walked off into the wheat.");
        else if(own.getBoolean("Silent"))a.append(" A man bought a soda and asked me the time. I didn't tell him. He walked off into the wheat.");
        else a.append(" A man bought a soda and walked off into the wheat.");
        parts.add(a.toString());
        // The light.
        var b=new StringBuilder("The radio gave a long tone, and out in the wheat the ground opened.");
        boolean below="cellar".equals(own.getString("AtLight"));
        if(own.getBoolean("BroughtFaye"))b.append(" I brought the girl's mother to the cellar by the hand.");
        if(own.getBoolean("HeldDoors"))b.append(" I held the cellar doors.");
        if(below){
            b.append(" I was under the ground when the light came.");
            int days=own.getInt("Days");
            if(days>0)b.append(" We were down there ").append(days==1?"a day":count(days)+" days").append(" by the lantern.");
            if(own.getBoolean("DugOut"))b.append(" We dug our way up the steps a stroke at a time.");
        }else if(own.contains("AtLight")){
            b.append(" I was out in it when the light came, and I woke at the House's door.");
            if(own.getBoolean("CameBack"))b.append(" I went back. The store had no roof.");
        }
        parts.add(b.toString());
        // After.
        var c=new StringBuilder();
        if(own.getBoolean("FoundShadow"))c.append("My shadow was on the store's front, where I had stood. ");
        if(own.getInt("OthersShadows")>0)c.append(own.getInt("OthersShadows")==1?"Somebody else's was beside it. ":"Other people's were beside it. ");
        if(own.getBoolean("SawGreen"))c.append("The girl put her bean in the ground in the yard, and sat with it, and the ground came up round her. ");
        int faces=own.getInt("SawFaces");
        if(faces!=0){
            c.append("A man came walking down the road out of the ash, from where the pads had been.");
            var worn=new ArrayList<String>();
            if((faces&1<<FallowRoad.KEEPER_FACE)!=0)worn.add("Harlan's");
            if((faces&1<<FallowRoad.BOYD_FACE)!=0)worn.add("Boyd's");
            if((faces&1<<FallowRoad.OTIS_FACE)!=0)worn.add("Otis's");
            if((faces&1<<FallowRoad.VIEWER)!=0)worn.add("mine");
            if(!worn.isEmpty())c.append(" Each time I looked at him again he had another face: ").append(joined(worn)).append(".");
            else c.append(" It was the man who had bought the soda.");
        }
        if(c.length()>0)parts.add(c.toString().trim());
        // What I told him, and what came of it.
        var d=new StringBuilder();
        int answer=own.getInt("RoadAnswer");
        if(answer!=0)d.append("At the cellar doors he asked me where the little one was. ").append(switch(answer){
            case FallowSteps.LIED->"I told him there was nobody down there.";case FallowSteps.TOLD->"I told him she was down the steps.";default->"I said nothing.";});
        int end=own.getInt("RoadEnd");
        if(end==1)d.append(own.getBoolean("OnSteps")?" I stood on the steps. He smiled at me, said he would find her anyway, and walked back up the road."
            :" Somebody was standing on the steps. He smiled, said he would find her anyway, and walked back up the road.");
        else if(end==2)d.append(" Nobody stood on the steps, and he went down them. She was not there. She was out past the fence, sitting in the field, and the wheat was coming up round her. He stood at the fence and looked at her.");
        if(d.length()>0)parts.add(d.toString().trim());
        parts.add(end==1?"She was still down the steps when I left. He had said he would find her.":end==2?"When I looked again he was gone from the fence. She was still out there, and it was still coming up."
            :"I did not see how it ended.");
        return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.FALLOW),name,pages(parts.toArray(String[]::new)));
    }
    private static String count(int n){String[] words={"one","two","three","four","five","six","seven","eight","nine","ten","eleven","twelve"};return n>=1&&n<=12?words[n-1]:Integer.toString(n);}
    private static String joined(List<String> items){
        if(items.size()<2)return items.isEmpty()?"":items.get(0);
        return String.join(", ",items.subList(0,items.size()-1))+(items.size()>2?",":"")+" and "+items.get(items.size()-1);
    }
}
