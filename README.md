# The Oldest House

A NeoForge 1.21.1 horror-mod prototype built around one impossible house.

## Current prototype

Version `0.1.31` implements the first stable lifecycle and redesigned domestic shell:

- NeoForge 1.21.1 / Java 21 project setup.
- Persistent world-level state for The Oldest House using `SavedData`.
- A static 15 x 19 furnished version of The Oldest House with a fixed exterior.
- **Automatic settlement-based eligibility:** five successful overnight sleeps within the same 32-block home area make the world eligible for The Oldest House.
- Eligibility does **not** spawn The Oldest House immediately.
- The hidden spawn chance begins at **5%**.
- After each eligible night in which The Oldest House does not appear, the next night's chance randomly **rises by 2-5 percentage points** or **falls by 1-2 points**, with a floor of **5%** and ceiling of **100%**.
- Automatic placement searches for empty, reasonably flat ground near the established settlement and will not deliberately bulldoze player structures.
- The Oldest House now tracks a persistent perceived age once it has appeared.
- At the provisional test threshold of age **3**, an ordinary-looking door appears at the end of the rear hall.
- The usable domestic interior lives in The Oldest House dimension at matching coordinates, with nearby Overworld scenery mirrored outside its windows.
- The age-gated rear door opens directly into interior-only impossible architecture; there is no second teleport behind it.
- Operator-only development commands for forcing, aging, and inspecting The Oldest House state.
- The exterior is generated once and then left alone. Future impossible space belongs behind it, not in a morphing facade.
- No mixins.

The actual interior dimension of The Oldest House, modular graph, nightly interior growth, navigation anomalies, explorer notes, Mother of Lost Things, dog, and Minotaur are intentionally **not** faked into this first milestone. Their design is tracked in [docs/DESIGN.md](docs/DESIGN.md).

## Natural spawn lifecycle

1. Sleep through five nights while remaining within the same 32-block settlement area.
2. On the fifth counted morning, that settlement becomes the House anchor and the world becomes eligible.
3. The hidden appearance chance is initialized to 5%.
4. Nothing can appear on that same morning.
5. Starting the next morning, The Oldest House makes one hidden daily appearance roll using the current chance.
6. If The Oldest House does not appear, the chance for the next eligible morning randomly changes:
   - 50% chance to increase by 2-5 percentage points.
   - 50% chance to decrease by 1-2 percentage points.
   - never below 5%; never above 100%.
7. When a roll succeeds and a suitable empty site is found 32-64 blocks from the settlement anchor, The Oldest House is placed.
8. No toast, chat message, advancement, or horror sting announces this.

The asymmetric step sizes mean the probability tends to drift upward over time, but individual nights can still make the House less likely. It feels less like a countdown and more like something circling the settlement.

## Perceived House age

Once The Oldest House has appeared, each successful new morning advances its perceived age by one day. The value is stored in world data and is intended to drive future staged changes such as the first impossible door, deeper architecture, navigation anomalies, and other progression.

For testing, `/oldesthouse advance` fast-forwards this value without changing the world's actual time.

The first impossible-door threshold is currently **3 days** only for rapid testing. It is a named constant and is not a final pacing decision.

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
| `/oldesthouse spawn` | Force-spawn the static test house about 24 blocks in front of the player. |
| `/oldesthouse status` | Show persistent The Oldest House and settlement state, including hidden spawn chance. |
| `/oldesthouse eligible` | Debug override: mark the current location eligible immediately and reset chance to 5%. |
| `/oldesthouse ineligible` | Clear eligibility without removing an already spawned house. |
| `/oldesthouse age <days>` | Set The Oldest House perceived age to an exact value. |
| `/oldesthouse advance` | Advance The Oldest House perceived age by one day. |
| `/oldesthouse advance <days>` | Fast-forward The Oldest House perceived age by the supplied number of days. |
| `/oldesthouse visit` | Increment the stored visit count. |
| `/oldesthouse reset` | Reset persistent The Oldest House state. This does **not** erase blocks already placed in the world. |

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

GitHub Actions builds and uploads the compiled mod jar on every push and pull request.

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
