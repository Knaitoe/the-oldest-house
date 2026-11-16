# Second Person: the aftermath (0.4.637)

The proposal's "Aftermath" (docs/SECOND_PERSON_PROPOSAL.md, section 5). New builds only. No layout, protocol or Witness change: 90
sources / 68 required / two kinds / three endings. The aftermath gives no credit and reopens no choice.

## What happens

Second Person joins the aftermath rooms (`Aftermath.PLACES`). A reader who has finished the year (read its account) can be dealt the
flat again, at most twice and never within their last eight crossings, like the pool and the other aftermath rooms. The flat is the
reader's own copy, so it needs no solitude.

On each arrival, `SecondPersonAfter.arrive` marks the visit as the aftermath if the year was already finished (`After637`). Then the
year's own clock stops: none of the seasons, the cold, the words on the glass, the phone or the cupboard run. Iris, Sam and the agent
are not there. The book on the bench is gone. The flat door stands open. What is left depends on the ending:

- **The door on the latch, stayed:** evening, the lamp on, the resident in the armchair. They do not look up.
- **The door on the latch, gone:** daytime, nobody, and on the mat a card with a forwarding address (`sp_fitting` application, face 3).
- **Their name on the post:** night, and the resident standing at the street window, looking down at the street.
- **Nobody home:** a grey day, nobody, and dust in the air round the reader (seen only by them). Their things are where they were
  last put. The reader can still move them, as they always could.
- **Two cups:** evening, the lamp on, the resident at the kitchen table, and the cups out with the second one full.

Forty ticks in, the visit is counted as a return (`Aftermath.count`) and a quiet line is shown once. Nothing in the flat answers a
hand (a click does nothing), except in the empty flat of nobody home.

## Art

`tools/draw_second_person_art.py`: one texture (`forwarding`, a cream card sampled 8 by 6 at the game's pixel size: a worn edge, a
stamp in the corner, a line of capitals and two lines of address) and one model (`application_3`, the card lying askew the other way
from the form). Reviewed on the hall carpet from the hall, from above and from the door, beside the signed form for scale.

## New lines (all new in this slice)

- "They are in the armchair, as they are most evenings now. They do not look up."
- "The flat is empty. On the mat there is a card: please forward, and an address a long way from here."
- "Somebody is standing at the window of the flat, looking down at the street."
- "Dust on everything. Their things are where they were left." (or "Dust on everything. Their (routine thing) is (where it was last
  put).")
- "Two cups on the kitchen table. The second one is out, the way it always is now."

## Tests

`SecondPersonTests.theFlatAfter` (batch `second_after`): the five aftermath states in turn, as the reader arrives back each time: the
empty flat and the card on the mat, the armchair, the window at night (with a click on the phone doing nothing), the empty flat of
nobody home (its things still movable), and the cups and the table; the returns counted, and no further deal once the House's limit
is reached. The Second Person namespace is 9/9.
