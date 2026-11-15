# The great staircase is carried by its well (0.4.593)

The owner, on 11 November:

> "improve the architecture of the great staircase"

Earlier (the texture pass, 0.4.376): the owner allowed architecture changes to the staircase, but not to its height.

New builds only: a world that has already built its staircase keeps it. No line, protocol, layout or Witness change.
Nothing that gives light is added: the staircase is as dark as it was.

**Nothing of the way down changes.** Every tread is where it was and as high as it was. So is every landing, hearth,
leaf and loose sheet. The well is as wide and as deep. Nine blocks are clear to walk on every flight, as before.

## What was there

Seen in eye-level frames of the staircase as it stood:

- **The stair touched the well nowhere.** It is a ribbon eleven blocks wide, wound square round a well sixty-nine
  blocks across: four flights and four landings to a turn. Four blocks of open air lay between its outer edge and
  the wall, all the way down. Under each landing two short stepped brackets pointed at the corner and stopped.
- **The walls were flat.** A fluted pier every twelve blocks was set flush in the wall: a change of picture, with no
  depth. A plain course of stone ran round the well every forty-eight blocks. The cap was a flat sheet.
- **The rails began and ended in mid-air.** A thin iron balustrade ran down each side of a flight and stopped where
  the landing started, a block out of line with the landing's own rail.
- **A landing was a bare square** with a border, a rail on the two sides that faced the walls, one lone piece of
  rail at its inner corner, and the hearth standing alone on it.
- **The head of the stair was unfinished:**
  - the first row's west end was open to the well north of the camp's walkway, with nothing to stop a reader
    walking off it;
  - two pieces of the stair's rail stood inside the entry hall;
  - the walkway's iron bars stood as separate posts, none joined to the next.

## What it is now

**The well carries the stair.**

- **Pilasters.** Each pier stands a block proud of its wall for the well's whole height. There are six to a wall,
  twelve blocks apart, six either side of the wall's middle. None stands at a wall's middle, where the entry
  hall, the passage to the cell and the wall lamps are, nor where a household door opens.
- **Ties.** Where a flight passes a pilaster, twice on every flight, a stone tie crosses from the flight to the
  wall. It is as high as the rail, runs down through the edge of the flight and rests on a stepped corbel against
  the pilaster.
- **Piers in the rails.** Where a tie meets the flight a stone pier stands in the rail, on both sides. The rails
  run in bays between piers, as the walls run in bays between pilasters.
- **Strings.** The outermost column on each side of a flight is the well's own stone, stepping down with the
  treads. The rails stand on it.
- **Landings fill their corner.** Each landing's floor now runs to the faces of both walls, sixteen blocks square,
  on two corbelled courses along each wall. A landing has no rail toward the walls: the walls are there.
- **Newels.** A stone post two blocks high stands either side of each flight where it meets a landing. The flight's
  rail starts from it and ends at the next. Nine blocks are clear between each pair.
- **The landing's own rails** stand only toward the well: round its inner corner between the two nearest newels,
  and from each outer newel to the pilaster at the wall.
- **A hearthstone** lies under each of the five hearths, and under the cold camps further down.

**The walls and the cap.**

- **Capitals.** Under each course of stone round the well, every pilaster widens by a block to either side.
- **The cap is vaulted in stone.** It springs from the walls in three corbelled courses, and a rib crosses it from
  each pilaster to the one facing it, so the cap is coffered in squares that follow the walls' bays.

**The head of the stair.**

- A strip of stone one block wide closes the first row's west end: a post where the walkway's rail stops, a post
  at the corner where the stair's north rail starts, and a rail between them.
- The first row itself is clear from the door to its north rail.
- No rail stands in the entry hall.
- The walkway's bars are one rail.

**Falling masonry.** A fragment of the wall no longer comes from behind a pilaster or a course of stone. It left
the wall into the block in front of it, and where that block was stone it had nowhere to fall. (This was already
so at the courses.)

How it is done, for whoever changes it next:

- All of it is in the staircase's one plan (`FinaleArchitecture.plan`): `pilasters`, `vault`, `landing`, `tie`,
  `head`, and `pierAt` for where a pier stands. Each is laid before the entrance, the camp, the passage to the
  cell, the chamber and the side mazes, so those still cut through it as they cut through the walls.
- The plan's version is unchanged, so a built staircase is not rebuilt.

## Checks

- **The staircase built whole,** in the game's own world (`ArchitectureTests`, the staircase case), measured over
  the first three hundred blocks of the descent:
  - every landing reaches the faces of both walls, with no gap along either, on its corbelled courses;
  - no landing has a rail toward its walls;
  - a newel stands either side of each flight, with nine clear blocks between each pair;
  - the rail turns each landing's inner corner, and runs from each outer newel to a pilaster;
  - every flight runs on strings of the well's stone;
  - every flight is tied to the wall where it passes a pilaster, as high as its rail, with a pier in each rail;
  - a pilaster stands proud of the wall at three depths, and none at a wall's middle;
  - the head of the stair is closed, its first row clear, no rail in the entry hall, the walkway's bars joined;
  - the cap is ribbed and corbelled;
  - and, as before: a body clears every tread of the whole route, down to the cell and the same depth again.
- **The falling masonry case,** with a pilaster stood in front of the nearest wall column: the fragment comes from
  the open wall beside it.
- **The staircase's other cases,** 76 of them, all passing: the entrance and the camp (9), the finale (11), the
  collapse (3), the household's doors and the leaks (8), the fires and the leaves (5), the growls and the falling
  stone (8), and every case that touches the staircase among the exploration, Witness, notes, sequence,
  integration, command, multiplayer-story, lake and tailoring classes (32).
- **Eye-level frames from the same places before and after:** the door, the head, the first flight, a tie, the
  first landing from four sides, the gap between a flight and the wall, across the well, up at the cap, up from
  the second turn, the middle of the descent, and the last turn above the cell.
- **A compile, and the quick checks.**

Not checked:

- **Nobody has walked it in a played game.**
- **The frames are evenly lit.** In play the staircase is dark and lit by the reader's book, twelve blocks at
  first and thirty-two once the five fires burn. How the new stone reads in that light has not been seen.
- **A household door opened on a rebuilt landing.** The door lays its boards and its two short fences over the
  landing's new margin. Its cases pass, but they do not build the landing under it.
- **Deep in the well.** The measurements are taken over the first three hundred blocks; below that only the route's
  clearance, the rails and the walls' stone are checked, as before.

## Caught while checking

- **A post stood dead ahead of the door.** I first closed the head of the stair on the first row itself. The frame
  from the door showed a stone post three blocks in front of the reader, in the line they walk. The guard stands a
  block west of the row now, on its own strip, and the row is clear.
- **Three posts made a lump.** Each landing's inner corner first had a post of its own between the two newels. In
  the frame it was the brightest and heaviest thing on the landing. The rail turns the corner there instead.
- **The ties could not be seen from the flight.** They were first level with the treads, behind the string and the
  rail, and showed only from above. They rise to the rail's height now, behind a pier in the rail.
- **The walls' rhythm had to move.** The piers stood at each wall's middle and every twelve blocks from it. A
  pilaster a block proud at those places would have stood in the entry hall, in the passage to the cell, under the
  wall lamps, and in front of every household door. They stand six either side of the middle instead.
- **A rail can end at a course, not a pilaster.** The course of stone that runs round the well every forty-eight
  blocks crosses the pilasters, and at three landings in the whole well (one of them above the cell, the sixth
  down) it is the course that the landing's rail meets. The measurement asked for a pilaster there and failed; it
  asks for either now. Nothing in the staircase changed.
- **The staircase's wear takes a rail's pier as it takes a rail.** Far down, a broken tread loses the rail beside
  it. One of those is where a flight passes a pilaster, so the pier in that rail is gone with the tread, and the
  tie behind it stands alone. I left it: it is what wear does. The measurement skips the cells the wear takes.

## For the owner

- **Choices that are mine:**
  - the piers moved (six either side of each wall's middle, where they were at the middle and every twelve from it);
  - landings that reach the walls, which makes each one a room-sized corner where it was a platform in the air;
  - newels two blocks high, and a pier one block high in the rails at each tie;
  - the well's own stone for the strings, ties and newels, so a flight reads as part of the wall;
  - a hearthstone under each fire;
  - the vaulted cap.
- **What I left alone:**
  - the height, the route, the treads and their four stones by depth, the hearths, the leaves, the sheets;
  - the entry hall, the camp and its walkway (but for the bars), the passage to the cell, the chamber, the mazes
    and the escape;
  - the undersides of the flights, which no reader on the stair is near enough to see;
  - the walls between the pilasters, which are plain. Niches or blind arches in the bays would be the next step if
    you want more on them.
- **A world that already has its staircase keeps the old one.** New builds only is the standing rule. The plan can
  still replace a built staircase in place while nobody is on it (it has done so before): one number does it. Say
  if you want it.
- **This does not answer your later note** (15 November: "I need to consider a way for the staircase to keep its
  immense size and depth for the spectacle, but also to keep the player engaged"). Nothing here changes how long
  the way down is or what happens on it. What I measured and what I suggested are in the reply to that note.
