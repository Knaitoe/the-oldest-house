# The homes that bleed in — 0.4.170

The owner:

> "Review the full houses that have "bled" in. Ensure they don't appear until several passes through the hallways, so they feel more impactful when first encountered. Maybe add food to the fridges. Inspect each "house" for its design and storytelling. Improve as you see fit."

The four homes from 0.4.128 extend four of the ordinary halls' domestic fragments into whole, empty houses:

- the family behind the straight hall's kitchen;
- the students behind the bent hall's dining room;
- a father living alone behind the bent hall's bedroom;
- a grandmother behind the cross hall's kitchen.

A world gets one of them, occasionally two.

## They bleed in later

Until now a home was built with its hall, so the first time a reader walked that hall, the fragment already ran on into a house. Now:

- **At first, the fragment ends where it always did.** A new home is still built with its hall, behind the fragment's wall, with its papers in place. Every cell a reader in the hall could see waits:
  - the opening;
  - the passage through the fragment;
  - any part of the home's wall that faces the hall's open air.
- **It bleeds in later.** The home opens once both of these are true:
  - the halls have been passed through **ten times**;
  - **this** hall has been seen **twice** without it.

  A pass is each time a hall goes from empty to occupied, counted once however many readers share it (`HallAtmosphere`'s "Returns", which already paces the hall changes).
- **Nobody sees it happen.** The opening, passage and wall are cut all at once, and only when:
  - the area is loaded;
  - no camera, spectators included, is within sixteen blocks;
  - nothing living stands in it.

  Cells holding a block entity are left alone. The next time a reader comes down that hall, there is a doorway where the fragment used to stop.
- **It stays open.** A reload, or a rebuild of the hall, never seals an open home again.

Homes already open in older saves are untouched. A rebuilt hall whose home has bled in, or was made before 0.4.170, is rebuilt whole.

## Food in the fridges

Each home's fridge was the diner's generic enamel fridge, empty inside. Now it is the household's own fridge-freezer, two cells tall:

- the fridge below, with two glass shelves and three places for food;
- the freezer above, with its own door and an ice tray.

Opening the fridge turns its light on, and the door runs up into the freezer's cell.

**Taking food.** In an open fridge, touching a shelf takes its food: real food, labelled in the household's own hand. It is taken once, by whoever opens the door first, and nothing is ever put back. Touching the door or an emptied shelf shuts it.

| Household | Top shelf | Middle shelf | Bottom shelf |
| --- | --- | --- | --- |
| Family | **Sandwiches** (2 bread): "In cling film. A strip of tape: LEAH - NO EGG." | **Apple juice** (2): "Two little cartons. BEN on one, in marker." | **Sunday's chicken**: "Under foil. For whoever's home first." |
| Students | **Melon** (3 slices): "ELI on the cling film. Then MINE, in someone else's hand." | **Chips from Friday** (2): "Still in the paper. Nobody has claimed them." | **Biscuits** (4): "Half a packet, folded over and clipped." |
| Father | **Pudding pot**: "A sticker on the lid: KIT. SATURDAY." | his bottles, left where they are | **The end of a loaf**: "Gone hard at the corners." |
| Grandmother | **Apple cake**: "Half of it, under an upturned plate. Wrapped for a train." | **Soup**: "A saucer on top. Enough for two days, or two people." | **Apples** (2): "Small ones, for the next cake." |

## Design and storytelling

Each house already tells its story through its layout and the papers left in it. This pass read them all, and the fridges are where it adds: each fridge is a second account of the same life.

- **Family.** The fridge answers Ruth's receipt ("Leah can't have egg. PLEASE check the sandwiches.") with sandwiches taped LEAH - NO EGG, Ben's name on a juice carton, and a plate for whoever is home first, in a house where one adult's things are packed.
- **Students.** Eli's name on the melon has been claimed over by someone else since he left; Friday's chips (his last night, perhaps) are unclaimed; the biscuits are clipped shut for later.
- **The father.** The only food is the pudding labelled for his daughter's Saturday, beside his bottles and the heel of a loaf, in the house of the letter that says "She was waiting for me and I just sat there."
- **The grandmother.** Ada's recipe has its servings crossed down from six to two, and says "Wrap half for his train." The fridge holds that half, soup that will do for two days or two people, and apples for a cake she may not make here.

None of the food is a supply. It is there once, like the papers. No residents, voices or narration are added.

## Tests and proofs

- **New bleed case:**
  - a new home's opening stays the fragment's wall, while the home itself and its papers stand behind it;
  - ten passes are not enough while this hall has been seen once, nor is seeing it twice in fewer than ten;
  - a camera in the hall keeps it sealed;
  - with nobody near, it opens all at once, and the opening and passage are walkable;
  - it stays open after a reload;
  - no household appears with it.
- **New fridge case:**
  - the family's fridge-freezer stands in its kitchen;
  - opening it opens the freezer's half of the door and turns the light on;
  - each shelf gives its own labelled food once;
  - an emptied shelf only shuts the door;
  - the freezer opens on its own;
  - a reload puts nothing back;
  - every other household's fridge gives its own food in each facing, and the father's bottles stay where they are.
- **Existing cases:** the other eighteen interior cases are unchanged. Their homes are built open, as a home that has already bled in is.
- **Client proof:** an eleventh native domestic page shows the four fridges shut, full, part-taken and emptied, with the freezer open, required by CI. The four home cutaways show the new fridges.
- **Locally:** the twenty interior cases pass on a clean world.

## Versions

- Protocol 96: five new household fitting kinds mean a client and server must both be 0.4.170.
- No layout or Witness change: 54 / 41 / two kinds / three endings.
