# The Fuse Board (0.4.254)

The owner's fourth puzzle proposal of 2 November (docs/PUZZLE_PROPOSALS_2026_11_02.md):

> One working fuse, two circuits: a work light and a powered shutter. You need the light to find and secure a manual catch, then
> transfer the fuse to operate the shutter. Deeper versions combine limited power with doors you can wedge and equipment whose operation
> is visible through adjoining windows. Wires physically lead to their appliances. Every change is reversible, so experimentation is
> useful.

The release adds a new puzzle family, POWER, with two rooms:
- the fuse room (`FUSE_ROOM`, slot 161, early);
- the fuse rooms (`FUSE_ROOMS`, slot 162, deep, from the strange depths once the fuse room is solved).

The layout version is 83 (new builds only). Both rooms follow `PuzzleRooms`' pacing:
- they are offered only after a story, and rarely;
- nothing counts down, hurts or is used up;
- every move can be undone;
- solved stays solved, for everyone;
- no Witness credit.

There is no protocol, Witness or source-kind change. Code: `FuseRooms`, `PowerBlock`.

## The board

A fuse board on the west wall:
- a holder for each circuit, its label plate in an old hand;
- a spare clip for each fuse, with the board's pilot lamp over it.

**Moving a fuse.** A hand on a holder with a fuse in it draws the fuse and pushes it into an empty clip. A hand on an empty holder
takes a fuse from a clip and pushes it home ("Somewhere along its cable, something wakes"), with a click and the mains hum. Nothing is
carried, so nothing can leave the room. The first touch of the board explains it once, for each reader.

**The cables.** Each circuit's cable runs in clips along the walls from its holder to what it feeds. It turns at junction boxes and
passes through walls at them. A hand on a cable says where it goes.

**The appliances.** Each thing with power shows it: the work light burns, and the shutter's motor, the latch box and the hoist each
have a lamp.

## The fuse room (early)

A basement workshop, dark but for the board's pilot lamp and the line of light under a steel roller shutter:
- whitewashed brick, painted concrete, a concrete soffit;
- a bench with a smithing table, a grindstone and barrels;
- the roller shutter in the north wall, its housing above it and a guide rail either side;
- beyond it, a lit loading bay with the far door, an ordinary door.

**The circuits.** One fuse, in the spare clip; holders for WORK LIGHT and SHUTTER.

**The catch.** With the fuse under SHUTTER, the motor strains: the shutter lifts a finger's width in its rails and drops back, once
each time it is given power. "Something on the guide rail is holding it." In the dark the catch can't be found: a hand on the rail
finds only cold iron.

**The solution.**
1. The fuse to the work light: the workshop is lit, and the catch on the west rail is plain to see.
2. A hand draws the catch and pins it back. The pin can be pulled again while the shutter is down.
3. The fuse to the shutter: the light goes out, and the shutter rolls up a row at a time into its housing, with the motor running.
4. "The shutter rolls up into its housing, and the pin holds it there. Beyond, a lit bay, and a door." The room is solved.

**Afterwards.** The pin holds the shutter up for good, so the fuse can go back to the light: a lit workshop with its way open. Built
again, the room is that way.

## The fuse rooms (deep)

Three rooms in a row with windows between, two fuses and four circuits: DOOR LATCH, HOIST, WORK LIGHT and SHUTTER.

**The first room.** The board, the two fuses in their clips, and an iron door in the north wall with an electric latch box beside it:
- **Without power.** The door won't open. The latch box's lamp is dark.
- **With power.** A hand opens and shuts it.
- **The closer.** If the power goes while the door stands open, it swings shut and the latch takes it.
- **The wedge.** A wooden wedge lies by the wall. Kicked under the open door, it holds it open, latch or no latch. It can be worked out
  again.

**The second room.** Seen from the first through a window:
- a hoist's motor on the west wall, its jib out over the floor;
- a crate of something heavy on its chain, sitting square on a hatch in the floor.

The hoist lifts the crate while it has power and holds it up only while powered; let down, the crate settles back onto the hatch and
shuts it. It never comes down onto anyone. With the crate up, the hatch can be hauled open: a ladder goes down, and a passage runs
north under the wall.

**The third room.** Through the passage and up a ladder, dark but for its work light, with a shutter, a guide rail and a catch of its
own. The second room's window looks into it.

**The puzzle.** Two fuses for four circuits, so the power has to go where it is needed in turn:
1. The latch, to open the door; the wedge, to keep it open.
2. The hoist, to lift the crate off the hatch. It has to stay powered, or the crate comes down.
3. The second fuse to the work light. Then down the hatch, along the passage and up into the third room. In the light the catch is
   drawn and pinned back.
4. Back at the board, the second fuse from the light to the shutter. Through the window the shutter rolls up into its housing and the
   pin holds it there. That is the rooms solved.

Built again, the way through stands open: the door wedged, the crate up, the hatch open, the shutter up and the light on.

**Nobody is shut in.** A solo reader can't strand themselves, but a second reader at the board could move a fuse while the first is
beyond the door or under the hatch. So:
- **The push bar.** From the second room's side the latched door has a push bar. It lets the latch go whatever its power, as a real
  electric lock lets anyone out.
- **The way back.** An iron door in the wall between the second and third rooms has a handle only on the third room's side. From the
  second room it is "an iron door with no handle on this side", so it never bypasses the hoist.
- **Grace.** Both doors shut themselves once nobody is in the doorway. A door opened by hand gets a moment first, forty ticks, to be
  walked through.

## Assets

- **The fittings.** `tools/draw_power_fittings.py` (checked in CI) draws the `power_fitting` block, facing north and turned by its
  facing:
  - the four holders, empty or with a fuse;
  - the spare clip with its pilot lamp;
  - the four circuits' cables, as a horizontal run, a rise or a junction box;
  - the work light, off or on;
  - the shutter's slats and bottom slat, its housing (motor lamp dark or lit), the guide rail and the catch (engaged or pinned back);
  - the latch box and the wedge;
  - the hoist's motor, its jib and pulley, the crate and the floor hatch, shut or open (climbable).

  It also draws four materials: painted concrete, whitewashed brick, a concrete soffit and steel plate. The shutter, its housing and
  the rails stop light, so the bay beyond is lit and the workshop dark.
- **The sounds.** `tools/generate_power_audio.py` (checked in CI) synthesizes three subtitled sounds, with no recordings: "Mains hum",
  "A motor runs" (the shutter rolling up, the hoist lifting) and "A motor strains, and stops".

## Tests

`FuseRoomTests` (exploration):
- `theFuseRoomsShutterRollsUpOnceItsCatchIsFoundInTheLight`:
  - an early POWER room, built whole, no story; the far door ordinary;
  - dark, the shutter down, the catch engaged, the fuse in the clip, the cables up the wall;
  - the first touch of the board explains it;
  - powered against the catch, the shutter strains and stays down;
  - in the dark the catch can't be found;
  - in the light it is drawn, pulled and pinned again;
  - the fuse to the shutter: it rolls up and the room is solved, and the light is out;
  - the fuse back to the light, the shutter held up by the pin;
  - built again it stays solved, with nothing earned.
- `theFuseRoomsPutTwoFusesWhereTheyAreNeededInTurn`:
  - offered after the fuse room;
  - the door shut without power and opened with it;
  - shut on its closer when the power goes;
  - opened by the push bar from the second room without power, the closer waiting before it takes it;
  - wedged open;
  - the crate lifted, the hatch opened (a hole with a ladder), the crate let down onto the hatch and lifted again;
  - both fuses in use and the shutter's holder empty;
  - the third room's light;
  - the way back openable only from the third room, shutting itself;
  - the passage under the wall;
  - the catch pinned and the second fuse moved to the shutter while the hoist and wedge hold;
  - the shutter rolled up and solved;
  - built again, the way through standing open, with nothing earned.
