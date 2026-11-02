# Five more puzzle rooms (owner proposals, 2 November)

The owner proposed five more puzzle rooms after the aftermath rooms:
- three puzzle families: the Folding Rooms, the Fuse Board and the Header Tank;
- two single encounters: the Window Cleaner and the Night Operator.

This page records the proposals as given, how each will be built, and the order. Each is its own release, with its own canon page once built.

All five join `PuzzleRooms` and keep its rules:
- **Pacing.** A reader is offered one only after resolving a story since their last puzzle, and a few halls on. The offer is shared
  among the puzzles they could have.
- **Where they come.** Early rooms come from the third crossing and never in the stone. A deep room comes only from the strange depths,
  once its family's early room is solved.
- **Nothing hurts.** Nothing in them counts down, hurts or is used up.
- **Solved for good.** A solved room stays solved, for everyone.
- **Not stories.** None of them counts toward the Witness. Witness stays derived from `WitnessAccount.Story`: no source, quota or
  ending change.

The two encounters are early rooms of families of their own, with no deep room.

| Slot | Place | Family | Depth |
| --- | --- | --- | --- |
| 156 | `FOLDING_ROOM` | FOLD | early |
| 157 | `FOLDED_ROOMS` | FOLD | deep |
| 158 | `WINDOW_CLEANER` | WINCH | early |
| 159 | `NIGHT_OPERATOR` | LINE | early |
| 160 | `FUSE_ROOM` | POWER | early |
| 161 | `FUSE_ROOMS` | POWER | deep |
| 162 | `HEADER_TANK` | WATER | early |
| 163 | `HEADER_TANKS` | WATER | deep |

The layout version rises with each release that adds places, as it did for the airing rooms; the build is for new worlds only.

## 1. The Folding Rooms (FOLD)

> A former dining room has heavy wooden partitions on floor and ceiling tracks. Moving one opens a passage while closing another; gaps
> above the panels let you see where you're trying to reach. The early version teaches one sliding partition. The deep version has
> several connected spaces, with wall recesses where you can stand while shifting them. Worn tracks and furniture impressions show how
> the room used to fit together. Solving it leaves an understandable, usable space.

**How a partition works.** A partition is a wall of folding panels as wide as the room, three blocks high, on a brass track let into
the floor and a matching track on the ceiling. The track runs along the room's length. The ceiling is two blocks above the panels, so
the far side can always be seen over them, but not reached. A hand on a partition's pull pushes it one track length away from the
reader:
- it rumbles along with its panels flexing;
- it stops at a stop block, a pocket, or anything standing in its way (a body or a piece of furniture), and never crushes;
- it can be pushed back from the other side, so every move can be undone and nobody is shut in.

As it moves, a doorway in the side wall comes out from behind it while another goes behind it, so moving it opens one way through
and closes another.

**Early (`FOLDING_ROOM`).** Built in 0.4.250; canon is docs/FOLDING_ROOMS_0_4_250.md. One dining room cut in two by one partition:
- **The partition.** It stands against its far stop, right in front of the far door in the side wall. From the dining room it will
  not go.
- **The pantry.** It runs along the side, with a doorway on either side of the partition.
- **The move.** From the far side the partition pushes back off the door. It comes to rest across the serving hatch, and it pushes back
  again from the dining room.
- **What it teaches.** That the panels slide only away from whoever pushes them, that the gap above shows where the way is, and that
  they slide back.

**Deep (`FOLDED_ROOMS`).** A long suite of dining room, morning room and serving pantry with two partitions on tracks along its
length:
- **Recesses.** Shallow recesses in the side walls stand beside the track, a body's width deep. A reader standing in one is off the
  track, and can reach a partition's edge pull as it passes. They can push a partition right past themselves and step out on its
  other side. This is the only way across a partition without a way round.
- **Side doorways.** Side doorways join the suite to the pantry corridor, and each partition covers one or another as it moves.
- **The sideboard.** One sideboard stands across a track where the floor shows the worn-in feet of where it used to stand, in a
  recess. Dragged back to its marks (as the dust rooms' chair is dragged), it frees the track, but it fills that recess.
- **Telling marks.** The worn bright patches on the tracks show where each partition usually stood. The pale squares on the boards
  show where the table and chairs went.

When the far door is reached, the partitions fold back into their pockets at the ends. The table stands where the floor shows it
belongs: one long room, laid for dinner, that anyone can see the sense of.

**Assets:** one `fold_fitting` block (folding panel, pull, track, stop, pocket, sideboard, worn marks), parquet and panelling
materials, drawn by a new `tools/draw_fold_fittings.py`.

## 2. The Window Cleaner (WINCH)

> A window cleaner stranded on a hanging platform asks you to operate an emergency winch inside the room. As you lower them, lit
> windows pass outside: a bedroom, a kitchen, a landing. Eventually, one reveals the corridor you entered from, seen from outside and
> below. The cleaner steps off into that corridor. A moment later they come through your door carrying their equipment, thank you, and
> leave. You can watch the entire return. The unease comes from seeing how those places apparently connect.

**The building.**
1. The entry door opens on a short corridor:
   - plaster;
   - a lamp;
   - a coat on a hook;
   - a framed print;
   - a casement window in its outer wall.
2. At its end a narrow stair climbs four storeys, its own small windows looking into a light well.
3. At the top is a plant room with the emergency winch: a drum, a crank, a red brake lever and a cable out through a slot in the frame.
4. Its big window looks across the light well.

Across the well, on the facing wall, are lit windows one above another:
- a bedroom;
- a kitchen with a table and a clock, the familiar kitchen's;
- a landing;
- at the bottom, below the reader, the corridor they came in by, its casement, lamp, coat and print.

The rooms behind those windows really are stacked there, over the corridor, with no door to any of them.

**The encounter.** The cleaner stands in a cradle hung outside the plant-room window with a bucket and squeegee, and knocks:
1. **Asking.** They ask for the brake to be taken off and the winch wound down, slowly. Their speech shows over their head, as
   everyone's does.
2. **Lowering.** Each turn of the crank lets the cradle down a step. It passes the lit windows, and the cleaner says something about
   each, ordinary things: a lamp left on, a kettle.
3. **The bottom.** It stops at the corridor's casement. The cleaner opens it and steps in.
4. **The return.** They walk the corridor and climb the stair. The reader can watch every step through the corridor's casement and the
   stair's windows.
5. **The thanks.** They come through the plant-room door carrying their bucket and squeegee and thank the reader.
6. **Leaving.** They unlock the staff door (the far door) and leave by it. That is the room solved.

Nothing is teleported: the corridor in the well is the corridor the reader walked.

Afterwards the cradle hangs empty at the bottom of the well.

**Assets:** one `winch_fitting` block (drum, crank, brake, cable slot, cradle, cradle rail), the cleaner's painted skin, and a winch
ratchet and cradle-creak sound.

## 3. The Night Operator (LINE)

> An operator at an old telephone exchange asks you to help someone find their way out of another wing. You patch into different rooms
> and identify them through background sounds: running water, a refrigerator, someone walking on a bare floor. Opening the correct
> connection lets the caller continue. Their footsteps become progressively nearer. When they finally say they're outside, footsteps
> cross your exchange toward its door. Nobody is visible. The operator removes their headset: "That's us finished."

**The exchange.** A small switchroom at night: one lamp, a cork board of shift notes, and a cord switchboard of numbered jacks, each
with its own lamp. The operator sits at it, in a headset. A spare headset hangs on the board.

**Listening.** A hand on a jack listens to it. For a few seconds the reader alone hears that line's room, thin and close as a
telephone makes it. Each jack is a room somewhere in the House with something going on in it:
- water running;
- a refrigerator;
- someone walking on a bare floor;
- a clock;
- rain on a window;
- a radio between stations;
- a kettle coming to the boil;
- nothing but the line.

Which jack is which is fixed for each world.

**Patching.** The caller's lamp is lit. The operator explains, once, and leaves the board to the reader. The caller, on the action bar
as speech always is, says what they can hear behind the next door: water running, then a refrigerator, then someone walking on bare
boards, then a clock. A crouching hand on a jack puts the caller through to it:
- **The wrong room.** The caller says it is not right and waits.
- **The right room.** The caller goes on. Their footsteps come, private to the room and from the direction of the exchange's entry,
  nearer each time.

**The end.**
1. The caller says they can see light under a door and asks to be put through. The last jack is the exchange's own.
2. They say they are outside.
3. Footsteps cross the switchroom, past the reader and the operator, to the far door.
4. The far door opens and closes. Nobody is there.
5. The operator takes off their headset: "That's us finished." The room is solved, and its far door unlocked.

**Assets:** one `exchange_fitting` block (jack field with lamps, cords, plug board, headset hook), the operator's painted skin, eight
line loops band-limited like a telephone line, and the footsteps. Audio by a new `tools/generate_operator_audio.py`.

## 4. The Fuse Board (POWER)

> One working fuse, two circuits: a work light and a powered shutter. You need the light to find and secure a manual catch, then
> transfer the fuse to operate the shutter. Deeper versions combine limited power with doors you can wedge and equipment whose operation
> is visible through adjoining windows. Wires physically lead to their appliances. Every change is reversible, so experimentation is
> useful.

**How the board works.**
- **The board.** Holders on the wall, each labelled for its circuit in an old hand, and one spare clip. A hand on a holder with a fuse
  in it moves the fuse to the clip. A hand on an empty holder moves the clip's fuse into it. Nothing is carried, so nothing can leave
  the room.
- **The cables.** They run in clipped lines along the walls from each holder to what it feeds.
- **The appliances.** Each has a small lamp that shows when it has power.

**Early (`FUSE_ROOM`).** A basement workshop, dark but for the fuse board's pilot lamp and a line of light under a steel roller
shutter:
- **The circuits.** One fuse, holders for WORK LIGHT and SHUTTER.
- **The catch.** The shutter's motor strains against a manual catch on its guide rail and stops. In the dark the catch cannot be
  found: a hand on the rail feels only cold iron.
- **The solution.** With the light on, the catch is plain to see, and a hand draws it and pins it back. Then the fuse goes to the
  shutter: the light goes out, and the shutter rolls up onto the lit room beyond.
- **Afterwards.** The pin holds the shutter up for good, so the fuse can go back to the light: a lit workshop with its way open.

**Deep (`FUSE_ROOMS`).** Three rooms in a row behind windows:
- **Power.** Two fuses, four circuits.
- **What they feed.** A work light; an electric latch on the door between the first two rooms (it can be opened only while powered,
  but it can be wedged); a hoist in the second room, seen working through the window, that lifts a crate off a floor hatch; and the
  far room's shutter, with its own manual catch to be found in light.
- **The puzzle.** The reader has to put the power where it is needed in the right order, using the wedge to keep what power has
  opened. Every move can be undone.

**Assets:** one `power_fitting` block (board, holder, clip, fuse, cable runs, work lamp, shutter, guide and catch, latch, hoist, crate),
drawn by a new `tools/draw_power_fittings.py`, with a hum and the shutter's motor.

## 5. The Header Tank (WATER)

> An attic tank feeds a partially flooded utility room. A wooden platform floats below an inaccessible doorway. Valves let you
> transfer water between chambers until the platform meets the landing. The early room makes the relationship easy to watch. The deep
> version uses two linked chambers: raising one platform lowers another, so you have to establish a route through both. Old watermarks,
> visible pipes and the movement of the floats communicate what each valve does.

**How it works.**
- **Water levels.** A chamber's water is whole layers of still water, one level at a time.
- **The platform.** A slatted wooden platform floats on the top layer, and anyone standing on it rides it up or down with the water.
- **The valves.** A wheel on a pipe, turned once, runs water through that pipe one level every second until turned back. The water
  shows at the pipe's outlet, and stops at the bottom or the brim.
- **The tank.** It has a float arm and a gauge board that go up and down with its level.
- **Watermarks.** The walls carry the stains of levels the water used to stand at, one of them at the landing.

**Early (`HEADER_TANK`).** A utility room with a flooded pit and, overhead through an open hatch, the attic tank:
- **The way up.** A gallery runs round the pit at the landing's height but stops short of the doorway.
- **The controls.** A feed valve runs water down from the tank. A hand pump beside it sends it back up.
- **The solution.** The platform has to come level with the gallery. The reader steps from the gallery onto it, and from it across to
  the doorway.
- **Too much or too little.** Too much water and the platform rides past the gallery; too little and it is out of reach. Either can be
  put right.

**Deep (`HEADER_TANKS`).** Two pits side by side, each with its own platform:
- **The link.** A transfer valve between them, so raising one lowers the other. The feed tank holds too little to set both by itself.
- **The route.** From the entry the first platform must stand at the middle landing. From that landing the second must stand at the
  far doorway.
- **The valves' places.** The valves are where they are reachable from, so the order matters.

**Assets:** one `tank_fitting` block (tank wall, float, gauge, valve wheel, pump, pipe outlet, platform, watermark), drawn by a new
`tools/draw_tank_fittings.py`, with running water and pump sounds.

## Order

In the owner's order, one release each:
1. the Folding Rooms (early and deep together);
2. the Window Cleaner;
3. the Night Operator;
4. the Fuse Board;
5. the Header Tank.
