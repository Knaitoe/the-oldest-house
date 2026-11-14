# The last swimmer's light, the owner's playtest note (0.4.581)

One of the owner's notes from playing the stories built after Borrasca, on the last swimmer
(`docs/LAST_SWIMMER_0_4_227.md`). New builds only. No protocol, layout or Witness change, and no line changed.

This release also carries a fix for a fault the frames turned up, which is not the pool's alone: a reader's game could keep
the wrong light for a room for a whole visit (the last section but two).

The note, as it was recorded when the owner gave it:

- The last swimmer: the pool is too dark to see the impressions.

## What was there

Frames from the game itself (the two connected clients of the scene review, on 0.4.578), and the light worked out for every
cell of the hall. Light in the game falls by one for each block it travels, from 15 at a lamp.

- **Twelve lamps for a hall 29 blocks by 35.** They hung under a ceiling eight blocks up: eight over the two long decks and
  four over the water, a third of the way in from each side. None hung over the middle lanes.
  - Straight under a lamp, the brightest spot on the deck had 8 of 15.
  - With every lamp on, the deck averaged 3.3 of 15, and 88 of its 590 cells had no light at all. Over the water the
    average was 3.0, and 67 of 425 cells had none.
  - In the frames the far end of the hall is black with every lamp on.
- **When the steps came, the cover had no light.** After the cover closes the lamps go off, leaving two, and then something
  crosses the cover. With two lamps left, 387 of the cover's 425 cells had no light at all. The brightest had 5.
- **The cover was near black:** a dark navy in any light.
- **The step was a low lump in the cover's own picture.** A box ten pixels by eight and two high, as dark as the cover round
  it. It stayed up half a second and went down the moment the next came up. Steps come every 0.7 seconds, so for part of
  each step nothing was up at all.

The reader is told "You can see where: the cover rises under each step." What there was to see was a black lump on a black
cover in no light.

## What changed

### The hall's lamps

- **Twenty-four lamps, where there were twelve.** Six rows of four across the hall, a row every six blocks from the far wall
  to the near one. Two of each row hang over the decks and two over the water.
- **Each lamp throws its light down.** A lamp under a ceiling eight blocks up lit the ceiling and little else. Each now
  puts its light at a point five blocks below it, over a reader's head, as a lamp with a shade over it does. The point is
  not seen; the light is. It goes out with its lamp and comes back with it.
  - With every lamp on, the deck averages 9.9 of 15 and no cell has less than 6. Over the water the average is 9.5.
- **They go off a row at a time.** The reader is told "Switches click along the wall, one bank of lights at a time." A bank
  is now a row of four: the far row three seconds after the cover closes, then a row each second, the click at each. It was
  one lamp every 0.6 seconds. The last lamp goes off 8 seconds after the cover closes (it was 8.4), and the steps begin at
  7.5, as they did.
- **The two left on are the two nearest the fire door.** They were the two at the near end, one over each deck.

### The pool's own lamps

- **Twelve lamps under the water.** Each is a lens set in a tile of the pool's wall, one block under the surface: six down
  each long side, one every four blocks. Their light carries through the water between them, evenly.
- **They are not on the hall's switches.** When the hall goes dark the pool is still lit from underneath, and the cover over
  it shows. With the hall's lamps off, 404 of the cover's 425 cells have 8 of 15, and the rest, nearest the two lamps left
  on, have 9 to 11. The hall's far corners have none.
- Before the cover goes on, they light the water from inside it.
- **They are off in the pool a reader comes back to.** A reader who closed the pool and is dealt it again is told "The hall
  is dark but for the two lamps by the fire door." It is: the lenses are dark, and nothing lights the water.

### The cover

A pale blue vinyl with a welded seam every half block. The length of it still rolled on the reel is the same blue.

### The step

- **Each step is a foot.** The cover swells a little round it, and the foot stands up out of the swell: four toes, the
  ball, the arch and the heel. All of it is the cover's own blue. It is palest on top, where the cover is stretched
  tightest, and in shadow down its sides.
- **The left and the right by turns,** each pointing the way it walks: down the pool toward the reader, then across. Eight
  shapes: two feet, four ways.
- **The foot before stays down until the next has landed,** as a walker's does, and then lifts. The last one down stays a
  second when no more come.
- **Where it waits** at the edge nearest the reader, it shifts from one foot to the other.
- **It is more than twice the size of a reader's own foot:** a little over half a block long and three eighths of a block
  wide. That is so it is seen from the far deck.
- **Never under the cover's bar.** The cover's leading edge, against the west wall, carries its bar and strap. A step in that
  column took the bar away for half a second and put it back. No step comes up there now.
- What a reader standing on the cover feels as it comes up under them is as it was.

### Not changed

- No line. The hauling, the lanes, the swimmer, the attendant, the fire door, the ending and the song are as they were,
  apart from their light. The pool a reader comes back to is as it was: the cover on, every lamp off but two.
- A lamp that wavers during the song goes dark for a moment, as it did. The light it throws on the deck stays, so the waver
  is no stronger than it was.

## Five new pictures, seven new models, two new blocks

`pool_lamp` and `pool_lamp_off` (the lens in its tile, lit and dark), `sole`, `rise` and `lee` (the cover over a foot, round
it, and in the shadow at the foot of a rise). `cover`, `cover_side`, `reel_face_0` and `reel_face_1` changed. New models
`cover_3` to `cover_9`; `cover_1` changed. Two new blocks, `swimmer_pool_lamp` and `swimmer_pool_lamp_off` (both "Pool
Light"). Every picture is sixteen pixels to the block, and each of the three new cover pictures is the cover's own picture
made lighter or darker, so the seams run on across a foot. `tools/draw_swimmer_art.py` draws them: 60 textures, 71 models
and states.

## What the frames show now

Frames from the game itself: two connected clients, one on the west deck beside the pool and one at the strap at the pool's
far end, each looking at a step with the hall's lamps off.

- **The hall is dark** and the two lamps by the fire door light its south-west end.
- **The cover is an even, dim blue-grey,** lit from under the water, from one end of the pool to the other.
- **From the west deck a step is plainly a pale foot.**
- **From the strap at the far end,** about twenty-five blocks off, a step is a small pale mark that grows as it comes down
  the pool. It reads as a foot from about half way. If it should read as one from the far end, the swell round it can be
  made wider and paler.
- **The light, of 15, at eight places in the hall,** read from the server and from both clients, which agree:

  | Place | Lamps on | Lamps off |
  |---|---|---|
  | The deck at the strap | 11 | 5 |
  | The wall beside it | 12 | 0 |
  | The east deck, half way | 10 | 3 |
  | The far corner | 10 | 0 |
  | The cover, two places | 8 | 8 |
  | Three blocks over the cover | 9 | 4 |
  | Under the ceiling | 10 | 1 |

- **The middle of each step frame,** of 255: from the west deck 53, 53 and 55; from the strap 35, 35 and 26. The frames from
  the strap have the far wall in their middle, not the cover.

## Checks

- `tools/draw_swimmer_art.py --check`: 60 textures, 71 models and states.
- **The pool's four cases** (`LastSwimmerTests`), which now read the light: twenty-four lamps in banks of four, each lit and
  throwing its light down; the pool's own lamps under the water and their light under every cell of the surface; the lamps
  off a bank at a time, and a dark lamp throwing nothing; every lamp off but the two by the fire door; the cover lit evenly
  from underneath in a dark hall and the far corner dark; and the pool a reader comes back to dark but for its two lamps,
  with nothing lighting the water.
- **The two-client scene review** (`tools/run_live_expedition.py --scenes-only`, a CI job): a step frame from the west deck,
  the middle of it at least 30 of 255; and each client's light at the eight places equal to the server's.
- Architecture's six cases, and the cast and two-reader namespaces' 102 (run before the fix below was added).
- The quick checks, and the declared case count (1,567).

## A reader's game could keep the wrong light for a room

**What was seen.** In three of five two-client runs, the client at the strap went on drawing the hall as lit after every
lamp had gone off. Its step frames were bright, and I first took them for good ones. The client was not slow to draw: it
held different light from the server's, and went on holding it.

**Why.** It is the game's own rule, not the pool's.

- A client is sent each chunk with whatever light the server holds for it at that moment. The server works light out on a
  thread of its own, a little behind the blocks.
- The game corrects light afterwards only for players at the edge of their view. A client nearer than that works light out
  for itself from the block changes it is sent, and it is sent none for a block that was already in the chunk.
- The House's dimensions have sky light. So a room built in open air as a reader came in, and sent before the server's light
  had caught up, kept the open sky's light on that reader's client for as long as they stood in it.

In play this could happen wherever a room is built or reset in the moment a reader arrives.

**What changed.** A new class, `LightSync` (`house`), on the server, in the House's dimensions only. A chunk's light is
marked when the chunk is queued for a reader and again when it is sent. Once the server's light has caught up, the chunk's
light is looked at once more, and if it has moved it is sent to that reader again, whole. Nothing is sent for a chunk whose
light did not move, which is nearly every chunk.

**The check.** The two-client run now compares each client's light at the eight places with the server's, and fails on any
sky light in the roofed hall. With the fix on, in two runs, both clients agreed with the server at every reading, and the
server had sent light again for 33 chunks in one run and 46 in the other. With the fix switched off, one more run happened to send the
chunks late enough to be right, so that check catches the fault only when it happens. There is no case that makes it happen
every time.

**What it costs.** For each reader, a mark of each chunk's light twice as it is sent, and a wait of at most 20 milliseconds
in a tick, shared among all readers, and only while chunks sent to them are waiting for their second look.

**What it does not cover.** The Overworld, which is the game's own. And light that changes later in a chunk a reader
already has: that reaches them as block changes, as it always did.

## Caught while building

- **A foot walked the wrong way.** The cover is laid facing west, and a block's facing turns its whole model, so a foot
  drawn pointing north pointed west in the pool. Each foot is drawn turned back by as much. The art tool now checks that
  the pool lays its cover that way, and that the block numbers its eight feet as the tool does.
- **The foot took six goes,** the first four judged in a small viewer that draws models without the game (a local aid,
  not in the repository) and the last two in frames from the game:
  - a sole drawn near black read as a shoe left lying on the cover;
  - a pale square rise under it read as a tile;
  - a darker foot on a paler, rounded rise still read as a thing set down on the cover, and from above its arch made it a
    hook;
  - the cover stretched pale over a foot, on two rises, read in the viewer as something under the cover. In the game's
    own frames the two rises read as a small stepped pyramid;
  - a foot standing up out of one slight swell read as a foot, but small, in a hall that was truly dark;
  - it is that foot, larger, and paler on top.
- **Faces above the block.** A face that stands higher than the top of its block takes its picture from outside the
  picture unless it is told which piece to take. Every raised face is given a piece of its own size.
- **The throw.** A light four blocks below each lamp left the deck at 8.7 on average. At five it is 9.9.
- **A check of light and dark squares.** The pool's light first fell away from twenty-four points in the water, and a
  cover lit that way showed, in the game's frames, as squares of lighter and darker blue. The light is now the same under
  every cell of the surface.
- **A line that would have stopped being true.** The pool a reader comes back to says the hall is dark but for two lamps.
  With the pool's lamps on under the cover it would not have been. They are off there.
- **Where the check reads the light.** Two of the cells first chosen held a floor drain and a stack of kickboards.

## For the owner

Nothing here is new story, and no line was written or changed. Seven things are choices of mine:

- **The pool's lamps stay on when the hall's go off.** This is new. Without them the only light on the cover would be the
  two lamps left by the fire door, and the far half of it would be black again. The other way to do it is to leave more of
  the hall's lamps on, which loses the hall going dark.
- **They are off for the return visit,** to keep its line true. Nothing steps there, so there is nothing to miss. If the
  cover should glow there too, the line needs your hand.
- **The cover is pale blue.** It was near black. A dark cover can be kept if the feet are made paler still against it.
- **A step is a foot, more than twice life size.** It can be made smaller, or put back to a plain rise in the cover. From
  the pool's far end it is a pale mark until it is half way down; say if it should read as a foot from there.
- **The hall is lit to its far end** while the pool is closing: twenty-four lamps where there were twelve. If the closing
  should be gloomier than this, the row over the far wall can go, or every other lamp.
- **A bank is a row of four,** going off a second apart. It was a lamp at a time.
- **The two lamps left on** are both on the fire door's side now.

## Still to come

- The Pell house with more than one reader: cases for the errands, June's jobs, Tilly's question and the book.
- The rest of the notes from the same playtest: the yard's stock stairs, wall light, long table and floating lamp.
