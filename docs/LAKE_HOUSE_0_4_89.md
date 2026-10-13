# A House at the Bottom of a Lake — 0.4.89 candidate

Claude's scene and diving foundation are integrated into `feat/playtest-notes-0.4.88`, preserving the latest Ghost Set, fan, nightclub, waking, mirror and scenery changes. The continuation supplies the house interior, private Tess, home haunting, final choices and their original artifacts. Dialogue and accounts are original adaptations.

## The visits

The rental log remains in the boathouse. A lent canoe crosses the clear lake and the covered culvert to the deep third lake, where the house and chimney raft already stand. A foyer dive and surface return earn the first visit; the back rooms earn the second. Only lent gear supplies underwater breath: the second visit's hose helmet requires the actual compressor partner, and later tanks carry a minute of air and refill at the compressor. Exhaustion brings a harmless blackout and a raft wake, preserving progress and originals.

The custom submerged furniture includes coat hooks, a key bowl, supported lamps, tables, deliberately dry chairs, loose ash, shelves, a pool table, sofa, changing painting, kitchen islands, porcelain beaver, fitted red stairs, wardrobes, beds and a cedar sauna. The central staircase reaches the dressing room and bedrooms; a hidden wardrobe reveals the spiral descent to the deeper pool. Floating dresses belong to their reader. The black dress descends to the warmer pool. Items sink, bubbles hang, and fish remain outside the house. Candles and the hearth burn underwater.

Visiting the dressing room, bedrooms and pool and returning to the surface earn the third beat. The next real morning wake at home brings private ceiling drips, wet footsteps and visible fading floor marks without changing the home. A later sauna visit lights the rooms and reveals the reader's own featureless wax figure upstairs.

## Tess and the painted cast

Tess appears only for a solo living reader; spectators do not count. A second reader starts her departure. If her owner keeps watching, she physically swims away and is removed only after leaving that camera's view. Another player leaving or an owner reconnecting cannot recreate her mid-visit. A genuine later arrival can bring her back. She rows in the canoe, swims through the house, points underwater and minds the second visit's compressor. Saved native bodies are retained while entity sections are loading.

The adaptation's Tess is 18. Her fitted painted atlas has pale skin, dark blue eyes, light freckles, long straight black hair, an opaque navy swim top and shorts-cut bottoms with a visible midriff. The long hair uses dedicated head-attached meshes, including rowing and swimming poses. The wax figure has a separate painted ivory-wax and black-dress atlas. Furniture regeneration preserves both finished cast atlases. Nine spring joints in the loose hair drift in outdoor water and become rigid immediately inside the house, including the basement. They step at 20 Hz on the client, interpolate while rendering and never run on the server. Impulse limits and the immediate indoor stop pass the finite solver check; a local warm benchmark measured 0.285 microseconds per nine-joint step (rendering excluded).

## Choices and originals

Trying to mine a house object lifts and turns a private display of the original without breaking or dropping the shared block. The first attempt warns; a later attempt saves the broken rule, begins upstairs footsteps and records it in the account. All endings remain available.

| Actual choice | Later house | Original keepsake |
| --- | --- | --- |
| Touch the lit wax figure | That reader returns to the shared empty-lake copy; later doors keep this route | Owned flashlight: steady outside beam and occasional peripheral movement |
| Leave through the half door | The original house remains lit for that reader; peers retain their own state | Owned house light: placed native custody, nearby night light and outside death return |
| Remain in the assigned dry chair | Wake on the raft with a private dawn; house dark and place still set | Owned gold helmet: outside breath, narrowed view, muffled sound and rare overhead creaks, without armour |

Each keepsake is issued once and bound to its original reader. The house light preserves every item component in its native block entity and returns the same original once when removed. Earned effects operate outside House dimensions and cannot bypass lent-gear breathing in the vignette. No world clock is changed by the private dawn. Tess's goodbye occurs once on a later actual solo visit.

Reading the rental log, reaching a personal ending and reading its account resolve one new Witness source. The existing two resolution kinds and three final House endings are unchanged.

## Integration and validation

- Mod 0.4.89; network protocol 68; layout 44.
- Existing nightclub slot 95 preserved; lake house appended at 96; internal empty lake at 97. The empty copy is never independently dealt.
- Forty-five Witness sources; thirty-four required resolutions. New builds use the existing bounded append queue and loaded-section guards.
- 595 declared native cases, including six foundation and six completion cases. Completion coverage exercises the actual visits and figure/door/chair choices, private Tess departure, mining packets and saved state, outside gear exclusion and exact named light custody.
- 760 PNGs; every pre-existing PNG remains byte-identical. Twenty-two fitting kinds, 352 state combinations and 44 fitting models.
- Ninety-four architecture views, plus three new native lake-house GPU views for cast, fittings and gear; all earlier proofs remain required.

Validation remains candidate status until the focused native cases and all twelve exact-source CI jobs, including native rendering and connected-client proofs, succeed. See the repository workflow for the retained checks.

Checkpoint validation: Java compilation, 760 PNG decodes, eight writing-font checks, previous PNG byte identity, and the bounded nine-joint hair solver pass. Eleven lake-house cases and twenty-one earlier-scene cases pass. The outside artifact case currently times out at its native section-readiness gate; its real death/respawn assertions are retained for the next repair. Local native GPU rendering cannot start the virtual display, so the added cast/hair proof remains required in CI. This is a development checkpoint, not a fully verified release.
