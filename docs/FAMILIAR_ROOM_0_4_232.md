# The familiar room (0.4.232)

The owner, 1 November (`docs/PHYSICAL_HOUSE_PROPOSALS_2026_11_01.md`, proposal 7): "A modest kitchen that comes back through
different approaches. Its contents persist: supplies left, chairs arranged. Its ordinary sounds (the fridge, the clock, the tap) are
its own, and can be heard through other doors (0.4.229). Over several visits, its window shows passages the reader has already
walked, from above. Shared: groups who find it meet there."

`labyrinth/FamiliarRoom`. One new gray place (slot 153), layout 76; no vignette, source kind, Witness or protocol change.

## The kitchen

One kitchen, the same for everyone (never copied for a reader), built whole: lino, panelling to the dado and faded paper above, a
run of counters along the west wall with the pantry (a barrel), the sink under the window, the stove and the fridge, cupboards over;
a table in the middle with four chairs and a light over it; a clock in a frame on the north wall. Its way in, a door on through its
north wall and one through its east.

It is dealt like a hall (weight 4) from the third crossing, from whatever hall a reader is in: the same room through different doors.
While anyone else is in it, it is dealt far more readily (weight 40) to everyone else, so readers who have found it tend to meet there.
Nothing in it is a story; nothing counts toward the Witness.

- **Its contents persist.** It is built once and kept. The pantry starts with a little (three loaves, two apples, a candle) and is
  never restocked: what is taken stays taken, and what is left in it stays for whoever comes next. Its chairs are the loose chairs of
  0.4.231: pulled, they stay where they are left, and a chair drawn up to its table can, rarely, find a second drawn up across it.
- **Its sounds are its own.** The clock ticks every second, the tap drips now and then, the fridge hums; and its clock is heard,
  faintly, through any closed door that leads to it (`DoorSounds` carries it as a clock), so a reader can know the kitchen is behind a
  door before opening it.
- **Its window.** The window over the sink looks down into a yard below. The first time a reader comes, there is only the yard. Each
  time after, the yard is laid out, for that reader alone, like the last ordinary halls they walked, seen from above at a third of
  their size, one more hall each visit up to three: their floors in pale stone on the dark ground, the walls and furniture left dark,
  the nearest the window the hall walked last. Another reader at the same window sees their own. The yard itself never changes.

A wedge (0.4.231) holds the kitchen's door as it holds an ordinary hall's.

## Tests

`FamiliarRoomTests` (exploration namespace): the kitchen built whole, the same for everyone, with two ways on and no story; its clock
carried through doors and hanging on its wall; the pantry stocked, then a loaf taken and string left, and built again as left; a chair
at the table pulled back and staying there; dealt from the third crossing, and far more readily to a reader while someone else is in
it, but not to the one who is. The window: an ordinary hall walked remembered and a puzzle room not; nothing shown the first visit;
the next, the hall last walked, its floor seen from above at a third of its size and laid out in the nearest lane of the yard, with
the yard itself unchanged. `LabyrinthTests`: twenty-two gray places at the start.

0.4.238: the window's yard is laid out again after a reconnect or when its chunk is sent afresh; see docs/REVIEW_FIXES_0_4_238.md.

0.4.239: the kitchen in use (chairs to sit in, the tap, the stove, the fridge, the cupboards, the table) and calm; see docs/KITCHEN_AND_DOORS_0_4_239.md.
