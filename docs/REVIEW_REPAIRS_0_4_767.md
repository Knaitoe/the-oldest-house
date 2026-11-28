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

- **A block's shape, built once.** The game asks a block for its shape whenever anything moves beside it or a line of sight
  crosses it, and does not keep the answer. 103 of the House's blocks built theirs afresh on every such call, from boxes
  joined together (the hotel's props walked every box of their shape and joined them again each time). Each now builds a
  state's shape once and keeps it, as five blocks already did. The change was made by rule, to the methods whose body uses
  nothing but the state: the body moves unchanged into `shapeBuilt0767` and `getShape` looks the state up. The rest were
  left: they return a shape already kept, or their collision depends on who is asking.

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

### Readers who could not leave

The owner's standing question for endings is whether every surviving reader can leave. These are the places where one could not.

- **Back in the game, standing in a story where nothing runs.** Thirteen stories keep what is going on (the day, the night, the
  run) only in memory. They make it when a reader arrives by a door and drop it when the server stops or the place has stood
  empty a while: Fallow, The list, Last Seen, Pinehook, the Last Crossing, The Drawing, the corner house, the black city, Kept,
  Last Orders, the marsh supper, the tall grass and Growing Things. A reader who logged out inside one (in a single-player game,
  every save and quit) came back to a place where the people stood still and nothing could be finished until they walked out
  and in again, and nothing told them to. In three the way out was shut as well, with nothing left to open it: the station's
  cellar after the light, where the doors are rubble and only the buried days dig them out; the corner house, shut again once
  its street has stood empty; and the seed house on the list's night, where the saved night still held the reader and no longer
  moved. Such a reader now arrives again, as by the door, once the place is loaded (or after ten seconds), and the place
  begins again (`LiteraryVignettes.returned`). The notes of 0.4.760 said a reader back late in Fallow or Last Seen "finds a
  fresh day rather than a held one"; they found no day at all, and now find a fresh one.
- **The night's meal, eaten out of reach or lost.** In the rabbit story the night ends only when that night's meal has been
  eaten, and the meal was handed over once. It was handed at ten blocks from the shelter and counted only at eight, so one
  eaten where it was given could count for nothing; and one dropped, lost with a death or left on the ground by a full pack
  was gone. The night then never ended: the reader was held, kept hungry, and could not die of the cold or the hunger. The
  meal now counts wherever it could be handed, and one that is neither eaten nor about is handed over again after ten seconds.
- **An open hatch nobody shuts.** The mending passage's hatch was remembered only by the tick that opened it. A game that
  stopped in the two seconds it stood open left it open for good, and the passage was a shaft: a reader walking back to the
  House's door fell, was refused the fall and was mended again, and could only leave by the far door after the whole trial.
  A hatch found open is now remembered and shut.

### Things taken and not given back

- **The chest of drawers, at every ending.** What a reader carries in goes to the drawers when they cross the hall, and the
  drawers open only to a hand in the front hall. Every ending puts the reader outside straight from where it ends and keeps
  them out of the House from then on; the escape takes the House away for everybody. The reader was told "Your own things are
  still in the chest of drawers", after the Witness ending's "Your belongings.", and could never go and get them. Once a
  reader is outside and the House is closed to them, the drawer, the thing on the hall stand and its arrows come to them:
  into the pack while there is room, the rest at their feet, where it does not despawn and only they can take it. This is done
  for all three endings, the reader who died in the finale included. If the House is meant to keep that reader's things, the
  one test to change is `FinaleController.lockedOut` in `HouseKeeping.handBack`.
- **What was in the hand when something was borrowed.** Mark's work case and the spare chair, and the furniture of the vacant
  flat, are put into the hand, and what the hand held is kept in the reader's record. It was given back only while the
  borrowed thing was still carried. Tossed, or dropped by a death, it was not. In the flat it was put back into the dead
  reader's hand, after the game had dropped what they carried, and was gone at the respawn. It now waits for the reader who
  respawns and is given back whenever nothing borrowed is carried.
- **A stack on the cursor when the staircase takes the reader.** The staircase's rooms keep the reader's inventory and put it
  back on return. A stack on the cursor or in the crafting grid was not part of what was kept and was lost. The reader's own
  screen is now closed first, which puts those stacks in the inventory.
- **A set-down piece, destroyed by anything but a hand.** A story piece gave itself back only when a player broke it. Water
  or lava running over one, a bucket, an explosion, or a piston pushed head-on into one of the three the game counts as
  breakable destroyed it and dropped nothing. A keepsake is given once. A piece now leaves itself however it goes.
- **Whose a dropped thing is.** A reader's own thing dropped in the manor was followed in memory by the item's id. After a
  restart it was nobody's: the hall let it past, and beyond the hall whoever picked it up kept it. It is now marked on the
  dropped thing itself.
- **The finale rehearsal command** put the operator's held thing away before filling their hands; it could go into the hand
  slot just emptied and be replaced by the weapon. The hands are filled first.

### What outlives its world

In a single-player game the program outlives the world: what a story keeps in memory is met again in the next world opened
that session, and a new server's tick count starts again at nothing while the old stamps stay. The stories had methods to
forget all this, and only the tests called them.

- **After a save and quit, doors were silent.** A knock made no sound and brought no reply, and listening at a door played
  nothing, until the new session had run as long as the old one. The same stale clocks kept a sphinx room from taking any
  answer in a world with an earlier time, left a door on a closer open (which solved its room), and stopped the tally
  counter's click and the lookout's bearing. A house visited in one world looked for its lamps where they stood in the last.
  All of it is now forgotten when the server stops (`LiteraryVignettes.stopped`, from `HouseLifecycleEvents`).
- **The way out's meadow after a relog.** The client is told it is in the meadow only when its reader is first counted there,
  and a reader who left the game was still counted. Back in, they had no meadow fog, so the edge of the built land showed.
- **A candle's light left in the dark.** The dare's candle lights an invisible block at the reader's face. A reader who left
  holding it lit, or a server that stopped, left the block in the world for good, one more each time.
- **Chunks held for nobody.** The labyrinth's builder kept a slot's chunks loaded when the slot stopped being due (its deepest
  reader logged out part-way); the list's runners kept the ground under them loaded after the town emptied mid-chase; the
  Truant's move kept its room loaded when its reader left before it finished. Each is now let go.

### The host's client and the server inside it

A single-player or LAN game runs the server inside the host's program, and both share the same classes. Three places let the
host's own state decide things for the server, so a LAN game behaved differently from a dedicated one.

- **Rain, on the server, wherever the host sat in the diner.** The client's storm patch on `Biome` says it is for the render
  thread only, and two of its seven tests did not ask. While the host was in the second waitress's diner every dry biome
  counted as rained on for the server too: lightning could strike there, fires went out, cauldrons filled. And the car among
  the pines read the client's world from the server's thread.
- **The sleeping king's country.** The client filled the server's own record of how much of a thing each reader is, and
  struck readers out of it when its news lapsed. Two seconds after the host left the country, the guests there counted for a
  moment as their whole selves, or left still rooted. The client has its own record now.
- **Another reader's cast could not be struck on a LAN game.** The client's rule that somebody else's cast does not stop this
  reader's hand also ran for the server, answering for the host.

### People and things made twice, or never gone

- **Mr. Harrigan's ghost, left behind.** The ghost is a zombie that cannot be hurt and never despawns, and the only record
  of it was in memory. The record was dropped when the server stopped (after the world was saved, so the zombie stayed in
  the save) and whenever the ghost's chunk unloaded. Each one left that way stood in the world for good; a third-strike
  ghost kept its ten damage; and a reader who outran one sent for a player got another, and another. A ghost that comes back
  from disk with no record naming it is not let back in.
- **On Peaceful, the game removes a monster on its first tick**, asked to persist or not. The crossing's drowned and the
  ghost are zombies: the nights made and lost their drowned without end, and with somebody sent for a new ghost was made
  every half second all night. Both now stay.
- **The Mother's kept pets, doubled.** The den laid out its kept pets as the reader arrived, before the den's own saved
  animals had loaded when it was come to from far off: none were found, so each was made again beside the one about to
  load, and again on the next such visit. Nothing is looked for now until they have loaded, and a second body for the same
  pet is taken away.
- **The courtyard's fish, one more each visit**, for the same reason. The basin is looked at once it has loaded; a fish there
  is the fish.
- **Bike night and the tall grass** clear whoever was left standing from last time by looking, and looked with only the
  blocks loaded. They now wait for the entities, as the other stories do.

### The newer stories

- **The porch, every fifteen seconds.** An unanswered whistle is taken back for five seconds of every fifteen so that it can
  blow again, and the yard was handed that: for those five seconds it was "before the whistle". The dog left the den and
  came back across the lawn, the one cooking went back to chopping with their head down, and the count toward the hand at
  the glass and the plate carried out began again each time and never arrived. The yard is now told when the whistle first
  blew.
- **Gus's pegs.** A reader who logged out on them took Gus and his bike out of the night for everybody (the game removes a
  mount with its only player, and everything riding it); they now step off first. A reader on the pegs went through the
  fold with him with no mist, no sound and no count of it. A reader caught while on the pegs stayed on them, told they had
  been thrown.
- **Five pieces nobody could see.** The sets take every block of a story that has no item, and five of those are invisible:
  a lamp's beam, a shadow, the brook's edge, and the car's and the doe's weight on the road. Each had a recipe and an empty
  button in the stonecutter. A beam was gone a second after it was placed; the brook's edge, once placed, could not be
  aimed at, broken or built into. They are out of the sets, and the set case now refuses an invisible piece.
- **A keepsake key in a Crafter.** A keepsake key works for its own reader only, and that was asked only at a crafting table
  and the inventory's grid. In a Crafter anybody's key made the cabinet, the pool table or the dartboard and came back, as
  often as wanted. A Crafter now makes nothing with one in it.
- **Two accounts that left a sentence out.** The Last Crossing's first night keeps how a reader got free as a word and read
  it as a flag, so "Somebody reached down through the landing's rail and pulled me up over it." and "Somebody beat it off
  me." were never written. Tilly's account says Callum was on the step if that was kept, and nothing kept it.
- **A plate that kept its meal.** A client ignores an empty update, and an emptied plate saves as nothing, so the meal went
  on being drawn on the diner's plate after the reader had taken it up.
- **A mistyped name on Mr. Harrigan's phone stopped the server.** A name that is no creature is looked up as a player, and a
  name the server does not hold is looked up over the network, with waits and retries, on the tick. It is now asked off the
  tick and answered when it comes.
- **Rain, asked for every column.** The weather of four stories and the lake's sky each looked up which place the reader
  stands in whenever asked, and the game asks whether it rains once for every column of rain it might draw: hundreds to
  thousands of lookups a frame. The place is now looked up once for each block the reader moves.

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

## Found and left, for the owner to decide

Each of these was confirmed by reading the code and is left as it is, because the right answer is a matter of what is
meant, or because the change is large enough to want its own release.

- **A crash between a logout and the next autosave can lose what the House holds.** A player's inventory is written when
  they log out. The House's records (the chest of drawers, a borrowed thing's place in the hand, a sewn keepsake, the
  blindfold of Hide and Clap) are written at the next autosave or a clean stop. A server killed in between comes back with
  the thing in neither place. The staircase's rooms already avoid this by keeping a copy on the player. The small repair is
  to write the House's records at each logout; that writes the whole record each time, so it wants measuring first.
- **The escape moves the whole world sixty days on.** The Red Room makes the same kind of jump only for a player alone in the
  world, "since everyone shares it"; the unguided escape does it for everybody (`FinaleController`, at the overgrowth).
- **A reader on Gus's pegs at the cemetery's fold.** Alone on his pegs a reader is never taken at the seam, and ridden out of
  the Hollow's back gate on them is recorded as through but not carried. The repaired case is the one where Gus is carried
  after a friend.
- **The film's screen is in Movie night's set.** It is visible, so the new rule passes it, but it is the film's anchor: its
  item may draw the whole film and the projector's beam around the hand. This was not looked at in a client.
- **Zampanò's cats are thinned by counting the cats that are loaded**, as the reader arrives, when they usually are not; the
  "fewer each visit" beat mostly does not happen. The fish beside it is repaired; the cats want the same wait.
- **The pick-up of a keepsake's line.** A keepsake set down and broken comes back without its line. The notes say that once
  set down it is furniture, which may cover it.
- **Collision shapes built on every call.** The outlines are now kept (under "The cost of each tick"). Twenty-three blocks
  still build a collision shape of their own on each call; fifteen of those depend on who is asking, so they cannot simply
  be kept by state.
- **Six patches replace a call outright** (`@Redirect`): the sleep count, the grip on ice, the mob tracking range, the
  landscape's far mobs, the font, and the lazy section buffers. Two mods cannot both replace one call, and the configs make a
  failed patch stop the game, so a pack with another mod at the same call will not start. The library that lets such patches
  share a call ships with NeoForge. The three server-side ones can be checked on the test server; the three client ones need
  a client.
- **A wrapped line over the status bars.** A long House line is drawn at a fixed height; the game's own line moves up for a
  reader with extra hearts. And the longer reading time is given to any plain line on the action bar, another mod's included.
- **Two small messages have no pause.** A request for the talk history and a torn blank page are each answered with a whole
  record. Only a client that repeats them is a cost.
- **The porch case's fast-forward moves nothing.** It sets an age on a copy of the reader's record, so the case waits out the
  real two thousand ticks. Writing the age back was tried and fails the case: the whistle's cycle acts on exact ages, which a
  jump steps over (in play an age only ever goes up by one). The case is left as it was written.
- **The piston case checks the push and not the pull**: the piece stands against the piston's face, so nothing is ever
  retracted.

## Looked at and sound

- **Commands.** Every command hangs under `/oldesthouse` or its alias `/ghosthouse`, both of which need operator level 2.
  The proof commands are only registered when their system property is set.
- **A dedicated server never loads a client class.** The only references from shared code are calls inside the payload
  handlers' own bodies; every client subscriber is marked for the client; no patch for a client class is in the shared list.
- **Messages from clients.** All 28 were read. Every text, list and array has a bound; every position and entity is checked
  against the sender. What was left is under "What the server accepts from a client".
- **The patches.** All 50 were checked against the decompiled game: every target, signature and shadowed member exists, and
  a patch that failed to apply would stop the game, not pass in silence.
- **What is saved against what is loaded.** The saved-data classes, the block entities and the entities save and load the
  same keys with the same types. The state getters return copies and the setters copy in; no caller was found changing a
  copy and not writing it back, apart from the porch case's fast-forward.
- **State keys.** Every key read with a literal name was checked for a write anywhere in the code (7,601 names, 56,801
  uses). Two faults came out of it, both in accounts and both repaired above. No gate was found that can never pass.
- **Chunk tickets.** Every ticket's removal matches its addition in type, chunk, radius and key, apart from the three leaks
  repaired above.
- **Asset checks.** All 186 generator and verifier checks of the quick gate pass, and the 24 cases for the playtest-log
  summary.

## What I could not check

- Nothing here was looked at in a running client. The shoes' new place was checked by their shape and by reading the model.
  The client-only changes (the storm patch, the cast patch, the place looked up once per block, the king's own record) were
  compiled and read, and are not exercised by the test server, which loads no client patch.
- The sizes under "The cost of each tick" are from reading, not from a profile.
- The reader who comes back into a story was not driven through a real save and quit. The arrival it uses is the door's own,
  which every case exercises.
