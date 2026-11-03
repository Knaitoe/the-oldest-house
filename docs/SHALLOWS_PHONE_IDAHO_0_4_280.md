# Indian Lake, in Idaho: the Shallows and Phone in the Canoe (0.4.280)

The owner: "Consider actual regional landscaping and architecture." The Shallows (the summer the boys threw her) and Phone in the
Canoe (the recording on the lake at night) are both Indian Lake, the Idaho mountain lake of Movie Night, Costume Night and Winter
Lake. They were still the old stage boxes: water from wall to wall, a flat strip of dirt, a few trunks with a bar of leaves on top,
and generic woods planted over them afterwards. Now they have the banks of a mountain lake, built with the scene by `LakeLandscape`
and the shared `IdahoShore` pieces. Every place either story uses is where it was. New builds only.

## The banks (`LakeLandscape.idaho`)

- **The beach stays level.** The near bank, where the story is told from, is a level beach: gravel and sand at the water, then
  needles, duff and thin grass. The path to the door is left as it is, and so is the phone's jetty. The black floor of the old stage
  box no longer shows in the row before the doorway.
- **The banks rise.** The sides and the far end climb steeply from the water into the woods, to about six blocks at the edge of the
  scene. Past the edge they climb more steeply still, so no edge shows. Along the beach the side banks stand lower.
- **Needles, granite and gravel.**
  - The slopes are needles and duff over dirt, with granite where they are too steep for soil.
  - The shore at the water is gravel and sand, with reeds standing in it here and there.
  - Granite boulders sit along the shore, and a few break the surface just off it.
  - A pine has fallen from a side bank into the water.
- **The water is narrower than the room, and not a rectangle.** The far corners are rounded and the edge wanders a block or two.
  - The Shallows' open water still holds the girl's whole flight and landing, and the drowned steeple.
  - The phone's lake still holds the steeple, the canoe's lane and nearly all of its water.
- **The woods** are lodgepole pine, Douglas fir and subalpine fir up the slopes, and aspen down by the water, with grass, fern and
  moss under them. Two pines stand on each beach.
- **Kept clear:**
  - The beach's spots, the boys' places and the swing.
  - The Shallows' run north: the band of the far bank she crosses into the woods after she rises has no tree or boulder in it.
  - The phone's jetty, canoe lane and steeple.
  - Both ways on. The Shallows' weathered door stands in a level clearing at the foot of its bank. The phone's root cellar is dug
    into the rise beside the doorway.

## What no longer runs over them

- The generic woods (`SceneCraft`).
- The old forest planting (`LakeSettlement`).

Movie Night, Costume Night and Winter Lake already skip both. The shoreline props of `VignetteArchitecture` (the tools table, the
rope, the crate and the sack) are still set on the beach.

## Rules kept

No Witness, layout, protocol or story change: 74 / 56 / two kinds / three endings.

## Tests

- `ArchitectureTests`: the Shallows bank check now reads the top of the ground as any full block that is not a trunk or a bough
  (needles, moss, grass or granite), rather than podzol. It still requires the bank to rise smoothly.
- The architecture pass passes locally. So do the Phone in the Canoe cases, the Shallows cases and the co-op namespace.
- Both views were rendered and checked.
