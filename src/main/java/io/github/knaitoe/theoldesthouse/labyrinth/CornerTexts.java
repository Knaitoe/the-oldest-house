package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.408, The House on the Corner: the exercise book left on the kerb (the first paper), the girl's stories about her arm, and the
 * account. The source story is in copyright: no names, no text and no title from it; every line here is original.
 */
public final class CornerTexts {
    private CornerTexts(){}
    public static final String HINT="The girl on the kerb wants someone to go in with her.";
    /** What she says happened to her arm, a different one each time she is asked, in the order she tells them. */
    static final String[] STORIES={
        "A dog took it. A big white one, at my grandmother's. It didn't even bite, it just took it and ran off into the field, and nobody could catch it.",
        "I was born like this. Nacho was born first and he was greedy, so I gave it to him. He's got three, really. You just can't see the third one.",
        "A train. I had it out of the window and we went into a tunnel. Don't tell Nacho. He thinks it was the dog.",
        "There was a man at the fair who guessed what you weighed. He got me wrong, so he had to give me something, and I gave him the arm instead. I'm getting it back when I'm older.",
        "I put it in the river to see if it would float. It did. It went all the way to the sea.",
        "I keep it somewhere safe. I'm not telling you where."};
    /** How each story is remembered in the account. */
    static final String[] REMEMBERED={
        "A dog had taken it at her grandmother's and run off into a field with it.",
        "She had given it to her brother, because he was born first and greedy.",
        "A train had taken it, in a tunnel, and her brother was not to know.",
        "She had given it to a man at a fair, and she would have it back when she was older.",
        "She had put it in the river to see if it floated, and it had gone all the way to the sea.",
        "She was keeping it somewhere safe, and she would not say where."};
    /** Told once, only to a reader who has lost an arm in the House: the only time she tells it. */
    static final String TRUE_STORY="We match. ... Mine wasn't anything. I was very small and I had a fever, and they took it so the rest of me could stay. That's all. I don't tell that one. It's boring.";

    private static List<Component> pages(HouseWriting.WritingStyle style,String... texts){var out=new ArrayList<Component>();for(String text:texts)for(String p:LookoutTexts.pages(text,150))out.add(HouseWriting.page(style,p));return out;}
    public static ItemStack source(){
        return HouseWriting.book("An exercise book","R.",pages(HouseWriting.WritingStyle.PLAIN,
            "Composition. Something that happened to me.\n\nMy arm. A dog took it at my grandmother's, a big white one, and ran off into the field with it and nobody\n\nThe rest is crossed out, hard enough to go through the page.",
            "Composition. Something that happened to me.\n\nMy brother was born first and he was greedy so I\n\nCrossed out.",
            "Composition. Something that happened to me.\n\nOn the corner of our street there is a house nobody lives in. Nobody ever has. It isn't for sale. The lights are on and nobody pays for them. I go past it every day with Nacho and Luli. One day I am going to go all the way in.\n\nUnderneath, in the teacher's hand: Better. Stay on the subject."));
    }

    public static ItemStack account(String name,CompoundTag own){
        var p=new ArrayList<String>();
        p.add("There was a house on the corner of a street at siesta. Nobody had ever lived in it. The shutters were down all along the street, and the jacarandas had dropped their flowers in the gutters.");
        p.add("A girl from the kerb took me in. She had one arm, and every time I asked about the other one she told me something different.");
        var told=own.getList("Stories",3);var heard=new StringBuilder();
        for(int i=0;i<told.size();i++){int s=told.getInt(i);if(s>=0&&s<REMEMBERED.length)heard.append(heard.isEmpty()?"":" ").append(REMEMBERED[s]);}
        p.add(heard.isEmpty()?"I never asked her about it.":heard.toString());
        if(own.getBoolean("TrueStory"))p.add("Once she looked at my arm, at where my arm had been, and said we matched. Then she told me what really happened to hers: a fever, when she was very small. She said that one was boring and she never told it.");
        String house=own.getBoolean("Jars")?"In the front room, which was clean and lit though the power had been off for years, there were jars on boards along the wall: teeth in one, fingernails in another, and one I could not make out.":"The front room was clean and lit, though the power had been off for years.";
        if(own.getBoolean("Hall"))house+=" A corridor at the back went on for longer than the house did.";
        p.add(house);
        p.add(own.getBoolean("HeldToDoor")?"I held her hand all the way to the last door. She let go of it there and told me to wait.":own.getBoolean("HeldEver")?"I held her hand for a while. Then I let go.":"I didn't take her hand.");
        String end=own.getBoolean("SawGo")?"She went into the last room, and the door shut behind her.":"She went into the last room.";
        end+=switch(own.getString("Done")){
            case "opened"->" I opened the door. The room was empty, and smaller than the door was. There was nowhere in it to go.";
            case "waited"->" I went outside and waited, with her brother at the gate. After a while the front door of the house opened onto the street. Nobody came out.";
            default->"";
        };
        p.add(end);
        p.add("When I came out the garden had grown over the path. "+(own.getBoolean("TrueStory")?"Hers is the only true story I know about that house.":"I don't know which of her stories was true."));
        return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.CORNER_HOUSE),name,pages(HouseWriting.WritingStyle.WILL,p.toArray(String[]::new)));
    }
}
