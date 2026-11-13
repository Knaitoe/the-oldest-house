# A trailer for the seventeenth: the Goatman's evenings by copy (0.4.552)

The owner, 13 November: "All multiplayer vignettes should be hooked through that system. If full".

0.4.551 gave the stories written for a party copies of their own (`docs/PARTY_COPIES_0_4_551.md`) and left the Goatman's trailer
for later, because its evening was one run for the whole world: a second trailer could have stood, but not with a night of its own.
This release keeps the evening by copy and gives the trailer its copies.

## An evening for each trailer

- The original trailer's evening is where it always was (the `goatman` record's run). A copy's is kept under its own record
  (`goatman@1`, `goatman@2`, ...), as Harrigan's room and the clapping game keep theirs. Every run carries the copy it belongs to
  (`Copy0552`), so whatever saves a run saves it in its own trailer.
- Each trailer that stands has its night on its own clock: the walk up the trail, the cousins, the supper, the knocking, the dawn.
  An empty trailer's clock waits, as the original's always has.
- Everything a child does goes to the trailer they are in, or the one they keep when they have stepped out: the door, the generator,
  the griddle and the plates, the buns, the beds, talking to the cousins, the walk out in the morning, and the operator's `status` and
  `plate` commands.
- What is a reader's own stays theirs wherever they sat their evening: being followed home, the night that ended at the door, the
  personal resolution.
- The accounts already count company within one copy (0.4.547), so a child's account has in it only the children of their own trailer.
  A reader in a copy is never told that someone reached the end before them: that is the original's.

## Full

| Story | Places | Why |
|---|---|---|
| The Goatman's trailer | 16 | the number of children one evening has always let in (`GoatmanVignette.MAX_PLAYERS`); the beds and plates are laid for them |

A trailer's places are held by the children in it and by the children of its evening while anyone is still there, though some have
stepped out to the House (`GoatmanVignette.places`, through `SceneInstances.held`). A trailer whose sixteen places are held is full.

- The seventeenth through the door goes into the next trailer, built whole a million blocks west as the first is, and begins an evening of
  their own there. Their door waits while it is made ready; it is made ready ahead when a trailer is down to its last place.
- One of the sixteen who steps out and comes back finds their place still theirs.
- The trailer's own rule for latecomers stands. A child who comes to a trailer with a place left after supper has started, or after dark,
  is told so and goes on elsewhere (0.4.405, 0.4.418). Only a full trailer sends anybody to another.
- A friend behind the first one through is not told it is too late when the trailer is full: the door takes them to another trailer, as
  it takes anyone (`GroupCrossings.late`).
- An empty trailer holds nobody's place. Its next evening begins afresh, as it always has.

## The case

`GoatmanTests.aSeventeenthChildIsGivenATrailerOfTheirOwn`: one child in the trailer and fifteen more on its evening are sixteen places;
the trailer is full to a seventeenth and never to one of the sixteen, and its evening will not take a seventeenth. A friend behind the first
one through is led through, not told it is too late. The seventeenth's door waits while a second trailer is made ready; one of the sixteen
goes back into theirs. The second trailer stands a million blocks west with what the first has (its door, the fire, the stove, the bathroom
window, the cooler); the seventeenth goes in by its own door and begins an evening of their own there, kept by the second trailer, with only
them in it and cousins of its own. The first trailer's evening is untouched.

## Decided here (any of it can move)

- Sixteen places: the trailer's own limit on an evening.
- A latecomer after supper or after dark is turned away by the trailer's rule, not sent to another trailer to sit an evening alone.
- An empty trailer keeps nobody's place.

## Not yet

- **The Ghosts Set** crew and **the Shallows**: their party sizes, and whether their state is per copy.
- **The shared scenes with no number yet** (Camp Blood, the lake nights, the hotel, the séance, Last orders, Polybius, the Sphinx, the
  yard's stories): each needs a number of places decided, or is left shared on purpose.

## Numbers

No layout change. No Witness change. New builds only.
