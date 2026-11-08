# Texture pass, release 2b: the quiet and threshold halls (0.4.375)

The plan (docs/TEXTURE_PASS_PLAN.md) found five halls built of bare vanilla stone and paint: the last sconce, the watched
threshold, the mirror hall, the blind stretch and the quiet hall. They are the halls that test a reader rather than lead them on.
Their floors were smooth stone; their walls were grey or white terracotta, grey concrete, or dark oak. Release 2 gave the stone halls
their own stone (docs/TEXTURE_PASS_STONE_0_4_374.md). This release gives these five the look of somewhere kept: linoleum, paint and
a dado.

## What changes

| Was | Becomes |
|---|---|
| Smooth stone floors, and the threshold under each doorway | Linoleum, four tiles to a block, each its own tone and flecked, laid in four turns so a long floor does not repeat. Down the middle of the hall it is worn: paler towards the middle of each tile, the flecks rubbed faint, a scuff here and there |
| The foot of every wall, beside a floor someone stands on | A dado: a stained rail along the top, darker paint below it that takes the knocks, and a rubber cove where it meets the floor |
| The wall above | Painted plaster: flat paint over plaster, a roller's faint stripes. About one stretch in nine is marked, with a scuff where something was carried past and a flake of paint gone to show the darker coat under it |
| The shell over a room | A ceiling of fibre tiles in a light metal grid, four to a block, pitted with the tiles' short pressed marks |
| The quiet hall's dark oak | Dark stained panelling, a frame round a sunk panel, with a skirting board at the foot of the wall |

The last sconce's dark column, the mirror hall's mirror wall and stair treads, the quiet hall's sculk boards and sensors, every
lamp, sconce and hazard block stay as they are.

Eight blocks, eight textures, drawn in code by `tools/draw_paint_hall_art.py` (CI and the quick gate run it with `--check`).

## How

`PaintFinish` is the last thing done to these halls as each is built (`HazardMaps.build` after its doors, for four of them;
`BlindStretch.build` after its doors). Like `HallFinish` and `StoneFinish`, it swaps a surface only from the vanilla block it was
laid in, one for one. Each new block copies the collision and sound of the block it replaces (smooth stone, terracotta or dark oak)
and is as unbreakable as the halls already were. Nothing moves: no shape, door, lamp, mirror, sensor, hazard block or gameplay
coordinate changes.

- **Floors first.** Smooth stone is a floor when no wall stands on it and the hall's own roof is above it. A block of the shell with
  room to stand over it, such as the threshold under a doorway, is floor too.
- **Walls next.** A block of the shell is a wall when a room of the hall stands beside it. Its height above that room's floor picks
  what it wears: the dado (or skirted panelling) at the foot, plaster (or panelling) above.
- **Ceilings last.** A block of the shell with no room beside it and a room under it is ceiling. So the block over a doorway is part
  of the wall, not a ceiling tile on the wall's face.
- **The blind stretch.** Its turns are carved out of the slot's own white terracotta fill, so the finish takes that fill as its shell
  as well as the grey concrete of its first room. It is dark by design, and the reader seldom sees any of it.

Every read and write goes through `BuildBlocks`, and every choice comes from coordinates relative to the hall, so a paced build is
the same as a direct one.

### The watched threshold's door

The watched threshold's one real door moves along the wall while nobody watches. It used to look for terracotta to move into and
leave terracotta behind. It now moves into the painted wall as well, and leaves behind the same dado and plaster the finish would have
laid there (`PaintFinish.wallAt`), so a door that has moved leaves no patch of old wall.

New builds only. A world that already carved these halls keeps them as they are.

## Not in this release

- The great staircase: release 3.
- The hazard rooms with their own skins already (the cold store's lockers, the listing hall's ship plate, the maze's panels and the
  rest) keep them.
- The halls' working doors stay vanilla.

## Tests

- **`HazardVarietyTests.quietAndThresholdHallsWearTheirOwnPaint`** (new) builds all five halls and counts, for each:
  - linoleum, worn down the middle (the quiet hall's middle is its sculk boards, so it has linoleum only at the sides);
  - more than 10 wall feet and more than 30 painted or panelled wall faces;
  - more than 20 ceiling tiles;
  - no vanilla shell or smooth stone showing on any face of any room in the hall.

  It then moves the watched threshold's door once and checks that it leaves the dado and the plaster behind it.
- The watched threshold, last sconce, mirror, quiet hall and blind stretch cases pass unchanged.
- The client check renders every architecture view as before.

Witness 81 / 61 / two kinds / three endings; protocol 163; layout 122.
