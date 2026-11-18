# The list 1b: the field, Amber's house and the yards (0.4.700)

The second part of *The list* (`docs/THE_LIST_PLAN.md`), after 1a (`docs/THE_LIST_0_4_699.md`). It adds three things to Lamesa at midnight: the high school's football field out west where South Seventh ends, Amber's house on South Seventh, and what the neighbors leave in their yards. The pasture comes in 1c and the town's people in 1d. The place is still not dealt and is not yet a Witness source.

## The place

The scene now runs from x -164 (was -88) to 96. Everything else in 1a stands as it was, except that Amber's house replaces the neighbor that stood at the same lot on South Seventh, and one mesquite that would have stood in her side yard is gone.

- **The road out.** South Seventh's caliche runs on west past the end of the lots and through the cotton to the stadium's gate. North of it is the school's lot, caliche, with one street light.
- **The field.** Chain-link runs round all of it (x -161 to -96, z -98 to -16).
  - The east fence opens where the road comes in. The north fence opens behind the field house's back doors.
  - Inside, the grass is worn through to dirt in patches.
  - A cinder track two blocks wide runs round the field, straight along the sides and round at the ends.
  - **The field** (x -136 to -112, z -88 to -28) has end zones at each end, a white line every five blocks from goal line to goal line, sidelines, and the turf mown in bands between the lines.
  - **The goalposts** stand behind each end line. Each has a padded foot, a pole up to the gooseneck, and the gooseneck bending out over the line to the middle of the crossbar. The crossbar spans five blocks, and an upright rises from each of its ends.
  - **The home stands** are on the west: a concrete walk at their foot and eight aluminum rows rising half a block apiece, so each row is the seat of one and the footboard of the next.
    - A steel frame stands under them, and a rail runs along the top.
    - Under the high rows a body can walk upright.
    - The press box stands behind the top row on four posts, its floor level with that row. It has plywood walls and a flat roof, windows along the field side, and a steel door off the top row. Inside are the announcer's table and microphone, and the horn is on its roof.
  - **The visitors' stands** are on the east: four rows with a rail along the top, short of the gate.
  - **The team benches** stand at the sidelines, the home team's on the west, their backs to the stands, with a gap at midfield.
  - **Four towers of lights** stand at the corners, their banks aimed in at the field. All are dark.
  - **The scoreboard** stands past the south end on two posts, read from the field: HOME, the quarter and VIS across the top, and the down, the yards to go and the ball's place under them, every bulb dark.
  - **The concession stand** is just inside the gate: cinder block under a flat roof, its shutter down over the counter, the prices on a board beside it and a light over it.
  - **The ticket booth** is outside the gate, its window on the road.
- **The field house** is on Main Street at the north end, its front on the south sidewalk with the stores' fronts and its back on the field. It is cinder block under a flat roof, with a steel door off the street and a pair onto the field, a light over each.
  - The locker room has lockers along the north and west walls and benches down the middle. One light is left on.
  - Through the wall to the east is the coach's office, with his desk and chair and the chalkboard.
  - Behind the office are the showers, tiled, a row of heads along the wall.
  - The steel doors open as doors do.
- **Amber's house** is on South Seventh. It is built as the reader's is, double-walled, but in face brick to the eaves.
  - Through the front door is a short hall.
  - The den is on the west, panelled. It has the television with the VCR under it against the west wall, the shelves of rented tapes beside it, tapes stacked on the carpet, the couch and its end table, the coffee table with the remote, the recliner, a framed photograph and the picture window.
  - Amber's room is on the east. Her desk with its notebook and lamp is under her own window; there is a chair at it, her bed against the east wall, the phone on the nightstand, two bookcases of paperbacks, a dresser and posters.
  - Behind them are the kitchen (counters, sink under the window, range, refrigerator, a small table and two chairs, the telephone), her mother's room (a double bed with a nightstand either side, a dresser) and the bathroom.
  - The back door opens onto a walk to the alley through a gate in the back fence. Her trash can is at the alley.
  - Out front are the walk, the porch light, the mailbox at the curb and chain-link across the yard with a gate at the walk.
  - The television is on in the den, her lamp is on, and her window is lit. The rest is dark.
  - Both outside doors are locked for now.
- **The yards.**
  - Sprinklers go on three front lawns.
  - A doghouse stands by each of two back fences.
  - Trash cans are out at the alley and at one curb.
  - A bike lies on its side in a front yard.
  - Two lawn chairs and a grill stand in a back yard, with a kiddie pool beside them.
  - A hose reel is on a side wall.

## Single and multiplayer

Shared and recurring, as in 1a.

- A hand on anything answers that reader alone, as before.
- Amber's front door says it is locked to the reader who tried it and stays shut. Her back door says what every neighbor's door says. The field house's steel doors and the press box's door open for anyone.
- The sprinklers' jets are seen by everyone near them: each ticks across half a circle and back, throwing water out three or four blocks. The sprinkler's tick is played at the sprinkler for anyone outside within fourteen blocks of it.
- The dog behind the nearest back fence is heard only in the neighborhood, not out at the field.
- Under the field house's roof, in Amber's house and in the press box there are no crickets or wind, as in the other buildings.

## Made as

- **Bounds.** `TheListSite.WEST` is -164. `LabyrinthPlace.THE_LIST`'s bounds and the client haze follow it.
- **`TheListSite`.** Constants for the fence, the field (`FIELD_W/E/N/S`, its middle `FIELD_X/Z`), the gate, the field house's back wall and the press box's door; Amber's walls, `AMBER_FRONT`, `AMBER_BACK`, `AMBER_TV`, `AMBER_DESK`; `SPRINKLERS`.
  - `school` builds the road, the lot, the ground inside the fence (`fromInfield` places the track as a ring two blocks wide round the line between the two centres of its turns; `fieldAt` the field's own turf), the fence, the goalposts, both stands with the press box, the benches, the towers, the scoreboard, the concession stand, the ticket booth and the field house.
  - `amberHouse` builds her house; `yards` sets the neighbors' things out.
  - `steelDoor` hangs the field house's doors in the outer face of their walls.
  - `inAmber` and `inFieldHouse` count as indoors.
- **`TheListBlock`.** Six new kinds of eight stages each: FIELD (the goalposts and the light towers), STANDS, LOCKER (the field house and the concession stand), SCORE (the scoreboard and the ticket booth), AMBER and LOT (the yards). The lights' banks, the horn, the chalkboard, the shower heads, the price board, the sprinkler and the bike have no collision.
- **`TheListRegistry`.** Six materials (field turf, mown turf, yard line, sideline, end zone, cinder track), the steel door, and one sound.
- **`TheList`.** Amber's front door has its own cue. The sprinklers spray and tick. The dog keeps to the neighborhood.
- **Art.** `tools/draw_the_list_art.py` now draws 280 textures and 355 models and states. The new ones are the turf in its kinds, the lines and end zones, the cinder, the goal paint and pads, the light banks, the aluminum, the press box glass, plywood, the horn, the lockers, the chalkboard, the concession menu, the scoreboard (one picture cut in six) and its lettering, the ticket window, the steel door, the VCR and tapes, Amber's notebook and window, the remote, the sprinkler, the doghouse, the galvanized can, the bike, lawn chair webbing, the grill, the hose and the kiddie pool. The crossbar's middle model reaches a block either way, so the bar spans all five blocks.
- **Sound.** `tools/generate_the_list_audio.py` adds a sprinkler ticking, subtitled.
- **Tests.** `TheListTests` gains a fourth case, `theFieldAmbersHouseAndTheYards`:
  - The field's lines, an end zone, a sideline and the mown turf; the cinder track on all four sides; both goalposts whole; a tower's lights dark; both stands, the rail and the press box's window; the scoreboard read from the field; the field house's steel doors shut and its lockers.
  - A body walks in at the gate from South Seventh to the track, the field, the end zone and the top rows of both stands, but not into the field house. Once a field door is open it goes on through the locker room to the coach's office and the showers.
  - Amber's house: face brick, the television, her desk and her lit window; her doors shut. Her front door says it is locked to the one reader and stays shut, and nobody walks into her house.
  - The three sprinklers are out on their lawns, and one is heard by a reader standing on its lawn.
  - The walk out of the reader's front door in the first case now covers the field too.

## Found and fixed in the making

Seen in eye-level frames before this commit:

- Cotton stood in the gate where South Seventh comes into the stadium, and in the gap behind the field house's back doors. The road's caliche runs on through the gate, and the gap is grass.
- A band of roof gravel showed along the top of the field house, the concession stand, the ticket booth and the press box, and the locker room's ceiling was gravel. Each has a ceiling inside, the gravel a course above it, and its walls carried up through both.
- The wall between the coach's office and the showers was tile on the office side. It is painted block.
- A microphone stood at every place along the press box's table. There are three: the announcer and two spotters.
- The scoreboard's back was the same dark as the night, so from behind the end zone only its posts showed. Its back is galvanized panels.

Also in this commit, A Place for You (run 1725, `invitingThePast`): with the reader standing on her place in the hall, Mara was pushed onto the stair's first tread and stood there. The 0.4.699 set-down tried only half a block toward her chair, which was under the tread. It now tries the next point on her way too (the foot of the stair), if that is within a block and a half.

## Every new line

Everything here is written for this part. Amber's name is the book's; nothing of her in the book is used.

**Doors**
- Amber's front door: "Locked. The window beside it flickers with a television."

**A hand on things**
- The goalposts and the lights: "The goalpost's foot, in its pad. The pad is split along one seam." / "The goalpost. It goes up into the dark." / "The crossbar." / "An upright." / "A light tower. Its ladder starts above your reach." / "The lights. Off. Nobody plays tonight."
- The stands: "The stands. The aluminum still holds the day's heat." / "Under the stands: cups, a program, a drift of sunflower seed shells." / "The rail along the top of the stands." / "The team's bench. Strips of tape mark where each of them sits." / "The press box window. Dark inside." / "The announcer's table. The microphone is unplugged." / "The horn on the press box's roof."
- The field house and the concession stand: "A locker. A name on tape across the vents, half peeled off." / "A bench, worn smooth." / "The coach's desk. A clipboard, a whistle on its cord." / "Last week's play is still on the board." / "A shower head. It drips." / "The concession stand's shutter, locked from inside." / "Hot dogs, chili pie, pickles, pop, and what each costs."
- The scoreboard and the ticket booth: "The scoreboard. Every bulb is dark." / "The down, the yards, the ball's spot. Dark." / "The scoreboard's post." / "The ticket booth. A roll of tickets on the counter inside."
- Amber's house: "The television's on with the sound down. The VCR's clock says twelve." / "Rented tapes, the video store's sticker on every one. Most of them are late." / "Tapes stacked on the carpet by the set." / "Her desk. A notebook open, written to the edges." / "The phone by her bed, its cord stretched to reach the pillow." / "Paperbacks. A knife or a mask on nearly every spine." / "Amber's window. Her desk lamp is on." / "The coffee table. The remote, two tapes out of their cases."
- The yards: "A sprinkler, ticking its way across the grass." / "A doghouse. Something has chewed the corner of its door." / "A trash can, out for the morning." / "A bike left on its side in the grass." / "A lawn chair facing the street." / "A grill, cold, the grate still greasy." / "A hose wound on its reel." / "A kiddie pool with an inch of rainwater in it."

**Painted on things** (in the textures)
- The scoreboard: "HOME", "QTR", "VIS", "DOWN", "YDS", "BALL".
- The ticket window: "GATE".
- The VCR's clock: "12".

**Elsewhere**
- Subtitle: "A sprinkler ticks across a lawn".
- Block names: Field Turf, Mown Turf, Yard Line, Sideline, End Zone, Cinder Track, Steel Door.
