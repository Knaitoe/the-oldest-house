# The household furniture at sixteen pixels (0.4.453)

This follows the owner's note 20 (docs/BORRASCA_PLAYTEST_2026_11_10.md), of the reading desk: "retexture this table and all
instances of it. It's got an hd texture that isn't in line with vanilla". The desk was redrawn in 0.4.452. It was the worst of a
set: the rest of the household furniture was drawn from pictures 32 pixels square, twice the game's scale. A cane chair stands
beside the desk in the owner's own screenshot. This is the rest of the set.

## What changes

Eleven pictures, each drawn again at sixteen pixels in its own colours:

| Picture | What it is on |
|---|---|
| walnut | the walnut desk, the frames and legs of the chairs, the sofa and the footstool, the bedside table and the chest of drawers, and the frames of the household's records (awards, photographs, notes, recipes, drawings) |
| cane | the cane chair's seat and back: strands three pixels wide over and under each other |
| bedside_front | the bedside table's face: two drawers, a brass pull each |
| drawer_front | the chest of drawers' face and the walnut desk's pedestal: four drawers, two brass pulls each, so that the desk's narrow pedestal shows one |
| blue_weave | the sofa: a check |
| floral | one armchair and the footstool: small flowers on cream |
| green_cloth | the other armchair |
| formica | the kitchen table's top and the stool's seat: flat laminate with its fleck |
| metal_legs | their legs, and the radiator's feet: tube, lit on one side |
| enamel | the radiator and the washing machine |
| washer_front | the washing machine's face: a strip of controls, the round door with the wash behind its glass, a kick plate |

The pieces keep their shapes, their places and their colours. Nothing a hand does with them changes. The trailer's formica table
and the chairs and table that lie toppled in the household halls use the same pictures and change with them.

## How it is made

- `tools/draw_household_furniture.py` draws the eleven pictures under `textures/block/household/` and points every model that
  named an old one at the new one: 132 models (12 pieces of furniture, 3 trailer tables, 7 toppled pieces, 110 records). Models
  are pointed, not rewritten: every part of a picture that a model shows already fell on whole pixels at sixteen.
- Its `--check` runs in the quick gate. It fails if a picture is not what it draws, or if any model names one of the old
  pictures again.
- `tools/draw_domestic_records.py` names the new walnut for the records' frames; `tools/draw_domestic_lives.py` tips the
  furniture's own models over, so it follows them. Both checks pass.
- The eleven old pictures are left where they are, unused. The art inventory records them, and nothing leaves the inventory
  without a record of its own.
- No code, layout, protocol or Witness change.

## Proof

Every piece was looked at in first-person renders, standing in two rows on a plank floor, from in front and from close (a local
review aid; not part of CI).

## Not done here

- The chess table is drawn from the game's own oak and needs nothing.
- Other rooms still have pictures over sixteen pixels (the fan room's sheets, the hotel's, the nightclub's among them). They
  belong to the texture pass, room by room.
