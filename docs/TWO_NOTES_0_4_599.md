# Two parked notes (0.4.599)

The owner's two notes of 9 November, parked until the list reached them:

> once a player fails a vignette that simply bounces them to the exit, if the vignette is retriable, it should disappear and lead to a normal hallway.

> <-this hazard map is impossible to pass.

The second came with a screenshot: a flooded, tiled room, the reader under water, steps up the walls.

## 1. A story that fails a reader is gone from its door

### Before

A story that fails a reader puts them at its door, or throws them back to the hall under a fade. The door they took still led to it.

- Thrown back to the hall, the reader could step straight back in.
- Walking out, they found the door quiet (*"… is quiet behind this door. Walk a little way off, then come back."*), and having walked off and come back it led into the same story.

### Now

- The story marks the visit failed (`VignetteGate.failed`).
- When the reader is out of it, the door they took is dealt an ordinary hall at once (`LabyrinthDoors.gone`, `LabyrinthDealer.openWayOn`), and the hall it stands in remembers it so, so the same road finds the same hall behind it. "Out of it" is any of the three roads out:
  - walking out by the entry (`VignetteGate.departed`);
  - being thrown back under a fade (`LabyrinthDoors.sendBack`, `VignetteGate.sentBack`);
  - going on by the story's own way on (the door behind them changes; their way back still ends at it).
- The story is not finished and not struck off. The dealer gives it again, later, behind some other door.
- No line is shown. The playtest log gets `story_put_out` with the place and the depth, and the summary (`tools/summarize_playtest_log.py`) counts them by place. A reader thrown back under a fade leaves no `story_leave`, so this is the only record of it.

### What stays as it was

- A reader who steps back out of their own accord still finds the door quiet.
- A finished story's door is still a plain passage.
- A door the reader's own wedge holds, a door opened by command and a door with a fixed destination keep what they had.

### Which failures count

Twelve handlers, each of which puts the reader out and leaves the story to be tried again:

- the uncanny rooms' catch (`UncannyStories.caught`: the boarded house's family, the diner, the tunnel, the cellar) and the monitor room's lost night (`SplitFeedNight.slaughter`);
- the foyer's intrusion (`FoyerEncounter.intrusion`);
- the Polaroid trail, Dead Air, the Left-Right game (the lunge, and the gaze that takes), the deer road;
- the floorboards' heart, Polybius, the Winchester house's walling in, Usher's burial, the lookout's shattering.

Left alone, and why:

- failures that kill (hide-and-clap, the cousins, State Route 9): a death takes its own road, and the gate is cleared when the reader comes back;
- returns that are not failures: the way out's waking, the back seat's ride home, the silo, the marsh supper (an ending), the end of a take on the Ghosts set, the last orders' front door, the work print's take;
- Borrasca's guard and the tall grass's "Get in and go back.", which belong to those stories' own telling;
- the dark halls' line and stillness, which are trials and not stories;
- Devil's Rock (the chase goes on), the Pitt (it resets inside the room), bike night's Hollow.

## 2. The rising stair

### Why it could not be passed

Four things, the first of them enough on its own.

1. **The room was taller than the slot it is carved in.** Interior rooms stand in slots twenty-four rows high, one on another in a column, and each slot is filled solid before its room is carved. The stair was twenty-seven rows from floor to vault and stood on its slot's fifth row. Its top seven rows lay in the slot of the room over it: the last treads, the landing at the head, the far door and the vault. Which of the two was carved second depends on how deep the readers have gone.
   - Carved first, the stair lost those rows when the room over it was filled: a shaft whose stair ends in rock, with the water coming up.
   - Carved second, its walls and vault stood across the first thirteen blocks of the room over it.
   - Either way, a reader above the eighteenth block was, to the game, in the room over it. The stair stopped being ticked: its water neither rose nor drained.

   No case caught it because every case builds one room at a time.
2. **The undertow took the reader's walking with it.** The server pushed a reader in the water down and sent their whole motion to their client every tick. On a real client that replaces the reader's motion, sideways too, so from the moment the water reached their feet they could barely move: they could not walk the treads under it, reach a pocket, or go back down to the door, all of which the room's notes promise.
3. **The water came at once.** It rose on the room clock's next thirtieth tick after anyone stepped into the shaft, the shaft floor included: a second and a half at most.
4. **A gap was a whole block up** as well as a stride across. The notes say half.

### What changed

- **The room fits its slot.** It is twenty-four rows from floor to vault and stands on its slot's first row.
  - Thirty-eight treads climb nineteen blocks, where forty-four cells used to climb twenty-two. The stair still goes round the shaft from beside the door, forty-four cells with six of them missing, a little under one and a half times round.
  - The landing at the head and the far door are where the plan had them, three blocks lower. Three clear blocks stand over the landing.
  - The water fills eighteen blocks and stops short of the last two treads.
  - The pockets open off the ninth and the twenty-fifth cells, at the fourth and the eleventh block up.
- **Every interior room stands in its own slot.** `LabyrinthPlaces.floorInSlot` puts a room's floor on the fifth row of its slot unless the room is too deep or too tall for that, and then as near the fifth as keeps the whole room inside. The stair was the only room that reached above its slot. Ten reached one to four rows below theirs, into the slot of the room under them, and are lifted by as much:

  | Room | Rows |
  | --- | --- |
  | the cistern hall, the fuse rooms, the gravity drift, the shelter | 1 |
  | the header tank, the last swimmer's pool, the layered man's cellar, the vacant flat | 2 |
  | the header tanks, the Truant's ledge | 4 |

  Every other room stands where it did.
- **The undertow is the reader's own client's.** While a reader is on the stair the server sends a lease (`HazardViewPayload.undertow`, every five ticks). In the shaft's water their client lets them rise no faster than twelve hundredths of a block falling, a tick (`TrialRooms.pulled`, `PULL`). Sideways they keep what they had. Out of the shaft, in a pocket or on a landing, nothing pulls.
- **The water waits.** It comes sixty ticks after a reader is first on the treads (`HEAD_START`), and then a block every thirty as before. Standing on the shaft floor brings none. Once the shaft has drained with nobody on the stair, whoever climbs next has the same wait.
- **A stride over a gap is half a block up**, like any other step, and straight: no gap is at a corner or beside another.

### What stays

- The water's rate, the drain, the pockets' air, the drowning line (*The landing again, and the taste of the water.*), the mark, the depth it is dealt from and the art.
- **Witness:** unchanged.
- **Protocol:** 195 (the lease is new).
- **Layout:** 138. New builds only: see below.

## Checks

- **A real body on the stair** (`HazardVarietyTests.aBodyClimbsTheRisingStairAndOneTheWaterCatchesHasItsWaysOut`). A player's body is moved the way a client moves its own: turned toward the next tread with forward held, the jump key at a gap, then the game's own player tick; and after each tick the undertow by the rule the client applies.
  - It opens the door at the foot by hand, climbs all thirty-eight treads over the six gaps and stands on the landing at the head within reach of the far door, unhurt, in 241 ticks (twelve seconds).
  - Its feet stay dry. The water comes up five blocks behind it and is never nearer than five blocks to its feet.
  - With the body at the head, the shaft drains.
  - Caught at the foot under two blocks of water, it walks up the treads under it, comes out and jumps the first gap.
  - On the shaft floor under eight blocks of water it holds the jump key for two seconds and does not rise. With the rule switched off it rose 6.3 blocks in the same two seconds.
  - In a pocket, under water, the same body comes up into the pocket's air.
  - On a tread under water it cannot cross a gap: it goes down where the tread is missing.
  - From there it walks back under the water to the door at the foot and out onto the dry landing with air left, and the water goes down.
- **The stair's own case** (`theRisingStairFloodsBehindAClimberAndPutsTheDrownedBackOnTheLanding`), rewritten: the plan tread by tread, every step and every stride half a block up; the room's rows inside its slot, and a reader at the head resolving to this room; no water for a reader on the shaft floor; the wait, then the rise; the lease with the pull; no motion sent by the server (it fails with the old push put back); drowning, draining, the pockets and coming back as before.
- **Every interior room in its own slot** (`everyInteriorRoomStandsInTheSlotItIsCarvedIn`). Before the rule it named the ten rooms above; after it, none. Ordinary halls are checked to stand on the fifth row as they did.
- **The doors** (`EntrancePlaytestTests`):
  - a reader steps out of a story and finds it quiet; goes in again, is failed, walks out, and the door is dealt a hall that takes them on;
  - a story that sends a reader back without failing them is still behind its door; failed and sent back, it is gone; failed and gone on by its own way on, the door behind is dealt a hall and their way back still ends at it;
  - the same by a story's own hand: the boarded house's family touch a reader, the room puts them at its door, they walk out, and the door opens onto a hall.
- **The summary tool** (`tools/test_summarize_playtest_log.py`): a log with three `story_put_out` lines is counted by place, and a log with none says nothing.

### What was run here

Local runs on the release's tree, before the workflow's own:

- every case of the exploration, uncanny and architecture namespaces (240), which hold the stair, the slot check and the cases of seven of the lifted rooms;
- every case of the house-read, two-reader, service and uncanny-environment namespaces (122): the shelter, the three door cases, and two readers put through the pool, the flat and the cellar;
- forty-six more, chosen for what the changes touch: the pool's and the flat's own cases, the doors that are quiet behind a reader who stepped out, the way on, the fade that throws a reader back, every case that checks a slot against its neighbours, and the cave's;
- the workflow's tool checks under its own Python, the package and branding checks on a built jar, and the client's proof chain.

## Not checked

- **No client has run the undertow.** The case walks a body on the server by the client's rule. What is the client's alone is small: the lease arriving, `HazardClient` holding it sixteen ticks, and the call to the shared rule. Nobody has climbed the stair in a client.
- **No world with every room carved was built** to look at the stair's head in place. The slot check is arithmetic on the rooms' boxes.
- **The ten lifted rooms were not looked at.** Their own cases pass where they stand now (above); nobody has walked them.
- **Eleven of the twelve failure handlers** are hooked by one line each and have no case of their own; the boarded house stands for them.

## For the owner

- **Was it this room?** It was picked from the description of the screenshot, which is no longer to hand. The stair is the only hazard room with steps up the walls of a flooded shaft, it was added on 8 November, the day before the note, and a body put through it found it could not be passed. The other flooded hazard room, the bent cistern, is tiled the same blue-green and has been in since October; it was not looked at here. If that one was meant, or another, say which.
- **Old worlds.** A world made before this release keeps its rooms where they were carved, and eleven rooms' floors are now reckoned one to four rows from there: the stair and the ten in the table. Make a new world.
- **The stair is three blocks shorter**, to fit. Keeping it at twenty-two would take two slots.
- **The water does not press a steady climber.** It was five blocks behind a body that never stopped. The dial is `RISE` (thirty ticks a block), which is as it was; `HEAD_START` (sixty ticks) and `PULL` are mine.
- **A reader with water over their feet cannot jump a gap**, as the room's notes say. They can walk up out of it, wait in a pocket, or go back down.
- **A failed story's door says nothing.** It simply opens onto a hall. A line could be added.
