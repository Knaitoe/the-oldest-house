# The Pell house 1: the street and the lot (0.4.518)

The owner, 12 November: "start by finishing each unfinished vignette". After Bike night the next in the owner's order is the
Pell house (`docs/PELL_HOUSE_PROPOSAL.md`), which was a proposal and nothing else. This is its first slice: the place, built
whole, with its doors. Nothing happens in it yet and the House does not deal it.

The owner's rule for it: "Since this is a small personal vignette, the house has to have a LOT of attention to detail in its
unique textures and environmental storytelling." So this slice lays down the ground, the walls, the roofs, the fences, the pool
and the doors in the house's own materials, and the next slices fill the rooms.

## What is there

**Wren Close.** Out of the House's door, in a brick substation across the mouth of the street, a road runs north between
curbs and footpaths to a turning circle. Two closed houses stand on each side: brick boxes under tiled roofs, blinds down in
every window, a door nobody answers ("Nobody comes. The blinds are down."), a path and a drive. Every lawn in the street is
dead.

**June Pell's lot** is at the head of the circle, forty blocks wide, and its lawn is green.

- **The front.** A path two slabs wide from the footpath to the step. The letterbox by the path (the first paper is in it:
  the placement notice). A garden bed along the front wall. The mat at the step (where each reader's account will wait).
- **The house.** One storey of brick veneer on a lowest course with weep holes and salt bloom, under a low tiled roof with
  eaves. In the west wall, a window bricked up in newer brick that does not match. Along the front: Nessa's room (a clear
  window onto the street), Callum's room (its window painted over from inside), the bathroom and June's room (blinds down).
  A hall across the middle, its carpet worn in one path from the front door to Nessa's door. Along the back: the kitchen (a
  window onto the pool), the living room (a window where the window seat will be, and a glass sliding door), the laundry (the
  back door).
- **The drive** runs down the east side from the circle, past the house, to the garage.
- **The garage** stands by itself in the back corner: brick, a roller door that is never opened ("It hasn't been up in years.
  The runners are painted over."), a walk door with its bolt on the yard's side and none inside, a stained slab, and one
  window in its west wall.
- **The pool** is the yard's west half behind its own black fence, whose child gate is padlocked ("The gate is padlocked. The
  lock is new and the latch is old."). Coping, pavers, tile down the walls with a darker band at the waterline, shallow at
  the house's end and deep at the far end. It is under its cover, which has sagged in the middle and holds a pond.
  `PellSite.pool(level, base, covered)` uncovers it for the later visits.
- **The shed** is corrugated iron against the back fence, padlocked ("Padlocked.").
- **The strip behind the shed**, between its back and the back fence, is seen from the house by nobody. The garage's window
  looks straight along it.
- **Fences.** Palings down both sides and across the back, too tall to get over, and across the side passage with a gate in
  it.

**Doors** open and shut to a hand, both cells, from either cell: the screen door, Nessa's, Callum's, the bathroom's, the
laundry's, the back door, the sliding door's east half, the garage's walk door, the side gate. June's does not ("June's room.
The door is shut."). The sliding door's west half is fixed ("This half doesn't slide.").

## The first paper

"Notice of placement", G. Mercer. Two children, the elder and a younger half-sister, to the care of Ms June Pell, 9 Wren
Close. It says what June is to the caseworker, that the younger child is partly sighted and that June's own daughter "had the
same condition, and the house is still set up for it", and that the elder child will have the converted garage, "for
privacy". It does not say what the elder child is: the elder child is the reader.

## What was decided here

The proposal describes the house room by room and does not give a plan. These are mine, and any of them can be moved:

- **The line's front run is at the step**, not at the curb: the proposal has it "just short of the step" and "under the
  doormat". So the front garden and the letterbox are outside it. (The line itself is a later slice; the ground is laid for
  it.)
- **The garage is detached, in the back corner**, reached by the drive. The proposal says "the converted garage across the
  yard" and "its door to the house locks from the house side": as built, its only walk door faces the house across the yard
  and its bolt is on the outside.
- **The pool takes the yard's west half**, enclosed by its own fence on two sides and the lot's fences on the other two, so
  that the line, running inside the lot's fence, crosses the pool fence's footing as the proposal says.
- **June has a room**, and its door does not open. The proposal lists no room for her.
- **The neighbours' houses are shells.** Nobody is in them and nothing is in them.
- **The street is named Wren Close and the house is number 9.** Both are new.
- **"Curb"**, as the owner asked of the Corner house.

## What was built

- `LabyrinthPlace.PELL_HOUSE` (three stays; slot 239), `LabyrinthBuilder.VERSION` 137.
- `PellSite`: the plan (its constants are the plan), `build`, `pool`, `leaf`, `doorAt`, and where things are (`inHouse`,
  `inGarage`, `inPoolYard`, `inWater`, `behindShed` and the rest).
- `PellRegistry`: twenty-four materials and the fitting. `PellBlock`: seventeen kinds of fitting (fences, gates, door leaves,
  the roller door, windows in three states, the sliding door, the pool's cover, the letterbox, the mat, the house's outside
  wall and its corners, the eaves).
- `PellHouse`: a hand on a door. `PellTexts`: the notice, the hint, the lines above.
- `tools/draw_pell_house.py`: every texture and model, at sixteen pixels to the block, with `--check`. It reads the game's
  list of materials and kinds and fails if they are not its own.
- The House's tables: the place among the literary places, under the outside sky, with its hour (late afternoon, until the
  house keeps its own), its miniature, its name on the staircase, and a service door in the substation's wall as its way on.

## The case

`PellHouseTests` (its own namespace, `the_oldest_house_pell`, and its own job).
`theStreetAndTheLotAreBuiltOfTheirOwnThingsAndEveryDoorAnswersAsItShould`: the materials are where the plan puts them, and no
cell of the ground anywhere on the site is the game's grass or dirt; every door and gate is the house's own and shut; with
everything shut a body walks from the House's door to the front step and down the drive to the yard, and not into the house,
the pool's yard, the shed or the garage; June's door, the shed's, the pool's gate and the fixed glass do not open and the
reader is told why of each; the rest open, both cells; open, a body walks to every room but June's, to the patio, the side
passage, the garage and the strip behind the shed; the pool is under its cover over water over tile, shallow and deep, with
the band under the coping, and uncovered is full to the brim; nothing stands between the garage's window and the back of the
shed; the letterbox and the mat are in their places; the notice names June and Gail and does not say what the elder child is;
and the House does not deal the place.

## What the frames showed

Eye-level frames of the street, the front, every room, the yard, the pool, the garage and the strip behind the shed were
looked at. Two things were wrong and are changed:

- **The house's outside walls showed brick indoors.** A wall one block thick is the same on both faces. The outside walls are
  now cells of veneer: brick on the face turned to the weather, plaster on the faces turned to the rooms, with two brick
  faces at a corner. The garage is still brick inside, as a garage is.
- **The eaves were a white band a whole block deep**, a third of the height of the wall. They are now the upper half of their
  cell: a painted board on the edge, the soffit under it, tiles on top; and the wall's brick goes up behind them.

The frames have no sky in them and no light but the renderer's own, so they say nothing about how the street looks in its
afternoon.

## Not yet

Everything else in the proposal: what stands in each room and its three states; the line; the kennel; the people; the three
visits; the tapes; being framed; the last night; the endings; a reader as Tilly; the neighbours' side of the fence; the
aftermath; the sounds (doors use the game's own for now); the Witness source. The house keeps the afternoon it was built in
until it has its own hours.

## Numbers

Layout 137: one place added, in the last slot. No Witness change (the place is no source until it can be finished). New
builds only.
