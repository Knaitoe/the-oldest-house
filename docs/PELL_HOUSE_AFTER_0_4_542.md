# The Pell house 20: after the house (0.4.542)

The owner, 12 November: "start by finishing each unfinished vignette". The Pell house (`docs/PELL_HOUSE_PROPOSAL.md`) can be finished
since its nineteenth slice (`docs/PELL_HOUSE_DAWN_0_4_541.md`). This is its twentieth: what is left of the house afterwards, and a door
that still led into it. From the proposal's "Aftermath":

> Like the pool and the flat (`docs/AFTERMATH_0_4_244.md`), the house comes back once in a later hall.
>
> - **After a success:** empty, with a "FOR SALE" sign on the front lawn; the line scrubbed off everywhere, though its ghost shows in
>   the dead grass; the pool drained; in Nessa's room, only the height marks, and the "N" rubbed out.
> - **After Tilly answered:** lived in; the line fresh; on the door frame, one more pencil mark, higher, with a later date and an "N".

## Found while building it: a door already dealt to the house

`LabyrinthDealer.finishedFor` turns the door of a story a reader has finished into a plain passage. It counted a story finished only
once its Witness source was resolved, so after Tilly answered (an ending with nothing to read) the door a reader had already been dealt
still led into the house, and the house began its last night again. Since 0.4.542 an ending that has landed (`LiteraryVignettes.ended`:
the reader's `Ended`) finishes the story too, read or not.

## First light, either way

- After Tilly came out, each reader the morning is for now has their ending land at first light too (`Ended`), as after she answered.
  Their account is ready under the mat as before; read, it resolves the house as before. The house is never dealt to them again as it
  was, whether they read it or not, and the door they came in by is only a passage.
- So every ending is permanent, as the proposal has it ("There is no second attempt"), and an account left unread waits under the mat.

## The house comes back once

- **The registry** (`Aftermath`): the Pell house joins the scenes that come back, once (`Aftermath.limit`), where the others come back
  twice. A reader has finished a scene when they have read its account (`Completed`) or their ending has landed (`Ended`,
  `Aftermath.finished`). As for the others: never within the reader's last eight crossings, solitary, and no credit.
- **When:** the house is one, shared by everybody dealt it, so it stands as its aftermath only while everybody at it has had their
  ending (spectators count for nothing), and it is dressed only where the house would begin its visit again (`Household.fresh`, after a
  hundred ticks with nobody at it): never in front of somebody it has been playing for. It waits for the whole street's ground and
  bodies to be loaded, so nobody is left standing in it.
- **Nobody is in it:** June, Tilly, Callum, Gail and Nessa, Gail's car and the plates are gone, and nobody is made while it stands.
- **Each reader is told once** (`Aftermath.counted`), and a reader whose account still waits is told it is here, as at first light.
- **A reader who has not had their ending** brings the house back as it always was, and the visit begins again for them, from the
  earliest visit anybody there is on.

### After Tilly came out: empty, and for sale

- **The sign:** an agent's board on a post in the front lawn, to the street: FOR SALE in red on white, and a number to ring.
- **Everything the family had is gone.** What is built in, plumbed in or fixed down is left: the benches, sink and stove, the upper
  cupboards, the mantel and window seat, the picture hooks and the coat hook, the basin, mirror, bath and toilet, the power point and
  trough, the garage's bulb and pegboard, the shed's workbench, the kennel, the pool pump, and the circle painted into Callum's floor.
- **Nessa's room:** only the height marks. Beside the last one, Tilly's, the N has been rubbed out, a grey smear on the paint. The
  border, the stars on the ceiling and the taped switch are gone.
- **The line is scrubbed off everywhere.** Where it lay on the lawn the grass is dead; elsewhere the concrete, pavers, deck and mulch
  show where it went.
- **The pool is drained** to its tiles, its rope and light gone. The gate's padlock is gone; the gate stands open.
- **A hand:** the sign ("FOR SALE, and a telephone number to ring."); the marks ("Pencil marks up the door frame, a date by each.
  Beside the last one a letter has been rubbed out."); the ground where the line ran ("The paint has been scrubbed off. Where it lay on
  the grass, the grass is dead." or "You can still see where it went."); the circle ("Somebody has tried to scrub it off."); anything
  else left ("Left with the house, and wiped clean."). Every door and the pool gate open and shut to a hand, June's too.
- Coming in: "The house is empty, and the pool has been drained. On the front lawn, a sign: FOR SALE."

### After Tilly answered: lived in, and nobody home

- **Everything is where the family keeps it,** and the doors are shut.
- **The line** has been gone over again all the way round, and is still wet.
- **The pool** is open, the lane rope across it, the gate padlocked.
- **The height marks:** above the rest, one more, dated later, with an N beside it.
- **A hand:** the marks ("Pencil marks up the door frame, a date by each. Above the rest, a new one, dated later, with an N beside
  it."); the line, wet; each thing as it always was. June's door is shut; the shed and the gate are padlocked; the other doors open.
- Coming in: "The line has been painted again, all the way round, and it is still wet. Nobody is home."

## How it is made

- **`labyrinth.PellAftermath`** (new): which ending stands (`dressed`, from the marks' upper cell), each reader's (`ending`), the house
  the readers at it would find (`wanted`), whether it stands or is about to (`holds`), its tick before the visit's, dressing it
  (`dress`) and bringing it back (`undress`), and a hand on it (`click`).
- `PellPeople.tick` runs it before the visit; `PellHouse.arrive` and `reader` make nobody while it holds; `PellHouse.click` asks it
  first.
- `PellDawn`: after Tilly came out, each reader's ending lands at first light (`Ended`).
- `Aftermath`: `PELL_HOUSE`, `limit`, `finished`; `alone`, `canDeal` and `returning` read `finished`. `LiteraryVignettes.ended`;
  `LabyrinthDealer.finishedFor` counts it.
- `PellThing`: `FOR_SALE` and `MARKS_AFTER` (flat, stage 0 rubbed out, 1 one more). The height marks' shape is as wide as the N beside
  the last mark, which a narrower one cut to its first stroke; the frame stays where it was.
- `PellTexts`: the aftermath's lines; `thing` for the two new kinds.
- Art: `tools/draw_pell_things.py` (`for_sale`, `for_sale_back`, `marks_hi_rubbed`, `marks_hi_later`; the sign's model; the marks'
  models widened).

## Decided here (any of it can move)

- The house comes back once, not twice like the others.
- After Tilly came out, the ending lands at first light whether or not the account is read; the account can still be read under the
  mat, at first light or in the aftermath, and resolves the house then. Nothing else in the aftermath counts for anything.
- What counts as built in, and that Nessa's room keeps only the height marks (no border, stars or taped switch).
- Lived in, the pool is as June keeps it a week in (open, the rope across it) and nobody is home.
- Two readers whose endings differ, at the house at once, find the first one's.

## Not yet

- After Tilly answered: the cane tapping behind doors in the House that will not open; the forged entries carried forward; a torn false
  page pinned in a later hall.
- Gail's third mark (open question 2); a reader playing Tilly (co-op); the kids next door.

## Checked

- `PellHouseTests.afterTillyCameOutTheHouseComesBackOnceEmptyAndForSale`: a reader whose ending has landed, their account unread; the
  door they came by a passage, the house able to come back eight halls later; dressed when the house would begin again: the sign, the
  marks, everything gone but what is built in, no stars, the line scrubbed off (dead grass on the lawn), the pool drained, its gate
  open, nobody; told once, with the account waiting; counted once; a hand on the sign, the marks, the ground where the line ran and the
  kennel; doors and gate open and shut; the account read there resolves the house; then a new reader brings the house back as it always
  was, on their first visit.
- `PellHouseTests.afterTillyAnsweredTheHouseComesBackOnceLivedInWithOneMoreMark`: a reader whose ending has landed with nothing to read;
  the house lived in with one more mark, the line wet, the pool open with its rope, the gate padlocked, nobody home; told once, with
  nothing to read; counted once; a hand on the marks, June's door, the gate and the line; Nessa's door opens; nothing resolved.
- The first-light case after Tilly came out now asserts that the reader who has not read their account is not dealt the house again,
  their ending landed and the account waiting.
