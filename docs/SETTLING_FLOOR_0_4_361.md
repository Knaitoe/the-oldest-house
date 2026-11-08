# The Settling Floor (0.4.361)

This is item 3 of the owner's "New Puzzle and Hazard Room Designs". The owner's notes describe it this way:
- a long gallery of boards over soft fill;
- standing or walking in one place for more than a few seconds settles that section one step, for good;
- settled sections open gaps under the skirting, and drafts move through them as they do under doors;
- a heavy object (a free-standing cabinet or a rolled carpet) can be pushed to concentrate weight and settle the floor faster;
- once enough has settled, a solid panel in the far wall shows daylight and cold air at its edges: a new seam, in the airing rooms'
  language;
- it is safe: no fall damage, no soft-lock, and it never collapses.

The tests the notes ask for:
- the settling is permanent across reconnects;
- the gaps redirect drafts;
- the far seam appears only after enough settling;
- the heavy object speeds settling under it.

It builds on the traces (0.4.231: the floor keeps what was done on it) and the drafts (0.4.230: the air shows where it is going).

**Pacing.** The owner lists it among the hazards, but nothing in it can hurt, so it is paced as a puzzle room:
- **The place.** `SETTLING_FLOOR` (slot 217) is a deep room in the AIR family. It is offered from the strange depths once the drying
  room and the propped door (the early rooms of the air and the traces) are solved. Like every puzzle room, it is offered only after a
  story, and rarely.
- **The door.** The far door is iron and has no handle on this side until the panel is open.
- **What stays.** Settled boards stay settled for everyone, and solved stays solved. Nothing counts down or is used up.
- **No soft-lock.** The way back is always open.
- **Witness.** No credit.

Layout 116, protocol 155. No Witness or source-kind change: 80 / 60 / two kinds / three endings. Code: `SettlingFloor`,
`SettlingBlock`.

## The gallery

A plastered gallery, 7 wide and 27 long:
- a skirting along both walls and a dark wood rail at shoulder height;
- lanterns hung down the middle;
- a line across the middle with two dust sheets on it.

**The floor.**
- **The runner.** A flagstone runner goes the length of the gallery, down the middle. It is the joist line and never moves.
- **The flags.** There are flagstones at the entry and before the far wall.
- **The boards.** Boards lie either side of the runner, over a bed of soft fill, in twelve sections: west and east of the runner,
  six along the gallery, each three boards wide and four long.

**The far wall.** The far wall is the same plaster as the rest. One panel of it, two blocks high, is a door, and behind it a short
passage leads to the iron far door.

**The cabinet.** A heavy free-standing cabinet stands on the runner a third of the way down, in the way.

## How it settles

- **Weight.** Every half second the room adds up the weight on each section: each reader standing or walking on it, and the cabinet
  if it stands there, which counts as two readers.
- **A step down.** Three seconds of one reader's weight take a section down one step, three pixels. The cabinet alone takes a
  second and a half; the cabinet with a reader, one second. Stepping off a section before then starts its count again.
- **How far.** A section goes down at most three steps (nine pixels, a little over half a block), always low enough to step back up
  out of. Nothing falls through, nothing gives way and nothing hurts.
- **What a reader notices.**
  - The boards creak while weight is on them.
  - A section that goes down does so with a groan and a soft thump, and dust puffs up from the fill. Whoever is standing on it reads
    "The boards go down under you, a step, and hold."
  - A hand on a board says how far it has gone.
  - The cabinet goes down with the boards it stands on.
- **The cabinet.** A reader leaning on the cabinet pushes it one cell directly away from them, along the gallery or across it. It
  moves only onto the gallery's own floor, never against a wall or onto anyone, with a scrape. Left on a section, it settles it by
  itself while anyone is in the room.

## The air

- **The gaps.** At two steps down, the skirting along that section's wall stands clear of the boards. A dark gap opens under it,
  and cold air comes through. Whoever is standing there is told so.
- **Still.** With no gaps the air is still and the dust sheets hang straight.
- **Out under the entry door.** With gaps open and the far wall still plaster, the air has nowhere to go but out under the door the
  readers came in by. Dust slides from the gaps toward it, and the sheets belly toward the entry.
- **The seam.** When twelve steps have settled in all, the far wall's panel shifts in its frame with a dry crack, and readers hear
  "At the far end of the gallery, plaster cracks." Daylight shows round its edges, dust lifts off them, and the air turns: the sheets
  belly toward the far wall and the dust slides there.
- **Opening it.** Pressed before then, the panel is only plaster. Pressed now, it gives: "The plaster gives under your hand. It was a
  panel, and cold air comes round it." The iron far door beyond it opens, and the room is solved.
- **Built again once solved.** Every west section is two steps down, with its skirting open, and the panel and far door stand open.

The dust sheets are the draft rooms' own sheets on their line. A hand on one says which way it is pulled.

## Assets

- **The block.** One new block, `settling_fitting` ("Gallery Fittings"), with kinds board, skirting, panel, upper panel and
  cabinet, a facing and a stage from 0 to 3. A board's shape follows its depth. The cabinet's model sits on whatever the boards under
  it have come down to.
- **Art.** Drawn by `tools/draw_settling_fittings.py` in vanilla's manner, twelve textures:
  - boards at four depths, their joints opening and a crack spreading;
  - the boards' cut ends;
  - the skirting, tight and lifted;
  - the panel's two halves with daylight at their edges;
  - the cabinet's front, sides and top.

  The plain panel uses the draft rooms' plaster itself, so before it shifts it is exactly the wall round it.
- **Sounds.** Four subtitled sounds are synthesized by `tools/generate_settling_audio.py`:
  - "Floorboards creak";
  - "Floorboards settle";
  - "Plaster cracks";
  - "A cabinet scrapes".
- **Checks.** CI and the quick gate check both tools with `--check`.

## Tests

SettlingTests (exploration namespace, batch `settling_floor`), `theBoardsSettleUnderWeightUntilTheFarPanelShifts`, with a real
reader's weight:
- the panel is only plaster at first;
- a moment's weight settles nothing, and the runner never moves;
- three seconds settle a section a step, and only that section;
- a reader drops out of the game halfway through a stand on the next section and reconnects: the settled step is still there, and the
  broken-off stand counts for nothing;
- at two steps the skirting opens along that wall only, and the sheets belly toward the entry;
- the cabinet, pushed off the runner onto the boards, settles them with nobody on them, in under two seconds, and goes down with them;
- eleven steps leave the far wall plaster;
- the twelfth shifts the panel and turns the air toward it;
- pressed then, the panel gives and the far door opens;
- built again, the room is solved;
- nothing is earned.
