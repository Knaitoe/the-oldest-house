# The list, part 4a-3: wounds, stains and the dead found (0.4.710)

The last third of part 4a of *The list* (`docs/THE_LIST_PLAN.md`), after the hunt (4a-1, `docs/THE_LIST_0_4_708.md`) and the catch and
the hay hook (4a-2, `docs/THE_LIST_0_4_709.md`). The plan's row for 4a-3: wounds drawn on the five and on the slasher's body, blood left
on what it lands on, and the dead found by the rest of the five and by other readers.

No night uses it yet; nights two to four (parts 5 to 7) will. `/ghosthouse test the_list hunt` shows all of it.

## The wound

From the moment the hook goes in, the one on it has the wound drawn on them: a short slit torn through the front of what they wear, on
the left of the chest under the collarbone, black inside, threads standing out of it; the cloth soaked round it, unevenly and more below;
and runs from the soak down to the hem. Drawn on the coat's front, and on the body's front under it, at the skins' own grid (two of the
sheet's pixels to a painted dot). It stays while they kneel, slump and lie there, and goes when the hunt ends.

## Blood on the slasher

- **The clothes.** Each time the hook goes in or comes out, more blood is on the slasher: up the front of the shirt toward the face, and
  down the sleeve of the hand that holds the hook, from the cuff upward, and on the fist (three levels, kept in the record as `Blood`).
  It is drawn on the outer layer of the reader's own skin, over whatever they wear, by every viewer, and on the reader's own arm in first
  person, which is where they see it.
- **The sack**, if it is on the head when the blood comes: flecks thrown at its face, then a run down from the mouth, then soaked
  (three levels). The eyeholes stay clear.
- **Torn.** Mel's camcorder, when she fights back, tears the sack across its left cheek: a tear through it from the front round onto the
  side, raw threads at its edges, open to what is under it.
- **Dirt.** Lesley's fistful leaves caliche dust thick round the eyeholes and caught in the weave below.
- The sack keeps all of this when it is taken off and left in the town, and is put back on as it was. The body taken back takes the
  blood with it.

## Blood where it lands

Blood is left on the ground, in four ways, each turned the way it went:

| When | What | Where |
| --- | --- | --- |
| The hook goes in | drops | under the one on the hook |
| The hook goes in | a spatter, thrown toward the slasher | where the slasher stands |
| The knees give | drops | in front of them |
| The hook comes out | a spatter | between them |
| They fold over and go down | a smear, dragged the way they fall | where they knelt |
| They are dead | a pool | under the chest, spread to the ground either side of them wherever they lie near the edge of a block |

A stain goes only into an open cell over something with a solid top (or over grass, which it covers until it is taken up); a heavier
stain is never made lighter. Each is kept in the town's record with what was there before, and all are taken up when the hunt ends, or,
if the server stopped first, at the town's next look while no hunt is on. Walked through. A hand on one: "Drops of blood. Still wet."
and so on (below).

## The dead found

- **By the rest of the five.** Every quarter second, any of the five still on their feet (at their place, running, hiding or cornered)
  who sees one of the others lying dead (within sixteen blocks, in front of them, nothing between) says so, once for each. One still at
  their place screams and runs as well, and the scream sends the others near enough, as in 4a-1.
- **By other readers.** A reader who is not the slasher, within ten blocks, looking that way (within 35 degrees) with nothing between,
  is told who it is, once a hunt for each. Their record keeps that they found them (`Found`, a bit for each). The slasher is told nothing.
  Spectators find nothing.
- **Lying there,** the dead take up the ground they lie on: a low, wide box (1.1 by 0.4), so a hand reaches them where they are and
  nobody walks into a standing shape over a body on the ground. The box is set down at the height they knelt at: the game lifts a body
  whose box grows to the middle of the old one, and the five do not fall of themselves.
- **A death after logging off.** If the slasher logs off after the hook goes in, the death is still recorded as theirs when it comes.

## Single and multiplayer

- **Alone**, the reader sees the blood on their own sleeve and fist, the stains, the wound.
- **With others**, every viewer sees the blood on the slasher (sent to all, and again every two seconds while it lasts, so one who comes
  into town later sees it too), the sack's marks (carried on the sack itself), the stains and the wound. Each reader who is not the
  slasher finds the dead for themselves, and keeps it in their own record.
- **Spectators** find nothing and are told nothing.

## Retreat

No change from 4a-2.

## Made as

- **`TheListHunt`**: `WOUND` (the cast's marks bit), `DROPS`, `SPATTER`, `SMEAR`, `POOL`; `stain`, `tidy`, `stains`, `leftover` (from
  `TheListPeople.ensure`), `toward`; `bodies` (the dead found, every five ticks), `found`, `saw`, `look` (for tests);
  `FINDS_BODY` 16, `READER_FINDS` 10, `READER_LOOK` 35. Each runner keeps whom it has seen dead (`saw`); the hunt keeps whom each reader
  has found. Stains are kept in the town's shared record (`Stains`: where, and what was there).
- **`TheListBody`**: `blood`, `bleed` (the record's `Blood`, the sack's blood), `mark` (torn, dirt), `sackMarks`, `shown` (the payload),
  `killed(server, reader, who)`, `found`. The record keeps `SackMarks` while the sack waits in the town.
- **`TheListSack`**: `marks`, `bleed`; `BLOOD`, `TORN`, `DIRT` on the sack's own data.
- **`TheListBlock`**: the kind `BLOOD` (stages 0 to 3), flat, walked through. `TheListTexts.thing` has words for it.
- **`LiteraryActor`**: the fallen five's box.
- **Network**: `TheListMarksPayload` (S2C, the player and their blood); the protocol is 204.
- **Client**: `TheListMarks` (the blood on the slasher's skin and own arm, and the five's wound layer, `TheListMarks.Wound`, on the cast
  renderer); `TheListSackLayer` draws the sack torn and with its dirt and blood.
- **Art** (`tools/draw_the_list_art.py`): the four stains, their models; the wound over the five's skins; the slasher's three spatters;
  the sack's three bloods, its dirt and the torn sack. Checks: blood in every stain and more at each stage, the wound only on the coat's
  and body's fronts, no mark over an eyehole, the torn sack open where it tore, more spatter at each level.

## Tests

`TheListTests.theDeadFound` (new, batch `the_list_found`), with a second reader:

- Nothing on the slasher or the ground at the start. Shannon taken from behind at the bus bench, and the hook.
- No wound before it goes in. After: the wound; blood on the slasher and the sack; drops under her and a spatter where the slasher
  stands, both kept to be taken up; the second reader sent the slasher's blood.
- Dead: more blood on the slasher and the sack, sent again; a smear where she knelt; a pool where she lies; her box low and long, on the ground she knelt on.
- The second reader, set down beside her and looking at her, is told who it is and keeps it; told only once; the slasher is not told.
- Deek, set down in sight of her while calm, says so and runs.
- Ended: every stain taken up and the ground as it was; the wound gone. The body taken back: no blood in the record, and every viewer sent
  that there is none.

`TheListTests.theCatch` also checks that Mel's camcorder tears the sack.

## Seen in the frames

Eye-level frames of the catch under the stands (Mel, from the slasher's place, in front, from the side and behind) and of six readers
in a row wearing the sack (clean, torn, dirty, and the three bloods, with the spatter on their shirts), from in front, both sides and
behind.

- **The wound** reads as blood from the slasher's place while she kneels: the slit under her left collarbone, the soak round it and the
  runs to the hem of her coat, on the coat's own two-pixel grid. Face down it is hidden, as it should be.
- **The stains**: the drops under her as the hook goes in, the spatter thrown toward the slasher, the smear where she knelt and the
  pool all lie flat on the caliche, each its own shape.
- **Found and fixed**: the fallen Mel lay 0.7 of a block in the air. Her box, widened to 1.1, was lifted by the game to the middle of
  her standing height, and the five do not fall of themselves; she is now set down at the height she knelt at, and the case checks it.
- **Found and fixed**: the pool sat half a block to one side of her, because she lay along the edge of a block and a stain fills only
  its own. It now spreads to the block beside her too, each turned another way.
- **The slasher's blood** shows on the shirt and down the sleeve to the fist at each level; the third reads as soaked. Level one is a
  few flecks, enough to see at arm's length.
- **The sack**: the blood is thrown at its face and soaks down from the mouth, the eyeholes stay clear, the dirt greys the weave
  round the eyes, and the tear opens the left cheek so that the face under it shows from that side.

## Every new line

Speech, over the speaker's head as their name. Coming across one of the others dead (the dead one's name):

- Deek: "That's not funny, %s. Get up."
- Wes: "%s? No. No, no, no."
- Mel: "Is %s breathing? Is anybody checking?"
- Lesley: "Don't look at %s. Don't look."
- Shannon: "That's %s. That's %s on the ground."

Grey cue on the action bar, to a reader who is not the slasher: "%s lies face down, not moving."

A hand on blood on the ground:

- "Drops of blood. Still wet."
- "Blood, thrown out across the ground in a spray."
- "Blood, dragged into a smear."
- "A pool of blood, not dry yet."
