# Feet Up, finished: the chalk and the house later (0.4.383)

The owner: "go ahead and finish feet up". Feet Up shipped in 0.4.200 ([FEET_UP_0_4_200.md](FEET_UP_0_4_200.md)) without two things from the proposal ([FEET_UP_PROPOSAL.md](FEET_UP_PROPOSAL.md)): the chalk stub, and the second stage, the same house later with no floors. This release builds both. Where the proposal left a question open, the choice made is noted.

## The chalk

A glazed jar stands on the attic boards beside the chest the notebook lies on, full of chalk worn down to stubs.

- **Who gets one.** A reader who has reached the attic (who has finished the game, clean or not) takes one stub with a hand on the jar. One per reader, ever. The jar stays full for whoever comes up next.
- **What it is.** A stick of the House's own chalk, worn down (`FeetUp.STUB_WORN`) and marked as that reader's own, like every keepsake. It still makes the House's chalk marks, and like any chalk it wears out. Once it is used up, it is gone.
- **What it does.** Carried anywhere in the House, a fall of up to six blocks costs its keeper nothing (`Keepsakes.chalkFall`). It does nothing in the reader's own world.
  - Proposal question 5 asked whether six blocks is too useful on the great staircase. It is not: the staircase's falls are far longer than six blocks, and inside Feet Up's houses falls were already free.
  - The effect counts only a fall that would have hurt (more than three blocks) and that nothing else had already forgiven.
- **Its line**, under the stub's name: *Worn to a stub. Carried in the House, a short fall costs you nothing, as if the floor only ever counted.*
- **Afterwards.** Once the stub has kept a fall, the Witness account's Feet Up line gains an afterwards sentence, the way the Borrasca patch's does: *Afterwards, once, a short fall in the House cost me nothing. The chalk was in my pocket.*

## The house later

Proposal question 4 asked whether the second time through the same space should be a second visit or a deep sibling. It is a deep sibling.

- **The place.** `FEET_UP_LATER`, slot 222, a gray room. It is one of the puzzle rooms (`PuzzleRooms`, family `FEET`), paced the way they are.
- **Who gets it.** It is offered only from the strange depths, only once someone has reached the first house's attic, and only to a reader who has reached it themselves.
- **No story.** It is not a Witness source, has no account and resolves nothing. Witness stays 81 / 61 / two kinds / three endings.
- **Why not a second visit.** The first house is one shared house; rebuilding it for a reader who has finished would break the game for a friend still in it.

### What it is

The same house, built from the same plan (`FeetUpRooms.buildLater`), then taken apart.

- **No floors.** The walls run down five blocks into the dark (`feet_up_under`, the one new material). Every floor is gone:
  - the ground floor's boards and tiles;
  - the stair;
  - the cupboard under the landing;
  - the first floor's boards.
- **What is left to stand on.**
  - The loose boards the route and the long way cross. They still give way when stood on.
  - The doormat, inside the front door.
- **The furniture** hangs where it stood. The chimney breast goes down into the dark in brick.
- **Lamps and light.** The lamps are out and the fire is cold. A grey light comes in from nowhere in particular.
- **The parlour** stands open on an empty room, and nobody is behind its door.
- **The attic.** The chalk jar is empty.
- **One new piece.** A stool the house has left in the hall, a step in from the doormat. In the first house a reader walks over the hall floor to the big chair, and here there is none. The way up is otherwise exactly the first house's: `LATER_START` (doormat, stool, hall table, bench, big chair), then `ROUTE` or `LONG_WAY`.

### The rules there

The same controller runs both houses. `FeetUp` now keeps each house's moment-to-moment state apart (`FeetUp.House`), so a reader in one and a reader in the other never share boards, chairs or cage.

- **The game.** It is on from the first thing climbed, as before. The doormat is not a start.
- **The count.** A fall into the dark counts. The dark is deep, so it is the floor here. Nobody whispers the count: *One. Nobody says it but you.* A beat later the reader is back on their base. No fall into the dark hurts anyone.
- **Out.** Three touches and the reader lies in the dark for a moment while the hatch shuts somewhere above. Then they are at the doormat with the count at nothing.
  - Nobody whispers from under the stair. They remember for themselves where it went wrong (`FeetUpTexts.hintLater`, which knows there is no hall floor or stair).
- **Let go at the door.** A reader who goes into the dark while the game is not on for them, before starting or after finishing, is let go at the doormat, uncounted. Nothing climbs out of the dark.
- **What still works.**
  - The chairs come while nobody looks.
  - The fan sweeps the table.
  - The dumbwaiter climbs when its rope is pulled.
  - The hatch's catch is reached from the press.
  - The loose boards go and come back unseen.
- **What is gone.** The voices, the shouting and the dark every ninety seconds.
- **The attic** ends the game. It marks the room solved for good, keeps the reader's tally (runs, fewest touches, a clean run), and says:
  - *On the rafter the tally is where it was, your own column among the others. In the column with no name there is one mark now, low down, where a small hand would reach.*
  - The ladder is left down or pulled up for friends exactly as in the first house. The way on is at the attic's north gable.

### Its words

All House writing: never the reader's gender, never a colour, never an age, and nothing explained.

- **The rules page** on the hall table, read in the house later: *The rules are where they were, on the hall table. Under the fifth, in the smallest hand, someone has added a sixth: 6. If you're the last one, count for yourself.*
- **The chalk**, rubbed faint, the same words as before. Two of them are answered:
  - THE BOARDS GO. KEEP GOING, and under it, newer: THERE AREN'T MANY LEFT.
  - WAIT FOR ME, and under it, in a bigger hand: WE DID.

Proposal question 6 asked how much the attic says. In the first house: a tally with an empty column. In the house later: one mark in it.

## Also, in both houses

The game now starts only when the reader stands on something solid. A reader settling onto the doormat, a hair above it, no longer counts as having climbed something.

## Art

`tools/draw_feet_up_art.py` draws three more textures: the dark under the house, the jar's glaze and a chalk stub. It also draws the chalk jar's two models, full and empty. No existing texture changes.

## Tests

Four new cases in `FeetUpTests` (literary namespace), beside the nine from 0.4.200, which pass unchanged. The route measurement is now a shared helper (`FeetUpTests.footings`).

1. **The house later has no floors and the same way up.**
   - It is a deep puzzle room built on Feet Up, with no story, and its way on is at the attic gable.
   - No stair, tile, floorboard or board that holds is left under a room, and no loose board off the way up.
   - The dark lies under every room, and the doormat is kept.
   - Nothing is lit, the fire is cold, the parlour is open and the jar is empty.
   - Every step of `LATER_START`, `ROUTE` and `LONG_WAY` is a footing within a walking jump.
   - It is offered only deep, and only to a reader who has finished the first house.
   - It exports the `feet_up_later` architecture view.
2. **A fall into the dark counts and three are out at the doormat.**
   - A fall counts once, the reader is put back, and nothing hurts.
   - A reader who never started is let go at the doormat uncounted.
   - Three falls are out, with a remembered hint, and end at the doormat.
   - The door stays open and the lamps out, and the first house's count is untouched.
3. **The house later's attic solves it and resolves no story.**
   - The hatch works from the press, and the attic finishes the game and marks the room solved.
   - The clean run is kept, with no account and nothing of the first house resolved.
   - Its words never read the reader or name a colour.
4. **The attic jar gives one stub and it keeps a short fall free.**
   - Nothing for an unfinished reader, one stub for a finished one, and never a second.
   - The stub is the worn House chalk with its line.
   - A five-block fall is forgiven with it, not without it and not from nine blocks.
   - The afterwards line counts it.

## What stays

- **Witness:** 81 / 61 / two kinds / three endings. The house later is not a source.
- **Protocol** 163.
- **Layout** 123: one appended slot (222). New builds only, no migration.
- **The first house** plays exactly as before.
