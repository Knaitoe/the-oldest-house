# A Little Longer 0.4.629: chapter 6, the cut

The ninth slice of the plan (docs/A_LITTLE_LONGER_PLAN.md): "The seam: architecture first, then the wall, the counter and Rowan
receding; one human reaction; the radio stopping mid-phrase. Works from every position; no rescue branch." Built from the proposal's
chapter 6 (docs/A_LITTLE_LONGER_PROPOSAL.md). Code: `labyrinth/LongerCut.java`, which takes over fifteen seconds after the present choice
(`LongerTomorrow`, DONE).

## What happens

- **One last ordinary thing.** Rowan goes to the toaster at the end of the worktop to pull its plug before they leave. Nothing is
  asked of the reader.
- **The radio stops in the middle of the phrase.** The tune is cut, not faded: the radio's stage drops to off and the playing sound is
  stopped for everyone in earshot, wherever it was.
- **A wrong edge.** One second later the wall beside the toaster has a seam in it: a narrow strip of the new `longer_seam` material
  ("Wrong Edge"), concentric dark rings that read as depth going back further than a wall is thick. At first it is only architecture.
- **"Wait."** One second after that Rowan notices it, turns to the reader and reaches for them (the reach pose, facing the room), and
  says the one word. That is the only human reaction; there is no farewell and no speech.
- **The room recedes.** A passage opens behind the seam (seam walls either side, a light at its far end), and every 0.7 seconds the
  worktop's end, the toaster and Rowan move one block further into it, still reaching back, the light narrowing. The cell they leave
  becomes an invisible stop at the mouth, so nobody can follow them in; behind them the passage is air until the last steps, when its top
  row turns to seam. The light goes out.
- **The space is gone.** After seven steps the passage closes. The kitchen is left changed: the facade is filled, a shallow alcove with
  bare brick at the back stands where the wall was, the worktop is a block shorter and the toaster is gone, the kettle and the cups are where they were, the coat hook is empty, the
  radio is silent. Rowan is not in the room, and does not come back on later visits.
- **No rescue.** It runs the same wherever the reader stands (beside Rowan, at the door, halfway down the stair, outside): there is no
  timing, gaze, item or choice that changes it, and nothing is labelled as saving Rowan.
  A reader who leaves part way through comes back to the room afterward, never to the seam half done.
- **Kept**: `Cut0629` (NONE, TOASTER, CUTTING, DONE). Rowan's cast phase is -1 from then on. The room afterward (chapter 7) is the next
  slice.

## New lines, flagged for the owner

From the proposal, word for word: "Wait." (Rowan; the proposal allows the reader's name, a call, or "Wait.").

New cues (grey italic, in the action bar):
- "Rowan goes to pull the toaster's plug out before you go."
- "The radio stops in the middle of the phrase."
- "By the toaster the wall has an edge it did not have, and it goes back further than any wall is thick."
- "Where the wall was there is a shallow space with bare brick at the back of it."

New block name: "Wrong Edge" (`longer_seam`).

## Art

- `longer_seam`: one 16x16 material drawn by `tools/draw_longer_art.py`, concentric rings darkening toward the centre with faint corner
  diagonals, so a strip of it reads as a hole in perspective rather than a surface. Blockstate, model and language entry added.
- Eye-level review frames of the kitchen were rendered and looked at in the middle of the cut (Rowan reaching from inside the narrowing
  passage, the seam either side, the light at the end) and after it (the alcove, the shortened worktop, the empty hook).

## Tests

`LongerTests.theCutFromBesideRowanAndTheRoomAfter` (the radio stopped, the seam at the edge cell, "Wait." and the reach, Rowan, the
worktop and the toaster moved into the passage, the mouth closed to the reader, the room afterward with Rowan gone, the kettle and cups kept, the hook empty, and Rowan not made again on arriving)
and `LongerTests.theCutFromTheDoorAndLeavingPartWay` (the same cut seen from the door; a reader who leaves mid-cut returns to the room
afterward). The longer namespace passes 19 of 19.

The door-bleed fix that went out with 0.4.625–0.4.628 (run 1666) gave A Little Longer its own door sound, a radio turning between
stations; that is the sound of the room before the cut.
