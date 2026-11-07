# The Sphinx: what holds still (0.4.338)

The Sphinx's third crossing, from the owner's draft "The Sphinx" (4 November), "Deep dark halls · What holds still":

> *"Which ledge can hold still when I breathe?"*

`LabyrinthPlace.SPHINX_SHELF`, slot 203, gray, layout 104. It is a rare detour (weight 2) from the dark halls on, the Great Staircase approach included. Its floor stands 14 blocks into its slot, for the cavern. The way onward is always open along a corridor off the east end of the ledge, so nobody has to answer her.

## The place

- **The passage.** A low passage, with a grille in its wall that asks her question again, and a sill. The sill is the line she stops at.
- **The ledge.** A near ledge of rock under a low overhang, at the edge of a cavern, lit by one soul lantern.
- **The shelves.** Across the gap, three stone shelves stand out from the far wall. Beside each one an iron survey pin is driven into the rock, its head level with a settled shelf. The cavern falls away below the gap and rises into the dark above it.
- **The ladders.** Each shelf has a short ladder standing at the near edge. On the posts beside it are a stop pin and a survey socket (docs/SPHINX_0_4_334.md).
- **Her voice.** Her question comes back first from below and then, a moment later, from high above.

## The proof

While anyone is on the ledge, she breathes about every nine seconds. Two of the shelves lift a little past their pins, grit running off their edges, and settle again two seconds later. The third never moves, so the clue reads even in low light or with the sound off.

Asking her question again at the grille brings the next breath sooner. The test can be repeated as often as anyone likes.

## Answering

- **Lowering a ladder** is the answer. Survey pins are locked exactly as in the vestibule, and one ladder moving at a time refuses another.
- **The fixed shelf.** Its ladder comes down across the gap and lies still. Crossing it, a passage behind that shelf leads round to the way onward and opens it for everyone, rebuilds included.
- **A moving shelf.** Its ladder tips forward and catches on its rope, and then:
  - she says "Not that one. That ledge does not hold still.";
  - that ladder's stop pin shows;
  - her eye opens upright in the gap before that shelf, her face turned on its side, following whoever is nearest;
  - after seven seconds it goes again.

## Forcing

Pulling an exposed pin is the only thing that brings her. It happens in this order:

1. The ladder drops across the gap onto that moving shelf.
2. The shelf slides sideways off what it was resting on, which was her: under it, fur rises and falls.
3. Her paw settles against the far wall in the gap beside it, her foreleg going up the rock into the dark.
4. Her eye lowers out of the dark until it is level with the ledge, her face on its side to see in under the overhang.
5. She says "Put the ladder back."

Then:

- **Anyone in her reach.** Anyone on the ledge within six blocks of that shelf is warned (her breath, grit on them), then her paw reaches in under the overhang and sweeps them back over the sill. Nobody is hurt. Each sweep is recorded.
- **Putting the ladder back.** She withdraws: the shelf slides home, the paw lets go of the wall, her eye rises away, and the pin goes back in. The record keeps `PutBack`.
- **Withdrawal.** Everyone stepping back over the sill also lets her withdraw, after five seconds with the ledge empty.
- **Going out along the dropped ladder.** Her hand comes down over the crossing and sets the reader down past her, in the onward corridor. The ladder is gone under it, and closed rock stands where it was, seen from the ledge too.

## Safety

A fall into the cavern is caught and given back in the passage. Testing is safe.

## Multiplayer

There is one room for the party:

- the shelves and her breath, the lowered ladder, the open way and closed rock are shared;
- each reader's answer (by pin or ladder), pulled pin, sweep, ladder put back and walkaway is their own, under `sphinx_0306` → reader → `shelf`;
- her question is asked privately to each reader who comes onto the ledge;
- her warnings reach everyone in the room;
- her eye follows whoever is nearest.

## Assets

Nothing new is drawn for this crossing. Its fittings, its rock and her sideways eye were all drawn ahead in 0.4.335:

- the shelf (settled and lifted);
- the survey pin in the wall;
- the ladder (standing, lowered, dropped) and its span across the gap;
- dark rock and closed rock;
- her eye with its new face state.

The grind cue is one of the seven made then.

## Tests

`SphinxShelfTests#theShelvesAreWatchedAnsweredCrossedAndForced` (exploration), with two readers, checks:

- the shelves, pins, ladders, overhang and cavern as built;
- the question, heard only on the ledge;
- her breath lifting the two moving shelves and not the fixed one, and settling them again;
- the grille's question bringing the next breath sooner;
- survey pins locked by a refused ladder, each pin its reader's own answer;
- the warning, the pin, and her eye in the gap opening and going;
- the live ladder lowered, walkable, and the passage behind the fixed shelf opening the way onward;
- the pin owned by its puller, the ladder dropped, the shelf sliding off her, her paw, and her eye lowered to the ledge;
- a sweep over the sill, with nobody hurt;
- the ladder put back and her withdrawal;
- a second provocation and going out along the dropped ladder, which seals it;
- a fall given back in the passage;
- the lowered ladder, open way and seal all kept through a rebuild;
- no Witness credit.
