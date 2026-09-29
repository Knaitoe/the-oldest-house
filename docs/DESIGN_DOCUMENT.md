# The Oldest House: Design Document

Sep 28, 2026 · @Sean Price · revised in the repository to match decisions made since

## Revisions in this copy

This is the design document with the decisions made while building the opening sequence applied. Everything else is as written.

- **The house is a large manor.** The Oldest House stays a large, old Tudor manor with a fixed exterior (see [ARCHITECTURE.md](ARCHITECTURE.md)), not a 12-block house. Its interior lives in the house dimension at the exterior's own coordinates; the impossible space begins at the labyrinth threshold, the door that appears at the end of the hall.
- **Beds work, until the labyrinth.** Beds work anywhere in the manor. Past the labyrinth threshold they don't (sleeping quietly fails and no spawn point is set), except where a vignette says otherwise. Karen's room is the one bed past the threshold that sets your spawn.
- **Entry is the opening sequence.** Navidson's letter and a snapshot of the player's own house arrive first. Hillary appears the next morning and leads the player across ordinary Overworld terrain to the Navidsons' manor. No impossible doorway ever appears in the player's own home. See [OPENING.md](OPENING.md).
- **The dog is Hillary**, the Navidsons' gray husky, who turns up on the player's doorstep and guides them to her owners' manor. When the player crosses the front-door boundary, Hillary enters the literal Overworld proxy manor while the player alone transitions to the House dimension.
- **Capture exists.** The first capture is Navidson's snapshot: the player's settlement copied into the outside dimension, altered, and photographed. The camera strongly prefers the real bearing from the Navidsons' porch to the player's home, widening around the copied settlement only when that side is genuinely obstructed. A successful Capture must always yield the player's copied home, never the baked stock house. If the visible facade has no suitable upper window, only the copy is changed to add one.
- **Domestic familiarity comes before impossible architecture.** The House's perceived age does not advance until somebody has actually entered the manor. Ignoring the invitation can never cause the first impossible threshold to reveal itself off-screen.
- **Answered open questions:** the platform (NeoForge, Minecraft 1.21.1), the first expedition (following Hillary to the Navidsons' ordinary front door), and part of multiplayer (per-player timelines).

## Canon and core rules

The mod is about the Oldest House: one impossible, expanding structure that only the end of the Minotaur can destroy.

- **The house is the core.** Borrowed stories live only inside their own vignettes. None of their motifs appear in the house's hallways, anchors, or endings. The Usher crack, for example, exists only in the Usher vault.
- **Nothing destroys the house except the end of the Minotaur.** Mining, explosions, and fire leave it untouched.
- **The Minotaur is never seen in first person until the finale.** Before that, players glimpse it only when the house pulls the camera out of their body, in photos, and in miniatures. Its voice is the Growl.
- **Two endings.** The Minotaur kills the player and they are locked out of the house for good. Or they wound it, and the house collapses.
- **The Mother of Strays is original to this mod.** She keeps what players lose, and she answers the book's Pekingese story: she keeps everything nobody else would.
- **Assets are not a constraint.** Designs are not scaled back to save on models, creatures, or audio.
- **Platform.** Minecraft Java Edition 1.21.1 with a NeoForge mod, designed for single-player first.

## Design principles

Every vignette is built on one Minecraft verb, runs as a small saved state machine, and yields one object.

- **One verb per vignette.** Something players already do (climb, crawl, swim, smelt, write, trade, sneak) is turned against them.
- **Saved state.** Each vignette records which beat it's on and what the player did. Triggers: entering an area, what the player looks at, item use, block interaction, time, sleep.
- **Every visit ends whole.** Each visit stops at a natural point, and multi-visit vignettes resume where they left off.
- **One yield.** Each vignette gives one object, which feeds the dog and the carry-to-find system.
- **Four questions for every moment.**
  1. What does the player see, and what guarantees it?
  2. Why do they act, and what happens if they don't?
  3. What happens when they cheat: peek, dig, build up, leave, attack?
  4. What resets, and what persists?
- **Hurting a vignette's people ends the visit.** The lights go out, the door shuts behind you, and the next beat is colder. The one exception is Father, Son, Holy Rabbit, where hitting keeps you both alive.
- **Speech is never chat.** People speak through floating text displays beside them. Tom speaks through a walkie-talkie.
- **"When you look away" checks** use the camera mode and FOV the client reports to the server.
- **Timers run on in-game time spent in the house**, never real days.
- **Prose comes in page-sized beats.** A Minecraft book page holds about a short paragraph.
- **One of each role per wing.** Duplicate ghost children and old men were cut.

## Technical architecture

The house is a fixed manor whose interior lives in its own dimension. Past the labyrinth threshold, that dimension is solid, with carved rooms joined by teleport doors, and vignettes that need a sky live in a second dimension.

### Spaces

- **Overworld:** the player's world, their base, the house's fixed exterior, and the entrance door.
- **The house dimension:** the manor's interior at the exterior's own coordinates, with the nearby Overworld mirrored outside its windows, so crossing the walls changes dimension without changing position. Past the labyrinth threshold it is generated solid, with rooms carved out and placed from structure files built in creative mode. Beds work in the manor. Past the threshold they don't, except where noted, and compasses spin.
- **The outside dimension:** every vignette with a sky, including the courtyard, the lake, the cabins, the hotel grounds, the base copy, and the copy Navidson photographs. It lies past the labyrinth threshold, so its beds don't work either.

### Systems

- **Doors:** custom blocks that teleport the player. Rearranging the house means changing where a door leads, never moving blocks. Mounts don't come through.
- **Seamless loops:** instant teleports between identical copies of a room, keeping position and facing. Used for the hallway, the stairs, and timed corridors.
- **Vignette state:** saved world data per vignette (beat, flags, visit count), read by the door dealer.
- **NPCs:** one human entity type that swaps skins. Speech appears as text displays.
- **Capture:** the mod snapshots a region of the player's world into a copy (Navidson's snapshot, the Red Room, the base copy, the first shelter). Captures are taken a few chunks per tick and rebuilt over several ticks. Containers are locked or emptied, frames and stands fixed, and redstone and farms frozen, so nothing can be duplicated.
- **Client side:** the first-person lock, forced camera moments, borrowed-eyes cameras, overlays (blindfold, drowsiness), a custom subtitle display, and screenshot capture for the Record.
- **Attributes:** scale, reach, step height, and jump strength handle height changes.
- **Resource pack:** a custom font per narrator, textures, models, mono sound files, and custom music discs.
- **Map art:** handwritten pages are scanned and converted to maps for walls and photos. Navidson's snapshot is rendered on the server from a captured copy, so it needs no client screenshot.

### The house's own systems

These behaviors belong to the house itself, independent of any vignette, and carry the core horror.

- **Entry: the opening sequence.** Once a player has settled in (a bed, two nights slept, three days since joining), the Navidsons move in next door: the House appears near their base. Will Navidson's letter and a snapshot of the player's own house wait on the doorstep of the door they use most. The photograph strongly prefers the real Navidson-porch-to-player-home bearing; only the copied settlement may gain the impossible lit upper window. The following morning Hillary appears at the player's doorstep and leads them toward the Navidsons' manor, waiting when they fall behind. At the ordinary front-door boundary she sits visibly outside and refuses to enter while the player alone is transferred to the matching House-dimension interior. The player's home never receives an impossible doorway. The opening sequence is the only automatic House-appearance path.
- **The quarter-inch:** the exterior manor remains fixed and measurable. At first, discrepancies should be subtle enough to doubt rather than a literal one-block mismatch. The first undeniable proof of impossible volume is the interstitial room described below; the long hallway is a later escalation.
- **Early domestic progression:** the House earns impossibility in stages rather than jumping directly from ordinary manor to labyrinth.
  - **Domestic-night clock.** Track completed sleeps in beds inside the ordinary House-dimension manor separately from world age, opening-sequence sleeps, and later labyrinth progression. Count at most once per distinct Minecraft morning. The current prototype's Overworld-only wake clock is not the intended final progression clock.
  - **First domestic night: rugs.** On waking from the first completed sleep in the manor, every authored domestic rug quietly shifts to a different plausible color palette. No message, sound, particles, or animation. Only rugs authored as part of the manor may change; player-placed carpet is never swept up by the effect. The shift is persistent and the Overworld proxy must agree with the House interior.
  - **First impossible architecture: a room between rooms.** The first true spatial impossibility is not the long hallway. One existing ordinary interior doorway, preferably the hall-to-study door, begins routing through a small, mundane room that cannot physically fit inside the exterior plan. Entering from the hall leads into the interstitial room; crossing its opposite door leads to the study exactly where the player expected to arrive. Reversing direction works symmetrically. The original exterior shell and the two ordinary rooms do not move.
  - **Timing for the interstitial room.** Use the old House-appearance idea of hidden eligibility plus rising odds, compressed to keep this reveal near day three. The room is ineligible until the player has completed at least one domestic sleep and two further distinct House mornings have elapsed. On the first eligible morning, roll 50%; if it does not appear, roll 75% on the next eligible morning; guarantee it on the third eligible morning. One roll per world morning globally, never one roll per player. In ordinary play this places the reveal around House day 3-5 without making day 3 mechanically predictable.
  - **Witnessing rule.** Eligibility may be decided on a morning, but routing must never visibly rewrite while a player is standing in or looking through the affected doorway. Sleeping is the ideal cover. If the roll succeeds at an awkward time, arm the stage and activate it the next time the doorway is safely unwitnessed.
  - **Teleporter contract.** This is the first use of the House's internal teleport-door system. It is a same-dimension relocation, not an Overworld/House boundary transition. Preserve position within the doorway, facing, pitch, and sensible momentum; preload the destination chunk; add no transition overlay or duplicate door sound. The ordinary Minecraft door supplies the physical motion. The remote room must be registered as valid House space so the exterior-boundary code cannot eject the player.
  - **Pre-labyrinth status.** The interstitial room is impossible space, but it does not yet invoke the full labyrinth presentation. Forced first person, impossible-hall bed rules, the long timed corridors, and the major navigation hostility begin later. This lets the player experience one impossible fact before the House becomes openly adversarial.
  - **Hallway later.** The long impossible hallway becomes a second architectural escalation after the player has discovered and traversed the interstitial room. The current test behavior that reveals the hallway automatically at perceived age 3 is prototype scaffolding to retire, not canon.
- **Shifting layout:** once teleport routing exists, doors quietly change destinations, so backtracking never quite works. The interstitial room is the player's first lesson that a familiar door no longer guarantees familiar adjacency.
- **Infinite digging:** past the threshold the dimension is solid, so any tunnel only finds more wall.
- **The Growl:** a low sound with no source, sometimes directly below. It is the Minotaur's voice, heard long before it is seen.
- **Echoes:** the mod logs the player's door, furnace, and mining sounds and replays them later from deep inside.
- **Holloway's markers:** torches go out or vanish when unseen, and string trails get cut.
- **The spiral staircase:** a loop that takes longer to climb than to descend. An anvil dropped down it never lands.
- **The Five and a Half Minute Hallway:** always takes exactly that long, because it's a timed loop that sprinting, speed, and ender pearls can't beat. A torch placed in it is gone on the next pass.
- **Maps:** a custom map item shows only the house's exterior footprint, with the player still inside it after 2,000 blocks.
- **The compass:** points somewhere deep in the house where the player hasn't died yet. It's a compass aimed at a coordinate, styled as a recovery compass.
- **Lost time:** some doors cost days. The player comes home to overgrown crops, mobs in the yard, and pets gone to the Mother, faked with targeted edits around the base. Single-player only.
- **Blue "house":** the word is blue in all mod text, and in chat through a display-only trick.
- **The Record:** the mod quietly screenshots key moments (entering, each death, first sight of the Mother) and keeps them on the player's computer. They reappear on Karen's projector, in the album, and in the miniatures.
- **Photos that show more:** now and then an F2 screenshot taken inside holds a figure the player never saw. Build last.
- **The blind stretch:** a dark passage navigated by a custom subtitle display whose direction arrows start to lie.

## Finding vignettes

The house deals the doors: players can tilt the odds, but never summon a place on demand.

### Three kinds of vignette

- **One-shots:** over in a minute or two, and changed if they ever recur.
- **Multi-visit:** one beat per visit, resuming where the player left off.
- **Anchors:** always findable (Karen's room, the Mother's den, Zampanò's courtyard, Tom's camp).

### The dealer

- Doors to unfinished multi-visit vignettes come up more often.
- A long dry spell guarantees a new door.
- Nothing appears on demand.
- Zampanò's courtyard leads back to places already visited, never forward.

Doors leak instead of being labeled. Carpet creeps from under the hotel door into the gray, lake water seeps under another, and warm light and a murmuring TV come from behind a third. Now and then a leak lies and opens onto gray.

### Ways to seek

- **The dog (Hillary).** Use a vignette's object on her. She sniffs, hands the object back, and heads for the nearest door, which points to that vignette for a few seconds.
  - Fall too far behind and she waits at the next door. Wait too long and the door changes.
  - She refuses some places (the well) and waits outside the worst ones instead of going in.
  - Notes from her previous walkers are tucked under her collar.
  - If she dies, she goes to the Mother, and getting her back costs you.
- **What you carry.** Holding a vignette's object or note raises the odds of its door.
- **Sleep.** Sleeping in Karen's room sometimes wakes you somewhere specific, at the cost of lost time.
- **Sound.** A vignette's sound drifts into nearby halls when its door is close, like the hotel orchestra.

## Perception control

Inside the house, the house controls how the player sees.

- **Forced first person.** The perspective toggle (F5) is locked from the first hallway on. This also removes the easiest labyrinth cheat, since third person sees around corners and over walls.
- **Out-of-body moments.** A few times, the house yanks the camera into third person for 2 to 3 seconds, showing what stands behind the player, then snaps back. It's the only way to glimpse the Minotaur before the finale.
- **Borrowed eyes.** The camera can sit behind another entity's eyes, as spectator mode already does.
  - The set: stepping on your mark cuts your view to the TV camera.
  - The séance: through the medium's eyes, the room is empty. You're the ghost.
  - The pages: through the security camera, the pages are already on the floor behind you.
- **Height is identity.** Scale, reach, step height, and jump strength set who the player is in a vignette. They're child-sized in the rabbit, the well, the child's room, and the wheel's memory rooms, and full size everywhere else.
- **Safety.**
  - Restore height only in open space beyond a door, or the player can suffocate inside a block.
  - Apply height changes so they aren't saved, and check them on every login and dimension change. Death resets them.
- **Accessibility.** Scale every camera and FOV trick by the player's FOV effects setting. Every sound cue also appears in subtitles.

## Core characters

Five characters belong to the house itself; everyone else lives inside a vignette.

### The Mother of Strays

- Keeps every meaningful item that despawns anywhere, starting when the mod is installed, and every tamed pet that dies.
- Her den is walls of item frames. The pets follow her and won't come when called. Zampanò's missing cats are there, and a small dog with a bandaged head.
- One item back per visit. Take one and she follows 30 to 40 blocks behind in the dark until you return it or trade something loved: renamed, enchanted, or the item you've carried longest.
- The den shows the latest 50 items plus the loved ones.
- Hitting her costs you an item. She never leaves the house; take something out and she's waiting at the entrance next time.
- In multiplayer, friends' things turn up on her shelves.

### Tom

- An NPC wearing the player's skin, waiting at the entrance camp (a tent and a campfire).
- Talks through a walkie-talkie item. His lines garble into Minecraft's obfuscated text the deeper you go.
- Doesn't make it out of the collapse.

### Holloway

- The player who came before. His camp is a dirt hut, torch trails, chests of ordinary gear, and signs that get stranger.
- Looting the camp triggers his hunt on the next visit, in two or three scripted arenas. He tracks you by the torches you place.
- He drops the shield the finale needs.
- Yields his half-explored, wrong map.

### The dog: Hillary

- The Navidsons' gray husky. She turns up on the player's doorstep the morning after the letter, and the first bone from her recipient tames her. She leads the player over ordinary terrain to the Navidsons' manor and waits if they fall behind.
- During the opening she enters the **literal Overworld manor proxy**, not the House dimension. The player's matching-coordinate transition happens independently at the boundary. Later, once the labyrinth exists, tamed Hillary becomes the guide described under Finding vignettes and may refuse particular dangerous destinations.

### The Minotaur

- Heard as the Growl throughout, glimpsed only in out-of-body moments, photos, and miniatures, and finally seen in the finale.

## Writing, documents, and human drama

The notes carry the human stories: the horror sits on top, and the human story stays in the margins, the way the barn and the well do.

### Formats and rules

- Each narrator has their own custom font, as in the novel. Navidson's letter uses his.
- Struck passages are red with strikethrough, which is vanilla formatting. Purple is used exactly once, on the hospital note.
- Acrostics are built on paragraph or page starts, never line starts, because books wrap text automatically.
- Deeper in, books thin to one word per page, run backwards, or get crossed out.
- A second handwriting annotates older notes and argues with them. Late notes are signed with the player's own name.
- The Pekingese appears as an off-topic memory, then a retelling with details changed, then a scrap admitting it never happened.
- Expedition logs get shorter and more frantic.
- Handwritten pages become map art for walls. Recorded testimonies become custom music discs.

### Threads that tie the notes into one story

- **Mothers who loved too hard:** Pelafina, the Mother of Strays, Olivia Crain, Grace, the Yellow Wallpaper narrator, the hospital mother.
- **Siblings, one who went in and one who stayed:** Tom and Navidson, Karen and her sister, Roderick and Madeline.
- **Fathers:** Jack Torrance, Junior's father, the father in Father, Son, Holy Rabbit, Johnny's father.

### Human-drama sources and where they live

| Drama | Source | Where it lives |
| --- | --- | --- |
| Protective love that slowly becomes a plan | Olivia, The Haunting of Hill House (Netflix) | A mother's diary, Hill House |
| Letters to a husband, with a gap of days she can't account for | Grace, The Others | The Others |
| A note written at dawn by someone who never told; a mother's confession about the match | Hereditary | The miniatures workshop |
| A brother who heard his sister and didn't go down | The Fall of the House of Usher | The vault |
| A new mother forbidden to write, writing anyway | The Yellow Wallpaper | Behind the nursery wallpaper |
| A widow who kept building | Sarah Winchester | Ledgers through the Winchester wing |
| A salesman who moved the headstones but not the bodies | Poltergeist | The model home |
| A chambermaid who kept working after the room nearly killed her | The Stanley Hotel | Room 217's housekeeping log |
| A winter caretaker counting days sober while the snow rises | The Shining | The caretaker's quarters |
| Karen and her sister, and the well | House of Leaves | The barn and the well |
| A father who gave his son his own body | Father, Son, Holy Rabbit | The rabbit vignette |

## Vignettes: House of Leaves places

These places come from the novel the house grows out of, so several double as anchors.

### Zampanò's courtyard

*Anchor · verb: coaxing a cat*

- An overgrown apartment courtyard in the outside dimension, fixed at 3 a.m. Stray cats thin out each visit, because they're going to the Mother.
- His door is locked, and the last cat sleeps on the mat. Feed it raw cod and it gets up; the key was underneath.
- Inside, the windows are nailed shut and caulked. The walls are papered in handwritten map art, mostly atmosphere, with a handful of pages that come off as books. The trunk sits with gouges beside it, among his readers' notes and seven women's names scratched into a wall.
- The other apartment doors lead back to places already visited.
- Yields: a cat collar. Assets: map art, gouge and nailed-window textures. The cats are vanilla.

### The Whale

*Multi-visit · verb: writing back*

- Pelafina's room in an institute with three attics.
- Her letters arrive in the next chest the player opens, anywhere, spaced out by in-game time. They escalate, then stop, with a gap in the dates.
- Acrostics on paragraph starts spell a knock pattern that opens one attic door.
- Optional: drop a signed book in her mail slot, and her reply picks up keywords from what you wrote.
- Yields: her letters. Assets: mail-slot texture, her font.

### The barn and the well

*One-shot · verb: waiting in the dark · child height*

- A farmyard at night. Climb down a one-block-wide ladder shaft as the view narrows.
- At the bottom, the cover closes overhead and can't be opened from below. The player waits about a minute in total dark until it opens from above.
- Two sets of initials are carved at the bottom, and nearby notes give them meaning. What happened stays offscreen.
- Yields: a ribbon. Assets: well cover, carved-initials texture.

### The plain

*One-shot · verb: the spyglass*

- Blinding desert sun, and a vulture circling something small that never gets closer. It's never rendered as more than a distant silhouette.
- Zoom in with the spyglass: a shutter sound, and the photo is yours. Drop, store, or burn it and it's back by morning; only the Mother will take it.
- Navidson's apology letter sits on a lectern nearby.
- Yields: the photo. Assets: custom vulture (circles, never swoops), silhouette model, shutter sound, photo item.

### The hospital

*One-shot per attempt · verb: the call button*

- A night ward in purple fog, with an incubator, a chair, and a call button.
- The monitor alarm keeps sounding, and the player presses the button each time. The alarms come faster over a night compressed to about three minutes. At dawn they stop; the pressing never mattered.
- Meanwhile Tom calls for help over the walkie-talkie and the dog barks at the door. Leave and the ward resets.
- Yields: the purple note, the only purple text in the mod. Assets: incubator model, monitor audio, purple fog.

### Karen's room

*Anchor · verb: respawning*

- The only bed past the labyrinth threshold that sets your spawn: warm and lit, with a projector showing the Record's screenshots.
- Each time you respawn, one thing in the room has changed.
- Sometimes you wake to a figure in your own skin at the foot of the bed, gone when you move.
- Sleeping here sometimes wakes you somewhere specific.
- Assets: projector and screen models.

### Holloway's camp, Tom's camp, and the Mother's den

These three are covered under Core characters.

## Vignettes: the hotel

One mechanic runs the whole hotel, the tab: everything is free, and the house collects later, at a moment you don't choose. The next time you open any chest, something is missing. The hotel blends the Stanley's real history with the Overlook's fiction.

### Arrival and the dining room

*First visit · verb: taking the seat*

- Closing night of the season. Every table has its chairs stacked on top except one set for two. The orchestra plays to empty chairs, and the front doors lock behind you.
- Sit, and food appears on your plate. Eating it goes on the tab.
- Assets: orchestra audio, food props.

### The hallway

*Connective · verb: walking*

- Seamless loops, with room numbers that climb and repeat. The exit rule: walk against the rising numbers.
- Carpet runners over bare wood make the player's footsteps fall into a trike's rhythm.
- An office window shows mountains, a painted diorama behind glass.
- Assets: an original loud carpet pattern, wallpaper textures, number plaques.

### The fire hose

*Recurring · verb: turning your back*

- Coiled on a rack across the hall from 217, lying differently each time you pass. Later it's stretched across the corridor, and at the end it follows you. Closed doors stop it.
- Assets: a segmented hose creature, rubber-slither audio.

### Room 217

*Multi-visit · verb: leaving your things*

- The only made bed in the hotel. You can sleep in it, but it doesn't set your spawn.
- You wake with your inventory neatly sorted, the candles snuffed, and every torch you placed returned to you, stacked.
- On later visits she starts putting your things away in 217's drawers, and you have to come back for them.
- The door reads 217 until you look back, then 237. The patched floorboards drop you into the dining room, onto the table set for two.
- The chambermaid's housekeeping log runs from 1911 onward.
- Assets: patched-floor texture.

### The bar

*Multi-visit · verb: drinking on the house*

- A lone bartender. Free drinks heal you and go on the tab.
- Strangers along the bar retell the player's real deaths, all wrong, and tell different stories about a scarred man.
- Assets: NPC skins, drink textures.

### The ballroom

*One-shot · verb: standing on the floor*

- A piano plays in the empty room. Stand on the dance floor and ghost couples fill it around you.
- A flash whites out the screen, and the old party photo on the wall now has your face in the front row.
- Assets: piano model and audio, a photo template that takes the face from the player's skin.

### The grounds

*Multi-visit · verb: moving when unseen*

- A snowbound hedge maze with the hotel's master key at the center. A client-side blizzard fog makes building up to see over it pointless.
- Topiary animals made of leaf blocks change pose when you're not looking.
- The pet cemetery gains a headstone with the name of each tamed pet the player loses.
- Assets: optional headstone model. The topiary are groups of leaf blocks on display entities.

### The caretaker's quarters

*Multi-visit · verb: reading the log*

- One caretaker's log: days sober, entries shrinking as the snow climbs the windows, and his family unmentioned after a certain date.
- A typewriter beside a manuscript of one sentence repeated.
- Assets: typewriter model.

### The boiler room

*Chore · verb: maintenance*

- The boiler starts running only once the player has found it. Its gauge climbs by in-game days, and a lever vents it.
- Neglect it and it blasts. The room's contents burn, no blocks break, and the hotel goes cold: the dining room's fire goes out and 217's bed is unusable until it's restarted.
- Assets: boiler model, gauge states, hiss audio.

## Vignettes: haunted-house classics

Each classic gets its own vignette, and its motifs stay inside it.

### Hill House: the nursery corridor

*One-shot · verb: walking*

- The nursery doorway frosts your screen and slows you, using vanilla freezing.
- Pounding travels along the walls and stops at the door of whatever room you're in. Vanilla's zombie-banging-on-a-door sound works.
- Writing on the wall asks for the player, by username, to come home.
- In the cellar below, a wall knocks back as many times as you knock.
- A mother's diary here carries Olivia Crain's story.
- Assets: the wall writing.

### Hill House: the Red Room

*Recurring temptation · verb: recognizing your room*

- A copy of the base room the player spends the most time in, rebuilt deep inside. Every container is empty, redstone is disabled, and the size is capped.
- Its bed works, one of the exceptions past the labyrinth threshold. Sleep in it and you lose time and wake somewhere deeper.
- It's stage one of the copy arc; the base copy (We Used to Live Here) is stage two.
- Assets: none; it's built from the player's own blocks.

### The Others: the séance and the locked wing

*One-shot · verb: haunting*

- A family at a candlelit table who can't see you. Open a chest and they scream. Blow out a candle and they flee.
- Borrowed eyes: for a moment you see through the medium's eyes, and the room is empty.
- Two outcomes. Scare them out and the room is yours, with whatever they left behind. Or let the séance finish, and the medium says your name and asks you to go.
- The locked wing: no door opens until the last one is shut and the trapdoor shutters are closed. The post-mortem album's last page is you, from the Record.
- Grace's letters to her husband, with a gap of days, are found here.
- Assets: family skins. The candles are vanilla.

### Hereditary: the miniatures

*Multi-visit · verb: looking closely*

- A workshop table of scaled copies of rooms you've been in, with a tiny you in each. The newest has something behind you. Sometimes one shows a room you haven't reached yet.
- Each miniature needs one custom renderer; hundreds of display entities per model would lag.
- Assets: renderer code.

### Poltergeist: the model home

*Multi-visit · verb: turning around*

- A furnished model home. Turn from the kitchen table, turn back, and every chair is stacked. The stack teeters as you approach.
- A tree outside the kid's window grows a stage closer each visit, until a branch is inside.
- The salesman's binder sits on the counter: the headstones were moved, the bodies weren't.
- Assets: none; all vanilla.

### The Conjuring: hide-and-clap

*One-shot · verb: following claps blindfolded*

- **Setup.** A child's bedroom with open floor. A blindfold hangs on the bedpost beside a note in a child's handwriting: put it on, count to ten, follow the claps, take it off when you think you've found me.
- **The blindfold, not the Blindness effect.** Blindness still shows a few blocks, and milk clears it. The blindfold is a head-slot item with a full-screen overlay, like a carved pumpkin: black, except a thin strip at the bottom showing the floor around your feet. Taking it off is the player's choice, and the game's own rule prompts it at the end.
- **The count.** Put it on and a child's voice counts to ten.
- **The claps.** Three or four claps from open spots. Get within a block or two and the next sounds elsewhere; wander, and the current one repeats louder. Clap files must be mono, because Minecraft only positions mono sounds. Subtitle arrows keep it playable with the sound off.
- **The glimpse.** At the third clap, small bare feet step away through the strip under the blindfold.
- **Facing away.** At the final clap, the mod reads which way the player faces and silently places the wardrobe two blocks directly behind them. If that spot is blocked, it uses the nearest open spot within 45 degrees. Two claps come from inside it, the player turns on their own, and the first thing through the strip is the base of a wardrobe that wasn't there.
- **Payoff.** Inside, a crayon drawing of someone in a blindfold, drawn from exactly where the wardrobe stands.
- **Cheating.** Take the blindfold off early and the claps stop, the room resets, and a new note on the bed says you peeked. If the player never puts it on, a single clap sometimes comes from under the bed.
- **Persists.** Once found, the wardrobe stays open and the game isn't offered again.
- Assets: blindfold item and overlay, mono clap and counting audio, wardrobe model, a child skin (only the feet show), the drawing as map art.

### The Tell-Tale Heart: the floorboards

*One-shot · verb: sneaking*

- Hidden sculk under a false floor speeds a heartbeat (the Warden's sound). Crouching makes no vibrations, as players know from Ancient Cities.
- If the heartbeat maxes out, the lights go out and you're back in the hall. Pry the loose board with an axe and you find the caregiver's note.
- Assets: loose-board texture.

### The Masque of the Red Death: the seven rooms

*One-shot · verb: freezing on the chime*

- Seven rooms, each walled in one color of stained glass with light behind it, full of masked dancers and music.
- When the clock chimes (a vanilla bell), the dancers and the music stop. Move during the chime and the red figure appears in your room.
- The goal is stopping the clock in the black room, which ends the party.
- Assets: dancer skins, music.

### Usher: the vault

*Multi-visit · verb: the lid*

- A coffin with a woman lying inside, alive.
- Close the lid and scratching grows each visit while a crack splits the vault's walls. The crack exists only here.
- Leave it open and next visit she's gone, somewhere in that wing.
- Assets: coffin model, crack texture, scratching audio.

### The Yellow Wallpaper: the nursery

*Multi-visit · verb: peeling*

- Animated wallpaper whose pattern shifts at night, with a figure creeping behind it.
- Strip it with an axe to peel. Only the squares where you last saw the figure hide her contraband pages, so you watch at night first.
- Assets: animated wallpaper textures.

### The Winchester wing

*One-shot · verb: climbing to nowhere · low priority*

- Stairs into ceilings, a door onto a survivable two-story drop, and a slab switchback staircase that takes forever to rise one floor.
- Sarah Winchester's construction ledgers sit on the desks.
- Assets: none.

### Skinamarink: the child's room

*One-shot · verb: losing exits · child height*

- Each time you look away, a door or window becomes wall. Toys hang on the ceiling, and mobs named Dinnerbone render upside down.
- The last exit is a crawl under the bed.
- Assets: none.

### Crimson Peak: the great hall

*Multi-visit · verb: playing recordings*

- A hole in the roof, with snow layers deepening each visit. Shovel them away and the floor underneath is red.
- Wax-cylinder discs from the wives before you are hidden around the hall and play on a jukebox.
- Assets: disc audio.

### Bly Manor: the Lady's route

*Recurring · verb: staying out of her way*

- She walks a short, fixed route each night into the lake room, and anyone in her path is dragged under.
- Dripping announces her before she's seen. Hide in closets.
- Assets: a blank-faced NPC skin, dripping audio.

## Vignettes: We Used to Live Here

Every We Used to Live Here beat plays out in a copy of the player's own base, occupied by the family.

### How the copy works

- **Snapshot:** taken the first night the player sleeps at home after first entering the house. It covers about 64 by 64 blocks and 48 tall, centered on their bed or the spot where they spend the most time, captured a few chunks per tick. It stays frozen from then on. (Navidson's snapshot already uses the same capture, earlier and smaller: see [OPENING.md](OPENING.md).)
- **Timing:** dealt only several in-game weeks later, so the copy has fallen out of date. With no base yet, it waits until the player has slept at home a few nights.
- **Placement:** built in the outside dimension over several ticks, with edges that fade into gray fog and walls.
- **Safety:** chests are locked, item frames and armor stands fixed, and redstone, hoppers, pistons, and farms frozen. Pets and livestock are copied as unowned versions.
- **Reading the base:** rooms are identified by contents, so a bed means bedroom and a crafting table with furnaces means kitchen. The front door is the one the player has used most.

### The visits

1. **The knock.** Your own front door is locked. Knock, and the father answers and asks what you want. You ask to come in on a dialogue screen, and he lets you in for a quick look.
2. **The tour.** They show you your house as if it were theirs, with floating text in each room. What they're proud of is what you've since changed: the window you removed, the wall you knocked out.
3. **The extra door.** The copy has one door you never built, in a wall that was solid in your real base. It's locked for now.
4. **Dinner.** Their table sits in your kitchen; in a cramped base, they eat standing around your crafting table. Dinner is suspicious stew. Eat it, or refuse: the lights dim, the father shows you out, disappointed, and the next visit they're colder.
5. **The youngest goes missing.** Mid-dinner she's gone. You search your own house by the sound of her giggling, and knowing your base is the player's advantage. She's at the extra door, now open, with stairs going down into the labyrinth.
6. **A different family.** Next visit, the skins and head count have changed, and so has the family photo on your wall.
7. **The heirloom.** One item frame holds a copy of your most-used weapon, and it's the only frame that isn't locked. This is where the finale's real-or-copy problem begins.
8. **Asked to leave.** Overstay and they grow cold until the father asks you to go. Refuse, and the lights go out and you're standing in the gray hall.

### The old man's cabin

*Multi-visit · verb: asking his name*

- A small snapshot, about 16 blocks square, of the first place the player ever slept, taken when the mod first sees them set a bed.
- The old man lives in it alone in the dark, under a different name every visit.
- He can tell an original from the house's copy, which matters for the finale.
- Optional: the window lamp blinks his name in Morse. Type it near him and he answers differently from any other visit.

Assets: several family skin sets, the family photo as map art, the dialogue screen, the old man's skin.

## Vignettes: Stephen Graham Jones and Paul Tremblay

Two Jones father stories sit side by side: in Mapping the Interior a father feeds on his son, and in Father, Son, Holy Rabbit a father feeds his son with himself. Much of Jones's material is specifically Blackfeet, so borrow structures and human dramas, and be careful lifting Blackfeet elements directly.

### The Only Good Indians: the elk through the fan

*One-shot · verb: climbing*

- A living room lit only by a flickering ceiling-fan light. Fixing it means climbing within reach, and the ladder stops two rungs short, so players build up the rest.
- From the top, through the blades, an elk lies on the carpet. It's drawn only in first person from that height, and only on the frames when the blades don't cover it. Stay too long and the blades knock you off onto the hearth.
- Next visit, there's a tape outline on the carpet. The visit after, the tape is peeled up and an elk tooth lies where the head was.
- Yields: the tooth. Assets: fan model, elk model, tape-carpet texture, tooth item.

### Mapping the Interior: the crawlspace

*Multi-visit · verb: crawling*

- A small manufactured home in its own yard. The first time you enter the living room, a figure crosses from the kitchen doorway to the hall, kept as a silhouette.
- A hatch opens into a crawlspace where the player is forced to crawl, and it visibly runs past the house's walls. A map carried down there shows your marker sliding outside the roofline, as a bonus.
- The younger brother sleeps longer each visit while the figure grows more solid. Deep underneath is a pile of the father's things. Break it and the draining stops. Take them and the dog can track their scent, but the brother keeps fading.
- Yields: the choice, plus a photograph if you take the things. Assets: silhouette skin, photo map art.

### Father, Son, Holy Rabbit

*One visit over several compressed nights · verb: being fed · child height*

- **The reading.** The title puts the rabbit where the Holy Ghost should be. What the son eats is communion, and the player never sees the knife; they work it out.
- **Setup.** A blizzard at night, a single spruce with a dry hollow under it, and the father, who has only a knife. Everything the player carried is frozen: no eating their own food, no potions. Snowballs are edible here and do nothing.
- **Child height.** The hollow fits the player, not him, so he sits half outside, taking the wind. He hands things down from above, and the initials he carves are above the player's head.
- **Staying awake.** The cold dims the player's vision toward black. When it's nearly dark, the father slaps them for half a heart and their vision clears. The player can hit him back, and it does the same for him.
- **The rabbit.** A white rabbit's tracks circle the tree, and it can't be caught. If the player names it with a name tag, the father calls it that from then on.
- **The first night.** He goes out with a sharpened stick, comes back with it, and throws the hide out past the tree after you eat it raw.
- **After that.** One night he returns empty-handed, one leg frozen from breaking through the creek. From then on he drags it, and still brings meat every night, but never a hide. Every morning the rabbit you named sits at the edge of the hollow, alive.
- **The carving.** A name appears on the trunk beside the initials that nobody carved while you watched.
- **The way out.** Minecraft won't let you sprint at three hunger shanks or below, and the only way out is a distance you can only cover sprinting before the cold kills you. So you can only leave if you've eaten what he brought.
- **The end.** You wake and he's gone. His dragging leg has plowed a trench through the drifts, too deep for a child to climb out of, so his trail is the only path, and it's the way out. Where it ends: his knife, and a rabbit's foot, the first thing the rabbit ever gave up.
- **Refusing.** Without eating you can't sprint. Try the trail and freeze, or go back to the tree, where he tries again the next night, weaker.
- **Cheats.** Elytra are disabled here, thrown ender pearls vanish into the storm, and mounts don't come through the door.
- Assets: father skin with a bandaged-leg variant, drag-trail snow texture, blizzard fog, drowsiness overlay, carved trunk. The rabbit and the rabbit's foot are vanilla.

### The Buffalo Hunter Hunter: the confession

*Multi-visit · verb: writing*

- A study where the same visitor sits each visit and tells one chapter on text displays. Leave early and he resumes mid-sentence next time.
- Carry a book and quill and his words write themselves into it.
- Over the visits, the confession becomes an accounting of the player, read from Minecraft's statistics: villagers, wolves, iron golems, and wandering traders killed. The story was about the listener.
- Later, the journal you filled turns up sealed inside a wall elsewhere, signed with your name.
- Borrow the structure, not the Marias Massacre history.
- Yields: your own journal. Assets: the visitor's skin.

### My Heart Is a Chainsaw: Drowned Town

*Multi-visit · verb: breath*

- A lakeshore at night, with a flooded town below: streets, a school, a church.
- A custom shore creature hunts you but can't enter water, so the lake is safe and the air bar is the clock. Doors, bubble columns, water breathing, and turtle shells all work.
- The school's essays are waterlogged. Dry them in a furnace set up on the shore while the creature comes for you. They explain horror-movie rules that are really the house's rules.
- On a later visit, a key from the school opens the church.
- Yields: the dried essays, the church key. Assets: the shore creature, wet and dried page items.

### Disappearance at Devil's Rock: pages on the floor

*Multi-visit · verb: trying to catch it*

- Two or three new diary pages lie on the floor each visit, placed only where no player can see.
- Every attempt to catch it fails with a trace: tripwires trip, sculk pulses, pressure plates click. Stand guard all night and the pages land behind you, or turn up in your inventory.
- The security camera prints motion timestamps. Through its borrowed eyes, the pages are already on the floor behind you.
- The last page ends the story in this room.
- Yields: the pages. Assets: loose-page block, camera model.

### "A Haunted House Is a Wheel Upon Which Some Are Broken": the wheel

*Available until finished · verb: choosing doors · height as time*

- A family house of numbered doors. Most doors loop back to earlier rooms, each slightly changed.
- The memory rooms are child-sized, and the present-day rooms are full size.
- The unnumbered front door leads back to room one until you've reached the mother's room. Then it's the way out, and it returns you to full height: you grow up by leaving.
- Yields: the house key; the wheel leaves the deck for good. Assets: none.

### A Head Full of Ghosts: the set

*One-shot · verb: hitting your mark*

- A family home rigged as a TV set, with light stands, a camera on a tripod, and tape X's on the floor.
- Step on an X and the lights come up, your view cuts to the TV camera, and the family plays a staged scene. Step off your mark and the real thing happens behind you.
- A confession booth records what you type in chat, and your words turn up later in a transcript.
- Yields: a tape, as a music disc. Assets: light stand and tripod models, tape-X texture, disc audio.

### The Cabin at the End of the World: the cabin

*One-shot · verb: giving something up*

- A lake cabin at dusk, with a jar of grasshoppers on the porch. Four polite strangers knock and ask you to choose something to give up: a named item, a pet, an enchanted tool.
- Refuse, and one of them walks into the lake and a vignette door closes forever. The cabin's TV shows which place just went dark.
- Agree, and the thing goes to the Mother, where you can see it but never have it back.
- Yields: the jar. Assets: four stranger skins, jar item, TV.

## The finale and the endings

Opening the locked boy's cell starts the finale: the player either dies and is locked out, or wounds the Minotaur and the house collapses. Players who never open the cell never reach an ending, unless the Minotaur comes looking (see Open questions).

### The cell and the fight

- The cell is scratched from the inside. The struck-through Minotaur play sits on a lectern outside, in red strikethrough.
- A nameless boss bar appears and never moves, whatever players throw at it.
- The Minotaur charges. Block the charge with a shield and it's stunned for a moment, the way a ravager is. Holloway's dropped shield teaches this.
- Only the original of the player's most-used weapon wounds it; the house's copy passes straight through. The old man can tell which is which. If the original went to the Mother, the player has to get it back first.

### Killed: locked out

- The player respawns outside the house's front door, and it never opens again.
- Everything they had inside goes to the Mother, permanently.
- Knock, and something inside knocks back.
- Unfinished vignettes stay unfinished. The player is the one at the door now.

### Wounded: the collapse

- It drags itself back into the cell it came from.
- The collapse begins, and Tom doesn't make it out.
- The bottom: a vast dark floor with drop-offs, under the Darkness effect. Burning a collected note lights a few blocks for a few seconds.
- The pet the Mother kept finds you and leads you out, if you left her shelves alone or gave her something she wanted. Otherwise you find your own way and come home months later to an overgrown base.
- The entrance becomes an empty lot. The entrance door in the player's wall is gone, and the wall it replaced is back as it was.
- Some time later, a chest turns up somewhere mundane: a village, a mineshaft, the player's own base. It holds the notes in a different hand, with annotations the player never wrote and one page from a room they never entered. After that, the notes keep surfacing in loot chests and in wandering traders' stock.

Assets: the Minotaur, a custom creature with charge, stun, and wounded-crawl animations, and a voice built from the Growl.

## Assets

The Minotaur is the centerpiece asset; most human characters are skins on one shared NPC type.

### Custom creatures

| Creature | Used in | Needs |
| --- | --- | --- |
| The Minotaur | The finale | Full model; charge, stun, and wounded-crawl animations; voice built from the Growl |
| Shore creature | Drowned Town | Pathfinding that treats water as impassable |
| Fire hose | The hotel | Segmented body; repositions when unseen, follows at the end |
| Vulture | The plain | Circling behavior only, never swoops |
| The Mother of Strays | Her den | Optional; she can be an NPC skin instead |

### NPC skins (one entity type)

- Tom (the player's own skin, applied at runtime), Holloway, the family (several sets), the old man, the father in Holy Rabbit plus a bandaged-leg variant, the confession's visitor.
- The four cabin strangers, the séance family and medium, masked dancers, the bartender and bar strangers, ballroom ghost couples.
- The Lady (blank face), the hide-and-clap child (only feet show), the crawlspace silhouette, the woman in the Usher coffin.

### Models and props

| Model | Used in |
| --- | --- |
| Ceiling fan (animated) and lying elk | The elk through the fan |
| Projector and screen | Karen's room |
| Boiler with gauge states | The boiler room |
| Typewriter | The caretaker's quarters |
| Incubator | The hospital |
| Piano | The ballroom |
| Coffin | The Usher vault |
| Wardrobe | Hide-and-clap |
| Light stand, tripod camera | The set |
| Security camera | Pages on the floor |
| TV | The cabin |
| Well cover | The barn and the well |
| Distant silhouette | The plain |
| Headstone (optional) | The hotel grounds |

**Items:** blindfold, walkie-talkie, the photo, cat collar, elk tooth, jar of grasshoppers, custom map and compass, wet and dried pages, ribbon, loose pages, the tape. Built for the opening: Navidson's letter (a written book in his font) and the snapshot (a locked map).

**Blocks:** the entrance door (built for the opening).

**Textures and blocks:** gouges, nailed windows, carved initials, the carved trunk, the Usher crack, the scratched cell, patched floorboards, tape outline carpet, tape X, drag-trail snow, animated and peeled wallpaper, loose board, loose page, mail slot, hotel carpet and wallpaper, room-number plaques.

**Drawn in code:** blindfold and drowsiness overlays, the custom subtitle display, the dialogue screen, blizzard fog, the projector's live screen, the miniatures renderer, the ballroom photo template. Built for the opening: the snapshot renderer.

**Audio** (mono for anything positional): the Growl, claps and a child counting, the hotel orchestra, the piano, monitor alarms, a camera shutter, coffin scratching, dripping, boiler hiss, rubber slither, knocking, the wax-cylinder testimonies, the set's tape. Vanilla covers the heartbeat (Warden), the pounding (zombie on a door), and the chime (bell).

**Fonts:** one per narrator, Navidson's included.

**Map art:** Zampanò's walls, the family photo, the post-mortem album, the crayon drawing, and handwritten pages. Navidson's snapshot is rendered from the player's own base.

## Consistency fixes made while compiling

Applying the rule that borrowed motifs stay inside their own vignettes changed a few earlier decisions.

- Karen's room is core, so it holds no borrowed motifs. The Hill House pounding went back to the Hill House corridor, and the Poltergeist tree went to the Poltergeist model home.
- The boiler now heats only the hotel. Neglecting it no longer affects Karen's room or the respawn point.
- The family's version of your base holds only We Used to Live Here beats. The stacking chairs moved to the Poltergeist model home. The cold spot and the knocking cellar wall moved to the Hill House corridor.
- The collapse no longer references the Usher crack. The crack exists only in the vault.
- Forced first person replaced the free F5 check. The figure behind you now appears only in involuntary out-of-body moments.
- Budget swaps were reversed. The vulture is a custom creature instead of a retextured phantom, and Drowned Town keeps its custom shore creature.

## Open questions and technical risks

Four design questions are still open, and most technical risk sits in capture, teleports, and client-side camera work.

### Answered

- **Platform:** NeoForge, Minecraft 1.21.1.
- **The first expedition:** the opening sequence. Navidson's letter prepares it, Hillary turns up on the doorstep, and she leads the player to the Navidsons' ordinary front door. The player transitions to the House dimension at the boundary while Hillary physically remains in the Overworld proxy manor.

### Open questions

- **An ending for everyone:** does the Minotaur come looking once enough vignettes are finished, or can a player who never opens the cell play forever?
- **The Skinamarink crawl:** does the crawl under the bed lead into the Mapping the Interior crawlspace, linking two borrowed vignettes, or simply out?
- **What counts as loved** for the Mother's trades: renamed, enchanted, and carried longest, or something else?
- **Multiplayer:** each player already has their own letter and Hillary while the house itself is shared. Still open: whose base gets copied for shared vignettes, and how lost time works when world time is shared.

### Technical risks

- **Performance:** placing the base copy (spread it over ticks), the miniatures (one renderer each), map-art counts on Zampanò's walls, and unseen checks across many objects.
- **Seamless teleports:** the destination's chunks must be loaded before the jump, or the loop visibly pops.
- **Duplication:** copied containers, item frames, and farms must stay locked or frozen.
- **Height changes:** suffocation if full height is restored in a tight space, and players stuck small after a crash.
- **Screenshots:** the Record and photos that show more may clash with shader mods. (Navidson's snapshot avoids this by rendering on the server.)
- **Weather:** weather in custom dimensions isn't reliable, so snow is placed rather than fallen.
- **Chat:** rewriting chat on the server fights Minecraft's chat signing, so blue "house" in chat stays display-only.
- **Motion sickness:** forced camera moments and FOV changes must scale with the FOV effects setting.

## Cut list

Nine ideas were cut or replaced along the way; each one's job is either covered elsewhere or broke a rule.

| Cut | Why |
| --- | --- |
| Ash Tree Lane (an empty lot, notes in a stranger's mailbox) | The chest of notes after the collapse does the same job |
| Clocks and the armchair (The Conjuring 2) | A second old man insisting it's his house; the family and the old man cover it. Not literature. |
| Abigail, the girl only you can see (Hill House) | One ghost child too many |
| The family moving into the player's real base | The locked-out ending covers it, and the family lives in the base copy instead |
| Tom's bee nest gift | A Jack Torrance beat pinned on the wrong character |
| A house-wide Usher crack | A borrowed motif; it now lives only in the vault |
| Notes appearing in a brand-new world | Replaced by the chest turning up later in the same world |
| Checking behind you with F5 | Replaced by forced first person and involuntary out-of-body moments |
| Budget stand-ins (retextured phantom, hooded shore NPC) | Assets aren't a constraint |
