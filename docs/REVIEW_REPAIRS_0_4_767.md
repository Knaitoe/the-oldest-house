# Review and repairs, 0.4.767

The owner asked for a thorough review of the project, with repairs wherever one was warranted. This release adds nothing to
play. It records what the review found, what was repaired, and what was looked at and left alone.

The review has two parts. The first read the seven commits of 0.4.766 line by line. The second reads the whole code base by
kind of fault rather than by file: faults that stop the server, readers who can be left unable to leave, things taken from a
player and not given back, state that outlives its world, the two threads of a single-player game, the mixins, what the
server accepts from a client, the cost of each tick, what is saved against what is loaded, and state keys that are read and
never written. Its repairs are added below as each is confirmed against the code.

## Repairs

### A half-built room from an earlier version no longer stops the server

A scene is built over several ticks from a recorded plan, and the place it has reached is saved so that a restart can carry
on. The saved place counted the plan's commands, and `BuildBlocks.Plan.resume` threw if the count differed from the plan in
hand. It differs whenever an update changes that scene: 0.4.766 added two commands to the porch. The throw came out of the
server tick, so the server stopped, and stopped again on every restart for as long as that save was used. Five builders
resumed this way: a reader's quiet rooms (`HouseReadSites`), the silo, the car among the pines, a reader's Drisking
(`BorrascaSites`) and every copy of a scene (`SceneInstances`).

A saved place that does not fit the plan is now set aside and the build starts from its first command, with a line in the log.
Starting over is safe for the blocks, since a command only sets a cell that differs. A step that installs something after its
blocks (a book on a lectern, the text of a sign) runs a second time if the earlier build had reached it.

Case: `HallwayCarveTests.aCursorSavedAgainstAnotherPlanStartsTheBuildOverInsteadOfStoppingTheServer` (a saved place that fits
is taken up and only the later commands run; one from a plan of another length, or past either end, is refused and the whole
plan runs).

### The porch's small shoes

- **They stopped a reader on the steps.** The strap, the shoes and the milk had been left out of the list of small things a
  body passes through. The shoes stand above the top stair, and their boxes sat higher above the lower tread than a reader
  can step, so anyone walking up the right-hand side was stopped and had to jump or go round. All three now stop nobody.
- **Their heels hung in the air.** A stair's upper tread is only its back half. The shoes ran past it, so their heels stood
  out over the lower tread with nothing under them. They now stand wholly on the upper tread, a little shorter, still side by
  side with their toes to the wall. The model and the shape come from `tools/house_read_porch_art.py` as before.

The porch case now also checks that the shoes end at the tread's edge and are stepped past.

### A porch built ahead gets its strap and shoes

A reader's quiet rooms are built ahead of them. The notes on `HouseReadSites.LAYOUT` say that a room built ahead under an
older layout, and never entered, is built again. Nothing did that any more: the number was written with each room and never
read, and 0.4.766 did not raise it. A reader whose porch was already built under 0.4.765 would have found no strap and no
shoes. `HouseReadSites.prepare` now builds such a room again, and the number is raised. A room the reader has been in is left
as they knew it.

### Fireflies whenever the reader is outside

The as-built note says the fireflies are sent "while the reader is outside". They stopped for good the first time the reader
went indoors, so the yard was bare on the way out. They are now sent whenever the reader is outside.

The note also says they drift away from the house. That is true of where they appear (ahead of the reader, on the side away
from the house) and not of how each one moves: the game scatters a dust mote whichever way it is sent. The comment now says so.

### Saved things this version cannot read no longer stop the server

The same kind of fault as the half-built room, found by reading every place where saved data is turned back into something on
the server's tick. One can be reached in ordinary play. The rest need a save written by another version or edited by hand, and
are closed because the cost of each is a server that will not start.

- **The teddy on the stairs.** `HouseRemembers.toy` remembers the stair tower's treads for as long as the reader stays in the
  manor, and asked the next tread which way it faced without looking whether a stair was still there. The tower is ordinary
  manor structure and can be broken. A missing tread stopped the server on the next House day. The treads are now found again.
- **A staircase room whose saved template does not fit.** `StaircaseLeakRooms.Template.load` throws when the saved cells do not
  add up to the room's size, and every saved room is asked for from the tick. Such a room is now skipped, with one line in the
  log.
- **Names and numbers that no longer name anything.** A saved kind of winter-lake prop, a saved kind of Drisking pile, the
  quiet room a reader was last in, and a staircase room's kind were each decoded with no answer for a value this version does
  not have. Each is now dropped or ignored.
- **An item this version cannot read.** Such an item comes back empty, and saving an empty item throws. The Goatman's supper pan
  and the phone at the lake both saved what they had just read without looking.
- **A fitting that is not there.** The Truant lift's brake and the Work Print tripod were asked for their stage without a look
  at what stood in the cell.

### The cost of each tick

Nothing here changes what happens; each change does the same work for less. None of it could be measured in this session, so
the sizes below come from reading the code.

- **Which place is this?** `LabyrinthPlaces.placeAt` worked out the box of every place (some 250, five small objects each) on
  every call. It is asked a dozen or more times a tick for every reader in the House, twice a tick for every player anywhere,
  and several times a frame by the client's fog. The boxes depend only on where the House stands, so they are now worked out
  once.
- **Has this reader met the finale?** Asked about fifty times a tick for every player online, wherever they are. A reader with
  no phase written (nearly all of them) made `Enum.valueOf("")` throw each time, and the reader's record was copied first to
  ask. Neither happens now.
- **Is this reader in that story?** `LiteraryVignettes.inside`, and the same question in the novel's stories, the classics and
  Drisking, found the scene's base (which copies the scene's record of copies) before asking the one cheap thing, whether the
  reader is in that dimension at all. Some forty tick handlers ask it of every player. The dimension is now asked first, and
  `current` asks whether the reader takes part once, not four times.
- **Records copied whole.** The novel's stories copied every reader's record three times a tick for each reader to read and
  write one; the phone at the lake did the same for every player in every world. They copy the one record. A reader who has
  seen the House no longer has the list of everywhere they have been copied each tick to learn it again.
- **Words over a head at the camp.** Looked for across two hundred blocks each way on every tick of every server. They are now
  looked for only while somebody is in the interior.
- **Two shape caches.** The roundabout and the dome kept their shapes in a map that the server's thread and the client's both
  fill in a single-player game. Filling such a map from two threads at once can throw; they are now maps made for it.

Not done, and the largest thing left: 154 shape methods in 119 blocks build their shape afresh on every call, and the game
asks for a block's shape whenever anything moves beside it or a line of sight crosses it. Five blocks already keep theirs. The
change is the same for each block, but there are many of them, and three depend on who is asking.

### What the server accepts from a client

Every message a client can send was read. The checks are thorough; these are what was left.

- **A game at the drugstore's cabinets never ended if the client went away.** The game ended only when the reader's client sent
  its score. A client that crashed or timed out with the screen up never did, and every cabinet then answered "You're already
  playing." until the server restarted. A reader who leaves is no longer playing.
- **Two readers could each take the oars.** At Pinehook's rack, two readers offered the oars at the same moment both got a
  pair, and the same for the boat hook. The rack is looked at again when the answer comes.
- **A reader who has died answers nothing.** An offer outlived its reader's death, and what follows an answer acts where the
  reader stands. A prize could likewise be taken off into a dead reader's hand and lost.
- **The payphone remembered every wrong number.** A number not in service costs nothing, and each one tried added a key to the
  reader's record without end. Only a number that rang is remembered.

### Tests

- **The three logout cases logged the reader out twice.** The Stillness, foyer and Last Stop cases of 0.4.766 posted the
  logout event by hand and then removed the player from the list, which posts it again
  (`PlayerList.remove` → `EventHooks.firePlayerLoggedOut`). They now leave the list once, as a real disconnect does.
- **The porch's particle check passed on any particle.** It counted every particle packet, so the moths alone satisfied
  "fireflies and moths". It now counts the fireflies and the moths separately and needs both.

### Housekeeping

- `TensionSafetyTests.java` was stored with Windows line endings while the repository's rules ask for Unix ones in Java
  files, so every fresh checkout showed it as changed. It is stored as the rules ask; nothing else in it changed.
- A comment in `HouseReadSites.porch` still said there was no glow over the yard. It now points at the fireflies.

## Looked at and sound

- **Commands.** Every command hangs under `/oldesthouse` or its alias `/ghosthouse`, both of which need operator level 2.
  The proof commands are only registered when their system property is set.
- **Asset checks.** All 186 generator and verifier checks of the quick gate pass, and the 24 cases for the playtest-log
  summary.

## What I could not check

- Nothing here was looked at in a running client. The shoes' new place was checked by their shape and by reading the model,
  not by eye.
- The full suite was not run locally; the cases named above and the suites they sit in were.
