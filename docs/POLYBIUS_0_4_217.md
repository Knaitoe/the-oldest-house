# Polybius (0.4.217)

The owner, 31 October: "vignette based on Polybius - real arcade games, real leaderboards per server?"

This is the build. It follows the urban legend:
- an arcade cabinet with no maker's name, in a suburban arcade in 1981;
- men in suits who came to read its scores and never played it;
- the children who played it, lost time, and stopped playing.

The games are original. None of them copies an existing arcade game.

**Slot 144. Literary, multi-visit. Witness: 68 sources / 51 required / two kinds / three endings.** The new story is `polybius`
("Polybius", survival).

Protocol 116. Layout 70 (the layout's column limit is raised from 18 to 22, so slot 144 fits at common manor heights; no earlier
slot moves). New builds only.

## The arcade

`PolybiusSite` builds one long room at the end of a strip mall: x -8 to 8, z 0 to -22, five high. It is built whole and is not
dressed by the polish passes. It holds:
- black carpet with scattered neon shapes;
- neon strips down both walls and across the back;
- posters for games the arcade doesn't have;
- ceiling lights;
- by the door: the change machine (a card taped over its slot says FREE PLAY TONIGHT) with the clipboard's place on top, and the
  notice board opposite (the source);
- down the sides: the three working cabinets with stools in front of them, and five taped-up dead ones;
- at the back on the right: the prize counter and its shelf;
- at the back in the middle, under the one dead light: the black cabinet;
- the fire door in the back wall, on the left: the way on.

One fitting block, `polybius_fitting`, with 14 kinds, is drawn by `tools/draw_polybius_art.py`. Every screen is animated in its
attract mode. The arcade's bleeps are vanilla's note blocks, so there are no new sounds.

## The source

The notice board holds a clipping from the local paper: GAME TAKEN OUT OF MALL ARCADE. It reports:
- two boys taken ill;
- a machine with no maker's name;
- two men in suits who came every week to read its scores.

Under the clipping is an index card in marker: "FREE PLAY TONIGHT / high scores stay up / if the black one is back DON'T".

## The three working games

Each working cabinet is a real game on the reader's own screen (`client/PolybiusScreen`). It is drawn on a 200 by 150 field of fat
pixels and played with the movement keys or arrows and the space bar:
- **Mothlight:** a moth round a porch lamp. Gather the motes of light drifting in. Bats cross the dark. The lamp pulls at the moth,
  and touching it burns. Three lives.
- **Undertow:** a swimmer in a black lake, under what the current brings down. Catch the bubbles coming up before the air runs out.
- **Signal:** a needle on a radio dial. Hold the space bar against the drift to keep it inside the station as the station wanders
  and narrows.

## The high-score tables

Each working game has a table kept for the whole server (`PolybiusScores`), not per reader:
- **The entries:** a reader holds one place on a table, their best. Ten places are kept.
- **Where it shows:** the top five hang over each cabinet as text, and the full table shows after each game.
- **What the server accepts.** A score only goes on the table when the server believes it:
  - it comes from a session the server opened;
  - it is no faster than the game can score in the time played.
- **One player at a time:** one reader plays a cabinet at a time.

## The black cabinet

The black cabinet's game is a spiral that turns slowly, and a dot that rides out along it. The reader turns the dot to keep it on
the line. Words come up in the middle for an instant: OBEY, NO THOUGHT, STAY, SLEEP, CONSUME, PLAY AGAIN, and the reader's own
name.

Nothing flashes. The colours drift slowly. A reader who hides lightning flashes sees the words held longer and fainter.

It keeps no table anyone can read. Its board over the cabinet is all question marks.

**The east wall (2 November).** The owner found the east wall cramped: the pizza counter stood a block from the regular at the
cabinet beside it, while the wall toward the door stood empty. That wall's three cabinets (Undertow and the two the regulars play)
now stand up toward the door, at z -4, -7 and -10, leaving three clear blocks before the counter. The stools follow their cabinets,
the east neon strips sit a block further back, clear of the high-score table, and the poster hangs in the new gap.

**Playing it (fixed on 2 November).** The owner asked whether Polybius worked. It did not. The dot only moved when a key was held,
while the line turned away under it, so it fell off within a few ticks unless D was held from the very first. After two times round,
no key could keep up. A play that ended that soon was under the 100 ticks a play needs to count, so the three plays never began.
Now:
- the dot rides out along the line by itself;
- after the first three seconds something starts pulling it off, a new way every two and a half seconds, eased into, and harder
  each time round;
- the pull is never more than a turn of the dot can answer;
- three seconds off the line ends the game.

Left alone, a play lasts long enough to count. Steered toward the line, the dot stays on it to the end. The screen's hint says "keep
it on the line". Test: `PolybiusTests#theBlackCabinetsDotRidesTheLineAndCanBeKeptOnItToTheEnd`.

**The men.** Two men in suits (new skins, stranger phases 41 and 42) stand either side of the black cabinet. They are the reader's
own: no one else sees them. They watch the reader, and the screen while the reader plays. After each play, one of them writes
something down. A play counts once it has lasted five seconds.

**Three plays.** After the third play, the cabinet calls the reader by name ("PLAY AGAIN, it says. It knows how to spell your
name."). The men watch the reader, not the machine.

**Success: walking away.** The reader walks to the front of the arcade without playing a fourth time. Then:
- the calling stops;
- the men are gone;
- their clipboard is left on the change machine by the door;
- the account is ready (`walked_away`).

**Failure: a fourth play.** The screen keeps the reader for six seconds, then takes the evening:
- they come to at the front, at closing time;
- their name is gone from every table, every place they held;
- the black cabinet is dark for the rest of that visit, and no account is ready.

Another night they may come back. The black cabinet starts again from nothing.

## The account

The account is the clipboard. Its first page is the men's carbon form, with no company name, only a number:
- SUBJECT, with the reader's name;
- SESSIONS;
- RESPONSE: stopped, or "stopped (second evening)" if a fourth play took one evening first;
- the reader's best on each working game, and their place on its table;
- at the bottom, in pen: "no further visits required".

The second page is the reader's own words: what they played, the spiral and the word that was theirs, the men who looked at them
and not the screen, and walking out with their name being called behind them.

## Literature

- **Witness line (survival):** "I stopped playing. There was a black machine at the back of an arcade with no name on it, and two
  men who wrote down how I did, and when it asked for me by name I walked away from it."
- **House of Leaves place:** "the arcade where the black machine was". The account's title is "The account: the arcade".
- **Co-op lines:** "at the next machine. Our names went up the same table, a place apart. Neither of us could see the men by the
  other one." / "still playing when I walked out."
- **Miniature:** a black cabinet the size of a matchbox with nothing written on it anywhere.
- **Advancement:** `account/polybius`, shown with a redstone lamp.

## Multiplayer

- The tables are the point: every reader who has played a game is on the same server-wide table.
- The men and the black cabinet's count are each reader's own.
- Two readers can play different cabinets at once.

## Tests

`PolybiusTests` (literary namespace), four cases:
- **The arcade's build:**
  - the cabinets, body and screen, and a table over each;
  - the source and the account's places;
  - the clipping's text;
  - the Witness pool.
- **The tables:**
  - one reader per cabinet;
  - a wrong token and an impossible score are refused;
  - one place each, best kept, in order;
  - no table for the black cabinet;
  - the board shows the top name;
  - the tables survive a reload.
- **Three plays and the walk:** the men appear; three plays; the call; the walk to the front readies `walked_away`; the men leave;
  the account names the reader and never genders them.
- **The fourth play:** it is the one the cabinet keeps; the evening is taken; the reader's places are struck; they come to at the
  front; another night starts again with the men back.

The architecture case checks the arcade's carpet, notice board and black cabinet, and exports its view.
