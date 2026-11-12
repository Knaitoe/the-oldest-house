# The Pell house: the ground, the roof and the pool cover (0.4.529)

The owner, 12 November, after 0.4.528's indoor detail: "Much better! Review your environmental textures too." Still sixteen pixels to
the block. This pass is the outside of the Pell house: the street, the lawns and beds, the roof and eaves, the windows seen from the
street, and the pool cover.

## What the renders showed

Eye-level renders of the street, the front, the drive, the yard and the pool, taken before this pass:

- **The road** was nearly black and almost flat. At any distance it read as a hole.
- **The lawns** were flat green, and the street's dry lawns flat straw, each with a few lines. **The mulch** was a flat brown.
- **The eaves' underside** used the ceiling's white stipple, so it glared under the roof.
- **The roof** was rows of tiles all of one colour.
- **The pool cover** drew its sag inside every block, so the middle of the cover showed a grid of separate puddles. A fold line ran
  across every block of it.

Found while redrawing:

- One bright pixel repeats on every block that uses the texture. The lawn's small white flower made a grid of white dots across the
  whole lawn.
- So does anything at a block's centre, even on a block turned at random, because turning keeps the centre where it is.
- The cover's sag lay lower than the cloth around it, and the sheet has no sides, so the step left a gap. The pool's lit tiles showed
  through it as a bright line round the sag.

## What changes

- **The road:** grey rather than black. The stones of its aggregate show in several greys, some lit on top with a little shadow
  under them, and darker patches where the tar has come up.
- **The lawns:** the blades grow in clumps, dark at the root and pale at the tip, some of them leaning. June's watered lawn has a
  little clover in it. The street's lawns, dry in the watering ban, are straw, with bare soil in patches and a few tufts still green.
- **The mulch:** bark chips, lit on their top edge and dark under it, and a few twigs.
- **The roof:** each tile is a shade apart from its neighbours, its rounded top lit and its nose in shadow, with a few spots of
  lichen.
- **The eaves:** the underside has a lining of its own, a painted sheet with its joint and nail heads and a cobweb in one corner
  (`soffit`, used by the eave's model). The fascia is a warmer cream.
- **The blinds,** seen from outside: slats in light and shade with dark gaps between them, one slat bent, and the cord.
- **The pool cover:** the cloth is faded in patches, faintly creased, with a little grit; its leaves have washed down into the water.
  The sag, five blocks by eight in the middle, holds one pond.
  - The water is dark, with the cloth's blue showing through it, two faint ripples, a smear of sludge and one gum leaf.
  - At its edge the cloth is darker where the water stood, with a broken line of grit where it stood highest. The water's edge
    catches the light.
  - The pond's corners are rounded, and a leaf has caught at the edge of each.

## How

- **Turned at random.** Vanilla breaks up grass by giving each block a random quarter-turn in its block state. The lawns, the mulch,
  the road, the cover's dry cloth and the inside of the pond are turned the same way (`TURNED`, `TURNED_FITTINGS` and `turned()` in
  `tools/draw_pell_house.py`), so their features do not line up in rows.
- **The pond's pieces.** The cover's block has a stage for each part:
  - stage 0: dry cloth;
  - stage 1: the pond's inside;
  - stage 2: the pond's edge, facing its dry side;
  - stage 3: a corner of the pond, dry to the side it faces and to that side's left.

  `PellSite.cover` sets them. The edge's waterline is six pixels in from the dry side. Each corner's waterline is a quarter circle
  ten pixels in radius about its inner corner, so it meets the edges six pixels in, and the turned copies join into one pond. From
  under the water every cell shows only the cloth.
- **One height.** Every cell of the cover now lies at the same height, and the sag is in the drawing alone. The cover is a sheet
  with no sides, so where the sag's cells lay lower, each step left a gap. The pool's lit tiles showed through it as a bright line
  round the pond, and the renders taken during this pass showed a second line round its inside. The cover before this pass had
  the same gap round its sag.
- **Nothing bright or centred** on a surface that repeats every block. The leaves sit off-centre in the water and lie only in the
  pond.

## What does not change

The house's layout, its people, their behaviour and the line. The cover is still where it was, holds nobody up, and has the same
shape. Only its states now say which part of the pond each block is. No Witness change.

## Checked

- All four Pell art tools redraw and pass `--check`.
- The Pell house's first case also checks the pond's corners, its edges and the dry cloth around it.
- The pond was put together from its turned pieces, seen from above, and the in-game renders of the street, the front, the drive,
  the yard and the pool were compared with those taken before.

## Numbers

The four Pell art tools still verify their pack with `--check`, and CI runs them. No layout change and no Witness change.
