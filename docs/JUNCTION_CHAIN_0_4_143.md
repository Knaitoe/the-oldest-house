# The quiet rooms open from the junction — 0.4.143

The owner's October 28 playtest direction (recorded in [the House Read design](HOUSE_READ_DESIGN.md)): the quiet rooms should open from the first room after the impossible hallway, not from the hallway itself; every room needs a way forward; a finished story is only a door between halls.

## The chain

The owner chose a chain over five extra doors. The junction keeps its three onward doors. Until a reader has entered all five quiet rooms, any of those doors leads into their next room, in the design's order: waiting room, bathroom, coatroom, shelter, porch.

- **Forward.** Each room's own way on leads into the next room. The porch's way on (kitchen door, side gate or road) arrives in the hall the junction door was dealt, as one ordinary forward crossing.
- **Back.** A room's entry returns the reader to the junction, in front of the door they used. A room they turned back from is not offered again; the chain resumes at the next one.
- **Private.** The rooms stay private copies, one set per reader, built ahead as before.
- **Nothing else changes.** The rooms give no route depth and no story credit. Only the junction's own onward doors start the chain; operator and command routes, and every other hall, are never diverted.
- **Saved visits.** A visit begun before 0.4.143 keeps its stored source and next destination. Only visits started from the junction carry the chain flag.

## The hallway doors

New impossible hallways are a plain approach again. In saved worlds, each of the five 0.4.142 hallway doors is walled up with its neighbouring course of wall, unseen, and dropping nothing. That happens only once it is loaded, no player camera is within 24 blocks, and no reader's active visit still returns through it. Until then the door is locked. A reader whose source door was retired returns to the far door's hallway side. A retired door is never revisited, so later edits stay.

## Finished stories are passages

When a dealt door leads to a story this reader has resolved, and that story can no longer be dealt to them, the House re-deals the door to an ordinary way on and takes them straight through. It also does so for a door left dormant behind a finished story. Recurring places never finish, and a shared ending still owed to the reader keeps its story. Other readers' maps of the same door are untouched.

## Tests

Three native cases in the quiet-room job, which now runs 15:

- the junction's real west door leads through all five rooms, with an observer, a reload, a retreat and the porch's way on into the dealt hall;
- saved hallway doors are walled up only when unwatched and free of active visits;
- a finished story's dealt door leads into an ordinary hall.

All 15 cases passed locally. The 0.4.142 hallway-door case is replaced by the first two.

No layout, protocol or Witness change: 54 / 41 / two kinds / three endings.
