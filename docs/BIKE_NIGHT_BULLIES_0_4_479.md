# Bike night 12: the three who come (0.4.479)

The third slice of the proposal's build order (`docs/BIKE_NIGHT_PROPOSAL.md`, "The bullies arrive"): Tully, who leads;
Ray-Ray; and Duff. This is the three at the playground, and the start of the chase. The chase itself goes through the
town, which is next; until the town is there, they only give chase, and nothing comes of being reached.

## Who they are

Children. The owner, of Gus: "if you don't have gus designed as a child, i'd probably assume him/the rest of the cast of
playground kids and bullies as such." They are a little bigger than the readers (a reader is .54 of a grown body; they are
.6 to .67), with children's faces, painted by the shared painter:

- **Tully**, dark hair, a black T-shirt with a skull on it whose eyes are fire, and jeans out at the knees. A second sheet has him in Gus's cap.
- **Ray-Ray**, fair, in a hooded sweatshirt and track pants.
- **Duff**, the biggest, a crew cut and a football jersey, number 66, over a long-sleeved shirt.

(Their clothes are 0.4.480's, `docs/BIKE_NIGHT_CLOTHES_0_4_480.md`. As first painted here Tully had a denim jacket with its sleeves cut off, Ray-Ray plaid, and Duff a sweatshirt.)

Their threat is humiliation and never injury. Nothing in this slice hurts anybody.

## What happens

1. **When.** After three minutes of play, on the night's own clock, which runs only while somebody is in the park.
2. **The car.** A car goes by on Park Street. Its lights go along the railings (a real moving light) and it is heard from
   the sand. Gus: "Car. Cars don't come down here this late."
3. **They ride up.** Three bikes come west along the street to the gate, in file.
4. **The padlock is Tully's.** The park's first paper says the gate is chained and the padlock on the chain is new. He
   put it there. "Who chained this up? Oh, right. Me." He undoes it, and the gate swings open: each leaf out over the
   pavement, end on, the chain hanging from one. It stands open for the rest of the night, and it is the readers' way out.
5. **They circle.** In at the gate and twice round the playground: down the path, along the railings, behind the swings,
   below the sand and up the path again. They never ride onto the sand. They jeer as they go.
6. **They get off.** Each leaves his bike standing.
   - **Tully** walks to Gus, takes his cap ("Nice hat."), and wears it. Gus: "Hey! Give it back! My mom wrote my name
     in that!" Then he stands in the middle of the sand with his arms folded.
   - **Ray-Ray** goes out on the sand and kicks it, every few seconds.
   - **Duff** takes an end of the seesaw and keeps it down ("Mine now."). The proposal has one of them standing on the
     seesaw; he sits on it, because that is what the seesaw's own rule makes mean: with his weight on one end and a
     reader's on the other the board does not move, so anybody who gets on the other end is kept up in the air, and a
     push does nothing with no sand under their feet. "Going up. Enjoy the view." They can get down by getting off.
7. **Sand balls.** A ball of sand that meets one of them stops him where he is, on his bike or off it, coughing into his
   hands, for three seconds. A second ball while he is still coughing is not counted. Each one that lands makes them
   angrier: "Ow! Hey! Who threw that?", "It's in my mouth. It's in my MOUTH.", "My eyes! Okay. Okay, that's it."
8. **"Get them."** Three balls landed, or a minute and a half of standing about with nothing thrown, and Tully says it.
   Each who is on foot runs for his bike. The readers are told the gate is open and their bikes are where they left
   them. Gus: "Bikes. BIKES. Go go go!"
9. **They give chase.** Each rides at a reader (with enough readers, a different one each), by way of the gate when the
   railings are between them. Reached, nothing happens yet.

## What they say

Captions over their heads and never chat. The first jeer is the proposal's "Look, it's the science club."; then one for
whatever the reader they are looking at is on, once each ("Swing higher, dork." is the swings'); then the rest, in a
different order each night. A hand on one of them gets "Hands off, dork." They are unkind and no worse: a case holds every
line of theirs to being short, to no slur, to nothing about anybody's body, and to nothing about what a reader is.

## How it is built

- **`PackBike`**: a bike ridden by somebody who is not a reader. "How they ride: on the same bike physics, simpler." It
  is drawn as any bike is, keeps to the ground as a reader's does (falls, rolls up a block, rides a ramp's or the chute's
  sheet), and goes where it is told at the speed it is told, turning no faster than a bike can. It has no balance to
  keep. `RiddenBike` was opened for it: who may ride (`takes`) and what drives it (`driven`) are a bike's own to say.
  Gus's bike will be one of these.
- **Three bikes**: the Wasp's frame and wheels in their own paint (`tools/draw_bike_night_art.py`). Tully's has stunt
  pegs; Duff's has a number plate.
- **`BikeNightBullies`**: the night's stages and the three. Their routes are places relative to the park's base; a
  rider takes up the next place a little before he reaches the last, so corners are ridden round.
- **The gate** opens and shuts as blocks (`BikeNightSite.gateOpen`, `gateShut`); the night's end shuts and chains it.
- **Five sounds**, all synthesized: a car passing, a padlock coming open and its chain running, a cough, sand kicked,
  and a freewheel ticking under a coasting bike. Subtitled.
- **The account**: that they came and whose padlock it was, the cap, how many balls landed, and that Tully said to get us.

## Tests

`BikeNightTests`, three more cases (38 in the namespace); the playground's other cases keep the three away.

- **Three come on bikes, undo the gate, circle the playground and make themselves at home.** The car's light and sound;
  the three on bikes of their own, children a little bigger than a reader; the padlock, the open gateway and the two
  leaves; twice round and never on the sand; the science club first, and nothing of theirs in chat; the cap on Tully
  and off Gus; Ray-Ray on the sand and kicking it; Duff on the seesaw, and a reader on its other end kept up; their bikes
  standing where they left them; how they talk; the account; and the open gate looked at.
- **A ball of sand stops one of them, and a few start the chase.** Tully hit as he rides: coughing, stopped, the others
  riding on; a second ball while he coughs not counted; he rides on. Ray-Ray and Duff, and at three Tully says it, the
  reader is told, and all three ride at the reader. Reached, the reader is not hurt.
- **Left alone long enough Tully says it anyway, and they come out by the gate.** Nothing thrown; they run for their
  bikes (Duff off the seesaw); a reader out on the street is come after through the gateway and not the railings; and
  the night's end leaves nobody, no bikes of theirs, and the gate chained.

The first runs found one fault of the game's: Duff's line to a reader he has just lifted was lost if he was in the
middle of a jeer when they got on. He now says it when he has finished.

## Not here yet

- **Being caught.** "A bully within arm's reach of a rider for two seconds has caught them." It comes with the town,
  where there is somewhere to run to.
- **How they hunt.** Here each rides straight at a reader and round whatever holds him up. In the town they split up
  and take the back ways, on the streets' own map.
- **Gus's bike.** He says to go for the bikes and has none of his own yet.
- **A reader's own cap.** The proposal has them take "Gus's cap, or a reader's own". Gus is in every night, so it is his.
- **The car** is a light and a sound. The cars come with the town's traffic, and this one will be one of them.

## Not seen

Nothing here was looked at in a client but the proof's new page (the three bikes and the open gate). The three on their
bikes with their legs going round, the cough, the kick and the folded arms are poses chosen by number and not seen on the
model in motion. How long three minutes of play is, and whether three balls is the right few, are the owner's to judge;
both are single numbers (`BikeNightBullies.ARRIVE`, `HITS`).
