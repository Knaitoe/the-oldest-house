# Ghosts Set playtest fixes (0.4.318)

The previous pass corrected dialogue advancement, added Barry's conversation panel and revised the sisters' stories and last supper. It did not complete the requested hair, bedroom staging or movement work. This pass addresses the screenshots and follow-up playtest reports.

| Playtest report | Change |
| --- | --- |
| Recording text replaces family speech | Camera toggles retain their sound and REC indicator without sending a competing action-bar message. |
| Marjorie looks too old and lacks long hair | Her painted face has softer features and level eyes. Dedicated back strands and front locks appear on her normal and dinner skins and follow the crawling and lying poses. |
| Marjorie faces away under the table | At the final crawl mark she turns toward John and the family. |
| John stands on the cupboard and keeps his arms raised | The cupboard moves to the back wall. John walks around the bed on the floor, raises his arms only at the help mark and releases the pose when the fall ends. |
| Furniture blocks later scenes | The nightstand, writing desk and pulled-aside chair have separate positions. Native collision checks cover the actual walking routes and their individual branches. Standing rooms from older builds are not rebuilt. |
| Sisters overlap at the bedroom pickup | They use the stairs and door in single file, retain a gap and finish on separate marks. |
| View suddenly points at the floor | During the private talk, downward pressure requires the reader's own camera to be held and recording. A small client turn accumulates slowly and can be overcome with the mouse. Stopping or stowing the camera immediately ends it. |
| Merry needs ordinary daily dialogue | Days one through six each have six everyday interaction lines. A reaction to the previous day's recorded cut is delivered separately, once, without consuming the everyday conversation cursor. |
| Barry's questions and answers sound wrong | All seven optional question branches are removed. His panel retains calling a take, reviewing footage and leaving. Spoken lines use his production pressure, impatience and evasiveness in the novel. |
| Pointless hallway track and clutter | Decorative cable strips are removed from unused hallways and rugs. The crew's working equipment remains in the den. |
| Family scene dialogue is missing from the journal | Scripted, idle and directly addressed speech is recorded when delivered to that reader, grouped by filming day. No dialogue is fabricated for an absent peer. Printed journal rows have enough vertical space for the font. |
| Overworld music plays in the vignette | The client stops already-playing music and prevents new vanilla music while inside the personal Ghosts house. |
| Bed expression looks mischievous | The bed pose has distinct frightened brows and mouth rather than the wall-climb expression. |
| Drawer cannot be seen from the doorway | A real wooden desk replaces the reused counter base. The drawer opens toward the bedroom entrance; the chair is pulled aside. A native doorway ray must reach the drawer. |
| Exorcism starts without enough buildup | A short room preparation precedes the prayers: Marjorie asks for warmth and the family responds. A white cloth, candle, repeated drawer movement and under-bed knocking build toward the mechanism and flight. Raw footage and the cut reflect what was actually recorded. |
| Marjorie runs through John and nobody reacts | She reaches John in her exit path, he recoils sideways and Sarah calls out. John, Sarah and the priest then follow at staggered intervals toward separate doorway positions. |

## Primary novel reference

The uploaded *A Head Full of Ghosts* scan was read directly. PDF page numbers refer to the scanned file, including front matter:

- 37, 50, 55, 112 and 136: dark hair tied back or hanging across Marjorie's face; tying it up briefly makes her look older.
- 111: the adult performer used to reenact a fourteen-year-old is explicitly criticized. The model should preserve Marjorie's youth.
- 133, 181–182, 193–195, 209–213 and 227–229: Barry's treatment of Merry, setup pressure, preference for usable dramatic material and handling of objections. He is not a documentary-ethics instructor.
- 232–246: the cold bedroom, cloth-covered desk, candles, prayers, Marjorie's pleas, drawer movement, under-bed noise, mechanism and violence around her departure.

Dialogue is original adaptation. The existing seventh-visit gear pickup and post-production reconstruction remain the mod's framing, rather than a claim that the novel contains a seventh broadcast episode. The scene account, Witness line, film keepsake and miniature description are updated with the revised staging.

## Validation and limits

Build 1450 on 0.4.317 completed with eighteen successful jobs, including the main build. The house-read-rooms job was cancelled during its asset/audio step, so the workflow remains cancelled, not a verified release.

All 82 required native cases passed together on a fresh world across Ghosts, house-read, tailoring, co-op, writing and the selected journal-history case. The package build passed. The generated writing-desk states and all 248 generated character sheets passed their asset checks. The new native cases exercise the actual camera toggle, private delivered speech and history reload, own-camera privacy cue, full sister pickup route, floor-level helping and pose release, doorway-to-drawer visibility, original cast identity, shove displacement and all three following endpoints. Existing per-reader filming, completion and return gates remain in force.

The first expanded route audit joined unrelated end marks into a path through a bedroom wall; its route list now separates actual walks and branches. A separate native visibility check caught the initially misplaced chair in front of the drawer, and the chair was moved aside.

Native model renders supplement the server cases; they do not constitute a complete manual seven-day playthrough. Layout changes require a newly generated set under the project's new-build policy. Full CI on this exact commit remains pending.
