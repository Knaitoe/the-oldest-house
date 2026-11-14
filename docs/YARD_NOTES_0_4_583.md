# The yard and the Supper, the owner's playtest notes (0.4.583)

Five of the owner's notes from playing the stories built after Borrasca: three on the yard's rooms
(`docs/YARD_FINISHING_0_4_358.md`) and two on the Supper (`docs/MARSH_SUPPER_0_4_348.md`). New builds only. No protocol, layout
or Witness change, and no line changed.

The notes, as they were recorded when the owner gave them:

- The stock yard: move the loading platform's stairs one block forward.
- The clothesline: fix the wall light. It is a dark empty tube on a cream base and reads as broken.
- The flooded yard: a floating lamp.
- The Supper: a white box floats in the air above the long table, with a dark block on top.
- The Supper: the table's textures.

## What was there

- **The stock yard's stair** stood with its foot against the wall the back door is in. A stair in the game is climbed from in
  front, where its low half is, and in front of this one was the wall. The first step could only be got onto from the side.
- **The yard's bulb was one model,** used for every one of the yard's 27 lights: a cord two pixels thick and ten long, with a
  small cream box at the end of it. The cord's picture was stretched to fit. It was drawn hanging from the top of its block,
  whatever was over that block.
  - **In a recess beside a back door** (the clothesline's, and both ditches') it hung in a one-block niche in the door's jamb.
    From the doorway it was a dark tube down a cream wall with a cream stub on the end: a wall light with its glass gone.
  - **Ten of the 27 had nothing over them,** so the cord ran up to nothing. Seven hung in open air in the lanes, the flooded
    yard's two among them. Two hung a block out from the heads of the posts that were meant to carry them, over the ledges of
    the flash-flood ditch. One sat on the storm field's pole with a gap between the pole and the glass.
- **The Supper's table lamp was the film set's ceiling lamp,** stood on the table. That lamp is a shade that hangs by a metal
  stem from the ceiling over it. On the table there was nothing over it: a pale shade in the air above the plates, with the
  dark stem standing up out of it.
- **The Supper's table was the film set's kitchen table.** Its top was one pixel thick and showed a four-pixel square of the
  set's picture sheet pulled over the whole block: a smear of worktop. Its legs showed another square of the same sheet,
  pulled the other way.

## What changed

### The stock yard's stair

The stair starts a block clear of the wall, so its foot can be walked onto from the door. It still has four steps. The top
one is now the edge of the platform's own deck, and the piece of the platform's rail beside it no longer reaches an arm out
over the step.

### The yard's lights

The bulb is a fitting in four mounts, and each of the 27 is mounted on what is really round it. A new class, `YardLights`,
does it once a yard room is built:

- **Hung** from the block over it: a rose on that block, a cord a pixel thick, a socket, the bulb. Eight.
- **Hung low** under the upper half of a block: the same on a longer cord that goes up to it. Two, under the joists.
- **On a wall:** a plate on the wall, an arm out from it, and the bulb hanging from the arm's end. Seven. A bulb in a recess
  with one open side goes on the wall at the back of the recess and faces out of it; a bulb beside a wall with nothing over
  it goes on that wall.
- **On a post:** a collar on the post's head and the bulb standing in it. Ten. Where no post stood, one is stood under the
  bulb down to the ground: the game's fence post, set with no arms, so it is a post and not a piece of fence. A post that
  stands in water is in the water, not in a pocket of air.

A hedge is not a wall and holds nothing up, so a bulb in a hedged lane with open sky over it is on a post.

Two bulbs moved, so that their posts are where posts would be:

- **The flooded yard's recess.** Its bulb hung over the middle of the recess's floor, where a post would have stood in the
  way of anyone sheltering from the water. It is in the recess's back corner.
- **The flash-flood ditch's ledges.** Each bulb hung over the ledge a block out from its post. Each stands on its post.

The bulb is still bare: glass, lit, with no shade, because its line calls it a bare bulb. Its glass is drawn lit whatever
the light round it.

**In the code.** `YardLights.mount(level, base, place)` runs after each yard builder in `LabyrinthBuilder` and sets each
bulb's stage (0 hung, 1 on a wall, 2 on a post, 3 hung low) and, on a wall, its facing. `YardLights.held(level, at)` says what
is wrong with how a bulb is held, or nothing. The bulb's shape for a hand is its mount's. The art tool
(`tools/draw_yard_fittings.py`) names every piece of the two pictures and checks that each face takes a piece of its own
size, so nothing is stretched.

### The Supper's lamp

An oil lamp stands on the table where the ceiling lamp hung: a brass font, a burner, and a glass chimney with the flame
inside it. It gives the light the old one gave.

### The Supper's table

The farm has a table of its own. Nine blocks of it are drawn, and none is turned, so its boards run the same way from one
end of the table to the other:

- **The top** is two pixels thick: three scrubbed boards to a block, running the table's length, a seam between each.
- **Its edge** shows the boards' ends across the table's two ends, and one board's long edge down its two sides.
- **A rail** runs under the top round the outside, a pixel in from the edge.
- **A leg** stands at each corner, three pixels square, with a square head where the rails meet it.

The plates, the bread, the jug, the roast and the chairs are where they were.

### Not changed

The film set keeps its own kitchen table and ceiling lamp. Nothing else in the yard's rooms or the farmhouse was touched.

## Four new pictures, thirteen new models

For the yard: `bulb` is redrawn and `bulb_mount` is new; `bulb_0` is redrawn and `bulb_1`, `bulb_2` and `bulb_3` are new.
For the farm: `table_top`, `table_parts` and `oil_lamp`; and `oil_lamp_0`, `table`, four `table_edge` models and four
`table_leg` models. The farm's fitting has four new kinds. Every picture is sixteen pixels to the block.

## What the frames show now

Thirty-two frames from eye level.

- **The clothesline's back door, from the doorway and from under the light:** a bare bulb, lit, hanging from the end of an
  arm that goes back to a plate on the recess's wall.
- **The clothesline's far end, and both ditches':** a bulb on a post beside the way on.
- **The flooded yard's recess, from the channel and from inside it:** a post in the back corner with the bulb on it, and
  the floor clear.
- **The flooded yard's turnout:** a post on the gravel with the bulb on it.
- **The flash-flood ditch's ledge:** the bulb standing on the head of its log post.
- **Under the joists:** a bulb on a long cord from the underside of the half block over it.
- **The turnaround's back door:** a bulb on a short cord from the porch roof.
- **The stock yard's stair, from its foot, from the side and from the deck:** four steps from clear ground up to the
  deck's edge, and the rail stopping at the stair's head.
- **The Supper's table, from each side, from its end and from knee height:** boards down its length, the rail under them,
  a leg at the corner with its square head, and the plates, the bread and the jug on it.
- **The lamp:** the font, the burner, the chimney and the flame, standing on the boards.

The frames show shapes and pictures, not the game's light. The tool that takes them lights every block from one side, so a
bulb's glass comes out dull on its sides in them. In the game the glass is drawn at full light.

## Checks

- `tools/draw_yard_fittings.py --check` (47 textures, 59 models and states) and `tools/draw_marsh_supper_art.py --check`
  (33 textures, 33 models and states).
- **A new check in the architecture case:** every bulb in every finished yard room hangs from a block, is on a wall or
  stands on a post. It finds 27 and logs each one's mount.
- **The stock yard's case, extended:** the stair's four steps stand a block clear of the wall with headroom over each, the
  cell before its foot is clear, and a reader set down at the door walks up it onto the deck without jumping.
- **The yard's twenty cases and the Supper's three,** which build every yard room and the farmhouse and play the Supper
  through.
- Architecture's six, and the cast and two-reader namespaces' 102.
- The quick checks, and the declared case count (1,569; no case was added, two were extended).

## Caught while building

- **The wall light first had a tin shade** over the bulb. The line a hand is told is "A bare bulb over the back step.
  Someone leaves it on." The shade went, and the bulb hangs bare from the arm.
- **A lamp on a post would have said that line too,** standing in a lane with no step near it. A bulb on a post says
  nothing. The storm field's light keeps the line it had of its own.
- **The flooded yard's post** would have stood in the middle of the recess. The bulb moved to the corner.
- **The ditch's ledges** would each have got a second post beside the first, on the ledge a reader climbs onto. The bulbs
  moved onto the posts that were there.
- **In the previews** (a small viewer that draws models without the game, a local aid): the bulb's glass had a white square
  on each face, which read as a label; the oil lamp's chimney was a white box, which was the note again; and the table's
  long edge showed board ends, as if the boards ran across it.
- **The table's blocks were first to be turned by their facing,** as the film set's were. Turned, the corner and edge blocks'
  boards would have run across the table. Each facing has a model of its own.

## For the owner

Nothing here is new story, and no line was written or changed. Six things are choices of mine:

- **A bulb on a post says nothing.** Ten of the 27 are on posts. The one line the yard's bulb has is about a back step, so
  it is kept for the bulbs hung under something or fixed to a wall. A line for a light on a post in a lane is yours to
  write.
- **The bulbs stay bare,** because that line says so. A shade over the wall lights would make them read more plainly as
  outdoor lamps, and would need the line changed.
- **Where a wall light faces.** The clothesline's is in the recess in the door's jamb, on the recess's back wall, facing the
  doorway. It could instead be on the house's own wall beside the door, which is a placement change.
- **The posts are the game's fence posts,** as the camp's poles and the lamp posts on the shore are in the city on the ice.
- **The Supper's lamp is an oil lamp.** The line about the table says "the lamp". The farm also has hanging lanterns, a
  cooker and a set of car keys, so a lamp with a shade and a flex would be as true to it.
- **The table is scrubbed boards on square legs,** a little paler than the room's floor.
