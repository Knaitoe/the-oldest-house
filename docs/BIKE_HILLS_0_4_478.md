# The bikes: hills (0.4.478)

The owner, on the bike ridden down the slide (0.4.477):

> ensure bike properly accelerates and decelerates from the ramp.

It did not. This is what was wrong and what is there now. It is the ride every bike shares (`RiddenBike`), so it holds for
Bike night's six and for Borrasca's two.

## What was wrong

- **Coming down gave almost nothing.** A bike was given .04 of a block a tick for each block it came down, whatever its
  speed. On the slide's chute that is less than the road takes off it in the same time. Let roll from the deck with one
  push, a bike was still crawling at the bottom (about .07 a tick, against .32 flat out).
- **Going up cost the same at any speed.** A block climbed cost .09 a tick, fast or slow. That is right for a bike going
  flat out and far too kind to a slow one, which went on up a ramp it had no business getting over.
- **On the chute it was not on the ground.** The chute stands as a flight of low steps, for a body. A bike going down
  left each step and fell to the next, in the air most of the way, where the pedals drive nothing and only shift the
  rider's weight.
- **A stopped bike was not stopped.** Under the speed at which a foot goes down it kept whatever it had left. Turning
  round where it stood takes a bike many ticks, and in them it wandered most of a block: off the deck, or into a guard
  rail, which it then rode up onto, since a bike rolls up anything a block high.
- **The brake never finished.** It took a share of the speed each tick, so it never reached nothing, and on the chute it
  held the bike to a creep and no better.

## What is there now

- **A hill is worth its height.** On the ground a bike gives up, or is given, the square of its speed for the height it
  gains or loses, as a real one does: twice `GRAVITY` (.0245, which is 9.8 metres a second each second in blocks and
  ticks) for each block. So:
  - flat out, a block climbed costs what it always did (about .09);
  - a slow bike stalls on a climb a fast one carries: at half speed the long plank ramp stops it part way;
  - let roll from the slide's deck with one push, a bike is at about .26 by the bottom; one already flat out comes off
    at about .39, faster than it can be pedalled, and runs that off again on the level, a little each tick, as before.
- **The Hare still climbs.** `Stats.climb` is now how hard a climb is on a make: the Hare gives up a third as much going
  up and is given no less coming down. It gets over a ramp that stalls an ordinary bike at the same speed. (The proposal's
  Hollow has "a climb only the Hare gets going up in time".)
- **The chute is a sheet, for a bike.** `BikeSurface` is a block a bike rides as one sloping surface: the plank ramp
  (which already was one) and now the slide's chute. A bike has no steps to stand on there; it is kept on the surface
  itself, at the chute's own slope, wheels down the whole way. A body still stands on the steps.
- **Where a sheet meets a deck or a ledge,** the bike's wheels are on the deck until they are past it. It used to be
  pulled down onto the sheet while part of it was still over the deck, which put it inside the deck's own block.
- **Stopped is stopped.** Under the speed at which a foot goes down, the bike stands where it is: on the level, on a
  ramp, on the chute. It turns round where it stands without wandering. From a standstill either pedal starts it.
- **The brake grips.** Beside its share it takes a fixed amount each tick, so it comes to a stop, and holds on the chute.
  The Comet, slow to stop, gets half the grip.
- **The deck's guards are too high for a bike.** For a bike they stand a block and a half, as a fence does.

## What is unchanged

- In the air a bike's speed changes as it did: the hop, the throw off a ramp and landings are as they were.
- A stalled bike does not roll back. A foot is down; it stands until it is pedalled.
- A bike that is not moving on a slope does not start by itself. One push and the hill does the rest.
- The most a bike can do downhill is still 1.4 times its top speed.

## Tests

`BikeNightTests`, two more cases (35 in the namespace):

- **A hill takes the way off a bike going up and gives it back coming down.** Three bikes at the long plank ramp. Flat
  out, an ordinary bike is over the top with about two thirds of its speed. At half that it stalls part way up and stands
  there, on the sheet. The Hare is over from the same half speed. Then down: from near the top with hardly a push it stays
  on the sheet all the way and is going well at the foot; on the level it runs that off again; and from flat out at the
  top it leaves the foot faster than it can be pedalled.
- **A bike rolls down the slide's chute gathering speed, and loses it going up.** From the deck with hardly a push: on the
  chute's own surface all the way, never in the air, and going well by the bottom. Up from the sand at a good speed and
  not pedalled: it stalls on the chute and stands where it stopped. Pedalled, it climbs the rest and is braked to a stop
  on the deck.

The slide case of 0.4.477 found the wandering: with the chute a sheet its rider came up onto the deck differently, crept
while turning round, and finished on top of the guard rail. It passes with the bike stopped.

## Not seen

Nothing here was looked at in a client. The chute is drawn as it was; a bike on it now follows the drawn surface where
before it followed the steps, which were at the same heights over their middles. How it feels to ride down is the owner's
to judge: if the hill should pull harder or less, it is one number (`RiddenBike.GRAVITY`).

## After: a hop where it stands (CI, run 1530)

CI found one more thing the hills had left behind. Run 1530 failed the Borrasca case that hops Kendra's bike where it
stands and expects it to come down level: it came down leaning (`-0.05`), by the dice, about one run in several.

In the air a bike trades speed for height: it pays `0.04` of speed for each block it climbs and is given the same for each
block it drops. A bike with no way on has nothing to pay going up, since its speed never goes below nothing, but it was
still given speed for the whole drop coming down. Hopped from a standstill it landed at about `0.034`, just over the speed
at which a bike counts as being ridden (`STOPPED`, `0.03`), so it rolled a little with nobody pedalling and wobbled as a
moving bike does.

Now a bike keeps count of what its climb has cost it since it left the ground (`lift`). A bike that is moving is given
speed for a drop as before, and a bike rolling off a ledge still picks up the fall. One with no way on gets back only what
its own climb took: hopped where it stands it paid nothing, so it comes down standing. The count is forgotten when it
lands.

The Borrasca case now says so: after the hop the bike's speed is nothing. It passed three times running with the change,
and every Bike night case with it.
