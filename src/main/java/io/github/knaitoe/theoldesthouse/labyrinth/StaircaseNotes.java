package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.world.item.ItemStack;

/** Sixty-four separate, short domestic scenes. Authored fiction; never an invented player memory. */
public final class StaircaseNotes {
    private StaircaseNotes(){}
    public static final List<String> TEXTS=List.of(
        "The last plate\n\nRuth washed. I dried. We left the cracked plate until last, as if deciding where it belonged would finish the evening. She put it under the geranium.",
        "A blue sock\n\nThe washing was warm when we tipped it out. Jonah matched the socks by size; I matched them by wear. We made different pairs. Nobody complained.",
        "On the way home\n\nMara drove with one hand resting on the gear stick. I held the paper bag between my knees. At every turn the bread tapped the door.",
        "Breakfast\n\nThe kettle stopped before anyone noticed. Bell poured his tea anyway. I opened the window to hear the man unloading bottles in the street.",
        "Thursday\n\nA button rolled out from under the dresser. Ruth fetched her blue coat and pushed it through the empty hole. I held the cloth flat while she threaded the needle.",
        "After the rain\n\nJonah held the pegs in his mouth. I hung the shirts by their shoulders. We argued about this every week. The wind settled it for us.",
        "The passenger seat\n\nThere was no reason to hurry. We let the bus pull out. Mara turned the radio down to tell me which house used to have the pear tree.",
        "For tomorrow\n\nI peeled one extra potato. Bell said he would be late. I wrapped his plate and wrote his name on the paper, though no one else would have taken it.",
        "The towel rail\n\nRuth put a second towel out for our visitor. He never came. We used it to dry the cups on Sunday and did not mention him.",
        "A small repair\n\nThe drawer caught at the same place. Jonah rubbed a candle along the runner. It opened without a sound. We stood there opening it again.",
        "At the lights\n\nMara wiped a clear patch on the windscreen with her sleeve. Beyond it, a boy was teaching his little sister to wait for the green man.",
        "The good cups\n\nBell asked whether we should use them. I said that was what cups were for. One had a hairline crack. We gave that one to the flowers.",
        "Saturday washing\n\nI found a shopping list in a trouser pocket. Ruth read it out while folding. We had bought everything except the matches. She added them to next week's list.",
        "The turn\n\nJonah missed the turning. Neither of us spoke until we had passed the garage. Then we laughed, because there was plenty of road and we knew how to get back.",
        "The sink\n\nA spoon lay beneath the water, almost invisible. I felt for it with my fingers. Ruth handed me a clean cloth before I asked.",
        "The airing cupboard\n\nWe put the older sheets underneath. Bell liked their thinness in summer. I could fold one alone, but waited for him to take the other end.",
        "Six o'clock\n\nMara sat in the parked car until the song ended. I watched the kitchen window. Somebody inside moved the washing-up bottle from one side of the sink to the other.",
        "The table\n\nI wiped around Jonah's newspaper. He lifted each corner without looking up. When I reached his cup he picked it up and finally asked about my day.",
        "The hem\n\nRuth threaded the needle on her third try. I held the lamp closer. She said I was making a shadow, so I sat beside her instead.",
        "A longer way\n\nBell drove us past the river. We could have gone home sooner. I opened my window an inch and let the cold air sit between us.",
        "The cupboard\n\nThree lids, two saucepans. Jonah tried every combination. Ruth found the missing pan in the oven, still holding yesterday's bread.",
        "The blanket\n\nWe shook it together at the back door. Crumbs fell into the grass. Mara stayed outside to watch the sparrows arrive. I left the door open for her.",
        "The queue\n\nThe car ahead stalled. Bell reached for the horn and then took his hand away. The driver looked back and raised one finger in apology.",
        "Sunday lunch\n\nI put the forks down first. Ruth changed two of them because their handles did not match. We had never cared about that before. I let her finish.",
        "The pocket\n\nA mint had melted into the lining. Jonah turned the coat inside out and held it under the tap. I kept the sleeves off the wet floor.",
        "The back seat\n\nMara fell asleep before we reached the bridge. Her shopping bag rustled whenever we braked. Bell asked me to move it so she could rest.",
        "The draining board\n\nI made room for one more cup. There was always one more cup. Ruth dried it while the water was still warm.",
        "Fresh sheets\n\nWe could not find the pillowcase with the stripes. Bell made the bed with two plain ones. In the morning the striped one was on the laundry basket.",
        "The petrol station\n\nJonah brought back a coffee for each of us. Mine was too hot to hold. We set them on the bonnet and watched the steam lift into the drizzle.",
        "The loaf\n\nMara cut the heel off and ate it standing up. I fetched the butter. There was no need to make plates dirty for this.",
        "Mending day\n\nRuth showed me how to turn the sleeve before sewing it. I had already stitched it shut. She unpicked it slowly, without making me feel foolish.",
        "The shortcut\n\nBell asked whether this was our road. I said yes too soon. We reached a field gate, reversed carefully, and stopped to look at the sheep.",
        "Before bed\n\nI rinsed the sink until the last trace of soap went away. Jonah switched off the big light. The little one over the cooker was enough.",
        "The basket\n\nRuth left the towels unfolded. I thought she had forgotten. Then I found her asleep in the chair and carried the basket upstairs myself.",
        "The mirror\n\nMara adjusted it after Bell got out. For a moment I could see the road behind us from her seat. Then she moved it back to where she needed it.",
        "Two chairs\n\nWe moved them closer to the window. Bell could read the fine print there. I put his old chair under the table. Nothing had to be thrown away.",
        "A stubborn stain\n\nJonah said cold water first. I had already used hot. Ruth pretended not to hear us arguing and brought another shirt.",
        "The lay-by\n\nWe stopped to eat the sandwiches. Mara folded the wrapping into a small square. Bell slept with his head against the glass. I counted three passing tractors.",
        "The saucepan\n\nThe porridge had caught at the bottom. I left it soaking. Ruth said we could come back to it after breakfast. We had other things to do.",
        "The clothes horse\n\nOne sleeve trailed on the floor. Jonah lifted it, found no free rail, and folded it over the other sleeve. It stayed damp longest.",
        "The last mile\n\nBell asked me to keep talking. I told him about the broken kitchen clock and how Ruth had wound it anyway. We reached home before I finished.",
        "Tuesday evening\n\nMara scraped the plates into yesterday's newspaper. I tied the rubbish bag. We both remembered to leave the back-door key where the other could find it.",
        "The small tin\n\nRuth kept spare buttons in it. The lid had once held biscuits. Jonah opened it every Christmas, knowing perfectly well what was inside.",
        "The bend by the school\n\nI leaned with the car. Bell said that would not help. I did it again at the next bend and he leaned with me.",
        "The tea cloth\n\nIt had worn thin at the corners. Mara folded the weak parts inward. She could still dry a glass with it if she took her time.",
        "Lost and found\n\nWe found the sock caught inside a fitted sheet. Ruth held it up like a prize. Jonah had already given its partner to the rag bag.",
        "The handbrake\n\nBell pulled it up twice to be certain. We sat a little longer. Rainwater ran down the windscreen in pairs that became one line.",
        "The window latch\n\nJonah caught his cuff on it while reaching for the pot. I held the sleeve steady. Ruth picked the thread free with the end of a spoon.",
        "The landing\n\nWe stopped to share an orange. Bell made a neat pile of the peel. Ruth kept one segment back for Jonah. He arrived before it dried.",
        "At first light\n\nMara boiled the water twice. The first cup was for warming her hands. I put the second beside the place where Ruth was sleeping.",
        "The bag\n\nJonah's buckle knocked against the rail. We wrapped it in a sock. He finally slept. I could hear Bell turning the pages of his book.",
        "The spoon\n\nRuth found it in my coat. I had been looking in the food tin. She washed it with a little of the water we had saved and gave it back.",
        "The wool scarf\n\nMara hung it where the draft was strongest. Bell moved it away from the lamp. By morning it was dry enough to wear.",
        "Our supper\n\nThree tins, four people. We ate from the lids and passed the knife carefully. Jonah tried to remember the name of the place where we had bought them.",
        "The washing line\n\nRuth tied it between two brackets. I held the lamp while she spread our socks along it. We left a space in the middle for Bell's shirt.",
        "A pause\n\nBell asked for five minutes. We gave him ten. Mara checked the straps on our bags while I poured the last of the tea.",
        "The blanket roll\n\nJonah could not make it fit. Ruth sat on one end while he tightened the straps. We laughed quietly so we would not wake Bell.",
        "The next landing\n\nMara carried the cups. I carried the kettle, though it was empty. We had agreed which things were worth keeping and did not need to agree again.",
        "The last candle\n\nRuth cut it in half with the warm knife. One piece was for now. Bell put the other in the tin and shut the lid.",
        "A familiar sound\n\nJonah washed a cup with a cloth. It squeaked just as it had in our kitchen. Mara heard it and asked him to do it once more.",
        "The seat by the wall\n\nBell spread his coat for Ruth. She sat without thanking him. They had been doing things like this for each other for years.",
        "After supper\n\nWe put everything back in the same order. Mara counted the cups. I counted the spoons. Jonah checked that the lamp would light again.",
        "The morning list\n\nRuth wrote water, food, dry socks. Bell added the names of the people carrying them. The list made our bags feel lighter.",
        "Before we moved on\n\nI dried the last cup against my sleeve. Mara waited while I put it away. Nobody called this delay. We were still together."
    );
    public static ItemStack specimen(int index){
        var hands=new HouseWriting.WritingStyle[]{HouseWriting.WritingStyle.KAREN,HouseWriting.WritingStyle.WILL,HouseWriting.WritingStyle.ZAMPANO};
        return HouseWriting.book("A loose sheet",signer(index),hands[Math.floorMod(index,hands.length)],List.of(TEXTS.get(index)));
    }
    private static final String[] NAMES={"Ruth","E. Marsh","Mara","A. Bell","Jonah","Sabine"};
    /** The sheet's writer is the "I" of its scene, never someone the scene speaks of. */
    public static String signer(int index){
        String text=TEXTS.get(index);
        for(int step=0;step<NAMES.length;step++){
            String name=NAMES[Math.floorMod(index+step,NAMES.length)];String surname=name.substring(name.lastIndexOf(' ')+1);
            if(!java.util.regex.Pattern.compile("\\b"+surname+"\\b").matcher(text).find())return name;
        }
        return NAMES[1];
    }
    public static ItemStack panther(){return io.github.knaitoe.theoldesthouse.house.CorrespondenceTexts.book(io.github.knaitoe.theoldesthouse.house.NovelCorrespondence.panther(),Map.of());}
    public static List<ItemStack> specimens(){var out=new ArrayList<ItemStack>();for(int i=0;i<TEXTS.size();i++)out.add(specimen(i));out.add(panther());return List.copyOf(out);}
}
