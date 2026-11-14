# The thirteenth witch comes ashore (0.4.589)

The owner, playing on 14 November:

> "witch is still not wanting to cross over to land"

This is the third time it has been said. Before it: "problems traversing from water to land" (fixed in 0.4.163, the
step up the bank's lip) and "Stacey graves still isn't wanting to cross to land" (0.4.201, a reader standing where she
may not go left her with no route).

New builds only. No line, protocol, layout or Witness change. Costume Night only: the other lakes, the drowned town
and Movie Night are untouched.

## What was found

- **She could cross, and did.** A case put a reader on the sand with their back to the lake after the lights went
  out: she was out of the water in two and a half seconds, at the reader in four, struck, and was back in the lake two
  seconds after that.
- **A look held her at any distance.** Her rule on this beach was to move only while no reader there could see her.
  It had no range, so a reader at the far end of the beach, looking her way, held her where she stood.
- **The scene tells the reader to look.** When the lights go, Hank says "Keep your eyes on it. Whatever you do, don't
  you look away from it." A reader who does as he says holds her in the lake for the rest of the night. A reader who
  turns away is struck from behind and, turning round, finds her already on her way back to the water.
- **So she was never seen on land.** She was on the sand only in the seconds nobody was looking at her.

## What it is now

After the lights go out:

- **A look holds her only from near.** Within eight blocks of a reader who can see her she does not move at all, as
  before.
- **Seen from further off, she comes ashore.** She steps up out of the lake onto the nearest sand, then crosses the
  beach toward the reader she is after, on all fours, at a crawl (a little over a block a second), in full view. She
  is one more of the thirteen crawling on the sand.
- **She stops dead where the look reaches her,** eight blocks off, and waits there for it to break.
- **Seen by anyone, she strikes nobody** and comes no nearer than three blocks to the reader she is after. Her swipe
  in passing is held too.
- **A back turned, she is as fast as she was,** strikes as she did, and goes back to the water for four seconds as she
  did.

Left as it was:

- **Before the lights go out.** She keeps to the water's edge, moves only unseen, and strikes nobody.
- **Where she may not go:** the park's grass, the ground by the entrance, the stage and the props.
- **Every line.** Hank's, and the account's "One of them held still whenever I looked at it. Every time I looked back,
  it was nearer."

## Checks

- **A new case** (`VignetteAnimationTests.afterTheLightsGoSheComesUpTheSandToAReaderWhoWatchesHerFromInland`), with a
  reader standing eleven blocks inland and looking at her the whole time:
  - she has left her place in the lake within a second, and is ashore within two;
  - six and a half seconds in she is on the sand, stopped just inside eight blocks of the reader;
  - no step of hers on the way was faster than the crawl;
  - the reader is unhurt and she was never within three blocks;
  - she does not move at all for the next two seconds, while the reader still looks;
  - the reader turns their back, and within four and a half seconds she has reached them and struck.
- **The beach's other four cases** pass: watched stillness and the unseen strike before and after lights out, Hank's
  numbers and the ribbon, the crossing to the park, and the two-reader scene check.
- **Twenty other cases with her in them** pass: Movie Night (3), Winter Lake (1) and the sixteen of her own class.
- **A compile.**

Not checked:

- **Nobody has watched her come up the sand in a played game.**
- **Two readers.** With one reader watching from far off and another with their back turned, she should crawl to
  three blocks from the second and wait; no case does this.
- **The crawl's look.** She uses the same all-fours pose and walk she has everywhere; at this pace it has not been
  seen in a client frame.

## Caught while checking

- **At a crawl she shuffled on the spot.** A new plan is made every half second, and each begins by centring the
  block she is in, which matters at a corner in the town. A crawl covers six tenths of a block in half a second: three
  tenths back to the middle and three tenths on again. The case caught her going nowhere. On this beach a plan no
  longer sends her back along the line she is already on.
- **She slid along the water's edge.** Left to the shortest way, she went up the lake's first column for five blocks
  before stepping onto the sand, which is not coming ashore. Seen in the water, she now makes for the nearest sand
  first.
- **The case's first tick.** The reader was set looking at her a tick late, and in that tick she ran. The case now
  looks before the first tick.

## For the owner

- **Eight blocks and the crawl's pace are mine.** Both are one number each (`COSTUME_HOLD`, `COSTUME_CRAWL`). Nearer
  than eight and she is close enough to reach a reader in one turned head; further and she stands a long way off.
- **The account's line is unchanged and is now true only from near:** "One of them held still whenever I looked at
  it." From far off she moves while looked at, as the other twelve do. Say if the line should change, or if she should
  be held from further.
- **In creative or as a spectator she ignores the player,** as she always has. To see this, play it in survival.
- **Before the lights go out she still stays in the water.** If she should come ashore during the contest too, that
  is a different rule and I have not made it.
