package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.205, the Left-Right Game: Apollo's teletype roll with the rule (the source), his voice on the CB, his convoy log (four
 * entries, one a night), Bonnie on the payphone at the Bluejay cut (her words come up backwards in the condensation), and the
 * account, the reader's own road log kept the way Apollo kept his. All of it is original. House writing: never the reader's
 * gender, never a colour of theirs, never an age.
 */
public final class LeftRightTexts {
    private LeftRightTexts(){}
    private static final int PAGE=150;
    static List<Component> plain(String text){var out=new ArrayList<Component>();for(String p:LookoutTexts.pages(text,PAGE))out.add(HouseWriting.page(HouseWriting.WritingStyle.PLAIN,p));return out;}

    /** The teletype roll on the booth's desk: what Apollo sent down the line the night he went out. */
    public static ItemStack source(){return HouseWriting.book("Teletype roll, Bristol Ferry toll","Channel 19",plain("""
        TTY / CH 19 / BRISTOL FERRY TOLL / 0214

        APOLLO TO WHOEVER IS READING THIS AT THE BOOTH. THE ROAD PAST THE TOLL FORKS AND KEEPS FORKING. THERE IS ONE RULE AND IT IS THE WHOLE GAME.

        LEFT AT THE FIRST FORK. THEN RIGHT. THEN LEFT. NEVER THE SAME WAY TWICE. THE CB WILL TELL YOU WHICH SIDE A NIGHT STARTS ON. ASK IT.

        WRONG TURN AND THE ROAD ENDS IN SALT. THERE WILL BE A WOMAN BY THE CARS. DO NOT TURN YOUR BACK. KEEP YOUR EYES ON HER AND WALK BACKWARD THE WAY YOU CAME. SHE ONLY MOVES WHEN NOBODY IS LOOKING.

        FLARES IN THE CRATE. ONE ON THE DIVIDER HOLDS THE SALT OFF FOR AS LONG AS IT BURNS. YOU WILL NEED THEM ON THE SECOND NIGHT.

        IF YOU SEE HOUSES WITH NO DOORS, LOOK AT THE YELLOW LINE. NOT THE WINDOWS. THE LINE.

        IF A GREEN WAGON IS IDLING ON THE ROAD, THAT IS ALICE. SHE WAITED FOR ME. OPEN THE BACK.

        APOLLO OUT""".stripIndent()));}

    // ------------------------------------------------------------------------------------------------ the CB
    /** The side a night starts on: left, right, left, right. */
    public static int firstTurn(int visit){return visit%2==1?0:1;}
    public static String side(int turn){return turn==0?"left":"right";}
    /** Apollo on the CB, for the night, the junction ahead and what the road is doing. */
    public static String radio(int visit,int junction,int expect,int mode){
        if(mode==LeftRightRoad.CLOSED)return "Apollo: That's the road for tonight. They've put the barriers out. Go on home. Over.";
        if(mode==LeftRightRoad.WAGON_END)return "Apollo: That's her. That's Alice. She waited. Open the back and get in. Over.";
        if(visit==2&&junction==2)return "Apollo: Left's chained at this one. The key's in the phone box down the right. Put a flare on the divider before you go right or the salt'll have you. Over.";
        if(visit==3)return "Apollo: Jubilation. Houses with no doors. Don't you look at a window that's lit. Eyes on the yellow line. It's "+side(expect)+". Over.";
        if(visit==4)return "Apollo: Ice on the road and the signs all bent. Doesn't matter what they say. It's "+side(expect)+". Over.";
        if(junction==1)return "Apollo: You're at the toll. Tonight it starts on the "+side(expect)+". Then the other way, every fork, never twice. Over.";
        return "Apollo: Next one's "+side(expect)+". Over.";
    }
    /** The squelch round a line, as the action bar shows it: an oscilloscope trace either side. */
    public static String cb(String line){return "∿∿ "+line+" ∿∿";}

    // ------------------------------------------------------------------------------------------------ Apollo's log
    /** Apollo's convoy log, one entry a night, read at the booth until the reader's own is ready there. */
    public static ItemStack log(int visit){
        String text=switch(Math.max(1,Math.min(4,visit))){
            case 1->"""
                CONVOY LOG. A.

                Night one. Bristol Ferry toll at two in the morning and nobody in the booth. I took the first fork left like the forum said and the second right, and the road kept going where there is no road on any map I own. Four forks and then barriers across both ways, like somebody knew I'd had enough.

                Went wrong once on purpose, to see. Salt flat, white as a plate, cars stacked at the end. A woman in a long coat by one of them, wet through, and it hasn't rained. I looked at her and she didn't move. I backed up without looking away and the salt let me go.""";
            case 2->"""
                CONVOY LOG. A.

                Night two. Bonnie's number rang in the phone box at the Bluejay cut. I know it was hers. Nobody else has it. The left was chained and the key was hanging in the box, on the wrong side, the side that goes to salt.

                Lit a flare on the divider and went right anyway. The box was filling with water from nowhere. Bonnie talked the whole time and her words came up on the glass, backwards, in the steam off the water. I got the key. I got back before the flare went out.""";
            case 3->"""
                CONVOY LOG. A.

                Night three. Jubilation. Pretty houses, pink and mint and butter, and not one of them has a front door or a window you could look in at. Only the little windows up in the roofs. When they light up you want to look, and once you look your legs go slow and you can't stop.

                I looked at the yellow line instead. Down at my feet, the whole way. Somebody moved behind every curtain I didn't look at.""";
            default->"""
                CONVOY LOG. A.

                Night four. Freezing rain and the road broken up like a dropped plate. The signs bent over and pointing at the ground. Two forks and then the road just goes down, into a hole full of ice.

                Alice's wagon was there, on our side of the hole, engine running, hazards going. She waited. The tailgate's how you get in. I'm writing this in the back.

                If you're reading this at the booth, I didn't come back for the log. I'm not coming back for it.""";
        };
        return HouseWriting.book("Convoy log","A.",plain(text.stripIndent()));
    }

    // ------------------------------------------------------------------------------------------------ Bonnie
    /** Bonnie on the payphone, three things, her words backwards on the glass. */
    public static String bonnie(int line){return switch(line){
        case 0->"Is this the box at the cut? Is anybody there? Apollo said somebody would answer.";
        case 1->"He said you'd need the key. He said you'd want to stay on the line with me. Don't. Take it and go.";
        default->"The water's coming up at my end too. Go back the way you came while your flare's still burning.";
    };}
    public static String backwards(String line){return new StringBuilder(line).reverse().toString();}

    // ------------------------------------------------------------------------------------------------ the account
    static String night(int visit){return switch(visit){case 1->"Night one, from the Bristol Ferry toll";case 2->"Night two, the Bluejay cut";case 3->"Night three, Jubilation";default->"Night four, the sinkhole";};}
    static String turns(String letters){
        var parts=new ArrayList<String>();for(char c:letters.toCharArray())parts.add(c=='L'?"left":"right");
        return parts.isEmpty()?"no forks":String.join(", ",parts);
    }
    static String times(int n){return switch(n){case 1->"once";case 2->"twice";case 3->"three times";default->n+" times";};}
    public static String accountText(String name,CompoundTag own){
        var b=new StringBuilder("A road log in my own hand, kept the way Apollo kept his.\n\n");
        for(int v=1;v<=4;v++){String t=own.getString("Turns205_"+v);if(t.isEmpty()&&v<4)continue;
            b.append(night(v)).append(": ").append(turns(t)).append(".");
            if(v==2&&own.getBoolean("Key205"))b.append(" I lit a flare on the divider and went the wrong way for a key, and came back for the chained gate.");
            if(v==2&&own.getInt("Bonnie205")>0)b.append(" A woman called the phone box. Written out the right way round, she said: ").append(bonnie(Math.min(2,own.getInt("Bonnie205")-1))).append("");
            if(v==3)b.append(own.getInt("Locks205")>0?" The lit windows held me "+times(own.getInt("Locks205"))+" before I looked down at the line.":" I kept my eyes on the yellow line the whole way.");
            if(v==4)b.append(" Past the place where the road fell in, a green wagon was idling with its hazards on. I let the tailgate down.");
            b.append("\n\n");
        }
        int sacs=own.getInt("Sacs205"),backed=own.getInt("Backed205"),lunged=own.getInt("Lunged205");
        if(sacs>0)b.append("I turned the wrong way ").append(times(sacs)).append(" and walked out onto the salt. She was by the cars every time.").append(backed>0?" I kept my eyes on her and backed out "+times(backed)+".":"").append(lunged>0?" "+(lunged==1?"Once":"Some of those times")+" I looked away, and she was there before I had turned my head, and I was in the House again with the night to do over.":"").append("\n\n");
        else b.append("I never once turned the wrong way. I never saw her.\n\n");
        b.append("Apollo's last entry is folded into the back: I'm not coming back for it.");
        return b.toString();
    }
    public static ItemStack account(String name,CompoundTag own){return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.LEFT_RIGHT),name,plain(accountText(name,own)));}
    public static final String HINT="Left, then right, never the same way twice; the CB says which side a night starts on. On the salt, keep your eyes on her and back out.";
}
