# A Place for You: Mara's room (0.4.689)

The fifth slice (item 4 of `docs/A_PLACE_FOR_YOU_PLAN.md`), the proposal's N6A, what keeping somebody costs:

> Mara invites the reader to help fold something or straighten a drawer. A photograph of Theo is already there; snooping is not the
> only access to the truth. She explains the last ordinary evening before his death and how she first noticed the house could repeat
> an unfinished moment. She has not been bringing him back. She has been preventing the evening from reaching the part where he is
> absent. The player can listen without promising to stay, promise company, ask what Kit wants, accuse her of manipulating the
> household, or ask why she thinks keeping them is different from losing them.

Code: `PlaceForYouRoom` (new), hooks in `PlaceForYou` (the sink chapter, her door, the basket and the drawer), `PlaceForYouSite` (the
basket, the chest of drawers, her door), two new kinds of the fitting block, the art in `tools/draw_place_for_you_art.py` and the
account in `PlaceForYouTexts`.

## What happens

- **Upstairs first.** At the sink, Mara asks the reader to help her with the sheets upstairs. "Can we talk here?" keeps the sink
  conversation as it was in slice 1.
- **Her door.** Shut all evening until now, it opens for the reader. Mara goes up ahead.
- **The washing.** A basket at the foot of her bed. Three folds with her: a sheet walked in between them; a pillowcase she does in the
  air; and at the bottom, a work shirt she takes from the reader's hands and folds herself.
- **The drawer.** She opens the top drawer of her chest of drawers to put them away. A photograph is already in it, face up: the two
  of them on the porch steps.
- **What she tells.** The day before; him going out after supper and saying he'd be back for supper; making too much every night
  since; the kettle at the same minute two nights running, which she let happen and then helped; that she has not been bringing him
  back but keeping the evening from reaching the part where he is gone.
- **What the reader says back.** Five answers: listen; promise to sit with her tonight; ask what Kit wants; say she is keeping all of
  them; ask how keeping them is different from losing them. Each gets its own answer, and what was said is kept for later slices and
  for the account, word for word where it was a promise.
- **After.** She lays the shirt in over the photograph and shuts the drawer; supper is laid again, as after the sink. Her door shuts
  again once nobody is looking at it.

## With more than one reader

Each reader's evening is a copy of its own, and so is Mara's room in it.

## Art

Drawn at sixteen pixels by `tools/draw_place_for_you_art.py`:

- The laundry basket (`basket`, three stages: full, half folded, empty), in a cane weave over upright stakes, with folded linen and the
  work shirt from Theo's photograph.
- Mara's chest of drawers (`dresser`, two stages): three drawers with brass knobs; and the top drawer pulled out into the room, its
  paper liner, and the photograph lying in it (Mara and Theo side by side on the porch steps, the house behind them, a white border).
- The room itself, furnished from what the house already has: her bed with its head to the front wall, a bedside table beside it, the
  floral armchair and a reading lamp by it, so the room is lit before anybody is in it.

## New lines

Every line below is new in this release.

**Cues**

- "Mara goes up ahead of you. Her door is open." *(new, flagged)*
- "A basket of washing at the foot of the bed." *(new, flagged)*
- "You take two corners of a sheet and Mara takes the other two, and you walk them in together." *(new, flagged)*
- "A pillowcase. Mara does hers in the air without looking." *(new, flagged)*
- "At the bottom, a work shirt. Mara takes it from you and folds it herself." *(new, flagged)*
- "Mara opens the top drawer to put them away. There's a photograph in it already, face up." *(new, flagged)*
- "Mara and a man in a work shirt, side by side on the porch steps." *(new, flagged)*
- "She lays the shirt in on top of the photograph and shuts the drawer." *(new, flagged)*
- "Mara's room." / "The basket, empty." / "A basket of washing." / "Mara's chest of drawers." *(new, flagged)*

**The reader**

- "All right." / "Can we talk here?" *(new, flagged)*
- "I'm listening." / "I'll sit with you tonight." / "What does Kit want?" *(slice 1's, at the sink)*
- "You're keeping all of them here." / "How is keeping them different from losing them?" *(new, flagged)*

**Mara**

- "Would you help me with something upstairs first? It's only sheets. They're easier with two." *(new, flagged)*
- "Thank you. Mind the third stair." / "You take that end." / "Up here." / "Take the corners." *(new, flagged)*
- "That's the last one of the two of us. The day before." *(new, flagged)*
- "He went out after supper. He said he'd be back for supper the next night, and I said I'd make too much, like always." *(new,
  flagged)*
- "I did make too much. I've made too much every night since." *(new, flagged)*
- "The first time the kettle went at the same minute two nights running, I let it. The next time, I helped it." *(new, flagged)*
- "I haven't been bringing him back. I've been keeping the evening from getting to the part where he isn't here." *(new, flagged)*
- "I don't want another conversation in an empty kitchen." *(the proposal's line, as at the sink)*
- Answers: "Thank you. That's all, really. That's all it is." / "Tonight. All right. I'll hold you to that." (slice 1's) / "Kit wants
  to go. Kit's wanted to go since the spring." then "...I know what Kit wants." / "I'm keeping the evening. They're in it." then "That
  isn't fair. It's also true." / "I can see them. That's the difference. It's the only one I've got." *(new, flagged except where
  marked)*

**The others, while she has the reader upstairs**

- Kit: "She's got you folding? She never asks me." Alex or Ren: "Go on up. She's waiting." *(new, flagged)*

**The account** (in place of the sink's paragraph, when the reader went upstairs)

- "Mara asked me upstairs to help her fold the washing. We did the sheets between us. At the bottom of the basket was a work shirt,
  and she took it from me and folded it herself." *(new, flagged)*
- "In her top drawer there was already a photograph of her and her brother, Theo, on the porch steps, the day before. He had gone out
  after supper and said he'd be back for supper the next night, she said, and she had made too much every night since." *(new,
  flagged)*
- "She hadn't been bringing him back. She had been keeping the evening from getting to the part where he wasn't here. She didn't want
  another conversation in an empty kitchen." *(new, flagged)*
- "I told her she was keeping all of them. She said she was keeping the evening, and they were in it, and that it wasn't fair and it
  was true." / "I asked how keeping them was different from losing them. She said she could see them, and it was the only difference
  she had." *(new, flagged)*

## Cases

`PlaceForYouTests#marasRoom` (new): her door shut until she asks; asked upstairs; her door open; in her room; three folds and the
basket empty; the top drawer out with the photograph; what she tells, with five answers; the drawer shut, what was said kept, supper
laid again; the account's paragraph in place of the sink's; and her door shut again while nobody looks.

## Results

Locally: the seven `PlaceForYouTests` cases (two of them now answer Mara's question at the sink with "Can we talk here?" before
the sink conversation they test) and the cast and together conduct checks for A Place for You, 9 of 9. Eye-level frames of the room
from the door, the chest of drawers, the basket, the bed and the armchair, and the landing.
