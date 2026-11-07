package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.333, State Route 9: the note wrapped round the brass key in the mailbox (the source, the owner's found text, verbatim), the
 * postcard of the county line, and the account each reader carries off the road. The account says only what that reader's own record
 * holds: whether they crossed for the key, crossed with someone, or watched from the fence; how often the road put them back; what the
 * median was like; whether they looked into the wagon, and whether the heads in it turned. The route sign is the one the House read
 * them for. House writing: never the reader's gender, never a colour, never an age.
 */
public final class StateRouteTexts {
    private StateRouteTexts(){}
    public static final String TITLE="The account: the median";
    public static final String NOTE_TITLE="Round the key";
    public static final String HINT="Judge the gap by the sound and the shine on the tar, not the lights. Get down in the weeds in the middle.";
    private static List<Component> pages(HouseWriting.WritingStyle style,String text){var out=new ArrayList<Component>();for(String p:LookoutTexts.pages(text,150))out.add(HouseWriting.page(style,p));return out;}

    /** The owner's found text, as written; it was wrapped round the key. */
    public static final String NOTE="We weren't ever allowed to cross the 9 before fourth grade, even in July when you could see all the way down to the grain elevators. "
        +"My mother said the logging trucks come off the grade with their brakes hot, and by the time you hear the tires singing on the tar, the wind off the bumper already has you by the coat.\n\n"
        +"You stand in the gravel waiting for a dark spot between the headlights, counting one-one-thousand, two-one-thousand until your knees lock up. Making it to the weeds in the middle is the easy part. "
        +"It’s crouching down in the ditch gravel with the exhaust blowing warm in your face from both directions—realizing you still have two more lanes to clear before the streetlights go out—that makes you wish you’d stayed on the porch.";
    public static ItemStack source(){return HouseWriting.book(NOTE_TITLE,"Somebody who grew up by the 9",pages(HouseWriting.WritingStyle.WILL,NOTE));}

    /** For a reader the House read as one who takes the road. */
    public static ItemStack postcard(){
        return HouseWriting.book("The county line","Unsent",pages(HouseWriting.WritingStyle.WILL,
            "On the front, the sign at the county line, taken from the shoulder at night.\n\nOn the back, no address. Only: Made it this far. The rest is just walking."));
    }

    static String words(int n){return Stillness.words(n);}
    static String times(int n){return n==1?"once":n==2?"twice":words(n)+" times";}

    /** The reader's own account, from their record only. */
    public static ItemStack account(String name,CompoundTag t){
        var text=new StringBuilder();
        String role=t.getString("RouteRole");String runner=t.getString("RouteRunner");
        boolean analog=t.getString("RouteAge").equals("ANALOG"),digital=t.getString("RouteAge").equals("DIGITAL");
        switch(role){
            case "runner"->text.append("The key was in a mailbox across the 9, under the one streetlight, with its flag up. The gate on my side of the road was padlocked, and I went across for it. ");
            case "crossed"->text.append("The key was in a mailbox across the 9, under the one streetlight. The gate on our side was padlocked. I crossed too. ");
            default->text.append("The key was in a mailbox across the 9, under the one streetlight. The gate on our side was padlocked. I stayed at the fence. ");
        }
        if(t.getInt("RouteCalls")>0)text.append("I watched the traffic from the gravel and called the gaps out loud, "+times(t.getInt("RouteCalls"))+". ");
        if(t.getBoolean("RouteHeld"))text.append("I held the gate as far open as the chain would let it, which was a hand's width. ");
        if(t.getInt("RouteStruck")>0)text.append(t.getInt("RouteStruck")==1?"The first time I misjudged it. I heard brakes, and then nothing, and then the gravel against my cheek back where I had started. ":"I misjudged it "+times(t.getInt("RouteStruck"))+". Each time I heard brakes, and then nothing, and then the gravel back where I had started. ");
        if(t.getInt("RoutePickup")>0)text.append("A pickup I had thought slow enough clipped me and put me down on the shoulder. ");
        if(t.getInt("RouteWake")>0)text.append(t.getInt("RouteWake")==1?"One truck missed me and still had me by the coat as it went by. ":"Even the trucks that missed me took hold of my coat as they went by. ");
        if(t.getBoolean("RouteCrossed")){
            if(t.getBoolean("RouteCrouched"))text.append("In the median I got down in the dead weeds with the trucks going past in front of my face and behind my back at once, and the exhaust warm from both sides. ");
            else if(t.getBoolean("RouteStood"))text.append("In the median I stayed on my feet, and the trucks on either side shook me like washing on a line. ");
            else if(t.getBoolean("RouteMedian"))text.append("I stopped in the median, in the dead weeds, and waited for the second half. ");
            text.append(analog?"By the mailbox the route shield was the old painted kind, steel, the number stencilled on. ":digital?"By the mailbox the route sign was one of the new aluminum ones that throw your own light straight back at you. ":"");
        }
        if(t.getBoolean("RouteKey"))text.append("The key had a note wrapped round it, somebody else's account of the same road. ");
        if(t.getBoolean("RoutePostcard"))text.append("There was a postcard in with it, the county line, never mailed. After that my legs knew how to cross. ");
        if(t.getBoolean("RouteCrawl")){
            text.append(t.getBoolean("RouteKey")?"When I took the key every engine on the road dropped to a crawl, bumper to bumper, ticking in the rain. ":"Then every engine on the road dropped to a crawl, bumper to bumper, ticking in the rain. ");
            if(t.getBoolean("RouteBetween"))text.append("Coming back I went between the bumpers, through the headlights, close enough to put a hand on them. ");
            if(t.getBoolean("RouteWagon")){
                text.append("Every car was empty but one, a station wagon with wood along its sides, every seat full of people sitting quite still. ");
                if(t.getBoolean("RouteUnseen"))text.append("While I kept low in the weeds they never turned. When I stood up to run, every head came round. ");
                else text.append("Every head in it turned to watch me squeeze past the grille. ");
            }
        }
        switch(role){
            case "runner"->text.append("At the gate I put the key in the padlock. Every horn on the highway went at once, one long chord, and then every light on the road went out.");
            default->text.append((runner.isEmpty()?"Someone":runner)+" put the key in the padlock. Every horn on the highway went at once, one long chord, and then every light on the road went out.");
        }
        text.append("\n\nNothing on the 9 moved after that. I went through the gate and didn't look back at it.");
        var pages=new ArrayList<Component>();pages.addAll(pages(HouseWriting.WritingStyle.WILL,text.toString().trim()));
        return HouseWriting.book(TITLE,name,pages);
    }
}
