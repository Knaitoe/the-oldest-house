# The Oldest House

A NeoForge 1.21.1 horror-mod prototype built around one impossible house.

## Current prototype

Version `0.1.3` implements the first stable lifecycle and redesigned domestic shell:

- NeoForge 1.21.1 / Java 21 project setup.
- Persistent world-level House state using `SavedData`.
- A static 15 x 19 furnished House with a fixed exterior.
- **Automatic settlement-based eligibility:** five successful overnight sleeps within the same 32-block home area make the world eligible for the House.
- Eligibility does **not** spawn the House immediately.
- The hidden spawn chance begins at **5%**.
- After each eligible night in which the House does not appear, the next night's chance randomly **rises by 2-5 percentage points** or **falls by 1-2 points**, with a floor of **5%** and ceiling of **100%**.
- Automatic placement searches for empty, reasonably flat ground near the established settlement and will not deliberately bulldoze player structures.
- Operator-only development commands for forcing and inspecting House state.
- The exterior is generated once and then left alone. Future impossible space belongs behind it, not in a morphing facade.
- No mixins.

The actual House dimension, modular graph, nightly interior growth, navigation anomalies, explorer notes, Mother of Lost Things, dog, and Minotaur are intentionally **not** faked into this first milestone. Their design is tracked in [docs/DESIGN.md](docs/DESIGN.md).

## Natural spawn lifecycle

1. Sleep through five nights while remaining within the same 32-block settlement area.
2. On the fifth counted morning, that settlement becomes the House anchor and the world becomes eligible.
3. The hidden appearance chance is initialized to 5%.
4. Nothing can appear on that same morning.
5. Starting the next morning, the House makes one hidden daily appearance roll using the current chance.
6. If the House does not appear, the chance for the next eligible morning randomly changes:
   - 50% chance to increase by 2-5 percentage points.
   - 50% chance to decrease by 1-2 percentage points.
   - never below 5%; never above 100%.
7. When a roll succeeds and a suitable empty site is found 32-64 blocks from the settlement anchor, the static House is placed.
8. No toast, chat message, advancement, or horror sting announces this.

The asymmetric step sizes mean the probability tends to drift upward over time, but individual nights can still make the House less likely. It feels less like a countdown and more like something circling the settlement.

## Development commands

Commands require permission level 2.

| Command | Purpose |
| --- | --- |
| `/oldesthouse spawn` | Force-spawn the static test house about 24 blocks in front of the player. |
| `/oldesthouse status` | Show persistent House and settlement state, including hidden spawn chance. |
| `/oldesthouse eligible` | Debug override: mark the current location eligible immediately and reset chance to 5%. |
| `/oldesthouse ineligible` | Clear eligibility without removing an already spawned house. |
| `/oldesthouse age <days>` | Set the stored House age for later growth testing. |
| `/oldesthouse visit` | Increment the stored visit count. |
| `/oldesthouse reset` | Reset persistent House state. This does **not** erase blocks already placed in the world. |

### Important

`/oldesthouse spawn` is a development command. It clears the House build volume before placing the prototype, so do not aim it at anything you care about.

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
