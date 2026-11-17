# Last Seen 1: the office and the park (0.4.667)

This is the first part of *Last Seen*, the first of the three vignettes in `docs/THREE_VIGNETTES_PROPOSAL_2026_11_17.md`. The owner approved the proposal on 17 November ("They read well ... begin work on implementing the vignettes"). It is built to the proposal's recommended decisions: the reader's own folder, yes; the Corner's girl once, without a word; no headscarves. The plan for all five parts is `docs/LAST_SEEN_PLAN.md`.

This part is the place alone: a municipal office at the edge of a city park, in the morning. Three people work in the office. The park behind it is empty, and its fountain is dry. Past the park's railing are an avenue and a row of houses.

Nobody has come back yet. The folders stay on their shelves, Nora's drawer stays locked, and the House does not deal the place until part 2, when there is something to do.

## The parts

The plan's build has five parts:

1. **The office and the park** (this release, 0.4.667): the place, Nora, Patricia and Luz, a hand on anything, art, sound and the door bleed.
2. **The archive and the first one** (0.4.668): folders as things to carry, Nora's rule, the girl at the fountain and her folder. Dealt from here.
3. **More of them** (0.4.669): eight more children matched to their folders, the families, the fence, the notices, and the man from the city.
4. **The night** (0.4.670): the gates chained, the men with flashlights, children led into the archive, and dawn.
5. **After** (0.4.671): the park by day, Nora's drawer, the reader's own folder and the account, the Corner's girl once, and the Witness source.

## The place

It is outdoors, under the outside sky, at nine in the morning (`SceneClock` mode 12, held). Every position is relative to the House's door: z 0, facing north. People stand at y 0 on ground at y -1.

- **The office** (x -24 to 8, z 0 to -14, five high). It is a low municipal building: render over a darker plinth, a flat roof behind a parapet, a floor of worn tiles, and tube lights. The House's door is in its south wall, so a reader comes in at the front desk.
  - **The front desk** (the east part, x -7 to 7):
    - The counter runs across the room at z -6, with a flap at its east end that lifts to let the staff through. It is down.
    - A typed card lies on the counter: the first paper.
    - Two chairs stand behind the counter, for Patricia and Luz.
    - Two short benches face the counter by the door, for the families.
  - **The poster wall.** Sixteen posters hang on the partition, facing the desk. The first ten are the ten children, each a face and two lines of type: a name, where they were last seen, and on what day. Every place named is in this park or on this avenue. The other six are older, and the faces in them have faded into the paper.
  - **The archive** (the west part, behind the partition, through a doorway at z -11):
    - Thirty-five bays of steel shelving run floor to ceiling along three walls. They hold folders standing spine out, each spine with a name and a date by hand. The top bay of each holds archive boxes labelled with years.
    - A long table runs down the middle, with its lamp and the day's pile, and three chairs.
    - Nora's desk has her typewriter, a half-typed sheet and the one locked drawer, with her chair behind it.
  - **Windows and doors.** Louvred windows look onto the park and the side street. Two plain doors lead north onto the park: the front door from the desk side, and the back door from the archive.
- **The park** (x -40 to 24, z -14 to -85), behind an iron railing with spear-topped posts every fourth bar.
  - **The paths.** A paved front runs along the office. Paths cross north-south at x -8 and east-west at z -50, with curbs where they meet the grass, and a few worn patches where people cut across.
  - **The fountain**, at the cross (-8,-50), stands in a round plaza. It is dry: a rim round a basin with leaves in it, and a plinth with its bowl on top, and a coin in the bowl.
  - **The playground** is in the north-east, on sand inside a low fence. It has a swing frame with two swings, a slide with its ladder, and a see-saw.
  - **The bandstand** is to the west: a raised octagon of stone with posts, a balustrade, steps up from the east, and a tiled roof rising to a point. Its eave is painted boards with a cast-iron fret hung on its outer face, on both faces at the corners. Its floor is walkable.
  - **The kiosk** stands by the cross with its shutter down and old bills pasted on its sides.
  - **Trees.** Fourteen plane trees stand along the paths and in the corners, their crowns rounded underneath and ragged at the edge. There are three silk-floss trees, their trunks flared into roots at the foot, swollen past the width of a block like a bottle, then narrowing to the fork; their canopies are in flower.
  - **Benches and lamps.** Benches line the paths and the plaza. There are ten globe lamps on posts, lit although the sun is up.
  - **The gate.** It is in the north railing, two leaves, shut but not locked. A hand opens it, and it stays open for everyone.
- **The avenue** (z -86 to -98): pavement, curb, two lanes of asphalt with a dashed centre line, curb and pavement. A striped crossing runs from the park's gate to the far pavement. The bus shelter on the near side has glass at each end, a bench, a roof and the stop's sign at the curb, and the timetable is torn out of its frame. Traffic is heard and never seen. A house's side closes each end of the avenue, so it turns out of sight.
- **The houses** (z -99 to -108) are four fronts of two storeys across the avenue, each in its own render (three renders by turns), with stone at its corners, a darker plinth along its foot, a band of stone between its storeys and a parapet over it. Each has its door in a stone surround, and tall windows set three pixels into the wall, each with a stone sill under it and a head over it, their roller blinds down. Their doors are locked, except one: the way on, at (-16,0,-99), a spruce door.
- **The office's row.** Two houses either side of the office face the park over their own strips of garden, dressed the same way, blinds down. The park's east and west railings look onto a strip of pavement, then the sides and backs of houses, which run on along the avenue's ends: their renders by turns, stone where one house ends and the next begins, a plinth, a band, and windows looking in on the park and the road.

## The three

Nora, Patricia and Luz are on stranger phases 253 to 255. Each sits in their chair (`ActorSeat` seats them at the chair's own height, facing the way the chair faces):

- **Nora**, the archivist, at her desk in the archive. She has a grey bun, glasses and a cardigan.
- **Patricia**, behind the counter, on the left. She has short hair and a pale shirt.
- **Luz**, behind the counter, on the right. She has long dark hair and a knit top.

Each has two lines: their first, and a second for every time after. Each line is spoken over their head and kept in the conversation history, never in chat. A reader's count is their own (`Heard0`, `Heard1`, `Heard2` on that reader's record).

The ten children are painted now (`literary_lastseen_rocio` to `_santino`, stranger phases 243 to 252), so that the posters can match them. They do not appear until part 2. Each has the face they went in, with a little grey under the eyes, level irises and a closed mouth. Earth has dried on one cheek, as if they had lain on that side, and there is earth on the knees, dried pale at its edge. All thirteen are painted by `tools/paint_character_skins.py` on the cast's grid and held to the catalog by the art check.

Each sits only if their body is not already there. Nothing is made while the office's entities are unloaded, so there is never more than one of each.

## Single and multiplayer

The place is recurring and shared: every reader sees the same office, the same three in the same chairs, and the same park.

- A hand on anything answers that reader alone, in a grey line over the hotbar, and changes nothing. The one exception is the park's gate: it opens for everyone.
- Each reader's talk counts are their own, so a second reader hears Nora's first line, not her second.
- The morning's sounds (traffic beyond the railing, pigeons on the paths, and the typewriter while someone is in the office) run on one clock while anyone is there, and stop 100 ticks after the last one leaves.
- Spectators are not readers: they hear nothing counted and change nothing.

The per-reader parts begin in part 2: the folders a reader carries and the children they match. The shared clock of days, which runs at the pace of the furthest unfinished reader so friends go on together, begins in part 3.

## Made as

- **The place.**
  - `LabyrinthPlace.LAST_SEEN` ("last_seen", recurring, slot 244), bounds (-42,-4,-110) to (26,16,0).
  - `LabyrinthBuilder.VERSION` 142.
  - `LastSeenSite`: the plan as constants, the build, and where a body is (in the office, the archive, at the desk, in the park, on the avenue).
  - `LastSeenRegistry`: sixteen materials (two more house renders, the centre line and the crossing's stripe among them) and the fitting block.
  - `LastSeenBlock`: twenty-three kinds of fitting, with stages:
    - The office: counter and flap; card; chair; shelf (three fills, one folder out, boxes); poster; table (bare, pile, lamp); desk (locked, unlocked, open); louvre; tube.
    - The park: railing and post; gate (shut, chained, open); lamp post (foot, shaft, lit, dark); bench; fountain (rim, corner, plinth, bowl); kiosk; silk-floss trunk; leaves (plane, silk-floss, thin edge); playground (seven parts); bandstand (post, balustrade, eave, step, corner eave).
    - The avenue: shelter (glass, roof, sign, bench); shutter (one block down or lit; a tall window's low and top halves, down, or the blind half up on a lit room). A window is part of its wall: nothing walks into it.
  - `LastSeen`: the three, a hand on anything, the morning's sounds, never dealt.
  - `LastSeenTexts`: every line, the ten names, and where and when each was last seen.
- **Art.** `tools/draw_last_seen_art.py` draws 94 textures, 114 models and their states at sixteen pixels, with `--check`. Each of the ten posters' faces is drawn from its child's look (skin, hair, style, eyes, glasses).
- **Sound.** `tools/generate_last_seen_audio.py` makes four sounds from noise and tone, every one subtitled:
  - traffic: three cars going by, each with its own engine and Doppler drop;
  - pigeons;
  - a folder sliding off a shelf, unused until part 2;
  - the typewriter.
- **The door bleed.** `bleed.last_seen` is the traffic and pigeons through a door, with a steady pale morning light (`tools/draw_door_bleeds.py` draws its picture: a plane tree's leaves against the light).
- **The House's tables.**
  - The literary places and the outdoor places.
  - The source (the card) and the ending (Nora's desk).
  - The miniature.
  - The staircase name ("the park by the archive").
  - A shared-account line.
  - The way on (a spruce door across the avenue).
  - The scene clock (nine in the morning).
  - A chair seat height for the cast.
  - The deal gate (never, until part 2).

## Checked

The new `last_seen` namespace and its own job run two cases.

`LastSeenTests.theOfficeTheArchiveAndThePark`:

- **Materials:** the office's render over its plinth, its floor and its ceiling; the path's paving with a curb where it meets the grass; the avenue's asphalt and curb.
- **The houses and the road:** the road's dashed centre line and the crossing's stripes at the gate; two houses side by side across the avenue, each in its own render with stone between them and a plinth along their foot; a tall window on each storey, its blind down, which is part of its wall (nothing walks into it); a window in the houses' side at the avenue's end, looking in.
- **Fittings:**
  - the counter and its flap;
  - the card, which is the place's source;
  - all sixteen posters, in order;
  - every shelf bay, with boxes on top;
  - the long table with its lamp and the day's pile;
  - Nora's desk, locked, which is where the account will be;
  - the three chairs at the three's places;
  - the fountain's plinth and bowl, and its basin dry;
  - both leaves of the gate, shut;
  - the way on, installed at its door.
- **Walking:**
  - from the House's door past the counter's end, through the partition's doorway and round the archive, without going through a door;
  - out through the front door to the park, the fountain, the playground and the top of the bandstand;
  - not past the shut gate;
  - once a hand has opened the gate, through it to the avenue, the far pavement and the way on's door.
- **The three:**
  - each in their chair once, at the chair's seat height and facing;
  - still seated with a reader beside them for two seconds;
  - each says their first line, then their second, counted per reader.
- **Lines and paper:** every thing answers a hand with its own line. The card reads as written, with no gendered word. The account's title fits a book.
- **The clock:** the scene's clock reads mode 12.
- **Not dealt.**

`LastSeenTests.theHousesDoorsStayLockedAndASpectatorOpensNothing`:

- A house door across the avenue answers a hand that it is locked, and stays shut. The way on stands among them, a door of its own.
- A spectator's hand does not open the gate.

Also:

- `ArchitectureTests`: the place lays no House rug (its own paving and tiles), and its source surface is the card.
- `LiteraryTests`: a place still being built is never dealt and is no source. The literary suite passes.
- The tool checks: art, audio, door bleed, the painter and the art catalog (343 entries).
- **Eye-level frames** (twenty, from the office's door to the avenue's end) were reviewed. They led to the windows being set into their walls with sills and heads, the houses' fronts each being given their own render, corners, plinth and band, the side walls' windows, the road's markings, the bandstand's eave and valance, the crowns' rounded undersides and the silk-floss's bottle-shaped trunk.

## New lines (for the owner)

All are in `LastSeenTexts` unless marked, and all are new.

**The hint:** "There is a typed card on the counter."

**The office:**

- Counter: "The front desk. Its top is worn smooth where people have leaned on it to wait."
- Flap: "The end of the counter, hinged so it lifts to let the staff through. It is down."
- Chair: "An office chair on steel legs. Its seat has taken the shape of whoever sits in it."
- Shelf: "Folders, spine out, floor to ceiling. On every spine is a name written by hand, and a date."
- Shelf with a gap (unused until part 2): "Folders, spine out, and a gap among them where one has been taken down."
- Boxes: "Archive boxes on the top shelf, too high to reach without the steps. The labels on them give years, not names."
- Table: "The long table. Its top is scored where folders have been slid along it, open, to whoever is sitting across."
- Pile: "The day's folders, in a pile, waiting to go back on the shelves."
- Lamp: "A desk lamp, on, though it is morning."
- Desk: "Nora's desk. A sheet in the typewriter, half typed. The one drawer is locked."
- Desk, open (unused until part 5): "Nora's desk, its drawer open."
- Louvre: "A louvred window, its slats open on the park."
- Tube: "A strip light. It hums very faintly."
- A poster of one of the ten: "A poster. A photograph of [name], and under it: last seen [where], on [day]."
- An older poster: "A poster, older than the rest. The face in it has faded into the paper, and the name with it."

**The ten, and where and when they were last seen** (names, places and days are all new):

| Name | Last seen | On |
| --- | --- | --- |
| Rocío | at the fountain in the park | a Tuesday in March |
| Tomás | at the bus stop on the avenue | a Friday in May |
| Micaela | at the park's north gate | a Sunday in July |
| Brian | outside the kiosk | a Monday in April |
| Daiana | on the bench by the plane trees | a Thursday in June |
| Lautaro | on the swings | a Saturday in September |
| Abril | at the bandstand | a Sunday in October |
| Joel | on the corner past the bus stop | a Wednesday in August |
| Yanina | on the path to the playground | a Friday in November |
| Santino | on the far pavement of the avenue, with a ball | a Saturday in December |

**The park:**

- Railing: "The park's railing. The paint on it is thick from years of coats, one over another."
- Post: "A railing post with a spear at the top, cold to the hand."
- Gate, opened by the hand: "The park's gate. It was shut, but not locked, and it swings open on its hinges."
- Gate, chained (unused until part 4): "The park's gate, chained and padlocked."
- Gate, open: "The park's gate, standing open."
- Lamp: "A park lamp. Its globe is lit, though the sun is up."
- Lamp, dark (unused until part 4): "A park lamp, its globe dark."
- Bench: "A park bench. Initials are cut into the back, and a heart, and a date."
- Fountain: "The fountain's rim. The basin inside is dry, with leaves blown into the corner."
- Bowl: "The fountain's bowl, dry. A coin lies in it."
- Kiosk: "The kiosk, its shutter down. The bills pasted on its side are years old, one over another."
- Silk-floss trunk: "A silk-floss tree. Its trunk swells like a bottle and is covered in thorns."
- Plane leaves: "The leaves of a plane tree, broad as a hand."
- Silk-floss leaves: "The silk-floss is in flower."
- Bandstand: "The bandstand. Nobody has played in it for a long time."
- The playground:
  - Swing: "A swing, hanging still on its chains."
  - Ladder: "The slide's ladder. Its rungs are worn smooth in the middle."
  - Slide: "The slide. The chute is worn smooth from top to bottom."
  - See-saw: "A see-saw, one end down on the ground."
  - Fence: "The playground's low fence."
  - Frame: "The swing frame."

**The avenue:**

- Shelter: "The bus shelter. The timetable has been torn out of its frame."
- Stop: "The stop's sign, on its pole at the curb."
- Shutter: "A window of one of the houses, its roller blind down."
- House door: "A front door. It is locked, and nobody comes."

**The three**, first line and then every time after:

- Nora: "We keep them all. Nobody has ever asked us to throw one out." / "The folders don't leave the archive. You can sit at the table with one, if you want."
- Patricia: "If you've come about someone, sit down and I'll take the details. If you've come to look, ask Nora." / "Everything you need to know is on the card. I typed it."
- Luz: "It's quiet in the mornings. People come in the afternoon, after work." / "You can see the whole park from the door. There's nobody out there yet."

**The first paper**, "The card on the counter", by the office, typed:

- "MISSING PERSONS: MINORS. / Open in the mornings. Bring a photograph if you have one. A recent one is best. Any one will do."
- "We will need the name, the date, and the place they were last seen. Be as exact as you can about the place. People remember the day better than the place."
- "Folders do not leave the archive. If you want to see one, ask at the desk, and someone will sit with you while you look."
- "Nobody here will tell you to wait. We know you are already waiting."

**The account** (unused until part 5, when it is rewritten from the reader's own record): "There was an office at the edge of a park, with a counter, and posters of children on the partition behind it. In the archive, folders stood on steel shelves to the ceiling. Outside, the park was empty in the morning, and the fountain was dry."

**Elsewhere in the House:**

- Account title (`LiteraryTexts`): "the park".
- Staircase name (`StaircaseProse`): "the park by the archive".
- Miniature (`MiniatureNotes`): "A park no bigger than a tea tray, a dry fountain the size of a button, and across a painted street an office with a counter, its wall papered with faces no larger than a grain of rice."
- Shared account (`CoopAccounts`): "%s {was} in the park with me that morning."
- Sound subtitles: "Traffic passes on the avenue"; "Pigeons coo"; "A folder slides off a shelf"; "A typewriter clacks"; door bleed: "Traffic passes and pigeons coo, behind a door".
- Block names: Park Fitting, Park Paving, Curbstone, Trodden Earth, Office Render, Office Plinth, Office Floor, Office Ceiling, Avenue Asphalt, Avenue Pavement, House Render, Park Stone, Plane Bark, Limewashed Render, Painted Render, Road Marking, Crossing Stripe.

No line names a colour or an age. The card's "MINORS" is the office's own category, as it would be typed on a municipal card; it gives no one's age. Neither the three's lines nor the card say what the reader is, and no line has a gendered word.
