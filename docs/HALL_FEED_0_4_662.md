# The feed (0.4.662)

The owner's second suggestion of 16 November, the half for a reader alone: "For solo, a CCTV feed could show the same hall a few seconds
after you left it, with a set of footsteps still going." The half for readers together is 0.4.661 (docs/HEARD_APART_0_4_661.md).

Nothing is written or said. The monitor shows a hall the reader knows, with nobody in it, and footprints coming along the way they
walked. Protocol 196 (one new message to the reader's game). No Witness or layout change. New builds only.

## Now

- **When.** Now and then, when a reader comes alone into an ordinary hall (the domestic and stone halls, not the camp, the stories or
  the staircase), six crossings or more into the House, the hall is watched for them: about one hall in three, and not again for
  fifteen minutes of the world's time after they were last shown it.
- **What their game keeps.** While they are in the watched hall, their own game takes a picture of it once a second, from just under the
  ceiling above the door they came in by, aimed where they are, without them in it. It keeps the way they walked across it.
- **When it is shown.** If they leave the watched hall alone, having been in it at least three seconds, by a door into another ordinary
  hall where they are alone too, a monitor hangs in that hall: on the first wall they face as they come in, between two and seven
  blocks off, or failing that a wall a little to one side, then further round, then behind them. It hangs high, its middle two blocks
  and a little above their feet, on a bracket on a solid wall: never on glass, never where something already hangs, never in a hall
  too low for it. If there is no such wall, nothing is shown and the hall is simply forgotten.
- **What it shows.** The last picture of the hall they left, washed grey like a night camera's, with a clock running in its corner from
  when they left. About three and a half seconds after they left, footprints come into it, one every eight ticks, a stride apart, feet
  turn about, along the last sixteen blocks of the way they walked, out to the door they left by. A new footprint has a little dust about
  it. The monitor gives out a footstep with each, sounding like the floor it fell on, fading.
- **After.** Two seconds after the last footprint the picture breaks up into noise for a second and the screen goes dark. The monitor
  stays, dark, until the reader leaves that hall; then it is gone.
- **Nothing in the world.** The monitor, the picture and the footsteps are drawn and played by that reader's own game. Nothing is placed
  and nothing can be walked into: it hangs above head height.

## With others

For a reader alone. A hall is watched only while nobody else living is in it, and shown only if nobody else was left in it and nobody
else is in the hall the reader comes into. Spectators are never watched. Another reader who comes into the hall later sees no monitor,
since it is only ever in the first reader's game. When each reader was last shown it is their own record.

## Checked

- `ExplorationPassTests.aWatchedHallIsShownOnAMonitorInTheNextHall`, in a built hall and room:
  - The same ordinary halls count as for readers together.
  - A reader at the House's first doors, a spectator and a reader not yet six in are never watched; not with someone else in the hall,
    nor outside the ordinary halls; alone in an ordinary hall, it is watched.
  - The camera hangs under the ceiling, set back towards the door behind the reader.
  - Fifteen minutes before it can be again.
  - The monitor hangs on the wall the reader faces, two above their feet, its screen to them; never on glass, where it goes on a wall a
    little to one side instead; never where something already hangs; never in a hall two high.
  - Too soon out of the hall, with someone left in it, into a hall with someone in it, into the camp or a story, or from a hall that was
    not the watched one: nothing is shown, and the watched hall is forgotten. Alone, out of it into another hall, long enough after: it
    is shown there, and kept as when it last was. Leaving that hall, the monitor is gone.
  - The footprints: a print each stride over only the last sixteen blocks of the way, feet turn about, a stride apart or less across a
    corner, the last pointing the way the reader went; a step or two is no footprints.
- The client proof `HallFeedVisualProof` (`native-hall-feed.png`): the monitor through its production model from the front, from each
  side and from below, with the feed through its production compositor on its screen at four moments (the empty hall, the first
  footprints, all of them, breaking up), and the feed flat. It checks that the model is loaded, that each footprint darkens the picture
  where it falls, and that the clock is lit.
- `tools/draw_hall_feed.py --check`: the textures and the model, and the glass well darker than the bezel round it.
- In play, in the scene review's two real clients (`SceneReviewSocketProof`, the CI job `scene-review-clients`, its last two
  phases): one client walks up the alcove hall with real movement while the server's own tick has it watched, its own game taking
  the pictures, and is then in the offset hall alone, where the server shows it the feed. That client must have taken at least two
  pictures, the last of them not flat; must have three footprints or more from its walk; must see the screen lit, then dark once
  the picture has broken up. It saves the monitor on the wall at four moments (`A-feed-empty`, `A-feed-steps`, `A-feed-all`,
  `A-feed-breaking`), the picture as its game took it (`A-feed-picture`) and the screen as composed at three of those moments
  (`A-feed-screen-*`). Staging: the two halls are built as the House builds them, six crossings are pushed behind the reader, the
  hall is watched for certain rather than one time in three, and the reader is set down in the second hall rather than walking
  through a door to it. The other client stays in the flat, so both halls are the first reader's alone.

## Not covered

- In the scene review the reader is set down in the second hall; coming into it through a door, as a reader does, is covered only by
  the case, which drives the same steps with places of its own.
- When a hall is watched and when the feed is shown are decided by the server's own tick from the readers' places; the case drives the
  same steps directly, with places and times of its own.

## New lines

None. The clock's figures are numbers.
