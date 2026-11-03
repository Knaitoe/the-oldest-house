# Indian Lake, in Idaho: Proofrock (0.4.281)

The owner: "Consider actual regional landscaping and architecture." Proofrock (Drowned Town) is the town on Indian Lake, the Idaho
mountain lake of the other lake scenes. Its town was built as one level on purpose (0.4.67): the witch can follow a reader anywhere
on land, the lawns are her only refuge, and nothing raised is out of her reach. So this pass leaves every street, yard and floor
exactly as level as it was, and changes what grows in it and what rises round it. New builds only.

## The mountains round the lake (`ProofrockTown.mountains`)

- **Slopes outside the town.** Past the town's west, north and east edges, and behind it on either side of the road in, the ground
  climbs steeply into the forest, to about twelve blocks. None of it is inside the town.
- **The first two rows past the edge rise a step at a time.** A reader can still walk onto them before being turned back, so nothing
  there is higher than she can follow. Past them the slope steepens fast.
- **Granite and forest.**
  - The slopes are needles, duff and thin grass over dirt, with granite where they are too steep for soil.
  - Boulders, fern and grass are scattered on them.
  - The forest is lodgepole pine, Douglas fir, subalpine fir and aspen, with aspen along the lake. Every crown stays within the
    ground the builder has ready.
- **Untouched:**
  - the lake's rim, which still holds its water;
  - the church and steeple out in the lake;
  - the arrival and the way up to it.

## The town's woods (`ProofrockTown.tree`)

The spruce of the town's edge woods are now Idaho's trees, at the same places:
- **In town:** lodgepole pine and aspen only. Their crowns start well above a reader's head and reach no further than the old spruce
  did, so nothing changes about who fits where, and no crown can reach into a building.
- **In the eastern woods past the garage and motel:** Douglas fir as well.
- **By the water:** aspen.

## The shore

The far ends of the north beach and the bay's western shore are a mountain lake's gravel and sand. The beach by the furnace, the shore
body, the pier, the picnic tables and the boathouse stay sand.

## Rules kept

- Every street, yard and floor she can cross is one level, as before.
- The lawns, the green and the school courtyard are still the only living grass in town.
- No Witness, layout, protocol or story change: 74 / 56 / two kinds / three endings.

## Tests

- 23 of the 24 cases that build or hunt in Proofrock pass locally:
  - the architecture pass;
  - every other Drowned Town case;
  - the lake ambush cases;
  - the streetlight case;
  - the town witch cases in `StaceyTests`;
  - the church originals case.
- The one that fails locally, `nativeWitchDetoursAroundGrassAndCannotBePushedIntoWater`, builds its own small fixture and does not
  use the town. It fails the same way locally with this release's changes taken out. In CI's full suite it last passed in run 1413.
- The town and its slopes were rendered and checked.
