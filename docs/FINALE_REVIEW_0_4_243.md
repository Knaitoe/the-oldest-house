# The finale, reviewed branch by branch (0.4.243)

The owner asked for the finale code and its branches to be reviewed, to make sure each plays out as intended. This review traced
every phase in `FinaleController`, `FinaleCollapse`, `WitnessEnding`, `MinotaurEntity`, `MinotaurLoose`, `FinaleArchitecture` and
`StaircaseFire` against `docs/DESIGN_DOCUMENT.md` ("The finale and the endings") and `docs/CHARGE_CELL_LAPS_FIRES_0_4_84.md`.
No protocol, layout, Witness or source-kind change.

## What plays out as intended

- **The descent.** The reader commits to nothing until they open the cell:
  - The fires gate each flight in order.
  - Walking back out at the top returns the reader through the route they came by.
  - The old man is there, and so is the warning at the arena mouth.
- **The fight.** Opening the cell seals the stairs behind the reader:
  - The child walks behind the cell wall and changes only where no camera sees it.
  - A blocked charge costs two hearts and wears the shield, and the third block breaks it.
  - An unblocked charge kills.
  - Only the recorded original weapon, or a projectile carrying its provenance, wounds the Minotaur while it is stunned.
  - Commands and teleports cannot turn the fight into a free exit.
  - An owner who logs off leaves the claim paused; nobody else can open the cell while it stands.
- **Killed in the fight.** The reader:
  - wakes in the cell with nothing, their belongings and pets sealed with the Mother;
  - watches the same Minotaur turn away and go up into the stairs;
  - leaves by the one door, locked out for good.
  
  While anyone is kept, nobody else opens the cell. The door is there only while a kept reader is in the game.
- **Wounded.** It crawls back into its cell and stays alive. The collapse runs on the reader's own clock: the fractures, the breach, then Tom's last words. After that come:
  - the real shaft and the water landing;
  - the darkness, and light from burned paper;
  - the rubble to jump and the lintel to crouch under;
  - four pulls on the latch.
  
  A guide comes only if the Mother allows it. Without one, sixty mornings pass. The House becomes an empty lot, everyone inside is set outside, and anyone still kept is let out locked out.
- **The Witness.** The release needs the quota, the read passage, empty hands, the weapon laid down and a crouch. Then:
  - the creature waits, passes close and climbs the real stairs;
  - the reader walks all the way back up;
  - the weapon comes back once;
  - the account comes home a day later;
  - the House stays for everyone else.

## What did not, and is fixed here

- **The release spawned a second child.** `WitnessEnding.begin` never recorded which creature was in the cell. The next check (`FinaleController.ensureMinotaur`) found no prisoner on the reader's record and spawned a fresh released child at the same spot. That child walked out while the real prisoner stayed behind the bars, watching. The operator shortcut (`/ghosthouse test ending witness`) patched the record by hand afterwards, so its test never saw the real path.
  - `begin` now takes the creature actually in the cell (the saved prisoner, or one an earlier reader wounded) and releases that one.
  - The shortcut no longer patches anything, so its existing test (`PlaytestCommandTests#releaseFixtureStoresEquipmentAndPreservesTheSingleNativePrisoner`: same UUID, released, one live prisoner) now tests the real path.
  - Once it has gone up the stairs the cell no longer holds it, and the next reader finds a new prisoner.
- **A Minotaur loose on the stairs could be taken for the prisoner.** Only an unlikely save state reached this (the creature unloaded at its killer's death). A fight or release that picked it up would wait forever with nothing in the cell. Now the cell never adopts a loose one as its prisoner, and a fight or release given one finds or makes its own instead.
- **A Witness reader climbing home was stranded when another reader's escape brought the House down.** The climb is long, and the claim on the cell ends when the creature leaves, so another reader can fight, wound and escape meanwhile. When the House collapsed, the climber was set outside with their record still on the stairs: no ending, no account, and no way back to finish. `WitnessEnding.completeOnCollapse` (called from `FinaleController.finish`) now completes every such release:
  - the reader is `WITNESSED` and their account comes home a day later;
  - a weapon still lying by the cell goes into their pack, or waits at the door if they are away or full.
  
  Readers who never opened the cell get nothing.
- **A guide whose reader fell in the escape was left behind, and kept twice.** The Mother's companion is restored from her shelf for the escape and only released to its reader at the door. A reader who fell on the way left the restored animal frozen at the bottom while its record also stayed on her shelf. The guide now goes back to her with the rest of what a defeated reader leaves, sealed like the other companions.

## Questions for the owner

These are choices, not faults, and nothing here changes them:

- **A reader who is not ready makes the Witness gesture.** Crouching at the bars with empty hands opens the fight when the account is not yet ready, with the weapon perhaps already laid on the floor. Should an unready reader who crouches be told they are not ready instead?
- **After a wound, the next reader skips the fight.** If a reader wounds the Minotaur and then dies in the collapse or escape, the creature stays wounded in its cell. The next reader to open it goes straight into the collapse. Is that the intended trade?
- **The Witness after a wound.** With this fix, a release after someone else's wound frees the wounded creature itself: the adult walks out, not a child. The alternative is to refuse the release until the House is whole again.

## Smaller notes, left as they are

- A new prisoner appears in the cell without checking whether anyone is looking. It only happens after a cell has emptied and while someone is on the lower stairs.
- A caged child is removed at once if another living reader is watching when its opener dies before it has changed.
- If no air cell sits on solid ground within four blocks of the reader's bed, the epilogue chest looks for one every tick.
- Death in the finale keeps every loaded companion in the House, not only those nearby.
- At the very end, an exterior landing that is built over keeps the ending retryable without saying why.

## Tests

- `WitnessTests#aReaderClimbingHomeKeepsTheirEndingWhenAnotherEscapeBringsTheHouseDown`: two climbers (one present, one away) and one reader still descending.
- `WitnessTests#aGuideLentForTheEscapeGoesBackToHerWhenTheReaderFalls`: a real death in the escape.
- `PlaytestCommandTests#releaseFixtureStoresEquipmentAndPreservesTheSingleNativePrisoner` now runs without the shortcut's patch.
- Locally, the three cases above pass. Each of the first two fixes, undone, makes its case fail.
- These existing cases also pass locally with the fixes:
  - the finale cases;
  - the collapse walk;
  - the cell door;
  - the defeat and escape shortcuts;
  - the Witness walk, return and terminal-ending cases.
- This is pending CI.
