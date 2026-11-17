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
        };
    }

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
