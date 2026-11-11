# Bike night 18: what changes in the town (0.4.486)

The town's note (`docs/BIKE_NIGHT_TOWN_0_4_481.md`) ended with what was not there yet:

> - **Things that change:** the dryers turning, the grocery's lights going off aisle by aisle, the neon's one bad letter,
>   tyre tracks in the dew on the field (the tracked turf is drawn and not yet laid).
> - **Sound.** The town is silent.

and the proposal (`docs/BIKE_NIGHT_PROPOSAL.md`) has, of the laundromat, "Lit, with dryers tumbling. A woman is folding
alone at the counter, and a television on a bracket plays a late horror film."; of the drive-in, "in neon that buzzes and
flickers one letter"; of the grocery, "Closing up: the lights go off aisle by aisle"; of the school, "Riders leave tyre
tracks in the dew on the field"; and of the town's sound, "distant music from a car radio, the dryers, the neon's buzz,
the theater's laughter".

All of that is here. The grocery's stock boy and his chain of carts are the next slice.

## Pictures that move

A shop's front is a picture hung on its wall, cut into blocks. Two of them move now. Each is drawn as its frames one under
another, with a card beside it that tells the game how they are cut and how long each is up; the game plays them by
itself, on every client, with nothing sent.

- **The Wash-O-Mat,** eight frames, a fifth of a second each. Four of its six dryers turn, what is in them going round
  an eighth of a turn a frame; the other two have stopped, with a heap at the bottom. **A woman stands at the counter
  alone:** she holds a sheet up by its corners, brings them together, lays it on the counter and smooths it down. **A
  television on a bracket** over the last dryer has a late film on: a house on a hill under the moon, a light coming on
  in it, a face in the dark, the reel jumping, somebody at the top of the hill with their arms up.
- **Big Top Burgers' cabinet,** four frames: a ship along the bottom of its screen, a shot going up, three rows of
  things coming down.

`tools/draw_borrasca_signs.py` (the signs' shared maker) learns `FRAMES`: a sign's name, how many frames, how many ticks
each. A block's model is cut from all the frames laid over one another, so it holds whatever any frame draws. Drisking's
signs have no frames and are as they were.

## The bad letter

The G of BURGERS goes out and comes back: out for a moment, one time in four for a second or two; back for a few
seconds, one time in three only for a moment. Both of the burger place's signs do it together (its front, and the one on
its back wall over Main Street), and it is heard at each: a short hard buzz, and a tick as the tube lets go.

## Closing up

Half a minute after the town wakes, the Food Fair's three windows go dark one after another from the west, eight seconds
apart, each with the clack of a switch at the box. The specials on their butcher paper can still be read against the
dark. Its name stays lit over the door.

An aisle whose time comes while that part of the town is asleep is dark when it wakes, and was not heard.

## Tracks

A bike going over the school field's turf, on the ground and moving, leaves the tracked turf under it: a reader's, one
of the three's, Gus's. A bike in the air leaves none, nor one standing, nor anybody on foot. The chalked lines stay
chalk.

## What is heard

| | where | how far |
|---|---|---|
| The dryers, three seconds of them, over and over | the laundromat's glass | about 20 blocks |
| A car radio, eight seconds of a tune, over and over | the blue sedan two of the teenagers sit on | about 20 |
| A full house laughing, every fifteen to thirty-five seconds | the theater's doors | about 24 |
| The neon's buzz | each of the burger place's signs | about 14 |
| The switch | the grocery's windows | about 28 |

The dryers and the radio are played only while somebody is near enough to hear them. All five are made in code, mono,
and subtitled (`tools/generate_bike_night_audio.py`, 37 cues). The ones that go on and on are made quiet in themselves:
a sound is sent further than sixteen blocks by being played louder than one.

## When, and the night's end

All of it begins when the town wakes (the gate is opened), as the cars and the people do. A night's end lights the
windows, makes the sign whole and takes every track off the field. If that part of the town was asleep when the night
ended, the next night does it the first time it finds it awake, before anything else.

## Cases

Three, in `BikeNightTests` (64 in all):

- until the town wakes the sign is whole and the grocery lit; then the G goes out and comes back on both signs
  together, the three windows go dark from the west some seconds apart with the name still lit, each sound is heard by
  the reader beside it and not by the one across town, the moving pictures have their cards, and the night's end puts
  it all back;
- a bike standing on the field leaves nothing, and neither does somebody on foot; ridden ten blocks up it, the turf
  under it is tracked and the turf either side and the chalk are as they were; the night's end leaves no track;
- nothing is heard of the town before it wakes; then the dryers outside the laundromat, and they go on; the radio by
  the car, with the dryers out of earshot; the house laughing under the marquee; and none of it in the park.

## Not seen

None of this has been seen or heard in a client. The frames were looked at laid out side by side
(`scratchpad` render), which shows what each frame draws and not how it reads in motion at riding height. The five
sounds were checked only as numbers (length, level, that they begin and end quietly). Whether the woman reads as
folding, whether the film reads as a film, and whether the radio's tune is a tune, are the owner's to judge.
