# The second chair (0.4.659)

The owner, with the notes that led to 0.4.656: "Let players answer the House through ordinary actions. A flat sets the table for one.
The player pulls up a second chair, and on a later visit there's a plate at that place. A response like that gives the player some say
in the relationship. It also leaves room for the uncomfortable question of who the House thinks they invited."

The familiar room (0.4.232) is the House's own kitchen, the one that comes back through different doors and keeps what is left in it,
so it is where the House sets its table. No Witness, layout or protocol change. New builds only.

## Now

- **Laid for one.** The kitchen's table is laid for one at its south side, where its one chair stands: a round plate, a knife and
  fork, and a glass. The kitchen's other three chairs stand back against its walls, facing into the room, clear of its doors and counters. All four
  are loose, as they always were: pulled a block at a time, and left where they are left.
- **A second chair.** A chair a reader draws up to another side of the table, facing it, is laid for: the next time nobody is in the
  kitchen and nobody can see the table, a second place is set where that chair stands. The reader who comes back finds it.
- **Taken away again.** If that chair is pulled away from the table, or turned from it, the place is cleared the same way: the next time
  nobody is there to see.
- **Only one more.** The chair drawn up last is the one laid for. The table is laid for two at most: the House's one and the reader's
  one. A chair the House set at the table itself, or one nobody drew up, lays nothing.
- **Nobody sees it happen.** Nobody may be in the kitchen or at its door, a spectator's camera included, and no living reader may see
  the table from further off. Until then the table waits, and is looked at again every second.
- **What is on the table.** Things set down on the table go onto the nearest free plate, or into a corner clear of the places laid.
  When a place is laid or cleared, whatever was lying there is moved onto the plate or clear of it, never left under it; if there is not
  room for everything, the table is not laid. Nothing is lost.
- **The visitors.** The kitchen's visitors still take the free chair at the table nearest the door. A reader sitting in the House's
  chair leaves them the place the reader drew up; with no chair free at the table, a visitor sits in one by a wall, facing the room, and
  steps out of it to the front. They sit down and stand up from behind a chair at the table, never through the table.
- **No word.** Nothing is said about any of it. The plate is the whole of it.

## With others

The kitchen is one for everyone, so its table is one table. Whichever reader drew a chair up last, the place is laid for that chair, and
every reader sees the same table. It is laid and cleared only while nobody at all can see it.

## Checked

- `FamiliarRoomTests.theTableIsLaidForTheChairDrawnUpToIt`:
  - The kitchen is built laid for one, its chair at the laid side and the other three against the walls.
  - A reader pulls a chair up to the north side. Nothing is laid while they are in the room.
  - Once they have gone, the north place is laid, and an apple left at that corner of the table has been moved onto the plate.
  - Back in the room, they pull the chair away. Nothing changes while they are there; after they leave, the place is cleared and the
    apple lies on the bare top.
  - A chair set at the west side that nobody drew up lays nothing.
  - With the reader sitting in the House's chair, a visitor's chair is one of those by the walls, and the visitor steps out of it to the
    front.
- `FamiliarRoomTests.theKitchenIsForUseAndNothingFrightensAnyoneInIt` counts the table's room from its laid places.
- The other familiar room cases and the kitchen's visitors (`CastConductTests.theKitchensVisitors`); `tools/draw_trace_fittings.py
  --check`; build, package, branding and quick checks.
- Review frames of the table at every stage and every turn, near and far. In the first drawing the plate was four pixels across
  with its corners cut away and read as a cross; it is five pixels round now, a rim round a well, and places laid at neighbouring
  sides meet only at the plates' empty corners.

## Not covered

- Only the kitchen answers a chair drawn up. Other tables do not, yet.

## New lines

None.
