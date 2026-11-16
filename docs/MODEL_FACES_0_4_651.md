# Faces that draw nothing (0.4.651)

The slice the 0.4.650 note left open: model faces that map only onto fully transparent pixels of their own texture, and so draw
nothing. Some were whole parts missing (a candle with no body under its flame, a chandelier with no cable, posts with no sides), others
the edges of thin pieces falling on a texture's clear margin. Each one is now given the part of its picture it is meant to show, or
left off where there is nothing for it to show. Nothing in play changes but how faces look. No Witness count, layout or protocol
change.

## The check

`tools/flicker.py` gains `blank`: every face at least a pixel across both ways (by the square it reads off its texture) whose texels
are all below the clear line (`CLEAR`, the same one the 0.4.597 flicker check uses) draws nothing. The copies flicker lays over the
faces behind them (0.4.597) are left out; they are their own faces' copies. `flicker.py --check`, in the quick gate, now also fails
on any such face, naming each model and its faces.

Two things it does not look at:

- **Faces thinner than a pixel** (the edges of a sheet of paper a tenth of a pixel thick). 69 such slivers in 19 models fall on clear
  pixels: the edges of notes, tags, cards and survey sheets, and the like. They cannot be seen, and are left.
- **Faces on the game's own pictures.** The Stanley dining table's candle (below) was found in 0.4.650's frames, not by the check.

At the start of this slice the check found 189 faces in 41 models of a pixel or more, and 69 slivers (258 faces in 59 models in
all; 0.4.650's note gave 300 in 62 from an earlier, looser scan). Now it finds none.

## What was done, model by model

Whole parts that drew nothing now show:

- **The Stanley's dining tables** (`stanley_table_set_0..2`): the candle on each place setting had no body; its faces fell on the
  empty middle of the game's candle picture, so its flame stood on nothing. Its sides take the candle's own strip, and its top and
  foot their own squares, as the game's candle does (`generate_thin_rooms_assets.box` takes a `uv` for this).
- **The Kept's chandelier**: the cable it hangs by fell between the two strands of its picture. It shows the middle of one strand.
- **The dead-air dishes and the six signal bulbs**: their short posts fell on the open middle of the mast's lattice. Their sides take
  the lattice's solid rail.
- **The Usher candelabra**: its three flames lay on the clear part of the candle picture. Each takes the flame.
- **The Usher books**: each book in the pile is its own piece now, its cover on top, its page edges on three sides and its spine on the
  fourth (`book_lie`, the covers and a row of page edges and spine colours drawn on the books picture). Before, the pile was flat covers
  whose sides fell on clear rows, so they lay paper-thin with gaps between.
- **The Usher armorial**: the shield plate's faces took the clear border; front and back show the shield, and its edges the shield's
  own edge.
- **The State Route mailbox**: the flag had been drawn across a whole block's picture, so only a corner of the raised flag fell on the
  plate it is painted on and none of the lowered one. Both flags are drawn at the plate's size, six across and seven high: raised
  (stage 0), the arm stands up from its pivot with the red flag at the top; lowered (stage 1), the arm lies forward along the box with
  the flag at its end.
- **The Pitt**: the anchor bolts' sides and tops fell on the clear border of the bolt picture; they take its grey square. Every length
  of cord (the tie from the bolt, the belay's run along the top and its drop, the line on the floor, the cut ends) shows the strand
  turned to lie along it, as wide as the face is across, and each end shows the strand's cut. Before, the long faces fell beside the
  strand and drew nothing, or showed two pixels of it in the middle.
- **Drisking's cut pie**, on the counter and in the pie case: one whole pie whose top fell on the empty corner of a picture of a cut
  pie, so it showed no top at all and stood with whole sides. It is now a pie with a quarter taken out: the half and the quarter left,
  each topped with its own part of the whole pie's picture, and the two cut sides showing the cherries. The cut pie's picture is gone.
- **The Corner's jars** (teeth, nails, murk): three of each jar's four glass sides drew nothing, so the lid floated over the contents.
  The glass picture is one side of a jar, six across and eight high, its edges and foot catching the light, a ring under the lid and a
  streak of shine, and every side shows it.
- **The county road's lamp**: the cage's two sides fell on the clear margin. They show the cage's own side, its frame and the light
  behind it.
- **The burnt torch** in the dark hall (`horror/stub`): its stick's sides fell on the clear rows below the soot. They show the stick,
  charred at the top.
- **The standing paddle** at Pinehook: its blade showed one line of the lying paddle's picture. The block's paddle picture now has the
  standing blade in its top corner, which the boat never shows, four across and six high with a darker edge (`block_paddle`); the
  item's own picture is unchanged.
- **The trailer's bathroom window**, shut and open: the frame round the opening read the picture's clear outer column, so it drew
  nothing and an open window stood in a bare hole. The frame takes the frame drawn in the picture, and the sash shows the picture's
  frame to its own edges, with those edges in the same metal, so it reads as one sash when it swings out.
- **The GIRLS plaque** in Third Floor Girls' (`dare/plaque_0`): its top and bottom take the plaque's own top and bottom rows.
- **The flood station's source**: the low tuft of weed's top shows the stalks' ends, where it took the clear gap between them.

Faces left off, where they lie outside what the picture draws, or against the floor or another piece:

- **The clock** in Third Floor Girls' (`dare/clock_0`): round, in a square plate; its square edges fell on the clear corners.
- **The hotel room plaques**: the plate is drawn inside a clear border, so the piece's top and bottom are clear.
- **The night ward's incubator**: the glass is clear but for its border and a glint or two, so each pane and the lid keep only their
  two broad faces; the rails round it are the frame's edges.
- **The uncanny window's glass**: its four edges lie against the steel frame.
- **The flood station's frayed rope end and the weed on the floor**: their undersides sat on the floor.
- **The circle on Callum's floor** (`pell/thing/circle_*`): the two cells inside the line and the four spare ones had a coat of
  nothing over their boards; they have no paint layer, and break into boards.

The hotel's sconce model had been placed by hand after its tool wrote it; the tool now writes it as it was placed, and the file is
the tool's own writing of the same model, so writing the hotel's models again changes nothing. The uncanny window's tool leaves off
any face it is told is none (`draw_uncanny_environments`), so its glass is written as it now lies.

## Checked

- `flicker.py --check` passes, with no face of a pixel or more that draws nothing, and so does every quick check (each art tool's
  `--check` agrees with the files).
- Look review: all 47 models whose look could change, from two sides each, before and after, from a headless client (16 pages, and two
  more for the two models the first set did not hold), each model cut out and enlarged beside its own before. (The two whole-pie models
  changed only in naming their one picture once instead of twice.) What the frames showed:
  - every part that was missing is there: the candles' bodies under their flames, the chandelier's cable, the posts under the bulbs and
    dishes, the candelabra's flames, the jars' glass with the lid on it, the lamp's cage sides, the burnt stick, the bolts standing out
    of the rock, the cords round, the mailbox flags, the paddle's blade, the trailer window's frame and its sash swung out;
  - the cut pie has its quarter gone, the cherries showing at the cut, on the counter and in the case;
  - the books lie as a pile of books with their pages and spines;
  - the models whose blank faces only came off (the clock, the hotel plaque, the incubator, the uncanny window, the circle cells, the
    rope end) look as they did.
- Nothing else changed: every other model and picture is as it was, and the paddle as an item is as it was.

## Not covered

- The 69 slivers thinner than a pixel.
- Faces on the game's own pictures are not checked; only the one found by eye was mended.
