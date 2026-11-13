# Four cameras to a set: the Ghosts Set by copy (0.4.553)

The owner, 13 November: "All multiplayer vignettes should be hooked through that system. If full".

Since 0.4.404 the Barretts' set has shot one production at a time: the first reader filming leads, and every other reader there is a
second camera on the lead's take (`docs/GHOSTS_CREW_0_4_404.md`). Any number of readers could crowd into the one house. This release
gives the set copies and a number of places.

## Full

| Story | Places | Why |
|---|---|---|
| The Ghosts Set | 4 | the lead's camera and three second cameras: the house is small, and Barry asks everyone who comes in to stay out of the lead's shot |

A set's places are held by the readers on it now. A camera who walks out keeps no place: the take goes on without them, and when they
come back they start from their first unfilmed day, as they have since 0.4.404.

- A fifth reader through the door goes into the next set, built whole a million blocks west, and leads a production of their own there,
  with a family of their own. Their door waits while the set is made ready; it is made ready ahead when a set is down to its last place.
- Each set has its own lead and its own second cameras (`GhostsCrew`; the lead is kept by copy, `CrewLead0404` for the original and
  `CrewLead0404@k` for copy k). A lead who walks out hands the bodies to the longest-present camera on the same set, as before.
- A reader's family on one set is not their family on another. A party story's private cast is kept under its set's ids
  (`LiteraryVignettes.actorId`), so a reader who films on the second set one visit and on the first the next has a family on each, and
  none is ever called across from the other.
- The call sheet on a copy's lectern reads as the original's does (`SceneInstances.originAt`).

## The case

`GhostsSetTests.aFifthCameraIsGivenASetOfTheirOwn` (ghosts): four readers on the set, one leading and three second cameras. The set is
full to a fifth and never to one of the four. The fifth's door waits while a second set is made ready, and one of the four goes back into
theirs. The second set stands a million blocks west; the fifth goes in by its own door and arrives on it. There the fifth leads (the
second set's own stored lead, not a second camera), and the first set's lead and cameras are as they were. The fifth's own family stands
on the second set, kept under its ids, and none under the first's.

## A case that left its set standing

Running the Ghosts cases with the new one in place moved the batches' order, and `GhostsLoreTests.eachPileOfBooksIsItsOwnAndMarjoriesDeskStandsAgainstHerWall`
found two drawers at Marjorie's desk. The second was the previous batch's reader's own: `ghosts_bed_walk` never closed its fixture, so its set and its
reader's family stood on for the next batch, at the same place. It now closes after its batch, as every other set batch does, and the drawer case counts
the drawer of its own reader, which is what it says it checks (each reader has a drawer of their own there by design).

## Decided here (any of it can move)

- Four cameras to a set.
- A camera who walks out keeps no place, since the take goes on without them.
- A reader's family is kept per set.

## Not yet

- **The Shallows**: its number of places, and its recollections by copy.
- **The shared scenes with no number yet** (Camp Blood, the lake nights, the hotel, the séance, Last orders, Polybius, the Sphinx, the
  yard's stories): each needs a number of places decided, or is left shared on purpose.

## Numbers

No layout change. No Witness change. New builds only.
