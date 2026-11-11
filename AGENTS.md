# This House Has Ghosts in It: notes for agents

**What this is.** A NeoForge 1.21.1 mod (mod id `the_oldest_house`, Java 21, Gradle 9.2.1). One impossible House deals the reader horror vignettes behind ordinary doors; each room is a small saved state machine with its own writing; a personal Witness account across the eligible stories leads to one of three endings. The design canon is `docs/DESIGN_DOCUMENT.md`; each release has a record at `docs/<NAME>_0_4_NNN.md`; the full log of candidates, runs and repairs that used to live in this file is `docs/HISTORY.md`.

**Where the numbers live.** Do not copy counts into prose that will go stale. Version: `gradle.properties` (`mod_version`). Protocol: the registrar version in `network/HouseNetwork`. Layout: `labyrinth/LabyrinthBuilder.VERSION`. The Witness pool and quota are derived from `WitnessAccount.Story` (75% rounded up, across two kinds); never freeze them to a number. Test counts are derived from the declared `@GameTest` cases in CI.

## Standing rules from the owner

- **Authorization (October 15, 2026):** "Yes, assume you have authorization in perpetuity." Routine development pushes and CI on this mod need no further asking. This does not authorize destructive operations, unrelated repositories, or a broader publication or merge scope.
- **No pull requests unless asked.** PR #10 is the draft that tracks `feature/vignette-environment-audit-0.4.125`; development pushes go to that branch.
- **Do not push while a CI run is in progress** on the branch: a push cancels it.
- **New builds only (October 31, 2026):** the mod is not publicly shared and needs no backward compatibility. Do not add save migrations, in-place upgrades or once-only repairs of older worlds; when you touch one that remains, remove it. A layout saved by an earlier version only gains the places it lacks (`LabyrinthBuilder`, the `labyrinth_places` carve record); nothing standing is carved or dressed again. Data-format tolerance (older SavedData, entity NBT, Witness records) is cheap and stays.
- **The hall drawers (November 2, 2026):** the House takes what a reader carries in from outside unless it is the House's own, House-made (custom data), eaten or drunk, or a torch, lantern or candle, and keeps it for them in the manor's front-hall chest of drawers (`house/HouseKeeping`, docs/HALL_DRAWERS_0_4_255.md). Rooms may assume readers carry nothing else; a test that carries an outside thing in through a crossing marks it House-made.
- **The Mother of Strays is not to be changed** ("Mother pvp is not something I've asked you to manipulate").
- **Witness** stays derived and hard: currently 72 sources / 54 required / two kinds / three endings. The cases pin these in one place, `gametest/WitnessPool`.
- **Literature travels with mechanics:** a change to a room changes its account, its Witness line, its House of Leaves fact and any keepsake line ("Ensure you're updating literature too").
- **House writing** never genders the reader and never names a colour or an age. "House" is blue everywhere; "Minotaur" is red in literature only.
- **Speech is never chat:** people speak through displays and the action bar; `sendSystemMessage` is for operators. A conversation that asks the reader something uses `Talk` (the conversation panel, 0.4.257): lines, then replies the reader picks; a reply is honoured once.
- "Start running with it. I'll edit as needed": build, then let the owner edit.
- Never put a model identifier in a commit message, PR text, code comment or any pushed artifact.

## Claims

A candidate is verified only when every job of the workflow passes on that exact commit. A local pass of a focused namespace is not verification: say pending or failed. Record what CI found and what changed in the commit that fixes it.

## Layout

- `labyrinth/`: the places (`LabyrinthPlace`), the dealer, the doors, the builders and every vignette. `house/`: the manor, calendar, opening, config and Witness. `journal/`, `opening/`, `network/`, `client/`, `command/`, `mixin/`, `gametest/`.
- How a room is made is one property on the place (0.4.226): `LabyrinthPlace.Made.WHOLE` (the default: built whole by its own builder) or `Made.DRESSED` (the older rooms the generic passes light and furnish). Every generic pass (polish, dressing, composition, the scene reviews, the playtest repairs, the vignette architecture, shells, exteriors) asks `place.authored()` and nothing else; never add a room to a pass by name to keep it out. A room built whole is exported in `ArchitectureTests` when the generic loop skips it. See `docs/AUTHORED_ROOMS_0_4_226.md`.
- How long words stay up is `labyrinth/Dialogue`'s to say (0.4.473, docs/DIALOGUE_PACE_0_4_473.md): a person's line, a reader's own words and the House's words on the action bar all take their time from it, scaled by the world's `readingTimePercent`. A scene never takes a line down on a timer of its own: it asks `LiteraryActor.saying()`, and uses `hush()` for a person who is cut off.
- A new vignette follows `docs/VIGNETTE_RELEASE_CHECKLIST.md`.
- Textures, models and blockstates are drawn by `tools/*.py`; CI runs each in `--check` mode. Never hand-edit a generated PNG.

## Commands

- Compile: `gradle compileJava --offline --no-daemon -q -Dorg.gradle.jvmargs=-Xmx768m`, from the repository root.
- Selected cases: `rm -rf run/world && gradle runGameTestServer --offline -PgameTestNamespaces=the_oldest_house_selected "-PgameTestMethods=Class#method,..." --no-daemon -Dorg.gradle.jvmargs=-Xmx1g`.
- A namespace: `printf 'view-distance=3\nsimulation-distance=3\n' > run/server.properties`, then `JAVA_TOOL_OPTIONS="$JAVA_TOOL_OPTIONS -Xmx6g" gradle runGameTestServer --offline -PgameTestNamespaces=the_oldest_house_<name> --no-daemon -Dorg.gradle.jvmargs=-Xmx512m`. The full suite is the default namespace list in `build.gradle` (about 75 minutes, 6 GiB).
- Client proofs: `ALSOFT_DRIVERS=null LIBGL_ALWAYS_SOFTWARE=1 xvfb-run -a gradle runClient --offline -PfontSmoke`, after the writing namespace has produced `build/font-smoke/serial-pages.json`.
- The Python tools need Pillow.

## Tests and CI

- `quick` gates the workflow: compile and jar, the asset and font checks, the playtest-log unit tests and one native server start. Every other job needs it.
- Counts are derived: declared `@GameTest` per namespace; the client renders every export in `build/architecture-proof`, and the workflow requires a page for each and an export for every vignette (`vignettes.txt`, written by `ArchitectureTests`).
- Fixed delays are the main source of flaky cases. Use `gametest/SceneReady` to wait for chunks, entity sections, a drained build or a named entity, bounded and named.
- Fixtures share one server: forget per-fixture static state in `@AfterBatch` hooks (`GroupCrossings.forget()` and the like), and build every hall a way on can lead into.
- A case's reader (`LiteraryTests.Fixture.player`) is weightless and the game does not move it: one that must fall is given weight and moved with `travel`. What a reader is told and what they hear reach a case down one pipe, and reading either (`NativeTestPlayers.messages`, `sounds`) empties it; a packet sent on one tick is readable on the next. A case that looks at something the game does a few ticks after a condition (a visit's dressing, a night's first tick) waits for that thing itself, not for the condition.
