# The city on the ice, the owner's playtest notes (0.4.579)

Two of the owner's notes from playing the stories built after Borrasca, both on the city on the ice
(`docs/THE_BLACK_CITY_0_4_409.md`). New builds only. No protocol, layout or Witness change, and no line changed.

The notes, as they were recorded when the owner gave them:

- The aeroplane should be redone: its canvas reads as sandstone with black blotches on it, its body is a plain box and its
  wing a slab.
- The fence up the ramp should be the camp's own: the game's wooden fence posts read as stray "F" shapes against the dark
  stone.

## What was there

Sixteen frames from eye level, before anything was changed.

- **The aeroplane was built of the tents' canvas blocks.** A hollow tube twelve blocks long and three square, a slab two
  blocks wide laid across it for a wing, a column of four for a tail, and three crossed sticks at one end for a propeller.
  One block in four of the tube was the "cut fabric" block.
- **The canvas** was a pale warm buff, speckled, with one line across it: sandstone's colour and sandstone's grain.
- **The cut fabric** was the same canvas with three wavering rows of holes in it. From any distance the holes ran together
  into dark blots.
- **The stakes on the pass** were the game's spruce fence posts, one at each edge of the steps on every fourth row. A fence
  post joins any solid block that stands beside it, and each of these stood against the rock. So each grew two arms into the
  wall: an upright with two bars to one side, which is an F.
- **The tents and the tarp** over the dissecting table were the same canvas, and the tents had the same cut blocks.

## What changed

### The aeroplane

It is an aeroplane now: a high-winged cabin machine on skis, standing with its tail down on the ice and its nose to the
east, where the box stood. It is thirteen blocks from rudder to nose and sixteen across the wing.

- **The body** tapers from the rudder post to the cabin and stands higher at every step of the way, with its corners taken
  off. It is doped fabric back to the tail and metal panels over the nose.
- **The cabin** has three windows a side and a windscreen over the pilot's seat.
- **The wing** lies across the cabin's roof, thickest a third of the way back from its leading edge. Its tips are rounded,
  and painted to be seen on snow.
- **Two struts a side** run from the foot of the body out to the wing.
- **The tail:** a tailplane through the body, a fin, a rudder painted as the wing's tips are, and a small ski under it.
- **The skis:** two long boards turned up at the toe, a post on each, a leg from each post up to the body and a bar between
  them.
- **The engine's cylinders** stand out round the nose, and its exhaust runs back along one side.
- **The propeller** has two blades of laminated wood with brass over their tips, on a steel boss. It stands a little off
  upright, as it was left.

**The fabric is cut.** Seven cuts cross the wing from front to back, through the top and through the underside, and from
five of them a flap of the fabric hangs down under the wing. Seven more run down the body's sides from its back to its
belly. Each is a straight dark slit, and in each of the body's a stringer of the frame shows across the dark.

**How it is made.** The art tool draws the aeroplane once, whole, in its own pixels, and cuts it into the 116 cells it
stands in. Each cell is a part of one new block with a model of its own, and the tool writes into the block's class where
each part stands, what of it stops a body and what of it a hand finds. So the thing has its own shape to walk round: a
reader walks under the wing and round the skis, and is stopped by the body, the wing, the tailplane and the skis.

**What a hand is told.** A hand on any part that is covered in fabric is told what a hand on the box was told: "The fabric
has been opened in long straight cuts." A hand on a ski, a strut or the engine is told nothing. The propeller says what it
said: "The propeller is whole. Nothing else is."

### The fixed rope

The fence posts are gone. In their place a hand line runs up the foot of the rock at each side of the steps, from the
camp's edge over the crest and down to the foot of the stair into the city.

- **The rope** is a rope, a pixel thick, three pixels out from the rock. It follows the ground: level on a landing and over
  the crest, and climbing with the steps where there are steps. From one row to the next it is unbroken.
- **The stakes** are split wood, two pixels square. One stands on each landing on the way up, and at every fourth row over
  the crest and down the far side. The rope is hitched round the head of each.
- **At each end** the rope is made fast to a stake, with its end hanging from the hitch.
- **The three nooks** in the east wall are left open. The rope on that side ends at a stake on one side of each nook's mouth
  and begins again at a stake on the other.
- 127 pieces of rope, 39 of them with a stake.

It stops nobody. The posts stopped a body at each edge of the steps on every fourth row; the steps are clear to their full
breadth now. A hand finds a stake and passes through the rope. Neither says anything.

### The tents' canvas

The tents and the tarp are still made of canvas blocks, and the tents still have blocks of it that are cut. Both pictures
are new.

- **The canvas** is a grey-green duck: the weave shows, and a seam with its stitches runs down one edge of every block.
- **The cut canvas** has two long straight cuts in it. Each is a line of nothing where the blade went, with the cut edge
  curled pale along one side of it and the dark of the inside along the other. Through a cut, the inside of the tent shows.

### Not changed

The dog line's posts and the tarp's poles are still the game's fence posts. None of them stands against a block it can
join, so each is a plain post. The post under each lamp on the shore is the same. Nothing else in the camp, on the pass or
in the city was touched.

## Twelve new pictures, 128 new models

`plane_dope` (the doped fabric), `plane_dope_tip` (the same, painted), `plane_dope_under` (the fabric's inside, where a flap
hangs), `plane_slit` (what shows through a cut), `plane_cowl`, `plane_glass`, `plane_strut`, `plane_ski`, `plane_engine`,
`prop_hub`, `rope` and `stake`. `city_canvas`, `fabric` and `prop` changed. New models: the aeroplane's 116 parts and the
rope's twelve (level, climbing one way and climbing the other, each with no stake, a stake the rope passes, and a stake at
either end of a run). The propeller's model changed. Two new blocks, `city_plane` and `city_rope`. Every picture is sixteen
pixels to the block.

## What the frames show now

Twenty-four frames from eye level, and the tents again after their cuts were redrawn.

- **The aeroplane from across the camp, from the hut's door:** small and pale between the tarp and a tent, its painted
  rudder showing.
- **From the south-west and from the south:** the rudder, the body climbing to the cabin, the wing with a painted tip, the
  skis under it.
- **From ahead of the nose:** the cylinders round the nose, the propeller on its boss, the legs down to the skis, the
  struts, the cabin's windows.
- **From behind the tail:** the fin and rudder, the tailplane, the wing from tip to tip beyond them, the flaps hanging under
  it.
- **From under the wing:** the cuts through the underside and the flaps beside them, the struts, the body's side.
- **Close on the body:** the cuts down its side, a stringer across each.
- **The pass from its foot, from half way, from the crest both ways and from the stair's foot:** a line at each side of the
  steps, climbing with them, a stake at each landing. No F.
- **A stake from beside it:** the stake, the hitch round its head, the rope coming in and going on.
- **The foot of the pass:** the first stake, the rope's end hanging from it, the rope going up.
- **A nook:** a stake at each side of its mouth and nothing across it.
- **The tents and the tarp:** canvas, seamed. The cuts are thin straight lines.

The frames show shapes and pictures, not the game's light. The tool that takes them lights every block from one side, as
the game lights a thing held in the hand. In them the aeroplane's upward faces come out nearly white, and so do the narrow
faces where one length of the body is broader than the last, which show as bright hairlines. In the game those narrow faces
are darker than the body's side, not brighter.

## Checks

- `tools/draw_city_art.py --check`: 51 textures, 305 models and states. It now also checks that the class of the
  aeroplane's block carries the table the tool draws, and that the rope's block and the tool draw the rope to the same two
  numbers.
- **A new case,** `CityTests.theAeroplaneStandsWholeAndAFixedRopeRunsUpThePass`: every part of the aeroplane stands in the
  cell the table gives it, with its own shape, and no cell holds two; it is thirteen cells long, seventeen across and three
  high; the camp holds exactly its 116 parts and nothing of the canvas box; the propeller is on the nose and faces the way
  the aeroplane points; a body walks under the wing and the wing stops one from above; a hand on fabric, on a ski and on
  the propeller is told what it should be. For the rope: a piece in every row at each side with the rock on its left; from
  each row to the next the two pieces meet at one height, and that height is the same over the lower of the two rows'
  ground everywhere; a stake at each end of a run, on each landing and at least every fourth row; nothing across a nook's
  mouth; it stops nobody; and no fence post is left on the steps.
- **The city's four cases** (that one and the three it had), architecture's six, and the cast and two-reader namespaces'
  102.
- The quick checks, and the declared case count (1,566).

## What the frames and the previews found

The aeroplane and the rope were judged first in a small viewer that draws models without the game. It is quicker than the
game and is a local aid, not in the repository. Then they were judged in the frames.

- **The flaps under the wing** were first the colour of unbleached cloth. In the preview that was the old canvas's buff
  again. They are a shadowed grey.
- **The tents' cuts took three goes.** Cuts that opened wider toward the foot read, laid out flat, as an L. Cuts that stood
  open along their middle read, in the frames, as a jagged tear. They are plain straight lines.

## Caught while building

- **The rope's end** at an end stake would have hung a half pixel clear of its hitch, in the air.
- **A stake on a landing** is twenty pixels tall, and the picture of its wood is sixteen. Stretched over it, the bruised top
  of the stake would have been drawn a quarter longer than it is. The stake is in two lengths, so the top is drawn once, at
  its top, and nothing is stretched.
- **A nook's mouth.** A rope up both sides would have run across the mouths of the three nooks. The rope breaks at each.
- **The propeller's boss** would have shown the same square of its picture squeezed onto faces of three sizes. Each face
  takes a piece of its own size.
- **What stops a body** is worked out for each named thing in a cell, not for the cell: the body, the wing, the tailplane,
  the fin, a ski and a post are each a box of their own. One box round everything in a cell would have closed the air
  between the wing and the body, and between a ski and the post on it.

## For the owner

Nothing here is new story, and no line was written or changed. Six things are choices of mine:

- **What kind of aeroplane it is.** The story says "an aeroplane", with fabric and a propeller. This is a single-engined
  high-winged cabin machine on skis.
- **Its paint:** bare silver dope, with the wing's tips and the rudder painted to be seen on snow. No line gives it a
  colour.
- **It can be climbed.** It is solid where there is any of it, so a reader can step up onto the tail and walk up its back
  onto the wing. Nothing is up there. Say if it should not be possible.
- **The rope is at both sides of the steps,** as the posts were, and breaks at the nooks. A real fixed line would be one
  rope; one side is a one-line change.
- **Nothing new says anything.** The rope and its stakes say nothing to a hand, as the posts said nothing. The aeroplane's
  skis, struts and engine say nothing; only its fabric and its propeller had lines, and they keep them. Anything more needs
  a line written.
- **The tents are grey-green now,** because their canvas was the picture the note called sandstone.

## Still to come

The rest of the notes from the same playtest: the witness book cutting accounts off, the last swimmer's pool, and the yard's
stock stairs, wall light, long table and floating lamp.
