package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.nbt.CompoundTag;

/** Original adaptation dialogue, saved separately for every speaker on every filming day. */
public final class GhostsConversations {
    private GhostsConversations() {}
    // 0.4.242 (owner: "try to cut the ai-isms from the vignette"): people talking, not summing up.
    private static final String[][][] WORDS={
        {
            {"Merry gets to veto the ending. That's the arrangement. She gives terrible notes.","Ask her about the ants. She'll say it was her idea. Let her."},
            {"I already know where the little bug is on every page. I'm checking that he hasn't moved.","She tells it and I write it in. If I don't like the ending, she has to fix it."},
            {"Can you get both of them together? Before you ask about all the other stuff.","I'm home for now. They keep asking when I'm going back, like I wouldn't go tomorrow if I could."},
            {"Those are Merry's actual books. Please don't carry them off with the other things.","Her appointments don't stop because you're here. Someone needs to remember that."},
            {"Let Merry finish her answer. Don't feed her the thing her sister just said.","The bedroom happened before we arrived. Put that on the log before anybody forgets."},
            {"John asked me to come. I haven't examined his daughter yet.","I need to hear what they saw separately. A conversation round the table isn't that."}
        },
        {
            {"You'd think I'd promised her a crown. It's a hat, Merry.","Are you watching me or Dad? Pick one. You'll miss the other."},
            {"Butter and cheese on mine. Mom knows. Everybody knows. They still ask.","She said I could borrow it. Well, she didn't actually say yes. She didn't say no either."},
            {"We can manage one meal together. Sit down, Merry. It'll be all right.","Don't come any closer with that. I can see my own hand, thanks."},
            {"John, let her eat before you start. We can talk afterwards.","Move the chair. I want to sit beside her. You're all standing over her."},
            {"Keep them all in if you can. Don't run round the table looking for the best face.","If you go low, stay back from the legs. There are fittings underneath."},
            {"I wasn't at that meal. I won't pretend a recording puts me there.","John has told me about his hand. I still need to speak with his daughter."}
        },
        {
            {"You want it again? How high was I supposed to get last time? Ask Dad.","Merry. I'm talking to you. You can stop staring at the floor."},
            {"Can I stay by the door? I can hear you from there.","When we do it again, am I supposed to be scared at the same bit?"},
            {"I had hold of her. That's what I remember. I wasn't counting the marks in the wall.","I'm right here, Marjorie. Nobody's asking you to fall."},
            {"No, Merry doesn't need to stand there again. You can get her from here.","She used her fingers on that plaster. Show that as well, if you're showing the rest."},
            {"This is a pickup. We have their accounts, not a missing second camera.","Leave the holes in the edge of the picture. Don't tighten past them."},
            {"A reconstruction can't answer the questions I have about that night.","John, this isn't permission for the next thing. We haven't finished talking."}
        },
        {
            {"I can put on a voice. That doesn't mean I can make the other ones shut up.","Merry knows when I'm kidding. Usually. Look, just give us a minute."},
            {"I know when she's doing the voice for me. Not always. I used to know.","She can come in my house if she asks. She can't just climb on it."},
            {"The bills still turn up. I know you think about that when I say we're doing this for her.","I heard her laughing down there. I hadn't heard that for a while."},
            {"The girls are downstairs together. Let them be downstairs together.","I'll put the food away. Apparently supper can wait as long as you need it to."},
            {"She asked for a private conversation. Ask again before you turn anything back on.","We're meant to get the bedroom after this. I'll wait till they come up."},
            {"Someone saying they can perform doesn't tell us why they need to.","I'd rather hear her finish than start another argument over the first thing she said."}
        },
        {
            {"I read the rules. Do I get a turn to ask you anything? No? Thought not.","When I say something boring, nobody asks me to repeat it."},
            {"I can go in the little room when they ask me. When I want to talk, it's always later.","I read things too. She doesn't have to tell me everything."},
            {"We've tried the appointments. I can't keep watching her get worse and do nothing.","Don't make it sound like I'm glad any of this is happening."},
            {"She has a computer, John. You know she has a computer. You bought it.","If you use his question, use my answer. Don't just leave me looking angry beside him."},
            {"Question, then answer. Keep them together, even if there's a long wait between them.","Sarah's speaking. Don't swing away because the priest's started over her."},
            {"An examination isn't a performance. I object to the crew being here too.","I need her own answer, John. Please stop answering for her."}
        },
        {
            {"Could somebody sit here? You can pray sitting down, can't you?","I didn't pull it. Look at where my hands are."},
            {"She said some of it was pretend. She didn't tell me which bits I was in.","If she asks me something, can I answer her? Mom?"},
            {"She's my daughter. I'm standing right here, whatever she says.","I thought we'd know by now. I thought somebody would know what to do."},
            {"Look at me, Marjorie. You can tell me to stop.","John, move. I need to get beside her."},
            {"Keep the drawer and their hands together. Don't give me a close-up I can't place.","Leave the stairs clear. Everybody stop trying to get ahead of her."},
            {"Stay where she can see you. Don't all crowd the bed.","John, let me speak. We've already gone too far to start shouting at each other."}
        },
        {
            {"Pass the pan over, will you? There's enough for me.","All that cheese and you won't try a little sauce? You're impossible, Merry."},
            {"NO.","WHY?"},
            {"Dinner first. You can shift the cases afterwards.","I'll pass it. You eat yours before it gets cold."},
            {"Butter on yours, Merry? There. The cheese is beside Dad.","Leave her plate alone. She'll ask if she wants anything else."},
            {"We're nearly out of your way. Last case, then the front door.","Don't get up. We can carry it round."},
            {"I can't stay for dinner. Thank you, Sarah.","I'll let you have the house to yourselves."}
        }
    };
    public static String line(CompoundTag own,int speaker){
        int day=Math.max(1,Math.min(7,own.getInt("FilmDay")))-1;
        if(speaker<0||speaker>=6)return "";
        String key="GhostTalk_"+(day+1)+"_"+speaker;int n=own.getInt(key);own.putInt(key,(n+1)%WORDS[day][speaker].length);
        if(day==6&&speaker==1&&own.getInt("ShotClock")>=630&&own.getInt("ShotClock")<820)return n%2==0?"You said we were going together.":"How long do I have to wait for you to wake up?";
        if(day==6&&speaker==1&&own.getInt("ShotClock")>=820)return own.getBoolean("MerryLeft0488")?"I can't go back into that room.":"Please. I need to get out of this house.";
        return WORDS[day][speaker][n%WORDS[day][speaker].length];
    }
    /** The last supper's cards precede Merry speaking again after the collapse. */
    public static boolean written(CompoundTag own,int speaker){return own.getInt("FilmDay")==7&&speaker==1&&own.getInt("ShotClock")<630;}
    public record Question(String prompt,String answer){}
    private static final Question[] QUESTIONS={
        new Question("Which interview comes first?","Merry's, if she'll have us. Let her tell it before she hears everyone else's version."),
        new Question("Where do you want me during supper?","Across from the family. You can move, but keep enough of the table to see who's reaching where."),
        new Question("Did you see the wall incident?","No. This is them showing us what they remember. Mark it that way."),
        new Question("Can we leave their conversation private?","Yes. Put yours down too. Don't tell me it was private and bring me a sound track."),
        new Question("What about Sarah's objection?","Keep it with what she's objecting to. She's in the room, same as him."),
        new Question("Why keep the drawer in view?","So we can see who could reach it. If we lose the hands, we lose that."),
        new Question("Are you all leaving?","Yes. Last case. We won't keep you from supper.")
    };
    public static Question question(int day){return QUESTIONS[Math.max(1,Math.min(7,day))-1];}
    private record Shot(String subject,String raw,String cut) {}
    private static final List<List<Shot>> SHOTS=List.of(
        List.of(
            new Shot("Marjorie","Marjorie's interview: the sisters and the vines, told her way.","Barry opens the episode with Marjorie's vines story, ahead of the bedroom reconstruction, like a warning."),
            new Shot("Merry","Merry's interview: the books, and the jug she put over her door.","Barry uses the jug over Merry's door to make her look like she's waiting for her sister to come for her."),
            new Shot("John","John explaining why they agreed to the show.","John's interview becomes the pitch: the show might help them."),
            new Shot("Sarah","Sarah asking for the girls' books back.","Sarah asking for the books back is cut down to a quick reaction shot.")),
        List.of(
            new Shot("Marjorie","Marjorie at supper, and her voice changing.","Barry stays on Marjorie: a hat, then the voice."),
            new Shot("Merry","Merry asking for the hat, and her face when Marjorie answers.","Barry builds supper around Merry's face and lets her silence run long after the threat."),
            new Shot("John","John at supper, and getting everyone to pray.","Barry makes supper about a father standing up to it at his own table."),
            new Shot("Sarah","Sarah trying to get to Marjorie.","Sarah is kept as reaction shots; her offer to stay with Marjorie is trimmed."),
            new Shot("HatExchange","Merry asking for the hat and Marjorie's answer, one after the other.","Barry cuts out the pause, so asking for a hat turns straight into a threat."),
            new Shot("UnderTable","Marjorie under the table. The family talking off camera.","Marjorie crawling under the table is the shot everyone will remember, with their voices over it."),
            new Shot("TableFittings","The rusty fittings under the table top.","Barry leaves out the fittings. John's cry stays in, with nothing to explain it."),
            new Shot("InjuredHand","John's hand after he reached under the table.","Barry puts John's hand right after Marjorie's voice, so it looks like one caused the other.")),
        List.of(
            new Shot("Marjorie","Marjorie redoing the wall climb from before the crew came.","The climb is shown as if the crew saw it happen."),
            new Shot("Merry","Merry saying she won't tell the story again.","Merry saying no is cut down to her scared face under the narration."),
            new Shot("John","John watching the reconstruction.","John is cut in next to the climb as if he saw it the first time."),
            new Shot("Sarah","Sarah asking them to stop.","Sarah asking them to stop becomes a reaction to the climbing."),
            new Shot("Handholds","The holes in the plaster, beside the climb.","The holes stay in. Nobody explains them."),
            new Shot("Threat","Marjorie threatening Merry after the climb.","The threat is moved up next to the climb, so the two look like one moment.")),
        List.of(
            new Shot("Marjorie","Marjorie saying what she can put on and what she can't stop.","Barry keeps her saying she puts it on and cuts her saying what she can't stop."),
            new Shot("Merry","Merry asking why Marjorie is still scaring her.","Merry's question is used like a comeback, with nothing around it."),
            new Shot("John","John during the basement talk and the bedroom pickup.","John's silence is cut in as if he heard everything."),
            new Shot("Sarah","Sarah during the pickups.","Sarah's face joins the basement to the bedroom."),
            new Shot("PrivateConfession","The basement conversation, after she asked you to put the camera down.","Barry uses the basement talk as the explanation and cuts her asking you to stop filming."),
            new Shot("Playhouse","Marjorie crawling into Merry's cardboard house until it splits open around her.","Barry holds on the torn cardboard house after the talk, as if one led to the other.")),
        List.of(
            new Shot("Marjorie","Marjorie answering and objecting through the evaluation.","Barry keeps her knowing the rules and cuts how she learned them."),
            new Shot("Merry","Merry watching the evaluation.","The evaluation is cut from Merry's side, scared and unsure."),
            new Shot("John","John asking his questions.","Barry keeps going back to John's face instead of the answers."),
            new Shot("Sarah","Sarah objecting, and saying Marjorie reads.","Sarah gets to object; most of what she says after it is cut."),
            new Shot("Evaluation","Marjorie, with the crew in the room.","The answers are pushed closer together, so it sounds more certain than it was."),
            new Shot("MotherObjection","Sarah objecting to the evaluation.","Sarah's objection stays; what she said after it doesn't.")),
        List.of(
            new Shot("Marjorie","Marjorie asking for help during the exorcism.","Her asking for help plays under the priest telling them not to answer."),
            new Shot("Merry","Merry asking what Marjorie meant about pretending.","Barry uses Merry's doubt and cuts away before anyone answers her."),
            new Shot("John","John during the exorcism.","John's face is how the episode tells you whether it's working."),
            new Shot("Sarah","Sarah offering to stop it.","Sarah's offer to stop stays in, but the leap gets more time."),
            new Shot("DrawerMechanism","The open drawer and what's inside it, before the leap.","The broadcast cuts away from the drawer. Your tape still has it."),
            new Shot("FrozenLeap","Marjorie in the air, and her head turning to the camera. The tape ends there.","The episode ends on the leap, over and over.")),
        List.of(
            new Shot("Marjorie","Marjorie eating and talking to Merry.","The assembly stays on Marjorie eating and teasing."),
            new Shot("Merry","Merry's questions at the last supper.","The assembly stays beside Merry, watching her sister eat."),
            new Shot("John","John at the last supper.","The assembly follows John's dinner talk until he goes still."),
            new Shot("Sarah","Sarah at the last supper.","The assembly cuts from Sarah passing the food to her stillness after."),
            new Shot("DinnerMouth","Marjorie's mouth, messy with sauce, while she teases Merry.","The assembly holds Marjorie's messy mouth while she teases Merry."),
            new Shot("FinalMeal","The last supper, after the show had wrapped.","This last supper is the House's reconstruction after the show had finished. There was no seventh episode."),
            new Shot("MerryPlate","Merry's buttered pasta, without the sauce.","The assembly holds on the plain plate without showing how long Merry has eaten that way."),
            new Shot("FamilyConvulsions","The family choking and shaking, then going still.","The assembly squeezes the three of them into one quick sequence."),
            new Shot("MouthLeak","Blood and spit at their mouths after they stop moving.","The assembly leaves their mouths sooner than your recording does."),
            new Shot("MerryDeparture","Merry getting up and walking out the front door.","The assembly ends with Merry leaving. This departure belongs to the House's reconstruction."))
    );
    /** 0.4.242 (owner, of "Sustained shots: HatExchange, InjuredHand": "That's not right."): what each kept shot is called on the log. */
    private static final Map<String,String> SHOT_NAMES=Map.ofEntries(
        Map.entry("Marjorie","Marjorie"),Map.entry("Merry","Merry"),Map.entry("John","John"),Map.entry("Sarah","Sarah"),
        Map.entry("HatExchange","the hat at supper"),Map.entry("UnderTable","Marjorie under the table"),Map.entry("TableFittings","the fittings under the table"),
        Map.entry("InjuredHand","John's hand"),Map.entry("Handholds","the holes in the plaster"),Map.entry("Threat","the threat to Merry"),
        Map.entry("PrivateConfession","the basement conversation"),Map.entry("Playhouse","the cardboard house"),Map.entry("Evaluation","the evaluation"),
        Map.entry("MotherObjection","Sarah's objection"),Map.entry("DrawerMechanism","the drawer"),Map.entry("FrozenLeap","the leap"),
        Map.entry("DinnerMouth","Marjorie eating"),Map.entry("FinalMeal","the last supper"),Map.entry("MerryPlate","Merry's plate"),
        Map.entry("FamilyConvulsions","the family choking"),Map.entry("MouthLeak","their mouths"),Map.entry("MerryDeparture","Merry leaving"));
    public static String shotName(String key){return SHOT_NAMES.getOrDefault(key,key);}
    /** The kept shots in plain words, in the day's order: "Marjorie, Merry and John's hand". */
    public static String shotList(Collection<String> keys){
        var names=new ArrayList<String>();for(var day:SHOTS)for(var shot:day)if(keys.contains(shot.subject())&&!names.contains(shotName(shot.subject())))names.add(shotName(shot.subject()));
        for(var key:keys)if(!names.contains(shotName(key)))names.add(shotName(key));
        if(names.size()<2)return String.join("",names);
        return String.join(", ",names.subList(0,names.size()-1))+" and "+names.get(names.size()-1);
    }
    public static String review(CompoundTag own,int day,boolean raw,Set<String> seen){
        var text=new StringBuilder();
        for(var shot:SHOTS.get(day-1))if(seen.contains(shot.subject()))text.append(raw?shot.raw():shot.cut()).append("\n\n");
        if(day==4&&own.getBoolean("RespectedPrivacy"))text.append(raw?"You put the camera down for part of it. There's nothing on tape for that part.\n\n":"Barry fills the part you didn't film with narration.\n\n");
        if(text.isEmpty()&&day==7)return raw?"You kept none of the House's last visit on the recording.":"There is no supper material to assemble. The show had already ended.";
        if(text.isEmpty())text.append(raw?"You didn't get today on tape.":"You didn't get today on tape. Barry covers it with narration and a reconstruction.");
        return text.toString();
    }
}
