# Borrasca: the valley, the ridge road and the taking (0.4.262)

The owner's notes on Borrasca, from the playtest document:

- "The architecture/mapping of borrasca needs a lot of improvement."
- "When the construction workers kidnap Nell, they need proper walking animations. It would be cool to sequence that with the ringing
  too, have the shiny gentleman looming above them from behind as they approach the player and their friend. The construction workers
  shouldn't have identical textures, and need an exit path too."
- "Compass needs a 3d model rather than a painted texture."

New builds only; nothing in a saved site is reshaped. No change to the story's visits, sources, Witness entry or ending.

## The valley (BorrascaLand)

Before, the mountain was a staircase. Its ground rose in flat terraces the full width of the map, one block every five, with sheer
risers, a stone wall down each side and one across the back. The trees stood on a six-block grid, and the boulders were cubes.

Now every column has one height, worked out from where it is and nothing else. The site is built from a recorded plan, so the builder
never reads the world back.

- **The trail floor is kept exactly.** The slopes rise gently away from it on both sides, with an undulation of their own, and fade in
  over z -72 to -84 so the town's edge has no step.
- **The valley walls are cliffs.** Past |x| 35, and past z -216 at the back, the ground climbs seven blocks or more above the slope's
  foot. A reader cannot climb them, and they replace the old stone walls.
- **Every authored place keeps its level.** The clearing (8), the plateau in front of the wire (12), the fort (0), both markers and the
  turning bay (12) are flats. The ground eases into each of them, with no lip.
- **Routes are cut into the slopes with banks of their own.** These are the gully, the way down the far side, and the new ridge road.
- **The yard stays sealed.** Ground beside the wire stays low, so the fence top is never one step from the hillside. No tree, rock or
  log stands within six blocks of it, and near the cliffs only tall pines grow: young spruce can be climbed, and pine crowns start
  seven blocks up.
- **The clearing** has a worn clay floor, a crag at its west side, a stump with roots, and mossy stones. The lane from the clearing up
  to the road has proper stairs.
- **The ridge road** (gravel ruts, a grass crown, coarse verges) runs from the head of the lane along the front of the wire, round the
  yard's corner, up the cutting outside the east fence and round the shoulder to a turning bay. The men's truck stands in the bay, out
  of sight. A post at the yard's corner reads PRESCOTT REFINING / Service road / No admittance.
- **The woods.** Seventy percent tall pines and the rest young spruce, on a jittered grid. There are a few fallen trunks on level
  ground, and ferns, dead bushes, mushrooms and grass underneath. Rocks are noisy clusters of stone, andesite, cobble, mossy cobble and
  tuff with moss on top, by the trail and scattered through the trees.

## The taking (BorrascaStory)

The men come for Nell down the ridge road, and the three strokes are paced to them.

1. **The light and the first stroke.** When Nell sees them, the two men appear together somewhere along the cutting outside the east
   wire, wherever nobody can see them come from. They try the nearest points first and fall back on the turning bay. The first stroke
   rings from the refinery.
2. **The second stroke.** It rings as the lead man turns down the lane toward her, or by 800 ticks.
3. **The third stroke.** It rings when they reach her and put a hand on her arm, or by 1200 ticks at the latest.

They walk at a steady pace. When they are within reach, each walks at her side and holds out a hand.

**The Shiny Gentleman stands on the ridge behind them while they come.** On each of the first two strokes he is placed on high ground
behind the men's midpoint. The spot is five to eighteen blocks back and six to twenty-two blocks above them, clear overhead, with his
head in the reader's sight, at least twelve blocks from the reader's eye. He is moved only while the reader is not looking at him.
After the third stroke he fades.

**The exit.** After the third stroke they lead her out the way they came: up the lane and back along the ridge road, a man on each side
with his hand on her upper arm (gestures 8 and 9). They go no faster than she can be made to go. Sixty ticks after they start, she
is missing as before: her compass lies where it always fell, and tracks run up the lane. Whoever is still in view walks on along the
road and up the cutting toward the truck, and is gone only once nobody is looking.

Nell herself is never moved while watched. The men may be put forward only if they stick for three seconds where nobody can see them.

## Two men, two faces, and walking

- **A second worker skin.** `borrasca_worker_two` (tools/paint_character_skins.py) is an older, heavier man with a short beard, a
  canvas chore coat with buttons, two pockets and a reflective band on the back, and a white hard hat. The first worker keeps his face.
  Each man keeps his own phase (WORKER, WORKER + 1). The visual proof shows six people front, side, back and dim.
- **The gestures arrive.** An actor's gesture was held to 0..5, so Nell's led pose and the reaching hand (6 and 7, since 0.4.235) and the
  men's hands on her arm (8 and 9) all reached the client as 5, a stoop that also stops the walking legs. LiteraryActor now carries up to 15;
  no other actor uses a gesture above 5.
- **Walking.** BorrascaNativeArt lays the stride over the pose after the pose is blended, not before. The blend was smoothing the gait
  away. Legs swing with the distance walked, free arms counter-swing, and the body bobs. A man with his hand on Nell's arm keeps that
  arm on her (head turned to her) while his legs walk.

## Nell's compass

The compass is a model now, not a painted icon: an octagonal brass case, a dial, a bezel, a bow, a strip of red tape, and a red-and-steel
needle on a pivot (tools/generate_borrasca_trail_assets.py, `item/borrasca_compass_parts`). The needle has sixteen positions
(`borrasca_compass_1..15` plus the base). In the hand it points north whichever way its holder turns, with a slight wobble that never
settles. On a shelf, on the ground or in a frame it rests pointing up.

## Sound

`borrasca.stroke`: one stroke of the refinery's triad, left to ring longer (tools/compose_borrasca_trail.py), heard at a fixed range
like the triple chime. Subtitle: "One stroke from the ridge".

## Tests

- BorrascaTests.theMenComeDownTheRidgeRoadToThreeStrokesAndLeadHerOut (batch borrasca_strokes) checks that:
  - each man has his own face;
  - the first stroke rings with the men more than twelve blocks from her, and the second before she is led;
  - she is led after three strokes, then goes missing;
  - the men walk her up the lane or are gone from sight;
  - nobody moves a block or more in one tick while watched;
  - the Gentleman stands behind the men.
- The sealed-refinery case (a child on the first visit cannot get at the machine) runs its flood fill over the new land.
- The four-visit, taking-walk and ahead-of-the-taking cases have longer timeouts for the longer approach.
- The Borrasca namespace passes locally: 20 of 20. CI verification is pending.

## Run 1409 (0.4.261)

Two jobs failed; both are fixed here.

- **house-read-rooms.** CoopTests' Whale-and-canoe copies case compared the two lakes' canoes in the same tick the copy was entered.
  LakeOriginals answers null until a canoe's entity section is loaded, so it now waits for both canoes before comparing them.
- **scene-review-clients.** Phase 65 stuck: the client carrying the dresser stopped in the flat's doorway. The open door's leaf stands
  along the west side of its cell (x 0 to 3/16). At 0.4.260's slower carrying pace the client finished its first leg at x 0.466
  instead of nearer the middle, and its body caught the leaf by two hundredths of a block. Every walk through that door, up or down,
  carrying or not, now steers to x 0.62 first.
