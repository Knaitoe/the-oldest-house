# Four of the owner's last notes (0.4.703)

The owner's notes of 18 November, after 1d of *The list*. This release answers four of them; the fifth (the finale and a kept item)
is 0.4.704 (`docs/FAVORITE_ITEM_0_4_704.md`).

> Get rid of monster sounds in clothing room vignette
> Increase frequency of passive kitchen room(shared space)
> Ensure player encounters Holloway prior to encountering staircase
> One player sleeping in the house triggers day phase. That player should just be synced in with the rest of the player sleeping count,
> if possible.

## The clothing room's monster sounds

The quiet rooms (the coatroom among them) stand in the game's own ground, in the House's dimension. Two things of the game's own were
heard there:

- the cave's mood sounds, the groans the dark brings on;
- monsters spawning in the caves round the room and heard through its walls.

Now:

- **No game monster** spawns within 32 blocks of any reader's quiet room (`LabyrinthSpawnRules.QUIET_MARGIN`, `HouseReadSites.nearRoom`),
  in either of the dimensions the rooms stand in.
- **In a quiet room the client hears none of the game's own** ambience, monsters or cave moods, nor its weather indoors
  (`HouseReadSites.quietens`, used by `HouseReadClient`). The porch, which is outdoors, keeps its weather. The rooms' own sounds are the
  mod's and are all kept, and so are doors, steps and the reader's own body.

## The kitchen

The shared kitchen (`FamiliarRoom`) comes up three times as often:

- its weight is 12 (was 4), as often as the shut-up rooms;
- after a visit it can come again after two crossings (was four), is back at its ordinary weight after six (was ten), and is twice as
  likely after fourteen (was twenty-four);
- with somebody already in it, it draws everyone else at 80 (was 40), still the strongest pull of any hall.

It is still never dealt before the third crossing, still three times as likely for a reader who is hungry or hurt, and still half as
likely in the deep halls.

## Holloway before the staircase

- **The staircase waits for Holloway.** `LabyrinthDealer.finaleOffered` now also needs the reader to have been to Holloway's camp, or to
  have no camp left to be dealt (gone from it, honest with him, or his story told: `metHolloway`). A staircase a reader has already
  found is still found again.
- **His camp comes to meet them.** From four crossings short of the staircase's depth (`HOLLOWAY_LEAD`, so from twenty), a reader who
  has not met him is owed his camp (`hollowayOwed`): one door of every hall they cross leads to it. Only a scent, a lost pet's rescue or
  a quiet room still owed comes before it.
- Nothing else about his camp changes. Each reader meets him on their own; one reader's visit does not stand for another's.

## A sleeper in the House

The game counts each dimension's sleepers on their own. A reader alone in a manor bed was the House's whole count, so the House passed
the night for everyone, whoever else was awake in the Overworld (`HouseDays` used to carry the House's morning to the Overworld).

Now the Overworld and the House keep one count (`HouseSleep`, `HouseSleepMixin`):

- **One count.** A reader in the House counts beside the Overworld's players, against the share that the `playersSleepingPercentage`
  rule asks for. Spectators do not count, as in the game.
- **Only the Overworld passes the night.** It does so when enough of both are asleep, and then wakes the House's sleepers with its own.
  The House never passes the night by itself.
- **One tally.** The "sleeping" tally the game shows goes to everyone in either, with the shared numbers.
- **Other places keep their own count.** The House's outdoor and in-between places are counted on their own, as the Nether is.
- `HouseDays.onSleepFinished` is retired.

## Single and multiplayer

- **Alone**, nothing changes. A reader sleeping in the manor still passes the night as before.
- **With others**, the House's sleeper now waits on the Overworld's players and they on the House's.
- The kitchen and Holloway's camp are dealt per reader, as before.
- The quiet rooms' quiet is each client's own.

## Made as

- `HouseReadSites.nearRoom`, `HouseReadSites.quietens`; `LabyrinthSpawnRules` (the quiet rooms' margin); `HouseReadClient.music`.
- `FamiliarRoom.WEIGHT/MEET/SOON/BACK/LONG`.
- `LabyrinthDealer.metHolloway/hollowayOwed/HOLLOWAY_LEAD`, `finaleOffered`, the story door in `deal`.
- `house/HouseSleep` (new), `mixin/HouseSleepMixin` and `mixin/HouseSleepAccess` (new, in `the_oldest_house.common.mixins.json`);
  `HouseDays` and `TheOldestHouse` lose the old handler.

## Tests

- **`HouseReadTests`**
  - `quietRoomsKeepOutTheGamesOwnMonstersAndCaveSounds` (new) covers the margin round a room, in the ground under it and not far below
    it, the porch outside, and which sounds are kept out and which kept.
  - The coatroom case also tries to let a zombie in and is refused.
- **`KitchenTests`**: the "back by degrees" weight follows the new numbers.
- **`FinaleTests`**
  - `hollowaysCampComesBeforeTheStaircase` (new):
    - he is not owed short of the approach and is owed from it;
    - one door of every hall leads to his camp and none to the staircase;
    - the staircase is still withheld at its own depth until the first visit to his camp, and offered after;
    - a reader whose story of him is told has met him.
  - The sealing case now visits his camp before it expects the staircase.
- **`HouseSleepTests.aSleeperInTheHouseCountsBesideTheOverworld`** (new, exploration):
  - which levels share the count;
  - how many must sleep;
  - a reader asleep in the House beside one awake in the Overworld does not pass the night, though the House's own count alone would
    have;
  - the spectator is not counted;
  - with both asleep the night can begin.

## Found and fixed in the making

- **Who is a spectator.** The shared count first asked each player whether they were a spectator. The test's stand-in players always
  answer no, so a watcher was counted. The count now reads the game mode the server keeps for each player, which is the same answer
  for a real player.

## Every new line

None. The tally is the game's own ("%s/%s players sleeping", "Sleeping through this night").
