# Last Seen: build plan

The first of the three vignettes the owner approved on 17 November (`docs/THREE_VIGNETTES_PROPOSAL_2026_11_17.md`, section 1), built
to the proposal's recommended decisions:

- **The reader's own folder:** yes. It is in Nora's drawer on the last visit, with their face painted from their skin.
- **The Corner's girl:** yes, once, and without a word, for a reader who finished The House on the Corner.
- **The headscarves:** no. The park carries the history without borrowing that symbol.

It is built in five parts, like *Not Yet*. Each part is a release with its own doc listing every new line. The House does not deal it
until part 2, when there is something to do. It becomes a Witness source in part 5: the pool goes from 92 to 93, and the quota from 69
to 70 (three quarters of 93, rounded up).

## The place

`LabyrinthPlace.LAST_SEEN` ("last_seen", recurring, slot 244), outdoors under the outside sky, 68 × 110 (x -42 to 26, z 0 to -110). Every
position is relative to the base, the ground just inside the House's door. North is -z. People stand at y 0 on ground at y -1. Part 1
(`docs/LAST_SEEN_0_4_667.md`) gives the place as built; these are its outlines.

- **The office** (x -24..8, z 0..-14), a low municipal building in rendered block with louvred windows. The House's door is in its south
  wall, so a reader comes in at the front desk.
  - **The front desk** (the east part): a counter at z -6 with its lifting flap, two chairs behind it, benches for the families, and a
    typed card on the counter (the first paper). The poster wall is the partition at x -8, facing the desk.
  - **The archive** (the west part, behind the partition): steel shelves of folders to the ceiling along three walls, a long table with
    a lamp, and Nora's desk with its typewriter and one locked drawer.
  - **Two doors north:** the front door from the desk side (x 0), and the back door from the archive (x -16). Both open onto the park.
- **The park** (x -40..24, z -14..-85), behind an iron railing all round.
  - Paved paths in a cross, the dry fountain where they meet (-8,-50).
  - The fenced playground to the north-east.
  - The bandstand to the west (-28,-62), and a shut kiosk by the cross.
  - Benches and globe lamps along the paths.
  - Plane trees, and silk-floss trees with their swollen, spined trunks.
  - The gate in the north railing (x -9 and -8).
- **The avenue** (z -86..-98): pavement, curbs, asphalt, a bus stop with its shelter, and the noise of traffic that never comes into sight.
- **The houses** (z -99..-108): a row of rendered fronts with doors and shuttered windows, seen from the park. One door there, at x -16,
  is the way on.

## The people

All on stranger phases from 243 (the ten from 243 to 252, the office's three from 253 to 255) and painted on the cast's grid. Every name is original.

- **Nora**, the archivist. **Patricia** and **Luz**, at the front desk.
- **The returned**, at child scale, each matching a poster and a folder: Rocío (the first, at the fountain), Tomás, Micaela, Brian,
  Daiana, Lautaro, Abril, Joel, Yanina and Santino.
  - They don't eat, they are cold to the touch, and they don't blink.
  - One of them has a scrape on one knee that is still wet.
  - At dusk they stand facing the avenue.
- **The families:** Tomás's grandmother, who brings a coat; Daiana's father, who takes her off by the wrist and brings her back; Joel's
  mother, across the avenue, who never crosses; Brian's family, who never come.
- **Ocampo**, the man from the city.
- **The men at night**, three, with flashlights.

## The parts

| Part | Version | What it adds |
| --- | --- | --- |
| 1 | 0.4.667 | **The place.** The office, the archive and its shelves, the poster wall, the park, the avenue and the houses. Nora, Patricia and Luz at their places, each with their own line. A hand on anything answers. Its own art, ambient sound and door bleed. Not dealt. |
| 2 | 0.4.668 | **The archive and the first one.** Folders as things to carry: each shows its child's face, and says where and on what day they were last seen. Nora's rule. The day's folders, shelved by the reader. Luz comes in: a girl at the fountain. The reader finds her folder by her face and brings it to her. The House deals it from here. |
| 3 | 0.4.669 | **More of them.** Eight children in the park, each matched by the reader who brings their folder. The families. The fence round the playground, the notices on the lamp posts, and Ocampo telling Nora to close the files. |
| 4 | 0.4.670 | **The night.** The gates chained from outside, the men on the avenue with flashlights and sticks. A reader can take a child by the hand and lead them through the back door into the archive, where each stands at their own folder. A reader can keep the door, or leave. Children left in the park are gone when the men come over the railing; nobody is shown being hurt. At dawn the archive's children are gone too, each folder open on the table. |
| 5 | 0.4.671 | **After.** The park by day, empty, the fence down and the notices torn. Nora's drawer unlocked, and in it the reader's own folder: their face, painted from their skin; last seen at a door, on the evening the House first took them in. The account is in the folder. The Corner's girl, once. The Witness source, the stair, the together lines. |

## The beats, as the House keeps them

- **One clock.** The park runs one day at a time for everyone in it, as the Corner's street does. The day is the furthest any unfinished
  reader there has reached, so friends go on together. A reader's day counts only if they were there for its beat.
- **Each reader's own:** the folders they carried, the children they matched, whether they were there when the men came and still there
  at dawn, and their own folder. Spectators earn nothing.
- **Retreat.** Any visit can be walked out of by the House's door until the night, when a reader who has led a child in or is keeping
  the door has committed to it.
- **Children.** No child is ever shown harmed. At night the park's children are simply gone when the men are over the railing, and the
  archive's are gone at dawn.

## Art and sound

- **Drawn in code** by `tools/draw_last_seen_art.py`, at Minecraft's own pixel scale: the park's paving and curbs, the railing and its
  gates, the bandstand, the silk-floss and plane trunks and leaves, the fountain, the kiosk, benches, globe lamps, the office's
  render and louvres, steel shelves of folders, the poster wall (each poster a face and two lines of type), the typewriter, the desk
  and its drawer, the long table and lamp, the avenue, the bus shelter and the houses' fronts. Each poster's face matches its child's
  skin.
- **People** painted on the cast's grid by `tools/paint_character_skins.py`.
- **Sound** by `tools/generate_last_seen_audio.py`, every one subtitled: traffic beyond the railings, pigeons, paper, the typewriter,
  the gate's chain, flashlights switched on, men's voices on the avenue.

## Writing

No colour or age is named. The text never says how the reader feels. The reader's gender comes only through the House's read. Speech is
in the conversation panel and over heads, never in chat. Every new line is flagged in its part's doc.
