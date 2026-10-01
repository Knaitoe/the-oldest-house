# The Oldest House 0.4.10

NeoForge 1.21.1, Java 21. Install the same version on client and server (network protocol 12).

## The Witness

Three different personal resolutions earn another passage in the play at the great staircase's cell. The current five sources are:

| Story | Resolution |
| --- | --- |
| Tell-Tale Heart | Pry the real loose floorboard with an axe. |
| Hide-and-clap | Find and open the wardrobe before the blindfold deadline. |
| Mr. Harrigan | Complete the funeral choice, either burying his phone or leaving with it. |
| Model home | See the tree cross the child's window on its final visit. |
| Mother of Strays | Complete the peaceful resolution or personally examine its settled aftermath. |

Each source counts once. Three sources already provide different kinds of experience; the Mother is optional and two stories can be missed. A written account grows after each resolution and uses the existing authored handwriting. Copies and traded accounts never confer their owner's progress. Losing the book does not erase knowledge: crouch while speaking to the old man, or visit the cell's lectern, to recover a current account.

The shared rooms remain physically exhausted. Another player, or an explorer upgrading an older save, can still record a personal afterword by crouching and using the exposed floor beneath the missing board, the opened wardrobe, Harrigan's casket, or the model home's child's window. The dealer may offer these finished rooms until that explorer has inspected the actual ending prop. Arrival provides a short prompt. Visits, ordinary loot pickup, and global completion flags alone count for nothing.

At the cell:

1. Read the lectern after earning three resolutions.
2. Ask the old man to identify the original most-used weapon. Drop that original within six blocks of the cell. Put your shield and other held objects in your inventory.
3. Crouch and use the cell with both hands empty. An ordinary click still begins the existing fight. A failed release attempt leaves the cell closed and explains what is missing.
4. Stand aside. The Minotaur pauses, leaves the cell, passes close by, then walks along the real corridor and climbs the stairs. It has a quiet walking pose and no attack in this route. Its departure unseals the return passage.
5. Walk back up the physical staircase and cross the entrance vestibule. The ending returns you to the real manor doorstep with inventory intact and your surviving loaded owned companions. The original laid-down weapon is returned once; it is protected from ordinary pickup and expiry during the scene. Already-kept pets or items stay with the Mother.

The House remains. This player's entry is permanently closed, with no answering knock. Other players retain their journeys. The Overworld clock advances naturally; this ending does not add the collapse ending's sixty-day absence. A chest near home appears after one in-game day with the completed account, including an extra page written from inside the cell. Progress, reading, scene position, and ending survive reloads; the creature pauses while its owner is absent. Death during the release or return still invokes the finale defeat ending.

## Tell-Tale Heart correction

The room's loose board has an explicit axe-prying interaction before ordinary mining protection. **Attack or use the board with an axe.** The action raises the board, produces one caregiver's note, credits the explorer, and leaves the rest of the room protected. Repeated attack/break notifications cannot duplicate the note. Offhand use wears the correct axe.

An original mono Vorbis sound gives the floor a low double heartbeat, roughly 67 beats per minute when calm and 171 at the peak. Vibrations make it faster and louder. Crouched footsteps remain quiet. The source is beneath the specific board, and a closed door leaks a muffled beat. Completion immediately stops the sound for the room's listeners and silences the real room's door leak. The reusable source is `tools/generate_heartbeat.py`.

## Playtest commands

Use a copy of a world when testing endings: defeat permanently seals inventory and the successful endings exclude that player.

- `/oldesthouse finale prepare`, then `/oldesthouse finale go` when building is ready.
- `/oldesthouse finale cell` moves to the preparation side of the cell.
- `/oldesthouse finale witness ready` supplies three personal test resolutions and an account, without reading the play or selecting an ending.
- `/oldesthouse finale witness status` shows the saved account; `/oldesthouse finale status` shows saved ending and release progress.
- `/oldesthouse finale witness begin` uses the same reading, weapon, empty-hand, crouch, and proximity checks as the real cell.
- `/oldesthouse reset` clears the account and finale state for a fresh fixture; existing prototype blocks remain.

The game tests cover personal progress versus room flags and traded books, distinct resolution thresholds, save/reload, completed-room afterwords, heartbeat cadence and stopped leaks, cancelled native axe interactions, release reload, a physical Minotaur walk through the actual staircase geometry, weapon recovery, inventory and time preservation, and the personal terminal ending. Existing architecture, Mother, companion, opening, combat, and collapse tests remain in the suite.
