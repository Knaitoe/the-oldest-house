package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.nbt.CompoundTag;

/** Original adaptation dialogue, saved separately for every speaker on every filming day. */
public final class GhostsConversations {
    private GhostsConversations() {}
    // 0.4.242 (owner: "try to cut the ai-isms from the vignette"): people talking, not summing up.
    private static final String[][][] WORDS={
        {
            {"You keep looking at me through that thing. Is it on now?","Ask Merry about the story with the vines. She tells it better. She tells it wrong, but better."},
            {"Can you leave the pictures? Mum said they're ours.","I put the jug over my door. That wasn't in the story. I just did it."},
            {"We need people to see what this has been like for us.","I'm not working right now. That's not why we said yes. Okay, it's part of it."},
            {"You can film her books. Just put them back where she had them.","She saw a doctor before you lot turned up. Write that down somewhere."},
            {"Interviews today, then the bedroom reconstruction. Label your tapes.","If she goes quiet, keep rolling. We can cut it down later."},
            {"I've spoken with John. I haven't seen her properly yet.","I'd like to hear it from Merry. Not from the tape."}
        },
        {
            {"Merry wants my hat. Merry always wants my stuff.","Keep the table in, if you're filming. Watch where his hand goes."},
            {"It was just a hat. I only wanted it for school.","She can do voices. She's always been able to do voices."},
            {"We were praying. We were just holding hands and praying.","My hand still hurts. Please don't get it close up again."},
            {"She needs some water. Let me get to her.","If she's under the table, leave her be. She'll come out."},
            {"Keep the table in frame. If she moves, go with her.","Get his face when he shouts. And get Merry."},
            {"I wasn't at your supper. I can't tell you what hurt his hand.","Ask what each of them saw. Then ask what they think happened."}
        },
        {
            {"The plaster was like that already. You weren't here for it.","Film it if you want. It's not the same as last night."},
            {"I don't want to tell the tongue thing again.","Do they have to show me being scared?"},
            {"I was trying to get her down. I wasn't looking at the wall.","Can we finish before the neighbours get home?"},
            {"She shouldn't have to do it again just because you missed it.","Look at the holes in the plaster. That's her fingers."},
            {"It's a reconstruction. Mark it as one.","Get the plaster in. Give them something to look at."},
            {"I didn't see her climb. Neither did your camera.","Nobody has given permission for an exorcism. Not yet."}
        },
        {
            {"It doesn't stop because you're watching. Sometimes I just get to pick what you see.","I asked you to put it down. You still can."},
            {"She talks normal when nobody's asking her to do stuff.","The cardboard house is mine. It's not for the show."},
            {"The money's keeping the lights on. I'm not going to pretend it isn't.","She says she can control some of it. Listen to the rest."},
            {"She shouldn't have to ask you for privacy.","She has a doctor. Put that in, when you cut this."},
            {"Basement first, then the bedroom pickup.","If she won't let you film, log that. Don't make something up."},
            {"She said it to the camera. I'd still like to hear the rest.","One afternoon on tape isn't her whole illness."}
        },
        {
            {"I read your rules. There's not supposed to be a camera in here.","When I answer quietly, you wait for me to say it louder."},
            {"Nobody asked me if I wanted him in her room.","She knows stuff because she reads. I read too."},
            {"I want an answer. We can't keep going appointment to appointment.","If you film me scared, film what I'm scared of."},
            {"She can read, John. She can use the internet. She's a person.","Keep the bit where I object. That goes in too."},
            {"Keep the question with the answer.","Get the mum as well as the priest."},
            {"There are rules for an evaluation. Your crew being here is one of my objections.","She answered before he'd finished asking. Who told her the question?"}
        },
        {
            {"I'm cold. Stay where I can see you.","I didn't touch that drawer. Don't stop filming if it moves."},
            {"She told me she was pretending. I don't know which bits.","Don't make me say it into the microphone again."},
            {"I thought this would help. It has to help.","If Sarah says stop, we stop. Promise me."},
            {"If she wants it to stop, I'll stop it.","Stay by the door. Let me get to her."},
            {"Get the drawer and everyone near enough to reach it.","Keep it steady when she moves."},
            {"Don't answer every voice like it's someone new.","Keep the way to the bed clear. It's still a child's room."}
        },
        {
            {"I did the sauce tonight. Eat it while it's hot.","I'm eating mine. Why are you looking at my plate?"},
            {"She said we'd go together. She wasn't supposed to eat it.","I thought they were going to sleep. Why won't they wake up?"},
            {"The show's done. Can we just have dinner?","Take the cases out the front. Mind the kitchen."},
            {"Spaghetti. Sit down, everyone, before it goes cold.","Sit down, John. We'll sort the equipment out after."},
            {"Show's wrapped. We're just picking up the gear.","Mind the cable on the stairs. That's the last of it."},
            {"There's nothing scheduled here today.","The show finishing doesn't make any of this finish."}
        }
    };
    public static String line(CompoundTag own,int speaker){
        int day=Math.max(1,Math.min(7,own.getInt("FilmDay")))-1;
        if(speaker<0||speaker>=6)return "";
        String key="GhostTalk_"+(day+1)+"_"+speaker;int n=own.getInt(key);own.putInt(key,(n+1)%WORDS[day][speaker].length);
        if(day==6&&speaker==1&&own.getInt("ShotClock")>=820)return own.getBoolean("MerryLeft0488")?"I can't go back into that room.":"Please. I need to get out of this house.";
        return WORDS[day][speaker][n%WORDS[day][speaker].length];
    }
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
            new Shot("Marjorie","Marjorie eating and talking to Merry.","Barry's unaired cut stays on Marjorie eating and teasing."),
            new Shot("Merry","Merry's questions at the last supper.","The unaired cut watches the supper through Merry not understanding."),
            new Shot("John","John at the last supper.","The unaired cut follows John's dinner talk up to when he goes down."),
            new Shot("Sarah","Sarah at the last supper.","The unaired cut puts Sarah's fussing next to the stillness after."),
            new Shot("DinnerMouth","Marjorie's mouth, messy with sauce, while she teases Merry.","Barry holds the sauce and spit on Marjorie's mouth under her words to Merry."),
            new Shot("FinalMeal","The last supper, after the show had wrapped.","Barry put the supper in an unaired cut. There was never a seventh episode."),
            new Shot("MerryPlate","Merry's plate, untouched.","A close-up of Merry's full plate, as if it explains something."),
            new Shot("FamilyConvulsions","The family choking and shaking, then going still.","Barry squeezes the three of them into one quick sequence."),
            new Shot("MouthLeak","Blood and spit at their mouths after they stop moving.","The unaired cut leaves their mouths sooner than your tape does."),
            new Shot("MerryDeparture","Merry getting up and walking out the front door.","The unaired cut ends with Merry leaving and skips the time she sat with them."))
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
        if(text.isEmpty())text.append(raw?"You didn't get today on tape.":"You didn't get today on tape. Barry covers it with narration and a reconstruction.");
        return text.toString();
    }
}
