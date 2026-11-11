# Bike night 7: the merry-go-round (0.4.469)

The seventh slice of Bike night (`docs/BIKE_NIGHT_PROPOSAL.md`). The owner's note:

> Lots of fun potential for unique interaction here - a seesaw, slides, swings, monkey bars, etc

The fifth piece of equipment on the playground. The small things come next (the spring rider, the dome, the fountain, the
payphone and the carved bench), then Gus.

**It still cannot be finished, so the House does not deal it.** Nothing is a Witness source.

## The merry-go-round

It stands in the north of the playground's sand, between the swings and the slide.

- **The wheel.** A round deck three blocks across in four painted quarters, a post at its hub, and four rails from the hub out to
  posts at its rim. It really turns: it is drawn as far round as it is.
- **Shoving it.** A reader on the sand beside it puts a hand on it, and it goes the way they are facing along its rim: a hand on
  the rim to their left takes its near side to their left. A hand straight at its middle turns it neither way, and says what it
  is. One shove at a time; several, or several readers, take it up to its fastest, about a turn in a second and a quarter. It
  cannot be shoved from on its deck, or from too far off, and a reader who tries is told so. A hand finds it anywhere over its
  deck up to its rails, so a reader running round beside it with the use key held keeps shoving it, as the proposal has it
  ("Readers spin it by running alongside, then jump on").
- **It runs down.** Left alone it slows and stops, and stays as far round as it stopped.
- **Standing on it.** The deck is stood on and walked on, out to its rim. Whoever stands on it goes round with it and is turned
  with it. So does anything left lying on it.
- **Holding on.** Sneaking, a reader keeps low and holds on, however fast it goes.
- **Being thrown off.** Standing up on it while it goes fast, a reader is pushed outward, harder the faster it goes and the
  further out they stand, and slides off its rim onto the sand. Nobody is hurt.
- Its bearing is heard each quarter of a turn ("Merry-go-round squeaks").
- A reader's record has how often they shoved it, how far round they have gone on it, and whether they came off it while it was
  going fast. Their account tells of it.

**The rails stop nobody.** They move, and the game has no moving thing a body can walk into. The deck and the hub's post are
solid.

### How it is made

The merry-go-round is its own block (`RoundaboutBlock`, `calder_roundabout`): nine cells of ground, the hub and the eight round
it, none of which draws anything. A round deck is over the same ground however far round it is, so each cell stops a body just
where the deck is over it: every two-pixel square of ground whose middle is within the deck's radius of the hub. The art tool
cuts the deck that is drawn by the same rule.

The hub keeps the wheel (`RoundaboutBlock.Wheel`, the block's entity): how far round and how fast. It keeps most of its speed
each tick. The server and each client step it alike, and the server sends where it is at each shove and once a second while it
turns. The hub's renderer (`RoundaboutRenderer`) draws the whole wheel, one model that is never a block in the world, turned
about the hub.

**Who carries a reader round.** Where a reader stands is their own game's to say, not the server's. So it is each reader's own
game that carries them (`RoundaboutClient`): after each tick, if they are standing on a wheel's deck, they are turned about its
hub by as far as it turned in that tick, and pushed outward if they are standing up and it is going fast. The server carries
what is nobody's own to move: a thing left lying on the deck. The server reads who is on the deck, for their records.

## How it is drawn

`tools/draw_calder_play_art.py` draws one more model (the wheel, fifty-four pieces) and the merry-go-round's block states.

One more sound from `tools/generate_bike_night_audio.py`.

## Tests

`BikeNightTests`, one more case (twenty-three in all), building the place itself in a batch of its own.

- **The merry-go-round is shoved round from the sand and carries whoever stands on it.** Its nine cells and that only the hub
  keeps the wheel; the deck's height over each, and the hub's post; that a hand finds it up to its rails' height; that the deck is stood on out to its rim every way and its
  ground's corners are open sand. No shove from on it, from too far off, or straight at its middle, and what a reader is told
  each time. A hand on its rim to a reader's left shoves it that way; a second at once adds nothing; it turns and slows by
  itself; a shove the other way slows it; it goes no faster than its fastest. A reader standing on the deck goes round as far as
  it has turned, no nearer its middle and no further, and is turned with it (the case does what the reader's own game does, by
  the same code); a stick left lying on the deck goes round too, carried by the server. Standing up on it while it goes fast a
  reader is pushed outward; keeping low they are not; going slowly it throws nobody. Their record has their turning; they were
  told once how to stay on; its bearing is heard. Out past its rim while it still goes fast, it threw them. The other reader's
  three shoves are in their record. Left alone it runs down and stops. The accounts say what each did.

The case for what a hand on the park's things says tries the merry-go-round's hub.

The client proof has a twelfth page: two merry-go-rounds drawn by the hub's renderer, one as it was built and one a little way
round. I looked at it.

I looked at the merry-go-round in the park in pictures made with the local aid that is not part of the build (which I taught to
draw the wheel as the hub's renderer does, as it was built): from the sand south of it, from close beside it, from on its deck,
down on it from the slide's deck, and across the playground from the swings. It is low and round, its rails are at a child's
chest, and it sits clear of the swings and the slide. Nothing needed changing.

### What the local runs found

- **The case hushed a reader and then waited for a line they had already been told.** They are told how to stay on once, the
  first time they go round. The case no longer forgets it. Nothing in the game changed.

## What I have not seen

- **A reader being carried.** The carrying is a reader's own game's to do, and no game with a person at it has been run. The case
  calls the same code on the server. Whether going round feels right, whether the turning of the view is welcome or sickening,
  and whether walking about on a turning deck works, I do not know.
- **Being thrown off.** How hard the push outward is was chosen, not tried.
- **The wheel turning.** The proof's page is two still pictures.
- **Shoving it by hand.** The case puts a hand on its rim from the sand. How it feels to get it going, I do not know.
- **Its sound, and the night.** As for the rest of the park.

## Numbers

No layout, protocol or Witness change. New builds only.
