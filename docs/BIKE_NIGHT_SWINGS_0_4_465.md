# Bike night 3: the swings and the sand (0.4.465)

The third slice of Bike night (`docs/BIKE_NIGHT_PROPOSAL.md`). The owner's notes:

> The vignette are dorks at night. Kids on a playground maybe? Lots of fun potential for unique interaction here - a seesaw, slides,
> swings, monkey bars, etc

and, of the bullies who come later: "players throw balls of sand".

This slice puts the first equipment on the playground and gives the sand its use: four swings that are pumped and jumped off, with
the furthest landing of the night marked in the sand, and balls of sand that are packed, thrown and burst. The rest of the
equipment comes next, then Gus.

**It still cannot be finished, so the House does not deal it.** Nothing is a Witness source.

## The swings

A steel frame stands along the playground's west side: three posts on the sand, each with a stay either way the frame is pushed,
and a bar over them four blocks up. Under the bar, two to a bay, hang four belt seats on chains, looking east over the open sand.
A lamp of their own stands behind them on the grass.

- **Getting on.** A hand that holds nothing, on a seat, sits on it. Every seat takes a reader, and readers swing side by side.
  Nobody gets on a swing somebody else is on.
- **Left alone** a swing comes up to a gentle arc by itself, as the porch's does.
- **Pumped**, it goes higher. Leaning forward while it goes forward, or back while it goes back, widens the arc; leaning against it
  narrows it. At its widest the seat is carried over two and a half blocks up at each end. A reader is told this once, the first
  time they are on one.
- **Letting go.** Jump lets go of it, as on the porch: the reader leaves from where the seat is, with the seat's speed and a push
  of their own. Off a high arc, on the way up, that is a long way.
- **Getting off** any other way stops the swing where it hangs.
- Its hangers are heard each time it comes through the bottom ("Swing creaks"), louder the higher it goes.
- **As the others see them**, a reader on a park swing goes over with it, about their hips, the way its chains do, and their
  hands are up on the chains (`SwingClient`). A reader does not see themself, and their own view is not turned.

### The jump

Where a reader who let go comes down is paced out from under the bar, in a child's paces (two to a block), and they are told how
far. It only counts on the sand. The furthest anyone has landed tonight is kept, and is there to see: a heel mark dug into the
sand where they came down, across the way they were going. A shorter jump is told how far the furthest is; a longer one moves the
mark. A hand on the mark says whose it is. The mark gives way to whatever is set down on it, as fallen leaves do, and is gone when
the night is over. A reader's own record keeps their best.

**The playground's sand is soft.** Nobody is hurt coming down on it, off a swing or off anything else. Off the sand a fall is a
fall.

### How the swings are made

The porch's swing (0.4.172) and the park's are one thing in the code (`HouseReadSwing`, `SeatEntity`): the same length, the same
rate, the same letting go. What differs is which blocks hang one (the porch's plank and ropes, two blocks wide; the park's belt and
chains, one), and that a park swing's arc is as wide as its rider has made it. The seat keeps that width and tells clients, and
where the seat is on the arc still comes from the time sat down, so nothing else is sent. The porch's swing and the swing in a
Drisking yard behave exactly as they did; their cases still pass.

One rule changed for every swing: a reader is kept off a swing by that swing's own moving seat, and no longer by any seat within
three blocks, which would have let only one reader use the park's four.

## The sand

- **Packing.** A hand in the playground's sand packs a ball of it: one at a time, eight at most ("Sand is packed"). It is the
  night's one item.
- **Throwing.** Using it throws it the way the reader looks ("Sand ball is thrown"). It is heavier than a snowball and a child
  threw it: it does not go far.
- **Bursting.** It bursts on whatever it meets, in a puff of sand ("Sand ball bursts"), and hurts nobody. A reader on their feet
  is knocked a little the way it was going. The failing lamp, hit while it is out, comes back on, as a kicked one does.
- **A hand with sand in it is throwing.** It does not stop to look at the bench it is aimed at; only the sand itself (to pack
  another) answers it.
- **It does not keep.** Let fall, a ball falls apart where it lands. Carried out of the night, it runs out through the fingers.
- A reader's record keeps how many they threw and how many met somebody.

## How it is drawn

`tools/draw_calder_play_art.py` draws the playground's equipment as the park's fittings are drawn: bars that take one swatch of a
paint card (the playground's own: the frame's paint and where it has worn to steel, chain, rubber, bolts, and the paints of the
equipment to come), and pictures for the chain's links, the heel mark and the ball of sand. Sixteen pixels to the block. It
refuses a model in which two differing faces share a plane; it caught one (a post's foot and the collar it stands in).

Four more sounds from `tools/generate_bike_night_audio.py`.

## Tests

`BikeNightTests`, three more cases, each building the place itself in a batch of its own (nineteen in all).

- **The swings hang from their frame, and two readers swing side by side.** Every block of the frame: each post's foot, shaft
  and top, the bar and where its hangers are, each seat and its chains, the lamp behind them; what stops a body and what does not.
  A hand on a seat sits on it; the still seat and chains give way to the moving one; nobody else gets on it, at rest or away from
  where it hangs; the next swing along takes a second reader while the first swings; left alone each comes up towards a gentle
  arc and the seat is where the arc puts it; the reader is told once how it is taken higher; the hangers are heard; off it, it
  hangs still again and the one beside it swings on; a seat left as if somebody were on it is put back with the night.
- **A swing pumped high throws its rider out over the sand, and the furthest is marked.** Leaning with it takes the arc to its
  widest and no wider, the seat well over two blocks up; leaning against it narrows it; letting go on the way up throws the reader
  up and out; they come down on the sand more than five blocks from under the bar, by the game's own falling; the distance is
  paced, told, kept in their record and marked where they landed; nobody is hurt on the sand; a hand on the mark says whose it is;
  a second jump off a gentle arc is shorter, is told how far the furthest is, and leaves the mark alone; the account has it; the
  mark goes with the night.
- **Sand is packed into balls, thrown, bursts, and is gone outside the night.** One at a time, eight at most, from the playground's
  sand and no other ground; using one throws it the way the reader looks; it bursts on another reader, who is not hurt and is
  knocked a little the way it was going, and is heard; the thrower's record has the throw and the hit; the failing lamp, hit while
  it is out, comes back on; with sand in the hand a bench is not looked at; a ball let fall falls apart; carried out of the night
  the sand is gone and the reader is told.

The case for what a hand on the park's things says now tries the swings' frame too, and reads every new line for the House's
rules (no colour, no gendering of the reader, nothing about how they feel).

The porch's swing and the Drisking yard's have their own cases, and those pass on this code, with the case for ordinary seats.
Run by itself, the Drisking case failed further on, at something that is not the swing's: it looked at the town's dressing for
the last visit the moment the cast was recalled, and by itself the ground that dressing changes was not in yet. It waits for the
dressing now. In the whole suite it has always passed.

The client proof has an eighth page: a bay of the frame as the builder puts it up, with one swing hanging still and one drawn at
the top of a high arc as the moving swing is drawn, the heel mark, and a ball of sand. I looked at it.

I looked at the swings in the park from a child's height, in eight pictures made with the local aid that is not part of the
build: from the open sand in front, along the bar from one end, close to a seat, from the grass behind with the park beyond, from
the side path, and from a grown height. Nothing needed changing.

## What the local runs found

- **The night's first tidying threw away what its readers had already done.** A night puts the park in order on its first tick,
  which comes a few ticks after its first reader is in it. That tidying cleared what the night had noted for its readers' records,
  so a ball thrown in those first moments was never counted. The sand case found it (its reader throws at once). The tidying no
  longer touches what is waiting for a record. Nothing in the first two slices kept such notes, so nothing was lost before.
- The art tool refused a post's foot: the pipe and the collar it stands in shared a face. The pipe stands on the collar now.
- The heel mark was hard to see on the sand on the first proof page. Its furrow is darker and its ridge lighter.
- Three things were the cases' own and not the game's. The fixture's readers are weightless, so that they stay where a case puts
  them: the first jumper went up and did not come down, and a case's jumper is now given weight for the flight. What a case's
  reader was told and what they heard come down one pipe, and reading either emptied it; they are read together now. And a case's
  reader keeps a grown body's eyes until it is next measured, so a ball thrown level went over a child's head; the cases aim.

## What I have not seen

- **A swing from the seat.** The cases sit readers on swings and the proof draws one mid-arc, but nobody has pumped one by hand:
  whether leaning in time is easy to find, whether the arc at its highest is fun or sickening, and how letting go looks from
  inside, I do not know.
- **A reader on a swing, from outside.** The turn given to the reader is the same turn the moving seat is drawn with, and the
  arms are set by the same hook the canoe's and the dance's are. I have not seen a body drawn on a swing with them.
- **The night.** Everything I looked at is drawn bright. The swings stand furthest from the playground's two lamps, which is why
  they have one of their own; how they look by it I have not seen.
- **The four sounds.** Made and registered; nobody has listened to them.
- **Two readers.** The cases put two readers on neighbouring swings and have one throw at the other. Two people have not done it.

## Numbers

No layout change (the park's own blocks changed, in new builds only). No protocol change: a swing's width travels in the seat's
own data. No Witness change.
