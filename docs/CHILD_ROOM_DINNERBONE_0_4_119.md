# The child's room: the shut door, the voices, the trapdoor and Dinnerbone, 0.4.119

The third batch of the owner's October 24 notes, with the texture review the owner asked for along the way. No layout,
protocol or Witness change: 52 sources / 39 resolutions / two kinds / three endings.

## The child's room

- **The House's door goes too.** Once every exit of the room has gone, the doorway in the south wall that leads back to
  the House's own door is wall to each reader who read the card and has not finished: their client is sent birch
  panelling for those two cells, refreshed every second, and the real doorway once they finish (or leave). It is per
  reader: a peer sees whatever their own progress shows them. The real cells stay open on the server.
- **Their voices.** The card says "if you hear us, wait until morning." The reader hears their parents anyway, in grey
  italics with a whisper from the floor: one line as each exit goes, a warning twenty seconds after the room is shut,
  and the last once they have gone under the floor. The voices do not agree with the card, or with each other. Each line
  once, in order, to that reader alone.
- **The trapdoor is found from the bed.** The way down under the bed starts as a floorboard like the rest. Using the bed
  (head or foot) gets the reader down to look under it: one board has a finger hole and lifts, an open spruce trapdoor
  over the existing ladder. It stays open for everyone after.
- **Dinnerbone.** Once the room is shut, a dark figure stands on the ceiling in the far corner. It arrives only while
  nobody can see that corner, and turns to the nearest reader only unseen. It is named Dinnerbone, so the game itself
  draws it the wrong way up.
- **Hanging toys.** The four toys the exits take now hang from the ceiling on a thread a hand's width below it, upside
  down, instead of lying flat against it; the blocks hang as their stacked tower. `tools/generate_nursery_assets.py`
  writes the four hanging models and points the ceiling stage at them.

## Texture review

Every texture added in 0.4.117–0.4.118 was rendered enlarged and on its model and revised where it read badly:

- Séance: the cupboard, side tables and stand use plank grain with tone noise, wavy streaks and oval knots instead of
  flat brown with dash marks; the sitting record's handwriting is uneven brown ink instead of printed bars; the album's
  oval holds a sepia portrait of three sitters instead of vertical bars; the letters carry a postmark.
- Trailer: the chair pine and bunk timber use the same grain; the cushion is a muted tartan with tufting buttons instead
  of a bright grid.

## Tests

- The child's room sequence case checks the floorboard, the trapdoor found from the bed, the doorway shut to the reader
  but not the peer and restored when they finish, the parents' lines, and Dinnerbone on the ceiling.
