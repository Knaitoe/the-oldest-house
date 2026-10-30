# The vaulted hall and the crumbled hall — 0.4.199

The owner, October 30: "You can add further variant hallways too - say, one with a massive ceiling and one with a crumbled floor players have to 'parkour' across."

Both halls are plain plaster halls like 0.4.197's four (`HallVariations.plain`). Nothing is bled into them, and they hold no story and no supplies. They are built by the same builder (`HallVariations.buildDomestic`), which now takes the ceiling height from `HallVariations.roof` and the walls from `HallVariations.shell`.

## The vaulted hall (`VAULTED_HALL`, slot 118)
- **The approach.** A low neck, three wide with an ordinary ceiling, leads in from the door. It opens into a nave seven wide and 37 long whose ceiling is fifteen blocks up (`HallVariations.VAULT`). Another low neck leads out to the far door. The hall is seen only once you're through the neck.
- **The walls.**
  - Plaster rises between six stripped-spruce pilasters on each long wall.
  - A picture rail runs at the height of an ordinary hall's ceiling.
  - A clerestory of dark glass sits high up, with a real deepslate back.
  - A cornice runs under the ceiling.
- **The ceiling.** Dark oak coffers, crossed by stripped dark-oak beams from pilaster to pilaster.
- **The light.** Four lanterns hang down the middle of the nave, each on ten blocks of chain, so the light stays at the height of an ordinary hall and the ceiling stays in shadow.
- **The doors.** One on the west wall halfway down, between two pilasters, and the far door.

## The crumbled hall (`CRUMBLED_HALL`, slot 119)
The floor has fallen three blocks into a cellar between the two landings. What is left of the boards crosses it (`HallVariations.CROSSING`, in order):
- a stub of board at the broken edge;
- three short lengths of floor;
- a joist, walked along its length;
- a beam end tipped up onto a heap of rubble, one step up;
- a length of floor that is the long jump, two blocks across;
- one last board before the far landing.

No jump is longer than two blocks or higher than one, so the crossing asks for timing, not exact pixel jumps.
- **A fall never hurts.** The pit is three blocks deep, and three blocks is the most a fall can be without harm.
- **A missed jump costs a climb.** The pit's one ladder climbs the near wall, back to the door they came in by, for another try. Nothing in the pit reaches the far side: its far wall is sheer, and the fallen boards on its floor are half a block high and stand against nothing.
- **The cellar.**
  - Foundation walls of stone brick, with the old joists' ends in them at the floor line.
  - A floor of cobble, moss and dirt.
  - The floor left standing rests on spruce posts.
  - Torches low on the walls show how shallow it is, so the polish pass never hangs a lamp down into the jumps.
  - A lamp over each landing.

## Dealing
- **The vaulted hall** is weighted 8 before the stone depth. That is a little rarer than the other plain halls, so it stays a surprise.
- **The crumbled hall** is weighted 8 from the third crossing until the stone depth, so nobody's first steps in the House are a jump.
- Both are in the gray pool, count as ordinary for the pacing and lead on (`LabyrinthDealer.onward`). Like every domestic hall, they give way to stone at the stone depth.
- **Saved routes** keep the halls already dealt to them.

## Saved worlds
Layout 53 appends the two slots through the existing bounded append, with no change to any earlier address. Slot 119 is the last one fifteen columns hold at the common eighty-block manor height (eight slots a column).

## Literature
The House of Leaves names them "a hall with a ceiling far too high" and "a hall whose floor had fallen in" (`StaircaseProse.place`).

## Tests
- **`ExplorationPassTests.theVaultedHallAndTheCrumbledHallAreHallwaysToCrossNotRooms`.**
  - **The vaulted hall** passes every ordinary hall check: reachable doors, supported bays and lamps, room for two. The nave is open to fifteen blocks, both necks are roofed solid at an ordinary height, and each lamp hangs on its chain.
  - **The crumbled hall:**
    - every piece left standing bears a body;
    - every jump in the crossing is at most two across and one up;
    - the pit has a floor three below the boards everywhere, and nothing in it stands higher than a fallen board;
    - its one ladder (three rungs) climbs the near wall, and its far wall is sheer;
    - the far door is reached across the boards, not by walking;
    - every lamp has support and hangs clear of the jumps.

    It also checks that both halls are plain, dealt as above and named in prose. Both views are exported, so the architecture proof expects 105 earlier views.
- **`HazardVarietyTests`** pins the slots at 118 and 119.
- **`LabyrinthTests`** counts fifteen halls in the opening pool.
- Locally the exploration namespace (57 cases) passes.

## What stays
- **Protocol 102**, unchanged.
- **Witness unchanged:** 55 / 42 / two kinds / three endings.
- **No PNG changes.**
