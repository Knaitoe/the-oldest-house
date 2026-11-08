# Four more halls (0.4.391)

The rest of the first batch of new ordinary halls in [HALLS_TRIALS_AND_TIME_DESIGN.md](HALLS_TRIALS_AND_TIME_DESIGN.md), after the laundry corridor ([LAUNDRY_CORRIDOR_0_4_390.md](LAUNDRY_CORRIDOR_0_4_390.md)). The owner asked for them: "go ahead and implement the four rooms please, then the rules."

All four are gray rooms built whole by `OrdinaryHalls`. They are ordinary ways on: never a trial or a story, with no mark and no Witness source.

| Hall | Slot | Dealt | Ways on |
|---|---|---|---|
| The long bedroom hall | 229 | with the domestic halls, until the stone | 2 |
| The atrium | 230 | with the domestic halls, from the third crossing | 2 |
| The cistern walk | 231 | with the stone halls, until only the dark halls are left | 2 |
| The library stacks | 232 | with the stone halls, until only the dark halls are left | 2 |

## The long bedroom hall

"A corridor of identical closed bedroom doors. Two open; the rest are painted onto the wall and say so when tried."

- **The hall.**
  - Forty blocks long and three wide.
  - Sprigged wallpaper over a panelled dado, boards with a runner down them, and a plaster ceiling.
  - Five lamps.
- **The doors.**
  - Eighteen down the walls, nine a side, every fourth block, and one more at the far end. All are the same: a painted frame, and the same four-panel door with a brass knob.
  - Sixteen of the eighteen, and the one at the end, are painted onto the wall.
  - Two are real doors (`hall_door`), drawn from the same picture. They are the hall's only ways on, one on each side.
- **Telling them apart.**
  - A painted door is flush with the wall. A real one stands back in its frame, as every door in the House does.
  - Trying a painted door gives a dull knock and a grey line. In turn: *It is painted on.*, *This one is painted too.*, *The light on the handle is painted on as well.*, then *Painted.*

## The atrium

"A tall square room with a balcony ring. One door is on the floor and one on the balcony, reached by a stair around the walls."

- **The room.**
  - Thirteen square and ten high: panelled plaster, a chequered marble floor and a coffered ceiling.
  - A skylight over the well, lit from above.
  - Lamps under the balcony and up under the ceiling.
- **The balcony.**
  - Two wide, all the way round, on boards, round a well nine square.
  - A turned balustrade a block and a half high, too high to climb over, stands round the well's edge.
- **The stair.**
  - Stone steps go up the west side of the well, half a block at a time, so the climb is a walk.
  - It rises from the north end to the south, where the top step meets the balcony.
  - A solid wall on the stair's open side rises with it, a block and a half or two over each step.
  - Over the top of the stair, where the balustrade cannot stand, the drop to the steps is two blocks at most.
- **The doors.** The floor door is in the north wall, under the balcony. The balcony door is in the east wall, at the balcony's level.

Nowhere in the room can a reader fall more than two blocks.

## The cistern walk

"A stone walkway over still water, with pillars and a dripping vault. Nothing in the water, ever."

- **The water.**
  - A cistern fifteen wide and forty long, filled with still water four deep over a silted floor.
  - The water is level with the walk. A reader who goes in can swim back to the walk and climb out.
- **The walk.** A causeway of flags, three wide, runs down the middle to the far door. A branch, three wide, goes east to the side door.
- **The columns.** Two rows of drums stand in the water from its floor to the vault, with the springing of an arch either side of each.
- **The vault.**
  - It steps down from over the walk to the walls.
  - Here and there it drips (`hall_cistern_drip`). A drop gathers under the stone, falls, and is heard landing in the water or on the walk.
- **Light.** Lamps hang over the walk and the branch. The water at the walls stays dark.
- **Nothing is in the water.** No creature, no object and no event.

## The library stacks

"Narrow aisles between tall shelves, forming a small maze with two exits, and books on the shelves that can't be taken."

- **The stacks.**
  - Behind a strip inside the door, the room is a maze of ten aisles by ten.
  - Its shelves are four high, higher than a reader's eyes, and every aisle is one wide.
  - The ceiling is a block above the shelves, so the lamps' light comes over them.
- **The maze.**
  - The same every time it is built, from a fixed seed.
  - Every aisle reaches every other.
  - In the middle the shelves are cleared for a reading table, with a book left open on it.
  - The two ways out, one in the west wall and one in the north, are found through the maze the long way round.
- **The books.** The shelves are packed in two fillings, every spine its own. Trying them gives a grey line and nothing comes away. In turn: *The books will not come out.*, *None of them come out. They are packed in too tight, or fixed.*, *Not one of them moves.*, then *They will not come out.*

## Art

`tools/draw_ordinary_hall_art.py` draws 25 more textures (37 in all):

- the bedroom hall's wallpaper, dado, boards, runner, ceiling, frame and the door's two halves;
- the atrium's plaster, marble, balcony boards, coffers, stone step, skylight and balustrade;
- the cistern's wet stone, flags, silt, column drums and vault brick;
- the stacks' panelling, boards, beamed ceiling and two fillings of books.

It also writes the models:

- the painted doors and shelves (`hall_face`, `HallFaceBlock`);
- the real doors on the game's own door shapes (`hall_door`, `HallDoorBlock`);
- the dripping vault (`hall_cistern_drip`, `HallDripBlock`);
- the half step, the balustrade and its corner, and the reading table (`hall_fitting`).

The materials are the `hall_bedroom_*`, `hall_atrium_*`, `hall_cistern_*` and `hall_stacks_*` blocks. The lamps are the still light's caged lamps, lit. The cistern's water is the game's own. No vanilla building block is used in any of the four.

## Tests

New cases in `ExplorationPassTests`:

- `theLongBedroomHallHasEighteenDoorsAndOnlyTwoOpen`:
  - The two real doors are hall doors, walked to.
  - Sixteen painted doors face into the hall, flush, each in the same frame, plus one at the end.
  - Runner, lamps.
  - A painted door tried says so and stays painted; a real one opens.
- `theAtriumHasADoorOnTheFloorAndOneOnTheBalconyUpAStairThatCannotBeFallenFrom`:
  - Not dealt on the first crossings.
  - The floor door is walked to, and the balcony door is on the balcony.
  - Every step is half a block up with room over it, and the top meets the balcony.
  - The balcony is walked round to its door.
  - Every edge of the well has a rail a block and a half high, or a drop of two blocks at most.
  - Skylight and lamps.
- `theCisternWalkCrossesStillWaterUnderADrippingVault`:
  - The walk reaches both doors.
  - Still water, four deep over silt, everywhere off the walk.
  - The columns stand from the floor to the vault.
  - The vault drips here and there, never over the middle of the walk, and is open under it.
  - Nothing is in the water. Lamps.
- `theLibraryStacksAreASmallMazeWithTwoWaysOutAndBooksThatWillNotComeOut`:
  - Every shelf is four high with air over it, and the maze is the same every time.
  - Both ways out are found through the maze, at least four blocks longer than straight.
  - The reading table is reached.
  - Books tried say so and nothing is taken. Lamps.

## What stays

- **Witness:** 81 / 61 / two kinds / three endings.
- **Protocol:** 163.
- **Layout:** 130. Four appended slots (229 to 232). New builds only.
