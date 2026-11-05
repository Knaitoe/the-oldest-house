# The Sphinx, first slice: the room you just left (0.4.306)

The owner's draft is the Drive document "The Sphinx" (4 November 2026). It describes four crossings, a dream and a reward. This slice
builds the first crossing, in the early domestic halls, from end to end. The other three crossings, the co-op survey pins, the dream
and the Dream Stela fragment come in later slices. They read the per-reader record this slice keeps.

## The place

`LabyrinthPlace.SPHINX_VESTIBULE`, slot 182, gray, layout 93. It is dealt as a rare detour, weight 2, from the third crossing until the
halls turn to stone. The way onward is always open past the hall's east end, so nobody has to answer her.

- **The vestibule.** Quarry tiles and limewash. An umbrella stand stands on one of two marked tiles (brass studs) and can be dragged to the other.
- **The hall.** You reach it through a short corridor, with a grille in its wall. The hall has three spruce doors in its north wall. Beside each door:
  - a letter flap set low in the wall: open, it is a frame round a real hole you can crouch and look through;
  - a latch on the wall;
  - a stop pin, hidden flush in the door's stop.
  The doors have no handles on this side and open only by their latches.
- **Behind each door.** A copy of the vestibule. The **live** door is chosen at random the first time the room is built and saved.
  - Its chamber's stand moves whenever the real stand is moved.
  - The two staged chambers' stands never move, one on each tile. So exactly one view other than the live one agrees with the vestibule at any time, and only moving the stand tells them apart.

## The rules she keeps

- **The question.** "Which of these doors opens into the room you just left?" She asks it as a reader first comes into the hall at a meeting. The voice is positional and subtitled, and the line is shown in chat. The grille in the corridor (the safe approach) asks it again.
- **Testing.** Move the stand, lift the flaps, look. In co-op, one reader moves the stand while another watches the flaps.
- **Answering.**
  - The live latch opens its door. Stepping through brings the reader out of the vestibule's own south wall, beside the door they first came in by, where there was plain wall. The vestibule's east wall also opens on the way onward. Solved, the room stays open for everyone, and rebuilding keeps it so.
  - A wrong latch catches at its stop. She says "Not that one. Leave that one shut," and that door's stop pin is exposed. A retry still opens the live door.
- **Forcing.** Pulling an exposed pin is a second, distinct act, and the only thing that brings her:
  - falling grit and a breath far too large for the room;
  - after two seconds her fingers (sandstone skin, a painted band) fold over that door's frame and the door bows open;
  - anyone in the hall within six blocks of that door hears her breathe, sees grit fall on them, and a second later is struck (three hearts, knocked back), again every two seconds while they stay;
  - the corridor sill back to the vestibule is as far as she reaches.
- **Retreat.** Once the hall has been empty for five seconds she withdraws: the fingers go, the door returns to its stop, the pin goes back in, and the room is its question again.
- **Pressing through.** Walking through her door brings a reader out in the onward corridor, and that door seals behind them as cracked plaster.

## What the House remembers

The room's state is shared (`sphinx_0306` → `Room`): Live, StandB, Solved, Warned, Provoked, Sealed.

Each reader's record (`sphinx_0306` → reader → `early`):
- `Heard`
- `First`: 1 right, 2 wrong; the first deliberate answer only
- `Solved`: a later correct answer still earns the passage
- `Walked`: counts leaving the place with the question heard and unanswered, while she is not provoked
- `Forced`: only the reader who pulled the pin
- `Retreated`: marks a retreat from her attack, which does not count as walking away

Disconnecting does not count as walking away. Nothing counts toward the Witness.

## Assets

- `tools/draw_sphinx_fittings.py` draws five materials and the `sphinx_fitting` block in vanilla's manner (16x16, small palettes with per-pixel noise): stand, flap shut/open, latch down/lifted, pin hidden/exposed/pulled, grille, fingers.
- `tools/generate_sphinx_audio.py` makes seven synthesized, subtitled cues: voice, breath, latch, catch, pin, strike, scrape.
- Both are checked in CI.

## Tests

`SphinxTests` (exploration) plays the crossing with two readers: the question on entry; doors refusing a hand; only the live view following the stand; the flap's hole; a wrong answer caught, warned and recorded; the retry opening the live door; the step through coming out of the south wall; each reader's first answer kept apart; the pin pulled and owned; the fingers; a strike in reach and none past the sill; withdrawal; and the open way surviving a rebuild.

## Revision after review: her stature (0.4.306, second pass)

The owner, on the first pass: the rooms she is in should have ceilings that feed into darkness, and she should peer down from the dark.
The first pass met the mechanics and missed what the encounter is for. "A voice asks a question from somewhere the room has no space to
contain": she is not in the hall. She lies in the dark over the chambers, on the far side of the doors, and her weight bows a door
*toward* the reader.

- **The hall stops making sense.** The vestibule keeps an ordinary ceiling. The hall rises eighteen blocks; above head height its walls
  are dark plaster the lamps never reach, and the lanterns hang on chains that go up out of sight. Its north wall stops at height 4
  (the doors and their fittings need 0-2). Over the wall top, and back from it, is one dark space over all three chambers: hers.
- **Her scale, fixed for every crossing:** an eye four blocks wide and two deep, fingers two blocks long, a hand that comes down from five
  above. The later crossings use the same proportions.
- **The eye looks down.** It lies face down over the top of the wall, its front row leaning out over the hall, so looking up from anywhere
  in the hall finds it face-on. Only the eye is lit (light 8); beyond the lids its skin fades into the dark and then nothing, so no block
  edge shows. One eye, no face.
- **Paced reveal.** On entering the hall: her voice from over the frames, nothing seen. A wrong latch: the warning, and the eye opens over
  that door and looks down for seven seconds while grit runs off the wall top in front of whoever is at the latch, and her breath comes
  from above; then it closes. Pulling the pin: grit, the eye opens and stays, her fingers come over the top of the wall and down its hall
  face to fold over the frame, the door bows. A strike: grit and breath on the reader, then her hand comes down out of the dark over them
  for a moment.
- **Going through is deliberate.** Only walking into the chamber beyond a door (her door, or the live one) carries a reader through;
  standing at the door, even in its open doorway, does not. Her reach runs up to and includes the doorways.

What the client walk caught that the tests had not: the first eye (2x2, set back) was hidden behind the wall top except within a
degree or two of pitch at the very back of the hall; a vertical eye seen from below was a sliver; the underside's rows render swapped;
the skin round it read as a blocky panel; a two-second look was over before anyone would look up; a spruce stripe ran along the walls
at height 4; standing at a bowed door carried a reader through it; the stop pin's hit box was too small to find. Each is fixed, and the
exploration case now checks the eye is in sight from the middle and the back of the hall with margin, that standing in her doorway is
struck but not carried through, and that the eye opens on a warning and closes again.

## Second review (owner): she moves, she does not wound; claws; a following eye

- **She never hurts anyone.** A reader who stays in the hall within her reach is warned (her breath, grit falling on them), then her
  foreleg comes down out of the dark over them and her paw sweeps them back through the doorway to the corridor sill, the line she
  stops at. Recorded per reader as `Swept`. The sill is still her limit; she keeps it by moving people, not wounding them.
- **A paw, not fingers.** She is lion-bodied. Over the door frame comes a paw: a furred heel, four heavy toes, and dark hooked claws
  curling down into the wood. Down the wall comes a thick furred foreleg.
- **Her pupil follows the reader.** The eye is its own block (`SphinxEyeBlock`): a tile (one of eight) and a gaze (one of nine:
  left/middle/right by away/under/toward the hall). The whole picture is drawn once per gaze. While it is open, the server turns it every
  four ticks toward the nearest reader, so wherever someone stands in the hall she is looking at them. The exploration case walks a reader
  west and then east along the hall and requires the pupil to follow.
