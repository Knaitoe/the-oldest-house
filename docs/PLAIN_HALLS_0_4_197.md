# Four hallways that are just hallways — 0.4.197

The owner, in the second document of October 30: "Try to reduce the frequency of these bleeding rooms. Design more hallways that are just hallways." 0.4.193 cut how often the three halls with bled-in rooms are dealt. This change adds the plain hallways.

## The four halls
All four are built by the plain plaster-hall builder that the 0.4.48 halls use (`HallVariations.buildDomestic`). That gives them spruce floors, white walls with stripped-spruce skirting, ceiling ribs and dark recessed windows. Nothing is bled into them, and they hold no story and no supplies.
- **A hall with a jog in it** (`JOGGED_HALL`, slot 114). Three wide, it runs 27 blocks, steps two to the east and runs on 28 more. A shallow niche on its west side holds a second door, so there is a choice early on.
- **A hall that forks** (`FORKED_HALL`, slot 115). A short stem leads to a crossing passage, and two arms turn north to a door each. A lamp hangs down each arm, so neither way is the dark one.
- **A hall of pillars** (`PILLAR_HALL`, slot 116). Five wide and 45 long, with six stripped-spruce posts down its middle, an aisle on either side and a door off its east side.
- **A hall that zigzags** (`ZIGZAG_HALL`, slot 117). It turns east, runs north, turns back west and runs on. You cannot see the far door from the near one.

## Dealing
- **The approach.** They are in the opening gray pool from the first door (`LabyrinthDealer.grayAvailable`, `LabyrinthBuilder.requiredDepth` 0). They give way to stone at the stone depth, like every domestic hall.
- **Weights before the stone depth.** The jogged hall is 14; the forked, pillar and zigzag halls are 12. Alongside 0.4.193's lower weights for the three bled-in halls, about one approach hall in six now has a room bled into it.
- **Ordinary halls.** They count as ordinary for the pacing (`LabyrinthPacing.ordinary`) and lead onward.
- **Saved routes** keep the halls already dealt to them.

## Saved worlds
Layout 52 appends the four slots (`HallVariations.plain` in the structural set). Saved worlds carve them through the existing bounded append, with no change to any earlier address, door or room. The four fit an ordinary world at the common manor height.

## Literature
The House of Leaves names every hall in prose (`StaircaseProse.place`). These four have names: "a hall with a jog in it", "a hall that forks", "a hall of pillars" and "a hall that zigzags". So do the three 0.4.48 halls, which had none: "a hall with alcoves", "a hall that steps aside" and "a service landing".

## Tests
- **`ExplorationPassTests.fourPlainHallsAreJustHallwaysWithRealRoutes`.** It runs the same checks as the six-hall case for each hall:
  - every door is reachable by a native body;
  - every bay is supported;
  - every lamp has support;
  - there is room for two side by side.
  It also checks that they are plain, with no fragments, are dealt only before the stone depth, are named in prose, and that the pillar hall's posts stand. It exports their four views, so the architecture proof expects 103 earlier views where it expected 99.
- **`HazardVarietyTests`** pins the four new slots at 114 to 117.
- **`LabyrinthTests`** counts thirteen halls in the opening pool where it counted nine.
- Locally the exploration namespace (56 cases) and the labyrinth and domestic labyrinth classes (34) pass.

## What stays
- **Protocol 102**, unchanged from 0.4.196.
- **Witness unchanged:** 55 / 42 / two kinds / three endings.
- **No PNG changes.**
