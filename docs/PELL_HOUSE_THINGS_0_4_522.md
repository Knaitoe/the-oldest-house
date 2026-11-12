# The Pell house 2: what is in the rooms (0.4.522)

The owner, 12 November: "start by finishing each unfinished vignette". The Pell house (`docs/PELL_HOUSE_PROPOSAL.md`) had its
street, its lot and its doors (`docs/PELL_HOUSE_STREET_0_4_518.md`) and nothing in its rooms. This is its second slice: what
stands in each room, as it is on the first visit, from the proposal's "The house, room by room". Nothing happens in it yet and
the House still does not deal it.

The owner's rule for it, as before: "the house has to have a LOT of attention to detail in its unique textures and
environmental storytelling."

## What is there

**The kitchen.** The fridge in the corner: the girl's photographs on its upper door, a year to each, and on an old one a magnet
set square over a face. The benches along the back wall: two lunchboxes (one has a name label, the other's has been scratched
off), the knife block with a handle in every slot, the sink under the window onto the pool, the stove, cupboards over them. The
table, chrome-edged, and four vinyl chairs. On the west wall the chore chart (CALLUM, then TILLY on a strip of paper stuck over
another name, its corner lifting) and the calendar still on the month she died. The lino is worn pale from the door to the
sink, and burnt in front of the stove.

**The living room.** The recliner turned to the television, not the window, one arm worn through. At its arm a side table: the
remote, a mug gone cold under a skin, and a tape's case, open and empty, its label a birthday. Behind that a standard lamp, its
shade tipped toward the chair, off (a hand switches it on, and its light falls on the chair). Under the television the tapes, each
labelled with a birthday. The window seat under the window onto the pool. The mantel with its cards, every one turned face down.

**The hall.** The school photographs along the back wall, one to a frame, the same girl a little older in each, read left to
right: her face is eight pixels square and the same in every frame (it is the face that will be under the frost). After the last, picture hooks already hammered in at the same spacing, each at the faint pencil cross it was measured
to, with nothing on them. Inside the front door,
the hook board: a small coat on one peg and a cane on the other.

**Nessa's room.** The bed, made, its head to the west wall, the quilt pressed flat at its edge where someone sits. The drawers
with raised dots on clear labels beside their knobs and the talking clock on top: pressed, it says the time. The desk with a magnifier on a stand
under the window. The large-print books. The wardrobe, which opens: clothes, and a cane like Tilly's, shorter. Textured tape
round the light switch. Pencil heights up the door frame, stopping, and under the last a fresh one with the day's date and a
small N. A border of small animals round the top of the walls, steamed off behind the bed and put back not quite straight.
Stars on the ceiling, which show in the dark.

**Callum's room.** A circle painted on the floor, the toys standing on its line facing out, and a mattress on the floor inside
it. Two small pairs of shoes lined up at the door, heels to the wall. A little chest with its top drawer pulled out: the house's
missing cutlery, spoons and forks in rows, no knives.

**The bathroom.** The bath under the window, a non-slip mat in it and a bath toy faded on one side. The basin with its taps and
two toothbrushes in the cup, one dry and old, and kept. The mirror. The toilet.

**The laundry.** The chest freezer against the east wall, humming, padlocked, a folded towel and a hairbrush with long hair in
it on its lid. Its cord taped across the floor from the power point, where a note over the switch says DO NOT SWITCH OFF. The
stool beside it, worn. The trough, the washing machine, a bucket, and a box of pool chlorine far too big for one pool.

**The garage.** The camp bed over the oil stain, a bare bulb on a pull cord over it (it goes on and off). Three boxes along the
east wall, read from the room left to right: NESSA, WINTER; NESSA, BOOKS; NESSA, DON'T, by the camp bed's head, the last word
blacker than the rest and gone over twice. The pegboard with every tool's outline painted on it, some empty, one of
them a knife's.

**The shed.** Two shelves of tapes, dates on the spines, in order. The workbench, three empty paint tins under it, a portable
television on a video deck on it, and the paint tin with LINE on its lid. The transcript taped up on the east wall, and down
the side of its last page one word over and over. The chemist's calendar on the west wall, nights crossed off toward a night
circled twice.

**Outside.** In the front bed a row of stakes, a dead seedling at each and a tag in a child's printing, NESSA; the newest,
nearest the step, a fresh seedling and TILLY in careful capitals. The kennel by the drive, two cells wide, BISCUIT painted over
its opening and a heart in crayon beside it, the chain clipped to nothing, and a bowl full of leaves. The pool chair turned to the water, the towel hung over its back faded
on one side. The pump. In the path a slab with a small handprint and a date. On the drive, a stain where a second car stood.

Each thing answers a hand with what is there (`PellTexts.thing`). None says what it means, and none names a colour or an age.

## How it is made

- `PellThing` (`pell_thing`): one block, sixty-three kinds, each with up to four stages, drawn facing north and turned by its
  facing. Things two cells wide (the table, the chore chart, the three boxes, the freezer, the pegboard, the kennel) put their
  second half in the cell to the right as one faces them (`PellThing.right`), so their labels read left to right. Things two cells long (the
  bed, the mattress, the camp bed, the bath) lie from their head to the cell they face (`PellThing.ahead`). Things two cells
  tall (the fridge, the wardrobe, the height marks, the tapes) stack. Flat things (on a wall, on the floor, hung) stop nobody.
- The circle is the floor of Callum's room: twelve cells of carpet with the paint on them, each its own model, never turned
  (`PellThing.circle`), with the toys standing on top, so the mattress can lie inside it.
- `PellRegistry`: the thing block, and five more materials: the path's slab with the handprint, the drive's stain, Nessa's
  ceiling with its stars (a sheet of the stars alone under it lights itself), and the kitchen's lino, worn and burnt. The stars
  come in four layouts (small stars, a few five-pointed ones, specks, and in one a moon), and each block of the ceiling takes one
  of them turned one of four ways, so the ceiling is no pattern.
- `PellSite.things`: where everything stands. `PellHouse.thing`: a hand on one (the wardrobe opens and shuts, both cells; the
  bulb and the lamp go off and on and their light with them; the clock says the time; the rest are looked at).
- `tools/draw_pell_things.py` draws every thing's textures and models at sixteen pixels to the block, with `--check` (CI runs
  it). It reads the game's list of kinds and fails if they are not its own, and its two folders hold nothing it does not draw.
  `tools/draw_pell_house.py` draws the five new materials and the stars' layouts.

## Decided here

The proposal says what is in each room and not where. These are mine, and any of them can be moved:

- **The kitchen's benches run along the back wall** with the sink under the window onto the pool, and the table stands in the
  middle with four chairs.
- **The photographs read left to right** along the hall's back wall, the youngest at the west end by the kitchen, and the
  empty hooks come after the last, toward the front door. One photograph to a cell, so that each face can be a real eight-pixel
  face (`NESSA_FACE` in the art tool, for the frozen skin to match): four years on the wall, then three empty hooks.
- **The LINE tin's word is on its lid**, as its line says, in marker too small to read from standing; its side has only the
  paint run down it.
- **The bed's head is against the west wall**, where the window was bricked up, and the border behind it is the crooked part.
- **The circle is three cells by four**, round the mattress, the toys on its line at the four sides.
- **The kennel stands by the drive**, where the line will run down the east side. The line itself is a later slice. It is two
  cells wide, a big dog's kennel, so that BISCUIT can be read over its opening at sixteen pixels to the block.
- **A side table and a standard lamp by the recliner.** The proposal does not list them; they are mine, so the chair has the
  things a person who sits in it every night would keep by it: the remote, a cold mug, the case of the tape in the machine.
- **The boxes read WINTER, BOOKS, DON'T** from the room, the last by the camp bed's head.
- **The plant tags and the drawers' labels are too small to read at sixteen pixels to the block**: they show writing and raised
  dots, and a hand gets what they say.
- **The coat hook's cane** is Tilly's, by the door; the wardrobe's is Nessa's, shorter.
- **The things that change between visits are drawn in every state now** (the knife block full, one slot empty, gone; the
  fridge with and without the two new photographs; the pegboard with one or two knives missing; the calendar's crosses; the
  freezer's padlock; the mirror steamed; the towel under the garage door; the television on). This slice places them as on the
  first visit; the visits will move them on.
- **The lines are for every reader alike.** What Tilly finds by touch is for the slice that lets a reader be Tilly.

## The case

`PellHouseTests.everyRoomHasItsThingsAndEachAnswersAHand`: every room's things are where the plan puts them, in their first
visit's state and turned the right way; the two-cell things read left to right from the room and the long ones lie head to
foot; the circle's twelve cells are in their places with the mattress inside it; the stars are on Nessa's ceiling and no other;
the floors have their marks; the cell either side of every door is clear for a body; a hand on a thing gets its own line; the
clock says the time; the wardrobe opens from its upper cell and shuts from its lower, both cells each time; the bulb goes off
and its light with it, and on again; the lamp is off and comes on with its light; the kennel's two halves and the bowl beside it; and with the doors open a body walks over the cord, the circle and the hall under the
photographs, and not through the mattress or the table.

The first case's walk now goes to cells beside Callum's mattress and the garage's camp bed rather than through them, and checks
the kitchen's plain lino where the worn path is not.

## Not yet

The line; the people; the three visits and what each moves on; the tapes; being framed; the last night; the endings; a reader
as Tilly (and the lines she finds by touch); the neighbours' side of the fence; the aftermath; the sounds (the freezer's hum,
the pump, the clock's voice); the Witness source. June's room is still shut.

## Numbers

No layout change (the place's slot and size are as they were). No Witness change: the place is no source until it can be
finished. New: one block (`pell_thing`, 63 kinds, 1,008 states), five materials, 177 textures and 121 models for the things,
and four layouts of Nessa's stars.
