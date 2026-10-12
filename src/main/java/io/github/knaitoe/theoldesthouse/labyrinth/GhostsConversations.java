package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.nbt.CompoundTag;

/** Original adaptation dialogue, saved separately for every speaker on every filming day. */
public final class GhostsConversations {
    private GhostsConversations() {}
    private static final String[][][] WORDS={
        {
            {"You keep looking through that thing. Do you ever look at me?","Merry remembers the stories differently. Ask her before you choose which one to keep."},
            {"Could you leave the pictures here? They're ours.","The jug above my door wasn't in the story. I put it there myself."},
            {"We need people to understand what this family is going through.","Being out of work isn't what brought you here. Don't make that the whole story."},
            {"You can film her books. Put them back where she left them.","The appointment was before you arrived. A camera isn't treatment."},
            {"Today is interviews and reconstructions. Mark the difference on your tape.","A pause can be longer in the edit. Remember how long it actually lasted."},
            {"I have spoken with John. I haven't performed an evaluation yet.","Something witnessed and something believed are different kinds of testimony."}
        },
        {
            {"Merry wants my hat. She always wants something that belongs to me.","Keep the table in your picture. You ought to see where his hand went."},
            {"It was only a hat. I wasn't asking her to tell another story.","She can make her voice sound as if it belongs to someone else."},
            {"It happened while we were praying together.","My hand still hurts. I don't want another close shot of it."},
            {"She needs water and a clean plate. Let me reach her.","If she goes under the table, give her room to come out."},
            {"Tonight the table stays in frame unless you choose to follow her.","A father's cry and a child's face can make two very different episodes."},
            {"I wasn't at your supper. I cannot tell you what injured his hand.","Ask what each person saw before you ask what they think caused it."}
        },
        {
            {"The plaster was broken before anyone asked me to climb again.","You can put the camera there. You can't put last night back."},
            {"I don't want to tell the tongue story again.","Do they have to show the bit where I'm scared?"},
            {"I was trying to help her down. I wasn't watching the wall.","Can we finish this pickup before the neighbours come home?"},
            {"She shouldn't have to repeat it because you missed it.","Look at the handholds. Then decide what your picture leaves out."},
            {"Today is a reconstruction. Keep that word with the shot.","Show the plaster if you want the audience to have something to examine."},
            {"An account of climbing is not the same as seeing it yourself.","I have not given the production permission for an exorcism."}
        },
        {
            {"Being watched doesn't make the voices stop. Sometimes I can decide what you see.","I asked for one conversation without the camera. You can still choose that."},
            {"She talks normally when you stop asking her to do things.","The cardboard house is mine. It isn't another prop for the show."},
            {"The money keeps us here. I can't pretend it doesn't matter.","If she says she can control some of it, listen to the rest of what she says."},
            {"Privacy shouldn't have to be a favour you grant her.","She has a doctor. Remember that when you put this together."},
            {"The basement conversation is today. The bedroom pickup comes after it.","If she refuses the lens, log the refusal. Don't invent an image to replace it."},
            {"A confession made for an audience may still contain something true.","Do not make her choice to perform settle everything about her illness."}
        },
        {
            {"I can read your rules. The camera shouldn't be in this evaluation.","When I answer quietly, you wait for me to say it again differently."},
            {"Nobody asked if I wanted him in her room.","She knows things because she reads. I can read too."},
            {"I want an answer. We can't keep living from appointment to appointment.","If you film me looking frightened, leave in what frightened me."},
            {"She can read, John. She can use the internet. She's a person.","The part where I object belongs on the tape too."},
            {"Keep the question with its answer in today's log.","Watch the mother as well as the priest. The evaluation has more than one witness."},
            {"There are rules for an evaluation. The presence of your crew is an objection I must hear.","Knowledge of a question does not establish where that knowledge came from."}
        },
        {
            {"I'm cold. Please stay where I can see you.","I haven't touched that drawer. Don't cut away if it moves."},
            {"She told me she was pretending. I don't know which part she meant.","Don't ask me to say that into your microphone again."},
            {"I thought this would help. I need it to help.","If Sarah asks you to stop, you will hear her. Promise me that much."},
            {"If she wants this stopped, I will stop it.","Stay near the doorway. Let me get to my daughter."},
            {"Today's tape includes the drawer and everyone who could reach it.","Keep your lens steady when she moves. We don't know what the next moment will be."},
            {"Do not answer every change of voice as though it were a different being.","Leave a clear path to the bed. This is still a child's room."}
        },
        {
            {"No sauce for Merry. She likes everything where she can see it.","I'm eating mine. Why are you watching my plate instead of my face?"},
            {"She said we could go together. She wasn't supposed to eat it.","I thought they would go to sleep. I don't understand what she's doing."},
            {"The programme is finished. Can we have an ordinary supper?","You can take the cases out. Leave the kitchen clear."},
            {"There is plain spaghetti for Merry. She doesn't like the sauce.","Sit down, John. We'll talk about the equipment afterwards."},
            {"The show is over. Today we're collecting our equipment.","There is no next episode on the schedule. Keep any tape you make."},
            {"The church has no service scheduled in this house today.","The programme ending does not end what has happened to this family."}
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
            new Shot("Marjorie","Marjorie's interview is held in frame. The crew records her account of the sisters and the vines, not the original incident.","Marjorie's interview becomes the first episode's warning: her invented sisters' story is placed before the bedroom reconstruction."),
            new Shot("Merry","Merry's interview remains in frame as she describes the books and the jug she balanced above her door.","Merry's description of a homemade trap is edited into a child's anticipation of her sister's return."),
            new Shot("John","John is recorded explaining why the family agreed to the programme.","John's interview gives the opening episode its argument that the programme might help the family."),
            new Shot("Sarah","Sarah is recorded asking the crew to return the girls' books.","Sarah's request to return the books becomes a brief reaction beside the reconstructed bedroom shots.")),
        List.of(
            new Shot("Marjorie","Marjorie stays in frame during the supper conversation and changed voice.","The episode stays with Marjorie: an ordinary request for a hat leads into the changed voice."),
            new Shot("Merry","Merry's reaction to the request for the hat and her sister's reply is recorded.","The supper episode is assembled around Merry's reaction, extending her silence after the threat has finished."),
            new Shot("John","John's response at supper is recorded, including his attempt to pray with his daughter.","John's response makes the supper episode a father's attempt to confront the threat at his table."),
            new Shot("Sarah","Sarah's effort to reach Marjorie at supper stays in frame.","Sarah's concern is kept as a reaction, while her offer to stay with her daughter is shortened."),
            new Shot("HatExchange","The request for the borrowed hat stays beside Marjorie's reply.","The cut makes the borrowed hat the opening of a threat, taking away the time between the request and the reply."),
            new Shot("UnderTable","The camera follows Marjorie beneath the table. The family speaks outside the frame.","Marjorie's crawl becomes the episode's central image, with the family's voices laid over it."),
            new Shot("TableFittings","The rusty fittings beneath the tabletop are visible in the tape.","Barry leaves out the fittings shot. His cut keeps the cry from above the table without this possible explanation."),
            new Shot("InjuredHand","John's injured hand is held in frame after he reaches under the table.","The close shot of John's hand is put directly after Marjorie's voice, implying a connection the framing cannot establish.")),
        List.of(
            new Shot("Marjorie","The crew records Marjorie reenacting an earlier wall incident.","Marjorie's wall reconstruction is presented as if the crew saw the earlier incident happen."),
            new Shot("Merry","Merry's refusal to repeat the frightening story is recorded.","Merry's refusal is cut down to her frightened face beneath the upstairs narration."),
            new Shot("John","John's response to the upstairs reconstruction stays in frame.","John's response is placed beside the wall reconstruction to supply an eyewitness the original event never had."),
            new Shot("Sarah","Sarah's request to stop the reconstruction stays on the tape.","Sarah's request to stop becomes a reaction to the climbing, losing its objection to repeating the event."),
            new Shot("Handholds","The damaged plaster and handholds are visible beside the reconstructed climb.","The damaged plaster remains beside the climb. The audience can see a physical detail the narration does not explain."),
            new Shot("Threat","The lens records the threat directed at Merry after the reconstruction.","The threat is moved nearer the climbing shots, making two separate exchanges seem continuous.")),
        List.of(
            new Shot("Marjorie","Marjorie stays in frame as she distinguishes choosing a performance from being able to stop the voices.","The cut uses Marjorie's account of performing and shortens her account of what she cannot control."),
            new Shot("Merry","Merry's question about still being frightened stays in frame.","Merry's question is used as a rebuttal, with the conversation around it removed."),
            new Shot("John","John is held in frame during today's private-conversation and bedroom pickups.","John's silence becomes a reaction to the sisters' conversation, even though the lens cannot establish what he heard."),
            new Shot("Sarah","Sarah's response during today's pickups is recorded.","Sarah's reaction is used to bridge the basement conversation and the later bedroom pickup."),
            new Shot("PrivateConfession","The camera records the conversation after Marjorie asks for the lens to be put down.","Barry uses the private conversation as an explanation while omitting the appeal to stop recording."),
            new Shot("Playhouse","The camera records the cardboard house collapsing in the bedroom pickup.","The cut holds on Merry's damaged cardboard house after the sisters finish speaking, placing it beside the earlier confession.")),
        List.of(
            new Shot("Marjorie","Marjorie's answers and objections during the evaluation are held in frame.","The evaluation episode keeps Marjorie's knowledge of the rules and shortens the explanation of how she learned them."),
            new Shot("Merry","Merry is recorded watching the evaluation.","The evaluation is assembled from Merry's frightened viewpoint, making her uncertainty carry the scene."),
            new Shot("John","John's questions during the evaluation are held in frame.","John's questions lead the evaluation episode; the cut repeatedly returns to his reaction rather than the answer."),
            new Shot("Sarah","Sarah's objection and explanation that Marjorie reads are recorded.","Sarah's explanation becomes the episode's competing account, though the surrounding argument is shortened."),
            new Shot("Evaluation","The lens stays on the person being evaluated while the crew is present.","The answers are placed closer together than they occurred, making the evaluation sound more conclusive."),
            new Shot("MotherObjection","Sarah's objection to the evaluation stays on the tape.","The cut keeps Sarah objecting and removes part of the explanation that follows her objection.")),
        List.of(
            new Shot("Marjorie","Marjorie's requests for help during the exorcism remain on the tape.","The exorcism episode places Marjorie's requests for help beneath the instructions not to answer her."),
            new Shot("Merry","Merry is held in frame as she questions what her sister said about pretending.","The episode uses Merry's doubt to carry the exorcism, cutting away before anyone answers her."),
            new Shot("John","John's response during the exorcism is recorded.","John's reaction becomes the exorcism episode's measure of whether it is working."),
            new Shot("Sarah","Sarah stays in frame when she offers to stop the exorcism.","Sarah's offer to stop remains in the episode, but receives less time than the held leap."),
            new Shot("DrawerMechanism","The opened drawer and its mechanism are held in frame before the leap.","The broadcast cuts away from the mechanism. Your retained tape contains a detail missing from the repeated leap."),
            new Shot("FrozenLeap","The lens records the held leap and Marjorie's head turning toward it. The take ends there.","The episode repeats the held leap as its final image. The repetition cannot supply anything after the cut.")),
        List.of(
            new Shot("Marjorie","Marjorie stays in frame as she eats the sauce and addresses Merry.","The unpublished assembly stays with Marjorie, making her decision to eat and her teasing carry the final meal."),
            new Shot("Merry","Merry's questions during the final meal stay on the tape.","The unpublished assembly uses Merry's confusion as the final meal's viewpoint; it cannot explain what she expected."),
            new Shot("John","John stays in frame during the final supper.","The assembly follows John's last supper, using his ordinary table conversation before his collapse."),
            new Shot("Sarah","Sarah stays in frame during the final supper.","The assembly follows Sarah's last supper, placing her ordinary concern beside the later stillness."),
            new Shot("DinnerMouth","Marjorie's messy mouth and teasing are recorded while she eats the spaghetti.","The assembly holds on the sauce and spit around Marjorie's mouth, keeping her words to Merry over the close shot."),
            new Shot("FinalMeal","The camera records the final meal after the scheduled programme has ended.","Barry puts the unscheduled meal into an unpublished assembly. It was never a scheduled seventh episode."),
            new Shot("MerryPlate","Merry's plate contains plain spaghetti without the family's sauce.","A close shot of Merry's plain plate is used to imply a cause. A plate alone cannot establish one."),
            new Shot("FamilyConvulsions","The family begins choking and convulsing before becoming still.","Barry compresses the different moments of collapse into one abrupt sequence."),
            new Shot("MouthLeak","Blood and spittle remain visible at the mouths after the movements stop.","The assembly cuts from the leaking mouths to the table sooner than the unedited tape does."),
            new Shot("MerryDeparture","Merry leaves her chair and walks through the front door after the family is still.","The assembly ends with Merry leaving. It omits the time she spent beside the table."))
    );
    public static String review(CompoundTag own,int day,boolean raw,Set<String> seen){
        var text=new StringBuilder();
        for(var shot:SHOTS.get(day-1))if(seen.contains(shot.subject()))text.append(raw?shot.raw():shot.cut()).append("\n\n");
        if(day==4&&own.getBoolean("RespectedPrivacy"))text.append(raw?"The camera was put down for part of the conversation. No image of that part is available.\n\n":"The cut fills the unrecorded portion with narration. The withheld image does not confirm the narration.\n\n");
        if(text.isEmpty())text.append(raw?"No sustained shot of today's incident was recorded. The log cannot supply what the lens missed.":"Your camera provides no sustained image of today's incident. The production fills the gap with narration and a reconstruction, neither of them your footage.");
        return text.toString();
    }
}
