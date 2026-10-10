# Playtest fixes — 0.4.68

Eleven fixes from `This kid begins in the doorway.docx`, applied to Claude's completed Proofrock/Whale source `3ffcff14d87b9f4c48381e2af22e9639f0b2b327` (all twelve baseline jobs passed).

1. The eighth cousin's ordinary starting activity is at the cupboards inside the trailer, clear of the entrance. Existing actors at the former doorstep activity walk to the new spot through the existing route system; their identities and runner/impostor roles stay fixed.
2. Child head tracking wraps and clamps local yaw to 75 degrees and pitch to 60 degrees before composing the torso rotation. The connected hips, shoulders and neck remain in use. The native client proof renders eight look boundaries, including turns behind both shoulders and cowering poses.
3. All four raw/cooked pan displays lie over the actual griddle block. Updating an existing display also moves it, preserving its UUID. Saved fresh rounds refresh their pan positions on their first occupied tick. A native block ray through each serving must hit the griddle, never the adjacent sink.
4. A modeled cooler beside the kitchen counter supplies the finite sealed packs. Each opens into four raw franks; the occupied cooking clock and player plating remain authoritative. The children explain where to get them and how to cook/serve them. A save awaiting its unseen cooler upgrade can still retrieve its existing finite packs from the stove; it never replenishes them.
5. Crickets play one quieter clip every twenty seconds, rotating through three woods positions, instead of three simultaneous clips every six seconds. The scripted silence still stops them.
6. The RV has four stepped rubber wheels with hubs, tread and actual matching collision shapes. The original exterior route and supported window approach remain in place.
7. Proofrock's eight parked cars use native painted door panels, handles, chrome trim, chipped paint, headlights, grilles and glazed cabins. Their positions and existing blocking heights stay fixed, preserving the hunt and street clearance.
8. Every institute stair tread has three blocks of clear space overhead, cut after installing the attic railings. The attic retains its closed roof and walls; a bounded saved repair completes missing roof tiles. Minecraft's unrelated `ambient.cave` is suppressed inside the institute; story and gameplay sounds remain audible.
9. The archive's exterior uses stucco, stone bands and brick detailing instead of the interior peeling-paper/plaster material. The exterior pass now recognizes its own authored plaster. The rear interior paper groups, original sources and courtyard furniture remain untouched.
10. The waterline note has its own timber notice board, post and pixel paper texture. Its real saved block entity keeps the exact original book. Its native reading menu can hand over that original once; repeated takes and later fixture removals cannot regenerate it.
11. The institute has custom painted enamel POST, OUTGOING MAIL, visiting-hours and writing notices, plus brass number plates. Door plates are aligned with the actual doorway's center rather than the neighboring block. Her missing number remains two screw holes, preserving the SEVEN discovery. The private calendar, posted letters and pigeonholes retain their original behavior.

## Saved worlds

Layout stays **40**. The protocol is **42**; clients and server must both use 0.4.68.

`PlaytestFixes` records a separate once-only checkpoint for each scene. It waits for loaded native chunks/entity sections and vacant camera space, verifies all target cells before changing any, and replaces only recognized fixtures/materials. It does not rebuild scenes, advance story clocks, reroll actors, restock packs/desks or issue new evidence. Exact note pages go into saved custody before the old lectern is emptied; custody resumes after an interrupted transfer. Completed checkpoints preserve later player removals. Existing player blocks, containers and attached authored props are respected.

The previous farm, trailer cast, Whale and Proofrock updates remain included. The 409 earlier PNGs are unchanged; 32 fixture textures are added. Regenerate these additions with `python3 tools/generate_playtest_fixes_assets.py` after any complete Proofrock asset regeneration.

## Required validation

486 declared native cases, every focused suite, actual socket multiplayer/two reconnects, all 71 architecture views, complete native writing and eight font grids, 441 decoded PNGs, playable package checks, and the four existing trailer GPU views plus three new head/fixture/sign views. The existing supper case now exercises actual display-to-griddle block rays; the existing bathroom case covers the cooler/wheel save repair. New cases cover private institute state across an upgrade/reload and the note's original across block replacement, disk serialization and native taking.

The 43 sources / 33 resolutions / two kinds / three endings, opening three days/two sleeps, and deliberate well/stair darkness stay fixed. Candidate checks must run against the complete exact source; baseline results are not candidate validation.
