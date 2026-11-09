# Borrasca: a longer hill (0.4.420)

The owner, November 9: "I'd also move the triple tree farther up the hill - expand the hill totally, so it's a walk to the tree and a
farther walk to the mill. The walk gives Nell and the player and other potential npcs, if not occupied by players, time to talk and
bond."

This release lengthens the hill only. The talk on the walk is #465 and the shops are #464; both come in later releases.

## How the hill is drawn out

Everything up the hill is still authored where it always was: the fort, the tree and its burl, the markers, the gully, the clearing,
the lane, the depot, the lower rooms and the graves. `BorrascaRooms` maps authored positions to the world. The map has five
parts:

| Authored | World | Lift | What is there |
|---|---|---|---|
| z 0 to -74 | z 0 to -74 | 0 | The town: the station, the store, the Sheriff's office, the houses and the chapel. |
| (one row) | z -74 to -235 | 0 rising to 6 | The street, flat to the town's new north end at z -134. Then the first slope, about a hundred blocks of pine woods. |
| z -75 to -110 | z -235 to -270 | 6 | The Triple Tree, the fort and the first marker. |
| z -110 to -118 | z -270 to -370 | 6 rising to 16 | The second slope, a hundred blocks to the second marker. |
| z -118 on | z -370 on | 16 | Marker Two and the tape, the gully, the clearing, the lane, the wire, the depot and everything under it. |

On a slope the trail swings side to side, up to seven blocks on the first and five on the second, so it reads as a path up a hill
rather than a ruler line.

Builders still write authored coordinates against a base for their own region, so their code is unchanged. Gameplay converts
through `at()` and `rel()`, and a clicked block is converted back to authored before it is matched. Steering on a slope
(`toward()`) aims a few blocks ahead along the step as the hill draws it out, so anyone following a route keeps to the trail.

## The walk

The walk from the station to the Tree is about 240 blocks, three times what it was: half of it up the street, half up the first
slope. From the Tree to Marker Two is about 120 more, and the depot is about 430 blocks from the station. At a walk, the Tree is a
minute away and the depot a little under two.

- **The street runs on.** It continues past the chapel to z -134, where the trailhead board now stands. The stretch between the
  chapel and the trailhead is left clear for the shops (#464).
- **The trail is wide enough to walk beside.** On the slopes the trail's own level runs five and a half blocks either side of its
  line, with a half-step wherever it rises, so walking beside it is as easy as walking on it. Past that, the hillside eases away at
  most nine blocks in ten, with low swells and woods. Trees and logs keep off the band.
- **The ridge is the ridge.** Past Marker Two everything is raised and moved as one, so the gully, the clearing, the lane and the
  depot keep their exact shape. The lane up to the wire has its half-steps again, including the first one off the clearing, and the
  gully keeps its steps where it comes up off the slope onto the ridge (the ridge's ways are matched on its own ground there).
- **The station, the store and the Sheriff's office stand where they were.** The cast's sealed store under the far bank stays at
  the town end too.

## Tests

The Borrasca namespace is updated for the longer hill:

- positions given in authored coordinates go through the hill map (`Fixture.at`, `click`, `rel`, `wbox`);
- distances that are walked rather than authored are placed in the world (`atWorld`);
- the walking cases have longer limits;
- the walking bots keep to the trail on a slope, as a reader would (`onTrail`);
- the flashlight case aims at the Sheriff wherever he actually stands.

Two other fixes:

- The compass handover now measures its reach from the Sheriff's place on the hill. It had used his unmapped position, so the
  choice closed at once.
- Checks on Nell's notes compare plain letters, through the new `HouseWriting.plain`, because a child's hand swaps some letters for
  drawn ones.

The architecture views map their bounds through the hill as well.

## What stays

- **Witness:** 86 / 65 / 63. Borrasca keeps its single Understanding source.
- **Protocol:** 170.
- **Layout:** 135.
- **Content note:** unchanged.
- **New builds only.**
