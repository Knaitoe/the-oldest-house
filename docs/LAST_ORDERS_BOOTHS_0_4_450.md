# Last orders: the booths and the league slate (0.4.450)

The owner's notes this answers (docs/BORRASCA_PLAYTEST_2026_11_10.md):

- 25, of the booths: "Custom booths in last order"
- 26, of the league table: "Custom signs for last orders"

## What is in the bar now

### The league slate

On the far wall west of the dartboard, where nine of the game's wooden signs hung, there is a slate four blocks by three in an oak
frame. LEAGUE is chalked across its top course. Under it eight squares are ruled, two rows of four, with a stub of chalk and a
duster on the tray along the bottom.

Each square is one block, and the game chalks a week into it in its own letters, laid flat on the slate: the week in yellow chalk,
the name in white, and who they are down as beating in grey. What is chalked is what the signs said: the newest eight weeks, the
newest at the right of the upper row, and a glass drunk in the bar still puts another week up under the reader's name. A long name
is drawn narrower to fit its square; it used to be cut at fifteen letters.

A lamp hangs in front of the slate so that it can be read. The signs were dark wood with dark lettering in the dimmest corner of
the room.

### The booths

The seats and tables are as they were. What stood between and around them was planks, fence posts, a wooden sign with a small
numeral on it and one of the game's candles. Now:

- **Between booths**, a panelled lower course in dark stained oak, and over it a screen of reeded glass in a frame with a brass
  rail along its top.
- **At the aisle**, each partition ends in a square post with a cap and a brass knob. The booth's number is on a brass plate on the
  post at eye level, and reads from the bar across the room. The partition past booth five ends in a bare post.
- **On each table**, against the wall, a candle in red glass. The game's candle stood in the air four pixels over the table, whose
  top is twelve pixels up; this one stands on it, clear of the napkins and the sauce. It burns a little brighter (6 where a single
  candle is 3).
- **On each booth's wall**, its own small picture in a frame: a horse, a ship, the team, a hill, and a brewery's mirror.

Everything is sixteen pixels to the block.

## How it is made

- `bar_slate` (`BarSlateBlock`, `BarSlateEntity`, `client/BarSlateRenderer`): twelve blocks, each showing one square of a single
  64 by 48 picture, hung a pixel thick on the wall behind them. Every block keeps three short lines. `BarSlateBlock.chalk` writes
  them, through the build plan when one is being recorded; `LastOrders.league` calls it for each of the eight squares.
- `bar_booth` (`BarBoothBlock`): kinds panel, screen, post, candle and picture, a number from 0 to 5 and a facing. A partition's
  facing is the way its aisle end looks; a picture's is the way it looks out from its wall.
- `tools/draw_bar_art.py` draws the 17 new textures and writes the 26 models and both block states. Its `--check` already runs in
  the quick gate.
- No layout, protocol or Witness change. New builds only.

## Tests and proof

- `LastOrdersTests`, first case: every partition's panels and screens, each post and its number, the bare post, each candle and its
  light, each booth's picture and the wall behind it, the slate's twelve blocks in order with their entities, the lamp, and that
  none of the game's own signs or candles is left anywhere in the bar.
- `LastOrdersTests`, the glass case: the regulars' eight weeks read off the slate by name and opponent, then the reader's week in
  the first square, then the earlier weeks moved along one.
- `HallwayCarveTests`, the bar built in slices: the slate's squares keep their entities and the posts their numbers.
- The built bar was looked at in first-person renders from the door, the aisle, a seat, the bar and the far end (a local review
  aid; not part of CI).
