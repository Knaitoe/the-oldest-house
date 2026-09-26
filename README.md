# The Oldest House

A NeoForge 1.21.1 horror-mod prototype built around one impossible house.

## Current prototype

Version `0.1.0` deliberately implements only the stable foundation:

- NeoForge 1.21.1 / Java 21 project setup.
- Persistent world-level House state using `SavedData`.
- A static 13 x 17 test-house exterior/interior shell built from vanilla blocks.
- Operator-only development commands for forcing and inspecting House state.
- The exterior is generated once and then left alone. Future impossible space belongs behind it, not in a morphing facade.
- No mixins.

The actual House dimension, modular graph, nightly growth, navigation anomalies, explorer notes, Mother of Lost Things, dog, and Minotaur are intentionally **not** faked into this first milestone. Their design is tracked in [docs/DESIGN.md](docs/DESIGN.md).

## Development commands

Commands require permission level 2.

| Command | Purpose |
| --- | --- |
| `/oldesthouse spawn` | Force-spawn the static test house about 24 blocks in front of the player. |
| `/oldesthouse status` | Show persistent House state. |
| `/oldesthouse eligible` | Mark the current world/player location as eligible and save the anchor. |
| `/oldesthouse ineligible` | Clear eligibility without removing an already spawned house. |
| `/oldesthouse age <days>` | Set the stored House age for later growth testing. |
| `/oldesthouse visit` | Increment the stored visit count. |
| `/oldesthouse reset` | Reset persistent House state. This does **not** erase blocks already placed in the world. |

### Important

`/oldesthouse spawn` is a development command. It clears the 13 x 17 x 7 build volume before placing the prototype, so do not aim it at anything you care about. Minecraft players famously learn this lesson immediately after aiming test commands at something they care about.

## Build

Requirements:

- JDK 21
- Gradle 9.2.1 or a generated Gradle wrapper

Build with:

```bash
gradle build
```

GitHub Actions installs Gradle 9.2.1 and runs the same build on every push and pull request.

To generate a local Gradle wrapper once Gradle is installed:

```bash
gradle wrapper --gradle-version 9.2.1
```

Then future builds can use `./gradlew build` (or `gradlew.bat build` on Windows).

## Target

- Minecraft 1.21.1
- NeoForge 21.1.251
- Java 21
- Mod ID: `the_oldest_house`
