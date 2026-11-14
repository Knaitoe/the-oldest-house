# The ink on the Truant's hands (0.4.591)

The owner, playing on 14 November:

> "truant's hands have blue specks on the staircase"

New builds only. No line, protocol, layout or Witness change. Two painted sheets change, in his hands only: nothing else
on him, and nothing he does.

## What was there

- **The specks were his ink.** The owner's draft of him has "traces of ink on his hands", and his painting put them
  there as dots.
- **Four dots on every face of each hand,** each a single pixel of dark blue with bare skin all round it, in the same
  four places on the front, the back and both edges.
- **Both hands the same.** His model draws one painted arm for both arms, turned over, so the dots were on both hands:
  thirty-two in all.
- **A lone pixel cannot say what it is.** Nothing about the dots said ink. They read as specks on the picture, which is
  what the owner saw.

## What it is now

- **Stains, where ink gets on a hand that writes and turns pages:**
  - worked into the first fingertips, seen from his thumb's side and on the pads of the same fingers;
  - a smear down the edge of the hand that drags over a page;
  - a faint rub across two knuckles on the back of the hand.
- **Each stain is a patch,** dark in the middle with a paler rim where it thins into the skin. No pixel of ink stands
  alone.
- **The ink is close to the tone it was,** a little darker and less blue. What changed is its shape.
- **It is the same on both hands,** as before, because one painted arm serves both.
- **He is the same wherever he is drawn:** the staircase, his own rooms, the halls where he waits, and the kitchen table,
  where he is the visitor Johnny. His other face (the leer) has the same hands.

## Checks

- **The painter's own check:** both sheets are what `tools/paint_character_skins.py` paints.
- **The art record's check** (`tools/verify_character_art.py`), with a new part for him:
  - each sheet is the one recorded in `art/inventory/truant_ink_04591.json`, which names the painting it replaces;
  - the sheets' size and opacity are what they were;
  - every pixel of ink touches another, so there is no speck;
  - none of the old ink is left.
- **A count of what changed:** fifty-eight painted pixels on each sheet, all of them on the arm.
- **The game's own renderer:** the character review page he is on, before and after, with every other proof in that
  run passing.
- **The quick checks.**

Not checked:

- **Nobody has looked at him on the staircase in a played game** since the change.
- **The review page is evenly lit.** The staircase is dark and lit by a held book; how the stains read there has not
  been seen.
- **His poses.** The page shows him standing. His hands held out (on a door, over a lamp, round a page) have not each
  been looked at with the new painting.

## For the owner

- **I kept the ink and changed its shape,** because your draft of him asks for it. If you would rather his hands were
  clean, say so: it is one line in the painter.
- **Where the stains sit, and the ink's tone, are mine.**
- **Both hands are stained alike, and that can change.** His model draws one painted arm twice. The Pell house's
  people already have a left arm painted on its own (June's bandage is on one hand), and he could have the same: ink on
  his writing hand, and less or none on the other. I have not done it; say if you want it.
