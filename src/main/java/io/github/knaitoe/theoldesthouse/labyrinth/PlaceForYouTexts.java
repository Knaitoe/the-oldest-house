package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.257, A Place for You (docs/A_PLACE_FOR_YOU_0_4_257.md): the place card (the only paper anyone has to read), the welcome's one
 * remembered thing, and the reader's account. Original writing for the owner's proposal. The account says only what the reader did and
 * what they were told; it never says what any of it proves, and never names the reader's gender, age or colour.
 */
public final class PlaceForYouTexts {
    private PlaceForYouTexts(){}
    private static final int PAGE=150;
    static List<Component> styled(HouseWriting.WritingStyle style,String text){var out=new ArrayList<Component>();for(String p:LookoutTexts.pages(text,PAGE))out.add(HouseWriting.page(style,p));return out;}

    public static final String HINT="Stay for supper. Whatever they think they remember, you can answer for yourself.";
    public static final String AFTER="The house across the road. You know which window is the kitchen's.";
    /** The arrival, by how the reader once answered the porch whistle; every way reaches the same house. */
    public static String arrival(String home){
        return switch(home){
            case "HAVEN"->"Across the road, a screen door stands open on a side porch. Someone has kept a plate warm.";
            case "DANGER"->"Across the road, somebody on a side porch is watching the gate. They prop it open with a foot.";
            case "RUNAWAY"->"Somebody is standing in the road, as if waiting to see which way you'll go.";
            default->"A house across the road, every downstairs window lit. The front path is clear all the way to the door.";
        };
    }

    /** The place card at the reader's place, folded once: their name on the front in Mara's hand, and inside, where they always sit. */
    public static ItemStack source(String name){
        var hand=HouseWriting.WritingStyle.KAREN;var pages=new ArrayList<Component>();
        pages.add(HouseWriting.page(hand,"\n\n\n      "+name));
        pages.add(HouseWriting.page(hand,"Your usual place.\n\nNext to Kit. Across from Theo.\n\nSame as always.\n\n    M."));
        return HouseWriting.book("A place card",name,pages);
    }

    /** The routine item's name, as the quiet rooms' bathroom knew it. */
    public static String routine(int item){return item<0?"washcloth":HouseReadMoments.ROUTINES[Math.floorMod(item,HouseReadMoments.ROUTINES.length)];}
    /** Who the stranger under the eaves was, here: Alex was the man, Ren the woman. */
    static String stranger(String stood){return switch(stood){case "man"->"Alex";case "woman"->"Ren";case "middle"->"Alex and Ren";default->"";};}

    /** The one small thing the snapshot lets Mara know, as its kind (or empty for none). */
    public static String detail(CompoundTag snap){
        if(!snap.getBoolean("Read"))return "";
        if(snap.getInt("Routine")>=0&&snap.contains("Routine"))return "routine";
        String glass=snap.getString("Glass");if(glass.startsWith("turned")||glass.startsWith("covered"))return "glass";
        if(snap.getString("Stood").equals("lantern"))return "lantern";
        if(!stranger(snap.getString("Stood")).isEmpty())return "eaves";
        if(snap.contains("Drawing"))return "drawing";
        return "";
    }
    /** Mara's line for that thing. */
    public static String detailLine(String kind,CompoundTag snap){
        return switch(kind){
            case "routine"->"I put your "+routine(snap.getInt("Routine"))+" out in the bathroom. I left it where your hand went last time. Unless you'd rather I moved it.";
            case "glass"->snap.getString("Glass").startsWith("covered")?"I put a cloth over the hall glass for you. I remembered. Unless you'd rather I didn't.":"I turned the hall glass to the wall for you. I remembered. Unless you'd rather I didn't.";
            case "lantern"->"Somebody saw you out by the lantern, in the rain. I thought, that one will want a warm room.";
            case "eaves"->stranger(snap.getString("Stood"))+(snap.getString("Stood").equals("middle")?" said":" said")+" you stood with them under the eaves, in the rain. I've been hearing about it all afternoon.";
            case "drawing"->"Your picture's on the fridge. I didn't like to move it.";
            default->"Come in, it's cold. The kettle's on.";
        };
    }
    /** What Mara offers, by a settled waiting-room read; a place to sleep otherwise. It changes nothing a reader can do. */
    public static String offer(String age){
        return switch(age){
            case "DIGITAL"->"Your room's still made up. It always is.";
            case "TRANSITIONAL"->"We've been waiting for you to sort us out. You always know what to do with us.";
            case "ANALOG"->"Sit down, sit. We'll do all that. You don't have to lift a thing.";
            default->"Stay the night, at least. The spare room's made up.";
        };
    }

    /** What the reader sees when it is over. */
    public static String ended(String outcome,CompoundTag own){
        if(outcome.equals("took_the_road"))return "The road goes on into the trees. Behind you, the house is exactly as you left it.";
        return own.getBoolean("LightOff")?"The kitchen light goes out. Nobody is left to switch it on.":"The kitchen light is still on. One chair is empty.";
    }

    static String pron(String who){return who.equals("Mara")?"she":"they";}
    static String Pron(String who){return who.equals("Mara")?"She":"They";}

    /** The account, in the reader's own words. */
    public static String afterwards(String reader,CompoundTag own){
        var t=new StringBuilder();var snap=own.getCompound("Snap");String outcome=own.getString("Outcome");
        t.append("I was expected at a house across the road from an old garden wall. A woman named Mara had set a place for me, with my name on a card in her hand.\n\n");
        String claim=own.getString("Claim"),detail=own.getString("Detail");
        switch(claim){
            case "family"->t.append("She said it was good to have me back, and I said it was good to be back.");
            case "guest"->t.append("She talked as if I had lived there. I told her I didn't think I had. She said she was sorry, and got small things wrong all evening, and after a while stopped correcting herself.");
            default->t.append("I said I would stay for supper, and left it at that.");
        }
        switch(detail){
            case "routine"->t.append(" She had put my "+routine(snap.getInt("Routine"))+" out in the bathroom, where my hand had gone once before.");
            case "glass"->t.append(snap.getString("Glass").startsWith("covered")?" The hall glass already had a cloth over it.":" The hall glass was already turned to the wall.");
            case "lantern"->t.append(" Somebody there remembered me out by a lantern, in the rain.");
            case "eaves"->t.append(" "+stranger(snap.getString("Stood"))+" remembered me under the eaves, in the rain.");
            case "drawing"->t.append(" My picture was on her fridge.");
            default->{}
        }
        if(!detail.isEmpty()&&claim.equals("guest"))t.append(" I hadn't told her any of it.");
        t.append("\n\n");
        switch(own.getString("Activity")){
            case "plates"->t.append("I cleared the plates with Mara. She had heated the supper once already that evening, she said, and it was better the first time.\n\n");
            case "clasp"->t.append("I mended the clasp on Kit's bag. Kit laughed at their own way of explaining it.\n\n");
            case "tea"->t.append("Ren and I made tea, and Ren couldn't find the cups in a kitchen they had been living in for weeks.\n\n");
            case "radio"->t.append("I sat with Alex by the radio. It only played songs from before.\n\n");
            default->{}
        }
        String helper=own.getString("Helper");
        switch(own.getString("Care")){
            case "help"->t.append("In the bathroom I asked "+helper+" to help me, and "+pron(helper)+" did. ");
            case "company"->t.append("In the bathroom "+helper+" sat on the edge of the tub and talked about nothing while I did it myself. ");
            case "private"->t.append("In the bathroom I asked to be on my own, and "+helper+" shut the door. ");
            default->{}
        }
        if(own.getBoolean("Repeated")){t.append("When I had put it away, it was laid out again, ready for the same moment.");if(!own.getString("Care").equals("private")&&!helper.isEmpty())t.append(" "+helper+" hadn't moved it. "+Pron(helper)+" saw it too.");t.append("\n\n");}
        if(own.getBoolean("BagSeen")){
            t.append("Kit had packed a bag and left it at the gate. Mara carried it back in.");
            if(own.getBoolean("KitPlan"))t.append(" Kit told me about a room above a workshop, and a course, and a letter that might be from any year.");
            switch(own.getString("BagWhere")){
                case "unpacked"->t.append(" I helped Mara unpack it.");
                case "gate"->t.append(" I took it back out to the gate.");
                case "kit"->t.append(" I gave it back to Kit.");
                case "mara"->t.append(" I handed it to Mara.");
                default->{}
            }
            if(own.getBoolean("AskedWait"))t.append(" I asked if it could wait until morning. Kit said sure.");
            if(own.getBoolean("NotMine"))t.append(" I said it shouldn't be me deciding whether somebody else leaves. Kit said they had only wanted me to hold the door.");
            if(own.getBoolean("DoorTaken"))t.append(" After that, I watched Kit's door become a cupboard door.");
            t.append("\n\n");
        }
        if(own.getBoolean("TheoTold")){
            t.append("At the sink Mara told me about her brother, Theo, who used to dry and put everything in the wrong cupboard. She said she didn't want another conversation in an empty kitchen.");
            switch(own.getString("Promise")){
                case "company"->t.append(" I told her: \"I'll sit with you tonight.\""+(own.getBoolean("PromiseKept")?" I did.":outcome.equals("took_the_road")?" I didn't.":""));
                case "listen"->t.append(" I listened, and didn't promise anything.");
                case "kit"->t.append(" I asked her what Kit wanted. She said she knew what Kit wanted.");
                default->{}
            }
            t.append("\n\n");
        }
        if(own.getInt("Chapter")>=PlaceForYou.DINNER){
            t.append("When we sat down again, the table was laid exactly as it had been.");
            if(own.getBoolean("SpoonTaken"))t.append(" Mara couldn't stop setting it, and I took the spoon out of her hand.");
            if(own.getBoolean("DoorHeld"))t.append(" The front door kept swinging shut ahead of me. I held it.");
            if(own.getBoolean("RoutineStopped"))t.append(" In the empty bathroom my routine was going on without me. I put my hand on it and it stopped.");
            if(own.getBoolean("CupboardOpened"))t.append(" I opened Kit's cupboard: shelves, and Kit's things folded on them, and Kit behind me in the hall, saying my name.");
            t.append("\n\n");
        }
        switch(own.getString("Confront")){
            case "open"->t.append("I said it to everyone at the table. Mara said she had only wanted one more evening like this one. Ren said it had been a lot more than one.\n\n");
            case "private"->t.append("I asked Mara to talk in the other room. She said she wasn't keeping anyone. She was keeping the evening, and they were in it.\n\n");
            case "kit"->t.append("I told Kit to go while I kept the others talking.\n\n");
            default->{}
        }
        if(outcome.equals("took_the_road")){
            t.append("I said it was more than I could take on, and went out through the gate onto the road.");
            if(own.getString("Promise").equals("company"))t.append(" I had told Mara I would sit with her tonight.");
            if(own.getBoolean("KitLeft"))t.append(" Kit was already outside.");
            t.append(" The house stayed exactly as I left it.");
            return t.toString();
        }
        t.append("I set the hall chair against the front door, and took my picture down off the fridge.");
        if(own.getBoolean("KitLeft")||own.getBoolean("KitGoing"))t.append(" Kit went out through the gate with the bag.");
        switch(own.getString("MaraEnd")){
            case "sat"->t.append(" Mara asked me to sit with her before she decided. I did. She said she would stay a while, and try it, and see.");
            case "gate"->t.append(" Mara walked Kit to the gate. Just to the gate, she said. Then she kept going.");
            default->t.append(" Mara stayed. It was still her kitchen, she said.");
        }
        switch(own.getString("AlexEnd")){
            case "walk"->t.append(" Alex was going to see whether the bus still ran to the coast. I walked with them as far as the road.");
            case "alone"->t.append(" Alex was going to see whether the bus still ran to the coast. They asked if I wanted to walk a while. I said no, and they said that was a whole answer.");
            default->{}
        }
        t.append(own.getString("RenEnd").equals("goes")?" Ren went with them as far as the bend.":" Ren stayed with Mara.");
        t.append(own.getBoolean("LightOff")?"\n\nThe last one out turned off the kitchen light.":"\n\nWhen I looked back from the road, the kitchen light was on, and one chair was empty.");
        return t.toString();
    }
    public static ItemStack account(String reader,CompoundTag own){
        var pages=new ArrayList<Component>(styled(HouseWriting.WritingStyle.PLAIN,afterwards(reader,own)));
        return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.PLACE_FOR_YOU),reader,pages);
    }
}
