# Design outline for The Oldest House

> **Historical implementation outline.** The current creative canon is [DESIGN_DOCUMENT.md](DESIGN_DOCUMENT.md), and the current opening contract is [OPENING.md](OPENING.md). Sections below remain useful as implementation history, but the retired natural-appearance roll and pre-Hillary dog concept must not be reintroduced.

This file records implementation decisions so implementation history does not disappear.

## Current implementation update

Version 0.4.17 adds a longer domestic labyrinth approach and trapped fragments of household rooms. Its current pacing, upgrade behavior and unchanged seven-of-nine Witness requirement are documented in [DOMESTIC_LABYRINTH_0_4_17.md](DOMESTIC_LABYRINTH_0_4_17.md).

Version 0.4.7 supersedes the older Hillary refusal and early House-spawn behavior below. The opening is letter/photo, Hillary at home, then House on three mornings. Hillary can accompany the player into the real manor and labyrinth and retrace return doors on a compass request. Gray geometry, sensory door leaks, sitting and recent-room suppression are detailed in [MAZES_AND_COMPANIONS.md](MAZES_AND_COMPANIONS.md).

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

The old five-settlement-night eligibility and hidden 5%-to-100% appearance roll are retired. Automatic appearance now belongs exclusively to the per-player opening sequence:

1. A player establishes an Overworld bed respawn point, completes two sleeps, and has spent at least three in-game days in the world.
2. On the next handled morning, the Navidsons move in. `HouseSpawnManager.ensureSpawnedNear` uses the existing terrain-aware safe-site search to place the manor 32-64 blocks from the player's home if it does not already exist.
3. Navidson photographs the player's home from the actual geographic side implied by the manor's porch, then leaves the letter and photograph at the player's familiar doorstep.
4. On the following handled morning, Hillary appears and leads the player over ordinary terrain toward the manor. She waits if they fall behind and refuses to cross the manor's front door.
5. There is no anomalous doorway in the player's own home.

No separate probability roll can make the House appear independently of that sequence.

## Perceived age

The House can exist in the Overworld before it has begun to change.

Perceived age advances once per successful Minecraft morning **only after at least one player has actually entered the manor**. A player can therefore ignore the letter or Hillary indefinitely without returning to find that the first impossible threshold revealed itself off-screen.

Perceived age is persisted separately from ordinary world time so development commands can fast-forward or set it directly. `/oldesthouse advance [days]` exists specifically to exercise staged changes.

After the first visit the House changes on its mornings, in order (`HouseProgression`, tuned in `HouseConfig`): the rugs trade colours the morning after someone first sleeps in the manor; from House morning 3 the room between rooms has a 50/75/100% daily roll to be armed behind the study door, which then starts leading through it once nobody is looking; on quiet mornings, subtle changes (`HouseShifts`: paintings, a deeper hall, door hinges, chests remembering, candles, echoes, chairs, notes, a guest bed, a window lit from outside); once someone has walked through the room and two subtle changes have happened, the hallway gets a heavily weighted daily roll ahead of them. At most one change per morning, and never anything a player can see happen.

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

The dog is Hillary, the Navidsons' gray husky.

She first appears at the player's doorstep during the opening and leads the player to her owners' manor, stopping outside the front door. The first bone from her recipient tames her.

Once the labyrinth exists, Hillary becomes a genuine companion and navigation ally: she can seek vignette doors from their yielded objects, wait when the player falls behind, refuse especially dangerous destinations, react to Growls and topology changes, and eventually become entangled with the Mother of Strays.

Built so far: seeking, from outside, since she never crosses into the House. Given a vignette's yielded object she takes the scent and scratches at the manor's front door; the next dealing that can have a vignette door does (unfound one-shots first), and that door leaks her bark instead of the heartbeat. It tilts the dealer's odds as far as they go without summoning a place: the player still has to find the door.

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

The rear impossible corridor is explicitly excluded from mirroring. When the hallway's roll succeeds (after someone has walked through the room between rooms and two subtle changes have happened), the interior partition at the end of the hall gains its door and a sealed 56-block corridor is constructed directly behind it in the House dimension. Its far wall carries a door into the labyrinth, which is stacked above the manor in the same dimension and entered by a same-tick shift, not a dimension change. Because the Overworld structure is fixed, rear-facing and rear-side windows are intentionally omitted from the authored facade so ordinary windows never gain a sightline to interior-only geometry.

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

**Superseded.** The handshake has since been removed. The camera motion it waited for is gone, and the ordering it guaranteed already holds without it: the context payload and the dimension switch that follows travel on one ordered connection and are handled on the client's main thread in order. The token remains, scoping the client's held context to one crossing.

On the server a pending transition is now an explicit state machine (`HouseTransitionEvents.Phase`): `PREPARED` (context sent; waiting for the next tick, and for up to 20 ticks for the destination level to exist) → `SYNCING` (mirror reconciliation, proxy evacuation, entity seeding) → `MOVING` (before hook, teleport, after hook) → `COMPLETE`, or `FAILED`. A transition is forgotten only on completion or failure. It fails, leaving the player where they were with none of its hooks run and telling the client (`HouseTransitionCancelPayload`) to drop its context, when the destination never appears, when the player has meanwhile left the source level another way (death, a command), when they are dead or gone, when the teleport does not arrive (another mod cancelled it), or when anything before arrival throws. A throw after arrival is logged and the crossing still counts.

The room between rooms decides by planes, not distances (`HouseBetweenRoom.Crossing`). Along the door's normal (x) it keeps the room's inside, between the two door blocks' room-side faces, and an exit plane on each side far enough out that a player put back in the manor stands clear of the real door's panel. Each tick the segment from last tick's x to this tick's is tested against the planes, so nothing fast can skip one. Having crossed into the inside, crossing either exit plane returns the player on that side (the other side from the one they came in by is a traversal). Not having been in, they are returned on their own side once past its exit plane and a full 0.75 block further out along the normal than the closest they came, or more than six blocks from the door, or off the floor. Strafing along the wall never counts, and in a doorway nothing happens at all.

## Several people at once

The room between rooms is shared: one pocket, one pair of copied doors. Leaving it shows the leaver its doors shut (the real partition they land in front of is shut), but with anyone else still in the pocket that is done with a per-player block update, not by changing the doors. The last one out shuts them for real. Entering always re-sends the copies' true door states to the one entering.

Hide-and-clap is one room with one game, reserved by the first player to enter. The entry shuts and other players cannot enter during that turn. Its claps and girl are private to its owner. The minute starts when that player equips the blindfold, and the reservation, timer and progress survive reconnects and world reloads. Failure drops the player's items in the room and returns them to the manor outside the impossible hallway; the Mother may collect abandoned keepsakes when those drops expire.

Every payload goes through `HousePackets`, which skips any connection that has not negotiated the mod's channel (fake players, GameTest mock players).

Multiplayer GameTests use GameTest mock players: real ServerPlayers on a connection that goes nowhere. The server ticks them as entities but never fires `PlayerTickEvent` for them, so tests fire the mod's player tick themselves. The House's dimensions do not exist on the test server. Crossings in these tests therefore go to the Nether, and the room between rooms is built at an isolated spot in the test level with its own `HouseSavedData`.

## Outdated layouts

`HouseSavedData` records the layout version the House was generated with. Every mechanism starts from `houseOrigin()` and stands down when it is null, so that method returns null for a House whose layout differs from the build's (`isOutdated()`). The mechanisms covered are crossings, the proxy mirror and entity mirror, the mornings, the room between rooms, the hallway, the labyrinth and the sound bridge. An outdated House goes quiet as a whole rather than acting on blocks that no longer mean what the code assumes. It is only an ordinary building. `housePosition()` still reports where it stands, for status and reset.

Players found in any of the mod's dimensions while it is outdated are sent through the transition machinery to the Overworld spawn: nothing in there can be trusted to line up with the Overworld any more. Operators are told once at login. `/oldesthouse reset` followed by a respawn is the way back; a migration can replace that when the layouts stop changing.


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


## Prefer real geometry over GUI block textures

The block-texture overlays used for window and breach transitions were rejected after playtesting. Scaling a single 16x16 block texture across a large GUI rectangle destroys the spatial cues supplied by Minecraft's normal world renderer and looks visually broken.

Window and breach transitions now rely on the real source-world geometry during the live camera phase. The final framebuffer already contains the correctly lit and perspective-rendered sill, wall, trim, held item, and surroundings. During the receiving-screen interval, no synthetic block surfaces are painted over that image; only low-opacity edge shadows provide limited occlusion while the destination becomes ready.

The frozen interval is correspondingly shorter.


## Front door closes behind entry

The front-door transition should resolve as an ordinary physical action rather than ending with an open door frozen in whichever state the player left it.

After a successful DOOR-classified entry into the House interior, the server waits eight ticks and closes the actual front oak door in the interior dimension using vanilla door behavior. The closed state is then copied to the matching Overworld shell. This preserves vanilla sound/game-event semantics and keeps both representations synchronized. Non-door crossings never trigger this behavior.


## Expanded domestic architecture

The trustworthy domestic structure now has three stable levels before impossible architecture begins.

The basement is excavated only within the fixed footprint and sealed in stone brick before its interior is cleared. It contains an ordinary workshop/storage layout and a stair into the rear-right ground-floor utility room.

The ground floor retains the established living/kitchen front half and central rear hall. The former ground-floor bedroom has become a study; the opposite rear room is utility/storage and contains the basement stair.

A full second story sits at y+6. A two-wide central staircase rises into a landing between two front bedrooms; a larger rear bedroom occupies the back half. Bedroom windows are restored on front and side elevations. No rear-facing bedroom window is used, because the eventual impossible hallway occupies the rear-center sightline below and must remain invisible from ordinary domestic windows.

The domestic boundary detector spans basement floor through the upper-story ceiling zone but stops below the roof. Breaking into the basement or upper floor therefore enters the House dimension as a breach, while standing on the roof remains ordinary Overworld space.

### Domestic chest loot

Authored chests use mod loot tables under `data/the_oldest_house/loot_table/chests/`:

- `bedroom`: papers, books, candles, food and occasional personal/navigation items;
- `study`: writing/map supplies with occasional compass, spyglass or name tag;
- `basement`: practical workshop fuel, iron, redstone, torches and occasional utility items.

These tables are deliberately useful but mundane. Deeper impossible-space rewards remain a separate progression system.

Because general block-entity NBT synchronization is not implemented yet, the builder deterministically reapplies the authored loot tables to matching chest block entities when the House interior dimension is initialized.


## Architectural language: vanilla-build hierarchy

The static exterior should resemble a deliberately authored Minecraft home before it reads as anomalous. Reference patterns from established survival/Tudor/farmhouse builds inform the implementation: strong timber bays, inset/recessed glazing, a visible masonry base, one-block eaves, a filled gable, separate roof-edge trim, and layered roof masses.

The main roof uses deepslate tile stair courses with a dark-oak bargeboard at the front and rear. The gable itself is plaster-filled and timber-framed instead of being an uninterrupted triangle. The entrance porch has its own smaller pitched gable, producing a clear primary roof / secondary roof hierarchy.

The ground-to-upper stair is intentionally narrow and open-railed. Domestic circulation should not dominate the room as a solid wooden sculpture.

## Terrain-aware siting and porch adaptation

A successful appearance roll now samples forty potential sites and selects the lowest-scoring valid candidate. Candidate scoring strongly penalizes water inside the house footprint and uneven terrain, while allowing nearby shoreline conditions that the exterior can plausibly accommodate.

The builder captures a front-approach terrain profile before clearing blocks. Porch supports extend downward until they meet non-replaceable terrain. On land, the stone-brick entrance stairs gain as many descending courses as needed (within a bounded range) to meet the sampled approach height. If enough front samples are water, the same entrance generates a short three-wide spruce landing/dock with stone-brick supports rather than submerged stairs.

This adaptation affects only the ordinary exterior relationship to terrain. The House footprint, measurements, and domestic/interior topology remain authored and stable.


### Front approach stair orientation

The adaptive entrance run descends away from the north-facing facade. Stone-brick stair blocks therefore face south, toward the building, so the run rises naturally as the player approaches the porch.


## Doorway clearance invariant

Authored domestic doorways are circulation constraints, not decorative suggestions. After all structural, furniture and lighting passes complete, the builder audits protected approach volumes on both sides of every doorway. Any block with a non-empty collision shape inside those approach volumes is removed; zero-collision decoration such as carpet is preserved.

Stairs and railings should be laid out to respect these zones in the first place. The clearance audit is a final invariant check so later architectural additions cannot silently turn a usable doorway into a decorative slot.


## Stage-aware shared mirror

The boundary between domestic and impossible architecture is exact: the threshold partition at relative z=15 belongs to the stable domestic house and must exist consistently in both the Overworld proxy and the House dimension. Impossible-only geometry begins at relative z=16.

Stage application is not a player block event, so it cannot rely on incremental event-driven mirror queues. Whenever a stage changes shared domestic architecture, the House dimension explicitly synchronizes the shared domestic footprint back into the Overworld. Positions classified as impossible/interior-only are skipped, so the physical hallway remains absent from the Overworld while its doorway and surrounding partition remain visually consistent.


## Mirror v2: authoritative domestic persistence

After first initialization, the House dimension is authoritative for the stable domestic interior. The Overworld representation is a proxy used to preserve exterior sightlines and threshold continuity.

Mirroring now copies compatible block-entity custom/component data as well as BlockState. Block-entity identity/coordinates are never copied; target entities retain their own metadata and receive only serialized custom/component state. This supports vanilla containers, furnaces, lecterns and compatible modded block entities without treating the two dimensions as independent inventories.

Explicit Overworld block interactions are applied to the House dimension first. A periodic active-area reconciliation then pushes the authoritative domestic footprint from the House dimension back to the Overworld. The pass runs only while a player is within 96 blocks of the structure in either dimension, avoiding permanent chunk churn when the House is inactive.

This periodic authority pass exists specifically for state changes that event-only mirroring misses: inventory menu operations, furnace progress, environmental/random ticks, redstone changes and similar server-side mutations.

The impossible hallway and future impossible graph cells remain outside the shared domestic set and are never written into the Overworld proxy.


## Overworld sightline into impossible space

The physical impossible hallway must never be instantiated in the Overworld: doing so would make it discoverable from behind the fixed exterior and violate the trustworthy-exterior rule.

After the first impossible threshold is revealed, clients receive the House origin/reveal state. When an Overworld camera is on the domestic/front side of the threshold and has a clear line of sight to the open threshold door, a client-only world-space proxy renders the hallway's inward-facing floor, wall, ceiling, carpet and depth-lighting surfaces. The existing Overworld depth buffer provides natural occlusion through the real house geometry.

The proxy has no blocks, collision, pathfinding, light propagation, saved data or rear-side visibility. It exists only to preserve the visual sightline through the ordinary house. The moment the player crosses the threshold, the real House-dimension corridor replaces the proxy.


## Upper-floor landing

The upper stair opening occupies relative x=6, z=2..6. No bedroom doorway may open directly onto those cells.

The two front-bedroom doors are paired on the solid landing at relative z=8, beyond the stair opening. A spruce-fence guardrail runs along the solid x=7 edge of the stairwell while x=8 remains the clear circulation lane. Floor runners stop at the void and resume only on solid landing blocks.


## Domestic finish pass

Domestic rooms should communicate different inhabitants/history without introducing custom assets. Bedroom layouts therefore use distinct rug silhouettes and palettes, wall banners as textile art, plants, book storage and hobby/work blocks rather than repeating the same bed/chest/bookshelf/carpet grammar.

The principal stair is a single visual mass. Railings are reserved for actual exposed landings rather than following every stair tread as a second parallel diagonal. The basement descent uses an L-turn at its lower end so circulation opens into usable basement floor.

Water-site docks are treated as complete small structures: perimeter fencing, a deliberate ladder opening, supported posts and a ladder reaching the water.

## Construction-debris suppression

Impossible-space carving is authored replacement, not player mining. Corridor construction uses update flags that suppress block drops and neighbor-shape cascades, then removes any item/falling-block entities inside the just-built corridor as a defensive cleanup. This happens only at construction time before the space is ordinarily accessible.

## Threshold sightline portal

Rendering the proxy at the corridor's true Overworld coordinates allowed ordinary terrain behind the fixed exterior to win the depth test. The external sightline is therefore represented at the threshold aperture itself.

While the player remains in the Overworld/front side and the revealed threshold door is open, a client-only perspective tunnel is drawn immediately behind that real doorway. The actual wall and door still occlude it through normal depth testing. The illusion contains converging wall/floor/ceiling planes, the red runner, a dark terminal plane and diminishing warm depth markers. It has no collision or saved world state. Crossing into the House dimension replaces it with the real physical corridor.


## Domestic architecture rewrite

The static House is no longer generated as a rectangular shell with a full-width front gable.

The main roof ridge runs east-west so the primary north facade presents a continuous eave and roof plane. Its profile uses a two-block horizontal run per rise, keeping the roof substantial without making it nearly as tall as the domestic walls. A smaller projecting upper-right gable, the left ground-floor bay, the offset chimney and the porch create asymmetrical massing.

The left living-room bay and right upper-bedroom gable project one block beyond the nominal front wall. Their playable interior remains on relative z=0, inside the domestic boundary; the structural wall sits at z=-1. Shared domestic synchronization therefore includes that single projected front wall layer.

The second floor is a single structural layer at relative y=6 and also serves as the ground-floor ceiling. No independent ceiling slab is generated beneath it.

Domestic furnishing follows focal points and room roles. The living room faces the hearth. Bedrooms use different functional props, furniture arrangements and actual paintings. Rugs anchor furniture groups rather than acting as arbitrary colored rectangles.

Windows and door approaches are invariants. Window panes are restored after structural/furnishing passes, and doorway clearance is audited after all furniture is placed.
