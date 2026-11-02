# Room repairs: the window cleaner, the windows, the washing lines (0.4.263)

Three things the owner found in play, with screenshots:

- **The window cleaner's room:** "the window cleaner room is a MESS dude. the glass panels aren't appropriately faced. NPCs are
  jumbled together."
- **The airing room:** "airing room has disconnected textures."

## Two cleaners in one cradle

The room kept its cleaner by a saved id and made a new one whenever that id did not answer. It does not answer while the
cleaner's chunk has its blocks but not yet its entities, which is exactly when a reader walks back into the room. The old cleaner then
loaded beside the new one. The reader found two cleaners in one cradle, each with the line it last spoke still over its head (only the
saved one's line was ever wiped).

`RoomActors` now keeps a room's one actor:

- it waits until every chunk under the room has its entities;
- it keeps the saved actor, or else the first of its kind standing in the room, which becomes the saved one;
- it removes any other of its kind;
- it wipes spoken lines from every one of them.

The window cleaner's room and the Night Operator's room (built the same way, with the same fault) both use it. Building either
room removes every actor of its kind in it, not only the saved one.

## Windows that were posts

A glass pane set by hand in its plain state stands as a bare post: nothing tells it about its neighbours. The cleaner's windows on
the well, the plant room's window, the stair's windows and the corridor's were all posts. `RoomActors.settlePanes` gives every pane
and bar in a room the joins it would have had if it had been placed by hand.

It runs at the end of building these rooms, all of which set panes this way:
- the window cleaner's room and the Night Operator's room;
- both fuse rooms;
- the flat and the pool;
- the four rest rooms;
- the People Lights house;
- A Place for You.

## The washing lines

The sheets hung from the top of their own block, so a gap of almost a block separated each sheet from the line it was meant to be
on. Their faces ran past the bottom of the block with no UV coordinates of their own, so they sampled whatever lay next to the
sheet's texture in the atlas. And each line stopped short of the walls: by a block in the narrow rooms, and by seven blocks either
side in the airing rooms' wide room, where it ended in mid-air.

- **The sheets** (tools/draw_draft_fittings.py) hang over the line: they fall in front to below their block, with a short flap
  behind and a fold over the top. Each piece is no taller than a block and mapped inside the sheet's own texture. The bellied sheet
  curves out the same way.
- **The lines** run wall to wall: the drying room's from x -6 to 6, the vent rooms' and the airing rooms' first rooms' from -4 to 4,
  and the airing rooms' wide room's from -11 to 11.

## Tests

- **WindowCleanerTests.theRoomKeepsOneCleanerAndItsWindowsAreGlassAcross** (batch window_cleaner_one) checks:
  - the well's windows, the plant room's window and the stair's window are panes joined across their openings;
  - a second cleaner set in the cradle is removed, leaving the room's own.
- **DraftRoomTests:** the drying room's, the vent rooms' and both airing rooms' lines are unbroken from wall to wall, solid at each end.

The rooms' existing cases are run alongside these: the cleaner, the operator, the fuse rooms, the draft rooms, the rest rooms, the
flat, the pool, People Lights and A Place for You.

## Run 1410 (0.4.262)

The literary job and the build job's full suite both failed one case, Borrasca's four confirmed returns. Before the third arrival, it
waits for the original cast to be recalled to their places, which needs the chunks under them loaded from disk. Since 0.4.262 the men
walk Nell up the lane, so after the taking the cast probably stands over more chunks than it did. CI runs test ticks faster than real time
while it waits: the suite's memory lines show 1,200 ticks in a second and a half, so the case's 2600-tick budget ran out while the cast
was still loading. Locally the case finishes in 12 seconds. Its budget is now 6000 ticks, in line with the other long Borrasca cases
(4400 and 5200), and while a staging or a recall is not ready yet the server thread rests five milliseconds a tick (the pattern the
Backseat and entrance cases already use), so the budget covers real time. A failed staging now says where each of the cast is, where
they are going and what holds them (BorrascaCast.why).

## Run 1411 (0.4.266)

The literary job failed the same case again, and the new message gave the real cause. Nell and both men were standing at the taking,
"watched", so the staging would not move them however long it waited. Nobody in that case was watching them. Every Borrasca case uses the
same site on the same origin, and the strokes case (new in 0.4.262), which watches the men take Nell from close by, had no cleanup after
its batch. Its reader was left standing there, looking at the taking spot, when the four-returns case ran next. The strokes batch now
closes its fixture like every other Borrasca batch. The wall-clock rest above stays: it is still needed on a loaded runner.

The same run failed the refinery case after two seconds of real time, waiting for its site's chunks on an 800-tick budget. Its budget
is now 3000 ticks; its readiness check already rests five milliseconds while waiting.

The native client check also caught the draft sheets' models: the folded sheet's elements were written as a list inside the element
list, which the client's model reader refuses. tools/draw_draft_fittings.py now writes one flat list, and sheet_0/sheet_1 are
regenerated from it. The selected-cases namespace gains the writing template (a copy of that namespace's empty structure), so the
writing corpus can be exported alongside a chosen case for a local client check.
