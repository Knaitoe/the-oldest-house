# Drisking's pumps, pews and benches (0.4.454)

Two of the owner's notes of 10 November (docs/BORRASCA_PLAYTEST_2026_11_10.md) ask for the same thing.

- Note 1, of a gas pump: "Borrasca needs a custom texture pass". The pump was a red box with two great digits on a cream square,
  and a black block on its side for a hose.
- Note 11, of the church's pews: "again, custom textures would be beneficial here." The pews were rows of oak plank stairs.

I first recorded note 11's screenshot as a waiting room's benches. It is the church: white walls, a plank floor, the church's
board through the window. The table in the playtest record is corrected.

This is the first part of that pass: the pumps, the pews, and every other seat in the town that was a stair.

## The pump

A pump is two blocks, as before, and is now built of parts:

- a steel plinth;
- the lower body in red enamel, with a door on each face (a seam round it, two louvres, a lock) under a white band;
- a chrome waist where the two halves meet;
- the head, all glass between chrome: the sale in one window and the gallons in another, on small wheels;
- a white cap, and on a stem above it a lit milk-glass lens with a red ring;
- down one side, the hose: from its elbow near the top, round under, and up to the nozzle hung in its boot.

Both faces of a pump are the same, since cars pull up on both sides of the island.

The two pumps are not the same pump. The owner asked earlier for each to have its own last sale, and each already says its own
when it is looked at. Now the wheels behind each pump's glass stand differently too. The figures are two pixels by two: they say
that a number stands there and that the two pumps stopped on different ones. They are too small to be read as that number, so the
line under the crosshair is still what tells it.

The station's price board is large enough to read, and it read 1.09 and 1.14 in every year. A grown reader is told the prices
went up. The board now shows 2.89 and 2.99 from the third visit on.

## The pews

The church has oak pews: a run either side of the aisle in each of its four rows, the last row standing back from the aisle
where the door comes in.

- The seat and the back are boards four pixels deep, golden under their varnish.
- Each run has two ends, cut from the same oak with its boards on end: low under the arm at the front, rising in two steps to
  the top of the back, with a line cut round inside the edge and a cross let into it.
- On the back of each pew, for the row behind, is a rack with hymnals standing in it: two red and one blue, a gilt mark on each.

## The benches

Everywhere else a stair stood for a seat there is now a bench: brown weathered slats on black cast-iron ends, with arms.

| Where | What stood there | What stands there now |
|---|---|---|
| The store's porch | two oak stairs | a bench two blocks long |
| The street outside the store | two oak stairs | a bench two blocks long |
| The porch of each house | one oak stair | a bench, looking out at the street |
| The trailhead, where the street ends | two oak stairs | a bench two blocks long |
| The ranger's hut, under the window | three spruce stairs | a bench three blocks long |
| The Sheriff's office, the public side | three spruce stairs | a bench two blocks long and one alone |
| The depot's waiting room | three spruce stairs | a bench three blocks long |
| Behind the store's counter | a spruce stair | the clerk's stool |
| Behind the depot's counter | a spruce stair | the clerk's chair |

A bench of one block has both its ends. A longer one has an end at each end and plain slats between.

The noticeboard's rule from 0.4.451 holds: when the store's front is papered over after the ending, the porch bench and the tin
sign are left as they are.

## How it is made

- One block, as before: `borrasca_shop`. Five new kinds: `pew_end`, `pew_mid`, `bench`, `bench_end`, `bench_mid`. A seat's
  facing is the way whoever sits on it looks. An end's own end is to the left of that (stage 0) or the right (stage 1).
- `BorrascaShops.seats(level, base, from, to, looks, pew)` lays a run and gives each cell its piece.
- A pump's stage is which pump it is. The price board's stage is the year, set by `BorrascaShops.years` when a reader arrives.
- The pictures are sixteen pixels to the block and are drawn in code by `tools/draw_borrasca_town_art.py`, from rows of letters
  where the shapes matter (the door, the dial, the lens) and from small rules where they repeat (boards, slats). Fifteen new
  pictures and one drawn again (the pump's side); three old ones are gone (the pump's old face, its old head and its old globe).
- `tools/draw_borrasca_town_art.py --sheet page.png` draws the pump, a pew and a bench from each side and from above, straight
  on, for review without starting the game.

## Checked

- The Borrasca suite: each pump is its own, foot and head; the pews and benches stand where the stairs stood, each piece of
  its kind, facing its way, its end on the right side; the church's aisle is clear; no stair stands for a seat; the price board
  shows the early prices in the childhood years and the later prices years on; the papered store front still leaves its bench.
- The quick gate (the art tool's own check among it).
- The Borrasca client proof, with a new page of the pumps, the pews and the benches.
- First-person renders in the game of the forecourt, the church and the benches.

## What this is not

It is the first part of the pass, not the whole of it. Still built of the game's own blocks in Drisking: the doctor's basin (a
cauldron), the rain barrels, the flagpole, the stove in the ranger's hut, and the furniture of the houses, which is note 12's
subject and comes with it.
