# Pencil rubbings — 0.4.179

Step 3 of [JOURNAL_DESIGN.md](JOURNAL_DESIGN.md): rubbings. This is the owner's idea 2, "one-click pencil rubbings and margin scraps".

## Taking one

The reader holds their journal and right-clicks an authored object in the rooms. They get:

- a charcoal rubbing of it, laid a little askew on a page;
- one line about what the paper caught, written small beside it in graphite.

The journal then opens at that page, so the reader can write beside it.

- **Which page.** The rubbing goes below the writing on the last written page, if that page has no rubbing yet and holds no more than 180 characters or 8 lines. Otherwise it goes on the next page that has that room. Each page holds one rubbing.
- **Once per object.** Each object gives one rubbing per reader. A second try only turns to its page.
- **Every other right-click opens the journal.** The air, any other block, and a click on another reader's journal or an old copy, which shows its usual line.
- **The room answers first.** The click handler runs at the lowest priority and also sees clicks the rooms have already handled. So the cave's marks, the well's initials and the funeral home's plaque still say what they always said, and the rubbing is taken after. The click is then marked done on both sides, so the journal does not also open at its first page.
- **What it is not.** A rubbing is no reward, story step or advancement, and it changes nothing in the room.

## The first twelve objects

All of them are already standing in the rooms (`journal/Rubbings.java`).

| Rubbing | Object | Where | The line beside it |
|---|---|---|---|
| `gouges` | `cell_gouges` | the courtyard archive's trunk | Four grooves, evenly spaced. The fifth would be under the hand. |
| `well_initials` | `well_carvings` | the barn well | Two pairs of initials. One pair has been touched so often it is nearly smooth. |
| `sealed_window` | `sealed_window` | the courtyard archive | Painted shut, then painted again over the first coat. Nothing of the glass came through. |
| `scribble` | the scribble wall's `vignette_detail` | Zampanò's paper wall | Writing pressed so hard it went through the paper and into the plaster. |
| `room_number` | `hotel_room_plaque` | the Stanley | The number stands proud of its plate. One figure is worn smoother than the rest. |
| `town_sign` | `town_sign` | Proofrock | The letters are cut deep. Paint has been laid into them more than once. |
| `bark` | `borrasca_bark` | Borrasca | Letters cut into bark. The tree has grown over the first of them. |
| `ward_notice` | `institute_sign` | Whalestoe | Raised letters on a plate. The screws at its corners are newer than the plate. |
| `pasted_paper` | `archive_paper` | Zampanò's paper wall | Paper pasted over paper. The top sheet lifts at one corner, and there is writing under it. |
| `funeral_plate` | `harrigan_finish`, plaque | Peabody & Sons | Raised letters on a plate. Someone has polished the name and nothing else. |
| `cave_marks` | `cave_marks` | Ted's cave | Notches in rows, cut and not worn. Someone was counting something down here. |
| `maker_mark` | `streetlight` | Proofrock's streetlights | A maker's mark cast into the iron, and a dent at the height of a knee. |

- **The lines** follow the House's writing rules: they never gender the reader and never name a colour or an age. A test checks every line.
- **Left out on purpose.** The pigeonholes, school desks and notice post already open menus of their own, so they are not rubbable for now. More objects can join as rooms are reviewed.

## The art

- **Twelve rubbings.** Each is 64 by 64 pixels, charcoal on clear paper (`textures/gui/rubbing/<id>.png`), drawn by `tools/draw_journal_art.py`.
- **How they are drawn.** Charcoal lies on the raised surface in a diagonal paper tooth and skips what is cut in, as a pencil laid flat over paper does. So:
  - the gouges, initials, notches and town lettering show as gaps;
  - the room number, the ward plate's letters and screws, the funeral plate's name and the maker's star show dark;
  - the sealed window's glass is solid charcoal;
  - one figure of the room number, and the second pair of initials, are worn.
- **Checked in CI.** The script gains `--check`, which CI now runs, covering the journal icons too.
- **On the page** a rubbing is drawn 52 pixels square, tilted a few degrees by its object. The line beside it is in the reader's graphite.

## Saved data

- **Pages** gain a `Rubbings` list (`JournalPage.Rubbing`: id, position, tilt). It is set only by the server.
- **Unknown ids are dropped** when a page loads.
- **The reader's edits keep a page's rubbings.** A screen that sends a `Rubbings` list is ignored.
- **Each book** gains `Rubbed`: the object each rubbing came from, and its page.
- **The open payload** carries `Spread`, so the screen opens at the rubbing's page. The network protocol is unchanged (100).

## Tests and proofs

- **`JournalTests.eachAuthoredObjectGivesOneRubbingAndASecondTryTurnsToIt`:**
  - each of the twelve objects gives its own rubbing on a page of its own;
  - a second try only turns to the page;
  - each line keeps the writing rules;
  - stone gives no rubbing;
  - another reader's journal takes nothing;
  - a real click with the journal on placed gouges takes the rubbing, and a click on stone is left to open the journal;
  - an old copy takes nothing, and the new copy has every rubbing.
- **`JournalTests.rubbingsOutlastTheReadersOwnEditsAndAReload`:**
  - writing beside a rubbing never removes it;
  - a page sent claiming a rubbing lays none;
  - rubbings and the `Rubbed` record survive a save and reload.
- **The native client proof** adds `native-journal-3.png`: a spread with the room number and gouges rubbings and their lines. It fails if any missing-texture magenta shows. The courtyard views are unchanged.

No layout or Witness change: layout 51; protocol 100; Witness 55 / 42 / two kinds / three endings.
