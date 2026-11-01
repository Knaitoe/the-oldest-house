# The dark halls (0.4.208)

The owner, 1 November, answering the proposal of a third series of ordinary halls on the road to the great staircase: "Let's
change the staircase to be offered at say, 22, and have dark halls range from 16 and start being the only halls at 21. Yes, the
house should occasionally take torches. Maybe sync that with a low rumble, separate from the Minotaur's own. Make Minotaur roars
more frequent in deep halls, but not excessively so. Your proposed five halls are good." The echoes were to be used sparingly.

Five new gray slots, 134 to 138, in the interior. Layout 64; no protocol change. No Witness source: still 61 sources / 46
required / two kinds / three endings.

## Where they sit

- **Domestic halls.** Up to depth six.
- **Stone halls.** From depth seven, as before, and gone from depth twenty-one.
- **Dark halls.** From depth sixteen (`DarkHalls.DARK_DEPTH`), weighted up as the band goes on, and the only ordinary halls from
  twenty-one (`DarkHalls.ONLY_DEPTH`). The edge, the last of the series, comes only from twenty.
- **The great staircase.** Offered from depth twenty-two (`LabyrinthPacing.STAIRCASE_DEPTH`, was twenty). That lengthens the road
  down without adding a rule, which answers the October note that the staircase comes too easily.

Hazards, anomalies, rests and stories still come by their own pacing in the dark band. Only the ordinary ways on change.

## What they are like

The house the book describes: smooth ash-grey walls, no fixtures, no light, cold. Three materials (ash wall, floor and ceiling)
are drawn by `tools/draw_dark_hall_art.py`. The polish pass leaves them alone, so nothing lights or furnishes them; the reader's
breath fogs now and then. Each hall is long, with forks, short real dead ends and ceilings that rise as you go. There is always
one true way on, and the way back is always open.

1. **The long hall** (`dark_long`). Three wide and 104 long, its ceiling rising from three to eight as it goes, and nothing else.
2. **The forking hall** (`dark_fork`). A way in and a cross passage, then three branches north. The west and middle branches end
   in rock; the east goes on.
3. **The ash maze** (`dark_maze`). Eleven by eleven cells a block wide, carved by a fixed walk so every reader meets the same
   maze, with two ways out at its far corners.
4. **The spiral** (`dark_spiral`). One block wide round a solid core, a block lower every six steps on stairs that climb back
   just as well. It passes under itself twice and comes out ten blocks down, to the north.
5. **The edge** (`dark_edge`). A walkway three wide along the west wall of a great room nobody can see. Beside it, with no rail,
   is a drop of thirteen blocks.

The spiral and the edge sit lower in their slots (the floor fourteen up, as the drowned town and the barn well do) so their depth
stays inside them.

## What the House does there

- **It takes torches.** Each time a reader walks into a dark hall, the House decides whether it will take a light there: about
  one hall in three, never two halls running. It takes one of the lights that reader set in that hall, and only when:
  - they have set at least two;
  - the light is ten blocks or more from them;
  - no reader is facing it, within 128 blocks.

  There is no drop. Its low rumble (`dark_hall.rumble`, stone settling, with no voice or breath in it, nothing like the growl) is
  heard by that reader and anyone near. It takes one a hall at most.
- **The Minotaur is heard more often.** In a dark hall the growl comes on average every two and a half minutes (from a minute
  and a quarter to three and three-quarters), against about four in the stone halls. A schedule set further out than that is
  pulled in when the reader enters. It is more often, but not twice as often.
- **Echoes, sparingly.** At most one a reader every four dark halls, on a coin toss:
  - **footsteps** behind them in the long hall and the spiral, while they walk, for six seconds, stopping when they do;
  - **a door slamming late**, behind them, four to six seconds after they cross in;
  - **the drop**: at the edge, a thing the reader throws over is gone, and two in three times it is heard landing, faint and
    much too late. This one is the reader's own doing, so it is not counted against the echoes.
- **Going over the edge.** A reader who goes over is put back at the start of the walkway, on their knees, unhurt; they never
  land.

All of it is personal: each reader's torches, echoes and rumble are theirs. The halls themselves are shared, as every ordinary
hall is. Four subtitled sounds come from `tools/generate_dark_hall_audio.py`: the rumble, footsteps, a late slam and a thing
landing.

## Advancements

The journey tab gains **The dark halls** ("Smooth grey walls, no fixtures, and no light but what you brought."), earned on the
first dark hall, between Stone and the staircase's depth. That step is now **Twenty-two doors deep** and follows the staircase
depth rather than a fixed twenty. The tab has 27 advancements.

## Tests

Four `DarkHallTests` in the exploration namespace:

- every dark hall can be walked to every way on, and has no light of its own;
- the band begins at sixteen, is the only way on from twenty-one, and the staircase waits for twenty-two;
- the House takes a light only where nobody can see it, one a hall, without a drop;
- the edge has its drop, and the Minotaur is heard more often in the dark, but not twice as often.

The first local run found the House taking a torch the reader was facing from 54 blocks away, because "seen" stopped at 48. A lit
torch in the dark is visible much farther than that, so the cone now reaches 128 blocks.

The five horrors that are dealt in this band are the next releases (docs/DARK_HALL_HORRORS_PROPOSAL.md).
