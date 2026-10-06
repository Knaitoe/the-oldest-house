# Exploration and shared halls — 0.4.48

This pass gives exploration longer intervals between stories and hazards, appends six ordinary hallway structures, and adds restrained shared room sounds and personal burning-page smoke. Multiplayer and existing-world preservation are release requirements.

## Hall structures and maps

Slots 72–77 append an alcove hall, offset hall, service landing, stone arcade, stone bend and stone landing. Earlier room coordinates and door addresses stay fixed. Wider bays, offsets, recessed glass, ceiling ribs and supported seating give these halls recognizable landmarks without folding their geometry. The stone variants appear from depth twelve; domestic halls fade by fourteen.

An existing personal route always restores its own saved map. Newly discovered ordinary/quiet destinations at the same route are shared even when the explorers' recent visits differ. Story availability, scent and living-pet searches remain personal. The shared discovery cache holds 2,048 entries and evicts its oldest entry; eviction never removes a personal map. Returning and reconnecting cannot reroll a remembered map or bank more story chance.

The old core remains usable while appended halls carve. Chunk preparation remains asynchronous; the six new structures execute through the existing construction recorder, with a maximum of 4,096 visits and a six-millisecond target per server-thread slice. Register doors only once their geometry finishes. Old halls, story scenes, actors and finite inventories are never rebuilt for this append.

## Server pacing settings

Edit `[exploration]` in the world's `serverconfig/the_oldest_house-house-server.toml`. Restart the server after editing. Existing routes retain their maps; changes affect fresh offers. These are per-arrival chances, not a fixed distribution of every door or every visit.

| Setting | Default | Effect |
|---|---:|---|
| `storyEarlyChance` | 8 | Initial story chance at depths 6–9 |
| `storyBaseChance` | 16 | Initial story chance from depth 10 |
| `storyChanceStep` | 7 | Added percentage points per eligible fresh dry deal |
| `storySpacing` | 3 | Visits separating ordinary story opportunities |
| `storyDryGuarantee` | 12 | Eligible dry deals before a guaranteed offer |
| `hazardEarlyChance` | 10 | Physical-hazard roll at depths 6–9 |
| `hazardMiddleChance` | 16 | Physical-hazard roll at depths 10–15 |
| `hazardDeepChance` | 22 | Physical-hazard roll from depth 16 |
| `hazardSpacing` | 3 | Visits separating physical hazards |

Only one story, anomaly or physical hazard can be offered by a fresh deal, across all its doors. Hazard rolls happen only when the story and anomaly rolls did not select an event. Dry spells pause during ineligible story spacing. The first five crossings remain domestic, later depth gates remain, and deliberate scent/live-pet rescue bypass normal story spacing. Rare spatial anomalies retain their existing separate low chance and breathing room.

## Atmosphere and comfort

Domestic halls have a quiet pipe knock, deep stone halls a low echo, and rest areas a wooden settling sound. These are original mono positional recordings from fixed room landmarks, with subtitles. One physical-room clock sends one cue to every listener; it advances only while the room has a living participant and pauses empty or offline. Rejoining does not produce a burst of overdue sounds.

After three separately occupied visits, the quiet room's familiar armchair can turn once. The whole room must be vacant of living bodies, native block and entity sections must be loaded, and every actual player camera, including spectators, must be outside a 48-block margin. No loading is forced for this change. Recognized original chairs only; removed/edited furniture stays edited. The cache, papers, furniture identity, pets and saved orders are untouched. Existing finite rest recovery is unchanged.

## Burning a personal leaf

After a successful canonical burn, the owner briefly sees an excerpt of that saved leaf rising from the hearth in its original narrator's hand. It lasts five seconds, respects native depth testing, and clears on death, respawn, level change or logout. No text is broadcast to peers. A brief quiet recollection sound is based on words/facts already in that saved account. The binding's original text, UUID, remaining components, found leaves and personal burn cursor still provide the sole authority; shared flames confer no peer credit.

Layout is **34**, network protocol **33**. Install the matching 0.4.48 JAR on the server and every client. Witness remains forty-three eligible sources, thirty-three personal resolutions across two kinds, and three endings.

## Descent playtest notes

Enclosed interiors and the great staircase suppress the outdoor sky and use black ambient fog, including creative and spectator cameras and a fully lit descent. Outdoor scenes retain their sky. Periodic Minotaur calls come from below the listener with falling stone dust and a brief camera shake that follows the existing effect-strength setting. Nearby explorers share one saved occupied clock; it pauses when nobody is present and does not change fires, creatures, progress or inventories.

The descent has an original 96-second sparse score on the native Music channel. Each client plays one instance and stops it on departure, death or level/profile change. It uses no borrowed recording. Sparse edge holes and rail breaks leave seven central walking columns, turn landings and original-leaf/hearth approaches intact. Existing shafts receive only three-cell changes at each planned edge, after native block/entity loading and vacancy checks, including actual spectator cameras and living Stay pets. Player-edited blocks are retained.

Loose stair sheets use 64 distinct domestic scenes, including washing dishes, sorting laundry and car journeys. New originals do not repeat across the authored landings; taking remains finite and personal. Every saved original keeps its exact prior words and components on reload. These are readable slice-of-life scenes; optional playable side rooms remain a separate design task. The family cast's seated render offset follows the actual native furniture height, including the child's scale. The Usher corpse renders face up within its existing two-block casket; the actor, location and lid/story state stay intact.

## Review repairs

- **Scene fog.** The black interior fog no longer covers scenes that set their own fog colour: the Goatman vigil and dawn, the hospital and the side mazes keep their tints.
- **Staircase shake.** The staircase's dust shake has its own short client lease. It no longer replaces a side maze's blackout, mist or caption.
- **Shared halls.** Spectators keep a personal map, but their arrivals never write the shared hall discovery cache, so an observer cannot decide the halls dealt to explorers.
- **Stair wear.** Edge breaks wait until every camera, spectators included, is at least 72 blocks away, which is wider than the open shaft. The bars either side of a rail break stop at the gap instead of pointing into it. A world whose wear is finished stops checking for it.
- **Descent score.** The score starts again if the sound engine drops it, for example when music is muted and restored. No game music starts over it while the listener is in the shaft.
- **Hall cues.** Each occupant hears a room's cue from the fixed landmark nearest them, placed every sixteen blocks down the hall, so the far end of a long hall is not silent. Spectators hear cues but never run a room's clock.
- **Landing bench.** The service landing's bench has its back to its own wall.
- **Loose sheets.** Each sheet is signed by a writer the scene does not mention. Saved originals keep their exact words and signatures.
- **Shared helpers.** The quiet chair and the stair wear share one vacancy check, and the room clocks share one counter.

## Required validation

Release remains pending until the exact candidate passes all **376** declared native cases, the staircase/multiplayer/hotel/literary/hallway/seam suites, fifteen exploration cases, the package/font/226-texture checks, full native book wrapping, four native ember hands, a native black-staircase pixel check and all **64** architecture views. The additional dedicated-server proof uses two actual Minecraft clients with socket connections and a same-profile reconnect. It exercises real interaction and movement packets, independent return stacks, a shared open door, native leaf menus, personal ember delivery and retained originals after native logout.

Run `gradle runGameTestServer`, or focus `-PgameTestNamespaces=the_oldest_house_exploration`. Run the connected proof under a graphical display with `python3 tools/run_live_expedition.py`; it starts a loopback-only offline proof server and disposable client directories. These proof hooks require their explicit JVM flag and are inert in ordinary sessions. Passing this controlled proof does not establish behavior for every third-party mod combination or network delay.
