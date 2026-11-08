# Trials in the way (0.4.392)

R2 of [HALLS_TRIALS_AND_TIME_DESIGN.md](HALLS_TRIALS_AND_TIME_DESIGN.md), as the owner set it on November 8.

- The first proposal was too frequent: "I don't know that I would increase the frequency quite as much as R2 suggests."
- The owner's setting: "R2, try from depth 20 - one in four should be good."
- The owner asked for it after the four halls: "go ahead and implement the four rooms please, then the rules."

## The rule

- **Before depth 20, nothing changes.** Every hall is dealt as it was.
- **From depth 20, about one ordinary hall in four has no ordinary way on.**
  - Every door of that hall leads to a trial, except its story door if one is dealt there.
  - Each door gets a different trial while there are trials enough.
  - The other three halls in four are dealt as they were.
- **The trials' own spacing still holds.** If a reader has just been through a trial and none is due yet, the hall is dealt as usual.
- **These come first and are never replaced:** a scent to follow, a lost pet's rescue, and a quiet room owed.
- **Going back still costs depth.** The way back is always open, as before.
- **Only an ordinary hall is ever dealt this way.** That includes the dark halls, which are the only ordinary halls from depth 21. Stories, trials and rooms are dealt as they were.
- **The staircase.** It can still be dealt behind one of the hall's doors once the reader is sealed, as before.

### Together

A fresh hall's doors are all dealt together when a reader arrives, from a seed taken from the route, as before. R2 is decided in that same deal. Friends who come the same way are dealt by the same rules from the same seed, so they usually meet the same trials together. Each reader's own state can still part them, as it always could: a trial done more recently, a story owed, a scent being followed.

## Why one in four

The staircase is at depth 24 and wants three trial marks (R1, the seals). From depth 20, a reader who has avoided trials until then meets a hall with nothing but trials every four halls or so. Most halls still leave an ordinary way on, so the House stays one that can be walked and mapped.

## Code

- `LabyrinthPacing.TRIAL_ONLY_DEPTH` (20) and `TRIAL_ONLY_IN` (4).
- `LabyrinthDealer.deal` decides once per fresh hall, when `arriveAt` deals its doors together: an ordinary hall, at depth 20 or more, nothing owed, scented or rescued, a trial due (`trialChance` above 0) with weight at this depth, then one draw in four.
  - The draw is made only that deep, so shallower deals use the same random numbers as before.
  - When it falls, the odd, trial and rest doors are not dealt. Every door but the story door gets a trial, picked by weight from those not yet used in this hall, in its route variant.

## Tests

New case: `ExplorationPassTests.fromDepthTwentyAboutOneHallInFourLeadsOnOnlyByTrials`.

- Four hundred fresh readers at depth 19 and four hundred at depth 20 are each dealt a three-door stone gallery.
- At depth 19, none is left without an ordinary way on.
- At depth 20, between 15 and 35 in a hundred are, their doors leading only to trials or a story.
- Most such halls put a different trial behind each door.

## What stays

- **Witness:** 81 / 61 / two kinds / three endings.
- **Protocol:** 163.
- **Layout:** 130. No new slot.
