# The hall drawers (0.4.255)

The owner's rule of 2 November:

> I also think the house should remove all player items 1) not provided by the house 2) not consumable 3) that aren't torches or
> lights which can be manipulated by the house. This prevents players from bringing building blocks inside and cheating some puzzle
> instances. The items could be stored for them in a cupboard or Chester drawer or something similar in the Navidson's residence.

The House now takes from each reader what they carry in from outside, and keeps it for them in a chest of drawers in the Navidsons'
front hall. Code: `house/HouseKeeping`. There is no protocol, layout, Witness or source change; the drawers are part of the manor's
hall, so they stand only in new worlds.

## What the House lets in

- **The House's own things.** Every item of this mod.
- **Things the House made.** Any item carrying custom data. In ordinary play only the House makes those: letters, originals, the
  Floorboards hatchet and the rest.
- **Food and drink.** Anything with food on it, potions you drink, milk, honey and ominous bottles. Chorus fruit is not let in,
  because eating it moves you.
- **Lights.** Torches, soul torches, lanterns, soul lanterns and candles.

Everything else is taken: blocks, tools, weapons, armour and shields, buckets, ender pearls, wind charges, splash potions, maps,
books written outside, shulker boxes. What the House gave a reader comes back in with them, as below.

## Where a reader is

- **Outside:** in none of the House's dimensions.
- **In the manor:** the Navidsons' own rooms, past the front door but not past the impossible door at the end of the hall.
- **Beyond:** the impossible hallway and everything after it, including the outdoor scenes.

The House acts only when a reader crosses from one to another, and speaks on the action bar. It has never seen some readers before, such as a new arrival or a test
fixture placed straight inside. Those it takes to be where they stand. Creative and spectating players are left alone.

## The three lists

The House never marks or changes an item: many rooms compare originals by their exact components. Instead it keeps three lists for
each reader, saved with the world.

- **The drawer.** What it took, exactly as it was.
- **Loose.** The reader's own things taken back out of the drawer and still in the manor, in hand or set down.
- **Carried out.** Things the House gave the reader (anything it would not let in from outside) that the reader carried out into the
  world. It remembers the last 256 stacks.

Two stacks count as the same thing if they match in every component except wear, so a sword used a little is still the reader's
sword.

## The crossings

- **In from outside.** Everything the House would not let in goes to the drawer, unless it is on the carried-out list: those come back
  in with the reader. The House checks:
  - the inventory, armour and off hand;
  - the two-by-two crafting grid;
  - whatever is on the cursor.

  "What you carried in from outside, the House has put away in the chest of drawers in the front hall."
- **From the manor beyond.** The reader's own things taken out of the drawer (the loose list) go back to it. Anything else they carry
  past the hall was found in the House and stays with them. "Not past the hall: the House has put your own things back in the chest of
  drawers."
- **Out into the world.** The reader's own things go with them and come off the loose list. What else they carry that the House would
  not let in is the House's, so it goes on the carried-out list. If anything is still in the drawer, the House says so: "Your own
  things are still in the chest of drawers in the Navidsons' front hall."

## The drawers

A chest of drawers stands against the west wall of the front hall, two blocks in from the front door, facing the hall.

**Opening.** A hand on it opens the reader's own drawer as a chest's six rows. Each reader sees only their own things. Since 0.4.267 a
drawer of more than six rows turns in pages, and a button takes everything back to the slots it came from
(docs/DRAWER_RETRIEVAL_0_4_267.md); before that, the rest stayed out of sight until room was made.

**Taking and putting back.** Anything can be taken out. Taken out, a thing the House would not let in goes on the loose list. Put
back, it comes off. Every change is written straight back, so nothing is lost or doubled if the server stops with the drawer open.

**Closing.** The drawer closes if the reader walks away or leaves the House.

**Set down in the manor.** A reader's own thing tossed in the manor is followed. Picked up again in the manor, it is loose in the
picker's hands. Picked up past the hall, it goes back to its owner's drawer: "The House takes it back to the chest of drawers in the
front hall."

## Tests

`HouseKeepingTests` (exploration), `theHouseKeepsWhatAReaderCarriesInFromOutsideInTheHallDrawers`:
- A reader the House has not seen is taken to be where they stand.
- In from outside:
  - bread, a torch, a candle, a lantern, a drinking potion, a House item and a House-made stick stay;
  - cobblestone, an iron sword, a water bucket, ender pearls, chorus fruit, an iron chestplate, a shield and planks in the crafting
    grid are put away, exactly and all of them.
- The drawers open as six rows:
  - the sword taken out is loose in the manor;
  - the stone taken out and put back comes off the loose list.
- Past the hall the sword goes back to the drawer.
- A plain iron pickaxe found past the hall is noted going out and comes back in, while dirt dug outside is put away.
- Saved and loaded, the lists are as they were.

The live Backseat socket proof's carried diamond is now a House original (custom data), since it is carried in from outside.
