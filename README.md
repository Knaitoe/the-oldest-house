# The Oldest House

A NeoForge 1.21.1 horror-mod prototype built around one impossible house.

## Current prototype

Version `0.3.5` revises the per-player opening sequence so the ordinary manor, not the player's home, is the first architectural relationship with the House:

- NeoForge 1.21.1 / Java 21 project setup.
- Persistent world-level state for The Oldest House using `SavedData`.
- A furnished, asymmetric Tudor manor of several distinct masses under independent, intersecting roofs, with a fixed exterior (see [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)).
- **Settling in:** a bed respawn point, two completed sleeps and three in-game days since joining make that player's opening eligible.
- **Morning 1:** the Navidsons move in nearby. The manor is placed with the terrain-aware site search, then Navidson photographs a frozen copy of the player's settlement from the real manor-to-home bearing. The visible copied facade gets one lit upper window, invented in the copy if necessary. The real base is never altered.
- Will Navidson's letter and the photograph are left at the player's familiar doorstep.
- **Morning 2:** Hillary appears at that doorstep. When she sees her recipient she stands, barks, trots over and looks up at them with her head tilted; she will not lead until they greet her back (a right-click, the bone, or crouching facing her), following them and whining if they walk off. Then she barks, bounds and darts off towards the manor, then leads over ordinary Overworld terrain at a pace the player can keep. She stops and calls back if they fall behind, and waits at the front entrance.
- **No impossible doorway is placed in the player's home.** The retired 0.3.0 entrance-door block, placement code, state, assets and debug controls have been removed.
- At the ordinary authored front-door boundary, the player alone is transferred to the House dimension. Hillary never crosses dimensions: she sits on the porch outside the front door and stays in the Overworld.
- The Oldest House's perceived age advances only after at least one real manor entry. Ignoring the invitation cannot reveal the impossible threshold off-screen.
- **The first night spent in the manor:** pairs of rugs trade colours between rooms.
- **House morning 3 to 5:** the door between the hall and the study begins to lead somewhere else first. Open it from the hall and there is a small windowless sitting room inside a one-block wall, with another door on its far side and, through that, the study exactly where it always was; the same in reverse. It is the manor's first teleport door, and it switches over only when nobody is in or looking at the doorway.
- **Subtle changes, from the first night on:** paintings trade walls, a candle nobody lit is burning, a chair turns to face a door, a note from the Navidsons turns up on a shelf, the hall is one step longer, and more. Nothing changes while anyone is looking.
- **After someone has walked through the room and two subtle changes:** the door at the far end of the central hall gets a heavily weighted daily chance of opening onto the impossible hallway. The subtle changes carry on afterwards, less often.
- The usable domestic interior lives in The Oldest House dimension at matching coordinates, with nearby Overworld scenery mirrored outside its windows.
- The hallway door at the end of the hall opens directly into interior-only impossible architecture; there is no teleport behind it.
- Operator-only development commands exist for opening-sequence testing, forcing/aging the House, and mirror inspection.
- The exterior is generated once and then left alone. Future impossible space belongs behind it, not in a morphing facade.
- No mixins.

The modular room graph, navigation anomalies, explorer notes, Mother of Strays, deeper Hillary behavior and Minotaur progression are tracked in [docs/DESIGN_DOCUMENT.md](docs/DESIGN_DOCUMENT.md).

## Opening lifecycle

1. Establish an Overworld bed respawn point and complete the configured settling-in requirements.
2. On the next handled morning, the opening places The Oldest House at the best site 32-96 blocks from the player's bed, searched in loosening passes (open and flat first; then wooded, clearing trees and undergrowth; then rough, up to 12 blocks of relief). Nothing that looks player-made (planks, glass, stripped logs, placed leaves, anything with a block entity) is ever cleared, and sites reaching into unloaded chunks are skipped. **The letter waits for the House:** if no site is found, the player stays eligible and the next morning tries again; Hillary likewise waits for it. Worlds where the letter or Hillary came without a House keep retrying every morning, and `/oldesthouse opening house` tries at once and reports why sites were turned down.
3. Navidson's photograph preserves the actual bearing from the manor's porch toward the player's home. Camera distance may be compressed to fit the captured copy, and framing may shift only slightly on that same side.
4. The letter and photograph arrive at the player's most familiar doorstep.
5. On the following handled morning, Hillary appears there. She greets the player and waits to be greeted back (right-click, bone, or crouch facing her) before guiding toward the manor.
6. The player physically reaches the Navidsons' ordinary front door. The boundary transfers the player to the matching House-dimension interior while Hillary waits outside on the porch in the Overworld. Only then may perceived House age begin advancing.

There is no independent hidden appearance roll anymore. The opening sequence owns automatic House appearance; `HouseSpawnManager.ensureSpawnedNear` still owns the terrain-aware site search.

## Days, mornings and perceived House age

The mod counts days itself (`HouseCalendar`). A day passes at every dawn the world clock crosses, and `/time set` can only move the count forward: `/time set day` after dark counts as a new morning, while `/time set night` never sends the count back to day 0 (which used to stall the opening's "nights slept" and "days since joining" indefinitely).

A **morning** is a player waking after a night's sleep, in the Overworld or in one of the manor's beds. Sleeping in the manor now ends the night for the world; before, the House dimension could not move the clock, so manor sleepers were woken at night and nothing advanced.

After the manor has actually been entered at least once, each new morning advances its perceived age by one. If the House has appeared but nobody has entered it, perceived age stays put, preserving the domestic-familiarity phase.

What the mornings bring, all tunable in `the_oldest_house-house-server.toml`:

1. **Rugs:** the morning after someone first sleeps in a manor bed, the great room and principal bedroom rugs trade colours, and so do the study's and literary bedroom's. Only carpet still where the house laid it changes. If nobody sleeps there, they change at perceived age 2 anyway.
2. **The room between rooms:** from House morning 3 (and at least a morning after the rugs), each morning has a 50% chance of arming the room behind the study door, then 75%, then certain: House morning 5 at the latest. Armed, the door switches over the first time nobody is in or looking at the doorway; nothing in the manor visibly changes. The first night in the manor only brings the rugs; after that the House's mornings are the clock, wherever anyone slept. `/oldesthouse status` shows the chance for the next morning.
3. **Subtle changes** (`HouseShifts`): from the rugs on, on any morning when nothing larger happens, there is a 45% chance of one, rising by 20 points each quiet morning in a row and falling back after one happens. Each only touches what no player can see (in range, in a wide view cone and in line of sight):
   - **Paintings** trade walls with one of the same size, or one hangs a block over.
   - **The hall** is one block longer (once): the end wall moves back into space only the House dimension has, and the last slice of hall repeats in front of it. Outside, and in the Overworld proxy, nothing changes.
   - **Doors:** one hangs from its other side, and any left open are shut.
   - **Chests:** the first time one of the manor's own chests or barrels is opened, the house remembers what was in it. Later it may put one common thing back (bread, string, a candle; never a tool, book or anything named or enchanted), only into an emptied slot, never the same slot twice.
   - **Candles:** one nobody lit is burning.
   - **Echoes:** the next time someone is across the house from the principal bedroom, they alone hear its door open and close, or footsteps crossing the upper hall and a door shutting.
   - **Chairs:** one turns a quarter to face a door (downstairs, the study door once it leads through the room).
   - **Notes:** a written book from Will or Karen Navidson turns up on a shelf, preferably in the literary bedroom: Will measuring the house, a different number each time; Karen watching him. Later notes wait for the rugs, the room, the deeper hall or the hallway.
   - **A guest bed** (one nobody's spawn is set in) changes colour, to the colour of a player's own bed at home if one is loaded.
   - **A window:** an invisible light sits behind one upstairs window of the Overworld proxy, preferring the side facing a player's home. From outside at night the window is lit; inside, the room is as dark as ever.
4. **The impossible hallway:** once someone has gone through the room (in one door and out the other) and two subtle changes have happened, it gets its own roll each morning, ahead of the subtle changes: 50%, rising by 25 each morning it stays shut. When it opens, the door is at the end of the hall where it always has been (a deepened hall's extra block becomes its threshold). Afterwards subtle changes carry on at 25% (+15 per quiet morning).

At most one of these happens on any morning.

The room is real and so are its doors, but they stand in the House dimension far above the manor (the pocket, 100 blocks up). Its east door is the study door, directly above the real one, with a copy of the hall in front of it; its west door is the same door seven blocks further west, with a copy of the study (and the view from its windows) shifted by the same amount behind it. Clicking the real door from the hall shifts the player straight up; from the study, up and seven blocks west. Either way the shift is relative to where they stand, so nothing about their position, facing or momentum changes and nothing loads, and in the same tick the copy's door swings open with an ordinary door's sound. Walking out of the room through either door shifts them back down to the matching side of the real door, which has closed behind them: out the far side, into the other room, exactly where they expected to arrive. Coming out the opposite side from the one you went in by is what counts as having found the room. The copies' paintings are matched one for one. The real door never opens, the wall around it cannot be broken, and nothing in the pocket can be used but its two doors. The room is not the labyrinth: beds and every other ordinary rule still apply in it.

For testing, `/oldesthouse day [n]` passes whole nights as if everyone online slept (in the manor if they are standing in it), `/oldesthouse reveal rugs|room|hallway` forces a stage, and `/oldesthouse shift [kind]` makes one subtle change now (a random one, or `paintings`, `deeper_hall`, `doors`, `chests`, `candles`, `echoes`, `chairs`, `notes`, `guest_bed`, `window`). `/oldesthouse age` and `/oldesthouse advance` still set perceived age directly, without running mornings.

## The labyrinth and its vignettes

When the impossible hallway opens, its far wall has a door. It leads into the labyrinth: places carved into solid slots of white plaster stacked above the manor (100-odd blocks up, in the House dimension, over the manor's own chunks; the slots that do not fit under the top of the world carry on below it), joined only by doors. Any tunnel out of a room finds only more wall, and there is enough of it overhead that no weather is heard.

- **Doors that lead elsewhere** are ordinary spruce doors the mod keeps shut; the client is told where they are so it never swings one open. Clicking one is a cut nobody can see: behind the destination's entry door the mod copies in the wall around the door you clicked and sixteen blocks of what lies behind you (turned to line up), shifts you to the same spot in front of that door, turned the same way, in the same tick, and then the door swings open onto the place with an ordinary door's sound. Nothing loads and nothing fades: the places are in chunks you already have. Walk back out through the entry door and you are shifted back to in front of the door you came through, shut; the entry door shuts behind you once you are inside. Only a test door placed in the Overworld crosses a dimension, under the manor's still frame.
- **The gray:** the junction (a plain gray hall with a door in each wall) and a long gray corridor, dealt most often, and three seamless loops, dealt less often. A loop is identical copies of a stretch of the house with a same-tick relative shift from one to the next that keeps your spot, facing and momentum; every copy bends before your view could reach anything that isn't a copy.
  - **The Five and a Half Minute Hallway:** a gray corridor that jogs aside every twelve blocks. Walking on loops back a period until five and a half minutes have passed since you came in; only then does its far door come. Sprinting, speed and ender pearls change nothing. Walking back unwinds only the laps you walked, so the space stays consistent. Anything placed or dropped in it is gone on the next pass.
  - **The hotel hallway:** the same bends in loud carpet and wallpaper, with locked numbered doors. The numbers climb as you walk on and repeat every 24 rooms; forward never ends. The way out is to walk against the rising numbers. Each player sees their own numbers.
  - **The spiral staircase:** a slab stair around an open well, four blocks a turn. Climbing repeats a turn four extra times; descending never does, so it takes longer to climb than to come down. An anvil (or sand, or gravel) set over the well falls and vanishes before it lands; nothing else can be placed there.
- **The dealer:** whenever you arrive somewhere with dealt doors, they are all dealt again, so backtracking never quite works. At most one leads to a vignette; the chance is 30%, +25% for every dealing without one, so a dry spell guarantees a door. A vignette door leaks: its heartbeat can be heard faintly behind it. Now and then a gray door leaks too, and lies. Finished one-shots are never dealt again.
- **Hillary seeks** (she never comes inside). Each vignette yields one object: the caregiver's note, the crayon drawing. Right-click Hillary with one and she takes the scent, runs to the manor's front door and scratches at it to be let in. The next time the house deals doors and can deal a vignette, it does (one not yet found, if any are left), and what leaks from behind that door is Hillary barking, faintly, as if she had found the way round outside. The dealer still never summons a place on demand: you have to find the door. With nothing left to find, she whines and lies down.
- **The Tell-Tale Heart: the floorboards** (one-shot; verb: sneaking). An old man's bedroom, one candle, his bed made. Sculk lies under the boards. Every vibration you make (the ones a sculk sensor hears, so crouching makes none) speeds a heartbeat coming from under one darker board. If it races to the top, the lights go out and you are back where you came from. Pry the dark board up with an axe (right-click, or break it with an axe) and the caregiver's note is underneath; the heartbeat stops. The room cannot be dug out of, built in, or its floor broken; trying is loud.
- **Hill House: the Red Room** (recurring; verb: recognizing your room). The mod keeps a quiet tally of where you spend your time indoors within 40 blocks of your bed. After two minutes in the spot you spend the most time in, it copies that room while you stand in it, and again each day you are there, so the copy follows the room. The Red Room is that copy: you come in through your own doorway (its door standing open), or through a gap cut in the wall if the room has no door. Every container is empty, levers and buttons do not move, redstone tells nothing it changed, and the room is cut off at 13 blocks across and 6 high; the windows look onto white. Its bed works at night, the one bed past the threshold that does, but sets no spawn point. Sleep in it and you lose a day or two (the world's clock jumps only when you are alone on the server; you wake hungry either way) and wake somewhere deeper in the gray; the way back leads first to the bed. It is only dealt once a room of someone's has been copied, and it keeps being dealt. If you leave something in one of its containers, the house keeps that copy standing rather than replacing it.
- **The Conjuring: hide-and-clap** (one-shot; verb: following claps blindfolded). A child's bedroom with open floor. On the wall over the bed's post hang a blindfold and a note: put it on, count to ten, follow the claps, take it off when you think you've found me. The blindfold is a head-slot item (right-click to wear it, like a carved pumpkin); while it is on the screen is black but for a strip at the bottom where the floor around your feet shows, and the hotbar, action bar and subtitles stay readable. Put it on in the room and a child counts to ten. Then three or four claps come from open spots; get within a block or two and the next one sounds somewhere else, wander off and the current one comes again, louder (the subtitles say "Someone claps", with their arrow). As the third begins, small bare feet step away through the strip. After the last, a wardrobe that wasn't there stands two blocks behind you (or the nearest open spot within 45 degrees), and two muffled claps come from inside it. Take the blindfold off and open it: a crayon drawing of someone in a blindfold, drawn from where the wardrobe stands. Take it off before the wardrobe and the claps stop, the game resets, and there is a new note on the bed. Never put it on and, now and then, a single clap comes from under the bed. Once opened, the wardrobe stays open and the game is not dealt again. The claps and footsteps reuse vanilla sounds under the mod's own subtitles until recorded audio exists.

### Dimension-backed domestic interior

Version 0.1.9 returns to the intended architecture: the Overworld contains the fixed exterior of The Oldest House, while the usable domestic interior exists in `the_oldest_house:house_interior`.

The interior dimension uses the same coordinates as the exterior. Entering the physical bounds of the structure transfers the player to the matching X/Y/Z in that dimension, so the front door is the normal route but digging through a wall or dropping through the roof is not a bypass.

The House dimension now uses Minecraft's actual Overworld noise settings, Overworld biome source, and the server's own world seed. Untouched terrain therefore generates natively at the same coordinates: water, biome tinting, hills, caves, trees, lighting, and the distant horizon no longer depend on a finite copied stage set.

On first initialization, the mod compares a 48-block-radius surface region against the real Overworld and overlays only block states that differ, such as The Oldest House itself and existing player edits. Matching native terrain, especially fluids, is left untouched.

NeoForge's dimension-transition screen hook replaces the normal loading presentation specifically for travel into and out of The Oldest House. Version 0.1.11 sends the entry context to the client before each transition and uses a matching vignette:

- **front door:** an animated spruce-door close-up with a vanilla wooden-door sound;
- **window opening:** a bright-to-dark exposure/refraction transition;
- **wall or other breach:** a closing plaster/dust aperture.

Each vignette uses a slightly randomized minimum duration so the transition does not become a perfectly timed loading ritual.

Version 0.1.12 added an initial context lead for door/window/breach transitions. Testing showed that a fixed tick delay was still not a reliable network ordering guarantee. Version 0.1.14 uses an explicit client acknowledgment handshake: the server sends the transition kind, the client stores it and replies, and only after that reply does the server change dimensions.

The initial exterior proxy is still seeded from a snapshot, but common player-driven block changes in the shared visible region are mirrored both directions after initialization. Breaking or placing blocks, breaking windows, and right-click state changes such as opening doors are deferred to the end of the server tick and copied to the matching coordinates in the other dimension. The impossible hallway is excluded from that synchronization. Its generated floor, ceiling, side walls, and terminal wall are protected against player mining and explosions, while decorative objects and player-placed markers remain ordinary breakable Minecraft blocks.

## Development commands

Commands require permission level 2.

| Command | Purpose |
| --- | --- |
| `/oldesthouse spawn` | Force-spawn the test house about 36 blocks in front of the player. |
| `/oldesthouse status` | Show the House's state, the mod's day count, the rugs, the room's chance of appearing next morning, when the hallway opens, and your own opening progress. |
| `/oldesthouse day [n]` | Pass one (or `n`) nights as the mod sees them: the clock jumps to dawn, everyone online counts as having slept, and each morning runs as a real one would (opening steps, House age, rugs, the room's roll, the hallway). Stops early while Navidson's photo develops. |
| `/oldesthouse reveal rugs\|room\|hallway` | Force that stage now. |
| `/oldesthouse shift [kind]` | Make one subtle change now: random by weight, or the named one. Fails if what it would change is in someone's view. |
| `/oldesthouse age <days>` | Set The Oldest House perceived age to an exact value (does not run mornings). |
| `/oldesthouse advance` | Advance The Oldest House perceived age by one day. |
| `/oldesthouse advance <days>` | Fast-forward The Oldest House perceived age by the supplied number of days. |
| `/oldesthouse door <floorboards\|red_room\|hide_and_clap\|junction\|gray_corridor\|long_hallway\|spiral_stair\|hotel_hallway\|dealt>` | Place a test door two blocks in front of you, facing you, that leads there (`dealt`: wherever the dealer deals it, each time). The door you arrive at leads back to it. Works in any dimension. |
| `/oldesthouse door remove` | Remove the nearest test door within eight blocks. |
| `/oldesthouse labyrinth go` | Go straight to the junction; its entry door leads back to where you stood (or the hallway, once it exists). |
| `/oldesthouse labyrinth build` | Carve every labyrinth place again. |
| `/oldesthouse labyrinth status` | Deals, the dealer's odds, finished vignettes, and how many doors deep your way back is. |
| `/oldesthouse vignette floorboards reset\|complete` | Put the floorboards back as new (dealable again), or mark them finished. |
| `/oldesthouse vignette hide_and_clap reset\|complete` | Put the child's room back as new (no wardrobe, blindfold on the wall, dealable again), or mark the game finished. |
| `/oldesthouse vignette red_room capture\|forget\|status` | Copy the room you are standing in as your room now; forget your tally and copy; or show where you spend your time and whose room stands in the Red Room. |
| `/oldesthouse restock` | Give every empty, untouched chest and barrel in the manor its room's loot table (for houses built before the tables existed). |
| `/oldesthouse visit` | Increment the stored visit count. |
| `/oldesthouse reconcile` | Force an authoritative House-dimension to Overworld reconciliation and report how many positions changed. |
| `/oldesthouse reset` | Reset persistent The Oldest House state. This does **not** erase blocks already placed in the world. |
| `/oldesthouse opening status [player]` | Show a player's opening stage, sleeps, bed, timing, Hillary, first-entry state and last photo. |
| `/oldesthouse opening advance [player]` | Run the player's next opening step now. |
| `/oldesthouse opening eligible [player]` | Skip the settling-in requirements; the letter arrives on the next handled morning. |
| `/oldesthouse opening letter [player]` | Take the photo and deliver Navidson's letter and snapshot immediately. |
| `/oldesthouse opening photo [player]` | Photograph the player's house again and hand you the snapshot. |
| `/oldesthouse opening copy [player]` | Stand where Navidson's camera stood in the outside dimension. |
| `/oldesthouse opening house [player]` | Have the Navidsons move in next door now (the same site search as the letter's morning) and report why sites were turned down. |
| `/oldesthouse opening hillary [player]` | Put a new Hillary on the player's doorstep, replacing any earlier one. |
| `/oldesthouse opening reset [player]` | Clear that player's opening progress. |

### Important

`/oldesthouse spawn` is a development command. It clears the The Oldest House build volume before placing the prototype, so do not aim it at anything you care about.

## Build

Requirements:

- JDK 21
- Gradle 9.2.1 or a generated Gradle wrapper

Build with:

```bash
gradle build
```

Run the structure game tests with:

```bash
gradle runGameTestServer
```

They generate The Oldest House on a game-test server and check the architecture the rest of the mod relies on: the front-door-to-threshold sightline, that every room is sealed from the outside, that every room is reachable on foot from the porch, that roofs have no holes and never intrude into rooms, glazing, doors and lighting. The log also carries an `OTH-DUMP|` block listing of the generated house; `tools/render_house_dump.py` turns it into eye-level renders and plans for review without launching the game.

GitHub Actions builds, runs the structure game tests and uploads the compiled mod jar on every push and pull request.

## Target

- Minecraft 1.21.1
- NeoForge 21.1.251
- Java 21
- Mod ID: `the_oldest_house`


### 0.1.13 test-world note

Version 0.1.13 changes the generator of `the_oldest_house:house_interior` from a flat world to native Overworld noise generation. Chunks of that dimension generated by older builds remain flat because Minecraft does not retroactively regenerate existing chunks. For a clean terrain/water test, use a fresh test world or an entirely ungenerated coordinate region.


### 0.1.15 transition-context persistence

Transition context is no longer consumed when the receiving screen is first constructed. The acknowledged kind/token remains stable for the duration of the dimension handoff and is cleared only when the transition screen closes. This prevents any receiving-screen recreation from silently falling back to the default door treatment.

Server logs also record the classified transition kind, token, source/destination dimensions, and player coordinates for each crossing so future threshold bugs can be distinguished from client presentation bugs.


### 0.1.16 physical transition vignettes

The three boundary transitions now share one presentation rule: the player's view is physically obstructed rather than covered by a supernatural effect.

- The front-door vignette now uses the authored oak door rather than spruce.
- Window crossings show the nearby white-terracotta sill/jamb moving close to the camera; the old exposure/refraction effect has been removed.
- Wall breaches show white-terracotta wall faces with a narrow stripped-dark-oak structural edge as the player squeezes through the hole; the old iris/dust effect has been removed.
- Entering The Oldest House is intentionally a little slower/tighter than leaving it.


### 0.1.17 captured-frame transitions

Transition screens now begin from a GPU capture of the actual final gameplay frame rather than replacing the player's view with a synthetic background.

When NeoForge requests the receiving-level screen, the client captures Minecraft's main render target with `Screenshot.takeScreenshot`, registers that image as a temporary dynamic texture, and holds it behind the physical obstruction while the destination dimension loads. The temporary texture is released when the receiving screen closes.

Door, window, and wall-breach overlays are therefore contextual interruptions of the player's real view:

- **door:** the oak door moves across the captured scene;
- **window:** the captured scene remains visible while a sill and jamb move close to the camera;
- **breach:** the real hole/view remains visible while plaster and a timber edge tighten around it.

The captured frame is rendered with an explicit normalized-UV quad instead of `GuiGraphics.blit`, avoiding the texture-scaling ambiguity encountered in earlier prototypes.


### 0.1.18 motion pass

Captured-frame transitions now include restrained camera-motion cues instead of treating the captured gameplay image as a stationary photograph.

- **window:** the captured world drifts downward and sideways with a slight forward zoom while the sill rises and one jamb travels across the view, approximating a head-and-shoulder climb through the opening;
- **breach:** the captured world slides laterally and slightly forward while one dominant wall face crosses the center and the opposite edge closes late, approximating a shoulder-first squeeze rather than an iris wipe.

The motion is intentionally small so the captured HUD does not visibly detach from the screen.


### 0.1.19 live-camera handoff

The window and breach transitions no longer fake body movement by translating the captured framebuffer. That approach moved the crosshair, held item, hotbar, and world together like a flat photograph.

The server now waits while the client performs a short **live source-world camera motion** before acknowledging the transition context. NeoForge camera-angle and FOV events provide a small yaw/pitch/roll lean and forward FOV compression while the world is still genuinely rendering and the HUD remains fixed. Only when that live motion reaches the handoff point does the client ACK, allowing the dimension change.

The receiving screen then captures that final live frame, holds it for a much shorter interval with restrained wall/window-edge occlusion, and the destination camera settles back to neutral after the screen closes.


### 0.1.20 real-geometry handoff

Recorded testing showed that the live camera motion worked, but the GUI-rendered white-terracotta and dark-oak overlays did not. A single 16x16 block face stretched across a large screen-space rectangle produced visibly distorted texel density and read as a broken texture rather than nearby Minecraft geometry.

Window and wall-breach transitions therefore no longer draw synthetic block-material surfaces. The real source-world geometry is allowed to provide the window frame, sill, wall, and timber during the live pre-handoff camera movement. The receiving screen holds that genuine final gameplay frame for a much shorter interval and applies only subtle edge shadows to hide the dimension swap.

The front-door treatment remains unchanged because its discrete door-panel animation already reads correctly.


### 0.1.21 front-door follow-through

A successful entry through the authored front door now schedules a real door close eight server ticks after the player reaches the House interior. Vanilla `DoorBlock.setOpen` performs the close so the normal wooden-door sound and game event are preserved. The resulting closed lower/upper door states are then mirrored back to the Overworld shell.

Window and wall-breach entries do not affect the front door.


### 0.1.22 domestic architecture expansion

The static domestic portion of The Oldest House is now a three-level usable residence:

- a stone-brick basement with a real stairwell, workshop/storage furnishings, lighting and loot chests;
- the existing ground floor, reorganized into living/kitchen space, a rear study, utility room and the original central hall that eventually reveals the impossible doorway;
- a full second story reached by a two-wide central staircase, with two front bedrooms and a larger rear bedroom.

Bedroom windows have been restored on safe front/side elevations. The rear upper facade remains solid because the impossible hallway eventually projects from the center of the rear ground floor and should never become visible through an ordinary window.

Six authored chests now use three custom lazy-generated loot tables: bedroom belongings, study supplies, and basement/workshop supplies. The same deterministic tables are reattached to the House-dimension chest block entities during first interior initialization because the mirror currently synchronizes BlockState rather than general BlockEntity NBT.

The domestic transition envelope now includes the basement and second story while still excluding the roof/attic.


### 0.1.23 architectural and terrain-aware generation pass

The static house exterior has been rebuilt around stronger vanilla-build conventions rather than treating the roof as a procedural cap.

- The main roof is now a complete steep deepslate-tile gable with a full one-block overhang, dark-oak front/back bargeboards, a filled plaster gable, central timber framing, and twin attic panes.
- A separate pitched porch gable creates a second architectural scale over the entrance instead of the old flat porch lid.
- The facade gains stronger vertical timber bays, projecting dark-oak window sills/headers, and a more grounded stone plinth.
- The main interior stair is reduced to a one-wide open flight with a rising railing so it no longer consumes the living room as a large central block mass.

Natural spawning now evaluates up to 40 candidate sites and chooses the best rather than accepting the first valid one. Dry footprints and flatter terrain are strongly preferred, while a small amount of nearby water is permitted.

Before construction clears anything, the builder samples the front approach. Porch posts extend down to real terrain. On land, the front stair run extends outward/downward to the sampled ground height. If the approach is substantially water-covered, the front becomes a short supported spruce landing/dock rather than generating stairs into water.


### 0.1.24 front stair orientation fix

Terrain-adaptive stone-brick approach stairs now face south toward The Oldest House, so their ascending side points back toward the porch while the courses descend northward to the sampled ground level.


### 0.1.25 doorway-clearance audit

The ground-to-second-floor staircase has been moved forward, away from the central rear-hall doorway, and its railing has moved to the west side so the house's center circulation line remains open.

The basement stair now begins one block deeper inside the utility room so its railing no longer crowds the utility-room doorway. Nearby utility furnishings were relocated accordingly.

A final doorway-clearance pass now runs after all structural, furnishing and lighting generation. Each authored doorway has protected approach cells on both sides; only colliding blocks are removed, so passable carpets and other zero-collision decoration can remain. The future impossible doorway receives the same clearance treatment when revealed.


### 0.1.26 stage-aware domestic mirror refresh

The first impossible doorway threshold is now correctly treated as shared domestic architecture. The impossible hallway begins at z+16; the z+15 partition containing the revealed doorway is no longer excluded from mirroring.

After a House stage modifies domestic/shared architecture, the House dimension now explicitly pushes the complete shared domestic footprint back into the Overworld proxy. This makes the threshold doorway appear immediately and keeps player-visible ordinary rooms consistent while still excluding all hallway geometry from the Overworld.


### 0.1.27 Mirror v2: authoritative domestic persistence

The House dimension is now the authoritative copy of the initialized domestic interior.

- Mirror copies now include block-entity custom/component NBT in addition to BlockState. Chests, barrels, furnaces, lecterns and compatible modded block entities can therefore carry inventory/state across the proxy boundary.
- Initial House-dimension creation copies existing block-entity data from the Overworld snapshot instead of reconstructing chest loot tables after the fact.
- Once initialized, the active domestic footprint is reconciled from the House dimension back to the Overworld once per second while a player is within 96 blocks in either dimension. This catches mutations that do not reliably fire placement/break events, including container menu changes, furnace progress, many redstone/environmental state changes and block-entity updates.
- Explicit Overworld player edits are still accepted first and pushed into the House dimension before the authoritative pass, preserving break/place/door interactions on the exterior shell.
- Explosions and piston movement now queue shared-position mirror updates as well.
- `/oldesthouse reconcile` forces an immediate authoritative domestic reconciliation and reports how many shared positions changed.
- `/oldesthouse reset` clears queued mirror work in addition to persistent House state.

Impossible-only hallway cells remain excluded from all domestic reconciliation.


### 0.1.28 Overworld hallway sightline proxy

The impossible hallway remains physically absent from the Overworld, but can now be seen from the ordinary domestic/front side after its threshold door is revealed and opened.

The server synchronizes the House origin and reveal state to clients. While the player remains in the Overworld, a client-only renderer draws only the inner corridor surfaces at the hallway's world-space coordinates. Vanilla depth testing lets the real domestic walls and closed doors occlude the proxy naturally. A ray check suppresses the render when there is no clear sightline toward the threshold, and the renderer is always disabled from the rear side of the threshold.

No proxy blocks, collision, lighting state, or chunks are created in the Overworld. Crossing the threshold still hands off to the real hallway in the House dimension.


### 0.1.29 upstairs landing correction

The front-bedroom doors no longer open onto the upstairs stairwell void. Both doors have moved to the solid rear section of the central landing at relative z+8, where each has a full walkable approach.

The open stairwell now has a continuous spruce-fence guardrail along its solid corridor edge. The upstairs runner has also been adjusted so it stops at the stair opening and resumes on the rear landing rather than implying floor where there is none.

Doorway-clearance volumes were updated to the new landing positions so later furnishing/structure passes cannot recreate the same defect.


### 0.1.30 domestic polish and threshold-view repair

This pass focuses on visible architectural finish and two immersion defects found in playtesting.

- The main domestic staircase is now a single clean stair flight. The rising fence course that visually read as a second staircase has been removed; railing exists only around the actual upper-floor opening.
- The basement stair now turns ninety degrees at the bottom so it discharges into open basement floor rather than aiming directly into the rear foundation wall.
- Water-site docks now have continuous perimeter fencing, a deliberate east-side ladder opening, a supported ladder column and a ladder extending down toward the sampled water level.
- The three upstairs bedrooms now have distinct decorative identities using different rug shapes/palettes, wall hangings, plants, books/work surfaces and hobby furniture instead of repeated rectangular carpet strips.
- Impossible-hall construction now uses client-update / known-shape / suppress-drops flags and immediately purges item/falling-block debris inside the construction volume.
- The first impossible threshold door appears open, making the long sightline legible as soon as the stage exists.
- The Overworld sightline no longer tries to render the physical corridor sixty blocks through ordinary terrain. Instead, it renders a depth-tested perspective view immediately behind the real threshold aperture. This avoids hills, water, trees and other valid Overworld geometry occluding the impossible view while keeping the physical exterior unchanged.


### 0.1.31 HouseBuilder rewrite

The domestic House generator has been replaced rather than incrementally patched.

- The old front-facing full-width gable is gone. The main roof now presents broad front/rear planes with a low two-block-run pitch and a two-wide ridge, so the primary facade reads as eave + roof rather than a giant plaster triangle edged with stair teeth.
- The facade has real massing: a one-block-deep ground-floor bay projects on the left, while the upper-right bedroom projects forward beneath its own smaller gable.
- The old double ceiling/floor sandwich has been removed. The second-floor blocks are the downstairs ceiling, eliminating the strange inter-floor cavity visible around the stairwell.
- Exterior timber is structural and bay-oriented rather than a uniform horizontal belt.
- The porch is a simpler lean-to composition that supports the main architecture instead of competing with it.
- Water-site docks keep the completed rail/ladder layout and now use a real corner lamp post instead of a lantern hanging from empty sky.
- The main stair has moved into a dedicated edge of the circulation zone and has no decorative second stair/fence course.
- The basement stair now turns through a real lower landing before entering the cellar.
- Living-room seating is arranged around the fireplace, with a rug and table anchoring the group.
- Upstairs bedrooms now differ by activities and furniture, not merely color: reader/writer, music/hobby, and formal/work-oriented rooms.
- Nightstand accessories sit on full blocks, eliminating floating pots/candles.
- Actual vanilla painting entities replace wall-banner stand-ins. The same authored paintings are spawned separately in the Overworld and authoritative House dimension because entity mirroring is not yet generalized.
- Window panes are restored after structural/furnishing passes as a final aperture invariant.
- The first impossible door again appears closed.


### 0.2.0 Tudor manor rebuild and mirror performance

**Breaking change:** the House layout changed completely. Worlds with a House generated by 0.1.x should run `/oldesthouse reset` and respawn it (or use a fresh test world); the server log warns when it finds an old-layout House.

The House is now a large, old, asymmetrical Tudor manor that reads as decades of additions rather than one shell:

- **Masses:** a dominant jettied great-room wing (front left in plan), a recessed central entrance under a small gable, a lower one-and-a-half-storey kitchen wing (front right), an older study wing under a perpendicular rear cross-gable (rear left), and a heavy stair tower with a service range around a walled yard (rear right). Fronts step back at different depths; the great-room upper floor jetties over the ground floor and the entrance storey overhangs the porch.
- **Roofs:** seven independent roof systems (three unequal front gables, the rear cross-gable, a hipped tower roof with flared eaves and a finial, the service roof, and two different dormers) with verge boards, varying ridge heights (13-18) and real valleys where they intersect.
- **Chimneys:** a broad external great-room stack that steps in twice and rises clear of every ridge, plus a rear study stack and a kitchen stack.
- **Materials:** plaster (white terracotta) and dark oak framing over a weathered stone plinth, brick chimneys and fireplaces, oak/spruce floors, deepslate roofs. The windowless rear carries bricked-up openings: the impossible hallway runs south from the threshold and must never be seen from an ordinary window.
- **Plan:** the front door, a long central hall and the rear threshold wall share one straight axis with nothing permanent across it. The dog-leg stair (two flights of three steps with a half landing) is off-axis in its own tower; its landing doubles as a library nook; the upper hall connects straight to the tower's upper landing.
- **Rooms with identities:** an inglenook great room (hearth with bressumer and mantel, flanking built-ins, exposed beams, a seating group turned to the fire, a deep bay window seat, pictures); a working kitchen with range, sink, counters and dresser; a cluttered study with a library wall and ladder, map tables, lecterns, a writing desk and its own fire; formal, literary and music/maker bedrooms; a long gallery under the cross-gable; a scullery with a cellar stair; a cellar workshop.
- Stairs now finish flush with the floors they serve (the previous stairs ended a full block short at the top).
- Container contents (loot, books) live only in the House dimension.

Performance and correctness:

- The House dimension is seeded in the background from the moment the House spawns, using chunk tickets and a couple of chunks per tick, instead of a ~400,000-block copy and synchronous chunk generation on the first entry. Entering before it finishes completes it synchronously.
- Authoritative reconciliation runs only while an Overworld player is within 96 blocks (plus once whenever someone leaves the House), reads chunk storage directly and never serializes container inventories.
- Piston, explosion, break, place and interaction handlers do a cheap bounds check before any work; pistons resolve their structure once.
- **Item duplication fix:** Overworld containers inside the House are empty proxies that cannot be opened, broken or exploded, and inventories never cross between dimensions.
- The House origin is cached; per-player transition state is updated in place and dropped on logout; static state is cleared on server stop and client disconnect (a stale hallway view could previously carry into another world).
- Natural spawn site search rejects candidates with heightmap reads before scanning blocks, and accepts flowers and saplings as clearable vegetation.
- The client caches the sightline sprites (refreshed on resource reload).

### 0.3.0 opening sequence

Everything before a player first enters the House, run per player (see [docs/OPENING.md](docs/OPENING.md)):

- **Settling in:** a bed respawn point, two completed sleeps and three in-game days since first joining (all configurable in the server config `the_oldest_house-server.toml`).
- **Morning 1:** the Navidsons' house (The Oldest House) appears next door if it has not already. The House captures the player's settlement, rebuilds it in the new outside dimension, lights the upper window facing the Navidsons' porch (cutting one into the copy's wall if there is none) and photographs the copy at night with a small ray tracer. A written book from Will Navidson ("Howdy, Neighbor") and that snapshot (a locked filled map of the player's own house) then wait on the doorstep of the player's most-used door, with one soft knock only the recipient hears. Both never despawn, only the recipient can pick them up, and water does not carry them off.
- **Morning 2:** Hillary, an ashen husky, waits on the doorstep; the first bone from her recipient always tames her. A new door appears in a plain wall within 12 blocks of the bed: never one with anything resting on, hanging from or connected to it, never one the player is looking at or just placed. After three nights without a wall, a freestanding framed door appears on open ground.
- **The door:** unbreakable in survival and immovable by pistons, it opens only for its owner, who steps through into the House's hall via the existing DOOR transition. Anyone else hears a locked rattle. The blocks it replaced are recorded for later restoration.
- **Hillary and the House:** if she follows her owner in, she runs a few blocks down the hall; out of sight (or after two seconds) she is taken out of the House and set down, sitting, on her doorstep.
- **Beds and the labyrinth:** beds work throughout the manor. Past the labyrinth threshold (the door at the end of the hall, the impossible hallway, and every place reached through the labyrinth, including the outside dimension) sleeping quietly fails and no spawn point is set.
- The adjusted design document is in [docs/DESIGN_DOCUMENT.md](docs/DESIGN_DOCUMENT.md).

### 0.3.1 opening continuity revision

The opening has been revised so the player's own home remains ordinary.

- The custom entrance door, its wall-placement/freestanding fallback logic, world records, registry entries, resources, immunity tags, config options and GameTests have been removed.
- Hillary now leads the recipient from their doorstep to the actual Navidsons' manor, waits when they fall behind, and stops outside the authored front door rather than entering.
- The old hidden settlement-night / appearance-roll lifecycle has been retired. The per-player opening is now the sole automatic appearance path; the terrain-aware safe-site search remains shared infrastructure.
- Perceived House age is gated on real manor entry, preventing the age-3 impossible threshold from appearing while the invitation is being ignored.
- Navidson's photograph now treats the real porch-to-home bearing as a hard relationship. Framing can shift only slightly on that side, while the copied facade can always receive a lit upper window without modifying the real home.
- Old 0.3.0 `door_placed` player stages migrate to `HILLARY_ARRIVED`; obsolete door/return NBT is ignored.

### 0.3.2 photograph regression fix

- Navidson's snapshot no longer substitutes the baked stock home merely because the real porch-facing camera angle is obstructed.
- Camera selection now tries the real porch relationship first, then broader same-side framing, and finally any viable angle around the captured settlement.
- Once Capture has successfully produced the player's settlement copy, the rendered pixels from that copy remain authoritative even if the conservative visibility flag is false.
- A GameTest now blocks the preferred facade of a custom test house and asserts that the result still depicts the captured house rather than stock art.

### 0.3.3 proxy-Hillary and smooth-door handoff

- Hillary no longer waits outside once the player enters. At the front-door handoff she is moved into the literal Overworld proxy foyer and remains an Overworld entity; only the player transitions to `the_oldest_house:interior`. (Superseded in 0.3.4: she waits outside on the porch.)
- Vanilla tame-owner follow behavior is suppressed while Hillary is parked in the proxy so she cannot try to follow/teleport across dimensions.
- The bespoke giant swinging-door transition screen and duplicate UI door sound are removed.
- `DOOR` transitions now use the same short captured-frame/motion treatment as the generic breach boundary, while the real Minecraft door supplies its own in-world animation and sound.

### 0.3.4 exterior entity continuity

- The Overworld manor proxy now rejects living non-player mobs instead of allowing them to become trapped in inaccessible rooms.
- Evacuated mobs remain intact and visible immediately outside the nearest authored exterior doorway; they are not deleted or hidden.
- Hillary is staged specifically at the front entrance, sitting there and refusing to enter.
- While a player occupies the domestic House dimension, nearby Overworld mobs are mirrored as non-interactive visual projections at matching coordinates so they remain visible through windows and open doors.
- The real Overworld mob remains authoritative. Projections have no AI, damage, interaction or gameplay authority and are removed when nobody is in the domestic interior.
- Native outdoor mobs in the House dimension's mirrored view are suppressed to prevent duplicate populations.

### 0.3.5 bidirectional entity continuity

- Exterior entity continuity now works both directions across the ordinary domestic boundary.
- Real Overworld mobs remain visible from inside the House via non-interactive matching-coordinate projections.
- Real House-dimension mobs inside the ordinary domestic volume now remain visible from outside via projections inside the Overworld proxy shell.
- Reverse projection is deliberately restricted to the domestic volume. Impossible hallway, vignette and deep-House entities do not bleed into the exterior facade.
- The dimension containing the real mob remains authoritative; projected entities cannot be attacked, interacted with or used for gameplay.
- Proxy evacuation explicitly ignores projections, so a visible interior NPC is not mistaken for a trapped real Overworld mob and thrown out of the house.
- Both projection directions are seeded before dimension exits/entries where appropriate to avoid one-frame pop-in.


### Unreleased fixes

- Hillary stops guiding once the player has entered the manor. She keeps her porch spot and her owner instead of being pulled back to the porch with no owner forever.
- If there is no room at the doorstep for Hillary on the second morning, the opening waits and tries again the next morning instead of moving on without her.
- The House dimension's native-mob cleanup no longer deletes mobs in the impossible hallway or anywhere past the labyrinth threshold.
- Mirrored projections no longer copy data attachments, so a projection of Hillary can't be tamed or treated as Hillary.
- Navidson's photograph stops searching camera spots once it has a good view, avoiding a one-tick lag spike.
- Days are counted by the mod, so `/time set` can no longer stall the opening, and sleeping in a manor bed now passes the night.
- The manor's front and back doors are crossed by their handles: clicking the shut door makes the dimension switch while the view is a still shut door, then the same door swings open on the far side and shuts itself behind you. The switch screen is now the last frame held still (no motion, darkening or minimum time), lifted once the sections around you have compiled; the manor's chunks stay loaded on both sides while anyone is near.
- The labyrinth no longer has a dimension of its own: its places are carved into solid slots stacked above the manor in the House dimension, and every door is a same-tick shift with a copied vestibule behind you. No loading screen, no fade.
- The room between rooms no longer uses a separate dimension or a transition: it stands above the manor in the House dimension, the player is shifted up and down by a relative move, and the door physically swings open. Copying the bedroom no longer duplicates its paintings (they were popping off the wall and dropping as items).
- The room between rooms now leads from the hall to the study: the familiar study door routes through a small room that cannot fit in the wall, and out the far side into the study. It is armed by a 50/75/100% roll from House morning 3 and switches over only when nobody is in or looking at the doorway. It is no longer treated as labyrinth space, and the hallway waits until someone has walked through it. Worlds with the earlier bedroom version have that door taken down and the room moved on the next start.
- The first night in the manor shifts the rugs; around day 3 the room between rooms appears; ten kinds of subtle change follow on quiet mornings; after the room and two of them, the impossible hallway gets a heavily weighted daily roll, at the end of the hall as before. Subtle changes continue after it opens.
- `/oldesthouse day [n]`, `/oldesthouse reveal rugs|room|hallway`, and a fuller `/oldesthouse status`.
- The House's site search no longer gives up in woods or hills: it searches 32-96 blocks out in loosening passes, clears trees (never anything player-made), and skips unloaded chunks instead of reading them as the bottom of the world. The letter and Hillary now wait for the House, older saves retry every morning, and `/oldesthouse opening house` spawns it now with a report.
- Hillary greets her recipient and waits to be greeted back (right-click, bone, or crouching facing her) before darting off towards the manor; ignored, she follows and whines.
- The labyrinth: a door in the impossible hallway's far wall into a solid plaster dimension, the gray junction and corridor, a dealer that re-deals doors on every arrival with a dry-spell guarantee and leaking vignette doors, and the first vignette, The Tell-Tale Heart's floorboards. `/oldesthouse door <place>` puts a test door to any of it wherever you stand.
- Hillary seeks: hold a vignette's object out to her and the next dealing that can have a vignette door does, with her bark leaking from behind it instead of the heartbeat. She scratches at the front door but never comes in.
- Seamless loops: the Five and a Half Minute Hallway, the hotel hallway with its climbing room numbers, and the spiral staircase that takes longer to climb than to descend. They are dealt as gray places, less often than the junction and corridor. `/oldesthouse labyrinth status` shows your laps while you are in one.
- Hide-and-clap: the blindfold (a head-slot item with its own black overlay), a counted start, claps that move and get louder when you wander, a glimpse of small bare feet, a wardrobe placed behind you at the end and a crayon drawing inside it. Peeking resets it with a note.
- The Red Room: a copy of the room you spend the most time in at home, dealt as a recurring vignette. Its bed works at night, loses you time and wakes you deeper. The labyrinth's slots now fill upwards above the manor and carry on below it, so a high manor keeps every place.
- Every chest and barrel in the manor now has a loot table for its room: pantry food in the kitchen, bottles and bowls in the scullery, candles and odds and ends in the parlour and landings, books and ink in the literary bedroom, fletching and scrap metal in the maker loft, moth-eaten leftovers in the box room. `/oldesthouse restock` applies them to an existing house.
