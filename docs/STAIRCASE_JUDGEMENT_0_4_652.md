# The staircase, by judgement (0.4.652)

The owner left the great staircase to my judgement ("You can use your judgement for the staircase", 15 November). The
0.4.593 to 0.4.596 notes each ended with questions for the owner. This note answers every one of them: what changes in
this release, what stays as built and why, and what comes in the next two releases. No Witness count, layout or protocol
change.

## What changes

### The wall lamps are gone

- **Before.** A soul lantern stood on a block of stone at the middle of each of the well's four walls, every 96 blocks,
  from the bottom of the lower well to the top: 108 lamps.
- **Now.** None. The walls' middles stay clear of pilasters, as 0.4.593 left them.
- **Why.** The novel's stair is dark all the way down, and the only light on it is what the explorers carry. A lamp every
  96 blocks also told a reader how deep they were for nothing, by counting lamps. Since 0.4.594 the one thing that tells
  the depth is a light let fall that is never heard to land, and the lamps undercut it.
- **What is still lit.** The lamp hanging at the head of the stair in the entry hall, the five camps' fires once a reader
  lights them, the two lamps in the room before the cell, and whatever readers carry. How far a reader sees on the way down (twelve blocks) is
  unchanged.

### A landing's inner corner

- **Before.** The rail turned the inner corner between two newels two blocks high. A reader could not lean there, and a
  light thrown from there came back off the newels.
- **Now.** The two newels at the inner corner stand one block high, as high as the rail. The two outer newels, toward the
  walls, are two blocks high as before.
- **The lean.** A reader pressed into the corner, facing it and looking down, leans out on the diagonal: their view goes
  over the corner's rail and between the two newels, past the rail's far corner and a quarter of a block lower, as it
  does over a flight's rail (`StaircaseLean.corner`). The same rules hold: standing still, looking down thirty degrees or
  more, the eye clearing the rail's top, and the drop beyond the rail. Within about twenty degrees of the corner's
  diagonal counts as facing it.
- **A light let go there** falls from the hand held out over the drop, and drifts the way the reader leaned: straight out
  over a flight's rail, along the diagonal over a corner (`StaircaseFallingLight.held`).
- **Why.** 0.4.594 said the inner corner is where a reader is most likely to stop and look into the well. From there the
  eye looks down the well's middle, where nothing stands between it and the dark.
- **Nothing new about falling.** The rails are iron bars a block high, which a reader could already climb onto anywhere
  along the stair. A newel of the same height adds no new way into the well.

### Tom's one line from the well

- The first time a reader calls Tom on the radio from inside the well, while they are on the stair, he says one line
  (below). He says it once to each reader; after that he has his usual lines.
- **Why.** 0.4.594 said nothing tells a reader that they can let a light fall. It relied on habit. Tom stayed behind at
  the camp, as Navidson's brother stays at the base camp with the radio in the novel, and he tells what is known of the
  well's depth without saying what to do about it.

## What stays as built, and why

From 0.4.594 (a light let fall):

- **"Near the bottom" stays the well's bottom, not the cell.** No floor at the cell's level. A floor there would hide the
  lower well from the cell, and the novel's stair is never heard to end. A light let fall from the stair above the cell
  is heard only if it strikes something on its way; on the lower stair, within forty-eight blocks of the cap.
- **The lean does not follow the Comfort setting.** It moves the view the way looking does, slowly, so it is not a flash
  or a shake. It is also the only way to watch a light fall, and what a scene needs the reader to see does not scale.
- **Nothing in the head of the stair's book tells a reader to let a light fall.** Tom's line is the only hint.
- **The numbers stay:** what counts as a light, six blocks below the hand, 260 blocks before the dark has it,
  forty-eight blocks to hear it land, eight seconds of light where it lands, the three tints, thirty degrees of looking
  down.

From 0.4.595 (the five fires):

- **The first stretch stays the shortest,** and nothing in it asks for the book before the first camp.
- **A leaf binds itself to a reader who walks past it.** A missed leaf would cost a climb.
- **Nothing happens at a camp but the fire:** no sleep, no fade, no line saying a night passed. In the novel a night on
  the stair is the camp; the fire is enough.
- **A camp stays a cold fire and two bedrolls.**
- **The climb home** no longer carries a reader past a camp; 0.4.596 did that.
- **The three lines listed as left alone stay the owner's.** I have not touched them.
- **The numbers stay.**

From 0.4.596 (company on the stair):

- **Shapes are made out from about seventeen blocks,** further than the book's twelve. Nothing unlit can be seen at
  twelve.
- **The way home stays lighter than the way down.** A reader who has turned back can see the column behind them.
- **How plain a shape is, and the other numbers, stay.**
- **The stories with nobody on the stair yet** (nineteen now) get someone standing in for them: that is the third release
  below.

## Next

- **0.4.653, the Minotaur bigger.** About half again his height (0.4.584 made him 3.1 blocks to the eyes), with the cell's
  gate and the passage to it widened and raised to let him through (the owner: "You can adjust the width of the cell gate and
  corridor too"). As built: five blocks wide and seven high, so his horns clear them (docs/MINOTAUR_SIZE_0_4_653.md).
- **0.4.654 and after, someone for every story.** A figure on the stair for each of the nineteen stories that has nobody
  there yet, each standing in for that story's people as 0.4.596's figures do. 0.4.654 gave five of them someone: the
  stories whose people are the cast's own bodies (docs/STAIR_STAND_INS_0_4_654.md). The other fourteen come next.

## Checked

- `ArchitectureTests`: the well built whole, now also checking that no lamp is left on the walls where 0.4.593 hung them,
  and that the two newels at each landing's inner corner stand one block high with nothing above them or the corner's
  rail; the other newels, the rails and the clear nine blocks between newels as before.
- `FireAndCompanionTests.aReaderLeansOverALandingsInnerCorner` (new): a reader pressed into a corner built as a landing's
  is, facing it and looking down, leans out on the diagonal past the rail's far corner and above its top; straight out
  from the corner there is a newel, not the rail; looking out level, or half a step back, there is no lean; with a newel
  two blocks high, or floor beyond the corner, there is none; a light let go while leaning starts beyond the rail's far
  corner, above the rail, and falls along the diagonal.
- `FireAndCompanionTests.tomTellsOfALightLetFallOnceFromTheWell` (new): Tom's line on the first call from the well and
  not on the second; the usual line beyond the well's walls; another reader hears it on their own first call.
- The 84 other cases that touch the staircase.
- Eye-level frames of the well from its plan, before and after: a landing's inner corner from behind it and from where a
  reader stands in it, the view from the lean down the well, a wall's middle, and a flight's lean. The lamp at the wall's
  middle is gone, with nothing left where it stood. Standing in the corner, the two newels are below the eye and the well
  shows over the rail; before, the newels filled the view. From the lean the well's middle drops away under the corner.
- Build, package and branding checks, the quick checks, the architecture and writing cases (6 and 6) and the client proof
  (64 pages).

## New line (for the owner)

- Tom, over the radio, the first time a reader calls from inside the well: "Somebody let a torch go over the rail once.
  Nobody heard it land."
