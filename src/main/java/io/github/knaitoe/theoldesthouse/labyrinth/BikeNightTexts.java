package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.464, Bike night (docs/BIKE_NIGHT_PROPOSAL.md): what is written and said. This slice: the sheet taped up by the park's door (the
 * first paper), what the reader is told about the park and the bikes, and as much of the account as there is a night to give.
 * Every name, place and line is original.
 */
public final class BikeNightTexts {
    private BikeNightTexts(){}
    public static final String HINT="Whichever bike you take from the rack is yours for the night.";
    public static final String ARRIVE="A town park after dark, on the last night of October. A path runs from the shed to the gate, and by the gate six bikes stand in a rack.";
    /** A bike taken; a bike given back for another; a bike that is someone else's (their name goes in); one's own, already had. */
    public static final String YOURS="It is yours for the night.",SWAPPED="You leave the other one for whoever wants it. This one is yours for the night.";
    public static final String THEIRS="%s has that one tonight.",SOMEONES="Somebody has that one tonight.";
    /** A hand on the park's own things. */
    public static final String GATE="The gate is chained, and the padlock on the chain is new.";
    public static final String RAILINGS="Iron railings, taller than you are, with points on them.";
    public static final String LAMP="A park lamp. It gives a small light and a hum.",LAMP_FAILING="This one keeps going out. Each time it comes back a little later.";
    public static final String BENCH="A park bench. Somebody has cut initials into the top slat, and somebody else has cut them out again.";
    public static final String BIN="A wire bin, a third full: wrappers, a paper bag, a burst balloon.";
    /** 0.4.471: the bench by the playground; the drinking fountain, pressed and already running; the payphone, lifted and already held. */
    public static final String CARVED="A park bench. A heart cut into the top slat, long ago, and gone the same weathered way as the wood. Inside it: R.D. + A.L. Nobody has cut these out.";
    public static final String FOUNTAIN="You press the button and drink. The water is cold, and tastes of the pipe.";
    public static final String FOUNTAIN_RUNNING="It is still running.";
    public static final String PHONE="A payphone. You lift the handset: a dial tone. It wants a coin, and you have not got one.";
    public static final String PHONE_HELD="The dial tone goes on.";
    public static final String RACK="A bike rack bolted to the path. Six places.";
    public static final String BORDER="Squared timbers round the sand, worn pale where people step over them.";
    /** 0.4.465, the swings: a hand on the frame; what a reader on one is told once; coming down off one; a hand on the mark. */
    public static final String SWINGS="A steel frame with four swings on chains under it. The paint is worn through where hands go. Scratched into one post: T.R. WENT OVER THE BAR. Under that, in other scratches: DID NOT.";
    /**
     * 0.4.475, over the bar. Nothing says how it is done; the post is the park's only word on it. What a reader who has been over
     * adds under it, once; what they find there after; what anybody else finds there for the rest of that night; what the one who
     * goes over is told, the first time in a night; and what anybody near enough to see it is told.
     */
    public static final String SCRATCH_ADD="You pick up a stone and scratch under that: DID TOO.";
    public static final String SCRATCH_OWN="Under that, in your own scratches: DID TOO.";
    public static final String SCRATCH_FRESH="Under that, in fresh scratches: DID TOO.";
    public static final String OVER_BAR="All the way over the bar.";
    public static final String OVER_SEEN="%s goes all the way over the bar.";
    public static final String SWING_HINT="Lean with the swing to take it higher. Jump to let go.";
    // 0.4.474: what somebody pushing a swing is told is the swing's own to say (HouseReadSwing), since any swing can be pushed.
    public static final String JUMP="You come down %s paces out from the swings. The furthest tonight is %s.";
    public static final String JUMP_LEVEL="You come down %s paces out from the swings, level with the furthest tonight.";
    public static final String JUMP_BEST="You come down %s paces out from the swings. Nobody has landed further tonight.";
    public static final String JUMP_OFF="You come down off the sand. That does not count.";
    public static final String MARK="A heel mark dug into the sand: the furthest out anyone has landed off the swings tonight.";
    public static final String MARK_OWN=MARK+" It is yours.",MARK_THEIRS=MARK+" It is %s's.";
    /** 0.4.466, the slide: a hand on any part of it. */
    public static final String SLIDE="A slide. Its chute is worn bright down the middle and dull at the edges.";
    /** 0.4.477: off the end of the chute on a bike, having ridden it down from the deck. */
    public static final String SLIDE_BIKE="Down the slide on a bike. The chute rings under the tyres all the way to the sand.";
    /**
     * 0.4.467, the monkey bars: a hand on their ladders; a hand on the rungs from the ground, from on top of them and from too
     * far off; what a reader hanging from them is told once; and going from one end to the other.
     */
    public static final String BARS="Monkey bars. The paint is gone from every rung where hands go.";
    public static final String BARS_LOW="Out of reach from down here. Jump for them, or go up the ladder at either end.";
    public static final String BARS_ON="You are standing on top of them. Nobody is about to tell you to get down.";
    public static final String BARS_FAR="Not from here. Get under them.";
    public static final String BARS_HINT="Strafe to swing. Strafe again at the top of the swing to catch the next rung. Too early, and you drop. Sneak lets go.";
    /** 0.4.470: a hand let go before the swing had carried it to the next rung. */
    public static final String BARS_EARLY="Too early. Your hand misses the rung.";
    public static final String BARS_ACROSS="Across, from one end to the other, and not a foot on the sand.";
    /** 0.4.468, the seesaw: a hand on it; a hand on an end somebody is on; and what a reader sitting on it is told once. */
    public static final String SEESAW="A seesaw: a long board on a steel stand, with a seat and a handle at each end.";
    public static final String SEESAW_TAKEN="Somebody is on that end.";
    public static final String SEESAW_HINT="Jump to push off the sand when your end is down. Push just as you land, and it bounces. Sneak gets off.";
    /** 0.4.470: the other end came down hard, to the reader thrown off the top; and to the one who came down, of the one they threw. */
    public static final String SEESAW_THROWN="The bump throws you off the top.";
    public static final String SEESAW_THREW="The bump throws %s off the other end.";
    /**
     * 0.4.469, the merry-go-round: a hand on it that pushes neither way; a hand on it from on its deck, and from too far off; and
     * what a reader going round on it is told once.
     */
    public static final String ROUNDABOUT="A merry-go-round. Push it along its rim, the way it is to go.";
    public static final String ROUNDABOUT_ON="You are on it. It is pushed from the sand, by a rail.";
    public static final String ROUNDABOUT_FAR="Too far off to push it.";
    public static final String ROUNDABOUT_HINT="Sneak to hold on. Stand up on it when it is going fast and it throws you off.";
    /** 0.4.471, the climbing frame: a hand on it; and what a reader who gets to the top of it is told once. */
    public static final String DOME="A climbing dome: steel bars bolted into triangles, worn bright where hands and shoes go. Walk into it and you climb it. Keep low, and you can get in between the bars at the bottom.";
    public static final String DOME_TOP="The top of the dome. From here you can see over the railings to the street.";
    public static final String SPRING="A horse on a spring, bolted into the ground. Its paint is gone where knees go.";
    public static final String SPRING_TAKEN="Somebody is on it.";
    public static final String SPRING_HINT="Strafe to rock it. Rock with it, and it goes further. Sneak gets off.";
    /** The sand: a ball packed; no more to be carried; no room for one; what is left of one carried out of the night. */
    public static final String SAND="You pack a ball of sand. It holds.";
    public static final String SAND_FULL="Your pockets are full of sand.";
    public static final String SAND_NO_ROOM="You have nowhere to put it.";
    public static final String SAND_GONE="The sand has run out through your fingers.";

    // ------------------------------------------------------------------------------------------------ 0.4.476: Gus
    /** Hello, to the first to come; and to the ones after. */
    public static final String GUS_GREETING="You came! I wasn't sure anybody would. I've been here since it got dark.";
    public static final String[] GUS_GREET_MORE={"Hey! You made it.","Oh, good. More of us."};
    /** Of the bike a reader took, by its make. */
    public static String gusBike(String make){
        return switch(make){
            case "wasp"->"The Wasp. It turns on a dime. I've seen it turn on an actual dime.";
            case "hare"->"The Hare! Three pushes and it's gone. Try to stay on it.";
            case "comet"->"The Comet goes like anything. It doesn't stop like anything, though.";
            case "hopper"->"The Hopper. You can stand somebody on those pegs. Not me. Somebody.";
            case "lamp"->"The Lamp has a real light. It runs off the wheel. That's science.";
            default->"Oh. The Rattler. No, take it. I said take any. I meant it. Mostly.";
        };
    }
    /** About nothing. */
    public static final String[] GUS_IDLE={
        "My cousin ate a whole pie once. A whole one. He said it was worth it. He didn't look like it was worth it.",
        "Do you think dogs know it's Halloween? Ours hides under the porch. Maybe that's just our dog.",
        "If you count the railings there's a hundred and twelve. I counted twice. The second time I got a hundred and nine.",
        "My mom thinks I'm at the library. The library shuts at five. She doesn't know that. Don't tell her.",
        "I had a flashlight. It's at home. That's the whole story.",
        "That lamp's been doing that since school started. Somebody should tell somebody.",
        "What's the furthest you've ever been from your house? By yourself. I've been to the creek.",
        "They say the fountain water comes from under the graveyard. I say it comes from a pipe. I still don't drink it.",
        "If I had a dime I'd call the radio station. I don't know what I'd say. I'd think of something.",
        "You ever notice the swings always stop facing the same way? I have a theory. It's not finished.",
        "My brother says the dome was taller when he was little. I think my brother was shorter.",
        "I brought candy. I ate the candy. I'm telling you so you don't ask.",
        "A few more years and we can drive. Then nobody can chase us. Nobody on bikes, anyway.",
        "Do you smell leaves burning? I like that smell. It means somebody's home.",
        "The trick with the merry-go-round is don't look at anything. Or look at one thing. I forget which.",
        "I timed myself on the bars once. I didn't have a watch. I counted. It was a lot.",
        "If Tully comes, we don't say anything. That's the plan. That's the whole plan.",
        "Somebody cut a heart in that bench before we were born. I wonder if they're still, you know. A heart.",
        "You can hear the highway from here if you hold your breath. Go on. I'll wait.",
        "This is good. This is a good night. I'm saying it out loud so it counts."};
    /** Why nobody cuts through the graveyard on Halloween, told badly. It is never told properly. */
    public static final String[] GUS_STORY={
        "You know why nobody cuts through the graveyard on Halloween?",
        "Okay. So. A long time ago some kids did. On bikes. I don't know how many kids. Some.",
        "And they went in the front gate, and... my brother tells it better. He does a voice.",
        "Anyway there's a row of little stones at the back. All the same day on them. I've seen it. I didn't stay."};
    /** A hand on him: before the reader has a bike; and after, in turn. */
    public static final String GUS_TALK_NO_BIKE="Go and pick a bike. Any one. I'll wait here and not look like I care which.";
    public static final String[] GUS_TALK={
        "I'm fine. I'm just standing. Standing is a thing people do.",
        "You want to do the seesaw? Get on an end. I'm heavier than I look. It's the jacket.",
        "I can push you on the swings, if nobody else is going to. I'm good at it. I've got a system.",
        "Don't cut through the graveyard later. I put it in the plan. It's number six.",
        "Did you read the plan? I wrote it twice. The first one had a map, but the map was wrong.",
        "If we get split up, meet at the rack. No. Meet at the... we won't get split up."};
    /** What he says of what he sees. */
    public static final String GUS_SWAP="Changed your mind. That's allowed.";
    public static final String GUS_JUMP="%s paces! That's the furthest tonight. I'm not writing it down, but it is.";
    public static final String GUS_PAST="Whoa. Whoa. It's not supposed to go that high.";
    public static final String GUS_OVER="YOU WENT OVER THE BAR. I saw it. I'm a witness. Nobody is ever going to believe me.";
    public static final String GUS_OVER_AGAIN="Again! You did it again! I'm still a witness!";
    public static final String GUS_SLIDE="The trick is don't think about the bolts.";
    public static final String GUS_SLIDE_BIKE="You rode down the slide. On a bike. I'm telling everyone. I'm telling my mom, and she thinks I'm at the library.";
    public static final String GUS_BARS="All the way across! I can do four rungs. Four is normal.";
    public static final String GUS_EARLY="Too early. Everybody goes too early.";
    public static final String GUS_THROWN="Are you okay? That was the best thing I've ever seen. Are you okay?";
    public static final String GUS_FLUNG="It does that. It did that to me in the summer. I landed on my lunch.";
    public static final String GUS_DOME="Can you see my house from up there? It's the one with the porch light. Don't tell me if it's off.";
    public static final String GUS_SPRING="That horse is older than my dad. My dad said so. He didn't sound happy about it.";
    public static final String GUS_FOUNTAIN="You drank it. Okay. I'll remember you like this.";
    public static final String GUS_PHONE="It wants a dime. Everything wants a dime.";
    public static final String GUS_POST="T.R. is Teddy Rusk. He moved away, so nobody can ask him. That's how he gets away with it.";
    public static final String GUS_DID_TOO="You wrote DID TOO. Good. It's true. I saw.";
    /** A ball of sand that meets him: the first, the second and the third. After that he has said all he has to say about it. */
    public static final String[] GUS_SAND={
        "Hey! Okay. I probably had that coming. I don't know what for.",
        "Sand gets in your ears. It stays there. For years.",
        "I'm not mad. I'm just going to stand over here."};
    /** The seesaw: getting on the other end; and when he has been left on it. */
    public static final String GUS_SEESAW_ON="I've got this end. Push off when you're down. I'll do the same. That's all there is to it.";
    public static final String GUS_SEESAW_OFF="Good seesaw. That was a good seesaw.";
    /** The swings: going round to push; and with the swing as high as pushing takes it. */
    public static final String GUS_SWING_OFFER="Stay there. I'll push. I've got a system.";
    public static final String GUS_HIGH="That's as high as it goes. That's what everybody says, anyway.";
    // ------------------------------------------------------------------------------------------------ 0.4.479: the three who come
    /** What Gus says of the car, of them, of the padlock, of his cap, of each ball of sand that lands on one of them, and when Tully says go. */
    public static final String GUS_CAR="Car. Cars don't come down here this late.";
    public static final String GUS_THEM="Oh no. That's Tully. And Ray-Ray. And Duff. Duff ate a crayon once and asked for another.";
    public static final String GUS_GATE="That's HIS padlock? Who puts a padlock on a park?";
    public static final String GUS_CAP="Hey! Give it back! My mom wrote my name in that!";
    public static final String[] GUS_HIT={"You hit one. You HIT one. That was the best thing I ever saw.","Stop. No. Do it again.","We are so dead. Worth it. But so dead."};
    public static final String GUS_RUN="Bikes. BIKES. Go go go!";
    /**
     * 0.4.483, Gus and his bike: what he says of it when a hand is on it; about nothing, as he rides; of one of the three close behind
     * a reader; of one gone round to wait ahead of them; near the graveyard; and when every reader's bike has been had.
     */
    public static final String GUS_HIS_BIKE="That one's mine. It was my brother's. The flag was my idea.";
    public static final String[] GUS_RIDE={
        "I'm right behind you! Mostly!",
        "My legs. My legs are not happy.",
        "Do they ever get tired? I'm tired. I've BEEN tired.",
        "Is this a race? It's like a race. Are we winning?",
        "I know a short cut. I think. I knew one in July.",
        "This is the fastest I ever went on purpose.",
        "They never chase me. I don't know if that's good.",
        "Don't look back. I looked back. It didn't help."};
    public static final String[] GUS_BEHIND={"Behind you! Right behind you!","He's on you! Go! GO!","Pedal! Don't look, just pedal!"};
    public static final String[] GUS_ROUND={"They went round! One of them went round!","Not that way! One's waiting up there!","They split up. Since when do they split up?"};
    public static final String GUS_CHURCH="Not the graveyard. Anywhere else. I told you the story!";
    /** 0.4.490, in the cemetery: coming in after his friend, and past its first bank. At the row of small stones he says nothing. */
    public static final String GUS_YARD="We're in the graveyard. We are in the graveyard.",GUS_YARD_ON="Stay on the path. That's all I'm saying. Stay on the path.";
    public static final String GUS_NOBODY="Nobody even looked at mine. I'm not sure what I think about that.";
    /** Gus, when one of the three gets a bike. */
    public static final String GUS_CAUGHT="They got a bike. They actually got a bike.";
    /** What a reader is told when the gate is opened and when the three are sent after them; and what the open gate is, looked at. */
    public static final String GATE_OPENED="Three bikes come in at the gate. It stands open behind them.";
    public static final String GET_THEM_CUE="They are going for their bikes. The gate is open, and yours is where you left it.";
    public static final String GATE_OPEN="The gate stands open. The chain hangs from one leaf, and the padlock is gone.";
    /** Tully: at his padlock, at Gus's cap, in it, and what starts the chase. */
    public static final String TULLY_GATE="Who chained this up? Oh, right. Me.";
    public static final String TULLY_HAT="Nice hat.";
    public static final String TULLY_HAT_ON="How do I look? Smarter?";
    public static final String GET_THEM="Get them.";
    /** Duff, on the seesaw: taking an end, keeping somebody up on the other, and finding both taken. */
    public static final String DUFF_MINE="Mine now.";
    public static final String DUFF_UP="Going up. Enjoy the view.";
    public static final String DUFF_OFF="Off. One of you. I'm sitting there.";
    /** Their jeers. The first is said first; the rest come in a different order each night. */
    public static final String[] JEERS={
        "Look, it's the science club.",
        "Nice bike. Is it your mom's?",
        "Park's closed. Says who? Says me.",
        "Aw, did we interrupt recess?",
        "You know the sandbox is for babies, right?",
        "What are you supposed to be? Oh. Just you.",
        "Trick or treat. We'll take the treat.",
        "Wave to the dorks, Duff.",
        "Hey Gus. Still scared of the big slide?",
        "Bet you all hold hands on the way home.",
        "Don't stop on our account. This is hilarious.",
        "Somebody call their moms. It's past bedtime.",
    };
    /** One for what a reader is on, said once each. */
    public static String jeerAt(String on){
        return switch(on){
            case "swing"->"Swing higher, dork.";
            case "bike"->"Careful, the training wheels fell off.";
            case "seesaw"->"Takes two. Got a friend? Didn't think so.";
            case "slide"->"Don't cry at the bottom.";
            case "bars"->"Gonna drop. They always drop.";
            default->"Giddy up, cowpoke.";
        };
    }
    /** One of them hit with sand: the first, the second, and from the third on. */
    public static final String[] HIT={"Ow! Hey! Who threw that?","It's in my mouth. It's in my MOUTH.","My eyes! Okay. Okay, that's it."};
    /** A hand on one of them. */
    public static final String[] HANDS_OFF={"Hands off, dork.","Did I say you could touch me?","Go play in your sand."};
    /** Called after the readers, in the chase. */
    public static final String[] CHASE_JEERS={"You can't pedal forever!","Run, science club!","I'm keeping the hat!","Nowhere to go, dorks!","Slow down, we just want to talk!","Your bike's gonna be MY bike!",
        // 0.4.482, in the town: they call to each other as much as at the readers.
        "Go round! Go ROUND!","They went down the alley!","Cut them off at the bank!","I see you, science club!","Take the gap! The gap!","Nice shortcut. We know it too!","That's a dead end, genius!","Where's your sand now?"};
    /** In the hunt (0.4.482): what the one who has a reader's bike says, each his own, and what each calls as they go off with it. */
    public static final String[] CAUGHT={"Thanks for the bike!","Got it! I got the bike!","Finders keepers, science club."};
    public static final String[] LEAVING={"See you around, dorks!","Later, science club!","Nice doing business!"};
    /** What a reader who has been caught is told: with a bike to lose, and without; and when nobody is left to carry them. */
    public static final String CAUGHT_CUE="%s has your bike. You are down in the leaves.";
    public static final String CAUGHT_NO_BIKE_CUE="You are down in the leaves. They ride off laughing.";
    public static final String OUT_CUE="Nobody is coming back this way. The way home is the way you came.";
    /**
     * 0.4.484, the cars. What a driver calls at a rider who has sat in front of their car a while, one for each car (the wagon, the
     * pickup, the sedan, the fast one); what a driver says to each of the three, coming by him in the town; and what he answers.
     */
    public static final String[] GET_OFF={"Get off the road!","Hey! This is a street, kid!","It's the middle of the road! Move!","Move it! Some of us have places to be!"};
    public static final String[] TOLD_OFF={"Tully Marsh! Does your mother know where you are?","Raymond! I am calling your father!","Duff! You leave those kids alone!"};
    public static final String[] TOLD_BACK={"We're not doing anything!","It's Ray-RAY.","They started it!"};
    /**
     * 0.4.485, the people on the pavements. What one of a pair says at a stop of its round (a door; the laundromat's window; the
     * video shop's); what each lot of them calls to a reader riding by (the pair round Elm Court, the pair on Main Street, the
     * theater's line, the two on the hood); what is called at each of the three; what one of the line tells one of the three who
     * rides into it, and what he says back; and what a hand on one of each lot gets.
     */
    public static final String[] AT_A_STOP={"Trick or treat!","Somebody left a whole wash in there.","They've got the one with the giant ants."};
    public static final String[][] CHEERS={
        {"Go, kid, go!","Whoa! Did you see that?"},
        {"Nice bike!","Faster! Faster!"},
        {"Go, kid, go!","Coming through! Let 'em by!","Ride like the wind!"},
        {"Go on, kid! Lose 'em!","Now that is a bike."}};
    public static final String[][] BOOS={
        {"Leave 'em alone, Tully!","Go home, Tully!"},
        {"Pick on somebody your own size, Ray-Ray!","Boo! Go home, Ray-Ray!"},
        {"Leave 'em alone, Duff!","Duff! We know where you live!"}};
    public static final String[] NO_CUTTING={"Back of the line, Tully.","No cutting, Ray-Ray.","The line starts back there, Duff."};
    public static final String[] CUT_BACK={"I'm not even in your line!","Move! They're getting away!","I don't want to see your movie!"};
    public static final String[][] PEOPLE_TALK={
        {"We've done the whole court. Nearly.","The house at the top gives whole bars."},
        {"We can't go past the corner.","I got two the same. Want to swap?"},
        {"It's the late show. The one with the hands.","We've been here since seven.","No cutting. Not even on a bike."},
        {"The lot's closed. We're only sitting.","You should see what those three did to the swings."},
        {"Forty-one carts. I count them every night.","We're shut. I'm only bringing these in."}};
    /**
     * 0.4.488, the grocery's stock boy: what he says when his chain of carts has waited a while for a reader in its way; when a
     * reader rides through the gap in it; and to each of the three who is in its way.
     */
    public static final String[] CARTS_MIND={"Mind out! These don't stop easy.","Coming through. When you're ready."};
    public static final String[] CARTS_THROUGH={"Through the gap! Go on!","Mind the handles!"};
    public static final String[] CARTS_ROUND={"Not through my carts, Tully.","Go round, Ray-Ray. Go on.","Round the end, Duff."};
    /** Every line of the people on the pavements, for the case that holds them to how they talk. */
    // 0.4.489, the cemetery: what is cut on its stones, as a hand on one is told (CalderGraves.inscription).
    /** The row of small stones near the back: the proposal's "names and nothing else", and the one date. */
    public static final String[] SMALL_NAMES={"LUCY ARDEN","TOM ARDEN","BETH KOWAL","PETER RUIZ","WALT FINCH","JUNE ABBOTT"};
    public static final int SMALL_YEAR=1957;
    public static final String SMALL_DATE="OCTOBER 31, "+SMALL_YEAR;
    /** The town's families, the names their dead were given, and the lines some have under their years. */
    public static final String[] FAMILIES={"HOLT","ALDOUS","BRANNOCK","TILLEY","VOSS","HARROW","KEANE","LINDQVIST","MOTT","OAKES","SAYER","WHITLOW","YARROW","DUNMORE","GARRITY"};
    public static final String[] GIVEN={"EDITH","HAROLD","AGNES","CLARENCE","MABEL","ERNEST","IDA","HOMER","PEARL","LLOYD","HAZEL","CHESTER","NELLIE","ELMER","CORA","ORVILLE","VIOLA","AMOS","ETTA",
        "RUFUS","LOTTIE","SILAS","MINNIE","JASPER","OPAL","FLOYD"};
    public static final String[] EPITAPHS={"AT REST","GONE HOME","ASLEEP","IN LOVING MEMORY","UNTIL THE DAY BREAK","THE DAY'S WORK DONE","REMEMBERED","PEACE","TOGETHER AGAIN"};
    /** The year on each low marker's face (tools/draw_calder_graves_art.py draws the same four), and the years the rest died between. */
    public static final int[] MARKER_YEARS={1934,1911,1946,1898};
    public static final int FIRST_YEAR=1880,LAST_YEAR=1979;
    public static final String ANGEL_STONE="ADELAIDE CALDER. 1822-1901. WIFE OF AMBROSE.";
    public static final String OBELISK_STONE="AMBROSE CALDER. 1811-1889. HE BUILT THE MILL.";
    public static final String TOMB_DOORS="The doors are iron, and shut. CALDER is cut in the stone over them.";
    public static List<String> graveLines(){
        var all=new ArrayList<String>(List.of(SMALL_NAMES));all.addAll(List.of(FAMILIES));all.addAll(List.of(GIVEN));all.addAll(List.of(EPITAPHS));
        all.addAll(List.of(SMALL_DATE,ANGEL_STONE,OBELISK_STONE,TOMB_DOORS));return all;
    }
    public static List<String> peopleLines(){
        var all=new ArrayList<String>(List.of(AT_A_STOP));all.addAll(List.of(NO_CUTTING));all.addAll(List.of(CARTS_MIND));all.addAll(List.of(CARTS_THROUGH));all.addAll(List.of(CARTS_ROUND));
        for(var lot:CHEERS)all.addAll(List.of(lot));
        for(var lot:BOOS)all.addAll(List.of(lot));
        for(var lot:PEOPLE_TALK)all.addAll(List.of(lot));
        return all;
    }
    /** A hand on another bike, by a reader whose own the three have had. */
    public static final String NO_MORE="Yours is gone. These are other people's.";
    /** Every line of theirs, for the case that holds them to how they talk. */
    /** 0.4.490, at the cemetery. The proposal's own, of the one who stops at the fence. */
    public static final String NOT_GOING_IN="I'm not going in there.";
    /** 0.4.495: somebody in the Hollow has missed, on a bike and on foot. */
    public static final String UNDER_RIDING="You go down into the mist. Something in it has the back wheel, and lets go.";
    /** 0.4.499: caught in the Hollow. */
    public static final String TAKEN="You come round on the pavement outside the cemetery gate. The bike is gone.";
    /** 0.4.498: through the back gate. */
    public static final String THROUGH="The gate is behind you. Down the hill, the lights of Calder Falls are on again.";
    public static final String UNDER_WALKING="You go down into the mist. Something in it has your ankle, and lets go.";
    /** What each of the two who go in says when he stops: Tully, Ray-Ray. After it, nothing. */
    public static final String[] FALL_BACK={"Ray-Ray? Quit messing around.","Tully, wait up. I can't see."};
    public static List<String> bullyLines(){
        var all=new ArrayList<String>(List.of(TULLY_GATE,TULLY_HAT,TULLY_HAT_ON,GET_THEM,DUFF_MINE,DUFF_UP,DUFF_OFF));
        all.addAll(List.of(JEERS));all.addAll(List.of(HIT));all.addAll(List.of(HANDS_OFF));all.addAll(List.of(CHASE_JEERS));
        all.addAll(List.of(CAUGHT));all.addAll(List.of(LEAVING));all.addAll(List.of(TOLD_BACK));all.addAll(List.of(CUT_BACK));
        all.add(NOT_GOING_IN);all.addAll(List.of(FALL_BACK));
        for(var on:List.of("swing","bike","seesaw","slide","bars","spring"))all.add(jeerAt(on));
        return all;
    }

    /** Every line of his, for the cases that hold him to how he talks. */
    public static List<String> gusLines(){
        var all=new ArrayList<String>(List.of(GUS_GREETING,GUS_TALK_NO_BIKE,GUS_SWAP,GUS_JUMP,GUS_PAST,GUS_OVER,GUS_OVER_AGAIN,GUS_SLIDE,GUS_SLIDE_BIKE,GUS_BARS,GUS_EARLY,GUS_THROWN,GUS_FLUNG,GUS_DOME,GUS_SPRING,GUS_FOUNTAIN,GUS_PHONE,GUS_POST,GUS_DID_TOO,GUS_SEESAW_ON,GUS_SEESAW_OFF,GUS_SWING_OFFER,GUS_HIGH,GUS_CAR,GUS_THEM,GUS_GATE,GUS_CAP,GUS_RUN,GUS_CAUGHT));
        all.addAll(List.of(GUS_HIT));
        all.addAll(List.of(GUS_HIS_BIKE,GUS_CHURCH,GUS_NOBODY,GUS_YARD,GUS_YARD_ON));all.addAll(List.of(GUS_RIDE));all.addAll(List.of(GUS_BEHIND));all.addAll(List.of(GUS_ROUND));
        all.addAll(List.of(GUS_GREET_MORE));all.addAll(List.of(GUS_IDLE));all.addAll(List.of(GUS_STORY));all.addAll(List.of(GUS_TALK));all.addAll(List.of(GUS_SAND));
        for(var make:List.of("wasp","hare","comet","hopper","lamp","rattler"))all.add(gusBike(make));
        return all;
    }

    private static List<Component> pages(HouseWriting.WritingStyle style,String... texts){var out=new ArrayList<Component>();for(String text:texts)for(String p:LookoutTexts.pages(text,150))out.add(HouseWriting.page(style,p));return out;}
    /** A sheet from a school pad, in pencil. (Not the House's child's hand, which is a small child's and loses letters: Gus can spell.) */
    public static ItemStack source(){
        return HouseWriting.book("A page from a school pad","Gus",pages(HouseWriting.WritingStyle.PLAIN,
            "HALLOWEEN PLAN. Secret.\n\n1. They say we are too old for trick or treat. Fine.\n\n2. Meet at Calder Park when it is dark. The gate will be chained. Come through the tool shed. The lock on it has been broken since summer.",
            "3. Bring your bike. If you have not got one there are always some in the rack that nobody comes back for.\n\n4. Bring a flashlight. Bring candy if you have any.",
            "5. If Tully and those guys turn up do NOT say anything. Remember last time.\n\n6. Do NOT cut through the graveyard. I mean it. You know why.\n\nGus\n\nP.S. The one with the card in its spokes is mine. Kidding. Take any."));
    }

    /** What a bike is called in the account. */
    static String bike(String make){
        return switch(make){case "wasp"->"the Wasp";case "hare"->"the Hare";case "comet"->"the Comet";case "hopper"->"the Hopper";case "lamp"->"the Lamp";case "rattler"->"the Rattler";default->"";};
    }
    /** The account, from the reader's own record. There is only the park to tell of yet. */
    public static ItemStack account(String name,CompoundTag own){
        var p=new ArrayList<String>();
        p.add("It was the last night of October in a town called Calder Falls, and I was in the park after it had shut.");
        String bike=bike(own.getString("Bike"));
        p.add(bike.isEmpty()?"There were six bikes in the rack by the gate. I did not take one.":"There were six bikes in the rack by the gate. I took "+bike+".");
        // 0.4.476: Gus, if they met him; and his story, if they were there for the end of it.
        if(own.getBoolean("GusMet"))p.add("Gus was there, in his cap, talking the whole time about nothing."+(own.getBoolean("GusStory")?" He told me why nobody cuts through the graveyard on Halloween. He told it badly.":""));
        // The playground, as far as this reader went on it.
        int far=own.getInt("SwingBest");String pushedBy=own.getString("SwingPushedBy"),pushed=own.getString("SwingPushed");
        if(far>0)p.add("I got one of the swings as high as it would go"+(pushedBy.isEmpty()?"":", with "+pushedBy+" pushing,")+" and let go of it, and came down "+Stillness.words(far)+" paces out in the sand.");
        else if(own.getBoolean("Swung"))p.add(pushedBy.isEmpty()?"I sat on one of the swings for a while.":"I sat on one of the swings, and "+pushedBy+" pushed me.");
        if(!pushed.isEmpty())p.add("I pushed "+pushed+" on the swings.");
        // 0.4.475: over the bar, and who saw.
        int overs=own.getInt("OverBar");String seenBy=own.getString("OverSeenBy"),sawOver=own.getString("SawOver");
        if(overs>0)p.add("I went all the way over the bar on one of the swings"+(overs>1?", and then did it again":"")+". "+(seenBy.isEmpty()?"Nobody saw.":seenBy+" saw.")
            +(own.getBoolean("Scratched")?" I scratched DID TOO into the post, under DID NOT.":""));
        if(!sawOver.isEmpty())p.add("I saw "+sawOver+" go all the way over the bar on the swings.");
        if(own.getInt("Slid")>0)p.add(own.getInt("Slid")>2?"I went down the slide, and then went round and did it again.":"I went down the slide.");
        String slideBike=bike(own.getString("SlideBike"));
        if(!slideBike.isEmpty())p.add("I rode "+slideBike+" up the slide, turned it round at the top, and rode it down again.");
        if(own.getInt("Crossed")>0)p.add("I went all the way across the monkey bars, hand over hand, without a foot on the sand.");
        else if(own.getInt("BarsMissed")>0)p.add("I went for the next rung of the monkey bars too early, and dropped into the sand.");
        else if(own.getInt("Hung")>0)p.add("I hung from the monkey bars for a while, and dropped.");
        String with=own.getString("SeesawWith");
        String threw=own.getString("SeesawThrew");
        if(!with.isEmpty())p.add("I went up and down on the seesaw with "+with+" on the other end."
            +(own.getBoolean("SeesawThrown")?" We got it bouncing, and a hard bump threw me off the top of it.":!threw.isEmpty()?" We got it bouncing, and I bounced "+threw+" off the top of it.":""));
        else if(own.getInt("Seesaw")>0)p.add("I sat on one end of the seesaw with nobody on the other, and pushed myself up, and came down again each time.");
        else if(own.getBoolean("SeesawSat"))p.add("I sat on one end of the seesaw for a while.");
        int rounds=(int)(own.getDouble("RoundTurned")/(Math.PI*2));
        if(own.getInt("RoundFlung")>0)p.add("I stood up on the merry-go-round while it was going fast, and it threw me off into the sand.");
        else if(rounds>0)p.add("I went round on the merry-go-round "+(rounds==1?"once":Stillness.words(rounds)+" times")+".");
        else if(own.getInt("RoundPushed")>0)p.add("I pushed the merry-go-round and watched it turn.");
        if(own.getBoolean("DomeTop"))p.add("I climbed to the top of the climbing dome and looked out over the railings.");
        if(own.getInt("Sprung")>0)p.add("I rocked the horse on its spring as far as it would go, for no reason at all.");
        else if(own.getBoolean("SpringSat"))p.add("I sat on the horse on its spring.");
        if(own.getBoolean("Drank")&&own.getBoolean("Phoned"))p.add("I drank from the fountain, and lifted the payphone by the door to hear its tone, with no coin to put in it.");
        else if(own.getBoolean("Drank"))p.add("I drank from the fountain. The water tasted of the pipe.");
        else if(own.getBoolean("Phoned"))p.add("I lifted the payphone by the door to hear its tone, with no coin to put in it.");
        if(own.getInt("Sand")>0)p.add(own.getInt("SandHits")>0?"I packed the sand into balls and threw them, and not all of them missed.":"I packed the sand into balls and threw them.");
        // 0.4.479: the three who came.
        if(own.getBoolean("Bullies"))p.add("Then three more came on bikes of their own, Tully and Ray-Ray and Duff. The padlock on the gate was Tully's, and he undid it."
            +(own.getBoolean("CapTaken")?" He took Gus's cap and wore it.":""));
        int struck=own.getInt("BullyHits");
        if(struck>0)p.add("I threw sand at them, and "+(struck==1?"one ball":Stillness.words(struck)+" balls")+" of it landed.");
        if(own.getBoolean("GetThem"))p.add("Tully said to get us.");
        return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.BIKE_NIGHT),name,pages(HouseWriting.WritingStyle.WILL,p.toArray(String[]::new)));
    }
}
