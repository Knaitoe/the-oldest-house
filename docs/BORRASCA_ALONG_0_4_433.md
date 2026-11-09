# Borrasca: as far as the first marker (0.4.433)

The owner, asking for the longer hill: "The walk gives Nell and the player and other potential npcs, if not occupied by players, time
to talk and bond." 0.4.432 gave Kelly things to say on both walks. This gives the other two children their part of the first one.

## Patrick and Kendra come part of the way

On the first visit, once Kelly has set off for the Tree, Patrick and Kendra leave the street and come up the trail behind her in single
file on her line, Patrick a few steps behind her and Kendra behind him (where the street gives out to the woods the way is too narrow to
walk abreast). They never pass her: while she is close ahead they hang back, and while she waits for a reader who has fallen behind, so
do they. At the marker they stand either side of the trail.

On the way each says one thing to a reader near enough to hear:

- Patrick: "Race you to the first marker. Kelly doesn't count, she stops for every bird."
- Kendra, after him: "He says that every time, and then he walks."

They stop at the first marker. When the reader comes within ten blocks of them there, they say goodbye:

- Kendra: "This is as far as I go. My mom can see the marker from our porch."
- Patrick: "I'm going up by the second marker some other time. Don't tell my brother."

Then they turn and walk back down the trail. The moment nobody is watching them they are home again in town, where they started. If no
reader comes to the marker within a minute (the reader ran on to the Tree, or hung back in town), they go home without saying goodbye.

By the second visit Patrick is missing, and Kelly tells the reader so on the way to the mill (0.4.432).

While they are on the trail with Kelly they do not greet the reader as they would in town ("Kelly's down by the sign.").

The account adds, when the reader heard them at the marker: "Patrick and Kendra came with us as far as the first marker. Patrick said
he would go up by the second one some other time."

## Other readers

Borrasca's site is each reader's own, so nobody else can be standing where Patrick or Kendra would be. Letting another reader walk up in
a child's place belongs to the co-op part of the rework, still to come.

## Code and tests

`BorrascaStory.tagAlong` runs on the first visit before the visit's story (`ALONG`: the trailhead, partway up and the first marker;
`EliAlong`/`WrenAlong` steps, `AlongStarted`, `AlongWrenSaid`, `AlongHeard`, `AlongHome`, `AlongWait` up to `ALONG_WAIT` 1200 reader
ticks, `AlongDone`). Their walk lines use 0.4.432's `walkTalk` (`EliAlongTalk`, `WrenAlongTalk`). Going home they walk back down and are
put back at `BorrascaCast.target` only when neither they nor that spot is watched (`BorrascaCast.recall`).
`BorrascaTests.patrickAndKendraWalkUpAsFarAsTheFirstMarker` walks beside Kelly, checks the two follow her up and stop at the marker on
either side of the trail, hears their goodbye (and finds it in the journal), then turns up the hill and checks they are home in town
and that the account remembers them.

## Numbers

No protocol, Witness or layout change: protocol 173, Witness 86/65/63, layout 135. New builds only.
