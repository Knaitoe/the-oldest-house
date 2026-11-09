# Borrasca: the shops' insides (0.4.430)

The owner, after 0.4.428: "i hope you're still working on detailing the architecture".

The shops along the street had their counters and their signature pieces, but the rest of what they sold sat on the same shelf
block as the Last Stop's bar, which reads as a bookcase. The general store's aisles were barrels and hay bales with composters on
top. This pass gives each shop its own stock and finishes the rooms overhead.

## New fittings

Eight new `BorrascaShopBlock` kinds, drawn at native pixel scale by `tools/draw_borrasca_town_art.py` like the rest of Drisking's
shops (69 textures and 52 models now):

| Kind | What it is | Where |
|---|---|---|
| `OIL` | Steel shelving: quarts of motor oil on the upper shelves, jugs of antifreeze on the bottom one | Ridge Service, back wall |
| `BAIT` | A chest freezer with LIVE BAIT on the lid and cups of worms on top | Ridge Service, back wall |
| `BAIT_SIGN` | A hand-painted WORMS / BAIT board with an arrow down to the freezer | Above the freezer |
| `SNACKS` | A wire rack of chip bags | Ridge Service, by the door |
| `CALENDAR` | A feed store calendar with a leaping bass | Ridge Service, behind the counter |
| `TIRES` | A stack of used tires with a chalked price | Ridge Service, out front by the wall |
| `GROCERIES` | A grocer's shelf: tins, boxes, and sacks of flour on the bottom | The general store's stock and aisles |
| `CROCKERY` | A kitchen shelf of plates, mugs and stacked bowls | Hilltop Cafe, the kitchen behind the counter |

The shelves, freezer, rack and tire stack are solid to walk into; the calendar and the bait sign hang on the wall and are reached
past, like the other wall pieces. Each has a line when looked at. The oil and the calendar change on the grown visits: the front
row's labels have faded, and the calendar is still on June.

## The shops

- **Ridge Service.** The back wall is the station's own stock now: chips by the door, two runs of oil either side of the bait
  freezer and its sign, and the calendar behind the counter. A mat lies inside the door, a second lamp hangs over the back, and
  spruce joists cross under the roof. Used tires are stacked out front.
- **The general store.** The stock behind the counter and both aisles are grocer's shelves. The feed is at the back of the second
  aisle: hay bales with barrels on them.
- **Hilltop Cafe.** The kitchen's shelf holds the diner's crockery. Birch joists cross the ceiling.
- **Dr. Hale's office, Main Street Drug and the Tipple** get ceiling joists (birch, dark oak and dark oak), on rows clear of every
  hanging lamp.

## Numbers

No protocol, Witness or layout change: protocol 173, Witness 86/65/63, layout 135. New builds only.
