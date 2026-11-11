# Bike night 21: the gate, the three and Gus in the graveyard (0.4.490)

The proposal (`docs/BIKE_NIGHT_PROPOSAL.md`, "Act three: the graveyard"), and what was answered on November 11:

> - **The bullies:** they come through the gate, slower; one stops at the fence ("I'm not going in there"); the other two
>   fall back in the mist, and then their jeers stop altogether.
> - **Getting in.** The town's hunt leaves the north open, so the gate is the one way the three are not in. It has been
>   shut all evening and is standing open when the readers reach it. Nobody says why.
> - **The three.** They can still catch a reader who comes off in the first rows. Nothing is shown happening to them:
>   their voices stop, and they are taken away only when nobody can see them go.
> - **The quiet row.** Gus has told the story badly in the park, and shouts "Not the graveyard" in the chase. At the row
>   he says nothing.

This is those four. The mist, and the town's lights going out, are the next slice: "fall back in the mist" is, for now,
falling back.

## The gate

Shut from the town's building until the chase is on. Then it opens the first moment **nobody is near enough and turned
its way to see it move**: within forty-eight blocks with it in front of them, or right beside it. That counts everybody
in the place, a spectator too. Nothing is heard and nothing is said. Its two leaves stand end on to the gateway, inside
it, each by its own pier, as the park's gate stands when Tully has undone it; the way through is the gateway's four
blocks less the leaves' thickness.

The night over, it is shut again.

## The three

A reader in the cemetery is hunted by `BikeNightGraveyard`, not by the town's ways. Each of the three who is after
that reader rides to the gate by the town's ways, and then:

- **Duff stops outside it** and says "I'm not going in there."
- **Tully and Ray-Ray come in at two thirds of the pace they hunt at** (0.2 blocks a tick, where they hunt at 0.3),
  weaving between the stones. Each has only so much hill in him: **Ray-Ray gets no further than the first bank, Tully
  no further than the second.** At the end of it, or after twenty seconds in there, he stops where he is and says one
  thing to the other ("Tully, wait up. I can't see." / "Ray-Ray? Quit messing around."). Then he says nothing again,
  and none of the three's jeers is his.
- Until he stops he is after a reader as he was in the town: within reach for two seconds is caught, in the first rows
  as anywhere. One who catches a reader goes off with their bike by the town's own rule.
- A reader who goes back out by the gate is followed out by it, and the town's hunt has them again.

**One who has stopped is gone, with his bike, the first moment after five seconds that nobody could see it:** nobody
within forty blocks with him in front of them. The one at the fence is the last to go. When all three are gone the
night knows they are (the stage it already had for it).

With several readers the three are shared out as in the town: one whose reader is still in the town hunts there.

## Gus

Comes in after his friend by the gate and through the stones, keeping the distance he keeps anywhere, and says what he
thinks of it twice: coming in ("We're in the graveyard. We are in the graveyard.") and past the first bank ("Stay on
the path. That's all I'm saying. Stay on the path."). He says these to somebody, as he says everything: with nobody
near enough to hear, they wait.

**Past the last full row, where the small stones are, he says nothing:** not of his own accord, not what he had been
about to say (it is dropped, not kept for later), and not to a hand on him.

## Between the stones

The three and Gus do not ride the town's ways in there; there are none. Each steers for where he is going **if the
next three blocks that way are clear**, and if not along the nearest heading to either side that is, the side he last
went round first. Clear is asked of the hill itself: nothing standing in the way at the height the hill is at each
step, and ground under it. A bank is not in the way and neither is a low marker; a standing stone, a cross, a
memorial, a tree, the railings and the heap are, and the open grave is no ground.

## Numbers

| | |
|---|---|
| The gate is seen from | 48 blocks, by somebody it is in front of |
| Their pace in there | 0.2 blocks a tick (0.3 in the town) |
| Ray-Ray stops | two blocks short of the first bank, or after 20 seconds |
| Tully stops | two blocks short of the second bank, or after 20 seconds |
| One who has stopped is left | 5 seconds at least |
| He is seen from | 40 blocks, by somebody he is in front of |
| A rider looks ahead | 3 blocks, and up to 100 degrees to either side in steps of 20 |

## Cases

Three, in `BikeNightTests` (74 in all):

- the gate is shut all evening; with the chase on and a reader on the lane looking at it, it does not move for two
  seconds; they look away and it is standing open, each leaf end on inside the gateway, nothing said; the way through
  is a clear ride and wide enough for two; the night over, it is shut as it was built;
- with a reader up the hill looking back: Duff stops outside the gate and says so; the other two come in, neither
  ever faster than two thirds of the hunt's pace, Ray-Ray to the first bank and Tully to the second, each saying his
  one thing; watched for seven seconds, all three are still there and nobody says anything more; the reader looks up
  the hill and all three and their bikes are gone, and the night knows it; the reader is not caught or hurt, and none
  of it was in chat;
- Gus comes in by the gate after his friend and says where they are; up the first bank and says the other; and to
  the row, through twelve rows of stones and round the open grave, held up for less than three seconds in all; at the
  row for fifteen seconds he says nothing, to a hand on him twice or otherwise.

The first run of Gus's case had the reader standing at the row from the start, seventy blocks from the gate: he had
nobody to say anything to, and said nothing. The reader goes up the hill with him now.

## Not seen, and the owner's

None of it has been seen in a client. Whether twenty blocks and forty are the right amounts of hill for the two of
them, whether five seconds is long enough, and whether a turned back at ten blocks should be enough for somebody to be
gone behind it (the mist will hide more than this does) are the owner's to judge. What the two say when they stop,
and Gus's two lines, are Claude's.

The three's bikes go with them. The proposal's aftermath has "three bikes by the front gate"; that is the endings'.
