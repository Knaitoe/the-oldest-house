# Borrasca: speech, the knife and the wire (0.4.429)

Five notes from the owner's playtest of 0.4.427 and 0.4.428:

- "text still needs to be centered, framed in center of player's vision and clear itself of obstacles"
- "spoken npc dialogue doesn't track to journal"
- "children should be pushed out of the bar rather than teleported out"
- "players should be able to take the knife from the kid, use it to carve their name."
- "players can jump in from here" (a screenshot of the wire round the refinery yard)

## What people say is a caption

Until now an actor's line was its nameplate: one line over its head, unwrapped, which ran off the screen up close and went behind
whatever stood in the way. It is now a caption (`client.SpeechCaptions`, a GUI layer under the chat).

- The line is wrapped to at most 300 pixels and five lines, centred, on a dark frame below the middle of the view, above the
  action bar and the hotbar. It is drawn over the world, so nothing stands in front of it.
- The speaker's name comes first, in grey, when the scene gives one (every Borrasca line does). `LiteraryActor.say(speaker,text)`
  carries the name; it is synced to clients with the line.
- Up to three people can speak at once. The nearest is lowest and the others stack above it.
- A speaker who is not in the middle of the view gets a small mark on the side the voice comes from.
- A line fades a little with distance and is not shown beyond 24 blocks.
- An actor kept private to another reader's scene is never sent to this client, so neither are its lines.
- Nameplates are not drawn for actors at all now. This applies to every scene that uses actors, not only Borrasca.
- Borrasca's lines are no longer repeated on the action bar.

## What people say goes in the journal

`TalkHistory.overheard`: every new line an actor says aloud is kept for each reader within 24 blocks who can see the speaker. It goes
into one record per place and day, under "What was said" in the journal. The record's heading names up to three people who spoke in
it, and each line keeps its speaker's name ("Someone" when the scene gives none). A line said again while it is still up is not
kept twice. The Barretts' filming days keep their own record, as before. The conversation panel's exchanges are kept as before.

## Pushed out, not set down

`BorrascaPeople.shove`: the child is walked out by the grown-up who finds them.

- In the bar, Gus says his line, opens the door, comes round the bar and walks the child out onto the sidewalk.
- In the historical society, Mr. Rowe says his line, opens the society's door and walks the child back out through the shop, to the
  front by the door.
- The way out is the shortest way through blocks a child fits through (`nextStep`, a search on the blocks round them, doors counted
  open). Every few ticks the child is given a shove along it, about a block's worth, and the grown-up walks on behind them.
- The child can push back against it for a while, but it keeps coming. A shove that has not got them out in fifteen seconds is
  given up; nobody is lifted out.
- The door swings shut behind them unless somebody is standing in it. The grown-up walks back to the counter.

## Her knife, his name

On the first visit, Kelly cuts her name in the Triple Tree and holds her pocketknife out: "Your turn. Take the knife."

- Clicking her takes the knife: it goes into the reader's empty hand, or anywhere it fits. "Careful. It's sharp."
- Clicking the bark with the knife in hand cuts the reader's name, a few letters at each cut (`BorrascaStory.CUTS`, four cuts),
  with the sound, the arm moving and chips of bark. The name shows in the bark as it is cut.
- Bare hands cut nothing. She says "Here. Take the knife." if she still has it; a knife in a pocket gets "The knife needs to be in
  your hand."
- When both names are there, she says "There. Now it has to keep a place for both of us." A few seconds later she asks for it back.
  Clicking her hands it over ("Thanks. My dad counts his knives."). If it is not given and the reader is next to her, she takes it
  back out of their hand.
- The knife is one of the reader's own Borrasca originals (`BorrascaBelongings.KNIFE`), owned by them alone and never copied. It
  does not leave the hill: if the reader goes with it still in a pocket, it is gone from the pocket, back with her.
- The account adds "She lent me her knife to cut mine." and, if it was not given back, "I nearly went home with it."

## The wire

From some of the ground outside the refinery's wire a running jump carried a reader over the top: the slopes on the south side west
of the gate, and the ground by both north corners, stood three or four blocks above the yard within a few blocks of the wire.

- Out to nine blocks from the wire the ground now stays under a line rising four blocks in five from beside the wire
  (`BorrascaLand.ground`). Beyond that the valley's walls stand as they did.
- The wire's mesh now stops a body half a block above its top, as a fence does.
- `BorrascaTests.nobodyJumpsTheWireFromTheGroundOutsideIt` walks every place a child can reach on the hill and checks that no
  sprint jump from any of them, worked out tick by tick, clears the top. Before the change it found 39 such places.

## Numbers

Protocol 173 (an actor's speaker is synced with its line, so an older client cannot join). Witness 86/65/63 and layout 135 unchanged.
New builds only.
