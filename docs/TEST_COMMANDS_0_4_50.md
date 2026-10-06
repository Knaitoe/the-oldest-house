# Operator testing commands — 0.4.50

Run `/oldesthouse test` for help. These require operator permission (level 2), as do the existing House commands. Player commands affect the executing player; from the console use `/execute as <player> run oldesthouse test ...`.

| Command | What it tests |
| --- | --- |
| `/oldesthouse test vignette <id>` | Places a normal registered door two blocks ahead, leading to the selected authored scene. Tab lists all room IDs, including hazard rooms. The existing `/oldesthouse door <id>` remains available. |
| `/oldesthouse test vignette remove` | Removes the nearest testing door within eight blocks. |
| `/oldesthouse test minotaur` | Teleports to the preparation side of the boy/Minotaur's closed cell. `/oldesthouse test boy` is equivalent. No fight begins. |
| `/oldesthouse test ending defeat` | Opens and commits the actual finale, then invokes native death. Respawn follows the real defeat route: permanent personal exclusion and sealed inventory/cursor custody. |
| `/oldesthouse test ending escape` | Commits the actual finale and wounds its existing creature, starting the collapse clock at its first beat. `/oldesthouse test ending collapse` is equivalent. Take the real breach, fall and escape route; this does not skip to the exit. |
| `/oldesthouse test ending witness` | Stages a personal operator account and play reading, lays the carried original weapon by the cell, stores held equipment in the inventory, and starts the actual peaceful release. Stand aside, then return up the staircase. |
| `/oldesthouse test cancel` | Cancels a shortcut still waiting for preparation/loading. It cannot undo a committed ending. |

**Use a copy of the world for endings.** Defeat permanently keeps the selected player's belongings. Finishing collapse removes the shared House and evacuates its residents; an unguided escape also advances the shared Overworld clock by sixty days. Peaceful release permanently excludes its reader while keeping the House. These commands start those existing mechanics; they do not create preview outcomes or reset completed endings.

Spawn the House first. Cell and ending shortcuts request existing asynchronous staircase preparation, then hold native chunk tickets until both blocks and entities are ready. They never force a synchronous timeout load. A request cancels on logout, native player replacement, dimension change, House relocation, an intervening commitment, or after sixty seconds of waiting. Tickets release after completion, cancellation or shutdown. A completed build can be retried after a preparation timeout.

The shared finale's owner remains authoritative, including while offline. Commands cannot steal the chamber, clear another player's progress, or bypass terminal exclusion. Saved actor identities load before movement; a shortcut never creates a duplicate prisoner. Game mode, existing original words/components, personal returns and companion records remain. An explicit operator cell visit permits inspection in the chamber without granting burned leaves or fires; ordinary staircase gates resume when that player leaves the chamber.

Witness staging is deliberately an operator fixture: it records missing personal resolutions as `operator_fixture` and marks the play read for that reader only. Normal release still checks genuine progress, reading, crouching, empty hands and the original weapon. If weapon history identifies an original that is neither carried nor already laid by the cell, retrieve it first; no replacement is fabricated. If held equipment cannot be stored, the shortcut refuses before moving the player or changing the account. The exact laid original and stored shield retain their names, wear, identity and other components. Peaceful release must be tested before the creature is wounded in that world.

Escape uses existing Mother's custody and kindness records to determine whether a real pet guide returns. It invents no companion or kindness history. The Red Room still needs a captured personal room (`/oldesthouse vignette red_room capture`); completed/shared story state and finite inventories are not refilled by testing doors. For new versions of a completed scene, use a fresh world copy.

Layout 35, protocol 34, forty-three eligible Witness sources, thirty-three required resolutions across two kinds, and three endings remain. Verification requires the complete 395 declared native cases, including seven operator command cases, all focused suites and existing writing, package, native rendering and real socket multiplayer proofs.
