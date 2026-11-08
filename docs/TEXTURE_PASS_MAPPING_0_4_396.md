# Texture pass, release 10: Mapping the Interior (0.4.396)

The plan ([TEXTURE_PASS_PLAN.md](TEXTURE_PASS_PLAN.md)) lists Mapping the Interior next. The owner asked to go "back to the textures" after the road rules (November 8). This release was drafted after the trailer and parked until now.

The brothers' house is built whole (0.4.201). Its siding, its furniture and its fittings were already its own; the rest was vanilla:

- partitions of spruce planks;
- a white concrete ceiling;
- white terracotta for the kitchen and utility floors and for the skirt;
- carpets of three colours of wool;
- a roof of deepslate tiles and slabs;
- under it, a tunnel floored with packed mud and a cistern of mud brick.

Its outer walls were the siding on both faces, so from inside every room showed the outside of the house.

This release gives the house the rest of its surfaces. Nothing moves:

- every wall, door, window, bed and piece of furniture stays where it was;
- so do the loose skirt panel, the plywood door, the shaft and its lid;
- the nights run as before.

## What changes

| Part | Was | Becomes |
|---|---|---|
| Outer walls | The siding on both faces | The siding outside, the house's wood panelling inside |
| The floor's edge between the skirt and the walls | Spruce planks | The siding, all but the front door's sill |
| Partitions | Spruce planks | Prefinished wood panelling: grooves at uneven spacing, a printed grain, a knot here and there |
| Ceiling | White concrete | A popcorn ceiling |
| Front room and hall floor | Brown wool | Front room carpet |
| Boys' room floor | Blue wool | The boys' room carpet |
| Mother's room floor | Light grey wool | Bedroom carpet |
| Kitchen and utility floors | White terracotta | Kitchen linoleum printed as tiles, every other one a shade darker, worn pale in the middle |
| Skirt round the base | White terracotta | Ribbed vinyl skirting, mud splashed up from the yard |
| Roof | Deepslate tiles and slabs | Asphalt shingles; the eaves' slabs show plywood with a strip of vents underneath |
| Tunnel floor | Packed mud | Trodden earth, two long smears where knees have gone |
| Cistern | Mud bricks | Small old brick, wet, with a green bloom along the bottom course |

These stay as they are:

- the loose skirt panel, which stays a birch trapdoor;
- the porch and back steps, which stay spruce;
- the spruce sills under the inner doors;
- the crawl's slope, which stays mud brick slabs;
- the lot's dirt, grass and gravel;
- the neighbour's dog run, the propane tank and the tetherball pole.

Twelve blocks and twelve textures are drawn in code by `tools/draw_mapping_house_art.py`. CI and the quick gate run it with `--check`. The outer walls reuse the siding's own texture outside.

## How

- `MappingHouse.build` lays the new blocks itself; there is no pass afterwards.
- The outer wall is `RoomShell.Skin`, the block the trailer's walls use, laid facing out. The corners belong to the long walls, whose side faces are the siding too.
- `RoomShell` holds the blocks, and `RoomShell.mapping(state)` tests the house's surfaces.
- Each block copies the collision and sound of the block it stands in for, and cannot be broken.

New builds only. A world that already built the house keeps it as it is.

## Tests

New case: `LiteraryTests.theBrothersHouseStandsInItsOwnSurfaces`. Right after the house is built, it checks that:

- every outer wall block faces out, and the walls are otherwise only glass and doors;
- the partitions are panelled;
- the ceiling is the house's own over every room;
- each room has its own floor, and the skirt is vinyl;
- the roof is shingled end to end;
- the tunnel and the cistern are their own;
- no white concrete, white terracotta, wool, deepslate tile or two-faced siding is left in the house, and no packed mud or mud brick under the yard.

The Mapping nights case passes unchanged.

## What stays

- **Witness:** 81 / 61 / two kinds / three endings.
- **Protocol:** 163.
- **Layout:** 130.
