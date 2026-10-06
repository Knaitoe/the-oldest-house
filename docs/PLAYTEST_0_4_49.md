# Playtest pass 0.4.49

Status: candidate; exact-head native verification pending.

The attached staircase playtest and multiplayer requirements authorize this pass. Claude's two 0.4.48 repair commits are included, with actual-camera vacancy checks and a per-server stair-wear cache correction. The later staircase leaks design remains a proposal, not a feature in this release. Its pocket allocation, spectator proxy, edit restoration, cancel/return transaction and reconnect handling need a separate design review before implementation.

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

Growls accelerate from thirty to eight occupied seconds and dust/shake from twelve to four as the reader approaches the child. Nearby growls are louder and the camera shake is stronger; the user's screen-effect scale is respected. Shared section clocks pause without living participants and do not run faster for extra peers. Unrelated vanilla weather, music and ambient loops are suppressed inside the shaft while footsteps and gameplay actions remain audible.

## Placement repairs

- The mapping cabin's roof foundation no longer seals its cellar ladder or replaces its indoor wood floor. Its partition reaches the ceiling and doors have headers. Rugs and supported furniture give the front room a usable arrangement. The silhouette follows a slow, bounded circuit; the brother rests above the mattress, face up.
- Native glass panes join their neighbours and frames.
- The outdoor arrival vestibule loses its exposed white slot-fill appearance: siding retains the exact full-block return geometry, with rooted ground beside the annex. The central copied passage, doors, nonwhite blocks and player edits are retained.
- The flooded passage's route has two blocks of swimming headroom and a protected dry arrival.
- Existing scenes receive small saved corrections with loaded native entity sections, camera exclusion, body checks and bounded read/write slices. Containers, papers, actor identities and personal progress are not reconstructed. Stray slot fill outside authored outdoor scenery is removed without touching the copied arrival vestibule or authored story volume. Explicit rebuilds forget the repair checkpoint.

Layout 34, protocol 33, source/resolution counts and all three endings remain unchanged. Verification must cover every declared native case and focused suite, full writing/model/package proofs, sixty-four architecture views, and two real NeoForge clients with an actual reconnect.
