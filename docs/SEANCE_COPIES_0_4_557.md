# Four to a house: the séance by copy (0.4.557)

The owner, 13 November: "All multiplayer vignettes should be hooked through that system. If full".

The séance has been one house for everyone since 0.4.34: one family at one table and one sitting for the whole world, kept in the
scene's own record (`classics_0434`). The first reader to drive the family out, or to sit through to the medium's words, ended the
sitting for everyone, and every reader after that came into the aftermath. Any number of readers could stand in the drawing room at once.
This release gives the séance copies and a number of places.

## Full

| Story | Places | Why |
|---|---|---|
| The séance | 4 | an unseen guest for each of the family at the table |

A house's places are held by the readers in it now. The séance keeps no roll, so a reader who walks out keeps no place.

- The fifth reader through the door goes into the next house, built whole a million blocks west by the séance's own builder, with a
  family of its own at its table. Their door waits while it is made ready; it is made ready ahead when a house is down to its last place.
- **Each house has its own sitting** (`ClassicsVignettes.world`): the original's in the scene's record as it always was, copy k's in
  `classics_0434@k`. It holds the family, whether the sitting has ended and how, and the family's flight. The family moves once a tick in
  each house.
- Disturbances, touches and the medium's words end the sitting of the house the reader is in, and frighten only that house's family. The
  first family stays at their table while the second runs for their back room.
- A reader's own part is theirs wherever they sit, as before: their disturbances, what they have read, how long they have watched.
- A reader who did not end a sitting can still take the aftermath for their account, as before, but only when the sitting has ended in the
  house they are in. Before there was only one sitting to have ended.
- The door between the rooms carries voices, or nothing, from the house it opens from (`ClassicsVignettes.familyGone(server, data, base)`).
- The accounts already count company within one copy (0.4.547), so a reader's line says who was in their own drawing room.

## The case

`ClassicsTests.aFifthReaderIsGivenASeanceHouseOfTheirOwn`: four readers at the séance are full to a fifth and never to one of the four.
The fifth's door waits while a second house is made ready, a million blocks west, and they go in by its own door. Its family is its own,
not the first's. The fifth's three disturbances end the second house's sitting and nobody else's: the first family sits on at their table,
unfrightened, while the second gets up and leaves their drawing room. The door between the rooms carries nothing from the second house
and voices from the first.

## What testing found

The case's first run found the second house's cupboard, letters, sitting record and album missing, with air where each should
stand. `ClassicsRooms` set its fittings, candles, shutters, lanterns and nursery panels with the level's own `setBlock`, which goes
straight to the world. A copy is built from a recorded plan (`BuildBlocks`) that is laid down afterwards in slices, and the plan's own
carve of the room laid air over everything that had been set straight to the world. The original house is built directly, so it never
showed there. `ClassicsRooms` now sets all of them through `BuildBlocks.set`, which records them into the plan while a copy is being
built and sets them at once otherwise.

## Decided here (any of it can move)

- Four places: an unseen guest for each of the family at the table.
- A reader who walks out keeps no place; their own disturbances and readings go with them into whichever house they come back to.
- A later reader's aftermath is the house they are in: its sitting must have ended there.

## Not yet

- **Camp Blood, the lake nights (Movie Night, Costume Night, Winter Lake) and the hotel**: each keeps its story in one record for the
  whole world, so a copy cannot have a night of its own until that record is kept by copy.
- **The Sphinx**: her halls are the House's own, and stay shared on purpose.

## Numbers

No layout change. No Witness change. New builds only.
