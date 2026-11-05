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
