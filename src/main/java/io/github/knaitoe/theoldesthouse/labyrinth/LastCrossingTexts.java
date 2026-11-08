package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import io.github.knaitoe.theoldesthouse.labyrinth.CrossingBlock.Kind;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.351, The Last Crossing: the work slate on the keeper's office wall (the source), what each of the station's fittings looks like to a
 * hand on it, and what Vera says. House writing: never the reader's gender, never a colour, never an age.
 */
public final class LastCrossingTexts {
    private LastCrossingTexts(){}
    public static final String SLATE_TITLE="The work slate";
    public static final String HINT="Sign the roll on the porch. Work the station with the others before dark.";
    private static List<Component> pages(HouseWriting.WritingStyle style,String text){var out=new ArrayList<Component>();for(String p:LookoutTexts.pages(text,150))out.add(HouseWriting.page(style,p));return out;}

    /** The work slate copied as it is chalked: who worked where on the days before, and the roll for the night of the last crossing. */
    public static final String SLATE="THE WORK SLATE, as it is chalked.\n\n"
        +"Days across the top, the station's jobs down the side, initials in the squares.\n\n"
        +"INTAKE: E, E, H, E\nGAUGE: V, V, V, V\nCABLE: H, P, P, H\nRAMP: P, H, E, E\n\n"
        +"Under the last day, in bigger letters: CROSSING. And the roll for it: V here. H here. P here. E here.\n\n"
        +"Low in the corner, smaller: wheel held. pin at the second knock.\n\nNobody has wiped it since.";
    public static ItemStack source(){return HouseWriting.book(SLATE_TITLE,"Rook Ferry",pages(HouseWriting.WritingStyle.WILL,SLATE));}

    /** What a hand on one of the station's fittings finds, when nothing else is happening there. */
    public static String look(Kind kind,int stage){
        return switch(kind){
            case TABLE->"The porch table.";
            case CLIPBOARD->"Vera's clipboard, with today's roll on it.";
            case SLATE->"The work slate. Nobody has wiped it.";
            case PLATE,PLATE_SET->"The handprint plate: thick glass in a brass frame, a palm etched on this side.";
            case BRACKET->"An empty brass bracket, the size of the handprint plate.";
            case STOVE->"The office stove, the kettle on it.";
            case GAUGE->stage==3?"The pressure gauge. The needle's settled.":"The pressure gauge. The needle's lying in a band.";
            case BRAKE->"The brake lever.";
            case VALVE->stage==3?"A red bypass wheel. It won't turn.":"Valve "+(stage+1)+".";
            case RUN->"A length of the intake pipe.";
            case BLEED->stage==1?"The bleed screw, backed off. Water spits from it.":"The bleed screw on the intake pipe.";
            case GRATE->stage==2?"A hand, up through the broken corner of the grate.":"A service grate. One corner is broken off; below it, black water.";
            case PUMP->stage==1?"The pump, running.":"The pump, idle.";
            case PIPE->"A speaking pipe. A knock on it carries to the other end.";
            case ROLLER->stage==1?"A cable roller, slewed off its cradle.":"A cable roller, the cable over it.";
            case FAIRLEAD->stage==0?"The fairlead. The cable's jumped it and kinked against the housing.":"The fairlead, the cable running straight out through it.";
            case DRUM->"The winch drum, the cable wound on it.";
            case HATCH->"The inspection hatch.";
            case WHEEL->stage==2?"The sluice wheel, pinned.":"The sluice wheel.";
            case PIN->stage==1?"The locking pin, seated.":"The locking pin, up in its sleeve.";
            case COIL->stage==3?"The line's snapped. A frayed end trails off the cleat.":"The ferry's mooring line.";
            case HOOK->stage==1?"The station lamp, on its hook.":"An empty lamp hook.";
            case DEPTH->"The depth post, marked off in feet. The water's at the second mark.";
            case NAMEPLATE->stage>=2?"Bolt holes in the wheelhouse front where the nameplate was.":"The ferry's nameplate, cast in brass: CORDELIA.";
            case BAR->stage==1?"The bar's across the door.":"A heavy bar, leaning by the door. There are brackets either side of the frame to take it.";
            case ISOLATOR->stage==1?"The isolation valve, shut. The upper floor's on its own pump now.":"The isolation valve for the lower floor. Open.";
            case BYPASS->stage==1?"A bypass wheel on its riser, turned open. The tin tag wired to it is turned to match.":"A bypass wheel on its riser, shut. A tin tag is wired to it.";
            case BRANCH->stage==1?"A grate into a service branch. Water's running behind it; the bars are wet.":stage==2?"A grate into a service branch. The bars are damp, beaded with water.":"A grate into a service branch, shut. Dry behind it.";
            case FLOOR_GRATE->stage==3?"A grate in the floor, a pin driven through its bars into the frame. Wet under it.":stage==1?"A grate in the floor. Water's standing right under it, and the bars are wet.":stage==2?"A grate in the floor, its bars damp.":"A grate in the floor. Dark and dry under it.";
            case WITNESS->stage==1?"The witness plate on the cable bed, scraped clean: a mark cut square across it.":"The witness plate on the cable bed, greased over.";
            case CATCH->stage==1?"The parking catch, down in the drum's teeth.":"The winch's parking catch, lifted clear of the drum.";
            case STAIN->stage==1?"A stain on the boards under the fairlead, drying.":"A dark wet stain on the boards under the fairlead.";
            case TOOLS->"The cable tools, rolled in canvas.";
            case PRINTS->stage==1?"Prints on the boards, dried to a tidemark. Somebody came up out of the water here.":"Wet prints on the boards. Somebody came up out of the water here.";
            case UNDERSIDE->stage==1?"The fairlead's throat from underneath, the cable running clear through it.":"The fairlead's throat from underneath, the cable kinked hard into it.";
            case FEED->stage==1?"A section of the live feed, a chalk arrow beside its tag.":"A section of the live feed, a tin tag wired to it: FEED.";
            case GALLERY_DOOR->stage==1?DOOR_OPEN_LOOK:DOOR_RUSTED;
            case FEED_GATE->stage==1?GATE_STAGED_LOOK:stage==2?"The feed gate, barred across.":stage==3?"The feed gate, drawn open.":GATE_LOOK;
            case DEBRIS->DEBRIS_LOOK;
            case CUTOFF->stage==1?"The upstream cutoff, wound shut.":CUTOFF_LOOK;
            case SOURCE->"Something at the bottom of the black water, wrapped in weed.";
            case MASTER->stage==1?"The master bypass, open.":MASTER_LOOK;
            case RELEASE->stage==1?"The launch rope's bollard, the rope slipped.":"The launch rope, made fast round its bollard.";
            case DOWNSTREAM->stage==1?"The downstream sluice, wound shut.":DOWNSTREAM_LOOK;
            case RACK->stage==1?"The launch pin, on its rack.":stage==2?"A locking bar for a sluice gate, on its hooks.":"An empty rack on the wall.";
        };
    }

    // ------------------------------------------------------------------------------------------------ Vera
    public static final String GREET="You'll be the crew, then. Sign the roll and I'll put the kettle on.";
    public static final String ROLL="Right. Roll.";
    public static final String ELLIS="Ellis.";
    public static final String AFTER_ROLL="Before you start. Come and look at this.";
    public static final String PLATE_SHOWN="A clean hand leaves a breath of fog on this side. Like that. If a second print ever comes up on the far side, closing round your hand, you say so. Anyone can try it.";
    public static final String KETTLE="Kettle's on. Intake first.";
    public static final String JOBS="Intake first: somebody below on the bleed screw, somebody up on the gauge to call the valve. Then the cable house; there's a roller off and something wrong at the fairlead. And that line on the ramp wants bringing in.";
    public static final String JOBS_FIVE="Somebody can carry the lamp between the intake and the gauge room, so you know the way back in the dark.";
    public static final String JOBS_SIX="And somebody walk the spillway with the lookout and try the bell.";
    public static final String ELLIS_ANSWER="Ellis works the ferry. Get the intake going first.";
    public static final String LOOK_ANSWER="Get the intake going first. Then we'll see.";
    public static final String JAM_ANSWER="Then that's tomorrow's job, and a pull to free it.";
    public static final String LINE_THANKS="Good. That would've gone with the first rise.";
    public static final String REHEARSE="Let's try it. One of you down on the wheel, one up here on the pin. Two knocks, and the pin goes in.";
    public static final String REHEARSED="That's it. Same again in the dark.";
    public static final String ROUTE="Now walk it once, all of it, before the light goes. Office, pump room, the intake below it, the cable house, the ramp.";
    public static final String DUSK="That's the light going. Get to your places.";
    public static final String DUSK_CALL="It's on %s! Turn it!";
    public static final String BACK="There you are.";

    // ------------------------------------------------------------------------------------------------ Night One (0.4.352)
    public static final String SURGE_CALL="Below! Somebody get down to the intake!";
    public static final String GRIPPED_CALL="It's got hold of someone! Get them up!";
    public static final String UP_HERE="Get them up here, into the light.";
    public static final String FLOODED_CALL="We've lost the lower floor. Shut the isolation valve by the landing!";
    public static final String ISOLATED="That'll hold us. Stay up here.";
    public static final String NIGHT_ANSWER="Keep the upper landing. Keep each other where you can see them. It'll get light.";
    public static final String TOLD_ANSWER="Then you know what the plate will show. Show them or don't, that's yours. Just don't go near the water on your own.";
    public static final String DAWN="That's the light. Come in and get warm.";
    /** What only the one it took hold of hears and knows. */
    public static final String CLAWED="Something comes up through the broken corner and a claw sinks into you. It holds.";
    public static final String INFECTED_ONE="You take one breath, and under it another answers.";
    public static final String INFECTED_TWO="You'll change at the next dusk. You know it the way you know your own hands.";
    public static final List<String> SYMPTOMS=List.of("Your breath comes twice.","Under your own breathing, another breath, slower.","The water in the channel sounds almost like talk.");
    public static final String DAWN_CARRIER="The light comes up. You're still yourself, for today.";

    // ------------------------------------------------------------------------------------------------ Day Two (0.4.353)
    public static final String SLATE_FIRST="Slate first.";
    public static final String SLATE_SEEN="Ellis is still up there. Marked present, the night of the last crossing. I never rubbed it out.";
    public static final String FAR_END="Cable tools are on the desk. The drum wants lining up on the witness marks at the far end before tonight's pull. Who's taking the far end?";
    public static final String DAY2_JOBS="The drum wants lining up on the witness marks before dark. One of you at the far end with the tools, reading the marks; one on the brake by the drum. Ease it out, crank it in, and when the marks meet, the catch goes in. Somebody keep an eye on the spillway while they do it. And whoever's on the gauge, watch it.";
    public static final String ADMIT_ASK="Did anybody ever answer for Ellis?";
    public static final String VERA_ADMITS="Once. The night the ferry went over, I called the roll down the intake pipe, like a fool. Something answered. I took my ear off the pipe and I didn't put it back.";
    public static final String LINED="That's it. Leave it there.";
    public static final String BRAKE_SET="Leave the brake set for the first pull. Whatever it's lined up on at dark, that's what we pull on.";
    public static final String ELLIS_VOICE="You'll want to know what it sounded like. It was Ellis's voice. Coming up the pipe a breath late, like it had been listening first.";
    public static final String BACK_DAY2="There you are. The drum's still to line up.";

    // ------------------------------------------------------------------------------------------------ Night Two (0.4.354)
    public static final String NIGHT_ROLL="Roll, before it's dark. Answer up.";
    public static final String ROLL_ABSENT="Nothing. Somebody find them, and don't go looking on your own.";
    public static final String ROLL_ALL="All here. Inside, then.";
    public static final String PULL_LINED="Hands on the brake. Take it off when you're ready and hold it. We pull on the marks.";
    public static final String PULL_OFF="Hands on the brake. She's not lined up, so it'll be a long haul. Take it off and hold it.";
    public static final String PULL_HOLD="That's it. Hold it. Hold it.";
    public static final String BINDING_JAM="She's binding at the fairlead! Somebody clear it!";
    public static final String BINDING_ROLLER="She's riding up on the loose roller! Seat it!";
    public static final String RUNNING_BACK="Brake! Somebody get on the brake!";
    public static final String CAUGHT="Got her. Hold it now.";
    public static final String PULLED="That's her taken up. Drop the catch in. She'll lie on that till morning.";
    public static final String PULL_FAILED="She's thrown the rollers. Leave it. We'll reset them from the landing in the light.";
    public static final String NIGHT_ANSWER2="Hold the winch through the pull. Watch the grate behind the brake. Keep the plate by one way in. It'll get light.";
    public static final String DAWN_TWO="That's the light. Who's here? Come in where I can see you.";
    /** What only the one who turns hears and knows. */
    public static final String TURN_START="The light goes, and the second breath comes up under yours and takes it.";
    public static final String TURNED="Your skin draws tight and slick. The air is thin in your throat; water would be easier. Your hands are still your hands, mostly.";
    public static final String TURNED_DAWN="The light comes up and the second breath goes back down under yours. Your skin is your own again, and soaked.";
    public static final String SECOND_SLOT="It lets go of you. Under your breath, slower, another breath answers. You'll change when the last light goes.";
    public static final String GRAPPLE_WOUND="It lets go and slides off you, and leaves a deep cut.";
    public static final String GATE_WARN="Holding it shut, your throat starts to close on the next breath.";
    public static final String GATE_CHOKE="You can't get a breath past it.";
    public static final String GATE_EASED="The weight comes off the grate, and your breath with it.";
    public static final String PLATE_EDGE="Your breath shortens. The plate is near.";
    public static final String PLATE_CHOKE="Near the plate the gills in your neck clamp shut.";
    public static final String BREATH_BACK="The breath comes back.";
    public static final String NIGHT_CALL="You crouch and breathe out, slow and long. Out in the dark, things turn toward you.";
    public static final String NIGHT_CALL_BELOW="Down here your breath goes out through the water, and things come down into the branch after it.";
    public static final String CLAWED_SOMEONE="Your hand comes down and opens something.";
    public static final String RAKED="Something rakes across your shoulder. Cloth tears.";
    public static final String GRAPPLED="It gets its arms round you from behind and drags you toward the water.";
    public static final String GRAPPLE_BROKEN="Its hold breaks.";
    public static final String JAM_BELOW="The fairlead's throat, from underneath: the cable kinked hard into it.";
    public static final String JAM_CLEARED_BELOW="You get both hands on the kink and shove. Above you the cable jumps clear of the housing.";
    public static final String JAM_SEEN="The cable jumps back into the fairlead with a bang. Nobody's at it.";
    public static final String JAM_WORK="You get the marlinspike in under the kink and lever. It gives a little.";
    public static final String JAM_WORKED="The kink comes free and the cable runs straight out through the fairlead.";
    public static final String GATE_HOLD="You stand on the grate and bear down on it. Under it, something pushes back.";
    public static final String GATE_DRY="Dry under it. Nothing's pushing.";
    public static final String GATE_PINNED="You drive the pin through the grate's bars into the frame. It'll hold now.";
    public static final String GATE_UNPINNED="You work the pin out of the grate.";
    public static final String HATCH_DOWN="You lift the hatch and go down into the black water under the rollers. You can breathe it.";
    public static final String HATCH_UP="You come up through the hatch onto the boards, running water.";
    public static final String GRATE_THROUGH="You fold yourself through the bars of the grate.";
    public static final String PRINTS_SEEN="Wet prints on the boards, by the hatch. Somebody came up out of the water.";

    // ------------------------------------------------------------------------------------------------ Day Three (0.4.355)
    public static final String DEPTH_FIRST="Depth first. And the pin's on the porch rack; I had it in overnight to dress it. Somebody take it down to the ramp.";
    public static final String DEPTH_READ="Third mark on the sill. On the next rise she'll clear it.";
    public static final String DAY3_ROLL="Roll again. Answer up.";
    public static final String PIPE_ANSWER="From the pipe, a breath late: “Here.”";
    public static final String DAY3_QUESTION="Who's working the launch? Who's looking for the source? And who's still below?";
    public static final String STILL_BELOW="Nothing from %s. Still below, or somewhere I can't see.";
    public static final String DAY3_JOBS="Two ways off this bank. The launch: every roller seated, the pin carried down and set in its sleeve, the wheel tried, one of you on the rope and one on the pin. Or the feed: whatever comes up our pipes comes up it. Trace it down from the gauge and you'll find the old gallery. Get the bar to its gate and the way to the cutoff clear. Do one, or split it. Before dark.";
    public static final String CROSS_ASK="Do you want to cross?";
    public static final String CROSS_ANSWER="I want to hear everybody who's actually here.";
    public static final String LAST_NIGHT_ASK="About last night.";
    public static final String LAST_NIGHT_ANSWER="Then say it to them, not me. They're the ones who'll be next to you in the dark.";
    public static final String DAY3_BRIEF="Last light soon. Launch crew at the ramp: one below on the wheel, one up top on the pin, one on the rope. Gallery crew down with the bar. And whoever has the plate, decide where it stands tonight. It can't be everywhere.";
    public static final String HEAVE="You get your shoulder under the roller and heave. It shifts in its cradle and drops back.";
    public static final String ROLLERS_SEATED="You heave the roller back into its cradle. Every roller's seated now, the cable lying true from the drum to the fairlead.";
    public static final String SLEEVE_EMPTY="The pin's sleeve stands empty. Vera left the pin on the porch rack.";
    public static final String PIN_TAKEN="You lift the launch pin off the rack: iron, oiled, a ring through its head.";
    public static final String PIN_STAGED="You set the launch pin up in its sleeve over the sluice. It'll drop in when the wheel's held shut below.";
    public static final String WHEEL_TESTED="You run the wheel through its whole travel and back. Stiff, but it'll hold, with somebody on it.";
    public static final String ROPE_EARLY="The launch rope, made fast round its bollard. Nobody's going anywhere yet.";
    public static final String ROPE="The launch rope: a slip line round the bollard, its tail to a tripping lever. Pull the lever and she's let go.";
    public static final String LAUNCH_READY="The rollers seated, the pin up in its sleeve, the wheel tried, a hand for the rope and one for the pin. The launch is ready for dark.";
    public static final String FEED_TRACED="Traced: from the gauge, down past the intake, through the wall and along the gallery to the feed gate.";
    public static String feedLook(int bit){return switch(bit){
        case LastCrossingDayThree.FEED_GAUGE->"A pipe out of the gauge's housing, a tin tag wired to it: FEED. It runs down through the floor.";
        case LastCrossingDayThree.FEED_INTAKE->"The feed comes down the wall from the gauge room and goes into the stone beside the rusted door.";
        default->"The feed runs along the gallery's roof and turns down into the feed gate's housing.";};}
    public static String feedTrace(int bit){return switch(bit){
        case LastCrossingDayThree.FEED_GAUGE->"You rub the grime off the tag: FEED. The pipe goes down through the floor. You chalk an arrow on the wall beside it.";
        case LastCrossingDayThree.FEED_INTAKE->"The feed comes down the wall from above and goes into the stone, right beside a rusted door. You chalk an arrow.";
        default->"Here it is again, along the roof, turning down into the gate's housing. You chalk an arrow.";};}
    public static final String DOOR_RUSTED="A rusted iron door in the wall. Rusted shut; nobody's opened it in years.";
    public static final String DOOR_SHOVE="You put your shoulder to it. Rust gives, a little.";
    public static final String DOOR_OPENED="It grinds open on a stair going down into the dark, and a cold that smells of the river.";
    public static final String DOOR_HEARD="Iron grinds on iron in the east wall. The rusted door's open.";
    public static final String DOOR_OPEN_LOOK="The rusted door, open on the stair down.";
    public static final String BAR_EARLY="A locking bar for a sluice gate, heavy, on its hooks. Nobody needs it yet.";
    public static final String BAR_TAKEN="You lift the locking bar off its hooks. It's heavy.";
    public static final String BAR_STAGED="You set the locking bar in its bracket beside the gate, ready to drop across.";
    public static final String GATE_LOOK="The feed gate: an iron plate in the wall, the feed knocking behind it. A bracket beside it for a locking bar.";
    public static final String GATE_STAGED_LOOK="The feed gate, the locking bar staged beside it.";
    public static final String DEBRIS_LOOK="Fallen brick and silt, heaped to the roof of the passage.";
    public static final String DEBRIS_FIRST="You drag a broken brick off the heap, and silt slides down after it.";
    public static final String DEBRIS_MORE="You haul at the heap. It comes away an armful at a time.";
    public static final String DEBRIS_CLEARED="The last of the heap comes away.";
    public static final String PATH_CLEAR="The passage is clear to the cutoff.";
    public static final String CUTOFF_LOOK="The upstream cutoff, a wheel on the alcove wall. Against the feed it won't move; with the gate barred it would.";
    public static final String GALLERY_READY="The feed traced, the bar staged at the gate, the way to the cutoff clear. The gallery's ready for dark.";
    public static final String SOURCE_SEEN="Through the thick glass, black water, and something at the bottom of it: wrapped in weed, the size of someone crouching. It moves when you breathe.";
    public static final String MASTER_LOOK="The master bypass for the ramp channel. Rusted fast; it hasn't been turned in years.";
    public static final String MASTER_KNOWN="The master bypass, rusted fast on this side. At high water the pressure will come round behind it and it'll turn. You know that the way you know your own hands.";
    public static final String DOWNSTREAM_LOOK="The downstream sluice, where the branches empty into the river. Open. Wound shut, whatever runs in the branches would back up behind it.";
    public static final String OTHER_BREATH="Under your own breath the other one speaks, nearly words: the master wheel, at high water. Hold it open till the water's at the ramp, and breathe like us.";
    public static final String BRAKE_TAKEN_UP="The cable's taken up, the catch in the drum's teeth. Nothing to pull on today.";
    public static final String BRAKE_THROWN="The brake's set. Nothing to pull on till the rollers are reset.";

    // ------------------------------------------------------------------------------------------------ Night Three (0.4.356)
    public static final String DUSK3="That's the last light. The rise comes tonight. Get to your places.";
    public static final String NIGHT3_ROLL="Last roll. Same order as the first day. Answer up.";
    public static final String NIGHT_ANSWER3="Wheel below, pin above, and the rope when she's off the sill. Or the gate in the gallery, and the cutoff. Nobody goes anywhere alone.";
    public static final String TURN_FIRST="The light goes, and the breath that's been under yours for a day comes up and takes it.";
    public static final String RISE3="Here it comes. That's the rise.";
    public static final String MASTER_NOW="The water's high. Down at the foot of the pit, the master wheel will turn now.";
    public static final String PINNED3="Pin's in! Let go the wheel!";
    public static final String WHEEL_EASED="The pin drops home above you, and your throat opens on the water again.";
    public static final String OFF_SILL="She's off the sill! Whoever's on the rope, when you're ready.";
    public static final String STILL_ON_SILL="She's still on her sill. Not yet.";
    public static final String NOT_PINNED="She won't float off the sill with the sluice open. Wheel and pin first.";
    public static final String LET_GO="Let her go!";
    public static final String ABOARD="The rope slips the bollard and the deck moves under you. The station starts to slide away.";
    public static final String LEFT_ON_STATION="The ferry moves off the landing without you.";
    public static final String FERRY_NO_CLEAN="She comes in at the far landing. Nobody aboard is clean.";
    public static final String GATE_HOLD3="You throw your weight on the gate. The feed hammers behind it. Somebody drop the bar.";
    public static final String GATE_HOLD_TURNED="You throw your weight on the gate, and as the feed hammers behind it your throat starts to close.";
    public static final String GATE_LET_GO="The gate kicks back against you and you lose it.";
    public static final String NO_BAR="There's no bar here to drop. It's on its hooks in the intake.";
    public static final String GATE_BARRED="You drop the bar across the gate. It takes the feed's weight with a clang.";
    public static final String FEED_GATE_EASED="The bar drops across and the gate's weight comes off you.";
    public static final String BARRED_CUTOFF="Barred. Now the cutoff.";
    public static final String CUTOFF_UNBARRED="It won't turn against the feed. Bar the gate first.";
    public static final String CUTOFF_SHUT="You wind the cutoff shut. Behind the gate the hammering stops, and somewhere the water starts to fall.";
    public static final String DRAINED="Behind the glass the water goes down, and the gate draws up into its housing. On the source, things are clinging, holding it.";
    public static final String COVERED="They're over it, covering it. Get them off it first.";
    public static final String SOURCE_STRUCK="It gives under the blow like something drowned, and the whole chamber breathes in.";
    public static final String SOURCE_AGAIN="Again. It shudders.";
    public static final String RELEASED="Something in your chest lets go. The second breath is gone. You're only yourself.";
    public static final String STILL_WATER="Listen. The water's gone still.";
    public static final String MUSTER="That's the bell. Out of the gallery, all of you, where I can see you.";
    public static final String MASTER_WONT="Rusted fast. It won't turn for you.";
    public static final String MASTER_DENIED_LOOK="The master wheel, slack. The water won't come through it now.";
    public static final String MASTER_HOLDING="You've got it open. Hold it.";
    public static final String MASTER_OPENED="At high water the wheel turns as if it wanted to. Water runs back into the ramp channel the wrong way. Hold it open.";
    public static final String MASTER_SWINGS_SHUT="You let go, and the master wheel swings shut behind you.";
    public static final String HAMMER="The pipes hammer. Water's coming through them the wrong way.";
    public static final String APPROACH_FLOOD="Water comes up over the ramp's approach, dark and quick.";
    public static final String BREATH_TAKEN="In the flood channel something lets go of your throat for good. You'll stay under longer now. Wherever you are.";
    public static final String SIDED="The water runs through. Whatever you were holding it open for, it's open.";
    public static final String DENIED="The water goes slack in the channel. Whatever was opening in your throat closes again.";
    public static final String DOWNSTREAM_IN_TIME="You wind the downstream sluice shut. The hammering in the pipes stops, and the water at the ramp goes back down.";
    public static final String DOWNSTREAM_LATE="You wind the downstream sluice shut. The water goes down off the landing.";
    public static final String DOWNSTREAM_NOTHING="You wind the downstream sluice shut. Nothing's running through it.";
    public static final String FAR_ROLL="Roll. Answer up, all of you.";
    public static final String SOURCE_DOCKS="The ferry's coming in. Listen.";
    public static final String FROM_THE_BOAT="From the far end of the boat:";
    public static final String BOAT_CALLS="Someone aboard, calling the roll:";

    // ------------------------------------------------------------------------------------------------ 0.4.357: what the readers carry home
    public static final String PLATE_BREAKS="Her name comes off her now. A piece for every name on the roll, here or not.";
    public static final String SPARE_ASK="There's a spare. It broke away at one bolt. It goes to one of you who came through clean. Say who.";
    public static final String NOMINATED="You say the name. Vera writes it at the bottom of the roll.";
    public static String spareTo(String name){return "The spare's for "+name+". That's what you said.";}
    public static String spareFromVera(String name){return "Nobody's saying, so it goes by the roll. "+name+", it's yours.";}
    public static final String SPARE_IN_HAND="The spare is put in your hand: a curl of brass with one bolt hole and no letters.";
    public static String pieceInHand(String letters){return letters.isEmpty()?"A piece of the nameplate is put in your hand, with no letters on it.":"A piece of the nameplate is put in your hand. The letters on it: "+letters+".";}
    public static final String PIECE_KNOCKS="The piece you carry knocks, softly, once.";
    public static final String TAP_ANSWERED="You tap the brass. Somewhere, softly, another piece answers.";
    public static final String TAP_QUIET="You tap the brass. Nothing answers.";
    public static final String SPARE_NOT_YET="Not yet. It isn't over.";
    public static final String SPARE_NOT_THEIRS="It isn't theirs to carry.";
    public static final String SPARE_NOT_TURNED="It's meant for someone who turned.";
    public static final String SPARE_GIVEN="You put the spare in their hand.";
    public static String spareReceived(String name){return name+" puts the spare in your hand. Against your own piece, the bolt holes line up.";}

    // ------------------------------------------------------------------------------------------------ 0.4.357: each reader's account
    public static final String TITLE="The account: the last crossing";
    private static String names(String list){var all=new ArrayList<>(Arrays.asList(list.split(",")));all.removeIf(String::isBlank);
        if(all.size()<=1)return String.join("",all);return String.join(", ",all.subList(0,all.size()-1))+" and "+all.get(all.size()-1);}
    private static String job(String job){return switch(job){
        case "intake"->"I worked the intake, down on the bleed screw.";case "gauge"->"I was up in the gauge room, reading the needle.";
        case "lookout"->"I was the cable lookout.";case "landing"->"I worked the landing and the line on the ramp.";
        case "spillway"->"I walked the spillway.";case "lamp"->"I carried the station lamp.";case "brake"->"I took the winch brake.";
        default->"";};}
    /** One reader's account of the crossing, from their own record. Every reader on the roll gets one, the dead and the absent included. */
    public static ItemStack account(String name,CompoundTag t){
        var text=new StringBuilder();
        text.append("A farm road ran down into floodwater, and a power line went up a levee to a flood station by a river, where the last ferry sat at its ramp and a lamp burned on the far bank. ");
        if(t.getBoolean("Partway")&&!t.getBoolean("Signed"))text.append("I came in after the roll had been called. ");
        else{text.append("The keeper, Vera, had us sign a roll on the porch and called every name aloud. ");
            if(t.getBoolean("HeardEllis"))text.append("Then she called one more, Ellis, and waited, and nobody answered. ");}
        String j=job(t.getString("Job"));if(!j.isEmpty())text.append(j).append(' ');
        if(t.getBoolean("PlateClean"))text.append("When I laid my palm on the handprint plate it left a breath of fog on the near side and nothing on the far. ");
        // Night One.
        if(t.getBoolean("Gripped")){text.append("On the first night a hand came up through the broken corner of the intake grate and took hold of me. ");
            if(t.getBoolean("FoughtFree"))text.append("I beat it off myself. ");else if(t.getBoolean("PulledUp"))text.append("Somebody reached down through the landing's rail and pulled me up over it. ");else if(t.getBoolean("Freed"))text.append("Somebody beat it off me. ");
            text.append("After that there was a second breath under my own. ");
            if(t.getBoolean("Told"))text.append("I told Vera. ");
            if(t.getBoolean("Exposed"))text.append("I laid my palm on the plate and let the others see a second print come up on the far side. ");}
        else if(t.getBoolean("SawClaw"))text.append("On the first night I saw a hand come up through the intake grate and take hold of one of us. ");
        if(t.getBoolean("SawUndertow"))text.append("Things came up out of the water at night and over the spillway's banks. ");
        // Day Two and Night Two.
        if(t.getBoolean("LinedUp"))text.append("On the second day I helped line the winch drum up on its marks. ");
        if(t.getBoolean("OpenedBypass"))text.append("I opened a bypass wheel and flooded a service branch. ");
        if(t.getBoolean("Turned")){String where=t.getString("TurnedWhere");
            text.append(where.isEmpty()?"At the second dusk I turned where I stood. ":"At the second dusk I turned where I stood, "+where+". ");
            text.append("I could breathe the water, and my hands were claws, and everything I said came out broken. ");}
        else if(t.getBoolean("SawTurn"))text.append("At the second dusk I watched someone I knew turn, and knew whose face it was. ");
        if(t.getBoolean("HeldPull")||t.getBoolean("Pulled"))text.append("I held the winch brake off through the pull. ");
        if(t.getBoolean("PullFailed"))text.append("The pull failed, and the drum ran back and threw the rollers. ");
        if(t.getBoolean("SecondSlot"))text.append("On the second night one of the things held me long enough, and I carried it too after that. ");
        // Day Three.
        String plan=t.getString("Plan");
        if(plan.equals("launch"))text.append("On the third morning I said I would work the launch. ");else if(plan.equals("source"))text.append("On the third morning I said I would look for the source. ");else if(plan.equals("either"))text.append("On the third morning I said I would go wherever I was needed. ");
        if(t.getBoolean("AskedCross"))text.append("I asked Vera whether she wanted to cross. She put the clipboard down and said she wanted to hear everybody who was actually there. ");
        if(t.getBoolean("SawSource"))text.append("Through thick glass under the ramp I saw something at the bottom of the water, wrapped in weed, that moved when I breathed. ");
        if(t.getBoolean("OfferedBreath"))text.append("Under my own breath the other one offered me the master wheel at high water. ");
        // Night Three.
        if(t.getBoolean("HeldWheelToPin"))text.append("On the last night I held the sluice wheel at the bottom of the flooded pit until the pin dropped home. ");
        if(t.getBoolean("SeatedLaunchPin"))text.append("I dropped the launch pin from the deck. ");
        if(t.getBoolean("PulledRelease"))text.append("I pulled the launch rope and let her go. ");
        if(t.getBoolean("HeldGateToBar"))text.append("I held the feed gate in the gallery while "+(t.getString("BarredWith").isEmpty()?"someone":names(t.getString("BarredWith")))+" dropped the bar across it. ");
        else if(t.getBoolean("BarredGate"))text.append("I dropped the bar across the feed gate while "+(t.getString("BarredWith").isEmpty()?"someone":names(t.getString("BarredWith")))+" held it. ");
        if(t.getBoolean("ClosedCutoff"))text.append("I wound the upstream cutoff shut. ");
        if(t.getBoolean("DestroyedSource"))text.append("When the things were off it, I struck the source until it was dead. ");
        else if(t.getBoolean("StruckSource"))text.append("When the things were off it, I struck the source. ");
        else if(t.getBoolean("SawSourceDie"))text.append("I was in the gallery when the source died. ");
        if(t.getBoolean("ReleasedByFight"))text.append("When it died the second breath went out of me, and I was only myself. ");
        if(t.getBoolean("OpenedMaster")){text.append("At high water I opened the master bypass and held it. The pipes hammered and the water came the wrong way. ");
            if(t.getBoolean("BargainDenied"))text.append((t.getString("DeniedBy").isEmpty()?"Someone":names(t.getString("DeniedBy")))+" wound the downstream sluice shut before it was done, and whatever was opening in my throat closed again. ");
            else if(t.getBoolean("OtherBreath"))text.append("I held it until the water was over the ramp's approach, and something let go of my throat for good. ");
            else if(t.getBoolean("SidedWithSource"))text.append("I held it until the water was over the ramp's approach. ");}
        else if(!t.getString("SawMasterOpened").isEmpty())text.append("I saw "+names(t.getString("SawMasterOpened"))+" open the master bypass. ");
        if(t.getBoolean("DeniedBargain"))text.append("I wound the downstream sluice shut while the master was open, before it was done. ");
        else if(t.getBoolean("DrainedLanding"))text.append("I wound the downstream sluice shut, too late for anything but the landing. ");
        // Dying.
        String died=t.getBoolean("DiedNightOne")?"on the first night":t.getBoolean("DiedNightTwo")?"on the second night":t.getBoolean("DiedDayThree")?"on the third day":t.getBoolean("DiedNightThree")?"on the last night":t.getBoolean("Died")?"there":"";
        if(!died.isEmpty())text.append("I died "+died+", and watched the rest of it from the far bank. ");
        // The ending.
        String ending=t.getString("Ending");
        if(ending.equals("ferry")){
            if(t.getBoolean("CrossedOver"))text.append(t.getString("AboardWith").isEmpty()?"I was aboard when she crossed. ":"I was aboard when she crossed, with "+names(t.getString("AboardWith"))+". ");
            else if(t.getBoolean("LeftOnStation"))text.append("The ferry moved off the landing without me. ");
            else text.append("The ferry crossed. ");
            text.append("On the far bank Vera called the roll. ");
            if(t.getBoolean("AnsweredFar"))text.append("I answered. ");else if(t.getBoolean("UnansweredFar"))text.append("Nobody answered for me. ");
            if(!t.getString("HeardUnanswered").isEmpty())text.append("Nobody answered for "+names(t.getString("HeardUnanswered"))+". ");
            text.append(t.getBoolean("HeardEllisLate")?"After Ellis's name she waited, and a long breath later something at the far end of the boat said Here. ":"After Ellis's name she waited. ");}
        else if(ending.equals("gallery")){
            text.append("The water went still. ");
            text.append(t.getBoolean("RangMuster")?"I rang the station bell for the muster. ":"The station bell rang for the muster. ");
            text.append(t.getBoolean("OutOfGallery")?"Vera marked me down as one of the ones who came out of the gallery with her. ":"Vera marked down who came out of the gallery with her. ");}
        else if(ending.equals("source")){
            text.append("Nobody clean was left to stop it, and the night ran out. The ferry came in at the far landing anyway, by itself. Someone aboard called the roll in Vera's order, and every name got an answer from the far end of the boat");
            text.append(t.getBoolean("HeardEllisFar")?", Ellis's too. ":". ");
            text.append("Vera said she didn't know who answered them. ");}
        if(t.getBoolean("BackAtMorning"))text.append("In the morning I was myself again. ");
        // What was carried home.
        if(t.getBoolean("GotPiece")){String letters=t.getString("PieceLetters");
            text.append("\n\nVera broke the ferry's nameplate along its bolt holes, a piece for every name on the roll. ");
            text.append(letters.isEmpty()?"Mine has no letters on it. ":"Mine says "+letters+". ");}
        if(t.getBoolean("NamedForSpare")||t.getBoolean("GivenSpareByVera")||t.getBoolean("GotSpare")&&t.getString("SpareFrom").isEmpty())text.append(t.getBoolean("GivenSpareByVera")?"Nobody said who should have the spare, so Vera gave it to me, by the roll. ":"The spare came to me. ");
        else if(!t.getString("SpareWentTo").isEmpty())text.append("The spare went to "+names(t.getString("SpareWentTo"))+". ");
        if(!t.getString("SpareFrom").isEmpty())text.append(t.getString("SpareFrom")+" gave me the spare, and against my own piece the bolt holes lined up. ");
        if(!t.getString("GaveSpareTo").isEmpty())text.append("I gave the spare to "+t.getString("GaveSpareTo")+". ");
        if(t.getBoolean("OtherBreath")||t.getBoolean("OtherBreathKept"))text.append("Under water I hold my breath a little longer than I used to. ");
        return HouseWriting.book(TITLE,name,pages(HouseWriting.WritingStyle.WILL,text.toString().trim()));
    }
    public static final String MORNING_FERRY="That's the light. Whoever's here, you're across.";
    public static final String MORNING_GALLERY="That's the light. Water's still. We'll take her across in the morning, all of us that are left.";
    public static final String MORNING_SOURCE="That's the light. Every name answered. I don't know who answered them.";
}
