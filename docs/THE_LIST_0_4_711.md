# The list, part 4b-1: the boards (0.4.711)

The first of the set pieces of *The list* (`docs/THE_LIST_PLAN.md`), after the hunt, the catch and the dead found (part 4a,
`docs/THE_LIST_0_4_708.md` to `docs/THE_LIST_0_4_710.md`). The plan listed the set pieces by place, the gin first. The hunt is at the
school and on Main Street, and the five are only taken to the gin on night four, so the school's come first: the boards on the home
stands (this part) and the sprinkler line on the field (4b-2). The drive-in and the pasture follow, and the gin last.

The hay hook finishes one held anywhere. A set piece is the place's own finish, for one held there: the slasher's hand on the thing it is
done with begins it instead of the hook. Like the hunt, it is begun by hand until a night uses it.

## The boards

The home stands are eight rows of aluminum planks, each half a block above the last, with nothing between one row and the back of the
next: the gap under each plank is open, down to the dirt under the stands. Mel and Lesley sit on the seventh row. Any row but the
lowest and the top will do: the lowest has its own plank's block filling the gap under the next, and the top has none above it.

1. **Over the row.** The slasher has hold of one of the five on the stands, within three blocks of a plank, and puts a hand on that
   plank. They are forced down on their knees on it, facing up the rows, bent over its far edge with the throat across it, the head
   through the gap under the next row up and hanging in the dark under the stands, the hands flat on the plank either side. Never
   across a post of the frame: on the block beside it, toward where they were. They say their own words and let fall what they hold
   (Mel's camcorder). Anyone else on the list near enough hears the scream and runs, as in 4a.
2. **Held there.** The slasher's walk stays stilled and the House's door stays shut to them, as while holding anyone. Left there without
   a stamp, they fight the plank as they fight the grip: looked at, they hold longer; looked away from, they tear loose sooner. Torn
   loose, they shove the slasher back and run, as in 4a-2.
3. **The stamps.** The slasher's hand or a blow on the plank they kneel on, or on the next row's plank over the back of their neck, or on
   them, is a stamp. Three stamps, at least half a second apart (one sooner counts for nothing):
   - the first: the aluminum rings, they jerk, and blood drops from the head onto the dirt under the stands. Torn loose after this, it
     takes them twice as long.
   - the second: something gives. Blood sprays onto the dirt below, there is more blood on the slasher, and the neck is marked. They
     cannot tear loose now.
   - the third: they go still. The scream stops short, and anyone in town within forty blocks who is not the slasher is told from which
     way. They hang over the row, the arms down over its edge, the head under the next plank. A pool spreads on the dirt under the
     head. The slasher's hold ends there: the body walks again and the House's door opens to it.
4. **Dead.** They hang there until the hunt ends. Anyone's hand on them is told they do not move. The kill is kept in the slasher's
   record, as with the hook.

## Found

As in 4a-3: the rest of the five who see them say so, and calm ones run; a reader who is not the slasher, near and looking, is told
once a hunt: "%s hangs over a row of the stands, not moving." Either the body on the plank or the head under the next one will do, so
a reader under the stands, looking up at the head, finds them too.

## Single and multiplayer

- **Alone**, the reader has the body and the stands to themselves.
- **With others**, one held is one slasher's; a hand on a plank begins it only for the one who has hold of them, and only theirs stamps.
  Everyone near hears the plank ring and the scream; everyone in town within forty blocks who is not the slasher is told from which way
  it stopped. The marks on the neck, the stains and the body are everyone's to see.
- **Spectators** touch nothing and are told nothing.

## Retreat

As while holding anyone: the House's door is shut to the slasher from the moment of the hold until they are dead or have torn loose.

## Made as

- **`TheListHunt`**: the state `BOARDS` (held over a plank); gestures `OVER` (10, on the knees over the row, the head through the gap)
  and `HUNG` (11, hanging dead over it); `NECK` (2, the marks bit); `row` (which row a plank is, 1 to 6, or -1), `boards(player, rel)`
  from a hand or a blow on a plank, `over`, `boarded` (each tick), `stamp`, `ground` (the dirt under the head, beside a post if one
  stands there), `head` and `stamps` (for tests); `plank`, `kneel`, `stamps`, `stampAt` and `dropped` on the runner; `BOARDS_REACH` 3,
  `STAMP_GAP` 10 ticks, `HEAD` (where the head hangs from where they kneel). The dead are found by a clear line to the body or the head.
- **`TheList.click`** and **`TheListBody.punched`**: a hand or a blow on a plank of the stands (stages 0 and 1) asks the hunt first.
- **`LiteraryActor`**: the over and hung take a kneeling box (0.8 by 0.9), set where they kneel.
- **Client**: the two poses (`LiteraryRenderers.CastModel.hunted`: the hips up off the plank, the legs back along it, the back bent past
  level over the edge; the hands flat out to the sides while alive, the head straining and twisting; dead, the arms hanging over the
  edge and the head hanging under the next row); the neck's mark (`TheListMarks.Wound`, `neck.png`).
- **Sounds**: `the_list.stamp` (the plank rings under a stamp), `the_list.crack` (something gives), subtitled.
- **Art** (`tools/draw_the_list_art.py`): the neck mark over the five's skins: a crease of broken skin across the nape and the top
  of the collar behind, bruised either side, blood soaked into the collar's lip and run from the nape up the back of the head and
  through the hair. It is only worn head down, so the blood runs toward the head; the check keeps the back below the collar clean.

## Tests

`TheListTests.theBoards` (new, batch `the_list_boards`): Lesley taken from behind by a slasher on the top row, with a second reader under
the stands. A plank out of her reach, or a plank under the other reader's hand, does nothing. A hand on her plank: over the row, her
words, the slasher's cue, still held with no retreat, the head under the next plank, down on her knees on the plank, and the other
reader hears the scream. A hand on the plank over her neck: the first stamp, drops on the dirt under the head, the row rings; a second
straight after counts for nothing. A blow on it: the second, the neck's mark, blood on the slasher, a spray on the dirt, the crack
heard. Looked away from for seventy ticks she still cannot tear loose. A hand on her: the third, dead and hung where she knelt, a pool
under the head, the other reader told a scream stopped, the kill kept, the hold let go; a hand after, she does not move. The other
reader, under the stands and looking up at her head, finds her. Ended: the stains taken up, Lesley back and unmarked.

## Seen in the frames

Eye-level frames of Lesley over the top row, held, after two stamps and dead: from where the slasher stands on the row above her,
from the row below behind her, from the side along the row, from the dirt under the stands, straight up from under her head, and
from the field.

- **Over the row** reads from every side: down on her knees on the plank, the back bent past level over its far edge, the head gone
  through the gap under the next row and hanging in the dark under the stands, the hands flat out on the plank. From under the
  stands she is a head and two arms hanging through a gap in the planks; from the field she is hidden by the rows in front.
- **Hung**, the hands are gone from the plank and the arms hang down through the gap either side of the head. From the slasher's
  place above her that is the whole difference, and it is enough: the hands that were flat on the plank are not there.
- **The stains**: the drops and then the spray lie on the dirt under her head, and the pool spreads there; seen from the field, a
  dark patch on the caliche between the posts.
- **Found and fixed**: the review tool that draws these frames drew the hook's wound on the five but not the neck's mark, so the
  first frames showed no mark at all; it draws both now, as the game does.
- **Found and fixed**: the first neck mark ran its blood from the collar down the back toward the hips, which for one bent head down
  over a row is uphill. It now runs the other way: over the collar's lip and from the nape up through the hair to the crown, and
  the back below the collar is clean.
- From the slasher's place on the row above, the nape and the collar are under the plank they stamp on, as they would be; the mark
  is seen from behind her, from the row below, and from under the stands.

## Every new line

Speech, over the speaker's head as their name, forced down over the row:

- Deek: "Wait, wait. I'll do whatever you want."
- Wes: "I can't see anything!"
- Mel: "The camera's still running."
- Lesley: "I can see the ground. Let me up."
- Shannon: "Jenna, I'm under the bleachers!"

Shannon, coming across one of the others hung there (the others keep their lines from 4a-3): "That's %s. That's %s up on the stands."

Grey cues on the action bar, to the slasher:

- "You force %s down over the row, head through the gap under the next one."
- "You stamp on the plank across the back of the neck. The row rings."
- "Again. Something gives under your heel."
- "Again. %s goes still."

To a reader who is not the slasher, coming across them: "%s hangs over a row of the stands, not moving."
