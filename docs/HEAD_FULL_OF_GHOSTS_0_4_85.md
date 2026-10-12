# A Head Full of Ghosts: filming days

Candidate 0.4.85 builds on `c52cb95`, the latest owner/Claude 0.4.84 changes. Protocol 57; layout 42. It changes the existing `GHOSTS_SET` source in slot 65 to a multi-visit vignette. Witness remains 44 sources, 33 personal resolutions across two kinds, and three endings.

The player joins the camera crew. Framing determines the material the production can use and the people it makes central to an episode. The family's experiences are not rewritten by the camera. A production cut and an unedited camera log remain separate, and neither settles whether Marjorie is possessed.

## The visits

| Filming day | Action | Useful choices of frame |
|---|---|---|
| 1 | Interviews and explicitly labelled reconstructions of the changed picture books and bedroom intrusion | Marjorie's performance, Merry's recollection, Sarah's request to return the books, John's explanation |
| 2 | Spaghetti dinner: the sparkly hat request, the silent green drool, the dead-things threat, the crawl beneath the table and John's hand | Marjorie's face, Merry's fear, Sarah's care, John's reaction, the real rusty fittings beneath the tabletop |
| 3 | Upstairs reconstruction of the wall incident and the threat to Merry's tongue | The damaged plaster, Marjorie's body, Merry asking to stop, Sarah interrupting the pickup |
| 4 | Basement conversation, followed by an upstairs pickup in which the cardboard playhouse is crushed | Putting the camera down for the private conversation; recording the sisters or the house's collapse |
| 5 | The evaluation, with Father Wanderly, John's belief and Sarah's medical objection | A performance, a father's fear, the objection or the person asking for help |
| 6 | The exorcism, the drawer mechanism and the movement to the stair landing | The mechanism the cut omits, Marjorie's plea, Merry's question or the landing |
| 7 | Final equipment pickup and the last dinner | The family's stillness, Merry, her unsauced plate, or the abandoned equipment |

The early incidents precede the novel's filming and are called reconstructions. The final pickup is an adaptation: this version places the player at a last visit to make the ending witnessable through the House's door. All dialogue and written production accounts are original adaptation text. The dinner uses the sparkly **hat**, rather than an unspecified garment.

## Door and filming controls

- Read the call sheet by the arrival, then speak with Barry to begin that day's take.
- Hold use with the production camcorder to film with the actual first-person lens. Put it away to speak with the cast.
- Sneak beside the dinner table to lower the actual collision body and camera beneath it. The top, legs and fittings continue to block movement and sight.
- After a take, Barry offers the edited episode and the unedited camera log. The booth monitor cycles that reader's recorded frames.
- A completed day advances only after physically leaving through the vignette door and later returning through it. A menu, timer, death or logout does not stand in for that departure.
- Leaving an unfinished day preserves its clock and positions. The next arrival resumes that day.
- Day 4 pauses while both sisters walk through the basement stair, landing stair and bedroom doorway. The player follows and speaks with Barry to begin the upstairs pickup.
- After all seven days, write the personal account at the booth and read the resulting own final account to resolve the existing Witness source.

## What framing changes

Only an owned camcorder in actual use records subjects. The native look vector and an unobstructed native block ray must include the subject. The most centred face receives lead time; even if several faces appear, the longest centred subject determines the broadcast's principal story. Holding Sarah gives the production a story about medical concerns and losing trust; holding Marjorie gives it a possession story; John gives it a rescue story, and Merry a frightened child's perspective. The cut can omit fittings or the mechanism that the camera log retains. Merry and Sarah can respond on later visits to having appeared in the previous episode.

The shipped client samples real rendered GPU frames, rather than creating a symbolic face or static substitute. Four scheduled keyframes per day include the later climaxes. Each is 24 × 16 RGB pixels, private to the reader and persisted with their day. Server-issued, expiring, single-use nonces check ownership, use, day, camera position and orientation, and bound the incoming array. A modified client can supply its own pixels; these frames are presentation, not a trusted authority for story completion.

## Home, cast and assets

The furnished home has a kitchen/dining room, living area, stair landing, two bedrooms, a basement and a production wing. Native custom geometry includes a crawlable continuous table, chairs, plates with sauce and Merry's plain plate, sequined hat, picture books, jug, cupboards, beds, lamps, equipment and visible rusty table fittings. A reader's playhouse and drawer are persistent, owner-bound native bodies rendered with custom meshes; their collapse/opening phases never affect another reader's take.

Marjorie, Merry, John, Sarah, Barry and Father Wanderly have separate skins. The common cast model has jointed elbows and knees and a small moving jaw: seated, slumped, still dinner stare, crawl, wall hold, bed, flinch and injured-hand poses use the same code in the scene and native visual proofs. Native bodies, seats and eye heights agree; movement uses native collision. Existing family UUIDs are claimed once where available, rather than discarded.

`tools/generate_ghosts_set_assets.py` reproduces the new native meshes, discrete textures, eight 512 × 256 cast atlases and camera art. The original picture-book illustration lives in `tools/asset_sources/ghosts_picture_book.png`. Its ImageGen brief: a childlike crayon drawing of two sisters beside an ordinary red-roofed home, blue windows and tangled green vines, on cream paper; a Minecraft pixel grid, without text. It is imported on a 64-pixel grid. `tools/generate_ghosts_audio.py` produces four original mono cues for low/thin voice timbres, chair scraping and cardboard tearing, each with subtitles. These are timbre/foley cues; spoken dialogue appears as text.

## Persistence and validation

Completed and already-ready legacy accounts stay completed/ready with their original text. The saved-room upgrade waits for loaded native entity sections and an unoccupied, unobserved area, including spectator cameras, and executes in bounded construction slices. Actual block entities are kept in their original cells. States unlike the legacy authored set, including removed pieces and prior decoration, are retained conservatively. Protected cells persist across interrupted upgrades, and edits during an upgrade are added to that protection. Original papers, inventories, family UUIDs and personal progress are not reset or replenished.

Validation is in progress. Require all twelve exact-source CI jobs, the complete native suite and focused suites, package verification, writing/fonts, all native texture decodes, GPU cast/fixture views, the new upstairs/basement architecture views and the existing actual-client/reconnect proofs. All 668 PNGs decode with valid chunk CRCs; the eight font grids, glyph coverage, punctuation and fallbacks pass their structural check. The thirteen new filming tests cover door-gated days, interrupted/reloaded state, owner-only camera use and real rays, bounded one-use frames, private cast identity and low bodies, legacy completion/quota, final reading, book-menu handoff, real dinner clearance, both stair routes, the cut's central subject, saved-upgrade interruption and preserved originals, and all seven actual takes through the collapse and stair landing. Do not present the candidate as a verified release before those checks pass.
