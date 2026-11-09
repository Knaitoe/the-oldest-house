# Texture pass, release 11: the cabin at the end of the world (0.4.406)

The plan ([TEXTURE_PASS_PLAN.md](TEXTURE_PASS_PLAN.md)) ends with the cabin at the end of the world. The owner asked to go on with the textures once the November 9 notes were done.

The cabin by the lake is a dressed room: `LiteraryRooms.endCabin` frames it, and the scene passes finish it.

- The polish pass and the interiors pass furnish it. The interiors pass panels its walls and partitions in knotty pine and tiles its kitchen.
- The composition pass (`SceneCraft`) gives it its roof, porch rail, shutters, woodpile and jetty.

The rest of what a reader sees was vanilla:

- spruce plank walls, with the pine panelling on both faces, so the outside was the inside;
- a dark oak plank course at the foot of the long walls;
- spruce plank floors;
- a birch plank ceiling with dark oak beams;
- a dark oak slab roof over spruce plank gables;
- a spruce plank porch under spruce slabs;
- a stone brick foundation ring round the walls, showing at the ground and across the porch's first row.

This release gives the cabin the rest of its surfaces. Nothing moves:

- every wall, window, door, bed and piece of furniture stays where it was;
- so do the timber bays, the hearth and its chimney;
- the visitors come at dusk as before.

## What changes

| Part | Was | Becomes |
|---|---|---|
| Outer walls | Spruce planks, then the pine panelling on both faces | Weathered shiplap outside, the same knotty pine within |
| Foot of the walls | Dark oak or spruce planks | Fieldstone outside, a dark skirting within |
| Foundation ring at the ground | Stone bricks | A fieldstone apron, and along the porch its first board of decking |
| Corners | Pine panelling | Hewn corner posts |
| Floor | Spruce planks | Wide pine floorboards, worn paler where feet go |
| Ceiling | Birch planks | Tongue-and-groove boards |
| Ceiling beams | Dark oak planks | Hewn beams |
| Roof | Dark oak slabs | Cedar shakes, mossy in the joints, with eave boards under the edge |
| Gables and eave line | Spruce planks | Board and batten |
| Porch floor and the jetty | Spruce planks | Grey decking |
| Porch roof | Spruce slabs | Cedar shakes |

These stay as they are:

- the kitchen's tile and the hearth's stone, which the interiors pass lays on the planks they expect, so the builder leaves those planks for them;
- the partitions' knotty pine;
- the timber bays and the window frames;
- the brick hearth and chimney;
- the shed by the House's door and the trodden path.

Nine blocks and nine textures are drawn in code by `tools/draw_cabin_art.py`. CI and the quick gate run it with `--check`. The walls' inner face reuses the interiors pass's own pine.

## How

- `LiteraryRooms.cabinSurfaces` runs at the end of `endCabin`:
  - it lays the walls and their foot as `RoomShell.Skin` blocks facing out;
  - it sets the corner posts, the floor, the ceiling and its beams, the porch deck and the porch roof;
  - it lays the foundation ring at the ground as a fieldstone apron, facing out, and decks it along the porch.
- `SceneCraft` roofs the cabin in `RoomShell.CABIN_SHAKE_SLAB` over `RoomShell.CABIN_GABLE`, shakes the porch plate where its old eave cut in, and decks the jetty.
  - It no longer trims the corners; the builder lays them.
- `VignetteInteriors` no longer panels the outer walls; it still panels the partitions and tiles the kitchen.
- `RoomShell.cabin(state)` tests the cabin's surfaces.
- Each block copies the collision and sound of the block it stands in for, and cannot be broken.

New builds only. A world that already built the cabin keeps it as it is.

## Tests

New case: `LiteraryTests.theCabinAtTheEndOfTheWorldStandsInItsOwnSurfaces`.

Right after the cabin is built, it checks that:

- every wall block faces out: fieldstone at the foot, shiplap above;
- the corners are posts, and the walls are otherwise only bays, frames, glass and the door;
- the floor is boarded, but the kitchen and the hearth keep their planks;
- the ceiling is boards between beams;
- the porch is decked, its first row included;
- no stone brick is left in the foundation ring at the ground, which is a fieldstone apron.

After the scene passes have run, it checks that:

- the walls are still the cabin's own;
- the roof is shakes end to end over board-and-batten gables;
- the porch roof is shakes;
- the kitchen has its tile and the partition its pine;
- no dark oak slab, stair, spruce slab, birch plank or plank gable is left in the roof.

The cabin's visitors, refusal, furniture, raised surface and interiors cases pass unchanged.

## What stays

- **Witness:** 81 / 61 / two kinds / three endings.
- **Protocol:** 163.
- **Layout:** 130.
