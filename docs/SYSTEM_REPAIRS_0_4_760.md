# System repairs, 0.4.760

The owner's review of 24 November recommended systemic repairs before more content, then natural solo and group journeys, then
targeted revisions to pacing, staging, lighting and sound. This release is the first part. Each repair moves an effect to the right
boundary: the world, a scene copy, or one reader.

## Repairs

- **Yard hazards earn their trial mark.** `ExpeditionRhythm.crossedHazard` accepted only doors in the interior dimension, so a
  physical trial standing outside (Downed Line, the flooded yard, the windbreak, the ditch, the storm field) never gave its mark or
  its calm deal. It now accepts any House dimension. A door still counts only if it is that trial's own far door. Case:
  `RhythmTests.aYardHazardsFarDoorEarnsItsMarkToo`.
- **A copied institute keeps its silence.** `StaircaseSoundscape` muted music and cave ambience only inside the original Whalestoe
  institute's bounds. It now finds the institute from the copy band the reader stands in (`SceneInstances.originAt`), as the sky and
  view already do. The lake house's underwater fog had the same fault and gets the same fix.
- **Waxworks feeds and turned heads belong to one museum.** `WaxMonitors.watched(feed)` asked whether anyone anywhere had that feed
  up, so watching a feed in one copy held the figures still in every copy. Each watcher now records the museum they sit in, and
  `watched(site, feed)` asks only that museum. `WaxNight.unturn` also cleared a moved figure's head for every reader in every copy;
  it now clears only for readers in the museum where the figure moved. The monitor case now checks that another copy's same feed is
  not held still.
- **Story pieces in the hand and on the ground.**
  - Facing: a piece turned toward the reader who set it down only when its block was a `HorizontalDirectionalBlock`. Many of the
    mod's blocks carry `horizontal_facing` (or `facing`) without that class, so they kept the facing recorded in their story. Any
    piece with either property now turns toward its placer.
  - Drawing: a piece whose block is drawn by a block entity drew nothing in the hand. It now also draws its block entity's renderer,
    once per state; one that fails to draw is remembered and left to its model.
  - The stack overflow of run 1792 (0.4.759 follow-up, `f21ba1ab`) is the reason the hand never goes back through
    `renderSingleBlock`.

### 0.4.761

- **Pistons leave a set-down piece where it is.** A piece outside the House is remembered by where it stands (`PlacedPieces`), so
  a piston that moved it would leave a piece nobody could break and a record pointing at nothing. Pistons now neither push nor pull
  a story piece (`PiecePistonMixin`); a piston against one stays in. Case: `StoryPiecesTests.aPistonCannotMoveAPieceSetDown` (a
  sticky piston powered, then unpowered, beside a piece the reader set down; the piece stays, is still remembered, and still breaks
  for its reader).
- **Only the feed up on the bank is live.** All six waxworks monitors used to update while only the selected one counted as
  watched, so a reader could watch five galleries at a glance while the House treated them as unwatched. Now only the selected feed
  is drawn, marked LIVE; the other five stand dark with NO FEED. Switching feeds shows black until the new feed's first frame.

### 0.4.762: endings

The owner's next pass is consistency at story endings: does the physical outcome register, does its aftermath have time to land, and
can every surviving reader leave? The group sweep (`GroupSweepTests`) now ends every story with a living reader coming to the end
while the others are still in it. The reader is made ready and reads their account where it lies (the diner, the state route, the
ward's drawer and the foyer give the end by their own act, and there it is given through the reader). Then:

- the account resolves the story for that reader, and for nobody else unless the story is one the others follow as witnesses;
- 200 ticks later the reader is alive and still in the story, not put out before the end has landed;
- the reader can leave, and nobody who could leave before is held because they finished.

What it found:

- **A finished reader held by someone else's hunt.** In the stillness, a later reader passing halfway brings the thing in, and while
  it is in the hall nobody leaves; that held a reader whose story there was already over. The closing hall held the same way while a
  run went on. Both locks now let a reader whose story is complete go (`Stillness.locked`, `ClosingHall.locked`). Case:
  `StillnessTests.aReaderWhoseStoryIsOverIsNotHeldByAnotherReadersHunt`.
- **Being carried out was invisible to the pacing numbers.** A reader taken back to the hall under a fade
  (`LabyrinthDoors.sendBack`) other than by a failure left no record. It now logs `story_sent_back` with whether the story was
  resolved, and the summarizer counts an unresolved one as a story to find again. Case:
  `test_a_reader_carried_out_of_a_story_is_tracked_like_one_who_left`.

The sweep reads each account with the reader made ready; the story's own way to its end (the act, the choice, the fall) stays in
each story's own tests.

## Pacing measurement

`tools/summarize_playtest_log.py` now reports, for each reader, from the opt-in journey log:

- the time spent in each story room, per stay and per story, with the median and longest stay;
- rooms come back to;
- how long a reader took to find an unfinished story again, and which they never found again;
- the minute at which each account was resolved, a curve of progress toward an ending.

Spectators confer nothing, and minutes are played minutes, as for the existing measures. Two cases cover these
(`test_pacing_measures_stays_returns_and_progress`, `test_pacing_ignores_a_spectator`).

0.4.761 corrects three ways these numbers misled:

- a reader who reconnects inside a story (no fresh arrival) resumes the stay from the first row back in it, rather than losing it;
- switching to spectator ends the stay, and spectating minutes are not labyrinth minutes;
- being put out of a story (`story_put_out`) is an unfinished departure, so the time to find it again is measured too.

Cases: `test_a_reconnect_inside_a_story_keeps_both_parts_of_the_stay`, `test_spectating_ends_the_stay`,
`test_a_reader_put_out_of_a_story_is_waiting_to_find_it_again`.

These numbers are what frequency and spacing changes should follow. Room-count spacing alone cannot tell whether a stretch with the
waxworks' twelve-minute shift and The List's nights is too much. The 33-account Witness requirement stays as it is until real
journeys have been measured.

## Multiplayer checks

- **Mixed progress.** Every story's group sweep (`GroupSweepTests`) now ends with a reader who has already resolved the story
  coming in among the others (not in a private story, whose copies nobody shares). Their credit is neither repeated nor lost, none of
  it reaches anyone else, and nobody else's place, life or reading changes.
- **Reconnect during a committed sequence.** The ten locks that hold a reader in (`LiteraryVignettes.retreatLocked` and
  `VignetteGate.exitLocked`) were read for what a logout does to them:
  - kept in saved or shared state, so the lock still holds on return: the Foyer, the Last Stop capture, the closing hall, the
    storeroom, the stillness and The list's nights (which also play on without a slasher who is away);
  - finished on return: the cabin's cut completes at login;
  - reset with the site: Fallow and Last Seen keep their day in memory and reset it once the site has been empty for a while, so a
    reader who comes back late finds a fresh day rather than a held one.

  Only Uncanny, Holloway and the clapping game have a case that logs a reader out under the lock and back in. The other seven are
  the next cases to write, each in its own story's tests, since each lock is reached only by playing that story to it.
- **Leaving before the account is read** is not yet covered.

## Still to do, in the review's order

1. Natural solo and group journeys with logging on: mixed-progress groups, simultaneous copies, a reconnect during a committed
   sequence, and a reader leaving before the account is read.
2. Completion and consequence: audit objective → aftermath → account → departure in every story. Point the reader to the account
   with light, behaviour and placement, without instructions, and make commitment and failure readable.
3. The House around the long stories: entrances and returns through familiar thresholds, scale, restrained sound, and a moment to
   regain bearings.
4. Pivotal moments seen from the reader's position in darkness, extending the actual-client proofs to the older catalogue.

No layout, protocol or Witness change. Version 0.4.762.
