# Branches brought in (0.4.712)

The owner, 18 November: "Can you review branches you didn't push and ensure they're all integrated with your most recent branch?"

Every branch on the repository was checked against `feature/vignette-environment-audit-0.4.125` at 0.4.711. Of 117 branches, 94
were already contained in it. The other 23 were read one by one. Two held work that was nowhere in this branch and are merged here;
the rest are already in it under other commits, or were never meant to be merged.

## Merged

- **`feature/hallway-variants`** (the owner, 17 November, `e79219f2`): twelve hallway variants across the House, recorded as 0.4.685 in
  `docs/HALLWAY_VARIANTS_0_4_685.md`. The branch gave them slots 246 to 257; slot 246 had since gone to *The list*, so they stand at 247
  to 258, in the same order. Nothing else in them changed. `tools/draw_hallway_variants.py --check` joins the quick checks, and
  `HallwayVariantsTests` (nineteen cases) the exploration pass, whose limit goes from eight minutes to fourteen to hold them.
- **`dev/tender-hypatia-kqigkl`** (another session, 17 to 18 November, `09d213b2`): *Neighbours*, the House beside other mods,
  first *A Reaping*; recorded as 0.4.675 in `docs/NEIGHBOURS_0_4_675.md` and `docs/NEIGHBOURS.md`. Fifteen commits, merged whole.

### Where the two lines met

- **The hall drawers.** This branch's 0.4.704 (the owner's fifth note: "items should only be lost on entering the impossible
  hallway. Not the house itself") takes nothing at the front door: a reader's own things are theirs in the manor and go to the drawer
  only on crossing into the rest of the House. *Neighbours* still took them at the front door, and added that the House's own stamps
  alone do not make a thing House-made, that what the House gave back is matched exactly before it is matched loosely, and that the
  reader's favourite weapon passes as their own (the owner, 17 November: "Yes, to favorite weapon"). Both stand: nothing is taken at
  the front door; at the crossing into the rest of the House the favourite weapon passes and everything else of the reader's own goes
  to the drawer; the matching and the stamps are *Neighbours'*. The hall stand that keeps one thing for the cell is unchanged.
- **The weapon history.** *Neighbours* stopped seeding a reader's weapon history from the game's statistics; the hall stand's
  keepsake (0.4.704) no longer writes the seeding mark either. Both sides' methods are kept.
- **Sleep.** This branch's 0.4.703 sleep mixins and *Neighbours'* container accessor are all registered.
- **Tests that carried an outside thing in** through the front door and looked for it in the drawer straight away now cross into
  the rest of the House first, as 0.4.704 asks, and five of them check that nothing was taken at the front door; what they check is
  otherwise unchanged (`HouseMadeTests`: the stamps, the favourite, stamped since, let fall, another mod's weapon).

## Already in this branch under other commits

- `dev/funny-edison-dmjvs1`: a marker for an earlier head of the lake-house work, recorded on 7 November with no change of
  content; its tree is the commit it came from, which this branch contains.
- `feature/six-personal-scene-touches`, `fix/domestic-paper-review-0.4.142`, `fix/entrance-playtest-0.4.142`,
  `feature/backseat-passing-environment`, `feature/borrasca-four-visits-0.4.108`, `dev/correspondence-0.4.31`,
  `work/character-art-paint-0.4.100`: each was taken in when it was current; what of it is not here word for word was changed by
  later work on the same rooms.
- `labyrinth-light-followup-20260929`: the deeper halls' thinner light of 29 September; the halls' lighting has been rebuilt since.

## Never meant to be merged

- `diagnostics/goatman-late-knock`, `diagnostics/backseat-stall`: probes for a CI failure, since fixed.
- `ci/harrigan-props`, `ci/review-artifact`: one-off workflow experiments.
- `test/goatman-exchange-0497-before`, `test/witch-retreat-0499-before`, `verify/ghosts-call-sheet-0.4.86`: the code as it stood before
  a fix, kept to show the failing case.
- `house-builder-*` (four branches of 27 September): earlier attempts at the House's builder, replaced by the one in use.
- `recovery/ghost-plaque-0.4.133-20261027`: a note left when a session went offline in October; the work it lists was finished in
  later releases.

## Found while testing

- **The falling plaster** (`HallwayVariantsTests.plasterWarnsFallsOnceAndLeavesShelterUnhurt`) failed here as it had on the branch's
  own run (1715): the span fell, but the reader under it was not hurt. A player who has just joined cannot be hurt for sixty ticks,
  and the plaster falls about forty-six ticks after the case's readers join when the room loads at once; the steam and wire cases
  only passed because their rooms took longer to load. The fixture now clears that grace for its readers, as `NotYetTests` already
  does, and the plaster case says what it saw when it fails. Nothing in the game changed.

## Checked

Every case of both branches and of the hall drawers, 77 in all (`HouseMadeTests`, `NotOwnWorldTests`, `NeighbourLandTests`,
`HomeBedTests`, `LeftAloneTests`, `OwnLightTests`, `ArtifactTests`, `HarriganDialogueTests`, `HallwayVariantsTests`,
`HouseKeepingTests`): 71 passed on the merge as it stood; the five drawer cases and the plaster case above failed, and all of them
pass after the changes here, with the rest of the hallway and hall stand cases run again (22 of 22). Compiled, and the quick gate
run, on the merged source.
