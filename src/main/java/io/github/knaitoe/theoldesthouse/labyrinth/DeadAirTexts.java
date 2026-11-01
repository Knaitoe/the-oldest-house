package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.206, the Dead Air Broadcast: three hands. The forum printouts from 2001, people trying to remember a children's programme
 * on channel four that nobody else remembers (the source, and two more from Percy's lap); Percy's brass marquee; and the
 * teleprompter's stage directions, which are what the reader did, written down as if it had been scripted. The account is the
 * last page of the script. All of it is original. House writing: never the reader's gender, never a colour of theirs, never an age.
 */
public final class DeadAirTexts {
    private DeadAirTexts(){}
    private static final int PAGE=150;
    static List<Component> plain(String text){var out=new ArrayList<Component>();for(String p:LookoutTexts.pages(text,PAGE))out.add(HouseWriting.page(HouseWriting.WritingStyle.PLAIN,p));return out;}

    /** The first printout, on the folding table by the receiver. */
    public static ItemStack source(){return HouseWriting.book("NetNostalgia, printed","Television (local)",plain("""
        NETNOSTALGIA FORUM > TELEVISION (LOCAL) > "little pirate theatre?? ch 4, early 80s"

        kestrel_77, 3 March 2001: There was a show on channel four after the news, maybe '82 or '83. A puppet boy called Percy on a boat that was really a basement. Everyone laughed at him. Does anybody else remember this or am I losing it.

        oldtvnut, 3 March 2001: Percy! The boy with the strings that were all cut and still moving. I remember being sorry for him.

        md_brink, 4 March 2001: There was a tall one. Made of sacks, sewn up, a top hat. Teeth like a sink. When it came on you had to turn the dial or it got closer to the camera. I think it was called the Skin-Taker. I'm not joking.

        kestrel_77, 4 March 2001: Yes. YES. And the man on the set in the corner with the moustache, Horace something, who you only saw in the snow.

        harbourlight, 5 March 2001: I asked my mother about it this weekend. She said I used to sit in front of channel four when it was off the air, for half an hour, and tell her about the boat. She said there was nothing on. It was just the snow.

        kestrel_77, 5 March 2001: My mother says the same.""".stripIndent()));}

    /** Percy drops a printout in his lap each visit: the thread, going on. */
    public static ItemStack printout(int n){
        String text=n<=1?"""
            NETNOSTALGIA FORUM > TELEVISION (LOCAL) > "little pirate theatre?? ch 4, early 80s" (page 2)

            oldtvnut, 9 March 2001: Somebody on another board says it wasn't the station at all. Somebody was getting onto channel four from a relay out on the salt marsh past the bypass. Three masts. You could tune it on a shortwave set if you had the right tube in it, seventy-four point five.

            md_brink, 9 March 2001: I remember the marsh. The tall one walked on the boardwalks. The only time it stopped was when the picture went dead. Blue, and nothing, and it couldn't move.

            harbourlight, 10 March 2001: Please stop posting about this.""":"""
            NETNOSTALGIA FORUM > TELEVISION (LOCAL) > "little pirate theatre?? ch 4, early 80s" (page 3)

            nightdesk_w, 21 March 2001: I worked nights at the county relay for two years. Three masts on the marsh, Alpha, Beta, Gamma. Somebody kept coming in and turning the dishes. Always to the same bearings: Alpha due east, ninety. Beta two hundred and twenty-five. Gamma three-fifteen. When all three locked the lights at the tops went blue and something went out on channel four.

            nightdesk_w, 21 March 2001: There was never anybody in the booth. I checked every night. There was a highchair.

            kestrel_77, 22 March 2001: Why was there a highchair.""";
        return HouseWriting.book("NetNostalgia, printed","Television (local)",plain(text.stripIndent()));
    }

    /** Percy's brass marquee: what scrolls across his chest when someone stands near him or touches him. */
    public static String percy(int visit,int channel,boolean dead){
        if(dead)return "PERCY: HE CAN'T MOVE NOW. GO. GO.";
        return switch(visit){
            case 1->channel==1?"PERCY: THE TALL MAN IS IN THE HALL. THERE'S A BREAKER ON THE WALL. KILL THE AIR AND CRAWL UNDER HIS ARMS.":"PERCY: TURN IT TO THE STAGE. THE TUBE IS IN THE CHEST AT THE END OF IT. I'M NOT SCARED. I'M NOT SCARED.";
            case 2->"PERCY: THE TUBE MAKES THE MARSH. TURN THE DISHES WHERE THE NIGHT MAN SAID. HE WALKS THE BOARDS.";
            case 3->"PERCY: THEY TOOK THE WALLS DOWN. YOU SEE? IT WAS ALWAYS A SET. READ YOUR LINES.";
            default->"PERCY: THE CURTAIN'S OPEN. EVERYBODY'S WAITING. GO UP.";
        };
    }

    // ------------------------------------------------------------------------------------------------ the script
    static String actor(int index){return index==0?"ACTOR":index==1?"SECOND ACTOR":"ACTOR "+(index+1);}
    /** The teleprompter's pages: what the reader did, as stage directions. */
    public static String scriptText(String who,CompoundTag own){
        var b=new StringBuilder("LITTLE PIRATE THEATRE. SHOOTING SCRIPT.\n\nSCENE 1. THE BASEMENT.\n\n");
        b.append(who).append(" enters through the door in the paneling. Looks at the snow on the set.\n\n");
        if(own.getBoolean("Tuned4_206"))b.append(who).append(" adjusts the shortwave dial to 088.1 MHz. The lights go magenta.\n\n");
        if(own.getInt("Lever206")>0)b.append(who).append(" pulls the wall-breaker. DEAD AIR.").append(own.getInt("Lever206")>1?" (Again.)":"").append("\n\n");
        if(own.getBoolean("Crawled206"))b.append(who).append(" gets down on hands and knees and crawls under the arms of THE TALL MAN.\n\n");
        if(own.getBoolean("GotTube206"))b.append(who).append(" opens the chest at the end of the stage and takes out the brass tube.\n\n");
        if(own.getBoolean("Fitted206"))b.append(who).append(" fits the tube into the receiver. It warms up, orange.\n\n");
        b.append("SCENE 2. THE MARSH.\n\n");
        if(own.getBoolean("Tuned3_206"))b.append(who).append(" adjusts the dial to 074.5 MHz. ").append(who).append(" hesitates before entering the marsh.\n\n");
        int dishes=Integer.bitCount(own.getInt("DishMask206"));
        if(dishes>0)b.append(who).append(" turns ").append(dishes==3?"all three dishes":dishes==2?"two of the dishes":"a dish").append(" to the night man's bearings.\n\n");
        if(own.getInt("Breakers206")>0)b.append(who).append(" throws a breaker on its pole. DEAD AIR. THE TALL MAN stops on the boardwalk.\n\n");
        if(own.getInt("Caught206")>0)b.append("(").append(who).append(" was touched ").append(own.getInt("Caught206")==1?"once":own.getInt("Caught206")+" times").append(" and had to go again from the top.)\n\n");
        b.append("SCENE 3. THE STAGE.\n\nThe walls come down. ").append(who).append(" sees the studio for the first time and reads this.\n\nSCENE 4. THE BOOTH.\n\n").append(who).append(" goes up the stair behind the curtain.");
        return b.toString();
    }
    public static ItemStack script(String who,CompoundTag own){return HouseWriting.book("Teleprompter pages","Little Pirate Theatre",plain(scriptText(who,own)));}

    // ------------------------------------------------------------------------------------------------ the account
    public static String accountText(String name,CompoundTag own){
        return scriptText("ACTOR",own)+"\n\nACTOR stands before nine monitors of snow. A highchair faces them, rusted, with cut strings hanging over it. There is no crew.\n\nThe centre monitor clears. PERCY's marquee scrolls across it:\n\nYOU ARE ON THE AIR, LITTLE PIRATE.\n\nEND OF BROADCAST.\n\n(Written in the margin, in pencil, in my own hand: there was never anybody in the booth.)";
    }
    public static ItemStack account(String name,CompoundTag own){return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.DEAD_AIR),name,plain(accountText(name,own)));}
    public static final String HINT="Turn the dial and the hallway is what it is tuned to. A breaker kills the air and stops the tall one where it stands, but not for long.";
}
