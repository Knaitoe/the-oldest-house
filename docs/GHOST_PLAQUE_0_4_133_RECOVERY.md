# Ghost-plaque continuation checkpoint — 0.4.133

The execution environment went offline before the uncommitted implementation could be extracted or pushed. This document preserves the precise work state; it is **not** a feature commit or a verified release. The active draft PR is #10 and its feature branch still points to 2fa3f9712895649c8c2055aab446453a074b11e4. Do not imply that the changes below are already in that branch.

## Workspace to recover first

Checkout: /workspace/scratch/a7f8c3717f13/vignette-review
Branch: feature/vignette-environment-audit-0.4.125
Base tree: b99496ddbb84cf31af306c669edb81b1cb20891d

The environment disconnected while attempting the final stale-test edit and another native run. That attempted command did NOT start. First check whether the checkout and its uncommitted changes still exist. Do not replace or reset it. If it is gone, the implementation source was written in this conversation's tool inputs; the complete feature tree has not been backed up by this checkpoint.

User has standing authorization for normal development commits/pushes/CI. Keep PR #10 draft; do not merge or call this a verified release without all current-source checks passing.

## Latest owner instructions

Add a proper door to Backseat. Painted interior: native mining must break the painted sun. Layered man: continuing to retreat after the first “come back” receives further spaced pleas, including “Please! ...please.”

Second waitress: follows and continues taunts/questions after the player leaves the booth; the handed menu shows food first and the original narrative on reuse; remove redundant counter narrative; actual eating animation and seven finite cake-like portions; eating or excessive café time transforms the same waitress; contemporary custom-textured bathroom; working kitchen appliances; small animated kitchen roach; actual till-side exit must conclude the personal encounter.

Courtyard: custom-textured, interactable filing drawers; preserve the seven reader names, original appointments and all finite custody. Some pages may be winding/labyrinthine, while other pages stay ordinary.

These diner instructions supersede the earlier conflicting page-consumption diner rewrite. Silo and the complicity-hallway structural rewrite remain separate unfinished backlog; do not claim they were implemented.

## Implemented locally

- Version 0.4.133; layout 47, protocol 88, 53 Witness sources / derived requirement 40.
- Backseat appended at slot 105. Public personal dealer integration, actual registered House source and private road return door, personal Witness completion only after the real crash and confirmed House landing. Pre-commit retreat/re-entry keeps the same car, seat, unread original notebook and edits, with no crash credit.
- SUN_BOARD accepted by the native LeftClickBlock START handler through House protection, removing both actual sun-board halves and saving BoardOpened.
- CellarDialogue saves a retreat stage, start time and location. Additional lines require further physical retreat after 45 / 110 ticks; returning below, eradication, rescue or completion suppresses them.
- The original handed menu first shows Pancakes / Waffles / Eggs and cheese / Grits. DinerMenuFoodSeen133 makes reuse show every word of UncannyTexts.source; original menu UUID and owned custody retained. Source/ending counter positions no longer open narrative. Native till still toggles ordinary state. Redundant LEDGER is absent in fresh rooms and removed only if recognized in saved-room repair.
- DinerWalk moves BOTH original waitress roles every native actor tick, using the existing bounded collision-aware SceneFootpaths implementation. The second waitress keeps following and addressing an off-table player.
- Each original meal has seven portions (MealBites133), with a native food-use duration / EAT animation. DinerPlate temporarily moves the actual original serving to the actual main hand and stashes an occupied hand in a real free inventory slot. Persistent context preserves interruption/login custody; no copied rewards or overwrites. Each completed bite returns the actual remaining serving to the same fitting and restores an unchanged displaced hand object. Full inventory and nonowner uses refuse safely.
- Food models lose a visible slice after each bite. Original food UUID/name/owner/components retained. Empty table placeholders are hidden while that actual serving is in the hand.
- First completed bite transforms the SAME private waitress to phase 3. Café presence also transforms after 3600 ticks, counting menu time; physical pursuit has 280 ticks of visible grace. Catch requires actual body contact and clear sight, not an elapsed off-screen death. Dialogue does not instantly fail the diner.
- A registered native till_exit DoorSpec at (-10,0,-6), facing WEST, returns to the actual saved House source. UncannyDiner.returned credits only the actual player after consumeReturn and confirmed landing, preserving peers.
- ServiceFittingBlock/Entity/Geometry/Renderer: real finite nine-slot drawers, bathroom toilet/basin/mirror/dryer/soap, opening microwave, timed toaster and kettle, and tiny six-legged crawling roach. Geometry and collision share native sixteenth-block cuboids. Drawers reconcile open state after reload.
- DinerDetails adds a genuine tiled bathroom x10..15 / z-28..-21, entry (10,0,-23), kitchen appliances on existing benches and roach at (5,0,-28). Recognized old-save repair is loaded/entity-ready, unobserved, bounded to 256 cells / 3 ms, and preserves player edits, originals and actual BEs.
- ArchiveReaderFiles builds 21 actual original-paper drawers across Beatrice, Leonie, Pauline, Ruth, Anne, Esther and Helen. Native container transfer permits taking the actual paper once; empty drawers stay empty through native BE/SavedData reload. Named wall signs and original appointments chest/book remain. Selected cross-reference pages wind down/up the page in the existing Zampano font.
- Existing static archive props have a separate once-only resident/unobserved recognized migration; edited labels and existing emptied modern drawers are not rebuilt/reseeded.
- New material painter tools/draw_vignette_details.py produces a native 16-pixel atlas and two contemporary bathroom tile materials. Existing breakfast painter emits 24 additional partial-portion models. No imagegen/photo assets. Protected 649 historical PNGs unchanged.
- UncannySocketProof/Client gained opt-in phases 19/20 for actual two-client menu food/reuse narrative, native animated seven bites, original private transformation, sequential physical till-side exits and confirmed independent House returns. These additions are NOT YET RUN.
- Base CI fixes: the staircase workflow checked declared==10 although all 11 native cases passed; corrected to 11. Backseat short rise/glass/impact/latch/lock sounds now preload to avoid missing collision decode on a slow returning client. The generator check passes; fresh actual two-client Backseat audio verification is still required.
- Existing original actors, private UUIDs, finite objects, books, cellar cuts, player edits, prior Backseat rain/body alignment and all preceding unrelated work retained.

## Actual verification obtained

1. All **14 required native cases passed**, with clean native shutdown, in the combined service + Backseat run:
   /workspace/scratch/a7f8c3717f13/service-car-check2.log
   Native output: “14 GAME TESTS COMPLETE”, “All 14 required tests passed”, BUILD SUCCESSFUL.
   This includes all nine new service cases and five Backseat cases, with the existing Backseat road case extended to use an actual personally DEALT source, physical pre-commit return/re-entry, same originals and reader-local Witness eligibility.
2. Four actual native GPU pages generated successfully and inspected:
   build/service-proof/bathroom-controls.png
   build/service-proof/kitchen-controls-and-roach.png
   build/service-proof/archive-drawers-and-page.png
   build/service-proof/seven-portions.png
   build/service-proof/gpu-passed.txt
   Material / drawer / portion art is present and coherent. The proof UI labels overlapped the kitchen/portion models. A layout-only fix was made to ServiceFittingProof afterward (smaller microwave; lower captions); rerender those four pages before calling the final captures checked.
3. Broader native run completed **68 cases, 67 passing**:
   /workspace/scratch/a7f8c3717f13/followup-check.log
   Only failure: aReadingMenuPausesThePersonalHazardWithoutRestockingAnything in UncannyTests.java, line ~192. It still clicks the old till source to open reading, which is intentionally removed. The physical menu now owns reading. All other older uncanny/environment/interior checks passed, including the updated breakfast and plate assertions.
4. Passed: draw_vignette_details.py --check; draw_diner_breakfasts.py --check; draw_vignette_interiors.py --check (shared mesh/collision); verify_textures.py (1458 PNGs); verify_writing_fonts.py; verify_character_art.py (155 human selections, 649 historical PNGs unchanged); verify_mod_branding.py --jar; generate_backseat_audio.py --check; workflow YAML parse.
5. No new feature commit pushed. No new full CI run. No new two-client diner or fresh Backseat audio run completed.

## Immediate next edit (not executed)

In UncannyTests.aReadingMenuPausesThePersonalHazardWithoutRestockingAnything, replace:
age(f,p,place,450); f.click(p,place,LiteraryRooms.source(place));
with an actual original-menu use:
age(f,p,place,450);
h.assertTrue(UncannyBreakfast.open(p,p.getMainHandItem()) && p.containerMenu instanceof LiteraryVignettes.Pages, "the original menu, rather than the till, opens the actual reading UI");

Keep the assertion that UncannyAge and OriginalMeal remain unchanged while reading. DinerPresence133 is separately allowed to advance per the owner's new timeout instruction. Do not re-enable counter narration.

Then rerun the entire 68-case group. The two initial native-fixture issues were already corrected: embedded connections need p.doTick() while using food to supply the real connection's normal player tick, and chat/metadata assertions must wait one actual host tick for packet delivery. Do not “fix” those by bypassing the native eating duration or weakening assertions.

## Runtime

Java 21: /workspace/scratch/a7f8c3717f13/toolchains/java21
Gradle: /workspace/scratch/a7f8c3717f13/toolchains/gradle-9.2.1
Wrapper: /workspace/scratch/a7f8c3717f13/run-gradle.py

The wrapper configures Java 21, proxy/truststore and a narrowly scoped NeoForm process-command agent. System default Java is 17. Minecraft sources/resources and 3888 assets were restored.

Use -Dorg.gradle.jvmargs=-Xmx768m -PgameTestHeap=3g locally to stay below the eight-GiB environment limit. The build's new gameTestHeap property retains the previous six-GiB default for full CI. Avoid concurrent Gradle tasks in separate tool execution scopes.

Successful native command:
python /workspace/scratch/a7f8c3717f13/run-gradle.py runGameTestServer -Dorg.gradle.jvmargs=-Xmx768m -PgameTestHeap=3g -PgameTestNamespaces=the_oldest_house_service,the_oldest_house_backseat -PgameTestDirectory=run-service-car-check

Broader command:
python /workspace/scratch/a7f8c3717f13/run-gradle.py runGameTestServer -Dorg.gradle.jvmargs=-Xmx768m -PgameTestHeap=3g -PgameTestNamespaces=the_oldest_house_uncanny,the_oldest_house_uncanny_environment,the_oldest_house_interior_review -PgameTestDirectory=run-followup-check

Prefer stdout through tee with pipefail, then poll the returned native exec session and require complete output. Custom GameTest directories outside the checkout put relative ../build exports next to that directory. The final run-* directories above are INSIDE the checkout, so the exports correctly reach repository build/.

Display helper: /workspace/scratch/a7f8c3717f13/run-display.py. It starts extracted Xvfb and the client in the SAME execution scope, DISPLAY=127.0.0.1:95, null OpenAL, software Mesa, LP_NUM_THREADS=1. Separate tool execution scopes could not connect to a standalone display. Xvfb and libs are in toolchains/x11; /usr/bin/xkbcomp was symlinked to the extracted binary.

GPU:
python /workspace/scratch/a7f8c3717f13/run-display.py python /workspace/scratch/a7f8c3717f13/run-gradle.py runClient -PserviceFittingSmoke -Dorg.gradle.jvmargs=-Xmx768m

Live CLI wrapper: /workspace/scratch/a7f8c3717f13/live-wrapper/bin/gradle, forwarding to run-gradle.py while filtering duplicate --no-daemon / --max-workers. Add that directory and Java 21 bin to PATH, then run the existing tools/run_live_expedition.py --uncanny-only under run-display.py. Both real clients/server/display must share the same execution scope. Preserve any previous run-uncanny-live and report folder before a fresh rerun. Also run --backseat-only fresh; it refuses stale worlds/pass/fail/restart files.

The new ServiceFittingTests and DinerArchiveDetailsTests are in the_oldest_house_service, with a matching copied empty.nbt template. The default test namespaces and CI service step include them. The environment cut off before a proposed run-checked.py wrapper was written.

## Important new local files

docs/PLAYTEST_NOTES_0_4_133.md
src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/ServiceFittingBlock.java
src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/ServiceFittingEntity.java
src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/ServiceFittingGeometry.java
src/main/java/io/github/knaitoe/theoldesthouse/client/ServiceFittingRenderer.java
src/main/java/io/github/knaitoe/theoldesthouse/client/ServiceFittingProof.java
src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/DinerDetails.java
src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/UncannyRooms0132.java
src/main/java/io/github/knaitoe/theoldesthouse/gametest/ServiceFittingTests.java
src/main/java/io/github/knaitoe/theoldesthouse/gametest/DinerArchiveDetailsTests.java
tools/draw_vignette_details.py
src/main/resources/data/the_oldest_house_service/structure/empty.nbt

UncannyRooms0132 is the exact base-commit UncannyRooms.java renamed to an immutable previous plan; obtain that exact content from git if reconstruction is required.

Many existing files also have uncommitted modifications: registry/routing/dealer/Witness, UncannyStories/Diner/Breakfast/Meal/Plate/Walk/reading routing, ArchiveReaderFiles, CellarDialogue, sun interactions, both native socket harnesses, shared rendering proofs, all hardcoded quota assertions, and .github/workflows/build.yml/build.gradle/gradle.properties. Use git diff if the checkout is recovered.

## Publication

Do not git add -A: temporary run worlds may be untracked. Curate source, generated resources, tests, workflow and approved docs. New PNGs require GitHub create_blob base64; empty.nbt can reuse the existing original blob. Compare the API-created tree SHA to local git write-tree, create the commit with the exact old parent, and update the feature ref with expected_sha. Existing GitHub connector can do all these operations even if local push authentication is unavailable. Full current-source CI is mandatory before a release claim.

The source tree is externally git-backed; do not duplicate repository items into ChatGPT Library. This recovery document is a separate GitHub checkpoint and does not substitute for saving the actual implementation.
