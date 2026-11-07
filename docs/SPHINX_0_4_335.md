# The Sphinx: the stair above you (0.4.335)

The Sphinx's second crossing, from the owner's draft "The Sphinx" (4 November), "Middle stone halls · The stair above you":

> *"Which stair takes you down to the landing above us?"*

`LabyrinthPlace.SPHINX_STAIR`, slot 202, gray, layout 103. It is a rare detour (weight 2) in the stone halls, from the deep depth until
the dark halls. Its floor stands 14 blocks into its slot to leave room for the shaft. The way onward is always open along a corridor
past the gallery, so nobody has to answer her.

## The place

- **The approach.** A flagstone strip behind an iron rail. A grille in its west wall asks her question again for anyone who wants to hear it. The approach rail is her stop line.
- **The gallery.** It stands at the head of a narrow stone shaft whose walls go dark above head height and below the flights. Three flights go down from it, left, middle and right, each three wide, ten treads into the dark and on round a turn nobody can see. Between the flights are long open gaps down into the shaft.
- **The landing.** It hangs over the heads of the flights: an iron grating with a stone lip along its gallery edge and a rail along the drop behind it. Its whole floor can be seen from the gallery through the grating, and from the landing the gallery can be seen back.
- **The mouths.** Each mouth has an iron gate, as tall as a fence to a body. On its posts are:
  - a release catch;
  - a stop pin;
  - a survey socket (docs/SPHINX_0_4_334.md).
  Rails close the gaps between the posts.
- **The tray.** A tray of loose stone chips stands at the rail.

## The proof

Take a chip from the tray, then right-click a mouth's gate with it in hand.

- **A false flight.** The chip rattles down it tread by tread, a little dust at each, and settles at the foot, far below.
- **The live flight.** The chip goes down five treads and round the turn out of sight. A moment later it comes back out onto the landing overhead, rolling across the grating, and stops against the lip, where everyone at the rail can see it.

Every sound has a visible counterpart: the chip itself, its dust, and a puff where it comes to rest. The last chip from each stair stays
where it came to rest until another is let go there. The test can be repeated from the rail as often as anyone likes.

Chips are plain flint by their look. They are kept only in the room: anyone who leaves finds them gone from their pockets.

## Answering

- **Releasing a catch** is the answer. Survey pins are locked exactly as in the vestibule, and one catch moving at a time refuses another.
- **The live gate** swings back against its post. Walking down that flight and round the turn brings the reader up onto the landing they were watching, with the gallery through the grating below them. The first reader up opens the short stair from the landing down to the way onward, and it stays open for everyone, rebuilds included.
- **A refused gate** catches at its stop:
  - her voice says, from under the reader's feet, "Not that stair. Leave that one shut.";
  - that gate's stop pin shows;
  - her eye comes up in the gap below the flights, face up, following whoever is nearest, while dust runs down out of the dark at that mouth;
  - after seven seconds the eye sinks again.

## Forcing

Pulling an exposed pin is the only thing that brings her. It happens in this order:

1. Dust comes down out of the dark overhead.
2. An eye passes up through the long gap between the flights (the west gap for the west and middle flights, the east gap for the east).
3. Her foreleg comes up the refused stair, tread by tread.
4. Her paw hooks over the top tread, and the gate bows out toward the gallery.

Then:

- **Anyone in her reach.** Anyone on the gallery within six blocks of that mouth is warned (her breath, grit on them), then her paw sweeps them back over the approach rail. Nobody is hurt. Each sweep is recorded.
- **Withdrawal.** Once the gallery has been empty for five seconds she goes back down: paw, foreleg and eye go, the gate returns to its stop and the pin goes back in.
- **Going down under her.** A reader who goes down the stair under her is turned out into the onward corridor, and that mouth closes behind them as cracked masonry, seen from the gallery too. Both sides keep their own way on: the corridor, and the gallery's opening into it.

## Safety

A fall into the shaft, from the gallery or off the landing, is caught and set back at the approach rail. Testing is safe.

## Multiplayer

There is one room for the party:

- the chips, the released gate, the open way and a sealed mouth are shared;
- each reader's answer (by pin or catch), pulled pin, sweep and walkaway is their own, under `sphinx_0306` → reader → `stair`;
- her question is asked privately to each reader who comes onto the gallery;
- her warnings reach everyone in the room;
- her eye follows whoever is nearest.

## Assets

- `tools/draw_sphinx_fittings.py`:
  - three materials: dressed stone, worn flagstones and cracked masonry;
  - the stair gate (shut, released, bowed), the chip tray, a chip, the landing grating with and without its lip, and her foreleg;
  - her eye face up, with a new `face` state (down, up, sideways);
  - the next crossing's fittings and rock, drawn ahead and used from 0.4.336.
- `tools/generate_sphinx_audio.py`: seven more original cues, made now for this and the later crossings (rattle, grind, rustle, wing, sand, crack, settle). The stair uses the rattle.

## Tests

`SphinxStairTests#theStairIsTestedWithChipsAnsweredClimbedAndForced` (exploration), with two readers, checks:

- the gates and landing as built, and the lip in sight from the gallery;
- the question, heard only on the gallery;
- a chip at a false foot and a chip on the landing;
- survey pins locked by a refused gate, each pin its reader's own answer;
- the warning, the pin, and the eye in the gap opening and closing;
- the live gate released and the flight down arriving on the landing, with the way onward opening;
- the pin owned by its puller, the eye and foreleg coming up, the paw and the bowed gate;
- a sweep over the rail, with nobody hurt;
- withdrawal;
- a second provocation and going down under her, which seals the mouth;
- a fall given back at the rail;
- chips left behind on leaving;
- the released gate, open way and seal all kept through a rebuild;
- no Witness credit.
