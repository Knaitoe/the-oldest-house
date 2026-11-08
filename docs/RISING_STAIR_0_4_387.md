# The rising stair (0.4.387)

The third of the new trial rooms from [HALLS_TRIALS_AND_TIME_DESIGN.md](HALLS_TRIALS_AND_TIME_DESIGN.md) (tier 2, trial 6): "A flooded stairwell whose water rises one step every three seconds. Climb faster than it, and dive for the two air pockets if you fall behind." It takes the listening room's place in the build order (see the soft floor's notes).

## The room

`RISING_STAIR`, slot 225, a gray room (`TrialRooms`). It is fifteen blocks wide and twenty-seven high.

- **The landing at the foot.** The reader comes in on a dry landing of stone flags. A door in its north wall opens on the shaft.
- **The shaft** is nine by nine, walled in wet glazed tile under a brick vault, with silted flags at the bottom.
- **The treads.** Forty-four stone treads are let into the walls, one to a cell, going clockwise round the shaft from beside the door. Each stands half a block above the last, so the climb is a walk, two and a half times round.
  - Six treads are missing. Each gap is a single stride to jump, up half a block.
  - The last tread is level with a landing at the head, where the far door is.
- **The pockets.** Two doorways open off the treads, a step to the side, at the fourth and the twelfth block up. Behind each is a cell whose top two blocks are closed round on every side, so the water can never reach them.
- **Light.** Frosted windows, lit from behind, are set into three walls at four heights.

## The rules

- **The water.** While anyone is on the stair, the water rises a block every second and a half (`RISE`, 30 ticks), with a pour of water each time. It closes over the treads as it comes; the treads stand in it.
  - The design's "one step every three seconds" was too slow to press anyone: a reader walking up the treads climbs three times as fast as the water. Here the water keeps pace with a reader who hesitates at a gap, not with one who keeps going.
- **The undertow.** In the shaft the water pulls down. Without it, a reader could tread water and ride the rising water to the top. With it:
  - nobody floats up;
  - a reader the water closes over can still walk up the treads underneath, but cannot jump a gap with water over their feet.
- **Falling behind.** A reader caught below a gap has two ways out:
  - **a pocket.** The air in a pocket holds. While the only readers in the shaft are in pockets, or already past the water on the landing at the head, the water goes down. When it has, the reader comes out and climbs on.
  - **back down.** Under the water, a reader can walk down and out through the door at the foot.
- **Out of air.** A reader who runs out of air under the water is back on the landing at the foot, breathing. One line, in grey: *The landing again, and the taste of the water.*
- **Draining.** Once nobody is on the stair, the water waits a second, then drains a block every four ticks.
- **Coming back.** A room is ticked only while someone is in it. Coming back to one left empty, the shaft is dry again. (The still light and the soft floor do the same: their boards and squares are whole, and the lamps out.)
- **The mark.** Coming through by the far door is a trial and gives its mark (`TrialMarks`, "the stair the water came up").
- **Depth.** It is dealt from depth ten, with the other second-tier hazards.

### Together

The water is shared, and it rises while anyone is on the stair. A slow climber brings it up for everyone, and a fast one at the head no longer holds it there. A reader who has fallen behind can shelter in a pocket and wait for the water to go down, but only once everyone still climbing has reached the head or come off the stair. Readers who drown come back to the landing at the foot and can't climb again until the water falls, so a party does best keeping together.

### Words

One line, for drowning. The rest is the water: the pour as each layer comes, and now and then the draw of it going down.

## Art

`tools/draw_trial_art.py` draws six more textures (nineteen in all):

- wet glazed tile, gone green and black where the water stands;
- a brick vault;
- silted flags at the foot of the shaft, and dry stone flags on the landings;
- a frosted window lit from behind;
- the stone tread.

The materials are `trial_rising_wall`, `trial_rising_ceiling`, `trial_rising_floor`, `trial_rising_landing` and `trial_rising_window`, the window giving light. The treads join `trial_fitting` as `rising_tread` (stage 0 a whole step, stage 1 a half). Every fitting can now stand in water. No vanilla building block is used in the room.

## Tests

New case: `HazardVarietyTests.theRisingStairFloodsBehindAClimberAndPutsTheDrownedBackOnTheLanding`.

- **The plan.**
  - Every tread is where it should be, a step or a stride on from the last, with headroom over it.
  - Each gap is a single stride.
  - The last tread is level with the landing at the head.
  - Both doors, the landing at the foot and the shaft floor are in place.
  - The pockets open off a tread and are closed round their air.
  - The windows are lit.
  - It is a second-tier physical trial with its own mark.
- **The water.** With a reader in the shaft it rises a layer every thirty ticks, closing over the treads. Its undertow pulls the reader down.
- **Drowning.** Out of air under it, the reader is back on the landing at the foot.
- **Draining.** With nobody on the stair the water goes down.
- **Full.** The water stops short of the landing at the head, and it fills each pocket's way in but never its air.
- **Where the stair is.** The shaft floor and the treads count as the stair; a pocket and both landings do not.
- **Coming back.** With nobody in the room the flooded shaft stays as it is; a reader coming back finds it dry.
- **A pocket.** With its only reader in a pocket, the water goes down.

The still light's and soft floor's cases pass unchanged with the reset on coming back.

## What stays

- **Witness:** 81 / 61 / two kinds / three endings.
- **Protocol:** 163.
- **Layout:** 126. One appended slot (225). New builds only.
