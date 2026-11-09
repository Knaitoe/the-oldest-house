# Pool, darts and a bar (proposal)

The owner, 9 November 2026: "How would you feel about developing a pool mini-game and a darts mini-game and a vignette based in/around
the bar? The player could again bring the pool table and darts board to the world outside too, through unlockable means."

Design only. Nothing here is built yet. Two choices are the owner's:
- **which bar:** a new vignette (recommended below) or the Last Stop reworked around the games;
- **when:** straight after the arcade cabinet (0.4.413), before The Ones Already Inside (recommended, since it reuses the cabinet's
  unlock and crafting code), or after A Little Longer.

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
| Pool table | the house chalk, planks, wool, iron | two blocks, plays the same eight-ball (two readers, or one against the table's practice rack) |
| Dartboard | the tin of darts, planks, string, a target block | on a wall, plays 301 and round the clock; the darts come with it |

Both are ordinary blocks: they break by hand and drop themselves. They pay nothing and keep their own scores, as the home cabinet
does. The recipes go into the reader's recipe book with the keepsakes.

The Last Stop's own table and board would play the same two games against Russ, Denny and Wade, with no reward there. That costs
almost nothing once the games exist.

## The bar's story (three pitches; one recommended)

Each is an original story with original people and words. In each, horror comes first and the sorrow is in the aftermath.

### A. Last orders (recommended)

- **The place:** a dive bar at closing time that does not close. The clock over the door says ten to, all night.
- **The chalkboard:** the league board has the reader's name on it, in the reader's own handwriting, with a season of scores. The
  regulars greet the reader as one of them and remember games the reader never played.
- **The man at the table:** one regular has been on the same frame of eight-ball since the last night the bar really closed, and
  will not let it end. Each rack the reader plays against him, the bar is a little later and a little emptier. More of "the
  reader's" games appear on the chalkboard, dated on nights the reader was somewhere else.
- **The darts:** a round-the-clock game on the board counts the hours. Hitting the bull at the end is the only way anyone has ever
  seen the clock move.
- **Endings:**
  - win his frame, and he puts his coat on and goes out the front door into a street that was not there before;
  - throw the frame on purpose, and the reader's name stays on the chalkboard for the next one;
  - walk out mid-frame.
- **The account:** the league sheet, with every game "the reader" played.

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
