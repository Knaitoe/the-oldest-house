# The waxworks 1: the museum (0.4.749)

The first part of the waxworks, the first of the two vignettes the owner asked for on 23 November ("House of wax vignette ... wax models slowly coming to life ... FNF style central security room with camera display for various rooms, watching for irregularities"). The design is `docs/WAXWORKS_AND_COLD_ROOM_0_4_749.md`, section 1.

This part is the place alone: Merriman's Wax Museum after closing, its staff corridor, the security office, the ring corridor, the five galleries and the twenty-four figures on their marks. Nothing moves yet. The House does not deal the place until part 4, when it has its account, and it is no Witness source until its account.

## The parts

1. **The museum** (this release, 0.4.749): the place and its figures.
2. **The flashlight and the figures** (0.4.750, `docs/WAXWORKS_0_4_750.md`): the locker's flashlight, the look at the edge of the view, the blink, the mirror.
3. **The shift** (0.4.751, `docs/WAXWORKS_0_4_751.md`; the monitors' feeds follow): the shared clock from 11:52 to six, the rounds and the log, the monitors on the CCTV renderer, irregularities, failure and success.
4. **The account**, dealt from here: the log in the reader's hand, the Witness story, the stair's company and the shared accounts.

## The place

All positions are relative to the House's door, at z 0 facing north. Everything is brick behind its own surfaces, from x -17 to 17 and z 0 to -75.

- **The staff corridor** (x -1 to 1, z 0 to -9): painted block over linoleum under ceiling tile. The time clock and the typed notice to nights by it on the west wall, two lockers on the east.
- **The security office** (x -5 to 5, z -11 to -17): the desk of eight along the north wall, six monitors on it (on, faintly lit), the shift log open and the telephone, two swivel chairs, and the clock over the far door at eight minutes to midnight. The way on is in its west wall.
- **The ring corridor** (x -16 to 16, z -19 to -61, three wide): the public side, panelling to shoulder height, paper above, the runner underfoot.
- **The galleries**, parquet under plaster, each through a doorway three wide in the ring and each with a camera up in a corner:
  - **The foyer** (north of the ring): the front doors in glass and bronze under MERRIMAN'S, chained from outside; the ticket booth.
  - **The Hall of Statesmen**: seven figures on plinths over their plaques, and the twenty-fourth in its corner.
  - **The kitchen**: a family at supper behind a rope, at a table laid with wax food, a dresser and a cold stove.
  - **The chamber**: a crypt behind a rope, a coffin with its lid off and someone sitting up in it, stones, and four more.
  - **The workshop**: two vats, the modeller's bench in three lengths, the shelves of heads, and two figures on plinths, one half made and one under a sheet.
- **The dark.** No light in any gallery. The monitors and the cameras' small lamps are all there is until part 2's flashlight.

The ropes run the full depth of their rooms, fence-high; a reader cannot walk or step up past them, and the plaques stand a block clear of them so none is a step.

## The figures

Twenty-four LiteraryActors on stranger phases 290 to 313, each painted as wax (`literary_wax_*`, the painter's `museum` face: clear eyes, a glint, a gloss). Each stands on its mark in its pose, turned the way its gallery shows it. A figure never turns to whoever touches it, never blinks and never speaks; a hand on it is told what it is (cold, a little tacky, real cloth over wax). Each is made only where its gallery's entities are loaded, and only when it is not already there.

Twenty-three have plaques. The notice counts twenty-three. The twenty-fourth stands in the Hall of Statesmen's corner with no plinth and no plaque.

## More than one reader

The museum is one place for a party of four (`SceneInstances`, party `Waxworks.PARTY`). The figures are made once for everyone; what each reader touches is on their own record. A spectator reads nothing and is not on the shift.

## Tests

`WaxworksTests` (namespace `waxworks`, its own CI job):

- `theMuseumItsGalleriesAndItsFigures`: every material, fitting, monitor, camera, plinth and plaque; a walk from the House's door reaches every room and not behind the ropes; no light in the galleries; one of each figure on its mark in its pose, turned to nobody, still with a reader among them; every fitting and figure answers a hand; the notice's count is the museum's own; the account title fits; not dealt.
- `twoReadersAndASpectator`: two readers in, still one of each figure; one reader's touch is theirs alone; a spectator reads nothing.

The multiplayer sweep (`GroupSweepTests#waxworks`), the cast checks (`CastConductTests#waxworks`) and the two-reader checks (`TogetherConductTests#waxworks`) include it. `ArchitectureTests` checks its source surface and that it lays no rug of the House's.

The build is worked out from the plan, never read back from the world: the walls' surfaces are painted where the plan says a room meets brick, so a recorded or sliced build lays them the same as a direct one.

## New text

Every line in `WaxTexts` is new in this release, for the owner's review: the notice to nights, the twenty-three plaques, what a hand finds on each figure and fitting, and the placeholder account.
