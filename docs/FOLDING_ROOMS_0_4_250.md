# The Folding Rooms (0.4.250)

The owner's first puzzle proposal of 2 November (docs/PUZZLE_PROPOSALS_2026_11_02.md):

> A former dining room has heavy wooden partitions on floor and ceiling tracks. Moving one opens a passage while closing another; gaps
> above the panels let you see where you're trying to reach. The early version teaches one sliding partition. The deep version has
> several connected spaces, with wall recesses where you can stand while shifting them. Worn tracks and furniture impressions show how
> the room used to fit together. Solving it leaves an understandable, usable space.

The release adds two puzzle rooms in a new family, FOLD:
- the folding room (`FOLDING_ROOM`, slot 156, early);
- the folded rooms (`FOLDED_ROOMS`, slot 157, deep).

The layout version is 79 (new builds only). Both rooms follow `PuzzleRooms`' pacing:
- they are offered only after a story, and rarely;
- the deep room comes from the strange depths, once the folding room is solved;
- nothing counts down, hurts or is used up, and solved stays solved for everyone;
- no Witness credit.

There is no protocol, Witness or source-kind change. Code: `FoldingRooms`, `FoldBlock`.

## A partition

A partition is a wall of hinged oak panels as wide as the room and two blocks high:
- **Tracks.** It hangs from a brass track in the plaster ceiling, on hanger rods, and is guided by a brass track let into the
  herringbone parquet. Both tracks run the length of the room.
- **The gap above.** Above it, to the ceiling, the gap is open but for the rods. The far side can be seen through it, the lamp and the
  top of the door frame, but not reached. The upper panels stand half a block taller to the hand than to the eye, as a fence does. So
  nothing in the room, not a chair, the table or the sideboard, can be climbed to get over.
- **Pushing it.** A hand on any panel pushes the whole partition one panel's length along its track, away from the reader, with a
  rumble. It stops at the end of its track, at furniture, and at anyone in its way: it never moves into a body.
- **From beside it.** In a recess, or in a doorway, off the track, the reader is beside the partition rather than in front of it, and
  it goes the way they face. So it can be pushed right past whoever is standing there, and they step out on its other side.
- **Undoing.** Every move can be undone by pushing it back.

## The folding room (early)

A dining room panelled to the dado and papered above. Its table and four chairs are at the south end, and a lamp hangs each side of the
partition.

The partition stands across the room against its north stop, right in front of the far door in the east wall. Pushed from the dining
room it will not go: it is at the end of its track. The pantry runs along the west side, with a doorway into the dining room on either
side of the partition. Through it, from the north side, the partition pushes south:
- it clears the door;
- it comes to rest across the serving hatch from the pantry;
- the way the reader came by is still open, and from the dining room it pushes back.

Reaching the door solves the room. The partition folds back on its hinges into the corners, and the room is one dining room. Built
again after that, it stays folded away.

The far door is an ordinary door; only the partition is in the way, as only the cage is in the Service Lift.

## The folded rooms (deep)

A long dining room cut in three by two partitions. A morning room opens off the west side.

**The first partition** stands at the dining room's south end:
1. Pushed up the room, it passes a recess in the west wall.
2. The recess has the sideboard in it, standing where the floor shows its feet have always stood: four squares less worn than the boards
   round them. The sideboard pulls out a block at a time toward the reader, as the dust rooms' chair does.
3. Pulled out of the recess, and out of the partition's way, it leaves the recess free to stand in.
4. From the recess, facing back down the room, the partition goes past the reader, and they are through.
5. Put back on its marks, it fills the recess again: tidying up undoes the way through, and that can be undone too.

**The second partition** stands across the middle room, in front of the morning room's far doorway:
1. The morning room's near doorway is on the reader's side.
2. Standing in the far doorway, off the track, the reader pushes the second partition past them the same way.
3. The way on is the far end of the room.

Both partitions can be crossed back the way they were crossed. The tracks are worn bright beside the recess and the doorway, and where
each partition has stood most often.

Reaching the far end solves the rooms:
- each partition folds back on its hinges into the corners, at the worn place on its track where it has always folded away, or where
  it stands if something is in the way there;
- the table is laid down the middle of the room, with a chair either side on the worn marks in the boards (each piece when nobody is
  standing where it goes);
- the sideboard goes back into its recess.

## Assets

`tools/draw_fold_fittings.py` (checked in CI) draws everything:
- **Materials.** Eight of them: herringbone parquet; the floor track, plain and worn bright; worn parquet with furniture marks; dark
  panelling; dining room paper; the plaster ceiling and its track.
- **Fittings.** The `fold_fitting` block:
  - the panel (lower, upper, upper with brass pulls);
  - the hanger rod (plain, with its carriage);
  - the folded partition (lower, upper);
  - the sideboard, the table and the chair;
  - the serving hatch (shut, open).

## Tests

`FoldingRoomTests` (exploration):
- **`theFoldingRoomsPartitionClearsTheDoorWhenPushedFromThePantrysSide`:**
  - against its stop from the south;
  - pushed off the door from the north;
  - back again from the south;
  - stopped by a bystander;
  - solved at the door and folded into the corners, and folded still when built again.
- **`theFoldedRoomsAreCrossedByStandingOffTheTrackAndLeftLaidForDinner`:**
  - the first partition pushed past the recess;
  - the sideboard pulled out of it and off the track;
  - the partition pushed past the reader from the recess;
  - the second pushed past from the morning room's doorway;
  - solved at the far end: both folded (one home, one in place where the sideboard took its home), the table and chairs laid, the
    sideboard back in its recess, nothing earned.
