# Bly's Lady follows the one she held — 0.4.189

This is the success-and-failure audit's fourth build (docs/SUCCESS_AND_FAILURE_AUDIT.md). Bly is one of the three rooms whose story is about something that follows: *The Haunting of Bly Manor* ends with the Lady coming back for Dani. A haunt here is the room's own play, smaller, in the reader's world:
- **In Bly,** the Lady holds the reader, carries them a pace ahead of her in the dark, and puts them in the lake.

## The failure, and what follows

Being held and dragged is Bly's failure, as before. It costs the cupboard wait and puts the reader in the lake room's water, and nothing is resolved. It now also starts the Lady following that reader (`BlyFollows`, `LadyFollows0189`).

**What happens:** once a night, out in their own world, near water in the dark (within six blocks):
- the dark closes in: "Something wet takes your arm";
- they are held for a moment;
- they are walked a few steps they did not take, to the nearest dry bank beside the water, which can be level with it or a block above;
- they are set down at the edge, never in it;
- wet footprints show from the water to where they had stood: "You are standing at the water's edge. Wet footprints lead from the water to where you were."

**Limits:**
- It never wounds them.
- It never happens in the House, or while they are already in the water.
- It is theirs alone.

**How it ends:** when they wait out a later night at Bly in a shut cupboard, which is the room's success. "The drips pass the cupboard and go on to the water. She does not follow you now."

## Tests

- **`VignetteAnimationTests.standingInTheLadysWayCostsTheReaderTheirPlace`** now also checks that the reader she held is followed.
- **New: `LiteraryTests.theLadyFollowsTheOneSheHeldAndSetsThemDownAtTheWatersEdge`.** On a stone floor with water set into it, it checks:
  - the dark closes in;
  - the reader is carried to the bank beside the water;
  - they are set down out of it.

Both pass locally.

## What stays

- **Protocol and layout unchanged:** protocol 101, layout 51.
- **Witness unchanged:** 55 / 42 / two kinds / three endings.
- **No PNG changes.**
