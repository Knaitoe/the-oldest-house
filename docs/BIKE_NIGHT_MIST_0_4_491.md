# Bike night 22: the mist, and the town's lights (0.4.491)

The proposal (`docs/BIKE_NIGHT_PROPOSAL.md`, "Act three: the graveyard"), and what was answered on November 11:

> - **The mist.** It rises off the grass and thickens with each row. The Lamp's headlight cuts it. Without it, the stones
>   come up close and sudden.
> - **The mist** is each reader's own, thicker with each row. The Lamp pushes it back for its rider and whoever rides
>   close.

And the owner's first note of the graveyard: "the city starts to melt away".

## The mist

**Each reader has their own**, and the server says how thick it is (`BikeNightGraveyard.mist`), in a hundred:

- none outside the railings;
- eighteen just inside the gate;
- about seven more **past each row of stones**, twelve rows, to all of it past the last, where the small stones are;
- **under half of that (forty-five in a hundred of it) within seven blocks of a Lamp that is ridden and making
  light**, for its rider and for anybody else. The Lamp's dynamo makes light only while its wheel turns (0.4.463): a
  Lamp standing still cuts nothing.

A reader is told every half second while they are in it, and once more when they are out of it. Nobody is told
anybody else's.

On the client (`BikeNightMistClient`) it is **how far they see**: with none of it, as far as the game draws; with all
of it, seven blocks; and between the two by shares, so that each row takes about the same part of what is left:

| Mist | Sees about |
|---|---|
| 18 (inside the gate) | 60 blocks |
| 45 (the fourth row) | 30 |
| 73 (the eighth row) | 14 |
| 100 (the small row) | 7 |
| 45, which is all of it beside a lit Lamp | 30 |

At a bike's top speed seven blocks is a little over a second. It comes and goes over a few seconds, never at once: a
reader coming in through the gate rides into it, and a Lamp going past somebody on foot clears their view and lets it
close again. The fog takes the mist's own grey. Wisps rise off the ground round whoever is in it, fewer for whoever has
asked the game for fewer particles.

It is fog and wisps. Nothing flashes and nothing shakes, so there is nothing here for the comfort setting to scale.
(Since 0.4.495 there is one thing: somebody who misses a jump in the Hollow has the mist put over everything at once
for a moment, and that is scaled by the comfort setting, with no floor. `docs/BIKE_NIGHT_HOLLOW_0_4_493.md`.)

## The town's lights

Once **every reader who is still in the night is on the hill, past its first bank**, the town's street lamps go out a
street at a time: Church Lane's, then Elm Street's and the court's, then Main Street's, then the lots' and the park's
own street's, two and a half seconds apart, each with the breaker's sound from where it was.

With a reader still down in the town nothing goes out: it is their town too. A reader who has been caught and is out
is not counted. Once it has begun it goes on to the end.

The park's lamps are the park's, and stay. The night over, the town is lit again.

**Only the lamps.** The shops' signs and windows are drawn lit and are not lights the night can put out; from the hill,
in the mist, they are not seen. If the owner wants the fronts dark too, they need a second drawing each.

## Cases

Two, in `BikeNightTests` (76 in all):

- no mist on the lane, eighteen just inside the gate, no thicker until a row is passed and six to eight thicker past
  each, all of it past the last; the reader at the back is told all of it, the reader in the park is told nothing;
  out on the lane they are told it is gone, and nothing more; with a Lamp ridden past a reader standing by the top
  path, their mist is nearly all of it with the Lamp far off, under half of that for them and for its rider with it
  beside them, and what it was when it has gone on; the Lamp stopped, it is dark and cuts nothing;
- every street of the town has lamps and they are lit; with the chase on, the gate open, one reader up the hill and
  one at the far end of Church Lane, nothing goes out for five seconds; the second comes up the hill and the streets go
  dark from the cemetery down, each no sooner than two and a half seconds after the last, the streets south of the
  newest still lit; then every lamp of the town is out, the park's are lit, the breaker was heard on the hill; the night
  over, every lamp is lit again.

The lights' case failed its first run before anything had gone out: it counted six lamps on Church Lane with four
lit. Two of the six were the posts the street's name hangs on, which stand on a lamp's foot and carry no lantern; the
game's own count was the same count and would have tried to put them out. A lamp is a foot with a lantern over it now,
in the game and in the case.

## Not seen

**None of the mist has been seen**: no case can look at a client's fog, and there is no page of it in the proof. What
is tested is what the server says of it and to whom. How it looks, whether seven blocks is too near or not near
enough, and whether its grey is right against this night's sky are the owner's to judge on a client;
the numbers are `BikeNightMistClient.THIN`, `THICK` and `EASE`, and `BikeNightGraveyard.MIST_GATE` and `LAMP_CUTS`.

Protocol 183 (one payload: how thick a reader's mist is). Since 0.4.495 the payload also says for how long the mist is
over everything, and the protocol is 184.

## 0.4.492: square into a wall, the rider comes off

The proposal: "A stone is a wall: a glance off it is fine, a head-on hit puts the rider over the bars." That is the
bike's own rule for anything it runs into (0.4.463), and **it did not always hold**. What a bike "kept" of its way was
measured as how much of that tick's move it got in before it met the thing. A bike going flat out square at a wall,
with more than two fifths of a tick's travel still to go when the tick began, was let off as a glance; that tick took
its speed to under the crash speed; and the next tick it was standing against the wall, going too slowly to come off,
with its rider on. **About one square hit in five at top speed**, and more at lower speeds, by nothing but where in
its stride the bike happened to be.

What it keeps now is the share of its way that lay along the thing: none of it, square on; half of it, at thirty
degrees to a wall. A glance is what it was. A head-on hit above the crash speed puts the rider off every time, into a
wall, a parked car, a cart, a standing stone, and in Borrasca as here.

The graveyard's riding case found it: it passed three runs and failed one in the same place, with the bike against
the stone at the speed of somebody pedalling into a wall. Its earlier failure (0.4.489), put down there to a wobble
off a low marker, was this, and the record of it in `docs/BIKE_NIGHT_GRAVEYARD_0_4_489.md` is wrong about why.

One case: five bikes flat out and square at a wall, each with a different part of a tick's travel left to it (a tenth
to nine tenths). All five riders come off. Before the change the middle one stayed on.

## 0.4.492: the place at its own height

A place in the outside dimension stands in a stack of slots, each twenty-four blocks high, and Bike night's room was
one slot. The Hollow, which is next, is "a course of floating ground, high and low, over nothing": far over and under
the hill it starts from.

So the place stands at a height of its own now (`BikeNightSite.BASE_Y`, 96), out of the stack, as the lookout, the
Black City, Borrasca and the lake house do; a position is matched to it by its own bounds and not by a slot; and its
room is seventy-two blocks over the park's ground and forty-eight under, where it was eighteen and five. It is no
longer north or south than it was: the room grows north when there is ground there.

Somebody who goes off the edge of the town is put back as soon as they were before, nine blocks down, and not at the
bottom of the new room.

Nothing is built in the new room and nothing else about the night changed. This is on its own so that the cases that
build every place and walk between them (which only the workflow's long job runs) meet it before the Hollow does. Here
it was run against the whole Bike night namespace and the client proof.
