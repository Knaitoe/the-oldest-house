# The House on the Corner, the owner's playtest notes (0.4.578)

Three of the owner's notes from playing the stories built after Borrasca, all on the House on the Corner
(`docs/THE_HOUSE_ON_THE_CORNER_0_4_408.md`). New builds only. No protocol, layout or Witness change. One word changes in four
lines and four block names; no other line changes.

The notes, as they were recorded when the owner gave them:

- The sleeping dog should be redrawn: a real dog's shape and fur, not a box with a hole.
- "kerb" should be "curb" in every line a player reads, and in the blocks' names.
- The iron fence is improper: loose bars, no top rail, a gap above the base rail, grey bars against a black gate.

## What was there

Eight frames from eye level, before anything was changed.

- **The fence was the game's own iron bars.** They were set one to a cell with their plain state, and in a way that stops a
  bar from joining the next. So each cell held one thin post, and the fence was three rows of posts with air between them:
  no rail at the foot, none at the head, nothing from one bar to the next. The bars are the game's pale iron. The gate was
  drawn in a black iron of its own.
- **The gate was two flat pictures of a grille,** two cells high, standing in a gap between the bars with no post at either
  side and nothing over it.
- **Open, the gate's east leaf stood in the middle of the gateway.** Both leaves were turned the same way, so both swung to
  the west edge of their own cell: the west leaf against the fence, the east leaf across the path. A reader walked through
  it, because an open leaf stops nobody.
- **The balconies** over the houses' doors were a ledge one slab wide with three bars along it. The two outer bars stood on
  nothing.
- **The dog was two boxes and a strip:** a block of plain fur across the pavement, a face on one end of it and a stub behind.
- **"Kerb"** stood in two of the Corner's own lines, in one visitor's line in the kitchen, in the line for the van's wheel in
  Vacant Possession, and in four blocks' names.

## What changed

### The fence

The fence is the lot's own now, in the gate's iron, and every picket, rail and post in it is a bar of its own.

- **Three cells high, each cell a different piece.** At the foot, a low stone plinth with a rail along the top of it and the
  pickets standing on the rail, so there is no gap under them. In the middle, pickets, and near the top of the cell a rail.
  At the head, a second rail just above the first, and above the two rails the pickets' points.
- **The points.** The two pickets at every join between cells are taller than the two between them, and each tall one has a
  short bar across it below its tip.
- **Posts.** A square post on a stone pier stands at every third cell, with a cap and a finial. One stands at each side of
  the gate, at the gate's edge of its cell, and one at the lot's corner, where the front fence and the side fence meet it.
- **It runs where the bars ran:** along the front of the lot, down the whole east side and down the part of the west side
  that the next house does not wall. 197 cells and the gate.

It stops a body as the bars did. Nobody could climb the bars and nobody can climb this.

### The gate

- **Each leaf is a frame:** a stile at each edge, a rail at the foot, a rail at the lock and a rail at the head, pickets
  between, and a lock plate where the leaves meet.
- **The fence's top cell runs over it,** from one gate post to the other, so the gate stands under the same two rails and
  points as the rest.
- **Each leaf hangs from its own post.** Open, the west leaf stands back against the west post and the east leaf against the
  east, and the gateway between them is clear.

### The balconies

- **A ledge three slabs wide** over each door, with a rail round its three open sides: a handrail, a foot rail and balusters,
  and a return at each end back to the wall. Seventeen balconies, fifty-one cells of rail, each on its slab.

### The dog

- **It lies on its side,** the way a dog sleeps on a warm pavement: its head up the street and flat on the ground, its back
  to the houses, its four legs out toward the road.
- **Its shape:** the ribs stand highest, the loin dips and the hip rises again. The head has a muzzle, a nose, and the ear
  that is uppermost fallen across its cheek. The two forelegs lie straight out side by side. The upper hind leg is drawn up
  toward its belly and the one under it trails back. Each leg is thicker where it leaves the body and ends in a paw. The
  tail lies round behind it.
- **Its coat** is painted as it is seen from above, in one picture that every part of its body shows its own piece of: a
  darker saddle down its back and onto its tail, a pale belly, throat, jaw and muzzle, pale paws, a pale tip to the tail, the
  eye shut, and the line of its lip. The hairs are short strokes that lie one way.
- **It lies along the pavement** now, not across it.
- What stops a body is its outline: its body from nose to tail, a quarter of a block high, and its legs beside it, lower.

Its line is as it was: "The dog is asleep in the shade. It doesn't wake."

### "Curb"

| Where | Was | Is |
| --- | --- | --- |
| The Corner, the hint | "The girl on the kerb wants someone to go in with her." | "The girl on the curb wants someone to go in with her." |
| The Corner, the account | "A girl from the kerb took me in. ..." | "A girl from the curb took me in. ..." |
| The kitchen, a visitor | "... out of a paper tray, on a kerb, with people I don't talk to any more. This is close." | "... out of a paper tray, on a curb, ..." |
| Vacant Possession, the van's wheel | "A van wheel, the tread worn smooth on the kerb side." | "A van wheel, the tread worn smooth on the curb side." |
| Block names | Kerbstone (Vacant Possession, Calder Falls), Kerb (the Second Person's street, Proofrock) | Curbstone, Curb |

The README's two sentences that said "kerb" say "curb". The blocks' ids, the code's own names and its comments keep the old
spelling; no player reads them. Older documents in `docs/` are records and were left as written.

## Six new pictures, twelve new models

`iron` (the fence's and the gate's one iron), `plinth`, `fur_dark`, `fur_pale`, `dog_top` (the dog from above) and `dog_nose`.
`fur` changed. `gate_lower`, `gate_upper` and `dog_face` are gone. New models: the fence's three cells, the same with a post,
the corner's three, and the balcony rail's three (straight, and with a return at either end). The gate's four models and the
dog's changed. Every picture is sixteen pixels to the block.

## What the frames show now

Fifteen frames from eye level, three of them with the gate open.

- **The fence from along the pavement:** a black fence with a rail at its foot on a pale plinth, two rails at its head and
  points above, a post on a pier every third cell, running to the corner post and away down the side of the lot.
- **The gate from the cross street:** two framed leaves with a lock plate between them, a post at each side, and the fence's
  rails and points carried over it.
- **The gate open, from the street and from inside the lot:** a leaf standing at each post, and the tiled path clear between
  them.
- **The balconies from across the road:** a ledge under each balcony window with a rail round it.
- **The dog from beside it, from behind it, from ahead of its nose and from across the road:** a dog asleep on its side. From
  over it, the saddle, the pale belly, the ear and the four legs. From across the road it is small, as a dog is.

The frames show shapes and pictures, not the game's light. The tool that takes them draws each block as the game draws one
held in the hand, so the tops of things come out darker in them than their sides. In the game a top is the brightest face.

## Checks

- `tools/draw_corner_art.py --check`: 32 textures, 42 models and states. The checks of the four tools whose block names
  changed (`draw_calder_art.py`, `draw_proofrock_art.py`, `draw_second_person_art.py`, `draw_vacant_art.py`) pass.
- **A new case,** `CornerTests.theLotsIronworkIsItsOwnAndStandsWhereItShould`: every cell of the front, of both sides and of
  the corner is the lot's fence at the right height and turned along its run; there is a post at each side of the gate and at
  every third cell; the fence's top cell is over the gate; each leaf hangs from its own post, a shut leaf stops a body and an
  open one stands at its post; none of the game's iron bars is left anywhere on the street; every cell of balcony rail stands
  on its slab; and the dog lies low along the pavement with its head up the street.
- **The Corner's four cases** (that one and the three it had), architecture's six, and the cast and two-reader namespaces'
  102.
- The quick checks, and the declared case count (1,565).

## What the frames and the previews found

The dog was judged in the frames and, between them, in a small viewer that draws one model from any side. It is quicker than
the game and is a local aid, not in the repository. The last pass was checked in the frames.

- **The dog was redrawn twice.** It was first redrawn lying on its belly with its head between its forelegs. From beside it
  that read as a small dog; from in front it read as a loaf with a dark bar across it. On its side it reads as a dog from
  every side.
- **Its head** was first too shallow for its neck, and read as a long snout on a long neck. It is as deep against its chest
  as a dog's head is now, and the neck is short.
- **Its legs took three goes.** Thin straight legs with square paws, fanned out, read as a comb. Thicker, with one foreleg
  reaching forward across the other, the two forelegs read as one. They lie side by side now, one a little longer.
- **Two raised pieces on its back,** meant to round the ribs and the hip, read as plates laid on it. They were taken off; the
  three heights of ribs, loin and hip do the work.

## Caught while drawing

- **The open east leaf** stood in the middle of the gateway. It showed in no frame, because the first frames were taken with
  the gate shut. It was found by working out where each model stands when it is turned.
- **The pickets of one cell are the same read from either end.** A cell turned round, as the post beside the gate is, still
  meets its neighbours: two tall pickets at every join and two short ones between.
- **The piers stand half a pixel above the foot rail,** so the rail's top and the pier's are never one surface, which
  flickers in the game. For the same reason no two tops of the dog's body are level where they meet, and each leg runs a
  little way into the body it comes from.
- **The east side's first post** would have stood half a block from the corner post. That side counts its posts so that
  the first stands three cells down from the corner.

## For the owner

Nothing here is new story. Four things are choices of mine:

- **The dog lies on its side.** Its line says only that it is asleep in the shade. If it should lie curled or on its belly
  with its head on its paws, say so.
- **Its coat:** a sandy dog with a darker saddle and a pale belly, muzzle and paws. The line gives it no colour, and it still
  gives it none.
- **The fence's points:** the tall pickets carry a short bar across them below the tip. From the street a row of them can
  read as a row of small crosses. If plain points are wanted, it is one line in the tool.
- **"Curb" everywhere,** including in Vacant Possession and the Second Person, whose streets are written in British words
  (a flat, a terrace, a launderette). That is what the note asked for; say if those two should keep "kerb".

## Still to come

The rest of the notes from the same playtest: the aeroplane and the ramp's fence in the city on the ice, the witness book
cutting accounts off, the last swimmer's pool, and the yard's stock stairs, wall light, long table and floating lamp.
