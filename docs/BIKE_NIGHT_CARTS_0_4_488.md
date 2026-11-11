# Bike night 19: the stock boy and his carts (0.4.488)

The proposal (`docs/BIKE_NIGHT_PROPOSAL.md`), of the grocery:

> - A stock boy is wheeling a long chain of carts across the lot. They are a moving obstacle, and riding through the gap
>   in the chain is a shortcut.

This is the last of the town. What is left of Bike night is the graveyard, the Hollow and the endings.

## The chain

Eight grocery carts in a row along the aisle between the Food Fair's two rows of bays, with **a gap after the fourth**
that a bike goes through. The stock boy, in the store's apron, wheels them from the west end of the lot to the east and
back, at a slow walk, all night. At each end he lets go, walks round beside them to their other end, takes hold and
sets off again. They rattle as they go.

The row of four carts that stood in the aisle since 0.4.481 is the chain now. The one left out by the bays is where it
was.

## Where it matters

The lot's east end is the mouth of the store's yard, and the yard is the way to Elm Street and the cut, which is the
straight way to the cemetery. **With the chain at the east end of its run the mouth is barred from side to side, but for
the gap.** Round the chain's west end is eight blocks further. It stands at that end for ten seconds, and is on its way
there or back for a while each side of that.

The three's way into the yard crosses the aisle there too. What they take for a clear ride is asked of the world, so
with the chain across it they do not ride at it as if it were not there; one of them who comes up against it is held up
as by anything else, and goes round.

## What a cart is

A thing in the way, as a parked car is (0.4.487): nobody goes through one, and **a rider who meets one going fast comes
off**, by the bike's own rule. It is a little higher than a bike rolls up onto.

**The chain never runs into anybody.** With somebody in its lane ahead of it, or between its carts, it waits. After a
moment he says so ("Mind out! These don't stop easy."). One of the three in its way is told to go round, at once and by
name. A reader who rides through the gap gets "Through the gap! Go on!"

## Numbers

| | |
|---|---|
| Carts, and the gap | 8, after the fourth; about a block and two thirds wide |
| The chain's length | about 7 blocks |
| Its pace | 0.05 blocks a tick, a block a second |
| Its run | 24 blocks, west end to east end |
| It stands at the west end, and at the east | 5 seconds, 10 seconds, and as long as he takes to walk round |
| It stops for somebody | within a block and a third ahead of it, in its lane |
| He speaks | after a second and a half of waiting; at most every 10 seconds |

## Cases

Two, in `BikeNightTests` (67 in all):

- no chain before the town wakes; then eight carts on the aisle's line, each a thing in the way of its own size, the
  gap after the fourth, him behind the last, pushing; the row that stood in the aisle gone and the one left out not;
  every way of the town clear with the chain at the west end; it goes at a walk and is heard; at the east end the
  yard's mouth is barred but for the gap and the way into the yard is not a clear ride; he walks round beside the
  carts, which do not move while he does, and pushes them back; no cart is ever out of its place; the night's end
  leaves no chain;
- the chain waits for a reader standing in front of it and is never where they are, and he says so; with them gone it
  goes on; a reader rides through the gap, still on and touching nothing, and it is counted; a reader riding flat out
  at the side of a cart comes off, the bike never inside it.

## Not seen

None of it has been seen in a client but a cart on the cars' proof page. Whether the chain is long enough, slow
enough and at the east end often enough to matter in a chase is the owner's to judge: its pace is `CalderCarts.PACE` and
its stands are `PAUSE_WEST` and `PAUSE_EAST`.
