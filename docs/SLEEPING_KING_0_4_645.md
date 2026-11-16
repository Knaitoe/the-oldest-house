# A sort of thing 5: "Who are you?", the mushroom and the drawn bridge (0.4.645)

The fifth part of the plan (docs/SLEEPING_KING_PLAN.md), from the proposal (docs/SLEEPING_KING_PROPOSAL.md). Dealt from twelve doors
deep since 0.4.643. No Witness, layout or protocol change here.

## The mushroom and the Caterpillar

A mushroom stands in the west file at the far edge of the fifth square (`KingSite.MUSHROOM`). It has a stalk and, a block up, a broad
flat cap with gills under it (`KingBlock` `STALK` and `CAP`). The Caterpillar sits on the cap (`KingCaterpillar`, stranger phase 222,
`literary_king_caterpillar`). It is an old man in a long coat ringed round in segments, with a full beard; the coat comes down to the cap, and no legs show under it. It sits with its arms
folded, swaying a little, and raises a hand when it speaks. It looks down at the nearest reader within twelve blocks, so its look is
notice (`KingBreath`): a reader at the mushroom is never less than a sort of thing.

A hand on the Caterpillar opens the panel. It asks "Who are you?". The two answers offered are what that reader is at that moment, and
on the out-breath every other word drops out of them. Either answer gets the same reply: what the mushroom does, and what the bridge
will bear. Being asked is saved as `KingAsked`.

## The mushroom

The side of the cap a reader stands on is the side they break from: the west side is more, the east side is less. A piece fills that
side's cake or bottle again, or gives one if the reader has none. Going back fills it again. The count is saved as `KingMushroom`.

## The drawn bridge

The footbridge over the brook past the fifth square is only drawn (`KingDrawnBridgeBlock`, the footbridge's ruled picture). A drawing
will not bear a thing: a thing goes through it into the brook and is set back on the near bank (`KingSite.BANK`), unhurt
(`KingFellThrough`). Anything less walks over it, and that is saved as `KingDrawnCrossed`. So the way over is to drink first, or to
come to it as a line.

## Checked

- `KingTests.theCaterpillarTheMushroomAndTheDrawnBridge`:
  - the Caterpillar sits on the cap;
  - its question's answers are whole on the in-breath, and lose words on the out-breath;
  - being asked is saved;
  - the west side gives a cake, going back fills it again without a second, and the east side gives a bottle;
  - the road crosses on the drawn bridge, which has no floor for a thing and a floor for a sort of thing;
  - a thing in the brook is set back on the bank;
  - less than a thing, standing on it, has crossed.
- The earlier seven cases still pass.
- Look review: the Caterpillar seated on the cap, sitting and speaking, on the model.

## New lines (for the owner)

Spoken (captions over the speaker; "Who are you?" and the reply about not being oneself are Carroll's):
- The Caterpillar: "Who are you?"
- The Caterpillar: "You! You were more of a thing at the hill, and you will be less of one at the bridge. Which is it?"
- The Caterpillar: "One side of this will make you more, and the other side will make you less."
- The Caterpillar: "The bridge is only drawn. A drawing bears a drawing."

The reader's answers (one of the first four, by what they are, and always the fifth):
- "Too much of a thing, I think, just now."
- "A thing, at the moment. I was less of one on the way here."
- "A sort of thing. I was a thing a little while ago."
- "Hardly anything. I have been several things since I came in."
- "I can't explain myself, because I'm not myself, you see."

Cues:
- The stalk: "A mushroom's stalk, as tall as a table."
- A piece from the west side: "A piece from this side. More."
- A piece from the east side: "A piece from that side. Less."
- Set back from the brook: "The bridge was only drawn, and would not bear a thing. The bank is under your feet again."
- Over it: "Over the drawn bridge, as a drawing."

Block name: "Drawn Bridge".

Account sentences:
- "Somebody on a mushroom asked me who I was, and I could only tell it what I was at the time."
- "The bridge past the fifth square was only drawn, and it bore me because I was less."
- "The bridge past the fifth square was only drawn. It let me through once, and bore me when I was less."
- "The bridge past the fifth square was only drawn, and it would not bear me."
