# Bike night 14: the hunt (0.4.482)

The next part of the proposal's fourth slice (`docs/BIKE_NIGHT_PROPOSAL.md`, "The bullies"): what the three do once the
chase is out of the park. Until now each rode straight at a reader and round whatever held him up, and nothing came of
being reached.

> - **How they hunt:** they split up. One stays on the readers' tail, and the others take the town's back ways to head
>   them off at a corner.
> - **Caught:** a bully within arm's reach of a rider for two seconds has caught them.

And, of being caught: "He takes the bike, the rider is shoved into the leaves, and the bullies ride off laughing with it."

## The town's ways

`CalderRoutes` is the town as the three know it: places a bike can stand on, and ways between them that a bike rides
without touching anything. The streets are ways along the middle of the road. The back ways are ways too: the four gaps
between the shops, the two alleys, the loading dock, the empty lot's trail, the drive-thru lane, the grocery's lot and
yard, the cut, and the path out of Elm Court. A gap, a hole in a fence or a footpath is a tight way, ridden slowly and on
its line.

What only a reader fits through is not there: the two holes in the school's fence, and the two back yards with boards
gone. The three go round.

## How each rides

At a reader he can see, straight: within fourteen blocks, with nothing between a bike and them. Up close he slows, to stay
beside them.

At a reader he cannot see, along the town's ways, thinking again about his way once a second:

- **The first after a reader is on their tail.** He makes for the end of the way they are on that is nearer them.
- **The second heads them off.** He makes for the end they are riding toward, or, if they are standing, for the far end
  of their way, by a way that does not go through them.
- **The third** makes for the place past that one, and waits there.

With one reader all three are after them. With more, each takes one of his own, and the first after each is on their tail.
The readers are shared out among those of the three who are still after somebody, so when one of them has gone with a
bike no reader is left to two of them while another has nobody.

**Nobody heads a reader off to the north.** The proposal: "the chase steers the readers toward the edge of town, where the
only way the bullies aren't blocking is the graveyard gate." One whose part would send him north of the reader closes the
way back instead, at the other end of the reader's way. So a reader who goes east, west or south finds somebody ahead of
them, and one who goes north finds nobody; and every way north comes out on Church Lane, at the cemetery's gate.

A reader on Main Street between the bank and the grocery, then, has Tully coming along the street at them, Ray-Ray coming
out of the gap between the hardware store and the bank behind them, having gone round by the alley, and Duff in the mouth
of that gap.

**A reader behind something.** The one on their tail does not wait at the end of their way for them to come out. From
there he goes in after them, round whatever is between, for ten seconds. If that gets him nowhere he goes back to the
corner for five, and comes again. The other two keep their corners. So standing behind a parked bike, a bench or a
lamp post is not somewhere to stay.

In the park and on Park Street the chase is as it was: at a reader, by way of the gate when the railings are between.

## Caught

A reader within reach of one of them for two seconds is caught, on a bike or off it, in the town or the park.

- The bike goes with him: the one under the reader, or the one they left standing somewhere. It is wheeled beside his own.
  It is taken from under them: it is not left standing where they were.
- A reader whose bike the three have had does not take another. A hand on one tells them so: "Yours is gone. These are
  other people's." They can still be carried.
- The reader is put down in the leaves, a little way off. They are not hurt, and nothing is taken from them but the bike.
- He says so, the three laugh, and the reader is told who has it.
- He rides off with it by the town's ways to the far end of town, and is gone once nobody is near.
- The others go on after whoever is left. When nobody is, they go too.

A ball of sand still stops one of them for three seconds, in the hunt as on the playground, and while he is stopped he
catches nobody.

## Out

"In co-op, the reader is not out yet: the Hopper can carry them on its pegs. Only a reader nobody carries is out."

A reader who has been caught and is on a Hopper's pegs is still in the night. One who is on foot is out when nobody is
left who could carry them, or when half a minute has gone by and nobody has. They are told the way home. What being out
closes to them (the vignette is never dealt to them again) comes with the endings.

## What they call

In the town they call to each other as much as at the readers: "Go round! Go ROUND!", "They went down the alley!", "Cut
them off at the bank!", "Take the gap! The gap!" The one who gets a bike says so, each in his own words, and each has a
last word as they go. All of it is over their heads and never in chat, and all of it is held to the rule their lines
already had: short, ended, unkind and no worse.

Two new sounds, both synthesized and subtitled: somebody landing in leaves, and laughter.

## Tests

Six cases in `BikeNightTests`, on the park and the town built whole:

- **Every way through the town is clear for a bike, and they all join up.** A body the size of a bike is put down every
  quarter of a block of every way, and touches nothing and always has ground under it. There is a way from the park's gate
  to every place. The way to the cemetery's gate ends on Church Lane. Keeping clear of a place on Mill Street takes a bike
  by the empty lot and the alley. No place is on the school's field.
- **In the town they split up, one behind and two by the back ways to head a reader off.** A reader stands on Main Street
  and keeps Tully off with sand. Tully makes for the near end of their way, Ray-Ray for its far end, and Duff for the mouth
  of the gap; Ray-Ray rides through the gap between the hardware store and the bank and comes out behind the reader while
  Tully is in front of them.
- **Nobody heads a reader off to the north, and somebody does to the west.** A reader standing on Mill Street and then
  riding north to Elm Street never has one of them north of them or making for a place north of them; turning west along
  Elm Street, they have one making for a place ahead.
- **Within reach for two seconds a reader is caught, the bike goes with them, and nobody is hurt.** Not before two
  seconds; the reader off the bike and at full health; the Wasp beside the bike of the one who caught them, and no bike
  left standing where they were; who has it told to the reader and kept in their record with the street; all three gone,
  with the Wasp; the reader out, and told the way home; a hand on another bike in the rack leaves it there, and they are
  told why.
- **Each of three readers has one of them, and the one behind something is gone in after.** One reader stands on Main
  Street with a bike stood across the road on either side of them, so that from neither end of that stretch is there a
  clear way to them; one stands on Mill Street and one outside the theater. Each is caught, each by a different one of
  the three, and each record keeps its own street. The one who had the reader between the bikes went in after them from
  the end of the street, and both bikes are still standing.
- **A caught reader on a friend's pegs is still in it, and out when nobody is left to carry them.** Caught with a friend
  still riding, a reader is not out; on the friend's Hopper they stay in the night for longer than a reader on foot is
  given; when the Hopper is caught as well, both are out.

The first run of the first case found four ways that were not clear: one went through a lamp post, one through the
grocery's carts, one through another lamp, and one along the empty lot's trail, over its ramp, which is a wall from the
west. They were moved.

The last case found two things, and the same bike was both of them. A reader caught on a bike had it taken twice: the one
who caught them wheeled it off, and it was also left on its stand where they had been, because taking it off its rider
went the way of its rider getting off it. And with that bike standing in the road, the two who were left came to the ends
of the street on either side of the Hopper and stopped there: neither had a clear way to it, and neither went in. Now a
taken bike is taken (`RiddenBike.taken`), and the one on a reader's tail goes in after a reader behind something.

Writing the case for that found two more. A reader taken off a bike was set down, for a moment, on top of it, and the
street written in their record was whichever place was nearest at that height: "the loading dock", from Main Street.
The record is now made from the ground under them. And the three were given their readers by which of the three they
were, not by who was still hunting: with one of them gone, two could be after one reader while another had nobody.

## Not here yet

- **Gus's bike.** He still has none, and does not come into the town.
- **The way out.** The north is left open and ends at the cemetery's gate, but the gate does not open yet: what is behind
  it comes with the graveyard.
- **The traffic and the people,** who get in the three's way and cheer the readers.

## Not seen

None of this has been seen in a client. Whether three bikes at this speed are too easy or too hard to get away from is
the owner's to judge: their speed in the hunt is one number (`BikeNightBullies.HUNT`), and so are how far they see
(`BikeNightHunt.SIGHT`), their reach (`REACH`) and the two seconds (`CAUGHT`).

One thing the tests showed and did not settle: the three's bikes roll up anything under a block high, as a reader's
does, and a parked bike is under a block high. One of them going in after a reader behind a parked bike goes over it.
