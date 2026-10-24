# Quick fixes, 0.4.117

The first batch of the owner's October 24 notes. No layout, protocol or Witness change: 52 sources / 39 resolutions /
two kinds / three endings.

## Names and sounds

- Literary actors are named "someone in the room", lowercase, wherever a name shows.
- The Tell-Tale floorboards lose their three sculk sensors. Nothing under the floor makes a sound but the heart.

## Hotel bar

- A death the bar retells now carries its own death message and the place it happened: "out in the world", "in the
  House", or the vignette's prose name ("in the red room"). Deaths the snow globe or a staircase leak cancel were never
  recorded and still are not. The scarred guest also counts them: "They tell me you have died N times in this house."

## Plain

- The developed photograph always holds the vulture. If the real bird is rendered inside the crop it is the bird;
  otherwise the camera composites its silhouette above the crouched figure, at a height and offset that scale with
  distance. The figure was already drawn crouched (0.4.109).

## Hospital (night ward)

- The ward's fog is purple and close: a stronger violet colour, and terrain fog from 1.5 to 20 blocks while the ward
  scene is active.
- The call button is its own block, `night_ward_call_button`: a real `ButtonBlock` (stone set, 20 ticks), so the click
  handler and both ward tests keep working, drawn as a red cap on an enamel bezel. `tools/draw_night_ward_assets.py`
  draws its texture, both models and its blockstate, and CI checks them.

## Séance

- One new block, `seance_fitting` (four kinds, facing, open), replaces the chest and the three lecterns at their own
  positions: a mahogany cupboard with paneled doors and a brass escutcheon, the unposted letters tied with a faded ribbon
  on a side table, the medium's sitting record open on a tilted stand, and the family album in green leather with an
  oval photograph window. Clicking the cupboard swings its doors open (or shut) and counts as the same single
  disturbance as before. `tools/draw_seance_fittings.py` draws the eleven textures, five models and the blockstate;
  CI checks them.
- The family stands at the table in front of their own chairs, hands on the wood, instead of sitting in them. Their
  flight starts from where they stand; the daughter's route steps round the table's corner first.

## Hill House nursery

- The cellar stair runs between its own walls: no void or sky shows from the steps.
- The cold is harmless. Freeze damage inside the nursery is cancelled; the frost and the cold door still count.
- "HELP <NAME> / COME HOME" is chalked large above the photograph on the nursery wall.
- The pounding now waits after it comes to the reader's nearest wall ("The pounding stops, as if it is listening.").
  Knocking back on that wall within five seconds is answered from the same wall, knock for knock, and counts as a knock
  toward the ending as the cellar knocks do. After an answer the pounding goes quiet for twelve seconds.

## Holy Rabbit

- Three lines that gave the meat away are rewritten: the child no longer folds the skin into the snow, the morning
  line no longer ties the meal to the rabbit's return, and Father no longer says "keep the skin folded".

## Tests

- New literary case `poundingWaitsForAKnockBackTheColdNeverWoundsAndTheStairIsWalled`.
- The séance haunting case checks the four fittings, the cupboard's doors, and that the family stands at the table
  rather than in the chairs.
