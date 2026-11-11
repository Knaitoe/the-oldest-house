# Bike night 15: Gus's bike (0.4.483)

The proposal (`docs/BIKE_NIGHT_PROPOSAL.md`, "The shape"): "Alone, a reader has one friend along, **Gus**, a character on
his own bike, who talks, rides the seesaw with them and keeps up in the chase." Until now he had no bike, and when the
three went for theirs he stayed on the sand.

## His bike

It stands on the grass by the bench, off the side path, a few steps from where he waits, pointing at the gate. It is the
Wasp's frame in his own paint, with a safety flag on a whip up from the back axle. It is his brother's old bike; the flag
was his idea, and he says so, once, when a reader puts a hand on it. Nobody but him gets on it.

It is one of the bikes the three ride (`PackBike`, look `GUS`): never a block, never saved, and nobody's to take. If it
goes from under him (the part of the town he is in is put away while a reader is far off) there is one under him again
when he is next there.

## Getting on

When Tully says "Get them" and the three go for their bikes, Gus drops what he is doing (the seesaw, a swing he is
pushing) and goes for his: off the sand by the side path, and up behind the bike. If he cannot get to it within eight
seconds he is on it anyway, as the three are on theirs.

## Riding

He rides beside whichever reader is nearest who still has a bike, and keeps to the one he was beside before another the
same distance off.

- In the park and on its street he rides at them, by the gate when the railings are between.
- In the town he rides straight to them when he can see them, and by the town's ways when he cannot: the same ways the
  three know (`CalderRoutes`), to the end of the reader's way that is nearer them.
- He stops a little over three blocks from them. He is not in anybody's way: nothing collides with a bike but the town.
- He rides at the three's pace. A reader who outruns them outruns him, and he catches up when they stop.
- With nobody left who has a bike, he stops where he is.

## What he calls out

All of it is over his head and never in chat, and held to the rule his lines already had.

- **About nothing,** as on the playground, but about the ride: eight lines.
- **One of them is close behind.** When one of the three is riding straight at his friend and is within nine blocks:
  "Behind you! Right behind you!"
- **One of them has gone round.** When one of the three who is within thirty blocks is making, by the town's ways, for a
  corner ahead of his friend: "They went round! One of them went round!" This is the hunt's splitting up said aloud. A
  reader who has not seen a bully turn off into an alley is told that one has.
- **The graveyard.** The first time his friend is within thirty blocks of the cemetery's gate: "Not the graveyard.
  Anywhere else. I told you the story!"
- **When every reader's bike has been had:** "Nobody even looked at mine. I'm not sure what I think about that."

He calls out about the three at most once in eight seconds, and with both things to say, not the same one twice running.

## The three do not go for him

They have his cap. It is the readers' bikes they are after, and a reader is what they hunt, head off and catch. Gus
rides through it. The proposal's "Gus is there if he made it" is left for the Hollow, where what catches a rider is not
the three.

This is a choice, and the owner's to change. The other way round (one of them takes Gus's bike if he is within reach of
it for two seconds, and rides off with it) would cost the three a hunter most nights, since Gus rides where the one on a
reader's tail does, and would leave a reader who is alone without him for the graveyard.

Two things of his that were wrong and are not now. When a reader on foot was caught, Gus said "They got a bike." He
says it when they did. And of a ball of sand that hits one of the three he had three things to say, and said the third
again for every hit after it; the case for his calls found that, because with a line always waiting he never called
anything else. He says each once.

## A ball of sand at arm's length

Found by the case for his calls, which first had a reader hold all three off with sand: a ball thrown at one of them who
was right on top of the reader went through him. The ball starts at the reader's eye, and a flight that starts inside a
body never meets it. That is the moment a reader most needs it. Now a ball has met whoever it starts inside
(`SandBall.tick`), and with more than one of them there, one who is not coughing already. It is still one ball for one
of them.

## What changed underneath

- `BikeNightHunt.Rider` is what somebody riding the town's ways has in mind: the way, where it ends, which side he is
  going round something. The three have one each (`Bully` extends it) and Gus has one.
- `BikeNightBullies.parkRide` is the riding in the park and on its street, by the gate, that the three's chase had
  inline. Gus uses it.

## Tests

Four cases in `BikeNightTests`:

- **Gus has a bike of his own by the bench, and a hand on it gets a word from him.** One bike of its kind in the park,
  where it should be, clear of the bench and off the sand; a hand on it puts nobody on it; he says whose it is, once, and
  not in chat; with nobody come for anybody he does not go for it.
- **Gus gets on his bike and rides beside a reader through the gate and the town.** He goes to it on his own feet; his
  hands are on the bars; he comes over to a reader at the rack and stops a little way off; with the reader on Main Street
  he leaves the park by its gate and is beside them there; with the reader round behind the shops he comes by the ways.
- **On the ride Gus calls out where the three are, and they never go for him.** A reader stands on Main Street with Gus
  beside them and the three come, one along the street and two by the back ways. He calls that one has gone round while
  the nearest is still more than nine blocks off, and that one is right behind when one is. The reader is caught and the
  Wasp goes from the rack; the three go; he says he saw them get a bike, and that nobody looked at his; he is still on
  it. None of it is in chat.
- **A ball of sand thrown at one of them who is right on top of a reader still meets him.** One of the three rides onto
  a reader on foot; the reader throws; he coughs. Without the change above this case waits for ever.

## Not here yet

- **Gus in the graveyard and the Hollow.** He rides to the gate with a reader. What is behind it is not built.
- **The traffic** (0.4.484, `docs/BIKE_NIGHT_CARS_0_4_484.md`) **and the people.**

## Not seen

None of this has been seen in a client: his bike's flag is on the proof page (`pack`), and his pose on the bike is the
one the three have. Whether his warnings come too often, or too late to be any use, is the owner's to judge: their
distances and the gap between them are `BikeNightGus.BEHIND_THEM`, `ROUND_THEM`, `AHEAD_THEM` and `WARN_GAP`.
