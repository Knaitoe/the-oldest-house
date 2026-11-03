# Indian Lake, in Idaho: Movie Night and Costume Night (0.4.278)

The owner: "Consider actual regional landscaping and architecture." The Indian Lake scenes are Proofrock, Idaho: a mountain lake in
the conifer forest of the northern Rockies. Movie Night's lake was a rectangle inside one row of identical trees. Costume Night's was a
straight strip with a spruce on every fifth block. This pass gives both the banks of a real mountain lake. Every place the story uses is
where it was. New builds only.

## The shared banks (`labyrinth/IdahoShore`, new)

Every Indian Lake scene can draw on one set of pieces:
- **Lodgepole pine:** a long, straight trunk and a short, narrow crown near the top.
- **Douglas fir:** dark, broad at the base of its crown and tapering to a point.
- **Subalpine fir:** a narrow spire, needled nearly to the ground.
- **Quaking aspen:** a pale slim trunk and a small, loose crown, likelier near water.
- **Granite:** light grey and flecked, mossed on its shaded faces, in boulders of one to three blocks.
- **Forest floor:** needles and duff, thin grass where the light gets in, moss in the damp.
- **Shore:** gravel, some sand.

`tree()` picks one for a spot and keeps its top under the scene's ceiling.

## Movie Night (`MovieNight`)

- **The banks rise.** The beach and the gravel shore round the water stay level, and so does the corridor from the far pier to the
  ticket hut. Beyond them the ground climbs steeply, as a mountain lake's does, to six or seven blocks at the scene's edge. It is
  needles and duff on the slopes and granite where they are too steep to hold soil (`bank`).
- **The shore is a lake's, not a ruler's.**
  - The four corners are rounded into gravel shore.
  - Granite boulders line the sides and the far end, some under the water and some breaking it.
  - A small cove is cut back into each side bank.
  - A lodgepole lies down off the west bank, half in the water.
- **The woods** are lodgepole, Douglas fir and subalpine fir up the slopes and aspen down by the water. Granite boulders stand among
  them, with grass and fern under them, moss in the damp and a little sage on the dry slopes nearest the beach.
- **Untouched:**
  - the beach, the cast's places and the speakers;
  - the dock, the projector and the canoe slots;
  - the screen's raft, the buoy and the barge;
  - the far pier, the boardwalk and the ticket hut;
  - the open water the canoes, the film's beam and the witch use.

## Costume Night (`CostumeNight`)

- **The banks rise** on the lake's east side, along the west dunes and at the far end, climbing steeply into the same Idaho forest
  (`bank`).
- **The lake's far shore.** Its two east corners are rounded into gravel, and granite boulders line the east bank.
- **Untouched:**
  - the lake's west edge, where the witch waits at her four places just off the sand;
  - the sand, the stage, the bonfire and the park's grass with its edge at z -49;
  - Hank's table and every place the story uses.

## Rules kept

No Witness, layout, protocol or story change: 74 / 56 / two kinds / three endings. The scene clock, the witch's footing (water and the
grey boards only) and every story position are as they were.

## Tests

The whole `the_oldest_house_literary` namespace (190 cases) passes locally, including every Movie Night and Costume Night case. So does
the architecture pass. Both scenes' views were rendered and checked.

## Next

The other Indian Lake scenes get the same banks in their own releases: Winter Lake, the Shallows and Phone in the Canoe (both built by
`LakeLandscape`), and Drowned Town (Proofrock).
