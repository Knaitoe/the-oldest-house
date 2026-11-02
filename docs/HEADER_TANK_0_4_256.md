# The Header Tank (0.4.256)

The owner's fifth puzzle proposal of 2 November (docs/PUZZLE_PROPOSALS_2026_11_02.md):

> An attic tank feeds a partially flooded utility room. A wooden platform floats below an inaccessible doorway. Valves let you
> transfer water between chambers until the platform meets the landing. The early room makes the relationship easy to watch. The deep
> version uses two linked chambers: raising one platform lowers another, so you have to establish a route through both. Old watermarks,
> visible pipes and the movement of the floats communicate what each valve does.

The release adds a new puzzle family, WATER, with two rooms:
- the header tank (`HEADER_TANK`, slot 163, early);
- the header tanks (`HEADER_TANKS`, slot 164, deep, from the strange depths once the header tank is solved).

The layout version is 84 (new builds only). Both rooms follow `PuzzleRooms`' pacing:
- they are offered only after a story, and rarely;
- nothing counts down, hurts or is used up;
- every move can be undone;
- solved stays solved, for everyone;
- no Witness credit.

There is no protocol, Witness or source-kind change. Code: `HeaderTanks`, `TankBlock`. The Fuse Board's rooms and these are the last
of the five puzzle proposals.

## How the water works

- **Layers.** Water is whole layers of still water, one at a time. The amount is fixed: it only moves between the tank and the pits.
- **The platform.** A slatted floor on floats, across the whole of a pit, floating on its top layer. Nobody can get into the water.
  Whoever stands on the platform rides it up and down.
- **The tank.** It sits in the attic, a steel box with a sight glass, seen from the room through an open hatch beside it. It holds four
  layers. A gauge board on the wall shows its float, from the bottom to the top.
- **The feed valve.** A wheel on the pipe down from the tank. Turned, it runs a layer into the pit every half second until it is turned
  back. The pipe's mouth in the pit wall pours while it runs, and stops when the tank is empty or the pit is full.
- **The hand pump.** Each stroke sends a layer back up to the tank.
- **Watermarks.** The pit walls carry the dark tide line of a level the water used to stand at, at the landing's level, and a fainter,
  older one lower down.
- **Getting out.** A ladder set into the near wall of each pit lets anyone on a low platform climb out. A near wall is the one on the
  entry's side, or the landing's.

## The header tank (early)

A utility room:
- quarry tiles, glazed brick, attic boards overhead;
- a flooded pit in the middle;
- the gallery round three sides of the pit. It stops short of the doorway in the far wall, where walls close in either side of the
  pit's last two rows.

The valve, the pump and the gauge are on the east wall by the entry.

**The start.** The pit holds one layer and the tank four. The platform sits four below the gallery, out of reach.

**Reaching the doorway.**
- **Level with the gallery (four layers).** The reader walks onto the platform and across to the doorway.
- **A step below (three).** The reader steps down onto it and steps up into the doorway.
- **Too much (five).** The platform rides past the gallery. A reader can jump onto it, but the doorway's lintel stops them going
  through.
- **Too little (two or fewer).** The platform is out of reach.

**Solved.** When a reader on the platform, at a level it can be crossed at, is within the last rows by the doorway, the room is solved:
"Level with the old watermark, the platform carries you across to the doorway." Built again, the platform stands level with the
gallery.

## The header tanks (deep)

Two pits in a row: the near pit north of the entry, then a landing across the room, then the far pit, two blocks deeper, with the
doorway beyond it. Walls either side of both pits mean the entry, the landing and the doorway are joined only across the platforms.

**Where the controls are.**
- **At the entry:** the tank, its feed valve, the pump and the gauge. The tank feeds the near pit only.
- **On the landing:** the transfer valve, on the east wall, with its pipe running under the landing into both pits. A hand turns it
  toward the far pit; a crouching hand turns it toward the near one; the same hand again shuts it. While it runs, a layer goes every
  half second from one pit to the other, so raising one platform lowers the other. The mouth in the receiving pit's wall pours.

**The start.** A layer in each pit and the tank full.

**The route.**
1. **The near platform at the landing.** From the entry, fill the near pit to three, four or five layers. Four is flush with both
   floors; three and five are a step either way, and both sides are open floors.
2. **Across to the landing.** Once the reader is there, the near platform can go back down.
3. **The far platform at the doorway.** From the landing, the far pit needs five or six layers: six is flush, five a step below. Six is
   the most it can hold before the lintel stops it.
4. **The tank is not enough.** It holds four layers, too few to set both platforms by itself. The far pit's water has to come from
   the near pit, through the transfer valve, after the reader has crossed.

**Solved.** When a reader on the far platform, at a level it can be crossed at, is by the doorway, the rooms are solved: "Level with
the landing, the far platform carries you across to the doorway." Built again, both platforms are set for the way through: the near at
four, the far at six. The House tops the water up to manage it.

## Assets

- **The fittings.** `tools/draw_tank_fittings.py` (checked in CI) draws the `tank_fitting` block, facing north and turned by its
  facing:
  - the platform: slats over two iron floats;
  - the feed valve's handwheel, shut and turned open;
  - the hand pump, its handle up and down;
  - a pipe's mouth, dry and pouring, with its stream;
  - the gauge board, its float at five heights;
  - a pipe on the wall: a run, a rise, a joint;
  - the transfer valve's dial, its lever upright when shut and over to either side.

  It also draws six materials: quarry tiles, glazed brick, the same brick with a strong and a faint watermark, galvanized plate and
  attic boards.
- **The sounds.** `tools/generate_tank_audio.py` (checked in CI) synthesizes four subtitled sounds, with no recordings: "A valve wheel
  turns", "Water runs in a pipe", "A hand pump gulps" and "Boards knock on a wall" (the platform as it rides).

## Tests

`HeaderTankTests` (exploration):
- `theHeaderTanksPlatformCarriesAReaderAcrossOnlyAtTheLanding`:
  - an early WATER room, built whole, no story; the far door ordinary;
  - a layer in the pit and the tank full; the platform across the whole pit;
  - the watermark at the landing's level, the ladder, and the tank behind its sight glass over the open hatch;
  - the first touch of the valve explains it;
  - opened, the pit fills a layer at a time; shut at four, the water that left the tank is in the pit;
  - each stroke of the pump sends a layer back, and a reader on the platform goes down with it;
  - left open, the valve runs the tank dry and the platform rides past the gallery with its rider;
  - past the gallery nothing is solved;
  - a stroke of the pump brings it level and the reader across is solved;
  - built again, the platform is level with the gallery, with nothing earned.
- `theHeaderTanksRouteRunsThroughBothPitsInTurn`:
  - offered after the header tank;
  - a layer in each pit, the tank full, the transfer shut;
  - the tank too small to set both platforms;
  - the feed fills only the near pit, to the landing;
  - the reader crosses to the landing; the first touch of the transfer valve explains it;
  - toward the far pit, all four layers move across;
  - a crouching hand runs water back, and shutting it stops it;
  - toward the far pit again, the far platform a step below the landing;
  - a reader across on it by the doorway is solved;
  - built again, both platforms are set for the way through, with nothing earned.
