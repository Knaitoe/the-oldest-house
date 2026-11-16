# A sort of thing 4: the hill, the glass table and the railway (0.4.644)

The fourth part of the plan (docs/SLEEPING_KING_PLAN.md), from the proposal (docs/SLEEPING_KING_PROPOSAL.md). Dealt from twelve doors deep since 0.4.643; no Witness, layout or protocol change here.

## The hill and the Queen

A low hill rises on the second rank's west side (`KingSite.HILL`, seven blocks across, three high). It is made of the country's own
soil and turf, so it breathes with the land. The Queen waits at its foot (`KingQueen`, stranger phase 220, `literary_king_queen`:
long hair, a gown, a crown with three points and a stone).

The first reader who comes within three blocks of her and is at least a sort of thing is taken by the hand. She runs on the spot
with them for five seconds. While she holds them they are a thing at least, rooted, and they get nowhere. Then she lets go, says
where they are, walks to the far side of the field and looks back at the peg. Each reader is taken once.

## The pegs

A peg stands at the near edge of each rank from the second to the eighth (`KingSite.peg`). A reader who stands on a peg while
somebody is looking at them (one of the dream's people, or another reader who is at least a sort of thing) is counted on that square. That is saved as one bit for each rank
(`KingCounted`), with a line in chat.

Being looked at now counts when it comes from the dream as well as from another reader. The Queen, the Guard and the brothers count,
within twenty-five degrees of where their heads point and with nothing in between. A peg the reader has already been counted on also
counts.

## The glass table

A glass-topped table (`KingBlock` `GLASS_TABLE`, translucent) stands east of the hill. A line's hand goes through its top. A sort of
thing's hand does too. Only a thing lifts anything. It gets one little bottle and one very small cake (`KingDraughtItem`, three uses
each, owner-tagged), once.

A swallow from the bottle makes the reader one step less of a thing for twenty seconds. A bite of the cake makes them one step more,
no further than too much of a thing. A thing who eats is rooted until it wears off. Leaving the country takes both away. Out of the
country they are only something to drink.

## The railway

A railway carriage stands in the field east of the road at the near edge of the third rank, its floor laid over the turf. It has panelled walls, a roof, windows, six seats facing
across, and wheels. The Guard stands in its aisle (`KingGuard`, stranger phase 221, `literary_king_guard`: a uniform, a moustache
and a paper cap).

A reader who sits in a seat and has not ridden is looked at through three glasses: "Ticket", then "Travelling the wrong way", then
the third line. While a glass is up, a reader who gets up is put back in the seat. Getting up between looks ends the ride, and
nothing is counted. After the third look the train whistles, the screen fades, and the reader is set down on the road beyond the
brook at the near edge of the fourth square (`KingSite.RIDE_OUT`), saved as `KingRode`.

Two new sounds: `king.train` (steam) and `king.whistle`.

## Checked

- `KingTests.theQueenThePegAndTheGlassTable`:
  - the Queen takes the hand of the reader who comes up to her, roots them and makes them a thing;
  - she lets go, looks from the far side, and the reader has run;
  - standing on the peg under her look counts them;
  - a line lifts nothing from the table, and a thing lifts a bottle and a cake once;
  - the bottle makes the reader a step less and the cake a step more, and a thing who eats is rooted;
  - leaving takes both away.
- `KingTests.theGuardAndTheTrain`:
  - sitting down starts his looks;
  - under his glass a reader who stands is put back;
  - the ride ends on the road beyond the brook.
- The earlier five cases still pass.
- Look review: the Queen (waiting, running, pointing) and the Guard (standing, looking) on the model.

## New lines (for the owner)

Spoken (captions over the speaker):
- The Queen, running: "Faster! Faster! Don't try to talk."
- The Queen, running: "Here, you see, it takes all the running you can do to keep in the same place."
- The Queen, letting go: "Here you are what you are counted as. Stand on the peg."
- The Guard, first glass: "Ticket. Every thing on this train has a ticket."
- The Guard, second glass: "Travelling the wrong way."
- The Guard, third glass: "A thing ought to know which way it is going, even if it doesn't know its own name."

Cues:
- She lets go: "She let go, and the hands in front of you were drawn flat."
- A hand on the Queen: "She is too busy to be held by the hand just now."
- A hand on the Guard: "He looks through his glass at the hand, and then at you, and does not take it."
- Counted on a peg: "Counted. Somebody is taking notice."
- A peg: "A peg, driven in at the near edge of the square."
- The table, not a thing: "The hand goes down through the glass top and finds nothing to lift."
- The table, a thing: "A little bottle and a very small cake, one for each hand."
- The table again: "The table has nothing more on it for you."
- The bottle: "Less, for a while."
- The cake: "More, for a while."
- Either, outside the country: "Nothing happens. It is only something to drink, out here."
- Getting up between looks: "The Guard lowers his glass, and the seat is only a seat."
- The ride's end: "The train took the brook at a jump. The road is under your feet on the far side."
- A wheel: "A carriage wheel, standing in the grass."

Item names: "A Little Bottle", "A Very Small Cake".

Account sentences (one each, as the reader went by them):
- "A queen in a long gown took me by the hand on the second square and ran with me as fast as we could go, and we were in the same place at the end of it. Then she let go and told me to stand on the peg."
- "I stood on one peg, and it counted me." / "I stood on two (… seven) pegs, and each of them counted me."
- "There was a bottle and a cake on a glass table. I had some of each." / "…and I drank from it." / "…and I ate some of it."
- "I sat in a railway carriage in a field while a guard looked at me through three glasses, and the train took the brook at a jump."
