# A sort of thing 1: the place (0.4.641)

The first part of `docs/SLEEPING_KING_PLAN.md`, from the owner's proposal (`docs/SLEEPING_KING_PROPOSAL.md`). The place alone: a
drawing-room and its looking-glass, the same room the other way round behind the glass, and the ruled country beyond it laid out in
squares, with the King asleep under his tree. Nobody else is in it yet, and the House does not deal it until part 3.

## The place

- **The drawing-room.** The House's door opens into it (x -6 to 6, z -1 to -12, ceiling five high). Paper over a dado panel, boards,
  a plaster ceiling. On the north wall the chimney-piece: two marble jambs and the fire in its grate between them (the room's light),
  the hearth before it, and over it the looking-glass, three wide and two high in a gilt frame, with the chimney-breast round it. The
  hearthrug; the chessmen fallen among the cinders, one king on his side; a little round table with the chess problem lying on it,
  printed backwards; a wing armchair turned to the fire; a tall window in each side wall. The way on is a door in the east wall.
- **Behind the glass.** The same room reflected across the glass's plane (every thing at the same height and the same distance from
  the glass, facing the other way). The problem on its table there reads the right way round. Where the House's door is in the first
  room, this one has an open doorway onto the country.
- **Through the glass.** Nothing passes the chimney wall on foot. A hand on the glass, standing at the hearth, takes a reader through
  to the hearth on the other side, facing on; a hand on the other glass brings them back.
- **The board.** Three files of sixteen blocks (x -25 to -10, -8 to 7, 9 to 24) by eight ranks of sixteen, northward from the house,
  light and dark turf by parity. Two rows of brook, sunk a block, lie between each rank and the next. A box hedge two high between the
  files, with a gap at the middle of every rank. A gravel road up the middle file, over a footbridge at each brook. Round the strip
  the country goes on to x ±64; beyond the eighth rank a low hedge ends it.
- **The wood.** Small trees over the whole fourth rank, clear of the road and the hedge gaps; in its east file the King's tree, taller,
  its trunk two thick.
- **Filled and ruled.** Every block of the country is drawn twice by `tools/draw_king_art.py`: filled, and ruled (the filled face's
  own outline and a little hatching on paper, derived from it so the two cannot drift apart). Part 1 draws the country as the deep
  sleep has it: the third to fifth ranks filled, the first, seventh and eighth ruled, and the second and sixth the band between, a block
  at a time, ruled growing toward the ruled side. The country beyond the strip is ruled, blending into fill near the filled ranks.
- **The King.** An old man in a long robe with a worn gilt hem, a white beard, and a night-cap pulled down to his brows, lying asleep
  at the foot of his tree. Painted by `tools/paint_character_skins.py` (`literary_king`, stranger phase 217). The cap's tall tip and
  tassel are not modelled yet (the renderer, part 2).

Every thing answers a hand with one line; the ruled ground says what it is.

## Made as

`LabyrinthPlace.SLEEPING_KING` ("a_sort_of_thing", one visit, slot 242, outside), `LabyrinthBuilder.VERSION` 140; `KingSite` (the
plan as constants, the build, where a body is); `KingRegistry` (nine country materials, each with its ruled twin, and six of the
room's); `KingBlock` (eight kinds of fitting, with stages for the chimney-piece, the glass, the window and the problem); `King` (the
King's body, a hand on anything, the glass; never dealt); `KingTexts`. `tools/draw_king_art.py` draws 37 textures (the fire a
four-frame strip) and every model and state at sixteen pixels, with `--check`. The House's tables: the literary places, the
miniature, the staircase name ("the ruled country"), a shared-account line, the way on, the door bleed, the scene clock (morning).
`tools/generate_king_audio.py` makes his breath, in and out (four seconds each), and a soft footstep, from noise, registered in
`KingRegistry` for part 2; the door bleed (`bleed.sleeping_king`, `tools/generate_door_bleed_audio.py`) is the two breaths through
a door, its own sound, as every room's is.

## Checked

`KingTests.theDrawingRoomTheGlassAndTheRuledCountry` (the `king` namespace and job): materials, both chimney-pieces and glasses, the
problem's two ways, the fittings, the windows; filled, ruled and band ground; the brook and bridge, the hedge and its gap, the tree;
walking (the drawing-room alone; from behind the glass, over the ranks to the King's square); the King asleep, once; through the glass
and back; every line; the backwards paper; not dealt.

## New lines (for the owner)

All in `KingTexts`:

- Hint: "There is a chess problem on the little table, printed small."
- Chimney-piece: "The fire is real enough to warm a hand. The marble over it has a crack, mended with something that has gone yellow."
- Glass: "The glass shows the room the other way round. Near the bottom it is not quite glass any more, more like a mist, and it
  gives under the hand." / "The glass shows the room the other way round, the way you came."
- Hearthrug: "A hearthrug, worn bald where feet have rested on it for years."
- Chessmen: "Chessmen in the hearth among the cinders, as if the game had been knocked over and nobody had picked it up. One king lies
  on his side."
- Table: "A little round table, polished until the grain shows like water."
- Problem (behind the glass): "Here the type reads the right way round: a chess problem, and under it, \"White to play, and win in
  eleven moves.\""
- Problem (before the glass, unused while the House reads it): "The type runs the wrong way. Held up to the glass, it might read."
- Armchair: "A deep armchair turned toward the fire. The seat is still a little warm."
- Window: "Through the glass the country lies in squares, as far as the eye goes."
- The King: "An old man asleep under the tree, in a heap, in a long robe and a tall night-cap with a tassel. He is snoring."
- Ruled ground: "Here the ground is only drawn: an outline on paper, and a little hatching where the shadow would be."
- The first paper, "A chess problem": ".sevom nevele ni niw dna ,yalp ot etihW / nwap etihw a sa nigeb uoY" and "The diagram above the
  words is a board of eight squares by eight, with the pieces drawn on it as little pictures. Somebody has drawn a line in pencil from
  the second square up through the board to the eighth, and stopped."
- The account (unused until part 3): "There was a drawing-room with a fire in it, and a glass over the fire, and the same room on the
  other side of the glass. Beyond it the country lay in squares, and an old man was asleep under a tree."
- Miniature: "A room with a fire in it and a glass over the fire no bigger than a thumbnail, and behind it a strip of country drawn in
  squares, with an old man asleep under a tree at the edge."
- Shared account: "%s {was} in the drawing-room too, in another copy of it."
- Subtitles: "Someone breathes in their sleep behind a door"; "The King breathes in"; "The King breathes out"; "Soft footsteps".
