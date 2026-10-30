# The puzzle rooms (0.4.199)

The owner: "Go ahead and add the rest. I trust ya and I'll test when I'm able to." Borrowed Light was the prototype ([BORROWED_LIGHT_0_4_199.md](BORROWED_LIGHT_0_4_199.md)). This adds the rest of the puzzle-room notes:

- the other two early rooms, the Service Lift and the Turning Landing;
- a deep room for each family.

All six share one rhythm and one set of rules (`PuzzleRooms`).

## The families

| Family | Early room (slot) | Deep room (slot) |
|---|---|---|
| Light | Borrowed Light (120) | the lamp rooms (123) |
| Lift | the Service Lift (121) | the twin lifts (124) |
| Landing | the Turning Landing (122) | the turning stair (125) |

Each early room teaches one household mechanism. Its deep room combines that mechanism across spaces. All six are gray slots appended to column 15/16 of the slot grid; no earlier slot moves.

## The rules they keep

- **Everything in view.** Every action answers at once, in sound and in the room itself.
- **Nothing counts down, hurts, or is used up.** Nothing is carried in or out. A mistake is never a fall onto stone: where there is a drop, there is a cistern or a shaft of water.
- **Shared and kept.** Each room is shared and keeps how it was left. Solved, it stays solved for everyone, through reloads and rebuilds.
- **The way back always opens.**
- **Not a story.** No Witness source and no account. Witness stays 55 / 42 / two kinds / three endings.
- **Left alone by the House's general passes.** `ScenePolish` skips all six, so nothing is dressed into a mechanism.

## Pacing: a story, some wandering, then a puzzle

`PuzzleRooms.weight` is a reader's weight for a puzzle room among the gray halls. By depth alone it is 0.

- **When a puzzle is offered at all.** Only once the reader has resolved a story since their last puzzle (any puzzle) and is three crossings past it. The offer is 40, shared among the puzzles the reader could have at that depth, so together they are never more than one room's worth.
- **Early rooms.** From the third crossing, never at stone depth.
- **Deep rooms.** Only from the strange depths, and only once their own family's early room is solved.
- **Solved rooms.** Weight 1, six or more crossings after the reader's last puzzle: an occasional pleasant room on the way.
- `PuzzleRooms.arrived` records each reader's visits, stories and depth on arriving in any puzzle room.

## The Service Lift

A whitewashed pantry.

- **The cage.** A dumbwaiter cage rests in the only opening in the far wall, cold air coming round it.
- **The counterweight.** Beside it, in an open recess, the counterweight pan hangs on a chain. The chain runs over a pulley wheel and down to the cage.
- **The weights.** Three cast-iron weights stand on the counter, stamped with bars: two ones and a two, in an order the world chooses.
- **Using it:**
  - A hand on a weight sets it in the pan. The pan sinks a step for every bar, the chain runs over the wheel, and the cage climbs as far.
  - Two steps clear the opening. A catch clicks and holds the cage there for good.
  - A hand on the pan lifts the last weight back out to its own place on the counter.
  - The cage never comes down onto anyone standing under it.
- **The far door.** Behind the cage, a stone passage leads to an ordinary far door.

## The Turning Landing

- **The stairwell.** A stone stairwell over a cistern. A stair climbs from the door to a ledge. Across the stairwell, a bridge leads to the far door.
- **The landing.** Between them stands a square landing, five boards across on a post, railed on every side but its gaps. It has three gaps, and it starts open to the stair or to the bridge, never both.
- **The handle** by the stair top turns the landing a quarter, scraping. Anyone standing on it turns with it and is set a step in from any rail they were carried into. One turn lines it up; it lines up every other quarter.
- **The scrapes.** The boards are scraped in arcs where its corners swing past. Its gaps' boards are worn where feet cross. A railed nook either side gives somewhere to stand and think.

## The lamp rooms (Light, deep)

Four dim rooms round a cross of walls, off a narrow hall from the door.

- **The first room.** A stage lamp behind its shutter and a dressing mirror on a pivot, bolted to the floor: only the glass turns.
- **The window north.** In the north wall is a leaded window. Through it, in the room beyond, stands a second mirror.
- **The long way round.** That room has no door from the first. It is reached the long way: through the east room, into the far room, and back west through its door.
- **The window east.** In the wall between those two rooms another window shows the second mirror from the third. The brass sun is on the far room's wall, beside a handleless iron door.
- **Solving it.** Turn each mirror an eighth at a time until the beam goes lamp, window, mirror, window, mirror, sun. The beam passes through the windows (`BorrowedLightBlock.Kind.WINDOW`, glass to a body). Each mirror starts two to six eighths from its aim.

## The twin lifts (Lift, deep)

A tall brick lift hall on three floors. From the door you see the far door high up across the hall, at the end of a gallery.

- **The lifts.** Two goods lifts stand in open brick shafts, one each side, hung from a single counterweight. Whatever one rises, the other sinks; the two always stand two floors apart in all.
- **Which floors they serve.** The west lift opens onto the ground floor and the middle walkway. The east lift opens onto the walkway and the top gallery.
- **The cords:**
  - Each lift has a pale cord to go up and a dark cord to go down.
  - Each landing has a brass-ringed cord that calls its lift a floor toward it.
  - Every cord hangs from an iron arm off the wall beside it.
- **The way.** Call the west lift down and ride it to the walkway; the east lift has come down to meet you there. Ride the east lift up, and the west lift goes back to the ground.
- **Nobody stranded:**
  - A lift never comes down onto someone below it in its shaft. Someone at the very bottom is safe under a lift stopping a floor above them.
  - The bottom of each shaft holds water.
  - The west shaft's bottom opens onto the floor.
  - The east shaft has a ladder in a slot in its back wall, up to the walkway's height, with its own call cord in a niche beside it.
- **After the gallery.** Once anyone has stood on the gallery, ladders unroll from the walkway and the gallery, so the room is quick to cross again.

## The turning stair (Landing, deep)

A tall stairwell over a cistern with two landings of two gaps each, geared together.

- **The geared handle.** The handle by the lower landing's stair turns the lower landing one way and the upper landing the other.
- **The second handle.** It stands in the lower landing's west nook and turns the upper landing alone.
- **The way through.** It needs the lower landing open south and east and the upper open east and north at once. Of the sixteen arrangements, exactly one goes through.
- **Why it takes thought.** The world never starts it where the geared handle alone can line it up. The nook with the second handle opens only when the lower landing is turned away from the way through.
- **The intended solve:**
  1. Turn the lower landing open to the west.
  2. Set the upper landing from the nook.
  3. Turn the geared handle until both knock into line.
- **The rest of the stairwell.** Broad ledges and nooks, a walkway east and a second stair up to the upper landing's ledge; rails on every open edge.

## Build and assets

- **Layout 55** (`LabyrinthBuilder.VERSION`). Early rooms stand with the halls (required depth 0); deep rooms from the strange depths. Saved worlds build each new slot once (`builtVersion < 55`); Borrowed Light is not rebuilt.
- **Protocol 104.** One new block, `puzzle_fitting`, with these kinds:
  - weight, pan, cage, pulley, handle;
  - cord up, cord down, call;
  - platform, scraped boards, worn boards, counterweight.
  
  Each kind has a facing and a stage (0..3). Borrowed Light's block gains the window kind.
- **Art.** `tools/draw_puzzle_fittings.py` draws the fittings: 13 textures and 22 models and states. `--check` verifies them. `tools/draw_borrowed_light.py` adds the leaded window. No existing PNG changes.
- **Rails.** Rails stand on a board lip wherever nothing else holds them up (`TurningLandings.fences`), and handles stand on posts down to the cistern floor.
- **Prose names** for all five new rooms (`StaircaseProse`).

## Tests and proofs

All in `ExplorationPassTests`, each room in its own batch:

- **`theServiceLiftsWeightsRaiseTheCageOffThePassage`:**
  - the cage blocks the only way to the far door;
  - a light weight lifts it a step and does not solve;
  - the cage will not settle on someone under it;
  - the pan returns a weight to its own place;
  - the heavy weight clears the passage and the catch holds;
  - all three weights in the pan;
  - kept through a reload and a rebuild.
- **`theTurningLandingComesRoundToTheFarDoor`:**
  - rails exactly where there are no gaps;
  - riders carried round and set in from the rail;
  - the far door walkable exactly when the landing is in line, over four more turns;
  - kept through a reload and a rebuild.
- **`theLampRoomsCarryTheBeamThroughWindowsToARoomReachedTheLongWay`:**
  - every mirror walkable, the second and third only through their doors;
  - mirrors bolted;
  - the beam through each window to each next mirror, then onto the sun;
  - the iron door's refusal, then the door opening;
  - kept through a reload and a rebuild.
- **`theTwinLiftsAreRiddenInTurnToTheGallery`:**
  - the gallery out of reach at first;
  - calling the west lift down;
  - riders carried a floor at a time, and no second order while the lifts move;
  - across the walkway, up the east lift, to the far door;
  - the gallery solving and the ladders, with their rails rejoined;
  - someone at the bottom of the east shaft safe under a lift stopping above and refused when it would come further, and the ladder out;
  - kept through a reload and a rebuild.
- **`theTurningStairNeedsBothHandles`:**
  - the geared handle alone never lines it up;
  - the intended solve;
  - all sixteen arrangements, with the far door walkable in exactly one;
  - kept through a reload and a rebuild.
- **`puzzleRoomsComeAfterAStoryAndSomeWandering`** replaces Borrowed Light's own pacing case: the shared offer, early and deep gates, each deep room waiting on its own family, and revisits.
- **Walkability.** The walk checks use a block-collision walker: stepping or jumping one block, dropping no more than one, never climbing.
- **Counts.** `HazardVarietyTests` and `LabyrinthTests` count the new slots and the opening gray pool; every puzzle fits its slot.
- **Native architecture views.** The six rooms are exported in their solved state: 111 earlier views, plus every uncanny room and the third-floor bathroom. The tall rooms are cut higher (landing 7, lifts and stair 11). CI requires the six images.
