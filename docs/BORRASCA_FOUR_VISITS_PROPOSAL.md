# Borrasca: the name beside yours

Design proposal — revised after owner review, 19 October 2026. Not implemented or playtested.

This specifies a new Borrasca vignette for new builds, replacing the current Borrasca implementation. Its scope is comparable to the hotel rebuild or larger. This document records the design; gameplay implementation and playtesting are future work.

## The central idea

The player makes a promise, loses the person who shared it, discovers someone has tried to erase that person, and chooses whether to put their name back.

The mountain is one remembered place across four visits, with years between the second and third. The same tree, trail markers, fallen timber, flashlight beam and distant sound acquire different meanings. The reveal implicates the Sheriff, Prescott and the ordinary institutions that sustain their operation. Stopping one machine is a concrete act; the wider operation and the Friend's fate remain unresolved.

Keep the story non-graphic. Abduction is communicated through absence and belongings. No captive display, assault imagery/audio, bodies in machinery, or playable abuse. Put the concise content note at the trailhead before Visit 2's investigation begins. Present it separately from the fictional trail notices, name the distressing themes without disclosing the reveal, and let the reader decline or reread it while the House return stays available.

## Spatial design

Build around a continuous rising pine trail: the House threshold, Mile Marker 1 and the Triple Tree, the bend at Mile Marker 2, the taking clearing at Mile Marker 3, then the chain-link perimeter at Mile Marker 4. The refinery occupies the ridge beyond it.

- The House door is visible from the lower trail, but not from every point in the woods.
- The Triple Tree is a three-trunk landmark with a real ladder, braced fort, railings and a carving face at the trunk base. Its roots shelter the entrance to the fern gully.
- Marker 2 is a narrow bend with a rock shoulder. The Sheriff can plausibly block the main path without an invisible wall.
- Marker 3 is a small clearing, framed by two pines and fallen timber. A side logging road explains the boot-and-tire tracks. The trees conceal the industrial approach beyond it.
- The gully passes behind the Tree, below the Sheriff's sightline, and rejoins the uphill trail beyond Marker 2. It is a walkable/crouchable route, not a teleport.
- Marker 4 overlooks the fence. Visit 3 allows only partial industrial glimpses; the machine's full silhouette becomes legible inside the compound on Visit 4.
- An unpowered service latch provides a physical way out of the compound. Cutting power must never trap the player.

Use native scale, eye height and collision to make the passage of years physical:

- Visits 1–2: the reader and Friend are children at comparable scales. The adult Sheriff's belt, badge and lowered flashlight are near the reader's eye line.
- Visits 3–4: the reader returns at adult height and looks the Sheriff in the eye. The fort feels smaller, and the old carving is now below their natural gaze. Weathered fort boards and older trail fixtures support the elapsed years.
- Derive the local scale from the saved visit and reapply it on arrival/reconnect. Release the scene's scale setting on departure, death and dimension change.
- Fit the ladder and fort to the child body and the gully to an adult's actual crouching collision. Growing up must not invalidate the bypass.

## Four visits

| Visit | Reader | Main verbs | Personal checkpoint before a completed House return |
| --- | --- | --- | --- |
| 1 — The pact | Child | Meet, follow, climb, listen, carve | The Friend has carved first; the player has carved beside them. |
| 2 — The taking | Child | Walk together, shelter, investigate, retrieve | The player has encountered the taking and personally examined/recovered the dropped compass. |
| 3 — The denial | Adult, years later | Examine the gouge, confront, observe, circumvent | The player has seen the erasure, heard the denial and reached the ridge perimeter by the bypass. |
| 4 — The names | Adult | Connect evidence, recover, disconnect, optionally restore | The player has established the human connection, recovered their Friend's keepsake, stopped the rig and physically returned. |

Visits advance only after an appropriate beat and a successful native House-door return. Logout, death, arrival and another player's progress are not completed returns. An unfinished visit resumes rather than skipping a chapter.

### Visit 1: the pact at the Triple Tree

Late afternoon: warm light through pine needles, red clay, wind high in the canopy. The Sheriff stands beside the trailhead sign and a recognizable county cruiser. His hat-tip is small and unhurried. He warns the children against going past **Marker 2**, matching the legend's boundary; the Tree at Marker 1 is within the permitted walk. His warning initially sounds protective.

Prescott makes an ordinary delivery at the trailhead. The Friend greets him by name, and the service pickup carries his company mark. Keep the encounter brief and familiar. It gives the later record a second recognizable person without adding an errand or explaining his business.

Give the Friend a fixed name and identity. Reuse an established named cast member where practical; never present them only as a generic quest label. Introduce their battered compass now: colored tape around its case, a habit of tapping its glass, an identifiable strap. This makes the later dropped object immediately theirs.

The Friend leads but waits at turns and at the ladder. They climb through native navigation/ladder movement and wait on the platform; they do not jump between scripted positions while watched. The fort contains modest evidence of actual use: a biscuit tin, folded blanket and old chalk marks, not a reward chest.

From the fort, the Friend tells the legend in a few short exchanges. It concerns the Shiny Gentleman, the pale figures, the sound and the warning about the second marker. It never mentions a machine or explains the sound's cause. The player can look around and move throughout. Down at the trunk, the Friend braces one hand against the bark, carves their own name, then offers the knife. The player's interaction adds their actual in-game name immediately beside it. Let the player see both names before anything frightening happens.

Suggested original line: “There. Now it has to keep a place for both of us.”

After the carve, one distant metallic chime comes from the ridge. The Friend listens, pockets the knife and gestures downhill toward the House. No Witness completion is awarded: Connection is the emotional seed, not a finished story.

### Visit 2: Marker 3 and the taking

The reader is still at child height. The content note is at the trailhead, before the taking route can begin; reading or declining it does not start the encounter. The Friend waits at Marker 1 with the same compass and clothing. Before setting off, they point to an older child's name that has been scraped away elsewhere on the Tree. Both pact names are still intact. This establishes deliberate erasure before it becomes personal.

The walk has two short shelter beats, not a repeating stealth gauntlet. The Friend reacts first to the metallic scream, ducks behind timber, then checks that the player is nearby. Disturbed needles and drifting dust show the sound's pulse even with audio muted. Distant pale figures remain ambiguous; their posture and spacing can later be recognized as workers around the industrial rig. Do not turn them into attackable monsters.

At Marker 3, the Friend reaches the edge of the clearing and pauses. The taking arms only when both original actors are in the authored encounter area. The Triple-Chime begins and the white searchlight strikes between the pines.

- The event lasts three occupied seconds. Save its phase so disconnect/reload cannot repeat the disappearance or produce a second compass.
- Looking toward the unobstructed beam produces a single rising bloom and white haze, not rapid flashes. Never force the player's camera.
- Looking away, using third person or sheltering behind timber does not cancel the story. Provide the Friend's interrupted call, heavy steps and movement along the obscured logging road. Their original body leaves view behind real cover; it must not visibly pop out of existence.
- Reduced-flash presentation uses dense dust, a dimmer beam and the same timing/cues. It changes no progression requirement.

When the light falls, the original Friend is absent and their compass lies in the mud. Boot impressions, tire ruts and a dragged strap point uphill. The compass, introduced in Visit 1, is enough to identify the loss without a floating objective arrow.

Recommended refinement to the proposed retreat lock: retrieving/examining the compass enables the *completed* return and next visit; it does not physically seal the House door. A frightened player may leave early and resume this unresolved aftermath later. A full inventory gets a clear recovery interaction rather than blocking the story.

### Visit 3: the Sheriff's blockade and the gouged bark

Return years later, at adult height, in colder light. The Friend's habitual waiting place is empty. The fort has weathered, but the broad, parallel steel-tool cuts through their name are fresh. The player's name remains. Let the reader approach and recognize the wound before offering an inspection response. Fresh cuts after all these years mean someone is still erasing names now.

At Marker 2, yellow tape spans the trail between actual posts. The Sheriff stands with a slowly sweeping flashlight. His uniform, cruiser mark and ordinary manner are familiar from Visit 1. The reader now meets him at eye level. He remains the same ordinary person, denying the same loss.

The denial happens whether or not the reader carries the compass. In every version, the Sheriff refers to Marker 3 before the reader has said where the Friend disappeared.

| Reader's situation | Encounter |
| --- | --- |
| Presents the compass | He denies that anyone was with them, demands the item, then slips on Marker 3. Handing it over and refusing remain real choices. |
| Arrives without the compass, or keeps it out of sight | He denies that anyone was with them and says there is no case to reopen, then makes the same Marker 3 slip. The reader can still discover the gully and complete the visit. |

The location slip is available through the encounter itself. It must not depend on showing an item the reader may have lost or left behind.

The demand is a real choice. Refusing does not immediately start combat. If the player hands over the original compass, save its custody and show it later among the refinery's confiscated belongings. Submission must not destroy the investigation or permanently disable its reward.

The gully is discoverable through bent ferns, clay scuffs and the remembered shelter at the Tree roots. A small route marker established during the first visit can help the player recognize its entrance. Do not require guessing an unmarked crouch hole.

The bypass is based on position, cover and timing:

1. Observe the flashlight sweep from safe cover.
2. Crouch into the gully behind the Tree.
3. Move between two visible timber shelters while a metallic pulse lifts dust and masks footsteps.
4. Pass behind the rock shoulder and emerge above the blockade.

Crouching alone is not invisibility. The actual cone, occluding terrain and movement outside the masking pulse determine detection. Give a readable warning before a catch: the beam holds and the Sheriff calls out. At a catch, he approaches and walks the reader back to the near side of the tape along the physical route. Use continuous ordinary movement and collision, with the reader retaining camera control; he waits if they stop. Resume the blockade after the walk and allow another attempt. Keep the investigation and inventory intact. There is no jump cut, teleport, instant death or mandatory fight. Retry timing stays consistent.

At Marker 4, the player finds the padlocked chain-link perimeter, utility conduit and truck entrance. The three-beat vibration is stronger through the fence. End this visit on the discovery of an ordinary secured workplace. The House return remains available via the gully.

### Visit 4: the Triple-Chime refinery

Night. The same gate is now breached: severed chain, fallen lock, no unexplained congratulatory helper. Do not imply who opened it without evidence. The gully remains a usable route past the earlier blockade.

Arrange the compound to reveal three connected facts through movement:

1. **The sound:** the crusher runs the exact rhythm heard in the woods. Its three contact strokes, not a separate horror soundtrack, produce the chime.
2. **The silhouette:** from the entrance angle, its polished housing and aimed work/searchlight form the Shiny Gentleman's shape. Walking sideways separates lamp, steel and machine. No cutscene or lore popup is needed to make the alignment work.
3. **The protection:** the Sheriff's recognizable cruiser/county marking is beside the conveyor. An impound sheet at the sorting table records the office's authorization and **Prescott** as the receiving/transport party. His company mark matches the ordinary pickup seen in Visit 1. These are familiar people performing recorded jobs in the same operation; the guilt extends beyond the Sheriff.

Write the impound sheet as a record: dates, property tags, contents, an authorizing office, Prescott's name and a receiving signature. Let those entries implicate them. Do not add an explanatory paragraph telling the reader what the record proves or summarizing the conspiracy.

The Friend's pack/jacket is recognizable from Visit 1, sitting beside the chute. Recover a small personal keepsake from it, such as their stitched name patch. The compass is the trail evidence; the patch is the final keepsake. They are distinct tracked originals, not two interchangeable quest tokens. If the Sheriff took the compass, it is here too.

Keep the causal meaning legible and non-graphic: the belongings were taken from people, the authorities knew, and folklore hid ordinary machinery and organized harm. The machine is not an innocent explanation that erases the disappearances. The Friend's exact fate is not newly invented or falsely resolved by stopping the motor.

The main breaker is visibly connected to the rig. Pulling it is an irreversible saved shutdown for this player's site: motor sag, three contacts losing their rhythm, then the belt and final ring settling. The machine sound remains off on reload and later returns. Keep enough emergency lighting to navigate. The silence contains wind, insects and footsteps, not victory music.

The player can now leave with the evidence, or carry the patch back to the Tree before using the House door. At the gouged carving, interaction offers a clear, deliberate name-restoration action. Re-carve the Friend's same name; do not replace the damaged bark with an untouched texture. The industrial scars remain around the fresh letters.

There is no returning ghost, restored NPC or supernatural thank-you. The final image is both names, unevenly, in the same piece of wood.

## Witness and the account

Borrasca keeps its fixed **Understanding** kind in `WitnessAccount.Story.BORRASCA`. Personally establish the evidence, recover the keepsake, stop the rig and complete the House return to earn one resolution, `silenced_the_mountain`.

Restoring the name is optional. Save the actual restoration and append one earned line to the account after the completed return, for example: “Before I left, I cut their name beside mine again. The gouges were still there.” The account must describe that act only when this reader performed it.

Connection describes the pact, and memory describes the optional act of restoration. Neither changes the Witness kind or grants another resolution. Keep the existing single-source kind lookup and all its readers; there is no outcome-dependent kind rule.

The pool remains 46 eligible sources, requiring 35 personal resolutions across at least two kinds, with three House endings. Restoring the carving changes the personal account and visible bark. It supplies no additional kind or eligibility credit.

## Custom art, poses and sound

| Element | Required visual states or behavior |
| --- | --- |
| Triple Tree bark | Old names; the Friend's fresh name; both pact names; Friend's name gouged; scarred bark with the name re-carved. Use a readable native name surface, not an entire texture baked separately for every player name. |
| Friend | Leading and checking behind, ladder climb, crouched shelter, hand-braced carving, offering the knife, pointing downhill. Keep one consistent face/outfit and original UUID. |
| Sheriff | Hat-tip, relaxed trailhead stance, flashlight inspection, demanding hand, guarded blockade, approach and walking escort back to the tape. The same recognizably ordinary person throughout. |
| Prescott | A recognizable face, work clothes and company pickup at the trailhead; matching name/company mark in the compound's impound record. |
| Trail and aging | Custom worn marker plates, clay boot/tire impressions, fallen pine timber, masking dust/needles and restrained yellow tape. Weather the fort and fixtures for Visits 3–4 while keeping the new gouge visibly fresh. Route cues work at child and adult eye heights. |
| Evidence | Compass case/strap and inventory icon; recognizable pack/jacket; recoverable name patch; an impound form carrying the Sheriff's office and Prescott's name. Appearance stays consistent between held, dropped and table states. |
| Compound | Connected chain-link panels, intact/cut lock, breaker on/off, county/cruiser insignia, conveyor and steel housing with painted edge highlights that do not require a reflective shader. |

Extend the existing Borrasca art vocabulary with additive sheets and native geometry. Preserve the retained atlas and unrelated character paintings; this proposal does not authorize a global repaint. Custom props must fit their actual supports and interaction volumes, not become full collision cubes because their textures look custom.

Use a common three-stroke source for distant chime, nearer scream and revealed crusher. Occlusion and distance change the mix, not the underlying rhythm. Add separate restrained cues for knife on bark, the flat steel gouge, tape in wind, compass in mud, chain/lock, breaker and motor wind-down. No score or sampled story narration. Subtitle important cues, including direction/meaning where appropriate, and retain Ambient/Master control. Visual dust and the flashlight sweep carry the stealth timing without requiring hearing.

Keep the reveal out of early writing and presentation. Visit 1's legend cannot mention the machine. Before the reveal, subtitles describe a metallic cry or chime from the ridge rather than naming a crusher. The final impound sheet stays a routine record; the reader makes the connection.

## Persistence, custody and recovery

- Keep the current per-reader persistent outdoor-site model and separate personal visit/evidence state. One explorer cannot erase another's Friend, shut down their machine, take their only keepsake or award them a Witness.
- Save the named Friend, pact names, original cast UUIDs, carving state, taking phase, compass custody, Sheriff encounter/escort, gully/perimeter discovery, keepsake custody, breaker state, restoration and confirmed returns. The saved visit determines whether the reader is a child or an adult.
- Reconcile an original actor's loaded phase/appearance with authoritative story state. Await a saved UUID's native entity load rather than spawning a replacement. The missing Friend must not return alive because an older entity snapshot loaded late.
- Keep finite evidence authoritative across inventories, item entities and confiscated storage. Examine/retrieve flags are earned in person, not from traded props. Recover tracked custody after interruption; do not spawn another valid original on every visit.
- Retain early retreat. A lost/dropped/destroyed compass must not seal the exit or erase the already witnessed taking; recovery follows saved custody rather than restarting the disappearance. Handing it over is explicitly supported.
- Pause timed encounter state while its owner is absent. Death/reconnect cannot duplicate a taking, skip a return or award completion.
- Violence against the cast chills/ends the attempt; it is not an alternate shortcut through the blockade or a source of Witness credit.

## Implementation scope

Treat this as a new vignette implementation in the existing Borrasca slot, replacing nearly all of the current Borrasca story and terrain code. Budget it at the scale of the hotel rebuild or larger. The work includes:

1. New rising trail terrain, the three-trunk Tree and furnished fort, an adult-passable fern gully, the taking clearing and the refinery compound.
2. A four-visit state machine, child/adult scale transitions, original cast movement and the timed taking sequence.
3. Bark displaying each reader's real name, persistent carving/gouge/restoration states, and two separately tracked originals: compass and name patch.
4. The Sheriff's flashlight cone, denial with and without the compass, handover custody, physical escort and retryable bypass.
5. The machine/searchlight alignment, shared complicity through Prescott and the Sheriff, impound record, saved breaker shutdown and optional account line.
6. Native models, poses, textures, original sound, full ordinary-play coverage and the existing release checks.

Likely touchpoints are `BorrascaRooms`, `BorrascaStory`, `BorrascaCast`, `BorrascaTexts`, the existing atmosphere/view payload, item custody and native prop/render assets. Keep Borrasca's existing Witness registration and fixed kind.

Apply the established **new-builds-only** rule.

## Playtest acceptance

Before calling an implementation complete:

1. Traverse all four visits from the normal House entry using native walking, ladder, crouch and interactions. Staged visit flags or camera teleports are not evidence of an ordinary playthrough.
2. Test looking away, third person, obstructed glare, reduced flashes, muted sound, a slow player and an inattentive player. Every version must make the loss understandable without demanding a precise camera angle.
3. Test refusal, voluntary handover, dropped items, full inventory, death, logout, unloading and restart at every taking/return boundary. Preserve exact original custody and cast identities; no live Friend on a later visit.
4. Test completion with and without restoring the name, including genuine House returns. Both retain Understanding; only the restoration adds the earned account line. Borrowed evidence, peers, arrival, incomplete escape and repeated restoration award no extra credit or kind.
5. Test the actual flashlight cone, terrain occlusion, both gully shelters and repeatable nonlethal detection/retry. Observe the Sheriff physically walking the reader back to the tape. Verify the denial and Marker 3 slip with and without the compass. Check child-height Visits 1–2, adult-height Visits 3–4, adult crouch clearance and scale restoration in every departure path.
6. Inspect native views of every carving state, held/dropped/recovered evidence, Friend/Sheriff poses and the searchlight-to-machine alignment. Confirm that sound stays stopped after reload.
7. Review the Visit 1 legend and early subtitles for machine spoilers, the trailhead content note before Visit 2, and the impound sheet for recognizable Sheriff/Prescott entries written as a record. Confirm that the fort has aged while the Visit 3 gouge looks fresh.
8. Retain the complete native corpus and unrelated focused coverage, package/art/writing/architecture checks, both actual clients and both reconnects. Rewrite the obsolete Borrasca progression cases for the replacement vignette and retain relevant custody, movement and personal-credit assertions. Add regressions for the new behavior; require all twelve exact-source CI jobs.

The intended last beat: the scream is gone, the gouge is still there, and their name is beside yours again.
