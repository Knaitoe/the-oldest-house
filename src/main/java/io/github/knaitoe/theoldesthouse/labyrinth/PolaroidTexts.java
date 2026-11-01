package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.204, the Polaroid Trail: the four balloon tags (the reader's own, from a class balloon release, each come back with
 * something written on its back in a small, careful hand), Josh's walkie-talkie, the captions on the developed photographs,
 * and the account, which is the album the photographs were kept in, captioned in the reader's own hand. All of it is original.
 * House writing: never the reader's gender, never a colour of theirs, never an age.
 */
public final class PolaroidTexts {
    private PolaroidTexts(){}
    private static final int PAGE=150;

    /** The first tag, on the porch rail: the source. */
    public static ItemStack source(){return tag(1,"(your name)");}
    public static ItemStack tag(int n,String name){
        String front="""
            HELLO! This balloon was let go by Room 6 at Linden Street School, all of us at once, at lunch.

            My name is %s.

            If you find this balloon please put this tag in the mail or call the school and say where it landed. I hope it goes really far.""".formatted(name);
        String back=switch(n){
            case 1->"On the back, small and careful, in pencil:\n\nIt came down in my yard. I kept it. I keep all of them.";
            case 2->"On the back, the same small hand:\n\nYou crawled right past where I sleep. You did not even look. I washed the bowl for her and she never came back.";
            case 3->"On the back:\n\nYou always sit at the front. I always sit behind you. Nobody ever turns round in a cinema.";
            default->"On the back:\n\nCome to where the trees are. Bring your friend. I have dug a place for each of you.";
        };
        var pages=new ArrayList<Component>();
        for(String p:LookoutTexts.pages(front,PAGE))pages.add(HouseWriting.page(HouseWriting.WritingStyle.CHILD,p));
        for(String p:LookoutTexts.pages(back,PAGE))pages.add(HouseWriting.page(HouseWriting.WritingStyle.PLAIN,p));
        return HouseWriting.book(n==1?"A balloon tag":"A balloon tag, come back","Room 6",pages);
    }

    /** Josh, over the walkie-talkie, after each sightline is found. Each line is static, his voice, and static again. */
    public static String josh(String shot){return switch(shot){
        case "start"->"Josh: You there? Over. I can see your porch light from my window. Over.";
        case "creek"->"Josh: There's a grate at the end of the big pipe. It's never been open. Somebody opened it. Over.";
        case "bowl"->"Josh: That's Mrs Maggie's cat's bowl. The cat went missing in the spring. Keep going. Over.";
        case "clothes"->"Josh: Those are my dad's shirts. And the sheets off our line. There's a hatch. Don't go down it. Over.";
        case "cinema"->"Josh: Don't sit at the front. Over. Don't sit at the front.";
        case "power"->"Josh: The fire door. Go. Over. Go.";
        case "pines"->"Josh: I'm in the trees. I can see the trenches. One of them is the right size for me. Over.";
        case "trench"->"Josh: Get in the one on the left. Then look up. Look up. Over.";
        case "end"->"Josh: I see the balloons from here. All of them. Come home now. Over and out.";
        default->"Josh: ...over.";
    };}
    /** A crackle of static round a line, as the action bar shows it: waveform bars either side. */
    public static String walkie(String line){return "▁▃▅▇ "+line+" ▇▅▃▁";}

    /** The caption a photograph carries, written on its white border. */
    public static String caption(String shot){return switch(shot){
        case "creek"->"you, in the creek. from the pipe.";
        case "bowl"->"you, under the house. from behind the bricks.";
        case "clothes"->"you, at my bed.";
        case "c1"->"you, at the front. me in K.";
        case "c2"->"you, at the front. me in H.";
        case "c3"->"you, at the front. me in E.";
        case "c4"->"you, at the front. me in B.";
        case "p1","p2","p3"->"you, in the trees.";
        case "p4"->"the top of your head.";
        case "hall"->"you, in the hall. from behind.";
        default->"you.";
    };}

    // ------------------------------------------------------------------------------------------------ the account
    private static String times(int n){return switch(n){case 1->"once";case 2->"twice";case 3->"three times";default->n+" times";};}
    public static String accountText(String name,CompoundTag own){
        var b=new StringBuilder();
        b.append("A photo album with a green cover. My own captions are in it, in pencil, under photographs I did not take.\n\n");
        var order=List.of("creek","bowl","clothes","c1","c2","c3","c4","p1","p2","p3","p4");
        var kept=new LinkedHashSet<String>();var list=own.getList("Shots204",Tag.TAG_COMPOUND);
        for(int i=0;i<list.size();i++){var s=list.getCompound(i);if(s.getInt("State")>=2)kept.add(s.getString("Id"));}
        for(String id:order){if(!kept.contains(id))continue;
            b.append(switch(id){
                case "creek"->"The first: me in the creek, up to my ankles, looking at the raft. Taken from inside the culvert. I stood in the creek again and looked back at the pipe, and the grate at the end of it was open.";
                case "bowl"->"Me under Mrs Maggie's house, on my elbows, by her cat's bowl. Taken from behind a pier. The water in the bowl was fresh.";
                case "clothes"->"Me at the heap of clothes he sleeps in. There was a shirt in it the same as the one I had on that evening on the porch.";
                case "c1"->"The cinema: me at the front, the screen lit behind me. Someone in row K.";
                case "c2"->"The same, from the booth. Row H.";
                case "c3"->"Row E.";
                case "c4"->"Row B. When I turned round he was in A, right behind where I had been standing, and the power went.";
                case "p1"->"The trees, and me between them, small.";
                case "p2"->"The trees again, from higher.";
                case "p3"->"The two trenches, and me between them.";
                case "p4"->"The top of my head, from straight above, in the trench on the left. I looked straight up and there was nobody in the branches, only balloons.";
                default->"";
            }).append("\n\n");
        }
        if(own.getInt("Caught204")>0)b.append("Some of the pages are white all over, and nothing else: ").append(times(own.getInt("Caught204"))).append(" I looked at him while the flash went off, and the visit went white and put me back on the porch.\n\n");
        b.append("The last page is Josh's walkie-talkie, taped in. Its light still comes on when I talk into it.");
        return b.toString();
    }
    public static ItemStack account(String name,CompoundTag own){
        var pages=new ArrayList<Component>();
        for(String p:LookoutTexts.pages(accountText(name,own),PAGE))pages.add(HouseWriting.page(HouseWriting.WritingStyle.PLAIN,p));
        return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.POLAROID_TRAIL),name,pages);
    }
    public static final String HINT="Every flash leaves a photograph under a yellow balloon. Stand where it was taken and look back at the camera. Never look at the flash.";
}
