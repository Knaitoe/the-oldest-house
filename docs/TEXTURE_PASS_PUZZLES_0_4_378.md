# Texture pass, release 5: the puzzle rooms' shells (0.4.378)

The plan (docs/TEXTURE_PASS_PLAN.md) lists the puzzle rooms next, as one shared set: the lift shaft, the worn stair and the
landing. The newer puzzle rooms (Borrowed Light and the lamp rooms, the drafts, the folding rooms, the fuse board, the header tank,
the tension rooms, the Sphinx) were built with their own materials. The three older ones were still laid in vanilla: the service
lift, the turning landings (the early landing and the deep turning stair) and the twin lifts. This release gives them their own
shells. Nothing moves: no cage, pan, weight, landing, handle, rail, lift or door changes place or behaviour.

## What changes

| Room | Was | Becomes |
|---|---|---|
| Service lift (the pantry) | Calcite walls | Limewashed plaster: soft clouding where the coats lie thicker and a faint brush arc. Picked by position, about one stretch in six has a hairline crack and one a flake gone to the grey wall under it |
| | Spruce plank floor | Scrubbed deal boards, four to a block, the end joints staggered and nailed |
| | Stripped spruce ceiling and counter | Beaded boarding: narrow boards with a bead run down each joint |
| | Stone bricks round the shaft and the cold passage | Shaft brick: dark brick in stretcher bond, sooted darker towards the top where the smoke and the rope's grease went up |
| Turning landings (both) | Stone brick walls | Cistern stone: two courses of uneven blocks, wide dark joints, wet patches, a little lime washed out of a joint |
| | Mossy stone brick floor | Cistern flags with moss grown into the joints, laid in four turns |
| | Calcite above the landing's height | The same limewashed plaster |
| | Dark oak plank landings, bridges, nooks and ceilings | Landing boards: old dark boards, four to a block, nailed at the joints |
| | Dark oak stairs | The landing stair: a stair underneath, its treads the same dark boards rubbed pale in the middle where every foot has gone |
| Twin lifts | Brick walls and shafts | Lobby brick: red brick in stretcher bond with pale mortar |
| | Spruce plank floor | The scrubbed deal boards |
| | Dark oak ceiling | The landing boards |
| | Stone brick underfloor | The shaft brick |

The worn boards where a landing's gap meets a ledge, the scraped arcs, the posts, rails, cords, handles, lanterns and water were
already the rooms' own or stay as they are.

Nine blocks and eleven textures, drawn in code by `tools/draw_puzzle_shell_art.py` (CI and the quick gate run it with `--check`).
The stair's block state is written in full, every facing, half and shape, as vanilla's stairs are.

## How

The builders lay the new blocks themselves (`ServiceLift.build`, `TurningLandings.buildEarly` and `buildDeep`, `TwinLift.build`,
and the landings' `lay` and `fences`); there is no pass afterwards. `PuzzleRegistry.shell(name)` gives each surface, and
`PuzzleRegistry.stair(facing)` the landing stair. Each copies the collision and sound of the vanilla block it stands in for and
cannot be broken.

New builds only. A world that already built these rooms keeps them as they are.

## Tests

- **The service lift, turning landing, turning stair and twin lift cases** each now check, right after the room is built, that
  more than 200 faces of the room show its own shell and none shows the vanilla blocks it was laid in.
- The puzzles themselves pass unchanged: the weights still raise the cage, the landing still comes round, the stair still needs
  both handles, the lifts are still ridden in turn, and both the Borrowed Light and lamp room cases and the pacing case still pass.

Witness 81 / 61 / two kinds / three endings; protocol 163; layout 122.
