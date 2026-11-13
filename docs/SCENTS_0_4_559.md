# What a thing smells of: scents (0.4.559)

The owner, 13 November: "Remember that pets should be able to track readers to specific vignettes too. Specific items or artifacts
should lead to specific vignettes."

Hillary could already take a scent from something a story had given up, and find a story with it: the House dealt the next story
door it could, any story the reader had not finished, and she was heard barking behind it. The thing held out to her only had to come from
some story; which one did not matter. Other companions had no scent at all. This release gives every reader's own companion a nose, and
makes what it finds depend on what it was given.

## Giving a scent

A reader holds something out to their own companion: their own cat or dog under the House's orders, or Hillary for the reader she
came to. A right-click with it in hand, as with Hillary before. What the animal takes from it:

| Held out | The scent | Read from |
|---|---|---|
| a thing someone else carries as theirs: a page torn from their journal, a keepsake or a lent thing marked with their name | that reader | the torn page's journal owner (`TornPages.ref`), the keepsake's reader (`LiteraryVignettes.OWNER`) |
| a thing a story gave up (the caregiver's note, the crayon drawing, a keepsake of the reader's own, a letter from the institute, and every other marked yield) | that story | the place it carries (`VignetteYields.of`) |
| anything else | none: the companion's wheel opens as before | |

A thing that is both, another reader's keepsake, smells of the reader.

## A story's scent

- If the story is still open to the reader, the companion takes it and leads on (the Deeper order). The doors of the hall the reader
  stands in are dealt again at once, as Hillary's always were, with that story behind one of them and the animal heard behind it; the
  companion goes to that door. A story out in the yard puts its scent on the back door onto the yard instead, and the scent goes on with
  the reader to find the story there. A scent for a story inside, taken out in the yard, waits until the reader is back in.
- If the reader is done with that story, the companion takes the scent of any story the reader has not finished, as Hillary always
  did. With none left at all, it whines and lies down.
- A story's scent is spent when its door is dealt, as before.

## A reader's scent

- The companion keeps another reader's scent for five minutes (`Scents.READER_TICKS`).
- While that reader is inside a story that is open to the one holding the scent, that story is dealt behind one of the holder's doors,
  at once for the hall they stand in, and the companion leads there. While the other reader is in the halls, the manor or outside, the
  companion waits with the scent: it can only follow them into a story, since every reader's halls are their own.
- Through that door the holder goes into the very copy the other reader is in, when it has room for them: one of the places at their
  table, their bank or their bar, a place at their camp, Tess's place beside them as a lake house diver alone, or the place beside them in a Pell house they are alone in. A story kept one reader to
  a copy gives the holder a copy of their own, as it would any reader. A story with no copies has only the one place, so they meet there.
  A holder who already has a place kept in a copy's story (an evening at the Goatman's trailer, the Pinehook roll, the Last Crossing's
  crew) goes back into that copy, scent or not.
- If that reader is not on the server, the companion sniffs at the thing and lies down.
- A reader's scent is spent when the holder goes through into the story it led to (`Scents.arrived`), or when its five minutes are up.
  Until then, each time the other reader goes into another story the companion finds their trail again and leads on.

## Hillary and the others

- Hillary takes a thing's own scent inside the House. Outside it she still sets off to find any story, as she always has.
- Any other companion takes a scent only inside the House; outside, the reader is told it cannot find that route here.
- Only a reader's own companion takes a scent, within reach, as with its orders (`CompanionOrders.canCommand`). Anything without a scent
  does what it did before: an empty hand opens the companion's wheel, food is food.

## The cases

- `LabyrinthTests.aThingFromAStoryLeadsToThatStory`: the caregiver's note smells of the floorboards, an ordinary book of nothing, another
  reader's torn page of that reader and the reader's own page of nothing. With six different deals, the note's scent leads to the
  floorboards and no other story, behind exactly one barking door, and is spent. Once the reader is done with the floorboards, the same
  note leads to another story still open, behind one barking door.
- `GoatmanTests.aSeventeenthChildIsGivenATrailerOfTheirOwn` (extended): with a place free again at the first trailer, a reader whose
  companion has the seventeenth's scent goes through into the second trailer, the seventeenth's, and the scent is spent; a reader with no
  scent goes into the first.

## What testing found

Both cases passed on their first run. With them, the companion, Hillary, pet and dealing cases (68) and the exploration suite all
passed: 237 in all.

## Decided here (any of it can move)

- Five minutes for a reader's scent; a story's scent lasts until its door is dealt, as Hillary's always did.
- A story the reader is done with falls back to any open story, so a thing never stops a companion finding one.
- Another reader's thing gives their scent rather than its story's.
- A place already kept in one copy's story outweighs the scent of a reader in another.

## Numbers

No layout change. No Witness change. New builds only.
