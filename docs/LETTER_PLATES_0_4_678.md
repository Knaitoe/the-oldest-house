# Lettered plates (0.4.678)

The owner, 17 November, of the Last Crossing:

> there are several signs in last crossing that could benefit from custom textures. i was hoping you'd notice that on your own

0.4.677 painted every short sign that was still the game's own wooden sign with words typed on it. Three kinds of notice were left, because they hold more words than a picture at sixteen pixels to the block can carry: Holloway's notes at his camp, the labels over the seven stacks of drawers in Zampanò's courtyard archive, and the road crew's notice on their trailer at the elk lot. This release gives each a plate drawn for it, with the words lettered over the plate by the game in the hand of whoever wrote them.

## The plates

| Plate | Where | What it is | The words, and in whose hand |
|---|---|---|---|
| note | Holloway's camp, three of them | a split board nailed to a stake, standing free, a nail at each corner | his three notes, in his own pencil (the hand his survey is written in) |
| label | the courtyard archive, over each of the seven stacks | a brass card holder screwed to the wall, the card behind its lip | the reader's name, typed |
| notice | the elk lot, on the crew's trailer | a typed sheet in a plastic sleeve, tacked to the siding at its top corners | ROAD CREW as a heading, then its three lines, typed |

`tools/draw_letter_plates.py` draws the three plates at sixteen pixels to the block and writes their models and states (`letter_plate`, by facing and kind). `LetterPlates` places one where there is nothing, with its words and what a hand on it reads; `LetterPlateEntity` keeps the words, set by the builder and never by a reader; `LetterPlateRenderer` letters them in the plate's hand over the part of its face the picture leaves clear. The lines share the face's height, and a line too long for the face is drawn narrower, never cut. Nothing walks into a plate.

## What a hand on each reads

Every line here is new.

- SUPPLIES. I counted them. Leave the torch. Pencil on a split board, staked in the dirt. **(new)**
- I went this way. The wall moved. It did not move when I watched. The same pencil, pressed harder. **(new)**
- YOUR LIGHT IS NOT A WAY OUT. The letters have been gone over twice. **(new)**
- Beatrice, typed on a card in a brass holder over the drawers. (And so for Leonie, Pauline, Ruth, Anne, Esther and Helen.) **(new)**
- ROAD CREW. Elk to the cut below the ridge. Sign in at gate. Typed, in a sleeve tacked to the siding. **(new)**

## What this changes for readers

Only how the notices look and what a hand on them reads. The words are the same words. Every reader sees the same plates.

## Checks

- `python3 tools/draw_letter_plates.py --check` in CI holds the plates, models, states and name to what the tool draws, and the tool's kinds to `LetterPlates.Kind`.
- The all-scenes architecture case now lets no game sign with words stand in any finished scene; the long notices it let stand in 0.4.677 are gone. It finds every lettered plate, holds a wall plate to the wall behind it, and holds each plate's words to what its builder wrote.
- The archive case reads each stack's name from its plate; the Holloway patrol case finds his note on its stake.

## Seen

Eye-level frames of every plate, from where a reader would stand: Holloway's three notes in their stakes at his camp, the seven names over the archive's drawers from the courtyard floor, and the crew's notice on the trailer, near and from the lot. Every line reads whole and inside its plate. The first frames showed Holloway's first and last lines running over the nails at the board's corners; the nails are now in the board's edge columns and his lines are lettered between them.
