# The cold room, part 1: the house (0.4.754)

The first part of the cold room (design: [WAXWORKS_AND_COLD_ROOM_0_4_749.md](WAXWORKS_AND_COLD_ROOM_0_4_749.md), slot 260). The
owner asked for "one based off of the exorcist"; this is the shape of that story with every name, line and date the House's own.
This part is the place alone. Nothing in it moves, nobody speaks except to a hand, and the House does not deal it.

## The place (`ColdSite`, layout 146)

Built whole in its interior slot. All positions are from the floor under the House's door; north is negative z.

- **The front hall** (x -2..1, z -1..-3): boards underfoot, faded paper on the walls, plaster overhead, a bulb on its flex. The narrow
  table by the door holds a bowl for keys and **the letter** (the source). Coats on hooks on the east wall: a long one with snow on its
  shoulders, a small one that has not been out this winter.
- **The passage** beside the stair runs on to the kitchen door.
- **The kitchen** (x -8..-4, z -3..-9): linoleum, the table with its two chairs, the stove with every ring lit, the sink, a bulb. The
  way on is a door in its north wall.
- **The stair**: five treads up the hall's east side (x 0..1, the n-th at y n, z -4-n), the landing's runner over the top tread.
- **The landing** (y 5, z -9..-12): its runner and its bulb.
- **The back bedroom** (x -4..3, z -14..-21): the iron bed against the east wall, its head (the pillow, at the wall) and foot, the
  straps buckled to the frame; a bedside table either side of the pillow, the lamp's (lit) and the flask's; the cross on a nail over
  the pillow; the chest of drawers on the west wall with the book shut on it; a kitchen chair carried up; the radiator under the
  window, two panes in the north wall.
- **Outside**: the steps fall north from under the window, one block down a block, fifteen of them (ten since 0.4.757, so the place keeps within its slot), between two rendered walls that
  stand two above them, to a landing and a street lamp. It is seen through the window and is not reached on foot from the house.

Materials (`cold_room_*`): boards, runner, paper, plaster, lino, brick, render, flags; the stair treads (of the boards) and the outside
steps (of the flags) are their own stair blocks. Every fitting is one block, `cold_room_fitting` (`ColdBlock`): BED, DRESSER, BOOK,
CRUCIFIX, NIGHTSTAND, RADIATOR, CHAIR, HALL_TABLE, HOOKS, KITCHEN_TABLE, STOVE, SINK, BULB, STREETLAMP, with the stages the later parts
use already drawn: the drawers out, the book open, the cross turned. The bed's head is a bed to the game. Drawn by
`tools/draw_cold_room_art.py`; the house's night sound and the stair boards by `tools/generate_cold_room_audio.py`; the door's bleed is
wind at a window.

## The three (`ColdRoom`, stranger phases 314 to 316)

- **The mother** sits in her chair at the kitchen table.
- **Her daughter** lies on her back in the bed, her head on the pillow; nothing gets her up. Painted with the possessed mark: sunken
  eyes, the slack mouth, barefoot under the nightdress.
- **The priest** stands at the bedroom window, looking out.

Each is made where they keep if they are not there, once for everyone in the house, and only where the house's entities are loaded.

## Lines

Every line is new and is the House's own. They are listed here for the owner's review.

- The hint: "There is a letter on the hall table."
- The letter ("A letter on the hall table", signed T. Aske), six pages: whoever comes is let in; the mother is in the kitchen and is
  not to be asked; her daughter is in the back bedroom; "Keep your coat on"; what talks in that room will know you, and it did not learn it
  anywhere you could follow; kneel where I can see you and read when I read; "I have done this once before, a long time ago, and it cost
  more than I was told it would."
- The mother: "He said somebody would come. Go on up. I can't go up there again tonight."
- Her daughter (a hand on her): asleep, or close to it; her wrists in the straps; her breath stands in the air over the bed.
- The priest: "Not yet. When it's time, I'll call you. Keep your coat on."
- Each fitting has a line of its own (ColdTexts): the bowl with no keys, the coats, the list of doctors crossed out, the stove lit and
  the kitchen cold, the cups, the chair carried up and still warm, the bed and the damp pillow, the drawers, the book "not opened yet",
  the cross, the lamp "the only warm thing in the room", the flask that has not frozen, the radiator cold to the touch, the bulb, the
  window over the steps.

## More than one reader

A party of three to a copy of the house (SceneInstances). The three people are made once for the copy; what a reader touches is theirs
alone; a spectator reads nothing and is not in the house.

## Not yet

Part 2 brings the voice and the things that move while nobody looks; part 3 the rite; part 4 the offer, the end, the account and
Witness, and dealing.

## Checks

- `ColdRoomTests#theHouseItsRoomsAndItsPeople`: the materials, every fitting where it belongs, the stair, the window, the steps and the
  lit lamp; a body walks from the House's door into the kitchen, up the stair and into the bedroom, and never outside; one of each of the
  three, where they keep, the daughter lying; every fitting and person answers a hand; the letter says where she is; not dealt.
- `ColdRoomTests#twoReadersAndASpectator`.
- `GroupSweepTests#coldRoom`, `CastConductTests#coldRoom`, `TogetherConductTests#coldRoom`; `ArchitectureTests` (the source surface and
  the floors).
