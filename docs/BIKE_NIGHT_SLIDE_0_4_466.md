# Bike night 4: the slide (0.4.466)

The fourth slice of Bike night (`docs/BIKE_NIGHT_PROPOSAL.md`). The owner's note:

> Lots of fun potential for unique interaction here - a seesaw, slides, swings, monkey bars, etc

The second piece of equipment on the playground. The seesaw and the monkey bars come next, then the rest, then Gus.

**It still cannot be finished, so the House does not deal it.** Nothing is a Witness source.

## The slide

It stands in the playground's north-east corner, under the lamp there, and goes down to the south.

- **The ladder.** Two blocks of it up the back, its rungs against the deck. It is climbed as any ladder is.
- **The deck.** A tread plate in a painted frame two blocks up, on four legs, with room to walk under it. Over it a guard: a rail
  and a mid-rail each side, open to the ladder behind and the chute in front.
- **The chute.** Four blocks of it, a steel sheet worn bright down the middle between two painted lips, falling at 22.5 degrees,
  and then a level end a third of a block off the sand. A body can stand on the chute and walk on it, up or down: each block is
  two steps a child takes. The top two blocks have room under them.
- **Going down.** A reader who comes into the top block of the chute (stepping off the deck, or climbing up the chute to it) sits
  and goes down it, faster all the way: a little over a second from top to bottom. At the level end they are let go with the speed
  they came down with, out over the sand ("Someone slides").
- **Getting off** part of the way down leaves a reader where they are, on the chute; that is not a go down it. In the top block
  of the chute it puts them back on the deck. Left inside the top of the chute they would be taken down again at once, and again
  for as long as they held the key that got them off. I found that reading the code before this was committed, not in a run.
- Somebody already riding something is not taken.
- A reader's record counts their goes, and their account tells of them.

### How it is made

The slide's pieces are four more kinds of the playground's block (`CalderPlayBlock`: LADDER, DECK, CHUTE, RUNOUT), each facing
the way the slide goes down. The ladder says it is one (`isLadder`), so nothing is added to the game's list of climbable blocks.
The top block of the chute takes a reader who is inside it (`entityInside`).

The go down is its own small thing (`SlideRide`): an unseen seat the reader sits on, whose place is worked out from when the go
began, on the client and the server alike, so nothing is sent while it runs: along the ground it has gone half the gain times the
ticks squared, until the chute's four blocks are behind it, then level at the speed it had. It is never saved, and it is gone
when its rider is.

## How it is drawn

`tools/draw_calder_play_art.py` draws nine more models and two more pictures (the chute's sheet, the deck's tread plate). A block
of chute is one sheet and two lips turned 22.5 degrees about the middle of its block; where its surface is at each edge is worked
out in the tool as it is in the game (the deck's height, less the fall of the blocks above). The first drawing had the chute
falling the wrong way (up to the north in a model drawn going down to the north); the review sheet showed it. The tool's flicker
rule caught the ladder's rails sharing a face with the feet they stand on.

One more sound from `tools/generate_bike_night_audio.py`.

## Tests

`BikeNightTests`, one more case (twenty in all), building the place itself in a batch of its own.

- **The slide is climbed, and a reader who comes into the top of its chute goes down it.** Every block of it and which way it
  faces; that the ladder is climbed and the deck is not; that the deck is two blocks up, its guard a rail each side and open at
  both ends, with room under it; that each block of chute is stood on, a step down from the last by the same fall, with room under
  the top of it; the level end and the open sand past it; the ride's own arithmetic. Then a go down: a reader in the top block
  sits, looking down it; nobody is taken twice, or by any other block; the ride is where the chute's surface is on every tick,
  lower and faster each time; at the end the reader is let go where the level end stops, with the speed they came down with; it is
  heard; their record has it. A second reader gets off in the top of the chute and is back on the deck, out of the chute; goes
  again, gets off further down and is left there; neither is counted. The first goes twice more, and their account says so.

The case for what a hand on the park's things says tries the slide's ladder and its level end.

The client proof has a ninth page: the whole slide as the builder puts it up, from one side. I looked at it.

I looked at the slide in the park in seven pictures made with the local aid that is not part of the build: from the sand beside
it, up the chute from its foot, up the ladder from behind, across the playground from the swings, down the chute from the deck,
from under it, and from the side path. Nothing needed changing.

## What I have not seen

- **A go down it from the seat.** The case rides it and checks where the ride is on every tick; nobody has sat on it. Whether a
  second and a bit is the right length, and whether being let go at the end feels like coming off a slide, I do not know.
- **The climb.** The case checks that the ladder is one the game climbs, and that the deck is level with its top. Nobody has
  climbed it by hand and stepped onto the deck.
- **Walking up the chute.** Each block of it is two steps a child's legs take, by the numbers. Not walked.
- **Its sound, and the night.** As for the rest of the park.

## Numbers

No layout, protocol or Witness change. New builds only.
