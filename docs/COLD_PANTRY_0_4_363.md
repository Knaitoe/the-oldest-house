# The Cold Pantry (0.4.363)

This is item 5 of the owner's "New Puzzle and Hazard Room Designs", the temperature hazard. The owner's notes ask for:
- an ice-box and ventilation shutters;
- a room that starts cold;
- opening shutters or removing ice to warm it slowly, and closing shutters or replacing ice to cool it slowly;
- the temperature shown by frost on the pipes and the reader's breath;
- a preserving door that opens only while the room is cold, and a thaw door only once it has warmed;
- everything reversible, with no damage and no lethal time pressure;
- an aftermath: if the room is left warm, something preserved may change on a later visit.

Nothing in the room hurts, so, though the owner lists it among the hazards, it is paced as a puzzle room:
- a new family, COLD, with one early room, the cold pantry (`COLD_PANTRY`, slot 220);
- offered only after a story, and rarely, like the other puzzle rooms;
- nothing counts down or is used up, and everything goes back by hand;
- solved stays solved, for everyone;
- no Witness credit.

Layout 118, protocol 157. No Witness or source-kind change: 80 / 60 / two kinds / three endings. Code: `ColdPantry`, `PantryBlock`.

## The room

A stone-flagged larder under a plank ceiling, lit by one hung lantern.
- **Along the west wall**, slate shelves of jars and crocks over barrels, and a scrubbed table in the middle of the floor.
- **The ice-box** stands in the north-east corner, packed with three blocks of ice.
- **The zinc tub** stands empty by the east wall, under two ventilation shutters set high in it.
- **Two pipes** run the width of the room under the ceiling, one at each end, thick with frost.
- **A thermometer** hangs on the far wall.
- **At the far end**, the cold store's heavy door stands in the wall on the west, and the far door on the east.
- **The cold store** is a small room behind its door. Shelves of preserves line it, and on a marble table a three-tier wedding cake stands under a glass bell. A blank card leans against the glass.

## The warmth

The room's warmth runs from 0 to 100 and starts at 0. It is shared and saved.
- **What it heads for.** Each open shutter adds 25. The ice lifted out of the box adds up to 50 more: about 17 for each block. So the room can be warmed by daylight, by taking the ice out, or both.
- **How fast it moves.** The warmth moves two points toward its target every half second. From bitter cold to the thaw takes about twenty seconds, and cooling back takes as long.
- **While nobody is there.** The room counts nothing while it stands empty. When someone comes back, it catches up for the time that has gone by, toward whatever the shutters and the ice were left at.

**The ice.** A hand on the ice-box lifts a block out and sets it in the tub; a hand on the tub puts one back. There are always three blocks between them: nothing melts away.

**The shutters.** A hand pushes a shutter open on daylight, which comes in with milder air, or pulls it to.

**What shows it.**
- The frost on both pipes thins in three stages as the room warms, cracking off as it goes, until the pipes are bare and wet.
- The thermometer's line climbs in four stages.
- Below 40, every reader's breath shows in small clouds, and each reader sees the others'.
- From 60, water drips from the pipes.
- While the room is cold, snow drifts down off the pipes, and frost glitters round the far door until it has let go.

## The doors

**The cold store's door.**
- From the larder it opens only while the room is cold, at 20 or below: "The seal gives with a soft sound, and colder air comes out round you."
- Above 20, its seal holds: "The door won't give. In the warmth its seal has swelled tight in the frame."
- From inside the store it always opens, so nobody can be shut in: "From this side the door has a plain latch. It opens."
- It always shuts, from either side.

**The far door.**
- It is held until the room has once been warm enough: "The door is frozen fast in its frame. Frost stands thick on the handle."
- At 75, the frost lets go: "At the far end something cracks. The frost has let go of the door, and water runs down its panels."
- The room is solved then, and stays solved however cold it is made again.

So the cake can be reached only while the room is cold, and the way on only once it has been warm.

## The aftermath

The cake is kept in the room's saved state, for everyone.
- **When it changes.** Someone comes back after the room has stood empty for more than ten seconds. If it was left at 50 or warmer, or warmed to 50 while nobody was there, the cake goes on one stage.
- **Its stages.** It goes from perfect to slumped and sweating, then to sunk and furred, and no further.
- **Seen, not told.** Nothing announces the change. A reader who looks at the cake sees it:
  - "The cake has slumped under its bell. The icing sweats, and the top tier leans."
  - "The cake has sunk in on itself. Mould furs the bottom tier, under the glass."
- **Keeping it.** Ice put back and shutters shut before leaving keeps it as it is.
- **Built again**, the room is cold, the ice is back in its box and the shutters are shut. The cake stays as it was left.

## Assets

- **The block.** One new block, `pantry_fitting` ("Pantry Fittings"). Its kinds are:
  - the ice-box and the tub, each holding nothing to three blocks of ice;
  - a shutter, shut or open (an open shutter gives light);
  - a pipe, bare or under three thicknesses of frost;
  - the thermometer at four heights;
  - the store door's lower and upper halves, shut or open;
  - the cake, perfect, slumped or sunk.

  It has a facing and a stage from 0 to 3.
- **Art.** Drawn by `tools/draw_pantry_fittings.py` in vanilla's manner, 27 textures. The cake's model sits on a marble foot under a glass bell.
- **Sounds.** Five subtitled sounds are synthesized by `tools/generate_pantry_audio.py`:
  - "Ice scrapes";
  - "A shutter swings";
  - "Water drips";
  - "Frost cracks";
  - "A seal gives".
- **Checks.** CI and the quick gate check both tools with `--check`.

## Tests

PantryTests (exploration namespace, batch `cold_pantry`), `thePantryWarmsByHandAndTheCakeRemembers`:
- **At the start.** It is cold: the ice is in its box, the pipes are frosted and the far door is frozen. The store door opens, and shuts.
- **Warming.** The ice goes into the tub and both shutters open. The room warms slowly.
- **The store door.** Past the cold, its seal holds from the larder, but from inside the store it opens, and it shuts.
- **The thaw.** At the thaw the room is solved, the far door gives, the pipes are bare and the thermometer is high.
- **Left warm.** The reader disconnects with the room warm. Nothing is counted while they are away. On their return the room has warmed on and the cake has gone on a stage.
- **Cooling.** The ice goes back and the shutters shut. The room cools, the store door opens from the larder again, and the room stays solved.
- **Left cold.** The reader disconnects with the room cold, and on their return the cake has kept.
- **Built again.** The room is cold with the cake as it was left.
- **Nothing is earned.**
