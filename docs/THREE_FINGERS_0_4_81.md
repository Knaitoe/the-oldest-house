# Three Fingers (0.4.81)

The owner asked for the monkey's paw vignette and confirmed two points. Real losses may be named, but the paw always corrupts the wish. The money is a small, real purse. Three Fingers is a cottage on the mill road on a wild night: a family at the fire, an old soldier with a story, and a withered paw that grants three wishes. Every wish comes true in the worst way it can.

It is a literary multi-visit scene in a new slot, 94 (`three_fingers`). It is built whole: the polish, composition, review and playtest passes skip it.

## The cottage

- **The cottage.** It is whitewashed and timber-framed under a stepped thatch roof with a brick chimney.
  - The kitchen door leads in from the House.
  - The kitchen has flagstones, a black range with a kettle, a plate dresser, a table and stools.
  - The parlour has boards, wallpaper over wainscot and a red rug laid into the floor.
  - In the west wall, a brick hearth holds a real lit campfire under a mantel and clock.
  - Around it stand the father's armchair, a cane chair and a chess table, the mother's armchair and footstool, and the soldier's chair.
  - A bedside table holds the soldier's pocketbook. A chest of drawers holds the family Bible.
- **Outside the front door.** A step and a dirt path run between beds to a picket fence with an open gate. Beyond them lie the rutted mill road, a lamp post and a milestone. Hedges and night close in on every side.
- **Lighting.** The road lamp burns and, while anyone is in the scene, gutters on a shared clock. The range glows a little.
- **The new block.** One `cottage` block (16 kinds × facing × stage, 128 states) carries every piece the household furniture does not: whitewash, wallpaper, wainscot, flagstone, thatch, range, kettle, dresser, mantel, sampler, pocketbook, Bible, lamp head, milestone and doorstep. Its 24 environment textures are drawn in code by `tools/generate_paw_assets.py`. Only whole cubes occlude. The sampler and mantel have no collision.

## Each reader's night

Everything below is per reader, kept in their own saved record. The family, the soldier and the man from the mill are private `LiteraryActor`s bound to that reader. Only that reader sees them, and only that reader can talk to them. The wind, the knocking, the scratching and the flames in the coals are private sounds and particles. Only the room, the fire and the road lamp are shared. A reader's people leave when that reader leaves, and the shared tick removes any actor whose owner is not inside.

1. **The telling.** Walter and Ruth Dunning, their son Will and Sergeant Coyle sit at the fire. The sergeant tells his story in three parts, every 15 occupied seconds or sooner when spoken to. Five seconds after the last part he throws the paw on the coals.
2. **The fire.** Reaching into the fire pulls the paw out. It costs half a heart, never below one heart. The paw is bound to that reader and shows their own count of fingers. Ten seconds later the sergeant leaves for the night. A reader who comes back has missed him.
3. **The first wish.** Held up in the cottage, the paw opens a menu of the wishes this reader can make:
   - **Money:** offered to everyone.
   - **A companion back:** only when the House actually saw this reader lose a tamed pet. It uses the name from their recorded native pet deaths, or "your wolf" for an unnamed one.
   - **The storm to stop:** offered to everyone.

   A finger curls down. Will laughs at it and goes up to bed for the early shift.
4. **The news.** This comes on a later visit, never the night of the wish. Will's chair is empty. A knock comes, and the man from the mill gives his news in stages. Each wish has its own corruption:
   - **Money:** Will was caught in the machinery. The firm admits no liability but pays compensation, "worked out to the penny": an envelope, seven emeralds and three gold ingots. It is given once and only once.
   - **Companion:** something scratches at the door first. Will went down to the race after something in the night and was caught in the machinery. The companion comes back only as its wet collar: a lead named for it, with the name still on the tag.
   - **Storm:** the wind dropped all at once. The wheel stopped and they sent Will down to free it, then the water came back. The wind never sounds at this reader's windows again.
5. **Mourning.** Ruth begs the reader to wish Will back. Walter tells them to leave it be.
6. **The second wish** is "Will back". A second finger falls. On a later or the same visit, after 20 occupied seconds, knocking starts at the front door, heard by that reader alone. It grows louder at 40 seconds, and from 60 seconds fists hammer at the door. Ruth pleads for it to be opened; Walter begs for it not to be. Coming back while the knocking is on starts it again from the first knock.

## Three endings, one source

The reader chooses how the last finger falls. Each ending readies the account; reading the soldier's pocketbook (the source) and then the Bible resolves the scene.

| Outcome | How | Account opens |
| --- | --- | --- |
| `made_the_last_wish_before_the_door` | Use the paw once the knocking has begun and wish it gone. The knocking stops mid-blow. The bolt comes back on an empty road. | THE LAST FINGER |
| `opened_the_door_onto_the_empty_road` | Open the front door once the knocking has begun. Nobody is on the step. The last finger falls without a wish. | THE ROAD |
| `gave_the_paw_back_to_the_fire` | Once the first wish has come home (mourning or knocking), put the paw back in the fire. It burns like anything else burns. Will's chair stays empty. | THE FIRE |

The fire refuses the paw before the first wish has come home. Each account's first line follows the reader's own first wish: the money, the collar's name or the wind.

## Losing the paw

A reader whose part has started but who no longer carries their paw can pull it from the fire again, at the same cost. "It is in the fire again, though nobody put it there." Each pull increases that reader's issue counter. Only the paw with the current issue answers, so a dropped or traded paw becomes "only a dried paw now". It is never a second paw to wish on. Another reader's paw never moves for you. Finite rewards stay finite: the purse and collar come once, and pulling the paw again gives nothing else.

## Witness and counts

`WitnessAccount.Story.THREE_FINGERS` ("The last finger", a release story) is the 44th eligible source. The derived quota stays at **33** (ceil(0.75 × 44) = 33), across at least two kinds. The game has three endings. Arrival, the sergeant's story, pulling the paw, a borrowed paw, a gift, a peer's progress and an observer's watching confer nothing. Only that reader's own outcome, followed by reading the source and the Bible, resolves the scene.

## Assets

- **Character skins** (`literary_paw_{walter,ruth,will,sergeant,messenger}.png`, stranger phases 9–13) are drawn in code by `tools/draw_three_fingers_art.py`. They are 512 × 256 atlases on the renderer's 128 × 64 layout, like the Last Stop's. The asset generator only supplies a missing file and never overwrites them. `art/inventory/three_fingers_0481.json` records their hashes.
  - **Walter:** balding, with white side-whiskers, wire spectacles, a collarless shirt, a tweed waistcoat with a watch chain, and carpet slippers. He wears a long green cardigan, open down the front, as his coat.
  - **Ruth:** greying hair parted and pinned in a bun, worried brows, a plum dress with a white collar and cameo, and an apron over the gown with its bow behind.
  - **Will:** dark tousled hair, a smudge of mill soot, a striped collarless shirt with rolled sleeves, braces, a red neckerchief and work boots.
  - **Sergeant Coyle:** a sunburnt, red face, small heavy-lidded eyes, a waxed moustache and cropped grey hair. His navy frock coat is buttoned to the throat in brass, with medal ribbons and a medal on the left breast.
  - **The man from the mill:** pale and careful, with a thin moustache, a black bowler on the hat layer, a black frock coat open over a black waistcoat, a white collar, a black tie and grey striped trousers.
- **The paw** (`withered_paw.png` plus `_2`, `_1` and `_0`, selected by custom model data from the fingers left) is drawn by the same tool. It is a dried, knuckled paw raised to wish, with dark nails, a faded red cord and fur at the cut wrist. Each spent wish bends one of its three long fingers down onto the palm, ending in a fist.
- **Six sounds** are synthesized and subtitled:
  - knock: "Someone knocks at the door"
  - hammer: "Fists hammer at the door"
  - wind: "Wind batters the cottage"
  - twist: "The paw twists"
  - scratch: "Something scratches at the door"
  - hiss: "The fire spits"

## Layout, protocol and tests

- **Versions.** Layout 42 / protocol 54 / mod 0.4.81. Saved worlds carve slot 94 once, as the 0.4.80 append did for slots 78–93. No earlier slot, route, actor, container or record changes.
- **Native cases.** Three new native literary cases:
  - `literary_paw_fire`: the build, private sergeants per reader, the telling, the fire's burn and paw, a peer's real lost wolf named in the menu, and the money wish leaving the peer unaffected.
  - `literary_paw_news`: the later-visit news, the wet collar by name, the second wish, the door refusing before the knocking, the door ending and personal Witness credit.
  - `literary_paw_endings`: the last-wish ending, the burn ending, the early burn refused, and a lost paw re-pulled with the old one inert.
- **Updated assertions.**
  - The pool assertions now expect 44 stories and 24 literary sources.
  - Slots now run 0–94.
  - Architecture checks the new interior (rug, reachable source and ending) and exports its view: 88 architecture views.
- **Build checks.** The build job checks the 128-variant blockstate and its models, the textures, the six sounds and their subtitles, the paw item's overrides and images, the skins and the classes.

Exact-source validation is pending.
