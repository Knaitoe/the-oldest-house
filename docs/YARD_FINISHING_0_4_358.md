# The yard's small things (0.4.358)

The last of the owner's "The House Has a Yard" (the list under "What remains" in `docs/YARD_STORIES_PROPOSAL.md`). After this release the yard draft is built through. Five small items:

- the night sky;
- a figure under a streetlight;
- a deeper Survey Stakes;
- the clothesline's quiet return;
- hedges in the wind.

None of them is a story or gives Witness credit. The Witness pool is unchanged: 80 sources, 60 required, two kinds, three endings. Layout 113 (one new place). Protocol 152 (a new yard fitting kind and two sounds).

## The night sky

From the draft: *"the night variation of the sky, a fixed sky with a slightly displaced star pattern."*

Outside, the House draws its own sky. Over any of the yard's places at night, it now draws the stars from home instead of its own scattered ones. These are the reader's Overworld stars, laid out by the same rule the Overworld uses, as they stand at midnight. They do not wheel with the hours. The whole field is turned about two and a half degrees off true, and one star in nine has drifted another degree or so on its own. Someone who has looked up at home will recognise it, and will notice it is wrong.

This is client side only (`YardSky`, `HouseOutsideEffects`). The native client check renders the yard's night looking up and saves `native-yard-sky.png`. It checks that all of home's stars are there (vanilla keeps about 780 of its 1,500 draws, so at least seven hundred), that none is further out of place than the turn and drift together, and that most have moved at least a degree.

## A figure under a streetlight

From the draft: *"on a long straight, a silhouette under a distant streetlight that keeps its distance as the reader comes on."*

On the long drive, the straight past the dead tree ends at the streetlight. A reader coming along it toward the light sees someone standing under the lamp, facing them, from about forty blocks off. As the reader walks on, the figure gives ground at the same pace, out of the light and round the far bend into the dark, never nearer than twenty-eight blocks along the road. If the reader stops, it stops. Once it is round the bend and out of sight, it is gone.

- **Who sees it.** Each reader sees their own, once. Nobody else sees it, another reader standing beside them included.
- **When it goes.** If the reader turns back or leaves the drive, it goes when they are not looking. If they stand still for a long while with their back to it, it goes too.
- **What is kept.** The reader's record keeps that they saw it and that it went.

## The deeper Survey Stakes

From the draft: *"the Survey Stakes' deeper variant, lined up with an opening in a fence that looks continuous until it is approached on that bearing."*

A new yard place, `yard_survey_deep`, the deep room of the survey family (after the Stock Gate). A close-boarded fence, higher than an eye, runs wall to wall across a field. It has no gate.

- **The opening.** In one place the near run stops and starts again two boards on. Two paces behind the gap, a length of the same boarding stands across it.
- **What it looks like.** From anywhere in the field the fence reads as unbroken, the gap included.
- **The bearing.** Only along one bearing, north-west across the field, does the gap show the pasture through it. An old concrete pad and two slotted stakes lie on that bearing: the eye goes through both slots and the gap.
- **Through it.** The way through is a short dogleg: into the gap, west along behind the near run, and out past the end of the boarding behind it. Reaching the pasture settles the survey. The far gate is beyond, under a bulb.
- **The decoy.** Elsewhere a second gap has its own boarding behind it, closed at both ends. It is a pocket, and nothing shows through it from any side.

## The clothesline's quiet return

From the draft: *"a reader's later walk through the same lane carries only the weight of wet fabric and the snap of a line, without the footsteps."*

A reader's first walk down the washing line is as before. The sheets gather to their posts, and steps follow behind until the last sheet. Each walk to the end is kept.

On a later walk nothing follows. Each sheet the reader gathers is heavy with water: a damp, dragging weight and a few drips. When it falls back after everyone has cleared it, the line snaps taut. Both sounds are original and subtitled, made by `tools/generate_yard_audio.py`: "Wet linen sags on the line" and "A washing line snaps taut".

## Hedges in the wind

From the draft: *"hedge edges that move a little in the wind while the lane's structure stays legible."*

In the ditch, the washing line and the ditch in flood, the hedges have loose edges. Each is a few sprigs standing off the hedge's face into the lane, at different heights down its length, only where there is hedge behind them (a new yard fitting, `fringe`).

While anyone is in the lane, the wind comes and goes. As it rises, the edges lean one way and back in a wave running down the lane. Between gusts they settle. In the flooded ditch the wind is stronger. The edges have no collision, so the lane is as wide as it was. When nobody is there, they stay where the wind left them.

The sprigs, their three leans and their texture come from `tools/draw_yard_fittings.py`.

## Tests

`YardFinishingTests` (exploration):

1. **`theDeeperStakesLineUpWithAFenceThatOpensOnlyOnTheirBearing`.**
   - Seen straight on, every board of the fence is shut. From the entrance the gap looks like fence.
   - Along the stakes' bearing the eye passes through both slots and the gap into the pasture.
   - The decoy is shut on every side.
   - A walking body goes through the dogleg to the far gate, and the survey settles, with no story credit.
2. **`theFigureUnderTheStreetlightKeepsItsDistanceForOneReader`.**
   - The silhouette stands under the lamp, seen by the reader on the straight and not by one on the apron.
   - It never comes nearer than its distance while the reader walks past the light, and it is gone round the bend.
   - It does not come back on a second walk.
3. **`aLaterWalkDownTheWashingLineHasOnlyTheWetLinenAndTheLine`.**
   - First walk: steps follow and the linen is dry.
   - Second walk: no steps, wet linen, and the line snaps as the sheets fall back.
   - Two walks are kept, the second a quiet one.
4. **`theHedgesLooseEdgesLeanInTheWindAndTheLaneStaysOpen`.**
   - Every edge stands off real hedge, with no collision.
   - The lane is open at either side.
   - The edges lean while someone is in the lane and stay still when the lane is empty.

The existing long drive, washing line, ditch, flood, survey, dealing and architecture cases still pass. The architecture views export the new field.
