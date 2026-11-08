# The laundry corridor (0.4.390)

The first of the new ordinary halls in [HALLS_TRIALS_AND_TIME_DESIGN.md](HALLS_TRIALS_AND_TIME_DESIGN.md) (hall 1, domestic): "Sheets hang on lines across a long hall. Doors are hidden behind some of them, and parting a sheet makes it sway behind you."

## The hall

`LAUNDRY_HALL`, slot 228, a gray room (`OrdinaryHalls`), built whole.

- **The corridor** is five wide, forty-one long and four high:
  - limewashed brick, tarred black at its foot against the wet;
  - quarry tiles underfoot;
  - a boarded ceiling;
  - a caged lamp between each pair of lines.
- **The washing.** Six lines are strung across it from wall to wall, at head height. On them hang:
  - plain sheets and striped ticking covers, to a hand's width off the floor;
  - towels, short, with a gap under them at knee height;
  - here and there a bare stretch of line, to see through.
- **The doors.** Three ways on, two of them behind the washing:
  - the **east door** is in the bay past the fourth line, which is hung full, wall to wall;
  - the **west door** is behind sheets hung along the west wall, on a short line between the second and third lines across;
  - the **far door** is past the last line, which is also hung full.
- **The rest.** Three wicker baskets of folded washing stand along the walls, and a stool for reaching the lines.

## The washing

The washing is one block, `hall_sheet` (`HallSheetBlock`), two blocks high: the line and the top of the cloth above, the rest below.

- **Nothing stops a body or a hand.** It has no collision and no outline, so the corridor is walked straight through, and a door behind a sheet can still be opened.
- **Light passes it, the view does not.** Each line hides what is past it until the reader is in it.
- **It swings.** A body walking into a sheet pushes it ahead of them.
  - The sheet stays pushed while the body is in it.
  - Once they are through, it swings back past where it hung, then settles.
  - The sheets either side stir a little.
  - It turns about the line, so the two halves stay one sheet.
  - A bare stretch of line never moves.
- **It sounds.** A soft flap as a still sheet is pushed.
- **It is shared.** Every reader sees the same sheets move. A friend going ahead leaves the washing swinging behind them.

No words are added. It is an ordinary hall: never a trial or a story, no mark and no Witness source.

## Dealing

The laundry corridor is dealt among the domestic halls, from the first crossing until the halls turn to stone (depth 7), as often as the vaulted hall. It is built with the other halls, ready before anyone can be offered it.

## Art

`tools/draw_ordinary_hall_art.py` is new, and CI and the quick gate run it with `--check`. It draws twelve textures:

- limewashed brick, and the same brick tarred at its foot;
- quarry tiles and the boarded ceiling;
- a linen sheet and a ticking cover, each in two halves, and a towel;
- the line and its pegs;
- wicker, and folded washing seen from above.

It also writes the washing's models at every stage of its swing (the cloth turned about the line for the push, shifted for the swing back and the settling), and the basket's and stool's models.

The materials are `hall_laundry_wall`, `hall_laundry_wall_low`, `hall_laundry_floor` and `hall_laundry_ceiling` (`OrdinaryHallRegistry`). The fittings are `hall_fitting` (`OrdinaryHallBlock`: a basket and a stool), which later halls will share. The lamps are the still light's caged lamps, lit. No vanilla building block is used in the hall.

## Tests

New case: `ExplorationPassTests.theLaundryCorridorIsCrossedThroughWashingThatSwingsBehindWhoeverPartsIt`.

- **The plan.**
  - It is an ordinary hall, built whole, ready from the start, dealt until the stone, with a name in prose.
  - All four doors are native doors, and a body walks to each onward door through the washing.
  - Every line is strung across, both halves, with a floor under it, room over its line and a ceiling.
  - The two full lines hide the east door and the far door.
  - The west door is behind washing along the wall, and a hand reaches through it.
  - The lamps are lit, the baskets stand, and two bodies walk side by side.
- **The swing.**
  - From the near side a sheet is pushed ahead of the body, both halves, and the sheets either side stir. Its tick is scheduled.
  - It stays pushed while the body is in it.
  - Once the body is through, it swings back and settles, both halves, and so do its neighbours.
  - From the far side it is pushed the other way.
  - A bare stretch of line does not swing.

## What stays

- **Witness:** 81 / 61 / two kinds / three endings.
- **Protocol:** 163.
- **Layout:** 129. One appended slot (228). New builds only.
