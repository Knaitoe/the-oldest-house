# The Clockwork Attic (0.4.362)

This is item 4 of the owner's "New Puzzle and Hazard Room Designs". The owner's notes ask for:
- stopped clock mechanisms and pendulum weights;
- starting one clock to start a slow, visible chain that opens or closes routes;
- every mechanism visible and started or stopped by hand, at a fixed and readable speed, and big enough to see through windows and
  hatches;
- trains that, left running, block or unblock a door, raise a ladder, or drop a counterweight another train needs.

**The early room.** One long train runs from a master clock to a final lever that raises a ladder to the exit hatch, and can be
stopped and restarted anywhere.

**The deep version.** Three interlocking trains. Starting the wrong one first leaves a blocking weight in the correct train's path,
so the reader watches through windows, stops the trains and runs them again in order. Everything rewinds by hand.

**The mild hazard.** A train left running keeps going while the reader is away.

The release adds a new puzzle family, CLOCKWORK, with two rooms:
- the clockwork attic (`CLOCKWORK_ATTIC`, slot 218, early);
- the clockwork attics (`CLOCKWORK_ATTICS`, slot 219, deep, from the strange depths once the clockwork attic is solved).

Layout 117, protocol 156. The rooms follow `PuzzleRooms`' pacing:
- they are offered only after a story, and rarely;
- nothing hurts, counts down or is used up;
- everything winds back by hand;
- solved stays solved, for everyone;
- no Witness credit.

No Witness or source-kind change: 80 / 60 / two kinds / three endings. Code: `Clockwork`, `ClockworkBlock`.

## How a train works

**What a train is.** A master clock stands at one end, with its case below and its face above. A row of brass gears runs along the
wall at head height and ends at a heavy iron lever.

**Running.**
- **Start and stop.** A hand on the case starts the pendulum swinging, or stills it.
- **The pace.** While the pendulum swings, the train moves on one tooth every half second, the same speed for every train.
- **The motion travels.** The motion goes down the row one gear at a time. Each new gear engages with a clack, and every gear it has
  already reached turns with it. The face's hands go round as it goes.
- **The end.** When the motion reaches the end, the lever seats with a heavy click and does that train's work. The pendulum stops.

**Pins.**
- **Pinning a gear.** A hand on a gear pushes an iron pin through its teeth, and another hand draws it out.
- **Ahead of the motion.** The train runs up to a pinned gear and stops there.
- **Behind the motion.** A pin pushed into a gear the motion has already reached stops the whole train at once.
- **Restarting.** Drawn out, the pin lets it go on. So a train can be stopped and restarted anywhere.

**Winding back.**
- With the pendulum stilled, a hand on the face winds the whole train back to its start, every gear at once, with a ratchet.
- Wound back from its end, the lever comes up again and what it did is undone where it can be.

**Left running.**
- A train left running keeps running while nobody is there. Each train keeps its count, its pins and the time it was last looked at,
  for everyone.
- When someone comes back, the room counts the teeth that have gone by and catches up, and whatever the train's lever does has
  happened while they were away.

**What stops a train.** Only a pin, or in the deep attics the fallen weight. Nothing makes a train faster.

## The clockwork attic (early)

An attic of stripped spruce under a dark roof, with beams across it and lanterns hung from them.
- **The clock.** The master clock stands by the door.
- **The train.** Sixteen gears run along the east wall to the lever, about twenty-four seconds of motion.
- **The loft.** It spans the far end, with the far door in its gable. Its floor has a hatch over the partition beneath it.
- **The ladder.** There is no ladder. The lever lets one down from the hatch: "Somewhere a lever seats with a heavy click, and a ladder
  comes down from the hatch into the loft."
- **The start.** The ninth gear starts pinned, so the first run stops halfway until the pin is drawn.
- **Solved.** The room is solved when the lever seats. After that the ladder stays down even if the train is wound back. Built
  again, the train stands at its end with the ladder down.

## The clockwork attics (deep)

A wider attic. An entry hall leads to a corridor down the west side, and three bays open off it, A, B and C, each with its own clock
and train along its east wall. Windows in the corridor wall and between the bays let a reader watch one train from beside another.
C's bay ends under a loft with the far door, reached by a hatch as in the early room.

What each lever does:

| Lever | What it does |
|---|---|
| A's | Swings an iron catch out under the chain of a weight that hangs over C's train. |
| B's | Draws up the bars across the doorway into C's bay; once up, they stay up. It also lets go of the weight. If A's catch is set, the weight drops an inch onto it and holds. If not, it comes down into C's third gear, too heavy to lift, and C stops there whenever it reaches it. |
| C's | Lets the ladder down to the loft, and the room is solved. |

**Run B first** (to open the way into C's bay) and the weight is down before C has run. C stops at the weight. To clear it:
1. Wind B back. Its rope hauls the weight up again.
2. Run A. Its catch swings out.
3. Run B again. The weight now holds on the catch.
4. Run C through to its end.

**Changing A later.** Winding A back while B's weight hangs on the catch lets the weight fall.

**Built again once solved,** every train stands at its end, with the catch set, the bars up and the ladder down.

## Assets

- **The block.** One new block, `clockwork_fitting` ("Clockwork"). Its kinds are:
  - the clock's case and face;
  - a gear and a pinned gear;
  - the lever;
  - the weight;
  - the catch;
  - the bars.

  It has a facing and a stage from 0 to 3.
- **Art.** Drawn by `tools/draw_clockwork_fittings.py` in vanilla's manner, seventeen textures:
  - an eight-toothed brass gear in four positions a quarter tooth apart, so stepping through them turns it;
  - the pin;
  - the case's glazed front with the pendulum still, swung left and swung right, and its sides and top;
  - the face with its hands in four places;
  - iron, the lever's grip, and the bars.
- **Sounds.** Six subtitled sounds are synthesized by `tools/generate_clockwork_audio.py`:
  - "A clock ticks";
  - "Gear teeth engage";
  - "A lever seats heavily";
  - "A ratchet winds back";
  - "A weight drops";
  - "Bars draw up".
- **Checks.** CI and the quick gate check both tools with `--check`.

## Tests

ClockworkTests (exploration namespace, batches `clockwork_attic` and `clockwork_attics`).

`theTrainRunsDownTheAtticToTheLadder`:
- the pendulum drives the train to the pinned gear, and it stops there, the gears beyond unmoved;
- a pin pushed into a gear already turning holds the train even with the first pin drawn;
- drawn, it lets the train go on;
- with the reader disconnected nothing is counted, and on their return the train has run on;
- at the end the lever seats, the ladder comes down and the room is solved;
- winding back keeps the ladder;
- built again, the room is solved;
- nothing is earned.

`theWeightComesDownUnlessTheCatchIsSetFirst`:
- B run first draws up the bars and drops the weight into C's gears;
- C runs only as far as the weight;
- wound back, B hauls it up, and the bars stay up;
- A sets the catch;
- B again leaves the weight on the catch;
- C runs to its end and the ladder comes down;
- built again, the rooms are solved;
- nothing is earned.
