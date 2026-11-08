# Texture pass, release 4: Proofrock's streets and houses (0.4.377)

The plan (docs/TEXTURE_PASS_PLAN.md) put Proofrock first among the interiors. After the halls, it is the largest vanilla surface in
the House: a whole town of grey concrete roads, stone-brick sidewalks, and houses and shops in planks and terracotta. This release
gives the town its own streets and walls. Nothing changes shape: every street, yard and floor is the same height as before, and
every hiding place, gap and refuge is where it was.

## What changes

| Was | Becomes |
|---|---|
| Grey concrete on Main Street, School Street, Lake Street, Beach Road, the garage forecourt and the motel lot | Asphalt: fine dark grain with the aggregate showing through as single stones. It is laid in four turns, so a long street does not repeat. About one block in eleven (one in five on Beach Road) is a patch of newer asphalt, its edges not quite square, sealed round with a thin dark band |
| The yellow concrete dashes down the middle of each street | A painted centre line on the asphalt, worn through in places. It runs down Main Street and along School and Lake Streets: the block turns with the street |
| Stone-brick sidewalks, and the cracked stone bricks among them | Poured concrete slabs, one to a block, with a broom finish and a tooled joint. About one in six is cracked, in one of four ways picked by where it lies: a crack wandering from corner to corner with a short branch; a corner broken off and sunk; a crack straight across with weeds come up through it; a slab heaved by a root, its far half darker where it tilts away |
| Polished andesite kerbs on Main Street | Granite kerbstones, speckled, their long edges worn |
| Planks and terracotta on the Pine Lane houses and the two houses by the green | Lap clapboard, four boards to a block, each board lit along its top and shadowed under its lip, a nail at each end. It comes in three paints: a fresh pale coat; a second colour, now and then missing a flake to show the board under it; and a faded coat gone from the boards in long runs, in three patterns of wear picked by where the board stands |
| The general store, the laundry and the town hall's terracotta | The same clapboard: the general store weathered, the laundry in the second colour, the hall freshly painted |
| The motel's terracotta and the garage's concrete walls | Painted concrete block, four courses to a block and staggered, the paint thin enough to show the block's pits |
| The garage's concrete floor | The same concrete slabs as the sidewalks |

The diner, the cinema, the hardware store, the sheriff's office, the post office and the bait shop keep their brick, stone, plank
and cobble fronts. The school, the beach, the pier, the boathouse, the green, the lawns, the trees and the drowned town under the
lake stay as they are. The lamps, cars, signs and dumpsters were already the town's own.

Ten blocks and sixteen textures, drawn in code by `tools/draw_proofrock_art.py` (CI and the quick gate run it with `--check`).

## How

`ProofrockTown` lays the new blocks itself as it builds the town; there is no pass afterwards. `ProofrockSurface` holds the ten
blocks. Each copies the collision and sound of the vanilla block it stands in for (the road and line of grey and yellow concrete,
the sidewalks of stone bricks, the kerb of polished andesite, the clapboard of oak planks, the block of terracotta) and, like the
rest of the House, cannot be broken. The centre line is a pillar block whose axis follows its street.

Nothing in the town's hunt reads these blocks: she still follows a reader along any level floor, the living grass of the lawns,
the green and the school courtyard is still a refuge, and the lake still hides a reader who stays under.

New builds only. A world that already built Proofrock keeps it as it is.

## Tests

- **The Drowned Town sequence case** checks that all of Main Street between its outer sidewalks is the town's own paving, that the
  centre line runs along Main Street and along School Street, that both kerbs are granite, and that every kind of surface is laid
  across the town in quantity: asphalt, line, sidewalk, kerb, each of the three clapboards and the painted block.
- The rest of the Drowned Town cases, the lake ambush cases and the streetlight cases pass unchanged. One exception, which this
  release did not cause: the witch's grass detour case fails on its first tick when it is run on its own. It does the same on the
  commit before this release and on e9b21b30, which passed every job in CI (run 1480). It builds its own ground and does not touch
  the town.
- The architecture cases, including Proofrock's passages and its views, pass unchanged.

Witness 81 / 61 / two kinds / three endings; protocol 163; layout 122.
