# Borrasca: grown women only below (0.4.437)

The owner, on the rework of the rooms under the depot:

- "the limit on children is fine. Why not have Kelly etc aged up if the player sees them as adults? Wouldn't that fit the rule and
  the lore?"
- "could Dana's letter read as a ransom note - stitched together? Like, is there a way to make it impersonal and therefore
  ambiguous?"

This is the first of three releases that rework the lower rooms (rework 4):

1. **0.4.437 (this release):** the records and Dana's note.
2. **0.4.438:** Kelly found grown in room 6.
3. **0.4.439:** the feeding scene at the machine.

The rule is that nobody in the lower rooms is a child, and nothing sexual is shown. A child the depot takes is "kept above", and the
records say no more than that. They come down at eighteen. What is born below goes up to town, placed by the doctor: the town's
reason, set down on paper.

## The years

The friend's poster puts the reader's childhood in the early 1990s. The last night is about 2010.

| Who | Tag | Taken | Down | Now |
|---|---|---|---|---|
| Dana Pruitt | 141-A | 1980, at fifteen (her poster) | 1983 | Room 2 |
| Kelly Garrity | 263-A | 1993, at twelve (the receipt) | 1999 | Room 6 until 2004, then transfer |
| Kayla | 264-A | tonight, at nineteen (the noticeboard) | tonight | Room 7 |

Kayla was fourteen on the last night's poster. She is nineteen now, born about 1991, so she is an adult the night she is taken.

## The night book

The book keeps the same pages, rewritten:

- **The rule, written in.** "None to come down before eighteen. Births to the doctor. Placings by the doctor."
- **1964.** Unchanged.
- **1983–1985.** 141-A comes down to room 2. "Rm 2. Delivered, girl. Placed in town."
- **1987.** "Rm 2. Letter withheld." Room 4 is closed, to the yard.
- **1993.** 263-A is received at 6:40 pm and "kept above".
- **1999.** 263-A comes down to room 6. She will not eat and asks for her friend by name.
- **2001 and 2004.** Room 6 delivers a boy, then a girl. Both are placed in town. Then: "Rm 6. Transfer, D-417. Night."
- **Tonight.** "264-A. Received tonight. Down at once. Rm 7."

The property receipt for Kelly's tag says "Kept above. Not to come down before eighteen." It used to say "Transfer D-417".

## Dana's note

It is "A pasted note, not sent", signed only "Rm 2". It is made of letters cut from the catalogues they are given and pasted down.

`BorrascaTexts.pasted` builds it. Each word is one cutting, or a cutting for each letter. Every cutting keeps its own:

- type (the plain face or the uniform face);
- weight (bold or not, sometimes italic, now and then underlined);
- dark ink;
- case.

All of it is chosen the same way every time from the title. It has no hand and gives no age. The pages read:

> TELL MY MOTHER / I DID NOT / RUN OFF
> I AM UNDER / THE DEPOT / PAST THE / SECOND MARKER
> THEY CALL ME / 141 / THERE ARE / OTHERS
> WE KNOCK / ON THE WALLS / FOUR STOPPED / KNOCKING
> THE BABIES / GO UP TO TOWN / ASK THE DOCTOR / WHOSE THEY ARE
> COME PAST / THE MARKER / NOT THE SHERIFF

The last page is the stamp, typed: "WITHHELD. Not to be posted. Rm 2. 141-A. W. P."

## The account

- The night book's line adds: "Nobody came down until they were grown. What was born down there went up to town."
- The note's line now reads: "Someone down there had made a note home out of letters cut from catalogues. It was never sent."

## Tests

`BorrascaTests#theRoomsUnderTheDepotHoldWhatTheNoteWarnedOf` adds three checks:

- **The night book:** nobody comes down before eighteen, a child taken is kept above, what is born below is placed in town, and
  141-A comes down.
- **What the note says:** where she is and where the babies go, and that it was never sent. It is no longer a letter to "Mom".
- **How it is made:** its first page is pasted from at least six differently styled cuttings.

`BorrascaVisualProof` still fits every page of the night book and the note to the native lectern page.

## Numbers

Protocol 175, Witness 86/65/63 and layout 135 are unchanged. New builds only.
