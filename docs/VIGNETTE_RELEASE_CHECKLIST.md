# Vignette release checklist

Apply this checklist whenever a vignette is added or its progression changes. Ending credit is part of the vignette implementation and release review.

- Identify the actual personal resolution. Award it to the explorer who completes or personally examines the authored ending. Arrival, ordinary loot, a borrowed artifact and shared room flags alone confer no credit. Different choices in the same vignette still count as one source.
- Register the fully playable source in `WitnessAccount.Story`, including its stable ID, story kind, account text and any supported aftermath. Verify completion and multiplayer credit hooks. Draft sites, scenery-only rooms and unfinished resolution paths do not enter the pool; document a deliberate exclusion.
- Check the resulting quota: **ceil(0.75 × eligible sources)**, still requiring at least two kinds. `WitnessAccount.REQUIRED` derives this from the shipped story enum. Do not substitute a fixed threshold or count individual visits as sources.
- Verify the threshold boundary, repeated-scene credit, new-source credit, saved accounts and the actual final passage. Update the pool assertion and account fixtures in `WitnessTests` when new sources change them. Operator preparation and status commands must follow the same quota. Preserve completed endings and releases already underway.
- Update the current pool and requirement in the design document, README, release notes and `AGENTS.md`. Keep older release descriptions historical. Confirm the game still exposes the intended ending options rather than equating story sources with additional endings.
- Run the required server GameTests and verify the playable package before publishing the release.

## Current baseline: 0.4.17

Nine eligible sources require **seven distinct personal resolutions** across at least two kinds. The game has three ending options.

| Source | Kind |
| --- | --- |
| Tell-Tale Heart floorboards | Understanding |
| Hide-and-clap | Survival |
| Mr. Harrigan | Connection |
| Model home | Survival |
| Drowned Town church roof | Understanding |
| Preserved cave | Understanding |
| Shallows | Memory |
| Phone in the Canoe | Memory |
| Mother's peaceful resolution | Release |

The next tenth source raises the quota to eight; twelve sources require nine. Existing recorded evidence remains saved as the quota rises. Explorers who have not committed to release must satisfy the current requirement. Completing every source, or resolving the Mother specifically, is not required.
