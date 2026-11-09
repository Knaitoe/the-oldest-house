# Pool, darts and a bar (proposal)

The owner, 9 November 2026: "How would you feel about developing a pool mini-game and a darts mini-game and a vignette based in/around
the bar? The player could again bring the pool table and darts board to the world outside too, through unlockable means."

The owner's answers, the same day: "Well, other players should be able to play too?" and "my suggestion would be to again develop a
vignette where the horror is focused in the bar - the Last Stop is focused on the events outside, no?"

Decided:
- **Every reader plays.** In the bar any reader can take a cue or darts. At home only the crafting needs the keepsake's owner; a
  placed table or board is anyone's to play, as the home cabinet is.
- **A new vignette, with its horror inside the bar.** The Last Stop's horror is the elk outside. Pitch A, Last orders, is the one
  built.
- **When:** next, straight after the arcade cabinet (0.4.413), unless the owner moves it.

## What exists

The Last Stop roadhouse (ElkLot) already has a pool table (`RoadhouseBlock` POOL_L/POOL_R and its lamp) and a dartboard. Neither is a
game:
- using the table racks or breaks with a sound;
- using the board prints one of three lines about three darts and turns the board's picture.

## The two games

Both games are real and playable on their own. Both run on the server, so readers can play each other and bystanders see the same
game.

### Pool

- **What it is:** eight-ball, played on a top-down screen, as the arcade's cabinets are.
- **Shooting:** the reader aims the cue around the cue ball with the mouse, then holds to draw back and releases to strike. The
  harder the draw, the harder the shot. Spin is left for later.
- **The table:** sixteen balls, six pockets and cushions, with simple, readable physics: rolling friction, ball-on-ball and cushion
  bounces, and a pocket's jaws.
- **The rules, kept short:**
  - the break;
  - open table, then each player's group (spots or stripes);
  - the eight last;
  - fouls: scratch, or hitting the other group first, give the other player the cue ball in hand;
  - potting the eight early, or scratching on it, loses the frame.
- **Who plays:**
  - two readers, shot by shot;
  - a reader against a regular, who aims at the easiest pocket with a little error, more or less by how well they play;
  - doubles, two against two, taking turns.
- **Server authority:** a shot is only an angle and a power. The server plays it out on a fixed step, decides what went down, and
  sends every player at the table the balls' paths to animate. A shot out of turn, or from someone not at the table, is ignored.
- **In the world:** the table shows the balls where they lie, drawn by the table's block entity, so anyone standing round it can
  follow the frame.

### Darts

- **What it is:** thrown in first person at the board itself.
- **Throwing:**
  - Hold use to raise a dart. The aim settles for a moment, then starts to sway, more the longer it is held.
  - Crouching steadies it.
  - Releasing throws a real dart, which flies with a little drop and sticks where it lands.
- **Scoring:** the board works out ring and segment from the exact point the dart struck its face, in the standard number order
  round the board. That gives single, double, treble, outer bull and bull. A dart that misses sticks in the wall or falls.
- **Games:**
  - **301:** down to exactly nothing, with a bust over;
  - **Round the clock:** 1 to 20, then the bull, in order.
  - Each player throws three darts a turn, for one to four players. The darts are pulled out at the end of each turn.
- **The score:** chalked on a board beside it as the game goes, so everyone in the room can read it.

## Taking them home

This works the way the arcade cabinet does (0.4.413). Finishing the bar's story gives two keepsakes, each marked for that reader
alone:
- the house chalk, for the pool table;
- a tin of darts, for the dartboard.

At a crafting table each keepsake makes its object and comes back unchanged. Someone else's keepsake makes nothing. The mixin
already written for the service key becomes one rule for every keepsake that unlocks a recipe.

| Object | Made from | Out in the world |
|---|---|---|
| Pool table | the house chalk, planks, wool, iron | two blocks, plays the same eight-ball for any two readers (four for doubles), or one alone on a practice rack |
| Dartboard | the tin of darts, planks, string, a target block | on a wall, plays 301 and round the clock; the darts come with it |

Both are ordinary blocks: they break by hand and drop themselves. Anyone can play them, not only the reader who made them. They pay
nothing and keep their own scores, as the home cabinet does. The recipes go into the reader's recipe book with the keepsakes.

The Last Stop's own table and board would play the same two games against Russ, Denny and Wade, with no reward there. That costs
almost nothing once the games exist.

## The bar's story (three pitches; A chosen)

Each is an original story with original people and words. In each, horror comes first and the sorrow is in the aftermath.

### A. Last orders (chosen)

The owner, the same day: "try to draw inspiration from literary sources if you can find any".

**Sources.** All three are public domain. Only their shapes are taken, and the people, names and words are original, as always. No
title appears in the game.
- **W. C. Morrow, "Over an Absinthe Bottle" (1893).** The spine. In a San Francisco saloon, a starving young man is invited into a
  private booth by a pale stranger with a stone-cold hand and a roll of bills. They play dice over a bottle of absinthe for stakes
  that rise from a dollar to everything. Two men in the next booth talk about a robbery, and that reveals who the stranger is. The
  young man takes the winnings and walks out into a dream of feasting. At the end, detectives find both men dead in the booth,
  staring at each other across the stacked money, the empty bottle and the dice beside them.
- **Washington Irving, "Rip Van Winkle" (1819).** The regulars. A silent company plays its game in the hills without a word, and the
  flagon they share takes twenty years from the man who drinks with them.
- **Samuel Taylor Coleridge, "The Rime of the Ancient Mariner" (1798).** The last game. Two figures cast dice for the crew. "The game
  is done! I've won!" is said by the winner, who whistles three times.

**The story:**
- **The place:** a saloon-bar long after midnight. There are numbered booths down one wall, a pool table under one lamp and a
  dartboard at the far end. The clock over the door says ten to, all night.
- **The stranger in booth four (Morrow).** He is pale, with a face like badly cut stone, a hand as cold as one, and a roll of notes
  tied with string. He wants a game, pool or darts, the reader's choice. He stakes from the roll and the reader stakes nothing they
  own; he says the company is stake enough.
  - He loses the first frames, and each time he doubles what is on the table.
  - Pool is played on the real table and darts at the real board, so anyone in the room can watch.
- **The regulars at the bar (Irving).** They are a few people in old coats who play darts among themselves without a word and never
  miss. When the reader passes, one pushes a glass along the bar toward them.
  - Each glass the reader takes adds another week of league games to the chalkboard, in the reader's own handwriting, that the
    reader never played.
  - The window behind them turns to another season.
  - Refusing is always possible. The chalkboard and the window are the bar's, shared by everyone in it.
- **The next booth (Morrow's reveal).** Once the stakes are high, two drinkers in booth three talk without looking round. One night
  a man came in, won everything at that table, and never went home. Partway through, they stop describing the stranger and start
  describing whoever is playing him now.
- **The last frame (Coleridge).** Everything on the table is played for. If the stranger wins, he says it is done and he has won,
  and whistles three times.
- **Endings:**
  - **Walked out (resolved, survival):** win the last frame, leave the roll on the table and walk out of the bar. Behind the reader
    the stranger sits on, staring at the empty seat.
  - **Took the roll (failure):** win the last frame and pick the roll up. The street outside is a feast, every window lit and full of
    food, and the reader is somehow back at the bar, at ten to, with their pockets empty. They may come back another night and try
    again.
  - **Lost the last frame (failure):** the three whistles, and the evening is taken, as at the black cabinet.
- **The account:** the morning shift's note left under the till, in an original voice. It records what was in booth four at opening:
  - for a reader who walked out, one man staring at an empty seat and the money stacked;
  - its earlier pages, from nights the reader took the roll, record two at the table, staring at each other, one in the reader's
    clothes.
- **Multiplayer:**
  - The stranger plays one reader at a time at pool, or two readers together at doubles.
  - Darts with the regulars is open to everyone.
  - Each reader's resolution is theirs alone: they must have played the last frame and left the roll.
  - The glasses, the chalkboard and the window are shared.

### B. The wake

- **The place:** a wake held in the bar for a regular everyone agrees was there every night. His cue is in the rack, and his darts
  in a tin behind the counter.
- **What happens:** the reader is asked to play his usual opponent one last frame, from where the balls lay the night he stopped.
  When the lights dip, someone takes his shots: the cue ball moves on its own, and the chalk on his cue is fresh.
- **The ending:** finish his frame, win or lose. The mourners go home and the tin of darts is given to the reader.

### C. Doubles (built around several readers)

- **The place:** league night, doubles only. A reader who comes alone is partnered with someone the bar provides, who plays the
  way the reader plays.
- **What happens:** with two or more readers, the partners are each other, and the opposing pair is the bar's: they copy the
  readers' shots back at them a frame later.
- **The ending:** beat them before they stop copying and start leading.

A suits a reader alone and a group equally, and uses both games for the story. B is the quietest and saddest. C is the strongest for
multiplayer, but the weakest for a reader alone.

## Multiplayer

- The pool table takes two players, or four for doubles, and bystanders see every ball.
- Darts takes one to four players, and everyone sees where the darts stuck.
- The story's state is the bar's, shared: the clock, the chalkboard and the man at the table.
- Each reader's credit is their own, and only for the games they played.

## Tests (planned)

- **Pool:**
  - the same shot always ends the same way;
  - pocketing, cushions and the eight;
  - scratch and wrong-group fouls give ball in hand;
  - a shot out of turn is ignored;
  - two readers take turns, and bystanders get the paths.
- **Darts:**
  - the hit point gives the right ring and segment;
  - a bust in 301, and the order of round the clock;
  - three darts a turn, pulled out after.
- **The keepsakes:** given once to the reader who finished; each crafts its object only for its own reader and comes back; the
  home table and board play.
- **The story:** its endings, and the account.

## What changes

- **Witness:** a new vignette with a personal resolution adds one source, and the quota follows from the pool (`WitnessPool`).
- **Layout:** one new slot (layout 135).
- **Protocol:** one step, for the pool table's shot and path payloads and the darts' aim.
- New builds only.
