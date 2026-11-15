# A light let fall into the well (0.4.594)

The owner, on 15 November:

> "I need to consider a way for the staircase to keep its immense size and depth for the spectacle, but also to keep
> the player engaged"

On my first suggestion, which showed the well's lamps and fires through the dark:

> "I'm uncertain about 1, too. I remember the staircase as dark in the novel. You've got the pdf of house of leaves,
> no?"

And on what I proposed after reading it ("a light the reader lets fall over the rail and watches until the dark takes
it, with no sound of landing until they are near the bottom"):

> "Sounds good. I agree with and appreciate the res of your suggestions too"

This release is that one thing. The rest of what was agreed (the five fires as the nights of the descent, the dark
kept at twelve blocks all the way down, the reader's own stories' people on the stair) follows in later releases.

Protocol 193: a new message tells readers of a fall, and a 0.4.593 client would not draw it. One new client mixin
moves the view when a reader leans. No line, layout or Witness change. Nothing is built or rebuilt.

## What the novel has

Read in the owner's copy, in the chapters on the explorations:

- **The staircase is never lit.** The only light is what the explorers carry, and it does not reach.
- **Its depth is known by a light let fall.** Flares dropped into it are never seen or heard to land: "the darkness
  just swallowed the flares right up". A lightstick dropped from the top "flickers and dies".
- **The book says the depth cannot be shown.** Depth needs light, and the flares "barely illuminate the size of that
  bore". "Only knowledge illuminates that bottomless place."

## What was withdrawn

The first thing I built for this note drew the well's 108 wall lamps and its burning hearths through the dark at any
distance, and threw burning scraps off a fed hearth. It was never committed. It lit exactly what the novel says cannot
be lit, and the owner said so before I had finished it.

## What it is now

**A reader may let a light fall into the well.**

- **How.** Anything that burns (a torch, a lantern: any thing whose block gives light) goes over a rail in one of two
  ways.
  - Thrown: standing at a rail and looking out, a reader drops it as they would drop anything, and it goes over.
  - Let go: leaning over a rail (below), a reader drops it and it falls from the hand held out over the drop.
  - Once it is six blocks below the hand that let it go, and still falling, the well has it.
  - A thing that lands on a tread or a landing is still a dropped thing, and lies there as before.
- **What is seen.** It falls as a small point of its own colour: warm for a flame, a soul fire's own, a redstone
  torch's red.
  - It stays in view past the twelve blocks the stair's dark allows, smaller and fainter as it goes.
  - At 260 blocks the dark has it. That is about eight seconds of falling.
  - Whatever really stands in front of it hides it: a flight, a landing, another reader.
  - It lights nothing.
- **Everyone in the well sees it,** at any depth. A light let fall near the top goes past readers further down.
- **Its landing.** The fall is followed against the blocks that are really there.
  - Where it comes down within forty-eight blocks of a reader, they hear it: the thing's own sound on stone. Its
    light burns there for eight seconds and gutters out.
  - Anywhere else, nothing is heard.
- **Nothing let fall is lost.** While it falls the thing itself is out of the world. When its fall is over it lies
  where it came down, as it would have had it fallen all that way as a dropped thing, and can be picked up there. A
  dropped stack falls as one light and lies there whole.

**A reader leans over a rail to watch it.** This is mine, and it is the second thing under For the owner below.

- **Why.** A reader on the stair stands behind a rail on a stone string, a block back from the drop. From there the
  stair they stand on hides everything more than about ten blocks straight below. A light let fall could be thrown,
  and then not watched.
- **How.** Standing still at a rail in the well and looking down (thirty degrees or more), the reader's view eases out
  over the rail: past its far face, and a quarter of a block lower, as a head goes over a balustrade. It takes about
  half a second. Looking up, or stepping away, it eases back.
- **What moves.** Only the view, and only in first person. The reader stands where they stood: they cannot fall, and
  other readers see them standing at the rail.
- **Where.** At any rail in the well with a drop beyond it: the flights' rails on both sides, a landing's short rails
  beside the walls, the rails at the head of the stair.
  - Not at a landing's inner corner. The one rail there turns the corner between two newels two blocks high, and a
    light thrown from there comes back off them.

**"Near the bottom" is the well's own bottom, not the cell.** This differs from the sentence the owner agreed to, and
it is the first thing under For the owner below.

- The well runs as far again below the cell as above it, to a stone cap. A light let fall from the stair above the
  cell goes past the cell's level and on down.
- So on the way down to the cell it is never heard to land, from any height, unless it strikes something on its way:
  the bridge at the cell's level, a flight, a tie.
- It is heard to land by a reader who has gone down the lower stair to within forty-eight blocks of the cap.

**What does not change:** how far a reader sees, the fires, the leaves, the wall lamps, and anything dropped that is
not a light. A thing that is not a light, let go at a rail, lands at the reader's feet as it always did.

How it is done, for whoever changes it next:

- `labyrinth.StaircaseFallingLight` watches what a reader in the well lets go of, for ten seconds, whatever it
  touches. When the well has it, it follows the whole fall once, against the blocks that are there, and tells every
  reader in the well (`network.StairLightFallPayload`): where it began, how fast, how many ticks it lasts and where it
  ends. When the fall is over it sets the thing down there.
- `client.StaircaseFallingLights` steps the same motion and draws it over the finished frame.
- A light that would drift into a wall on its way is followed again with no drift, so the path a client draws is the
  path the server followed.
- `labyrinth.StaircaseLean` is the one rule for leaning, called by both sides: the client eases its view to the offset
  it answers (`client.StaircaseLeanView`, `client.mixin.StairLeanCameraMixin`), and the server puts a light let go
  while leaning at the same place.

## Checks

- **The falling light's case** (`FireAndCompanionTests`, a light let fall):
  - a torch and a soul lantern are lights; a stick, an unlit candle and a striker are not;
  - a torch three blocks below the hand, or lying on a tread, or outside the well's walls, is not the well's; seven
    blocks below and still falling, it is;
  - the torch is then out of the world, and when its fall is over it lies on the stone set twenty-three blocks below;
  - a client stepping the same fall is over that stone one tick before;
  - with nothing below, it goes on down for more than a thousand blocks;
  - a light that would strike a wall falls straight down beside it;
  - the watch itself, with real things falling and nothing called by hand: three lanterns let go over the drop are
    taken, and lie at the bottom of it, all three; two torches that come down on a tread two blocks below are still
    the dropped thing they were.
- **The lean's case** (`FireAndCompanionTests`, a reader leans over a rail), on a tread with a rail on a string and
  the drop beyond, as a flight's edge is built:
  - at the rail, looking down, the eye goes out past the rail's far face and a little lower, and stays above the
    rail's top;
  - no lean looking out level, a step back from the rail, facing away from it, outside the well, with an eye too low
    to clear the rail, or with floor beyond the rail;
  - a light let go while leaning starts beyond the string's face and falls; a stick does not move; a light let go
    looking out level is thrown as anything is.
- **The build's own client proof** (`OutsideSkyProof`, `native-falling-light.png`): one light at eleven moments of
  its fall, with nothing in front of it. Measured in the picture's pixels:
  - in view at the first eight moments, and at none of the last three, which are past its reach;
  - a warm point where each of the eight should be;
  - strongest at the nearest (0.96 of full) and faintest at the furthest (0.06).
- **A look in a real level, with two clients,** run on this tree with a local probe that is not committed. The top
  340 blocks of the staircase were built; one reader stood at the inner rail of the first flight, the other at the
  inner rail of the second, four blocks lower. Nothing was called by hand: the lights were dropped as the drop key
  drops them.
  - Leaning: the first reader's view went a block and a tenth out over the rail and a quarter down; the second
    reader's did the same when they looked down.
  - Let go while leaning (a torch, then a soul torch): each left the hand beyond the rail and was taken by the well
    6.2 blocks below it.
  - Thrown, looking out level (a torch): it went over the rail and was taken 6.2 blocks below the hand and 5.2 blocks
    out.
  - In the first reader's frames each light is a point just below the middle of the view that shrinks and dims for
    about six seconds and is then gone; the soul torch's is its own colour. In the second reader's frames the same
    lights come down past them and go on below.
- **The staircase's other cases** (77), **a compile, the quick checks,** and the whole client proof chain (64
  proofs), which loads the client with the new mixin.

Not checked:

- **Nobody has played it.**
- **The workflow does not run the two-client look.** There, the drawing is measured alone, with nothing in front of
  it, and the lean is checked as a rule on the server. That a real stair hides the light and a real lean shows it was
  seen only in the local look.
- **The lean was looked at on one flight.** Not at a landing's rail beside a wall, the head of the stair, or the lower
  stair.
- **The landing was not heard.** In the look the lights fell out of the built part. The case checks that the thing
  lies where it lands, not that anyone heard it.
- **A light that comes down on the bridge at the cell's level** was not tried.

## Caught while checking

- **The floor of the world is a landing.** With nothing built under it, my case expected a light to be lost. It is
  followed to the floor of the House's interior, more than three thousand blocks down, and comes to rest there, far
  out of anyone's hearing. In a built staircase the well's own cap stops it first.
- **From a landing's inner corner a light comes back.** The first look threw three torches from there and all three
  lay at the reader's feet: two newels two blocks high flank the one rail. Left as 0.4.593 built it; these notes say
  where a light does go over.
- **The stair hides the fall.** Working out what the look should show, a reader behind a rail sees about ten blocks
  straight down. A light thrown over it could not have been watched. The lean is the answer, and nothing else in this
  release is as large.
- **A throw that clipped a rail was forgotten.** The watch dropped anything that touched down, and a level throw can
  touch the rail's top on its way over. It now watches for ten seconds whatever the thing touches.
- **A light that came down a few blocks below was gone for good.** The well took the thing and gave nothing back, so
  a torch that landed on the next flight burned eight seconds and vanished. Now the thing lies where it comes down,
  and a server that stops sets down whatever is still falling.
- **The look's readers were put back at the first landing.** The two-client harness registers the House once a phase
  begins, so the stair's own rule ran: with no fire lit, nobody goes past the first landing. I had taken the rule not
  to run there. Both readers now stand inside that stretch, and the real handler answered the drops.
- **Two faults in my own cases.** The lean's case cleared the well's lists while the case beside it in the batch had
  lights on their way. And one torch sat in a chunk that was not ticking, and never fell: the case now holds its
  chunks and waits for them to tick.

## For the owner

- **"Near the bottom" is not the cell.** You agreed to "no sound of landing until they are near the bottom". As
  built, the bottom is the well's, 1,280 blocks below the cell, so a reader going down to the cell never hears a
  light land unless it strikes the bridge there.
  - To make it true at the cell, the well would need a floor at the cell's level, with the lower stair going on down
    through an opening in it. A light let fall from the last turns would then be heard to come down on that floor.
  - That is a change to the staircase's architecture, and it would hide the lower well from the cell's level. Say if
    you want it.
- **The lean is mine.** You agreed to a light the reader "watches until the dark takes it". From behind a rail they
  could not have watched it, so a reader who stands at a rail and looks down now leans over it.
  - It moves the view by itself: about a block out and a quarter down, over half a second. It is not a flash or a
    shake, so it does not follow the Comfort setting. Say if it should.
  - Other readers do not see it: the leaning reader's body stands as it stood.
  - Without it, a light can be watched for its first ten blocks, or by a reader across the well.
- **Nothing tells a reader they can do either.** Dropping a torch down a hole to see how deep it is, is an old habit
  in this game, and looking down at a rail is what a reader does there anyway. That is all this relies on. A line in
  the book at the head of the stair could say it; lines are yours.
- **At a landing's inner corner nothing goes over,** and there is no leaning there. It is the place a reader is most
  likely to stop and look into the well. The two newels that flank the corner rail could come down to the rail's
  height; that is a change to what 0.4.593 built, so I have left it.
- **The wall lamps are still there.** You did not say whether they stay or go. They are not in the novel. They show
  only from close by.
- **Choices that are mine:**
  - what counts as a light (any thing whose block gives at least seven of light: torches, lanterns, glowstone, a
    redstone torch; not an unlit candle);
  - six blocks below the hand before the well has it;
  - 260 blocks before the dark has it;
  - forty-eight blocks for its landing to be heard, and eight seconds of light where it lands;
  - the three tints;
  - thirty degrees of looking down before a reader leans, and how far out the view goes.
