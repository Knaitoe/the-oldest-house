# Notes from the 14 November playtest, first part (0.4.586)

The owner, playing on 14 November, sent these as they came:

> "grass isn't rendering/behaving correctly in the in-the-tall grass vignette"

> "truant is still sitting in this box, in the steam vents?"

> "very small complaint, on the don't move bull monster? when it huffs and the screen flashes, the top and lower halves
> are at slightly different times/gradients"

New builds only. No line, protocol, layout or Witness change. The notes about Usher's coffin, torches a reader has set
down, and the witch at the costume contest are in the releases after this one.

## The field had no grass

**What the owner saw.** A mown lawn where the tall grass should stand, with a row of posts across it that had tufts of
stalks on their tops, a trampled yellow patch, and the boy calling from somewhere close by.

**What went wrong.** Every place in the House is built in two steps: its builder is run once to write down what goes
where, and the list is then carried out a slice at a time.
- The field's stems were written straight into the world during the first step.
- The list's own first entry clears the place's whole box to air, so it then removed every stem.
- What was left is what had gone through the list: the fence's tufts, the trampled ring round the cap, the cap.
- The same fault emptied the Last Stop's pool table in 0.4.441.
- The story's own cases never saw it, because they build the place in one step.

**What it is now.** The stems go through the list like everything else. The field stands three high from the fence
to its far edge, thins toward the road, and is trampled round the cap.
- `TallGrassSite.column()`, which writes straight into the world, is kept for play, where a track stands up again.

## The Truant stood inside the barrel

**What the owner saw.** In the hall with the steam main, him sunk to the chest in a barrel.

**What went wrong.** His spot there was written as (-6.4, -14.2). The barrel is the cell at (-7, -15), which runs
from -7 to -6 and from -15 to -14, so that spot is inside it. He does not fall, so he stood in it.

**Where he is now.** A step inside the side branch, against its east wall, under the main he knocks on.
- Nobody waiting in the corridor can see him there, which is what his knock is for.
- The pipe is within his reach.
- The barrel is where it was, across the branch from him.

**A second one, found by the new case.** In the lantern yard his spot was inside the lean-to's plank wall, so nothing
of him showed there but whatever stuck out of the boards. He stands under the roof's edge now, beneath the lantern
that hangs there.

## The breath was two halves

**What the owner saw.** When the thing in the dark hall breathes on a reader who is holding still, the screen fogs. The
top half and the bottom half fogged by different amounts, with a line across the middle, and not quite together.

**What went wrong.** The fog was drawn as two flat fills, the lower at half the strength of the upper. Because the
lower was half of a whole number, it came a moment later and went a moment sooner.

**What it is now.** One fill that thins evenly from the top of the screen to half as much at the bottom: it still
comes down from above, where the thing is standing.
- It now follows the comfort setting (the game's Distortion Effects), as every other flash does. At nothing, there is
  no fog; the breath's count and its sound are unchanged.

## Checks

- **A new case for the field,** built the way the game builds it (written down, then carried out in slices): every
  column past the fence stands three high, the first rows are thin and not bare, the cap's ring is trampled, the wagon
  is on the shoulder. It fails on the old code and passes on the new. The four other sliced-building cases pass with it.
- **The field's own two cases** pass.
- **A new case for the Truant** builds all ten of his places and checks that where he stands in each is clear of every
  block from his feet to his head, with a floor under him, and the other spots he walks to in the ledge and lamp
  halls. It also checks that the spot he had is one it would have caught. It failed first on the lantern yard.
- **His seven other service and yard cases** pass with both spots moved.
- **A compile.**

Not checked:

- **Nobody has looked at the field, the Truant or the breath in a played game** since the change, and no frame of them
  was taken here. The breath's fog has no case: it is drawn on the client, where the cases do not run.

## For the owner

- **Other places may have the same fault as the field.** A quick search shows a few more builders that write straight
  into the world while a place is being written down (the Holloway camp, the lamp rooms, parts of the lakes, the model
  home's yard). They have not been gone through one by one, and some may be harmless; say if that should come before
  the next notes.
- **The Truant's knock** now comes from a different side of the branch's mouth than before. If he should lean on the
  barrel, or sit on it, that is a small change.
