# Texture pass, release 7: the child's room and the hotel's boiler room (0.4.380)

The plan (docs/TEXTURE_PASS_PLAN.md) lists the child's room and the hotel basement next. Both are built whole. The child's room's
furniture, toys and wallpaper were already its own (0.4.74), but its floor, panelling, rail, ceiling and cellar were vanilla. The
boiler room's boiler, gauge, valve and switch were the hotel's own, but its walls, floor, ceiling and stair were vanilla. This
release gives both rooms their own surfaces. Nothing moves: every wall, floor, opening, exit, window, ladder and hatch is where it
was and the same height. The sequence, the sealing, the crawl and the boiler work as before.

## What changes

| Room | Was | Becomes |
|---|---|---|
| Child's room | Birch planks on the floor | Honey floorboards, four to a block, the end joints staggered and a nail by each, in two turns. The board under the bed that lifts is one of them |
| | Birch planks to the rail | Cream beadboard |
| | Stripped birch along the rail | A painted moulding, lit along its round and shadowed under its lip |
| | Smooth quartz ceiling | Plaster. About one stretch in four has three or four of the pale stars a child sticks up there |
| | Stripped birch logs at the corners | Painted corner posts |
| | Brick in the cellar | Old brick, whitewashed long ago, the wash still whole on some bricks and the joints, flaked off others |
| | Polished andesite cellar floor | Dusty concrete with a crack across it, in two turns |
| Boiler room | Brick walls | Brick blackened in patches by the boiler's smoke |
| | Smooth stone floor | Worn concrete. About two stretches in nine have an oil stain and one a drain grate |
| | Spruce plank ceiling | The joists of the porch floor overhead. From above, where it is the porch's floor, it is plain spruce as before |
| | Stone brick stair and its fill | The service stair's own stone, the treads worn pale in the middle |

The window sills, the doors, the crawlspace's earth, the night behind the windows, the furnace, the coal, the pipes and every toy
and fitting stay as they are. Sealed exits still become the room's own wall: panelling to the rail, the rail, then the paper.

Twelve blocks and fifteen textures, drawn in code by `tools/draw_room_shell_art.py` (CI and the quick gate run it with `--check`).
The stair's block state is written in full, every facing, half and shape.

## How

`ChildRoom.build` and `HotelRooms.basement` lay the new blocks themselves; there is no pass afterwards. `RoomShell` holds the twelve
blocks, `RoomShell.nurseryWall(y)` gives the child's room's wall at a height (and `ChildRoom.wallAt` uses it to seal an exit). Each copies the collision and sound of the vanilla block it stands in for and cannot be broken.
Looking under the bed still finds the lifting board: it is the room's own floorboard now.

New builds only. A world that already built these rooms keeps them as they are.

## Tests

- **The child's room sequence case** checks, right after the room is built, that more than 400 faces of the bedroom, crawlspace and
  cellar are the room's own and none shows the vanilla it was laid in; that the board under the bed is the room's floorboard; and
  that a sealed window becomes exactly the room's own wall at its height.
- **The hotel's routes case** checks that more than 200 faces round the boiler room and its stair are the room's own and none is
  vanilla brick, smooth stone or stone brick, that the stair is the worn stone and that the porch floor stands on the joists.
- The rest of the child's room cases (the toys, the late readers, the finished room) and the hotel's boiler and furnishings cases pass
  unchanged.

Witness 81 / 61 / two kinds / three endings; protocol 163; layout 122.
