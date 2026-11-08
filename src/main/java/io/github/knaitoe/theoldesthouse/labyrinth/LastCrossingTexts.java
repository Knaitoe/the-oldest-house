package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import io.github.knaitoe.theoldesthouse.labyrinth.CrossingBlock.Kind;
import java.util.*;
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
            case NAMEPLATE->"The ferry's nameplate, cast in brass: CORDELIA.";
            case BAR->stage==1?"The bar's across the door.":"A heavy bar, leaning by the door. There are brackets either side of the frame to take it.";
            case ISOLATOR->stage==1?"The isolation valve, shut. The upper floor's on its own pump now.":"The isolation valve for the lower floor. Open.";
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
}
