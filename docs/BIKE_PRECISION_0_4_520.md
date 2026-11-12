# The bike, made more precise for the Hollow (0.4.520)

The owner, playing Bike night on November 12: "the bike controls should be more precise for the sort of platforming included. not
exactly sure how to improve that."

## What made it imprecise

The ride is the same as it was built (`RiddenBike`, 0.4.435 and 0.4.463):

- The strafe keys are the pedals. Each push drives the bike on and tips it, so speed and balance are one thing.
- The bike goes where the rider looks.
- Holding jump winds up a hop, and letting go pops it.

On the Hollow's islands that asks for several exact things at once, and some of them were harder than they had to be:

- **The speed at the brink was hard to hold.** How far a hop carries depends on it. A bike loses a little every tick, so holding a
  speed meant pushing in turn the whole way, and every push also tips the bike.
- **The hop had to be let go while a wheel was still on the ground.** A tap clears a two-block gap only from the last block before
  the brink (docs/BIKE_NIGHT_HOLLOW_0_4_493.md). Let go a moment late and the wound-up hop was simply lost.
- **In the air the bike barely answered.** It kept a third of its turning, and a jump could not be shortened.
- **Landing a little off level tipped it hard.** Anything past an eighth of the way over counted as crooked.
- **The bike wandered most just before a jump,** while the rider was lining it up with the hop wound up.
- **Turning slowly was sluggish.** At a crawl the bike had a third of its turning, so lining up on a narrow piece was slow.

## What changed

None of these rides the bike for the rider. The pedals still make the speed and keep the balance.

- **Holding forward keeps the pace.** On the ground the bike holds the speed the pedals have given it. Its cranks turn at that
  pace, and it adds no speed of its own. Sand and mud still drag at it. A run-up can now be judged: pedal up to speed, then hold
  forward to the brink. The kept pace's strokes are only drawn: the next push still follows the rider's own last push, so a push
  after holding forward does what it would have done before it.
- **A hop just over the brink still pops (`COYOTE`, five ticks).** Roll off an edge with the hop wound up, let go in the next
  quarter of a second, and it pops as it would have from the edge. After that it is spent, as before. A bike thrown up by a ramp
  never gets a second pop.
- **The bars answer better in the air (`AIR_TURN`, .35 to .6),** and **the brake sits the rider back (`AIR_BRAKE`).** Held in
  the air, the brake brings the bike down shorter, about a quarter shorter over a full hop.
- **Down on both wheels is wider (`LEVEL`, .12 to .2).** A landing a fifth of the way over keeps its way on. Further over still
  tips, as before.
- **Wound up for a hop, the rider is braced.** The bike wanders off true half as much.
- **Turning slowly takes less way (`SLOW_TURN`, .3 to .55):** the least share of its turning a bike has when barely moving.

## Not done, for the owner

Two things would make it more precise still, and both change the game more than these do:

- **A mark where the bike will come down,** shown to its rider only while a hop is wound up. Most platformers give something like
  it. It is a game's sign in a place that has none.
- **Pedalling by holding forward** instead of keeping the pace. That would end the pedals as the balance, which was the owner's
  own rule (0.4.435: "Maintaining balance and pedaling left/right should be a function.").

## Checked

- Two new cases in the Bike night namespace:
  - held forward, a bike keeps its pace for two seconds and its cranks go round, while one left to roll slows; a push on the
    other pedal afterwards still drives as a push in turn does; a hop let go two ticks over a ledge's brink pops up over it, and one
    let go eight ticks over does not;
  - holding the brake in the air comes down shorter than not; a landing a little off level from a full hop is not tipped.
- The existing hop, ramp and whole-Hollow cases, and the whole namespace.
- The quick checks.

## Not seen

- None of it under a real rider's hands. Whether the numbers are right (how long the brink allows, how much the air brake takes,
  how wide "level" is) is the owner's to judge. Each is one constant in `RiddenBike`.
