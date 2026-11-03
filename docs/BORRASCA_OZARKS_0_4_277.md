# Borrasca, in the Ozarks (0.4.277)

The owner, on the recomposed Borrasca (0.4.276): "Consider actual regional landscaping and architecture. Drisking Missouri".

Drisking is a Missouri town in the Ozark hills; the trail out of it was already signed as the Ozark Pine Trail. The 0.4.276 pass had
dressed it as northern taiga instead: spruce, podzol, a white New England chapel and picket fences. This pass keeps every position
from 0.4.276 and every story rule, and changes what the land and the town are made of. New builds only.

## The woods (`BorrascaLand`)

- **Trees.** Ozark woods are oak and hickory with shortleaf pine, so the spruce woods are now:
  - **white and black oak and hickory** (`tallOak`): a long, clean trunk and a broad, uneven crown from seven blocks up, with a bough
    or two under it; hickory has the darker bark and leaf;
  - **shortleaf pine** (`pine`): the trail's own tree, with a bare trunk and an open, irregular crown with a rounded top instead of a
    spruce's spire;
  - **sycamores** (`sycamore`): along the gully, the white trunks an Ozark creek is lined with, leaning a little, with white limbs in a
    high crown;
  - **eastern red cedar** (`cedar`): in the open places, a reddish trunk and a narrow, dense dark column.
- **The story's rules still hold.**
  - Every tree but the cedar keeps its crown well above head height, so the slopes past the second marker can still be seen into.
  - Only the tall trees grow near the cliffs, so nothing there can be climbed like a stair.
- **The ground.** The floor is the oak woods' own:
  - grass and sedge where light gets through;
  - last year's leaves lying in broad brown drifts;
  - rooted earth on the upper slopes;
  - moss only in the wettest hollows.
  
  Podzol is gone everywhere: the woods, the clearing, the trail edges and the fort.
- **Rock.** Grey, bedded dolomite with white chert in it and moss on its shaded faces (tuff is gone).
- **The gully.**
  - The bed is chert gravel with ledges of rock.
  - The risers are rock ledges.
  - The cut banks show the Ozarks' red cherty clay under their roots.
  - The trunk fallen across it is a sycamore.
- **The clearing.** Its rim is grass and its middle is worn earth. The trees closing round its west side are red cedars, which take
  any Ozark opening first. The stump and the snapped saplings are oak.

## The town (`BorrascaDressing`, `BorrascaTown`)

- **The houses.** They are what an Ozark street has:
  - **Pruitt:** white clapboard under green tin;
  - **Hollis:** an Ozark rock house, odd brown and tan slabs in their mortar, under galvanised tin;
  - **Webb:** pale boards under shingles;
  - **Corley:** brick under rusted tin, with a brick chimney.
- **House details.**
  - Frame houses have white corner boards and white porch posts.
  - Each porch roof matches its house's roof.
  - Fieldstone chimneys have moss on them.
- **Along the street.**
  - The picket fences are chain-link, with an opening for the path and the family's name on a post beside it.
  - The Pruitts have a redbud in their yard and the Corleys a dogwood, both in bloom.
- **The church.** A white frame Baptist church under a galvanised tin roof with a tin steeple top. Its board reads
  "FIRST BAPTIST CHURCH / Sunday 10 am / All welcome".
- **Poles and trees.**
  - The street's poles are creosoted dark wood.
  - The valley sides behind the lots have big yard oaks, lower oaks and red cedars.
  - The back of the town is a hedgerow on an oak rail fence, with two big oaks behind the store and the office.
- **The store.** The porch is oak under a rusted tin roof.
- **The Triple Tree.** A great white oak, trunks, burl, limbs and crown, under which the forks, the ladder and the fort are where they
  were.
- **The depot's heap.** Grey chat, as the lead belt's tailings are.

Lumber is unchanged: fort planks, floors, interior doors and the depot's fittings.

## Rules kept

Every story position, walk and sightline is unchanged, along with the station, store and office, the cast, the visits and the texts.
No Witness, layout or protocol change: 74 / 56 / two kinds / three endings.

## Tests

- `BorrascaTests`: the fort case now expects an oak trunk through the fort's back wall. The whole `the_oldest_house_borrasca`
  namespace (21 cases) passes locally.
- `ArchitectureTests.nativeEverySceneHasSupportedDetailAndPreservesOriginalsOnUpgrade` passes locally.
- The five Borrasca views (`borrasca_town`, `borrasca_treehouse`, `borrasca_gully`, `borrasca_taking`, `borrasca_refinery`) were
  rendered and checked.
