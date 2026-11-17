# The staircase's stone (0.4.657)

The owner, with the notes that led to 0.4.656: "Feel free to rework the texture of the staircase too."

Two textures cover most of what a reader sees on the great staircase: the stone of the well's walls, and the stone of the treads and
the first landings. Neither shape, block, collision nor sound changes. No Witness, layout or protocol change.

## What was wrong with them

- **The treads' stone** (`staircase_stone`) was a texture at eight times Minecraft's size, a hundred and twenty-eight pixels to a
  block, of fine noise like a photograph of slate. Beside every other block in the House it read as smooth and smeared, and it did
  not look drawn. It covers the treads and landings of the first hundred and twenty-eight blocks of the descent and of every fourth
  such stretch below, the causeway across the well, the cell and its walls, the way out below and the household rooms' floors.
- **The well's stone** (0.4.376) was laid two courses to a block, with the same two joints in the same places in every block. Up
  close it read as brick; across the well it made a fine regular grid that shimmered.

## Now

- **The well** is laid in courses a block high. Each block of a course is one of five stones, picked by the block state as the
  game picks among grass or stone variants: an upright joint six, eight or ten pixels in, or none, where the stone runs on into the
  next block. So the stones run to different lengths, a block and two thirds long on the average and never shorter than ten pixels,
  and no two stretches of wall are laid alike.
- **Every joint is two pixels wide** and starts on an even pixel, the bed joint along the foot of each course and the upright
  joints alike. Minecraft draws a wall at a distance from smaller copies of its texture, each pixel of one the average of two of the
  last; a joint a pixel wide is averaged into the stone beside it there, while one two wide on an even pixel stays whole. The first
  drawing had upright joints a pixel wide and level tooling across every third row: in the review frames the bed joints stayed and
  the upright ones were lost, and across the well the wall read as boarding.
- **A stone's face** is a few soft patches of tone, a little grain and two or three short tooling strokes slanting across it, never
  level stripes; it has a lit edge along its top, a little shadow above the bed joint, and beside an upright joint a shadow on its
  west side and a lit edge on its east. No stone is lighter or darker than the next, so a stone that runs on shows no seam where one
  block meets the next. The three bands keep their colours, each darker and colder than the one above: the middle band is stained
  down from its joints, and the lowest is wet, a sheen down its face and a crust of salt along its bed. The fluted piers, the
  balustrade and the bands' places down the well are as they were.
- **The treads** are stone flags at sixteen pixels, one to a block, the joint along the back and the west side of each, so that
  laid together they meet in a joint a pixel wide. The joint is only a little darker than the stone and the flags have no bevelled
  edges: the first drawing's joints were near black, with a lit and a shadowed edge to every flag, and in the frames the landings
  read as tiles in a grid. Each flag has a few soft patches of tone and a lighter, broken patch where feet have smoothed it, a
  different shape on each, and is a shade of its own, as cut stone is. The block picks among six flags: plain (four in thirteen), a
  shade deeper (three), a shade worn (two), cracked (one), chipped along its back edge (one) and flecked (two). The stairs and
  slabs cut from it pick among the three plain flags the same way, so a flight is not one flag repeated; a double slab picks among
  all six.
- All of it is drawn by `tools/draw_stair_well_art.py`, which writes the textures, the models and the block states and checks them
  (`--check`, run by CI and the quick checks). The stairs keep Minecraft's own turns for every facing, half and shape (checked equal
  to the turns their block state had). `tools/build_whale_hospital_assets.py`, which copies those turns for the night ward's stairs,
  takes the first of the three.
- The treads' stone was one of the textures kept exactly since 0.4.100. It supersedes only its own hash there
  (`art/inventory/staircase_stone_04657.json`), and `tools/verify_character_art.py` checks it is the one drawn and sixteen pixels
  square.

## Unchanged

The landings' banded tuff, deepslate and blackstone bricks below the first stretch (Minecraft's own), the entry hall's deepslate
tiles, the cell's bars and cracked wall, the fires and the camps. Every block in the staircase is the block it was: only how two
of them are drawn changes.

## Checked

- `draw_stair_well_art.py --check`; `flicker --check` (no model with faces that flicker or draw nothing); every block model
  resolves a particle; every texture decodes; `verify_character_art.py`. The stairs' turns are checked equal to the forty their
  block state had.
- Review frames at eye level on the stair, eleven of them, drawn with each block's stone picked by its place as the game picks it:
  a flight, its treads, its wall and a landing in the upper band, a landing across the well, a wall close to, the middle band's
  landing and flight, and the causeway, the foot of the stair and the floor before the gate below. The first drawing was looked at
  the same way, and redrawn for what those frames showed (the boarding and the tiles).
- Build, package, branding and quick checks; architecture, writing and the client proof.

## Not covered

- The frames are lit evenly. On the stair the only light is what a reader carries and the fires: the stone was looked at that way
  only in the frames' darker bands.

## New lines

None.
