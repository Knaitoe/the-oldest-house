# Novel sites and the wounded finale — 0.4.23

This pass implements the six House of Leaves places planned in the design document, with original writing and five personal resolutions. Karen's room is an anchor, excluded from Witness. The Minotaur survives its wound and remains in its cell.

| Place | Interaction and resolution |
| --- | --- |
| Zampano's courtyard | Real outdoor courtyard at a locally presented 3 a.m.; five native cats thin across visits into Mother's collection. One vanilla meat moves and adopts the last cat, revealing a physical key. Unlock the sealed apartment and read the survey's final page. Finite personal survey and collar; visited apartment doors lead back into this explorer's history. |
| The Whale | Four personal letters arrive in real opened chests, at least one game minute apart. The first pages spell THREE / ONE / TWO through paragraph beginnings. Knock at the middle attic with pauses; read the undated letter upstairs. Optional signed books are kept in saved outgoing mail; a reply responds to a few words in the submitted book. |
| Barn and well | Outdoor farmyard, temporary child height and real ladder. The cover closes at the bottom. Wait a present minute, climb above the cover, then collect the finite ribbon. Native departure, death and logout restore height. |
| The plain | Real outdoor dunes; an original articulated vulture circles the inaccessible silhouette. Hold a native spyglass on the shape for two seconds to make the original silhouette map. It returns by a subsequent morning after removal; observed storage is searched before recalling it. Mother's actual custody stops its return. |
| Hospital | Empty modeled incubator, native call button, original monitor cue and personal purple fog. Stay through a three-minute night; leaving resets the attempt, disconnection pauses it. The alarms stop regardless of calls. Read the dawn chart's final page. |
| Karen's room | Warm bed is the new native spawn exception beyond the threshold. Projector cycles up to six bounded photographs of the explorer's actual visited rooms. Respawning changes an expected vacant surface; every third wake briefly shows a private figure wearing the explorer's skin. No Witness credit. |

The outgoing letters, survey, key, collar, ribbon, apology and final chart use finite saved collection rules. Traded artifacts and shared scene flags do not convey Witness knowledge. The courtyard can be revisited; the other resolved personal stories leave that explorer's dealer. Existing evidence and completed endings remain saved.

Sixteen eligible sources require **twelve** resolutions across at least two kinds. There are still three endings. Layout 21 appends slots 33–38 without rebuilding earlier rooms. The three outdoor sites use separate horizontal locations in the existing outside dimension so their skies cannot be obstructed by stacked scenes. Network protocol is 23.

## The wound and the escape

- Block the Minotaur's charge with a shield, then hit the staggered creature with the original most-used weapon. A copy still passes through. The wound changes its behavior, without reducing a conventional health bar.
- The same actor crawls back into its cell and remains alive through the retreat, reloads and the completed escape. An expired crawl timer does not discard or kill it. A new explorer entering an already wounded scene finds the existing wound.
- Three fractures remove actual ceiling and side-floor blocks. Native falling masonry cannot become collectible loot or permanently seal the route. A west-wall breach opens over a supported dogleg; it leads to a continuous shaft with a real water catch. There is no relocation to replace the fall.
- Tom waits at the staircase camp, wearing the explorer's skin, and gives a finite walkie-talkie. Calls become garbled at depth. His collapse transmission cuts off; he does not return with the explorer.
- Below, physical rubble requires jumping and a 1.5-block lintel requires crouching. House fragments break overhead. Outer floor margins crumble behind the explorer; the center remains for following players and pets.
- Darkness and the existing note-burning mechanic remain. An added finite barrel contains flint, six pages and practical instructions. Hold flint in the main hand and a page in the offhand, then use for six seconds of light. Old caches do not refill on upgrade.
- A qualifying Mother-kept pet can still guide the route. Without one, the same physical route is traversable. Crossing all three damaged passages and pulling the final handle four times, with one-second pauses, opens the exit.
- Death still causes the permanent locked-out ending. Escape still evacuates residents and turns the real entrance into an empty lot. The peaceful Witness route retains its original staircase, same-day return and intact House.

Camera motion is a small dust tremor scaled to Minecraft's screen-effects setting, with a short presentation heartbeat. It never locks aiming or movement. Sky time and fog are client presentation; server time is unchanged until the existing unguided-ending time skip.

## Direct playtesting

Use a test world for permanent endings. With a spawned House:

```text
/oldesthouse finale prepare
/oldesthouse finale rehearse
/oldesthouse finale start
```

Preparation runs in bounded batches; retry rehearsal once ready. Rehearsal preserves held belongings, equips an original weapon plus shield and places the player beside the cell in Survival. It does not wound the creature or finish the ending.

For each new site:

```text
/oldesthouse vignette novel zampano_courtyard go
/oldesthouse vignette novel whale go
/oldesthouse vignette novel barn_well go
/oldesthouse vignette novel plain go
/oldesthouse vignette novel hospital go
/oldesthouse vignette novel karen_room go
```

The corresponding `status` command shows personal saved progress. Existing `/oldesthouse door <id>` commands also work. Karen's room offers a route anchor during ordinary exploration; pets and trail/chalk navigation remain available.

## Assets and validation

Eight generated 64px material tiles are integrated into native UV models: cell gouges, sealed windows, institute paint, collapse plaster, well carvings, archive paper, mail-slot metal and ward cloth. `tools/novel_materials_preview.png` shows their imported resolutions. `tools/build_novel_assets.py` authors prop/item models and four original mono cues. Pelafina has a separate compact provider in the existing handwriting system. The original Minotaur material atlas was reviewed and retained; its wounded movement now turns toward the cell.

The built-in image generator produced the material sheet. The prompt brief specified eight flat Minecraft pixel-art surface tiles in two columns and four rows: claw-gouged dark stone, a sealed painted window, worn institute paint, cracked collapse plaster, carved well initials, layered archive paper, weathered mail-slot metal, and muted lilac ward fabric. It requested an even tile grid, no perspective, objects, labels or rendered scenes. The imported native assets live under `src/main/resources/assets/the_oldest_house/textures/block/`; the preview shows the final 64px tiles used by the mod.

Native server checks cover real cat adoption/key/menus, timed chest correspondence and knocks, cover/scale restoration, spyglass memory and actual Mother custody, ward interruption and personal dawn reading, the native bed and actual projected map, append-only upgrade, physical collapse movement, finite new supplies and the surviving saved wound. Client lighting, animation, audio balance and the full in-game finale still need a human playthrough.

Holloway's separate torch-following hunt arenas remain a subsequent core-character pass; this release retains his existing shield and last survey in the finale preparation chamber. The six novel sites above are playable adaptations of the plan, not verbatim novel passages.
