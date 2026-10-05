# Review repairs — 0.4.47

Based on Claude's verified 0.4.46 commit `2cf20d39785c886da96201487c91d6437f150ebb`.

The staircase support repair now waits for native entity sections before checking living occupants. It plans every addition and removal before changing a block, and waits if an actual living body occupies a changed cell or stands on flooring that would be removed. A saved Stay companion remains in place with its original UUID, owner, health and order. Tom and companions on the retained camp floor allow unrelated repairs to finish. The existing `staircase_supports_0445` checkpoint remains authoritative.

Earlier 0.4.46 builds could save `scene_craft_0446` before later roof and barn corrections existed. The separate per-origin/per-scene `scene_craft_repairs_0447` checkpoint repairs recognizable stepped roofs on the bar, lodge, six camp cabins and end cabin, the barn's original roof or leftover birch lid, and the legacy Goatman lattice. It waits for loaded blocks, native entities, players and affected living bodies. One scene is considered per scheduled production pass. It runs only structural work, with no repeated furnishings, story construction or inventory creation. Finished roofs and mixed thickets are recognized; removed scenery stays removed. Fresh composition records both checkpoints, and an explicit rebuild clears both.

New personal accounts describe native walking distance and playtime as lifetime totals. They do not claim those totals were recorded before the House or only in the Overworld. Independent latest-break/latest-placement records no longer imply an unsupported sequence of work. Original saved pages, bindings, found leaves and burned fires remain unchanged.

Layout 33 / protocol 32 and forty-three Witness sources / thirty-three resolutions across two kinds / three endings remain.

Regression coverage includes a native Stay wolf on the old platform, safe retained camp occupants including original Tom, affected roof/forest companions, old completed scene checkpoints, all six cabin roofs, intermediate barn builds, exact native container property, real player-body trail clearance, saved-data reload, repeated repairs, recognition of already repaired structures and explicit rebuild checkpoint clearing.

Required before release: all 361 declared native gameplay cases, ten focused staircase cases, every existing focused suite, native writing and package checks, complete client proofs and all fifty-eight architecture views. Exact-head verification is pending the attached CI run.
