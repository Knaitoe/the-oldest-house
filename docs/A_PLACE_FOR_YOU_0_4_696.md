# A Place for You: the house afterwards (0.4.696)

The twelfth slice, item 8 of `docs/A_PLACE_FOR_YOU_PLAN.md`: what a reader finds on coming back after the account has been read. The
house is as their ending left it, each of them says one thing a visit, and nothing can be decided again.

Code: `PlaceForYouAfter` (new); hooks in `PlaceForYou` (arrival, where people stand, the evening's clock, talking). No new art.

## What happens

Before this release a reader who came back after the account found the house as it was and the one cue "The house across the road.
You know which window is the kitchen's.", and nobody answered. Now the first visit after the one the account was read on, and every
one after it, is counted, and:

- **The house opened (E1).** The front door stands open with the hall chair against it. Kit and Alex are gone. Whoever stayed is at
  the table: Mara and Ren, unless they went to the gate with Kit, and then the house is empty and the kitchen light is off. If the door
  was shut on the way out last time, it is propped again while the reader is away from it and not looking.
- **The place kept (E2).** The evening is laid again, and each of them says word for word what they said at that table.
- **The past let in (E3).** Theo is at the table with Mara. Nobody knows the reader.
- **The road (E4).** The household goes on without the reader. Mara is at the sink with her back to the door; a hand on her gets
  only that she doesn't turn round. The reader's chair goes from the table while the reader isn't looking, and the front door shuts
  behind them when they are outside.

Each of them answers one thing a visit, over their head; after that, nothing more until the next visit. There is no source, no Witness
and no keepsake here: the record changes only by the visit count and who has spoken this visit.

## With more than one reader

Each reader's evening is a copy of its own, and so is each reader's aftermath: their own ending, their own visit count, their own one
thing from each of them. Two readers who ended differently come back to different houses.

## New lines

Every line below is new in this release unless marked otherwise.

**Cues**

- "The front door stands open, the hall chair against it." *(new, flagged)*
- "The front door stands open. The kitchen light is off." *(new, flagged)*
- "Your place is laid. It was laid before you reached the door." *(new, flagged)*
- "Keys under the hall glass. Two voices from the kitchen." *(new, flagged)*
- "Mara doesn't turn round from the sink." *(new, flagged)*
- The road keeps "The house across the road. You know which window is the kitchen's." *(unchanged)*

**The house opened**

- Mara: "Sit anywhere you like. I mean it now." *(new, flagged)*
- Ren: "She sleeps most nights now. I leave the radio on low." *(new, flagged)*

**The place kept** (the kept table's own lines, said again)

- Mara: "Sit down. It's still warm." / Ren: "Told you she'd make too much." / Alex: "Drink that before she fills it again." / Kit:
  "I'm still going." *(unchanged lines, said again)*

**The past let in**

- Mara: "Sorry. You've been here before, haven't you? I'm no good with faces." *(new, flagged)*
- Theo: "Evening. You'll want the gate. It sticks." *(new, flagged)*

**The road**

- Ren: "She laid a place anyway. Then she put it away." *(new, flagged)*
- Alex: "You took the road. Good. One of us should have." *(new, flagged)*
- Kit: "You went before I did." *(new, flagged)*

## Cases

- `PlaceForYouTests#theHouseAfterwards` (new): the road taken and the account read; not yet a visit after it on the visit it was read
  on; the next visit counted; the reader's chair gone from the table unseen; Mara at the sink; a hand on her is a cue, not the panel;
  Ren says one thing, then nothing more; the next visit counted and cleared; nothing else in the record changed. The other endings'
  cues and lines from the record.

## Results

Locally: `theHouseAfterwards` 1 of 1; with it, the four ending cases (`aGuestOpensTheHouse`, `somebodyAtTheDoor`, `keepingAPlace`,
`invitingThePast`), `aReaderTheHouseKnowsTakesTheRoad` and the cast and together conduct checks for A Place for You, 7 of 7. The first
run of the new case failed on the case itself: it never answered the welcome, so the panel was still open when Mara was asked.
