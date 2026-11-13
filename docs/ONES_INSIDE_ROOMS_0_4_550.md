# The Ones Already Inside 2: what is in the rooms (0.4.550)

The plan's second slice (`docs/THE_ONES_ALREADY_INSIDE_PLAN.md`): "furniture and dressing, the beds, the washstands, the workroom's
preparations and the record of guests, Daniel's room's years, the scullery, the cellar's dead post." The lodging house of 0.4.549 was
built empty; now its rooms hold what the script needs in them. Still nobody is in it, and the House does not deal it.

## What is there now

- **The six guest rooms.** Each has a bed made and turned down, two cells long, its head to a wall: the front rooms' beds with their
  heads to the front wall, the others' to the room's back. A washstand with its basin, jug and glass against the back wall by the door,
  and a hard chair close by the bed's head, turned to it. On the gallery's wall beside each door, a brass holder for a card, empty: the
  script puts each reader's name in it (N1). Nothing stands between the door and the frame, before the frame, or under the hatch.
- **The parlor**, where everybody waits together: a settee for two facing the grate, an armchair either side of it turned to each other,
  a small table at the settee's end, and a chair by each window. Six seats.
- **The dining room:** the long table, two cells by four with all its leaves in, ten chairs round it (four a side and one at each end),
  and a sideboard against the back wall.
- **Daniel's room, his years:** a narrow cot along the west wall, its head to the back; under the food hatch a shelf the height of a
  tray with his tin plates and a mended cup; a plain chair turned to face the frame in the back wall, the boards under it worn in arcs;
  a pail with a lid. The marks on the east wall and the scuffed floor were already there.
- **The scullery,** Vera's kitchen: the range against the east wall, in and low (it glows), its flue to the ceiling and the kettle warm
  on it; a stone sink under the back wall; a dresser with its plates in the rack; on the table in the middle, the tray laid for one for
  Daniel's hatch; a pail of clean water.
- **The workroom:** the table of preparations (a basin, cloths folded in squares, thread wound on cards, a roll of oilcloth tied shut, a
  jar) and its chair; the record of guests, a cabinet of little drawers against the back wall; the release key on its hook by the door
  (the plan: it starts here and Rae takes it on her way up); a tall cupboard, locked.
- **Vera's room:** a narrow bed made tight, a dressing table with its glass and its chair, a wardrobe.
- **The cellar:** a coil of rope beside the dead post, its end cut; crates along the back; a heap of coal.

## What answers a hand

Chairs, the armchairs, the settee and the dining chairs seat an empty hand, facing as they face (the House's own sitting, as every seat
in the House). Crouching, or with something in the hand, each thing says what it is:

- Bed: "A bed, made and turned down for the night." Vera's: "A narrow bed, made so tight the blanket does not crease."
- Washstand: "A basin, a jug of water and a glass. The water is fresh."
- A guest room's chair: "A hard chair, set close to the bed's head." Daniel's: "A plain chair, turned to face the doorcase in the back
  wall. The boards under it are worn in arcs where it has been turned on the one spot." Any other: "A plain chair."
- The card holder: "A brass holder beside the door, for a card. Empty."
- Armchair, settee, small table, long table, sideboard: each its own line.
- Cot: "A narrow cot. The mattress is worn through in one place, and the blanket at its foot is folded as square as it will go."
- Shelf: "A shelf the height of a tray, under the hatch. Tin plates on it, washed and stacked, and a cup with its handle mended."
- Range: "The range is in, low. The kettle on it is warm." Its flue: "The range's flue. It is warm to the hand."
- The tray: "A tray laid for one: a tin plate, a tin cup, a spoon. Nothing on the plate yet."
- The preparations: "Cloths folded in squares, a basin, thread wound on cards, and a roll of oilcloth tied shut. Everything is clean,
  and nothing has been put away."
- The record of guests: "A cabinet of little drawers, each full of cards. Every card has a room number and a date on it in the same hand;
  most have a second date under the first. None has a name."
- The key: "A hook by the door with a key on it, long and plain. Its tag is blank." Once the key is gone (a later slice): "An empty hook
  by the door. The paint is worn under it where something has hung a long time."
- The rope: "A coil of rope, its end cut, not frayed." The crates, the coal, the sink, the dresser, the cupboards and the pails say what
  they are.

None of it says anything of what the reader is like or how they feel.

## Decided here (any of it can move)

- The guest rooms hold the script's bed, washstand and chair and nothing else; the beds sit off the line from the door to the frame.
- The front rooms' beds stand with their heads to the front wall (under the shuttered window), since the room is only four deep there
  and the frame is on its middle line.
- The parlor seats six; the dining room ten.
- Daniel's chair faces his frame.
- The record of guests is a card cabinet: every card a room number and dates, none a name ("The house does not keep a register. The
  house remembers.").
- The release key hangs inside the workroom beside its door.

## What was built

- `LodgingThing` (`lodging_thing`): one block, twenty-four kinds (bed, cot, washstand, chair, armchair, settee, side table, long table,
  sideboard, work table, key hook, card index, wardrobe, dressing table, card holder, sink, range, dresser, scullery table, shelf, pail,
  rope, crate, coal), with facing and a stage for each kind's parts (a bed's head and foot, a settee's halves, a wardrobe's upper cell,
  the range's flue, the tray, the key on its hook or not). Shapes for every kind; the flat ones (the hook, the card holder, the shelf, the
  rope) stop nothing. The range glows.
- `LodgingSite.furnish`, after the fittings, and where each thing stands (`bed`, `bedFacing`, `washstand`, `guestChair`, `cardHolder`,
  `SETTEE`, `ARMCHAIRS`, `TABLE_*`, `COT`, `SHELF`, `DANIEL_CHAIR`, `TRAY`, `RANGE`, `WORK_TABLE`, `RECORD`, `KEY_HOOK`, `VERA_BED`,
  `ROPE` and the rest), so later slices find them.
- `HouseSitting` and the cast's seated pose (`LiteraryRenderers`) know the lodging house's seats.
- `LodgingHouse` answers a hand on them; `LodgingTexts.thing` has their lines.
- `tools/draw_lodging_things.py`: forty textures and every model and state, sixteen pixels to the block, with `--check`; it reads
  `LodgingThing`'s kinds and fails if they are not its own. In the quick gate.

## The cases

`LodgingHouseTests` (namespace `the_oldest_house_lodging`, job lodging-house-gameplay):
- `theLodgingHouseIsBuiltOfItsOwnThingsAndEveryDoorAnswersAsItShould` as before; the furniture is among the house's own things, and the
  walks go to free cells of the dining room and the scullery (their tables stand where the old cells were).
- New, `whatIsInTheRoomsStandsInItsPlaceAndAnswersAHand`: each guest room's bed (head and foot, facing, its head to a wall), washstand,
  chair and empty card holder; nothing before any frame or hatch, nor either side of any door or the way on; the parlor's seats; the long
  table's eight cells and ten chairs and the sideboard; Daniel's cot, the shelf under the food hatch, his chair facing north; the tray,
  the range lit with its flue, the sink, the dresser; the workroom's table, record and key on its hook inside the workroom; Vera's bed
  made tight, dressing table and wardrobe; the rope beside the dead post, the crates and the coal; every one of the ninety things standing
  on something or hung on a wall; a body sat in a guest chair, an armchair, the settee and at the table, facing as each faces; and
  fifteen of them saying what they are, none of them a word about the reader.

## Not yet

The four people, the doubles in the beds, the night, the endings, the accounts and the source dealt: slices 3 to 20.

## Numbers

No layout change (the place is rebuilt whole, new builds only). No Witness change.
