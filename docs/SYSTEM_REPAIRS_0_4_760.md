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

## Pacing measurement

`tools/summarize_playtest_log.py` now reports, for each reader, from the opt-in journey log:

- the time spent in each story room, per stay and per story, with the median and longest stay;
- rooms come back to;
- how long a reader took to find an unfinished story again, and which they never found again;
- the minute at which each account was resolved, a curve of progress toward an ending.

Spectators confer nothing, and minutes are played minutes, as for the existing measures. Two cases cover these
(`test_pacing_measures_stays_returns_and_progress`, `test_pacing_ignores_a_spectator`).

These numbers are what frequency and spacing changes should follow. Room-count spacing alone cannot tell whether a stretch with the
waxworks' twelve-minute shift and The List's nights is too much. The 33-account Witness requirement stays as it is until real
journeys have been measured.

## Still to do, in the review's order

1. Natural solo and group journeys with logging on: mixed-progress groups, simultaneous copies, a reconnect during a committed
   sequence, and a reader leaving before the account is read.
2. Completion and consequence: audit objective → aftermath → account → departure in every story. Point the reader to the account
   with light, behaviour and placement, without instructions, and make commitment and failure readable.
3. The House around the long stories: entrances and returns through familiar thresholds, scale, restrained sound, and a moment to
   regain bearings.
4. Pivotal moments seen from the reader's position in darkness, extending the actual-client proofs to the older catalogue.

No layout, protocol or Witness change. Version 0.4.760.
