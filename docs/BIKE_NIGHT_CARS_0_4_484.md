# Bike night 16: the cars (0.4.484)

The proposal's traffic (`docs/BIKE_NIGHT_PROPOSAL.md`, "Traffic"):

> - **The cars:** a new, simple actor, a car driving its lane with its headlights on.
> - **Who drives them:** a station wagon with wood panels, a pickup, a long sedan, a muscle car with its windows down.
> - **Honks:** they honk at a rider in their lane and slow down. They never hit anyone, but a rider who swerves into one
>   comes off.
> - **Voices:** people call from windows and sidewalks, in captions [...] One car slows beside the bullies and yells at
>   them, which buys the readers a few seconds.

The cars are here, and the driver who yells at the three. The people on the pavements are the next slice.

## The four

Each is four and a half blocks long, drawn in code at sixteen pixels to the block (`tools/draw_calder_cars.py`), with its
headlights and tail lights lit:

- **the station wagon:** cream, with wood panels between its wheels and a rack on its roof;
- **the pickup:** red, with two pumpkins riding in its bed;
- **the sedan:** dark green, long, with a vinyl roof;
- **the fast one:** burnt orange with two black stripes over it, a scoop on its hood, a spoiler, fat back tyres, and
  its side windows down.

A model's piece may not reach more than a block outside its own block, so each car is two models end to end, and no
piece of one is more than a block long: a longer one would have its picture stretched over it. A strip painted on one
row of a car's side is one strip along the whole car.

## Where they drive

Two rounds of the town's streets, each driven both ways, one car to a way:

- Park Street, West Avenue, Main Street and East Avenue;
- Main Street, West Avenue, Elm Street and East Avenue.

A car keeps to the right of the middle of the road, so the two ways round are two lanes. Church Lane, under the cemetery,
has no cars on it. Mill Street has none either: it is the readers' and the three's way up from the park.

They start when the chase does. Until then, as Gus says of the one that goes by, cars don't come down here this late.

## How one drives

- It looks along its lane ahead of its nose. With a rider, one of the three, Gus or another car in it, it slows, and
  stops short of them.
- **A rider in its lane gets its horn,** each car its own. After five seconds sat behind somebody, its driver says what
  they think: "Get off the road!"
- **It never hits anyone.** With somebody under it (they rode into its side) it stays where it is until they are out.
- **A rider who rides into one comes off,** as at a wall: faster than a bike survives a wall, they are over the bars
  and the bike lies in the road. Slower, nothing comes of it.
- **The three's bikes and Gus's go round it:** one that is where a car is, is put to one side of it.
- Its light is a real light on the road ahead of it, as the Lamp's is.

A car is the night's, and its body is only what is seen of it. Where each is on its round is kept and moved whether or
not that part of the town is awake; the body is put there while it is, and made again when it is not there.

## Telling off

Twice a night at most, and once for each car, a driver who comes by one of the three in the town slows beside him:
"Tully Marsh! Does your mother know where you are?" He stops for it, for about three seconds, as he does for a ball of
sand, and answers back ("We're not doing anything!"). That is the readers' few seconds.

What a driver calls is a caption, as a person's line is, and never chat. `Voiced` is what a caption can come from: a
person, or a car with somebody in it.

## Sounds

Five, synthesized and subtitled: an engine going by (played over and over as a car drives, each car at its own pitch),
and four horns.

## Tests

Five cases in `BikeNightTests`:

- **The cars' rounds keep to the road, and each way round has its own lane.** Every half block of every way: the middle
  of the car and both its sides are on the road, a body the size of one touches nothing, and it is south of Church
  Lane. The two ways round a round are never in each other's lane. No car starts by the park's gate, and with no chase
  there are no cars.
- **Four cars drive their ways round with their lights on.** Four bodies and no more; each has driven on at a car's
  pace, is where its car is, is on the road, points the way it is going, and has a lit light on the road ahead of its
  nose; one going by is heard.
- **A car stops short of somebody in its lane, sounds its horn, and goes on when they have gone.** Its nose stops about
  a block off; it is never over them; they hear its horn; after a while its driver says so, over the car and not in
  chat; when they step onto the pavement it drives on.
- **A rider who rides into a car comes off, and one going slowly does not.** Against its side at a crawl the rider is
  still on, and the car does not move; from eight blocks off at a run they are over the bars, told so, and the bike lies
  in the road by the car, which is where it was.
- **A driver tells one of the three off and he stops for it, and their bikes go round a car.** A car comes by one of
  them riding in the town: its driver says what they think of him, he stops and answers, and the car crawls beside
  him; he rides on when it is over; a second driver tells another of them off, and no third has anything to say; a
  bike a car is put on top of is beside it a moment later; and none of their bikes is ever inside a car.

## Not here yet

- **The people on the pavements:** the voices from windows and sidewalks, the trick-or-treaters, the theater's line.
- **Drivers.** Nobody is seen in a car: the glass is dark, and the fast one's open windows show a dark inside.
- **The car that goes by the park** before the three come is still a light and a sound, not one of these.

## Not seen

None of this has been seen in a client but the cars on their proof page (`cars`). Whether four cars are enough, or too
many for a chase, and whether they are too slow or too fast to be a hazard, are the owner's to judge: their pace is
`CalderTraffic.CRUISE`, and there is one car to a way round because `ROUND_OF` says so.
