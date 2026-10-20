package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/** The owner's found documents, kept whole and split at word boundaries for native book pages. */
public final class UncannyTexts {
    private UncannyTexts(){}
    public static List<String> pages(String text){
        var pages=new ArrayList<String>();
        for(String paragraph:text.strip().split("\\n\\s*\\n")){
            String rest=paragraph.strip().replaceAll("[ \\t\\r\\n]+"," ");
            while(rest.length()>145){int at=rest.lastIndexOf(' ',145);if(at<1)at=145;pages.add(rest.substring(0,at));rest=rest.substring(at).stripLeading();}
            if(!rest.isEmpty())pages.add(rest);
        }
        return List.copyOf(pages);
    }
    private static ItemStack book(String title,String author,String text){return HouseWriting.book(title,author,HouseWriting.WritingStyle.WILL,pages(text));}
    public static String title(LabyrinthPlace place){return switch(place){
        case PAINTED_INTERIOR->"The painted sun";case SECOND_WAITRESS->"The second apron";case TUNNEL_PERIPHERY->"Under the green lights";
        case MISSING_SLEEP->"The empty mattress";case LAYERED_MAN->"The cellar shift";case MOTION_LIGHT->"The unhinged door";default->throw new IllegalArgumentException(place.id());};}
    public static ItemStack source(LabyrinthPlace place){return book(title(place),switch(place){
        case PAINTED_INTERIOR->"Wallpaper from the table";case SECOND_WAITRESS->"A receipt behind the till";case TUNNEL_PERIPHERY->"A maintenance clipboard";
        case MISSING_SLEEP->"A page beside the monitor";case LAYERED_MAN->"The cellar ledger";case MOTION_LIGHT->"The vestibule wallpaper";default->"";
    },log(place));}
    public static String log(LabyrinthPlace place){return switch(place){
        case PAINTED_INTERIOR->"""
            Everyone lives in the ground eventually. When you’re in this house, you’re in the ground right now.

            My father spent his last three weeks painting the planks over the parlor windows. He mixed yellow ochre with lard until he had a sun that never sets, and he painted a little china dog onto the wood where the sill used to be so Ma would have something to look at while the rot took her eyes. He told us the boards were to keep the dirt from pressing the glass in. He lied. He was trying to give us something to stare at so our eyes wouldn't wander to the cracks between the timber.

            Yesterday, the neighbor kids came down from the farm and pried a knot out of the porch boarding to peek inside. I heard them whispering that nobody was home. They left before dark.

            They didn't see anybody because they only looked straight ahead. They didn't know we were all still here, propped up stiff in the corners just past the edges of the window frame—three inches to the left of where their necks could turn. Our jaws are unhinged so we don't make a sound breathing. Our eyelids are dried back to the bone. We are waiting for someone to stop looking at the painted sun on the boards, and realize the edges of their vision are full of teeth.
            """;
        case SECOND_WAITRESS->"""
            2:15 AM: The draft is back behind booth four. I mopped the floor twenty minutes ago and there’s already black frost spidering up the napkin dispenser. The butter knives are rattling against the Formica again—that fast, tinny tink-tink-tink like someone’s teeth chattering against a plate.

            3:00 AM: She’s standing by the pie case now. You can still see the headlights from the highway shining right through her apron, so we’ve got an hour before she gets heavy. People think she’s the ghost of the Miller girl who died in the ditch four years back. She isn't. That girl is deader than dead; something snatched her soul out of the air and ate it on the way up, and now whatever is left is wearing her uniform. She was a big girl in life, and she smiled wide in all her photos—lips stretched tight, eyes crinkled, all teeth. She learned early that if you smile wide enough, you can stretch your features into something unrecognizable.

            3:45 AM: She just picked up a salt shaker. It didn't fall through her hand. Her knuckles made a wet snap when they closed around the glass. Keep your eyes on your coffee. Don't answer her when she asks how your meal is. Her mouth has split past her cheekbones now, and she isn't here to serve the tables—she’s here to clear them.
            """;
        case TUNNEL_PERIPHERY->"""
            Don't run. If you sprint, you just give her something to pace.

            She’s tall enough that she doesn't need to hurry; her lazy, dragging stride is enough to stay dead even with my fastest sprint. For the first mile I thought she was the ghost from all the stories—the bent-neck woman, broken and weeping just outside the streetlamp. Then the tunnel narrowed, and her face sloped down over my shoulder from the dark above the pipes, smelling like wet hair and butcher paper.

            Her neck isn't broken. She isn't grieving. The concrete ceiling is eight feet high, and she’s just stooping to get in.

            Watch the crosswalk lights ahead. I don't know what broken rule she’s bound to, but she stops dead at the green lights. When the green comes on, she freezes with her spine pressed flat against the ceiling tiles and her chin hovering six inches above my hair. When it flips red, she walks. And don't look at the electronic road signs bolted to the walls—they stopped flashing detour warnings ten minutes ago. Now they just flicker on and off, counting down how many steps I have left in my legs.
            """;
        case MISSING_SLEEP->"""
            Night 1: The cameras skipped from midnight to 6:00 AM. Customer service said it was a hard drive glitch. I woke up under the covers, but the mud from the backyard was dried between my toes.

            Night 3: Four new cameras. Different brands. It isn't the cameras. Time jumps when I close my eyes. I woke up in bed twenty minutes ago, rewound the bedroom feed, and watched an empty mattress for six straight hours. I never laid down last night. Where was I while the bed was empty?

            Night 5: He’s in the hall right now. If I crack the bedroom door and look with my own eyes, his arms are jetted straight out from his shoulders and his head bobs like a marionette on tangled string. His feet don't touch the carpet—they glide an inch above the clutter, tiptoeing his jerking body toward my parents' room. I want to say there’s pain on his face, but it’s nearer to ecstasy. Orgasmic, almost. His lips are quivering, his eyes are watering, and he’s letting out this wet, guttural purr that clicks on and off in his throat.

            But when I look down at the security monitor on my desk, the hallway looks normal. It shows me walking toward their door with a carefree lilt, a noiseless whistle on my lips, dragging the baseball bat along the drywall.

            He just reached for their doorknob on the screen. My own right hand is tingling.
            """;
        case LAYERED_MAN->"""
            Every beginning is an end. If you let it live, it will grow.

            You have to rip the skin away from the collarbones down while it’s still wet, before the fat has time to set into muscle. Stop for five minutes to catch your breath and he lays down three new coats of meat over the ribs.

            Marcus took the first shift with the skinning knife until his fingers locked up. He didn't make it up the stairs. He tripped by the sump pump, and before I could pull him loose, the thing surged over his boots and knitted him straight into its hip. You can still see the outline of Marcus’s face stretched tight under the fourth layer of belly skin, his mouth open beneath a sheet of someone else’s blubber, trying to scream through two inches of warm tallow.

            My blade snapped off in a collarbone ten minutes ago. I’m using my nails now, tearing wet ribbons off his cheeks just to keep his shoulders from wedging the cellar door shut. The bottom three steps are already swallowed in folds of hairless, sweating flank. Grab the cleaver off the bench and start peeling, or in ten minutes there won't be enough room left in this basement for our lungs to expand.
            """;
        case MOTION_LIGHT->"""
            The buzzer means some faceless, voiceless thing is on the step and wants to be invited in. Do not touch the brass button.

            Watch the courtyard through the vestibule glass. The motion light out there is wired to trip whenever something moves. As long as the light stays ON, it means the wind is still blowing and you’re alone. When the floodlight clicks OFF and drops the glass into pitch black, the yard isn't empty. It means somebody is standing dead still in the dark three feet from the glass—holding their breath, locking their joints, waiting out the sensor's timer so you can't see what they’re doing to their face.

            And don't back up without looking behind you first.

            Look at the mahogany door at the end of the hall. Look closely at the frame. There are no hinges. That door wasn't there when we came into this building, and the hallway behind it doesn't belong to this street. Stay between the glass and the wall. If the light goes out in front of you, and the unhinged door clicks open behind you, pray the thing on the porch is faster than whatever is waiting in that new hallway.
            """;
        default->throw new IllegalArgumentException(place.id());
    };}
    public static String ring(){return "Whittled from the finger of a man who died trying to claw his way out of a pine box. Put it on and the skin of the world goes thin. You’ll see the pulse of every living thing through the drywall, and hanging above their heads like a butcher’s price tag, the exact ugly thought they’re holding behind their teeth.";}
    public static String mirror(){return "Black obsidian, slick as grease at the bottom of a well. An astronaut once said that when you’re out in all that deep dark, the emptiness almost looks shiny. Something convinced the people here a long time ago that She demands blood. Slice your thumb across the edge to wake the glass up—it’ll show you what’s been standing in the corner of the room while you thought you were alone.";}
    public static ItemStack artifact(boolean mirror){return book(mirror?"The smoky mirror":"The bone ring","A tag tied with thread",mirror?mirror():ring());}
    public static ItemStack account(String reader,LabyrinthPlace place,CompoundTag own){return book(title(place),reader,switch(place){
        case PAINTED_INTERIOR->"The sun on the boards had brush marks. I took a plank down and found no daylight behind it. There were people in the corners whenever I turned away. The unpainted door still had a handle on the other side.";
        case SECOND_WAITRESS->"There were two aprons behind the counter. Only one woman had a name on the rota. I watched the other close her hand around a plate. I left her meal where she put it and asked the woman at the till for the way out.";
        case TUNNEL_PERIPHERY->"She kept pace without running. Under each green light she stopped, and I went on. Her neck straightened where the ceiling rose. I reached the last signal before she could stand upright.";
        case MISSING_SLEEP->"The bed was empty in every missing hour. The hallway camera showed my clothes and my face. Looking out from the bedroom, I saw arms held straight from the shoulders. I closed the other bedroom door before either of us reached it.";
        case LAYERED_MAN->own.getBoolean("Helped")?"I took the cleaver from the bench and opened the same wet layers he had been stripping. There was still a man beside the sump when I found the key. The steps were narrower on the way up.":"His knife broke. I heard it hit the concrete. I shut the cellar door from the top step. Something was still working its way into the frame.";
        case MOTION_LIGHT->"The light went out while I was looking at the courtyard. Behind me, a door had no hinges. I kept my hand away from the buzzer and found two little brass pins on the service door. That was the one I opened.";
        default->throw new IllegalArgumentException(place.id());
    });}
}
