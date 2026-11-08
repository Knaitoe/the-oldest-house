# Last Weekend at Pinehook, slice 3 (0.4.366)

The third slice of the owner's cabin weekend (`docs/PINEHOOK_PROPOSAL.md`). Earlier slices: `docs/PINEHOOK_0_4_364.md` (arrival to the turn) and `docs/PINEHOOK_0_4_365.md` (the night's parallel scenes).

This slice adds the proposal's puzzles with consequences:

> *"Power and light. The circuit can illuminate the dock or the cabin's rear approach fully, but a damaged junction cannot initially sustain both. Repairing it takes exposure and time. The group can choose which person to protect while another works."*

> *"The costume timeline. Compare what Jude wore, where his props were placed, and the extra mask fastening. This exposes a second use of the costume. No arbitrary numeric code."*

> *"The boathouse door. An old brace and jammed bolt can keep Len safe or trap him with the attacker. The same mechanism has different moral uses. Another player can operate the release from the dock."*

> *"The safe. Len can open it, be persuaded to reveal the release, or be bypassed through a physical inspection puzzle. The evidence is not available only to one character class."*

> *"Emergency transport. Getting a vehicle or boat ready is one practical branch during the crisis."*

> *"All required tasks have fallback tools and a slower solo operation. A lost role or disconnected player cannot make them impossible."*

What each puzzle leads to belongs to the crisis, the outcome families and the accounts, which are the last slice's. The weekend is still not dealt, and it still adds no Witness source.

Layout 121 and protocol 160 add:

- seven fitting kinds: the junction box, the floodlight, the brace bar, the slip gate, the release cleat, the wall rack, the paddle;
- five carried things: the oars, the boat hook, the paddle, the safe's small key, Len's envelope;
- the floodlights, the junction, the boathouse's rack and slip gate, the brace by the door, the release rope's piling off the dock, and the paddle in the woodshed.

## Power and light

- **The lights.** There are three floodlights: one over the back step, one on a pole at the head of the beach, one on the T's east post (where the lantern was).
  - They are lit through the evening.
  - They go out with the breaker at dusk.
  - When the cabin's power comes back, they stay out.
- **The junction box** is on the cabin's east wall by the kitchen. In the night a reader can use it to:
  - **throw it to the dock**: the beach's and the T's floods come on, and the back step goes dark;
  - **throw it to the back of the cabin**: the back step's flood comes on, and the dock goes dark;
  - **work on the burnt wire**: stand at the box for three seconds per wire, three wires. Stepping away loses that wire's work. When the last wire goes back, every flood comes on.
- **Not before the night.** In the evening the box only hums.

The exposure is the point: standing out there in the dark while someone else waits.

## The costume timeline

A reader who holds the costume from the rowboat can lay it out on the kitchen table. If they know Jude knots his strap, or carry the strap from behind the shed, they see:

- Jude's mask went into the shed with a knot in its strap;
- the costume came out of the lake on a buckled strap cut to fit another head;
- so whoever wore it after Jude cut that strap before tonight.

Without that knowledge, the buckle only bothers them. Once laid out, it is another thing a reader can tell Casey.

## The boathouse door

- **The brace.** A length of timber leans by the boathouse door, with brackets either side for it.
  - Dropped across, nobody opens the door from either side; lifted, it leans back.
  - On the next knock, Len asks who did that. A reader can say it is to keep her out (Len lets it stay) or so he stays where he is (Len begs them to take it off).
- **The slip.** The boathouse has a slip gate in its lake wall. It opens only from outside, by a rope run to a cleat on a piling off the dock.
  - Casting the rope off opens the gate onto the lake, and Len shouts to shut it.
  - Made fast again, the gate shuts, unless someone is in the way.
- **In by the water.** With the slip open, a reader can swim in. Inside, Len can be asked:
  - to let them help (go back up and lock the door);
  - what's in the safe (nothing that's yours);
  - and, for a reader who heard him say "she", laid out the timeline, or turned Casey: *"Whatever's in that safe is why she's here. Open it and it's finished."* He says the key is under the bench, and that he isn't touching any of it.

The same brace and slip that keep Len in can let someone else in. Who uses them, and for what, is the crisis's.

## The safe

There are two ways in, and neither belongs to one part:

- **The key.** Once Len has said where it is, it is taped under the bench.
- **The rusted back.** The boards under the safe are always wet, and its back has rusted. Three goes with the boat hook from the wall rack tear the back away. Len shouts at the first.

Inside is Len's envelope:

- photographs of a car outside a petrol station at night, the number plate circled;
- a cutting about the robbery there two winters ago, the clerk hurt, nobody charged;
- a notebook of dates and sums, one a month for more than a year, with an N beside each.

Read, it is something a reader can tell Casey.

## The boat

Len's rowboat has no oars in it. They are on the boathouse rack. With the oars shipped it is ready. The fallback is the canoe paddle in the woodshed by the House's door: with it the boat is ready too, but slow.

## Things stay at the cabin

The oars and hook go back on the rack, the paddle back in the woodshed, the envelope back in the safe, and the key back under the bench.

## Multiplayer

- **Shared:** the junction and how far it is mended, the brace, the slip, the safe, the boat and the timeline.
- **Private:** what a reader says to Len about the brace, what he tells them inside, whether they read the envelope.
- **Two or more readers.** One stands at the junction while another watches the dark. One pulls the rope at the dock while another swims in.
- **One reader alone** can do every one of these, slower.

## The account (provisional)

The provisional account gains lines for:

- the envelope;
- the brace (and why);
- readying the boat;
- the timeline;
- the junction (thrown one way, or mended).

## Art

`tools/draw_pinehook_art.py` now draws 71 textures and 71 models and states: the junction box in its four positions, the floodlight, the timber brace leaning and across, the slip gate, the cleat with and without its rope, the rack in four states, the paddle, the empty safe, the rowboat with oars or the paddle, and the five new carried things.

## Tests

- **`theBoathouseAndTheBoat`** (new). It covers:
  - the brace across the door, and keeping her out;
  - the slip opened from the dock, and Len shouting;
  - in by the water; the hook and oars off the rack;
  - the safe's back, and three pries;
  - the envelope read; the oars in the boat;
  - the paddle from the woodshed;
  - the account.
- **`theNightTurnsCasey`** gains:
  - the costume lifted by one reader and laid out on the table by the other;
  - the slip opened from the dock;
  - in by the water, and Len's key;
  - the safe opened with it, and the envelope read.
- **`caseyBurnsHisPlanIfNobodyFindsIt`** gains:
  - the floods out with the breaker, and still out with the power back;
  - the junction thrown each way;
  - all three wires mended, and every flood lit.

## No Witness change

80 / 60 / two kinds / three endings.
