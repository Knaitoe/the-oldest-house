# The soft floor (0.4.386)

The second of the new trial rooms from [HALLS_TRIALS_AND_TIME_DESIGN.md](HALLS_TRIALS_AND_TIME_DESIGN.md) (tier 1, trial 2): "Floor plates sink while someone stands on them. Stop for more than two seconds and the plate drops you to the corridor below, which leads back to the start. Keep moving."

## The room

`SOFT_FLOOR`, slot 224, a gray room (`TrialRooms`). It is forty-two blocks long and seven wide.

- **The floor.** Squares of sodden carpet laid over a passage three blocks down. Firm landings lie at each end.
- **The passage below** is packed earth between the joists. It runs back under the floor to two steps up beside the entry, so a reader who goes through comes out at the start. A fall of three costs nothing.
- **What slows a reader down:**
  - **Low beams.** Three run across the room at shoulder height, so a reader has to crouch under them.
  - **Half-walls.** Two stand across the room, staggered left and right, so the way across has to wind.
- **Light.** The room is lit. This trial is about time, not sight.

## The rules

- **Sinking.** A square sinks while anyone stands on it:
  - a little after half a second;
  - more after a second and a quarter;
  - at two seconds it gives way, and the reader drops into the passage.
- **Weight.** Two readers on one square sink it twice as fast.
- **Recovering.** A square nobody is on rises back as fast as it sank. One that gave way is back, unseen, once nobody is where it was.
- **Crossing.** Walking, a reader is on each square for about five ticks. Crouching under a beam, about fifteen. Neither is anywhere near two seconds, so a reader who keeps moving gets across. A reader who stops to think, does not.
- **The mark.** Coming through by the far door is a trial and gives its mark (`TrialMarks`, "the floor that would not hold a pause").
- **Depth.** It is dealt from depth six, with the other first-tier hazards.

### Together

Squares are shared, and two readers on one sink it twice as fast. Readers who cross together have to cross side by side, not in single file. The landings hold anyone.

### Words

None. The sinking is felt underfoot: the square drops under the reader with a soft wet give, and they drop with it.

## Art

`tools/draw_trial_art.py` draws six more textures:

- damp wallpaper;
- stained ceiling tiles;
- quarry tiles for the landings;
- packed earth;
- the sodden carpet square;
- the adzed beam.

The materials are `trial_soft_wall`, `trial_soft_ceiling`, `trial_soft_landing` and `trial_soft_under`. The squares and beams join `trial_fitting`, whose stage now runs from 0 to 3. Every kind has a model at every stage. The lamps are the still light's caged lamps, lit. No vanilla building block is used in the room.

## Tests

New case: `HazardVarietyTests.theSoftFloorGivesUnderAPauseAndHoldsAReaderWhoKeepsMoving`.

- **The plan.** Every square, half-wall and beam is where it should be.
  - There is room to crouch under every beam, and not to walk upright under it.
  - There is room to pass each half-wall, standing room in the passage under every square, two steps up, a far landing, a far door and lit lamps.
  - It is a first-tier physical trial with its own mark.
- **A pause.** A moment's standing does nothing. Then the square sinks a little, then more, and gives way at two seconds.
- **Moving.** A reader who keeps walking never sinks a square past its first stage.
- **Company.** Two on one square sink it through twice as fast.
- **Recovery.** Every square is back once nobody is on it.

The still light's case and the hazard layout case pass unchanged with the wider stage range.

## What stays

- **Witness:** 81 / 61 / two kinds / three endings.
- **Protocol:** 163.
- **Layout:** 125. One appended slot (224). New builds only.

## Why not the listening room

The design's third tier-1 trial, the listening room, would duplicate the quiet hall of 0.4.80, which already fills a noise meter and sweeps a loud reader back to the start. It is set aside; the next trial is the rising stair (tier 2).
