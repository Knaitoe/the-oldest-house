# The airing rooms: the wedge, the chair and the air together (0.4.245)

The owner, 2 November:

> Combine the exploration mechanics. A wedge holds a door open, changing the draft in the adjoining room. Dragging a chair into that
> doorway holds it open but leaves a scrape across the dust. That lets deeper puzzles build on things players already understand,
> with visible evidence of their solution.

The airing rooms (`AIRING_ROOMS`, slot 155, layout 78) are a deep puzzle room built on two others: the vent rooms (the air,
`docs/DRAFTS_0_4_230.md`) and the sprung rooms (doors held open, `docs/TRACES_0_4_231.md`). Nothing in them is new to the reader:
every part is something an earlier room taught. Like every puzzle room:
- nothing counts down, hurts or is used up;
- a solved room stays solved, for everyone;
- there is no Witness credit, and no protocol or source-kind change.

## When it is offered

It comes from the strange depths, with the shared puzzle rhythm (a story since the last puzzle, a few halls walked), and only once
**both** the vent rooms and the sprung rooms are solved. `PuzzleRooms.Puzzle` now names what a room is built on (`builtOn()`): its
family's early room for every other deep room, or these two for the airing rooms.

## The rooms

The plan is the vent rooms' own, so the air goes the same ways for the same reasons:

- **The first room**, narrow and papered, under dust. Air comes down cold through a grate in the ceiling, with two sheets on a line
  under it. The wedge's hook is by the way in: it gives the reader a wedge only if they have never taken one.
- **The first door**, on a closer: left open, it swings shut by itself thirty ticks later unless something holds it.
- **The second room**, wide, with furniture under dust sheets and its own line of sheets. A louvre stands open in its west wall with
  curtains either side. The one loose chair stands here.
- **The second door**, also on a closer, set in a plaster wall.
- **The third room**, plain plaster all round, with a curtain on a rail by the east wall. Behind that wall are the seam, a short
  passage and the far door, which has no handle on this side: "The door has no handle on this side. Cold air comes round its edges."

## What the air does

| First door | Louvre | Second door | The air |
|---|---|---|---|
| shut | any | any | held in the first room, dust under its door, the second room's sheets still |
| held open | open | any | through the second room (its sheets belly) and out of the louvre, pulling its curtains |
| held open | shut | shut | through the second room, held at the second door, which rattles |
| held open | shut | held open | through to the third: the curtain lies flat and dust lifts off the edges of one panel of plaster |

- **The wedge in the first door changes the draft next door at once.** The second room's sheets belly and the louvre's curtains
  pull. Walking through without holding the door shows the same for a moment, then the door swings shut and the sheets fall
  still.
- **The chair in the second doorway holds it.** The reader drags the chair a block at a time into the cell before the door. Every
  cell it leaves is scored where it was dragged, so a scrape runs across the dust from where it stood to the doorway. A second
  reader standing in a doorway holds it too.
- **The plaster gives.** Pressed while the air is at it: "The plaster gives under your hand: a panel, and it swings in on the air you
  let through." The far door opens, and the room is solved for good.

What solved it stays to be seen: the wedge lying at the first door's foot, the chair in the second doorway, and the scrape and
footprints across the dust. Nothing tidies any of them away.

## Code

- `LabyrinthPlace.AIRING_ROOMS` (box -12,-1,-28 to 12,6,0; entry 0,0,1; far 11,0,-28). `LabyrinthBuilder`:
  - `VERSION` 78;
  - built at the strange depth by `DraftRooms.buildAiring`.
- `LabyrinthDealer`: a gray place from the strange depths, with no plain-hall weight.
- `DraftRooms`:
  - `AIRING`, `B_SHEETS`, `AIRING_HOOK` and `AIRING_CHAIR`;
  - `airAiring` (the vent rooms' rule, plus the second room's sheets);
  - the fittings' hands and the ten-tick clock now include the airing rooms;
  - the seam solves whichever room it is in;
  - `flowAt` for tests.
- `TraceRooms`: `SPRING_ROOMS` and `springs(place)` include the airing rooms' two doors. Their far door is opened by the air, not by
  the closers.
- No new textures: the rooms use the draft fittings and materials and the trace fittings and materials as they are.

## Tests

`DraftRoomTests#theWedgeAndADraggedChairLetTheAirThroughToThePlaster` (exploration namespace):
- **When it is offered:** not before both rooms it builds on are solved, and not with only the vent rooms solved.
- **The build:** built whole as a deep puzzle room with no story; dust over the floors; both doors on closers.
- **The first door:**
  - shut, the air is held and the second room's sheets hang still;
  - wedged, the second room's sheets belly and the louvre's curtains pull, and the wedged door does not swing shut.
- **The second door:** with the louvre shut, the air is held there, and opened, the door swings shut again.
- **The chair:**
  - dragged twelve blocks into the second doorway, each cell it left scored;
  - it holds the door, and the air reaches the plaster.
- **The finish:** pressed, the panel opens and the far door with it, and the wedge, chair and scrape are still there.

The vent rooms, the sprung rooms, the slot fit for every place, the dealer and scent cases, and the puzzle pacing case pass
locally. This is pending CI.
