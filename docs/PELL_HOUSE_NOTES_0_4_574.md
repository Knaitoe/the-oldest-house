# The Pell house, the owner's playtest notes: the front beds (0.4.574)

From the owner's notes on playing the Pell house on 13 November (the earlier batches: `docs/PELL_HOUSE_NOTES_0_4_566.md` to
`docs/PELL_HOUSE_NOTES_0_4_573.md`). New builds only. No protocol, layout or Witness change.

The owner: "flowers on pell-house could use an improvement".

This is the third of the three parts that began with the kitchen (0.4.572) and the walls and floors (0.4.573).

## What was there

Five frames of the front of the house, from eye level, before anything was changed.

- **The stakes.** Six in the bed west of the step, each a stick two thirds of a block high with a tag across it. The seedling
  at each was a box a quarter of a block across with a few pixels on it. From the path they read as sticks.
- **The bed east of the step** had nothing in it at all.
- **The beds' edge.** The mulch met the lawn with nothing between them.

## The stakes

- **A stake** now stands as high as a child's shoulder, its label near the top of it, on the side to the street.
- **What grows or grew at its foot** is a picture on two crossed planes, as the game's own flowers are, standing in front of
  the stake.
- **Nessa's five** are no longer one model five times. Along the row: a seedling slumped over, one dried upright where it
  stood, a slumped one, one gone (a loop of twine left on the stake and a leaf on the mulch), a dried one. No two side by side
  are alike. A hand on each says which it is:
  - "A stake with a dead seedling. The tag says NESSA, in a child's printing."
  - "A stake, and a seedling dried upright where it stood. The tag says NESSA, the S the wrong way round."
  - "A stake with a loop of twine on it and nothing in the loop. The tag says NESSA. The pen has run."
- **The labels.** A name five letters long cannot be drawn in a label nine pixels wide at sixteen pixels to the block. Nessa's
  are uneven marks in felt pen, off the line; Tilly's are upright strokes of one height. The hand says the name.

## Tilly's stake

The newest stake, nearest the step, is Tilly's. It changes with the visits, as the knife block and the calendar do
(`PellWeek.dress`).

| Visit | What stands there | A hand on it |
| --- | --- | --- |
| The first | A fresh seedling | "The newest stake, a fresh seedling. The tag says TILLY, in careful capitals." |
| A week in | The seedling a hand high, tied in to the stake with twine, a tight bud at its head | "The newest stake. The seedling is tied in to it, a hand up from the mulch. The tag says TILLY, in careful capitals." |
| The last night | The plant in flower; a second label tied over the first | "The newest stake. The plant is in flower. A second tag is tied over the first: NESSA, in careful capitals. A corner of the first shows under it." |

The second label is the chore chart turned round. In the kitchen a strip with TILLY is stuck over Nessa's name; by the last
night, in the bed, NESSA is tied over Tilly's.

A reader who has put a hand on the stake can tell Tilly about it, in Nessa's room, where she asks what things look like
(0.4.571):

| Visit | The reader | Tilly |
| --- | --- | --- |
| The first | "There's a stake in the front bed with your name on its tag. A seedling." | "My name? We only got here today." |
| A week in | "Your plant out the front has grown. It's tied to its stake now." | "June waters it with me. She puts my hand on the can and hers over it." |

On the last night Tilly asks nothing (the house is not waiting on an hour then, and she asks only while it waits), so the
second label is there to be seen and is not hers to be told.

## June's geraniums

The bed east of the step has a geranium in every cell, two plants turn about, so the row is not one picture repeated. A hand
on one: "Geraniums, deadheaded. The mulch under them is wet." June keeps one bed and leaves the other exactly as it was.

## The edging

Bricks on edge run along the lawn side of both beds, with one brick turning each far corner and a block of them up each far
end. There are none across the path. They stop a block short of the wall at each end: that last cell is where the painted
line crosses the bed's end, and no brick stands on the line. A hand on the edging: "Bricks on edge along the bed. They are the
newer brick, the same as the patch in the west wall." They are what was left over from bricking up the window behind Nessa's
bed.

None of it stops a body: a reader walks along the beds and in among the stakes, as June does with the tin.

## The house afterwards

In the empty house (0.4.542) the edging is still there. The stakes, Tilly's plant and the geraniums have gone with everything
else that was not built in.

## What the frames show now

Twelve frames from eye level.

- **From the path:** a row of labelled stakes to the left of the step, a flowering border to the right, and a line of brick
  between both beds and the lawn.
- **The west bed, three blocks out:** Nessa's labels in a child's felt pen, a dead seedling the colour of straw at the foot of
  each stake, each a little different.
- **Tilly's stake close up,** with the three visits stood side by side for the frame: the fresh seedling, the tied-in one and
  the flower with its two labels.
- **The east bed:** the geraniums, a plant to a cell.
- **Along the bed from its west end:** the painted line in the row by the wall, the stakes in the other, the edging.
- **Both far corners:** the edging turning the corner and stopping where the line crosses.

The frames are drawn at full brightness and show shapes and pictures, not the game's light; the ground draws darker in them
than it is in play.

## Checks

- **New case** `PellHouseTests.theFrontBedsHaveTheirPlantsAndTheirEdgingAndTillyHearsOfHerStake`:
  - Nessa's five stakes as above, each on the bed's mulch with its label to the street, no two neighbours alike;
  - Tilly's nearest the step, as the first visit has it;
  - a geranium in every cell east of the step;
  - edging on the lawn along both beds on the side to the bed, none across the path, a block up each far end, a brick in the
    right corner of each corner cell; and where the line crosses each end, the line in the ground and nothing over it;
  - nothing of it stops a body, and a body walks along the beds and into them;
  - a hand on each gets its line, and every stake as it stands has a line of its own;
  - the reader's hand on Tilly's stake is kept, the panel then offers it to tell her, and she answers.
- **A week in** (`aWeekInTheHouseIsAsJuneHasMadeItAndSheSlipsOnTheName`): the seedling is tied in; a reader who has put a
  hand on it is offered it to tell Tilly, and she answers.
- **The last night** (`theLastNightBeginsWithThePoolLitTheKnivesPutAwayAndCallumOnTheLine`): the plant is in flower with
  the second label.
- **A reader who has had neither visit** (`theWeeksCallAndTheVisitsAfterIt`) finds it as it was the day they came.
- `tools/draw_pell_things.py --check`: 72 kinds, 211 textures, 149 models.
- **The Pell house namespace**: 61 cases. Architecture: 6 cases. The cast and two-reader namespaces: 102 cases.

## What local testing and review found

- **The first run of the Pell cases failed two of the new checks.** Neither was a fault in the house.
  - The week's case put its reader's hand on the stake from the far end of the hall. A hand reaches six blocks and no further,
    so the click was dropped and nothing was kept. The reader now stands at the bed for it.
  - The bed case looked for the painted line a block too high. The line is the ground block itself with paint on its top. The
    case now asks for the line in the ground at each end of the beds, and for nothing over it.
- **A third answer of Tilly's could never have been heard.** It was written for the last night, about the second label. She
  asks what things look like only while the house waits on its hour, and on the last night it waits on none. The line and
  the record kept for it were taken out before anything was committed.
- **Considered and not built:** running the edging over the painted line with a band of paint across the bricks. The line is
  scrubbed out a quarter at a time, painted again by June, and gone in the empty house, and a painted brick would have had
  to follow each of those. The edging stops short of the line instead.

## For the owner

Two things here are new story and not only better pictures. Say if either should go.

- **The second label on the last night**, NESSA tied over TILLY.
- **Tilly's two answers** about her stake.

One thing was written and left out. Told on the last night that a second label says Nessa, Tilly answered "She doesn't say
sorry now." (a week before, told June had called her Nessa, she says "She says sorry after. She always says sorry."). Nothing
on the last night has her asked anything, so the line had nowhere to be said. If it is wanted, it needs a moment on the last
night when a reader can talk to her.

## Still to come

- **June's room.** It has its paper and its carpet and nothing in it, and it can be seen into when June stands in it with her
  door open. It will be furnished. A hand on her door from inside the room will open it, and June will not shut it on a reader
  who is inside.
- The furniture's own textures, where the frames show them flat.
- Each vignette door's own bleed and sound in the halls.
- Put by for later: a ball, and a game of catch with Callum; taking Tilly round the house by the arm.
