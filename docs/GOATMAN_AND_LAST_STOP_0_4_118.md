# Goatman and the Last Stop, 0.4.118

The second batch of the owner's October 24 notes. No layout, protocol or Witness change: 52 sources / 39 resolutions /
two kinds / three endings.

## Goatman

- **Beds.** Stacks stand three blocks apart, west and east, in rows z -57, -60, -63, -66, -69 and -72, clear of the
  side windows (z -59, -67, -73), so an upper bunk never hides a pane the night's sightings depend on. The twelve
  lower beds fill first. Uppers stand on a new `trailer_bunk_frame` block: posts at the outer end, a ladder at the
  foot, side boards under the mattress. Only the posts collide. The cousins' bunk spots follow the new rows.
- **Porch.** The two porch posts now start at the deck and stand on the log sill instead of hovering half a block over
  the slab.
- **Chairs.** The dinette chairs are a new `trailer_chair` block: still a `StairBlock` underneath (seat height, facing
  and the existing seating tests unchanged), drawn as a camp chair with a plaid cushion. All ten table chairs always
  stand, so the far end of the table has its pair; only overflow chairs follow the expected diners.
  `tools/draw_trailer_furniture.py` draws the three materials, the chair and bunk models and both blockstates; CI
  checks them.
- **Walks.** Each cousin has a distinct carriage rather than a small variation of one gait: a skipper, a shuffler, a
  swagger, a stiff marcher, hands in pockets, a timid toe-in walk, a head-first hurrier and a loper. Phase still comes
  from distance walked, and a copied face copies that cousin's walk.
- **Filing in.** Walking to supper, a cousin waits while another walking cousin is just ahead of it, instead of passing
  through. Head-on, the higher index gives way, and no one waits more than a second and a half. The runner's exchange
  in the doorway keeps its own timing.
- **The door first.** In fresh rounds the first knock is on the front door, then a quick run round: one side, the back,
  the other side. The circling body also knocks on the door as it leaves the step on its first lap.
- **Door words wait for a knock.** Until something has knocked on the front door (the Goatman's blows or the runner's
  knuckles), nobody says it is pushing back, that we're not opening it, or anything about knocking.
- **The generator.** The cousin goes for gas the moment the last frank is on its plate; the old clock still sends him if
  nobody serves.

## The Last Stop

- **The elk leaves at a run.** Its way out through the gap and up the lane is smoothed into one curve, run at about
  twice the old pace with faster hoofbeats.
- **The men get out.** A man near the side door opens it if it is shut. A man left inside the bar, after a capture or
  held up by the furniture, goes back out by the side door.
- **They gather.** Once they have stopped asking (the last stage), the men close in on a reader outside, as the herd does
  in the field.
- **Contact ends it.** A single touch from a man or from the herd begins the capture: outside the bar a man's hand
  chooses the men, and a herd body by the gap chooses the herd. Entering the bar or the field still chooses as before.

## Tests

- New Goatman case `nobodyTalksAboutTheDoorUntilSomethingKnocksOnIt`.
- The collision-stall case now checks that the first knock lands on the front door, and counts four surrounding cues.
