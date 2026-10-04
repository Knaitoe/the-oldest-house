# Vignette release checklist

Apply this checklist whenever a vignette is added or its progression changes. Ending credit is part of the vignette implementation and release review.

- Identify the actual personal resolution. Award it to the explorer who completes or personally examines the authored ending. Arrival, ordinary loot, a borrowed artifact and shared room flags alone confer no credit. Different choices in the same vignette still count as one source.
- Register the fully playable source in `WitnessAccount.Story`, including its stable ID, story kind, account text and any supported aftermath. Verify completion and multiplayer credit hooks. Draft sites, scenery-only rooms and unfinished resolution paths do not enter the pool; document a deliberate exclusion.
- Check the resulting quota: **ceil(0.75 × eligible sources)**, still requiring at least two kinds. `WitnessAccount.REQUIRED` derives this from the shipped story enum. Do not substitute a fixed threshold or count individual visits as sources.
- Verify the threshold boundary, repeated-scene credit, new-source credit, saved accounts and the actual final passage. Update the pool assertion and account fixtures in `WitnessTests` when new sources change them. Operator preparation and status commands must follow the same quota. Preserve completed endings and releases already underway.
- Update the current pool and requirement in the design document, README, release notes and `AGENTS.md`. Keep older release descriptions historical. Confirm the game still exposes the intended ending options rather than equating story sources with additional endings.
- Run the required server GameTests and verify the playable package before publishing the release.

## Current candidate: 0.4.37

The 0.4.37 review repair adds no new source and retains layout 32 / protocol 32. Require all 317 declared native tests, sixteen focused literary and seven hotel cases plus all existing native client/package proofs. Patch validation is pending. The following 0.4.36 evidence is historical and belongs to its exact stated source.

Forty-three eligible sources require **thirty-three distinct personal resolutions** across at least two kinds. The game has three ending options. The [0.4.36 native run](https://github.com/Knaitoe/the-oldest-house/actions/runs/37168605798), source `3c324aef78aea325f40961171958208226ea3faf`, passed all 312 required gameplay tests, the thirteen-case literary and six-case hotel suites, and complete writing/texture/package/client proofs, including fifty-three architecture views. Older recorded evidence and completed endings remain saved.

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
| Goatman door vigil | Survival |
| Caver’s return above the rope | Survival |
| Zampano’s final survey page | Understanding |
| Whale’s personally decoded undated letter | Connection |
| Well wait and return above the cover | Memory |
| Plain’s native spyglass photograph | Memory |
| Hospital's personal dawn chart | Understanding |
| Holloway's personal three-arena escape and service latch | Survival |
| Mother's peaceful resolution | Release |
| The séance's personally examined album | Connection |
| The nursery's four personally discovered installments and final reading | Understanding |
| The hotel's personally completed and read closing account | Understanding |
| Hill House's cold nursery and returned cellar knocks | Understanding |
| Miniatures of personally visited rooms | Memory |
| Masque's seven rooms and stopped clock | Release |
| Usher's personally chosen lid and inspected consequence | Understanding |
| Winchester's physical upper route and ledger | Understanding |
| The child's disappearing exits and actual crawl out | Survival |
| Crimson hall's three played recordings and original account | Connection |
| The Lady's route, refuge and lake-room aftermath | Survival |
| The elk lot's personal escape and named clipping | Survival |
| The fan's repaired reach, image and later tooth | Understanding |
| Mapping's crawlspace, belongings choice and brother's consequence | Connection |
| Holy Rabbit's actual meals, mornings and dragging trail | Survival |
| The confession's own signed, sealed and read journal | Understanding |
| The elk hides' still vigil and service escape | Survival |
| Costume night's observed movement and grass refuge | Survival |
| Movie night's actual canoe route through firework exposures | Survival |
| Winter lake's broken ice, real submersion and return | Survival |
| Camp Blood's personal survival and service-route escape | Survival |
| Devil's Rock's eight discovered pages and final reading | Memory |
| The wheel's actual numbered route and front-door return | Memory |
| The ghost set's performed marks and personal transcript | Connection |
| The cabin's explicit offering/refusal and read choice record | Release |
| The copied family's visits, actual heirloom and personal account | Memory |

The forty-fourth source still requires thirty-three. Existing recorded evidence remains saved as the quota rises. Explorers who have not committed to release must satisfy the current requirement. Completing every source, or resolving the Mother specifically, is not required.

Every new vignette includes authored structural generation and furnished interiors, appropriate custom native meshes/textures for props, items and cast, original integrated writing, personally earned outcomes, multiplayer/save preservation and a reviewed Witness resolution. Require the full native gameplay suite and client/package proofs, including generated architecture views and custom-asset rendering, before delivery.

Karen’s room is a native navigation/respawn anchor, excluded from Witness.

The old-man cabin is recurring guidance and a private first-recorded-bed snapshot, excluded from Witness.

Version 0.4.21 serial notes, poems and furniture are scenery, excluded from the playable Witness pool. They confer no resolution credit.

The blind stretch and hotel grounds are navigation/scenery; they are excluded as separate Witness sources. Only the personally completed and read hotel account supplies the hotel resolution.
