# What the playtest found broken, second batch (0.4.447)

From the owner's notes of 10 November (docs/BORRASCA_PLAYTEST_2026_11_10.md): the rest of what was plainly broken, in Borrasca,
Last orders, the Drawing and the Sphinx's vestibule. The sign and texture pass and the larger pieces follow.

## Last orders: the two in the next booth

Owner: "Lol was this intentional, in last orders?" It was not. The two in the next booth are told to lean in over their table.
Each scene that poses its own cast numbers its gestures from one, and the bar's lean is five. Five is also Costume Night's crawl,
and the cast's model applied the crawl to anybody whose gesture was five: the pair were drawn on all fours in their seats, heads
out over the table, and stood only as tall as a crawling body.

The same thing was happening in the Kept. Their reach toward the crate is five, and all four were drawn on all fours for it.
The bar's whistle is four, which is also the plain two-armed reach, so the man in booth four whistled with his left arm out.

A body now answers for itself: `LiteraryActor.ownGestures()` is true for the casts whose scenes keep their own gestures (Last
orders, the Kept, Pinehook, the truant, the Drawing, the house on the corner), and `crawling()` and `reaching()` are false for
them. The model and the body's height both ask those. A scene that gives its cast a gesture table of its own belongs in that
list.

## The Drawing: the board across the window

The noticeboard on the post office is two halves. The second half was hung one block north of the first, across the lower pane
of the post office's window. It hangs to the south of the first now, on the wall.

## Borrasca

- **The payphone** at the gas station was mounted on a window pane. It is on the wall two blocks along, and it now counts as a
  piece that hangs on a wall, so the town's test for those covers it.
- **The boards in the yard.** Two of the plank slabs by the depot's east door hung in the air: one was a top slab over nothing
  and one was set a whole block up over a half one. The corner is a stack three boards high and the rest lie on the gravel. A
  third, by the tailings, is set down the same way.
- **The end of the world.** Since 0.4.429 nothing within nine blocks of the wire stands high enough to jump the wire from. The
  hill ended six rows behind the wire, inside those nine blocks, so the cliff at the valley's head was never built: the ground
  ran on level behind the yard and stopped at nothing. The hill now goes on fourteen rows further (`BorrascaRooms.HEAD`). Past
  the nine blocks the cliff stands as it does down both sides of the valley, seven blocks and more over the last ground a reader
  can reach, with tall trees on top. The woods on the rest of the hill are where they were.
- **Two more edges.** The test that walks everywhere a reader can get to was then asked whether any of it is at the edge of the
  hill, and it found two more places. Behind the station the valley's walls stopped a row short of the station's back wall, with
  three rows of level grass past them and nothing beyond; the back fence began a block clear of the station, and the hedge in
  front of it was a step up over it. A reader could walk round the back of the station, or over the fence, to a corner of the
  hill. The walls now run on south and the same rock closes the valley behind the town: the fence, from the station's own corners
  to the walls, the hedge behind it, a row of rough grass, then the cliff with the woods on top. The House's door and its way in
  behind the station are as they were. And on the long climb between the Tree and the second marker, the way down the far side
  had worn the west wall into a slope that rose a block at a time to the hill's edge. The walls stand along the drawn-out slopes
  now as they do everywhere else.
- **The bikes' wheels.** Each tyre was eight separate pieces with a gap between each two. It is a closed ring of sixteen, every
  other piece taking the tread's groove so the wheel is seen to turn, with a hub and eight spokes.
- **Riding.** The rider was placed .3 above the road less the .6 a player is carried at. For a child that put their feet on the
  road, their hips at the cranks and their eyes level with the handlebars and the top of the saddle: the bars filled the top of
  the view and the saddle's top lay across the middle of it. The rider now sits on the saddle, whatever their height, and the
  bars are low in the view. Seen by others, they lean with the bike about the road under it.
- **The depot's front door.** Its lever is the way in on the last visit. On any other it did nothing and said nothing, and the
  door itself, which has no handle, said nothing at all. Both now say so.
- **The water cooler** in the depot's office (owner: "re-use water fountain from personhood vignette.") is the waiting room's
  cooler, its back to the wall. It was a block of white concrete with a block of blue glass on it. A hand on it sends a bubble
  up through the bottle.

## The Sphinx's paw

Owner: "Sphinx talons still need three distinct nails, not the many several smaller ones." Her paw over a door, a tread or a
shelf is three blocks side by side under the three columns of her foreleg. Each block had four toes with a claw apiece, which
made a fringe of twelve small claws. Each block is now one toe with one talon: three toes, three nails.

## "No entry" (notes 37 to 39)

Three screenshots carry this note: the gap between the depot's front and the wire, a yard lamp seen through the wire, and the
ladder up the water tank. It can be read three ways: that these should be marked NO ENTRY, that there should be a way in, or
that a reader should not be able to get to them. The first is the likeliest and belongs to the sign pass. The owner is asked.

## Tests

- `LastOrdersTests.aFrameForEverythingWonAndLeftOnTheTable`: the two in the next booth lean, and neither is crawling, reaching
  or low; the man's whistle is not the plain reach.
- `KeptTests.theDoorsOpenedWithEyesShutAndTheMachineTakesAMouth`: the four reach toward the crate standing.
- `DrawingTests.aMorningInTheSquareFromTheNoticeToTheStone`: both halves of the board have wall behind them, and the window's
  panes have nothing across them.
- `BorrascaTests.theTownFortAndDepotAreRealBuildingsAndTheTownsPapersChangeBetweenVisits`: every board in the yard rests on the
  ground or on the board under it.
- `BorrascaTests.nobodyJumpsTheWireFromTheGroundOutsideIt`: nowhere a reader can walk to, from the station, is at the edge of the
  hill on any side, and the cliff behind the yard stands over the ground in front of it.
- `BorrascaTests.aChildGetsOnKendrasBikePedalsAndGetsOff`: the rider's hips are on the saddle and their eyes well over the bars.
