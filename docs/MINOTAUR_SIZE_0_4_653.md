# The Minotaur, half again (0.4.653)

0.4.584 rebuilt the Minotaur from many parts and asked whether it should be bigger still, adding that the cell's gate would be
the thing to change. The owner answered: "You can adjust the width of the cell gate and corridor too". The staircase note
(docs/STAIRCASE_JUDGEMENT_0_4_652.md) set the size: about half again. This release makes it so, and widens and raises every
way it walks. No Witness count, layout or protocol change.

## What changes

### The creature

- **Before.** 1.8 blocks broad, 3.7 tall, its eyes 3.1 from the floor (0.4.584).
- **Now.** Half again in every way, drawn and bodied alike: 2.7 broad, 5.55 tall, its eyes at 4.65 (`MinotaurEntity.SCALE`,
  1.5). The renderer scales the whole model and its shadow. The grit from its hooves, the breath of its bellow and the dust
  where it strikes a wall come from where its hooves, nostrils and horns now are.
- **The boy in the cell** keeps a boy's size: his own body and his own renderer, as before. He grows into the creature only
  when he wakes, out of sight, in the same corner of the cell; at its new size it still stands clear of the cell's walls there.
- **What it does is unchanged:** the same wind-up and charge, the same two hearts through a raised shield and the same three
  blocks to break it, the same wound, the same walk out at the peaceful ending and the same roaming on the stair after a death.
  A broader body reaches further to either side of its charge, so a reader steps about half a block further aside to let it
  by. That is the point of it: 0.4.84 asked for a more devastating charge.

### The ways it walks

- **The cell's gate:** bars five blocks wide and seven high, where they were three by four. The cell's walls stand eight high,
  a block higher than they did, so its cracked wall still closes over the bars and stands either side.
- **The two doorways of the passage**, through the room's front wall and the well's wall: five wide and seven high, where they
  were three by four, with a lintel over each. The seal that shuts the passage shuts all of it.
- **Why seven high and not six.** The body is 5.55 tall, but the horns rise above it: in the creature's ordinary stance their
  tips stand about six and a quarter blocks high (about six when it walks out at the peaceful ending, lower when it charges with
  its head down). In six-high doorways they would have swept through every lintel. They did at the old size too, by about a
  third of a block under four-high lintels. Seven high clears them in every pose it walks in.
- **The causeway across the well:** five wide, where it was three. Its far edge stays where the stair goes on down below it;
  it widens toward the room.
- **Where the causeway meets the stair.** From 0.4.593 the flight's inner rail ran on across the way: a pier and a length of
  rail stood beside the stair's last two treads, which are level with the causeway, so a reader coming off the stair climbed
  over them or jumped. The rail is now open beside those two treads and over the first riser above them, and the rail above
  that ends at its post. A reader walks off the stair onto the causeway. The creature, broader than the two level treads,
  turns onto the stair up that riser beside them, as a broad body takes the bottom step of any stair.
- **The walk out:** at the peaceful ending it passes the reader by as far again as it is broad, and keeps as much further out
  on the stair's turns.

## Why half again, and not more

- It stands well over a reader (more than three times their height) and fills the doorways it walks through, as the owner
  asked.
- It still fits where it has to go: inside the cell's eight-high walls, through seven-high doorways, horns and all, and up a
  stair nine blocks wide, leaving a reader more than six blocks to pass it on the stair.
- Larger again would need the cell and the doorways raised once more, and leave a reader less of the stair to pass it by.

## Checked

- `FinaleTests.greatStaircaseHasPhysicalDepthAndNoRoomSlotOverlap`, extended: both doorways of the passage five wide and seven
  high with a lintel over each; the gate's bars five by seven with the cell's cracked wall either side and over them, and the
  cell's walls eight high all round; the
  causeway five wide to the room and to the stair; the rail open beside the last two treads and over the first riser, with the
  string still under the riser and the rail above it ending at its post; and a body of the creature's size clearing its way
  from the middle of its cell, through the gate and both doorways, along the causeway on the causeway's floor and up the one
  riser to where its climb begins.
- 123 cases on this source (below): the 86 that touch the staircase, every finale, Witness, collapse, playtest-command,
  sequence-repair, notes-repair and integration-repair case, the charge case (two hearts through a shield, the third block
  breaks it), the stillness goring case and the dark hall's growls. Among them
  `WitnessTests.releasedCreaturePhysicallyReachesAndClimbsTheStairs` walks the creature at its new size out of the cell, past
  the reader, through both doorways and up the stair by its own collision, and `NotesRepairTests` fights it.
- `MinotaurVisualProof` (the client proof) measures the creature's highest point from the drawn model, horns included, in
  every pose it walks in (watching, winding up, charging, staggered, released, roaming) through a whole stride, at the size
  it is drawn, and fails if it comes within a quarter of a block of the openings' height. It writes the figure beside its
  frame times.
- Eye-level frames, before and after, with the creature standing in the gate and in the doorway: the gate from the room, from
  its side, the passage's doorway, the causeway and the foot of the stair. The first after frames showed the horns standing
  over the six-high gate's lintel, which is how the height above was found; the causeway, the doorways and the opening in the
  rail read as intended.
- Build, package and branding checks, the quick checks, the architecture and writing cases and the client proof.

## Not covered

- The fight at the new size is checked by its cases (the charge, the block, the wound), not played through by hand.
- `MinotaurVisualProof` draws the model at the model's own size; the eye-level frames show it at the creature's. Its height
  check does not cover the head thrown back for the moment it is hurt, which can lift the horns about half a block more; it is
  hurt only in the fight.

## New lines

None.
