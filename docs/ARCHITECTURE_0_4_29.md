# Integrated architecture and interaction repairs · 0.4.29

Minecraft 1.21.1 · NeoForge 21.1.251 · Java 21. Layout 27, protocol 28. Install the same JAR on server and clients.

Claude's seven commits through `6f360a5135e306a61dd642182d4b65b26a46a676` are integrated unchanged before this pass: once-only room shells, framed windows and hearths, physical dunes and courtyard facades, mixed forest, native-facing shelf repair, actual terrain checks and the survival-only scene boundary guard. Their original 0.4.28 description remains historical.

| Requested area | Combined behavior |
| --- | --- |
| Stair rails below the first stretch | Both complete 1,280-block descents have straight-flight rails, guarded corner platforms and outer structural brackets. Nine walking blocks and thirteen-block landings stay clear. Native stair profiles alternate original masonry, tuff, deepslate and blackstone at depth. |
| Lake-town fronts | Framed civic windows and cornices, cottage sash windows and shutters, grocery storefront, two-storey brick bays, a weatherboard cabin and a separately framed boat shed. The submerged church keeps its old location, puzzles and roof hatch. |
| Lake-town interiors | Native classroom furniture; cottage sitting room and sleeping nook; grocery shelves/counter; shop and furnished upstairs flat; cabin fittings and boat-shed tool ledges. Existing papers, keys, containers, original desks and refuges remain intact. |
| Trailer | Weathered metal siding, window surrounds, wheels/axles, underframe supports, a stepped metal roof, porch canopy and offset tow frame. Dynamic cousins, bunks, kitchen and door vigil remain authoritative. |
| Holloway hut | Stone foundation and toe, rough timber braces, an earth/moss roof and supported entrance equipment beside the existing patrol area. The finite supplies, journal, paranoia and three-arena shield encounter are retained. |
| Archive | Brick/stucco front, paired pilasters, window frames, recessed portal, projecting cornice, lead roof and glazed rooflights. Original sealed side windows, appointments, gouges, survey and cat remain. |
| Outdoor spacing | Sites stand 4,096 blocks apart. The blanket 110-block haze cap is removed; each scene retains its own presentation. Native old-save relocation moves original blocks and block-entity data, hanging-object attachments, actor UUIDs, vehicle riders, return paths and camera coordinates. Reconnecting explorers follow the saved relocation. |
| Finale lock release | Only the actual owner releases the shared claim and physical seal. Peaceful prisoner departure releases before the long homeward walk. Offline combat remains paused and owned; stale terminal claims are repaired. Personal ending exclusion and commitment are retained. |
| Companion wheel | Owned native cats and wolves only, with server ownership/range/life/custody checks and matching client eligibility. Parrots and other tamable species keep their native interactions. |
| Canoe toss loss | Canceled tosses return the actual native stack. A full or partially full inventory saves residual originals with all components, then restores them once when space is available or as one owner-targeted drop when the binding ends. Native saves preserve that custody. |

## Upgrade rules

`scene_exteriors_0429` records a finite per-origin/per-scene pass and waits for visitors. Removed scenery is not replenished. It preserves block entities, beds, doors, story anchors, copied vestibules, attached supports and living/hanging residents. No actor or reward is added by decoration.

`outdoor_relocation_0429` copies native originals before clearing their retired island. Source entity chunks must finish loading first. Riders are captured before their vehicles move, then translated once with same-level native connection teleport. Existing lake decoration checkpoints follow the moved scene, preventing restocking. Recorded phone footage remains historical; only the live dropped-camera position moves. Explicit rebuilds remain distinct from an ordinary upgrade.

Stair carve version 429 waits for residents before replacing an older shaft. The preparation cache, cell, saved seal, creature and escape are preserved. The peaceful return now follows radius 24 instead of its obsolete radius-12 offset.

## Required native verification

The release requires all **263 declared native GameTests**, package/font atlas validation and the native font/NPC/cast/home client checks. Architecture checks include every actual tread on both descents, rails in every full depth band, native town passages, attached props, finite checkpoints and preserved actors/containers/evidence. Separate regression tests exercise cat/dog server eligibility, partial-inventory toss custody across a native save, owner-specific physical finale release, and island migration with a live rider, hanging frame and finite cache.

The native client renders **twenty-two architecture views**: nineteen authored scenes/camps and the upper, middle and lower staircase. Three outside-sky elevations retain the native gradient/star checks. Completed results and matching package artifacts are available in [the development build report](https://github.com/Knaitoe/the-oldest-house/actions/workflows/build.yml?query=branch%3Adevelopment).

No new Witness source or ending is added: **seventeen eligible stories, thirteen personal resolutions across at least two kinds, three endings**. The approved home, threshold, personal correspondence, retreat and prisoner design remains in [DESIGN_DOCUMENT.md](DESIGN_DOCUMENT.md).
