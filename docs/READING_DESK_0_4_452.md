# The reading desk at sixteen pixels (0.4.452)

The owner's note this answers (docs/BORRASCA_PLAYTEST_2026_11_10.md):

- 20, of the desk in Drisking's historical society: "retexture this table and all instances of it. It's got an hd texture that
  isn't in line with vanilla"

## What it was

The reading desk (`HouseholdFurnitureBlock.Kind.READING_DESK`) was the only model drawn from `hunt_fan_oak`, a sheet 64 pixels
square that was made for the fan room: four scratched boards at four times the game's scale. Every reading desk in the mod used
it, so the fault was in each of them: in Drisking (the historical society, the chapel and the hut in the depot's yard), in the
school at Indian Lake, in the Whalestoe hospital, in Zampanò's flat, in the Winchester house, in the quiet rooms, in the folding
rooms and at the inquiry.

## What it is now

Three pictures of its own, each sixteen pixels square, drawn in the long runs of two or three tones that the game's own planks
are drawn in:

- **the top**: three boards with a dark seam between them and a knot or two;
- **the wood**: the edge of the top (its worn arris, and the shadow under it), and below that an upright grain for the legs, the
  rail and the sides of the pedestal;
- **the front**: the pedestal's face, three drawers with a brass pull each and a dark gap between them.

The desk's shape, where it stands in every room, and what a hand does with it are unchanged. Every reading desk changes at once,
because they are one model.

## How it is made

- `tools/draw_reading_desk.py` draws the three pictures and writes `models/block/furniture_reading_desk.json` with them on it. Its
  `--check` runs in the quick gate.
- `hunt_fan_oak.png` is left where it is, unused. The art inventory records it, and nothing leaves the inventory without a record
  of its own.
- `tools/generate_hunt_review_assets.py` no longer puts that sheet on the desk.
- No code, layout, protocol or Witness change.

## Proof

The desk was looked at in first-person renders, in each of its four facings, on a dark floor against brick as it stands in the
society, with a cane chair beside it (a local review aid; not part of CI).

## Not done here

- The rest of the household furniture is on pictures 32 pixels square, twice the game's scale where the desk was four times: the
  walnut desk, the cane chair, the bedside table, the chest of drawers, the armchairs and the sofa, the footstool, the formica
  table and the stool. They are the next thing in this pass.
- The fan room's own sheets are 64 pixels. They belong to the texture pass and are not touched here.
