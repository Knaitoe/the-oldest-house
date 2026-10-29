# The layered man and the bent-neck woman — 0.4.171

Two of the uncanny rooms, from the owner's backlog:

- **The Unbuttoned Basement.** The layered man needed tearing sounds, more dialogue and a grosser mouth and nose.
- **The Stooping Giant.** The bent-neck woman's texture needed improving.

While the woman was being reworked, the owner added:

> "she should have empty eyes, too. like the pale eyes of the dead"

> "the bend should be more subtle. like, the class bent-neck woman of many horror stories" (with a still of the Bent-Neck Lady)

> "like bending through a doorway" (with a still of a tall figure ducking through a door)

> "maybe shorten the height of her head so you've got more room to display her neck, and it's more in proportion with the rest of her body?"

> "and make her body thicker, if possible, so it's not the same width as her neck lol" / "the depth is what i'd meant! The width is fine."

> "Make sure she has a visible, bent neck. She should be barefoot too"

## The layered man

- **Tearing.** Every cut, the reader's cleaver or the worker's nails, already played one wet tear as the flesh broke. Each cut now also plays a second take from the same six, at the cut and at a different pitch, so the two layer into a longer, fuller rip.
- **The worker's story.** The worker tells the ledger's story a piece at a time. Six lines are new:
  - "From the collarbones down. While it's still wet, before it sets."
  - "Stop for five minutes and he puts three more coats on."
  - "Marcus had the first shift. He's in there now. By the hip."
  - "Don't look for Marcus's face. You'll find it."
  - "My blade broke in a collarbone. I've been using my nails."
  - "If he gets the bottom step, there's no room left to breathe down here."
- **What the worker says first.** He answers to what is happening, in this order:
  1. once the man is gone: quiet;
  2. before the cleaver is taken: where it is;
  3. while the key is out of the body and the reader is not carrying it: "The key. It's out. Take it before he closes over it.";
  4. once the mouth is cut: "Keep cutting";
  5. once the growth reaches the stairs: "He's on the steps. We're running out of room.";
  6. after the reader has peeled: every third line urges them deeper;
  7. otherwise: the story.
- **The man.** He says six new things between the old ones:
  - "There's someone else in here with me. He won't stop crying."
  - "It's so warm. Why is it so warm."
  - "Every time you cut, it comes back thicker. I'm sorry."
  - "I had a name. It's under all this somewhere."
  - "Not with your nails. Not like him. Use the blade."
  - "My mouth isn't mine any more."
- **The mouth.** It is still slack and open, but swollen and lopsided:
  - a fold of skin sags over thick, cracked lips;
  - the teeth are crooked, some rotten and some missing, with a smaller row behind;
  - a tongue lies at the bottom, and spit hangs from the lower lip.
- **The nose.** Bulbous and shiny, with broken veins over the bridge, a crusted scab, a raw tip, and flared wet nostrils, one of them dripping.

The art is drawn in code (`tools/draw_uncanny_cellar.py`). Nothing about how the room plays changes: growth, the key, the cut mouth, eradication and the reset are as they were.

## The bent-neck woman

### Her painting

She has her own painter, `tools/draw_tunnel_woman.py`. It replaces the shared painter's generic injured woman, with her bruises and wound. The look takes her text at its word: "Her neck isn't broken. She isn't grieving."

- **Hair.** Long, wet and black, with a sheen. It hangs over the face's edges, lies over her shoulders and runs down her back.
- **Face.** Long and waxy. One strand is stuck across the forehead, with a drip. The mouth is thin, patient and downturned.
- **Eyes.** Empty: the pale, clouded eyes of the dead. They have no pupils, only a milky film over a fainter iris, under heavy brows with sunken shadows below.
- **Gown.** A long pale nightgown, white as butcher paper, sprigged with faded flowers like an old print. It is darkened where her hair drips on it and has one old stain low down. The sleeves run to the wrist, and the hem falls below the calf.
- **Hands and feet.** The hands are long, with dirty fingertips. She is barefoot: pale feet with the toes apart, dirt around the soles, and ankle bones and tendons showing.
- **Neck.** Her neck's own net is pale, with tendons drawn tight.

### Her pose

She no longer folds her head flat against her shoulder. She is only a little too tall for the tunnel, so she ducks the way a tall person ducks under a doorframe:

- a slight lean from the hips, with soft knees and long arms hanging;
- a long pale neck that rises straight out of her collar, kinks, and bows forward with its upper half tipped hard to one side;
- a smaller head, about two-thirds its old height, hanging further along the bend, bowed and tipped; it half-turns toward the reader.

The neck is two parts with a visible kink between them. The new upper part, `bent_neck_upper`, uses the same pale net as the lower.

**Proportions.** She is posed at natural proportions, then drawn at 0.56 about her feet. That keeps her crown at about 2.92 blocks, under the tunnel's three-block ceiling.

- Her torso is now as deep as a person's (4 pixels before scaling, up from 2.4), so it is no longer as thin as her neck from the side.
- Her legs and arms are deeper too.
- Her widths are unchanged.

When she walks, her stride, catch-up stride and bob drive the same pose.

**Unchanged:**

- her actor and its 2.5 scale;
- her collision body;
- her movement and counted steps;
- the signs, the cameras, the story and the account.

## Tests and proofs

- **New uncanny case.** A cut through the man's tissue sends a tear to the reader. Asked again and again, the worker's story moves on each time.
- **Locally:**
  - the 57 uncanny cases pass with these changes;
  - the character painting checks pass (`tools/paint_character_skins.py --check` and `tools/verify_character_art.py`);
  - the native uncanny proof renders her front and side, walking and in the catch-up stride (`native-uncanny-8`, now labelled "Stooping under the ceiling"), and her face on the first two pages.

## Versions

- No protocol change (96): the new neck part and the textures are client art; no block, item or state is added.
- No layout or Witness change: 54 / 41 / two kinds / three endings.
