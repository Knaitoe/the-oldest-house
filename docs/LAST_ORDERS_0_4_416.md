# Last orders (0.4.416)

The third part of Last orders (docs/BAR_GAMES_PROPOSAL.md, pitch A): the bar itself, a new vignette whose horror is inside the bar. Its
pool table and dartboard are the real games from 0.4.414 and 0.4.415, so everyone in the bar can play and everyone can watch. The
keepsakes that let a reader make a table and a board at home come in 0.4.417.

The owner asked for the story to draw on literary sources. Three public-domain stories give its shapes, and nothing else is taken from
them; the people, names and words are original, and no title appears in the game:

- **A saloon booth where a pale stranger plays a hungry young man for a roll of notes, with the stakes doubling (1893).** The spine:
  the man in booth four, the doubling, the next booth's talk, the feast and the two found staring at each other in the morning.
- **A silent company in the hills whose drink takes twenty years (1819).** The regulars, who play darts without a word, and Benny's
  glasses.
- **Two figures who cast dice for a crew, and the winner's three whistles (1798).** The last frame.

## The place

An interior slot, 25 blocks across and 32 deep: a saloon bar long after midnight. From the door, looking north:

- **The front.** The house rules on a corkboard by the door (the source). A clock over the door says ten to, and the second hand does not
  move. The street door, the way on, is in the west wall by the front.
- **The bar.** The counter runs down the east side, with stools, taps, glasses and the till at its near end (where the account waits).
  The back bar has shelves and mirrors, and a beer sign above it.
- **The booths.** Five, numbered, down the west wall, each with a table and two benches and a candle, with high partitions between them.
  Booth four is the stranger's.
- **The pool table.** In the middle of the floor, under its own lamp, with the cue rack on the wall.
- **The board.** On the far wall, with the oche marked in the floor and the league board beside it, eight weeks of results chalked on it.
- **The window.** In the east wall past the end of the bar, three panes onto a scrap of street: a tree, the ground, the sky, and daylight
  whatever the hour inside. It is in one season or another.
- The floor is bare dark boards. The bar is dark between its few low lamps.

## The people

All seven are the bar's, shared by everyone in it, on stranger phases 139 to 145 with painted skins.

- **The man in booth four.** Pale, in a good dark coat, sat facing the door with his roll of notes. He is chalked up as "Booth four".
- **Ferris and Mags**, in booth three with their backs to booth four, leaning in over their table.
- **Hob, Ott and Cass**, the regulars at the board, in old coats. They never say a word.
- **Benny**, on the far stool at the bar.

## The match

- **Asking for a game.** Talking to the man in booth four offers pool or darts. The reader takes a place at the table or the board; he
  takes the other side and walks over to play.
  - **Pool:** eight-ball on the real table. Every reader who joins before the break plays on the reader's side; he plays the other side
    alone.
  - **Darts:** legs of a hundred and one at the real board. Readers who chalk up before the first dart play too, and the regulars give
    up the board for it.
- **The stakes.** He puts up the money; the reader stakes nothing. One note on the table in booth four for the first frame. Each frame
  after, he doubles it, whoever won: two notes for the second, then his whole roll for the last.
- **How he plays.** Badly in the first two frames. Well in the last: his pool aim strays far less and his darts barely wander.
- **The next booth.** Once the stakes are high, Ferris and Mags talk, a line every few seconds, without looking round: about somebody who
  sat at that table one night, won everything off him and never went home. Partway through, what they describe is the reader who asked
  for the game, from what the House has really written down: how many times the reader has died, a companion it saw them lose, a story
  they walked out of before the end, the glasses they took off Benny, and finally their name. Talked to, they say they weren't talking
  about you.
- **If he wins the last frame:** "It's done. I've won." He whistles three times, and the evening is taken from everyone who played it:
  the lamps go down, and they are back at the door, at ten to, with no memory of the rest of the evening.
- **If the readers win it:** he walks back to his booth, lays his hands flat on the table beside his roll and tells them to take it.
  - **A winner who takes it** (using the table in booth four) walks out into a street where every window is lit and every table laid,
    and eats; then they are back at the door at ten to with their pockets empty, and he is counting his roll again.
  - **A winner who leaves the roll on the table and walks out of the bar** has the account. Behind them nobody calls them back, and he
    sits on staring at the empty seat across from him.
- **Leaving.** If nobody is left playing him, at the table or the board, he says "Another time" and goes back to his booth.

## Around it

- **The regulars' darts.** Hob, Ott and Cass play 301 among themselves, never missing much, whoever's throw it is stepping up to the
  oche. A reader who uses the board during their game is put down for the next one, and goes on the chalk before them; a reader at an
  empty board has the regulars make up the numbers. Talked to, they say nothing.
- **Benny's glasses.** Whoever comes along the bar has a glass pushed toward them on the counter: "Go on. Steadies the hand." Only the
  one it was poured for can pick it up; left half a minute, it is taken back ("Suit yourself"), and another comes later.
  - **Drunk in the bar,** each glass puts another week on the league board under the reader's name, against a regular they never played,
    and turns the street outside the window to the next season. The league board and the window are the bar's, shared.
  - **It takes the edge off him:** each glass a player in the match has drunk on this visit makes the man in booth four's last frame a
    little worse.
- The house rules say it plainly enough: "What is won at the tables stays on the tables," and "Don't drink with the regulars unless
  you're stopping."

## The account

The morning shift's note under the till, in its own voice. It records what was in booth four at opening on the nights this reader was
there:

- for a reader who walked out: the old one, sat staring at the seat across from him as if somebody was still in it, every note stacked in
  front of him, untouched;
- for nights the reader took the roll: two of them, sat across from each other, staring, neither breathing, one in the coat of whoever was
  playing him at closing;
- a wiped league board for each lost evening, and the weeks chalked under the reader's name;
- and that the clock is still stuck at ten to.

## Multiplayer

- The bar is shared: its people, its games, the league board and the window.
- Each reader's ending is their own. A reader must have played the last frame and won it, left the roll and walked out. One winner can
  take the roll and another walk out, and only the one who walked out has the account.
- The glasses are personal: each is poured for one reader.

## What is new

- **Place:** LAST_ORDERS appends at slot 237 (layout 135). LastOrdersSite builds it whole; LastOrders is its controller and
  LastOrdersTexts its words.
- **Games:** PoolTables gains a table played against its own player (every reader on one side), another frame for the same players,
  and taking a table's game away; DartBoards gains shorter legs, another game for the same players, and taking a board's game away.
  `DartsGame` takes a starting score.
- **Sound:** one subtitled sound, "Someone whistles" (`orders.whistle`), synthesized by `tools/generate_bar_audio.py`.
- **Skins:** seven painted by `tools/paint_character_skins.py` (`literary_orders_*`), with their poses in the cast renderer: throwing,
  staring, bent over a cue, whistling, leaning in, hands flat on the table.
- **Witness:** one survival source, "Last orders": 86 / 65 / 63 across two kinds; three endings.
- **Tests:** three cases in the literary namespace (`LastOrdersTests`):
  - `aFrameForEverythingWonAndLeftOnTheTable`: the bar built whole; a pool match with a second reader on the reader's side; the stakes
    doubling, the next booth talking, the last frame played well; one winner walks out and has the account, the other takes the roll and
    is back at the door with none;
  - `aGlassFromBennyAndALegOfDartsLost`: a glass pushed along the bar, drunk, a week on the league board under the reader's name and the
    window in the next season; darts against booth four alone; the last leg lost, the whistles, the evening taken, the regulars given
    the board back;
  - `theRegularsNextGameTakesAReaderFirst`: a reader waiting for the regulars' next game goes on the chalk first.
- Architecture: the bar lays no rug, only its bare boards.

New builds only. Protocol 170 is unchanged.

## 0.4.441: the table stands in the bar

The owner: the pool table "is not physically in last_orders". In play the House builds a scene from a recorded plan, laid a slice at a
time. `PoolTableBlock.place` wrote the table straight into the level while that plan was still being recorded, so it went in first and
the plan's own clearing of the room took it out again; the literary fixtures build rooms directly, so their checks always found it. It
now goes through `BuildBlocks` and is laid in its turn, after the room and its floor. Borrasca's bar places its table the same way and
had lost it the same way.

`HallwayCarveTests.slicedBarKeepsItsPoolTable` builds the bar from a recorded plan in slices: recording writes nothing (the old code
failed here), and the finished bar has the whole table, its frame at the foot, and the dartboard.

