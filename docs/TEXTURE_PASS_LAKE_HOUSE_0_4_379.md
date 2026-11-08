# Texture pass, release 6: the lake house's outside (0.4.379)

The plan (docs/TEXTURE_PASS_PLAN.md) lists the lake house next. Its rooms already had their own surfaces (the floor, the wall, the
cedar of the sauna and the pool's stone), so this release dresses what was still vanilla round them: the drowned house's walls and
chimney, its foundation, the boathouse the reader comes in by, the dock, the culvert under the drive and the drive itself. Nothing
moves: every wall, floor, door, channel and ceiling is where it was and the same height, and the canoe, the dive and Tess's route
pass exactly as before.

## What changes

| Was | Becomes |
|---|---|
| Brick on the drowned house's walls, gables and chimney | Drowned brick: four courses a block in stretcher bond, gone dark under the lake, silt lying in the joints and a green film over the top of each brick. Picked by position, about one block in three is further gone, the film thicker and one brick's face spalled away |
| Stone bricks under the house | Silted stone: two courses of grey stone under a blanket of silt, thickest along the top of each course |
| Oak and spruce planks where the house's floors run under its walls, and across the eaves | The house's own floor, which the rooms already used inside |
| Spruce planks on the boathouse's walls and gables | Weathered siding: wide boards gone silver with a narrow batten over each joint, a nail head or two |
| Spruce planks on the boathouse floor, its counters and the dock | Dock boards: four bleached boards a block with a dark gap between them and a nail at each end, laid in two turns |
| Light grey concrete on the culvert's walls and ceiling | Culvert concrete: board-formed, the shuttering's lines across it and two tie holes |
| Cyan terracotta along the culvert's waterline | The same concrete, dark and green where the water stands, darkest at the foot, with slime run down it |
| Grey concrete on the drive | Proofrock's asphalt (0.4.377) |
| Spruce planks lining the arrival vestibule | The siding and the dock boards |

The rooms inside, their fittings, the raft, the compressor, the lamps, the woods, the banks and the water stay as they are.

Six blocks and seven textures, drawn in code by `tools/draw_lake_house_exterior_art.py` (CI and the quick gate run it with
`--check`).

## How

`LakeHouse` lays the new blocks itself as it builds the lake (and the empty lake, which shares the land, culvert, boathouse and
dock); there is no pass afterwards. `LakeHouseRegistry` holds the six blocks. Each copies the collision and sound of the vanilla
block it stands in for (the brick of bricks, the silted stone of stone bricks, the siding and dock of spruce planks, both culvert
concretes of light grey concrete) and, like the rest of the House, cannot be broken.

New builds only. A world that already built the lake house keeps it as it is.

## Tests

- **The boathouse, culvert and house case** now also checks the dock's boards, the boathouse's siding and the culvert's concrete
  stained at the waterline, that every one of the six surfaces is laid round the lake in quantity, that the drive is asphalt, and
  that no vanilla brick or concrete is left anywhere in the lake house's bounds. Its brick checks look for the drowned brick.
- The rest of the lake house cases (the canoes, the culvert's strokes, the dive, the helmet and hose, the flashlight, the account,
  the pairs and Tess, the dry chair, the half door, mining and the earned gear) pass unchanged.

Witness 81 / 61 / two kinds / three endings; protocol 163; layout 122.
