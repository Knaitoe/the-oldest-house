# Playtest pass 0.4.49

Status: candidate; exact-head native verification pending.

The attached staircase playtest and multiplayer requirements authorize this pass. Claude's two 0.4.48 repair commits are included, with actual-camera vacancy checks and a per-server stair-wear cache correction. The user also authorized Claude's five note-triggered staircase scenes and custom assets. Their personal rooms, waiting reader bodies, saved template restoration, interruption returns and reconnect recovery are implemented and included in the verification contract below.

Internal doorway shifts send authoritative landing coordinates, preserve view and stride with native rotation/motion packets, and reset the connection's movement baseline immediately. Two-client runs exposed inconsistent predicted landings and a return consumed before vanilla's movement guard corrected the player back into the destination hallway. The live proof requires both physical returns and clean native movement validation across the crossings.

## Encounters and exploration

New ordinary hall discoveries use the six stone layouts from return depth seven. Existing drawn maps and shared discoveries are retained. One stone gallery builds with the core to keep a standing stone route available; the other five build three crossings ahead of their eligibility. Impossible anomalies keep their separate gates.

| Setting | Previous default | New default |
|---|---:|---:|
| Story offer, depths 6–9 | 8% | 22% |
| Story offer, depth 10+ | 16% | 38% |
| Additional chance per eligible dry discovery | 7% | 12% |
| Visits between stories | 3 | 2 |
| Eligible dry deals before guaranteed story | 12 | 6 |
| Hazard offer, depths 6–9 | 10% | 24% |
| Hazard offer, depths 10–15 | 16% | 36% |
| Hazard offer, depth 16+ | 22% | 48% |
| Visits between hazards | 3 | 2 |

These are conditional offers on fresh routes, not per-door rolls. A new arrival still has one story/anomaly/hazard budget. Backtracking does not reroll or bank dry deals. The complete old default preset upgrades once on config loading; a deliberately changed preset stays untouched. Settings are in the server config's exploration section.

## Equipment and Tom

Holloway's earned reward now uses a registered ShieldItem with worn wood, straps and rivets. Its native blocking, durability and Minotaur stun are covered by the existing survival-blocking fixture. Existing earned vanilla shields remain usable. The shield is not a new Witness source.

```
/give @s the_oldest_house:holloway_shield
/give @s the_oldest_house:lighter
```

The lighter inherits native flint-and-steel behavior and has its own pocket-lighter artwork. Legacy flint and steel still lights valid hearths. Tom visibly holds a lighter and gives one to his own nearby living explorer on interaction, once. The shelf no longer adds fire equipment automatically. The role/owner/distance and finite saved handoff apply separately in multiplayer. His radio name stays readable; static remains audible.

## Narrative and descent

A new original contains five connected chapters selected from that explorer's real Minecraft facts and confirmed House interactions. It uses a fold and thumbprint running across the pages rather than numeric ledger rows. Empty categories remain unwritten rather than inventing a past. The original words stay saved once composed.

To revise a previously issued unfinished original in a test world, carry your own current book and use the operator command:

```
/oldesthouse finale rewrite
```

The previous words are archived exactly. Found leaves, burned chapters, personal fire count, remaining finite inventory and unrelated item components stay unchanged. The carried book receives a new original identity so old copies cannot replay it. Other readers' books stay unchanged. Upgrading does not silently rewrite old originals or loose sheets.

Growls now arrive every sixty to thirty occupied seconds as the reader approaches the child; shake and dust accompany that same cue. A real masonry block dislodges and falls for each group of living readers within twelve blocks, with nearby readers across vertical section boundaries sharing the same clock. The backing keeps the shaft enclosed, and falling rubble neither hurts players nor leaves drops or walking-route obstructions. Nearby growls are louder and the camera shake is stronger; the user's screen-effect scale is respected. Shared section clocks pause without living participants and do not run faster for extra peers. Unrelated vanilla weather, music and ambient loops are suppressed inside the shaft while footsteps and gameplay actions remain audible.

## Placement repairs

- The mapping cabin's roof foundation no longer seals its cellar ladder or replaces its indoor wood floor. Its partition reaches the ceiling and doors have headers. Rugs and supported furniture give the front room a usable arrangement. The silhouette follows a slow, bounded circuit; the brother rests above the mattress, face up.
- Native glass panes join their neighbours and frames.
- The outdoor arrival vestibule loses its exposed white slot-fill appearance: siding retains the exact full-block return geometry, with rooted ground beside the annex. The central copied passage, doors, nonwhite blocks and player edits are retained.
- The flooded passage's route has two blocks of swimming headroom and a protected dry arrival.
- Existing scenes receive small saved corrections with loaded native entity sections, camera exclusion, body checks and bounded read/write slices. Containers, papers, actor identities and personal progress are not reconstructed. Stray slot fill outside authored outdoor scenery is removed without touching the copied arrival vestibule or authored story volume. Explicit rebuilds forget the repair checkpoint.

Layout 35 / protocol 34; source/resolution counts and all three endings remain unchanged. Verification must cover every declared native case and focused suite, full writing/model/package proofs, sixty-nine architecture views, and two real NeoForge clients with two actual reconnects including one from a private note scene.

## Note-triggered domestic scenes

Five original staircase sheets open personal scenes: kitchen (0), laundry bedroom (12), parked car (6), drawer repair (9), and the same kitchen later (32). Read the final page, close the sheet, and stay still for three seconds. Movement, damage, combat and a nearby hostile cancel the offer before entry. Each selected sheet is once per reader. Carried copies and borrowed books do not trigger it.

The chores use virtual cups, socks, a shopping list, radio, candle and drawer. Native menus prevent transfers to the real inventory. Readers keep inventory components, health, food, effects, XP, phase and their exact return pose. Waiting pets retain their UUID, health and wheel order. Each reader has separate saved room templates, restored after leaving; doorway exits, damage, disconnects, active-save reload and reconnect return to the stairs. Spectator cameras and uninvited readers stay outside personal rooms. These scenes add no Witness sources, rewards, leaves or fires.

The wallpaper, 35 supported prop meshes, seven item meshes and five original quiet cues are packaged with the mod. The kitchen’s cup arrangement carries into its later scene. The descent score fades out for the room and returns afterward. [Design](STAIRCASE_LEAKS_DESIGN.md) / [asset provenance](STAIRCASE_LEAK_ASSETS.md).

## Review repairs

- **Flooded passage.** The swimming channel keeps its two blocks of headroom. A low stone-brick kerb now edges the dry arrival and the far landing wherever the channel or a surfacing well meets open floor, so water cannot run out over the dry rows. Walkers step over the kerb.
- **Note scenes, room protection.** Nothing can be placed, used, broken or struck in a private room. The main hand still does the chore; the off hand, left clicks, block breaking and placement are refused.
- **Note scenes, saves.** The player's own saved copy decides whether they are in a scene. If the world was saved mid-scene but the player was saved after returning, the leftover world record is cleared at login without moving the player or touching their inventory, and the room is restored.
- **Note scenes, details.**
  - A late kitchen allocated before the first kitchen was finished is rebuilt with the cups where they now hang.
  - A room keeps only the visitor's return pose, not their inventory.
  - Readers with no scene are remembered as such, so the hot paths no longer copy saved records.
  - The scene engine is dropped again after the server stops.
- **Tom and the lighter.**
  - Tom's "Leave the radio on" line now plays after the handoff.
  - A reader who has lost the lighter, carries no other flame and still has a cold hearth ahead gets one more from Tom.
  - A hearth clicked without a flame says so.
- **Shaft sound and stone.** Creatures' own idle voices stay audible in the shaft; only ambient loops, weather, music and block ambience are suppressed. Masonry never falls where something is mounted on it or hangs from it.
- **Saved originals.** A House of Leaves record saved before titles were stored keeps exactly the title page it always showed.
- **Encounter preset.** The previous default preset is upgraded once, and the file records that it was checked. An operator who later chooses the old values keeps them.
- **Repairs and layout.**
  - The cabin, arrival-ground and sill construction stage runs once; deferred sweeps and restarts resume only the sweep, so player removals stay removed.
  - A layout bump keeps the record of places already carved, so standing halls are not carved again.
  - In an upgraded world, existing doors do not wait for a newly added core room that is still queued.
