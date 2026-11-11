# Bike night 2: the park (0.4.464)

The second slice of Bike night (`docs/BIKE_NIGHT_PROPOSAL.md`). The owner's note:

> The vignette are dorks at night. Kids on a playground maybe? Lots of fun potential for unique interaction here - a seesaw, slides,
> swings, monkey bars, etc

This slice is the place itself, and the bikes of the first slice standing in it: Calder Park on the last night of October, the six
bikes in the rack by its gate, and the rule for whose bike is whose. The playground has its sand and nothing on it yet: the swings
and the sand balls come next, then the rest of the equipment, then Gus.

**It cannot be finished yet, so the House does not deal it.** An operator reaches it by a test door. Nothing is a Witness source.

## The place

`bike_night`, a literary place under the outside sky, finished once (so that its failures can be permanent, as the owner asked). Its
room is the park and the street past its gate; it grows as the town, the graveyard and what lies past it are built. The night is
midnight. Readers are at a child's height from the moment they arrive, as on Borrasca's childhood visits.

The House's door is the door of the park's tool shed, in the brick wall along the park's south side. The first paper is a sheet
from a school pad taped to the brick beside it: Gus's plan for the night, in pencil. It says to come through the shed because the
gate will be chained, to take a bike from the rack, what to do if Tully turns up, and not to cut through the graveyard. It is in a
plain hand: the House's child's hand is a small child's and loses letters, and Gus can spell (a case caught the first draft of it
coming out as "T say we ar to od for rick or tea").

## The park

- **The back wall.** Brick, six courses, under a stone coping, the whole width of the ground; the shed's own front stands two
  courses over it. The park's rules are on an enamel board by the door: PARK RULES, SHUT AT DUSK, NO CYCLING, and under them in
  marker, in another hand, NO FUN.
- **The path.** Concrete flags, four wide, from the door to the gate, with a side path to the playground.
- **The gate.** Two iron leaves, shut, a chain twice round the stiles where they meet and a padlock on the park's side. Over it,
  between the piers, an iron board: CALDER PARK. It reads the right way round from both sides.
- **The railings.** Iron, two blocks high, with spear tops, between brick piers under stone caps: one at each corner, one each side
  of the gate, and more between. They stop a body half a block above the spears, and the piers stand higher still, so the best hop
  there is (the Hopper's, a little over two blocks) falls short.
- **The playground.** Sand in an edging of squared timbers, open where the side path comes in. Two lamps stand over it, and one of
  them is failing.
- **Lamps.** Four: by the door, the two over the playground, and one over the rack. Each is a foot, two lengths of post and a
  lantern, and a lit lantern lights the ground under it.
- **Benches and a bin.** Slatted benches on iron ends, two blocks long; a wire bin by the first.
- **The rack and the bikes.** Six places by the gate, the six bikes on their stands in it in the order they are named.
- **Trees.** Oaks in October leaf inside the railings, and a dark belt of them outside and past the street. Their leaves are down on
  the grass, thickest under the crowns. Fallen leaves are a block of their own that gives way to whatever is set down on it, so a
  bike laid on the grass does not look for a bare patch.
- **Park Street.** Outside the gate: pavement, kerb, a road with a worn centre line, the far kerb and pavement. The town it leads to
  is a later slice; until then there are only trees past it.

**Nothing in the park is one of the game's own blocks standing for something else.** It is earth, grass, sand, oak, and its own
things: `calder_brick`, `calder_asphalt`, `calder_road_line`, `calder_paving`, `calder_kerb`, `calder_leaves`, `calder_litter`, the
fittings (`calder_fitting`: railings, gate, lamp, bench, bin, edging, pier cap, the sheet of paper) and the painted signs
(`calder_sign`, made as Drisking's are). A case looks at every block in the place and names anything else it finds.

## Whose bike is whose

Whichever bike a reader gets on is theirs for the night.

- A first hand on a bike is still a look at it, and takes nothing.
- Getting on a bike nobody has makes it that reader's. A little after, they are told so.
- Nobody else can take it: a hand on it is told whose it is tonight.
- Getting on another bike that nobody has gives the first one back, and anyone may then take that one.
- A reader's own record keeps the bike they ended with, for the account.

## The failing lamp

One of the two lamps over the playground goes out for a moment every few seconds, and now and then for longer, each time a little
longer than the last. Its lantern is dark and gives no light while it is out. When it comes back after one of the longer times it
is heard: the starter's tick, the tube catching, the hum ("Lamp buzzes"). A hand on it says that it keeps going out.

## The night over

One night for everyone in the park, on one clock that runs while anyone is there. When the park has stood empty for a while the
night is over: every bike is back in the rack on its stand, nobody has one, and the failing lamp is lit for whoever comes next.

Where each bike is, is kept with the world. A night cut short by the server stopping leaves no bike behind: the next night finds
each where it was left and takes it back to the rack.

## A hand on the park's things

The gate, the railings, each lamp, the benches, the bin, the rack, the sandpit's edging and both signs answer a hand with what is
there to be seen, and the gate's chain rattles ("Chain rattles"). None of those lines names a colour, genders the reader or says
how they feel; a case reads them for it, and it caught one that did (the lamp's light had a colour).

## How it is drawn

`tools/draw_calder_art.py` draws the materials and the fittings at sixteen pixels to the block, small pieces as bars that take one
swatch of the park's paint card, larger faces from pictures. `tools/draw_calder_signs.py` draws the signs with Drisking's sign
tool's lettering and pieces. One thing is its own: a long board that hangs free shows, on the back of each of its blocks, the
square from the board's other end, so that it reads left to right from behind too. Both tools refuse a model in which two
differing faces share a plane (`tools/flicker.py`).

## Tests

`BikeNightTests`, in its own namespace and job. The park's cases build the place itself, one to a batch.

- **Built whole of its own things.** The door, the wall and both copings; the sheet and what it says; every block of both signs; the
  gate's eight blocks; every block of the three lines of railings, the piers and their caps; the four lamps and the light they give;
  the benches and the bin; the rack and each bike; the sand, its edging and the gap in it; the street; the trees; and nothing in the
  place but earth, grass, sand, oak, the House's door and the park's own blocks. Not a source, not dealt, the quota as it was.
- **Arriving.** Told where they are; at a child's height; the sheet read to its end.
- **Whose bike is whose.** Two readers: a look takes nothing; on it, it is theirs; told so; the other is refused, with whose it is;
  the first takes another and the first bike is anybody's; the second takes it; the account has the bike they ended with.
- **The failing lamp.** Out, dark and giving no light; the others lit; back.
- **The night over.** A bike left out on the grass is back in the rack once the park has stood empty; a bike the world says was
  left somewhere when the server stopped is taken back at the start of the next night.
- **A hand on things.** Each of ten says what it should, and the park's lines keep the House's rules.
- **The railings.** A rider on the Hopper, hopping as high as it goes at a run, does not get past them.

The client proof (`gradle runClient -PbikeNightSmoke`, required in CI) has two more pages: a corner of the park put together as
the builder puts it, seen from inside the gate, so that the gate's board is seen from behind; and both signs flat, the board from
in front and from behind. Every state of every park block must have its model. I looked at both pages. The rules board's line
said one thing more than is painted on it (by whose order); the line now says what the board does.

I also looked at the park itself at eye level, from a child's height, in eighteen pictures made with a local aid that is not part
of the build: from the door, back at the wall and the shed, along the path, across the playground, at the rack, at the gate close
to and from the street, along the street, and at a corner of the railings. The first set showed two things, both changed: the lamp
by the door stood in front of the rules, and the piers' caps were plain blocks. The lamp is on the other side of the door now, and
a pier has a cap of its own, a slab that oversails it.

## What the other suites found

A new place is looked at by cases that are not its own, so I ran the ones that loop over every place: the architecture pair, the
slots, the dealer, the ways on, the test doors, the staircase's seal, the tiered carve, the folds, the miniature cards, the hazard
layouts, the domestic approach, the stone halls and the rhythm cases. One failed. The Witness registry case holds that every
literary place but two has a story of its own, and the park has none yet. The park is now named there as the third, with the
reason beside it and a check on that reason: the House never deals it, and it has no story. When the night can be finished the
exemption goes and the pool grows by one.

A second failed for a reason that is not the park's. One rhythm case (the retreat guidance) sends its reader to the House's outside
level without making that level first; in the whole suite an earlier case has always made it, and run with only its neighbours it
found none. It makes the levels itself now, and passes by itself.

## What I have not seen

- **The night.** The eye-level pictures I looked at are drawn bright, without the game's sky or its darkness. How dark the park is
  between its lamps at midnight, and how the leaves look by lamplight, I have not seen.
- **The failing lamp, failing.** It is tested by its state, not looked at.
- **The two sounds.** Made and registered; nobody has listened to them.
- **A person in it.** Nobody has walked or ridden in the park but the cases' readers.

## Numbers

Layout 136 (one place added). No protocol change. No Witness change: the place has no story until its night can be finished.
New builds only.

Run 1518 (89c81b7f, the commit before the bikes): all 22 jobs passed. The bikes (8528ebf1, 0.4.463) were pushed after it; their
run had not finished when this was written.
