# Borrowed Light (0.4.199): the first puzzle room

The owner: "Borrowed Light would be my first choice to prototype." This is the prototype the puzzle-room notes asked for: one room, built whole, with the rules those notes set.

## The room

`BORROWED_LIGHT`, gray slot 120 (column 15 of the slot grid, which grows to sixteen columns; no earlier slot moves). A dim dressing room fourteen blocks wide and twenty-two deep, walled in plum paper above a dark dado, plank floor, plaster ceiling, two sconces by the entry and one along the west wall. Entry at z 0 (RETURN); the far door at (0,0,-22) is DEALT and is an iron door with no handle on this side.

Before anything is touched, everything the reader needs is in view:

- **The lamp** on the east wall at (6,1,-16), lit (light 7) behind a shut **shutter** at (5,1,-16) with a brass catch.
- **The mirror**, a walnut dressing mirror standing on a **track** of scraped boards along z -16 from x -5 to 4. Three stops are scraped deeper, at x -3, 0 and 3. The mirror starts at the middle stop with its glass facing the far wall.
- **The sun**, a brass plate over the far wall beside the door, at (±3,1,-21). Which side it is on comes from the world's seed.

## What the reader does

- **Lift the catch** (right-click the shutter): it folds back and latches open. The beam comes out across the room at head height and stops at the mirror's edge.
- **Turn the glass** (right-click the mirror above its base): an eighth of a turn clockwise. The beam follows the glass.
- **Kick the base** (right-click the mirror low, below 0.45 of a block): it slides along the track, away from the reader, to the next stop. At the end of the track it stays and says so; something in its way stops it.
- **Look** at the lamp, the sun or the track: a line describes what is there and what state it is in.

The beam reflects across the glass (d − 2(d·n)n/|n|², on the grid's eight directions); the mirror's back or edge stops it, as does anything solid. The air it crosses is lit (level 9 light blocks, at most 64), and the lamp's block entity carries the path so the client draws it: two warm translucent shafts, unlit by the room, dust drifting along them (`BorrowedLightRenderer`).

Laid on the sun, the plate lights (11), the room's state records `Solved`, the iron door opens with a resonant note, and those inside read "The sun over the door warms. The door gives." The solution is the mirror at the stop under the sun with its glass turned once (north-east or north-west); turning it at the middle stop sends the beam to the door beside the sun instead, which is the room showing where the light needs to go.

## The rules it keeps

- Every action answers at once, in sound and light. Nothing counts down, hurts, or is used up; nothing is carried in or out.
- The room is shared and keeps how it was left. Solved, it stays solved: through reloads, and through a rebuild, which lays the shutter open and the mirror at the solution so the beam is still on the sun.
- The far door refuses before the sun is lit ("The door has no handle on this side. The sun beside it is cold.") and is checked before any deal, so nothing reroutes past the puzzle. The way back is always open.
- It is not a story: no Witness source, no account. Witness stays 55 / 42 / two kinds / three endings.
- The House's general passes leave it alone (`ScenePolish.applies` excludes it), so no lamp, rug or furniture is set into the beam's path.

## Pacing: story, then wandering, then the puzzle

`BorrowedLight.weight` sets the reader's weight for the room among the gray halls (`LabyrinthDealer.grayWeight(data, player, place, depth)`); by depth alone it is 0.

- Never in the first three crossings, never at stone depth.
- Unsolved: 40 once the reader has resolved more stories than when they last saw it and is three crossings deeper than where they saw it; otherwise 0. A first visit therefore follows a resolved story and a few halls.
- Solved, after the reader has seen it: 1, only six or more crossings past where they last saw it, an occasional pleasant room on the way.
- `BorrowedLight.arrived` records the reader's visits, stories and depth on arrival.

## Build and assets

- Layout 54 (`LabyrinthBuilder.VERSION`): it stands with the halls (required depth 0), so it is ready before anyone can be offered it; saved worlds build the slot once (`builtVersion < 54`).
- Protocol 103: new blocks `borrowed_light` (kind × facing × turned × open) and `borrowed_light_wall/floor/ceiling`, block entity `borrowed_light_beam`.
- Textures, models and block states are drawn by `tools/draw_borrowed_light.py` (`--check` verifies them): fifteen 16-pixel textures and seventeen models. No existing PNG changes.
- Prose name: "a dressing room with a lamp and a mirror".

## Tests and proofs

- `ExplorationPassTests.borrowedLightIsSolvedByLayingTheLampsBeamOnTheSun`: the room as first seen, the native shutter use, the beam stopping at the mirror's edge and lighting the air, the turn, the kick to the stop under the sun, the lit sun and open door, the far door's refusal before and passage after, the end of the track, and the solved room kept through a reload and a rebuild, with nothing hurt, used or counted.
- `ExplorationPassTests.borrowedLightComesAfterAStoryAndSomeWandering`: the pacing above.
- `HazardVarietyTests` and `LabyrinthTests` count the new slot and the opening gray pool (weight 0 at depth 0).
- The client art proof (`-PdareSmoke`) adds a fifth view, `native-dare-borrowed-light.png`: every working part in each state, the three materials, and the beam turned by the mirror onto the lit sun. CI requires it.
