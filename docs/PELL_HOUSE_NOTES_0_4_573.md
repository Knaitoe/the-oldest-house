# The Pell house, the owner's playtest notes: walls and floors (0.4.573)

From the owner's notes on playing the Pell house on 13 November (the earlier batches: `docs/PELL_HOUSE_NOTES_0_4_566.md` to
`docs/PELL_HOUSE_NOTES_0_4_572.md`). New builds only. No protocol, layout or Witness change.

The owner: "the whole pell house is too uniform in its walls and carpets, and the kitchen isn't spaced believably. all of the
textures could be improved."

The kitchen was 0.4.572. This is the second part: every room's walls and every room's floor. The flower bed is the third.

## What was there

- **One plaster.** Every wall in the house showed the same off-white plaster on every face, floor to ceiling, with nothing at
  its foot and nothing at its head. The outside walls were cells of the house's fitting, brick on the face to the weather and
  that plaster on the other five; the walls between rooms were blocks of the plaster.
- **One carpet.** The hall, the entry, the living room and the three bedrooms had the same carpet. Only the kitchen, the
  bathroom and the laundry had lino.
- **Doorways.** The cell under every inside door and arch was the hall's carpet, and a room's own floor began past the wall
  with nothing across the join.

## A wall with two sides

The house's walls are a block of their own now (`PellWall`). A cell of wall is as thick as the cell, as before, and has a finish
to each of its faces:

- **Front and back.** A wall between two rooms has one room's finish on one face and the other's on the face behind it. An
  outside wall has brick to the weather and the room's finish inside.
- **Three cells from floor to ceiling.** Every finish is drawn in three: the lowest with the skirting at its foot, the middle,
  and the top with the cornice at its head. The outside brick keeps its stained lowest course.
- **Ends.** The faces at a cell's two ends are painted timber: a door's jamb, a window's reveal. Where a room goes round the
  end of a wall instead, the end takes that room's finish. The hall does this at the entry and through its two arches, so an
  arch's jamb is the hall's cream and not a metre of white timber.
- **Which face is which is worked out from the plan.** `PellSite.ROOMS` gives each room its cells, its finish and its floor,
  and `PellSite.wall` asks, for every cell that is no room's, what each of its faces looks into. Nothing is placed by hand, so
  a wall cannot show the wrong room.
- **Over the kitchen's benches** the back wall's middle cells are tiled instead of painted.
- **The bricked-up window** in the west wall keeps its newer brick outside, and has Nessa's paper over it inside.

## Daylight

The old outside walls let the day through them a little dimmer, because the fitting they were cells of does not stop light, and
every room was lit by its walls as much as by its windows. A solid wall would have left the rooms darker than they were. A cell
with brick on its front still lets light through the same way; a wall between two rooms stops light, as the plaster did. The
rooms are lit as they were.

## Room by room

| Room | Walls | Floor |
| --- | --- | --- |
| Hall and entry | Cream paint gone a little yellow, scuffed low down, white skirting and cornice | The carpet that was there, with its worn path from the front door to Nessa's |
| Nessa's room | A child's paper in duck-egg stripes, under the border of small animals that was already there | An oatmeal loop, vacuumed in stripes |
| Callum's room | Flat white rolled over everything, the skirting and the cornice with it; the roller's laps show | The carpet taken up: pine boards, with nails and specks of white paint. The circle is painted on the boards |
| Bathroom | Mint tiles to head height under a darker capping tile, paint above gone grey in spots | Small tiles in two greens, chequered |
| June's room | A dusty rose paper with a trellis in it, a small flower where the lines cross | A dusty mauve carpet |
| Kitchen | Pale yellow gloss, a cove where the lino turns up the wall; white tiles over the benches, one in eight with an orange flower | The lino that was there, with its worn and burnt places |
| Living room | An oatmeal paper with a gold diamond in it, over a skirting stained dark | A rust twist |
| Laundry | Painted sheet with a cover strip every half a block, a dark kickboard | Grey paint over concrete, the concrete showing where the paint has lifted |

No two rooms are finished alike except the hall and the entry, which are one space.

## Doorways

In each inside doorway (the four doors off the hall's front side, the laundry door, and both cells of each arch) the hall's
carpet runs to the door's line and the room's floor begins there, under a metal bar. Nine cells, each drawn from the two floors
it joins.

## What the frames show now

Eighteen frames from eye level, the same cameras as before the change.

- **The hall**, both ways: cream walls with a white skirting and cornice, the doors in their timber frames.
- **Nessa's room**: the stripes under the animal border, on all four walls; the door's jamb white.
- **Callum's room**: white walls with no trim of their own, boards, the circle on the boards.
- **The bathroom**: tiles to the capping band, paint over it, the chequered floor.
- **June's room**: the trellis paper and the mauve carpet, and nothing else. The room is still empty; that is next (below).
- **The kitchen**: yellow walls, the flowered tiles behind the benches from the fridge to the stove, the arch's jamb in the
  hall's cream.
- **The living room**: the diamond paper, the dark skirting, the rust carpet, the sliding door and the window in white reveals.
- **The laundry**: the sheet and its strips, the kickboard.

The frames are drawn at full brightness and show finishes and layout, not the game's light.

## What went

- The plaster block (`pell_plaster`) and its picture.
- The fitting's wall cell. The four corners of the house are still the fitting's corner, which is brick on both faces to the
  weather.

## Checks

- **New case** `PellHouseTests.everyRoomHasItsOwnWallsAndItsOwnFloor`:
  - every cell of the house's plan is a room's, a wall, or an opening in one;
  - every face of wall that looks into a room shows that room's finish (tiles over the benches, the hall's at the arches), at
    the level it stands at; every face out of doors is brick;
  - no two rooms are finished alike but the hall and the entry;
  - each room's floor is its own, and each of the nine doorways has its bar, under a door or under an arch;
  - a door's jambs are timber, and an arch's are the hall's on the faces the hall goes round;
  - an outside wall lets light through as the old one did, and an inside wall stops it;
  - the bricked-up window has its newer brick outside and Nessa's paper inside.
- `PellHouseTests`' case for the house as built now asks the new block for its brick (the stained course, the newer brick, the
  top course under the roof).
- `tools/draw_pell_house.py --check` draws the finishes and checks that its list of them is `PellWall.Finish`.
- **The Pell house namespace**: 60 cases. Architecture: 6 cases. The cast and two-reader namespaces: 102 cases.

## What local testing found

- The new case first reported no faces at all: it had made no reader, and a Pell case that makes no reader has no house built.
- The first frames showed each arch's jamb as a metre of white timber. An arch's cells now count as the hall's when a wall asks
  what its end looks into, and the case checks the jambs.
- Callum's boards read as a dark deck in the first frames; the gaps between them were lightened.

## Still to come

- The flower bed, the third part of the owner's note.
- **June's room.** It has its paper and its carpet and nothing in it, and it can be seen into when June stands in it with her
  door open. It will be furnished. A hand on her door from inside the room will open it, and June will not shut it on a reader
  who is inside.
- The furniture's own textures, where the frames show them flat.
- Each vignette door's own bleed and sound in the halls.
- Put by for later: a ball, and a game of catch with Callum; taking Tilly round the house by the arm.
