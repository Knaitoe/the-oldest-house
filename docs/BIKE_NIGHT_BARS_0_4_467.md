# Bike night 5: the monkey bars (0.4.467)

**Changed in 0.4.470** (`docs/BIKE_NIGHT_RHYTHM_0_4_470.md`): going along the bars is the proposal's timing game with the strafe keys, not the walk keys at a steady pace as written below, and a jump at the bars grabs one. What is below is what 0.4.467 built.

The fifth slice of Bike night (`docs/BIKE_NIGHT_PROPOSAL.md`). The owner's note:

> Lots of fun potential for unique interaction here - a seesaw, slides, swings, monkey bars, etc

The third piece of equipment on the playground, and the first with a verb the game does not have: hanging by the hands. The
seesaw comes next, then the rest, then Gus.

**It still cannot be finished, so the House does not deal it.** Nothing is a Witness source.

## The monkey bars

They stand along the south side of the playground's sand, running east and west.

- **The ladders.** One at each end, two blocks tall, facing the rungs, in the bars' own paint. They are climbed as any ladder is.
- **The rungs.** Six blocks of them between the tops of the ladders: two rails in line with the ladders' rails, and a rung every
  half block with the paint gone from it. They are two blocks over the sand. A reader in this night is a child, a little under a
  block tall, so the rungs are out of reach from the ground and there is room to walk under them. A body can also stand on top of
  them, and walk along them.
- **Taking hold.** A hand on the rungs from under them takes hold, if the reader's fists could get there: from up either ladder,
  or at the top of a jump under them. A child standing on the sand cannot, and is told so; so is one beside them or past their
  end, and one standing on top of them.
- **Hanging.** A reader who has hold hangs at their full length under the rung over where they were, their feet well off the
  sand. As the others see them their arms are straight up, and they are not sat down as on a seat.
- **Hand over hand.** The walk keys take them along the bars the way they are looking, forward or back, a rung to a hand, slower
  than walking; a look square across the bars goes neither way. A rung is heard each time a hand comes down on one ("Rung rings").
  They stop at the last rung each way.
- **Letting go.** Sneak lets go, and they drop where they hang. The sand is soft.
- **Across.** A reader who gets from the last rung at one end to the last rung at the other on one hold has gone across, and is
  told so. Their record counts their holds and their crossings, and their account tells of them.
- Two readers can hang from the same bars at once. Each has their own hold, and they pass each other.

### How it is made

The rungs are one more kind of the playground's block (`CalderPlayBlock.RUNGS`), a thin run at the bottom of its block; an end
of them is a stage whose rails run on over its ladder's. The ladders are the slide's ladder at two more stages, in the bars'
paint.

The hold is its own small thing (`BarsHang`): an unseen thing at the rungs' height that the reader rides, which does not sit
them down and hangs them under it by the reach of their arms. The server moves it from the reader's walk keys and the way they
are looking; it is sent every tick. It is never saved, and it is gone when its rider is, or when the rungs it is under are.

What stops a reader on the ground is arithmetic, not a wall: their height, how far their fists are over their feet with their
arms up, and a little stretch, against how high the rungs are. A jump in this game is not smaller for a smaller body, so a child
at the top of a jump reaches them.

## How it is drawn

`tools/draw_calder_play_art.py` draws four more models: a block of rungs, an end of them, and the two blocks of the ladder in
the bars' paint. No new pictures. The review sheet showed the rails and rungs where they were meant to be at the first drawing.

One more sound from `tools/generate_bike_night_audio.py`.

`BarsClient` raises a hanging reader's arms (through the hook the swings use), one hand ahead of the other by turns as they go
along, with their legs trailing behind the way they are going.

## Tests

`BikeNightTests`, one more case (twenty-one in all), building the place itself in a batch of its own.

- **The monkey bars are crossed hand over hand by readers who can reach them.** Every block of them and which way it faces; that
  the ladders are climbed and the rungs begin beside their tops; that the rungs are stood on, with room under them; where the
  first and last rungs are. A child on the sand cannot reach them, and is told so; nor one beside them, past their end, or on top
  of them. From up the west ladder a hand on the rungs takes hold over the first rung, and nobody takes a second hold. The reader
  hangs at their full length, their feet off the sand, and is not sat down. Hanging still they stay; looking square across, the
  walk keys go neither way; looking along, forward goes along no faster than a hand goes, and backwards goes back. A second reader
  at the height of a jump under the far end catches the rungs, and both hang at once. The first goes on to the last rung and no
  further, is told they are across and (once) how it is done; the rungs are heard; their records have one across and the other
  only hanging. Letting go leaves a reader where they hung and their hold is gone, the other reader's is not; the rungs taken
  from over a reader take their hold with them. Both accounts say what each did.

The case for what a hand on the park's things says tries both of the bars' ladders, and their rungs from too far off.

The client proof has a tenth page: the bars as the builder puts them up, from one side. I looked at it.

I looked at the bars in the park in seven pictures made with the local aid that is not part of the build: from the sand south
of them, along them from outside each ladder, up at the rungs from under them, across the playground from the swings, along
their top from on top of them, and from the east. They stand well over a child's head and their rails meet the ladders' at each
end. Nothing needed changing.

## What I have not seen

- **A reader hanging.** The pose is written from the model's numbers. Nobody has looked at one: whether the hands meet the rungs,
  whether the arms' turn about reads as hand over hand, whether the legs' trailing looks right. The proof's page is the bars
  alone.
- **Taking hold by hand.** The case puts a reader where a climber or a jumper would be and puts a hand on the rungs. Nobody has
  jumped and clicked. How hard it is to catch them at the top of a jump, I do not know.
- **The pace.** A hand goes a block in a little under a second. Whether that is right, I do not know.
- **Its sound, and the night.** As for the rest of the park.

## Numbers

No layout, protocol or Witness change. New builds only.
