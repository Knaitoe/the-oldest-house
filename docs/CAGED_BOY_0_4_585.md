# The boy in the cell, painted again (0.4.585)

The owner, 11 November, after the note that rebuilt the Minotaur:

> "improve the texture of the boy before he transforms, too"

And on 14 November, on the previews of this painting:

> "actually your rendition is great, ignore my earlier thoughts about him mirroring the player or being featureless"

New builds only. No line, protocol, layout or Witness change. He is the same entity with the same UUID, the same name
("The boy"), the same size and the same body for the game to collide with; what happens at his cell is as it was.

## What was there

- **One row of the cast's painter.** A buttoned grey shirt with long sleeves, dark trousers and dark shoes, messy brown
  hair, and the face the painter gives a grown man, at a child's size.
- **Nothing of his own.** Nothing about him said where he was or how long he had been there, and nothing tied him to
  what he becomes.

## What he is now

He is painted by himself, from what he becomes (`tools/draw_minotaur.py`, 0.4.584). Nothing on him says what that is:
no horn, no hoof, nothing red.

- **His eyes are the Minotaur's.** 0.4.199 settled those with the owner ("Don't give it red eyes. It's not necessarily a
  monster." and "Try to make them look human in the way a cows eyes can look human"). His are the same eyes in the same
  tones: large and dark, white only at the outer corner, the brown lighter along its top.
  - On a child's face the lid over each is a soft brown line and not the bull's black one.
  - His brows are thin and set a little apart from his eyes, their inner ends a pixel higher than their outer.
  - There is shadow under his eyes, and a smear of dirt on one cheek and on his chin.
- **His hair is its dark mane,** in the same tones. It has never been cut: down over his ears and to his collar, short
  over his brows, one lock down between them, longer at the temples. Here and there a lock catches the light as it turns
  over.
- **He wears a grown person's shirt as a tunic.** It is the cloth of the Minotaur's rag, two tones lighter: the same
  cloth with less dirt in it.
  - The collar hangs wide of his neck, and his throat shows in it.
  - It is tied at his waist with the Minotaur's rope, in the same tones, knotted over the same hip (his left), both
    ends hanging.
  - Below the rope it hangs as a skirt, lower in front and behind than at the sides, where it is split, and worn to
    threads along its edge.
  - His right sleeve is gone above the elbow. His left hangs to the elbow and is split up the front.
  - It has been mended twice with what cloth there was: a square of washed-out blue over his heart, a piece of sacking
    across one shoulder blade.
- **Trousers worn off below the knee,** one leg longer than the other, out at one knee.
- **Bare feet,** the toes parted, the soles black with the floor. Dirt at his knuckles, his elbows and his shins.
- **No wound.** He is thin and he is dirty. Nothing on him is a bruise, a cut or a mark of being held.

### How he is drawn

- **On the cast's grid:** the standard 64 by 64 player layout at two pixels to a model unit, as every other person in
  the House is.
- **On the slim player model.** Its arms are three pixels wide where the model he had has four. That is the one change
  to his shape. His height, his scale (seven tenths of a grown figure) and how he moves are as they were.
- **With the model's second layer.** It stands a quarter of a pixel off the first, half a pixel round the head, and it
  carries what has thickness:
  - the mass of his hair, and the locks it ends in;
  - the shirt's loose sleeves and its skirt;
  - the rope, its knot and its ends;
  - the blue patch.

  Every face of the first layer is painted under it, so nothing shows through where the second layer ends in a ragged
  edge.

## How it is kept

- **`tools/draw_caged_boy.py`** paints him. The cast's painter (`tools/paint_character_skins.py`) calls it for his
  sheet, as it calls the bent-neck woman's painter for hers, so its `--check` in the workflow's quick gate covers him.
- **His approval is recorded** as the cast's others are: `art/inventory/caged_boy_04585.json` supersedes only his own
  0.4.100 painting, and `tools/verify_character_art.py` follows it, for the sources and for the packaged JAR.
  - That tool now also checks that nothing of him is painted outside the slim model's two layers.
- **The game's art catalogue** (`character_art.json`) draws him on the slim model in the character art sheets.
- **The first boy's tool** (`tools/generate_prisoner_skin.py`, 0.4.30, the faceless one) is marked as superseded and
  kept for the record. Running it would write over this painting, and the art checks would then fail.

## Checks

- **The painter's check** and **the art verifier,** with the new sheet: only his sheet changed among the 351.
- **The quick checks,** and a compile.
- **The game's own renderer,** in the native home proof, where he stands beside the tree's tendril: the slim model and
  both layers draw with his sheet. He is small in that frame.
- **A preview of the model outside the game,** from every side and close on his face. These are what the owner saw.

Not checked:

- **The character art sheets** (front and back at a larger size) were not run here; the workflow's build job runs them.
- **Nobody has stood at his bars in a played game** since the change.
- **No server case was run,** because nothing on the server changed: the painting, his renderer's model and two proofs
  are all on the client.

## Caught while painting

- **The first face was a sullen one.** A black lid three pixels wide sat on each eye under a heavy brow, and a light
  drawn in the brown read as white on both sides of it, so he looked sideways. The lid is a soft brown, the brows are
  thin and set apart, and the brown is lighter along its top instead.
- **The first hair was noise:** a lit stroke in nearly every third pixel. It is sparse now, a lock here and there.
- **On the model's ordinary arms** his bare arms were as thick as his legs. He has the slim model's.
- **The first tear at his knee** read as a flower, the first patch as a grille of stitches. The tear slants; a patch
  has a stitch at each corner.
- **The rope was knotted over his right hip,** and the Minotaur's is over its left. His is over his left.

## For the owner

- **He does not mirror the reader and he has a face,** as you said on the 14th.
- **The slim arms are a choice.** Say if he should have the model's ordinary arms back: it is one word in his renderer
  and a repaint of the two arms.
- **The blue patch** is the only cool colour on him. It can be sacking like the one on his back.
- **What ties him to the Minotaur is quiet on purpose:** the eyes, the hair, the cloth and the rope. A reader who goes
  back and looks will find them; nothing gives it away the first time.
- **The great staircase** (the next note) is separate and not in this.
