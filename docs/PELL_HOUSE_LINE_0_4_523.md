# The Pell house 3: the line (0.4.523)

The owner, 12 November: "start by finishing each unfinished vignette". The Pell house (`docs/PELL_HOUSE_PROPOSAL.md`) has its
street and lot (`docs/PELL_HOUSE_STREET_0_4_518.md`) and its rooms (`docs/PELL_HOUSE_THINGS_0_4_522.md`). This is its third slice:
the line, from the proposal's "The line". Nothing happens on it yet, and the House still does not deal the house.

## What is there

A line of paint a hand wide, round the whole of June's lot, unbroken. From the front step it runs west along the front of the
house, through the bed, and turns up the side passage, under the side gate, which has been planed at the bottom to let it through.
It crosses the patio's pavers, goes over the pool fence's footing into the pool's yard, across the deck where the chair stands, and
turns along the back fence over the yard's pavers. It goes over the footing of the pool fence's other run, along the back of the
shed, where it is at its thinnest, and behind the garage, and turns down the east side between the drive and the fence, straight
through the kennel. It turns along the front again: across the drive, where the tyres' marks on it have been painted over, through
the other bed, and across the path just short of the step, under the mat.

How the paint has taken depends on what it is on, and each is drawn as it is:

- the path's concrete: soaked in, its oil wicked out into the concrete beside it and darkening it;
- the drive, where the wheels cross it: a newer patch over their marks, the tread still showing through, and their grime beside it;
- the pavers: beaded where it would not take, and run into the joins;
- the lawn: sitting on the blades, the grass dead under it and beside it;
- the beds' mulch: pooled, over a first coat that sank in;
- the deck: flaking along the grain, broken at every gap between the boards.

Older coats show at its chipped edges, in other tones and widths. In the kennel's opening it runs straight across the floor.
Under the mat, lifted, it runs straight on.

A hand on the line gets what it is painted on (`PellTexts.line`). A hand on the mat lifts its near edge and folds it back toward
the door (`PellTexts.MAT_LIFTED`), and puts it down again. Once a reader's account is ready, the mat is the account's, as before.

Every age of the line is drawn now, on every ground: as on the first visit; the second visit, painted again; the last night, wet in
places; the last night with a footprint smeared through it; scrubbed half away; and scrubbed out, which leaves its ghost (dead grass
on the lawn, a paler strip on the concrete, paint in the pavers' joins and the deck's gaps, churned mulch). This slice lays it as on
the first visit. Later slices age it, wet it and let it be scrubbed.

## How it is made

- `PellLine` (`pell_line`, `PellRegistry.LINE`) is the ground block itself with the line painted on its top. It is not a layer laid
  over the ground, so the gate, the mat, the fence and the kennel stand on the line as they would on paint, and the line goes under
  them. SHAPE is straight, corner or thin; SURFACE is concrete, tyre (the drive's concrete where the wheels ran), paver, lawn, mulch
  or deck; STAGE is one of the six ages; FACING is the way it runs. It sounds like the ground it is: grass, boards or stone.
- `PellSite.line` lays it along `PellSite.route()` (the runs `LINE_W`, `LINE_E`, `LINE_S` and `LINE_N`), and reads the ground under
  each cell for its surface. `WHEELS` are the drive's two wheel cells; `PellSite.thin` is behind the shed.
- The ground it crosses:
  - the front beds are two rows deep now: the line runs along the row by the wall, and the stakes stand in the other;
  - the pool's yard has a deck along the pool's west side (`DECK_*`), where the chair stands;
  - the pool fence has a concrete footing under both its runs.
  `PellSite.poolYard` lays all of this once, so covering and uncovering the pool (`PellSite.pool`) no longer re-lays the yard.
- The kennel's floor inside its opening has the line across it (`kennel_floor`, `tools/draw_pell_things.py`). The mat has a lifted
  stage (`doormat_1`, `tools/draw_pell_house.py`).
- `tools/draw_pell_line.py` (with `--check`, run by CI) draws every cell at sixteen pixels to the block.
  - Each cell is drawn as it lies and never turned, so the ground's joints, joins and boards stay true with the plain ground
    beside it: a straight cell north to south or east to west, a corner by the two sides it joins, and the thin line on the lawn.
    The paint is drawn in one orientation and turned into place over the ground.
  - It reads PellLine's shapes, surfaces and stages from the game and fails if they disagree. Its two folders hold nothing it does
    not draw.

## Decided here

The proposal says what the line crosses, but not exactly where it runs. These are mine, and any of them can be moved:

- **The front run is at the step**, along the front of the house and through the beds. It crosses the path just short of the step
  and under the mat, as the proposal has it. The letterbox and the front lawn are outside it.
- **The west run goes up the middle of the side passage**, where the gate is, so it can go under the gate. The east run is just
  inside the east fence, and the back run just inside the back fence.
- **The line is painted into the ground, not laid on it** (see above).
- **A deck.** The proposal's line flakes off deck boards, and the lot had no deck on its route. The pool's yard now has a timber
  deck along its west side, where the chair stands.
- **A footing.** The pool fence stands on a strip of concrete along both runs, so the line goes over it.
- **The tyre marks are on the paint.** Where the wheels cross the line, the dirt they left on it has been painted over with a newer
  patch, the tread still showing through. This is on the drive's two cells where the wheels ran.
- **The thinnest point is behind the shed** (the six cells along its back): barely a finger wide, with the grass showing through.
- **Moved.** The stakes are one row out from the wall, now that the bed is two rows deep. The bowl has moved from beside the kennel
  to its door, on the edge of the drive and off the line.

## Found in the eye-level frames, and fixed

- The line on the concrete was pale on pale and hard to make out at a few steps. Its coat takes less of the concrete's grey now, and
  the darker edge where it soaked in sets it off.
- The windows' clear glass was drawn as a faint tint under the cutout render type, which draws any pixel it keeps at full strength:
  the clear windows and the sliding door were solid panes. Clear glass is translucent now (`tools/draw_pell_house.py`), so the
  garage's window can be looked through, as a later slice needs.

## The case

`PellHouseTests.theLineGoesRoundTheWholeLotUnbrokenAndTakesAsItsGroundDoes` checks:

- from the step, each cell of the line runs out to the next and the last back to the first, once each, the whole way round, and it
  turns at the four corners;
- what it is on: the path's concrete at the step; the drive's wheel cells and its plain concrete; the beds; the lawn at the front,
  up the side passage, down the east side and behind the garage; the patio's and the pool yard's pavers; the pool fence's footing,
  twice; the deck;
- it is thin behind the shed, and only there;
- nothing stands on it but the mat, the side gate, the pool fence at its two crossings and the kennel;
- a body walks onto it from the street: at the step, down the east side and behind the shed;
- a hand on it gets its own line for the lawn, the thin line, the drive and the deck; a hand on the mat lifts it, with the line
  under it, and puts it back;
- uncovering the pool leaves the line, the deck and the pavers as they were.

The rooms' case now looks for the stakes and the bowl in their new places.

## Not yet

The ages of the line in play (the second visit, the last night, the footprint); June painting it at night, seen from the garage's
window; Callum on it; scrubbing it with the laundry's brush and bucket, and the gap; Tilly feeling the cold at it; the people; the
visits; the tapes; the endings; the Witness source.

## Numbers

No layout change. No Witness change: the place is no source until it can be finished. New: one block (`pell_line`, 432 states),
228 textures and 228 models for it, and two more textures (the kennel's floor, the mat's underside) and one more model (the lifted
mat).
