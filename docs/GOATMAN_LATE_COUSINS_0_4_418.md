# Goatman: friends who come in late take the cousins' places (0.4.418)

The owner, after the November 9 night playtest: "Did you adjust the Goatman vignette for multiplayer, too? The plans were for secondary
players to occupy the roles of the cousins, but last nights Playtest did not preform like that."

## What went wrong

Since 0.4.150 a second reader takes a cousin's place, and that cousin is never seen. The rule only worked on the trail, though, before
anyone had reached the camp. In the playtest the friends came through one after another. By the time the second reader was in, the
first had already reached the trailer. So the second reader was let in as one more child, every cousin was still there, and the night
played as if there were nine of them.

## What changes

- **Before supper, anyone who comes in takes a cousin's place.** That holds on the trail and at the camp until supper starts (or the
  meal is frozen for the test command). The cousin they replace stays unseen for the night. Once supper has started, the held door
  still turns a newcomer away, as in 0.4.405.
- **The gas job only goes to someone while the cousin who would go hasn't gone yet.** If the runner has already left for the gas, a
  late friend takes another cousin's place: someone else from the porch, never the wrong cousin or a carrier.
- **A late friend is told where their place is.** The line names the chair at the table and their own bunk, worked out from the real
  bunk layout. For example: "the second bed on the right as you come in". A reader who takes the gas job is told the generator is theirs.

## Tests

The existing case `GoatmanTests.readersAfterTheFirstTakeCousinsPlacesAndTheGasJobFallsToOne` now also checks a reader who comes in at
the camp, after the first reader got there and before supper:

- the place is still open;
- the reader takes a cousin's place, not an extra one, and the expected headcount stays at eight;
- it isn't the gas job, the wrong cousin, a carrier or the runner;
- the line names their bed;
- once supper has started the place is closed.

All 32 cases in the Goatman and meal namespaces pass.

## What stays

- **Witness:** 86 / 65 / 63.
- **Protocol:** 170.
- **Layout:** 135.
- **New builds only.**
