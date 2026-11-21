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
    // ------------------------------------------------------------------------------------------------ 0.4.705: the slasher's body
    /** The body given: the sack over the head, and the town's hearts. */
    public static final String BODY="A feed sack over your head, the eyeholes cut crooked. Every heart in town is loud.";
    /** A locked door struck: the first blow, the second, and the third, when it gives. */
    public static final String[] STRIKES={"The door takes it. Inside, somebody turns the television down.","The frame splits along the lock.",
        "It gives. The door swings in on what is left of the frame."};
    /** Down, and up again where they fell. */
    public static final String DOWN="You go down and stay down.",UP="You get up where you fell.";
    /** Somebody in town has seen the face under the sack: "%s" is their name. */
    public static final String FACE="%s has seen your face.";
    /** Leaving the town by the House's door, the sack stays behind; coming back, it is where it was left. */
    public static final String SACK_LEFT="You leave the sack on the back step.",SACK_WORN="The sack is on the back step. You pull it on.",
        SACK_CARRIED="The sack is on the back step. You take it with you.";
    // ------------------------------------------------------------------------------------------------ 0.4.706: night one
    /** Coming into town while the party is on, a cue to that reader. */
    public static final String PARTY_HEARD="Out past the back fence, a radio. Headlights in the pasture.";
    /** What each of the five says at the stock tank to a reader: the first line, then the second, and the second again after that. Deek's
     * to the one he has picked are his own (ASK, RIDE). */
    private static final String[][] PARTY={
        {"You came. Get something out of the ice chest.","We're waiting on one more thing. Then we'll go."},
        {"That's my brother's tape. Don't touch the radio.","Deek's got a plan. Deek always has a plan."},
        {"Say something for the camera. No, something good.","The battery's at half. I'm saving it for later."},
        {"Don't lean on the truck. It's his dad's.","Somebody saw coyotes out here last week. Mel wants to film one."},
        {"Sit if you're staying. There are stickers in the blanket.","Ask Deek what we're doing after. He won't tell me."}};
    /** The nth line one of the five says at the party (Deek, Wes, Mel, Lesley, Shannon, by their number in TheListPeople). */
    public static String party(int who,int heard){
        int i=who==TheListPeople.DEEK?0:who==TheListPeople.WES?1:who==TheListPeople.MEL?2:who==TheListPeople.LESLEY?3:4;
        var lines=PARTY[i];return lines[Math.min(heard,lines.length-1)];
    }
    /** Deek to the one he has picked: the ask, said once when they come near; then, talked to again, the ride. */
    public static final String ASK="Wes's brother left a case of beer in the seed house at the gin. That door takes two. You and me?",
        RIDE="Get in the truck.";
    /** At the gin, the door rolled open: to the one who is going in, and to anybody else who tries. */
    public static final String GO_IN="It's in the back, past the auger. Go on. I've got the door.",
        ONE_AT_A_TIME="One at a time in there.";
    /** The door shut: what the five say outside it, in order (Deek, Wes, Mel, Lesley, Deek, Shannon, Wes). */
    public static final String[] SHUT_LINES={"Count to a hundred. Then knock.","Lights!","Hold still. I can't get anything.","Deek. Is that the auger?",
        "I didn't touch it.","Open it. Open it!","It's stuck. Deek, it's stuck."};
    /** Who says each of SHUT_LINES. */
    public static final int[] SHUT_SAYS={TheListPeople.DEEK,TheListPeople.WES,TheListPeople.MEL,TheListPeople.LESLEY,TheListPeople.DEEK,TheListPeople.SHANNON,TheListPeople.WES};
    /** After the cut: the one who went in, coming to; anybody else, where they stood. */
    public static final String CAME_TO="You come to under the water tower, face down in the caliche.",
        ALONE="Nobody is left. The trucks are gone. The seed house door is shut, the lamp over it out.";
    /** 0.4.707, the walk home: Amber's pages at her window, the first for the one who went in (the sack on, or off) or for anyone else,
     * and the second for everyone. What she says through the glass if knocked for. */
    public static final String PAGE_SACK="Amber holds a page against her window. In marker: YOU'RE WEARING A FEED SACK.",
        PAGE_FACE="Amber holds a page against her window. In marker: YOU LOOK LIKE YOU GOT DUG UP.",
        PAGE_WHAT="Amber holds a page against her window. In marker: WHAT HAPPENED OUT THERE?",
        PAGE_TOMORROW="She turns it over, writes, and holds it up again: TOMORROW. BACK DOOR. AFTER TEN.",
        AMBER_AFTER="Go home. And don't knock on the front tomorrow.";
    /** 0.4.707, first light: in bed, in the house, still out, and for anyone else in town. */
    public static final String LIGHT_BED="First light at the blinds. Your shoes are still on, and there is caliche on the sheet.",
        LIGHT_HOUSE="It gets light, and you are home, still on your feet. Down the street a sprinkler comes on.",
        LIGHT_OUT="It gets light, and you are not home yet. A screen door bangs somewhere down the street.",
        LIGHT_OTHER="It gets light over the gin. Sprinklers come on, and a dog starts up somewhere.";
    // ------------------------------------------------------------------------------------------------ 0.4.708: the hunt
    /** What each of the five says, by who they are (TheListPeople), as they start to run, when they yell for help, when they turn and
     * fight, and cornered a second time. */
    public static final String[] RUN={null,null,null,"No. We saw that door shut.","It's the sack. Deek, it's the sack!","Lesley! Get down off the top!",
        "Mel, run!",null,null,null,null,null};
    public static final String[] HELP={null,null,null,"Coach! Somebody get the coach!","Hold the door! Hold it!","Somebody call somebody!",
        "Up here! Help! Up here!",null,null,null,null,null};
    public static final String[] FIGHT={null,null,null,"Get off me!","It wasn't me! It was Deek's idea!","Back up. I'll swing it again.",
        "Don't. Don't!",null,null,null,null,null};
    public static final String[] COWER={null,null,null,"Okay. Okay. What do you want?","Please. Please.","Here. Take the camera. Take it.",
        "I wasn't even there. I was by the truck.",null,null,null,null,null};
    /** Shannon's are by the bench; Jenna lets her into the drive-in's stand. */
    static {RUN[TheListPeople.SHANNON]="I said open it! I told them!";HELP[TheListPeople.SHANNON]="Jenna! Jenna, open up!";
        FIGHT[TheListPeople.SHANNON]="I didn't touch it! I said open it!";COWER[TheListPeople.SHANNON]="I'm sorry. I'm sorry.";}
    public static final String JENNA_IN="Get in. Get in!",JENNA_DOWN="Get down. Stay down.";
    /** To the slasher, as each of them fights back and puts the body down. */
    public static final String[] FELLED={null,null,null,"Deek comes up off the floor into you. A shoulder, and you are down.",
        "Wes swings wild and catches you. You go down.","The camcorder catches you across the sack. You go down.",
        "A fistful of dirt through the sack's eyeholes. Something takes your knees.","Shannon shoves with both hands. The floor comes up.",
        null,null,null,null};
    /** A door shut and bolted behind one of them, to the slasher near it; to anyone's hand on it. */
    public static final String BOLTED_STEEL="Steel slams. A bolt shoots home.",BOLTED_DOOR="A door slams. The lock turns.",
        BOLTED="Bolted from the other side.";
    /** A bolted steel door struck, blow by blow: it takes five. */
    public static final String[] STEEL_STRIKES={"The steel door booms. It holds.","It booms again. The frame shifts in the block.",
        "A hinge pin starts out of its knuckle.","The bolt bends in its keeper.","The bolt tears loose, and the door swings in."};
    /** To anyone else in town, once for each name that yells: where it came from. */
    public static final String HELP_HEARD="Somewhere to the %s, %s is yelling for help.";
    public static final String[] COMPASS={"south","southwest","west","northwest","north","northeast","east","southeast"};
    // ------------------------------------------------------------------------------------------------ 0.4.709: the catch
    /** What each of the five says when the slasher has hold of them, and when they tear loose (Lesley says nothing; she goes). */
    public static final String[] CAUGHT=new String[TheListPeople.CAST],LOOSE=new String[TheListPeople.CAST];
    static {
        CAUGHT[TheListPeople.DEEK]="Everybody knows I came out here!";CAUGHT[TheListPeople.WES]="Deek! Deek, it's got me!";
        CAUGHT[TheListPeople.MEL]="There's tape of you. There's tape!";CAUGHT[TheListPeople.LESLEY]="My mom's waiting up. Please.";
        CAUGHT[TheListPeople.SHANNON]="Somebody! Please, somebody!";
        LOOSE[TheListPeople.DEEK]="Too slow, freak.";LOOSE[TheListPeople.WES]="I'm out, I'm out!";LOOSE[TheListPeople.MEL]="Run! Everybody run!";
        LOOSE[TheListPeople.SHANNON]="Jenna!";
    }
    /** To the slasher: a hand on one who sees it coming; hold of one; one tearing loose. */
    public static final String TWIST="%s twists out of your reach.",HOLD="You have hold of %s.",TORE="%s tears loose.";
    /** To the slasher, the hay hook: in, the knees, out. */
    public static final String HOOK_IN="The hook goes in under the collarbone.",HOOK_KNEES="%s's knees give.",
        HOOK_OUT="You tear it free, and %s folds over.";
    /** To anyone else in town, near enough to hear it: where the scream was cut off. */
    public static final String STOPS_SHORT="Somewhere to the %s, a scream stops short.";
    /** To anyone's hand on one of them after. */
    public static final String STILL_BODY="%s doesn't move.";
    // ------------------------------------------------------------------------------------------------ 0.4.710: the dead found
    /** What each of the five says on coming across one of the others dead (the dead one's name). */
    public static final String[] FOUND_BODY=new String[TheListPeople.CAST];
    static {
        FOUND_BODY[TheListPeople.DEEK]="That's not funny, %s. Get up.";FOUND_BODY[TheListPeople.WES]="%s? No. No, no, no.";
        FOUND_BODY[TheListPeople.MEL]="Is %s breathing? Is anybody checking?";FOUND_BODY[TheListPeople.LESLEY]="Don't look at %s. Don't look.";
        FOUND_BODY[TheListPeople.SHANNON]="That's %s. That's %s on the ground.";
    }
    /** To a reader who is not the slasher, coming across one of them dead. */
    public static final String FOUND_DEAD="%s lies face down, not moving.";
    // ------------------------------------------------------------------------------------------------ 0.4.711: the boards
    /** What each of the five says, forced down over a row of the stands. */
    public static final String[] OVER=new String[TheListPeople.CAST];
    static {
        OVER[TheListPeople.DEEK]="Wait, wait. I'll do whatever you want.";OVER[TheListPeople.WES]="I can't see anything!";
        OVER[TheListPeople.MEL]="The camera's still running.";OVER[TheListPeople.LESLEY]="I can see the ground. Let me up.";
        OVER[TheListPeople.SHANNON]="Jenna, I'm under the bleachers!";
    }
    /** To the slasher: forcing one of them over the row, and each of the three stamps. */
    public static final String FORCED_OVER="You force %s down over the row, head through the gap under the next one.";
    public static final String[] STAMPS={"You stamp on the plank across the back of the neck. The row rings.","Again. Something gives under your heel.",
        "Again. %s goes still."};
    /** One hung there dead, come across: by the rest of the five (Shannon's own line says where), and by a reader who is not the slasher. */
    public static final String[] FOUND_HUNG_BODY=FOUND_BODY.clone();
    static{FOUND_HUNG_BODY[TheListPeople.SHANNON]="That's %s. That's %s up on the stands.";}
    public static final String FOUND_HUNG="%s hangs over a row of the stands, not moving.";
    // ------------------------------------------------------------------------------------------------ 0.4.713: the line
    /** What each of the five says, forced onto a riser of the sprinkler line. */
    public static final String[] RISER=new String[TheListPeople.CAST];
    static {
        RISER[TheListPeople.DEEK]="Get it out. Get it out of me.";RISER[TheListPeople.WES]="It's in my back. It's in my back!";
        RISER[TheListPeople.MEL]="Don't turn it on. Please don't turn it on.";RISER[TheListPeople.LESLEY]="I can't feel my legs.";
        RISER[TheListPeople.SHANNON]="Jenna, call somebody. Call anybody.";
    }
    /** To the slasher: forcing one of them onto the riser; once after, where the valve is; and when the water has done it. */
    public static final String ONTO_RISER="You force %s back onto the riser, and its head comes up through the front.",
        VALVE_HINT="The line's valve stands past the end of the bench.",LINE_STILL="%s goes still. The sprinkler head goes on turning.";
    /** To anyone's hand on one on the riser. */
    public static final String PINNED="%s is fixed on the riser. It won't come out.";
    /** To whoever turns the valve. */
    public static final String VALVE_OPEN="The wheel turns. Water knocks down the line.",VALVE_SHUT="The wheel turns back. The heads run down and stop.";
    /** To everyone near, once, when the water reaches one on the riser. */
    public static final String WATER_THROUGH="The sprinkler line knocks with water. On the home sideline, %s screams.";
    /** One dead on the line, come across: by the rest of the five (Shannon's own line says where), and by a reader who is not the slasher. */
    public static final String[] FOUND_LINE_BODY=FOUND_BODY.clone();
    static{FOUND_LINE_BODY[TheListPeople.SHANNON]="That's %s. That's %s on the sprinkler.";}
    public static final String FOUND_LINE="%s lies across the sprinkler line, not moving.";
    // ------------------------------------------------------------------------------------------------ 0.4.714: the fryer
    /** What each of the five says, forced down over the fryer. */
    public static final String[] FRYER=new String[TheListPeople.CAST];
    static {
        FRYER[TheListPeople.DEEK]="Not the grease! Not the grease!";FRYER[TheListPeople.WES]="It's on! It's on, it's on!";
        FRYER[TheListPeople.MEL]="Please. My face. Not my face.";FRYER[TheListPeople.LESLEY]="Turn it off! Somebody turn it off!";
        FRYER[TheListPeople.SHANNON]="Jenna, help me! Jenna!";
    }
    /** To whoever turns the knob, and to anyone near when the oil comes up to heat. */
    public static final String FRYER_ON="The burners catch under the oil.",FRYER_OFF="The burners go out.",FRYER_HOT="The fryer's oil starts to smoke.";
    /** To the slasher: the oil not hot yet; forcing one of them into it; and when it has done it. */
    public static final String FRYER_NOT_HOT="The oil isn't hot yet.",INTO_FRYER="You force %s down over the fryer and push the face into the oil.",
        FRYER_STILL="%s goes still. The oil goes on spitting.";
    /** To anyone else's hand on one held in the oil. */
    public static final String FRYER_HELD="%s is held down in the oil.";
    /** Jenna, in the stand, as one of them goes in. */
    public static final String JENNA_FRYER="Let go of %s! Let go!";
    /** One dead in the fryer, come across: by the rest of the five (Shannon's own line says where), and by a reader who is not the slasher. */
    public static final String[] FOUND_FRYER_BODY=FOUND_BODY.clone();
    static{FOUND_FRYER_BODY[TheListPeople.SHANNON]="That's %s. That's %s in the fryer.";}
    public static final String FOUND_FRYER="%s is bent over the fryer, face down in the oil.";
    // ------------------------------------------------------------------------------------------------ 0.4.715: the car door
    /** What each of the five says, forced down at the car with the head in at its door. */
    public static final String[] DOOR=new String[TheListPeople.CAST];
    static {
        DOOR[TheListPeople.DEEK]="Not the door. Come on, not the door.";DOOR[TheListPeople.WES]="Get my head out! Get my head out of there!";
        DOOR[TheListPeople.MEL]="Please. You'll break my neck.";DOOR[TheListPeople.LESLEY]="Whose car is this? Somebody's coming back for it!";
        DOOR[TheListPeople.SHANNON]="Jenna! Jenna, I'm at the cars!";
    }
    /** To the slasher: forcing one of them down at the door, and each of the three slams. */
    public static final String INTO_DOOR="You force %s down on the knees at the car and push the head in at the open door.";
    public static final String[] SLAMS={"You slam the door. It stops on the head and swings back.","Again. Something gives against the frame.",
        "Again. %s goes still."};
    /** To anyone else's hand on one in the door, or on the door; and to anyone's hand on the door with one dead in it. */
    public static final String DOOR_HELD="%s's head is caught in the car door.",CAR_STOPPED="The door won't shut. %s is in the way.";
    /** To whoever opens or shuts the door, and to a hand anywhere else on the car. */
    public static final String CAR_OPENED="The door swings open. Nobody's in the car.",CAR_SHUT="The door slams shut.",
        CAR_LOOK="A car in its stall, its lights off. Nobody's in it.";
    /** One dead at the car, come across: by the rest of the five (Shannon's own line says where), and by a reader who is not the slasher. */
    public static final String[] FOUND_CAR_BODY=FOUND_BODY.clone();
    static{FOUND_CAR_BODY[TheListPeople.SHANNON]="That's %s. That's %s at the car.";}
    public static final String FOUND_CAR="%s kneels at a car in the drive-in, the door shut on the head.";
    // ------------------------------------------------------------------------------------------------ 0.4.716: the pumpjack
    /** What each of the five says, forced down beside a pumpjack's gearbox with the head under its weight. */
    public static final String[] JACK=new String[TheListPeople.CAST];
    static {
        JACK[TheListPeople.DEEK]="Not under that thing. Come on, not under that.";JACK[TheListPeople.WES]="It's still going! Turn it off! It's still going!";
        JACK[TheListPeople.MEL]="There's a brake. On the box. Somebody pull the brake.";JACK[TheListPeople.LESLEY]="My face is in the dirt! Get off me!";
        JACK[TheListPeople.SHANNON]="Jenna! Jenna, I'm out at the wells!";
    }
    /** To the slasher: forcing one of them down; the weight not yet clear, or hanging in the slot with the brake on; and the weight down. */
    public static final String INTO_JACK="You force %s down on the knees beside the gearbox and hold the head where the weight comes round.",
        JACK_WAIT="Not yet. The weight is coming round.",JACK_IN_SLOT="The weight hangs in the slot. There's no room under it.",
        JACK_STILL="The weight comes down on the head and stops there. %s goes still.";
    /** To anyone else's hand on one under the weight; to anyone's hand on the gearbox with one dead under it. */
    public static final String JACK_HELD="%s is held down by the gearbox, the head under the weight.",
        JACK_STALLED="The well has stopped on %s. The brake makes no difference now.";
    /** To whoever hauls the brake on or knocks it off. */
    public static final String BRAKE_ON="You haul the brake on. The jack slows and stops.",BRAKE_OFF="You knock the brake off. The jack starts up again.";
    /** One dead at a pumpjack, come across: by the rest of the five (Shannon's own line says where), and by a reader who is not the slasher. */
    public static final String[] FOUND_JACK_BODY=FOUND_BODY.clone();
    static{FOUND_JACK_BODY[TheListPeople.SHANNON]="That's %s. That's %s at the pumpjack.";}
    public static final String FOUND_JACK="%s kneels at a pumpjack, the head under its weight.";
    // ------------------------------------------------------------------------------------------------ 0.4.717: the stock tank
    /** What each of the five says, forced over the stock tank's rim with the head going under. */
    public static final String[] TANK=new String[TheListPeople.CAST];
    static {
        TANK[TheListPeople.DEEK]="Not the water. Come on. Not the water.";TANK[TheListPeople.WES]="I can't swim! You know I can't swim!";
        TANK[TheListPeople.MEL]="Let go of my hair! Let go!";TANK[TheListPeople.LESLEY]="It's in my mouth! It's in my mouth!";
        TANK[TheListPeople.SHANNON]="Jenna! Jenna, the tank! I'm at the tank!";
    }
    /** To the slasher: forcing one of them over the rim; the head come up; put back under; the last time; and still. */
    public static final String INTO_TANK="You force %s over the rim of the tank and push the head under the water.",
        TANK_UP="The head comes up out of the water, gasping.",TANK_AGAIN="You push the head back under.",
        TANK_LAST="You push the head back under. This time you hold it there.",
        TANK_STILL="The water stops moving round the head. %s goes still.";
    /** To anyone else's hand on one held under, or on the wall there; to anyone's hand on the wall where one is drowned. */
    public static final String TANK_HELD="%s is held over the rim of the tank, the head under the water.",
        TANK_STILLWATER="%s hangs over the rim, the head under the water. Nothing moves in it.";
    /** One drowned at the tank, come across: by the rest of the five (Shannon's own line says where), and by a reader who is not the slasher. */
    public static final String[] FOUND_TANK_BODY=FOUND_BODY.clone();
    static{FOUND_TANK_BODY[TheListPeople.SHANNON]="That's %s. That's %s in the tank.";}
    public static final String FOUND_TANK="%s hangs over the rim of the stock tank, the head under the water.";

    /** 0.4.718, the gin's auger: what each of them shouts as the screw takes the legs; what the slasher is told; a hand on one in it;
     * the screw jammed on them; the switch; the gin before the fourth night. */
    public static final String[] AUGER=new String[TheListPeople.CAST];
    static{
        AUGER[TheListPeople.DEEK]="My leg! It's got my leg!";AUGER[TheListPeople.WES]="Turn it off! Somebody turn it off!";
        AUGER[TheListPeople.MEL]="The switch! By the big door, the switch!";AUGER[TheListPeople.LESLEY]="Pull me out! Grab my arms and pull!";
        AUGER[TheListPeople.SHANNON]="Jenna! Jenna, I'm in the gin!";
    }
    public static final String INTO_AUGER="You force %s down into the auger's trough, feet first, and let go. The screw has them.",
        AUGER_STILL="The screw jams on what it has taken and the gin stops. %s goes still.",
        GIN_NOT_TONIGHT="The augers aren't turning tonight. The screw won't take anyone.";
    public static final String AUGER_HELD="%s is down in the auger's trough, the screw turning on the legs.",
        AUGER_STILL_BODY="%s is down in the trough to the waist. The screw doesn't turn.";
    public static final String GIN_OFF="You throw the switch. The augers run down and stop.",GIN_ON="You throw the switch. The gin shudders and the augers turn.",
        GIN_JAMMED="You throw the switch. Nothing turns; the screw is jammed.",GIN_DARK="The switch clicks. Nothing in the gin moves tonight.";
    public static final String[] FOUND_AUGER_BODY=FOUND_BODY.clone();
    static{FOUND_AUGER_BODY[TheListPeople.SHANNON]="That's %s. That's %s in the gin.";}
    public static final String FOUND_AUGER="%s is down in the auger's trough to the waist.";

    /** 0.4.719, the gin's press: what each of them shouts in its chamber; what the slasher is told; the release; one crushed there. */
    public static final String[] PRESS=new String[TheListPeople.CAST];
    static{
        PRESS[TheListPeople.DEEK]="Get me out of here! Get me out!";PRESS[TheListPeople.WES]="It's coming down! It's coming down on me!";
        PRESS[TheListPeople.MEL]="There's a release on the side! Somebody pull it!";PRESS[TheListPeople.LESLEY]="I can't stand up in here! I can't stand up!";
        PRESS[TheListPeople.SHANNON]="Jenna! Jenna, I'm in the press!";
    }
    public static final String INTO_PRESS="You shove %s into the press's chamber and drop the latch. The ram starts down.",
        PRESS_HELD="%s is in the press's chamber, the ram coming down.",
        PRESS_STILL="The ram comes all the way down. %s goes still.",
        PRESS_NOT_TONIGHT="The press is shut down tonight. Its chamber won't open.";
    public static final String PRESS_RELEASE="You haul the release. The ram stops and goes back up, and %s comes out of the chamber.",
        PRESS_STILL_BODY="%s is folded in the press's chamber under the ram.";
    public static final String[] FOUND_PRESS_BODY=FOUND_BODY.clone();
    static{FOUND_PRESS_BODY[TheListPeople.SHANNON]="That's %s. That's %s in the press.";}
    public static final String FOUND_PRESS="%s is folded in the press's chamber, under the ram.";
    // ------------------------------------------------------------------------------------------------ 0.4.702: the town's people
    /** Who each of them is, as their captions name them; the deputy and the reader's parents go unnamed. */
    public static final String[] NAMES={"Amber","Jenna","Janice","Deek","Wes","Mel","Lesley","Shannon","Stace","The deputy",null,null};
    /** What each says to a reader: the first line, then the second, and the second again after that. */
    private static final String[][] SAID={
        {"It's one in the morning. Mom's asleep across the hall.","Go home. I'll see you in first period."},
        {"We're closed. The fryer's off. I'm here till this drawer comes out right.","Twelve dollars short. It's been twelve every night this week."},
        {"The slot's for tapes. Somebody keeps putting notes in it.","If you want a horror tape, the wall's picked over. Everybody's renting the same three."},
        {"Field's ours once the lights go off. Coach doesn't know.","Sit if you're sitting. You're blocking my view of nothing."},
        {"Don't tell anybody we were out here. Deek's dad would kill him.","We're waiting on a ride that isn't coming. You can wait too."},
        {"From up here you can see the water tower. That's all there is to see.","Lesley keeps seeing somebody on the field. It's a dog. I told her."},
        {"Don't sit by Mel. She talks the whole time.","Somebody keeps crossing the field down there. Back and forth. That's not you, is it?"},
        {"The bus doesn't come this late. I know. I'm not waiting for the bus.","If my brother drives by, you didn't see me."},
        {"Careful on the ladder. It shakes enough with one up here.","I come up here to see who's still out. Tonight it's you."},
        {"Little late for a walk.","I'm not calling your folks. Go on home."}};
    /** The nth line one of them says to a reader. */
    public static String said(int who,int heard){var lines=SAID[who];return lines[Math.min(heard,lines.length-1)];}
    /** A hand on the reader's parents, asleep: a cue, for they say nothing. */
    public static final String MOTHER="Your mother, asleep on her back. She breathes slowly through her mouth.",
        FATHER="Your father, asleep. His watch is still on.",
        PARENTS_BED="The edge of your parents' bed. The mattress dips toward the middle.",
        // A knuckle on Amber's window, and on the drive-in's order window.
        KNOCK_AMBER="You tap on Amber's window with a knuckle.",
        KNOCK_ORDER="You knock on the order window.";
    /** 0.4.701: the pasture's wire gap, opened and shut by hand; the two machines. */
    public static final String GAP_OPEN="You lift the loop off the post and lay the gap back on the grass.",
        GAP_SHUT="You stretch the gap across and drop the loop back over the post.",
        // 0.4.701: a yard's gate, by hand.
        GATE_OPEN="You lift the latch and swing the gate open. Its hinge complains.",
        GATE_SHUT="You swing the gate to. The latch drops into its fork.",
        WINDMILL_HEAD="The windmill's head, up on its tower. The wheel goes round in front of it.",
        JACK_BEARING="The beam's bearing on top of the post. The horsehead rises and falls over the well.";

    private static List<Component> pages(String... texts){var out=new ArrayList<Component>();for(String text:texts)for(String p:LookoutTexts.pages(text,150))out.add(HouseWriting.page(HouseWriting.WritingStyle.PLAIN,p));return out;}
    /** The first paper: the pad by the kitchen telephone. */
    public static ItemStack source(){
        return HouseWriting.book("The pad by the phone","Mom",pages(
            "Plate in the oven. Wrap it up if you don't want it.",
            "Amber called. Twice. I said you were out.",
            "The back door sticks. Lift it when you shut it or it won't latch.",
            "Lights off when you come in. All of them."));
    }
    /** 0.4.721: the account, on the tape deck in the reader's room once the fourth night is over: the town; for the one who went in,
     * the four nights, who of the five they finished and the rule in the notebook; for anyone else, that somebody else had the sack. */
    public static ItemStack account(String name,CompoundTag own){
        var parts=new ArrayList<String>();
        parts.add("The back door let me into my own kitchen, in Lamesa, at night. Out front the street was caliche and the houses across it were dark. "
            +"Main Street was lit for nobody, and past the highway the gin stood by the tracks under the water tower with the town's name on it.");
        if(own.getBoolean("WentIn")){
            parts.add("Four nights running it was ten o'clock when I came in. The first night Deek took me to the gin and shut me in the seed house, and I came to under the water tower with a sack over my head.");
            int kills=own.getInt("Kills");var names=new ArrayList<String>();
            for(int who:TheListBody.LISTED)if((kills&1<<who)!=0)names.add(NAMES[who]);
            if(names.isEmpty())parts.add("I had the hook in my hand. At the end every name on the list was still walking around town.");
            else if(names.size()==TheListBody.LISTED.length)parts.add("There were five names on the list. By the last morning I had been to every one of them.");
            else parts.add("There were five names on the list. By the last morning I had been to "+join(names)+".");
            if((own.getInt("Rules")&1)!=0)parts.add("Amber wrote one rule in her notebook from the field, in her own hand: he can't run, and doesn't need to.");
        }else parts.add("Somebody else had the sack and the hook. I was in town when it got light the last time.");
        parts.add("The list was five names long.");
        return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.THE_LIST),name,pages(parts.toArray(String[]::new)));
    }
    private static String join(List<String> names){
        if(names.size()==1)return names.get(0);
        return String.join(", ",names.subList(0,names.size()-1))+" and "+names.get(names.size()-1);
    }

    /** What a hand finds on a fitting of this kind at this stage; null if there is nothing to say. */
    public static @Nullable String thing(Kind kind,int stage){
        return switch(kind){
            case COTTON->switch(stage){case 1->"A plant that never got its height. The bolls on it are open anyway.";case 2->"It leans out over the end of the row, heavy.";case 3->"Picked. Stalks and husks, and a few tufts caught in them.";default->"Cotton, open. It comes away in your fingers, and there's a seed in it.";};
            case MESQUITE->switch(stage){case 0,1->"Mesquite. The bark is ridged and the thorns are long.";case 4->"A low branch. There's a thorn in it as long as a finger.";default->"Small leaves, a lot of them. The branches are mostly thorn.";};
            case WEED->switch(stage){case 0->"A tumbleweed, caught here till the wind changes.";case 1->"Broomweed, dry, gone to seed.";case 2->"A tuft of grass, dead at the top.";case 3->"Goatheads. One goes straight into your thumb.";case 4->"Prickly pear. You keep your hand off it.";default->null;};
            case POLE->switch(stage){case 5->"The street light hums. Bugs go round it.";case 6->"A transformer on the pole. It ticks.";default->"A pole, creosote and splinters. Staples in it where flyers were.";};
            case FENCE->stage==1?"A fence post, set in a lump of concrete.":"Chain-link. It gives a little and rings along its length.";
            case SIGNPOST->switch(stage){case 0->"South Ninth and Avenue K.";case 1->"Main Street and Avenue K.";case 2->"South Seventh and Avenue K.";case 3->"A stop sign. Somebody has shot at it.";case 4->"The bus stops here. The schedule on it has been torn off.";case 5->"The crossbuck at the tracks. The paint is coming off it in flakes.";default->null;};
            case TRACK->"The rail is still warm from the day.";
            case MAILBOX->"The flag's down. There's nothing in it.";
            case WINDOW->switch(stage){case 1->"Lit. Somebody's moving around in there.";case 2->"The kitchen window. The curtain is half drawn.";case 3->"A screen, with a tear at one corner patched with tape.";case 4,5->"The picture window. The street's dark in it.";default->"Dark. The curtain is drawn.";};
            case KITCHEN->switch(stage){case 1->"The sink. A glass upside down on the drainboard.";case 2->"The range. One burner has a pan on it, cold.";case 3->"The refrigerator hums. The door's covered in magnets and a schedule.";case 4->"The freezer's top half. Ice trays and a carton of something.";case 5->"The cabinet. Plates, glasses, the good ones at the back.";case 6->"The telephone. The cord is long enough to reach the hall.";case 7->"The canisters, Flour, Sugar, Coffee, and the toaster with its crumbs.";default->"The counter. A drawer of string and batteries and rubber bands.";};
            case TABLE->stage==1?"A dinette chair. The vinyl is split on the seat.":"The table. Someone's left the salt out.";
            case BED->stage==1?"The foot of the bed. A blanket kicked down.":"Your pillow. It still has the shape of your head in it.";
            case ROOM->switch(stage){case 1->"Your desk. The tape deck has a tape in it, rewound.";case 2->"The clock radio. The numbers glow.";case 3->"The closet. Clothes on hangers, shoes in a heap.";case 4->"A low shelf. Paperbacks with broken spines.";default->"A dresser. The top drawer won't shut all the way.";};
            case POSTER->switch(stage){case 6->"A flyer. CURFEW 9 PM, and a drawing of the face in the sack. A phone number under it.";case 0->"A band. You know every song on the tape.";case 1->"A film poster, a hooded figure. Tacked up at the corners.";case 2->"The feed store's calendar, still on last month.";case 3->"A pennant from the high school.";case 4->"A picture in a frame. You don't look at it long.";case 5->"Mountains. Somewhere a long way from here.";default->null;};
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
            // 0.4.701, the pasture.
            case PASTURE->switch(stage){case 0->"A cedar post, the wire stapled to it. The staples have rusted into the wood.";
                case 1->"Barbed wire, four strands. A tuft of hair is caught on a barb.";case 4->"A cattle guard. Pipes over a pit, far enough apart for a hoof to go through.";
                case 5->"The corner post, braced against the pull of the wire.";case 6->"A mineral block in a tub, licked into a bowl.";
                default->"A hay ring. What is left of the bale inside is trodden flat.";};
            case STOCK->switch(stage){case 3->"The windmill's pipe comes over the rim. A little runs from it with each stroke.";
                case 4->"The pipe from the windmill, along the ground to the tank.";default->"The stock tank, full nearly to the rim. Moths float on the water.";};
            case WINDMILL->switch(stage){case 0->"The windmill's pump. The rod rises and drops in it.";case 6->"A footing, the leg bolted to it. The nuts are rusted round.";
                case 7->"The tail, holding the wheel into the wind.";default->"The pump rod, up the middle of the tower to the head.";};
            case PUMPJACK->switch(stage){case 0->"The wellhead. It smells of crude, and the polished rod slides in and out of it.";
                case 1->"The Samson post. It shakes with every stroke.";default->"The gearbox, warm, oil weeping from its seal. Behind it the motor hums.";};
            // 0.4.706, night one.
            case PARTY->switch(stage){case 0->"A radio with a tape in it, turned up as far as it goes.";case 1->"An ice chest. Ice water and two cans floating in it.";
                case 2->"Empty cans, most of them stepped on.";case 3->"A camp lantern, hissing. Bugs knock against the glass.";
                case 4->"A blanket spread on the grass. Sandburs in the weave.";case 5->"A cardboard case, torn open. Empty.";
                case 6->"The lamp over the door. Out.";default->"The bulb. Out.";};
            // 0.4.710: blood where it landed.
            case BLOOD->switch(stage){case 1->"Blood, thrown out across the ground in a spray.";case 2->"Blood, dragged into a smear.";
                case 3->"A pool of blood, not dry yet.";default->"Drops of blood. Still wet.";};
            case BATTERY->switch(stage){case 2->"The separator. A gauge on it, the needle steady.";case 4->"The lease's sign. Somebody has shot a hole through one corner.";
                case 5,6,7->"A plate with the well's number on it.";default->"A tank on the lease, a hatch on its roof and a ladder up its side.";};
            // 0.4.713, the line down the home sideline.
            case LINE->switch(stage){case 1->"The end of the line, capped. Mud dried round the cap.";
                case 2->"A riser to the knee, a sprinkler head on top. Its arm rests against the nozzle.";
                case 3->"A riser. Something has run down it into the grass, and the head is clogged with it.";
                case 4->"The line turns here into a hose.";case 5->"A hose, flat in the grass.";
                case 6->"The valve. A wheel on top to open it.";case 7->"The valve, open. The line knocks with water.";
                default->"Aluminum pipe laid down the sideline, length to length.";};
            // 0.4.714, the fryer in the drive-in's stand.
            case FRYER->switch(stage){case 1->"The fryer's on. The oil is warming.";case 2->"The fryer. The oil shimmers and smokes.";
                case 3->"The fryer, off. The oil has gone dark.";case 4->"The fryer's on. The dark oil is warming.";
                case 5->"The fryer. The dark oil spits and smokes.";default->"The fryer, off. Two baskets hang over cold oil.";};
        };
    }

    // ------------------------------------------------------------------------------------------------ 0.4.720, night two: the den
    public static final String HOME2="Ten o'clock. Amber's page said her back door, after ten.",
        BACK_DOOR_OPENS="Amber has the back door open before your knuckle lands.",
        DEN_OTHER="Shut the door behind you. My mom's asleep.",
        ON_THE_WAY="South end zone. Keep up. Or don't, that's the test.",
        AT_THE_END="Stand next to me. I'm not counting till you're here.",
        RAN_KEPT="That's not fair. You walked the whole way. I ran.",
        RAN_LOST="Huh. Maybe you're just slow. Again.",
        NEXT_DOOR="Next one's the field house. It has a lock on it.",
        NOTEBOOK_GIVEN="Amber hands you a spiral notebook. The first page says THE RULES.",
        NOTEBOOK_TITLE="Amber's notebook",
        RULE_WRITTEN="Amber writes in the notebook against her knee and hands it back.",
        RULES_TAPES="THE RULES\n(from the tapes)\n\n- Only at night.\n- Light keeps him back.\n- He can't come in unless you ask him in. (That's vampires. Writing it down anyway.)",
        RULES_TESTED="TESTED";
    /** Amber in her den, in turn, to the one who went in; the third hands the notebook over, and after the last she sets off. */
    public static final String[] DEN={
        "You came to the back. Good. Keep it down, my mom's asleep.",
        "I watched three of these last night to get ready. If you're what you say you are, there are rules. There are always rules. That's the whole point.",
        "Here. I wrote down the ones from the tapes already. We test the rest.",
        "First one. The football field. I run, you walk. If I get away from you, you're just somebody in a sack.",
        "South end zone. Come on."};
    /** Her count at the end zone; on the last she runs. */
    public static final String[] COUNT={"On three.","One.","Two.","Three!"};
    /** The rules the tests write, in the notebook's own words. */
    public static final String[] RULES={"1. He can't run. He doesn't need to. You don't get away from him. (The field.)",
        "2. Locks don't stop him. Steel doesn't either. Five hits. (The field house.)",
        "3. He hears your heart through a wall, and he knows it's yours. (The field house again.)",
        "3. He hears hearts through a wall, but he can't tell whose. He has to guess. (The field house again.)",
        "4. If he falls he stays down a while. Five seconds off the tower. Higher, longer. (The water tower.)"};
    /** 0.4.722, the rest of night two's tests. Amber's lines over her head; the cues to the one who went in. */
    public static final String DOOR_ASK="Field house. I'm going in and locking it. You get in.",
        DOOR_OUT="Out first. You have to be on the other side of it.",
        DOOR_BOLTED="The field house doors bang shut. A bolt goes across on the inside.",
        DOOR_DONE="Five. I counted. That's a steel door.",
        HEART_ASK="Now go back out. Stay out till I bang on something.",
        HEART_OUT="Out. Both feet.",
        HEART_KNOCK="Inside the field house a locker door bangs once.",
        HEART_RIGHT="How'd you know which one? There's a wall.",
        HEART_WRONG="Wrong room. Ha. So you can't tell.",
        HEART_TALK="I'm not telling you where I am. That's the whole test.",
        TOWER_ASK="Last one. The water tower. You go up and you come off it.",
        TOWER_UP="All the way up. Now off. Not the ladder.",
        TOWER_LADDER="That's the ladder. Off the side.",
        TOWER_DONE="Five seconds and you got up. Higher would be longer, right? Has to be.",
        TOWER_TALK="Up the ladder, off the catwalk. I'll watch from here.",
        TESTS_DONE="That's all of them. That's what we know.",
        FELL="The ground comes up all at once.";

    /** 0.4.721: the first name out, said by Amber over her head; the cue to the one who went in, and to anyone else in town. */
    public static final String FIRST_NAME="That's the first one. They're out there now. Go on.",
        FIRST_NAME_CUE="Across town a porch light goes off.",
        HUNT_OTHER="Somewhere a screen door bangs, and somebody starts running.",
        // 0.4.721: ten o'clock again on nights three and four, in the kitchen.
        NIGHT3="Ten o'clock. The hay hook is on the kitchen table where you didn't leave it.",
        // 0.4.723: what comes back, and what the town knows.
        HOOK_BACK="The hay hook is on your bed.",
        FLYERS="There's a flyer taped to the kitchen window. CURFEW 9 PM, and under it somebody's drawing of a face in a sack.",
        // 0.4.724: the deputy, out on his round.
        DEPUTY_HEY="Hey. You. Stay right where you are.",
        DEPUTY_SEEN="A flashlight finds you from down the street.",
        DEPUTY_SHOT="Something hits you in the chest, and then you hear it.",
        DEPUTY_FIRED="Stay down.",
        DEPUTY_CURFEW="There's a curfew. Go home and stay there.",
        NIGHT4="Ten o'clock. Over the highway the gin is running. You can hear it from the kitchen.",
        // 0.4.721: first light after the fourth night: the one who went in, and anyone else.
        END_WENT_IN="It gets light over the gin, and the hands are your own again. The tape deck in your room is running.",
        END_OTHER="It gets light over the gin. Nobody is out on the streets yet.";
}
