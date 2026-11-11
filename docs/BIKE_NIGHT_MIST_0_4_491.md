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

Protocol 183 (one payload: how thick a reader's mist is).
