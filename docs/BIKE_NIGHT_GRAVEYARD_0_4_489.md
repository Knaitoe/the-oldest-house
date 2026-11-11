# Bike night 20: the graveyard's ground (0.4.489)

The proposal (`docs/BIKE_NIGHT_PROPOSAL.md`, "Act three: the graveyard"):

> **The graveyard.** It sits on a rise at the edge of town, inside an old iron fence.
> - **The paths** are gravel.
> - **The obstacles:** headstones at every lean and height, and taller memorials: an angel; an obelisk; a family mausoleum
>   with iron doors; a fresh open grave with a mound and a tarp, which is a hazard to jump.
> - **Riding it:** riders weave and hop between the stones. A stone is a wall: a glance off it is fine, a head-on hit puts
>   the rider over the bars.
> - One row of small headstones near the back all carries the same autumn date, years before, with names and nothing else.

This slice is the ground and what stands on it. What happens there is the next: the gate standing open, the mist, the
three falling back, Gus at the row, the town's lights going out. Nothing here moves and nobody is hunted in it yet.

## The hill

Calder Hill Cemetery is the whole width of the town, north of the railings the town already had (0.4.481), and eighty-eight
blocks deep. **It rises three blocks from the gate to the back**, in three banks. A bank is a row of half blocks with the
ground a block higher behind it: anybody walks up one, and a bike rides up one without losing its way.

The gravel is one path north from the gate and as wide as the gate, four paths across it (one to a level), two up the
sides, and a spur to the mausoleum's doors.

**The fence is joined all the way round** ("which must be connected properly, unlike the Corner's fence"). The town's front
railings end in a pier at each corner; the side railings start from those piers; a pier stands on every bank, a block
taller than the rest so that it meets the rails below it and above; the back has its own corners and a gate between two
piers in the line of the front gate, shut. Behind the back railings the ground goes on a few blocks under trees.

## What stands in it

- **Twelve rows of stones** across the hill, so that riding north is riding through them. A row is not full: a place a
  row would have and has not is a gap to ride through, and the rows fill toward the back (half the places of the first
  row are empty; a tenth of the last). **The line of the gate's path is clear in every row.**
- **Sixteen headstones**: a round-topped tablet, a square one under a cap, a pointed one and a low wide marker, each in
  four stones (granite, slate, marble gone grey, sandstone), and of each standing shape one leaning back and one or two
  to a side. Lichen on all of them, moss at the foot. About one stone in eleven is a cross instead, of stone or of wood
  and leaning. Things are left at some: flowers in a jar, a wreath, a small flag, a lantern with its candle lit, which
  is the only light the cemetery has.
- **The angel**, on the third level west of the path, and **the obelisk**, on the second level east of it: three blocks
  each.
- **The Calders' mausoleum**, on the top level to the east: marble, solid (nobody goes in), its roof oversailing its
  walls and stepped to a ridge, and two iron leaves set back in its south wall, studded, with a ring in each and a
  grille over it.
- **The open grave**, where the gate's path ends: two wide, two long and two deep, its heap at its south end, a
  tarpaulin thrown over the rest of the earth on one side and one pegged out flat on the other.
- **The row of small stones**, behind every other row: six, three on each side of the gate's line, each with **1957** on
  its face large enough to read from a bike.
- Trees, inside the railings and behind the back ones.

## What a stone is to a bike

A bike takes a step a block high in its stride, and no headstone is a block high. So **to a bike a standing stone stands
a block and a half**, as a fence does to everybody: it is a wall, a glance along it keeps most of the bike's way and a
head-on hit above the bike's own crash speed puts the rider off, by the rule a wall and a parked car already have. No
hop clears it but the Hopper's held to its fullest. A stone that leans is in the way where it stands, not where it
leans to.

**A low marker is half a block to a bike as to anybody**: it is hopped, or it is a bump. It does not stop a bike and it
does not put a rider off. (The owner's to judge: whether riding over one without hopping should cost anything.)

To anybody on foot a stone is as high as it looks and can be climbed.

## The heap

"The mound is the ramp." The heap is ridden as one slope, as a plank ramp is (`BikeSurface`), a little under two thirds
of a block up in one block along, and a bike that leaves its top still climbing is thrown by its own speed. **Flat out,
a plain bike comes down a little under three blocks on**, which is over a pit of two and the bike's own half length
with something to spare. Much under nine tenths of that and it goes in.

The first pit was three long and a plain bike flat out landed in the far end of it (its lowest over the pit was 2.85,
where the grass is 3): the first run of the case said so, and the pit is a grave's length now.

Going in is a drop of two blocks and three quarters from the heap's top, which is not a hard landing by the bike's own
rule: the rider is still on. A step of earth is left at the north end, so the way out is two rises of one block: a bike
rides up them and anybody jumps them. "Falling in costs time and nothing else."

## What is cut on them

A stone's face is sixteen pixels, with room for an emblem and the lines of a name, and for a year only on the low
markers and the small ones. So **a hand on a stone is told what is cut on it**, in the grey italic every other thing in
the park answers in.

- The small row: `LUCY ARDEN. OCTOBER 31, 1957.` and five more, names and the date and nothing else.
- Every other stone: a given name and a family's, two years, and for about half a line under them (`AT REST`,
  `UNTIL THE DAY BREAK`). It is made from where the stone stands, so the same stone says the same thing to everybody
  and in every copy of the town. Six places of a row are one family's plot, so neighbours mostly share a name.
  A low marker's last year is the one on its face. A wooden cross has a name and a year.
- **Nobody under a stone here died young** (twenty-one at the least), and none later than 1979. The only children in
  this ground are the row's. The row's year is on nothing but the row.
- The obelisk is Ambrose Calder's, who built the mill; the angel is his wife's; the mausoleum has `CALDER` over its
  doors, which is said and not yet drawn.

The names are original and not the proposal's: it asks for "carved names and dates (original, period-plausible)".

## The place

The place's room is longer: to 272 blocks north of the park's wall, where it was 184. Nothing else about the place
changed; it is still not dealt.

The line of trees the town had behind its railings is gone: the cemetery is there, with its own.

## Cases

Four, in `BikeNightTests` (71 in all):

- the hill: grass, gravel on every path and half blocks up each bank; the side and back railings whole, two high, with a
  pier at each corner and a taller one on each bank, and the back gate shut between two piers; stones of all sixteen
  kinds, low ones among them, and crosses, each on grass, off the paths, its name looking down the hill; the gate's line
  clear in every row; the rows fuller toward the back; things left at some, a lantern among them; the small row's six in
  their places behind every other row; the angel, the obelisk, the mausoleum's four quarters of door with stone behind
  them; the pit, its step, its heap and its tarpaulins; nothing vanilla but earth under the grass and the trees' trunks;
- a hand on each small stone is told its name and the date; on any part of the angel or the obelisk, and on the doors,
  what they say; the heap says nothing of the kind; and every other stone's inscription is a name, two years and
  perhaps a line, of somebody who lived twenty-one years at least, in the town's own names, never the row's year;
- a bike rides from the gate up the path to its end, three banks up, never under four fifths of its top speed on the
  hill; over a low marker, half a block up and down again, still ridden; and flat out into the back of a standing
  stone, where the rider comes off and the bike gets no further than the stone;
- flat out up the heap, a bike is thrown over the pit and never lower than the grass while over it; from standing two
  blocks short, it goes in, its rider still on and not hurt; and it rides out by the step.

What the runs before they passed found, all of it the cases' own but the pit:

- the pit was a block too long (above);
- the riding case measured a bike's middle against the stone's block and not its face;
- the hands case asked what was heard in the tick it clicked, where what is said arrives the tick after (the park's
  own hands case waits, and this one does now); and it took the oldest low marker's year for the first year anybody
  died in, where that is 1880;
- taken straight from the bump over a low marker to a run at a standing stone, the case's rider reached the stone at
  under the crash speed and stayed on, pedalling at it. Why it was slow was not found: the likeliest is that it came
  down off the marker leaning and the case's pedalling, which pushes one side to right a lean, never got it going
  again. The cases brake to a stop between their parts now, so each part starts upright from standing. If a bump does
  leave a rider wobbling, that is the bike's own rule for a landing and is some of what not hopping costs;
- the pit case's slow rider stopped pedalling at the foot of the heap and stood there. It is pushed whenever it is
  going slower than a walk now.

## Seen, and not

Two pages are added to the client proof (the cemetery's things, each kind once; a strip of the hill as the builder puts
it together), and both were looked at. What they changed: the grass had a leaf on every other pixel and has a few now;
the marble's one vein, repeated on every block, is two faint ones that run on from block to block; the angel's wings
are swept back from its shoulders; and the heap, which was four steps and read as a stair, is eight lumps of uneven
width with clods on them, lit evenly, in an earth with no lines in it. That last change (the even light) was made
after the last proof run and has not been looked at.

None of it has been seen in a client from a bike, at night. The decisions that are the owner's:

- how many stones a row has, and how the rows fill (`CalderGraves.EMPTY`);
- whether a low marker should cost a rider who does not hop it;
- the names, the two Calders and what they are said to have done;
- that the mausoleum is solid.
