# Design outline for The Oldest House

This file records the current design decisions so implementation does not outrun the concept.

## Central rule

There is one anomalous structure: The Oldest House.

Its overworld exterior is a fixed authored design. The facade does not grow, morph, or advertise supernatural behavior. Impossible space is confined to what lies behind it.

The technical implementation should be boring and dependable even when the player-facing result is impossible:

- Java 21.
- NeoForge 1.21.1.
- No core mixins unless a later mechanic proves genuinely impossible without one.
- Persistent global The Oldest House state in Overworld `SavedData`.
- Modular interior cells and a persistent topology graph rather than one enormous contiguous structure.
- Generate/instantiate only what players can currently reach or interact with.

## Appearance

The world begins normally. The Oldest House does not exist at world creation.

The spawn trigger is **settlement residency**, not generic exploration and not a cursed-item quest.

A player establishes the candidate settlement by repeatedly sleeping through nights in the same home area. The initial implementation counts five successful overnight sleeps whose wake positions stay within 32 horizontal blocks of the same settlement anchor. Moving outside that area begins a new candidate settlement count.

Reaching the threshold only makes the world **eligible**. It does not spawn The Oldest House immediately.

Eligibility initializes a hidden appearance chance at **5%**. The same morning that establishes eligibility cannot spawn The Oldest House.

Beginning on the following morning, the mod makes one hidden appearance roll per Minecraft day using the current chance. If The Oldest House does not appear, the chance for the next eligible morning performs a random walk:

- 50% chance to increase by 2-5 percentage points,
- 50% chance to decrease by 1-2 percentage points,
- minimum 5%,
- maximum 100%.

Because upward movements are larger than downward movements, the chance has a gentle long-term upward drift while remaining capable of falling from one night to the next. The spawn should therefore feel increasingly plausible without becoming a visible or deterministic countdown.

If a roll succeeds, the game searches for reasonably flat, empty terrain near the established settlement and places The Oldest House there. If no safe site is available, nothing is destroyed; the chance still changes for the next eligible night and the system tries again later.

No advancement, toast, chat line, sound sting, or other notification reveals eligibility, probability changes, or the successful appearance.

One morning, The Oldest House simply exists on ground that was previously empty.

The exterior remains permanently measurable and trustworthy.

## Perceived age

After The Oldest House appears, the mod tracks how long it has perceived the structure as having existed in the world.

The initial implementation advances this value once per successful Minecraft morning while The Oldest House is present. It is persisted separately from ordinary world time so development commands can fast-forward or set it directly.

Future architectural and narrative stages should key off perceived age rather than requiring testers or players to wait an exact amount of real time. Development command `/oldesthouse advance [days]` exists specifically to exercise these stages.

The current transition prototype reveals the first impossible door at age 3. That threshold is deliberately provisional and isolated in `HouseStageManager` so later pacing work does not require architectural changes.

## Domestic phase

The Oldest House is useful before it is scary:

- furnished,
- lit,
- safe from ordinary hostile spawning,
- suitable for storage and sleeping,
- player-modified domestic rooms become protected anchors.

The player should have time to adopt it as property.

Later, a door appears in an interior wall where no door existed before. The exterior has not changed. Space beyond the door cannot fit inside the measured shell.

## Domestic boundary transition

The fixed Overworld structure is the exterior shell. The usable domestic interior exists in The Oldest House dimension at the same X/Y/Z coordinates.

Crossing into the shell's physical bounds changes dimension without changing coordinates, facing, or motion. The normal front door is the expected route, but a player who breaks a window or cuts through an exterior wall still enters the same interior rather than discovering backstage geometry.

The transition presentation is context-sensitive. The server classifies the crossed boundary as the authored front door, a known window opening, or another breach and sends that context to the client before the dimension change. The client then uses a matching short vignette rather than always showing a door.

The unavoidable dimension-loading interval is intentionally disguised rather than eliminated. Durations vary slightly so the pause does not become a perfectly repeatable teleport tell.

## Interior

The impossible interior lives in a controlled interior dimension of The Oldest House.

It is represented internally as a graph of modular rooms/corridors/stair modules. Connections can move players between distant physical cells while preserving the illusion of continuous architecture.

The Oldest House grows a little each night by changing/unlocking graph state. New physical cells are generated only when approached.

The main staircase goes deeper on later visits.

## Navigation horror

The mod should attack ordinary Minecraft navigation habits rather than depend on cinematic effects.

### Torches

Players often mark one wall with torches to find their way home. The Oldest House records player-placed torches and, rarely while unobserved, can:

- move one,
- add one,
- alter spacing.

This must remain rare enough that the player can doubt their own memory.

### Coordinates

Do not falsify F3 in the initial design.

F3 tells the truth. Modular transitions make the player's experienced travel disagree with the coordinates: forty visible blocks may correspond to eleven actual X blocks, or several flights of stairs may barely change Y.

### Maps

Maps should work. Their honesty is the problem.

Because interior cells occupy impossible/disconnected real coordinates, an interior map can naturally show a footprint larger than the exterior or disconnected architecture. Old and new maps may disagree after topology changes.

### Measurement

A custom measuring tool can return slightly different plausible readings for the same span.

Earlier explorer notes tell players to count steps, producing another source of measurement that may disagree.

## Sound and population

The House is nearly silent and mostly empty.

Do not fill it with conventional hostile mobs.

Occasional distant growls foreshadow the Minotaur for a long time before there is any physical entity to fight.

## Explorer notes

Earlier explorers leave behind notes, measurements, journals, supplies, and contradictory observations.

They serve three jobs:

1. tell a story worth completing,
2. teach navigation behavior such as counting steps,
3. provide evidence that the player's experiences are not unique.

## Rewards

There must be useful rewards found nowhere else, improving with depth.

The Oldest House should not become a conventional loot dungeon. Unique tools and artifacts should ideally relate to understanding, navigating, or surviving the House.

## Death

Death inside creates a strong return loop.

Ordinary dropped gear should be preserved rather than simply despawn. The room containing the items remains, but the topology may rearrange before the player returns.

The Oldest House changes the route to the gear, not the gear itself.

Compatibility with normal Minecraft death rules and common grave/keep-inventory behavior is preferred over replacing the death system wholesale.

## Dog

The player eventually finds a lost dog inside.

It becomes a genuine companion and useful ally, with enough time for ordinary Minecraft attachment to develop. Possible House-specific behaviors include reacting to bad doors, growls, topology changes, or lost belongings.

## Mother of Lost Things

A rare humanoid presence inspired by the woman who calls herself a mother to lost things.

She should not be a normal roaming hostile mob.

Her idea of caretaking is cruel. She becomes interested in the lost dog. Eventually the House separates the player from the dog; recovering it leads to an encounter with the Mother, who kills it with abrupt, casual violence.

The current practical staging idea is a fall from a great height using ordinary Minecraft movement/physics rather than gore or elaborate animation.

Text dialogue is straightforward. Branching dialogue remains optional until choices have meaningful consequences.

## Minotaur

The Minotaur is effectively endgame/conclusion, not a routine mob.

For most of the mod it exists as sound, testimony, and abstract The Oldest House state. A physical entity should only be instantiated when a local encounter requires one.

The final resolution is intentionally undecided. Do not reduce it prematurely to a standard boss health bar.


## Dimension-backed domestic interior prototype

The active architecture now treats the Overworld structure as the fixed exterior shell and the dedicated House dimension as the actual usable interior from the moment the player enters.

The House dimension uses the exterior's exact X/Y/Z coordinates. Crossing into the structure's physical bounds from the Overworld changes dimension without changing position, facing, or motion. The normal front door is therefore the expected entrance, but mining through an exterior wall or roof does not reveal backstage geometry; entering the volume still reaches the same House interior.

To preserve the ordinary-looking main-floor windows, initialization copies a 32-block radius of nearby Overworld blocks around the structure into the House dimension at matching coordinates. The dimension uses Overworld visual effects, skylight, a plains sky, and synchronized day/weather state. The windows are ordinary glass looking at ordinary copied blocks rather than portal surfaces.

The proxy exterior is currently a one-time snapshot. A later synchronization pass should update changed visible terrain selectively rather than overwrite player-modified interior rooms.

NeoForge 1.21.1 supports custom dimension-transition screens through `RegisterDimensionTransitionScreenEvent`. The prototype uses this instead of a mixin to replace the normal loading presentation with a close-up door texture and brief diegetic text while the target level loads.

The age-gated impossible rear door still exists, but it is now created inside the House dimension rather than in the Overworld shell.


## Mirror synchronization and direct impossible hallway

The initial dimension copy no longer rebuilds or clears a copied House. It writes the Overworld block states directly into the interior dimension with client-update-only flags. This avoids neighbor-physics cascades that previously created dropped block debris during the first transition.

After initialization, player-driven changes in the shared visible region are mirrored at matching coordinates in both directions. The current synchronization layer queues break, placement, multi-placement, and right-click interactions and resolves them at the end of the server tick, after vanilla has committed the real block state. Neighbor positions are included so two-block doors and connected panes stay visually consistent.

The rear impossible corridor is explicitly excluded from mirroring. At the provisional age threshold, the interior partition gains its door and a sealed 56-block corridor is constructed directly behind it in the House dimension. No secondary teleport is used. Because the Overworld structure is fixed, rear-facing and rear-side windows are intentionally omitted from the authored facade so ordinary windows never gain a sightline to interior-only geometry.

The custom transition presentation now has a minimum visible duration of 1.65 seconds. Its purpose is not to pretend the dimension handoff takes no time; it turns that unavoidable pause into a deliberate door-focused beat instead of an unreadably fast loading flash.


## Structural resistance beyond the domestic layer

The ordinary domestic rooms remain editable because player ownership is part of the premise.

Generated impossible architecture follows a different rule. In the current hallway prototype, the structural floor, ceiling, side walls, and terminal wall cannot be mined by players and are removed from explosion block lists. Decorative carpet and lanterns are not protected, and player-placed torches, blocks, markers, and other objects remain ordinary.

This distinction should persist as the graph system grows: players may annotate and inhabit impossible space, but they cannot simply quarry through The Oldest House's generated structural fabric to bypass topology.


## Transition presentation timing correction

Recorded playtesting exposed two presentation bugs. First, the wrong GuiGraphics blit overload treated the desired screen-space door size as texture-coordinate size, producing a tiled spruce pattern. The door vignette now uses separate destination and 16x16 source dimensions.

Second, sending boundary context and changing dimension in the same server tick could allow the dimension-transition screen to be chosen before the context payload had been handled. Crossings now use a one-tick lead: classify boundary, send context, then perform the matching-coordinate dimension handoff on the following tick. The authored front-door hitbox was also narrowed so breaking the plaster immediately beside the door is classified as a breach rather than a door.


## Native terrain mirror

The finite copied-landscape model was rejected after playtesting exposed truncated terrain and incorrect water presentation.

The House dimension now uses `minecraft:overworld` noise settings, the Overworld multi-noise biome preset, and NeoForge's server-seed dimension option. At matching coordinates, untouched terrain is therefore generated from the same seed rather than copied into a flat dimension.

The synchronization layer becomes an **overlay**, not a terrain generator. During initialization it compares nearby House-dimension blocks with the real Overworld and writes only differing states. This naturally captures the authored House and pre-existing player edits while leaving matching water, terrain, biome context, lighting, heightmaps, and distant chunks to vanilla generation.

Existing chunks generated under the older flat-dimension prototype cannot be repaired merely by changing the datapack generator and should not be used to judge this version's terrain continuity.

## Transition sprite scaling correction

The attempted destination-size blit still tiled Minecraft's block texture in recorded testing. The door vignette now draws each 16x16 door half at native GUI size inside a pushed pose, then scales the pose itself to screen dimensions. Only one texture quad exists, so the animation no longer depends on ambiguous blit overload behavior.


## Acknowledged transition context

A fixed one-tick lead did not reliably ensure that the client had processed the door/window/breach context before Minecraft created its dimension transition screen.

The transition now uses an explicit handshake. Each crossing gets a unique integer token. The server sends `HouseTransitionContextPayload(kind, token)`; the client stores the kind and immediately replies with `HouseTransitionContextAckPayload(token)`. The server performs the dimension handoff only after the matching acknowledgment returns. A stale or mismatched token is ignored. If no acknowledgment arrives within 40 server ticks, the pending transition is cancelled rather than falling back to an incorrect animation.

This makes presentation selection causally ordered instead of timing-dependent.


## Persistent client transition context

The client transition kind is now token-scoped and persistent for the complete receiving-screen lifetime. Screen factories use a non-destructive peek instead of consuming the value. The context is cleared only when the transition screen's `removed()` lifecycle callback fires for the matching token.

Server-side transition preparation also logs the classified boundary kind and crossing coordinates. This makes future testing diagnostic rather than visual guesswork: if the server reports WINDOW while the client renders DOOR, classification is exonerated and the bug is isolated to client state/rendering.


## Physical obstruction transition language

Boundary transition effects should never visually identify themselves as portals.

The front door uses the same oak material as the authored exterior door and briefly fills the view as the player passes it. A window crossing uses only the surrounding white-terracotta sill/jamb because a player capable of crossing that opening has already removed the glass. A generic wall breach uses the authored white-terracotta wall material plus a narrow dark-oak structural edge, leaving only a tight dark gap while the dimension handoff completes.

Entering is intentionally slower and more constricted than leaving. The difference should be perceptible over repeated use without becoming an explicit supernatural announcement.


## Captured-frame transition foundation

Synthetic full-screen reconstructions of doors, windows, and breaches were rejected in playtesting because they visually detached from the player's camera and read as loading-screen artwork.

The current transition system captures the actual main framebuffer immediately when NeoForge constructs the receiving-level screen. That final gameplay frame is registered as a temporary dynamic texture and remains the visual background throughout the loading interval.

Physical obstruction is then composited over the real frozen view. The door, window frame, or broken-wall edges only need to explain why the player's vision is partially blocked; they no longer need to fabricate an entire scene. The dynamic capture texture is released when the screen closes.

This preserves camera orientation and environmental context up to the dimension handoff while keeping the underlying cross-dimension architecture unchanged.


## Motion cues over captured frames

A captured frame alone preserved continuity but still read as a stationary image being covered by UI geometry. Window and breach transitions now apply small whole-frame transforms to imply camera movement during the hidden handoff.

Window entry combines a slight forward zoom, downward world drift, lateral head movement, a rising sill, and a traveling jamb. Breach entry favors lateral camera displacement and one wall face crossing the center before the opposite edge closes. These transforms remain restrained because the framebuffer capture includes the HUD; large transforms would make the interface itself visibly move and reveal the trick.


## Live camera motion before handoff

Playtesting showed that transforming the captured framebuffer could not convincingly imply player movement because the HUD, held item, crosshair, and world all moved together as one 2D image.

Window and breach crossings now delay their transition ACK for a few hundred milliseconds while the source world remains live. During that interval, client camera yaw, pitch, roll, and FOV receive small additive offsets through NeoForge viewport events. The HUD remains fixed because this is real camera-space movement rather than framebuffer movement.

At the peak of that motion the client sends the transition acknowledgment. The server then changes dimensions, the receiving screen captures the final source frame, and only restrained architectural edges continue across the shorter frozen interval. When the screen closes, the camera offset decays back to neutral in the destination world.
