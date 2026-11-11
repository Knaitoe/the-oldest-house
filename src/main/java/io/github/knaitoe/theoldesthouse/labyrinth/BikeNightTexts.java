package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.464, Bike night (docs/BIKE_NIGHT_PROPOSAL.md): what is written and said. This slice: the sheet taped up by the park's door (the
 * first paper), what the reader is told about the park and the bikes, and as much of the account as there is a night to give.
 * Every name, place and line is original.
 */
public final class BikeNightTexts {
    private BikeNightTexts(){}
    public static final String HINT="Whichever bike you take from the rack is yours for the night.";
    public static final String ARRIVE="A town park after dark, on the last night of October. A path runs from the shed to the gate, and by the gate six bikes stand in a rack.";
    /** A bike taken; a bike given back for another; a bike that is someone else's (their name goes in); one's own, already had. */
    public static final String YOURS="It is yours for the night.",SWAPPED="You leave the other one for whoever wants it. This one is yours for the night.";
    public static final String THEIRS="%s has that one tonight.",SOMEONES="Somebody has that one tonight.";
    /** A hand on the park's own things. */
    public static final String GATE="The gate is chained, and the padlock on the chain is new.";
    public static final String RAILINGS="Iron railings, taller than you are, with points on them.";
    public static final String LAMP="A park lamp. It gives a small light and a hum.",LAMP_FAILING="This one keeps going out. Each time it comes back a little later.";
    public static final String BENCH="A park bench. Somebody has cut initials into the top slat, and somebody else has cut them out again.";
    public static final String BIN="A wire bin, a third full: wrappers, a paper bag, a burst balloon.";
    /** 0.4.471: the bench by the playground; the drinking fountain, pressed and already running; the payphone, lifted and already held. */
    public static final String CARVED="A park bench. A heart cut into the top slat, long ago, and gone the same weathered way as the wood. Inside it: R.D. + A.L. Nobody has cut these out.";
    public static final String FOUNTAIN="You press the button and drink. The water is cold, and tastes of the pipe.";
    public static final String FOUNTAIN_RUNNING="It is still running.";
    public static final String PHONE="A payphone. You lift the handset: a dial tone. It wants a coin, and you have not got one.";
    public static final String PHONE_HELD="The dial tone goes on.";
    public static final String RACK="A bike rack bolted to the path. Six places.";
    public static final String BORDER="Squared timbers round the sand, worn pale where people step over them.";
    /** 0.4.465, the swings: a hand on the frame; what a reader on one is told once; coming down off one; a hand on the mark. */
    public static final String SWINGS="A steel frame with four swings on chains under it. The paint is worn through where hands go.";
    public static final String SWING_HINT="Lean with the swing to take it higher. Jump to let go.";
    public static final String JUMP="You come down %s paces out from the swings. The furthest tonight is %s.";
    public static final String JUMP_LEVEL="You come down %s paces out from the swings, level with the furthest tonight.";
    public static final String JUMP_BEST="You come down %s paces out from the swings. Nobody has landed further tonight.";
    public static final String JUMP_OFF="You come down off the sand. That does not count.";
    public static final String MARK="A heel mark dug into the sand: the furthest out anyone has landed off the swings tonight.";
    public static final String MARK_OWN=MARK+" It is yours.",MARK_THEIRS=MARK+" It is %s's.";
    /** 0.4.466, the slide: a hand on any part of it. */
    public static final String SLIDE="A slide. Its chute is worn bright down the middle and dull at the edges.";
    /**
     * 0.4.467, the monkey bars: a hand on their ladders; a hand on the rungs from the ground, from on top of them and from too
     * far off; what a reader hanging from them is told once; and going from one end to the other.
     */
    public static final String BARS="Monkey bars. The paint is gone from every rung where hands go.";
    public static final String BARS_LOW="Out of reach from down here. Jump for them, or go up the ladder at either end.";
    public static final String BARS_ON="You are standing on top of them. Nobody is about to tell you to get down.";
    public static final String BARS_FAR="Not from here. Get under them.";
    public static final String BARS_HINT="Strafe to swing. Strafe again at the top of the swing to catch the next rung. Too early, and you drop. Sneak lets go.";
    /** 0.4.470: a hand let go before the swing had carried it to the next rung. */
    public static final String BARS_EARLY="Too early. Your hand misses the rung.";
    public static final String BARS_ACROSS="Across, from one end to the other, and not a foot on the sand.";
    /** 0.4.468, the seesaw: a hand on it; a hand on an end somebody is on; and what a reader sitting on it is told once. */
    public static final String SEESAW="A seesaw: a long board on a steel stand, with a seat and a handle at each end.";
    public static final String SEESAW_TAKEN="Somebody is on that end.";
    public static final String SEESAW_HINT="Jump to push off the sand when your end is down. Push just as you land, and it bounces. Sneak gets off.";
    /** 0.4.470: the other end came down hard, to the reader thrown off the top; and to the one who came down, of the one they threw. */
    public static final String SEESAW_THROWN="The bump throws you off the top.";
    public static final String SEESAW_THREW="The bump throws %s off the other end.";
    /**
     * 0.4.469, the merry-go-round: a hand on it that pushes neither way; a hand on it from on its deck, and from too far off; and
     * what a reader going round on it is told once.
     */
    public static final String ROUNDABOUT="A merry-go-round. Push it along its rim, the way it is to go.";
    public static final String ROUNDABOUT_ON="You are on it. It is pushed from the sand, by a rail.";
    public static final String ROUNDABOUT_FAR="Too far off to push it.";
    public static final String ROUNDABOUT_HINT="Sneak to hold on. Stand up on it when it is going fast and it throws you off.";
    /** 0.4.471, the climbing frame: a hand on it; and what a reader who gets to the top of it is told once. */
    public static final String DOME="A climbing dome: steel bars bolted into triangles, worn bright where hands and shoes go. Walk into it and you climb it. Keep low, and you can get in between the bars at the bottom.";
    public static final String DOME_TOP="The top of the dome. From here you can see over the railings to the street.";
    public static final String SPRING="A horse on a spring, bolted into the ground. Its paint is gone where knees go.";
    public static final String SPRING_TAKEN="Somebody is on it.";
    public static final String SPRING_HINT="Strafe to rock it. Rock with it, and it goes further. Sneak gets off.";
    /** The sand: a ball packed; no more to be carried; no room for one; what is left of one carried out of the night. */
    public static final String SAND="You pack a ball of sand. It holds.";
    public static final String SAND_FULL="Your pockets are full of sand.";
    public static final String SAND_NO_ROOM="You have nowhere to put it.";
    public static final String SAND_GONE="The sand has run out through your fingers.";

    private static List<Component> pages(HouseWriting.WritingStyle style,String... texts){var out=new ArrayList<Component>();for(String text:texts)for(String p:LookoutTexts.pages(text,150))out.add(HouseWriting.page(style,p));return out;}
    /** A sheet from a school pad, in pencil. (Not the House's child's hand, which is a small child's and loses letters: Gus can spell.) */
    public static ItemStack source(){
        return HouseWriting.book("A page from a school pad","Gus",pages(HouseWriting.WritingStyle.PLAIN,
            "HALLOWEEN PLAN. Secret.\n\n1. They say we are too old for trick or treat. Fine.\n\n2. Meet at Calder Park when it is dark. The gate will be chained. Come through the tool shed. The lock on it has been broken since summer.",
            "3. Bring your bike. If you have not got one there are always some in the rack that nobody comes back for.\n\n4. Bring a flashlight. Bring candy if you have any.",
            "5. If Tully and those guys turn up do NOT say anything. Remember last time.\n\n6. Do NOT cut through the graveyard. I mean it. You know why.\n\nGus\n\nP.S. The one with the card in its spokes is mine. Kidding. Take any."));
    }

    /** What a bike is called in the account. */
    static String bike(String make){
        return switch(make){case "wasp"->"the Wasp";case "hare"->"the Hare";case "comet"->"the Comet";case "hopper"->"the Hopper";case "lamp"->"the Lamp";case "rattler"->"the Rattler";default->"";};
    }
    /** The account, from the reader's own record. There is only the park to tell of yet. */
    public static ItemStack account(String name,CompoundTag own){
        var p=new ArrayList<String>();
        p.add("It was the last night of October in a town called Calder Falls, and I was in the park after it had shut.");
        String bike=bike(own.getString("Bike"));
        p.add(bike.isEmpty()?"There were six bikes in the rack by the gate. I did not take one.":"There were six bikes in the rack by the gate. I took "+bike+".");
        // The playground, as far as this reader went on it.
        int far=own.getInt("SwingBest");
        if(far>0)p.add("I got one of the swings as high as it would go and let go of it, and came down "+Stillness.words(far)+" paces out in the sand.");
        else if(own.getBoolean("Swung"))p.add("I sat on one of the swings for a while.");
        if(own.getInt("Slid")>0)p.add(own.getInt("Slid")>2?"I went down the slide, and then went round and did it again.":"I went down the slide.");
        if(own.getInt("Crossed")>0)p.add("I went all the way across the monkey bars, hand over hand, without a foot on the sand.");
        else if(own.getInt("BarsMissed")>0)p.add("I went for the next rung of the monkey bars too early, and dropped into the sand.");
        else if(own.getInt("Hung")>0)p.add("I hung from the monkey bars for a while, and dropped.");
        String with=own.getString("SeesawWith");
        String threw=own.getString("SeesawThrew");
        if(!with.isEmpty())p.add("I went up and down on the seesaw with "+with+" on the other end."
            +(own.getBoolean("SeesawThrown")?" We got it bouncing, and a hard bump threw me off the top of it.":!threw.isEmpty()?" We got it bouncing, and I bounced "+threw+" off the top of it.":""));
        else if(own.getInt("Seesaw")>0)p.add("I sat on one end of the seesaw with nobody on the other, and pushed myself up, and came down again each time.");
        else if(own.getBoolean("SeesawSat"))p.add("I sat on one end of the seesaw for a while.");
        int rounds=(int)(own.getDouble("RoundTurned")/(Math.PI*2));
        if(own.getInt("RoundFlung")>0)p.add("I stood up on the merry-go-round while it was going fast, and it threw me off into the sand.");
        else if(rounds>0)p.add("I went round on the merry-go-round "+(rounds==1?"once":Stillness.words(rounds)+" times")+".");
        else if(own.getInt("RoundPushed")>0)p.add("I pushed the merry-go-round and watched it turn.");
        if(own.getBoolean("DomeTop"))p.add("I climbed to the top of the climbing dome and looked out over the railings.");
        if(own.getInt("Sprung")>0)p.add("I rocked the horse on its spring as far as it would go, for no reason at all.");
        else if(own.getBoolean("SpringSat"))p.add("I sat on the horse on its spring.");
        if(own.getBoolean("Drank")&&own.getBoolean("Phoned"))p.add("I drank from the fountain, and lifted the payphone by the door to hear its tone, with no coin to put in it.");
        else if(own.getBoolean("Drank"))p.add("I drank from the fountain. The water tasted of the pipe.");
        else if(own.getBoolean("Phoned"))p.add("I lifted the payphone by the door to hear its tone, with no coin to put in it.");
        if(own.getInt("Sand")>0)p.add(own.getInt("SandHits")>0?"I packed the sand into balls and threw them, and not all of them missed.":"I packed the sand into balls and threw them.");
        return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.BIKE_NIGHT),name,pages(HouseWriting.WritingStyle.WILL,p.toArray(String[]::new)));
    }
}
