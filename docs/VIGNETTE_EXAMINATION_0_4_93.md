# Vignette examination — 0.4.93

October 14, 2026. Examination in progress. This checkpoint does not claim that the new candidate has passed native tests or that every branch has been manually played.

## Scope and evidence

Review all 45 eligible Witness sources, the recurring Red Room, Karen's anchor, the old cabin, nightclub and empty-lake aftermath, and their doorway/lifecycle integration. Trace entry, the actual player verbs, dialogue and time, physical actors and props, resolution, ordinary retreat versus commitment, save/rejoin, shared versus private state and finite originals.

The baseline is pushed source `7fe2dd580f247a64b1db0d4998b25bff508768e5`, CI run [38509353701](https://github.com/Knaitoe/the-oldest-house/actions/runs/38509353701): all twelve jobs and all 612 required native GameTests passed. The downloaded artifact checksum matches that run. Its ninety-four generated architecture views and dedicated cast/fixture views use the shipped native models and textures. All 45 eligible scene cutaways have been visually inspected. The Red Room has no full cutaway in this artifact; its construction depends on the player's snapshot.

Architecture cutaways omit actors, remove exterior walls/ceilings and use full-bright rendering. They establish layout/material evidence, not ordinary player lighting, actor movement or a complete playthrough. The older generic scene audit is report-only and treats several decorative custom blocks as interactions; its warnings require investigation and its presence in a green workflow is not proof that every warning is resolved.

Both supplied October 13 playtest documents were read: `The lights aren.docx` (fourteen pages) and `The lights aren(1).docx` (table supports and plating). Their accepted requirements remain part of the review, including painted cast art, Ghosts room gates/prayer/exorcism/departure, Harrigan's reading and originals, nightclub lighting/music and Goatman supper/idle dialogue.

## Confirmed defects corrected in this checkpoint

| Scene | Concrete trigger and previous behavior | Correction and regression evidence |
| --- | --- | --- |
| Farm/well | A new reader enters the farm and immediately tries to retreat. The return-door predicate locked them before entering the shaft. | Lock requires that reader's actual `WellEntered` state. The existing native vigil case now checks initial retreat for two readers, commitment only for the child who descended, and retains the full cover/wait/climb/original-ribbon checks. Native rerun pending. |
| Holloway's camp | A saved original is absent from native entity lookup for more than 100 ticks. Recovery could create a different resident and overwrite its UUID. | A saved original UUID remains authoritative regardless of elapsed loading time. A new native regression unloads and serializes the original, waits 140 ticks through SavedData reload, then restores the same UUID/name/health. Native rerun pending. |
| Wallpaper nursery | A seam's block has been changed or removed and the player right-clicks with no axe. The handler read the absent `PEELED` property and could throw. | Only a real wallpaper panel runs its seam interaction. A new native event regression uses an edited gold block and verifies no crash, replacement, new paper, progress or credit. Native rerun pending. |
| Ghosts set | Merry changes from sitting to prayer. That pose retained the old sloping thighs and an adult standing collider. | Prayer uses the same horizontal thighs, bent knees, seated collider and saved-seat fitting as her other seated poses. The existing native original-seat case now includes prayer; a separate assembled GPU prayer view checks the actual joint angles. Native/GPU reruns pending. |
| Review evidence | Generated scene screenshots hardcoded version 0.4.80 and the success log said 91 despite rendering 94 scenes. | Captions use the actual loaded mod version, identify full-bright cutaways and omitted actors, and log the actual view count. GPU rerun pending. |

## Release status

Candidate 0.4.93 / protocol 71 / layout 44. No new story, quota or ending: 45 sources, 34 required distinct resolutions across two kinds, three House endings. Two additional native cases bring the corpus to 614. All existing PNG bytes must remain unchanged. Complete per-vignette findings, targeted verification and the exact-source build will be added after the examination, before final delivery.
