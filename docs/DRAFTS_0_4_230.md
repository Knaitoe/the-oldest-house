# Drafts and pressure (0.4.230)

The owner, 1 November (`docs/PHYSICAL_HOUSE_PROPOSALS_2026_11_01.md`, proposal 5): air moves from one opening to another and you can
see it; an early room that teaches what the House's air does, a deep room that carries one draft through several spaces to a door
found only by watching the air; and ordinary halls with faint drafts of their own. And, for the lighter puzzles: "a simple draft
becomes ventilation across several rooms. Learning something useful should itself feel rewarding."

`labyrinth/DraftRooms`, `DraftBlock`; the fourth puzzle family, `PuzzleRooms.Family.AIR`. Like every puzzle room: offered by the
shared puzzle rhythm (an early room from the third crossing and never in the stone, the deep room only once the early one is solved
and only from the strange depths); nothing counts down, hurts or is used up; a solved room stays solved, for everyone; no Witness.
Layout 74 (slots 148 and 149). No protocol change.

## The air

Air comes in at one opening and goes out by the easiest other, and it shows: sheets on a line belly toward where it is going,
curtains are pulled flat back against their wall, dust slides under a shut door and the door rattles in its frame, and dust motes
drift along its way. A door that is the only way left for it is pushed open by it.

## The drying room (early, slot 148)

A laundry (boards, a copper, a basket, sheets drying on a line across the room) and, through an inner door, a scullery (quarry
tiles, a stone sink, a shelf, a sash window with curtains either side, and the far door, iron, with no handle on this side: "The door
is stuck in its frame. It gives a little and settles back, as if it wanted pushing harder than you can.").

| Vent grille | Inner door | Window | The air |
|---|---|---|---|
| shut | any | any | still; the sheets hang straight |
| open | shut | any | leans on the inner door: the sheets belly toward it, dust slides under it, it rattles |
| open | open | open | goes out of the window, pulling its curtains |
| open | open | shut | has only the far door, and opens it: "The air in the room leans on the far door, and it gives." |

Everything is right-clicked: the grille slides open and shut, the sash goes up and down, the inner door is an ordinary door.

## The vent rooms (deep, slot 149)

Three rooms in a line. In the first, air comes down cold through a grate in the ceiling (dust falling from it, sheets on a line under
it). The second is wide, with benches, and a louvre in its west wall, open, curtains either side. The third is plain plaster all
round with a curtain on a rail by its east wall, and nothing else.

| First door | Louvre | Second door | The air |
|---|---|---|---|
| shut | any | any | held in the first room, dust under its door |
| open | open | any | out through the louvre, pulling its curtains |
| open | shut | shut | held at the second door, which rattles |
| open | shut | open | into the third room: the curtain lies flat against the east wall, and dust lifts off the edges of one panel of plain plaster |

That panel is a door (`DraftBlock` SEAM): until the air is drawn to it, it is drawn with the plaster's own texture and answers only
"Plaster." Pressed while the air is at it, "The plaster gives under your hand. It was never plaster: a panel, and it swings in on
cold air." It opens on a short passage to the far door, which opens with it. Once open, it stays open.

## Ordinary halls

Every second, near a reader in an ordinary hall, a little dust slides over the floor toward the gap under a nearby House door, more
often and higher to an open one: an empty room has something to read without a note or an encounter. (Not in stories or puzzle
rooms.)

## Art

`tools/draw_draft_fittings.py` (16 textures; three materials, one fitting block of eight kinds). Its check also holds that the plain
seam uses the plaster's own texture, not a near likeness.

## Tests

`DraftRoomTests` (exploration namespace): the drying room built whole as an early air room with no story; the far door shut with no
handle; still air with the grille shut; the grille opened and the air held at the shut inner door, the sheets bellied; the inner door
open and the air out of the window, curtains pulled, the far door still shut; the window shut and the far door opened, solved for
good, built again as it was left. The vent rooms: the seam a wall like the plaster, pressed with the air still only plaster; the
first door open and the air out through the louvre; the louvre shut and the air held at the second door; the second door open, dust
off the seam and the curtain flat; pressed, the panel opens, the far door opens, the passage behind is clear, and it stays open.
`LabyrinthTests`: nineteen gray places at the start, the drying room among them.
