# The Pell house: more detail in its textures (0.4.528)

The owner, 12 November, looking at June's hairbrush: "Can we try to add a little more detail to textures?" HD textures are out, so
this stays at sixteen pixels to the block. Every texture the Pell house's own art tools draw is drawn again with more in it. Nothing
else about the house changes: its layout, models, blocks, people and behaviour are as they were.

## What changes

- **Light and form.** Light falls from the upper left, as on vanilla's own blocks and items. An edge facing it is lighter, the edge
  away from it darker.
  - Raised panels are lit along their top and left and shaded along their bottom and right: the cupboard and wardrobe doors, the
    drawers, the oven door, the inner doors' panels, the fridge's handle.
  - Recesses go the other way round: the sink's bowl, the trough, the screens, the grilles, the toilet's bowl.
- **What things are made of.** Six new surfaces in `tools/draw_pell_house.py`, each from a fixed seed:
  - `wood`: lines of grain drifting along the board, darker streaks, now and then a knot;
  - `metal`: brushed lines, some bright and some dull;
  - `enamel`: nearly even, with a few specks;
  - `cloth`: a fine twill;
  - `card`: the flutes showing as faint bands, a few fibres;
  - `plastic`: smooth, with a scuff.

  Each tiles, so a part that takes a strip of one from anywhere shows the same stuff. `bevel` and `inset` light a raised panel or a
  recess on whatever is drawn there.
- **The held things** (`tools/draw_pell_people.py`) have an outline in a darker shade of their own colour and four or five tones
  where there were two.
  - **The hairbrush:** a paddle with a lit rim, a dark cushion, pale bristle tips in rows with deeper ones between, and a rounded
    handle with a worn patch where the thumb goes.
  - **The saucepan:** its side lit on the left with a bright streak, the rim and the dark inside, the spoon leaning on the far rim, a
    riveted bracket, and a hole in the handle's end to hang it by.
  - **The plates:** a glint on the glaze, a patterned band, the well's wall shaded on the side toward the light.
  - **Dinner:** the mash rough on top, the gravy glossy, the sausages browned with a char line across each, the peas each catching
    the light, and a little shadow under the food.
  - **The folder:** the card's grain, a typed label, a stamp half worn off, a paper clip.
  - **The cane:** a ribbed grip, and the band's reflective line.
- **The room things** (`tools/draw_pell_things.py`) are each made of one of those surfaces, and the fronts have their relief.
  - **Kitchen:** the bench doors in grained laminate with their handle grooves; the sink in brushed steel with a lit rim and ridged
    drainer; the stove's knobs and clock, its oven glass with a glint; the coils lit on one side.
  - **Bedrooms and living room:** the television's wood case and curved dark screen; the mantel's bricks each a shade apart; the
    cushion's flowers; the quilt's stitching; the mattress's ticking and tufts; the books' spines with bands; the photographs'
    frames and glass.
  - **Laundry and bathroom:** the frost along the freezer's seal; the padlock's brass.
  - **Garage:** the sleeping bag's quilting and zip; the cartons' tape; the pegboard's holes.
- **The house** (`tools/draw_pell_house.py`):
  - the bricks lit along their top course and shaded along their bottom;
  - the deck's boards grained;
  - the pavers each a shade apart, with lit edges;
  - more aggregate in the concrete, curb and slab;
  - the ceiling's stipple, the plaster's trowel sweeps and the carpet's pile;
  - the inner doors' raised panels, the palings' grain and lit edges, the roller door's slats and the posts' grain.
- **The line** (`tools/draw_pell_line.py`) is drawn over the house's ground, so it now lies on the new concrete, pavers and deck. Its
  own paint is unchanged. The pavers' joins and the deck's gaps keep the exact colours the line finds them by.

## What does not change

Models, block states, layout, sounds, behaviour and tests. The people's skins are painted by `tools/paint_character_skins.py` with
every other cast in the game, and are not part of this. No Witness change.

## Decided here

- Light from the upper left everywhere, as vanilla does.
- Outlines only on the held things, which are seen against nothing; blocks keep their edges in their own colours.
- The glass, paper and painted signs keep their flat colours where they are meant to read as flat.

## Numbers

The four Pell art tools still verify their pack with `--check`, and CI runs them. No layout change and no Witness change.
