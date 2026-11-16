# A Little Longer 0.4.632: personhood

The twelfth and last slice of the plan (docs/A_LITTLE_LONGER_PLAN.md): "SELF address, HAIR's one callback, HOME's first question, AGE
casting; neutral for every unread state." From the proposal's "Casting and personhood". Code: `labyrinth/LongerSelf.java`.

## What changes

Only a **settled** read changes anything (HouseRead, with the operator's switches). Unknown, tentative, uncertain and switched-off reads
get every line exactly as it was written before this slice.

- **SELF: address.** Rowan's first request at the bench ("Could you hold that side a second?") ends with "man", "miss" or "friend" for a
  settled masculine, feminine or androgynous read. UNSEEN and every other state keep the line bare. It decides nothing about romance or
  who leads.
- **HAIR: one practical callback.** Back inside from the rain on the roof, before Rowan's "don't go yet", Rowan takes a dry tea towel off
  the rail and holds it out, once. A wrapped read gets its own words. It happens once in the evening and never again; nothing else
  follows from it. The account mentions it only if it happened.
- **HOME: the first question about staying.** Rowan's "You could stay for it." (chapter 2) is followed by one more sentence for a settled
  haven, danger or runaway read. Rowan is still Rowan: the question, the replies and what follows are the same.
- **DRAWN_TO** was already the casting (LongerRowan.castFor, 0.4.622): the settled read picks the body, both and the rest pick by the
  reader's id, and it is fixed at the first meeting.
- **AGE: not built.** There is one adult cast per body, so every AGE read meets the same adult Rowan; an older variant would need new
  painted skins. Nothing in the story uses AGE, and it gives the reader no history.
- **Kept**: `Cloth0632`.

## New lines, flagged for the owner

- "Could you hold that side a second, man?" / "...miss?" / "...friend?"
- "You could stay for it. Before you head back." (HAVEN)
- "You could stay for it. The door's right there whenever you want it." (DANGER)
- "You could stay for it. You don't have to be anywhere." (RUNAWAY)
- Cue: "Rowan takes a dry tea towel off the rail and holds it out."
- "Here. The rain'll have got in your hair." / "Here. In case the rain got through." (wrapped)
- Account: "Back inside, Rowan handed me a dry tea towel for the rain."

## Tests

`LongerTests.personhoodOnlyFromSettledReads`: unread and tentative reads give the lines as written; settled SELF, HOME and HAIR reads
give the address, the staying question and the towel (once); the account has the towel only if it was handed over. The longer namespace
passes 22 of 22.

## Not covered

- No case walks a reader with settled reads through the bench, the kettle and the return in play; the lines are checked where they
  are made.
- The AGE variant is not built (see above).

With this slice every part of the plan is built. The House's other readers (two-person scenes, the multiplayer audit) need nothing new:
every evening is a private copy.
