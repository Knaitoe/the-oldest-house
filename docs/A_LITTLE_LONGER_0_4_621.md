# A Little Longer 1: the place (0.4.621)

The first slice of `docs/A_LITTLE_LONGER_PLAN.md`, from the owner's proposal (`docs/A_LITTLE_LONGER_PROPOSAL.md`). The place alone:
Rowan's radio repair shop, the flat above it, the roof landing and a short stretch of the House's own corridor, built whole at night
in the rain. Nobody is in it yet, and the House does not deal it until its evening can be played.

## The place

- **The House's corridor.** The House's door opens on three blocks' width and six blocks' length of the House's own paper and runner,
  with a second door (the way on) in its east wall. It ends at the shop's back door.
- **The shop.** Plaster walls, worn boards. The workbench against the west wall, in three parts, with the parts trays, the radio with
  its back off and the mended lamp on it, the work order on its spike above, and the chair whose front leg has been mended twice. Parts
  shelves of sets waiting their turn on the east wall; the counter across the front, its varnish worn pale where elbows go; the
  shopfront's rain windows either side of a door that does not open. Through the glass: a pavement, a curb, a wet road, lamp posts and
  the facade across the street, lit here and there.
- **The flat.** A stair up the back wall to the flat: the coat hook with Rowan's coat at the stairhead, the worktop along the east
  wall with the kettle, the toaster, the kitchen radio and the sink; the table under the front window with the two cups on it, the
  comfortable chair on one side and the kitchen chair on the other; the bathroom in the west back corner behind a door.
- **The roof.** A stair up the west wall through the ceiling into a small stair house, whose door opens onto the felt. A parapet two
  courses and a coping high all round (too high to step over: the proposal asks that no fall separate the pair); a water tank; a
  chimney stack; two benches under a corrugated awning on posts in the north-east corner. Round the yard the buildings rise and lean in
  from the eighteenth row, so the sky over the roof is one narrow patch: "a tiny patch of sky between the wrong buildings".
- **Rain.** On the roof and in the street, for whoever is in the place (client only, as the state route's rain is); on every window
  an animated run of drops. No game music: the only music here will be the radio's.

Every door opens; the street door stays shut and says so. Every fitting answers a hand with one line; the lamp switches on and off,
the kitchen radio warms up into a hiss and stops, the toaster's lever goes down and up. The chairs, the armchair and the roof benches
seat a reader.

## Made as

`LabyrinthPlace.LITTLE_LONGER` (seven visits, slot 241, outside), `LabyrinthBuilder.VERSION` 139; `LongerSite` (the plan as
constants, the build, where a body is); `LongerRegistry` (seventeen materials); `LongerBlock` (twenty-two kinds of fitting);
`Longer` (a hand on a door or a fitting; never dealt); `LongerTexts`; `LongerClient` (rain, no music). `tools/draw_longer_art.py`
draws 56 textures (the rain an eight-frame strip) and every model and state at sixteen pixels, with `--check`, and fails if the game's
lists of materials and kinds are not its own. The House's tables: the literary places, the miniature, the staircase name ("the radio
shop"), a shared-account line, the way on, the door bleed, the scene clock (night).

## Checked

- New namespace and job (`the_oldest_house_longer`, `longer-gameplay`): `LongerTests.theRadioShopIsBuiltOfItsOwnThingsAndEveryDoorAndFittingAnswers`.
- The architecture namespace: the work order is the place's first-paper surface, and it lays its own boards and felt instead of a rug.
- The literary registry case: never dealt and no source.

No Witness change: the place is no source until its evening can be finished. New builds only.

## New lines for the owner

Every line in `LongerTexts` is new: the work order (three pages, signed R.), the hint, and one line for each fitting. None names a
colour, an age or how the reader feels; none speaks for Rowan.

## Also in this release: run 1662's two gaps

Run 1662 (0.4.620) failed two tables that must name every story, because the lodging house (`ones_already_inside`) was missing
from both. The Pell house's twisted journal lines gain one for it, new and flagged here: "I knew which of the two men was the first
one. I told the house it was the other." The staircase company lists it with the stories whose people have no body to stand on the
stair yet (`StairCompany.NOT_YET`), since the lodging house's people are drawn by their own renderer.

## The plan as built

The shop is floor 0 with its ceiling at 4; the flat is floor 6 with its ceiling at 10; the roof's felt is at 12. Both stairs have six
treads, and each top tread sits in the plane of the floor it reaches. The stairhead's rail stops a block short of the worktop, so the
top of the shop stair opens onto the kitchen; the roof stair climbs toward the back of the building into the stair house.
