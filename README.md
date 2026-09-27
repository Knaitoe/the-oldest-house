# The Oldest House

A NeoForge 1.21.1 horror-mod prototype built around one impossible house.

## Current prototype

Version `0.1.17` implements the first stable lifecycle and redesigned domestic shell:

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
