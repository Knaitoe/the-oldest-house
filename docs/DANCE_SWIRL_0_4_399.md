# The dance turns smoothly and swirls, the title fades out, the book on the reading table (0.4.399)

Four of the owner's notes from November 8.

## The dance turns smoothly

The owner: "dancing animation needs to be smoothed out."

Since 0.4.397 the view has turned with the swing so the partner stays in front. It turned once a game tick, twenty times a second. The reader's own view is not smoothed between ticks, so it turned in jerks.

Now the view turns every frame, by exactly as far as the partner has gone round the reader since the last frame. Both of their positions are taken as drawn, smoothed between ticks. The partner stays in front through a swing or a swirl, and the mouse still looks about freely (`DanceClient.follow`).

## Pulling against the weight swirls the partner

The owner: "continuing to pull against the weight should swirl your partner."

A push away still leans the reader back against the partner's hands. Keep pulling, and after about three quarters of a second the pull swings the partner round the reader instead (`DanceWeight.swirl`, `HouseReadShelter.swirl`):

- **Which way.** The swirl goes the way the reader was last swinging, or anticlockwise from a standing start.
- **How fast.** It builds the longer the pull is held, up to about one turn in a second and a half.
- **Let go.** It carries on a little, then dies away, as the swing does.
- **The lean holds.** While swirling, the partner keeps the distance the reader holds them at, between arm's length and the hands' give. They keep facing the reader the whole way round.
- **Both strangers.** In the dance with both, each is swirled round the reader in the same way.

The server sees the pull without any new message. The partner is pulled more than a tenth of a block behind arm's length, tick after tick. A pull that stops short of three quarters of a second is only a lean, as before. A swirl is not saved: a reload begins the dance without one.

## The title fades out

The owner: "can we make the 'This House Has Ghosts in It' text at the end of the personhood vignettes fade out, rather than cut out?"

The last quiet room's closing card ends on the title. Its text faded quickly, and then the black screen closed all at once onto the House. Now, after the title's own time:

- the title fades over a second and a half;
- the black lifts over two and a half seconds;
- the card closes once it has cleared.

The reader has already been carried into the House under the black, so what shows through is where they are going. Every other card still cuts, as the rooms are meant to be cut off.

## The book on the stacks' reading table

The owner: "no interaction here, in the book maze." The reading table in the clearing at the middle of the library stacks holds the one book that is out, and it gave nothing to a hand. Now it reads like the stacks' other answers. A page turns and a grey line shows, one in turn for each reader:

1. *The one book that came off the shelves. It is open at a plan of the stacks, with this table marked.*
2. *Someone has drawn the way out in pencil. It goes the long way round.*
3. *There is a pencil mark beside the table, where you are standing.*
4. *The plan of the stacks. The way out, the long way round.*

It cannot be lifted, and it gives no mark and no credit.

## Tests

- **`HouseReadTests.theTwoOriginalStrangersComeOnlyWhenLookedAtAndADanceEndsTheSong`:**
  - *In the room:* the reader is held leaning away from her for about two seconds. She is swirled round them by more than 25 degrees, stays in their hands and is still dancing.
  - *The rule itself:* a short pull is only a lean. A long one builds the swirl the way the reader was swinging, up to its fastest. Let go, it carries on the same way, then dies away.
- **`ExplorationPassTests.theLibraryStacksAreASmallMazeWithTwoWaysOutAndBooksThatWillNotComeOut`:** the book on the reading table is read twice and stays on the table.
- **The house-read client check (`HouseReadLineProof`):** the title card stays up while the title fades and the black lifts, and closes once it has cleared.

## What stays

- **Witness:** 81 / 61 / two kinds / three endings.
- **Protocol:** 163.
- **Layout:** 130.
