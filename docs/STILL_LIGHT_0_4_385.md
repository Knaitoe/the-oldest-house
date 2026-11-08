# The still light (0.4.385)

The first of the new trial rooms from [HALLS_TRIALS_AND_TIME_DESIGN.md](HALLS_TRIALS_AND_TIME_DESIGN.md) (tier 1, trial 1): "A dark corridor in which the lamps come on only while you stand still and go out when you move. Cross it from memory, a stretch at a time."

With 0.4.384's seals, a trial is something the staircase asks for, so it has to be a real crossing. Where the design left the details open, the choices made are noted.

## The room

`STILL_LIGHT`, slot 223, a gray room (`TrialRooms`). It is forty-four blocks long and nine wide.

- **The floor.** Boards over a trough three blocks deep, with a landing at each end:
  - one way across is worn pale, and those boards always hold;
  - the boards either side are dusty and unworn.
- **The trough** is wet brick. It runs back under the floor to two steps up beside the entry, so a fall puts the reader back at the start. A fall of three costs nothing.
- **The lamps.** Seven caged lamps hang down the middle of the ceiling. Nothing else lights the room.
- **The way across** runs in eight stretches, each turning on its last row (`TrialRooms.wayInOrder`). It is the same in every world, so it can be learned.

## The rules

- **Light.** The lamps come up when any reader in the room has held still for half a second (`STILL`, 10 ticks). They go out a breath after everyone moves (`FADE`, 6 ticks), with the click of a bulb.
- **The boards.** In the light, every board holds. In the dark, a dusty board gives way under a reader who stands on it (`GIVE`, 2 ticks) and drops them into the trough. Crouching does not help: the boards are whole until they go.
- **Coming back.** A board that gave is back, unseen, once nobody is where it was.
- **No flame.** Elsewhere the House lets a reader set down a torch or lantern. Here anything that gives light will not stay alight: it goes out as it leaves the reader's hand.
- **The mark.** Coming through by the far door is a trial (`LabyrinthPacing.physicalTrial`) and gives its mark (`TrialMarks`, "the room that lit for stillness").
- **Depth.** It is dealt from depth six, with the other first-tier hazards.

So the crossing is the design's: stand still, look at the next stretch while the lamps are up, then walk it in the dark from memory.

### Together

The lamps are shared. While one reader holds still, every board holds, so a friend can stand at the entry and light the way for another. The last one across still has to do it in the dark. The design left this open; it was chosen because it makes the room a thing friends do together rather than side by side.

### Words

None. The room teaches itself: a reader who stops to look around sees the lamps come up, and the first step in the dark shows the rest. The one line is for a light that will not take: *It goes out as it leaves your hand.*

## Art

`tools/draw_trial_art.py` (CI and the quick gate run it with `--check`) draws seven textures:

- the blackened wainscot;
- the smoke-dark ceiling;
- the worn boards and the dusty boards;
- the wet brick;
- the caged lamp, dark and lit.

The materials are `trial_still_wall`, `trial_still_ceiling`, `trial_still_path` and `trial_still_under`. The boards and the lamps are one fitting, `trial_fitting` (`TrialBlock`), whose kinds later trial rooms will share. No vanilla building block is used in the room.

## Tests

New case: `HazardVarietyTests.theStillLightLightsForStillnessAndItsDustyBoardsGiveInTheDark`.

- **The plan.** Every board is where it should be: worn on the way, dusty off it, open over the steps. The trough has standing room under every board. The way is one unbroken walk from landing to landing. The steps, landings, far door and lamps are in place. It is a first-tier physical trial with its own mark.
- **The lamps.** Holding still lights every lamp and the room. Moving puts them out a breath later.
- **The boards.**
  - In the dark the dusty board under a moving reader gives, and only that one.
  - The worn way holds.
  - While a friend holds still, a dusty board holds for someone walking on it.
  - The board comes back once nobody is there.
- **No flame.** A torch the House's own rule would allow is refused.

The hazard layout case now also builds the still light and checks its entrance and retreat door.

## What stays

- **Witness:** 81 / 61 / two kinds / three endings.
- **Protocol:** 163.
- **Layout:** 124. One appended slot (223). New builds only.
