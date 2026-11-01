# The Polaroid Trail (0.4.204)

The owner's fourth proposal of 31 October (docs/OWNER_VIGNETTE_PROPOSALS_2026_10_31.md, section 4), built in the confirmed
order after Work print. After Penpal's footsteps, balloons, boxes, screens and friends: every place the reader walked as a
child was being photographed from ten feet away.

Slot 130, `LabyrinthPlace.POLAROID_TRAIL`, a multi-visit literary scene outside (the outside dimension, dusk on the scene clock).
Layout 60, protocol 111. Witness: a new understanding source, **59 sources / 45 required / two kinds / three endings**.

## The place

`PolaroidSite` builds it whole (the polish, review and playtest passes skip it), 53 by 152 blocks, front to back:

- **The yard.** The back of the reader's house (the entry door), a porch with the first balloon tag on its rail (the source),
  a gravel drive, the creek with a half-built pallet raft and a stump, chain-link along both sides and across the far end. The
  only way through the far fence is the culvert's mouth.
- **The culvert.** Corrugated steel into the embankment, ending at a side grate latched from the other side; past it, a scramble
  of earth up to the crawlspace vent.
- **The crawlspace** under Mrs Maggie's house next door: one block high, so the reader is down on hands and knees from the top
  step of the scramble; brick piers, drains, hanging insulation, the cat bowl by the west wall, a bed of stolen clothes in the
  north-east corner, and a hatch in the clay. Under the hatch, a ladder, a brick tunnel north and a ladder up.
- **The cinema.** A store room, a lobby with its concession counter, the auditorium (rows K to A, an aisle light at each row's
  end, velvet down the walls, the screen torn at one corner), the projection booth above the lobby with its open port, and a
  fire exit beside the screen onto an alley.
- **The pines.** A wood on the plateau, two trenches dug side by side with their spoil heaped beside them, a tall pine whose
  branch reaches over the trench on the left, a deer blind with the photo album on its boards (the account) and a lunchbox, and
  at the far end a garden shed whose door is the way on. Spruce hedges close the plateau's edges.

One fitting block, `polaroid_fitting` (20 kinds, stages), and eight materials, all drawn by `tools/draw_polaroid_trail_art.py`;
seven subtitled sounds from `tools/generate_polaroid_trail_audio.py` (charge, flash, shutter, balloon, walkie, projector, power).

## The rule

The watcher is never seen: only his camera, an amber glint from a hiding place while the flash charges (a second and a half,
with a thin rising whine), then the flash. A photograph is taken at that moment from where he stood, by the House's own camera
through the real blocks (`HouseLens.shoot`, the reader and anyone with them as figures), and half a minute later it is left
nearby under a yellow balloon. Whoever picks it up keeps it, whoever it is of; using it holds it up to look at (dithered black
and white in its white border, the caption in pencil under it).

To go on, the reader stands where the photograph was taken of them and looks back at where he was: a beam of dust runs down the
sightline with the shutter's click, and what was shut opens. The proposal had the photograph show the reader thirty seconds
before the flash from a ring of past positions; here it shows the moment of the flash and develops half a minute later, which
keeps the same delay and is something a reader can remember standing in.

The proposal's Photographer (coveralls, a head lost in the flare) is left out on purpose: in this House the watcher is only ever
the glint and the flash, and the last photograph, from straight above, finds nobody in the branches.

Josh, the reader's friend, is only a sepia shape and a flashlight beam pointing the way for a few seconds after each sightline,
fading into fireflies, and his voice over the walkie-talkie (a static ribbon with waveform bars in the action bar, once per line).

## The four visits

1. **The yard.** Josh comes on the walkie as the reader comes out. Wading into the creek brings a flash from inside the culvert;
   the photograph is left on the stump. Standing in the creek again and looking back at the pipe swings its grate open.
2. **The crawlspace.** A while crawling and the camera behind the bricks flashes; the photograph is left by the cat bowl. Looking
   back from there moves the camera to the bed of stolen clothes (one of the shirts is the reader's from the porch); looking back
   from that one draws the bolt on the hatch.
3. **The cinema.** The projector clatters on behind the reader once they are in the auditorium; someone is sitting alone in the
   back row. At the front, four flashes from the booth, each photograph left at the foot of the screen and each captioned with a
   row nearer (K, H, E, B); the one behind moves only while the reader is not looking at them. With all four taken they are in
   row A, right behind the reader; turning round to face them finds the seat empty, the power goes, the aisle lights go out and
   the fire exit stands open.
4. **The pines.** Three flashes from shifting places up in the trees (the boughs are cleared at each for his line of sight), then
   Josh: get in the one on the left, then look up. Down in trench A the last flash comes from straight above, and its photograph
   is the top of the reader's head. Looking straight up from the trench (pitch below -80) sends the beam up into the branches,
   where fifty yellow balloons light and nobody is there. The album is ready in the deer blind; Josh's walkie-talkie is the
   reader's to keep.

An unfinished visit starts again from its beginning on the next arrival; what an earlier visit opened stays open for that
reader (their own `Gates204`, unioned with anyone else in the copy).

## Failure

Looking at the camera as the flash goes off (the reader's look within about twenty degrees of it, nothing between). The visit
goes white (a full-screen flash on that reader's client for three seconds) and the reader is back on the porch with the visit to
do again; its photographs and balloons are taken back. Until they finish the trail, about one ordinary hall in four, once and
after a while in it, there is a click behind them and a photograph of them from behind in their pocket (`HouseLens.fromBehind`).
The album keeps a white page for each time.

## The account and the walkie-talkie

The album, captioned in the reader's own hand under photographs they did not take: one entry for each photograph that reader
picked up, in the order of the trail, the white pages if they were caught, and Josh's walkie-talkie taped in at the back. Read in
the deer blind, it resolves the trail (`looked_up_from_the_trench`, understanding).

Josh's walkie-talkie, kept, is owner-bound: used anywhere in the House it gives Josh's voice and his flashlight beam toward the
way the reader came into the place they are in (ten seconds, then thirty seconds before it can be used again).

## Multiplayer

Each reader has their own watcher, trail and photographs; the scene is one copy for those in it together. Photographs show
everyone in the frame, so in company they hold both readers. Anyone can pick up anyone's photograph, and anyone can stand in
another reader's photographed spot and look back for them (the gate is that reader's, and opens for everyone in the copy). Two
readers photographed on the same visit share a drop: when one picks theirs up, the other's is still there. The shared world
state (gates, the projector, the canopy balloons) is set from everyone present and reset when the copy empties.

## Tests

`PolaroidTrailTests`, six cases in the literary namespace: the tag with the reader's name and every way on shut; the creek, the
photograph and the look back that opens the grate; looking at the flash (back on the porch, the haunting armed, a real
photograph from behind); the crawlspace's bowl and clothes and the hatch; the cinema's four flashes, the one behind in row A and
turning round; the trench, looking up, the walkie-talkie and the album resolving the trail. The Witness cases count 59 / 45.
