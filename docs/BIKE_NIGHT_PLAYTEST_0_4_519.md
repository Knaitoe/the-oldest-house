# Bike night: four notes from playing it (0.4.519)

The owner played Bike night on November 12 and sent frames with notes. Four are fixed here. Two more need more than a fix and
come next: the bike's handling on the Hollow's jumps, and the Hollow seen from the cemetery.

## "the animation for the boys on their bikes needs to be fixed"

The frame: one of the three, at the park's gate, standing bolt upright over his bike, his legs swinging out in front of the bars.

**Why.** A bike carries its rider with their hips on the saddle. The rule worked this out for a reader: the game sets a passenger
at the bike's point less the passenger's own "vehicle attachment", and a reader's is six tenths of their height up. The rule added
that back. One of the House's people has none, so the three and Gus were carried a third of a block higher than a reader: their
feet where their hips should have been. And the three's bikes and Gus's took the saddle height of Borrasca's bikes, a pixel and a
half higher than the Wasp's frame drawn under them.

**Now.**

- `RiddenBike.getPassengerAttachmentPoint` takes each rider's own attachment, and each kind of body's own hips
  (`READER_HIPS`, `CAST_HIPS`). A reader is carried where they were.
- `PackBike.saddleOf` gives a bike of each look the saddle drawn on it.
- `client/BikeRiderPose` poses somebody on a bike from the bike's own shape:
  - they lean forward a little from the hips, which stay on the saddle;
  - each hand is on its grip;
  - each foot follows its own pedal round. A leg has no knee, so it is drawn a little shorter or longer to meet a pedal that
    has come up close or gone down far.
- The whole of them leans with the bike about the line its wheels touch the road, as a reader does.
- The numbers (the pedals from the axle, the Wasp's grips) are the art tool's. `tools/draw_bike_night_art.py` checks the game's
  against its drawing.

## "the benches also don't have the initials referenced in descriptor text. both benches should have unique text"

The park has three benches. The two plain ones said that initials had been cut and cut out, and had nothing cut in them. The
carved one had a heart and some strokes that were not letters.

Each is now told apart by something drawn on it, on the end a reader standing before it reads first, and says so:

- **By the bin:** two places on its back where a pair of initials has been shaved off to the raw wood, and the cross that was
  between them still there. "A park bench. Somebody has cut initials into its back, and somebody else has cut them out again. The
  cross that was between them is still there."
- **By the playground:** R D, a heart, and on its other end A L, in letters that are letters. "A park bench. Cut into its back
  long ago, and gone the same weathered way as the wood: R.D., a heart, A.L. Nobody has cut these out."
- **Nearest the gate:** no cuts. The end of its seat nearest the gate is worn pale. "A park bench. Its seat is worn smooth at the
  end nearest the gate, where somebody has waited a great many evenings."

The cuts are a weathered grey, lighter than the wood: on the proof's first drawing the old dark grey did not read on the dark top
slat. One new fitting kind, `BENCH_CUT` (a bench's right end with its mark), drawn by `tools/draw_calder_art.py`.

## "you should have the line curve towards the window. last person should be facing the ticket window"

The six outside the theater stood in a straight row down the middle of the pavement, all looking east. The first of them was past
the ticket window with their back to it.

Now the head of the line stands an arm's length from the ticket window and looks at it. The rest come along the pavement from the
west, each a step behind the one ahead and looking at their back, and round a quarter turn in to the window
(`CalderPeople.lineSpot`, `lineLook`). They still part for a rider on the pavement and close up behind them.

## "the mirroring on this crate doesn't work too well"

The milk crate's lattice had a bar every fourth pixel of a side ten wide. So a side had a post at one end and none at the other, a
corner was a post from one side and a gap from the next, and the walls had no inside.

Now:

- a side reads the same from either end, so the far wall's lattice lies exactly behind the near one's;
- there is a post at every corner;
- each wall has an inside and a rim;
- the floor has its own bars.

## Checked

- The Bike night namespace, 103 of 103, with the cases extended:
  - the three sit on their saddles;
  - the three benches, their marks and their lines;
  - the head of the line at the window, looking at it, and each of the rest looking at the one ahead.
- The quick checks.
- The Bike night proof, 31 pages, with a new page of the three and Gus on their bikes. Looked at: the riding page, the benches,
  and the crate on the alley page.

## Not seen

- None of it moving in a client: the riders pedalling and leaning through a turn, the line parting and closing round the bend.
- The benches at eye level in the park.
