# A Little Longer 0.4.627: the return indoors

The seventh slice of the plan (docs/A_LITTLE_LONGER_PLAN.md): "The return indoors. 'I just don't want you to go yet.'" Built from the
proposal's "Let them stop hiding behind the activity" (docs/A_LITTLE_LONGER_PROPOSAL.md). Code: `labyrinth/LongerReturn.java`, which takes
Rowan over once they are back at the worktop after the roof (`LongerRoof`, stage DONE).

## What happens

- **The excuse dropped.** When the reader comes within four blocks of Rowan in the flat (or puts a hand on them), Rowan starts on another
  job and abandons it, word for word: "I was going to say there's something wrong with the radio again." / "Is there?" / "No. I just don't
  want you to go yet."
- **Answered in kind.** A reader who has already asked to stay on the roof (glad they stopped, asked to sit closer, or asked for the kiss)
  is not asked as if it were new: "I was going to make something up about the radio. I don't need to, do I." / "No." / "Good. I don't want
  you to go yet."
- **Four answers.** "I don't want to go either." (Rowan looks at them and does not look away); "You could have just said that." ("I'm
  trying."); "I was hoping for another excuse too." ("Then we were both hoping."); or let it stand. The admission stands: no joke follows
  it.
- **Ordinary time.** Then one ordinary thing: wash the cups (Rowan walks to the sink and washes while the reader dries, then rests there);
  "The toast wasn't that bad." if a slice burnt in the kettle chapter ("It was charcoal. We both know it was charcoal."); or sit down (Rowan
  walks to their own chair and sits with the cup). The companionship route reads the same; nothing here is romantic unless the reader made
  it so.
- **Kept**: `Return0627` (stage), `ReturnHow0627` (DIRECT, TEASE, HOPED, QUIET) and `Ordinary0627` (CUPS, TOAST, SAT). Leaving before
  answering puts the return back to its start; leaving while washing or walking finishes it. Chapter 5 (tomorrow) is the next slice.

## New lines, flagged for the owner

From the proposal, word for word: "I was going to say there's something wrong with the radio again.", "Is there?" (the reader's), "No. I
just don't want you to go yet.", "You could have just said that." (the reader's), "I'm trying."

New (Rowan):
- "I was going to make something up about the radio. I don't need to, do I." / "Good. I don't want you to go yet." (the reciprocal form)
- "Then we were both hoping."
- "It was charcoal. We both know it was charcoal."
- "Stay as long as you like." (a hand on Rowan once it is done)

New (the reader's): "No.", "I don't want to go either.", "I was hoping for another excuse too.", "Let it stand", "Wash the cups", "The toast
wasn't that bad.", "Sit down".

New cues: "Rowan looks at you, and doesn't look away.", "Neither of you fills the quiet.", "Rowan washes. You dry. The radio plays.",
"Rowan sits. Nobody says the next thing for a while."

## Art

No new poses: Rowan uses the sink pose (gesture 12) and the seated cup pose (gesture 11), both rendered and reviewed in earlier slices.

## Tests

`LongerTests.backIndoorsRowanDropsTheExcuseAndTheCupsAreWashed` (the plain form, teased, no joke after, Rowan walks to the sink and
washes) and `LongerTests.aReaderWhoAskedToStayIsAnsweredInKindAndRowanSits` (the reciprocal form, let it stand, Rowan walks to their chair
and sits). The longer namespace passes 15 of 15.
