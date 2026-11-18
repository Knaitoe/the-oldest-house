package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import io.github.knaitoe.theoldesthouse.labyrinth.TheListBlock.Kind;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.699, The list, part 1a (docs/THE_LIST_0_4_699.md): what a hand on each thing in Lamesa finds, said as a grey cue. Every line here
 * is new and flagged in the doc. The town and county are the book's; the stores, the signs and everything said are written for this.
 */
public final class TheListTexts {
    private TheListTexts(){}
    /** The House's hint for the place, given once. */
    public static final String HINT="Your own back door, and the town outside it.";
    /** A hand on a neighbor's door, shut for now. */
    public static final String SHUT_DOOR="Locked. Somebody's television is on inside.";
    public static final String EMPTY_DOOR="Nobody has lived here a while. The door gives when you lean on it.";
    /** 0.4.700: Amber's front door, shut for now. */
    public static final String AMBER_DOOR="Locked. The window beside it flickers with a television.";

    private static List<Component> pages(String... texts){var out=new ArrayList<Component>();for(String text:texts)for(String p:LookoutTexts.pages(text,150))out.add(HouseWriting.page(HouseWriting.WritingStyle.PLAIN,p));return out;}
    /** The first paper: the pad by the kitchen telephone. */
    public static ItemStack source(){
        return HouseWriting.book("The pad by the phone","Mom",pages(
            "Plate in the oven. Wrap it up if you don't want it.",
            "Amber called. Twice. I said you were out.",
            "The back door sticks. Lift it when you shut it or it won't latch.",
            "Lights off when you come in. All of them."));
    }
    /** The account. Nothing asks for it until a later part; it says only what anybody could see of the town. */
    public static ItemStack account(String name,CompoundTag own){
        return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.THE_LIST),name,pages(
            "The back door let me into my own kitchen, in Lamesa, at night. Out front the street was caliche and the houses across it were dark. "
                +"Main Street was lit for nobody, and past the highway the gin stood by the tracks under the water tower with the town's name on it."));
    }

    /** What a hand finds on a fitting of this kind at this stage; null if there is nothing to say. */
    public static @Nullable String thing(Kind kind,int stage){
        return switch(kind){
            case COTTON->switch(stage){case 1->"A plant that never got its height. The bolls on it are open anyway.";case 2->"It leans out over the end of the row, heavy.";case 3->"Picked. Stalks and husks, and a few tufts caught in them.";default->"Cotton, open. It comes away in your fingers, and there's a seed in it.";};
            case MESQUITE->switch(stage){case 0,1->"Mesquite. The bark is ridged and the thorns are long.";case 4->"A low branch. There's a thorn in it as long as a finger.";default->"Small leaves, a lot of them. The branches are mostly thorn.";};
            case WEED->switch(stage){case 0->"A tumbleweed, caught here till the wind changes.";case 1->"Broomweed, dry, gone to seed.";case 2->"A tuft of grass, dead at the top.";case 3->"Goatheads. One goes straight into your thumb.";case 4->"Prickly pear. You keep your hand off it.";default->null;};
            case POLE->switch(stage){case 5->"The street light hums. Bugs go round it.";case 6->"A transformer on the pole. It ticks.";default->"A pole, creosote and splinters. Staples in it where flyers were.";};
            case FENCE->switch(stage){case 1->"A fence post, set in a lump of concrete.";case 2->"The gate's latch lifts. Its hinge complains.";default->"Chain-link. It gives a little and rings along its length.";};
            case SIGNPOST->switch(stage){case 0->"South Ninth and Avenue K.";case 1->"Main Street and Avenue K.";case 2->"South Seventh and Avenue K.";case 3->"A stop sign. Somebody has shot at it.";case 4->"The bus stops here. The schedule on it has been torn off.";case 5->"The crossbuck at the tracks. The paint is coming off it in flakes.";default->null;};
            case TRACK->"The rail is still warm from the day.";
            case MAILBOX->"The flag's down. There's nothing in it.";
            case WINDOW->switch(stage){case 1->"Lit. Somebody's moving around in there.";case 2->"The kitchen window. The curtain is half drawn.";case 3->"A screen, with a tear at one corner patched with tape.";case 4,5->"The picture window. The street's dark in it.";default->"Dark. The curtain is drawn.";};
            case KITCHEN->switch(stage){case 1->"The sink. A glass upside down on the drainboard.";case 2->"The range. One burner has a pan on it, cold.";case 3->"The refrigerator hums. The door's covered in magnets and a schedule.";case 4->"The freezer's top half. Ice trays and a carton of something.";case 5->"The cabinet. Plates, glasses, the good ones at the back.";case 6->"The telephone. The cord is long enough to reach the hall.";case 7->"The canisters, Flour, Sugar, Coffee, and the toaster with its crumbs.";default->"The counter. A drawer of string and batteries and rubber bands.";};
            case TABLE->stage==1?"A dinette chair. The vinyl is split on the seat.":"The table. Someone's left the salt out.";
            case BED->stage==1?"The foot of the bed. A blanket kicked down.":"Your pillow. It still has the shape of your head in it.";
            case ROOM->switch(stage){case 1->"Your desk. The tape deck has a tape in it, rewound.";case 2->"The clock radio. The numbers glow.";case 3->"The closet. Clothes on hangers, shoes in a heap.";case 4->"A low shelf. Paperbacks with broken spines.";default->"A dresser. The top drawer won't shut all the way.";};
            case POSTER->switch(stage){case 0->"A band. You know every song on the tape.";case 1->"A film poster, a hooded figure. Tacked up at the corners.";case 2->"The feed store's calendar, still on last month.";case 3->"A pennant from the high school.";case 4->"A picture in a frame. You don't look at it long.";case 5->"Mountains. Somewhere a long way from here.";default->null;};
            case DEN->switch(stage){case 0,1->"The couch. There's change down between the cushions.";case 2->"The recliner. It leans back on its own.";case 3->"The television. Off. Your face is in it.";case 4->"The lamp on the end table. It's on.";case 5->"The coffee table. Magazines and a coaster.";default->null;};
            case BATH->switch(stage){case 0,1->"The tub. A ring round it at the water line.";case 2->"The toilet. The handle has to be held down.";case 3->"The vanity. A toothbrush in a cup.";case 4->"The mirror. You don't look long.";default->null;};
            case ROOFTOP->switch(stage){case 0->"The swamp cooler, off. It smells of wet straw.";case 1->"The cooler's duct.";case 2->"The aerial. It's bent a little toward Lubbock.";case 3->"A vent stack.";default->null;};
            case YARD->switch(stage){case 0->"The porch light. Moths.";case 1->"The clothesline's post.";case 2->"The line. A clothespin left on it.";case 3->"The burn barrel. Ash and the end of a box.";case 4->"The faucet. It drips.";case 6->"A carport post, rusted at its foot.";case 7->"The carport's roof.";default->null;};
            case SHOP->switch(stage){case 0,1->"The shop's roll-up door. It's down and it stays down.";case 2->"The shop's window. Too dirty to see in.";case 3->"The light over the shop door.";default->null;};
            case FIXTURE->switch(stage){case 0->"The kitchen light. Bugs dead in the globe.";case 1->"The hall light.";case 2->"A strip light. It flickers once and holds.";case 3->"A bare bulb.";case 4->"A floodlight.";default->null;};
            case FRONT->switch(stage){case 0->"The display window. Things lit for nobody.";case 1->"The transom over the window.";case 2->"The awning. Something has nested in its frame.";case 3->"The valance.";case 4->"The glass gives you back the street.";case 5->"Brick.";default->null;};
            case SIGN_DRUG->"Dawson Drug.";
            case SIGN_FEED->"Feed and Seed.";
            case SIGN_VIDEO->"Reel Time Video. The letters are lit.";
            case SIGN_LAW->"The sheriff's substation.";
            case SIGN_DRIVEIN->stage==0?"The sign's pole.":"The Caprock Drive-In. It's lit, and the arrow points in.";
            case DRUG->switch(stage){case 1->"The end of an aisle. Cough drops, hair nets.";case 2->"The counter.";case 3->"The register, shut.";case 4->"The pharmacist's shelves, behind the counter. You can't read the labels from here.";case 5->"A fountain stool. It turns.";case 6->"The magazine rack.";case 7->"The card spinner. Get well, sympathy, a birthday.";default->"Shampoo, aspirin, film, bobby pins.";};
            case VIDEO->switch(stage){case 0->"The horror wall. You've seen most of them.";case 1->"New releases. All out.";case 2->"A shelf of boxes, empty inside, the tapes behind the counter.";case 3->"The counter.";case 4->"The register.";case 5->"The return slot.";case 6->"A cardboard standee with a cardboard knife.";case 7->"The television on its bracket. Static.";default->null;};
            case LAW->switch(stage){case 0->"A desk. A coffee ring on the blotter.";case 1->"The radio desk. It crackles now and then.";case 2->"A filing cabinet, locked.";case 3->"A bench. Somebody carved a name into it and somebody else sanded it.";case 4->"The flagpole. The rope slaps it.";case 5->"The flag.";case 6->"The bulletin board. Wanted, missing, lost dog.";case 7->"The water cooler. A cone cup in the trash.";default->null;};
            case FEED->switch(stage){case 0,1->"Feed sacks, stacked. They smell of molasses.";case 2->"A mineral block, licked to a dip.";case 3->"The counter. A spike of receipts.";case 4->"A seed bin.";case 5,6->"The bus bench. It says the drive-in's name on the back.";case 7->"The loading dock's bumper.";default->null;};
            case DRIVEIN->switch(stage){case 0,1->"The canopy over the stalls.";case 2->"A canopy post.";case 3->"The menu and the speaker. The speaker hisses.";case 4->"The order window. Shut, lit.";case 5->"The fryer. The oil's gone cloudy.";case 6->"The flat-top, scraped clean.";case 7->"A picnic table, sticky.";default->null;};
            case GIN->switch(stage){case 0,1->"The module feeder. Lint in every seam.";case 2->"A gin stand. The saws are still.";case 3->"The lint cleaner.";case 4,5->"The press. It's taller than the door.";case 6->"An auger in its trough. A guard over it, mostly.";case 7->"The suction pipe. It hums even off.";default->null;};
            case BALE->switch(stage){case 1->"A bale with its tag. A name and a weight.";case 2->"A module under its tarp. It's as long as a trailer.";case 3->"The module's end, packed hard.";default->"A wrapped bale. It's warm.";};
            case SEED->switch(stage){case 1->"Cottonseed in a heap. You sink to the ankle.";case 2->"The auger in its trench.";case 3->"The ladder up the seed house.";case 4,5->"The sliding door. It's heavy on its rail.";case 6->"The lamp over the door.";case 7->"The chute.";default->"The bin's wall, galvanized, dented from inside.";};
            case TOWER->switch(stage){case 0,1->"A leg of the water tower. Bolts as big as your fist.";case 2->"The tower's ladder. It goes up a long way.";case 3->"The catwalk. You can see the whole town from here.";case 4->"The rail.";case 5->"The beacon.";case 6->"The riser.";default->null;};
            case TANK->switch(stage){case 1,2,3->"Lamesa, in letters taller than you.";case 4->"The tank's roof.";case 5->"The cap.";case 6->"The bowl under the tank.";default->"The tank. It rings when you knock on it.";};
            // 0.4.700: the school's field, Amber's house, the yards.
            case FIELD->switch(stage){case 0->"The goalpost's foot, in its pad. The pad is split along one seam.";case 1,2->"The goalpost. It goes up into the dark.";
                case 3->"The crossbar.";case 4,5->"An upright.";case 6->"A light tower. Its ladder starts above your reach.";default->"The lights. Off. Nobody plays tonight.";};
            case STANDS->switch(stage){case 0,1->"The stands. The aluminum still holds the day's heat.";case 2->"Under the stands: cups, a program, a drift of sunflower seed shells.";
                case 3->"The rail along the top of the stands.";case 4->"The team's bench. Strips of tape mark where each of them sits.";case 5->"The press box window. Dark inside.";
                case 6->"The announcer's table. The microphone is unplugged.";default->"The horn on the press box's roof.";};
            case LOCKER->switch(stage){case 0,1->"A locker. A name on tape across the vents, half peeled off.";case 2->"A bench, worn smooth.";case 3->"The coach's desk. A clipboard, a whistle on its cord.";
                case 4->"Last week's play is still on the board.";case 5->"A shower head. It drips.";case 6->"The concession stand's shutter, locked from inside.";
                default->"Hot dogs, chili pie, pickles, pop, and what each costs.";};
            case SCORE->switch(stage){case 0,1,2->"The scoreboard. Every bulb is dark.";case 3,4,5->"The down, the yards, the ball's spot. Dark.";case 6->"The scoreboard's post.";
                default->"The ticket booth. A roll of tickets on the counter inside.";};
            case AMBER->switch(stage){case 0->"The television's on with the sound down. The VCR's clock says twelve.";case 1->"Rented tapes, the video store's sticker on every one. Most of them are late.";
                case 2->"Tapes stacked on the carpet by the set.";case 3->"Her desk. A notebook open, written to the edges.";case 4->"The phone by her bed, its cord stretched to reach the pillow.";
                case 5->"Paperbacks. A knife or a mask on nearly every spine.";case 6->"Amber's window. Her desk lamp is on.";default->"The coffee table. The remote, two tapes out of their cases.";};
            case LOT->switch(stage){case 0->"A sprinkler, ticking its way across the grass.";case 1->"A doghouse. Something has chewed the corner of its door.";case 2->"A trash can, out for the morning.";
                case 3->"A bike left on its side in the grass.";case 4->"A lawn chair facing the street.";case 5->"A grill, cold, the grate still greasy.";case 6->"A hose wound on its reel.";
                default->"A kiddie pool with an inch of rainwater in it.";};
        };
    }
}
