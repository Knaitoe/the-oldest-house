# Owner's notes, October 30 (second document) — 0.4.193

Five notes with four screenshots. In the document each screenshot sits just above its note.

## Built here

### The waiting room's sign
The owner, on a screenshot of the reception ledge: "This should be rotated in the waiting room."
- **The cause.** The "Back in a moment" desk sign has its strut behind its face. At the fitting's default facing (south), its lettered face turned toward the partition, and the waiting room saw its back, or its edge from along the counter.
- **The fix.** It now faces north (`HouseReadRemainingRooms.waiting`), so its face is toward the room. This applies to new builds; quiet rooms are built per visit.

### Fewer rooms bled into the halls
The owner, on a screenshot of a laundry corner opening off a hall: "Try to reduce the frequency of these bleeding rooms. Design more hallways that are just hallways."
- **The cause.** Only three halls carry domestic fragments (kitchen, laundry, dining and bedroom corners): the straight, bent and cross halls. But they are among the commonest halls dealt before the stone depth, so about half the halls on the approach bled.
- **The fix.** Their weights before the stone depth fall from 26, 26 and 12 to 9, 9 and 4. About a quarter of approach halls now bleed. The rest are the alcove, offset and service-landing halls, the junction, the quiet room and the gray corridor, which are just hallways.
- **Unchanged.** Saved routes keep the halls already dealt to them. The bled-in homes still wait for their ten passes, and they come later now.
- **Since (0.4.197).** Four plain hallways: one with a jog in it, one that forks, one of pillars and one that zigzags. They append layout slots 114 to 117. See docs/PLAIN_HALLS_0_4_197.md.

## Built since

- **Skin tone (0.4.195).** The owner: "I'm realizing that skin tone isn't being properly applied." The tone drawn at the kitchen table now recolours every reader's own skin inside the House, keeping its shading. See docs/SKIN_TONE_0_4_195.md.
- **The child's drawing (0.4.196).** The owner: "a good opportunity to let the player select a feminine or masculine body, and maybe what kind of face they want – happy, scared, sad, etc. customization through a child's eyes." A pencil draws the face and trousers or a dress, and the reader wears them in the House. See docs/DRAWN_SELF_0_4_196.md.
- **The hands (0.4.194).** These turned out to be the shelter's dance: the reader's arms now hold the partner. See docs/LITERATURE_AND_DANCE_0_4_194.md.

## What stays

- **Protocol and layout unchanged:** protocol 101, layout 51.
- **Witness unchanged:** 55 / 42 / two kinds / three endings.
- **No PNG changes.**
