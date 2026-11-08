# Texture pass, release 9: Goatman's trailer (0.4.382)

The plan (docs/TEXTURE_PASS_PLAN.md) lists Goatman's trailer siding next. The camper's skin was one block thick and vanilla on both
sides: white and cyan terracotta with a light grey concrete stripe, iron blocks at the corners and along the top of the long sides,
smooth stone slabs for the roof, and a floor of white and light grey concrete laid as a checker. From inside, the walls were the
same terracotta as outside. This release gives the trailer its own skin, outside and in. Nothing moves: every window, frame, sill,
door, porch post, fixture, chair, bunk and the table are where they were, and the night runs as before.

## What changes

| Part | Was | Becomes |
|---|---|---|
| Walls | White terracotta | Ribbed aluminium outside, the camper's wood-veneer panelling inside. About one stretch in five has a rust streak running down from its rivets |
| | Light grey concrete course | The trailer's painted stripe, faded and chipped back to metal along the ribs; panelling inside |
| | Cyan terracotta course at the foot | Darker sheet under a drip edge, splashed with mud; inside, the panelling with a skirting board |
| | Iron blocks along the top of the long sides | A rounded drip rail outside; inside, the panelling meeting a pale ceiling cove |
| Corners | Iron blocks | Rounded corner trim, screwed every few pixels |
| Roof | Smooth stone slabs | Coated roof sheet with seams, on top; underneath, the camper's ceiling board between battens, with a water mark here and there. The eaves over the end walls are the same sheet run out a block |
| Floor | White and light grey concrete checker | Linoleum in two tones, laid as the same checker |

Each wall block faces away from the trailer, so its outside is always the aluminium and its inside always the panelling. The window
sills, iron bar frames, porch rails and the bathroom's sill stay as they are; so do the chassis rails, the porch, the door, the
kitchenette, the partitions, the table, the chairs and every fixture.

Eight blocks and fifteen textures, drawn in code by `tools/draw_trailer_art.py` (CI and the quick gate run it with `--check`). The
roof is a slab, its block state written for bottom, top and double.

## How

`SceneExteriors.trailer` lays the skin when it dresses the trailer, as it laid the terracotta and iron before; `SceneShells` lays
the floor with the linoleum; `VignetteArchitecture` runs the roof out over the end walls. Each cell gets its final block at once,
since the exterior pass leaves the House's own blocks alone once they are laid. `RoomShell` holds the blocks beside the other
rooms', `RoomShell.Skin` is the wall block that faces out, and `RoomShell.trailer(state)` tests the trailer's surfaces. Each copies
the collision and sound of the vanilla block it stands in for and cannot be broken.

New builds only. A world that already built the trailer keeps it as it is.

## Tests

- **The architecture case** checks that the trailer's walls, roof and floor are its own (more than 300 wall faces, 600 roof and
  150 floor), that every wall block faces out, and that no white, cyan or light grey terracotta, concrete, iron block or smooth
  stone slab is left in them. Its roof check now finds the trailer's own sheet.
- The Goatman cases (the windows, the finished camper's night, the porch steps) pass unchanged; the sills and frames they check
  were not touched.

Witness 81 / 61 / two kinds / three endings; protocol 163; layout 122.
