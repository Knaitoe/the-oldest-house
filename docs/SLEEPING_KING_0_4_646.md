# A sort of thing 6: the court of cards and the Cat (0.4.646)

The sixth part of the plan (docs/SLEEPING_KING_PLAN.md), from the proposal (docs/SLEEPING_KING_PROPOSAL.md). No Witness, layout or
protocol change here.

## The court

The court sits on the sixth square's road (`KingCourt`). The herald waits at the far bank of the drawn bridge (`KingSite.HERALD`). The
King and Queen of Hearts stand at the far end of the court (`COURT_KING`, `COURT_QUEEN`), with a juror row on either side. The pack's
guard stands shoulder to shoulder across the middle file just short of the brook (`GUARD_ROW`), faces to the court. The jurors and the
guard are cards (`KingCardBlock`, `king_card`): a card a block and a half tall, flat, with a face and the pack's back. A standing card
bears a thing and nothing less: a thing cannot get between two of them, and anything less goes between them edge-on. A fallen card lies
flat in the grass and bears nothing.

The brook past the sixth square walls off a thing everywhere but the road's footbridge, so the guard across the road holds the court
shut to a thing.

## The summons and the sentence

The first time a reader sets foot on the sixth square while the court stands, the herald reads their name off his scroll
(`KingSummoned`). From then until they leave the court, they are a thing for as long as the court attends to them (`KingCourt.holding`,
in `KingBreath`). Coming up to the dock, the Queen of Hearts sentences them first, and the King says what it was for will come
afterwards (`KingSentenced`). Nothing is carried out. A held reader who gets past the guard anyway is set back in the dock
(`KingSite.DOCK`).

## Three ways out

Which way a reader left is kept as `KingCourtWay`:

1. **The Cat's way.** The Cat's head is in the tree on the lawn in the east file (`KingSite.CAT_TREE`). A hand on it opens the panel,
   and the Cat offers to go and be looked at instead (`KingCalledCat`). For thirty seconds its head is over the court, and the court
   looks up at it and argues whether a head with no body can be taken off. Nobody is held while they do. A reader is then what the land
   makes of them, and as a sort of thing goes out between the guard. (Run 1739: with two readers the one speaking in the argument
   kept looking up at the Cat, not at the reader ten blocks off. Whoever is speaking in it now turns to the nearest reader within
   sixteen blocks for the line; the others keep looking up at the Cat.)
2. **The bottle's way.** The hold makes a reader a thing, and a swallow from the bottle takes them a step under that. A sort of thing
   goes out between the guard without the Cat.
3. **The cards' way.** A bite of the cake takes a held reader to too much of a thing. Talking to the court then offers one more answer,
   and saying it brings the pack up and down as cards on the grass. The King, Queen and herald come down as their own cards where they
   stood. The King stirs once, a long way off (`KingSleep.stir`). After that nobody is held. A reader who comes later finds a pack of
   cards on the grass and is named by nobody (way 4).

A fresh dream stands the pack up again, and so does a dream that has forgotten it came down, such as after a restart.

## The people

- The King of Hearts: a crown, a full beard, and a coat with a card down its front, large heart in the middle. Hands together at the
  waist; one raised as he speaks.
- The Queen of Hearts: a crown, long hair, and a gown with the same card. Hands together; she points at the dock to sentence, and up at
  the Cat when it is over them.
- The herald: a pale tabard with a row of hearts. He holds his scroll up in both hands to read.
- The Cat: only a head, striped, with round pale eyes with slit pupils, a grin of teeth nearly the width of the head, and ears (the
  cast model's `ears`, shown only for the Cat). It bobs a little in the air, looks at whoever is nearest, and looks down over the court
  when it is there.

All four are painted by the shared painter at the cast's scale (stranger phases 223 to 226).

## The cards

The card faces are drawn one texel to one model unit by `tools/draw_king_art.py`, eleven texels by twenty-four. Each card has the pale
face, a darker edge and cut corners. The two, three, four, five, six and eight of hearts and spades carry their pips and an index at
two corners. The knave, queen and king each show a figure from the waist, crowned, mirrored about the middle. The back is a lattice.

## Sound

`king.cards`: the pack thrown up in a riffle of edges, then coming down over two seconds, each card a flutter and a slap on grass
(`tools/generate_king_audio.py`).

## Checked

- `KingTests.theCourtOfCardsAndTheCat`:
  - the court, herald and Cat are made;
  - a reader on the square is named and held a thing;
  - the guard bears a thing and lets a sort of thing through;
  - past the guard by no way, a reader is set back in the dock;
  - the bottle's way, the Cat's way (the Cat over the court, the court looking up), and the cards' way (guard fallen, court gone, the
    King of Hearts a card where he stood, one stir);
  - a late reader is not named and finds the cards;
  - each account says which way;
  - a fresh dream stands the pack up again.
- The earlier eight cases still pass.
- Look review: the court, the herald and the Cat on the model; the cards drawn whole.

## New lines (for the owner)

Spoken (captions over the speaker; "the court" is the House's name for the King and Queen of Hearts):
- The herald: "[the reader's name]!"
- The herald: "Come before the court, and stand in the dock, and be looked at."
- The herald, to a hand: "Your name is on the scroll. That is all the scroll says."
- The court (King), to a hand: "The court is sitting. It has been sitting a long while."
- The court (Queen): "Sentence first."
- The court (King): "What it was for, we'll find out afterwards. We have never got as far as afterwards."
- The court (King), asked what for: "That comes afterwards. Nothing has ever been carried out."
- The court (King), over the Cat: "A head can't be taken off unless there's something to take it off."
- The court (Queen), over the Cat: "Anything that has a head can be made to do without it."
- The herald, over the Cat: "It's only grinning. It never does anything else."
- The court (King), over the Cat: "Then it must be sentenced. Sentence first."
- The Cat: "They can't take a head off a line. They have tried it on me." (the proposal's)
- The Cat: "Shall I go and be looked at, instead of you? They can only look at one thing at a time."
- The Cat: "I'll be here. Some of me, anyway."
- The Cat, over the court: "They're looking at me now. Go on, while they are."
- The Cat, near the end: "They'll stop looking at me soon. I'd be on the far side by then."

The reader's answers:
- To the court: "What is the sentence for?"
- To the court, as too much of a thing: "You're nothing but a pack of cards!" (Carroll's, as the proposal names it)
- To the Cat: "Go and be looked at." / "Not yet."

Cues:
- Named: "Your name, read off a scroll. On this square you are a thing for as long as the court attends to you."
- Set back: "The guard closed up, and the court set you back in the dock. It has not finished looking at you."
- Out by the Cat or the bottle: "Out between two of the guard, edge-on."
- A hand on the court after leaving: "The court has done with you."
- The pack: "The pack went up, and came down as cards on the grass. A long way off, the King stirred."
- The Cat over the court: "A head is over the court, grinning, and every face in it has turned up to look."
- A standing card: "One of the court's pack, standing on its edge."
- A fallen card: "A card, lying flat in the grass."

Block name: "One of the Pack". Subtitle: "A pack of cards flies up and falls".

Account sentences:
- "A herald on the sixth square read my name off a scroll, and a court of cards sentenced me first and left it there."
- "A head with no cat to it came over the court, and while they argued whether it could be taken off, I went out between the guard
  edge-on."
- "I drank, and went out between the guard edge-on while the court was still looking at where I had been."
- "I told the court what it was, and it was a pack of cards on the grass. He stirred, a long way off."
- "The court was a pack of cards on the grass by the time I came to it."
