# Borrasca: a working town, the people and the pocket money (0.4.425)

The owner, November 9: "Make the town more functional too. A diner where the player can grab some pie and coffee - the host might
respond if the player orders coffee as a child, a sheriff's office with a town drunk, etc." The buildings came in 0.4.422. This
release puts people behind the counters and money in the reader's pocket.

## The keepers

Each shop has its keeper, standing behind the counter. They are the reader's own, like the rest of Borrasca's cast, and are painted
in the house style on stranger phases 146 to 153 (`literary_drisking_*`).

| Keeper | Where | Sells |
|---|---|---|
| **Lorna** | behind the Hilltop Cafe's counter | coffee, pie by the slice, a malt |
| **Mr. Ames** | behind the general store's till | a flashlight, soda, candy |
| **Mr. Rowe** | behind the antiques counter, by the society's door | nothing; he talks about the society |
| **Kurt** | behind the gas station's counter | soda, candy |
| **Mrs. Odell** | by the doctor's reception desk | nothing; a child gets a lollipop from the jar |
| **Mr. Fenn** | behind the drugstore's soda fountain | soda, a malt, candy |
| **Gus** | behind the Tipple's bar | beer and soda, to grown readers |
| **Earl** | in the Sheriff's cell, sleeping it off | nothing; he talks |

On the last night only the gas station and the bar are open, and Earl is still in the cell. The rest of the keepers are gone and
their shops sell nothing.

Talking to a keeper gets a line, which changes each time and differs for a child and a grown reader. A keeper who sells something
then opens the counter: each thing with its price. The prices are the ones on the menu and the price cards, the early nineties'
on the childhood visits and later prices years afterwards.

| | Child | Grown |
|---|---|---|
| Coffee | .50 | 1.25 |
| Pie, by the slice | 1.25 | 3.50 |
| Malt | 1.50 | 3.75 |
| Soda | .50 | 1.25 |
| Candy bar | .35 | 1.00 |
| Flashlight | not sold | 9.99 |
| Beer | not sold | 3.00 |

## The money

A child has a dollar fifty in change on each childhood visit; a grown reader has a wallet with twenty dollars in it. What is left is
kept in the reader's own record for that visit, and the purse shows it on hover and when used. The money stays in the town: leaving,
dying there, or carrying the purse anywhere else takes it away, and the next visit brings fresh change or a fresh wallet. A dollar
fifty buys a coffee and some candy, or one slice of pie, but not everything; a child who is short is told so (and the diner's host
says to come back with friends, which friends pooling their coins in 0.4.426 will answer).

## What happens at the counters

- **Coffee as a child.** Lorna: "Coffee? You'll be bouncing off the walls. I'll make it mostly milk." The child gets *Coffee, mostly
  milk*. Grown, it is a cup of coffee, and gives a short burst of speed.
- **A flashlight as a child.** Mr. Ames: "A flashlight. For the trail, I bet. I don't sell those to kids, not for up there. Bring your
  folks in." Grown, he sells one and says not to go wandering past the markers with it.
- **The lollipop jar.** A child who talks to Mrs. Odell, or takes from the jar on her desk while she is there, gets one lollipop on
  each childhood visit and the jar shows one fewer. A second try gets "One's the rule."
- **The bar.** A child who walks into the Tipple is turned out by Gus onto the sidewalk ("Out. No kids in here."). Grown readers stay.
- **Earl.** On the childhood visits he asks the child to tell the Sheriff he was only asleep. Years later he half remembers the
  reader as smaller. He says nothing yet about the town; the drunk who says too much comes in 0.4.426.

## The things

Ten items, drawn in native pixels by `tools/draw_borrasca_town_art.py` (its check runs in the quick gate): the child's change and the
wallet, a slice of pie on a saucer, a cup of coffee, coffee mostly milk, a bottle of soda, a candy bar, a lollipop, a chocolate malt and
a bottle of beer. Each food says what it is on the hover and one plain thing when finished.

## Tests

`BorrascaTests.theTownsKeepersSellToAChildAndAGrownReaderAndTurnChildrenOutOfTheBar`:

- every keeper stands at the counter on a childhood visit, and Earl is in the cell;
- a child has 1.50; coffee comes mostly milk and costs .50; the dollar left is short for pie; the store won't sell a flashlight;
  candy is .35 at the station; the bar serves a child nothing; one lollipop, and the jar shows it;
- a child who walks into the bar is turned out onto the sidewalk;
- grown, the wallet holds 20.00, the coffee is coffee at 1.25, a flashlight is 9.99 and a beer 3.00, and the reader stays in the bar;
- on the last night only the station, the bar and Earl are there, and the diner sells nothing;
- leaving takes the purse out of the reader's inventory.

## What stays

- **Witness:** 86 / 65 / 63.
- **Protocol:** 171.
- **Layout:** 135.
- **Content note:** unchanged.
- **New builds only.**

## Next

- **0.4.426, the town's day:**
  - the mail carrier, children on bikes, parents at the diner counter and a truck;
  - the payphone on the phone screen, where the tip line says there is no open case;
  - the couple at the doctor's and the baby announcement after;
  - the bar's retired deputies, and Earl saying too much;
  - friends pooling coins;
  - one friend keeping Mr. Rowe talking while another looks through the society.
- **The drugstore's arcade** (its own original games), the walk, and the lower rooms' rework follow.
