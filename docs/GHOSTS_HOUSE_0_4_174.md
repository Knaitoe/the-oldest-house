# The Barretts' house, condensed — 0.4.174

The last part of the owner's Ghosts note:

> "ghosts set - condense house - make it into a appropriately smaller, more lived in structure. add command to trigger specific film days for testing purposes. improve Marjorie's crawl into/destruction of the cardboard box."

0.4.172 added the film-day command and the box. This version condenses the house.

## The size

The house was 41 blocks across and 47 deep. Its downstairs room was 26 by 30, the girls' bedrooms 13 by 22 each and the front hall 25 by 13. Most of that was empty floor between pieces of furniture along the walls.

It is now 21 by 25, two storeys over a basement:

- **The room box** shrinks from `(-21,-5,-47)..(21,12,0)` to `(-12,-5,-27)..(12,12,0)`.
- **The floor area** is about a quarter of what it was.
- **The scene's way on** is the kitchen's back door, at `(0,0,-26)`.

## Downstairs

- **The hall.** The front door opens on a hall that rises through both storeys. The stair climbs its west side under a banister to a landing. Coats and shoes hang by the door, the mail is on the console, school portraits hang under the landing and a basket of laundry waits at the foot of the stairs.
- **The front room** (west). The good sofa is under the window. There are radiators, an armchair and footstool on a rug, the shelves and the sideboard with photographs.
- **The den** (east). The crew have made it their video village:
  - the family's sofa, television and shelves;
  - the call sheets on the crew's folding table (the source);
  - the monitor, its two director's chairs, the village cart and a camera;
  - the private-interview corner (the booth) and the account.
- **The family's door** joins the hall to the back of the house.
- **The kitchen and dining room** behind it:
  - the dinner table where the days are shot;
  - the family corner with two armchairs facing each other on a rug, where the day-one interviews are filmed, with the television and the shelves;
  - the kitchen along the back wall: counters, sink, cooker, fridge with its calendar, and the little table they eat at when nobody is filming;
  - the family computer and the sideboard by the dining table, and the softboxes trained on it.
- **The utility strip** (east):
  - the basement stair inside a banister;
  - the washer and dryer;
  - the pantry shelves and the water heater.

## Upstairs

- **Along the front:** John and Sarah's room (double bed, John's desk with his job listings, filing boxes, laundry) and the family bathroom.
- **The landing** overlooks the hall, and a corridor runs behind it.
- **At the back:** Marjorie's room and Merry's room, with the girls' bathroom between them.
  - Marjorie's room has her bed, the drawer, her books and the borrowed hat on the counter, her desk, her dresser and bookshelf, her poster, a rug, a laundry basket, clothes on the floor and a softbox. The plaster her climb cracks is in her west wall.
  - Merry's room has her bed and bedside table, her books, her homework desk, her dresser, a plant, a rug, the planets poster, the drawing on the wall and the jug over her door. The cardboard house stands in the middle.
- **The corridor** has a runner along its length.

## The basement

The basement is under the dining room and utility strip. It holds the boxes nobody unpacked, the shelves, the water heater, the lamp, the case and Marjorie's books.

## The seven days

Every take is in the same room it was, with the same marks relative to that room:

- **The table and Marjorie's crawl** are kept relative to the table.
- **Marjorie's room** keeps its marks as before:
  - the bed;
  - the drawer beside it;
  - the climb on the west wall;
  - the family's line for the evaluation.
- **Merry's room** keeps the cardboard house and the pickup's end.
- **The basement** keeps both sisters' marks.

The routes are new paths through the smaller house, each walked by the house test:

- **The crew's exit** goes from the den out to the hall by the front door.
- **The pickup** runs from the basement up its stair, through the family's door into the hall, up the hall stair and along the corridor into Merry's room.
- **Merry's last walk** leads from her chair round the table, through the family's door and down the hall.
- **Day six.** Marjorie walks from her bed out of her room and along the corridor to the landing, and leaps over its banister into the open hall. That is the same leap, over a real two-storey drop now, held at the same apex relative to the banister.

## New builds only

A saved world keeps the house it already built until that slot is rebuilt. Like the 0.4.121 rework, this is for new houses only.

The older repair passes for saved sets (`GhostsSetFinish`, `GhostsSetDressing` and the soundstage upgrade) are unchanged. New houses are marked done by them as before.

## Tests

- **The house case** now checks:
  - the closed front and bedroom wall;
  - two storeys over the den, the utility strip and the dining room, with the hall rising through both;
  - the call sheets on the folding table in the den;
  - the fridge, the console and the washer;
  - the village cart and a cable nobody trips on;
  - at least 60 windows, and a body for every bed and tub;
  - each stair tread's banister;
  - every route walked with a body's clearance.
- **The other 22 Ghosts cases** stand the reader in the new rooms, with the same checks. All 23 pass locally, with the architecture case and the 106-view render.
- **The saved-dressing case** lays the old set's camera case itself, since the compact house keeps none there. The pass it tests is for saved sets only.
- **The architecture case and the basement view** follow the new table and basement.
- **The live two-client proof** puts its readers in Marjorie's room and on the landing to film the leap.
- **The Ghosts render proof** draws the table from its new place.

## Run 1339

Run 1339 tested 246f7c1 (0.4.173). Its literary job failed one case: the Last Stop capture in `Vignette0492Tests`. Its 600-tick clock ran out on a loaded runner before the men's walk and capture finished.

- **The fix.** The case now has 2400 ticks, as its siblings have 900 and 2400.
- **The message.** It now reports the capture count, the men's positions and the reader's.
- **The other jobs.** The run's other fifteen jobs passed, and its build job was still running when this was pushed.

## Versions

- **Mod version:** 0.4.174.
- **Protocol:** 98.
- **Layout:** 51.
- **Witness:** 55 sources, 42 required, two kinds, three endings.
