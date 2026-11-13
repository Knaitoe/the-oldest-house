# The Ones Already Inside 1: the lodging house (0.4.549)

The owner, 12 November: "start by finishing each unfinished vignette". After the Pell house the next in the owner's order is The Ones
Already Inside (`docs/THE_ONES_ALREADY_INSIDE_PROPOSAL.md`), the owner's multiplayer choose-your-own-adventure, which was a proposal and
nothing else. The proposal asks for a complete neutral script before anything else, so this release has two parts:

- **The plan** (`docs/THE_ONES_ALREADY_INSIDE_PLAN.md`): the place, the rules as mechanisms (each with its second source), who knows
  what, the complete neutral script from Rae's request to the four endings, multiplayer and party changes, the twenty slices, and every
  decision the proposal leaves open.
- **The first slice:** the lodging house built whole, with its doors, hatches and frames answering. Nobody is in it yet and the House
  does not deal it.

## What is there

The House's door is the lodging house's own front door: readers come through it into the front hall. Two storeys over a cellar, with
passages behind the walls. The shutters are screwed shut from inside; the house keeps its own night.

- **The front hall.** Coat hooks by the door, the hall table with the house's terms on it (the first paper), the hall stand with its
  glass, and across from them the bench (where each reader's account will wait). The stair rises north up the east side over a panelled
  cupboard whose low door opens from the hall. The hall's floor is small tiles, worn.
- **The parlor** (west): its paper a faded damask, its grate laid and never lit under a mantel and a glass, two shuttered windows, a
  hatch from the passage.
- **The dining room** (east): a green paper with a sprig, two shuttered windows, a hatch from the passage.
- **The back passage,** across the house behind them, its boards worn in a path. At its west end a jib door papered like the hall, flush
  with the wall until it is opened, into the passages. Off its north side:
  - **Daniel's room** (back west): its door bolted on the passage side; distemper gone grey; scuffed boards with the arcs of a chair
    turned on one spot; the food hatch in its west wall; his frame in its back wall, the plaster in it worn smooth at the height of a
    pair of shoulders, straps hanging at the height of his wrists; marks scratched on the east wall, in fives, then not in fives, then
    only lines.
  - **The back stair** (middle), through an arch, down to the cellar: four bare treads, a cell lower each.
  - **The scullery** (back east), round behind the back stair: tiled walls, quarry tiles underfoot, a door into the east passage, a
    back door into the north passage, and the way on.
- **Upstairs, the bedroom gallery:** the upstairs hall, its runner down the middle, railed round the stairwell. Six guest rooms off it,
  three a side, each with its enamel number over the door, a neat sprigged paper, boards, a shuttered window in the front two, a hatch
  from the passage, and in its outside wall a **frame**: a doorcase with nothing behind it, its plaster set a hand's depth back.
- **North of the gallery,** a narrow corridor to the workroom (west; bare plaster full of pin holes and paler squares where things were
  pinned, its boards stained), Vera's room (east; a narrow stripe, a carpet) and a door at its end into the passage.
- **The passages:** a body wide, down both sides and across the back on both floors, laths on one side and brick on the other, dark.
  Ladders at the side passages' front ends join both floors to the cellar.
- **The cellar** runs under all of it, low, brick piers, flags underfoot and the brick's lowest course gone damp. Round three sides,
  under the frames' walls, the **sill**: a timber laid on the floor. Under every frame a **post** stands on it, its foot in an iron
  **shoe** with a **wedge** driven beside it. In the middle of the back run, the **ratchet**: an iron wheel with a pawl in its teeth, a
  lever to lift the pawl, and a chain going up from it into the floor. In one corner, the **dead post**, standing in its own shoe on the
  bare floor, nothing above it, rope burns round it at the height of a pair of wrists.

## What answers a hand

- Doors open and shut, both cells, to a hand on either. **Daniel's** has its bolt on the passage side: the first hand draws it ("The bolt
  is on this side. It slides back easily; it is used.") and the next opens the door; shut, it stays unbolted until the bolt is shot
  again. The **jib door** is flush with the wall until it is opened.
- **Hatches** slide open (a hole through to the far side) and shut, from either side.
- A **frame**: "A doorcase set into the outside wall. Behind it there is only plaster, a hand's depth back, and the plaster is warm."
  **Daniel's**: "A doorcase in the back wall. The plaster inside it is worn smooth at the height of a pair of shoulders."
- The **shutters**, the **hall glass**, the **grate**, the **ratchet**, a **post**, the **dead post** and the **marks** each say what
  they are.

## The first paper

"Terms of the house", signed V., on a card on the hall table: rooms by the week, guests in their rooms after eleven, the hatches for the
house's use; "Do not wake the sleeper in your room. It is not yours yet, and it is not used to being looked at."; "Anyone who is tired
may rest. Anyone who rests will be looked after, and missed by nobody."; "The house does not keep a register. The house remembers." It
says nothing of what a guest is like.

## Decided here (any of it can move)

As listed in the plan, and in this slice:
- The lodging house has no name. The terms are signed V.
- Two storeys over a cellar three blocks high; passages a body wide behind the side and back walls on both floors; ladders at the side
  passages' front ends.
- The frames: a doorcase set into the outside wall, three cells wide and three high, its opening plastered a hand's depth back; one in
  each guest room, Daniel's in his back wall.
- The support: the sill round three sides of the cellar under the frames' walls, a post under every frame, one ratchet.
- The workroom and Vera's room are upstairs, north of the gallery.
- The way on is the scullery's back door.
- The hall's walls take the hall's paper on both floors and the back passage; each other room has its own paper, on its own side of every
  wall (the walls are two-faced).

## What was built

- `LabyrinthPlace.ONES_INSIDE` (one night; slot 240; interior), `LabyrinthBuilder.VERSION` 138. `LabyrinthPlaces` now allows thirty-four
  columns: at eight slots to a column (manor heights 80 to 100) slot 240 is the first past the thirtieth, so it had no room.
- `LodgingSite`: the plan (its constants are the plan), `build`, and where a body is (`inHall`, `inDaniels`, `inPassages`, `guestRoom`,
  `inCellar` and the rest).
- `LodgingRegistry`: fourteen materials; `LodgingWall` (a wall cell papered as its own room on each face, twenty-one pairs, with its
  skirting course and its hatch, shut or open); `LodgingFloor` (a floor cell, the room above's floor on top and the room below's ceiling
  underneath, fifteen pairs); the main stair under its runner and the bare back stair; `LodgingBlock`, twenty-four kinds of fitting.
- `LodgingHouse`: a hand on a door, a hatch or a fitting. `LodgingTexts`: the terms, the lines above, the account's beginning.
- `tools/draw_lodging_house.py`: 108 textures and every model and state, sixteen pixels to the block, with `--check`. It reads the
  game's lists of materials, pairs, parts and kinds and fails if they are not its own.
- The House's tables: the place among the literary places, its miniature, its name on the staircase ("the lodging house"), its line in
  a shared account, and the scullery's back door as its way on.

## The case

`LodgingHouseTests` (its own namespace, `the_oldest_house_lodging`, and its own job).
`theLodgingHouseIsBuiltOfItsOwnThingsAndEveryDoorAnswersAsItShould`: the shell, every floor and ceiling its room's, every wall papered on
each face as its own room is, a skirting on both floors; the main stair's five treads rising north and the back stair's four, each clear
to climb; the gallery's rail round the well and the newel at the stair's foot; the ladders, climbed; every frame in its wall facing into
its room, Daniel's in his; a post on the sill under every frame, the ratchet, the dead post on the bare floor; nothing in the house that is
the game's own; every door the house's own and shut, Daniel's bolted; shut, a body walks the hall and the back passage to the back stair's
head and nowhere else, and upstairs the gallery and the corridor but not off it into the well or into a shut room; each fitting says what
it is; Daniel's bolt drawn, then the door opened, shut, bolted again; every door opens to a hand, both cells; every hatch slides open and
shut; open, a body walks to every room, the cupboard, the passages on both floors, every guest room up to its frame, and under the house
over the sill to the ratchet, the dead post and both ladders; the terms on the hall table, signed V.; and the House does not deal the place.

## Not yet

Everything else in the plan: the rooms' furniture and dressing, the four people, the failed bodies, the night, the endings, the accounts,
the source dealt, the copies for a seventh reader. Doors use the game's own sounds for now.

## Numbers

Layout 138: one place added, in the last slot. No Witness change (the place is no source until it can be finished). New builds only.
