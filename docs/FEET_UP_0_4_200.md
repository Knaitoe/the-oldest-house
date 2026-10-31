# Feet Up: 0.4.200

The owner asked for "a vignette focused on parkour/'obby' gameplay", then: "Anyway, go ahead and start work on that vignette next." The design is [FEET_UP_PROPOSAL.md](FEET_UP_PROPOSAL.md). This document records what was built and where it differs from the proposal.

## What it is

A family house on an ordinary evening, built whole in the interior dimension at slot 127. On the hall table, in a child's hand that pressed too hard, are the rules of the children's game: the floor is lava, all of it, and stairs are floor. You rest on bases. A touch sends you back to your base and counts. Three touches and you are out. If they start shouting, go up, not down. The attic is safe.

The reader plays the game from the big chair to the attic. The account is a notebook under the attic lantern, and reading it resolves the vignette. It is a new Witness source of the survival kind, `feet_up`, titled "Feet up". The pool is now 56 eligible sources; 75% of 56 is 42, so the quota stays at 42, across two kinds, with three endings.

## The house

`FeetUpRooms.build` builds the house whole. The scene passes (polish, review, playtest repairs) leave it alone, like the cottage, the dare and the lake house. The footprint is x -10..10 and z -26..0, with three standing levels: the ground floor (floor at -1, air 0..4), the first floor (boards at 5, air 6..9) and the attic (boards at 10, air 11..13, roof at 14).

- **The hall.** Tiles, the bench, the table with the rules page, a stool, and the stair at the back. The parlour door in the west wall stays shut on the adults' voices until a reader is out.
- **The front room.** The big chair (base one), the footstool, the sofa, the bookcase, the mantel over the real fire, and a floral armchair that comes closer while nobody looks.
- **The dining room.** The long table under the ceiling fan, the sideboard (base two), and a cane chair that comes along the wall.
- **The kitchen.** The counters along the east wall, the stove (its top burns a foot), the fridge, six pots on a rail at a hand's width each, and the dumbwaiter's cage in the wall.
- **The landing.** The cage opens onto it. The trunk, the console, a chair and the window seat under the back window (base three).
- **The rail.** Fence tops along the void over the stair, from the landing to the upstairs hall. The chalk says to sneak on it.
- **The long way.** For anyone who will not take the rail: the children's room with its bouncing bed (base four), the bath, and the parents' room. Their floors are loose boards too.
- **The upstairs hall.** Loose boards, the linen press (base five) and, above it, the hatch.
- **The attic.** Blankets, the lantern with nothing in it, the notebook, the tally on the gable and the way on at the north gable.

Ten chalk marks on the walls give the game's lines once each, in grey italics, when a reader first sees them close up.

## The rules in play

`FeetUp` runs the game on the server tick, with each reader's state kept in their own record.

- **The start.** The game is on from the big chair. It is also on from the first thing a reader climbs onto instead, so nobody walks up the stair to the attic without playing. Stepping onto the stair counts as a touch at once.
- **The count.** A foot on any floor while the game is on (the boards and tiles downstairs, the stair, a fallen board) is counted in a whisper: *One.*, *Two.* After a short beat and a dip to black the reader is back on their base.
- **Out.** The third touch is *Out.*: the parlour door opens, the lamps go down, footsteps come to the stair, and after five seconds the reader is at the front door with the count at nothing. A whisper from under the stair then says where it went wrong (see below).
- **Bases.** Reaching the next base on the route moves a reader's own put-back point there.
- **The chairs.** Each helping chair moves one cell to its near place after a second in which no camera in the level sees it, spectators included. It never moves past that place. An empty house puts both back.
- **The boards.** A loose board that carries a reader for half a second falls as a block into the room below and lies there as floor. It comes back only when nobody can see its gap, and the debris goes the same way.
- **The fan.** Its four blades turn with the room's clock. A reader standing tall on the table in a blade's path is knocked off it. A reader who keeps low passes under.
- **The dumbwaiter.** A hand on the cage pulls its rope. It climbs a step at a time to the landing with whoever stands in it. Left empty for two seconds, it comes back down, and a pull on the rope while it stands empty at the top calls it down at once.
- **The hatch.** Its catch can be reached only from the top of the press. The hatch opens and its ladder comes down the wall.
- **The dark.** Every ninety occupied seconds the voices below peak in a shout and the lamps go out for two seconds. Nothing else changes.
- **The attic.** Standing on the attic boards with the game on ends it. The reader's tally keeps their fewest touches and whether they ever came up clean, and the account is ready. The rules say to pull the ladder up after; see the ladder below.
- **An empty house** is set for the next game: chairs back, boards whole, cage down, hatch shut and its ladder up, lamps lit, the parlour door shut.

## Winning and losing

Feet Up is a contest in the sense of [SUCCESS_AND_FAILURE_AUDIT.md](SUCCESS_AND_FAILURE_AUDIT.md): a real way to win and a real way to lose, each a trade. Neither haunts the reader home; the story is a game, not something that follows.

| | Gives | Takes |
|---|---|---|
| **Win: the attic** | The account in the notebook and, once it is read, the Witness credit (survival). The tally keeps the fewest touches and a clean run. | Nothing. The rules said the attic was safe. |
| **Fail: out** | A whisper from under the stair about where the reader last stood off the floor before the third touch: the chair that comes, keeping low under the fan, the pots and the rope, the rail or the long way, the boards, the bed. | The attempt: the count, the base reached, the way up. The reader starts again from the front door. |

- **Failing resolves nothing.** No credit, no account; the reader can climb onto the big chair again at once, or come back another visit.
- **Leaving is allowed.** An unfinished visit may retreat by the front door; the attempt is simply dropped, and the next arrival starts a new one.
- **The floor is the penalty, not the fall.** Falls of up to eight blocks inside the house do no harm; they count instead. The stove's top and the fire still burn, and a death follows the House's ordinary rules.
- **After winning** the house is a passage for that reader: the floor counts nothing, and they may open the hatch from above to go back down.

## Together

The house is one house for everyone in it; the count, the bases, the whisper and the outcome are each reader's own. Friends go in together (the scene is followable), and each keeps off the floor for themselves: nobody is credited for another's climb.

- **The chairs and the boards are shared.** A friend who stands and watches freezes the chairs and keeps a fallen board's gap open; a friend who looks away lets them come. Two readers on one loose board both come down with it, and both accounts say so.
- **One reader put out darkens the house for everyone.** While anyone is out, the parlour door stands open and the lamps are down for all. The moment nobody is out (put back at the front door, or gone from the house) the lamps come back. Everyone else's count is untouched.
- **One hatch.** Whoever reaches the catch from the press opens it for everyone. A reader who arrives at the press with the ladder already down climbs it; their account says someone had gone up before them.
- **The ladder.** The first up with anyone else still in the house and unfinished leaves the ladder down; their account says so: "Someone was still counting below me, so I left the ladder down. The rules had not said what to do about that." The last one up pulls it up, unless someone is on it, and their account ends: "Then I pulled the ladder up after me, the way the rules said."
- **The cage** carries whoever stands in it; anyone may call it down when it stands empty at the top.
- **Closing lines.** Each reader's Witness account closes on their party (`CoopAccounts`, as for the other scenes everyone comes through for themselves):
  - those who came through first: "*names* had reached the attic before me. Their marks were already on the rafter beside the empty column."
  - those still in it: "*names* was/were still on the furniture below when I reached the attic, so I left the ladder down."
- **Spectators** never play and never count, but their cameras freeze the chairs and the boards like anyone's.

## Literature

All House writing, so it never genders the reader and never names a colour or an age.

- **The source** is the rules page, in the child hand (`FeetUpTexts.source`).
- **The account**, "The account: feet up", is built from the run: whether the rules were read, how many times the reader was put out (and that the one who kept the count whispered where it went wrong), how many touches the floor had and whether the boards went under them, which way they went up, whether they opened the hatch or found the ladder already down, and whether they left the ladder down for someone below or pulled it up after them.
- **The Witness line**: "I kept off the floor all the way to the attic. The chairs came while I was not looking; the boards went while I was. Three touches would have put me out, and the lamps would have gone down. I pulled the ladder up after me."
- **The hint**, given once after the source: "Start on the big chair and keep off the floor to the attic. The bases are rests; the third touch puts you out at the front door."
- The staircase prose names the place "the house where the floor was lava". The miniatures room has a doll's house with its front off. The other hand may write after this room, as after the other survival rooms.

## Art and sound

`tools/draw_feet_up_art.py` draws, in native 16-pixel textures from fixed seeds, the nine materials and every fitting: the loose board, the ticking mattress, the pots and their rail, the cage, the window seat, the counters, the stove, the fridge, the press, three lamps lit and out, the bath's two ends, the rules page, the notebook, the chalk's four words, the tally, the blankets and the cobwebs. It writes 50 textures, the models and the block states, and `--check` verifies them in CI.

`tools/generate_feet_up_audio.py` makes three original cues with no words: the voices below raised, the count's knock, and a board giving way. `--check` verifies them against `art/feet_up_audio200.json`.

## Differences from the proposal

- **One fitting block.** `feet_up_fitting` carries every fitting by kind and stage. The furniture is the House's own household set.
- **The rail is the stair's east fence line.** The banister with missing balusters became a fence-top line along the void, with the long way round as the easier line.
- **The chairs move one cell, not across the room.** Each has one far and one near place.
- **No keepsake yet.** The chalk stub that forgives short falls is left for later, as the proposal's open question five suggested. Inside the house, short falls already do no harm.
- **No second stage yet.** The house later, with no floors, is left for later.
- **Tests run in the literary namespace** rather than their own.

## Tests

`FeetUpTests` (namespace `the_oldest_house_literary`) has nine cases:

1. The house stands, and every step of the route and the long way is a footing with room to stand (room to crouch under the fan), within a walking jump, with no sprint needed.
2. The game starts on the big chair, a foot on the floor counts and puts the reader back, and three put them out at the front door with the house set back.
3. The chairs come only while no camera sees them, and an empty house puts them back.
4. A loose board goes under a reader, lies below as floor, and comes back only unseen.
5. The fan knocks a standing reader off the table and spares one who keeps low.
6. The dumbwaiter climbs when its rope is pulled, carries its rider and comes back down empty.
7. The hatch opens only from the press, the attic ends the game, and reading the account resolves it for that reader alone.
8. Two readers: the first up leaves the ladder down for the friend still below, the last pulls it up, each account says which, the closing lines name the party, and a finished reader walks the floor uncounted.
9. Two readers: one put out darkens the house and opens the parlour door for both, only while they are out; leaving mid-sequence relights it, and the friend's count and base are untouched.

`CoopTests.everySharedStoryHasAnAccountForBeingThereWithOthers` covers the closing lines with every other shared scene's. `AdvancementTests` counts the new account advancement.

The architecture test checks the hall tiles and the rules page as the scene's discovery surface, and exports three views of the house: the ground floor (`feet_up`), the first floor (`feet_up_upstairs`) and the attic (`feet_up_attic`).

No migration: new builds only. Layout version 57.
