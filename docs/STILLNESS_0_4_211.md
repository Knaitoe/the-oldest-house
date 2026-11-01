# The stillness (0.4.211)

The third of the dark halls' horrors (docs/DARK_HALL_HORRORS_PROPOSAL.md): the growl, made flesh. The owner, 1 November: these
"should be the most extreme yet, either psychologically or physically"; "real deaths are the right call".

Slot 141, layout 67, no protocol change. One new Witness story: **64 sources / 48 required / two kinds / three endings**.

## Where it is dealt

Only in the dark band, from depth sixteen (`DarkHalls.DARK_DEPTH`): one door among several, with the usual story spacing
(`DarkHalls.horror`). It is a one-shot room; a failed visit can be dealt again.

## The hall

Built whole in the dark halls' ash (`StillnessSite`), with no light of its own:

- **The hall:** eleven wide, three high (a hand can touch the ceiling) and forty-eight long, with square pillars down both sides
  every six blocks.
- **The doors:** one at each end. The far door is the way on.
- **The source:** a surveyor's log page nailed to the wall just inside the way in.
- **The account:** a count scratched into the dust before the far door, in rows of five.

## What happens

When a reader is past halfway, a growl comes from the far door and something enormous walks the hall: the Minotaur's voice and
weight. Nobody ever sees it.

**What it leaves:**
- Its hoofs shake the floor and throw up the dust.
- It leaves cloven prints in the dust, one at a time, wherever it walks. Readers with a light can watch them appear.
- While it is in the hall, neither door lets anyone out.

**It hunts by sound.** Each noise carries a set distance:

| Noise | Carries |
|---|---|
| Standing still | nothing |
| Crouch-walking | 3 blocks |
| Walking | 10 |
| A jump | 14 |
| Running | 24 |
| Food, a dropped thing, a door | the whole hall (48) |

- **To a sound it goes fast:** about a sprint, snorting.
- **When it hears nothing,** it finds the nearest reader it has not yet stood over by smell, slowly, snuffling at the floor.

**When it reaches a reader, it stands over them** for twenty to forty seconds.
- Its breath comes down on them every three seconds. Each breath fogs their sight and is counted on their screen: "Two."
  "Three."
- Now and then it shoves them with its head, a real push, to see if they will move.
- Their heart climbs until the screen shakes with it.

**Moving while it stands over them** (a step, a jump, food, a dropped thing) **is a goring:**
- With nothing raised, it is a real House death: the Mother's custody, and the portrait if it is the first. The damage type is the
  new `stillness`, through armour and the grace after joining.
- **A raised shield** takes it, once. The shield is cracked (it counts toward the Minotaur's three) and two hearts get through.
- **A survivor** is thrown back out of the door they came in by.

A short grace when it arrives lets a reader who was still moving come to a stop. Its own shoves never count as moving.

## How it ends

**Survival:** once it has stood over everyone in the hall, it walks back to the far door. The hoofs fade, and go through the door
without its opening. A reader it stood over who then reaches the far door finds the count scratched into the dust, in their own
hand (`let_it_breathe`).

**Failure:** the goring: death, or thrown out to try again. The account remembers how many times.

The Witness line: "I let it breathe on me." Kind: survival.

## Co-op

The thing, its hearing and the hall are shared; the counts and outcomes are each reader's own.

- One sound draws it to everyone.
- **A reader can draw it off a friend** by making a noise: it leaves the one it is standing over (who is spared, and counted as
  stood over) and comes for the one who made the noise, who must then stand still themselves.
- Both accounts remember who drew it off whom.

## Literature

**The source:** a page from a survey log, unsigned:
- the hall's measurements;
- the prints in the dust;
- D., who ran;
- the writer, who couldn't run and so lived. It stood over them for thirty-one breaths and pushed them twice.
- It ends: "When you hear the hoofs, stop where you are. ... It doesn't see. It listens, and then it smells, and then it waits for
  you to move. Don't."

**The account:** "The account: the count", in the reader's own words:
- the number of its breaths, in words;
- how many times it pushed them;
- the times they moved and were put back out;
- the friend who drew it off them, or the friend they drew it off;
- it ends with the count scratched into the dust, "so that there would be a record that it had been ... and not one more."

**Elsewhere:**
- the co-op line;
- the miniature (a long, low matchbox with a ring of cloven prints pressed into the wax around a pinhead);
- the staircase prose ("the low hall where it breathed on me");
- the co-author pool;
- the advancement under survival.

**Limits held:**
- nothing gory is shown;
- the thing is never seen;
- the reader's gender, colour and age are never named;
- lines are on the action bar, never in chat.

## Art and sound

`tools/draw_horror_art.py` adds three pieces to the shared `horror_fitting` block:
- the log page;
- the tally in the dust;
- the cloven print (no shape, so it never stops a step).

`tools/generate_horror_audio.py` adds four subtitled cues:
- the breath overhead;
- snuffling at the floor;
- a heavy head shoving;
- the horns.

The hoofs, the bellow and the growl are the Minotaur's own (`FinaleRegistry`, `Growl`), and the heartbeat is the storeroom's.

The breath's fog and the shaking are a new mode (21) of the existing novel-scene presentation, so there is no protocol change.

## Tests

Five `StillnessTests` in the literary namespace:
- dealt only in the dark band, with the page, the pillars and the far door;
- past halfway it comes in from the far door and goes to what it hears;
- still under it, the breaths are counted, it moves on, and the far door holds the count;
- moving under it is a goring, which a raised shield takes once;
- a noise draws it off a friend, who must then stand still.
