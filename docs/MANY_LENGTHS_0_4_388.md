# The hall of many lengths (0.4.388)

The time trial from [HALLS_TRIALS_AND_TIME_DESIGN.md](HALLS_TRIALS_AND_TIME_DESIGN.md) (trial 10): "A forty-block hall with shared ends and a private, folded middle (T1 and T2), paced by P1." P1 is "hurry is punished": "Sprinting or jumping folds you back one repeat; walking crosses in fixed time. Zeno's corridor: whoever runs arrives last."

## The room

`MANY_LENGTHS`, slot 226, a gray room (`TrialRooms`).

- **The lengths.** The hall is five lengths, each nine blocks of straight hall and then a bend three blocks aside, like the long hallway's (`LabyrinthLoops`).
- **Every length is the same as the next, block for block:**
  - walls of distempered plaster, with a walnut rail at waist height and a brass rule let into it;
  - oak boards, with a brass strip across them at every block;
  - a coffered ceiling;
  - a caged lamp over each straight.
- **The ends.** The entry door opens into a bend, with standing room inside the door, so every length but the first has the same bend behind it. The first is never one a reader is moved back into. The far door is round the last bend, where a sixth length would begin, and can't be seen from the last straight.
- **Length.** Walked, the whole hall takes about fifteen seconds. The bends make it longer than the design's forty blocks; they are what hide one length from the next.

## The rules

- **Hurrying** means sprinting, or jumping.
- **Moved back.** A reader who hurried in the second, third or fourth length is moved back one length as they step into the next.
  - They land at the same spot in the length they just left, facing the same way and keeping their pace.
  - The lengths are the same, so nothing shows it happened.
- **Free lengths.** The first and the last lengths are free: the first has the entry behind it, and the last has the far door ahead.
- **Walking**, a reader crosses the hall once, in the time it takes to walk it.
- **Never seen.** Nobody is moved back while anyone else, a spectator included, could see them or the place they would land.
  - The move waits until nobody can.
  - A reader who gets through that length's straight while still watched is let off.
- **Words.** On the first move back: *The same bend, again.* On the third: *No nearer for hurrying.* Both are grey, in the action bar.
- **The mark.** Coming through by the far door is a trial and gives its mark (`TrialMarks`, "the hall that would not be hurried").
- **Depth.** It is dealt from depth ten, with the other second-tier hazards.

### Together

The ends are shared. The middle is, in effect, private: the bends hide each length from the next, so a reader moved back out of everyone's sight is as alone as in a copy of their own. Readers who go in together come out apart once one of them hurries.

- **Watching.** A reader being watched is not moved back. A friend who keeps them in sight can carry a hurrying reader through, but nobody can sprint down the hall and watch someone beside them at once.
- **Footsteps.** The first through, in the last length, hear the others' steps in the walls, one side then the other, for as long as anyone behind them is still moving.

### What the design proposed and this doesn't do

- **Private copies (T1).** The bends make a move back out of sight as private as a copy, without building one per reader.
- **Felt time (T4).** Sounds pitched by pace are not built; the moves back are the whole effect.

## Art

`tools/draw_trial_art.py` draws four more textures (23 in all):

- distempered plaster;
- the measuring rail: walnut, with a brass rule ticked every two pixels, longer every four, longest every eight;
- measured oak boards;
- a plain coffered ceiling.

The materials are `trial_lengths_wall`, `trial_lengths_rule`, `trial_lengths_floor` and `trial_lengths_ceiling`. The lamps are the still light's caged lamps, lit. No vanilla building block is used in the room.

## Tests

New case: `HazardVarietyTests.theHallOfManyLengthsSendsBackAReaderWhoHurries`.

- **The plan.**
  - Each length is the same as the next, block for block.
  - Every straight and bend clears a body, every lamp is lit, and the far door is round the last bend.
  - It is a second-tier physical trial with its own mark.
- **Walking.** A reader who walks the whole route is never moved back.
- **Sprinting.** Sprinting through the second length puts the reader back at its start, one length back, facing the same way. Walking it again, they go on.
- **Jumping.** A jump in a length counts as hurrying it.
- **Watching.** While another reader watches, nobody is moved back. Once nobody can see, the move back owed is made.
- **Free lengths.** Sprinting through the first length moves nobody back, and nor does sprinting through the last, to the far door.

## What stays

- **Witness:** 81 / 61 / two kinds / three endings.
- **Protocol:** 163.
- **Layout:** 127. One appended slot (226). New builds only.
