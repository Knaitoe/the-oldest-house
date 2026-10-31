# The workflow gate and the derived checks (0.4.199)

From the project review of October 31, 2026: five of the last twenty-five runs passed every job, most failures were in fixtures rather than gameplay, and every run cost about seventy minutes across seventeen jobs even when the push did not compile.

## The gate

A new first job, `quick`, runs in a few minutes: compile and jar, the branding check, the writing-font, texture and character-art checks, every asset generator in `--check` mode, the playtest-log unit tests, and one native server start (the three-charge shield check and the domestic projection check, which used to run inside the build job). Every other job `needs` it, so a push that does not compile or breaks an asset check stops there.

## Derived instead of hand-kept

- **Scene views.** `ArchitectureTests` writes `vignettes.txt`, the list of places that are vignettes (the Red Room's personal copy excepted). After the full suite, the client renders every export in `build/architecture-proof`; the workflow then requires a rendered page for each export and an export for every vignette. The client's hand-kept total (`112 + …`) and the per-scene `test -s` lines are gone, so adding a scene no longer means bumping a number in two places.
- **Variant counts.** Three blockstate counts whose generator already runs in `--check` mode are no longer asserted a second time by hand.
- Test counts were already derived from the declared `@GameTest` cases.

## Readiness, not delays

`gametest/SceneReady` waits, bounded and named, for a box's chunks and entity sections, for a recorded build or the labyrinth carve to finish, or for an entity under a saved id to load and be ready. A wait that runs out fails saying what was still missing. The Holy Rabbit's rabbit wait uses it first; new fixtures should prefer it to `runAfterDelay(n, …)` when what they wait for is the world rather than time.

## Also in this pass

`AGENTS.md` is a short file of standing rules, layout and commands; its 330 KB history of candidates and runs is `docs/HISTORY.md`. The README opens with how to play and build before its release notes.
