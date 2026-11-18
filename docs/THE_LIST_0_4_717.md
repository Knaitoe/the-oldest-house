# The list, part 4d-2: the stock tank (0.4.717)

The second of the pasture's set pieces (`docs/THE_LIST_PLAN.md`), after the pumpjack (`docs/THE_LIST_0_4_716.md`). Like the others it
is begun by the slasher's hand on the thing it is done with, and by hand until a night uses it.

## The tank

The stock tank stands by the windmill in the pasture (`docs/THE_LIST_0_4_701.md`): galvanized, seven across, full nearly to the rim.
Its wall runs round the water in straight runs with corners between them.

1. **Over the rim.** The slasher with hold of one of the five near a straight run of the tank's wall puts a hand on it (or strikes it).
   They are pressed against the wall and bent over it, and the head is pushed under the water. Their own words, a scream into the
   water; what they had in hand they let fall. Anyone on the list near enough runs, as in 4a. A hand on a corner, or on the wall with
   nobody held, is a look, as before.
2. **Under.** While the head is under, the breath goes and the water churns over it. They fight as they fight a grip (4a-2), quicker
   while the slasher looks away, and the count full brings the head up out of the water, gasping, with some breath back.
3. **Up.** The head up, the slasher has a little over a second to put a hand on them, or on the wall, and push it back under. Left
   alone, they tear loose and run. The slasher going from the town lets them up the same way.
4. **The third time.** The third time the head goes under, it stays under: they can no longer fight it up. The breath gone, they go
   still, slumped over the rim with the head under and the arms hanging down into the water. The slasher is told; anyone in town within
   forty blocks who is not the slasher is told from which way a scream stopped. The kill is kept in the slasher's record, even if they
   have logged off since. There is no blood: the churn stops and the water settles round the head.
5. **After.** A hand on the wall there finds nothing moving in the water; a hand on them is told they do not move.

## Found

As in 4a-3: the rest of the five who see them say so (Shannon's own line says where), and calm ones run; a reader who is not the
slasher, near and looking, is told once a hunt that they hang over the rim of the stock tank with the head under the water.

## Single and multiplayer

- **Alone**, the reader catches one of the five, brings them out to the tank, puts them over the rim, and keeps putting the head back.
- **With others**, only the slasher holding them puts the head under or back; another reader's hand on them, or on the wall there,
  finds them held under and changes nothing (what Amber's side can do about that is part 8's). The splash, the churn and the gasps
  are the same for everyone near.
- **Spectators** touch nothing and are told nothing.

## Retreat

As with one held (4a-2): while the slasher holds one of the five at the tank, the House's door is shut to them and they cannot walk away
from it. Once the one in the water has gone still, the hold is let go: the slasher walks, and the door is open to them again.

## Ended

The hunt ended (or begun again), everyone goes back to their place, unmarked.

## Made as

- **`TheListTank`** (new): `into` (the way into the water from a straight cell of the wall; none for a corner, a seam or the water);
  `stand` (lying over the wall from the dry side: the rim is higher than the hips, so the hips rest on its top and the feet hang off the ground), `head` and `surface` (the water over the head); `REACH` 2.6,
  `BREATH` 140, `SURFACE` 24, `LAST` 3; `hand` (a hand or a blow on the wall: the hunt's first, otherwise a look).
- **`TheListHunt`**: the state `TANK`; gestures `DUNKED` (20), `SURFACED` (21) and `SUNK` (22); `DROWNED` (64, the marks bit);
  `tank(player, base, wall, in)`, `dunked`, `over`, `splash`, `dunk` (the head back under), `tanking` (each tick: the breath, the churn,
  the struggle at a grip's strength, the moment up), `surfaced`, `drowned`; `under`, `dunks` and `breath` for tests. A hand or a blow
  from the slasher on one come up puts the head back; anyone else's finds them held under. One drowned is found by the back, near the
  rim.
- **`TheList.click`**, **`TheListBody.punched`**: a hand or a blow on the tank's wall asks `TheListTank.hand`.
- **Client**: three poses (`LiteraryRenderers.CastModel.hunted`): bent over the rim from the hips with the head pushed under, the hands
  on the rim shoving and the feet scrabbling; come up, the head thrown back gasping; still, slumped over the rim with both arms hanging
  into the water. The drowned face (`TheListMarks`, `drowned.png`).
- **Sound**: `the_list.dunk` (the plunge and slap, the churn, bubbles breaking, a cry pushed out under the water, the rim knocked),
  subtitled; the gasp coming up is the existing `the_list.gasp`.
- **Art** (`tools/draw_the_list_art.py`): `drowned()` over the five's skins: the face gone dull, the lips dark, the eyes bloodshot,
  froth at the nose and the mouth and running from its corner, the hair soaked flat into wet strands clinging down over the brow.

## Tests

`TheListTests.theTank` (new, batch `the_list_tank`): the north wall leads south into the water; a corner and the water are no wall.
Another reader's hand on the wall is a look. Deek taken from behind on the home bench and brought to the tank. The slasher's hand on the
wall forces him over the rim with the head under: the slasher's cue, his words, pressed to the wall, still held with no retreat, the
other reader hearing him go under. The other reader's hand on him, and on the wall: held under. He fights the head up, gasping, and the
slasher's hand on him puts it back; up again, a hand on the wall puts it back a third time, and it stays. Drowned: slumped over the rim,
the face marked, the slasher told, the other reader told a scream stopped, the kill kept by the slasher, the hold let go. A hand on the
wall then: still; a hand on him: he does not move. The other reader, looking, finds him at the tank. Ended: Deek back and unmarked.

## Seen in the frames

Eye-level frames of Deek held under, come up, and drowned, each from west, east, north, across the water and close. The first frames
showed the head level with the water and the face looking out across it: the rim is higher than a standing body's hips, so bending
from the hips could not bring the head under. He is now laid over the rim, the hips on its top and the feet off the ground, the back
bent past level and the head turned down. Held under, only the back of the head and the shoulders show over the rim from across the
water; come up, the head is thrown back above it with the hands braced; drowned, he lies over it with the head under and the arms
hanging into the water.

## Every new line

Speech, over the speaker's head as their name, forced over the rim:

- Deek: "Not the water. Come on. Not the water."
- Wes: "I can't swim! You know I can't swim!"
- Mel: "Let go of my hair! Let go!"
- Lesley: "It's in my mouth! It's in my mouth!"
- Shannon: "Jenna! Jenna, the tank! I'm at the tank!"

Shannon, coming across one of the others at the tank (the others keep their lines from 4a-3): "That's %s. That's %s in the tank."

Grey cues on the action bar, to the slasher:

- "You force %s over the rim of the tank and push the head under the water."
- "The head comes up out of the water, gasping."
- "You push the head back under."
- "You push the head back under. This time you hold it there."
- "The water stops moving round the head. %s goes still."

To anyone else whose hand is on one held under, or on the wall there: "%s is held over the rim of the tank, the head under the water."

To anyone whose hand is on the wall where one drowned: "%s hangs over the rim, the head under the water. Nothing moves in it."

To a reader who is not the slasher, coming across them: "%s hangs over the rim of the stock tank, the head under the water."

Subtitle: "Someone is held under water".
