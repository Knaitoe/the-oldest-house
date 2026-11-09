# Borrasca: a working town, the buildings (0.4.422)

The owner, November 9: "Make the town more functional too. A diner where the player can grab some pie and coffee - the host might
respond if the player orders coffee as a child, a sheriff's office with a town drunk, etc. it shouldn't feel like drowned town". The
shops agreed after that are a hardware counter in the general store, a historical society behind an antiques shop, a doctor's office,
a bar, a drugstore with a soda fountain and its own arcade games, and a gas station with candy, soda and a payphone. The town should
also have people going about their day, and pocket money to spend.

The town comes in three releases. This one puts up the buildings and their fittings, and makes everything in them readable or worth
a look. The people behind the counters, buying things and the payphone come next.

## The street past the chapel

The longer hill (0.4.420) left sixty blocks of street between the chapel and the trailhead. It now has six shops, three on each side,
all facing the street, with a sidewalk in front of each and the lamp poles carried on up the east side.

| West side | East side |
|---|---|
| **Hilltop Cafe**: a counter with stools, booths by the windows, the pie case, the coffee urn and the till on the counter, the letter-board menu, a jukebox, and the kitchen behind the counter. | **Dr. Hale's office**: a waiting room with chairs, the receptionist's desk and its lollipop jar, and the corkboard of birth cards; through the partition, the examination table, the scale and the eye chart. |
| **Rowe's Antiques**: glass cases of small old things, a counter with a bell, chairs and lamps for sale, a clock on the wall, a pressed tin ceiling. Through the door at the back, the **Drisking Historical Society**: the framed history (moved here from the store), a map of the old workings, framed photographs, a miner's helmet and a reading table. | **Main Street Drug**: the soda fountain with its taps and stools, the pharmacy counter and its shelves at the back, a comic spinner and a candy rack. Two places by the door are kept for its own arcade cabinets. |
| **Ridge Service**: two pumps under a canopy on the forecourt, a price board by the street, a payphone on the wall, a soda cooler by the door; inside, a candy rack, the counter and till, shelves of oil and bait, and a second cooler. | **The Tipple**: a long bar with its back bar, taps and stools, a pool table and a dartboard (the bar games from Last orders, which play here as anywhere), a booth, and a beer sign in the window. |

The general store gets its **hardware counter**: a pegboard of rope, flashlights, batteries and pocketknives on the south wall where
the frame used to hang, with its price card.

## What can be read and looked at

- **The diner's menu**, the **hardware price card** and the **gas prices** show the early nineties on the childhood visits and later
  prices years afterwards.
- **The doctor's corkboard** holds the cards the families sent in after their babies were born. There are five, a few weeks apart,
  each thanking the doctor. The grown visits show a later year's cards.
- **The map of the old workings** shows the four shafts up the ridge. The first, at the top, is marked as given out in 1902, and a
  later hand has drawn a small square over it marked DEPOT.
- **The framed history** now hangs in the historical society, still too high for a child to read. The account line for a reader who
  read it now says it was at the historical society.
- Every other fitting has a line when looked at: the pies under the glass, the dials on the pumps, the scratched-out number on the
  payphone, the pencil heights by the doctor's scale, the photographs at the depot gate, and so on.

## The ground

The way down the far side used to run as a straight line through the woods. Past the chapel it crossed the new street as a low bank,
right where the west side's shops stand. Every way that crosses a drawn-out slope (the way down and the gully) is now drawn out with
the hill, a point every two blocks, swaying and rising with the trail. The way down now ends at the trailhead, so the street is level.

## The fittings

One new block, `borrasca_shop`, has 24 fittings drawn in native pixels by `tools/draw_borrasca_town_art.py`, plus three surfaces: the
diner's checkered floor, the doctor's and drugstore's linoleum, and the antiques shop's pressed tin. The pie case, the pegboard and the
jar each have a second state: a pie cut, a flashlight gone from its hook, the jar nearly empty. The tool's check runs in the quick
gate and in CI.

## Tests

`BorrascaTests.theTownFortAndDepotAreRealBuildingsAndTheTownsPapersChangeBetweenVisits` now also checks:

- the pumps, the pie case and the pool table are there;
- every shop has a real door;
- the street past the chapel is level;
- the childhood menu, the corkboard, the map and the hardware card read as written;
- years later the menu's prices have gone up and the corkboard is a later year's;
- the frame is in the society, out of a child's reach and readable grown.

## Next

- **0.4.424, people and pocket money:**
  - each shop's keeper;
  - a few coins in a child's pocket on each visit and a wallet when grown;
  - pie and coffee at the diner, with the host answering a child who orders coffee;
  - a flashlight the store won't sell a child "for the trail";
  - soda and candy;
  - the town drunk in the Sheriff's cell;
  - children turned away at the bar.
- **0.4.425, the town's day:**
  - the mail carrier, children on bikes, parents at the diner counter and a truck;
  - the payphone on the phone screen, where the tip line says there is no open case;
  - the couple at the doctor's and the baby announcement after;
  - the bar's retired deputies and the drunk who says too much;
  - friends pooling coins;
  - one friend keeping the antiques dealer talking while another looks through the society.
- **The drugstore's arcade:** its own original games and cabinets, with the friend's initials at the top of one high-score table.
  Nothing from Polybius is reused.

## What stays

- **Witness:** 86 / 65 / 63.
- **Protocol:** 170.
- **Layout:** 135.
- **Content note:** unchanged.
- **New builds only.**
