# A Little Longer 0.4.623: chapter 1, the workbench

The third slice of the plan (docs/A_LITTLE_LONGER_PLAN.md): "The panel held, the screw, the lamp; the penciled route; the one
remembered act." Built from the proposal's chapter 1 (docs/A_LITTLE_LONGER_PROPOSAL.md). Code: `labyrinth/LongerBench.java`, with
Rowan's tick and talk handing the chapter Rowan while it runs (`LongerRowan`).

## What happens

- **The ask.** The first hand on Rowan at the bench, or a reader near the chair for three seconds, opens the panel: "Could you hold that
  side a second?" The reader may hold it, watch, or say not now.
- **Held.** To hold the panel the reader stands within reach of the radio and looks at it for three seconds; Rowan's hands quicken while
  they do. A hold never made in thirty seconds lapses: Rowan does it one-handed.
- **Alone.** Watched, or not now: the last screw goes off the bench and rolls across the floor toward the counter, a real screw. The reader
  may pick it up and hand it back. Left on the floor fifteen seconds after it stops, Rowan gets out of the chair, walks to it, stoops for
  it and walks back. A screw kept in a pocket is replaced from the tray after forty-five seconds; the shop's screw never leaves the shop.
- **The lamp.** While Rowan works, a hand on the lamp turns it onto the radio (a new lamp position, lit).
- **The burst.** Done, Rowan says "There. Thank you." (or only "There." when the reader did nothing), switches the set on; it plays a
  moment and bursts (a new sound, sparks and smoke) and goes off for good. The panel opens on its own if the reader is in the shop near
  Rowan: "That sounded expensive." Rowan turns in the chair toward them.
- **The talk.** The four ways to answer, each once. After the first, the route on its paper in Rowan's hand, read in the panel. The
  stairs, word for word from the proposal; the crossed-out turn; "Leave it there" puts the paper away and the tool down. Rowan rests in
  the chair after, and no longer hums. The second chapter (the kettle) is the next slice.
- **The remembered act** (`Act0623`): the first of these actually done, only that, never assumed: waited at the back door before coming
  in (five seconds in the corridor by the door, the shop not yet entered), held the panel, returned the screw, moved the lamp. Nothing if
  none. Rowan will recall it on the roof (slice 6).
- **Leaving part way.** A reader who leaves before the burst finds the work as it was before the ask when they come back; what they did
  is kept. The talk after the burst waits for them.

## New lines, flagged for the owner

From the proposal, word for word: "Could you hold that side a second?", "There. Thank you.", "That sounded expensive.", "I said I knew
where the stairs were.", "You drew them in three places." (the reader's), "And one of those was right."

New (Rowan):
- "Fine. I've done it one-handed before." (not now; or a hold never made)
- "The left side. Flat, like that." / "Just the left side. Flat."
- "There." (finished with nothing done by the reader)
- "No. No, no." (the screw rolls)
- "That's the one. I'd have been on my knees for an hour." (handed back)
- "Never mind. There's another in the tray." (kept)
- "Better. Leave it there." (the lamp)
- "Sorry. You've been standing there."
- "My shop. It was on Carver Street until about a week ago. Now it's on whatever this is."
- "It picks something up late at night and loses it again. I'd like to know what."
- "It's going exactly as well as it sounded."
- "Everybody is. I've tried most of the doors."
- "I've tried every door on that side. Here."
- "The one with the line through it. It went back to here."
- "I'm leaving it alone tonight. It can think about what it did." (after the chapter)

New (the reader's replies): "Hold the side of the radio", "Watch", "Not right now.", "Where is this?", "What's wrong with it?", "It's
going well, then.", "I'm only passing through.", "Are you sure about the stairs?", "Which turn didn't work?", "Leave it there".

New (cues and the paper): "You hold the back panel flat against the case.", "You turn the lamp onto the radio.", the paper: "In pencil:
back door. Corridor. Left at the second door, crossed out. Stairs? Stairs? Stairs."; the item "Small screw".

The old first bench line "Sorry. One minute. I nearly have it." is gone: the first hand at the bench is now the chapter's ask.

## Art and sound

- Rowan's four new gestures: hurrying (the screwdriver hand quick), stooping (built on vanilla's crouch, taken further, so the parts stay
  joined), holding out the route (paper in the hand), resting (hands in the lap). Seen in review frames from the side and front.
- The lamp turned onto the radio: `lamp_2`, the arm swung over and the shade tipped down at the set (tools/draw_longer_art.py).
- `longer.burst`, "A radio bursts and pops" (tools/generate_longer_audio.py): warm-up hum, broadband blast with a falling howl, a pop,
  crackle, the hum dying.

## Cases

Three new: the held panel through the burst and the whole talk; the screw rolled, the lamp turned (and kept as the first act), the screw
handed back and the shop's screw taken from a pocket outside; the screw fetched by Rowan on their own feet through nothing solid. The
first Rowan case now expects the ask at the bench.
