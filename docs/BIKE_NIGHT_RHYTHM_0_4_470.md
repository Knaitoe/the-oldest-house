# Bike night 8: the monkey bars and the seesaw as the proposal has them (0.4.470)

The eighth slice of Bike night (`docs/BIKE_NIGHT_PROPOSAL.md`). It adds nothing new to the playground. It puts right two pieces
I had built short of the proposal.

**What went wrong.** I built the monkey bars (0.4.467) and the seesaw (0.4.468) from my own short note of the playground, not
from the proposal's text. Reading the proposal again before starting on Gus, I found that both were simpler than what it says:

> **Monkey bars.** A new verb: hanging and swinging hand over hand. A jump at the bars grabs one. The strafe keys swing the
> body, and each swing at the top of its arc reaches the next bar. Letting go drops. Too early misses the bar, and the reader
> drops into the sand. It is a timing game, like the pedals.

> **Seesaw.** It takes two: two readers, or one and Gus. Each pushing down at the right moment bounces the other higher. A big
> bounce throws the one on top off with its speed, like a swing jump.

0.4.467 moved a hanging reader along by the walk keys at a steady pace, with nothing to time and no way to miss. 0.4.468 had a
push off the sand and nothing more: no bounce, and nobody was ever thrown. The merry-go-round (0.4.469) I checked against the
proposal before it was committed.

**It still cannot be finished, so the House does not deal it.** Nothing is a Witness source.

## The monkey bars

- **A jump at the bars grabs one.** A reader in the air under the rungs, near enough for their fists to reach, catches the rung
  nearest over them without a hand on it. From up a ladder a hand on the rungs still takes hold, as before; a reader on a ladder
  is not caught by the bars as they climb past, so the top of the bars can still be climbed onto.
- **The swing.** A strafe key swings the body the way the reader is looking along the bars. As the others see them, the body
  swings out under their hands, back through the middle and a little the other way, and hangs still again.
- **The catch.** A strafe key again while the swing is at its top (from a little before it to a little after) lets one hand go
  and catches the next rung that way. The catch is the next swing's beginning, so a reader who keeps the rhythm goes along a rung
  to a swing, about a block and a quarter in a second.
- **Too early,** the hand lets go before the swing has carried it there. It misses, the reader is told so, and they drop into
  the sand.
- **Too late** costs nothing: once the swing has gone back a strafe key swings them again.
- **Having let go** (by sneaking, or by missing), a reader does not catch the bars again in the same fall. They catch them again
  after they have touched the ground.
- On the last rung there is no next one: a strafe key at the top of the swing swings them again, and they stay.
- The walk keys do nothing on the bars now.
- Going across, the record of holds and crossings, and what a reader is told for it, are as they were. Their record also counts
  the rungs they missed, and their account says so if they never got across.

## The seesaw

- **The bounce.** A push made as a rider's end lands (within a fifth of a second of it) is a bounce: on top of the push, it
  turns round most of the speed that end came down with. A push made later than that is only a push, as before. A push made a
  little before their end lands is kept, and made as it lands.
- **Faster each time.** Two riders who each bounce as they land send the board over faster each time.
- **The throw.** When an end comes down hard enough, the rider up on the other end is thrown off it: up, and a little outward,
  with the speed their end had, multiplied as a jump off a swing is. Two bounces in a row are enough. Both riders are told. The
  one who came down stays sat on their end.
- Nobody is hurt: the sand is soft.
- A reader's record counts their bounces, and has whether they were thrown and whom they threw. Their account says so.

**How this reads the proposal.** The proposal says "pushing down at the right moment". Here the moment is the moment a rider's
end lands, and what they do then is push off the sand. I have not built a separate way of throwing one's weight down on the way
down. The one who is thrown is the one on top, as the proposal says; that is the rider whose own end went up fastest.

## How it is made

`BarsHang` keeps when the body's swing began and which way it goes (both sent to clients, so that the swing is drawn from the
same clock), and reads the rider's strafe key as a press, not a hold. `BikeNight.barsJump` looks, each tick, for a reader in the
air under the bars. `BarsClient` turns a hanging reader's body about their hands by the swing.

`SeesawBlock.Board` keeps when each end last landed and how hard, and a push that was asked for a moment too soon. Landing is
where a bounce is made and where a throw is decided. `SeesawSeat.thrown` puts its rider off with the end's speed.

## Tests

`BikeNightTests`: the monkey bars' case is rewritten from where a reader has taken hold, and the seesaw's case has a new part.
Twenty-three cases, as before.

- **The monkey bars.** Hanging still. The walk keys move nothing. A strafe key with a look square across the bars swings them
  neither way. Looking along them, a strafe key swings the body that way and their hands stay. The swing's own shape: out to its
  top, back through the middle, a little the other way, still. A strafe key at the top catches the next rung, twice. A second
  reader in the air under the far end catches a rung without a hand on it, the one nearest over them. The first lets their swing
  go back; a strafe key then swings them again and nothing is lost. The second swings and goes for the next rung too early: they
  drop where they hung and are told why; still in the air, they do not catch the bars again; on the sand and up again, they do.
  The first goes on rung by rung to the last, where a strafe key at the top moves them no further. Across, told so; the rungs
  heard; the records (one across; the other twice on them, one rung missed); letting go; the rungs taken away; the accounts.
- **The seesaw.** As before, with the pushes that are meant to be only pushes made some while after landing, and checked to be
  only pushes. Then: the second reader gets back on; the first pushes off; the second pushes as they land, and the board goes
  back over faster than a push sends it; the first bounces in their turn, faster again; the other end comes down hard and the
  first, up on top, is thrown off, up and a little outward, the second still sat on their end; both are told; their records have
  one bounce each, who was thrown and who threw them; their accounts say so.

### What the local runs found

- **The seesaw's case pushed too soon.** The third reader sits on an end that the new part leaves up in the air, and the case
  pushed four ticks later, before their weight had brought it down. The case waits for the end to be down. Nothing in the game
  changed.
- **The bars' case went for a rung on a swing that had died.** After the second reader's part the first had hung still for a
  while, so their next strafe key was a swing and not a catch, which is what the game should do. The case swings them first.

## What I have not seen

- **The rhythm.** The numbers that make it (the swing is at its top eight ticks after it begins; a catch is good from six ticks
  to thirteen) were chosen, not played. Whether it reads as a timing game like the pedals, whether the window is fair, and
  whether a reader can tell the top of a swing they cannot see themselves make, I do not know. A reader's own view does not
  swing: they have only the count to go by, and what they are told once.
- **The body swinging,** as others see it. Written from the model's numbers.
- **A jump catching the bars.** The case puts a reader in the air under them. Nobody has jumped.
- **The seesaw's bounce and throw,** by hand: whether "as you land" is findable with a key, and how the throw feels.
- **Sounds, and the night.** As for the rest of the park.

## Numbers

No layout, protocol or Witness change. New builds only.
