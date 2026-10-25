# Vignette defect review, 0.4.123

This review continues on the active `feature/borrasca-four-visits-0.4.108` line, preserving the latest depot changes and previous hallway, note, cast and quiet-cue corrections. The review distinguishes native structure inspection, model/render inspection, scripted gameplay and actual socket-client actions. A cutaway omits actors and uses full-bright lighting; it cannot verify visibility or atmosphere.

## Confirmed corrections

- **Second waitress:** a separate native staff exit and supported outside landing, in addition to the front exit. The kitchen door remains internal.
- **Tunnel woman:** presence and red-light pursuit no longer depend on reading the clipboard. The original starts within the tunnel. The production dispatcher now reaches her enlarged folded-body rendering bounds; distance uses the actual camera position relative to the actor, preserving native distance behavior away from world zero.
- **Foyer:** remove the solid courtyard front wall immediately behind the lobby glazing. This was a genuine blocked sightline and light-propagation defect. Keep the glazing and usable door, add real foyer spill, and check actual propagated light at the visitor while the floodlight is off. The native visitor becomes active without reading and recovers its same UUID only after loaded entity sections settle.
- **Courtyard support:** the original gravel sat above an empty row and could fall. Add native stone backing. A saved-yard repair recognizes gravel that actually settled one row down, restoring the original surface while preserving unrelated edits. A body that settled with it receives a clear native one-row lift onto that floor, retaining its UUID. Native tests wait for a legacy floor to fall before applying the repair, and check the fresh floor after real light cycles.
- **Cellar safety and old keys:** resetting a vacant home also respects living bodies in cut recesses. Legacy carried keys convert even after their reader completed the room and left it; finite UUID/component data and unrelated Wheel keys stay intact.
- **Layered man:** see `UNCANNY_PRESENCE_AND_CELLAR_0_4_123.md` for the home/basement, supported stair and ladder, tissue, finite key, worker, physical lighting and personal contact effect.
- **Gravity-drift hall:** the scene audit found two unsupported soul lanterns a block below their ceiling. Add their missing chains in fresh rooms and in the existing loaded, unwatched support-repair pass; keep the original lanterns, trench, ladders and exit. The existing native hazard case now checks native survival both fresh and after removing/restoring the legacy support.
- **Hidden-actor shadows:** the production dispatcher must reject invisible uncanny forms and the dormant hotel bath creature before shadow rendering. Returning early from their model renderer hid the body but left a shadow. The foyer client capture now waits a full second after each physical light-state change, requires real drawing in darkness and no actor draw in the lit interval.
- **Harrigan:** the laid-out casket mesh extends beyond its short physical body. Its renderer uses enlarged bounds, reached through the production dispatcher. The existing native vertex measurement now requires the actual posed body to fit those bounds, alongside casket clearance, folded hands, seated/standing isolation and closed joint caps.
- **Review evidence:** actual client captures now require the tunnel reader to remain on the native floor, and the giant/visitor to pass through the production renderer before capture. The cellar has a separate room overview before the contact close-up. Every fixture arrival must have a real supporting block. The full native scene-audit report is retained as a downloadable diagnostic rather than only temporary CI annotations.

## Wider scope inspected

| Scenes | Review performed and retained checks |
| --- | --- |
| Goatman / trailer | Native shell and porch cutaway, existing window-entry/crawl GPU checks, native supper/door/cousin regressions and actual-client expedition stages. |
| Ghosts set | Ground/upper/basement cutaways; dispatch, prop dimensions and pose paths; retain the existing twenty-case vignette-animation namespace and native filming/death/reconnect stages. |
| Lake house / Tess / canoe | Full house cutaway and existing native pushing, submersion, canoe seating and actual-client recording/playback checks. |
| Drowned town / slasher scenes | Town, forest/camp and hotel cutaways; existing actual survival-player pursuit/contact/refuge/door-path checks, plus client expedition stages. |
| Hotel / bath guest | Floor and cellar cutaways, shared versus bath renderer dispatch; retain fourteen focused native hotel cases, creature GPU pages and actual bath pursuit/contact/reload tests. |
| Farm / well | Farm and well cutaway, existing descent/cover-darkness/native sky tests and physical return/animal support checks. |
| Winchester / Masque / Wheel / child's room | Existing native cutaways reviewed for structure and approaches; retain actual scene-specific construction, interaction, route and finite-original regressions. |
| Borrasca | Native refinery cutaway and the latest source changes; prior full common-style cast pages and actual four-visit native/client progression remain required. Kyle and Kimber keep child proportions. |
| Remaining registered scenes and hazard halls | Full native architecture audit and gameplay suite cover door approaches, supported furnishings/floors, source/account connectivity, loaded old-save repairs and preserved original identities. Full-bright cutaways and passing tests do not establish that every ordinary playthrough is defect-free. |

## Validation status

The first compiling cellar candidate passed all 25 focused uncanny cases, native environment/model pages, focused literary/hotel/caver/staircase/seam/exploration/Goatman and multiplayer-story jobs. Its broader architecture run caught an outdated workshop-floor expectation; the assertion now requires the new cellar material and still requires full native support. The later font-client failure was downstream of that interrupted prerequisite and lacked its generated server corpus.

Reviewing the actual two-client captures rejected the tunnel and foyer images despite the old fixture reporting success: the tunnel reader had left the authored floor and the foyer visitor was hidden behind masonry. Strengthened gates must pass and refreshed images must be inspected before verification. The subsequent two-client preflight passed on 1e43d8a7, including original-night reconnects and actual giant/visitor render calls. Those captures exposed the unsupported gravel, which is fixed in the final follow-up. All 105 earlier native structure cutaways were reviewed, along with the ten new uncanny environment views and the native flesh/key/overlay pages.

The complete exact-source 741-case suite, 105 architecture views, native asset/font pages, actual clients and original-profile reconnects, and all twelve CI jobs remain required. This document does not claim completed ordinary playthroughs of all 52 source stories.

Audit annotations also flag deliberate off-floor interaction targets, cutaway leaf clusters, dark scenes and small vanilla plants that cannot survive their final light/ground state. These are not silently treated as route or actor failures. No scene is declared wholly free of defects from this report.

The actual-client fixture now retains the native dimension-loading screen and acknowledges initial cross-dimension arrivals only after the real destination floor is loaded/compiled and the reader is grounded with gravity enabled. Until that acknowledgement, the host may resend that same initial fixture placement and holds only its explicitly staged visual age. This does not move a reader during the subsequent four-waypoint bedroom walk, replace terrain, change production transitions or award progress.
