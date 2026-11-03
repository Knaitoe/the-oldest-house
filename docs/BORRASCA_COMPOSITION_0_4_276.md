# Borrasca, recomposed (0.4.276)

The owner, on the Borrasca architecture views: "The renders are plain and look messily composed." Then: "Yes, sounds great" to a
composition pass on the five sites.

This is composition only. Every story position, walk and sightline stays where it was:
- the station, the store, the Sheriff's office and the lot;
- the child's walk up the middle of the town;
- the Triple Tree's burl, its forks at x -4 and x 3, the ladder and hatch;
- the clearing's taking, drop and hiding places;
- the lane up to the wire;
- the depot's aisle from its front door to the service exit;
- the line from the clearing to the ridge lamp;
- the ridge road and the truck bay;
- the cast's standing places in town and in the yard.

New builds only.

## What changed

**The town** (`BorrascaDressing.town`). The town had three buildings at its front and fifty blocks of empty lawn behind them. Now:
- **The street.** The trail leaves the square as the town's one street: gravel with grass shoulders, narrowing to the trail where it
  gives out.
- **Four houses.** Each stands on its own lot, with its door on the street:
  - stone footing, clapboard (birch, spruce, pale oak or brick) and a gable roof along the street;
  - a porch with a light, windows on every side, and a chimney at the back;
  - inside, a room with a lamp, a table and chairs, an armchair, drawers and a bed;
  - a picket fence and gate, the family's name on the gatepost, and a tree in the yard;
  - each yard has its own thing: a swing, a washing line, flower beds or a kennel.
- **The chapel.** White boards and a steep roof, with a steeple on the street. The steeple holds a bell, and the door at its foot
  leads into pews and a table at the far end.
- **The trailhead.** Where the street ends: a gravel turnout with log stops, a bin and a bench, and two posts with the trail's sign
  across them, high over the path.
- **Poles and trees.** Poles down the street's east side each carry a lamp over the street. Oaks, birches and pines stand along the
  valley's sides behind the lots, the store and the office.

**The trail and the paths** (`BorrascaRooms.walked`, `riser`). The red painted strip is now a walked path:
- vanilla dirt path down the middle, its edges giving way to podzol;
- a spruce stair as the riser at each rise, in place of the grey stone stairs;
- the lane up to the wire and the shelter branch are the same.

**The valley floor** (`BorrascaLand`).
- **Calmer ground.** It lies in broad patches (needles, moss in the hollows, bare earth on the rises) instead of a speckle, with no
  rooted-dirt dots.
- **Oaks.** The lower slopes are mixed, with an oak among the pines.
- **The gully.** It is one stream cut, surfaced by the terrain itself instead of a stamped strip:
  - a bed of gravel and stones;
  - steeper banks of earth and roots;
  - cobblestone risers where the bed climbs;
  - ferns along the tops of the banks;
  - one trunk fallen across it, high enough to walk under.
- **The way down the far side.** The terrain surfaces it the same way, with timber risers.
- **Rocks.** The rocks beside the trail no longer come in a pair at every eleventh block. They stand one side then the other, at
  different sizes and distances.

**The clearing.**
- **The ground.** The smear of red terracotta is gone. The needles are worn through to earth where people stood: coarse dirt and
  packed mud at the middle, rooted earth around it, needles at the rim.
- **Around it.** There are dead stems and two snapped saplings at its edges, and ferns at its rim. Young spruce close round its
  west side, away from the ridge; the line up to the lamp stays open.

**The Triple Tree** (`BorrascaDressing.tree`).
- Each trunk is thickened to two by two from where it leaves the burl.
- Roots flare out to the sides, beyond the forks. Behind the burl is the trail again, so nothing grows there.
- Limbs reach out over the roof, and a crown of five broken clumps sits over the fort.

**The ridge depot's yard** (`BorrascaDressing.yard`).
- **Bays.** Steel columns in the long walls' own plane break them into bays, with doors and windows kept.
- **The front door.** A copper hood on two brackets, with a lamp, stands over it.
- **The yard floor.** There are ruts from the gate to the shed's bay, and patches where the yard stays wet.
- **New things in the yard:**
  - a tailings heap east of the shed;
  - a water tank on legs by the east fence, with a ladder;
  - pallets by the depot;
  - a stack of pipe.

## The views

`ArchitectureTests` exports a new full-height `borrasca_town` view: the whole town with its roofs and trees. It is not an
`_exterior` view, so the trees are drawn. The `borrasca_treehouse` view is widened and raised to show the roof and the crown.
The other views keep their crops.

## Rules kept

No Witness, layout, protocol or story change: 74 / 56 / two kinds / three endings. The House's writing is unchanged. The town's
signs carry only family names, the chapel's board and the trail's sign.

## Tests

The whole `the_oldest_house_borrasca` namespace passes on the recomposed site, and so does the architecture pass (`ArchitectureTests.nativeEverySceneHasSupportedDetailAndPreservesOriginalsOnUpgrade`). That covers the taking walk, the escape route, the
town buildings, the depot aisle, the fort's forks and roof, the flashlight and cover, the lower rooms and the four visits.
