# The bathroom's hair, seen in the glass — 0.4.167

The owner, about the bathroom's routine counter:

> "develop more tools for textured hair - the player should be able to see a preview when picking up the tool ... reflected in the mirror"
>
> "It does seem like seem some overlap the others, or maybe hairs can't be re-selected after having been selected previously?"
>
> "More hair styles would still be appropriate"
>
> "rug texture can be expanded too."

## What was wrong

Two things made the counter feel broken:

- **The tools looked like they overlapped.** Each routine block held three tools in thirds of a block, about five pixels each. The clickable thirds did not actually overlap, but the tools were too small and too close to tell apart.
- **A choice could not be taken back.** Nothing in the glass showed what a tool would do. The first tool used at the mirror ended the room ("You have what you need."), so the reader could not try another.

## The counter

- **Sixteen tools on eight blocks**, two tools to a block, one in each half. The vanity now runs the full width of the wall (x -4 to 4).
- **Four new tools.** Each is drawn in code like the rest, with its own item and its place in the House's prose:

  | Tool | Prose |
  | --- | --- |
  | satin bonnet | tied on the satin bonnet |
  | edge brush | laid your edges with the little brush |
  | hot rollers | set your hair in the hot rollers |
  | wave brush | brushed your waves down |

- **The four sets are unchanged.** The House still reads the same four routines (wrapped, braided and oiled, brushed dry, washed and barbered), and the new tools join them:
  - bonnet with the headwrap;
  - edge brush with the scalp oil;
  - rollers with the paddle brush;
  - wave brush with the pocket comb.

  Each set's two blocks stand side by side. The pairs are shuffled along the counter, and the two tools on a block swap at random.
- **Picking.** A click takes the tool from the half of the block it lands on. A tool put back by its own place can be picked up again, and taking another puts the first back. The test checks both.
- **The postcard** moved up beside the mirror's top, at (1,2,-9), to make room.

## The preview in the glass

- While a routine tool is in the reader's hand, the bathroom glass shows the reader wearing its style. There are sixteen styles, one for each tool:

  | | | | |
  | --- | --- | --- | --- |
  | silk headwrap | defined curls | picked-out afro | oiled braids |
  | cornrows | two puffs | brushed straight | blown-out volume |
  | clipped-up twist | side part | slicked back | close crop |
  | satin bonnet | laid edges and a ponytail | set in rollers | 360 waves |

- **Colour.** The hair is tinted to the reader's own hair: the commonest colour on the crown of their skin (the hat layer if it covers the crown). Wraps, bonnets, ties, clips and rollers keep their own colours.
- **Trying them out.** Picking up another tool changes the reflection, so a reader can try them one by one. Nothing is decided until a tool is used at the mirror.
- **Afterwards.** Once a tool is used, the House keeps that style for its version of the reader: in later glass and on the doubles.
- **Never the live model.** The style is drawn only in glass and on the House's copies of the reader.
- **Trait switched off.** If the operator switches the hair trait off, the House keeps no style.
- **Pixel scale.** The patterns are drawn at Minecraft's own pixel scale (`tools/draw_hair_styles.py`; CI checks the shipped sheets).

## The rug

The bath mat is a woven rug nearly two blocks long: a blue field, a rust and cream border all the way round, and a fringe at both ends. Its two halves join without a seam.

## Tests and proofs

- **Routine test:**
  - eight distinct routine blocks, each set's pair side by side;
  - the postcard in its new place;
  - the first tool sets the read tentatively;
  - a tool put back can be picked up again;
  - using the tool in hand at the glass settles it.
- **Light test:** a routine used in the dark is read, completes the room, and the House keeps its style (`HouseReadLook.hair`).
- **Reader test:** loops over all sixteen tools.
- **Client proof:** three native pages in `build/house-read-hair/`. CI requires them.
  - the sixteen styles from the front, tinted to four crowns;
  - the same from behind;
  - the eight routine blocks, with a tool lifted, and the rug.
- **Locally:** the house-read, tailoring and co-op suites pass (37 cases).

## Versions

- **Protocol 93:** the reader's look now carries the kept hair style.
- **Quiet-room build 167:** a quiet room built but not yet entered is built again with the new counter. Rooms already entered stay as they are.
- **Unchanged:** the labyrinth layout and the Witness: 54 / 41 / two kinds / three endings.
