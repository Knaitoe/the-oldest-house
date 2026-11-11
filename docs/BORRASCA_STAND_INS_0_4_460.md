# Drisking's stand-ins (0.4.460)

Note 1 of the owner's playtest of 10 November (docs/BORRASCA_PLAYTEST_2026_11_10.md), of a gas pump: "Borrasca needs a custom
texture pass". 0.4.454 was its first part: the pumps, the pews and the benches. This is the second: everything in the town that
was still one of the game's own blocks standing for something else.

## What stood for what

| Where | It was | It is |
|---|---|---|
| The ranger's station | A smoker under a column of iron bars | A pot-bellied stove, cold, on four legs, a mica window in its door, and its pipe up through the roof |
| The diner's kitchen | A smoker and a furnace | A griddle (two patties and a heap of onions on the plate, three knobs, a bar handle) and a fryer (two wells of oil, a basket in each) |
| The diner's kitchen, the doctor's, the depot's canteen | Cauldrons | An enamel sink on four chrome legs, two taps and a spout, a bar of soap in its dish |
| The diner | The game's jukebox, a wooden box | A jukebox: walnut cheeks, chrome bars over coloured cloth, a row of buttons, an arch of lit tubes round the record. Years on it is unplugged: grey glass, brown cloth, and no light from it |
| Mr. Rowe's counter | The game's great bell, stood on the counter | A brass counter bell. It rings |
| The Sheriff's office | A brewing stand over a cauldron | The office's coffee: the diner's own urn, on a drawer unit |
| The Sheriff's cell | A cauldron | A steel toilet with no seat, a plate on the wall behind it |
| Each of the twelve rooms under the depot | A cauldron ("a cot, a bucket, and nothing else") | A galvanised bucket with a square of board over it |
| Behind the store, and in the yard | Cauldrons | A galvanised bucket, its wire handle up |
| The depot's cage | Barrels, one and two high | Cardboard boxes on steel shelving, two shelves to a unit, a tag wired to each box. Two arrangements |
| The depot's canteen | A brewing stand | The urn again |
| The machine shed | A heavy core on three chains | A crane's block, barred yellow and black, and its hook |
| The yard, and the trailhead | Barrels | Steel drums, in two paints, a stencilled panel on each |

The chapel's bell is a bell. The antique shop's pot is a pot. The store's porch barrel, the office's rain barrel and the barrels in
the children's fort are barrels. Those stay.

## What a hand finds

Each of them answers a hand with what is there:

- "A pot-bellied stove, cold, its pipe going up through the roof. The ash under it has been swept."
- "The griddle. Two patties and a heap of onions on the plate, and the scraper's marks all round them."
- "The fryer. Two baskets hang over the oil."
- "An enamel sink on four legs. One tap drips."
- "A steel toilet with no seat, bolted to the wall."
- "A galvanised bucket." In the rooms under the depot: "A bucket with a square of board over it. There is nothing else in the
  room but the cot."
- "A steel drum. A panel is stencilled on it, and a number under the panel."
- "Cardboard boxes on steel shelving, a tag wired to each. The tags have numbers on them and no names."
- "A crane's block and hook, hanging still on its chains."
- "A jukebox, lit. The titles are typed on little cards. A good half of them are hymns." Years on: "The jukebox, unplugged. The
  title cards in it have gone brown."

**The counter bell rings.** The first ring is only a ring. Rung again with Mr. Rowe at his counter, he says "Once is plenty,
hon. I'm standing right here." (to a grown reader, "I heard it. I'm right here."), and after that "That's enough of that." With
him out front for his smoke, nobody answers it.

## How it is made

`borrasca_shop` gains twelve kinds: `stove`, `pipe`, `grill`, `fryer`, `sink`, `toilet`, `pail`, `drum`, `effects`, `hook`,
`desk_bell`, `jukebox`. A bucket's stage 1 has its board; a drum's and the shelving's stages are two looks; the jukebox's stage is
its year, set beside the station's price board (`BorrascaShops.years`). The lit jukebox gives light.

Twenty pictures and sixteen models at sixteen pixels by `tools/draw_borrasca_town_art.py`.

New builds only. No layout, protocol or Witness change.

## Tests

`theTownFortAndDepotAreRealBuildingsAndTheTownsPapersChangeBetweenVisits`: each new thing stands where the old block stood
(nineteen places); the bucket in a room has its board and the one behind the store has not; every block of Drisking is looked
through, and none is a cauldron, a smoker, a furnace, a brewing stand, a heavy core or the game's jukebox; a hand on each kind is
answered; the jukebox is lit, and unplugged years on; the bell's three rings, and nobody answering with Mr. Rowe out front.

A new page of the Borrasca client proof (`standins`): each of the twelve, turned to show its front and a side, with the second
look of those that have one.
