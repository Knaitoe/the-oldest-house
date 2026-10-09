# Farm, well and encounter pacing — 0.4.62

The October 9 playtest found disconnected farm rails, loose livestock, a well that dominated an otherwise empty clearing, an abrupt blackout and a wooden initials block hidden behind the ladder. This pass repairs that existing scene and modestly increases fresh story and hazard offers. It retains the verified 0.4.61 boat, Stacy, slasher, source writing and gameplay repairs.

## Encounter balance

These percentages apply to eligible fresh arrivals, not every individual door. A fresh arrival offers at most one story, anomaly or physical hazard; remembered routes remain exact. Story offers have priority, so hazard percentages apply only when no story or anomaly is offered.

| Default | Previous | New |
| --- | --- | --- |
| Story, depths 6–9 | 22% | 28% |
| Story, depth 10+ | 38% | 44% |
| Added story chance per eligible dry discovery | 12 points | 12 points |
| Eligible dry discoveries before guaranteed offer | 6 | 5 |
| Physical hazard, depths 6–9 | 24% | 28% |
| Physical hazard, depths 10–15 | 36% | 42% |
| Physical hazard, depth 16+ | 48% | 54% |
| Strange anomaly, depths 8 / 12 / 16 / 24 | 2 / 3 / 4 / 5% | 3 / 4 / 5 / 6% |

Story and physical-hazard spacing stays at two visits. Anomalies still require depth eight and three visits between them; larger folds and loops retain their gates. Scent, pet rescue and earned calm routes remain authoritative. The complete old default preset upgrades once; a partially customized server preset stays exact. This aims to shorten uneventful exploration, without claiming a measured limit in minutes. Water-hallway mechanics are unchanged.

## Farm composition

The main track bends toward the barn and its feed aisle. A small timber farmhouse/tool shed and a fenced irrigated crop field sit opposite it. The low stone well is off that track, with a short gravel spur, rooted timber uprights, a pitched canopy, a winding spindle, chain and bucket. Connected rails cover both sides of the farm.

The barn has two closed native livestock pens around its central aisle. Hay bedding lies below the animals' feet rather than forming a jump step beside the rails. The original two cows, two sheep and three chickens are penned; saved animals retain their native UUID, health and state. Opening or breaking a gate remains an actual player action.

One shared barrel holds six bread, four baked potatoes, two cooked beef, two cod and three bones. One actual wolf and one cat can be tamed through the existing native bone/fish or House one-meat interactions and retain normal companion ownership/orders. Food, livestock and pets are finite: a peer, restart, death or depleted cache cannot replenish them. Existing edited food containers are not overwritten.

## Well descent and closing cover

The one-block ladder shaft remains physically climbable. The stone initials now face the ladder at `(0, -10, -22)`, above the bottom-standing player's head, and are visible without the rungs covering them. Their full cube uses the exact surrounding vanilla mossy-cobblestone sprite, with small native chisel-stroke faces for `K. G. / D. G.`; no wood panel or stretched photographic material remains. The original reward barrel and writing source remain in place. Saved personal initials recognition stays valid.

An actual native block entity saves the shared lid position. Five planks, under-straps and two iron hinges rotate around its north edge. An articulated anonymous figure stands on the stone rim, leans over the mouth and lowers its hands with the cover. The single lid closes over 160 occupied ticks (eight seconds), regardless of player count. New collision cannot seal through a living body, including a spectator's native camera body. Opening takes forty occupied ticks from fully closed.

The player's field of view narrows smoothly with depth, to 77% at the bottom; restrained peripheral shading also increases on descent and reverses on ascent. Mouse/look control stays free. A quiet original mono inhale/exhale loop follows that reader locally and stops on departure, death, a world/profile change or an expired scene lease. Its subtitle reads “Your breathing”. Peers outside the shaft do not hear it.

The personal blackout starts after sixty occupied ticks and reaches opaque black at 220 ticks. It therefore leaves time to look up and see the closing hands. The established deliberately dark wait remains opaque after that descent; its duration and personal completion condition remain 1,200 occupied ticks followed by an actual climb above the mouth. The scene grants no new Witness source.

## Multiplayer and saved worlds

There is one physical cover and one visual figure. Each living reader owns their wait, darkness and return evidence; spectators confer none. An unfinished reader cannot reset or inherit another's wait. A completed reader reaching the mouth makes the shared cover reopen so they can leave, while an unfinished peer retains their own dark vigil. No lid or personal wait time accrues without a living participant present; block-entity reload preserves the native occupied position.

The farm upgrade uses a separate per-origin `farmstead_0462` checkpoint. It waits for all actual block chunks/entity sections and nearby cameras, including spectators. Existing livestock move into the pens only while unobserved. Each bounded construction slice checks the actual collision change against living bodies, including owned Stay pets, and waits if it would remove support or add collision through them. Writes honor changed blocks and preserve block entities, original books, finite contents, ownership/health/orders and personal progression. Once finished, removed rails and later player edits remain removed. The old initials move only when their authored source and destination stone are recognizable.

Layout remains 36; protocol becomes 35 for the new native lid and descent behavior. Install **matching 0.4.62 client and server JARs**. Witness remains 43 eligible sources / 33 personal resolutions across two kinds, with three endings. The opening remains three in-game days/two sleeps. All 46 novel source finds and 16 blind scribble textures stay exact.

## Required release evidence

Exact-source validation requires all **468** native cases, all eight focused suites including **72 multiplayer** and **39 exploration** cases, the 17 playtest-log regressions, all 311 PNG CRCs/eight fonts, complete native writing, all 71 architecture views, the existing client proofs plus nine new native well mesh/fade views, and both actual socket clients with two reconnects and native yacht mining/crouching/crawling.

The new native multiplayer cases cover closed pen collision, shared finite food/pets/livestock across reload, actual one-meat taming and Stay ownership, spectator/body-gated saved-farm migration, preserved books/depleted barrels/personal wait, paused shared lid reload and a real spectator blocking final closure. Existing well cases now require gradual physical closure, continuous native ladder ascent and independent peer waits/return credit.

Validation pending on the candidate commit.
